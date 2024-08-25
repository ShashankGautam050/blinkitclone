buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
        classpath("com.google.gms:google-services:4.4.2")
        // In project-level build.gradle
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.1")

        // Add other classpaths for plugins you use
    }
}



