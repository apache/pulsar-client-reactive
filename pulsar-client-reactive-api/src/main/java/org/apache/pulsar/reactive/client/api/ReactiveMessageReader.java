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

import org.apache.pulsar.client.api.Message;
import org.reactivestreams.Publisher;

public interface ReactiveMessageReader<T> {

	Publisher<Message<T>> read();

	default <R extends ReactiveMessageReader> R adapt(Class<R> adaptedReaderType) {
		if (adaptedReaderType.equals(ReactorMessageReader.class)) {
			return (R) toReactor();
		}
		if (adaptedReaderType.equals(RxJavaMessageReader.class)) {
			return (R) toRxJava();
		}
		throw new IllegalArgumentException("Can only be adapted to ReactorMessageReader or RxJavaMessageReader");
	}

	/**
	 * Convert to a reactor based message sender.
	 * @return the reactor based message sender instance
	 */
	default ReactorMessageReader<T> toReactor() {
		return new ReactorMessageReader<>(this);
	}

	/**
	 * Convert to a RxJava 3 based message sender. Use only if you have RxJava 3 on the
	 * class path (not pulled by pulsar-client-reactive-api).
	 * @return the RxJava 3 based message sender instance.
	 */
	default RxJavaMessageReader<T> toRxJava() {
		return new RxJavaMessageReader<>(this);
	}

}
