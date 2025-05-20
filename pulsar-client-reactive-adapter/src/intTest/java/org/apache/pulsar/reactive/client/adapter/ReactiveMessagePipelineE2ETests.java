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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.policies.data.SubscriptionStats;
import org.apache.pulsar.common.policies.data.TopicStats;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.MessageSpecBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipeline;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipelineBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveMessagePipelineE2ETests {

	static final int KEYS_COUNT = 100;

	static final int ITEMS_PER_KEY_COUNT = 100;

	@Test
	void shouldConsumeMessages() throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient()) {
			String topicName = "test" + UUID.randomUUID();
			// create subscription to retain messages
			pulsarClient.newConsumer(Schema.STRING).topic(topicName).subscriptionName("sub").subscribe().close();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactiveMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
				.topic(topicName)
				.build();
			messageSender.sendMany(Flux.range(1, 100).map(Object::toString).map(MessageSpec::of)).blockLast();

			List<String> messages = Collections.synchronizedList(new ArrayList<>());
			CountDownLatch latch = new CountDownLatch(100);

			try (ReactiveMessagePipeline ignored = reactivePulsarClient.messageConsumer(Schema.STRING)
				.subscriptionName("sub")
				.topic(topicName)
				.build()
				.messagePipeline()
				.messageHandler((message) -> Mono.fromRunnable(() -> {
					messages.add(message.getValue());
					latch.countDown();
				}))
				.build()
				.start()) {
				assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
				assertThat(messages).isEqualTo(Flux.range(1, 100).map(Object::toString).collectList().block());
			}
		}
	}

	@Test
	void shouldSupportWaitingForConsumingToStartAndStop() throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient();
				PulsarAdmin pulsarAdmin = SingletonPulsarContainer.createPulsarAdmin()) {
			String topicName = "test" + UUID.randomUUID();
			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);
			ReactiveMessagePipeline pipeline = reactivePulsarClient.messageConsumer(Schema.STRING)
				.subscriptionName("sub")
				.topic(topicName)
				.build()
				.messagePipeline()
				.messageHandler((message) -> Mono.empty())
				.build()
				.start();

			// wait for consuming to start
			pipeline.untilStarted().block(Duration.ofSeconds(5));
			// there should be an existing subscription
			List<String> subscriptions = pulsarAdmin.topics().getSubscriptions(topicName);
			assertThat(subscriptions).as("subscription should be created").contains("sub");

			// stop the pipeline
			pipeline.stop();
			// and wait for it to stop
			pipeline.untilStopped().block(Duration.ofSeconds(5));
			// there should be no consumers
			TopicStats topicStats = pulsarAdmin.topics().getStats(topicName);
			SubscriptionStats subStats = topicStats.getSubscriptions().get("sub");
			assertThat(subStats.getConsumers()).isEmpty();
		}
	}

	@ParameterizedTest
	@EnumSource(MessageOrderScenario.class)
	void shouldRetainMessageOrder(MessageOrderScenario messageOrderScenario) throws Exception {
		try (PulsarClient pulsarClient = SingletonPulsarContainer.createPulsarClient()) {
			String topicName = "test" + UUID.randomUUID();
			// create subscription to retain messages
			pulsarClient.newConsumer(Schema.INT32).topic(topicName).subscriptionName("sub").subscribe().close();

			ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

			ReactiveMessageSender<Integer> messageSender = reactivePulsarClient.messageSender(Schema.INT32)
				.topic(topicName)
				.build();

			List<MessageSpec<Integer>> messageSpecs = generateRandomOrderedMessagesWhereSingleKeyIsOrdered(
					messageOrderScenario);

			messageSender.sendMany(Flux.fromIterable(messageSpecs)).blockLast();

			ConcurrentMap<Integer, List<Integer>> messages = new ConcurrentHashMap<>();
			CountDownLatch latch = new CountDownLatch(messageSpecs.size());

			List<Integer> orderedSequence = IntStream.rangeClosed(1, ITEMS_PER_KEY_COUNT)
				.boxed()
				.collect(Collectors.toList());

			ReactiveMessagePipelineBuilder.OneByOneMessagePipelineBuilder<Integer> reactiveMessageHandlerBuilder = reactivePulsarClient
				.messageConsumer(Schema.INT32)
				.subscriptionName("sub")
				.topic(topicName)
				.build()
				.messagePipeline()
				.messageHandler((message) -> {
					Mono<Void> messageHandler = Mono.fromRunnable(() -> {
						Integer keyId = Integer.parseInt(message.getProperty("keyId"));
						messages.compute(keyId, (k, list) -> {
							if (list == null) {
								list = new ArrayList<>();
							}
							list.add(message.getValue());
							return list;
						});
						latch.countDown();
					});
					if (messageOrderScenario != MessageOrderScenario.NO_PARALLEL) {
						// add delay which would lead to the execution timeout unless
						// messages are handled in parallel
						messageHandler = Mono.delay(Duration.ofMillis(5)).then(messageHandler);
					}
					return messageHandler;
				});
			if (messageOrderScenario != MessageOrderScenario.NO_PARALLEL) {
				reactiveMessageHandlerBuilder.concurrency(KEYS_COUNT).useKeyOrderedProcessing();
			}
			try (ReactiveMessagePipeline ignored = reactiveMessageHandlerBuilder.build().start()) {
				boolean latchCompleted = latch.await(5, TimeUnit.SECONDS);
				assertThat(latchCompleted).as("processing of all messages should have completed").isTrue();
				for (int i = 1; i <= KEYS_COUNT; i++) {
					assertThat(messages.get(i)).as("keyId %d", i).containsExactlyElementsOf(orderedSequence);
				}
			}
		}
	}

	private List<MessageSpec<Integer>> generateRandomOrderedMessagesWhereSingleKeyIsOrdered(
			final MessageOrderScenario messageOrderScenario) {
		List<Queue<MessageSpec<Integer>>> remainingMessages = Flux.range(1, KEYS_COUNT).concatMap((keyId) -> {
			String keyIdString = keyId.toString();
			byte[] keyBytes = ByteBuffer.allocate(4).putInt(keyId).array();
			return Flux.range(1, ITEMS_PER_KEY_COUNT).map((i) -> {
				MessageSpecBuilder<Integer> messageSpecBuilder = MessageSpec.builder(i).property("keyId", keyIdString);
				switch (messageOrderScenario) {
					case PARALLEL_PASS_KEY_IN_MESSAGEKEY:
						messageSpecBuilder.key(keyIdString);
						break;
					case PARALLEL_PASS_KEY_IN_ORDERINGKEY:
						messageSpecBuilder.orderingKey(keyBytes);
						break;
					case NO_PARALLEL:
						break;
				}
				return Tuples.of(keyId, messageSpecBuilder.build());
			});
		})
			.collectMultimap(Tuple2::getT1, Tuple2::getT2)
			.map(Map::values)
			.block()
			.stream()
			.map(LinkedBlockingQueue::new)
			.collect(Collectors.toList());

		List<MessageSpec<Integer>> messageSpecs = new ArrayList<>(KEYS_COUNT * ITEMS_PER_KEY_COUNT);
		while (messageSpecs.size() < KEYS_COUNT * ITEMS_PER_KEY_COUNT) {
			int randomIndex = ThreadLocalRandom.current().nextInt(remainingMessages.size());
			Queue<MessageSpec<Integer>> specsForKey = remainingMessages.get(randomIndex);
			MessageSpec<Integer> messageSpec = specsForKey.poll();
			messageSpecs.add(messageSpec);
			if (specsForKey.size() == 0) {
				remainingMessages.remove(randomIndex);
			}
		}
		return messageSpecs;
	}

	enum MessageOrderScenario {

		NO_PARALLEL, PARALLEL_PASS_KEY_IN_ORDERINGKEY, PARALLEL_PASS_KEY_IN_MESSAGEKEY

	}

}
