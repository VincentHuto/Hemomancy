# Hemomancy Gameplay and Discovery Loop Roadmap

> Working design document for making Hemomancy clearer to discover and less tedious to play.
>
> Date: 2026-04-26
> Scope: gameplay structure, discovery loops, exploration, lore delivery, and player-facing progression.
> References: `LORE_REFERENCE.md`, `HEMOMANCY_REFERENCE.md`, `MNA_COMPATIBILITY_BRAINSTORM.md`

---

## 1. Purpose

Hemomancy already has a large amount of strong content: Harbinger degrees, blood memories, manipulations, rites, scars, morphlings, bloodlines, the Unstained path, Blood Moons, Saints, sanctums, Qliphoth progression, and the Fungal Dimension. The main problem is not lack of features. The main problem is that many features can feel like separate islands.

The plan connects those islands into clearer gameplay and discovery loops:

- Players should learn systems by investigating the world, not by reading isolated instructions.
- Lore should reveal mechanics, and mechanics should reveal lore.
- Progression should feel like following traces through blood, ruins, witnesses, and ritual consequences.
- Repetition should be reduced by replacing chores with decisions, preparation, and meaningful returns to earlier places.
- Harbinger and Unstained progression should stay morally gray, mutually exclusive, and opt-in.

---

## 2. Current Diagnosis

### 2.1 What Works Already

| Existing Strength | Why It Matters |
|---|---|
| Harbinger degree progression | Gives the mod a strong ceremonial backbone. |
| NPC dialogue trees | Give the player authored guidance and faction tone. |
| Blood memories and the Somatic Loom | Frames manipulations as remembered biology instead of normal spell learning. |
| Scars and vascular systems | Makes the player's body feel like a build surface. |
| Morphlings | Adds living equipment and symbiotic progression. |
| Bloodlines | Gives multiplayer a faction/community identity. |
| Unstained path | Creates a true alternate progression path instead of a simple anti-magic gimmick. |
| Saints | Give the player exploration, ancient history, and mastery checks. |
| Blood Moons | Creates world-level events with cosmological meaning. |
| Qliphoth and Fungal Dimension | Gives endgame a strong eldritch reveal. |

### 2.2 What Feels Disconnected

| Symptom | Likely Player Experience |
|---|---|
| Many systems unlock through separate recipes, GUIs, or items | "I found another machine, but I do not know why it matters yet." |
| Lore is rich but mostly reference-style | "The story is cool, but I am not discovering it through play." |
| Rites, memories, scars, and morphlings each have their own progression | "I am juggling subsystems instead of following a path." |
| Exploration structures do not always point to the next kind of mystery | "I found a place, looted it, and moved on." |
| Resource repetition can dominate | "I need more ingredients before I can do the interesting thing." |
| The Liber Sanguinium exists but does not yet fully carry progression | "The guidebook is not the heart of the mod yet." |

### 2.3 Design Goal

The mod should feel less like a catalog of features and more like an investigation into the player's own altered body.

The player should repeatedly ask:

1. What did I find?
2. What does my blood, purity, tendency, or degree let me understand about it?
3. What preparation do I need?
4. What ritual, trial, or choice does this unlock?
5. What consequence changes my body, base, world, or understanding?

---

## 3. Core Design Pillars

### 3.1 Discovery Before Instruction

Avoid giving every recipe, machine, and mechanic as a flat manual entry first. Let the player find traces, partial diagrams, ritual fragments, contradictory testimonies, or biological symptoms before the full instruction appears.

The guidebook should become a field journal that fills itself through discovery.

### 3.2 Fewer Chores, Stronger Preparation

Tedious gameplay often comes from repeated low-stakes gathering. Preparation is better when it asks for:

- A location choice
- A faction choice
- A timing choice
- A body/build choice
- A risk choice
- A ritual consequence

The player can still need ingredients, but the interesting part should be deciding where and how to obtain them, not repeating the same harvest loop forever.

