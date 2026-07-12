# Player Power Systems Audit — Manipulations, Armor Sets, Morphlings

> **Last updated:** 2026-07-02
> **Scope:** Cross-system design audit of the three main player buff/bonus systems: Blood Manipulations, Harbinger Armor Sets, and Morphlings. Covers role comparison, overlap diagnosis, cleanup/cut targets, and the forward plan for coherent, balanced growth.
> **Sources:** [LORE_REFERENCE.md](LORE_REFERENCE.md) for canon, [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) for mechanics, and [MORPHLING_REFERENCE.md](MORPHLING_REFERENCE.md) for the eight canonical strains. Code anchors report implementation status and must be corrected when they contradict those docs.
> **Status vocabulary:** `implemented` / `partial` / `dormant` / `planned`, matching HEMOMANCY_REFERENCE.md.
> **Follow-on specs (2026-07-02):** [Rogue Hemomancer Wills](superpowers/specs/2026-07-02-rogue-hemomancer-wills-design.md) → [Fungal Scar Consolidation](superpowers/specs/2026-07-02-fungal-scar-consolidation-design.md) → [Morphling Fungal-Strain Reframe](superpowers/specs/2026-07-02-morphling-fungal-strain-reframe-design.md). These three cross-linked docs carry the morphling 12→8, fungal-scar 9→8, naming register, and the Wills ambusher system forward from this audit.
> **Companion examination:** [BLOOD_MANIPULATION_EXAMINATION.md](BLOOD_MANIPULATION_EXAMINATION.md) — current-state deep dive on the manipulation (expenditure) system: the 60-entry catalog by tendency, acquisition lanes, modifier economy, and code-verified runway gaps.
> **Implementation plans (2026-07-03), recommended build order:** [Guardrails / Phase 1](superpowers/plans/2026-07-03-audit-phase1-guardrails.md) → [Fungal Scars](superpowers/plans/2026-07-03-fungal-scar-consolidation.md) → [Morphlings](superpowers/plans/2026-07-03-morphling-fungal-strain-reframe.md) → [Wills](superpowers/plans/2026-07-03-rogue-hemomancer-wills.md). Orphaned ideas and pending decisions are ledgered in [DEFERRED_IDEAS.md](DEFERRED_IDEAS.md).

---

## 0. Canonical Theme

All three systems express one doctrine, and future work on any of them should be tested against it:

> *Use the life around you that yearns to feed from and lend to your blood, as all blood was once one and to one it shall return.*

Read mechanically, this sentence is a **circulation diagram** with three phases — and Hemomancy has exactly three player power systems. The core design decision of this audit is to assign each system **one phase of circulation** so that overlap becomes structurally impossible rather than case-by-case policed:

| System | Circulation role | The question it answers | Character |
|---|---|---|---|
| **Blood Manipulations** | **Expenditure** — blood leaving the body as will | "What do you *choose* to do?" | Active, instant, targeted, paid per cast |
| **Armor Sets** | **Exchange membrane** — the terms of contact at the skin | "When the world touches you, who pays, and at what rate?" | Reactive, unconditional, slow to change |
| **Morphlings** | **Metabolism** — the living world lending and feeding | "What does the life attached to you gather, convert, and share?" | Ambient, contextual, relational |

Cerebral and fungal **Scars** are the quiet fourth layer. They are not part of the triad and should not compete with it: scars are the **tuning layer** — percentages, alignments, and modifiers on the other three systems, never independent verbs. `Antiphonomyces resonans` (echo-casting *manipulations*) is the model scar; scars that grant standalone reactive combat effects drift toward armor's lane and should be tuned back toward modification over time.

---

## 1. The Three Systems at a Glance

