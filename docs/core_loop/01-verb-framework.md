# Track 1: The verb framework

**Goal:** make Projection and Absorption the two mechanical powers, and let a selected manipulation shape what they do
to the thing being aimed at. This track builds the plumbing and converts nothing; Track 2 is the first use.

**Depends on:** Track 0 (`PlayerBlockEdits` helpers).

## What the code already gives

- **Both verbs ride vanilla use-item.** `BloodProjectionItem.onUseTick` and `BloodAbsorptionItem.onUseTick` run on the
  server while the button is held. No payload is needed for a Shaping.
- **The projection chain is already an ordered list of claims** in `BloodProjectionItem.performProjection` (about lines
  83 to 148): `EarlyInitiation.tryProject`, `SaintChannelRouter.tryProject`,
  `WillBloodUtilityInteractions.tryProjectBanishFalteringWill`, `CardinalRiteInteractionHandler.tryProject`,
  `BlockBloodInteractions.tryProjectIntoLookedAtBlock` (Earthen Vein travel, Bloodwood growth, block endpoints), then
  on a block hit `BloodStructureFeedManager.feedStructure`, `SanguineCondenserProjection.tryProject`,
  `BloodInfusionManager.feedBlock`, `ProjectedBloodSink.takeProjectedBlood`.
- **The absorption chain is the same shape** in `BloodAbsorptionItem.onUseTick` (97 to 157): Vesper, Saint,
  `CardinalRiteCancellationHandler.tryChannel`, `tryAbsorbFromLookedAtBlock`, Will, then living targets on a pulse.
- **The attach pattern exists.** `setDrudgeAction(...)` and `setEntityCast(EntityCasts::x)` hang optional behaviour on
  a manipulation in `ManipulationInit`, with a guardrail test that keeps name checks out of the dispatcher.
- **Continuous casting exists.** `canContinueChannel`, `tickContinuousAction`, `clearContinuousSession`,
  `finishContinuousAction` on `BloodManipulation`; `ManipulationChannelManager` for R channels.
- **Movement and cadence skills exist** for absorption (`BloodAbsorptionChannelRules`, `skill_dragging_siphon`,
  `skill_quickened_draw` and the rest) and rate skills for the Staff (`skill_living_conduit`, `skill_vascular_draw`,
  `skill_crimson_projection`, `skill_hematic_focus`).

## What is missing

- **Targets.** `SanguineProjectionTargeting.pick` returns a block hit (`OUTLINE`, `Fluid.ANY` on request). There is no
  handling of a fluid hit as a fluid, no miss, no aimed entity for projection, and nothing targets an `ItemEntity`.
  Living-target absorption is not aimed at all (nearest in a box).
- **Reach.** `PROJECTION_REACH` is a fixed 5.5 that neither the Staff nor Sanguine Reach changes. Absorption reach is
  5.0 bare and `6 + 1.5 × conduit + 0.75 × focus` on the Staff.
- **A gate.** The hand tools bypass everything `tryPerformAction` checks: paralysis, path permissions, purity, the
  alignment gate, mastery. A Shaping must not be a way round those.
- **A target in the signature.** `getAction` receives only `player.blockPosition()`.
- **A verb choice.** On the Staff, the selected manipulation decides project or absorb
  (`LivingStaffUtilitySelectionRules`, by name). If selection becomes the Shaping, something else has to pick the
  verb.
- **Client agreement.** `CellHandParticleEffects` re-picks its own target on the client (lines 92, 161, 224), so the
  hand's stream can point somewhere the server did not.
- **A HUD.** `ManipulationChargeOverlay` is CHARGED-only. Nothing shows a channel, its cost, or what it is aimed at.

## Target design

### 1. One target type

`common/manipulation/verb/VerbTarget` (sealed): `Block(hit, state)`, `Fluid(pos, fluidState)`, `Creature(living)`,
`Items(list of ItemEntity, focus point)`, `Air(point at reach)`. One pure function,
`VerbTargeting.pick(eye, look, reach, wants)`, used by the server chain and by the client stream. `wants` is a small
set of flags the Shaping declares, so plain Projection keeps today's block-only pick and nothing changes for it.

