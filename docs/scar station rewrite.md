# Hemomancy — Cerebral Scarring / Mason’s Effigy / Repathing Overhaul Implementation Brief

## 1. High-Level Design Goal

Overhaul the current Cerebral Scarring / Scar Binder system so scars feel like **sympathetic vascular magic**, not physical brain surgery or simple equipment.

The new fiction:

* Scars are not physical items the player equips.
* Scars are **remembered vessel pathways**.
* The player uses a ritual object, the **Mason’s Effigy**, to create paper copies of scar routes.
* Those copied scar patterns are then sneak-used on an empty, lit **Iron Brazier** to teach, recall, or attune those scars.
* At later progression, around Degree 6, the Mason’s Effigy can be converted into the already-existing but currently unobtainable **Fungal Implantation Pylon**, which handles fungal scars.

Core rule:

> The Effigy is the engraved self.
> The paper is the copied instruction.
> The brazier burns the instruction.
> The blood follows the route.

---

## 2. Remove / Replace Scar Binder Concept

The old “Scar Binder” concept no longer fits well.

It made sense when scars were treated like physical items stored in an inventory-like system. Now that scars are remembered pathways, the player should not carry a book or binder full of scars.

Replace the Scar Binder with:

## Mason’s Effigy

A placeable block that represents the player’s vessel-pathways.

It should function as:

* A symbolic self-map.
* A ritual tracing block.
* A source block for creating Scar Pattern items.
* A progression object that visually and mechanically evolves.
* A future upgrade path into the Fungal Implantation Pylon.

Do not treat the Mason’s Effigy as disposable. It is the permanent ritual matrix. The disposable item is the paper copy made from it.

---

## 3. Mason’s Effigy Block

### Block Name

Recommended registry name:

```text
mason_effigy
```

Display name:

```text
Mason’s Effigy
```

Alternate internal/lore names:

```text
Cicatrix Effigy
Vessel Effigy
Anastomotic Effigy
```

Use **Mason’s Effigy** for player-facing name.

### Function

The Mason’s Effigy is a placeable block that allows the player to create Scar Pattern items by placing paper/parchment over it and applying blood/tendency materials.

The action is meant to feel like making a carbon copy, rubbing, or ritual tracing from an engraved anatomical icon.

### Visual States

The block should support progression states if feasible:

```text
UNMARKED
MARKED
INSCRIBED
INTERTWINED
FRUITING
```

Suggested meaning:

| State       | Progression    | Meaning                                         |
| ----------- | -------------- | ----------------------------------------------- |
| UNMARKED    | Newly acquired | No scars known or no routes etched              |
| MARKED      | Early D4       | First scars learned                             |
| INSCRIBED   | Mid D4-D5      | Several known scars                             |
| INTERTWINED | D5             | Compound/advanced routes unlocked               |
| FRUITING    | D6             | Ready to convert into Fungal Implantation Pylon |

If full visual state implementation is too much initially, start with one block and store the state in the block entity for later rendering/model changes.

---

## 4. Scar Pattern Items

Existing Scar Pattern name remains but needs to be made into a single DYNAMIC item that can store 
knowledge of between 1-4 known scars for the below process, the rendering should change so that
instead of flashing a smaller version of a SINGLE scar it should be between 1 and 4 in a 2 x2 grid

They are no longer “the scar itself.”

They are:

```text
Paper copies / rubbings / tracings of a route engraved into the Mason’s Effigy.
```

Recommended item terminology:

* Keep item category/name as **Scar Pattern** for compatibility and clarity.
* Tooltip may call them **Cicatrix Rubbings** or **Red Tracings**.

Example tooltip:

```text
A copied route taken from the Mason’s Effigy.
Burned during Repathing so the vessel may remember its shape.
```

Scar Pattern items are consumed by the Iron Brazier scar interaction.

---

## 5. Creating Scar Patterns From the Mason’s Effigy

