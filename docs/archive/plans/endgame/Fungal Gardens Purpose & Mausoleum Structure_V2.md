# Fungal Gardens Purpose & Mausoleum Structure (V2)

Oct 27, 2026 · @Vi · V2. V1 is kept alongside this file. Companion: `Endgame Path Asymmetry Fungal Gardens  Saint Mausoleums_V2.md`, which covers the path split, possession and doors.

The two questions share an answer if the places are linked: the Gardens hold what was taken, and the Mausoleums hold what was kept. Each place should be incomplete without the other, following the "everything points somewhere else" rule and the preservation-and-witness identity.

## Changes from V1

- The Core is no longer "the site of the Severance". Severance is the player's act at the Qliphoth tree, not a place.
- The Antecedent and sculk are removed from the Gardens. They don't touch D7 or D8.
- Velorum's tendencies are Congeatio and **Tenebris** ("Umbral" is the enzyme name).
- Hemorath is a powerful early Hemomancer, not a Harbinger. Erythravane was the first Vicar and Archon.
- Martyrdom framing is removed. The Penance room becomes the Downfall.
- Station is renamed **Chapel**, and the room types are renamed to avoid collisions with existing names.
- Saint fights and Chapels are D8. Outer halls open from D3 to D4.
- The ring door works in two steps: Chapel nodes light up, then the Silentium projects to connect them.
- Saint gifts are reworks of the four existing Canon Memories, not new slots.

## Lore foundations

- **The Gardens are a projection.** The Fungal Gardens dimension is a three-dimensional projection, into the space above the player's world, of a higher-dimensional being reaching down into real space. The dimension doesn't contain the being; it is the being's cross-section where it touches ours.
- **The Qliphoth tree is will and antenna.** It manifests the player's own will, and it is also a beacon to the hivemind.
- **Severance** is the player deliberately cutting that tree (the Rite of Pruning). The cut is a physical ego death, and it severs their ties to the hivemind as a whole. It is what sets them on Vesper's path. It is an act, not a location.
- **The sculk was the hive's first attempt.** It tried to infect the world that way, failed and stagnated. The Antecedent Inquiry tells that history, and it has no bearing on the present state of the world or on D7/D8 progression.
- **"Study" is the closest human word.** When this doc says the hivemind studies or keeps something, the word is borrowed. Its reasons aren't human reasons.

## 1. Why be in the Fungal Gardens

**Core premise: the incursion doesn't destroy, it digests and keeps.** Everything it has consumed still exists inside it in some form. That makes the Gardens the largest archive in the mod, held by something that doesn't want to give it back.

### Depth layers

| Layer | What's there | Why go |
| --- | --- | --- |
| Fringe | Fruiting growth, blooms, rendering pools, morphic pools | Materials and alchemical reagents; routine runs |
| Archive | Facsimiles: the hive's warped recreations of Earth life (bats, wolves, golems and more, the same creatures possession borrows), and the memories it holds | Recovery: Saint recollections, lost memories, Blood Memory fragments |
| Core | Where the projection is densest, the heaviest point of the being's touch-down | Apotheos endgame: the Mycophant and the heir thread (proposed) |

The facsimiles are not infected Earth-life. They are the organism's own attempts to learn and rebuild what it consumed, gotten grossly wrong. This keeps the Gardens' native life alien.

### Per-path reasons

- **Apotheos: a home.**
  - Plant a heart-node that grows into a private, Fane-like sanctum that exists only in the Gardens.
  - It is their respawn point, it grants a bonus while they stand inside it, and it is their workshop.
  - It is the only place they can **molt**: re-shape their morphology or realign their tendencies.
  - The Archive is open to them.
- **Silentium: recollection.**
  - The hivemind keeps the Saints' powers in the Archive.
  - Recreating a Saint's gift needs that memory, and a Silentium can only reach it by possessing a facsimile.
