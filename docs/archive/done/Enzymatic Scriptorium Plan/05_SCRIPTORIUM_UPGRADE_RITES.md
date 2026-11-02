# Enzymatic Scriptorium — D5/D7 Cardinal Rite Upgrades

> **Scope:** Visual and mechanical implementation of the station upgrades.  
> **Core decision:** Use the existing Cardinal Rite framework and add narrow rite-ID-specific behavior.  
> **No bespoke failure state is currently part of these rites.**

## 1. General integration rule

Do **not** build a second ritual controller exclusively for the Scriptorium.

Both upgrades should be authored as normal Harbinger Cardinal Rite recipes and use the existing ceremony infrastructure for:

- multiblock floor matching;
- degree access;
- Cardinal Focus / medium handling;
- boundary anchors;
- blood projection;
- lit-brazier offerings;
- Humanity Daemon sealing;
- optional existing support systems where appropriate;
- culmination;
- structure/world-effect completion.

Suggested rite IDs:

```text
hemomancy:cardinal_rite/eightfold_script
hemomancy:cardinal_rite/monolithic_script
```

## 2. Scriptorium-specific hooks

Special code should handle only:

- validating the correct Scriptorium tier inside the ritual space;
- validating preloaded enzyme reservoirs;
- spawning eight Scriptorial Orbs after normal sealing;
- accepting projection into those orbs;
- rendering persistent tendency-colored writing tendrils from completed orbs to matching Scriptorium tubes;
- reacting physical tube fill/pressure to orb completion;
- custom offering-transfer visuals;
- changing Scriptorium tier during culmination.

Everything else remains Cardinal Rite behavior.

## 3. No custom failure state for now

The special inscription objectives are **completion-gated, not failure-gated**.

Current rules:

- wrong/inactive Scriptorial Orb rejects projection;
- no bespoke timeout;
- no Scriptorial Instability meter;
- no custom item destruction;
- no special player-damage failure;
- the rite-specific phase waits until required nodes are complete.

---

# PART I — D5 RITE OF THE EIGHTFOLD SCRIPT

## 4. Identity

**Player-facing name:** Rite of the Eightfold Script  
**Suggested ID:** `hemomancy:cardinal_rite/eightfold_script`  
**Required degree:** D5 / Illuminatus  
**Suggested form:** Greater  
**Rank-up:** false  
**Target station:** Base Enzymatic Scriptorium  
**Result:** Consecrated Enzymatic Scriptorium  
**Unlock:** Overexertion I (`normal max +1`)

## 5. Ritual floor and Scriptorial Seat

Use a normal Greater Cardinal Rite floor.

Author one position inside the matched ritual footprint as:

```text
scriptorial_seat
```

The **Cardinal Focus remains the ritual heart**.

The Enzymatic Scriptorium occupies the Scriptorial Seat as the subject being transformed.

Do not replace the Cardinal Focus with the Scriptorium.

### Validation

Before activation:

```text
subject block at scriptorial_seat
must be Enzymatic Scriptorium
must be BASE tier
```

The Scriptorium must contain:

```text
1 Vivacious Enzyme
1 Fervent Enzyme
1 Neurotic Enzyme
1 Incandescent Enzyme
1 Ruinous Enzyme
1 Frigid Enzyme
1 Ferric Enzyme
1 Umbral Enzyme
```

Suggested failure-to-start feedback:

> **The Scriptorial Seat lacks a complete eightfold circulation.**

## 6. D5 offerings

Use ordinary Cardinal Rite lit-brazier offerings.

Recommended set:

- **1× Vicar's Consecration Kit**
- **1× Lapis Block**
- **1× Blood Crystal Shard**

Meaning:

### Vicar's Consecration Kit

Institutional/Lodge consecration and permanent station hardware.

### Lapis Block

Conventional enchanting at mature/full strength.

### Blood Crystal Shard

Hematic intrusion into conventional magical structure.

Do not require manual pre-installation of the Kit.

## 7. Normal Consecration phase

Use the normal D5 Cardinal Rite boundary:

```text
5 rings
20 boundary anchors
50 mL per anchor
1,000 mL mandatory boundary blood
```

### Rite-ID visual exception

As boundary rings close:

