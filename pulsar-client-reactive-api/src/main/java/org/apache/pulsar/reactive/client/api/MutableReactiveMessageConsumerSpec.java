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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

public class MutableReactiveMessageConsumerSpec implements ReactiveMessageConsumerSpec {

	private List<String> topicNames = new ArrayList<>();

	private Pattern topicsPattern;

	private RegexSubscriptionMode topicsPatternSubscriptionMode;

	private Duration topicsPatternAutoDiscoveryPeriod;

	private String subscriptionName;

	private SubscriptionMode subscriptionMode;

	private SubscriptionType subscriptionType;

	private KeySharedPolicy keySharedPolicy;

	private Boolean replicateSubscriptionState;

	private Map<String, String> subscriptionProperties;

	private String consumerName;

	private Map<String, String> properties;

	private Integer priorityLevel;

	private Boolean readCompacted;

	private Boolean batchIndexAckEnabled;

	private Duration ackTimeout;

	private Duration ackTimeoutTickTime;

	private Duration acknowledgementsGroupTime;

	private Boolean acknowledgeAsynchronously;

	private Scheduler acknowledgeScheduler;

	private Duration negativeAckRedeliveryDelay;

	private DeadLetterPolicy deadLetterPolicy;

	private Boolean retryLetterTopicEnable;

	private Integer receiverQueueSize;

	private Integer maxTotalReceiverQueueSizeAcrossPartitions;

	private Boolean autoUpdatePartitions;

	private Duration autoUpdatePartitionsInterval;

	private CryptoKeyReader cryptoKeyReader;

	private ConsumerCryptoFailureAction cryptoFailureAction;

	private Integer maxPendingChunkedMessage;

	private Boolean autoAckOldestChunkedMessageOnQueueFull;

	private Duration expireTimeOfIncompleteChunkedMessage;

	public MutableReactiveMessageConsumerSpec() {

	}

	public MutableReactiveMessageConsumerSpec(ReactiveMessageConsumerSpec consumerSpec) {
		this.topicNames = (consumerSpec.getTopicNames() != null && !consumerSpec.getTopicNames().isEmpty())
				? new ArrayList<>(consumerSpec.getTopicNames()) : new ArrayList<>();

		this.topicsPattern = consumerSpec.getTopicsPattern();

		this.topicsPatternSubscriptionMode = consumerSpec.getTopicsPatternSubscriptionMode();

		this.topicsPatternAutoDiscoveryPeriod = consumerSpec.getTopicsPatternAutoDiscoveryPeriod();

		this.subscriptionName = consumerSpec.getSubscriptionName();

		this.subscriptionMode = consumerSpec.getSubscriptionMode();

		this.subscriptionType = consumerSpec.getSubscriptionType();

		this.keySharedPolicy = consumerSpec.getKeySharedPolicy();

		this.replicateSubscriptionState = consumerSpec.getReplicateSubscriptionState();

		this.subscriptionProperties = (consumerSpec.getSubscriptionProperties() != null
				&& !consumerSpec.getSubscriptionProperties().isEmpty())
						? new LinkedHashMap<>(consumerSpec.getSubscriptionProperties()) : null;

		this.consumerName = consumerSpec.getConsumerName();

		this.properties = (consumerSpec.getProperties() != null && !consumerSpec.getProperties().isEmpty())
				? new LinkedHashMap<>(consumerSpec.getProperties()) : null;

		this.priorityLevel = consumerSpec.getPriorityLevel();

		this.readCompacted = consumerSpec.getReadCompacted();

		this.batchIndexAckEnabled = consumerSpec.getBatchIndexAckEnabled();

		this.ackTimeout = consumerSpec.getAckTimeout();

		this.ackTimeoutTickTime = consumerSpec.getAckTimeoutTickTime();

		this.acknowledgementsGroupTime = consumerSpec.getAcknowledgementsGroupTime();

		this.acknowledgeAsynchronously = consumerSpec.getAcknowledgeAsynchronously();
		this.acknowledgeScheduler = consumerSpec.getAcknowledgeScheduler();
		this.negativeAckRedeliveryDelay = consumerSpec.getNegativeAckRedeliveryDelay();

		this.deadLetterPolicy = consumerSpec.getDeadLetterPolicy();

		this.retryLetterTopicEnable = consumerSpec.getRetryLetterTopicEnable();

		this.receiverQueueSize = consumerSpec.getReceiverQueueSize();

		this.maxTotalReceiverQueueSizeAcrossPartitions = consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions();

		this.autoUpdatePartitions = consumerSpec.getAutoUpdatePartitions();

		this.autoUpdatePartitionsInterval = consumerSpec.getAutoUpdatePartitionsInterval();

		this.cryptoKeyReader = consumerSpec.getCryptoKeyReader();

		this.cryptoFailureAction = consumerSpec.getCryptoFailureAction();

		this.maxPendingChunkedMessage = consumerSpec.getMaxPendingChunkedMessage();

		this.autoAckOldestChunkedMessageOnQueueFull = consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull();

		this.expireTimeOfIncompleteChunkedMessage = consumerSpec.getExpireTimeOfIncompleteChunkedMessage();
	}

