# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载 test.6 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.6/Spotify-5.5.0-HeadUnit-test.6.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

Headunit-test.6：APK 审核 31 项通过，0 项失败。Android 9 和 15 模拟器分别通过 18 项合成律动测试和 19 项播放恢复测试。在配备 DiLink 5 / Android 12 的 2024 款比亚迪唐 EV 上，覆盖更新保留了登录状态；音乐启动原生律动，暂停停止律动，进程结束释放会话。用户已确认灯光实际响应。

**headunit-test.6 实验版本。** 实际车辆休眠或重启后的恢复、长时间使用、真实账号 A → B → A 切换、离线下载、方向盘控制和其他 BYD/AAOS 车机仍待验证。test.6 未重新运行完整账号和界面测试。

Android 9+（API 28）。比亚迪律动已在 DiLink 5 / Android 12 上验证；DiLink 5.1 / Android 13 和其他固件尚未验证。已实现 AAOS 和 GENERIC 配置；其他品牌真实车机及 AAOS 尚未验证。

## 安装方法

1. 点击上方下载按钮获取 APK，然后将其传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 要使用比亚迪氛围灯，请在车辆自身的氛围灯设置中开启“音乐律动”。此开关位于车辆设置中，而不是 Spotify 中。
5. 添加其他驾驶员：主机设置 → 账号 → 添加账号 · QR。切换时会停止音乐并重启播放器。
6. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9+ · 55.2 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.6.apk` · 57 880 576 bytes

当前测试版本的 SHA-256：

```text
f5eac6dcb254d095bc9bf26a7aabaf111ea6af71079fcccd79b4f3463cf4c086
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.6.md)
