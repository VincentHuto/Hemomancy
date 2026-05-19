# Hemomancy — Lore Reference

> This document covers **lore only**: world history, cosmic origins, factions, characters, beliefs, mythology, and narrative themes. For mechanics, systems, and code details see [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md).
>
> **Last Updated:** 2026-05-19 Sporitic Thurible alignment note. Implementation-status notes use the same implemented / partial / dormant / planned vocabulary as `HEMOMANCY_REFERENCE.md`; lore intent remains canonical here, while mechanics details remain code/reference-doc sourced.

> **Current Lore-State Snapshot (2026-05-06 audit):**
> - Core narrative pillars remain directly represented in gameplay: Harbinger initiation/degrees (now including the explicit Apotheos gate), Unstained purification path, faction NPC dialogue trees, blood-memory framing, and fungal-whisper escalation.
> - Qliphoth Communion now has a clearer in-game ritual rhythm: the Sanguine Monolith breaks open with a black void-bloom, the Qliphoth Tree drops nine named husks with personal whispers, and only Cult Pruning can remove the bloom by normal progression.
> - Blood Moons now visibly match their lore state in the sky: the red lunar face and fungal-vein overlay appear while the Pale Lady's costly immune response is active.
> - **Annetta Knowles encounter is partially complete:** the two-route encounter is wired and playable, dedicated Java models/textures are present, and final animation polish, fuller Phase 1 biological combat identity, and Sanguis Lancea rendering remain WIP.
> - Several high-impact lore arcs are intentionally established as foreshadowed endgame content and remain partial in gameplay implementation: broader Fungal Dimension progression, non-Hemorath Saint chambers/world placement, and fuller boss artwork.
> - Covenant social structure gained stricter in-game expression: bloodline leadership can now ritually sever members, reinforcing the Order's "chosen-family covenant" framing over biological lineage.
> - This document remains canonical for worldbuilding intent; for implementation status and mechanics-level detail, treat [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) as the source of truth.

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

### 2.2 How It Infects a World

The Entity extends **hyphae tendrils** across the surface of whichever world it has "landed" on. These tendrils reach upward; the bulb-shaped nodes at their tips eventually break free of gravity and drift off as **spores**, spreading pockets of Erythrocytic Mycelium across the land. The infection zones do not multiply exponentially. They are slow, deliberate, geologically patient.

The Entity's true purpose appears to be **reproduction**: the Hematic Order it catalyzed is not a human institution. Each degree of initiation is a stage of sporulation. Each rite triggers the next bloom. The Harbingers, in ascending their ranks, are not discovering blood magic — they are becoming fruiting bodies for a vast mycelial network. This is the truth delivered by the Fungal Whispers at the highest degrees, and it is the truth the Hematic Order does not know.

> *"The Hematic Order was never a human creation. It is a reproductive strategy."*

### 2.3 The Entity and Mod Compatibility

The Entity's existence is not entirely self-contained. If a player aligned with the Harbingers also practices other forms of magic (e.g., Mana and Artifice), and those spells happen to align with their blood tendency, there are **compounding resonances** — arcane synergy between blood and mana. This is not coincidental. The Entity's influence bleeds into adjacent magic systems.

---

## 3. The Fungal Dimension (The Flesh Beyond)

At the peak of the Harbinger path — after reaching Archon (Degree 7), completing Qliphoth Communion by eating all nine pomes from one bloom, and completing the Rite of Apotheos — the player receives a **Fungal Spine** that tears free from their back. Using it transports them to what appears to be a separate dimension.

### 3.1 What It Is

The player is **not physically transported**. Their body remains in the world. What happens is closer to an **astral projection** or a forced expansion of consciousness — the fungal infection within them is now advanced enough to let them perceive the Entity's local "surface." They have no body in this place. If they look down, there is nothing there.

What they encounter:
- A vast **spherical mass of flesh, meat, and pulsing biology**
- Enormous **hyphae tendrils** arcing upward into a sky where the world and moon are visible, as though this space exists just above the Earth
- Bulb-nodes on the tendrils that eventually break off like seeds, falling back toward the world as spores
- Alien, bizarre creatures that exist nowhere else — beings that are not infected Earth-life but genuinely alien life forms native to the Entity's substrate
- **Constant Fungal Whispers**, nearly harassing in frequency, saying things like *"This is what you wanted. This is what you were working toward. Is it not?"*

Everything here is hostile, even to an Archon. The player is a babe in the woods.

### 3.2 Return and Choice

