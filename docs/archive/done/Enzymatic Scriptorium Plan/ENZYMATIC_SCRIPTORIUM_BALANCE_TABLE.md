# Enzymatic Scriptorium — Balance Table and Playtest Targets

> **Status:** First playable balance pass  
> **Purpose:** Define provisional blood costs, enzyme fill thresholds, Scriptorial Strain, Hematic Curse accumulation, Overexertion penalties, D5/D7 rite costs, and recommended playtest bands.  
> **Important:** These are baseline tuning values, not final numbers. Preserve the shape of the progression curve even if individual values move during testing.

---

## 1. Core Balance Goals

The Enzymatic Scriptorium should create three distinct risk tiers:

```text
D3 — Enzymatic Scriptorium
Clever alchemical bias.
Better control, modest danger.

D5 — Consecrated Scriptorium
Overexertion I.
Normal maximum +1.
Clearly risky.

D7 — Monolithic Scriptorium
Overexertion II.
Normal maximum +2.
Extreme compound inscriptions become possible,
but are expensive and difficult to sustain.
```

The standard Enchanting Table must remain attractive whenever the player values safety over control.

---

# 2. Core Scriptorium Balance

| Parameter | Recommended Baseline | Playtest Range | Notes |
|---|---:|---:|---|
| Normal enzyme inputs | 1–3 | 1–3 | Hard cap for ordinary D3 enchanting |
| Primary-affinity weight bonus | **+75% per matching enzyme** | +60–90% | Should feel immediately noticeable |
| Secondary-affinity weight bonus | **+35% per matching enzyme** | +25–45% | Useful without competing with primary |
| Maximum normal weight multiplier | **3.5×** | 3.0–4.0× | Prevents ordinary bias becoming deterministic |
| Low-tier blood cost | **100 mL** | 50–150 mL | In addition to vanilla XP/lapis |
| Medium-tier blood cost | **200 mL** | 150–250 mL | |
| High / level-30 blood cost | **300 mL** | 250–400 mL | |
| D3 maximum enchantment level | Vanilla maximum | Fixed | No Overexertion yet |
| Normal curse-risk cap | **45%** | 35–50% | Before Overexertion |
| Overexerted curse-risk cap | **95%** | 90–100% | Extreme items should usually be cursed |

---

# 3. Normal Enzyme Tube Fill Thresholds

For ordinary D3 enchanting, use the three-unit input maximum as the visible fill scale.

| Matching Enzyme Units Loaded | Physical Tube Fill |
|---:|---:|
| 0 | 0% |
| 1 | ~33% |
| 2 | ~66% |
| 3 | 100% |

During station-upgrade rites, use ritual-specific behavior instead:

```text
D5:
1 required enzyme of each tendency
→ corresponding ritual reservoir considered fully charged

D7:
2 required enzymes of each tendency
→ first unit fills normal chamber
→ second unit fills upper / overpressure chamber
```

---

# 4. Scriptorial Strain

Use one conceptual score:

# **Scriptorial Strain**

Recommended conceptual formula:

```text
Scriptorial Strain
=
Bias Pressure
+ Enchantment Power
+ Multi-Enchantment Load
+ Overexertion Load
```

For a simple first implementation:

```text
1 Scriptorial Strain
≈
1 percentage point of Hematic Curse chance
```

Clamp according to mode:

```text
Normal D3 inscription:
~45% maximum

Overexertion:
~95% maximum
```

The important rule is:

> **Stronger bias and stronger resulting enchantments both increase curse risk.**

---

# 5. Bias Pressure

Suggested starting values:

| Enzymes Used | Scriptorial Strain |
|---:|---:|
| 0 | +0 |
| 1 | **+3** |
| 2 | **+7** |
| 3 | **+12** |

This is intentionally nonlinear.

The third enzyme should represent a meaningful commitment rather than merely another flat increment.

---

# 6. Enchantment Result Load

