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

import java.util.function.Supplier;

import org.apache.pulsar.client.api.PulsarClient;

class ReactivePulsarResourceAdapter {

	private final Supplier<PulsarClient> pulsarClientSupplier;

	ReactivePulsarResourceAdapter(Supplier<PulsarClient> pulsarClientSupplier) {
		this.pulsarClientSupplier = cachedSupplier(pulsarClientSupplier);
	}

	private static <T> Supplier<T> cachedSupplier(Supplier<T> supplier) {
		return new Supplier<T>() {
			T cachedValue;

			@Override
			public synchronized T get() {
				if (this.cachedValue == null) {
					this.cachedValue = supplier.get();
				}
				return this.cachedValue;
			}
		};
	}

	ReactiveProducerAdapterFactory producer() {
		return new ReactiveProducerAdapterFactory(this.pulsarClientSupplier);
	}

	ReactiveConsumerAdapterFactory consumer() {
		return new ReactiveConsumerAdapterFactory(this.pulsarClientSupplier);
	}

	ReactiveReaderAdapterFactory reader() {
		return new ReactiveReaderAdapterFactory(this.pulsarClientSupplier);
	}

}
