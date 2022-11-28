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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

/**
 * Builder interface for {@link ReactiveMessageConsumer}.
 *
 * @param <T> the message payload type
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessageConsumerBuilder<T> {

	/**
	 * Apply a consumer spec to configure the consumer.
	 * @param consumerSpec the consumer spec to apply
	 * @return the consumer builder instance
	 */
	default ReactiveMessageConsumerBuilder<T> applySpec(ReactiveMessageConsumerSpec consumerSpec) {
		getMutableSpec().applySpec(consumerSpec);
		return this;
	}

	/**
	 * Converts this builder to an immutable reactive consumer spec.
	 * @return the reactive consumer spec.
	 */
	default ReactiveMessageConsumerSpec toImmutableSpec() {
		return new ImmutableReactiveMessageConsumerSpec(getMutableSpec());
	}

	/**
	 * Converts this builder to a mutable reactive consumer spec.
	 * @return the reactive consumer spec.
	 */
	MutableReactiveMessageConsumerSpec getMutableSpec();

	/**
	 * Creates and returns a copy of this reactive consumer builder.
	 * @return the cloned reactive reader builder
	 */
	ReactiveMessageConsumerBuilder<T> clone();

	/**
	 * Adds a topic this consumer will subscribe on.
	 * @param topicName a topic that the consumer will subscribe on
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#topic(String...)
	 */
	default ReactiveMessageConsumerBuilder<T> topic(String topicName) {
		getMutableSpec().getTopicNames().add(topicName);
		return this;
	}

	/**
	 * Adds topics this consumer will subscribe on.
	 * @param topicNames a set of topic that the consumer will subscribe on
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#topic(String...)
	 */
	default ReactiveMessageConsumerBuilder<T> topic(String... topicNames) {
		for (String topicName : topicNames) {
			getMutableSpec().getTopicNames().add(topicName);
		}
		return this;
	}

	/**
	 * Sets the topics this consumer will subscribe on.
	 * @param topicNames a set of topic that the consumer will subscribe on
	 * @return the consumer builder instance
	 */
	default ReactiveMessageConsumerBuilder<T> topics(List<String> topicNames) {
		getMutableSpec().setTopicNames(topicNames);
		return this;
	}

	/**
	 * Sets a pattern for topics that this consumer will subscribe on.
	 *
	 * <p>
	 * The pattern will be applied to subscribe to all the topics, within a single
	 * namespace, that will match the pattern.
	 *
	 * <p>
	 * The consumer will automatically subscribe to topics created after itself.
	 * @param topicsPattern a regular expression to select a list of topics to subscribe
	 * to
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#topicsPattern(Pattern)
	 */
	default ReactiveMessageConsumerBuilder<T> topicsPattern(Pattern topicsPattern) {
		getMutableSpec().setTopicsPattern(topicsPattern);
		return this;
	}

	/**
	 * Sets to which topics this consumer should be subscribed to - Persistent,
	 * Mon-Persistent, or both. Only used with pattern subscriptions.
	 * @param topicsPatternSubscriptionMode pattern subscription mode
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionTopicsMode(RegexSubscriptionMode)
	 */
	default ReactiveMessageConsumerBuilder<T> topicsPatternSubscriptionMode(
			RegexSubscriptionMode topicsPatternSubscriptionMode) {
		getMutableSpec().setTopicsPatternSubscriptionMode(topicsPatternSubscriptionMode);
		return this;
	}

	/**
	 * Sets the topics auto discovery period when using a pattern for topics consumer.
	 * @param topicsPatternAutoDiscoveryPeriod duration between checks for new topics
	 * matching pattern set with {@link #topicsPattern(Pattern)}
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#patternAutoDiscoveryPeriod(int, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> topicsPatternAutoDiscoveryPeriod(
			Duration topicsPatternAutoDiscoveryPeriod) {
		getMutableSpec().setTopicsPatternAutoDiscoveryPeriod(topicsPatternAutoDiscoveryPeriod);
		return this;
	}

	/**
	 * Sets the subscription name for this consumer.
	 * <p>
	 * This argument is required when constructing the consumer.
	 * @param subscriptionName the name of the subscription that this consumer should
	 * attach to
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionName(String)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionName(String subscriptionName) {
		getMutableSpec().setSubscriptionName(subscriptionName);
		return this;
	}

	/**
	 * Sets the subscription mode to be used when subscribing to the topic.
	 *
	 * <p>
	 * Options are:
	 * <ul>
	 * <li>{@link SubscriptionMode#Durable} (Default)</li>
	 * <li>{@link SubscriptionMode#NonDurable}</li>
	 * </ul>
	 * @param subscriptionMode the subscription mode value
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionMode(SubscriptionMode)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionMode(SubscriptionMode subscriptionMode) {
		getMutableSpec().setSubscriptionMode(subscriptionMode);
		return this;
	}

	/**
	 * Sets the subscription type to be used when subscribing to the topic.
	 *
	 * <p>
	 * Options are:
	 * <ul>
	 * <li>{@link SubscriptionType#Exclusive} (Default)</li>
	 * <li>{@link SubscriptionType#Failover}</li>
	 * <li>{@link SubscriptionType#Shared}</li>
	 * </ul>
	 * @param subscriptionType the subscription type value
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionType(SubscriptionType)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionType(SubscriptionType subscriptionType) {
		getMutableSpec().setSubscriptionType(subscriptionType);
		return this;
	}

	/**
	 * Sets the initial position of the subscription for the consumer.
	 * @param subscriptionInitialPosition the position where to initialize a newly created
	 * subscription
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionInitialPosition(SubscriptionInitialPosition)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionInitialPosition(
			SubscriptionInitialPosition subscriptionInitialPosition) {
		getMutableSpec().setSubscriptionInitialPosition(subscriptionInitialPosition);
		return this;
	}

	/**
	 * Sets the KeyShared subscription policy for the consumer.
	 *
	 * <p>
	 * By default, KeyShared subscription uses auto split hash range to maintain
	 * consumers. If you want to set a different KeyShared policy, you can set it like
	 * this:
	 *
	 * <pre>
	 * client.messageConsumer(Schema.BYTES)
	 *          .keySharedPolicy(KeySharedPolicy.stickyHashRange().ranges(Range.of(0, 10)))
	 *          .build();
	 * </pre> For details about sticky hash range policy, please see
	 * {@link KeySharedPolicy.KeySharedPolicySticky}.
	 *
	 * <p>
	 * Or <pre>
	 * client.messageConsumer(Schema.BYTES)
	 *          .keySharedPolicy(KeySharedPolicy.autoSplitHashRange())
	 *          .build();
	 * </pre> For details about auto split hash range policy, please see
	 * {@link KeySharedPolicy.KeySharedPolicyAutoSplit}.
	 * @param keySharedPolicy the KeyShared policy to set
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#keySharedPolicy(KeySharedPolicy)
	 */
	default ReactiveMessageConsumerBuilder<T> keySharedPolicy(KeySharedPolicy keySharedPolicy) {
		getMutableSpec().setKeySharedPolicy(keySharedPolicy);
		return this;
	}

	/**
	 * Sets whether the subscription shall be replicated.
	 * @param replicateSubscriptionState whether the subscription shall be replicated
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#replicateSubscriptionState(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> replicateSubscriptionState(boolean replicateSubscriptionState) {
		getMutableSpec().setReplicateSubscriptionState(replicateSubscriptionState);
		return this;
	}

	/**
	 * Sets the subscription properties for this subscription. Properties are immutable,
	 * and consumers under the same subscription will fail to create a subscription if
	 * they use different properties.
	 * @param subscriptionProperties the subscription properties to set
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionProperties(Map)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionProperties(Map<String, String> subscriptionProperties) {
		getMutableSpec().setSubscriptionProperties(subscriptionProperties);
		return this;
	}

	/**
	 * Adds a subscription property for this subscription. Properties are immutable, and
	 * consumers under the same subscription will fail to create a subscription if they
	 * use different properties.
	 * @param key the key of the property to add
	 * @param value the value of the property to add
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#subscriptionProperties(Map)
	 */
	default ReactiveMessageConsumerBuilder<T> subscriptionProperty(String key, String value) {
		if (getMutableSpec().getSubscriptionProperties() == null) {
			getMutableSpec().setSubscriptionProperties(new LinkedHashMap<>());
		}
		getMutableSpec().getSubscriptionProperties().put(key, value);
		return this;
	}

	/**
	 * Sets the consumer name.
	 *
	 * <p>
	 * Consumer name is informative and it can be used to indentify a particular consumer
	 * instance from the topic stats.
	 * @param consumerName the consumer name
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#consumerName(String)
	 */
	default ReactiveMessageConsumerBuilder<T> consumerName(String consumerName) {
		getMutableSpec().setConsumerName(consumerName);
		return this;
	}

	/**
	 * Sets the properties for the consumer.
	 *
	 * <p>
	 * Properties are application defined metadata that can be attached to the consumer.
	 * When getting the topic stats, this metadata will be associated to the consumer
	 * stats for easier identification.
	 * @param properties the properties to set
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#properties(Map)
	 */
	default ReactiveMessageConsumerBuilder<T> properties(Map<String, String> properties) {
		getMutableSpec().setProperties(properties);
		return this;
	}

	/**
	 * Add a property to the consumer.
	 *
	 * <p>
	 * Properties are application defined metadata that can be attached to the consumer.
	 * When getting the topic stats, this metadata will be associated to the consumer
	 * stats for easier identification.
	 * @param key the key of the property to add
	 * @param value the value of the property to add
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#property(String, String)
	 */
	default ReactiveMessageConsumerBuilder<T> property(String key, String value) {
		if (getMutableSpec().getProperties() == null) {
			getMutableSpec().setProperties(new LinkedHashMap<>());
		}
		getMutableSpec().getProperties().put(key, value);
		return this;
	}

	/**
	 * Sets the priority level for the consumer.
	 *
	 * <b>Shared subscription</b> Sets the priority level for the shared subscription
	 * consumers to which the broker gives more priority while dispatching messages. Here,
	 * the broker follows descending priorities. (eg: 0=max-priority, 1, 2,..)
	 *
	 * <p>
	 * In Shared subscription mode, the broker will first dispatch messages to max
	 * priority-level consumers if they have permits, else the broker will consider next
	 * priority level consumers.
	 *
	 * <p>
	 * If the subscription has consumer-A with priorityLevel 0 and consumer-B with
	 * priorityLevel 1 then the broker will dispatch messages only to consumer-A until it
	 * runs out of permits, then the broker will start dispatching messages to consumer-B.
	 *
	 * <p>
	 * <pre>
	 * Consumer PriorityLevel Permits
	 * C1       0             2
	 * C2       0             1
	 * C3       0             1
	 * C4       1             2
	 * C5       1             1
	 * Order in which the broker dispatches messages to consumers: C1, C2, C3, C1, C4, C5, C4
	 * </pre>
	 *
	 * <p>
	 * <b>Failover subscription</b> The broker selects the active consumer for a
	 * failover-subscription based on the consumer priority-level and lexicographical
	 * sorting of consumer names. eg: <pre>
	 * 1. Active consumer = C1 : Same priority-level and lexicographical sorting
	 * Consumer PriorityLevel Name
	 * C1       0             aaa
	 * C2       0             bbb
	 *
	 * 2. Active consumer = C2 : Consumer with highest priority
	 * Consumer PriorityLevel Name
	 * C1       1             aaa
	 * C2       0             bbb
	 *
	 * Partitioned-topics:
	 * The broker evenly assigns partitioned topics to highest priority consumers.
	 * </pre>
	 * @param priorityLevel the priority level of this consumer
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#priorityLevel(int)
	 */
	default ReactiveMessageConsumerBuilder<T> priorityLevel(Integer priorityLevel) {
		getMutableSpec().setPriorityLevel(priorityLevel);
		return this;
	}

	/**
	 * Sets whether the consumer will read messages from the compacted topic rather than
	 * reading the full message backlog of the topic. This means that, if the topic has
	 * been compacted, the consumer will only see the latest value for each key in the
	 * topic, up until the point in the topic message backlog that has been compacted.
	 * Beyond that point, the messages will be sent as normal.
	 *
	 * <p>
	 * readCompacted can only be enabled for subscriptions to persistent topics, which
	 * have a single active consumer (i.e. failure or exclusive subscriptions). Attempting
	 * to enable it on subscriptions to a non-persistent topic or on a shared
	 * subscription, will lead to the subscription call throwing a PulsarClientException.
	 * @param readCompacted whether to read from the compacted topic
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#readCompacted(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> readCompacted(boolean readCompacted) {
		getMutableSpec().setReadCompacted(readCompacted);
		return this;
	}

	/**
	 * Enables or disables the batch index acknowledgment. For the batch index
	 * acknowledgment feature to work, it must also be enabled on the broker.
	 * @param batchIndexAckEnabled whether to enable batch index acknowledgment
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#enableBatchIndexAcknowledgment(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> batchIndexAckEnabled(boolean batchIndexAckEnabled) {
		getMutableSpec().setBatchIndexAckEnabled(batchIndexAckEnabled);
		return this;
	}

	/**
	 * Sets the timeout for unacknowledged messages, truncated to the nearest millisecond.
	 * The timeout needs to be greater than 1 second.
	 *
	 * <p>
	 * By default, the acknowledge timeout is disabled and that means that messages
	 * delivered to a consumer will not be re-delivered unless the consumer crashes.
	 *
	 * <p>
	 * When enabling the acknowledge timeout, if a message is not acknowledged within the
	 * specified timeout it will be re-delivered to the consumer (possibly to a different
	 * consumer in case of a shared subscription).
	 * @param ackTimeout the timeout for unacknowledged messages.
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#ackTimeout(long, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> ackTimeout(Duration ackTimeout) {
		getMutableSpec().setAckTimeout(ackTimeout);
		return this;
	}

	/**
	 * Sets the granularity of the ack-timeout redelivery.
	 *
	 * <p>
	 * By default, the tick time is set to 1 second. Using an higher tick time will reduce
	 * the memory overhead to track messages when the ack-timeout is set to bigger values
	 * (eg: 1hour).
	 * @param ackTimeoutTickTime the minimum precision for the acknowledge timeout
	 * messages tracker
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#ackTimeoutTickTime(long, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> ackTimeoutTickTime(Duration ackTimeoutTickTime) {
		getMutableSpec().setAckTimeoutTickTime(ackTimeoutTickTime);
		return this;
	}

	/**
	 * Sets the duration for grouping the acknowledgement messages.
	 *
	 * <p>
	 * By default, the consumer will use a 100 ms grouping time to send the
	 * acknowledgements to the broker.
	 *
	 * <p>
	 * Setting a group time of 0, will send the acknowledgements immediately. A longer
	 * acknowledgement group time will be more efficient at the expense of a slight
	 * increase in message re-deliveries after a failure.
	 * @param acknowledgementsGroupTime the duration for grouping the acknowledgement
	 * messages
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#acknowledgmentGroupTime(long, TimeUnit)
	 */
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
	 * @return the consumer builder instance
	 */
	default ReactiveMessageConsumerBuilder<T> acknowledgeAsynchronously(boolean acknowledgeAsynchronously) {
		getMutableSpec().setAcknowledgeAsynchronously(acknowledgeAsynchronously);
		return this;
	}

	/**
	 * Sets the scheduler to use to handle acknowledgements.
	 * @param acknowledgeScheduler the scheduler to use to handle acknowledgements
	 * @return the consumer builder instance
	 */
	default ReactiveMessageConsumerBuilder<T> acknowledgeScheduler(Scheduler acknowledgeScheduler) {
		getMutableSpec().setAcknowledgeScheduler(acknowledgeScheduler);
		return this;
	}

	/**
	 * Sets the delay to wait before re-delivering messages that have failed to be
	 * processed.
	 *
	 * <p>
	 * When the application uses {@link MessageResult#negativeAcknowledge(Message)}, the
	 * failed message will be redelivered after a fixed timeout. The default is 1 min.
	 * @param negativeAckRedeliveryDelay the redelivery delay for failed messages
	 * @return the consumer builder instance
	 * @see MessageResult#negativeAcknowledge(Message)
	 * @see ConsumerBuilder#negativeAckRedeliveryDelay(long, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> negativeAckRedeliveryDelay(Duration negativeAckRedeliveryDelay) {
		getMutableSpec().setNegativeAckRedeliveryDelay(negativeAckRedeliveryDelay);
		return this;
	}

	/**
	 * Sets a dead letter policy for the consumer.
	 *
	 * <p>
	 * By default messages are redelivered indefinitely if they are not acknowledged. By
	 * using a dead letter mechanism, messages that have reached the max redelivery count
	 * will be acknowledged automatically and send to the configured dead letter topic.
	 *
	 * <p>
	 * You can enable the dead letter mechanism by setting a dead letter policy. Example:
	 * <pre>
	 * client.messageConsumer(Schema.BYTES)
	 *          .deadLetterPolicy(DeadLetterPolicy.builder().maxRedeliverCount(10).build())
	 *          .build();
	 * </pre> Default the dead letter topic name is {TopicName}-{Subscription}-DLQ. You
	 * can set o set a custom dead letter topic name like this: <pre>
	 * client.messageConsumer(Schema.BYTES)
	 *          .deadLetterPolicy(DeadLetterPolicy
	 *              .builder()
	 *              .maxRedeliverCount(10)
	 *              .deadLetterTopic("your-topic-name")
	 *              .build())
	 *          .build();
	 * </pre> When a dead letter policy is specified, and no acknowledgement timeout is
	 * specified, then the acknowledgement timeout will be set to 30000 millisecond.
	 * @param deadLetterPolicy the dead letter policy to use
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#deadLetterPolicy(DeadLetterPolicy)
	 */
	default ReactiveMessageConsumerBuilder<T> deadLetterPolicy(DeadLetterPolicy deadLetterPolicy) {
		getMutableSpec().setDeadLetterPolicy(deadLetterPolicy);
		return this;
	}

	/**
	 * Sets whether automatic routing to retry letter topic and dead letter topic are
	 * enabled.
	 * @param retryLetterTopicEnable whether to automatic retry/dead-letter topics are
	 * enabled
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#enableRetry(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> retryLetterTopicEnable(boolean retryLetterTopicEnable) {
		getMutableSpec().setRetryLetterTopicEnable(retryLetterTopicEnable);
		return this;
	}

	/**
	 * Sets the size of the consumer receiver queue.
	 *
	 * <p>
	 * The consumer receiver queue controls how many messages can be accumulated by the
	 * {@link ReactiveMessageConsumer} before backpressure triggers. Using a higher value
	 * could potentially increase the consumer throughput at the expense of bigger memory
	 * utilization.
	 *
	 * <p>
	 * <b>Setting the consumer queue size to zero</b>
	 * <ul>
	 * <li>Decreases the throughput of the consumer, by disabling pre-fetching of
	 * messages. This approach improves the message distribution on shared subscription,
	 * by pushing messages only to the consumers that are ready to process them./li>
	 * <li>Doesn't support batched messages: if the consumer receives any batched message,
	 * it will close the connection with the broker and
	 * {@link ReactiveMessageConsumer#consumeOne},
	 * {@link ReactiveMessageConsumer#consumeMany} will emit an error. <b> the consumer
	 * will not be able receive any further message unless the batch message is
	 * removed</b></li>
	 * </ul>
	 * The default value is {@code 1000} messages and should be good for most use cases.
	 * @param receiverQueueSize the receiver queue size value
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#receiverQueueSize(int)
	 */
	default ReactiveMessageConsumerBuilder<T> receiverQueueSize(Integer receiverQueueSize) {
		getMutableSpec().setReceiverQueueSize(receiverQueueSize);
		return this;
	}

	/**
	 * Sets the maximum total receiver queue size across partitons.
	 *
	 * <p>
	 * This setting is used to reduce the receiver queue size for individual partitions
	 * {@link #receiverQueueSize(Integer)} if the total exceeds this value (default:
	 * 50000). The purpose of this setting is to have an upper-limit on the number of
	 * messages that a consumer can be pushed at once from a broker, across all the
	 * partitions.
	 * @param maxTotalReceiverQueueSizeAcrossPartitions the maximum pending messages
	 * across all the partitions
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#maxTotalReceiverQueueSizeAcrossPartitions(int)
	 */
	default ReactiveMessageConsumerBuilder<T> maxTotalReceiverQueueSizeAcrossPartitions(
			Integer maxTotalReceiverQueueSizeAcrossPartitions) {
		getMutableSpec().setMaxTotalReceiverQueueSizeAcrossPartitions(maxTotalReceiverQueueSizeAcrossPartitions);
		return this;
	}

	/**
	 * Sets whether the consumer shall automatically subscribe to new partitions added to
	 * the topic. This is only for partitioned topics.
	 * @param autoUpdatePartitions whether to automatically subscribe to new partitions
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#autoUpdatePartitions(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> autoUpdatePartitions(boolean autoUpdatePartitions) {
		getMutableSpec().setAutoUpdatePartitions(autoUpdatePartitions);
		return this;
	}

	/**
	 * Sets the interval for checking partitions updates <i>(default: 1 minute)</i>. This
	 * only applies if {@link #autoUpdatePartitions} is enabled.
	 * @param autoUpdatePartitionsInterval the interval for checking partitions updates
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#autoUpdatePartitionsInterval(int, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> autoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		getMutableSpec().setAutoUpdatePartitionsInterval(autoUpdatePartitionsInterval);
		return this;
	}

	/**
	 * Sets the key reader to be used to decrypt the message payloads.
	 * @param cryptoKeyReader the key reader to be used to decrypt the message payloads.
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#cryptoKeyReader(CryptoKeyReader)
	 */
	default ReactiveMessageConsumerBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	/**
	 * Sets the action the consumer will take in case of decryption failures.
	 * @param cryptoFailureAction the action the consumer will take in case of decryption
	 * failures
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#cryptoFailureAction(ConsumerCryptoFailureAction)
	 */
	default ReactiveMessageConsumerBuilder<T> cryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

	/**
	 * Sets the maximum pending chunked messages. Consumer buffers chunk messages into
	 * memory until it receives all the chunks of the original message. While consuming
	 * chunk-messages, chunks from same message might not be contiguous in the stream and
	 * they might be mixed with other messages' chunks. so, consumer has to maintain
	 * multiple buffers to manage chunks coming from different messages. This mainly
	 * happens when multiple publishers are publishing messages on the topic concurrently
	 * or publisher failed to publish all chunks of the messages.
	 *
	 * <pre>
	 * eg: M1-C1, M2-C1, M1-C2, M2-C2
	 * Here, Messages M1-C1 and M1-C2 belong to original message M1, M2-C1 and M2-C2 messages belong to M2 message.
	 * </pre> Buffering large number of outstanding uncompleted chunked messages can
	 * create memory pressure. It can be guarded by providing a
	 * {@code maxPendingChunkedMessage} threshold. Once the consumer reaches this
	 * threshold, it drops the outstanding unchunked-messages by silently acknowledging or
	 * asking the broker to redeliver later by marking it unacknowledged. This behavior
	 * can be controlled by setting {@link #autoAckOldestChunkedMessageOnQueueFull} The
	 * default value is 10.
	 * @param maxPendingChunkedMessage the maximum pending chunked messages.
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#maxPendingChunkedMessage(int)
	 */
	default ReactiveMessageConsumerBuilder<T> maxPendingChunkedMessage(Integer maxPendingChunkedMessage) {
		getMutableSpec().setMaxPendingChunkedMessage(maxPendingChunkedMessage);
		return this;
	}

	/**
	 * Sets whether non-chunked messages are silently acknowledged when
	 * {@code maxPendingChunkedMessage} is reached. Buffering large number of outstanding
	 * uncompleted chunked messages can create memory pressure. It can be guarded by
	 * providing {@link #maxPendingChunkedMessage} threshold. Once the consumer reaches
	 * this threshold, it drops the outstanding non-chunked messages by silently
	 * acknowledging if autoAckOldestChunkedMessageOnQueueFull is true or else it marks
	 * them for redelivery. Defaults to false.
	 * @param autoAckOldestChunkedMessageOnQueueFull whether non-chunked messages are
	 * silently acknowledged
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#autoAckOldestChunkedMessageOnQueueFull(boolean)
	 */
	default ReactiveMessageConsumerBuilder<T> autoAckOldestChunkedMessageOnQueueFull(
			boolean autoAckOldestChunkedMessageOnQueueFull) {
		getMutableSpec().setAutoAckOldestChunkedMessageOnQueueFull(autoAckOldestChunkedMessageOnQueueFull);
		return this;
	}

	/**
	 * Sets the duration after which incomplete chunked messages are expired (happens for
	 * instance if the producer fails to publish all the chunks).
	 * @param expireTimeOfIncompleteChunkedMessage the duration after which incomplete
	 * chunked messages are expired
	 * @return the consumer builder instance
	 * @see ConsumerBuilder#expireTimeOfIncompleteChunkedMessage(long, TimeUnit)
	 */
	default ReactiveMessageConsumerBuilder<T> expireTimeOfIncompleteChunkedMessage(
			Duration expireTimeOfIncompleteChunkedMessage) {
		getMutableSpec().setExpireTimeOfIncompleteChunkedMessage(expireTimeOfIncompleteChunkedMessage);
		return this;
	}

	/**
	 * Builds the reactive message consumer.
	 * @return the reactive message consumer
	 */
	ReactiveMessageConsumer<T> build();

}