### Possible Inputs

Viable recipe:

```text
motif paper
Player blood cost
Known scar selections
```

### Basic Interaction Flow

Player interacts with the Mason’s Effigy using motif paper.

Suggested flow:

1. Player right-clicks Mason’s Effigy with valid motif paper item.
2. Paper appears visually placed/draped on the effigy if block entity rendering supports it.
3. Player adds required catalyst/material (Parchment placed on block, right click with befouling paste).
4. After a short process/channel of blood projection onto the block using our existing bloodprojection on block properties, output becomes a Scar Pattern item.
5. The paper is consumed.
6. Scar Pattern output changes texture and pops off the block onto the ground to be used later.

### Required Data

The Mason’s Effigy needs to know which scars the player has already learned.

Possible implementation:

* On interaction, open GUI similar to bloodbornes rune/covnent selection gui.
* GUI lists known scars from player scar capability.
* Player selects 1 to 4 known scars DEPENDING ON HOW MANY ARE UNLOCKED AT THE TIME.
* Effigy creates a Scar Pattern for those scars if requirements are met, by above process.

Preferred implementation:

```text
Known scars are stored on the player.
Mason’s Effigy UI reads known scars.
Player selects the desired scar pattern to copy.
```

This avoids recipe bloat and keeps the system readable.

---

## 6. Iron Brazier Scar Interaction

The existing Iron Brazier gains a deliberate scar interaction that burns Scar Patterns.

It handles two major ritual types:

1. Learning a new scar.
2. Reattuning active scars.

this block does not already exist, implement it as a new ritual block,using the iron brazier block as a starting point.

Recommended registry name:

```text
iron_brazier
```

---

## 7. Scar Learning Rite

### Rite Name

Recommended:

```text
Rite of First Repathing
```

### Purpose

Consumes a Scar Pattern item to permanently add that scar to the player’s known scars.

### Inputs

```text
Scar item made at the cerebral scar station itself the same as it exists now
Blood cost
Optional ash/catalyst
Player must meet degree requirement
```

### Output

```text
Scar added to known scars capability
Scar Item consumed
Short post-rite effect applied(Same as the shift right click on a sanguine omen block/eye bloodshot effect the vicar causes)

### Important Rule
Learning a scar should consume the Scar Pattern.
The player is not keeping the item or pattern as equipment.
---

## 8. Scar Attunement / Active Scar Changing Rite

### Rite Name

Recommended:

```text
Rite of Repathing
```

### Purpose

Allows the player to change which known scars are currently active.

### Important Design Constraint

Do not make players burn one separate Scar Pattern per active scar every time they want to change loadout. That would become tedious.

Instead:

* Player selects active scars from known scars.
* The system creates or prepares one combined ritual instruction.
* The Iron Brazier performs one rite to commit the whole scar loadout.

### Implementation Options

#### Option A — GUI-Based Attunement

1. Player opens Mason’s Effigy.
2. Player selects desired active scars.
3. UI marks selection as “Prepared Repathing.”
4.Player performs the steps outlined in the above (Creating Scar Patterns From the Mason’s Effigy) to create a pattern item
4. Player performs Rite of Reinscription at an empty, lit Iron Brazier.
5. Active scars are updated.

This is mechanically clean.

### Suggested Cost

Reattuning active scars should be cheap enough that players experiment.

Use:

```text
Blood cost
Small amount of ash / hematic dust
Short channel time
```

Avoid:

```text
Rare enzymes every time
Expensive catalysts every time
Long cooldowns
Destroying important items
```

---

## 9. Player Scar Data

The player needs persistent scar data.

Suggested capability/data structure:

CerebralScar(name, tier, effects etc etc based on the existing scar item fields)
LOOK INTO HOW KNOWNMAIPULATIONS ARE STORED AS A CAPABILITY TO REFACTOR THIS

The existing ScarType Capability needs to be refacorted into a 'HarbingerEquipment' capability
since it no longer handles any scar functionality/storage


A NEW SCAR CAPABILITY NEEDS TO BE MADE WITH ATLEAST
```java
Set<CerebralScar> knownScars;
List<CerebralScar> activeScars;
int maxActiveScars *based on progression;

