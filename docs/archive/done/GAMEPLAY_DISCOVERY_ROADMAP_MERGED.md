# Hemomancy Gameplay Discovery Roadmap

> Merged design document combining the broad gameplay-loop roadmap with the concrete implementation audit from `C:/Users/Vince/Desktop/Claude Doc.md`.
>
> Date: 2026-04-26
> Status: Design and planning document, not an implementation plan.
> Sources: `docs/GAMEPLAY_DISCOVERY_LOOPS.md`, `Claude Doc.md`, `LORE_REFERENCE.md`, `HEMOMANCY_REFERENCE.md`

---

## 1. Executive Summary

Hemomancy does not need more random features. It needs stronger connective tissue between the features it already has.

The best merged direction is:

```text
Discover -> Interpret -> Prepare -> Perform -> Consequence -> Return
```

Every major system should fit that loop:

- **Discover** a structure, residue, rumor, body symptom, Blood Moon omen, Saint epitaph, or Unstained trace.
- **Interpret** it through the Liber, Tome, NPCs, machines, tendency alignment, purity, degree, or bloodline context.
- **Prepare** with a body build, rite blueprint, morphling, scar set, bloodline support, pilgrimage task, or timed event.
- **Perform** a rite, trial, combat puzzle, cleansing, communion, sanctum project, or investigation.
- **Consequence** changes the player's body, book, base, faction standing, available recipes, lore understanding, or world state.
- **Return** to older places with new context, unlocking a richer reading of what was already there.

Claude's document contributes the concrete backlog. The earlier roadmap contributes the design spine. This merged document keeps both: the poetic investigation loop and the practical list of things worth building first.

---

## 2. Current Reality Check

Claude's document was useful, but some of its "broken" claims are stale against the current `HEMOMANCY_REFERENCE.md`. Treat this table as the corrected baseline before starting work.

| Topic | Claude Claim | Current Reference State | Merged Takeaway |
|---|---|---|---|
| Skill wiring | Several key skills unwired | Reference says all 21 skills are now wired. | Do not spend time "wiring" resolved skills. Instead define clearer earn pacing and player feedback. |
| Vascular debuffs | Vascular sections tracked but no consequences | Reference includes vascular config and debuff toggle; code should be verified before assuming absence. | Focus on visibility, tuning, section-specific identity, and making healing matter. |
| Morphlings | Passive blood sinks with weak effects | Reference says all 12 have maturity abilities, secondary tendencies, and Morphling Cradle support. | Do not rebuild from zero. Add behavior-based maturity, ecology hooks, and stronger discovery. |
| Scars | Opaque acquisition | Scars and effects are implemented, but acquisition clarity is still a strong design gap. | Keep Claude's T1/T2/T3 acquisition chain and resonance idea. |
| Visceral organs / organ echoes | Items have no purpose | Visceral organ effects are resolved in reference; echo-item utility may still need verification. | Add optional echo rites only if the current items still need sinks. |
| Saints | Framework with no loop | Several Saint entities/fights are implemented; chamber generation, models, and some bosses remain WIP. | Focus on Saint trial chambers, epitaph clues, peaceful vs forced resolution, and reward integration. |
| Blood Moons | Needs narrative event shape | Natural Blood Moon mechanics are implemented; manual ritual trigger remains WIP. | Add expedition rewards, phases, and manual trigger use cases. |
| Founding Sanctum | Buff area with little interior structure | Buffs, persistence, and catalyst are implemented; tuning/boundaries remain WIP. | Expand into a midgame base project and bloodline hub. |

---

## 3. Design Pillars

### 3.1 Discovery Before Instruction

The player should not learn Hemomancy only by opening JEI or reading a complete manual. They should encounter traces first: blood residue, torn pages, rite fragments, Saint epitaphs, strange body symptoms, contradictory outpost notes, or Unstained warnings.

The guidebooks should record, clarify, and reinterpret discoveries. They should not replace discovery.

### 3.2 Existing Systems First

Prefer finishing and connecting existing systems before inventing new ones.

High-value existing anchors:

- Liber Sanguinium and Tome/Liber Immaculatus content
- NPC dialogue framework
- Blood Moon event framework
- Vascular system
- Scars and scar station
- Morphlings and Morphling Cradle
- Somatic Loom
- Saints and Hallowed Residuum
- Founding Sanctum
- Bloodlines
- Unstained purity and clarity rites

