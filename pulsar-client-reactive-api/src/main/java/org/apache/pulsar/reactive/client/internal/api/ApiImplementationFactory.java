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
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipelineBuilder;

public final class ApiImplementationFactory {

	private ApiImplementationFactory() {

	}

	public static <T> MessageResult<T> acknowledge(MessageId messageId, T value) {
		return new DefaultMessageResult<>(messageId, true, value);
	}

	public static <T> MessageResult<T> negativeAcknowledge(MessageId messageId, T value) {
		return new DefaultMessageResult<T>(messageId, false, value);
	}

	public static MessageResult<Void> acknowledge(MessageId messageId) {
		return new EmptyMessageResult(messageId, true);
	}

	public static MessageResult<Void> negativeAcknowledge(MessageId messageId) {
		return new EmptyMessageResult(messageId, false);
	}

	public static <T> MessageSpecBuilder<T> createMessageSpecBuilder(T value) {
		return new DefaultMessageSpecBuilder<T>().value(value);
	}

	public static <T> MessageSpec<T> createValueOnlyMessageSpec(T value) {
		return new ValueOnlyMessageSpec<>(value);
	}

	public static <T> ReactiveMessagePipelineBuilder<T> createReactiveMessageHandlerPipelineBuilder(
			ReactiveMessageConsumer<T> messageConsumer) {
		return new DefaultReactiveMessagePipelineBuilder<>(messageConsumer);
	}

}
