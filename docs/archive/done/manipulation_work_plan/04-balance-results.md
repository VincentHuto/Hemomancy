# Work area 4 — Balance implementation

7 September 2026 · [M18–M25](04-balance.md) · [Acquisition ledger](03-memory-recipe-ledger.md)

> Later verification: see [Area 6 results](06-acceptance-results.md) for current dedicated-test totals, remote evidence, new fixes and remaining acceptance. Earlier counts and presentation notes below describe this area's original pass.

The server balance pass is implemented. The changes below give expensive powers a clearer purpose and fix confirmed output, damage and channel problems. Live encounter comparisons remain open; the automated checks do not establish final difficulty or visual quality.

All costs are base mL before alignment, skill, scar and other cost modifiers. HP means health points, not hearts. For a ten-second channel interval **0 ≤ t < 10 seconds**, the start plus nine upkeep pulses cost ten payments. Holding through the payment at exactly ten seconds costs eleven. Damage figures describe the configured payload unless explicitly identified as accepted damage.

## Changes

| Task / manipulation | Before | Implemented result and purpose |
|---|---|---|
| M18: Ironhearted | 2 stored HP per fully charged cast | 4 HP per cast, unchanged 10 HP base cap, 300 mL and mandatory two-second charge. Three casts fill the cap for 900 mL; scars can extend capacity. Ten-minute storage allows preparation before combat. |
| M18: Iron Choir | One interception only at each second's pulse | Three interceptions per paid pulse, checked every channel tick. Own/allied projectiles are preserved. 250 mL/s and Resistance I retained. Budget resets rather than accumulates. |
| M19: Carrion Communion | Credited nominal damage, including overkill | Heals half the health actually removed from up to four afflicted blood-bearing targets. Rejected damage, absorption-only hits and overkill do not generate health. Cost remains 220 mL/s. |
| M20: Hemorrhage | 100 mL, eight seconds of Blood Loss | 75 mL, twelve seconds. Cheap visible-target status setup, without requiring a projectile collision. |
| M20: Crimson Coronation | 1,000 mL; default needle damage | 600 mL; full charge sets needle base damage to 3 and permits one additional pierced target. Partial strength scales damage. Up to eight incoming hostile attacks trigger one counterattack each within twenty seconds. Friendly attacks do not spend the crown. |
| M20: Blood Needle family | Hit override applied bleed even when damage was rejected | Removed the duplicate status path; accepted-hit effects remain in the existing arrow post-hurt hook. |
| M21: Absolute Stillness | Projectile slowing once per second | Hostile projectile velocity multiplied by 0.75 every channel tick inside the field. Own/allied shots preserved. Ordinary enemies retain Slowness IV; bosses receive Slowness I. Cost remains 300 mL/s. |
| M22: Penumbral Drift | 175 mL/s; repeated aggro clearing also affected bosses | 75 mL/s; ordinary aggro clears every half-second, bosses keep their target. Invisibility, Speed II and attack cancellation retained. |
| M23: Furnace Veins | 250 mL/s; owned animals could enter the enemy branch | 150 mL/s; shared allegiance/PvP policy protects allies, pets and summons. Allies thaw and receive refreshed Fire Resistance. Hostiles retain the two-damage heat pulse and ignition. |
| M24: Conductive Mark | 225 mL; eight-second setup | 150 mL; twelve-second setup, leaving 9.5 seconds after its 2.5-second shared cooldown. Missing targets fail before payment. |
| M24: Crimson Tithe | 400 mL entry for 500 borrowed mL | 100 mL entry, yielding 400 mL net liquidity. Requires room for the whole advance. Existing thirty-second expiry, double spent-blood collection and six-HP penalty retained. |
| M25: Vascular Dowsing | 500 mL/s, twenty-second cooldown, limited ore tags and occluded particles | 75 mL/s, two-second cooldown; common ore tag includes quartz, ancient debris and correctly tagged mod ores. Action bar reports count and nearest ore coordinates even underground. Unloaded chunks are skipped. |
| M25: Pyretic Forge | Output count overwritten with input count | Preserves recipe multiplicity and components, splitting output at item stack limits. Eight base inputs per 350 mL retained. |
| M25: Lignum / Canopy / Worked Lignum | Completed selections could keep charging until release | Completion or the 128-block limit automatically harvests permitted blocks and ends the channel. Earlier manual release still harvests; forced cancellation preserves blocks. Channel manager stops processing a channel that completed during its tick. |

## Comparison decisions

### M18 — Defense

