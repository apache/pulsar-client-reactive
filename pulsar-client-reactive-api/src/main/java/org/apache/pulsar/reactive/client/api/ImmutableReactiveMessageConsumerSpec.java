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

package org.apache.pulsar.reactive.client.api;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

/**
 * Immutable spec for a {@link ReactiveMessageConsumer}.
 */
public class ImmutableReactiveMessageConsumerSpec implements ReactiveMessageConsumerSpec {

	private final List<String> topicNames;

	private final Pattern topicsPattern;

	private final RegexSubscriptionMode topicsPatternSubscriptionMode;

	private final Duration topicsPatternAutoDiscoveryPeriod;

	private final String subscriptionName;

	private final SubscriptionMode subscriptionMode;

	private final SubscriptionType subscriptionType;

	private final SubscriptionInitialPosition subscriptionInitialPosition;

	private final KeySharedPolicy keySharedPolicy;

	private final Boolean replicateSubscriptionState;

	private final Map<String, String> subscriptionProperties;

	private final String consumerName;

	private final Map<String, String> properties;

	private final Integer priorityLevel;

	private final Boolean readCompacted;

	private final Boolean batchIndexAckEnabled;

	private final Duration ackTimeout;

	private final Duration ackTimeoutTickTime;

	private final Duration acknowledgementsGroupTime;

	private final Boolean acknowledgeAsynchronously;

	private final Scheduler acknowledgeScheduler;

	private final Duration negativeAckRedeliveryDelay;

	private final RedeliveryBackoff negativeAckRedeliveryBackoff;

	private final RedeliveryBackoff ackTimeoutRedeliveryBackoff;

	private final DeadLetterPolicy deadLetterPolicy;

	private final Boolean retryLetterTopicEnable;

	private final Integer receiverQueueSize;

	private final Integer maxTotalReceiverQueueSizeAcrossPartitions;

	private final Boolean autoUpdatePartitions;

	private final Duration autoUpdatePartitionsInterval;

	private final CryptoKeyReader cryptoKeyReader;

	private final ConsumerCryptoFailureAction cryptoFailureAction;

	private final Integer maxPendingChunkedMessage;

	private final Boolean autoAckOldestChunkedMessageOnQueueFull;

	private final Duration expireTimeOfIncompleteChunkedMessage;

