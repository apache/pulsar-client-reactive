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
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactorMessageConsumer<T> implements ReactiveMessageConsumer<T> {

	private final ReactiveMessageConsumer<T> delegate;

	public ReactorMessageConsumer(ReactiveMessageConsumer<T> delegate) {
		this.delegate = delegate;
	}

	public <R> Mono<R> consumeOne(Function<Mono<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return Mono.from(this.delegate.consume((messages) -> messageHandler.apply(messages.next())));
	}

	@Override
	public <R> Flux<R> consume(Function<Flux<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return Flux.from(this.delegate.consume(messageHandler));
	}

	@Override
	public Mono<Void> consumeNothing() {
		return Mono.from(this.delegate.consumeNothing());
	}

}
