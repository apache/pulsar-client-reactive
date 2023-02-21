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

import org.apache.pulsar.reactive.client.api.EndOfStreamAction;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderBuilder;
import org.apache.pulsar.reactive.client.api.StartAtSpec;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageReader;
import org.apache.pulsar.reactive.client.mutiny.api.MutinyMessageReaderBuilder;

/**
 * Implementation of {@link MutinyMessageReaderBuilder} wrapping a
 * {@link ReactiveMessageReaderBuilder}.
 *
 * @param <T> the type of the messages
 */
class MutinyMessageReaderBuilderImpl<T> implements MutinyMessageReaderBuilder<T> {

	private final ReactiveMessageReaderBuilder<T> readerBuilder;

	MutinyMessageReaderBuilderImpl(ReactiveMessageReaderBuilder<T> readerBuilder) {
		this.readerBuilder = readerBuilder;
	}

	@Override
	public MutableReactiveMessageReaderSpec getMutableSpec() {
		return this.readerBuilder.getMutableSpec();
	}

	@Override
	public MutinyMessageReaderBuilder<T> startAtSpec(StartAtSpec startAtSpec) {
		this.readerBuilder.startAtSpec(startAtSpec);
		return this;
	}

	@Override
	public MutinyMessageReaderBuilder<T> endOfStreamAction(EndOfStreamAction endOfStreamAction) {
		this.readerBuilder.endOfStreamAction(endOfStreamAction);
		return this;
	}

	@Override
	public MutinyMessageReaderBuilder<T> clone() {
		return new MutinyMessageReaderBuilderImpl<>(this.readerBuilder.clone());
	}

	@Override
	public MutinyMessageReader<T> build() {
		return new MutinyMessageReaderImpl<>(this.readerBuilder.build());
	}

}
