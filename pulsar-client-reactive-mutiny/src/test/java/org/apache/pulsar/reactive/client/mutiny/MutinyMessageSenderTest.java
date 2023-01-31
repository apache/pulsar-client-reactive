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

import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MessageSendResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageSender;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MutinyMessageSender}.
 */
class MutinyMessageSenderTest {

	@Test
	void sendOne() throws Exception {
		ReactiveMessageSender<String> sender = new ReactiveMessageSender<String>() {
			@Override
			public Mono<MessageId> sendOne(MessageSpec<String> messageSpec) {
				if (messageSpec.getCorrelationMetadata().equals("test")) {
					return Mono.just(MessageId.earliest);
				}
				return Mono.just(MessageId.latest);
			}

			@Override
			public Flux<MessageSendResult<String>> sendMany(Publisher<MessageSpec<String>> messageSpecs) {
				throw new UnsupportedOperationException("should not be called");
			}
		};
		MutinyMessageSender<String> mutinySender = createMutinyMessageSender(sender);
		MessageId messageId = mutinySender.sendOne(MessageSpec.builder("test").correlationMetadata("test").build())
				.subscribeAsCompletionStage().get(5, TimeUnit.SECONDS);
		assertThat(messageId).isEqualTo(MessageId.earliest);
	}

	@Test
	void sendMany() {
		ReactiveMessageSender<String> sender = new ReactiveMessageSender<String>() {
			@Override
			public Mono<MessageId> sendOne(MessageSpec<String> messageSpec) {
				throw new UnsupportedOperationException("should not be called");
			}

			@Override
			public Flux<MessageSendResult<String>> sendMany(Publisher<MessageSpec<String>> messageSpecs) {
				return Flux.from(messageSpecs)
						.map((messageSpec) -> new MessageSendResult<>(MessageId.earliest, messageSpec, null));
			}
		};
		MutinyMessageSender<String> mutinySender = createMutinyMessageSender(sender);

		MessageSpec<String> message1 = MessageSpec.of("test1");
		MessageSpec<String> message2 = MessageSpec.of("test2");
		Flux<MessageSpec<String>> messageSpecs = Flux.just(message1, message2);
		mutinySender.sendMany(messageSpecs).map(MessageSendResult::getMessageSpec).subscribe()
				.withSubscriber(new AssertSubscriber<>(2)).assertCompleted().assertItems(message1, message2);
	}

	private static MutinyMessageSender<String> createMutinyMessageSender(ReactiveMessageSender<String> sender) {
		ReactiveMessageSenderBuilder<String> senderBuilder = new ReactiveMessageSenderBuilder<String>() {

			@Override
			public MutableReactiveMessageSenderSpec getMutableSpec() {
				return null;
			}

			@Override
			public ReactiveMessageSenderBuilder<String> cache(ReactiveMessageSenderCache producerCache) {
				return null;
			}

			@Override
			public ReactiveMessageSenderBuilder<String> maxInflight(int maxInflight) {
				return null;
			}

			@Override
			public ReactiveMessageSenderBuilder<String> maxConcurrentSenderSubscriptions(
					int maxConcurrentSenderSubscriptions) {
				return null;
			}

			@Override
			public ReactiveMessageSenderBuilder<String> stopOnError(boolean stopOnError) {
				return null;
			}

			@Override
			public ReactiveMessageSenderBuilder<String> clone() {
				return null;
			}

			@Override
			public ReactiveMessageSender<String> build() {
				return sender;
			}
		};

		ReactivePulsarClient reactiveClient = new ReactivePulsarClient() {

			@Override
			public <T> ReactiveMessageSenderBuilder<T> messageSender(Schema<T> schema) {
				return (ReactiveMessageSenderBuilder<T>) senderBuilder;
			}

			@Override
			public <T> ReactiveMessageReaderBuilder<T> messageReader(Schema<T> schema) {
				return null;
			}

			@Override
			public <T> ReactiveMessageConsumerBuilder<T> messageConsumer(Schema<T> schema) {
				return null;
			}
		};

		return MutinyPulsarClientFactory.create(reactiveClient).messageSender(Schema.STRING).build();
	}

}
