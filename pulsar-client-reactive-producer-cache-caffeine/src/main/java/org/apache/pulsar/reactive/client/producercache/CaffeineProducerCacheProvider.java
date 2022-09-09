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

package org.apache.pulsar.reactive.client.producercache;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;
import reactor.core.scheduler.Schedulers;

public class CaffeineProducerCacheProvider implements ProducerCacheProvider {

	final AsyncCache<Object, Object> cache;

	public CaffeineProducerCacheProvider() {
		this(Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(1)).expireAfterWrite(Duration.ofMinutes(10))
				.maximumSize(1000));
	}

	public CaffeineProducerCacheProvider(CaffeineSpec caffeineSpec) {
		this(Caffeine.from(caffeineSpec));
	}

	public CaffeineProducerCacheProvider(Caffeine<Object, Object> caffeineBuilder) {
		this.cache = caffeineBuilder.scheduler(Scheduler.systemScheduler())
				.executor(Schedulers.boundedElastic()::schedule).removalListener(this::onRemoval).buildAsync();
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