	/**
	 * Constructs a ImmutableReactiveMessageConsumerSpec from another
	 * {@link ReactiveMessageConsumerSpec}.
	 * @param consumerSpec the spec to construct from
	 */
	public ImmutableReactiveMessageConsumerSpec(ReactiveMessageConsumerSpec consumerSpec) {
		this.topicNames = (consumerSpec.getTopicNames() != null && !consumerSpec.getTopicNames().isEmpty())
				? Collections.unmodifiableList(new ArrayList<>(consumerSpec.getTopicNames())) : null;

		this.topicsPattern = consumerSpec.getTopicsPattern();

		this.topicsPatternSubscriptionMode = consumerSpec.getTopicsPatternSubscriptionMode();

		this.topicsPatternAutoDiscoveryPeriod = consumerSpec.getTopicsPatternAutoDiscoveryPeriod();

		this.subscriptionName = consumerSpec.getSubscriptionName();

		this.subscriptionMode = consumerSpec.getSubscriptionMode();

		this.subscriptionType = consumerSpec.getSubscriptionType();

		this.subscriptionInitialPosition = consumerSpec.getSubscriptionInitialPosition();

		this.keySharedPolicy = consumerSpec.getKeySharedPolicy();

		this.replicateSubscriptionState = consumerSpec.getReplicateSubscriptionState();

		this.subscriptionProperties = (consumerSpec.getSubscriptionProperties() != null
				&& !consumerSpec.getSubscriptionProperties().isEmpty())
						? Collections.unmodifiableMap(new LinkedHashMap<>(consumerSpec.getSubscriptionProperties()))
						: null;

		this.consumerName = consumerSpec.getConsumerName();

		this.properties = (consumerSpec.getProperties() != null && !consumerSpec.getProperties().isEmpty())
				? Collections.unmodifiableMap(new LinkedHashMap<>(consumerSpec.getProperties())) : null;

		this.priorityLevel = consumerSpec.getPriorityLevel();

		this.readCompacted = consumerSpec.getReadCompacted();

		this.batchIndexAckEnabled = consumerSpec.getBatchIndexAckEnabled();

		this.ackTimeout = consumerSpec.getAckTimeout();

		this.ackTimeoutTickTime = consumerSpec.getAckTimeoutTickTime();

		this.acknowledgementsGroupTime = consumerSpec.getAcknowledgementsGroupTime();

		this.acknowledgeAsynchronously = consumerSpec.getAcknowledgeAsynchronously();
		this.acknowledgeScheduler = consumerSpec.getAcknowledgeScheduler();
		this.negativeAckRedeliveryDelay = consumerSpec.getNegativeAckRedeliveryDelay();
		this.negativeAckRedeliveryBackoff = consumerSpec.getNegativeAckRedeliveryBackoff();
		this.ackTimeoutRedeliveryBackoff = consumerSpec.getAckTimeoutRedeliveryBackoff();

		this.deadLetterPolicy = consumerSpec.getDeadLetterPolicy();

		this.retryLetterTopicEnable = consumerSpec.getRetryLetterTopicEnable();

		this.receiverQueueSize = consumerSpec.getReceiverQueueSize();

		this.maxTotalReceiverQueueSizeAcrossPartitions = consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions();

		this.autoUpdatePartitions = consumerSpec.getAutoUpdatePartitions();

		this.autoUpdatePartitionsInterval = consumerSpec.getAutoUpdatePartitionsInterval();

		this.cryptoKeyReader = consumerSpec.getCryptoKeyReader();

		this.cryptoFailureAction = consumerSpec.getCryptoFailureAction();

		this.maxPendingChunkedMessage = consumerSpec.getMaxPendingChunkedMessage();

		this.autoAckOldestChunkedMessageOnQueueFull = consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull();

		this.expireTimeOfIncompleteChunkedMessage = consumerSpec.getExpireTimeOfIncompleteChunkedMessage();
	}