- **Everyone else: no way in.** The Gardens are for Harbinger D8 players. D7 Archons see the dimension once, through the first forced projection. Unstained and unaligned players never enter. A possible far-future Unstained invasion rite stays parked.

## 2. Mausoleum structure

### The Saints

- All four were infected Hemomancers of great power, feared and revered. Each unlocked a singular gift, whether on purpose or not, and that gift became their downfall.
- **Hemorath** was never a true Harbinger. He was one of the first and most powerful Hemomancers. His successors and students feared they would be lost to bloodlust as he was, and that fear shaped the tradition that Archon Erythravane, the first Vicar and Archon, later organised into the Hematic Order at the Incarnadine Fane.
- The other three were never associated with the Order. Either they predate it or they lived far from it; that is left to interpretation.
- By this late stage the player has mastered most mundane powers. Their task now is to rediscover the Saints' gifts for themselves.

| Saint | Tendencies | Gift |
| --- | --- | --- |
| Hemorath | Animus, Ferric | Eternal bloodlust (the affliction) |
| Seraphae | Lux, Ductilis | Cursed, uncontrollable healing |
| Putriciel | Mortem, Flammeus | Consecration corruption |
| Velorum | Congeatio, Tenebris | Perfection through stillness |

Saints are never killed. Seraphae is contained, Hemorath reforms, Putriciel rests and then the rot reclaims them, and Velorum returns to dormancy.

### Per-Saint mausoleums

| Saint | Core mechanic | Signature hazard | Enraged (forced) fight | Gift |
| --- | --- | --- | --- | --- |
| Hemorath | Blood basins built to ration his absorption | Player blood drains toward him, faster with depth; full basins slow the drain | He begins refilling, swelling toward his bloated form as the fight goes on | Passive siphon: blood answers your presence |
| Seraphae | Chains and containment anchors | Any healing causes overgrowth (swelling, involuntary flinches, damage over time). Broken anchors let her radiance reach deeper. | She reaches to heal you, and every touch makes it worse | A healing pulse that also mutates whatever it touches |
| Putriciel | Cycling rot phases and burn phases | Rhythm puzzle: fungal spread alternates with flame purges sweeping the halls. Apotheos hyphae lengthen the rot phase. | No lucid burn windows; the man beneath never surfaces | A purge that burns infection, then lets it regrow stronger |
| Velorum | The frozen congregation | Stillness kills: lingering slows, then freezes. His Repose Chapel inverts into one where you cannot stop. Devotee poses mark safe paths and answers. | The congregation thaws and fights against you. In a proper awakening, the devotees you woke fight beside you instead. | Arrested circulation: freeze a target while its mind stays aware |

The uncommitted Saint pass already covers part of the hazard column:
- Hemorath's proximity drain and leaking meter (`HemorathSiphonRules`)
- Velorum's stillness meter (`VelorumStillnessRules`)
- Putriciel's burn only inside the cleansing window (`PutricielCleansingRules`)
- Seraphae's growth transfusion (`SeraphaeGrowthRules`)

The frozen congregation and Hemorath's swelling are still missing.

### Walk the Saint's life backwards

Players enter at the Saint's rest and move back toward who they were. The chamber is where it all began, not only where the body lies. Velorum's complex is his own frozen monastery, and no one built it for him. That makes it the clearest case: the deepest room is where his stillness started.

### Layout and degree tiers

All four complexes share the same parts, generated by jigsaw. Each Saint's designs vary widely to suit their aesthetic and lore.

| Part | Who gets in | What's there |
| --- | --- | --- |
| Entrance, antechamber | Anyone | Lore, inscriptions, the first sight of the Saint's theme |
| Outer trial chambers | Harbingers from about D3 to D4 | Completable trials with early loot chests. The existing `blood_trial_altar` and `offering_gate` loop, plus the orphaned mausoleum chest tables, belong here. |
| Inner seal | D8 | The point where lower players can't go further |
| Three Chapel slots | D8, either path | The Saint's life, told backwards |
| Nave, ring door | D8 | The Silentium projects; the Apotheos shatters |
| Chamber | D8 | The Saint, and the start of their story |

