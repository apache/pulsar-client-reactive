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

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MutinyConsumer}.
 */
class MutinyConsumerTest {

	@Test
	void consumeOne() throws Exception {
		ReactiveMessageConsumer<String> consumer = new ReactiveMessageConsumer<String>() {
			@Override
			public <R> Mono<R> consumeOne(Function<Message<String>, Publisher<MessageResult<R>>> messageHandler) {
				return Mono.from(messageHandler.apply(new TestMessage("test-message"))).map(MessageResult::getValue);
			}

			@Override
			public <R> Flux<R> consumeMany(
					Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {
				throw new UnsupportedOperationException("should not be called");
			}

			@Override
			public Mono<Void> consumeNothing() {
				throw new UnsupportedOperationException("should not be called");
			}
		};

		MutinyConsumer<String> mutinyConsumer = new MutinyConsumer<>(consumer);
		Function<Message<String>, Uni<MessageResult<Integer>>> handler = (message) -> Uni.createFrom()
				.item(MessageResult.acknowledge(message.getMessageId(), message.getValue().length()));
		Uni<Integer> message = mutinyConsumer.consumeOne(handler);
		assertThat(message.subscribeAsCompletionStage().get(5, TimeUnit.SECONDS)).isEqualTo(12);
	}

	@Test
	void consumeMany() {
		ReactiveMessageConsumer<String> consumer = new ReactiveMessageConsumer<String>() {
			@Override
			public <R> Mono<R> consumeOne(Function<Message<String>, Publisher<MessageResult<R>>> messageHandler) {
				throw new UnsupportedOperationException("should not be called");
			}

			@Override
			public <R> Flux<R> consumeMany(
					Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {
				Flux<Message<String>> messages = Flux.range(0, 10).map((i) -> new TestMessage("test-message-" + i));
				return Flux.from(messageHandler.apply(messages)).map(MessageResult::getValue);
			}

			@Override
			public Mono<Void> consumeNothing() {
				throw new UnsupportedOperationException("should not be called");
			}
		};

		MutinyConsumer<String> mutinyConsumer = new MutinyConsumer<>(consumer);
		Function<Multi<Message<String>>, Publisher<MessageResult<Integer>>> handler = (messages) -> messages
				.map((message) -> MessageResult.acknowledge(message.getMessageId(),
						Integer.valueOf(message.getValue().split("-")[2])));
		Multi<Integer> messages = mutinyConsumer.consumeMany(handler);
		messages.subscribe().withSubscriber(new AssertSubscriber<>(10)).assertItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
				.assertCompleted();

	}

}
