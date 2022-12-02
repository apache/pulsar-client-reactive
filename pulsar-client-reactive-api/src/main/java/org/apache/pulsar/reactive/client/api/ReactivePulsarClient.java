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

package org.apache.pulsar.reactive.client.api;

import org.apache.pulsar.client.api.Schema;

/**
 * Apache Pulsar Reactive Client interface
 *
 * Contains methods to create builders for {@link ReactiveMessageSender},
 * {@link ReactiveMessageReader} and {@link ReactiveMessageConsumer} instances.
 */
public interface ReactivePulsarClient {

	/**
	 * Creates a builder for building a {@link ReactiveMessageSender}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link ReactiveMessageSender}
	 */
	<T> ReactiveMessageSenderBuilder<T> messageSender(Schema<T> schema);

	/**
	 * Creates a builder for building a {@link ReactiveMessageReader}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link ReactiveMessageReader}
	 */
	<T> ReactiveMessageReaderBuilder<T> messageReader(Schema<T> schema);

	/**
	 * Creates a builder for building a {@link ReactiveMessageConsumer}.
	 * @param schema the Pulsar Java client Schema for the message payload
	 * @param <T> the message payload type
	 * @return a builder for building a {@link ReactiveMessageConsumer}
	 */
	<T> ReactiveMessageConsumerBuilder<T> messageConsumer(Schema<T> schema);

}
