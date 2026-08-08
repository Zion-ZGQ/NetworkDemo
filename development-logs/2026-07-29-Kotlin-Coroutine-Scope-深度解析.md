# Kotlin 协程 Scope 深度解析

## 📅 日期：2026-07-29

---

## 🎯 核心概念：什么是 Scope？

**Scope（作用域）** 定义了协程的**生命周期边界**：

```
Scope = 协程的生命周期管理器
    ↓
决定：
    • 何时启动协程
    • 何时取消协程
    • 在哪个线程执行
    • 如何处理异常
```

---

## 📊 所有 Scope 分类

### 第一类：全局作用域

| Scope | 生命周期 | 内存泄漏风险 | 使用场景 |
|-------|---------|------------|---------|
| **GlobalScope** | 应用进程级别 | ⚠️ **极高** | 几乎不用 |

```kotlin
// ❌ 不推荐：GlobalScope（生命周期不可控）
GlobalScope.launch {
    // 应用进程死亡时才结束
    // 容易导致内存泄漏
}
```

**为什么不推荐？**
- ❌ 无法手动取消
- ❌ 生命周期不可控
- ❌ 容易内存泄漏

---

### 第二类：Android 组件作用域

#### 1. MainScope()

```kotlin
// ✅ 手动管理的顶级作用域
class MyApplication : Application() {
    private val mainScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        mainScope.launch {
            // 在主线程执行
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        mainScope.cancel()  // ← 必须手动取消！
    }
}
```

**特点：**
- ✅ 在主线程执行
- ⚠️ 需要手动取消
- ✅ 适合 Application 级别

---

#### 2. lifecycleScope

```kotlin
// ✅ Activity/Fragment 生命周期绑定
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 自动绑定 Activity 生命周期
        lifecycleScope.launch {
            // Activity 销毁时自动取消
            delay(1000)
            textView.text = "Hello"
        }

        // 在 IO 线程执行
        lifecycleScope.launch(Dispatchers.IO) {
            val data = fetchFromNetwork()
            withContext(Dispatchers.Main) {
                textView.text = data
            }
        }
    }
}
```

**特点：**
- ✅ 自动绑定 Activity/Fragment 生命周期
- ✅ Activity 销毁时自动取消
- ✅ 防止内存泄漏
- ✅ 最常用

**依赖：**
```kotlin
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
```

---

#### 3. viewModelScope

```kotlin
// ✅ ViewModel 生命周期绑定
class MyViewModel : ViewModel() {

    fun loadData() {
        viewModelScope.launch {
            // ViewModel 清除时自动取消
            val data = repository.fetchData()
            _data.value = data
        }
    }

    // 自定义异常处理
    fun loadDataWithCatch() {
        viewModelScope.launch {
            try {
                val data = repository.fetchData()
                _data.value = data
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
```

**特点：**
- ✅ 自动绑定 ViewModel 生命周期
- ✅ ViewModel 清除时自动取消
- ✅ 最佳实践（配合 ViewModel）
- ✅ 自动处理屏幕旋转

**依赖：**
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
```

---

#### 4. rememberCoroutineScope (Compose)

```kotlin
// ✅ Compose 生命周期绑定
@Composable
fun MyScreen() {
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            // Composable 销毁时自动取消
            delay(1000)
            // 执行操作
        }
    }) {
        Text("Click me")
    }
}

// ❌ 错误用法：在 Composable 函数内部直接 launch
@Composable
fun BadExample() {
    // ❌ 不能在 Composable 函数内直接启动协程
    // launch { ... }  // 编译错误！
}
```

**特点：**
- ✅ Compose 专用
- ✅ Composable 销毁时自动取消
- ✅ 在事件处理器中使用（如 onClick）

**依赖：**
```kotlin
implementation("androidx.compose.runtime:runtime:1.7.8")
```

---

### 第三类：协程构建器作用域

#### 5. runBlocking {}

```kotlin
// ⚠️ 阻塞当前线程（测试用）
fun main() = runBlocking {
    println("Start")
    delay(1000)  // ← 阻塞主线程 1 秒
    println("End")
}
```

**特点：**
- ⚠️ **阻塞当前线程**
- ⚠️ 不适合 Android UI 线程
- ✅ 适合单元测试
- ✅ 适合 main 函数

**使用场景：**
```kotlin
// ✅ 单元测试
@Test
fun testNetworkRequest() = runBlocking {
    val result = repository.fetchData()
    assertEquals("expected", result)
}

