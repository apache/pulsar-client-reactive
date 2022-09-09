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

import java.util.Objects;

import org.apache.pulsar.client.api.MessageId;

public final class MessageIdStartAtSpec extends StartAtSpec {

	private final MessageId messageId;

	private final boolean inclusive;

	public MessageIdStartAtSpec(final MessageId messageId, final boolean inclusive) {
		this.messageId = messageId;
		this.inclusive = inclusive;
	}

	public MessageId getMessageId() {
		return this.messageId;
	}

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
