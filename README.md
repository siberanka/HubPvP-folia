<!-- DOWNLOAD_BADGES_START -->
<p align="center">
  <a href="https://gitlab.com/siberanka/HubPvP-folia/-/releases/permalink/latest/downloads/plugin.jar"><img alt="Download Paper" src="https://img.shields.io/badge/Download-Paper-2c2f33?logo=gitlab&amp;logoColor=white"></a>
  <a href="https://gitlab.com/siberanka/HubPvP-folia/-/releases/permalink/latest/downloads/plugin.jar"><img alt="Download Folia" src="https://img.shields.io/badge/Download-Folia-87c540?logo=gitlab&amp;logoColor=white"></a>
</p>
<!-- DOWNLOAD_BADGES_END -->

# HubPvP

[Upstream project, authors, and license status](UPSTREAM_ATTRIBUTION.md)

HubPvP plugin, created by Quared.

# What is HubPvP?

HubPvP is an essential plugin for every server's lobby. This allows players to equip the sword and PvP each other! Every
message and option can be changed in the plugin's config.yml.

# How it works

When someone equips the sword, their PvP is enabled. This allows them to PvP other players with their PvP on as well.
Once the sword is unequipped, the player will no longer be in PvP and can't be attacked.

# Features:

- Fully customizable
- Configuration reloading
- Easy to set up
- Fun plugin for everyone
- Paper 1.21.x support
- Folia support
- PlaceholderAPI support (`%hubpvp_status%`)
- Geyser/Floodgate compatibility improvements

# Commands:

- /hubpvp - Permission: hubpvp.reload

# What versions is it?

HubPvP natively supports Paper 1.21.x and Folia 1.21.11.

# 2.0.0 Update (siberanka)

The following updates were made by **siberanka**:

- Upgraded project version to `2.0.0`
- Updated API target to Paper `1.21.x` and enabled Folia support in `plugin.yml`
- Added inventory lock setting: `inventory.lock-item-slots` (default `true`)
- Added weapon custom model data setting: `items.weapon.custom-model-data` (default `-1`)
- Implemented `%hubpvp_status%` PlaceholderAPI expansion
- Enabled PlaceholderAPI parsing in player-facing messages
- Added automatic config default sync on startup and `/hubpvp reload`
- Fixed Geyser/Floodgate join issues by checking Floodgate API directly and clearing join armor for Floodgate players
- Refactored scheduler flow to be Paper/Folia compatible

# Where can I download it?

You can download it at https://www.spigotmc.org/resources/hubpvp.93475/ or in the releases section on GitHub.
