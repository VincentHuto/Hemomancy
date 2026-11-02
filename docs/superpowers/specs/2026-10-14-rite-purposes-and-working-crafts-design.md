# Rite purposes and Working crafts design

Date: 2026-10-14. Status: approved in conversation, section by section. Sub-project 1 of the Blood Structure / Cardinal Rite merge.

## Goal

Hemomancy has three overlapping ways to make things with blood: Blood Infusion (one block), Blood Structures (a built pattern fed with Blood Projection, sometimes with brazier offerings) and Cardinal Rites (floor, Focus, staff, braziers, optional structure). The line between Blood Structures and rites has blurred: structures took brazier offerings, station upgrades became rites, and the Working floor already exists for "crafting and station-upgrade rites" but only Rooted Vein and the twelve station upgrades use it.

This project gives rites one grammar and moves machine crafting onto the Working floor:

- Every Harbinger rite declares what it is for (its **purpose**), and the purpose decides its floor style.
- Machines, stations and constructs are made by **Working crafts**: Cardinal Rites whose subject is the built structure. The braziers carry the offerings and the floor rises up the structure and swallows it.
- Early and commonly repeated crafts stay **floorless** and work as they do today, but without brazier offerings.

Success: one offering system, one ceremony for anything that matters, no special cases, more frequent simple rites, and progression that still teaches in order. Net line count may stay roughly even; unification is the point.

## 1. Rite grammar

### Purposes

Every Harbinger rite JSON declares `"purpose"`:

| Purpose | Floor style | Default culmination | Meaning |
|---|---|---|---|
| `self` | Threshold | `ascension` | Degree up, or a power/body change in the caster |
| `world` | Dominion | `domain` (or `grounding` where authored) | Changes the world: conjure an entity, change weather, make something appear |
| `making` | Working | `forging` | Components are consumed and something new is made |
| `bond` | Communion | `binding` | Acts on ties between people: bloodlines, covenants, shared pools |

Rules:

- The serializer rejects a Harbinger rite whose floor style does not match its purpose.
- `"special": true` lets a rite keep its purpose but name any floor. This is the hook for future Qliphoth/Apotheos floors (new floor JSON + visual style, no new engine). Apotheos (`self`), Bloom of the Qliphoth (`world`) and Ancestral Communion (`bond`) are marked special and stay on their current floors.
- Unstained rites are exempt (no floor, no purpose) until their own ritual system exists.
- A data test pins every Harbinger rite's purpose so misfiling cannot come back.
- The authored `culmination` still wins; the purpose only supplies the default.

### Rehoming

Each rehomed rite keeps its tier (minor/lesser/greater/grand) and changes only floor style. Floor style currently changes only palette and default culmination (per-floor ceremony mechanics were never built), so rehoming changes what is built and how it looks, not how the rite plays.

| Rite | Purpose | From | To |
|---|---|---|---|
| Vascular Mending | self | Working | Threshold |
| Hematic Fortification | self | Dominion | Threshold |
| Crimson Beacon | self | Dominion | Threshold |
| Eternal Covenant | self | Communion | Threshold |
| Sanguine Attunement | self | Communion | Threshold |
| Chamber of Will | self | Communion | Threshold |
| Rooted Vein | world | Working | Dominion |
| Hungering Earth | world | Working | Dominion |
| Sanguine Fervor | world | Working | Dominion |
| Sanguine Eclipse | world | Working | Dominion |
| Pallid Shadow | world | Communion | Dominion |
| Pallid Vessel | making | Threshold | Working |
| Crimson Vessel | making | Threshold | Working |
| Ashen Vessel | making | Threshold | Working |
| Horn of Culmination | making | Dominion | Working |
| Hematic Unbinding | bond | Working | Communion |
| Scarlet Summons | bond | Dominion | Communion |

Unchanged assignments: the five rank rites (`self`, Threshold); Sanguine Dominion, Founding Fane, Hematic Succession, Mnemonic Restoration and the seven Puppeteer trials (`world`, Dominion); the twelve station upgrades and Exsanguination (`making`, Working); Bloodline Founding, Bloodline Recall and Covenant Vigil (`bond`, Communion).

Judgement calls (approved): Crimson Beacon is `self` (a death-return power, tied to a place); Puppeteer trials are `world` (they conjure the hostile; the shape learned is the reward); Succession and Mnemonic Restoration are `world` (they reanimate an effigy); Pallid Shadow is `world` (an attack on another player, not a tie).

## 2. Working crafts

### Recipe shape

Twenty Blood Structure recipes become Cardinal Rites at `data/hemomancy/recipe/cardinal_rite/working/<id>.json`, with `purpose: making`, `riteType` equal to their floor tier, and `floor: hemomancy:working_<tier>`.

