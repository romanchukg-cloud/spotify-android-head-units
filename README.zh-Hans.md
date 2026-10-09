# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载测试版 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-byd-unified-test.1) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

已在 Android 35 模拟器上验证安装、界面启动、连接内部服务及打开登录页面。

**此整合版 APK 尚未在车辆上测试。** 音频、登录后的内容、方向盘按键、播放时关闭应用以及离线下载仍需测试。

需要 Android 9 或更高版本。尚未验证其他厂商车机的兼容性；此版本并非适用于所有设备的通用版本。

## 安装方法

1. 从 GitHub Releases 下载 APK，并传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9 及以上 · 54.6 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-BYD-unified-test.apk` · 57 243 570 bytes

当前测试版本的 SHA-256：

```text
0e057caf7f3a7962da0ba73207b16a7f699cc8b3220d198d9102bf60654d3d65
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。
