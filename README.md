<div align="center">

<img src="assets/elephant.png" alt="Footprints Logo" width="160"/>

<h1>Footprints</h1>

> **Your private AI companion. 100% on-device. 100% offline. Zero compromises.**

[![Platform](https://img.shields.io/badge/Android-11%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/11)
[![Model](https://img.shields.io/badge/Model-Qwen%202.5%200.5B-FF6B6B?logo=huggingface&logoColor=white)](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF)
[![Engine](https://img.shields.io/badge/Engine-llama.cpp-8A2BE2?logo=cplusplus&logoColor=white)](https://github.com/ggml-org/llama.cpp)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![CI/CD](https://github.com/rajumark/Footprints/actions/workflows/release.yml/badge.svg)](https://github.com/rajumark/Footprints/actions/workflows/release.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-BOM%202026.03-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![AGP](https://img.shields.io/badge/AGP-9.0.1-3DDC84?logo=android&logoColor=white)](https://developer.android.com/build/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/Gradle-9.1.0-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![MinSDK](https://img.shields.io/badge/minSdk-30-brightgreen?logo=android&logoColor=white)](https://developer.android.com/about/versions/11)
[![TargetSDK](https://img.shields.io/badge/targetSdk-36-brightgreen?logo=android&logoColor=white)](https://developer.android.com/about/versions/14)

</div>

---

## ✨ Why Footprints?

Every step you take in the digital world leaves a **footprint**. Your data, your conversations, your thoughts — they shouldn't belong to a server in someone else's data center.

**Footprints** is a chat app that runs entirely on your Android device. No cloud. No internet. No data collection. Just you and a powerful AI — Qwen 2.5 0.5B — living in your pocket.

---

## 🔒 Security Posture

Footprints is designed with privacy as its **zero-trust foundation**:

| Threat | Mitigation |
|--------|-----------|
| Network surveillance | **No `INTERNET` permission**. The app cannot make network calls — **period**. |
| Cloud data breaches | All inference is **on-device**. Data never leaves your phone. |
| Account tracking | **No account system**. No sign-up, no login, no profile. |
| Analytics / Telemetry | **Zero tracking SDKs**. No Firebase, no Google Analytics, no Crashlytics. |
| Third-party data mining | **No ad SDKs, no third-party APIs, no server dependencies**. |
| Physical device theft | Conversations saved in **app-internal storage** (`filesDir`), not accessible to other apps without root. |

> The only permission requested is `RECORD_AUDIO` for optional voice input. Everything else works with zero permissions.

---

## 📊 Performance Benchmarks

Measured on **moto g57 power** (arm64-v8a, Android 14, SDK 36):

| Metric | Value |
|--------|-------|
| Model load time (cold start) | ~45s |
| Inference speed | ~10–15 tok/s (text generation) |
| Peak RAM usage | ~866 MB |
| APK size | 507 MB |
| Model size (GGUF Q4_K_M) | 469 MB |
| App + native libs overhead | ~38 MB |
| Startup to ready | ~50s (model copy + load) |
| Token context window | 32,768 tokens (full Qwen 2.5 capacity) |
| Max generation tokens | 2048 (configurable via settings, 128–2048) |

> Performance varies by device CPU. On newer flagships (Snapdragon 8 Gen 2/3), expect 15–20+ tok/s.

---

## 🧠 What's Under the Hood

```
┌─────────────────────────────────────────────────────────────┐
│                        Footprints                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Presentation Layer (Jetpack Compose + Material 3)  │   │
│  │  ┌──────────────┐  ┌────────────┐  ┌────────────┐  │   │
│  │  │  ChatScreen  │  │  Settings  │  │Conversation│  │   │
│  │  │  (Compose)   │  │  Sheet     │  │  Sheet     │  │   │
│  │  └──────┬───────┘  └────────────┘  └────────────┘  │   │
│  └─────────┼───────────────────────────────────────────┘   │
│            │ collectAsStateWithLifecycle()                    │
│  ┌─────────▼───────────────────────────────────────────┐   │
│  │  ViewModel (ChatViewModel)                          │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │   │
│  │  │ sendMsg │ │ history  │ │ export   │ │ voice  │ │   │
│  │  │ stream  │ │ persist  │ │ chat     │ │ input  │ │   │
│  │  └────┬─────┘ └──────────┘ └──────────┘ └────────┘ │   │
│  └───────┼─────────────────────────────────────────────┘   │
│          │ Flow<String> (token-by-token)                      │
│  ┌───────▼─────────────────────────────────────────────┐   │
│  │  LlmEngine (Singleton Wrapper)                      │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │  AiChat.getInferenceEngine(context)           │   │   │
│  │  │  → loadModel(path) → setSystemPrompt()       │   │   │
│  │  │  → sendUserPrompt() → Flow<String>            │   │   │
│  │  └──────────────────────┬───────────────────────┘   │   │
│  └─────────────────────────┼───────────────────────────┘   │
│                            │ JNI / Native                     │
│  ┌─────────────────────────▼───────────────────────────┐   │
│  │  llama.cpp C++ (via Android SDK wrapper)            │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │  Qwen 2.5 0.5B (Q4_K_M GGUF) ← CPU Inference │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 🏗️ Architecture Highlights

- **MVVM**: Clean separation — `Compose UI → ViewModel (StateFlow) → LlmEngine → JNI → C++`
- **No DI framework**: Manual dependency injection via `AndroidViewModel` + `LlmEngine` singleton — keeps the dependency graph trivially traceable
- **Single Activity**: `MainActivity` sets edge-to-edge, applies theme, composes `ChatScreen` — no navigation complexity
- **Token Streaming**: `sendUserPrompt()` returns `Flow<String>` — the ViewModel collects tokens one-by-one and updates UI in real time via `MutableStateFlow`
- **Global Theme Toggle**: `AppDarkTheme` — a top-level `mutableStateOf` variable overrides system theme across all composables without recomposition issues

---

## 🧪 Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.3.20 |
| Build System | Gradle | 9.1.0 |
| Android Plugin | AGP | 9.0.1 |
| UI Framework | Jetpack Compose (BOM) | 2026.03.01 |
| Design System | Material 3 | via BOM |
| Architecture | MVVM + StateFlow | — |
| Serialization | kotlinx-serialization-json | 1.8.1 |
| Lifecycle | Lifecycle Runtime/ViewModel Compose | 2.10.0 |
| LLM Engine | llama.cpp (Android SDK) | v0.0.4 |
| Inference Model | Qwen 2.5 0.5B Instruct (GGUF) | Q4_K_M |
| Navigation | Navigation3 (declared, unused — single screen) | 1.0.1 |
| Testing (planned) | JUnit 4 + kotlinx-coroutines-test | 4.13.2 / 1.10.2 |

---

## 📱 Feature Comparison

| Feature | Footprints | ChatGPT | LlamaChat | Ollama Mobile |
|---------|-----------|---------|-----------|---------------|
| 100% Offline | ✅ | ❌ | ✅ | ✅ |
| No Account Required | ✅ | ❌ | ✅ | ✅ |
| Zero Telemetry | ✅ | ❌ | ❌ | ❌ |
| Open Source | ✅ | ❌ | ❌ | ✅ |
| Token Streaming | ✅ | ✅ | ✅ | ✅ |
| Chat History | ✅ | ✅ | ✅ | ❌ |
| Export Chat | ✅ | ✅ | ❌ | ❌ |
| System Prompt Control | ✅ | ✅ | ✅ | ✅ |
| Voice Input | ✅ | ✅ | ❌ | ❌ |
| Markdown Rendering | ✅ | ✅ | ✅ | ✅ |
| Dark/Light Theme | ✅ | ✅ | ❌ | ❌ |
| On-Device Model | 0.5B | ❌ (cloud) | Any GGUF | Any GGUF |
| APK Size | 507 MB | N/A | ~10 MB + model | ~10 MB + model |
| Free & No Subscription | ✅ | ❌ | ✅ | ✅ |
| Privacy Guaranteed | ✅ (no internet perm) | ❌ | Depends on model | Depends on model |

---

## 🎥 Demo

<div align="center">
  <img src="screenshots_video/screenshot1.png" alt="Footprints Chat" width="280"/>
  <br/><br/>
  <video src="screenshots_video/demovideo.mp4" controls width="280" poster="screenshots_video/screenshot1.png">
    Your browser does not support the video tag.
  </video>
  <br/><br/>
</div>

---

## 💡 Edge AI Philosophy

**Why run a model on your phone when the cloud is faster?**

1. **Latency**: Cloud round-trips add 300–1500ms. On-device inference starts generating instantly.
2. **Privacy**: Your conversations are literally never transmitted. Not encrypted-in-transit — not transmitted at all.
3. **Offline-first**: Works in airplanes, tunnels, rural areas, or when you simply turn off data.
4. **No subscriptions**: You own the model. You own the hardware. No monthly fees, no API pricing.
5. **Sustainability**: Edge inference uses your existing device instead of powering server farms.

Footprints embodies the **Edge AI ethos**: the most private AI is the one that never leaves your pocket.

---

## ❓ Why Not... (FAQ)

| Question | Answer |
|----------|--------|
| Why not GPT-4? | GPT-4 is 1.7T parameters — 6,300× larger than 0.5B. No phone can run it. Cloud GPT requires internet, account, and subscription. |
| Why not a 7B model? | 7B models (Mistral, Llama 3) require 4–8GB RAM at Q4 and run at 2–5 tok/s on phones. 0.5B fits in ~500MB and runs 3–5× faster. **Smallest viable model for real-time chat.** |
| Why Qwen 2.5 specifically? | Top-performing 0.5B model on MT-Bench, supports 32K context, strong instruction following. Compact GGUF at Q4_K_M fits in APK assets. |
| Why no GPU acceleration? | llama.cpp CPU inference runs on **every** Android device. GPU (Vulkan/OpenCL) support varies wildly by OEM driver quality. CPU is universal. |
| Why isn't Python used? | Android does not ship a Python runtime. Kotlin/Native + JNI to C++ is the standard path for high-performance Android ML. |
| Why is the APK so large? | The GGUF model file (469 MB) is bundled in APK assets. This guarantees 100% offline first launch — no download required. |

---

## 📦 Installation

| Method | Command |
|--------|---------|
| **Download APK** | Grab latest from [Releases](https://github.com/rajumark/Footprints/releases) |
| **Install via ADB** | `adb install app-debug.apk` |
| **Build from source** | `./gradlew assembleDebug` |

> **Requirements:** Android 11+ (API 30), ~1GB free storage, 500MB+ RAM

---

## 🚀 Quick Start

```bash
# Clone with LFS (model file is LFS-tracked)
git clone https://github.com/rajumark/Footprints.git
cd Footprints
git lfs pull

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n raju.shingadiya.footprints/.MainActivity
```

<div align="center">
  <img src="screenshots_video/screenshot1.png" alt="App Screenshot" width="240"/>
  <em>Footprints running on Android 14</em>
</div>

---

## 📁 Project Structure

```
app/
├── src/main/
│   ├── assets/
│   │   └── qwen2.5-0.5b-instruct-q4_k_m.gguf  ← The model (469MB, LFS)
│   ├── java/raju/shingadiya/footprints/
│   │   ├── MainActivity.kt                     ← Entry point (edge-to-edge, theme)
│   │   ├── llm/
│   │   │   └── LlmEngine.kt                     ← llama.cpp singleton wrapper
│   │   ├── ui/chat/
│   │   │   ├── ChatScreen.kt                    ← All Compose UI (713 lines)
│   │   │   ├── ChatViewModel.kt                 ← State & business logic (373 lines)
│   │   │   └── ChatMessage.kt                   ← @Serializable data model
│   │   └── theme/
│   │       ├── Color.kt                         ← Light/dark palette (navy/blue)
│   │       ├── Theme.kt                         ← Material 3 + global toggle
│   │       └── Type.kt                          ← Typography overrides
│   ├── AndroidManifest.xml
│   └── res/
│       ├── drawable/                            ← Vector icons + launcher foreground
│       ├── mipmap-anydpi-v26/                   ← Adaptive launcher icon
│       └── values/                              ← Strings, colors, fallback theme
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml                       ← Version catalog
│   └── wrapper/gradle-wrapper.properties
├── .github/workflows/release.yml                ← CI/CD
└── docs/index.html                              ← Landing page
```

**File count:** 8 Kotlin files (~1,255 lines total).

---

## 🔄 CI/CD

Every push to `main` with `#go` in the commit message triggers GitHub Actions to:

1. ✅ Checkout with LFS (model file)
2. ✅ JDK 17 setup (Temurin)
3. ✅ Build the debug APK (`assembleDebug`)
4. ✅ Extract version from `build.gradle.kts`
5. ✅ Create a **GitHub Release** with auto-generated body
6. ✅ Upload the APK (`app-debug.apk`) as a release artifact

[![CI/CD](https://github.com/rajumark/Footprints/actions/workflows/release.yml/badge.svg)](https://github.com/rajumark/Footprints/actions/workflows/release.yml)

> Planned: add `lint` and `test` steps before release gates.

---

## 🧪 Testing (Planned)

| Test Type | Status | Framework |
|-----------|--------|-----------|
| Unit tests (ViewModel) | Pending | JUnit 4 + kotlinx-coroutines-test |
| Unit tests (LlmEngine) | Pending | JUnit 4 |
| UI tests (Compose) | Pending | Compose UI Test |
| Lint checks | Pending | detekt or ktlint |
| Integration (CI) | Pending | GitHub Actions |

Dependencies are already declared in `build.gradle.kts` — tests are the next priority.

---

## 🗺️ Roadmap

### v1.0 (Current)
- [x] Local chat with Qwen 2.5 0.5B (token streaming)
- [x] 100% offline inference (no network permission)
- [x] CI/CD with auto-release (`#go` commits)
- [x] Chat history persistence (JSON, app-internal storage)
- [x] Token-by-token streaming UI
- [x] System prompt presets (Default / Creative / Professional / Concise / Custom)
- [x] Export conversation (Downloads/)
- [x] Dark / light theme toggle
- [x] Markdown rendering (bold, italic, inline code, code blocks)
- [x] Voice input (SpeechRecognizer)
- [x] Conversation management (list, switch, delete)

### v1.1 (Next)
- [ ] In-app model download (switch between 0.5B / 1.5B / 3B)
- [ ] RAG (Retrieval-Augmented Generation) — chat with documents
- [ ] Token usage counter / speed display
- [ ] Search within conversation history
- [ ] Custom system prompt editor (text field for "Custom" preset)

### v1.2 (Future)
- [ ] Multi-model support (swap Qwen ↔ Llama ↔ Phi at runtime)
- [ ] Streaming TTS for AI responses
- [ ] Image understanding (multimodal Qwen)
- [ ] i18n / localization (Hindi, Gujarati, Spanish, etc.)
- [ ] In-app benchmark page (tok/s, RAM, load time)

---

## 🤝 Contributing

Contributions are welcome! Here's how:

1. **Fork** the repo
2. **Create a feature branch**: `git checkout -b feat/amazing-idea`
3. **Commit with `#go`** if you want CI to build a release: `git commit -m "Add amazing idea #go"`
4. **Push** and open a Pull Request

### PR Checklist
- [ ] Code follows existing patterns (MVVM, StateFlow, Compose)
- [ ] No new permissions added unless absolutely necessary
- [ ] No network (INTERNET) permission — ever
- [ ] Lint passes (`./gradlew lint`)
- [ ] Tests pass (`./gradlew test`)
- [ ] Dark & light theme both look correct

---

## 🧠 Model Details: Qwen 2.5 0.5B

| Attribute | Value |
|-----------|-------|
| Architecture | Transformer decoder-only |
| Parameters | 494M (0.5B) |
| Context length | 32,768 tokens |
| Quantization | Q4_K_M (4-bit, 469 MB) |
| Training data | Up to June 2024 |
| Languages | English, Chinese, multilingual |
| Instruction-tuned | Yes (Instruct variant) |
| MT-Bench score (0.5B) | 4.82 |
| Alignment | RLHF + DPO |

The **smallest usable LLM for real-time mobile chat**. At Q4_K_M quantization, it fits entirely in device RAM with ~500MB overhead, leaving room for the OS and other apps.

---

## 📊 Sizing

| Component | Size |
|-----------|------|
| Qwen 2.5 0.5B (Q4_K_M GGUF) | 469 MB |
| llama.cpp native libraries | ~18 MB |
| Compose + Material 3 + Kotlin stdlib | ~12 MB |
| App code + resources | ~8 MB |
| **Total APK** | **~507 MB** |

---

## 🔧 Profiling & Monitoring

Footprints currently supports basic performance monitoring via `adb logcat`:

```bash
# View model load progress and inference timings
adb logcat -s "Footprints" --pid $(adb shell pidof raju.shingadiya.footprints)
```

> Future versions will include an in-app stats panel showing tokens/sec, memory usage, and generation latency.

---

## 🌐 i18n / Localization

| Language | Status |
|----------|--------|
| English | ✅ Full support |
| Other languages | ❌ Planned for v1.2 |

The UI is currently English-only. The Qwen 2.5 model itself supports English, Chinese, and limited multilingual responses. Localization of the UI strings (`strings.xml`) is planned. Contributions welcome!

---

## 📜 License

MIT — Use it, fork it, ship it. See [LICENSE](LICENSE).

---

<div align="center">

**Built with ❤️ for the Edge AI era**  
*Your data stays where it belongs — in your hands.*

[![GitHub Repo](https://img.shields.io/badge/GitHub-rajumark/Footprints-181717?logo=github&logoColor=white)](https://github.com/rajumark/Footprints)

</div>
