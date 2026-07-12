
# 🪶 Ink Client
> A sleek, high-performance utility client for Minecraft Pocket Edition (Bedrock) on Android. 

Ink Client changes how you experience mobile gameplay. Inspired by legendary setups like Lumina and Flarial, it strips away the bloated menus and aggressive neon clutter of traditional clients, replacing them with a highly optimized, enterprise-grade dark aesthetic paired with next-generation modular control.

---

## 🖤 The Core Profile

* **Ink Minimalist ClickGUI:** A pure, flat monochrome UI dashboard. Deep velvet black menus (`#0A0A0A`) paired with razor-sharp white active switches. Built for flawless visibility and speed mid-game.
* **Ink Loader Engine:** An ultra-lightweight, localized performance loader designed to manage internal system resources smoothly with maximum FPS stability.
* **In-Game Texture Changer:** A game-changing utility that intercepts live game assets. Swap, load, and test custom resource packs directly while connected to servers or active worlds—no game restarts required.
* **Pro Performance HUD:** Modern, borderless, crystal-clear screen overlays tracking your active keystrokes, frame timings, and actions-per-second (CPS) without blocking your field of view.

---

## 🎛️ Built-In Modules

Ink Client comes packed with a premium, fully customizable toolkit engineered for performance and utility:

| Category | Modules Included | Description |
| :--- | :--- | :--- |
| **Movement** | `Fly`, `Speed`, `Sprint`, `Step` | Dominate map traversal with fluid, custom-tuned velocity multipliers. |
| **Combat** | `Killaura`, `TriggerBot`, `Hitboxes` | Highly responsive tracking and hit detection optimized for mobile touch inputs. |
| **Visuals** | `HUD`, `Keystrokes`, `ESP`, `Fullbright` | Crystal-clear display overlays that keep vital stats visible without crowding your screen. |
| **Utility** | `GlobalResourcePackChanger`, `FastPlace` | Hot-swap global textures mid-game on any server and maximize block deployment speeds. |

---

## 🛠️ Compilation & Deployment

To compile your custom copy of Ink Client from source into an installable application, open your Android build pipeline or environment with the following environment standards:

* **Java Environment:** OpenJDK 17
* **Build System:** Gradle Lifecycle (Wrapper included in codebase)

### Running the Build

From your development workspace environment, run the automation commands to package the application:

```bash
# Grant execution permissions to the build tool
chmod +x gradlew

# Assemble the modular APK package
./gradlew assembleDebug

its a fork of wclient a huge inspiration 
