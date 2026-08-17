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
	id("com.adarshr.test-logger")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

testlogger {
	showStandardStreams = true
}

val main: SourceSet = sourceSets[SourceSet.MAIN_SOURCE_SET_NAME]
val intTest: SourceSet = sourceSets.create("intTest") {
	compileClasspath += main.output
	runtimeClasspath += main.output
}

configurations[intTest.implementationConfigurationName]
	.extendsFrom(configurations[JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME])
configurations[intTest.runtimeOnlyConfigurationName]
	.extendsFrom(configurations[JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME])

// `isolated` keeps this compatible with configuration caching and project isolation.
val sharedTestResources = files(isolated.rootProject.projectDirectory.dir("shared-test-resources"))

dependencies {
	// add shared-test-resources directory to runtime classpath
	intTest.runtimeOnlyConfigurationName(sharedTestResources)
	JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME(sharedTestResources)
	intTest.runtimeOnlyConfigurationName(libs.findLibrary("junit-platform-launcher").get())
}

val integrationTest = tasks.register<Test>("integrationTest") {
	description = "Runs integration tests."
	group = LifecycleBasePlugin.VERIFICATION_GROUP

	testClassesDirs = intTest.output.classesDirs
	classpath = intTest.runtimeClasspath
	shouldRunAfter(tasks.named(JavaPlugin.TEST_TASK_NAME))
}

tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
	dependsOn(integrationTest)
}

tasks.withType<Test>().configureEach {
	// reduces CPU usage in tests when JIT compiler doesn't spend time compiling code
	// this could help reduce flakiness in CI where there's less CPU resources available
	jvmArgs("-XX:TieredStopAtLevel=1")
}
