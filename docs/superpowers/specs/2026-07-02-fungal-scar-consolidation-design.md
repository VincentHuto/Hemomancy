# Fungal Scar Consolidation — Design

> **Date:** 2026-07-02
> **Status:** Design / planned. No code or data changes yet.
> **Parent audit:** [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md)
> **Sibling specs:** [Rogue Hemomancer Wills](2026-07-02-rogue-hemomancer-wills-design.md) · [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md)
> **Current-state references:** [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) §13.2 / §13.4, [fungalscar.md](../../fungalscar.md)

This is the middle doc in a chain. The [Rogue Hemomancer Wills](2026-07-02-rogue-hemomancer-wills-design.md) spec links **back** to this one because its counter-mechanic is the *Oculiflora reticularis* scar defined below (§5.3). This spec in turn **references** the [Morphling Fungal-Strain Reframe](2026-07-02-morphling-fungal-strain-reframe-design.md) for the shared fungal identity, the material-economy split, the naming register, and the Sanguiflora relocation.

---

## 1. Goal

Consolidate fungal scars from **9 to 8**, cutting the ones that are generic potion-charms, that bleed into another system's lane, or that are redundant death-saves, and adding new abilities that are **wholly unique to the fungal-scar identity**. Keep fungal scars Latin-only on purpose (see §6).

## 2. Current roster (verified against code)

There are **9** registered `ItemFungalScar` variants (the `immature_fungal_scar` is an intermediate, not a scar). Confirmed in `ItemInit` / `ScarInit` and the 9 recipe files under `data/hemomancy/recipe/fungal_scar/`.

## 3. Identity: what only a fungal scar can be

With four systems that grant power, a fungal scar earns its single equipped slot (`ScarType.FUNGAL`, slot 0) only by doing something none of the others can:

- Armor = the **worn contract** (contact terms, immunities, conversion, incorporeality).
- Morphling = the **raised symbiote** (ecology / resource / anatomical movement). See the [Morphling reframe](2026-07-02-morphling-fungal-strain-reframe-design.md).
- Manipulation = the **willed cast** (targeted burst / CC / placed constructs).
- **Fungal scar = a permanent rewrite of how the body connects to the mycelial network** — the single-slot, Qliphoth-tier bodily alteration.

Because only one fungal scar equips at a time, they **do not need tendency-completeness** the way cerebral scars do. Aim for a few unforgettable choices, not broad coverage. (The result below doubles Animus and omits Lux — acceptable for a single-slot system.)

## 4. Verdict on all 9

| Fungal scar | Tendency | Effect | Verdict |
|---|---|---|---|
| *Antiphonomyces resonans* | Ductilis | Echo-cast: 20% free re-cast | **KEEP — flagship.** Modifies manipulations; the model scar. |
| *Saprovitta vestigium* | Flammeus | Feeding Wake damaging trail while moving | **KEEP.** Movement-triggered, no overlap. |
| *Talaromyces Minus* | Ferric | Haste + ore vein-mining | **REWORK.** Strip Haste (redundant with `ferric_resonance` + Irontooth morphling); keep vein-mining as "hyphal-sense mining." |
| *Noctifly Agaric* | Animus | Fungal elytra (glide) | **KEEP — restored.** A desirable, distinctive utility scar; the glide/Edacious overlap is acceptable for a single-slot pick, and it is the one aspirational-mobility fungal scar. |
| *Thanomyces resurgens* | Congeatio | Split Husk — cheat death, reform at 25% | **CUT — redundant death-save.** Foxfire (Ink Mantle), Winter Shroud (Cryptobiosis), Silent Archon refusal, and vanilla totems already over-cover death-prevention. The Congeatio slot is better spent on a non-death-save scar (§5.4). |
| *Sanguiflora cadens* | Mortem | Vein Orchard — on-kill resource bloom | **CUT — relocate to morphlings.** On-kill resource-gen is the morphling lane; folds into Gravecap's Cordyceps line (see [Morphling reframe](2026-07-02-morphling-fungal-strain-reframe-design.md) §10). |
| *Lumina Devorans* | Tenebris | Night Vision + Strength + Resistance | **CUT.** Generic three-buff stat-stick; no network-rewrite identity. |
| *Respergillus* | Animus | Water Breathing | **CUT.** Pure single-potion charm; weakest fit. |
| *Anastocordyceps nexus* | Lux | Latching Vein — on-strike tether | **CUT.** On-strike CC leans into the manipulation lane. |

