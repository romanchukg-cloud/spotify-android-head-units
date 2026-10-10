# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载测试版 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.4) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

Headunit-test.4：APK 审计 26 PASS / 0 FAIL。Android 9 和 15 模拟器测试涵盖主机策略、五种尺寸下的未登录界面和设置、加密存储、错误回滚、重启恢复以及两个合成账号的切换。这些测试不代表已验证两个真实 Spotify 账号的服务器认证。

**headunit-test.4 实验版本。** 登录后的浏览和音频、真实账号 A → B → A 切换、离线下载、方向盘控制、休眠或重启后的实际音频恢复，以及 BYD/AAOS 硬件回归仍待验证。

需要 Android 9+（API 28）；Android 8 属于后续阶段。已实现 BYD、AAOS、GENERIC 配置，本次仅验证 GENERIC。其他品牌的真实车机及 AAOS 尚未验证。

## 安装方法

1. 从 GitHub Releases 下载 APK，并传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 添加其他驾驶员：主机设置 → 账号 → 添加账号 · QR。切换时会停止音乐并重启播放器。
5. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9+ · 55.2 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.4.apk` · 57 868 288 bytes

当前测试版本的 SHA-256：

```text
534a3d0000ab34c87523509e183e326218d83b01c496da2cb4285c942a09edc4
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.4.md)
