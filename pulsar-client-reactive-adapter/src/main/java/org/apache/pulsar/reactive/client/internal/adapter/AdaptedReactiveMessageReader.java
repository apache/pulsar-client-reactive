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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.ReaderBuilder;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.EndOfStreamAction;
import org.apache.pulsar.reactive.client.api.InstantStartAtSpec;
import org.apache.pulsar.reactive.client.api.MessageIdStartAtSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReader;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.StartAtSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AdaptedReactiveMessageReader<T> implements ReactiveMessageReader<T> {

	private final Schema<T> schema;

	private final ReactiveMessageReaderSpec readerSpec;

	private final ReactiveReaderAdapterFactory reactiveReaderAdapterFactory;

	private final StartAtSpec startAtSpec;

	private final EndOfStreamAction endOfStreamAction;

	AdaptedReactiveMessageReader(ReactiveReaderAdapterFactory reactiveReaderAdapterFactory, Schema<T> schema,
			ReactiveMessageReaderSpec readerSpec, StartAtSpec startAtSpec, EndOfStreamAction endOfStreamAction) {
		this.schema = schema;
		this.readerSpec = readerSpec;
		this.reactiveReaderAdapterFactory = reactiveReaderAdapterFactory;
		this.startAtSpec = startAtSpec;
		this.endOfStreamAction = endOfStreamAction;
	}

	static <T> Mono<Message<T>> readNextMessage(Reader<T> reader, EndOfStreamAction endOfStreamAction) {
		Mono<Message<T>> messageMono = PulsarFutureAdapter.adaptPulsarFuture(reader::readNextAsync);
		if (endOfStreamAction == EndOfStreamAction.COMPLETE) {
			return PulsarFutureAdapter.adaptPulsarFuture(reader::hasMessageAvailableAsync).filter(Boolean::booleanValue)
					.flatMap((__) -> messageMono);
		}
		else {
			return messageMono;
		}
	}

	ReactiveReaderAdapter<T> createReactiveReaderAdapter(StartAtSpec startAtSpec) {
		return this.reactiveReaderAdapterFactory.create(readerStartingAt(startAtSpec));
	}

	private Function<PulsarClient, ReaderBuilder<T>> readerStartingAt(StartAtSpec startAtSpec) {
		return (pulsarClient) -> {
			ReaderBuilder<T> readerBuilder = pulsarClient.newReader(this.schema);
			if (startAtSpec != null) {
				if (startAtSpec instanceof MessageIdStartAtSpec) {
					MessageIdStartAtSpec messageIdStartAtSpec = (MessageIdStartAtSpec) startAtSpec;
					readerBuilder.startMessageId(messageIdStartAtSpec.getMessageId());
					if (messageIdStartAtSpec.isInclusive()) {
						readerBuilder.startMessageIdInclusive();
					}
				}
				else {
					InstantStartAtSpec instantStartAtSpec = (InstantStartAtSpec) startAtSpec;
					long rollbackDuration = ChronoUnit.SECONDS.between(instantStartAtSpec.getInstant(), Instant.now())
							+ 1L;
					if (rollbackDuration < 0L) {
						throw new IllegalArgumentException("InstantStartAtSpec must be in the past.");
					}
					readerBuilder.startMessageFromRollbackDuration(rollbackDuration, TimeUnit.SECONDS);
				}
			}
			configureReaderBuilder(readerBuilder);
			return readerBuilder;
		};
	}

	private void configureReaderBuilder(ReaderBuilder<T> readerBuilder) {
		readerBuilder.topics(this.readerSpec.getTopicNames());
		if (this.readerSpec.getReaderName() != null) {
			readerBuilder.readerName(this.readerSpec.getReaderName());
		}
		if (this.readerSpec.getSubscriptionName() != null) {
			readerBuilder.subscriptionName(this.readerSpec.getSubscriptionName());
		}
		if (this.readerSpec.getGeneratedSubscriptionNamePrefix() != null) {
			readerBuilder.subscriptionRolePrefix(this.readerSpec.getGeneratedSubscriptionNamePrefix());
		}
		if (this.readerSpec.getReceiverQueueSize() != null) {
			readerBuilder.receiverQueueSize(this.readerSpec.getReceiverQueueSize());
		}
		if (this.readerSpec.getReadCompacted() != null) {
			readerBuilder.readCompacted(this.readerSpec.getReadCompacted());
		}
		if (this.readerSpec.getKeyHashRanges() != null && !this.readerSpec.getKeyHashRanges().isEmpty()) {
			readerBuilder.keyHashRange(this.readerSpec.getKeyHashRanges().toArray(new Range[0]));
		}
		if (this.readerSpec.getCryptoKeyReader() != null) {
			readerBuilder.cryptoKeyReader(this.readerSpec.getCryptoKeyReader());
		}
		if (this.readerSpec.getCryptoFailureAction() != null) {
			readerBuilder.cryptoFailureAction(this.readerSpec.getCryptoFailureAction());
		}
	}

	@Override
	public Mono<Message<T>> readOne() {
		return createReactiveReaderAdapter(this.startAtSpec)
				.usingReader((reader) -> readNextMessage(reader, this.endOfStreamAction));
	}

	@Override
	public Flux<Message<T>> readMany() {
		return createReactiveReaderAdapter(this.startAtSpec).usingReaderMany((reader) -> {
			Mono<Message<T>> messageMono = readNextMessage(reader, this.endOfStreamAction);
			if (this.endOfStreamAction == EndOfStreamAction.COMPLETE) {
				return messageMono.repeatWhen((flux) -> flux.takeWhile((emitted) -> emitted > 0L));
			}
			return messageMono.repeat();
		});
	}

}