The player keeps their Fungal Spine and can use it to return. There may also be natural exits or anchored places of transit, but the spine is the personal return-thread. If the player digs to the very bottom of the space and "punctures" through the core, it severs their connection temporarily (as though they harmed the Entity's surface), ejecting them.

Upon returning, the player faces a choice: **stay silent and remain an Archon**, or **continue deeper into the eldritch truth** and pursue the true 8th degree. This choice is the mod's deepest opt-in.

### 3.3 Transcendence (Late/DLC Content)

The rarest and most extreme endpoint — performing small fungal rituals that strip away what remains of the player's physical form and merge them with the hive mind. This is envisioned as expansion/DLC content, not expected to be reached by most players.

---

## 4. Blood Magic — What It Actually Is

Blood manipulations — the spells and powers of a Hemomancer — are not truly learned. They are **remembered**.

Everyone carries dormant **blood memories** deep in their biology, inherited from the infection's long history in the world. These memories are potential, not active — they cannot be accessed without the infection fully taking hold. Once awakened (via the Mortal Display and Sanguine Initiation), these memories can be drawn out, shaped, and activated through a process called **Memory Weaving** — feeding enzymes and reagents into a Somatic Loom to crystallize a specific dormant memory into a usable Hematic Memory item.

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
- The Hermit's final words and a **Rite Blueprint** for the Sanguine Initiation
- The activation of their own latent blood magic

The Hermit then dies — the heart, now given, can no longer sustain them. His last recorded words reflect the tradition:

> *"Heart was never mine to keep, only to give... Walk the crimson path, young Harbinger. I will go now where blood returns to silence."*

This is a deeply personal, singular act. Each Blood Temple in the world belongs to one specific Hermit. The Hermit is **invulnerable** until the player chooses to take the heart (the "Farewell" dialogue option).

---

## 6. The Harbingers (Hematic Order)

### 6.1 What They Believe

The Hematic Order does not view blood magic as dark, evil, or taboo. To them, it is a **sacred essence that is a part of life itself** — a gift their blood carries, passed down and refined through the centuries. They believe it would be sacrilegious and wasteful not to use and share it.

They do not worship a blood god. There is no deity at the center of the Order's faith. Blood is simply seen as the most profound material in existence — the carrier of life, of memory, of identity. To master it is to master the most fundamental truth of being alive.

**They are not evil.** They are shunned, taboo, misunderstood — the way anything unfamiliar and vaguely body-horror-adjacent tends to be. There have been incidents: a Harbinger losing control in a fight, blood magic being used in anger or defense, accidents from latent powers awakening under stress. These incidents create fear. The Order lives with that reputation.

### 6.2 Culture and Appearance

Harbingers favor:
- **Lighter armor** — cloth, natural materials, beetle-shell armor (Hematic Iron chitin)
- **Natural materials** — things the body produces or modifies (iron fused with blood, chitin, bone)
- **Blood manipulations as their primary defensive and offensive tool** — hardening blood into barriers, using iron-tendency powers, the living weapons they carry

They do not view themselves as recluses by choice. The Hematic Order is, at its best, a community — a found family. Joining the Order typically means leaving behind one's biological family, not because it's required, but because the lifestyle of a Harbinger makes it impractical to maintain typical family relationships. The Order provides a new family.

### 6.3 The Harbinger Outpost and Its NPCs

Once players advance past the Blood Temple, they are directed to **Harbinger Outposts** — multi-story structures that serve as the Order's visible presence in the world.

Each outpost contains:
- A **Harbinger Vicar** — the outpost's lore-keeper and rank-progression guide. The Vicar guides players from Degree 1 through Degree 5, then steps back as an equal once the player reaches the Crimson Lodge. He delivers faction history, doctrinal lore, and the secret of the 8th Degree at Archon.
- A **Harbinger Alchemist** — found at the crafting stations, teaches how to use the mod's machines (Alembic, Centrifuge, Somatic Loom, etc.).
- Generic **chests and loot** from the Order's stores.

> *Vicar's Secret (Archon only):* "The Hematic Order never had seven degrees. There have always been eight. The eighth degree is silence."

### 6.4 Degrees of Initiation

Most Harbingers — most outposts, most individual groups — only ever reach **Degree 5 (Illuminatus)**. To the average practitioner, this is the summit of what blood magic can offer. The existence of Degrees 6 and 7 is known only to those who reach them; the existence of the 8th is known only to Archons who are told, and most keep the secret.

| Degree | Title |
|--------|-------|
| 0 | Uninitiated |
| 1 | Neophyte of the Crimson Veil |
| 2 | Votary of the Hematic Covenant |
| 3 | Initiate of the Scarlet Sanctum |
| 4 | Adept of the Sanguine Brotherhood |
| 5 | Illuminatus of the Crimson Lodge |
| 6 | Sanctified of the Bloodline Covenant |
| 7 | Archon of the Hematic Order |
| — | (The Silence — no formal title) |

### 6.4a Degree Themes

Each degree carries a **lore theme** — the historical/metaphysical institution the player is entering — and a **gameplay theme** — the primary system or tool that opens at that rank. The escalation traces a straight line: from "blood as a personal resource" at Neophyte to "blood as a cosmic reproductive strategy" at Apotheos.

#### NEOPHYTE 1 — *of the Crimson Veil*
**Lore:** The Crimson Veil is the membrane between ordinary life and the world the Order inhabits. The player has just torn through it. The Hermit greets them as a first tentative step. The Order is not a secret society to them yet — it is a rumour. The Rite of Sanguine Initiation *is* the degree-granting event: performing the ritual is the first step.

**Gameplay:** Blood as a living resource. The **Ghastly Alembic** is introduced — entry-level machine for blood-product creation (Befouling Ash, Sanguine Formations, early ingredient items). First Humilis-rank blood manipulations become unlockable. The Liber Sanguinum is obtained. Core loop: fill blood, spend blood, cast manipulations.

#### VOTARY 2 — *of the Hematic Covenant*
**Lore:** A Votary has taken a vow — not to the Order as an institution but to the Covenant as a compact between practitioners. The difference between someone who was bitten and someone who *chose to stay bitten*. The Vicar introduces the seven blood tendencies. The Fungal Podium becomes accessible (gated at Votary+), framed as a meditation or resonance tool — the player does not yet know what it connects to.

**Gameplay:** The **Vial Centrifuge** and the tendency system. Players separate tendency-aligned blood from raw samples via the Living Syringe + Vial Rack workflow. Tendency alignment scores begin shifting toward chosen affinities. The Blood Tendency UI is fully revealed. Humilis and Mediocritas manipulations from the chosen tendency line become craftable. The Alchemist also introduces **Blood Structure crafting** at this degree — Basic-tier multiblock patterns are now available (simple flat layouts using a Blood Key and raw blood cost). Advanced patterns unlock at Degree 4 (Adept); Grand-tier patterns requiring sustained Blood Conduit flow unlock at Degree 5 (Illuminatus).

#### INITIATE 3 — *of the Scarlet Sanctum*
**Lore:** The Scarlet Sanctum was founded by Archon Erythravane in the Second Age — the first organized gathering of hemomancers, predating the current Order's structure. Becoming an Initiate means joining a lineage centuries old. The Vicar reveals the Sanctum's founding and the person of Erythravane. This is when the player first understands they are not discovering a novelty — they are inheriting a tradition. The Vicar also directs them toward the ancient Saints.

**Gameplay:** The **Somatic Loom** (memory weaving) becomes fully active. The Saints system opens: players seek entombed Saints in their Trial Chambers, extract Hallowed Residuum via Consecrated Syringe + Centrifuge, then use it in the Loom alongside tendency-aligned enzyme configurations to unlock **Canon Memories** — SUMMA-rank manipulations unique to each Saint's blood legacy.

#### ADEPT 4 — *of the Sanguine Brotherhood*
**Lore:** The Sanguine Brotherhood arose during a time of war, when Harbingers began sharing blood pools to sustain each other in battle — the earliest form of the Bloodline system. An Adept has moved beyond solo practice into understanding blood as communal inheritance. Simultaneously, the first **Fungal Whispers** begin — subliminal and barely perceptible. Just seeds of doubt.

**Gameplay:** **Scars** and the **Cerebral Scarring Station**. Scar crafting requires minimum Degree 4 (wired in code). Players gain access to the Cerebral Scarring Station (surgical encoding of venous/neural pathways) and the Chisel Station (rune encoding). Scars equip in the Scar Binder and grant passive bonuses, skill amplification, or manipulation modifiers. Visceral Organ extraction via the Visceral Mirror ritual also becomes Adept-tier content. The Sporitic Thurible now sits in this same Adept band: a held thurible that burns aligned spores with blood upkeep, externalizing the Brotherhood's battlefield covenant as infectious support incense rather than simple weaponry.

#### ILLUMINATUS 5 — *of the Crimson Lodge*
**Lore:** The Crimson Lodge documented the link between hemomancy and the mycelial network — and kept that knowledge secret, insisting it must be "arrived at independently." Becoming Illuminatus means the Lodge judges the player ready to receive what they recorded. The Vicar reveals it: *"The blood you command… it was not always blood."* Most Harbingers never reach this degree. Fungal Whispers become clearer intrusions.

**Gameplay:** Three major unlocks. (1) **Morphling Incubator** — biological/fungal crafting for organism-derived items. (2) **Blood Structure Grand tier** — the Blood Conduit pipeline network becomes a mandatory crafting input for the most complex structural patterns; sustained flow within range is required, not just a single key strike. (3) **Founding Sanctum** — a Sanguine Quintessence is granted by the Illuminatus rite, required for the founding ritual that consecrates a 5×5 chunk area as a Harbinger Sanctum (Damage Boost, Regeneration, Resistance buffs for all Harbingers present). The Sanguine Monolith becomes accessible (Degree 5+ gated).

#### SANCTIFIED 6 — *of the Bloodline Covenant*
**Lore:** The Sanctified do not belong to the Order — they *are* the Order. The Vicar delivers the degree's doctrine: *"The blood becomes indistinguishable from the blood of the world."* The Bloodline Covenant is revealed — a ritual compact between Harbingers that merges blood pools and capabilities. Fungal Whispers grow direct and revelatory: *"The first Archons did not discover hemomancy. They were infected by it."* Erythromycelium is named as the original organism.

**Gameplay:** **Bloodline Covenant system** fully unlocked — players ritually bind capabilities into a shared Covenant: linked blood reserves, tendency bonuses that amplify between members, blood-link combat synergies. The Bloodline leader can ritually sever members (reinforcing the covenant-as-chosen-family framing). The Alchemist describes this degree as "final synthesis" — all machines working as one unified process.

#### ARCHON 7 — *of the Hematic Order*
**Lore:** The Archon is told what no one below this degree ever hears: *"The Hematic Order never had seven degrees. There have always been eight. The eighth degree is silence."* The Fungal Whispers deliver the complete revelation: the Order was never a human institution. Every degree was a stage of sporulation. The Vicar kneels. And then, quietly: the Sanguine Monolith begins to feel wrong. Hollow. Like something has been waiting inside it.

**Gameplay:** **Qliphoth Communion** — the 5-stage endgame sequence. Shatter the Sanguine Monolith; perform the Bloom of the Qliphoth rite; collect and eat all nine Qliphoth Pomes; prune the bloom via the Cult Pruning rite. The most elaborate multi-step sequence in the mod. Completing Communion unlocks the Apotheos rite choice. **Fungal Scars** are implemented as a fourth scar family through Mycelial Crucible cultivation rather than the older Fungal Gardens harvesting plan; they alter the player's relationship to the mycelial network rather than deepening tendency alignment. Deeper Apotheos-tier fungal scar concepts remain planned design space.

#### APOTHEOS 8 — *of the Hematic Order*
**Lore:** Not an ascension to godhood — a *completion of the sporulation cycle*. The Fungal Spine tears free from the player's back. Their consciousness is cast into the Fungal Dimension — not physically, but as astral projection into the Entity's local surface. There is no body here. Everything here is hostile, even to an Apotheos. On returning, the player faces the choice: carry the truth in silence, or continue deeper into hive-mind dissolution (expansion content).

**Gameplay:** **Fungal Dimension** access via the Fungal Spine item. On first exit attempt from the dimension, the Fungal Podium fires the core witness dialogue and the two-option choice fork (stamped as `archon_choice_made`). Post-return, the Apotheos retains full Qliphoth Pome empowerment (reduced manipulation costs), maximum blood capacity, and unique cosmetic effects reflecting the fungal transformation.

### 6.5 The Founding Sanctum

At Degree 5, a Harbinger can perform a founding rite that consecrates an area around their chosen base. A **5×5 chunk area** becomes a Harbinger Sanctum — a zone where all Harbingers present receive enhanced powers: stronger regeneration, lower cooldowns, more potent attacks. This is meant to encourage collective settlement and base-building with others. A special crafting material called a **Quintessence** is granted by the Illuminatus rite and is required to perform the founding ritual.

### 6.5a The Sanguine Monolith (The Crimson Lodestone)

The Sanguine Monolith — known within the Lodge as *The Crimson Lodestone* — is a 1×2 stone construct accessible to Harbingers of Degree 5 and above. It provides degree-gated guidance up to Degree 7, at which point an Archon may shatter it to recover the Qliphoth Seed inside.

**What it actually is:** The Crimson Lodge built the Monolith as an incubation vessel: a sealed stone construct housing a dormant fragment of mycelial consciousness, extracted from a deep Erythromycelium vein in the Second Age. The Lodge believed it could sense a Harbinger's degree because hemomancy leaves detectable signatures in the blood — and that explanation is almost right. The fragment inside is alive. It reads degree not as a record-keeping system but as a sporulation assessment. It is watching practitioners ripen.

At low degrees the fragment is barely aware; its surface is cold. By Degree 5–6 it begins to stir — the stone grows faintly warm. At Degree 7, the Archon's blood is mature enough that the fragment fully awakens and the vessel cracks. The seed inside is not merely a byproduct: it *is* the fragment, or the most coherent part of it, made physical.

The Monolith uses degree-gated dialogue that gradually discloses its nature. Players who ask *"What are you?"* receive answers that escalate from official Lodge description (Degree 4) to admitted self-awareness (Degree 5) to disclosure of the contained thing (Degree 6) to direct recognition of kinship (Degree 7). Archon dialogue is intimate in a way that should feel wrong before it feels significant.

### 6.6 The Hematic Order's Historical Record

From the Harbinger Vicar's lore branches:

- The **Scarlet Sanctum** was founded by **Archon Erythravane** in the Second Age — the first organized gathering of Hemomancers.
- The **Sanguine Brotherhood** arose during a time of war, when Harbingers began sharing blood pools to sustain each other in battle — the origin of the Bloodline system.
- The **Crimson Lodge** documented the link between hemomancy and the mycelial network. This knowledge was kept secret — considered something that must be "arrived at independently" to be understood correctly.
- The **Hematic Order** itself is described by the Vicar not as a rank, but as a state of being, where "the blood becomes indistinguishable from the blood of the world."

---

## 7. The Unstained

### 7.1 Origins

The Unstained are not a wholly separate tradition. Their **original members were Harbingers who left** — practitioners who had experienced blood magic firsthand and then chose to reject it. This is why they are so effective at combating it: the founders knew it from the inside. Their methods of purification, their understanding of what hemolytic solution does to blood-infused veins — this was all discovered and refined by people who had once been what they now oppose.

This also explains why the Unstained will **warmly welcome back** a Harbinger who wants to leave, even at high degrees. They have no investment in keeping people out. A former high-ranking Harbinger who wants purification is an *asset* — someone who understands the enemy as well as anyone.

> *Exception:* A full Archon who has begun to accept the Fungal Whispers and their truth is a different matter. The Unstained will still permit them to seek purity, but are far more cautious.

### 7.2 What They Believe

The Unstained do not necessarily believe blood magic is evil in an absolute moral sense. They believe it is:
- **An infection, not a gift** — something that has colonized the bearer, not something that was ever theirs
- **A threat to the self** — as purification progresses, practitioners discover that what felt like personal power was always the fungus guiding them
- **Something that can be shed** — through painful, deliberate effort

They are devout, but not fanatical in the way of those who seek to convert or conquer. They welcome those who come to them and leave alone those who do not. A Harbinger wandering into an Unstained Church will receive a measured response:
- Degree 0: concerned sadness; the Church sees a newly blood-touched person as infected and frightened rather than culpable
- Degree 1-2: careful welcome; help is offered plainly, though the Unstained warn that every rite makes the blood harder to shed
- Degree 3-4: wary help; Zealots and Acolytes still offer cure, but ask whether the player seeks healing or merely another power
- Degree 5: hesitant disdain; aid is still possible, but the Church assumes the player has loved the crimson road long enough to make repentance suspect
- Degree 6+: ordinary Church members refuse the normal cure path, treating the infection as too deeply rooted for safe hemolytic rites
- Guardians use blunt force to escort hostile or deceptive visitors out when necessary; they do not spill blood near the church

### 7.3 Appearance and Equipment

The Unstained aesthetic is **white, silver, and oxidized copper**. Their Guardians wear:
- White plate mail with flowing white robes beneath
- Oxidized (fully patinated) copper accents and components
- Glass helmets — intentionally suggesting they are not breathing the same air as others, either literally or metaphorically

Their weapons are exclusively **blunt instruments**: maces, war hammers, staves. They explicitly refuse to carry bladed weapons. The philosophy is simple: they do not want blood spilled anywhere near them.

Their field equipment is practical before it is mystical. A **Pale Silver Bell** makes blood-magic hesitate and gives frightened practitioners a moment to breathe. A **Lethean Chalice** carries still water for rinsing poison, fever, and old influence from the body. A **Verdigris Censer** burns oxidized copper salts into a diagnostic smoke that marks blood-active bodies without needing to open them.

### 7.4 The Significance of Copper and Silver

**Copper** is historically antiseptic — it does not harbor disease, and the Unstained have known this for generations (even if they did not understand the chemistry). Copper door handles, copper tools, copper armor — surrounding oneself with copper was believed to stave off illness.

**Oxidized (patinated) copper** holds an additional sacred status:
- Copper that has fully oxidized and stopped changing represents something that has "done its job" — absorbed all the blood and disease it could until it was saturated, and was then consecrated by that service.
- A person carrying fully oxidized copper must have survived long enough for it to reach that state, meaning they successfully kept themselves pure throughout. The copper earned its patina by protecting them.

**Virid Salis** is the Unstained's green salt-ash: a verdigris-colored ritual powder that borrows the holiness of patinated copper without pretending the copper is clean. It marks lines of warding and purification in the same practical spirit as ash trails, but its meaning is defensive and antiseptic rather than sanguine.

**Silver** in this world does not spawn naturally. It is **consecrated copper** — copper that has been processed through a ritual of purification until it achieves a purer, refined state. Pale Silver Ingots are the primary high-tier Unstained material.

### 7.5 What Happens to the Purified

Once a practitioner completes the full purification process (Phase 1: Purity 100%), they enter a brief coma and wake fundamentally changed. They are, biologically, no longer fully human. The Hemolytic Solution they drank was essentially **formaldehyde** — they have embalmed their own veins from the inside. Their blood no longer flows in a conventional sense. Their eyes drain of color, hair and nails may appear to continue growing as skin recedes, giving them a gaunt, glassy-eyed appearance.

Abilities that emerge at this stage:
- Improved night vision
- Ability to sense illness in those nearby
- Healing touch
- Passive plant growth in their vicinity
- Greatly enhanced resistance to blood corruption

And critically: **biological immortality**. A fully purified Unstained does not age, does not hunger in the normal sense, and cannot die from blood loss (they have none). They can only be killed through physical destruction — being blown apart, burned, destroyed by magic — or by fungal means. Even severing a limb does not kill them; they simply won't bleed.

The Unstained Church does not need many members. High-quality, immortal practitioners who arrive at purity and stay are sufficient. The church grows slowly, surely, and endures.

Once Clarity opens, the purified no longer force power into being the way Harbingers do through Hematic Memories. Instead, they learn **Still Arts**: quiet, defensive acts that settle into the silvery vital humor as gifts from Our Lady or as the fruit of non-breaking rites. These arts are progressive rather than manufactured. A newly clarified Unstained might learn Silver Rebuke, while later clarity allows Lethean Mute, Still Pulse, Pale Diagnosis, Memory Shear, Absolving Step, Quietus Bell, and finally Autoimmune Edge.

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
- Serves as a crafting ingredient for Unstained recipes

The **Tears of Silthmere** are Lethean Dew distilled to its purest form — a one-time blessing from the Lady herself, offering a significant burst of purification when offered at an Altar of Cleansing.

The **Pallid Icon** — a rare relic carved from pale silver found at the bottom of a forgotten river — is said to allow an Unstained practitioner to summon a brief apparition of Our Lady and ask her lore questions directly. She does not manifest physically in any other way.

### 8.6 Her Hidden Nature — A Warning

Toward the end of the Clarity path, Our Lady's whispers change subtly. She grows *quieter* and then, occasionally, she begins to push the player toward **actively eradicating sources of corruption** — something that is out of character for a purely defensive force. This is meant as a seed of doubt for the player and a hint at something not yet fully explored in the narrative. She is not perfect. She is still part of nature, and nature can become something other than what we expect.

---

## 9. Blood Moons — Cosmological Significance

Blood Moons in Hemomancy are not just a gameplay event. They are a **manifestation of the conflict** between the Pale Lady and the Fungal Entity.

When a Blood Moon rises, it is the Pale Lady expending a significant burst of her power to push back against the fungal infection for another cycle — a kind of cosmic immune response. She wins these small battles, which is why the world is not already a meatball of flesh. But each one costs her something. The night after a Blood Moon, the moon may appear new or very dim — she is recovering.

From the player's perspective:
- Blood Moons are uncommon natural events; current gameplay checks once per night and may start one for the rest of that night
- Harbingers / Hemomancers receive **enhanced strength and night vision**
- Non-blood-magic users receive **weakness**
- Thirsters and Fargones stir in the dark
- The moon itself renders red, with fungal-vein sky forms intruding around it

A ritual is still intended to trigger a Blood Moon manually in the future, but it should remain intentionally expensive — a single player triggering a worldwide event that affects everyone on the server is something that should be done sparingly.

---

## 10. The Saints

The Saints are ancient Hemomancers from the world's past whose power grew so extraordinary — or so uncontrolled — that they were entombed rather than buried normally. Their sarcophagi persist, and their bodies retain potent blood essence that can be harvested.

There are **four Saints** in the mod. Which one a player encounters first is somewhat randomized. Each has:
- A unique **Trial Chamber** structure — a dungeon-like puzzle that must be completed before reaching the sarcophagus
- A **tendency affinity** — if the player's blood tendency aligns with the saint's thematic affinity, the saint peacefully yields a blood sample; if not, they awaken and attack
- A unique **boss fight mechanic** if awakened
- A **special high-level blood memory** that can be crafted from their blood sample

Players are directed to seek the Saints around **Degree 3–4**, when they are strong enough to attempt the trials.

**Triggering a boss fight deliberately**: Smearing **Foul Paste** on a sarcophagus will anger the saint and force a fight even if the player had already extracted a sample peacefully.

**Implementation status:** Partial. The shared sarcophagus spine, peaceful aligned extraction, unaligned awakening, Foul Paste forced awakening, saint-specific boss dispatch, syringe tagging, and direct residuum rewards are implemented for all four Saints. Hemorath's trial flow is the first complete chamber. Seraphae, Putriciel, and Velorum have registered boss AI, but bespoke chambers, world placement, final balance, and dedicated art/animation remain WIP.

### 10.1 Hemorath — The First Saint

**Narrative:** One of the original great Archons of the Hematic Order. Hemorath represents the iron, unchanging, Ferric-tended aspect of blood magic — the belief that the blood can be made permanent, solid, inevitable.

**Trial:** The player enters a locked chamber. Their blood is slowly sapped throughout the trial. Four basins must be filled to precisely the correct level — not overflowing, not empty — while blood-construct monsters spawn throughout. Filling all four basins correctly opens the sealed gates to the inner sarcophagus chamber.

**Boss Mechanic:** Hemorath is described as tall, lanky, dry, and hollow-looking — a desiccated but still animate form. His current fight hybridizes blood debt and overload: blood magic cast near him deepens the player's owed blood, especially once he weakens, while also filling Hemorath with absorbed blood. Reckless casting can collapse the player under their own debt, but enough absorbed blood overloads Hemorath until he bursts and exsanguinates — killed by the very thing he sought to master. This is an intentional puzzle fight, not a straightforward damage race.

### 10.2 Seraphae, the Chain Saint — Bound Radiance

**Narrative:** Seraphae is the final identity of the figure once called only the Chain Saint: a nurse or healer who discovered she could heal people with a touch. As she healed, her veins began to glow. Plants grew rapidly around her. She could not stop — everything she touched bloomed and grew faster, then faster, until her vessels burned through and she became an uncontrolled force of raw life and creation. She had to be restrained with chains and containment anchors rather than killed, because she could not be killed in a conventional sense.

**Boss Mechanic:** The chamber floods with light she emits, which burns the player. The player must avoid her light, bind her fragments, activate containment anchors, and strike during condensing windows to restore containment integrity. The victory condition is to chain Seraphae's radiance back into restraint — returning the room to darkness — rather than killing her directly.

---

## 11. The Stained Priestess — Annetta Knowles

Annetta Knowles is a boss character and one of the more tragic figures in the mod's lore. She was one of the **highest-ranking members of the Unstained**, a cleric who had given her life to purification and the service of Our Lady.

Her downfall came from a single, impossible fact: a **Chthonian** (one of the iron-mandible termite creatures that the lore establishes as only seeking out infected individuals) found its way into the church and **bit her**. These creatures do not bite the uninfected. They have no interest in the clean.

This one small fact — an insect bite that should not have been possible — **shattered everything she had built herself on**. If she could be bitten, she was infected. If she was infected, had she ever truly been clean? Had any of her purification been real? Had she been walking the Lady's path, or had she been deluding herself while the infection marinated inside her all along?

Her sanity broke under the weight of this question. And in that fracture, the latent blood power she had been suppressing for her entire life — through years of purity work, through complete devotion — came flooding out all at once, released by the psychological collapse of her defenses.

**What she has become:** A being caught between both paths — still carrying the silver of the Unstained (her eyes are that teal patina-blue, the color of verdigris copper) but also bursting with awakened hemomantic power. She wields a spear that appears to be made of hyper-solidified blood — her blood, long-latent, finally crystallized. In her second phase, she extracts this from herself as her primary weapon.

**Her thematic domain:** Teeth, nails, hair — the biological materials that are *not* blood-connected (calcium, keratin) but are undeniably biological. These fall into neither the Harbinger (blood, iron, bone) nor the fully Unstained (copper, silver) domain. They are hers.

**Draught of Still Mercy** — a specialized Unstained-crafted tincture that can suppress the latent infection in a host who has not yet fully converted. If brought to Annetta by an Unstained practitioner with sufficient Clarity, she will drink it, allowing the infection to be externalized and fought separately rather than burning through her. This is not a cure in the traditional sense — it is a violent purging — but it leaves the host alive and, eventually, at peace.

> *She is not the Pale Lady.* Our Lady of Still Waters is a force of nature, non-physical and non-corporeal. Annetta is a person — a broken person with enormous and terrible power.

**Implementation status:** Partial. Annetta's encounter is wired and playable. She spawns in COWERING state inside a `BrokenChurchStructure` with contextual scene dressing (a ToothPecks Specimen Jar placed beside her, Devil's Tooth decorations). Two routes are implemented:

