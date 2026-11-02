# Vascular Load and Damage Catalog

Implementation snapshot: 2026-08-25, NeoForge 1.21.1 checkout.

This catalogs every code path that directly lowers the player's five-section vascular-health meter, then expands the Noetic and Thelemic registries so section pressure can be compared. Values below are **vascular health lost on a 0-100 scale**, not ordinary player HP or blood volume.

## Executive balance view

| Section | Active Noetic routes | Fixed mechanical routes | Thelemic memories | Ordinary incoming-damage exposure | Main pressure |
|---|---:|---:|---:|---|---|
| `HEAD` | 12 | 0 | 1 | 100% drown/in-wall; 20% fallback; 20% explosions | Moderate |
| `ARMS` | 20 | 3 | 2 | 40% fallback; 20% explosions | Very high |
| `HEART` | 2 | 0 | 1 | 20% explosions only | Very low outside special effects |
| `BODY` | 24 | 0 | 2 | 100% arrows/tridents/fire/lava; 40% fallback; 20% explosions | Highest |
| `LEGS` | 3 | 0 | 2 | 100% falls; 20% explosions | Low registry pressure, activity-dependent travel pressure |

- `BODY` and `ARMS` own **44 of 61 active Noetic routes (72.1%)**.
- Including the three fixed mechanical routes, `BODY` and `ARMS` own **47 of 64 castable manipulation routes (73.4%)**.
- `HEART` and `LEGS` together own only **5 of 61 active Noetic routes (8.2%)**.
- The default fallback for every unrecognized incoming damage type is another **40% BODY / 40% ARMS / 20% HEAD**. It never selects HEART or LEGS.
- Thelemic assignment is much more even by count, but trigger cadence is not even. Sanguine Fists, Laboring Arms, Coursing Legs, and Hushed Gait can fire repeatedly during ordinary activity; Second Pulse is the largest single Thelemic hit but has a 30-second cooldown.

## Exhaustive direct mutation paths

These are the only production paths found that directly subtract from the vascular-health map.

| Source family | Section | Base amount | Clamped to 0? | Fortification? | Config gate? |
|---|---|---:|---|---|---|
| Successful Noetic manipulation cast | Manipulation's registered section | `vascularManipStrain`, default **1.0** | Yes | Yes, `x0.85` | `vascularDegradationOnManipEnabled` |
| Successful fixed mechanical manipulation cast | Registered section; all three use ARMS | Default **1.0** | Yes | Yes, `x0.85`; no cerebral-scar reduction | Same manipulation gate |
| Successful Thelemic useful-action trigger | Memory's registered section | **0.10-0.75** before resonance | No | Yes, `x0.85` | Same manipulation gate |
| Incoming health damage | Damage-category-selected section | `event new damage x vascularDamagePerHit`, default **x0.5** | Yes | Yes, `x0.85` | `vascularDegradationOnDamageEnabled` |
| Interactive Cardinal Rite collapse: explicit section hit | Uniform random section per hit | **10** once or **15** twice, by failure profile | No | No | No |
| Lethean Judgment | Every section | **30 each / 150 total** | Yes | No | No |

The direct writers are `VascularSystemEvents`, `MuscleMemoryActivationService`, `HarbingerCardinalRiteEvents`, and `UnstainedCardinalRiteEvents`. Healing, sync, diagnosis, and rendering paths do not add another damage authority.

## Noetic Memories and ordinary manipulations

### Amount formula

Every successful ordinary manipulation use applies the same configured strain regardless of rank, blood cost, cooldown, or effect strength:

```text
Noetic strain = vascularManipStrain
               x cerebral-scar multiplier
               x Hematic Fortification multiplier
```

Default `vascularManipStrain` is `1.0` and can be configured from `0.01` to `50.0`.

| Matching active cerebral scar | Scar multiplier | Default strain | With Hematic Fortification |
|---|---:|---:|---:|
| None | `1.00` | **1.0000** | **0.8500** |
| Tier 1 | `0.95` | **0.9500** | **0.8075** |
| Tier 2 | `0.90` | **0.9000** | **0.7650** |
| Tier 3 | `0.85` | **0.8500** | **0.7225** |

