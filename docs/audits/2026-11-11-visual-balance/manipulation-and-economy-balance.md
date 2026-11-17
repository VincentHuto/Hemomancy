# Manipulation and blood-economy balance (2026-11-11)

Read-only audit of every manipulation and every blood price after the core loop overhaul. Nothing was built or run. The round two job was editing source while this was read, so line numbers will drift; no value looked half-changed, but the second-wave Shapings (Flame Kindle, Furnace Veins, Eclipse Snuff, Carrion Draw, the rampart and mending Shapings) are fresh from round two P4 and may still move. Code is authoritative throughout.

Companion files in this folder:

- `manipulations.csv`: all 115 registry entries, one row each, with registry metadata, class magnitudes, Shaping and strand prices, and computed yardsticks.
- `tuning-sheet.csv`: about 230 tunable numbers grouped by system, each with value, location, kind (config, data or code) and a suggested direction.

Paths below are under `src/main/java/com/vincenthuto/hemomancy/common/` unless they start with `data/` (meaning `src/main/resources/data/hemomancy/`) or `docs/`.

## 1. Yardsticks

Every number in this report is read against these.

| Yardstick | Value | Where |
| --- | --- | --- |
| Player pool | 5000 mL (+500 per Capacity level, +500 Covenant) | `MaxBloodLedger.java:21-22` |
| Base regeneration | 1 mL/s (config), so a full pool refills in 83 minutes | `bloodRegenRate`, `bloodRegenInterval` |
| Best body regeneration | 2.5 mL/s (well fed 1.5 plus Well Rested +1.0) | `BloodRegenRules` |
| Skill and item regeneration | Sanguine Surge 5/10/15 mL/s; Last Wind 10/20/30; Candle 30; Morphic Mending 50 | `SkillPointHelper`, `MnemonicCandleRules:9`, `MorphicTonicRules:13` |
| Kill blood | 25 x max(1, maxHP/20) mL; bosses x5; none for a draw kill | config `bloodGainPerKill` |
| Absorption income | 1 mL and 1 Temper per HP drawn; bare 2 mL/s, base Staff 8 mL/s per target | `BloodAbsorptionItem:316-344`, `LivingStaffFocusRules` |
| Temper pool | 20 + 0.6 x alignment (20 to 80) per school | `TemperRules:20-22` |
| Median R cast cost by rank | Humilis 150, Mediocritas 275, Summa 400, Magister 600, Perfectus 900 mL | computed from `ManipulationInit` |
| Median R cast damage per mL by rank | Humilis 0.020, Mediocritas 0.018, Summa 0.020, Magister 0.06 (two casts), Perfectus 0.015 | computed, single target |

The last two rows are the headline. Cost rises six-fold from Humilis to Perfectus. Single-target damage per mL stays flat at about 0.02 HP/mL. Perfectus is the worst rank per mL.

## 2. Manipulations

### 2.1 Coverage

- 115 registry entries (the docs say 108; see section 7).
  - By primary Temper: Animus 28, Ferric 19, Ductilis 15, Flammeus 13, Mortem 13, Tenebris 10, Congeatio 9, Lux 8.
  - By type: 75 QUICK, 17 CHARGED, 14 CONTINUOUS, 9 PASSIVE.
  - By rank: Humilis 33, Mediocritas 40, Summa 20, Magister 12, Perfectus 10.
- 39 entries carry at least one Shaping. 17 of those put out a strand: 3 lash, 3 jet, 4 spray, 3 plant, 4 brand.
- 6 entries refuse the R key and work only as Shapings: tendon_line, blood_lash, living_land, venous_cling, lode_draw, hematic_hewing.
  - The "15", "10" and "20" in their registry lines are alignment gates, not costs. `VerbsOnlyManip` and `StaffMiningManip` pass cost 0.
- 9 Staff weapon forms (`StaffWeaponFormManip`) cost 0 to cast.
  - Swapping between forms costs 250 mL, less 50 per Weapons Master level, floor 50 (25 at ring 5).
  - They burn no Temper.
- The Staff and the two cell hands (`ConjurationManip`) cost 1000 mL with a 40-tick cooldown, and burn no Temper.

### 2.2 Shared rules every row obeys

- **Charged casts** pay cost x charge fraction and are refused below 25% charge (`ManipulationCastingRules:6-27`). Most charge in 20 ticks; the class overrides vary from 30 to 80 ticks.
- **Continuous casts and the five avatar stages** pay their full registered cost every 20 ticks, so their cost is mL per second (`ManipulationChannelManager.PULSE_TICKS`).
- **Damage** is base x Crimson Mastery (+15% per level, max 3) x Temper affinity (1.25 to 2.0, only against an opposing school) x Temper strength (`SchoolCombatEvents:134-140`).
- **Strands:**
  - They pulse every 10 ticks, so mL/s is twice the per-pulse price.
  - They never start the cast's cooldown.
  - Lockout is one ordinary hit per target per 10 ticks; accents ignore it.
  - Snaps cost lash 25, jet 30, spray 30, plant 20 and brand 40 mL, and burn no Temper.
- **Temper burn:**
  - R casts burn 1 to 5 by rank.
  - Channels burn 1 per second.
  - Every Shaping and strand burns 0.5 per 10 ticks, so 1 per second whatever its blood price.
- **Running dry:**
  - An R cast costs 1.25x blood and lands at 0.9.
  - A Shaping costs 1.5x blood at 0.75 strength (`TemperRules:40-41`).
- **Cost discounts stack with no floor:** efficiency 0.659, mastery 0.80, dynamic use 0.769, Blood Moon 0.75, Dominion 0.75 and Bloom 0.75 multiply to about 0.17x (`ManipulationCostRules`).

### 2.3 Strand and Shaping forms against their R casts