### 3.3 Less Grind, More Preparation

Tedious loops should become planning loops.

Replace:

```text
repeat gathering -> craft item -> repeat gathering
```

With:

```text
find clue -> choose method -> prepare body/base/timing -> perform event -> unlock convenience
```

Ingredients can still matter, but they should be memorable:

- A clot taken during a Blood Moon.
- Dew gathered from Lethean Poppies at dawn.
- Residue from a Saint basin.
- A morphling secretion earned by matching its behavior.
- Copper oxidized by surviving a vigil.
- A blood sample willingly offered by a bloodline member.

### 3.4 Moral Grayness Must Survive Mechanics

Do not let gameplay accidentally flatten the lore.

| Faction / Force | Avoid | Prefer |
|---|---|---|
| Harbingers | Evil blood cult | Found family, taboo scholars, sacred inheritance, hidden infection |
| Unstained | Good holy church | Brutal purification, stillness, defensive devotion, bodily cost |
| Fungal Entity | Demon or final evil god | Alien organism, reproductive strategy, cosmic biology |
| Pale Lady | Savior goddess | Local immune force, defensive nature, possible autoimmune danger |
| Saints | Loot bosses | Sealed histories, old power, useful but troubling remains |

---

## 4. The Main Merged Loop

### 4.1 Blood Echoes As The Common Discovery Currency

Blood Echoes are the strongest unifying idea. They can connect exploration, lore, recipes, machines, Saints, Blood Moons, Unstained sites, and endgame revelations without requiring every system to be rewritten.

#### Basic Version

- Echoes are items, data fragments, or unlock flags found through structures/events.
- Each echo has a tendency, faction, source, and revelation tier.
- The Liber/Tome records the echo when found.
- Machines or NPCs can interpret the echo into recipe hints, rite fragments, map hints, lore pages, or materials.

#### Example Echoes

| Echo | Found In | Interpretation | Reward |
|---|---|---|---|
| Clotted Witness | Blood Temple, ruined battlefield | Hermit or early Order testimony | Liber page, initiation hint |
| Ferric Residue | Chthonian mound, Saint chamber | Iron-stabilized blood memory | Ferric tendency clue, Hemorath hint |
| Pale Condensate | Unstained church, cold river | Purification trace | Tome page, purity task |
| Hyphal Pulse | Fungal biome, Qliphoth site | Entity intrusion | Fungal addendum, whisper variant |
| Verdigris Stain | Old failed cleansing site | Saturated copper memory | Clarity clue |
| Blackened Pome Husk | Qliphoth bloom | Endgame communion residue | Apotheos clue |

#### Why It Wins

Blood Echoes let one mechanic ask the player to explore, interpret, revisit, and prepare. They also match Hemomancy's core fantasy: blood as memory, evidence, inheritance, infection, and choice.

---

## 5. Guidebooks As Living Field Journals

### 5.1 Highest Priority Fix

The guidebook problem should be the first milestone:

- Re-enable or replace the inactive Java progression entries.
- Surface existing HutosLib JSON book content.
- Make the Harbinger and Unstained books unlock entries through play.
- Use books as the record of discoveries, not just manuals.

### 5.2 Entry Types

| Entry Type | Source | Purpose |
|---|---|---|
| Rite Blueprint | Fragments, NPCs, failures | Shows ritual shape, cost, catalyst, and degree gate progressively. |
| Blood Echo Record | Echo acquisition / interpretation | Records discovered traces and their likely origin. |
| Memory Plate | Somatic Loom discovery | Explains manipulations as remembered biology. |
| Scar Diagram | Scar station, Saint reward, loot | Shows scar acquisition chain and resonance hints. |
| Morphling Note | Incubator, Cradle, ecology task | Explains maturity behavior and tendency affinity. |
| Saint Epitaph | Trial chamber inscription | Hints at boss/trial solution and moral framing. |
| Acolyte Margin | Unstained church | Gives counter-readings of Harbinger assumptions. |
| Fungal Addendum | Whisper, Qliphoth, Archon event | Reinterprets earlier Harbinger lore. |

### 5.3 Progressive Revelation

Entries should update in layers:

1. **Rumor:** vague title, silhouette, or contradiction.
2. **Trace:** partial recipe, tendency, or location category.
3. **Understanding:** complete player-facing instruction.
4. **Reinterpretation:** later truth complicates the earlier entry.

Example:

| Stage | Saint Entry Text Shape |
|---|---|
| Rumor | "A sealed body beneath stone. Iron without rust." |
| Trace | "Four basins remember a measured loss." |
| Understanding | Shows Hemorath's chamber mechanic and reward path. |
| Reinterpretation | Archon-tier addendum suggests the Saint was sealed as part of the fungus' long experiment. |

---

## 6. Concrete Systems To Build Or Refine

### 6.1 Rite Blueprint Fragments

Rites should feel ceremonial and discovered, not pasted from JEI.

#### Loop

1. Find a fragment in a structure, NPC reward, echo interpretation, or Blood Moon event.
2. The book records a partial ritual diagram.
3. More fragments fill in blocks, catalyst, blood cost range, and degree requirement.
4. Failed attempts produce poetic but useful feedback.
5. Completed rites become reusable known ceremonies.

#### Failure Feedback Examples

| Failure | Message Style |
|---|---|
| Wrong material | "The blood refuses this stone; it remembers no vessel here." |
| Wrong degree | "The rite recognizes a smaller heart." |
| Wrong path | "The solution in your veins silences the pattern." |
| Missing catalyst | "The center waits for a witness." |
| Wrong timing | "The moon is closed to this request." |

### 6.2 Skill Point Economy And Feedback

Even if skills are wired, the player still needs a clear earn loop.

#### Recommended Sources

| Source | Suggested Reward |
|---|---|
| Degree advancement | +1 skill point |
| First manipulation kill per tendency | +1, once per tendency |
| First Saint discovery / completion | +1 per Saint milestone |
| First completion of notable utility rites | +1 per rite family or tier |
| Founding Sanctum project completion | +1 for major projects |
| Unstained purity / clarity milestones | Equivalent points or separate clarity unlocks |

#### Rule

Skill points should reward breadth and milestone play, not raw grinding. Avoid points for every cast, every kill, or every repeated craft.

### 6.3 Vascular Consequence And Visibility

The vascular system should be a body-management layer players notice without becoming a constant nuisance.

#### Recommended Direction

- Verify current debuff implementation before adding new code.
- If debuffs exist, tune them by section and improve UI feedback.
- If debuffs are weak or generic, give each section a distinct identity.
- Make Vascular Mending and related healing choices matter.

#### Section Identity

| Section | Gameplay Identity |
|---|---|
| Head | Perception, confusion, manipulation focus |
| Arms | Attack damage, tool use, cast stability |
| Legs | Movement, jump, escape |
| Spine | max blood, regeneration, resilience |
| Torso / Heart | armor, bleed vulnerability, emergency survival |

#### Guardrail

Vascular penalties should create pressure, not punish experimentation. They work best when players can predict, inspect, and recover from them.

### 6.4 Scar Acquisition And Resonance

Scars already have strong thematic potential. The missing piece is a clean path from first scar to mastery.

#### Acquisition Chain

| Tier | Acquisition |
|---|---|
| T1 | Blood Temple / Harbinger Outpost loot, common scar station recipes, early Blood Echo interpretation |
| T2 | T1 scar + thematic mob drop + tendency reagent |
| T3 | Saint trial completion, Hallowed Residuum, rare chamber inscription, or high-degree scar station recipe |

#### Resonance Combos

Equipping linked scars can unlock a bonus that makes experimentation feel personal.

| Combo | Bonus Idea |
|---|---|
| Heart scar + eye scar | Low-health enemies glow briefly. |
| Ferric scar pair | Ferric manipulations pull nearby items or enemies slightly. |
| defensive scar pair | Chance to create a temporary absorption shell on hit. |
| Lux + Tenebris scars | Alternating light/dark combat bonus. |
| Animus + Mortem scars | Healing on kill becomes stronger after applying decay. |

#### Guardrail

Scar resonance should reward theme and build identity, not create mandatory best-in-slot pairs.

### 6.5 Morphling Behavior-Based Maturity

Morphlings now have maturity abilities, so the next step is making maturity feel earned through behavior.

