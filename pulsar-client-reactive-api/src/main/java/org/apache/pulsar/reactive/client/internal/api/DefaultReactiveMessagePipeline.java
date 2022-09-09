/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.reactive.client.internal.api;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageGroupingFunction;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.retry.Retry;

class DefaultReactiveMessagePipeline<T> implements ReactiveMessagePipeline {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultReactiveMessagePipeline.class);

	private static final String INFLIGHT_LIMITER_CONTEXT_KEY = DefaultReactiveMessagePipelineBuilder.class.getName()
			+ ".INFLIGHT_LIMITER_CONTEXT_KEY";

	private final AtomicReference<Disposable> killSwitch = new AtomicReference<>();

	private final Mono<Void> pipeline;

	private final Function<Message<T>, Mono<Void>> messageHandler;

	private final BiConsumer<Message<T>, Throwable> errorLogger;

	private final Retry pipelineRetrySpec;

	private final Duration handlingTimeout;

	private final Function<Flux<Message<T>>, Flux<MessageResult<Void>>> streamingMessageHandler;

	private final int concurrency;

	private final int maxInflight;

	private final MessageGroupingFunction groupingFunction;

	DefaultReactiveMessagePipeline(ReactiveMessageConsumer<T> messageConsumer,
			Function<Message<T>, Mono<Void>> messageHandler, BiConsumer<Message<T>, Throwable> errorLogger,
			Retry pipelineRetrySpec, Duration handlingTimeout, Function<Mono<Void>, Mono<Void>> transformer,
			Function<Flux<Message<T>>, Flux<MessageResult<Void>>> streamingMessageHandler,
			MessageGroupingFunction groupingFunction, int concurrency, int maxInflight) {
		this.messageHandler = messageHandler;
		this.errorLogger = errorLogger;
		this.pipelineRetrySpec = pipelineRetrySpec;
		this.handlingTimeout = handlingTimeout;
		this.streamingMessageHandler = streamingMessageHandler;
		this.groupingFunction = groupingFunction;
		this.concurrency = concurrency;
		this.maxInflight = maxInflight;
		this.pipeline = messageConsumer.consumeMessages(this::createMessageConsumer).then().transform(transformer)
				.transform(this::decoratePipeline);
	}

	private Mono<Void> decorateMessageHandler(Mono<Void> messageHandler) {
		if (this.handlingTimeout != null) {
			messageHandler = messageHandler.timeout(this.handlingTimeout);
		}
		if (this.maxInflight > 0) {
			messageHandler = messageHandler.transformDeferredContextual((original, context) -> {
				InflightLimiter inflightLimiter = context.get(INFLIGHT_LIMITER_CONTEXT_KEY);
				return inflightLimiter.transform(original);
			});
		}
		return messageHandler;
	}

	private Mono<Void> decoratePipeline(Mono<Void> pipeline) {
		if (this.maxInflight > 0) {
			Mono<Void> finalPipeline = pipeline;
			pipeline = Mono.using(() -> new InflightLimiter(this.maxInflight),
					(inflightLimiter) -> (finalPipeline)
							.contextWrite(Context.of(INFLIGHT_LIMITER_CONTEXT_KEY, inflightLimiter)),
					InflightLimiter::dispose);
		}
		if (this.pipelineRetrySpec != null) {
			return pipeline.retryWhen(this.pipelineRetrySpec);
		}
		else {
			return pipeline;
		}
	}

	private Flux<MessageResult<Void>> createMessageConsumer(Flux<Message<T>> messageFlux) {
		if (this.messageHandler != null) {
			if (this.streamingMessageHandler != null) {
				throw new IllegalStateException(
						"messageHandler and streamingMessageHandler cannot be set at the same time.");
			}
			if (this.concurrency > 1) {
				if (this.groupingFunction != null) {
					return GroupOrderedMessageProcessors.processGroupsInOrderConcurrently(messageFlux,
							this.groupingFunction, this::handleMessage, Schedulers.parallel(), this.concurrency);
				}
				else {
					return messageFlux.flatMap((message) -> handleMessage(message).subscribeOn(Schedulers.parallel()),
							this.concurrency);
				}
			}
			else {
				return messageFlux.concatMap(this::handleMessage);
			}
		}
		else {
			return Objects.requireNonNull(this.streamingMessageHandler,
					"streamingMessageHandler or messageHandler must be set").apply(messageFlux);
		}
	}

	private Mono<MessageResult<Void>> handleMessage(Message<T> message) {
		return this.messageHandler.apply(message).transform(this::decorateMessageHandler)
				.thenReturn(MessageResult.acknowledge(message.getMessageId())).onErrorResume((throwable) -> {
					if (this.errorLogger != null) {
						try {
							this.errorLogger.accept(message, throwable);
						}
						catch (Exception ex) {
							LOG.error("Error in calling error logger", ex);
						}
					}
					else {
						LOG.error("Message handling for message id {} failed.", message.getMessageId(), throwable);
					}
					// TODO: nack doesn't work for batch messages due to Pulsar bugs
					return Mono.just(MessageResult.negativeAcknowledge(message.getMessageId()));
				});
	}

	@Override
	public ReactiveMessagePipeline start() {
		if (this.killSwitch.get() != null) {
			throw new IllegalStateException("Message handler is already running.");
		}
		Disposable disposable = this.pipeline.subscribe(null, this::logError, this::logUnexpectedCompletion);
		if (!this.killSwitch.compareAndSet(null, disposable)) {
			disposable.dispose();
			throw new IllegalStateException("Message handler was already running.");
		}
		return this;
	}

	private void logError(Throwable throwable) {
		LOG.error("ReactiveMessageHandler was unexpectedly terminated.", throwable);
	}

	private void logUnexpectedCompletion() {
		if (isRunning()) {
			LOG.error("ReactiveMessageHandler was unexpectedly completed.");
		}
	}

	@Override
	public ReactiveMessagePipeline stop() {
		Disposable disposable = this.killSwitch.getAndSet(null);
		if (disposable != null) {
			disposable.dispose();
		}
		return this;
	}

	@Override
	public boolean isRunning() {
		return this.killSwitch.get() != null;
	}

	@Override
	public void close() throws Exception {
		stop();
	}

}
