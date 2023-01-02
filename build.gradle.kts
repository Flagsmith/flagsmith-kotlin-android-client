plugins {
    id("com.android.application").version("7.3.0").apply(false)
    id("com.android.library").version("7.3.0").apply(false)
    kotlin("android").version("1.7.20").apply(false)
}

val clean by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}