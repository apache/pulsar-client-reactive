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

package org.apache.pulsar.reactive.client.jackson;

@SuppressWarnings("unused")
class DeadLetterPolicyConf {

	private int maxRedeliverCount;

	private String retryLetterTopic;

	private String deadLetterTopic;

	private String initialSubscriptionName;

	int getMaxRedeliverCount() {
		return this.maxRedeliverCount;
	}

	void setMaxRedeliverCount(int maxRedeliverCount) {
		this.maxRedeliverCount = maxRedeliverCount;
	}

	String getRetryLetterTopic() {
		return this.retryLetterTopic;
	}

	void setRetryLetterTopic(String retryLetterTopic) {
		this.retryLetterTopic = retryLetterTopic;
	}

	String getDeadLetterTopic() {
		return this.deadLetterTopic;
	}

	void setDeadLetterTopic(String deadLetterTopic) {
		this.deadLetterTopic = deadLetterTopic;
	}

	String getInitialSubscriptionName() {
		return this.initialSubscriptionName;
	}

	void setInitialSubscriptionName(String initialSubscriptionName) {
		this.initialSubscriptionName = initialSubscriptionName;
	}

}