### 3.3 Every System Should Point Somewhere Else

Each feature should connect to at least two other systems.

Examples:

- A Saint chamber points to a Canon Memory, a scar pattern, and a lore contradiction.
- A Blood Moon points to rare blood samples, Unstained omens, and Qliphoth acceleration.
- A morphling points to a biome, a tendency, a maturity challenge, and a body cost.
- A sanctum points to bloodline cooperation, defenses, NPC visits, and ritual projects.

### 3.4 Lore Should Be Earned In Layers

The truth should not be front-loaded. Use a revelation ladder:

| Stage | What The Player Believes | What Later Complicates It |
|---|---|---|
| Early Harbinger | Blood magic is inherited sacred memory. | Memories are also fungal instructions. |
| Mid Harbinger | The Order preserved ancient wisdom. | The Order may itself be a reproductive structure. |
| Early Unstained | Purification removes corruption. | Purification also embalms and alters the self. |
| Late Unstained | The Pale Lady protects the world. | A defensive force can become autoimmune. |
| Saints | The past was glorious and powerful. | The past was unstable, desperate, and often sealed away. |
| Qliphoth | Endgame power is transcendence. | Transcendence may be surrender to something inhuman. |

### 3.5 Maintain Moral Grayness

The Harbingers are not villains. The Unstained are not heroes. The Fungal Entity is not evil. The Pale Lady is not pure good.

Gameplay should preserve that by making both paths useful, costly, and unsettling.

---

## 4. The Unifying Core Loop

The proposed core loop is:

```text
Discover -> Interpret -> Prepare -> Perform -> Consequence -> Return
```

| Step | Gameplay Form | Example |
|---|---|---|
| Discover | Find a structure, omen, residue, NPC rumor, memory fragment, body symptom, or environmental sign. | A ruined basin pulses during a Blood Moon. |
| Interpret | Use degree, purity, tendency, Liber entries, NPCs, or machines to understand it. | The Somatic Loom identifies it as Ferric/Congeatio residue. |
| Prepare | Gather targeted materials, choose a build, time an event, invite bloodline members, or cleanse yourself. | Bring pale copper tools or a Ferric memory. |
| Perform | Complete a rite, trial, diagnosis, purification, communion, or combat puzzle. | Balance four blood basins while constructs spawn. |
| Consequence | Gain a memory, scar, morphling maturity, lore page, sanctum effect, or faction reaction. | Unlock a Canon Memory and a darker Vicar dialogue branch. |
| Return | Earlier systems update; old places gain new meaning. | The Liber adds a new page, and Blood Moons now reveal related echoes. |

This loop should become the main shape of Hemomancy progression.

---

## 5. Major Proposed Gameplay Loops

### 5.1 Blood Echo Investigation

#### Concept

Add discoverable "blood echoes" or "residues" to structures, mobs, biomes, rites, and boss arenas. These are not full quests by themselves. They are traces that the player can interpret through Hemomancy systems.

#### Player Loop

1. Find a blood residue in the world.
2. Inspect it with an appropriate tool, manipulation, block, or Liber interaction.
3. Learn its tendency signature and possible origin.
4. Bring the residue to a machine or NPC for interpretation.
5. Unlock a recipe, map hint, lore page, trial clue, or ritual variant.

#### Why It Helps

Blood Echoes make exploration naturally point into crafting and progression. Instead of "find recipe in JEI, gather ingredients," the player thinks "this place left a trace, and my blood can read it."

#### Example Echoes

| Echo | Found In | Interpretation | Reward |
|---|---|---|---|
| Clotted Witness | Blood Temple, old battlefields | Early Harbinger testimony | Liber page, rite hint |
| Ferric Residue | Saint chambers, Chthonian mounds | Iron-stabilized blood memory | Hemorath clue, Ferric recipe hint |
| Pale Condensate | Unstained church, cold rivers | Purification trace | Unstained recipe clue |
| Hyphal Pulse | Fungal biome, Qliphoth sites | Entity intrusion | Fungal Whisper variant |
| Verdigris Stain | Unstained relic sites | Old purification attempt | Clarity clue |
| Blackened Pome Husk | Qliphoth area | Endgame communion residue | Apotheos lore |

