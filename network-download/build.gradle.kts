plugins {
    id("net.android.library")
    id("net.android.publish")
}

android {
    namespace = "com.zion.network_download"
}

dependencies {
    implementation(project(":network-core"))
    implementation(project(":network-http"))

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}