THE ABILITY TO STORE 1 ITEM ON THE PLAYER FOR THE FUNGAL SCAR* See existing ScarCapability *to be refactored into the HarbingerEquipment Capability
```

Optional later fields:

```java
Map<CerebralScar, Integer> scarExperience;
long lastRepathingTime;
```

### Known Scars

Permanent unlocks.

A scar is added here after the player completes the Rite of First Repathing.

### Active Scars

Currently attuned scars.

These are changed through the Rite of Reinscription.

### Max Active Scars

This system should unlock at Degree 4 and new slots should .
Should be progression-gated.

Example:

```text
D4: 1 active scar
D5: 2 active scars
D6: 4 active scars
D7+: special fungal slot
```

Adjust for balance.

---

## 10. Existing Cerebral Scarring Station

The Cerebral Scarring Station should no longer feel like direct mental surgery.

Possible handling:

The Cerebral Scarring Station becomes the crafting block used to the INITIAL scar items we already have for the player to unlock them at the brazier, they are then stored permanantly and can be selected at the Mason’s Effigy .

---



### NPC Introduction

Introduce a new NPC concept:

```text
Cicatrix Anchorite
```

Common title:

```text
Vein-Mason
```

The Vein-Mason is not part of normal Harbinger outposts. They are an ascetic teacher found in a remote hermitage.

### Structure

Add or plan structure:

```text
Cicatrix Hermitage
```

Possible structure features:

```text
Stone cell
Ash circles
Hanging vessel diagrams
Mason’s Effigy
Iron Brazier
Scar pattern lore
```

### Quest Flow

1. Player reaches Degree 4.
2. Vicar tells player to seek the Vein-Mason.
3. Player finds Cicatrix Hideaway in a cave/mountain.
4. Vein-Mason teaches sympathetic scarring.
5. Player obtains Mason’s Effigy.
6. Player creates first Scar Pattern.
7. Player sneak-uses it on an empty, lit Iron Brazier.
8. Player learns first scar.

---

## 12. Fungal Implantation Pylon Conversion

Around Degree 6, the Mason’s Effigy should convert into the existing but currently unobtainable Fungal Implantation Pylon.

This gives the pylon a clean progression source.

### Concept

At D4, the Mason’s Effigy is symbolic.

At D6, the player learns the symbol was always mycelial.

The Effigy progresses into a FRUITING state, then can be converted.

### Conversion Rite

Suggested name:

```text
Rite of Fruiting Repathing
```

### Inputs

```text
Mason’s Effigy block
Erythromycelium material
Fungal scar catalyst
Blood offering
Degree 6 progression requirement
Optional Crimson Lodge item
```

### Output

```text
Fungal Implantation Pylon
```

### Mechanical Split

Normal scars and fungal scars should be distinct:

| System       | Block                                | Meaning                       |
| ------------ | ------------------------------------ | ----------------------------- |
| Normal scars | Mason’s Effigy + Iron Brazier         | Persuade the vessel           |
| Fungal scars | Fungal Implantation Pylon            | Allow controlled colonization |
| Memories     | Somatic Loom / Mnemonic Reliquary    | Recall inherited blood-memory |

---

## 13. Fungal Scars

Fungal scars should not use exactly the same fiction as normal scars.

Normal scars are:

```text
Copied route -> burned instruction -> vessel remembers
```

Fungal scars are:

```text
Living route -> implanted growth -> vessel is colonized
```
---

## 14. Tooltips / Flavor Text

### Mason’s Effigy

```text
A carved likeness of the vessel, etched with roads the blood has learned.
Place motif paper upon it to copy a route.
The copy burns. The vessel remembers.
```

### Scar Pattern

```text
A copied route taken from the Mason’s Effigy.
Burned during Repathing so the vessel may remember its shape.
```


### Iron Brazier Scar Interaction

```text
A ritual brazier used to burn copied routes into blood-memory.
It does not wound the mind.
It persuades the vessel.
```

### Fungal Implantation Pylon

```text
Once an Effigy. Now a listening thing.
Used to implant fungal scars into a willing vessel.
```

---

## 15. Suggested Vein-Mason Dialogue

```text
You came expecting knives. Good. Leave that foolishness outside.
```

```text
A surgeon cuts what is present. A Mason persuades what is absent to become inevitable.
```

```text
The Effigy is not you. That is why it may safely suffer instruction.
```

```text
The paper burns. The route remains.
```

```text
At first, you copy the scar. Later, the scar copies you.
```

---

## 16. Implementation Priorities

### Phase 1 — Core Data

* Add persistent player scar data:

  * known scars
  * active scars
  * max active scar count
  * scar tiers if not already implemented
* Ensure known scars and active scars save/load correctly.
* Ensure existing scar effects read from active scars only.

### Phase 2 — Mason’s Effigy

* Add Mason’s Effigy block and block entity.
* Add UI for selecting known scars and producing Scar Patterns.
* Add interaction with  motif paper.
* Consume paper and required materials.
* Output Scar Pattern item.

### Phase 3 — Iron Brazier Scar Interaction

* Add the deliberate scar-burning interaction to the existing Iron Brazier.
* Implement Rite of First Repathing:

  * consumes Scar Pattern
  * adds known scar
* Implement Rite of Reinscription:

  * changes active scars
  * consumes Prepared Repathing Folio or equivalent cost

### Phase 4 — Replace Binder Flow

* Remove or deprecate Scar Binder as direct equipment system.
* Migrate its functionality to:

  * Mason’s Effigy for preparation/copying
  * Iron Brazier for committing changes
  * player capability for storage
* If old Scar Binder items exist in worlds, consider converting them to Mason’s Effigy or refunding materials.

### Phase 5 — Degree 4 Progression

* Gate Mason’s Effigy and first scar rite behind Degree 4.
* Add Vein-Mason / Cicatrix Anchorite dialogue hooks.
* Add or stub Cicatrix Hermitage structure.
* Add guidebook entries for scarring.

### Phase 6 — Fungal Pylon

* Make Fungal Implantation Pylon obtainable by converting Mason’s Effigy at Degree 6.
* Add conversion recipe/rite.
* Ensure fungal scars are handled separately from normal scars if possible.
* Add FRUITING state to Mason’s Effigy if feasible.

---

## 17. Compatibility / Migration Notes

If there are existing scar items or Scar Binder items:

* Do not immediately delete registry entries if worlds may already contain them.
* Mark old binder as deprecated.
* Add conversion recipe:

  * old Scar Binder -> Mason’s Effigy
  * or old Scar Binder -> materials + guide text
* Existing Scar Pattern items should remain valid.
* Existing scar effects should be refactored to read active scar IDs from player data.

---

## 18. Final Mechanical Summary

The desired final gameplay loop:

### Learning a Scar

```text
Reach Degree 4
Obtain Scarring Station,
Obtain Mason’s Effigy
Sneak-use Scar Item on an empty, lit Iron Brazier
Scar is added to known scars

```

### Attuning Scars

```text
Use Mason’s Effigy to prepare selected known scars
Perform Rite of Reinscription at an empty, lit Iron Brazier
Active scars update
```

### Fungal Scar Progression

```text
Reach Degree 6
Convert Fruiting Mason’s Effigy into Fungal Implantation Pylon
Use pylon to implant fungal scars
```

The intended feel:

```text
Memories are remembered.
Scars are repathed.
Fungal scars are implanted.
```
