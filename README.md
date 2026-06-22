# ZenSelf Mobile 🧘

Android 伴侣 App，配合 [ZenSelf](https://github.com/ylyy/ZenSelf) Mac 端使用。手机端采集语音倾诉、心情打卡、剪贴板、位置、屏幕使用时长，写入 `mobile_buffer.jsonl`，经 Syncthing 同步到 Mac，让 ZenSelf agent 在你不在电脑前时也能感知真实状态。

## 安装

### 1. 下载 APK

1. 打开 [Actions 页面](https://github.com/ylyy/zenself-mobile/actions)
2. 点最新一次成功的构建
3. 在页面底部 **Artifacts** 区域下载 `zenself-mobile-debug`
4. 解压得到 `app-debug.apk`

### 2. 安装到手机

1. 把 APK 传到手机（USB / 网盘 / 微信发送给自己）
2. 手机文件管理器点击 APK
3. 系统提示"为了安装此应用，请允许来自此来源"→ 允许
4. 安装完成

### 3. 授予权限

首次打开 App 会请求：
- **麦克风** — 语音倾诉
- **位置** — 地点打卡 + 后台位置追踪
- **通知** — Android 13+ 前台服务必需

然后去 **设置** 页面手动开启：
- **使用时长统计** — 点击会跳转到系统"使用情况访问权限"页面，找到 ZenSelf 并开启

## 功能

### 手动（4 个底部标签）

| Tab | 功能 |
|---|---|
| 🎙️ 语音倾诉 | 点击麦克风说话 → 自动转文字 → 写入 voice event |
| 😊 心情打卡 | 选 emoji（焦虑/专注/低落/放松/分心）+ 可选备注 → checkin event |
| 📋 复制即记 | 读取剪贴板 → 预览 → 确认 → clipboard event |
| 📍 地点打卡 | 获取 GPS → 可选填下一个日程 → context event |

### 后台（需在设置开启）

- **定时指标采集** — 每 30 分钟自动写一条 metric event（屏幕使用时长）
- **地点触发打卡** — 持续追踪位置，每分钟写一条 context event（lat,lng）

## Mac 端配合

App 默认写入 `Downloads/ZenSelfMobileSync/mobile_buffer.jsonl`。Mac 端用 Syncthing 同步该目录，然后用 bridge 脚本 append 到 daemon 的 buffer。详见 [ZenSelf 的 Android 配置指南](../ZenSelf/docs/mobile-setup/android-setup.md)。

简版 bridge 脚本（Mac 上跑）：

```bash
brew install fswatch
cat > ~/ZenSelfMobileSync/bridge.sh << 'EOF'
#!/bin/bash
SYNC="$HOME/Downloads/ZenSelfMobileSync/mobile_buffer.jsonl"
DAEMON="$HOME/.config/zenself/mobile_buffer.jsonl"
mkdir -p "$(dirname "$DAEMON")"
[ -s "$SYNC" ] && cat "$SYNC" >> "$DAEMON" && > "$SYNC"
EOF
chmod +x ~/ZenSelfMobileSync/bridge.sh
fswatch -o ~/Downloads/ZenSelfMobileSync | xargs -n1 -I{} bash ~/ZenSelfMobileSync/bridge.sh &
```

## Event 格式

每行一条 JSON，append 到 `mobile_buffer.jsonl`：

```json
{"ts":"2026-06-21T14:30:00Z","kind":"voice","text":"刚开完会被批了"}
{"ts":"2026-06-21T14:35:00Z","kind":"checkin","mood":"anxious"}
{"ts":"2026-06-21T15:00:00Z","kind":"metric","screen_time_min":47}
{"ts":"2026-06-21T15:30:00Z","kind":"context","location":"39.90420,116.40740","next_event":"周会"}
{"ts":"2026-06-21T16:00:00Z","kind":"clipboard","text":"复制的网页内容"}
```

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- WorkManager（定时采集）
- Foreground Service（位置追踪）
- FusedLocationProviderClient（GPS）
- GitHub Actions 远程构建（无需本地 Android SDK）

## 重新构建

push 到 main 即触发 GitHub Actions。也可在 Actions 页面手动点 "Run workflow"。

## License

MIT
