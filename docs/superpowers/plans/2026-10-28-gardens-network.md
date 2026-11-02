# Gardens Network & Immune Response (Section 2b) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** The Gardens' network hears footsteps, the Apotheos senses what stands on it, puffballs burst, and antibodies hunt the over-autonomous.

**Spec:** `docs/superpowers/specs/2026-10-28-gardens-network-design.md`

## Global Constraints
Follow AGENTS.md:
- session maps go through `TransientServerStateEvents` (clear plus logout);
- particles only through `HemoParticleData`;
- lang keys, not literal text;
- no client classes under `common/`;
- every mixin listed in `hemomancy.mixins.json`;
- frozen fixtures updated for the antibody and its egg;
- no commits.

## Review Focus
1. A sneaking player, or one standing still, never raises the alarm. Covered by the footfall rule tests.
2. An Apotheos never raises the alarm and is never flagged by blood. Covered by rule tests.
3. Antibodies never attack anyone but their target, and never persist after it dies or leaves. Covered by a GameTest on retargeting and expiry.
4. Puffballs outside the Gardens are unchanged. Covered by the outcome rule test.
5. No session map leaks: logout and clear are registered. Covered by `SessionStateRegistryTest`.

### Task 1: Rules (JVM)
`common/world/GardensNetworkRules`:
- `double footfall(boolean sneaking, boolean onFungalGround, double moveSqr)`
- `boolean shouldAlarm(double walked, long ticksSinceAlarm)`, using 8 blocks and 100 ticks
- `float castSuspicion(boolean apotheos)`, giving 2 or 1
- `float bloodSuspicionPerSecond(boolean apotheos, double volume, double max)`, giving 0.25 above 75% for non-Apotheos, else 0
- `int antibodiesFor(float suspicion)`: 0 below 12, else `min(4, 2 + (int)((s-12)/6))`
- `boolean canSummon(long ticksSinceSummon)`, using 600 ticks
- `float decayPer10s()` = 1
- `boolean senses(boolean apotheos, boolean inDimension, boolean onFungalGround, double distSqr)`, within 32 blocks
- `PuffballOutcome puffball(boolean apotheos, boolean inGardens)`: NONE, ABSORB or BURST

Write the tests first: `GardensNetworkRulesTest`, with a table per method (RED → GREEN).

### Task 2: Server systems
- `common/world/GardensNetwork`: a static `Map<UUID, Trace>` (walked, lastAlarm, lastPos, suspicion, lastSummon), `tick(ServerPlayer)`, `onCast(ServerPlayer)`, `clearSessionState()`, `logout(Player)`.
- `common/event/GardensNetworkEvents`: a `PlayerTickEvent.Post` subscriber that calls `tick`.
- `KnownManipulationEvents.onManipulationUsed` calls `GardensNetwork.onCast`.
- Register in `TransientServerStateEvents`.
- `PuffballFungusBlock.entityInside` uses `puffball()`.
- Lang keys.
- Verify with `SessionStateRegistry`, `RegistrationStyle` and `HardCodedEnglish`.

### Task 3: Antibody
- `common/entity/mob/monster/AntibodyEntity`: flying, a target UUID, a lifetime, goals, and `summonFor(ServerPlayer, int count)`.
- `EntityInit` entry and attributes, egg, lang, empty loot table, egg model.
- Generated texture.
- Client: `AntibodyModel` (hand-written), `AntibodyRenderer`, layer and renderer registration.
- Frozen fixtures.
- GameTest: antibody constructible, retargets to its summoner, and discards when the summoner is gone.

### Task 4: Apotheos sense
- `mixin/core/MixinApotheosSense` on `Minecraft#shouldEntityAppearGlowing`. It delegates to `client/render/world/ApotheosSense.sees(Entity)`, which uses `GardensNetworkRules.senses`.
- List it in the mixin config.
- Verify with `SourceTreeHygiene` and `MixinConfigResource`.

### Task 5: Docs, verification, review
Update HEMOMANCY_REFERENCE and the wiki. Run the full `test`, `build` and GameTests, then the final review.
