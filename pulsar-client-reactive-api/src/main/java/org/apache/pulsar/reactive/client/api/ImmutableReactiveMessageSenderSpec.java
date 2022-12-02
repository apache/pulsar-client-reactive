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
 * Immutable spec for a {@link ReactiveMessageSender}.
 */
public class ImmutableReactiveMessageSenderSpec implements ReactiveMessageSenderSpec {

	private final String topicName;

	private final String producerName;

	private final Duration sendTimeout;

	private final Integer maxPendingMessages;

	private final Integer maxPendingMessagesAcrossPartitions;

	private final MessageRoutingMode messageRoutingMode;

	private final HashingScheme hashingScheme;

	private final ProducerCryptoFailureAction cryptoFailureAction;

	private final MessageRouter messageRouter;

	private final Duration batchingMaxPublishDelay;

	private final Integer roundRobinRouterBatchingPartitionSwitchFrequency;

	private final Integer batchingMaxMessages;

	private final Integer batchingMaxBytes;

	private final Boolean batchingEnabled;

	private final BatcherBuilder batcherBuilder;

	private final Boolean chunkingEnabled;

	private final CryptoKeyReader cryptoKeyReader;

	private final Set<String> encryptionKeys;

	private final CompressionType compressionType;

	private final Long initialSequenceId;

	private final Boolean autoUpdatePartitions;

	private final Duration autoUpdatePartitionsInterval;

	private final Boolean multiSchema;

	private final ProducerAccessMode accessMode;

	private final Boolean lazyStartPartitionedProducers;

	private final Map<String, String> properties;

	public ImmutableReactiveMessageSenderSpec(String topicName, String producerName, Duration sendTimeout,
			Integer maxPendingMessages, Integer maxPendingMessagesAcrossPartitions,
			MessageRoutingMode messageRoutingMode, HashingScheme hashingScheme,
			ProducerCryptoFailureAction cryptoFailureAction, MessageRouter messageRouter,
			Duration batchingMaxPublishDelay, Integer roundRobinRouterBatchingPartitionSwitchFrequency,
			Integer batchingMaxMessages, Integer batchingMaxBytes, Boolean batchingEnabled,
			BatcherBuilder batcherBuilder, Boolean chunkingEnabled, CryptoKeyReader cryptoKeyReader,
			Set<String> encryptionKeys, CompressionType compressionType, Long initialSequenceId,
			Boolean autoUpdatePartitions, Duration autoUpdatePartitionsInterval, Boolean multiSchema,
			ProducerAccessMode accessMode, Boolean lazyStartPartitionedProducers, Map<String, String> properties) {
		this.topicName = topicName;
		this.producerName = producerName;
		this.sendTimeout = sendTimeout;
		this.maxPendingMessages = maxPendingMessages;
		this.maxPendingMessagesAcrossPartitions = maxPendingMessagesAcrossPartitions;
		this.messageRoutingMode = messageRoutingMode;
		this.hashingScheme = hashingScheme;
		this.cryptoFailureAction = cryptoFailureAction;
		this.messageRouter = messageRouter;
		this.batchingMaxPublishDelay = batchingMaxPublishDelay;
		this.roundRobinRouterBatchingPartitionSwitchFrequency = roundRobinRouterBatchingPartitionSwitchFrequency;
		this.batchingMaxMessages = batchingMaxMessages;
		this.batchingMaxBytes = batchingMaxBytes;
		this.batchingEnabled = batchingEnabled;
		this.batcherBuilder = batcherBuilder;
		this.chunkingEnabled = chunkingEnabled;
		this.cryptoKeyReader = cryptoKeyReader;
		this.encryptionKeys = encryptionKeys;
		this.compressionType = compressionType;
		this.initialSequenceId = initialSequenceId;
		this.autoUpdatePartitions = autoUpdatePartitions;
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
		this.multiSchema = multiSchema;
		this.accessMode = accessMode;
		this.lazyStartPartitionedProducers = lazyStartPartitionedProducers;
		this.properties = properties;
	}

	/**
	 * Constructs a ImmutableReactiveMessageConsumerSpec from another
	 * {@link ReactiveMessageConsumerSpec}.
	 * @param senderSpec the spec to construct from
	 */
	public ImmutableReactiveMessageSenderSpec(ReactiveMessageSenderSpec senderSpec) {
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
				? Collections.unmodifiableSet(new HashSet<>(senderSpec.getEncryptionKeys())) : null;

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

	@Override
	public String getProducerName() {
		return this.producerName;
	}

	@Override
	public Duration getSendTimeout() {
		return this.sendTimeout;
	}

	@Override
	public Integer getMaxPendingMessages() {
		return this.maxPendingMessages;
	}

	@Override
	public Integer getMaxPendingMessagesAcrossPartitions() {
		return this.maxPendingMessagesAcrossPartitions;
	}

	@Override
	public MessageRoutingMode getMessageRoutingMode() {
		return this.messageRoutingMode;
	}

	@Override
	public HashingScheme getHashingScheme() {
		return this.hashingScheme;
	}

	@Override
	public ProducerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	@Override
	public MessageRouter getMessageRouter() {
		return this.messageRouter;
	}

	@Override
	public Duration getBatchingMaxPublishDelay() {
		return this.batchingMaxPublishDelay;
	}

	@Override
	public Integer getRoundRobinRouterBatchingPartitionSwitchFrequency() {
		return this.roundRobinRouterBatchingPartitionSwitchFrequency;
	}

	@Override
	public Integer getBatchingMaxMessages() {
		return this.batchingMaxMessages;
	}

	@Override
	public Integer getBatchingMaxBytes() {
		return this.batchingMaxBytes;
	}

	@Override
	public Boolean getBatchingEnabled() {
		return this.batchingEnabled;
	}

	@Override
	public BatcherBuilder getBatcherBuilder() {
		return this.batcherBuilder;
	}

	@Override
	public Boolean getChunkingEnabled() {
		return this.chunkingEnabled;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	@Override
	public Set<String> getEncryptionKeys() {
		return this.encryptionKeys;
	}

	@Override
	public CompressionType getCompressionType() {
		return this.compressionType;
	}

	@Override
	public Long getInitialSequenceId() {
		return this.initialSequenceId;
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
	public Boolean getMultiSchema() {
		return this.multiSchema;
	}

	@Override
	public ProducerAccessMode getAccessMode() {
		return this.accessMode;
	}

	@Override
	public Boolean getLazyStartPartitionedProducers() {
		return this.lazyStartPartitionedProducers;
	}

	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

}
