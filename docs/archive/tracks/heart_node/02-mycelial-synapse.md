# Track 2: Mycelial Synapse

**Status (2026-10-29): built, uncommitted.** Built on Track 1's real API (no stubs). What later tracks code against:
- `MycelialSynapseBlock` (`FACING` six-way, `WITHERED`), `MycelialSynapseBlockEntity` (`owner()`, `linkedHeart()`, `active(level)`, both synced in the update tag). `MycelialSynapseBlock.witherAll(level, positions)` / `wither(level, pos)`.
- `HeartNodeFootprint.connectedSynapses()`: the synapses that still chain back to the heart, in raised order (for Track 4's link threads).
- The stake's special path is now shared: `rite/structure/BloodStructureFormation` (`pos`, `place`, `fallbackDrops`). `HematicStakeBlock.Formation` and `MycelialSynapseBlock.Formation` implement it; `ProjectionCraftMatch.formation()` replaced `stakeFormation()`. `PendingBloodCraftManager.pendingFormations()` lists formations still closing their ring.
- Model: **TODO Track 5.** `models/block/mycelial_synapse.json` and `_withered.json` are one-cube stand-ins (a 6×14×6 stalk on the sporite / calcified-hyphae textures), rotated by the blockstate like an end rod. Replace them with the generator's model; keep `facing` × `withered`.
- Review: `SynapseScenes`, called from `HeartNodeClientReview` (captures `heart-node-3` … `-7`). Launched 2026-10-29: all 18 checks PASS (Track 1's 8 included, its first run). Drive it with `{"op":"create","name":"HeartNodeReview","seed":20261029}` (create needs a seed).
- **Fixed on the way:** `BloodStructureFeedManager.sendHint` throttled with `gameTime - Long.MIN_VALUE`, which overflows negative, so no Blood Structure refusal hint had ever reached the action bar. It now reads a missing entry as "never hinted".
- GameTests: `MycelialSynapseGameTests`. The GameTest server builds only the overworld, so they run past the Apotheos and dimension checks: the refusals through `MycelialSynapseBlock.sanctumRefusal` (the part of `findFormation` after them), placement through a real `PendingBloodCraftManager.PendingCraft`. The full feed in the Gardens (blood cost, catalyst, action-bar refusal) is proved only by the review client.

**Decisions and deviations (review these):**
- **Cut-off synapses keep working.** `FoundingFaneSavedData.removeStake` forgets only that stake and the rest stay, so per this doc the synapse does the same; `connectedSynapses()` reports, and prunes nothing. The cost: a player can raise a chain, break the middle to get budget back, and keep raising off the stranded outer lobe (stakes allow the same leapfrog). To prune instead, have `MycelialSynapseBlock.onRemove` wither everything not in `connectedSynapses()` after the removal. One method.
- **It has a hidden item, not no item.** A Blood Structure recipe needs an item result (the codec refuses air, and JEI and the late-refusal fallback both read it), so `mycelial_synapse` is in `HIDDEN_BLOCK_ITEMS`, not `ITEMLESS_BLOCKS`. `getStateForPlacement` refuses every item placement (`raise_only`), so the item can't make a record-less synapse that drops its inputs.
- **No support rule.** The iron itself becomes the synapse, so it needs nothing to stand on and never pops off. That keeps "it never deletes itself".
- **`withered_heart` is not built.** A synapse can be raised off a node that is still maturing.
- **Budget race:** findFormation counts this owner's synapses still in `PendingBloodCraftManager`, so a second feed is refused at its next tick (before that tick's blood). Blood it took before the first ring closed is spent. `Formation.place` re-runs `addSynapse` (chain, budget, foreign) and on a refusal drops the iron and crystal.
- **No matcher change.** `PatternOrigins.scanAround` already tries every forwards/up pair, so the 1×2 pattern matches with the crystal on all six faces (`MycelialSynapseGameTests.synapsePatternMatchesOnAllSixFaces`); no `any_orientation` flag.
- **Liber:** there is no Apotheos section in the book. The synapse is on the Rites & Crafts map (Floorless Crafts, linked from the Hematic Stake) and in JEI's Blood Structures category through its recipe.

**Depends on:** Track 1's `HeartNodeFootprint.canAddSynapse`, `HeartNodeSavedData.addSynapse` / `removeSynapse` and the envelope sync. Code against those signatures; if Track 1 hasn't landed, stub them in a branch-local way and say so in the ledger.

**Goal:** the Apotheos grows their sanctum outward by raising **Mycelial Synapses**, the heart-node's equivalent of Hematic Stakes. A synapse is a Blood Structure. It is raised from a block of hematic iron with a Sporite Crystal set against it on **any** face, so it can sit on a floor, hang from a ceiling or jut from a wall. Each one adds a lobe to its owner's envelope and nothing to anyone else's.

## Current state (what to mirror, what not to)
- `hematic_stake` is a Blood Structure (`data/hemomancy/recipe/blood_structure/hematic_stake.json`: `bloodCost` 100, `required_degree` 5, held `sanguine_formation`, hit `hematic_iron_bars`, pattern crystal + bars). `BloodStructureCraftingHelper.findProjectionCraftMatch` gives it a special path: `findStakeFormation` → `HematicStakeBlock.findFormation(level, player, barPos, crystalPos)` works out the stake's facing and face from where the crystal sits, checks `canPlaceStake` (progenitor, envelope connection, budget), and `placeFormation` places the stake and records it in `FoundingFaneSavedData`.
- `HematicStakeBlock` has `FACING` + `ATTACH_FACE` (floor, wall, ceiling), is passable, owner-instant-mined, and is removed when the heart breaks.
- Mirror the special path, the face handling and the removal-with-heart. Don't mirror the bloodline: a synapse has one owner and no members.

## Target

### The block
**`mycelial_synapse`** (`MycelialSynapseBlock` + `MycelialSynapseBlockEntity`), in `common/block/harbinger/functional` and `common/tile/harbinger/functional`.
- `FACING` over all six directions (`DirectionalBlock.FACING`): the direction from the iron to where the Sporite Crystal stood. The synapse grows toward it, so on a floor it points up, on a ceiling down, on a wall outward.
- No collision past its stalk. The light-friendly, non-occluding shape follows the stake's. Light level 4, a dim pulse.
- The BE stores `owner` and `linkedHeart` (the heart position at the time it was raised) and exposes `active()` (its record is in the owner's entry). It syncs both for Track 4's link threads and Track 5's renderer.
- **Inert state**: a synapse whose record is gone shows as withered (a block state `WITHERED` or BE flag), stops adding range and drops itself as a hematic iron block plus a Sporite Crystal when broken. This happens when its node is broken or replanted elsewhere, or when the synapse was cut off. It never deletes itself, which avoids invisible block loss in unloaded chunks. When the owner loads the chunk, `onLoad` checks the record and sets the state.
- Breaking: the owner breaks it instantly and gets the inputs back (hematic iron block + Sporite Crystal; no blood refunded). Strangers can't break it (envelope privacy, Track 1). It is explosion-proof while active.
- Item-less (it only exists as the crafted result): add it to `BlockInit.ITEMLESS_BLOCKS`, the `RegistrationGameTests` list and `registry-ids.txt`. Model: a placeholder until Track 5's generator lands. No hand-written JSON beyond a one-cube stand-in marked TODO for Track 5 in the ledger.

### The recipe
`data/hemomancy/recipe/blood_structure/mycelial_synapse.json`:
- `type` `hemomancy:blood_structure_recipe`, `required_degree` 8, `bloodCost` 120 (a little above the stake: the Apotheos pays in blood, and synapses are fewer).
- `heldItem` `hemomancy:sanguine_formation`, `hitBlock` `hemomancy:hematic_iron_block`.
- `pattern` `[["S", "I"]]`, key `S` → `hemomancy:sporite_crystal`, `I` → `hemomancy:hematic_iron_block`.
- **Any orientation.** Confirm what the pattern matcher accepts. Vanilla `BlockPattern.find` tries every forward/up pair, but check that `MultiblockPattern` → `BlockPattern` and `getVisibleMatchPositions` keep the vertical cases. Write a GameTest for all six placements of the crystal around the iron (above, below, four sides) before changing the matcher. If any fail, add a recipe flag `"any_orientation": true` read by `BloodStructureRecipeSerializer`, and widen the search only for flagged recipes so no existing structure starts matching sideways.

### The special path
Like the stake, the synapse is not placed by the generic completion:
- `BloodStructureCraftingHelper`: when the result is `mycelial_synapse`, call `MycelialSynapseBlock.findFormation(level, player, ironPos, crystalPos)`. It returns `Formation(owner, pos, facing)` or a refusal component.
- Refusals, checked before any blood is fed, each with its own lang key under `block.hemomancy.mycelial_synapse.*`:
  - `not_apotheos`
  - `not_in_gardens` (the Gardens dimension only, like the node)
  - `no_heart` (the player has no node in this dimension)
  - `unconnected` (the new lobe would not touch the heart lobe or another of this owner's synapse lobes)
  - `budget` (`SYNAPSE_BUDGET` spent)
  - `foreign` (the lobe would touch another owner's envelope)
  - `withered_heart` (the node is still maturing below stage 1; optional, decide in review)
- `placeFormation`: the iron becomes the synapse (facing toward the crystal's old cell), the crystal is consumed, the record is added through `HeartNodeSavedData.addSynapse`, and the envelope resyncs. The re-check right before placing guards against the budget or connection changing during the feed (`BloodStructureFeedManager` completion lock).
- The JEI page and Liber entry show the synapse among the Blood Structures (it is floorless, so it goes on the pinned floorless roster; keep `WorkingCraftRosterDataTest` green and add it to the roster list with a one-line reason).

### Cut-off lobes
When a synapse is removed, a lobe further out may lose its chain back to the heart. Match what `FoundingFaneSavedData` does for a stake chain:
- If stakes keep working when cut off, keep that rule. Otherwise, after a removal, walk the chain from the heart, and every synapse no longer connected goes inert (record removed, block state set when loaded). Re-raising the gap restores nothing automatically; the player re-raises the outer synapses.
- Write the rule into `HeartNodeFootprint` as `connectedSynapses()`, tested pure.

### Teaching
- Liber, Apotheos section: "Mycelial Synapse", covering the recipe, the any-face rule, the personal rule and the budget.
- HR §5.6d: the synapse paragraph, the numbers, the special path.
- The Blood Structure guide page lists the synapse with its orientation note.

## Tasks
1. `MycelialSynapseBlock` / BE, six-way facing, inert state, owner break with refund; registry and fixture updates.
2. Six-orientation GameTest against the matcher; matcher flag only if it fails.
3. Recipe JSON; special path, refusals and lang; completion re-check.
4. `connectedSynapses` and cut-off handling, tested pure and with one GameTest (remove a middle synapse, the outer one goes inert).
5. Roster, JEI and Liber entries; HR.
6. Review client: raise a floor synapse, a wall synapse and a ceiling synapse in a chain; capture each; capture a refused foreign placement message.

## Acceptance
- A synapse can be raised with the crystal on any of the six faces of the iron, and it points toward where the crystal was.
- It only extends its owner's envelope. A second Apotheos can't raise one that touches someone else's envelope and gets the `foreign` message before paying blood.
- Breaking the node withers every synapse of that owner. Breaking a synapse returns its inputs.
- No synapse can be raised past the budget, and the budget can't be bypassed by a feed that started before another synapse finished.
