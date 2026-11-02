# Manipulation Balance & Placement Plan

Status: **first and second passes implemented in the working tree (uncommitted)** — see [§10 Implementation status](#10-implementation-status) for pass 1 and [§11 Pass 2](#11-pass-2) for pass 2.
Audit date: 2026-10-09, against the working tree on `neo-1.21.1`.

This plan reviews every active blood manipulation for power, cost, school identity and tendency placement. It is based on reading `ManipulationInit`, every active manipulation class, the shared school-state layer (`common/damage/SchoolCombatEvents.java`, `SchoolStates.java`, `SchoolStateRules.java`) and §8 of [the developer reference](../../HEMOMANCY_REFERENCE.md). Code is treated as authoritative where the reference disagrees.

All numbers are **base values**: no Crimson Mastery (+15% per level on manually computed damage), no tendency-affinity multiplier, no armor. Nothing here has been playtested; proposed values are starting points for tuning, not final numbers.

---

## 1. Roster snapshot

`ManipulationInit` registers 117 entries: 102 active and 15 retired compatibility IDs (`ManipulationRetirementRules`). Counts below are active entries by **primary** tendency and rank. Saint canon memories and `conjure_*` staff forms are included.

| Tendency | Total | Humilis (D0) | Mediocritas (D1) | Summa (D3) | Magister (D5) | Perfectus (D6) |
|---|---|---|---|---|---|---|
| **Animus** | **30** | 14 | 3 | 11 | 1 | 1 |
| Mortem | 14 | 4 | 4 | 2 (both Saint) | 2 | 2 |
| Flammeus | 12 | 6 | 3 | 1 | 1 | 1 |
| Ductilis | 11 | 3 | 4 | 1 | 1 | 2 |
| Ferric | 11 | 2 | 4 (3 fixed utilities) | 3 | 2 | **0** |
| Tenebris | 9 | 2 | 5 | **0** | 1 | 1 |
| Congeatio | 8 | 2 | 2 | 2 (1 Saint) | 1 | 1 |
| Lux | **7** | 1 | 4 | 1 (Saint) | **0** | 1 |

<details>
<summary>Full placement list</summary>

- **Animus** — H: `blood_shot`, `guided_blood_shot`, `hematic_mortar`, `sanguine_halo`, `blood_binding`, `lingering_blood_binding`, `chain_blood_binding`, `blood_lattice`, `sanguine_marionette`, `blood_needle`, `blood_needle_fan`, `blood_needle_lance`, `blood_rush`, `vital_effusion` · M: `hematic_rebuke`, `hematic_riposte`, `conjure_blade` · S: `hematic_impressment`, `summon_avatar` (+4 forms), `blood_cloud` (+3 forms), `blood_aneurysm` · Ma: `sovereign_instinct` · P: `crimson_coronation`
- **Ductilis** — H: `deadly_gaze`, `synaptic_jolt`, `hemolymphal_pulse` · M: `activation_potential`, `conductive_mark`, `sanguine_ward`, `conjure_crossbow` · S: `thread_ripper` · Ma: `living_circuit` · P: `synaptic_storm`, `axonal_transduction`
- **Ferric** — H: `sanguine_mending`, `vascular_dowsing` · M: `conjure_staff`, `blood_absorption`, `blood_projection`, `iron_retort` · S: `sanguine_magnetism`, `ferric_rampart`, `ferric_spikes` · Ma: `ironhearted`, `iron_choir` · P: —
- **Flammeus** — H: `crimson_flame_conjuration`, `sanguine_ignition`, `scalding_updraft` (+3 forms) · M: `conjure_torch`, `pyretic_forge`, `cauterizing_rebuke` · S: `vitric_combustion` · Ma: `furnace_veins` · P: `phoenix_debt`
- **Congeatio** — H: `glacial_grasp`, `cryogenic_pulse` · M: `conjure_flail`, `glacial_rampart` · S: `osseous_bloom`, `endless_hour` (Saint) · Ma: `rimebound_sentence` · P: `absolute_stillness`
- **Lux** — H: `hematic_flare` · M: `conjure_spear`, `prismatic_reproof`, `hematic_beacon`, `lumen_suture` · S: `unclosing_eye` (Saint) · Ma: — · P: `white_verdict`
- **Mortem** — H: `hemorrhage`, `lignum_mortis` (+2 forms) · M: `conjure_axe`, `exsanguinate`, `insatiable_hunger`, `grave_debt` · S: `crimson_tithe` (Saint), `bloom_of_rot` (Saint) · Ma: `blackhearted`, `carrion_communion` · P: `conjure_sickle`, `funeral_bell`
- **Tenebris** — H: `void_shroud`, `gloam_laceration` · M: `conjure_claws`, `umbral_step`, `umbral_reversal`, `blood_eclipse`, `black_veil_covenant` · S: — · Ma: `penumbral_drift` · P: `eclipse_well`

</details>

**Observations**

- Animus holds about 30% of the catalogue. Most of that is the Avatar, Cloud, Binding, Shot and Needle families.
- Outside Animus, the Summa tier (Degree 3–4) is close to empty. Tenebris has nothing there; Lux and Mortem only have Saint canon memories; Ductilis only has the Circus-specific Thread Ripper.
- The retirement pass removed four entries each from Lux (`blood_lamp`, `hemosynthesis`, `crimson_sight`, `vigil_of_glass`) and Ferric (`ferric_resonance`, `ferric_transmutation`, `hematic_ballast`, `sanguine_excavation`). Neither school was backfilled. Lux lost its only Magister and its only crude starter shard (`crude_memory_blood_lamp`).
- Vein-section load is skewed: BODY 29, HEAD 27, ARMS 25, LEGS 11, HEART 6.

---

## 2. Damage and cost comparison

Single target, full charge where relevant.

| Manipulation | Rank | Cost (mL) | Cooldown | Damage | Notes |
|---|---|---|---|---|---|
| `blood_shot` | H | 100 | 10t | **9** | Arrow: speed 4.5 × base 2.0 |
| `blood_needle` | H | 100 | 10t | **8–16** per needle, 10–20 needles | Single target limited by i-frames; huge vs groups |
| `blood_needle_lance` | H (form) | 150 | 25t | **~16 × 3, piercing** | Speed 8 × base 2.0 |
| `hematic_mortar` | H (form) | 200 | 60t | 4–8 AoE, r4 | Requires full charge |
| `sanguine_halo` | H (form) | 220 | 120t | 5 × 9 | Seeking orbit |
| `deadly_gaze` | H | 100 | 20t | 6 (delayed slam) | Plus lift/interrupt |
| `synaptic_jolt` | H | 150 | 25t | 3 | Plus Disrupted |
| `hematic_flare` | H | 125 | 30t | 3 | Plus Illuminated |
| `gloam_laceration` | H | 175 | 35t | 3.5 (6 ambush) | Plus Blood Loss |
| `sanguine_ignition` | H | 125 | 25t | 2 AoE, r5 | Plus Searing |
| `cryogenic_pulse` | H | 150 | 30t | 3 AoE, r5 | Plus Rime |
| `hemorrhage` | H | 75 | 20t | ~12 over 12s | Blood Loss deals 0.5 per tick, i-frame limited to ~1 DPS on mobs |
| `glacial_grasp` | H | 125 | 20t | 0 | Wet targets only |
| `activation_potential` | M | 200 | 30t | 5 AoE, r5 | |
| `prismatic_reproof` | M | 325 | 80t | **2** (4 vs Illuminated) | Weakest paid attack |
| `blood_eclipse` | M | 300 | 45t | 3, cone | Plus Obscured |
| `blood_cloud` | S | 300 | 40t | ~4 DPS for 5s, AoE | 2 per tick, i-frame limited |
| `sanguine_tempest` | S (form) | 600 | 120t | ~4 DPS for 8s + 4/s lightning | |
| `blood_aneurysm` | S | 400 | 40t | 8 + 3 splash (+2–6 Pressure rupture) | |
| `vitric_combustion` | S | 350 | 60t | 8 AoE, r4 (+1–4 Searing) | |
| `osseous_bloom` | S | 600 | 60t | 25% current HP, AoE r6 | Boss cap 12 |
| `rimebound_sentence` | Ma | 750 | 120t | 2–8 + cage | |
| `synaptic_storm` | P | 900 | 100t | 2–8 per target, up to 8 chained | Plus paralysis |
| `white_verdict` | P | 800 | 100t | 2–10 (×1.5 vs Illuminated/Veiled) | Beam |
| `funeral_bell` | P | 1000 | 160t | **4 + up to 6 Necrosis debt**, AoE r4–10 | |
| `crimson_coronation` | P | 600 | 120t | ~11 per lance × 8, reactive | Needle base 3 × speed 3.4 |

Defensive comparison:

| Manipulation | Rank | Cost | Effect | Approx. efficiency |
|---|---|---|---|---|
| `sanguine_ward` | M | **10/s** | 6-point pool, +2 per pulse up to 8 | ~0.2 HP/mL |
| `iron_retort` | M | 250 | One direct hit halved + 4 retaliation, 60t | — |
| `hematic_riposte` | M | 60 | 9-tick frontal parry | Skill-gated |
| `ironhearted` | Ma | 300 | +4 Iron Heart HP (cap 10 + scars), 10 min | ~0.013 HP/mL |
| `iron_choir` | Ma | 250/s | Resistance I + up to 3 projectile intercepts per pulse | — |
| `endless_hour` | S (Saint) | 600 | **Resistance IV + Absorption V for 10s**; only lethal overflow is deferred | Near-invulnerability |
| `phoenix_debt` | P | 2000 | Cheat death once per 6000t | — |
| `summon_avatar*` | S | 75–400/s | 25–65% damage reduction plus attributes | — |

---

## 3. Balance outliers (priority order)

### 3.1 Arrow-based Animus projectiles out-damage capstones — **High**

`BloodShotEntity` and `BloodNeedleEntity` extend `AbstractArrow` and never call `setBaseDamage`. Vanilla arrow damage is `ceil(speed × baseDamage)` with base 2.0. So:

- Blood Shot (speed 4.5): **9** per cast, 100 mL, 10t cooldown.
- Blood Needle (speed 4–8): **8–16** per needle.
- Needle Lance (speed 8, pierce 3): **~16** per needle, three needles, 150 mL.

That puts a Humilis form above every Perfectus single-hit spell (Storm 8, Verdict 10, Bell 4–10). These projectiles also bypass Crimson Mastery, which every manually computed spell uses.

**Proposal**

- Set an explicit base damage on manipulation-fired shots and needles so the result lands in the starter band. Targets: Shot ~4–5, baseline needle ~1.5 each, Fan ~1 each, Lance ~6 each.
- Apply `SkillPointHelper.getCrimsonMasteryMultiplier` to that base, the same way the entity casts (`EntityCasts`) and Coronation already call `setBaseDamage`.
- Keep Mortar's explicit 4–8 AoE, or reduce it to 3–6 so it stays below Vitric Combustion.
- Leave Crimson Coronation's lance formula (`1 + 2 × strength`) alone; it already sets base damage.

Files: `common/manipulation/animus/BloodShotManip.java`, `BloodNeedleManip.java`, `common/entity/projectile/BloodShotEntity.java`, `BloodNeedleEntity.java`.

### 3.2 Sanguine Ward is underpriced — **High**

`ManipulationReactiveEvents.refreshSanguineWard` creates a 6-point pool and adds 2 per one-second pulse, up to 8, for 10 mL/s. That is roughly fifteen times more HP per mL than Ironhearted.

**Proposal**

- Raise the cost to 40–60/s, **or** stop refilling while the pool is above zero (refill only after it breaks).
- Give it a Ductilis identity: when the pool absorbs a hit, apply a short Disrupted or a small shock to the attacker. That way it stops competing with Ferric as a generic shield (see §5.5).

### 3.3 Endless Hour is near-free invulnerability at Degree 3 — **High**

`EndlessHourManip` grants Resistance IV (80% reduction) and Absorption V for 10 seconds. `deferDamage` only defers the portion of a hit that exceeds health plus absorption. Unless the player would have died, nothing is repaid. The reference says it "repays the full accumulated damage". In practice it outclasses Phoenix Debt (Perfectus, 2000 mL).

**Proposal** (pick one)

- Match the doc: record **all** damage prevented by the effect and repay it on expiry, or
- Keep overflow-only repayment but drop to Resistance II and Absorption II, and raise cost to ~900.

### 3.4 Funeral Bell underperforms and has few setup sources — **Medium**

`FuneralBellManip` deals `4 × charge`, and `SchoolCombatEvents.prepare` adds only stored Necrosis debt (capped at 6). The reference claims Wither, Poison, Blood Loss and Grave Debt also add damage; they do not.

Necrosis is applied only by DIRECT Mortem school hits. Hemorrhage, Insatiable Hunger and Grave Debt apply their effects directly and deal no Mortem hit, so they never build Necrosis. Today the realistic Necrosis sources are Carrion Communion, Bloom of Rot (Saint), the Living Axe and the Bell itself.

**Proposal**

- Raise base damage to ~6–8 at full charge.
- Implement the documented bonus: +2 per distinct affliction (Wither, Poison, Blood Loss, Grave Debt), consumed on detonation.
- Have Hemorrhage apply one Necrosis level alongside Blood Loss so the Mortem starter feeds the capstone.

### 3.5 Prismatic Reproof is the weakest paid attack — **Medium**

2 damage (4 on Illuminated) in a cone for 325 mL on an 80t cooldown. **Proposal:** 4 base (8 on Illuminated), or cut cost to ~200 and cooldown to 50t.

### 3.6 Glacial Grasp does nothing to dry targets — **Medium**

`GlacialGraspManip` only applies Rime to targets standing in water, bubbles or rain. As the Congeatio crude starter, it frequently hits nothing. **Proposal:** dry targets in the path receive 1 Rime level and small freeze damage (~2); wet targets keep the current stronger effect (2 levels in rain/water plus Frozen Cruor).

### 3.7 Hematic Mortar overlaps Vitric Combustion — **Low**

Mortar (Humilis form, 200 mL) matches Vitric (Summa, 350 mL) in radius and peak damage. Resolved by §3.1 (reduce Mortar), or by giving Vitric a larger radius at full charge.

---

## 4. School-state loops

Every tendency's DIRECT hits apply a state (`SchoolStates.primaryState`), and specific abilities consume it in `SchoolCombatEvents.prepare`.

| Tendency | Applies | Consumed by | Loop status |
|---|---|---|---|
| Animus | Pressure | Blood Aneurysm, fully charged Living Blade | **Complete** |
| Flammeus | Searing | Vitric Combustion | **Complete** |
| Lux | Illuminated | Prismatic Reproof, White Verdict | **Complete** (Reproof underpowered) |
| Mortem | Necrosis | Funeral Bell | **Thin**: few Necrosis sources (§3.4) |
| Congeatio | Rime | Ferric hits on 3 Rime (+4) | **Broken**: Ferric has almost no direct-damage spells |
| Ferric | Lodestone | Ferric knockback toward magnetic pillars | **Broken**: same reason |
| Ductilis | Disrupted + conductive arcs | Conductive Mark arcs, Synaptic Storm hop range | Works |
| Tenebris | Obscured | Perception only | **No damage payoff** |

**Proposals**

- Give Ferric real direct-damage spells (§5.1 needle move, §6 Ferric Perfectus). That fixes both the Ferric and Congeatio rows at once.
- Add a Tenebris consumer. For example, Gloam Laceration's ambush bonus also triggers against Obscured targets and consumes the state, or the new Tenebris Summa (§6) detonates Obscured.

---

## 5. Placement changes

### 5.1 Blood Needle family → Ferric primary, Animus secondary — **Recommended**

The §9 tendency table defines Ferric as "Iron, Barbs, Solidity". Needles are hardened blood barbs, and the family already carries a Ferric secondary. Moving `blood_needle`, `blood_needle_fan` and `blood_needle_lance`:

- gives Ferric a starter attack and a working Lodestone / Rime-shatter loop;
- takes three entries off Animus;
- keeps Crimson Coronation (Animus Perfectus) as the Animus needle capstone.

Work: swap primary/secondary in `ManipulationInit`; move the classes to `common/manipulation/ferric/`; update `ManipulationTreeInit` placement, memory-weaving catalysts/enzymes, lang descriptions and the reference catalogue; check Drudge action text.

### 5.2 Hematic Rebuke / Impressment and Sanguine Marionette → Ductilis primary — **Recommended**

The lore defines Ductilis as carrying impulse through nerves, and Ductilis paralysis as "the hostile interruption of voluntary nerve signals". Mob command is nerve control, and Thread Ripper (puppet tethers) is already Ductilis. Moving these gives Ductilis a coherent command/puppetry lane and fills its thin Summa with Impressment.

- `hematic_rebuke` (M) and `hematic_impressment` (S): primary Ductilis, secondary Animus.
- `sanguine_marionette` is currently a Blood Binding form (mastery 3). Either keep it in that family as an Animus/Ductilis exception, or move it into the Rebuke family as a mastery-2 form.
- `sovereign_instinct` stays Animus Magister, reframed as blood frenzy, so Animus keeps a Magister.

### 5.3 Blood Cloud family → Mortem primary — **Optional**

The family already has a Mortem secondary. Moving it gives Mortem a non-Saint Summa line and trims Animus by four more. Weigh this against Animus losing its main AoE; Aneurysm and Mortar would remain. If moved, the cloud should apply Necrosis instead of Pressure, which also feeds Funeral Bell.

### 5.4 Deadly Gaze — **Bookkeeping only**

Already Ductilis/Tenebris in code, and its nerve-eye visuals fit Ductilis. But the class lives in `common/manipulation/animus/`, and §8.3 labels `crude_memory_deadly_gaze` as "Starter Animus". Move the class to `ductilis/` and relabel the shard as the Ductilis starter.

### 5.5 Sanguine Ward — **Keep in Ductilis, change behaviour**

Moving it to Ferric would deepen Ferric's all-defense problem. Keep it Ductilis and give it the reactive shock from §3.2.

### 5.6 Crimson Flame Conjuration — **No move**

Flammeus primary with an Animus secondary is correct. The reference lists it as Animus; fix the doc.

### 5.7 Tendency concept table (§9) — **Doc update**

- Animus lists "Life, Regen", but Animus has no healing manipulation. Healing lives in Lux (Lumen Suture, Hematic Beacon) and Mortem (Carrion Communion).
- Lux lists "Flight", but flight is Flammeus (Updraft family).

Either update the concepts to match the roster, or use the new slots in §6 to add an Animus regeneration effect and a Lux mobility effect.

### 5.8 Resulting roster (if 5.1 and 5.2 land, 5.3 skipped)

| Tendency | Before | After |
|---|---|---|
| Animus | 30 | 24–25 |
| Ductilis | 11 | 13–14 |
| Ferric | 11 | 14 |

---

## 6. Gaps to fill

| Gap | Why it matters | Direction |
|---|---|---|
| **Lux Magister** | Lost with `vigil_of_glass` | A sustained Lux working, e.g. a channelled radiance that heals allies and Illuminates enemies, or a Lux flight/glide effect matching the §9 concept |
| **Lux crude starter shard** | Only shard was `crude_memory_blood_lamp` (retired) | Add `crude_memory_hematic_flare` and put it in outpost loot alongside the other starters |
| **Tenebris Summa** | Tier is empty | An offensive shadow working that consumes Obscured for damage (closes the §4 loop) |
| **Tenebris identity** | 4 of 9 are movement/stealth; Penumbral Drift is a held Void Shroud | Make Drift distinct (e.g. phasing through mobs, or an ambush damage multiplier on the first hit), or fold it into a Void Shroud family form and use its Magister slot for something else |
| **Ferric Perfectus** | Tier is empty | A Ferric offensive capstone that cashes in Lodestone and Rime, e.g. a charged iron lance or magnetic crush |
| **Flammeus Mediocritas attack** | Tier is Pyretic Forge (utility), Cauterizing Rebuke (self-cleanse), Conjure Torch | A targeted Searing applicator between Ignition and Vitric |
| **Congeatio early offense** | Smallest real school; starter is situational | Fix Glacial Grasp (§3.6); consider a Mediocritas Rime projectile |
| **Ductilis solo Magister** | Living Circuit fails without a teammate | Fall back to the caster, as Lumen Suture already does |
| **Mortem non-Saint Summa** | Both Summa entries are Saint canon memories | §5.3, or a new Necrosis-spreading working |

---

## 7. Reference drift to fix

§8.2 of [the reference](../../HEMOMANCY_REFERENCE.md) predates the school-state rework. Many entries describe vanilla effects (Slowness, Blindness, Invisibility, Wither) where the code now applies Rime, Obscured, Veiled or Necrosis: Cryogenic Pulse, Glacial Grasp, Osseous Bloom, Blood Eclipse, Void Shroud, Bloom of Rot, among others.

Numeric mismatches found:

| Manipulation | Reference | Code |
|---|---|---|
| `crimson_tithe` cost | 400 | 100 |
| `crimson_coronation` cost | 1000 | 600 |
| `vitric_combustion` cost | 500 | 350 |
| `conductive_mark` cost | 225 | 150 |
| `hemorrhage` cost | 100 | 75 |
| `furnace_veins` cost | 250/s | 150/s |
| `penumbral_drift` cost | 175/s | 75/s |
| `vascular_dowsing` cost / cooldown | 500/s, 400t | 75/s, 40t |
| `hemolymphal_pulse` cooldown | 20t | 300t |
| `vital_effusion` cooldown | 20t | 60t |
| `crimson_flame_conjuration` tendency | Animus | Flammeus / Animus |
| `blood_cloud` tendency | Animus | Animus / Mortem |

Behavioural mismatches:

- **Funeral Bell**: reference says Wither, Poison, Blood Loss and Grave Debt add damage; code only consumes Necrosis debt.
- **Bloom of Rot**: reference says Wither II, Poison I and Slowness III; code applies Necrosis (plus self-Poison).
- **Endless Hour**: reference says full damage is repaid; code repays only lethal overflow.
- **Hematic Flare**: reference says +2 damage vs concealed targets; `HematicFlareManip` has no such bonus.
- **Blood Shot**: reference calls the baseline "tracking"; only the Guided form homes.

---

## 8. Implementation order

1. **Arrow damage normalization** (§3.1). Biggest power outlier, smallest change.
2. **Reprice Sanguine Ward and Endless Hour** (§3.2, §3.3).
3. **Buff Funeral Bell and Prismatic Reproof; fix Glacial Grasp** (§3.4–3.6), including Hemorrhage feeding Necrosis.
4. **Re-home the Needle and Command families** (§5.1, §5.2) and the Deadly Gaze bookkeeping (§5.4).
5. **Fill the gaps** (§6): Lux starter shard first (cheap), then Tenebris Summa, Ferric Perfectus, Lux Magister, Flammeus Mediocritas.
6. **Resync §8.2 and §9 of the reference** (§7, §5.7), plus any affected `wiki/*.md` pages.

Each step should keep the existing acceptance standards: rank gates through `ManipulationRankGates`, family mastery through `ManipulationFamilyRegistry`, server-authoritative targeting and payment, and the visual standard in [MANIPULATION_VISUALS.md](../../MANIPULATION_VISUALS.md) for any new focal effect.

## 9. Open questions

- Should Animus remain the "default combat" school with deliberately more entries, or should all eight schools converge on a similar size?
- For moved families, do existing players keep their learned entries (tendency change only), or does a move need a migration note for tree placement and mastery?
- Is Thread Ripper meant to stay a Circus-only tool, or should it gain general use so it can count as Ductilis's Summa?
- Should Saint canon memories count toward a school's tier coverage, or be treated as a separate reward track?

---

## 10. Implementation status

Implemented 2026-10-09. Sections 1–9 above are the original audit and are left as written; this section records what changed and where the implementation deviated.

### Done

| Plan item | Result |
|---|---|
| §3.1 Arrow damage | Player-cast Blood Shot sets base 1.0 (5 damage); needles base 0.4 / fan 0.4 / lance 0.75 (2–4 / ~3 / 6). All scale with Crimson Mastery. Mortar bursts 6→3 with mastery. Mob-cast shots in `EntityCasts` keep their own bases. |
| §3.2 Sanguine Ward | 40 mL/s; pool 6, +1 per pulse, cap 6; an absorbed direct melee hit jolts the attacker (Disrupted + jolt visual). |
| §3.3 Endless Hour | Removed the stacked Resistance IV + Absorption V. The hour now only holds lethal overflow as debt, which matches its tooltip. Cost unchanged. |
| §3.4 Funeral Bell | 7 base; +2 per Wither, Poison, Blood Loss or Grave Debt, all scaled by charge; Wither/Poison/Blood Loss are spent, Grave Debt is left to pay out. Hemorrhage now seeds Necrosis. |
| §3.5 Prismatic Reproof | 4 base (8 on Illuminated), cooldown 50t. |
| §3.6 Glacial Grasp | Dry targets in the path take 2 freeze damage and one Rime level. |
| §4 Tenebris/Ferric payoffs | `SchoolCombatEvents` consumes Obscured for Umbral Rend (+4) and Lodestone for Lodestone Collapse (+4 × charge). |
| §5.1 Needles → Ferric | Primary Ferric, secondary Animus; class moved to `manipulation/ferric/`; tree node in the Ferric cluster; weaving recipes now draw Ferric enzyme (baseline 1, +1 per form stage). |
| §5.2 Command → Ductilis | Rebuke and Impressment are Ductilis / Animus; class moved to `manipulation/ductilis/`; Rebuke's tree node sits under Deadly Gaze; Riposte and Sovereign Instinct now branch from Blood Rush. |
| §5.4 Deadly Gaze | Class moved to `manipulation/ductilis/`; weaving recipe uses Ductilis enzyme instead of Lux; crude shard labelled the Ductilis starter. |
| §5.7 Concept table | §9 of the reference updated for Animus, Ductilis, Lux and Ferric. |
| §6 Lux starter shard | `crude_memory_hematic_flare` added with model, lang, Mnemonist inquiry and outpost loot (weight 3). |
| §6 Tenebris Summa | **Umbral Rend** (`umbral_rend`, 450 mL, 80t): tears every Obscured enemy in a 14-block cone for 5 + 4; fails before payment if none. |
| §6 Ferric Perfectus | **Lodestone Collapse** (`lodestone_collapse`, 900 mL, 70t charge): drags enemies into an aimed point for 3–10, cashes Lodestone; full charge pins. |
| §6 Lux Magister | **Radiant Wake** (`radiant_wake`, 120 mL/s): channelled glide; each pulse heals the caster and allies by 1 and Illuminates enemies. |
| §6 Flammeus Mediocritas | **Searing Lash** (`searing_lash`, 225 mL, 30t): 4 damage, six seconds of Searing (primes Vitric's full +4) and 3s fire. |
| §6 Congeatio early offense | **Cruor Javelin** (`cruor_javelin`, 200 mL, 25t): 4 freeze damage and two Rime levels on one aimed target. |
| §6 Mortem non-Saint Summa | **Fruiting Contagion** (`fruiting_contagion`, 400 mL, 80t): Necrosis carriers take 3 + Wither and spread Necrosis within 4 blocks. |
| §6 Tenebris identity | Penumbral Drift's breaking attack is a 1.5× ambush that applies Obscured. |
| §6 Ductilis solo Magister | Living Circuit runs through the caster when no ally is in reach. This reverses the earlier "Circuit requires allied recipients" decision; the GameTest that pinned it was updated. |
| Save compatibility | Saved known-manipulation entries are NBT copies that kept their learned school and prices. `ManipulationRetirementRules.sanitizeKnownManipulations` (run on every known-manipulation sync) now refreshes each copy from the live registry entry via `BloodManipulation.adoptRegisteredMetadata`, so old saves show and re-save the new tendencies and costs. |
| §7 Reference drift | All listed rows in §8.2 of the reference resynced; six new rows added; counts now 123 registered / 108 active. Wiki `School-Combat.md` and `Blood-Systems.md` updated. |

### Deviations from the plan

- **Searing has no levels.** It is always one level; Vitric's bonus scales with remaining duration. Searing Lash therefore lays a long Searing instead of "two levels".
- **Endless Hour** took a third option: keep overflow-only deferral (the tooltip's contract) and remove the extra buffs, rather than repaying all prevented damage or reducing the buffs.
- **Sanguine Marionette** stays an Animus / Ductilis form of Blood Binding (the plan allowed this). Moving it would split a shared `ManipLevel` family.
- **Blood Cloud (§5.3)** was not moved; Fruiting Contagion fills Mortem's Summa instead.
- **New-spell visuals.** All six new manipulations have bespoke geometry to the [Riposte standard](../../MANIPULATION_VISUALS.md): `UmbralRendGeometry`, `LodestoneCollapseGeometry`, `SearingLashGeometry`, `CruorJavelinGeometry`, `RadiantWakeGeometry` and `FruitingContagionGeometry`, each with a pure rules class (`*Rules`) and JVM tests. New forms `UMBRAL_REND`, `LODESTONE_COLLAPSE`, `SEARING_LASH`, `CRUOR_JAVELIN`, `RADIANT_WAKE` and `CONTAGION` are appended after the existing ordinals. Graded "A? pending in-game review" in the [visual catalogue](MANIPULATION_VISUAL_CATALOGUE.md).
- **Alignment after re-homing.** Casting checks alignment in the *primary* tendency. Rebuke (10) and Impressment (25) now need Ductilis alignment, so a player who reached them through Animus may need to raise Ductilis before casting again. The needle family has no alignment requirement.
- **Memory icons** for the six new spells are first-pass 8×8 glyphs in each school's existing palette. They pass `MemoryOverlayResourceTest` but are placeholder art.

### Resulting roster

| Tendency | Total | Humilis | Mediocritas | Summa | Magister | Perfectus |
|---|---|---|---|---|---|---|
| Animus | 25 | 11 | 2 | 10 | 1 | 1 |
| Ferric | 15 | 5 | 4 | 3 | 2 | 1 |
| Mortem | 15 | 4 | 4 | 3 | 2 | 2 |
| Ductilis | 13 | 3 | 5 | 2 | 1 | 2 |
| Flammeus | 13 | 6 | 4 | 1 | 1 | 1 |
| Tenebris | 10 | 2 | 5 | 1 | 1 | 1 |
| Congeatio | 9 | 2 | 3 | 2 | 1 | 1 |
| Lux | 8 | 1 | 4 | 1 | 1 | 1 |

Every school now has at least one entry at every rank.

### Verification

- `gradlew verifyPackagedClientResources`: only the pre-existing `textures/item/old/sanguine formation/` path (space in a tracked folder name) is rejected.
- `gradlew test`: 2668 tests (16 new: six visual rules classes and the renderer source contract), 8 failures, all pre-existing and unrelated (retired pre-Lumenlace strain name in an OrchestratorReports doc, missing cuttlefish lang, missing Silent Archon/Covenant Mantle textures, Hemojelly blood profile, Phlegethontic Bombardier resource, acolyte lang encoding, two harness tests reading a non-UTF-8 file).
- `gradlew runGameTestServer`: 820 tests. The new `ManipulationBalancePassGameTests` (13 tests, including old-save metadata adoption) and every updated manipulation test pass. Remaining failures are in centrifuge, Circus/puppeteer summons, sampling restrictions, pelagic spawning, Hemojelly, Veinwing Vulture and Cardinal Rite ally fixtures, none touched by this pass; several are entity-loading flakes that pass on other runs.
- Not yet verified in a client: glide feel for Radiant Wake, how the six new effects read in game, and the new icons in the radial.

### Still open

- Combat playtest of every number above.
- In-game review of the six new effects, and final icon art for the six new memories.
- §9 open questions remain unanswered.

---

## 11. Pass 2

Implemented 2026-10-09 to 2026-10-10 in the working tree (uncommitted), from a second audit of the pass-1 result. The task plan is [`superpowers/plans/2026-10-09-manipulation-balance-pass-2.md`](../../superpowers/plans/2026-10-09-manipulation-balance-pass-2.md). Sections 1–10 are left as written; §11.5 corrects the pass-1 claims that turned out to be wrong. Numbers are base values before Crimson Mastery (CM) and affinity unless stated. Nothing here has been playtested.

### 11.1 What changed

**State and reactive bugs**

| Item | Result |
|---|---|
| Searing expiry | `SchoolCombatEvents` drops the Searing meter once it expires or the effect is gone. A mixin already did this for ticking entities, so this is a safety net. |
| Penumbral Drift | A melee swing that breaks the drift now gets the 1.5× ambush and Obscured. The swing tick is stamped before the channel stops. |
| Gloam Laceration | Ambush = `isInvisible()` (an un-Illuminated Veil counts) or darkness. Blood Loss lands only on harmable targets actually hit. |
| Sanguine Ward | A ward recast within 100t of lapsing resumes its depleted pool (+1); only after that does it start a fresh six. This closes the tap-cycle refill. |
| Endless Hour | Debt settles as one `hemomancy:endless_hour_debt` hit that ignores armor, effects, enchantments, absorption and the Sheolic Bastion stance. A Totem of Undying or Phoenix Debt can answer it. The Drudge action is now Absorption I for 10 s (was Absorption III plus Resistance III). New death messages. |
| Phoenix Debt | Shows ready and fires only while the caster holds blood at least equal to its current effective price and its Heart section is not DEAD. |
| Funeral Bell | A partial charge cashes in only that fraction of stored Necrosis debt; the rest releases at expiry. Insatiable Hunger counts as an affliction (+2, spent). |
| Carrion Communion | Insatiable Hunger makes a target eligible. |

**Targeting, payment and bosses**

| Item | Result |
|---|---|
| Boss predicate | `ManipulationReactiveEvents.isBoss` adds the Warden, Elder Guardian and `c:bosses`. Every boss cap reads it. |
| Exsanguinate | Refund 600 → 250 on a kill; boss drain capped at 12 × CM, re-applied after tendency affinity. |
| Osseous Bloom | Uses the shared boss cap; an empty radius is refused before payment. Displayed as **Crystalline Bloom** (lang only; the ID is unchanged). |
| Validation before payment | Hemorrhage, Grave Debt, Insatiable Hunger, Blood Aneurysm, Vital Effusion, Rimebound Sentence and Glacial Rampart refuse in `canPerformAction`. Mob and Will casts keep working. |
| Hostile-only auto-targeting | New `ManipulationCombatHelper.isHostileTarget`: harmable, and a monster, a mob targeting the caster, or a player. Used by Sanguine Halo and Blood Aneurysm. |
| Projectiles | Manipulation Blood Shots and Needles (player and Will) no longer gain weapon enchantments; the weapon still lends its tendency. |
| Ferric Spikes | Contact is a DIRECT Ferric hit (3.5 × CM captured at cast) that applies Lodestone. |
| Lodestone Collapse | Targets need a clear line from the point; walls shield. |
| Glacial Grasp | Rain counts as wet. |
| Hematic Beacon | Illuminates any harmable creature or player, not only mobs. |
| Updrafts | Need footing (ground, water or a ladder). |
| Build permission | `mayAlter` gates Crimson Flame Conjuration, Furnace Veins melting, Glacial Rampart, Glacial Grasp freezing and Lignum harvesting. |
| Blood Binding | A bound player's zeroed velocity is synced, so binding holds players. |
| Sovereign Instinct | Redirects only commandable mobs (80 max HP or less, not bosses); bosses get Pressure. |
| Coronation, Black Veil Covenant | Cannot be recast while the crown or veil is active. |
| Activation Potential Drudge | Shocks `Monster`s within 5 blocks: Disrupted and 3 magic damage (was Regeneration to allies). |

**Cast pipeline**

| Item | Result |
|---|---|
| Charge floor | A CHARGED release below 25% fizzles before payment ("Not enough charge"). Quick, Continuous and Passive are unaffected; full-charge spells are unchanged. |
| Rewards | Alignment and skill points scale with charge; below half charge earns no mastery use and does not count toward the manipulation-use milestones. |
| Server clamp | Charge starts on the server's first `CastingChargePacket`; credited charge is at most the observed hold + max(2, ceil(15% × hold)). A key-down older than 2 × required + 20 ticks is stale and counts as none. No live key-down means a silent refusal. The clock clears with the other session state when a server starts or stops. |
| Aneurysm | The Pressure bonus is 2 × level × charge. |
| Crimson Mastery coverage | Synaptic Storm, White Verdict, Funeral Bell (toll only), Coronation lances, Rimebound Sentence, Iron Choir intercepts, Carrion Communion (pulse, and therefore its heal), Furnace Veins, Scalding Updraft, Deadly Gaze slam, Phoenix eruption, Iron Retort thorns, Grave Debt burst, Blood Cloud (fixed at cast). State DoTs stay flat. |

**Economy, affinity and profiles**

| Item | Result |
|---|---|
| Sanguine Surge / Last Wind | 0.25 / 0.5 mL/tick per level (were 1 and 2). |
| Mixed affinity | `max(primary, 0.75 × primary + 0.25 × secondary)`: a secondary never dilutes. |
| Synaptic patterns | Applying starts a 10 s lockout (session state); newly equipped manipulations start a 2 s cooldown, except passives, Avatar forms and staff forms. |
| Vein strain | Per cast 1/1/2/2/3 by rank × `vascularManipStrain`. CLOTTED adds 25% cost ("Clotted vein"); DEAD refuses before payment. |
| Kill alignment | No Animus default: unclaimed mobs split the shift across their blood-profile tendencies; unprofiled mobs give nothing. |
| Config | New `tendencyShiftOnCastEnabled`; `tendencyShiftOnKillEnabled` now gates kills only. |
| Loot | The Harbinger outpost memory pool adds Prismatic Reproof, Lumen Suture, Searing Lash and Cruor Javelin (weight 2 each). |

**Animus re-rank and command moves**

| Item | Result |
|---|---|
| Ranks | Mediocritas: Hematic Mortar, Sanguine Halo, Blood Lattice, Summon Avatar, Avatar Arms. Magister: Sanguine Tempest, Avatar Complete. Form mastery thresholds are unchanged. |
| Blood Rush | Blood Rush I for 8 s (was II for 12.5 s); cooldown 60t → 160t; heal unchanged. |
| Blood Cloud family | First contact per creature is DIRECT (Pressure once), then periodic; Expansive radius 3 → 4; Tempest is a static storm. |
| Avatar | Damage reduction skips void, `/kill` and starvation. |
| Chain Binding | 120t → 80t. |
| Sanguine Marionette | Ductilis / Animus; a Hematic Rebuke form at mastery 2 (Impressment stays 3); casting it on a Blood-Bound creature takes that creature. Known entries migrate. Recipe: 2 Vivacious + 4 Neurotic. |
| Sovereign Instinct | Ductilis / Animus, Magister, tree node under Hematic Rebuke. Recipe: 3 Neurotic. |
| Secondaries | Sanguine Tempest Mortem → Ductilis (recipe +1 Neurotic); Crimson Tithe Lux → Animus. |
| Class moves | Crimson Flame Conjuration to `flammeus/`, Marionette to `ductilis/`. |

**Per-school tuning and gap fills**

| School | Changes |
|---|---|
| Ferric | Sanguine Magnetism slams pulled enemies within 3 blocks at expiry for 4 (6 if Lodestoned) × CM. Iron Choir fires one needle (~3 × CM) per pulse at the nearest visible hostile within 10. Ferric Rampart 450 → 350 mL, 6 → 10 s, friendly projectiles pass through. |
| Congeatio | Glacial Rampart lasts 12 s. Rimebound Sentence 4 + 8c (was 2 + 6c). Absolute Stillness pulses deal 1.5 × CM to targets at three Rime. Cruor Javelin drives three Rime into wet targets. |
| Flammeus | Suspended and Expulsive Updraft → Mediocritas; Expulsive launches lower (0.6) and bursts for 3 × CM in radius 4 with Searing and knockback. Pyretic Forge → Humilis, 350 → 150. Cauterizing Rebuke 275 → 150, 1 HP per ailment, then a 3 × CM Searing flare at hostiles within 4. Phoenix Debt 2000 → 1200. Sanguine Ignition sets targets alight for 2 s (not wet, rimed or fire-proof ones). |
| Lux | Lumen Suture Absorption II 20 s → Absorption I 10 s. White Verdict 3 + 9c (was 2 + 8c). Radiant Wake pulses deal 1 × CM to already-Illuminated enemies. |
| Mortem | Insatiable Hunger → Humilis, with a 2 × CM Mortem bite that seeds Necrosis. Bloom of Rot deals 3 × CM. Worked Lignum → Mediocritas. |
| Tenebris | Black Veil Covenant cooldown 220t → 900t. Eclipse Well bites Obscured bodies for 1.5 × CM per second; a full-charge well scatters non-boss hostiles within 2 blocks up to 8 blocks when it collapses. |
| Ductilis | Hemolymphal Pulse 400 → 175 mL, 300t → 200t, harmable bodies only (its Drudge action: `Monster`s only). Living Circuit 180 → 100 mL/s; each pulse arcs 2 × CM to the nearest visible hostile within 4 of each linked body. Thread Ripper also unravels a Vex or a foreign-owned summon at any health, never tamed animals or mounts; bosses are immune. An unravelled Drudge drops its equipped Blood Memory first. |

**Text (Task 7).** `en_us.json`, §8–§11 and the Drudge table of [the reference](../../HEMOMANCY_REFERENCE.md), and the affected `wiki/` pages were resynced with the code, and review spot-checked them. New lang keys: `death.attack.endless_hour_debt` (and `.player`), `hemomancy.manipulation.osseous_bloom` ("Crystalline Bloom"), `hemomancy.manipulation.gloam_laceration.desc` and `hemomancy.manipulation.thread_ripper.desc`. Player-facing manipulation names (Manipulations tab, radial, Reliquary, Scrying Podium, vascular view, memory tooltips, selection and level-up messages, Drudge labels) now go through `BloodManipulation.getDisplayName()`. That uses the `hemomancy.manipulation.<id>` lang entry when one exists and the ID-derived name otherwise, so Crystalline Bloom and the "Blood Avatar: …" form names display. Action-bar refusal messages remain untranslated literals.

### 11.2 Roster after pass 2

`ManipulationInit` registers 123 entries: 108 active and 15 retired (`ManipulationRetirementRules`). Counted the same way as §1 (primary tendency × rank; Saint memories and the `conjure_*`/utility conjurations included):

| Tendency | Total | Humilis (D0) | Mediocritas (D1) | Summa (D3) | Magister (D5) | Perfectus (D6) |
|---|---|---|---|---|---|---|
| Animus | 23 | 7 | 7 | 6 | 2 | 1 |
| Ductilis | 15 | 4 | 5 | 2 | 2 | 2 |
| Ferric | 15 | 5 | 4 | 3 | 2 | 1 |
| Mortem | 15 | 4 | 4 | 3 | 2 | 2 |
| Flammeus | 13 | 5 | 5 | 1 | 1 | 1 |
| Tenebris | 10 | 2 | 5 | 1 | 1 | 1 |
| Congeatio | 9 | 2 | 3 | 2 | 1 | 1 |
| Lux | 8 | 1 | 4 | 1 | 1 | 1 |

<details>
<summary>Full placement list</summary>

- **Animus** — H: `blood_shot`, `guided_blood_shot`, `blood_binding`, `lingering_blood_binding`, `chain_blood_binding`, `blood_rush`, `vital_effusion` · M: `hematic_mortar`, `sanguine_halo`, `blood_lattice`, `hematic_riposte`, `summon_avatar`, `summon_avatar_arms`, `conjure_blade` · S: `summon_avatar_armor`, `summon_avatar_legs`, `blood_cloud`, `expansive_blood_cloud`, `pursuing_blood_cloud`, `blood_aneurysm` · Ma: `summon_avatar_complete`, `sanguine_tempest` · P: `crimson_coronation`
- **Ductilis** — H: `deadly_gaze`, `synaptic_jolt`, `hemolymphal_pulse`, `sanguine_marionette` · M: `hematic_rebuke`, `activation_potential`, `conductive_mark`, `sanguine_ward`, `conjure_crossbow` · S: `hematic_impressment`, `thread_ripper` · Ma: `sovereign_instinct`, `living_circuit` · P: `synaptic_storm`, `axonal_transduction`
- **Ferric** — H: `blood_needle`, `blood_needle_fan`, `blood_needle_lance`, `sanguine_mending`, `vascular_dowsing` · M: `iron_retort`, `conjure_staff`, `blood_absorption`, `blood_projection` · S: `sanguine_magnetism`, `ferric_rampart`, `ferric_spikes` · Ma: `ironhearted`, `iron_choir` · P: `lodestone_collapse`
- **Mortem** — H: `hemorrhage`, `lignum_mortis`, `canopy_mortis`, `insatiable_hunger` · M: `worked_lignum`, `exsanguinate`, `grave_debt`, `conjure_axe` · S: `fruiting_contagion`, `crimson_tithe` (Saint), `bloom_of_rot` (Saint) · Ma: `blackhearted`, `carrion_communion` · P: `funeral_bell`, `conjure_sickle`
- **Flammeus** — H: `crimson_flame_conjuration`, `pyretic_forge`, `sanguine_ignition`, `scalding_updraft`, `soaring_updraft` · M: `cauterizing_rebuke`, `suspended_updraft`, `expulsive_updraft`, `searing_lash`, `conjure_torch` · S: `vitric_combustion` · Ma: `furnace_veins` · P: `phoenix_debt`
- **Tenebris** — H: `void_shroud`, `gloam_laceration` · M: `umbral_step`, `blood_eclipse`, `black_veil_covenant`, `umbral_reversal`, `conjure_claws` · S: `umbral_rend` · Ma: `penumbral_drift` · P: `eclipse_well`
- **Congeatio** — H: `glacial_grasp`, `cryogenic_pulse` · M: `glacial_rampart`, `cruor_javelin`, `conjure_flail` · S: `osseous_bloom` (Crystalline Bloom), `endless_hour` (Saint) · Ma: `rimebound_sentence` · P: `absolute_stillness`
- **Lux** — H: `hematic_flare` · M: `prismatic_reproof`, `hematic_beacon`, `lumen_suture`, `conjure_spear` · S: `unclosing_eye` (Saint) · Ma: `radiant_wake` · P: `white_verdict`

</details>

Animus drops from 25 to 23 and no longer dominates Humilis (7, down from 11). Ductilis rises to 15 and has at least two entries at every rank from Mediocritas up. Every school still has at least one entry at every rank.

### 11.3 Rulings

Decisions made during implementation that changed behaviour from the task plan:

- **Gloam ambush** is `isInvisible() || dark`. The explicit Veiled and channel terms were dropped: `isInvisible()` already covers an un-Illuminated Veil, and Illumination must override Veiled everywhere. A caster who is both Veiled and Illuminated loses the ambush the plan wording would have granted.
- **Charge clamp tolerance** is `elapsed + max(2, ceil(0.15 × elapsed))`, not a flat +2 ticks. A flat +2 silently stripped full-charge bonuses from honest casts under ordinary TPS loss. The cost is that a player can shave about 13% off long charge times.
- **Kill alignment** splits the base shift across a mob's blood-profile tendencies when no class rule matches; mobs with no profile give nothing. Profiled mobs such as the Ravager now feed their own tendency (Ferric) instead of Animus.
- **Thread Ripper** never discards tamed animals or horses, even when the owner is harmable. Only magical summons and constructs unravel. Permanently deleting a pet in PvP is griefing beyond the audit's intent; Ripper is weaker against pet-heavy PvP opponents as a result.
- **Thread Ripper and Drudges.** A foreign Drudge still unravels, but it drops its equipped Blood Memory first (through the Drudge's own drop path), so the cast removes the construct without destroying the item. Ripper can still remove another player's crafted servant.
- **Stale key-downs.** The charge clock ignores a key-down older than 2 × required + 20 ticks. A refused cast no longer leaves a start that a modified client could credit much later. Holding the key through a cooldown longer than that window now needs a fresh press.
- **Hemolymphal Pulse's Drudge action** is limited to `Monster`s too, for consistency with the player cast.
- **Endless Hour settlement** uses a dedicated damage type (`endless_hour_debt`) rather than vanilla magic. Lethe Silver Ward and Still Waters therefore no longer catch it, and the Sheolic Bastion stance cannot zero it.
- **Phoenix readiness** compares blood against the cost ledger's effective price, not the raw registered cost, so a trained caster is not refused a rebirth they can pay for.
- **The Eclipse Well pulse** is PERIODIC damage, not DIRECT, so an unattended well does not strip the caster's own Veil every second.
- **Task 5 was split** into 5A (Ferric, Flammeus, Congeatio) and 5B (Lux, Mortem, Tenebris, Ductilis). Task 6 was folded away.

### 11.4 Deferred

Minor findings logged during review and not fixed in this pass:

- **Edge-case mechanics.** Drift's same-tick stamp could survive a cancelled main swing and grant a sweep hit the ambush. Creative or invulnerable players do not pay Endless Hour debt, and other cancel-all damage handlers besides the Bastion could still reduce settlement. A Synaptic lockout can be skipped by relogging. Marionette migration resets mastery even when no Blood Binding entry exists. The Eclipse Well collapse needs the caster online in the same dimension. Lumen Suture's Drudge action still grants Absorption II for 15 s.
- **Build permission gaps.** Glacial Rampart charges an adventure-mode caster and then places nothing (`mayAlter` is not checked before payment). The Rimebound Sentence cage is not `mayAlter`-gated.
- **A second boss list** remains in `ConstrictorCordEntity.isBossTarget`, without the Warden, Elder Guardian or `c:bosses`.
- **Congeatio freeze damage** (Cryogenic Pulse, Osseous Bloom, Absolute Stillness) gets vanilla's ×5 freeze bonus against blazes, magma cubes and striders. This is school-wide and should be fixed together if wanted.
- **Persistence and resync.** `EntityIronPillar.pulled` is not saved, so a reload loses the Magnetism slam list. A cloud's first contact may clear Veiled twice after a chunk reload. The DEAD-section message uses the raw section enum name.
- **Content sign-off (pending owner).** The Tempest weaving recipe gained a Neurotic enzyme (+1) instead of a swap, because it had no Mortem enzyme to swap. It is left as implemented until the owner signs off.
- **Cleanup.** Refusal messages lost § styling inconsistently. `spikeDamage` is stored for WALL/PILLAR constructs. Fully qualified class names repeat (`SkillPointHelper`, `EclipseWellManip`). There are import-order nits. The scatter sound uses `SoundSource.PLAYERS`. The `BloodTendencyEvents` Javadoc about Snow Golems is wrong.
- **Test gaps and flakes.** Several GameTests use a fallback context or skip positive paths. The full-charge-only spells are not driven through the packet path. Iron Choir has no line-of-sight test. A shared Pass-2 test utility class would remove repeated helpers. The Morphling sinking fixture releases its force-load only on success. Live-config toggles (`ModConfigSpec.set`) inside GameTests disturb concurrent fixtures.

### 11.5 Corrections to pass-1 claims

- **Crimson Coronation and Crimson Mastery.** §3.1 and §10 treated Coronation's lance formula as already scaled by Crimson Mastery. It was not: the lance base was `1 + 2 × strength` with no mastery term until pass 2 (see Cast pipeline in §11.1).
- **Penumbral Drift ambush.** §10 listed the 1.5× ambush as done. It only reached projectile and spell hits: a melee swing ended the channel before its damage arrived, so the swing that most often breaks the drift got no bonus. Pass 2 delivers it for melee.
- **"Animus has no healing."** §5.7 said healing lives only in Lux and Mortem. Blood Rush already healed 0.5 HP per second through its effect tick, so "Life, Regen" was not wrong for Animus. It still heals after pass 2, now for 8 seconds.
- **Endless Hour settlement.** §10 described the overflow-only debt as fixed. Settlement still bypassed death protection (direct `setHealth`/`die`), so a totem could not answer it. Pass 2 settles it as a real hit.
- **Sanguine Ward.** The pass-1 "pool 6, +1 per pulse, cap 6" could be reset to a full six by tapping the channel. Pass 2 adds the 100-tick resume rule.

### 11.6 Verification

- Each code task (1, 2, 3a, 3b, 4, 5A, 5B) ended with `gradlew compileJava compileGameTestJava test runGameTestServer`. Each added no failures beyond the pre-pass baseline of 8 JVM and 10 GameTest failures. The Blood Binding and Sanguine Mending fixtures flaked in some runs and passed on rerun. New GameTests live in the `ManipulationBalancePass2*GameTests` classes.
- Text pass: `en_us.json` parses (5,537 keys). `gradlew test verifyPackagedClientResources` reports 2,692 tests with the same 8 baseline failures, including the existing acolyte-line `EnglishLocalizationEncodingTest` mojibake. `verifyPackagedClientResources` rejects only the existing `textures/item/old/sanguine formation/` path.
- Final fix wave: Thread Ripper returns a Drudge's memory, Phoenix Debt readiness respects a DEAD Heart, stale charge key-downs expire, the charge clock joins the session-state reset, Drudge memory names are red again, sub-half charges skip use milestones, and Exsanguinate's boss cap holds after affinity. The Blood Binding and Sanguine Mending "flakes" were a Blood Moon: a long suite crossed day time 12542, the 1-in-7 roll started one and its ×0.75 cost broke their exact-cost assertions. A gameTest-only `GameTestWorldClock` now stops the daylight cycle at noon and clears any Blood Moon when the GameTest server starts, and both tests read the live ledger price. Three consecutive GameTest runs and the JVM suite showed only the baseline failures (10 GameTest, 8 JVM).
- Not yet verified in a client: how the re-ranked and retuned spells feel in play, and every number in §11.1.

