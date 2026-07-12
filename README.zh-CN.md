# airplay-win

[![License](https://img.shields.io/github/license/jensoncheung11/airplay-win)](./LICENSE)

[English README](./README.md)

适用于 Windows 10 / 11 x64 的便携版 AirPlay 接收器。

本项目基于 [serezhka/java-airplay](https://github.com/serezhka/java-airplay) 进行整理和适配，目标是提供一个适合个人直接使用的 Windows 无线投屏接收方案：解压即可运行，可接收 iPhone / iPad 的 AirPlay 投屏，并在电脑上播放画面和声音。

## 功能特点

- 支持 iPhone / iPad 无线投屏到 Windows
- 支持电脑端播放投屏声音
- 免安装，解压即用
- 内置 Java 运行环境
- 内置 GStreamer 运行环境
- 提供启动、调试、关闭脚本
- 适配 Windows 10 / 11 x64

## 下载

请前往发行版页面下载最新打包文件：

- GitHub Releases: [https://github.com/jensoncheung11/airplay-win/releases/latest](https://github.com/jensoncheung11/airplay-win/releases/latest)
- Gitee 发行版: [https://gitee.com/jensoncheung1/airplay-win/releases](https://gitee.com/jensoncheung1/airplay-win/releases)

主要附件：

- `ios-airplay-win-x64.zip`

## 快速使用

1. 下载 `ios-airplay-win-x64.zip`
2. 将压缩包完整解压到一个普通文件夹
3. 双击运行 `Start AirPlay.cmd`
4. 首次运行如果弹出 Windows 防火墙提示，请允许专用网络访问
5. 确保电脑和 iPhone / iPad 在同一局域网内
6. 在 iPhone / iPad 打开控制中心
7. 点击“屏幕镜像”
8. 选择 `AirPlay`

如需完全退出接收器，可双击 `Stop AirPlay.cmd`，或者直接关闭启动后的控制台窗口。

## 附带脚本说明

- `Start AirPlay.cmd`
  - 正常启动投屏接收器

- `Start AirPlay Debug.cmd`
  - 用于调试启动问题，方便查看输出信息

- `Stop AirPlay.cmd`
  - 用于彻底关闭当前运行中的接收器进程

## 日志

运行日志默认写入：

```text
logs\receiver.log
```

## 常见问题

### 1. 手机里找不到 `AirPlay`

请检查：

- 程序是否已经启动
- 手机和电脑是否处于同一局域网
- 是否开启了 VPN、代理或网络隔离
- Windows 防火墙是否已允许程序通过专用网络

### 2. 有画面但没有声音

请尝试：

- 断开投屏后重新连接一次
- 检查当前 Windows 默认播放设备是否正常输出声音

### 3. 有声音但没有画面

请尝试：

- 重新连接一次
- 部分受版权保护的视频内容可能无法正常显示

### 4. 断开投屏后程序没有自动退出

这是当前设计行为。  
断开投屏只会回到等待连接状态，不会自动关闭程序。  
如果需要完全退出，请运行 `Stop AirPlay.cmd`。

### 5. 投屏卡顿或不流畅

建议：

- 优先使用 5GHz Wi‑Fi
- 尽量让手机靠近路由器
- 保持局域网环境稳定
- 避免同时进行高占用网络传输

## 从源码构建

项目内已包含 Windows 打包脚本，可用于构建并生成便携版压缩包。

在项目根目录执行：

```powershell
.\packaging\windows\package.ps1
```

生成结果：

```text
dist\ios-airplay-win-x64\
dist\ios-airplay-win-x64.zip
```

## 技术说明

- 默认播放器后端：GStreamer
- 默认显示方式：Swing 窗口
- 默认设备名：`AirPlay`
- 默认目标分辨率：1920x1080
- 默认目标帧率：60fps

## 上游项目

本项目基于以下开源项目：

- [serezhka/java-airplay](https://github.com/serezhka/java-airplay)

感谢上游项目提供 AirPlay 接收实现基础。
