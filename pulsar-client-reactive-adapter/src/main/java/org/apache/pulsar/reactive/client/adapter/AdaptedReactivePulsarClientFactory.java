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

package org.apache.pulsar.reactive.client.adapter;

import java.util.ServiceLoader;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import org.apache.pulsar.reactive.client.internal.adapter.AdapterImplementationFactory;
import org.apache.pulsar.reactive.client.internal.adapter.ConcurrentHashMapProducerCacheProvider;

/**
 * Class to create {@link ReactivePulsarClient} and {@link ReactiveMessageSenderCache}.
 * instances.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public final class AdaptedReactivePulsarClientFactory {

	private AdaptedReactivePulsarClientFactory() {

	}

	/**
	 * Creates a ReactivePulsarClient by wrapping an existing PulsarClient instance.
	 * @param pulsarClient the Pulsar Client instance to use
	 * @return a ReactivePulsarClient instance
	 */
	public static ReactivePulsarClient create(PulsarClient pulsarClient) {
		return create(() -> pulsarClient);
	}

	/**
	 * Creates a ReactivePulsarClient which will lazily call the provided supplier to get
	 * an instance of a Pulsar Client when needed.
	 * @param pulsarClientSupplier the supplier to use for getting a Pulsar Client
	 * instance
	 * @return a ReactivePulsarClient instance
	 */
	public static ReactivePulsarClient create(Supplier<PulsarClient> pulsarClientSupplier) {
		return AdapterImplementationFactory.createReactivePulsarClient(pulsarClientSupplier);
	}

	/**
	 * Creates a {@link ReactiveMessageSenderCache} adapting the provided
	 * {@link ProducerCacheProvider}.
	 * @param producerCacheProvider a ProducerCacheProvider instance
	 * @return a ReactiveMessageSenderCache instance
	 */
	public static ReactiveMessageSenderCache createCache(ProducerCacheProvider producerCacheProvider) {
		return AdapterImplementationFactory.createCache(producerCacheProvider);
	}

	/**
	 * Creates a {@link ReactiveMessageSenderCache}. If a
	 * {@link ProducerCacheProviderFactory} can be loaded by the {@link ServiceLoader}, it
	 * is used to supply a {@link ProducerCacheProvider} from which the
	 * {@link ReactiveMessageSenderCache} is adapted. Otherwise, the
	 * {@link ReactiveMessageSenderCache} is adapted from a new
	 * {@link ConcurrentHashMapProducerCacheProvider} instance.
	 * @return a ReactiveMessageSenderCache instance
	 */
	public static ReactiveMessageSenderCache createCache() {
		return AdapterImplementationFactory.createCache();
	}

}