| Old Blood Structure field | Working craft field |
|---|---|
| `pattern` / `key` | `subject.pattern` / `subject.key` |
| `heldItem` (offhand catalyst) | `medium` (seated in the Focus, consumed on success) |
| `offerings` | `brazier_signature` |
| `bloodCost` | `subject.feed` (blood fed into the structure; values unchanged) |
| `hitBlock` | removed (the subject is matched in the bed) |
| `result`, `required_degree` | unchanged |

The rite's own `bloodCost` is 0. Ceremony: `profile: simple`, no anchors, support sockets, sigils, waves or helpers, faint fog, `failure: safe_retry`, `culmination: forging`.

### The bed

The structure stands on the floor's upper layer, touching any side of the Focus, in any horizontal rotation, at most three blocks tall. Its footprint must lie inside that side's bed:

| Floor tier | Floor | Bed (wide × deep, measured outward from the Focus) |
|---|---|---|
| minor | 3×3 | 3 × 1 |
| lesser | 5×5 | 3 × 2 |
| greater | 7×7 | 3 × 3 |
| grand | 9×9 | 5 × 4 |

Each craft names the smallest Working tier whose bed fits its subject; any larger Working floor also satisfies it. The serializer rejects a subject that does not fit its floor tier's bed.

### Ceremony

1. Seat the catalyst in the Focus, light the braziers and plant the Living Staff. On staff impact the brazier offerings and the catalyst are escrowed and the subject is locked (its matched block positions and states recorded).
2. **Feeding.** The caster holds Blood Projection on any subject block. This is the old structure feed at the same rate and cost, drawn from the caster's blood. The floor membrane climbs the structure as the fill rises. Reaching full seals the rite.
3. The daemon rises over the Focus and the escrowed offerings are drawn into it (the existing offering procession beats).
4. Forging culmination: the structure's blocks are removed and the result condenses on the Focus pad and drops there.

### Safety and recovery

Working crafts follow the station-upgrade model, not the standard rite:

- Recall (Blood Absorption on the Focus) or collapse returns the escrowed offerings, catalyst and staff through the shared recovery path, and leaves the structure untouched. Blood already fed is not refunded.
- Breaking or changing any subject block mid-rite collapses the rite the same way.
- A damaged floor or an absent/out-of-range caster pauses the rite.
- Completion is recorded once under a rite identity token: across interruption or restart it never removes the structure twice or drops two results.
- The completion effect refuses at activation and before the finale if the subject no longer matches; nothing is consumed while it waits.

### Progression hooks

Completion grants exactly what a floorless craft grants today: `MachineAccessEvents.awardMachineCrafted` (the `ITEM_CRAFTED` stat that machine access checks), `FirstBloodcraftAssignment.recordStructure` and `LivingStaffBondHelper.unlockFromBloodStructureCraft`. Machines players have already built stay usable.

### Shared code

The escrow, recovery and completed-once logic moves out of `StationUpgradeRites` into a shared subject-escrow helper used by both Working crafts and station upgrades. Station upgrades keep their JSON, `StationUpgradeCatalog` and the `STATION_UPGRADES.md` contract; they are the same Working rite with an upgrade-in-place subject instead of a consumed one.

### Roster

Patterns keep their authored design except where noted. Four patterns used `minecraft:copper_block`; Harbinger floors never carry copper (Unstained material, a White Humor witness, and it weathers), so those blocks become `hemomancy:chiseled_hematic_iron_block`. Offerings marked *new* are a first pass and can be tuned freely.

