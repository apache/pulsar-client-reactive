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
import java.util.Collections;
import java.util.HashSet;
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
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;

/**
 * Mutable spec for a {@link ReactiveMessageSender}.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public class MutableReactiveMessageSenderSpec implements ReactiveMessageSenderSpec {

	private String topicName;

	private String producerName;

	private Duration sendTimeout;

	private Integer maxPendingMessages;

	private Integer maxPendingMessagesAcrossPartitions;

	private MessageRoutingMode messageRoutingMode;

	private HashingScheme hashingScheme;

	private ProducerCryptoFailureAction cryptoFailureAction;

	private MessageRouter messageRouter;

	private Duration batchingMaxPublishDelay;

	private Integer roundRobinRouterBatchingPartitionSwitchFrequency;

	private Integer batchingMaxMessages;

	private Integer batchingMaxBytes;

	private Boolean batchingEnabled;

	private BatcherBuilder batcherBuilder;

	private Boolean chunkingEnabled;

	private CryptoKeyReader cryptoKeyReader;

	private Set<String> encryptionKeys;

	private CompressionType compressionType;

	private Long initialSequenceId;

	private Boolean autoUpdatePartitions;

	private Duration autoUpdatePartitionsInterval;

	private Boolean multiSchema;

	private ProducerAccessMode accessMode;

	private Boolean lazyStartPartitionedProducers;

	private Map<String, String> properties;

	/**
	 * Constructs a default MutableReactiveMessageSenderSpec.
	 */
	public MutableReactiveMessageSenderSpec() {

	}

	/**
	 * Constructs a MutableReactiveMessageSenderSpec from another
	 * {@link ReactiveMessageSenderSpec}.
	 * @param senderSpec the spec to construct from
	 */
	public MutableReactiveMessageSenderSpec(ReactiveMessageSenderSpec senderSpec) {
		this.topicName = senderSpec.getTopicName();
		this.producerName = senderSpec.getProducerName();
		this.sendTimeout = senderSpec.getSendTimeout();
		this.maxPendingMessages = senderSpec.getMaxPendingMessages();
		this.maxPendingMessagesAcrossPartitions = senderSpec.getMaxPendingMessagesAcrossPartitions();
		this.messageRoutingMode = senderSpec.getMessageRoutingMode();
		this.hashingScheme = senderSpec.getHashingScheme();
		this.cryptoFailureAction = senderSpec.getCryptoFailureAction();
		this.messageRouter = senderSpec.getMessageRouter();
		this.batchingMaxPublishDelay = senderSpec.getBatchingMaxPublishDelay();
		this.roundRobinRouterBatchingPartitionSwitchFrequency = senderSpec
				.getRoundRobinRouterBatchingPartitionSwitchFrequency();
		this.batchingMaxMessages = senderSpec.getBatchingMaxMessages();
		this.batchingMaxBytes = senderSpec.getBatchingMaxBytes();
		this.batchingEnabled = senderSpec.getBatchingEnabled();
		this.batcherBuilder = senderSpec.getBatcherBuilder();
		this.chunkingEnabled = senderSpec.getChunkingEnabled();
		this.cryptoKeyReader = senderSpec.getCryptoKeyReader();
		this.encryptionKeys = (senderSpec.getEncryptionKeys() != null && !senderSpec.getEncryptionKeys().isEmpty())
				? new HashSet<>(senderSpec.getEncryptionKeys()) : null;

		this.compressionType = senderSpec.getCompressionType();
		this.initialSequenceId = senderSpec.getInitialSequenceId();
		this.autoUpdatePartitions = senderSpec.getAutoUpdatePartitions();
		this.autoUpdatePartitionsInterval = senderSpec.getAutoUpdatePartitionsInterval();
		this.multiSchema = senderSpec.getMultiSchema();
		this.accessMode = senderSpec.getAccessMode();
		this.lazyStartPartitionedProducers = senderSpec.getLazyStartPartitionedProducers();
		this.properties = (senderSpec.getProperties() != null && !senderSpec.getProperties().isEmpty())
				? Collections.unmodifiableMap(new LinkedHashMap<>(senderSpec.getProperties())) : null;
	}

	@Override
	public String getTopicName() {
		return this.topicName;
	}

	/**
	 * Sets the topic to publish on.
	 * @param topicName the topic
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	@Override
	public String getProducerName() {
		return this.producerName;
	}

	/**
	 * Sets the name of the producer.
	 * @param producerName the producer name
	 */
	public void setProducerName(String producerName) {
		this.producerName = producerName;
	}

	@Override
	public Duration getSendTimeout() {
		return this.sendTimeout;
	}

	/**
	 * Sets the send timeout.
	 * @param sendTimeout the send timeout
	 */
	public void setSendTimeout(Duration sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	@Override
	public Integer getMaxPendingMessages() {
		return this.maxPendingMessages;
	}

	/**
	 * Sets the maximum size of the queue holding the messages pending to receive an
	 * acknowledgment from the broker.
	 * @param maxPendingMessages the maximum pending messages
	 */
	public void setMaxPendingMessages(Integer maxPendingMessages) {
		this.maxPendingMessages = maxPendingMessages;
	}

	@Override
	public Integer getMaxPendingMessagesAcrossPartitions() {
		return this.maxPendingMessagesAcrossPartitions;
	}

	/**
	 * Sets the maximum number of pending messages across all the partitions.
	 * @param maxPendingMessagesAcrossPartitions the maximum number of pending messages
	 * across all the partitions
	 */
	public void setMaxPendingMessagesAcrossPartitions(Integer maxPendingMessagesAcrossPartitions) {
		this.maxPendingMessagesAcrossPartitions = maxPendingMessagesAcrossPartitions;
	}

	@Override
	public MessageRoutingMode getMessageRoutingMode() {
		return this.messageRoutingMode;
	}

	/**
	 * Sets the message routing logic for producers on partitioned topics.
	 * @param messageRoutingMode the message routing mode
	 */
	public void setMessageRoutingMode(MessageRoutingMode messageRoutingMode) {
		this.messageRoutingMode = messageRoutingMode;
	}

	@Override
	public HashingScheme getHashingScheme() {
		return this.hashingScheme;
	}

	/**
	 * Sets the hashing function determining the partition where to publish a particular
	 * message on partitioned topics.
	 * @param hashingScheme the hashing scheme
	 */
	public void setHashingScheme(HashingScheme hashingScheme) {
		this.hashingScheme = hashingScheme;
	}

	@Override
	public ProducerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	/**
	 * Sets the action the producer will take in case of encryption failures.
	 * @param cryptoFailureAction the action the producer will take in case of encryption
	 * failures
	 */
	public void setCryptoFailureAction(ProducerCryptoFailureAction cryptoFailureAction) {
		this.cryptoFailureAction = cryptoFailureAction;
	}

	@Override
	public MessageRouter getMessageRouter() {
		return this.messageRouter;
	}

	/**
	 * Sets a custom message router.
	 * @param messageRouter the message router
	 */
	public void setMessageRouter(MessageRouter messageRouter) {
		this.messageRouter = messageRouter;
	}

	@Override
	public Duration getBatchingMaxPublishDelay() {
		return this.batchingMaxPublishDelay;
	}

	/**
	 * Sets the time period within which the messages sent will be batched.
	 * @param batchingMaxPublishDelay the batch delay
	 */
	public void setBatchingMaxPublishDelay(Duration batchingMaxPublishDelay) {
		this.batchingMaxPublishDelay = batchingMaxPublishDelay;
	}

	@Override
	public Integer getRoundRobinRouterBatchingPartitionSwitchFrequency() {
		return this.roundRobinRouterBatchingPartitionSwitchFrequency;
	}

	/**
	 * Sets the partition switch frequency while batching of messages is enabled and using
	 * round-robin routing mode for non-keyed message.
	 * @param roundRobinRouterBatchingPartitionSwitchFrequency the frequency of partition
	 * switch
	 */
	public void setRoundRobinRouterBatchingPartitionSwitchFrequency(
			Integer roundRobinRouterBatchingPartitionSwitchFrequency) {
		this.roundRobinRouterBatchingPartitionSwitchFrequency = roundRobinRouterBatchingPartitionSwitchFrequency;
	}

	@Override
	public Integer getBatchingMaxMessages() {
		return this.batchingMaxMessages;
	}

	/**
	 * Sets the maximum number of messages permitted in a batch.
	 * @param batchingMaxMessages the maximum number of messages in a batch
	 */
	public void setBatchingMaxMessages(Integer batchingMaxMessages) {
		this.batchingMaxMessages = batchingMaxMessages;
	}

	@Override
	public Integer getBatchingMaxBytes() {
		return this.batchingMaxBytes;
	}

	/**
	 * Sets the maximum number of bytes permitted in a batch.
	 * @param batchingMaxBytes the maximum number of bytes in a batch
	 */
	public void setBatchingMaxBytes(Integer batchingMaxBytes) {
		this.batchingMaxBytes = batchingMaxBytes;
	}

	@Override
	public Boolean getBatchingEnabled() {
		return this.batchingEnabled;
	}

	/**
	 * Sets whether automatic batching of messages is enabled for the producer.
	 * @param batchingEnabled true to enable batching
	 */
	public void setBatchingEnabled(Boolean batchingEnabled) {
		this.batchingEnabled = batchingEnabled;
	}

	@Override
	public BatcherBuilder getBatcherBuilder() {
		return this.batcherBuilder;
	}

	/**
	 * Sets the batcher builder of the producer.
	 * @param batcherBuilder the batcher builder
	 */
	public void setBatcherBuilder(BatcherBuilder batcherBuilder) {
		this.batcherBuilder = batcherBuilder;
	}

	@Override
	public Boolean getChunkingEnabled() {
		return this.chunkingEnabled;
	}

	/**
	 * Sets whether chunking of messages is enabled.
	 * @param chunkingEnabled true to enable message chunking
	 */
	public void setChunkingEnabled(Boolean chunkingEnabled) {
		this.chunkingEnabled = chunkingEnabled;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	/**
	 * Sets the key reader to be used to encrypt the message payloads.
	 * @param cryptoKeyReader the key reader to be used to encrypt the message payloads
	 */
	public void setCryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		this.cryptoKeyReader = cryptoKeyReader;
	}

	@Override
	public Set<String> getEncryptionKeys() {
		return this.encryptionKeys;
	}

	/**
	 * Sets the public encryption key names, used by producer to encrypt the data key.
	 * @param encryptionKeys the public encryption key names
	 */
	public void setEncryptionKeys(Set<String> encryptionKeys) {
		this.encryptionKeys = encryptionKeys;
	}

	@Override
	public CompressionType getCompressionType() {
		return this.compressionType;
	}

	/**
	 * Sets the compression type for the producer.
	 * @param compressionType the compression type
	 */
	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	@Override
	public Long getInitialSequenceId() {
		return this.initialSequenceId;
	}

	/**
	 * Sets the baseline for the sequence ids for messages published by the producer.
	 * @param initialSequenceId the initial sequence id
	 */
	public void setInitialSequenceId(Long initialSequenceId) {
		this.initialSequenceId = initialSequenceId;
	}

	@Override
	public Boolean getAutoUpdatePartitions() {
		return this.autoUpdatePartitions;
	}

	/**
	 * Sets whether partitioned producer will automatically discover new partitions at
	 * runtime.
	 * @param autoUpdatePartitions true to enable auto discovery of the partition
	 * configuration changes
	 */
	public void setAutoUpdatePartitions(Boolean autoUpdatePartitions) {
		this.autoUpdatePartitions = autoUpdatePartitions;
	}

	@Override
	public Duration getAutoUpdatePartitionsInterval() {
		return this.autoUpdatePartitionsInterval;
	}

	/**
	 * Sets the interval of partitions updates if autoUpdatePartitions is enabled.
	 * @param autoUpdatePartitionsInterval the interval of partitions updates
	 */
	public void setAutoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
	}

	@Override
	public Boolean getMultiSchema() {
		return this.multiSchema;
	}

	/**
	 * Sets whether the multiple schema mode is enabled.
	 * @param multiSchema true to enable the multiple schema mode
	 */
	public void setMultiSchema(Boolean multiSchema) {
		this.multiSchema = multiSchema;
	}

	@Override
	public ProducerAccessMode getAccessMode() {
		return this.accessMode;
	}

	/**
	 * Sets the type of access mode that the producer requires on the topic.
	 * @param accessMode the access mode
	 */
	public void setAccessMode(ProducerAccessMode accessMode) {
		this.accessMode = accessMode;
	}

	@Override
	public Boolean getLazyStartPartitionedProducers() {
		return this.lazyStartPartitionedProducers;
	}

	/**
	 * Sets whether producers register and connect immediately to the owner broker of each
	 * partition or start lazily on demand.
	 * @param lazyStartPartitionedProducers true to enable lazy starting of partitioned
	 * producers
	 */
	public void setLazyStartPartitionedProducers(Boolean lazyStartPartitionedProducers) {
		this.lazyStartPartitionedProducers = lazyStartPartitionedProducers;
	}

	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

	/**
	 * Sets the properties of the producer.
	 * @param properties the properties of the producer
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * Updates this spec from the defined values of another spec.
	 * @param senderSpec the spec from which to update
	 */
	public void applySpec(ReactiveMessageSenderSpec senderSpec) {
		if (senderSpec.getTopicName() != null) {
			setTopicName(senderSpec.getTopicName());
		}

		if (senderSpec.getProducerName() != null) {
			setProducerName(senderSpec.getProducerName());
		}

		if (senderSpec.getSendTimeout() != null) {
			setSendTimeout(senderSpec.getSendTimeout());
		}

		if (senderSpec.getMaxPendingMessages() != null) {
			setMaxPendingMessages(senderSpec.getMaxPendingMessages());
		}

		if (senderSpec.getMaxPendingMessagesAcrossPartitions() != null) {
			setMaxPendingMessagesAcrossPartitions(senderSpec.getMaxPendingMessagesAcrossPartitions());
		}

		if (senderSpec.getMessageRoutingMode() != null) {
			setMessageRoutingMode(senderSpec.getMessageRoutingMode());
		}

		if (senderSpec.getHashingScheme() != null) {
			setHashingScheme(senderSpec.getHashingScheme());
		}

		if (senderSpec.getCryptoFailureAction() != null) {
			setCryptoFailureAction(senderSpec.getCryptoFailureAction());
		}

		if (senderSpec.getMessageRouter() != null) {
			setMessageRouter(senderSpec.getMessageRouter());
		}

		if (senderSpec.getBatchingMaxPublishDelay() != null) {
			setBatchingMaxPublishDelay(senderSpec.getBatchingMaxPublishDelay());
		}

		if (senderSpec.getRoundRobinRouterBatchingPartitionSwitchFrequency() != null) {
			setRoundRobinRouterBatchingPartitionSwitchFrequency(
					senderSpec.getRoundRobinRouterBatchingPartitionSwitchFrequency());
		}

		if (senderSpec.getBatchingMaxMessages() != null) {
			setBatchingMaxMessages(senderSpec.getBatchingMaxMessages());
		}

		if (senderSpec.getBatchingMaxBytes() != null) {
			setBatchingMaxBytes(senderSpec.getBatchingMaxBytes());
		}

		if (senderSpec.getBatchingEnabled() != null) {
			setBatchingEnabled(senderSpec.getBatchingEnabled());
		}

		if (senderSpec.getBatcherBuilder() != null) {
			setBatcherBuilder(senderSpec.getBatcherBuilder());
		}

		if (senderSpec.getChunkingEnabled() != null) {
			setChunkingEnabled(senderSpec.getChunkingEnabled());
		}

		if (senderSpec.getCryptoKeyReader() != null) {
			setCryptoKeyReader(senderSpec.getCryptoKeyReader());
		}

		if (senderSpec.getEncryptionKeys() != null && !senderSpec.getEncryptionKeys().isEmpty()) {
			setEncryptionKeys(new HashSet<>(senderSpec.getEncryptionKeys()));
		}

		if (senderSpec.getCompressionType() != null) {
			setCompressionType(senderSpec.getCompressionType());
		}

		if (senderSpec.getInitialSequenceId() != null) {
			setInitialSequenceId(senderSpec.getInitialSequenceId());
		}

		if (senderSpec.getAutoUpdatePartitions() != null) {
			setAutoUpdatePartitions(senderSpec.getAutoUpdatePartitions());
		}

		if (senderSpec.getAutoUpdatePartitionsInterval() != null) {
			setAutoUpdatePartitionsInterval(senderSpec.getAutoUpdatePartitionsInterval());
		}

		if (senderSpec.getMultiSchema() != null) {
			setMultiSchema(senderSpec.getMultiSchema());
		}

		if (senderSpec.getAccessMode() != null) {
			setAccessMode(senderSpec.getAccessMode());
		}

		if (senderSpec.getLazyStartPartitionedProducers() != null) {
			setLazyStartPartitionedProducers(senderSpec.getLazyStartPartitionedProducers());
		}

		if (senderSpec.getProperties() != null && !senderSpec.getProperties().isEmpty()) {
			setProperties(new LinkedHashMap<>(senderSpec.getProperties()));
		}
	}

}
