# Track 2: Strain powers

**Status:** DONE 2026-10-30 (third unattended run). All shared pieces and all eight ladders are built at the spec's numbers and proven: JUnit (4,714 tests, 0 failures), the `morphling_strain_validation` suite (49 required GameTests), `combat_targeting_validation` (19) and a live `/hemoverify strands` run on a dedicated server with two driven clients, where each client's digest of the other player matched the server's (active bits, strand ids, anchors, Purse fill) through a partner relog and a host relog. The Purse's +35% is now a ceiling on the total with the Prismatic set. `docs/MORPHLING_STRAINS.md` has the numbers table. Left for other tracks: Track 3 draws the field entity, the strands and the active bits and replaces three placeholder effect icons; Track 4 writes the final text for the placeholder keys. Numbers stay open to in-play tuning inside the bands (M8). See "Handover".

Phase A. Depends on Track 1's `MorphlingStrains` table. Spec sections: "Strain powers and appearance" (shared rules, balance rules, the eight per-strain tables).

## Goal

Each strain grants the passive, Developing, Mature, Apex and Primal powers in the spec, with its home condition, at the spec's first-pass numbers (SETTLED 2026-10-29, M8: build first and tune in play, always inside the cost and cooldown bands). The numbers live in the spec tables; this track does not restate them. Where this file and the spec disagree, the spec wins.

## Shared work, built first

These are the pieces more than one strain needs, or that the spec calls new mechanics. Build them as small pure-rule classes with unit tests, the way `EmberfangHeatRules` and `DeadmansPurseFeedBankRules` are built now.

| Piece | PROPOSED home | Used by | Notes |
| --- | --- | --- | --- |
| Home conditions | `MorphlingHomeConditions` (one predicate per strain) plus `MorphlingHomeBonus` (the shared +50% durations and rates, +25% radii) | all eight | Kinbud's heat check can lift its logic from the old `EmberfangHeatRules` into a new `KinbudHeatRules`. Hushbell's "standing still" and Sunburst's "water within a few blocks of a daylit surface" need a cheap still-tracker and a short upward column scan; cache per player per second. Expose `isHome(player, strain)` for Track 3's tint and HUD. |
| Stung | `StungEffect` in `EffectInit` | Kinbud | 1 damage per second, refreshes rather than stacks, hurts undead, not cleansed by poison immunity (balance rule 6). Its own damage type so death messages read right. |
| Detection range | `MorphlingVisibility` on `LivingEvent.LivingVisibilityEvent` | Ink Cap | Multiplies visibility, capped at 50% reduction even at home. Combines with sneaking and Invisibility. Check against Silent Archon armor so neither doubles the other. |
| Damage redirection | `MorphlingDamageShare` on `LivingIncomingDamageEvent` | Holdfast (Breakwater), Dead Man's Rope (Colony) | One handler, two rule sets. Holdfast: 25% of ally damage moved to the host, paid at 2 mL per HP through `BloodAccounting.pay`, stops at 30% host health. Rope: 20% spread across linked creatures, none below 50%. Guard against recursion (redirected damage must not redirect again). |
| Links | `MorphlingLinks` (session map, UUID-keyed, cleared through `TransientServerStateEvents`) | Dead Man's Rope | Up to 1 link (Mature) or 3 (Apex) to an ally, tamed companion or block anchor. Breaks out of range or on release. Synced to tracking clients for the strand render (one new clientbound payload: bump the protocol). |
| Queued regrowth | `MorphlingRegrowthQueue` (per-player, survives relog only inside its 10-second window, so session state) | Kinbud | Queues a share of each non-lethal hit and heals it over 10 s. Counts toward the circulation cap (balance rule 5). Kindled caps total regrowth at 20 HP (10 away from home). |
| Body strike | `KinbudSecondHead` | Kinbud | Not a summon: no summon registry, no command, no Puppet Interference. A timed server-side strike from the host's shoulder (nearest hostile in 4 blocks, once a second, 8 s), targeting through `ManipulationCombatHelper.canHarmFor`. Its visible state rides the equipped-Morphling sync for Track 3. |
| Field patches | `MorphlingFieldEntity` (one entity, a `kind`: `BLOOM`, `WHALE_FALL`) | Sunburst Apex, Marrowplume Primal | Short-lived area with no blocks placed. Ticks its aura, despawns on time, never saves past its lifetime. Track 3 draws it. |
| Anchoring | `MorphlingAnchorState` | Sunburst Arbor, Holdfast Breakwater and The Holding, Marrowplume's Whale Fall bonus | "Sneaking without moving for N seconds" and "uprooted by moving or jumping" in one helper. Anchoring is never petrification: nothing permanent happens to the player. |
| Primal actives | existing `MorphlingItem.tryBeginPrimalAbility` | all eight | Already pays blood and applies Morphic Strain. Give each Primal a named `BloodAccounting.Source` constant. Check that `tryBeginPrimalAbility` pays through `BloodAccounting.pay`; fix it if it drains directly. |
| Passive effects | eight new effect classes | all eight | `SunFedEffect`, `GripEffect`, `PurseSiphonEffect`, `LateralStrandsEffect`, `VeilEffect`, `HushEffect`, `MarrowFeedingEffect`, `CultivatedMendingEffect`, registered as `morphling_<new id>` through the table. |
| Strain hooks | `IMorphling` | all eight | Move Emberfang's special branches out of `EquippedMorphlingEvents` (`applyHotheadedTick`, `scaleExhaustion`) and `MixinHooks` into strain hooks. Add `onEquippedHurtOther` (ally damaged nearby) if Breakwater needs it rather than a second subscriber. |

