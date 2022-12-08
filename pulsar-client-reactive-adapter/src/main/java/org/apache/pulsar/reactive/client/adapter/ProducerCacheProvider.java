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

package org.apache.pulsar.reactive.client.adapter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;

/**
 * Cache provider interface used by the {@link ReactiveMessageSender} to cache the
 * underlying {@link Producer}s it uses.
 */
public interface ProducerCacheProvider extends AutoCloseable {

	/**
	 * Gets or create an entry in the cache.
	 * @param key the cache key
	 * @param <K> the type of the cache key
	 * @param createEntryFunction a function called to create the entry in the cache if
	 * it's not present.
	 * @param <V> the type of the cache entry value
	 * @return the cache entry value
	 */
	<K, V> CompletableFuture<V> getOrCreateCachedEntry(K key, Function<K, CompletableFuture<V>> createEntryFunction);

}
