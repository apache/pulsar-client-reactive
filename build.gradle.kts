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
	alias(libs.plugins.rat)
	alias(libs.plugins.versions)
	alias(libs.plugins.version.catalog.update)
	id("pulsar-client-reactive.codestyle-conventions")
}

// A single RAT run over the whole repository; it already covers every subproject directory.
tasks.rat {
	inputDir = layout.projectDirectory
	setExcludes(
		listOf(
			".asf.yaml", // ASF metadata for github integration excluded from src zip
			"**/.gradle/**", "**/wrapper/**", "gradlew*", // gradle wrapper files excluded from src zip
			"gradle.properties", // artifactory release plugin removes header when bumping version
			"**/build/**", "**/target/**", "licenses/**", "notices/**",
			"**/META-INF/services/*",
			"out/**", "*.ipr", "**/*.iml", "*.iws", ".idea/**", // Intellij files
			".classpath", ".project", ".settings/**", "bin/**", // Eclipse files
		)
	)
}

tasks.register<Tar>("sourceTar") {
	description = "Assembles a source distribution archive of the whole repository."
	group = BasePlugin.BUILD_GROUP

	destinationDirectory = layout.buildDirectory
	archiveBaseName = "pulsar-client-reactive-$version"
	archiveClassifier = "src"
	archiveExtension = "tar.gz"
	compression = Compression.GZIP
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	includeEmptyDirs = false
	into(archiveBaseName) {
		from(layout.projectDirectory) {
			exclude(
				".github/**", ".asf.yaml",
				"**/build/**", "**/target/**",
				"out/**", "*.ipr", "**/*.iml", "*.iws", ".idea/**", // Intellij files
				".classpath", ".project", ".settings/**", "bin/**", // Eclipse files
				"**/.gradle/**", "**/wrapper/**", "gradlew*", // gradle wrapper files excluded from src zip
			)
		}
	}
}

tasks.dependencyUpdates {
	rejectVersionIf {
		listOf("alpha", "beta", "rc").any { candidate.version.contains(it) }
	}
}

// Note: `versionCatalogUpdate` reads `Task.project` at execution time, so it discards the
// configuration cache entry. It is a maintenance task outside the assemble/check flows.
versionCatalogUpdate {
	keep {
		// keep versions that no library or plugin entry references
		keepUnusedVersions = true
	}
}
