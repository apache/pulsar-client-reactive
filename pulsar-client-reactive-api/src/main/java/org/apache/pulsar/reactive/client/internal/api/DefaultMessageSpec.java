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

package org.apache.pulsar.reactive.client.internal.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.TypedMessageBuilder;

/**
 * Default spec for a message. Can configure all the properties of a
 * {@link TypedMessageBuilder}.
 *
 * @param <T> the message payload type
 * @see DefaultMessageSpecBuilder
 * @see TypedMessageBuilder
 */
class DefaultMessageSpec<T> implements InternalMessageSpec<T> {

	private final String key;

	private final byte[] orderingKey;

	private final byte[] keyBytes;

	private final T value;

	private final Map<String, String> properties;

	private final Long eventTime;

	private final Long sequenceId;

	private final List<String> replicationClusters;

	private final boolean disableReplication;

	private final Long deliverAt;

	private final Long deliverAfterDelay;

	private final TimeUnit deliverAfterUnit;

	private final Object correlationMetadata;

	DefaultMessageSpec(String key, byte[] orderingKey, byte[] keyBytes, T value, Map<String, String> properties,
			Long eventTime, Long sequenceId, List<String> replicationClusters, boolean disableReplication,
			Long deliverAt, Long deliverAfterDelay, TimeUnit deliverAfterUnit, Object correlationMetadata) {
		this.key = key;
		this.orderingKey = orderingKey;
		this.keyBytes = keyBytes;
		this.value = value;
		this.properties = properties;
		this.eventTime = eventTime;
		this.sequenceId = sequenceId;
		this.replicationClusters = replicationClusters;
		this.disableReplication = disableReplication;
		this.deliverAt = deliverAt;
		this.deliverAfterDelay = deliverAfterDelay;
		this.deliverAfterUnit = deliverAfterUnit;
		this.correlationMetadata = correlationMetadata;
	}

	@Override
	public void configure(TypedMessageBuilder<T> typedMessageBuilder) {
		if (this.key != null) {
			typedMessageBuilder.key(this.key);
		}
		if (this.orderingKey != null) {
			typedMessageBuilder.orderingKey(this.orderingKey);
		}
		if (this.keyBytes != null) {
			typedMessageBuilder.keyBytes(this.keyBytes);
		}
		typedMessageBuilder.value(this.value);
		if (this.properties != null) {
			typedMessageBuilder.properties(this.properties);
		}
		if (this.eventTime != null) {
			typedMessageBuilder.eventTime(this.eventTime);
		}
		if (this.sequenceId != null) {
			typedMessageBuilder.sequenceId(this.sequenceId);
		}
		if (this.replicationClusters != null) {
			typedMessageBuilder.replicationClusters(this.replicationClusters);
		}
		if (this.disableReplication) {
			typedMessageBuilder.disableReplication();
		}
		if (this.deliverAt != null) {
			typedMessageBuilder.deliverAt(this.deliverAt);
		}
		if (this.deliverAfterDelay != null) {
			typedMessageBuilder.deliverAfter(this.deliverAfterDelay, this.deliverAfterUnit);
		}
	}

	@Override
	public <C> C getCorrelationMetadata() {
		return (C) this.correlationMetadata;
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder().append("DefaultMessageSpec{");
		stringBuilder.append("value=").append(this.value);
		if (this.key != null) {
			stringBuilder.append(", key='").append(this.key).append('\'');
		}
		if (this.orderingKey != null) {
			stringBuilder.append(", orderingKey=").append(Arrays.toString(this.orderingKey));
		}
		if (this.keyBytes != null) {
			stringBuilder.append(", keyBytes=").append(Arrays.toString(this.keyBytes));
		}
		if (this.properties != null) {
			stringBuilder.append(", properties=").append(this.properties);
		}
		if (this.eventTime != null) {
			stringBuilder.append(", eventTime=").append(this.eventTime);
		}
		if (this.sequenceId != null) {
			stringBuilder.append(", sequenceId=").append(this.sequenceId);
		}
		if (this.replicationClusters != null) {
			stringBuilder.append(", replicationClusters=").append(this.replicationClusters);
		}
		if (this.disableReplication) {
			stringBuilder.append(", disableReplication=").append(this.disableReplication);
		}
		if (this.deliverAt != null) {
			stringBuilder.append(", deliverAt=").append(this.deliverAt);
		}
		if (this.deliverAfterDelay != null) {
			stringBuilder.append(", deliverAfterDelay=").append(this.deliverAfterDelay);
			stringBuilder.append(", deliverAfterUnit=").append(this.deliverAfterUnit);
		}
		if (this.correlationMetadata != null) {
			stringBuilder.append(", correlationMetadata=").append(this.correlationMetadata);
		}
		return stringBuilder.append('}').toString();
	}

}
