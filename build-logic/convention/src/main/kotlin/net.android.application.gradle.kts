import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    id("com.android.application")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

extensions.configure<ApplicationExtension>{
    // 引入AndroidCommon中的方法
    // 1. 灌注公共的 compileSdk、minSdk 和 Java 11 配置
    configureCommonAndroid(this)
    // 2. 打包相关的配置
    configureMultiChannels(this)
    // 3. 灌注当前 App 壳模块独享的 targetSdk 现代属性
    defaultConfig {
        // 从 TOML 提取并强转 Int
        targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()

        // 🎯 新增：从 TOML 提取 versionCode 并强转 Int
        versionCode = libs.findVersion("app-versionCode").get().requiredVersion.toInt()

        // 🎯 新增：从 TOML 提取 versionName 字符串
        versionName = libs.findVersion("app-versionName").get().requiredVersion
    }
}

// 重命名打包文件
// 1. 获取次 Android 组件扩展器
val androidComponents = extensions.getByType<ApplicationAndroidComponentsExtension>()

// 2. 监听变体诞生事件，在打包的一瞬间切入
androidComponents.onVariants { variant ->
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())

    // 遍历当前打包出来的所有输出产物（通常是 Release 出来的 APK）
    variant.outputs.forEach { output ->
        // 动态拼装大厂标准的规范命名：项目名_变体组合(如prodHuaweiRelease)_版本号_打包时间.apk
        val customApkName = "${hostProjectName}_${variant.name}_v${output.versionName.get()}_$timestamp.apk"

        // 🚀 使用现代 Property 机制完美注入，绝不破坏底层 output-metadata.json 映射
        output.outputFileName.set(customApkName)
    }
}