	/**
	 * Constructs an instance with the specified parameters.
	 * @param topicNames the topics to subscribe to
	 * @param topicsPattern the topics pattern of the topics to subscribe to
	 * @param topicsPatternSubscriptionMode the type of topics to subscribe to when using
	 * a topic pattern - Persistent, Non-Persistent, or both
	 * @param topicsPatternAutoDiscoveryPeriod the topics auto discovery period when using
	 * a topic pattern
	 * @param subscriptionName the subscription name
	 * @param subscriptionMode the subscription mode
	 * @param subscriptionType the subscription type
	 * @param subscriptionInitialPosition the subscription initial position
	 * @param keySharedPolicy the policy used for {@link SubscriptionType#Key_Shared}
	 * subscriptions
	 * @param replicateSubscriptionState whether the subscription shall be replicated
	 * @param subscriptionProperties the properties for the subscription
	 * @param consumerName the consumer name
	 * @param properties the consumer properties
	 * @param priorityLevel the priority level for the consumer to which a broker gives
	 * more priority while dispatching messages
	 * @param readCompacted whether to read messages from the compacted topic rather than
	 * reading the full message backlog of the topic
	 * @param batchIndexAckEnabled whether batch index acknowledgement is enabled
	 * @param ackTimeout the timeout duration for unacknowledged messages
	 * @param ackTimeoutTickTime the tick time of the ack-timeout redelivery
	 * @param acknowledgementsGroupTime the duration used to group acknowledgements
	 * @param acknowledgeAsynchronously whether to ignore the acknowledge operation
	 * completion and make it asynchronous from the message consuming processing to
	 * improve performance by allowing the acknowledges and message processing to
	 * interleave
	 * @param acknowledgeScheduler the scheduler to use to handle acknowledgements
	 * @param negativeAckRedeliveryDelay the delay to wait before re-delivering messages
	 * that have failed to be processed
	 * @param deadLetterPolicy the dead letter policy for the consumer
	 * @param retryLetterTopicEnable whether the retries are enabled
	 * @param receiverQueueSize the size of the consumer receive queue
	 * @param maxTotalReceiverQueueSizeAcrossPartitions the maximum total receiver queue
	 * size across partitions
	 * @param autoUpdatePartitions whether the consumer shall subscribe automatically to
	 * new partitions of partitioned topics
	 * @param autoUpdatePartitionsInterval the interval of updating partitions when
	 * autoUpdatePartitions is enabled
	 * @param cryptoKeyReader the key reader to be used to decrypt the message payloads
	 * @param cryptoFailureAction the action the consumer will take in case of decryption
	 * failures
	 * @param maxPendingChunkedMessage the maximum number of messages in the queue holding
	 * pending chunked messages
	 * @param autoAckOldestChunkedMessageOnQueueFull whether to automatically acknowledge
	 * pending chunked messages when maxPendingChunkedMessage is reached
	 * @param expireTimeOfIncompleteChunkedMessage the time interval to expire incomplete
	 * chunks if a consumer fails to receive all the chunks
	 * @deprecated since 0.7.0 for removal in 0.9.0 in favor of
	 * {@link #ImmutableReactiveMessageConsumerSpec(List, Pattern, RegexSubscriptionMode, Duration, String, SubscriptionMode, SubscriptionType, SubscriptionInitialPosition, KeySharedPolicy, Boolean, Map, String, Map, Integer, Boolean, Boolean, Duration, Duration, Duration, Boolean, Scheduler, Duration, RedeliveryBackoff, RedeliveryBackoff, DeadLetterPolicy, Boolean, Integer, Integer, Boolean, Duration, CryptoKeyReader, ConsumerCryptoFailureAction, Integer, Boolean, Duration)}
	 */
	@Deprecated
	public ImmutableReactiveMessageConsumerSpec(List<String> topicNames, Pattern topicsPattern,
			RegexSubscriptionMode topicsPatternSubscriptionMode, Duration topicsPatternAutoDiscoveryPeriod,
			String subscriptionName, SubscriptionMode subscriptionMode, SubscriptionType subscriptionType,
			SubscriptionInitialPosition subscriptionInitialPosition, KeySharedPolicy keySharedPolicy,
			Boolean replicateSubscriptionState, Map<String, String> subscriptionProperties, String consumerName,
			Map<String, String> properties, Integer priorityLevel, Boolean readCompacted, Boolean batchIndexAckEnabled,
			Duration ackTimeout, Duration ackTimeoutTickTime, Duration acknowledgementsGroupTime,
			Boolean acknowledgeAsynchronously, Scheduler acknowledgeScheduler, Duration negativeAckRedeliveryDelay,
			DeadLetterPolicy deadLetterPolicy, Boolean retryLetterTopicEnable, Integer receiverQueueSize,
			Integer maxTotalReceiverQueueSizeAcrossPartitions, Boolean autoUpdatePartitions,
			Duration autoUpdatePartitionsInterval, CryptoKeyReader cryptoKeyReader,
			ConsumerCryptoFailureAction cryptoFailureAction, Integer maxPendingChunkedMessage,
			Boolean autoAckOldestChunkedMessageOnQueueFull, Duration expireTimeOfIncompleteChunkedMessage) {
		this.topicNames = topicNames;
		this.topicsPattern = topicsPattern;
		this.topicsPatternSubscriptionMode = topicsPatternSubscriptionMode;
		this.topicsPatternAutoDiscoveryPeriod = topicsPatternAutoDiscoveryPeriod;
		this.subscriptionName = subscriptionName;
		this.subscriptionMode = subscriptionMode;
		this.subscriptionType = subscriptionType;
		this.subscriptionInitialPosition = subscriptionInitialPosition;
		this.keySharedPolicy = keySharedPolicy;
		this.replicateSubscriptionState = replicateSubscriptionState;
		this.subscriptionProperties = subscriptionProperties;
		this.consumerName = consumerName;
		this.properties = properties;
		this.priorityLevel = priorityLevel;
		this.readCompacted = readCompacted;
		this.batchIndexAckEnabled = batchIndexAckEnabled;
		this.ackTimeout = ackTimeout;
		this.ackTimeoutTickTime = ackTimeoutTickTime;
		this.acknowledgementsGroupTime = acknowledgementsGroupTime;
		this.acknowledgeAsynchronously = acknowledgeAsynchronously;
		this.acknowledgeScheduler = acknowledgeScheduler;
		this.negativeAckRedeliveryDelay = negativeAckRedeliveryDelay;
		this.negativeAckRedeliveryBackoff = null;
		this.ackTimeoutRedeliveryBackoff = null;
		this.deadLetterPolicy = deadLetterPolicy;
		this.retryLetterTopicEnable = retryLetterTopicEnable;
		this.receiverQueueSize = receiverQueueSize;
		this.maxTotalReceiverQueueSizeAcrossPartitions = maxTotalReceiverQueueSizeAcrossPartitions;
		this.autoUpdatePartitions = autoUpdatePartitions;
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
		this.cryptoKeyReader = cryptoKeyReader;
		this.cryptoFailureAction = cryptoFailureAction;
		this.maxPendingChunkedMessage = maxPendingChunkedMessage;
		this.autoAckOldestChunkedMessageOnQueueFull = autoAckOldestChunkedMessageOnQueueFull;
		this.expireTimeOfIncompleteChunkedMessage = expireTimeOfIncompleteChunkedMessage;
	}

