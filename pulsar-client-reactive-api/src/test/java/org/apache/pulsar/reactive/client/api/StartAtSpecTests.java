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

import java.time.Instant;

import org.apache.pulsar.client.api.MessageId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StartAtSpec}
 */
class StartAtSpecTests {

	@Test
	void ofEarliest() {
		MessageIdStartAtSpec spec = StartAtSpec.ofEarliest();
		assertThat(spec.getMessageId()).isEqualTo(MessageId.earliest);
		assertThat(spec.isInclusive()).isTrue();
	}

	@Test
	void ofLatest() {
		MessageIdStartAtSpec spec = StartAtSpec.ofLatest();
		assertThat(spec.getMessageId()).isEqualTo(MessageId.latest);
		assertThat(spec.isInclusive()).isFalse();
	}

	@Test
	void ofLatestInclusive() {
		MessageIdStartAtSpec spec = StartAtSpec.ofLatestInclusive();
		assertThat(spec.getMessageId()).isEqualTo(MessageId.latest);
		assertThat(spec.isInclusive()).isTrue();
	}

	@Test
	void ofMessageId() {
		MessageIdStartAtSpec spec = StartAtSpec.ofMessageId(MessageId.earliest);
		assertThat(spec.getMessageId()).isEqualTo(MessageId.earliest);
		assertThat(spec.isInclusive()).isFalse();
	}

	@Test
	void ofMessageIdWithInclusive() {
		MessageIdStartAtSpec spec = StartAtSpec.ofMessageId(MessageId.earliest, true);
		assertThat(spec.getMessageId()).isEqualTo(MessageId.earliest);
		assertThat(spec.isInclusive()).isTrue();
	}

	@Test
	void ofInstant() {
		Instant instant = Instant.now();
		InstantStartAtSpec spec = StartAtSpec.ofInstant(instant);
		assertThat(spec.getInstant()).isEqualTo(instant);
	}

}
