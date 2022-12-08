/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.reactive.client.internal.adapter;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.ReaderBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ReactiveReaderAdapter<T> {

	private final Supplier<PulsarClient> pulsarClientSupplier;

	private final Function<PulsarClient, ReaderBuilder<T>> readerBuilderFactory;

	ReactiveReaderAdapter(Supplier<PulsarClient> pulsarClientSupplier,
			Function<PulsarClient, ReaderBuilder<T>> readerBuilderFactory) {
		this.pulsarClientSupplier = pulsarClientSupplier;
		this.readerBuilderFactory = readerBuilderFactory;
	}

	private Mono<Reader<T>> createReaderMono() {
		return AdapterImplementationFactory.adaptPulsarFuture(
				() -> this.readerBuilderFactory.apply(this.pulsarClientSupplier.get()).createAsync());
	}

	private Mono<Void> closeReader(Reader<?> reader) {
		return AdapterImplementationFactory.adaptPulsarFuture(reader::closeAsync);
	}

	<R> Mono<R> usingReader(Function<Reader<T>, Mono<R>> usingReaderAction) {
		return Mono.usingWhen(createReaderMono(), usingReaderAction, this::closeReader);
	}

	<R> Flux<R> usingReaderMany(Function<Reader<T>, Flux<R>> usingReaderAction) {
		return Flux.usingWhen(createReaderMono(), usingReaderAction, this::closeReader);
	}

}
