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

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.ReaderBuilder;

/**
 * Spec to specify to a {@link ReactiveMessageReader} from where to start reading.
 *
 * @see ReaderBuilder#startMessageId(MessageId)
 * @see ReaderBuilder#startMessageFromRollbackDuration(long, TimeUnit)
 */
public abstract class StartAtSpec {

	private static final MessageIdStartAtSpec EARLIEST = ofMessageId(MessageId.earliest, true);

	private static final MessageIdStartAtSpec LATEST = ofMessageId(MessageId.latest, false);

	private static final MessageIdStartAtSpec LATEST_INCLUSIVE = ofMessageId(MessageId.latest, true);

	StartAtSpec() {
	}

	/**
	 * Gets a spec to start from the oldest message available in the topic.
	 * @return the start spec
	 * @see MessageId#earliest
	 */
	public static MessageIdStartAtSpec ofEarliest() {
		return EARLIEST;
	}

	/**
	 * Gets a spec to start from the next message that will be available in the topic.
	 * @return the start spec
	 * @see MessageId#latest
	 */
	public static MessageIdStartAtSpec ofLatest() {
		return LATEST;
	}

	/**
	 * Gets a spec to start from the most recently published message in the topic,
	 * including that message.
	 * @return the start spec
	 * @see MessageId#latest
	 * @see ReaderBuilder#startMessageIdInclusive()
	 */
	public static MessageIdStartAtSpec ofLatestInclusive() {
		return LATEST_INCLUSIVE;
	}

	/**
	 * Gets a spec to start from a message id.
	 * @param messageId the message id to start from
	 * @param inclusive whether the message with the message id should be included
	 * @return the start spec
	 * @see ReaderBuilder#startMessageId(MessageId)
	 * @see ReaderBuilder#startMessageIdInclusive()
	 */
	public static MessageIdStartAtSpec ofMessageId(MessageId messageId, boolean inclusive) {
		return new MessageIdStartAtSpec(messageId, inclusive);
	}

	/**
	 * Gets a spec to start from a message id, not including that message.
	 * @param messageId the message id to start from
	 * @return the start spec
	 * @see ReaderBuilder#startMessageId(MessageId)
	 */
	public static MessageIdStartAtSpec ofMessageId(MessageId messageId) {
		return ofMessageId(messageId, false);
	}

	/**
	 * Gets a spec to start from an instant in time.
	 * @param instant the instant to start from
	 * @return the start spec
	 * @see ReaderBuilder#startMessageFromRollbackDuration(long, TimeUnit)
	 */
	public static InstantStartAtSpec ofInstant(Instant instant) {
		return new InstantStartAtSpec(instant);
	}

}
