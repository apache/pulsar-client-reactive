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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.reactive.client.internal.api.PublisherTransformer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ProducerCacheEntry implements AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(ProducerCacheEntry.class);

	private final AtomicReference<Producer<?>> producer = new AtomicReference<>();

	private final AtomicReference<Mono<? extends Producer<?>>> producerCreator = new AtomicReference<>();

	private final AtomicInteger activeLeases = new AtomicInteger(0);

	private final PublisherTransformer producerActionTransformer;

	private volatile boolean removed;

	ProducerCacheEntry(Producer<?> producer, Supplier<PublisherTransformer> producerActionTransformer) {
		this.producer.set(producer);
		this.producerCreator.set(Mono.fromSupplier(this.producer::get));
		this.producerActionTransformer = (producerActionTransformer != null) ? producerActionTransformer.get()
				: PublisherTransformer.identity();
	}

	private static void flushAndCloseProducerAsync(Producer<?> producer) {
		producer.flushAsync().thenCompose((__) -> producer.closeAsync()).whenComplete((r, t) -> {
			if (t != null) {
				log.error("Error flushing and closing producer", t);
			}
		});
	}

	void activateLease() {
		this.activeLeases.incrementAndGet();
	}

	void releaseLease() {
		int currentLeases = this.activeLeases.decrementAndGet();
		if (currentLeases == 0 && this.removed) {
			cleanupResources();
		}
	}

	<T> Producer<T> getProducer() {
		return (Producer<T>) this.producer.get();
	}

	<T> Mono<ProducerCacheEntry> recreateIfClosed(Mono<Producer<T>> producerMono) {
		return Mono.defer(() -> {
			Producer<?> p = this.producer.get();
			if (p != null) {
				if (p.isConnected()) {
					return Mono.just(this);
				}
				else {
					Mono<? extends Producer<?>> previousUpdater = this.producerCreator.get();
					if (this.producerCreator.compareAndSet(previousUpdater, createCachedProducerMono(producerMono))) {
						this.producer.compareAndSet(p, null);
						flushAndCloseProducerAsync(p);
					}
				}
			}
			return Mono.defer(this.producerCreator::get).filter(Producer::isConnected)
					.repeatWhenEmpty(5, (flux) -> flux.delayElements(Duration.ofSeconds(1))).thenReturn(this);
		});
	}

	private <T> Mono<Producer<T>> createCachedProducerMono(Mono<Producer<T>> producerMono) {
		return producerMono.doOnNext((newProducer) -> {
			log.info("Replaced closed producer for topic {}", newProducer.getTopic());
			this.producer.set(newProducer);
		}).cache();
	}

	@Override
	public void close() {
		this.removed = true;
		if (this.activeLeases.get() == 0) {
			cleanupResources();
		}
	}

	private void cleanupResources() {
		try {
			closeProducer();
		}
		finally {
			this.producerActionTransformer.dispose();
		}
	}

	private void closeProducer() {
		Producer<?> p = this.producer.get();
		if (p != null && this.producer.compareAndSet(p, null)) {
			log.info("Closed producer {} for topic {}", p.getProducerName(), p.getTopic());
			flushAndCloseProducerAsync(p);
		}
	}

	<R> Publisher<? extends R> decorateProducerAction(Flux<R> source) {
		return this.producerActionTransformer.transform(source);
	}

	<R> Mono<? extends R> decorateProducerAction(Mono<R> source) {
		return Mono.from(this.producerActionTransformer.transform(source));
	}

	PublisherTransformer getProducerActionTransformer() {
		return this.producerActionTransformer;
	}

}
