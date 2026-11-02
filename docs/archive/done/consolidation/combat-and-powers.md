# Combat events, attacks, and powers

[Stage 6](stage-6-combat/README.md) traces the current attack, incoming, Pre, Post, and death boundaries with representative armor, capability, and manipulation powers. Use that event-order sheet before moving any damage or reward handler.

For the phase-by-phase list of player and living-entity subscribers that read capability state, see [Entity and player event phases](entity-and-player-event-phases.md).

## Current paths

- [SchoolCombatEvents](../../src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java) works across projectile launch, attack, incoming damage, final damage, knockback, effect removal, ticks, tracking, and death. It uses [SchoolDamage](../../src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolDamage.java), [SchoolHitContext](../../src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolHitContext.java), and state helpers for attribution and reactions.
- [ArmorSetBonusHandler](../../src/main/java/com/vincenthuto/hemomancy/common/event/ArmorSetBonusHandler.java) has its own equipment, damage dealt, and damage taken hooks. Other powers live in capability event classes, manipulation implementations, and feature managers.
- [ManipulationInit](../../src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java) owns manipulation entries and metadata. [ManipulationChannelManager](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationChannelManager.java) owns continuous casts; [CastingAnimationManager](../../src/main/java/com/vincenthuto/hemomancy/common/manipulation/animation/CastingAnimationManager.java) coordinates casting pose state.

## Overlap and target contract

Write each offensive or defensive power as a sequence: **trigger → eligibility → cost/cooldown → damage or state change → visual/audio cue → sync/cleanup**. The reusable part is often eligibility or cost accounting, while effect behavior should stay explicit. An attack context must preserve source entity, owner, damage school, direct/indirect status, and child reaction identity. Do not flatten these into a generic `hurt` call.

Damage event phase matters: pre-damage adjustment, post-reduction payoff, and death/cleanup have different information. Document subscriber priority and cancellation behavior before moving a handler. `SchoolCombatEvents` is a likely coordination point for school reactions; armor set effects can remain separate while sharing small attribution and cost helpers.

## First migration slice

1. Trace one attack from input/cast to damage source, event handlers, status effects, and visuals. Record its exact ordering.
2. Identify two powers with the same eligibility or cost logic and extract only that shared rule.
3. Keep reaction bookkeeping and cleanup together; check that chained attacks cannot re-trigger a parent payoff unintentionally.
4. Verify the affected attack in gameplay with direct hits, projectiles where applicable, armor, and a second player. A source-level match alone cannot prove event ordering.

**Avoid:** one universal power event class, moving handlers solely to shorten files, or combining phases with different semantics.
