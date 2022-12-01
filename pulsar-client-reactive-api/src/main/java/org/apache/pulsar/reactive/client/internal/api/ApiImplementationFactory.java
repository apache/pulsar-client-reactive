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

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.MessageSpecBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipeline;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipelineBuilder;

/**
 * Internal API implementation.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public final class ApiImplementationFactory {

	private ApiImplementationFactory() {

	}

	/**
	 * Returns the processed value and signals that the message must be acknowledged.
	 * @param <T> the type of the processed value
	 * @param messageId the id of the message to acknowledge
	 * @param value the processed value
	 * @return the result of the message processing
	 */
	public static <T> MessageResult<T> acknowledge(MessageId messageId, T value) {
		return new DefaultMessageResult<>(messageId, true, value);
	}

	/**
	 * Returns the processed value and signals that the message must be negatively
	 * acknowledged.
	 * @param <T> the type of the processed value
	 * @param messageId the id of the message to negatively acknowledge
	 * @param value the processed value
	 * @return the result of the message processing
	 */
	public static <T> MessageResult<T> negativeAcknowledge(MessageId messageId, T value) {
		return new DefaultMessageResult<T>(messageId, false, value);
	}

	/**
	 * Returns an empty value and signals that the message must be acknowledged.
	 * @param messageId the id of the message to acknowledge
	 * @return the result of the message processing
	 */
	public static MessageResult<Void> acknowledge(MessageId messageId) {
		return new EmptyMessageResult(messageId, true);
	}

	/**
	 * Returns an empty value and signals that the message must be negatively
	 * acknowledged.
	 * @param messageId the id of the message to negatively acknowledge
	 * @return the result of the message processing
	 */
	public static MessageResult<Void> negativeAcknowledge(MessageId messageId) {
		return new EmptyMessageResult(messageId, false);
	}

	/**
	 * Creates a message spec builder from a value.
	 * @param <T> the message payload type
	 * @param value the value to create the message spec builder from
	 * @return the message spec builder
	 */
	public static <T> MessageSpecBuilder<T> createMessageSpecBuilder(T value) {
		return new DefaultMessageSpecBuilder<T>().value(value);
	}

	/**
	 * Creates a message spec from a value.
	 * @param <T> the message payload type
	 * @param value the value to create the message spec from
	 * @return the message spec
	 */
	public static <T> MessageSpec<T> createValueOnlyMessageSpec(T value) {
		return new ValueOnlyMessageSpec<>(value);
	}

	/**
	 * Creates a builder for building a {@link ReactiveMessagePipeline}.
	 * @param <T> the message payload type
	 * @param messageConsumer reactive consumer used by the pipeline
	 * @return a builder for building a {@link ReactiveMessagePipeline}
	 */
	public static <T> ReactiveMessagePipelineBuilder<T> createReactiveMessageHandlerPipelineBuilder(
			ReactiveMessageConsumer<T> messageConsumer) {
		return new DefaultReactiveMessagePipelineBuilder<>(messageConsumer);
	}

}
