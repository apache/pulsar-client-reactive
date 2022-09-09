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

import java.time.Instant;

import org.apache.pulsar.client.api.MessageId;

public abstract class StartAtSpec {

	private static final MessageIdStartAtSpec EARLIEST = ofMessageId(MessageId.earliest, true);

	private static final MessageIdStartAtSpec LATEST = ofMessageId(MessageId.latest, false);

	private static final MessageIdStartAtSpec LATEST_INCLUSIVE = ofMessageId(MessageId.latest, true);

	StartAtSpec() {
	}

	public static MessageIdStartAtSpec ofEarliest() {
		return EARLIEST;
	}

	public static MessageIdStartAtSpec ofLatest() {
		return LATEST;
	}

	public static MessageIdStartAtSpec ofLatestInclusive() {
		return LATEST_INCLUSIVE;
	}

	public static MessageIdStartAtSpec ofMessageId(MessageId messageId, boolean inclusive) {
		return new MessageIdStartAtSpec(messageId, inclusive);
	}

	public static MessageIdStartAtSpec ofMessageId(MessageId messageId) {
		return ofMessageId(messageId, false);
	}

	public static InstantStartAtSpec ofInstant(Instant instant) {
		return new InstantStartAtSpec(instant);
	}

}
