# Heart-Node Sanctum (Section 3) — Design

Date: 2026-10-28. Source: `docs/Endgame Path Asymmetry …_V2.md`, "Heart-node sanctum", plus `Fungal Gardens Purpose …_V2.md`, "Apotheos: a home". Written during the user's overnight run, so every choice here is a ruling made on the user's behalf.

## Goal

An Apotheos plants a heart-node in the Gardens. It grows into a private sanctum that is their spawn and gives a bonus while they stand inside. When they die, they regrow there, and their gear waits in a fruiting body at the death point.

## Decisions

### D1. The heart-node
- **Block** `hemomancy:heart_node`: a pulsing crimson knot of flesh, with a block entity that records its owner.
- **Planting.** Only an Apotheos can plant one, only in the Gardens **dimension**, and only on `hemomancy:fungal_network_ground`.
  - Any other placement is refused with an action-bar line.
  - Each player has **one** heart-node. Planting a second one withers the first (it is removed, with no drop).
- **Recipe** (shapeless): 1 bleeding heart + 4 spore sacs + 1 conscious mass. The item says nothing about who may plant it; the placement gate decides.
- **Breaking.**
  - Only the owner can break it, and it drops itself.
  - Anyone else's break is cancelled.
  - It is explosion-proof.
- **Registry.** `HeartNodeSavedData` lives on the Gardens level and maps owner → pos. It is the single source of truth for "where is X's node".

### D2. The sanctum (the footprint and its growth)
- **Footprint.** A cylinder of horizontal radius **8**, reaching **6** blocks above and below the node.
- **Growth.** Every 100 ticks for 8 stages, the node grows one rib: a 3-tall `calcified_hyphae` pillar on a radius-6 ring, capped with `infected_cap`. Ribs grow only into air or replaceable plants, so the node never digs into terrain or a player's builds. The growth stage is stored on the block entity.
- **Privacy.** Inside the footprint, players other than the owner cannot break or place blocks. Creative players are exempt.
- **Bonus.** Every 40 ticks, an owner inside their own footprint gets Regeneration I for 60 ticks and **+4 spore load**, which doubles the dimension's gain rate.

### D3. Spawn
- Planting calls `setRespawnPosition(gardens, node.above(), forced = true)`.
- When the node is removed, the respawn is cleared if the owner is online. If they are offline, it is cleared on their next login, when `HeartNodeSavedData` no longer lists a node at their respawn point.
- The Covenant Throne stays the D6 progenitor respawn. The heart-node is a separate feature.

### D4. Regrowth and the fruiting-body cache
- **When it applies.** When an Apotheos who owns a heart-node dies without keepInventory, their drops go into a **fruiting-body cache** at the death point instead of scattering.
  - The cache is a block placed in the nearest free spot within 8 blocks, clamped inside the build height.
  - If no spot is found, the items drop as usual.
- **Block** `hemomancy:fruiting_body_cache`, with a block entity holding any number of stacks.
  - It never despawns.
  - It is explosion-proof.
  - Only the owner can open it (right-click): the stacks go to their inventory, the overflow drops, and the block is removed.
  - Anyone else's break is cancelled.
  - If the owner breaks it, the contents drop.
- **The handler.** It runs at `LivingDropsEvent` LOWEST priority, so earlier handlers (bound Pomes, morphling recovery, ScaleGrip) have already taken their items.

### D5. Out of scope
- **Molting** (re-shaping morphology or tendencies) stays a stub: the user's call.
- **Endgame reset:** removing the node on a Harbinger reset is left to the next login check. A reset clears the path, so the bonus stops (bonus and cache are gated on `isApotheos`).
