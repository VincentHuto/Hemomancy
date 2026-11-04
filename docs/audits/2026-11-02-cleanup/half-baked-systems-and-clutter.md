# Half-baked systems and clutter (system level)

Date: 2026-11-02. Read-only audit of `neo-1.21.1` at `8d01a316c` plus the working tree. Scope: whole mechanics,
subsystems and content tracks that exist in partial form, judged as a lead designer reviewing scope before alpha. Dead
code line by line, progression blockers and doc drift each have their own report in this folder; where a finding here
overlaps one of them it is kept short and marked. Nothing was built or run; every "Verified" claim was checked in source
or data in this session.

## Summary

The census below covers **92 systems**: **56 finished**, **5 need finishing**, **6 should merge into a neighbour**,
**11 should be cut** and **14 need a design decision**. The mod's main lines (rites, manipulations, scars, the D8 split,
the Court, the Circus, the mausoleums, NPC trade) are complete loops. The clutter is at the edges. There are
first-attempt systems that a later redesign made obsolete but never deleted: the pre-Court Morphling potions, the legacy
Saint trial puzzle, the "vanilla milestone gate" blocks, and an old-save compatibility layer kept for a mod that isn't
live. There are half-exposed parked systems that a survival player can reach but not finish: Drudges, Visceral Organs,
rite spaces, the Hemopothecary villager and the Morphling Cradle. There is a cluster of June 2026 vanilla-convenience
items nobody teaches. And three acquisition chains are broken, so whole features can't be done in survival: Morphling
incubation, eight Unstained rites, and the Mnemonist Puppet trial.

There are **33 findings**: 9 High, 15 Medium, 9 Low. Eighteen need the user's call outright and six more need a narrow
choice. The cut list has 15 items, mostly S effort. The finish list has 16 items. The top six are all S effort, and five
of them need no design call.

Counts used below were taken by Grep/Glob sweeps over `src/main/java/com/vincenthuto/hemomancy` (abbreviated `J/`),
`src/main/resources/data/hemomancy` (`D/`) and `src/main/resources/assets/hemomancy` (`A/`). Line counts per system are
approximate (pattern-matched file names, including client code).

---

## 1. System census

Verdicts: **F** finished, **NF** needs finishing, **M** merge, **C** cut, **D** decide. "Surface" means what a player
can see: text, a Liber page, an advancement, a tab entry, a teacher line. "Doc status" is what `HEMOMANCY_REFERENCE.md`
(HR) or the topic doc says.

