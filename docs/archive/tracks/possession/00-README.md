# Silentium possession rework — work paths

Date: 2026-10-28. The overnight Section 4a/4b build (`docs/superpowers/specs/2026-10-28-silentium-possession-design.md`, `…-possession-hazards-wax-visitation-design.md`; ledgers `.superpowers/sdd/2026-10-28-silentium-possession/` and `…-possession-hazards-wax-visitation/`) works but took the easy road in three places: offerings as loose items, a Thread of Will item instead of the manipulation wheel, and no cinematics. Its facsimile bodies are also unfinished (frozen poses, player hitbox, vanilla fire and iron).

Each file below is one independent track. A session takes one, reads this README, and leaves the others alone.

| # | Track | File | Depends on |
| --- | --- | --- | --- |
| 1 | Rite offerings through the braziers, Sanguine Quintessence as the medium on a Qliphoth grand floor | [01-rite-offerings.md](01-rite-offerings.md) | — |
| 2 | The possessed radial wheel replaces the Thread of Will | [02-possessed-radial-wheel.md](02-possessed-radial-wheel.md) | — |
| 3 | Rite cinematics: departure, arrival, the two returns | [03-cinematics.md](03-cinematics.md) | 1 (the daemon/medium beats), 4 (bodies animate) |
| 4 | Facsimile bodies done properly: ticking, hitboxes, nameplates, crimson fire, hematic iron | [04-facsimile-bodies.md](04-facsimile-bodies.md) | — |
| 5 | Adipocere on the walls of vanilla dungeons, strongholds and ancient cities | [05-adipocere-worldgen.md](05-adipocere-worldgen.md) | — |

**Status (2026-10-29):** Track 1 built. Track 2 built: `PossessionAction`, `PossessionWheelRules`, `PossessionActionPacket` (protocol 21), `PossessedRadialScreen`, Thread of Will removed; `possessionReviewClient` (`PossessionClientReview`, gameTest) possesses all nine forms, captures each wheel and checks Return. Track 4 built: `PossessionBodyRules` + size event (hitbox and eye height per form), ticked client stand-in bodies with nameplates, `crimson_fireball` (possessed Spit and the fauna blaze's volley), crimson blaze embers/skin/charged fire, hematic-iron golem skin and crimson crack layers; its review scenes (body room check, idle/moving/gift shots, fauna pass) are in the same driver. Track 3 adds its scenes there too. Track 3 built: `PossessionSceneRules` (tick table), `PossessionSequences` (staged begin/return, skip, abandon), `PossessionScenePacket` + `PossessionSkipPacket` (protocol 22), `PossessionScene` (six director halves), `PossessionSceneClientState` + `PossessionSceneClientEvents` (hiding, posing), echo `manifest` blend, `PossessionSceneRenderer` (tendrils, spores, daemon haze, shatter, goo); its review passes play every scene for the wolf and blaze and a skip pass.

Tracks 1, 2, 4 and 5 can run at once. Track 3 should start after 1 and 4 have landed, since its beats depend on the medium being in the Focus and on the bodies animating.

## What stays
- `Possession` attachment, `PossessionService` (begin/finish), `PossessionRules`, `SurrogateForm`, `SurrogateLedger`, `PossessionEvents` (Concentration from damage, hurt cooldown, water drain, ejection, logout/login recovery, block/entity interaction lock, no PvP), `MindBrokenEffect`, the echo entity, the Concentration HUD, `PacketSyncPossession`.
- `AbhorrentGazeRules` (gaze and grab), adipocere's item, scrape, loot and distillation, the Visitation rite.
- The rite's refusal chain (Silentium, not Mind Broken, not already possessing, a surrogate in the rite space, not in a Chamber visit or projection).

## What goes
- `PossessionService.consumeOffering` and the loose-item offering scan (Track 1).
- `ThreadOfWillItem`, its texture, model and lang; `PossessionAbilities.use` moves behind the wheel (Track 2).
- The `communion_lesser` floor and the empty brazier signature on `possession_rite.json` (Track 1).
- `PossessionClientEvents.BODIES`'s never-ticked cached entities as they stand (Track 4 replaces the approach).

## Shared rules
- `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md`: lang keys, `HemoParticleData`, static render types / `SeededRenderTypes`, `WorldEffectDispatcher`, `AnimationTime`, `BossReplayClock` / scene tracks for timed presentation, no copper or brass, 16 px/block.
- Custom visuals first (`docs/MANIPULATION_VISUALS.md`): the daemon haze, tendril and spore effects already exist; reuse them through their public bridges.
- Any payload change bumps `PacketHandler.PROTOCOL_VERSION` and rewrites `payload-registry.txt`.
- Client proof is required: the possession review client (Track 2 sets one up: `possessionReviewClient`) must show the result.
- No commits.
