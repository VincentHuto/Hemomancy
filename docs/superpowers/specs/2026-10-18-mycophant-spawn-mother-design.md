# Mycophant (Spawn Mother) Rebuild — Design

> The moveset was expanded on 2026-10-24: see `2026-10-24-mycophant-moveset-expansion-design.md` (position-aware picking, new moves, hold kinds, brood tumors, the Last Brood).

Date: 2026-10-18
Status: approved in conversation 2026-10-18

## 1. Intent

The Mycophant is the final boss of the Apotheos path. Today it is the old Uzouthrhix body recolored. It has no animation states, and its Nursery fight is three timed attacks (sweep, cocoon, nectar surge) whose only telegraph is a sound. This rebuild brings it up to the standard of the recent Annetta and Vesper work, covering the model, the animation and the theatrics.

**What the fight means (the user's framing).** The Mycophant is the hivemind's brood cultivator and the mother of every Erythromycelium strain. It is the gate and the key. Its job has two halves:
- absorb whoever transcends and breed new world-infecting strains from them
- tend every life form its infection has made

An Apotheos candidate reaching it is the system working as designed: the best specimen has come home to be taken in. The player does not kill a villain. They **survive being claimed**. Every assimilation attempt fails and the joining never completes, and that refusal proves the player is *the* Harbinger: the highest thing the hivemind ever produced and the heir to its will.

**Character.** A **genderless brood-mother**. Like real fungi it has no sex: motherly, never sexual, never feminine. Assimilation is reproduction in a biological sense only (spores, strains, fusion, gestation), with nothing erotic about it. Player-facing text and docs use "she/her" for the motherly register, because that is how the user refers to her. Lore text must never imply a sex. (This pronoun choice is my call; see §14.)

**Emotional arc.** Phase 1 treats the player as a stray child to bring home. At the split she starts treating them as an equal to be joined with. At the end she concedes the heir.

**Success** means playing the full ordeal end to end and seeing all of this:
- the opening reads as waking up inside her nursery
- every grab is readable before it lands and escapable through play
- the 50% split is a spectacle
- the ending reads as recognition, not an explosion
- the model matches the locked blockout from every side

## 2. Goals, non-goals, anti-goals

**Goals**
1. A new generated model matching the locked blockout (§4), with phase-2 geometry (split skull, pulp face, wings) built in.
2. Procedural Java animation driven by synced attack and state data, Annetta/Vesper style. No Blockbench keyframes.
3. A scripted opening: the dazed wake-up, her tending the brood, the notice, turn, drop, roar, and the rushing embrace.
4. Phase 1, "the claim": a moveset built around being retrieved: embrace/claim, siphon feeding, plucking, brood calls, claw rakes, and nectar pressure as the slow assimilation clock.
5. The split transition at 50%.
6. Phase 2, "the joining": a fusion embrace, wing guard, wing sweep and pulp attacks. Breaking a fusion is the main damage track.
7. A yielding ending: she lowers herself, folds her collar flat, offers the Tendril with the siphon, and sinks into the nectar.
8. A `runMycophantReviewClient` that drives every pose, attack and sequence.

**Non-goals**
- The Gardens hunt, its exposure timer, cues or config.
- Entry and return: `enterNursery`, `failAttempt`, `exitChamber`.
- Rewards and the pending-Tendril delivery contract, the Fruiting Lure and rematch rules.
- The Morphic Nursery sky theme.
- Rebalancing player manipulations or Primal Morphling abilities.
- The Mycophant Tendril item and its fungalization layer.

**Anti-goals** (failure even if it "works")
- Any reading of the assimilation as sexual, or of her as female rather than motherly.
- God/Satan framing. She is a biological endpoint, not a punishment (LORE_REFERENCE).
- A grab the player could not see coming, or one they cannot escape through play.
- Freezing the boss with `NoAI` as a "vulnerable" state.
- Vanilla particle clouds dominating focal moments, including the existing soul-fire and ash on her body (see AGENTS.md and `docs/MANIPULATION_VISUALS.md`).
- Greebled geometry. The user repeatedly asked for big forms only; seams, plating, veins and fungus belong in the texture.
- Floating, disconnected or z-fighting parts.
- A death explosion.
- Walking legs or a spider-like stance. She drags herself on her belly with her claws.

## 3. Constraints

- NeoForge 1.21.1. Registries via `DeferredHolder`. Payload networking per `PacketHandler`.
- Pure rule classes for timing and selection, JVM-tested in the style of `VesperMountControllerRules` and `LatentCombatRules`.
- **Contracts to keep green:**
  - `EndgameBossRewardResourceTest` source strings: `MycophantEntity` must not mention `mycophant_tendril`, and the manager keeps its `clear → exitChamber → givePendingTendril` ordering and pending-flag lines.
  - `ChamberThemeCommandSourceTest`.
  - The Orb of Perspective tests (`MycophantEncounterManager.isActive`).
  - `EntityLootTableCoverageResourceTest`.
  - GameTest `mycophantTendrilWaitsForInventorySpaceAcrossDeath`.
- `MycophantCombatRulesTest` pins the current cadences, node counts and pressure math. These change with the new moveset, so the test changes with them. Keep the pressure functions and hazard thresholds unless §6 changes them.
- Old saves: an owned Mycophant loaded mid-fight resumes sanely. Phase comes from health. If health is at or below 50%, the split is already complete. The state falls back to IDLE with no claim held. Nothing is inferred beyond that.
- An unowned, direct-summoned Mycophant (development/legacy) runs the new controller without the intro and without nectar pressure. The old `tickMycophantPattern` path (crimson flames, mass blind) is retired.
- Visuals: use the Hemomancy/HutosLib suite (`HemoParticleData`, `VesperVisualEffects`-style telegraphs, HutosLib tendrils, `sendClawSlash`, client geometry in the Riposte style). Vanilla particles are a deliberate fallback only.
- Texel density: 16 px per block (memory: texel density). The model renders at 2× with `TEXEL_SCALE = 2`, as the Latent infection does.
- No copper, brass or bronze accents (blood-side palette).
- Docs updated in the same change: `docs/HEMOMANCY_REFERENCE.md` (§"Mycophant Hunt and Morphic Nursery", §5.10, the bestiary row, the sound table if new sounds are added) and `docs/LORE_REFERENCE.md` (the Mycophant paragraphs, the pronoun, the yielding ending).

## 4. The model

### 4.1 Source of truth

The locked blockout is `.superpowers/brainstorm/33952-1792299911/content/spawn-mother-v8.html`. It is a three.js box model at world scale (1 unit = 1 px, 16 px = 1 block) with its poses, drag cycle, opening and phase-2 toggle. Its geometry and pivots are the reference: proportions, part placement and silhouette from front, side, rear and above. Copy that file into `tools/model_export/reference/` so it lives in the repo.

The generator is `tools/model_export/mycophant.py` on `annetta_kit`, with the hand-written animation in `mycophant_anim.java`. Its output is `MycophantModel.java` in `client/model/entity/boss/endgame/`, which replaces the Uzouthrhix model, plus textures in `textures/entity/boss/endgame/`. **Never hand-edit the generated model.** Because the renderer scales 2×, every blockout dimension is halved in model units.

### 4.2 Anatomy (bone hierarchy)

```
root
└ body                         belly-drag bob, sway, haul surge
  ├ belly                      heavy plated belly resting on the ground
  ├ claw_front_L/R             crab claws that drag her: shoulder → upper → elbow → fore → pincer(fixed, dactyl)
  ├ claw_rear_L/R              smaller second pair, same chain
  ├ sac                        the great gestation sac: tall oval, low between the claws, pushed into the belly; stalk into the chest
  ├ back_plate_0..8            nine plates on an S-curve, crest → abdomen; concave flanks; side skirts
  │  └ (plates 2 and 5) vent_trumpet_A/B
  ├ abdomen                    five masses swelling then tapering; sac cavities (2 per side); petal_vent_L/R/rear (5 petals each); tail_tendril_0..2
  └ thorax                     core, shoulder hump, shoulders, one chest plate, one egg pocket
     ├ arm_hand (L)            shoulder → upper → elbow → fore → wrist → 3 fingers (3 segments) + thumb claw (2)
     ├ arm_siphon (R)          shoulder → upper → elbow → fore → wrist → siphon chain (12) → flared mouth
     ├ wing_L/R                phase-2 hyphae lattice (scale 0 until the split)
     └ neck (3 segments)
        └ head
           ├ skull_half_L/R    the goat-like beaked bone skull, split in two; amber eye pits in uneven clusters
           ├ pulp              phase-2 Ebrietas face: many eyes, ring of hollow tubes, pulp_tentacle_0..5 (scale 0 until the split)
           ├ maw               vertical mouth; mandible_L/R part sideways; tongue chain (10)
           ├ collar            frilled-lizard collar: 2 rows of spoke→hinge plates (19 + 15) ringing the face, gap at the jaw
           └ face_tentacle_*   8: two long lateral whips, three short under the mouth, three reaching forward
```

Rules:
- Joint pivots sit at the actual joints.
- Every moving part is its own bone.
- Chains (tentacles, tongue, siphon, collar spokes, wing spars) are emitted as `ModelPart[]` arrays through `EXTRA_FIELDS`, as `latent_annetta_infection.py` does.

### 4.3 Materials and texture

- **Palette:** crimson and maroon flesh, rust secondary armor, amber/gold sacs and vent interiors, olive and chartreuse fungal seams, ivory claw tips and gripping edges, pale bone skull, muted pink-red tongue and soft tissue, near-black cavities.
- **Surface detail is painted, not modeled:** plate seams, chitin pitting, fungal shelves and trumpet fungi, veins, mottling, slime. Painters use the kit's noise.
- **Glow atlas** (`mycophant_glow.png`, an `EyesLayer`-style emissive layer): eye pits, pulp eyes, sac cavities, the chest egg pocket, vent and petal-vent interiors, the siphon mouth. Its intensity is driven by state (pulse at idle, brighter during spawning or claiming).
- **The great sac** renders in its own translucent layer (`entityTranslucent` or a custom `HemoRenderTypes` entry). The shell is translucent amber, and the egg mass and three curled embryos inside are emissive. The shell geometry lives in a separate model part rendered only by that layer.
- The existing `mycophant_awoken*.png` and `MycophantAwokenMaskLayer` are replaced. Phase 2 is shown by the geometry (split skull, pulp, wings) and the glow state, not by a texture swap.

### 4.4 Size and hitbox

The blockout stands about 7 blocks to the collar crest. The body is about 7 blocks long including the abdomen, and the claws span about 7–8 blocks wide when open.
- The entity bounding box changes from 4.0×6.0 to roughly **4.5 wide × 5.0 tall**, the builder's call.
- The abdomen and claws can extend past the box (vanilla multipart is out of scope).
- Melee reach for her attacks comes from the controller (§5), not from the box.

## 5. Controller and animation plumbing

Follow the `VesperMountController` pattern:
- One `MycophantController` owns locomotion (the drag), facing (turn-rate-limited) and the move timeline.
- Keep only `FloatGoal` and the target selectors. Remove the vanilla melee, move and stroll goals.

- **Moves** are `windup → active → recovery` timelines held in `MycophantControllerRules`. During windup a move tracks the target at a limited turn rate, then **locks its aim** at the commit tick so the player can sidestep. Higher-priority events (the split, the yield, a grief stagger) queue to the next recovery boundary or play a short authored stagger. They never hard-cut an active move.
- **Locomotion is the drag.** Diagonal claw pairs alternate reaching forward, biting into the ground and hauling. Ground speed comes in surges synced to the stance phase, not a constant glide. The pure function `MycophantMotionRules.dragPhase/haul` is shared by the server speed and the client animation.
- **Synced data** (ints/floats on `SynchedEntityData`):
  - STATE: INTRO, IDLE, DRAG, ATTACK, STAGGER, SPLIT, YIELD
  - ATTACK (ordinal+1) and ATTACK_TICK
  - STATE_TICK
  - PHASE (1/2)
  - SPLIT_TICK
  - LOCKED_AIM (xyz)
  - CLAIMED_ENTITY_ID (the player held in an embrace or fusion, or −1)
  - SIPHON_TARGET_ID
  - GLOW_LEVEL
  - INTRO_TICK
- **Client animation:** `mycophant_anim.java` defines a `Drive` with channels (body haul/bob/sway, thorax lift and pitch, neck/head, collar fold 0..1, maw open, tongue, both claw pairs as IK-free authored angles, hand grip, siphon curl/reach, wing fold/guard/sweep, split 0..1, pulp bloom, vent petals, tail and tentacle waves). It is sampled per joint with time lags, as in the Latent. `sample(...)` reads the synced state and attack and adds `partialTick`.
- **Review hooks:** static `reviewHold...` fields like Annetta's let the review client force poses.

## 6. The fight

All numbers here are starting values. They live in rules classes so they can be tuned.

### 6.1 Opening: the waking (about 10 seconds, invulnerable)

Arrival is unchanged: the existing 5-second root claim in the Gardens, then the transfer into the Nursery. After that:

| Tick | Player | Mycophant |
|---|---|---|
| 0 | Arrives lying in the nectar, **bound**: inputs ignored, movement zeroed. Client: black screen fading up to dim, view tilted about 30° and low to the ground, gentle shake. Mycelium fibers drawn across the screen. | Already risen from the central pool, **back turned**, in the TENDING pose: hand cradling a fungling, siphon feeding it, collar folded, head bowed. Soft brood glow. |
| ~60 | The view rights itself and rises to eye height as the player gets up. | Tending continues. |
| ~75 | The fibers strain and **snap** (client overlay plus a crack sound). | |
| ~85 | | **Notices**: head lifts, collar half-flares, tentacles go still. |
| ~95–120 | | **Turns** to face the player (turn-rate-limited drag pivot). |
| ~120 | | **Drops the fungling.** It falls and dissolves into the nectar: a short client prop plus amber glow and spores, no entity. |
| ~130 | **Control returns** (unbound). | |
| ~150 | Camera shake. | **Rears and roars**: thorax lifts, collar fully flared, maw wide, claws raised, wings still absent. Boss bar and music start here. Vulnerability starts here. |
| ~190 | | **Rushing Embrace**, the first real attack (§6.2). |

- The intro timeline is pure: `MycophantIntroRules` holds the beats and their ticks.
- The server sends a packet at tick 0 with the intro timeline. A client state class (the `CardinalRiteImpactClientState` pattern) runs the dim fade, camera roll and shake through `ViewportEvent.ComputeCameraAngles`, and draws a GUI overlay layer for the fibers (`FungalWhisperVignetteOverlay` is the closest precedent).
- Being bound is server-side: the player's horizontal motion is zeroed each tick and they take no damage. **Do not** apply vanilla Slowness or Darkness here; the client state carries the look.
- The arena gains a **central nectar pool** (about a 4-block radius, at most one block deep) where she rises. The existing scattered nectar spots stay. Spawn position, player arrival point and facing are tuned so she appears to rise from the pool with her back to the player, about 10–12 blocks away.
- Rematches (Fruiting Lure) play a **shortened** opening of about 4 seconds: no binding, she turns, roars and rushes. Rematches skip the dropped fungling.

### 6.2 Phase 1: the claim (100% → 50%)

She fights like a mother retrieving a stray. Menace comes from intimacy, not hatred.

| Move | Telegraph | What happens | Escape / counter |
|---|---|---|---|
| **Rushing Embrace** (signature) | Collar flares, she rears, both claws spread, a hauling surge builds. Readable at distance, aim locks at commit. | A committed lunge-drag along the locked line. Claws and arms sweep shut. On contact the player is **CLAIMED**. | Sidestep the committed line. |
| **Claimed state** | — | The player is held against the sac: pinned and moved with her. The **assimilation meter** fills over about 8 s. The sac and eye pits brighten as it rises. The hand strokes, the siphon latches. | Ordinary hits on her **both damage her and wear the hold down** over several hits (the builder decides whether to keep visible nodes or use a meter). A **blood manipulation or Primal Morphling ability that hits her**, or that is cast while the player is held, breaks it at once and staggers her about 2 s: her biggest damage window. Only one such instant break counts per hold. If the meter fills: heavy damage, a blood-volume drain and a nectar-pressure spike, then she releases the player. **Never a loss by itself.** |
| **Siphon Feed** | The siphon rears and its mouth glows. A tether line telegraph runs to the player. | The siphon latches at medium range and a tether forms. While latched it drains blood volume (or health if blood is inactive), adds pressure, and slowly reels the player in. | Break line of reach (distance or a solid block), hit the siphon (a hitbox-free check: damage to her while the tether is up plus facing), or dodge the latch. |
| **Pluck** | The hand rises, fingers open. | A short-range grab-and-toss that throws the player toward the sac and the front claws. Sets up an embrace. | Sidestep, or keep distance. |
| **Claw Rake** | One front claw lifts and draws back. | A ground rake in a cone, then a haul forward. This replaces the old sweep. | Leave the cone. |
| **Brood Call** | Locomotion stops, the abdomen pulses, the petal vents spread and the interiors brighten. | 2–3 **brood funglings** spawn from the vents. They herd: they shove the player toward her and try to pin them; they do not wander. | Kill them. Killing a brood fungling makes her **grieve**: a short stagger (about 1.5 s) where she turns her head toward it. That's an opening. |
| **Nectar Surge** | Vents puff spores, ground glow rises. | Raises pressure (an environmental beat, rarer than now). | — |

- **Nectar pressure** stays the assimilation clock. It rises per second, spikes on failures, and its hazards (Slowness tiers, engulfing drown ticks) are kept.
- **Brood funglings** are the existing `FunglingEntity`, flagged as brood. Brood flagging gives them herding AI and makes them dissolve when she yields or the fight ends. They don't drop loot. Their cap is 4 at a time.
- The move scheduler picks from weighted pools by range and phase (`MycophantControllerRules`). Same-move repeats have a cooldown.

### 6.3 The split (at 50%, about 4 seconds, invulnerable)

She stops treating the player as a child to retrieve and starts treating them as an equal to join with.

1. An authored stagger: she reels and her head jerks.
2. The goat skull **cracks down the middle** (a crack line glows first), and the halves swing out to hang off the sides of her head.
3. The **pulp face blooms** out: many eyes open, the tube ring flexes, the pulp tentacles uncurl.
4. **Wings sprout** from the shoulders, growing out of the hyphae lattice.
5. A burst pushes the player back and away. Pressure is floored at 50 (current rule). The boss bar text changes.

Her motion becomes **faster but calmer**: smoother easing, fewer feints, longer holds of eye contact. This matches the current lore line, "struggle has become recognition."

### 6.4 Phase 2: the joining (50% → 0)

The pulp's eyes track the player directly: the head yaw follows them at all times.

| Move | Telegraph | What happens | Escape / counter |
|---|---|---|---|
| **Fusion Embrace** (signature) | As the Rushing Embrace, plus the wings sweep forward to close the sides. | The lunge, then the player is held in **FUSION**: the wings wrap closed around them and the pulp presses close. The fusion meter fills faster than the phase-1 claim. | Same breaks as the claim. **Each fusion the player breaks deals a bonus chunk of damage** (start at 8% of max health) on top of the stagger window. Ordinary damage still lands at full rate, so this is a reward for the risk, not a gate. A filled meter is as in phase 1 but harsher (still nonlethal). |
| **Wing Guard** | The wings fold forward around her front. | For about 3 s she takes heavily reduced damage from the front (start at 80% reduction). | Hit from the sides or back, or break the guard with a manipulation or Primal ability that hits her. |
| **Wing Sweep** | The wings rise and draw back. | A wide frontal arc strike with knockback, which can chain into a Pluck. | Duck under it by closing in, or leave the arc. |
| **Pulp Bloom** | Pulp eyes flare in sequence, tentacles splay. | A spore burst around her head and front that applies pressure and a short blindness-like **custom** screen effect (not vanilla Blindness). | Break line of sight or leave the radius. |
| Siphon Feed, Pluck, Claw Rake, Brood Call | As in phase 1, faster. | | |

Cadences and weights are separate per-phase tables in the rules.

### 6.5 The yielding (at 0 health, about 6 seconds)

There is no death explosion and no vanilla death flip:
1. She stops. Her wings fold, and her collar folds flat back along the neck.
2. She **lowers herself** to the nectar, head bowed to the player.
3. The siphon extends toward the player and its mouth opens, offering the Tendril. **This is a visual only.** The real reward still goes through the unchanged `completeVictory` path, with its pending-claim delivery.
4. Her body **sinks into the nectar**, bowed. Amber glow, spores drifting up, sacs dimming one by one.
5. `completeVictory` runs at the end, as the current `FINAL_DEATH_TICKS` handoff does. Brood funglings dissolve.

Repeat victories play the same yielding.

### 6.6 Failure

Failure is unchanged mechanically: nonlethal expulsion through `failAttempt`. **Optional, the builder's call:** a brief client beat before the expulsion, where the screen fills amber as if taken into the sac.

## 7. Visual effects (custom suite)

Remove the vanilla `SOUL_FIRE_FLAME`, `ASH`, `SMOKE` and `DRIPPING_OBSIDIAN_TEAR` from `tickMycophantClientParticles` and `tickMycophantDeathParticles`.

| Effect | Approach |
|---|---|
| Telegraph lines, rings, cones | `VesperVisualEffects`-style helpers. Extract the shared ones into a common boss-telegraph helper rather than calling Vesper's class. |
| Siphon tether | Client geometry in the Riposte standard (`ribbonPath`/`stream`), amber core with a crimson clot. |
| Claim and fusion | A pulsing glow on the sac, HutosLib tendrils wrapping the held player, amber `glow` and `bloodCell` accents. |
| Claw rake | `sendClawSlash` plus ground fissure chips. |
| Brood call and vents | `sporiticSpore` and amber `glow` from the vent positions, which the server derives from her body yaw. |
| Pulp bloom | Spore burst plus a custom vignette overlay. |
| Split | Bone crack glow, `bloodCell` spray, a pulp light bloom. |
| Yield and sink | Rising spores, dimming glow, a nectar ripple ring. |

Particles are accents only. Geometry carries the focal moments (`docs/MANIPULATION_VISUALS.md`).

## 8. Sounds

The existing `ENTITY_MYCOPHANT_*` set is reused. New events, recorded or copied placeholders, are the builder's call to add as needed (for example the roar, the fiber snap, the skull crack and the siphon latch). Each new event gets an entry in sounds.json and in the reference doc's sound table.

## 9. Review client

`./gradlew.bat runMycophantReviewClient`, with `-PmycophantReviewOnly=<scene,...>`, is built like `annettaReviewClient`. Scenes:
- stills: front, side, rear, top, head close-up, phase-2 split head, wings guard and sweep
- every pose
- each attack forced against an invulnerable dummy
- the opening captured from the player's view
- the split transition
- the yielding

Screenshots land in `build/mycophant-client-review/`, using the operator inbox flow (`{"op":"open","name":"MycophantReview"}`).

## 10. Rules and tests

New pure classes:
- `MycophantControllerRules`: timelines, cadences, weights, turn rates
- `MycophantMotionRules`: the drag phase and haul
- `MycophantIntroRules`
- `MycophantClaimRules`: meter fill rates, break thresholds, fusion damage, the guard reduction
- `MycophantSplitRules` and `MycophantYieldRules`

`MycophantCombatRules` keeps the pressure and hazard math. JVM tests cover each rules class. Update `MycophantCombatRulesTest` for the changed cadences.

Add a source-contract test that:
- the model's generator header is present
- no vanilla `ParticleTypes` appear in the Mycophant effect paths
- `MycophantEntity` never calls `setNoAi`

Keep every contract in §3 green.

Optional loaded GameTests: an embrace claims and releases, an empowered rupture breaks a claim, the split triggers at 50% exactly once, and the yield completes victory exactly once.

## 11. Decided by the user

- The framing, the genderless brood-mother, and that the mating stays biological (§1).
- The opening beats: dazed wake, bound, fibers snap, control returns a few seconds before the rush, and the embrace is the first real attack.
- The body: belly-drag on 4 crab claws, no walking legs; a hand (left: 3 fingers and a thumb claw) and a siphon (right); a slim goat skull on a raised neck; a frilled-lizard collar; a big low sac pushed into the belly; an S-curve hump with concave flanks; big forms only.
- At 50%: the skull cracks open to reveal the Ebrietas pulp face, the halves hang off the sides, and the wings sprout. The wings are used offensively and defensively, never for flight.
- The fight arc: phase 1 is retrieval of a child, phase 2 is the joining of equals, and the ending is yielding with the Tendril offered.
- Damage: ordinary damage lands at full rate in both phases. Broken fusions add a bonus chunk on top.
- Cast breaks: a manipulation or Primal ability breaks a hold or the Wing Guard only when it hits her, or (for holds) when cast while held. One instant break per hold. This replaces today's "any cast anywhere" hook in `KnownManipulationEvents` and `MorphlingItem`.

## 12. Left to the builder

- Exact timings, damage, cadence and weight numbers within the shapes above.
- Whether the claim uses visible binding nodes or only a meter.
- The exact hitbox, and how the siphon's "hit the siphon" check is done.
- Sound placeholders.
- Painter details for the texture.
- The optional failure beat.

## 13. Out of scope / later

- Texture art beyond what the painters produce (the user may hand-paint later).
- Multipart hitboxes.
- Unstained-side content.
- A rematch-specific moveset.

## 14. Calls I made (to review)

1. Build with the Python generator and Java animation, not Blockbench (repo practice; the pasted spec said Blockbench).
2. "She/her" in player-facing text and docs, with lore never implying a sex.
3. The model renders at 2× with `TEXEL_SCALE = 2`, and the hitbox is about 4.5×5.
4. The great sac is its own translucent layer. The awoken texture swap is replaced by geometry and glow.
5. The legacy unowned pattern (crimson flames, mass blind) is retired, and unowned bosses use the new controller.
6. A central nectar pool is added to the arena for the rise.
7. Rematches get a shortened opening.
8. Brood funglings are the existing `FunglingEntity` with a brood flag, capped at 4.
9. Phase-2 fusion breaks deal a bonus 8% of max health, and Wing Guard reduces frontal damage by 80%.
10. Pulp Bloom uses a custom screen effect instead of vanilla Blindness.
11. **Arena height.** The Nursery today has a barrier ceiling at y+9 and shroomlights at y+8. That is too low for a 7-block boss that rears. `ensureArena` raises the ceiling to y+18 and moves the lights to y+16; the footprint stays 50×50. The arena is owner-bound, so existing arenas are simply rebuilt on next entry.
12. **A filled meter is never lethal.** The claim and fusion damage, and any engulfing drown tick that lands in the same second, are clamped so the player keeps at least 2 hearts. Only ordinary attacks can kill (and death stays the existing nonlethal expulsion).
13. **Unowned bosses** play the same yielding, then drop their loot table with no explosion. That keeps `EntityLootTableCoverageResourceTest` valid.
