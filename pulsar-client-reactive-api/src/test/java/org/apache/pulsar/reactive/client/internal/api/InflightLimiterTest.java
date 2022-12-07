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

package org.apache.pulsar.reactive.client.internal.api;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler.Worker;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import static org.assertj.core.api.Assertions.assertThat;

class InflightLimiterTest {

	@ParameterizedTest
	@CsvSource({ "7,100", "13,100", "37,500", "51,1000" })
	void shouldNotRequestOrSubscribeMoreThanMaxInflightForMonos(int maxInflight, int maxElements) {
		InflightLimiter inflightLimiter = new InflightLimiter(maxInflight, maxInflight, Schedulers.single(),
				InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS);

		AtomicLong totalRequests = new AtomicLong();
		AtomicLong requestsMax = new AtomicLong();
		AtomicInteger subscriptionsActiveBeforeCompletingFirstElement = new AtomicInteger();
		AtomicInteger subscriptionsMax = new AtomicInteger();

		List<Integer> inputValues = IntStream.rangeClosed(1, maxElements).boxed().collect(Collectors.toList());

		Worker worker = Schedulers.boundedElastic().createWorker();
		try {
			Flux<Integer> flux = Flux.fromIterable(inputValues).flatMap((i) -> {
				AtomicLong currentRequests = new AtomicLong();
				return Mono.delay(Duration.ofMillis(25L)).thenReturn(i).doOnSubscribe((subscription) -> {
					worker.schedule(() -> {
						int currentSubscriptionsCount = subscriptionsActiveBeforeCompletingFirstElement
								.incrementAndGet();
						subscriptionsMax.accumulateAndGet(currentSubscriptionsCount, Math::max);
					});
				}).doOnRequest((requested) -> {
					worker.schedule(() -> {
						currentRequests.addAndGet(requested);
						long current = totalRequests.addAndGet(requested);
						requestsMax.accumulateAndGet(current, Math::max);
					});
				}).doOnNext((__) -> {
					worker.schedule(() -> {
						subscriptionsActiveBeforeCompletingFirstElement.decrementAndGet();
						// Mono will complete after this element, so reduce all remaining
						// requests;
						totalRequests.addAndGet(-currentRequests.getAndSet(0));
					});
				}).transform(inflightLimiter::transform).subscribeOn(Schedulers.single());
			}, maxElements);
			List<Integer> values = flux.collectList().block();
			SoftAssertions.assertSoftly((softly) -> {
				softly.assertThat(values).containsExactlyInAnyOrderElementsOf(inputValues);
				softly.assertThat(requestsMax.get()).as("requestsMax").isEqualTo(maxInflight);
				softly.assertThat(subscriptionsMax.get()).as("subscriptionsMax").isEqualTo(maxInflight);
			});
		}
		finally {
			worker.dispose();
		}
	}

	@ParameterizedTest
	@CsvSource({ "7,100", "13,100", "37,500", "51,1000" })
	void shouldNotSubscribeMoreThanMaxInflightForMonos(int maxInflight, int maxElements) {
		InflightLimiter inflightLimiter = new InflightLimiter(maxInflight, maxInflight, Schedulers.single(),
				InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS);

		AtomicInteger completableFuturesInflight = new AtomicInteger();
		AtomicInteger completableFuturesInflightMax = new AtomicInteger();

		List<Integer> inputValues = IntStream.rangeClosed(1, maxElements).boxed().collect(Collectors.toList());

		Flux<Integer> flux = Flux.fromIterable(inputValues).flatMap((i) -> Mono.fromCompletionStage(() -> {
			completableFuturesInflightMax.accumulateAndGet(completableFuturesInflight.incrementAndGet(), Math::max);
			CompletableFuture<Integer> future = new CompletableFuture<>();
			Schedulers.boundedElastic().schedule(() -> {
				completableFuturesInflight.decrementAndGet();
				future.complete(i);
			}, 5, TimeUnit.MILLISECONDS);
			return future;
		}).transform(inflightLimiter::transform).subscribeOn(Schedulers.parallel()), maxElements);

		List<Integer> values = flux.collectList().block();
		assertThat(values).containsExactlyInAnyOrderElementsOf(inputValues);
		assertThat(completableFuturesInflightMax.get()).isEqualTo(maxInflight);
	}

