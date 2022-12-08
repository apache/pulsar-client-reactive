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

package org.apache.pulsar.reactive.client.internal.api;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jctools.queues.MpmcArrayQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

/**
 * Transformer class that limits the number of reactive streams subscription requests to
 * keep the number of pending messages under a defined limit.
 *
 * Subscribing to upstream is postponed if the max inflight limit is reached since
 * subscribing to a CompletableFuture is eager. The CompetableFuture will be created at
 * subscription time and this demand cannot be controlled with Reactive Stream's requests.
 * The solution for this is to acquire one slot at subscription time and return this slot
 * when request is made to the subscription. Since it's not possible to backpressure the
 * subscription requests, there's a configurable limit for the total number of pending
 * subscriptions. Exceeding the limit will cause IllegalStateException at runtime.
 */
public class InflightLimiter implements PublisherTransformer {

	/** Default limit for pending Reactive Stream subscriptions. */
	public static final int DEFAULT_MAX_PENDING_SUBSCRIPTIONS = 1024;

	private final MpmcArrayQueue<InflightLimiterSubscriber<?>> pendingSubscriptions;

	private final AtomicInteger inflight = new AtomicInteger();

	private final AtomicInteger activeSubscriptions = new AtomicInteger();

	private final int maxInflight;

	private final int expectedSubscriptionsInflight;

	private final Scheduler.Worker triggerNextWorker;

	private final AtomicBoolean triggerNextTriggered = new AtomicBoolean(false);

	/**
	 * Constructs an InflightLimiter with a maximum number of in-flight messages.
	 * @param maxInflight the maximum number of in-flight messages
	 */
	public InflightLimiter(int maxInflight) {
		this(maxInflight, 0, Schedulers.single(), DEFAULT_MAX_PENDING_SUBSCRIPTIONS);
	}

	/**
	 * Constructs an InflightLimiter.
	 * @param maxInflight the maximum number of in-flight messages
	 * @param expectedSubscriptionsInflight the expected number of in-flight
	 * subscriptions. Will limit the per-subscription requests to maxInflight / max(active
	 * subscriptions, expectedSubscriptionsInflight). Set to 0 to use active subscriptions
	 * in the calculation.
	 * @param triggerNextScheduler the scheduler on which it will be checked if the
	 * subscriber can request more
	 * @param maxPendingSubscriptions the maximum number of pending subscriptions
	 */
	public InflightLimiter(int maxInflight, int expectedSubscriptionsInflight, Scheduler triggerNextScheduler,
			int maxPendingSubscriptions) {
		if (maxInflight < 1) {
			throw new IllegalArgumentException("maxInflight must be greater than 0");
		}
		this.maxInflight = maxInflight;
		this.expectedSubscriptionsInflight = expectedSubscriptionsInflight;
		this.triggerNextWorker = triggerNextScheduler.createWorker();
		if (expectedSubscriptionsInflight > maxInflight) {
			throw new IllegalArgumentException("maxSubscriptionInflight must be equal or less than maxInflight.");
		}
		this.pendingSubscriptions = new MpmcArrayQueue<>(maxPendingSubscriptions);
	}

	@Override
	public <T> Publisher<T> transform(Publisher<T> publisher) {
		if (publisher instanceof Mono<?>) {
			return createOperator((Mono<T>) publisher);
		}
		else {
			return createOperator(Flux.from(publisher));
		}
	}

	<I> Flux<I> createOperator(Flux<I> source) {
		return new FluxOperator<I, I>(source) {
			@Override
			public void subscribe(CoreSubscriber<? super I> actual) {
				handleSubscribe(this.source, actual);
			}
		};
	}

	<I> Mono<I> createOperator(Mono<I> source) {
		return new MonoOperator<I, I>(source) {
			@Override
			public void subscribe(CoreSubscriber<? super I> actual) {
				handleSubscribe(this.source, actual);
			}
		};
	}

	<I> void handleSubscribe(Publisher<I> source, CoreSubscriber<? super I> actual) {
		this.activeSubscriptions.incrementAndGet();
		InflightLimiterSubscriber<I> subscriber = new InflightLimiterSubscriber<>(actual, source);
		actual.onSubscribe(subscriber.getSubscription());
	}

	void maybeTriggerNext() {
		if (!this.triggerNextWorker.isDisposed() && this.inflight.get() < this.maxInflight
				&& !this.pendingSubscriptions.isEmpty()) {
			if (this.triggerNextTriggered.compareAndSet(false, true)) {
				this.triggerNextWorker.schedule(() -> {
					this.triggerNextTriggered.set(false);
					int remainingSubscriptions = this.pendingSubscriptions.size();
					while (this.inflight.get() < this.maxInflight && remainingSubscriptions-- > 0) {
						InflightLimiterSubscriber<?> subscriber = this.pendingSubscriptions.poll();
						if (subscriber != null) {
							if (!subscriber.isDisposed()) {
								subscriber.requestMore();
							}
						}
						else {
							break;
						}
					}
				});
			}
		}
	}

	void scheduleSubscribed(InflightLimiterSubscriber<?> subscriber) {
		if (!this.triggerNextWorker.isDisposed()) {
			this.triggerNextWorker.schedule(() -> {
				if (!subscriber.isDisposed()) {
					subscriber.requestMore();
				}
			});
		}
	}

