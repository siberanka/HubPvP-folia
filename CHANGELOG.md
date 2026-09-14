# Changelog

All notable changes maintained in this fork are documented here.

## 2.0.1 - 2026-09-14

### Fixed

- Mark temporary PvP equipment so stale armor can be identified safely after an unclean shutdown.
- Remove legacy PvP armor only when the complete equipped set exactly matches the configured HubPvP armor.
- Preserve and restore both flight permission and active flight state.
- Clone saved armor snapshots to avoid accidental inventory aliasing.
- Use concurrency-safe collections for player state handled by Paper and Folia schedulers.

## 2.0.0

### Added

- Paper 1.21.x and Folia support.
- `%hubpvp_status%` PlaceholderAPI expansion and placeholder parsing in messages.
- Configurable inventory slot locking and weapon custom model data.
- Automatic configuration default synchronization on startup and reload.
- Geyser and Floodgate compatibility improvements.

### Changed

- Refactor scheduling paths for Paper and Folia compatibility.
