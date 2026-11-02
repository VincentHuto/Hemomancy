# Hemomancy: Advanced Potion Brewing

**Status:** Design draft for review  
**Target progression:** Degree 3 Hematic Condenser, then Degree 5 Sanguine Athanor  
**Core purpose:** Let players refine a useful potion, compound two finished potions into one inventory slot at D3, then bind three effects into a refillable vessel at D5. Blood stabilizes compounds and sometimes spares a prepared dose; enzymes give the Centrifuge's products another deliberate use.

## Existing systems to preserve

- The Brewing Stand continues to make vanilla and existing Hemomancy potions. Existing enzyme alternatives in the Brewing Stand remain intact.
- The Ghastly Alembic already has an input, flask, result, and optional catalyst slot, a blood reservoir, and a fire requirement. Its current distillation and blood-filling work remain available before and after upgrading.
- The Vial Centrifuge turns sampled blood into the eight tendency enzymes. This system consumes enzyme items; it does not produce enzymes or restore the samples.
- Mnemonic Whispers retains its existing redrinking backlash. Compound brewing must not bypass it.

## Degree 3 unlock: Hematic Condenser Kit

At Degree 3, the Alchemist offers an **optional** practical lesson after the player has obtained a Ghastly Alembic and performed one successful ordinary distillation. This does not replace the existing D3 Mnemonist assignment. For a lesson that demonstrates both inputs, the player shows the Alchemist a finished potion and two different enzyme types; showing items does not consume them. The Alchemist gives one Hematic Condenser Kit and records the reward claim on the player.

The kit is the declared upgrade offering in a **Degree 3 station-upgrade Cardinal Rite**, following the same subject-and-culmination pattern as the Enzymatic Scriptorium. Seat an unmodified Ghastly Alembic as the rite subject, commit the kit, complete the ordinary rite phases, then project blood into the Alembic's condenser coil during a short profession-specific culmination. The ordinary rite handles floor, focus, anchors, boundary, offerings, and interruption. Only a successful culmination transforms the subject into a Hematic Condenser and consumes the committed kit. The server validates that the subject is a plain Alembic when the rite starts and again before transformation. An upgraded Alembic cannot be selected to repeat the D3 rite.

The machine stores its tier. Breaking a Condenser returns the normal Alembic block item and its applied Condenser Kit; a D5 machine returns both applied kits. Reinstalling recovered kits on another machine requires the matching rites again, but no second NPC reward. Degree gates apply to performing each upgrade rite, not to operating an already upgraded shared machine. These two utility rites have the safe interruption and escrow rules below; neither can consume a one-time kit without granting its upgrade.

The D3 model gains a visible condenser coil, two small return tubes, and a raised catching vessel, while retaining the base silhouette. The GUI shows its tier. It keeps its current slots:

| Operation | Input slot | Catalyst slot | Blood reservoir | Result slot |
| --- | --- | --- | --- | --- |
| Existing distillation | Existing ingredient | Existing optional catalyst | Existing behavior | Existing result |
| Enzyme refinement | Finished drinkable potion | One enzyme | Refining cost | One refined potion |
| Compound brewing | Base drinkable potion | Second drinkable potion | Stabilizing cost | One compound potion |

The flask slot retains its existing blood-filling role. It is not a second potion input. New operations require the upgraded state and heat below the Alembic. The catalyst item's type chooses the operation; there is no mode toggle. If no valid recipe exists, the machine remains idle and consumes nothing. Existing distillation recipes take priority when their exact input and catalyst already match; new recipes cannot shadow them.

The pre-upgrade Alembic has **six** slots: input 0, flask 1, result 2, catalyst 3, flask output 4, and tincture blood 5. Athanor appends Catalyst 2 at **slot 6**. The base geometry is a resource-backed block model with reservoir fluid drawn by `GhastlyAlembicRenderer`; there is no current `GhastlyAlembicModel` Java class.

## Enzyme refinement

