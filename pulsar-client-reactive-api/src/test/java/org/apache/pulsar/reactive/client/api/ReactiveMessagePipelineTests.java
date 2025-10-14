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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.internal.DefaultImplementation;
import org.apache.pulsar.common.api.EncryptionContext;
import org.apache.pulsar.reactive.client.internal.api.InternalConsumerListener;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReactiveMessagePipelineTests {

	@Test
	void startStopPipeline() throws Exception {
		AtomicInteger subscriptions = new AtomicInteger();
		AtomicInteger cancellations = new AtomicInteger();
		Function<Mono<Void>, Publisher<Void>> transformer = (mono) -> mono
			.doOnSubscribe((s) -> subscriptions.incrementAndGet())
			.doOnCancel(cancellations::incrementAndGet);
		try (ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
			.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then())
			.transformPipeline(transformer)
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
			.doOnSubscribe((s) -> subscriptions.incrementAndGet())
			.doOnCancel(cancellations::incrementAndGet);
		try (ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
			.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then())
			.transformPipeline(transformer)
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
			.doOnSubscribe((s) -> subscriptions.incrementAndGet())
			.doOnCancel(cancellations::incrementAndGet);
		ReactiveMessagePipeline pipeline = new TestConsumer(Integer.MAX_VALUE).messagePipeline()
			.messageHandler((message) -> Mono.delay(Duration.ofSeconds(1)).then())
			.transformPipeline(transformer)
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
		Function<Message<String>, Publisher<Void>> messageHandler = (
				message) -> Mono.empty().then().doFinally((__) -> latch.countDown());
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void pipelineUntilStartedAndStopped() throws Exception {
		int numMessages = 10;
		Duration subscriptionDelay = Duration.ofSeconds(1);
		TestConsumer testConsumer = new TestConsumer(numMessages, subscriptionDelay);
		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (
				message) -> Mono.empty().then().doFinally((__) -> latch.countDown());
		ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build();
		pipeline.start();
		// timeout should occur since subscription delay is 1 second in TestConsumer
		assertThatThrownBy(() -> pipeline.untilStarted().block(Duration.ofMillis(100)))
			.isInstanceOf(IllegalStateException.class)
			.hasCauseInstanceOf(TimeoutException.class);
		// now wait for consuming to start
		pipeline.untilStarted().block(Duration.ofSeconds(2));
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		// now wait for consuming to stop
		pipeline.stop().untilStopped().block(Duration.ofSeconds(1));
	}

	@Test
	void streamingHandler() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(numMessages);
		Function<Flux<Message<String>>, Publisher<MessageResult<Void>>> messageHandler = (
				messageFlux) -> messageFlux.map(MessageResult::acknowledge).doOnNext((__) -> latch.countDown());
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.streamingMessageHandler(messageHandler)
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
		assertThatIllegalStateException().isThrownBy(() -> new TestConsumer(0).messagePipeline()
			.messageHandler((m) -> Mono.empty())
			.streamingMessageHandler((messageFlux) -> messageFlux.map(MessageResult::acknowledge))
			.build());
	}

	@Test
	void handlingTimeout() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(1);
		testConsumer.setFinishedCallback(latch::countDown);
		AtomicReference<MessageId> timedoutMessageId = new AtomicReference<>();
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> Mono.defer(() -> {
			Duration delay;
			if (message.getValue().equals("5")) {
				delay = Duration.ofMillis(15);
				timedoutMessageId.set(message.getMessageId());
			}
			else {
				delay = Duration.ofMillis(2);
			}
			return Mono.delay(delay).then();
		});
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler(messageHandler)
			.handlingTimeout(Duration.ofMillis(5))
			.build()) {
			pipeline.start();
			assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
			// 9 messages should have been acked
			assertThat(testConsumer.getAcknowledgedMessages()).hasSize(9);
			// 1 message should have been nacked
			assertThat(testConsumer.getNackedMessages()).hasSize(1);
			assertThat(timedoutMessageId).isNotNull();
			// the nacked message id should be the one with the longer processing delay
			assertThat(testConsumer.getNackedMessages()).first().isEqualTo(timedoutMessageId.get());
		}
	}

	@Test
	void negativeAcknowledgement() throws Exception {
		int numMessages = 10;
		TestConsumer testConsumer = new TestConsumer(numMessages);
		CountDownLatch latch = new CountDownLatch(1);
		testConsumer.setFinishedCallback(latch::countDown);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> {
			if (message.getValue().equals("5")) {
				throw new RuntimeException("Handling message 5 failed");
			}
			return Mono.empty();
		};
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
			// 9 messages should have been acked
			assertThat(testConsumer.getAcknowledgedMessages()).hasSize(9);
			// 1 message should have been nacked
			assertThat(testConsumer.getNackedMessages()).hasSize(1);
		}
	}

	@Test
	void errorLogger() throws Exception {
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
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler(messageHandler)
			.errorLogger(errorLogger)
			.build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}

	}

	@Test
	void concurrency() throws Exception {
		int numMessages = 1000;
		TestConsumer testConsumer = new TestConsumer(numMessages);

		// Test that non-concurrent fails to process all messages in time
		InflightCounter inflightCounterNoConcurrency = new InflightCounter();
		CountDownLatch latch1 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler = (message) -> Mono.delay(Duration.ofMillis(100))
			.transform(inflightCounterNoConcurrency::transform)
			.then()
			.doFinally((__) -> latch1.countDown());
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline().messageHandler(messageHandler).build()) {
			pipeline.start();
			assertThat(latch1.await(150, TimeUnit.MILLISECONDS)).isFalse();
		}
		assertThat(inflightCounterNoConcurrency.getMax()).isEqualTo(1);

		// Test that concurrent succeeds to process all messages in time
		InflightCounter inflightCounterConcurrency = new InflightCounter();
		CountDownLatch latch2 = new CountDownLatch(numMessages);
		Function<Message<String>, Publisher<Void>> messageHandler2 = (message) -> Mono.delay(Duration.ofMillis(100))
			.transform(inflightCounterConcurrency::transform)
			.then()
			.doFinally((__) -> latch2.countDown());
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler(messageHandler2)
			.concurrency(1000)
			.build()) {
			pipeline.start();
			assertThat(latch2.await(1, TimeUnit.SECONDS)).isTrue();
		}
		assertThat(inflightCounterConcurrency.getMax()).isEqualTo(1000);
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
			.messageHandler(messageHandler)
			.concurrency(10)
			.build()) {
			pipeline.start();
			assertThat(queue.poll(5, TimeUnit.SECONDS)).isNotEqualTo("0");

			// Drain the queue
			for (int i = 0; i < 9; i++) {
				String poll = queue.poll(5, TimeUnit.SECONDS);
				assertThat(poll).isNotNull();
			}
		}

		// Make all messages go to the same handler and verify that messages are processed
		// in order.
		MessageGroupingFunction groupingFunction = (message, numberOfGroups) -> 0;
		try (ReactiveMessagePipeline pipeline = new TestConsumer(numMessages).messagePipeline()
			.messageHandler(messageHandler)
			.concurrency(10)
			.groupOrderedProcessing(groupingFunction)
			.build()) {
			pipeline.start();
			assertThat(queue.poll(5, TimeUnit.SECONDS)).isEqualTo("0");
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
			.doOnNext((it) -> latch.countDown())
			.then()
			.transform(inflightCounter::transform);

		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler(messageHandler2)
			.concurrency(1000)
			.maxInflight(maxInFlight)
			.build()) {
			pipeline.start();
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
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

		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler((__) -> Mono.empty())
			.build()) {
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
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler((__) -> Mono.empty())
			.pipelineRetrySpec(retrySpec)
			.build()) {
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
		try (ReactiveMessagePipeline pipeline = testConsumer.messagePipeline()
			.messageHandler((__) -> Mono.empty())
			.transformPipeline(transformer)
			.build()) {
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

		private final Duration subscriptionDelay;

		private volatile Runnable finishedCallback;

		TestConsumer(int numMessages) {
			this(numMessages, null);
		}

		TestConsumer(int numMessages, Duration subscriptionDelay) {
			this.numMessages = numMessages;
			this.subscriptionDelay = subscriptionDelay;
		}

		private final List<MessageId> acknowledgedMessages = new CopyOnWriteArrayList<>();

		private final List<MessageId> nackedMessages = new CopyOnWriteArrayList<>();

		void setFinishedCallback(Runnable finishedCallback) {
			this.finishedCallback = finishedCallback;
		}

		@Override
		public <R> Flux<R> consumeMany(Function<Flux<Message<String>>, Publisher<MessageResult<R>>> messageHandler) {
			Flux<R> flux = Flux.deferContextual((contextView) -> {
				Optional<InternalConsumerListener> internalConsumerListener = contextView
					.getOrEmpty(InternalConsumerListener.class);
				internalConsumerListener.ifPresent((listener) -> listener.onConsumerCreated(this));
				Flux<Message<String>> messages = Flux.range(0, this.numMessages)
					.map(Object::toString)
					.map(TestMessage::new);
				return Flux.from(messageHandler.apply(messages)).doOnNext((result) -> {
					if (result.isAcknowledgeMessage()) {
						this.acknowledgedMessages.add(result.getMessageId());
					}
					else {
						this.nackedMessages.add(result.getMessageId());
					}
				}).mapNotNull(MessageResult::getValue).doFinally((__) -> {
					if (this.finishedCallback != null) {
						this.finishedCallback.run();
					}
					internalConsumerListener.ifPresent((listener) -> listener.onConsumerClosed(this));
				});
			});
			if (this.subscriptionDelay != null) {
				return flux.delaySubscription(this.subscriptionDelay);
			}
			else {
				return flux;
			}
		}

		List<MessageId> getAcknowledgedMessages() {
			return this.acknowledgedMessages;
		}

		List<MessageId> getNackedMessages() {
			return this.nackedMessages;
		}

	}

	static class TestMessage implements Message<String> {

		private static final AtomicLong MESSAGE_ID_GENERATOR = new AtomicLong(0L);

		private final String value;

		private final MessageId messageId;

		TestMessage(String value) {
			this.value = value;
			this.messageId = DefaultImplementation.getDefaultImplementation()
				.newMessageId(123456L, MESSAGE_ID_GENERATOR.incrementAndGet(), -1);
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
			return this.messageId;
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
		public Optional<byte[]> getSchemaId() {
			return Optional.empty();
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

	static class InflightCounter {

		AtomicInteger max = new AtomicInteger();

		AtomicInteger current = new AtomicInteger();

		private void begin() {
			int incremented = this.current.incrementAndGet();
			this.max.updateAndGet((currentMax) -> Math.max(incremented, currentMax));
		}

		private void end() {
			this.current.decrementAndGet();
		}

		int getMax() {
			return this.max.get();
		}

		<T> Publisher<T> transform(Publisher<T> publisher) {
			if (publisher instanceof Mono<?>) {
				return Mono.using(() -> {
					this.begin();
					return this;
				}, (__) -> Mono.from(publisher), (__) -> end());
			}
			else {
				return Flux.using(() -> {
					this.begin();
					return this;
				}, (__) -> Flux.from(publisher), (__) -> end());
			}
		}

	}

}