- **Harbinger route**: Approach holding a ToothPecks Specimen Jar. The jar shatters, the termite bites her, and the boss fight begins (silver aura, hemolytic vials, hair-and-nails slash). If the fight proceeds to near-death she mutates into the Stained Priestess — a more powerful blood-spear phase. The Harbinger route drops `Annetta's Sanguis Lancea`.
- **Unstained route**: Approach holding a Draught of Still Mercy (requires Clarity unlocked). She drinks it, transitions to a cured ally state, and the **Latent Infection** — the suppressed blood magic given physical form — tears itself free as a separate boss. Cured Annetta fights alongside the player until the infection is destroyed. The Unstained route drops `Annetta's Absolution Dagger` and Pale Silver Ingots.

After either route concludes she either lies broken (Harbinger) or stands resolved and at peace (Unstained).

**Remaining WIP:** GeckoLib animation polish; the fuller teeth/nails/hair biological domain for Phase 1; Sanguis Lancea projectile rendering.

---

## 12. The Qliphoth — Tree of Death

In Kabbalistic tradition, the **Sephiroth** are the ten attributes/emanations of God arranged in the **Tree of Life**. The **Qliphoth** are their opposites — the Tree of Death, the shells or husks, the inverse emanations.

Hemomancy draws on this concept not as good-versus-evil but as complementary forces. The **Qliphoth Tree** summoned by the Bloom of the Qliphoth ritual is not an evil thing. Fungi are the cleanup crew of life — they are not bad because they consume the dead, just as ravens and vultures are not evil for doing the same. They simply occupy the other side of the cycle.