## The eight ladders

Build order PROPOSED: most reuse first, so each strain lands testable and the new mechanics arrive one at a time.

| Order | Strain | Old code worth lifting (a guide only; nothing has to match the old strain) | New pieces it needs |
| --- | --- | --- | --- |
| 1 | Hushbell | Winter Shroud (poison immunity, Cryptobiotic Hide, Cold Cleanse, Cryptobiosis numbers) | slower hunger and air loss; Fold Shut (rooted, Resistance IV, no attacks or item use, cleanse on unfold). Remove Winter Shroud's speed and Tun Molt's invisibility. Cut the Developing Resistance II baseline. |
| 2 | Marrowplume | Gravecap (Mycorrhizal Mending, Sporulation, Network, Cordyceps Burst, Primal Mycorrhiza) | Plume Cloud's feeding heal (replaces Wither); Root Network's solo fallback; Fall's Inheritance as a 25%/40% roll with the economy guard (no players, bosses or spawner mobs); Whale Fall as a field patch. |
| 3 | Mermaid's Purse | Deadman's Purse (siphon, Feed Banking, kill banking, Hemophage Covenant) | swim speed (capped at +35%); sprint-swim banking bonus; School's Share; Raise the Sail (Dolphin's Grace to allies behind, bill strike once per 8 s). |
| 4 | Ink Cap | Lumenlace (Sepia Wake, Low-Light Camouflage, Ink Mantle Reprieve) | Veil detection range; Deliquesce (dissolve: Invisibility, Speed II, mobs drop target, no attacking or items). Ink Reprieve keeps `hemomancy:ink_mantle` but moves to Apex. |
| 5 | Holdfast | Irontooth (mining speed, Seismic Slam), Lumenlace (knockback resistance), Bootlace (Wall Climbing) | stone-only breaking speed; Cling in water currents; Brace; Breakwater (damage redirection); The Holding (pin, then the slam). Knockback resistance stays within the triad caps. |
| 6 | Dead Man's Rope | Witch's Ear (Echoic Perception), Bootlace (Silk Tether, Arachnid Anastomosis, Web of Red Thread) | Lifeline fall caps per stage; Strand Link and its block grapple; Colony (links + damage share); Red Rope's ally rescue pull and its 12 s cooldown, no poison. |
| 7 | Kinbud | Emberfang (Venom Strike, Constrict, heat rules) | Stung; Cultivated Mending regrowth queue; Second Head; Kindled. Retire Emberfang's speed passive, Ambush Predator and Sovereign Venom. |
| 8 | Sunburst | nothing (spec: "carries forward nothing mechanical from Lumenlace") | Sun-Fed (upkeep waived at home unless Puppet Interference is active, which restores normal upkeep ×1.25; hunger then blood surplus); Shared Table and its solo food fallback; Lantern Fronds (still-standing aura); Bloom field; Arbor anchor. No self death-save. |

