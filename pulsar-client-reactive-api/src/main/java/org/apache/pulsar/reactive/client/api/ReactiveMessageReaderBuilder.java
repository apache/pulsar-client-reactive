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

import java.util.List;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Range;

public interface ReactiveMessageReaderBuilder<T> {

	ReactiveMessageReaderBuilder<T> startAtSpec(StartAtSpec startAtSpec);

	ReactiveMessageReaderBuilder<T> endOfStreamAction(EndOfStreamAction endOfStreamAction);

	default ReactiveMessageReaderBuilder<T> applySpec(ReactiveMessageReaderSpec readerSpec) {
		getMutableSpec().applySpec(readerSpec);
		return this;
	}

	ReactiveMessageReaderSpec toImmutableSpec();

	MutableReactiveMessageReaderSpec getMutableSpec();

	ReactiveMessageReaderBuilder<T> clone();

	ReactiveMessageReader<T> build();

	default ReactiveMessageReaderBuilder<T> topic(String topicName) {
		getMutableSpec().getTopicNames().add(topicName);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> topic(String... topicNames) {
		for (String topicName : topicNames) {
			getMutableSpec().getTopicNames().add(topicName);
		}
		return this;
	}

	default ReactiveMessageReaderBuilder<T> topicNames(List<String> topicNames) {
		getMutableSpec().setTopicNames(topicNames);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> readerName(String readerName) {
		getMutableSpec().setReaderName(readerName);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> subscriptionName(String subscriptionName) {
		getMutableSpec().setSubscriptionName(subscriptionName);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> generatedSubscriptionNamePrefix(String generatedSubscriptionNamePrefix) {
		getMutableSpec().setGeneratedSubscriptionNamePrefix(generatedSubscriptionNamePrefix);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> receiverQueueSize(Integer receiverQueueSize) {
		getMutableSpec().setReceiverQueueSize(receiverQueueSize);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> readCompacted(Boolean readCompacted) {
		getMutableSpec().setReadCompacted(readCompacted);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> keyHashRanges(List<Range> keyHashRanges) {
		getMutableSpec().setKeyHashRanges(keyHashRanges);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	default ReactiveMessageReaderBuilder<T> cryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

}