| Craft | Degree | Tier | Medium | Feed (mL) | Offerings (each entry = one lit brazier per count) | Pattern change |
|---|---|---|---|---|---|---|
| Resonant Forge | 3 | minor | ferric_binder | 500 | wax_cylinder | — |
| Scarlet Vanity | 3 | minor | sanguine_formation | 300 | *new:* dried_gourd, minecraft:leather | copper → chiseled hematic iron |
| Somatic Loom | 3 | lesser | sanguine_formation | 150 | *new:* minecraft:string, neurotic_enzyme | trimmed to 3×2 |
| Mycelial Lantern | 3 | lesser | ferric_binder | 2,500 | *new:* minecraft:brown_mushroom, minecraft:glowstone_dust | copper → chiseled hematic iron |
| Fungal Implantation Pylon | 4 | lesser | hyphal_substrate | 1,200 | *new:* neurotic_spores, minecraft:bone_meal | copper → chiseled hematic iron |
| Sporitic Thurible | 4 | lesser | spore_sac | 1,000 | *new:* minecraft:chain, minecraft:brown_mushroom | copper → chiseled hematic iron |
| Enzymatic Scriptorium | 3 | greater | ferric_binder | 500 | enzyme_primer, blood_crystal_shard, minecraft:lapis_block | — |
| Mnemonic Reliquary | 3 | greater | sanguine_formation | 75 | *new:* minecraft:book | — |
| Warp Chair | 3 | greater | sanguine_formation | 250 | blood_crystal ×2, minecraft:ender_pearl ×2 | — |
| Semi-Sentient Construct | 3 | greater | befouling_ash_trail | 250 | *new:* minecraft:rotten_flesh, minecraft:bone | redrawn from 6×6×2 to 3×3×2 |
| Mason Effigy | 4 | greater | runic_motif_paper | 150 | #minecraft:flowers ×2 | — |
| Mind Spike | 4 | greater | sanguine_formation | 500 | *new:* neurotic_enzyme, minecraft:amethyst_shard | — |
| Runic Chisel Station | 4 | greater | sanguine_formation | 100 | *new:* minecraft:flint, minecraft:iron_ingot | — |
| Visceral Mirror | 4 | greater | sanguine_formation | 200 | *new:* minecraft:ink_sac, minecraft:quartz | — |
| Dendritic Distributor | 5 | greater | sanguine_formation | 500 | *new:* tendon_line, ferric_binder | — |
| Morphling Incubator | 5 | greater | morphling_polyp | 200 | *new:* swollen_leech, minecraft:slime_ball | — |
| Vacant Effigy | 5 | greater | sanguine_formation | 250 | *new:* minecraft:armor_stand, blood_stained_stone | — |
| Sanguine Monolith | 6 | greater | sanguine_formation | 175 | *new:* sanguine_quintessence, minecraft:echo_shard | — |
| Covenant Throne | 6 | greater | sanguine_formation | 8,000 | *new:* sanguine_quintessence, blood_crystal, minecraft:crying_obsidian | — |
| Vascular Effigy | 6 | grand | sanguine_formation | 6,500 | *new:* vascular_poultice, tendon_line, sanguine_quintessence | — |

Unprefixed IDs are `hemomancy:`. Somatic Loom trimmed pattern (top layer first, as in the current format): `[["P P","TIT"],["P P","TIT"]]`. Semi-Sentient Construct redrawn pattern: `[["B B","STS"],[" C ","TVT"],["B B","STS"]]` with its existing key (soul sand, hematic iron block, nether wart block, befouling ash trail, conscious mass). The construct moves to greater rather than grand because a Degree 3 craft should not demand a grand floor.

## 3. The floorless tier

Stays floorless, hand-fed with Blood Projection, no Focus:

- Blood Infusion (Stone → Venous Stone) and Sanguine Formation condensation.
- Eight Harbinger Blood Structures: Hematic Iron Block, Liber Sanguinium, Living Staff, Iron Brazier, Dried Gourd, Hematic Stake, Vial Centrifuge, Ghastly Alembic.
- The two Unstained structures (Pallid Retort, Unstained Pillar), untouched.

Rules:

- The `offerings` field is removed from the Blood Structure schema and the serializer rejects it. Offerings exist only in rites. The Vial Centrifuge loses its glass bottle and copper ingot offerings.
- A data test pins the floorless roster. Adding a floorless recipe is a deliberate edit.

Removed:

- `BloodStructureOffering`, `BloodStructureOfferingPlacement`, offering matching/consumption in `BloodStructureCraftingHelper` and offering parsing in the serializer.
- `BloodStructureCraftingHelper.isRiteExclusive`, its message, and `blood_structure/consecrated_bloodwell.json` (the Founding Fane already manifests the Bloodwell).
- `PacketBloodStructureOfferingBurst` and its client data. Puppeteer trials switch to the rite's existing offering beat.
- The twenty migrated `blood_structure/*.json` files.

Kept: `BloodStructureFeedManager`, `PendingBloodCraftManager`, the warp renderer, the C-key path for Unstained structures and rite starts, and the JEI Blood Structure category for the floorless tier.

The floorless completion is touched by the offering removal, so its vanilla block-break particles (`levelEvent 2001`) and Enderman-scream sound are replaced with Hemomancy blood cells and a `rite.*` sound.

### Legacy IDs

One table maps each migrated `hemomancy:blood_structure/<id>` to `hemomancy:cardinal_rite/working/<id>`. Mnemonic Blueprints rewrite their target through it when read. The Liber recipe map, hints and tests use the new IDs.

## 4. Visuals: the floor swallows the structure