---

### 5.2 Liber Sanguinium As A Living Field Journal

#### Concept

The Liber should not just explain mechanics. It should become the player's evolving record of discoveries. Entries unlock when the player encounters evidence, performs rites, talks to NPCs, or survives events.

#### Entry Types

| Entry Type | Unlock Source | Purpose |
|---|---|---|
| Rite Fragments | Structures, NPCs, ritual failures | Helps build multiblocks and understand degree gates. |
| Memory Plates | Somatic Loom discoveries | Explains manipulations as remembered biology. |
| Witness Accounts | Harbinger outposts, hermits, ruins | Gives human context without making the Order purely noble. |
| Acolyte Margins | Unstained temples | Gives counter-readings of Harbinger mechanics. |
| Biological Notes | Morphlings, scars, organs, mobs | Connects body-horror systems together. |
| Forbidden Addenda | Fungal Whispers, Qliphoth, Archon events | Revises previous Harbinger assumptions. |

#### Key Rule

The Liber should often show partial information first.

Example:

- Before a Saint is found: "A sealed body beneath stone. Iron without rust."
- After the chamber is entered: basin mechanics are hinted.
- After victory or peaceful extraction: full Canon Memory recipe appears.
- After Archon whispers: the same entry gains a fungal reinterpretation.

#### Why It Helps

This turns documentation into gameplay progression and makes lore cleanup easier. Player-facing lore can live in the Liber, while developer truth stays in reference docs.

---

### 5.3 Tendency Pilgrimages

#### Concept

Each blood tendency should have a small discovery chain that teaches its identity through locations, mobs, materials, and manipulations.

This makes tendencies feel like world philosophies instead of only spell categories.

#### Structure

Each pilgrimage has:

- One natural sign
- One crafted test
- One combat or exploration use
- One memory unlock
- One optional scar or morphling synergy

#### Examples

| Tendency | Natural Sign | Test | Reward Direction |
|---|---|---|---|
| Animus | Growth around old blood | Heal or sustain something living | Life, restoration, morphling bonds |
| Flammeus | Heat in fresh wounds or ash | Burn without consuming the offering | Fire, volatility, transformation |
| Ductilis | Nerve-like strands, motion, reflex | Redirect a moving force | Speed, control, conductive motion |
| Lux | Pale glare, exposed truth | Reveal what is hidden | Light, witness, perception |
| Mortem | Rot without decay, clean endings | End a lingering effect | Death, finality, severance |
| Congeatio | Frozen flow, halted blood | Stop movement or stabilize a wound | Ice, stasis, preservation |
| Ferric | Iron-rich clots, rustless blood | Harden or magnetize blood | Metal, armor, permanence |
| Tenebris | Blind spots, shadowed veins | Hide from perception | Darkness, silence, absence |

#### Why It Helps

Players learn by doing, and each tendency becomes a reason to explore certain spaces or try certain builds.

---

### 5.4 Rite Blueprints And Ritual Failure As Guidance

#### Concept

Rites should be discoverable as incomplete ritual blueprints. Failed rites should teach the player instead of simply wasting time.

#### Blueprint Stages

| Stage | What Player Has | What It Shows |
|---|---|---|
| Fragment | A partial diagram or NPC clue | Theme, shape silhouette, missing catalyst |
| Draft | Enough blocks identified | Most of the multiblock and blood cost range |
| Complete Rite | Final catalyst or degree gate met | Exact pattern, cost, duration, consequence |

#### Ritual Failure Feedback

Instead of a generic failure:

- Wrong material: "The blood refuses this stone; it remembers no vessel here."
- Wrong degree: "The rite recognizes a smaller heart."
- Wrong path: "The solution in your veins silences the pattern."
- Missing catalyst: "The center waits for a witness."
- Bad timing: "The moon is closed to this request."

#### Why It Helps

Rituals become puzzles and ceremonies rather than block recipes copied from a screen.

---

### 5.5 Saints As Investigation Dungeons

#### Concept

Saints should become midgame investigation dungeons that bridge lore, combat mastery, tendency identity, and memory crafting.

#### Suggested Loop

1. Hear a rumor from the Vicar, an Outpost ledger, or a Blood Echo.
2. Locate a mausoleum or trial chamber.
3. Read environmental clues about the Saint's tendency pair.
4. Decide whether to align peacefully or provoke the fight.
5. Complete a trial that teaches the boss mechanic.
6. Gain a blood sample, Hallowed Residuum, Canon Memory, scar pattern, and contradictory lore.

#### Peaceful Extraction vs Boss Fight

| Route | Requirement | Reward Feel |
|---|---|---|
| Peaceful extraction | Tendency alignment, correct offering, respectful chamber behavior | "I understood the old blood." |
| Forced fight | Foul Paste, wrong tendency, theft, desecration | "I took power from a sealed history." |

#### Why It Helps

Saints become a high-value bridge between exploration and build identity. They also preserve moral grayness: peaceful respect and violent extraction are both mechanically valid, but narratively different.

---

### 5.6 Blood Moon Opportunity Loop

#### Concept

Blood Moons should be rare windows where several disconnected systems briefly overlap.

#### Proposed Uses

| During Blood Moon | Gameplay Opportunity |
|---|---|
| Harbinger player | Stronger manipulations, rare blood samples, special echoes visible. |
| Unstained player | Stronger defensive omens, cleansing tasks, Pale Lady whispers. |
| Neutral or early player | Fear, weakness, world danger, curiosity about the conflict. |
| Bloodline group | Shared pool stress test, group defense, rare communal rite. |
| Qliphoth player | Faster pome maturation or darker whisper variants. |

#### Manual Blood Moon Ritual

The manual trigger should feel expensive because it affects the whole world. It should be useful enough to justify, but not something players spam.

Possible uses:

- Summon rare mobs for blood samples.
- Reveal hidden Blood Echoes.
- Open Saint chamber locks.
- Mature a Qliphoth stage.
- Test a Founding Sanctum's defenses.
- Empower a dangerous Grand Rite.

#### Why It Helps

Blood Moons become planned expeditions instead of only background events.

---

### 5.7 Founding Sanctum As A Base-Building Loop

#### Concept

The Founding Sanctum should become the player's long-term Hemomancy base project, not just an area buff.

#### Sanctum Projects

| Project | Purpose |
|---|---|
| Sanguine Archive | Stores discovered Blood Echoes and unlocks Liber entries faster. |
| Covenant Basin | Improves Bloodline pool donation, auto-draw, and group ritual stability. |
| Morphling Cradle Garden | Improves morphling upkeep, maturity training, and passive auras. |
| Scar Theatre | Improves scar crafting, surgery safety, or scar effect tuning. |
| Rite Court | Reduces rite setup friction by validating multiblocks and showing boundaries. |
| Guest Quarters | Encourages Harbinger NPC visits, rumors, and trade. |
| Sealed Cellar | Stores dangerous Qliphoth or fungal materials with consequences. |

#### Why It Helps

The player returns to a meaningful home base, and many systems have a place to connect.

---

### 5.8 Unstained Purification As Pilgrimage, Not Grind

#### Concept

The Unstained path should feel like a brutal pilgrimage of subtraction, clarity, and bodily stillness. It should not feel like repeatedly feeding items into a meter.

#### Proposed Loop

