# Annetta Encounter Combat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (user preference: native, one final reviewer).

**Goal:** Build both Annetta routes end to end: dialogue starts, Phase 1 combat, the scripted break and rise,
Priestess flight combat, the purge, Latent Infection combat, the ally AI, the resolution, and every animation.

**Spec:** `docs/superpowers/specs/2026-10-16-annetta-encounter-combat-design.md`

## Global Constraints

- Server owns timers. Each entity syncs `action` + `actionTick`, and scenes sync `sceneTick`. Models animate from
  `tick + partial`.
- No vanilla particle clouds: use `HemoParticleData` (bloodCell, glow, sporiticSpore, hitGlow) and
  `PacketHandler.sendClawSlash`.
- Keep the literals that `AnnettaCanonSourceTest` asserts:
  - `releaseSpecimen` before `clearSpecimen(jarStack)`
  - `if (!server.addFreshEntity(infection))` before `brewStack.shrink(1)`
  - `if (!server.addFreshEntity(priestess))` before `this.discard()`
  - `.orElse(false) && !isHarbinger(player)`
  - `type == EntityInit.tooth_pecks.get()`
- Keep the existing rewards and the severance unlock.
- Keep the review-harness hooks `setEncounterState(EncounterState)` and `setAction(int,int)`, and update the harness to match.

## Review Focus

1. Annetta can never die in Phase 1 before the break. Lethal damage clamps to 1 HP.
2. The break starts only when both conditions hold: 2400 ticks AND 50 damage.
3. A Priestess saved mid-flight reloads into a sane mode without floating forever. A reload while emerging
   resumes or finishes the emerge.
4. The flying Priestess never clips into the church ceiling (a headroom raycast clamps hover height).
5. Annetta in the ally phase is invulnerable and never targetable by players. The infection can stagger her but
   never kill her.

## Tasks

1. **Rules + JVM tests:**
   - `AnnettaEncounterRules`: break, guard, timelines, pools, Phase 1 choice.
   - `PriestessCombatRules`: modes, emerge, hover point, swoop.
   - `LatentCombatRules`: choice, emerge.
2. **Pool entity:** `AnnettaPoolEntity` (infected, humor, reanimation; growth; effects) with its renderer,
   textures and registration.
3. **Bolt entity:** `InfectedBoltEntity` (blood or spore) with a tendril-knot renderer and registration.
4. **Annetta:** dialogue-driven starts, Phase 1 AI, break scene, purge scene, ally AI and resolution. Add the
   `annetta_` dialogue event branch, the trees and the lang keys.
5. **Priestess:** emerge, hover, swoop, landed combos, shield, wind blast, volley and lance throw. Sync the wing
   pose and growth into the tendril layer.
6. **Latent Infection:** emerge, whip lash, spear thrust, claw rake, bloom, root eruption, turning on Annetta,
   and death.
7. **Animations:** Annetta, Priestess and Latent `setupAnim` timelines from the scripts. Add the Annetta blood
   creep layer, the purge hair switch and headless rendering.
8. **Review harness:** live AI scenes against a dummy target, the forced break and rise, and the forced purge.
   Iterate from screenshots.
9. **Finish:** docs, the full test suite, one final reviewer.
