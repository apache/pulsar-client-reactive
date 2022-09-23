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

package org.apache.pulsar.reactive.client.jackson;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageSenderSpec;

@JsonDeserialize(as = ImmutableReactiveMessageSenderSpec.class)
abstract class ImmutableReactiveMessageSenderSpecMixin {

	@SuppressWarnings("unused")
	@JsonCreator
	ImmutableReactiveMessageSenderSpecMixin(@JsonProperty("topicName") String topicName,
			@JsonProperty("producerName") String producerName, @JsonProperty("sendTimeout") Duration sendTimeout,
			@JsonProperty("maxPendingMessages") Integer maxPendingMessages,
			@JsonProperty("maxPendingMessagesAcrossPartitions") Integer maxPendingMessagesAcrossPartitions,
			@JsonProperty("messageRoutingMode") MessageRoutingMode messageRoutingMode,
			@JsonProperty("hashingScheme") HashingScheme hashingScheme,
			@JsonProperty("cryptoFailureAction") ProducerCryptoFailureAction cryptoFailureAction,
			@JsonProperty("messageRouter") MessageRouter messageRouter,
			@JsonProperty("batchingMaxPublishDelay") Duration batchingMaxPublishDelay,
			@JsonProperty("roundRobinRouterBatchingPartitionSwitchFrequency") Integer roundRobinRouterBatchingPartitionSwitchFrequency,
			@JsonProperty("batchingMaxMessages") Integer batchingMaxMessages,
			@JsonProperty("batchingMaxBytes") Integer batchingMaxBytes,
			@JsonProperty("batchingEnabled") Boolean batchingEnabled,
			@JsonProperty("batcherBuilder") BatcherBuilder batcherBuilder,
			@JsonProperty("chunkingEnabled") Boolean chunkingEnabled,
			@JsonProperty("cryptoKeyReader") CryptoKeyReader cryptoKeyReader,
			@JsonProperty("encryptionKeys") Set<String> encryptionKeys,
			@JsonProperty("compressionType") CompressionType compressionType,
			@JsonProperty("initialSequenceId") Long initialSequenceId,
			@JsonProperty("autoUpdatePartitions") Boolean autoUpdatePartitions,
			@JsonProperty("autoUpdatePartitionsInterval") Duration autoUpdatePartitionsInterval,
			@JsonProperty("multiSchema") Boolean multiSchema, @JsonProperty("accessMode") ProducerAccessMode accessMode,
			@JsonProperty("lazyStartPartitionedProducers") Boolean lazyStartPartitionedProducers,
			@JsonProperty("properties") Map<String, String> properties) {

	}

}
