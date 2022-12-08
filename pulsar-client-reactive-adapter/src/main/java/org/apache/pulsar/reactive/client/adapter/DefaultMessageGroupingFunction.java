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

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.impl.Murmur3Hash32;
import org.apache.pulsar.reactive.client.api.MessageGroupingFunction;

/**
 * Default message grouping function. It uses a {@link Murmur3Hash32} hashing function to
 * compute a hash of the message key. All messages with the same key hash will be
 * processed in order by the same message handler instance.
 */
public class DefaultMessageGroupingFunction implements MessageGroupingFunction {

	private static byte[] getMessageKeyBytes(Message<?> message) {
		byte[] keyBytes = null;
		if (message.hasOrderingKey()) {
			keyBytes = message.getOrderingKey();
		}
		else if (message.hasKey()) {
			keyBytes = message.getKeyBytes();
		}
		if (keyBytes == null || keyBytes.length == 0) {
			// use a group that has been derived from the message id so that redeliveries
			// get handled in order
			keyBytes = message.getMessageId().toByteArray();
		}
		return keyBytes;
	}

	@Override
	public int resolveProcessingGroup(Message<?> message, int numberOfGroups) {
		byte[] keyBytes = getMessageKeyBytes(message);
		int keyHash = Murmur3Hash32.getInstance().makeHash(keyBytes);
		return keyHash % numberOfGroups;
	}

}
