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
import org.apache.pulsar.reactive.client.api.EndOfStreamAction;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReader;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.StartAtSpec;

class AdaptedReactiveMessageReaderBuilder<T> implements ReactiveMessageReaderBuilder<T> {

	private final ReactiveReaderAdapterFactory reactiveReaderAdapterFactory;

	private final Schema<T> schema;

	private final MutableReactiveMessageReaderSpec readerSpec;

	private StartAtSpec startAtSpec = StartAtSpec.ofEarliest();

	private EndOfStreamAction endOfStreamAction = EndOfStreamAction.COMPLETE;

	AdaptedReactiveMessageReaderBuilder(Schema<T> schema, ReactiveReaderAdapterFactory reactiveReaderAdapterFactory) {
		this(schema, reactiveReaderAdapterFactory, new MutableReactiveMessageReaderSpec());
	}

	private AdaptedReactiveMessageReaderBuilder(Schema<T> schema,
			ReactiveReaderAdapterFactory reactiveReaderAdapterFactory, MutableReactiveMessageReaderSpec readerSpec) {
		this.reactiveReaderAdapterFactory = reactiveReaderAdapterFactory;
		this.schema = schema;
		this.readerSpec = readerSpec;
	}

	@Override
	public ReactiveMessageReaderBuilder<T> startAtSpec(StartAtSpec startAtSpec) {
		this.startAtSpec = startAtSpec;
		return this;
	}

	@Override
	public ReactiveMessageReaderBuilder<T> endOfStreamAction(EndOfStreamAction endOfStreamAction) {
		this.endOfStreamAction = endOfStreamAction;
		return this;
	}

	@Override
	public ReactiveMessageReaderSpec toImmutableSpec() {
		return new ImmutableReactiveMessageReaderSpec(this.readerSpec);
	}

	@Override
	public MutableReactiveMessageReaderSpec getMutableSpec() {
		return this.readerSpec;
	}

	@Override
	public ReactiveMessageReaderBuilder<T> clone() {
		AdaptedReactiveMessageReaderBuilder<T> cloned = new AdaptedReactiveMessageReaderBuilder<>(this.schema,
				this.reactiveReaderAdapterFactory, new MutableReactiveMessageReaderSpec(this.readerSpec));
		cloned.startAtSpec = this.startAtSpec;
		cloned.endOfStreamAction = this.endOfStreamAction;
		return this;
	}

	@Override
	public ReactiveMessageReader<T> build() {
		return new AdaptedReactiveMessageReader<>(this.reactiveReaderAdapterFactory, this.schema, this.readerSpec,
				this.startAtSpec, this.endOfStreamAction);
	}

}