Built to `docs/MANIPULATION_VISUALS.md`: a `WorkingCraftSwallowGeometry` class and a pure `WorkingCraftSwallowRules` timing class with JVM tests; Hemomancy/HutosLib particles only; the Working floor palette plus the pale core `0xFFD2C4` and clot `0x7A1020` accents; visual timing never gates gameplay.

1. **Rising** (feeding; fill from the server). The floor membrane swells into a lip at the structure's foot. A fill line climbs the structure's faces: below it the existing structure warp shader draws the membrane in the floor's palette, cut off at the line by a new fill-height uniform. A ragged wet meniscus, matching the floor's scab rim, sits at the line. Vessels climb from the floor's vessels up the structure's corners as tapered strands pulsing on the floor's 36-tick heartbeat. The Blood Projection beam lands on the fill line.
2. **Seal.** One hard pulse runs up the structure; the membrane caps it.
3. **Offerings.** The existing procession: each offering is drawn along a vessel into the daemon.
4. **Forging.** The server removes the blocks on the finale beat. The client keeps a ghost of the captured block states and renders them sinking into the floor under the membrane, which closes over the bed with a ripple. The existing forging culmination then drops the knot onto the Focus.
5. **Recall or collapse.** The membrane drains back down the structure into the floor, faster than it rose, leaving the structure standing.

Plumbing: the rite snapshot carries subject positions/states and fill progress; the swallow draws in the rite render pass beside the floor; two new `rite.*` sound events (rise loop, sink) layer existing samples until bespoke audio exists.

## 5. Progression and teaching (Option B)

- **Degree 1:** floorless crafts; Vicar proofs unchanged (Liber or Hematic Iron Block).
- **Degree 2:** Vial Centrifuge and Ghastly Alembic stay floorless. Rooted Vein (now on a Dominion lesser floor) stays the Vicar's first-rite lesson: Focus, staff, braziers and anchors. First Separation and its delivery contracts are untouched.
- **Degree 3:** the Alchemist teaches the Working floor through Pallid Vessel and Working crafts, and the Somatic Loom is the first Working craft most players make. The Woven Vessel's final Loom weave implicitly needs it; teacher and ledger hints say so. No new Main proof.
- **Degree 4+:** station upgrades already use Working floors; players expand a floor they own.

Changes that follow:

- Alchemist Votary/Initiate/Illuminatus lines stop presenting "Blood Structure crafting" for machines and teach floorless crafts versus Working crafts.
- The Vicar's Rooted Vein lesson names the Dominion floor's blocks (deepslate tiles/bricks, chiseled hematic iron studs).
- Working-craft Mnemonic Blueprints are rite blueprints (floor, Focus and subject on its bed).
- Existing saves: built machines keep working; a Degree 3+ player without a Working floor builds one; no progress is lost.

## 6. Screens, tools and tests

This project keeps every surface correct; the full Liber/JEI consolidation is sub-project 2.

- **Liber recipe map:** the twenty machines keep their Crafting-tab families under their new IDs and open the rite hint screen, which shows the subject on its bed. The Blood Structure hint screen drops its offerings display.
- **JEI:** a new Working Craft category (structure preview, Focus medium, brazier offerings, floor tier, feed, result) covers every `making` rite with a result item. The Blood Structure category stays for the floorless tier.
- **Debug Structure Spawner:** also builds the subject on its bed so each Working craft spawns ready to run.
- **JVM tests:** purpose/floor validation including the special exemption; offerings rejected on floorless recipes; floorless roster pin; subject-fits-bed; bed geometry; legacy ID table; swallow timing rules; renderer hook source contracts.
- **GameTests:** a lesser-floor Somatic Loom craft runs end to end and grants machine access; recall and collapse return escrow and leave the structure; completion is once-only; a broken floor pauses; Living Staff and Vial Centrifuge still craft floorless without braziers; Rooted Vein runs on Dominion; spawned Working crafts run.
- **Finish:** `./gradlew.bat alphaCheck`; update `HEMOMANCY_REFERENCE.md` (§5.3 floors, §25.1–25.2), `STATION_UPGRADES.md` (shared escrow note), relevant `wiki/*.md`, `CARDINAL_RITE_AUDIT.md` status and a contract line in `AGENTS.md`.

## Out of scope

- Liber/JEI consolidation (merging the Crafting and Rites tabs, regrouping by purpose): sub-project 2.
- Special Qliphoth/Apotheos floors and effects.
- Rite space as a lasting boundary (items/blocks/entities that work only inside it); the `common/rite/zone` model is the natural home.
- Unstained structures and rites.
- Per-floor ceremony mechanics (the audit's Endure/Hold/Share/Precision archetypes).
- A blood-cost balance pass.
