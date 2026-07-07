# The Blood Manipulation System — Complete Examination

> **Date:** 2026-07-02
> **Type:** Current-state examination / reference companion to [POWER_SYSTEMS_AUDIT.md](POWER_SYSTEMS_AUDIT.md).
> **Scope:** What a manipulation is, how it is learned, how it is cast, the full 60-entry catalog by tendency, the modifier economy, second lives in other systems, and an assessment with code-verified gaps.
> **Sources of truth:** [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §8 (Manipulations), §9 (Tendency), §11 (Skill Tree), §25.7 (Somatic Loom recipes); `ManipulationInit`, `EnumManipulationRank`, `EnumManipulationType`, and the `data/hemomancy/recipe/memory_weaving/` JSONs. Where code and prose disagree, code wins; the code-verified figures in §1 and §10 were checked against the current NeoForge 1.21.1 branch on 2026-07-02.

Unlike the redesign specs under `docs/superpowers/specs/`, this doc **examines what exists** rather than proposing changes. It is the manipulation-level companion to the cross-system [audit](POWER_SYSTEMS_AUDIT.md), which places manipulations as the *expenditure* phase of the mod's blood-circulation model.

---

## 1. What a manipulation is

A manipulation is a "remembered instruction" carried in blood. Each registered entry (`BloodManipulation`) has eight defining properties plus two runtime axes:

- **Blood cost** — mL drained from `IBloodVolume` on cast.
- **XP cost** — additional experience spent.
- **Alignment level** — required tendency alignment to use effectively.
- **Type** — `QUICK`, `CHARGED`, `PASSIVE`, or `CONTINUOUS`.
- **Rank** — `HUMILIS`, `MEDIOCRITAS`, `SUMMA`, `MAGISTER`, `PERFECTUS`.
- **Primary tendency** (+ optional **secondary**) — drives unlock, tree placement, color, and damage composition.
- **Vein section** — which of 7 body sections takes strain when cast.
- **Cooldown** — tick-based.
- **ManipLevel** (runtime) — manipulations level up with use.

**Code-verified figures (2026-07-02):** `ManipulationInit` registers **60** manipulations. Rank population: **Humilis and Mediocritas** carry the bulk, **Summa** is the current ceiling at **10** entries (including the four Saint Canon memories), and **Magister / Perfectus are defined in the enum but hold zero manipulations.** Type population: `QUICK` dominates (~53), then `CONTINUOUS` (5) and `PASSIVE` (3); **`CHARGED` is plumbed** (the continuous-cast packet and the Manipulations tab controller reference it) **but no registered manipulation uses it.**

### 1.1 Two structural rules that give the system texture

- **Vein strain is anatomical.** Head-casts tax the head section, leg-casts tax legs, and `vital_reservoir` / `crimson_tithe` tax the **heart**. Sections degrade healthy → stressed → clotted → dead and debuff the player until healed (well-fed regen or the Vascular Mending rite). A mono-build literally wears out one body part.
- **Mixed-tendency composition.** Casting grants the primary tendency the full use-gain and the secondary half. Damage blends **75% primary + 25% secondary** affinity, so a specialist still gets partial value from a manipulation's off-tendency undertone. This 75/25 rule is the balance counterweight referenced by the triad-Resonance proposal in the [audit](POWER_SYSTEMS_AUDIT.md) and the [morphling spec](superpowers/specs/2026-07-02-morphling-fungal-strain-reframe-design.md).

### 1.2 Rank degree gates

Centralized in `ManipulationRankGates`, shared by full memories, crude shards, and the progress UI:

| Rank | Degree required | Population |
|---|---|---|
| `HUMILIS` | 0 | large — the workhorse tier |
| `MEDIOCRITAS` | 1 | large — the identity tier |
| `SUMMA` | 3 | 10 (incl. 4 Saint Canons) — current ceiling |
| `MAGISTER` | 5 | **empty (runway)** |
| `PERFECTUS` | 6 | **empty (runway)** |

Degrees 5–6 currently gate *systems* (synaptic loadouts, covenants) rather than new spell tiers, which is why the spell ladder plateaus at Summa. See §10.

---

## 2. How manipulations are learned — five lanes

1. **Initiation grant.** `blood_absorption` and `blood_projection` are granted free at Degree 1 and cannot be unequipped — they are mechanical verbs, not spells. Existing saves are backfilled on login.
2. **Crude Memory Shards** (10 starters, one per lane): scraped echoes from outpost loot — `blood_shot`, `blood_rush`, `deadly_gaze`, `crimson_harvest`, `sanguine_mending`, `blood_lamp`, `hemorrhage`, `glacial_grasp`, `sanguine_ignition`, `void_shroud`. Teach + auto-equip, no station needed.
3. **Somatic Loom weaving** (Degree 3, the main lane): blank Hematic Memory + exact catalyst list + stored enzyme units + projected blood + the physical orb-dragging rite. Full schema in [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §25.7.
4. **Scar-catalyst alternates.** Five memories accept a cerebral **scar** as their catalyst, often *cheaper* than the standard route (e.g. `blood_rush` via `scar_heart` needs 2 enzyme orbs + 100 blood vs. the standard 7 orbs + 350 blood). Scar investment pays back in the memory economy.
5. **Saint Canon Memories.** Hallowed Residuum as catalyst yields the four SUMMA capstones — "imprinted rather than learned" (Dynamic Use discount does not apply).

**Special cases:** the 7 base Living Staff weapon forms are normal `conjure_*` manipulations, but their current acquisition route is a Living Weapon Graft: form-aligned behavior unlocks the graft recipe, the player crafts the componentized `living_weapon_graft`, then completes the Iron Brazier + Living Staff Blood Absorption rite to teach the memory. Legacy `memory_living_*` items still teach the same forms for old saves, but their normal survival recipes are removed. `conjure_staff` unlocks via the staff *bond* (first Living Staff craft); a few entries (`vital_effusion`, `hemolymphal_pulse`, `vascular_dowsing`, `ferric_resonance`) sit outside the weaving catalog.

### 2.1 Catalyst symbolism

The ~55 weaving recipes read as literate rather than arbitrary: ender eye → Umbral Step, ink sac → Void Shroud, wither rose → Hemorrhage, fermented spider eye → Blood Eclipse, echo shard → Black Veil Covenant, glistering melon → Lumen Suture, scrying dish → Deadly Gaze, gourd seeds → Crimson Harvest — and the same Vitality Chalice catalyzes both Vital Reservoir and Exsanguinate (the vessel of life read in both directions). Orb-count is the effort curve: starters ask 1 enzyme, dual-tendency spells 2, living weapons 2–4, and **Summon Avatar demands a Nether Star plus all eight enzymes at 400 blood** — the grand weave.

---

## 3. How manipulations are cast

- **Two-layer radial** (`RadialChooseManipScreen`): an unselectable center readout; an **inner mechanical band** permanently split between Blood Absorption (top) and Blood Projection (bottom); an **outer band** of equipped memorized manipulations; and — with a full final Bloodlust armor set — a **third armor-ability wedge**.
- **Keybinds** (Hemomancy category): Use Manipulation, Use Quick Manipulation, Use Continuous Manipulation, Cycle Known Manipulations, plus construct/formation/draw keys.
- **Slots & loadouts:** equipped capacity starts small and grows via the **Manip Slots** skill (+1/level ×5). The **Mnemonic Reliquary** (D2) manages loadouts; the Degree-5 **Dendritic Distributor** saves named *synaptic patterns* (3 base → 7 with the Synaptic Memory skill) for 100 blood + 25 XP per save.

---

## 4. The complete catalog by tendency

All 60 registered manipulations. Cost is mL; `hot-swap` marks Living Staff weapon forms (250 mL default, reduced by the Weapons Master skill). ⛧ marks Saint Canon (SUMMA).

### Animus (Life) — red · Vivacious Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `blood_shot` | 100 | Humilis | Head · 10t | Tracking blood projectile — the workhorse attack |
| `blood_needle` | 100 | Humilis | Head · 10t | 10–20 needle scatter-volley (the shotgun to Blood Shot's rifle) |
| `deadly_gaze` | 100 | Humilis | Head · 20t | 100-block raycast launches target skyward (pick-off / fall damage) |
| `blood_rush` | 100 | Humilis | Body · 60t | +20% move / +10% attack speed; summons a Wretched Will |
| `vital_effusion` | 350 | Humilis | Body · 20t | Bonemeal-accelerates a targeted growable area |
| `crimson_flame_conjuration` | 150 | Humilis | R.Arm · 15t | Places Crimson Flames on surfaces (range scales with Sanguine Reach) |
| `blood_cloud` | 300 | Summa | Head · 40t | Carrier projectile deploys an AoE blood cloud — zone control |
| `blood_aneurysm` | 400 | Summa | Body · 40t | Nearest-enemy nuke: 8 dmg + launch + 4-block splash (scales Crimson Mastery) |
| `summon_thrall` | 500 | Medioc. | Body · 60t | Two-raycast courier: mark source, mark destination, hauls autonomously |
| `summon_avatar` | 500 | Summa | Body · 100t | Toggles the Blood Avatar transformation (synced to all players) |
| `conjure_blade` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Blade (Animus weapon form) |

### Flammeus (Fire) — orange · Fervent Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `sanguine_ignition` | 125 | Humilis | Body · 25t | 5-block fire pulse — early crowd peel |
| `scalding_updraft` | 225 | Humilis | L.Leg · 80t | Superheated-air self-launch + slow-fall + scorch — the fire school's mobility |
| `pyretic_forge` | 350 | Medioc. | Body · 30t | Smelts held items in-hand (base 8, scales Crimson Mastery) — a furnace in your veins |
| `cauterizing_rebuke` | 275 | Medioc. | Body · 90t | Self-cleanse Poison/Wither, then burn nearby enemies |
| `vitric_combustion` | 500 | Summa | Body · 60t | 22-block targeted blood explosion (8 hearts + fire + knockback) |
| `conjure_torch` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Torch; ignites on hit |

### Ductilis (Lightning / Nerves) — yellow · Neurotic Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `activation_potential` | 200 | Medioc. | Body · 30t | AoE lightning to all within 5 blocks (named for the neuron term) |
| `sanguine_ward` | 10 | Medioc. | Body · 20t | Continuous damage-reduction shield — cheapest cast, paid in upkeep |
| `hemolymphal_pulse` | 400 | Humilis | Head · 20t | Blood-sense sonar: Glowing on nearby life for 15s |
| `crimson_harvest` | 200 | Humilis | L.Leg · 60t | 5×5 bonemeal pulse around the caster |
| `conjure_crossbow` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Crossbow (ranged form) |

### Lux (Light) — white · Incandescent Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `blood_lamp` | 75 | Humilis | L.Arm · 10t | Places an invisible light-15 block — torchless caving |
| `hemosynthesis` | 200 | Humilis | Body · 40t | Blood → food (4 hunger, 4 saturation): eat yourself |
| `hematic_flare` | 125 | Humilis | Head · 30t | Short ray: 3 magic damage, Glowing, strips Invisibility, +2 vs concealed targets |
| `crimson_sight` | 250 | Medioc. | Head · 60t | Night Vision 60s + Glowing on mobs in 32 blocks |
| `prismatic_reproof` | 325 | Medioc. | Head · 80t | Refracted cone: blind + weaken; 2 magic damage, or 4 against marked/glowing targets |
| `hematic_beacon` | 350 | Medioc. | Body · 160t | Rally point: Regen/Resistance to players, Glowing to mobs (8-block) |
| `lumen_suture` | 250 | Medioc. | R.Arm · 120t | Heal nearest wounded player + Absorption II, clear Blindness/Wither |
| `unclosing_eye` ⛧ | 350 | Summa | Head · 120t | **Seraphae Canon:** Glowing on ALL life in 32 blocks incl. self; strips Invisibility |
| `conjure_spear` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Spear |

### Mortem (Death) — dark green · Ruinous Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `hemorrhage` | 100 | Humilis | R.Arm · 20t | Wither II on the closest enemy — the DoT opener |
| `vital_reservoir` | 50 | Medioc. | Heart · 60t | 10 XP levels → 1000 blood (the emergency exchange) |
| `exsanguinate` | 300 | Medioc. | R.Arm · 50t | Execute ≤30% HP: 1.5× current-HP damage, restores 600 blood — killing refuels |
| `crimson_tithe` ⛧ | 400 | Summa | Heart · 100t | **Hemorath Canon:** bank 500 blood as debt — repay in 30s or pay double + take 6 |
| `bloom_of_rot` ⛧ | 500 | Summa | Body · 80t | **Putriciel Canon:** 8-block Wither II + Poison + Slow III; poisons the caster too |
| `conjure_axe` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Axe |

### Congeatio (Ice) — blue · Frigid Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `glacial_grasp` | 125 | Humilis | L.Arm · 20t | Freezes 7×7 water — on-demand Frost Walker |
| `cryogenic_pulse` | 150 | Humilis | Body · 30t | AoE chill: damage + Slow III + Mining Fatigue |
| `glacial_circulation` | 175 | Humilis | Body · 100t | 90s Fire Resistance at the cost of Slowness — chilled blood, honest tradeoff |
| `glacial_bastion` | 350 | Medioc. | L.Arm · 50t | Instant iceberg shell around you (your space stays open) — the panic room |
| `glacial_rampart` | 350 | Medioc. | L.Arm · 50t | Projected 3×3 ice wall at range — battlefield architecture |
| `osseous_bloom` | 600 | Summa | Body · 60t | 6-block crystallization: 25% current-HP freeze damage + Slow IV — an opener |
| `endless_hour` ⛧ | 600 | Summa | Body · 200t | **Velorum Canon:** absorb all damage 10s, then repay the full accumulated total |
| `conjure_flail` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Flail; slows, physics chain |

### Ferric (Iron) — gray · Ferric Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `sanguine_mending` | 150 | Humilis | R.Arm · 30t | +50 durability to held item — blood as repair kit |
| `vascular_dowsing` | 500 | Humilis | R.Arm · 20t | Ore-sense: reveals nearby ores with colored particles |
| `sanguine_excavation` | 400 | Medioc. | R.Arm · 40t | Flood-fill vein-mines matching blocks (base 9, scales Reach) |
| `ferric_resonance` | 600 | Medioc. | R.Arm · 200t | 30s Haste II + Strength + Resistance — the miner's war-chant |
| `ferric_transmutation` | 1000 | Summa | Body · 20t | "Sanguine Alloy": 90s Strength II + accelerated blood regen |
| `venous_travel` | 1000 | Medioc. | R.Arm · 20t | Continuous: teleport across saved Earthen Vein nodes (fast-travel network) |
| `conjure_staff` | 1000 | Medioc. | R.Arm · 40t | Recovers/conjures the bonded Living Staff (purges duplicates) |
| `blood_absorption` | 1000 | Medioc. | R.Arm · 40t | Mechanical verb: draw blood in |
| `blood_projection` | 1000 | Medioc. | R.Arm · 40t | Mechanical verb: spend blood out (crafting, braziers, loom, reservoirs) |

### Tenebris (Shadow) — purple · Umbral Enzyme
| Manipulation | Cost | Rank | Vein · CD | Use |
|---|---|---|---|---|
| `void_shroud` | 100 | Humilis | Body · 20t | 5s Invisibility + Speed II + Night Vision — the dash-stealth opener |
| `gloam_laceration` | 175 | Humilis | R.Arm · 35t | Short ambush slash: 3.5 magic damage, Blood Loss + Weakness, +2.5 from Invisibility/darkness, rendered as a three-line claw ribbon |
| `umbral_step` | 300 | Medioc. | L.Leg · 40t | Teleport to target block (range 24) — **destination must be dark (light ≤ 7)** |
| `blood_eclipse` | 300 | Medioc. | Head · 45t | 18-block cone: Blindness II + Weakness + 1.5 hearts shadow damage |
| `blood_eclipse_mantle` | 325 | Medioc. | Body · 180t | Defensive stance: Resistance II + Fire Resistance, Weakness the price |
| `umbral_reversal` | 375 | Medioc. | L.Leg · 100t | Slip backward into the nearest dark space, blinding pursuers at the origin |
| `black_veil_covenant` | 425 | Medioc. | Body · 220t | Raises a black-veined sphere that **counts as synthetic darkness** for umbral checks |
| `conjure_claws` | 250 hot-swap | Medioc. | R.Arm | Staff → Living Baghnakh claws |

**Tally:** Animus 11 · Flammeus 6 · Ductilis 5 · Lux 9 · Mortem 6 · Congeatio 8 · Ferric 9 · Tenebris 8 = **62**.

Lux and Tenebris both now have Humilis true-offense verbs before their broader Mediocritas control tools: `hematic_flare` gives Lux a mark/reveal hit that can answer concealed enemies, while `gloam_laceration` gives Tenebris a short ambush slash that rewards its darkness and invisibility setup. Tenebris remains the most *systemic* school: Umbral Step is gated on darkness → Black Veil Covenant manufactures darkness → Umbral Reversal is the defensive inverse → Void Shroud is the approach — and the Phantasmal Bloodlust armor set later removes Umbral Step's darkness gate as its endgame payoff. A verb economy with counterplay and a gear capstone.

---

## 5. The modifier economy

Effective cost / cooldown / power runs through an unusually deep, fully diegetic stack:

**Subsidies (reduce cost/CD or boost effect):** Efficiency (−8%/lvl, multiplicative), Dynamic Use (discount when cast matches dominant tendency), Blood Flow (−5% CD/lvl), Crimson Mastery (+15% damage/lvl), Sanguine Reach (+15% range on the ranged set), Sporitic Resonance (lit matching-spore thurible: −15% cost, −10% CD), Mnemonic Whispers potion (−25% CD), Qliphoth Pome empowerment (post-Communion cost reduction), Founding Fane ground buffs, Blood Moon Harbinger empowerment.

**Taxes (increase cost/CD):** purity progression on the Unstained path (+10% → +25% → +50% → blocked), Blood Drunkenness (+15–60% cost, +25% CD at amp 3), Mnemonic Screams (+50% cost, the anti-abuse backlash), and vein-section degradation debuffs.

Casting is a market with subsidies and taxes — every modifier is a piece of fiction, not a stat line.

Implementation note: selected-manipulation blood cost now resolves through `ManipulationCostLedger` and syncs a `ManipulationCostSnapshot` to the Scrying Podium, so active discounts, surcharges, and blocking conditions can be inspected as exact source rows.

---

## 6. Second lives — manipulations reused as content

Manipulations are deliberately re-consumed by other systems rather than living only in the radial:

- **Drudge programs:** ~40 manipulations carry a bespoke `DrudgeAction` — installing the memory item into a Drudge makes it execute that manipulation autonomously (e.g. `blood_lamp` lights a base, `sanguine_mending` repairs allies' armor, `umbral_step` teleports the drudge). Some manipulations are intentionally unsupported (conjurations, `summon_avatar`, `crimson_tithe`).
- **Living Staff arsenal:** the 7 `conjure_*` forms are the staff's hot-swappable weapon platform, each with its own tendency and the 75/25 mixed-damage rule.
- **Canon-as-program:** Saint Canons double as elite Drudge behaviors (a drudge with `endless_hour` self-buffs; one with `unclosing_eye` becomes a permanent watchtower).

---

## 7. Assessment

**Strengths.** Of 60 entries, roughly a third are non-combat utility (light, food, repair, smelting, farming, mining, travel, sensing), which sells hemomancy as a way of *living in the world*, not just a combat kit. The best designs encode a cost-philosophy per school: Mortem trades in debt and executes-as-refuel; Congeatio trades power for immobility; Lux pays in exposure (Unclosing Eye reveals the caster); Tenebris pays in positioning constraints. Acquisition redundancy across five lanes means no build is bricked by one missing catalyst. And the reuse of manipulations as Drudge programs and staff forms multiplies the value of every authored spell.

**Honest, code-verified gaps.**
1. **The spell ladder plateaus at Summa.** `MAGISTER` and `PERFECTUS` are defined but hold zero manipulations; degrees 5–6 gate *systems* (synaptic loadouts, covenants) rather than spell tiers. This is deliberate runway, aligned with [POWER_SYSTEMS_AUDIT.md](POWER_SYSTEMS_AUDIT.md) §3.3.
2. **`CHARGED` is plumbed but unpopulated.** The continuous-cast packet and Manipulations tab controller handle the type, yet no registered manipulation is `CHARGED`. (The audit's §3.3 phrasing "registered but unused" is slightly imprecise — precisely, it is *plumbed but unused by any manipulation*.) A natural fit is the empty Magister rank: held, blood-channeled greater forms of known Summa spells, unlocked by a forced rank-up rite.
3. **Cost tuning has flat spots.** Utility is priced by *value* (`vascular_dowsing` 500 at Humilis) while combat is priced by *rank* (`blood_aneurysm` 400 at Summa). Workable, but not legible to players without tooltip explanation.
4. **`ManipLevel` has thin payoff.** Manipulations track a use-level, but its gameplay effect is minimal today — an obvious lever for use-based evolution that would rhyme perfectly with the memory/organism theme (a memory *deepening* as it is lived).

None of these are defects so much as visible unfinished runway, consistent with the mod's public-alpha posture.

---

## 8. Related docs

- Cross-system framing and the circulation model: [POWER_SYSTEMS_AUDIT.md](POWER_SYSTEMS_AUDIT.md)
- Canonical mechanics reference: [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §8–§11, §25.7
- Tendency/enzyme detail: [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §9
- Systems that consume manipulations downstream: [Morphling reframe](superpowers/specs/2026-07-02-morphling-fungal-strain-reframe-design.md) (triad Resonance), [Rogue Hemomancer Wills](superpowers/specs/2026-07-02-rogue-hemomancer-wills-design.md) (Wills cast school-specific manipulations)
