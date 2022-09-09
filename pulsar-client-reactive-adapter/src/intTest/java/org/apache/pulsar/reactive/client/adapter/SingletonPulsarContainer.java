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

package org.apache.pulsar.reactive.client.adapter;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

public final class SingletonPulsarContainer {

	private SingletonPulsarContainer() {

	}

	/** The singleton instance for Pulsar container. */
	public static PulsarContainer PULSAR_CONTAINER = new PulsarContainer(
			DockerImageName.parse("apachepulsar/pulsar").withTag("2.10.1"));

	static {
		PULSAR_CONTAINER.start();
	}

	public static PulsarClient createPulsarClient() throws PulsarClientException {
		return PulsarClient.builder().serviceUrl(SingletonPulsarContainer.PULSAR_CONTAINER.getPulsarBrokerUrl())
				.build();
	}

}