| Morphling | Maturity Behavior |
|---|---|
| Leeches | Restore blood under pressure, survive near-empty blood volume, protect allies through transfusion. |
| Chitinite | Block heavy hits, withstand projectiles, defend a sanctum or bloodline member. |
| Fungal | Heal or survive in corrupted spaces, support allies, endure poison/rot. |
| Bat | Navigate darkness, reveal hidden entities, survive vertical traversal. |
| Spider | Use walls, webs, traps, or ambush terrain. |
| Moth | Use light/dark transitions, blind attackers, survive near death. |
| Mole | Mine underground, detect buried entities, fight below a certain depth. |
| Serpent | Strike from stealth or movement, poison chained targets. |

#### Cradle Integration

The Morphling Cradle should make morphlings feel like living companions at base:

- Cradled morphlings can train or stabilize.
- Sanctum projects can improve cradle range/upkeep.
- Bloodline members can benefit from hosted support effects.
- Mature morphlings can produce rare secretions for rites, scars, or echoes.

### 6.6 Saints As Investigation Dungeons

Saints should bridge lore, exploration, build identity, and high-tier rewards.

#### Loop

1. Hear rumor or find Blood Echo.
2. Locate chamber through triangulation, book hint, or outpost record.
3. Read Saint epitaphs and environmental clues.
4. Infer tendency pair and chamber rules.
5. Choose peaceful extraction or forced fight.
6. Earn Hallowed Residuum, Canon Memory, T3 scar path, and lore complication.

#### Peaceful Vs Forced

| Route | Requirement | Feel |
|---|---|---|
| Peaceful extraction | Matching tendency, correct offering, respectful interaction | "I understood the old blood." |
| Forced fight | Wrong tendency, Foul Paste, theft, desecration | "I took power from sealed history." |

#### Priority

Finish chamber/worldgen and clue delivery before adding more Saint concepts.

### 6.7 Organ Echo Utility Rites

Use this only if organ echo items still need purpose after verifying the current visceral organ implementation.

| Echo | Rite | Effect |
|---|---|---|
| Heart Echo | Cardiac Surge | Short burst of blood regen and movement. |
| Lung Echo | Vital Breath | Temporary drowning/suffocation immunity and poison cleanse. |
| Kidney Echo | Purging Flow | Clears debuffs, followed by brief vulnerability. |
| Liver Echo | Blood Refinement | Next few manipulations cost less; regen pauses during window. |
| Spleen Echo | Hemolytic Surge | Temporary tendency amplification with a cost. |

These should be utility rites, not permanent mandatory upgrades.

### 6.8 Blood Moon Expedition Loop

Blood Moons should become rare, planned expeditions.

#### Three-Phase Shape

| Phase | Harbinger Experience | Unstained Experience |
|---|---|---|
| Rising | Book glows, echoes become visible, mobs begin stirring | Hemolytic tools sharpen, Pale Lady omen appears |
| Full | Strongest blood power, rare samples, dangerous rite window | High pressure, anxiety, urgent cleansing tasks |
| Setting | Rewards settle, fatigue or aftereffect begins | Cleansing potency peaks, chance to seal traces |

#### Uses

- Reveal hidden Blood Echoes.
- Open or weaken Saint chamber seals.
- Enable a manual Blood Moon rite.
- Mature a Qliphoth or expose a pome clue.
- Spawn rare blood sample mobs.
- Stress-test Founding Sanctum defenses.

#### Manual Trigger

Manual Blood Moon ritual should be expensive and socially meaningful on servers. It should affect the whole world because it is asking the world to bleed on schedule.

### 6.9 Founding Sanctum As Midgame Hub

The Sanctum should be the place where Harbinger systems finally gather.

#### Project Board

| Project | Unlock / Benefit |
|---|---|
| Sanguine Archive | Stores Blood Echoes, accelerates Liber interpretation. |
| Covenant Basin | Improves bloodline donation/auto-draw and group rite stability. |
| Morphling Cradle Garden | Improves hosted morphling support, maturity, or secretions. |
| Scar Theatre | Improves scar station outcomes or resonance discovery. |
| Rite Court | Validates multiblocks, shows boundary, reduces setup friction. |
| Guest Quarters | Enables NPC rumors, trades, or visits. |
| Sealed Cellar | Stores Qliphoth/fungal materials with benefits and risks. |

