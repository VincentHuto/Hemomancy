# Somatic Loom overnight work: rulings and deferred minors (2026-10-11)

Four plans were implemented and left uncommitted in the working tree:

- witnessed weave core
- loom tiers
- Cortical Chamber dome
- echo casts

This file lists every decision made without you (**Ruling**) and every review finding left for later (**minor (deferred)**), copied from the per-plan ledgers in `.superpowers/sdd/2026-10-11-somatic-loom-*/progress.md`.

## witnessed-weave-core

### Rulings
- Setup: Ruling: work in place on neo-1.21.1, no worktree, no commits — the user forbids auto-commits and the loom work depends on large uncommitted changes (from this and other sessions) that a worktree off HEAD would lack — cost if wrong: no per-task commit ranges; the ledger and git diff are the record.
- Task 2: Ruling: vials enlarged beyond the plan (radius .75, floor 4.4, depth 2.2) and shelf brackets moved to z -1.75/11.65 — plan values made vials ~1px and unreadable in preview; brackets would otherwise intersect the larger cradles — cost if wrong: purely cosmetic numbers.
- Task 2: Ruling: frame test keeps only the convention test (as the plan directed) — cost if wrong: none.
- Task 3: Ruling: weaverPlayer returns Player and falls back to a live reference to the paying player (weaverRef) when the UUID isn't on the server list — fixtures and fake players are never on it, and the plan's ServerPlayer-only lookup would pause every test weave forever; ThreadAttacks.perform takes Player — cost if wrong: a stale ref after relog is never used (removed check), online players still resolve by UUID.
- Task 3: Ruling: moved the knot/tether/tail helpers into client/render/world/ThreadKnotGeometry now (Task 6 Step 3) instead of deleting then recovering them — same code, fewer steps — cost if wrong: none.
- Task 3: Ruling: migration test's adopter is a FakePlayer added with level.addNewPlayer — makeMockServerPlayerInLevel trips Moonlight's login payload in this pack — cost if wrong: test-only.
- Task 3: Ruling: HarbingerRepairGameTests.selectedLoomRecipeSurvivesStockAndChargedReload now expects 19 stocked + 1 spun — payment spins enzymes per spec — cost if wrong: none.
- Task 4: Ruling: miasma test victim is a level-added FakePlayer (area harm only finds entities in the level); Lux check resets the victim's invulnerableTime after the Congeatio hit (vanilla i-frames) — test-only — cost if wrong: none.
- Task 4: Ruling: perform/jolt take Player (see Task 3 weaver ruling); markWeaveHarm still requires ServerPlayer, guarded by instanceof.
- Task 6: Ruling: windup tell is a smooth swell with hot flush by windup progress rather than the plan formula — the plan expression was malformed — cost if wrong: cosmetic. Bolt/hazard renderers written as described.
- Task 8: Ruling: echo renders an aura pass (school colour, 1.07x) plus the body, scale 0.35 weaving -> 0.75 at climax (base tier) and sinks into the fell; memory item rises into its hands at the climax; result preview hidden while weaving; echo motes from BE clientTick — fuller than the plan sketch per the user asking for care on the echo — cost if wrong: cosmetic.
- Task 8: Ruling: EchoCasts comment reworded so the coverage regex does not read it as a registration (test caught it).
- Task 9: Ruling: client recipe resolution bug (echo invisible) found in native capture, fixed via getWeaveRecipe with a source contract RED->GREEN; echo/memory/ganglion tuned from captures; alphaCheck deferred to the end-of-all-plans verification to avoid repeated full runs during concurrent sessions — cost if wrong: later discovery of a cross-suite failure.
- Final: Ruling: Ferric iron spikes (shared EntityIronSpike) can still splash a helper standing beside the weaver — the spike class is shared boss code; spikes ring the weaver at 1.5 blocks so splash needs a hugging helper — cost if wrong: helper takes 3.5 damage.
- Final: Ruling: a low-rank thread's own blow is floored for anyone it lands on, but only the weaver blacks out and pauses the weave — harmless protection for helpers, no pause griefing — cost if wrong: a helper survives a blow they'd otherwise die from.
- Final: Ruling: areaAttacksLandOnTheWeaver and lowRankThreadsBlackOutInsteadOfKilling now use the weaver as the victim (hazards spare helpers; blackout is weaver-only) — tests followed the corrected semantics — cost if wrong: none.
- Final: Ruling: mock-player weavers deactivate their blood after paying, because BloodVolumeEvents casts active-blood victims to ServerPlayer — test-fixture only — cost if wrong: none.

