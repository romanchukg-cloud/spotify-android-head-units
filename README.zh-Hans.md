# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的实验性 Spotify Automotive 播放器，拥有为汽车设计的自有界面：一个 APK、一个图标和标准的 Spotify 登录。免费——切勿付费。

[下载 test.12 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.12/Spotify-5.5.0-HeadUnit-test.12.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

![BYD 汽车中夜间的首页](docs/screens/car-home-night.jpg)

## 已完成的测试

Headunit-test.12 用自有界面取代了基于 BYD 的界面：首页、音乐库、搜索，歌单、专辑、艺人和播客页面，以模糊封面为背景的全屏播放器和播放队列，下载进度、睡眠定时，以及应用免费的提示。使用完整参考构建制作。发布审核 46 PASS / 0 FAIL，152 项更新器检查和 39 项登录检查通过，签名证书与 test.11 相同。在 Android 9 和 13 模拟器上，test.12 可作为更新安装在 test.11 之上，并在检查更新时不崩溃。预发布版本已在 BYD 汽车（DiLink 5，Android 12）中运行：横屏、竖屏以及与导航分屏、二维码登录、方向盘按键和 Spotify Connect。

**实验版 headunit-test.12。** 发布文件本身尚未安装到汽车中。右舵布局、DiLink 5.1 / Android 13 及其他车机尚未验证；阿拉伯语仅在模拟器上检查过。Spotify 为其 Automotive 客户端关闭了下载；此版本开启了客户端已有的支持，因此 Spotify 可能会改变下载的工作方式。

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

Android 9+ · 55.7 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.12.apk` · 58 378 169 bytes

当前测试版本的 SHA-256：

```text
00f82a80d9a40d9d3138c3fde1d4c3c88edf270c380c54a70fa01032ab6982a1
```

非官方实验项目。此应用免费——如果您为此付费，请要求退款。Spotify 和 BYD 是其各自所有者的商标。

## 源代码与验证

我们的 Java 代码——界面、集成、补丁和构建脚本、审核及模拟器测试——均在仓库中。专有的 Spotify 后端是本地构建输入。APK 仍包含 BYD（Vivid）播放器的部分内容：用于汽车语音助手的媒体服务、资源和一种字体；之后会移除。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.12.md) · [NOTICE](NOTICE.md)
