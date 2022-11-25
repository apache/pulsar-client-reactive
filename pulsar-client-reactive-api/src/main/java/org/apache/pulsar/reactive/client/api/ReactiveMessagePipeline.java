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

/**
 * Reactive message pipeline interface.
 *
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessagePipeline extends AutoCloseable {

	/**
	 * Starts the reactive pipeline.
	 * @return the pipeline
	 */
	ReactiveMessagePipeline start();

	/**
	 * Stops the reactive pipeline.
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

}
