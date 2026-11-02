# Enzymatic Scriptorium — Overexertion

> **Scope:** D5/D7 enchantment-limit breaking.  
> **Progression:** D5 `+1`, D7 `+2`.  
> **No D8 extension is currently planned.**

## 1. Core definition

**Bias** changes which legal enchantment is likely to appear.

**Overexertion** changes how far a supported enchantment may exceed its normal maximum.

```text
D3
normal maximum only

D5 Consecrated Scriptorium
normal maximum +1

D7 Monolithic Scriptorium
normal maximum +2
```

Examples:

| Enchantment | Normal | D5 | D7 |
|---|---:|---:|---:|
| Efficiency | V | VI | VII |
| Fortune | III | IV | V |
| Unbreaking | III | IV | V |
| Sharpness | V | VI | VII |
| Power | V | VI | VII |
| Looting | III | IV | V |

## 2. Explicit allowlist/data support

Do **not** blindly exceed every registered enchantment's max level.

Some enchantments:

- do not meaningfully scale;
- may be boolean-like;
- may break if a mod never authored behavior above its cap.

Examples that should not automatically gain higher levels:

- Silk Touch;
- Infinity;
- Mending;
- Aqua Affinity;
- arbitrary third-party enchantments.

Recommended data/tag:

```text
#hemomancy:overexertable_enchantments
```

or per-enchantment data:

```json
{
  "enchantment": "minecraft:efficiency",
  "overexertable": true,
  "max_overexertion": 2
}
```

## 3. Excess Load

Every enchantment level above its ordinary maximum contributes **Excess Load**.

Formula:

```text
ExcessLoad(item) =
Σ max(0, actualLevel - normalMaxLevel)
```

Examples:

```text
Efficiency VI
= Load 1

Efficiency VII
= Load 2

Efficiency VI + Fortune IV
= Load 2

Efficiency VII + Fortune V
= Load 4

Efficiency VII + Fortune V + Unbreaking V
= Load 6
```

## 4. Two distinct costs

Overexertion has two different consequences.

### 4.1 Hematic Curse pressure

Higher Excess Load sharply increases **Scriptorial Strain** and therefore curse risk.

### 4.2 Guaranteed Overexertion instability

Even a lucky item that avoids a Hematic Curse still carries the physical/bodily burden of being above normal enchantment limits.

This prevents rerolling until the player obtains impossible enchantments with no meaningful downside.

## 5. Use-triggered instability

Instability should trigger primarily when the impossible enchantment is actually doing work.

Examples:

### Efficiency VI/VII

Extra pressure while mining.

### Fortune IV/V

Extra pressure when Fortune participates in a loot calculation.

### Sharpness VI/VII

Pressure on successful melee hits.

### Power VI/VII

Pressure when firing/using the over-limit bow effect.

### Protection above normal

Pressure when damage is actually mitigated.

### Feather Falling above normal

Pressure when fall reduction matters.

The item is not simply radioactive in inventory. It is **overworked when asked to perform the impossible effect**.

## 6. Tool instability

Durability should be the primary guaranteed cost for overexerted tools.

Starting conceptual bands:

| Excess Load | Mechanical pressure |
|---:|---|
| 1 | noticeably increased durability use |
| 2 | roughly doubled or greater effective wear |
| 3–4 | severe wear + blood/vascular recoil begins |
| 5+ | intentionally extreme; heavy wear + meaningful wielder backlash |

Exact durability math is still balance work.

Important:

> Extra Overexertion wear should not be fully erased by ordinary Unbreaking.

Otherwise `Unbreaking V` immediately cancels the system intended to constrain it.

## 7. Fortune-specific pressure

Economic enchantments deserve use-based cost as well.

Candidate rule:

```text
When over-limit Fortune successfully alters a drop:
apply additional durability wear
and/or other Excess Load pressure.
```

This permits Fortune IV/V without making the additional output free.

## 8. Weapon instability

Candidate progression:

### Low load

- extra durability.

### Moderate load

- extra durability;
- small blood consumption during qualifying attacks/shots.

### High load

- durability;
- blood consumption;
- occasional rate-limited vascular strain or direct recoil damage.

Do not make every swing randomly explode.

## 9. Armor instability

Do not drain blood constantly just because armor is worn.

Prefer event-linked pressure:

```text
over-limit Protection participates
→ additional durability / blood / vascular cost
```

The cost occurs when the impossible enchantment is exercised.

## 10. Player harm

At sufficiently high Excess Load, direct user risk is allowed.

Candidate effects:

- small blood drain;
- relevant vascular strain;
- occasional small direct damage;
- stronger Hematic Curse severity;
- increased Rogue Will attention if the curse uses that family.

Direct player damage should be:

- rate-limited;
- tied to meaningful item use;
- proportionate to extreme Load.

## 11. Curse-risk escalation

Exact constants are not yet settled.

The design requirement is:

```text
normal strong Scriptorium result
< D5 +1 Overexertion
< D7 +2 Overexertion
< multiple overexerted enchantments
```

By the time the player creates something like:

```text
Efficiency VII
Fortune V
Unbreaking V
```

a Hematic Curse should be expected rather than surprising.

## 12. Curse severity and Excess Load

Recommended future linkage:

- one Hematic Curse remains the readable maximum;
- Excess Load can increase its severity.

Example:

### Sanguine Appetite

```text
Load 1 → light blood use
Load 2 → moderate
Load 3–4 → heavy
Load 5+ → severe
```

### Marked by the Will

```text
Load 1 → mild attention
Load 2 → meaningful attention
Load 3–4 → strong
Load 5+ → extreme broadcast while actively used
```

## 13. Station progression

### D5 — Overexertion I

Unlocked only after the **Rite of the Eightfold Script** upgrades the station using the Vicar's Consecration Kit.

Capability:

```text
supported enchantment max +1
```

### D7 — Overexertion II

Unlocked only after the **Rite of the Monolithic Script** upgrades the Consecrated station using the Monolithic Cornerstone.

Capability:

```text
supported enchantment max +2
```

### D8

No change.

Apotheos and Silent Archon currently use the same final Monolithic Scriptorium rules.

## 14. Visual language

All Scriptorium items retain the red enchantment glint.

Optional Overexertion polish:

- slightly more forceful/faster red glint motion;
- intermittent crimson pulse;
- subtle black-red fracture detail.

This is secondary polish. The red glint remains the provenance language.

On the station:

### D5 Overexertion

- active tube fluid rises above the normal safe graduation;
- pressure valves engage;
- chains on the book tighten.

### D7 Overexertion

- upper reservoir chambers fill;
- Monolithic restraint frame illuminates;
- book motion visibly strains against the fixed frame;
- high Excess Load creates controlled red-black arcs/creaking rather than uncontrolled particle spam.

## 15. No arbitrary `+3` progression

Do not turn the station into:

```text
D5 +1
D7 +2
D8 +3
future +4
```

The current progression ends at `+2`.

Additional endgame depth, if ever needed, should come from other costs/interactions rather than endless numeral inflation.
