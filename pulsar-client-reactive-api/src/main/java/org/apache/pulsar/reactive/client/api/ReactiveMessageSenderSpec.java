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
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;

public interface ReactiveMessageSenderSpec {

	String getTopicName();

	String getProducerName();

	Duration getSendTimeout();

	Integer getMaxPendingMessages();

	Integer getMaxPendingMessagesAcrossPartitions();

	MessageRoutingMode getMessageRoutingMode();

	HashingScheme getHashingScheme();

	ProducerCryptoFailureAction getCryptoFailureAction();

	MessageRouter getMessageRouter();

	Duration getBatchingMaxPublishDelay();

	Integer getRoundRobinRouterBatchingPartitionSwitchFrequency();

	Integer getBatchingMaxMessages();

	Integer getBatchingMaxBytes();

	Boolean getBatchingEnabled();

	BatcherBuilder getBatcherBuilder();

	Boolean getChunkingEnabled();

	CryptoKeyReader getCryptoKeyReader();

	Set<String> getEncryptionKeys();

	CompressionType getCompressionType();

	Long getInitialSequenceId();

	Boolean getAutoUpdatePartitions();

	Duration getAutoUpdatePartitionsInterval();

	Boolean getMultiSchema();

	ProducerAccessMode getAccessMode();

	Boolean getLazyStartPartitionedProducers();

	Map<String, String> getProperties();

}