1. Accept purification after seeing enough of the Harbinger path.
2. Receive a list of symptoms, doubts, and cleansing tasks.
3. Visit cold rivers, Lethean Poppy fields, Unstained churches, and old failed purification sites.
4. Use hemolytic tools to remove or forget specific blood attachments.
5. Gain purity, but lose blood-magic comfort and identity.
6. At high clarity, receive whispers that are protective but increasingly severe.

#### Purification Tasks

| Task Type | Example |
|---|---|
| Lethean offering | Bring a wreath or Tears of Silthmere to an altar. |
| Memory release | Voluntarily forget a manipulation for a larger purity gain. |
| Blood refusal | Complete an encounter without casting blood magic. |
| Copper vigil | Hold a site through a night while carrying oxidized copper. |
| Gentle healing | Cure an infected creature or stabilize a wounded NPC. |
| Pruning | Remove a Qliphoth or fungal growth without taking its power. |

#### Why It Helps

The Unstained path becomes an active alternate style of play, not just a meter with anti-blood penalties.

---

### 5.9 Morphling Maturity Through Behavior

#### Concept

Morphlings should mature through behavior that matches their nature, not just generic use or passive time.

#### Examples

| Morphling | Maturity Behavior |
|---|---|
| Leeches | Restore blood under pressure, survive near-empty blood volume, protect allies through transfusion. |
| Chitinite | Block heavy hits, hold a defensive line, survive projectile encounters. |
| Fungal | Heal in fungal or corrupted spaces, survive poison/rot, support allies. |
| Bat | Navigate darkness, reveal hidden entities, survive falls or vertical terrain. |
| Spider | Use walls, traps, or webs during combat. |
| Moth | Use light/dark transitions, survive near death, blind attackers. |

#### Why It Helps

Morphlings become mini-build identities that pull players into different environments and tactics.

---

### 5.10 Lore Contradiction Loop

#### Concept

Some discoveries should revise earlier lore entries. This is especially important for Hemomancy because the central horror is not "evil magic," but the slow realization that several sincere traditions are only partially right.

#### Examples

| Early Claim | Later Complication |
|---|---|
| "Blood memories are ancestral gifts." | They are also fungal instructions. |
| "The Order preserves the truth." | The Order may be the mechanism that keeps the truth useful to the Entity. |
| "Purification makes you clean." | It preserves you by making you less conventionally alive. |
| "The Pale Lady protects." | Protection can become overcorrection. |
| "The Saints mastered blood." | Some were sealed because mastery became indistinguishable from catastrophe. |

#### Why It Helps

This lets lore stay rich without needing huge exposition dumps. The player experiences reinterpretation over time.

---

## 6. Reducing Tedious Gameplay

### 6.1 Replace Repetition With Unlockable Convenience

| Current Tedious Pattern | Better Alternative |
|---|---|
| Repeatedly gather basic blood ingredients | Unlock sanctum cultivation, bloodline support, or machine batching. |
| Manually rebuild ritual patterns | Let completed blueprints validate structures or project ghost outlines. |
| Search randomly for rare structures | Use Blood Echo triangulation, NPC rumors, or Liber maps. |
| Repeat the same purification offering | Rotate between pilgrimage tasks with different play styles. |
| Use machines as disconnected stations | Give each machine a role in interpreting discoveries. |

### 6.2 Keep Ingredients, But Make Them Memorable

Use fewer generic ingredient demands and more named, contextual ingredients:

- A clot taken during a Blood Moon.
- A residue read from a Saint basin.
- Dew gathered at dawn from Lethean Poppies.
- A morphling secretion gained by fulfilling its behavior.
- Copper oxidized by surviving a vigil.
- A blood sample willingly donated by a bloodline member.

### 6.3 Improve Recipe Discovery

Recipe discovery should come from:

- Liber entries
- NPC hints
- Rite blueprint fragments
- Machine interpretation
- Blood Echo analysis
- Trial chamber clues
- Advancements as soft nudges

JEI remains useful for convenience, but the fantasy should not depend on it.

---

## 7. Exploration Cleanup

### 7.1 Structure Roles

