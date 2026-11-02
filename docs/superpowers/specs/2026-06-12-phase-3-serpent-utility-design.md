# Phase 3 Serpent Utility Design

## Goal
Give `serpent_scale` two unique utility uses without opening a large armor, AI, or machine system. Phase 3 adds field-control and death-risk utility: `constrictor_cord` and `scale_grip`.

## Scope
This phase includes:
- `constrictor_cord`: throwable consumable that temporarily binds a target in place.
- `constricted`: short harmful status effect used by Constrictor Cords.
- `scale_grip`: single-use inventory protection token consumed on player death.
- Recipes, lang, item models, textures, docs, and focused sentinel tests.

This phase does not include:
- Tendon Line traversal. That is Phase 4.
- Curor Lens diagnostics. That is Phase 5.
- New armor/tool modifier infrastructure.
- Permanent item enchantments or broad inventory-retention rewrites.

## Constrictor Cord
`constrictor_cord` is a throwable utility item crafted from `serpent_scale`, `puppeteering_thread`, and an organic/blood binder already present in the mod.

On use, the item spawns a small projectile. On entity hit, the projectile applies `constricted` to one living target, then disappears. On block hit, it disappears with a small particle/sound burst. It must not deal damage, ignite, poison, pull, or launch the target.

`constricted` is field control:
- Duration: 100 ticks.
- Movement is heavily reduced through a standard movement-speed attribute modifier.
- Jumping/AI/pathing are not rewritten.
- Boss-class entities receive half duration rather than being skipped outright.

The intent is "pin this enemy long enough to reposition," not "stun-lock combat."

## Scale Grip
`scale_grip` is a single-use inventory token crafted from `serpent_scale` and a binder. It protects one stack when the owning player dies, then consumes itself.

Death behavior:
- If the player has at least one `scale_grip`, find one eligible non-empty inventory stack that would normally drop.
- Prefer hotbar/main inventory before armor/offhand, so the token protects carried tools/materials rather than fighting armor systems.
- Move that protected stack into the respawned player's inventory.
- Remove exactly one `scale_grip`.
- Do nothing when gamerule `keepInventory` is true.
- Do not protect itself as the saved stack.

Implementation avoids arbitrary NBT mutation on protected items. The item is moved as an `ItemStack` copy through a small pending-return map keyed by player UUID, then restored on respawn.

## Architecture
Add small rule helpers so behavior is testable without a running Minecraft world:
- `ConstrictorCordRules`: duration, velocity, cooldown, and movement multiplier constants.
- `ScaleGripRules`: selection rules for which inventory slot is protected.

Runtime classes:
- `ConstrictorCordItem` spawns the projectile.
- `ConstrictorCordEntity` applies the effect on hit.
- `ConstrictedEffect` owns the movement modifier.
- `ScaleGripEvents` listens to death/respawn events and handles one-stack retention.

Registries:
- Add items in `ItemInit`.
- Add projectile in `EntityInit`.
- Add effect in `EffectInit`.

## Assets And Data
Add:
- `assets/hemomancy/models/item/constrictor_cord.json`
- `assets/hemomancy/models/item/scale_grip.json`
- `assets/hemomancy/textures/item/constrictor_cord.png`
- `assets/hemomancy/textures/item/scale_grip.png`
- `assets/hemomancy/textures/mob_effect/constricted.png`
- recipes for both items in `data/hemomancy/recipe/`
- language entries for both items and the effect

## Testing
Add plain Java sentinel tests:
- `ConstrictorCordRulesTest`
  - duration is short.
  - movement multiplier is strong but nonzero.
  - projectile velocity/cooldown are sane.
- `ScaleGripRulesTest`
  - selects the first eligible normal inventory stack.
  - never selects `scale_grip` itself.
  - returns no selection for empty inventory.
- `PhaseThreeSerpentUtilityResourceTest`
  - verifies recipes, models, textures, and effect icon exist.

Verification command:
- `./gradlew.bat build --no-daemon`
- explicit `java -cp ...` runs for the new sentinel tests.

## Documentation
Update `docs/HEMOMANCY_REFERENCE.md` material utility notes:
- `serpent_scale` now crafts Constrictor Cords and Scale Grip.
- Constrictor Cords are temporary field-control tools, not damage items.
- Scale Grip is a one-stack death-retention token and consumes itself.
