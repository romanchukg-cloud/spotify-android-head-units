# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的 Spotify Automotive 实验版播放器。将基于比亚迪的界面与音乐服务整合到一个 APK 中，使用一个图标和 Spotify 标准登录流程。

[下载测试版 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.3) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

## 已完成的测试

headunit-test.3：APK 静态审计 23 项通过、0 项失败；在普通 Android 9、11、13、15 模拟器上完成 16 项逻辑检查。在 800×480@160、1024×600@160、1280×720@240、1920×720@240 和 768×1024 竖屏上检查未登录界面及设置。独立 MediaBrowserCompat 客户端在允许外部控制时连接根目录，禁用后被拒绝。Android 11 重启会清除关闭状态并发送恢复请求。此前 BYD DiLink5 的测试结果属于 test.2，不能证明新版本已通过实车验证。

**headunit-test.3 实验版本。** 登录后的 Home、媒体库、播放列表、音频，界面关闭或进程死亡后的媒体键，Now Playing 返回导航，休眠或重启后的实际恢复播放，离线下载及 BYD DiLink5 回归测试仍待验证。本版本不包含多账户实验。

需要 Android 9+（API 28）；Android 8 属于后续阶段。已实现 BYD、AAOS、GENERIC 配置，本次仅验证 GENERIC。其他品牌的真实车机及 AAOS 尚未验证。

## 安装方法

1. 从 GitHub Releases 下载 APK，并传输到车载多媒体系统。
2. 如果已安装我们之前使用相同签名的测试版本，请直接更新，无需卸载，登录状态可能保留。签名不同的版本无法通过此方式更新。
3. 打开 Spotify，按需扫码登录。
4. 检查音乐播放和控制功能。确认新 APK 工作正常后，再卸载原来独立的比亚迪界面应用。

Android 9+ · 55.2 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.3.apk` · 57 835 520 bytes

当前测试版本的 SHA-256：

```text
9dc4a3bbd99eabee77ab6b47576f0b431966386c5866e055801d61d78dac059b
```

非官方实验项目。Spotify 和 BYD 商标归各自所有者所有。

## 源代码与验证

仓库提供我们的 Java 集成代码、补丁与构建脚本、审计工具及模拟器测试源码。闭源 Spotify 后端和 BYD 界面作为本地构建输入。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.3.md)
