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

import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.reactive.client.api.MessageSpec;

/**
 * Internal interface for the spec of a message to be sent.
 *
 * @param <T> the message payload type
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface InternalMessageSpec<T> extends MessageSpec<T> {

	/**
	 * Configures the message from this message spec.
	 * @param typedMessageBuilder the message to configure
	 */
	void configure(TypedMessageBuilder<T> typedMessageBuilder);

}
