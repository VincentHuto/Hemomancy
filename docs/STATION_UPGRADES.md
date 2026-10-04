# Station upgrades: shared implementation contract

Current contract: 2026-09-30. Read before adding or changing **any station upgrade**, kit, teacher, rite, recipe, book page, dialogue, or tests. Current source/resources remain authoritative; update this contract when intentionally changing them. Station projects are not new degree-promotion checklists.

## Rules for every upgradeable station

- Use `common/station/StationUpgradeCatalog`, `StationUpgradeProgress`, `StationUpgradeRules`, `StationTierProperty`, and `UpgradeableStation`, with `common/rite/harbinger/StationUpgradeRites`. Do not introduce private station-upgrade ceremonies or parallel reward ledgers.
- Upgrade the existing machine in place, exactly one stage at a time. The current shared `stage` property is 0 (base), 1, or 2. Preserve the block entity, contents, stored blood, orientation, and relevant machine state. More stages require explicitly extending the property, rules, persistence, resources, and tests.
- Distinguish **installed stage**, **personal eligibility**, and **free-kit claim**. A rite requires the correct installed source stage and the acting player's eligibility for the requested upgrade and every earlier upgrade of that station. Eligibility means active blood, sufficient degree, and all required personal machine-use proofs. It does not mean accepting a teacher's gift.
- Crafted and gifted upgrade items are equally valid. Shaped kit recipes remain craftable before eligibility or a claim; recipe-book unlocking is not a crafting permission gate. Server rite activation checks eligibility, so another player's kit cannot bypass it.
- Teachers give one free item per eligible tier, with pending delivery when inventory is full. Accepting the first gift is not required to accept the second if all earlier personal requirements are satisfied. Never reset claims to issue another free kit.
- Unclaimed kits show a tooltip naming the teacher and warning that crafting may waste materials because a free kit is still available when eligible. Do not block crafting or imply the gift is already claimable below its degree/practice requirements. Client tooltip data is a private synchronized snapshot; the server attachment remains authoritative.
- Personal practice on another player's station counts when the actual operation/extraction is credited to the acting player. Merely owning a kit or borrowing a higher-tier station grants no missing proof. Do not ban shared-machine use to enforce personal eligibility.
- Ordinary operation gates, armor lineage/ending requirements, enchanting enzymes, recipe reagents, and degree promotion remain separate from station-upgrade eligibility. Do not merge them into kit claims without an explicit design change.

## Current tiers and personal gates

### Creative-only direct kit use

Creative players may right-click any of the five stations (including linked filler parts) with that station's catalog upgrade item to install its corresponding stage immediately. Tier-two items can upgrade a base station directly; equal/lower-tier items never downgrade it. The item is not consumed. This server-authoritative exception bypasses degree, active blood, personal practice, claims, rites, offerings, blood costs, idle/occupancy/lock requirements, and Alembic clearance. It preserves the existing block entity, identity, inventory, stored blood, facing, processing state, and any active rite lock. It does not grant personal practice or free-kit claims. Survival and Adventure retain the ordinary rite requirements, and other stations' kits do not apply.

Alembic linked fillers are expanded when the target footprint has room. A blocked footprint still permits Creative stage conversion without deleting obstructing blocks; expanded collision parts cannot be placed until the footprint is clear. An already running rite retains its lock and escrow and follows normal subject-stage revalidation/recovery if Creative changes its subject's stage.

Usage is cumulative: tier 2 also requires tier 1 eligibility. Degree numbers below are upgrade gates, not base-machine access gates. All ten require active blood; shared claim/rite eligibility does not add Purifying or Clarity checks.

