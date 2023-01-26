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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.reactive.client.api.MessageSendResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSendingException;
import org.apache.pulsar.reactive.client.internal.api.InternalMessageSpec;
import org.apache.pulsar.reactive.client.internal.api.PublisherTransformer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AdaptedReactiveMessageSender<T> implements ReactiveMessageSender<T> {

	private final Schema<T> schema;

	private final ReactiveMessageSenderSpec senderSpec;

	private final int maxConcurrency;

	private final ReactiveProducerAdapterFactory reactiveProducerAdapterFactory;

	private final ProducerCache producerCache;

	private final Supplier<PublisherTransformer> producerActionTransformer;

	private final Object producerActionTransformerKey;

	AdaptedReactiveMessageSender(Schema<T> schema, ReactiveMessageSenderSpec senderSpec, int maxConcurrency,
			ReactiveProducerAdapterFactory reactiveProducerAdapterFactory, ProducerCache producerCache,
			Supplier<PublisherTransformer> producerActionTransformer, Object producerActionTransformerKey) {
		this.schema = schema;
		this.senderSpec = senderSpec;
		this.maxConcurrency = maxConcurrency;
		this.reactiveProducerAdapterFactory = reactiveProducerAdapterFactory;
		this.producerCache = producerCache;
		this.producerActionTransformer = producerActionTransformer;
		this.producerActionTransformerKey = producerActionTransformerKey;
	}

	ReactiveProducerAdapter<T> createReactiveProducerAdapter() {
		return this.reactiveProducerAdapterFactory.create((pulsarClient) -> {
			ProducerBuilder<T> producerBuilder = pulsarClient.newProducer(this.schema);
			configureProducerBuilder(producerBuilder);
			return producerBuilder;
		}, this.producerCache, this.producerActionTransformer, this.producerActionTransformerKey);
	}

	private void configureProducerBuilder(ProducerBuilder<T> producerBuilder) {
		if (this.senderSpec.getTopicName() != null) {
			producerBuilder.topic(this.senderSpec.getTopicName());
		}

		if (this.senderSpec.getProducerName() != null) {
			producerBuilder.producerName(this.senderSpec.getProducerName());
		}

		if (this.senderSpec.getSendTimeout() != null) {
			producerBuilder.sendTimeout((int) (this.senderSpec.getSendTimeout().toMillis() / 1000L), TimeUnit.SECONDS);
		}

		if (this.senderSpec.getMaxPendingMessages() != null) {
			producerBuilder.maxPendingMessages(this.senderSpec.getMaxPendingMessages());
		}

		if (this.senderSpec.getMaxPendingMessagesAcrossPartitions() != null) {
			producerBuilder.maxPendingMessagesAcrossPartitions(this.senderSpec.getMaxPendingMessagesAcrossPartitions());
		}

		if (this.senderSpec.getMessageRoutingMode() != null) {
			producerBuilder.messageRoutingMode(this.senderSpec.getMessageRoutingMode());
		}

		if (this.senderSpec.getHashingScheme() != null) {
			producerBuilder.hashingScheme(this.senderSpec.getHashingScheme());
		}

		if (this.senderSpec.getCryptoFailureAction() != null) {
			producerBuilder.cryptoFailureAction(this.senderSpec.getCryptoFailureAction());
		}

		if (this.senderSpec.getMessageRouter() != null) {
			producerBuilder.messageRouter(this.senderSpec.getMessageRouter());
		}

		if (this.senderSpec.getBatchingMaxPublishDelay() != null) {
			producerBuilder.batchingMaxPublishDelay(this.senderSpec.getBatchingMaxPublishDelay().toNanos(),
					TimeUnit.NANOSECONDS);
		}

		if (this.senderSpec.getRoundRobinRouterBatchingPartitionSwitchFrequency() != null) {
			producerBuilder.roundRobinRouterBatchingPartitionSwitchFrequency(
					this.senderSpec.getRoundRobinRouterBatchingPartitionSwitchFrequency());
		}

		if (this.senderSpec.getBatchingMaxMessages() != null) {
			producerBuilder.batchingMaxMessages(this.senderSpec.getBatchingMaxMessages());
		}

		if (this.senderSpec.getBatchingMaxBytes() != null) {
			producerBuilder.batchingMaxBytes(this.senderSpec.getBatchingMaxBytes());
		}

		if (this.senderSpec.getBatchingEnabled() != null) {
			producerBuilder.enableBatching(this.senderSpec.getBatchingEnabled());
		}

		if (this.senderSpec.getBatcherBuilder() != null) {
			producerBuilder.batcherBuilder(this.senderSpec.getBatcherBuilder());
		}

		if (this.senderSpec.getChunkingEnabled() != null) {
			producerBuilder.enableChunking(this.senderSpec.getChunkingEnabled());
		}

		if (this.senderSpec.getCryptoKeyReader() != null) {
			producerBuilder.cryptoKeyReader(this.senderSpec.getCryptoKeyReader());
		}

		if (this.senderSpec.getEncryptionKeys() != null && !this.senderSpec.getEncryptionKeys().isEmpty()) {
			this.senderSpec.getEncryptionKeys().forEach(producerBuilder::addEncryptionKey);
		}

		if (this.senderSpec.getCompressionType() != null) {
			producerBuilder.compressionType(this.senderSpec.getCompressionType());
		}

		if (this.senderSpec.getInitialSequenceId() != null) {
			producerBuilder.initialSequenceId(this.senderSpec.getInitialSequenceId());
		}

		if (this.senderSpec.getAutoUpdatePartitions() != null) {
			producerBuilder.autoUpdatePartitions(this.senderSpec.getAutoUpdatePartitions());
		}

		if (this.senderSpec.getAutoUpdatePartitionsInterval() != null) {
			producerBuilder.autoUpdatePartitionsInterval(
					(int) (this.senderSpec.getAutoUpdatePartitionsInterval().toMillis() / 1000L), TimeUnit.SECONDS);
		}

		if (this.senderSpec.getMultiSchema() != null) {
			producerBuilder.enableMultiSchema(this.senderSpec.getMultiSchema());
		}

		if (this.senderSpec.getAccessMode() != null) {
			producerBuilder.accessMode(this.senderSpec.getAccessMode());
		}

		if (this.senderSpec.getLazyStartPartitionedProducers() != null) {
			producerBuilder.enableLazyStartPartitionedProducers(this.senderSpec.getLazyStartPartitionedProducers());
		}

		if (this.senderSpec.getProperties() != null && !this.senderSpec.getProperties().isEmpty()) {
			producerBuilder
					.properties(Collections.unmodifiableMap(new LinkedHashMap<>(this.senderSpec.getProperties())));
		}
	}

	@Override
	public Mono<MessageId> sendOne(MessageSpec<T> messageSpec) {
		return createReactiveProducerAdapter()
				.usingProducer((producer, transformer) -> createMessageMono(messageSpec, producer, transformer));
	}

	private Mono<MessageId> createMessageMonoWrapped(MessageSpec<T> messageSpec, Producer<T> producer,
			PublisherTransformer transformer) {
		return createMessageMono(messageSpec, producer, transformer)
				.onErrorResume((throwable) -> Mono.error(new ReactiveMessageSendingException(throwable, messageSpec)));
	}

	private Mono<MessageId> createMessageMono(MessageSpec<T> messageSpec, Producer<T> producer,
			PublisherTransformer transformer) {
		return PulsarFutureAdapter.adaptPulsarFuture(() -> {
			TypedMessageBuilder<T> typedMessageBuilder = producer.newMessage();
			((InternalMessageSpec<T>) messageSpec).configure(typedMessageBuilder);
			return typedMessageBuilder.sendAsync();
		}).transform(transformer::transform);
	}

	@Override
	public Flux<MessageSendResult<T>> sendMany(Publisher<MessageSpec<T>> messageSpecs) {
		return createReactiveProducerAdapter()
				.usingProducerMany((producer, transformer) -> Flux.from(messageSpecs).flatMapSequential(
						(messageSpec) -> createMessageMonoWrapped(messageSpec, producer, transformer)
								.map((messageId) -> new MessageSendResult<>(messageId, messageSpec)),
						this.maxConcurrency));
	}

}