Suggested result contribution:

| Result Feature | Scriptorial Strain |
|---|---:|
| Enchantment below normal maximum | **+2** |
| Enchantment at normal maximum | **+5** |
| Each additional enchantment after the first | **+3** |
| Explicitly rare/high-value enchantment | Optional **+0–3** data override |

Example:

```text
Efficiency V
Fortune III
Unbreaking III
```

All three are at their normal maximum.

Calculation:

```text
3 max-level enchantments:
3 × 5 = 15

2 additional enchantments:
2 × 3 = 6

Result Strain:
21
```

With three enzymes:

```text
Bias Strain:
12

Result Strain:
21

Total:
33

Approximate curse chance:
33%
```

---

# 7. Example D3 Outcomes

| Result | Calculation | Approx. Curse Chance |
|---|---|---:|
| 1 enzyme + Efficiency III | 3 + 2 | **5%** |
| 2 enzymes + Efficiency V | 7 + 5 | **12%** |
| 3 enzymes + Efficiency V | 12 + 5 | **17%** |
| 3 enzymes + Efficiency V + Unbreaking III | 12 + 10 + 3 | **25%** |
| 3 enzymes + Efficiency V + Fortune III + Unbreaking III | 12 + 15 + 6 | **33%** |
| Exceptional normal-cap package | calculated value | Cap around **45%** |

Recommended expected D3 curse incidence over many ordinary uses:

```text
~10–25%
```

Very strong D3 results should frequently land in:

```text
~25–40%
```

---

# 8. Excess Load

Every enchantment level above its normal maximum contributes **Excess Load**.

Formula:

```text
ExcessLoad(item)
=
Σ max(0, actualLevel - normalMaxLevel)
```

Examples:

```text
Efficiency VI
= Excess Load 1

Efficiency VII
= Excess Load 2

Efficiency VI + Fortune IV
= Excess Load 2

Efficiency VII + Fortune IV
= Excess Load 3

Efficiency VII + Fortune V
= Excess Load 4

Efficiency VII + Fortune V + Unbreaking V
= Excess Load 6
```

---

# 9. Overexertion Curse Surcharge

Use a steep nonlinear surcharge.

| Total Excess Load | Added Scriptorial Strain |
|---:|---:|
| 0 | +0 |
| 1 | **+18** |
| 2 | **+40** |
| 3 | **+55** |
| 4 | **+70** |
| 5 | **+82** |
| 6+ | **+90** |

Then:

```text
Curse Chance
=
Bias Strain
+ Result Strain
+ Excess Load Surcharge
```

Clamp at the current Overexertion ceiling:

```text
95%
```

---

# 10. Overexertion Examples

Assume three-enzyme bias unless otherwise stated.

| Item | Excess Load | Approx. Curse Risk |
|---|---:|---:|
| Efficiency VI | 1 | ~**35%** |
| Efficiency VII | 2 | ~**57%** |
| Efficiency VI + Fortune IV | 2 | ~**65%** |
| Efficiency VII + Fortune IV | 3 | ~**80–85%** |
| Efficiency VII + Fortune V | 4 | ~**90%+** |
| Efficiency VII + Fortune V + Unbreaking V | 6 | **95% cap** |

Recommended experiential target:

```text
D5 +1 item:
~30–65% curse risk depending on package

D7 single +2 enchantment:
~50–75%

D7 multi-overexerted "god tool":
~80–95%
```

---

# 11. Guaranteed Overexertion Penalties

Curse luck is not the only balance mechanism.

Even an uncursed Overexerted item carries guaranteed instability from its **Excess Load**.

Recommended baseline:

| Excess Load | Expected Durability Use | Blood Cost on Qualifying Use | User Backlash |
|---:|---:|---:|---|
| **1** | **1.5×** normal | 0 | None |
| **2** | **2.0×** | ~2 mL | None |
| **3** | **2.5×** | ~4 mL | Small vascular-risk check |
| **4** | **3.0×** | ~6 mL | Moderate vascular-risk check |
| **5** | **3.5×** | ~8 mL | Vascular strain; rare direct damage |
| **6+** | **4.0×** | ~10–15 mL | Meaningful vascular/direct-damage risk |