| Manipulation | R cast | R HP/mL | Form | Form mL/s | Form HP/mL | Form over cast |
| --- | --- | --- | --- | --- | --- | --- |
| blood_lash | refused | - | lash, scale 0.6 | 15 | 0.24 to 0.84 | only form |
| searing_lash | 225 mL, 4 HP | 0.018 | lash | 60 | about 0.35 | about 20x |
| gloam_laceration | 175 mL, 3.5 HP | 0.020 | lash, scale 0.85 | 40 | about 0.45 | about 22x |
| hematic_flare | 125 mL, 3 HP | 0.024 | jet | 40 | 0.075 to 0.25 | 3 to 10x |
| cruor_javelin | 200 mL, 4 HP | 0.020 | jet | 50 | about 0.23 | about 11x |
| blood_needle_lance | 150 mL, 18 HP | 0.12 | jet | 35 | about 0.26 | about 2x |
| synaptic_jolt | 150 mL, 3 HP | 0.020 | spray | 40 | 0.064 per target | about 3x |
| prismatic_reproof | 325 mL, 4-8 per target in a 14-block cone | 0.012 | spray (reach 3.3) | 60 | 0.055 per target | R keeps range |
| blood_eclipse | 300 mL, 3 HP cone | 0.010 | spray | 60 | 0.05 per target | 5x |
| blood_needle_fan | 140 mL, 24 needles | - | spray | 40 | 0.064 per target | - |
| glacial_grasp | 125 mL, 2 HP | 0.016 | plant | 40 | 0.075 full, 0.25 one-pulse tap | 5 to 15x |
| ferric_spikes | 450 mL | - | plant | 50 | 0.072 full | plant cheaper |
| crimson_flame_conjuration | 150 mL, no damage | - | plant | 35 | 3.6 to 10.8 area | - |
| conductive_mark | 150 mL, 240 ticks | - | brand | 40 | about 40 mL for 600 ticks | about 9x per tick of mark |
| hemorrhage, insatiable_hunger, grave_debt | 75 / 225 / 325 mL | - | brand | 40 / 30 / 60 | state only | brand drops Grave Debt's refund |

Every damage strand outclasses its R cast per mL by a factor of 3 to 22. The R cast keeps value only where it reaches further (Prismatic Reproof's 14-block cone, Hematic Flare's 16-block ray) or carries an effect the strand does not.

Non-strand Shapings undercut their channels in the same way:

| Manipulation | Channel | Shaping | Ratio |
| --- | --- | --- | --- |
| lignum_mortis | 100 mL/s | 16 mL/s | about 6x |
| canopy_mortis | 125 mL/s | 16 mL/s | about 8x |
| worked_lignum | 175 mL/s | 16 mL/s | about 11x |
| furnace_veins | 150 mL/s | 12 mL/s | 12.5x |
| sanguine_mending | 3 mL per durability point | 2 mL per point | 1.5x |
| pyretic_forge | 18.75 mL per item (cast) | 10 mL per item | about 1.9x |
| vital_effusion | 350 mL cast (bone meal on every plant in a 5x3x5 box) | 15 mL per bone meal | cast wins on a field, Shaping on one plant |

### 2.4 Per-school notes

Full rows are in `manipulations.csv`. These are the notable points.

- **Animus**
  - Sanguine Halo (220 mL for 25 HP, 0.11 HP/mL) and the blood clouds (0.064 to 0.075 HP/mL, estimates) are the only R casts well above the curve.
  - Blood Aneurysm (Summa, 400 mL, 8 + 3 splash) sits below the Humilis Blood Shot per mL.
  - Avatar upkeep runs from 75 to 400 mL/s plus 1 Temper/s; the complete Avatar empties a full pool in 12.5 seconds.
  - Crimson Tithe lends 500 mL. The debt is repaid as min(debt, current volume), so a player near empty repays almost nothing and takes only the 6 HP hit.
- **Ductilis**
  - Thread Ripper is a Summa cast for 35 mL. It deletes any foreign summon you may harm, a PvP pet included.
  - Synaptic Storm costs 900 mL for 8 HP on one target; it pays only against 6 to 8 grouped mobs.
  - Deadly Gaze at 25% charge costs 25 mL and still lifts a mob and stops it, on a 20-tick cooldown.
  - Sovereign Instinct charges 450 mL every time it fires on its own.
  - Paralysis and command refuse bosses and any mob over 80 max HP.
- **Lux**
  - Hematic Beacon heals 3.2 HP for 350 mL (0.009 HP/mL). Lumen Suture is 2.7x better per mL.
  - White Verdict costs 800 mL for 12 HP per target on a line (18 on Illuminated targets).
  - Radiant Wake costs 120 mL/s for 1 heal per pulse.
- **Ferric**
  - Lodestone Collapse (Perfectus, 900 mL) tops out at 10 HP.
  - Iron Choir costs 250 mL/s for about 3 DPS plus Resistance I.
  - Ironhearted gives +4 absorb HP per cast (cap 10) for 10 minutes at 75 mL per HP. That is cheap next to Iron Retort's 250 mL for 3 seconds.
  - Hematic Hewing's face cut costs 4 mL per block at any hardness, against the pickaxe's 2 mL per hardness point (up to 40). Hard blocks are about ten times cheaper to hew.
  - The Staff and both cell hands refund 90% on dispel regardless of wear (`ConjurationManip.dispelHeld`, pending D-25).
- **Congeatio**
  - Osseous Bloom takes 25% of current HP with no cap except 12 on bosses, so untagged elites take huge hits.
  - Rimebound Sentence (Magister, 750 mL) does 12 HP.
  - Absolute Stillness costs 300 mL/s.
  - Endless Hour's cooldown equals its duration, so it may chain.
- **Flammeus**
  - The four updrafts deal 1.5 to 3 HP; their price is for movement.
  - Phoenix Debt is a cheat-death for 1200 mL; it arms only while the pool holds that much.
  - With Pyretic Forge selected, the brazier smelts anything for 15 mL and no fuel.
