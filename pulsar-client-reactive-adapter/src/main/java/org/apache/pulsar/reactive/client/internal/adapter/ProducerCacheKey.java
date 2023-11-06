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

package org.apache.pulsar.reactive.client.internal.adapter;

import java.util.Objects;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.apache.pulsar.common.protocol.schema.SchemaHash;

final class ProducerCacheKey {

	private final PulsarClient pulsarClient;

	private final ProducerConfigurationData producerConfigurationData;

	private final Schema<?> schema;

	private final SchemaHash schemaHash;

	private final Object producerActionTransformerKey;

	ProducerCacheKey(final PulsarClient pulsarClient, final ProducerConfigurationData producerConfigurationData,
			final Schema<?> schema, Object producerActionTransformerKey) {
		this.pulsarClient = pulsarClient;
		this.producerConfigurationData = producerConfigurationData;
		this.schema = schema;
		this.schemaHash = (this.schema != null) ? SchemaHash.of(this.schema) : null;
		this.producerActionTransformerKey = producerActionTransformerKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProducerCacheKey that = (ProducerCacheKey) o;
		return (Objects.equals(this.pulsarClient, that.pulsarClient)
				&& Objects.equals(this.producerConfigurationData, that.producerConfigurationData)
				&& Objects.equals(this.schemaHash, that.schemaHash))
				&& Objects.equals(this.producerActionTransformerKey, that.producerActionTransformerKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.pulsarClient, this.producerConfigurationData, this.schemaHash,
				this.producerActionTransformerKey);
	}

	@Override
	public String toString() {
		return "ProducerCacheKey{" + "pulsarClient=" + this.pulsarClient + ", producerConfigurationData="
				+ this.producerConfigurationData + ", schema=" + this.schema + ", producerActionTransformerKey="
				+ this.producerActionTransformerKey + "}";
	}

}
