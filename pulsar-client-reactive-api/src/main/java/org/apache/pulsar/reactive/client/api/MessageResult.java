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

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.reactive.client.internal.api.ApiImplementationFactory;

public interface MessageResult<T> {

	static <T> MessageResult<T> acknowledge(MessageId messageId, T value) {
		return ApiImplementationFactory.acknowledge(messageId, value);
	}

	static <T> MessageResult<T> negativeAcknowledge(MessageId messageId, T value) {
		return ApiImplementationFactory.negativeAcknowledge(messageId, value);
	}

	static MessageResult<Void> acknowledge(MessageId messageId) {
		return ApiImplementationFactory.acknowledge(messageId);
	}

	static MessageResult<Void> negativeAcknowledge(MessageId messageId) {
		return ApiImplementationFactory.negativeAcknowledge(messageId);
	}

	static <V> MessageResult<Void> acknowledge(Message<V> message) {
		return acknowledge(message.getMessageId());
	}

	static <V> MessageResult<Void> negativeAcknowledge(Message<V> message) {
		return ApiImplementationFactory.negativeAcknowledge(message.getMessageId());
	}

	static <V> MessageResult<Message<V>> acknowledgeAndReturn(Message<V> message) {
		return acknowledge(message.getMessageId(), message);
	}

	boolean isAcknowledgeMessage();

	MessageId getMessageId();

	T getValue();

}
