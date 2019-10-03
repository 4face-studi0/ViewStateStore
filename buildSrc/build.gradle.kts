plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
}

dependencies {
    val android =       "3.6.0-alpha12" // Updated: Sep 18, 2019
    val easyGradle =    "0.32"          // Updated: Oct 03, 2019

    implementation("com.android.tools.build:gradle:$android")
    implementation("studio.forface.easygradle:dsl-android:$easyGradle")
}