| Station | Upgrade / degree | Item ID | Free-kit teacher | Required personal use | Rite path | Circuits |
|---|---|---|---|---|---|---|
| Ghastly Alembic | Condenser / D4 | `hematic_condenser_kit` | Alchemist | Extract a completed distillation | `first_condensation` | 2 × 50 mL |
| Ghastly Alembic | Athanor / D6 | `sanguine_athanor_kit` | Alchemist | Extract both a refinement and a compound | `sanguine_athanor` | 3 × 50 mL |
| Vial Centrifuge | Calibrated Rotor / D4 | `centrifugal_governor_kit` | Alchemist | Personally recover an ordinary specimen result from a balanced spin; existing First Separation recovery counts | `steady_separation` | 2 × 50 mL |
| Vial Centrifuge | Fractionating Rotor / D6 | `fractionating_rotor_kit` | Alchemist | Personally recover an ordinary specimen result processed at stage 1 or higher | `second_fraction` | 3 × 50 mL |
| Resonant Forge | Precision / D5 | `precision_governor_kit` | Artificer | Complete a grind and an application | `true_groove` | 2 × 250 mL |
| Resonant Forge | Masterwork / D7 | `master_cam_kit` | Artificer | Complete a single-selection grind | `enduring_pattern` | 3 × 250 mL |
| Hematic Armature | Consecrated / D5 | `vicars_consecration_kit` | Artificer | Complete an armor upgrade | `armature_consecration` | 2 × 50 mL |
| Hematic Armature | Monolithic / D7 | `monolithic_cornerstone` | Sanguine Monolith | Complete an armor upgrade at a consecrated Armature | `monolithic_armature` | 3 × 50 mL |
| Enzymatic Scriptorium | Eightfold / D5 | `rubricators_quill` | Mnemonist | Complete an enchant | `eightfold_script` | 2 × 50 mL |
| Enzymatic Scriptorium | Palimpsest / D7 | `palimpsest_burin` | Mnemonist | Complete a targeted enchant | `palimpsest` | 3 × 50 mL |

IDs use namespace `hemomancy`; rite paths are under `cardinal_rite/`. Repeat-craft recipe IDs equal item IDs. `vicars_consecration_kit` displays as **Armature Consecration Kit**; retain the registry ID. It and the Cornerstone are not current Scriptorium upgrade items.

Condenser opens refinement/compounding and increases the Alembic's maximum stored blood by 1.5×, from 5,000 to 7,500 mL. The upgrade preserves the current amount without adding blood; capacity follows the installed tier across reloads and moving the station. Athanor retains the 7,500 mL capacity and adds the **second catalyst slot**, plus binding/refilling. Its mantle supplies permanent internal heat without any block beneath the controller, and all Alembic recipes process at 2x speed (half time, rounded up to a server tick). Costs, reagents, and yields are unchanged; base and Condenser still need external heat and use ordinary recipe times. Precision enables individual pattern selection; Masterwork enables master patterns/stabilization. Consecrated and Monolithic Armatures support corresponding D5–D6 and D7+ armor recipes without replacing their other requirements. Eightfold and Palimpsest support targeted enchantment improvements of +1 and +2 respectively.

### Centrifuge recovery and output storage

The Calibrated Rotor spins in 150 ticks (7.5 seconds), boosts ordinary primary quantity by 25% on average through fractional rounding, and has a 75% chance of one Hematic Iron Powder per ordinary vial. The Fractionating Rotor uses 100 ticks (5 seconds), +50% primary quantity on average, and one guaranteed powder per ordinary vial; these values replace the first tier's benefits. Each mixed specimen has a 50% chance of one item of a different native fraction, including infected fungus when the existing fungal property supplies it. Single-fraction specimens cannot invent a second type. Consecrated Syringes still yield one matching Hallowed Residuum without ordinary vial bonuses. Balance, empty-vial return, sample restrictions, and 250 mL blood income per successful batch remain.

Machine slots 0–19 retain their indices. Primary and secondary fractions share the original eight outputs at 10–17: matching items and components merge first, then empty slots fill in ascending order. Capacity is reserved for the complete batch together. Old slots 20–27 migrate into these outputs with their processing-stage credit; overflow remains in a saved recovery queue, drains when space opens, blocks new spins and upgrade rites until recovered, and drops with the inventory when the machine breaks. Successful startup rolls and saves the complete batch once. All primary/secondary outputs, powder, and vial returns must fit before any sample is consumed. A blocked completion keeps the rolled batch and inputs for an explicit retry after clearing outputs; reload does not reroll it. Samples cannot be moved while a batch is active or pending. Extraction credit follows the actual processing stage saved with the completed result, so upgrading a machine with old output cannot invent calibrated practice. Shared-machine extraction counts. Rite locking also stops blood transfer and inventory interaction.

The Calibrated body adds an octagonal iron footing, four bearing braces, indexed rails, and a broad front governor dial. The Fractionating body retains those fittings and adds paired glass receivers, independent return pipes, and a raised split manifold. Both reuse existing station materials and the shared eight-vial animated rotor. All new geometry uses the 0.25-unit grid and stays within one horizontal block; the rear manifold reaches 21.5/16 blocks high and clears the rotor sweep. All four facings select the appropriate body by installed stage, and the custom item renderer reads the saved stage when rendering a moved station. The base model is retained.

