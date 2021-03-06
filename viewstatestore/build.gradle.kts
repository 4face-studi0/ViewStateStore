import studio.forface.easygradle.dsl.`coroutines-android`
import studio.forface.easygradle.dsl.`kotlin-jdk7`
import studio.forface.easygradle.dsl.android.`lifecycle-liveData`
import studio.forface.easygradle.dsl.android.publishAndroid
import studio.forface.easygradle.dsl.api
import studio.forface.easygradle.dsl.dokka

plugins {
    `android-library`
    `kotlin-android`
}

android { applyAndroidConfig() }

dependencies {
    applyAndroidTests()

    api(
        `kotlin-jdk7`,
        `coroutines-android`,
        `lifecycle-liveData`
    )
}

dokka()
publishAndroid(defaultPublishConfig) {
    projectName = "viewstatestore"
    artifact = "viewstatestore"
}
