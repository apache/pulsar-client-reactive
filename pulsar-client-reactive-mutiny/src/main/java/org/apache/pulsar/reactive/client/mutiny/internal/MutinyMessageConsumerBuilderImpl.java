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

import org.apache.pulsar.reactive.client.api.MutableReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerBuilder;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageConsumer;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageConsumerBuilder;

/**
 * Implementation of {@link MutinyMessageConsumerBuilder} wrapping a
 * {@link ReactiveMessageConsumerBuilder}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageConsumerBuilderImpl<T> implements MutinyMessageConsumerBuilder<T> {

	private final ReactiveMessageConsumerBuilder<T> consumerBuilder;

	MutinyMessageConsumerBuilderImpl(ReactiveMessageConsumerBuilder<T> consumerBuilder) {
		this.consumerBuilder = consumerBuilder;
	}

	@Override
	public MutinyMessageConsumerBuilder<T> clone() {
		return new MutinyMessageConsumerBuilderImpl<>(this.consumerBuilder.clone());
	}

	@Override
	public MutinyMessageConsumer<T> build() {
		return new MutinyMessageConsumerImpl<>(this.consumerBuilder.build());
	}

	@Override
	public MutableReactiveMessageConsumerSpec getMutableSpec() {
		return this.consumerBuilder.getMutableSpec();
	}

}
