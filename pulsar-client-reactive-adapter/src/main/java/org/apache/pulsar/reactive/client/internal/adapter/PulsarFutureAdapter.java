/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.reactive.client.internal.adapter;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.apache.pulsar.client.api.PulsarClientException;
import reactor.core.publisher.Mono;

/**
 * Stateful adapter from CompletableFuture to Mono which keeps a reference to the original
 * future so that it can be cancelled. Cancellation is necessary for some cases to release
 * resources.
 * <p>
 * There's additional logic to ignore Pulsar client's
 * {@link org.apache.pulsar.client.api.PulsarClientException.AlreadyClosedException} when
 * the Mono has been cancelled. This is to reduce unnecessary exceptions in logs.
 */
final class PulsarFutureAdapter {

	private volatile boolean cancelled;

	private volatile CompletableFuture<?> futureReference;

	private PulsarFutureAdapter() {
	}

	static <T> Mono<T> adaptPulsarFuture(Supplier<? extends CompletableFuture<T>> futureSupplier) {
		return Mono.defer(() -> new PulsarFutureAdapter().toMono(futureSupplier));
	}

	private static void handleException(boolean cancelled, Throwable e) {
		if (cancelled) {
			rethrowIfRelevantException(e);
		}
		else {
			sneakyThrow(e);
		}
	}

	private static boolean isAlreadyClosedCause(Throwable e) {
		return (e instanceof PulsarClientException.AlreadyClosedException
				|| e.getCause() instanceof PulsarClientException.AlreadyClosedException);
	}

	private static void rethrowIfRelevantException(Throwable e) {
		if (!isAlreadyClosedCause(e) && !(e instanceof CancellationException)) {
			sneakyThrow(e);
		}
	}

	private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
		throw (E) e;
	}

	<T> Mono<? extends T> toMono(Supplier<? extends CompletableFuture<T>> futureSupplier) {
		return Mono.fromFuture(() -> createFuture(futureSupplier)).doOnCancel(this::doOnCancel);
	}

	private <T> CompletableFuture<T> createFuture(Supplier<? extends CompletableFuture<T>> futureSupplier) {
		try {
			CompletableFuture<T> future = futureSupplier.get();
			this.futureReference = future;
			return future.exceptionally((ex) -> {
				handleException(this.cancelled, ex);
				return null;
			});
		}
		catch (Exception ex) {
			handleException(this.cancelled, ex);
			return CompletableFuture.completedFuture(null);
		}
	}

	private void doOnCancel() {
		this.cancelled = true;
		CompletableFuture<?> future = this.futureReference;
		if (future != null) {
			future.cancel(false);
		}
	}

}