- **Tenebris**
  - Void Shroud keeps Veiled and Speed II up nearly permanently for about 20 mL/s (100 mL every 5 s).
  - Penumbral Drift charges 75 mL/s for nearly the same effect.
  - Eclipse Well (Perfectus, 900 mL) does at most 15 HP per target.
- **Mortem**
  - Exsanguinate costs 300 and returns 250 on a kill. Grave Debt costs 325 and returns 250 on the target's death from any cause. Section 6 covers both loops.
  - Hemorrhage's blood_loss rolls a 0.1% Sanguine Formation drop per tick on mobs, about 21% per 75 mL cast. Gloam Laceration and Umbral Rend roll it too.
  - Carrion Communion's channel costs 220 mL/s; its Absorption Shaping is a Mortem Temper loop (section 6).

## 3. The blood economy

### 3.1 Body

| Item | Value | Where |
| --- | --- | --- |
| Regeneration multipliers | starving 0, hungry (food under 6) 0.5, plain 1.0, well fed 1.5; Well Rested +1.0 for 6000 ticks after a full night; Mind Broken 0.85 | `BloodRegenRules`, `MindBrokenRules:10` |
| Wound drain | 5 mL per HP (x0.65 Automatic Coagulation) | config `bloodDrainPerDamage` |
| Circulation cap | 6 + 1.5 per degree + 0.002 per capacity point, per 20 ticks; base regen, meals and gourds skip it | config `circulation*` |
| Borrowed reserve | cap 500; 25 mL per overkill HP | `BorrowedBloodRules:14-16` |
| Bloodline trickle | 0.5 per 100 ticks; keeps 25% of max | config `bloodlinePool*` |

Meals are blood paid at mL per nutrition, from `data/blood_food/`:

| Food | mL per nutrition | Example | Temper |
| --- | --- | --- | --- |
| cooked_meat | 6 | steak 48 mL | - |
| raw_meat | 4 | raw beef 12 mL | - |
| cooked_fish | 4 | cooked cod 20 mL | - |
| raw_fish | 3 | raw cod 6 mL | - |
| venison | 8 | 64 mL | Animus +3 |
| raw_venison | 6 | 18 mL | Animus +1 |
| pelagic_herring | 6 | 30 mL | Ductilis +2 |
| raw_pelagic_herring | 4 | 8 mL | Ductilis +1 |
| gourd_stew | 4 | 28 mL | Animus +2 |
| tainted_harvest | 12 | 36 mL | - |
| tainted_meat | 20 | 60 mL | - |

A full food bar of steak is 120 mL, about one cast. Once Sanguine Surge is taken, the hunger and sleep multipliers stop mattering: Surge alone is 5 to 15 times base regeneration.

### 3.2 BloodAccounting sources and typical prices

Every source constant, grouped by category. "T/F" pairs are countsAsSpend / reserveProtected.

- **CAST** (T/F)
  - manipulation_cast: ledger cost, 35 to 1200 mL.
  - shaping: per pulse.
  - strand_snap: 20 to 40.
  - venous_cling: upkeep.
  - animal_dose: 20.
  - menagerie_leash: upkeep.
  - synaptic_loadout: 100.
  - crimson_tithe_debt (F/F): what is owed.
- **WORLD** (T/F)
  - ground_stain: 4; ground_pulse: 15; vein_growth: 20.
  - vein_upkeep (F/T): 1 mL/s.
  - ash_trail_ignition: 25.
  - chum, earthen_vein_travel (125 to 4000), bloodwood_growth, heartwood_core_feeding: amount fed.
  - monolith_gate: 2.5 per tick.
  - ring_door: per tick.
- **STATION**
  - blood_projection (F/F): 100 mL/s bare, 150+ with the Staff.
  - blood_routing: up to 160 mL/s from a sutured owner.
  - sanguine_condenser: amount offered.
  - covenant_throne_trance: 2500.
  - drudge_birth: 3000 (config).
  - resonant_forge_hammer: up to 500.
  - somatic_loom, loom_burnt_spans (5), sanguine_vigil, morphling_cradle_draw: varies.
  - blood_formation (SKILL): 300.
- **RITE**
  - brazier_lighting, brazier_transmutation (10 to 80), scar_brazier_rite, cardinal_rite, countdown_rite_completion, puppeteer_trial (500 to 1400), blood_structure_craft (0 to 150): recipe cost.
  - rite_boundary: 25 per tick, which is 500 mL/s.
  - blood_infusion: 25 to 75.
  - blood_structure_feed: amount.
  - saint_sarcophagus_offering: 100.
- **BODY and BLOODLINE**
  - wound (F/T): 5 per HP.
  - bloodline_trickle, bloodline_donation, consecrated_bloodwell: amount.
  - blood_loss: 0.5 x (amp+1) per tick.
  - hemolysis: per tick.
- **TOOL**
  - living_dig: 2 per hardness.
  - living_staff_hot_swap: 250.
  - living_torch_breath: 60 mL/s.
  - living_blade, living_axe, living_crossbow and the other weapon forms: per feral hit, up to 225.
  - blood_thrall: 500.
  - bound_brew_dose: 1000.
  - succession_self_sample: 100.
  - crude_memory_shard: 500.
  - vein_spider: 2 per transfer.
  - recall_barb: upkeep.
- **ARMOR, SCAR and MORPHLING**
  - silent_slipping: 250.
  - silent_archon_refusal: 3000.
  - covenant_mantle: 10 per 40 ticks.
  - ink_mantle_reprieve: 500.
  - morphling_upkeep: 0.5 per 60 ticks.
  - morphling_starvation: 2.
  - breakwater: 2 per HP moved.
  - scar_upkeep, scar_weapon_bleed, masons_effigy_motif: varies.
