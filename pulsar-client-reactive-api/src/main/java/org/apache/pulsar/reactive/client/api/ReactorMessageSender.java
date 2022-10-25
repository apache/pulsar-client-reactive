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

public class ReactorMessageSender<T> implements ReactiveMessageSender<T> {

	private final ReactiveMessageSender<T> delegate;

	public ReactorMessageSender(ReactiveMessageSender<T> delegate) {
		this.delegate = delegate;
	}

	public Mono<MessageId> send(MessageSpec<T> messageSpec) {
		return send(Mono.just(messageSpec)).single();
	}

	@Override
	public Flux<MessageId> send(Publisher<MessageSpec<T>> messageSpecs) {
		return Flux.from(this.delegate.send(messageSpecs));
	}

}
