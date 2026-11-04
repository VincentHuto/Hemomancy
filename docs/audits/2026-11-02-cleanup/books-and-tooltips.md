# Books, tooltips and advancement text audit (2026-11-02)

Read-only audit of the two HutosLib guide books, the Liber Rites & Crafts map, manipulation and rite descriptions,
item tooltips and advancement text against code and data. Nothing was built, run or edited. This was the Area 6
sub-audit of [docs-and-test-drift.md](docs-and-test-drift.md), delivered after that report closed, so it stands on its
own here. Ids are `BK-n`.

Paths: B = `src/main/resources/data/hemomancy/books/libersanguinium`, BI = `.../books/liberimmaculatus`,
L = `src/main/resources/assets/hemomancy/lang/en_us.json`, MI = `src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java`.

## Structure

- Two HutosLib guide books. Page text is inline English in `hutoslib:page` JSON; no page references a lang key.
  - `hemomancy:libersanguinium`: 10 chapters, 102 pages (alembic 4, centrifuge 2, cosmic_forces 9, intro 4,
    manipulations 14, resonant_forge 3, tendency 12, the_hematic_order 22, the_infection 29, the_unstained_path 3), one
    glossary term (`glossary/the_cut.json`).
  - `hemomancy:liberimmaculatus`: 4 chapters, 19 pages (intro 3, our_lady 4, sacred_tools 7, the_path 5).
- Page unlocks are wired in `common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java`; every
  entry path and rite path it names exists.
- The `blood_structure_page` serializer is registered at `Hemomancy.java:141` but no book JSON uses it.
- The Rites & Crafts map (`client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java`): 56 rites in 11
  families matching all 56 non-Unstained `recipe/cardinal_rite/*.json`; 29 crafts (9 floorless Blood Structures and 20
  `working/` crafts); floor families per purpose plus Ichorian Sigils. No map id lacks a recipe file, no Harbinger rite
  or craft is missing from the map, all 66 book `icon` ids resolve.
- Other Liber text comes from lang: `hemomancy.manipulation.<id>.desc`, `hemomancy.rite.<id>.desc` (falling back to
  the JSON `riteDescription`), and the bestiary (specimens and morphlings only).

## Findings

