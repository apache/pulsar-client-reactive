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

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.internal.adapter.ConcurrentHashMapProducerCacheProvider;
import org.apache.pulsar.reactive.client.producercache.CaffeineProducerCacheProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveMessageSenderE2ETest {

	private static Stream<Arguments> shouldSendMessageToTopicWithCachedProducer() {
		return Arrays
				.asList(Arguments.of("ConcurrentHashMapProducerCacheProvider",
						AdaptedReactivePulsarClientFactory.createCache(new ConcurrentHashMapProducerCacheProvider())),
						Arguments.of("Default", AdaptedReactivePulsarClientFactory.createCache()),
						Arguments.of("CaffeineProducerCacheProvider",
								AdaptedReactivePulsarClientFactory.createCache(new CaffeineProducerCacheProvider())))
				.stream();
	}

	@Test
	void shouldSendMessageToTopic() throws PulsarClientException {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient()) {
			String topicName = "test" + UUID.randomUUID();
			Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING).topic(topicName).subscriptionName("sub")
					.subscribe();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactiveMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
					.topic(topicName).maxInflight(1).build();
			MessageId messageId = messageSender.sendOne(MessageSpec.of("Hello world!")).block();
			assertThat(messageId).isNotNull();

			Message<String> message = consumer.receive(1, TimeUnit.SECONDS);
			assertThat(message).isNotNull();
			assertThat(message.getValue()).isEqualTo("Hello world!");
		}
	}

	@ParameterizedTest
	@MethodSource
	void shouldSendMessageToTopicWithCachedProducer(String name, ReactiveMessageSenderCache cacheInstance)
			throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient();
				ReactiveMessageSenderCache producerCache = cacheInstance) {
			String topicName = "test" + UUID.randomUUID();
			Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING).topic(topicName).subscriptionName("sub")
					.subscribe();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactiveMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
					.cache(producerCache).maxInflight(1).topic(topicName).build();
			MessageId messageId = messageSender.sendOne(MessageSpec.of("Hello world!")).block();
			assertThat(messageId).isNotNull();

			Message<String> message = consumer.receive(1, TimeUnit.SECONDS);
			assertThat(message).isNotNull();
			assertThat(message.getValue()).isEqualTo("Hello world!");
		}
	}

}
