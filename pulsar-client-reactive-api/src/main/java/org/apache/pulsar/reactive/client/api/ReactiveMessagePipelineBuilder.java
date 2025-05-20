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

package org.apache.pulsar.reactive.client.api;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Builder interface for {@link ReactiveMessagePipeline}.
 *
 * @param <T> the message payload type
 */
public interface ReactiveMessagePipelineBuilder<T> {

	/**
	 * <p>
	 * Sets a handler function that processes messages one-by-one. When the message
	 * handler completes successfully, the message will be acknowledged. When the message
	 * handler emits an error, the error logger will be used to log the error and the
	 * message will be negatively acknowledged. If the error logger is not set, a default
	 * error message will be logged at the error level.
	 * </p>
	 * <p>
	 * NOTE: Be aware that negative acknowledgements on ordered subscription types such as
	 * Exclusive, Failover and Key_Shared typically cause failed messages to be sent to
	 * consumers out of their original order. Negative acknowledgements for Key_Shared
	 * subscriptions may also cause message delivery to be blocked on broker versions
	 * before Pulsar 4.0. To maintain ordered message processing, it is recommended to
	 * wrap the message handler with Project Reactor's native retry logic using <a href=
	 * "https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#retryWhen-reactor.util.retry.Retry-">Mono.retryWhen</a>
	 * to retry processing of each message indefinitely with backoff.
	 * </p>
	 * @param messageHandler a function that takes a message as input and returns an empty
	 * Publisher
	 * @return a builder for the pipeline handling messages one-by-one
	 */
	OneByOneMessagePipelineBuilder<T> messageHandler(Function<Message<T>, Publisher<Void>> messageHandler);

	/**
	 * Sets a handler function that processes the stream of messages.
	 * @param streamingMessageHandler a function that takes a stream of messages as input
	 * and returns a {@link MessageResult} that contains the acknowledgement or negative
	 * acknowledgement value of the processing.
	 * @return the pipeline builder instance
	 */
	ReactiveMessagePipelineBuilder<T> streamingMessageHandler(
			Function<Flux<Message<T>>, Publisher<MessageResult<Void>>> streamingMessageHandler);

	/**
	 * Sets a transform function that can be used to customize the pipeline.
	 * @param transformer a transform function
	 * @return the pipeline builder instance
	 * @see Mono#transform(Function)
	 */
	ReactiveMessagePipelineBuilder<T> transformPipeline(Function<Mono<Void>, Publisher<Void>> transformer);

	/**
	 * Sets a retry spec that will be used in case of failures in the pipeline. The
	 * default is to retry indefinitely with an exponential backoff.
	 * @param pipelineRetrySpec the retry spec
	 * @return the pipeline builder instance
	 * @see Mono#retryWhen(Retry)
	 */
	ReactiveMessagePipelineBuilder<T> pipelineRetrySpec(Retry pipelineRetrySpec);

	/**
	 * Builds the pipeline instance.
	 * @return the pipeline instance
	 */
	ReactiveMessagePipeline build();

	/**
	 * Builder interface for a pipeline that handles messages one-by-one.
	 *
	 * @param <T> the message payload type
	 * @see #messageHandler(Function)
	 */
	interface OneByOneMessagePipelineBuilder<T> extends ReactiveMessagePipelineBuilder<T> {

		/**
		 * Sets the timeout for the message handler function. Defaults to 2 minutes.
		 * @param handlingTimeout the handling timeout value
		 * @return the pipeline builder instance
		 * @see #messageHandler(Function)
		 */
		OneByOneMessagePipelineBuilder<T> handlingTimeout(Duration handlingTimeout);

		/**
		 * Sets a function which will be called when the message handler emits an error.
		 * If not set, a default message will be logged at the error level.
		 * @param errorLogger the error logger function
		 * @return the pipeline builder instance
		 */
		OneByOneMessagePipelineBuilder<T> errorLogger(BiConsumer<Message<T>, Throwable> errorLogger);

		/**
		 * Sets the concurrency for the pipeline. The messages will be dispatched to
		 * concurrent instances of the message handler.
		 * @param concurrency the number of concurrent message handlers
		 * @return a concurrent pipeline builder instance
		 */
		ConcurrentOneByOneMessagePipelineBuilder<T> concurrency(int concurrency);

	}

	/**
	 * Builder interface for a pipeline that handles messages with concurrent one-by-one
	 * messages handlers.
	 *
	 * @param <T> the message payload type
	 * @see #concurrency(int)
	 */
	interface ConcurrentOneByOneMessagePipelineBuilder<T> extends OneByOneMessagePipelineBuilder<T> {

		/**
		 * Sets whether messages with the same key should be sent in order to the same
		 * message handler.
		 * @return the pipeline instance builder
		 */
		ConcurrentOneByOneMessagePipelineBuilder<T> useKeyOrderedProcessing();

		/**
		 * Sets a function to group messages to be sent to the same message handler.
		 * @param groupingFunction the function used to group the messages
		 * @return the pipeline instance builder
		 */
		ConcurrentOneByOneMessagePipelineBuilder<T> groupOrderedProcessing(MessageGroupingFunction groupingFunction);

		/**
		 * Sets a global limit to the number of messages in-flight over all the concurrent
		 * handlers.
		 * @param maxInflight the maximum in-flight messages
		 * @return the pipeline instance builder
		 */
		ConcurrentOneByOneMessagePipelineBuilder<T> maxInflight(int maxInflight);

	}

}
