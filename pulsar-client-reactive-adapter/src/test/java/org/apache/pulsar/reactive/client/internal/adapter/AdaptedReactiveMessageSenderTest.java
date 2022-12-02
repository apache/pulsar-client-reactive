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

package org.apache.pulsar.reactive.client.internal.adapter;

import java.time.Duration;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.ProducerBase;
import org.apache.pulsar.client.impl.PulsarClientImpl;
import org.apache.pulsar.client.impl.TypedMessageBuilderImpl;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.internal.api.InternalMessageSpec;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AdaptedReactiveMessageSender}.
 */
class AdaptedReactiveMessageSenderTest {

	@Test
	void sendOne() throws Exception {
		MessageRouter messageRouter = new MessageRouter() {
		};
		BatcherBuilder batcherBuilder = () -> null;

		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		ProducerBase<String> producer = mock(ProducerBase.class);
		doReturn(CompletableFuture.completedFuture(null)).when(producer).closeAsync();
		TypedMessageBuilderImpl<String> typedMessageBuilder = spy(
				new TypedMessageBuilderImpl<>(producer, Schema.STRING));
		doReturn(CompletableFuture.completedFuture(MessageId.earliest)).when(typedMessageBuilder).sendAsync();

		doReturn(typedMessageBuilder).when(producer).newMessage();

		CryptoKeyReader cryptoKeyReader = mock(CryptoKeyReader.class);

		ProducerConfigurationData expectedProducerConf = new ProducerConfigurationData();
		expectedProducerConf.setTopicName("my-topic");
		expectedProducerConf.setProducerName("my-producer");
		expectedProducerConf.setSendTimeoutMs(TimeUnit.SECONDS.toMillis(1));
		expectedProducerConf.setMaxPendingMessages(2);
		expectedProducerConf.setMaxPendingMessagesAcrossPartitions(3);
		expectedProducerConf.setMessageRoutingMode(MessageRoutingMode.CustomPartition);
		expectedProducerConf.setHashingScheme(HashingScheme.Murmur3_32Hash);
		expectedProducerConf.setCryptoFailureAction(ProducerCryptoFailureAction.SEND);
		expectedProducerConf.setCustomMessageRouter(messageRouter);
		expectedProducerConf.setBatchingMaxPublishDelayMicros(TimeUnit.SECONDS.toMicros(4));
		expectedProducerConf.setBatchingPartitionSwitchFrequencyByPublishDelay(5);
		expectedProducerConf.setBatchingMaxMessages(6);
		expectedProducerConf.setBatchingMaxBytes(7);
		expectedProducerConf.setBatchingEnabled(false);
		expectedProducerConf.setBatcherBuilder(batcherBuilder);
		expectedProducerConf.setChunkingEnabled(true);
		expectedProducerConf.setCryptoKeyReader(cryptoKeyReader);
		expectedProducerConf.setEncryptionKeys(Collections.singleton("my-key"));
		expectedProducerConf.setCompressionType(CompressionType.LZ4);
		expectedProducerConf.setInitialSequenceId(8L);
		expectedProducerConf.setAutoUpdatePartitions(true);
		expectedProducerConf.setAutoUpdatePartitionsIntervalSeconds(9);
		expectedProducerConf.setMultiSchema(true);
		expectedProducerConf.setAccessMode(ProducerAccessMode.Exclusive);
		expectedProducerConf.setLazyStartPartitionedProducers(true);

		SortedMap<String, String> properties = new TreeMap<>();
		properties.put("my-key", "my-value");

		expectedProducerConf.setProperties(properties);

		CompletableFuture<String> failedProducer = new CompletableFuture<>();
		failedProducer.completeExceptionally(new RuntimeException("didn't match expected producer conf"));
		doReturn(failedProducer).when(pulsarClient).createProducerAsync(any(), eq(Schema.STRING), isNull());
		doReturn(CompletableFuture.completedFuture(producer)).when(pulsarClient)
				.createProducerAsync(eq(expectedProducerConf), eq(Schema.STRING), isNull());

		ReactiveMessageSender<String> reactiveSender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
				.messageSender(Schema.STRING).topic("my-topic").producerName("my-producer")
				.sendTimeout(Duration.ofSeconds(1)).maxPendingMessages(2).maxPendingMessagesAcrossPartitions(3)
				.messageRoutingMode(MessageRoutingMode.CustomPartition).hashingScheme(HashingScheme.Murmur3_32Hash)
				.cryptoFailureAction(ProducerCryptoFailureAction.SEND).messageRouter(messageRouter)
				.batchingMaxPublishDelay(Duration.ofSeconds(4)).roundRobinRouterBatchingPartitionSwitchFrequency(5)
				.batchingMaxMessages(6).batchingMaxBytes(7).batchingEnabled(false).batcherBuilder(batcherBuilder)
				.chunkingEnabled(true).cryptoKeyReader(cryptoKeyReader).encryptionKeys(Collections.singleton("my-key"))
				.compressionType(CompressionType.LZ4).initialSequenceId(8).autoUpdatePartitions(true)
				.autoUpdatePartitionsInterval(Duration.ofSeconds(9)).multiSchema(true)
				.accessMode(ProducerAccessMode.Exclusive).lazyStartPartitionedProducers(true)
				.property("my-key", "my-value").clone().build();

		MessageSpec<String> messageSpec = spy(MessageSpec.of("test"));
		MessageId messageId1 = reactiveSender.sendOne(messageSpec).block(Duration.ofSeconds(5));

		verify(pulsarClient).createProducerAsync(any(), any(), isNull());
		verify((InternalMessageSpec<String>) messageSpec).configure(typedMessageBuilder);
		assertThat(messageId1).isEqualTo(MessageId.earliest);
	}

	@Test
	void sendMany() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		ProducerBase<String> producer = mock(ProducerBase.class);
		doReturn(CompletableFuture.completedFuture(null)).when(producer).closeAsync();
		TypedMessageBuilderImpl<String> typedMessageBuilder1 = spy(
				new TypedMessageBuilderImpl<>(producer, Schema.STRING));
		doReturn(CompletableFuture.completedFuture(MessageId.earliest)).when(typedMessageBuilder1).sendAsync();
		TypedMessageBuilderImpl<String> typedMessageBuilder2 = spy(
				new TypedMessageBuilderImpl<>(producer, Schema.STRING));
		doReturn(CompletableFuture.completedFuture(MessageId.latest)).when(typedMessageBuilder2).sendAsync();

		doReturn(typedMessageBuilder1, typedMessageBuilder2).when(producer).newMessage();
		doReturn(CompletableFuture.completedFuture(producer)).when(pulsarClient).createProducerAsync(any(),
				eq(Schema.STRING), isNull());

		ReactiveMessageSender<String> reactiveSender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
				.messageSender(Schema.STRING).topic("my-topic").build();

		Flux<MessageSpec<String>> messageSpecs = Flux.just(MessageSpec.of("test1"), MessageSpec.of("test2"));
		StepVerifier.create(reactiveSender.sendMany(messageSpecs)).expectNext(MessageId.earliest)
				.expectNext(MessageId.latest).verifyComplete();

		verify(pulsarClient).createProducerAsync(any(), any(), isNull());
		InOrder inOrder = Mockito.inOrder(typedMessageBuilder1, typedMessageBuilder2);
		inOrder.verify(typedMessageBuilder1).value("test1");
		inOrder.verify(typedMessageBuilder1).sendAsync();
		inOrder.verify(typedMessageBuilder2).value("test2");
		inOrder.verify(typedMessageBuilder2).sendAsync();
	}

}