### 2. One reach

`VerbReach.of(player, verb)` replaces the fixed constant and both absorption formulas: base 5.5, plus the Staff's
conduit and focus terms, plus Sanguine Reach, plus Track 8's staff growth. A Shaping may scale it (a beam reaches
further than a touch) but may not replace it. This is what makes range worth investing in.

### 3. Shapings

A manipulation may carry a projection Shaping, an absorption Shaping, or both:

```java
ManipulationInit: .setProjectionShaping(Shapings::lignumBeam)
                  .setAbsorptionShaping(Shapings::itemDraw)
```

A `VerbShaping` declares: which target kinds it `wants`; `claims(player, target)`; `pulse(player, target, context)`;
optional `begin` / `end` for sessions; its pulse cadence; its blood per pulse; whether it roots the player. One
dispatcher, `common/manipulation/verb/ShapingDispatcher`, finds the selected manipulation's Shaping and runs it. The
dispatcher never names a manipulation (`ShapingGuardrailTest`, modelled on `EntityCastGuardrailTest`).

### 4. Where it sits in the chains

```
Projection:  initiation → Saint → Will banish → Cardinal Rite → [Shaping, if it claims the target]
             → block endpoints → structure feed → Condenser → infusion → reservoir → [stain, Track 6]
Absorption:  Vesper → Saint → rite cancellation → block endpoints → Will → [Shaping, if it claims]
             → living targets on the pulse → strain
```

- Projection: between lines 111 and 113, so a Shaping that claims a brazier or a log acts before the default
  endpoints. A Shaping that does not claim falls through, so projecting into a machine with Pyretic Forge selected
  still fills the machine. (`CL-4`.)
- Absorption: between 135 and 136, ahead of the "is my blood full" stop, so an item draw works on a full body.
- Staff: `LivingStaffItem.onUseTick` keeps `EngramTracing`, `isPlantingStaff` and the `useOn` rite activation first.
- `SaintCombatKitGuardrailTest.theChannelItemsNameOnlyTheRouter` checks text order in `BloodProjectionItem`; rewrite
  it to pin the new order.

### 5. Payment and gates

A new entry on `BloodManipulation`, `tryPerformVerbPulse(player, shaping, target)`, shares the front half of
`tryPerformAction`: paralysis, `canPerformAction`, dead vein, `HarbingerPathPermissions`, `isActive`, the alignment
gate, `ManipulationCostLedger.collect`. It then pays through `BloodAccounting.payUpkeep` with a new
`Source` (`SHAPING`, counts as spend), charges Temper (Track 4), and credits mastery at a reduced rate. It applies no
cooldown and sends no `ManipCooldownPacket`. It does not go through `UseManipKeyPacket`, which has no rate limiter.

Costs are per pulse, declared by the Shaping, default cadence 10 ticks. Staff rate skills scale the work done per
pulse, not the price.

### 6. Choosing the verb (`CL-2`)

Recommended: the verb is its own piece of state, not a selection.

- `KnownManipulations` gains `activeVerb` (PROJECT or ABSORB). A key toggles it; the hand tool in the hand swaps to
  match; the Staff reads it instead of the selected name.
- `blood_projection` and `blood_absorption` stop being selectable entries. `conjure_staff` stays. The radial then
  holds only Shapings and casts, and the three always-first slots in `ManipulationEquipHelper.normalizeEquippedNames`
  drop to one.
- A Shaping that supports only one verb runs on that verb; on the other, the plain verb runs.
- Selecting a Shaping while the Staff is in a weapon form returns it to staff form, as selecting any non-form
  manipulation does today (`LivingStaffWeaponFormHelper.applySelection`). Weapon forms fight; the staff works. No
  change needed, but say so in the Liber.

