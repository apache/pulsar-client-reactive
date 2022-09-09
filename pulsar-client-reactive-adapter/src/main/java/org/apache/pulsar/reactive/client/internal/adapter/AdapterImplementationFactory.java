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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProviderFactory;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import reactor.core.publisher.Mono;

public final class AdapterImplementationFactory {

	private AdapterImplementationFactory() {

	}

	private static final ProducerCacheProviderFactory PRODUCER_CACHE_PROVIDER_FACTORY;

	static {
		Iterator<ProducerCacheProviderFactory> iterator = ServiceLoader.load(ProducerCacheProviderFactory.class)
				.iterator();
		if (iterator.hasNext()) {
			PRODUCER_CACHE_PROVIDER_FACTORY = iterator.next();
		}
		else {
			PRODUCER_CACHE_PROVIDER_FACTORY = null;
		}
	}

	public static ReactivePulsarClient createReactivePulsarClient(Supplier<PulsarClient> pulsarClientSupplier) {
		return new ReactivePulsarResourceAdapterPulsarClient(new ReactivePulsarResourceAdapter(pulsarClientSupplier));
	}

	public static <T> Mono<T> adaptPulsarFuture(Supplier<? extends CompletableFuture<T>> futureSupplier) {
		return PulsarFutureAdapter.adaptPulsarFuture(futureSupplier);
	}

	public static ReactiveMessageSenderCache createCache(ProducerCacheProvider producerCacheProvider) {
		return new ProducerCache(producerCacheProvider);
	}

	public static ReactiveMessageSenderCache createCache() {
		return new ProducerCache((PRODUCER_CACHE_PROVIDER_FACTORY != null) ? PRODUCER_CACHE_PROVIDER_FACTORY.get()
				: new ConcurrentHashMapProducerCacheProvider());
	}

}
