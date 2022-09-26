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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Range;

public class ImmutableReactiveMessageReaderSpec implements ReactiveMessageReaderSpec {

	private final List<String> topicNames;

	private final String readerName;

	private final String subscriptionName;

	private final String generatedSubscriptionNamePrefix;

	private final Integer receiverQueueSize;

	private final Boolean readCompacted;

	private final List<Range> keyHashRanges;

	private final CryptoKeyReader cryptoKeyReader;

	private final ConsumerCryptoFailureAction cryptoFailureAction;

	public ImmutableReactiveMessageReaderSpec(List<String> topicNames, String readerName, String subscriptionName,
			String generatedSubscriptionNamePrefix, Integer receiverQueueSize, Boolean readCompacted,
			List<Range> keyHashRanges, CryptoKeyReader cryptoKeyReader,
			ConsumerCryptoFailureAction cryptoFailureAction) {
		this.topicNames = topicNames;
		this.readerName = readerName;
		this.subscriptionName = subscriptionName;
		this.generatedSubscriptionNamePrefix = generatedSubscriptionNamePrefix;
		this.receiverQueueSize = receiverQueueSize;
		this.readCompacted = readCompacted;
		this.keyHashRanges = keyHashRanges;
		this.cryptoKeyReader = cryptoKeyReader;
		this.cryptoFailureAction = cryptoFailureAction;
	}

	public ImmutableReactiveMessageReaderSpec(ReactiveMessageReaderSpec readerSpec) {
		this.topicNames = (readerSpec.getTopicNames() != null && !readerSpec.getTopicNames().isEmpty())
				? Collections.unmodifiableList(new ArrayList<>(readerSpec.getTopicNames())) : null;
		this.readerName = readerSpec.getReaderName();
		this.subscriptionName = readerSpec.getSubscriptionName();
		this.generatedSubscriptionNamePrefix = readerSpec.getGeneratedSubscriptionNamePrefix();
		this.receiverQueueSize = readerSpec.getReceiverQueueSize();
		this.readCompacted = readerSpec.getReadCompacted();
		this.keyHashRanges = readerSpec.getKeyHashRanges();
		this.cryptoKeyReader = readerSpec.getCryptoKeyReader();
		this.cryptoFailureAction = readerSpec.getCryptoFailureAction();
	}

	@Override
	public List<String> getTopicNames() {
		return this.topicNames;
	}

	@Override
	public String getReaderName() {
		return this.readerName;
	}

	@Override
	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	@Override
	public String getGeneratedSubscriptionNamePrefix() {
		return this.generatedSubscriptionNamePrefix;
	}

	@Override
	public Integer getReceiverQueueSize() {
		return this.receiverQueueSize;
	}

	@Override
	public Boolean getReadCompacted() {
		return this.readCompacted;
	}

	@Override
	public List<Range> getKeyHashRanges() {
		return this.keyHashRanges;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	@Override
	public ConsumerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

}
