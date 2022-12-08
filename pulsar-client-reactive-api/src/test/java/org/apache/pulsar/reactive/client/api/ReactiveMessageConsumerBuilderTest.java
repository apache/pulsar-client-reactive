/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.EncryptionKeyInfo;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveMessageConsumerBuilder},
 * {@link MutableReactiveMessageConsumerSpec} and
 * {@link ImmutableReactiveMessageConsumerSpec}.
 */
class ReactiveMessageConsumerBuilderTest {

	private static final KeySharedPolicy keySharedPolicy = KeySharedPolicy.autoSplitHashRange();

	private static final CryptoKeyReader cryptoKeyReader = new TestCryptoKeyReader();

	private static final DeadLetterPolicy deadLetterPolicy = DeadLetterPolicy.builder().build();

	private static final Scheduler scheduler = Schedulers.newSingle("my-sched");

	@Test
	void emptyBuilder() {
		MutableReactiveMessageConsumerSpec spec = new TestReactiveMessageConsumerBuilder().getMutableSpec();
		assertThat(spec).hasAllNullFieldsOrPropertiesExcept("topicNames");
		assertThat(spec.getTopicNames()).isEmpty();
	}

	@Test
	void getMutableSpec() {
		assertConsumerSpecWithAllValues(createConsumerSpec());
	}

	@Test
	void mutableSpecFromReactiveMessageConsumerSpec() {
		ReactiveMessageConsumerSpec spec = new MutableReactiveMessageConsumerSpec(createConsumerSpec());
		assertConsumerSpecWithAllValues(spec);
	}

	@Test
	void immutableSpecFromReactiveMessageConsumerSpec() {
		ReactiveMessageConsumerSpec spec = new ImmutableReactiveMessageConsumerSpec(createConsumerSpec());
		assertConsumerSpecWithAllValues(spec);
	}

	@Test
	void toImmutableSpec() {
		ReactiveMessageConsumerSpec spec = createConsumerBuilder().toImmutableSpec();
		assertConsumerSpecWithAllValues(spec);
	}

	@Test
	void applySpec() {
		ReactiveMessageConsumerSpec spec = new TestReactiveMessageConsumerBuilder().applySpec(createConsumerSpec())
				.getMutableSpec();
		assertConsumerSpecWithAllValues(spec);
	}

	@Test
	void topics() {
		ArrayList<String> topics = new ArrayList<>();
		topics.add("topic-1");
		ReactiveMessageConsumerSpec spec = new TestReactiveMessageConsumerBuilder().topic("ignored-1")
				.topic("ignored-2", "ignored-3").topics(topics).topic("topic-2").topic("topic-3", "topic-4")
				.toImmutableSpec();
		assertThat(spec.getTopicNames()).containsExactly("topic-1", "topic-2", "topic-3", "topic-4");
	}

