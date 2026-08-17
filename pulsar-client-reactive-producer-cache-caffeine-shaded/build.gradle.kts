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

import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.specs.Spec

plugins {
	id("pulsar-client-reactive.codestyle-conventions")
	id("pulsar-client-reactive.library-conventions")
	alias(libs.plugins.shadow)
}

dependencies {
	api(project(":pulsar-client-reactive-adapter"))
	implementation(libs.caffeine)
	shadow(project(":pulsar-client-reactive-adapter"))

	testImplementation(libs.junit.jupiter)
	testImplementation(libs.assertj.core)
	testImplementation(libs.reactor.test)
	testImplementation(libs.mockito.core)
}

description = "Caffeine (shaded) implementation of producer cache"

// The shadowed jar is published as the main artifact, so the java component must not also
// expose a separate shadow variant.
shadow {
	addShadowVariantIntoJavaComponent = false
}

tasks.jar {
	archiveClassifier = "original"
}

val relocatedGroups = setOf("com.github.ben-manes.caffeine", "org.checkerframework", "com.google.errorprone")

tasks.shadowJar {
	archiveClassifier = ""
	relocate("com.github.benmanes.caffeine", "org.apache.pulsar.reactive.shade.com.github.benmanes.caffeine")
	relocate("com.google", "org.apache.pulsar.reactive.shade.com.google")
	relocate("org.checkerframework", "org.apache.pulsar.reactive.shade.org.checkerframework")
	dependencies {
		include(Spec<ResolvedDependency> { it.moduleGroup in relocatedGroups })
	}
}

// disable module metadata - otherwise original jar will be used when published
tasks.withType<GenerateModuleMetadata>().configureEach {
	enabled = false
}

publishing {
	publications {
		named<MavenPublication>("mavenJava") {
			artifact(tasks.shadowJar)
			// the relocated dependencies are bundled into the shadowed jar, so they must not
			// be declared as dependencies of the published module
			pom.withXml {
				(asNode()["dependencies"] as NodeList).filterIsInstance<Node>().forEach { dependencies ->
					dependencies.children().removeIf {
						it is Node && (it["artifactId"] as NodeList).text() == "caffeine"
					}
				}
			}
		}
	}
}