The player brews a normal finished potion first, then refines it with one centrifuged enzyme. Refinement is an authored, data-driven recipe list, **not** an automatic combination of every potion and tendency. Each recipe identifies an allowed base potion, one enzyme, a resulting potion, blood cost, processing time, and any drawback. Display each recipe and cost in JEI and show the prospective result in the Alembic GUI when inputs and blood are present.

Refinement changes a potion's specific use: a shorter, stronger burst; a modest added behavior; or a tactical tradeoff. Most modest refinements cost an enzyme and blood without a further penalty. Only particularly strong results carry a clearly listed drawback. A refined potion may serve as one component of a compound if it carries exactly one primary effect; its authored drawback remains in force. Refinement never applies to a compound potion, and a refined potion cannot be refined again.

The first D3 refinement recipe is specified in the pilot catalogue below. Further D3 recipes should use distinct base/enzyme pairs or explicit tier-priority rules so D5 recipes never ambiguously shadow them.

## Compound brewing

The base potion enters the **input** slot. A second finished potion enters the **catalyst** slot. The Alembic uses stored blood to bind their effects into **one drinkable compound potion** in the result slot. Both input potions and their bottles are consumed; the output supplies one bottle. The saved inventory slot is the reward, with one bottle and blood spent to obtain it.

The base determines the compound's display name, primary tint, and ordering of effects. The catalyst supplies the second effect. Neither effect gains amplifier or duration; preserve the effect values encoded on each ingredient potion. Both effects start when the compound is used. An effect with an independent redrinking rule, especially Mnemonic Whispers, keeps that rule. A compound drink counts as consuming Whispers for that check, even if Whispers is the catalyst effect.

**Eligibility rules for the first release:**

1. Both inputs are finished drinkable potions with exactly one primary effect each. Water, Awkward, Thick, Mundane, empty-effect, splash, lingering, and tipped-arrow potions are invalid. Already-compounded potions are reserved for the D5 vessel workflows described below.
2. The two primary effects must be distinct. Duplicate effects cannot be used to multiply duration, strength, or value.
3. At most one ingredient may be a refined potion. Any drawback carried by it is preserved in the compound and shown in the tooltip.
4. Positive and negative effects may be combined deliberately; show both before brewing. Do not quietly discard the harmful effect.
5. Only effect sources whose use-time behavior can be preserved are eligible. Initially support ordinary duration-based potion effects; expand instant or special event-driven effects only after their interactions have been implemented and checked individually.
6. A D3 compound potion cannot be compounded again at D3. D5 binding uses three separate one-effect potions; already-compounded potions are not valid binding ingredients. A three-effect vessel cannot be compounded further.

The finished item is nonstacking like an ordinary potion. Its tooltip lists both effects, amplifiers, durations, and any post-use drawback. Splash, lingering, and arrow conversion can be added only after those delivery forms correctly apply both effects and any related backlash. The first release centers on the drinkable form.

## Degree 5 upgrade: Sanguine Athanor

The second upgrade requires a Hematic Condenser as its rite subject and a Degree 5 Harbinger performing the rite. After the player demonstrates one enzyme refinement and one two-effect compound, the Alchemist grants a one-time **Sanguine Athanor Kit** and teaches the upgrade rite. The kit is its defining offering, joined by authored alchemical materials in the rite recipe. Use the standard D5 Cardinal Rite floor, focus, boundary, anchors, support sigil, and offering procession as presentation and interaction; disable combat waves, instability collapse, and other punitive failure rules for this station-upgrade recipe. Validate subject tier at activation and culmination. After the daemon seal, three raised alchemical vessels on the subject accept three short blood projections in sequence; each projection lights one tube and draws a red stream toward a shared central neck. The completed circulation triggers the ordinary culmination and transforms the existing block entity in place. An interrupted rite safely pauses or unwinds, leaving the Condenser at D3 and the offerings recoverable.

