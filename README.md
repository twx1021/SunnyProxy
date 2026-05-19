# SunnyProxy（SunnyNet）

Android 平台上的 **SOCKS5 代理 VPN 客户端**。通过系统 `VpnService` 建立虚拟网卡，将设备流量经 **tun2socks** 转发至上游 SOCKS5 代理，支持全局代理与按应用分流，并可在 Root 环境下辅助安装抓包/调试所需证书。

---

## 代开源说明

本项目为 **代开源** 发布，实际开源提供方：

| 项目 | 信息 |
|------|------|
| 提供方 | **久久** |
| 联系 QQ | **1021284089** |

仓库中的代码与发布事宜由上述联系人授权代为公开；使用、二次分发或商业用途前请自行与提供方确认。

---

## 主要功能

- **SOCKS5 代理**：配置服务器地址、端口、用户名与密码
- **VPN 隧道**：基于 Android `VpnService`，将 TCP/UDP 流量导入隧道
- **全局 / 分应用代理**：可选全局代理，或仅代理指定应用列表
- **tun2socks 内核**：通过 Go JNI（`libgojni.so` 等）与原生组件完成 TUN → SOCKS 转发
- **Root 证书安装(SunnyNet证书)**（可选）：在已 Root 设备上，支持通过 Magisk / KernelSU / APatch 等环境安装系统/用户 CA，便于 HTTPS 调试（需用户自行承担合规责任）
- **开机自启**：若上次处于连接状态，可在开机后尝试恢复 VPN（见 `ServiceReceiver`）

---

## 技术栈

| 类别 | 说明 |
|------|------|
| 语言 | Java |
| 最低 SDK | 24（`app/build.gradle.kts`） |
| 目标 SDK | 34 |
| 架构 | `arm64-v8a`、`armeabi-v7a`、`x86` |
| 包名 | `com.SunnyNet.Sockes` |
| 核心模块 | `hev.sockstun`（界面与 VPN 服务）、`tun2socks`（流量转发）、`zhenshu`（Root/证书相关） |

---

## 环境要求

- Android Studio（推荐最新稳定版）或兼容的 Gradle 构建环境
- JDK 8+
- Android SDK（`compileSdk 34`）
- 真机或模拟器（VPN 与证书功能建议在真机测试）

---

## 构建与安装

```bash
# 克隆仓库后，在项目根目录执行
./gradlew assembleDebug

# 或在 Windows 上
gradlew.bat assembleDebug
```

生成的 APK 位于：

`app/build/outputs/apk/debug/app-debug.apk`

Release 构建：

```bash
./gradlew assembleRelease
```

---

## 使用说明

1. 安装并打开应用 **SunnyNet**
2. 填写 SOCKS5 服务器地址、端口及认证信息（如有）
3. 点击 **启动**；首次使用需同意系统 VPN 授权
4. 点击 **停止** 可断开 VPN
5. **证书安装**（可选）：仅适用于已 Root 设备，按界面提示选择 Magisk / KernelSU / APatch 等环境操作

> 应用内「关于」含用户协议与免责声明，首次启动会弹出，使用前请仔细阅读。

---

## 权限说明

应用可能申请包括但不限于：

- `INTERNET`、`ACCESS_NETWORK_STATE`、`CHANGE_NETWORK_STATE`：网络与代理
- `BIND_VPN_SERVICE`：建立 VPN
- `FOREGROUND_SERVICE`：前台 VPN 服务
- `RECEIVE_BOOT_COMPLETED`：开机自启
- `QUERY_ALL_PACKAGES`：分应用代理时列举已安装应用
- 存储、相机等：与证书/资源脚本等相关逻辑

---

## 免责声明

本软件 **仅供技术交流、学习与研究** 使用。请勿用于任何非法目的或未经授权的网络访问、数据窃取、外挂制作等行为。使用者须遵守当地法律法规及目标服务的服务条款；因违规使用产生的一切后果由使用者自行承担。

禁止对本应用进行逆向工程（包括但不限于反编译、篡改安装包、修改签名等），除非法律明确允许或已获得版权方书面授权。

---

## 开源协议

本项目采用 [MIT License](LICENSE) 发布。开源提供方：**久久**（QQ：1021284089）。

---

## 致谢与第三方组件

本项目中部分能力基于或参考社区方案实现，包括但不限于：

- VPN / SOCKS 隧道与 `hev.sockstun` 相关实现思路
- `tun2socks` / Go JNI 流量转发组件

若您发现侵权或需补充署名，请联系提供方 QQ：**1021284089**。
