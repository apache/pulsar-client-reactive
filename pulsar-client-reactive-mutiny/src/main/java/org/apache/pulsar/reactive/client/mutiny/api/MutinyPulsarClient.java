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

package org.apache.pulsar.reactive.client.mutiny.api;

import org.apache.pulsar.client.api.Schema;

/**
 * A reactive Pulsar client that uses Mutiny.
 */
public interface MutinyPulsarClient {

	/**
	 * Creates a builder for building a {@link MutinyMessageSender}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link MutinyMessageSender}
	 */
	<T> MutinyMessageSenderBuilder<T> messageSender(Schema<T> schema);

	/**
	 * Creates a builder for building a {@link MutinyMessageReader}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link MutinyMessageReader}
	 */
	<T> MutinyMessageReaderBuilder<T> messageReader(Schema<T> schema);

	/**
	 * Creates a builder for building a {@link MutinyMessageConsumer}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link MutinyMessageConsumer}
	 */
	<T> MutinyMessageConsumerBuilder<T> messageConsumer(Schema<T> schema);

}