	@Test
	void properties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("key-1", "value-1");
		ReactiveMessageConsumerSpec spec = new TestReactiveMessageConsumerBuilder()
				.property("key-ignored", "property-ignored").properties(properties).property("key-2", "value-2")
				.toImmutableSpec();
		assertThat(spec.getProperties()).hasSize(2).containsEntry("key-1", "value-1").containsEntry("key-2", "value-2");
	}

	@Test
	void subscriptionProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("key-1", "value-1");
		ReactiveMessageConsumerSpec spec = new TestReactiveMessageConsumerBuilder()
				.subscriptionProperty("key-ignored", "property-ignored").subscriptionProperties(properties)
				.subscriptionProperty("key-2", "value-2").toImmutableSpec();
		assertThat(spec.getSubscriptionProperties()).hasSize(2).containsEntry("key-1", "value-1").containsEntry("key-2",
				"value-2");
	}

	private void assertConsumerSpecWithAllValues(ReactiveMessageConsumerSpec spec) {
		assertThat(spec.getTopicNames()).containsExactly("my-topic");
		assertThat(spec.getTopicsPattern().pattern()).isEqualTo("my-topic-*");
		assertThat(spec.getTopicsPatternSubscriptionMode()).isEqualTo(RegexSubscriptionMode.PersistentOnly);
		assertThat(spec.getTopicsPatternAutoDiscoveryPeriod()).hasSeconds(1);
		assertThat(spec.getSubscriptionName()).isEqualTo("my-sub");
		assertThat(spec.getSubscriptionMode()).isEqualTo(SubscriptionMode.Durable);
		assertThat(spec.getSubscriptionType()).isEqualTo(SubscriptionType.Exclusive);
		assertThat(spec.getSubscriptionInitialPosition()).isEqualTo(SubscriptionInitialPosition.Latest);
		assertThat(spec.getKeySharedPolicy()).isSameAs(keySharedPolicy);
		assertThat(spec.getReplicateSubscriptionState()).isTrue();
		assertThat(spec.getSubscriptionProperties()).hasSize(1).containsEntry("my-sub-key", "my-sub-value");
		assertThat(spec.getConsumerName()).isEqualTo("my-consumer");
		assertThat(spec.getProperties()).hasSize(1).containsEntry("my-key", "my-value");
		assertThat(spec.getPriorityLevel()).isEqualTo(2);
		assertThat(spec.getReadCompacted()).isTrue();
		assertThat(spec.getBatchIndexAckEnabled()).isTrue();
		assertThat(spec.getAckTimeout()).hasSeconds(3);
		assertThat(spec.getAckTimeoutTickTime()).hasSeconds(4);
		assertThat(spec.getAcknowledgementsGroupTime()).hasSeconds(5);
		assertThat(spec.getAcknowledgeAsynchronously()).isTrue();
		assertThat(spec.getAcknowledgeScheduler()).isSameAs(scheduler);
		assertThat(spec.getNegativeAckRedeliveryDelay()).hasSeconds(6);
		assertThat(spec.getDeadLetterPolicy()).isSameAs(deadLetterPolicy);
		assertThat(spec.getRetryLetterTopicEnable()).isTrue();
		assertThat(spec.getReceiverQueueSize()).isEqualTo(7);
		assertThat(spec.getMaxTotalReceiverQueueSizeAcrossPartitions()).isEqualTo(8);
		assertThat(spec.getAutoUpdatePartitions()).isTrue();
		assertThat(spec.getAutoUpdatePartitionsInterval()).hasSeconds(9);
		assertThat(spec.getCryptoKeyReader()).isSameAs(cryptoKeyReader);
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ConsumerCryptoFailureAction.FAIL);
		assertThat(spec.getMaxPendingChunkedMessage()).isEqualTo(10);
		assertThat(spec.getAutoAckOldestChunkedMessageOnQueueFull()).isTrue();
		assertThat(spec.getExpireTimeOfIncompleteChunkedMessage()).hasSeconds(11);
	}

	private ReactiveMessageConsumerSpec createConsumerSpec() {
		return createConsumerBuilder().getMutableSpec();
	}

	private ReactiveMessageConsumerBuilder<String> createConsumerBuilder() {
		return new TestReactiveMessageConsumerBuilder().topic("my-topic").topicsPattern(Pattern.compile("my-topic-*"))
				.topicsPatternSubscriptionMode(RegexSubscriptionMode.PersistentOnly)
				.topicsPatternAutoDiscoveryPeriod(Duration.ofSeconds(1)).subscriptionName("my-sub")
				.subscriptionMode(SubscriptionMode.Durable).subscriptionType(SubscriptionType.Exclusive)
				.subscriptionInitialPosition(SubscriptionInitialPosition.Latest).keySharedPolicy(keySharedPolicy)
				.replicateSubscriptionState(true).subscriptionProperty("my-sub-key", "my-sub-value")
				.consumerName("my-consumer").priorityLevel(2).readCompacted(true).property("my-key", "my-value")
				.batchIndexAckEnabled(true).ackTimeout(Duration.ofSeconds(3)).ackTimeoutTickTime(Duration.ofSeconds(4))
				.acknowledgementsGroupTime(Duration.ofSeconds(5)).acknowledgeAsynchronously(true)
				.acknowledgeScheduler(scheduler).negativeAckRedeliveryDelay(Duration.ofSeconds(6))
				.deadLetterPolicy(deadLetterPolicy).retryLetterTopicEnable(true).receiverQueueSize(7)
				.maxTotalReceiverQueueSizeAcrossPartitions(8).autoUpdatePartitions(true)
				.autoUpdatePartitionsInterval(Duration.ofSeconds(9)).cryptoKeyReader(cryptoKeyReader)
				.cryptoFailureAction(ConsumerCryptoFailureAction.FAIL).maxPendingChunkedMessage(10)
				.autoAckOldestChunkedMessageOnQueueFull(true)
				.expireTimeOfIncompleteChunkedMessage(Duration.ofSeconds(11));
	}

	static class TestReactiveMessageConsumerBuilder implements ReactiveMessageConsumerBuilder<String> {

		MutableReactiveMessageConsumerSpec consumerSpec = new MutableReactiveMessageConsumerSpec();

		@Override
		public MutableReactiveMessageConsumerSpec getMutableSpec() {
			return this.consumerSpec;
		}

		@Override
		public ReactiveMessageConsumerBuilder<String> clone() {
			return null;
		}

		@Override
		public ReactiveMessageConsumer<String> build() {
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
