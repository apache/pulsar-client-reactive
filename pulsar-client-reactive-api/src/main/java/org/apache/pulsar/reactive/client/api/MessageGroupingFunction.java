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

import org.apache.pulsar.client.api.Message;

public interface MessageGroupingFunction {

	/**
	 * Resolves a processing group for a message. This is used for implementing
	 * key-ordered message processing with
	 * {@link ReactiveMessagePipelineBuilder.ConcurrentOneByOneMessagePipelineBuilder}
	 * @param message the Pulsar message
	 * @param numberOfGroups maximum number of groups
	 * @return processing group for the message, in the range of 0 to numberOfGroups,
	 * exclusive
	 */
	int resolveProcessingGroup(Message<?> message, int numberOfGroups);

}