| # | System | What it is | Size | Surface | Doc status | Verdict |
|---|---|---|---|---|---|---|
| 1 | Blood volume and accounting | blood meter, `BloodAccounting`, circulation, borrowed blood | `bloodvolume/` 36 files | HUD, Liber | Implemented | F |
| 2 | Degrees and the D8 split | D0 to D8, `EnumArchonPath`, titles | `degree/` 9 files | titles, ledger, advancements | Implemented | F |
| 3 | Cardinal Rites | floors, Focus, braziers, anchors, waves, daemon | `rite/` 160 files; 93 rite recipes, 19 floors | Liber map, JEI | Implemented | F |
| 4 | Blood Structures (floorless) | hand-fed crafts | 11 recipes | Liber map, JEI | Implemented | F |
| 5 | Working crafts | machines as `making` rites | 20 recipes | Liber map, JEI | Implemented (D-21 balance) | F |
| 6 | Rite spaces | rite-bound tags, spaces over completed floors | `rite/zone` 14 files (~1,000 lines), 15 tag files | none reachable in survival | built; D-10 open | D (SY-8) |
| 7 | Ichorian sigils | rite support and response sigils | `rite/sigil` 18 files, 9 sigils | rite HUD | Implemented | F |
| 8 | Manipulations | eight schools | `manipulation/` 185 files; 123 ids, 15 retired | wheel, Liber, tree | Implemented | F (retired ids: SY-6) |
| 9 | Memory learning, engram weaving | crude memories, Somatic Loom puzzle | ~60 files; 103 weaving recipes | Liber, Mnemonist | Implemented | F |
| 10 | Thelemic tinctures | bodily memories brewed at the Alembic (code name "muscle memory") | `musclememory/` 11 files; 16 recipes | ledger, Liber | Implemented | F |
| 11 | Synaptic loadouts, Reliquary | saved manipulation loadouts | small | screen, Mnemonist | Implemented | F |
| 12 | Skill tree | six Java-owned branches | `init/skills/` 6 files | Sanguine Conduit tabs | Implemented | F |
| 13 | Blood tendency | kinship alignment | `tendency/` 4 files | Liber | Implemented | F |
| 14 | Vascular system | per-section strain, poultice, salve | `vascular/` 7 files | gauge, Anchorite | Implemented | F |
| 15 | Cerebral and fungal scars | Effigy/Brazier loadouts, crucible cultures | ~100 files; 25 + 8 recipes | Liber, Vein-Mason | Implemented | F |
| 16 | Visceral organs | Mirror extraction, 5 echoes, 5 organ effects | ~14 files | WIP tab, atlas | "Split status" (HR:5248) | D (SY-4) |
| 17 | Morphling house strains | 8 strains, incubator, jar, nectar, Primal | ~130 files | Liber, tooltips, HUD | Implemented | NF (SY-1) |
| 18 | Morphling Cradle | owner-bound host block with support aura | 435-line block entity | Liber page, WIP tab | Implemented (HR §16.4) | NF (SY-21) |
| 19 | Puppetry | Crossbar, Spindle, 8 bodies, trials | ~54 files | Circus, Liber | Implemented | F |
| 20 | Rogue and claimed Wills | ambush, bend, claim | ~38 files | Summons tab | Implemented | F |
| 21 | Drudges | SSC-bound servants with one memory | ~12 files, ~2,300 lines | WIP tab; SSC on the Liber map | "WIP post-alpha" (HR:2867) | D (SY-5) |
| 22 | Direct blood routing | suture needle and nodes | ~22 files | needle on the WIP tab, sold by the Artificer | Implemented | F (tab: SY-3) |
| 23 | Bloodlines, ledger, pool | D5 line | ~33 files | ledger item, NPCs | Implemented | F |
| 24 | Founding Fane | D5 territory, stakes, bloodwell | ~20 files | rite, renderer | Partial (balance) | F |
| 25 | Covenant Throne | D6 bond | small | Working craft | Implemented | F |
| 26 | Hematic Succession | successor NPCs | ~20 files | rite, wiki | Implemented | F |
| 27 | Bloodline NPC recruitment | pledge an outpost NPC | 2 files, 304 lines | dialogue | Implemented | F |
| 28 | Chamber of Will | D6 refuge, guided visits | ~46 files | chair, Orb | Implemented | F |
| 29 | Earthen Veins | D2 travel network, Speculum | ~26 files | Vicar lesson, Rooted Vein rite | Implemented | F |
| 30 | Station upgrades | Alembic, Forge, Armature, Scriptorium, Centrifuge, Condenser | contract doc | rites, kits, books | Implemented | F |
| 31 | Advanced brewing | Condenser-tier "refine, compound" | `brewing/` 12 files; **3 recipes** | Liber page | Implemented | M (SY-11) |
| 32 | Custom potions | `EffectInit.POTION_TYPES` | 19 potions, **1 brewable** | vanilla potion tabs, JEI | HR §14 implies brewable | C (SY-7) |
| 33 | Mob effects | 68 registered | 12 never applied | effect icons | HR §14 | C, orphans only (SY-7) |
| 34 | Blood infusion | projection turns one block into another | 2 recipes | D1 proof | Implemented | F (thin: SY-32) |
| 35 | Living weapons, staff fittings | Staff forms, fittings | fitting models wired | Artificer | Implemented | F |
| 36 | Armor sets and abilities | set bonuses, radial abilities | `armor/` 9 files | tooltips | Implemented | F |
| 37 | School combat | school states, damage stages | `damage/` 10 files | effects | Implemented | F |
| 38 | Clinical blood tools | microscope, cabinet, field case, injection | ~48 files | Alchemist, Liber | Implemented | F |
| 39 | Living Bestiary, specimen jars | creature catalogue | small | Bestiary tab | Implemented | F |
| 40 | Liber knowledge, memos | discoveries, virtual Field Notes | ~16 files | HutosLib button | Implemented | F (legacy shims: SY-6) |
| 41 | Harbinger assignment ledger | D1 to D6 Main and Side assignments | `mission/` 52 files; 1,942-line screen | ledger item | Implemented | M (SY-10) |
| 42 | Unstained Observances | 9 assignments | 276 + 319 lines | Book of Observances | Implemented | M (SY-10) |
| 43 | Circus school quests | professors, practicals, guest acts | `mission/circus` 6 files | dialogue | Implemented | M (SY-10) |
| 44 | Court life | membership, standing, lesson | `court/` 9 files | dialogue | Implemented | F (rapport overlap: SY-25) |
| 45 | NPC dialogue hub, inquiries | four boxes, 385 inquiry files | `entity/npc/dialogue` | every NPC | Implemented | F |
| 46 | NPC trade | want cards, rapport, rotation | 56 cards | Trade box | Implemented | F |
| 47 | Hemopothecary villager | profession, POI, 8 trades | 2 files; 5 orphan house NBTs | any villager by a Scrying Podium | "injection is a no-op" (HR §30) | D (SY-9) |
| 48 | Whispers | fungal, Our Lady, pending inbox | small | chat, memos | Implemented | F |
| 49 | Blood Moon | world event | 6 files | Liber page | Implemented | F |
| 50 | Qliphoth chain | Monolith, Pome, Spine, Bloom | ~29 files | rites, Liber | Implemented | F |
| 51 | Apotheos body | spore load, no food | small | HUD | Implemented, unreviewed | F |
| 52 | Gardens dimension | network, terrain, facsimiles, hunters | ~31 files | none | Implemented, unreviewed | F |
| 53 | Heart-node sanctum | Apotheos personal Fane | ~30 files | screen | Implemented; no Liber | F |
| 54 | Silentium possession | possess a facsimile in the Gardens | ~28 files | wheel, cinematics | Implemented, unreviewed | D (SY-17) |
| 55 | Adipocere crusts | corpse-wax worldgen pass | 4 files + decoration, saved data, config | crust block | Implemented | M (SY-16) |
| 56 | Rite of Visitation | Apotheos rides to the heart-node | 2 files, 69 lines | rite | Implemented (D-8) | F |
| 57 | Molting, Chimerization | Apotheos re-shaping and its offering | **0 files** | none | "stub only" / "not built" | D (SY-27) |
| 58 | Saint mausoleums | four complexes, Chapels, ring door, fights | ~93 files | world, bosses | pass 1 done; HR §38 still "post-release WIP" | F |
| 59 | Legacy mausoleum, trial puzzle | disabled generic structure, pylon/altar/gate | 10 NBTs, 3 blocks and their block entities | WIP tab | "post-release WIP" | C (SY-13) |
| 60 | Bosses | Vesper, Mycophant, Annetta, Naeglerophaeon, Phlegethosoma, Excoriated | large | bars, scenes | built; Annetta "Partial" is stale | F |
| 61 | Brined Court | 8 houses, Seats, outposts, NPCs, presences | ~71 files | world, Liber, tooltips | built, never walked | F |
| 62 | Pelagic terrain, ecology | 7 habitats, 6 mobs, growths | ~40 files | world, Bestiary | Implemented | F |
| 63 | Crimson Troupe | pavilion, camps, school, two endings | ~62 files | world, Liber, trade | built | F (D-18) |
| 64 | Cortical Drift, Vagrant Mind | End biome, structure, miniboss | ~38 files | world | Implemented | F |
| 65 | Phlegethontic Nether | basin biome, mobs, commission | ~37 files | world | Implemented | F |
| 66 | Antecedent inquiry, Clairaudiograph | Deep Dark side track, recordings | ~47 files, 158 sound files | 14 Liber pages | Implemented | F (payoff: SY-30) |
| 67 | Dynasty Castle | castle with Fargone and Thirster | 15 templates, ~11 files | world | Implemented | D (SY-26) |
| 68 | Crimson Lodge Annex | hall-camp structure | class, set, pool, biome tag; **no template** | none | "Does not generate yet" (HR:4615) | C or build (SY-14) |
| 69 | Blood Tower | two templates | 2 NBTs | none | listed as a structure (HR:4619) | C (SY-14) |
| 70 | Chthonian termite mound | savanna feature and queen | small | world | Implemented | F |
| 71 | Bog bodies, Ossuary Niche | swamp encounters | small | world | Implemented | F |
| 72 | Non-Euclidean hallway | folded-hallway spike | `world/fold` 12 files + block, renderer, mixin hooks | WIP tab | "Partial/Prototype" | C (SY-15) |
| 73 | Vanilla milestone gates | Crucible of Nether Ichor, Voidtouched Vessel | 2 plain blocks; 13 progression tags, 6 unused | WIP tab, recipes | undocumented | C (SY-12) |
| 74 | Hematic memory tools | Hearty Compass, Husk Effigies, Vein Spider, Void Eye, Witness Organ, Vitality Chalice, Gauge | ~10 classes | WIP tab, recipes | barely documented | D (SY-18) |
| 75 | Blood Chum | fishing lure boost via mixin | 4 files + mixin | recipe | undocumented | M into SY-18 |
| 76 | Gourdvine Tap | living blood-producing plant | 3 files, ~400 lines | WIP tab, recipe | "Partial: draft" (HR §38) | D (SY-28) |
| 77 | Sanguine Vigil | D6 hostile ward block | ~400 lines | WIP tab, Working craft | one table row | NF (SY-29) |
| 78 | No-op items and blocks | idols, Mind Spike, Neutralizing Gasket, Sanguine Blob | small | recipes, tabs | rows only | C (SY-19) |
| 79 | Unstained purity, clarity, observances | the Unstained path | ~138 files | Liber Immaculatus, mirror | Implemented | F |
| 80 | Unstained rites | 18 rites on the pre-purpose grammar | `rite/unstained` 8 files, 1,336 lines | rites | Implemented; D-11 | D (SY-31; blocker SY-2) |
| 81 | Still Arts | Unstained abilities | small | radial | Implemented | F |
| 82 | White Humor | purification pools | small | JEI, Liber | Implemented | F |
| 83 | Our Lady presence, Pale Intercession | whispers, a summon | small | dialogue | Partial | F as scoped |
| 84 | Fully Purified traits | 4 biological traits | 0 runtime | none | Planned (HR:1714-1718) | D (SY-31) |
| 85 | Unstained gear | Final Molt (an advancement grant), Silthmere Glaive | small | main tab, atlas | HR §22.6 | NF (SY-20) |
| 86 | MnA and Curios compat | dormant integrations | `compat/` excluded; `HemoMnAConfig` unregistered | none | Dormant | D (SY-23) |
| 87 | Save-compatibility layer | migrations, retired ids, legacy aliases | 11 migrations, 42 retired item and manipulation ids, shims | some visible | AGENTS.md mandates | C (SY-6) |
| 88 | Config | 133 values in 3 registered specs | 2 dead, 1 inert, 1 unregistered spec | config files | HR §3 | C, dead values (SY-23) |
| 89 | `/hemo` commands | 18 branches plus `test` and `hemoverify` | 22 files | operators | HR §34 | D (SY-22) |
| 90 | WIP creative tab | 43 entries | `CreativeTabInit.java:59-102` | creative | HR §31.1b | NF, triage (SY-3) |
| 91 | Dev items | showcase, scanner, spawner, Chamber teleporter | ~1,000 lines | WIP tab | — | D (SY-22) |
| 92 | Dormant and orphan entities | 5 unregistered tendency mobs, 4 orphan types | — | renderers, lang | "dormant" | C (SY-24) |

---

## 2. Findings

Each finding: id, severity, claim, evidence, confidence, recommendation, effort, and whether it needs the user's call.
Open-question ids (D-, M-, C-, PT-) refer to `docs/OPEN_QUESTIONS.md`; they are cited, not restated.

### High

**SY-1 (High). Morphling incubation, the only Morphling source the design intends, can't be done in survival.**
- Evidence:
  - The Morphling Incubator is a Working craft that needs a Swollen Leech brazier offering: `D/recipe/cardinal_rite/working/morphling_incubator.json`.
  - Swollen Leech drops only from `D/loot_table/entities/leech.json`.
  - The `leech` entity has no biome modifier (none under `D/neoforge/biome_modifier/`), no biome JSON spawner and no Java spawn. Its only spawn path is breeding two existing leeches (`J/common/entity/mob/animal/LeechEntity.java:79`).
  - No structure template contains an incubator or a leech (scanned all 225 NBTs).
  - Court welcome gifts are vanilla items (`J/common/entity/npc/court/CourtHospitality.java:70-80`).
  - So the only other survival Morphling is the Hemopothecary villager's 1-emerald Marrowplume (SY-9).
