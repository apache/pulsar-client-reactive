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

import java.io.IOException;
import java.time.Duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;
import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.KeySharedPolicy;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.Range;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ImmutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.MutableReactiveMessageSenderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageConsumerSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageReaderSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSenderSpec;
import reactor.core.scheduler.Scheduler;

/**
 * Jackson module to handle the reactive client spec classes.
 */
public class PulsarReactiveClientModule extends SimpleModule {

	/**
	 * Constructs a {@link PulsarReactiveClientModule}.
	 */
	public PulsarReactiveClientModule() {
		super();
		addDeserializer(KeySharedPolicy.class, new KeySharedPolicyDeserializer());
		addSerializer(KeySharedPolicy.class, new KeySharedPolicySerializer());
		addDeserializer(Scheduler.class, new SchedulerDeserializer());
		addSerializer(Scheduler.class, new SchedulerSerializer());
		addDeserializer(DeadLetterPolicy.class, new DeadLetterPolicyDeserializer());
		addDeserializer(RedeliveryBackoff.class, new ClassDeserializer<>());
		addSerializer(RedeliveryBackoff.class, new ClassSerializer<>());
		addDeserializer(CryptoKeyReader.class, new ClassDeserializer<>());
		addSerializer(CryptoKeyReader.class, new ClassSerializer<>());
		addDeserializer(Range.class, new RangeDeserializer());
		addDeserializer(MessageRouter.class, new ClassDeserializer<>());
		addSerializer(MessageRouter.class, new ClassSerializer<>());
		addDeserializer(BatcherBuilder.class, new ClassDeserializer<>());
		addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
		addSerializer(Duration.class, DurationSerializer.INSTANCE);
		setMixInAnnotation(ReactiveMessageConsumerSpec.class, ImmutableReactiveMessageConsumerSpecMixin.class);
		setMixInAnnotation(ImmutableReactiveMessageConsumerSpec.class, ImmutableReactiveMessageConsumerSpecMixin.class);
		setMixInAnnotation(MutableReactiveMessageConsumerSpec.class, MutableReactiveMessageConsumerSpecMixin.class);
		setMixInAnnotation(ReactiveMessageReaderSpec.class, ImmutableReactiveMessageReaderSpecMixin.class);
		setMixInAnnotation(ImmutableReactiveMessageReaderSpec.class, ImmutableReactiveMessageReaderSpecMixin.class);
		setMixInAnnotation(MutableReactiveMessageReaderSpec.class, MutableReactiveMessageReaderSpecMixin.class);
		setMixInAnnotation(ReactiveMessageSenderSpec.class, ImmutableReactiveMessageSenderSpecMixin.class);
		setMixInAnnotation(ImmutableReactiveMessageSenderSpec.class, ImmutableReactiveMessageSenderSpecMixin.class);
		setMixInAnnotation(MutableReactiveMessageSenderSpec.class, MutableReactiveMessageSenderSpecMixin.class);
	}

	/**
	 * Jackson deserializer for {@link KeySharedPolicy}.
	 */
	public static class KeySharedPolicyDeserializer extends JsonDeserializer<KeySharedPolicy> {

		@Override
		public KeySharedPolicy deserialize(JsonParser p, DeserializationContext context) throws IOException {
			return ConverterUtils.toKeySharedPolicy(p.getValueAsString());
		}

	}

	/**
	 * Jackson serializer for {@link KeySharedPolicy}.
	 */
	public static class KeySharedPolicySerializer extends JsonSerializer<KeySharedPolicy> {

		@Override
		public void serialize(KeySharedPolicy keySharedPolicy, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			if (keySharedPolicy instanceof KeySharedPolicy.KeySharedPolicySticky) {
				gen.writeString("STICKY");
			}
			else if (keySharedPolicy instanceof KeySharedPolicy.KeySharedPolicyAutoSplit) {
				gen.writeString("AUTO_SPLIT");
			}
			else {
				gen.writeString(keySharedPolicy.getClass().getName());
			}
		}

	}

	/**
	 * Jackson deserializer for {@link Scheduler}.
	 */
	public static class SchedulerDeserializer extends JsonDeserializer<Scheduler> {

		@Override
		public Scheduler deserialize(JsonParser p, DeserializationContext context) throws IOException {
			return ConverterUtils.toScheduler(p.getValueAsString());
		}

	}

	/**
	 * Jackson serializer for {@link Scheduler}.
	 */
	public static class SchedulerSerializer extends JsonSerializer<Scheduler> {

		@Override
		public void serialize(Scheduler scheduler, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			switch (scheduler.getClass().getName()) {
				case "reactor.core.scheduler.BoundedElasticScheduler":
					gen.writeString("boundedElastic");
					break;
				case "reactor.core.scheduler.ParallelScheduler":
					gen.writeString("parallel");
					break;
				case "reactor.core.scheduler.SingleScheduler":
					gen.writeString("single");
					break;
				case "reactor.core.scheduler.ImmediateScheduler":
					gen.writeString("immediate");
					break;
				case "reactor.core.scheduler.Schedulers$CachedScheduler":
					gen.writeString(scheduler.toString()
						.substring("Schedulers.".length(), scheduler.toString().length() - "()".length()));
					break;
				default:
					gen.writeString(scheduler.getClass().getName());
			}
		}

	}

	/**
	 * Jackson deserializer for {@link DeadLetterPolicy}.
	 */
	public static class DeadLetterPolicyDeserializer extends JsonDeserializer<DeadLetterPolicy> {

		@Override
		public DeadLetterPolicy deserialize(JsonParser p, DeserializationContext context) throws IOException {
			DeadLetterPolicyConf deadLetterPolicyConf = p.readValueAs(DeadLetterPolicyConf.class);
			return ConverterUtils.toDeadLetterPolicy(deadLetterPolicyConf);
		}

	}

	/**
	 * Jackson serializer for {@link Range}.
	 */
	public static class RangeDeserializer extends JsonDeserializer<Range> {

		@Override
		public Range deserialize(JsonParser p, DeserializationContext context) throws IOException {
			RangeConf rangeConf = p.readValueAs(RangeConf.class);
			return ConverterUtils.toRange(rangeConf);
		}

	}

	/**
	 * Jackson serializer for {@link Class}.
	 *
	 * @param <T> the type of class
	 */
	public static class ClassSerializer<T> extends JsonSerializer<T> {

		@Override
		public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeStartObject();
			gen.writeStringField("className", value.getClass().getName());
			gen.writeEndObject();
		}

	}

	/**
	 * Jackson deserializer for {@link Class}.
	 *
	 * @param <T> the type of class
	 */
	public static class ClassDeserializer<T> extends JsonDeserializer<T> {

		@Override
		public T deserialize(JsonParser p, DeserializationContext context) throws IOException {
			ClassConf classConf = p.readValueAs(ClassConf.class);
			return ConverterUtils.toClass(classConf);
		}

	}

}
