# Apotheos Body (Section 1) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry Fungal Gardens  Saint Mausoleums_V2.md` ("Apotheos in the Fungal Gardens": spore load, no eating, replacement healing loop). Builds on Section 0 (`HarbingerPathPermissions.isApotheos`).

## Goal

An Apotheos body no longer eats. It runs on spore load gathered from the Gardens, heals by burning spores or sinking into fungal ground, and is cured only by the morphic tonic inside the Gardens.

"Apotheos" below always means `HarbingerPathPermissions.isApotheos(degree, path)`.

## Decisions

### D1. Spore load
- New player attachment `SporeLoad` (`hemomancy:spore_load`): a float 0–100, plus a transient stand-still counter. **Not** copied on death, so a respawn starts empty.
- Owner sync: `PacketSyncSporeLoad(float)` through the `PlayerStateSync` table, with a dirty flag flushed at end of tick.
- Gain, once per second, Apotheos only (`SporeLoadRules.gainPerSecond`):

| Where | Gain per second |
| --- | --- |
| Fungal Gardens dimension | 2.0 |
| Overworld `fungal_gardens` biome | 0.5 |
| Anywhere else | 0 |

- Non-Apotheos players never gain spore load and never see the bar.
- "In the Gardens" means the dimension or the Overworld biome: `FungalGardensPlaces.isInGardens(level, pos)` in `common/world`.

### D2. Burning spores (instant heal)
- New key "Burn Spores" (default U), sent as the serverbound `BurnSporeLoadKeyPacket`, handled by `SporeLoadService.burn(player)`.
- The burn needs all of: Apotheos, alive, health below max, load ≥ 25, and the `PlayerCooldowns.SPORE_BURN` cooldown ready.
- It costs 25 load, heals 6 health and starts a 100-tick cooldown. It shows a burst of the mod's own glow particles (no vanilla particles).
- A failed burn says why on the action bar: not enough spores, cooling, or already whole.

### D3. Sinking into the mycelium (regrowth)
- An Apotheos **inside the Gardens** who is crouching, on the ground and not moving horizontally (position change² < 0.0004 per tick), with the block below in the `hemomancy:fungal_network_ground` tag, counts as **sinking**. (Gardens gate added in the review fix pass.)
- After 40 ticks of sinking, they heal 2 health every 20 ticks. Regrowth costs no spore load.
- Any movement, standing up or leaving fungal ground resets the counter.

### D4. No eating
- An Apotheos cannot start using any item with the `FOOD` data component, anywhere. `LivingEntityUseItemEvent.Start` is cancelled with the action-bar line "Your body refuses it."
- Inside the Gardens, an Apotheos also cannot drink vanilla potions (anything with `POTION_CONTENTS` and the drink animation) or milk buckets.
- The morphic tonic is not food and is never refused.

### D5. Hunger pin
- Every server tick, an Apotheos has food level 17, saturation 0 and exhaustion 0.
- 17 is high enough to sprint but below the regen threshold of 18, so there is no free regeneration. Chamber visits that restore food are re-pinned on the next tick.
- Morphling upkeep: for an Apotheos, the Symbiotic Metabolism split treats the player as not symbiotic, so the hunger share is paid in blood. Otherwise the pin would make that share free.

### D6. Morphic tonic
- New item `hemomancy:morphic_tonic`, stacks to 16, 16-tick drink with the drink animation, usable by anyone anywhere.
- Drinking it removes every harmful effect and grants Regeneration I for 200 ticks.
- Made by throwing a Bloody Jug into a morphic nectar pool: a `hemomancy:morphic_nectar` recipe, `input` `hemomancy:bloody_jug`, `result` `hemomancy:morphic_tonic`, `transform_time` 100.
- Texture is a recolour of the Lethean Dew flask in morphic nectar tones at 16 px.
- It goes in the main creative tab.

### D7. HUD
- For a local Apotheos player, the vanilla `FOOD_LEVEL` layer is cancelled through `RenderGuiLayerEvent.Pre` (the precedent is `FungalProjectionClientEvents`). A spore bar is registered with `registerAbove(VanillaGuiLayers.FOOD_LEVEL, …)` in `GuiLayerRegistrar`.
- The bar is ten 9×9 spore icons in the food bar's place (right of the hotbar, drawn right to left), each worth 10 load: full, half or empty.
- It is pixel art drawn with `fill`; no new texture.

### D8. Docs
Add the Apotheos body to `HEMOMANCY_REFERENCE.md` §5 (Apotheos) and `wiki/Harbinger-Path.md` Degree 8.

## Out of scope
- Spore load feeding manipulations and refilling blood.
- Draining colonized facsimiles.
- Hallowed ground drain (Section 7).
- The heart-node sanctum (Section 3).
