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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.EncryptionKeyInfo;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveMessageSenderBuilder},
 * {@link MutableReactiveMessageSenderSpec} and
 * {@link ImmutableReactiveMessageSenderSpec}.
 */
class ReactiveMessageSenderBuilderTest {

	private static final MessageRouter messageRouter = new MessageRouter() {
	};

	private static final BatcherBuilder batcherBuilder = () -> null;

	private static final CryptoKeyReader cryptoKeyReader = new TestCryptoKeyReader();

	@Test
	void emptyBuilder() {
		MutableReactiveMessageSenderSpec spec = new TestReactiveMessageSenderBuilder().getMutableSpec();
		assertThat(spec).hasAllNullFieldsOrProperties();
	}

	@Test
	void getMutableSpec() {
		assertSenderSpecWithAllValues(createSenderSpec());
	}

	@Test
	void mutableSpecFromReactiveMessageSenderSpec() {
		ReactiveMessageSenderSpec spec = new MutableReactiveMessageSenderSpec(createSenderSpec());
		assertSenderSpecWithAllValues(spec);
	}

	@Test
	void immutableSpecFromReactiveMessageSenderSpec() {
		ReactiveMessageSenderSpec spec = new ImmutableReactiveMessageSenderSpec(createSenderSpec());
		assertSenderSpecWithAllValues(spec);
	}

	@Test
	void toImmutableSpec() {
		ReactiveMessageSenderSpec spec = createSenderBuilder().toImmutableSpec();
		assertSenderSpecWithAllValues(spec);
	}

	@Test
	void applySpec() {
		ReactiveMessageSenderSpec spec = new TestReactiveMessageSenderBuilder().applySpec(createSenderSpec())
				.getMutableSpec();
		assertSenderSpecWithAllValues(spec);
	}

	@Test
	void properties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("key-1", "value-1");
		ReactiveMessageSenderSpec spec = new TestReactiveMessageSenderBuilder()
				.property("key-ignored", "property-ignored").properties(properties).property("key-2", "value-2")
				.toImmutableSpec();
		assertThat(spec.getProperties()).hasSize(2).containsEntry("key-1", "value-1").containsEntry("key-2", "value-2");
	}

	private void assertSenderSpecWithAllValues(ReactiveMessageSenderSpec spec) {
		assertThat(spec.getTopicName()).isEqualTo("my-topic");
		assertThat(spec.getProducerName()).isEqualTo("my-producer");
		assertThat(spec.getSendTimeout()).hasSeconds(1);
		assertThat(spec.getMaxPendingMessages()).isEqualTo(2);
		assertThat(spec.getMaxPendingMessagesAcrossPartitions()).isEqualTo(3);
		assertThat(spec.getMessageRoutingMode()).isEqualTo(MessageRoutingMode.SinglePartition);
		assertThat(spec.getHashingScheme()).isEqualTo(HashingScheme.JavaStringHash);
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ProducerCryptoFailureAction.FAIL);
		assertThat(spec.getMessageRouter()).isSameAs(messageRouter);
		assertThat(spec.getBatchingMaxPublishDelay()).hasSeconds(4);
		assertThat(spec.getRoundRobinRouterBatchingPartitionSwitchFrequency()).isEqualTo(5);
		assertThat(spec.getBatchingMaxMessages()).isEqualTo(6);
		assertThat(spec.getBatchingMaxBytes()).isEqualTo(7);
		assertThat(spec.getBatchingEnabled()).isTrue();
		assertThat(spec.getBatcherBuilder()).isSameAs(batcherBuilder);
		assertThat(spec.getChunkingEnabled()).isTrue();
		assertThat(spec.getCryptoKeyReader()).isSameAs(cryptoKeyReader);
		assertThat(spec.getEncryptionKeys()).containsExactly("my-key");
		assertThat(spec.getCompressionType()).isEqualTo(CompressionType.LZ4);
		assertThat(spec.getInitialSequenceId()).isEqualTo(8);
		assertThat(spec.getAutoUpdatePartitions()).isTrue();
		assertThat(spec.getAutoUpdatePartitionsInterval()).hasSeconds(9);
		assertThat(spec.getMultiSchema()).isTrue();
		assertThat(spec.getAccessMode()).isEqualTo(ProducerAccessMode.Shared);
		assertThat(spec.getLazyStartPartitionedProducers()).isTrue();
		assertThat(spec.getProperties()).containsEntry("my-key", "my-value");
	}

	private ReactiveMessageSenderSpec createSenderSpec() {
		return createSenderBuilder().getMutableSpec();
	}

	private ReactiveMessageSenderBuilder<String> createSenderBuilder() {
		return new TestReactiveMessageSenderBuilder().topic("my-topic").producerName("my-producer")
				.sendTimeout(Duration.ofSeconds(1)).maxPendingMessages(2).maxPendingMessagesAcrossPartitions(3)
				.messageRoutingMode(MessageRoutingMode.SinglePartition).hashingScheme(HashingScheme.JavaStringHash)
				.cryptoFailureAction(ProducerCryptoFailureAction.FAIL).messageRouter(messageRouter)
				.batchingMaxPublishDelay(Duration.ofSeconds(4)).roundRobinRouterBatchingPartitionSwitchFrequency(5)
				.batchingMaxMessages(6).batchingMaxBytes(7).batchingEnabled(true).batcherBuilder(batcherBuilder)
				.chunkingEnabled(true).cryptoKeyReader(cryptoKeyReader).encryptionKeys(Collections.singleton("my-key"))
				.compressionType(CompressionType.LZ4).initialSequenceId(8).autoUpdatePartitions(true)
				.autoUpdatePartitionsInterval(Duration.ofSeconds(9)).multiSchema(true)
				.accessMode(ProducerAccessMode.Shared).lazyStartPartitionedProducers(true)
				.property("my-key", "my-value");
	}

	static class TestReactiveMessageSenderBuilder implements ReactiveMessageSenderBuilder<String> {

		MutableReactiveMessageSenderSpec consumerSpec = new MutableReactiveMessageSenderSpec();

		@Override
		public MutableReactiveMessageSenderSpec getMutableSpec() {
			return this.consumerSpec;
		}

		@Override
		public ReactiveMessageSenderBuilder<String> cache(ReactiveMessageSenderCache producerCache) {
			return null;
		}

		@Override
		public ReactiveMessageSenderBuilder<String> maxInflight(int maxInflight) {
			return null;
		}

		@Override
		public ReactiveMessageSenderBuilder<String> maxConcurrentSenderSubscriptions(
				int maxConcurrentSenderSubscriptions) {
			return null;
		}

		@Override
		public ReactiveMessageSenderBuilder<String> clone() {
			return null;
		}

		@Override
		public ReactiveMessageSender<String> build() {
			return null;
		}

	}

	static class TestCryptoKeyReader implements CryptoKeyReader {

		@Override
		public EncryptionKeyInfo getPublicKey(String keyName, Map<String, String> metadata) {
			return null;
		}

		@Override
		public EncryptionKeyInfo getPrivateKey(String keyName, Map<String, String> metadata) {
			return null;
		}

	}

}