### Review fixes
- Final: fixed lava/campfire/hot-floor floored during low-rank weaves — lingering harm narrowed to ON_FIRE/FREEZE/MAGIC (+FALL only in a Ferric launch window, +orphan spike hits), window marked only when harm lands — lavaStillKillsDuringALowRankWeave + aMissedAttackOpensNoHarmWindow RED→GREEN, suite 22/22
- Final: fixed helpers floored/pausing the weave and hit by bolts/hazards — FrayedThreadEntity.hunts(), bolt canHitEntity/mayStrike, hazard victims filtered, markWeaveHarm weaver-only, blackout only for the weaver — helpersAreNotHuntedByBoltsOrHazards RED→GREEN, suite 22/22
- Final: fixed Ferric spike/Ductilis jolt bypassing the floor — thread attacker floors by its own rank with no window; spikes/jolt mark before they land — firstThreadHarmIsFlooredWithoutAWindow + ferricSpikesAndTheirLaunchAreFloored RED→GREEN, suite 22/22
- Final: fixed echo loop/climax casts skipped on a second client weave — loadShared resets echo bookkeeping on restart — echoBookkeepingResetsForANewWeave RED→GREEN, suite 22/22
- Final: fixed staff strain unreachable/spammable — 10-tick strike cooldown, melee PLAYER_ATTACK with direct entity only, threads dive to 2.2 blocks while winding up — staffStrikesMustBeMeleeAndRecover + SomaticWeaveRulesTest.windingUpThreadsDiveIntoStaffReach RED→GREEN, suite 22/22
- Final: fixed stale "Hold a strand ... captured distance" instruction (re-graded Important: wrong player guidance) — MemoryWeavingRedesignSourceTest assertion RED→GREEN

### Deferred minors
- Final: minor (deferred): climax ignores weaver presence; memory can drop as an item if they log out in those 3 s.
- Final: minor (deferred): legacy-orb migration ignores enzymeSpent, so an old save's unspent orbs are charged no enzyme (one-time free enzyme).
- Final: minor (deferred): client never sees awaiting-blood visuals (curRecipe null on client) — predates this work.
- Final: minor (deferred): per-frame allocations (getWeaveThreads copies, pose() arrays, getPreviewRecipe tag builds).
- Final: minor (deferred): non-memory results (blood_thrall_effigy, sanguine_formation) get no echo and HUMILIS rank.
- Final: minor (deferred): /kill cannot remove frayed threads (hurt always false).
- Final: minor (deferred): thread attack cooldown and bolt power not saved.
- Final: minor (deferred): any player can crouch-cancel/adopt a weave; last payer becomes weaver.

## tiers

### Rulings
- Task 1: Ruling: keep synaptic_node in the Cortical rite — it is craftable from ganglion_cluster (myelin_borer and siphonophore drops) + myelin_sheath and mined from Cortical Drift hubs — cost if wrong: one offering swap.
- Task 3: Ruling: stage bodies are whole per-stage models (somatic_loom, _ganglionic, _cortical) with the base glass shared at every stage, following the Forge/Scriptorium precedent rather than additive overlays — cost if wrong: three body JSONs instead of overlays (bake size only).
- Task 3: Ruling: generator exports GANGLION_RADII/FEET/TOPS and FEELER_TIPS so the renderer's ganglion glow follows the grown knot — cost if wrong: none.
- Task 4: Ruling: readyForUpgradeRite allows a chosen-but-unpaid weave (only paid blood or a live weave blocks), and the snapshot drops derived recipePath/craftPhase — the plan's !isCrafting() would refuse any loom with a recipe merely selected, since refreshRecipe sets AWAITING_BLOOD on selection — cost if wrong: a selection made before the rite is kept through it (intended).
- Task 4: Ruling: the loom refuses its own upgrade kits as catalysts with a pointer to the rite — found by directKitUseRejectsSurvivalAndWrongStations (Survival click fed the harness in as a catalyst) — cost if wrong: none.
- Task 4: Ruling: creativeKitOnALoomFillerRaisesTheMainLoom was written after CreativeStationUpgrades.tryApply was wired (Task 3) and passed first run; it pins Review Focus 5 rather than driving code — cost if wrong: none.
- Task 4: Ruling: Axonal fixture looms in SomaticLoomWeavingGameTests now sit at stage 2 (PERFECTUS needs a Cortical loom once Task 5 lands) — cost if wrong: none.
- Task 5: Ruling: a paid or live weave keeps its recipe even if its loom's stage no longer allows it (only unpaid selections are held back) — a paid weave must never strand blood; stage can only drop via Creative/commands — cost if wrong: a creative-downgraded loom finishes one refined weave.
- Task 5: Ruling: non-memory results (effigy, formation) count as Humilis for the rank gate — they carry no manipulation rank — cost if wrong: none.
- Task 5: Ruling: the staff's ranged hook uses its own look-raycast (FrayedThreadEntity.lookedAtSlackThread) on both sides, so the client does not start a blood channel at a thread; base reach is 4.5 blocks (beyond vanilla entity reach) and Ganglionic 8 — cost if wrong: base staffs gain 1.5 blocks of hook reach.
- Task 6: Ruling: tuned from native captures — Cortical echo alpha 0.42-0.8 and wash 0.34-0.5 (plan said 0.6-0.95 / full colour; it read as a solid golden statue, not a memory); aura colour mixes ASH by stage so the base echo stays grey; feelers thickened to read at range — cost if wrong: values only.
- Task 7: Ruling: fixed stale orb-dragging prose found while documenting tiers (reference §25.7 schema note, enzyme storage note, recipe-type table, Canon Memory line) and documented the review's narrowed floor rules in §25.7 step 8 — docs must match Plan 1's shipped behaviour — cost if wrong: none.
- Task 7: Ruling: staff ranged hook moved into LivingStaffItem.hookLookedAtThread so startUsingItem still precedes the server-only utility branch (LivingStaffUtilitySourceTest ordering contract) — cost if wrong: none.

