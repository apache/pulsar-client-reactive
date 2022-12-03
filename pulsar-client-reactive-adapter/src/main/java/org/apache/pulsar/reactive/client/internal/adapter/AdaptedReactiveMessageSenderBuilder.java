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

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderBuilder;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderCache;
import org.apache.pulsar.reactive.client.internal.api.InflightLimiter;
import org.apache.pulsar.reactive.client.internal.api.PublisherTransformer;
import reactor.core.scheduler.Schedulers;

class AdaptedReactiveMessageSenderBuilder<T> implements ReactiveMessageSenderBuilder<T> {

	private static final int MAX_CONCURRENCY_LOWER_BOUND = 32;

	private static final int MAX_CONCURRENCY_UPPER_BOUND = 256;

	private final Schema<T> schema;

	private final ReactiveProducerAdapterFactory reactiveProducerAdapterFactory;

	private final MutableReactiveMessageSenderSpec senderSpec;

	private ReactiveMessageSenderCache producerCache;

	private int maxInflight = -1;

	private int maxConcurrentSenderSubscriptions = InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS;

	private Supplier<PublisherTransformer> producerActionTransformer = PublisherTransformer::identity;

	AdaptedReactiveMessageSenderBuilder(Schema<T> schema,
			ReactiveProducerAdapterFactory reactiveProducerAdapterFactory) {
		this(schema, reactiveProducerAdapterFactory, new MutableReactiveMessageSenderSpec());
	}

	private AdaptedReactiveMessageSenderBuilder(Schema<T> schema,
			ReactiveProducerAdapterFactory reactiveProducerAdapterFactory,
			MutableReactiveMessageSenderSpec senderSpec) {
		this.schema = schema;
		this.reactiveProducerAdapterFactory = reactiveProducerAdapterFactory;
		this.senderSpec = senderSpec;
	}

	@Override
	public ReactiveMessageSenderBuilder<T> cache(ReactiveMessageSenderCache producerCache) {
		this.producerCache = producerCache;
		return this;
	}

	@Override
	public MutableReactiveMessageSenderSpec getMutableSpec() {
		return this.senderSpec;
	}

	@Override
	public ReactiveMessageSenderBuilder<T> maxInflight(int maxInflight) {
		this.maxInflight = maxInflight;
		return this;
	}

	@Override
	public ReactiveMessageSenderBuilder<T> maxConcurrentSenderSubscriptions(int maxConcurrentSenderSubscriptions) {
		this.maxConcurrentSenderSubscriptions = maxConcurrentSenderSubscriptions;
		return this;
	}

	@Override
	public ReactiveMessageSenderBuilder<T> clone() {
		AdaptedReactiveMessageSenderBuilder<T> cloned = new AdaptedReactiveMessageSenderBuilder<>(this.schema,
				this.reactiveProducerAdapterFactory, new MutableReactiveMessageSenderSpec(this.senderSpec));
		cloned.producerCache = this.producerCache;
		cloned.maxInflight = this.maxInflight;
		cloned.maxConcurrentSenderSubscriptions = this.maxConcurrentSenderSubscriptions;
		cloned.producerActionTransformer = this.producerActionTransformer;
		return cloned;
	}

	@Override
	public ReactiveMessageSender<T> build() {
		Object producerActionTransformerKey;
		if (this.maxInflight > 0) {
			this.producerActionTransformer = () -> new InflightLimiter(this.maxInflight,
					Math.max(this.maxInflight / 2, 1), Schedulers.single(), this.maxConcurrentSenderSubscriptions);
			producerActionTransformerKey = new ProducerActionTransformerKey(this.maxInflight,
					this.maxConcurrentSenderSubscriptions);
		}
		else {
			producerActionTransformerKey = null;
		}
		return new AdaptedReactiveMessageSender<>(this.schema, this.senderSpec, resolveMaxConcurrency(),
				this.reactiveProducerAdapterFactory, (ProducerCache) this.producerCache, this.producerActionTransformer,
				producerActionTransformerKey);
	}

	private int resolveMaxConcurrency() {
		return Math.min(Math.max(MAX_CONCURRENCY_LOWER_BOUND, this.maxInflight / 2), MAX_CONCURRENCY_UPPER_BOUND);
	}

	private static class ProducerActionTransformerKey {

		private final int maxInflight;

		private final int maxConcurrentSenderSubscriptions;

		ProducerActionTransformerKey(int maxInflight, int maxConcurrentSenderSubscriptions) {
			this.maxInflight = maxInflight;
			this.maxConcurrentSenderSubscriptions = maxConcurrentSenderSubscriptions;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ProducerActionTransformerKey that = (ProducerActionTransformerKey) o;
			return this.maxInflight == that.maxInflight
					&& this.maxConcurrentSenderSubscriptions == that.maxConcurrentSenderSubscriptions;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.maxInflight, this.maxConcurrentSenderSubscriptions);
		}

	}

}
