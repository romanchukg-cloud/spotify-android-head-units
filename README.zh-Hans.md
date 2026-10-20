# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的实验性 Spotify Automotive 播放器，拥有为汽车设计的自有界面：一个 APK、一个图标和标准的 Spotify 登录。免费——切勿付费。

[下载 test.13 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.13/Spotify-5.5.0-HeadUnit-test.13.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

![BYD 汽车中夜间的首页](docs/screens/car-home-night.jpg)

## 已完成的测试

headunit-test.13 从 APK 中移除了 BYD (Vivid) 播放器的最后部分，新增滑动切换曲目、右舵支持（在 BYD 上自动识别）和延伸到屏幕边缘的界面，竖屏分屏时保留大封面，并新增 Spotify 小部件，其他应用可在其面板中显示它（首个支持的将是 Google Maps for BYD）。用其他密钥重新签名的副本现在会提示已被修改。采用完整的参考构建。发布审核：48 PASS / 0 FAIL；152 项更新、39 项登录和 14 项面板访问检查均通过；签名证书与 test.12 相同。在 Android 9 和 13 模拟器上，test.13 可作为更新安装在 test.12 之上，并在检查更新时无崩溃。test.13 的构建已在 BYD 唐（DiLink 5，Android 12）上运行：滑动、方向盘按键、BYD 语音按钮、氛围灯律动、车辆报告左舵，以及与 Google Maps 分屏；车上安装的正是这个文件。

**实验版 headunit-test.13。** 右舵尚未在右舵车辆上验证（数值来自 BYD 的代码）；DiLink 5.1 / Android 13 及其他车机尚未验证；阿拉伯语仅在模拟器上检查。小部件仅在支持它的应用中显示；Google Maps for BYD 的支持正在准备中。Spotify 为其 Automotive 客户端关闭了下载功能；此版本开启了客户端支持，因此 Spotify 可能会改变下载行为。

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

Android 9+ · 46.0 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.13.apk` · 48 279 511 bytes

当前测试版本的 SHA-256：

```text
d7469ce9383defdc9455e40b7fcbabb101ca7f1d55cfd37ddb8b4d684de3433c
```

非官方实验项目。此应用免费——如果您为此付费，请要求退款。Spotify 和 BYD 是其各自所有者的商标。

## 源代码与验证

我们的 Java 代码——界面、集成、补丁和构建脚本、审核及模拟器测试——都在仓库中，遵循 NOTICE.md 的条款（免费、不得出售、保留免费声明）。专有的 Spotify 后端是本地构建输入。自 test.13 起，APK 不再包含 BYD (Vivid) 播放器的任何部分。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.13.md) · [NOTICE](NOTICE.md)