	/**
	 * Constructs an instance with the specified parameters. Intended for use by the
	 * Jackson mixin for the Jackson serde.
	 * @param topicNames the topics to subscribe to
	 * @param topicsPattern the topics pattern of the topics to subscribe to
	 * @param topicsPatternSubscriptionMode the type of topics to subscribe to when using
	 * a topic pattern - Persistent, Non-Persistent, or both
	 * @param topicsPatternAutoDiscoveryPeriod the topics auto discovery period when using
	 * a topic pattern
	 * @param subscriptionName the subscription name
	 * @param subscriptionMode the subscription mode
	 * @param subscriptionType the subscription type
	 * @param subscriptionInitialPosition the subscription initial position
	 * @param keySharedPolicy the policy used for {@link SubscriptionType#Key_Shared}
	 * subscriptions
	 * @param replicateSubscriptionState whether the subscription shall be replicated
	 * @param subscriptionProperties the properties for the subscription
	 * @param consumerName the consumer name
	 * @param properties the consumer properties
	 * @param priorityLevel the priority level for the consumer to which a broker gives
	 * more priority while dispatching messages
	 * @param readCompacted whether to read messages from the compacted topic rather than
	 * reading the full message backlog of the topic
	 * @param batchIndexAckEnabled whether batch index acknowledgement is enabled
	 * @param ackTimeout the timeout duration for unacknowledged messages
	 * @param ackTimeoutTickTime the tick time of the ack-timeout redelivery
	 * @param acknowledgementsGroupTime the duration used to group acknowledgements
	 * @param acknowledgeAsynchronously whether to ignore the acknowledge operation
	 * completion and make it asynchronous from the message consuming processing to
	 * improve performance by allowing the acknowledges and message processing to
	 * interleave
	 * @param acknowledgeScheduler the scheduler to use to handle acknowledgements
	 * @param negativeAckRedeliveryDelay the delay to wait before re-delivering messages
	 * that have failed to be processed
	 * @param deadLetterPolicy the dead letter policy for the consumer
	 * @param negativeAckRedeliveryBackoff the redelivery backoff policy for messages that
	 * are negatively acknowledged
	 * @param ackTimeoutRedeliveryBackoff the redelivery backoff policy for messages that
	 * are redelivered due to acknowledgement timeout
	 * @param retryLetterTopicEnable whether the retries are enabled
	 * @param receiverQueueSize the size of the consumer receive queue
	 * @param maxTotalReceiverQueueSizeAcrossPartitions the maximum total receiver queue
	 * size across partitions
	 * @param autoUpdatePartitions whether the consumer shall subscribe automatically to
	 * new partitions of partitioned topics
	 * @param autoUpdatePartitionsInterval the interval of updating partitions when
	 * autoUpdatePartitions is enabled
	 * @param cryptoKeyReader the key reader to be used to decrypt the message payloads
	 * @param cryptoFailureAction the action the consumer will take in case of decryption
	 * failures
	 * @param maxPendingChunkedMessage the maximum number of messages in the queue holding
	 * pending chunked messages
	 * @param autoAckOldestChunkedMessageOnQueueFull whether to automatically acknowledge
	 * pending chunked messages when maxPendingChunkedMessage is reached
	 * @param expireTimeOfIncompleteChunkedMessage the time interval to expire incomplete
	 * chunks if a consumer fails to receive all the chunks
	 */
	@SuppressWarnings("unused")
	ImmutableReactiveMessageConsumerSpec(List<String> topicNames, Pattern topicsPattern,
			RegexSubscriptionMode topicsPatternSubscriptionMode, Duration topicsPatternAutoDiscoveryPeriod,
			String subscriptionName, SubscriptionMode subscriptionMode, SubscriptionType subscriptionType,
			SubscriptionInitialPosition subscriptionInitialPosition, KeySharedPolicy keySharedPolicy,
			Boolean replicateSubscriptionState, Map<String, String> subscriptionProperties, String consumerName,
			Map<String, String> properties, Integer priorityLevel, Boolean readCompacted, Boolean batchIndexAckEnabled,
			Duration ackTimeout, Duration ackTimeoutTickTime, Duration acknowledgementsGroupTime,
			Boolean acknowledgeAsynchronously, Scheduler acknowledgeScheduler, Duration negativeAckRedeliveryDelay,
			RedeliveryBackoff negativeAckRedeliveryBackoff, RedeliveryBackoff ackTimeoutRedeliveryBackoff,
			DeadLetterPolicy deadLetterPolicy, Boolean retryLetterTopicEnable, Integer receiverQueueSize,
			Integer maxTotalReceiverQueueSizeAcrossPartitions, Boolean autoUpdatePartitions,
			Duration autoUpdatePartitionsInterval, CryptoKeyReader cryptoKeyReader,
			ConsumerCryptoFailureAction cryptoFailureAction, Integer maxPendingChunkedMessage,
			Boolean autoAckOldestChunkedMessageOnQueueFull, Duration expireTimeOfIncompleteChunkedMessage) {
		this.topicNames = topicNames;
		this.topicsPattern = topicsPattern;
		this.topicsPatternSubscriptionMode = topicsPatternSubscriptionMode;
		this.topicsPatternAutoDiscoveryPeriod = topicsPatternAutoDiscoveryPeriod;
		this.subscriptionName = subscriptionName;
		this.subscriptionMode = subscriptionMode;
		this.subscriptionType = subscriptionType;
		this.subscriptionInitialPosition = subscriptionInitialPosition;
		this.keySharedPolicy = keySharedPolicy;
		this.replicateSubscriptionState = replicateSubscriptionState;
		this.subscriptionProperties = subscriptionProperties;
		this.consumerName = consumerName;
		this.properties = properties;
		this.priorityLevel = priorityLevel;
		this.readCompacted = readCompacted;
		this.batchIndexAckEnabled = batchIndexAckEnabled;
		this.ackTimeout = ackTimeout;
		this.ackTimeoutTickTime = ackTimeoutTickTime;
		this.acknowledgementsGroupTime = acknowledgementsGroupTime;
		this.acknowledgeAsynchronously = acknowledgeAsynchronously;
		this.acknowledgeScheduler = acknowledgeScheduler;
		this.negativeAckRedeliveryDelay = negativeAckRedeliveryDelay;
		this.negativeAckRedeliveryBackoff = negativeAckRedeliveryBackoff;
		this.ackTimeoutRedeliveryBackoff = ackTimeoutRedeliveryBackoff;
		this.deadLetterPolicy = deadLetterPolicy;
		this.retryLetterTopicEnable = retryLetterTopicEnable;
		this.receiverQueueSize = receiverQueueSize;
		this.maxTotalReceiverQueueSizeAcrossPartitions = maxTotalReceiverQueueSizeAcrossPartitions;
		this.autoUpdatePartitions = autoUpdatePartitions;
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
		this.cryptoKeyReader = cryptoKeyReader;
		this.cryptoFailureAction = cryptoFailureAction;
		this.maxPendingChunkedMessage = maxPendingChunkedMessage;
		this.autoAckOldestChunkedMessageOnQueueFull = autoAckOldestChunkedMessageOnQueueFull;
		this.expireTimeOfIncompleteChunkedMessage = expireTimeOfIncompleteChunkedMessage;
	}