	@Override
	public List<String> getTopicNames() {
		return this.topicNames;
	}

	public void setTopicNames(List<String> topicNames) {
		this.topicNames = topicNames;
	}

	@Override
	public Pattern getTopicsPattern() {
		return this.topicsPattern;
	}

	public void setTopicsPattern(Pattern topicsPattern) {
		this.topicsPattern = topicsPattern;
	}

	@Override
	public RegexSubscriptionMode getTopicsPatternSubscriptionMode() {
		return this.topicsPatternSubscriptionMode;
	}

	public void setTopicsPatternSubscriptionMode(RegexSubscriptionMode topicsPatternSubscriptionMode) {
		this.topicsPatternSubscriptionMode = topicsPatternSubscriptionMode;
	}

	@Override
	public Duration getTopicsPatternAutoDiscoveryPeriod() {
		return this.topicsPatternAutoDiscoveryPeriod;
	}

	public void setTopicsPatternAutoDiscoveryPeriod(Duration topicsPatternAutoDiscoveryPeriod) {
		this.topicsPatternAutoDiscoveryPeriod = topicsPatternAutoDiscoveryPeriod;
	}

	@Override
	public String getSubscriptionName() {
		return this.subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	@Override
	public SubscriptionMode getSubscriptionMode() {
		return this.subscriptionMode;
	}

	public void setSubscriptionMode(SubscriptionMode subscriptionMode) {
		this.subscriptionMode = subscriptionMode;
	}

	@Override
	public SubscriptionType getSubscriptionType() {
		return this.subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	@Override
	public KeySharedPolicy getKeySharedPolicy() {
		return this.keySharedPolicy;
	}

	public void setKeySharedPolicy(KeySharedPolicy keySharedPolicy) {
		this.keySharedPolicy = keySharedPolicy;
	}

	@Override
	public Boolean getReplicateSubscriptionState() {
		return this.replicateSubscriptionState;
	}

	public void setReplicateSubscriptionState(Boolean replicateSubscriptionState) {
		this.replicateSubscriptionState = replicateSubscriptionState;
	}

	@Override
	public Map<String, String> getSubscriptionProperties() {
		return this.subscriptionProperties;
	}

	public void setSubscriptionProperties(Map<String, String> subscriptionProperties) {
		this.subscriptionProperties = subscriptionProperties;
	}

	@Override
	public String getConsumerName() {
		return this.consumerName;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public Integer getPriorityLevel() {
		return this.priorityLevel;
	}

	public void setPriorityLevel(Integer priorityLevel) {
		this.priorityLevel = priorityLevel;
	}

	@Override
	public Boolean getReadCompacted() {
		return this.readCompacted;
	}

	public void setReadCompacted(Boolean readCompacted) {
		this.readCompacted = readCompacted;
	}

	@Override
	public Boolean getBatchIndexAckEnabled() {
		return this.batchIndexAckEnabled;
	}

	public void setBatchIndexAckEnabled(Boolean batchIndexAckEnabled) {
		this.batchIndexAckEnabled = batchIndexAckEnabled;
	}

	@Override
	public Duration getAckTimeout() {
		return this.ackTimeout;
	}

	public void setAckTimeout(Duration ackTimeout) {
		this.ackTimeout = ackTimeout;
	}

	@Override
	public Duration getAckTimeoutTickTime() {
		return this.ackTimeoutTickTime;
	}

	public void setAckTimeoutTickTime(Duration ackTimeoutTickTime) {
		this.ackTimeoutTickTime = ackTimeoutTickTime;
	}

	@Override
	public Duration getAcknowledgementsGroupTime() {
		return this.acknowledgementsGroupTime;
	}

	public void setAcknowledgementsGroupTime(Duration acknowledgementsGroupTime) {
		this.acknowledgementsGroupTime = acknowledgementsGroupTime;
	}

	@Override
	public Boolean getAcknowledgeAsynchronously() {
		return this.acknowledgeAsynchronously;
	}

	public void setAcknowledgeAsynchronously(Boolean acknowledgeAsynchronously) {
		this.acknowledgeAsynchronously = acknowledgeAsynchronously;
	}

	@Override
	public Scheduler getAcknowledgeScheduler() {
		return this.acknowledgeScheduler;
	}

	public void setAcknowledgeScheduler(Scheduler acknowledgeScheduler) {
		this.acknowledgeScheduler = acknowledgeScheduler;
	}

	@Override
	public Duration getNegativeAckRedeliveryDelay() {
		return this.negativeAckRedeliveryDelay;
	}

	public void setNegativeAckRedeliveryDelay(Duration negativeAckRedeliveryDelay) {
		this.negativeAckRedeliveryDelay = negativeAckRedeliveryDelay;
	}

	@Override
	public DeadLetterPolicy getDeadLetterPolicy() {
		return this.deadLetterPolicy;
	}

	public void setDeadLetterPolicy(DeadLetterPolicy deadLetterPolicy) {
		this.deadLetterPolicy = deadLetterPolicy;
	}

	@Override
	public Boolean getRetryLetterTopicEnable() {
		return this.retryLetterTopicEnable;
	}

	public void setRetryLetterTopicEnable(Boolean retryLetterTopicEnable) {
		this.retryLetterTopicEnable = retryLetterTopicEnable;
	}

	@Override
	public Integer getReceiverQueueSize() {
		return this.receiverQueueSize;
	}

	public void setReceiverQueueSize(Integer receiverQueueSize) {
		this.receiverQueueSize = receiverQueueSize;
	}

	@Override
	public Integer getMaxTotalReceiverQueueSizeAcrossPartitions() {
		return this.maxTotalReceiverQueueSizeAcrossPartitions;
	}

	public void setMaxTotalReceiverQueueSizeAcrossPartitions(Integer maxTotalReceiverQueueSizeAcrossPartitions) {
		this.maxTotalReceiverQueueSizeAcrossPartitions = maxTotalReceiverQueueSizeAcrossPartitions;
	}

	@Override
	public Boolean getAutoUpdatePartitions() {
		return this.autoUpdatePartitions;
	}

	public void setAutoUpdatePartitions(Boolean autoUpdatePartitions) {
		this.autoUpdatePartitions = autoUpdatePartitions;
	}

	@Override
	public Duration getAutoUpdatePartitionsInterval() {
		return this.autoUpdatePartitionsInterval;
	}

	public void setAutoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
	}

	@Override
	public CryptoKeyReader getCryptoKeyReader() {
		return this.cryptoKeyReader;
	}

	public void setCryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		this.cryptoKeyReader = cryptoKeyReader;
	}

	@Override
	public ConsumerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	public void setCryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		this.cryptoFailureAction = cryptoFailureAction;
	}

