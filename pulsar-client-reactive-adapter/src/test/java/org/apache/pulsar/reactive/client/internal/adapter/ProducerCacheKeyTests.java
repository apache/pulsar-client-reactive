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

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ProducerCacheKey}.
 */
class ProducerCacheKeyTests {

	@Test
	void matchShouldTakeIntoAccountPulsarClientField() {
		PulsarClient client = mock(PulsarClient.class);
		ProducerConfigurationData data = new ProducerConfigurationData();
		ProducerCacheKey cacheKey = new ProducerCacheKey(client, data, Schema.STRING, null);
		assertThat(cacheKey).isEqualTo(new ProducerCacheKey(client, data, Schema.STRING, null));
		assertThat(cacheKey).isNotEqualTo(new ProducerCacheKey(mock(PulsarClient.class), data, Schema.STRING, null));
	}

	@Test
	void matchShouldTakeIntoAccountProducerConfigDataField() {
		PulsarClient client = mock(PulsarClient.class);
		ProducerConfigurationData data1 = new ProducerConfigurationData();
		data1.setTopicName("foo");
		ProducerConfigurationData data2 = new ProducerConfigurationData();
		data2.setTopicName("foo");
		ProducerCacheKey cacheKey = new ProducerCacheKey(client, data1, Schema.STRING, null);
		assertThat(cacheKey).isEqualTo(new ProducerCacheKey(client, data1, Schema.STRING, null));
		assertThat(cacheKey).isEqualTo(new ProducerCacheKey(client, data2, Schema.STRING, null));
		data2.setTopicName("bar");
		assertThat(cacheKey).isNotEqualTo(new ProducerCacheKey(client, data2, Schema.STRING, null));
	}

	@Test
	void matchShouldTakeIntoAccountSchemaField() {
		PulsarClient client = mock(PulsarClient.class);
		ProducerConfigurationData data = new ProducerConfigurationData();
		assertThat(new ProducerCacheKey(client, data, Schema.STRING, null))
			.isEqualTo(new ProducerCacheKey(client, data, Schema.STRING, null));
		assertThat(new ProducerCacheKey(client, data, Schema.JSON(Foo.class), null))
			.isEqualTo(new ProducerCacheKey(client, data, Schema.JSON(Foo.class), null));
		assertThat(new ProducerCacheKey(client, data, Schema.JSON(Foo.class), null))
			.isNotEqualTo(new ProducerCacheKey(client, data, Schema.JSON(Bar.class), null));
		assertThat(new ProducerCacheKey(client, data, Schema.STRING, null))
			.isNotEqualTo(new ProducerCacheKey(client, data, Schema.JSON(Foo.class), null));
	}

	@Test
	void matchShouldTakeIntoAccountTransformerKeyField() {
		PulsarClient client = mock(PulsarClient.class);
		ProducerConfigurationData data = new ProducerConfigurationData();
		assertThat(new ProducerCacheKey(client, data, Schema.STRING, null))
			.isEqualTo(new ProducerCacheKey(client, data, Schema.STRING, null));
		assertThat(new ProducerCacheKey(client, data, Schema.STRING, "a"))
			.isEqualTo(new ProducerCacheKey(client, data, Schema.STRING, "a"));
		assertThat(new ProducerCacheKey(client, data, Schema.STRING, "a"))
			.isNotEqualTo(new ProducerCacheKey(client, data, Schema.STRING, "b"));
	}

	static class Foo {

		private String id;

		Foo(String id) {
			this.id = id;
		}

		String getId() {
			return this.id;
		}

	}

	static class Bar {

		private String id;

		Bar(String id) {
			this.id = id;
		}

		String getId() {
			return this.id;
		}

	}

}
