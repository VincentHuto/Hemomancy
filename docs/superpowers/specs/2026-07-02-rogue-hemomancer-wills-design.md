# Rogue Hemomancer Wills — Ambusher System Design

> **Date:** 2026-07-02
> **Status:** Design / planned. Largest item in this cluster — a new enemy system, not a tweak. No code yet.
> **Parent audit:** [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md)
> **Counter-mechanic lives in:** [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) §5.3 (*Oculiflora reticularis*)
> **Also references:** [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md) · [LORE_REFERENCE.md](../../LORE_REFERENCE.md) · [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md)

This is the first doc in a chain. It links **back** to the [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) spec, whose *Oculiflora reticularis* scar is the intended counter to the ambush mechanic defined here; that scar spec in turn references the [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md).

---

## 1. Goal

Introduce **Wills** — tiered, semi-hidden rogue hemomancers that ambush the player on the fly using blood manipulations of a specific school. The system:

- turns the mod's rich manipulation kit into a solo **PvE experience that feels like PvP** (blood-mage duels), filling the thin "server-type interaction" gap;
- **reintegrates the orphaned Blood Drunk Puppeteer** from a random mob into a lore-coherent enemy class;
- gives *Oculiflora reticularis* a killer application (threat and counter introduced as a pair); and
- **dramatizes the revelation arc** — the Fungal Whispers *tell* the truth; the Wills *show the stakes and test the player*.

## 2. Naming: build on "Will," a word the mod already owns

"Will" already means self/ego rendered as a blood-spirit in this mod: `blood_rush` summons a **Wretched Will**; the Degree-6 refuge is the **Chamber of Will**; the Silent Archon's identity is *keeping your will*. So the ambushers are hostile **Wills**, and the existing Wretched Will summon becomes the visible tip of a larger truth — the network is full of Wills, and the loose ones hunt you. No new noun required.

## 3. Two origins (mapped onto the endgame fork)

Both types are wanted; they are the two halves of the revelation dramatized.

### 3.1 Broken Wills (the Faded / the Unwilled)
Dead or absorbed Harbingers whose ego collapsed under the revelation. Tragic, not clinical — they run their old school's combat muscle-memory with nobody home. **The failure state made flesh:** the Silent Archon *kept* their will; these are everyone who could not. Fighting them is a running preview of what dissolving-without-strength looks like, which gives the eventual Silent-Archon-vs-Apotheos choice weight the player *earned* by seeing the cost.

### 3.2 Sent Wills (Proctors / the hive's examiners)
Tailor-made by the Fungal Entity to measure the player. Cold and deliberate. This is fully in character: existing lore establishes the Entity as *watching practitioners ripen* and *reading degree as a sporulation assessment* (the Sanguine Monolith fragment). A Sent Will **mirrors or counters the player's dominant tendency** — a skill-check on the player's own build. The hive is not fighting; it is *grading*.

Broken Wills look backward (what you could become); Sent Wills look forward (whether you are ready for Apotheos).

## 4. Reintegrating the Blood Drunk Puppeteer

The Puppeteer is currently a random mob that drops Prismatic-armor material. Reframe it as **one type of Broken Will** — a Harbinger so lost to **Blood Drunkenness** (the existing foreign-blood backlash effect) that the hive pulls its strings. This explains the entire orphaned cluster: *Blood Drunk* = the failure mode that unmade it; *Puppeteering Thread* = the hive's strings, harvested off the corpse (Prismatic material link preserved); *Enthralled Dolls* = its combat gimmick, a puppet fighting a puppet. Nothing is cut — the Puppeteer gains a reason to exist and becomes the archetype for the wider class.

## 5. Tier ladder (scaling on Initiatory Degree)

The dread engine: the deeper the player goes, the more the hive notices — ambushes escalate in lockstep with the Whispers.

| Tier | Gate | Who | Kit | Feel |
|---|---|---|---|---|
| — | pre-D4 | nothing | — | beneath notice; Whispers not yet begun |
| **I** | D4–5 (Whispers begin) | rare single **Faded**, one school | Humilis manips, flickery | "did I just see something?" |
| **II** | D5–6 | **Broken Wills**, school-flavored, sometimes paired | Mediocritas manips | real duels; ambush pressure |
| **III** | D6–7 | first **Sent Wills** — counter the player's dominant tendency | school combos | the hive starts grading |
| **IV** | D7 / Archon+ (post-Communion) | elite Proctors that mirror the player's build | Summa manips | the final exam before Apotheos |

Strict degree-gating: a Tier-IV must never reach a D4 player.

## 6. When and where they strike

- **Ambient chance scaling with degree**, amplified in fungal terrain, near Qliphoth Blooms, and in the Fungal Dimension.
- **Blood Moons** amplify spawns (lore already: "Thirsters and Fargones stir in the dark").
- **After a Fungal Whisper fires**, a Will may follow — the whisper becomes the *herald* of a test.
- **Vulnerability pressure:** deep **Blood Drunkenness** makes the player "smell riper" and draws Wills harder (ties the existing debuff into the system).