	@Override
	public Integer getMaxPendingChunkedMessage() {
		return this.maxPendingChunkedMessage;
	}

	public void setMaxPendingChunkedMessage(Integer maxPendingChunkedMessage) {
		this.maxPendingChunkedMessage = maxPendingChunkedMessage;
	}

	@Override
	public Boolean getAutoAckOldestChunkedMessageOnQueueFull() {
		return this.autoAckOldestChunkedMessageOnQueueFull;
	}

	public void setAutoAckOldestChunkedMessageOnQueueFull(Boolean autoAckOldestChunkedMessageOnQueueFull) {
		this.autoAckOldestChunkedMessageOnQueueFull = autoAckOldestChunkedMessageOnQueueFull;
	}

	@Override
	public Duration getExpireTimeOfIncompleteChunkedMessage() {
		return this.expireTimeOfIncompleteChunkedMessage;
	}

	public void setExpireTimeOfIncompleteChunkedMessage(Duration expireTimeOfIncompleteChunkedMessage) {
		this.expireTimeOfIncompleteChunkedMessage = expireTimeOfIncompleteChunkedMessage;
	}

	public void applySpec(ReactiveMessageConsumerSpec consumerSpec) {
		if (consumerSpec.getTopicNames() != null && !consumerSpec.getTopicNames().isEmpty()) {
			setTopicNames(new ArrayList<>(consumerSpec.getTopicNames()));
		}
		if (consumerSpec.getTopicsPattern() != null) {
			setTopicsPattern(consumerSpec.getTopicsPattern());
		}
		if (consumerSpec.getTopicsPatternSubscriptionMode() != null) {
			setTopicsPatternSubscriptionMode(consumerSpec.getTopicsPatternSubscriptionMode());
		}
		if (consumerSpec.getTopicsPatternAutoDiscoveryPeriod() != null) {
			setTopicsPatternAutoDiscoveryPeriod(consumerSpec.getTopicsPatternAutoDiscoveryPeriod());
		}
		if (consumerSpec.getSubscriptionName() != null) {
			setSubscriptionName(consumerSpec.getSubscriptionName());
		}
		if (consumerSpec.getSubscriptionMode() != null) {
			setSubscriptionMode(consumerSpec.getSubscriptionMode());
		}
		if (consumerSpec.getSubscriptionType() != null) {
			setSubscriptionType(consumerSpec.getSubscriptionType());
		}
		if (consumerSpec.getKeySharedPolicy() != null) {
			setKeySharedPolicy(consumerSpec.getKeySharedPolicy());
		}
		if (consumerSpec.getReplicateSubscriptionState() != null) {
			setReplicateSubscriptionState(consumerSpec.getReplicateSubscriptionState());
		}
		if (consumerSpec.getSubscriptionProperties() != null && !consumerSpec.getSubscriptionProperties().isEmpty()) {
			setSubscriptionProperties(new LinkedHashMap<>(consumerSpec.getSubscriptionProperties()));
		}
		if (consumerSpec.getConsumerName() != null) {
			setConsumerName(consumerSpec.getConsumerName());
		}
		if (consumerSpec.getProperties() != null && !consumerSpec.getProperties().isEmpty()) {
			setProperties(new LinkedHashMap<>(consumerSpec.getProperties()));
		}
		if (consumerSpec.getPriorityLevel() != null) {
			setPriorityLevel(consumerSpec.getPriorityLevel());
		}
		if (consumerSpec.getReadCompacted() != null) {
			setReadCompacted(consumerSpec.getReadCompacted());
		}
		if (consumerSpec.getBatchIndexAckEnabled() != null) {
			setBatchIndexAckEnabled(consumerSpec.getBatchIndexAckEnabled());
		}
		if (consumerSpec.getAckTimeout() != null) {
			setAckTimeout(consumerSpec.getAckTimeout());
		}
		if (consumerSpec.getAckTimeoutTickTime() != null) {
			setAckTimeoutTickTime(consumerSpec.getAckTimeoutTickTime());
		}
		if (consumerSpec.getAcknowledgementsGroupTime() != null) {
			setAcknowledgementsGroupTime(consumerSpec.getAcknowledgementsGroupTime());
		}
		if (consumerSpec.getNegativeAckRedeliveryDelay() != null) {
			setNegativeAckRedeliveryDelay(consumerSpec.getNegativeAckRedeliveryDelay());
		}
		if (consumerSpec.getDeadLetterPolicy() != null) {
			setDeadLetterPolicy(consumerSpec.getDeadLetterPolicy());
		}
		if (consumerSpec.getRetryLetterTopicEnable() != null) {
			setRetryLetterTopicEnable(consumerSpec.getRetryLetterTopicEnable());
		}
		if (consumerSpec.getReceiverQueueSize() != null) {
			setReceiverQueueSize(consumerSpec.getReceiverQueueSize());
		}
		if (consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions() != null) {
			setMaxTotalReceiverQueueSizeAcrossPartitions(consumerSpec.getMaxTotalReceiverQueueSizeAcrossPartitions());
		}
		if (consumerSpec.getAutoUpdatePartitions() != null) {
			setAutoUpdatePartitions(consumerSpec.getAutoUpdatePartitions());
		}
		if (consumerSpec.getAutoUpdatePartitionsInterval() != null) {
			setAutoUpdatePartitionsInterval(consumerSpec.getAutoUpdatePartitionsInterval());
		}
		if (consumerSpec.getCryptoKeyReader() != null) {
			setCryptoKeyReader(consumerSpec.getCryptoKeyReader());
		}
		if (consumerSpec.getCryptoFailureAction() != null) {
			setCryptoFailureAction(consumerSpec.getCryptoFailureAction());
		}
		if (consumerSpec.getMaxPendingChunkedMessage() != null) {
			setMaxPendingChunkedMessage(consumerSpec.getMaxPendingChunkedMessage());
		}
		if (consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull() != null) {
			setAutoAckOldestChunkedMessageOnQueueFull(consumerSpec.getAutoAckOldestChunkedMessageOnQueueFull());
		}
		if (consumerSpec.getExpireTimeOfIncompleteChunkedMessage() != null) {
			setExpireTimeOfIncompleteChunkedMessage(consumerSpec.getExpireTimeOfIncompleteChunkedMessage());
		}
	}

}
