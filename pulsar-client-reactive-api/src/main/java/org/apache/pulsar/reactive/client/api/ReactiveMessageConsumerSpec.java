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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

/**
 * Spec for a {@link ReactiveMessageConsumer}.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessageConsumerSpec {

	/**
	 * Gets the topics to subscribe to.
	 * @return the topic names
	 * @see ConsumerBuilder#topics
	 */
	List<String> getTopicNames();

	/**
	 * Gets the topics pattern of the topics to subscribe to.
	 * @return the topics pattern
	 * @see ConsumerBuilder#topicsPattern(Pattern)
	 */
	Pattern getTopicsPattern();

	/**
	 * Gets the type of topics to subscribe to when using a topic pattern - Persistent,
	 * Non-Persistent, or both.
	 * @return the pattern subscription mode
	 * @see ConsumerBuilder#subscriptionTopicsMode
	 */
	RegexSubscriptionMode getTopicsPatternSubscriptionMode();

	/**
	 * Gets the topics auto discovery period when using a topic pattern.
	 * @return the topics discovery period
	 * @see ConsumerBuilder#patternAutoDiscoveryPeriod(int)
	 */
	Duration getTopicsPatternAutoDiscoveryPeriod();

	/**
	 * Gets the subscription name.
	 * @return the name of the subscription
	 * @see ConsumerBuilder#subscriptionName
	 */
	String getSubscriptionName();

	/**
	 * Gets the subscription mode.
	 * @return the subscription mode
	 * @see ConsumerBuilder#subscriptionMode
	 */
	SubscriptionMode getSubscriptionMode();

	/**
	 * Gets the subscription type.
	 * @return the subscription type
	 * @see ConsumerBuilder#subscriptionType
	 */
	SubscriptionType getSubscriptionType();

	/**
	 * Gets the subscription initial position.
	 * @return the position where to initialize a newly created subscription
	 * @see ConsumerBuilder#subscriptionInitialPosition
	 */
	SubscriptionInitialPosition getSubscriptionInitialPosition();

	/**
	 * Gets the policy used for {@link SubscriptionType#Key_Shared} subscriptions.
	 * @return the key-shared policy
	 * @see ConsumerBuilder#keySharedPolicy
	 */
	KeySharedPolicy getKeySharedPolicy();

	/**
	 * Gets whether the subscription shall be replicated.
	 * @return true if the subscription is replicated
	 * @see ConsumerBuilder#replicateSubscriptionState
	 */
	Boolean getReplicateSubscriptionState();

	/**
	 * Gets the properties for the subscription.
	 * @return the subscription properties
	 * @see ConsumerBuilder#subscriptionProperties
	 */
	Map<String, String> getSubscriptionProperties();

	/**
	 * Gets the consumer name.
	 * @return the consumer name
	 * @see ConsumerBuilder#consumerName
	 */
	String getConsumerName();

	/**
	 * Gets the consumer properties.
	 * @return the consumer properties
	 * @see ConsumerBuilder#properties
	 */
	Map<String, String> getProperties();

	/**
	 * Gets the priority level for the consumer to which a broker gives more priority
	 * while dispatching messages.
	 * @return the priority level of the consumer
	 * @see ConsumerBuilder#priorityLevel
	 */
	Integer getPriorityLevel();

	/**
	 * Gets whether to read messages from the compacted topic rather than reading the full
	 * message backlog of the topic.
	 * @return true if messages are read from the compacted topic
	 * @see ConsumerBuilder#readCompacted
	 */
	Boolean getReadCompacted();

	/**
	 * Gets whether batch index acknowledgement is enabled.
	 * @return true if batch index acknowledgement is enabled
	 * @see ConsumerBuilder#enableBatchIndexAcknowledgment
	 */
	Boolean getBatchIndexAckEnabled();

	/**
	 * Gets the timeout duration for unacknowledged messages.
	 * @return the timeout duration for unacknowledged messages
	 * @see ConsumerBuilder#ackTimeout
	 */
	Duration getAckTimeout();

	/**
	 * Gets the tick time of the ack-timeout redelivery.
	 * @return the tick time of the ack-timeout redelivery
	 * @see ConsumerBuilder#ackTimeoutTickTime
	 */
	Duration getAckTimeoutTickTime();

	/**
	 * Gets the duration used to group acknowledgements.
	 * @return the duration used to group acknowledgements
	 * @see ConsumerBuilder#acknowledgmentGroupTime
	 */
	Duration getAcknowledgementsGroupTime();

	/**
	 * Gets whether to ignore the acknowledge operation completion and make it
	 * asynchronous from the message consuming processing to improve performance by
	 * allowing the acknowledges and message processing to interleave.
	 * @return true if the acknowledge operation completion is ignored
	 */
	Boolean getAcknowledgeAsynchronously();

	/**
	 * Gets the scheduler to use to handle acknowledgements.
	 * @return the scheduler to use to handle acknowledgements
	 */
	Scheduler getAcknowledgeScheduler();

	/**
	 * Gets the delay to wait before re-delivering messages that have failed to be
	 * processed.
	 * @return the redelivery delay for failed messages
	 * @see ConsumerBuilder#negativeAckRedeliveryDelay
	 */
	Duration getNegativeAckRedeliveryDelay();

	/**
	 * Gets the dead letter policy for the consumer.
	 * @return the dead letter policy
	 * @see ConsumerBuilder#deadLetterPolicy
	 */
	DeadLetterPolicy getDeadLetterPolicy();

	/**
	 * Gets whether the retries are enabled.
	 * @return true if retries are enabled
	 * @see ConsumerBuilder#enableRetry
	 */
	Boolean getRetryLetterTopicEnable();

	/**
	 * Gets the size of the consumer receive queue.
	 * @return the size of the consumer receive queue
	 * @see ConsumerBuilder#receiverQueueSize
	 */
	Integer getReceiverQueueSize();

	/**
	 * Gets the max total receiver queue size across partitons.
	 * @return the max total receiver queue size across partitons
	 * @see ConsumerBuilder#maxTotalReceiverQueueSizeAcrossPartitions
	 */
	Integer getMaxTotalReceiverQueueSizeAcrossPartitions();

	/**
	 * Gets whether the consumer shall subscribe automatically to new partitions of
	 * partitioned topics.
	 * @return true if the consumer subscribes automatically to new partitions
	 * @see ConsumerBuilder#autoUpdatePartitions
	 */
	Boolean getAutoUpdatePartitions();

	/**
	 * Gets the interval of updating partitions when autoUpdatePartitions is enabled.
	 * @return true if the consumer subscribes automatically to new partitions
	 * @see ConsumerBuilder#autoUpdatePartitionsInterval
	 * @see ConsumerBuilder#autoUpdatePartitions
	 */
	Duration getAutoUpdatePartitionsInterval();

	/**
	 * Gets the key reader to be used to decrypt the message payloads.
	 * @return the key reader to be used to decrypt the message payloads
	 * @see ConsumerBuilder#cryptoKeyReader
	 */
	CryptoKeyReader getCryptoKeyReader();

	/**
	 * Gets the action the consumer will take in case of decryption failures.
	 * @return the action the consumer will take in case of decryption failures
	 * @see ConsumerBuilder#cryptoFailureAction
	 */
	ConsumerCryptoFailureAction getCryptoFailureAction();

	/**
	 * Gets the maximum number of messages in the queue holding pending chunked messages.
	 * @return the maximum number of messages in the queue holding pending chunked
	 * @see ConsumerBuilder#maxPendingChunkedMessage messages.
	 */
	Integer getMaxPendingChunkedMessage();

	/**
	 * Gets whether to automatically acknowledge pending chunked messages when
	 * maxPendingChunkedMessage is reached.
	 * @return true to acknowledge the messages, false to have them redelivered.
	 * @see ConsumerBuilder#autoAckOldestChunkedMessageOnQueueFull
	 * @see ConsumerBuilder#maxPendingChunkedMessage
	 */
	Boolean getAutoAckOldestChunkedMessageOnQueueFull();

	/**
	 * Gets the time interval to expire incomplete chunks if a consumer fails to receive
	 * all the chunks.
	 * @return the time interval to expire incomplete chunks.
	 * @see ConsumerBuilder#expireTimeOfIncompleteChunkedMessage
	 */
	Duration getExpireTimeOfIncompleteChunkedMessage();

}