- Scriptorium book turns pages;
- crimson lines faintly reach from the boundary toward the Scriptorial Seat;
- preloaded enzyme tubes begin responding;
- by the final ring, all eight reservoirs visibly pulse.

Presentation only.

## 8. Normal daemon seal

Proceed through existing Cardinal Rite sealing.

The player Blood Projects into the Humanity Daemon as usual.

### D5 special seal visual

```text
Humanity Daemon
→ stretches into thin red-black writing filament
→ filament enters/threads through Scriptorium book
```

Then begin the special objective.

## 9. Eight Scriptorial Orbs

Enter:

# **Eightfold Inscription**

Spawn eight large virtual targets around the Scriptorium on the ritual floor.

They reuse the readable language of blood anchors:

- large comfortable aim targets;
- projection-targetable in world space;
- independent of block-model hitboxes;
- surface-aware placement.

### Layout

Place at the eight compass points relative to the rite's **rotated local frame**:

```text
             N

        NW       NE


    W       [S]       E


        SW       SE

             S
```

Recommended radius:

```text
~3–4 blocks from the Scriptorium
```

adjusted to the Greater footprint.

## 10. Tendency mapping

Example clockwise convention:

| Local position | Tendency | Enzyme |
|---|---|---|
| N | Animus | Vivacious |
| NE | Flammeus | Fervent |
| E | Ductilis | Neurotic |
| SE | Lux | Incandescent |
| S | Mortem | Ruinous |
| SW | Congeatio | Frigid |
| W | Ferric | Ferric |
| NW | Tenebris | Umbral |

This is a ritual layout convention only.

## 11. Orb appearance

Each orb should look related to a Cardinal blood anchor but distinct:

- dark crimson outer blood membrane;
- tendency-colored inner core;
- subtle internal tendency glyph;
- faint vascular hint toward the Scriptorium;
- larger/elongated enough to distinguish it from a normal boundary anchor.

Visual hierarchy:

```text
blood/crimson first
tendency color second
```

## 12. D5 orb fill rules

All eight may be active simultaneously.

No forced order.

Each accepts:

```text
50 mL projected blood
```

Special-phase total:

```text
8 × 50 mL
= 400 mL
```

Combined mandatory blood before optional other costs:

```text
1,000 mL boundary
+ 400 mL Scriptorial Orbs
= 1,400 mL
```

## 13. D5 writing tendril

Completing an orb:

### Orb completion

- orb contracts;
- tendency core brightens;
- small pulse crosses its membrane.

### Tendril writing

A blood-writing tendril grows from the orb across the ritual floor toward the Scriptorium.

It should not be a straight beam.

Use a writing/vascular path:

```text
orb
↓
curved crimson vessel writes across floor
↓
surface-following path
↓
subtle branches/calligraphic turns
↓
matching Scriptorium tube
```

### Color rule

- dark-crimson vascular body;
- tendency-colored illumination/inner current.

### Persistence

The tendril stays visible for the rest of the rite.

By completion, the floor contains an eight-spoked vascular inscription centered on the Scriptorium.

## 14. Tube response

The player never projects into the physical tube.

When the tendril reaches its matching reservoir:

- tube flashes;
- liquid rises/pulses;
- internal tubing carries the tendency toward the book;
- machine visibly receives what the floor inscription wrote.

Design rule:

> **The floor is where the player writes. The tubes show what the machine receives.**

## 15. D5 culmination visuals

When all eight are complete:

```text
8 orbs
→ 8 persistent tendency tendrils
→ 8 physical enzyme reservoirs
→ central book
```

Then offerings visually incorporate.

### Lapis Block

Blue enchanting glyphs peel from its brazier and spiral toward the book.

### Blood Crystal Shard

Crimson crystalline flecks enter central blood channels.

### Vicar's Consecration Kit

Visual pieces assemble onto the station:

- reinforced tube collars;
- pressure valves;
- fine book chains;
- crimson/gold Lodge fittings;
- candles/consecration ornament.

Actual consumption remains governed by Cardinal Rite offering lifecycle.

## 16. D5 completion

During normal Culmination:

```text
BASE
→ CONSECRATED
```

Preserve appropriate facing/persistent station data.

Unlock:

```text
Overexertion I
max supported enchantment level = normal max +1
```

