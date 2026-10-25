# 适用于 Android 车机的 Spotify

[English](README.md) · [Українська](README.uk.md) · [Русский](README.ru.md) · [العربية](README.ar.md) · [简体中文](README.zh-Hans.md)

适用于 Android 车机的实验性 Spotify Automotive 播放器，拥有为汽车设计的自有界面：一个 APK、一个图标和标准的 Spotify 登录。免费——切勿付费。

[下载 test.17 APK](https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.17/Spotify-5.5.0-HeadUnit-test.17.apk) · [🌐 适用于 Android 车机的 Spotify](https://romanchukg-cloud.github.io/spotify-android-head-units/zh-Hans/)

![BYD 汽车中夜间的首页](docs/screens/car-home-night.jpg)

## 已完成的测试

Headunit-test.17 让加载和切换更流畅：不再显示“正在加载…”，而是显示与内容形状相同的占位块，内容依次浮现，封面淡入，页面从侧面滑入，标签页淡入淡出切换。自 test.16 起，它会显示音乐在哪里播放，并可在本车和你的其他设备之间切换（Spotify Connect）；迷你播放器提供时间、随机播放、重复和设备按钮，屏幕四周有一圈细边框，导航栏会收起全屏播放器。test.14 为可以添加小组件的启动器加入了桌面小组件（按大小提供五种布局，支持白天和夜间），可以打开其他应用中的 Spotify 链接，会告诉其他应用正在播放什么，并可在车机启动时在后台启动 Spotify（比亚迪除外）。它由完整参考构建生成。发布审计为 52 PASS / 0 FAIL；152 项更新、39 项登录、14 项面板访问、13 项 Connect 消息和 19 项播放启动检查通过，签名证书与 test.16 相同。在 Android 9 和 13 模拟器上，test.17 可作为更新安装在 test.16 之上，并在无崩溃的情况下检查更新；登录和“账号”流程在 Android 13 上通过。小组件已在 Android 11 和 12 模拟器的原生启动器中检查；链接在其中打开了歌单、专辑和艺人。相同代码的版本正在比亚迪唐（DiLink 5，Android 12）上运行，并在车上验证了新的动效；test.16 时还在车上验证了把音乐转到笔记本电脑再转回来。

**实验版 headunit-test.17。** 转移音乐仅在笔记本电脑上验证过。小组件目前只显示过虚构的曲目，比亚迪原厂启动器也无法显示它们。通过链接播放歌曲以及真正读取“正在播放”的应用尚未验证。右舵尚未在右舵车上见过；DiLink 5.1 / Android 13 和其他车机尚未验证；阿拉伯语只在模拟器上检查过。面板区块只会出现在支持它的应用中。Spotify 为其 Automotive 客户端关闭了下载；本版本开启了客户端支持，因此 Spotify 可能会改变下载的工作方式。

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

Android 9+ · 46.1 MiB · 非官方适配

## 文件校验

`Spotify-5.5.0-HeadUnit-test.17.apk` · 48 387 186 bytes

当前测试版本的 SHA-256：

```text
ad06da3de32647acdaba22b7411dc8a58468a09a0ee44814781cab2da646f8eb
```

非官方实验项目。此应用免费——如果您为此付费，请要求退款。Spotify 和 BYD 是其各自所有者的商标。

## 源代码与验证

我们的 Java 代码——界面、集成、补丁和构建脚本、审核及模拟器测试——都在仓库中，遵循 NOTICE.md 的条款（免费、不得出售、保留免费声明）。专有的 Spotify 后端是本地构建输入。自 test.13 起，APK 不再包含 BYD (Vivid) 播放器的任何部分。

[Java / build](adapter/) · [已完成的测试](reports/headunit-test.17.md) · [NOTICE](NOTICE.md)
