# Cardinal Rite audit: process, effects, and what to change (2026-10-12)

Read-only audit of the Cardinal Rite system: the server lifecycle, the client effects for active rites, the 69 rite recipes, and the systems around them (Unstained rites, brazier mini-rites, persistent rite zones). Nothing was changed in code.

Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/` or `docs/`. Abbreviations:

| Short | File |
|---|---|
| HCRE | `common/rite/harbinger/HarbingerCardinalRiteEvents.java` |
| ACR | `common/rite/ActiveCardinalRite.java` |
| OE | `common/rite/harbinger/CardinalRiteOrdealEngine.java` |
| IH | `common/rite/harbinger/CardinalRiteInteractionHandler.java` |
| BCKP | `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` |
| UCRE | `common/rite/unstained/UnstainedCardinalRiteEvents.java` |
| BR | `client/render/world/CardinalRiteBoundaryRenderer.java` |
| FCR | `client/render/world/CardinalFloorConsecrationRenderer.java` |

**Verified** marks findings re-checked against the source after the audit. The others carry the auditing pass's file and line references and should be confirmed before they are changed.

## Rite merge stages 2–4 (2026-10-18)

Implemented in the working tree, uncommitted: one purpose-grouped Rites & Crafts Liber map and per-purpose JEI categories; special floors for Apotheos (the Hush), the Bloom of the Qliphoth (the inverted tree) and Ancestral Communion (the lineage); lasting rite spaces over completed floors with `hemomancy:rite_bound/*` tags. Spec: `docs/superpowers/specs/2026-10-18-rite-merge-stages-2-4-design.md`. Per-floor ceremony mechanics remain out of scope.

## Status after the cleanup pass (2026-10-12, overnight)

Implemented in the working tree, uncommitted. Nothing has been checked in a live client yet; visual tuning needs an in-game pass.

Verification:
- Main, test and GameTest sources compile; the floor membrane shaders pass `glslangValidator`.
- JVM `test`: 2,939 tests, the same 16 unrelated failures as before (model generators, mojibake, missing lang/textures, legacy suite count).
- `runGameTestServer`: 935 tests, only the known `blockItemExceptionsKeepTheirOwners` failure.
- `runClinicalGameTestServer`: 131 tests, only the two known journey-snapshot failures.
- `runLivingWeaponGameTest`: 10/10. `runSuccessionGameTestServer`: 21/22; `legacyVialsAreRejectedBeforeConsumption` reads a brazier at a fixed offset that the 2026-10-12 floor-socket redesign moved, so its fixture needs updating.
- Two independent read-only reviews (server engine, client renderers) found issues that are now fixed: a no-ordeal rite sealing straight into its finale skipped the finale refusal; a failed Succession was reported as completed; lethal backlash could end a rite twice mid-loop (now one-shot `markEnded` plus snapshot iteration); an abandoned rite could hand the staff into an observational Chamber visit copy; finale holds waited a full still interval; transient maps were never cleared; unknown failure profiles now fail at parse; long beat styles could kick a player; a GLSL `pow` of a negative base; client rite memories not cleared on logout/reload; beats aging while paused; daemon culling ignoring its strands.
- Known follow-up: the floor and staff-melt render types are rebuilt once per frame per Focus (their key includes game time); a mutable per-Focus type would remove that churn.

**Fixed:** every bug in §2 except #14 being reworked (an abandoned rite now ends cleanly and its staff goes to the recovery queue) and #16/#17 handled client-side.
- #1 `second_fraction` anchors moved to ring 1; a data test forbids stacked ring slots.
- #2, #3, #18 Unstained gates live in preflight; Pale Consecration and Lethe Covenant tick every dimension; Lethe wins over Fervor in one spawn handler.
- #4 Exsanguination drains a name-tagged creature into its anchors and needs 200 mL before sealing.
- #5 `covenant_pressure` drains every anchor until the Seal is traced.
- #6 the checklist no longer messages; #7 planted staff uses the owner's fitting; #8 `failRite` follows the failure profile; #9 the pool total only reaches the caster and allies; #10 the helix is client-side and animated; #11 Unstained rites no longer drain blood, show the blood helix or the daemon; #12 Dominion's 25% discount is wired; #13 finale refusals hold the rite before any offering is consumed; #15 station rites can always be recalled; #19 deaths in any dimension end the rite, and a caster may only run one rite across dimensions.

**Unified (§3):** one termination path (`CardinalRiteTermination`); completion effects and their refusals in a registry (`CardinalRiteCompletionEffects`), checked at activation, before the finale and at completion; rank targets come from the recipe; data-driven `wave_count` and `culmination`; one zone model (`common/rite/zone`) with a sync packet and renderer; one `BrazierRite` pipeline and `consumeForRite`; one ash-trail base class without redstone output; shared rite math, heartbeat, lightning palette and quad helpers; typed client phases; cached fog, floor mesh, render types and boundary layout.

**Localization:** rite names and descriptions now read `hemomancy.rite.<rite>.name/.desc` with the JSON text as fallback; keys exist for all 69 rites and the 9 stale ones were refreshed.

**Effects (§6.2):** rite beats (`CardinalRiteEventPacket`) drive the floor membrane, boundary, fog and daemon; layered daemon geometry; offerings drawn along vessels; five culmination styles; choreographed collapse, break, completion and cancel endings; wave telegraphs; `rite.*` sound events.

**World stage (2026-10-13):** each milestone pushes the world around a rite a step further (fog, trench, braziers, creep, sky wound, blood rain, sound bed), and the top panel is off by default with owner-only action-bar guidance. Design: `docs/superpowers/specs/2026-10-13-cardinal-rite-world-escalation-design.md`.

**Rite purposes and Working crafts (2026-10-14):** every Harbinger rite declares a purpose (`self`/`world`/`making`/`bond`) that decides its floor style; 17 rites were rehomed. Twenty machines moved from floorless Blood Structures to Working crafts (`cardinal_rite/working/`, phase `SUBJECT_FEEDING`), structure offerings were removed, and offering escrow is shared through `RiteOfferingEscrow`. Design: `docs/superpowers/specs/2026-10-14-rite-purposes-and-working-crafts-design.md`.

**Ring gating (2026-10-13):** later rings wake per phase; see HEMOMANCY_REFERENCE "Rings wake one phase at a time". Design: `docs/superpowers/specs/2026-10-13-cardinal-rite-ring-gating-design.md`.

**Gameplay (§6.1):** graded instability (strained anchors bleed, rupturing empowers threats); a clean rite with an awakened Reservoir returns half its anchor blood (plain rites keep their no-refund contract); staggered offering draws; finale holds; wave decks cycle their authored pool.

**Not done:** a `RiteVariant` interface for station/succession/trial (termination covers their cleanup, but their tick special cases remain); floor-style ceremony archetypes and sigil result modifiers beyond the Reservoir refund; the participatory (absorption-driven) procession; `MapCodec.encode` still drops ceremony data; Liber entries for the uncovered rites (a lore choice of chapter per rite); Unstained vanilla particle sweep and a stillness ceremony (deferred by request).

---

## 1. Summary

The consecrated floor (membrane, trench, Focus pad) is the most polished part of the rite, but nothing that happens during a rite affects it. The rite's own layers are much weaker:

- rings, sockets, sigil vessels, staff tendrils and the finale flash are flat-colour quads on vanilla `RENDERTYPE_LIGHTNING_SHADER`;
- the daemon, the centrepiece, is a server-sent particle cloud, and its renderer draws nothing;
- completion is the same for every rite: `ENDERMAN_SCREAM`, `BEACON_ACTIVATE` and a burst at the caster;
- failure and collapse have no visuals of their own: every boundary layer disappears at once.

In play, rites are front-loaded and back-loaded:

- 39 of the 52 Harbinger ceremonies have no ordeal. The loop is: fill 4–8 anchors, project into the daemon, then watch the procession and culmination, during which the player does nothing.
- 30 of the 52 use `safe_retry`, so failing costs nothing.
- Instability between 0 and 100 has no effect.
- 16 rites play identically, and four D6 rites (archon, covenant_vigil, eternal_covenant, pallid_shadow) differ only in their offerings.

Structurally, five lifecycles share `ActiveCardinalRite`: legacy/Unstained, interactive, puppeteer trial, station upgrade and succession. They are told apart by special-case checks scattered through the code. Completion is dispatched through two chains of string `if` statements, 23 branches long for Harbinger rites and 17 for Unstained. The same cleanup bundle is pasted in nine places, and eight persistent-zone SavedData classes are written separately.

Unstained rites run on the Harbinger LEGACY path. As a result they show the red blood-cell helix and the blood dispersal, and leaving the circle drains the caster's blood. They have no HUD, and every effect they do have uses vanilla particles. This contradicts their still-water tone.

---

## 2. Bugs to fix first

| # | Finding | Where | Status |
|---|---|---|---|
| 1 | **`second_fraction` soft-lock.** Its 8 anchors are all on ring 0, with order 0–7. `CardinalRiteRingTuning.anchor` uses `order % 4`, so anchors 4–7 sit on the same positions as 0–3. `nearestAnchorIndex` breaks ties toward the first index, and a full anchor returns `handled(0)`. Anchors 4–7 can never be filled, so consecration never finishes. This blocks the D6 Centrifuge upgrade. The other tier-2 station rites use rings 0 and 1 correctly. | `src/main/resources/data/hemomancy/recipe/cardinal_rite/second_fraction*.json`; `common/rite/CardinalRiteRingTuning.java:25`; `common/rite/harbinger/CardinalRiteTargetGeometry.java:43`; IH:113 | Verified |
| 2 | **Unstained rites run even when their gate refuses.** Silver Veil (purity ≥25), Lethean Tide (purity, active Blood Moon), Silver Dawn and Severed Covenant check their gates inside the effect handler, which simply returns. `completeRite` still returns `true`, so the pattern breaks and the result item drops with no effect. | UCRE:115-190, 296-305, 565, 807, 817, 238 | Verified (Silver Veil) |
| 3 | **Pale Consecration and Lethe Covenant only work in the Overworld.** Both level-tick handlers return early unless the level is the Overworld, so zones cast in the Nether or End never sear hostiles or grow purity. | `common/rite/unstained/PaleConsecrationEvents.java:43`; `LetheCovenantEvents.java:47` | Verified |
| 4 | **Exsanguination never sacrifices anything.** Sacrifice processing is gated to `LEGACY`, but Exsanguination is an interactive ceremony, so it produces Sanguine Quintessence with no sacrifice. Its description and the reference doc ("sole passive-sacrifice rite") are wrong. | HCRE:318-320 | Verified |
| 5 | **The `covenant_pressure` wave is not implemented.** It is used by Apotheos and Bloom of the Qliphoth but has no Java handler. It falls through to spawning Bloodlickers and shows the wrong cue, "A response sigil is demanded". | OE:313, 331; `apotheos_rite.json`, `bloom_of_qliphoth.json` | Verified (no handler) |
| 6 | **Chat spam during Inscription.** `buildChecklist` calls `sealMediumReady`, which sends a chat message. The checklist is rebuilt on every sync, so a wrong or missing medium repeats the message about every 10 ticks. | HCRE:955 → IH:655-666 | Verified |
| 7 | **The planted staff shows the viewer's skin.** It uses `staffVisualFor(mc.player)`, so everyone sees their own staff fitting on someone else's rite. It should use the rite owner. | BR:166-167 | Verified |
| 8 | **`failRite` ignores `failureProfile`.** It always deals 10 damage and knocks the caster back, even for `safe_retry` rites. `collapseInteractiveRite` does respect the profile. | HCRE:761-792 versus HCRE:1135+ | Verified |
| 9 | **The caster's bloodline pool volume is sent to every player in the dimension**, because the sync packet is broadcast to all of them. | HCRE:843-854, 123-126 | Verified |
| 10 | **The helix never rotates in interactive rites.** `elapsed = totalTicks - remainingTicks` is always 0 because `remainingTicks` only decrements in LEGACY. | HCRE:477 | Verified |
| 11 | **Unstained rites use blood-path presentation and penalties.** Leaving the circle drains blood. They show the red helix and the humanity dispersal, and they have no HUD (the overlay hides LEGACY and Unstained rites). | HCRE:280-293, 327, 373; `CardinalRiteOverlay:23` | Verified |
| 12 | **Sanguine Dominion's manipulation discount is dead.** `isInOwnDomain` has no callers; only the Bloom discount is wired into `ManipulationCostLedger`. | `SanguineDominionEvents:88` | Verified |
| 13 | **Late completion gates fire after the offerings are gone.** If a helper leaves, a Bloom overlaps, Rooted Vein is obstructed, or Scarlet Summons finds no member online or not enough pool blood, the rite "falls silent" after the procession has already consumed the offerings. | HCRE:1246-1303 | Reported |
| 14 | **A disconnect or dimension change leaves a COLLAPSED rite in place.** If the caster returns outside the leash, they take leash damage until they walk back in, and then the collapse backlash hits. | HCRE:193-211, 280-294, 353-361 | Reported |
| 15 | **A station rite cannot be cancelled while its structure is invalid or the caster is outside the leash.** The loop `continue`s before the cancellation check. | HCRE:256-258 | Reported |
| 16 | **Joining or changing dimension replays every seal at once.** Each completed segment re-grows, and all the heartbeat and charge sounds fire together. | `ActiveRiteClientData:189-192, 498-506`; BR:217-235 | Reported |
| 17 | **Rings drift off their sockets during the procession.** Ring rotation follows absolute game time, while the sockets stay fixed. | `CardinalRiteCompletionRingTheatrics:30`; BR:318 | Reported |
| 18 | **Unstained spawn hooks conflict.** Fervor (SUCCEED) and Lethe (FAIL 50%) are copies of each other with opposite results at the same priority. Where zones overlap, the result depends on handler order. | `SanguineFervorEvents:42-64`; `LetheCovenantEvents:74-96` | Reported |
| 19 | **A death in another dimension is never cleaned up,** and parallel rites can run in different dimensions, because saved data is per level. | HCRE:405; BCKP:347 | Reported |

---

## 3. Unification

### 3.1 Server lifecycle

- **One `terminate(rite, outcome)` method.** The bundle of `clearThreats`, recover, restore staff, return allies, discard sprites and succession cleanup is pasted in at least nine places: HCRE:197-206, 220-231, 375-377, 383-392, 411-417, 625-627, 765/797, 1137/1180, 1511-1512. Staff return has three separate paths: `CardinalRiteStaffEscrow.restore`, `StationUpgradeRites.releaseStaff` and a raw `ItemEntity` drop at HCRE:202.
- **A `RiteVariant` hook interface** with `prepare`, `tick`, `complete`, `recover`, `cleanup` and `clientPhase`. Station upgrade, succession and puppeteer trial would each implement it, replacing about 12 `StationUpgradeRites.isRite` checks and 6 `SuccessionRites.is` checks across HCRE, ACR, OE and BCKP. Each variant would own its state rather than storing raw `upgrade` and `succession` CompoundTags on every rite.
- **A `RiteEffect` registry in place of the string chains**, with methods `canComplete(ctx)`, `complete(ctx)` and `culmination()`, covering HCRE:1371-1457 and UCRE:115-190.
  - Run `canComplete` at activation, again before the offering procession, and once more at completion. This fixes bugs #2 and #13.
  - This also merges the gates now duplicated between BCKP:355-465 and HCRE:1246-1297, whose path-gate messages already differ, and the two Founding Fane checks, `canStartFoundingFane` and `canConsecrateFoundingFane`.
- **Put the rank-up target in JSON.** The target is defined twice, in `RecipeDegreeGates.RANKUP_RITE_TARGET_DEGREES` and `HCRE.DEGREE_RITE_PATHS`. Change `"rankup": true` to `"rankup": 5`.
- **Route all brazier offering consumption through one method.** There are five ways today, and they disagree on whether the fire goes out: `BrazierItemAbsorptionRite:50`, OE:114, `StationUpgradeRites:109-116`, `SuccessionRites:197-201` and `BloodStructureCraftingHelper:301-307`. Replace them with `IronBrazierBlockEntity.consumeForRite(extinguish)`.
- **Split the god class.** HCRE (2,523 lines) has three methods of about 255 lines each (`onLevelTick`, `completeRite`, and `tryStartCardinalRite` in BCKP). Move client snapshot building (HCRE:840-1133) into a `CardinalRiteClientSnapshot` class and the effects into the registry. What is left is the tick orchestration.

### 3.2 Data and schema

**Make the wave count data-driven.** It is hard-coded by degree at BCKP:536-545: D5 → 1, D6 → 3, D7 → 4–6, otherwise 0. Meanwhile `CardinalRiteCeremonyRules.fullWaveCount` is unused and `guaranteed_waves` is empty in all 52 rites.

**Fields that are parsed but have no effect.** Either implement or remove each:

- `profile` (simple/standard/ceremonial) and `abbreviated`;
- `layout`, which is ignored whenever `anchors` is present (all 12 station rites);
- `focus`, which the policy forces to `living_staff` anyway;
- `helper_roles`, which never restricts anything;
- `target_duration_ticks`, which is UI-only;
- `required_purity`, which no rite uses.

**Dead rules in `CardinalRiteCeremonyRules`**, used only by tests: `fullWaveCount`, `upfrontBloodCost`, `allyQuota`, `anchorDecayPerSecond`, `NPC_RESERVE_ML` and `BLOODSPENT_RECOVERY_TICKS`.

**Rite names and descriptions are literal JSON strings.** The existing `hemomancy.rite.*.name` (13) and `.desc` (9) lang keys are never read. Move names and descriptions to lang keys.

**`id` is load-bearing but present in only 13 files.** Without it, the serializer falls back to `cardinal_rite/unknown`, and the Apotheos and station-rite exemptions depend on it. Derive `id` from the recipe location instead.

**`MapCodec.encode` loses data.** It drops `ceremony`, `floor`, `brazier_signature`, `required_structure` and `pattern`, so a codec round-trip of any Harbinger rite fails.

**Form/floor mismatches:**
- `riteType` does not match the floor tier for founding_fane (greater rite on a grand floor) and hematic_unbinding (greater rite on a lesser floor).
- Pattern size does not match the form for lethean_tide, silver_dawn and severed_covenant.
- 13 of the 17 Unstained patterns contain unused keys.

### 3.3 Persistent rite zones

Eight hand-written SavedData classes do the same job: StillWaters, PaleConsecration, LetheCovenant, SanguineFervor, SanguineDominion, CrimsonLodge, QliphothBloom and CrimsonBeacon. They have the same load and save loops, and the containment test is written 9 times. Two pairs are byte-for-byte equivalent apart from names: StillWaters and PaleConsecration, and LetheCovenant and SanguineFervor.

**Proposal:** one `RiteZone` holding `{owner, center, dim, blockRadius, expiry?, kind}`, with effect hooks for tick, damage and spawn. One dispatcher would tick every level, which fixes bug #3, and spawn rules would be resolved explicitly by priority, which fixes bug #18. One sync packet and one renderer would make zones visible: today only Qliphoth Bloom has any client view, and every other zone disappears after its chat line.

### 3.4 Brazier mini-rites

Memory, Scar and Living Weapon Graft are each hand-chained from `BrazierBlock.absorbBloodFromBlock`. Between them they repeat:

- `messageEverySecond` three times;
- the raw lit check four times, although `BrazierBlock.isLit` exists;
- Scar's validation three times.

**Proposal:** a `BrazierRite` interface and registry with match, preflight, completion and VFX methods. The item-drawn-out visual has two implementations: the bespoke packet, which goes only to the channelling player, and vanilla `ITEM` particles in the procession. Use the bespoke packet for both and broadcast it. `RITUAL_PHASE=2` ("sanguine flames") is never set, and `isSanguineFlame` is unused.

### 3.5 Client rendering

- **One boundary renderer with a style strategy** for shape and palette. Harbinger and Unstained render types are byte-identical (`RenderTypeInit:58-102`), and Unstained still uses the legacy tier-to-ring formula.
- **Shared parts:**
  - one ring/band generator: main ring, socket ring, `drawEffectRing` and `drawFlatDisc` are the same band with different radius and width;
  - one `emitQuad`, currently copied into 7 renderers;
  - one undulation formula, currently written out 4 times in BR;
  - one copy of the ring palette, which is pasted twice (BR:334-345 and 631-641);
  - one lightning palette, defined 3 times (`CardinalRiteFogLightning:14-15`, IH:48-49, HCRE:112-113).
- **One rite heartbeat clock.** The floor uses a 36-tick lub-dub (`CardinalRiteFloorVisualRules.heartbeat`), sigils use a plain sine (`IchorianSigilOrganicGeometry.heartbeat`), and BR and Unstained have their own pulses. As a result the floor, rings and sigils beat out of sync.
- **One small math utility.** `smoothstep`, `clamp` and `normalizeAngle`/`angularDistance` are redeclared in about eight classes. The partial-tick lerp appears six times in `ActiveRiteClientData` and could be a single `InterpolatedFloat`.
- **Typed phases.** The client compares phase strings such as "LEGACY", "OFFERING_PROCESSION" and the fake "SUCCESSION". Use `CardinalRitePhase`.
- **Seven tendril and vessel implementations** exist: staff ribbons, boundary veins, BloodCraft veins, Ichorian ribbons, Awakened tubes, the Focus braid and HutosLib `TendrilRenderer`. None of them uses `LuxUmbraGeometry.ribbonPath` or the `CRUOR` material in `LuxUmbraBatch`.

### 3.6 Ash trails

Four classes of about 540 lines each are copies of vanilla redstone wire, and they really emit redstone power (`getSignal`/`isSignalSource`). Smouldering and Befouling differ by 35 lines; inert and active differ by 22. Collapse them into one class, or two with an `ACTIVE` property, drop the redstone signal, and replace `DustParticleOptions` with Hemomancy embers. No rite or floor uses them today; they appear in Blood Structure patterns, crafting and as Hungering Earth's replacement block.

---

## 4. Dead code and leftovers

**Server:**
- `completeCrimsonLodge` (HCRE:1847), `CRIMSON_LODGE_CHUNK_RADIUS`, `CrimsonLodgeSavedData` and the empty `CrimsonLodgeEvents`. This is **verified**: there are no callers, and Illuminatus deliberately no longer consecrates a Fane.
- `popFungalSpineFromBack` (HCRE:2362) and the empty "Gourd" header (HCRE:1208).
- `getInstabilityBand` (ACR:757), `CancellationHandler.canStart`, the 4-argument `drawSigil` overload (OE:568), and `hasActionableSigilNodeAt` / `currentWaveSigilProgressKey` (IH:396, 579).
- `RITE_CASTER` is written but never read (OE:341). `CardinalRiteThreatEvents` re-declares the `RITE_BOUND` string.
- `sigilProgress` is overloaded with `attendant:`, `blood:` and `false_stroke:` keys. Give them typed fields.
- Seven puppeteer-trial fields are serialised for every rite.
- `totalTicks` and `remainingTicks` mean nothing for interactive rites.
- Live migrations to review for retirement: `ALEMBIC_PROJECTION`, `SCRIPTORIAL_INSCRIPTION`, the `sanguine_initiation`/`votary_rite` retirements, and the empty-escrow branch in `StationUpgradeRites:164-178`.

**Client:**
- **The `rite_pillar` particle, its geometry and its factory are registered but never spawned.** This is **verified**. Either delete them or use them as the rank-rite ascension beat (§6.2).
- `SHOW_FOCUS_BRAID=false` leaves `CardinalFocusBraidRenderer` and `drawMound` dead.
- `EXTEND_TO_GASH` and `DIG_BLOCK_DUST` are still marked experimental. Decide on both. `DIG_BLOCK_DUST` is the documented vanilla-particle exception.
- BR socket-neck methods are uncalled: `drawSocketNecks`, `emitSocketNeckPass`, `socketBandEdge` and `SocketBandEdge` (BR:749-880).
- The fog diagnostic builds a `StringBuilder` every frame and logs at INFO whenever the state changes (`CardinalRiteFogRenderer:69-86`). This is **verified**.
- `MINIMUM_PUFFS == MAXIMUM_PUFFS`, so `PUFFS_PER_RADIUS` is dead (`CardinalRiteFogGeometry:11-13`).
- `CardinalRiteOverlay` is still documented as a "Prototype".

---

## 5. Performance

- **Fog.** `CardinalRiteFogGeometry.puffs(seed, radius)` rebuilds about 437 records per frame for each rite, then sorts them and allocates about 12 `Vec3` per puff. The result is deterministic, so cache it by seed and radius.
- **Floor.** FCR rescans a `(2·(half+4)+1)²` grid of `getBlockState` calls every frame and rebuilds the drape maps, trench grounding and ground colours. Only the `GashLoop` is cached. Cache the vertex data and invalidate it when a block or brazier changes.
- **RenderTypes.** `HemoRenderTypes.cardinalFloorMembrane`, `cardinalFloorMound`, `cardinalStaffBloodMelt` and `cardinalRiteFog` build a fresh RenderType on every call, and FCR requests the membrane three times per frame. Pass uniforms through per-frame shader state instead.
- **Boundary renderer.** It looks up the recipe for every rite twice per frame, allocates streams for each ring and anchor, creates a `new ItemStack` each frame, and has no distance culling.
- **Network.** The helix sends 16 `sendParticles` packets every 2 ticks for each rite (HCRE:487-501). The daemon sends one packet per point (`HumanitySpriteEntity:179`). Both are better as one client-driven effect packet per rite state.
- **Cache staleness.** `GROUND_COLOURS` is keyed by BlockState and never cleared on resource reload.

---

## 6. Making rites more interesting

### 6.1 Gameplay

**Where it stands.** Each phase gives the player one action at most:

| Phase | Player action |
|---|---|
| Consecration | Fill anchors |
| Inscription | Optional sigils, then seal. There is no pressure. |
| Ordeal | Kill mobs or trace a sigil (13 rites only) |
| Procession | None |
| Culmination | None |

Rite form (minor to grand) changes only the boundary size and a +3 instability per form on missed waves. Degree changes only the number of anchors and the hard-coded wave count.

#### A. Give each floor style its own ceremony archetype

Floors already split rites by meaning, and each style already has its own palette. Let the style also decide the ceremony's central verb. This sorts the 52 rites into four families that feel different without writing 52 bespoke mechanics:

| Floor | Rites | Verb | Twist |
|---|---|---|---|
| **Threshold** | Rank and vessel rites | **Endure** | Anchors bleed slowly while the daemon matures, and the caster keeps them topped up while the threats test that discipline. The leash tightens as the rite goes on. |
| **Dominion** | Beacon, Fortification, Dominion, trials, Fane | **Hold ground** | Waves come out of the fog bank and go for the anchors, not the caster. Bastion and Cage gain real jobs here. |
| **Communion** | Bloodline and covenant rites | **Share** | Paired anchors on opposite sides must be fed at the same time, by the caster and an ally or NPC helper. The shared pool is drawn down visibly along the floor vessels. With no helper, the caster runs between them against a short decay timer. |
| **Working** | Crafting and station rites | **Precision** | Circuits and sigil strokes have timing windows on the floor's heartbeat: feeding on the beat costs less and adds no instability. This needs the unified heartbeat clock from §3.5. |

#### B. Make optional sigils change the result

Today support sigils only matter during waves, so the 7 rites that require Bastion with no waves turn it into a tracing chore. Instead, give every awakened sigil a generic modifier that works in any rite:

| Sigil | Modifier |
|---|---|
| Reservoir | A clean completion refunds part of the anchor blood. |
| Seal | Offerings are not lost on collapse. |
| Hematic Lattice | Ally contributions count more. |
| Mnemonic | Records the rite in the Liber or reveals hidden false omens. |
| Lens | Shows the next wave's spawn points in the fog. |

Keep these to economy and information only, never progression proof, to respect the gate rules in AGENTS.md.

#### C. Make instability a gradient, not a cliff

Map the existing (unused) instability bands to rising pressure:

1. Anchor decay starts.
2. The fog closes in, and visibility drops.
3. Threats get stronger.
4. Brazier fires gutter, and the offerings are at risk.

Reward the other end with a "clean rite" bonus for finishing under 25 instability, such as a blood refund or a Liber note. Retire `safe_retry` on anything above D3, or make it mean "no damage, but committed blood is lost".

#### D. Put the caster to work in the procession

Rather than the daemon visiting each brazier on its own, the caster holds Blood Absorption toward each brazier in turn to pull its offering along a tendril into the daemon. This is the same verb as the shared brazier lifecycle (insert, ignite with Projection, hold Absorption), so Scar, Memory and Graft teaching carries over to the cardinal rites. At D6 and above, the last wave can overlap the procession.

#### E. Fix and widen the ordeal deck

- Implement `covenant_pressure`. A natural design is that it draws from the bloodline pool and helpers each second until the response sigil is traced.
- Ship data for the `discover_*` waves, which have code but no data.
- Use `guaranteed_waves`.
- Make Exsanguination a true sacrifice. A tethered, named creature inside the boundary is drained into the anchors during the ordeal, and its blood partly fills them. This gives the rite its own mechanic and fixes bug #4.

#### F. Unstained: give them a stillness ceremony

The inverse of the blood ceremony, run on the same engine:

- Instead of filling anchors, the caster **quiets fonts**: standing still near each one, or pouring Lethean water or cleansed shards into it.
- **Turbulence** replaces instability. It rises with movement, with blood-bearing creatures entering the boundary, and with damage taken.
- Leaving the circle disturbs the water instead of draining blood.

This brings Unstained rites onto the ceremony engine, which also fixes bugs #2 and #11, and keeps their still-water tone. Today they are "build, press C, stand for 5–40 s".

### 6.2 Effects

#### 1. Make the floor membrane react to the rite

It is the best layer and already has heartbeat, spread, collapse and dig-front uniforms. Feed it rite events so it becomes the rite's body:

| Rite event | Membrane response |
|---|---|
| Staff impact | One hard pulse out from the Focus, and the vessels flare. This fills the missing impact beat. |
| Anchor filled | A blood pulse runs along the matching vessel to its socket. |
| Instability | The membrane bruises and darkens around damaged anchors, and the vessels thin. |
| Wave start | The membrane flinches and goes pale for a moment, then the fog bank parts where mobs will emerge. |
| Completion | A flush and swell, followed by a slow settle. |
| Collapse | The membrane tears back to the Focus at speed, reusing the mirrored-formation code at about 3× rate. |

#### 2. Give the daemon geometry

It is currently a particle point cloud, and `HumanitySpriteRenderer` draws nothing. Build it to the Riposte standard: a core of HutosLib tendril ribbons and cells on a dynamic glow, with layered silhouette, glow and spark passes. Let it grow visibly through the rite:

- it emerges from the staff;
- it firms up when sealed;
- each absorbed offering adds a visible feature, replacing the vanilla `ITEM` orbit;
- in culmination it unwinds into the caster.

#### 3. Move rings, sockets and sigils onto a real material

They currently use the vanilla lightning shader with flat vertex colour. Give them a vessel texture plus a glow pass, take the palette from the floor style so the whole rite reads as one organism, and drive them from the shared heartbeat.

#### 4. Give each family its own culmination

Add a `culmination` field to the ceremony JSON, with a small set of styles:

| Style | Rites | Look |
|---|---|---|
| **Ascension** | Rank rites | The unused `rite_pillar` rises through the caster, who is lifted briefly as the daemon merges. |
| **Grounding** | Rooted Vein, Hungering Earth | The pulse runs through the membrane and out into the terrain, so the effect visibly spreads from the floor. |
| **Binding** | Bloodline and covenant rites | Tendrils run to each participant or to the ledger. |
| **Forging** | Vessel and station rites | The result condenses out of the daemon onto the Focus. |
| **Domain** | Dominion, Fervor, Beacon | The boundary grows out to the zone edge, then fades into the persistent zone render (§3.3). |

#### 5. Choreograph failure and collapse

Today every layer disappears at once. Instead:

- the rings rupture segment by segment and drain into the trench;
- the staff melts using the existing melt shader;
- the fog surges, then thins;
- broken-anchor lightning fires;
- the membrane tears back.

Base the timing on the cancellation sequence, but make it violent rather than controlled. Unstained rites need their own completion and cancellation as well.

#### 6. Telegraph waves

Before the mobs appear, a fog lightning bolt strikes each spawn point and the fog parts. Wave completion and the deadline instability spike are currently silent and need a beat each.

#### 7. Add custom sounds

There are no rite sounds in `SoundInit`. The gaps:

- **Moments with no bespoke sound:** impact, anchor fill, seal, wave start, culmination, collapse and stillness.
- **The completion sting:** today it is `ENDERMAN_SCREAM` plus `BEACON_ACTIVATE`.
- **Sounds played from the render thread:** `WARDEN_HEARTBEAT` and `RESPAWN_ANCHOR_CHARGE` in BR:229-232.

#### 8. Sweep out vanilla particles

These go against the custom-particle rule:

| Particle | Where |
|---|---|
| `ITEM` | OE:196, 198; `HumanitySpriteEntity:205`; `CellHandParticleEffects:241` |
| `CRIMSON_SPORE` | HCRE:2517 (×300, Founding Fane); `PuppeteerTrialRiteController:143` |
| `levelEvent(2001)` block break | HCRE:1587, 1698; BCKP:770 |
| `END_ROD` / `SCRAPE` / `SNOWFLAKE` / `WAX_OFF` | About 20 calls in UCRE; `PaleConsecrationEvents:73` |
| `DustParticleOptions` | The four ash-trail classes |
| `BLOCK` | FCR:853 (`DIG_BLOCK_DUST`, a known experiment) |

---

## 7. Documentation drift

**`docs/HEMOMANCY_REFERENCE.md`:**

- Stale entries:
  - Apotheos is listed at 7000 blood; it is 0.
  - Bloom is described with "blood cost 1200" and a pattern-block list.
  - "Adept Rite" should be "Rite of the Sanguine Brotherhood".
  - The puppeteer trials are counted as "six"; there are seven.
  - Cinder Bellows' atmosphere is given as "Fire Breather instruction"; it is dense fog.
  - Exsanguination is described as the passive-sacrifice rite.
  - Silver Veil is said to grant Silver Ward; it applies Verdigris Aura.
- Missing from the rite tables: sanguine_fervor, sanguine_eclipse (listed elsewhere as D3+, but its JSON says D4), mnemonic_restoration, the vessel and horn rites, and severed_covenant.

**`docs/archive/concept/Hemo Concept/CARDINAL_RITE_SAMPLE_GUIDE.md`:** First Circulation uses Cut Copper and describes Degree 0 activation with a Sanguine Formation. Initiation no longer uses Cardinal Rites.

**Rite descriptions that contradict behaviour:**
- initiate_rite claims "formal entry" but grants no degree.
- covenant_vigil says "Grand" but is a greater rite.
- Four Unstained rites claim to teach Still Arts.

**Liber coverage:** `LiberEntryDefinitions` has no entry for 22 rites, including all the trials, the vessel rites, covenant_vigil and eternal_covenant.

---

## 8. Suggested order

1. **Bug pass**, small and low-risk: #1 (data), #2, #3, #6, #7, #8, #9, #10 and #12, with regression tests for #1 and #2. Bugs #4, #5 and #11 are covered by later steps or need design.
2. **Server unification:** `terminate()`, the `RiteEffect` registry with staged `canComplete`, rank-up targets in JSON, and data-driven waves. Then `RiteVariant` for station, succession and trial, and finally `RiteZone`.
3. **Client unification and performance:** one boundary renderer with styles, the shared band generator, heartbeat and math, then the fog, floor and RenderType caches. Remove the dead code from §4.
4. **Effects:** the reactive membrane, culmination styles, collapse choreography and wave telegraphs, then daemon geometry and the custom sound set.
5. **Gameplay:** floor-style archetypes, sigil result modifiers and graded instability, then the participatory procession and the Unstained stillness ceremony. Update the Vicar and teacher dialogue, the Liber, the reference doc and the activation tests with each one.
