# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载 test.11 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.11/Spotify-5.5.0-HeadUnit-test.11.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

![BYD 车内的新版样式主页（日间主题）](docs/screens/car-home-day.jpg)

## 已完成的测试

Headunit-test.11 新增离线下载（实验功能，需 Spotify Premium）、车机支持语音识别时的语音搜索、带歌曲和艺人电台的播放队列编辑器，以及为乘客提供二维码的 Jam。同时修复了从队列移除或向队列添加曲目时音乐进程崩溃的问题。由于界面有变化，本版本使用完整的参考构建流程构建。发布审计结果 45 PASS / 0 FAIL，152 项更新检查和 39 项登录检查通过，签名证书与 test.10 相同。在 Android 9 和 13 模拟器上，test.11 可作为 test.10 的更新安装并检查更新，无崩溃。在登录 Premium 账号的 Android 12 模拟器上，下载内容可离线播放（包括冷启动后），语音搜索在测试识别器下正常，队列编辑、电台和 Jam 均正常。

**headunit-test.11 实验版本。** test.11 尚未在车内检查，也不确定 BYD DiLink 是否提供可供麦克风按钮使用的语音识别。Spotify 对其车载客户端关闭了下载功能；本版本开启了客户端已有的支持，因此 Spotify 可能改变下载的行为。新版样式的阿拉伯语（从右到左）、右舵布局、DiLink 5.1 / Android 13 及其他车机仍未验证。

Android 9+（API 28）。比亚迪律动已在 DiLink 5 / Android 12 上验证；DiLink 5.1 / Android 13 和其他固件尚未验证。已实现 AAOS 和 GENERIC 配置；其他品牌真实车机及 AAOS 尚未验证。

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## 安装方法

1. 点击上方下载按钮获取 APK，然后将其传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 要使用比亚迪氛围灯，请在车辆自身的氛围灯设置中开启“音乐律动”。此开关位于车辆设置中，而不是 Spotify 中。
5. 添加其他驾驶员：主机设置 → 账号 → 添加账号 · QR。切换时会停止音乐并重启播放器。
6. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9+ · 55.6 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.11.apk` · 58 251 193 bytes

当前测试版本的 SHA-256：

```text
6e1cf2566026f08859861fc63ab1a6a53eba8c84d1329c29985708e4e7030fe7
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.11.md)
