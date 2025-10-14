#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
set -xe -o pipefail

# Validate that the staging repo contains the expected artifacts
# by building a simple application with Gradle that uses the artifacts.

VERSION=$1
STAGING_REPO=$2

if [[ -z "$VERSION" || -z "$STAGING_REPO" ]]; then
    echo "Usage: $0 <version> <staging repo>"
    exit 1
fi

if ! command -v gradle &>/dev/null; then
    set +x
    echo "gradle not found in PATH"
    echo "Please install gradle with instructions available at https://gradle.org/install/"
    echo "The simplest way to install gradle is with sdkman: https://sdkman.io/"
    echo "sdk install gradle"
    exit 1
fi

DOCKER_CONTAINER_NAME=pulsar-standalone-$$
: ${DOCKER_IMAGE_NAME:=apachepulsar/pulsar:4.1.1}

mkdir test-app-reactive-$$
cd test-app-reactive-$$

cat >build.gradle <<EOF
plugins {
    id 'application'
}

repositories {
    mavenCentral()
    maven {
        url '${STAGING_REPO}'
    }
}


dependencies {
    implementation "org.apache.pulsar:pulsar-client-reactive-adapter:${VERSION}"
    implementation "org.apache.pulsar:pulsar-client-reactive-producer-cache-caffeine:${VERSION}"
}

application {
    mainClass = 'HelloPulsarClientReactive'
}
EOF

cat >settings.gradle <<EOF
rootProject.name = 'test-app-reactive'
EOF

mkdir -p src/main/java
cat >src/main/java/HelloPulsarClientReactive.java <<EOF
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.reactive.client.adapter.AdaptedReactivePulsarClientFactory;
import org.apache.pulsar.reactive.client.api.MessageSpec;
import org.apache.pulsar.reactive.client.api.ReactiveMessagePipeline;
import org.apache.pulsar.reactive.client.api.ReactiveMessageSender;
import org.apache.pulsar.reactive.client.api.ReactivePulsarClient;
import reactor.core.publisher.Mono;

public class HelloPulsarClientReactive {

    public static void main(String[] args) throws PulsarClientException, InterruptedException {
        // Before running this, start Pulsar within docker with this command:
        // docker run -it -p 8080:8080 -p 6650:6650 apachepulsar/pulsar:4.1.1 /pulsar/bin/pulsar standalone -nss -nfw

        try (PulsarClient pulsarClient = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").build()) {

            ReactivePulsarClient reactivePulsarClient = AdaptedReactivePulsarClientFactory.create(pulsarClient);

            CountDownLatch latch = new CountDownLatch(3);

            ReactiveMessagePipeline reactiveMessagePipeline = reactivePulsarClient.messageConsumer(Schema.STRING)
                    .subscriptionName("sub").topic("testreactive").build().messagePipeline()
                    .messageHandler(message -> Mono.fromRunnable(() -> {
                        System.out.println("Received message:" + message.getValue());
                        latch.countDown();
                    })).build().start();

            ReactiveMessageSender<String> messageSender = reactivePulsarClient.messageSender(Schema.STRING)
                    .topic("testreactive").cache(AdaptedReactivePulsarClientFactory.createCache()).build();

            messageSender.sendOne(MessageSpec.of("Hello world!")).block();
            messageSender.sendOne(MessageSpec.of("Hello Pulsar!")).block();
            messageSender.sendOne(MessageSpec.of("Hello Pulsar Client Reactive!")).block();

            if(!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Did not receive all messages");
            }
            reactiveMessagePipeline.stop();
        }
    }

}
EOF

gradle build

docker run --name $DOCKER_CONTAINER_NAME -d -p 8080:8080 -p 6650:6650 $DOCKER_IMAGE_NAME /pulsar/bin/pulsar standalone -nss -nfw
trap "docker rm -f $DOCKER_CONTAINER_NAME" EXIT
while ! curl http://localhost:8080/metrics > /dev/null 2>&1; do
    echo "Waiting for Pulsar to start"
    sleep 1
done
gradle run
