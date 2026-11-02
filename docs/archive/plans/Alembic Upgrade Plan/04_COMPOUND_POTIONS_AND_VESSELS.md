# Compound Potions and Bound Vessels — Implementation Steps

> **Depends on:** `02_ALEMBIC_TIERS_AND_RECIPE_ENGINE.md`; Ferric Poise attachment from `03_REFINED_POTIONS_AND_PILOT_RECIPES.md`.  
> **Deliverable:** Two-effect drinkable compounds at D3 and a durable three-effect vessel with exact refills and blood substitution at D5.

## 1. First-release source filter

Accept a drinkable `minecraft` or `hemomancy` registered potion whose encoded payload has **exactly one primary, positive-duration effect** with a valid registry ID and supported use-time behavior. Reject unknown namespaces by default until compatibility rules are authored. Reject water, Awkward, Thick, Mundane, instant-effect, empty-effect, splash, lingering, arrow, already-compounded, and malformed potions. An approved attached behavior is stored separately from the primary effect; at first only Ferric Poise may carry one through a D3 compound. D5 pilot Nerve Surge and Fervent Reprisal are standalone only.

Use the same server-side validator for preview, recipe match, and final commit. Do not allow a hopper to place a disallowed item that the GUI rejects. Mixed positive/negative effects are allowed when each source passes the validator; both consequences are visible before commit.

## 2. D3 two-effect bottle

| Slot | Item |
| --- | --- |
| Input 0 | Finished **base** potion |
| Catalyst 3 | Different finished potion |
| Flask 1 | Existing flask function only; not a second potion slot |
| Result 2 | One drinkable compound |

Requires Condenser or Athanor, heat, **500 mL** Alembic blood, and **160 ticks**. Consume both complete bottles and return exactly one filled bottle; the second glass bottle is part of the cost. Preserve each effect's exact duration and amplifier independently. The base determines name, primary tint, and display order. A pair with the same primary effect ID is invalid even if amplifier or duration differs; no re-compounding or duration addition.

When used, apply both effects at the same use event. Explicitly invoke existing drink-start/finish rules for effects such as Mnemonic Whispers even when Whispers came from the catalyst. If Whispers is already active and the compound is drunk, the existing Whispers→Screams backlash occurs and the second effect is handled according to the established finish-drink semantics, with no duplication. Ensure a Ferric Poise attachment ends on its own 90-second timer rather than on the other effect's timer.

**Tests:** swap base/catalyst to confirm naming/tint change without effect-loss; unequal durations expire separately; duplicate effect IDs are refused with no consumption; compound + compound and compound + vessel are refused; redrinking Whispers from either slot triggers the existing protection.

## 3. D5 vessel binding

| Slot | Item |
| --- | --- |
| Input 0 | One finished base potion |
| Catalyst 3 | One different finished potion A |
| Catalyst 2 (D5-only) | One different finished potion B |
| Flask 1 | Unused by binding; retains normal blood-container handling |
| Result 2 | The transformed base potion, bound at **1/3 doses** |

Requires Athanor, heat, **1,000 mL** reservoir blood, and **200 ticks**. Consume three potions; a copy of the base potion becomes the durable bound output with one dose. No separate empty vessel or binding ingredient exists. The base effect is permanent to this potion and is paid only at initial binding. Its name and tint remain when empty. Require three distinct primary effect IDs; first-release binding does not accept D3 compound bottles, either D5 standalone refined output, or a bound potion as an ingredient.

Persist a **canonical formula signature**: registered effect IDs, amplifier, duration, and approved behavior/provenance; the two catalyst signatures are compared unordered, while the base stays ordered and named. Avoid translated names or raw tooltip strings. A bound potion has a maximum of three doses and is nonstacking.

## 4. D5 refill

| Slot | Item |
| --- | --- |
| Input 0 | Bound vessel, 0/3 to 2/3 doses |
| Catalyst 3 + Catalyst 2 | Two finished potions exactly matching its two bound catalyst signatures, either order |
| Flask 1 | No refill role; ordinary blood-container handling remains available |
| Result 2 | Same vessel with **one additional dose** |

Requires heat, **750 mL** reservoir blood, and **160 ticks**. Consume the two fresh catalyst potions. Do not consume another base potion. No refill at 3/3; no mismatched effect, amplifier, or duration substitution; no silent rewriting of the vessel's name, base, or existing doses. All matches are checked again at commit. At 0/3 the formula remains and the two matching catalysts refill it.

If a player wants a different base or catalyst pair, they bind a **new base potion**. Do not invent a cleansing or retuning recipe in this phase. Refill output inherits the exact bound potion identity and applicable components except the incremented dose count.

## 5. Use and blood substitution

Right-click drinking resolves once on completion of the item's use time, server-side:

```text
if doses == 0: reject; no effects, no blood roll
if player can spend 1,000 mL by the existing blood-cost API:
    roll one uniform chance (20%)
    on success: spend 1,000 mL; apply all three effects; preserve dose
on no eligible blood or failed roll:
    consume one dose; apply all three effects
```

Roll only once for an actual completed use. Cancelling a drink, changing hands, taking damage during use, or previewing a tooltip never rolls. If a successful roll cannot actually deduct blood at commit because player state changed, fall back to spending one dose without applying the effects twice. A non-Harbinger or anyone lacking spendable blood simply uses a dose. Blood comes from **the user**, never a remote Alembic tank, another player, or the vessel's crafting blood.

Each effect retains its bound amplifier and duration. Preserve special drink hooks such as Mnemonic Whispers and avoid bypassing their backlash by using a custom item. On 0/3, keep the empty bound vessel in hand for refilling. Tooltip: bound base/name, three effects and timers, doses `n/3`, `20% chance to spend 1,000 mL blood instead of a dose`, and a clear inactive state at zero.

Expected activations with enough blood are about **1.25 per prepared dose** at 20%, before the blood cost is considered. This is a balancing consequence, not a separate guaranteed charge; log real usage in playtests.

## 6. Transaction and corruption checks

- [ ] Reuse document 02's one-output commit path; save/reload between start and completion cannot duplicate a bottle or a dose.
- [ ] Reject a vessel with invalid count (negative or >3), missing effect registry entry, duplicate primary effect, unknown attachment, or formula version without consuming inputs.
- [ ] Verify normal glass/flask filling still works when a cured clay flask is inserted. Binding selects only with all three valid potion inputs in slots 0, 3, and 6; slot 1 has no binding role.
- [ ] Verify hopper/top/side input and bottom output cannot bypass tier, signature, or full-output checks.
- [ ] Test 0/3, 1/3, 2/3, 3/3 through save/reload, death, drops, transfer to another player, and inventory move; no dose can be negative or exceed three.
- [ ] Test a player at 999 mL and 1,000 mL spendable blood, failed/successful rolls with an injected deterministic RNG, zero doses, early use cancellation, and changed blood state between preview and finish.
- [ ] Compare ordinary two-effect compounds and vessel use against Mnemonic Whispers drink hooks so both preserve the existing Screams backlash.

**Gate to document 05:** All four recipe operations produce one output and exact resource payment on a server; malformed and mixed inputs consume nothing; old Alembic and Retort tests remain green.
