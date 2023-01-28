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

import java.util.Objects;

import org.apache.pulsar.client.api.MessageId;

/**
 * Result of a message sending. Holds the spec of the message sent and the assigned
 * message ID.
 *
 * @param <T> the type of the message
 */
public class MessageSendResult<T> {

	private final MessageId messageId;

	private final MessageSpec<T> messageSpec;

	private final Throwable exception;

	/**
	 * Creates a new instance.
	 * @param messageId the ID assigned to the message
	 * @param messageSpec the message spec that was sent
	 * @param exception the exception if any
	 */
	public MessageSendResult(MessageId messageId, MessageSpec<T> messageSpec, Throwable exception) {
		this.messageId = messageId;
		this.messageSpec = messageSpec;
		this.exception = exception;
	}

	/**
	 * Gets the ID assigned to the message.
	 * @return the ID assigned to the message.
	 */
	public MessageId getMessageId() {
		return this.messageId;
	}

	/**
	 * Gets the message spec that was sent.
	 * @return the message spec that was sent
	 */
	public MessageSpec<T> getMessageSpec() {
		return this.messageSpec;
	}

	/**
	 * Gets the exception if any.
	 * @return the exception if any
	 */
	public Throwable getException() {
		return this.exception;
	}

	/**
	 * Gets the correlation metadata of the message spec that was sent.
	 * @param <C> the correlation metadata
	 * @return the correlation metadata
	 */
	public <C> C getCorrelationMetadata() {
		return this.messageSpec.getCorrelationMetadata();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MessageSendResult<?> that = (MessageSendResult<?>) o;
		return Objects.equals(this.messageId, that.messageId) && Objects.equals(this.messageSpec, that.messageSpec);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messageId, this.messageSpec);
	}

}
