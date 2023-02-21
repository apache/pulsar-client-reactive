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
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReader;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageReader;

/**
 * A Mutiny reader implementation wrapping a {@link ReactiveMessageReader}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageReaderImpl<T> implements MutinyMessageReader<T> {

	private final ReactiveMessageReader<T> reader;

	/**
	 * Create a new {@link MutinyMessageReaderImpl} wrapping the given
	 * {@link ReactiveMessageReader}.
	 * @param reader the reader to wrap
	 */
	MutinyMessageReaderImpl(ReactiveMessageReader<T> reader) {
		this.reader = reader;
	}

	@Override
	public Uni<Message<T>> readOne() {
		return this.reader.readOne().as(Uni.createFrom()::publisher);
	}

	@Override
	public Multi<Message<T>> readMany() {
		return this.reader.readMany().as(Multi.createFrom()::publisher);
	}

}
