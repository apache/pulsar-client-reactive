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

package org.apache.pulsar.reactive.client.jackson;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageConsumerSpec;
import reactor.core.scheduler.Scheduler;

@JsonDeserialize(as = ImmutableReactiveMessageConsumerSpec.class)
abstract class ImmutableReactiveMessageConsumerSpecMixin {

	@SuppressWarnings("unused")
	@JsonCreator
	ImmutableReactiveMessageConsumerSpecMixin(@JsonProperty("topicNames") List<String> topicNames,
			@JsonProperty("topicsPattern") Pattern topicsPattern,
			@JsonProperty("topicsPatternSubscriptionMode") RegexSubscriptionMode topicsPatternSubscriptionMode,
			@JsonProperty("topicsPatternAutoDiscoveryPeriod") Duration topicsPatternAutoDiscoveryPeriod,
			@JsonProperty("subscriptionName") String subscriptionName,
			@JsonProperty("subscriptionMode") SubscriptionMode subscriptionMode,
			@JsonProperty("subscriptionType") SubscriptionType subscriptionType,
			@JsonProperty("subscriptionInitialPosition") SubscriptionInitialPosition subscriptionInitialPosition,
			@JsonProperty("keySharedPolicy") KeySharedPolicy keySharedPolicy,
			@JsonProperty("replicateSubscriptionState") Boolean replicateSubscriptionState,
			@JsonProperty("subscriptionProperties") Map<String, String> subscriptionProperties,
			@JsonProperty("consumerName") String consumerName,
			@JsonProperty("properties") Map<String, String> properties,
			@JsonProperty("priorityLevel") Integer priorityLevel, @JsonProperty("readCompacted") Boolean readCompacted,
			@JsonProperty("batchIndexAckEnabled") Boolean batchIndexAckEnabled,
			@JsonProperty("ackTimeout") Duration ackTimeout,
			@JsonProperty("ackTimeoutTickTime") Duration ackTimeoutTickTime,
			@JsonProperty("acknowledgementsGroupTime") Duration acknowledgementsGroupTime,
			@JsonProperty("acknowledgeAsynchronously") Boolean acknowledgeAsynchronously,
			@JsonProperty("acknowledgeScheduler") Scheduler acknowledgeScheduler,
			@JsonProperty("negativeAckRedeliveryDelay") Duration negativeAckRedeliveryDelay,
			@JsonProperty("deadLetterPolicy") DeadLetterPolicy deadLetterPolicy,
			@JsonProperty("retryLetterTopicEnable") Boolean retryLetterTopicEnable,
			@JsonProperty("receiverQueueSize") Integer receiverQueueSize,
			@JsonProperty("maxTotalReceiverQueueSizeAcrossPartitions") Integer maxTotalReceiverQueueSizeAcrossPartitions,
			@JsonProperty("autoUpdatePartitions") Boolean autoUpdatePartitions,
			@JsonProperty("autoUpdatePartitionsInterval") Duration autoUpdatePartitionsInterval,
			@JsonProperty("cryptoKeyReader") CryptoKeyReader cryptoKeyReader,
			@JsonProperty("cryptoFailureAction") ConsumerCryptoFailureAction cryptoFailureAction,
			@JsonProperty("maxPendingChunkedMessage") Integer maxPendingChunkedMessage,
			@JsonProperty("autoAckOldestChunkedMessageOnQueueFull") Boolean autoAckOldestChunkedMessageOnQueueFull,
			@JsonProperty("expireTimeOfIncompleteChunkedMessage") Duration expireTimeOfIncompleteChunkedMessage) {

	}

}
