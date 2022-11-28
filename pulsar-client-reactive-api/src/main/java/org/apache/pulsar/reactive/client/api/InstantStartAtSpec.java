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

package org.apache.pulsar.reactive.client.api;

import java.time.Instant;
import java.util.Objects;

/**
 * Spec for a {@link ReactiveMessageReader} to start reading from a given instant in time.
 *
 * @author Lari Hotari
 */
public final class InstantStartAtSpec extends StartAtSpec {

	private final Instant instant;

	public InstantStartAtSpec(final Instant instant) {
		this.instant = instant;
	}

	public Instant getInstant() {
		return this.instant;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		InstantStartAtSpec that = (InstantStartAtSpec) o;
		return Objects.equals(this.instant, that.instant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.instant);
	}

	@Override
	public String toString() {
		return "InstantStartAtSpec{" + "instant=" + this.instant + '}';
	}

}
