# SodaPop

每天脑子里会冒出很多想法，但大部分过几分钟就忘了。

SodaPop 是一个 Android App，让你随手记下这些碎片化的思考，然后用 LLM 帮你做整理、归纳和深度思考。

## 功能

**随手记** — 首页有个输入框，打开就能写。也能语音。记完 AI 自动打标签。

**每日总结** — 把今天记的东西汇总一下，告诉你今天在想什么，提几个值得继续想的问题。如果某条想法你反复在提，会建议你升级为「主题」；如果你表达了很确定的判断，会建议标记为「信念」。

**预测验证** — 觉得某件事会发生？记下来，设个日期。到时候回来看看对没对，AI 帮你分析原因。时间长了你会知道自己的判断力到底怎么样。

**思维对话** — 点开任何一条想法，跟 AI 聊。三种模式：帮你挑毛病（反面推敲）、帮你发散（联想拓展）、帮你评估能不能做（可行评估）。对话记录会保存，下次打开接着聊。

**灵感回放** — 随机翻出以前的想法，顺便看看和最近想的东西有没有关联。

**三层记忆** — 碎片 → 主题 → 信念，从原始记录到长期关注的方向再到稳定认知。AI 在总结时自动建议，你确认就行。

## 设计

### Prompt Chain

SodaPop 不做任何自主决策。每个 AI 功能背后是一条固定的 Prompt 链路：

```
记录 → 自动打标签
     → 每日总结 → 提升建议
     → 思维对话（多轮）
     → 预测分析
     → 关联发现
```

### 数据全在本地

没有服务器。数据库用 SQLCipher 加密存在手机里。LLM API 你自己配，密钥存在系统加密存储中。

支持所有 OpenAI 兼容接口：OpenAI、DeepSeek、Kimi、OpenRouter 等等。

## 使用

1. 装上 APK
2. 设置 → 填 API 配置（Base URL 填到 `/v1`，比如 `https://api.openai.com/v1`）
3. 测试连接通过 → 回首页开始用

## 构建

需要 JDK 17 和 Android SDK 34。

```bash
# Debug
./gradlew assembleDebug

# Release（需要先配签名，见下方）
./gradlew assembleRelease
```

Release 签名配置：

```bash
# 生成密钥（一次性）
mkdir -p keystore
keytool -genkeypair -v \
  -keystore keystore/sodapop-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias sodapop \
  -storepass YOUR_PASSWORD -keypass YOUR_PASSWORD \
  -dname "CN=SodaPop"

# 设环境变量
export SODAPOP_STORE_PASSWORD=YOUR_PASSWORD
export SODAPOP_KEY_ALIAS=sodapop
export SODAPOP_KEY_PASSWORD=YOUR_PASSWORD
```

## 技术栈

Kotlin、Jetpack Compose、Room + SQLCipher、Retrofit、Hilt、WorkManager、Glance

## License

[MIT](LICENSE)