Every major structure should have a clear player-facing role.

| Structure | Primary Role | Secondary Role |
|---|---|---|
| Blood Temple | Opt-in initiation and first mystery | Hermit story, first rite blueprint |
| Harbinger Outpost | Midgame guidance and machine education | Rumors, faction warmth, social context |
| Unstained Church | Purification hub | Pale Lady lore, anti-blood tasks |
| Mausoleum / Saint Chamber | Investigation dungeon | Canon Memory and tendency mastery |
| Chthonian Mound | Natural hemomancy ecology | Ferric/chitin materials and Chthonian royal ecology |
| Fungal Biome | Alien ecology | Hyphal echoes, morphling and Qliphoth clues |
| Founding Sanctum | Player-created hub | Bloodline and base projects |
| Fungal Dimension | Endgame revelation | Apotheos choice, cosmic horror |

### 7.2 Rumor Network

NPCs should give rumors that point to structure categories without exact coordinates every time.

Example rumor styles:

- Harbinger Vicar: historical, ceremonial, degree-gated.
- Harbinger Alchemist: material, machine, practical.
- Unstained Acolyte: spiritual, task-based, cautionary.
- Unstained Zealot: recruitment, doubt, warnings.
- Hermit: personal, early, sacrificial.
- Fungal Whisper: invasive, incomplete, sometimes misleading.

### 7.3 Revisit Hooks

Old places should gain new interactions after progression.

| Place | New Reason To Return |
|---|---|
| Blood Temple | Hermit memory, late fungal reinterpretation, hidden basement clue. |
| Harbinger Outpost | Vicar dialogue changes, machine upgrades, bloodline politics. |
| Unstained Church | Clarity tasks, Annetta foreshadowing, pruning missions. |
| Saint Chamber | Scar pattern, peaceful second reading, Archon reinterpretation. |
| Chthonian Mound | Chthonian ecology, chitin Morphling maturity, Ferric trial. |
| Qliphoth Site | Pome cycle, pruning consequence, Apotheos gate. |

---

## 8. Lore Cleanup Plan

### 8.1 Separate Lore Into Three Layers

| Layer | Audience | Contents |
|---|---|---|
| Developer Truth | Mod author and maintainers | Canon answers, hidden mechanics, future plans. |
| Player-Facing Records | Liber, Tome, NPC dialogue | What factions believe, hints, partial truths. |
| Environmental Story | Structures, items, mobs, particles | What the world implies without direct explanation. |

### 8.2 Keep Contradictions Intentional

Contradiction is good when it is authored. It is bad when the player cannot tell whether it is lore or inconsistency.

Recommended rule:

- If two sources disagree, there should be a reason.
- If the reason is not meant to be known yet, the Liber can mark it as uncertain.
- If the docs and code disagree, code wins and the docs should be updated.

### 8.3 Tone Guardrails

| Topic | Avoid | Prefer |
|---|---|---|
| Harbingers | Evil cult, blood villains | Found family, taboo scholars, sacred inheritance |
| Unstained | Holy good faction | Brutal purification, defensive devotion, stillness |
| Fungal Entity | Demon, corruption god | Alien organism, reproductive cycle, cosmic biology |
| Pale Lady | Savior goddess | Defensive force of nature, possible autoimmune danger |
| Blood Moons | Generic combat event | Cosmological immune flare |
| Saints | Dungeon bosses only | Sealed histories with useful but troubling remains |

### 8.4 Suggested Player-Facing Documents

| Document | Form | Purpose |
|---|---|---|
| Liber Sanguinium | Harbinger book | Rites, memories, blood philosophy, discoveries |
| Tome of the Unstained | Unstained book | Purification, still waters, copper/silver, clarity |
| Outpost Ledgers | Structure loot pages | Rumors, historical fragments, machine hints |
| Saint Epitaphs | Chamber inscriptions | Trial clues and moral framing |
| Acolyte Margins | Unstained temple notes | Counter-readings of blood magic |
| Fungal Addenda | Whisper-unlocked entries | Late reinterpretation of previous truths |

