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

import org.apache.pulsar.client.api.MessageId;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveMessageSender<T> {

	/**
	 * Send one message.
	 * @param messageSpec the spec of the message to send
	 * @return a publisher that will emit one message id and complete
	 */
	Mono<MessageId> sendOne(MessageSpec<T> messageSpec);

	/**
	 * Send multiple messages and get the associated message ids in the same order as the
	 * sent messages.
	 * @param messageSpecs the specs of the messages to send
	 * @return a publisher that will emit a message id per message successfully sent in
	 * the order that they have been sent
	 */
	Flux<MessageId> sendMany(Publisher<MessageSpec<T>> messageSpecs);

}
