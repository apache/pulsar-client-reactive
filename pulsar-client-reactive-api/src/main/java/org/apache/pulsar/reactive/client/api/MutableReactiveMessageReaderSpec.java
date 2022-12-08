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
import java.util.List;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Range;

/**
 * Mutable spec for a {@link ReactiveMessageReader}.
 */
public class MutableReactiveMessageReaderSpec implements ReactiveMessageReaderSpec {

	private List<String> topicNames = new ArrayList<>();

	private String readerName;

	private String subscriptionName;

	private String generatedSubscriptionNamePrefix;

	private Integer receiverQueueSize;

	private Boolean readCompacted;

	private List<Range> keyHashRanges;

	private CryptoKeyReader cryptoKeyReader;

	private ConsumerCryptoFailureAction cryptoFailureAction;

	/**
	 * Constructs a default MutableReactiveMessageReaderSpec.
	 */
	public MutableReactiveMessageReaderSpec() {

	}

	/**
	 * Constructs a MutableReactiveMessageReaderSpec from another
	 * {@link ReactiveMessageReaderSpec}.
	 * @param readerSpec the spec to construct from
	 */
	public MutableReactiveMessageReaderSpec(ReactiveMessageReaderSpec readerSpec) {
		this.topicNames = (readerSpec.getTopicNames() != null && !readerSpec.getTopicNames().isEmpty())
				? new ArrayList<>(readerSpec.getTopicNames()) : new ArrayList<>();
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

	/**
	 * Sets the topics to read from.
	 * @param topicNames the topic names
	 */
	public void setTopicNames(List<String> topicNames) {
		this.topicNames = topicNames;
	}

	@Override
	public String getReaderName() {
		return this.readerName;
	}

	/**
	 * Sets the reader name.
	 * @param readerName the reader name
	 */
	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}

	@Override
	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	/**
	 * Sets the subscription name.
	 * @param subscriptionName the name of the subscription
	 */
	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	@Override
	public String getGeneratedSubscriptionNamePrefix() {
		return this.generatedSubscriptionNamePrefix;
	}

	/**
	 * Sets the generated subscription name prefix.
	 * @param generatedSubscriptionNamePrefix the generated subscription name prefix
	 */
	public void setGeneratedSubscriptionNamePrefix(String generatedSubscriptionNamePrefix) {
		this.generatedSubscriptionNamePrefix = generatedSubscriptionNamePrefix;
	}

	@Override
	public Integer getReceiverQueueSize() {
		return this.receiverQueueSize;
	}

	/**
	 * Sets the size of the reader receive queue.
	 * @param receiverQueueSize the size of the reader receive queue
	 */
	public void setReceiverQueueSize(Integer receiverQueueSize) {
		this.receiverQueueSize = receiverQueueSize;
	}

	@Override
	public Boolean getReadCompacted() {
		return this.readCompacted;
	}

	/**
	 * Sets whether to read messages from the compacted topic rather than reading the full
	 * message backlog of the topic.
	 * @param readCompacted true to read messages from the compacted topic
	 */
	public void setReadCompacted(Boolean readCompacted) {
		this.readCompacted = readCompacted;
	}

	@Override
	public List<Range> getKeyHashRanges() {
		return this.keyHashRanges;
	}

	/**
	 * Sets the key hash ranges of the reader.
	 * @param keyHashRanges the key hash ranges
	 */
	public void setKeyHashRanges(List<Range> keyHashRanges) {
		this.keyHashRanges = keyHashRanges;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	/**
	 * Sets the key reader to be used to decrypt the message payloads.
	 * @param cryptoKeyReader the key reader to be used to decrypt the message payloads
	 */
	public void setCryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		this.cryptoKeyReader = cryptoKeyReader;
	}

	@Override
	public ConsumerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	/**
	 * Sets the action the reader will take in case of decryption failures.
	 * @param cryptoFailureAction the action the reader will take in case of decryption
	 * failures
	 */
	public void setCryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		this.cryptoFailureAction = cryptoFailureAction;
	}

	/**
	 * Updates this spec from the defined values of another spec.
	 * @param readerSpec the spec from which to update
	 */
	public void applySpec(ReactiveMessageReaderSpec readerSpec) {
		if (readerSpec.getTopicNames() != null && !readerSpec.getTopicNames().isEmpty()) {
			setTopicNames(new ArrayList<>(readerSpec.getTopicNames()));
		}
		if (readerSpec.getReaderName() != null) {
			setReaderName(readerSpec.getReaderName());
		}
		if (readerSpec.getSubscriptionName() != null) {
			setSubscriptionName(readerSpec.getSubscriptionName());
		}
		if (readerSpec.getGeneratedSubscriptionNamePrefix() != null) {
			setGeneratedSubscriptionNamePrefix(readerSpec.getGeneratedSubscriptionNamePrefix());
		}
		if (readerSpec.getReceiverQueueSize() != null) {
			setReceiverQueueSize(readerSpec.getReceiverQueueSize());
		}
		if (readerSpec.getReadCompacted() != null) {
			setReadCompacted(readerSpec.getReadCompacted());
		}
		if (readerSpec.getKeyHashRanges() != null && !readerSpec.getKeyHashRanges().isEmpty()) {
			setKeyHashRanges(new ArrayList<>(readerSpec.getKeyHashRanges()));
		}
		if (readerSpec.getCryptoKeyReader() != null) {
			setCryptoKeyReader(readerSpec.getCryptoKeyReader());
		}
		if (readerSpec.getCryptoFailureAction() != null) {
			setCryptoFailureAction(readerSpec.getCryptoFailureAction());
		}
	}

}
