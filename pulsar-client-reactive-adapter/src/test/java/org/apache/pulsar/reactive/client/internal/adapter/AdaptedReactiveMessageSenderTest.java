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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.apache.pulsar.client.impl.MessageIdImpl;
import org.apache.pulsar.client.impl.ProducerBase;
import org.apache.pulsar.client.impl.PulsarClientImpl;
import org.apache.pulsar.client.impl.TypedMessageBuilderImpl;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.internal.api.InternalMessageSpec;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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

	@ParameterizedTest
	@MethodSource
	void senderCache(String name, ReactiveMessageSenderCache cache) throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		ProducerBase<String> producer = mock(ProducerBase.class);
		doReturn(CompletableFuture.completedFuture(null)).when(producer).closeAsync();
		doReturn(CompletableFuture.completedFuture(null)).when(producer).flushAsync();
		doReturn(true).when(producer).isConnected();
		TypedMessageBuilderImpl<String> typedMessageBuilder = spy(
				new TypedMessageBuilderImpl<>(producer, Schema.STRING));
		doReturn(CompletableFuture.completedFuture(MessageId.earliest)).when(typedMessageBuilder).sendAsync();

		doReturn(typedMessageBuilder).when(producer).newMessage();

		doReturn(CompletableFuture.completedFuture(producer)).when(pulsarClient).createProducerAsync(any(),
				eq(Schema.STRING), isNull());

		ProducerBase<Integer> producer2 = mock(ProducerBase.class);
		doReturn(CompletableFuture.completedFuture(null)).when(producer2).closeAsync();
		doReturn(CompletableFuture.completedFuture(null)).when(producer2).flushAsync();
		doReturn(true).when(producer2).isConnected();
		TypedMessageBuilderImpl<Integer> typedMessageBuilder2 = spy(
				new TypedMessageBuilderImpl<>(producer2, Schema.INT32));
		doReturn(CompletableFuture.completedFuture(MessageId.earliest)).when(typedMessageBuilder2).sendAsync();

		doReturn(typedMessageBuilder2).when(producer2).newMessage();

		doReturn(CompletableFuture.completedFuture(producer2)).when(pulsarClient).createProducerAsync(any(),
				eq(Schema.INT32), isNull());

		// Sender without cache
		createSenderAndSendMessages(pulsarClient, Schema.STRING, "my-topic", null, new String[] { "a", "b", "c" });
		verify(pulsarClient, times(3)).createProducerAsync(any(), any(), isNull());

		// Sender with cache
		createSenderAndSendMessages(pulsarClient, Schema.STRING, "my-topic", cache, new String[] { "a", "b", "c" });
		verify(pulsarClient, times(4)).createProducerAsync(any(), any(), isNull());

		// Other sender wih same cache, same Schema, same Producer config
		createSenderAndSendMessages(pulsarClient, Schema.STRING, "my-topic", cache, new String[] { "d", "e", "f" });
		verify(pulsarClient, times(4)).createProducerAsync(any(), any(), isNull());

		// Other sender wih same cache, same Schema, different Producer config
		createSenderAndSendMessages(pulsarClient, Schema.STRING, "my-other-topic", cache,
				new String[] { "a", "b", "c" });
		verify(pulsarClient, times(5)).createProducerAsync(any(), any(), isNull());

		// Other sender wih same cache, different Schema, same Producer config
		createSenderAndSendMessages(pulsarClient, Schema.INT32, "my-topic", cache, new Integer[] { 42, 43, 44 });
		verify(pulsarClient, times(6)).createProducerAsync(any(), any(), isNull());

	}

	private static Stream<Arguments> senderCache() {
		return Arrays.asList(
				Arguments.of("ConcurrentHashMapProducerCacheProvider",
						AdaptedReactivePulsarClientFactory.createCache(new ConcurrentHashMapProducerCacheProvider())),
				Arguments.of("Default", AdaptedReactivePulsarClientFactory.createCache())).stream();
	}

	private static <T> void createSenderAndSendMessages(PulsarClient client, Schema<T> schema, String topic,
			ReactiveMessageSenderCache cache, T[] values) {
		assertThat(values).hasSize(3);
		ReactiveMessageSenderBuilder<T> builder = AdaptedReactivePulsarClientFactory.create(client)
				.messageSender(schema).topic(topic);
		if (cache != null) {
			builder.cache(cache);
		}
		ReactiveMessageSender<T> sender = builder.build();

		sender.sendOne(MessageSpec.of(values[0])).then(sender.sendOne(MessageSpec.of(values[1])))
				.thenMany(Flux.just(MessageSpec.of(values[2])).as(sender::sendMany)).blockLast(Duration.ofSeconds(5));
	}

	@Test
	void senderCacheEntryRecreatedIfProducerClosed() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		ProducerBase<String> producer = mock(ProducerBase.class);
		doReturn(CompletableFuture.completedFuture(null)).when(producer).closeAsync();
		doReturn(CompletableFuture.completedFuture(null)).when(producer).flushAsync();
		doReturn(true).when(producer).isConnected();
		TypedMessageBuilderImpl<String> typedMessageBuilder = spy(
				new TypedMessageBuilderImpl<>(producer, Schema.STRING));
		doReturn(CompletableFuture.completedFuture(MessageId.earliest)).when(typedMessageBuilder).sendAsync();
		doReturn(typedMessageBuilder).when(producer).newMessage();

		doReturn(CompletableFuture.completedFuture(producer)).when(pulsarClient).createProducerAsync(any(),
				eq(Schema.STRING), isNull());

		ReactiveMessageSenderCache cache = AdaptedReactivePulsarClientFactory.createCache();
		createSenderAndSendMessages(pulsarClient, Schema.STRING, "my-topic", cache, new String[] { "a", "b", "c" });

		ReactiveMessageSender<String> sender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
				.messageSender(Schema.STRING).topic("my-topic").cache(cache).build();

		sender.sendOne(MessageSpec.of("a")).block(Duration.ofSeconds(5));
		sender.sendOne(MessageSpec.of("b")).block(Duration.ofSeconds(5));

		verify(pulsarClient).createProducerAsync(any(), any(), isNull());

		// Disconnect the producer and send a new message
		doReturn(false).when(producer).isConnected();
		CompletableFuture<MessageId> messageIdFuture = sender.sendOne(MessageSpec.of("c")).toFuture();

		Thread.sleep(100);

		// Check that the disconnected producer is flushed and closed and a new producer
		// is created
		verify(producer).closeAsync();
		verify(producer).flushAsync();
		verify(pulsarClient, times(2)).createProducerAsync(any(), any(), isNull());

		// Check that we wait for the producer to be reconnected
		Thread.sleep(1000);
		assertThat(messageIdFuture.isDone()).isFalse();

		// Re-connect the producer
		doReturn(true).when(producer).isConnected();
		messageIdFuture.get(5, TimeUnit.SECONDS);

		verify(pulsarClient, times(2)).createProducerAsync(any(), any(), isNull());

		sender.sendOne(MessageSpec.of("d")).block(Duration.ofSeconds(5));
		verify(pulsarClient, times(2)).createProducerAsync(any(), any(), isNull());

		// Verify that an error is emitted if the producer doesn't reconnect in time
		doReturn(false).when(producer).isConnected();
		Duration reconnectTimeout = StepVerifier.create(sender.sendOne(MessageSpec.of("c")))
				.verifyError(IllegalStateException.class);

		assertThat(reconnectTimeout).isBetween(Duration.ofSeconds(4), Duration.ofSeconds(5));
	}

	@Test
	void maxInFlightUsingSendOne() throws Exception {
		doTestMaxInFlight((reactiveSender, inputFlux) -> inputFlux
				.flatMap((i) -> reactiveSender.sendOne(MessageSpec.of(String.valueOf(i))), 100));
	}

	@Disabled("This fails since sendMany doesn't currently work with InflightLimiter")
	@Test
	void maxInFlightUsingSendMany() throws Exception {
		doTestMaxInFlight((reactiveSender, inputFlux) -> inputFlux.window(3).flatMap(
				(subFlux) -> subFlux.map((i) -> MessageSpec.of(String.valueOf(i))).as(reactiveSender::sendMany), 100));
	}

	void doTestMaxInFlight(BiFunction<ReactiveMessageSender<String>, Flux<Integer>, Flux<MessageId>> sendingFunction)
			throws Exception {
		ScheduledExecutorService executorService = null;
		try {
			executorService = Executors.newSingleThreadScheduledExecutor();
			final ScheduledExecutorService finalExecutorService = executorService;
			PulsarClientImpl pulsarClient = spy(
					(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());
			AtomicLong totalRequests = new AtomicLong();
			AtomicLong requestsMax = new AtomicLong();
			ProducerBase<String> producer = mock(ProducerBase.class);
			given(producer.closeAsync()).willReturn(CompletableFuture.completedFuture(null));
			given(producer.isConnected()).willReturn(true);
			given(producer.newMessage()).willAnswer((__) -> {
				TypedMessageBuilderImpl<String> typedMessageBuilder = spy(
						new TypedMessageBuilderImpl<>(producer, Schema.STRING));
				given(typedMessageBuilder.sendAsync()).willAnswer((___) -> {
					CompletableFuture<MessageId> messageSender = new CompletableFuture<>();
					finalExecutorService.execute(() -> {
						long current = totalRequests.incrementAndGet();
						requestsMax.accumulateAndGet(current, Math::max);
					});
					finalExecutorService.schedule(() -> {
						totalRequests.decrementAndGet();
						// encode integer in message value to entry id in message id
						int encodedEntryId = Integer.parseInt(typedMessageBuilder.getMessage().getValue());
						messageSender.complete(
								DefaultImplementation.getDefaultImplementation().newMessageId(1, encodedEntryId, 1));
					}, 5, TimeUnit.MILLISECONDS);
					return messageSender;
				});
				return typedMessageBuilder;
			});

			given(pulsarClient.createProducerAsync(any(), eq(Schema.STRING), isNull()))
					.willReturn(CompletableFuture.completedFuture(producer));

			ReactiveMessageSender<String> reactiveSender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
					.messageSender(Schema.STRING).maxInflight(7).cache(AdaptedReactivePulsarClientFactory.createCache())
					.maxConcurrentSenderSubscriptions(1024).topic("my-topic").build();

			List<Integer> inputValues = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());

			Flux<Integer> inputFlux = Flux.fromIterable(inputValues);
			Flux<MessageId> outputFlux = sendingFunction.apply(reactiveSender, inputFlux);

			// get message value from encoded entry id in message id
			List<Integer> outputValues = outputFlux.map((m) -> (int) ((MessageIdImpl) m).getEntryId()).collectList()
					.block();
			assertThat(outputValues).containsExactlyInAnyOrderElementsOf(inputValues);
			assertThat(requestsMax.get()).isEqualTo(7);
		}
		finally {
			if (executorService != null) {
				executorService.shutdownNow();
			}
		}
	}

}
