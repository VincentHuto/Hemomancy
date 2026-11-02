# Sanguine Condenser — design

Date: 2026-10-20. Status: draft for approval.

## Intent and why

Today Blood Projection condenses a Sanguine Formation on almost any solid block that has no block entity (`SanguineFormationProjectionHandler`, the last fallback in `BloodProjectionItem.performProjection`). Blood infusion runs earlier in that chain. A new D1 player who projects at the nearest stone hoping for a formation instead turns it into Venous Stone, or whatever that block's infusion recipe makes. The Vicar's first chapter lists "project a Sanguine Formation" and "project into stone for Venous Stone" side by side, which makes the clash worse.

The fix gives formations one dedicated, deliberate home: **bare blocks no longer make formations.** The player projects blood into a **vanilla cauldron**. The cauldron fills with blood and becomes a stage-0 **Sanguine Condenser**, which crystallizes formations on its own. Later Working rites grow that cauldron, in place, into larger and faster Condensers. Projecting at stone now only ever means infusion, and projecting into a cauldron only ever means collecting blood.

## Goals

- Formations come only from a Sanguine Condenser. The existing non-projection sources stay: the shapeless recipe, loot and rewards.
- The first Condenser costs only a vanilla cauldron (7 iron). That keeps the Vicar's D1 chapter in any order and needs no gift system.
- The Condenser is the seventh upgradeable station under `docs/STATION_UPGRADES.md`, with two in-place Working-floor upgrades that make it larger, faster and more productive.
- The loop is fill-and-leave: projecting fills an internal pool, and the Condenser works passively from it.

## Non-goals

- No change to blood infusion, blood structures or other projection targets.
- No new formation sources, and no change to the formation item.
- No automation beyond letting hoppers pull finished formations.
- No change to Unstained content. The Stillwater Condenser is unrelated.
- **Pallid Retort** (a D2 blood structure whose target block is a cauldron): out of scope. The user plans to rework or remove it in a later Unstained/Artificer pass. Structure feeding already runs first, so its craft still wins when the full pattern and Pale Distillate are present.
- **Resonant Forge Working craft** (uses a plain cauldron in its structure): out of scope. A converted cauldron reverts to vanilla once emptied (see below), and the user accepts the rare edge case.

## Anti-goals (a failure even if it works)

- Any path where projecting at an ordinary block still produces formations.
- Projecting at a Condenser running an infusion recipe, or projecting at stone producing a formation.
- A Condenser that silently swallows blood with no visible feedback.
- Vanilla particle clouds as the main visual. See `docs/MANIPULATION_VISUALS.md` and the custom-particle rule.
- Copper, brass, bronze or silver accents on the upgraded models. Use hematic iron, bone and crimson lacquer.
- A station upgrade that bypasses the shared contract: a private ceremony, a private claim ledger, or a crafted kit skipping server eligibility.
- Disturbing other vanilla cauldron uses (water, lava, powder snow, buckets, dripstone filling) for players who never project into one.

## Decided with the user

- Bare-block formation projection goes away. Venous Stone and Blood Stained Stone lose their faster-formation-surface role.
- **Stage 0 is a vanilla cauldron filled with projected blood.** The user chose this over an earlier idea: a small crafted platter (Hematic Iron + bowl), which is now dropped.
- The Condenser is upgraded later through Working-floor rites to be larger, faster and more productive.
- Projecting fills a pool, and the Condenser crystallizes formations on its own. Finished formations wait until collected, and projecting at a full Condenser does nothing.
- The Vicar's lesson becomes "fill a cauldron with Blood Projection and collect a formation it condenses."
- The Condenser is the seventh station under the station-upgrade contract:
  - Stage 0 from D1.
  - Stage 1 at D3 on the Lesser Working floor.
  - Stage 2 at D5 on the Greater Working floor.
  - The Vicar is the free-kit teacher, and personal-use proofs follow the contract.

## Calls I made (user can override)

- Upgraded stages **grow out of the cauldron**, not into a platter, because "larger" should read as larger.
- An emptied stage-0 Condenser reverts to a vanilla cauldron.
- The Vicar's D1 proof requires that the collector projected into that Condenser themselves.
- All names, numbers and offerings marked *(my call)* below.

## The block

