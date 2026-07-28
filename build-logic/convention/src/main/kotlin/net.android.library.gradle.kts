import com.android.build.api.dsl.LibraryExtension
import org.gradle.kotlin.dsl.configure

// 1. 自动为引入此插件的子模块勾选官方的 Android Library 插件
plugins {
    id("com.android.library")
}
extensions.configure<LibraryExtension>{
    // 调用AndroidCommon中的方法，配置相关的版本信息
    configureCommonAndroid(this)
}