- Confidence: Verified.
- Recommendation: finish. Give the leech a natural spawn (swamp, mangrove or river), or put the Swollen Leech in Bog-Body or Ossuary loot. Also cut the Hemopothecary Morphling trade.
- Effort: S. User's call: no. This probably also appears in the progression-blocker report.

**SY-2 (High). Three structure and offering blocks have no survival source, which blocks ten rites and crafts.**
- Evidence:
  - `suspended_cleansed_blood_crystal` has no recipe, no loot table and no placement, and `ConsecrationHandler` does not convert to it (`J/common/event/ConsecrationHandler.java:58-62`). Eight Unstained rites name it in their structure: `antiseptic_ground`, `clarity_ascension`, `lethean_font`, `lethean_judgment`, `lethe_covenant`, `moon_washed_copper`, `pale_consecration`, `pale_vigil`.
  - `engram_block` has no item source; it exists only in `qliphoth_fane.nbt`, which is used only by dormant MnA compat. The `D/recipe/mnemonist_cradle.json` recipe needs it, and that recipe gates the Mnemonist Puppet trial.
  - `suspended_blood_crystal` (an `enduring_pattern` brazier offering) is placed in two templates but has no loot table.
- Confidence: Verified for the first two; the third comes from the item census.
- Recommendation: finish. Pick a survival source for each, for example consecrating a placed Suspended Blood Crystal, an engram drop from a Qliphoth Fane, or self-drops.
- Effort: S. User's call: no (only which source).

**SY-3 (High). The WIP creative tab is a junk drawer that mixes three kinds of entry, so "WIP" no longer means anything.**
- Evidence: `J/common/init/CreativeTabInit.java:59-102` has 43 entries.
  - (a) Dev tools: `debug_showcase`, `structure_spawner`, `structure_scanner`, `chamber_of_will_teleporter`.
  - (b) Abandoned prototypes: the legacy trial blocks, the milestone-gate blocks, the hallway, the idols.
  - (c) Live, survival-reachable progression parts:
    - `hematic_suture_needle`: recipe plus an Artificer trade (`D/npc_trade/artificer/suture_work.json`).
    - `vascular_poultice`: a Vicar trade, an ingredient of the `vascular_effigy` Working craft, and the D5 Vein-Mason remedy.
    - `memory_thread`: Mnemonist trades and the `puppeteer_trial_mnemonist_puppet` offering.
    - `lethean_brew`: a Guardian trade and the Unstained purity source.
    - `draught_of_still_mercy`: the Annetta cure.
    - `unsigned_ancestral_ledger`: the bloodline founding rite.
    - `sanguine_blob`, `vitality_chalice` and `vascular_status_gauge`: Loom catalysts.
    - `semi_sentient_construct`, `visceral_mirror` and `sanguine_vigil`: Working crafts.
    - `gourdvine_tap` and `witness_organ`: crafting recipes.
  - The Creative Tab Sections work (HR §31.1b) sorted only the main tab.
- Confidence: Verified.
- Recommendation: finish. Triage the tab. Move (c) into the main sections, cut (b) through the cut list, and keep (a) on a dev-only tab, or gate it.
- Effort: S. User's call: partly; the filing of ambiguous items is a "decided without you" row already.

**SY-4 (High). Visceral Organs is a dangling loop: the effects are implemented, but no survival action raises an organ.**
- Evidence:
  - The Visceral Mirror ritual (D3, scalpel, a blood channel) hands out an organ echo (`J/common/tile/harbinger/functional/VisceralMirrorBlockEntity.java:120-180`, `:263-295`).
  - `OrganEchoItem` only shows a tooltip, dissolves duplicates and dissolves out of a container (`J/common/item/harbinger/OrganEchoItem.java`, 104 lines). Nothing consumes it.
  - `IVisceralOrgans.setOrganLevel` is called only by `/hemo organs set` (`J/common/command/OrgansSubcommand.java:85`).
  - The five effects in `VisceralOrgansEvents` (Spleen +1000 max blood, Liver, Lungs, Kidneys, Heart) therefore never trigger in play.
  - The Mirror is a survival Working craft (`D/recipe/cardinal_rite/working/visceral_mirror.json`) and an atlas entry. The echoes are on the WIP tab.
  - HR §20.8 and HR:5248 call the effects "implemented"; the Brazier upgrade route is "disabled".
- Confidence: Verified.
- Recommendation: decide. To cut: remove the Mirror craft, the echoes, the attachment and the command (S). To finish: an echo-to-level route, for example burning an echo in a lit Iron Brazier as a Working-rite offering, as the original design had (M).
- Effort: S to cut, M to finish. User's call: yes.

**SY-5 (High). Drudges are parked "post-alpha" but half-exposed: the hub can be crafted in survival, while the tools that use it can't.**
- Evidence:
  - HR:2867 says the Drudge system is "isolated in the WIP tab".
  - But the Semi-Sentient Construct is a Working craft (`D/recipe/cardinal_rite/working/semi_sentient_construct.json`) and a node on the Liber Rites & Crafts map (`J/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java:74`).
  - Birthing needs the Drudge Electrode (`J/common/block/harbinger/functional/SemiSentientConstructBlock.java:172-220`). Neither the electrode nor the Submission Device has a recipe.
  - The system still reaches outward:
    - `DrudgeActions` and `setDrudgeAction` hooks in manipulation classes;
    - `DrudgeTenderSource` in routing (HR:3044, 3070);
    - `frozen_cruor`, which only Drudges place;
    - 8 config keys.
  - About 2,300 lines in Drudge-named files, 29 files referencing Drudges.
- Confidence: Verified.
- Recommendation: decide. The mod isn't live, so cutting outright is cheap (M: entity, SSC, items, hooks, config, routing source). Finishing means recipes for both tools, a teacher line and a Liber page (M). Either way, take the SSC off the Liber map until it works.
- Effort: M. User's call: yes.

**SY-6 (High). An old-save compatibility layer survives in a mod that isn't live, against the user's 2026-10-29 ruling.**
- Evidence:
  - `J/common/capability/player/shared/migration/PlayerDataMigrations.java:43-64` runs 11 login migrations (fungal scar items, station upgrade claims, Thelemic priming, legacy Liber notes, Archon choice, circus root keys, chamber access, pending whispers, cooldowns, Artificer claims, Mnemonist starter), sunsetting 2027-04-23 and 2027-05-01.
  - Retired ids kept "for old save compatibility" (HR:1933): 15 retired manipulations plus 20 retired memory items (`ManipulationRetirementRules.java`, 206 lines) and 7 legacy `memory_living_*`.
  - `advanced_brewing` and `resonant_forge` attachments are kept only to migrate into `station_upgrades` (HR:820).
  - The legacy `dictation_table` block (`J/common/init/BlockInit.java:1057`, `D/recipe/dictation_table.json`; F-22) shows in the main tab.
  - `LegacyBloodStructureIds` aliases 20 old `blood_structure/*` ids, and the Liber map still uses those old ids (`HarbingerRecipeMapDefinitions.java:194`).
  - The legacy Field Notes migration (HR:3239), and the unregistered `FieldNotesItem` and `FieldNotesBookItem`.
  - The "legacy" `ambergris_cylinder` medium (HR §Clairaudiograph).
  - Several AGENTS.md "Documentation contract" bullets govern historical-save inference.
  - The user's memory note (2026-10-29) says no migration or aliases for replaced first attempts.
- Confidence: Verified.
- Recommendation: decide, then cut. Retire the migrations, retired ids and aliases in one change, rewrite the map ids, and update `PlayerDataMigrationGuardrailTest`, the persistent-data allow-list and the AGENTS.md bullets. The `PlayerDataVersion` machinery can stay for post-launch use.
- Effort: M. User's call: yes, because AGENTS.md mandates the pattern.

