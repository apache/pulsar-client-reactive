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

plugins {
	`java-library`
	id("pulsar-client-reactive.publish-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

java {
	withJavadocJar()
	withSourcesJar()
	// Pinning the toolchain decouples the build from whichever JDK happens to be on PATH
	// and keeps the compiled output reproducible.
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

tasks.named<JavaCompile>(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
	options.release = 8
}

tasks.withType<Javadoc>().configureEach {
	options.encoding = "UTF-8"
}

dependencies {
	testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<Test>().configureEach {
	useJUnitPlatform()
}

publishing {
	publications {
		named<MavenPublication>("mavenJava") {
			from(components["java"])
			versionMapping {
				usage(Usage.JAVA_API) {
					fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
				}
				usage(Usage.JAVA_RUNTIME) {
					fromResolutionResult()
				}
			}
		}
	}
}
