# Morphling Fungal-Strain Reframe — Design

> **Date:** 2026-07-02
> **Status:** Design / planned. No code or data changes yet.
> **Implementation plan:** [2026-07-03-morphling-fungal-strain-reframe.md](../plans/2026-07-03-morphling-fungal-strain-reframe.md)
> **Parent audit:** [POWER_SYSTEMS_AUDIT.md](../../POWER_SYSTEMS_AUDIT.md)
> **Sibling specs:** [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) · [Rogue Hemomancer Wills](2026-07-02-rogue-hemomancer-wills-design.md)
> **Current-state references:** [MORPHLING_REFERENCE.md](../../MORPHLING_REFERENCE.md), [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) §16

This is the third doc in a chain. The [Rogue Hemomancer Wills](2026-07-02-rogue-hemomancer-wills-design.md) spec introduces the ambusher enemy and its counter; that counter lives in the [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) spec; and both depend on the identity, material-economy, and naming rules established **here**, because morphlings and fungal scars are two expressions of the same organism.

---

## 1. Goal

Reduce morphlings from **12 species to 8** while (a) removing overlap with armor and manipulations per the parent audit, (b) making morphlings feel like *their own thing* rather than tamed copies of world mobs, and (c) fixing the material-economy collision where a morphling shares its source-creature with an armor set (e.g. Serpent morphling vs. Serpent-scale Prismatic armor).

## 2. The core reframe: strains, not animals

Morphlings are already, in lore, "special strains of the fungus," and wild polyps already express "biome-shaped appendage layers." Formalize that: **a morphling is not an animal — it is a fungal parasite-strain that has convergently evolved an animal-like solution.** It resembles an archetype (climber, feeder, digger) without *being* the creature.

This reframe is the spine of all three decisions below:

- **Convergent, not imitative.** A morphling "pulls the best of the species in its environment" rather than copying one animal — which is exactly the fantasy the strain framing produces.
- **Unifies with fungal scars.** A scar is the fungus grown *into* the host (fixed, internal); a morphling is the fungus grown *beside* the host (motile, fed, raised). Same Erythrocytic Mycelium, different growth habit — "all blood was once one and to one it shall return." See the [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) spec for the internal counterpart.
- **Preserves the polyp pipeline.** Wild polyp "layer families" become "which of the 8 strains this larva is expressing." Capture / Bestiary / Incubator / Nectar flows are unchanged; only the target identities become original fungal strains.

## 3. The double-overlap being fixed

Almost every current morphling collides with a world mob, an armor material animal, or both:

| Morphling | Collides with mob | Collides with armor material |
|---|---|---|
| Serpent | Scarlet Serpent | Prismatic (Serpent Scale) |
| Cuttlefish | Prism Cuttle | Prismatic (Cuttlefish Chromatophores) |
| Chitinite | Chitinite / Fervent Chitinite | Chitinite (whole set) |
| Urchin | Barbed Urchin | Barbed (Calcified Blood Spine) |
| Centipede | Venom-Rib Centipede | Barbed (Toxicognath) |
| Tick | Lantern Tick | — |
| Mole | Hematic Burrower (mole-like) | — |
| Spider | — | Prismatic (Puppeteering Thread ≈ web) |

Seven of twelve. Making morphlings original fungal strains dissolves all of these at once, and future-proofs against new mobs/armor colliding later.

## 4. Rule: the material-economy split

Because morphlings are fungal, the two systems draw from **entirely separate resource pools**, reinforcing the ownership split in the parent audit:

- **Armor is fed by hunting the world's fauna** — urchin spines, snail sclerites, serpent scales, scorpion telsons, termite mandibles, cuttle chromatophores. The membrane is built from *other creatures*.
- **Morphlings are fed by cultivating the infection** — enzymes, spore sacs, hyphal substrate, morphic nectar. The metabolism is grown from the *shared fungal body*.

**Design law:** *no morphling shares a source-creature or an ingredient with an armor set.* This is mostly already true in the data (the Armature takes mob-part reagents; the Incubator takes enzymes) — it becomes an enforced rule. The Serpent-scale problem disappears because strains have caps, cysts, cords, and spores, not scales/shells/spines to harvest.

## 5. Rule: the naming register (mod-wide guideline)

Name-blending across systems is a *register* problem, not a per-name problem. Assign each system one distinct linguistic register so the register itself is a wayfinding signal:

| System | Register | In-world voice | Example |
|---|---|---|---|
| Manipulations | Named arts (technique) | The Order's taught arts | Exsanguinate · Umbral Step |
| Cerebral scars | "Scar of the [X]" | Mythic / tarot | Scar of the Phoenix |
| Armor sets | Descriptive vow / material | Martial / craft | Barbed · Silent Archon |
| **Fungal scars** | **Pseudo-Latin binomial** | Scholarly — the Lodge catalogued them | *Thanomyces resurgens* |
| **Morphlings** | **Fungal folk-names** | Vernacular — named in the wild before the Order classified them | Foxfire · Bootlace |

**The split is diegetic and load-bearing.** The setting is ~1500 CE, pre-Linnaean. A binomial is the voice of a monk who *catalogued* the thing; a folk name is what a villager called a growth they met in the woods. Morphlings go maximally folk *so that* fungal scars can stay maximally Latin — the contrast becomes the signal: folk name = a strain you raise; Latin = a scar the Order refined. Folk names also reinforce the "best of the environment" theme, because folk names are observational and environmental where binomials abstract the environment away.

**Presentation:** the folk name is the item's display name (primary handle). The pseudo-Latin binomial survives only as a greyed tooltip subtitle and a Bestiary "classification" line — flavor for players who want it, invisible to those who don't. NPCs (Alchemist/Mnemonist) may use the binomial in dialogue to characterize the Order as the faction that abstracts the wild into taxonomy.

## 6. The consolidated roster of 8 (one living exemplar per tendency)

Eight tendencies, eight strains — one exemplar each. This makes duplication structurally impossible, powers the triad Resonance in the parent audit, and turns "which morphling" into "which blood you lean into."

