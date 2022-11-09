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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import reactor.core.scheduler.Scheduler;

public interface ReactiveMessageConsumerSpec {

	List<String> getTopicNames();

	Pattern getTopicsPattern();

	RegexSubscriptionMode getTopicsPatternSubscriptionMode();

	Duration getTopicsPatternAutoDiscoveryPeriod();

	String getSubscriptionName();

	SubscriptionMode getSubscriptionMode();

	SubscriptionType getSubscriptionType();

	SubscriptionInitialPosition getSubscriptionInitialPosition();

	KeySharedPolicy getKeySharedPolicy();

	Boolean getReplicateSubscriptionState();

	Map<String, String> getSubscriptionProperties();

	String getConsumerName();

	Map<String, String> getProperties();

	Integer getPriorityLevel();

	Boolean getReadCompacted();

	Boolean getBatchIndexAckEnabled();

	Duration getAckTimeout();

	Duration getAckTimeoutTickTime();

	Duration getAcknowledgementsGroupTime();

	Boolean getAcknowledgeAsynchronously();

	Scheduler getAcknowledgeScheduler();

	Duration getNegativeAckRedeliveryDelay();

	DeadLetterPolicy getDeadLetterPolicy();

	Boolean getRetryLetterTopicEnable();

	Integer getReceiverQueueSize();

	Integer getMaxTotalReceiverQueueSizeAcrossPartitions();

	Boolean getAutoUpdatePartitions();

	Duration getAutoUpdatePartitionsInterval();

	CryptoKeyReader getCryptoKeyReader();

	ConsumerCryptoFailureAction getCryptoFailureAction();

	Integer getMaxPendingChunkedMessage();

	Boolean getAutoAckOldestChunkedMessageOnQueueFull();

	Duration getExpireTimeOfIncompleteChunkedMessage();

}