#### Tier Shape

| Tier | Identity |
|---|---|
| Tier 1 | Consecrated boundary and basic buffs. |
| Tier 2 | Crafting and interpretation hub. |
| Tier 3 | Bloodline support and communal projects. |
| Tier 4 | High-degree rite court and endgame consequences. |

### 6.10 Spirit Covenant For Solo Players

Bloodlines are thematically important, but solo players need a parallel experience.

#### Concept

A Spirit Covenant rite lets solo players bind echoes of past Harbingers they have discovered through memories, pages, or Blood Echoes.

#### Rules

- Available around Degree 3+.
- Requires a restored memory, echo, or outpost record.
- Each covenanted spirit contributes a small bloodline-like support effect.
- Up to 3 spirits, matching a small multiplayer group.
- Spirits occasionally request tasks tied to their memory.

#### Guardrail

This should not make solo strictly better than multiplayer bloodlines. It should offer a flavorful equivalent.

### 6.11 Living Weapon Evolution

Living/blood-imbued weapons should feel grown, not merely crafted.

#### Example Growth

| Stage | Trigger | Reward |
|---|---|---|
| Tempered | 50 meaningful kills or rites | Minor passive or tendency affinity. |
| Awakened | 150 meaningful uses | Visible change and second passive. |
| Named | 300 meaningful uses + rite | Unique ability tied to tendency. |

#### Guardrail

Avoid raw kill grinding by counting meaningful conditions: manipulation synergy, Blood Moon use, Saint chamber use, or sanctum defense.

### 6.12 Unstained Combat Identity

The Unstained should not feel like "Harbinger but with blood magic removed." They need a still, blunt, cleansing style.

#### Mechanics

| Mechanic | Purpose |
|---|---|
| Hemolytic Coating | Blunt weapons gain purity-scaling anti-blood effects. |
| Purification Strikes | Repeated hits build a taint/purge threshold. |
| Clarity of Form | High purity resists nausea, blindness, confusion, bleed, and hemolysis. |
| Copper Vigil | Defensive task that rewards survival without spilling blood. |
| Pruning Rites | Remove Qliphoth/fungal growth without consuming its power. |

#### Tone

Unstained combat should feel calm, hard, and frighteningly absolute.

---

## 7. Lore And Exploration Cleanup

### 7.1 Three Lore Layers

| Layer | Audience | Contents |
|---|---|---|
| Developer Truth | Maintainers | Canon answers, future plans, hidden causal truth. |
| Player Records | Liber, Tome, dialogue | Partial truth filtered through faction belief. |
| Environmental Story | Structures, particles, loot, mobs | Evidence without direct explanation. |

### 7.2 Contradicting Accounts

Harbinger and Unstained records should sometimes describe the same event differently.

| Event | Harbinger Reading | Unstained Reading |
|---|---|---|
| Initiation | Sacred inheritance awakened | Concentrated infection accepted |
| Blood Moon | Crimson alignment | Costly immune flare |
| Saint sarcophagus | Ancestor or master | Warning sealed in blood |
| Qliphoth Tree | Fruit of hidden truth | Growth requiring pruning |
| Bloodline | Chosen family | Shared contamination |
| Fungal Whisper | Revelation | Intrusion |

### 7.3 Structure Roles

| Structure | Primary Role | Secondary Role |
|---|---|---|
| Blood Temple | Opt-in initiation | Hermit story, first blueprint, first Echo |
| Harbinger Outpost | Midgame guidance | Rumors, machine education, social warmth |
| Unstained Church | Purification hub | Pale Lady lore, pilgrimage tasks |
| Saint Chamber | Investigation dungeon | Canon Memory, T3 scars, contradiction |
| Chthonian Mound | Natural hemomancy ecology | Ferric/chitin materials and Chthonian royal ecology |
| Fungal Biome | Alien ecology | morphlings, echoes, Qliphoth clues |
| Founding Sanctum | Player-created hub | bloodlines, projects, rites |
| Fungal Dimension | Endgame revelation | silence vs Eighth Degree |

### 7.4 Revisit Hooks