**Sanctuaries (no Wills spawn):** Founding Fanes, the Chamber of Will, and Harbinger Outposts are consecrated ground. This hands the territory systems a concrete new value — safety from the hive — and gives the player a reason to build a home, with no new systems designed.

## 7. Combat identity

- **They fight with the player's own systems** — school-keyed blood manipulations, not mob swings. A Flammeus Will opens Sanguine Ignition → Vitric Combustion; a Mortem Will runs Hemorrhage → Exsanguinate; a **Tenebris Will runs Void Shroud + Umbral Step** (double-cloaked — the nastiest ambusher and the strongest argument for Oculiflora).
- **Ambush approach:** a Will drifts in **semi-incorporeal** (hard to see/hit), closes distance, then **materializes and bursts**. Unseen, it is a jumpscare — which is the setup for the counter in §8.
- **Multiplayer rule:** target the ripest (highest-degree) player, or scale to the group as a shared threat.

## 8. The Oculiflora counter (the matched pair)

The ambush **telegraphs**. When a Will is selected to attack, a **pre-materialization anchor** exists for a few seconds — invisible to normal players, rendered as a **faint ghostly outline to an *Oculiflora reticularis* wearer** (see [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) §5.3). With the scar the ambush becomes a *prepared fight* — seconds to reposition, ward, or pick an opener. Without it, the player eats the jumpscare.

This is the elegant property of the pairing: **the scar's value scales with the threat** — deeper degree → more/stronger Wills → the eye matters more. It is the one thing in any system that converts the hive's assassins from ambush into engagement.

**Guardrail:** Oculiflora must not be *mandatory*. Keep a non-scar cue for everyone (audio sting / Whisper line / HUD tremor) so unscarred players can react; the scar only grants the clean visual lead time. Edge, not entry fee.

*Impl sketch:* the ambush picks a spawn anchor entity; that anchor renders through-terrain only for scar-wearers (reuse Prismatic/Crimson Sight outline tech); a timer then materializes the Will and begins engagement.

## 9. Rewards — "to one it shall return"

Wills do not drop iron and rot. On death they **dissolve — blood returning to the one** — leaving school-keyed spoils:

- **Crude memory shards / enzyme essence of their tendency** — a Flammeus Will becomes a *source* of Flammeus material, plugging the ambushers into the progression economy instead of being pure nuisance.
- **Faded Memory** (rare) — a fragment of the unmade Harbinger, usable as a loom/scar catalyst.
- **Optional ripeness track:** defeating **Sent Wills** ticks a hidden readiness the Entity assesses, foreshadowing or soft-gating Apotheos. Keeps the examiner's tests meaningful; scope-flagged as optional.

## 10. Why this is a keystone, not a side-mob

The Whispers *tell* the truth; the Wills *show* the stakes (fail = become the Faded) and *test* the player (the hive measuring the fruit). By the Silent-Archon-or-Apotheos fork, the player has spent the midgame fighting people whose will did not survive — so "keep yourself" vs. "dissolve into the one" is a choice they were educated into at swordpoint. Oculiflora completes the poetry: the player uses the hive's own connective tissue to see the hive's own assassins coming.

## 11. Scope & balance flags (read before building)

- **Biggest build in this cluster:** new caster entity AI running the manipulation kit, a degree-scaled dynamic spawn/ambush system, the materialization + pre-spawn telegraph, sanctuary exclusion, and a loot economy. Sequence it after the scar/morphling reshuffles.
- **Frequency is load-bearing.** On-the-fly assaults get infuriating if they interrupt building/exploring. The sanctuary rule and a per-player cooldown are mandatory, not optional. Everything behind server config (frequency, tier gates, enable/disable).
- **Difficulty gating** strictly by player degree; never over-tier a player.
- **Opt-in tone:** the mod is morally gray and opt-in — Wills should feel like the *cost of ascending*, avoidable by staying out of fungal terrain and using sanctuaries, not a grind wall. The Silent Archon path might draw fewer (they refused the hive).
- **Don't make Oculiflora mandatory** (§8 guardrail).

## 12. Open questions

1. Do Broken and Sent Wills share one entity type with data-driven origin/school, or separate types? Lean one type, data-driven.
2. Should the "ripeness" track (§9) be real, or pure flavor? Decide before wiring Sent Wills.
3. Do Wills despawn if the player flees to a sanctuary mid-fight, or pursue to the boundary?
4. How many schools spawn per encounter at Tier IV — single mirrored school, or a small coven?
5. Testing: entity registration/AI, degree-gated spawn rules, sanctuary exclusion, the Oculiflora telegraph render, and loot tables; update [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) (mob entities) and [LORE_REFERENCE.md](../../LORE_REFERENCE.md) (the Wills, the two origins, the Puppeteer reintegration).
