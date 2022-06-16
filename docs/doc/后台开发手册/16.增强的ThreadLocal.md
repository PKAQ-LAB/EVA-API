# 🔧 功能

👉 `TransmittableThreadLocal`(`TTL`)：在使用线程池等会池化复用线程的执行组件情况下，提供 `ThreadLocal`值的传递功能，解决异步执行时上下文传递的问题。一个 `Java`标准库本应为框架/中间件设施开发提供的标配能力，本库功能聚焦 & 0依赖，支持 `Java` 17/16/15/14/13/12/11/10/9/8/7/6。

`JDK`的[`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html)类可以完成父线程到子线程的值传递。但对于使用线程池等会池化复用线程的执行组件的情况，线程由线程池创建好，并且线程是池化起来反复使用的；这时父子线程关系的 `ThreadLocal`值传递已经没有意义，应用需要的实际上是把 **任务提交给线程池时**的 `ThreadLocal`值传递到 **任务执行时**。

本库提供的[`TransmittableThreadLocal`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)类继承并加强 `InheritableThreadLocal`类，解决上述的问题，使用详见 [User Guide](#-user-guide)。

整个 `TransmittableThreadLocal`库的核心功能（用户 `API`与框架/中间件的集成 `API`、线程池 `ExecutorService`/`ForkJoinPool`/`TimerTask`及其线程工厂的 `Wrapper`），只有 **_~1000 `SLOC`代码行_**，非常精小。

欢迎 👏

- 建议和提问，[提交 Issue](https://github.com/alibaba/transmittable-thread-local/issues/new)
- 贡献和改进，[Fork 后提通过 Pull Request 贡献代码](https://github.com/alibaba/transmittable-thread-local/fork)

# 🎨 需求场景

`ThreadLocal`的需求场景即 `TransmittableThreadLocal`的潜在需求场景，如果你的业务需要『在使用线程池等会池化复用线程的执行组件情况下传递 `ThreadLocal`值』则是 `TransmittableThreadLocal`目标场景。

下面是几个典型场景例子。

1. 分布式跟踪系统 或 全链路压测（即链路打标）
2. 日志收集记录系统上下文
3. `Session`级 `Cache`
4. 应用容器或上层框架跨应用代码给下层 `SDK`传递信息

各个场景的展开说明参见子文档 [需求场景](docs/requirement-scenario.md)。

# 👥 User Guide

使用类[`TransmittableThreadLocal`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)来保存值，并跨线程池传递。

`TransmittableThreadLocal`继承 `InheritableThreadLocal`，使用方式也类似。相比 `InheritableThreadLocal`，添加了

1. `copy`方法用于定制 **任务提交给线程池时** 的 `ThreadLocal`值传递到 **任务执行时** 的拷贝行为，缺省是简单的赋值传递。
   - 注意：如果传递的是一个对象（引用类型）且没有做深拷贝，如直接传递引用或是浅拷贝，那么
     - 跨线程传递而不再有线程封闭，传递对象在多个线程之间是有共享的；
     - 与 `InheritableThreadLocal.childValue`一样，使用者/业务逻辑要注意传递对象的线程安全。
2. `protected`的 `beforeExecute`/`afterExecute`方法
   执行任务(`Runnable`/`Callable`)的前/后的生命周期回调，缺省是空操作。

<blockquote>
<details>

<summary>关于<code>copy</code>方法 的 展开说明</summary>
<br>

<p>严谨地说，应该是『传递行为』，而不是『拷贝行为』；相应的，这个方法应该命名成<code>transmiteeValue</code>，与<code>InheritableThreadLocal.childValue</code>方法有一致的命名风格。

<p>但多数情况下，传递的是一个复杂的对象，习惯上会先想到的是如何做拷贝，如深拷贝、浅拷贝；命名成<code>copy</code>反而更容易理解这个过程与行为了。 😂

<p>关于构词后缀<code>er</code>与<code>ee</code>的说明：

<ul>
<li><code>transmit</code>是动词传递，<code>transmitter</code>动作的执行者/主动方，而<code>transmitee</code>动作的接收者/被动方。</li>
<li><code>er</code>与<code>ee</code>后缀的常见词是<code>employer</code>（雇主）/<code>employee</code>（雇员）、<code>caller</code>（调用者）/<code>callee</code>（被调用者）。</li>
</ul>

</details>
</blockquote>

具体使用方式见下面的说明。

## 1. 简单使用

父线程给子线程传递值。

示例代码：

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// 在父线程中设置
context.set("value-set-in-parent");

// =====================================================

// 在子线程中可以读取，值是"value-set-in-parent"
String value = context.get();
```

\# 完整可运行的Demo代码参见[`SimpleDemo.kt`](src/test/java/com/alibaba/demo/ttl/SimpleDemo.kt)。

这其实是 `InheritableThreadLocal`的功能，应该使用 `InheritableThreadLocal`来完成。

但对于使用线程池等会池化复用线程的执行组件的情况，线程由线程池创建好，并且线程是池化起来反复使用的；这时父子线程关系的 `ThreadLocal`值传递已经没有意义，应用需要的实际上是把 **任务提交给线程池时**的 `ThreadLocal`值传递到 **任务执行时**。

解决方法参见下面的这几种用法。

## 2. 保证线程池中传递值

### 2.1 修饰 `Runnable`和 `Callable`

使用[`TtlRunnable`](src/main/java/com/alibaba/ttl/TtlRunnable.java)和[`TtlCallable`](src/main/java/com/alibaba/ttl/TtlCallable.java)来修饰传入线程池的 `Runnable`和 `Callable`。

示例代码：

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// 在父线程中设置
context.set("value-set-in-parent");

Runnable task = new RunnableTask();
// 额外的处理，生成修饰了的对象ttlRunnable
Runnable ttlRunnable = TtlRunnable.get(task);
executorService.submit(ttlRunnable);

// =====================================================

// Task中可以读取，值是"value-set-in-parent"
String value = context.get();
```

**_注意_**：
即使是同一个 `Runnable`任务多次提交到线程池时，每次提交时都需要通过修饰操作（即 `TtlRunnable.get(task)`）以抓取这次提交时的 `TransmittableThreadLocal`上下文的值；即如果同一个任务下一次提交时不执行修饰而仍然使用上一次的 `TtlRunnable`，则提交的任务运行时会是之前修饰操作所抓取的上下文。示例代码如下：

```java
// 第一次提交
Runnable task = new RunnableTask();
executorService.submit(TtlRunnable.get(task));

// ...业务逻辑代码，
// 并且修改了 TransmittableThreadLocal上下文 ...
// context.set("value-modified-in-parent");

// 再次提交
// 重新执行修饰，以传递修改了的 TransmittableThreadLocal上下文
executorService.submit(TtlRunnable.get(task));
```

上面演示了 `Runnable`，`Callable`的处理类似

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// 在父线程中设置
context.set("value-set-in-parent");

Callable call = new CallableTask();
// 额外的处理，生成修饰了的对象ttlCallable
Callable ttlCallable = TtlCallable.get(call);
executorService.submit(ttlCallable);

// =====================================================

// Call中可以读取，值是"value-set-in-parent"
String value = context.get();
```

### 2.2 修饰线程池

省去每次 `Runnable`和 `Callable`传入线程池时的修饰，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.ttl.threadpool.TtlExecutors`](src/main/java/com/alibaba/ttl/threadpool/TtlExecutors.java)完成，有下面的方法：

- `getTtlExecutor`：修饰接口 `Executor`
- `getTtlExecutorService`：修饰接口 `ExecutorService`
- `getTtlScheduledExecutorService`：修饰接口 `ScheduledExecutorService`

示例代码：

```java
ExecutorService executorService = ...
// 额外的处理，生成修饰了的对象executorService
executorService = TtlExecutors.getTtlExecutorService(executorService);

TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// 在父线程中设置
context.set("value-set-in-parent");

Runnable task = new RunnableTask();
Callable call = new CallableTask();
executorService.submit(task);
executorService.submit(call);

// =====================================================

// Task或是Call中可以读取，值是"value-set-in-parent"
String value = context.get();
```

\# 完整可运行的Demo代码参见[`TtlExecutorWrapperDemo.kt`](src/test/java/com/alibaba/demo/ttl/TtlExecutorWrapperDemo.kt)。

### 2.3 使用 `Java Agent`来修饰 `JDK`线程池实现类

这种方式，实现线程池的传递是透明的，业务代码中没有修饰 `Runnable`或是线程池的代码。即可以做到应用代码 **无侵入**。
\# 关于 **无侵入** 的更多说明参见文档[`Java Agent`方式对应用代码无侵入](docs/developer-guide.md#java-agent%E6%96%B9%E5%BC%8F%E5%AF%B9%E5%BA%94%E7%94%A8%E4%BB%A3%E7%A0%81%E6%97%A0%E4%BE%B5%E5%85%A5)。

示例代码：

```java
// ## 1. 框架上层逻辑，后续流程框架调用业务 ##
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();
context.set("value-set-in-parent");

// ## 2. 应用逻辑，后续流程业务调用框架下层逻辑 ##
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new RunnableTask();
Callable call = new CallableTask();
executorService.submit(task);
executorService.submit(call);

// ## 3. 框架下层逻辑 ##
// Task或是Call中可以读取，值是"value-set-in-parent"
String value = context.get();
```

Demo参见[`AgentDemo.kt`](src/test/java/com/alibaba/demo/ttl/agent/AgentDemo.kt)。执行工程下的脚本[`scripts/run-agent-demo.sh`](scripts/run-agent-demo.sh)即可运行Demo。

目前 `TTL Agent`中，修饰了的 `JDK`执行器组件（即如线程池）如下：

1. `java.util.concurrent.ThreadPoolExecutor` 和 `java.util.concurrent.ScheduledThreadPoolExecutor`
   - 修饰实现代码在[`JdkExecutorTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/JdkExecutorTtlTransformlet.java)。