// ✅ main 函数
fun main() = runBlocking {
    val data = fetchData()
    println(data)
}
```

---

#### 6. coroutineScope {}

```kotlin
// ✅ 挂起函数作用域
suspend fun fetchAllData(): List<Data> = coroutineScope {
    // 并发执行多个请求
    val deferred1 = async { fetchFromApi1() }
    val deferred2 = async { fetchFromApi2() }
    val deferred3 = async { fetchFromApi3() }

    // 等待所有请求完成
    deferred1.await() + deferred2.await() + deferred3.await()
}
```

**特点：**
- ✅ 挂起函数内部使用
- ✅ 子协程全部完成后才返回
- ✅ 异常会取消其他子协程
- ✅ 结构化并发

---

#### 7. supervisorScope {}

```kotlin
// ✅ 独立异常处理
suspend fun loadAllData() = supervisorScope {
    val job1 = launch {
        // 子协程1
        throw Exception("Error in job1")  // ← 失败
    }

    val job2 = launch {
        // 子协程2
        delay(1000)
        println("Job2 completed")  // ← 继续执行
    }

    job1.join()
    job2.join()
}
```

**特点：**
- ✅ 子协程异常互不影响
- ✅ 适合独立任务
- ✅ 一个失败不影响其他

---

### 第四类：自定义作用域

#### 8. CoroutineScope()

```kotlin
// ✅ 完全自定义
class MyRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun loadData() {
        scope.launch {
            // 在 IO 线程执行
        }
    }

    fun clear() {
        scope.cancel()  // ← 必须手动取消！
    }
}
```

**特点：**
- ✅ 完全自定义
- ⚠️ 需要手动管理生命周期
- ✅ 适合自定义组件

---

## 📊 对比表格

| Scope | 自动取消 | 线程 | 适用场景 | 推荐度 |
|-------|---------|------|---------|--------|
| **GlobalScope** | ❌ 否 | Default | 几乎不用 | ⭐ |
| **MainScope()** | ⚠️ 手动 | Main | Application | ⭐⭐⭐ |
| **lifecycleScope** | ✅ 自动 | Main | Activity/Fragment | ⭐⭐⭐⭐⭐ |
| **viewModelScope** | ✅ 自动 | Main | ViewModel | ⭐⭐⭐⭐⭐ |
| **rememberCoroutineScope** | ✅ 自动 | Main | Compose | ⭐⭐⭐⭐⭐ |
| **runBlocking {}** | ✅ 自动 | 当前线程 | 测试/main | ⭐⭐⭐ |
| **coroutineScope {}** | ✅ 自动 | 继承父作用域 | 挂起函数 | ⭐⭐⭐⭐ |
| **supervisorScope {}** | ✅ 自动 | 继承父作用域 | 独立任务 | ⭐⭐⭐⭐ |

---

## 🎯 最佳实践

### 场景一：Activity/Fragment
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 使用 lifecycleScope
        lifecycleScope.launch {
            val data = fetchData()
            textView.text = data
        }
    }
}
```

---

### 场景二：ViewModel
```kotlin
class MyViewModel : ViewModel() {
    fun loadData() {
        // ✅ 使用 viewModelScope
        viewModelScope.launch {
            val data = repository.fetchData()
            _data.value = data
        }
    }
}
```

---

### 场景三：Compose
```kotlin
@Composable
fun MyScreen() {
    // ✅ 使用 rememberCoroutineScope
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            val data = fetchData()
            // 更新 UI
        }
    }) {
        Text("Load")
    }
}
```

---

### 场景四：Repository
```kotlin
class MyRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun fetchData() = scope.async {
        // 网络请求
    }

    fun clear() {
        scope.cancel()  // ← 手动取消
    }
}
```

---

### 场景五：挂起函数
```kotlin
// ✅ 使用 coroutineScope
suspend fun fetchAllData(): List<Data> = coroutineScope {
    val deferred1 = async { fetchApi1() }
    val deferred2 = async { fetchApi2() }
    deferred1.await() + deferred2.await()
}

// ✅ 使用 supervisorScope（独立异常）
suspend fun loadAllData() = supervisorScope {
    val job1 = launch { loadApi1() }
    val job2 = launch { loadApi2() }
    job1.join()
    job2.join()
}
```

---

## ⚠️ 常见错误

### 错误一：在 Activity 中使用 GlobalScope
```kotlin
// ❌ 错误
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch {  // ← Activity 销毁后不会取消
            val data = fetchData()
            textView.text = data  // ← Activity 已销毁，崩溃！
        }
    }
}
```

---

### 错误二：在 ViewModel 中使用 lifecycleScope
```kotlin
// ❌ 错误
class MyViewModel : ViewModel() {
    fun loadData() {
        // lifecycleScope 需要 LifecycleOwner
        lifecycleScope.launch {  // ← 编译错误！
            // ...
        }
    }
}
```

---

### 错误三：在 Composable 中使用 GlobalScope
```kotlin
// ❌ 错误
@Composable
fun MyScreen() {
    GlobalScope.launch {  // ← Composable 销毁后不会取消
        // ...
    }
}
```

---

## 🎯 核心原则

```
1. 永远不要用 GlobalScope
2. 优先使用结构化并发
3. 选择生命周期最接近的 Scope
4. 让协程自动取消，不要手动管理
```

---

## 📋 选择指南

```
Activity/Fragment → lifecycleScope
ViewModel         → viewModelScope
Compose           → rememberCoroutineScope
Application       → MainScope() (手动取消)
Repository        → CoroutineScope(Dispatchers.IO)
挂起函数          → coroutineScope
测试              → runBlocking
```

---

## 💡 进阶理解

### 结构化并发的好处

```
父协程
├── 子协程1
├── 子协程2
└── 子协程3

当父协程取消时：
✅ 所有子协程自动取消
✅ 避免资源泄漏
✅ 异常自动传播
```

### Dispatcher 的选择

| Dispatcher | 适用场景 | 特点 |
|-----------|---------|------|
| **Dispatchers.Main** | UI 操作、Android 组件交互 | 主线程 |
| **Dispatchers.IO** | 网络、数据库、文件操作 | IO 密集型 |
| **Dispatchers.Default** | CPU 密集型任务、排序、解析 | CPU 密集型 |
| **Dispatchers.Unconfined** | 特殊场景（不推荐） | 不限制线程 |

---

## 📚 参考资料

- [Kotlin Coroutines 官方文档](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android Coroutines 指南](https://developer.android.com/kotlin/coroutines)
- [Kotlin Flow 官方文档](https://kotlinlang.org/docs/flow.html)

---

**文档编写：** AI 助手
**文档版本：** v1.0
**最后更新：** 2026-07-29