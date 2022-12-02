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

import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.ReaderBuilder;

/**
 * Spec for a {@link ReactiveMessageReader}.
 */
public interface ReactiveMessageReaderSpec {

	/**
	 * Gets the topics to read from.
	 * @return the topic names
	 * @see ReaderBuilder#topics(List)
	 */
	List<String> getTopicNames();

	/**
	 * Gets the reader name.
	 * @return the reader name
	 * @see ReaderBuilder#readerName(String)
	 */
	String getReaderName();

	/**
	 * Gets the subscription name.
	 * @return the name of the subscription
	 * @see ReaderBuilder#subscriptionName(String)
	 */
	String getSubscriptionName();

	/**
	 * Gets the generated subscription name prefix. The default prefix is "reader".
	 * @return the generated subscription name prefix
	 * @see ReaderBuilder#subscriptionRolePrefix(String)
	 */
	String getGeneratedSubscriptionNamePrefix();

	/**
	 * Gets the size of the reader receive queue.
	 * @return the size of the reader receive queue
	 * @see ReaderBuilder#receiverQueueSize(int)
	 */
	Integer getReceiverQueueSize();

	/**
	 * Gets whether to read messages from the compacted topic rather than reading the full
	 * message backlog of the topic.
	 * @return true if messages are read from the compacted topic
	 * @see ReaderBuilder#readCompacted
	 */
	Boolean getReadCompacted();

	/**
	 * Gets the key hash ranges of the reader.
	 * @return the key hash ranges
	 * @see ReaderBuilder#keyHashRange(Range...)
	 */
	List<Range> getKeyHashRanges();

	/**
	 * Gets the key reader to be used to decrypt the message payloads.
	 * @return the key reader to be used to decrypt the message payloads
	 * @see ReaderBuilder#cryptoKeyReader
	 */
	CryptoKeyReader getCryptoKeyReader();

	/**
	 * Gets the action the reader will take in case of decryption failures.
	 * @return the action the reader will take in case of decryption failures
	 * @see ReaderBuilder#cryptoFailureAction
	 */
	ConsumerCryptoFailureAction getCryptoFailureAction();

}
