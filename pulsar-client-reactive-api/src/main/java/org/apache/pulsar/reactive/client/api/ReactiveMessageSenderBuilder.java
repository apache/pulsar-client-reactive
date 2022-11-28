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
import java.util.Map;
import java.util.Set;

import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.reactivestreams.Publisher;

/**
 * Builder interface for {@link ReactiveMessageSender}.
 *
 * @param <T> the message payload type
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessageSenderBuilder<T> {

	/**
	 * Sets the cache to use for the sender.
	 * @param producerCache the cache to set
	 * @return the sender builder instance
	 */
	ReactiveMessageSenderBuilder<T> cache(ReactiveMessageSenderCache producerCache);

	/**
	 * Sets the maximum number of in-flight messages for the sender. When this value is
	 * reached, backpressure will be triggered on the
	 * {@link ReactiveMessageSender#sendOne(MessageSpec)}/{@link ReactiveMessageSender#sendMany(Publisher)}
	 * operations. Note that the maxInflight setting applies globally for all the
	 * operations called on the sender.
	 * @param maxInflight the maximum number of in-flight messages for the sender.
	 * @return the sender builder instance
	 */
	ReactiveMessageSenderBuilder<T> maxInflight(int maxInflight);

	/**
	 * Sets the maximum number of concurrent subscriptions for the sender. Note that the
	 * maxConcurrentSenderSubscriptions setting applies globally for all the operations
	 * called on the sender.
	 * @param maxConcurrentSenderSubscriptions the maximum number of concurrent
	 * subscriptions for the sender.
	 * @return the sender builder instance
	 */
	ReactiveMessageSenderBuilder<T> maxConcurrentSenderSubscriptions(int maxConcurrentSenderSubscriptions);

	/**
	 * Creates and returns a copy of this reactive sender builder.
	 * @return the cloned reactive reader builder
	 */
	ReactiveMessageSenderBuilder<T> clone();

	/**
	 * Applies a sender spec to configure the sender.
	 * @param senderSpec the sender spec to apply
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> applySpec(ReactiveMessageSenderSpec senderSpec) {
		getMutableSpec().applySpec(senderSpec);
		return this;
	}

	/**
	 * Converts this builder to an immutable reactive sender spec with the same settings.
	 * @return the reactive sender spec.
	 */
	default ReactiveMessageSenderSpec toImmutableSpec() {
		return new ImmutableReactiveMessageSenderSpec(getMutableSpec());
	}

	/**
	 * Converts this builder to a mutable reactive sender spec with the same settings.
	 * @return the reactive sender spec.
	 */
	MutableReactiveMessageSenderSpec getMutableSpec();

	/**
	 * Sets the topic this sender will be publishing on.
	 *
	 * <p>
	 * This argument is required when constructing the sender.
	 * @param topicName the name of the topic
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> topic(String topicName) {
		getMutableSpec().setTopicName(topicName);
		return this;
	}

	/**
	 * Sets a name for the producers created under this sender.
	 *
	 * <p>
	 * If not assigned, the system will generate a globally unique name to each producer.
	 *
	 * <p>
	 * <b>Warning</b>: When specifying a name, it is up to the user to ensure that, for a
	 * given topic, the producer name is unique across all Pulsar's clusters. Brokers will
	 * enforce that only a single producer given name can be publishing on a topic.
	 * @param producerName the name to use for the producer
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> producerName(String producerName) {
		getMutableSpec().setProducerName(producerName);
		return this;
	}

	/**
	 * Sets the send timeout <i>(default: 30 seconds)</i> for this sender.
	 *
	 * <p>
	 * If a message is not acknowledged by the server before the sendTimeout expires, an
	 * error will be reported.
	 *
	 * <p>
	 * Setting the timeout to zero, for example {@code setTimeout(Duration.ZERO)} will set
	 * the timeout to infinity, which can be useful when using Pulsar's message
	 * deduplication feature, since the client library will retry forever to publish a
	 * message. No errors will be propagated back to the application.
	 * @param sendTimeout the send timeout to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> sendTimeout(Duration sendTimeout) {
		getMutableSpec().setSendTimeout(sendTimeout);
		return this;
	}

	/**
	 * Sets the maximum size of the queues holding the messages pending to receive an
	 * acknowledgment from the broker. This setting applies to each producer created under
	 * this sender.
	 *
	 * <p>
	 * The producer queue size also determines the max amount of memory that will be
	 * required by the client application. Until the producer gets a successful
	 * acknowledgment back from the broker, it will keep in memory (direct memory pool)
	 * all the messages in the pending queue.
	 *
	 * <p>
	 * Default is 0, disable the pending messages check.
	 * @param maxPendingMessages the maximum size of the pending messages queue for the
	 * sender to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> maxPendingMessages(int maxPendingMessages) {
		getMutableSpec().setMaxPendingMessages(maxPendingMessages);
		return this;
	}

	/**
	 * Sets the maximum number of pending messages across all the partitions. This setting
	 * applies to each producer created under this sender.
	 *
	 * <p>
	 * This setting will be used to lower the max pending messages for each partition
	 * ({@link #maxPendingMessages(int)}), if the total exceeds the configured value. The
	 * purpose of this setting is to have an upper-limit on the number of pending messages
	 * when publishing on a partitioned topic.
	 *
	 * <p>
	 * Default is 0, disable the pending messages across partitions check.
	 *
	 * <p>
	 * If publishing at high rate over a topic with many partitions (especially when
	 * publishing messages without a partitioning key), it might be beneficial to increase
	 * this parameter to allow for more pipelining within the individual partitions
	 * senders.
	 * @param maxPendingMessagesAcrossPartitions the maximum number of pending messages
	 * across all the partitions to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> maxPendingMessagesAcrossPartitions(int maxPendingMessagesAcrossPartitions) {
		getMutableSpec().setMaxPendingMessagesAcrossPartitions(maxPendingMessagesAcrossPartitions);
		return this;
	}

	/**
	 * Sets the routing mode for a partitioned producer. This setting applies to each
	 * producer created under this sender.
	 *
	 * <p>
	 * The default routing mode is to round-robin across the available partitions.
	 *
	 * <p>
	 * This logic is applied when the application is not setting a key on a particular
	 * message. If the key is set with {@link MessageSpecBuilder#key(String)}, then the
	 * hash of the key will be used to select a partition for the message.
	 * @param messageRoutingMode the message routing mode to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> messageRoutingMode(MessageRoutingMode messageRoutingMode) {
		getMutableSpec().setMessageRoutingMode(messageRoutingMode);
		return this;
	}

	/**
	 * Sets the hashing scheme used to choose the partition on where to publish a
	 * particular message.
	 *
	 * <p>
	 * Standard hashing functions available are:
	 * <ul>
	 * <li>{@link HashingScheme#JavaStringHash}: Java {@code String.hashCode()} (Default)
	 * <li>{@link HashingScheme#Murmur3_32Hash}: Use Murmur3 hashing function. <a href=
	 * "https://en.wikipedia.org/wiki/MurmurHash">https://en.wikipedia.org/wiki/MurmurHash</a>
	 * </ul>
	 * @param hashingScheme the hashing scheme to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> hashingScheme(HashingScheme hashingScheme) {
		getMutableSpec().setHashingScheme(hashingScheme);
		return this;
	}

	/**
	 * Sets the action the sender will take in case of encryption failures.
	 * @param cryptoFailureAction the action the sender will take in case of encryption
	 * failures to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> cryptoFailureAction(ProducerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

	/**
	 * Sets a custom message routing policy by passing an implementation of
	 * {@link MessageRouter}.
	 * @param messageRouter the message router to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> messageRouter(MessageRouter messageRouter) {
		getMutableSpec().setMessageRouter(messageRouter);
		return this;
	}

	/**
	 * Sets the time period within which the messages sent will be batched <i>default: 1
	 * ms</i> if batched messages are enabled. If set to a non-zero value, messages will
	 * be queued until either:
	 * <ul>
	 * <li>this time interval expires</li>
	 * <li>the maximum number of messages in a batch is reached
	 * ({@link #batchingMaxMessages(int)})
	 * <li>the maximum size of a batch is reached ({@link #batchingMaxBytes(int)})
	 * </ul>
	 *
	 * <p>
	 * All messages will be published as a single batch message. The consumer will be
	 * delivered individual messages in the batch in the same order they were enqueued.
	 * @param batchingMaxPublishDelay the time period within which the messages sent will
	 * be batched
	 * @return the sender builder instance
	 * @see #batchingMaxMessages(int)
	 * @see #batchingMaxBytes(int)
	 */
	default ReactiveMessageSenderBuilder<T> batchingMaxPublishDelay(Duration batchingMaxPublishDelay) {
		getMutableSpec().setBatchingMaxPublishDelay(batchingMaxPublishDelay);
		return this;
	}

	/**
	 * Sets the partition switch frequency while batching of messages is enabled and using
	 * round-robin routing mode for non-keyed message <i>default: 10</i>.
	 *
	 * <p>
	 * The time period of partition switch is
	 * roundRobinRouterBatchingPartitionSwitchFrequency *
	 * {@link #batchingMaxPublishDelay}. During this period, all messages that arrive will
	 * be routed to the same partition.
	 * @param roundRobinRouterBatchingPartitionSwitchFrequency the frequency of partition
	 * switch
	 * @return the sender builder instance
	 * @see #messageRoutingMode(MessageRoutingMode)
	 * @see #batchingMaxPublishDelay(Duration)
	 */
	default ReactiveMessageSenderBuilder<T> roundRobinRouterBatchingPartitionSwitchFrequency(
			int roundRobinRouterBatchingPartitionSwitchFrequency) {
		getMutableSpec()
				.setRoundRobinRouterBatchingPartitionSwitchFrequency(roundRobinRouterBatchingPartitionSwitchFrequency);
		return this;
	}

	/**
	 * Sets the maximum number of messages permitted in a batch. <i>default: 1000</i> If
	 * set to a value greater than 1, messages will be batched until this threshold or the
	 * maximum byte size of a batch is reached or the batch publish delay has elapsed.
	 *
	 * <p>
	 * All messages in a batch will be published as a single batch message. The consumer
	 * will be delivered individual messages in the batch in the same order they were
	 * enqueued.
	 * @param batchingMaxMessages the maximum number of messages in a batch to set
	 * @return the sender builder instance
	 * @see #batchingMaxPublishDelay(Duration)
	 * @see #batchingMaxBytes(int)
	 */
	default ReactiveMessageSenderBuilder<T> batchingMaxMessages(int batchingMaxMessages) {
		getMutableSpec().setBatchingMaxMessages(batchingMaxMessages);
		return this;
	}

	/**
	 * Sets the maximum number of bytes permitted in a batch. <i>default: 128KB</i> If set
	 * to a value greater than 0, messages will be queued until this threshold is reached
	 * or other batching conditions are met.
	 *
	 * <p>
	 * All messages in a batch will be published as a single batched message. The consumer
	 * will be delivered individual messages in the batch in the same order they were
	 * enqueued.
	 * @param batchingMaxBytes the maximum number of bytes in a batch to set
	 * @return the sender builder instance
	 * @see #batchingMaxPublishDelay(Duration)
	 * @see #batchingMaxMessages(int)
	 */
	default ReactiveMessageSenderBuilder<T> batchingMaxBytes(int batchingMaxBytes) {
		getMutableSpec().setBatchingMaxBytes(batchingMaxBytes);
		return this;
	}

	/**
	 * Sets whether batching of messages is enabled for the sender. <i>default:
	 * enabled</i>
	 *
	 * <p>
	 * When batching is enabled, multiple calls to
	 * {@link ReactiveMessageSender#sendOne(MessageSpec)}/{@link ReactiveMessageSender#sendMany(Publisher)}
	 * may result in a single batch to be sent to the broker, leading to better
	 * throughput, especially when publishing small messages. If compression is enabled,
	 * messages will be compressed at the batch level, leading to a much better
	 * compression ratio for similar headers or contents.
	 *
	 * <p>
	 * When enabled default batch delay is set to 1 ms and default batch size is 1000
	 * messages
	 *
	 * <p>
	 * Batching is enabled by default since 2.0.0.
	 * @param batchingEnabled whether batching is enabled
	 * @return the sender builder instance
	 * @see #batchingMaxPublishDelay(Duration)
	 * @see #batchingMaxMessages(int)
	 */
	default ReactiveMessageSenderBuilder<T> batchingEnabled(boolean batchingEnabled) {
		getMutableSpec().setBatchingEnabled(batchingEnabled);
		return this;
	}

	/**
	 * Sets the batcher builder of the sender. The sender will use the batcher builder to
	 * build a batch message container.This is only used when batching is enabled.
	 * @param batcherBuilder the batcher builder to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> batcherBuilder(BatcherBuilder batcherBuilder) {
		getMutableSpec().setBatcherBuilder(batcherBuilder);
		return this;
	}

	/**
	 * Sets whether chunking of messages is enabled If enabled, when the message size is
	 * higher than the maximum allowed publishing payload size on the broker, then the
	 * sender will split the message into multiple chunks and publish them to the broker
	 * separately and in order. The consumer will stitch them together to form the
	 * original published message.This allows clients to publish large messages.
	 *
	 * <p>
	 * Recommendations to use this feature:
	 *
	 * <pre>
	 * 1. Chunking is only supported by non-shared subscriptions and persistent-topic.
	 * 2. Batching shouldn't be used together with chunking.
	 * 3. The {@link ReactivePulsarClient} keeps published messages into a buffer until it receives the acknowledgements from the broker.
	 * So, it's better to reduce the {@link #maxPendingMessages} size to prevent the sender occupying large amount
	 *  of memory from these buffered messages.
	 * 4. Set the message TTL on the namespace to cleanup incomplete chunked messages.
	 * (sometimes, due to broker-restart or publish timeout, the sender might fail to publish an entire large message.
	 * So the consumer will not be able to consume and acknowledge those messages. So, those messages can
	 * only be discarded by message TTL) Or configure
	 * {@link ReactiveMessageConsumerBuilder#expireTimeOfIncompleteChunkedMessage}
	 * 5. Consumer configuration: the consumer should also configure {@link ReactiveMessageConsumerBuilder#receiverQueueSize} and {@link ReactiveMessageConsumerBuilder#maxPendingChunkedMessage}
	 * </pre>
	 * @param chunkingEnabled whether to enable chunking
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> chunkingEnabled(boolean chunkingEnabled) {
		getMutableSpec().setChunkingEnabled(chunkingEnabled);
		return this;
	}

	/**
	 * Sets the key reader to be used to encrypt the message payloads.
	 * @param cryptoKeyReader the key reader to be used to encrypt the message payloads.
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	/**
	 * Sets the public encryption key names, used by the producer to encrypt the data key.
	 *
	 * <p>
	 * At the time of producer creation, the Pulsar client checks if there are keys added
	 * to encryptionKeys. If keys are found, a callback
	 * {@link CryptoKeyReader#getPrivateKey(String, Map)} and
	 * {@link CryptoKeyReader#getPublicKey(String, Map)} is invoked against each key to
	 * load the values of the key. Applications should implement this callback to return
	 * the key in pkcs8 format. If compression is enabled, the message is encrypted after
	 * compression. If batch messaging is enabled, the batched message is encrypted.
	 * @param encryptionKeys the names of the encryption keys in the key store
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> encryptionKeys(Set<String> encryptionKeys) {
		getMutableSpec().setEncryptionKeys(encryptionKeys);
		return this;
	}

	/**
	 * Sets the compression type for the sender.
	 *
	 * <p>
	 * By default, message payloads are not compressed. Supported compression types are:
	 * <ul>
	 * <li>{@link CompressionType#NONE}: No compression (Default)</li>
	 * <li>{@link CompressionType#LZ4}: Compress with LZ4 algorithm. Faster but lower
	 * compression than ZLib</li>
	 * <li>{@link CompressionType#ZLIB}: Standard ZLib compression</li>
	 * <li>{@link CompressionType#ZSTD}: Compress with Zstandard codec. Since Pulsar 2.3.
	 * Zstd cannot be used if consumer applications are not in version >= 2.3 as well</li>
	 * <li>{@link CompressionType#SNAPPY} Compress with Snappy codec. Since Pulsar 2.4.
	 * Snappy cannot be used if consumer applications are not in version >= 2.4 as
	 * well</li>
	 * </ul>
	 * @param compressionType the compression type to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> compressionType(CompressionType compressionType) {
		getMutableSpec().setCompressionType(compressionType);
		return this;
	}

	/**
	 * Sets the baseline for the sequence ids for messages published by the producer. This
	 * setting applies to each producer created under this sender.
	 *
	 * <p>
	 * First message will be using {@code (initialSequenceId + 1)} as its sequence id and
	 * subsequent messages will be assigned incremental sequence ids, if not otherwise
	 * specified.
	 * @param initialSequenceId the initial sequence id for the producer to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> initialSequenceId(long initialSequenceId) {
		getMutableSpec().setInitialSequenceId(initialSequenceId);
		return this;
	}

	/**
	 * If enabled, the sender will automatically discover new partitions of partitioned
	 * topics at runtime.
	 *
	 * <p>
	 * Default is true.
	 * @param autoUpdatePartitions whether to auto discover the partition configuration
	 * changes
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> autoUpdatePartitions(boolean autoUpdatePartitions) {
		getMutableSpec().setAutoUpdatePartitions(autoUpdatePartitions);
		return this;
	}

	/**
	 * Sets the interval of partitions updates <i>(default: 1 minute)</i>. This only works
	 * if {@link #autoUpdatePartitions} is enabled.
	 * @param autoUpdatePartitionsInterval the interval of partitions updates
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> autoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		getMutableSpec().setAutoUpdatePartitionsInterval(autoUpdatePartitionsInterval);
		return this;
	}

	/**
	 * Sets whether to enable the multiple schema mode for the producer. If enabled, the
	 * producer can send a message with a different schema from the one specified when it
	 * was created, otherwise an invalid message exception would be thrown.
	 *
	 * <p>
	 * Enabled by default.
	 * @param multiSchema whether to enable or disable multiple schema mode
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> multiSchema(boolean multiSchema) {
		getMutableSpec().setMultiSchema(multiSchema);
		return this;
	}

	/**
	 * Sets the type of access mode that the producer requires on the topic. This setting
	 * applies to each producer created under this sender.
	 *
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li>{@link ProducerAccessMode#Shared}: By default multiple producers can publish on
	 * a topic
	 * <li>{@link ProducerAccessMode#Exclusive}: Require exclusive access for producer.
	 * Fail immediately if there's already a producer connected.
	 * <li>{@link ProducerAccessMode#WaitForExclusive}: Producer creation is pending until
	 * it can acquire exclusive access
	 * </ul>
	 * @param accessMode the access mode to set
	 * @return the producer builder instance
	 */
	default ReactiveMessageSenderBuilder<T> accessMode(ProducerAccessMode accessMode) {
		getMutableSpec().setAccessMode(accessMode);
		return this;
	}

	/**
	 * Sets whether to start partitioned producers lazily. This setting applies to each
	 * producer created under this sender. This config affects Shared mode producers of
	 * partitioned topics only. It controls whether producers register and connect
	 * immediately to the owner broker of each partition or start lazily on demand. The
	 * internal producer of one partition is always started eagerly, chosen by the routing
	 * policy, but the internal producers of any additional partitions are started on
	 * demand, upon receiving their first message. Using this mode can reduce the strain
	 * on brokers for topics with large numbers of partitions and when the SinglePartition
	 * or some custom partial partition routing policy like
	 * PartialRoundRobinMessageRouterImpl is used without keyed messages. Because producer
	 * connection can be on demand, this can produce extra send latency for the first
	 * messages of a given partition.
	 * @param lazyStartPartitionedProducers whether to start partition producers lazily
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> lazyStartPartitionedProducers(boolean lazyStartPartitionedProducers) {
		getMutableSpec().setLazyStartPartitionedProducers(lazyStartPartitionedProducers);
		return this;
	}

	/**
	 * Adds a property to the producer. This setting applies to each producer created
	 * under this sender.
	 *
	 * <p>
	 * Properties are application defined metadata that can be attached to the sender.
	 * When getting the topic stats, this metadata will be associated to the sender stats
	 * for easier identification.
	 * @param key the key of the property to add
	 * @param value the value of the property to add
	 * @return the producer builder instance
	 * @see ProducerBuilder#property(String, String)
	 */
	default ReactiveMessageSenderBuilder<T> property(String key, String value) {
		if (getMutableSpec().getProperties() == null) {
			getMutableSpec().setProperties(new LinkedHashMap<>());
		}
		getMutableSpec().getProperties().put(key, value);
		return this;
	}

	/**
	 * Sets the properties to the producer. This setting applies to each producer created
	 * under this sender.
	 *
	 * <p>
	 * Properties are application defined metadata that can be attached to the sender.
	 * When getting the topic stats, this metadata will be associated to the sender stats
	 * for easier identification.
	 * @param properties the properties to set
	 * @return the sender builder instance
	 */
	default ReactiveMessageSenderBuilder<T> properties(Map<String, String> properties) {
		getMutableSpec().setProperties(properties);
		return this;
	}

	/**
	 * Builds the reactive message sender.
	 * @return the reactive message sender
	 */
	ReactiveMessageSender<T> build();

}
