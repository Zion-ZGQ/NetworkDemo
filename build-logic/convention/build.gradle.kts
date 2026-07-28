plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.library) apply false
}
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies { //为“编写插件的代码”提供编译期依赖
    // 提示：确保你的主项目 libs.versions.toml 的 [libraries] 节点下有对应的别名配置
    implementation(libs.android.gradlePlugin)
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
}