| Choice | Slot purpose | Cost / limitation |
|---|---|---|
| Sanguine Ward | Cheap maintained protection | 10 mL/s; initial 6 HP pool, subsequent pulses add 2 up to 8. Requires continued channeling; no ten-minute reserve. |
| Ironhearted | Store protection, then cast something else | Degree V / Ferric 45; 300 mL buys up to 4 stored HP. Existing memory burden retained. Preparation and capacity prevent cheap emergency stacking. |
| Iron Retort | Timed retaliation | 250 mL; retain the reactive option rather than adding another persistent shield. |
| Iron Choir | A bounded answer to volleys | Degree V; 2,500 mL over the defined ten-second interval, at most thirty interceptions if projectiles actually arrive, plus Resistance I. Retaliation shares ordinary damage immunity frames. |
| Avatar / Armor / Complete | Sustained transformation packages | Base 75, Armor 175, Complete 400 mL/s. Retain their different body benefits and higher acquisition burden; do not compare their full package to shield HP alone. |
| Endless Hour / Phoenix Debt | Premium damage deferral / fatal-hit recovery | Retain the lifecycle and trigger restrictions established in areas 1–2. Neither becomes a cheap repeatable ward. |

The volley fixture uses real Arrow entities and verifies the three-shot budget and caster-shot exclusion. It is not a moving combat encounter or a measurement of retaliation DPS.

### M19 — Healing and cleansing

| Choice | Restored HP | Separate protection / condition |
|---|---|---|
| Blood Rush, 100 mL | 0.5 HP per second; twelve pulses over its 250-tick effect, up to 6 HP total | Gradual self-healing; caps at missing health. Recasting refreshes rather than stacks regeneration. |
| Carrion, 220 mL/pulse | Up to 1 HP per target per pulse, 4 HP with four targets | Needs afflicted blood-bearing enemies and accepted damage. A 0.25-HP victim now yields only 0.125 HP. |
| Lumen Suture, 250 mL | 2 immediate HP | Absorption II supplies 8 separate shield HP for twenty seconds; beneficial target routing retained. |
| Cauterizing Rebuke, 275 mL | No healing; loses 2 HP per removed poison/wither/bleed, with a 1-HP floor | On a successful purge, Absorption I supplies 4 shield HP for five seconds. It is a cleanse, not an enemy attack. |
| Hematic Beacon, 350 mL | 0.4 HP per pulse per eligible ally, up to 8 over twenty seconds | Stationary group support; staying inside the field matters. |

Keep Rush as economical recovery, Suture as immediate protection, Beacon as group value, Carrion as conditional combat sustain and Cauterizing as a specialist purge. Maximum figures assume enough missing health and no other modifiers.

### M20 — Projectiles and bleed

Retain Shot's 100 mL baseline, Guided's 140 mL targeting premium, Mortar's 200 mL charged area payload, and Halo's 220 mL orbital coverage. Needle, Fan and Lance retain single-target, spread and piercing roles at 100/140/150 mL. Aneurysm remains a separate finishing option. Their family memories and charge/collision rules were established in earlier areas.

Hemorrhage now offers the cheapest direct status setup. It does not inherit Needle's much longer bleed. Mob Blood Loss attempts 0.5 HP damage each tick, but normal immunity frames mean those attempts cannot be summed into ten HP/s. Player Blood Loss drains blood instead. Likewise, Fan/Halo projectiles hitting simultaneously cannot be counted as independently accepted damage.

Coronation's full-charge needle has nominal pre-mitigation damage around 11 at launch speed, versus the previous approximately 7. Flight, armor, affinity and immunity frames change actual damage. Eight prepared lances are eight conditional opportunities, not eight guaranteed hits or an instant burst. The accepted-hit regression also prevents a rejected needle impact from applying its long bleed.

### M21 — Control

Binding variants keep single-target duration, chaining and lattice coverage; existing boss duration/root restrictions stay in place. Glacial Grasp, Cryogenic Pulse and Rimebound keep aimed, area and sentence roles, including Rimebound's reduced boss slow and no boss cage. Magnetism retains pull control; Jolt, Activation Potential and Storm retain their different targeting/coverage. Rebuke, Impressment and Sovereign retain their command and boss restrictions.

Stillness now reacts between upkeep pulses: a projectile moving at 3 blocks/tick becomes 2.25 after one field tick. Successive ticks compound friction. The field is not swept collision detection: an exceptionally fast projectile can still cross the whole region between checks. Boss Slowness I is a 15% movement modifier versus ordinary Slowness IV's 60%; bosses remain responsive rather than being locked down.

### M22 — Stealth and revelation

Void Shroud stays the economical five-second burst at 100 mL. Drift's 750 mL ten-second budget buys maintained movement/invisibility and repeated ordinary-aggro clearing. Boss pursuit and attacking remain counterplay. Umbral Step/Reversal retain positional escape; Covenant retains its retaliation role; Eclipse/Well and Gloam retain field control. Flare, Prismatic Reproof and Unclosing Eye remain revelation or aimed pressure tools. Revealing enemies is not itself damage. No unsupported damage buffs were added to these alternatives.