| Axis | Manipulations | Armor Sets | Morphlings |
|---|---|---|---|
| **Count / breadth** | 60 registered (`ManipulationInit`) across 8 tendencies, 5 ranks | 6+ full sets on one Armature ladder, 3 final ascensions, Silent Archon, one-off pieces | 12 species × 6 maturity stages |
| **Body slots** | N equipped slots (Manip Slots skill, base + 5) | 4 worn pieces | 1 equipped (staff-carried) + optional Cradle |
| **Acquisition** | Crude shards → Somatic Loom weaving → scar-catalyst routes → Saint Canon | Armature reagent ladder, degree-gated (D2→D7) | Wild polyps → Specimen Jar/Bestiary → Incubator → Nectar |
| **Progression driver** | Rank degree gates; `ManipLevel` use-leveling (thin today) | Initiatory Degree + reagents + rite hardware | Enzyme power thresholds (10/30/60/100) + Apotheos nectar gate |
| **Cost model** | Blood + XP per cast, cooldown, vein-section strain | None ongoing; large one-time blood costs at the Armature | Currently free (`getBloodCost()` = 0); Primal actives cost blood + Morphic Strain |
| **Swap friction** | Loadout management (Reliquary, Dendritic Distributor) | High — re-forge at the Armature | **None today** (free hot-swap; identified problem, see §4.3) |
| **Power ceiling** | Unlimited, but every point paid from a finite pool | Highest unconditional floor (immunities, incorporeality) | Currently approaches a second armor set in one slot (see §3) |
| **Code anchors** | `BloodManipulation`, `ManipulationInit`, `KnownManipulationEvents` | `ArmorSetBonusHandler`, `ArmorSetAbilityRegistry`, `HematicArmatureBlockEntity` | `MorphlingItem` subclasses, `MorphlingIncubatorBlockEntity`, `MorphlingCradleBlockEntity`, `EquippedMorphlingEvents` |

### 1.1 What each system does well today

**Manipulations** are already a clean expenditure system: per-cast blood/XP costs, cooldowns, anatomical vein strain, and a deep modifier economy (Efficiency, Dynamic Use, Blood Flow, Sporitic Resonance, Blood Drunkenness, purity penalties). Manipulation blood-cost modifiers now route through `ManipulationCostLedger`, while equipped manipulation capacity routes through `ManipulationSlotLedger`; the Scrying Podium exposes the selected manipulation's base -> effective cost and the active slot cap breakdown. Roughly a third of the catalog is non-combat utility, which sells hemomancy as a way of living rather than a combat kit. No structural changes needed; the gaps are runway (empty `MAGISTER`/`PERFECTUS` ranks, unused `CHARGED` type, thin `ManipLevel` payoff).

**Armor sets** already read as contact contracts: Barbed (wounding me costs you blood), Blood Lust (my wounds on you feed me), Sheolic (fire cannot touch me; touching me burns), Silent Archon (physical contact severed in both directions). Flat shared base stats put all identity in behavior. The Armature ritual gives armor high commitment weight. This is the membrane system and should own that role exclusively.

**Morphlings** have the strongest *fantasy* (a living organism raised, fed, and visibly colonizing the player) but the weakest *role discipline*: several species currently do membrane work (on-hit reflect, on-hit flash, lifesteal) that duplicates armor, and their unique claim — being the ambassador of external life — is underexploited.

---

## 2. Overlap Audit

Collision severity: **HIGH** = same trigger *and* same effect family (direct duplication); **MEDIUM** = same effect family, different trigger (stacking pressure); **LOW** = thematic echo, acceptable as-is.