**Identity.** `hemomancy:sanguine_condenser` is one block with a block entity and a renderer. It is registered through `BlockInit` / `BlockEntityInit` / `ClientEvents` like `gourdvine_tap`. Blockstate properties are `FACING` (horizontal, needed for the station rite's forward direction) and `StationTierProperty.STAGE` (0–2).

**Stage 0: converting a cauldron.**
- Blood Projection aimed at an empty `minecraft:cauldron` replaces it with `sanguine_condenser[stage=0]` and begins filling it. It does this only when:
  - the player's blood is active, and
  - no blood-structure recipe claimed the projection first.
- `FACING` is set opposite the player's look direction.
- Water, lava and powder-snow cauldrons are ignored, and so are non-projection interactions.
- Stage 0 uses the vanilla cauldron's shape and shell texture. Its blood surface shows at the vanilla fill heights.
- It is a mod block, not a vanilla `LayeredCauldronBlock` variant. It does not take part in vanilla `CauldronInteraction` maps, so buckets, bottles, rain and dripstone do nothing to it *(builder's call: extend `AbstractCauldronBlock` or not, provided these hold)*.
- **Revert.** A stage-0 Condenser turns back into a plain vanilla cauldron when it holds no blood, has nothing in progress, has no formations waiting and is not rite-locked.
- **Breaking stage 0** drops a vanilla cauldron plus any formations. The blood is lost. Stage 0 must never drop a `sanguine_condenser` item.

**Stages 1–2.**
- These only come from the upgrade rites, or from Creative direct-kit use under the contract.
- Breaking drops `sanguine_condenser` with its stage (loot `copy_state` "stage") plus formations. The blood is lost.
- The loot table branches on stage: stage 0 drops a cauldron, stages 1 and 2 drop the block itself. Adjust `StationTierPersistenceResourceTest` to accept this form.
- Placing a stage 1–2 item restores that stage, and these stages do not revert when empty.

**Blood pool.** The block entity implements `IBloodReservoir`, using the `BLOCK_BLOOD_VOLUME` attachment set up as in `GourdvineTapBlockEntity.ensureReservoirConfigured`. Max volume follows the installed stage and is re-applied on load and after an upgrade. An upgrade preserves the current amount.

**Finished formations.** These are an internal inventory whose capacity depends on stage. Expose an extract-only `ItemHandler.BLOCK` capability so hoppers and pipes can pull formations; nothing can be inserted.

**Condensing loop.**
- While the pool holds at least the stage's per-formation cost and there is room, a progress timer advances each server tick.
- When the timer completes, the Condenser drains the cost and rolls one outcome:
  - **collapse:** blood lost, no item
  - **success:** one formation
  - **success plus bonus:** two formations, if there is room
- It pauses, keeping its progress, when blood or room runs out.
- Persist progress, pool, items, owner and contributors in NBT. Sync to the client for rendering (update tag/packet, as the Gourdvine Tap does).

**Starting numbers** *(my call; tune in play)*:

| Stage | Display name | Pool (mL) | Cost per formation (mL) | Time per formation | Holds | Collapse | Bonus |
|---|---|---|---|---|---|---|---|
| 0 | Sanguine Cauldron | 300 | 150 | 200 t (10 s) | 1 | `max(0, 0.25 − 0.05 × crystallization level)` | — |
| 1 | Latticed Condenser | 750 | 125 | 120 t (6 s) | 4 | 0 | — |
| 2 | Druse Condenser | 1500 | 100 | 60 t (3 s) | 8 | 0 | 25% |

- **Collapse skill source.** Collapse uses the Sanguine Crystallization level of the block's **owner**: the player who converted the cauldron, stored by UUID. If the owner is offline, use their last cached level. If there is no owner, use 0.
- **Rules class.** Put the formulas in a pure `SanguineCondenserRules` with JVM tests. It replaces `SanguineFormationProjectionRules`.

**Interaction.**
- **Blood Projection at a Condenser** fills the pool at the tile transfer rate.
  - Make this, and the cauldron conversion, an explicit early-returning branch in `BloodProjectionItem.performProjection`. Place it **after** the Cardinal rite and structure-feed handlers, so station-rite circuits and blood-structure crafts still work, and **before** blood infusion and the generic block-entity branch.
  - Projecting at a full Condenser spends nothing and shows a short actionbar line ("The cauldron can hold no more.").
  - Each accepted projection adds the projector to the block entity's contributor set.
- **Collecting.** Right-clicking with an empty hand moves every finished formation into the player's inventory; any other item except Blood Projection or the station kit does the same.
  - Count actual insertion only. Whatever doesn't fit stays put, and Creative discard does not count.
  - Personal collection records the station `CONDENSE` usage (any stage) and `CONDENSE_LATTICED` (stage 1+). This follows the contract's shared-machine rule.
- **Blood Absorption** may draw from the pool like other reservoirs. It is not a proof, and draining stage 0 to empty can trigger the revert.

**Infusion and structure safety.** Add a test asserting that no `blood_infusion` recipe targets `minecraft:cauldron` or `sanguine_condenser`, and that no blood-structure recipe targets `sanguine_condenser`. Pallid Retort's cauldron target is the known, accepted exception.

## Removing bare-block formation projection

- Delete the fallback call in `BloodProjectionItem`. Remove:
  - `SanguineFormationProjectionHandler`, along with its `tick` hook in the rite events and its `clear` hook in `TransientServerStateEvents`
  - `SanguineFormationProjectionRules`
  - `PacketSanguineFormationProjection` and its `PacketHandler` registration
  - `ActiveSanguineFormationProjectionClientData`
- **Keep:**
  - `SanguineProjectionTargeting`, the shared ray used by many systems.
  - `SanguineFormationProjectionRenderer.renderSphere`, used by the Ichorian sigil renderers. If the rest of that renderer goes, move `renderSphere` to a small shared helper and repoint both callers. The Condenser's in-progress visual should reuse it.
- Delete the `hemomancy:sanguine_formation_projectors` block tag.
- In-progress formations were never saved (they lived in a static map), so no save migration is needed.
- Update or remove the tests that pinned the old path:
  - `SanguineFormationProjectionSourceTest`
  - `SanguineFormationProjectionRulesTest`
  - `SanguineFormationProjectionRendererTest`, only if its subject goes
  - `TransientServerStateLifecycleTest`:25
  - `CellHandItemRendererParticleOriginTest`:20/68
  - `attunedProjectorTarget` in `HarbingerJourneyFixtureGameTests`
  - the journey harness `FORMATION_PROJECTED` fixtures (`HemoJourneyFixtures`, `HarbingerJourneyAutomation.projectFormation`, `HemoJourneyChecks.verifyFormation`), which should now place a cauldron, project, wait or advance ticks, and collect

## Station upgrades

The Condenser follows `docs/STATION_UPGRADES.md` in full:
- installed stage vs personal eligibility vs free-kit claim
- upgrade in place
- escrow and recovery
- Creative direct-kit use
- crafted kits equal to gifted ones
- server-side eligibility

The rite's subject is `hemomancy:sanguine_condenser` at the source stage, so a stage-0 cauldron must already be converted. An upgrade preserves blood, formations, owner and contributors.

| Tier | Degree | Floor | Kit item *(my call)* | Rite path *(my call)* | Required personal use | Circuits |
|---|---|---|---|---|---|---|
| Stage 1 | D3 | `working_lesser` | `clotting_lattice` (**Clotting Lattice**) | `cardinal_rite/first_clotting` | Collect a condensed formation from any Condenser (`CONDENSE`) | 2 × 50 mL |
| Stage 2 | D5 | `working_greater` | `druse_heart` (**Druse Heart**) | `cardinal_rite/druse_crystallization` | Collect a condensed formation from a Stage 1+ Condenser (`CONDENSE_LATTICED`) | 3 × 50 mL |

Kit and stage names avoid "condensation" and "Hematic Condenser", which already belong to the Alembic's first upgrade and the `first_condensation` rite.

**Offerings** *(my call; the builder should confirm each is obtainable by that degree)*:
- **First Clotting:** `clotting_lattice`, `hematic_iron_scrap`, `befouling_ash_trail`, `venous_stone`, `blood_stained_stone`, `sanguine_formation`
- **Druse Crystallization:** `druse_heart`, `amethyst_shard`, `blood_crystal_shard`, `sanguine_glass`, `hematic_iron_block`, `suspended_blood_crystal`

Kit repeat recipes are shaped, and each recipe's id matches its item. The Clotting Lattice is a natural home for the "hematic iron" feel of the dropped platter recipe: scraps and a formation.

**Wiring the seventh station:**
- Add `UpgradeStation.CONDENSER` with `blockPath` `sanguine_condenser`. Match the seat distance of the single-block stations unless the floor geometry demands otherwise.
- Add two tiers to `StationUpgradeCatalog`, with dialogue keys under the Vicar.
- `StationUpgradeTier.lesser()` is hard-coded to ALEMBIC tier 1. Generalize it to a catalog field or rule instead of adding a second special case.
- Add a Vicar case to the exhaustive switches in `StationUpgradeDialogue.allowedSpeaker` and `StationUpgradeTooltips`. Hook the Vicar's tree with `StationUpgradeDialogue.append(CONDENSER, ...)`, as the Alchemist and Mnemonist trees do.
- The block entity implements `UpgradeableStation`.
- Update every test that enumerates stations:
  - `StationUpgradeCatalogTest`: the total becomes 14
  - `StationTierPersistenceResourceTest`
  - `StationUpgradeRecipeResourceTest`
  - `StationUpgradeRiteResourceTest`
  - `CardinalRiteProgressionResourceTest`
  - the station gametests (`StationUpgradeActivationGameTests`, `StationUpgradeGameTests`, and the `CentrifugeUpgradeGameTests` station array)
- Keep these green:
  - `CardinalRitePurposeDataTest`: purpose `making`, Working floors
  - `WorkingCraftRosterDataTest`
  - `RecipeMapPurposeGroupDataTest`: file both rites in the making group of `HarbingerRecipeMapDefinitions`
- JEI lists the rites automatically by purpose. Add a JEI info page for the cauldron conversion.

## Vicar chapter and teaching

- **The formation proof moves.** `FirstBloodcraftAssignment.recordFormation(player)` now fires on personal collection of a condensed formation from a Condenser. It still only counts at D1, through the existing `mark()` gate. *(My call)* It also requires that the collector is in that Condenser's contributor set, so the lesson still teaches projection.
- **Old saves.** The ledger key `first_bloodcraft_formation` and persisted progress are unchanged, so saves that already recorded the proof keep it.
- **Rewrite the teaching text** as "project blood into a cauldron; it will condense a formation; collect it". This covers the ledger step `project_formation`, the Vicar's `projection_hint` and `vicar.blood_crafting.projection`, the Hermit neophyte hint, and the line in the Liber's `degrees.json`. Each hint also says outright that projecting into ordinary stone makes Venous Stone, which turns the old confusion into the Venous Stone lesson.
- **Rename the option** "How do I make the two projections?" to match, for example "How do I condense a formation, and make Venous Stone?"
- **New Liber and inquiry content.** Add a Liber entry for the Condenser in the bloodcraft chapter, and entries for both rites through `LiberEntryDefinitions.forRite`. Add a Vicar item inquiry for `sanguine_condenser`, and update the Alchemist's `sanguine_formation` inquiry to name the cauldron route.
- **Skill text** (lang 4469): "Steadies a Sanguine Cauldron: reduces its collapse chance by 5% per level."
- **Materials atlas.** Add a Condenser node to `MaterialsData` / `MaterialAtlasSpec`, gated at D1 next to the formation.

## Visuals *(builder's call within these rules)*

- **Stage 0:** the vanilla cauldron shell. The blood surface has the dark-crimson wavy look of the `ScryingPodiumRenderer` pool and rises with fill.
- **Stage 1 (Latticed):** the cauldron is re-banded in dark hematic iron with a crimson-lacquer lip, short bone feet, and a lattice of clotting ribs across the mouth. Drip channels run down the sides.
- **Stage 2 (Druse):** a wider, crystal-lined basin with vein-like growth over the rim. It still fits one block.
- **Model tooling:** keep 16 px/block texel density. Author the Stage 1–2 models with a `tools/model_export/` Python exporter that writes the `.bbmodel`, the stage models and the facing/stage blockstate variants, following `vial_centrifuge.py`.
- **Formations:** the in-progress formation grows at the centre of the surface using the shared `renderSphere`. Finished formations rest on the blood or the lattice as small items, the way `IronBrazierRenderer` draws offerings.
- **Effects:** completion is a short custom glow/cell burst, and collapse a dark fizz, both from the Hemomancy/HutosLib toolkit (`HemoParticleData` and friends). Conversion from cauldron gets a small custom splash. No vanilla particle clouds. Sounds may stay vanilla, as in the old handler (amethyst break, fire extinguish, plus a fill sound).
- **Kit sprites:** native 16×16 with binary alpha, following the authored centrifuge-kit sprite pattern.

## Docs to update

- `docs/HEMOMANCY_REFERENCE.md`: §205, §221 and §223 (replace the mechanic paragraph), the items table at §2834, and a new Condenser station section.
- `docs/STATION_UPGRADES.md`: the seventh station, the tiers and offerings tables, and stage behaviour.
- `wiki/Harbinger-Path.md`:36.
- `docs/LORE_REFERENCE.md`: check any formation lore.

## Testing

**JVM**
- `SanguineCondenserRules`: capacity, cost, time, collapse and bonus at each stage.
- Resource tests for the stage-branching loot table, both rite JSONs and both kit recipes.
- The infusion/structure safety test.
- A source test that the Condenser branch comes after the rite and structure handlers and before infusion, and that no bare-block formation path remains.

**GameTests**
- Projecting into an empty cauldron converts and fills it.
- Projecting into stone makes Venous Stone and never a formation.
- Water cauldrons are untouched.
- The Condenser crystallizes over time and stops when full.
- Collecting records the Vicar proof only for a contributor at D1, and records `CONDENSE` usage.
- With a full inventory, collection leaves the remainder in the Condenser.
- An emptied stage 0 reverts to a cauldron, and breaking stage 0 drops a cauldron.
- Stage 1/2 rites upgrade in place and preserve blood and items. A broken Stage 1 keeps its stage.
- The journey fixtures are updated.

**Finish** with `./gradlew.bat build` and `alphaCheck`.

## Left to the builder

- exact geometry and textures
- the numbers above once playtested
- the kit recipes' exact ingredients
- seat distance
- sounds
- whether stage 0 extends `AbstractCauldronBlock`
- how the "full" and "collected" feedback is phrased
