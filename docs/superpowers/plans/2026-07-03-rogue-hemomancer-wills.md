# Rogue Hemomancer Wills Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Design spec:** [2026-07-02-rogue-hemomancer-wills-design.md](../specs/2026-07-02-rogue-hemomancer-wills-design.md) — read it first; this plan implements it and does not re-argue decisions. Sections referenced as *spec §N*.

**Goal:** Ship the Wills ambusher system: one data-driven `WillEntity` with Broken/Sent origins and school-keyed manipulation combat; a degree-scaled ambush director with sanctuary exclusion and a pre-materialization telegraph; school-keyed dissolve loot; the Blood Drunk Puppeteer reintegration; and the Archon-gated bend layer (Absorb / Redirect / Commandeer).

**Architecture:** Combat math, spawn eligibility, composition, and bend rules live in pure, unit-testable `*Rules` classes (the `EndgameBossCombatRules` pattern). The entity is one type with synched `ORIGIN / SCHOOL / TIER / PHASE` data. Casting reuses the existing per-manipulation `DrudgeAction` behaviors by widening their caster parameter — Wills get ~40 school behaviors for free. The ambush director is a server `PlayerTickEvent.Post` system (the `FungalWhisperEvents.onPlayerTick` pattern). The telegraph is a tracked anchor entity that all clients receive but only Oculiflora-wearing clients *render* — no per-player packet filtering needed, because scar state is already client-synced.

**Tech Stack:** NeoForge 1.21.1, Java 21, `PathfinderMob` + goal AI, synched entity data, `HemoServerConfig` section pattern, `HemoAttachmentTypes` player attachments, payload packets via `PacketHandler`, HutosLib/monolith shader render types, focused resource/source tests.

---

## Current-state anchors (verified 2026-07-03)

| Concern | Anchor |
|---|---|
| Manipulation-casting mobs | `common/manipulation/DrudgeAction.java` — functional interface `(drudge, world, centre, radius) -> boolean`, assigned per-manipulation via `.setDrudgeAction(...)` in `ManipulationInit` (~40 behaviors) |
| Hostile mob home | `common/entity/mob/monster/` (e.g. `BloodDrunkPuppeteerEntity.java`) — Wills go in `common/entity/mob/monster/will/` |
| Player degree | `IInitiatoryDegree` via `HemoCapabilityAccess` |
| Whisper scheduler (herald hook) | `common/entity/npc/dialogue/FungalWhisperEvents.onPlayerTick(PlayerTickEvent.Post)` + `FungalWhisperDialogueTrees` |
| Sanctuaries | `common/event/worldevent/FoundingFaneSavedData.java` + `FaneFootprint`; `common/worldgen/ChamberOfWillManager`; outpost check pattern: `StructureManager#getStructureWithPieceAt` for `hemomancy:harbinger_outpost` (as used by `HarbingerRecruitmentRules`) |
| Spawn amplifiers | `common/event/worldevent/BloodMoonSavedData` / `BloodMoonEvents`; `QliphothBloomSavedData` (bloom center + 3-chunk radius); fungal block tags; Blood Drunkenness effect (`EffectInit`) |
| Testable-rules pattern | `EndgameBossActions` + `EndgameBossCombatRules` (+ `EndgameBossMusicHandler`) |
| Commandeer economy | `common/summon/PuppeteerSummonRules` (owner-scan active cap), `PuppeteerSummonDefinition(s)`, `PuppeteerSummonFactory`, Marionette Crossbar thread charge/upkeep/tether |
| Client cue pattern | `client/screen/overlay/FungalWhisperVignetteOverlay.java` |
| Registration/rendering | `EntityInit` (+ attributes), `ClientEvents.renderEntities`, `LayerEvents`, `SoundInit`(-equivalent), `data/hemomancy/loot_table/entities/` |
| Oculiflora dependency | *Not yet implemented* — defined in [fungal-scar spec §5.3](../specs/2026-07-02-fungal-scar-consolidation-design.md). This plan lands the anchor + audio cue for everyone first; the scar-only render gate activates when the scar ships (Task 6 Step 4 is written to no-op safely until then). |

## Phase map (build order)

