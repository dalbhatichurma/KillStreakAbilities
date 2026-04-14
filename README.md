# ⚔️ KillStreakAbilities — Minecraft Plugin

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-brightgreen)
![Version](https://img.shields.io/badge/Version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-21-orange)

> Hypixel-style PvP kill streak system with unlockable abilities, god mode, and full config control.

---

## 🎯 Kill Streak Milestones

| Streak | Ability | Effect |
|--------|---------|--------|
| 3 kills | ⚡ Speed Boost | Speed II for 30s |
| 5 kills | 💪 Strength | Strength I for 30s |
| 10 kills | 🌟 God Mode | Invincible for 10s + BossBar countdown |

- Streak **resets instantly on death**
- All milestones are **configurable** (level, duration, on/off)

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/ks check <player>` | View a player's current streak + god mode status |
| `/ks reset <player>` | Reset a player's streak and remove god mode |
| `/ks reload` | Reload config.yml live |

Aliases: `/killstreak`, `/ks`

---

## 🔐 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `killstreak.admin` | OP | Access to all commands |

---

## ⚙️ config.yml

```yaml
save-streaks: true          # Persist streaks across restarts

streaks:
  speed:
    enabled: true
    kills-required: 3
    amplifier: 1            # 0 = Speed I, 1 = Speed II
    duration-seconds: 30

  strength:
    enabled: true
    kills-required: 5
    amplifier: 0            # 0 = Strength I
    duration-seconds: 30

  god-mode:
    enabled: true
    kills-required: 10
    duration-seconds: 10

feedback:
  sounds-enabled: true
  particles-enabled: true
  titles-enabled: true
  bossbar-enabled: true
```

---

## 📦 Installation

1. Drop `KillStreakAbilities.jar` into `plugins/`
2. Restart your server
3. Edit `plugins/KillStreakAbilities/config.yml`
4. Use `/ks reload` to apply changes live

---

## 🛠️ Building

```bash
cd KillStreakAbilities
mvn clean package
```

Output: `target/KillStreakAbilities.jar`

---

## 📁 Project Structure

```
KillStreakAbilities/
├── src/main/java/com/killstreak/
│   ├── KillStreakAbilities.java         ← Main class
│   ├── managers/
│   │   ├── KillStreakManager.java       ← Streak tracking + YAML persistence
│   │   └── AbilityManager.java         ← Speed, Strength, God Mode logic
│   ├── listeners/
│   │   ├── DeathListener.java          ← PvP kill detection + streak reset
│   │   └── DamageListener.java         ← God mode damage cancellation
│   └── commands/
│       └── KillStreakCommand.java       ← /ks with tab-complete
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## 📝 Changelog

### v1.0.0
- Kill streak tracking (PvP only)
- Speed II at 3 kills
- Strength I at 5 kills
- God Mode at 10 kills with BossBar countdown
- Damage cancellation during god mode
- Title + subtitle + sound + particles on ability unlock
- YAML streak persistence
- Full command suite with tab-complete

---

**Made with ❤️ for competitive PvP servers**
