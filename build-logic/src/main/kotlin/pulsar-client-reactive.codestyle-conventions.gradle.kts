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
	checkstyle
	id("io.spring.javaformat")
	id("com.diffplug.spotless")
}

// The version catalog has no type-safe accessors inside a precompiled script plugin, so it
// has to be looked up through the extension.
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// `isolated` keeps this compatible with configuration caching and project isolation.
val checkstyleConfigDir = isolated.rootProject.projectDirectory.dir("checkstyle")
val licenseHeader = checkstyleConfigDir.file("HEADER.txt")

dependencies {
	checkstyle(libs.findLibrary("spring-javaformat-checkstyle").get())
}

checkstyle {
	toolVersion = libs.findVersion("checkstyle").get().requiredVersion
	// `configFile` defaults to `checkstyle.xml` inside `configDirectory`
	configDirectory = checkstyleConfigDir
}

// Formatting of Java sources is owned by spring-javaformat; Spotless only enforces the ASF
// license header. Apache RAT is the repository-wide backstop for every other file type.
spotless {
	kotlinGradle {
		target("*.gradle.kts")
		licenseHeaderFile(licenseHeader, "(plugins|import|rootProject|pluginManagement|dependencyResolutionManagement)")
	}
}

// `java` has no sources to point Spotless at until the `java` plugin is applied, which
// happens after this convention plugin. Reacting to the plugin avoids `afterEvaluate`.
pluginManager.withPlugin("java") {
	spotless {
		java {
			licenseHeaderFile(licenseHeader)
		}
	}
}
