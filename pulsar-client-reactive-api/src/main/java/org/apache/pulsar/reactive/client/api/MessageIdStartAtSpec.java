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

import java.util.Objects;

import org.apache.pulsar.client.api.MessageId;

/**
 * Spec for a {@link ReactiveMessageReader} to start reading from a given message id.
 */
public final class MessageIdStartAtSpec extends StartAtSpec {

	private final MessageId messageId;

	private final boolean inclusive;

	/**
	 * Contructs a {@link MessageIdStartAtSpec}.
	 * @param messageId the message id from which to start reading from
	 * @param inclusive true to include the message with the message id
	 */
	public MessageIdStartAtSpec(final MessageId messageId, final boolean inclusive) {
		this.messageId = messageId;
		this.inclusive = inclusive;
	}

	/**
	 * Gets the message id from which to start reading from.
	 * @return the message id from which to start reading from
	 */
	public MessageId getMessageId() {
		return this.messageId;
	}

	/**
	 * Gets whether the message with the message id must be included.
	 * @return true if the message with the message id must be included
	 */
	public boolean isInclusive() {
		return this.inclusive;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MessageIdStartAtSpec that = (MessageIdStartAtSpec) o;
		return (this.inclusive == that.inclusive && Objects.equals(this.messageId, that.messageId));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messageId, this.inclusive);
	}

	@Override
	public String toString() {
		return ("MessageIdStartAtSpec{" + "messageId=" + this.messageId + ", inclusive=" + this.inclusive + '}');
	}

}