- **Phase A — the enemy exists:** Tasks 1–4 (rules, entity, casting, registration/render). Exit: a `/summon`-spawned Will fights a player with school manipulations.
- **Phase B — the world sends it:** Tasks 5–7 (sanctuaries, director, telegraph + cues). Exit: organic ambushes with telegraph, cooldowns, and sanctuary immunity.
- **Phase C — it matters:** Tasks 8–9 (loot economy, Puppeteer reintegration). Exit: killing Wills feeds progression; Puppeteer reads as a Will.
- **Phase D — it can be claimed:** Task 10 (falter + Absorb/Redirect/Commandeer). Exit: Archon+ players bend Broken Wills; Sent Wills bite back.
- **Phase E — closeout:** Tasks 11–12 (config/balance defaults, tests, docs, smoke script).

---

## File Structure

- Create `common/entity/mob/monster/will/WillEntity.java` — the mob (synched ORIGIN/SCHOOL/TIER/PHASE, goal wiring, dissolve death).
- Create `common/entity/mob/monster/will/WillAnchorEntity.java` — pre-materialization marker (no AI, no collision, timed).
- Create `common/entity/mob/monster/will/goal/WillDriftApproachGoal.java`, `WillCastGoal.java`, `WillFalterGoal.java` — AI.
- Create `common/entity/mob/monster/will/WillCombatRules.java` — pure: tier→fixed Broken stats, player→scaled Sent stats, school kit selection, cast cadence, falter thresholds.
- Create `common/entity/mob/monster/will/WillCompositionRules.java` — pure: degree→tier, pack composition (Faded count, Proctor presence), multiplayer target choice.
- Create `common/entity/mob/monster/will/WillSanctuaryRules.java` — pure core + thin level-facing wrapper.
- Create `common/entity/mob/monster/will/WillBendRules.java` — pure: bend eligibility (origin/phase/degree), verb costs, backfire outcomes.
- Create `common/event/WillAmbushDirector.java` — server tick system: eligibility, site scoring, anchor spawn, cooldowns, herald/blood-moon/drunkenness modifiers.
- Create `common/manipulation/MobManipCaster.java` — bridge that invokes a manipulation's `DrudgeAction` with a generic mob caster (see Task 3 for the widening strategy).
- Create `client/render/entity/mob/will/WillRenderer.java` + `WillModel.java` (+ `WillAnchorRenderer.java`) — flicker/translucent Broken pass, solid hive-marked Sent pass, anchor outline.
- Create `client/screen/overlay/WillPresenceOverlay.java` — the non-scar warning pulse.
- Create `common/network/will/WillPresenceCuePacket.java` — server→client cue on materialization.
- Modify `common/init/EntityInit.java` (2 entity types + attributes + spawn eggs), `ClientEvents.renderEntities`, `LayerEvents`, sound-event init, `HemoServerConfig` (new `wills` section), `HemoAttachmentTypes` (per-player ambush state), `ManipulationInit` (only if Task 3 widening requires signature touch-ups), `FungalWhisperEvents` (herald flag), `ItemInit` (`faded_memory`).
- Add resources: `data/hemomancy/loot_table/entities/will_broken.json` (baseline; school bonuses in code), entity tag `data/hemomancy/tags/entity_type/wills.json`, `sounds.json` entries + placeholder `.ogg`s, lang, textures `textures/entity/will/`, `faded_memory` item model/texture.
- Tests: `WillCombatRulesTest`, `WillCompositionRulesTest`, `WillSanctuaryRulesTest`, `WillBendRulesTest`, `WillResourceTest`.
- Docs at closeout: `HEMOMANCY_REFERENCE.md` (§26 mobs + new §system), `LORE_REFERENCE.md` (two origins, Puppeteer retcon, commandeering).

---

### Task 1: Pure rules first (`WillCombatRules`, `WillCompositionRules`)

**Files:** create both rules classes + their tests.

