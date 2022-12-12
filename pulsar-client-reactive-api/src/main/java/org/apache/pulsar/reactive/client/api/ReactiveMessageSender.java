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
import reactor.util.function.Tuple2;

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
	 * Send multiple messages and get the associated message ids in the same order as the
	 * messages sent.
	 * @param messageSpecs the specs of the messages to send
	 * @return a publisher that will emit a message id per message successfully sent in
	 * the order that they have been sent
	 */
	Flux<MessageId> sendMany(Publisher<MessageSpec<T>> messageSpecs);

	/**
	 * Send multiple messages and correlate the resulted message ids to a provided
	 * correlation key. The correlation key can be any type of object.
	 * @param tuplesOfCorrelationKeyAndMessageSpec a publisher where the element is a
	 * tuple of the correlation key and the specs of the message to send
	 * @param <K> type of correlation key
	 * @return a publisher that will emit a tuple of the provided correlation key and the
	 * message id for each message successfully sent
	 */
	<K> Flux<Tuple2<K, MessageId>> sendManyCorrelated(
			Publisher<Tuple2<K, MessageSpec<T>>> tuplesOfCorrelationKeyAndMessageSpec);

}