The Athanor model is visibly more substantial: a taller crowned upper glasswork assembly, three asymmetric distillation bulbs, three animated blood channels converging over the original boiler, and a brighter pulsing central reservoir. The D3 coil remains recognizable underneath. The model communicates the new second catalyst slot with a third physical vessel. Give the renderer appropriate culling bounds; the functional block footprint and neighboring block collision remain those of the existing Alembic. The GUI gains Catalyst 2 only at D5 and visibly distinguishes D3 and D5 tiers.

### Three-effect compound vessels

The Athanor unlocks a second catalyst slot and a durable **bound potion** with a three-dose capacity for initial balance testing. A finished drinkable base potion in input slot 0 and two different finished drinkable catalyst potions in slots 3 and 6 establish the bound formula. The base potion itself becomes the vessel; there is no separate empty vessel, binding ingredient, or flask-slot requirement. Its effect, name, tint, and applicable components become the bound identity, while the two catalyst effects define its refill signature. This initial recipe consumes all three potions and Alembic blood, and returns the transformed base potion with one prepared dose.

To refill, put the bound potion in the input slot and fresh potions matching its two recorded catalysts in the two catalyst slots. The flask slot retains normal blood handling. Each successful refill consumes those two potions plus Alembic blood and adds **one dose of the complete three-effect mixture**. The base potion is paid only at initial binding and remains baked into the vessel. This intentionally makes later doses cheaper than creating new vessels; the D5 unlock and refill costs balance that benefit. Refill never changes the bound base, catalysts, effect strength, or duration. Different catalyst order is accepted if effects and parameters match. At zero doses the bound potion retains its identity and can be refilled, but cannot be activated. New base identity requires binding another base potion.

Only three distinct primary effects qualify. The binding step always consumes three separate one-effect potions; a D3 compound cannot stand in for two of them. A three-effect vessel cannot be used as an ingredient in another recipe.

### Blood substitution when used

On every activation with at least one prepared dose, roll once on the server. Initial balance value: **20% chance** to consume **1,000 mL from the user's own blood-volume reserve** and preserve the prepared dose. Only roll when the player has the full blood cost available under normal blood-expenditure rules. Otherwise skip the roll and consume one dose. If the roll fails, consume one dose and no blood. There is no reroll by swapping hands or cancelling a use animation: resolve once at the point the effects actually apply. At zero doses neither blood payment nor effects occur. The vessel's tooltip shows remaining doses, maximum doses, blood cost, and substitution chance. These numbers are tuning defaults, not promises of final balance.

### D5 refinement range

The Athanor also unlocks a separately authored group of higher-tier refinement recipes. Their distinctive value is wider tactical range: shorter but stronger bursts, carefully limited reach or area effects, and transformations of existing Hemomancy potions tied to the eight tendencies. Each recipe declares its base, catalyst enzyme, output effect parameters, blood and heat cost, and any rebound condition. It may not increase effect count beyond the vessel's three-effect cap, bypass existing Whispers backlash, or convert one reagent into multiple potion uses. Avoid a generic rule that automatically upgrades every amplifier or duration. Begin with the two D5 pilot recipes below and add vivacious siphon sustain only after their behavior has been tested.

## Three pilot refinement recipes

These are the **initial balance values**, not an auto-generated effect ladder. Each brew consumes one ordinary-strength, drinkable base potion and one enzyme in a heated Alembic with the required tier. Long and strong vanilla variants, splash/lingering variants, and already-refined potions do not match. Each produces one drinkable, nonstacking refined potion; matching compares the base potion's exact registered type rather than its translated name. All effects and side behaviors are applied on the server.

