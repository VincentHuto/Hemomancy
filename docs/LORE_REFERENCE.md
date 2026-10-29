# Hemomancy — Lore Reference

## Clinical knowledge jurisdiction

The Alchemist teaches blood as material and specimen: sampling, physiological classification, microscopy, injection, preservation, and laboratory storage. The Artificer converts established laboratory practice into durable field apparatus after an Alchemist referral. The Mnemonist receives the Alchemist's unexplained auditory observations and teaches retained expression through the Clairaudiograph at Degree 3. Construction alone does not determine knowledge ownership. See [Clinical Blood Tools](../wiki/Clinical-Blood-Tools.md).

### Alchemist station teaching: separation at D4/D6

The Alchemist recognizes practiced work rather than conferring knowledge through a gift. The D4 Calibrated Rotor teaches steady recovery: material previously lost during a rough spin can be retained. The D6 Fractionating Rotor teaches attention to the remainder: a mixed specimen can yield another fraction it already contains. This extends the same blood-as-specimen jurisdiction as the Alembic, whose Condenser and Athanor upgrades now sit at D4 and D6. All use the shared Cardinal ceremonial grammar and optional station-project progression; the Artificer's apparatus assignments and degree-promotion proofs remain separate.

The other station disciplines retain their own voices within that shared ceremony: the Artificer teaches resonance and vestment apparatus, and the Mnemonist teaches recollection through the Scriptorium. Crafted or gifted equipment cannot supply personal learning. See [the shared station contract](STATION_UPGRADES.md).

## Current narrative synchronization (2026-10-03)

The [source/history audit](validation/REFERENCE_SYNC_2026-10-03.md) covers the latest substantial reference commit on October 1 and checks coverage back to the September 19 broad audit. Recent repairs clarify personal practice, shared belonging, scar preparation and the two ending states; they do not introduce another faction, tendency or degree ladder. The current assignment ledger recognizes either completed Archon path while retaining Apotheos's unresolved decision as pending. Interface completion is a record of the existing ending, not another ascension.

Mnemonic Whales remain living archives that Voyagers collect from without killing. Their predation on squid and Prism Cuttles now supports repeated ambergris collection through live prey. Nonlethal refers to treatment of the whale; it does not imply prey survive. Sampling and shedding remain distinct collection methods. This ecology establishes no new doctrine for the Brined Votary or other reef inhabitants.

Dynasty Castles and the Fargone's Scout, Attendant, Guard, Elder and Reclaimed variants are present world content. Their authored populations do not establish an additional Harbinger faction, player rank system or complete history of the castle builders. Appearance and population labels alone must not be expanded into settled political canon.

> This document covers **lore only**: world history, cosmic origins, factions, characters, beliefs, mythology, and narrative themes. For mechanics, systems, and code details see [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md).
>
> **Last Updated:** 2026-10-03 lore/reference synchronization audit. LORE_REFERENCE.md is the authority for canon. `HEMOMANCY_REFERENCE.md` describes the mechanics that implement it. Game code, dialogue, inquiries, item text, advancements, and wiki pages are downstream and must be corrected when they disagree with these documents.

## Canon Governance

This file and the settled decisions incorporated into it are the canonical narrative source. `HEMOMANCY_REFERENCE.md` is the canonical mechanics specification, but it may not redefine the lore. The wiki is a downstream presentation of these docs, never an independent authority. Superseded design notes and implementation plans are historical records only.

The following settled facts are non-negotiable consistency anchors:

- Annetta's canonical creature is the Tooth Peck. Tooth Pecks seek active infection, but Annetta never understands what they detect in her. Chthonians remain a separate termite ecology and have no role in the encounter.
- Verdigris Aura is the early Purity defense; Silver Ward is its advanced Clarity form.
- The ninth and final Qliphoth Pome grants the Fungal Spine. The first Fungal Spine visit is a two-minute consciousness projection: the player can move and run but has no accessible items or armor, and an accelerating red vignette warns of forced return to the physical body.
- After that forced return, the eighth Degree splits into two equal ends. The Rite of Apotheosis makes the player Degree 8 Apotheos. The Rite of Pruning, victory over Vesper and absorbing his memory make the player Degree 8 Silentium (colloquially, a silent archon).
- The ordinary Unstained cure closes when a Harbinger founds a bloodline. Reaching Degree 5 or joining another bloodline does not close it. Defeating the infection unleashed by Annetta's fatal Harbinger encounter unlocks an exceptional severance route for a founder.
- A bloodline is founded only by the Degree-5 Bloodline Founding rite. An Unsigned Ledger records or joins a covenant; it does not create one by itself.
- Pale Silver is refined only from Consecrated Copper. It is never a natural metal and is never refined directly from iron.
- The Saints' tendency pairs are fixed: Hemorath is Animus and Ferric, Seraphae is Lux and Ductilis, Putriciel is Mortem and Flammeus, and Velorum is Congeatio and Tenebris. Every Canon Memory takes its Saint's pair.
- Hemorath was one of the first and most powerful Hemomancers, but never a true Harbinger and never one of the Order's Archons. Erythravane was the first Vicar and the first Archon. No Saint can be killed: every encounter returns the Saint to its tomb.
- Degree 7 is Archon for everyone, a full stage rather than a stepping stone. The Fungal Gardens dimension is reached in the body only by an Apotheos, by a Silentium only through possession, and by a Degree-7 Archon only once, in the first forced projection.
- **Severance** is the player's own act: cutting their Qliphoth tree in the Rite of Pruning. It is an ego death and a cut with the hive, not a place.
- Eight canonical Morphling strains are implemented. Older references to twelve describe superseded prototypes.

> **Current Lore-State Snapshot (2026-10-03 audit):**
> - Core narrative pillars remain directly represented in gameplay: Harbinger initiation/degrees (now including the explicit Apotheos gate), Unstained purification path, faction NPC dialogue trees, blood-memory framing, and fungal-whisper escalation.
> - **Memory Weaving now reads as a physical Somatic Loom rite:** blank Hematic Memories, catalyst patterns, enzyme-dye reservoirs, projected blood, and an engram of recalled fragments threaded home all make refined manipulation learning feel like wrestling dormant biology into a chosen shape rather than selecting a recipe from a menu.
> - Qliphoth Communion now has a clearer in-game rhythm: the Sanguine Monolith breaks open with a black void-bloom, the Qliphoth Tree drops nine named husks with personal whispers, and an Archon who chooses silence must turn their Living Arsenal into the sickle that wounds it.
> - Blood Moons now visibly match their lore state in the sky: the red lunar face and fungal-vein overlay express a fungal surge, while pale effects represent the Lady's containment response.
> - **The Chamber of Will is now the Degree 6 inward refuge:** not the external Fungal Dimension, but a private blood-memory chamber whose space and sky react to Archon, Qliphoth, Silent Archon, and Apotheos progression.
> - **Vesper and The Mycophant now have implemented endgame encounters:** Vesper is the two-phase boss for Archons who refuse fungal dissolution, while The Mycophant stalks Apotheos-aligned Harbingers through the Fungal Gardens before claiming them for the Morphic Nursery.
> - **Annetta Knowles encounter is partially complete:** the two-route encounter is wired and playable, dedicated Java models/textures are present, and Annetta's Sanguis Lancea has custom held/item rendering. Final animation polish, fuller Phase 1 biological combat identity, and Annetta-specific thrown projectile rendering remain WIP.
> - Several high-impact lore arcs remain partial in gameplay implementation: broader Fungal Dimension progression, the per-Saint trial chambers (only one generic mausoleum exists, and it does not generate), each Saint's planned combat overhaul, and fuller boss artwork.
> - Covenant social structure gained stricter in-game expression: bloodline leadership can now ritually sever members, reinforcing the Order's "chosen-family covenant" framing over biological lineage.
> - Harbinger outpost recruitment now reflects covenant restraint: a player's bloodline can draw one pledged voice from an outpost and one of each calling, supporting the Order's doctrine of spreading the cause rather than absorbing an entire lodge.
> - Cerebral scars now read more like internalized mind-maps than worn trinkets: Scar items are burned into memory, Mason's Effigy prepares known scars into a motif, projected blood writes the motif into a dynamic Scar Pattern, and an empty, lit Iron Brazier burns that pattern to set the active scar loadout.
> - **Cardinal Rites now have a shared bodily liturgy:** an authored ritual floor establishes the grammar, a Cardinal Focus holds the rite's medium, lit braziers declare offerings, and a planted Living Staff becomes the temporary vascular axis around which the rite manifests.
> - **Mnemonic Blueprints replace disposable hint sheets:** an unlocked rite or blood structure can be impressed into a reusable mnemonic plan, examined as an authored diagram, and projected into the world as a construction memory. Mnemonic Folios preserve collections of those plans.
> - **Puppetry unlocks are now Cardinal Ordeals:** six artificial bodies share the Dominion Lesser floor, preserve the owner's attuned Crossbar as their medium, and become known only after the manifested puppet itself is defeated.
> - This document remains canonical for worldbuilding. [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) records how that canon is implemented and must be updated when the implementation changes.

---

## Table of Contents

