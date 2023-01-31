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

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.reactive.client.api.MessageSendResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageSender;
import org.reactivestreams.Publisher;

/**
 * A Mutiny sender implementation wrapping a {@link ReactiveMessageSender}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageSenderImpl<T> implements MutinyMessageSender<T> {

	private final ReactiveMessageSender<T> sender;

	/**
	 * Create a new {@link MutinyMessageSenderImpl} wrapping the given
	 * {@link ReactiveMessageSender}.
	 * @param sender the sender to wrap
	 */
	MutinyMessageSenderImpl(ReactiveMessageSender<T> sender) {
		this.sender = sender;
	}

	@Override
	public Uni<MessageId> sendOne(MessageSpec<T> messageSpec) {
		return this.sender.sendOne(messageSpec).as(Uni.createFrom()::publisher);
	}

	@Override
	public Multi<MessageSendResult<T>> sendMany(Publisher<MessageSpec<T>> messageSpecs) {
		return this.sender.sendMany(messageSpecs).as(Multi.createFrom()::publisher);
	}

}
