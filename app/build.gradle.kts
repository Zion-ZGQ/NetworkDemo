plugins {
    id("net.android.application")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.zion.networkdemo"

    defaultConfig {
        applicationId = "com.zion.networkdemo"
    }

    // 🎯 就是这里！加入以下 packaging 冲突过滤器，彻底消灭 Netty 打架问题
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            // 顺便多屏蔽几个 Netty/HiveMQ 常见的冲突文件，防患于未然
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/licenses/**"
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        // 🎯 核心武器 1：强制激活核心库脱糖工具，把 Java 8 所有的 Lambda 翻译成 5.0 能看懂的老旧代码
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 🎯 核心武器 2：在依赖里亲手塞入官方的脱糖无敌补丁包！
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}