### Chapel types

Each Chapel is drawn from a deed in the Saint's life.

| Chapel | What happens |
| --- | --- |
| Beholding | A memory replays as a re-enactment. The player must observe correctly; something in the scene answers the next room. |
| Litany | An Abocipher inscription must be decoded to find the right order of offerings. |
| Downfall | The Saint's fall to the power of Hemomancy, played as a combat or hazard gauntlet |
| Repose | Stillness under threat. Silentium players have an edge, but anyone can do it. |

### The ring door

- The door has three rings, one per Chapel.
- Completing a Chapel lights its node on the door, so the door shows progress.
- When all the nodes are lit, the Silentium projects into the door to connect them. The rings fill with blood and the door opens.
- The Apotheos can shatter the door instead, at any time.
- Full detail is in the companion doc.

### Skipping Chapels (Apotheos)

- An Apotheos who reaches the door can force it at any time, skipping every Chapel.
- The enraged Saint carries the power of every Chapel the player didn't complete. Skip the Downfall and it fights with its fall; skip the Repose and it never pauses.
- In gameplay terms this is a Terraria-style enrage. In lore terms it is the sum of what the player refused to see.
- It works in reverse too: each Chapel a Silentium completes reveals a tell or removes a phase from the fight.

### Linking the Gardens and the Mausoleums

Recreating a Saint's gift takes two things, woven together at the Somatic Loom. Each path finds one easy and the other hard.

| Ingredient | Where | Silentium | Apotheos |
| --- | --- | --- | --- |
| Hallowed Residuum | The Saint's mausoleum, after the fight | Easier: Chapels completed, Saint fought as designed, whole residuum | Harder: forced entry, enraged Saint, hallowed ground burns, a fragment per fight |
| Recollection (the memory, or the reagent for it) | The Gardens' Archive | Harder: possession rite, Concentration counter, Mind Broken risk | Easier: free to come and go |

In multiplayer, the two paths have something to trade.

### Reward

- Hallowed Residuum and the Recollection, woven at the Somatic Loom, let the player rediscover that Saint's gift as a new power.
- The gifts **replace the four Canon Memories** (Crimson Tithe, Unclosing Eye, Bloom of Rot, Endless Hour). Each one gets a full rework to fit this lore and be more fun.
- The `memory_weaving` recipe format already takes a residuum catalyst plus the Saint's two enzymes, so the Recollection becomes one more catalyst.

## Canon changes this design makes

- **`docs/LORE_REFERENCE.md` 634, 654 and 1038:** they call Hemorath "the first Harbinger". He becomes a powerful early Hemomancer who was never a Harbinger.
- **LORE_REFERENCE 325 and 430:** add that Erythravane was the first Vicar as well as Archon.
- **LORE_REFERENCE 609:** the Vicar sending players to the Saints at D3 to D4 now means the outer halls. The fights are D8.
- **LORE_REFERENCE 1051:** "None of it is built yet" is stale now that the Saint powers pass exists.
- **LORE_REFERENCE 1284:** "the exact nature of the severance". Severance is now defined as the Qliphoth cut. The Antecedent's old "cut" needs its own wording so the two don't blur.
- **Add:** the Gardens as a projection of the higher-dimensional being, and the Qliphoth tree as will and antenna.
- **Add:** hallowed ground as the Saints' residue rejecting the hive (see the companion doc).

## Open questions

- [ ] Mechanical design of each Saint's gift as a learnable power (the Canon Memory rework)
- [ ] Heart-node sanctum: its bonus, its footprint, and how it shares or splits code with the Founding Fane
- [ ] Molting: what exactly can be re-shaped, and at what cost
- [ ] Core: confirm it as the Mycophant's domain and the heir thread (`mycophant_heir` is written but never read)
- [ ] Hallowed Residuum fragments: how many make a whole one
- [ ] Unstained invasion rite (far future, parked)