| Tendency | Folk name | *(classification)* | Role (no other system's lane) | Converges on — but isn't |
|---|---|---|---|---|
| Animus | **Deadman's Purse** | *Sanguibursa vorax* | Blood economy / feeding | leech, mosquito |
| Mortem | **Gravecap** | *Necrophyta saprovex* | Corpse & decay resource engine | cordyceps (the "pure" one) |
| Ductilis | **Witch's Ear** | *Tympanospora susurra* | Recon / perception / reflex | bat, moth |
| Lux | **Foxfire** | *Ignisfatuus lucens* | Rescue / concealment / light | cuttlefish, anglerfish |
| Tenebris | **Bootlace** | *Rhizomorpha tenebra* | Traversal / cord-traps | spider, velvet worm |
| Ferric | **Irontooth** | *Ferrophyta lithovora* | Excavation / underground domain | mole, antlion |
| Flammeus | **Emberfang** | *Pyrrhiza digestans* | Priority-kill venom / attrition | assassin bug, spitting cobra |
| Congeatio | **Winter Shroud** | *Sporangia dormiens* | Survival / stasis / escape | tardigrade |

Two happy accidents worth keeping: **Foxfire** and **Bootlace** are real folk names for real fungi (bioluminescent foxfire; the black *rhizomorph* bootlace cords of Armillaria), so the register is genuine, not invented.

## 7. Cut / merge logic (12 → 8)

Cut four species whose kit is armor's job (contact-reactive defense) or duplicates a survivor; absorb their best parts:

| Cut | Why | Best part rescued into |
|---|---|---|
| **Chitinite** | Toughness/plating/thorns = Chitinite *armor* (same effect name) + Barbed set | — (armor owns this space) |
| **Urchin** | Reflect thorns = Barbed armor; Reefheart root+resist = armor lane | Knockback pulse → Winter Shroud; spine/material harvest → Irontooth |
| **Pests** | Not an animal; Swarm Retaliation is on-hit (armor lane) | Infest (kills spawn swarm) → Gravecap; area denial → Bootlace webs |
| **Tick** | Redundant blood-feeder; Mortem DoT overlaps Gravecap | Spreading bleed + feed-on-wounded → Deadman's Purse |

Reskin one in place: **Centipede → Winter Shroud (Tardigrade)** — cryptobiosis (real suspended-animation revival) is a straight upgrade for the Congeatio survival slot over "another armored bug."

## 8. Per-strain kept kit (passive → Developing → Mature → Apex → Primal)

All contact-reactive (on-hit) abilities removed per the audit; each strain keeps only ecology/state/resource/movement verbs.

- **Deadman's Purse (Animus).** Sanguine Siphon (routed through the circulation cap) → borrowed-blood banking (not flat lifesteal — that is Blood Lust armor's) → Blood Fever near wounded (from Tick) → Sanguine Frenzy execute → **Hemophage Covenant** (bloodline blood-share; the "return" primal).
- **Gravecap (Mortem).** Mycorrhizal Mending → Sporulation on-hurt field → Mycorrhizal Network ally heal → Cordyceps Burst + fungling spawn (Pests' Infest folded in) → **Primal Mycorrhiza** (elite kills seed communion patches, drop fungal-scar materials). **Owns "resource from kills"** — see §10.
- **Witch's Ear (Ductilis).** Echoic Perception (ambient passive sense) → Sonar Shriek → Membrane Glide → Nightwing Frenzy → **Echothesis** (active mass reveal through terrain).
- **Foxfire (Lux).** Luminous Dissipation → Chromatic Camouflage while stationary in water/darkness (replaces the cut on-hit flash) → Sepia Wake vision-denial terrain → Ink Mantle Reprieve (shared Last Rite slot) → **Last-Light Mantle** rescue.
- **Bootlace (Tenebris).** Wall Climbing → Silk Tether → Web Nest slowing terrain (Pests' area denial as webbing) → Web Cocoon → **Web of Red Thread** grapple.
- **Irontooth (Ferric).** Burrower's Instinct → Burrow Sense → Earthen Bulwark (state-gated to underground, not a contact wall) → Seismic Slam → **Deep Tremor Sense** (ore/cave/nectar-pool mapping) + Urchin's material-harvest on a cooldown.
- **Emberfang (Flammeus).** Serpentine Guile (speed, cap-grouped) → Venom Strike DoT → Constrict → Ambush Predator → **Sovereign Venom** (mark one target, escalate to paralysis then rupture; anti-elite scalpel).
- **Winter Shroud (Congeatio).** Cryptobiotic Hide (self-state resistance floor) → Tun Plating rest-Absorption → Anhydrobiosis cleanse → Tun Molt decoy-husk escape (Centipede's molt merged) + Urchin knockback pulse → **Cryptobiosis** stasis-invulnerability revival (shared Last Rite slot with Foxfire).

## 9. Mutation-layer silhouettes

Each strain needs a distinct attachment for the existing morphling mutation render system (HEAD/BODY/ARMS/LEGS, staged by maturity):

| Strain | Anchor | Silhouette |
|---|---|---|
| Deadman's Purse | BODY | translucent blood-bladder that swells as you feed |
| Gravecap | HEAD | mushroom cap + mycelial mantle *(reuse existing fungal-head asset)* |
| Witch's Ear | HEAD | twitching frilled gill-antennae crest |
| Foxfire | BODY | emissive color-shifting gills/spots |
| Bootlace | BODY | back-node sprouting reaching black rhizomorph cords |
| Irontooth | ARMS | iron-crusted crystalline digging claws |
| Emberfang | ARMS | forearm proboscis-lance that glows hot when charged |
| Winter Shroud | BODY | segmented desiccated shell that seals into a spore-pod |

## 10. Cross-spec decision: "resource from kills"

On-kill resource generation currently belongs to *both* a fungal scar (Sanguiflora cadens) and morphlings (Gravecap / Deadman's Purse). **Resolution: morphlings own it.** Sanguiflora is relocated out of the fungal-scar roster and its fantasy consolidated into Gravecap's Cordyceps line. See the [Fungal Scar Consolidation](2026-07-02-fungal-scar-consolidation-design.md) spec §"cut list" for the reciprocal decision.

## 11. Scope, migration, and open questions

- **Rename map (planned):** `morphling_serpent` → `morphling_emberfang`, `morphling_cuttlefish` → `morphling_foxfire`, `morphling_mole` → `morphling_irontooth`, `morphling_leeches` → `morphling_deadmans_purse`, `morphling_bat` → `morphling_witchs_ear`, `morphling_spider` → `morphling_bootlace`, `morphling_fungal` → `morphling_gravecap`, `morphling_centipede` → `morphling_winter_shroud`. Removed: `morphling_chitinite`, `morphling_urchin`, `morphling_pests`, `morphling_tick`. Provide data-fixer/alias handling for existing saves.
- **Assets:** audit which existing morphling models/textures can be retinted vs. need new geometry for the new silhouettes.
- **Open — hunger/husbandry:** the audit's Fed/Hungry/Starving loop and per-species husbandry stage-ups (§Progression) should land alongside this reskin, not after, so the new identities ship with their intended progression.
- **Open — does hunger apply to wild-bound (Developing-capped) morphlings, or only Mature+?** Lean gentler early.
- **Testing:** focused resource/source tests for the 8 registrations, rename aliases, incubator recipe retargeting, and Bestiary classification-line rendering; update [MORPHLING_REFERENCE.md](../../MORPHLING_REFERENCE.md) and [HEMOMANCY_REFERENCE.md](../../HEMOMANCY_REFERENCE.md) §16.
