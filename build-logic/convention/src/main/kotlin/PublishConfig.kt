import com.android.build.api.artifact.MultipleArtifact.MULTIDEX_KEEP_PROGUARD.name
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * 切换项目的时候，修改这个文件的配置项
 */
val Project.publish_group: String
    get() = "io.github.zion-zgq"

/**
 * 1. 项目名称转 CamelCase（例如：my-awesome-lib -> myAwesomeLib）
 * 用于映射 toml 文件中的 version 字段名称
 */
val Project.tomlVersionKey: String
    get() = name.split("-").mapIndexed { index, s ->
        if (index == 0) s else s.replaceFirstChar { it.uppercase() }
    }.joinToString("")

/**
 * 2. 自动从 libs.versions.toml 中解析对应的版本号
 * 找不到时降级兜底为 "1.0.0"
 */
val Project.publishVersion: String
    get() {
        val libs = extensions.findByType(VersionCatalogsExtension::class.java)?.named("libs")
        val versionOpt = libs?.findVersion(tomlVersionKey)
        return if (versionOpt != null && versionOpt.isPresent) {
            versionOpt.get().requiredVersion
        } else {
            "1.0.0"
        }
    }

/**
 * 3. 发布相关全局集中配置单例对象
 * 所有需要手工配置的项目元信息（Group、POM、Nexus地址等）统一在这里修改一次即可！
 */
object PublishingDefaults {

    const val description =
        "A high-availability, responsive NetworkUtilLib client SDK sub-module for Android:"
    const val inceptionYear = "2026"
    const val url = "https://github.com/Zion-ZGQ/FlexMqtt"

    // POM 基础配置（Maven Central 或私有 Nexus 必填）
    const val POM_DEVELOPER_ID = "Zion-ZGQ"
    const val POM_DEVELOPER_NAME = "ZGQ"
    const val POM_DEVELOPER_EMAIL = "1102564948@qq.com"

    const val POM_LICENCE_NAME = "The Apache Software License, Version 2.0"
    const val POM_LICENCE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"

    const val SCM_URL = "https://github.com/Zion-ZGQ/FlexMqtt/tree/main"
    const val SCM_CONNECTION = "scm:git:github.com/Zion-ZGQ/FlexMqtt.git"
    const val SCM_DEV_CONNECTION = "scm:git:ssh://github.com/Zion-ZGQ/FlexMqtt.git"

    // 私有 Maven 仓库默认地址 (支持环境变量或默认值)
    fun getReleaseRepositoryUrl(): String =
        System.getenv("MAVEN_RELEASE_URL")
            ?: "https://nexus.yourcompany.com/repository/maven-releases/"

    fun getSnapshotRepositoryUrl(): String =
        System.getenv("MAVEN_SNAPSHOT_URL")
            ?: "https://nexus.yourcompany.com/repository/maven-snapshots/"
}