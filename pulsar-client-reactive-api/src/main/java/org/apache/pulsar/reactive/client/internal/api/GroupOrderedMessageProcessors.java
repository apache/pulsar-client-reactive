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

import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.reactive.client.api.MessageGroupingFunction;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.scheduler.Scheduler;
import reactor.util.concurrent.Queues;

/**
 * Functions for implementing In-order parallel processing for Pulsar messages using
 * Project Reactor.
 * <p>
 * A processing group is resolved for each message based on the message's key. The message
 * flux is split into group fluxes based on the processing group. Each group flux is
 * processes messages in order (one-by-one). Multiple group fluxes are processed in
 * parallel.
 */
public final class GroupOrderedMessageProcessors {

	private GroupOrderedMessageProcessors() {

	}

	/**
	 * Splits the flux of messages by message key into the given number of groups.
	 * @param <T> message payload type
	 * @param messageFlux flux of messages
	 * @param groupingFunction function to use for resolving the group for a message
	 * @param numberOfGroups number of processing groups
	 * @return the grouped flux of messages
	 */
	public static <T> Flux<GroupedFlux<Integer, Message<T>>> groupByProcessingGroup(Flux<Message<T>> messageFlux,
			MessageGroupingFunction groupingFunction, int numberOfGroups) {
		return messageFlux.groupBy((message) -> groupingFunction.resolveProcessingGroup(message, numberOfGroups),
				Math.max(Queues.XS_BUFFER_SIZE, numberOfGroups));
	}

	/**
	 * Processes the messages concurrently with the targeted concurrency. Uses ".flatMap"
	 * in the implementation
	 * @param <T> message payload type
	 * @param <R> message handler's resulting type
	 * @param messageFlux the flux of messages
	 * @param groupingFunction function to use for resolving the group for a message
	 * @param messageHandler message handler function
	 * @param scheduler scheduler to use for subscribing to inner publishers
	 * @param concurrency targeted concurrency level
	 * @return flux of message handler results
	 */
	public static <T, R> Flux<R> processGroupsInOrderConcurrently(Flux<Message<T>> messageFlux,
			MessageGroupingFunction groupingFunction,
			Function<? super Message<T>, ? extends Publisher<? extends R>> messageHandler, Scheduler scheduler,
			int concurrency) {
		return groupByProcessingGroup(messageFlux, groupingFunction, concurrency)
				.flatMap((groupedFlux) -> groupedFlux.publishOn(scheduler).concatMap(messageHandler), concurrency);
	}

	/**
	 * Processes the messages in parallel with the targeted parallelism. Uses ".parallel"
	 * in the implementation
	 * @param <T> message payload type
	 * @param <R> message handler's resulting type
	 * @param messageFlux the flux of messages
	 * @param groupingFunction function to use for resolving the group for a message
	 * @param messageHandler message handler function
	 * @param scheduler scheduler to use for subscribing to inner publishers
	 * @param parallelism targeted level of parallelism
	 * @return flux of message handler results
	 */
	public static <T, R> Flux<R> processGroupsInOrderInParallel(Flux<Message<T>> messageFlux,
			MessageGroupingFunction groupingFunction,
			Function<? super Message<T>, ? extends Publisher<? extends R>> messageHandler, Scheduler scheduler,
			int parallelism) {
		return groupByProcessingGroup(messageFlux, groupingFunction, parallelism).parallel(parallelism).runOn(scheduler)
				.flatMap((groupedFlux) -> groupedFlux.concatMap(messageHandler)).sequential();
	}

}
