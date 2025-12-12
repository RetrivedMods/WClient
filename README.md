
# WClient - #1 Utility Client for MCPE

**WClient** is a modular, high‑performance utility client designed for **Minecraft Bedrock Edition**. Built with a strong focus on stability, packet‑level control, and extensibility, WClient delivers advanced combat, movement, and visual enhancements while maintaining a clean, maintainable architecture.

WClient does **not** modify game memory directly and is engineered for compatibility across multiple Bedrock environments.

---

## Acknowledgements

Special thanks to the developers and open‑source projects that inspired this client, including **MuCuteClient**, **ProtoHax**, **Team-Nova** and **Project Lumina**. Their work formed the foundation for many architectural and conceptual improvements in WClient.

---

## Features

### Combat

* **Killaura** - TP Aura, Criticals, APS/CPS limiters, strafing logic, auto‑weapon switching, priority targeting (Nearest, Lowest HP), invisibility checks, team detection.
* **WAura** - Classic, highly configurable Killaura with multiple rotation and attack strategies.
* **OPFightBot** - Automated combat logic with predictive strafing and optional teleport mechanics.
* **EnemyHunter** - Auto Kills enemies brutally.

### Movement

* **MotionFly** - Horizontal/vertical speed control, anti‑kick systems, anti‑noclip bypasses.
* **Speed** - Stable high‑speed movement adjustments for multiplayer environments.
* **PlayerTP** - Teleport to enemies at specific distance.

### Visual

* **ESP** - Player and block highlighting with optimized render performance.
* **DamageText** - Real‑time damage indicators and tracers.
* **Smooth Zoom** - Adjustable zoom with smooth interpolation.

### Utility

* **Auto Armour** - Automatically equips the best armor available.
* **Auto Disconnect** - Automatically disconnects when low health.

---

## Platform Support

WClient is primarily developed and tested for **Android**, but can interface with other platforms using MITM‑style packet interception depending on setup.

Supported:

* Android (primary)
* Additional platforms may work depending on network configuration

---

## Module Categories

WClient modules are organized into the following categories:

* **Combat** – PvP enhancements and automated combat logic
* **Motion** – Mobility and speed modules
* **Visual** – Rendering and awareness tools
* **Misc** – General gameplay improvements

---

## GUI and Configuration

WClient supports dynamic configuration via JSON files, allowing runtime customization of modules, thresholds, ranges, and behavior.

Example:

```json
{
  "modules": {
    "AdvanceCombatAura": { "enabled": true, "range": 4.5 },
    "MotionFly": { "enabled": false, "horizontalSpeed": 1.4 }
  }
}
```

---

## License

WClient is licensed under the **GNU General Public License v3.0 (GPLv3)**.

### Permitted

* Personal use and modification
* Redistribution of modified or unmodified versions **with source code included**
* Creating content (videos, showcases, tutorials)

### Prohibited

* Distributing modified binaries **without providing full source code**
* Claiming ownership of the project or its original code

Full license text: [https://www.gnu.org/licenses/gpl-3.0.en.html](https://www.gnu.org/licenses/gpl-3.0.en.html)

---

## Disclaimer of Warranty

This software is provided **“AS IS”**, without warranty of any kind, express or implied, including but not limited to fitness for a particular purpose or non‑infringement.

## Limitation of Liability

In no event shall the authors be liable for any damages arising from the use of this software.

## Intended Use

This project is intended **solely for educational and research purposes**.
Users are responsible for ensuring compliance with local laws and server rules.

---

## Contributions

Contributions are welcome. Bug reports, feature suggestions, and pull requests help improve WClient.

---

## Community & Support

Join the WClient community for support, updates, and discussions:

Discord: [https://discord.gg/wclient](https://discord.gg/jVWPuDvdRX)

---

*WClient is not affiliated with Mojang Studios or Microsoft.*