The scar must be an active `CEREBRAL` scar assigned to the manipulation's primary tendency. Only the best matching tier applies. Secondary tendency does not qualify.

### Active Noetic catalog by section

All entries below cost the same default **1.0 vascular health per successful cast**, modified by the table above.

#### BODY — 24 routes

`activation_potential`, `black_veil_covenant`, `blood_aneurysm`, `blood_eclipse_mantle`, `blood_rush`, `bloom_of_rot`, `cauterizing_rebuke`, `cryogenic_pulse`, `endless_hour`, `ferric_transmutation`, `glacial_circulation`, `hematic_beacon`, `insatiable_hunger`, `iron_retort`, `osseous_bloom`, `pyretic_forge`, `sanguine_ignition`, `sanguine_magnetism`, `sanguine_ward`, `summon_avatar`, `summon_thrall`, `vital_effusion`, `vitric_combustion`, `void_shroud`

#### ARMS — 20 routes

`conjure_axe`, `conjure_blade`, `conjure_claws`, `conjure_crossbow`, `conjure_flail`, `conjure_spear`, `conjure_staff`, `conjure_torch`, `crimson_flame_conjuration`, `exsanguinate`, `ferric_resonance`, `glacial_bastion`, `glacial_grasp`, `glacial_rampart`, `gloam_laceration`, `hemorrhage`, `lumen_suture`, `sanguine_mending`, `vascular_dowsing`, `venous_travel`

#### HEAD — 12 routes

`blood_cloud`, `blood_eclipse`, `blood_needle`, `blood_shot`, `conductive_mark`, `crimson_sight`, `deadly_gaze`, `hematic_flare`, `hemolymphal_pulse`, `prismatic_reproof`, `synaptic_jolt`, `unclosing_eye`

#### LEGS — 3 routes

`scalding_updraft`, `umbral_reversal`, `umbral_step`

#### HEART — 2 routes

`crimson_tithe`, `grave_debt`

### Fixed mechanical routes

These still apply ordinary manipulation strain, but `ScarNoeticRoutingRules` deliberately excludes them from cerebral-scar reduction:

| Route | Section | Default | Fortified |
|---|---|---:|---:|
| `blood_absorption` | ARMS | **1.00** | **0.85** |
| `blood_projection` | ARMS | **1.00** | **0.85** |
| `conjure_sickle` | ARMS | **1.00** | **0.85** |

### Registered but retired routes

These remain registered for compatibility but `ManipulationRetirementRules` blocks casting, removes them from active loadouts, and therefore prevents new vascular strain:

| Route | Historical section |
|---|---|
| `blood_lamp` | ARMS |
| `sanguine_excavation` | ARMS |
| `hemosynthesis` | BODY |
| `vital_reservoir` | HEART |
| `crimson_harvest` | LEGS |

### Noetic behavior notes that affect balance

- Strain is paid after blood payment and `getAction`. It is not scaled by rank, blood cost, cooldown, or whether the action found a useful target.
- Zero-blood weapon-form casts still apply the full vascular strain when accepted.
- Crawling Choir echo-casts call `getAction` directly and do **not** add a second vascular-strain payment.
- Drudge and other entity-cast paths do not strain the owning player's vascular meter.
- Noetic strain clamps the section at `0`. A dead Noetic section does not itself block later Noetic casting.

## Thelemic / Muscle Memories

Thelemic strain is hard-coded per useful trigger. The numeric `vascularManipStrain` config does not scale it; only the shared manipulation-degradation boolean enables or disables it.

### Exact strain table

`Secondary` is the `x0.8` cross-tendency Morphling resonance. `Preferred` is the `x2/3` primary-tendency resonance. Armed overexertion multiplies the already-resonance-adjusted strain by `2.5`. Hematic Fortification then multiplies the final number by `0.85`.

