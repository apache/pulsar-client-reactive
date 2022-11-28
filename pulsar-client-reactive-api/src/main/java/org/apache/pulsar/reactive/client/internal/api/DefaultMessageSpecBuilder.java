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

package org.apache.pulsar.reactive.client.internal.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.MessageSpecBuilder;

/**
 * Default class to build a message spec.
 *
 * @param <T> the message payload type
 * @author Lari Hotari
 * @see ApiImplementationFactory#createMessageSpecBuilder(Object)
 * @see DefaultMessageSpec
 */
class DefaultMessageSpecBuilder<T> implements MessageSpecBuilder<T> {

	private String key;

	private byte[] orderingKey;

	private byte[] keyBytes;

	private T value;

	private Map<String, String> properties;

	private Long eventTime;

	private Long sequenceId;

	private List<String> replicationClusters;

	private boolean disableReplication;

	private Long deliverAt;

	private Long deliverAfterDelay;

	private TimeUnit deliverAfterUnit;

	@Override
	public MessageSpecBuilder<T> key(String key) {
		this.key = key;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> keyBytes(byte[] key) {
		this.keyBytes = key;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> orderingKey(byte[] orderingKey) {
		this.orderingKey = orderingKey;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> value(T value) {
		this.value = value;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> property(String name, String value) {
		if (this.properties == null) {
			this.properties = new HashMap<>();
		}
		this.properties.put(name, value);
		return this;
	}

	@Override
	public MessageSpecBuilder<T> properties(Map<String, String> properties) {
		if (this.properties == null) {
			this.properties = new HashMap<>();
		}
		this.properties.putAll(properties);
		return this;
	}

	@Override
	public MessageSpecBuilder<T> eventTime(long timestamp) {
		this.eventTime = timestamp;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> sequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> replicationClusters(List<String> clusters) {
		this.replicationClusters = new ArrayList<>(clusters);
		return this;
	}

	@Override
	public MessageSpecBuilder<T> disableReplication() {
		this.disableReplication = true;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> deliverAt(long timestamp) {
		this.deliverAt = timestamp;
		return this;
	}

	@Override
	public MessageSpecBuilder<T> deliverAfter(long delay, TimeUnit unit) {
		this.deliverAfterDelay = delay;
		this.deliverAfterUnit = unit;
		return this;
	}

	@Override
	public MessageSpec<T> build() {
		return new DefaultMessageSpec<T>(this.key, this.orderingKey, this.keyBytes, this.value, this.properties,
				this.eventTime, this.sequenceId, this.replicationClusters, this.disableReplication, this.deliverAt,
				this.deliverAfterDelay, this.deliverAfterUnit);
	}

}
