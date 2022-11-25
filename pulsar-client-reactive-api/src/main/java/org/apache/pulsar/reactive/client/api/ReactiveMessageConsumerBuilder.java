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

package org.apache.pulsar.reactive.client.api;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

public interface ReactiveMessageConsumerBuilder<T> {

	default ReactiveMessageConsumerBuilder<T> applySpec(ReactiveMessageConsumerSpec consumerSpec) {
		getMutableSpec().applySpec(consumerSpec);
		return this;
	}

	default ReactiveMessageConsumerSpec toImmutableSpec() {
		return new ImmutableReactiveMessageConsumerSpec(getMutableSpec());
	}

	MutableReactiveMessageConsumerSpec getMutableSpec();

	default ReactiveMessageConsumerBuilder<T> topic(String topicName) {
		getMutableSpec().getTopicNames().add(topicName);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> topic(String... topicNames) {
		for (String topicName : topicNames) {
			getMutableSpec().getTopicNames().add(topicName);
		}
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> topicNames(List<String> topicNames) {
		getMutableSpec().setTopicNames(topicNames);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> topicsPattern(Pattern topicsPattern) {
		getMutableSpec().setTopicsPattern(topicsPattern);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> topicsPatternSubscriptionMode(
			RegexSubscriptionMode topicsPatternSubscriptionMode) {
		getMutableSpec().setTopicsPatternSubscriptionMode(topicsPatternSubscriptionMode);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> topicsPatternAutoDiscoveryPeriod(
			Duration topicsPatternAutoDiscoveryPeriod) {
		getMutableSpec().setTopicsPatternAutoDiscoveryPeriod(topicsPatternAutoDiscoveryPeriod);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionName(String subscriptionName) {
		getMutableSpec().setSubscriptionName(subscriptionName);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionMode(SubscriptionMode subscriptionMode) {
		getMutableSpec().setSubscriptionMode(subscriptionMode);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionType(SubscriptionType subscriptionType) {
		getMutableSpec().setSubscriptionType(subscriptionType);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionInitialPosition(
			SubscriptionInitialPosition subscriptionInitialPosition) {
		getMutableSpec().setSubscriptionInitialPosition(subscriptionInitialPosition);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> keySharedPolicy(KeySharedPolicy keySharedPolicy) {
		getMutableSpec().setKeySharedPolicy(keySharedPolicy);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> replicateSubscriptionState(boolean replicateSubscriptionState) {
		getMutableSpec().setReplicateSubscriptionState(replicateSubscriptionState);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionProperties(Map<String, String> subscriptionProperties) {
		getMutableSpec().setSubscriptionProperties(subscriptionProperties);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> subscriptionProperty(String key, String value) {
		if (getMutableSpec().getSubscriptionProperties() == null) {
			getMutableSpec().setSubscriptionProperties(new LinkedHashMap<>());
		}
		getMutableSpec().getSubscriptionProperties().put(key, value);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> consumerName(String consumerName) {
		getMutableSpec().setConsumerName(consumerName);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> properties(Map<String, String> properties) {
		getMutableSpec().setProperties(properties);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> property(String key, String value) {
		if (getMutableSpec().getProperties() == null) {
			getMutableSpec().setProperties(new LinkedHashMap<>());
		}
		getMutableSpec().getProperties().put(key, value);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> priorityLevel(Integer priorityLevel) {
		getMutableSpec().setPriorityLevel(priorityLevel);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> readCompacted(boolean readCompacted) {
		getMutableSpec().setReadCompacted(readCompacted);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> batchIndexAckEnabled(boolean batchIndexAckEnabled) {
		getMutableSpec().setBatchIndexAckEnabled(batchIndexAckEnabled);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> ackTimeout(Duration ackTimeout) {
		getMutableSpec().setAckTimeout(ackTimeout);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> ackTimeoutTickTime(Duration ackTimeoutTickTime) {
		getMutableSpec().setAckTimeoutTickTime(ackTimeoutTickTime);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> acknowledgementsGroupTime(Duration acknowledgementsGroupTime) {
		getMutableSpec().setAcknowledgementsGroupTime(acknowledgementsGroupTime);
		return this;
	}

	/**
	 * When set to true, ignores the acknowledge operation completion and makes it
	 * asynchronous from the message consuming processing to improve performance by
	 * allowing the acknowledges and message processing to interleave. Defaults to true.
	 * @param acknowledgeAsynchronously when set to true, ignores the acknowledge
	 * operation completion
	 * @return the current ReactiveMessageConsumerFactory instance (this)
	 */
	default ReactiveMessageConsumerBuilder<T> acknowledgeAsynchronously(boolean acknowledgeAsynchronously) {
		getMutableSpec().setAcknowledgeAsynchronously(acknowledgeAsynchronously);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> acknowledgeScheduler(Scheduler acknowledgeScheduler) {
		getMutableSpec().setAcknowledgeScheduler(acknowledgeScheduler);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> negativeAckRedeliveryDelay(Duration negativeAckRedeliveryDelay) {
		getMutableSpec().setNegativeAckRedeliveryDelay(negativeAckRedeliveryDelay);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> deadLetterPolicy(DeadLetterPolicy deadLetterPolicy) {
		getMutableSpec().setDeadLetterPolicy(deadLetterPolicy);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> retryLetterTopicEnable(boolean retryLetterTopicEnable) {
		getMutableSpec().setRetryLetterTopicEnable(retryLetterTopicEnable);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> receiverQueueSize(Integer receiverQueueSize) {
		getMutableSpec().setReceiverQueueSize(receiverQueueSize);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> maxTotalReceiverQueueSizeAcrossPartitions(
			Integer maxTotalReceiverQueueSizeAcrossPartitions) {
		getMutableSpec().setMaxTotalReceiverQueueSizeAcrossPartitions(maxTotalReceiverQueueSizeAcrossPartitions);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> autoUpdatePartitions(boolean autoUpdatePartitions) {
		getMutableSpec().setAutoUpdatePartitions(autoUpdatePartitions);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> autoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		getMutableSpec().setAutoUpdatePartitionsInterval(autoUpdatePartitionsInterval);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> cryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> maxPendingChunkedMessage(Integer maxPendingChunkedMessage) {
		getMutableSpec().setMaxPendingChunkedMessage(maxPendingChunkedMessage);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> autoAckOldestChunkedMessageOnQueueFull(
			boolean autoAckOldestChunkedMessageOnQueueFull) {
		getMutableSpec().setAutoAckOldestChunkedMessageOnQueueFull(autoAckOldestChunkedMessageOnQueueFull);
		return this;
	}

	default ReactiveMessageConsumerBuilder<T> expireTimeOfIncompleteChunkedMessage(
			Duration expireTimeOfIncompleteChunkedMessage) {
		getMutableSpec().setExpireTimeOfIncompleteChunkedMessage(expireTimeOfIncompleteChunkedMessage);
		return this;
	}

	ReactiveMessageConsumer<T> build();

}