| # | Effect family | Colliding sources | Severity |
|---|---|---|---|
| 1 | **On-hit blind/flash counter** | Prismatic armor set flash (blind+nausea+outline attacker, 8s CD) · Cuttlefish **Chromatophore Flash** (Mature: flash blinds attacker + nearby hostiles) · Scar of the Halo / Veil (blind attackers) | **HIGH** — Prismatic vs Cuttlefish is near word-for-word |
| 2 | **Melee reflect / thorns** | Barbed set (2 dmg + Blood Loss) · Urchin **Spine Lash** (reflect 25–45% + slow) · Chitinite morphling **Carapace Thorns** (reflect 20–40%) · Scars of Thorn/Anvil/Crucible (reflect 1/2/3) | **HIGH** — two morphlings duplicate each other *and* the armor fork |
| 3 | **Passive blood income** | Hematic Iron set (+2 blood/s) · Leeches **Sanguine Siphon** passive · Lodestone Faceplate trickle · `ferric_transmutation` granting Sanguine Siphon II · Cradle leeching | **MEDIUM** — different systems, one uncapped income channel |
| 4 | **Melee lifesteal** | Blood Lust set bonus (10% of melee damage) · Leeches **Life Steal** (Developing: 15–25%) | **HIGH** — same trigger, same effect, stacks |
| 5 | **Death prevention** | Cuttlefish **Ink Mantle Reprieve** (Apex, blood spend, 10 min) · Silent Archon refusal (3,000 blood, 10 min) | **LOW** — Thanomyces Split Husk was removed during fungal-scar consolidation, leaving two high-cost reprieves with different sources |
| 6 | **Armor toughness** | Chitinite *armor* set (+2.0 toughness, 25% projectile DR) · Chitinite *morphling* **Chitinous Bulwark** passive (toughness) — same effect name across two systems | **MEDIUM** — brand confusion plus stacking |
| 7 | **Move/attack speed** | Serpent **Serpentine Guile** (+15%/+10%) · `blood_rush` effect (+20%/+10%) · Tengu mask (speed on hit) | **MEDIUM** — persistent vs cast vs contact; needs a cap, not a redesign |
| 8 | **Reveal/Glowing** | Bat **Echoic Perception** (passive radius glow) · `hemolymphal_pulse` / `crimson_sight` / `unclosing_eye` (cast reveals) · Mole **Burrow Sense** | **LOW** — passive sense vs willed sonar is a legible split; keep |
| 9 | **Mining haste** | Mole **Burrower's Instinct** · Talaromyces Minus fungal scar (vein mining only) · `ferric_resonance` (Haste II, 30s) | **LOW** — Talaromyces no longer contributes Haste, so this is now a utility overlap rather than a stacking-speed risk |
| 10 | **Fall/flight mitigation** | Noctifly Agaric (fungal elytra) · Bat **Membrane Glide** · Spider fall arrest / Silk Tether · Edacious flight · Venous Strider Sabatons · Scar of Descendence | **LOW** — mobility spread across tiers is fine; Edacious flight must stay the only true flight below Apotheos |

**Root cause:** all three systems are currently allowed to answer the same question — *"what buff do you have?"* — so they compete on one axis and inevitably converge on the same effect vocabulary (regen, thorns, lifesteal, blind, reveal, speed, cheat-death).

---

## 3. Cleanup and Cut List

Concrete migrations, ordered by severity. "Re-role" preserves the fantasy while moving the mechanic to the owning system's trigger vocabulary. Every migration below is `planned` unless noted.

### 3.1 HIGH — direct duplications (cut or re-role)

1. **Cuttlefish Chromatophore Flash → re-role.** The on-hit counter-flash is Prismatic armor's set identity; cut the on-hit trigger from the morphling. Replacement in the cuttlefish's own (ecological) lane: chromatic camouflage — fading invisibility while stationary in water or darkness — and/or expand Sepia Wake's sprint-ink into brief area vision-denial that follows movement. The cuttlefish stops being a counter-attack and becomes an environment.
2. **Chitinite morphling Carapace Thorns → cut reflect; keep the plate.** Reflect is doubly redundant (Barbed armor + Urchin morphling). Chitinite's identity is absorption and storage — keep Ablative Plating and Ironhide, and replace the Developing reflect with damage *storage* feeding Primal Carapace, or a "sclerite shed" that drops Chalybeate-style material on heavy hits (resource generation = morphling verb).
3. **Leeches Life Steal → re-role to banking.** Flat lifesteal is Blood Lust's set bonus. Leeches' Developing ability becomes **borrowed-blood banking**: feeding events (melee hits, kills) deposit into a small separate "borrowed blood" reserve rather than healing directly (see §6.1). Emergency Transfusion and Sanguine Frenzy stay — they are metabolism, not membrane.
4. **Urchin Spined Barricade passive → re-role the thorns component.** Keep the armor bonus small; convert the passive thorns into **spine harvest** — attackers embed/break spines, dropping collectible calcified material on a cooldown. The reflect *pulse* (Tidal Anchor) stays: periodic, not contact-reactive, so it does not share Barbed's trigger.

