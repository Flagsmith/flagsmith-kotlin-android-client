import groovy.time.TimeCategory
import kotlinx.kover.api.CounterType
import kotlinx.kover.api.VerificationTarget
import kotlinx.kover.api.VerificationValueType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.ByteArrayOutputStream
import java.util.Date

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
}

val versionNumber: String by lazy {
    val stdout = ByteArrayOutputStream()
    rootProject.exec {
        isIgnoreExitValue = true
        commandLine("git", "describe", "--tags", "--abbrev=0")
        standardOutput = stdout
        errorOutput = ByteArrayOutputStream()
    }
    val version = stdout.toString().trim().replace("v", "")
    return@lazy version.ifEmpty { "0.1.0" }
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        version = versionNumber
        namespace = "com.flagsmith.kotlin"
        aarMetadata {
            minCompileSdk = 31
        }
        testFixtures {
            enable = true
        }
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
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // HTTP Client
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Server Sent Events
    implementation("com.squareup.okhttp3:okhttp-sse:4.11.0")
    testImplementation("com.squareup.okhttp3:okhttp-sse:4.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.mock-server:mockserver-netty-no-dependencies:5.14.0")

    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
}

kover {
    filters {
        classes {
            excludes += listOf("${android.namespace}.BuildConfig")
        }
    }
    verify {
        rule {
            target = VerificationTarget.ALL
            bound {
                minValue = 60
                maxValue = 100
                counter = CounterType.LINE
                valueType = VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

tasks.withType(Test::class) {
    // if the excludeIntegrationTests property is set
    // then exclude tests with IntegrationTest in the name
    // i.e. `gradle :FlagsmithClient:testDebugUnitTest --tests "com.flagsmith.*" -P excludeIntegrationTests`
    if (project.hasProperty("excludeIntegrationTests")) {
        exclude {
            it.name.contains("IntegrationTest")
        }
    }

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
            println(
                """
                ${tableLine(lineLength, "┌", "┐")}
                ${suiteDescription.contentLine(lineLength)}
                $separator
                $fullSummaryLine
                $separator
                ${duration.contentLine(lineLength)}
                ${tableLine(lineLength, "└", "┘")}
                Report: file:///${this.reports.html.entryPoint}
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
            groupId = "com.flagsmith"
            artifactId = "flagsmith-kotlin-android-client"
            version = versionNumber

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
