import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import org.gradle.plugins.signing.SigningExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByType

// 1. 自动为引入此插件的子模块勾选底层官方插件
plugins {
    id("com.vanniktech.maven.publish")
    id("signing")
}
// 发布配置
extensions.configure<MavenPublishBaseExtension> {
    // 指明发布到manven
    publishToMavenCentral()

    /* 使用vanniktech的签名时开启。通知 Vanniktech 插件，我们要启用签名流程
     * 它会自动把编译出的 aar, sources.jar, javadoc.jar 全部加入待签名队列*/
//    signAllPublications()

    // 指明Maven仓库的唯一id
    coordinates(project.publish_group, project.name, project.publishVersion)
    // 中央仓库硬性要求的开源协议和项目描述（没有这些，Sonatype 审核会直接拦截）
    pom {
        name.set("${hostProjectName} - ${project.name}")
        description.set(PublishingDefaults.description)
        inceptionYear.set(PublishingDefaults.inceptionYear)
        url.set(PublishingDefaults.url)

        licenses {
            license {
                name.set(PublishingDefaults.POM_LICENCE_NAME)
                url.set(PublishingDefaults.POM_LICENCE_URL)
            }
        }
        developers {
            developer {
                id.set(PublishingDefaults.POM_DEVELOPER_ID)
                name.set(PublishingDefaults.POM_LICENCE_NAME)
                email.set(PublishingDefaults.POM_DEVELOPER_EMAIL)
            }
        }
        scm {
            // 只读网络通道
            connection.set(PublishingDefaults.SCM_CONNECTION)
            // 核心开发者写入通道
            developerConnection.set(PublishingDefaults.SCM_DEV_CONNECTION)
            // 网页浏览地址
            url.set(PublishingDefaults.SCM_URL)
        }
    }

    // 统一配置 Android 的发布变体和源码包生成
    configure(
        AndroidSingleVariantLibrary(
            variant = "release"
        )
    )
}

// ==================== 🏠 追加：本地独立文件夹全量卸货通道 ====================
extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "LocalBundle"
            // 定位到你当前主项目根目录的 build/repo 下
            // 彻底告别写死路径，放到项目的目录中
            url = uri("${rootProject.projectDir}/build/repo")
        }
    }
}

// 全局统一的数字钢印签名（自适应云端 GitHub Actions 或本地 Windows 环境）
extensions.configure<SigningExtension> {
    useGpgCmd() // 自动寻找你电脑里安装的 gpg.exe (Kleopatra)
    sign(extensions.getByType<PublishingExtension>().publications)
}
