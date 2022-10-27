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

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MessageResult;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumer;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerSpec;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

class AdaptedReactiveMessageConsumer<T> implements ReactiveMessageConsumer<T> {

	private final ReactiveConsumerAdapterFactory reactiveConsumerAdapterFactory;

	private final Schema<T> schema;

	private final ReactiveMessageConsumerSpec consumerSpec;

	private final boolean acknowledgeAsynchronously;

	AdaptedReactiveMessageConsumer(ReactiveConsumerAdapterFactory reactiveConsumerAdapterFactory, Schema<T> schema,
			ReactiveMessageConsumerSpec consumerSpec) {
		this.reactiveConsumerAdapterFactory = reactiveConsumerAdapterFactory;
		this.schema = schema;
		this.consumerSpec = consumerSpec;
		this.acknowledgeAsynchronously = consumerSpec.getAcknowledgeAsynchronously() == null
				|| consumerSpec.getAcknowledgeAsynchronously() != null && consumerSpec.getAcknowledgeAsynchronously();
	}

	static <T> Mono<Message<T>> readNextMessage(Consumer<T> consumer) {
		return PulsarFutureAdapter.adaptPulsarFuture(consumer::receiveAsync);
	}

	@Override
	public <R> Mono<R> consumeOne(Function<Mono<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return createReactiveConsumerAdapter().usingConsumer((consumer) -> Mono.using(this::pinAcknowledgeScheduler,
				(pinnedAcknowledgeScheduler) -> Mono.from(messageHandler.apply(readNextMessage(consumer))).delayUntil(
						(messageResult) -> handleAcknowledgement(consumer, messageResult, pinnedAcknowledgeScheduler))
						.handle(this::handleMessageResult),
				Scheduler::dispose));
	}

	private Scheduler pinAcknowledgeScheduler() {
		return Schedulers.single((this.consumerSpec.getAcknowledgeScheduler() != null)
				? this.consumerSpec.getAcknowledgeScheduler() : Schedulers.boundedElastic());
	}

	private <R> Mono<?> handleAcknowledgement(Consumer<T> consumer, MessageResult<R> messageResult,
			Scheduler pinnedAcknowledgeScheduler) {
		if (messageResult.getMessageId() != null) {
			Mono<Void> acknowledgementMono;
			if (messageResult.isAcknowledgeMessage()) {
				acknowledgementMono = Mono.fromFuture(() -> consumer.acknowledgeAsync(messageResult.getMessageId()));
			}
			else {
				acknowledgementMono = Mono
						.fromRunnable(() -> consumer.negativeAcknowledge(messageResult.getMessageId()));
			}
			acknowledgementMono = acknowledgementMono.subscribeOn(pinnedAcknowledgeScheduler);
			if (this.acknowledgeAsynchronously) {
				return Mono.fromRunnable(acknowledgementMono::subscribe);
			}
			else {
				return acknowledgementMono;
			}
		}
		else {
			return Mono.empty();
		}
	}

	private ReactiveConsumerAdapter<T> createReactiveConsumerAdapter() {
		return this.reactiveConsumerAdapterFactory.create((pulsarClient) -> {
			ConsumerBuilder<T> consumerBuilder = pulsarClient.newConsumer(this.schema);
			configureConsumerBuilder(consumerBuilder);
			return consumerBuilder;
		});
	}

