# 一念间 · Codex 接手说明（2026-10-26）

> 给 Codex / 其他协作者：先读完本文件，再读 `需求确认_v5_26_10_26.md` 和 `真机测试清单_v5_26_10_26.md`。
> 不要从第一版重新设计；当前代码已经是可安装的 v5，主要是修 bug 和真机验证。
> 不要改包名、应用名、Slogan、视觉方向、签名密钥；不要擅自公开发布。

## 1. 项目位置与状态

- 本地工程：`/Users/mac/Documents/app_attention/intent-android`
- 公开仓库：https://github.com/hetao8453-star/yinianjian
- 当前分支：`main`，最新提交 `b31712b`
- 包名：`com.intentpause.app`
- 版本：versionCode 5 / versionName "5.0"
- 目标：Android 16（compileSdk/targetSdk 36，minSdk 24），真机 iQOO 12；文案面向所有 Android 用户。
- 技术：Android 原生 Java + View，**没有 Compose**；无障碍覆盖层提供确认页；前台服务；SQLite；SharedPreferences；不联网。
- 最新 Release：https://github.com/hetao8453-star/yinianjian/releases/tag/v5.0
- 最新 APK：https://github.com/hetao8453-star/yinianjian/releases/latest/download/yinianjian_v5.0.apk
- 最新 SHA-256：`b043ad017065bd8c207115fa31a91988cf022b116d4929a023f3e358726349cf`
- CI：`.github/workflows/android-release.yml`；推 `v*` tag 自动构建、签名、发 Release；最新 Run #5 成功、0 警告。

## 2. 产品是什么

在打开“受控应用”前停一下，让用户选目的并选时长；到时如果还在用，就复查。新增“鼓励应用”：打开时和使用中给轻鼓励，不限制。核心是“照看”，不是自律审判；不评价理由，不要求把休息说成正当。

两类应用完全分开，一个应用只能属于一种：

- **受控应用**：确认页 → 目的（查找内容/回复消息/放松一下/还没想清楚）→ 时长（3/10/30/自定义 1–180）→ 到点复查（继续/换目的/离开）。
- **鼓励应用**：打开时顶部轻提示；使用中按间隔轻提示；文案预设 + 自定义；不限制。
- **敏感应用**：优先于两类，不弹、不鼓励、不记录。

首页、记录、我的三个 Tab。Slogan：**陪你停一下，也陪你做下去。**

## 3. 关键文件

```text
MainActivity.java                      三 Tab 容器；支持 start_tab 调试参数
HomeTab_26_10_25.java                  首页：状态 + 受控应用 + 鼓励应用 + 鼓励设置
RecordsTab_26_10_26.java               v5 记录页：花园/日周月/柱图/6 数字/明细/感悟
GardenView_26_10_26.java               小缓花园 Canvas
PeriodBarsView_26_10_26.java           主动停柱图
MineTab_26_10_25.java                  我的：状态折叠/权限/今日总结/暂停照看/记录
PromptOverlay_26_10_25.java            受控应用确认页、复查页
IntentAccessibilityService_26_10_25.java  核心服务：前后台、计时、鼓励会话、覆盖层
EncourageBanner_26_10_26.java          鼓励顶部轻提示
AlternativeOffer_26_10_26.java         退出后的可选替代小入口
DailySummary_26_10_26.java             今日总结、通知、近似调度
DailySummaryReceiver_26_10_26.java     每日总结闹钟接收
CarePause_26_10_26.java                “今天先暂停照看”
AppPickerActivity_26_10_25.java        选应用：普通/敏感/鼓励三种 mode
Apps_26_10_25.java                     应用选择、敏感、鼓励设置、文案、默认时长
IntentDb_26_10_25.java                 SQLite：sessions/events/notes/service_log/encourage_sessions
Perms_26_10_25.java                    无障碍/通知/电池/自启动检查与跳转
Ui_26_10_25.java                       B 视觉 token、通用 UI 工具、减弱动画
KeepAliveService_26_10_26.java         前台服务/常驻通知
IntroActivity_26_10_26.java            首次介绍页
BootReceiver_26_10_25.java             开机清理与恢复
```

旧文件 `RecordsTab_26_10_25.java`、`HeatmapView_26_10_25.java`、`WeekBarsView_26_10_25.java`、`RingChart_26_10_25.java` 仍存在但已不接入；**不要误改旧文件**，记录页以 `RecordsTab_26_10_26` 为准。

## 4. 当前已实现（v5）

### 受控应用
- 打开前确认页；第 1 页显示“今天第 N 次打开”。
- 目的、时长、自定义输入；复查页可继续/换目的/离开。
- 每应用默认时长；默认 10 分钟；3/10/30/自定义。
- 敏感应用跳过；切换广告/过渡页有 300ms 延迟隐藏；离开 ≤1 分钟宽限。
- 替代小入口：选“先不打开/现在离开”后出现可选卡：一个鼓励应用 + “喝口水，走两步” + “回桌面”，6 秒自动收起。

### 鼓励应用
- 首页独立区；`mode=encourage` 选择；与受控/敏感互斥。
- 打开时小缓 + 一句鼓励，顶部非触摸轻提示，2.5 秒消失。
- 使用中每应用间隔（默认 30，10–120）；打开时/使用中两个开关；同一时刻最多一条；短暂切走 ≤60 秒合并。
- 预设 10 句 + 每应用自定义多句；随机/顺序；可关闭预设。
- 记录打开次数、提示次数、使用时长；`encourage_sessions` 表。

