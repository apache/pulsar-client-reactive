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

public interface ReactiveMessageConsumer<T> {

	<R> Publisher<R> consume(Function<Flux<Message<T>>, Publisher<MessageResult<R>>> messageHandler);

	/**
	 * Creates the Pulsar Consumer and immediately closes it. This is useful for creating
	 * the Pulsar subscription that is related to the consumer. Nothing happens unless the
	 * returned Mono is subscribed.
	 * @return a Mono for consuming nothing
	 */
	Publisher<Void> consumeNothing();

	default <R extends ReactiveMessageConsumer> R adapt(Class<R> adaptedConsumerType) {
		if (adaptedConsumerType.equals(ReactorMessageConsumer.class)) {
			return (R) toReactor();
		}
		if (adaptedConsumerType.equals(RxJavaMessageConsumer.class)) {
			return (R) toRxJava();
		}
		throw new IllegalArgumentException("Can only be adapted to ReactorMessageConsumer or RxJavaMessageConsumer");
	}

	/**
	 * Convert to a reactor based message sender.
	 * @return the reactor based message sender instance
	 */
	default ReactorMessageConsumer<T> toReactor() {
		return new ReactorMessageConsumer<>(this);
	}

	/**
	 * Convert to a RxJava 3 based message sender. Use only if you have RxJava 3 on the
	 * class path (not pulled by pulsar-client-reactive-api).
	 * @return the RxJava 3 based message sender instance.
	 */
	default RxJavaMessageConsumer<T> toRxJava() {
		return new RxJavaMessageConsumer<>(this);
	}

}
