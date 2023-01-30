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

package org.apache.pulsar.reactive.client.mutiny;

import java.util.Map;
import java.util.Optional;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.common.api.EncryptionContext;

/**
 * Mock message for testing.
 */
class TestMessage implements Message<String> {

	private final String value;

	TestMessage(String value) {
		this.value = value;
	}

	@Override
	public Map<String, String> getProperties() {
		return null;
	}

	@Override
	public boolean hasProperty(String name) {
		return false;
	}

	@Override
	public String getProperty(String name) {
		return null;
	}

	@Override
	public byte[] getData() {
		return new byte[0];
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public MessageId getMessageId() {
		return null;
	}

	@Override
	public long getPublishTime() {
		return 0;
	}

	@Override
	public long getEventTime() {
		return 0;
	}

	@Override
	public long getSequenceId() {
		return 0;
	}

	@Override
	public String getProducerName() {
		return null;
	}

	@Override
	public boolean hasKey() {
		return false;
	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public boolean hasBase64EncodedKey() {
		return false;
	}

	@Override
	public byte[] getKeyBytes() {
		return new byte[0];
	}

	@Override
	public boolean hasOrderingKey() {
		return false;
	}

	@Override
	public byte[] getOrderingKey() {
		return new byte[0];
	}

	@Override
	public String getTopicName() {
		return null;
	}

	@Override
	public Optional<EncryptionContext> getEncryptionCtx() {
		return Optional.empty();
	}

	@Override
	public int getRedeliveryCount() {
		return 0;
	}

	@Override
	public byte[] getSchemaVersion() {
		return new byte[0];
	}

	@Override
	public boolean isReplicated() {
		return false;
	}

	@Override
	public String getReplicatedFrom() {
		return null;
	}

	@Override
	public void release() {

	}

	@Override
	public boolean hasBrokerPublishTime() {
		return false;
	}

	@Override
	public Optional<Long> getBrokerPublishTime() {
		return Optional.empty();
	}

	@Override
	public boolean hasIndex() {
		return false;
	}

	@Override
	public Optional<Long> getIndex() {
		return Optional.empty();
	}

}