Editable static bodies are `models/block/bbmodel/vial_centrifuge_calibrated.bbmodel` and `vial_centrifuge_fractionating.bbmodel`. Regenerate runtime JSON, embedded-texture Blockbench bodies, and stage/facing variants with `python tools/model_export/export_centrifuge_upgrades.py`; validate with `check_centrifuge_upgrades.py` and preview with `preview_centrifuge_upgrades.py`. The existing Java arm model remains the moving-part source. Both kit items have distinct authored native 16x16 sprites: a bone-rimmed red calibration dial for the Governor and a forked iron crosshead with paired glass fractions for the Fractionating Rotor. They reference their own `item/centrifugal_governor_kit` and `item/fractionating_rotor_kit` textures. Pixel grids and palette live in `tools/model_export/centrifuge_kit_sprites.json`; run `export_centrifuge_kit_sprites.py` to regenerate the PNGs and `--check` to validate native dimensions, binary alpha, palette limits, exact export parity, and item model references.

## Exact brazier offerings

Each rite consumes **one of each of six items**: its upgrade item and the five companions below. Offerings are not ordinary operating catalysts or interchangeable generic kits.

| Rite | Five companion offerings (in addition to its kit) |
|---|---|
| First Condensation | `glass_bottle`, `blaze_powder`, `sanguine_glass`, `ferric_binder`, `vivacious_enzyme` |
| Sanguine Athanor | `brewing_stand`, `ghast_tear`, `sanguine_quintessence`, `chromatic_sublimate`, `hemolytic_solution` |
| Steady Separation | `iron_ingot`, `chain`, `sanguine_glass`, `ferric_binder`, `neurotic_enzyme` |
| Second Fraction | `amethyst_shard`, `ghast_tear`, `hematic_iron_block`, `chromatic_sublimate`, `enzyme_primer` |
| True Groove | `grindstone`, `honeycomb`, `amethyst_shard`, `wax_cylinder`, `hematic_iron_block` |
| Enduring Pattern | `anvil`, `netherite_scrap`, `mnemonic_ambergris_block`, `chalybeate_sclerite`, `suspended_blood_crystal` |
| Armature Consecration | `armor_stand`, `chain`, `tendon_line`, `hemolytic_plating`, `sanguine_formation` |
| Monolithic Armature | `netherite_scrap`, `monolith_fragment`, `monolith_imbued_cloth`, `calcified_blood_spine`, `sanguine_quintessence` |
| Eightfold Script | `ink_sac`, `feather`, `lapis_block`, `cuttlefish_chromatophores`, `runic_motif_paper` |
| Palimpsest | `echo_shard`, `glow_ink_sac`, `mnemonic_folio`, `axonal_slate`, `myelin_sheath` |

Vanilla companions use `minecraft`; mod companions use `hemomancy`. Verify quantities/consumers against `src/main/resources/data/hemomancy/recipe/cardinal_rite/`, not historical prose or item names alone.

## Shared ceremony, layout, and recovery

First Condensation uses the lesser working floor; all other upgrades use the greater working floor. Each has six lit offering braziers, a Cardinal Focus, a planted Living Staff, and the matching source-stage station. Tier 1 uses four anchors and two circuits; tier 2 uses eight anchors and three circuits. Fill anchors, seal the daemon, project through circuits in order, then finish the procession/finale. No sigils, helpers, or ordeal waves are required. Failure uses `safe_retry`.

Anchors take 50 mL each in addition to circuit costs: 300/550 mL for the non-Forge first/second upgrades and 700/1,150 mL for Forge upgrades. A recipe's `bloodCost: 0` is not a blood-free ceremony.

Seat the machine on the upper layer behind the Focus, opposite the rite's forward direction. Alembic: distance 1 (3×3 upper pattern). Forge, Armature, Scriptorium, Centrifuge: distance 2 (5×5 upper pattern). Do not restore old 3×3 Forge/Armature upper patterns: they cannot match the handler's station position. Forge needs its complete footprint and matching facing; Armature must be idle/unoccupied and face correctly; Alembic must be idle, unlocked, and not processing; Scriptorium must be unlocked. Centrifuge must be unlocked, face the rite, and have no running or pending batch.

The server locks the subject and records its identity, source stage, and relevant contents before escrowing offerings. Completion revalidates the subject and settles only once. The rite pauses for an absent/out-of-range caster or damaged floor; missing subject/focus triggers recovery. Cancellation/failure returns exact escrowed offerings and the Staff through persistent Cardinal recovery. **Blood already spent is not refunded.** Recovery/completion must remain idempotent across interruption/restart.