1. [World Overview & Timeline](#1-world-overview--timeline)
2. [The Cosmic Origin — The Fungal Entity](#2-the-cosmic-origin--the-fungal-entity)
3. [The Fungal Dimension (The Flesh Beyond)](#3-the-fungal-dimension-the-flesh-beyond)
4. [Blood Magic — What It Actually Is](#4-blood-magic--what-it-actually-is)
5. [Two Ways to Become Infected](#5-two-ways-to-become-infected)
6. [The Harbingers (Hematic Order)](#6-the-harbingers-hematic-order)
7. [The Unstained](#7-the-unstained)
8. [Our Lady of Still Waters (The Pale Lady)](#8-our-lady-of-still-waters-the-pale-lady)
9. [Blood Moons — Cosmological Significance](#9-blood-moons--cosmological-significance)
10. [The Saints](#10-the-saints)
11. [The Stained Priestess — Annetta Knowles](#11-the-stained-priestess--annetta-knowles)
12. [The Qliphoth — Tree of Death](#12-the-qliphoth--tree-of-death)
13. [Infection Biology & Society](#13-infection-biology--society)
14. [Material Philosophy](#14-material-philosophy)
15. [Flora of the World](#15-flora-of-the-world)
16. [Arthropods as Natural Hemomancers](#16-arthropods-as-natural-hemomancers)
17. [Moral Ambiguity — The Mod's Tone](#17-moral-ambiguity--the-mods-tone)

---

## 1. World Overview & Timeline

Hemomancy is set in the Minecraft world at approximately **1500 CE** by real-world reckoning — the cusp of the Late Medieval and Early Modern period, a time when full plate armor was common, the first cannons existed, alchemy was a serious pursuit, and the world still held a great many mysteries.

The **Erythrocytic Mycelium** (blood-cell fungus) has been a part of this world for roughly **800 years**, originating around 700–900 CE. To the common folk of the world it is simply another piece of nature — strange, somewhat unsettling, but no more remarkable than any other dangerous wilderness. It does not spread rapidly or uncontrollably. It does not devour the world. Entire regions bear no trace of it. Those who encounter it are more likely to find it merely eerie than genuinely threatening.

**Minecraft's common people view the infection with discomfort at best, fear at worst** — not because it is actually dangerous to them, but because things touched by it are unfamiliar, and the blood mages who emerge from it are shunned as taboo. Much like vultures, fungi are the cleanup crew of nature. That they consume the dead does not make them evil.

---

## 2. The Cosmic Origin — The Fungal Entity

At the root of everything in this mod is a single, incomprehensible being: **a fourth-dimensional organism** so vast and alien that it exists largely beyond the perception of three-dimensional creatures.

### 2.1 Its Nature

The Fungal Entity does not think the way living things think. It is a **hive-mind**, a distributed consciousness spread across multiple dimensions and possibly across multiple universes. What we experience as the "meatball" seen in the Fungal Dimension — the great sphere of meat, flesh, and hyphae tendrils — is merely a three-dimensional **sliver** of something far larger poking into our reality.

Think of it this way: just as a sphere passing through a flat two-dimensional plane appears to a 2D observer as a circle that grows and shrinks, the Fungal Entity passes through three-dimensional space as a localized chunk of biology. The full creature extends infinitely in directions we cannot perceive. This is why:
- It is not everywhere in the world, despite having been here for 800 years
- It can apparently "retract" and vanish
- Its spores affect the world without the core being physically present
- Encountering it directly seems limitless in power from our perspective, even though it actually has limits

**It is not God.** It is a deity in an infinite universe — powerful, incomprehensible, old, but not omnipotent. Think of a pagan god versus an archangel in scale of being — the Pale Lady is the pagan god; the Fungal Entity is something even beyond that.

The fungal voice does not knowingly lie. It is nevertheless wholly self-serving: it states observations through the scale and values of the mycelium, omits what does not serve proliferation, and presents its interpretation as if no other interpretation mattered. Its words may mislead through framing, but literal factual contradictions must be rewritten rather than excused as deception.

### 2.2 How It Infects a World

The Entity extends **hyphae tendrils** across the surface of whichever world it has "landed" on. These tendrils reach upward; the bulb-shaped nodes at their tips eventually break free of gravity and drift off as **spores**, spreading pockets of Erythrocytic Mycelium across the land. The infection zones do not multiply exponentially. They are slow, deliberate, geologically patient.

The Entity's true purpose appears to be **reproduction**: the Hematic Order it catalyzed is not a human institution. Each degree of initiation is a stage of sporulation. Each rite triggers the next bloom. The Harbingers, in ascending their ranks, are not discovering blood magic — they are becoming fruiting bodies for a vast mycelial network. This is the truth delivered by the Fungal Whispers at the highest degrees, and it is the truth the Hematic Order does not know.

> *"The Hematic Order was never a human creation. It is a reproductive strategy."*

### 2.3 The Entity and Mod Compatibility

The Entity's existence is not entirely self-contained. If a player aligned with the Harbingers also practices other forms of magic (e.g., Mana and Artifice), and those spells happen to align with their blood tendency, there are **compounding resonances** — arcane synergy between blood and mana. This is not coincidental. The Entity's influence bleeds into adjacent magic systems.

---

## 3. The Fungal Dimension (The Flesh Beyond)

At Archon (Degree 7), consuming the ninth and final Qliphoth Pome causes a **Fungal Spine** to tear free from the player's back. Its first use projects only their consciousness into what appears to be a separate dimension; neither Apotheosis nor Pruning has happened yet.

### 3.1 What It Is

The Fungal Gardens are a **projection**. The higher-dimensional Entity (§2.1) is reaching down into real space, and the dimension is the three-dimensional shape of that touch, cast into the space above the player's world. The dimension does not contain the being. It is the being's cross-section where it meets ours, which is why the world and moon hang in its sky. The Overworld's Fungal Gardens biome is a different place: an infection zone on the ground that anyone can walk.

On the first visit the player is **not physically transported**. Their body remains in the world. What happens is closer to an **astral projection** or a forced expansion of consciousness — the fungal infection within them is now advanced enough to let them perceive the Entity's local "surface." They have no body in this place. If they look down, there is nothing there.

That first projection is the only visit a Degree-7 Archon ever makes. Afterwards the paths part. An **Apotheos** travels in the flesh, at will, through the Spine or a Fungal Podium: the Gardens treat that body as part of the organism. A **Silentium** never goes bodily. They force their will into one of the Gardens' own facsimiles through the Rite of Possession and are thrown out, Mind Broken, if the organism breaks their concentration. Unstained and unaligned players never enter.

What they encounter:
- A vast **spherical mass of flesh, meat, and pulsing biology**
- Enormous **hyphae tendrils** arcing upward into a sky where the world and moon are visible, as though this space exists just above the Earth
- A low, uneven living floor where exposed hemorrhagic crust has grown over with erythrocytic soil, calcified hyphae, and mycelial mats rather than lying as bare stone
- Bulb-nodes on the tendrils that eventually break off like seeds, falling back toward the world as spores
- Alien, bizarre creatures that exist nowhere else — beings that are not infected Earth-life but genuinely alien life forms native to the Entity's substrate
- **Facsimiles**: the organism's own attempts to rebuild the Earth creatures it consumed (bats, wolves, golems and more), gotten grossly wrong. They are not infected animals either. The hive keeps what it digests, and "studies" is the closest human word for what it does with it; its reasons are not human reasons.
- **Constant Fungal Whispers**, nearly harassing in frequency, saying things like *"This is what you wanted. This is what you were working toward. Is it not?"*

Everything here is hostile, even to an Archon. The player is a babe in the woods.

### 3.2 Return and Choice

The first visit lasts roughly two minutes. The projection can move and run, but carries no accessible inventory, armor, or physical equipment. An increasingly rapid red vignette is the body's warning; when time expires, consciousness is forced back without item loss. Later visits use the Spine as a normal return-thread after the player has answered the revelation.

Upon forced return, the player faces a choice between two equal ends of the eighth Degree. They can perform the **Rite of Apotheosis** and become **Apotheos**, letting the hivemind in. Or they can choose silence: cut their own Bloom with the Living Sickle in the **Rite of Pruning**, defeat Vesper, and take the Memory of Vesper in by force to become **Silentium** (colloquially, a silent archon). Both ascend by taking something in. This choice is the mod's deepest opt-in.

### 3.3 Transcendence (Late/DLC Content)

The rarest and most extreme endpoint — performing small fungal rituals that strip away what remains of the player's physical form and merge them with the hive mind. This is envisioned as expansion/DLC content, not expected to be reached by most players.

---

## 4. Blood Magic — What It Actually Is

Blood manipulations — the spells and powers of a Hemomancer — are not truly learned. They are **remembered**.

Everyone carries dormant **blood memories** deep in their biology, inherited from the infection's long history in the world. These memories are potential, not active — they cannot be accessed without the infection fully taking hold. Once awakened through the Mortal Display and formally received by the Vicar, these memories can be drawn out, shaped, and activated through a process called **Memory Weaving**.

Memory Weaving is not ordinary crafting. A blank Hematic Memory is placed into the Somatic Loom as a vessel, while catalyst items act like witnesses, anchors, or symbolic hooks that tell the infection which dormant instruction to expose. Enzymes are no longer just present or absent; the loom stores them as internal, unrecoverable dye-like reservoirs keyed to the eight tendencies. When the correct pattern is present, the player must project blood into the loom before the memory can be physically wrestled into form.

The visible rite is a recollection, not a fight. Paid blood unfurls an engram around the loom: an eight-pointed star of empty vessels sunk in living flesh, with the memory's fragments waiting at its points, each a piece of what some Harbinger before the practitioner once knew, surfacing from the bloodline's collective blood-memory. The Harbinger threads each fragment home along the star's lines with the Living Staff, and above the warp the echo of that past Harbinger remembers the memory beat by beat until it crystallizes. The blood keeps its failures as faithfully as its lessons: false fragments from failed Harbingers surface among the true ones, recognisable by the wrong memory they show and by beating against the loom instead of with it. A bloodline that has recalled a memory before remembers which fragments lie.

A manipulation is therefore not a clean spell laid over the world. The infection makes the remembered instruction out of the practitioner's own blood. It gathers in beads, ligaments, veins, plates, films, or vapor; takes on the tendency's material character; and then loosens, flakes, drains, or recoils when the instruction ends. Even effects that resemble light, darkness, fire, ice, iron, or lightning remain expressions of a living sanguine medium rather than eight independent elements.

The tendencies describe recurring ways that medium behaves. **Animus** circulates as living crimson flow. **Mortem** dries, fissures, sludges, and eventually fruits into pale fungal contamination. **Ferric** fixes blood into weight-bearing iron, while **Ductilis** carries impulse through nerves, water, and conductive metal. **Flammeus** boils blood into flame, smoke, molten fluid, and thin sanguine glass. **Congeatio** arrests it as dark cruor under pale frost; structures raised by cold memories are **Frozen Cruor**, temporary blood-ice rather than ordinary winter ice. **Lux** stretches it into open white radiance and fine luminous threads. **Tenebris** thins it into liquid darkness with torn, smoky edges.

These forms also explain why manipulation consequences cling to bodies and places. An Iron Heart sits against the practitioner's chest instead of existing as an abstract reserve. Binding and suture remain attached to their participants. Ductilis paralysis is the hostile interruption of voluntary nerve signals, not the suspension of gravity or awareness. A Ferric wall can bear impact, a conducted current can follow connected water or iron, and the residue of a completed working visibly remembers where the blood was made to act.

The Order uses **Mnemonic Blueprints** for a related but distinct act of memory. A Blueprint does not teach a manipulation and does not contain the rite itself. It fixes the remembered spatial relationships of a known Cardinal Rite or blood structure into a portable plan. Reading it recalls the authored diagram; pressing it against the world projects that memory over real blocks so the practitioner can compare intention with construction. A **Mnemonic Folio** is an archive of these spatial memories, not an additional source of secret knowledge: it can preserve plans the bearer has legitimately received or imprinted, but it cannot make an unknown rite known.

**This is the truth the Harbingers believe**: that blood magic is simply a gift they were born carrying, a sacred inheritance that should not be wasted.

**This is the real truth**: the "memories" are instructions left by the fungus, encoded into blood over generations to make infected hosts more useful, more dangerous, and more capable of spreading the infection further.

---

## 5. Two Ways to Become Infected

### 5.1 Natural Infection (Lore-Only, Not a Player Mechanic)

In the world's lore, a rare few people become naturally infected without ever meeting a Harbinger. This happens through:
- Breathing in spores near an Erythrocytic Mycelium biome
- Consuming infected meat (e.g., flesh from creatures who lived in the infected zones)
- In theory, eating infected mushroom meat or mushroom stew from a mooshroom in the infected biome

The infection does not immediately manifest. It can lie **dormant for an entire lifetime**, suppressed by an intact will and an unshocked mind. The original Saints of the blood are believed to have been naturally infected in this way — the formal Hermit Heart method came later, after Harbingers learned to pass on a concentrated dose deliberately.

Dormant infection can be triggered by **extreme stress** — a severe shock that weakens the mental barriers enough for the waiting power to flood in all at once. This is presented analogously to how some latent conditions in the human body awaken under trauma.

> *Note: Natural infection is not implemented as a player-accessible mechanic. The mod is fully opt-in. This is lore background only.*

### 5.2 The Harbinger Hermit — The Mortal Display Method

The standard (and only player-accessible) way to become a Hemomancer is through the **Harbinger Hermit** and the **Blood Temple**. This is both a gameplay mechanic and a rich piece of lore.

**The Hermit's Tradition:**

Harbinger Hermits are Hemomancers who have grown old and know their time is drawing short. Rather than retire quietly, they undertake one final sacred act — they **travel alone** to a remote location, build a small hut or outpost, and create what is called a **Mortal Display**.

The Mortal Display is a ritual pedestal upon which the Hermit **removes their own heart** and places it. While their heart beats on the pedestal, they themselves are in a strange state of suspended mortality — essentially immortal and unable to wander far, but still present, still conscious, still able to speak. They become the eternal keeper of that Display, waiting for someone worthy to come along.

When a worthy person (the player) arrives and claims the heart from the Display, they receive:
- A **living adornment** — a necklace/charm attuned to the Hermit's blood
- The Hermit's final words and an **outpost waybill** directing them to the Vicar
- The activation of their own latent blood magic

The Hermit then dies — the heart, now given, can no longer sustain them. His last recorded words reflect the tradition:

> *"Heart was never mine to keep, only to give... Walk the crimson path, young Harbinger. I will go now where blood returns to silence."*

This is a deeply personal, singular act. Each Blood Temple in the world belongs to one specific Hermit. The Hermit is **invulnerable** until the player chooses to take the heart (the "Farewell" dialogue option).

---

## 6. The Harbingers (Hematic Order)

### 6.1 What They Believe

The Hematic Order does not view blood magic as dark, evil, or taboo. To them, it is a **sacred essence that is a part of life itself** — a gift their blood carries, passed down and refined through the centuries. They believe it would be sacrilegious and wasteful not to use and share it.

They do not worship a blood god. There is no deity at the center of the Order's faith. Blood is simply seen as the most fundamental material in existence — the carrier of life, memory, and identity. To master it is to master the most basic truth of being alive.

**They are not evil.** They are shunned, taboo, misunderstood — the way anything unfamiliar and vaguely body-horror-adjacent tends to be. There have been incidents: a Harbinger losing control in a fight, blood magic being used in anger or defense, accidents from latent powers awakening under stress. These incidents create fear. The Order lives with that reputation.

### 6.2 Culture and Appearance

Harbingers favor:
- **Lighter armor** — cloth, natural materials, beetle-shell armor (Hematic Iron chitin)
- **Natural materials** — things the body produces or modifies (iron fused with blood, chitin, bone)
- **Blood manipulations as their primary defensive and offensive tool** — hardening blood into barriers, using iron-tendency powers, the living weapons they carry

They do not view themselves as recluses by choice. The Hematic Order is, at its best, a community — a found family. Joining the Order typically means leaving behind one's biological family, not because it's required, but because the lifestyle of a Harbinger makes it impractical to maintain typical family relationships. The Order provides a new family.

### 6.2a Hematic Memory: Mneme, Nous, and Thelema

**Hematic Memory** is the Order's broad name for repeatable instruction preserved in blood. It has two expressions, which describe how a remembered pattern becomes action rather than what tendency, rank, body section, or moral path it belongs to.

- A **Noetic Memory** is apprehended as an intelligible pattern and enacted through a blood manipulation. It is the Mnemonist's usual field: a remembered form which the practitioner understands well enough to project.
- A **Thelemic Memory** is enacted as embodied will. The body learns a repeatable answer through tincture preparation, holds a finite reserve of that preparation, and answers through a vascular section when the learned action becomes useful.

Some Noetic Memories are not isolated tricks but families of one remembered grammar. The first form teaches the pattern; deeper forms are more exact, larger, or more forceful uses of that same understanding. The Order treats mastery as cumulative rather than asking a practitioner to relearn the first lesson for every refinement.

The specialist mnemonic is: **Mneme keeps. Nous knows. Thelēma acts.** Memory preserves; intellect apprehends; will enacts. This quietly echoes the older `memoria–intelligentia–voluntas` structure and its Greek vocabulary, but the Order claims no historical founder or secret lineage for it. Routine controls use the simple Noetic/Thelemic terms and do not lecture the player about later philosophical descendants.

Blood tendencies remain recurring Harbinger classifications: maps which often predict materials, tissues, Morphling resonance, and mnemonic behavior. They are not eight cosmic elements. Expression, tendency, rank, vascular section, and acquisition route are independent axes.

The Alchemist reaches Thelemic teaching through reproducible reaction, tissue, enzymes, and physiological correlation. The Mnemonist reaches Noetic teaching through apprehension, interpretation, and Loom practice. They respect each other's successful predictions while remaining professionally irritated by each other's language. Conductive Mark is the clearest shared example: a Ductilis tissue record and a successful weave disclose one existing Noetic Memory by different routes.

The fungal revelation does not make Harbinger scholarship worthless. Its maps remain predictively useful even after their alien provenance is known. Early embodied and later projected powers remain complementary; true opposition is reserved for the endgame. **Voluntas Sola** names the Silent Archon's inversion of embodied will into sovereign separation and belongs only to that late material.

### 6.3 The Harbinger Outpost and Its NPCs

Once players advance past the Blood Temple, they are directed to **Harbinger Outposts** — multi-story structures that serve as the Order's visible presence in the world.

Each outpost contains:
- A **Harbinger Vicar** — the outpost's lore-keeper and rank-progression guide. The Vicar guides players from Degree 1 through Degree 5, then steps back as an equal once the player reaches the Crimson Lodge. He delivers faction history, doctrinal lore, and the secret of the 8th Degree at Archon.
- A **Harbinger Alchemist** — found at the crafting stations, teaches how to use the mod's machines (Alembic, Centrifuge, Somatic Loom, etc.).
- A **Harbinger Mnemonist** - teaches crude memories, manipulation slots, Mnemonic Reliquary practice, and the Somatic Loom as memory work rather than living equipment.
- A **Hematic Artificer / Redwright** - a maker-priest of living gear. He briefs and inspects five optional equipment assignments, permanently records the first fork and Archon armor lineages, and demands field proof that the finished work actually answers before granting a fitting. The Alchemist supplies the chemical materials after two formal referrals; the Artificer remains responsible for frame, fit, and external reinforcement. His staff fittings mark witnessed obligations rather than new powers. He is part of ordinary outpost craft, not a hidden ascetic calling.
- Generic **chests and loot** from the Order's stores.

The **Cicatrix Anchorite**, commonly called the **Vein-Mason**, is deliberately not part of these outposts. They keep a remote hermitage at Masons Respite: stone cell, ash circles, vessel diagrams, scar-pattern lore, Mason's Effigy, and Iron Brazier arranged as a teaching place rather than a lodge. A Vicar sends Adepts there when ordinary outpost instruction is no longer enough. The Anchorite's teaching is ascetic and practical: the player is handed an existing low-tier scar pattern that matches their strongest blood tendency, then coached through carving the scar, burning it into memory, preparing an Effigy motif, and committing a first scar loadout. Only after that full cycle does the Anchorite offer a second pattern, teaching continuation rather than hoarding.

When an advanced Harbinger asks an outpost member to pledge blood into their covenant, the outpost does not empty itself into that one bloodline. Each outpost may offer only one voice to a player's covenant, and each calling should remain distinct. This is framed as a doctrine of propagation rather than conquest: *spread our cause, do not dilute or absorb the whole lodge.*

> *Vicar's Secret (Archon only):* "The Hematic Order never had seven degrees. There have always been eight. The eighth degree is silence."

### 6.3a Cardinal Rites as Bodily Liturgy

Cardinal Rites are not ordinary multiblock recipes enlarged for spectacle. They are the formal grammar by which the Hematic Order persuades an infection, a place, and a practitioner to behave as one body.

- The **ritual floor** is the rite's grammar: a reusable diagram that declares the scale and family of the work.
- The **Cardinal Focus** is its temporary heart. The seated medium identifies what the rite is acting through or upon; preserving a medium means the rite changes its relationship to the bearer without consuming the object itself. Living Staffs are the common degree-rite medium, but specialized rites may preserve another relationship-bearing object such as an attuned Crossbar; early initiation instead belongs to the Hermit, Vicar, and Alchemist.
- **Lit braziers** are deliberate mouths around the body of the rite. Their offerings are absorbed only when the manifested work has successfully begun, so a failed attempt before manifestation is not mistaken for a completed sacrifice.
- When a **Living Staff** is the declared medium, it is planted rather than merely waved. For the duration of the rite it becomes a vascular axis, holding the practitioner's claim, the emerging daemon, and the route by which offerings and power return to the caster.
- **Anchors, sockets, sigils, boundaries, fog, and ordeal waves** are expressions of the rite becoming anatomically coherent. Breaking the authored station tears that temporary body apart; cancellation is an intentional reabsorption, not a clean refund.

The simpler rites keep this anatomy restrained. Greater and Grand rites make it visible enough to become an ordeal in its own right. The shared form is why one learned ritual floor can support multiple rites without making those rites interchangeable: floor, medium, offerings, ceremony profile, and intended transformation all carry separate meaning.

The Order sorts its liturgy by what a rite asks of the body. **Threshold** floors carry rites that change the practitioner: rank, power, the shape of one's own blood. **Dominion** floors carry rites that change the world around them: a vein rooted into stone, a domain, a Blood Moon dragged up early, a thing called into being. **Communion** floors carry rites that bind people to one another: bloodlines, covenants, the shared pool. **Working** floors carry rites that make. A Harbinger does not hand-feed a machine into existence beyond the first humble tools; they build its frame beside a Focus, and the Working floor rises up the frame like a membrane closing over a wound, drinks the blood poured into it, takes the brazier offerings as its own, and swallows the whole frame into the floor before setting the finished work on the Focus. The floor is not a workbench. It is an organ the Order has taught to digest intention into apparatus.

### 6.3b The Circus

The **Circus** is a specialist Harbinger sect, not a separate degree ladder and not an enemy faction by default. Its practitioners take puppetry, attention, decoys, displacement, light, sound, and mental manifestation further than ordinary outpost instruction. Saints retain singular unstable powers; the Circus instead refines systems that other Harbingers already recognize.

Their physical camp is a rough traveling pavilion of dark cloth, blackened hematic frames, bones, pelts, masks, hanging Crossbars, and visible thread. The complete spectacle exists in layered perception. An outsider first sees heat-haze, scraps, contradictory silhouettes, and incomplete geometry. Familiarity makes the shared vision calmer and more coherent until tents, performers, impossible movement, and apparently solid manifestations agree with one another. Collective imagination can acquire physical consequence, so the danger is not dismissed as harmless hallucination.

Scarlet Serpents and Prism Cuttles are natural Hemomancy-reactive animals rather than Circus members. Their hood displays, warning color, chromatophores, camouflage, and false visual tells inform Circus practice without transferring their ecology to the sect. The Mnemonist may direct an Adept toward these specialists after the player has practical experience with puppetry.

### 6.4 Degrees of Initiation

Most Harbingers — most outposts, most individual groups — only ever reach **Degree 5 (Illuminatus)**. To the average practitioner, this is the summit of what blood magic can offer. The existence of Degrees 6 and 7 is known only to those who reach them; the existence of the 8th is known only to Archons who are told, and most keep the secret.

| Degree | Title |
|--------|-------|
| 0 | Uninitiated |
| 1 | Neophyte of the Crimson Veil |
| 2 | Votary of the Hematic Covenant |
| 3 | Initiate of the Incarnadine Fane |
| 4 | Adept of the Sanguine Brotherhood |
| 5 | Illuminatus of the Crimson Lodge |
| 6 | Sanctified of the Bloodline Covenant |
| 7 | Archon of the Hematic Order |
| 8 | Apotheos of the Hematic Order, or Silentium of the Hematic Order (two equal ends) |

### 6.4a Degree Themes

Each degree carries a **lore theme** — the historical/metaphysical institution the player is entering — and a **gameplay theme** — the primary system or tool that opens at that rank. The escalation traces a straight line: from "blood as a personal resource" at Neophyte to "blood as a cosmic reproductive strategy" at Apotheos.

#### NEOPHYTE 1 — *of the Crimson Veil*
**Lore:** The Crimson Veil is the membrane between ordinary life and the world the Order inhabits. The player has just torn through it. The Hermit greets them as a first tentative step. The Order is not a secret society to them yet — it is a rumour. The Vicar's formal reception grants Degree 1. Before it, the unsettled charm can still be removed: an undedicated Harbinger is no use to the Order, and may be worse than an accidental one. Commitment surrounds the newcomer in black and crimson tendrils while the Vicar projects blood from their hand. Only afterward does the Vicar entrust them with a Sanguine Conduit. A Vicar who has founded their own bloodline may perform this reception for another player through Blood Projection. Their blood settles the newcomer's charm and receives them into the founder's bloodline.

The Alchemist's First Separation then earns Concentrated Blood. Injected directly from its vial, it needs a chance to settle during sleep; the player wakes at Degree 2. Practical blood work precedes the later teaching of the Living Staff and Cardinal Rites.

**Gameplay:** Blood as a living resource. The Vicar teaches Blood Absorption and Blood Projection at this stage: fill the vessel, then spend stored blood into blood-structure recipes. The first Neophyte assignment asks the player to carry 5000 ml, project blood into a Liber Sanguinium, and harden a Hematic Iron Block. The **Vial Centrifuge** is introduced as the first blood-processing station, separating sampled vials into tendency enzymes and storing 250 mL of blood per completed spin. First Humilis-rank blood manipulations become unlockable. Core loop: fill blood, spend blood, craft with projection, cast manipulations.

#### VOTARY 2 — *of the Hematic Covenant*
**Lore:** A Votary has taken a vow — not to the Order as an institution but to the Covenant as a compact between practitioners. The difference between someone who was bitten and someone who *chose to stay bitten*. The Vicar introduces the eight blood tendencies: Animus/Vivacious, Flammeus/Fervent, Ductilis/Neurotic, Lux/Incandescent, Mortem/Ruinous, Congeatio/Frigid, Ferric, and Tenebris/Umbral. The Fungal Podium may be encountered as an unreadable Lodge apparatus, but it carries no one into the Fungal Gardens except an Apotheos, long after the ninth-pome Fungal Spine.

**Gameplay:** The **Ghastly Alembic** and the wider tendency system become available. Players distill organic inputs into concentrated alchemical compounds while continuing the Living Syringe + Vial Rack workflow. The Alchemist's D2 work now runs on two formal tracks: Red Taxonomy field specimens and the Eightfold Centrifuge enzyme record, which asks the player to obtain all eight enzyme expressions without consuming them. A quieter Living Bestiary runs alongside those assignments: specimen jars can bring Hemomancy fauna, including wild Morphling Polyps, to the Alchemist for record-and-return study or surrender. Captured polyps introduce morphling layer families early through wild-bound morphlings, while the Degree 5 Incubator remains the full cultivation path. Tendency alignment scores begin shifting toward chosen affinities. The Blood Tendency UI is fully revealed. Humilis and Mediocritas manipulations from the chosen tendency line become craftable. More involved blood-structure crafting continues to open from here: Basic-tier multiblock patterns are available early, Advanced patterns unlock at Degree 4 (Adept), and Grand-tier patterns requiring sustained Blood Conduit flow unlock at Degree 5 (Illuminatus).

#### INITIATE 3 — *of the Incarnadine Fane*
**Lore:** The Incarnadine Fane was founded by Archon Erythravane in the Second Age — the first organized gathering of hemomancers, predating the current Order's structure. Erythravane was the first Vicar as well as the first Archon; every Vicar since holds the office Erythravane first held. Becoming an Initiate means joining a lineage centuries old. The Vicar reveals the Fane's founding and the person of Erythravane. This is when the player first understands they are not discovering a novelty — they are inheriting a tradition. The Vicar also directs them toward the Saints' mausoleums, whose outer halls an Initiate can already walk.

**Gameplay:** The **Somatic Loom** (memory weaving) becomes fully active. The Saints system opens: players explore the outer halls of the Saints' mausoleums, whose trials and loot open from about Degree 3–4, while the ward past the threshold, the Chapels and the Saint itself wait for Degree 8. Players extract Hallowed Residuum via Consecrated Syringe + Centrifuge, then use it in the Loom with a blank Hematic Memory, stored tendency enzymes, projected blood, and Engram Weaving at the loom to unlock **Canon Memories** — SUMMA-rank manipulations unique to each Saint's blood legacy.

#### ADEPT 4 — *of the Sanguine Brotherhood*
**Lore:** The Sanguine Brotherhood arose during a time of war, when Harbingers began sharing blood pools to sustain each other in battle — the earliest form of the Bloodline system. An Adept has moved beyond solo practice into understanding blood as communal inheritance. Simultaneously, the first **Fungal Whispers** begin — subliminal and barely perceptible. Just seeds of doubt.

**Gameplay:** **Scars** and the **Cerebral Scarring Station**. Scar crafting requires minimum Degree 4 (wired in code). The Vicar now sends Adepts to Masons Respite, where the Cicatrix Anchorite / Vein-Mason gives a first low-tier scar pattern keyed to the player's strongest blood tendency and coaches the four-part scar assignment: find the hermitage, carve and burn the first Scar item, prepare an Effigy loadout pattern, then burn that prepared pattern to set the active loadout. Players gain access to the Cerebral Scarring Station (surgical encoding of venous/neural pathways), the Mason's Effigy (scar loadout attunement), and the Iron Brazier scar interaction (burning scars into memory or burning prepared Scar Patterns into the active loadout). Scars grant passive bonuses, skill amplification, or manipulation modifiers from the player's active cerebral scar ids rather than from an equipped normal-scar item slot. Visceral Organ extraction via the Visceral Mirror ritual also becomes Adept-tier content. The Sporitic Thurible now sits in this same Adept band: a held thurible that burns aligned spores with blood upkeep, externalizing the Brotherhood's battlefield covenant as infectious support incense rather than simple weaponry.

#### ILLUMINATUS 5 — *of the Crimson Lodge*
**Lore:** The Crimson Lodge documented the link between hemomancy and the mycelial network — and kept that knowledge secret, insisting it must be "arrived at independently." Becoming Illuminatus means the Lodge judges the player ready to receive what they recorded. The Vicar reveals it: *"The blood you command… it was not always blood."* Most Harbingers never reach this degree. Fungal Whispers become clearer intrusions.

**Gameplay:** Three major unlocks. (1) **Morphling Incubator** — biological/fungal crafting for organism-derived items. (2) **Blood Structure Grand tier** — the Blood Conduit pipeline network becomes a mandatory crafting input for the most complex structural patterns; sustained flow within range is required, not just a single key strike. (3) **Sanguine Quintessence & Founding Fane** — the Illuminatus rite is the degree-advancement rite into the Crimson Lodge, while the separate **Founding Fane ritual** is the true consecration rite. Sanguine Quintessence is the founding medium seated in the Cardinal Focus; the rite establishes a bloodline-owned **Flexible Envelope** shaped by its Consecrated Bloodwell heart and connected Hematic Stakes. The Sanguine Monolith becomes accessible (Degree 5+ gated).

#### SANCTIFIED 6 — *of the Bloodline Covenant*
**Lore:** The Sanctified do not belong to the Order — they *are* the Order. The Vicar delivers the degree's doctrine: *"The blood becomes indistinguishable from the blood of the world."* The Bloodline Covenant is revealed — a ritual compact between Harbingers that merges blood pools and capabilities. Fungal Whispers grow direct and revelatory: *"The first Archons did not discover hemomancy. They were infected by it."* Erythromycelium is named as the original organism.

**Gameplay:** **Bloodline Covenant system** fully unlocked — players ritually bind capabilities into a shared Covenant: linked blood reserves, tendency bonuses that amplify between members, blood-link combat synergies. The Bloodline leader can ritually sever members (reinforcing the covenant-as-chosen-family framing). The Alchemist describes this degree as "final synthesis" — all machines working as one unified process. The **Rite of the Chamber of Will** also unlocks here: a Grand, inward cardinal rite that lets the caster enter a private blood-memory refuge. The chamber begins as a small 9x9 inner room and later grows with the Harbinger's progression; its sky changes from will-default vascular/neural imagery into Archon revelation, Qliphoth communion, Silent Archon restraint, and an Apotheos state whose first implemented treatment is a renderer-only black/red floor funnel.

#### ARCHON 7 — *of the Hematic Order*
**Lore:** The Archon is told what no one below this degree ever hears: *"The Hematic Order never had seven degrees. There have always been eight. The eighth degree is silence."* The Fungal Whispers deliver the complete revelation: the Order was never a human institution. Every degree was a stage of sporulation. The Vicar kneels. And then, quietly: the Sanguine Monolith begins to feel wrong. Hollow. Like something has been waiting inside it.

The Vicar's line now reads straight rather than ironically. Silentium is an eighth Degree, and the Hush that the Apotheosis floor rehearses is the other. The Vicar does not say which silence he means.

Every Degree-7 player is an Archon, and Archon is a full stage with its own work, not a waiting room for the eighth.

**Gameplay:** **Qliphoth Communion** — the endgame sequence. Shatter the Sanguine Monolith; perform the Bloom of the Qliphoth rite; collect and eat all nine Qliphoth Pomes, the ninth of which tears the Fungal Spine free; endure the first forced projection; then choose. Choosing Silence leads to the Rite of Pruning: reshape any base Living Weapon into the temporary sickle and cut the Bloom open. Choosing the eighth Degree opens the Rite of Apotheosis. **Fungal Scars** are implemented as a fourth scar family through Mycelial Crucible cultivation rather than the older Fungal Gardens harvesting plan; they alter the player's relationship to the mycelial network rather than deepening tendency alignment. Deeper Apotheos-tier fungal scar concepts remain planned design space.

#### APOTHEOS 8 or SILENTIUM 8 — *of the Hematic Order*
**Lore:** The eighth Degree has two equal ends, and both are chosen only after the ninth Pome has produced the Spine and the first temporary projection has returned the player to their body. Apotheosis is not an ascension to godhood — it is a *completion of the sporulation cycle*: the Apotheos lets the hivemind in. Silentium is the opposite act. The Archon cuts their own Qliphoth tree, the antenna that tied them to the hive, then takes Vesper's memory in by force. Both ascend by taking something in.

Each end has a home. The Fungal Gardens are the Apotheos's: they walk there in the body and the organism treats them as its own. The Saints' mausoleums are the Silentium's: the Saints' residue tolerates a will that has cut its antenna, and only a Silentium can wake a Saint properly. Each can trespass into the other's home at a cost. The choice decides where a player belongs, not what they may see.

**Gameplay:** The first Spine use is a forced two-minute projection with no accessible equipment and an accelerating red vignette. The choice opens after automatic return. Pruning, Vesper's defeat and the absorption of his memory make a Silentium; Apotheosis makes an Apotheos. Both are Degree 8.

### 6.4b Endgame Bosses: Refusal and Fruiting

These bosses are not good-ending and bad-ending mascots. They are two consequences of reaching the same awful truth and choosing what kind of body must carry it afterward.

**Vesper, The Crowned Refusal -> Vesper, The Evening Star** is the final boss for Archons who reach the eighth threshold and refuse fungal surrender. He should remain red and black, not yellow. The old yellow association is only a source-model inheritance, not his final palette or doctrine. His first phase is **The Crowned Refusal**: the Archon who keeps the crown because he will not kneel to the hive. His second phase is **The Evening Star**: the same refusal burned down into a colder, more beautiful, more dangerous sign. He is not "redeemed" by refusing Apotheos. He is the cost of human-scaled silence made militant.

Vesper's lore should avoid extra true names and public titles for now. The clean canonical phrasing is **Vesper, The Crowned Refusal** for phase 1 and **Vesper, The Evening Star** for phase 2. His guaranteed final reward is **Memory of Vesper**, a disciplined refusal that can be placed directly in an Iron Brazier and taught to the player's existing Living Staff through the rite instead of replacing it. The owned ordeal earns it once; a full inventory leaves a death-persistent claim waiting for space rather than dropping the Memory. The Mycophant's one-time owned Tendril reward follows the same delivery rule. Neither ending status alone grants replacements.

**The Mycophant** is the final boss for Apotheos-aligned Harbingers: the Spawn Mother, the hivemind's brood cultivator and the mother of every Erythromycelium strain. Her palette follows the mod's mushroom language: crimson and maroon flesh, rust and ochre, amber gestation, olive fungus at the seams. She is not Satan, not a punishment, and not a simple monster spawned by corruption. She is the gate and the key of the Hematic Order's hidden biology: her work is to absorb whoever transcends and breed new world-infecting strains from them, and to tend every life her infection has made. She is a genderless brood-mother. Fungi have no sexes and neither does she; "she" is the motherly register, never a feminine or sexual one, and the assimilation she attempts is reproduction only in the biological sense of spores, strains, fusion and gestation.

An Apotheos candidate reaching her is the system working as designed: the best specimen has come home to be taken in. The player does not kill a villain. They survive being claimed. Every assimilation she attempts fails, the joining never completes, and that refusal is the proof: nothing the hive has grown held out against its own gate. The player leaves as the Harbinger, the highest thing the hivemind ever produced and the heir to its will.

The confrontation begins as a timed certainty rather than a discovered altar. An Apotheos who spends fifteen eligible minutes in the Fungal Gardens hears her voice somewhere off among the fruiting bodies, then feels the edges of sight furring over with mycelium, and at last glimpses her standing in the fog behind them, gone into the ground the moment they turn. Roots then claim the immediate space over five seconds: hyphae lace across the candidate's sight, pull them to their knees and down, and close their eyes. They are carried alone into the **Morphic Nursery**, a round flesh hollow vaulted in bone ribs around a nectar pool, an owner-bound organic band of the Chamber of Will. The player wakes there lying in the nectar, still laced in the same hyphae, and tears free as they rise. She has risen from the nectar with her back turned, cradling a fungling and feeding it on her siphon. She senses the candidate, turns, and lets the fungling fall back into the nursery: next to this prize the brood she held does not matter. She rears, roars, and rushes in with claws and tendrils spread wide, an embrace.

In the first phase she fights like a mother retrieving a stray child: she gathers the player against her gestation sac, feeds on them with her siphon, plucks them back toward her, and calls her brood to herd them home, grieving when one of them is killed. Morphic Nectar rises around them as the slow clock of assimilation. Ordinary blows wear her hold down; blood manipulations and Primal Morphling abilities answer the claim more violently and tear the player free. At half health she stops treating the player as a child and starts treating them as an equal to be joined with. Her bone skull cracks and hangs open over a flowering face of eyes, wings of hyphae grow from her shoulders, and she becomes faster but calmer, as though struggle has become recognition. Each fusion the player breaks costs her dearly. Defeat is nonlethal expulsion. Victory is not an explosion: she folds her wings and collar, lowers herself to the player, offers the Tendril on her siphon, and sinks into the nectar. The hive yields, and names its heir.

After that first answer, a proven Apotheos may craft a **Fruiting Lure** from Sanguine Quintessence, a Spore Sac, Hyphal Substrate, and Infected Fungus. Letting one soak in Morphic Nectar for two seconds calls the Nursery again. The lure is consumed only when the owner is eligible; failed rematches require another lure after the five-minute Gardens-time recovery.

The Mycophant's guaranteed final reward is a Tendril equipped in the Charm of Vascularium rune slot. Lore-wise, it is not a trophy taken from a corpse; she offers it as she yields, the hive's will passing to its heir and the player's consent to let their visible body answer the same mycelial claim she embodies. While equipped, it fully fungalizes the player appearance, closer to a whole-body expression of the fungal Morphling's mushroom-head silhouette than a small cosmetic overlay.

Together, these two bosses preserve the Harbinger path's moral tension. Refusal keeps the self but turns silence into violence. Fruiting accepts the truth but risks losing the scale of a person. Neither should be framed as clean victory.

### 6.4c Vestments and the Hematic Armature

Harbinger armor is not simply blacksmithing. The Hematic Armature is a ritual scaffold: the practitioner enters the frame wearing the armor to be remade, offers blood into the overhead heart, and lets prepared reagents teach the metal what the next vow means. The rite should feel intimate and a little invasive. The armor remembers the body because it is transformed on the body.

The Armature itself now advances with the practitioner. The Hematic Artificer / Redwright teaches the base rack once armor-work becomes practical, while the Armature Consecration Kit marks the Degree 5 transition from machine to vestment-rite: candles, banners, chains, and formal Lodge attention turn the rack into a consecrated frame. The Artificer offers the kit; the Vicar retains authority over doctrine and degree rites. At Archon depth, the Monolithic Cornerstone completes the block's final form, letting the Crimson Lodestone's memory burden become part of the apparatus used for Silent Archon Vestments.

The Enzymatic Scriptorium grows through the Mnemonist's craft rather than the Lodge's. The **Rubricator's Quill** is the old scribe's habit of ruling a remembered line in red so the blood can find it again; the Rite of the Eightfold Script teaches the Scriptorium to single out one inscription from the rest. The **Palimpsest Burin** belongs to deeper practice: the Rite of the Palimpsest scrapes the page back to what the blood still remembers and cuts the chosen line in deeper than ink can hold. Neither is sacred. They are tools of recollection, and the Mnemonist hands them over as plainly as a Loom pattern.

All station upgrades share the same Cardinal ritual grammar: offerings, anchors, daemon seal, ordered blood circuits, and a completed procession. Their materials and teachers carry different meanings, not different progression rules. A teacher's free kit recognizes personal practice; accepting it is optional, and a crafted or gifted kit is equally usable once that practice and degree are earned. Borrowing another practitioner's apparatus can support learning, but possessing their tool cannot supply missing experience. The machine is changed in place, not discarded and rebuilt. The Scriptorium's former orb-writing and creature waves were an implementation holdover, not a lore requirement to restore. See [the shared station-upgrade contract](STATION_UPGRADES.md) for current mechanics and rules for future stations.

The ladder is a degree ladder with sidegrade forks. Hematic Iron is the first chosen shell: iron taught to carry blood. Barbed, Chitinite, and Prismatic armor are the Initiate's animal-tier answers to danger: venomous retaliation, ferric shelter, and blinding display. Each should feel like a blended ecology rather than a costume from one creature. Barbed borrows from urchin, centipede, and Desiccant; the Desiccant contribution is the Telson, a sting-bulb that swells red while drinking blood. Chitinite borrows from chitinites, Chthonians, and Chalybeate Snails; Prismatic borrows from Prism Cuttle, Lantern Ticks, and Blood Drunk Puppeteers. Blood Lust is the Crimson Lodge's combat vestment, but it is controlled hunger rather than mindless berserker rage. Its masks are small vows and martial tells, not separate doctrines.

Silent Archon Vestments are different. They are cloth and armor mixed once the Archon's refusal of Apotheos has ended in Silentium, made with Monolith Imbued Cloth and the memory-burden of the shattered Crimson Lodestone. Their theme is refusal that still costs blood: survival through discipline, not transcendence. They should never read as the "good" end of the Harbinger path, only as a human-scaled way to continue carrying the infection without surrendering wholly to it.

The Silent Archon route, which ends at the eighth Degree in Silentium, exists because refusing Apotheos is not ignorance or simple fear. The Archon has witnessed the fungal revelation and either cannot speak of it, is cursed into silence by it, or chooses exile rather than becoming another mouth of the hive. "Silent" names the madness, restraint, and refusal that follow the truth. The monolith-themed gear should make that refusal visible: the Monolith is a reflection of the player's will, and wrapping the body in its shattered remnants means the player has become their own fixed will through the ultimate rejection of the revelation. Their blood still works, but by sheer will rather than obedience; at this point the Harbinger's selfhood is strong enough to overpower even the influence of the fungal source.

The curse of silence should remain deliberately ambiguous. It may be self-imposed discipline, trauma, exile, madness, or a final kick in the teeth from the hive mind, but the result is the same: no one who reaches the truth can verbally speak it plainly to those below them. This is one reason the Harbingers can still exist and recruit without the whole Order collapsing under forewarning. Most Harbingers never reach Degree 5 and found a bloodline as a Vicar-level practitioner, let alone Degree 6 or higher. By the end of Degree 6 and the beginning of Degree 7, the player is already approaching the scale of a Harbinger Saint; beyond that, they are a living myth. Silent Archons cannot tell the warning, and Apotheos bodies no longer resemble ordinary Harbingers or meaningfully interact with mere mortals.

One-off pieces should remain covenant-breaking tradeoffs in tone as well as mechanics. The Crown of Sacred Marrow, Hemolymphopoda Headpiece, Venous Strider Sabatons, and Covenant Mantle each express a narrow vow or ecology. Wearing one means choosing that vow over the comfort of a full-set identity.

### 6.5 The Founding Fane

A shared Fane is sustained by individual covenant contributions. A member's personal blood deposited at its heart can write their own covenant proof; merely standing near another practitioner's rite does not. This preserves the distinction between belonging and practiced commitment. A shared heart does not require each member to found another bloodline.

At Degree 5, a Harbinger becomes eligible to perform the **Founding Fane ritual** — a distinct ceremony separate from both the Illuminatus rank-up rite and the Bloodline Founding rite. The **Rite of the Crimson Lodge** makes the practitioner Illuminatus; **Bloodline Founding** creates the covenant; the **Founding Fane ritual** gives that existing bloodline territory. Its boundary is a **Flexible Envelope**, beginning at the Consecrated Bloodwell heart and expanding through connected Hematic Stakes rather than occupying a fixed chunk square. Harbingers inside receive strength scaled by the living footprint, while its visible boundary, recall ground, and ledger permissions follow the envelope. **Sanguine Quintessence** is the medium seated in the Cardinal Focus, but the Quintessence itself is not the consecration — the founding ritual is.

### 6.5a The Sanguine Monolith (The Crimson Lodestone)

The Sanguine Monolith — known within the Lodge as *The Crimson Lodestone* — is a 1×2 stone construct accessible to Harbingers of Degree 5 and above. It provides degree-gated guidance up to Degree 7, at which point an Archon may shatter it to recover the Qliphoth Seed inside.

It should not be conflated with the Founding Fane. Both become part of Degree 5 play, but they serve different roles: the Monolith is a Lodge relic and guide, while the Founding Fane ritual is the act that consecrates territory, manifests fane boundaries, and anchors bloodline logistics around a chosen home.

**What it actually is:** The Crimson Lodge built the Monolith as an incubation vessel: a sealed stone construct housing a dormant fragment of mycelial consciousness, extracted from a deep Erythromycelium vein in the Second Age. The Lodge believed it could sense a Harbinger's degree because hemomancy leaves detectable signatures in the blood — and that explanation is almost right. The fragment inside is alive. It reads degree not as a record-keeping system but as a sporulation assessment. It is watching practitioners ripen.

At low degrees the fragment is barely aware; its surface is cold. By Degree 5–6 it begins to stir — the stone grows faintly warm. At Degree 7, the Archon's blood is mature enough that the fragment fully awakens and the vessel cracks. The seed inside is not merely a byproduct: it *is* the fragment, or the most coherent part of it, made physical.

The shattering also leaves behind **Monolith Fragments**: stackable black-red facets that behave less like ordinary stone and more like frozen witness. They carry the weight of the Monolith's last coherent shape, making them fitting late-game catalysts for Archons who reject full fungal surrender. They are useful, familiar, and burdened rather than purely empowering; Apotheos-aligned bodies should find them oppressive.

The Monolith uses degree-gated dialogue that gradually discloses its nature. Players who ask *"What are you?"* receive answers that escalate from official Lodge description (Degree 4) to admitted self-awareness (Degree 5) to disclosure of the contained thing (Degree 6) to direct recognition of kinship (Degree 7). Archon dialogue is intimate in a way that should feel wrong before it feels significant.

### 6.5b The Chamber of Will

The **Chamber of Will** is the Sanctified Harbinger's inward refuge. It should never read as a second Fungal Dimension. The Fungal Dimension is the Entity's surface, hostile and external; the Chamber is a controlled place inside the caster's own blood-memory, stabilized by will, infection, scar tissue, and accumulated degree.

Its progression role mirrors the Founding Fane without replacing it. The Founding Fane is outward: territory, bloodline logistics, covenant ownership, and the public declaration that this ground belongs to the Order. The Chamber of Will is inward: recovery, reflection, preparation, and the private admission that a high-degree Harbinger's mind has become a place.

The chamber should feel like refuge first. It can be quiet, useful, and safe by default. As the player moves through later Chamber states, the sky and boundaries can show that privacy eroding: vascular and neural structures gaining stranger colors, Qliphoth influence disturbing the pulse, and Silent Archon restraint muting the chamber. Apotheos has its fungal transformation treatment with a renderer-only black/red floor funnel: concentric, meaty bands descend into a dark aperture below the refuge, implying the Harbinger ascended through the previous Qliphoth black-hole state. Its wall pass frames that aperture with dark blue-black fibrous membrane, rib/tendril side structures, faint pale pink-blue web traces, and a low red floor glow. Apotheos ceiling mass now defines the top of the chamber as a red/purple organic dome with black and white tendrils, yellow light blooms, yellow/green orbs, and a glowing red rim at the wall-top handoff, all as visual pressure rather than terrain or danger.

In the Silent Archon state, the chamber should feel less like a living vascular interior and more like a cold dream of willpower: gray sky, dark storm-cloud or nebula masses, scattered black monolith pillars rising from a low cloud deck, far pillar-shadows half-lost in haze, and heavy foreground storm clouds that swallow the lower monoliths and make the refuge feel bottomless. Those pillars are not decorations so much as selfhood made architectural, the shattered Lodestone's lesson repeated across the refuge.

### 6.6 The Hematic Order's Historical Record

From the Harbinger Vicar's lore branches:

- The **Incarnadine Fane** was founded by **Archon Erythravane** in the Second Age — the first organized gathering of Hemomancers. Erythravane was the first Vicar and the first Archon.
- **Hemorath**, one of the first and most powerful Hemomancers, came before both the Fane and the Order's structure and was never a true Harbinger. His successors and students feared they would be lost to bloodlust as he was, and that fear shaped the tradition Erythravane later organised into the Hematic Order. The Order inherited his tomb, and the tombs of the three later Saints, from the people who sealed them (§10).
- The **Sanguine Brotherhood** arose during a time of war, when Harbingers began sharing blood pools to sustain each other in battle — the origin of the Bloodline system.
- The **Crimson Lodge** documented the link between hemomancy and the mycelial network. This knowledge was kept secret — considered something that must be "arrived at independently" to be understood correctly.
- The **Hematic Order** itself is described by the Vicar not as a rank, but as a state of being, where "the blood becomes indistinguishable from the blood of the world."

### 6.7 Rogue Hemomancer Wills

**Broken Wills are remnants of failed former Harbingers**: practitioners whose bodies or identities collapsed while trained blood-memory continued to repeat incomplete techniques. Faded Wills are the most eroded members of this same family, not a separate species.

**Sent Wills are purpose-built by the Fungal Entity** to assess practitioners who are approaching ripeness. Proctors are their deliberate, evaluative expression. They are not dead Harbingers and should behave with more coherent priorities than Broken Wills.

**Blood Drunk Puppeteers are Broken Wills commandeered by the hive's strings.** Their visible control and their susceptibility to player commandeering are literal extensions of that origin. These categories describe implemented origins, not competing NPC theories: Broken/Faded are failed people, Sent/Proctor are made assessors, and Blood Drunk Puppeteers are failed people being actively pulled.

Harbinger puppeteering imitates that visible principle without accepting the hive's claim over the practitioner. A Puppeteer's Spindle fixes a learned blood-memory into a prepared shape and permanently attunes its Marionette Crossbar to one owner; the Spindle does not project the body itself. Enthralling Filament becomes stored tension in the Crossbar, and the attuned Harbinger spends that tension to call, sustain, recall, or direct the shaped body in the field. A Crossbar can hold a shared behavioral command for its exact skein, while the Degree 5 disciplines **Sanguine Spinning** and **Skein Transposition** let an advanced Harbinger wind their own blood into a held Crossbar or replace its prepared artificial shape without returning to the Spindle. Spinning creates tension rather than physical Thread, and transposition changes the shaped expression, not the identity of the tether: unrelated shapes and claimed people are not rewritten. The tether is personal and local: another practitioner cannot inherit the Crossbar, starvation unravels only the body whose upkeep failed, and crossing dimensions, death, or the owner's departure severs the projection even when its chunk was asleep. A faltering Broken Will can be Commandeered into the same tether system and obey its commands, but it remains a claimed failed person rather than becoming one of the four learned artificial shapes or a candidate for transposition.

An equipped Morphling and an owned puppet tether can coexist, but they pull against one another. The incorporated organism reads projected will as foreign, edible tension and gnaws at the thread; the puppet's imposed will repeatedly scatters and frustrates the Morphling's attention. This is reciprocal inefficiency rather than doctrinal exclusion: the tether shortens and costs more thread to sustain, while the distracted Morphling consumes more blood. The Mnemonist can identify the conflict at Degree 3 while both are active, but records no permanent choice or prohibition.

The four canonical artificial shapes are learned through **Puppetry Cardinal Ordeals**, not by assembling and consuming a body as an ordinary craft. Each ordeal uses the shared Dominion Lesser floor because all four test the same claim: whether the practitioner can impose a stable artificial body without surrendering the tether to the hive. The owner's attuned Crossbar is seated intact in the Cardinal Focus and remains unchanged, because it is the enduring relationship under examination rather than fuel. Bespoke lit-brazier offerings declare the body being called. Once manifestation succeeds, the puppet itself is the ordeal and must be defeated; cancellation, abandonment, structural failure, or the caster's loss grants no memory of mastery. Victory teaches the exact shape whose resistance the practitioner overcame.

---

## 7. The Unstained

### 7.1 Origins

The Unstained are not a wholly separate tradition. Their **original members were Harbingers who left** — practitioners who had experienced blood magic firsthand and then chose to reject it. This is why they are so effective at combating it: the founders knew it from the inside. Their methods of purification, their understanding of what hemolytic solution does to blood-infused veins — this was all discovered and refined by people who had once been what they now oppose.

This also explains why the Unstained welcome Harbingers who sincerely seek a cure. The ordinary hard cutoff is not a degree and not membership in someone else's covenant: it is **founding a bloodline**, the act that roots the infection through a covenant created by the practitioner. A founder cannot use Annetta's Unstained purification route. Defeating the infection unleashed by her fatal Harbinger encounter teaches the exceptional founder-severance rite that can reopen the path.

### 7.2 What They Believe

The Unstained do not necessarily believe blood magic is evil in an absolute moral sense. They believe it is:
- **An infection, not a gift** — something that has colonized the bearer, not something that was ever theirs
- **A threat to the self** — as purification progresses, practitioners discover that what felt like personal power was always the fungus guiding them
- **Something that can be shed** — through painful, deliberate effort

They are devout, but not fanatical in the way of those who seek to convert or conquer. They welcome those who come to them and leave alone those who do not. A Harbinger wandering into an Unstained Church will receive a measured response:
- Degree 0: concerned sadness; the Church sees a newly blood-touched person as infected and frightened rather than culpable
- Degree 1-2: careful welcome; help is offered plainly, though the Unstained warn that every rite makes the blood harder to shed
- Degree 3-4: wary help; Zealots and Acolytes still offer cure, but ask whether the player seeks healing or merely another power
- Degree 5+: caution rises with experience, but degree alone never closes ordinary cure
- Bloodline founder: ordinary cure is refused until the exceptional severance learned from the unleashed infection has cut the founder's integration
- Guardians use blunt force to escort hostile or deceptive visitors out when necessary; they do not spill blood near the church

### 7.3 Appearance and Equipment

The Unstained aesthetic is **white, silver, and oxidized copper**. Their Guardians wear:
- White plate mail with flowing white robes beneath
- Oxidized (fully patinated) copper accents and components
- Glass helmets — intentionally suggesting they are not breathing the same air as others, either literally or metaphorically

Their doctrine minimizes bloodshed rather than banning every blade. Guardians favor maces, war hammers, and staves. The **Silthmere Glaive** is sanctioned because its reach keeps infection away from the wielder. Every initiated Unstained carries an **Absolution Dagger**, whose inherent special hemolytic coating makes it an antiseptic last resort rather than an ordinary knife.

Their field equipment is practical before it is mystical. A **Pale Silver Bell** makes blood-magic hesitate and gives frightened practitioners a moment to breathe. A **Lethean Chalice** carries still water for rinsing poison, fever, and old influence from the body. A **Verdigris Censer** burns oxidized copper salts into a diagnostic smoke that marks blood-active bodies without needing to open them.

### 7.4 The Significance of Copper and Silver

**Copper** is historically antiseptic — it does not harbor disease, and the Unstained have known this for generations (even if they did not understand the chemistry). Copper door handles, copper tools, copper armor — surrounding oneself with copper was believed to stave off illness.

**Oxidized (patinated) copper** holds an additional sacred status:
- Copper that has fully oxidized and stopped changing represents something that has "done its job" — absorbed all the blood and disease it could until it was saturated, and was then consecrated by that service.
- A person carrying fully oxidized copper must have survived long enough for it to reach that state, meaning they successfully kept themselves pure throughout. The copper earned its patina by protecting them.

**Virid Salis** is the Unstained's green salt-ash: a verdigris-colored ritual powder that borrows the holiness of patinated copper without pretending the copper is clean. It marks lines of warding and purification in the same practical spirit as ash trails, but its meaning is defensive and antiseptic rather than sanguine.

**Silver** in this world does not spawn naturally. It is **consecrated copper** — copper that has been processed through a ritual of purification until it achieves a purer, refined state. Pale Silver Ingots are the primary high-tier Unstained material.

**Silthmere is a liturgical title of Our Lady of Still Waters** — a shorter, name-like title used in rites and sacred instruments, not the name of a separate historical mortal. “Remembrance” invokes the Lady under that title and recalls the discipline she represents; it does not commemorate a person who once walked the path.

### 7.5 What Happens to the Purified

During Phase 1, purification changes the practitioner's relationship to blood rather than visibly remaking the rest of the body: blood magic weakens, bleeding diminishes, and sanguine signs fade. There are no additional progressive changes to skin, eyes, hair, nails, food, poison, or ordinary healing during the Purity stages.

At Purity 100%, the Hemolytic Solution has prepared the practitioner for preservation, but its broader physiology unfolds only after **Clarity Ascension**. As Clarity rises, their veins become functionally embalmed and their blood ceases to behave conventionally. The change is expressed through diminishing bodily processes, not a sequence of new cosmetic mutations.

Abilities that emerge at this stage:
- Improved night vision
- Ability to sense illness in those nearby
- Healing touch
- Passive plant growth in their vicinity
- Greatly enhanced resistance to blood corruption

**Implementation boundary:** these traits remain canonical consequences of complete purification, but they are not all represented by current runtime effects. The mechanics reference must track coma/bloodlessness, halted normal aging, altered hunger, illness sense, healing touch, and passive plant growth separately as implemented, partial, or planned rather than treating the list as one completed feature.

And critically, the path tends toward **biological immortality**. Across Clarity, ordinary healing potions and Regeneration work progressively less, poison has progressively less purchase, and hunger slows as the preserved body needs less food. At Enlightenment those responses are greatly diminished, not wholly removed. The Unstained can still die through sufficient physical destruction, fire, magic, or fungal means; the release scope does not grant general invulnerability or rewrite drowning and every ordinary damage source.

The Unstained Church does not need many members. High-quality, immortal practitioners who arrive at purity and stay are sufficient. The church grows slowly, surely, and endures.

Once Clarity opens, the purified no longer force power into being the way Harbingers do through Hematic Memories. Instead, they learn **Still Arts**: quiet, defensive acts that settle into the silvery vital humor as gifts from Our Lady or as the fruit of non-breaking rites. These arts are progressive rather than manufactured. A newly clarified Unstained might learn Silver Rebuke, while later clarity allows Lethean Mute, Still Pulse, Pale Diagnosis, Memory Shear, Absolving Step, Quietus Bell, Pale Intercession at Resolute, and finally Autoimmune Edge.

The last of these is intentionally troubling. Autoimmune Edge is not evil, but it shows the risk in Our Lady's nature: an immune response can protect the world, and it can also begin to attack anything that resembles infection too eagerly.

### 7.6 The Zealots, Acolytes, and Guardians

- **Zealots** are the recruiters and frontline representatives, the NPCs who approach Harbingers with the offer of purification.
- **Acolytes** are found at the temples/churches — some are genuine believers pursuing purification, others are simply people who found community there and are not yet committed to the full path.
- **Guardians** are the purified and near-purified warriors who protect sacred sites. They are the closest thing to a military force the Unstained have, and they use their blunt weapons with full commitment.

---

## 8. Our Lady of Still Waters (The Pale Lady)

### 8.1 Who She Is

Our Lady of Still Waters — sometimes whispered as *"Our Lady of Lethe"*, *"The Lady of the Forgotten Waters"*, or *"She Who Absolves"* — is a being who has existed in this world since before the fungal infection arrived.

She is not a goddess in the divine-mandate sense. She is a **force of nature** that has always been present: the cold wind in the middle of winter, the icy death at the bottom of a river, the stillness of a lake untouched for centuries. These things are not evil. They are part of nature — a stagnant part, but one from which life still springs in its own way.

Her relationship to the infection is analogous to **white blood cells in an immune system**. She did not set out to oppose the fungus with righteous fury. The fungus arrived, it began altering the world, and she manifested a more concentrated, invested form in response — the world's antibody taking a shape that could interact with the thinking beings caught up in the conflict. She recruits through the Unstained not out of active aggression, but because having allies on the ground is how a defensive force operates.

> *Note:* If white blood cells go on the offensive, that is called an **autoimmune disease** — not something desirable. The Pale Lady is purely defensive, by design and by nature.

### 8.2 Her Appearance

Those who glimpse her or see depictions of her describe:
- A **tall woman** with **white hair** that flows like water
- **White robes** with faint silver thread
- Eyes of **liquid silver** that see through all deception and corruption
- Skin of **pale blue**, as though always touched by cold water

### 8.3 Her Power and Scale

The Pale Lady is, in three-dimensional terms, a significant power. A fully enlightened Unstained practitioner operating with her backing could reasonably fight a Harbinger Vicar to a standstill. This is genuinely impressive — but it is not the same scale as the Fungal Entity, which operates across four dimensions.

Think of it as: pagan river deity vs. Zeus vs. something beyond classical theology. She is the river deity. She is real, she has genuine power, and she does real things in the world. But the Entity is playing a different game entirely.

She knows this. She does not aspire to destroy the Entity. Her duty is to protect the world, not pursue the source.

### 8.4 Her Domain

The Pale Lady's domain **is the Earth itself**. She has no separate dimension, no underworld, no realm beyond. The world she protects is the world. This is partly why she is outmatched by a being that exists partly outside of this world — she is bound to it in a way the Entity is not.

She is associated with:
- **Still and cold water** — rivers, ponds, frozen lakes
- **Winter and stillness** — cold winds, ice, states of stasis
- **Forgetting** — her connection to the mythological River Lethe, the waters of forgetfulness in the underworld. The dew of the Lethean Poppies carries her essence: the power to make you forget, to let go, to shed what was once held.
- **The Moon** — the moon orbiting the Earth is believed to be an expression of her watchful presence, a guardian eye.

### 8.5 The Lethean Poppies and Her Gifts

Wherever Our Lady once walked, **Lethean Poppies** bloom. These pale flowers bloom in cold areas and along riverbeds. They secrete a nectar called **Lethean Dew** that:
- Accelerates purification when used correctly
- Causes a Hemomancer to **forget one learned blood memory** — literally erasing a manipulation from their knowledge, severing a tie to the infection
- Is a crafting ingredient for Unstained recipes

The **Tears of Silthmere** are Lethean Dew distilled to its purest form — a one-time blessing from the Lady herself, offering a significant burst of purification when offered at an Altar of Cleansing.

The original **Pallid Icon** is a rare relic carved from pale silver and found at the bottom of a forgotten river. Consecrated copies reproduce its posture and serve as the carried emblems used by Guardians and rites; they do not become the historical river relic merely because they share its item form. The Icon calls a brief communicative apparition through which an Unstained practitioner can ask lore questions. **Pale Intercession** is different: in immediate danger, Our Lady sheds one small veiled defensive fragment of her attention, the world's immune response coming to the greater aid of an Unstained practitioner. Neither manifestation is her complete physical arrival.

### 8.6 Her Hidden Nature — A Warning

Toward the end of the Clarity path, Our Lady's whispers change subtly. She grows *quieter* and then, occasionally, she begins to push the player toward **actively eradicating sources of corruption** — something that is out of character for a purely defensive force. This is meant as a seed of doubt for the player and a hint at something not yet fully explored in the narrative. She is not perfect. She is still part of nature, and nature can become something other than what we expect.

---

## 9. Blood Moons — Cosmological Significance

Blood Moons in Hemomancy are not just a gameplay event. **Blood Moons are fungal surges**: periods when the Entity's influence presses unusually hard into the world. The red lunar face, fungal-vein overlay, Harbinger empowerment, Loom discount, and increased fungal activity all belong to that surge.

Our Lady does not create the Blood Moon. Pale and Unstained effects during the event are her costly containment response, and the Rite of Lethean Tide ends the event by strengthening that response until the surge is forced back. The red night therefore shows attack and defense at once, but its initiating cause is fungal.

From the player's perspective:
- Blood Moons are uncommon natural events; current gameplay checks once per night and may start one for the rest of that night
- Harbingers / Hemomancers receive **enhanced strength and night vision**
- Non-blood-magic users receive **weakness**
- Thirsters and Fargones stir in the dark
- The moon itself renders red, with fungal-vein sky forms intruding around it

The implemented **Rite of the Sanguine Eclipse** can trigger a Blood Moon manually. It remains intentionally expensive in meaning even where balance values continue to be tuned: a single practitioner is invoking a worldwide fungal surge that affects everyone on the server, so the act should remain deliberate and uncommon rather than routine weather control.

---

## 10. The Saints

The Saints are ancient hemomancers whose gifts carried their bodies past ordinary biological limits. Each began with a distinct command over the blood and followed it to an extreme from which there was no simple return. Sainthood is not moral goodness, and a Saint's tomb is not a grave. Each sarcophagus is a relic, a containment site and evidence of what blood can become.

There are **four Saints**: Hemorath, Seraphae, Putriciel and Velorum. They are not randomly assigned. A sarcophagus holds the Saint it was made for, and an unassigned sarcophagus stays inert until a Saint relic names its occupant.

**How they are found.** Each Saint rests in its own mausoleum complex, built to that Saint's palette and history. The Vicar directs Initiates toward the Saints around **Degree 3–4** (§6.4a), and the Sanguine Monolith points Adepts toward the Sainted Mausoleums, but what that sends them to is the outer halls: the entrance, the antechamber and the outer trial chambers with their early loot. A ward across the threshold turns back anyone below Degree 8. Beyond it lie three Chapels, the ring door and the Saint's own chamber. The Saint fights are Degree-8 content. The older generic mausoleum stays in the data but no longer generates.

**Walking the life backwards.** A complex is read from the Saint's rest back toward who they were: the chamber is where it all began, not only where the body lies. Each Chapel is drawn from a deed in the Saint's life. A **Beholding** replays a memory that must be watched correctly. A **Litany** hides the order of offerings in an Abocipher inscription. The **Downfall** is the Saint's fall to the power of Hemomancy, fought as a gauntlet. A **Repose** asks for stillness under threat. Each completed Chapel lights a node on the ring door.

**Opening the chamber.** A Silentium who has lit every node projects into the ring door and connects them with their own blood. The rings fill, the door falls away, and the Saint wakes properly. An Apotheos can instead drive hyphae into the stone and shatter the door at any time, skipping every Chapel. The Saint then wakes enraged, carrying the power of each Chapel the player did not complete: skip the Downfall and it fights with its fall; skip the Repose and it never pauses. The player is fighting the sum of what they refused to see, and a forced awakening yields only a fragment of Hallowed Residuum.

**Hallowed ground.** A mausoleum's ground is consecrated, not holy. Every Saint fell to the power of Hemomancy, and that fall was their downfall. Their residue lashes out against the hive's reach wherever they rest. The rejection is aimed at the hive's will, not at fungal matter: the growth in the tombs and the Saints' own infection are untouched. What the ground refuses is a body the hive speaks through. An Apotheos burns there, first through spore load and then through flesh. A Silentium is tolerated, having cut the antenna to the hive. Lower Harbingers in the outer halls are too faint a signal to provoke it.

**Sampling and waking.** A sarcophagus is first consecrated with blood poured into the bowl at its foot. The player then slides the lid off and draws from the Preserved Corpus with an empty Blood Vial. If the player's dominant blood tendency is one of the Saint's two tendencies, the corpus yields a Consecrated Syringe once, peacefully, and the Vial Centrifuge turns it into that Saint's Hallowed Residuum. If not, the corpus recoils and the Saint wakes to test the player. Smearing **Foul Paste** on the sarcophagus is deliberate desecration: it wakes the Saint whatever the player's alignment, even after a peaceful sample. Pressing a Saint relic to the sarcophagus names that Saint and wakes it.

**Canon Memories.** Hallowed Residuum is the catalyst for each Saint's Canon Memory at the Somatic Loom. Each is a SUMMA-rank manipulation that takes its Saint's tendency pair: Crimson Tithe (Hemorath), Unclosing Eye (Seraphae), Bloom of Rot (Putriciel) and Endless Hour (Velorum).

**No Saint can be killed.** Every encounter ends with the Saint back in its tomb: the sarcophagus is restored in place, closed and dormant. Each Saint has its own reason, given in its entry below. Whatever the ending, the encounter rolls its reward once, from that Saint's loot table: its Hallowed Residuum and one or two Sanguine Quintessence.

**Release scope:** The entire Saints system is modular expansion content. Initial lore may foreshadow the four Saints, but no Saint encounter—including Hemorath—is part of first-release progression. Chambers, environmental storytelling, victory conditions, placement, rewards, Canon memories, balance, and final presentation are reserved for the first major post-release content update.

| Saint | Title | Tendencies | Canon Memory | Initial gift | Limit exceeded | Condition at entombment |
|---|---|---|---|---|---|---|
| **Hemorath** | The First Saint | **Animus + Ferric** | **Crimson Tithe** | Blood absorption and command of the iron in blood | Production, intake and loss of blood | Desiccated, skeletal, insatiable; living marrow drives a ceaseless blood vacuum |
| **Seraphae** | The Chain Saint | **Lux + Ductilis** | **Unclosing Eye** | Healing through luminous transfusion, including nerve and limb restoration | Cellular growth and nervous excitation | Chained, radiant, dangerously regenerative; even her proximity induces uncontrolled growth |
| **Putriciel** | The Befouled Saint | **Mortem + Flammeus** | **Bloom of Rot** | Precise cleansing of infected flesh, relics and land through hematic fire | Infection, decomposition and combustion | Fungal overgrowth broken by brief, involuntary returns of cleansing flame |
| **Velorum** | Saint of the Endless Hour | **Congeatio + Tenebris** | **Endless Hour** | Suppression of blood flow, warmth, metabolism and sensation | Biological stillness and sensory deprivation | Lucid, cold, motionless; presides over a congregation preserved against its will |

### 10.1 Ascension and Recognition

A Saint is made by two separate events: becoming one, and being recognised as one.

1. **Biological ascension.** A hemomancer's blood and body reach an exceptional, irreversible condition that sustains a form of existence that should be impossible. Greed, exploitation, infection or deliberate discipline can bring it about. It requires neither goodness nor religious approval.
2. **Recognition by the Hematic Order.** The Order later names such a person a Saint because their condition shows an extraordinary expression of blood manipulation. Recognition need not happen while the Saint is alive in any ordinary sense; it may come centuries after entombment.
3. **Containment and veneration.** The Saint's chamber is a tomb in form but not in function. It holds an active biological danger, preserves a singular source of blood, and offers an object of study and ritual reverence.

This is why **Hemorath can be a Saint of the Order without ever having been a Harbinger**. He was one of the first and most powerful Hemomancers, and he predates the Order's structure and Erythravane's Incarnadine Fane. Seraphae, Putriciel and Velorum never belonged to the Order in life. Each was sealed away by their own people, and the Order inherited, preserved and reinterpreted the tombs afterwards.

**Why the Saints are kept rather than killed:**

- They cannot be killed. Their altered bodies survive every method used against them, and each returns to its dormant condition after any defeat.
- Their presence makes uncontrolled exposure dangerous to settlements and caretakers.
- Their blood is the only source of **Hallowed Residuum**, the catalyst for the Saint-associated Canon Memories.
- The Order reads their conditions as both warnings and proofs of what blood can do.
- A trial chamber and sarcophagus therefore serve both **containment** and **initiatory examination**.

In play, a Saint yields a sample to a player whose dominant blood tendency aligns with it and rises against one it rejects. The account above explains why the sarcophagus holds something that remains dangerous and sacred rather than simply dead.

> **Interpretive maxim:** The Saint is not necessarily an example to follow. The Saint is proof of what the blood can become.

### 10.2 Hemorath — The First Saint

*Saint of Iron and Marrow · The Insatiable · The Hollow Sovereign*
**Tendencies:** Animus + Ferric
**Canon Memory:** Crimson Tithe

Hemorath is remembered as one of the first and most powerful of the Hemomancers, a man who discovered that the blood within his body was not merely a substance necessary for life, but something he could command. He was never a true Harbinger: the Order that would later name him a Saint did not yet exist.

His earliest experiments concerned the iron contained within his own blood. Through concentration, he found that he could draw it together, hardening his blood into crude metallic formations beneath his skin. With practice, he learned to move his blood independently of his body, and eventually discovered that he could replenish what he lost by drawing blood directly from other living creatures.

What began as experimentation quickly became an obsession.

Hemorath found that every additional measure of blood strengthened his abilities. His body grew more resilient, his iron formations became larger and harder, and injuries that would have killed an ordinary man became little more than inconveniences. He began draining animals, then men, taking far more than he required simply to discover how much blood his body could hold.

As his power increased, so did his appetite.

Hemorath began consuming blood in extraordinary quantities, swelling his body far beyond its natural proportions. His limbs thickened, his belly distended, and the skin stretched across his body became thin and discoloured. He continued feeding despite the pain, convinced that his body would eventually adapt to whatever quantity he forced into it.

Instead, his flesh began to fail.

His skin split and softened, gradually losing its structure until great portions liquefied and sloughed away. The exposed tissues beneath continued swelling, sustained by the enormous volume of blood circulating through them. Even as his body deteriorated, Hemorath refused to stop.

Eventually, his flesh could no longer support the demands of his blood. The absorption overloaded him. Much of his flesh collapsed and died, leaving little more than a grotesquely enlarged vascular system supported by bone.

Yet Hemorath himself remained alive.

His bone marrow had become so altered by his prolonged manipulation that it continued producing new blood at an impossible rate. The death of his surrounding tissues did nothing to interrupt this process. Instead, his body began compensating by absorbing blood from anything living in his vicinity.

He no longer needed to consciously draw blood from his victims. Their blood simply answered his presence.

The transformation had an unforeseen consequence. No matter how much blood his body produced or absorbed, it could no longer retain it properly. His ruined vessels leaked continuously, and the very iron he had once commanded began accumulating throughout his exposed skeleton.

He became a living circulation without a complete body.

In time, even the tremendous bloating that had defined his transformation disappeared. His remaining tissues contracted and dried, leaving a tall, skeletal figure with a sunken abdomen, hollow cavities, and exposed structures hardened by centuries of iron deposition. Within his bones, however, the marrow continued its ceaseless labour.

He had become insatiable not merely through desire, but through necessity.

The hemomancers who followed him regarded Hemorath with a mixture of reverence and terror. His discoveries formed the foundation of blood absorption and Ferric manipulation, yet his continued existence threatened everyone who approached him. His successors and students feared they would be lost to bloodlust as he was, and that fear shaped the discipline that Erythravane, the first Vicar and Archon, later organised into the Hematic Order.

Attempts to destroy his body proved unsuccessful. Blood spilled against him was absorbed, injuries stimulated further production, and even when starved he continued to draw what little remained within his own ruined tissues.

Eventually, his successors constructed a great stone mausoleum around him, surrounding his resting place with blood basins designed to regulate the quantities he could absorb.

There, deprived of living prey, Hemorath slowly contracted into the desiccated figure preserved within his sarcophagus.

He became known as the First Saint not because he had achieved any particular holiness, but because he demonstrated that blood could sustain life long after the body itself had ceased to function.

To this day, the marrow within his bones continues to produce blood.

And should a living creature draw sufficiently near, the hollow thing within the sarcophagus will once again begin to fill.

**Hemorath at a glance**

- **Original character:** Morally questionable from the outset; curious, acquisitive and increasingly greedy.
- **Tendencies:** Animus is the living flow his marrow cannot stop producing and drawing in. Ferric is the iron he first commanded, which now hardens his exposed skeleton.
- **Source of horror:** Appetite becomes a physiological mandate; he can neither retain blood nor stop acquiring it.
- **Fate:** A grotesquely bloated phase gave way to the present **frail, sunken, desiccated** body.
- **Environment:** Blood basins, sarcophagus, stained stone, iron deposits, a controlled supply of blood.
- **Visual direction:** Skeletal proportions and a hollow torso, exposed iron-stiffened anatomy; **not** a permanently obese figure.
- **Older names:** "Hollow Vessel" was Hemorath's working title. Player text and the loot table now use Hemorath; the second-phase card keeps "The Vessel Empties".
- **Thematic sentence:** *Hemorath consumed.*

**Appearance:** A tall, hunched and desiccated saint with an exposed rib cage and long skeletal hands. Pale bone and dried crimson cords obscure his bowed face beneath a rusted, spiked iron halo. He carries a tall iron staff hung with chains and reliquary fittings; ruined ivory and crimson vestments trail around his feet. Irregular iron scabs and tapered spikes erupt through his ribs, skull and exposed limb bones. His ribs prise apart as stolen blood fills the hollow frame. The reaching claw draws debt along blood-threaded chains; when the vessel overloads, the rigid posture gives way and the emptied skeleton folds into its ruined vestments.

**Trial / Encounter:** The prototype trial seals the player in a chamber that slowly saps their blood. Four basins must be filled to precisely the correct level, not overflowing and not empty, while blood constructs spawn throughout; filling all four correctly opens the gates to the inner sarcophagus chamber. Waking Hemorath starts a blood-debt and overload fight. Blood the player loses near him, to wounds or to casting, accumulates as blood debt, which he collects as Hematic Collapse damage, and his Empty Pulse inflicts Hemophagy, which cuts healing. Below half health he enters a second phase ("The Vessel Empties"): faster attacks, a shorter Collapse cadence, and blood spending amplified by a quarter. The same blood fills his absorbed-blood meter, so reckless casting can collapse the player under their own debt, while enough of it (3,000 points) overloads him. It is a puzzle fight, not a damage race.

**Ending:** Overload, or wearing his body down, ends the same way: Hemorath bursts and is exsanguinated again, emptied by the thing he sought to master. He does not die. The marrow never stops producing blood, and in time he reforms. The emptied skeleton folds into its vestments, the sarcophagus is restored over him, dormant, and the hollow thing inside begins, slowly, to fill.

### 10.3 Seraphae — The Chain Saint

*Saint of Light and Nerve · The Bound Radiance · The Unclosing Eye*
**Tendencies:** Lux + Ductilis
**Canon Memory:** Unclosing Eye

Seraphae was originally a nurse who tended to the sick and wounded in a small settlement. She possessed no formal understanding of blood manipulation and had never considered herself capable of anything beyond ordinary medicine.

Her abilities first manifested while attempting to save a patient suffering from severe blood loss.

With no means of replacing what the patient had lost, Seraphae offered her own blood. As it entered the patient's body, her veins began to emit a faint white light, and the dying man's condition improved with extraordinary speed.

The bleeding stopped, his colour returned, and within hours he had recovered from injuries that should have killed him.

At first, Seraphae believed the event to be a miracle.

She soon discovered that the effect could be repeated. Blood taken from her body retained an extraordinary capacity to stimulate growth and repair in living tissue. Wounds closed, broken bones knitted together, and patients suffering from illness recovered at an unnatural rate.

More remarkable still was the effect her blood had upon those suffering from paralysis.

Patients who had lost the use of their limbs began moving again after receiving her blood. Their damaged nerves appeared to regain function, and muscles that had wasted away gradually strengthened.

As her abilities developed, Seraphae no longer required a conventional transfusion. She could direct her blood into another person through physical contact, stimulating the tissues and nerves within them. During these treatments, the vessels beneath her skin shone with brilliant white light, while brief golden flashes travelled along the nerves of both healer and patient.

Word of her gift spread rapidly.

People travelled considerable distances seeking treatment, and Seraphae willingly exhausted herself to help them. However, as knowledge of her abilities reached those in positions of authority, her work ceased to be entirely voluntary.

She was placed under the supervision of local nobles and physicians, who soon discovered that repeated treatments with her blood could sustain the lives of individuals who should have died years earlier.

The elderly regained strength, the terminally ill survived, and those suffering catastrophic injuries could be kept alive through repeated infusions.

Unwilling to lose such a valuable resource, her keepers began restricting her movements.

Eventually, the nobles had Seraphae restrained in chains, and their physicians subjected her to continuous blood extraction. When her body weakened, they permitted her a brief recovery before beginning again.

For years, she was forced to sustain people whose bodies had long since reached their natural limits.

Yet the more her abilities were exploited, the less predictable they became.

At first, the changes were subtle. Wounds healed with excessive scar tissue, broken bones grew thicker than before, and formerly paralysed patients developed involuntary movements.

Then the growth became violent.

Seraphae's blood no longer distinguished between necessary repair and unchecked proliferation. Small injuries produced swollen masses of tissue. Muscles contracted without instruction. Nerves multiplied through regions of the body where they had no proper function, producing unbearable sensations and continuous convulsions.

Those who received substantial quantities of her blood sometimes suffered such rapid internal growth that their bodies ruptured under the strain.

Others survived, but were left covered in luminous sores, their muscles and nerves continually stimulated by tissues that refused to become still.

Her keepers attempted to reduce the frequency of her treatments, but by then Seraphae's abilities had begun operating independently of her will.

Her own veins glowed continuously, and the radiance spread outward from her body. Plants flourished in her presence, insects multiplied, and living tissue began responding to her proximity alone.

Even approaching her became dangerous.

The chains surrounding her were reinforced, but the light continued escaping. Her body gradually became a mass of uncontrolled vitality, continually growing, repairing, and transforming itself. When portions of her flesh were damaged, they regenerated almost immediately, often returning in distorted and unfamiliar forms.

The people who had once celebrated her as a miracle came to fear her as a living catastrophe.

Unable to kill her, they constructed a mausoleum equipped with heavy restraints and specially prepared containment anchors. Seraphae was chained within its deepest chamber, where her blood and radiance could no longer reach the outside world.

The Hematic Order eventually came to recognise her as the Chain Saint, an extraordinary manifestation of the Lux and Ductilis tendencies whose powers had exceeded the body's capacity to regulate them.

Her imprisonment was preserved rather than disturbed, and the chains surrounding her became objects of reverence in their own right.

Within the mausoleum, Seraphae continues to radiate life.

Her wounds heal as quickly as they form. Her veins shine through her pale flesh, and the nervous impulses travelling through her body never cease.

It is said that she still reaches toward anyone who enters her chamber, compelled by the same instinct that once drove her to help the sick.

But there is no longer any distinction between her desire to heal and the terrible growth that follows.

**Seraphae can no longer allow anything living to remain as it is.**

**Seraphae at a glance**

- **Original character:** A compassionate nurse who genuinely sought to restore life and movement.
- **Tendencies:** Lux is the white light in her vessels and the mending in her blood. Ductilis is the nerve impulse: the gold flashes along the nerves, the restored movement, and later the convulsions.
- **Source of horror:** Exploitation turns directed healing into indiscriminate growth and nervous excitation.
- **Fate:** Chained and bled by the nobles and physicians who kept her, then sealed away by her own people once her blood and presence became dangerous.
- **Environment:** Heavy chains, containment anchors, mausoleum, inescapable illumination.
- **Visual direction:** **White** radiance specifically within the blood vessels for Lux; separate **gold/yellow** flashes for Ductilis nerve activity; pale flesh and restraints.
- **Thematic sentence:** *Seraphae restored.*

**Appearance:** A saintly female figure with open, branching wing-like nervous projections and ruined, flowing vestments. Her halo and radiance frame a gentle face; visible chains bind her body and wings. The restrained, winged saint remains the defining silhouette even as her light grows unstable. Her wings recoil before tearing open in waves of light; dispersed radiance takes the shape of white axon threads with brief gold nerve impulses. As containment tightens, braided light runs from the anchors to her wrist restraints, drawing her wings and hands inward.

**Trial / Encounter:** Seraphae's trial room is still WIP. Waking her starts a containment fight, not an execution. The chamber floods with the light she emits, which burns the player. She cycles through fracturing, dispersal and condensing: the player avoids her light, binds the fragments her radiance breaks into, activates the containment anchors, and strikes during condensing windows. Each of these raises containment integrity, which decays if left alone. Full integrity chains her radiance back into restraint and returns the room to darkness.

**Ending:** Seraphae is immortal by nature and can only be contained. When integrity is full, the anchors draw her wings and hands inward, the sarcophagus is restored, and she is chained inside it again, still radiant and still reaching.

### 10.4 Putriciel — The Befouled Saint

*Saint of Rot and Flame · The Failed Absolution · The Everlasting Blight*
**Tendencies:** Mortem + Flammeus
**Canon Memory:** Bloom of Rot
**Pronouns:** he in life; **they** now. Putriciel was a man, but the rot has malformed them into an agender monstrosity.

Putriciel was once a wandering hemomancer who possessed an unusual affinity for fire.

Unlike others who used Flammeus manipulation to burn enemies or destroy obstacles, Putriciel discovered that the heat produced through his blood could be directed with extraordinary precision.

By controlling the temperature and movement of his blood, he could burn away diseased tissue, destroy fungal growths, and cleanse objects that had become contaminated by unnatural biological influences.

His earliest work involved treating infected wounds and destroying diseased material. However, he soon discovered that his abilities extended beyond ordinary infections.

He could cleanse objects that had become overgrown with Erythrocytic Mycelium, remove fungal infestations from buildings, and burn corruption from soil without necessarily destroying the surrounding land.

His tools were censers: hanging vessels in which his blood smouldered, so the cleansing heat could be swung through a room or carried across a field. Smaller things he took into his open hand, holding them out as an offering while the fire worked through them.

These abilities made him highly sought after.

Communities brought him contaminated relics, infected tools, and materials recovered from places where the fungal growth had become particularly aggressive. When an infestation spread across a field or settlement, Putriciel was often summoned to destroy it.

He became known as a purifier, a man capable of restoring things that others considered permanently lost.

Yet Putriciel did not fully understand what his abilities were doing.

His cleansing flames were not simply destroying the biological material they encountered. In order to burn so precisely, he was drawing portions of the infection into his own blood, where his Flammeus manipulation appeared to consume them. Every cleansing fed him a little more of the corruption he removed.

For years, the process seemed harmless.

Eventually, small dark wounds began appearing across his body. They resembled burns that had healed poorly, with thick, leathery surfaces similar to eschar.

Putriciel dismissed them as an unavoidable consequence of his work.

However, the wounds did not heal.

Instead, their edges gradually hardened, and pale fungal tissue began growing beneath them. Small shelf-like protrusions emerged from his back and shoulders, accompanied by discoloured patches of skin and a persistent earthy smell.

At first, he attempted to destroy these growths using the same methods he employed against external infections.

The affected tissue burned away, but the growths returned.

Worse still, each attempt at cleansing himself seemed to drive the infection deeper into his body.

Putriciel continued his work despite his deteriorating condition, believing that he could eventually develop a method of purifying his own blood.

As the years passed, his abilities became increasingly unreliable.

Objects he attempted to cleanse sometimes developed new fungal growths days later. Soil that had appeared restored began producing strange fruiting bodies, and wounds treated with his blood occasionally became infested with unfamiliar organisms.

His flames still destroyed visible infection, but the underlying contamination remained.

Eventually, Putriciel became so thoroughly colonised that his body could no longer distinguish between the infection he sought to destroy and the fungal tissue sustaining his own life. The corruption he had fed on for years overran him.

Somewhere in that change the man was lost. What remained was no longer he; the Order's records call Putriciel they.

The shelf-like growths expanded across their torso, back, and limbs. Their skin took on sickly shades of ochre, pale yellow, and decaying green, while thick fungal structures forced their body into a permanently hunched posture.

Their blood became a medium for the very infection Putriciel had spent a life combating.

Any organic material brought near them began to change. Wood softened and sprouted unfamiliar growths. Plants developed swollen stems and distorted leaves. Living creatures exposed to their blood suffered rapid fungal colonisation, their tissues becoming hosts to aggressive new growth.

Eventually, the infection overwhelmed their conscious control.

Their Flammeus abilities almost entirely disappeared, leaving a shambling source of contamination. Yet fragments of the original will remained buried beneath the proliferating fungal tissue.

At irregular intervals, Putriciel would regain enough awareness to recognise what they had become.

During these moments, they attempted to purge themselves.

Their blood would suddenly ignite, and tremendous waves of heat erupted through their body, burning away the fungal protrusions and consuming the infected tissue surrounding them.

For a brief period, the original form could sometimes be glimpsed beneath the charred remains.

But the cleansing never lasted.

The infection had become too deeply integrated into their biology. The surviving fungal structures absorbed the heat and rapidly regenerated, emerging from the burned tissues in even greater quantities.

Each resurgence left Putriciel less recognisably human than before.

Attempts by others to destroy them through fire proved equally unsuccessful. Flames were absorbed into the network of infected tissue, feeding the dormant Flammeus power within until it erupted outward in violent bursts.

Eventually, those who had once sought the purifier's aid were forced to isolate Putriciel within a fortified mausoleum, surrounded by stone and materials resistant to fungal growth.

The Hematic Order later recognised Putriciel as a Saint, not for the success of the purification, but for the extraordinary transformation that had occurred within that blood.

Putriciel had become a living union of decay and combustion, two opposing biological processes endlessly consuming and renewing one another.

Their resting place remains overgrown with thick fungal shelves, pale moulds, and sickly vegetation.

Most of the time, Putriciel lies almost entirely concealed beneath the growths, the original body barely distinguishable from the surrounding infestation.

Occasionally, the fungal mass begins to tremble.

Heat builds within the chamber, the growths blacken, and the faint outline of a man emerges from beneath the accumulated decay.

For a few moments, Putriciel remembers what the purifier once sought to accomplish.

Then the fire dies.

And the rot begins again.

**Putriciel at a glance**

- **Original character:** A purifier whose blood-fire removed infection from people, objects and places, Erythrocytic Mycelium included.
- **Tendencies:** Mortem is the rot that colonised them. Flammeus is the purifier's fire, which now surfaces only in brief cleansing eruptions.
- **Tools:** Censers that carried the cleansing heat, and the open offering hand into which contaminated things were placed.
- **Source of horror:** Each cleansing quietly drew infection into his blood until it overran him and he became a source of violent new growth.
- **Fate:** Mostly subsumed by fungus, with fleeting returns of agency expressed as self-cauterising fire.
- **Environment:** Eschar-like surfaces, pale fungal shelves, infected organic material, a stone-lined containment mausoleum.
- **Visual direction:** Ochre, olive, mould-green, muted yellow, pale cream, blackened charcoal. **Avoid the predominantly red palette of other infections.** The current model's crimson vestments predate this direction; the palette pass is planned.
- **Phase direction (planned):** **Rot/infection → dedicated cleansing-fire interval → rot/infection.** Fire should **not** be constantly active through the ordinary infected phases.
- **Thematic sentence:** *Putriciel purified.*

**Appearance:** A walking reliquary of rot and spoiled offerings. A tall, tarnished open crown rises above a hollow, vine-screened face. Ruined olive and ivory vestments hang beneath colonies of layered shelf fungi and swollen amber spore sacs. One corrupted hand extends in offering, trailing thick olive rot and fungal cords; the other carries an ornate staff hung with dripping censers. Their sacs swell out of rhythm as rot gathers through the ground; the release travels through their shoulders, hands and fungal mantle. Small back colonies swell as rot gathers and shrink beneath the brief cleansing flame of absolution. When the rot stills, their open offering palm carries five small fungal nodules that light with each absolution. The body recoils beneath an accepted offering, and the final release leaves spores and fading threads where the saint stood.

**Trial / Encounter:** Putriciel's fight is an absolution trial. Every 120 ticks the spore sacs swell and a rot nova releases, dealing Wither damage and setting players alight across an 8-block area (12 blocks, with stronger Wither, below half health). Every 300 ticks the rot stills for 80 ticks: an absolution window. A hit landed while the window is open counts as an absolution and lights one of the five nodules in the offering palm. Lethal damage outside that condition is clamped, so Putriciel cannot simply be beaten down. Five absolutions complete the trial.

**Ending:** The fifth absolution purifies Putriciel, and they rest for a time. The growths blacken, the outline of the man shows for a moment, and they sink back into the restored sarcophagus. It never lasts: the rot always reclaims them.

### 10.5 Velorum — Saint of the Endless Hour

*The Still Saint · The Perfected Flesh*
**Tendencies:** Congeatio + Tenebris
**Canon Memory:** Endless Hour

Velorum was once an ascetic belonging to a secluded religious order whose teachings held that the body's natural desires were the source of all human suffering. Hunger, lust, exhaustion, pain, and even the simple need for warmth were considered weaknesses that prevented mankind from achieving a higher state of existence.

After becoming a hemomancer, Velorum discovered that he could manipulate the temperature and circulation of his own blood, slowing his heartbeat and suppressing his body's natural functions. Through years of meditation and experimentation, he learned to remain motionless for weeks without food, sleep, or warmth.

He came to believe he had discovered the means to perfect the human body.

His teachings gradually attracted followers, many of whom willingly submitted themselves to his experiments. Velorum learned to extend his influence into others, slowing their circulation until their bodies became cold and motionless, preserved against hunger, aging, and disease.

But while their bodies remained perfectly still, their minds did not.

His followers described being trapped in an endless darkness, unable to move or speak, with little sense of how much time had passed. Some begged to be released. Others went mad within their motionless bodies.

Velorum dismissed these complaints as the final resistance of an imperfect mind, insisting that true enlightenment required the surrender of every earthly desire, including the desire to move.

As his influence grew, he began preserving people without their consent. He arranged his followers into elaborate formations throughout his monastery, treating their motionless bodies as living monuments to the perfection he believed he had achieved.

Eventually, Velorum subjected himself to his greatest experiment, suppressing his own circulation until his blood barely moved and his body became permanently cold. His flesh grew pale and rigid, frost gathered across his limbs, and the shadows surrounding him deepened until light itself seemed reluctant to enter his presence.

He no longer experienced hunger, exhaustion, or the passing of ordinary bodily time.

Yet his mind remained entirely conscious.

When the remaining members of his order attempted to destroy his work, Velorum extended his influence throughout the monastery, arresting the circulation of everyone within its walls.

Centuries later, he remains seated within his frozen sanctum, surrounded by hundreds of perfectly preserved devotees. Some kneel in prayer, others stand with their arms outstretched, and many remain frozen in the desperate positions they assumed while attempting to escape.

Velorum considers every one of them a success.

Any living creature that approaches him gradually feels its warmth fade and its movements slow. Those who remain too long are condemned to join his congregation, preserved indefinitely within their own bodies.

To Velorum, this is neither imprisonment nor cruelty.

It is perfection.

**Velorum at a glance**

- **Original character:** An ascetic convinced that bodily desire and change caused suffering.
- **Tendencies:** **Congeatio** suppresses circulation, warmth and metabolism; **Tenebris** isolates awareness and sensation.
- **Source of horror:** He consciously perfected techniques of stillness and forced that perfection on other people.
- **Fate:** Lucid and convinced of his righteousness; not a caretaker, not a martyr, and not an accidental casualty of his power.
- **Environment:** Frozen monastery, unmoving worshippers, long shadows, frost, diminished light.
- **Visual direction:** Ice-blue and pale frost against black and deep shadow; dignified rigidity rather than frenzy.
- **Endless Hour is not literal time manipulation.** Time stretches only as the body and senses experience it.
- **Thematic sentence:** *Velorum preserved.*

**Appearance:** A tall, still ascetic whose dark face recedes beneath a deep plain hood. Sober charcoal vestments hang close to the body, bound by a knotted cord, with a short frost-edged mantle. A spare iron halo frames the hood, its hanging links crusted with frozen cruor. One skeletal hand rises in a crooked invocation; the other grips a tall lantern staff. Blue-white crystals shine inside open lantern cages at the staff's crown and along hanging chains. Cold gathers in fractured seams under his feet before the raised claw releases it as leaning black-ice splinters. Darkness rises in narrow folds around the lantern light, and Silence draws pale threads inward through the crooked hand. He bows beneath a wound without losing the invocation; when the body fails, the hood bows down and the remaining frost falls out of the light.

**Trial / Encounter:** Velorum fights from stillness. Every 100 ticks a frost nova congeals the cold around him, damaging, slowing and briefly rooting players within 10 blocks. Every 160 ticks a Veil of Darkness blinds players within 12 blocks; blood-active players also suffer Nausea as their blood recoils from the silence. Every hit that lands earns him a short burst of Resistance I, so attacks fall into a rhythm around those windows. At a quarter health or less, Silence draws 200 blood every 80 ticks from blood-active players within 8 blocks.

**Ending:** Velorum cannot be killed. When his body fails, the veil folds down, the frost falls out of the light, and he reverts to his dormant state. The sarcophagus is restored and closes over him, and he sits inside it as lucid and as certain as before.

### 10.6 Comparing the Saints

**Gifts, obsessions and transformations**

| Saint | Original motivation | Original capacity | Escalation | Ultimate corruption |
|---|---|---|---|---|
| Hemorath | Curiosity that becomes greed, power and consumption | Absorb blood and command its iron | Deliberately consumes more than his body can bear | Cannot stop taking blood, even as it leaks from him |
| Seraphae | Compassion and restoration | Transfuse glowing, healing blood and restore nerve function | Nobles and physicians chain her and bleed her | Heals with lethal excess; growth and nerve impulses become indiscriminate |
| Putriciel | Purification and the destruction of blight | Cleanse living tissue, relics and land with controlled fire | Each cleansing draws infection into his own blood | Becomes a fungal infection with momentary eruptions of cleansing will |
| Velorum | Ascetic discipline and perfection | Suppress circulation and bodily need | Widens self-imposed stillness into a compulsory practice | Preserves people in conscious sensory imprisonment |

**Will and mental state**

| Saint | Awareness and agency after transformation | Tragic or moral distinction |
|---|---|---|
| **Hemorath** | Dominated by appetite, which began as greed but became biological necessity | His ruin follows choices already questionable from the start |
| **Seraphae** | Retains the impulse to help, but the gift acts without restraint | A genuine healer exploited until healing becomes monstrous |
| **Putriciel** | Largely subsumed by infection; the original will briefly surfaces during fire-cleansing | The purifier repeatedly tries to burn out what they have become |
| **Velorum** | Lucid, deliberate and convinced of his teaching | He is not out of control; the horror lies in his certainty |

**The four pathological extremes**

| Saint | Core biological process | Perverted endpoint | Short thematic formulation |
|---|---|---|---|
| Hemorath | Blood production, circulation, iron | Unending absorption and leakage | **Hemorath consumed.** |
| Seraphae | Healing, growth, nervous activation | Uncontrolled regeneration and stimulation | **Seraphae restored.** |
| Putriciel | Cleansing, combustion, decomposition | Blight that survives every purge | **Putriciel purified.** |
| Velorum | Temperature, metabolism, sensory response | Conscious life without movement or release | **Velorum preserved.** |

**Why each Saint returns to the tomb**

| Saint | How the encounter ends | Why the Saint survives it |
|---|---|---|
| Hemorath | Overloaded and exsanguinated, or worn down | The marrow never stops producing blood; he reforms in time |
| Seraphae | Containment integrity reaches full and she is chained again | She is immortal by nature and can only be contained |
| Putriciel | The fifth absolution purifies them | They rest for a time, until the rot reclaims them |
| Velorum | His body fails | He reverts to his dormant state, unchanged |

**Encounter and visual differentiation**

| Saint | Dominant visual language | Environmental storytelling | Fight identity suggested by lore |
|---|---|---|---|
| Hemorath | Dry, sunken anatomy; iron-hard bone; restrained blood-red | Basins that meter blood, dry sarcophagus, stains and iron residue | Blood drain, iron, blood debt, hunger and replenishment |
| Seraphae | Ivory, white-lit veins, distinct golden nerve flashes, chains | Impossibly vigorous growth held back by restraints | Luminous overgrowth, nerve convulsions, dangerous healing |
| Putriciel | Fungal green, cream, ochre, yellow, eschar-black; little red | Successive fungus and ash layers record repeated failed purges | Infection/rot phases **separated by** a dedicated fire-cleansing phase |
| Velorum | Pale ice, dark shadow, rigid solemn silhouette | A preserved congregation, a light-starved frozen sanctuary | Slowing circulation, darkness, immobility and the threat of permanent stillness |

> These fight identities are design implications, not a claim that every corresponding attack is implemented. The current encounters are described in each Saint's **Trial / Encounter** paragraph.

### 10.7 Continuity Notes

- **Hemorath is a powerful early Hemomancer, never a true Harbinger and not an Archon.** He predates the mature Hematic Order and Erythravane's Fane; Erythravane was the first Vicar and the first Archon. Older notes that call Hemorath the first Harbinger, one of the original great Archons, or a purely Ferric Saint of permanence are superseded.
- **No martyrdom framing.** No Saint is a martyr, and no mausoleum room is a penance. The fall each Chapel recalls is the Saint's downfall to the power of Hemomancy.
- **Mausoleums are consecrated, not holy.** Hallowed ground is the Saints' own residue rejecting the hive's will, not divine favour.
- **Do not depict Hemorath only as bloated.** His historical excess produced that condition, but his resting silhouette is frail, skeletal and desiccated. His marrow remains active.
- **Do not make Seraphae a voluntary villain.** Her tragedy depends on the nobles and physicians who chained and bled her, and on her inability to restrain an originally beneficial gift. Keep white Lux light in the vessels distinct from gold Ductilis flashes along the nerves.
- **Do not make Putriciel a generic red-fungus fire boss.** Their infection has its own green/yellow/ochre/shelf-fungus palette. Fire is a brief, distinct *cleansing* interlude between infection phases, not a constant appearance. Use he for the living purifier and they for the Saint now.
- **Do not make Velorum another nurse, plague healer or grieving caretaker,** and do not make him a martyr. His motive is bodily perfection through stillness. He chooses to impose it and remains lucid.
- **Do not give Velorum literal chronomancy by accident.** Near-time wording such as the Endless Hour or a held moment is fine, but the effect is experienced through arrested circulation, immobility, darkness and sensory deprivation.
- **No Saint dies.** Every ending returns the Saint to its restored sarcophagus, for the reason in its entry.
- **No Saint is assigned at random.** A sarcophagus holds the Saint it was built for, or waits inert until a relic names one.
- **Do not assume canon recognition dates.** Specific centuries, how the Order formally recognised each Saint, and the detailed chronology of the tombs remain open unless separately specified.
- **Keep the moral contrasts:** Hemorath's greed and Velorum's conviction are conscious moral failings; Seraphae is exploited; Putriciel's attempts at cleansing create and intensify the corruption they sought to stop.

### 10.8 Planned Per-Saint Overhauls

The deferred list lives in [the Saint reconciliation spec](superpowers/specs/2026-10-26-saint-reconciliation-design.md) under "Deferred per-Saint work". Much of it is now built: Hemorath's passive drain and leaking blood meter, Seraphae's growth and nerve hazards, Putriciel's dedicated cleansing interval, and Velorum's stillness meter arrived with the Saint power pass, and each Saint now has its own mausoleum complex with Chapels, a ring door and hallowed ground. Still open: Velorum's wakeable congregation (which fights beside a Silentium who woke him properly and against an Apotheos who forced the door), Hemorath's swelling as an enraged fight goes on, and the Saints' gifts. Those gifts will replace the four Canon Memories, each rediscovered by weaving the Saint's Hallowed Residuum with a Recollection recovered from the Gardens' Archive.

---

## 11. The Stained Priestess — Annetta Knowles

Annetta Knowles is a boss character and one of the more tragic figures in the mod's lore. She was one of the **highest-ranking members of the Unstained**, a cleric who had given her life to purification and the service of Our Lady.

Her downfall came from a fact she never understood: **Tooth Pecks sought her out** inside the church and would not leave her alone. Tooth Pecks seek active infection rather than the mundane residue carried by most people, but Annetta never knew she was infected.

This one small fact — an insect bite that should not have been possible — **shattered everything she had built herself on**. If she could be bitten, she was infected. If she was infected, had she ever truly been clean? Had any of her purification been real? Had she been walking the Lady's path, or had she been deluding herself while the infection marinated inside her all along?

Her sanity broke under the weight of this question. And in that fracture, the latent blood power she had been suppressing for her entire life — through years of purity work, through complete devotion — came flooding out all at once, released by the psychological collapse of her defenses.

**What is buried in her:** An infection hidden beneath the silver of the Unstained. On the Harbinger route it erupts, kills Annetta, and continues through her remains as the Stained Priestess. Its spear is hyper-solidified blood, not an ordinary Hematic Order weapon.

**Her thematic domain:** Teeth, nails, hair — the biological materials that are *not* blood-connected (calcium, keratin) but are undeniably biological. These fall into neither the Harbinger (blood, iron, bone) nor the fully Unstained (copper, silver) domain. They are hers.

**Draught of Still Mercy** — a specialized Unstained-crafted tincture that can suppress the latent infection in a host who has not yet fully converted. Only an Unstained practitioner with sufficient Clarity can use it to externalize Annetta's infection so it can be fought separately rather than burning through her. This is not a gentle cure, but it leaves her alive and, eventually, purified.

> *She is not the Pale Lady.* Our Lady of Still Waters is a force of nature, non-physical and non-corporeal. Annetta is an Unstained woman unknowingly carrying an enormous and terrible infection.

**Implementation status:** Partial. Annetta's encounter is wired and playable. She spawns in COWERING state inside a `BrokenChurchStructure` with contextual scene dressing (a ToothPecks Specimen Jar placed beside her, Devil's Tooth decorations). Two routes are implemented:

- **Harbinger route**: Approach holding a Tooth Pecks specimen jar and unseal it. The Tooth Peck latches onto her neck as a tick and she fights in a frantic Phase 1 with her dagger, Still arts and her Unstained Shield, crying out without ever understanding what is happening to her; infected blood and Unstained humor are knocked out of her into lasting pools. When the player has lasted two minutes and dealt fifty damage she falters, drops her shield, drives her dagger into the swollen tick, and the burst takes her head. Her body falls, the blood pools and climbs it, and it rises as the Stained Priestess: tendril wings, a thorned head with three yellow eyes, the red-and-blue lancea drawn from the pool. The Priestess never speaks; she flies, swoops, lands to fight with the spear, shields herself in her wings and throws a sickly wind. Defeating her unlocks Severed Covenant knowledge for a founder and drops `Annetta's Sanguis Lancea`.
- **Unstained route**: Offer her the Draught of Still Mercy after unlocking Clarity. She drinks; the red drains out of her hair and the **Latent Infection** tears itself out of her back. Weakened, with only her dagger, she fights beside the player: she heals them, strikes the infection with still water and steel, and reels when it remembers her. When the infection is destroyed she survives in the RESOLVED state and will talk about how long it was in her and what she will do now. The route drops `Annetta's Absolution Dagger` and Pale Silver Ingots.

After either route concludes she either lies broken (Harbinger) or stands resolved and at peace (Unstained).

**How she looks.** When found she is almost entirely human: a high cleric in white robes threaded with pale silver, a short mantle closed by a consecrated copper crescent, verdigris fittings and a high collar. Her only tells are **long red hair and gold eyes**. Ordinary Unstained lose their pigment (albino or bald, grey-blue eyes), so an attentive player should feel something is wrong before anything happens. In the Harbinger Phase 1 fight she keeps that look, carries her own Absolution Dagger and an Unstained Shield, and a Tooth Peck swollen into a tick hangs from the side of her neck, fattening as the fight goes on. Purged on the Unstained route, she becomes one of her order: **short white hair, blue eyes**, the same vestments cleaned, only her dagger in hand. She is still weak, but she reaches out to heal the player while the infection is fought.

The **Stained Priestess** is Annetta's headless body after the infection wins: robes in blood-soaked tatters, no copper left on her, a ball of thorns and tentacles where her head was with three glowing yellow eyes, crude wings torn from her back, each a vessel arm with writhing finger tendrils and torn veined webbing between them (about five blocks a side), tendrils spiralling her arms and dragging from her hem and the red-and-blue Sanguis Lancea in her hand. The **Latent Infection** is gaunt, only vaguely humanoid and about five blocks tall. A pointed bone cowl arches over an empty face and runs down into a toothed maw opening on its chest around a red core; ropes of her red hair hang from under the hood. A ragged skirt of hanging vessel strands hides a serpentine lower half whose tail trails over the floor shedding roots and mycelium. Its far-left arm is twisted vessels ringed with vertebrae, coiled until it lashes; two skinny right arms end in long black hooked claws and grip one gnarled lance. It is crimson fungus and vessel mottled with ivory, the last of her robes and bones.

**Remaining WIP:** Annetta-specific thrown projectile rendering (the thrown lancea still shares the standard blood-lance path) and in-church balance passes.

---

## 12. The Qliphoth — Tree of Death

In Kabbalistic tradition, the **Sephiroth** are the ten attributes/emanations of God arranged in the **Tree of Life**. The **Qliphoth** are their opposites — the Tree of Death, the shells or husks, the inverse emanations.

Hemomancy draws on this concept not as good-versus-evil but as complementary forces. The **Qliphoth Tree** summoned by the Bloom of the Qliphoth ritual is not an evil thing. Fungi are the cleanup crew of life — they are not bad because they consume the dead, just as ravens and vultures are not evil for doing the same. They simply occupy the other side of the cycle.

The tree is **will and antenna** at once. It manifests the Archon's own will, grown out of the Monolith's fragment and fed by their blood, and it is also a beacon to the hivemind, the place where the hive's signal reaches them. That is why cutting it means so much. **Severance** is the Archon deliberately cutting their own tree in the Rite of Pruning: a physical ego death that severs their ties to the hivemind as a whole and sets them on Vesper's path. Severance is an act, not a location.

The Qliphoth Tree:
- Provides **blood volume regeneration** and enhanced regen auras in its area
- Produces **Qliphoth Pome** fruits — void-dark fruits grown around a crystallized blood core. Each pome is one of nine husks of the Qliphoth. Eating one causes a brief expansion of awareness into the void-register — perceived from the outside as darkness — alongside a surge of blood power and reduced manipulation costs. Nine pomes drop from a single tree's lifecycle, each corresponding to one of the nine corrupted shells of the Qliphoth system. Consuming the ninth pome from one bloom completes Communion and forces the Fungal Spine from the Archon before either endgame rite.
- Whispers through those fruits in a personal register — the voice naming the husk feels directed to the current bearer, not to the crowd at large.
- **Must be summoned through the Bloom of the Qliphoth rite**: the rite registers the owner and nine-pome lifecycle. Placing a Bloom block alone does not establish that bond or begin Communion. This is a blood-memory relationship, not a divine prohibition.
- Grows with slight random variation each time (no two trees are identical)
- Cannot be felled by ordinary hands. After Communion and the choice of Silence, any of the seven base Living Weapons can hook inward into a temporary Living Sickle. Its deliberate second cut opens the owned Bloom as Vesper's wound rather than deleting the tree. That cut is the Severance.

**The three special floors.** The rites that stand apart are not laid on the common floors.
- The **inverted tree**, for the Bloom: Calcified Hyphae ground walked by hyphae paths, with nine husk-lights on three pillars, one for each Pome to come. The floor is drawn as the tree it will raise, upside down, its roots already reaching past the edge.
- The **Hush**, for Apotheosis: pale venous stone in rings that close in on the Focus, so that every step toward the centre is quieter. The membrane over it pales and its heart barely beats. Apotheos is the final hush behind the Order's mask, and the floor rehearses that silence.
- The **lineage**, for Ancestral Communion: amber threads of Mnemonic Ambergris running from a ring of the near dead out to the corners, framed in blood wood. The memory carried in the blood runs outward along them to wherever it goes.

**Rite spaces.** A floor that has carried a rite to completion keeps the space above it. It is not holy ground. It is ground that remembers what was done there, and some things will only answer where that memory is fresh. The Sanguine Omen is one: its portent reads a Dominion floor's memory of the world being changed, and says nothing elsewhere.

---

## 13. Infection Biology & Society

### 13.1 The Nature of the Dormant Infection

The fungal infection in a person exists on a spectrum. Most people who carry it are completely unaware. It can remain dormant for an entire lifetime, triggered only by stress, proximity to active infection zones, or the rare chance encounter with enough spores to activate it fully.

**The Harbingers** (those who've been deliberately initiated) differ from those naturally infected in a subtle but narratively important way: initiated Harbingers received a concentrated dose from a Hermit's heart — they are, in a sense, "clean" in that their infection was invited and formal. Naturally infected people carry whatever the wild spores gave them, which may be different in character — more volatile, less predictable, potentially harder to fully cleanse.

There is a suggestion that natural infection is like shingles: even if you cleanse yourself, something may remain dormant, waiting for the right trigger to re-emerge.

### 13.2 Bloodline vs. Biological Family

**"Bloodline"** in the game's mechanics refers to the **Harbinger Covenant** — a sworn group of practitioners who share a blood pool, communicate secretly, and form a chosen family. This has nothing to do with biological lineage.

Because it is a covenant rather than genealogy, membership can be ritually severed by the bloodline progenitor; "family" here is oath-bound, not irreversible by birth.

The Hematic Order implicitly expects its members to separate from their biological families — not cruelly, but practically. A Harbinger's life, worldview, and community are fundamentally different from a typical person's. It is easier for both parties if the Harbinger joins their new family fully.

The fungal infection is unlikely to pass biologically from parent to child in any reliable way. The fungus likely:
- Sterilizes or reduces interest in reproduction in heavily infected individuals
- Would harm a fetal or very young host more than help them
- Views creating new human hosts through birth as simply "too slow" compared to direct infection or initiation

### 13.3 How Society Views the Harbingers

Common folk view Harbingers on a spectrum:
- **Best case**: "There's that weird fungal area, and those strange people. Bit creepy, but probably fine."
- **Typical**: Discomfort, avoidance, treating them as taboo
- **Worst case**: Fear, hostility, viewing them as dangerous cultists

The taboo exists for the same reason most taboos do: things associated with blood, bodily fluid, and death trigger instinctive revulsion in most people. The historical incidents — blood magic used in anger, accidents during awakening — have given the Order a reputation they cannot fully escape.

---

## 14. Material Philosophy

Each faction's relationship with materials reflects their worldview.

| Material | Faction | Significance |
|----------|---------|--------------|
| **Hematic Iron** | Harbingers | Blood-fused iron — the organic and the mineral made one. The body's mineral content, weaponized. |
| **Barbed Splinter** | Harbingers | Shared Barbed Urchin and Vampire Squid material used for the retaliatory armor fork: bone-like, thorn-like, and still animal enough to imply that defense can wound back. |
| **Crimson Lacquer** | Crimson Lodge | A ritual finish rather than a simple paint: blood-crystal polish, ferric powder, and salve sealing Barbed or Chitinite ancestry into the disciplined hunger of Blood Lust. |
| **Chalybeate Sclerite** | Harbingers | Iron-sulfide armor scale from deep vent snails; a Ferric ocean expression of the same living-mineral philosophy behind hematic iron. It is best taken by careful knapping from a withdrawn living animal, not by slaughter. |
| **Monolith Imbued Cloth** | Silent Archons | Cloth threaded with Monolith Fragment burden and enthralling filament. It belongs to Archons who reject Apotheos: not purity, not victory, but the decision to keep wearing the truth without dissolving into it. |
| **Memory of Vesper** | Silent Archons | Guaranteed loot-table drop from Vesper's Evening Star phase. It is burned directly in the Iron Brazier and drawn into the existing Living Staff, permanently teaching the inherited `conjure_sickle` form and strengthening Vesper focus. Taking it in is what makes the Archon a Degree-8 Silentium. |
| **Mycophant Tendril** | Apotheos Harbingers | Guaranteed loot-table drop from The Mycophant and Charm of Vascularium rune-slot reward. It symbolizes chosen mycelial embodiment and visibly fungalizes the whole player while equipped. |
| **Erythrocoral Fragment** | Harbingers | Warm-ocean fungal-coral tissue: a Vivacious expression of hemomancy as ecology rather than conquest. It supports spore craft through careful shearing, not reef-clearing extraction. |
| **Vivianite** | Harbingers | A blood-adjacent mineral idiom used by the Order for specialist craft and instrument work; treated as part of Harbinger material culture rather than neutral trade stock. |
| **Living materials** (bone, chitin, organic tools) | Harbingers | Blood magic makes materials *alive* — tools that feed and grow. |
| **Copper** | Unstained | Antiseptic in nature; the Unstained discovered it historically staved off illness. The foundational material of their practice. |
| **Oxidized Copper (Verdigris)** | Unstained | The "sacred" form — copper that has absorbed so much corruption it has been consecrated by the act. To carry fully patinated copper is proof of survival and devotion. |
| **Pale Silver** | Unstained | Consecrated copper refined through purification ritual — the highest-tier Unstained material. Does not occur in nature; must be made. |
| **Gold** | Neutral (both factions, neither primarily) | Biocompatible — can be embedded in the body without harm — but does not *heal* or *purify* the body. Useful, but not sacred to either side. Primarily a utilitarian and economic material. The Chthonian Queens are the only creatures in the mod associated with gold, by virtue of their royalty. |
| **Teeth / Nails / Hair** | Annetta Knowles' domain | Calcium and keratin — biological but neither blood nor copper. These are the materials of Annetta's awakened power. |
| **Bone** | Harbingers | Living bone produces blood — bones are alive, and the Harbingers claim this domain. |

**On Gold:** The Unstained view gold as a symbol of gross opulence. The Harbingers see it as unnecessary but somewhat beautiful. Both factions *can and will* use gold when needed, but neither considers it sacred. It is the material of trade, of clocks, of technology — not of power.

---

**Faction architecture note:** The current alpha building palettes use these material philosophies directly. Harbinger fixtures lean on hematic iron for chains, bars, doors, and trapdoors: blood-fused iron as containment, machinery, and covenant infrastructure. Unstained fixtures lean on pale silver for bells, bars, lanterns, and chains: clean metal, pale light, and ritual restraint rather than blood-conductive machinery.

## 15. Flora of the World

Plants in Hemomancy are designed around real-world plant biology and carry both gameplay and symbolic meaning.

### 15.1 Harbinger/Blood-Side Plants

| Plant | Real-World Basis | Significance |
|-------|-----------------|--------------|
| **Bleeding Heart** (Dicentra) | Real flowering plant | The name speaks for itself — heart-shaped flowers that appear to bleed. Grants Absorption effect. Used to brew the Potion of Sanguine Siphon. |
| **Stinkhorn Fungus** | Real parasitic fungus | Pungent, blood-red, a classic of the "wrong" side of nature. Used in Foul Paste recipes and the Potion of Blood Binding. |
| **Infected Fungus** | Fictional variant | Grants Confusion. Used in Foul Paste and the Potion of Mycorrhizal Mending. |
| **Rafflesia** | Real parasitic plant (world's largest flower) | Extremely rare. A parasitic plant with no leaves, no chlorophyll — lives entirely inside other plants. Deeply fitting for the infection theme. |
| **Puffball Mushroom** | Real fungus | Common fungus, used for Harbinger recipes. |
| **Erythrocoral** | Fictional fungal coral | A warm-ocean shelf ecology where hemomantic tissue behaves like reef life: living mass, pale calcified skeleton, soft fans, and tendrils. It is eerie and biologically wrong to ordinary eyes, but not evil corruption; it is a thriving habitat with its own grazers and drifters. |

**Choir Keeper:** This End bird combines an owl's broad feathered face, paired facial discs, forward-facing eyes, short beak and ear tufts with a peacock's long display train. Its fifteen tail feathers carry Eye-of-Ender markings. A compact feathered neck replaces the former bare vulture neck; its chorus perches and association with Eyes of Ender remain central to its appearance.

**Osteophage Vulture:** Soul Sand Valley fossils serve as roosts for these skeletal bearded vultures. An exposed rib cage, hooked beak, dark beard and narrow feathered wings distinguish them from End Choir Keepers and water-going Venous Striders. They climb above skeleton prey and rake downward in repeated passes, then grind loose bones, including player offerings. Their shared Venous Pinions connect the scavenger niche to the existing strider material rather than introducing a separate vulture reagent. They do not hunt living travelers; Mortem blood reflects their bone-scavenging ecology.

**Ice Fish:** Pale, scaleless, broad-headed schools inhabit cold oceans, frozen rivers and submerged Ice Spikes pools. Their haemoglobin-free circulation gives the Unstained a naturally pure counterpart to Hemolymphopoda's cleansing fluid. They share Cleansing Hemolymph rather than adding another isolated reagent; their sampled blood has the aquatic property and no tendency. Their purity is a biological association, not a new faction or purification gate.

**Pelagic terrain foundation:** Rockpool Shore and Erythrocoral shelves descend into open Pelagic water, then Twilight, Midnight, Carrion and Hydrothermal depths. The descent follows erosion, canyon cutting, sediment deposition and localized mineral venting. Fossil ribs remain static remnants in sediment. Chitons graze the wet shore, colonial Pyrosomes and swift herring inhabit open water, Siphonophores fish the Twilight, crimson-gutted comb jellies drift through Midnight, and Hagfish gather around Carrion bones. Vampire Squids swim through Carrion and Hydrothermal water, their eight arms joined by a crimson web lined with pale inward-facing barbs. Threat makes them fold this web around their mantle and withdraw; their small radial form recalls Naeglerophaeon without its predatory rites. Anemones occupy pools, Bone Worms root on fossil ribs, and pale Giant Tube Worms raise red plumes around heated mineral aprons. These organisms express their own biology: Chiton Ferric, Pyrosome Animus, Herring Ductilis, Siphonophore Congeatio, Comb Jelly Tenebris, Hagfish Mortem and Vampire Squid Tenebris. Habitat does not reassign an existing animal's tendency. This ecology does not establish Court houses, Morphling mechanics, dynamic whale corpses or new progression.
| **Mnemonic Whale** | Fictional reef megafauna | A slow deep-reef drifter treated by Voyagers as a "living archive" of currents, vents, and reef pulse. It hunts squid and Prism Cuttles, shedding one Mnemonic Ambergris after each kill. Crews may supply live prey for repeatable collection while keeping the whale unharmed; field ethics forbid hunting the archive itself. |
| **Qliphoth Pome** | Fictional fruit | A void-dark fruit grown around a crystallized blood core — one of nine husks of the Qliphoth. Eating causes a brief expansion of awareness into the void-register (perceived as darkness), a surge of blood power, and reduced manipulation costs. Nine drop from a single tree's lifecycle; consuming all nine from one bloom is the unnamed act that precedes the Eighth Degree. |

### 15.2 Unstained/Pale Lady Plants

| Plant | Real-World Basis | Significance |
|-------|-----------------|--------------|
| **Lethean Poppy** | Inspired by real poppies (opium poppy aesthetic) | Named for the River Lethe. Chosen because poppies induce sleep/forgetting (opium). Bloom in cold regions and near rivers. Secrete Lethean Dew when harvested, then revert to closed form. |
| **Ghost Pipe** (Monotropa uniflora) | Real plant (called "Indian Pipes" in real life) | Completely white — has no chlorophyll whatsoever. A myco-heterotrophic plant that lives parasitically on fungal networks. An Unstained plant that is, ironically, itself a parasite on fungi. Used in Unstained crafting. |

The mod aims to maintain roughly a **2:1 ratio** of blood-side to Unstained-side content. The Unstained path is a viable alternate route but the primary experience is hemomancy.

---

## 16. Arthropods as Natural Hemomancers

The mod's worldbuilding extends into the animal kingdom: **arthropods** are the world's native practitioners of blood magic. They do not do it consciously, but the same forces that allow blood mages to harden blood into iron, or spin it into chitin, are expressed naturally and instinctively across the insect and crustacean kingdoms.

| Creature | "Blood Magic" Expression |
|----------|-------------------------|
| **Sea Urchins** | Produce Barbed Splinters — the natural equivalent of blood being hardened into barbs. The inspiration for the Barbed armor set and the Urchin Morphling. |
| **Chthonians (termites)** | Grow iron-infused mandibles — essentially, hematic iron that grew biologically. They chew through wood (and eventually wooden tools) with their metal jaws. Spawn in Savanna biome termite mounds; each mound contains exactly one Queen. |
| **Barbed Urchins** | Shallow-water expression of iron blood magic — spined aquatic creatures. |
| **Chalybeate Snails** | Implemented deep-ocean iron snails inspired by the real-world iron-plated snail (Chrysomallon squamiferum). They cluster around hydrothermal vent fields and grow iron-sulfide sclerites that Harbingers prize as Ferric material. Their best harvest is careful knapping while retracted, reinforcing the Order's uneasy covenant with living sources rather than simple predation. |
| **Scarlet Serpents** | Warm-biome blood-serpents with black, red, and yellow warning colors. Their hood flare is taboo folklore in desert and jungle communities, but they are natural territorial wildlife rather than cosmic omens or factional agents. |

**Ocean ecology V1/V2:** Hydrothermal vent fields are the first deep-ocean slice. They are mineral heat-scars on the sea floor, not ore nodes: basalt, blackstone, deepslate, magma, and restrained blood-organic accents where Chalybeate Snails graze. Erythrocoral Reefs are the warm shelf counterpart: fungal-coral habitats with murky red-violet water, Blood Lantern Jellies drifting through the reef, Prism Cuttles, rare Mnemonic Whales moving through deeper shelves, and light Vivacious harvesting. Harbinger Voyager Wrecks are failed reef/vent research craft: covenant field laboratories swallowed by the sea, guarded by Brined Votaries who are tragic remnants of duty rather than a new enemy faction. Active Harbinger voyager expeditions now appear as rare Survey Cog vessels: compact moving laboratories and chapels crewed by a captain-scholar Voyager, with an occasional Votary Wayfarer aboard to learn by witnessing rather than by conquest. Whale-associated Mnemonic Ambergris is an offered or shed sample, including an immediate secretion after the whale consumes squid or cuttles. Crews can supply prey without harming the whale; ambergris remains no prize for killing a living archive. Trade, rumors, and faction tension remain future maritime hooks without flattening Harbingers into villains.

The **Chthonian Queen** is the only creature in the mod associated with gold — a nod to her royal status and to gold's position as a material that sits between but belongs to neither faction.

**Chthonian termite mounds** spawn in Savanna biomes and will contain a small loot chest (iron, minerals). The Chthonians will actively chew wood in the area, including potentially player structures if they settle in the wrong spot.

---

## 17. Moral Ambiguity — The Mod's Tone

Hemomancy deliberately avoids hard moral binary between its factions. Neither side is good; neither side is evil.

- **The Harbingers** are not villains because they use blood magic. They are a found family built around something most people find uncomfortable. Their worst members have caused harm, as have the worst members of any group. Their best members are genuinely curious, community-minded people exploring something extraordinary.
- **The Unstained** are not heroes because they oppose blood magic. Their methods are genuinely brutal (embalming yourself in formaldehyde is not a kind process). Their Guardians will beat people to death with hammers to protect their sacred spaces. Their Lady, while not malicious, is herself not fully understood.
- **The Fungal Entity** is not evil. It is an alien organism fulfilling what appears to be its reproductive cycle. It is no more evil than a mushroom sporing onto bread.
- **The Pale Lady** is not unconditionally good. She is a force of nature. Forces of nature do not have morality.

This is a world of **gray**, of bodies and blood and fungus and old powers that predate human understanding. The player chooses their path, and neither path is wrong.

---

## 18. The Vagrant Mind

Past the central island, out where the void breaks into scattered plateaus, one of them is not stone. It is grown: a colossal hollow organ of black bulk tissue, blue banded strata, and a bone-white crust, its outer skin ridged into gyri like any brain's and divided by a dark sagittal sulcus. Nothing about it is architecture. Nobody built the Vagrant Mind. Something grew it, and something has been dead — or merely still — inside it for longer than any faction has kept records.

It is a dormant organ, not a deity. Treat it the same way the mod treats the Fungal Entity and the Pale Lady: enormous, unsettling, and utterly indifferent to being worshiped. There are two competing readings for what it is, and the game does not adjudicate between them.

One possibility is that it is nervous tissue the infection never reached — a fragment of cognition that grew up parallel to the Entity's spread rather than inside it, isolated by the void long enough to finish its own private course of thought and then simply stop. Its interior is strung with fiber the same way the Entity's fungal networks are strung with hyphae, but the resemblance is convergent, not inherited. It is what unclaimed nervous matter does when it is given world enough and time and nothing to interrupt it.

The other possibility is worse, in the way the Pale Lady's warning is worse: that it is not unclaimed at all, that it thought — actively, for a very long time — and what surrounds it now is not a fossil but a silence it arrived at on purpose. Something thought too long and stopped. Nobody who has stood in the cavity and watched the nerve-lightning still jumping between its synaptic nodes, decades after whatever used to occupy it went quiet, finds that reading comforting.

The Hematic Order reads it as an archive. To a Hematic Order surveyor, dormant nervous tissue this large and this intact is preserved memory waiting to be tapped — not a mind to bargain with, but a structure to study the way the Order studies every other unclaimed fragment of blood cognition, filed alongside the Sanguine Brotherhood's older salvage work and the Crimson Lodge's habit of cataloguing anything that still conducts. They do not claim to know whose memory it was. They are patient enough to find out.

Unstained pilgrims who make the crossing read it the other way, in Lethean still-water terms: not a resource but a lesson in the shape of a corpse. If even something built entirely of thought can choose stillness over continuing, it argues for the Lady's case better than any sermon. They do not disturb the fissure. They stand at its mouth, listen to the nerve arcs firing over dead tissue, and leave with their conviction reinforced rather than their curiosity satisfied.

Neither reading requires the other to be wrong, and neither is endorsed as canon fact within the story. It is a body. What it was thinking, and why it stopped, stays unresolved — the same kind of deliberate unresolved question the mod already keeps open about the Fungal Entity's origin and the First Incursion's builders. See [World Content](../wiki/World-Content.md) and [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §28.5 for the biome's mechanics.

---

*For mechanics, systems, blocks, items, mobs, and code details, see [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md).*

## The Vein-Mason's division of work

The Alchemist treats injury and prepares medicine. The Artificer reinforces armor, weapons, and the body's external frame. The Cicatrix Anchorite conditions scars and vasculature so the internal body can bear repeated instruction. The Mnemonist interprets what a memory means; the Anchorite determines how its tendency is routed through living tissue. Their cooperation is correspondence rather than companionship: the Anchorite remains cloistered at Mason's Respite, refuses purification, and cannot be recruited.

The first scar lesson requires the practitioner's own Effigy preparation followed by a committed cerebral loadout. A traded pattern can arrange already known routes, but cannot stand in for that practice. The Brazier lesson follows the shared offering rite: place the item, kindle it with Blood Projection, then draw the instruction with held Blood Absorption. Sneaking retrieves the offering rather than internalizing it. This changes the teaching controls, not the separation between cerebral memory, fungal implantation, and external equipment.

## Manifested successors

Form comes from the Vacant Effigy, vocation from consenting donor blood, affiliation from the officiant, life from Quintessence, and continuity from Ambergris. The resulting person carries a profession without becoming the original teacher. A Bound Mnemonic Remnant preserves that person through bodily death. Counterfeit identities cause mnemonic rejection and a Misbegotten vessel; the legitimate dead identity remains recoverable. See [Hematic Succession](HEMATIC_SUCCESSION_DESIGN_SPEC.md).


## The Antecedent: the First Incursion

Sculk is evidence of an older strategy by the intelligence behind the modern incursion: the hive's first attempt to infect the world. It failed and stagnated. The Antecedent Inquiry tells that history, and it has no bearing on the present state of the world or on Degree 7 and 8 progression; neither the Fungal Gardens nor the Archon's choice touches sculk. Its surviving ecology is acoustic, territorial, contiguous, and fed by local death. Modern Erythromycelium keeps intelligent hosts mobile, then has them cultivate and spread it. The connection is strategic, not anatomical. Sculk is not primitive Apotheos, crimson sculk, or a ninth tendency. The city's last listeners kept a damaged specimen so they could watch for coordinated response beyond the cut. The archive vessel answers an unresolved trace that the Clairaudiograph can separate from sound.

The Alchemist runs the experiment. The Vicar interprets what it may mean. Their dialogue stays qualified until the evidence supports recognition. The optional Archon epilogue acknowledges a changed approach without explaining the Entity's origin or exactly what the cut was. See [Antecedent Inquiry](../wiki/Antecedent-Inquiry.md).

### What the investigation establishes

The First Incursion left an ecology that depends on contiguous growth, local death, and acoustic disturbance. Its remnants can feed and react after the connection to something beyond them has been cut. Continued biological activity does not prove that coordinated direction has returned. That distinction is why the Last Listeners preserved the lower specimen and recorded its failures instead of finishing its destruction.

The modern incursion uses a different arrangement: intelligent, mobile hosts learn to cultivate and carry Erythromycelium. The Vicar recognizes a change in method by the same intelligence. This does not establish a common blood chemistry or make every sculk response a conscious act. Keep the Entity's origin, the exact nature of the cut that silenced the First Incursion, and the full history of the builders unresolved. That ancient cut is not **Severance**, which names only an Archon cutting their own Qliphoth tree (§12); write it as "the cut" or "the silencing" so the two never blur. The Warden is not an early Apotheos, and Incertae is not a ninth tendency.

### The evidence the player handles

The dark sample is Ahaematic Colloid, a separate nonblood specimen obtained from a Warden or Sculk Catalyst. Microscopy finds no hematic content, tendency, or enzyme response. Its classification, Incertae, records the limit of the current system. The shared vial shape is a container convention, not evidence that the fluid is blood. It cannot grant an injection power or a new enzyme.

The sample first responds to vibration. During the Severed Record it behaves in a coordinated way while the recorded audio falls silent. The Clairaudiograph's measured spectrograph remains empty in that interval; the unresolved trace is shown separately. The specimen's physical tap and the final click behind the listener occur in the world, not in the recorded waveform. Preserve that separation when rewriting captions, instrument explanations, and dialogue: ordinary acoustic analysis is the control that makes the other response meaningful.

The archive's ancient sample vessel is the surviving apparatus for this second observation. Replaying the Severed Record nearby makes its contents rise into a geometric response. It answers the specific unresolved interval, not every noise or inserted blood sample. Its narrative purpose is to let the player repeat the experiment at the original site and connect the portable sample with the preserved chamber.

### Surviving records

| Record | Narrative role |
| --- | --- |
| Lower District Survey | A surviving report of the lower district and its acoustic conditions before the decisive recorded incident. |
| The Quieting | Evidence of the imposed quiet and the practices used to live around listening growth. |
| Severed Record | The central acoustic event: distress and interruption, followed by recorded silence while an unresolved response persists. |
| Tablet of Quiet Earth | Practical cautions about territorial growth, noise, and the dead feeding the floor. |
| Lower Passage | Instructions for the listening road and the deliberate end of wool damping. |
| Lower Vigil: Watch Order | Repeated tests at upper and lower marks, feeding receded growth, and recording failures as useful results. |
| Lower Vigil: Notice | Orders to preserve the specimen and leave the weakened listeners unrepaired. |
| Lower Vigil: Record 7 | The growth still feeds and reacts, but no answer returns from beyond the cut; renewed coordination would mean the cut failed. |
| The Failed Burning | Failed surface destruction and the disturbance caused by breaking growth. |
| The Silence Order | Restrictions on bells, wheels, and travel, with escalating instructions for retreat. |

Five documents are on lecterns and two are chest books; the three recordings are separate cylinders. The texts describe the last listeners' observations and orders, not an omniscient history. Keep their practical concern with work, danger, and maintenance distinct from the modern Vicar's interpretation.

### The Vigil and its surviving ecology

The Vigil Beneath is a self-contained observation site adjoining the Ancient City's roads wherever the completed city leaves room. Its entrance is not tied to the central frame. A wool-lined approach gives way to an intentionally undamped passage, a gallery, and an archive. The intact boundary matters: the player crosses from avoiding sound into a place where sound is used as a test. Delayed clicks and reduced ambience establish uncertainty before the player sees the specimen bed.

The three original Sensors answer slowly and inconsistently. The Shrieker cannot complete its response. The Catalyst still feeds from death but produces limited local growth. These are preserved failures in one maintained specimen, not a new rule for all sculk. Replacing a fixture restores ordinary block behavior. Deepslate brick walls, a stepped roof, and retained surrounding rock place this chamber within the city and terrain. Enclosed entrance, listening passage, gallery, and archive modules join in several arrangements while keeping the sequence of approach, listening passage, observation balcony, descent, and archive. The corrected generation avoids an artificial rectangular excavation; a giant exposed lava moat is not part of the chamber's intended lore.

### Teachers, recognition, and the Listening Scar

At Degree 3 the Alchemist and Vicar can discuss the examined sample. The Alchemist requests a controlled replay after the player hears the Severed Record, and the experiment requires the player's own continuous observation. At Degree 4 the Vicar interprets the controlled result and gives the clue about passing where the wool ends. Degree 5 permits comparison of the two incursions. The optional Degree-7 Entity encounter follows completion; it acknowledges adaptation without supplying a complete cosmology.

Personal evidence is deliberate. Possessing an identified vial or another person's recording does not establish that the player witnessed the response. Observations can arrive out of order, but the implemented completion condition combines Vicar recognition and the archive response. The expansion is optional and does not add a rank gate to the main Harbinger path.

The Listening Scar is a small continuation of the experiment. The Vicar supplies a replaceable housing, the player fills it with analyzed Colloid, and the Severed Record awakens it. Worn within the Charm of Vascularium, it reports nearby actual Sensor activations as a directional pulse. It does not tame the Warden, grant immunity, silence the bearer, or reveal the location of every threat. Its use remains observation.

### Relationship to the other new systems

Clinical study belongs to the Alchemist until the question moves beyond physiology. The Artificer handles durable field apparatus, and the Mnemonist teaches retained expression through cylinders and the Clairaudiograph. Shared instruments connect these fields without transferring the ownership of every lesson to the person who built the instrument. Entity blood profiles encode blood composition and collection requirements; they do not make the Colloid hematic.

Hematic Succession is a separate Degree-5 practice. Consenting professional blood contributes vocation, the officiant contributes affiliation, the Effigy supplies form, Quintessence supplies life, and Ambergris supplies continuity. A successor becomes a distinct resident, not a copied teacher or a shortcut through another player's learning. Bound Mnemonic Remnants preserve that resident's identity across restoration; counterfeit identity produces rejection and a Misbegotten rather than an authentic replacement. Service and rite assistance depend on a maintained workplace and bloodline affiliation. See the [succession guide](../wiki/Hematic-Succession.md) for the implemented costs and recovery rules.

The Bombardier's nearby-growth habitat and revised posture/flame presentation belong to Phlegethontic ecology. Portrait, item-art, and laboratory-layout revisions likewise add no new First Incursion claims on their own. The [working-tree coverage index](WORKTREE_REFERENCE_AUDIT_2026-09-19.md) separates these changes from the expansion.

### Runtime text and editorial drafts (2026-09-19)

The current localized dialogue, captions, journal JSON, generated NBT books, and OGG recordings are the installed telling. The original [First Incursion brief](HEMOMANCY_FIRST_INCURSION_DEEP_DARK_BRIEF.md) supplies design background. The [fresh prose draft](antecedent-fresh-draft/README.md) contains seventeen Markdown files for rewriting, but its wording has not been imported into the game or synthesized into new audio. Do not quote those drafts as already spoken or displayed in the current build.

Book changes must reach `tools/oneoff/antecedent/build_vigil.py` and the regenerated structure. Recording changes must reach the script, captions, runtime cue timeline, and audio together; the silent and observation intervals are gameplay constraints. Existing generated books and terrain are not rewritten by changing these source files. The [developer reference](HEMOMANCY_REFERENCE.md#antecedent-inquiry-and-audio-analysis) records the concrete behavior, sources, and validation boundaries.

## 19. The Myelin Borers

Something still tends the Vagrant Mind's cabling. The Borers are blind segmented grubs that never touch
the islands if they can help it; they live on the fibers, gripping the bundles and working their way
along them, and where a span has been cut they patiently spin it closed again. They are not vermin and
they are not servants. They are maintenance tissue — grown by the organ, or grown on it, and still
carrying out their one function long after whatever directed it went quiet.

Cut the cables and they change. Within moments of a severed fiber the nearest Borers stop mending and
start eating, unpicking the bridges faster than they ever repaired them, and they will bite whoever did
it. Left alone for a while they settle and go back to the work, and the network heals. They will not
touch the synaptic knots, ever; whatever they are for, the junctions are not theirs to eat.

The Hematic Order reads them as proof the organ still has reflexes — that something so plainly finished
still defends its own circulation, and that the Mind is therefore not a corpse but a patient. The
Crimson Lodge is more interested in what comes out of one when you kill it. Lethean readings are colder:
the network was quiet, the quiet was the point, and a person who takes a blade to it is not discovering
anything. They are only waking a thing that had managed to stop.

## 20. Naeglerophaeon

The Borers mend the Vagrant Mind's fibers. Naeglerophaeon sleeps within a dormant ganglion, a maroon knot held in an ivory sheath among the inner nerves. Passing through the organ does not wake it. Breaking that sheath releases it into the web. When the organ empties of intruders, the predator eventually withdraws and the knot closes again. Once roused, Naeglerophaeon takes what enters the fibers. A maroon core
holds a cluster of burning ganglia, surrounded by ivory tendrils and translucent crimson webs.
Signals travel along the shorter arms, each ending in a small electrical discharge. One immense
red axon trails behind, its lights steady. When the creature feeds, that axon swings forward to
bind its prey and draw it toward the brightening core. No one agrees whether it is a parasite that
found the Mind or an old reflex of the Mind itself. It does not need open water to cross the organ: it folds into a synapse and travels the nerves as a signal, surfacing somewhere else. That is the same passage its ganglion later teaches. A severed fiber is not a distraction to it but an injury, and it answers the hand that made the cut. Near death it will not leave the web to anyone. It hangs inverted at the centre and burns out the Mind's synapses one by one, flinging sparks from every knot it kills. When the last one goes dark, it dims to grey and drifts between the dead junctions, twitching, until someone ends it. Its ganglion still conducts after death. In the Somatic Loom, that organ can disclose Axonal Transduction: a memory of dissolving the body into a living impulse and entering the nerves themselves. A practitioner who carries it can grow a route of fibers and synaptic knots, then pass through that route as a signal. The world outside remains a dark red impression; the nerves keep their pale course and the knots burn yellow.

### Shared restraint and marine feeding materials

Enthralling Filament is the ordinary restraint fibre formerly named Puppeteering Thread. A Peacock Spider spins it; a surface Hemojelly gathers prey with living tendrils; a Blood Drunk Puppeteer supplies it through a different expression of restraint. Its broader origin does not grant knowledge or control of a Crossbar. Hemojellies float like blood-coloured man-of-war colonies in warm and temperate seas, their feeding reach hanging below the surface. Deeper Siphonophores express Ductilis through brief tentacle discharges and ordinary neural ganglions. Those clusters are not the singular Naeglerophaeon organ. Chromatophores describe shared color/light anatomy rather than a cuttle-exclusive substance.


### Phlegethosoma, the Arterial Fluke

Phlegethosoma is a native ichor-feeding animal of the Phlegethontic Basin. Its long, serpentine blood-fluke body, adhesive oral sucker, soft frilled edges and distending crop support rapid, vicious swimming, muscular suction, clotting, feeding and rupture. It travels through ichor by a wave of lateral contractions, with a narrow body tapering into a ribbon tail; the former broad, plated limpet silhouette is superseded. It is neither a Horseman variant nor a summoned guardian, demon or Harbinger creation. Its pressure spray is expelled ichor rather than flame magic. The encounter is accessible to both paths and does not add a promotion gate.

The Arterial Fluke can disappear below deep ichor, drive its body out of the pool toward prey, then fold back beneath the surface. The crimson disturbance above its submerged body precedes the breach; it remains an animal propelled by muscular motion.