| Section | Memory | Useful trigger | Cooldown / cadence | Normal: none / secondary / preferred | Overexerted: none / secondary / preferred |
|---|---|---|---|---:|---:|
| LEGS | Hushed Gait | Move without sprinting at brightness 4 or lower | No cooldown; re-arms after 2 s normal or 6 s empowered active window | **0.1000 / 0.0800 / 0.0667** | **0.2500 / 0.2000 / 0.1667** |
| ARMS | Laboring Arms | Break hardness 1+ block with the correct tool, non-creative | 10 ticks | **0.1500 / 0.1200 / 0.1000** | **0.3750 / 0.3000 / 0.2500** |
| LEGS | Coursing Legs | Sprint 12 horizontal blocks while grounded | Distance-based; no time cooldown | **0.1500 / 0.1200 / 0.1000** | **0.3750 / 0.3000 / 0.2500** |
| HEAD | Predatory Eyes | Moving hostile detected in darkness | Checked each second; 100-tick cooldown | **0.1500 / 0.1200 / 0.1000** | **0.3750 / 0.3000 / 0.2500** |
| BODY | Enduring Viscera | Lose one food point with no saturation | 100-tick cooldown | **0.2000 / 0.1600 / 0.1333** | **0.5000 / 0.4000 / 0.3333** |
| BODY | Carrion Metabolism | Finish tagged carrion food | No cooldown; once per food completion | **0.2000 / 0.1600 / 0.1333** | **0.5000 / 0.4000 / 0.3333** |
| ARMS | Sanguine Fists | Valid direct primary melee hit | No cooldown; four-tick duplicate guard per target | **0.2500 / 0.2000 / 0.1667** | **0.6250 / 0.5000 / 0.4167** |
| HEART | Second Pulse | Incoming hit crosses the player below 30% maximum health | 600 ticks / 30 s | **0.7500 / 0.6000 / 0.5000** | **1.8750 / 1.5000 / 1.2500** |

Multiply any displayed number by `0.85` after resonance and overexertion when Hematic Fortification is present. Examples: ordinary unresonated Second Pulse becomes `0.6375`; unresonated overexerted Second Pulse becomes `1.59375`.

### Thelemic behavior notes that affect balance

- Strain and blood are paid only on a useful trigger, not while merely learned, equipped, selected, reserved, or active.
- One enabled Thelemic Memory may occupy each vascular section, so the two ARMS, two LEGS, and two BODY memories compete within their section.
- A `DEAD` section blocks that section's Thelemic trigger.
- Thelemic subtraction does not clamp at `0`, so a trigger can leave negative vascular health. That differs from Noetic and incoming-damage handling.
- Matching cerebral scars do not reduce Thelemic strain. Morphling resonance does.

## Incoming player damage

When the blood-volume system is active, every accepted player health-damage event can also damage one vascular section:

```text
vascular loss = event.getNewDamage()
              x vascularDamagePerHit (default 0.5)
              x 0.85 if Hematic Fortification is present
```

The source category only chooses the section; it does not change the multiplier.

| Incoming damage category | Section selection | Default vascular loss |
|---|---|---:|
| Fall | 100% LEGS | `health damage x 0.5` |
| Arrow or trident | 100% BODY | `health damage x 0.5` |
| Explosion or player explosion | Uniform random: 20% each section | `health damage x 0.5` |
| Drowning or in-wall suffocation | 100% HEAD | `health damage x 0.5` |
| On-fire, in-fire, or lava | 100% BODY | `health damage x 0.5` |
| Everything else, including melee, magic, most mod attacks, poison-like damage, and unlisted vanilla types | 40% BODY / 40% ARMS / 20% HEAD | `health damage x 0.5` |

The default multiplier is configurable from `0.01` to `50.0`. The path clamps the chosen section to `0`.

### Hemolysis as a repeated indirect source

