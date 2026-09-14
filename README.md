# HubPvP Folia

A maintained Paper and Folia fork of [Quared0/HubPvP](https://github.com/Quared0/HubPvP). HubPvP gives lobby players an opt-in PvP item: players can fight only while they have PvP enabled.

## Features

- Paper and Folia 1.21.x support
- Configurable weapon, armor, slots, worlds, cooldowns, and messages
- Optional PlaceholderAPI integration with `%hubpvp_status%`
- PlaceholderAPI parsing in player-facing messages
- Geyser and Floodgate-compatible join handling
- Safe recovery of temporary HubPvP armor after an unclean shutdown
- Preservation of the player's original armor and flight state

## Requirements

- Java 17 or newer
- Paper 1.21.x or a compatible Folia build
- PlaceholderAPI is optional

## Installation

1. Download the JAR from [Releases](https://github.com/siberanka/HubPvP-folia/releases).
2. Put it in the server's `plugins` directory.
3. Restart the server and review `plugins/HubPvP/config.yml`.

Do not replace a production configuration blindly. Back it up first and merge newly introduced options where necessary.

## Commands and permissions

| Command or permission | Purpose | Default |
| --- | --- | --- |
| `/hubpvp` | Reloads the plugin configuration | `hubpvp.reload` |
| `hubpvp.use` | Allows use of the PvP item | Everyone |

## Temporary equipment safety

HubPvP marks the armor it creates. On join, only marked HubPvP armor is removed. For installations upgraded from an older release, `inventory.cleanup-legacy-pvp-armor-on-join` may remove a legacy armor set only when all four pieces exactly match the configured HubPvP set. Unrelated player armor is not cleared.

## Building

```bash
mvn clean verify
```

The release JAR is written to `target/HubPvP-<version>.jar`.

## Changes

See [CHANGELOG.md](CHANGELOG.md).

## Attribution and licensing

HubPvP was created by **Quared**. This repository is a maintained fork by **siberanka** and preserves the original Git history and attribution.

The upstream repository does not currently contain a license file. No additional license is asserted here; obtain permission from the relevant rights holders before redistributing or reusing the source outside the terms under which you received it.