Breaking a completed station retains its tier on the dropped item through `stage` copy-state; Forge's custom drop also carries station state. It does not refund the consumed kit. Ordinary break behavior for inventory/blood is machine-specific: in-place preservation does not promise every machine's contents survive breaking.

## Lore and teaching contract

Dense station instructions explicitly set `decal: false` on their page resources. The reader draws decorative decals over body text; the page-level opt-out keeps named actions, offerings and costs readable without changing the Fane theme's global decal chance or authored artwork. The seven upgrade pages, advanced Alembic operation pages and base Scriptorium page use this setting; discovery and gameplay gates are unchanged.

The Liber's seven authored upgrade lessons unlock when the player meets the catalog's existing personal eligibility, without accepting a free kit or completing the upgrade first. Shared station synchronization checks this before its unchanged-state early return, so degree changes and login repair work even if practice data has not changed. First Condensation, Sanguine Athanor, Steady Separation, Second Fraction, Groove and Cam, Eightfold Script, and Palimpsest are explicitly registered discoverable pages. Completed matching rites also unlock their lesson. Refinement/compounding instructions accompany First Condensation eligibility; binding/refilling instructions accompany Athanor eligibility. Knowledge remains learned afterward; losing active blood does not erase it. This grants no kit, recipe, practice proof, or promotion. Armature teaching remains separate.

The base Scriptorium page unlocks when a D3-or-later Harbinger asks the Mnemonist about the station through its existing dialogue choice. This event uses normal server dispatch and validates a nearby living teacher and the existing non-purifying/non-Clarity path; it is not a generic `liber_unlock` event. No enchantment, station ownership, upgrade claim or Main proof is needed to read the construction lesson. Previously visited conversations can be repeated to learn the page; no historical visit is inferred from rank alone.

### Alembic model and clearance

The basic Alembic is non-occluding and does not block visibility through its open frame. Its lit and unlit baked models, and editable base model, disable ambient occlusion to avoid false dark patches on the tall cap and pipe assembly. Normal block lighting and directional face shading remain enabled; linked fillers transmit light.

The Condenser uses the complete Alembic with its cooling coils, return pipe, and supported receiver. The Athanor adds a heating mantle beneath this assembly. Its model is raised by 7/16 block so the mantle feet rest at floor level; its visible bounds are 29/16 blocks wide, 22/16 blocks deep, and 38/16 blocks tall. Reserve the occupied cells of a 3×3 footprint and three vertical layers, rotated with the machine's facing. The actual collision/selection shapes follow the model cubes within each cell, rather than filling that entire volume.

Placement checks the stage saved on the dropped item. Both preparation and completion of an Alembic upgrade require clearance for the target stage, accepting only replaceable cells or fillers already linked to this machine. Completion preserves the controller, inventory, and blood; blocked completion follows the shared recovery path. Linked parts redirect interactions and breaking, and removal clears only this machine's fillers. Base and Condenser require a heat source below the controller. Athanor supplies permanent heat through its mantle and needs no block below it.

Editable full models are `models/block/bbmodel/ghastly_alembic_condenser.bbmodel` and `ghastly_alembic_athanor.bbmodel`. Run `python tools/model_export/export_alembic_upgrades.py` to regenerate their embedded atlases, runtime JSON, blockstate wiring, and `AlembicGeometry.java`. Face UV dimensions follow cube dimensions at a consistent material texel scale.

The same ritual grammar serves every station. Material identity and teacher voice distinguish recollection, separation, resonance, and vestment work; they do not imply separate progression engines. Teachers recognize practiced work and offer a gift, rather than granting permission by handing over an object. A crafted or gifted tool cannot supply knowledge the practitioner has not earned.

The Scriptorium's former bespoke ceremony was an early implementation holdover, not intentional lore. Do not restore orb-writing, colored connections, Bloodlicker waves, or upgrade enzyme charges. Enzymes still matter for ordinary enchanting. Quill and Burin are tools of recollection, not sacred objects. Armor remaking on the body remains distinct from upgrading the Armature apparatus.

## Refactor record and superseded references

The September 2026 refactor unified four station stacks under the catalog/progress/rite system, introduced unique Scriptorium upgrade items, and stored stages on original blocks. Follow-up work made prior-tier **eligibility**, not prior-tier **claim**, the prerequisite; guarded actual server activation; synchronized free-kit warnings; expanded activation/recovery tests; and corrected Forge/Armature upper-pattern distance. Athanor documentation now describes its second catalyst slot.