| Unlock | Registered base + catalyst | Result | Reservoir blood | Time | Exact effect and limit |
| --- | --- | --- | ---: | ---: | --- |
| D3 Condenser | Standard Potion of Strength + Ferric Enzyme | **Potion of Ferric Poise** | 250 mL | 100 ticks | Strength I for **90 s**; **+20% knockback resistance** for the same 90 s. The resistance is an attached behavior, not a second primary potion effect. No rebound. |
| D5 Athanor | Standard Potion of Swiftness + Neurotic Enzyme | **Potion of Nerve Surge** | 750 mL | 120 ticks | Speed II for **25 s**; apply Slowness I for **8 s** when the surge ends or is removed early, except on death. Each drink schedules its own rebound; redrinking does not erase an earlier rebound. |
| D5 Athanor | Standard Potion of Fire Resistance + Fervent Enzyme | **Potion of Fervent Reprisal** | 750 mL | 160 ticks | Fire Resistance for **90 s**. Once every **5 s** at most, a direct melee attacker who damages the wearer is ignited for **2 s**. No retaliation on blocked, indirect, reflected, or self-inflicted damage. No rebound. |

Ferric Poise is eligible as one refined component of a D3 two-effect compound: its +20% knockback resistance accompanies its Strength effect and remains visible in the output tooltip. **Nerve Surge and Fervent Reprisal are single-use refinements for the pilot release**, excluded from D3 compounding and D5 vessel binding until their delayed and reactive behaviors have been checked in combination. This is an explicit eligibility rule, not an effect-count accident. In all cases, use the already-registered vanilla Strength/Speed/Fire Resistance effect semantics and add only the described attached behavior; do not make a second potion effect merely to display the behavior.

For Ferric Poise, the +20% is an attribute modifier that is removed exactly when the 90-second effect ends and does not add another 20% on every tick or every drink. Reapplication follows the usual stronger/longer effect rules but never stacks copies of that modifier. For Fervent Reprisal, the cooldown belongs to the wearer, and a fresh dose does not clear an active retaliation cooldown. For Nerve Surge, persist the pending rebound through logout; clear it on death. All three show their exact duration, behavior, and any rebound in the tooltip. Evaluate balance after testing these three before authoring additional refinery recipes.

## Upgrade rites: authored ceremony

Both station upgrades are **safe utility Cardinal Rites**, not degree advancement rites or combat ordeals. They use the shared floor/Focus activation, exact lit-brazier offering signature, Living Staff medium, boundary consecration, daemon seal, persistent phase state, offering procession, and final server-side validation. They deliberately override the shared collapse and nonrefund behavior: no enemies, instability timer, offering-loss hazard, or random failure. The Alembic is a declared **subject block on a marked plinth beside the Focus**; it does not replace the Focus or its Living Staff medium. Its block position, tier, and inventory/tank snapshot are captured at activation. Lock its menu, hoppers, and blood routing for the rite; reveal temporary ghost geometry on the client without changing the actual tier until culmination. Preserve the station's inventory and tank across successful in-place transformation.

Hold the exact kit and other authored offerings in **recoverable rite escrow** until success; return the exact stacks on deliberate cancellation or safe abort. Projected blood is spent when projected, but missing blood merely pauses the next interaction. The player may leave and return, log out and return, or repair an accidentally damaged floor without losing progress. If the subject or Focus is removed or replaced, safely abort: undo temporary visuals, return the exact Living Staff and every escrowed offering to the player or a persistent Focus recovery claim, and leave any surviving station at its prior tier. Preserve recovery across reload and death; never silently drop escrowed items into an unloaded area. At a valid culmination, consume the offerings exactly once and change the tier in the same server transaction. If the final check fails, leave the tier unchanged and return escrow instead. The standard 80-tick Blood Absorption cancellation gesture can initiate this **safe cancellation** override. These utility rites have no true fail state and cannot strand a one-time kit.

### D3 — The First Condensation

**Eligibility:** Degree 3+ caster; unmodified Ghastly Alembic as subject; one ordinary Alembic distillation already demonstrated to the Alchemist. The NPC grants one Hematic Condenser Kit after the lesson. The rite accepts the kit even when it has been recovered from another machine. Its recipe is Lesser form, with one four-anchor boundary ring; it awards no degree.

