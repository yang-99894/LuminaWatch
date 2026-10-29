# LuminaWatch · 小云

独立的 Kotlin / Jetpack Compose Android 原生应用。当前完成了前端页面与本机持久化，尚未接入蓝牙配对、传感器、云岸接口或真实消息推送。Debug 构建提供心率和血氧预览数据；Release 构建在未连接手表时不显示这两项读数。保存设备 ID 不等于完成配对。

## 运行

用 Android Studio 打开本目录，同步 Gradle 后运行 `app`。也可运行：

```bat
gradlew.bat assembleDebug
```

本机默认 JDK 为 25，AGP 8.12 无法解析，因此 `gradle.properties` 固定使用 DevEco Studio 自带的 JDK 21：

```properties
org.gradle.java.home=D:/Devecostudio/DevEco Studio/jbr
```

换机器时请改成可用的 JDK 17–23 路径。Android SDK 由 `local.properties` 的 `sdk.dir` 指定。

## 页面

底部是**一条可拖动的浅色液态玻璃轨道**，三个等宽锚点：

| 顺序 | 一级入口 | 内容 |
|---|---|---|
| 1 | 我的手表 | 设备状态、横向表盘预览、闹钟/同步/形象/隐私及健康陪伴快捷入口 |
| 2 | 表盘图库 | 纯白、珊瑚、深色三款手机端预览主题，选择结果在本机持久化 |
| 3 | 发现 | 健康与心情、小云对话与记忆、关怀及今日总览的分组入口 |

原有功能均作为二级页保留：今日/健康/小云/关怀/设备原页面，以及心率/血氧/步数/站立/活动消耗详情、心情记录/月历/详情、关怀详情与规则、闹钟列表/编辑、绑定、形象表盘、设置、会话历史、记忆与聊天风格。

## 当前接入边界

- Debug 构建的心率与血氧预览值是固定数据（78 次/分、98%）；Release 构建在未接入手表时显示无数据。接入真实手表前不得将预览值用于医疗判断。
- 步数、站立与活动消耗图表目前也是前端预览数据，尚未从手表读取；上线前须切换到真实数据源或无数据状态。
- 保存设备 ID 只在本机保留信息；连接、电量和同步不伪装为成功状态。
- 闹钟只保存在本机，没有申请系统闹钟权限，也不会在手表响起。
- 小云回复由本地关键词规则生成，未调用任何模型服务。
- 语音入口为禁用态，并说明原因。
- 家人共享只保存本机偏好，不发送任何数据。

## 数据与持久化

`AppStore` 使用 SharedPreferences + JSON 保存闹钟、心情、关怀事件、对话、设备绑定和偏好，重启后仍在。没有 Room、DataStore、账号或云同步。

## 代码结构

```
app/src/main/java/com/pointerwander/luminawatch/
├─ MainActivity.kt          入口，边到边主题
├─ app/                     应用外壳、一级/二级路由、欢迎页
├─ data/                    模型、AppStore（本地状态与持久化）
├─ ui/                      Token、通用组件、Backdrop 玻璃底栏/分段滑块
├─ feature/today|health|chat|care|device|mood/   各功能页面
```

底栏使用 kyant `backdrop`（1.0.2）的 `drawBackdrop` + `layerBackdrop` 采样页面内容；日/周/月分段滑块支持拖动。承载文字和数据的卡片保持实体表面，玻璃仅用于交互层。原 `NativeLiquidGlass` 截图采样实现已删除。

## 已知限制（后续阶段）

- Backdrop 使用 1.0.2：2.0.0 的 AAR 要求 `minCompileSdk=37` 且由 Kotlin 2.3.21 编译，而本工程为 compileSdk 36（Kotlin 已升到 2.2.21）。升级到 2.0.0 需要把 AGP/compileSdk/Kotlin 一起抬到 37/2.3，属于独立的一次工具链迁移。
- 未使用 Room / DataStore：以 SharedPreferences + JSON 满足本轮持久化验收。
- 情绪云为自绘 Path（无 `graphics-shapes` 依赖），月历格子为静态着色，不做逐格 Morph。
- 心情记录暂不支持编辑（可删除）；闹钟不支持自然语言结构化确认卡。
- 仅在 Android 16 / API 36 模拟器上验证；minSdk 26、API 31/33 真机矩阵尚未验证。
- 真实手表协议、唯一 ID 校验、云岸对话与同步留待 B 阶段。
