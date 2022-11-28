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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.ReaderBuilder;

/**
 * Builder interface for {@link ReactiveMessageReader}.
 *
 * @param <T> the message payload type
 * @author Lari Hotari
 * @author Christophe Bornet
 */
public interface ReactiveMessageReaderBuilder<T> {

	/**
	 * Sets the position where to start reading from.
	 * <p>
	 * This setting applies to each new {@link Reader} created under the built
	 * {@link ReactiveMessageReader}.
	 * @param startAtSpec the position where to start reading from
	 * @return the reader builder instance
	 * @see ReaderBuilder#startMessageId(MessageId)
	 * @see ReaderBuilder#startMessageIdInclusive()
	 * @see ReaderBuilder#startMessageFromRollbackDuration(long, TimeUnit)
	 */
	ReactiveMessageReaderBuilder<T> startAtSpec(StartAtSpec startAtSpec);

	/**
	 * Sets the action to perform once the end of the stream is reached.
	 * @param endOfStreamAction the action to perform once the end of the stream is
	 * reached
	 * @return the reader builder instance
	 */
	ReactiveMessageReaderBuilder<T> endOfStreamAction(EndOfStreamAction endOfStreamAction);

	/**
	 * Applies a reader spec to configure the reader.
	 * @param readerSpec the reader spec to apply
	 * @return the reader builder instance
	 */
	default ReactiveMessageReaderBuilder<T> applySpec(ReactiveMessageReaderSpec readerSpec) {
		getMutableSpec().applySpec(readerSpec);
		return this;
	}

	/**
	 * Gets an immutable spec from the underlying spec of this builder.
	 * @return the immutable reactive reader spec.
	 */
	default ReactiveMessageReaderSpec toImmutableSpec() {
		return new ImmutableReactiveMessageReaderSpec(getMutableSpec());
	}

	/**
	 * Gets the mutable spec of this builder.
	 * @return the reactive reader spec
	 */
	MutableReactiveMessageReaderSpec getMutableSpec();

	/**
	 * Creates and returns a copy of this reactive reader builder.
	 * @return the cloned reactive reader builder
	 */
	ReactiveMessageReaderBuilder<T> clone();

	/**
	 * Builds the reactive reader.
	 * @return the built reactive reader
	 */
	ReactiveMessageReader<T> build();

	/**
	 * Adds a topic this reader will read from.
	 * @param topicName the name of the topic to add
	 * @return the reader builder instance
	 * @see ReaderBuilder#topic(String)
	 */
	default ReactiveMessageReaderBuilder<T> topic(String topicName) {
		getMutableSpec().getTopicNames().add(topicName);
		return this;
	}

	/**
	 * Adds topics this reader will read from.
	 * @param topicNames the names of the topics to add
	 * @return the reader builder instance
	 * @see ReaderBuilder#topic(String)
	 */
	default ReactiveMessageReaderBuilder<T> topic(String... topicNames) {
		for (String topicName : topicNames) {
			getMutableSpec().getTopicNames().add(topicName);
		}
		return this;
	}

	/**
	 * Sets the topics this reader will read from.
	 * @param topicNames the names of the topics to set
	 * @return the reader builder instance
	 * @see ReaderBuilder#topics(List)
	 */
	default ReactiveMessageReaderBuilder<T> topics(List<String> topicNames) {
		getMutableSpec().setTopicNames(topicNames);
		return this;
	}

	/**
	 * Sets the reader name.
	 * <p>
	 * The reader name is purely informational and can be used to track a particular
	 * reader in the reported stats. By default, a randomly generated name is used for
	 * each {@link Reader} created under the built {@link ReactiveMessageReader}.
	 * @param readerName the name to use for the reader
	 * @return the reader builder instance
	 * @see ReaderBuilder#readerName(String)
	 */
	default ReactiveMessageReaderBuilder<T> readerName(String readerName) {
		getMutableSpec().setReaderName(readerName);
		return this;
	}

	/**
	 * Sets the subscription name.
	 * <p>
	 * If {@link #generatedSubscriptionNamePrefix} is set at the same time, this
	 * configuration will prevail.
	 * @param subscriptionName the name of the subscription to set
	 * @return the reader builder instance
	 * @see ReaderBuilder#subscriptionName(String)
	 */
	default ReactiveMessageReaderBuilder<T> subscriptionName(String subscriptionName) {
		getMutableSpec().setSubscriptionName(subscriptionName);
		return this;
	}