- **HAZARDS**
  - blood_moon: 50 per 40 ticks (75 mL/s).
  - fungal_gardens_travel: 500.
  - desiccant_sting: 250.
  - lantern_tick_bite: 8.
  - stained_priestess_drain: 300.
  - mycophant_siphon: 1.5% or 2.5% of max.
  - mycophant_claim: 15% of max.
  - intruding_memory: 10 x power.
  - Saint and Hemorath sources: specific to each.

Not every call site was traced to a fixed number. "Varies" means the price is passed in by the caller.

### 3.3 Income

| Source | Rate |
| --- | --- |
| Kill | 25 mL x max(1, maxHP/20), bosses x5; a draw kill pays nothing; overflow goes to the first worn gourd |
| Bare Absorption | 1 HP per 10 ticks, 1 mL per HP, so 2 mL/s plus 2 Temper/s |
| Staff Absorption | 4 HP per target per pulse (+0.75 per draw level, +0.25 per focus level), up to 8 targets; the cadence skill shortens the pulse from 10 ticks to 4 |
| Riders | Pressure 1.25x blood; Searing 0.6x blood and 1.5x damage |
| Acclimation per creature type | x1 under 750 mL drawn, x0.5 under 1250, x0.25 under 1500, then nothing; decays 0.25 per tick |
| Absorbing from a tank | bare 100 per use tick; Staff 150 +75 per level +35 per focus |
| Specimen Vat | every 10 ticks: 2 mL x share x acclimation and 6 Temper, for -2 Vigor |
| Rib gourd | generates 0.25 mL per tick (5 mL/s), skipping the circulation cap |
| Rites | none pay blood to the player; Eternal Covenant gives +500 max |
| Exsanguinate and Grave Debt | 250 mL back per kill or death |

Combat Absorption cannot pay for combat. A base Staff on one target draws 8 mL/s, while strands cost 15 to 60 mL/s. It is a Temper tool, which matches CL-24. The full-body stop (`BloodAbsorptionChannelRules:37-39`) also blocks Temper while blood is full, unless the player has Sated Siphon.

### 3.4 Stations and machines

| Station | Numbers |
| --- | --- |
| Ghastly Alembic | tank 5000, 7500 upgraded; leaks 50 per 200 ticks |
| Enzymatic Scriptorium | tank 600; inscribe 100/200/300 by row; denature 300 |
| Resonant Forge | tank 5000; hammer 200 + 50 per load; grind 100 + 25; master surcharge 1000; hammer repair 500 |
| Hematic Armature | tank 8000 |
| Mycelial Crucible | 30 mL/s |
| Mycelial Lantern | 600 mL per enzyme (0.25 per tick over 2400 ticks); drains directly, not through BloodAccounting (`MycelialLanternBlockEntity:127`) |
| Morphling Incubator | 10 mL/s |
| Sanguine Vigil | 25 per 20 ticks, radius 20 |
| Planting Basin | tank 500; 5 mL per growth |
| Venous Basin | 2000 |
| Sanguine Condenser | pool 300 / 750 / 1500; one Formation per 200 / 120 / 60 ticks; holds 1 / 4 / 8 |
| Station upgrade rites | 50 per circuit (Forge 250) |
| Vial Centrifuge | no blood per spin |

**Sutured machines** (`BloodRoutingHelper:31-37`):

- **Reach:** the owner must be within 16 blocks at D3+, or anywhere in their Fane at D5+.
- **Draw:** up to 8 mL per tick (160 mL/s) from the owner, plus 12% per Fane Suture level. The machine budget is 10 per tick, and a worn gourd is drawn first.
- **Owner floor:** the owner is never drawn below 50% of max.

### 3.5 Living blood (coagulation)

All in `tile/CoagulationRules.java` unless noted.

- **Fresh window:** 6000 ticks (5 minutes) after the last feed.
- **Clot rate:** 10% of the remaining fresh blood per minute, so about a 6.6-minute half-life. Everything counts as clot at 72000 ticks.
- **Formation yields:**
  - neglected tank: 1 per 400 mL of clot;
  - Condenser stage 0: 150 mL, with a 25% loss chance (less 5% per Crystallization level), about 200 effective;
  - stage 1: 125 mL;
  - stage 2: 100 mL with a 25% bonus, about 80 effective;
  - Formation key: 300 mL from the body;
  - wound drop: 8% on hits of 6+ HP at D1 to D3.
- **Never clots:** the Gourdvine Tap, the Specimen Vat, tanks held by a running upgrade rite, blood_rock, gourds, and the player's own blood.
- **Freshening:** any fill above 0 restamps the whole tank (`BlockBloodVolume.java:113-154`). A full sutured buffer with its owner in range is restamped free every 200 ticks (`keepBufferFresh`).
- **Flasks:** `bottled_at` is stamped on Alembic bottling or on the first tick in a player's hands. An unstamped flask reads fully fresh (`BloodyFlaskItem:57-78`).

### 3.6 Temper

| Item | Value | Where |
| --- | --- | --- |
| Cap | 20 + 0.6 x alignment, 20 to 80 | `TemperRules:20-22` |
| Burn | cast 1-5 by rank (x charge); channel 1 per s; every Shaping and strand 1 per s | `TemperRules:24-25,91-102` |
| Dry | cast 1.25x blood, 0.9 strength; Shaping 1.5x blood, 0.75 strength; raised walls stand at least a quarter of their time | `TemperRules:40-41`, `ShapingRules:566` |
| Scars | burn -25% per tier; refill toward 5 x tier at 0.25 x tier per s | `TemperRules:29,105-124`, `TemperEvents:34-46` |
| Death and decay | death keeps half; no decay | `TemperRules:31` |
| Fills: creature draw | 1 per HP, split over the creature's schools; a tainted offspring adds half a share | `TemperCharge:139-154` |
| Fills: Wills | 10 per tier | `TemperRules:33` |
| Fills: meals | 1 to 3 | `data/blood_food` |
| Fills: Vat | 6 per draw | `VatVigorRules:22-27` |
| Fills: Carrion Draw | flesh 2, bone 3, skull 10, bone block 24 | `CarrionRules:10-49` |
| Catalysts | enzyme 10, spores 5; vanilla reagents 3 (rotten flesh, packed ice, spider eye, hematic iron scrap, blaze powder, coal, charcoal, glowstone dust, bone, ink sac); emberpod and glowroot 2 | `data/temper_catalyst/` |
| Catalyst order | off hand first, then first inventory slot; not the best fit | `TemperCatalysts:97-109` |

