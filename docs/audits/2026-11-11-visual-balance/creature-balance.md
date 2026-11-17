# Creature and encounter balance table (2026-11-11)

Read-only audit. Nothing was built, run or edited outside this folder. Code is authoritative; where the docs say something else, section 6 lists it. Another job was editing sources while this was read, so expect small drift.

- Raw table: `docs/audits/2026-11-11-visual-balance/creature-balance.csv`. One row per living entity type, one column per attribute, plus the source file.
- Tempers are the eight kinds in `EnumBloodTemper`; a creature's blood profile is `data/hemomancy/blood_profiles/<id>.json` (`EntityBloodProfile`). A missing file means the empty profile (no tempers, no properties, ordinary vials allowed).

## 1. Scope and method

`EntityInit` registers 178 entity types: 160 declared one by one plus 18 Brined Court roles built by `courtMembers(...)`. 134 of them are living (they get attributes in `EntityInit.onAttributeCreate`) and are in the CSV. The other 44 are projectiles, hazards, seats, set pieces and item entities (blood bolts, Mycophant globs and lances, thread bolts, the Hydra's rising head, the Covenant throne seat, the Impaled Procession carousel, the Possession echo and so on); they carry no health and are left out.

The 134 living types break down as:

| Group | Count | Notes |
|---|---|---|
| Wild mobs and animals (Overworld, Nether, End, sea, Gardens) | 62 | includes 8 Facsimiles, the Will, the Hematic Construct, the Bloodlicker and the Enthralled Doll |
| People (Harbinger, Unstained, Court, Circus) | 38 | 18 Court roles, 8 Circus performers and faculty, 8 Harbinger, 4 Unstained |
| Bosses, Saints and their adds | 17 | 4 Saints, 2 Saint adds, 2 Vespers, Mycophant, Naeglerophaeon, Phlegethosoma, 3 Annetta forms, Bloom Warden, Kindled Champion, Circus Ringmaster |
| Player summons, servitors and constructs | 16 | 8 Puppeteer summons, Drudge, Blood Thrall, Pale Intercession, Phantasmal Echo, 4 Ferric and blood constructs |
| Hazard | 1 | Intruding Memory (Engram weaving thread) |

(The Ringmaster lives in `entity/npc/circus` in code and is counted with the bosses here.)

How each column was filled:

- Attributes: `setAttributes()` / `attributes()` / `createAttributes()` bodies, with constants resolved from the `*Rules` and `*Tuning` classes. Vanilla bases (Spider, Bat, Enderman, Blaze, Iron Golem, Wolf, Horse, Zombie, Skeleton, Vex) use the vanilla 1.21.1 values. Puppeteer summons take health, damage and speed from `PuppeteerSummonDefinitions`. Runtime rewrites are noted (Will, Sanguine Hound, Saint challenger and enrage scaling).
- Time to kill: `hits_to_kill_iron_sword_6` (a D0 player with an iron sword) and `hits_to_kill_hematic_sword_11` (Hematic Iron Sword) after the target's armour, toughness 0. Mob threat: `melee_hits_to_kill_*` against a 20 HP player bare, and in a full Hemomancy set (20 armour, 12 toughness; every set uses the same defence map).
- Spawns: `data/hemomancy/neoforge/biome_modifier/*.json` with their `#hemomancy:*_spawnlist` tags, and the `spawners` blocks of `data/hemomancy/worldgen/biome/*.json`. Rules are the `canSpawnHere` methods registered in `EntityInit.onRegisterSpawnPlacements`.
- Keeping: `MenagerieRules.canKeep` through `MenagerieLeash.candidate` (bosses by package `entity.boss`, Wills, anything in `entity.npc`, Puppeteer summons, owned creatures and the Warden are refused). Leash: 60 ticks of Blood Binding, a hostile at or under a third of its health, one led creature at D3, two at D5, three at D7, upkeep `max(1, 0.25 x max health)` mL/s. Jar: tag `specimen_jar_capturable`, at most 2 x 2 blocks and 60 health (`SpecimenCaptureRules`). Vat: at most 1.5 wide, 2 tall, 40 health (`MenagerieRules.VAT_*`). Brood cost `max(150, 30 x max health)` mL (`BroodRules`).
- Expected Degree: no creature carries a Degree gate in code. The column is inferred from where it lives and what gates that place (dimension access, Alchemist and Mnemonist side inquiries, `WILL_MIN_DEGREE`, Saint wards).

## 2. Player yardsticks

- Health 20. Blood: flat 5000 mL max at every Degree (`MaxBloodLedger.BASE_MAX_BLOOD`), plus Capacity skill up to +2500 and Eternal Covenant +500. Regeneration 1 mL/s (`HemoServerConfig.BLOOD_REGEN_RATE` per `BLOOD_REGEN_INTERVAL` 20), x0 to x1.5 by food, +1 when Well Rested; Sanguine Surge adds 5/10/15 mL/s from D2; Hematic Iron set +2 mL/s. Taking a hit costs 5 mL per damage point (`BLOOD_DRAIN_PER_DAMAGE`). A kill gives 25 mL, x5 for a boss.
- Weapons (shown damage / attacks per second): iron sword 6 / 1.6; Living Blade 9 / 1.7 (+5 tame hit, +20 unleashed hit for 75 mL); Living Axe, Spear, Baghnakh, Torch 7; Living Flail 8 (thrown 6 to 14); Living Sickle 8 (D6); Hematic Iron Sword 11 / 1.6; Unstained Warhammer 17 / 0.6, Silthmere Glaive 13 / 1.2, Absolution Dagger 5 / 2.2 plus up to 5 execute. Living weapons deal x1.25 to x2.0 against an opposing Temper (`TemperWeaponHelper`). There is no per-Degree weapon scaling.
- Armour: every Hemomancy set is 3/6/8/3 = 20 armour, 3 toughness a piece (12 a set), 0.1 knockback resistance a piece (`EnumModArmorTiers.createDefenseValues`). That is netherite parity from the first set.
- Manipulations: damage does not scale by rank; rank only gates the Degree. Blood Shot 5 for 100 mL; Hematic Mortar 6 to 3 for 200 mL; Blood Needle volley 10 to 20 needles of 2 to 4 for 100 mL; Cruor Javelin 4 + Rime for 200 mL; Blood Aneurysm 8 direct + 3 splash for 400 mL; Exsanguinate 1.5 x current health under 30% (boss cap 12). Crimson Mastery adds 15% a level.

Read against those: a D0 player kills a 20 HP mob in 4 iron-sword hits; a D5 player in 2 Hematic hits. Any mob under 10 damage needs 15 or more hits to kill a player in a full Hemomancy set, so armour, not health, is what most overworld threats test.

## 3. The bestiary in brief

Full numbers are in the CSV. Grouped by where a player meets them; HP / armour / damage.

**Overworld, D0 to D2.**
- Hostile: Bog Revenant 22/2/4 (Slowness I 80t); Desiccant 12/4/3 with a sting that takes 250 mL of blood (`DesiccantEntity.STING_BLOOD_DRAIN`) plus Hunger II and Blood Loss II; Venom-Rib Centipede 34/0/6 (Poison 160t + Blood Binding 50t); Blood-Drunk Puppeteer 18/2/2 with four Enthralled Dolls 7/0/1; Lantern Tick 8/0/1 that drains 8 mL every 160 ticks while latched; Morphling Polyp 8/0/2; Chthonian Queen 60/6/7 in savanna termite mounds; Mortarbound 24/0/4 slam in the deep dark; Scarlet Serpent 12/0/3 (Poison + Blood Binding, retaliates only); Barbed Urchin 7/0/1+ (puff poison); Hemojelly 6 (sting 2).
- Passive: Leech 2, Crimson Doe 10, Venous Strider 12, Peacock Spider 16 (vanilla spider stats, never attacks), Hematic Burrower 8, Luminal Cicada 4, Verdigris Moth 4, Chitinite 7/0/1, Hemolymphopoda 8, the pelagic fauna (Chiton 8, Pyrosome 8, Herring 3, Ice Fish 6, Siphonophore 6, Comb Jelly 5, Hagfish 10, Vampire Squid 8, Lantern Jelly 4, Chalybeate Snail 12/8, Prism Cuttle 8 with a blinding flash) and the Mnemonic Whale 38 (bites only squid and cuttles for 12).
- Overworld Fungal Gardens biome (on by default): Abhorrent Thought 40/4/7 and Fargone 16/0/3, both in packs of exactly 8 (see 5.1).

**Nether, D2 to D5.** Vampire Bat 6 (bite 1 or 2), Osteophage 16 (attacks skeletons only, 6), Fervent Chitinite 7/0/1. Phlegethontic Basin (Alchemist side commission at D5): Bombardier 28/8, neutral flame 2 + 4 s fire; Excoriated 52/6/8 with breath, bolts and Recall Barb; Phlegethosoma 120/6/7 miniboss, natural spawn weight 1, one per 64 blocks.

**End, D5 to D6.** Choir Keeper 10 (passive), Myelin Borer 8/0/2 with stacking Neural Overload; Naeglerophaeon 150/4 from a Vagrant Mind (D6 side inquiry).

**Brined Court and sea outposts.** Brined Votary 14/2/3. Court members are protected people (30 HP, 4 damage used only by the two defending roles). Bloom Warden 160 and Kindled Champion 220 are the Court's fights.

**Circus (D4 referral, optional).** Performers 26 to 40 HP with 1 to 4 armour; they fall to 1 HP and kneel (downed) instead of dying, and a hit summons dolls and alerts the troupe. Fire Eater 4 + ignite, Stilt Walker slam 3, Acrobat 4, Knife Thrower knives. The four faculty have 30 HP and an unused attack of 4. Ringmaster 80/12/7.

**Rogue Wills, D4 and up.** Broken tiers 20/30/34/38 HP and 3/5/6/6 damage; a sent Will has `max(28, player max health x 1.2 + 8 per Degree over 6)` HP and `6 + 1.25 per Degree over 6` damage (`WillCombatRules`). Absorbable below 40% health.

**Fungal Gardens dimension, D7 to D8.** Fungling 7/0/1 (herds, shield break), Thirster 20/0/4, Lump of Thought 24/0/5, Erythromycelium Eruptus 30/2/6 (corpse ambush and a 6-damage lunge every 40 ticks), Hunter Fungus 26/2/5 (converges on alarms within 64), Abhorrent Thought 40/4/7, Fargone 16/0/3, Chthonian 14/3/3, the eight Facsimiles at vanilla stats (golem 100/15), and the Antibody 10/0/4 (D8 Apotheos network response, 60 s life).

**Player side.** Puppeteer summons D3 to D5: Veinwing Vulture 14/4, Marrow Spitter 22/5, Gorebound Hulk 55/9, Scarlet Mummer 24/3, Sanguine Hound 30/6, Cinder Bellows 30/4, Ringmaster Pattern 28/1, Mnemonist Puppet 26/3, each x1.15 health and x1.10 damage per Living Sinew level. Drudge 20/4/3. Pale Intercession strikes for 6. Hematic Construct (chapel puzzle guard) 12/2/8.

## 4. Bosses and Saints

Paths below are under `src/main/java/com/vincenthuto/hemomancy/common/`. Timelines are W/A/R = `BossMoveTimeline(windup, active, recovery)` in ticks.

### 4.1 Summary table

| Boss | HP | Armour | Phases (HP per band) | Biggest hits | Defences | Degree | Reward |
|---|---|---|---|---|---|---|---|
| Phlegethosoma | 120 | 6, 3, 0 | 48 / 42 / 30 (60%, 25%) | Breach 8, Latch 7, Lunge 7, Spray 6 | x1.25 while exposed; 6 damage staggers suction | Nether basin, no gate (D5 commission) | 2-4 Sanguine Formation, 1-2 Barbed Splinter, 15 XP |
| Naeglerophaeon | 150 | 4 | 75 / 60 / timed Overload (floors 15 then 1.5) | Discharge 8, Nova 7, Lunge 6 | invulnerable in intro 70, transition 40, blink | D6 (End) | Naeglerophaeon Ganglion |
| Circus Ringmaster | 80 | 12 | not health-based: Rafters 160t, Carousel, Descent | Staff 7, knives 4, horses 4 | immune except to the owner in Descent; nonlethal | none checked | Ringmaster Pattern or thread_ripper, Topper |
| Bloom Warden | 160 | 0 | rooted 160t / uprooted 100t cycle; cannot die | Root Lash 8, Frond Spear 7 | x0.25 rooted with a fed bloom; heals up to 4/s | Court | none |
| Kindled Champion | 220 | 6 | 74.8 / 72.6 / 50.6, yields at 22 | Sweep 9, vents 6 | regrows 2 / 3 / 4.5 per s | Court | title only |
| Annetta Knowles | 350 | 8 | cannot go below 1; breaks after 2400 ticks and 50 damage | Dagger 5/5/7, vial 10 + Hemolysis + Blood Loss | x0.2 frontal shield | no gate found | none (route continues) |
| Stained Priestess | 420 | 10 | one band; javelin fan below 50% | Swoop 14, Plummet 14, Plunge 12, Lance 12 | shield x0.2 after 24 recent damage (cooldown 260) | no gate found | Annetta's Sanguis Lancea + 4 Hematic Iron Scrap |
| Latent Infection | 360 | 8 | one band | Spear 12, Whip 9 + Blood Loss | invulnerable to purge tick 100 | Unstained route | Annetta's Absolution Dagger + 3 Pale Silver |
| Vesper, Crowned Refusal | 520 | 16 | 145.6 / 156 / 156, last 62.4 never removed | Burrow eruption 22, Diving Rend 14, Brood Trample 13 | health floors at 72/42/12%; no body damage while a wound is open or submerged | D7 SILENT_PENDING | none (phase 1) |
| Vesper, Evening Star | 640 | 18 | 213.3 x 3 acts | Grave Splitter 18, Reaper Sweep 17, Ichimonji 16 | same-Temper hits x0.5; never dies, absorbed at 1 HP | D7 to D8 Silentium | Memory of Vesper (code), advancement |
| Mycophant | 720 | 20 | 360 / 180 / 144 at x0.5 (288 raw) / 36 | Rushing Embrace 10, Brood Settle 14, hold 10/14 + blood | Wing Guard x0.2 frontal; untouchable in intro, split, yield | D8 Apotheos | first: Tendril; repeat: Quintessence + Spore Sacs; 180 XP |
| Hemorath | 600 | 4 | 300 / 300 (or forced at the first full meter, tearing 90) | Tithe up to 16, Crown Fall 12, Staff Fall 11 | x0.7 / 1.0 / 1.3 by meter, -4% per iron patch, openings up to x1.4 | D8 by ward and door only | Residuum, 1-2 Quintessence, Hemorath Rib |
| Seraphae | 500 (unused) | 20 | integrity 30 to 40 / 40 to 75 / 75 to 100 | Chain Wrench 9, Convulsion 8, Burst 6/8 | health damage x0; wins by integrity meter | D8 | Residuum, 1-2 Quintessence |
| Putriciel | 400, floor 30% | 12 | 5 absolutions over 3 intervals (2 / 2 / 1) | Pyre Heart 12, Censer Drop and Cast 10 | fire immune (fire stokes the Kindling); health resets each interval | D8 | Residuum, 1-2 Quintessence (drops at body) |
| Velorum | 640 | 14 | 256 / 224 / 160 (floors 60/25/10%) | Release, Set in Place, Veins 10 | Still Guard x0.3 outside openings; last 10% only in openings | D8 | Residuum, 1-2 Quintessence (drops at body) |

### 4.2 Shared Saint kit

- Move damage = base x (current ATTACK_DAMAGE / base ATTACK_DAMAGE) (`entity/boss/saint/SaintMoves.damage`), so enrage's +30% reaches every move, presence tick and hazard. Minimum windup 12 for melee, 20 for area (`MELEE_FLOOR`, `AREA_FLOOR`).
- Picker: at most 3 moves in a string (`SaintPickRules.MAX_CHAIN` 2), repeat damping {0, 0.4, 0.6, 0.8}.
- Saints never die (`SaintTrialEntity.die` holds at 1 HP; only `/kill` bypasses). Each fight ends through its own meter or floor, rolls its loot table once and gives it to the victor or nearest player within 32 (`REWARD_RANGE`). An empty chamber for 600 ticks resets the trial with no loot.
- Challengers: max health x(1 + 0.5 per extra player) (`SaintKit.CHALLENGER_SCALING`).
- Forced door enrage (`SaintEnrageRules`): +30% attack, +15% speed always; per skipped Chapel: +20% speed, +50% health, Resistance I, or Regeneration I. The residuum becomes fragments (3 per residuum).
- Rise 60 ticks invulnerable; phase cards 50 ticks.
- Degree: there is no Degree check on the Saint, the sarcophagus or the relic (`HarbingerSaintEncounterHooks.spawnSaintBoss`). D8 is enforced only by the ward (`MausoleumWardRules.WARD_DEGREE` 8) and the ring door.

### 4.3 Per-Saint notes

- **Hemorath.** Meter 3000 x scale (leak 1.2 to 13.2 per tick). Presence alone feeds 2.5/t, which balances the leak near 33%, so Filling and Brimming need claw drains (80 blood a claw), the Siphon or the wells. Last Thirst at 20% (120 HP): rooted 200 ticks, heals 1 HP per 40 meter, x0.5 damage while dragged. Damage taken ranges x0.448 to x1.82. 18 moves; most deal 5 to 9.
- **Seraphae.** Integrity starts 30, decays 0.2/s solo plus 0.008/t per living fragment; gains only inside openings (hit 2 to 3, cast 3 to 4, Congeatio, Tenebris and Mortem casts x1.5, Lux and Ductilis x0.5). Phase 1 is only 10 integrity points against 35 and 25. Radiance Flood at 0 resets integrity to 10. Growth bursts at 10 for 6 (8 in P3). Fragments 12 HP, anchors invulnerable.
- **Putriciel.** Wins by 5 absolutions: a hit of 4 raw damage or 100 blood of Projection during a 50 to 60 tick Offering. Offerings arm as the Kindling burns past 66% and 33%. Rot II creeps slower (20 ticks a point) than Rot I (12), a non-monotonic step. Colonisation 0 to 20 per player (Overgrown 20: root 30 and 6 damage). Faithful adds are Bog Revenants (2 alive, 8 a fight) under the Downfall deed.
- **Velorum.** Phases 40% / 35% / 25%; with Still Guard x0.3 outside openings, phase 1 is about 853 effective HP. Endless Hour (P3) holds 200 ticks and breaks at 8% max. Stillness 0 to 100 per player (preserved at 100). Devotees 30 HP, blow 4 every 30 ticks, fall still at 0 rather than die.

### 4.4 Per-boss notes

- **Vesper, the Crowned Refusal.** Wounds need 100 drain (held Absorption, owner only); a hit adds 10% of its damage, a killed puppet 25; scabs after 80 idle ticks at 0.4/t down to half the peak. Flight from 50%. Puppet roster of 7 shapes, 3 out at once, at x1.5 health and x1.25 damage (Gorebound Hulk 82.5 HP / 11.25). Carapace Aneurysm exposes him for x1.5 damage on ticks 25 to 104, cooldown 240. Burrow eruption 22 in r2.5 is the largest single hit of either Vesper phase.
- **Vesper, the Evening Star.** Stances tour all eight Tempers, 240 ticks each (180 at 60%, 140 at 25%). Temper defence (`VesperTemperDefenseRules`): a hit of his current stance's Temper does x0.5; a hit of the opposing Temper uses the attacker's affinity multiplier (x1.25 to x2.0); untyped damage x1.0. Acts at 2/3 and 1/3; sickle Harvest below 10%; recovery x0.8 below 25%. Weapon hits range 4 to 18.
- **Mycophant.** Holds fill at 1/100 per tick (1/80 in P2), break in 4 or 5 hits, deal 10 or 14 and drain 10% or 15% of blood. Siphon drains 2 or 3 HP and 1.5% or 2.5% blood a second. Pressure rises 0.25/s (0.5/s in P2), with Slowness I to III and 2 drown damage per second at 85: the only soft timer among the bosses. Brood Call adds 2 or 3 Funglings (7 HP, 1 damage, see 5.2).
- **Naeglerophaeon.** Overload destroys one node every 30 ticks, each releasing 2 to 6 homing sparks of 3; it is killable (1.5 HP) only when the nodes are gone, so the last band is a timer.
- **Phlegethosoma.** Armour drops 6 to 3 to 0 by phase. Suction is interrupted by 6 direct damage (35-tick stagger). Clots 10 HP.
- **Annetta.** The break needs 2400 ticks and 50 damage counted after the shield multiplier; her 350 HP never matters. Cooldowns shrink to 45% as she is hurt.
- **Stained Priestess.** Melee and swoops drain 300 blood from blood-active players and heal her 3.
- **Kindled Champion.** The last bud band is shortest but regrows fastest (4.5/s), so it can stall a low-damage player.
- **Bloom Warden.** Cannot die; re-anchors at 1 HP. With 12 blooms fed it heals 4/s, which out-heals a D0 to D2 player's iron sword at x0.25 while rooted.

## 5. Analysis: outliers

### 5.1 Spawn tables

1. **Overworld Fungal Gardens fills its monster cap with packs of 8 elites.** `worldgen/biome/fungal_gardens.json` has exactly two monster entries, `abhorrent_thought` weight 10 min 8 max 8 and `fargone` weight 10 min 8 max 8. Every hostile spawn in that biome is a pack of eight; eight Abhorrent Thoughts are 320 HP dealing 7 each, with no other monster to dilute them. `FungalGardensOverworldRegion` is on by default (`HemoCommonConfig.DEFAULT_ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION = true`) and the D2 side assignment "The Overworld Gardens" sends players there. Every other biome in the mod uses packs of 1 to 4. The ambient bat entry is also 8-8 and mooshrooms 4-8. `BiomeInit.fungalGardens` in code (Fungling 15 3-6, Chitinite 10, Strider 8, Doe 6, Fargone 4 1-2) describes a calmer biome than the JSON that actually ships; it looks like the datapack JSON was regenerated by `tools/worldgen/fungal_gardens_worldgen.py` with a different intent, or the 8-8 is a typo for 1-8.
2. **Blood-Drunk Puppeteer weight 24 with no light check.** `add_blood_drunk_puppeteer.json` gives weight 24 in dark forest, swamps and old-growth taigas. `BloodDrunkPuppeteerEntity.canSpawnHere` uses `checkMobSpawnRules`, not the monster light rules, so it spawns at noon. Each one brings 4 dolls (`BloodDrunkPuppeteerTuning.DOLL_COUNT`). Against vanilla's roughly 515 monster weight in those biomes it is about 4.5% of hostile spawns, but because it ignores light it also spawns in daytime when nothing else does, so in practice it is most of the daytime hostiles there.
3. **Desiccant ignores light too** (`checkMobSpawnRules` on hot dry ground, weight 6). That may be intended for a desert scorpion; it means a 250 mL sting is the one daytime threat in deserts.
4. **Hemolymphopoda weight 30 (ambient, packs 2 to 5)** on beaches, rivers, swamps, mushroom fields, lush and dripstone caves, and again 20 (3 to 8) in Mycelial Depths and 30 in Rockpool Shore. Vanilla bats are weight 10 and the ambient cap is 15, so isopods take about three quarters of the ambient cap wherever they are listed, and bats become rare in lush and dripstone caves.
5. **Vampire Bat** weight 4, packs 4 to 8, in every Nether biome (`#minecraft:is_nether`). The Nether has no other ambient entry, so they are all of its ambient cap. Fine if intended; worth knowing.
6. **Morphling Polyp is listed in every Overworld biome** (`#minecraft:is_overworld`). Weight 3 is low and water spawns are throttled in code; it is the only hostile that can appear everywhere.
7. **Pelagic Herring weight 45 (6 to 10)** in Pelagic Ocean is the largest single weight in the mod; it is water ambient, so it only competes with other fish.
8. **Natural minibosses.** Phlegethosoma (120 HP, `entity/boss`) spawns naturally (weight 1, one per 64 blocks); the Chthonian Queen (60 HP, 6 armour, 7 damage, "a boss tier above her workers") is placed in termite mounds in savanna, a D0 biome.

### 5.2 Template stats

The block `MAX_HEALTH 7.0, MOVEMENT_SPEED 0.3, ATTACK_DAMAGE 1.0` appears four times, identically: `AbstractChitiniteEntity` (Chitinite and Fervent Chitinite), `FunglingEntity`, `EnthralledDollEntity`, and `BarbedUrchinEntity` uses 7 / 1 at speed 0.3 too. Fervent Chitinite is a Nether armadillo with a two-temper profile and Blood Crystal plus 2 to 4 Fervent Husk drops, yet it shares the overworld Chitinite's 7 HP and 1 damage. The Fungling is a D7 to D8 dimension mob with the stats of a D0 cave armadillo: 2 iron-sword hits, 97 hits to kill an armoured player.

Other values that look unfinished:
- Attack 4 on every Harbinger professional NPC (`HarbingerAlchemistEntity`, Artificer, Cicatrix Anchorite, Mnemonist, Vicar) and the four Circus faculty (`CircusFacultyEntity.setAttributes`): none of them attacks, and the professionals are `ProtectedNpc`. Harmless, but noise in any stat table.
- Unstained Scout 1 HP and speed 0, Guardian speed 0. Safe only because `ProtectedNpc` refuses survival damage.
- `phantasmal_echo` and `spectral_companion` have 1 HP; intended (the Intercession loses duration, not health).
- Circus performers (Fire Eater, Stilt Walker, Acrobat, Knife Thrower) have no `ATTACK_DAMAGE` attribute; their damage is hard-coded in each class (4, 3, 4). Tuning them means editing four classes rather than one table.
- Vampire Bat damage (1, or 2 aggregated) is hard-coded in `VampireBatEntity` on a vanilla Bat base.

### 5.3 Time to kill against neighbours

- **Overworld D0 to D2 band.** Most hostiles take 3 to 4 iron-sword hits and deal 2 to 4. The Venom-Rib Centipede (34 HP, 6 damage, poison and binding) takes 6 hits and kills a bare player in 4; it is the strongest wandering overworld mob and spawns in forests, jungles, swamps and lush caves (weight 4). The Chthonian Queen (60 HP, 6 armour) takes 12 iron-sword hits and kills a bare player in 3; she is a miniboss in a D0 biome.
- **Desiccant blood drain is out of scale with its body.** 12 HP and 3 damage, but each sting takes 250 mL (5% of the base pool, four minutes of plain regeneration). The Lantern Tick, the other blood-draining parasite, takes 8 mL per 160 ticks. Thirty times apart for two creatures of the same band.
- **Gardens D7 to D8 band is soft.** Fungling 7/1, Fargone 16/3, Thirster 20/4, Lump 24/5, Hunter Fungus 26/5, Eruptus 30/6, Abhorrent Thought 40/7. With the Hematic Iron Sword the whole roster dies in 1 to 4 hits and, against a full Hemomancy set, needs 12 to 97 hits to kill the player. The same Thirster (20/4) is a plain overworld-strength zombie. The Gardens are the D7 to D8 place; their mobs sit level with the D0 Bog Revenant (22/2/4). The Facsimile Golem (100/15) is the only creature there that tests a D8 player, and it only fights back.
- **End band.** Myelin Borer 8/0/2 and Choir Keeper 10 (passive) are also at overworld strength for a D5 to D6 place; the Borer's Neural Overload is the real threat.
- **Nether basin step.** Bombardier 28/8 (neutral), Excoriated 52/6/8, Phlegethosoma 120/6/7. The Excoriated is double anything wild in the Overworld; fine for a D5 commission, sharp if a D2 player wanders into the Basin.
- **Will scaling.** Broken tiers top out at 38 HP / 6 damage; a sent Will at D8 for a 20 HP player is `max(28, 24 + 16) = 40` HP and 8.5 damage. A D8 player with Living weapons kills it in 4 to 5 blows. The Will is the only Degree-scaled enemy and it scales gently.
- **Hematic Construct** (chapel puzzle guard, D3 to D4) is 12 HP with 8 damage: dies to two iron-sword hits but kills a bare player in 3. A glass cannon by design or two numbers swapped.
- **Mnemonic Whale** bites for 12 every second, but `isFeedingPrey` limits it to squid and Prism Cuttles; no risk to players.

### 5.4 Drops

Every item that any mob drops is used somewhere (a recipe, a ritual or code). No orphan drops.

Wild creatures that drop nothing on death (empty table or no table, and no code drop): Pyrosome, Hagfish, Blood Lantern Jelly, Antibody, Bloodlicker, Velorum Devotee. The Mnemonic Whale (sampling) and Verdigris Moth (shedding) give through living interaction, and the Will through `WillCombatRules.lootFor` (its `will.json` is empty). Bosses without tables reward in code (section 4); the Bloom Warden gives nothing, and the Kindled Champion gives only a title. Two reward paths are dead: `vesper_evening_star.json` (Memory of Vesper) and the Evening Star's xpReward 180 never fire because he is discarded, not killed; the Memory arrives through `VesperOrdealManager.completeVictory` instead. The Saints' xpReward (80 to 120) is unreachable for the same reason.

### 5.5 Blood profiles that give nothing

- 29 living types have no profile file at all (empty profile, ordinary vials). Among wild creatures: Leech, Fargone, Thirster, Blood-Drunk Puppeteer, Enthralled Doll, Barbed Urchin, Peacock Spider, Vampire Bat, Morphling Polyp, Antibody, Tooth Pecks, Hemolymphopoda, Lantern Tick, Myelin Borer, Chthonian, Verdigris Moth, Choir Keeper, Bloodlicker. Several are written as blood creatures (the Fargone "mosquito-like blood monster", the Thirster, the Vampire Bat, the Lantern Tick and Leech parasites) and yield no Temper.
- Many profile files exist but list no tempers and no properties, only `requires_living_syringe: true`: every boss, every Saint, every NPC, the Court roles, the Puppeteer summons, the Bog Revenant, Abhorrent Thought, Eruptus, Lump of Thought, Chthonian Queen, Mnemonic Whale. For people and bosses that is a deliberate "consent or Living Syringe" gate; for the Gardens mobs, the Whale and the Queen it means a Living Syringe draw yields blood with no Temper.
- Tempers that are well covered: Ferric (Chitinite, Chiton, Snail, Burrower, Centipede, Hematic Construct, Facsimile Golem), Animus (Doe, Strider, Hemojelly, Pyrosome, Lantern Jelly, Facsimile horse and donkey). Thin: Lux (Luminal Cicada, Facsimile Blaze only), Congeatio (no wild creature found), Mortem (Hagfish, Osteophage, Brined Votary).

### 5.6 Keeping under the menagerie rules

- The Chthonian Queen (60 HP) is exactly at the jar limit (`SpecimenCaptureRules.MAX_HEALTH = 60`) and is in `specimen_jar_capturable`, so a "boss tier" queen goes in a jar.
- The Venom-Rib Centipede (1.85 wide, 34 HP) fits the jar but not the vat, which is 1.5 wide at most; a jar is the only vessel for the strongest wandering overworld mob. The Fargone (1.0 x 1.8, 16 HP) fits both.
- The Excoriated is not a boss by package (`entity/mob/monster`) and can be leashed below a third of its health; the Phlegethosoma (`entity/boss/...`) cannot. Fine, but note the rule is package-based: moving a class changes whether it can be kept.
- The Abhorrent Thought (40 HP, 1.5 x 3.25) and Mortarbound can be leashed but fit no vessel; the Mnemonic Whale (4.2 wide) can be leashed but fits nothing.
- Brood cost is `30 mL x max health`: a Facsimile Golem brood is 3000 mL, more than half the base blood pool.

## 6. Docs that differ from code

Code is authoritative in every case.

- `docs/HEMOMANCY_REFERENCE.md` §28.1 and §5.6c describe `fungal_gardens` as a "TerraBlender infection zone"; nothing there mentions that its JSON monster list is two 8-8 packs, and `BiomeInit.fungalGardens` (Java) still holds a different, calmer spawn list than the shipped JSON.
- `docs/HEMOMANCY_REFERENCE.md` §26 "Entity blood profiles": "The current resources ship 153 profiles". `data/hemomancy/blood_profiles` holds 106 files (other namespaces may hold the rest; not checked).
- `docs/HEMOMANCY_REFERENCE.md` §26.1 Lantern Tick: "deals small fallback damage" matches (`FALLBACK_DAMAGE_PER_BITE` 1.0). Thirster 20/4, Abhorrent Thought 40/7/4, Lump 24/5, Eruptus 30/2/6, Osteophage weight 2 and 6 damage, Ice Fish weight 12 and 3 to 5: all match.
- `docs/archive/tracks/core_loop/05-menagerie-and-research.md` says the vat takes "creatures up to about 2 blocks high and 40 health": matches `MenagerieRules`; but it does not mention the 1.5 width limit.

Bosses (`docs/HEMOMANCY_REFERENCE.md`, `docs/BOSSES.md`, `docs/SAINT_MAUSOLEUMS.md`):

- Reference, Phlegethosoma status (about line 6529): drops "one to two Calcified Blood Spines"; `phlegethosoma.json` drops Barbed Splinter (and 2 to 4 Sanguine Formation).
- Reference, Mycophant Hunt (line 1626): Brood Call "spawns two brood funglings"; code spawns 3 in phase 2. Siphon "1.5% of max blood (or 2 health)"; phase 2 is 2.5% and 3 HP. Nectar Surge's 6 damage in r6 (unowned only) is not mentioned.
- Reference §5.10: phase 1 "transitions on defeat"; in code the Crowned Refusal never reaches 0, the third wound rupture at the 12% floor starts the transition. Phase 2 "is the final kill"; the Evening Star is never killed (held at 1 HP, absorbed, discarded), so his loot table and XP never fire. The Temper defence and the Carapace x1.5 window are not documented.
- `docs/BOSSES.md` open question (line 117) says `LORE_REFERENCE` §11 has the Priestess javelin on the standard blood-lance path; code uses `StainedLanceEntity` (12 damage), so the lore doc is the stale one.
- Reference §5.8 (line 1916): each Saint table gives "one Residuum and 1 to 2 Quintessence"; Hemorath's adds the Hemorath Rib, and Quintessence needs a player kill with a Looting bonus. `SAINT_MAUSOLEUMS.md` line 45 repeats this.
- Reference §5.8 Hemorath: "25% of the leak (40% in phase 2) into the nearer of his four wells"; code splits it between the two nearest wells. "125-tick ending": `endTrial` is at 125 of a 130-tick window (also `SAINT_MAUSOLEUMS.md` line 141).
- Reference §5.8 Putriciel: an Offering takes "one hit of at least 4 after armour"; code checks the raw amount before armour. "20 fed ticks of Projection": code needs 100 blood at up to 5 a tick (20 ticks is the minimum).
- Reference §5.6g (lines 1751, 1759): Velorum's congregation "not built"; `VelorumDevoteeEntity` and its allegiance exist. Reference §5.8 line 1972 says no Saint chamber generates naturally; §5.6g and `SAINT_MAUSOLEUMS.md` say the four complexes generate. Hemorath and Seraphae legacy paragraphs (lines 1982, 1984) describe the pre-kit fights with no "superseded" note.
- Reference §5.8 header and `SAINT_MAUSOLEUMS.md` lines 13 and 98: "the Saint is D8"; the Saint itself has no Degree check (the doc's own open question at line 175 says so).
- `SAINT_MAUSOLEUMS.md` line 142: Seraphae "fourteen moves"; the enum has 16 plus the Flood. Line 144: Velorum "fifteen moves and four strings"; code has 21 moves and 7 strings. Line 154: "Saint damage is fixed magic, so the attack bonus mostly helps melee"; `SaintMoves.damage` scales every move by the attack ratio. Same line: "Seraphae and Putriciel ignore the health and Regeneration deeds"; Putriciel's Downfall +50% also raises its 30% floor (120 to 180 HP) and Regeneration still heals.
- Omissions rather than contradictions: Priestess wind blast 4, blood and spore bolt values, the 3 HP heal on drain and the 260 shield cooldown; Latent's Poison 80 on melee and bloom and root cooldowns; the Kindled Champion's grab (3 every 10 ticks).

## 7. Ranked tuning list

Highest impact first. Each item names where the number lives.

1. **Overworld Fungal Gardens 8-packs.** `src/main/resources/data/hemomancy/worldgen/biome/fungal_gardens.json`, `spawners.monster`: `abhorrent_thought` and `fargone` `minCount 8 / maxCount 8` (also `minecraft:bat` 8-8). The JSON is generated: fix it in `tools/worldgen/fungal_gardens_worldgen.py` and re-run. Decide whether `BiomeInit.fungalGardens` (Java) or the JSON is the intended list, and delete the other.
2. **Gardens dimension roster is D0 strength for a D7 to D8 place.** Raise health and damage together: `FunglingEntity.setAttributes` (7 / 1), `FargoneEntity.setAttributes` (16 / 3), `ThirsterEntity.setAttributes` (20 / 4), `LumpOfThoughtEntity.setAttributes` (24 / 5), `HunterFungusEntity.setAttributes` (26 / 5), `ErythromyceliumEruptusEntity.setAttributes` (30 / 6), `AbhorrentThoughtEntity.setAttributes` (40 / 7). Do item 1 first, or the overworld biome gets the stronger versions in packs of 8.
3. **Blood-Drunk Puppeteer daytime spawns.** `BloodDrunkPuppeteerEntity.canSpawnHere` (`checkMobSpawnRules`, swap to `checkMonsterSpawnRules`) and `neoforge/biome_modifier/add_blood_drunk_puppeteer.json` weight 24; `BloodDrunkPuppeteerTuning.DOLL_COUNT` 4.
4. **Desiccant sting cost.** `DesiccantEntity.STING_BLOOD_DRAIN` 250.0 mL per sting, against `LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE` 8.0 per 160 ticks. Pick one scale for creature blood drain.
5. **Template block 7 HP / 1 damage / 0.3 speed.** `AbstractChitiniteEntity.setAttributes` (shared by Fervent Chitinite: give `FerventChitiniteEntity` its own), `FunglingEntity.setAttributes`, `EnthralledDollEntity.setAttributes`, `BarbedUrchinEntity.setAttributes`.
6. **Chthonian Queen as a D0 miniboss that fits a jar.** `ChthonianQueenEntity.setAttributes` (60 HP, 6 armour, 7 damage) and `SpecimenCaptureRules.MAX_HEALTH` 60 (she sits exactly on the limit); remove her from `tags/entity_type/specimen_jar_capturable.json` or raise her health.
7. **Mycophant phase split.** `MycophantCombatRules.phase` (50% to phase 2), `MycophantLastBroodRules` (trigger 25%, `DAMAGE_SCALE` 0.5, `FLOOR` 0.05). Phase 1 is 360 HP of a 720 bar; phase 2 before the Last Brood is 180.
8. **Annetta's 350 HP does nothing.** `AnnettaEncounterRules.BREAK_MIN_TICKS` 2400 and `BREAK_MIN_DAMAGE` 50 decide the fight; either lower `AnnettaKnowlesEntity.setAttributes` MAX_HEALTH to match, or make the break health-based.
9. **Seraphae phase 1 is 10 integrity points.** `SeraphaeIntegrityRules` START 30 and `SeraphaePhases` thresholds 40 / 75 (bands 10 / 35 / 25).
10. **Putriciel Rot II creeps slower than Rot I.** `PutricielPhase` Kindling creep ticks per point 12 (Rot I) / 20 (Rot II) / 15 (Rot III), and the 2 / 2 / 1 absolution split (`PutricielAbsolutionRules.NEEDED` 5).
11. **Crowned Refusal first band is short and 12% is never used.** `VesperCombatRules.ANCHOR_THRESHOLDS` {0.72, 0.42, 0.12}: bands 145.6 / 156 / 156, last 62.4 HP unused. 0.70 / 0.40 / 0.10 would even them and waste less bar.
12. **Kindled Champion last band.** `KindledChampionRules.BUD_THRESHOLDS` {0.66, 0.33}, `REGROWTH` {2.0, 3.0, 4.5}, `YIELD_AT` 0.1: the shortest band regrows fastest.
13. **Venom-Rib Centipede is the strongest wandering overworld mob.** `VenomRibCentipedeRules.MAX_HEALTH` 34, `ATTACK_DAMAGE` 6 (poison 160t + binding 50t) at weight 4 in common forests.
14. **Hemolymphopoda crowds the ambient cap.** `neoforge/biome_modifier/add_hemolymphopoda.json` weight 30, packs 2 to 5; also `rockpool_shore.json` 30 and `mycelial_depths.json` 20 (3 to 8).
15. **Hematic Construct 12 HP / 8 damage.** `HematicConstructEntity.setAttributes` and `BASE_DAMAGE` 8.0: confirm the glass-cannon intent.
16. **Dead Evening Star rewards.** `loot_table/entities/vesper_evening_star.json` and `VesperTheEveningStarEntity` xpReward 180 never fire; delete or route through `completeVictory`. Same for the Saints' xpReward (80 to 120).
17. **Blood profiles that give nothing.** Add tempers in `data/hemomancy/blood_profiles/` for the blood-themed creatures with no file (Fargone, Thirster, Vampire Bat, Lantern Tick, Leech, Bloodlicker, Tooth Pecks) and for the Gardens mobs with empty lists. Lux and Congeatio have almost no wild sources.
18. **Unused attack attributes on NPCs.** `HarbingerAlchemistEntity`, `HarbingerArtificerEntity`, `HarbingerCicatrixAnchoriteEntity`, `HarbingerMnemonistEntity`, `CircusFacultyEntity`: ATTACK_DAMAGE 4 with no attack. Cosmetic.
19. **Hard-coded mob damage.** Circus performers (`CircusFireEaterEntity` 4.0F, `CircusStiltWalkerEntity` 3.0F, `CircusAcrobatEntity` 4.0F) and `VampireBatEntity` (1.0F / 2.0F): move into attributes or a rules class so they can be tuned with the rest.
20. **Docs.** Fix the mismatches in section 6, starting with the Phlegethosoma drop, the Vesper "defeat" wording and the Saint move counts.

## 8. What could not be determined by reading

- Real spawn density: weights are read, but chunk-level counts depend on caps, light and terrain that only a running world shows (especially the Overworld Fungal Gardens 8-packs and the Puppeteer's daytime spawns).
- Effective damage of hard-coded and projectile attacks (Circus knives, Marrow Spitter shots, Veinwing feathers, Cinder Bellows, Iron Spike, Blood Cloud) needs the projectile classes; only the entity side was read.
- Vanilla-based mobs (Facsimiles, Peacock Spider, Vampire Bat) assume vanilla 1.21.1 base values and difficulty scaling; difficulty multipliers on damage are not applied in the CSV.
- Expected Degree is inferred from place, not gated in code.
- Whether the empty-profile Gardens mobs and empty loot tables are intended.