- [ ] **Step 1 — `WillCombatRules`.** Encode spec §5/§6 as data: `brokenStats(tier)` returns fixed HP/damage/speed per tier (I: 20/3, II: 30/5, III: 34/6, IV: 38/6 — *fixed forever; never reads the player*). `sentStats(playerDegree, playerMaxHealth)` returns scaled values (baseline ~1.2× player-proportional HP, tuned later). `schoolKit(EnumBloodTendency, tier)` returns an ordered list of manipulation ids per school/tier (Tier I: 1–2 Humilis; II: +Mediocritas; III: school combos; IV: +Summa) — start with 3 manipulations per school from the registered catalog (e.g. Flammeus: `sanguine_ignition`, `scalding_updraft`, `vitric_combustion`; Tenebris: `void_shroud`, `umbral_step`, `blood_eclipse`; Mortem: `hemorrhage`, `exsanguinate`, `bloom_of_rot`). `counterSchool(playerDominantTendency)` for Sent Wills (fire→ice etc., a fixed 8-entry map). `falterFraction()` = 0.25, `falterWindowTicks()` = 100.
- [ ] **Step 2 — `WillCompositionRules`.** `tierFor(degree, qliphothCommunionDone)` per spec §5 gates (pre-D4 → empty). `compose(tier, random)` → record of `(brokenCount, brokenTier, sentPresent)` implementing spec §6: Tier I: 1 Faded; II: 1–2 Faded; III: 1 Sent + 0–1 Faded; IV: 1 Sent + 2–3 Tier-II Faded (fodder escort). `chooseTarget(List<PlayerSnapshot>)` → highest degree (spec §8 multiplayer rule); take a snapshot record, not `Player`, for testability.
- [ ] **Step 3 — Tests.** `WillCombatRulesTest`: fixed Broken stats identical across player inputs; Sent stats monotonic in degree; every school kit id exists in `ManipulationInit` (registry-name check); counter map is total over the 8 tendencies. `WillCompositionRulesTest`: degree gating exact at boundaries (D3 empty, D4 Tier I, D7+communion Tier IV); Tier IV always includes escort fodder; target choice picks max degree.
- [ ] **Step 4 — Gate.** `./gradlew compileJava && ./gradlew test --tests "*WillC*"`.

### Task 2: `WillEntity` core

**Files:** create `WillEntity`, modify `EntityInit`.