### 记录页
- 小缓花园：受控尖叶 + 鼓励圆叶，最多画 24 片，显示总叶数；可收起。
- 日/周/月；日期/范围前后翻；主动停柱图（日按小时、周/月按天）；点周/月柱切到对应日。
- 6 个数字：受控（打开/提醒/使用时长）+ 鼓励（打开/鼓励提示/使用时长）。
- 受控/鼓励分应用明细；日视图感悟；柔性照看天数。
- 热力图按确认**v5 延后**；独立趋势已移除。

### 今日总结
- 首次启动询问时间；我的页可改。
- 近似 AlarmManager 调度；到点通知；通知关闭时下次打开补看。
- 6 项 + 先不打开次数 + 温和句 + 前 7 天同一截止时刻平均（不足 3 天不显示）。
- 通知不含应用名/数字。

### 其他
- 我的运行状态默认折叠成摘要；自启动返回后询问一次、显示“已允许（手动确认）”。
- 权限说明“只申请这些/不申请这些”，无英文常量、无 iQOO/OriginOS。
- 电池设置三层兜底；常驻通知“一念间正在运行 / 小缓在，陪你照看自己。”。
- 隐私：不读窗口类名；敏感包名不写入快照；标记敏感时询问删除历史；备份排除数据库。
- B 视觉：暖奶油底 #F3F1E7、卡片 #FBF9F1、主字 #2A312A、主绿 #4F7A5E、鼠尾草 #A9C3B4、暖沙 #DCC9A8；小缓呼吸、叶子/柱图轻动画；尊重系统减弱动画。

## 5. 刚修复、必须保留

1. **记录页闪退**：`RecordsTab_26_10_26.renderDetails` 曾对同一行重复 `addView`，已改为 `detailRow()` 内部只加一次。不要再加第二次。
2. **上滑回桌面**：服务检测到 Launcher/Home 前台时 `finishSession("home",...)` 并隐藏覆盖层，防止上滑后又被盖回来。**未在 iQOO 真机验证**；如果仍被盖回，再评估透明 Activity，必须先问用户，不要直接重构。
3. **无障碍检查**：`Perms.accessibilityEnabled` 同时兼容 `pkg/.Class` 和 `pkg/full.Class`；不要改回只 `contains(flattenToString())`。
4. **发布包**：`release.keystore` 在项目根目录，密码在 `keystore.properties`（都已 gitignore）。**不要重新生成密钥、不要提交、不要 force push 覆盖签名。**

## 6. 尚未验证 / 待办（按优先级）

1. **iQOO 12 真机**：
   - 点“记录”不闪退；有数据时花园/柱图/6 数字正常；
   - 确认页底部上滑回桌面后，不再自己盖回来；
   - 鼓励应用打开 banner、间隔提示、两个开关；
   - 今日总结到点通知、通知关闭补看、重启/改时区。
2. **键盘/输入**：自定义 1–180 提示已缩短为“自定义（1–180）”；键盘遮挡和滚动未真机复测。
3. **敏感应用**：不弹/不记录、标记时删除历史，未真机走查。
4. **记录页**：底部导航/点击柱状图在模拟器上不稳定；需要真机确认交互。
5. **旧问题**：广告/游戏抢界面、强停后无障碍恢复、横屏/分屏未回归。
6. **热力图/年度回顾**：按确认延后，不要擅自加。
7. **联网功能（远程配置/应用内更新）**：用户 2026-10-26 明确“先算了”，**不要加 INTERNET 或更新权限**；当前保持完全不联网。

## 7. 构建与发布

```bash
cd /Users/mac/Documents/app_attention/intent-android
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

- 当前 `assembleDebug`、`lintDebug` 通过；最新 CI Run #5 成功、0 annotations。
- debug APK：`app/build/outputs/apk/debug/app-debug.apk`
- release APK：`app/build/outputs/apk/release/app-release.apk`（使用本地 keystore 签名）
- 发布流程：`发布与更新_26_10_26.md`；推 `v*` tag 触发 GitHub Actions。
- 不要为了“省一次 APK”加联网/远程配置；用户已明确暂不需要。

## 8. 测试环境

模拟器启用无障碍：

```bash
adb shell settings put secure enabled_accessibility_services \
  com.intentpause.app/com.intentpause.app.IntentAccessibilityService_26_10_25
adb shell settings put secure accessibility_enabled 1
```

- 调试直达记录页（MainActivity 不导出，需 root 或 adb root）：
  `adb shell am start -n com.intentpause.app/.MainActivity --ei start_tab 1`
- 端口/路径/设备以实际为准；不要把模拟器调试参数写死到正式流程。

## 9. 协作边界

- 不要改包名、应用名、Slogan、图标、B 视觉方向，除非用户明确要求。
- 不要新增依赖/Compose/WebView；继续用原生 View + Canvas。
- 不要读取窗口内容、聊天、通知、剪贴板；隐私承诺必须保持。
- 不要自行发布 Release、改签名、删 Release、改 GitHub Secrets。
- 不要用“已验证”描述未真机测试的内容；测试清单里没测就写“未测”。
- 大改前先看 `需求确认_v5_26_10_26.md`；冲突时以用户最新明确指令为准。