**Floor and offerings:** Put the Alembic on a marked stone-and-Hematic-Iron plinth one block off the central Focus, with a separate clear boiler silhouette. The exact lit-brazier signature is **one Hematic Condenser Kit**, which already incorporates the lesson's alchemical components. This keeps the D3 rite's offering burden modest. No support sigil or allied role is required; existing optional support behavior may remain available when allowed by the rite definition.

**Sequence:**

1. Light the kit's offering brazier, place the unmodified Alembic, and plant the Living Staff at the Focus. The activation check names the chosen subject in its preview: *Ghastly Alembic → Hematic Condenser*. The kit remains a committed offering, not an item clicked directly onto the block.
2. Project blood into four boundary anchors in the normal order. The completed ring draws a thin red channel inward to the Alembic plinth. The ordinary central daemon rises from the staff, then takes on a narrow glasslike sternum with a blood filament passing through it. Seal it in the ordinary manner.
3. During the profession act, two small, hollow condenser coils appear over the existing Alembic model. Project **50 mL** into the intake terminal, then **50 mL** into the return terminal. The first projection drives a red meniscus around the boiler; the second closes the circuit and draws a visible droplet through the unformed receiving bulb. A wrong interaction outside these two lit terminals does not consume blood or reset the rite.
4. The ordinary offering procession draws the committed kit to the Focus. At culmination, a receiving-bulb silhouette nests above the boiler, the frame sets beneath it, and the two blood-filled coils become solid parts of the D3 model. The kit is consumed, tier changes atomically, the original inventories/tank remain, and the exact escrowed Living Staff is returned. A brief red drip from the new condenser signals the unlock; no extra potion is created.

Leaving the boundary pauses progress and dims the temporary parts; returning resumes the same step. Cancellation or subject loss uses the safe escrow-return rule above. The machine remains an ordinary Alembic until the completed culmination.

### D5 — The Sanguine Athanor

**Eligibility:** Degree 5+ caster; existing Hematic Condenser as subject; Alchemist has recorded one enzyme refinement and one two-effect compound and granted the one-time Sanguine Athanor Kit. Greater-form safe utility rite with the D5 boundary and support presentation but no combat or collapse; it awards no degree and cannot be run on a base or already-D5 machine.

**Floor and offerings:** A broader three-lobed floor extends from the subject plinth toward three bulb-shaped surface marks without moving the central Focus. Exact five-brazier signature: Sanguine Athanor Kit, Sanguine Quintessence, Vivacious Enzyme, Neurotic Enzyme, and Ferric Enzyme. The three distinct enzymes stand for flow, impulse, and containment; they are rite offerings, not three preloaded brew ingredients. Require the Reservoir support sigil for its visual circulation. Use two rings of four anchors, but omit the ordinary D5 hostile ordeal and instability collapse. The player advances by completing the three alchemical projections at their own pace.

**Sequence:**

1. Seat the Condenser on its plinth, light the exact five braziers, and plant the Living Staff. The preview identifies *Hematic Condenser → Sanguine Athanor*. Activation captures the subject tier and rejects a currently processing machine; idle inventory/tank contents remain untouched.
2. Consecrate both four-anchor rings. A completed arc sends a thin blood stream into the subject's existing D3 coils. Trace the required Reservoir sigil during inscription and seal the central daemon. Once sealed, three spectral distillation bulbs unfold above the subject, each linked to one of the three enzyme offerings by a colored thread.
3. With no combat timer, the bulbs illuminate **flow → impulse → containment**. Project **50 mL** into each lit floor mark in that order. Each mark sends a corresponding blood tendril upward into its bulb. A projection at an unlit mark is ignored without spending blood. If the floor is disturbed, pause the same bulb until it is repaired; completed bulbs remain complete.
4. After the third bulb is charged, the ordinary offering procession draws the escrowed kit and other offerings into the three bulbs. The central daemon is drawn through the existing condenser coil, splits into three visible red streams, and reunites inside the crown. The old D3 parts remain as the lower machinery while the upper glasswork settles into solid geometry. At the server-side culmination check, consume offerings and advance tier atomically, reveal Catalyst 2 in the GUI, and return the exact Living Staff. A short three-beat pulse acknowledges the upgraded machine; it produces no free vessel or potion.