### 3.2 MEDIUM — stacking discipline (shared mechanics, not nerf whack-a-mole)

5. **One circulation for passive blood income.** Route all passive/reactive blood income (armor regen, morphling siphons, mask trickles, cradle leeching, routing trickle) through a single helper — working name `CirculationIncomeHelper` — with a per-second **circulation bandwidth** ceiling scaled by degree and the Capacity skill. Stacking income sources then yields diminishing returns instead of additive snowballing, and balance becomes one tunable number. `BloodVolumeEvents` and `BloodRoutingHelper` already centralize most of these paths, so this is a consolidation, not a rewrite. Theme: one blood, one circulation, finite flow.
6. **One "Last Rite."** All death-prevention effects (Ink Mantle Reprieve, Winter Shroud Cryptobiosis, Silent Archon refusal — the fungal-scar Split Husk having since been cut as redundant) share a single tag and a single armed slot or shared cooldown group. The most recently equipped source arms; the others read as dormant in their tooltips. Doctrine: *your blood may refuse the return only once.*
7. **Speed and toughness caps.** Keep collisions #6/#7 as-is mechanically, but clamp total bonus move/attack speed and toughness contributed by the triad (attribute-modifier group caps in `ArmorSetBonusHandler` + morphling effect application). Rename the Chitinite morphling's passive (e.g., *Sclerite Bulwark*) so the same effect name no longer appears in two systems.

### 3.3 Runway gaps inside single systems (not overlaps, but part of this audit)

8. **`CHARGED` manipulation type is registered but unused.** Either cut it from the enum or — preferred — reserve it as the **Magister rank identity** (see §5.1), so the empty D5 rank and the unused cast type solve each other.
9. **`MAGISTER` / `PERFECTUS` ranks are empty.** Degrees 5–6 currently gate systems (loadouts, covenants) rather than spell tiers. Intentional runway; the plan in §5.1 fills it without inflating the Humilis→Summa catalog.
10. **Morphling upkeep is dormant.** `morphlingPassiveDrainEnabled` / `morphlingDrainRate` / `morphlingDrainInterval` exist in config but every species returns `getBloodCost() = 0`. Do **not** enable flat drain — replace it with the hunger loop in §5.3. (`partial`: hooks exist.)
11. **Mind Spike** is documented as a legacy override scar; fold it into the scar-tuning cleanup pass rather than the triad.

---

## 4. Ownership Rules Going Forward

These are design laws. Every future ability, set bonus, or morphling stage should be sorted by them before authoring.

### 4.1 Trigger ownership

- **Armor owns contact:** when-struck, when-striking. No morphling or manipulation keys off attacker contact again.
- **Morphlings own ecology and state:** on-kill, while-in-terrain/biome, while-moving, while-fed/hungry, near-allies, near-wounded, over-time.
- **Manipulations own the cast:** the only system that *targets*, places world objects, or acts instantly at will.

### 4.2 Effect-type ownership

- **Armor only:** immunities, damage-type conversion, incorporeality, unconditional attribute floors.
- **Morphlings only:** resource *generation* from the living world (blood from feeding, enzymes from environments, materials from corpses/attackers), anatomical movement verbs (climb, glide, burrow, wall-cling).
- **Manipulations only:** burst damage/CC, placed constructs, targeted utility.
- **Scars:** modifiers on the above three — never new verbs.

### 4.3 Swap friction

Armor swaps cost an Armature visit; manipulation loadouts cost Reliquary/Distributor interaction. Morphlings currently hot-swap free, which prices their conditional kit at zero. Rule: **a newly equipped morphling arrives Hungry** (§5.3) and takes to the blood over ~60s before lending full power. Swapping stays legal; toolbelting mid-fight costs tempo.

