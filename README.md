# 一念间

打开应用前，先停一下，想清楚这次要做什么。受控应用会先问目的和时长；鼓励应用只在打开和使用中给一句轻鼓励。不评价理由，不做自律审判。

- 本地优先：不联网、不读屏、不上传记录。
- Android 原生应用；包名 `com.intentpause.app`。
- 小绿团子叫“糯糯”；Slogan「这一念，听自己。」

## 下载

最新版（固定链接，始终指向最新 Release）：

**https://github.com/hetao8453-star/yinianjian/releases/latest/download/yinianjian-latest.apk**

指定版本 v8.0：

https://github.com/hetao8453-star/yinianjian/releases/tag/v8.0

## 安装与更新

- v8.0 与 v7.0、v6.0、v5.0 正式包使用同一签名和包名，可以直接覆盖安装；从 debug 包换正式包需要先卸载，数据会清空。
- 这是侧载 APK，不是应用商店版本；安装时 Android 会提示“未知来源”。
- 项目不联网，不会在应用内检查更新；新版本请回到本页或 Releases 下载。

## 文档

- 版本记录：[CHANGELOG_26_10_26.md](CHANGELOG_26_10_26.md)
- 需求确认：[需求确认_v5_26_10_26.md](需求确认_v5_26_10_26.md)
- 真机测试：[真机测试清单_v6_26_10_26.md](真机测试清单_v6_26_10_26.md)
- 发布流程：[发布与更新_26_10_26.md](发布与更新_26_10_26.md)
- Codex 交接：[CODEX_HANDOFF_26_10_26.md](CODEX_HANDOFF_26_10_26.md)

## 构建

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

## 边界

无障碍覆盖层、通知权限、厂商电池/自启动策略在不同 Android 设备上表现不同；真机结果以测试清单为准。项目目前不联网；不要为了“自动更新”擅自添加联网权限。
