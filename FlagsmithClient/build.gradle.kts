import groovy.time.TimeCategory
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Date

plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        version = "1.0.0"
        namespace = "com.flagsmith.kotlin"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.mock-server:mockserver-netty-no-dependencies:5.14.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}

tasks.withType(Test::class) {
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT
        )
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL

        debug {
            events(
                TestLogEvent.STARTED,
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat
    }

    afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) {
            val summary = "Results: ${result.resultType} " +
                    "(" +
                    "${result.testCount} tests, " +
                    "${result.successfulTestCount} passed, " +
                    "${result.failedTestCount} failed, " +
                    "${result.skippedTestCount} skipped" +
                    ")"
            val fullSummaryLine = summary.contentLine(summary.length)
            val lineLength = fullSummaryLine.length
            val suiteDescription = "${this.project.name}:${this.name}"
            val duration = "in ${TimeCategory.minus(Date(result.endTime), Date(result.startTime))}"
            val separator = tableLine(lineLength, "│", "│")
            println("""
                ${tableLine(lineLength, "┌", "┐")}
                ${suiteDescription.contentLine(lineLength)}
                $separator
                $fullSummaryLine
                $separator
                ${duration.contentLine(lineLength)}
                ${tableLine(lineLength, "└", "┘")}
                Report file: ./${this.reports.html.entryPoint.relativeTo(rootProject.rootDir)}
            """.trimIndent()
            )
        }
    }))
}

fun String.padToLength(length: Int) =
    this + " ".repeat(maxOf(length - this.length, 0))

fun String.wrapWith(leading: String, trailing: String = leading) =
    "$leading$this$trailing"

fun String.contentLine(length: Int, extraPadding: String = "  ") =
    "$extraPadding$this$extraPadding".padToLength(length - 2)
        .wrapWith("│")

fun tableLine(length: Int, leading: String, trailing: String) =
    "─".repeat(length - 2).wrapWith(leading, trailing)

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.Flagsmith"
            artifactId = "flagsmith-kotlin-android-client"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}