	/**
	 * Sets the generated subscription name prefix. The default prefix is "reader". A
	 * subscription name will be generated for each {@link Reader} created under the built
	 * {@link ReactiveMessageReader}.
	 * @param generatedSubscriptionNamePrefix the generated subscription name prefix to
	 * set
	 * @return the reader builder instance
	 * @see ReaderBuilder#subscriptionRolePrefix(String)
	 *
	 */
	default ReactiveMessageReaderBuilder<T> generatedSubscriptionNamePrefix(String generatedSubscriptionNamePrefix) {
		getMutableSpec().setGeneratedSubscriptionNamePrefix(generatedSubscriptionNamePrefix);
		return this;
	}

	/**
	 * Sets the size of the reader receiver queue.
	 * <p>
	 * The setting applies to each {@link Reader} created under the built
	 * {@link ReactiveMessageReader}.
	 * <p>
	 * The reader receiver queue controls how many messages will be prefetched into the
	 * {@link Reader}. Using a higher value could potentially increase the reader
	 * throughput at the expense of bigger memory utilization.
	 * <p>
	 * The default value is {@code 1000} messages and should be good for most use cases.
	 * @param receiverQueueSize the receiver queue size to set
	 * @return the reader builder instance
	 * @see ReaderBuilder#receiverQueueSize(int)
	 */
	default ReactiveMessageReaderBuilder<T> receiverQueueSize(Integer receiverQueueSize) {
		getMutableSpec().setReceiverQueueSize(receiverQueueSize);
		return this;
	}

	/**
	 * Sets whether the reader will read messages from the compacted topic rather than
	 * reading the full message backlog of the topic. This means that, if the topic has
	 * been compacted, the reader will only see the latest value for each key in the
	 * topic, up until the point in the topic message backlog that has been compacted.
	 * Beyond that point, the messages will be sent as normal.
	 * <p>
	 * readCompacted can only be enabled for subscriptions to persistent topics, which
	 * have a single active consumer (i.e. failover or exclusive subscriptions).
	 * Attempting to enable it on subscriptions to a non-persistent topic or on a shared
	 * subscription, will lead to the
	 * {@link ReactiveMessageReader#readOne()}/{@link ReactiveMessageReader#readMany()}
	 * calls emitting an {@link PulsarClientException}.
	 * @param readCompacted whether to read from the compacted topic
	 * @return the reader builder instance
	 * @see ReaderBuilder#readCompacted(boolean)
	 */
	default ReactiveMessageReaderBuilder<T> readCompacted(Boolean readCompacted) {
		getMutableSpec().setReadCompacted(readCompacted);
		return this;
	}

	/**
	 * Sets the key hash ranges of the reader. The broker will only dispatch messages for
	 * which the hash of the message key is inside the one of the key hash ranges.
	 * Multiple key hash ranges can be specified on a reader.
	 *
	 * <p>
	 * Total hash range size is 65536, so the maximum end of the range should be less than
	 * or equal to 65535.
	 * @param keyHashRanges the key hash ranges to set
	 * @return the reader builder instance
	 * @see ReaderBuilder#keyHashRange(Range...)
	 */
	default ReactiveMessageReaderBuilder<T> keyHashRanges(List<Range> keyHashRanges) {
		getMutableSpec().setKeyHashRanges(keyHashRanges);
		return this;
	}

	/**
	 * Sets the key reader to be used to decrypt the message payloads.
	 * @param cryptoKeyReader the key reader to be used to decrypt the message payloads.
	 * @return the consumer builder instance
	 * @see ReaderBuilder#cryptoKeyReader(CryptoKeyReader)
	 */
	default ReactiveMessageReaderBuilder<T> cryptoKeyReader(CryptoKeyReader cryptoKeyReader) {
		getMutableSpec().setCryptoKeyReader(cryptoKeyReader);
		return this;
	}

	/**
	 * Sets the action the reader will take in case of decryption failures.
	 * @param cryptoFailureAction the action the consumer will take in case of decryption
	 * failures
	 * @return the reader builder instance
	 * @see ReaderBuilder#cryptoFailureAction(ConsumerCryptoFailureAction)
	 */
	default ReactiveMessageReaderBuilder<T> cryptoFailureAction(ConsumerCryptoFailureAction cryptoFailureAction) {
		getMutableSpec().setCryptoFailureAction(cryptoFailureAction);
		return this;
	}

}
