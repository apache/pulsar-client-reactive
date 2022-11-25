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

public interface ReactiveMessageSenderBuilder<T> {

	ReactiveMessageSenderBuilder<T> cache(ReactiveMessageSenderCache producerCache);

	ReactiveMessageSenderBuilder<T> maxInflight(int maxInflight);

	ReactiveMessageSenderBuilder<T> maxConcurrentSenderSubscriptions(int maxConcurrentSenderSubscriptions);

	default ReactiveMessageSenderBuilder<T> applySpec(ReactiveMessageSenderSpec senderSpec) {
		getMutableSpec().applySpec(senderSpec);
		return this;
	}

	default ReactiveMessageSenderSpec toImmutableSpec() {
		return new ImmutableReactiveMessageSenderSpec(getMutableSpec());
	}

	MutableReactiveMessageSenderSpec getMutableSpec();

	default ReactiveMessageSenderBuilder<T> topic(String topicName) {
		getMutableSpec().setTopicName(topicName);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> producerName(String producerName) {
		getMutableSpec().setProducerName(producerName);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> sendTimeout(Duration sendTimeout) {
		getMutableSpec().setSendTimeout(sendTimeout);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> maxPendingMessages(int maxPendingMessages) {
		getMutableSpec().setMaxPendingMessages(maxPendingMessages);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> maxPendingMessagesAcrossPartitions(int maxPendingMessagesAcrossPartitions) {
		getMutableSpec().setMaxPendingMessagesAcrossPartitions(maxPendingMessagesAcrossPartitions);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> messageRoutingMode(MessageRoutingMode messageRoutingMode) {
		getMutableSpec().setMessageRoutingMode(messageRoutingMode);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> hashingScheme(HashingScheme hashingScheme) {
		getMutableSpec().setHashingScheme(hashingScheme);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> cryptoFailureAction(ProducerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> messageRouter(MessageRouter messageRouter) {
		getMutableSpec().setMessageRouter(messageRouter);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> batchingMaxPublishDelay(Duration batchingMaxPublishDelay) {
		getMutableSpec().setBatchingMaxPublishDelay(batchingMaxPublishDelay);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> roundRobinRouterBatchingPartitionSwitchFrequency(
			int roundRobinRouterBatchingPartitionSwitchFrequency) {
		getMutableSpec()
				.setRoundRobinRouterBatchingPartitionSwitchFrequency(roundRobinRouterBatchingPartitionSwitchFrequency);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> batchingMaxMessages(int batchingMaxMessages) {
		getMutableSpec().setBatchingMaxMessages(batchingMaxMessages);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> batchingMaxBytes(int batchingMaxBytes) {
		getMutableSpec().setBatchingMaxBytes(batchingMaxBytes);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> batchingEnabled(boolean batchingEnabled) {
		getMutableSpec().setBatchingEnabled(batchingEnabled);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> batcherBuilder(BatcherBuilder batcherBuilder) {
		getMutableSpec().setBatcherBuilder(batcherBuilder);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> chunkingEnabled(boolean chunkingEnabled) {
		getMutableSpec().setChunkingEnabled(chunkingEnabled);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> encryptionKeys(Set<String> encryptionKeys) {
		getMutableSpec().setEncryptionKeys(encryptionKeys);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> compressionType(CompressionType compressionType) {
		getMutableSpec().setCompressionType(compressionType);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> initialSequenceId(long initialSequenceId) {
		getMutableSpec().setInitialSequenceId(initialSequenceId);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> autoUpdatePartitions(boolean autoUpdatePartitions) {
		getMutableSpec().setAutoUpdatePartitions(autoUpdatePartitions);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> autoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		getMutableSpec().setAutoUpdatePartitionsInterval(autoUpdatePartitionsInterval);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> multiSchema(boolean multiSchema) {
		getMutableSpec().setMultiSchema(multiSchema);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> accessMode(ProducerAccessMode accessMode) {
		getMutableSpec().setAccessMode(accessMode);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> lazyStartPartitionedProducers(boolean lazyStartPartitionedProducers) {
		getMutableSpec().setLazyStartPartitionedProducers(lazyStartPartitionedProducers);
		return this;
	}

	default ReactiveMessageSenderBuilder<T> properties(Map<String, String> properties) {
		getMutableSpec().setProperties(properties);
		return this;
	}

	ReactiveMessageSender<T> build();

}