**A full body draws no Temper: still true.** Creature Temper comes only through the living draw, and the full-body stop blocks that draw unless the player has Sated Siphon. Catalysts, meals, Wills, the Vat and Carrion Draw still fill a full body.

### 3.7 Ground network

| Item | Value | Where |
| --- | --- | --- |
| Lighting a run | 15 mL per pulse, at most one per second; 600 ticks lit with no catalyst; runs up to 64 cells | `world/ground/GroundRunRules:21,36,40,42` |
| Effects | smelt every 1.5 s; feed crops every 6 s; hot runs add 15 burn ticks per beat to every adjacent furnace, with no cap | `GroundRunRules:25-34`, `GroundNetwork:383` |
| Stains | 4 mL, rewet 2, refund 2 | `GroundRunRules:50-54` |
| Stain decay | age 0.5, fade 0.25 per random tick | `GroundRunRules:58-60` |
| Stain from kills and bleeding | 35% per kill, 25% per bleed check | `GroundRunRules:62-65` |
| Ash ignition | 25 mL | `GroundNetwork:97` |
| Gouge cutting | 15 mL/s | `GougeCuttingShaping:44` |
| Living veins | 20 mL per cell, 4 cells per pulse; allowance 16/24/32/48/64 cells for D3 to D7; flow 20 mL/s; upkeep 1 mL/s; owner range 32; wither after 24000 ticks | `GroundRunRules:70-80,169` |
| Basin and gutters | basin 2000; gutter 10 mL/s; falloff 8 | `GroundRunRules:85-91` |
| Gouge catalysts | catalyst worth x 1200 ticks (Lux x 2400); blue ice 24000; organic prism 48000; iron ingot 6000; compass 12000; no catalyst asks for blood | `data/gouge_catalyst/*.json` |
| Catalyst Feeder | one stack; uses no blood | `CatalystFeederBlock:30-33` |
| Earthen Vein travel | 500 / 1000 / 2000 / 3000 mL by distance, 4000 across dimensions; 125 within a dimension when a living vein joins the openings | `world/vein/EarthenVeinTravelRules:17,31-42` |

### 3.8 Farming and water

| Item | Value | Where |
| --- | --- | --- |
| Extra growth chance by fed level | unfed 0, drip 0.25, basin or feed run 0.5, cloche 1.0 | `world/farm/BloodFedGrowthRules:11-17` |
| Drip | 4 mL; reach 10; feeds the ground for 6000 ticks | `BloodFedGrowthRules:23-29` |
| Planting basin | tank 500; 5 mL per extra growth | `BloodFedGrowthRules:47-49` |
| Cloche | 25 mL per stage, 600 ticks per stage, one plant | `BloodFedGrowthRules:55-57` |
| Taint | 8% on ripe level-2 crops | `BloodFedGrowthRules:43` |
| Feed run | +1 stage every 6 s to every crop beside a lit cell | `GroundNetwork:418-429` |
| Vital Effusion Shaping | 15 mL/s | `VitalEffusionShaping:35-40` |
| Chum pouring | 2 mL per tick; pool full at 400, max 800; half-life 900 ticks; radius 3 to 12 | `world/chum/ChumRules:11-22` |
| Chum catch | 0.04 base, 0.21 at full; x3 in home waters; cap 0.75 | `ChumRules:33-37` |

### 3.9 Menagerie

| Item | Value | Where |
| --- | --- | --- |
| Leash upkeep | 0.25 x max HP per second, minimum 1 (cow 2.5, a 100 HP creature 25) | `entity/menagerie/MenagerieRules:16-17` |
| Leash hold and ranges | hold 60 ticks; hostiles must be at 1/3 health; follow 48, abandon 600 ticks, carry 16 | `MenagerieRules:12-22` |
| Vat size limit | height 2, width 1.5, max HP 40 | `MenagerieRules:24-26` |
| Vigor | max 100, captured at 60, -1 per 1200 ticks untended; feeding max(2, 0.5 x max HP) mL per point at 1 point per second | `VatVigorRules:14-18`, `KeptSpecimen:312` |
| Vat draw | every 10 ticks: -2 Vigor for 6 Temper and up to 2 mL; refused at Vigor 10 or below | `VatVigorRules:22-27` |
| Sampling | vial 5 Vigor, syringe 3 | `VatVigorRules:29-31` |
| Rattle and breakout | rattle below 25 Vigor; breakout below 10 for 20+ HP hostiles | `VatVigorRules:33-38` |
| Brood | parents need 75 Vigor; cost max(150, 30 x max HP) over 120 s | `BroodRules:19-37` |
| Animal dose | 20 mL per 40 ticks; feral chance +0.25 per dose (cap 0.9) | `AnimalTaint:54-64` |
| Blood Thrall | costs 500; carries 1000; moves 50 per transfer; returns 250 when absorbed | `BloodThrallEntity:65-67` |

### 3.10 Living Staff, mining, Living Land and Venous Cling

