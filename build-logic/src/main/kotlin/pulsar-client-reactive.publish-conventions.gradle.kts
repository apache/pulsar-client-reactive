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
	`maven-publish`
	signing
}

val publishDebug = providers.gradleProperty("publishDebug")
val isSnapshot = provider { version.toString().endsWith("-SNAPSHOT") }

// `project.description` is assigned at the bottom of each module's build script, so it has
// to be read lazily rather than when this convention plugin is applied.
val projectDescription = objects.property<String>().value(provider { description })

publishing {
	repositories {
		maven {
			if (publishDebug.isPresent) {
				url = uri(publishDebug.get())
			}
			else {
				name = "asf"
				url = uri(
					if (isSnapshot.get()) "https://repository.apache.org/content/repositories/snapshots/"
					else "https://repository.apache.org/service/local/staging/deploy/maven2"
				)
				credentials(PasswordCredentials::class)
			}
		}
	}
	publications {
		create<MavenPublication>("mavenJava") {
			pom {
				name = project.name
				description = projectDescription
				url = "https://github.com/apache/pulsar-client-reactive"
				licenses {
					license {
						name = "The Apache License, Version 2.0"
						url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
						distribution = "repo"
					}
				}
				developers {
					developer {
						id = "lhotari"
						name = "Lari Hotari"
						email = "lhotari@apache.org"
					}
					developer {
						id = "cbornet"
						name = "Christophe Bornet"
						email = "cbornet@apache.org"
					}
				}
				scm {
					connection = "scm:git:https://github.com/apache/pulsar-client-reactive.git"
					developerConnection = "scm:git:https://github.com/apache/pulsar-client-reactive.git"
					url = "https://github.com/apache/pulsar-client-reactive"
				}
			}
		}
	}
}

signing {
	setRequired(!publishDebug.isPresent)
	sign(publishing.publications["mavenJava"])
}
