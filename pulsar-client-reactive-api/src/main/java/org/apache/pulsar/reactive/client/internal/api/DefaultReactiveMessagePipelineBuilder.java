/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.reactive.client.internal.api;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageGroupingFunction;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipeline;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipelineBuilder;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

class DefaultReactiveMessagePipelineBuilder<T>
		implements ReactiveMessagePipelineBuilder.ConcurrentOneByOneMessagePipelineBuilder<T> {

	private static final MessageGroupingFunction KEY_ORDERED_GROUPING_FUNCTION;

	static {
		Iterator<MessageGroupingFunction> groupingFunctionIterator = ServiceLoader.load(MessageGroupingFunction.class)
				.iterator();
		if (groupingFunctionIterator.hasNext()) {
			KEY_ORDERED_GROUPING_FUNCTION = groupingFunctionIterator.next();
		}
		else {
			KEY_ORDERED_GROUPING_FUNCTION = null;
		}
	}

	private final Logger LOG = LoggerFactory.getLogger(DefaultReactiveMessagePipelineBuilder.class);

	private final ReactiveMessageConsumer<T> messageConsumer;

	private Function<Message<T>, Publisher<Void>> messageHandler;

	private BiConsumer<Message<T>, Throwable> errorLogger;

	private Retry pipelineRetrySpec = Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
			.maxBackoff(Duration.ofMinutes(1))
			.doBeforeRetry((retrySignal) -> this.LOG.error(
					"Message handler pipeline failed." + "Retrying to start message handler pipeline, retry #{}",
					retrySignal.totalRetriesInARow(), retrySignal.failure()));

	private Duration handlingTimeout = Duration.ofSeconds(120);

	private Function<Mono<Void>, Publisher<Void>> transformer = (it) -> it;

	private Function<Flux<Message<T>>, Publisher<MessageResult<Void>>> streamingMessageHandler;

	private int concurrency;

	private int maxInflight;

	private MessageGroupingFunction groupingFunction;

	DefaultReactiveMessagePipelineBuilder(ReactiveMessageConsumer<T> messageConsumer) {
		this.messageConsumer = messageConsumer;
	}

	@Override
	public OneByOneMessagePipelineBuilder<T> messageHandler(Function<Message<T>, Publisher<Void>> messageHandler) {
		this.messageHandler = messageHandler;
		return this;
	}

	@Override
	public ReactiveMessagePipelineBuilder<T> streamingMessageHandler(
			Function<Flux<Message<T>>, Publisher<MessageResult<Void>>> streamingMessageHandler) {
		this.streamingMessageHandler = streamingMessageHandler;
		return this;
	}

	@Override
	public OneByOneMessagePipelineBuilder<T> errorLogger(BiConsumer<Message<T>, Throwable> errorLogger) {
		this.errorLogger = errorLogger;
		return this;
	}

	@Override
	public ConcurrentOneByOneMessagePipelineBuilder<T> useKeyOrderedProcessing() {
		Objects.requireNonNull(KEY_ORDERED_GROUPING_FUNCTION,
				"MessageGroupingFunction to use for key ordered processing wasn't found by service loader.");
		groupOrderedProcessing(KEY_ORDERED_GROUPING_FUNCTION);
		return this;
	}

	@Override
	public ConcurrentOneByOneMessagePipelineBuilder<T> groupOrderedProcessing(
			MessageGroupingFunction groupingFunction) {
		this.groupingFunction = groupingFunction;
		return this;
	}

	@Override
	public ConcurrentOneByOneMessagePipelineBuilder<T> concurrency(int concurrency) {
		this.concurrency = concurrency;
		return this;
	}

	@Override
	public ConcurrentOneByOneMessagePipelineBuilder<T> maxInflight(int maxInflight) {
		this.maxInflight = maxInflight;
		return this;
	}

	@Override
	public OneByOneMessagePipelineBuilder<T> handlingTimeout(Duration handlingTimeout) {
		this.handlingTimeout = handlingTimeout;
		return this;
	}

	@Override
	public ReactiveMessagePipelineBuilder<T> pipelineRetrySpec(Retry pipelineRetrySpec) {
		this.pipelineRetrySpec = pipelineRetrySpec;
		return this;
	}

	@Override
	public ReactiveMessagePipelineBuilder<T> transformPipeline(Function<Mono<Void>, Publisher<Void>> transformer) {
		this.transformer = transformer;
		return this;
	}

	@Override
	public ReactiveMessagePipeline build() {
		if (this.messageHandler != null && this.streamingMessageHandler != null) {
			throw new IllegalStateException(
					"messageHandler and streamingMessageHandler cannot be set at the same time.");
		}
		if (this.messageHandler == null && this.streamingMessageHandler == null) {
			throw new NullPointerException("messageHandler or streamingMessageHandler must be set.");
		}
		return new DefaultReactiveMessagePipeline<>(this.messageConsumer, this.messageHandler, this.errorLogger,
				this.pipelineRetrySpec, this.handlingTimeout, this.transformer, this.streamingMessageHandler,
				this.groupingFunction, this.concurrency, this.maxInflight);
	}

}