| Item | Value | Where |
| --- | --- | --- |
| Staff rings | 5000 / 25000 / 100000 / 300000 / 750000 mL moved | `item/harbinger/tool/living/LivingStaffGrowthRules:11` |
| What counts as moved | projection into a tank, absorption out of one, Shaping blood, creature draws and digging all count | `LivingStaffItem:177,318,347,362,383` |
| Ring rewards | reach +0.5 / +1 / +1; rate x1.10 from ring 3; iron dig tier at ring 2, diamond at ring 4; hot-swap floor 25 at ring 5 | `LivingStaffGrowthRules:15-110` |
| Dig price | 2 mL per hardness point, max 40 per block | `LivingDigRules:10-12` |
| Lode Draw | 12 mL per ore | `StaffMiningRules:14-15` |
| Hematic Hewing | 36 mL per cut | `StaffMiningRules:17-18` |
| Talaromyces vein | free after the first block, up to 64 blocks | `ScarsEntityEventHandler:148` |
| Living Land | 30 mL/s for a 4 to 10 HP strike about every 1.5 s, about 0.13 HP/mL | `LivingLandTerrainRules:39-95` |
| Venous Cling | root 6 mL; then 4 mL/s on a wall or 6 on a ceiling, a quarter of that with Hematic Cleats | `ClingRules:34-65` |

### 3.11 The world's answer

| Item | Value | Where |
| --- | --- | --- |
| Regions | 64x64; half-life 48000 ticks; cap 12000 mL; up to 3 wards, each halving | `world/answer/WorldAnswerRules:16-24` |
| Bands | 200 / 600 / 1500 / 3000 mL | `WorldAnswerRules:26-33` |
| What counts | stains at 1.0; chum at 0.5; a Harbinger kill 6 x max(1, maxHP/20); absorbing a stain -4 | `WorldAnswerRules:37-41` |
| Blood Moon chance by phase (full first) | 0.25, 0.10, 0.04, 0.01, 0, 0.01, 0.04, 0.10, multiplied by (1 + 3 x pressure), cap 0.9 | `WorldAnswerRules:45-48,148-152` |
| Blood Moon frequency | one night in 14.5 in a quiet world; about one in 4 at seething | computed |
| Omens and Wills | omen 0.10; Will ambush x (1 + 2 x pressure) | `WorldAnswerRules:50-54` |
| Scavengers | 3 wet stains within 12 blocks; checked every 200 ticks; 0.2 + 0.15 per band; cap 3 | `WorldAnswerRules:58-64` |
| Moon effects | drains 75 mL/s from exposed players and discounts every cast by 25% | sources |

## 4. Recipes that turn blood into things

- **Recipe counts by type:**
  - memory_weaving (loom) 108; cardinal_rite_recipe 91 (18 Working crafts); armature_upgrade 48; distillation 43; scar_recipe 25.
  - blood_infusion 18; brazier_transmutation 12; blood_structure 11; rendering 11.
  - enzyme_fruiting 8; fungal_scar 8; incubator 8; blood_gourd_fill 4; advanced_brewing 3.
  - There is no "sanguine formation" recipe type. 71 recipe files name `sanguine_formation`; 2 of them produce Formations.
- **What a Formation costs:**
  - about 80 mL at a finished Condenser; 100 to 200 at earlier stages; 300 by the key; 400 from a neglected tank.
  - free from wound drops (D1 to D3) and from Hemorrhage-family blood_loss drops.
  - free from the crafting recipe (section 6).
- **Formation consumers:** 4 for a gourd fill (+200 mL), 4 for hematic_memory or sanguine_conduit, 1 for each of about 20 crafting items, 1 per tincture, 1 per Working craft (which then feeds 75 to 8000 mL).
- **Working craft feeds:**
  - 75 to 300 mL: 10 crafts.
  - 500 mL: dendritic distributor, scriptorium, forge.
  - higher: sporitic_thurible 1000, fungal_implantation_pylon 1200, mycelial_lantern 2500 (D3), vascular_effigy 6500, covenant_throne 8000.
- **Brazier against vanilla:**

  | Recipe | Brazier | mL | Vanilla |
  | --- | --- | --- | --- |
  | packed ice | 1 ice to 1 | 30 | 9 to 1 |
  | blue ice | 1 packed ice to 1 | 60 | 9 to 1 |
  | flint | 1 gravel to 2 | 10 | 1 at 10% |
  | bone meal | 1 bone to 5 plus spores | 15 | 3 |
  | charcoal | 1 log to 2 | 20 | 1 |
  | glass | 1 sand to 2 | 20 | 1 |
  | hematic iron scrap | powder + coal to 1 | 40 | none |
  | leather | 1 rotten flesh to 1 | 25 | none |
  | moss block | 1 stone to 1 | 30 | spreads free |
  | oak sapling | 1 dead bush to 1 | 40 | none |
  | obsidian | 1 magma to 1 | 80 | none |
  | rooted dirt | 1 dirt to 1 | 20 | none |

  Seven of twelve beat vanilla. Ice to blue ice is 81 to 1 in vanilla and 1 to 1 for 90 mL here.
- **Rendering:** no blood. Vanilla reagents give enzymes at 25% to 50%: blaze powder, hematic iron scrap, packed ice and spider eye at 50%; coal, charcoal, glowstone, ink sac, rotten flesh and bone at 25%.
- **Enzyme fruiting:** 600 mL per enzyme, which the free rendering pool makes pointless.
- **Alembic:** only the 16 tinctures cost blood (2500 + 1 Formation per flask, 3 doses; jug 5000 for 6, no saving). Hematic iron scrap distils to 3 powder.
- **Loom:**
  - 50 to 750 mL and 1 to 12 enzymes per memory.
  - memory_blood_rush (Humilis) costs 350 mL and 7 enzymes; other Humilis memories cost 50 to 100.
  - The four Saint relic memories (Summa) cost 100 mL and 2 enzymes plus a residuum.
- **Blood infusion:** oak log 60, planks 40, stone bricks 75, glass 50, pane 25, stone to venous stone 50.
- **Fungal scars:** phase one costs 2400 mL (1200 for noctifly, saprovitta and talaromyces).
- **Advanced brewing:** 250 to 750 mL.
- **Centrifuge:** spins cost no blood.

