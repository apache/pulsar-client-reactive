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

import org.apache.pulsar.reactive.client.internal.api.ApiImplementationFactory;

/**
 * Interface for the spec of a message to be sent.
 *
 * @param <T> the message payload type
 */
public interface MessageSpec<T> {

	/**
	 * Creates a message spec builder from a value.
	 * @param <T> the message payload type
	 * @param value the value to create the message spec builder from
	 * @return the message spec builder
	 */
	static <T> MessageSpecBuilder<T> builder(T value) {
		return ApiImplementationFactory.createMessageSpecBuilder(value);
	}

	/**
	 * Creates a message spec from a value.
	 * @param <T> the message payload type
	 * @param value the value to create the message spec from
	 * @return the message spec
	 */
	static <T> MessageSpec<T> of(T value) {
		return ApiImplementationFactory.createValueOnlyMessageSpec(value);
	}

}