Each strain is one task: its class, its passive effect, its rules class and tests, its lang keys for `getMaturityBonusDescriptions` (Track 4 writes the final text; placeholder keys are fine), and a GameTest per stage.

## Balance rules every strain keeps (SETTLED, spec "Balance rules")

1. Repeatable Primal costs 250–450 mL on 12–90 s cooldowns; death-saves cost 500–650 mL on the shared Last Rite 10-minute cooldown.
2. Developing 3–15 s, Mature 8–20 s, Apex 20–60 s cooldowns. Always-on conditional powers have none.
3. Every Developing, Mature and Apex power does something solo ("Alone" fallbacks).
4. Giving health never drops the giver below 50% (Shared Table, Colony) or 30% (Breakwater).
5. Morphling healing counts toward the circulation cap; knockback resistance respects the triad caps; resistance amplifiers don't stack across sources.
6. Stung, not Poison.
7. Bonus loot excludes players, bosses and spawner-flagged mobs, and rolls by chance.

Encode 1, 2 and 4 as constants checked by `MorphlingBalanceBandsTest`, so a later tuning pass can't drift out of band unnoticed.

## Things to watch

- Holdfast's `Cling` and Ink Cap's `Folded Away` both answer "standing still" or "against a wall" every tick; share the still-tracker with home conditions.
- Lantern Fronds gives allies Water Breathing and Night Vision, and Hushbell slows air loss. Check them against Court NPC breathing (Track 9) and the existing diving and conduit behaviour so nothing makes the deep trivial at Mature.
- Mermaid's Purse swim speed against the Prismatic set (+35% ceiling) and Dolphin's Grace stacking.
- Kinbud's Second Head and Stung against `CombatTargetingMatrixGameTests`: add it to the matrix.
- Red Rope pulls a hostile and roots it; pulling bosses is capped at 2 s root by the spec. Check every boss's knockback and teleport immunity flags.
- Fold Shut and Deliquesce block attacking and item use: route both through one "Morphling-locked" check the input and use-item paths already respect, not two.

## Tests and proof

- Unit: one rules test per strain (numbers per stage, home multipliers, caps), plus `MorphlingBalanceBandsTest`, `MorphlingDamageShareTest`, `MorphlingRegrowthQueueTest`, `MorphlingHomeConditionsTest`.
- GameTests: a `morphling_strain_validation` batch: each strain at each stage in and out of home, solo and with an ally, and every death-save once. Keep `MorphlingEnvironmentGameTests` meaningful (it pins the old environment rules; rewrite to the new home conditions).
- Add Kinbud's Second Head and Red Rope to `CombatTargetingMatrixGameTests`; add the Rope link and Purse school share to `MultiplayerVerification`.
- Client proof is Track 3's review client.

## Docs

`docs/archive/done/CURRENT_MORPHLING_STATS_AND_ABILITIES.md` describes the old strains. Leave it as history and write `docs/MORPHLING_STRAINS.md` as the current stats reference (Track 4 owns the prose; this track supplies the numbers table).

## Open questions

- Not blocking: Sun-Fed surplus converts hunger to blood "only in daylight". Confirm that underwater near a daylit surface counts (the home condition says it does).

## Handover (written 2026-10-30, unattended run)

### What landed

