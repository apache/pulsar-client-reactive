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

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.internal.api.PublisherTransformer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ProducerCache implements ReactiveMessageSenderCache {

	private final ProducerCacheProvider cacheProvider;

	ProducerCache(ProducerCacheProvider cacheProvider) {
		this.cacheProvider = cacheProvider;
	}

	static <T> CompletableFuture<ProducerCacheEntry> createCacheEntry(Mono<Producer<T>> producerMono,
			Supplier<PublisherTransformer> producerActionTransformer) {
		return producerMono.map((producer) -> new ProducerCacheEntry(producer, producerActionTransformer)).toFuture();
	}

	private <T> Mono<ProducerCacheEntry> getProducerCacheEntry(final ProducerCacheKey cacheKey,
			Mono<Producer<T>> producerMono, Supplier<PublisherTransformer> producerActionTransformer) {
		return AdapterImplementationFactory
				.adaptPulsarFuture(() -> this.cacheProvider.getOrCreateCachedEntry(cacheKey,
						(__) -> createCacheEntry(producerMono, producerActionTransformer)))
				.flatMap((producerCacheEntry) -> producerCacheEntry.recreateIfClosed(producerMono));
	}

	<T, R> Mono<R> usingCachedProducer(ProducerCacheKey cacheKey, Mono<Producer<T>> producerMono,
			Supplier<PublisherTransformer> producerActionTransformer,
			BiFunction<Producer<T>, PublisherTransformer, Mono<R>> usingProducerAction) {
		return Mono.usingWhen(this.leaseCacheEntry(cacheKey, producerMono, producerActionTransformer),
				(producerCacheEntry) -> usingProducerAction.apply(producerCacheEntry.getProducer(),
						producerCacheEntry.getProducerActionTransformer()),
				this::returnCacheEntry);
	}

	private Mono<Object> returnCacheEntry(ProducerCacheEntry producerCacheEntry) {
		return Mono.fromRunnable(producerCacheEntry::releaseLease);
	}

	private <T> Mono<ProducerCacheEntry> leaseCacheEntry(ProducerCacheKey cacheKey, Mono<Producer<T>> producerMono,
			Supplier<PublisherTransformer> producerActionTransformer) {
		return this.getProducerCacheEntry(cacheKey, producerMono, producerActionTransformer)
				.doOnNext(ProducerCacheEntry::activateLease);
	}

	<T, R> Flux<R> usingCachedProducerMany(ProducerCacheKey cacheKey, Mono<Producer<T>> producerMono,
			Supplier<PublisherTransformer> producerActionTransformer,
			BiFunction<Producer<T>, PublisherTransformer, Flux<R>> usingProducerAction) {
		return Flux.usingWhen(this.leaseCacheEntry(cacheKey, producerMono, producerActionTransformer),
				(producerCacheEntry) -> usingProducerAction.apply(producerCacheEntry.getProducer(),
						producerCacheEntry.getProducerActionTransformer()),
				this::returnCacheEntry);
	}

	@Override
	public void close() throws Exception {
		if (this.cacheProvider != null) {
			this.cacheProvider.close();
		}
	}

}
