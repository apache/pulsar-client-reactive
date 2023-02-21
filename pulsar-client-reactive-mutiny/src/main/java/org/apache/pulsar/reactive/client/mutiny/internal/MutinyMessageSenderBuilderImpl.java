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

import org.apache.pulsar.reactive.client.api.MutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageSender;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageSenderBuilder;

/**
 * Implementation of {@link MutinyMessageSenderBuilder} wrapping a
 * {@link ReactiveMessageSenderBuilder}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageSenderBuilderImpl<T> implements MutinyMessageSenderBuilder<T> {

	private final ReactiveMessageSenderBuilder<T> senderBuilder;

	MutinyMessageSenderBuilderImpl(ReactiveMessageSenderBuilder<T> senderBuilder) {
		this.senderBuilder = senderBuilder;
	}

	@Override
	public MutinyMessageSenderBuilder<T> cache(ReactiveMessageSenderCache producerCache) {
		this.senderBuilder.cache(producerCache);
		return this;
	}

	@Override
	public MutinyMessageSenderBuilder<T> maxInflight(int maxInflight) {
		this.senderBuilder.maxInflight(maxInflight);
		return this;
	}

	@Override
	public MutinyMessageSenderBuilder<T> maxConcurrentSenderSubscriptions(int maxConcurrentSenderSubscriptions) {
		this.senderBuilder.maxConcurrentSenderSubscriptions(maxConcurrentSenderSubscriptions);
		return this;
	}

	@Override
	public MutinyMessageSenderBuilder<T> stopOnError(boolean stopOnError) {
		this.senderBuilder.stopOnError(stopOnError);
		return this;
	}

	@Override
	public MutinyMessageSenderBuilder<T> clone() {
		return new MutinyMessageSenderBuilderImpl<>(this.senderBuilder.clone());
	}

	@Override
	public MutableReactiveMessageSenderSpec getMutableSpec() {
		return this.senderBuilder.getMutableSpec();
	}

	@Override
	public MutinyMessageSender<T> build() {
		return new MutinyMessageSenderImpl<>(this.senderBuilder.build());
	}

}