`ScriptoriumRites`, `AlembicUpgradeRites`, `ResonantForgeUpgradeRites`, and `ArmatureUpgradeRites` were removed. References to those handlers, shared Scriptorium kits, kit drops on normal break, mandatory earlier gifts, or a third Athanor catalyst are superseded. `monolithic_script` is an old alias for `palimpsest`, not another current upgrade.

Compatibility remains in `StationUpgradeMigration` and rite loading, including old attachments/block-entity tiers and safely collapsing old Scriptorium orb rites. The mod is unreleased: legacy migration is not a release acceptance priority and should not drive new complexity without a new requirement. Keep historical reports as evidence, not current instructions.

## Extension checklist for contributors and AI agents

1. Add catalog metadata, station implementation, personal usage hooks, teacher routing/rewards, and unique kit resources through the shared system. Validate actual teacher/player on the server; never trust client event names alone.
2. Author six offerings, floor, source-stage subject pattern, seat distance/facing, anchor/circuit counts, and finale consistently in JSON and handler rules. New costs/layout exceptions must be explicit, tested, and documented; do not assume every future upgrade is D5/D7.
3. Preserve in-place state, locking, recovery, one-time claims/pending delivery, server eligibility, client sync, and dropped-item stage. Direct kit use is restricted to the Creative-only exception above; ordinary play must use rites.
4. Test real activation/ordered completion, wrong degree/inactive blood/missing current or earlier practice, gifted/crafted kits, optional prior claims, wrong installed stage/subject/layout, interruption/refund, and persistence. Test successful personalized usage as well as rejection.
5. Update this document, mechanics/lore references, guidebook pages, dialogue/item inquiries, tooltips, recipes/loot, and agent instructions together. Keep display names separate from registry IDs. Leave unrelated artwork alone.

## Evidence and repeatable validation

The 2026-10-01 shape-contract follow-up replaced an obsolete full-box source assertion with controller model-part wiring checks and a loaded-server test of all three Alembic stages and four facings. Actual placed controller/filler selection and collision match the authored clipped geometry, stay inside each linked cell, and retain the 31/16 base/Condenser and 38/16 Athanor heights. Both focused shape JVM tests and all 33 station GameTests passed in `build/alembic-shape-contract-current.log`. No production shape changed. This does not establish client visual acceptance; the full progression gate remains red.

```powershell
./gradlew.bat test --tests 'com.vincenthuto.hemomancy.common.station.*' compileGameTestJava --offline --console=plain
./gradlew.bat runStationUpgradeGameTestServer --offline --console=plain
```

The 2026-09-29 implementation verification passed 17 station JVM/resource tests and 9 dedicated GameTests. `StationUpgradeActivationGameTests` exercises both upgrades of all four stations through real floor matching/server activation, escrow, ordered circuit interactions, engine procession/finale, and shared completion. It also checks missing eligibility, recovery without duplicate refunds, one-time/pending rewards, and second-tier claims without accepting the first gift.

Tests prefill anchor/seal state and accelerate ordeal ticks: they do not establish a player's full physical ceremony or natural elapsed timing. No live-client tooltip review, two-connected-player session, survival material-availability audit, or exhaustive restart/migration validation is claimed. Logs: `build/station-upgrade-gametest/logs/`; JVM reports: `build/test-results/test/`. Report new evidence separately from this dated result.

The 2026-09-30 Centrifuge implementation passed 21 focused JVM tests, 32 station runtime tests, 43 clinical progression tests, and 7 advanced-brewing tests, plus assembly. The station suite now exercises both upgrades of all five stations. See [Centrifuge validation](validation/CENTRIFUGE_UPGRADES_2026_09_30.md) for commands, regressions, full-suite failures, and the live-client capture limitation.

The same-day [Centrifuge model follow-up](validation/CENTRIFUGE_MODELS_2026_09_30.md) adds the two stage bodies, saved-stage item appearance, and a textured front/rear preview. Export, geometry, UV, hierarchy, rotor-clearance checks, 21 focused JVM tests, 32 station runtime tests, and assembly passed; live visual acceptance remains open after the review client exited before capture.

The [Centrifuge kit sprite follow-up](validation/CENTRIFUGE_KIT_SPRITES_2026_09_30.md) replaces both item sprites with distinct native art. Both PNGs pass grid/palette/alpha checks and are verified in the assembled JAR with separate texture references.