	@Disabled("This test was used to debug the flakiness issue")
	@RepeatedTest(100)
	void repeatShouldNotSubscribeMoreThanMaxInflightForMonos() {
		shouldNotSubscribeMoreThanMaxInflightForMonos(7, 100);
		shouldNotRequestOrSubscribeMoreThanMaxInflightForMonos(7, 100);
		shouldNotRequestOrSubscribeMoreThanMaxInflightForFluxes(7, 100);
	}

	@ParameterizedTest
	@CsvSource({ "7,100", "13,100", "37,500", "51,1000" })
	void shouldNotRequestOrSubscribeMoreThanMaxInflightForFluxes(int maxInflight, int maxElements) {
		int subfluxSize = 3;

		InflightLimiter inflightLimiter = new InflightLimiter(maxInflight, maxInflight, Schedulers.single(),
				InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS);

		AtomicLong totalRequests = new AtomicLong();
		AtomicLong requestsMax = new AtomicLong();
		AtomicInteger subscriptionsActiveBeforeCompletingFirstElement = new AtomicInteger();
		AtomicInteger subscriptionsMax = new AtomicInteger();

		List<Integer> inputValues = IntStream.rangeClosed(1, maxElements).boxed().collect(Collectors.toList());

		Flux<Integer> flux = Flux.fromIterable(inputValues).window(subfluxSize).flatMap((subFlux) -> {
			AtomicLong currentRequests = new AtomicLong();
			return subFlux.flatMap((i) -> Mono.delay(Duration.ofMillis(25L)).thenReturn(i)).index()
					.doOnSubscribe((subscription) -> {
						int currentSubscriptionsCount = subscriptionsActiveBeforeCompletingFirstElement
								.incrementAndGet();
						subscriptionsMax.accumulateAndGet(currentSubscriptionsCount, Math::max);
					}).doOnRequest((requested) -> {
						currentRequests.addAndGet(requested);
						long current = totalRequests.addAndGet(requested);
						requestsMax.accumulateAndGet(current, Math::max);
					}).doOnNext((indexed) -> {
						currentRequests.decrementAndGet();
						totalRequests.decrementAndGet();
						if (indexed.getT1() == 0L) {
							subscriptionsActiveBeforeCompletingFirstElement.decrementAndGet();
						}
					}).doFinally((__) -> totalRequests.addAndGet(-currentRequests.getAndSet(0)))
					.transform(inflightLimiter::transform).subscribeOn(Schedulers.parallel());
		}, maxElements).map(Tuple2::getT2);

		List<Integer> values = flux.collectList().block();
		assertThat(values).containsExactlyInAnyOrderElementsOf(inputValues);
		assertThat(requestsMax.get()).isEqualTo(maxInflight);
		assertThat(subscriptionsMax.get()).isEqualTo(maxInflight);
	}

	@Test
	void shouldSpreadRequestsEvenlyAcrossUpstream() {
		InflightLimiter inflightLimiter = new InflightLimiter(1, 0, Schedulers.single(),
				InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS);
		List<Integer> values = Flux
				.merge(100, Flux.range(1, 100).delayElements(Duration.ofMillis(3)).as(inflightLimiter::createOperator),
						Flux.range(101, 100).delayElements(Duration.ofMillis(3)).as(inflightLimiter::createOperator),
						Flux.range(201, 100).delayElements(Duration.ofMillis(3)).as(inflightLimiter::createOperator))
				.collectList().block();
		assertThat(values).containsExactlyInAnyOrderElementsOf(
				IntStream.rangeClosed(1, 300).boxed().collect(Collectors.toList()));
		for (int i = 0; i < 100; i = i + 3) {
			assertThat(new int[] { values.get(i), values.get(i + 1), values.get(i + 2) })
					.containsExactly(values.get(i), values.get(i) + 100, values.get(i) + 200)
					.as("values at index %d-%d", i, i + 2);
		}
	}

}
