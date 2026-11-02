# Silent Archon Incorporeality Design

## User-approved direction

Silent Archon remains its own endgame armor set instead of upgrading the final Bloodlust sets. Its identity is refusal and incorporeal selfhood rather than another Bloodlust lineage expression.

## Gameplay contract

While the full four-piece Silent Archon set is worn:

- Physical attacks pass through the player and deal no damage.
- The player cannot deal mundane physical damage.
- Blood manipulations and living weapons remain valid offensive tools.
- Existing Silent Archon death refusal remains as the emergency failsafe for damage that is not covered by incorporeality.

## Radial ability

Register `hemomancy:silent_slipping` as an armor set ability.

- Display name: Silent Slipping.
- Display icon: Silent Archon Veil.
- Required set: Silent Archon helm, chestplate, leggings, boots.
- Activation: enter a short incorporeal slipping state.
- Duration: short, traversal-focused.
- Cooldown and blood cost: present, to prevent permanent wall flight.

## Silent Slipping behavior

During Silent Slipping:

- The player is put into a no-physics slipping state.
- The player gains short-range slow flight.
- Fall damage is cleared.
- The state ends when duration expires, the full set is removed, or the player activates the radial ability again.
- On exit, if the player is still intersecting blocks, the server attempts to place them at a nearby safe position.

## Balance notes

Silent Archon should feel powerful through refusal, not raw damage. Its combat weakness is losing normal weapon/bow melee pressure; it must lean on blood manipulations and living weapons to fight while benefiting from strong physical defense.
