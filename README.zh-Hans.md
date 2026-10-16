# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载 test.10 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.10/Spotify-5.5.0-HeadUnit-test.10.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

![BYD 车内的新版样式主页（日间主题）](docs/screens/car-home-day.jpg)

## 已完成的测试

Headunit-test.10 新增默认启用的新版样式：圆角卡片、Figtree 字体、日间和夜间主题、封面取色、重新排布并带有可用“喜欢”按钮的播放器、播放队列中的封面和均衡器动画、圆角应用边框，以及全新的主机设置。经典样式保留原有界面。现在关闭 Spotify 总会停止音乐；在媒体库打开时切换日夜模式也不再导致播放器崩溃。由于界面资源有变化，本版本使用完整的参考构建流程构建。发布审计结果 45 PASS / 0 FAIL，152 项更新检查和 39 项登录检查通过，签名证书与 test.9 相同。在车主的车上（BYD DiLink 5.0 / Android 12），test.10 已作为 test.9 的更新安装：登录和音乐均保留，新版样式在日间主题下检查无崩溃。在已登录真实账号的 Android 12 模拟器上，从 test.9 更新后登录保持；已检查日间和夜间、1024×600 和竖屏，以及经典与新版之间的切换。

**headunit-test.10 实验版本。** 尚未在车机上看到通过应用本身从 test.9 更新到 test.10 的过程。新版样式尚未在阿拉伯语（从右到左）和右舵布局下检查，右舵布局的播放器保持经典排布。车辆真实休眠/重启、账号 A → B → A、离线下载、方向盘按键、DiLink 5.1 / Android 13 及其他车机也仍未验证。

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

Android 9+ · 55.5 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.10.apk` · 58 222 521 bytes

当前测试版本的 SHA-256：

```text
ccf440e37acd3208f2b076d1023bfec9d5370c626230b65f12980b73f6fcf806
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.10.md)
