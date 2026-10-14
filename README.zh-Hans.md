# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载 test.8 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

Headunit-test.8 保持 Spotify 登录状态：固定设备 ID；在 Android 设置中移除旧的非活动账号不再导致退出；车机上丢失的登录信息会自动恢复一次。基于已发布的 test.7 构建：仅 Spotify DEX、适配器 DEX 和 versionCode 有变化；资源、比亚迪界面和原生库逐字节相同。8 个新补丁均各匹配一次，39 项登录逻辑检查和兼容性审计通过，发布审计结果 42 PASS / 0 FAIL，签名证书与 test.7 相同。

**headunit-test.8 实验版本。** 登录相关改动尚未在模拟器或车内使用真实账号测试；实际的 ANDROID_ID 变化和断电后恢复均未验证。车辆真实休眠/重启、账号 A → B → A、离线下载、方向盘按键及其他车机也仍未验证。test.7 的模拟器测试未针对 test.8 重新运行。

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

Android 9+ · 55.3 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.8.apk` · 57 962 496 bytes

当前测试版本的 SHA-256：

```text
caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.8.md)
