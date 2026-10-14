# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载 test.7 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.7/Spotify-5.5.0-HeadUnit-test.7.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

Headunit-test.7: APK audit 39 PASS / 0 FAIL. Android 9, 12 and 13 emulators each passed 94 diagnostic logic checks, 7 Support/notice UI checks, 8 process-restart persistence checks, 18 synthetic rhythm checks and 19 playback-resume checks. Diagnostics delivery was tested with a fake transport; no production Spotify report was submitted.

Experimental headunit-test.7. This version has not been installed on the car. The earlier test.6 lighting integration was verified on a BYD Tang EV with DiLink 5 / Android 12. Physical DiLink 5.1 / Android 13 lighting, real vehicle sleep/reboot, real account switching and other head units remain unverified.

Android 9+ (API 28). BYD, AAOS and GENERIC profiles are implemented; compatibility depends on vehicle firmware.

## Support and diagnostics

Open Head unit settings → Support. Send diagnostics lets you preview approved technical data before sending. Reports go to our shared server and a limited summary may appear in this project’s PUBLIC GitHub Issues. Automatically send diagnostics is selected on the first-use notice and starts only after Continue; you can turn it off in Support. Automatic reports cover measured integration failures only, at most one new report per day. Manual sending, pending status/cancellation and ZIP export remain available with automatic sending off. Reports exclude account data, song/playlist names, location and screenshots. Technical metadata is not a guarantee of anonymity.

## 安装方法

1. 点击上方下载按钮获取 APK，然后将其传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 要使用比亚迪氛围灯，请在车辆自身的氛围灯设置中开启“音乐律动”。此开关位于车辆设置中，而不是 Spotify 中。
5. 添加其他驾驶员：主机设置 → 账号 → 添加账号 · QR。切换时会停止音乐并重启播放器。
6. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9+ · 55.3 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.7.apk` · 57 946 112 bytes

当前测试版本的 SHA-256：

```text
9b127d9e9f9b05dfa210207370e9f21dd9ca5eb61ec6e4d6c6bb748d069f4da7
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.7.md)
