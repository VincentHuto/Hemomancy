# Harbinger Alpha Journey: Degree 2 Design

## Goal

Extend the existing connected-player Harbinger alpha journey from the confirmed Degree-1 First Bloodcraft hand-in through a complete Degree-2 progression loop. The new slice covers the Rite of the Votary, Alchemist briefing, the real Vial Centrifuge interaction, enzyme recovery, and a one-time Alchemist reward.

The existing commands remain unchanged:

```text
/hemo test journey start
/hemo test journey next
/hemo test journey status
/hemo test journey reset
```

The journey remains continuous. A new internal chapter boundary groups Degree-2 fixtures and checks without requiring a separate command or a second snapshot.

## Scope

The slice includes:

1. Completing the real Rite of the Votary.
2. Verifying Degree 2 and its advancement.
3. Speaking to a marked Harbinger Alchemist to accept The First Separation.
4. Operating a real Vial Centrifuge with a deterministic balanced sample setup.
5. Starting a valid separation through the actual machine screen.
6. Recovering the resulting enzyme from the machine.
7. Returning to the Alchemist for a one-time sampling-kit reward.
8. Restoring the original pre-journey player snapshot on final completion.

Optional Degree-2 assignments, Living Bestiary progress, Red Taxonomy, Eightfold Centrifuge mastery, Degree 3, and unrelated machine workflows are outside this slice.

## Checkpoint Sequence

The existing `VICAR_REWARD` stage transitions into these stages instead of `COMPLETE`:

### `VOTARY_RITE`

Prepare the authoritative Rite of the Votary as a horizontal 5-by-5 pattern using the loaded `cardinal_rite/votary_rite` recipe orientation. Give the player enough blood to meet its 250 mL cost and position them within interaction range. The player personally invokes the blood-crafting control on the rite's center Hematic Iron Block.

Verification requires initiatory degree exactly 2 and the `degree_2_votary` advancement. The rite's incidental result item is not required; rank and advancement are the authoritative checkpoint outcomes.

### `DEGREE_2_REACHED`

Report the new rank and prepare the Alchemist introduction. This explicit boundary makes status output understandable and prevents the rite fixture from being confused with the assignment fixture.

### `ALCHEMIST_BRIEFING`

Spawn one marked, invulnerable, no-AI Harbinger Alchemist. Add a real Degree-2 dialogue option for accepting The First Separation. Selecting it grants a hidden impossible-trigger advancement used as the durable acceptance record. The handler rechecks Degree 2 before granting it.

Verification requires the newly earned briefing record. Repeated conversation is harmless and does not duplicate state or rewards.

### `CENTRIFUGE_PREPARED`

Place a journey-owned Vial Centrifuge in the fixture. Preload two matching sampled Blood Vials into an opposing pair of input slots so the machine is balanced. The sampled entity must resolve to one deterministic non-empty enzyme output. No journey code calls `attemptStartup()` directly.

The player opens the real menu and presses its Start button. The action instruction explains that the paired samples are intentional counterbalance.

### `SEPARATION_STARTED`

Verification requires all of the following:

- The fixture still owns the expected Vial Centrifuge block entity.
- The machine entered its real spinning state.
- The `first_separation_started` advancement was incomplete at preparation and is now complete.

After verification, the same machine and contents remain in place. The journey does not rebuild the fixture between start and recovery because doing so would destroy the active operation.

### `ENZYME_RECOVERED`

The player waits for the normal 200-tick spin to finish, opens the machine, and removes the enzyme from its output slot. Verification accepts the deterministic enzyme only as a positive inventory delta attributable to this stage and requires the existing `first_separation_complete` return-ready advancement.

The action message tells the player to take the enzyme rather than leave it in the machine.

### `ALCHEMIST_REWARD`

Replace the machine fixture with one marked Alchemist. The Alchemist offers a completion option only when the player is Degree 2+, has the briefing record, has both separation advancements, and has not claimed the reward.

