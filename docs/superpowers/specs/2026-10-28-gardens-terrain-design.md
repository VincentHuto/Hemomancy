# Gardens Terrain, Rendering Pools & Hunter Fungi (Section 2c) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry …_V2.md`, the "Shared systems, split outcomes" rows *Terrain* ("Caps extend into bridges" / "Gills snap shut, puffballs burst") and *Mycelium network* ("hunter fungi converge"), and Privileges ("Rendering pools: break items down into alchemical compounds"). `Fungal Gardens Purpose & Mausoleum Structure_V2.md` puts rendering pools in the Fringe. Builds on Section 2b (`GardensNetwork`, the responders tag, `ApotheosBodyEvents.isApotheos`, `FungalGardensPlaces`).

## Goal

The Gardens' ground answers the Apotheos and closes against anyone else: caps grow into bridges under an Apotheos's hand, gills bite and shut on a stranger, rendering pools break an Apotheos's offerings into enzymes, and hunter fungi come from far off when the network hears a footstep.

## Decisions

### D1. Rendering pools
- **Fluid.** A new fluid, `hemomancy:rendering_bile`, has a source, a flowing form and the liquid block `rendering_bile_block`.
  - It has no bucket, and it is viscous (density 1300, viscosity 4000, light 2).
  - It uses its own still, flow and overlay textures, recoloured from morphic nectar to an ochre bile.
- **Worldgen.** The `rendering_pool` feature is the morphic pool's shape, rimmed with `calcified_hyphae` instead of sporite.
  - It is placed in the Fringe biomes: mycelial meadows, fungal isles, sporecrown thicket and drifting mycelium.
  - Its rarity is 1 in 60.
  - `MorphicPoolFeature` takes the fluid and rim blocks as constructor suppliers, so both pools share one class.
- **Rendering.** An item entity in rendering bile is broken down when all of the following hold:
  - it was thrown by an Apotheos (`ItemEntity#getOwner()`);
  - a `hemomancy:rendering` recipe matches it.
- **What happens:**
  - After the recipe's `render_time` (default 100 ticks), the stack is consumed.
  - For each item in the stack, each result rolls its `chance` independently.
  - The results that land spawn as item entities.
  - Pale spore motes pulse every 10 ticks; completion gives a crimson spore puff and a wet squelch.
- **Anyone else's items** (thrown by a non-Apotheos, dropped on death, or with no thrower) float untouched. Nothing is digested, so no one loses a death drop to a pool.
- **Recipe type** `hemomancy:rendering`: `input` (ingredient), `results` (a list of `{id, count, chance}`, chance 0–1, default 1), `render_time` (int, default 100).
- **Starting recipes** (one per tendency enzyme, plus spore sac):

| Input | Results |
| --- | --- |
| spore_sac | recycled_enzyme ×1 |
| rotten_flesh | vivacious_enzyme 25% |
| bone | ruinous_enzyme 25%, bone_meal ×2 |
| spider_eye | neurotic_enzyme 50% |
| hematic_iron_scrap | ferric_enzyme 50% |
| blaze_powder | fervent_enzyme 50% |
| packed_ice | frigid_enzyme 50% |
| glowstone_dust | incandescent_enzyme 25% |
| ink_sac | umbral_enzyme 25% |

### D2. Cap bridges
- **Trigger.** An Apotheos, alive and not a spectator, right-clicks an `hemomancy:bridging_caps` block (`infected_cap`, `fruiting_infected_cap`) with an empty main hand, in the Gardens **dimension**.
- **Direction.** The clicked face if it is horizontal, otherwise the player's horizontal facing.
- **Growth.**
  - A `cap_bridge` block grows from the cap outward, one block every 2 ticks, up to **16** blocks.
  - It only grows into air and stops at the first obstruction.
  - The click is consumed (arm swing), with the fungus place sound.
- **Block** `cap_bridge`:
  - itemless and dropless, a top-half slab of cap flesh flush with the cap's top;
  - state: `facing` (horizontal), `reach` (0–16, blocks still to grow) and `grown`;
  - anyone can stand on it.
- **Withering.**
  - A segment withers **600** ticks after it grows.
  - If an Apotheos stands within 3 blocks, it waits another **100** ticks.
  - Withering removes it with a pale spore puff.
- Bridge blocks are not bridging caps, so a bridge cannot be extended from its end.

### D3. Gills
- **Block** `fungal_gill`: hanging pale lamellae.
  - State: `open` (default true).
  - Open: a cross model with cutout and no collision.
  - Shut: a full cube of closed flesh.
  - It survives only under another gill or a block whose bottom face is sturdy.
  - It has an item, and drops itself only to shears or Silk Touch.
- **Snap.** When a living, non-creative, non-spectator player who is **not** an Apotheos touches an open gill:
  - that gill and every open gill connected to it (6-neighbour, at most **24**) snap shut;
  - the player takes **3** `hemomancy:gill_bite` damage and is knocked back 0.6 away from the gill.
  - Gills react wherever they are: they generate only in the Gardens, and a placed gill is an Apotheos-only curtain anywhere.
- **Reopen.** Shut gills reopen after **100** ticks. If a non-Apotheos player is within 2 blocks, they wait another 20.
- **Who passes.** An Apotheos passes freely, and mobs never trigger gills.
- **Worldgen.** The `gill_curtain` feature is a vanilla `block_column` of 2–4 gills hung downward from a solid ceiling. It is found by an upward environment scan of up to 12 blocks, so it lands under canopy caps and in cave roofs. It is placed in meadows, thicket, drifting mycelium and mycelial depths (decoration step 9, count 24).

### D4. Hunter fungi
- **Entity** `hemomancy:hunter_fungus` (MONSTER, 0.9×0.9): a low four-legged stalker with a cap for a head and a gill-maw beneath it.
  - 26 health, 5 attack, 0.27 speed, 48 follow range, 2 armor, 0.3 knockback resistance.
  - Drops spore_sac 1–2. It has a blood profile (`hemomancy:fungal`) and a spawn egg.
- **Targets.**
  - It hunts any player who is not an Apotheos and not creative or spectator.
  - It strikes back at whoever hurts it.
  - It is in `hemomancy:gardens_network_responders`.
- **Converge.** When the network alarms on a player, every hunter fungus within **64** blocks that has no target responds:
  - **within 16 blocks:** it targets the player;
  - **further out:** it remembers the player's position for **600** ticks and walks there at 1.2× speed, then searches. It forgets once it arrives within 3 blocks.
- **Spawns.** Natural spawns in the dimension: meadows (6, 1–2), thicket (6, 1–2), drifting mycelium (5, 1–2), hyphal spires (5, 1–2), depths (4, 1–1).
- **Visuals.** A hand-written model with a generated 64×32 texture.

## Out of scope
- A JEI category for rendering.
- Hunter-fungus convergence on a possessed facsimile (Silentium possession, Section 5).
- Withering bridges under a standing non-Apotheos (bridges are open to all).
