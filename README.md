# 🪶 Ink Client
> A sleek, high-performance proxy client and launcher for Minecraft Bedrock on Android. 

Ink Client strips away the clutter of traditional setups, offering an ultra-clean, enterprise-grade dark aesthetic paired with robust network-layer control.

---

## 🖤 Core Aesthetics & Features

* **Ink Minimalist Theme:** A pure, flat monochrome design. Deep velvet black layouts (`#0A0A0A`) paired with razor-sharp white active accents.
* **Ink Loader Engine:** A lightweight, optimized internal proxy proxying local loopback streams (`127.0.0.1`) smoothly.
* **In-Game Hot-Swapper:** Dynamically intercept and swap custom textures mid-game over the packet stream without restarting your app.
* **Pro Performance HUD:** Modern, borderless text overlays to trace your frame timings and actions with zero performance drag.

---

## 🛠️ Developer Setup & Compilation

To build your custom copy of Ink Client from source code into an installable application, use an Android IDE or standard command-line build tools equipped with the following dependencies:

* **Java Development Kit:** Version 17 (OpenJDK 17)
* **Build Automation:** Gradle Wrapper (included in the project tree)

### Triggering the Build

Open your project compiler, navigate into the root directory of the repository, and execute the build sequence:

```bash
# Grant execution rights to the automation wrapper
chmod +x gradlew

# Compile and package the application
./gradlew assembleDebug
