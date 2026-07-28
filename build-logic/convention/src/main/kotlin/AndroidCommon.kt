import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * 全局获取外部宿主主项目名称（全局单一真理源）
 * 优先取 rootProject 的名称，支持复合构建（Composite Build）溯源
 */
val Project.hostProjectName: String
    get() = gradle.parent?.rootProject?.name?:rootProject.name

val Project.fullModuleName: String
    get() = "${hostProjectName}-${name}"
/**
 * 统一模块中的gradle配置，简单来说就是把 app 和 library 中重复的部分抽出来，在这里统一写，
 * 之后在各自的gradle文件中引用这个方法就可以了。
 */
internal fun Project.configureCommonAndroid(
    commonExtension: CommonExtension
) {
    // 获取到settings.gradle.kts中找到的主项目的版本信息文件
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    // 从版本库中获取定义的版本信息
    val tomlCompileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
    val tomlMinSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

    commonExtension.apply {
        // 1. 统一配置 SDK 版本
        compileSdk = tomlCompileSdk

        defaultConfig.apply {
            minSdk = tomlMinSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // 3. java版本
        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        // lint相关的设置
        lint.apply {
            abortOnError = true        // 发现致命级别缺陷，当场中断编译，不打包
            checkDependencies = true   // 顺便把当前模块依赖的其他子模块也扫描了
            warningsAsErrors = false   // 警告可以不报错，但会在报告里显现
            htmlReport = true          // 全自动生成网页版安检报告，方便去浏览器里复查缺陷
        }
    }

    // 【架构层强制锁死】：确保应用此基础配置的所有模块（App/Lib），协程版本绝对统一
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines")) {
                    useVersion("1.7.3")
                    because("为了完美兼容 Android 5.0 设备，由架构层强制对齐协程版本，防止版本割裂导致闪退")
                }
            }
        }
    }
}

fun myTest() {
    println("自定义方法，根据需要进行配置")
}

/**
 * 配置打包信息
 */
internal fun Project.configureMultiChannels(
    appExtension: ApplicationExtension
) {
    appExtension.apply {
        // 🎯 1. 强制开启 BuildConfig 动态类生成（AGP 9+ 默认关闭了此功能）
        buildFeatures {
            buildConfig = true
        }

        // 🎯 2. 定义两个独立的维度：环境、市场渠道
        flavorDimensions += listOf("environment", "market")

        productFlavors {
            // ======= 维度一：网络环境 =======
            create("dev") {
                dimension = "environment"
                applicationIdSuffix = ".dev" // 测试版包名追加 .dev，支持与正式版共存

                // 🚀【核心武器】：动态注入测试环境的 MQTT 服务器 URL
                buildConfigField("String", "MQTT_SERVER_URL", "\"tcp://broker.hivemq.com:1883\"")
            }
            create("prod") {
                dimension = "environment"
                // 正式环境的真实安全域名
                buildConfigField("String", "MQTT_SERVER_URL", "\"tcp://broker.hivemq.com:1883\"")
            }

            // ======= 维度二：国内各大主流市场渠道 =======
            val markets = listOf(
                "google", "official", "huawei", "xiaomi",
                "oppo", "vivo", "realme", "oneplus", "iqoo"
            )

            markets.forEach { marketName ->
                create(marketName) {
                    dimension = "market"
                    // 🚀【核心武器】：给每一个商店包，自动打入独一无二的“渠道烙印”
                    buildConfigField("String", "MARKET_CHANNEL", "\"$marketName\"")
                }
            }
        }
    }
}