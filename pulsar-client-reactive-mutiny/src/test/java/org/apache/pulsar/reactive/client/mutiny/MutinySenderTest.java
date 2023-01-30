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

package org.apache.pulsar.reactive.client.mutiny;

import java.util.concurrent.TimeUnit;

import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.reactive.client.api.MessageSendResult;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MutinySender}.
 */
class MutinySenderTest {

	@Test
	void sendOne() throws Exception {
		ReactiveMessageSender<String> sender = new ReactiveMessageSender<String>() {
			@Override
			public Mono<MessageId> sendOne(MessageSpec<String> messageSpec) {
				if (messageSpec.getCorrelationMetadata().equals("test")) {
					return Mono.just(MessageId.earliest);
				}
				return Mono.just(MessageId.latest);
			}

			@Override
			public Flux<MessageSendResult<String>> sendMany(Publisher<MessageSpec<String>> messageSpecs) {
				throw new UnsupportedOperationException("should not be called");
			}
		};
		MutinySender<String> mutinySender = new MutinySender<>(sender);
		MessageId messageId = mutinySender.sendOne(MessageSpec.builder("test").correlationMetadata("test").build())
				.subscribeAsCompletionStage().get(5, TimeUnit.SECONDS);
		assertThat(messageId).isEqualTo(MessageId.earliest);
	}

	@Test
	void sendMany() {
		ReactiveMessageSender<String> sender = new ReactiveMessageSender<String>() {
			@Override
			public Mono<MessageId> sendOne(MessageSpec<String> messageSpec) {
				throw new UnsupportedOperationException("should not be called");
			}

			@Override
			public Flux<MessageSendResult<String>> sendMany(Publisher<MessageSpec<String>> messageSpecs) {
				return Flux.from(messageSpecs)
						.map((messageSpec) -> new MessageSendResult<>(MessageId.earliest, messageSpec, null));
			}
		};
		MutinySender<String> mutinySender = new MutinySender<>(sender);

		MessageSpec<String> message1 = MessageSpec.of("test1");
		MessageSpec<String> message2 = MessageSpec.of("test2");
		Flux<MessageSpec<String>> messageSpecs = Flux.just(message1, message2);
		mutinySender.sendMany(messageSpecs).map(MessageSendResult::getMessageSpec).subscribe()
				.withSubscriber(new AssertSubscriber<>(2)).assertCompleted().assertItems(message1, message2);
	}

}