2. `java.util.concurrent.ForkJoinTask`（对应的执行器组件是 `java.util.concurrent.ForkJoinPool`）
   - 修饰实现代码在[`ForkJoinTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/ForkJoinTtlTransformlet.java)。从版本 **_`2.5.1`_** 开始支持。
   - **_注意_**：`Java 8`引入的[**_`CompletableFuture`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)与（并行执行的）[**_`Stream`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html)底层是通过 `ForkJoinPool`来执行，所以支持 `ForkJoinPool`后，`TTL`也就透明支持了 `CompletableFuture`与 `Stream`。🎉
3. `java.util.TimerTask`的子类（对应的执行器组件是 `java.util.Timer`）
   - 修饰实现代码在[`TimerTaskTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/TimerTaskTtlTransformlet.java)。从版本 **_`2.7.0`_** 开始支持。
   - **_注意_**：从 `2.11.2`版本开始缺省开启 `TimerTask`的修饰（因为保证正确性是第一位，而不是最佳实践『不推荐使用 `TimerTask`』:）；`2.11.1`版本及其之前的版本没有缺省开启 `TimerTask`的修饰。
   - 使用 `Agent`参数 `ttl.agent.enable.timer.task`开启/关闭 `TimerTask`的修饰：
     - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:true`
     - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:false`
   - 更多关于 `TTL Agent`参数的配置说明详见[`TtlAgent.java`的JavaDoc](src/main/java/com/alibaba/ttl/threadpool/agent/TtlAgent.java)。

