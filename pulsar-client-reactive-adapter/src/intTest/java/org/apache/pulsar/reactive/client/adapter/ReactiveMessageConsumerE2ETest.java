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

package org.apache.pulsar.reactive.client.adapter;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveMessageConsumerE2ETest {

	@Test
	void shouldConsumeMessages() throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient();
				ReactiveMessageSenderCache producerCache = AdaptedReactivePulsarClientFactory.createCache()) {
			String topicName = "test" + UUID.randomUUID();
			// create subscription to retain messages
			pulsarClient.newConsumer(Schema.STRING).topic(topicName).subscriptionName("sub").subscribe().close();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactiveMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
					.cache(producerCache).topic(topicName).build();
			messageSender.sendMany(Flux.range(1, 100).map(Object::toString).map(MessageSpec::of)).blockLast();

			ReactiveMessageConsumer<String> messageConsumer = reactivePulsarClient.messageConsumer(Schema.STRING)
					.topic(topicName).subscriptionName("sub").build();
			List<String> messages = messageConsumer
					.consumeMany((messageFlux) -> messageFlux
							.map((message) -> MessageResult.acknowledge(message.getMessageId(), message.getValue())))
					.take(Duration.ofSeconds(2)).collectList().block();

			assertThat(messages).isEqualTo(Flux.range(1, 100).map(Object::toString).collectList().block());

			// should have acknowledged all messages
			List<Message<String>> remainingMessages = messageConsumer
					.consumeMany((messageFlux) -> messageFlux.map(MessageResult::acknowledgeAndReturn))
					.take(Duration.ofSeconds(2)).collectList().block();
			assertThat(remainingMessages).isEmpty();
		}
	}

}