Hemolysis does not write the vascular map directly. For a blood-active Hemomancer who has not begun purification, every scheduled effect tick drains blood and calls magic health damage. Each health hit that reaches `LivingDamageEvent.Pre` therefore routes through the generic **40% BODY / 40% ARMS / 20% HEAD** rule.

| Hemolysis amplifier | Blood drained per scheduled tick | Magic health damage attempted per scheduled tick | Default vascular loss per accepted health hit |
|---:|---:|---:|---:|
| 0 | 5 mL | 0.5 HP | **0.25** |
| 1 | 10 mL | 1.0 HP | **0.50** |
| 2 | 15 mL | 1.5 HP | **0.75** |
| 3 | 20 mL | 2.0 HP | **1.00** |

The effect schedules every game tick, but Minecraft hurt/invulnerability handling can reject or reduce repeated health hits; the table is per accepted `LivingDamageEvent.Pre`, not a promise of 20 vascular hits per second. Coagulation can block individual Hemolysis ticks completely.

Current mod applications include:

- Lethean Judgment: amplifier 2 for 600 ticks.
- Hemolytic Vial against a Harbinger: immediate 10 magic HP, then amplifier 1 for 300 ticks. The immediate hit alone produces default **5.0 vascular loss** on a fallback section before mitigation/fortification.
- Hemolytic Vial against other blood-active living entities: 8 magic HP, then amplifier 0 for 200 ticks. Only player targets have a vascular attachment.
- Absolution Dagger: amplifier 0 for 120 ticks on every valid initiated Unstained hit.
- Pale-Humor-coated Silthmere Glaive and Unstained Warhammer: amplifier 0 for 120 ticks when a coating hit is consumed.
- Potion of Hemolysis: amplifier 3 for 1,000 ticks.
- MnA Hemolysis spell component: data-adjustable duration and magnitude.

## Cardinal Rite damage

### Interactive Harbinger rite collapse

An instability collapse has two separate vascular consequences:

1. Magic player HP damage, which can feed the ordinary incoming-damage rule and chooses from BODY/ARMS/HEAD at 40/40/20.
2. Hard-coded direct vascular hits on uniformly random sections. Repeated hits may select the same section.

| Failure profile | Rite-collapse magic HP | Indirect vascular at default x0.5 | Explicit direct vascular | Total vascular before random placement |
|---|---:|---:|---:|---:|
| `safe_retry` | 0 | 0 | 0 | **0** |
| `offering_loss` | 2 | up to **1** on one fallback section | 0 | **1** |
| `fragile_damage` | 4 | up to **2** on one fallback section | **10** on one uniform section | **12** |
| `collapse` | 8 | up to **4** on one fallback section | **15 x 2** uniform hits | **34** |

Only the indirect incoming-damage portion receives Hematic Fortification and respects the incoming-damage config gate. The explicit 10/15-point hits do neither and do not clamp at zero.

Current recipe distribution:

- `safe_retry` (17): `crimson_beacon`, `exsanguination`, `hematic_fortification`, `hungering_earth`, `initiate_rite`, `pallid_vessel_rite`, `puppeteer_trial_gorebound_hulk`, `puppeteer_trial_marrow_spitter`, `puppeteer_trial_mnemonist_puppet`, `puppeteer_trial_scarlet_mummer`, `puppeteer_trial_veinwing_vulture`, `sanguine_attunement`, `sanguine_brotherhood`, `sanguine_fervor`, `sanguine_initiation`, `vascular_mending`, `votary_rite`.
- `offering_loss` (3): `crimson_vessel_rite`, `illuminatus_rite`, `sanguine_eclipse`.
- `fragile_damage` (8): `ashen_vessel_rite`, `bloodline_founding`, `bloodline_recall`, `founding_fane`, `hematic_unbinding`, `sanctified_rite`, `sanguine_dominion`, `scarlet_summons`.
- `collapse` (9): `ancestral_communion`, `apotheos_rite`, `archon_rite`, `bloom_of_qliphoth`, `chamber_of_will`, `covenant_vigil`, `eternal_covenant`, `horn_of_culmination_rite`, `pallid_shadow`.

