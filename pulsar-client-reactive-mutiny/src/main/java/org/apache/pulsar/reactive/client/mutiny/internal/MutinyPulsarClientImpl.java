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

package org.apache.pulsar.reactive.client.mutiny.internal;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageConsumerBuilder;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageReaderBuilder;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageSenderBuilder;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyPulsarClient;

class MutinyPulsarClientImpl implements MutinyPulsarClient {

	private final ReactivePulsarClient reactiveClient;

	MutinyPulsarClientImpl(ReactivePulsarClient reactiveClient) {
		this.reactiveClient = reactiveClient;
	}

	@Override
	public <T> MutinyMessageSenderBuilder<T> messageSender(Schema<T> schema) {
		return new MutinyMessageSenderBuilderImpl<>(this.reactiveClient.messageSender(schema));
	}

	@Override
	public <T> MutinyMessageReaderBuilder<T> messageReader(Schema<T> schema) {
		return new MutinyMessageReaderBuilderImpl<>(this.reactiveClient.messageReader(schema));
	}

	@Override
	public <T> MutinyMessageConsumerBuilder<T> messageConsumer(Schema<T> schema) {
		return new MutinyMessageConsumerBuilderImpl<>(this.reactiveClient.messageConsumer(schema));
	}

}
