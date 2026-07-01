# CloverUIforAndroid 版本号管理规则

## 1. 总则

本项目（`cn.lemondrop.clover:clover-ui`）使用**语义化版本控制**（[Semantic Versioning 2.0.0](https://semver.org/lang/zh-CN/)），统一在 `gradle.properties` 中维护 Library 版本号，第三方依赖版本统一在 `gradle/libs.versions.toml` 中维护。

## 2. Library 版本号格式

```text
MAJOR.MINOR.PATCH[-prerelease]
```

| 段位 | 含义 | 何时递增 |
|------|------|----------|
| `MAJOR` | 主版本号 | 发生**不兼容的 API 变更**时递增。例如删除公共 API、修改公共函数签名、改变主题/颜色默认值导致消费方 UI 明显变化。 |
| `MINOR` | 次版本号 | 进行**向后兼容的功能新增**时递增。例如新增组件、新增参数、新增主题变体。 |
| `PATCH` | 修订号 | 进行**向后兼容的问题修复**时递增。例如修复崩溃、修复动画异常、修复颜色计算错误。 |
| `prerelease` | 预发布标识 | 开发/测试阶段使用，例如 `-SNAPSHOT`、`-alpha.1`、`-beta.2`、`-rc.1`。正式发布时去掉。 |

### 2.1 0.x 阶段说明

当前 `MAJOR = 0`，表示 API 仍处于快速迭代期，**不承诺二进制兼容**。在 `1.0.0` 发布之前：
- 可以把“不兼容但必要的 API 调整”放到 `MINOR` 版本；
- 进入 `1.0.0` 后必须严格遵守 SemVer，所有破坏性变更都必须提升 `MAJOR`。

## 3. 版本号单一数据源

Library 版本号只允许出现在一个地方：

```properties
# CloverUIforAndroid/gradle.properties
cloverUiVersion=0.1.0-SNAPSHOT
```

所有需要版本号的地方（如 `maven-publish` 的 `version`）都必须从该属性读取，禁止在 `build.gradle.kts` 中再次硬编码。

读取方式示例：

```kotlin
val cloverUiVersion: String = project.findProperty("cloverUiVersion")?.toString()
    ?: error("Missing 'cloverUiVersion' in gradle.properties")

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "cn.lemondrop.clover"
            artifactId = "clover-ui"
            version = cloverUiVersion
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
```

## 4. 第三方依赖版本管理

Android Gradle Plugin、Kotlin、Compose BOM 以及 Haze、Lucide、Fluent 等第三方库的版本统一放在：

```text
CloverUIforAndroid/gradle/libs.versions.toml
```

- 依赖版本**不允许**散落在各个模块的 `build.gradle.kts` 中；
- 每次升级依赖后，应在 `CHANGELOG.md`（或 Git commit message）中记录升级原因和可能的影响；
- 对于 AGP/Kotlin/Compose Compiler 等关键构建工具，升级前应先确认 Gradle Wrapper、JDK、Android Studio 版本是否匹配。

## 5. Git Tag 与发布流程

- 每次发布正式版本时，必须打一个 Git tag：

  ```bash
  git tag -a v0.1.0 -m "Release CloverUI v0.1.0"
  git push origin v0.1.0
  ```

- tag 名称必须与 `cloverUiVersion` 完全一致（前面加 `v`）；
- `-SNAPSHOT` 版本不需要打 tag；
- 发布前检查清单：
  1. `cloverUiVersion` 已去掉 `-SNAPSHOT`；
  2. `consumer-rules.pro` / `proguard-rules.pro` 已更新；
  3. 所有模块可成功 `./gradlew assembleRelease`；
  4. 已更新 `CHANGELOG.md`；
  5. 已打 tag 并推送。

## 6. 分支与预发布版本策略（推荐）

| 分支 | 版本示例 | 说明 |
|------|----------|------|
| `master` / `main` | `0.2.0-SNAPSHOT` | 日常开发，始终为 SNAPSHOT；发布前改为目标版本。 |
| `release/x.y` | `0.2.0-rc.1` | 发版前 stabilizing 分支，可打 `-rc.N`。 |
| tag `v0.2.0` | `0.2.0` | 正式发布，无预发布标识。 |

## 7. 反例

以下写法**禁止**出现在项目中：

```kotlin
// ❌ 硬编码版本号
version = "0.1.0-SNAPSHOT"

// ❌ 在 build.gradle.kts 里手动拼接版本
version = "0.1.0"

// ❌ 依赖版本散落在模块 build.gradle.kts
implementation("androidx.compose:compose-bom:2026.02.01")
```

## 8. 谁负责更新版本号

- 修复 bug 后由提交者提升 `PATCH`；
- 新增功能后由提交者提升 `MINOR`；
- 破坏性变更由提交者在 PR 中明确说明，并提升 `MAJOR`；
- 正式发布由仓库维护者统一提升版本号并打 tag。