	@Override
	public List<String> getTopicNames() {
		return this.topicNames;
	}

	@Override
	public Pattern getTopicsPattern() {
		return this.topicsPattern;
	}

	@Override
	public RegexSubscriptionMode getTopicsPatternSubscriptionMode() {
		return this.topicsPatternSubscriptionMode;
	}

	@Override
	public Duration getTopicsPatternAutoDiscoveryPeriod() {
		return this.topicsPatternAutoDiscoveryPeriod;
	}

	@Override
	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	@Override
	public SubscriptionMode getSubscriptionMode() {
		return this.subscriptionMode;
	}

	@Override
	public SubscriptionType getSubscriptionType() {
		return this.subscriptionType;
	}

	@Override
	public SubscriptionInitialPosition getSubscriptionInitialPosition() {
		return this.subscriptionInitialPosition;
	}

	@Override
	public KeySharedPolicy getKeySharedPolicy() {
		return this.keySharedPolicy;
	}

	@Override
	public Boolean getReplicateSubscriptionState() {
		return this.replicateSubscriptionState;
	}

	@Override
	public Map<String, String> getSubscriptionProperties() {
		return this.subscriptionProperties;
	}

	@Override
	public String getConsumerName() {
		return this.consumerName;
	}

	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

	@Override
	public Integer getPriorityLevel() {
		return this.priorityLevel;
	}

