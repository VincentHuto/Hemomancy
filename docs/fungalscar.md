# Hemomancy Implementation Brief — Additional Fungal Scars

## Goal

Implement a new batch of **Fungal Scars** for Hemomancy.

These are late-game Archon/Apotheos-tier scar effects that grant abilities normally unavailable through vanilla Minecraft or ordinary Hemomancy progression.

Do **not** implement:
- Vein miner
- Elytra-style flight
- Permanent water breathing / swim boosting

Those already exist.

## Design Rules

Fungal Scars should:
- Be distinct from normal scars.
- Feel like biological/world-rule mutations, not simple potion effects.
- Be limited so the player cannot stack too many at once.
- Prefer one equipped Fungal Scar at a time unless an existing upgraded binder supports more.
- Require late-game access, ideally Archon or Apotheos.
- Integrate with existing scar, capability, blood volume, tendency, and morphling systems where practical.

## New Fungal Scars To Implement

---
## 2. Scar of the Crawling Choir

### Effect
Certain blood manipulations have a chance to echo-cast at reduced strength.

### Behavior
When the player successfully casts a manipulation:
- Roll a chance.
- If successful, repeat the manipulation effect at reduced potency.
- Echo should cost reduced or no additional blood.
- Echo should not recursively trigger another echo.

### Suggested tuning
- 15–25% echo chance.
- Echo potency around 35–50%.
- Add a flag/context parameter to prevent infinite recursion.

### Implementation notes
Integrate into existing manipulation cast pipeline, not generic item use.

---

## 3. Scar of the Vein Orchard

### Effect
Killed mobs may leave behind a temporary blood/fungal growth node.

### Behavior
When the player kills a valid living entity:
- Chance to place or spawn a temporary “blood growth” node at death location.
- Node can be harvested for small blood/fungal resources.
- Node expires after a configurable time.
- Optional: nearby hostile mobs are attracted to it.

### Suggested resources
- Hematic dust
- Spore sac
- Small blood vial equivalent
- Fungal residue item if one exists

### Implementation notes
Use entity death event.  
Prefer a simple temporary block or entity with saved lifetime.

---

## 4. Scar of the Split Husk

### Effect
Prevents death by bursting the player into a fungal survival state, then reforming them.

### Behavior
When lethal damage would kill the player:
- Cancel death if scar is off cooldown.
- Set player to low health.
- Apply brief invulnerability.
- Teleport/displace player slightly nearby.
- Spawn fungal burst particles/entities.
- Apply long cooldown.

### Suggested tuning
- Cooldown: 10–20 minutes.
- Reform health: 20–30%.
- Blood cost: large, or drain all current blood.
- If insufficient blood, the effect should fail.

### Implementation notes
Hook into living death or player death-prevention event.  
Be careful not to conflict with totems or other death-prevention mechanics.

---

## 7. Scar of the Latching Vein

### Effect
Links damaged enemies together so damage echoes between them.

### Behavior
When the player damages an enemy:
- Apply a temporary tether marker to nearby enemies or the struck enemy.
- Tethered enemies share a small percentage of damage.
- Tethers expire after a few seconds.
- Avoid infinite damage loops.

### Suggested tuning
- Tether duration: 5–8 seconds.
- Echo damage: 15–25%.
- Max tethered targets: 3–5.
- Do not echo damage from another echo.

### Implementation notes
Use persistent entity data or a temporary capability/attachment if available.  
Damage source should be marked as echo damage to prevent recursion.

---

## 9. Scar of the Feeding Wake

### Effect
Player leaves a temporary fungal/blood trail while moving.

### Behavior
As the player moves:
- Periodically place temporary trail blocks or spawn area effects.
- Trail damages enemies.
- Trail may heal allies or feed owned morphlings/cradles.
- Trail expires quickly.

### Suggested tuning
- Spawn trail every 4–8 ticks while moving.
- Trail lifetime: 5–10 seconds.
- Small damage pulse to enemies.
- Small blood/feed transfer to owned morphlings.

### Implementation notes
Use player tick event.  
Avoid placing permanent blocks.  
Use temporary block/entity/area-effect system.

---

## Shared Implementation Requirements

## Scar Registration

Add each scar using the existing scar registration pattern.

Suggested IDs:
- `inverted_vein`
- `crawling_choir`
- `vein_orchard`
- `split_husk`
- `veiled_aperture`
- `blood_silence`
- `latching_vein`
- `hollow_horizon`
- `feeding_wake`

## Item / Data

For each scar:
- Add item/registry entry if scars are item-backed.
- Add localization.
- Add model/texture placeholder if needed.
- Add tooltip explaining:
    - Primary effect
    - Cost/drawback
    - Late-game requirement

## Gating

Require one of:
- Degree 7 Archon
- Degree 8 Apotheos
- Completion of Qliphoth Communion
- Fungal Dimension material

Recommended:
- Basic fungal scars require Degree 7.
- Stronger ones require Qliphoth Communion or Apotheos.

## Tags

Add useful tags:
- `hemomancy:fungal_scar`
- `hemomancy:scar_blood_silence_immune`
- `hemomancy:scar_latching_vein_immune`
- `hemomancy:veiled_aperture_visible`

## Safety / Balance

Prevent:
- Infinite damage recursion.
- Infinite manipulation echo recursion.
- Death-prevention loops.
- Permanent block spam from trail/orchard effects.
- Mob AI crashes from target manipulation.
- Client/server desync for hidden content rendering.

## Testing Checklist

For each scar:
- Equip scar.
- Confirm effect only works when equipped.
- Confirm effect does not work below required degree.
- Confirm effect persists through death only if existing scar system normally persists.
- Confirm multiplayer behavior does not affect unrelated players.
- Confirm server does not crash when scar owner logs out.
- Confirm client visual effects are optional and do not control gameplay.

## Suggested Implementation Order

1. Add scar registration/data/localization placeholders.
2. Implement `Hollow Horizon` first because it is likely simplest.
3. Implement `Inverted Vein`.
4. Implement `Blood Silence`.
5. Implement `Latching Vein`.
6. Implement `Crawling Choir`.
7. Implement `Split Husk`.
8. Implement `Vein Orchard`.
9. Implement `Feeding Wake`.
10. Implement `Veiled Aperture` last, because it may need new hidden-content infrastructure.

## Important

Do not rewrite the entire scar system unless absolutely necessary.

Extend existing scar/event patterns.

Keep each scar isolated in its own handler/helper where possible.