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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.EncryptionKeyInfo;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.impl.KeyBasedBatcherBuilder;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;

class PulsarReactiveClientModuleTests {

	private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new PulsarReactiveClientModule())
		.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	@ParameterizedTest
	@ValueSource(classes = { ReactiveMessageConsumerSpec.class, ImmutableReactiveMessageConsumerSpec.class,
			MutableReactiveMessageConsumerSpec.class })
	void shouldSerDeserReactiveMessageConsumerSpec(Class<? extends ReactiveMessageConsumerSpec> klass)
			throws Exception {
		// @formatter:off
		String content = ("{"
				+ "'topicNames': ['my-topic'],"
				+ "'topicsPattern': 'my-topic-*',"
				+ "'topicsPatternSubscriptionMode': 'PersistentOnly',"
				+ "'topicsPatternAutoDiscoveryPeriod': 30,"
				+ "'subscriptionName': 'my-sub',"
				+ "'subscriptionMode': 'Durable',"
				+ "'subscriptionType': 'Exclusive',"
				+ "'subscriptionInitialPosition': 'Latest',"
				+ "'keySharedPolicy': 'STICKY',"
				+ "'replicateSubscriptionState': true,"
				+ "'subscriptionProperties': {'my-key': 'my-value'},"
				+ "'consumerName': 'my-consumer',"
				+ "'properties': {'my-key': 'my-value'},"
				+ "'priorityLevel': 42,"
				+ "'readCompacted': true,"
				+ "'batchIndexAckEnabled': true,"
				+ "'ackTimeout': 30,"
				+ "'ackTimeoutTickTime': 30,"
				+ "'acknowledgementsGroupTime': 30,"
				+ "'acknowledgeAsynchronously': true,"
				+ "'acknowledgeScheduler': 'boundedElastic',"
				+ "'negativeAckRedeliveryDelay': 30,"
				+ "'negativeAckRedeliveryBackoff': {"
				+ "    'className': '" + TestRedeliveryBackoff.class.getName() + "'"
				+ "},"
				+ "'ackTimeoutRedeliveryBackoff': {"
				+ "    'className': '" + TestRedeliveryBackoff.class.getName() + "'"
				+ "},"
				+ "'deadLetterPolicy': {"
				+ "    'maxRedeliverCount': 1,"
				+ "    'retryLetterTopic': 'my-retry-topic',"
				+ "    'deadLetterTopic': 'my-dlq',"
				+ "    'initialSubscriptionName': 'my-dlq-sub'"
				+ "},"
				+ "'retryLetterTopicEnable': true,"
				+ "'receiverQueueSize': 42,"
				+ "'maxTotalReceiverQueueSizeAcrossPartitions': 42,"
				+ "'autoUpdatePartitions': true,"
				+ "'autoUpdatePartitionsInterval': 30,"
				+ "'cryptoKeyReader': {"
				+ "    'className': '" + TestCryptoKeyReader.class.getName() + "',"
				+ "    'args': {'dummy': 'my-dummy'}"
				+ "},"
				+ "'cryptoFailureAction': 'FAIL',"
				+ "'maxPendingChunkedMessage': 42,"
				+ "'autoAckOldestChunkedMessageOnQueueFull': true,"
				+ "'expireTimeOfIncompleteChunkedMessage': 30"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		ReactiveMessageConsumerSpec spec = MAPPER.readValue(content, klass);

		assertThat(spec.getTopicNames()).containsExactly("my-topic");
		assertThat(spec.getTopicsPattern().pattern()).isEqualTo("my-topic-*");
		assertThat(spec.getTopicsPatternSubscriptionMode()).isEqualTo(RegexSubscriptionMode.PersistentOnly);
		assertThat(spec.getTopicsPatternAutoDiscoveryPeriod()).hasMillis(30_000);
		assertThat(spec.getSubscriptionName()).isEqualTo("my-sub");
		assertThat(spec.getSubscriptionMode()).isEqualTo(SubscriptionMode.Durable);
		assertThat(spec.getSubscriptionType()).isEqualTo(SubscriptionType.Exclusive);
		assertThat(spec.getSubscriptionInitialPosition()).isEqualTo(SubscriptionInitialPosition.Latest);
		assertThat(spec.getKeySharedPolicy()).isInstanceOf(KeySharedPolicy.KeySharedPolicySticky.class);
		assertThat(spec.getReplicateSubscriptionState()).isTrue();
		assertThat(spec.getSubscriptionProperties()).containsOnlyKeys("my-key");
		assertThat(spec.getSubscriptionProperties()).containsEntry("my-key", "my-value");
		assertThat(spec.getConsumerName()).isEqualTo("my-consumer");
		assertThat(spec.getProperties()).containsOnlyKeys("my-key");
		assertThat(spec.getProperties()).containsEntry("my-key", "my-value");
		assertThat(spec.getPriorityLevel()).isEqualTo(42);
		assertThat(spec.getReadCompacted()).isTrue();
		assertThat(spec.getBatchIndexAckEnabled()).isTrue();
		assertThat(spec.getAckTimeout()).hasMillis(30_000);
		assertThat(spec.getAckTimeoutTickTime()).hasMillis(30_000);
		assertThat(spec.getAcknowledgementsGroupTime()).hasMillis(30_000);
		assertThat(spec.getAcknowledgeAsynchronously()).isTrue();
		assertThat(spec.getAcknowledgeScheduler().toString()).isEqualTo("Schedulers.boundedElastic()");
		assertThat(spec.getNegativeAckRedeliveryDelay()).hasMillis(30_000);
		assertThat(spec.getNegativeAckRedeliveryBackoff()).isEqualTo(new TestRedeliveryBackoff());
		assertThat(spec.getAckTimeoutRedeliveryBackoff()).isEqualTo(new TestRedeliveryBackoff());
		assertThat(spec.getDeadLetterPolicy().getMaxRedeliverCount()).isEqualTo(1);
		assertThat(spec.getDeadLetterPolicy().getDeadLetterTopic()).isEqualTo("my-dlq");
		assertThat(spec.getDeadLetterPolicy().getRetryLetterTopic()).isEqualTo("my-retry-topic");
		assertThat(spec.getDeadLetterPolicy().getInitialSubscriptionName()).isEqualTo("my-dlq-sub");
		assertThat(spec.getRetryLetterTopicEnable()).isTrue();
		assertThat(spec.getReceiverQueueSize()).isEqualTo(42);
		assertThat(spec.getMaxTotalReceiverQueueSizeAcrossPartitions()).isEqualTo(42);
		assertThat(spec.getAutoUpdatePartitions()).isTrue();
		assertThat(spec.getAutoUpdatePartitionsInterval()).hasMillis(30_000);
		assertThat(spec.getCryptoKeyReader()).isInstanceOf(TestCryptoKeyReader.class);
		Map<String, Object> params = ((TestCryptoKeyReader) spec.getCryptoKeyReader()).params;
		assertThat(params).containsOnlyKeys("dummy");
		assertThat(params).containsEntry("dummy", "my-dummy");
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ConsumerCryptoFailureAction.FAIL);
		assertThat(spec.getMaxPendingChunkedMessage()).isEqualTo(42);
		assertThat(spec.getAutoAckOldestChunkedMessageOnQueueFull()).isTrue();
		assertThat(spec.getExpireTimeOfIncompleteChunkedMessage()).hasMillis(30_000);

		String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(spec);

		// @formatter:off
		String expected = ("{\n"
				+ "  'topicNames' : [ 'my-topic' ],\n"
				+ "  'topicsPattern' : 'my-topic-*',\n"
				+ "  'topicsPatternSubscriptionMode' : 'PersistentOnly',\n"
				+ "  'topicsPatternAutoDiscoveryPeriod' : 30.000000000,\n"
				+ "  'subscriptionName' : 'my-sub',\n"
				+ "  'subscriptionMode' : 'Durable',\n"
				+ "  'subscriptionType' : 'Exclusive',\n"
				+ "  'subscriptionInitialPosition' : 'Latest',\n"
				+ "  'keySharedPolicy' : 'STICKY',\n"
				+ "  'replicateSubscriptionState' : true,\n"
				+ "  'subscriptionProperties' : {\n"
				+ "    'my-key' : 'my-value'\n"
				+ "  },\n"
				+ "  'consumerName' : 'my-consumer',\n"
				+ "  'properties' : {\n"
				+ "    'my-key' : 'my-value'\n"
				+ "  },\n"
				+ "  'priorityLevel' : 42,\n"
				+ "  'readCompacted' : true,\n"
				+ "  'batchIndexAckEnabled' : true,\n"
				+ "  'ackTimeout' : 30.000000000,\n"
				+ "  'ackTimeoutTickTime' : 30.000000000,\n"
				+ "  'acknowledgementsGroupTime' : 30.000000000,\n"
				+ "  'acknowledgeAsynchronously' : true,\n"
				+ "  'acknowledgeScheduler' : 'boundedElastic',\n"
				+ "  'negativeAckRedeliveryDelay' : 30.000000000,\n"
				+ "  'negativeAckRedeliveryBackoff' : {\n"
				+ "    'className' : '" + TestRedeliveryBackoff.class.getName() + "'\n"
				+ "  },\n"
				+ "  'ackTimeoutRedeliveryBackoff' : {\n"
				+ "    'className' : '" + TestRedeliveryBackoff.class.getName() + "'\n"
				+ "  },\n"
				+ "  'deadLetterPolicy' : {\n"
				+ "    'maxRedeliverCount' : 1,\n"
				+ "    'retryLetterTopic' : 'my-retry-topic',\n"
				+ "    'deadLetterTopic' : 'my-dlq',\n"
				+ "    'initialSubscriptionName' : 'my-dlq-sub'\n"
				+ "  },\n"
				+ "  'retryLetterTopicEnable' : true,\n"
				+ "  'receiverQueueSize' : 42,\n"
				+ "  'maxTotalReceiverQueueSizeAcrossPartitions' : 42,\n"
				+ "  'autoUpdatePartitions' : true,\n"
				+ "  'autoUpdatePartitionsInterval' : 30.000000000,\n"
				+ "  'cryptoKeyReader' : {\n"
				+ "    'className' : '" + TestCryptoKeyReader.class.getName() + "'\n"
				+ "  },\n"
				+ "  'cryptoFailureAction' : 'FAIL',\n"
				+ "  'maxPendingChunkedMessage' : 42,\n"
				+ "  'autoAckOldestChunkedMessageOnQueueFull' : true,\n"
				+ "  'expireTimeOfIncompleteChunkedMessage' : 30.000000000\n"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		assertThat(json).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(classes = {
			// MutableReactiveMessageConsumerSpec.class,
			ReactiveMessageConsumerSpec.class, ImmutableReactiveMessageConsumerSpec.class })
	void shouldSerDeserEmptyReactiveMessageConsumerSpec(Class<? extends ReactiveMessageConsumerSpec> klass)
			throws Exception {
		String content = "{}";
		ReactiveMessageConsumerSpec spec = MAPPER.readValue(content, klass);
		String json = MAPPER.writeValueAsString(spec);
		assertThat(json).isEqualTo(content);
	}

	@ParameterizedTest
	@ValueSource(classes = { ReactiveMessageReaderSpec.class, ImmutableReactiveMessageReaderSpec.class,
			MutableReactiveMessageReaderSpec.class })
	void shouldSerDeserReactiveMessageReaderSpec(Class<? extends ReactiveMessageReaderSpec> klass) throws Exception {
		// @formatter:off
		String content = ("{"
				+ "'topicNames': ['my-topic'],"
				+ "'readerName': 'my-reader',"
				+ "'subscriptionName': 'my-sub',"
				+ "'generatedSubscriptionNamePrefix': 'my-prefix-',"
				+ "'receiverQueueSize': 42,"
				+ "'readCompacted': true,"
				+ "'keyHashRanges': [{"
				+ "    'start': 42,"
				+ "    'end': 43"
				+ "}],"
				+ "'cryptoKeyReader': {"
				+ "    'className': '" + TestCryptoKeyReader.class.getName() + "',"
				+ "    'args': {'dummy': 'my-dummy'}"
				+ "},"
				+ "'cryptoFailureAction': 'FAIL'"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		ReactiveMessageReaderSpec spec = MAPPER.readValue(content, klass);

		assertThat(spec.getTopicNames()).containsExactly("my-topic");
		assertThat(spec.getReaderName()).isEqualTo("my-reader");
		assertThat(spec.getSubscriptionName()).isEqualTo("my-sub");
		assertThat(spec.getGeneratedSubscriptionNamePrefix()).isEqualTo("my-prefix-");
		assertThat(spec.getReceiverQueueSize()).isEqualTo(42);
		assertThat(spec.getReadCompacted()).isTrue();
		assertThat(spec.getCryptoKeyReader()).isInstanceOf(TestCryptoKeyReader.class);
		assertThat(spec.getKeyHashRanges()).containsExactly(new Range(42, 43));
		assertThat(spec.getCryptoKeyReader()).isInstanceOf(TestCryptoKeyReader.class);
		Map<String, Object> params = ((TestCryptoKeyReader) spec.getCryptoKeyReader()).params;
		assertThat(params).containsOnlyKeys("dummy");
		assertThat(params).containsEntry("dummy", "my-dummy");
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ConsumerCryptoFailureAction.FAIL);

		String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(spec);

		// @formatter:off
		String expected = ("{\n"
				+ "  'topicNames' : [ 'my-topic' ],\n"
				+ "  'readerName' : 'my-reader',\n"
				+ "  'subscriptionName' : 'my-sub',\n"
				+ "  'generatedSubscriptionNamePrefix' : 'my-prefix-',\n"
				+ "  'receiverQueueSize' : 42,\n"
				+ "  'readCompacted' : true,\n"
				+ "  'keyHashRanges' : [ {\n"
				+ "    'start' : 42,\n"
				+ "    'end' : 43\n"
				+ "  } ],\n"
				+ "  'cryptoKeyReader' : {\n"
				+ "    'className' : '" + TestCryptoKeyReader.class.getName() + "'\n"
				+ "  },\n"
				+ "  'cryptoFailureAction' : 'FAIL'\n"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		assertThat(json).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(classes = { MutableReactiveMessageSenderSpec.class, ReactiveMessageSenderSpec.class,
			ImmutableReactiveMessageSenderSpec.class })
	void shouldSerDeserEmptyReactiveMessageSenderSpec(Class<? extends ReactiveMessageSenderSpec> klass)
			throws Exception {
		String content = "{}";
		ReactiveMessageSenderSpec spec = MAPPER.readValue(content, klass);
		String json = MAPPER.writeValueAsString(spec);
		assertThat(json).isEqualTo(content);
	}

	@ParameterizedTest
	@ValueSource(classes = {
			// MutableReactiveMessageReaderSpec.class,
			ReactiveMessageReaderSpec.class, ImmutableReactiveMessageReaderSpec.class })
	void shouldSerDeserEmptyReactiveMessageReaderSpec(Class<? extends ReactiveMessageReaderSpec> klass)
			throws Exception {
		String content = "{}";
		ReactiveMessageReaderSpec spec = MAPPER.readValue(content, klass);
		String json = MAPPER.writeValueAsString(spec);
		assertThat(json).isEqualTo(content);
	}

	@ParameterizedTest
	@ValueSource(classes = { ReactiveMessageSenderSpec.class, ImmutableReactiveMessageSenderSpec.class,
			MutableReactiveMessageSenderSpec.class })
	void shouldSerDeserReactiveMessageSenderSpec(Class<? extends ReactiveMessageSenderSpec> klass) throws Exception {
		// @formatter:off
		String content = ("{"
				+ "'topicName': 'my-topic',"
				+ "'producerName': 'my-producer',"
				+ "'sendTimeout': 30,"
				+ "'maxPendingMessages': 42,"
				+ "'maxPendingMessagesAcrossPartitions': 42,"
				+ "'messageRoutingMode': 'SinglePartition',"
				+ "'hashingScheme': 'JavaStringHash',"
				+ "'cryptoFailureAction': 'FAIL',"
				+ "'messageRouter': {"
				+ "    'className': '" + TestMessageRouter.class.getName() + "',"
				+ "    'args': {'dummy': 'my-dummy'}"
				+ "},"
				+ "'batchingMaxPublishDelay': 30,"
				+ "'roundRobinRouterBatchingPartitionSwitchFrequency': 42,"
				+ "'batchingMaxMessages': 42,"
				+ "'batchingMaxBytes': 42,"
				+ "'batchingEnabled': true,"
				+ "'batcherBuilder': {"
				+ "    'className': 'org.apache.pulsar.client.impl.KeyBasedBatcherBuilder'"
				+ "},"
				+ "'chunkingEnabled': true,"
				+ "'cryptoKeyReader': {"
				+ "    'className': '" + TestCryptoKeyReader.class.getName() + "',"
				+ "    'args': {'dummy': 'my-dummy'}"
				+ "},"
				+ "'encryptionKeys': ['my-encryption-key'],"
				+ "'compressionType': 'LZ4',"
				+ "'initialSequenceId': 42,"
				+ "'autoUpdatePartitions': true,"
				+ "'autoUpdatePartitionsInterval': 30,"
				+ "'multiSchema': true,"
				+ "'accessMode': 'Shared',"
				+ "'lazyStartPartitionedProducers': true,"
				+ "'properties' : {"
				+ "  'my-key' : 'my-value'"
				+ "}"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		ReactiveMessageSenderSpec spec = MAPPER.readValue(content, klass);

		assertThat(spec.getTopicName()).isEqualTo("my-topic");
		assertThat(spec.getProducerName()).isEqualTo("my-producer");
		assertThat(spec.getSendTimeout()).hasMillis(30_000);
		assertThat(spec.getMaxPendingMessages()).isEqualTo(42);
		assertThat(spec.getMaxPendingMessagesAcrossPartitions()).isEqualTo(42);
		assertThat(spec.getMessageRoutingMode()).isEqualTo(MessageRoutingMode.SinglePartition);
		assertThat(spec.getHashingScheme()).isEqualTo(HashingScheme.JavaStringHash);
		assertThat(spec.getCryptoFailureAction()).isEqualTo(ProducerCryptoFailureAction.FAIL);
		assertThat(spec.getMessageRouter()).isInstanceOf(TestMessageRouter.class);
		Map<String, Object> params = ((TestMessageRouter) spec.getMessageRouter()).params;
		assertThat(params).containsOnlyKeys("dummy");
		assertThat(params).containsEntry("dummy", "my-dummy");
		assertThat(spec.getBatchingMaxPublishDelay()).hasMillis(30_000);
		assertThat(spec.getRoundRobinRouterBatchingPartitionSwitchFrequency()).isEqualTo(42);
		assertThat(spec.getBatchingMaxMessages()).isEqualTo(42);
		assertThat(spec.getBatchingMaxBytes()).isEqualTo(42);
		assertThat(spec.getBatchingEnabled()).isTrue();
		assertThat(spec.getBatcherBuilder()).isInstanceOf(KeyBasedBatcherBuilder.class);
		assertThat(spec.getChunkingEnabled()).isTrue();
		assertThat(spec.getCryptoKeyReader()).isInstanceOf(TestCryptoKeyReader.class);
		params = ((TestCryptoKeyReader) spec.getCryptoKeyReader()).params;
		assertThat(params).containsOnlyKeys("dummy");
		assertThat(params).containsEntry("dummy", "my-dummy");
		assertThat(spec.getEncryptionKeys()).containsExactly("my-encryption-key");
		assertThat(spec.getCompressionType()).isEqualTo(CompressionType.LZ4);
		assertThat(spec.getInitialSequenceId()).isEqualTo(42);
		assertThat(spec.getAutoUpdatePartitions()).isTrue();
		assertThat(spec.getAutoUpdatePartitionsInterval()).hasMillis(30_000);
		assertThat(spec.getMultiSchema()).isTrue();
		assertThat(spec.getAccessMode()).isEqualTo(ProducerAccessMode.Shared);
		assertThat(spec.getLazyStartPartitionedProducers()).isTrue();
		assertThat(spec.getProperties()).containsEntry("my-key", "my-value");

		String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(spec);

		// @formatter:off
		String expected = ("{\n"
				+ "  'topicName' : 'my-topic',\n"
				+ "  'producerName' : 'my-producer',\n"
				+ "  'sendTimeout' : 30.000000000,\n"
				+ "  'maxPendingMessages' : 42,\n"
				+ "  'maxPendingMessagesAcrossPartitions' : 42,\n"
				+ "  'messageRoutingMode' : 'SinglePartition',\n"
				+ "  'hashingScheme' : 'JavaStringHash',\n"
				+ "  'cryptoFailureAction' : 'FAIL',\n"
				+ "  'messageRouter' : {\n"
				+ "    'className' : '" + TestMessageRouter.class.getName() + "'\n"
				+ "  },\n"
				+ "  'batchingMaxPublishDelay' : 30.000000000,\n"
				+ "  'roundRobinRouterBatchingPartitionSwitchFrequency' : 42,\n"
				+ "  'batchingMaxMessages' : 42,\n"
				+ "  'batchingMaxBytes' : 42,\n"
				+ "  'batchingEnabled' : true,\n"
				+ "  'batcherBuilder' : { },\n"
				+ "  'chunkingEnabled' : true,\n"
				+ "  'cryptoKeyReader' : {\n"
				+ "    'className' : '" + TestCryptoKeyReader.class.getName() + "'\n"
				+ "  },\n"
				+ "  'encryptionKeys' : [ 'my-encryption-key' ],\n"
				+ "  'compressionType' : 'LZ4',\n"
				+ "  'initialSequenceId' : 42,\n"
				+ "  'autoUpdatePartitions' : true,\n"
				+ "  'autoUpdatePartitionsInterval' : 30.000000000,\n"
				+ "  'multiSchema' : true,\n"
				+ "  'accessMode' : 'Shared',\n"
				+ "  'lazyStartPartitionedProducers' : true,\n"
				+ "  'properties' : {\n"
				+ "    'my-key' : 'my-value'\n"
				+ "  }\n"
				+ "}").replaceAll("'", "\"");
		// @formatter:on

		assertThat(json).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "AUTO_SPLIT", "STICKY" })
	void shouldSerDeserKeySharedPolicy(String keySharedPolicy) throws Exception {
		String content = (String.format("\"%s\"", keySharedPolicy));
		KeySharedPolicy policy = MAPPER.readValue(content, KeySharedPolicy.class);
		String json = MAPPER.writeValueAsString(policy);
		assertThat(json).isEqualTo(content);
	}

	@Test
	void shouldSerializeCustomKeySharedPolicy() throws Exception {
		String json = MAPPER.writeValueAsString(new TestKeySharedPolicy());
		String expected = "\"org.apache.pulsar.reactive.client.jackson.PulsarReactiveClientModuleTests$TestKeySharedPolicy\"";
		assertThat(json).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = { "parallel", "boundedElastic", "immediate", "single" })
	void shouldSerDeserScheduler(String scheduler) throws Exception {
		String content = (String.format("\"%s\"", scheduler));
		Scheduler policy = MAPPER.readValue(content, Scheduler.class);
		String json = MAPPER.writeValueAsString(policy);
		assertThat(json).isEqualTo(content);
	}

	@Test
	void shouldSerDeserDeprecatedElasticScheduler() throws Exception {
		Scheduler policy = MAPPER.readValue("\"elastic\"", Scheduler.class);
		String json = MAPPER.writeValueAsString(policy);
		assertThat(json).isEqualTo("\"boundedElastic\"");
	}

	@Test
	void shouldSerializeCustomScheduler() throws Exception {
		String json = MAPPER.writeValueAsString(new TestScheduler());
		String expected = "\"org.apache.pulsar.reactive.client.jackson.PulsarReactiveClientModuleTests$TestScheduler\"";
		assertThat(json).isEqualTo(expected);
	}

	@Test
	void shouldSerDeserDeadLetterPolicy() throws Exception {
		// @formatter:off
		String content = ("{\n"
				+ "  'maxRedeliverCount' : 0,\n"
				+ "  'retryLetterTopic' : 'my-retry-topic',\n"
				+ "  'deadLetterTopic' : 'my-dlq',\n"
				+ "  'initialSubscriptionName' : 'my-dlq-sub'\n"
				+ "}").replaceAll("'", "\"");
		// @formatter:on
		DeadLetterPolicy policy = MAPPER.readValue(content, DeadLetterPolicy.class);
		String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(policy);
		assertThat(json).isEqualTo(content);
	}

	@Test
	void shouldSerDeserEmptyDeadLetterPolicy() throws Exception {
		DeadLetterPolicy policy = MAPPER.readValue("{}", DeadLetterPolicy.class);
		String json = MAPPER.writeValueAsString(policy);
		assertThat(json).isEqualTo("{\"maxRedeliverCount\":0}");
	}

	@Test
	void shouldSerDeserEmptyRedeliveryBackoff() throws Exception {
		TestRedeliveryBackoff backoff = MAPPER.readValue("{}", TestRedeliveryBackoff.class);
		String json = MAPPER.writeValueAsString(backoff);
		assertThat(json).isEqualTo("{\"className\":\"" + TestRedeliveryBackoff.class.getName() + "\"}");
	}

	@Test
	void shouldSerDeserCryptoKeyReader() throws Exception {
		// @formatter:off
		String content = ("{"
				+ "    'className': '" + TestCryptoKeyReader.class.getName() + "',"
				+ "    'args': {'dummy': 'my-dummy'}"
				+ "}").replaceAll("'", "\"");
		// @formatter:on
		CryptoKeyReader cryptoKeyReader = MAPPER.readValue(content, CryptoKeyReader.class);
		String json = MAPPER.writeValueAsString(cryptoKeyReader);
		String expected = ("{'className':'" + TestCryptoKeyReader.class.getName() + "'}").replaceAll("'", "\"");
		assertThat(json).isEqualTo(expected);
	}

	@Test
	void shouldSerDeserDeliveryBackoff() throws Exception {
		// @formatter:off
		String content = ("{"
				+ "    'className': '" + TestRedeliveryBackoff.class.getName() + "'"
				+ "}").replaceAll("'", "\"");
		// @formatter:on
		RedeliveryBackoff backoffReader = MAPPER.readValue(content, RedeliveryBackoff.class);
		String json = MAPPER.writeValueAsString(backoffReader);
		String expected = ("{'className':'" + TestRedeliveryBackoff.class.getName() + "'}").replaceAll("'", "\"");
		assertThat(json).isEqualTo(expected);
	}

	public static class TestScheduler implements Scheduler {

		@Override
		public Disposable schedule(Runnable task) {
			return null;
		}

		@Override
		public Worker createWorker() {
			return null;
		}

	}

	public static class TestKeySharedPolicy extends KeySharedPolicy {

		@Override
		public void validate() {
		}

	}

	static class TestCryptoKeyReader implements CryptoKeyReader {

		private final Map<String, Object> params;

		// CHECKSTYLE:OFF
		public TestCryptoKeyReader(Map<String, Object> params) {
			this.params = params;
		}
		// CHECKSTYLE:ON

		@Override
		public EncryptionKeyInfo getPublicKey(String keyName, Map<String, String> metadata) {
			return null;
		}

		@Override
		public EncryptionKeyInfo getPrivateKey(String keyName, Map<String, String> metadata) {
			return null;
		}

	}

	static class TestMessageRouter implements MessageRouter {

		private final Map<String, Object> params;

		// CHECKSTYLE:OFF
		public TestMessageRouter(Map<String, Object> params) {
			this.params = params;
		}
		// CHECKSTYLE:ON

	}

	static class TestRedeliveryBackoff implements RedeliveryBackoff {

		// CHECKSTYLE:OFF
		public TestRedeliveryBackoff() {
		}
		// CHECKSTYLE:ON

		@Override
		public long next(int redeliveryCount) {
			return redeliveryCount * 2L;
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof TestRedeliveryBackoff);
		}

		@Override
		public int hashCode() {
			return TestRedeliveryBackoff.class.hashCode();
		}

	}

}
