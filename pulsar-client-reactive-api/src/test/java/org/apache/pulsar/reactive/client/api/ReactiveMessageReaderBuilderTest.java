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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.EncryptionKeyInfo;
import org.apache.pulsar.client.api.Range;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ReactiveMessageReaderBuilder},
 * {@link MutableReactiveMessageReaderSpec} and
 * {@link ImmutableReactiveMessageReaderSpec}.
 */
class ReactiveMessageReaderBuilderTest {

	private static final CryptoKeyReader cryptoKeyReader = new TestCryptoKeyReader();

	@Test
	void emptyBuilder() {
		MutableReactiveMessageReaderSpec spec = new TestReactiveMessageReaderBuilder().getMutableSpec();
		assertThat(spec).hasAllNullFieldsOrPropertiesExcept("topicNames");
		assertThat(spec.getTopicNames()).isEmpty();
	}

	@Test
	void getMutableSpec() {
		assertReaderSpecWithAllValues(createReaderSpec());
	}

	@Test
	void mutableSpecFromReactiveMessageReaderSpec() {
		ReactiveMessageReaderSpec spec = new MutableReactiveMessageReaderSpec(createReaderSpec());
		assertReaderSpecWithAllValues(spec);
	}

	@Test
	void immutableSpecFromReactiveMessageReaderSpec() {
		ReactiveMessageReaderSpec spec = new ImmutableReactiveMessageReaderSpec(createReaderSpec());
		assertReaderSpecWithAllValues(spec);
	}

	@Test
	void toImmutableSpec() {
		ReactiveMessageReaderSpec spec = createReaderBuilder().toImmutableSpec();
		assertReaderSpecWithAllValues(spec);
	}

	@Test
	void applySpec() {
		ReactiveMessageReaderSpec spec = new TestReactiveMessageReaderBuilder().applySpec(createReaderSpec())
			.getMutableSpec();
		assertReaderSpecWithAllValues(spec);
	}

	@Test
	void topics() {
		ArrayList<String> topics = new ArrayList<>();
		topics.add("topic-1");
		ReactiveMessageReaderSpec spec = new TestReactiveMessageReaderBuilder().topic("ignored-1")
			.topic("ignored-2", "ignored-3")
			.topics(topics)
			.topic("topic-2")
			.topic("topic-3", "topic-4")
			.toImmutableSpec();
		assertThat(spec.getTopicNames()).containsExactly("topic-1", "topic-2", "topic-3", "topic-4");
	}

	private void assertReaderSpecWithAllValues(ReactiveMessageReaderSpec spec) {
		assertThat(spec.getTopicNames()).containsExactly("my-topic");
		assertThat(spec.getReaderName()).isEqualTo("my-reader");
		assertThat(spec.getSubscriptionName()).isEqualTo("my-sub");
		assertThat(spec.getGeneratedSubscriptionNamePrefix()).isEqualTo("my-prefix-");
		assertThat(spec.getReceiverQueueSize()).isEqualTo(1);
		assertThat(spec.getReadCompacted()).isTrue();
		assertThat(spec.getCryptoKeyReader()).isSameAs(cryptoKeyReader);
		assertThat(spec.getKeyHashRanges()).containsExactly(new Range(2, 3));
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ConsumerCryptoFailureAction.FAIL);
	}

	private ReactiveMessageReaderSpec createReaderSpec() {
		return createReaderBuilder().getMutableSpec();
	}

	private ReactiveMessageReaderBuilder<String> createReaderBuilder() {
		return new TestReactiveMessageReaderBuilder().topic("my-topic")
			.readerName("my-reader")
			.subscriptionName("my-sub")
			.generatedSubscriptionNamePrefix("my-prefix-")
			.receiverQueueSize(1)
			.readCompacted(true)
			.cryptoKeyReader(cryptoKeyReader)
			.keyHashRanges(Collections.singletonList(new Range(2, 3)))
			.cryptoFailureAction(ConsumerCryptoFailureAction.FAIL);
	}

	static class TestReactiveMessageReaderBuilder implements ReactiveMessageReaderBuilder<String> {

		MutableReactiveMessageReaderSpec consumerSpec = new MutableReactiveMessageReaderSpec();

		@Override
		public ReactiveMessageReaderBuilder<String> startAtSpec(StartAtSpec startAtSpec) {
			return null;
		}

		@Override
		public ReactiveMessageReaderBuilder<String> endOfStreamAction(EndOfStreamAction endOfStreamAction) {
			return null;
		}

		@Override
		public MutableReactiveMessageReaderSpec getMutableSpec() {
			return this.consumerSpec;
		}

		@Override
		public ReactiveMessageReaderBuilder<String> clone() {
			return null;
		}

		@Override
		public ReactiveMessageReader<String> build() {
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