---

## 9. Recommended Progression Shape

### 9.1 Harbinger Path

| Phase | Player Fantasy | Main Loop |
|---|---|---|
| Pre-Initiation | Something is wrong and sacred in the world | Find Blood Temple, meet Hermit, choose Mortal Display |
| Neophyte / Votary | Learn blood as power and memory | Basic manipulations, first rites, machine introduction |
| Initiate / Adept | Build identity through tendency and body | Somatic Loom, scars, Saints, morphlings |
| Illuminatus | Create a home and covenant | Founding Sanctum, bloodlines, group rites |
| Sanctified / Archon | Learn the Order's hidden truth | Fungal Whispers, Qliphoth, Ancestral Communion |
| Apotheos Direction | Choose what to do with the truth | Fungal Dimension, silence vs Eighth Degree |

### 9.2 Unstained Path

| Phase | Player Fantasy | Main Loop |
|---|---|---|
| Invitation | Doubt enters the blood path | Zealot plea, hemolytic solution, first sacrifice |
| Corrupted / Tainted | Begin subtraction | Lethean materials, memory release, anti-blood tasks |
| Cleansing / Absolved | Become still and hard to corrupt | Temple tasks, Silver Ward, copper vigils |
| Purified | No longer conventionally alive | Full blood-magic severance, altered body benefits |
| Clarity | See too much | Verdigris tasks, Pale Lady whispers, possible overcorrection |
| Enlightened | Defensive immortality with cost | Rare anti-fungal duties, unresolved moral tension |

### 9.3 Cross-Path Tension

Some content should be viewable from both paths but interpreted differently.

| Content | Harbinger Reading | Unstained Reading |
|---|---|---|
| Blood Moon | Powerful crimson alignment | Costly immune flare |
| Saint Sarcophagus | Ancestor or master | Warning sealed in blood |
| Qliphoth Tree | Fruit of hidden truth | Growth that must be pruned |
| Morphling | Symbiotic companion | Infection made useful but dangerous |
| Bloodline | Chosen family | Shared contamination |
| Fungal Whisper | Revelation | Intrusion |

---

## 10. Implementation Priority Roadmap

This is a design roadmap, not a code plan. Each phase should be broken into implementation tasks later.

### Phase 1: Connect Existing Systems With Low-Risk Content

Goal: improve discovery without inventing major new engines.

- Add or expand Liber/Tome entries for major systems.
- Add more NPC rumor branches using existing dialogue framework.
- Add advancement descriptions that point to the next mystery.
- Add structure loot notes that hint at machines, rites, Saints, and Unstained tasks.
- Add clearer ritual failure messages.
- Add recipe discovery pages for current machines and rites.

### Phase 2: Make Exploration Point To Progression

Goal: give structures stronger roles and revisit hooks.

- Add Blood Echo items or data objects as loot/interaction rewards.
- Add machine interpretation recipes for echoes.
- Add Saint chamber clues and partial Canon Memory unlocks.
- Add Unstained pilgrimage task items tied to cold rivers, poppies, and churches.
- Add Blood Moon-only drops or visibility triggers.

### Phase 3: Build The Sanctum And Bloodline Hub Loop

Goal: turn midgame into a home-base and covenant phase.

- Add sanctum projects.
- Add Bloodline-linked ritual bonuses.
- Add machine convenience upgrades inside sanctums.
- Add NPC visits or rumor updates after sanctum founding.
- Add group Blood Moon defense or rite opportunities.

### Phase 4: Deepen Body Identity

Goal: make scars, morphlings, organs, and tendencies feel like one body-alteration layer.

- Add morphling behavior-based maturity triggers.
- Add tendency pilgrimage achievements or Liber pages.
- Add scar/morphling/tendency synergies.
- Add body-state entries in the Liber.
- Add clearer tradeoffs for body modification.