### Review fixes
- Final: fixed Critical empty-hand cycling stuck on the first stage-refused recipe (Blue Ice group unreachable on base looms) — chooseNextRecipe records the refused id (held back by refreshRecipe, refused at payment) and answers tier_required on the action bar — cyclingStepsPastMemoriesTheLoomCannotWeave RED (reached []) → GREEN, loom suite 33/33, station suite 43/43
- Final: fixed Liber pages claiming the loom keeps its contents when moved (inserted items drop, enzymes are lost) and the Cortical page omitting "finish or cancel any paid weave" (re-graded Important: the rite silently refuses) — StationUpgradeRecipeResourceTest.loomLessonsTellTheTruthAboutMovingAndPaidWeaves RED → GREEN
- Final: fixed tier_required feedback flooding chat every channel tick (re-graded Important) — action bar; pinned by MemoryWeavingRedesignSourceTest contract written after the one-token fix (no RED observed)

### Deferred minors
- Final: minor (deferred): ClearLoomStatePacket/clearTendency ignores riteLocked and paid weaves (no client sender; crafted packet only; rite fails safe).
- Final: minor (deferred): staff ranged hook ignores block occlusion (can hook through walls within 8 blocks).
- Final: minor (deferred): client/server hook disagreement at the edge of reach; hookLookedAtThread ignores tryHook's result.
- Final: minor (deferred): kit-as-catalyst refusal is hard-coded English (no lang key).
- Final: minor (deferred): "twice as far" wording (4.5 → 8 blocks is ~1.8×) in Liber/wiki.
- Final: minor (deferred): drawFeelers flares for any slack thread in range, including another loom's.
- Final: minor (deferred): planned helpers maxRankOrdinal/firstRankNeedingStage not added (unused).
- Final: minor (deferred): no full-rite test with a chosen-but-unpaid selection held through the rite.

## cortical-dome

### Rulings
- Task 1: Ruling: added ChamberOfWillManager.skyThemeOf(UUID), a read-only accessor, instead of getChamberState (which computeIfAbsent-creates a Chamber record for every weaver) — cost if wrong: one extra method on a shared manager.
- Task 3: Ruling: renders at AFTER_CUTOUT_BLOCKS (plan text said so) with the Vesper-floor model-view convention (pose from event.getModelViewMatrix(), model-view stack identity) and skyDistance 48 (not 128) so short render distances do not far-clip the sky — cost if wrong: theme layers sit nearer the camera (camera-centred, so parallax-free either way).
- Task 3: Ruling: looms are tracked from SomaticLoomRenderer (client) rather than the block entity's clientTick, so no client class is referenced from common code; one-frame latency — cost if wrong: none.
- Task 3: Ruling: from inside a dome, ground beyond the dome radius gives way to Chamber void (a ray leaves the sphere before reaching it) — this is the portal's honest geometry and reads as the Chamber's floating refuge; kept rather than adding a ground plane — cost if wrong: an art-direction change.
- Task 3: Ruling: the portal source contract was written after the renderer (GL rendering has no headless test); the native capture is the real evidence — cost if wrong: none.
- Final: Ruling: single outer try/finally now owns the model-view pop (reviewer minor 4, folded into the depth restructure rather than a separate change) — cost if wrong: none.