### 4.4 Power budget

- **Armor:** ~40–50% of passive power. Unconditional, universal, committed.
- **Morphling:** ~25% unconditional floor, rising to ~35–40% **inside its context** (underground, submerged, in darkness, while Fed, near allies). The specialist slot.
- **Manipulations:** unlimited ceiling, always paid per cast from a finite pool. The skill-expression axis.
- **Cross-system synergy:** efficiency only (cost/cooldown), hard-capped near 20–25% total. Synergy makes a build smoother, never bigger.

---

## 5. Progression Differentiation

All three systems currently progress by "bring materials to a station" (Armature reagents, Loom catalysts, Incubator enzymes) — three parallel shopping lists. Differentiate the *nature* of growth:

### 5.1 Manipulations grow by use and rite (`partial`)

`ManipLevel` already tracks use; give it a visible payoff (small per-level efficiency or potency curve surfaced in the detail panel). Fill the empty ranks with identity, not inflation: **Magister (D5)** introduces `CHARGED` casts — held, blood-channeled greater forms of known Summa manipulations, unlocked by a forced rank-up rite (already on the deferred roadmap) rather than new loom recipes. **Perfectus (D6+)** stays reserved for covenant/Apotheos-scale expressions.

### 5.2 Armor grows by institution (`implemented`)

Degree gates, reagent forks, Consecration Kit, Monolithic Cornerstone, choice-gated Silent Archon. No changes; armor is the reference standard for committed progression.

### 5.3 Morphlings grow by husbandry (`planned` — the audit's biggest lever)

Enzymes remain the accelerant, but stage-ups gate behind **lived experience matching the organism's nature**: the Serpent matures by poisoning, the Mole by depth mined, the Tick by feeding on the wounded, the Fungal by nearby decomposition, the Spider by falls survived. A morphling is *raised* into Apex, not crafted.

Add the **hunger loop** on top, replacing dormant flat drain:

| State | Behavior |
|---|---|
| **Fed** | Full passive amplifier and reactive kit |
| **Hungry** | Reduced amplifier; reactive abilities intact; visible cue on the mutation layer |
| **Starving** | The symbiote feeds from *you*: small blood drain + Morphic Strain; eventually dormant (passive only) |

Feeding = blood, kills, or the species' preferred diet. This is the theme made literal — *yearns to feed from and lend to* — and it simultaneously provides the swap-friction rule (§4.3) and the power governor morphlings currently lack. Wild-bound capture, Bestiary recording, and the Apotheos nectar gate all stay as-is.

---

## 6. Synergy Design — Handshakes, Not Duplicates

Cross-system design should mean each system touching a **different phase of one loop**, never two systems providing the same effect.

### 6.1 The borrowed-blood loop (`planned`)

- **Morphling (income):** Leeches banks *borrowed blood* from feeding events into a small separate reserve.
- **Manipulation (expenditure):** casts may drain borrowed blood at a discount before touching the player's own volume.
- **Armor (exchange):** Blood Lust converts overkill lifesteal into the borrowed reserve instead of wasting it.

Three systems, one resource, zero shared triggers. Borrowed blood also gives Blood Drunkenness a natural sibling: emergency-drinking *foreign* blood is punished, while blood borrowed through the symbiote is the sanctioned path.

### 6.2 Triad resonance (`planned`)

All three systems already speak tendency (manipulation primaries/secondaries, morphling preferred/secondary, and the armor forks map cleanly: Barbed ≈ Ferric retaliation, Chitinite ≈ Ferric/Congeatio bastion, Prismatic ≈ Lux/Tenebris display). Formalize **one** Resonance state on the Sporitic Resonance template: when worn set, equipped morphling, and cast manipulation share a tendency, grant the capped efficiency bonus (§4.4). One shared mechanic replaces three parallel buff stacks; the 75/25 mixed-damage rule remains the counterweight that keeps mono-tendency from being strictly dominant.

