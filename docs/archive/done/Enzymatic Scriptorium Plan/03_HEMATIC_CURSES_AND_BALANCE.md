# Enzymatic Scriptorium — Hematic Curses and Balance

> **Scope:** The price of forcing enchanting probability.  
> **Core decision:** Stronger bias and stronger resulting enchantment packages both increase curse risk.

## 1. Why curses replace grafts

The Scriptorium should not create a second family of active item powers.

The benefit is already supplied by the Scriptorium:

- better control;
- better odds of desired enchantment families;
- potentially richer enchantment packages;
- later, enchantment levels beyond normal caps.

The curse is the price.

A curse does **not** need to secretly grant an additional upside.

## 2. Standard enchanting remains safer

| Standard Enchanting Table | Enzymatic Scriptorium |
|---|---|
| Lower control | Higher control |
| Normal weighting | Enzyme-biased weighting |
| Normal quality | Potentially richer results |
| No Hematic Curses | Hematic Curse risk |
| No blood/enzyme cost | Blood/enzyme cost |
| Purple glint | Red glint |

## 3. Scriptorial Strain

Use one conceptual score for curse risk:

# **Scriptorial Strain**

It should rise from:

```text
Bias Pressure
+ Enchantment Power
+ Multi-Enchantment Load
+ Overexertion Load (when applicable)
```

The settled behavior is:

> **The more aggressively the process is biased and the more powerful the final result is, the more likely a Hematic Curse becomes.**

Two identical enzyme setups can therefore produce different curse risk if one result is much stronger.

## 4. Proposed normal-mode risk bands

These are starting balance targets, not final constants.

| Outcome | Suggested risk band |
|---|---:|
| Mild bias + weak result | ~3–6% |
| Moderate bias + ordinary strong result | ~8–15% |
| Strong bias + good result | ~15–25% |
| Very strong bias + several high-level enchants | ~25–35% |
| Exceptional heavily forced normal-cap result | ~35–45% |

Normal D3 Scriptorium play should usually remain below the extreme risk of Overexertion.

## 5. Bias-pressure contribution

Example starting model:

```text
1 enzyme → small strain contribution
2 enzymes → moderate contribution
3 enzymes → strong contribution
```

An earlier tuning sketch used roughly:

```text
1 enzyme: +2 percentage points
2 enzymes: +6
3 enzymes: +12
```

Treat these as provisional.

The important property is nonlinear escalation.

## 6. Enchantment-power contribution

The result itself should contribute strain based on:

- total enchantment levels;
- closeness to natural maximums;
- number of enchantments rolled;
- rarity/weight if useful;
- Overexertion above maximum.

Unexpectedly excellent results are inherently more dangerous.

## 7. Curse count

Recommended V1:

```text
Maximum Hematic Curses per item: 1
```

High strain should increase severity, not stack many unrelated curses.

## 8. Curse applicability

Hematic Curses should make the item more dependent on being used by a Harbinger/Hemomancer.

Good cost vocabularies:

- consume blood;
- increase blood drain;
- increase Rogue Will attention;
- create vascular strain;
- increase item wear;
- modestly interfere with Manipulation economy;
- create detectable Hematic signatures.

Avoid generic fantasy penalties unrelated to Hemomancy.

## 9. Candidate curse roster

These are candidates, not a finalized registry.

### Sanguine Appetite

**Tools:** pickaxe, axe, shovel, hoe, similar tools.

```text
Valid tool use consumes a small amount of blood.
```

Suggested low-severity range:

```text
1–2 mL per qualifying action.
```

If the wielder lacks blood, prefer additional durability wear over completely disabling the tool.

### Marked by the Will

**Weapons:** sword, bow, crossbow, similar combat items.

```text
Holding/using the weapon increases Rogue Will attention/ambush pressure.
```

Feed this into existing Will-pressure logic rather than spawning Wills on a hardcoded kill counter.

Possible behavior:

- mild while held;
- stronger temporary increase after combat/kills.

### Open Vessel

**Armor**

```text
Taking damage while wearing the cursed piece drains additional blood.
```

### Mnemonic Friction

**Broad equipment category**

```text
While actively equipped/held, Manipulation blood cost is modestly increased.
```

Keep the modifier small.

### Thirsting Edge

**Melee weapons**

```text
Successful attacks consume blood.
```

No lifesteal. The curse is a cost.

### Hemorrhagic String

**Bow / crossbow**

```text
Firing consumes blood.
```

### Vascular Resonance

**Rare/high-strain equipment**

```text
Repeated qualifying use may add minor strain to a relevant vascular section.
```

Suggested mapping:

- tool/weapon → arm;
- boots → legs;
- helmet → head;
- chest armor → body.

Use sparingly and rate-limit it.

## 10. Tendency-biased curse selection

The enzyme mixture can bias the kind of defect that appears.

Possible practical themes:

| Dominant tendency | Common curse character |
|---|---|
| Animus | hunger / blood consumption |
| Flammeus | escalating use-cost / fever |
| Ductilis | nervous strain / Will attention |
| Lux | exposure / detection |
| Mortem | blood debt / kill-linked pressure |
| Congeatio | slowing/recovery pressure |
| Ferric | durability / structural load |
| Tenebris | observation / Rogue Will attention |

These are balance classifications, not immutable lore oppositions.

## 11. Red glint is not curse signaling

All Scriptorium items use the **red glint**, cursed or not.

```text
Red glint
= Scriptorium provenance

Tooltip curse line / curse icon
= curse state
```

## 12. Curse removal

Curse removal must not trivialize the risk.

Strong candidate:

### Denaturation

```text
Cursed Scriptorium item
→ Denaturation
→ physical item survives
→ ordinary enchantments + Hematic Curse removed
→ provenance may remain
```

This lets the player save valuable base gear without keeping the powerful result while deleting only its price.

An Unstained exception may be designed later.

## 13. Grindstone

Recommended:

- ordinary removable enchantments behave normally;
- Hematic Curse is not trivially removed by vanilla grindstone;
- Scriptorium provenance follows the final cleansing rule.

## 14. Repeated Scriptorium use

Recommended:

- existing Hematic Curse is preserved;
- no second curse stacks;
- severity may later rise if deliberately designed;
- do not allow infinite rerolling for a preferred curse without a deliberate mechanic.

## 15. Balance guardrails

- Do not make every curse a buff with a downside.
- Do not make curse risk independent of result quality.
- Do not make the standard enchanting table obsolete.
- Do not make curse cleansing cheap.
- Do not make curses grant new active verbs.
- Do not rely on random item destruction as the primary balance lever.

Prefer:

- blood cost;
- durability pressure;
- vascular strain;
- player damage;
- Rogue Will attention;
- stripping the inscription.

## 16. Diagnostics

Survival UI can show broad bands:

```text
Scriptorial Strain: LOW
Scriptorial Strain: MODERATE
Scriptorial Strain: HIGH
Scriptorial Strain: SEVERE
```

Developer/debug output may expose:

```text
bias contribution
enchantment-package contribution
multi-enchantment contribution
Overexertion contribution
final curse probability
selected curse weighting
```
