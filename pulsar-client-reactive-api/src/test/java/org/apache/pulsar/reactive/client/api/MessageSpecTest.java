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

package org.apache.pulsar.reactive.client.api;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.reactive.client.internal.api.InternalMessageSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MessageSpec}.
 */
class MessageSpecTest {

	@Test
	void builder() {
		MessageSpec<String> messageSpec = MessageSpec.builder("my-value").key("my-key").keyBytes(new byte[] { 1 })
				.orderingKey(new byte[] { 2 }).property("my-prop-key-1", "my-prop-value-1")
				.properties(Collections.singletonMap("my-prop-key-2", "my-prop-value-2")).eventTime(3).sequenceId(4)
				.replicationClusters(Collections.singletonList("my-cluster")).disableReplication().deliverAt(5)
				.deliverAfter(6, TimeUnit.SECONDS).build();

		assertThat(messageSpec).isInstanceOf(InternalMessageSpec.class);

		TestTypedMessageBuilder typedMessageBuilder = new TestTypedMessageBuilder();
		((InternalMessageSpec<String>) messageSpec).configure(typedMessageBuilder);

		assertThat(typedMessageBuilder.getValue()).isEqualTo("my-value");
		assertThat(typedMessageBuilder.getKey()).isEqualTo("my-key");
		assertThat(typedMessageBuilder.getKeyBytes()).containsExactly(1);
		assertThat(typedMessageBuilder.getOrderingKey()).containsExactly(2);
		assertThat(typedMessageBuilder.getProperties()).hasSize(2).containsEntry("my-prop-key-1", "my-prop-value-1")
				.containsEntry("my-prop-key-2", "my-prop-value-2");
		assertThat(typedMessageBuilder.getEventTime()).isEqualTo(3);
		assertThat(typedMessageBuilder.getSequenceId()).isEqualTo(4);
		assertThat(typedMessageBuilder.getReplicationClusters()).containsExactly("my-cluster");
		assertThat(typedMessageBuilder.getDisableReplication()).isTrue();
		assertThat(typedMessageBuilder.getDeliverAt()).isEqualTo(5);
		assertThat(typedMessageBuilder.getDeliverAfter()).hasSeconds(6);
	}

	@Test
	void ofValue() {
		MessageSpec<String> messageSpec = MessageSpec.of("my-value");

		assertThat(messageSpec).isInstanceOf(InternalMessageSpec.class);

		TestTypedMessageBuilder typedMessageBuilder = new TestTypedMessageBuilder();
		((InternalMessageSpec<String>) messageSpec).configure(typedMessageBuilder);

		assertThat(typedMessageBuilder.getValue()).isEqualTo("my-value");
		assertThat(typedMessageBuilder).hasAllNullFieldsOrPropertiesExcept("value");
	}

	public static class TestTypedMessageBuilder implements TypedMessageBuilder<String> {

		private String key;

		private byte[] orderingKey;

		private byte[] keyBytes;

		private String value;

		private Map<String, String> properties;

		private Long eventTime;

		private Long sequenceId;

		private List<String> replicationClusters;

		private Boolean disableReplication;

		private Long deliverAt;

		private Duration deliverAfter;

		@Override
		public MessageId send() {
			throw new IllegalStateException("not implemented");
		}

		@Override
		public CompletableFuture<MessageId> sendAsync() {
			throw new IllegalStateException("not implemented");
		}

		@Override
		public TypedMessageBuilder<String> key(String key) {
			this.key = key;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> keyBytes(byte[] key) {
			this.keyBytes = key;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> orderingKey(byte[] orderingKey) {
			this.orderingKey = orderingKey;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> value(String value) {
			this.value = value;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> property(String name, String value) {
			throw new IllegalStateException("not implemented");
		}

		@Override
		public TypedMessageBuilder<String> properties(Map<String, String> properties) {
			this.properties = properties;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> eventTime(long timestamp) {
			this.eventTime = timestamp;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> sequenceId(long sequenceId) {
			this.sequenceId = sequenceId;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> replicationClusters(List<String> clusters) {
			this.replicationClusters = clusters;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> disableReplication() {
			this.disableReplication = true;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> deliverAt(long timestamp) {
			this.deliverAt = timestamp;
			return this;
		}

		@Override
		public TypedMessageBuilder<String> deliverAfter(long delay, TimeUnit unit) {
			this.deliverAfter = Duration.ofMillis(unit.toMillis(delay));
			return this;
		}

		@Override
		public TypedMessageBuilder<String> loadConf(Map<String, Object> config) {
			throw new IllegalStateException("not implemented");
		}

		public String getKey() {
			return this.key;
		}

		public byte[] getOrderingKey() {
			return this.orderingKey;
		}

		public byte[] getKeyBytes() {
			return this.keyBytes;
		}

		public String getValue() {
			return this.value;
		}

		public Map<String, String> getProperties() {
			return this.properties;
		}

		public Long getEventTime() {
			return this.eventTime;
		}

		public Long getSequenceId() {
			return this.sequenceId;
		}

		public List<String> getReplicationClusters() {
			return this.replicationClusters;
		}

		public Boolean getDisableReplication() {
			return this.disableReplication;
		}

		public Long getDeliverAt() {
			return this.deliverAt;
		}

		public Duration getDeliverAfter() {
			return this.deliverAfter;
		}

	}

}
