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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class InflightLimiterTest {

	@Test
	void shouldLimitInflight() {
		List<Integer> values = Collections.synchronizedList(new ArrayList<>());
		InflightLimiter inflightLimiter = new InflightLimiter(48, 24, Schedulers.single(),
				InflightLimiter.DEFAULT_MAX_PENDING_SUBSCRIPTIONS);
		Flux.merge(Arrays.asList(
				Flux.range(1, 100).publishOn(Schedulers.parallel()).log().as(inflightLimiter::createOperator),
				Flux.range(101, 100).publishOn(Schedulers.parallel()).log().as(inflightLimiter::createOperator),
				Flux.range(201, 100).publishOn(Schedulers.parallel()).log().as(inflightLimiter::createOperator)))
				.as(StepVerifier::create).expectSubscription().recordWith(() -> values).expectNextCount(300)
				.expectComplete().verify();
		assertThat(values)
				.containsExactlyInAnyOrderElementsOf(IntStream.range(1, 301).boxed().collect(Collectors.toList()));
		// verify "fairness"
		// TODO: this is flaky, fix it
		// verifyFairness(values);
	}

	private void verifyFairness(List<Integer> values) {
		int previousValue = 1;
		for (int i = 0; i < values.size(); i++) {
			int value = values.get(i) % 100;
			if (value == 0) {
				value = 100;
			}
			assertThat(Math.abs(previousValue - value)).as("value %d at index %d", values.get(i), i)
					.isLessThanOrEqualTo(48);
			previousValue = value;
		}
	}

}
