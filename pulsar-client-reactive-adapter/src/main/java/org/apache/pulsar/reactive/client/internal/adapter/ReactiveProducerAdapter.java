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

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.ProducerBuilderImpl;
import org.apache.pulsar.reactive.client.internal.api.PublisherTransformer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

class ReactiveProducerAdapter<T> {

	private final ProducerCache producerCache;

	private final Function<PulsarClient, ProducerBuilder<T>> producerBuilderFactory;

	private final Supplier<PulsarClient> pulsarClientSupplier;

	private final Supplier<PublisherTransformer> producerActionTransformer;

	ReactiveProducerAdapter(Supplier<PulsarClient> pulsarClientSupplier,
			Function<PulsarClient, ProducerBuilder<T>> producerBuilderFactory, ProducerCache producerCache,
			Supplier<PublisherTransformer> producerActionTransformer) {
		this.pulsarClientSupplier = pulsarClientSupplier;
		this.producerBuilderFactory = producerBuilderFactory;
		this.producerCache = producerCache;
		this.producerActionTransformer = producerActionTransformer;
	}

	private Mono<Producer<T>> createProducerMono() {
		return AdapterImplementationFactory.adaptPulsarFuture(
				() -> this.producerBuilderFactory.apply(this.pulsarClientSupplier.get()).createAsync());
	}

	private Mono<Tuple2<ProducerCacheKey, Mono<Producer<T>>>> createCachedProducerKeyAndMono() {
		return Mono.fromCallable(() -> {
			PulsarClient pulsarClient = this.pulsarClientSupplier.get();
			ProducerBuilderImpl<T> producerBuilder = (ProducerBuilderImpl<T>) this.producerBuilderFactory
					.apply(pulsarClient);
			ProducerCacheKey cacheKey = new ProducerCacheKey(pulsarClient, producerBuilder.getConf().clone(),
					producerBuilder.getSchema());
			return Tuples.of(cacheKey, AdapterImplementationFactory.adaptPulsarFuture(producerBuilder::createAsync));
		});
	}

	private Mono<Void> closeProducer(Producer<?> producer) {
		return AdapterImplementationFactory.adaptPulsarFuture(producer::closeAsync);
	}

	<R> Mono<R> usingProducer(Function<Producer<T>, Mono<R>> usingProducerAction) {
		if (this.producerCache != null) {
			return usingCachedProducer(usingProducerAction);
		}
		else {
			return usingUncachedProducer(usingProducerAction);
		}
	}

	private <R> Mono<R> usingUncachedProducer(Function<Producer<T>, Mono<R>> usingProducerAction) {
		return Mono.usingWhen(createProducerMono(),
				(producer) -> Mono.using(this.producerActionTransformer::get,
						(transformer) -> usingProducerAction.apply(producer)
								.as((mono) -> Mono.from(transformer.transform(mono))),
						Disposable::dispose),
				this::closeProducer);
	}

	private <R> Mono<R> usingCachedProducer(Function<Producer<T>, Mono<R>> usingProducerAction) {
		return createCachedProducerKeyAndMono().flatMap((keyAndProducerMono) -> {
			ProducerCacheKey cacheKey = keyAndProducerMono.getT1();
			Mono<Producer<T>> producerMono = keyAndProducerMono.getT2();
			return this.producerCache.usingCachedProducer(cacheKey, producerMono, this.producerActionTransformer,
					usingProducerAction);
		});
	}

	<R> Flux<R> usingProducerMany(Function<Producer<T>, Flux<R>> usingProducerAction) {
		if (this.producerCache != null) {
			return usingCachedProducerMany(usingProducerAction);
		}
		else {
			return usingUncachedProducerMany(usingProducerAction);
		}
	}

	private <R> Flux<R> usingUncachedProducerMany(Function<Producer<T>, Flux<R>> usingProducerAction) {
		return Flux.usingWhen(createProducerMono(), (producer) -> Flux.using(this.producerActionTransformer::get,
				(transformer) -> usingProducerAction.apply(producer).as(transformer::transform), Disposable::dispose),
				this::closeProducer);
	}

	private <R> Flux<R> usingCachedProducerMany(Function<Producer<T>, Flux<R>> usingProducerAction) {
		return createCachedProducerKeyAndMono().flatMapMany((keyAndProducerMono) -> {
			ProducerCacheKey cacheKey = keyAndProducerMono.getT1();
			Mono<Producer<T>> producerMono = keyAndProducerMono.getT2();
			return this.producerCache.usingCachedProducerMany(cacheKey, producerMono, this.producerActionTransformer,
					usingProducerAction);
		});
	}

}
