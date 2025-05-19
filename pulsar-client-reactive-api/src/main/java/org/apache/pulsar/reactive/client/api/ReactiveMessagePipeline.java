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
	 * Starts the reactive pipeline asynchronously. Use the
	 * {@link #untilConsumingStarted()} method for waiting for the pipeline to start
	 * consuming messages.
	 * @return the pipeline
	 */
	ReactiveMessagePipeline start();

	/**
	 * Stops the reactive pipeline asynchronously. Use the
	 * {@link #untilConsumingStopped()} method for waiting for the pipeline to stop
	 * consuming messages.
	 * @return the reactive pipeline
	 */
	ReactiveMessagePipeline stop();

	/**
	 * Gets whether the reactive pipeline is running.
	 * @return true if the reactive pipeline is running
	 */
	boolean isRunning();

	/**
	 * Closes the reactive pipeline.
	 * @throws Exception if an error occurs
	 */
	default void close() throws Exception {
		stop();
	}

	/**
	 * Waits for consuming to be started. It is required to call {#@link #start()} before
	 * calling this method.
	 * @return a Mono that is completed after the pipeline is consuming messages.
	 */
	default Mono<Void> untilConsumingStarted() {
		return Mono.empty();
	}

	/**
	 * Waits until consuming has stopped when stopping the pipeline. It is required to
	 * call {#@link #stop()} before calling this method.
	 * @return a Mono that is completed when the pipeline has stopped consuming messages.
	 */
	default Mono<Void> untilConsumingStopped() {
		return Mono.empty();
	}

}
