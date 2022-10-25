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

public interface ReactiveMessageSender<T> {

	/**
	 * Send messages and get the associated message ids in the same order as the sent
	 * messages.
	 * @param messageSpecs the specs of the messages to send
	 * @return a publisher that will emit a message id per message successfully sent in
	 * the order that they have been sent
	 */
	Publisher<MessageId> send(Publisher<MessageSpec<T>> messageSpecs);

	default <R extends ReactiveMessageSender> R adapt(Class<R> adaptedSenderType) {
		if (adaptedSenderType.equals(ReactorMessageSender.class)) {
			return (R) toReactor();
		}
		if (adaptedSenderType.equals(RxJavaMessageSender.class)) {
			return (R) toRxJava();
		}
		throw new IllegalArgumentException("Can only be adapted to ReactorMessageSender or RxJavaMessageSender");
	}

	/**
	 * Convert to a reactor based message sender.
	 * @return the reactor based message sender instance
	 */
	default ReactorMessageSender<T> toReactor() {
		return new ReactorMessageSender<>(this);
	}

	/**
	 * Convert to a RxJava 3 based message sender. Use only if you have RxJava 3 on the
	 * class path (not pulled by pulsar-client-reactive-api).
	 * @return the RxJava 3 based message sender instance.
	 */
	default RxJavaMessageSender<T> toRxJava() {
		return new RxJavaMessageSender<>(this);
	}

}
