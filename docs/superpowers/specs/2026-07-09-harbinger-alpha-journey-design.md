# Harbinger Alpha Journey Design

## Goal

Provide a development-only, controlled early-game journey that uses a real logged-in player and real gameplay interactions from Blood Temple activation through completion of the Degree-1 Vicar assignment. The controller prepares deterministic checkpoints, but the tester performs each gameplay action personally.

## Scope

The first journey covers these checkpoints in order:

1. Activate the Mortal Display.
2. Complete the Sanguine Initiation rite.
3. Fill the player's vessel to the 5,000 mL assignment milestone.
4. Project Sanguine Formation from an attuned projector block.
5. Complete the Liber Sanguinium blood-structure craft.
6. Complete the Hematic Iron Block blood-structure craft.
7. Return to a Harbinger Vicar and claim the First Bloodcraft completion kit.

Degree 2, Hermit Road, optional side assignments, and fully automatic bot-driven interaction are outside this slice.

## Commands

The existing development-only `/hemo test` command tree gains:

```text
/hemo test journey start
/hemo test journey next
/hemo test journey status
/hemo test journey reset
```

`start` creates or restores the controlled fixture area, snapshots the player's journey-owned state, resets the early Harbinger progression fields, teleports the player to the first checkpoint, and records the active stage.

`next` verifies only the observable outcome of the current stage. On failure it reports every unmet condition and leaves the stage unchanged. On success it records completion, prepares the next fixture, teleports the player into position, and explains the next action.

`status` reports the current stage, expected player action, and verification conditions without mutating state.

`reset` removes journey-owned entities/blocks, restores the controlled fixture area to its initial layout, resets journey progression, and returns to the Mortal Display checkpoint. It does not restore the pre-journey player snapshot; that happens when the journey completes or when `/hemo test clear` abandons the journey.

## Controlled World Area

The journey uses a fixed development fixture origin relative to the player's position when `start` is invoked. The origin and stage are stored in persistent player data so reloads do not lose the journey.

Each stage owns a bounded fixture region. Preparation clears and rebuilds only blocks inside that region. Journey-created NPCs receive a persistent marker and are removed by reset/cleanup; unrelated nearby entities are never removed.

The initial implementation builds fixtures programmatically from registered blocks, items, recipes, and entity types. This avoids shipping a mutable save inside the repository and allows fixture preparation to follow current registry data. Large structures may later move to structure-template NBT without changing the command contract.

## Player State

At journey start, the controller snapshots only state it will modify:

- Blood active flag, current volume, and maximum volume.
- Initiatory degree.
- Relevant early-game advancement completion.
- Inventory and equipped Harbinger items needed by the journey.
- Journey command origin and current stage.
- Whether the First Bloodcraft completion reward was already claimed.

The journey reset path deliberately establishes a known test state: inactive blood, Degree 0, zero relevant assignment milestones, and a controlled inventory. Stage preparation grants only the materials required to perform the next action.

On successful completion, the controller prints a full checkpoint report and restores the pre-journey snapshot after the tester acknowledges completion through one final `journey next`. If restoration cannot reconstruct a saved item or advancement state, the command stops and reports the exact field rather than silently discarding data.

The existing `/hemo test clear` command detects an active journey, removes journey-owned fixtures, restores the pre-journey snapshot, and clears the journey marker. It retains its current behavior when no journey is active.

## Checkpoint Verification

Verification reads real server-authoritative state:

- Mortal Display: blood is active, First Awakening advancement is complete, the display transformed, and the Charm of Vascularium was equipped or dropped.
- Sanguine Initiation: degree is exactly 1, the Degree-1 advancement/milestone is complete, and the Sanguine Conduit reward is present.
- Vessel: current blood is at least 5,000 mL and `vessel_filled` is complete.
- Formation projection: a new Sanguine Formation item exists in the expected fixture region and the expected blood was spent.
- Liber craft: the player or fixture output contains Liber Sanguinium and `liber_sanguinium` is complete.
- Hematic Iron craft: the expected output exists and `iron_in_the_blood` is complete.
- Vicar return: all three First Bloodcraft milestones remain complete, the real Vicar dialogue grants the completion kit, and the hidden claim advancement prevents duplicate rewards.

## First Bloodcraft Hand-In

The Degree-1 Vicar dialogue exposes a completion option only when `vessel_filled`, `liber_sanguinium`, and `iron_in_the_blood` are complete and `first_bloodcraft_reward_claimed` is not complete. The option dispatches a dedicated server-side dialogue event.

The handler re-checks all four conditions before changing inventory. A valid claim grants exactly:

- 4 Hematic Iron Scrap.
- 8 of the placeable `hemomancy:befouling_ash_trail` item (displayed to players as Befouling Ash).
- 2 Sanguine Formation.

Items that do not fit in inventory are dropped at the Vicar. After issuing the kit, the handler awards the hidden `hemomancy:hemomancy/first_bloodcraft_reward_claimed` advancement. Repeated or forged dialogue events cannot issue another kit. An early claim attempt grants nothing and returns an explanatory message.

The advancement uses the `minecraft:impossible` trigger, is hidden from the advancement UI, and is the authoritative idempotency record. The Harbinger Assignment Ledger treats First Bloodcraft as complete when its existing three milestones are complete; reward-claimed state is reported separately rather than increasing the assignment's `3/3` count.

## Failure Handling

Checkpoint failures never advance the journey. The command reports all failed conditions in red and the successful conditions in green. Preparation exceptions leave the previous stage active and invoke cleanup for partially created blocks/entities.

Starting a new journey while one is active first asks the operator to use `journey reset`; it never overwrites an existing snapshot. The existing `/hemo test run_all` remains independent and cannot clear an active journey snapshot.

## Test Strategy

- JUnit source/contracts verify command registration, stage ordering, persistent keys, snapshot ownership, and documentation.
- Focused gameplay contracts verify the hand-in eligibility predicate, exact kit quantities, dialogue option/event wiring, hidden advancement resource, and duplicate-claim guard.
- Pure stage transition tests verify that failures do not advance and successful verification advances exactly one stage.
- Headless GameTests cover deterministic fixture construction that does not require a connected player.
- Manual acceptance uses `runClient`, a real logged-in player, and every journey checkpoint from start through restored completion.
- `alphaCheck` remains the automated regression gate; the Alpha Journey is the connected-player acceptance layer.