	private void configureConsumerBuilder(ConsumerBuilder<T> consumerBuilder) {
		if (this.consumerSpec.getTopicNames() != null && !this.consumerSpec.getTopicNames().isEmpty()) {
			consumerBuilder.topics(this.consumerSpec.getTopicNames());
		}
		if (this.consumerSpec.getTopicsPattern() != null) {
			consumerBuilder.topicsPattern(this.consumerSpec.getTopicsPattern());
		}
		if (this.consumerSpec.getTopicsPatternSubscriptionMode() != null) {
			consumerBuilder.subscriptionTopicsMode(this.consumerSpec.getTopicsPatternSubscriptionMode());
		}
		if (this.consumerSpec.getTopicsPatternAutoDiscoveryPeriod() != null) {
			consumerBuilder.patternAutoDiscoveryPeriod(
					(int) (this.consumerSpec.getTopicsPatternAutoDiscoveryPeriod().toMillis() / 1000L),
					TimeUnit.SECONDS);
		}
		if (this.consumerSpec.getSubscriptionName() != null) {
			consumerBuilder.subscriptionName(this.consumerSpec.getSubscriptionName());
		}
		if (this.consumerSpec.getSubscriptionMode() != null) {
			consumerBuilder.subscriptionMode(this.consumerSpec.getSubscriptionMode());
		}
		if (this.consumerSpec.getSubscriptionType() != null) {
			consumerBuilder.subscriptionType(this.consumerSpec.getSubscriptionType());
		}
		if (this.consumerSpec.getKeySharedPolicy() != null) {
			consumerBuilder.keySharedPolicy(this.consumerSpec.getKeySharedPolicy());
		}
		if (this.consumerSpec.getReplicateSubscriptionState() != null) {
			consumerBuilder.replicateSubscriptionState(this.consumerSpec.getReplicateSubscriptionState());
		}
		if (this.consumerSpec.getSubscriptionProperties() != null
				&& !this.consumerSpec.getSubscriptionProperties().isEmpty()) {
			consumerBuilder.subscriptionProperties(this.consumerSpec.getSubscriptionProperties());
		}
		if (this.consumerSpec.getConsumerName() != null) {
			consumerBuilder.consumerName(this.consumerSpec.getConsumerName());
		}
		if (this.consumerSpec.getProperties() != null && !this.consumerSpec.getProperties().isEmpty()) {
			consumerBuilder.properties(this.consumerSpec.getProperties());
		}
		if (this.consumerSpec.getPriorityLevel() != null) {
			consumerBuilder.priorityLevel(this.consumerSpec.getPriorityLevel());
		}
		if (this.consumerSpec.getReadCompacted() != null) {
			consumerBuilder.readCompacted(this.consumerSpec.getReadCompacted());
		}
		if (this.consumerSpec.getBatchIndexAckEnabled() != null) {
			consumerBuilder.enableBatchIndexAcknowledgment(this.consumerSpec.getBatchIndexAckEnabled());
		}
		if (this.consumerSpec.getAckTimeout() != null) {
			consumerBuilder.ackTimeout(this.consumerSpec.getAckTimeout().toMillis(), TimeUnit.MILLISECONDS);
		}
		if (this.consumerSpec.getAckTimeoutTickTime() != null) {
			consumerBuilder.ackTimeoutTickTime(this.consumerSpec.getAckTimeoutTickTime().toMillis(),
					TimeUnit.MILLISECONDS);
		}
		if (this.consumerSpec.getAcknowledgementsGroupTime() != null) {
			consumerBuilder.acknowledgmentGroupTime(this.consumerSpec.getAcknowledgementsGroupTime().toMillis(),
					TimeUnit.MILLISECONDS);
		}
		if (this.consumerSpec.getNegativeAckRedeliveryDelay() != null) {
			consumerBuilder.negativeAckRedeliveryDelay(this.consumerSpec.getNegativeAckRedeliveryDelay().toMillis(),
					TimeUnit.MILLISECONDS);
		}
		if (this.consumerSpec.getDeadLetterPolicy() != null) {
			consumerBuilder.deadLetterPolicy(this.consumerSpec.getDeadLetterPolicy());
		}
		if (this.consumerSpec.getRetryLetterTopicEnable() != null) {
			consumerBuilder.enableRetry(this.consumerSpec.getRetryLetterTopicEnable());
		}
		if (this.consumerSpec.getReceiverQueueSize() != null) {
			consumerBuilder.receiverQueueSize(this.consumerSpec.getReceiverQueueSize());
		}
		if (this.consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions() != null) {
			consumerBuilder.maxTotalReceiverQueueSizeAcrossPartitions(
					this.consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions());
		}
		if (this.consumerSpec.getAutoUpdatePartitions() != null) {
			consumerBuilder.autoUpdatePartitions(this.consumerSpec.getAutoUpdatePartitions());
		}
		if (this.consumerSpec.getAutoUpdatePartitionsInterval() != null) {
			consumerBuilder.autoUpdatePartitionsInterval(
					(int) (this.consumerSpec.getAutoUpdatePartitionsInterval().toMillis() / 1000L), TimeUnit.SECONDS);
		}
		if (this.consumerSpec.getCryptoKeyReader() != null) {
			consumerBuilder.cryptoKeyReader(this.consumerSpec.getCryptoKeyReader());
		}
		if (this.consumerSpec.getCryptoFailureAction() != null) {
			consumerBuilder.cryptoFailureAction(this.consumerSpec.getCryptoFailureAction());
		}
		if (this.consumerSpec.getMaxPendingChunkedMessage() != null) {
			consumerBuilder.maxPendingChunkedMessage(this.consumerSpec.getMaxPendingChunkedMessage());
		}
		if (this.consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull() != null) {
			consumerBuilder.autoAckOldestChunkedMessageOnQueueFull(
					this.consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull());
		}
		if (this.consumerSpec.getExpireTimeOfIncompleteChunkedMessage() != null) {
			consumerBuilder.expireTimeOfIncompleteChunkedMessage(
					this.consumerSpec.getExpireTimeOfIncompleteChunkedMessage().toMillis(), TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public <R> Flux<R> consumeMany(Function<Flux<Message<T>>, Publisher<MessageResult<R>>> messageHandler) {
		return createReactiveConsumerAdapter().usingConsumerMany((consumer) -> Flux.using(
				this::pinAcknowledgeScheduler, (
						pinnedAcknowledgeScheduler) -> Flux
								.from(messageHandler.apply(readNextMessage(consumer).repeat()))
								.delayUntil((messageResult) -> handleAcknowledgement(consumer, messageResult,
										pinnedAcknowledgeScheduler))
								.handle(this::handleMessageResult),
				Scheduler::dispose));
	}

	@Override
	public Mono<Void> consumeNothing() {
		return createReactiveConsumerAdapter().usingConsumer((consumer) -> Mono.empty());
	}

	private <R> void handleMessageResult(MessageResult<R> messageResult, SynchronousSink<R> sink) {
		R value = messageResult.getValue();
		if (value != null) {
			sink.next(value);
		}
	}

}
