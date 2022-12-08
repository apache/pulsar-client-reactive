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

package org.apache.pulsar.reactive.client.jackson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageReaderSpec;

@JsonDeserialize(as = ImmutableReactiveMessageReaderSpec.class)
abstract class ImmutableReactiveMessageReaderSpecMixin {

	@SuppressWarnings("unused")
	@JsonCreator
	ImmutableReactiveMessageReaderSpecMixin(@JsonProperty("topicNames") List<String> topicNames,
			@JsonProperty("readerName") String readerName, @JsonProperty("subscriptionName") String subscriptionName,
			@JsonProperty("generatedSubscriptionNamePrefix") String generatedSubscriptionNamePrefix,
			@JsonProperty("receiverQueueSize") Integer receiverQueueSize,
			@JsonProperty("readCompacted") Boolean readCompacted,
			@JsonProperty("keyHashRanges") List<Range> keyHashRanges,
			@JsonProperty("cryptoKeyReader") CryptoKeyReader cryptoKeyReader,
			@JsonProperty("cryptoFailureAction") ConsumerCryptoFailureAction cryptoFailureAction) {
	}

}
