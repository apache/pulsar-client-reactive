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

package org.apache.pulsar.reactive.client.adapter;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

final class SingletonPulsarContainer {

	private SingletonPulsarContainer() {

	}

	/** The singleton instance for Pulsar container. */
	static PulsarContainer PULSAR_CONTAINER = new PulsarContainer(getPulsarImage())
		.withEnv("PULSAR_PREFIX_acknowledgmentAtBatchIndexLevelEnabled", "true");

	static {
		PULSAR_CONTAINER.start();
	}

	static PulsarClient createPulsarClient() throws PulsarClientException {
		return PulsarClient.builder()
			.serviceUrl(SingletonPulsarContainer.PULSAR_CONTAINER.getPulsarBrokerUrl())
			.build();
	}

	static PulsarAdmin createPulsarAdmin() throws PulsarClientException {
		return PulsarAdmin.builder()
			.serviceHttpUrl(SingletonPulsarContainer.PULSAR_CONTAINER.getHttpServiceUrl())
			.build();
	}

	static DockerImageName getPulsarImage() {
		return DockerImageName.parse("apachepulsar/pulsar:4.1.1");
	}

}
