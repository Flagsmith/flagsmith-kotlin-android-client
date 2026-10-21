// Builds the SDK for the JVM together with the steps file and the spec's Kotlin harness:
// gradle -p sdk-spec installDist -Pharness=<sdk-spec>/harness/kotlin
// The stubs in src/main/java stand in for the Android framework classes the SDK references.
plugins {
    kotlin("jvm").version("1.8.0")
    application
}

sourceSets {
    main {
        kotlin.srcDirs("../FlagsmithClient/src/main/java", providers.gradleProperty("harness"))
    }
}

tasks.compileKotlin {
    source("steps.kt")
}

dependencies {
    // Keep in sync with FlagsmithClient/build.gradle.kts
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.11.0")
    // Part of the Android framework
    implementation("org.json:json:20231013")
}

application {
    mainClass.set("Harness")
}