Cancellation, subject loss, or a rejected final check safely unwinds the spectral bulbs and returns escrow; the D3 machine remains. On later block breakage the upgraded machine yields its block item plus both installed kits exactly once; the player can repeat the rites on another placement without repeating the Alchemist's one-time lessons.

## Resource and balance rules

- Initial tuning defaults: **250 mL** reservoir blood and **100 ticks** processing for D3 refinement; **500 mL** and **160 ticks** for D3 compounding; **1,000 mL** and **200 ticks** to bind a three-effect vessel; **750 mL** and **160 ticks** for each refill. D5 pilot refinements override the D3 numbers as specified in the recipe table. Values belong in recipe data or configuration so playtesting can adjust them.
- The machine checks all inputs, output fit, upgrade state, heat, and blood before processing starts. It commits item and blood consumption only once an output can be delivered. Interrupted processing leaves its unspent inputs intact.
- Refined and compound potions cannot be multiplied by automation or by conversion recipes. The output is always exactly one bottle.
- At D3, the compound benefit is carrying two effects in one slot: both potions are spent. At D5, the bound base effect is deliberately reusable; every additional complete dose costs both matching catalyst potions and blood. Effects retain their own durations and strengths.
- Effects that already have special limits, such as Mnemonic Whispers, apply them to compound use. Add exclusions if a combination defeats a specific game's balance rule rather than barring all mixed potions.

## Player feedback and Alchemist teaching

Before activation, the GUI shows **Refine**, **Compound**, **Bind vessel**, **Refill vessel**, or **Ordinary distillation** based on valid inputs and tier. If the recipe is blocked, show a short reason: upgrade needed, no heat, not enough blood, incompatible effects, wrong refill signature, vessel full, or output occupied. The player can inspect the predicted result and its full tooltip. Installed parts remain visible through save/reload; on relocation the recovered kits can be reinstalled by rite.

The Alchemist's lesson should explain the distinction in plain language: *“An enzyme changes what a potion does. Blood lets two finished potions hold together. Brew them first; bring them here when you know what you want to carry.”*

## Implementation boundaries and checks

Add a persistent three-state tier field (base, Condenser, Athanor) and tier-dependent model parts to the existing Alembic rather than a separate brewing block. Add a second catalyst inventory slot that is accessible only at D5. Add explicit recipe handling for refinement, compounding, binding, and refilling with deterministic matching and server-side output validation. Reuse the existing potion contents representation where possible; store a compound marker and the vessel's bound base/catalyst signature for refill checks, tooltip, color, and anti-recursion. Keep normal distillation matching untouched.

Verify: old distillation and flask filling still work; a non-upgraded Alembic rejects advanced recipes; both upgrade rites require their exact subject tier and never duplicate kits; safe cancellation, logout, subject loss, and final-check rejection return every escrowed item exactly once; neither rite spawns enemies or collapses through instability; each installed kit is returned once on block break; D5-only catalyst access cannot be bypassed by hoppers; reloading mid-process cannot consume resources twice; the three pilot outputs have their specified durations, attribute cleanup, rebound persistence, and retaliation cooldown; two and three distinct potion effects retain their independent durations; vessel binding and refill enforce the recorded signature; zero-dose activation does nothing; blood substitution charges only on success; Whispers redrinking protection still fires; invalid and recursive combinations consume nothing.

## Open review decisions

The first release specifies drinkable, duration-based compounds. Before implementation, confirm whether the player also wants instant-effect compounds and splash/lingering/arrow forms in the same milestone. Their support changes use-time event handling, not the station layout or D3 unlock.
