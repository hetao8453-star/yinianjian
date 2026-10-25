# 进度日志

## 会话：2026-10-25

### 阶段 0：工作区与基线
- **状态：** complete
- 执行的操作：
  - 复制 `handoff/.../project/intent-android` 到 `intent-android/`，交接包原样保留
  - `git init` + 初始提交（本地，无远程）
  - 写入 `local.properties`（`sdk.dir=/Users/mac/Library/Android/sdk`）
  - 基线构建：首次 `assembleDebug` 成功；第二次构建意外与编辑并发导致编译错误，已重新构建通过
- 创建/修改的文件：`local.properties`、`task_plan.md`、`findings.md`、`progress.md`

### 阶段 1：数据与基础设施
- **状态：** complete
- 执行的操作：
  - 新增 `Ui_26_10_25`、`Apps_26_10_25`、`StatsMath_26_10_25`
  - 新增 `IntentDb_26_10_25`（sessions/events/notes/service_log）
  - 新增 `SessionState_26_10_25`（活动会话与页面恢复）
  - 新增 `Perms_26_10_25`、`Notify_26_10_25`、`BootReceiver_26_10_25`
- 创建/修改的文件：上述 Java 文件、`strings.xml`、`themes.xml`、图标资源

### 阶段 2：确认覆盖层与无障碍服务
- **状态：** complete
- 执行的操作：
  - 重写 `IntentAccessibilityService_26_10_25`：两页流程、应急 3 分钟、还没想清楚、复查、1 分钟宽限、息屏/亮屏、服务重启恢复
  - 输入法包名动态过滤（`InputMethodManager.getEnabledInputMethodList()` + 兜底关键字）
  - 会话事件落库与 service_log
  - 新增 `PromptOverlay_26_10_25`：单窗口换页、150ms 过渡、IME 目标窗口、键盘避让
- 创建/修改的文件：`IntentAccessibilityService_26_10_25.java`、`PromptOverlay_26_10_25.java`

### 阶段 3：主界面与应用选择
- **状态：** complete
- 执行的操作：
  - `MainActivity` 改为底部 3 Tab；新增 `HomeTab_26_10_25`、`RecordsTab_26_10_25`、`MineTab_26_10_25`
  - 新增 `AppPickerActivity_26_10_25`（搜索、已选优先、分批加载）
  - 新增 `AppRecordActivity_26_10_25`（按应用 → 每次会话）
- 创建/修改的文件：上述 Java 文件、`Activity` 主题与 drawable