### Phase 5: Endgame Revelation Polish

Goal: make Archon, Qliphoth, Fungal Dimension, and Apotheos feel inevitable but still opt-in.

- Add late-stage reinterpretations of old Liber entries.
- Add Fungal Dimension discoveries that echo earlier systems.
- Add consequences for silence vs pursuit of the Eighth Degree.
- Add Unstained reactions to Archon/Apotheos discoveries.
- Add pruning and anti-Qliphoth gameplay for Unstained players.

---

## 11. High-Value Concrete Ideas

### 11.1 Blood Echoes

Small discoverable traces that connect exploration to mechanics.

Minimum viable version:

- Echo items drop from specific structures/events.
- Echoes have tendency tags.
- The Liber unlocks pages when an echo is acquired.
- Existing machines process echoes into recipe hints or materials.

### 11.2 Rite Blueprint Fragments

Partial ritual diagrams that build toward complete rite knowledge.

Minimum viable version:

- Add fragment items as structure loot and NPC rewards.
- The Liber tracks collected fragments.
- Completing a fragment set unlocks the rite page.

### 11.3 Saint Epitaph System

Short chamber inscriptions that teach each trial and preserve lore.

Minimum viable version:

- Add readable blocks/items in Saint chambers.
- Each epitaph hints at the Saint's tendency pair and fight solution.
- The Liber records the epitaph after reading.

### 11.4 Unstained Pilgrimage Tasks

Rotating or staged tasks that raise purity/clarity through different play patterns.

Minimum viable version:

- Add staged Acolyte tasks using existing dialogue outcomes.
- Each task grants purity or clarity once.
- Repeatable offerings remain, but are less central.

### 11.5 Blood Moon Expedition Rewards

Rare but predictable reasons to prepare for Blood Moons.

Minimum viable version:

- Add Blood Moon-specific drops to existing special mobs.
- Make certain echoes visible or obtainable only during Blood Moons.
- Add one manual trigger use case beyond atmosphere.

### 11.6 Sanctum Project Board

A base-building progression surface for midgame Harbingers.

Minimum viable version:

- A block or GUI lists sanctum projects.
- Projects consume meaningful materials and unlock convenience or group benefits.
- Projects reinforce the chosen-family identity of bloodlines.

---

## 12. What Not To Do

- Do not make natural infection a random player punishment. The mod should remain opt-in.
- Do not turn Harbingers into villains or Unstained into heroes.
- Do not solve discovery only by adding more JEI-visible recipes.
- Do not make every system mandatory for every player.
- Do not add grind to make content last longer.
- Do not bury all lore in long books before the player has a reason to care.
- Do not make Blood Moons common enough that they become background noise.
- Do not make the Fungal Entity a simple final boss or evil god.

---

## 13. Success Criteria

The redesign is working if:

- Players can explain what they are currently investigating and why.
- The Liber/Tome feels like a growing record of play, not just a manual.
- Finding a structure naturally points to another system.
- Rites feel ceremonial and discoverable, not copied from a recipe list.
- The Unstained path feels like a full alternate loop, not a penalty mode.
- Blood Moons are planned around.
- Saints feel like lore dungeons, not just boss rooms.
- Morphlings and scars feel like body-build choices.
- Players revisit older locations because their understanding changed.
- The moral grayness remains intact.

---

## 14. Short Recommendation

The best first move is to make the Liber Sanguinium and Tome of the Unstained into living field journals, then add Blood Echoes as the common discovery currency.

Those two changes would connect exploration, lore, recipes, rites, Saints, tendencies, Blood Moons, and both player paths without requiring every system to be rebuilt at once. They also give future content a clear pattern:

```text
Find a trace -> understand it -> record it -> act on it -> reinterpret it later
```

That loop fits Hemomancy's strongest identity: not just blood magic as power, but blood as memory, evidence, inheritance, infection, and choice.
