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

import org.apache.pulsar.reactive.client.adapter.ProducerCacheProvider;
import org.apache.pulsar.reactive.client.adapter.ProducerCacheProviderFactory;

/**
 * {@link ProducerCacheProviderFactory} that creates instances of
 * {@link CaffeineProducerCacheProvider}.
 */
public class CaffeineProducerCacheProviderFactory implements ProducerCacheProviderFactory {

	@Override
	public ProducerCacheProvider get() {
		return new CaffeineProducerCacheProvider();
	}

}
