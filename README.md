# BearPrison

The core gameplay plugin for the **Bear Nation** prison server: rank progression, combat tagging, ore condensing, item repair and server upkeep.

![Paper](https://img.shields.io/badge/Paper-26.1.2-blue) ![Java](https://img.shields.io/badge/Java-25-orange) ![Build](https://img.shields.io/badge/build-Gradle-02303A)

## Features

- **Rank-ups** paid for with BearEconomy money and applied through LuckPerms, including `/rankupmax` to buy as many ranks as you can afford at once
- **Rank-up reminders** when a player can afford the next rank
- **Combat tagging**, with a timer players can check
- **Ore condensing** (`/condense`) turns raw ores into blocks
- **Item repair** for the held item or the whole inventory
- **Ground-item clearing** on a schedule or by command
- Live **name tags** and **tab list** that show each player's rank
- Colour codes on signs

## Commands

| Command | Aliases | Description |
|---|---|---|
| `/rankup` | – | Buy the next rank |
| `/rankupmax` | `rumax`, `maxrankup` | Rank up as many times as you can afford |
| `/ranks` | – | View rank costs |
| `/condense` | `blocks` | Turn raw ores into blocks |
| `/fix [hand\|all]` | `repair` | Repair items |
| `/combat-timer` | `ctime` | Time left on your combat tag |
| `/cleardrops` | `cd` | Clear all ground items now |

## Requirements

- Paper 26.1.2+
- Java 25
- [BearEconomy](https://github.com/SashaWildfang/BearEconomy)
- [LuckPerms](https://luckperms.net/)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional)

## Building

BearEconomy is compiled against from `libs/BearEconomy-1.0.jar`.

```bash
./gradlew build
```

The plugin jar goes to `build/libs/`.

## Project layout

```
src/main/java/com/sasha/bearprison/
├── BearPrison.java      # Plugin entry point
├── commands/            # Rank-up, condense, fix, combat timer, clear drops
├── listeners/           # Combat, inventory, player and sign listeners
├── managers/            # Combat tagging
└── tasks/               # Item clearing, name tags, tab list, reminders
```

---

Part of the Bear Nation plugin suite.