**SY-7 (High). 18 of 19 registered potions have no brewing route, and 9 of them belong to the Morphling strains the Court replaced.**
- Evidence:
  - `J/common/init/EffectInit.java:133-209` registers potions for `chitinous_bulwark`, `serpentine_guile`, `verminous_aura`, `luminous_dissipation`, `hemorrhagic_venom`, `venomous_resilience`, `echoic_perception`, `spined_barricade` and `burrowers_instinct`. HR:2432-2440 still labels these "Bat / Cuttlefish / Tick / Urchin / Centipede / Mole morphling effect".
  - Nothing in Java or data applies any of those effects, apart from the potions.
  - `registerEnzymeBrewingRecipes` (`EffectInit.java:333-342`) brews only vanilla potions plus `potion_of_mnemonic_whispers`.
  - The other nine custom potions (blood_binding, blood_loss, blood_rush, hemolysis, monolithic_dislocation, mycorrhizal_mending, sanguine_fertility, sanguine_siphon, arachnid_anastomosis) are also unbrewable.
  - Vanilla's creative potion tabs and JEI list every registered potion as potion, splash, lingering and tipped arrow: about 72 visible entries, and 240 lang keys.
  - Effects never applied by anything: `marked_by_canon` (its "failed extraction" design is gone; `MarkedByCanonEffect.java` header) and `chummed_waters` (Blood Chum uses an area manager instead).
  - `arcane_resonance` and `sanguine_clarity` are applied only by the excluded MnA compat.
- Confidence: Verified.
- Recommendation: cut the nine old-strain effects and their potions, `marked_by_canon` and `chummed_waters`. For the other nine potions, either brew them (S each) or cut them. Fix HR §14.
- Effort: S. User's call: only for which of the nine non-strain potions to keep.

**SY-8 (High). Rite spaces have no survival content: their one member is creative-only.**
- Evidence:
  - The rite-space system is `J/common/rite/zone` (14 files, about 1,045 lines) plus a sync packet and a renderer, with 15 `D/tags/{block,item,entity_type}/rite_bound/*.json` files.
  - 14 of the tags are empty. `block/rite_bound/world.json` holds only `hemomancy:sanguine_omen`.
  - The Sanguine Omen has no recipe and no placement; it has only a self-drop and the WIP tab. Its block does nothing but a sound and a screen overlay (54 lines).
  - D-10 asks which content should be rite-bound. This finding adds that today the answer is "nothing a player can reach".
