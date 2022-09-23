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

package org.apache.pulsar.reactive.client.jackson;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.Range;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

abstract class ConverterUtils {

	static DeadLetterPolicy toDeadLetterPolicy(DeadLetterPolicyConf deadLetterPolicy) {
		if (deadLetterPolicy == null) {
			return null;
		}
		return DeadLetterPolicy.builder().maxRedeliverCount(deadLetterPolicy.getMaxRedeliverCount())
				.deadLetterTopic(deadLetterPolicy.getDeadLetterTopic())
				.retryLetterTopic(deadLetterPolicy.getRetryLetterTopic())
				.initialSubscriptionName(deadLetterPolicy.getInitialSubscriptionName()).build();
	}

	static <T> T toClass(ClassConf conf) {
		if (conf == null) {
			return null;
		}
		Class<?> klass;
		try {
			klass = loadClass(conf.getClassName(), Thread.currentThread().getContextClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(String.format("Failed to load class %s", conf.getClassName()));
		}

		try {
			if (conf.getArgs() != null) {
				Constructor<?> ctor = klass.getConstructor(Map.class);
				return (T) ctor.newInstance(conf.getArgs());
			}
			else {
				Constructor<?> ctor = klass.getConstructor();
				return (T) ctor.newInstance();
			}
		}
		catch (NoSuchMethodException ex) {
			throw new RuntimeException(
					String.format("Class %s does not have a no-arg constructor or a constructor that accepts map",
							klass.getName()),
					ex);
		}
		catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new RuntimeException(String.format("Failed to create instance for %s", klass.getName()), ex);
		}
	}

	static KeySharedPolicy toKeySharedPolicy(String keySharedPolicy) {
		if (keySharedPolicy == null) {
			return null;
		}
		switch (keySharedPolicy) {
			case "AUTO_SPLIT":
				return KeySharedPolicy.autoSplitHashRange();
			case "STICKY":
				return KeySharedPolicy.stickyHashRange();
			default:
				throw new IllegalArgumentException("Unsupported keySharedPolicy: " + keySharedPolicy);
		}
	}

	static Scheduler toScheduler(String scheduler) {
		if (scheduler == null) {
			return null;
		}
		switch (scheduler) {
			case "parallel":
				return Schedulers.parallel();
			case "single":
				return Schedulers.single();
			case "boundedElastic":
				return Schedulers.boundedElastic();
			case "elastic":
				return Schedulers.elastic();
			case "immediate":
				return Schedulers.immediate();
			default:
				throw new IllegalArgumentException("Unsupported scheduler: " + scheduler);
		}
	}

	static Range toRange(RangeConf rangeConf) {
		if (rangeConf == null) {
			return null;
		}
		return new Range(rangeConf.getStart(), rangeConf.getEnd());
	}

	static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
		Class<?> objectClass;
		try {
			objectClass = Class.forName(className);
		}
		catch (ClassNotFoundException | NoClassDefFoundError ex) {
			if (classLoader != null) {
				objectClass = classLoader.loadClass(className);
			}
			else {
				throw ex;
			}
		}
		return objectClass;
	}

}
