# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载测试版 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-unified-test.2) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

已在搭载 Android 12 的 BYD DiLink5 上测试：更新后保留登录状态，Home、Recents、Library 和播放列表曲目正常加载。打开播放列表时音乐继续播放，选择曲目后可开始播放。用户确认关闭应用会停止音乐。二维码登录也已在 Android 35 模拟器上测试。

**实验性测试版本 .2。** 方向盘按键、离线下载及其他车机仍需测试。

需要 Android 9 或更高版本。尚未验证其他厂商车机的兼容性；此版本并非适用于所有设备的通用版本。

## 安装方法

1. 从 GitHub Releases 下载 APK，并传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9 及以上 · 54.6 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-BYD-unified-test.apk` · 57 247 666 bytes

当前测试版本的 SHA-256：

```text
0fa0b0d9eb5b9b95c989b59f751c838d0b318d9b481ff788572f2d9e5635a124
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。
