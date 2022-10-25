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

package org.apache.pulsar.reactive.client.adapter;

import java.util.List;
import java.util.UUID;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.api.ReactorMessageReader;
import org.apache.pulsar.reactive.client.api.ReactorMessageSender;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveMessageReaderE2ETest {

	@Test
	void shouldReadMessages() throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient();
				ReactiveMessageSenderCache producerCache = AdaptedReactivePulsarClientFactory.createCache()) {
			String topicName = "test" + UUID.randomUUID();
			// create subscription to retain messages
			pulsarClient.newConsumer(Schema.STRING).topic(topicName).subscriptionName("sub").subscribe().close();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactorMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
					.cache(producerCache).topic(topicName).buildReactor();
			messageSender.send(Flux.range(1, 100).map(Object::toString).map(MessageSpec::of)).blockLast();

			ReactorMessageReader<String> messageReader = reactivePulsarClient.messageReader(Schema.STRING)
					.topic(topicName).buildReactor();
			List<String> messages = messageReader.read().map(Message::getValue).collectList().block();

			assertThat(messages).isEqualTo(Flux.range(1, 100).map(Object::toString).collectList().block());
		}
	}

}
