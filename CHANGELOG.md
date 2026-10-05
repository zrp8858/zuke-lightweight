# Changelog -- zukeLightweight

## 0.1.0+26.3

- Initial project scaffolding.
- Slot refill: when the selected hotbar stack runs out, a matching stack from the main inventory is swapped in.
- Config: every feature is a toggle with an optional hotkey, via malilib (the
  library Tweakeroo and MiniHUD use). Open the config screen with `Z,C`
  (rebindable); it has Features and Hotkeys tabs.
- Optional Mod Menu integration: "Configure" opens the config screen.
- Slot Refill can be switched on and off.
- Hotbar Shuffle (replaces the block-placement-shuffler mod): while on, placing a block randomly reselects another placeable block from your hotbar. Toggle it and bind its hotkey (default `R`) in the config; toggling shows malilib's ON/OFF message.
- Mouse Tweaks: Shift + left-click drag quick-moves every stack you drag across (inventory, containers, and the creative Survival Inventory tab). Toggle it on the Mouse Tweaks tab of the config.
