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

/**
 * Spec for {@link ReactiveMessageSender} configuration.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessageSenderSpec {

	/**
	 * Gets the topic to publish on.
	 * @return the topic
	 * @see ProducerBuilder#topic
	 */
	String getTopicName();

	/**
	 * Gets the name of the producer.
	 * @return the producer name
	 * @see ProducerBuilder#producerName
	 */
	String getProducerName();

	/**
	 * Gets the send timeout.
	 * @return the send timeout
	 * @see ProducerBuilder#sendTimeout
	 */
	Duration getSendTimeout();

	/**
	 * Gets the maximum size of the queue holding the messages pending to receive an
	 * acknowledgment from the broker.
	 * @return the maximum pending messages
	 * @see ProducerBuilder#maxPendingMessages
	 */
	Integer getMaxPendingMessages();

	/**
	 * Gets the maximum number of pending messages across all the partitions.
	 * @return the maximum number of pending messages across all the partitions
	 * @see ProducerBuilder#maxPendingMessagesAcrossPartitions
	 */
	Integer getMaxPendingMessagesAcrossPartitions();

	/**
	 * Gets the message routing logic for producers on partitioned topics.
	 * @return the message routing mode
	 * @see ProducerBuilder#messageRoutingMode
	 */
	MessageRoutingMode getMessageRoutingMode();

	/**
	 * Gets the hashing function determining the partition where to publish a particular
	 * message on partitioned topics.
	 * @return the hashing scheme
	 * @see ProducerBuilder#hashingScheme
	 */
	HashingScheme getHashingScheme();

	/**
	 * Gets the action the producer will take in case of encryption failures.
	 * @return the action the producer will take in case of encryption failures
	 * @see ProducerBuilder#cryptoFailureAction
	 */
	ProducerCryptoFailureAction getCryptoFailureAction();

	/**
	 * Gets the custom message router.
	 * @return the message router
	 * @see ProducerBuilder#messageRouter
	 */
	MessageRouter getMessageRouter();

	/**
	 * Gets the time period within which the messages sent will be batched.
	 * @return the batch delay
	 * @see ProducerBuilder#batchingMaxPublishDelay
	 */
	Duration getBatchingMaxPublishDelay();

	/**
	 * Gets the partition switch frequency while batching of messages is enabled and using
	 * round-robin routing mode for non-keyed message.
	 * @return the frequency of partition switch
	 * @see ProducerBuilder#roundRobinRouterBatchingPartitionSwitchFrequency
	 */
	Integer getRoundRobinRouterBatchingPartitionSwitchFrequency();

	/**
	 * Gets the maximum number of messages permitted in a batch.
	 * @return the maximum number of messages in a batch
	 * @see ProducerBuilder#batchingMaxMessages
	 */
	Integer getBatchingMaxMessages();

	/**
	 * Gets the maximum number of bytes permitted in a batch.
	 * @return the maximum bytes of messages in a batch
	 * @see ProducerBuilder#batchingMaxBytes
	 */
	Integer getBatchingMaxBytes();

	/**
	 * Gets whether automatic batching of messages is enabled for the producer.
	 * @return true if batching is enabled
	 * @see ProducerBuilder#enableBatching
	 */
	Boolean getBatchingEnabled();

	/**
	 * Gets the batcher builder of the producer.
	 * @return the batcher builder
	 * @see ProducerBuilder#batcherBuilder
	 */
	BatcherBuilder getBatcherBuilder();

	/**
	 * Gets whether chunking of messages is enabled.
	 * @return true if message chunking is enabled
	 * @see ProducerBuilder#enableChunking
	 */
	Boolean getChunkingEnabled();

	/**
	 * Gets the key reader to be used to encrypt the message payloads.
	 * @return the key reader to be used to encrypt the message payloads
	 * @see ProducerBuilder#cryptoKeyReader
	 */
	CryptoKeyReader getCryptoKeyReader();

	/**
	 * Gets the public encryption key names, used by producer to encrypt the data key.
	 * @return the public encryption key names
	 * @see ProducerBuilder#addEncryptionKey
	 */
	Set<String> getEncryptionKeys();

	/**
	 * Gets the compression type for the producer.
	 * @return the compression type
	 * @see ProducerBuilder#compressionType
	 */
	CompressionType getCompressionType();

	/**
	 * Gets the baseline for the sequence ids for messages published by the producer.
	 * @return the initial sequence id
	 * @see ProducerBuilder#initialSequenceId
	 */
	Long getInitialSequenceId();

	/**
	 * Gets whether partitioned producer will automatically discover new partitions at
	 * runtime.
	 * @return true if auto discovery of the partition configuration changes is enabled
	 * @see ProducerBuilder#autoUpdatePartitions
	 */
	Boolean getAutoUpdatePartitions();

	/**
	 * Gets the interval of partitions updates if autoUpdatePartitions is enabled.
	 * @return the interval of partitions updates
	 * @see ProducerBuilder#autoUpdatePartitionsInterval
	 */
	Duration getAutoUpdatePartitionsInterval();

	/**
	 * Gets whether the multiple schema mode is enabled.
	 * @return true if the multiple schema mode is enabled
	 * @see ProducerBuilder#enableMultiSchema
	 */
	Boolean getMultiSchema();

	/**
	 * Gets the type of access mode that the producer requires on the topic.
	 * @return the access mode
	 * @see ProducerBuilder#accessMode
	 */
	ProducerAccessMode getAccessMode();

	/**
	 * Gets whether producers register and connect immediately to the owner broker of each
	 * partition or start lazily on demand.
	 * @return true if lazy starting of partitioned producers is enabled
	 * @see ProducerBuilder#enableLazyStartPartitionedProducers
	 */
	Boolean getLazyStartPartitionedProducers();

	/**
	 * Gets the properties of the producer.
	 * @return the properties of the producer
	 * @see ProducerBuilder#properties
	 */
	Map<String, String> getProperties();

}