| Id | Sev | Claim | Conf. | Action | User call |
| --- | --- | --- | --- | --- | --- |
| BK-1 | High | The Second Fraction page says the upgrade costs 550 mL; the code prices it at 602 mL | Verified | fix book | No |
| BK-2 | Medium | Every tier-2 upgrade page says "fill eight anchors, seal, close circuits"; the code wakes ring 0 only and the outer ring sleeps until the station projection begins | Verified | fix book | No |
| BK-3 | High | The Bloodwork Stations page describes the retired hand activation of the Scarlet Vanity ("copper heart") and the Fungal Implantation Pylon; both are Working crafts now | Verified | fix book | No |
| BK-4 | Medium | The Enzymatic Scriptorium page reads like a hand-activated structure; it is a D3 Greater Working craft with a floor and Focus | Verified | fix book | No |
| BK-5 | High | The Fervent page has wrong costs (Pyretic Forge 350 vs 150, Cauterizing Rebuke 275 vs 150, Vitric Combustion 500 vs 350) and Vitric Combustion is CHARGED, not QUICK | Verified | fix book | No |
| BK-6 | High | The Vivacious page mistypes four manipulations (Blood Rush, Blood Needle, Deadly Gaze, Blood Aneurysm), files three under the wrong tendency (Blood Needle is Ferric, Deadly Gaze is Ductilis/Tenebris, Crimson Flame is Flammeus) and lists Summon Avatar as QUICK 500 where it is PASSIVE 75 with staged costs | Verified | fix book | No |
| BK-7 | Medium | The Ferric page calls Sanguine Mending QUICK; it is CONTINUOUS | Verified | fix book | No |
| BK-8 | Medium | The Ruinous page still has placeholder costs ("Hemorrhage (QUICK, ?)", "Exsanguinate (QUICK, ?)"); code says 75 and 300 | Verified | fix book | No |
| BK-9 | Medium | The manipulations overview lists three ranks; the enum has five (MAGISTER, PERFECTUS), and the weave pages name them | Verified | fix book | No |
| BK-10 | Medium | Blood Binding shows a raw lang key in the Liber Manipulations tab: `hemomancy.manipulation.blood_binding.desc` is the only missing `.desc` of 123 | Verified | add lang | No |
| BK-11 | Medium | The Initiate Rite is described as granting Degree 3 but has `rankup: false` (same as PG-11) | Verified | decide | Yes |
| BK-12 | Low | The Virid Salis page calls D5 "Perfected"; D5 is "Illuminatus of the Crimson Lodge". `ViridSalisTrailHandler.java:109` also compares a degree with a bare literal instead of `ProgressionGates` | Verified | fix book, fix code | No |
| BK-13 | Medium | The `scars_of_the_mind` advancement says "Obtain a Scar Binder"; the criterion is `scar_pattern` and no `scar_binder` item exists | Verified | fix lang | No |
| BK-14 | Medium | Four hidden advancements have a display but no parent and become their own tab when earned (same as RS-15); `first_bloodcraft_reward_claimed` also has no title or description key | Verified (tab rendering Suspected) | fix data | No |
| BK-15 | Medium | The empty Morphling Jar tooltip uses `hemomancy.jar.empty`, missing from lang (same as RS-2) | Verified | fix lang | No |
| BK-16 | Medium | `ConsecrationHandler` uses `hemomancy.consecration.not_ready` / `.success`, both missing from lang (same as RS-2) | Verified | fix lang | No |
| BK-17 | Medium | The Anchorite's Mason's Effigy inquiry is keyed `masons_effigy`; the block is `mason_effigy` (same as DL-25). `dialogue_inquiry/vicar/hemomancy/blood_structure_hint.json` targets an unregistered item | Verified | rename file | No |
| BK-18 | Low | First Clotting and Druse Crystallization have no `hemomancy.rite.<id>.name/.desc` keys and their pages lack `"decal": false`; Druse's description promises a collapse fix the Latticed tier already has | Verified | fix data | No |
| BK-19 | Low | The Neurotic page omits that Axonal Transduction (PERFECTUS) needs loom stage 2 (`SomaticWeaveRules.java:53-58`) | Verified | fix book | No |
| BK-20 | Low | Naming drift: "Puppeteering Thread" (item is Enthralling Filament), "Blood Crafting key" (label is Activate Blood Construct), Canon Memories "of tremendous cost" (Crimson Tithe costs 100 mL), "Perform the Adept Rite" (Rite of the Sanguine Brotherhood), Archon "the highest degree" (D8 exists), Hematic Iron Block "using a blood structure recipe", "any Hematic Memory" (criterion lists 15 of ~120) | Verified | fix text | No |
| BK-21 | Low | Orphan lang: Field Notes tooltip keys (code uses keys missing from lang, lang holds keys for the unregistered item), Scar Binder keys and `key.hemomancy.scarbinderpickup.desc`, `recipe_map_family.ritual_floors`, 16 `hemomancy:components/*` MnA keys | Verified | fix lang (MnA keys: decide with B-4) | MnA only |
| BK-22 | Low | Recipe map: the seven Puppeteer ordeals split across "Puppetry" (4) and "Trials" (3); `sanctified_rite` and `armature_consecration` share one icon; Marrow naming splits three ways (Juggler / Spitter Carriage / Spitter Ordeal) | Verified | decide | Yes |
| BK-23 | Low | 57 of 123 manipulations have no `hemomancy.manipulation.<id>` name key and show an id-derived fallback; the `skill_efficiency` description says "8% per level" but code compounds `0.92^level` | Verified | fix lang; decide the wording | Efficiency only |

## Coverage gaps

- **Station upgrade with no page:** the Hematic Armature has no book page at all; its two upgrade rites
  (`armature_consecration` D5, `monolithic_armature` D7) have no page and no `registerUpgradeLesson`. The other six
  stations' twelve upgrade rites all have pages.
- **Working crafts with no book page** (the map covers them): mnemonic_reliquary, sporitic_thurible, mycelial_lantern,
  runic_chisel_station, visceral_mirror, dendritic_distributor, sanguine_monolith, covenant_throne,
  semi_sentient_construct, mind_spike, vacant_effigy, vascular_effigy, warp_chair; morphling_incubator and mason_effigy
  are only mentioned in passing.
- **Floorless crafts never named in the book:** dried_gourd, hematic_stake, mycelial_synapse.
- **Manipulations never named in the book** although every tendency has a page: Animus (blood_binding and its chain
  forms, blood_lattice, guided_blood_shot, hematic_mortar, sanguine_halo, hematic_riposte, the summon_avatar stages,
  the blood_cloud forms, sanguine_tempest, vital_effusion, crimson_coronation, conjure_blade); Ductilis (venous_travel,
  hematic_rebuke, hematic_impressment, synaptic_jolt, conductive_mark, hemolymphal_pulse, crimson_harvest,
  sanguine_marionette, sovereign_instinct, synaptic_storm, living_circuit, thread_ripper, conjure_crossbow); Ferric
  (blood_needle_fan/lance, vascular_dowsing, iron_retort, ironhearted, sanguine_magnetism, ferric_rampart,
  ferric_spikes, hematic_ballast, iron_choir, lodestone_collapse, conjure_axe); Lux (hemosynthesis, blood_lamp,
  white_verdict, vigil_of_glass, radiant_wake, conjure_spear); Congeatio (glacial_circulation, osseous_bloom,
  cruor_javelin, absolute_stillness, rimebound_sentence, conjure_flail); Flammeus (the three updrafts, searing_lash,
  furnace_veins, phoenix_debt, conjure_torch); Tenebris (gloam_laceration, penumbral_drift, eclipse_well, umbral_rend,
  conjure_claws); Mortem (lignum_mortis, canopy_mortis, worked_lignum, insatiable_hunger, grave_debt, blackhearted,
  funeral_bell, carrion_communion, fruiting_contagion, conjure_sickle).
