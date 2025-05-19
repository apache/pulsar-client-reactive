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

package org.apache.pulsar.reactive.client.api;

import reactor.core.publisher.Mono;

/**
 * Reactive message pipeline interface.
 */
public interface ReactiveMessagePipeline extends AutoCloseable {

	/**
	 * Starts the reactive pipeline asynchronously.
	 *
	 * @see #untilConsumingStarted() For returning a reactive publisher (Mono) that
	 * completes after consuming has actually started.
	 * @return the pipeline
	 */
	ReactiveMessagePipeline start();

	/**
	 * Stops the reactive pipeline asynchronously.
	 * @see #untilConsumingStopped() For returning a reactive publisher (Mono) that
	 * completes after consuming has actually stopped.
	 * @return the reactive pipeline
	 */
	ReactiveMessagePipeline stop();

	/**
	 * Gets whether the reactive pipeline is running.
	 * @return true if the reactive pipeline is running
	 */
	boolean isRunning();

	/**
	 * Closes the reactive pipeline asynchronously without waiting for shutdown
	 * completion.
	 * @throws Exception if an error occurs
	 */
	default void close() throws Exception {
		stop();
	}

	/**
	 * <p>
	 * Returns a reactive publisher (Mono) that completes after the pipeline has
	 * successfully subscribed to the input topic(s) and started consuming messages for
	 * the first time after pipeline creation. This method is not intended to be used
	 * after a pipeline restarts following failure. Use this method to wait for consumer
	 * and Pulsar subscription creation. This helps avoid race conditions when sending
	 * messages immediately after the pipeline starts.
	 * </p>
	 * <p>
	 * The {@link #start()} method must be called before invoking this method.
	 * </p>
	 * <p>
	 * To wait for the operation to complete synchronously, it is necessary to call
	 * {@link Mono#block()} on the returned Mono.
	 * </p>
	 * @return a Mono that completes after the pipeline has created its underlying Pulsar
	 * consumer
	 */
	default Mono<Void> untilConsumingStarted() {
		return Mono.empty();
	}

	/**
	 * <p>
	 * Returns a reactive publisher (Mono) that completes after the pipeline has closed
	 * the underlying Pulsar consumer and stopped consuming new messages.
	 * </p>
	 * <p>
	 * The {@link #stop()} method must be called before invoking this method.
	 * </p>
	 * <p>
	 * To wait for the operation to complete synchronously, it is necessary to call
	 * {@link Mono#block()} on the returned Mono.
	 * </p>
	 * @return a Mono that completes when the pipeline has closed the underlying Pulsar
	 * consumer
	 */
	default Mono<Void> untilConsumingStopped() {
		return Mono.empty();
	}

}