These are first-pass values.

---

# 12. Recommended Overexertion Playtest Ranges

| Variable | Baseline | Test Range |
|---|---:|---:|
| Wear added per Excess Load | +50% | +35–65% |
| EL2 blood/use | 2 mL | 0–4 mL |
| EL4 blood/use | 6 mL | 4–10 mL |
| EL6 blood/use | 12 mL | 8–20 mL |
| EL3–4 vascular backlash chance | ~5% | 2–8% |
| EL5+ backlash chance | ~10% | 5–15% |
| Backlash cooldown | 20 ticks | 20–40 ticks |

Important:

> **Overexertion wear should not be fully canceled by ordinary Unbreaking.**

Otherwise an Overexerted Unbreaking enchantment becomes the automatic answer to the balancing system intended to constrain it.

---

# 13. Qualifying-Use Rules

Overexertion penalties should trigger when the impossible enchantment actually contributes.

| Enchantment Family | Qualifying Event |
|---|---|
| Efficiency | Valid block breaking |
| Fortune | Fortune modifies a loot calculation |
| Sharpness / Smite / Bane | Successful applicable attack |
| Power | Bow shot / supported projectile use |
| Protection | Incoming damage actually mitigated |
| Feather Falling | Fall damage reduction |
| Looting | Kill whose loot calculation uses Looting |
| Unbreaking | Does **not** independently trigger merely by existing |

The item is not dangerous merely because it sits in inventory.

It is dangerous when asked to perform beyond normal enchantment limits.

---

# 14. Fortune-Specific Pressure

Fortune is economically powerful and may deserve additional wear.

Recommended playtest rule:

```text
If over-limit Fortune actually modifies a drop:
apply +1 to +3 additional durability wear
```

Suggested starting value:

```text
Fortune IV:
+1 additional durability when over-limit Fortune contributes

Fortune V:
+2 additional durability when over-limit Fortune contributes
```

Test range:

```text
+1 to +3
```

Goal:

Allow Fortune IV/V to exist without making its extra resource output free.

---

# 15. Hematic Curse Severity by Excess Load

Keep:

```text
Maximum Hematic Curses per item = 1
```

Recommended severity mapping:

| Excess Load | Curse Severity |
|---:|---|
| 0–1 | Tier I |
| 2–3 | Tier II |
| 4–5 | Tier III |
| 6+ | Tier III / maximum authored severity |

This prevents tooltip clutter while still making extreme inscriptions more dangerous.

---

# 16. Example Curse Scaling — Sanguine Appetite

| Curse Tier | Suggested Blood Cost per Valid Tool Use |
|---|---:|
| I | 1–2 mL |
| II | 3–4 mL |
| III | 5–8 mL |

This stacks with any separate Excess Load blood cost.

Example:

```text
Efficiency VII
Excess Load 2
Sanguine Appetite II
```

Could cost:

```text
2 mL from Excess Load
+
3–4 mL from Sanguine Appetite
=
5–6 mL per qualifying use
```

This is intentional.

---

# 17. Example Curse Scaling — Marked by the Will

| Excess Load / Curse Tier | Recommended Pressure |
|---|---|
| Tier I | Mild Rogue Will attention increase |
| Tier II | Meaningful sustained increase |
| Tier III | Strong attention / combat broadcast |
| Extreme Load | Very strong pressure while actively used |

Do not directly spawn a Rogue Will on every fixed number of kills.

Feed the curse into the existing Rogue Will attention / encounter-pressure logic.

---

# 18. D5 Rite Balance

## Rite of the Eightfold Script