	@Override
	public Boolean getReadCompacted() {
		return this.readCompacted;
	}

	@Override
	public Boolean getBatchIndexAckEnabled() {
		return this.batchIndexAckEnabled;
	}

	@Override
	public Duration getAckTimeout() {
		return this.ackTimeout;
	}

	@Override
	public Duration getAckTimeoutTickTime() {
		return this.ackTimeoutTickTime;
	}

	@Override
	public Duration getAcknowledgementsGroupTime() {
		return this.acknowledgementsGroupTime;
	}

	@Override
	public Boolean getAcknowledgeAsynchronously() {
		return this.acknowledgeAsynchronously;
	}

	@Override
	public Scheduler getAcknowledgeScheduler() {
		return this.acknowledgeScheduler;
	}

	@Override
	public Duration getNegativeAckRedeliveryDelay() {
		return this.negativeAckRedeliveryDelay;
	}

	@Override
	public RedeliveryBackoff getNegativeAckRedeliveryBackoff() {
		return this.negativeAckRedeliveryBackoff;
	}

	@Override
	public RedeliveryBackoff getAckTimeoutRedeliveryBackoff() {
		return this.ackTimeoutRedeliveryBackoff;
	}

	@Override
	public DeadLetterPolicy getDeadLetterPolicy() {
		return this.deadLetterPolicy;
	}

	@Override
	public Boolean getRetryLetterTopicEnable() {
		return this.retryLetterTopicEnable;
	}

	@Override
	public Integer getReceiverQueueSize() {
		return this.receiverQueueSize;
	}

	@Override
	public Integer getMaxTotalReceiverQueueSizeAcrossPartitions() {
		return this.maxTotalReceiverQueueSizeAcrossPartitions;
	}

	@Override
	public Boolean getAutoUpdatePartitions() {
		return this.autoUpdatePartitions;
	}

	@Override
	public Duration getAutoUpdatePartitionsInterval() {
		return this.autoUpdatePartitionsInterval;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	@Override
	public ConsumerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	@Override
	public Integer getMaxPendingChunkedMessage() {
		return this.maxPendingChunkedMessage;
	}

	@Override
	public Boolean getAutoAckOldestChunkedMessageOnQueueFull() {
		return this.autoAckOldestChunkedMessageOnQueueFull;
	}

	@Override
	public Duration getExpireTimeOfIncompleteChunkedMessage() {
		return this.expireTimeOfIncompleteChunkedMessage;
	}

}
