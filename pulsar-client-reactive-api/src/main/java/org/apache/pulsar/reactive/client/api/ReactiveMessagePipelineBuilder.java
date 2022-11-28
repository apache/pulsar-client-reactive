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

package org.apache.pulsar.reactive.client.api;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public interface ReactiveMessagePipelineBuilder<T> {

	OneByOneMessagePipelineBuilder<T> messageHandler(Function<Message<T>, Publisher<Void>> messageHandler);

	ReactiveMessagePipelineBuilder<T> streamingMessageHandler(
			Function<Flux<Message<T>>, Publisher<MessageResult<Void>>> streamingMessageHandler);

	ReactiveMessagePipelineBuilder<T> transformPipeline(Function<Mono<Void>, Publisher<Void>> transformer);

	ReactiveMessagePipelineBuilder<T> pipelineRetrySpec(Retry pipelineRetrySpec);

	ReactiveMessagePipeline build();

	interface OneByOneMessagePipelineBuilder<T> extends ReactiveMessagePipelineBuilder<T> {

		OneByOneMessagePipelineBuilder<T> handlingTimeout(Duration handlingTimeout);

		OneByOneMessagePipelineBuilder<T> errorLogger(BiConsumer<Message<T>, Throwable> errorLogger);

		ConcurrentOneByOneMessagePipelineBuilder<T> concurrency(int concurrency);

	}

	interface ConcurrentOneByOneMessagePipelineBuilder<T> extends OneByOneMessagePipelineBuilder<T> {

		ConcurrentOneByOneMessagePipelineBuilder<T> useKeyOrderedProcessing();

		ConcurrentOneByOneMessagePipelineBuilder<T> groupOrderedProcessing(MessageGroupingFunction groupingFunction);

		ConcurrentOneByOneMessagePipelineBuilder<T> maxInflight(int maxInflight);

	}

}