| Requirement | Recommended Value | Playtest Range |
|---|---:|---:|
| Degree | **5** | Fixed |
| Rite form | **Greater** | Fixed initially |
| Required station | Base Enzymatic Scriptorium | Fixed |
| Enzyme preload | **1 of each ×8** | Fixed |
| Vicar's Consecration Kit | **1** | Fixed |
| Lapis Block | **1** | 1–2 |
| Blood Crystal Shard | **1** | Fixed |
| Boundary anchors | **20** | Existing D5 Cardinal standard |
| Boundary blood | **1,000 mL** | Existing D5 Cardinal standard |
| Scriptorial Orbs | **8** | Fixed |
| Blood per orb | **50 mL** | 25–75 mL |
| Scriptorial blood | **400 mL** | 200–600 mL |
| Mandatory total | **1,400 mL** | 1,200–1,600 mL |
| Result | Consecrated Scriptorium | Fixed |
| Overexertion | **+1 max level** | Fixed |

Recommended starting point:

```text
1,400 mL total mandatory blood
```

D5 should feel significant but practical enough that players actually use the upgrade.

---

# 19. D7 Rite Balance

## Rite of the Monolithic Script

| Requirement | Recommended Value | Playtest Range |
|---|---:|---:|
| Degree | **7** | Fixed |
| Rite form | **Grand** | Fixed |
| Required station | Consecrated Scriptorium | Fixed |
| Enzyme preload | **2 of each ×8 = 16** | Fixed |
| Monolithic Cornerstone | **1** | Fixed |
| Monolith Fragments | **4** | 4–8 if supply proves trivial |
| Sanguine Quintessence | **1** | Fixed |
| Lapis Blocks | **2** | 1–3 |
| Boundary anchors | **28** | Existing D7 Cardinal standard |
| Boundary blood | **1,400 mL** | Existing D7 Cardinal standard |
| Scriptorial Orbs | **8**, two stages each | Fixed |
| Blood per stage | **50 mL** | 25–75 mL |
| Scriptorial blood | **800 mL** | 400–1,200 mL |
| Mandatory total | **2,200 mL** | 1,800–2,600 mL |
| Result | Monolithic Scriptorium | Fixed |
| Overexertion | **+2 max levels** | Fixed |

Recommended starting point:

```text
2,200 mL total mandatory blood
```

---

# 20. Recommended First Playtest Targets

| Question | Healthy Target |
|---|---|
| Does one enzyme feel noticeable? | Yes, but not dependable |
| Do three enzymes feel meaningfully stronger? | Yes |
| Can three enzymes guarantee one enchantment? | **No** |
| Typical D3 curse incidence | ~**10–25%** |
| Very strong D3 result curse incidence | ~**25–40%** |
| Typical D5 `+1` item curse incidence | ~**30–65%** |
| D7 single `+2` enchantment curse incidence | ~**50–75%** |
| D7 multi-overexerted "god tool" curse incidence | ~**80–95%** |
| Is Excess Load 1 noticeable? | Yes, mostly through durability |
| Is Excess Load 2 usable long-term? | Yes, with upkeep |
| Is Excess Load 4 uncomfortable? | Definitely |
| Is Excess Load 6 technically usable? | Yes, but deliberately unreasonable |
| Does vanilla enchanting remain attractive? | Yes, whenever safety matters |
| Does red glint immediately identify provenance? | Yes |

---

# 21. Balance Curve to Protect

Individual numbers may move substantially during testing.

The curve itself should remain:

```text
Ordinary enchanting
= safest, least controlled

D3 Scriptorium
= clever alchemy and manageable risk

D5 Overexertion
= powerful and clearly costly

D7 Overexertion
= permits equipment that should not normally exist,
  but makes maintenance and biological consequences
  part of the item's identity
```

The intended feeling is:

> **Ordinary bias should feel like clever alchemy. D5 Overexertion should feel risky. D7 compound Overexertion should feel like deliberately operating equipment that should not exist.**