<blockquote>
<details>

<summary>关于<code>java.util.TimerTask</code>/<code>java.util.Timer</code> 的 展开说明</summary>
<br>

<p><code>Timer</code>是<code>JDK 1.3</code>的老类，不推荐使用<code>Timer</code>类。

<p>推荐用<a href="https://docs.oracle.com/javase/10/docs/api/java/util/concurrent/ScheduledExecutorService.html" rel="nofollow"><code>ScheduledExecutorService</code></a>。<br>
<code>ScheduledThreadPoolExecutor</code>实现更强壮，并且功能更丰富。
如支持配置线程池的大小（<code>Timer</code>只有一个线程）；<code>Timer</code>在<code>Runnable</code>中抛出异常会中止定时执行。更多说明参见 <a href="https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/#concurrency" rel="nofollow">10. <strong>Mandatory</strong> Run multiple TimeTask by using ScheduledExecutorService rather than Timer because Timer will kill all running threads in case of failing to catch exceptions. - Alibaba Java Coding Guidelines</a>。</p>

</details>
</blockquote>

#### `Java Agent`的启动参数配置

在 `Java`的启动参数加上：`-javaagent:path/to/transmittable-thread-local-2.x.y.jar`。

**_注意_**：

- 如果修改了下载的 `TTL`的 `Jar`的文件名（`transmittable-thread-local-2.x.y.jar`），则需要自己手动通过 `-Xbootclasspath JVM`参数来显式配置。比如修改文件名成 `ttl-foo-name-changed.jar`，则还需要加上 `Java`的启动参数：`-Xbootclasspath/a:path/to/ttl-foo-name-changed.jar`。
- 或使用 `v2.6.0`之前的版本（如 `v2.5.1`），则也需要自己手动通过 `-Xbootclasspath JVM`参数来显式配置（就像 `TTL`之前的版本的做法一样）。
  加上 `Java`的启动参数：`-Xbootclasspath/a:path/to/transmittable-thread-local-2.5.1.jar`。

