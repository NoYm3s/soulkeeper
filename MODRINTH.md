# Soulkeeper

**Your death leaves a Soul Anchor, not a scattered pile of loot.**

Soulkeeper is a lightweight Fabric mod for Minecraft 1.21.1 that gives survival death a fairer outcome without removing the risk. Your items and XP are sealed in a Soul Anchor at the place you died, a Soul Compass guides you back, and the Soulbound enchantment lets you keep a favourite piece of gear through death.

## Why Soulkeeper?

Losing an entire inventory is one of the harshest parts of survival, especially on long expeditions and multiplayer servers. `keepInventory` removes the risk completely, while vanilla item scattering can make a corpse run impossible before items despawn.

Soulkeeper offers a middle ground:

- your progress is not deleted;
- you still have to return to the place of death;
- Soulbound gives you a way to protect a few important items at a real cost;
- the mod stays close to vanilla: no GUI, no teleporting, no complicated progression.

## Features

- **Soul Anchor** — stores your items and XP where you died.
- **Soul Compass** — automatically provided after death; shows direction, distance, coordinates, and warns when the anchor is in another dimension.
- **Soulbound enchantment** — keeps an item in your inventory after death and costs 10% of its maximum durability (at least 1 point, never breaking it).
- **Curse of Vanishing compatibility** — vanishing items are destroyed instead of being stored.
- **Full XP recovery** — break the anchor to get your stored experience back.
- **Vanilla-style interaction** — right-click the anchor for its status, break it to recover the contents.
- **English and Russian localization**.

## Crafting

### Soul Anchor

```
S S S
S A S
S S S
```

`S` = Soul Soil, `A` = Amethyst Shard.

### Soul Compass

Shapeless: Compass + Soul Sand + Amethyst Shard.

Use the compass on a Soul Anchor to bind it. Use it in the air to display the direction and distance to the anchor.

## Soulbound

Soulbound is a level 1 data-driven enchantment for durability items in the mainhand, offhand, and armor slots. It can appear at an enchanting table and can be applied through an anvil.

On death, Soulbound items stay with you and lose 10% of their maximum durability. The curse of vanishing takes priority and destroys the item instead.

## How it works

When a player dies, Soulkeeper collects the non-Soulbound inventory and XP into a Soul Anchor. The anchor is placed at the first air or replaceable block above the death position (up to 32 blocks); if the player died in the void, the world spawn is used. A bound Soul Compass is added to the player's inventory and a message with the anchor coordinates is sent after respawn.

Breaking the anchor returns its contents and XP to the player who breaks it. Right-clicking it shows the owner, stored stacks, XP, and coordinates.

If only Soulbound items were in the inventory and there was no XP, no anchor is created.

## Requirements

- Minecraft **1.21.1**
- Fabric Loader **0.16.0+**
- Fabric API
- Java **21**

Install the mod on both the server and the client so the custom block, item, and textures are available.

## Notes and limitations

- `keepInventory=true` disables Soul Anchor creation.
- The mod cancels vanilla inventory dropping for handled deaths, so it may conflict with other grave or inventory-retention mods.
- Any player can break an anchor and take its contents. Consider this on public SMP servers.
- Anchors are highly blast resistant but not completely immune to destruction.
- The compass continues to point at the stored coordinates even if the anchor is later removed or moved.
- There is no configuration screen or anchor GUI.
- The mod does not teleport players or transfer items across dimensions automatically.

## Build

```bash
./gradlew build
```

The remapped jar is written to `build/libs/soulkeeper-1.0.0.jar`.

## License

MIT.