## 5. Things the first run's verifiers named

| Item | Status now | Evidence |
| --- | --- | --- |
| Thrall couriers keep blood fresh | still true, and wider: any fill above 0 restamps the whole tank, so 1 mL every five minutes keeps an 8000 mL tank fresh | `BlockBloodVolume:113-154`; CQ-6 |
| Catalyst-fed runs need no blood | still true: only the 15 mL lighting pulse is blood; a feeder keeps the run lit on catalysts alone, and hot runs fire every adjacent furnace with no cap | `CatalystFeederBlock:30-33`, `GroundRunRules:32`; CQ-7, CQ-43 |
| Free Talaromyces vein | still true: up to 64 ores free after the first; Lode Draw (12 mL per ore) is pointless for anyone with the scar | `ScarsEntityEventHandler:148`; CQ-8 |
| Staff rings farmed through a tank | still true: projecting into a tank and absorbing back both count as moved, at 150+ mL per use tick each way, and lose no blood | `LivingStaffItem:177,318`; CQ-8 |
| Blood Binding holds bosses | still true and worse than the first report: the Shaping renews a `forceAddEffect` hold every 10 ticks for 20 mL/s, and the R cast relocks before the boss hold lapses (boss hold 60 ticks on a 60-tick cooldown; Lingering 120 on 80) | `BloodBindingManip:116-119`, `BloodBindingShaping:63-79` |
| Brazier yields above vanilla | still true for 7 of 12 recipes | section 4 |
| A full body draws no Temper | still true for creature draws; catalysts and other sources still work | `BloodAbsorptionChannelRules:37-39` |

## 6. Outliers and broken loops, ranked

1. **The Formation economy has two free sources and a duplicator.**
   - `sanguine_formation.json` crafts 2 Formations from foul paste, a bleeding bulb and redstone, with no blood.
   - blood_stained_stone takes 1 Formation and comes back as 2 at the Alembic.
   - Either undercuts the Condenser, the 300 mL key and the whole clotting design.
   - Through the gourd fill (4 Formations for 200 mL of never-clotting blood) they also mint blood.
2. **Blood Binding locks any boss indefinitely** for 20 mL/s (Shaping) or by R recasts every 3 to 4 seconds (section 5). Blood Lattice pins 8 creatures for 44 mL/s.
3. **Strands make their R casts pointless.**
   - Every damage strand beats its cast by 3 to 22 times per mL (section 2.3).
   - The teaching strand, Blood Lash, is the most efficient damage source in the mod (up to about 0.84 HP/mL at 15 mL/s).
   - The R cast survives only where it reaches further.
4. **Rank does not buy efficiency.**
   - Single-target damage per mL is flat at about 0.02 from Humilis to Summa, and drops to about 0.015 at Perfectus.
   - Synaptic Storm, Lodestone Collapse, Eclipse Well, White Verdict and Funeral Bell sit at 0.009 to 0.017 HP/mL for 800 to 1000 mL.
   - Blood Aneurysm and Rimebound Sentence fall below Humilis Blood Shot.
5. **One drop keeps a tank fresh.**
   - Any fill restamps the whole tank.
   - A full sutured buffer restamps for free every 200 ticks, with no range limit in a Fane.
   - Flasks moved without a player holding them never start their clock.
   - Living blood barely touches an organised base.
6. **Catalyst-fed ground runs and feed runs need no blood after the first 15 mL.**
   - A coal feeder fires every adjacent furnace.
   - A feed run grows every crop beside 64 cells, one stage every 6 s. That makes the cloche (25 mL per stage) and the planting basin pointless.
   - Tainted harvest, glowroot and emberpod are farmable catalysts, so some runs feed themselves.
7. **The hematic iron scrap loop.**
   - Scrap distils to 3 powder, and powder + coal + 40 mL brazes back to 1 scrap, so each cycle nets +2 scrap for 120 mL.
   - Scrap renders to ferric enzymes, which are then effectively unlimited.
8. **Exsanguinate and Grave Debt refund 250 mL** against 300 and 325 mL casts.
   - Grave Debt pays on any death, not only the caster's kill.
   - With no floor on discounts (a Blood Moon alone is 0.75, and the full stack reaches about 0.17x), both turn a blood profit. Exsanguinate becomes profitable above about a 17% discount, Grave Debt above about 23%.
9. **The Temper loops and Temper pricing.**
   - Scar tiers 2 and 3 refill faster than their scarred burn, so that school never runs dry.
   - Carrion Draw gives 24 Temper per bone block (3 bones, worth 9 as catalysts) for 16 mL, multiplied by the Staff's work scale.
   - Coal and charcoal are 3 Flammeus Temper each.
   - Every Shaping and strand burns the same 1 Temper/s from 4 to 60 mL/s, so Temper does not separate dear powers from cheap ones.
   - An R cast burns 1 to 5 Temper while one second of Staff drawing gives 8, so the cast dry penalty (1.25x / 0.9) is rarely felt.
10. **Channel upkeeps are too dear to use.**
    - Avatar stages cost 75 to 400 mL/s.
    - Iron Choir 250, Absolute Stillness 300, Carrion Communion 220, Radiant Wake 120, Living Circuit 100 mL/s, all for 1 to 3 DPS or small buffs.
    - Only Sanguine Ward (40 mL/s) is priced against its effect.
11. **Thread Ripper costs 35 mL at Summa** (median 400) and deletes any foreign summon you may harm, at any HP.
12. **Crimson Tithe's debt can be dodged.** Repayment is min(debt, current volume), so a player near empty when the window closes repays almost nothing for 500 mL borrowed every 100 ticks.
13. **Hemorrhage mints Formations.** blood_loss rolls a 0.1% Formation drop per tick on mobs, about 21% per 75 mL cast. Gloam Laceration and Umbral Rend share the roll.
14. **Free and nearly free mining.**
    - The Talaromyces vein is free after the first block.
    - Hematic Hewing's face cut is 4 mL per block at any hardness, against the pickaxe's up to 40.
    - Staff rings farm through a tank with no blood lost.
