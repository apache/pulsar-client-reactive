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

package org.apache.pulsar.reactive.client.internal.adapter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.PulsarClientException.AlreadyClosedException;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.PulsarClientImpl;
import org.apache.pulsar.client.impl.conf.ConsumerConfigurationData;
import org.apache.pulsar.common.partition.PartitionedTopicMetadata;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AdaptedReactiveMessageConsumer}.
 */
class AdaptedReactiveMessageConsumerTests {

	@Test
	void consumerProperties() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());
		doReturn(CompletableFuture.completedFuture(new PartitionedTopicMetadata())).when(pulsarClient)
			.getPartitionedTopicMetadata(anyString(), anyBoolean(), anyBoolean());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();

		CryptoKeyReader cryptoKeyReader = mock(CryptoKeyReader.class);

		DeadLetterPolicy deadLetterPolicy = DeadLetterPolicy.builder()
			.retryLetterTopic("my-rlt")
			.maxRedeliverCount(1)
			.build();

		ConsumerConfigurationData<String> expectedConsumerConf = new ConsumerConfigurationData<>();
		expectedConsumerConf.setTopicNames(new HashSet<>(Arrays.asList("my-topic", "my-rlt")));
		expectedConsumerConf.setSubscriptionName("my-sub");
		expectedConsumerConf.setSubscriptionMode(SubscriptionMode.NonDurable);
		expectedConsumerConf.setSubscriptionType(SubscriptionType.Failover);
		expectedConsumerConf.setSubscriptionInitialPosition(SubscriptionInitialPosition.Earliest);
		expectedConsumerConf.setReplicateSubscriptionState(true);

		Map<String, String> subscriptionProperties = new HashMap<>();
		subscriptionProperties.put("my-sub-key", "my-sub-value");
		expectedConsumerConf.setSubscriptionProperties(subscriptionProperties);

		expectedConsumerConf.setConsumerName("my-consumer");

		SortedMap<String, String> properties = new TreeMap<>();
		properties.put("my-key", "my-value");
		expectedConsumerConf.setProperties(properties);

		expectedConsumerConf.setPriorityLevel(2);
		expectedConsumerConf.setReadCompacted(true);
		expectedConsumerConf.setBatchIndexAckEnabled(true);
		expectedConsumerConf.setAckTimeoutMillis(TimeUnit.SECONDS.toMillis(3));
		expectedConsumerConf.setTickDurationMillis(TimeUnit.SECONDS.toMillis(4));
		expectedConsumerConf.setAcknowledgementsGroupTimeMicros(TimeUnit.SECONDS.toMicros(5));
		expectedConsumerConf.setNegativeAckRedeliveryDelayMicros(TimeUnit.SECONDS.toMicros(6));
		expectedConsumerConf.setDeadLetterPolicy(deadLetterPolicy);
		expectedConsumerConf.setRetryEnable(true);
		expectedConsumerConf.setReceiverQueueSize(7);
		expectedConsumerConf.setMaxTotalReceiverQueueSizeAcrossPartitions(8);
		expectedConsumerConf.setAutoUpdatePartitions(false);
		expectedConsumerConf.setAutoUpdatePartitionsIntervalSeconds(9);
		expectedConsumerConf.setCryptoKeyReader(cryptoKeyReader);
		expectedConsumerConf.setCryptoFailureAction(ConsumerCryptoFailureAction.DISCARD);
		expectedConsumerConf.setMaxPendingChunkedMessage(10);
		expectedConsumerConf.setAutoAckOldestChunkedMessageOnQueueFull(true);
		expectedConsumerConf.setExpireTimeOfIncompleteChunkedMessageMillis(TimeUnit.SECONDS.toMillis(11));

		CompletableFuture<String> failedConsumer = new CompletableFuture<>();
		failedConsumer.completeExceptionally(new RuntimeException("didn't match expected consumer conf"));
		doReturn(failedConsumer).when(pulsarClient).subscribeAsync(any(), eq(Schema.STRING), isNull());
		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(eq(expectedConsumerConf), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.subscriptionMode(SubscriptionMode.NonDurable)
			.subscriptionType(SubscriptionType.Failover)
			.subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
			.replicateSubscriptionState(true)
			.subscriptionProperty("my-sub-key", "my-sub-value")
			.consumerName("my-consumer")
			.priorityLevel(2)
			.readCompacted(true)
			.property("my-key", "my-value")
			.batchIndexAckEnabled(true)
			.ackTimeout(Duration.ofSeconds(3))
			.ackTimeoutTickTime(Duration.ofSeconds(4))
			.acknowledgementsGroupTime(Duration.ofSeconds(5))
			.negativeAckRedeliveryDelay(Duration.ofSeconds(6))
			.deadLetterPolicy(deadLetterPolicy)
			.retryLetterTopicEnable(true)
			.receiverQueueSize(7)
			.maxTotalReceiverQueueSizeAcrossPartitions(8)
			.autoUpdatePartitions(false)
			.autoUpdatePartitionsInterval(Duration.ofSeconds(9))
			.cryptoKeyReader(cryptoKeyReader)
			.cryptoFailureAction(ConsumerCryptoFailureAction.DISCARD)
			.maxPendingChunkedMessage(10)
			.autoAckOldestChunkedMessageOnQueueFull(true)
			.expireTimeOfIncompleteChunkedMessage(Duration.ofSeconds(11))
			.clone()
			.build();

		reactiveConsumer.consumeNothing().block(Duration.ofSeconds(5));

		verify(pulsarClient).subscribeAsync(any(), any(), isNull());
	}

	@Test
	void keySharedPolicy() throws Exception {
		KeySharedPolicy keySharedPolicy = KeySharedPolicy.autoSplitHashRange();

		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();

		ConsumerConfigurationData<String> expectedConsumerConf = new ConsumerConfigurationData<>();
		expectedConsumerConf.setTopicNames(Collections.singleton("my-topic"));
		expectedConsumerConf.setSubscriptionName("my-sub");
		expectedConsumerConf.setSubscriptionType(SubscriptionType.Key_Shared);
		expectedConsumerConf.setKeySharedPolicy(keySharedPolicy);

		CompletableFuture<String> failedConsumer = new CompletableFuture<>();
		failedConsumer.completeExceptionally(new RuntimeException("didn't match expected consumer conf"));
		doReturn(failedConsumer).when(pulsarClient).subscribeAsync(any(), eq(Schema.STRING), isNull());
		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(eq(expectedConsumerConf), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.subscriptionType(SubscriptionType.Key_Shared)
			.keySharedPolicy(keySharedPolicy)
			.clone()
			.build();

		reactiveConsumer.consumeNothing().block(Duration.ofSeconds(5));

		verify(pulsarClient).subscribeAsync(any(), any(), isNull());
	}

	@Test
	void topicsPattern() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();

		Pattern topicsPattern = Pattern.compile("my-topic-.*");

		ConsumerConfigurationData<String> expectedConsumerConf = new ConsumerConfigurationData<>();
		expectedConsumerConf.setSubscriptionName("my-sub");
		expectedConsumerConf.setTopicsPattern(topicsPattern);
		expectedConsumerConf.setRegexSubscriptionMode(RegexSubscriptionMode.AllTopics);
		expectedConsumerConf.setPatternAutoDiscoveryPeriod(1);

		CompletableFuture<String> failedConsumer = new CompletableFuture<>();
		failedConsumer.completeExceptionally(new RuntimeException("didn't match expected consumer conf"));
		doReturn(failedConsumer).when(pulsarClient).subscribeAsync(any(), eq(Schema.STRING), isNull());
		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(eq(expectedConsumerConf), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.subscriptionName("my-sub")
			.topicsPattern(topicsPattern)
			.topicsPatternSubscriptionMode(RegexSubscriptionMode.AllTopics)
			.topicsPatternAutoDiscoveryPeriod(Duration.ofSeconds(1))
			.clone()
			.build();

		reactiveConsumer.consumeNothing().block(Duration.ofSeconds(5));

		verify(pulsarClient).subscribeAsync(any(), any(), isNull());
	}

	@Test
	void consumeOneAndAcknowledge() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).acknowledgeAsync(any(MessageId.class));

		Message<String> message = mock(Message.class);
		doReturn(MessageId.earliest).when(message).getMessageId();
		doReturn(CompletableFuture.completedFuture(message)).when(consumer).receiveAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.acknowledgeAsynchronously(false)
			.clone()
			.build();

		Message<String> received = reactiveConsumer
			.consumeOne((m) -> Mono.just(MessageResult.acknowledge(m.getMessageId(), m)))
			.block(Duration.ofSeconds(5));

		verify(consumer).receiveAsync();
		verify(consumer).acknowledgeAsync(MessageId.earliest);
		assertThat(message).isSameAs(received);
	}

	@Test
	void consumeOneAndNegativeAcknowledge() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();
		Message<String> message = mock(Message.class);
		doReturn(MessageId.earliest).when(message).getMessageId();
		doReturn(CompletableFuture.completedFuture(message)).when(consumer).receiveAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.acknowledgeAsynchronously(false)
			.clone()
			.build();

		Message<String> received = reactiveConsumer
			.consumeOne((m) -> Mono.just(MessageResult.negativeAcknowledge(m.getMessageId(), m)))
			.block(Duration.ofSeconds(5));

		verify(consumer).receiveAsync();
		verify(consumer).negativeAcknowledge(MessageId.earliest);
		assertThat(message).isSameAs(received);
	}

	@Test
	void consumeOneAndDontAcknowledge() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();
		Message<String> message = mock(Message.class);
		doReturn(MessageId.earliest).when(message).getMessageId();
		doReturn(CompletableFuture.completedFuture(message)).when(consumer).receiveAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.clone()
			.build();

		Message<String> received = reactiveConsumer.consumeOne((m) -> Mono.just(MessageResult.acknowledge(null, m)))
			.block(Duration.ofSeconds(5));

		verify(consumer).receiveAsync();
		verify(consumer, never()).acknowledge(any(MessageId.class));
		assertThat(message).isSameAs(received);
	}

	@Test
	void consumeManyAndAcknowledge() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).acknowledgeAsync(any(MessageId.class));

		Message<String> message1 = mock(Message.class);
		doReturn(MessageId.latest).when(message1).getMessageId();

		Message<String> message2 = mock(Message.class);
		doReturn(MessageId.earliest).when(message2).getMessageId();

		doReturn(CompletableFuture.completedFuture(message1), CompletableFuture.completedFuture(message2),
				new CompletableFuture<String>())
			.when(consumer)
			.receiveAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.clone()
			.build();

		StepVerifier
			.create(reactiveConsumer.consumeMany((messages) -> messages.map(MessageResult::acknowledgeAndReturn))
				.take(Duration.ofMillis(200)))
			.expectNext(message1)
			.expectNext(message2)
			.verifyComplete();

		verify(consumer, times(3)).receiveAsync();
		verify(consumer).acknowledgeAsync(eq(MessageId.earliest));
		verify(consumer).acknowledgeAsync(eq(MessageId.latest));
	}

	@Test
	void consumePulsarException() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.completedFuture(null)).when(consumer).closeAsync();

		CompletableFuture<String> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(new PulsarClientException.InvalidMessageException("test"));
		doReturn(failedFuture).when(consumer).receiveAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.build();

		StepVerifier.create(reactiveConsumer.consumeOne((message) -> Mono.just(MessageResult.acknowledge(message))))
			.verifyError(PulsarClientException.InvalidMessageException.class);

		StepVerifier
			.create(reactiveConsumer.consumeMany((messages) -> messages.map(MessageResult::acknowledgeAndReturn)))
			.verifyError(PulsarClientException.InvalidMessageException.class);
	}

	@Test
	void closeConsumerExceptionIsIgnored() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Consumer<String> consumer = mock(Consumer.class);
		doReturn(CompletableFuture.failedFuture(new AlreadyClosedException("Already closed"))).when(consumer)
			.closeAsync();

		doReturn(CompletableFuture.completedFuture(consumer)).when(pulsarClient)
			.subscribeAsync(any(ConsumerConfigurationData.class), eq(Schema.STRING), isNull());

		ReactiveMessageConsumer<String> reactiveConsumer = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageConsumer(Schema.STRING)
			.topic("my-topic")
			.subscriptionName("my-sub")
			.build();

		StepVerifier.create(reactiveConsumer.consumeNothing()).expectComplete().verify();
		verify(consumer).closeAsync();
	}

}
