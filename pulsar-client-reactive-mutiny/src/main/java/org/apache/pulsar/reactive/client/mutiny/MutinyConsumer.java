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

package org.apache.pulsar.reactive.client.mutiny;

import java.util.function.Function;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.reactivestreams.Publisher;

/**
 * A Mutiny wrapper for a {@link ReactiveMessageConsumer}.
 */
public class MutinyConsumer<T> {

	private final ReactiveMessageConsumer<T> consumer;

	/**
	 * Create a new {@link MutinyConsumer} wrapping the given
	 * {@link ReactiveMessageConsumer}.
	 * @param consumer the consumer to wrap
	 */
	public MutinyConsumer(ReactiveMessageConsumer<T> consumer) {
		this.consumer = consumer;
	}

	/**
	 * Consumes one message.
	 * @param messageHandler a {@link Function} to apply to the consumed message that
	 * returns a {@link MessageResult} which contains the acknowledgement or negative
	 * acknowledgement referencing the message id of the input message together with an
	 * optional return value object
	 * @param <R> the type of MessageResult returned by the message handler
	 * @return the value contained by the {@link MessageResult} returned by the message
	 * handler
	 */
	public <R> Uni<R> consumeOne(Function<Message<T>, Uni<MessageResult<R>>> messageHandler) {
		return Uni.createFrom().publisher(this.consumer.consumeOne((m) -> messageHandler.apply(m).toMulti()));
	}

	/**
	 * Consumes messages continuously.
	 * @param messageHandler a {@link Function} to apply to the consumed messages that
	 * returns {@link MessageResult}s, each containing the acknowledgement or negative
	 * acknowledgement referencing the message id of the corresponding input messages
	 * together with an optional return value object
	 * @param <R> the type of MessageResult returned by the message handler
	 * @return the values contained by the {@link MessageResult}s returned by the message
	 * handler
	 */
	public <R> Multi<R> consumeMany(Function<Multi<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return Multi.createFrom()
				.publisher(this.consumer.consumeMany((m) -> messageHandler.apply(Multi.createFrom().publisher(m))));
	}

}