15. **Regeneration skills swamp the body rules.**
    - Sanguine Surge (5 to 15 mL/s), the Candle (30) and the Rib gourd (5 mL/s, skipping the circulation cap) dwarf the 1 mL/s base and the 1.5 to 2.5x food and sleep multipliers.
    - Tainted meat (20 mL per nutrition) is 3.3 times steak.
    - Raw fish (3) is too small to notice.

Smaller items:

- Osseous Bloom has no cap on untagged elites (25% of current HP).
- Deadly Gaze lifts for 25 mL at 25% charge.
- Sovereign Instinct charges 450 mL unasked.
- Hematic Beacon heals 3.2 HP for 350 mL.
- The conjuration refund is 90% regardless of wear (D-25).
- Endless Hour's cooldown equals its duration.
- A one-pulse plant tap beats a full plant 3.3 times per mL.
- Spray snaps have no wait.
- The catalyst pick order spends the first catalyst found, not the best fit.
- The Mycelial Lantern charges 600 mL per enzyme that the rendering pool gives free, and it drains outside BloodAccounting.
- Covenant Throne (8000) and Vascular Effigy (6500) sit 13 to 16 times above their tier.
- memory_blood_rush costs 350 mL and 7 enzymes against 50 to 100 for other Humilis memories.
- A Harbinger mob farm holds a region at seething (about 6 minutes of one 20 HP kill per second), raising Blood Moons to one night in 4.
- The rite boundary drains 500 mL/s.

No closed blood loop was found in the menagerie, the stains or the vat. Vat draws return less blood than the Vigor cost to feed.

## 7. Docs against code

Code wins in every row. About 830 stated numbers were checked and about 821 match. The dated core-loop sections at the top of `docs/HEMOMANCY_REFERENCE.md` are accurate; the drift is in summary lines and an older roster.

| Doc | Says | Code |
| --- | --- | --- |
| `docs/HEMOMANCY_REFERENCE.md:334` | tainted meat gives 40 mL | 60 mL (20 per nutrition x 3); line 123 of the same doc already says 60 |
| `docs/MANIPULATIONS_AND_CASTING.md:3` | 33 Shapings | 44 Shaping attachments across 39 memories |
| `docs/MANIPULATIONS_AND_CASTING.md:13-16` | 108 manipulations: Animus 23, Ferric 15, Mortem 15 | 115: Animus 28, Ferric 19, Mortem 13 (other schools match) |
| `docs/CORE_LOOP.md:62` | Shapings 6 to 60 mL/s | non-strand Shapings 4 (Glacial Thaw) to 48 (Glacial Rampart); 60 exists only on strands |
| `docs/HEMOMANCY_REFERENCE.md:1111` | `vascularDebuffsEnabled` default true | false (`config/HemoServerConfig.java:289`) |
| `docs/HEMOMANCY_REFERENCE.md:1165` | config key `claimedWillBonusCapSilentArchon` | no such literal in source; line 1173 `realEffectMinRank` also not found as a literal (may be built dynamically) |
| `docs/HEMOMANCY_REFERENCE.md:2852-2865` (section 8.2) | conjurations and weapon forms list a primary Temper only | each has a secondary (blade, spear, crossbow, flail and sickle add Ferric; axe, staff and both hands add Animus; claws add Mortem; torch adds Lux) |
| `wiki/Blood-Systems.md:242` | Well Rested gives twice the base on top | it adds 1x base on top (2x total plain, 2.5x well fed) |
| `wiki/Blood-Systems.md:95` | chum pool up to 12 blocks across | 12 is the radius, so up to 24 across |

## 8. What reading code could not settle

- Exact single-target totals for the scatter casts: Blood Needle (10 to 20 needles at 2 to 4 each, spread) and the four blood clouds (assumed one target sits in the cloud for its whole life). The needle damage formula was taken from a source comment, not confirmed in `BloodNeedleEntity`.
- Damage of the Ferric Spikes entity and the Living Crossbow bolt.
- Crimson Coronation's per-sword damage (about 11 at full charge is an estimate from the arrow formula).
- Axonal Transduction's session upkeep (no constant found).
- How Blood Rush's +0.2 speed and +0.1 attack speed apply (likely multipliers).
- Whether Staff tank transfer runs every tick or on a pulse. Ring-farming time depends on it; the loop exists either way.
- Whether the thrall's pickup reads clotted-aware blood or the raw tank.
- How deferred damage settles when Endless Hour ends.
- Whether a blood structure consumes its held Formation (assumed it does).
- World rarity of venous stone, bleeding heart and hematic iron scrap, which sets how fast the Formation and scrap loops run.
- Whether a gourd's blood feeds the player's regeneration, and a possible double tick when two Rib gourds are worn.
- Drip blood per hour (drips follow random ticks).
- Everything that needs a client: feel of the dry penalty, lash pace, Blood Moon frequency in play, vat economy over a session. CORE_LOOP.md already lists these as owed.

## 9. Tuning sheet

`tuning-sheet.csv` lists every number above that is worth the owner's attention, grouped as body, income, food, temper, cast, strand, shaping, manipulation, coagulation, station, ground, farm, menagerie, staff, world and recipe. Each row gives the current value, unit, location (file and constant, data path, or config key), kind, and a suggested direction.

The suggestions are first proposals to argue with, not rulings. The ones that close loops are higher priority than the ones that only move a curve:

- Formation recipes;
- tank freshening;
- the Binding boss hold;
- the discount floor;
- the refunds;
- the scrap distillation;
- ring counting;
- the Talaromyces vein;
- catalyst-fed runs.