- **Bosses:** the four Saints and Annetta have pages. Vesper, the Mycophant, Naeglerophaeon, Phlegethosoma, Bloom
  Warden and Kindled Champion have none. The missing Apotheos section is C-8.
- **Rites:** no Harbinger rite is missing from the map, and no map id is stale.

## Not a problem (checked and matching)

- Station upgrades: all 12 paged upgrade rites match `StationUpgradeCatalog.java` and the recipe JSON on degree, floor,
  brazier offerings, anchor count and circuit count/mL. Alembic capacity 5,000 to 7,500 mL, Athanor twice as fast.
  Bound potion 1,000 mL / 100 ticks, refill 750 mL / 80 ticks, one in five drinks pays 1,000 mL. Compound 500 mL / 160
  ticks (80 at the Athanor); Ferric Poise 90 s; Nerve Surge 25 s then Slowness 8 s. Centrifuge 7.5 s and 5 s spins,
  +25% / +50% yield, 75% / 100% iron powder, 50% second fraction, 300 mL for Steady Separation. Resonant Forge hammer
  service every 64, wheel every 32, 500 mL repair, 8 scalpel durability, 5,000 mL basin. Sanguine Condenser tiers
  150 mL / 10 s; 750 mL / 4 / 6 s / 125 mL; 1,500 mL / 8 / 3 s / 100 mL with a one-in-four bonus formation.
- Scars: light 50 mL, learn 100 mL, 500 mL per scar at the Effigy, commit 50 mL; slots 1/2/4 plus 3 resonance levels
  for 7; Runic Motif Paper recipe.
- Circus: Cinder Bellows frame recipe, 900 mL ordeal, 1 s / 2 s / 2 s breath cycle, 5-block 60° cone, all seven lesson
  degrees.
- Morphlings: strain tendencies and Cradle flags, Last Rite costs (500/650) and the shared 10-minute cooldown, Whale
  Fall at 20 max health, Primal powers 250 to 450 mL, Apotheos gate on Morphic Nectar, enzyme weights 1 / 0.75 / 0.5.
- Scriptorium: enchantment affinities, 64 per reservoir, 3 selected doses, power capped at 15.
- Manipulations and memories: Blank Hematic Memory recipe and the 50 mL Blood Shot weave; Axonal Transduction PASSIVE
  150 mL woven with 600 mL and 4 Neurotic; tincture tank draw 2,500 / 5,000 mL; the Frigid, Incandescent and Umbral page
  costs and types; Canon Memory catalysts and tendencies; Crimson Tithe 500 mL / 30 s / double repayment; Endless Hour
  10 s; three fragments make one Residuum.
- Bosses and clinical blood: Putriciel's 5 absolutions; Clairaudiograph 4 s carve and 32-block play range; wax cylinder
  recipe makes 2; injection 0.8 s and 20 s saturation; Cabinet 64 per cell, Field Case 16.
- Liber Immaculatus: purity thresholds 25/50/75/100; Pale Intercession at Resolute (Clarity 75); default keys Z and R;
  abstinence reward every 5 minutes; White Humor pool 32 uses; Ghost Pipe within 4 blocks; Lattice halves the cycle.
- Rite descriptions: Lethe Covenant, Lethean Font, Pale Vigil, Pale Consecration, Still Waters (30%), Silthmere's
  Remembrance, Lethean Tide (purity 50), Sanguine Fervor (3 chunks), Visitation (2 spore sacs).
- Tooltips (31 checked): Mnemonic Folio 30 slots, every armour set bonus, Marrow Crown, Cabinet capacity, advanced-brew
  substitution, poise, surge and reprisal, the four Qliphoth Pome effects, Living Blade 20/5, Draught of Still Waters,
  Pale Humor Flask 24 hits, Pallid Infusion, Absolution Dagger, Pale Silver Pickaxe 4 blocks, the three Silthmere Glaive
  values, Living Crossbow 50% / 50 mL, Living Flail 5 s, Suture Needle D5, vial inject and saturation, the 8 tincture
  keys.
- Skill and manipulation descriptions: 12 skill descriptions (all but Efficiency, BK-23) and 4 manipulation
  descriptions match code.
- Advancements: vessel_filled, first_separation_started, crimson_lodge, sanguine_domain, degree_8_silentium and the
  inventory_changed item ids match their grant code or criteria; every advancement lang key resolves except the
  first_bloodcraft one in BK-14.
