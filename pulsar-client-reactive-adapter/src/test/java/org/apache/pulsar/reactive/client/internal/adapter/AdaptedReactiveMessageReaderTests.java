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
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.PulsarClientImpl;
import org.apache.pulsar.client.impl.conf.ReaderConfigurationData;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.EndOfStreamAction;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReader;
import org.apache.pulsar.reactive.client.api.StartAtSpec;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AdaptedReactiveMessageReader}.
 */
class AdaptedReactiveMessageReaderTests {

	@Test
	void readOne() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true)).when(reader).hasMessageAvailableAsync();
		Message<String> message = mock(Message.class);
		doReturn(CompletableFuture.completedFuture(message)).when(reader).readNextAsync();

		CryptoKeyReader cryptoKeyReader = mock(CryptoKeyReader.class);

		ReaderConfigurationData<String> expectedReaderConf = new ReaderConfigurationData<>();
		expectedReaderConf.setTopicNames(Collections.singleton("my-topic"));
		expectedReaderConf.setReaderName("my-reader");
		expectedReaderConf.setSubscriptionName("my-sub");
		expectedReaderConf.setSubscriptionRolePrefix("my-prefix-");
		expectedReaderConf.setStartMessageId(MessageId.latest);
		expectedReaderConf.setResetIncludeHead(true);
		expectedReaderConf.setReceiverQueueSize(1);
		expectedReaderConf.setReadCompacted(true);
		expectedReaderConf.setCryptoKeyReader(cryptoKeyReader);
		expectedReaderConf.setKeyHashRanges(Collections.singletonList(new Range(2, 3)));
		expectedReaderConf.setCryptoFailureAction(ConsumerCryptoFailureAction.DISCARD);

		CompletableFuture<String> failedReader = new CompletableFuture<>();
		failedReader.completeExceptionally(new RuntimeException("didn't match expected reader conf"));
		doReturn(failedReader).when(pulsarClient).createReaderAsync(any(), eq(Schema.STRING));
		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(eq(expectedReaderConf), eq(Schema.STRING));

		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.readerName("my-reader")
			.subscriptionName("my-sub")
			.generatedSubscriptionNamePrefix("my-prefix-")
			.startAtSpec(StartAtSpec.ofLatestInclusive())
			.receiverQueueSize(1)
			.readCompacted(true)
			.cryptoKeyReader(cryptoKeyReader)
			.keyHashRanges(Collections.singletonList(new Range(2, 3)))
			.cryptoFailureAction(ConsumerCryptoFailureAction.DISCARD)
			.clone()
			.build();

		Message<String> messageRead = reactiveReader.readOne().block(Duration.ofSeconds(5));

		verify(pulsarClient).createReaderAsync(any(), any());
		assertThat(messageRead).isSameAs(message);
	}

	@Test
	void readMany() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true), CompletableFuture.completedFuture(true),
				CompletableFuture.completedFuture(false))
			.when(reader)
			.hasMessageAvailableAsync();
		Message<String> message1 = mock(Message.class);
		Message<String> message2 = mock(Message.class);

		doReturn(CompletableFuture.completedFuture(message1), CompletableFuture.completedFuture(message2),
				new CompletableFuture<String>())
			.when(reader)
			.readNextAsync();

		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(any(), eq(Schema.STRING));

		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.build();

		StepVerifier.create(reactiveReader.readMany()).expectNext(message1).expectNext(message2).verifyComplete();

		verify(reader, times(2)).readNextAsync();
	}

	@Test
	void readPulsarException() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true)).when(reader).hasMessageAvailableAsync();

		CompletableFuture<String> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(new PulsarClientException.InvalidMessageException("test"));
		doReturn(failedFuture).when(reader).readNextAsync();

		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(any(), eq(Schema.STRING));

		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.build();

		StepVerifier.create(reactiveReader.readOne()).verifyError(PulsarClientException.InvalidMessageException.class);
		StepVerifier.create(reactiveReader.readMany()).verifyError(PulsarClientException.InvalidMessageException.class);
	}

	@Test
	void endOfStreamPoll() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true), CompletableFuture.completedFuture(true),
				CompletableFuture.completedFuture(false))
			.when(reader)
			.hasMessageAvailableAsync();
		Message<String> message1 = mock(Message.class);
		Message<String> message2 = mock(Message.class);

		doReturn(CompletableFuture.completedFuture(message1), CompletableFuture.completedFuture(message2),
				new CompletableFuture<String>())
			.when(reader)
			.readNextAsync();

		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(any(), eq(Schema.STRING));

		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.endOfStreamAction(EndOfStreamAction.POLL)
			.build();

		StepVerifier.create(reactiveReader.readMany().timeout(Duration.ofMillis(100)))
			.expectNext(message1)
			.expectNext(message2)
			.verifyError();

		verify(reader, times(3)).readNextAsync();
	}

	@Test
	void startAtInstant() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true)).when(reader).hasMessageAvailableAsync();
		Message<String> message = mock(Message.class);
		doReturn(CompletableFuture.completedFuture(message)).when(reader).readNextAsync();

		ReaderConfigurationData<String> expectedReaderConf = new ReaderConfigurationData<>();
		expectedReaderConf.setTopicNames(Collections.singleton("my-topic"));
		expectedReaderConf.setStartMessageId(MessageId.earliest);
		expectedReaderConf.setStartMessageFromRollbackDurationInSec(6);

		CompletableFuture<String> failedReader = new CompletableFuture<>();
		failedReader.completeExceptionally(new RuntimeException("didn't match expected reader conf"));
		doReturn(failedReader).when(pulsarClient).createReaderAsync(any(), eq(Schema.STRING));
		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(eq(expectedReaderConf), eq(Schema.STRING));

		Instant now = Instant.now();
		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.startAtSpec(StartAtSpec.ofInstant(now.minusSeconds(5)))
			.build();

		try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
			mockedStatic.when(Instant::now).thenReturn(now);
			mockedStatic.when(() -> Instant.from(any())).thenCallRealMethod();
			Message<String> messageRead = reactiveReader.readOne().block(Duration.ofSeconds(5));
			verify(pulsarClient).createReaderAsync(any(), any());
			assertThat(messageRead).isSameAs(message);
		}

	}

	@Test
	void startAtInstantInTheFutureFails() throws Exception {
		PulsarClientImpl pulsarClient = spy(
				(PulsarClientImpl) PulsarClient.builder().serviceUrl("http://dummy").build());

		Reader<String> reader = mock(Reader.class);
		doReturn(CompletableFuture.completedFuture(null)).when(reader).closeAsync();
		doReturn(CompletableFuture.completedFuture(true)).when(reader).hasMessageAvailableAsync();
		Message<String> message = mock(Message.class);
		doReturn(CompletableFuture.completedFuture(message)).when(reader).readNextAsync();

		doReturn(CompletableFuture.completedFuture(reader)).when(pulsarClient)
			.createReaderAsync(any(), eq(Schema.STRING));

		ReactiveMessageReader<String> reactiveReader = AdaptedReactivePulsarClientFactory.create(pulsarClient)
			.messageReader(Schema.STRING)
			.topic("my-topic")
			.startAtSpec(StartAtSpec.ofInstant(Instant.now().plusSeconds(5)))
			.build();

		StepVerifier.create(reactiveReader.readOne()).verifyError(IllegalArgumentException.class);
	}

}