- Shared pure rules, all in `common/item/harbinger/morphlings/`: `MorphlingHomeBonus`, `MorphlingPower` and `MorphlingBalanceBands` (rules 1, 2 and 4 as constants), one `<Strain>Rules` class per strain with its first-pass numbers and `POWERS` ladder, `MorphlingDamageShareRules` (Breakwater and Colony), `MorphlingRegrowthQueue`.
- Shared runtime: `MorphlingHomeConditions` (one predicate per strain, wired into the `MorphlingStrains` rows, cached once a second, `isHome(player, strain)` for Track 3), `MorphlingAnchorState` (the one still and anchored tracker), `MorphlingAllies`, `MorphlingHealing` (host healing claims the circulation window through the new `BloodFlowLedger.claimCirculationBandwidth`), `MorphlingFeeding` (fractional hunger), `MorphlingLinks`, `KinbudSecondHead`, `MorphlingLock`, `MorphlingSession` (short per-host timers), `MorphlingPowers` (ladders, synced state, teardown), `MorphlingActiveState` (the synced bits).
- Effects and types: `stung` (`StungEffect`, damage type `hemomancy:stung`, bypasses armour, death messages), `pinned` and `morphling_sealed` (`MorphlingLockEffect` markers, placeholder icons copied from strain sprites). `Paralysis.blocksActions` folds in `MorphlingLock.blocksActions`; a new `Paralysis.blocksMovement` (paralysis or pinned) replaces `isParalyzed` in the four paralysis movement mixins; the paralysis attack mixin calls `MorphlingLock.onBlockedAttack`, which ends Deliquesce.
- `MorphlingFieldEntity` (`hemomancy:morphling_field`, kinds `BLOOM` and `WHALE_FALL`), never saved, registered with a `NoopRenderer` placeholder until Track 3 draws it.
- `IMorphling` hooks: `onEquippedServerTick`, `activeFlags`, `visibilityMultiplier`, `exhaustionMultiplier`, `airLossSkipChance`, `breakSpeedMultiplier`, `onEquippedSneakUse`, `resistsEffect`, `waivesUpkeep`, `onUnequipped`. Wiring: `EquippedMorphlingEvents` (per-tick hook, stillness update, Sun-Fed waiver with bonding credit, active-state sync on change, teardown on unequip and death, kill record for Marrowplume's home), new subscribers `MorphlingPowerEvents` (breathing, break speed, effect immunity, sneak-use strands, knockback, target drop, Cling), `MorphlingDamageShare`, `MorphlingVisibility`; `MixinPlayer` gained the current-resistance hook and `MixinHooks` the Hush exhaustion scale.
- The eight strain classes carry their ladders. Each Primal pays through `MorphlingItem.tryBeginPrimalAbility` with its own named `BloodAccounting.Source` (new overload); the existing path already paid through `BloodAccounting.pay`. An unset cooldown now reads as never used.
- Sessions: every new UUID-keyed map is cleared and logged out from `TransientServerStateEvents` (the regrowth queue survives a relog inside its window).
- Payload: `SyncEquippedMorphlingPacket` now carries the active bits and the strands; `PROTOCOL_VERSION` 25 to 26 and `payload-registry.txt` rewritten.
- Goldens: `frozen/registry-ids.txt` regenerated (stung damage type and effect, pinned, morphling_sealed, morphling_field). Creative order unchanged.
- Removed: `MorphlingCamouflageRules` and `MorphlingColdResilienceRules` (replaced by `MorphlingAnchorState` and `HushbellRules`) and their test cases.
- `HarbingerRepairGameTests.staffPrimalChargesOnceAndRejectsCooldownWithoutWear` is back, on Holdfast's The Holding (350 mL once, the cooldown refuses without blood or Staff wear).
- Tests: `morphling_strain_validation` is a new suite (`runMorphlingStrainGameTestServer`, alpha) with `MorphlingPowerGameTests`.
- Docs: `docs/MORPHLING_STRAINS.md` (numbers table); HR §16 and the effects table point at it. Placeholder lang for every bonus line, Primal message, blood source, the three effects, the entity and Stung's death messages.

### Synced active state (for Track 3)

`SyncEquippedMorphlingPacket` sends `activeFlags` (varint), linked entity ids (varint array) and block anchors (`BlockPos.asLong` array) to the host and trackers whenever they change. Client side they are on `IEquippedMorphling` (`getActiveFlags`, `getLinkedEntityIds`, `getLinkedAnchors`). Bits, from `MorphlingActiveState` (append only, never renumber):

| Bit | Name | Bit | Name |
| --- | --- | --- | --- |
| 0 | HOME | 12 | DELIQUESCE |
| 1 | STILL | 13 | FOLD_SHUT |
| 2 | ANCHORED | 14 | CRYPTOBIOSIS |
| 3 | LANTERN_FRONDS | 15 | SECOND_HEAD |
| 4 | ARBOR | 16 | SECOND_HEAD_TWIN |
| 5 | CLING | 17 | KINDLED |
| 6 | BRACE | 18 | PLUME_CLOUD (10-tick pulse) |
| 7 | BREAKWATER | 19 | WHALE_FALL_ROOTED |
| 8 | HOLDING | 20 | REELING |
| 9 | SAIL_RAISED | 21 | LIFELINE (5 s) |
| 10 | SHARED_WAKE | 22 | BLOOM |
| 11 | FOLDED_AWAY | 24-27 | Mermaid's Purse banked-blood fill, sixteenths (`MorphlingActiveState.fill`) |

Field patches are `MorphlingFieldEntity` with synced kind, radius and lifetime. Rope strands render between the host and each id or anchor.

### Calls made unattended

- No eight named passive effect classes: Track 1's one `MorphlingStrainEffect` per row stays the visible marker, and each strain class does its passive work in its own tick (procs every 2 s). Eight empty classes would only rename the marker.
- "Bloodline ally or tamed companion" is whatever `ManipulationCombatHelper.allied` counts for the host (teammates, the host's pets and puppets, impressed mobs). There is no bloodline-membership query to use instead.
- Morphling healing of the host counts toward the circulation cap by claiming the same window at 1 bandwidth unit per HP (`MorphlingHealing.BANDWIDTH_PER_HP`). Healing given to allies is not capped.
- Knockback resistance has no triad cap today; Grip is clamped so the host's total stays at or under 1.0. Breakwater's and Brace's immunity cancel the knockback event instead of adding attribute.
- One generic Morphling lock (two marker effects, `pinned` and `morphling_sealed`) folded into the paralysis checks, not a second input path. Pinning a boss is capped at 2 s everywhere (the spec's Red Rope rule applied to every pin); bosses are not pulled by Red Rope.
- Deliquesce: a swing is blocked and ends the dissolve ("attacking ends it early" with "cannot attack while dissolved").
- Raise the Sail's bill strike limit (8 s) is outside the Apex band, so Raise the Sail is listed as a conditional power with a re-trigger limit; Ink Wake's 4 s per target, Cold Cleanse's 5 s and Root Network's 4 s are periodic, not cooldowns.
- Lifeline leaves no block (the old strain placed a temporary cobweb); its strand is the `LIFELINE` bit for Track 3 to draw.
- Strand Link uses an empty-hand sneak-use; reeling to a block anchor runs while sneaking more than 2 blocks from it. A partner sneak-using on the host releases the strand.
- Whale Fall's "Marrow Feeding II" is Mycorrhizal Mending at amplifier 1, the same healing curve.
- Sun-Fed credits bonding with the upkeep it waives, so a Sunburst kept at home still bonds.
- Lantern Fronds' Night Vision is refreshed at 4 s, so it flickers like a short potion; a longer refresh would outlast the 3-second linger.
- Kinbud's heat home reuses `MorphlingHeatRules.environmentLevel` at HOT or above, plus Hydrothermal Depths and a 9x5x9 scan for magma, fire, campfires and lava.
- Mermaid's Purse open ocean is the deep-ocean tag or any Pelagic layer below the shore and reef.
- Tests: one `MorphlingStrainLadderRulesTest` with a case per strain rather than eight files.
- `MultiplayerVerification` gained `/hemoverify strands <host> <partner>`, and its digest now prints the active bits and strands. It was not run with two clients.

### Second unattended run (2026-10-30)

- `MorphlingStrainMatrixGameTests` (batch `morphling_strain_matrix`): one test per strain x home/away x alone/with a teammate, 32 in all. Each builds its habitat once, then runs Unfed to Primal on the same host, driving the equipped tick and checking which powers answer at that stage, the home bonus where it applies, and that allies are or are not reached. The home predicate is asserted against the built habitat at every stage. Habitats: Hushbell standing still (away: shifting every tick); Marrowplume a bone block in reach; Mermaid's Purse a pool to sprint-swim in alone, a teammate within 8 with one (away keeps the teammate at 10); Ink Cap a sealed stone room; Holdfast stone underfoot (away: planks); Dead Man's Rope a two-deep pool; Kinbud a magma block in reach; Sunburst open noon sky (away: a stone roof).
- Single tests in the same class: Ink Reprieve saves at Apex only and refuses a second save (the other death-save, Cryptobiosis, was already covered); Red Rope roots a boss (Elder Guardian) for 2 s and never pulls it; the strands and the Purse fill reach a tracking player through the real equipped tick and survive the payload codec.
- `SyncEquippedMorphlingPacket` gained read accessors (`playerUUID`, `linkedEntityIds`, `linkedAnchors`); the layout is unchanged, so no protocol bump.
- `MorphlingPowerGameTests.done` now removes only its own players' team, so tests running in parallel keep theirs; its helpers are package-visible for the matrix.
- Lantern Fronds: each refresh now lasts only to the end of the 3-second linger window (plus one refresh), instead of a flat 4 s, so the breathing ends about 3 s after the host moves, as the spec says. It used to run up to 7 s.

### Things to watch, checked

- Court NPC breathing: Track 9 has not built Court members yet; its spec has them breathe underwater natively, so Lantern Fronds gives them nothing they lack. For players, the fronds need a still host and now end about 3 s after it moves, and Hush at most slows air loss by 60%, so neither makes long dives trivial at Mature. Conduit Power still outdoes both. Track 9 should add a breathing fixture for a member inside a Sunburst's fronds.
- Purse swim speed and the Prismatic set: the strain's own share is capped at +35% as specified, but the set multiplies the total by 1.35, so a Primal Purse at home in the full set swims at about x1.82. Third run: clamped. The strain now only tops the total up to +35% (`MermaidsPurseRules.swimBonus(stage, home, otherMultiplier)`), so inside the full set it adds nothing; see the third run below.
- Dolphin's Grace: vanilla Dolphin's Grace has no amplifier effect, so Raise the Sail plus a dolphin's grace is one effect, not a stack.
- Bosses and Red Rope: a boss is never pulled (so knockback-resistance flags never come into play) and every Morphling pin on a boss (Red Rope, The Holding, Grasping Crown) is capped at 2 s in `MorphlingLock.pin`. A pin stops walking and jumping only; a boss or mob that teleports by its own code still teleports. That is within the spec (a short root, not a hold).
- Host self-healing claims the circulation window (balance rule 5): a degree-0 host gets about 6 HP a second, so School's Share alone at Apex and Primal (9 and 12) is clipped for a low-degree host. That is the intended rule; noted in `docs/MORPHLING_STRAINS.md`.

### Calls made unattended (second run)

- No `@GameTestGenerator`: the suite guardrail in `gradle/game-tests.gradle` treats a generator class as its own seeded-world suite, so the 32 matrix cases are plain `@GameTest` methods calling one runner.
- Dead Man's Rope and Mermaid's Purse homes include "with a teammate in reach" (a strand or the school), so their "away, with an ally" cases keep the ally outside the home's reach: the Purse's teammate stands at 10 blocks (outside the school's 8), and the Rope's away case tests its strand before asserting that the strand makes it home.
- The tracker-sync test stands in for the watcher's client by acknowledging chunk batches (a test player's connection is never ticked), then lets the chunk map re-check what it sees. It proves the server sends the strands and fill to other players; whether the other client applies them is still the two-client run's job.
- Lantern Fronds' effect length follows the linger window (above) rather than staying at 4 s; the Night Vision flicker under 10 s is unchanged.
- Purse plus Prismatic stacking left as built (above) rather than clamped, since the spec's wording ("the strain never grants more than +35%") reads as the strain's own share. Superseded by the third run, which clamps it.

### Third unattended run (2026-10-30)

- Live two-client run, done. A dedicated server (`runServer`, `-Dhemomancy.mpVerify=true`, game directory `build/mp-verify/server`, fresh `strands` world) and two driven clients, VerifyA (host) and VerifyB (partner), launched from a scratch init script that adds `mpVerifyA` / `mpVerifyB` client runs with `hemomancy.gameplayDriver`. The clients connected through the driver's `connect` action; commands went through `command`, and each client logged its own and its peer's digest with `peers`. Evidence: `build/mp-verify/strands-run1-*` and `strands-run2-*` (server log and both client journals).
  - After `/hemoverify strands VerifyA VerifyB`: server `VerifyA morphflags=3 strands=[160] anchors=[-2199023157178]`, `VerifyB morphflags=8000003` (HOME, STILL, Purse fill 8/15). Client B's view of A and client A's view of B matched the server exactly, as did each client's view of itself.
  - Partner relog: the entity strand drops when the partner leaves (its entity is gone), the block anchor stays. After B rejoined, both clients matched the server (`morphflags=3 strands=[] anchors=[-2199023157178]`; B's fill kept).
  - Host relog: the strands command re-tied (B's view showed `strands=[211]` plus the anchor), then A left. The links are session state, so on return the server read `morphflags=2 strands=[] anchors=[]`, and B's view of A matched.
- `MultiplayerVerification`: the digest now prints the anchors, and `strands` also anchors a strand to the block under the host and banks the partner's Purse to half the borrowed-blood cap, so the fill bits read nonzero.
- Purse and Prismatic: decided and clamped (see the calls below). `MermaidsPurseRules.swimBonus(stage, home, otherMultiplier)` caps the strain against the set's x1.35; `MermaidsPurseMorphlingItem` passes `ArmorSetBonusRules.pelagicSwimMultiplier` for the worn set. Unit cases in `MorphlingStrainLadderRulesTest`; GameTest `purseSwimSpeedNeverStacksPastThePrismaticSet` in `MorphlingStrainMatrixGameTests`. `docs/MORPHLING_STRAINS.md` updated.

### Calls made unattended (third run)

- The Purse's +35% is read as a ceiling on the total swim speed together with the Prismatic or Phantasmal Bloodlust set, per this file's "Things to watch" ("+35% ceiling") and the spec's "matching the full Prismatic set". Inside the full set the strain adds nothing; out of it nothing changes. Other swim sources (Dolphin's Grace, Depth Strider) are not counted.
- The live run did not drive School's Share (a hurt partner), a partner's sneak-use release, a death or a dimension change; the GameTests cover those on the server, and the sync path they use is the one the run proved.
- The run's entity ids were checked against the server's digest, not against a printed id of the partner on each client; vanilla clients use the server's entity ids.
- In-play tuning inside the bands (M8) is ongoing balance work, not a Done-when gate, so the track is marked DONE.

### What remains

- In-play tuning inside the bands (M8), as play shows the need.
- Track 3: draw the field entity (it has a `NoopRenderer`), the strands and every bit above, and replace the three placeholder effect icons. Track 4: final text for the placeholder bonus lines, sources and messages.