The Qliphoth Tree:
- Provides **blood volume regeneration** and enhanced regen auras in its area
- Produces **Qliphoth Pome** fruits — void-dark fruits grown around a crystallized blood core. Each pome is one of nine husks of the Qliphoth. Eating one causes a brief expansion of awareness into the void-register — perceived from the outside as darkness — alongside a surge of blood power and reduced manipulation costs. Nine pomes drop from a single tree's lifecycle, each corresponding to one of the nine corrupted shells of the Qliphoth system; eating all nine from a single bloom is the unnamed act that prepares the Eighth Degree, and Apotheosis forces the Fungal Spine from the player's body.
- Whispers through those fruits in a personal register — the voice naming the husk feels directed to the current bearer, not to the crowd at large.
- **Must be summoned through ritual** — placed trees don't work, because you can't cheat god
- Grows with slight random variation each time (no two trees are identical)
- Cannot be felled by ordinary hands. It can only be removed cleanly by the **Rite of Cult Pruning**, which can be performed by either Hemomancers or the Unstained. If the Unstained prune a tree that still bears pomes, those pomes are severed from their source incorrectly — tainted pomes that bring weakness rather than power.

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
| **Chalybeate Sclerite** | Harbingers | Iron-sulfide armor scale from deep vent snails; a Ferric ocean expression of the same living-mineral philosophy behind hematic iron. It is best taken by careful knapping from a withdrawn living animal, not by slaughter. |
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
| **Mnemonic Whale** | Fictional reef megafauna | A slow deep-reef drifter treated by Voyagers as a "living archive" of currents, vents, and reef pulse. Harbinger crews are expected to witness and record, not hunt; field ethics emphasize distance, patience, and nonlethal sampling. |
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
| **Sea Urchins** | Produce calcified blood spines — the natural equivalent of blood being hardened into barbs. The inspiration for the Barbed armor set and the Urchin Morphling. |
| **Chthonians (termites)** | Grow iron-infused mandibles — essentially, hematic iron that grew biologically. They chew through wood (and eventually wooden tools) with their metal jaws. Spawn in Savanna biome termite mounds; each mound contains exactly one Queen. |
| **Barbed Urchins** | Shallow-water expression of iron blood magic — spined aquatic creatures. |
| **Chalybeate Snails** | Implemented deep-ocean iron snails inspired by the real-world iron-plated snail (Chrysomallon squamiferum). They cluster around hydrothermal vent fields and grow iron-sulfide sclerites that Harbingers prize as Ferric material. Their best harvest is careful knapping while retracted, reinforcing the Order's uneasy covenant with living sources rather than simple predation. |
| **Scarlet Serpents** | Warm-biome blood-serpents with black, red, and yellow warning colors. Their hood flare is taboo folklore in desert and jungle communities, but they are natural territorial wildlife rather than cosmic omens or factional agents. |

**Ocean ecology V1/V2:** Hydrothermal vent fields are the first deep-ocean slice. They are mineral heat-scars on the sea floor, not ore nodes: basalt, blackstone, deepslate, magma, and restrained blood-organic accents where Chalybeate Snails graze. Erythrocoral Reefs are the warm shelf counterpart: fungal-coral habitats with murky red-violet water, Blood Lantern Jellies drifting through the reef, rare Mnemonic Whales moving through deeper shelves, and light Vivacious harvesting. Harbinger Voyager Wrecks are failed reef/vent research craft: covenant field laboratories swallowed by the sea, guarded by Brined Votaries who are tragic remnants of duty rather than a new enemy faction. Active Harbinger voyager expeditions now appear as rare Survey Cog vessels: compact moving laboratories and chapels crewed by a captain-scholar Voyager, with an occasional Votary Wayfarer aboard to learn by witnessing rather than by conquest. Whale-associated Mnemonic Ambergris is framed as an offered or shed sample, never a hunting prize. Trade, rumors, and faction tension remain future maritime hooks without flattening Harbingers into villains.

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

*For mechanics, systems, blocks, items, mobs, and code details, see [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md).*