### 阶段 4：权限、通知、开机
- **状态：** complete
- 执行的操作：
  - Manifest 增补 `POST_NOTIFICATIONS`、`RECEIVE_BOOT_COMPLETED`、`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
  - 新增 `PermissionGuideActivity_26_10_25`：逐项解释、iQOO 自启动步骤、“为什么可能每次都要重开”
  - 开机检查通知、兜底通知、电池优化入口
- 创建/修改的文件：`AndroidManifest.xml`、`PermissionGuideActivity_26_10_25.java`、`Notify_26_10_25.java`、`BootReceiver_26_10_25.java`

### 阶段 5：v2 构建与模拟器验证
- **状态：** complete
- 执行的操作与结果：
  - `assembleDebug` 成功；`testDebugUnitTest` 7 个测试通过；`lintDebug` 通过
  - 产出 `deliverables/慢一拍_v2_26_10_25.apk`，SHA-256 `38d397cae36f2e76addbfeb19ba6663f32dbd28efeb0cf91cb3bd6693315478b`
  - 在 Android 16 模拟器（DSH_Pixel，API 36）安装并验证：
    - 首页渲染、状态卡、已控制应用、开关样式
    - 应用选择页搜索框、勾选 Chrome、完成返回
    - 打开 Chrome 出现第 1 页；选“查找内容”进入第 2 页
    - 点文字框弹出键盘、成功输入 `test123`
    - 选 5 分钟进入 Chrome；5 分钟后出现复查页，显示“查找内容：test123”“本次已过去 5 分钟”
    - 复查页“现在离开”回桌面；数据库记录 status=left、saved_est=5 分钟、foreground≈5.5 分钟
    - 记录页热力图、当日统计、分应用明细；我的页运行状态、记录入口、权限说明
  - 模拟器发现并修复：
    - 底部导航文字未居中（TextView 默认宽度撑满）
    - “打开次数”按最终 status 统计导致离开后为 0（改为 `allowed_wall>0`）
    - 服务被 uiautomator 重启后覆盖层回到第 1 页（持久化 page，恢复第 2/3 页）
    - Switch 默认紫色（改为低饱和绿）
- **状态：** complete

### 阶段 6：v3（2026-10-26）
- **状态：** complete（图标仍待用户生成）
- 本轮根据 iQOO 12 反馈完成：
  - 确认页铺满整屏，覆盖状态栏/挖孔安全区
  - 时长改为 3/10/30 + 自定义 1–180；每个应用可设默认时长
  - 计时器不再因短暂切换被取消；Home 5 秒返回后 3 分钟复查仍触发（模拟器验证）
  - 广告/过渡页 300ms 延迟隐藏，减少抢界面
  - 取消监测二次确认；首页点行改为默认时长设置
  - 启动自动跳无障碍设置；增加安全边界与强制停止说明
  - 记录页移除重复的“最近会话”；自绘开关
  - 产出 `deliverables/慢一拍_v3_26_10_26.apk`，SHA-256 `95bcea31c590db00cff421ab3efeb55a1ab875a01d929bc77e1c3581ce399182`
- 模拟器验证：状态栏覆盖、3/10/30/自定义、默认 3 高亮、3 分钟计时+短暂 Home 后复查、取消确认弹窗、自动跳设置。
- 仍未真机验证：iQOO 输入法、游戏广告抢界面、横屏/分屏/锁屏、后台清理后的恢复。
- 剩余：用户 ChatGPT 图标接入；UX 走查中的待决项。

## 测试结果
| 测试 | 输入 | 预期结果 | 实际结果 | 状态 |
|------|------|---------|---------|------|
| 构建 | `assembleDebug` | 成功 | 成功 | 通过 |
| 单测 | `testDebugUnitTest` | 全部通过 | 7/7 通过 | 通过 |
| Lint | `lintDebug` | 无 error | 通过 | 通过 |
| 首页 | 模拟器启动 | 渲染正常 | 正常 | 通过（模拟器） |
| 输入法 | 第 2 页点输入框 | 弹键盘可输入 | 键盘弹出，输入 test123 | 通过（模拟器） |
| 计时复查 | 5 分钟 | 到点显示复查 | 到点显示，目的/备注保留 | 通过（模拟器） |
| 离开 | 复查页离开 | 回桌面并记录 | 回桌面，status=left，saved=5 分钟 | 通过（模拟器） |
| v3 状态栏 | 确认页打开 | 覆盖状态栏区域 | 模拟器覆盖，状态栏图标隐藏 | 通过（模拟器） |
| v3 时长 | 3/10/30/自定义 | 按所选计算 | 3 分钟会话到点复查 | 通过（模拟器） |
| v3 短暂切换 | Home 5 秒后返回 | 仍按原截止复查 | 到 3 分钟出现复查 | 通过（模拟器） |
| v3 默认时长 | 应用设为 3 | 第 2 页默认 3 | 默认 3 高亮 | 通过（模拟器） |
| iQOO 真机 | 用户测试 | 待测 | 未测 | 待用户 |

## 错误日志
| 时间戳 | 错误 | 尝试次数 | 解决方案 |
|--------|------|---------|---------|
| 23:43 | 构建与文件编辑并发，编译引用了尚未创建的类 | 1 | 等文件写完重新构建 |
| 23:43 | lint：GestureBackNavigation、forceDarkAllowed NewApi | 1 | 改用 OnBackPressedDispatcher；移除 forceDarkAllowed |
| 23:52 | 记录页“打开次数”为 0 | 1 | 查询改为 `allowed_wall>0` |
| 23:53 | 底部导航文字左对齐 | 1 | TextView 设 `Gravity.CENTER` |
| 23:53 | 服务重启后回到第 1 页 | 1 | SessionState 持久化 page，恢复对应页面 |

## 五问重启检查
| 问题 | 答案 |
|------|------|
| 我在哪里？ | 阶段 5 完成，v2 APK 已产出 |
| 我要去哪里？ | 用户用 ChatGPT 生成图标后接入；按 iQOO 真机反馈修复 |
| 目标是什么？ | 按批准计划完成改版并交付可安装验证包 |
| 我学到了什么？ | 见 findings.md |
| 我做了什么？ | 见上方记录 |
