# Refined Potions and Pilot Recipes — Implementation Steps

> **Depends on:** `02_ALEMBIC_TIERS_AND_RECIPE_ENGINE.md`.  
> **Deliverable:** Exactly three authored first-wave refinements: one D3 and two D5. Their combat behavior is server-owned and survives relog where applicable.

## 1. Common refinement contract

The input must be an exact **ordinary-strength, drinkable** registered potion; long/strong/splash/lingering variants do not match. The catalyst is one registered enzyme. Only one output bottle is produced. The machine decides eligibility by recipe ID and required tier; it does not compute arbitrary enzyme × effect combinations. Base or D3 stations cannot craft a D5-only recipe. An Athanor may still run a D3 recipe.

Output data stores the recipe ID, one primary effect, optional attached behavior, and any rebound rule. Attached behavior is shown in the tooltip but is not miscounted as a second primary effect in the compound validator. `PotionContents` supplies standard effect application. Use a distinct registered potion type or validated component as needed to preserve the recipe ID. Brewing Stand conversions must not duplicate the result or silently strip its behavior.

## 2. The three exact recipes

| Tier | Base → enzyme → result | Cost | Effect |
| --- | --- | --- | --- |
| D3 | Standard Strength → Ferric Enzyme → **Ferric Poise** | 250 mL; 100 ticks | Strength I for 90 s; +20% knockback resistance for those 90 s; no rebound. |
| D5 | Standard Swiftness → Neurotic Enzyme → **Nerve Surge** | 750 mL; 120 ticks | Speed II for 25 s; Slowness I for 8 s when the surge ends or is removed early, except on death. |
| D5 | Standard Fire Resistance → Fervent Enzyme → **Fervent Reprisal** | 750 mL; 160 ticks | Fire Resistance for 90 s; direct melee attackers who deal damage ignite for 2 s, once per wearer per 5 s. |

No other D5 refinement is required for the first shipment. In particular, earlier speculative Sanguine Siphon healing is **not** a recipe: the existing Sanguine Siphon effect drains blood, and a later vivacious variant needs separate balancing against blood-farm rules.

## 3. Ferric Poise

Use one modifier with a stable, namespaced identity for **+0.20 knockback resistance** while the refined effect is active. Strength I is its standard primary effect. Remove the modifier when the effect expires, milk removes it, the player dies, or the server unloads/loads a player without the effect. Redrinking refreshes duration through usual potion rules and cannot add a second copy of the modifier.

Ferric Poise may be one refined ingredient in a D3 two-effect compound. Encode its attachment in the compound's provenance so the knockback bonus lasts 90 seconds even if the other effect lasts longer. Only one refined attachment is allowed per D3 compound.

**Check:** Two consecutive drinks leave one +0.20 modifier, never +0.40; expiring or clearing the effect returns the attribute to its prior value. The D3 machine produces it; base does not.

## 4. Nerve Surge

Apply Speed II for 25 seconds. Each completed drink records a server-side rebound deadline 25 seconds later. Apply Slowness I for 8 seconds at that deadline, or **immediately when Speed is explicitly removed early**, except on death. Ordinary effect refresh or replacement is not an early removal. A later drink does not erase an earlier pending rebound. Persist deadlines over relog and clear them at death. Use a bounded list or merge rebounds at the same tick so malformed data cannot accumulate unlimited pending events.

The first implementation makes Nerve Surge **standalone only**: reject it from D3 compounding and D5 vessel binding. This keeps its delayed side effect from disappearing into a mixed potion before combined-use handlers have been exercised.

**Check:** Milk removes Speed and triggers the rebound immediately; death cancels it; logging out and back in cannot evade it; re-drinking does not overwrite the earlier due time. Exactly one dose is consumed per completed drink.

## 5. Fervent Reprisal

Apply ordinary Fire Resistance for 90 seconds and one attached retaliation rule for the same duration. On a **direct melee damage event** whose damage actually lands on the wearer, if the wearer's last retaliation was at least 5 seconds ago, ignite the attacker for 2 seconds and record the new cooldown. Do not fire for projectiles, Thorns/reflection, self-damage, blocked/cancelled hits, or an attacker who is no longer a valid living entity. Fire-immune attackers may be ignited visually according to standard rules but take no forced bypass damage. Match the mod's pinkish-red/black-tipped Flammeus flame visuals where a custom particle is emitted; do not introduce a different damage source merely for the visual.

The cooldown belongs to the wearer and a new drink does not reset it. Fervent Reprisal is **standalone only** in the first implementation.

**Check:** Ten successful melee hits inside 5 seconds cause one ignition; a hit after 5 seconds can cause another. Arrow, blocked melee, and thorns do not retaliate. When the 90 seconds end, no further retaliation occurs.

## 6. Compatibility and regression

- Existing enzyme Brewing Stand recipes (Vivacious→Regeneration, Fervent→Fire Resistance, Neurotic→Swiftness, and the other recorded alternatives) are unchanged.
- All three use their base potion's normal Brewing Stand acquisition. No new awkward-potion shortcut produces a refined output.
- Ferric Poise may mix with one eligible different primary effect at D3. The other two are excluded from compounding/vessels in the first release by explicit recipe eligibility, not by hidden item checks.
- Apply any authored side behavior only when the output is used, never at Alembic completion. No attacker event may read client-only state.
- Show duration, attached behavior, exact rebound/cooldown, and one-use status in the item tooltip and recipe display.

## 7. Work packages

- [ ] Register/load the three exact recipe definitions with IDs, tier, input potion, enzyme, output, cost, and processing ticks; reject unknown registry IDs and colliding pairs.
- [ ] Implement Ferric Poise modifier lifecycle and focused attribute/compounding checks.
- [ ] Implement Nerve Surge persisted rebound and prove death/relog/milk and repeat-dose behavior.
- [ ] Implement Fervent Reprisal direct-damage filter, cooldown, and Flammeus presentation.
- [ ] Validate three recipe outputs and all negative matches on a dedicated server; record observed costs and durations before proposing more recipes.
