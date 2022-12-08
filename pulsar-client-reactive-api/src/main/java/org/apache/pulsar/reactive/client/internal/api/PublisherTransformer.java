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

package org.apache.pulsar.reactive.client.internal.api;

import org.reactivestreams.Publisher;
import reactor.core.Disposable;

/**
 * Class to transform a {@link Publisher} to another {@link Publisher}.
 */
public interface PublisherTransformer extends Disposable {

	/**
	 * Gets a {@link PublisherTransformer} that returns the input {@link Publisher}
	 * without modification.
	 * @return the input {@link Publisher} not modified
	 */
	static PublisherTransformer identity() {
		return new PublisherTransformer() {
			@Override
			public void dispose() {
			}

			@Override
			public <T> Publisher<T> transform(Publisher<T> publisher) {
				return publisher;
			}
		};
	}

	/**
	 * Transforms a {@link Publisher} to another {@link Publisher}.
	 * @param publisher the publisher to transform
	 * @param <T> the type of the publisher
	 * @return the transformed publisher
	 */
	<T> Publisher<T> transform(Publisher<T> publisher);

}