Rejected: sneak plus use for the opposite verb (sneak already retrieves brazier offerings and plants the Staff); two
mouse buttons (the left button is attack and block breaking).

### 7. Feedback

- The hand's stream uses `VerbTargeting.pick` on the client, so it lands where the server acts.
- A channel layer in `GuiLayerRegistrar`: the verb, the Shaping's icon, blood per second, and a small reticle state
  for "this target will be claimed". Thin; no bars across the screen.
- `CastingAnimationManager.tick` cancels sustained cues unless `ManipulationChannelManager.isChanneling`; give
  Shaping sessions the same standing or their animation drops after one tick.
- `ManipulationVisuals` sends to everyone within 64 blocks. A Shaping pulse every 10 ticks is fine; a per-tick visual
  is not. Send a begin and an end, and let the client animate between.

## What changes, file by file

| File | Change |
| --- | --- |
| `BloodProjectionItem`, `BloodAbsorptionItem` | Insert the dispatcher call; use `VerbTargeting` and `VerbReach`. |
| `LivingStaffItem` (108 to 190) | Read `activeVerb`; drop the name check. |
| `LivingStaffUtilitySelectionRules` | Retire. |
| `BloodProjectionInteractionEvents` (62, 73), `CellHandParticleEffects.isStaffChannel`, `isChannelingBloodAbsorption`, `CardinalRiteStaffEscrow` (56 to 65) | Read the verb, not the selection. |
| `KnownManipulations`, `ChangeSelectedManipPacket`, `UpdateCurrentManipPacket` | `activeVerb`; one new small payload for the toggle (protocol bump). |
| `ManipulationEquipHelper`, `ManipSlotHelper`, radial screen | Two fewer fixed entries. |
| `BloodManipulation` | Shaping fields and `tryPerformVerbPulse`. |
| `ManipulationInit` | Fluent setters. |
| New `common/manipulation/verb/` | `VerbTarget`, `VerbTargeting`, `VerbReach`, `VerbShaping`, `ShapingDispatcher`, `ShapingSessions` (registered with `TransientServerStateEvents`). |

## Tests

- New: `VerbTargetingTest`, `VerbReachTest`, `ShapingDispatcherTest`, `ShapingGuardrailTest`; a `shaping_validation`
  GameTest batch that runs a stub Shaping against each target kind with a real player and checks the chain order.
- Rewrite: `SaintCombatKitGuardrailTest` (order), `BloodAbsorptionItemSourceTest`, `LivingStaffUtilitySourceTest`,
  `LivingStaffUtilitySelectionRulesTest` (delete), `CellHandFormSwapSourceTest`, `RadialChooseManipScreenSourceTest`,
  `KnownManipulationGrantHelperTest`, `ConjureStaffEquipTest`, `MemoryEquipRulesTest`, `ManipulationLoadoutTest`,
  `ServerboundGuardsCoverageTest`, `PayloadProtocolVersionTest`.
- Must stay green untouched: every GameTest that calls `projectFromEntity` directly (BloodInjection, ChamberGateway,
  HarbingerRepair) and the playthrough suite's `PlaythroughPlayer.startHold`.

## Hazards

- A Shaping that claims too eagerly steals rite and machine input. `claims` should be narrow and each Shaping's
  GameTest should include "aimed at a rite anchor, the rite still wins".
- Removing Projection and Absorption from the radial touches the First Bloodcraft teaching (`FirstBloodcraftAssignment`)
  and about 177 lang lines that name the two as things you select. Track 11 holds the list.
- Absorption roots the player unless skilled; projection does not. A Shaping declares its own rooting so a beam does
  not inherit the wrong one.

## Open questions

`CL-1`, `CL-2`, `CL-3`, `CL-4`.

## Acceptance

- With nothing Shaping-like selected, both verbs behave exactly as before and the playthrough suite is unchanged.
- A stub Shaping can claim each of the five target kinds, pays through `BloodAccounting`, respects paralysis and the
  alignment gate, and loses to rites, Saints and Wills.
- The hand stream and the server agree on the target.