### Review fixes
- Final: fixed the dome hiding threads 7-12 blocks out behind the far-wall depth while they still attack — DOME_RADIUS 13 (> RITUAL_RADIUS 12) — SomaticLoomEffectRulesTest.theDomeHoldsTheWholeRitualSoNoThreadHidesBeyondIt RED → GREEN; native capture loom-4/6 re-reviewed
- Final: fixed depth ALWAYS breaking self-occluding 3D theme geometry — inside the stencil the far plane is written first (glDepthRange(1,1)), then the theme draws with its own LEQUAL — ModelFirstTileRendererResourceTest dome contract RED → GREEN; native capture re-reviewed
- Final: fixed missing stencil-availability guard (shader-pack targets / first frame after enableStencil painting sky full-screen) — enable then skip a frame; skip when the bound draw framebuffer is not the main target — same contract RED → GREEN

### Deferred minors
- Final: minor (deferred): theme fallback reuses the failed theme's PoseStack.
- Final: minor (deferred): no frustum check before the per-dome renderSky; sphere is immediate-mode rather than a cached VertexBuffer.
- Final: minor (deferred): Lowtide-theme dome inside the Chamber would rebuild its OBJ VBO per skyDistance.
- Final: minor (deferred): pre-change mid-weave saves have no weaverTheme until the next weave (default sky).
- Final: minor (deferred): depth-disabled draws (see-through name tags) beyond the dome still show over the sky.
- Final: minor (deferred): first enableStencil mid-frame flashes one frame (shared with NonEuclideanHallway/VisceralMirror); enabling once at client setup would fix all three.

## echo-casts

### Rulings
- Task 1: Ruling: EchoContext carries the echo's absolute `figure` (0.42 base .. 1.0 Cortical) instead of the old base-relative `scale`, because live cues are sized for a full-size caster — cost if wrong: one multiplier.
- Task 1: Ruling: dummies are AbstractArrow stand-ins (blood shots, forged needles, coronation swords) flown with noPhysics/noGravity on a fixed line; iron constructs are NOT used as dummies (unowned constructs spawn vanilla particles) — Ferric constructs use FERRIC_CONJURE/FERRIC_IMPACT/LODESTONE cue geometry instead — cost if wrong: rampart/spikes read as cue bursts rather than solid iron.
- Task 2-4: Ruling: one file per school pair (Animus, Mortem, Ductilis, Ferric, LuxUmbra, Thermal) instead of the plan's per-task batching, with a shared EchoMoves helper — cost if wrong: file layout only.
- Task 2-4: Ruling: body-skin forms (WOUND, HUNGER, GRAVE, ROT_INFECTION, FROZEN_VEINS, BONE, RIMEBOUND, CAUTERIZE) are sent as entity-less world bursts — their skin layers need a live entity, the world cue still draws — cost if wrong: those casts show only their world geometry.
- Task 5: Ruling: EchoDummies drives each stand-in's flight absolutely (start + velocity*age, motion and rotation set each tick) — the first gallery showed client-only arrows never move on their own (they wait for server lerps), so BloodProjectileEffects lodged them in place — cost if wrong: none.
- Task 5: Ruling: area radii capped to ~2-3 blocks at life size (bell, bloom, ignitions, cryogenic pulse, hour, stillness, furnace, nerve pulse, beacon, well, clouds, lodestone, magnet, aneurysm) after the gallery showed life-size casts flooding a 7-block dome — cost if wrong: casts read smaller than their live versions.
- Task 5: Ruling: PHOENIX wing geometry does not scale with radius and still spans wide at life size; kept (Perfectus phoenix_debt is a focal moment) — cost if wrong: wings overhang the Cortical dome briefly.

### Review fixes
- Final: fixed a lagging resync replaying the same echo beat (backward weaveTicks reset echoLastLoop) — reset only when the beat goes backward or the weave is not running — aLaggingResyncNeverReplaysTheSameBeat RED → GREEN, loom suite 34/34
- Final: fixed the first cast beat running unguarded inside the block-entity render pass — EchoCasts.releaseSafely defers it a tick with a try/catch — EchoCastCoverageTest.theLoomReleasesCastsOffTheRenderPassAndSafely RED → GREEN
- Final: fixed sanguine_marionette's tether never drawing (MARIONETTE_TETHER needs live body and caster ids) — a LIVING blood flow from the hands to the body — castsAvoidFormsThatOnlyDrawBetweenLiveEntities RED → GREEN

### Deferred minors
- Final: minor (deferred): dummies may step without interpolation (setOldPosAndRot after the arrow's own client tick).
- Final: minor (deferred): chargeThen cues are unkeyed and briefly double-draw; final charge(...,0) and riposte/coronation ticks-0 pre-sends are no-ops.
- Final: minor (deferred): umbral step/reversal comment says forward/back, code moves sideways.
