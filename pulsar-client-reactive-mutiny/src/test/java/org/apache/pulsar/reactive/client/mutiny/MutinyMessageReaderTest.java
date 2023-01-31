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
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.EndOfStreamAction;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReader;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.api.StartAtSpec;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageReader;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MutinyMessageReader}.
 */
class MutinyMessageReaderTest {

	@Test
	void readOne() throws Exception {
		ReactiveMessageReader<String> reader = new ReactiveMessageReader<String>() {
			@Override
			public Mono<Message<String>> readOne() {
				return Mono.just(new TestMessage("test-message"));
			}

			@Override
			public Flux<Message<String>> readMany() {
				throw new UnsupportedOperationException("should not be called");
			}
		};
		MutinyMessageReader<String> mutinyReader = createMutinyMessageReader(reader);
		assertThat(mutinyReader.readOne().subscribeAsCompletionStage().get(5, TimeUnit.SECONDS).getValue())
				.isEqualTo("test-message");
	}

	@Test
	void readMany() {
		ReactiveMessageReader<String> reader = new ReactiveMessageReader<String>() {
			@Override
			public Mono<Message<String>> readOne() {
				throw new UnsupportedOperationException("should not be called");
			}

			@Override
			public Flux<Message<String>> readMany() {
				return Flux.just(new TestMessage("test-message-1"), new TestMessage("test-message-2"));
			}
		};
		MutinyMessageReader<String> mutinyReader = createMutinyMessageReader(reader);
		mutinyReader.readMany().map(Message::getValue).subscribe().withSubscriber(AssertSubscriber.create(2))
				.assertCompleted().assertItems("test-message-1", "test-message-2");

	}

	private static MutinyMessageReader<String> createMutinyMessageReader(ReactiveMessageReader<String> reader) {
		ReactiveMessageReaderBuilder<String> readerBuilder = new ReactiveMessageReaderBuilder<String>() {
			@Override
			public ReactiveMessageReaderBuilder<String> startAtSpec(StartAtSpec startAtSpec) {
				return null;
			}

			@Override
			public ReactiveMessageReaderBuilder<String> endOfStreamAction(EndOfStreamAction endOfStreamAction) {
				return null;
			}

			@Override
			public MutableReactiveMessageReaderSpec getMutableSpec() {
				return null;
			}

			@Override
			public ReactiveMessageReaderBuilder<String> clone() {
				return null;
			}

			@Override
			public ReactiveMessageReader<String> build() {
				return reader;
			}
		};

		ReactivePulsarClient reactiveClient = new ReactivePulsarClient() {

			@Override
			public <T> ReactiveMessageSenderBuilder<T> messageSender(Schema<T> schema) {
				return null;
			}

			@Override
			public <T> ReactiveMessageReaderBuilder<T> messageReader(Schema<T> schema) {
				return (ReactiveMessageReaderBuilder<T>) readerBuilder;
			}

			@Override
			public <T> ReactiveMessageConsumerBuilder<T> messageConsumer(Schema<T> schema) {
				return null;
			}
		};

		return MutinyPulsarClientFactory.create(reactiveClient).messageReader(Schema.STRING).build();
	}

}