## 5. New scars (wholly unique to the system)

Each new scar uses a fungal property no other system touches, and each is a different verb from the survivors (echo / trail / mine / glide). Rhizovitta, Putrivora, and Oculiflora backfill the Animus, Mortem, and Tenebris slots the cuts vacate; Cryostroma (§5.4) replaces the cut Thanomyces death-save so Congeatio keeps a fungal scar without another cheat-death.

### 5.1 *Rhizovitta communis* — "the shared rootband" (Animus)

**Verb: draw from the network.** While standing on or near fungal terrain (erythrocytic mycelium, venous stone, hyphae), inside a Qliphoth Bloom radius, or anywhere in the Fungal Dimension, the wearer is "plugged in": partial manipulation-cost refund and slow blood-volume refill from the ground. Off the network, nothing.

**Unique because** it is the only ability keyed to the mod's fungal *world* content — armor/morphling/manipulation all work anywhere; this exists only in the shared body's soil. Turns Qliphoth terrain into home ground; "to one it shall return" as a standing wave. *Impl:* tick check for tagged fungal blocks / `QliphothBloomSavedData` radius / dimension test → scaling "Rooted" state.

### 5.2 *Putrivora resolvens* — "the dissolving rot-eater" (Mortem)

**Verb: digest affliction.** Incoming negative effects (Poison, Wither, Hunger, Blood Loss) are not cleansed — they are *eaten*: each decays several times faster than normal, and while draining it feeds the wearer a small blood trickle instead of damaging them.

**Unique because** every other debuff answer either *negates* (Unstained armor immunity) or *removes* (Cauterizing Rebuke, Absolving Step, Winter Shroud cleanse). None *convert* affliction into sustenance over time. Keeping it as accelerated-decay-plus-feeding (not negation) is what keeps it out of armor's immunity lane. *Impl:* on effect-applied/tick for the wearer, if the effect is in a "digestible" set, step down duration and add a small heal/blood tick.

### 5.3 *Oculiflora reticularis* — "the eye-bloom of the network" (Tenebris)

**Verb: see the network — and its agents.** A permanent, dialed-down version of the Qliphoth pome's void-register awareness. Reveals fungal-network features through terrain: buried blooms, spore veins, morphic-nectar pools (the Primalization gate), Saint chambers / Hallowed Residuum, wild Morphling Polyps, and infected / blood-active creatures — including ones cloaked by Void Shroud / Umbral Step.

**Its killer application — see [Rogue Hemomancer Wills](2026-07-02-rogue-hemomancer-wills-design.md):** the ambushing Wills approach semi-incorporeal and *materialize* to strike. Oculiflora renders their **pre-materialization outline** for a few seconds before they fully spawn, converting an ambush into a prepared fight. This is the single unique counter to the ambush mechanic, and the scar's value scales exactly as the Will threat scales with degree.

**Design guardrails:**
- **Do not make it mandatory.** Wills must be beatable without it — keep a non-scar cue for everyone (audio sting / Whisper line / HUD tremor). Oculiflora gives the clean visual lead time; it is the edge, not the entry fee.
- **Optional active hook ("sight → tap"):** while active, the wearer may draw a trickle of blood/enzyme from revealed fungal terrain, so the eye also *feeds* — looping back into the Rhizovitta "draw from the network" fantasy. Decision pending (see §8).
- Reveal targets are fungal/infected/network features only — it does **not** reveal ore (that is Talaromyces / Irontooth), keeping it out of the mining lane.

