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

package org.apache.pulsar.reactive.client.internal.adapter;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;

class ReactivePulsarResourceAdapterPulsarClient implements ReactivePulsarClient {

	private final ReactivePulsarResourceAdapter reactivePulsarResourceAdapter;

	ReactivePulsarResourceAdapterPulsarClient(ReactivePulsarResourceAdapter reactivePulsarResourceAdapter) {
		this.reactivePulsarResourceAdapter = reactivePulsarResourceAdapter;
	}

	@Override
	public <T> ReactiveMessageReaderBuilder<T> messageReader(Schema<T> schema) {
		return new AdaptedReactiveMessageReaderBuilder<>(schema, this.reactivePulsarResourceAdapter.reader());
	}

	@Override
	public <T> ReactiveMessageSenderBuilder<T> messageSender(Schema<T> schema) {
		return new AdaptedReactiveMessageSenderBuilder<>(schema, this.reactivePulsarResourceAdapter.producer());
	}

	@Override
	public <T> ReactiveMessageConsumerBuilder<T> messageConsumer(Schema<T> schema) {
		return new AdaptedReactiveMessageConsumerBuilder<>(schema, this.reactivePulsarResourceAdapter.consumer());
	}

}
