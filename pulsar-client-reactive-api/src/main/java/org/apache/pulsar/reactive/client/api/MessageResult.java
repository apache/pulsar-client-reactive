/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.reactive.client.api;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.reactive.client.internal.api.ApiImplementationFactory;

/**
 * Result of a message processing.
 *
 * @param <T> the message payload type
 */
public interface MessageResult<T> {

	/**
	 * Returns the processed value and signals that the message must be acknowledged.
	 * @param <T> the type of the processed value
	 * @param messageId the id of the message to acknowledge
	 * @param value the processed value
	 * @return the result of the message processing
	 */
	static <T> MessageResult<T> acknowledge(MessageId messageId, T value) {
		return ApiImplementationFactory.acknowledge(messageId, value);
	}

	/**
	 * Returns the processed value and signals that the message must be negatively
	 * acknowledged.
	 * @param <T> the type of the processed value
	 * @param messageId the id of the message to negatively acknowledge
	 * @param value the processed value
	 * @return the result of the message processing
	 */
	static <T> MessageResult<T> negativeAcknowledge(MessageId messageId, T value) {
		return ApiImplementationFactory.negativeAcknowledge(messageId, value);
	}

	/**
	 * Returns an empty value and signals that the message must be acknowledged.
	 * @param messageId the id of the message to acknowledge
	 * @return the result of the message processing
	 */
	static MessageResult<Void> acknowledge(MessageId messageId) {
		return ApiImplementationFactory.acknowledge(messageId);
	}

	/**
	 * Returns an empty value and signals that the message must be negatively
	 * acknowledged.
	 * @param messageId the id of the message to negatively acknowledge
	 * @return the result of the message processing
	 */
	static MessageResult<Void> negativeAcknowledge(MessageId messageId) {
		return ApiImplementationFactory.negativeAcknowledge(messageId);
	}

	/**
	 * Returns an empty value and signals that the message must be acknowledged.
	 * @param <V> the type of message payload
	 * @param message the message to acknowledge
	 * @return the result of the message processing
	 */
	static <V> MessageResult<Void> acknowledge(Message<V> message) {
		return acknowledge(message.getMessageId());
	}

	/**
	 * Returns an empty value and signals that the message must be negatively
	 * acknowledged.
	 * @param <V> the type of message payload
	 * @param message the message to negatively acknowledge
	 * @return the result of the message processing
	 */
	static <V> MessageResult<Void> negativeAcknowledge(Message<V> message) {
		return negativeAcknowledge(message.getMessageId());
	}

	/**
	 * Returns a message and signals that it must be acknowledged.
	 * @param <V> the type of message payload
	 * @param message the message
	 * @return the result of the message processing
	 */
	static <V> MessageResult<Message<V>> acknowledgeAndReturn(Message<V> message) {
		return acknowledge(message.getMessageId(), message);
	}

	/**
	 * Gets whether the message must be acknowledged.
	 * @return true if the message must be acknowledged
	 */
	boolean isAcknowledgeMessage();

	/**
	 * Gets the id of the message to acknowledge or negatively acknowledge.
	 * @return the id of the message
	 */
	MessageId getMessageId();

	/**
	 * Gets the value processed by the message processing.
	 * @return the value
	 */
	T getValue();

}