`Java`命令行示例如下：

```bash
java -javaagent:path/to/transmittable-thread-local-2.x.y.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo

# 如果修改了TTL jar文件名 或 TTL版本是 2.6.0 之前
# 则还需要显式设置 -Xbootclasspath 参数
java -javaagent:path/to/ttl-foo-name-changed.jar \
    -Xbootclasspath/a:path/to/ttl-foo-name-changed.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo

java -javaagent:path/to/transmittable-thread-local-2.5.1.jar \
    -Xbootclasspath/a:path/to/transmittable-thread-local-2.5.1.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo
```

<blockquote>
<details>

<summary>关于<code>boot class path</code> 的 展开说明</summary>
<br>


大概意思就是，在ThreadPoolExecutor类中，如果方法参数含有Runnable或者Callable，会在方法体第一行，加上一段代码

这样就实现了无感知包装Runnable的逻辑。通过agent相当于无侵入引入了ttl，但是ttl的创建这一步还是需要我们手动的，不可能去改写tl或者itl的字节码，**tl，itl，ttl三者在jvm内共存**



<p>因为修饰了<code>JDK</code>标准库的类，标准库由<code>bootstrap class loader</code>加载；修饰后的<code>JDK</code>类引用了<code>TTL</code>的代码，所以<code>Java Agent</code>使用方式下<code>TTL Jar</code>文件需要配置到<code>boot class path</code>上。</p>

<p><code>TTL</code>从<code>v2.6.0</code>开始，加载<code>TTL Agent</code>时会自动设置<code>TTL Jar</code>到<code>boot class path</code>上。<br>
<strong><em>注意</em></strong>：不能修改从<code>Maven</code>库下载的<code>TTL Jar</code>文件名（形如<code>transmittable-thread-local-2.x.y.jar</code>）。
如果修改了，则需要自己手动通过<code>-Xbootclasspath JVM</code>参数来显式配置（就像<code>TTL</code>之前的版本的做法一样）。</p>

<p>自动设置<code>TTL Jar</code>到<code>boot class path</code>的实现是通过指定<code>TTL Java Agent Jar</code>文件里<code>manifest</code>文件（<code>META-INF/MANIFEST.MF</code>）的<code>Boot-Class-Path</code>属性：</p>

<p><code>Boot-Class-Path</code></p>
<p>A list of paths to be searched by the bootstrap class loader. Paths represent directories or libraries (commonly referred to as JAR or zip libraries on many platforms).
These paths are searched by the bootstrap class loader after the platform specific mechanisms of locating a class have failed. Paths are searched in the order listed.</p>

<p>更多详见</p>

<ul>
<li><a href="https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html#package.description" rel="nofollow"><code>Java Agent</code>规范 - <code>JavaDoc</code></a></li>
<li><a href="https://docs.oracle.com/javase/10/docs/specs/jar/jar.html#jar-manifest" rel="nofollow">JAR File Specification - JAR Manifest</a></li>
<li><a href="https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html" rel="nofollow">Working with Manifest Files - The Java™ Tutorials</a></li>
</ul>

</details>
</blockquote>
