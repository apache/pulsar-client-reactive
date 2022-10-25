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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.apache.pulsar.client.api.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class RxJavaMessageConsumer<T> implements ReactiveMessageConsumer<T> {

	private final ReactiveMessageConsumer<T> delegate;

	public RxJavaMessageConsumer(ReactiveMessageConsumer<T> delegate) {
		this.delegate = delegate;
	}

	public <R> Single<R> consumeOne(Function<Single<Message<T>>, Single<MessageResult<R>>> messageHandler) {
		return Single.fromPublisher(
				this.delegate.consume((messages) -> messageHandler.apply(Single.fromPublisher(messages)).toFlowable()));
	}

	@Override
	public <R> Flowable<R> consume(Function<Flux<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return Flowable.fromPublisher(this.delegate.consume(messageHandler));
	}

	@Override
	public Flowable<Void> consumeNothing() {
		return Flowable.fromPublisher(this.delegate.consumeNothing());
	}

}
