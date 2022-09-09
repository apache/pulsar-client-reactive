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

import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveMessageConsumer<T> {

	<R> Mono<R> consumeMessage(Function<Mono<Message<T>>, Mono<MessageResult<R>>> messageHandler);

	<R> Flux<R> consumeMessages(Function<Flux<Message<T>>, Flux<MessageResult<R>>> messageHandler);

	/**
	 * Creates the Pulsar Consumer and immediately closes it. This is useful for creating
	 * the Pulsar subscription that is related to the consumer. Nothing happens unless the
	 * returned Mono is subscribed.
	 * @return a Mono for consuming nothing
	 */
	Mono<Void> consumeNothing();

}