	@Override
	public void dispose() {
		this.triggerNextWorker.dispose();
		this.pendingSubscriptions.drain(InflightLimiterSubscriber::cancel);
	}

	@Override
	public boolean isDisposed() {
		return this.triggerNextWorker.isDisposed();
	}

	private enum InflightLimiterSubscriberState {

		INITIAL, SUBSCRIBING, SUBSCRIBED, REQUESTING

	}

	private class InflightLimiterSubscriber<I> extends BaseSubscriber<I> {

		private final CoreSubscriber<? super I> actual;

		private final Publisher<I> source;

		private final AtomicLong requestedDemand = new AtomicLong();

		private final AtomicReference<InflightLimiterSubscriberState> state = new AtomicReference<>(
				InflightLimiterSubscriberState.INITIAL);

		private final AtomicInteger inflightForSubscription = new AtomicInteger();

		private final Subscription subscription = new Subscription() {
			@Override
			public void request(long n) {
				if (n == Long.MAX_VALUE) {
					InflightLimiterSubscriber.this.requestedDemand.set(n);
				}
				else if (InflightLimiterSubscriber.this.requestedDemand.get() != Long.MAX_VALUE) {
					InflightLimiterSubscriber.this.requestedDemand.addAndGet(n);
				}
				maybeAddToPending();
				maybeTriggerNext();
			}

			@Override
			public void cancel() {
				InflightLimiterSubscriber.this.cancel();
			}
		};

		InflightLimiterSubscriber(CoreSubscriber<? super I> actual, Publisher<I> source) {
			this.actual = actual;
			this.source = source;
		}

		@Override
		public Context currentContext() {
			return this.actual.currentContext();
		}

		@Override
		protected void hookOnSubscribe(Subscription subscription) {
			if (this.state.compareAndSet(InflightLimiterSubscriberState.SUBSCRIBING,
					InflightLimiterSubscriberState.SUBSCRIBED)) {
				scheduleSubscribed(this);
			}
		}

		@Override
		protected void hookOnNext(I value) {
			this.actual.onNext(value);
			InflightLimiter.this.inflight.decrementAndGet();
			this.inflightForSubscription.decrementAndGet();
			maybeAddToPending();
			maybeTriggerNext();
		}

		@Override
		protected void hookOnComplete() {
			InflightLimiter.this.activeSubscriptions.decrementAndGet();
			this.actual.onComplete();
			clearInflight();
			maybeTriggerNext();
		}

		private void clearInflight() {
			InflightLimiter.this.inflight.addAndGet(-this.inflightForSubscription.getAndSet(0));
		}

		@Override
		protected void hookOnError(Throwable throwable) {
			InflightLimiter.this.activeSubscriptions.decrementAndGet();
			this.actual.onError(throwable);
			clearInflight();
			maybeTriggerNext();
		}

		@Override
		protected void hookOnCancel() {
			InflightLimiter.this.activeSubscriptions.decrementAndGet();
			clearInflight();
			this.requestedDemand.set(0);
			maybeTriggerNext();
		}

		Subscription getSubscription() {
			return this.subscription;
		}

		void requestMore() {
			// spread requests evenly across active subscriptions (or expected number of
			// subscriptions)
			int maxInflightForSubscription = Math
					.max(InflightLimiter.this.maxInflight / Math.max(InflightLimiter.this.activeSubscriptions.get(),
							InflightLimiter.this.expectedSubscriptionsInflight), 1);
			if (this.requestedDemand.get() > 0 && (this.state.get() == InflightLimiterSubscriberState.SUBSCRIBED
					|| (this.inflightForSubscription.get() < maxInflightForSubscription
							&& InflightLimiter.this.inflight.get() < InflightLimiter.this.maxInflight))) {
				if (this.state.compareAndSet(InflightLimiterSubscriberState.INITIAL,
						InflightLimiterSubscriberState.SUBSCRIBING)) {
					// consume one slot for the subscription, since the first element
					// might already be in flight
					// when a CompletableFuture is mapped to a Mono
					InflightLimiter.this.inflight.incrementAndGet();
					this.inflightForSubscription.incrementAndGet();
					this.source.subscribe(InflightLimiterSubscriber.this);
				}
				else if (this.state.get() == InflightLimiterSubscriberState.REQUESTING
						|| this.state.get() == InflightLimiterSubscriberState.SUBSCRIBED) {
					// subscribing changed the values, so adjust back the values on first
					// call
					if (this.state.compareAndSet(InflightLimiterSubscriberState.SUBSCRIBED,
							InflightLimiterSubscriberState.REQUESTING)) {
						// reverse the slot reservation made when transitioning from
						// INITIAL to SUBSCRIBING
						InflightLimiter.this.inflight.decrementAndGet();
						this.inflightForSubscription.decrementAndGet();
					}
					long maxRequest = Math.max(Math.min(this.requestedDemand.get(),
							maxInflightForSubscription - this.inflightForSubscription.get()), 1);
					InflightLimiter.this.inflight.addAndGet((int) maxRequest);
					this.requestedDemand.addAndGet(-maxRequest);
					this.inflightForSubscription.addAndGet((int) maxRequest);
					request(maxRequest);
				}
			}
			else {
				maybeAddToPending();
			}
		}

		void maybeAddToPending() {
			if (this.requestedDemand.get() > 0 && !isDisposed() && this.inflightForSubscription.get() == 0) {
				InflightLimiter.this.pendingSubscriptions.add(this);
			}
		}

	}

}
