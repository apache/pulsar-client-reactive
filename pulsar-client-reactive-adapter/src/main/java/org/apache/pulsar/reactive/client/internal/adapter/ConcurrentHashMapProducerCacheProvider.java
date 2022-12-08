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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;

/**
 * Producer cache provider that uses a {@link ConcurrentHashMap} to cache entries.
 */
public class ConcurrentHashMapProducerCacheProvider implements ProducerCacheProvider {

	private final ConcurrentHashMap<Object, CompletableFuture<Object>> cache;

	/**
	 * ConcurrentHashMapProducerCacheProvider's constructor.
	 */
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
	public void close() {
		for (CompletableFuture<Object> future : this.cache.values()) {
			future.thenAccept((value) -> {
				if (value instanceof AutoCloseable) {
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