The dialogue handler repeats every eligibility check immediately before claiming. It records the claim before issuing items and fails closed if the claim record cannot be verified.

The exact one-time reward is:

- 1 Living Syringe.
- 1 Vial Rack initialized with eight empty sampling vials.

The rack's embedded vial inventory is part of the exact reward contract; eight loose items are not granted in addition. Inventory overflow drops beside the marked Alchemist and is attributed using the same inventory-delta and exact-UUID rules as the First Bloodcraft reward.

### `COMPLETE`

The existing final acknowledgement removes Degree-2 fixtures and attributed outputs, then restores the original pre-journey snapshot.

## Production Gameplay Additions

Add a focused First Separation assignment helper responsible for:

- Briefing and reward advancement identifiers.
- Server-authoritative briefing, eligibility, claim, and verified-claim operations.
- Construction of the exact initialized reward stacks.

Alchemist dialogue owns presentation only. Dialogue event handling delegates all state and reward decisions to the helper. The hidden briefing and reward advancements use `minecraft:impossible`, remain absent from the visible advancement tree, and provide durable per-player state without introducing separate saved data.

Existing public progression identifiers remain unchanged. The new state is additive and does not invalidate existing worlds.

## Fixture Ownership and State

Degree-2 fixture blocks, the Vial Centrifuge block entity, marked NPCs, machine contents, and attributable output entities remain development-only concerns under `src/gameTest`.

The snapshot expands its exact advancement ownership to include:

- `degree_2_votary`
- the hidden First Separation briefing record
- `first_separation_started`
- `first_separation_complete`
- the hidden First Separation reward-claimed record

Reset revokes those journey-owned milestones, removes the active centrifuge and its contents, removes marked NPCs and attributed drops, clears active potion effects, and restarts at `MORTAL_DISPLAY` while retaining the original snapshot. Final completion and `/hemo test clear` restore all captured progression and inventory state.

The active centrifuge must survive the transition from `SEPARATION_STARTED` to `ENZYME_RECOVERED`. This is an explicit retained-fixture transition, not a normal cleanup-and-prepare transition.

## Failure Handling

Every checkpoint reports all currently unmet conditions and remains at the same stage on failure. Important failures include:

- Rite pattern or rank-up did not complete.
- Alchemist briefing was not accepted.
- Centrifuge block entity is missing or no longer journey-owned.
- Samples are no longer balanced or processable.
- Start button did not produce a real spinning state.
- Start or completion advancement was not newly earned.
- Expected enzyme was not removed into the player's inventory.
- Alchemist eligibility changed before option selection.
- Claim record could not be persisted and verified.
- Reward quantities or initialized rack contents do not match exactly.

The verified-stage latch continues to protect successful destructive checks from being repeated if preparation of the following checkpoint fails.

## Testing

Automated coverage must include:

- Rite fixture geometry matches the authoritative horizontal 5-by-5 recipe.
- Rite completion produces Degree 2 and its advancement.
- Briefing is Degree-2-gated, idempotent, and durable.
- Centrifuge fixture uses an opposing balanced pair and a deterministic processable sample.
- Only the real Start-button path grants `first_separation_started`.
- The machine persists across the start-to-recovery stage transition.
- Enzyme inventory delta and `first_separation_complete` are both required.
- Preexisting enzymes cannot satisfy recovery.
- Alchemist handler rejects forged, premature, and duplicate events.
- Successful hand-in grants exactly one Living Syringe and one initialized eight-vial rack.
- Full inventory overflow remains attributable and cleanable.
- Reset removes the machine, contents, NPCs, outputs, new advancements, and potion effects.
- Final restore reconstructs the captured player state.
- All journey code remains excluded from the release jar.

The full acceptance gate remains `./gradlew.bat alphaCheck --no-daemon`, followed by release-jar boundary inspection and `git diff --check`.
