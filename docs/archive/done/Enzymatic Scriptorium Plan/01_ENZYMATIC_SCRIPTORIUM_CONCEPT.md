# Enzymatic Scriptorium — Core Concept

> **Role:** Feature identity and progression contract.  
> **Depends on:** Existing eight tendencies/enzymes, vanilla enchanting, Harbinger blood resource, Cardinal Rite progression.

## 1. Core thesis

The **Enzymatic Scriptorium** is a Harbinger modification of the ordinary enchanting table.

It should still read immediately as an enchanting apparatus. Hemomancy has not invented a completely separate enchantment system; it has learned to interfere with an existing one.

> **Ordinary enchanting accepts uncertainty. The Scriptorium chemically biases that uncertainty through blood tendency.**

The Scriptorium offers better control and, eventually, power beyond ordinary enchantment limits. The cost is that the process becomes increasingly unsafe and increasingly dependent on the Harbinger's blood.

## 2. Relationship to vanilla enchanting

### Standard Enchanting Table

- Safe.
- No Hematic Curse risk.
- Normal enchantability and weighting.
- Normal enchantment maximums.
- Purple vanilla enchantment glint.
- No enzyme or blood cost.

### Enzymatic Scriptorium

- Enzyme-biased legal enchantment weighting.
- May receive modestly improved enchanting quality/control.
- Costs enzymes and blood in addition to ordinary enchanting resources.
- Can produce Hematic Curses.
- Produces a permanent **red Hemomancy enchantment glint**.
- D3 version remains inside normal enchantment maximums.

### Consecrated / Monolithic Scriptorium

- Keeps all Scriptorium behavior.
- Adds deliberate **Overexertion**.
- D5 can exceed a supported enchantment's normal maximum by `+1`.
- D7 can exceed it by `+2`.
- Higher power produces sharply greater curse and instability pressure.

## 3. Progression

| Degree | Station state | Capability |
|---:|---|---|
| **D3** | **Enzymatic Scriptorium** | Enzyme-biased enchanting, Hematic Curse risk, red glint, normal enchantment caps |
| **D5** | **Consecrated Enzymatic Scriptorium** | Vicar's Consecration Kit rite; unlocks Overexertion I (`normal max +1`) |
| **D7** | **Monolithic Enzymatic Scriptorium** | Monolithic Cornerstone rite; unlocks Overexertion II (`normal max +2`) |

There is currently **no D8 upgrade or Apotheos-specific behavior**.

This deliberately parallels the Hematic Armature's institutional progression:

```text
practical Harbinger apparatus
        ↓
D5 Vicar/Lodge consecration
        ↓
D7 Monolithic final form
```

## 4. Visual identity

The base block should look like a **semi-standard enchanting table altered by biological alchemy**.

Keep the recognizable visual grammar:

- compact altar/table body;
- central enchanting surface;
- floating/open book or equivalent inscription focus;
- scholarly/magical silhouette.

Add Hemomancy through:

- Hematic Iron fittings;
- blood channels;
- glass apparatus;
- crimson vascular details;
- the eight enzyme reservoirs.

Do **not** turn the entire block into an undifferentiated red organic mass.

## 5. The eight enzyme tubes

The signature feature is **eight permanent glass tubes/vials**, one for each tendency.

| Tendency | Enzyme |
|---|---|
| Animus | Vivacious |
| Flammeus | Fervent |
| Ductilis | Neurotic |
| Lux | Incandescent |
| Mortem | Ruinous |
| Congeatio | Frigid |
| Ferric | Ferric |
| Tenebris | Umbral |

### Normal use

Normal D3 enchanting should accept a limited active enzyme profile, recommended **1–3 enzyme inputs**.

The inserted enzyme is routed to its corresponding physical tube:

```text
2 Ferric + 1 Neurotic inserted
        ↓
Ferric tube shows stronger/higher fill
Neurotic tube shows one unit
other six remain empty
```

### Upgrade rites

D5/D7 station-upgrade rites temporarily use all eight reservoirs:

- D5 requires one of each enzyme.
- D7 requires two of each enzyme.

The player does not click the tiny tubes directly during the ritual. The physical tubes are **receivers and visual indicators**, while ritual interaction occurs on large floor-space virtual targets.

## 6. Suggested model arrangement

Recommended Minecraft-readable layout:

- four tubes near table corners;
- four tubes centered between them / side-middle positions;
- short visible glass necks;
- Hematic Iron collars;
- modeled or textured vessel channels leading toward the central book.

Avoid eight identical potion bottles simply attached to the perimeter.

The physical tube layout should support the eight compass directions used during the later Cardinal Rite upgrades.

## 7. Animation language

### Idle

- Subtle enchanting-book movement.
- Small fluid motion or bubbles in occupied tubes.
- Weak blood-channel pulse.
- No excessive particle cloud.

### Enzyme inserted

- Matching reservoir fills.
- Tendency-colored pulse travels inward.
- Central inscription focus briefly answers with that tendency's coloration.
- Blood/crimson remains the dominant visual language.

### Enchantment committed

- Active tubes brighten.
- Blood circulation joins the tendency colors.
- Book/item lifts or tightens into the inscription focus.
- Result resolves with red Scriptorium glint.

## 8. Red enchantment glint

Every item successfully enchanted through the Scriptorium receives the mod's existing **red enchantment glint**.

```text
Purple glint
= conventional enchantment provenance

Red glint
= Scriptorium / Hematic inscription provenance
```

The red glint does **not** mean the item is cursed.

Recommended persistent item data:

```text
hemomancy:scriptorium_inscribed = true
```

or:

```text
hemomancy:inscription_origin = enzymatic_scriptorium
```

It should survive relog, storage, repair, rename, and normal anvil work.

Removing a Hematic Curse later should not necessarily remove the red provenance.

## 9. Rejected direction: Hematic Grafts

Earlier brainstorming considered deterministic Hematic Grafts that would add new item powers.

That direction is deliberately abandoned because it creates ownership ambiguity with Manipulations, armor, Morphlings, and Scars.

The replacement is:

```text
ORDINARY ENCHANTMENTS
= what the item gains

HEMATIC CURSES
= what forcing the process costs
```

## 10. System ownership

### Manipulations

Own deliberate casts, targeted burst effects, placed constructs, and instant utility.

### Armor

Own major defensive identity, immunities, and strong contact/reaction behavior.

### Morphlings

Own ecology, metabolism, husbandry, and organism-derived contextual behavior.

### Scars

Own tuning and sympathetic modification of the player's existing systems.

### Scriptorium

Owns:

- legal enchantment probability manipulation;
- Hematic inscription provenance;
- enchantment-related curses;
- enchantment limit Overexertion.