Legacy rites without a loaded recipe use form-derived compatibility values: 2/4/6/8 magic HP by form, no explicit hit below Greater, one 10-point hit at Greater, or two 15-point hits at Grand.

### Lethean Judgment

Every other blood-active player within 16 blocks receives:

- **30 direct vascular damage to HEAD, ARMS, HEART, BODY, and LEGS**: 150 total, clamped to 0.
- Hemolysis amplifier 2 for 600 ticks, creating the repeated indirect path described above.

The direct 30-per-section loss ignores Hematic Fortification and both vascular-degradation config toggles. From full health, every section immediately falls from 100 to 70 (`STABLE`) before Hemolysis follow-up.

## Thresholds and recovery context

| Section health | Flow state |
|---:|---|
| 90-100 | `RAGING` |
| 75 to under 90 | `FLOWING` |
| 50 to under 75 | `STABLE` |
| 15 to under 50 | `VARICOSE` |
| Above 0 to under 15 | `ClOTTED` |
| 0 or below | `DEAD` |

Default passive recovery is **+0.1 every 100 ticks (5 seconds)** to every damaged section while food level is above 6: `0.02` vascular health per second. At that rate, one default 1-point Noetic cast takes 50 seconds to recover and Lethean Judgment's 30 points take 25 minutes. Cryostroma stillness can ramp the passive multiplier from `x1` to `x4`.

Other direct recovery offsets are:

- Sanguine Salve: +25 to the most damaged section, 40-tick cooldown.
- Vascular Poultice: +12 to each of the two most damaged sections, 40-tick cooldown.
- Arachnid Anastomosis / Bootlace: +`0.5 + 0.25 x amplifier` to every damaged section every 40 ticks.
- Rite of Vascular Mending: sets all five sections to 100.

Vascular debuffs are configurable and currently default to **disabled**. A dead section still blocks its Thelemic Memory even when debuffs are disabled.

## Findings worth balancing first

1. **BODY and ARMS are double-loaded.** They hold nearly three quarters of manipulation routes and receive 80% of fallback incoming damage. This is the clearest structural imbalance.
2. **HEART is protected until it suddenly is not.** It has only two Noetic routes and no fallback incoming-damage exposure, but Second Pulse is the largest Thelemic trigger and Lethean Judgment/rite randomness hit it normally.
3. **All Noetics cost the same vascular amount.** A cheap Humilis utility and an expensive Summa power both cost 1.0. Balance currently comes from blood/cooldown/effect differences, not vascular load.
4. **Thelemic cadence matters more than its small numbers.** Frequent mining, sprinting, stealth movement, and melee can outpace passive recovery despite individual 0.10-0.25 hits.
5. **Cardinal collapse stacks two systems.** Its magic HP hit invokes generic vascular degradation, then explicit direct vascular damage is applied separately.
6. **Lethean Judgment is the dominant single event.** It guarantees 150 total direct vascular loss, then adds a long Hemolysis exposure.
7. **Clamp and modifier behavior is inconsistent.** Noetic, incoming damage, and Lethean clamp to zero; Thelemic and explicit Harbinger rite hits do not. Fortification applies to Noetic, Thelemic, and incoming damage, but not direct rite loss.
8. **The shared config name hides split tuning.** `vascularDegradationOnManipEnabled` disables both Noetic and Thelemic strain, but `vascularManipStrain` changes only ordinary manipulation strain.
9. **Reference documentation has stale seven-section wording.** Runtime authority is five sections: HEAD, ARMS, HEART, BODY, and LEGS.

## Audit boundary

This is exhaustive for writes that lower vascular health. It does not enumerate every sword, mob attack, projectile, boss move, environmental hazard, or other call site that deals ordinary player HP damage, because all of those converge on the single incoming-damage formula above. Blood cost, Blood Absorption's separate channel-strain tiers, Morphic Strain, and Hematic Strain are not vascular-health damage unless they also cause an accepted player health-damage event.
