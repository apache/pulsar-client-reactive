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

/**
 * Message result holding only the id of the message to acknowledge or negatively
 * acknowledge.
 *
 * @author Lari Hotari
 */
class EmptyMessageResult implements MessageResult<Void> {

	private final MessageId messageId;

	private final boolean acknowledgeMessage;

	EmptyMessageResult(MessageId messageId, boolean acknowledgeMessage) {
		this.messageId = messageId;
		this.acknowledgeMessage = acknowledgeMessage;
	}

	@Override
	public MessageId getMessageId() {
		return this.messageId;
	}

	@Override
	public boolean isAcknowledgeMessage() {
		return this.acknowledgeMessage;
	}

	@Override
	public Void getValue() {
		return null;
	}

}
