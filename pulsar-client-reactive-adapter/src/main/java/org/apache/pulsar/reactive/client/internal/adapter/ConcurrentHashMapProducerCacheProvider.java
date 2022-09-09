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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;

public class ConcurrentHashMapProducerCacheProvider implements ProducerCacheProvider {

	private final ConcurrentHashMap<Object, CompletableFuture<Object>> cache;

	public ConcurrentHashMapProducerCacheProvider() {
		this.cache = new ConcurrentHashMap<>();
	}

	@Override
	public <K, V> CompletableFuture<V> getOrCreateCachedEntry(K key,
			Function<K, CompletableFuture<V>> createEntryFunction) {
		return (CompletableFuture<V>) this.cache.computeIfAbsent(key,
				(__) -> (CompletableFuture) createEntryFunction.apply(key));
	}

	@Override
	public void close() throws Exception {
		for (CompletableFuture<Object> future : this.cache.values()) {
			future.thenAccept((value) -> {
				if (value != null && value instanceof AutoCloseable) {
					try {
						((AutoCloseable) value).close();
					}
					catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			});
			this.cache.clear();
		}
	}

}
