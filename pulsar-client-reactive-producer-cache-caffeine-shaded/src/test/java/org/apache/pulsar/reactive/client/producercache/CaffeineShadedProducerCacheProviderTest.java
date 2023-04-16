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

package org.apache.pulsar.reactive.client.producercache;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.ProducerBase;
import org.apache.pulsar.client.impl.PulsarClientImpl;
import org.apache.pulsar.client.impl.TypedMessageBuilderImpl;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CaffeineShadedProducerCacheProviderTest {

	@ParameterizedTest
	@MethodSource
	void cacheProvider(String name, CaffeineShadedProducerCacheProvider cacheProvider) throws Exception {
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

		ReactiveMessageSenderCache cache = AdaptedReactivePulsarClientFactory.createCache(cacheProvider);

		ReactiveMessageSender<String> sender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
				.messageSender(Schema.STRING).topic("my-topic").cache(cache).build();

		sender.sendOne(MessageSpec.of("a")).then(sender.sendOne(MessageSpec.of("b")))
				.thenMany(Flux.just(MessageSpec.of("c")).as(sender::sendMany)).blockLast(Duration.ofSeconds(5));

		verify(pulsarClient).createProducerAsync(any(), any(), isNull());
	}

	private static Stream<Arguments> cacheProvider() {
		return Arrays.asList(Arguments.of("Default <init>", new CaffeineShadedProducerCacheProvider()), Arguments.of(
				"Params <init>",
				new CaffeineShadedProducerCacheProvider(Duration.ofMinutes(1), Duration.ofMinutes(10), 1000L, 50)))
				.stream();
	}

	@Test
	void loadedByServiceLoader() {
		ReactiveMessageSenderCache cache = AdaptedReactivePulsarClientFactory.createCache();
		assertThat(cache).extracting("cacheProvider").isInstanceOf(CaffeineShadedProducerCacheProvider.class);
	}

	@Test
	void caffeinePropsAreRespected() throws Exception {
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

		CaffeineShadedProducerCacheProvider cacheProvider = new CaffeineShadedProducerCacheProvider(
				Duration.ofMinutes(1), Duration.ofMillis(100), 100L, 50);
		ReactiveMessageSenderCache cache = AdaptedReactivePulsarClientFactory.createCache(cacheProvider);

		ReactiveMessageSender<String> sender = AdaptedReactivePulsarClientFactory.create(pulsarClient)
				.messageSender(Schema.STRING).topic("my-topic").cache(cache).build();

		sender.sendOne(MessageSpec.of("a")).then(sender.sendOne(MessageSpec.of("b")))
				.thenMany(Flux.just(MessageSpec.of("c")).as(sender::sendMany)).blockLast(Duration.ofSeconds(5));

		Thread.sleep(101);

		sender.sendOne(MessageSpec.of("d")).block(Duration.ofSeconds(5));

		verify(pulsarClient, times(2)).createProducerAsync(any(), any(), isNull());
	}

}
