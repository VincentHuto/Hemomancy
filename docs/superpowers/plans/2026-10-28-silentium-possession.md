# Silentium Possession (Section 4a) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans.

**Goal:** the possession rite, Concentration, forms, the echo, return and ejection, and Mind Broken.
**Spec:** `docs/superpowers/specs/2026-10-28-silentium-possession-design.md`

## Global Constraints
- **AGENTS.md:**
  - attachments are read through HemoCapabilityAccess;
  - owner sync goes through the PlayerStateSync table;
  - a payload bumps PROTOCOL_VERSION to 19 and rewrites payload-registry.txt;
  - lang keys only;
  - particles through HemoParticleData;
  - no client classes in common;
  - GUI layers are registered in GuiLayerRegistrar;
  - FlightArbiter gets a new Source.
- A new rite updates CardinalRitePurposeDataTest PURPOSES, HarbingerRecipeMapDefinitions and lang.
- Frozen fixtures.
- No commits.

## Review Focus
1. No item is lost or duplicated across start, return, ejection, logout or crash recovery. Covered by `PossessionInventoryRules` tests and code review.
2. A possessed player never takes health damage or dies, and never stays possessed outside the Gardens.
3. The surrogate is always released or killed, never left stilled forever (unloaded chunk at return).
4. Mind Broken can't be milked away. Covered by a GameTest.
5. Rite refusals hold: not Silentium, Mind Broken, already possessing, no surrogate. Covered by rule tests.

### Task 1: Rules (JVM)
`common/capability/player/harbinger/possession/PossessionRules` and `SurrogateForm`:
- `SurrogateForm` enum: pool, speed, jump, flight, keepsInventory, wearsStandGear, waterHurts, abilityCooldown, and `fromEntityTypeId(String)`;
- `hitCost(float damage)`;
- `Hit absorb(int shell, float concentration, float cost)`, giving shell, concentration and broken;
- `mayBegin(boolean silentium, boolean mindBroken, boolean active)`;
- `regenPer100(boolean ambergris)`;
- constants `MIND_BROKEN_TICKS=6000`, `RETURN_HOLD_TICKS=40`, `SHELL_HITS=3`.

Test: `PossessionRulesTest`.

### Task 2: State and sync
- `Possession` attachment (INBTSerializable): active, form, concentration, max, shell, regen, totem, surrogate UUID, echo UUID, return point, saved inventory ListTag, and a dirty flag.
- HemoAttachmentTypes, HemoCapabilityAccess and the PlayerStateSync entry.
- `PacketSyncPossession` (sent to the player and trackers) and its client cache.
- The payload registry and protocol bump.

### Task 3: Mind Broken and the echo
- `MindBrokenEffect` (no cures, attack damage −2), registered in EffectInit.
- The WillAmbushDirector multiplier.
- `FungalWhisperEvents.triggerWhisper`.
- `PossessionEchoEntity` (no hitbox) + `PossessionEchoRenderer` (PlayerModel with the owner's skin, dark translucent), registered.
- GameTest `mindBrokenSurvivesMilk`.

### Task 4: The rite and the service
- `PossessionService`: `begin(ctx)`, `returnClean`, `eject`, `recover`.
- The rite JSON, a CardinalRiteCompletionEffects registration with refusal, the recipe map, lang and the purpose test.
- The `thread_of_will` item (use: ability or hold-to-return).
- FlightArbiter `POSSESSION`.

### Task 5: Events and client
`PossessionEvents`:
- damage → Concentration, death cancel;
- tick: regen, water drain, dimension check, the spider climb on the client;
- logout/login;
- toss guard;
- start tracking.

Client:
- `PossessionClient`: replaces the render with the facsimile, and does the spider climb;
- `ConcentrationOverlay` layer.

### Task 6: Docs, verification, review