*Impl:* client outline/glow pass over fungal-tagged entities/blocks/bloom positions and Will pre-spawn anchors within radius, gated on the equipped scar; reuses the outline tech from Prismatic / Crimson Sight / the Mole-primal nectar highlight.

### 5.4 *Cryostroma perdurans* — "the enduring cold-mass" (Congeatio)

**Verb: conserve.** While the wearer holds still — stationary, sneaking, or otherwise at rest — the scar sinks the body into a fungal cryo-dormancy: blood-volume regeneration and vein-section healing accelerate sharply, ramping the longer you stay still and canceling the moment you move or cast.

**Unique because** it is a *rest-state bodily-repair* rewrite. It never triggers on lethal damage, so it is **not** a death-save (unlike the cut Thanomyces, Foxfire's Ink Mantle, or Silent Archon refusal — which, with vanilla totems, already over-cover death-prevention). It is not terrain-gated like Rhizovitta, and it is not the Winter Shroud morphling's active molt-escape. It fills the recovery / downtime niche — safe-spot regeneration and post-fight vein-section repair — that nothing else occupies as a scar, and it keeps Congeatio's preservation/stasis identity after the death-save is removed. *Impl:* tick a "conserving" state while wearer movement is below a threshold and no manipulation was cast recently; scale blood regen and `IVascularSystem` section healing by a ramping multiplier; clear on movement/cast.

## 6. Naming: Latin-only, on purpose

Fungal scars stay pseudo-Latin binomials as their **primary** name — the in-world justification being that they are so rare and one-off the Order has not given them common names yet. This is the deliberate counterweight to the morphlings' folk-name register (see [Morphling reframe](2026-07-02-morphling-fungal-strain-reframe-design.md) §5): morphlings go maximally folk *so that* scars can stay maximally scholarly, and the contrast becomes the wayfinding signal. Holding to 8 keeps the "too rare to have earned a name" conceit reading as intentional rather than as clutter.

## 7. Resulting roster (9 → 8)

| Fungal scar | Tendency | Verb | Status |
|---|---|---|---|
| *Antiphonomyces resonans* | Ductilis | casts **echo** | keep |
| *Saprovitta vestigium* | Flammeus | movement **trails** spores | keep |
| *Talaromyces* (reworked) | Ferric | **mine** by hyphal sense (Haste removed) | rework |
| *Noctifly Agaric* | Animus | **glide** on fungal elytra | keep (restored) |
| *Rhizovitta communis* | Animus | **draw** from fungal ground | new |
| *Putrivora resolvens* | Mortem | **digest** affliction into blood | new |
| *Oculiflora reticularis* | Tenebris | **see** the network + Will ambushers | new |
| *Cryostroma perdurans* | Congeatio | **conserve** — rest-state regen / vein-heal | new |

**Cut for good:** Respergillus, Lumina Devorans, Anastocordyceps nexus, Thanomyces resurgens (redundant death-save). **Relocated:** Sanguiflora cadens → morphling (Gravecap). **Restored:** Noctifly Agaric.

## 8. Open questions & scope

- **Oculiflora "sight → tap":** in or out? Lean in, to guarantee an active payoff beyond perception.
- **Migration:** the four cut scars (Respergillus, Lumina Devorans, Anastocordyceps, Thanomyces) and the relocated Sanguiflora need save-data handling — convert existing equipped/known instances to the nearest survivor or refund crucible materials. Noctifly is restored, so existing Noctifly instances stay valid.
- **Recipe data:** remove 5 files under `data/hemomancy/recipe/fungal_scar/` (Respergillus, Lumina Devorans, Anastocordyceps, Thanomyces, Sanguiflora), keep/retarget Talaromyces (drop Haste) and Noctifly, add 4 new cultivation recipes (Rhizovitta, Putrivora, Oculiflora, Cryostroma), per the `fungal_scar_cultivation` schema in [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) §13.4.
- **Testing:** focused resource/source tests for the 8 registrations, the removed-item migration (incl. Thanomyces), and the Oculiflora render gate; update [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) §13 and [fungalscar.md](../../fungalscar.md).
