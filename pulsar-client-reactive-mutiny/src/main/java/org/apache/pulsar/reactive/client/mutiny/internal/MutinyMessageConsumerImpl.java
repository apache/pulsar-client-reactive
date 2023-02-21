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

package org.apache.pulsar.reactive.client.mutiny.internal;

import java.util.function.Function;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageConsumer;
import org.reactivestreams.Publisher;

/**
 * A Mutiny consumer implementation wrapping a {@link ReactiveMessageConsumer}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageConsumerImpl<T> implements MutinyMessageConsumer<T> {

	private final ReactiveMessageConsumer<T> consumer;

	/**
	 * Create a new {@link MutinyMessageConsumerImpl} wrapping the given
	 * {@link ReactiveMessageConsumer}.
	 * @param consumer the consumer to wrap
	 */
	MutinyMessageConsumerImpl(ReactiveMessageConsumer<T> consumer) {
		this.consumer = consumer;
	}

	@Override
	public <R> Uni<R> consumeOne(Function<Message<T>, Uni<MessageResult<R>>> messageHandler) {
		return this.consumer.consumeOne((m) -> messageHandler.apply(m).toMulti()).as(Uni.createFrom()::publisher);
	}

	@Override
	public <R> Multi<R> consumeMany(Function<Multi<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return this.consumer.consumeMany((m) -> messageHandler.apply(Multi.createFrom().publisher(m)))
				.as(Multi.createFrom()::publisher);
	}

}