| Place | New Reason To Return |
|---|---|
| Blood Temple | Hermit memory, late fungal reinterpretation, hidden fragment. |
| Harbinger Outpost | Vicar branches, alchemist recipes, rumors, NPC visits. |
| Unstained Church | clarity tasks, Annetta foreshadowing, pruning duties. |
| Saint Chamber | epitaph reinterpretation, scar pattern, peaceful second reading. |
| Chthonian Mound | Chthonian ecology, Morphling maturity, Ferric echo. |
| Qliphoth Site | pome cycle, pruning consequence, Apotheos gate. |

---

## 8. Priority Roadmap

### Phase 1: Make The Mod Understandable

Goal: players can see what they are doing and why.

1. Fix/surface Liber and Unstained book entries.
2. Add progressive book unlocks for existing systems.
3. Add better advancement text and NPC hints.
4. Add ritual failure feedback.
5. Verify vascular debuffs and skill-point earn visibility.

### Phase 2: Make Exploration Feed Progression

Goal: structures point into mechanics.

1. Add Blood Echo proof-of-concept.
2. Add Rite Blueprint Fragments.
3. Add outpost/church/temple notes as loot.
4. Add Saint epitaphs.
5. Add Blood Moon-only echo/reward hooks.

### Phase 3: Clarify Body-Build Systems

Goal: scars, morphlings, vascular health, tendencies, and organs feel like one body layer.

1. Add scar acquisition chain.
2. Add scar resonance hints and a few first combos.
3. Add morphling behavior-based maturity milestones.
4. Improve vascular UI feedback and recovery meaning.
5. Verify whether organ echo items need utility rites.

### Phase 4: Build Midgame Home And Social Loops

Goal: Degree 5+ becomes covenant/base play.

1. Add Sanctum project board.
2. Add Covenant Basin / bloodline benefits.
3. Add Morphling Cradle Garden and Scar Theatre projects.
4. Add solo Spirit Covenant equivalent.
5. Add Blood Moon sanctum defense or group rite.

### Phase 5: Deepen Unstained And Endgame Identity

Goal: both paths feel complete and unsettling.

1. Add Unstained pilgrimage tasks.
2. Add Unstained combat identity and pruning gameplay.
3. Add late-stage Pale Lady warnings.
4. Add Fungal Addenda that reinterpret earlier entries.
5. Add consequences for Archon silence vs Eighth Degree pursuit.

---

## 9. Best First Milestone

Build a small vertical slice instead of trying to implement every loop at once.

### Recommended Slice: "The First Trace"

1. Player finds a **Clotted Witness** Blood Echo in or near a Blood Temple.
2. The Liber records a partial entry.
3. The Harbinger Hermit or an early book page explains that blood can remember.
4. The echo unlocks a Rite Blueprint Fragment for Sanguine Initiation or a utility rite.
5. A failed rite gives improved feedback.
6. Completing the rite updates the Liber entry.
7. Later, an Archon-tier Fungal Addendum reinterprets the same entry.

### Why This Slice

It proves the whole merged design in miniature:

```text
find trace -> record trace -> interpret trace -> perform rite -> update book -> reinterpret later
```

If this feels good, the same pattern can scale to Saints, Blood Moons, Unstained pilgrimages, Qliphoth, morphlings, scars, and sanctums.

---

## 10. Things To Avoid

- Do not make natural infection a random player punishment.
- Do not make the Harbingers into villains.
- Do not make the Unstained into pure heroes.
- Do not add grind just to slow progression.
- Do not solve discovery only through JEI.
- Do not make every subsystem mandatory for every player.
- Do not make Blood Moons so common they become background noise.
- Do not make the Fungal Entity a simple final boss.
- Do not implement stale audit items without checking current code/reference state.

---

## 11. Final Recommendation

The best version of Hemomancy is not "more systems." It is a game about reading evidence left in blood.

Use the earlier roadmap as the philosophical spine:

```text
Discover -> Interpret -> Prepare -> Perform -> Consequence -> Return
```

Use Claude's document as the backlog of concrete knobs:

- Liber visibility
- skill point pacing
- vascular consequence
- scar progression
- morphling maturity
- Saint chambers
- Blood Moon phases
- organ echo sinks
- living weapons
- solo covenants
- sanctum projects
- Unstained combat

But filter every task through one question:

> Does this make the player discover, understand, alter, or reinterpret something?

If yes, it belongs in Hemomancy. If not, it is probably just another isolated feature.