- Confidence: Verified.
- Recommendation: decide. Either design one real rite-bound piece (for example a `world` rite's conjured object), or freeze the system behind its tags and drop the Omen.
- Effort: S to freeze, M to give it a member. User's call: yes (D-10).

**SY-9 (High). The Hemopothecary villager is half a system, and its trades undercut the Brined Court.**
- Evidence:
  - The profession and its POI (`J/common/init/VillagerInit.java:32-39`; the job block is the Scrying Podium) and 8 trades (`J/common/worldgen/village/VillageEvents.java:47-55`) are live.
  - The trades sell a Marrowplume Morphling for 1 emerald, Barbed Helm for 3, and Fervent Husk, Foul Paste and the Heart Pattern.
  - The village-house injection is a no-op ("should not be promised until this injection has a supported API path", `VillageEvents.java:133-137`), so the five `*_hemopothecary.nbt` houses never generate. They are orphan templates.
  - Any player who places a Scrying Podium beside an unemployed villager gets the trader. With SY-1, this is the only easy Morphling in survival.
  - It is also a third trade system beside NPC trade and Court customs.
- Confidence: Verified.
- Recommendation: decide.
  - Cut: profession, POI, trades, the five NBTs, and the Heart Pattern moved to a recipe or loot (S).
  - Or finish: houses through an access-widened pool, plus a trade list that sells only commons and never strains (M).
- Effort: S or M. User's call: yes.

### Medium

**SY-10 (Medium). Four quest frameworks do the same job with separate state, screens and packets.**
- Evidence:
  - The Harbinger assignment ledger is bespoke per assignment: each one adds a Progress record, a field on `OpenHarbingerAssignmentLedgerPacket` (276 lines, 20+ fields, `J/common/network/mission/`) and code in `HarbingerAssignmentLedgerScreen` (1,942 lines).
  - Unstained Observances have their own helper, state and screen (`J/common/mission/unstained/UnstainedObservances.java`, 276 lines; `BookOfObservancesScreen`, 319 lines).
  - Circus school quests (`CircusSchoolQuests`, 137 lines; `CircusCurriculum`) live in dialogue.
  - Court life is server state behind `CourtLife` (272 lines).
  - The Vein-Mason (`VeinMasonAssignments`) and the Artificer (`ArtificerAssignments`, 590 lines) are each their own sub-frameworks inside the ledger.
  - Proof storage is split three ways: attachments, advancements used as state (105 `hasAdvancement(` call sites) and `getPersistentData` root keys (411 call sites; S-1 in the architecture ledger).
- Confidence: Verified.
- Recommendation: merge, post-alpha. Use one assignment registry (step ids, a proof predicate, a presentation key) feeding one ledger screen with per-path skins. Do not start this before the play-proof pass.
- Effort: L. User's call: yes (timing).

**SY-11 (Medium). The Alembic has four overlapping potion ideas, and "advanced brewing" is thin.**
- Evidence:
  - Vanilla enzyme mixes: 8 (`EffectInit.java:333-342`).
  - Thelemic tinctures: 16 distillation recipes.
  - Advanced brewing has 3 recipes in total (`D/recipe/advanced_brewing/`: ferric_poise, fervent_reprisal, nerve_surge). Each re-brews a vanilla potion with a rider effect stored in persistent-data root keys (`J/common/brewing/AdvancedBrewEffects.java`, on the S-1 allow-list).
  - The 18 dead potions of SY-7.
  - The Morphic Tonic.
  - The Condenser tier is sold as "Advanced brewing (refine, compound)" (HR:805), and the Liber has a "refined_and_compound" page for three recipes.
- Confidence: Verified.
- Recommendation: decide.
  - Either grow advanced brewing to one rider per tendency, eight recipes (M).
  - Or fold the three riders into Thelemic tinctures and let the Condenser tier be capacity and speed only (S).
  - Either way, move the rider state into an attachment.
- Effort: S or M. User's call: yes.

**SY-12 (Medium). An abandoned "vanilla milestone gate" track left two inert craftable blocks and six unused tags.**
- Evidence:
  - Commit `4095ab296` (2026-05-05, "Feature 2: Vanilla milestone gates") added `crucible_of_nether_ichor` and `voidtouched_vessel` to gate the D3 and D5 rites.
  - Today both are plain `new Block(...)` (`J/common/init/BlockInit.java:1301-1311`) with crafting recipes, self-drops and WIP-tab entries. No rite, recipe or code consumes either.
  - Six of the 13 `D/tags/item/progression/*` tags are referenced nowhere: `dragon_proofs`, `early_blood_reagents`, `end_city_relics`, `judgment_relics`, `lethean_reagents`, `monolith_fragments`.
- Confidence: Verified.
- Recommendation: cut the blocks, their recipes, loot tables, models and lang, and the unused tags. If vanilla milestone gates are still wanted, that is a fresh design.
- Effort: S. User's call: no.

**SY-13 (Medium). The legacy Saint trial puzzle is still registered, and the reference still describes it.**
- Evidence:
  - `blood_pylon`, `blood_trial_altar` and `offering_gate` (`BlockInit.java:1288-1298`) and their block entities appear only in `D/structure/mausoleum/trialroom2way.nbt` and `trialroom4way.nbt`.
  - Those templates belong to the disabled generic `mausoleum` structure (M-6 proposes retiring it).
  - HR §5.8 (HR:1245) still gives Hemorath's trial as the "Four-basin blood-filling puzzle". The Chapels replaced it.
  - Keep `blood_basin`: Hemorath's live chamber uses it (`mausoleum/hemorath/chamber.nbt`).
  - Keep `hematic_construct` (the mob the old altar spawned): it is still Hemorath's live Downfall wave mob, as the default branch of the Chapel's wave switch (`J/common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java:396-401`). But it is a 50-line generic melee mob drawn with the vanilla zombie model and texture (`HematicConstructRenderer.java:12-15`). A D8 Saint Chapel spawns zombies in all but name, so this is a stub with surface.
- Confidence: Verified.
- Recommendation:
  - Cut the three trial blocks and their block entities together with M-6.
  - Give `hematic_construct` a real model on the jointed-rig generator, or switch Hemorath's waves to an existing iron-themed mob.
- Effort: S (cut), M (construct model). User's call: M-6 (default "Retire"); the construct look is the user's.

**SY-14 (Medium). Worldgen leftovers: one structure tries to place a template that doesn't exist, and nine templates have no placement.**
- Evidence:
  - The Crimson Lodge Annex has a live structure set, a structure JSON, a pool and a biome tag (dark forest and taigas). The pool element is `hemomancy:crimson_lodge_annex`, but no `.nbt` is committed (HR:4615 admits "Does not generate yet").
  - Orphan templates, referenced by no pool and no Java:
    - `tower/blood_tower_core`, `tower/blood_tower_top_1` (listed as structures at HR:4619);
    - `broken_unstained_church` (the tainted variant is used);
    - the five `*_hemopothecary` (SY-9);
    - `lowtide_ruin/kit_showcase` and `page_slab` (dev).
- Confidence: Verified, by scanning all 225 NBTs against pools and Java.
- Recommendation: cut the Lodge's set, structure, pool and tag, or author its template. Cut the Blood Tower and the old church NBT. Keep the lowtide dev kits only if a tool reads them.
- Effort: S. User's call: Lodge only (build or cut).

**SY-15 (Medium). The Non-Euclidean hallway spike touches core movement and collision for a creative-only block.**
- Evidence:
  - `J/common/world/fold/` (12 files), `NonEuclideanHallwayBlock` (121 lines), `NonEuclideanHallwayRenderer` (283 lines), a `MixinEntity` movement compression and a `MixinGameRenderer` hook, and `FoldedHallwayCollision`, which replaces player collision queries.
  - No recipe and no structure place it.
  - It is labelled "Puzzle / boss-room blocks Hemorath encounter" (`BlockInit.java:1273`), but no encounter uses it.
  - HR:5265 lists its known faults: F3 reveals it, multiplayer observers see nothing, projection is disabled.
- Confidence: Verified.
- Recommendation: cut from the branch, keeping it in git history or a side branch. A spike that mixes into `Entity` movement is a standing risk for a feature with no owner.
- Effort: S. User's call: yes.

**SY-16 (Medium). Adipocere crust worldgen is a subsystem built to serve one optional D8 offering.**
- Evidence:
  - The crust pass: `AdipocereCrustDecoration` (226 lines; queued per-chunk decoration of vanilla dungeons, strongholds and ancient cities), `AdipocereCrustSavedData`, `AdipocereCrustRules`, the `adipocere_crust` block, a density config, a review client and `adipocere_crust_validation` GameTests (`docs/ENDGAME_PATHS.md`, "Where it lives").
  - Adipocere's only use is the Rite of Possession's optional +3 Concentration offering (`D/recipe/cardinal_rite/possession_rite.json`), plus a 2x2 decor crust.
  - It already has three other sources: bog bodies (axe), bog revenants (35%), and the Alembic from honeycomb (`D/recipe/distillation/adipocere.json`).
- Confidence: Verified.
- Recommendation: merge. Keep adipocere as a material with its loot and Alembic sources. Either cut the crust decoration pass, or give adipocere two or three more uses (a candle or lantern tier, a Loom catalyst) so the worldgen earns its keep.
- Effort: S to cut, M to expand. User's call: yes.

**SY-17 (Medium). Possession is a complete mechanic with no reason to use it.**
- Evidence:
  - Each entry costs a Sanguine Quintessence and a bound surrogate, and risks Mind Broken (6000 to 12000 ticks).
  - The payoff is walking the Gardens as a facsimile and bringing back "anything found" (HR §5.6e, "Return").
  - A Silentium hears nothing from the hive (`HarbingerPathPermissions.hearsTheHive`), cannot attack players (D-9), and has no Gardens-only goal. D-3 (Silentium D8 powers) is open.
- Confidence: Verified for the mechanics; the "no goal" part is design judgement.
- Recommendation: decide alongside D-3. Give the Silentium one Gardens objective that only possession can reach, for example a material for the Silent Archon set or a mausoleum unlock. Otherwise accept possession as exploration-only and say so in the Liber.
- Effort: M to L. User's call: yes.

**SY-18 (Medium). A June 2026 cluster of vanilla-convenience items sits untaught on the WIP tab.**
- Evidence: commit `70c2b0a39` (2026-06-13) added these items, all built on `HematicMemoryToolItem` or plain `Item`:
  - Hearty Compass: a last-death compass, like the vanilla recovery compass, backed by the `last_death_memory` attachment.
  - Zombie, Desert and Spider Husk Effigies: temporary mob control.
  - Vein Spider: an item courier between two inventories.
  - Void Eye Organ: opens the ender chest for 35 blood.
  - Witness Organ: note recording and playback.
  - Vitality Chalice: no behaviour (a plain `Item`, only a catalyst).
  - Vascular Status Gauge.
  - Blood Chum (with `MixinFishingHook`, `ChummedWatersAreaManager`, `BloodChumEntity`) is the same kind of thing, and its docs, Liber and NPC coverage is zero.
  - None of the husk effigies, the Vitality Chalice or the Blood Chum has a line in HR, the lore doc, the wiki, the Liber or any dialogue tree.
- Confidence: Verified.
- Recommendation: decide.
  - Either adopt them as a "field kit" with one Alchemist or Artificer card and a Liber page (M),
  - or cut the convenience clones (the compass, the void eye, the witness organ, the blood chum and its mixin) and keep the two that serve blood magic (Vein Spider, the Gauge) (S).
- Effort: S or M. User's call: yes.

**SY-19 (Medium). Craftable or visible items do nothing.**
- Evidence:
  - Mind Spike: a crafting recipe (`D/recipe/mind_spike.json`), a Working craft (`D/recipe/cardinal_rite/working/mind_spike.json`) and a node on the Liber map. Its `use` returns success and does nothing (`J/common/item/harbinger/scar/ItemMindSpike.java`, 27 lines). Nothing consumes it.
  - `neutralizing_gasket`: a recipe and no consumer.
  - The Humane Idol's `use` only returns SUCCESS. The Serpentine Idol only toggles ACTIVE, with its old code commented out (`BlockSerpentineIdol.java:99-106`). Both are creative-only.
  - `SanguineBlobItem` (27 lines): its `use` fails. It is a real ingredient, so it is fine as a material.
- Confidence: Verified for the Mind Spike and the idols; the gasket comes from the item census.
- Recommendation: cut the Mind Spike's two recipes and map node, or give it its scar-tool role. Cut the idols. Cut the gasket unless a station upgrade wants it.
- Effort: S. User's call: Mind Spike only.

**SY-20 (Medium). The Silthmere Glaive and the Hemorath rib are visible, but nothing grants them.**
- Evidence:
  - The Silthmere Glaive is in the main tab, at purity 50 in the Unstained atlas (`MaterialAtlasSpec.java:441`), has a Liber discovery hook (`LiberDiscoveryEvents.java:90`) and a Guardian item inquiry (`D/dialogue_inquiry/guardian/hemomancy/silthmere_glaive.json`), and is named in lore and in three wiki pages as a sanctioned weapon.
  - It has no recipe, no loot and no grant.
  - `hemorath_rib`, `gilded_venous_stone` and `suspended_vivianite` are in the same state.
  - By contrast, the Vestment of the Final Molt is granted by `UnstainedAdvancementGranter.java:84`.
- Confidence: Verified for the Glaive; the item census reports the others.
- Recommendation: finish. Grant the Glaive at a clarity stage, or give it a Pallid Retort recipe. Give the rib a Hemorath drop.
- Effort: S. User's call: which reward.

**SY-21 (Medium). The Morphling Cradle is taught in the Liber but can't be obtained.**
- Evidence:
  - `MorphlingCradleBlockEntity` (435 lines: owner binding, bloodline support, leech buffer, Primal host rules) and the `cradlePrimal` column in the `MorphlingStrains` table.
  - HR §16.4 (HR:2712) presents it as live, and the Liber page `D/books/libersanguinium/the_hematic_order/pages/morphling_cradle_and_nectar.json` teaches it.
  - It has no recipe, no Working craft and no structure placement; it has only a self-drop and the WIP tab.
- Confidence: Verified.
- Recommendation: finish. Add a Working craft (making, lesser, D5) and move it to the main tab.
- Effort: S. User's call: no.

**SY-22 (Medium). Dev fixtures ship to every operator, and one dev command ships to every player.**
- Evidence:
  - `/hemo` has one permission-2 gate (`J/common/command/HemoCommand.java:22`; its javadoc calls it "debug and test controls", `:8`). Branches that place structures or force progression have no production gate:
    - `vesper fight` sets degree 7 Silent and grows a pruned tree (`VesperSubcommand.java:40-49`);
    - `qliphoth tree/archon/pome` (`QliphothSubcommand.java:162-204`);
    - `mycophant fight/rematch`;
    - `chamber` overrides, which are saved to world data (`ChamberOfWillManager.java:674-697`);
    - `fane preview`.
  - `/hemocastpreview` is a client command with no permission check (`J/client/event/CastingPreviewCommands.java`).
  - The WIP-tab dev items (`debug_showcase`, 541 lines; `structure_scanner`, 349; `structure_spawner`; `chamber_of_will_teleporter`, ungated) are in the jar.
  - `/hemo test` and `/hemoverify` are properly gated: they are in `src/gameTest`.
- Confidence: Verified.
- Recommendation: decide. Move fixture branches under `/hemo test` (gameTest source set) or behind an `FMLEnvironment.production` check. Move `/hemocastpreview` to the review harness. Update `HemoCommandTreeTest` and its snapshot. M-5 is the same call for `/hemo test mausoleum`.
- Effort: S. User's call: yes.

**SY-23 (Medium). Config options promise behaviour that doesn't exist.**
- Evidence:
  - Read nowhere: `morphling.husbandryStageQuota` (`HemoServerConfig.java:348-350`, marked "Legacy") and `powergen.renderMorphlingFeedingAnimation` (`HemoClientConfig.java:84-86`). Tests pin both.
  - Inert: `cast_outpouring.realEffectMinRank`, because `OutpouringWorldEffects.effect()` always returns `NONE` (`OutpouringWorldEffects.java:36`).
  - Unregistered spec: `HemoMnAConfig` (15 values) is never registered (`J/config/HemoConfig.java:15-31`), yet core `ManipulationCostLedger.java:65-71` reads `ARCANE_RESONANCE_BLOOD_REDUCTION`, which would be null if MnA were installed.
  - The client HUD section is named `powergen` ("power generator").
  - The `willsEnabled` comment claims it controls bends and anchors; it doesn't.
- Confidence: Verified.
- Recommendation: cut the two dead values and the inert one, until an outpouring effect exists. Move the MnA read into `compat/`. Rename the comment; leave the section key, unless configs can be reset (the mod isn't live).
- Effort: S. User's call: no.

**SY-24 (Medium). Orphan entity types and dormant mob classes are still registered or still present.**
- Evidence:
  - Registered but never spawned:
    - `wretched_will` (`EntityInit.java:1011`; asserted absent from Blood Rush by a GameTest);
    - `dark_arrow` (`:1016`);
    - `tracking_pests` (`:933`) and `tracking_snake` (`:914`), whose classes call `EntityInit.x.get()` in static initialisers.
  - Five unregistered tendency mobs keep model, renderer, texture, sounds and lang: Cruor Fiend, Void Drinker, Frozen Clot, Abyssal Siphon, Synapse Hound (`J/common/entity/mob/monster/`). `TendencyMobSpawnRulesSourceTest` pins them unregistered. `A/models/item/frozen_clot.json` is left over.
- Confidence: Verified for the files; spawn sites come from the entity census.
- Recommendation: cut. This is also dead-code territory; listed here because each is a whole content stub (mob, art, sound, lang).
- Effort: S. User's call: no.

### Low

**SY-25 (Low). Court NPCs run two "how well they know you" ladders.**
- Evidence:
  - House standing: stranger, guest, friend (`CourtStandingRules.java:15`).
  - Per-NPC rapport: Stranger, Known, Trusted, Confidant, with Court rapport lore lines (`DialogueRapportLore.java:63-67`).
  - The custom card pays rapport and the dialogue offering does not (PT-56).
- Confidence: Verified.
- Recommendation: merge. Derive Court rapport from house standing, or exclude the `COURT` side from rapport.
- Effort: S. User's call: yes (PT-56).

**SY-26 (Low). The Dynasty Castle is frequent world content with no loop.**
- Evidence:
  - Spacing 24, separation 8 (`D/worldgen/structure_set/dynasty_castle.json`), 15 templates.
  - The templates have no chest, barrel, spawner, lectern or discovery inscription (scanned).
  - There is no NPC, Liber or advancement hook. LORE_REFERENCE line 19 forbids expanding it into canon.
- Confidence: Verified.
- Recommendation: decide. Give it loot and one discovery inscription, raise its spacing, or cut it.
- Effort: S. User's call: yes.

**SY-27 (Low). Molting and Chimerization are plans with no code.**
- Evidence:
  - Grep finds no `molt`/`chimer` code apart from `vestment_of_the_final_molt` and `scar_chimera`.
  - `docs/ENDGAME_PATHS.md:97` calls molting "a stub only"; there is no stub.
  - D-5 and D-6 are open.
- Confidence: Verified.
- Recommendation: decide. Strike both from the D8 Apotheos scope until designed, and fix the "stub" wording.
- Effort: S (docs). User's call: yes.

**SY-28 (Low). The Gourdvine Tap is a draft machine that survival players can craft.**
- Evidence:
  - It has a recipe (`D/recipe/gourdvine_tap.json`), a 252-line block entity and four growth stages, and it sits on the WIP tab.
  - HR §38 calls it a "Partial: Draft".
  - It overlaps the Blood Gourd fill recipes and the Cradle's circulation income.
  - There is no Liber page or teacher.
- Confidence: Verified.
- Recommendation: decide. Promote it with a Liber line, or cut it.
- Effort: S. User's call: yes.

**SY-29 (Low). The Sanguine Vigil is a D6 grand Working craft whose product is parked on the WIP tab.**
- Evidence:
  - `vascular_effigy` (HR:3928, 6,500 blood plus Quintessence) produces `sanguine_vigil`, a hostile-binding ward (`SanguineVigilBlockEntity`, 171 lines).
  - The block is on the WIP tab and has no Liber page or dialogue line.
- Confidence: Verified.
- Recommendation: finish. Move it to the main tab and add one Artificer or Vicar line.
- Effort: S. User's call: no.

**SY-30 (Low). The Antecedent inquiry is a long optional track with a weak payoff.**
- Evidence:
  - It includes colloid sampling, microscopy, Alchemist replay, a Vicar interpretation, three recordings, a Vigil worldgen with `MixinVigilTerrain`, 158 clairaudiograph sound data files and 14 Liber pages.
  - The end reward is the Listening Scar: a directional cue on sculk-sensor activation, "neither Warden tracking nor protection" (HR §Listening Scar).
  - The 2026-10-27 canon now says sculk and the Antecedent touch neither D7 nor D8 (`docs/ENDGAME_PATHS.md`).
- Confidence: Verified (the reward); design judgement (weak).
- Recommendation: decide, low priority. Keep it as finished side content, or give the Scar one practical use.
- Effort: S. User's call: yes.

**SY-31 (Low). Unstained parity: the rites sit on an older grammar and the top-tier traits are only planned.**
- Evidence:
  - The 18 Unstained rites run through their own events and saved data (`J/common/rite/unstained/`, 8 files, 1,336 lines) on the pre-purpose rite form.
  - The Fully Purified traits are "Planned" (HR:1714-1718).
  - Our Lady's presence is "Partial".
  - D-11 parks the Unstained ritual system.
  - Eight of the 18 rites are blocked by SY-2.
- Confidence: Verified.
- Recommendation: decide the D-11 timing. Until then, fix SY-2 so the existing rites at least work.
- Effort: L for the system. User's call: yes.

**SY-32 (Low). Blood Infusion has a full visual stack for two recipes.**
- Evidence:
  - `D/recipe/blood_infusion/` holds stone to venous stone and glass to sanguine glass.
  - Behind them are `BloodInfusionManager` (173 lines), the `blood_infusion_veins` shader, a melt overlay and `runInfusionReviewClient`.
  - HR §25.9 notes "additional entries can use the same route".
- Confidence: Verified.
- Recommendation: finish. Add four to six more block pairs from existing families (bone bricks, hematic iron, cleansed/stained stone for the Unstained inverse).
- Effort: S. User's call: no.

**SY-33 (Low). Two recipe serializers have no recipes.**
- Evidence: `morphling_jar_upgrade` (`CopyMorphlingJarRecipe`) and `blood_gourd_upgrade` (`CopyBloodGourdRecipe`) are registered (`J/common/init/RecipeInit.java:68-72`), and no JSON under `D/recipe` uses either type.
- Confidence: Verified for the missing JSON. Whether jar or gourd tier upgrades were meant to be craftable is Suspected.
- Recommendation: decide. Add the upgrade recipes, or cut the serializers.
- Effort: S. User's call: no.

---

## 3. Stubs with surface (item 3 of the brief, collected)

| Visible surface | How thin | Finding |
|---|---|---|
| ~72 potion, splash, lingering and tipped-arrow entries in vanilla tabs and JEI | 18 of 19 potions unbrewable; 9 belong to deleted strains | SY-7 |
| Visceral Mirror Working craft, 5 organ echoes, atlas entries | echoes do nothing; levels only by command | SY-4 |
| Semi-Sentient Construct on the Liber map | electrode has no recipe | SY-5 |
| Morphling Cradle Liber page | no recipe | SY-21 |
| Mind Spike: recipe, Working craft, map node | `use` does nothing | SY-19 |
| Silthmere Glaive: main tab, atlas, Liber hook, inquiry | nothing grants it | SY-20 |
| Hemorath's Downfall waves (a D8 Chapel) | `hematic_construct` drawn with the vanilla zombie model | SY-13 |
| Rite spaces (HR §5.3, Liber map tints) | the only member is creative-only | SY-8 |
| `realEffectMinRank` config | every effect is a no-op | SY-23 |
| Crucible of Nether Ichor, Voidtouched Vessel recipes | plain blocks, nothing uses them | SY-12 |
| Dictation Table in the main tab | a conversion shim | SY-6 |
| Hemopothecary trades | the houses never generate | SY-9 |
| Vitality Chalice recipe | plain item | SY-18 |
| Crimson Lodge Annex structure set | no template | SY-14 |

## 4. Underexposed depth (item 4, collected)

These have real implementation but no surface that points a player at them. The smallest exposure is given for each.

| Feature | What exists | Missing surface | Smallest exposure |
|---|---|---|---|
| Sanguine Vigil | ward block, D6 grand Working craft | Liber page, teacher, main tab | main tab, plus one Artificer line (SY-29) |
| Gourdvine Tap | passive blood plant, growth stages | Liber, teacher, main tab | one Liber line, if kept (SY-28) |
| Blood Chum | fishing boost with a mixin | any text but its name | an Alchemist inquiry, if kept (SY-18) |
| Husk Effigies, Vein Spider, Hearty Compass | working tools | any docs or teacher | one field-kit Liber page (SY-18) |
| Morphling Cradle | 435-line host station | an acquisition route (the inverse case) | a Working craft (SY-21) |
| Heart-node, possession, Apotheos body | full D8 systems | no Liber section (known: C-8, ENDGAME "Proof owed") | an Apotheos and Silentium Liber chapter with D-1 |
| Advanced brewing | 3 rider brews | a teacher line names the Condenser tier but not the riders | resolve SY-11 first |

Earthen Veins looked underexposed but aren't: the Vicar teaches the Rooted Vein
(`J/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java:350-365`).

## 5. Scope-creep candidates (item 5)

| Track | Acquire | Use | Reward | Progression tie-in | Loop |
|---|---|---|---|---|---|
| Crimson Troupe | worldgen pavilion, D4 referral | puppetry lessons, practicals | bodies, Pattern, Topper | D3 to D5 puppetry | complete (D-18 open) |
| Brined Court | discovery routes | standing, lesson, Champion | strains, title | D5, Morphlings | complete, but strain supply broken (SY-1) |
| Pelagic | worldgen | sampling, specimens | materials | Court habitat | complete |
| Missions / ledger | NPCs | assignments | kits, proofs | D1 to D6 | complete, bespoke (SY-10) |
| Encounters (Chamber, Vesper, Mycophant, Vagrant Mind) | rites, worldgen | fights, visits | memories, Tendril, D8 | D6 to D8 | complete |
| Possession | D8 rite | facsimile body | "anything found" | none | dangles (SY-17) |
| Adipocere | 5 sources | 1 optional offering | +3 Concentration | none | over-sourced (SY-16) |
| Visitation | D8 rite | travel | heart-node landing | Apotheos | complete (D-8) |
| Molting, Chimerization | — | — | — | — | not built (SY-27) |
| Engram weaving | Loom | octagram puzzle | memories | D3 gate | complete |
| Skill tree | skill points | Java-owned branches | stats, toggles | all degrees | complete; the tool writes Java markers |
| Staff fittings | Artificer claims | 7 fittings | Staff visuals and forms | D3 to D7 | complete; `tools/staff_fittings` outputs are the shipped `living_staff_*_fitting` models |
| Book kit | — | HutosLib book assets | fonts, page art | Liber | complete; `tools/book_kit` writes runtime assets |
| Dialogue editor | — | dev studio | — | — | live tool; it reads the Java trees; the JSON exports in its folder are snapshots, not wired |
| Antecedent | D3 to D5 | inquiry chain | Listening Scar | none | complete, weak payoff (SY-30) |
| Dynasty Castle | worldgen | — | nothing | none | dangles (SY-26) |

## 6. Config and commands (item 6)

- **Config.** 133 values across three registered specs: 98 server, 7 common, 28 client. Of these:
  - Two are dead and one is inert (SY-23). The 15-value MnA spec is unregistered (SY-23).
  - `willsEnabled` controls only ambushes; bends and Commandeer have their own toggles.
  - The common worldgen switches are real: Pelagic, Phlegethontic, Overworld Gardens and Cortical Drift. But `enableOverworldFungalGardensRegion` lacks `worldRestart()`, and its weight is hard-coded (`HemoCommonConfig.java:28-30`, `BiomeInit.java:452`).
  - There is no config screen.
- **Commands.** 18 `/hemo` branches.
  - Admin tools a server owner would use: blood, bloodline, degree, morphling, skills, unstained, organs, tendency, bloodmoon, summons, manipulations, slots.
  - Dev fixtures that ship ungated: qliphoth, fane, chamber, will, mycophant, vesper (SY-22).
  - `/hemo slots equip/unequip` doesn't sync to the client and returns 1 on failure (`SlotsSubcommand.java:76,89`); this is a bug, not a design item.
  - `/hemowhisper` is a legitimate player command (a chat click target).

## 7. Documentation debt as a signal (item 7)

20 sections sampled. "Code ahead" means the doc understates what is built. "Doc ahead" means the doc claims more than the
code does.

| Section | Doc says | Code | Direction |
|---|---|---|---|
| HR §38 Saints (HR:5285), §5.8 Release scope (HR:1250) | post-release WIP, no natural placement | four `mausoleum_<saint>` structures in the live `saint_mausoleums` set | code ahead (C-1) |
| HR §5.8 Known Saints, Hemorath row (HR:1245) | four-basin puzzle | Chapels; the basin puzzle is legacy (SY-13) | doc stale |
| HR §38 Fungal Dimension (HR:5288) | Partial, terrain WIP | Gardens biomes, network, facsimiles, hunters, pools | code ahead |
| HR §38 Annetta (HR:5290) | Partial | fight complete per `BOSSES.md` | code ahead (C-7) |
| HR §38 Partial "Progression/Liber Java renderer", HR:4878 | `HemoProgressionScreen.setupEntries()` commented out | the class no longer exists | doc stale |
| HR §38 Planned "voyager trade expansion" (HR:5241) | planned | Voyager and Wayfarer trade cards ship | code ahead (partly) |
| HR §38 Planned "forced manipulation rank-up rituals" | planned | none | consistent |
| HR §18 Drudges (HR:2867) | isolated in the WIP tab | SSC is a Working craft on the Liber map | doc ahead in isolation, code ahead in exposure (SY-5) |
| HR §16.4 Morphling Cradle (HR:2712) | live support station | unobtainable | doc ahead (SY-21) |
| HR §20.8 / §38 Visceral Organs (HR:5248) | effects implemented | unreachable in survival | doc ahead (SY-4) |
| HR §14 effects (HR:2432-2440) | bat, mole, cuttlefish... morphling effects | strains deleted, nothing applies them | doc stale (SY-7) |
| HR §14 intro (HR:2411) | most effects have potions | 18 of 19 unbrewable | doc ahead |
| HR §29 Blood Tower (HR:4619) | structure | orphan templates | doc ahead (SY-14) |
| HR §29 Crimson Lodge (HR:4615) | does not generate yet | set live, template missing | consistent, but the code still tries |
| HR §30 Villagers | injection no-op | same | consistent |
| HR §38 Entity loot (HR:5243) | 44 files | 86 files | doc stale |
| HR §5.4 inquiry count | 317 | 385 | doc stale (known, NPC topic doc) |
| HR §11 Skill tree (HR:2146) | editor "ignores the old skilltrees folder" | the folder is gone | doc stale (trivial) |
| HR §38 Gourdvine Tap, Non-Euclidean Hallway, Founding Fane, Our Lady | Partial | matches | consistent |
| HR §3.1 `realEffectMinRank` (HR:464) | every effect a no-op | matches | consistent (the option itself is SY-23) |

Tally: 10 sections where the doc lags the code (stale or understated), 5 where the doc overclaims, 5 consistent. The
pattern: **finished work is under-reported in HR §38** (the October passes never went back to the roll-up), while
**parked or abandoned systems are over-reported** in their own sections, which still read as live. HR §38 is the single
most misleading section; rewriting it as a short table pointing at the topic docs would remove most of the drift. The
reference also opens with thirteen dated changelog sections before its table of contents (HR:3-145) and ends with seven
appendix sections after §38 (HR:5324-5496). Both belong in the topic docs or the archive.

---

## 8. Proposed cut list (ordered)

| # | Cut | Why | Effort | User's call |
|---|---|---|---|---|
| 1 | Pre-Court Morphling effects and potions, `marked_by_canon`, `chummed_waters` (SY-7) | first attempt replaced by a ruling; ~72 junk creative entries | S | no |
| 2 | Crucible of Nether Ichor, Voidtouched Vessel, six unused progression tags (SY-12) | abandoned gate design, inert blocks | S | no |
| 3 | Legacy mausoleum and the three trial puzzle blocks; keep `blood_basin` and `hematic_construct` (SY-13) | replaced by the Chapels | S | M-6 (default retire) |
| 4 | Orphan templates: Blood Tower, `broken_unstained_church`; the Lodge set unless built (SY-14) | worldgen noise, a missing template | S | Lodge only |
| 5 | Orphan entity types and the five dormant tendency mobs (SY-24) | registered stubs with art and lang | S | no |
| 6 | Dead and inert config values; move the MnA read into compat (SY-23) | options that lie | S | no |
| 7 | Mind Spike recipes, the idols, the Neutralizing Gasket (SY-19) | craftable no-ops | S | Mind Spike |
| 8 | Save-compatibility layer: 11 migrations, 42 retired ids, the Dictation Table, `LegacyBloodStructureIds`, legacy Field Notes (SY-6) | the mod isn't live (2026-10-29 ruling) | M | yes (AGENTS.md) |
| 9 | Non-Euclidean hallway spike and its mixin hooks (SY-15) | core-movement risk, no owner | S | yes |
| 10 | Visceral Organs, if not finished (SY-4) | dangling loop | S | yes |
| 11 | Drudges, if not finished (SY-5) | parked, half-exposed | M | yes |
| 12 | Hemopothecary villager, if not finished (SY-9) | half-built, undercuts the Court | S | yes |
| 13 | Adipocere crust decoration pass (SY-16) | worldgen for one optional offering | S | yes |
| 14 | Vanilla-convenience memory tools and Blood Chum, if not adopted (SY-18) | untaught clones | S | yes |
| 15 | Molting and Chimerization from the D8 plan, until designed (SY-27) | plans with no code | S (docs) | yes |

## 9. Proposed finish list (ordered)

| # | Finish | Why | Effort | User's call |
|---|---|---|---|---|
| 1 | Leech natural spawn or Swollen Leech loot (SY-1) | Morphling incubation is impossible today | S | no |
| 2 | Survival sources for the Suspended Cleansed Blood Crystal, the Engram Block and the Suspended Blood Crystal (SY-2) | 8 Unstained rites, the Mnemonist Puppet trial and `enduring_pattern` are blocked | S | no |
| 3 | WIP-tab triage: promote the live progression parts (SY-3) | live items look unfinished | S | partly |
| 4 | Morphling Cradle Working craft (SY-21) | taught but unobtainable | S | no |
| 5 | Sanguine Vigil out of WIP, plus one teacher line (SY-29) | a D6 craft hides its product | S | no |
| 6 | HR §38 rewrite and the §7 drift fixes | the roll-up misleads both ways | S | no |
| 7 | Silthmere Glaive and Hemorath rib sources (SY-20) | lore-named gear with no source | S | which reward |
| 8 | Gate the dev fixtures and `/hemocastpreview` (SY-22) | operators can corrupt progression by accident | S | yes |
| 9 | Blood Infusion entries (SY-32) | cheap content on a finished pipeline | S | no |
| 10 | A real model for `hematic_construct`, Hemorath's Downfall mob (SY-13) | a D8 Chapel spawns a zombie lookalike | M | look only |
| 11 | Advanced brewing: grow to eight or fold into tinctures (SY-11) | three recipes behind a station tier | S to M | yes |
| 12 | Court rapport derived from standing (SY-25) | two ladders on one NPC | S | yes (PT-56) |
| 13 | Visceral Organs echo-to-level route, if kept (SY-4) | effects exist, the route is missing | M | yes |
| 14 | Silentium Gardens objective with D-3 (SY-17) | possession has no payoff | M to L | yes |
| 15 | Dynasty Castle loot and hook, or spacing (SY-26) | world content with no loop | S | yes |
| 16 | One assignment framework, post-alpha (SY-10) | four parallel quest systems | L | yes (timing) |

---

## Not a problem

These looked suspicious and checked out:
- **Staff fittings tool.** `tools/staff_fittings/gen_staff_fittings.py` writes the `living_staff_*` models and textures that `A/models/item/living_staff.json` overrides by `hemomancy:staff_visual` 0-15; the fitting items are registered and claimed through the Artificer.
- **Skill-tree editor.** It rewrites Java between `// <skill-editor>` markers; the old `skilltrees` JSON folder is gone, so there is no shadow data.
- **Book kit.** Its outputs are the shipped HutosLib fonts and page art.
- **Dialogue editor.** It reads the live `*DialogueTrees.java`; the JSON files in its folder are exports, not runtime data.
- **Ichorian sigils.** All nine have ordeal behaviour (`CardinalRiteOrdealEngine.java:590-609,767`, `CardinalRiteInteractionHandler.java:588-590`, Reservoir in `CardinalRiteCompletion.java:214`), not only Reservoir.
- **Earthen Veins.** Taught by the Vicar's Rooted Vein node (`HarbingerVicarDialogueTrees.java:350-365`).
- **Muscle memory against Thelemic.** One system under two names: code ids versus doc vocabulary, not two systems.
- **Two `Standing` enums.** `CourtMemberDialogue.Standing` is a presentation view over `CourtStandingRules.Standing`, not a second save.
- **Circus acclimation.** A perception score, not a reputation bar (the Circus topic doc forbids one); it does not overlap rapport.
- **`blood_basin`.** Still used by Hemorath's chamber (`mausoleum/hemorath/chamber.nbt`) and his generator theme; keep it when the trial blocks go.
- **Antecedent Vigil templates.** Loaded by computed path in `VigilPlacement.java:125-131`, not orphaned.
- **Vestment of the Final Molt.** Granted by `UnstainedAdvancementGranter.java:84`.
- **`spectral_companion`.** It points at `PaleIntercessionEntity` on purpose; Misbegotten is a flag on `ProfessionalHarbingerEntity`, not a missing entity.
- **Facsimiles.** Spawn naturally in seven Gardens biomes.
- **`NoopRenderer` on `morphling_field` and `crimson_fireball`.** Intentional; they draw through world effects.
- **Banner patterns.** Both work: `heart_pattern` through the villager (see SY-9 if that goes), `veins_pattern` by recipe.
- **Bloodline recruitment beside Hematic Succession.** Different jobs: borrowing a live outpost NPC versus manifesting a successor. PT-85 covers the friction.
- **Placed features.** All 40 are referenced by a biome, a modifier or Java.
- **`/hemo test` and `/hemoverify`.** Live in the gameTest source set, which the jar does not package.