### M23 — Fire and traversal

Furnace's ten-second budget is now 1,500 mL for nearby enemy heat and allied fire/frost support. Torch remains cheaper aimed sustained offense at 60 mL/s plus its staff-switch cost; Ignition and Flame retain quick focused/close offense. Vitric retains its 350 mL heavy charged blast: full three-second charge, configured eight damage, four-block impact radius and eight-second ignition. Its footprint and ignition distinguish it from Mortar.

Updraft's 225/300/350/425 mL forms retain baseline launch, greater ascent, suspended descent and expulsive control. No movement constants were changed without terrain/airtime evidence. Practical ledge reach and landing safety remain live acceptance work.

### M24 — Combos and saint rewards

Mark plus a follow-up costs 150 mL plus that follow-up's effective price. The new twelve-second mark leaves 9.5 seconds after shared cooldown, before subtracting any follow-up charge. Existing Living Circuit, Cloud/Expansive/Pursuing/Tempest, Hunger, Grave Debt, Funeral Bell and Bloom roles remain: retaliation, area/moving debuffs, conditional sustain and finishers. Their rejected-damage and once-only reward fixes from areas 1–2 remain in force.

Tithe example at base prices: start with 500 blood, pay 100, then hold 400 blood plus 500 borrowed reserve. That is a 400 mL temporary net advance. If 100 reserve is spent, expiry reclaims the remaining 400 and attempts to collect 200 real blood plus six magic damage. Settlement clears its state before collection and cannot repeat. Blood collection is capped by current blood; this is not an indefinitely carried debt. The reserve only covers eligible spending deficits, not a direct healing refill. Existing borrowing configuration still applies.

### M25 — Utility

Forge processes eight base inputs for 350 mL: 43.75 mL/input, with recipe-defined output counts. The regression uses a sixteen-output recipe to verify 128 results and legal stack sizes. Mending retains up to fifty durability for 150 mL. Dowsing checks a 17×17×17 cube (4,913 positions) once per paid pulse; an empty result is explicit and does not load more chunks.

Lignum starts at two selected blocks/second and accelerates with distance up to eight; selection remains capped at 128 blocks and a maximum radius of 24. Automatic completion goes through the existing break-permission, block-entity and unbreakable-block exclusions. Canopy traverses leaves to connect trees; it does not harvest leaves. Effusion, Hemolymphal Pulse and Rampart retain growth, detection and temporary cover roles. Their practical output/terrain-restoration comparisons remain acceptance checks rather than new tuning claims.

## Acquisition and remaining acceptance

No ranks, alignment requirements or memory recipes changed. The [90-row acquisition ledger](03-memory-recipe-ledger.md) remains the exact catalyst, enzyme, weaving-blood and lantern-time reference. Crude alternatives, family learning gates, staff graft activities and saint reward routes remain intact. M14's final recipe repricing still follows encounter validation; lowering both acquisition and casting prices now would obscure which adjustment helped.

M18–M25 have implementation and source-comparison evidence, but their full live acceptance remains open: moving volleys, accepted damage against armored targets, boss pursuit, PvP/team encounters, traversal reach, underground display readability and survival acquisition effort. No live client combat or visual quality was verified in this pass.

Area 5 must still author the promised effects. **Crimson Coronation still needs visible bloody swords**, and Dowsing still needs an intentional visual treatment; existing particles are not claimed as finished signature effects.

## Verification

- Twelve new `ManipulationBalanceGameTests` pass: shield amount/cap, accepted drain healing, volley budget, per-tick friction, Forge multiplicity, allied Furnace support, rejected needle status, Nether ore coverage, boss aggro, Tithe settlement, Lignum completion and crown charge consumption/piercing.
- Thirteen focused JVM checks pass across BodyIdiom, MorphlingBodyIdiom, ManipulationCasting and LignumMortis rules.
- Full GameTest run: **254/257 pass**. The same three baseline failures remain: `delayedinvalidationfallsbacktothestakeitem`, `formationacceptsautopickedupoutput`, `formationplacesandregistersallsixstakedirections`.
- Evidence: `build/manipulation-balance-red.log`, `build/manipulation-balance-expanded.log`, `build/manipulation-balance-final.log` and `build/test-results/test/`.

Changed implementation owners: `ManipulationInit`, `BodyIdiomRules`, `ManipulationCombatHelper`, `ManipulationReactiveEvents`, `ManipulationChannelManager`, `BloodNeedleEntity`, and the named manipulation classes above. In-game English descriptions now match these behaviors; cost panels use the updated registry values.