### 6.3 The return (`planned`, endgame)

*"…and to one it shall return."* The bloodline pool, Founding Fane, and direct routing already exist as one-blood plumbing. The late-game synthesis is letting the triad touch the communal pool: the morphling may drink from the covenant pool when its host runs dry, armor may tithe overkill into it, manipulations may overdraw from it inside a fane. The three player systems become three organs of one shared body — the theme stated mechanically at exactly the point where the lore reveals the blood was never individually owned.

---

## 7. Forward Roadmap

Phased to match the alpha posture in [PUBLIC_ALPHA_READINESS.md](PUBLIC_ALPHA_READINESS.md): guardrails and honest labeling first, migrations second, new loops third.

### Phase 1 — Guardrails (low code risk)

- [ ] Adopt §4 ownership rules as the review checklist for new triad content.
- [x] Implement the circulation bandwidth helper and route Hematic Iron regen, Leeches siphon, Lodestone trickle, and cradle leeching through it. *(2026-07-03: `CirculationIncomeRules` + `CirculationIncomeHelper`; pure layer test-verified, dev-machine gradle gate pending — see the guardrails plan execution log.)*
- [x] Implement the shared Last Rite tag/cooldown across Ink Mantle, Winter Shroud Cryptobiosis, and Silent Archon refusal. *(2026-07-03: `LastRiteRules` + `LastRiteHelper` wired into Ink Mantle Reprieve, Last-Light Mantle, and Silent Archon refusal; Cryptobiosis joins when the morphling reframe lands.)*
- [ ] ~~Rename the Chitinite morphling passive so no effect name is shared across systems.~~ *Superseded — the morphling reframe cuts the Chitinite species outright.*

### Phase 2 — Collision migrations

- [ ] Cuttlefish: remove on-hit flash; add camouflage/ink-terrain re-role.
- [ ] Chitinite morphling: remove reflect; add storage/shed re-role.
- [ ] Leeches: convert Developing lifesteal into borrowed-blood banking.
- [ ] Urchin: convert passive thorns into spine harvest; keep Tidal Anchor pulse.
- [ ] Group caps for speed/toughness contributions.

### Phase 3 — Morphling identity

- [ ] Hunger states (Fed/Hungry/Starving) replacing dormant flat drain; equip-arrives-Hungry swap friction.
- [ ] Husbandry stage-up experiences per species (enzymes remain accelerant).
- [ ] Surface hunger and experience progress in the Bestiary tab and mutation layer.

### Phase 4 — Synergy loops

- [ ] Borrowed-blood reserve + Leeches/Blood Lust/manipulation handshake.
- [ ] Triad tendency Resonance (capped efficiency only).
- [ ] Endgame communal-pool interactions (fane overdraw, armor tithe, morphling covenant drinking).
- [ ] Magister `CHARGED` casts via forced rank-up rites; `ManipLevel` payoff curve.

Each phase should land with focused resource/source tests per repo convention and updates to HEMOMANCY_REFERENCE.md §8/§16/§22 and MORPHLING_REFERENCE.md.

---

## 8. Open Questions

1. **Decided 2026-07-04:** circulation bandwidth is visible in the Scrying Podium Blood Flow diagnostics, alongside positive/negative/net mL/t and per-source requested/applied rows.
2. Does the Cradle count as a second morphling "slot" against the power budget, or as covenant infrastructure outside it? Current lean: infrastructure — it already pays staged blood upkeep.
3. Do Saint Canon memories participate in triad Resonance, or stay outside it (they already ignore Dynamic Use)? Current lean: outside — imprinted, not aligned.
4. Should hunger apply to wild-bound (Developing-capped) morphlings, or only Mature+? Starting gentler on early-game players is probably right.
5. When scars get their tuning pass, do fungal scars keep their wildcard actives (Feeding Wake, Cryostroma's conserve) or migrate those toward morphling/armor lanes as well? Deferred until the triad migrations land.
