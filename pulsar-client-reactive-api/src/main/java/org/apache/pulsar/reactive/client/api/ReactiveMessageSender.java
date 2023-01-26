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

import org.apache.pulsar.client.api.MessageId;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive message sender interface.
 *
 * @param <T> the message payload type.
 */
public interface ReactiveMessageSender<T> {

	/**
	 * Send one message.
	 * @param messageSpec the spec of the message to send
	 * @return a publisher that will emit one message id and complete
	 */
	Mono<MessageId> sendOne(MessageSpec<T> messageSpec);

	/**
	 * Send multiple messages and get the sending results in the same order as the
	 * messages sent. The results are {@link MessageSendResult} objects composed of the
	 * message ID of the message sent and of the original message spec that was sent. A
	 * {@code correlationMetadata} can be attached to a {@link MessageSpec} and retrieved
	 * with {@link MessageSendResult#getCorrelationMetadata()} to correlate the messages
	 * sent with the results. A send error will terminate the returned Flux with an error
	 * that is wrapped in a {@link ReactiveMessageSendingException} where the
	 * {@link ReactiveMessageSendingException#getMessageSpec()} method will return the
	 * MessageSpec sent as input.
	 * @param messageSpecs the specs of the messages to send
	 * @return a publisher that will emit a {@link MessageSendResult} per message
	 * successfully sent in the order that they have been sent
	 */
	Flux<MessageSendResult<T>> sendMany(Publisher<MessageSpec<T>> messageSpecs);

}
