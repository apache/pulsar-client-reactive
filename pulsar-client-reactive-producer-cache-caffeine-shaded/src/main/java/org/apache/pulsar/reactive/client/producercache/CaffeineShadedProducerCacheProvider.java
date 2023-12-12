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

package org.apache.pulsar.reactive.client.producercache;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;
import reactor.core.scheduler.Schedulers;

/**
 * Producer cache provider that uses a shaded Caffeine {@link AsyncCache} to cache
 * entries.
 */
public class CaffeineShadedProducerCacheProvider implements ProducerCacheProvider {

	private final AsyncCache<Object, Object> cache;

	/**
	 * Create a cache provider instance with default values.
	 */
	public CaffeineShadedProducerCacheProvider() {
		this(Duration.ofMinutes(1), Duration.ofMinutes(10), 1000L, 50);
	}

	/**
	 * Create a cache provider instance with the specified options.
	 * @param cacheExpireAfterAccess time period after last access to expire unused
	 * entries in the cache
	 * @param cacheExpireAfterWrite time period after last write to expire unused entries
	 * in the cache
	 * @param cacheMaximumSize maximum size of cache (entries)
	 * @param cacheInitialCapacity the initial size of cache
	 */
	public CaffeineShadedProducerCacheProvider(Duration cacheExpireAfterAccess, Duration cacheExpireAfterWrite,
			Long cacheMaximumSize, Integer cacheInitialCapacity) {
		this.cache = Caffeine.newBuilder()
			.expireAfterAccess(cacheExpireAfterAccess)
			.expireAfterWrite(cacheExpireAfterWrite)
			.maximumSize(cacheMaximumSize)
			.initialCapacity(cacheInitialCapacity)
			.scheduler(Scheduler.systemScheduler())
			.executor(Schedulers.boundedElastic()::schedule)
			.removalListener(this::onRemoval)
			.buildAsync();
	}

	private void onRemoval(Object key, Object entry, RemovalCause cause) {
		if (entry instanceof AutoCloseable) {
			try {
				((AutoCloseable) entry).close();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void close() {
		this.cache.synchronous().invalidateAll();
	}

	@Override
	public <K, V> CompletableFuture<V> getOrCreateCachedEntry(K key,
			Function<K, CompletableFuture<V>> createEntryFunction) {
		return (CompletableFuture<V>) this.cache.get(key,
				(__, ___) -> (CompletableFuture) createEntryFunction.apply(key));
	}

}