## 17. D5 permanent visual tier

Add:

- reinforced tube collars;
- pressure valves;
- central blood manifold;
- fine chains restraining the floating book;
- small candles;
- crimson/gold Lodge trim;
- visible consecration seals.

The eight glass reservoirs remain the central identity.

---

# PART II — D7 RITE OF THE MONOLITHIC SCRIPT

## 18. Identity

**Player-facing name:** Rite of the Monolithic Script  
**Suggested ID:** `hemomancy:cardinal_rite/monolithic_script`  
**Required degree:** D7 / Archon  
**Suggested form:** Grand  
**Rank-up:** false  
**Target station:** Consecrated Enzymatic Scriptorium  
**Result:** Monolithic Enzymatic Scriptorium  
**Unlock:** Overexertion II (`normal max +2`)

## 19. D7 validation

The Scriptorial Seat must contain:

```text
Consecrated Enzymatic Scriptorium
```

Preload:

```text
2 Vivacious
2 Fervent
2 Neurotic
2 Incandescent
2 Ruinous
2 Frigid
2 Ferric
2 Umbral
```

The renderer shows each reservoir overfilled into its upper chamber / beyond its D5 safe graduation.

## 20. D7 offerings

Recommended lit-brazier offering set:

- **1× Monolithic Cornerstone**
- **4× Monolith Fragments**
- **1× Sanguine Quintessence**
- **2× Lapis Blocks**

Total: eight physical offering items.

| Offering | Ritual purpose |
|---|---|
| Monolithic Cornerstone | final structural heart / station-tier anchor |
| 4 Monolith Fragments | four permanent restraints around inscription focus |
| Sanguine Quintessence | living medium joining blood and impossible inscription |
| 2 Lapis Blocks | conventional enchanting substrate under extreme compression |

## 21. Normal D7 Consecration

Use ordinary D7 boundary:

```text
7 rings
28 anchors
50 mL each
1,400 mL mandatory boundary blood
```

### Rite-ID visual exception

As rings close:

- pairs of enzyme reservoirs climb farther into overfill chambers;
- black-red geometry begins appearing around Scriptorial Seat;
- Monolith Fragment offerings subtly react/lift;
- book chains progressively tighten.

## 22. Normal sealing

Use existing Humanity Daemon sealing.

### D7 seal visual

After seal:

- daemon is drawn toward Scriptorium;
- pale/red form appears suspended in/above open book;
- Sanguine Quintessence forms a translucent blood sphere around inscription focus;
- Monolith Fragment offerings respond.

Then begin special objective.

## 23. D7 Scriptorial Orbs

Reuse the **same eight floor positions** and tendency mapping as D5.

Do not spawn sixteen targets.

Each orb has **two fill stages**.

Recommended appearance:

- crimson outer membrane;
- tendency core;
- two concentric colored nuclei or two orbiting motes;
- clear two-step fill feedback.

## 24. D7 fill rules

Each orb accepts:

```text
First charge: 50 mL
Second charge: 50 mL
```

Each tendency:

```text
100 mL
```

Across eight:

```text
8 × 100 mL
= 800 mL
```

Combined mandatory authored blood before optional other costs:

```text
1,400 mL boundary
+ 800 mL double Scriptorial inscription
= 2,200 mL
```

## 25. First D7 charge

First 50 mL:

- first nucleus fills;
- initial writing tendril grows to matching tube;
- tube fills normal lower/primary chamber;
- tendril remains visible.

## 26. Second D7 charge

Second 50 mL:

- second nucleus fills;
- existing tendril pulses;
- second vascular strand grows alongside/coils around first;
- tube fluid rises into **upper Overexertion chamber**;
- local black-red restraint geometry answers.

Final floor contains eight **double-written vascular cords**, not sixteen unrelated lines.

## 27. Monolith Fragment reaction

Recommended visual milestones:

```text
2 fully doubled tendencies
→ Fragment 1 rises/aligns

4
→ Fragment 2

6
→ Fragment 3

8
→ Fragment 4
```

Pure presentation; no fragment belongs metaphysically to a tendency.

## 28. D7 floor-script completion

When all eight are double-filled:

- all double tendrils pulse inward;
- eight overfilled reservoirs answer simultaneously;
- Monolith Fragments move inward;
- Cornerstone rises;
- book strains against D5 chains;
- black-red structural lines connect fragments to Cornerstone.

The rite should look **dangerously precise**, not chaotic.

## 29. D7 offering incorporation

### Monolith Fragments

Become four permanent restraints around inscription focus.

### Monolithic Cornerstone

Locks visibly into lower/rear station body and remains visible permanently.

### Sanguine Quintessence

Collapses through book/inscription core.

### Two Lapis Blocks

Dissolve into dense blue enchanting glyphs drawn through the book.

### Humanity Daemon

Final temporary figure to vanish:

```text
daemon eye remains visible in pages
↓
Monolithic frame seals
↓
eye closes
↓
daemon disperses
```

## 30. D7 completion

During normal Culmination:

```text
CONSECRATED
→ MONOLITHIC
```

Unlock:

```text
Overexertion II
max supported enchantment level = normal max +2
```

No D8 modifier.

## 31. D7 permanent visual tier

### Enzyme reservoirs

Remain glass and visible, gaining:

- black-red stone footing;
- heavy Hematic Iron collars;
- upper Monolithic restraints;
- crimson seams connecting to Cornerstone.

### Book progression

```text
D3: free-floating
D5: chained
D7: chained inside a rigid Monolithic frame
```

The frame must not hide the book.

### Cornerstone

Must remain visibly embedded rather than becoming only an invisible blockstate flag.

## 32. Scriptorial Orbs vs Ichorian sigils

Do **not** register these Scriptorium lines as normal Ichorian sigils.

They may share infrastructure:

- virtual target interaction;
- blood-fill rendering;
- surface-following path/tendril code;
- client/server progress sync.

But they are:

- rite-specific;
- generated automatically;
- not discovery content;
- not reusable player sigil knowledge;
- not support/response sigils;
- not awakened castes.

Use a distinct internal target type, e.g.:

```text
SCRIPTORIAL_NODE
```

## 33. HUD

D5:

> **Eightfold Inscription**  
> Write the eight tendencies into the Scriptorium.  
> `5 / 8 channels inscribed`

D7:

> **Monolithic Inscription**  
> Double the eightfold script.  
> `11 / 16 inscriptions complete`

D7 still has only eight world-space orbs; HUD counts two fill stages each.

## 34. Suggested hook contract

Possible narrow adapter:

```text
ScriptoriumRiteHooks
```

Responsibilities:

```text
validateSubject(rite, subjectPos)
validateEnzymeReservoirs(rite, scriptorium)
onConsecrationProgress(rite, progress)
onDaemonSeal(rite)
spawnScriptorialNodes(rite)
acceptScriptorialProjection(rite, node, amount)
syncScriptorialProgress(rite)
renderWritingTendrils(rite)
renderOfferingTransfer(rite)
onCulmination(rite)
```

No-op for every non-Scriptorium rite.

## 35. State data

Active Cardinal Rite may need:

```text
subjectPos
scriptorialNodeFill[8]
scriptorialNodeStage[8]   // D5 max 1, D7 max 2
specialPhaseActive
```

Persist/sync through existing active-rite lifecycle.

## 36. Rendering contract

For each inscription stage, sync enough data to render:

```text
node position
tendency/index
stage 0/1/2
subject Scriptorium position
rite rotation/local frame
```

Prefer deterministic path generation from server-authored endpoints.

Render rules:

- surface-following or gently hovering curve;
- crimson vessel body;
- tendency-colored inner flow;
- stage 2 adds paired/spiraling strand around stage 1;
- completed tendrils persist until culmination.

## 37. Final ritual language

### Boundary anchors

**Define the temporary body of the rite.**

### Humanity Daemon

**Commits the practitioner's will to the transformation.**

### Scriptorial Orbs

**Write tendency into the ritual body.**

### Writing tendrils

**Carry that tendency from the floor into the machine.**

### Physical enzyme tubes

**Receive and visibly pressurize the tendency.**

### Book

**Receives the magical instruction.**

### D5 Vicar hardware

**Allows the instruction to exceed ordinary enchantment limits once.**

### D7 Monolithic frame

**Fixes an even more impossible inscription into a stable-enough form to be used.**
