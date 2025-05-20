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

/**
 * Internal interface to signal the creation and closing of a native consumer. This is not
 * to be intended to be used by applications.
 */
public interface InternalConsumerListener {

	/**
	 * Called when a new native consumer is created. This is called each time a new
	 * consumer is created initially or as a result of a reactive pipeline retry.
	 * @param nativeConsumer the native consumer instance
	 */
	default void onConsumerCreated(Object nativeConsumer) {
		// no-op
	}

	/**
	 * Called when a native consumer is closed. This is called each time a consumer is
	 * closed as a result of a reactive pipeline retry or when the pipeline is closed.
	 * @param nativeConsumer the native consumer instance
	 */
	default void onConsumerClosed(Object nativeConsumer) {
		// no-op
	}

}
