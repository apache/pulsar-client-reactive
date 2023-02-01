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

import org.apache.pulsar.client.api.TypedMessageBuilder;

/**
 * Spec for a message that configures only the value of the message.
 *
 * @param <T> the message payload type
 */
class ValueOnlyMessageSpec<T> implements InternalMessageSpec<T> {

	private final T value;

	ValueOnlyMessageSpec(T value) {
		this.value = value;
	}

	@Override
	public void configure(TypedMessageBuilder<T> typedMessageBuilder) {
		typedMessageBuilder.value(this.value);
	}

	@Override
	public String toString() {
		return "ValueOnlyMessageSpec{" + "value=" + this.value + '}';
	}

	@Override
	public T getValue() {
		return this.value;
	}

}
