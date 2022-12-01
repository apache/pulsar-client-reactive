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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.common.api.EncryptionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ReactiveMessagePipelineTest {

	@Test
	void startStopPipeline() throws Exception {
		AtomicInteger subscriptions = new AtomicInteger();
		AtomicInteger cancellations = new AtomicInteger();
		Function<Mono<Void>, Publisher<Void>> transformer = (mono) -> mono
				.doOnSubscribe((s) -> subscriptions.incrementAndGet()).doOnCancel(cancellations::incrementAndGet);
		try (ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
				.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then()).transformPipeline(transformer)
				.build()) {
			assertThat(pipeline.isRunning()).isFalse();

			// Stopping not started
			pipeline.stop();
			assertThat(pipeline.isRunning()).isFalse();
			assertThat(subscriptions.get()).isEqualTo(0);
			assertThat(cancellations.get()).isEqualTo(0);

			// Starting
			pipeline.start();
			assertThat(pipeline.isRunning()).isTrue();
			assertThat(subscriptions.get()).isEqualTo(1);
			assertThat(cancellations.get()).isEqualTo(0);

			// Stopping
			pipeline.stop();
			assertThat(pipeline.isRunning()).isFalse();
			assertThat(subscriptions.get()).isEqualTo(1);
			assertThat(cancellations.get()).isEqualTo(1);

			// Stopping again
			pipeline.stop();
			assertThat(pipeline.isRunning()).isFalse();
			assertThat(subscriptions.get()).isEqualTo(1);
			assertThat(cancellations.get()).isEqualTo(1);

			// Starting again
			pipeline.start();
			assertThat(pipeline.isRunning()).isTrue();
			assertThat(subscriptions.get()).isEqualTo(2);
			assertThat(cancellations.get()).isEqualTo(1);
		}

	}

	@Test
	void startTwiceFails() throws Exception {
		AtomicInteger subscriptions = new AtomicInteger();
		AtomicInteger cancellations = new AtomicInteger();
		Function<Mono<Void>, Publisher<Void>> transformer = (mono) -> mono
				.doOnSubscribe((s) -> subscriptions.incrementAndGet()).doOnCancel(cancellations::incrementAndGet);
		try (ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
				.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then()).transformPipeline(transformer)
				.build()) {
			pipeline.start();
			assertThatIllegalStateException().isThrownBy(pipeline::start);
			assertThat(pipeline.isRunning()).isTrue();
			assertThat(subscriptions.get()).isEqualTo(1);
			assertThat(cancellations.get()).isEqualTo(0);
		}
	}

	@Test
	void closePipeline() throws Exception {
		AtomicInteger subscriptions = new AtomicInteger();
		AtomicInteger cancellations = new AtomicInteger();
		Function<Mono<Void>, Publisher<Void>> transformer = (mono) -> mono
				.doOnSubscribe((s) -> subscriptions.incrementAndGet()).doOnCancel(cancellations::incrementAndGet);
		ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
				.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then()).transformPipeline(transformer)
				.build();

		pipeline.close();
		assertThat(pipeline.isRunning()).isFalse();
		assertThat(subscriptions.get()).isEqualTo(0);
		assertThat(cancellations.get()).isEqualTo(0);

		pipeline.start();
		assertThat(pipeline.isRunning()).isTrue();
		assertThat(subscriptions.get()).isEqualTo(1);
		assertThat(cancellations.get()).isEqualTo(0);

		pipeline.close();
		assertThat(pipeline.isRunning()).isFalse();
		assertThat(subscriptions.get()).isEqualTo(1);
		assertThat(cancellations.get()).isEqualTo(1);

		pipeline.close();
		assertThat(pipeline.isRunning()).isFalse();
		assertThat(subscriptions.get()).isEqualTo(1);
		assertThat(cancellations.get()).isEqualTo(1);
	}

	@Test
	void messageHandler() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> {
			latch.countDown();
			return Mono.empty();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void streamingHandler() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Flux<Message<String>>, Publisher<MessageResult<Void>>> messageHandler = (messageFlux) -> messageFlux
				.map((message) -> {
					latch.countDown();
					return MessageResult.acknowledge(message);
				});
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().streamingMessageHandler(messageHandler)
				.build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void missingHandler() {
		assertThatNullPointerException().isThrownBy(() -> new TestConsumer(0).messagePipeline().build());
	}

	@Test
	void bothMessageHandlerAndStreamingHandler() {
		assertThatIllegalStateException()
				.isThrownBy(() -> new TestConsumer(0).messagePipeline().messageHandler((m) -> Mono.empty())
						.streamingMessageHandler((messageFlux) -> messageFlux.map(MessageResult::acknowledge)).build());
	}

	@Test
	void handlingTimeout() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch1 = new CountDownLatch(numMessages);
		CountDownLatch latch2 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> {
			latch1.countDown();
			return Mono.delay(Duration.ofMillis(2)).doOnNext((it) -> latch2.countDown()).then();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch1.await(10000, TimeUnit.MILLISECONDS)).isTrue();
			assertThat(latch2.await(10, TimeUnit.MILLISECONDS)).isTrue();
		}

		CountDownLatch latch3 = new CountDownLatch(numMessages);
		CountDownLatch latch4 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler2 = (message) -> {
			latch3.countDown();
			return Mono.delay(Duration.ofMillis(2)).doOnNext((it) -> latch4.countDown()).then();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler2)
				.handlingTimeout(Duration.ofMillis(1)).build()) {
			pipeline.start();
			assertThat(latch3.await(100, TimeUnit.MILLISECONDS)).isTrue();
			assertThat(latch4.await(10, TimeUnit.MILLISECONDS)).isFalse();
			assertThat(latch4.getCount()).isEqualTo(10);
		}

	}

	@Test
	void errorHandler() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> Mono
				.error(new RuntimeException("error"));
		BiConsumer<Message<String>, Throwable> errorLogger = (message, throwable) -> {
			int messageValue = Integer.parseInt(message.getValue());
			if (throwable.getMessage().equals("error") && messageValue >= 0 && messageValue < numMessages) {
				latch.countDown();
			}
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler)
				.errorLogger(errorLogger).build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void concurrency() throws Exception {
		int numMessages = 1000;
		TestConsumer testConsumer = new TestConsumer(numMessages);

		// Test that non-concurrent fails to process all messages in time
		CountDownLatch latch1 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> {
			latch1.countDown();
			return Mono.delay(Duration.ofMillis(100)).then();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch1.await(150, TimeUnit.MILLISECONDS)).isFalse();
		}

		// Test that concurrent succeeds to process all messages in time
		CountDownLatch latch2 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler2 = (message) -> {
			latch2.countDown();
			return Mono.delay(Duration.ofMillis(100)).then();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler2)
				.concurrency(1000).build()) {
			pipeline.start();
			assertThat(latch2.await(150, TimeUnit.MILLISECONDS)).isTrue();
		}

	}

	@Test
	void messageGroupingFunction() throws Exception {
		int numMessages = 10;
		BlockingQueue<String> queue = new LinkedBlockingQueue<>();
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> {
			String value = message.getValue();
			if (value.equals("0")) {
				// Delay the first message. If the messages are not grouped, then "0"
				// won't be the first message added to the queue.
				return Mono.delay(Duration.ofMillis(10)).doOnNext((it) -> queue.add(value)).then();
			}
			queue.add(value);
			return Mono.empty();
		};

		// Verify that without a grouping function, the messages are not processed in
		// order.
		try (ReactiveMessagePipeline pipeline = new TestConsumer(numMessages).messagePipeline()
				.messageHandler(messageHandler).concurrency(10).build()) {
			pipeline.start();
			assertThat(queue.poll(5, TimeUnit.SECONDS)).isNotEqualTo("0");

			// Drain the queue
			for (int i = 0; i < 9; i++) {
				String poll = queue.poll(5, TimeUnit.SECONDS);
				System.out.println(poll);
				assertThat(poll).isNotNull();
			}
		}

		// Make all messages go to the same handler and verify that messages are processed
		// in order.
		MessageGroupingFunction groupingFunction = (message, numberOfGroups) -> 0;
		try (ReactiveMessagePipeline pipeline = new TestConsumer(numMessages).messagePipeline()
				.messageHandler(messageHandler).concurrency(10).groupOrderedProcessing(groupingFunction).build()) {
			pipeline.start();
			assertThat(queue.poll(5, TimeUnit.SECONDS)).isEqualTo("0");
		}
	}

	static class InflightCounter {

		AtomicInteger max = new AtomicInteger();

		AtomicInteger current = new AtomicInteger();

		private void begin() {
			int incremented = current.incrementAndGet();
			max.updateAndGet(currentMax -> incremented > currentMax ? incremented : currentMax);
		}

		private void end() {
			current.decrementAndGet();
		}

		int getMax() {
			return max.get();
		}

		<T> Publisher<T> transform(Publisher<T> publisher) {
			if (publisher instanceof Mono<?>) {
				return Mono.using(() -> {
					this.begin();
					return this;
				}, __ -> Mono.from(publisher), __ -> end());
			}
			else {
				return Flux.using(() -> {
					this.begin();
					return this;
				}, __ -> Flux.from(publisher), __ -> end());
			}
		}

	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 13, 29 })
	void maxInflight(int maxInFlight) throws Exception {
		int numMessages = 1000;
		TestConsumer testConsumer = new TestConsumer(numMessages);

		InflightCounter inflightCounter = new InflightCounter();

		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler2 = (message) -> Mono.delay(Duration.ofMillis(2))
				.doOnNext((it) -> latch.countDown()).then().as(inflightCounter::transform);

		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler2)
				.concurrency(1000).maxInflight(maxInFlight).build()) {
			pipeline.start();
			assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
			assertThat(inflightCounter.getMax()).isEqualTo(maxInFlight);
		}
	}

	@Test
	void defaultPipelineRetry() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);
		ManyConsumer testConsumer = new ManyConsumer() {
			@Override
			public <R> Flux<R> consumeMany(
					Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {

				return Flux.range(0, 10).map((value) -> {
					if (value == 0) {
						// We check that the flux is restarted and the message received is
						// "0"
						latch.countDown();
					}
					throw new RuntimeException("error");
				});
			}
		};

		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().build()) {
			pipeline.start();
			// The default pipeline first retry is 5 seconds
			assertThat(latch.await(8, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void customPipelineRetry() throws Exception {
		CountDownLatch latch = new CountDownLatch(2);
		ManyConsumer testConsumer = new ManyConsumer() {
			@Override
			public <R> Flux<R> consumeMany(
					Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {

				return Flux.range(0, 10).map((value) -> {
					if (value == 0) {
						// We check that the flux is restarted and the message received is
						// "0"
						latch.countDown();
					}
					throw new RuntimeException("error");
				});
			}
		};

		Retry retrySpec = Retry.fixedDelay(1, Duration.ofMillis(1));
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().pipelineRetrySpec(retrySpec).build()) {
			pipeline.start();
			// Wait less than the default retry backoff.
			assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void transformer() throws Exception {
		ManyConsumer testConsumer = new ManyConsumer() {
			@Override
			public <R> Flux<R> consumeMany(
					Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {
				return Flux.error(new RuntimeException("error"));
			}
		};
		CountDownLatch latch = new CountDownLatch(1);
		Function<Mono<Void>, Publisher<Void>> transformer = (mono) -> mono.doOnError((t) -> {
			if (t.getMessage().equals("error")) {
				latch.countDown();
			}
		});
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().transformPipeline(transformer).build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@FunctionalInterface
	interface ManyConsumer extends ReactiveMessageConsumer<String> {

		@Override
		default <R> Mono<R> consumeOne(Function<Message<String>, Publisher<MessageResult<R>>> messageHandler) {
			throw new IllegalArgumentException("not implemented");
		}

		@Override
		default Mono<Void> consumeNothing() {
			throw new IllegalArgumentException("not implemented");
		}

	}

	static class TestConsumer implements ManyConsumer {

		private final int numMessages;

		TestConsumer(int numMessages) {
			this.numMessages = numMessages;
		}

		@Override
		public <R> Flux<R> consumeMany(Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {
			Flux<Message<String>> messages = Flux.range(0, this.numMessages).map(Object::toString)
					.map(TestMessage::new);
			return Flux.from(messageHandler.apply(messages)).mapNotNull(MessageResult::getValue);
		}

	}

	static class TestMessage implements Message<String> {

		private final String value;

		TestMessage(String value) {
			this.value = value;
		}

		@Override
		public Map<String, String> getProperties() {
			return null;
		}

		@Override
		public boolean hasProperty(String name) {
			return false;
		}

		@Override
		public String getProperty(String name) {
			return null;
		}

		@Override
		public byte[] getData() {
			return new byte[0];
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public MessageId getMessageId() {
			return null;
		}

		@Override
		public long getPublishTime() {
			return 0;
		}

		@Override
		public long getEventTime() {
			return 0;
		}

		@Override
		public long getSequenceId() {
			return 0;
		}

		@Override
		public String getProducerName() {
			return null;
		}

		@Override
		public boolean hasKey() {
			return false;
		}

		@Override
		public String getKey() {
			return null;
		}

		@Override
		public boolean hasBase64EncodedKey() {
			return false;
		}

		@Override
		public byte[] getKeyBytes() {
			return new byte[0];
		}

		@Override
		public boolean hasOrderingKey() {
			return false;
		}

		@Override
		public byte[] getOrderingKey() {
			return new byte[0];
		}

		@Override
		public String getTopicName() {
			return null;
		}

		@Override
		public Optional<EncryptionContext> getEncryptionCtx() {
			return Optional.empty();
		}

		@Override
		public int getRedeliveryCount() {
			return 0;
		}

		@Override
		public byte[] getSchemaVersion() {
			return new byte[0];
		}

		@Override
		public boolean isReplicated() {
			return false;
		}

		@Override
		public String getReplicatedFrom() {
			return null;
		}

		@Override
		public void release() {

		}

		@Override
		public boolean hasBrokerPublishTime() {
			return false;
		}

		@Override
		public Optional<Long> getBrokerPublishTime() {
			return Optional.empty();
		}

		@Override
		public boolean hasIndex() {
			return false;
		}

		@Override
		public Optional<Long> getIndex() {
			return Optional.empty();
		}

	}

}