- [ ] **Step 1 — Entity skeleton.** `WillEntity extends PathfinderMob` (hostile, MONSTER category). Synched data: `ORIGIN` (byte: 0 broken / 1 sent), `SCHOOL` (byte ordinal of `EnumBloodTendency`), `TIER` (byte 1–4), `PHASE` (byte: 0 DRIFTING / 1 MATERIALIZED / 2 FALTERING / 3 DISSOLVING), `TARGET_UUID` (optional). Persist all to NBT. `finalizeSpawn` applies `WillCombatRules` attributes by origin (Broken: fixed by TIER; Sent: scaled from the stored target player at spawn time — snapshot once, don't live-track).
- [ ] **Step 2 — Phase semantics.** DRIFTING: no collision damage dealt/taken beyond 50% (semi-incorporeal: `isInvulnerableTo` physical partial — implement as damage ×0.35 taken and no attacks), moves toward target, invisible-ish (client handles visuals). MATERIALIZED: normal combat. FALTERING: AI stalls (Task 10 arms bends here); auto-recovers to MATERIALIZED after the window if not bent. DISSOLVING: 40-tick death spectacle then discard (the boss delayed-death pattern), fire loot hook (Task 8).
- [ ] **Step 3 — Registration.** `EntityInit`: register `will` + attributes supplier (baseline; per-instance overrides in `finalizeSpawn`) + creative spawn egg (testing affordance, matching repo convention). Add both `will` and `blood_drunk_puppeteer` to new tag `data/hemomancy/tags/entity_type/wills.json`.
- [ ] **Step 4 — Gate.** `./gradlew compileJava`; `runClient`: `/summon hemomancy:will` yields a (temporarily cube-rendered) mob that chases and melees. Melee is the pre-Task-3 fallback attack — keep a weak melee goal permanently as the out-of-blood fallback.

### Task 3: Manipulation casting via `DrudgeAction` widening

The single highest-leverage step: Wills inherit ~40 authored behaviors.

**Files:** modify `common/manipulation/DrudgeAction.java`, create `common/manipulation/MobManipCaster.java`, create `goal/WillCastGoal.java`; touch `ManipulationInit` lambdas only where compilation demands.

- [ ] **Step 1 — Widen the caster parameter.** Change `DrudgeAction`'s first parameter type from `DrudgeEntity` to `PathfinderMob` (keep the interface name — call sites are lambdas and mostly use position/level/targeting). Compile. For each lambda that used Drudge-specific members (expected: `vital_reservoir` refilling drudge charge, possibly `sanguine_mending`/ally checks), add an `instanceof DrudgeEntity d` guard preserving old behavior and a sensible mob fallback (e.g. `vital_reservoir` for a Will: restore health instead of charge). **Document each guarded site in the PR description.**
- [ ] **Step 2 — `MobManipCaster`.** Static helper: `boolean cast(PathfinderMob caster, ResourceLocation manipId, int radius)` → looks up the manipulation, invokes its action with the caster's position, returns success. No blood accounting for mobs (their budget is cadence, not mL) — cadence lives in `WillCombatRules`.
- [ ] **Step 3 — `WillCastGoal`.** While MATERIALIZED with a living target: every `castIntervalTicks(tier)` (from `WillCombatRules`, ~60–100t), pick the next kit entry (cycle for Broken — *perseveration*, spec §3.1; priority-pick for Sent: mobility when player far, burst when close — spec §3.2). Face target, cast via `MobManipCaster`, play cast sound. Broken Wills occasionally (15%) *stall* one interval (muscle-memory skip).
- [ ] **Step 4 — Gate.** `./gradlew compileJava && ./gradlew test` (Drudge tests must stay green — the widening must not change Drudge behavior). `runClient`: summoned Flammeus Will visibly casts ignition/updraft/combustion at the player; Drudge still executes an installed memory normally.

### Task 4: Rendering, sound, and the two looks

**Files:** create `WillModel`, `WillRenderer`; modify `ClientEvents.renderEntities`, `LayerEvents`, sound init + `sounds.json`, lang; textures under `textures/entity/will/`.

- [ ] **Step 1 — Model.** One humanoid-ish `WillModel` (start from `BloodDrunkPuppeteerEntity`'s model class as the donor skeleton). Two texture pairs: `will_broken.png` (desaturated, school-tint overlay strip) and `will_sent.png` (solid, fungal-marked) + emissive overlay `will_sent_marks.png`.
- [ ] **Step 2 — Renderer.** Broken: translucent render type + per-tick alpha flicker (sine + hash jitter; spike the flicker when PHASE=FALTERING) and school tint from `SCHOOL` (the 8 tendency particle colors already defined in the tendency system). DRIFTING renders at ~35% alpha for *everyone* (it is hard to see, not invisible — the scar advantage is the *anchor pre-spawn*, not this). Sent: opaque pass + emissive mark layer (`LayerEvents` layer, half-health gate like `VesperEveningStarLinesLayer`). Player-mirroring silhouette for Sent is **deferred stretch** — note in docs, do not block.
- [ ] **Step 3 — Sounds.** Register `will_ambient_broken` (fragmented whisper loop), `will_materialize`, `will_falter`, `will_dissolve`, `will_presence_sting` with placeholder `.ogg`s (repo convention: copied/edited existing whisper/boss sounds until final audio). Wire ambient/hurt/death overrides in `WillEntity`.
- [ ] **Step 4 — Gate.** `runClient`: Broken Will flickers with school tint; Sent Will renders solid with marks; sounds fire on spawn/death.

### Task 5: Sanctuary rules

**Files:** create `WillSanctuaryRules` + test.

- [ ] **Step 1 — Pure core.** `boolean isSanctuary(boolean insideFane, boolean inChamberDimension, boolean insideOutpostPiece)` → trivial OR, testable. Thin wrapper `isSanctuary(ServerLevel, BlockPos)` composes: `FoundingFaneSavedData` footprint test (reuse the fane-membership lookup that Blood Routing's fane mode uses), `ChamberOfWillManager` dimension identity, and `StructureManager#getStructureWithPieceAt(pos, <harbinger_outpost>)` (the `HarbingerRecruitmentRules` fallback pattern).
- [ ] **Step 2 — Boundary behavior (spec §14 Q3 decision).** Director never *spawns* into sanctuary; already-spawned Wills entering a fane get PHASE=DISSOLVING after 60 ticks inside (pursue to the boundary, dissolve past it — resolves the open question toward "sanctuary is real safety").
- [ ] **Step 3 — Test + gate.** `WillSanctuaryRulesTest` on the pure core; `./gradlew test --tests "*Sanctuary*"`.

### Task 6: The Ambush Director + telegraph anchor

**Files:** create `WillAmbushDirector`, `WillAnchorEntity`, `WillAnchorRenderer`, `WillPresenceCuePacket`, `WillPresenceOverlay`; modify `HemoAttachmentTypes`, `FungalWhisperEvents`, `EntityInit`, `PacketHandler`.

- [ ] **Step 1 — Player ambush state attachment.** New attachment `WillAmbushState`: `lastAmbushGameTime` (long), `heraldUntilGameTime` (long), `hiveAttention` (int, Task 10 backfire feeds it). Register in `HemoAttachmentTypes`.
- [ ] **Step 2 — Director loop.** Static `@SubscribeEvent` on `PlayerTickEvent.Post`, server-side, every 200 ticks per player: skip unless `willsEnabled`, degree ≥ 4, not creative/spectator, cooldown elapsed (`ambushCooldownTicks`, default 20 min), active Wills targeting this player < `maxActivePerPlayer` (default 3), and `!WillSanctuaryRules.isSanctuary(here)`. Compute spawn chance = `baseChancePerCheck` × terrain multiplier (fungal block tags underfoot/nearby, Qliphoth bloom radius via `QliphothBloomSavedData`, Fungal Dimension id) × Blood Moon multiplier (`BloodMoonSavedData` active) × Blood Drunkenness multiplier (effect amplifier) × herald multiplier (if `heraldUntilGameTime` in future). Roll once.
- [ ] **Step 3 — Herald hook.** In `FungalWhisperEvents`, after a whisper fires for a player, set `heraldUntilGameTime = now + 2400` on the attachment (the whisper becomes the omen, spec §7). One-line touch; keep the whisper system otherwise untouched.
- [ ] **Step 4 — Anchor + materialization.** On a successful roll: `WillCompositionRules.compose(...)`, pick a spawn site 12–24 blocks out (prefer light ≤ 7, out of the target's view cone, on solid ground; 8 attempts then abort silently). Spawn `WillAnchorEntity` (no AI/collision, `anchorLifetimeTicks` default 80) storing the pending composition. On expiry, replace with the composed `WillEntity` group in DRIFTING phase, send `WillPresenceCuePacket` to players within 48 blocks. **Anchor rendering:** `WillAnchorRenderer` draws the faint through-terrain outline **only if the local client player has the Oculiflora scar equipped** (client reads its own synced `SCARS` capability); until the scar exists in `ScarInit`, the lookup no-ops and the anchor renders nothing — the system ships scar-ready (spec §9).
- [ ] **Step 5 — Non-scar cue.** `WillPresenceCuePacket` → play `will_presence_sting` + flash `WillPresenceOverlay` (a 40-tick vignette pulse; clone the `FungalWhisperVignetteOverlay` structure). This is the universal fallback warning (spec §9 guardrail: the scar is the edge, not the entry fee).
- [ ] **Step 6 — Gate.** `runClient` with `baseChancePerCheck` cranked in a test config: stand on erythrocytic mycelium at D4+ (use the degree debug command) → sting + vignette → flickering Faded materializes and attacks; walk into a Fane → no ambushes, pursuing Will dissolves at the boundary.

### Task 7: Multiplayer + cadence hardening

- [ ] **Step 1:** Director targets via `WillCompositionRules.chooseTarget` over eligible nearby players (highest degree); composition scales `brokenCount +1` per extra player within 32 blocks (cap +2).
- [ ] **Step 2:** Global concurrency guard: never more than `maxActivePerDimension` (default 8) director-spawned Wills per dimension; command/egg spawns exempt.
- [ ] **Step 3:** Persist `WillAmbushState` across relog (attachment serializer) so cooldowns cannot be reset by rejoining. Test by relogging in `runClient`.

### Task 8: Rewards — dissolve loot

**Files:** create `will_broken.json` loot table; modify `WillEntity.dropCustomDeathLoot`, `ItemInit` (`faded_memory`), lang/model/texture.

- [ ] **Step 1 — School-keyed drops in code.** On DISSOLVING completion (not vanilla death — Wills never leave a corpse): roll from `WillCombatRules.lootFor(school, tier, origin)`: 1× matching enzyme (`vivacious_enzyme`…`umbral_enzyme` by school) at 80%; matching crude memory shard (where the school has one of the 10 starters) at 25%; `faded_memory` at 8% (Broken only). Sent Wills drop the enzyme + increment the target's `hiveAttention` "ripeness" counter (attachment; flavor-only for now — spec §14 Q2 resolved as *track it, gate nothing yet*).
- [ ] **Step 2 — `faded_memory` item.** Register (rare, stack 16), tooltip: *"A fragment of someone who did not survive knowing."* Wire as an accepted Somatic Loom **catalyst candidate** only (no recipes yet — recipes are follow-up content; the item existing unblocks the loot table).
- [ ] **Step 3 — Gate.** Kill 20 summoned Wills of two schools in `runClient`; verify drop mix and no vanilla corpse-loot leakage.

### Task 9: Blood Drunk Puppeteer reintegration

**Files:** modify spawn composition, dialogue-inquiry JSONs, entity tag (done in Task 2), `LORE_REFERENCE.md` note (closeout).

- [ ] **Step 1:** `WillCompositionRules` Tier II+: 20% chance one Faded slot is filled by `BloodDrunkPuppeteerEntity` instead of a `WillEntity` (it already fights; it now *arrives* like a Will — spawned by the director post-anchor). No changes to its own class beyond membership in the `wills` entity tag.
- [ ] **Step 2:** Item-inquiry lore: add `dialogue_inquiry/vicar/hemomancy/puppeteering_thread.json` + `mnemonist/.../faded_memory.json` entries reframing thread as "the hive's strings" and the Puppeteer as a Blood-Drunk Broken Will (data-only, follows `ItemInquiryLoader` schema).
- [ ] **Step 3:** Gate: composition test updated; `runClient` Tier-II ambush occasionally includes a Puppeteer.

### Task 10: The bend layer — Absorb / Redirect / Commandeer

**Files:** create `WillBendRules` + test; modify `WillEntity` (falter + interaction), `WillFalterGoal`; small hooks in the Marionette Crossbar item for the Commandeer path.

- [ ] **Step 1 — Falter.** In `WillEntity.hurt`: Broken Will crossing below `falterFraction` → PHASE=FALTERING for `falterWindowTicks`; flicker spike; AI frozen (`WillFalterGoal` supersedes others). Recover to MATERIALIZED afterward. Sent Wills **never** falter.
- [ ] **Step 2 — `WillBendRules` (pure).** `BendVerb resolve(origin, phase, playerDegree, heldItemKind)`: FALTERING + Broken + degree ≥ 7 + empty/staff hand → ABSORB (plain use) or REDIRECT (sneak-use); + Marionette Crossbar in hand → COMMANDEER. Sent or non-faltering → BACKFIRE. Costs: Redirect 400 blood; Commandeer 56 thread from the crossbar + counts against the puppeteer active-summon cap (`PuppeteerSummonRules` owner scan). Backfire: apply Blood Drunkenness II 60s + Hematic Strain 30s + `hiveAttention += 2`. Encode all as returned data; no side effects in the rules class.
- [ ] **Step 3 — Absorb.** `mobInteract` path: consume the Will (DISSOLVING immediately, no loot roll), grant: +alignment in its school (reuse the tendency-gain path manipulation casts use, ~3 units), 50% one matching enzyme, 15% `faded_memory`. Play a drain visual (reuse the `bloodDrainConfig` HutosLib tendril preset used by exsanguinate).
- [ ] **Step 4 — Redirect.** Set `redirectedOwner` UUID + `redirectUntilGameTime` (+90s). Targeting goals invert: attack hostiles and other Wills, never the owner's team; on expiry → DISSOLVING (standard loot). No cap interaction, no persistence (redirect does not survive unload — deliberate).
- [ ] **Step 5 — Commandeer.** Convert in place to OWNED state: store owner UUID + crossbar UUID (the crossbar already carries a stable UUID for summon binding); the Will now follows the puppeteer tether contract — red thread render to owner, upkeep drain from crossbar thread per minute (reuse the summon upkeep path via `PuppeteerSummonFactory`/rules where the contract is generic; where it is summon-definition-specific, mirror the drain in a small `WillOwnedRules` and **note the duplication for a later unification pass**), unravel on unpaid upkeep or unequipped crossbar (5s grace, same as summons). Claimed Wills count in the owner's active-summon cap scan. Silent-Archon edge (spec §11.3): if `archon_choice_made == "silent"`, Commandeer thread cost ×0.5 and cap +1.
- [ ] **Step 6 — Tests + gate.** `WillBendRulesTest`: verb resolution truth table (origin × phase × degree × held item), backfire on Sent always, cost numbers stable. `runClient`: full loop — weaken a Faded, Absorb one, Redirect one against a second Will, Commandeer one and watch upkeep/tether/unravel; attempt on a Sent Will → backfire effects land.

### Task 11: Config — the `wills` section (`HemoServerConfig`)

- [ ] **Step 1:** Add keys with these defaults (spec §13 "frequency is load-bearing" — conservative): `willsEnabled=true`, `ambushCheckIntervalTicks=200`, `baseChancePerCheck=0.02`, `ambushCooldownTicks=24000` (20 min), `maxActivePerPlayer=3`, `maxActivePerDimension=8`, `terrainMultiplier=3.0`, `bloodMoonMultiplier=2.0`, `bloodDrunkennessMultiplierPerAmplifier=0.5` (additive), `heraldMultiplier=4.0`, `anchorLifetimeTicks=80`, `falterWindowTicks=100`, `bendEnabled=true`, `commandeerEnabled=true`, `claimedWillBonusCapSilentArchon=1`, `puppeteerSpawnChance=0.2`, `minDegree=4`.
- [ ] **Step 2:** Every director/bend read goes through config (no literals in the event handler). Gate: config file generates with the section; toggling `willsEnabled=false` silences the director in `runClient`.

### Task 12: Tests, docs, and closeout

- [ ] **Step 1 — `WillResourceTest`.** Entity registrations + attributes present; `wills` entity tag contains both types; loot table parses; all school-kit manipulation ids resolve; sounds.json entries have files; lang keys for entity names, `faded_memory`, overlay text.
- [ ] **Step 2 — Full gate.** `./gradlew build` green (all focused tests + existing Drudge suite unchanged).
- [ ] **Step 3 — Docs.** `HEMOMANCY_REFERENCE.md`: new "Wills" subsection (system summary, config table, entity entries in §26, `faded_memory` in items); `LORE_REFERENCE.md`: the two origins, the silence→Wills relationship, the Blood Drunk Puppeteer retcon, commandeering as Archon-craft; update the design spec's status line to `partial — implemented per plan`.
- [ ] **Step 4 — Smoke script (manual, record results in PR).** (1) D3 player: zero ambushes in 30 min on fungal terrain. (2) D4: Tier-I Faded appears after cooldown; vignette+sting fire. (3) D6 + post-whisper: elevated frequency observed. (4) D7: Tier-IV Proctor + Faded escort; Proctor counters the player's dominant school. (5) Fane interior: no spawns; pursuing Will dissolves at boundary. (6) Bend loop: Absorb/Redirect/Commandeer + Sent backfire. (7) Blood Moon night: visibly more pressure. (8) Second player joins: target = higher degree; +1 escort.

## Sequencing, dependencies, risks

- **Strict order:** Task 1 → 2 → 3 → 4, then 5 → 6 → 7, then 8/9 (parallel), 10, 11 alongside 6+, 12 last. Nothing outside Task 3 touches shared systems; Task 3's `DrudgeAction` widening is the one cross-system risk — its gate (Drudge tests green) is mandatory before proceeding.
- **Oculiflora:** intentionally *not* a dependency. Task 6 ships the anchor scar-ready with a no-op render gate; the fungal-scar consolidation delivers the scar and flips the gate on. Cross-reference both PRs.
- **Deferred by design:** Sent-Will player-mirroring silhouette; Faded Memory loom recipes; ripeness gating anything; boss-bar/music treatment for Tier IV; the "Claimed Will decay" open question (spec §14 Q7 — decide after first balance pass).
- **Kill-switch philosophy:** every phase lands behind `willsEnabled` + its own sub-flag (`bendEnabled`, `commandeerEnabled`), so alpha testers can run the enemy without the bend layer or disable the system wholesale without a rebuild.
