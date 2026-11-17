# The Phantom Show: Circus perception presentation rebuilt

2026-11-16. Replaces the presentation half of `CircusPerceptionOverlay` and the motion jitter in
`CircusPerformerRenderer`. Server rules, scoring and stages are unchanged.

## Intent

Inside the Crimson Troupe's big top, acclimation should feel like a show taking shape around the player. Today it
reads as debug geometry: flat figure planes, hanging cloth quads, floating diamonds, a thin bone border, and NPCs that
shake. The owner's arc (2026-11-16): **a newcomer catches unsettling fragments; an attuned visitor sees the whole
phantom performance, as if they belong in the audience.** The tent's lighting gets darker and more dramatic as that
happens.

## Goals

- Ghostly audience apparitions in the seating tiers and on the galleries.
- Proper string lights: bulbs on sagging wire, strung across the tent.
- Phantom performers acting on the real apparatus (trapezes, ring), not diamonds.
- The tent darkens into a theatrical look: the house goes dark, the ring is lit.
- Every stage reads differently at a glance: fragments, a gathering show, the full show.

## Non-goals

- No change to `CircusProgressRules` scores, stage thresholds, the sync packet or anything server-side.
- No new sounds in this pass (a crowd murmur and applause are a later pass; noted in `docs/OPEN_QUESTIONS.md`).
- No change to the real NPCs' behaviour, the galleries or the pavilion template.

## Anti-goals (failure even if it works)

- Thick strands, noise contours or a screen-filling overlay ("spaghetti"; rejected twice before).
- Vanilla particles. Motes are `HemoParticleData`.
- Phantoms or darkness that hide real threats: a real NPC or hostile is never drawn over or lost in black.
- Anything another player sees. All of it is per viewer.
- Constant shaking. The current NPC jitter goes.

## Constraints

- Client-only presentation; the server owns the score and never reads any of this (CIRCUS.md D-29 ruling).
- Drawn from `WorldEffectDispatcher` (no new `RenderLevelStageEvent` subscriber); HUD parts as a `GuiLayerRegistrar`
  layer; animation time from `AnimationTime` (freezes under `/tick freeze`); render types built once
  (`SchoolRenderTypes` / static constants / `SeededRenderTypes`); easing via `HemoEasing`.
- Fog changes follow the `SchoolDarknessPresentation` pattern (`ViewportEvent.RenderFog`) and scale by the vanilla
  Darkness Effect accessibility option (`darknessEffectScale`).
- Performance: the pavilion is 65 by 65; phantoms are capped per stage and culled beyond the existing 28-block cue
  range. Marks are read by the existing two-second scan, never per frame.

## Approaches

- **A. Phantoms from the troupe's own models (chosen by the owner).** Draw the existing jointed performer and faculty
  models (`CircusAcrobatModel`, `CircusKnifeThrowerModel`, `CircusFireEaterModel`, `CircusStiltWalkerModel`,
  `CircusStrongmanModel`, `CircusFacultyModel`, ...) as translucent tinted apparitions with no entity behind them; string
  lights as authored geometry with the glow material; darkness as a fog and colour-grade presentation; spotlight cones
  on the ring. Most work; looks like the troupe itself and reuses the toolkit.
- B. Client-only fake entities. Free movement and animation, but fights entity tracking and is fragile across relogs and
  dimension changes.
- C. Painted silhouette cards. Cheapest, stays 2D: the look the owner wants gone.

## The three stages (Unaware shows nothing, as now)

| | Disturbed: fragments | Acclimating: the show gathers | Attuned: the full show |
|---|---|---|---|
| Audience | 1-3 seated phantoms, only in peripheral vision: each fades out as the view turns toward it (within ~20 degrees of the crosshair) and reappears in another seat later | about a third of the seats, partly faded, settled; they turn their heads to follow the player | every scanned seat and gallery deck filled; they sway and lean toward the ring, applause bursts on an act's beat |
| Performers | a single still figure glimpsed on a trapeze or in the ring, gone after 1-2 s, at most once every ~20 s | one act at a time, rehearsal-pale, cycling between the ring and a trapeze | trapeze artists swinging on every trapeze in view plus a ring act; acts cycle |
| String lights | one or two runs, most bulbs dead, a few flicker | all runs strung, warm and uneven, some flicker | all runs steady, the bulbs brighten in a chase on each act's beat |
| Lighting | slight darkening, a faint cold tint | the house dims; one spotlight swings slowly over the ring | the house is dark; two or three spotlight cones hold the ring and follow the trapezes |
| Screen | thin vignette, a brief afterimage stutter now and then on a real NPC (replaces the shake) | soft vignette, no stutter | a wide, soft theatre vignette; nothing else |

Phantom look: the model drawn translucent (`entityTranslucent` family) in bone white with a crimson rim, no texture
detail beyond the model's own albedo washed toward bone, alpha by stage (Disturbed low, Attuned highest but never
opaque), and a slow fade at the feet. A phantom fades out if the player walks within ~2 blocks, so it can't be
touched.

## How it is built (decided)

- **Marks.** The existing scan gains seats (`circus_seating`, facing gives the way a phantom looks), king poles and
  quarter poles (string-light anchors), and keeps rails, trapezes, rigs and the ring curb. The ring centre is the
  centroid of the scanned curb blocks.
- **String lights.** Runs are catenaries between anchors chosen at scan time: king pole tops to quarter pole tops,
  and along each gallery rail. Wire is a thin dark line; bulbs are small glow quads every ~0.6 blocks with a soft
  halo. Bulb state (dead, flicker, steady, chase) comes from the stage and a seeded per-bulb hash, so it is stable.
- **Phantoms.** A small client class poses a performer or faculty model without an entity: a pose record (limb
  angles, sway) driven by `AnimationTime`, fed to the model's parts, then rendered translucent. Seated audience reuse
  one faculty body in a seated pose with per-seat seeded variation (head turn, lean, which model). Trapeze artists
  swing on a pendulum anchored at the trapeze block; the ring act uses the model's existing performance animation where
  it has one.
- **Darkness.** A `ViewportEvent.RenderFog` and `ComputeFogColor` presentation while inside the pavilion at a stage
  above Unaware: fog colour toward near-black crimson, far plane pulled in moderately by stage (never under 24 blocks
  so the ring stays visible), scaled by `darknessEffectScale`. Spotlights are additive cones from the roof down to the
  ring, drawn in the world pass. Real block light is not changed.
- **Replaced.** The figure planes, cloth quads, diamonds and bone border go. The NPC shake (`motionJitter`) goes; the
  motion echo becomes the Disturbed-only occasional stutter.
- **Code home.** `client/render/world/circus/` for the show (marks, lights, phantoms, spotlights), the fog presentation
  in `client/event/`, the vignette as the existing HUD layer. `CircusPerceptionOverlay` keeps the state and the scan
  and calls into them.

## Builder-check calls (mine, 2026-11-16)

- **Phantoms never cover a real creature.** A phantom whose screen bounds overlap a living entity (not the viewer)
  fades out over ~0.3 s and back when clear: one entity query per phantom batch per scan of nearby living entities
  inside the cue range, not per frame per phantom.
- **The fog grades colour more than it hides.** Far plane never under 32 blocks; hostiles beyond it fade as anything
  does. No entity exemption.
- **Anchors.** `circus_king_pole` and `circus_quarter_pole` are `TroupePoleBlock`s with a `section` property; the scan
  takes the `finial` (or top) section as the light anchor. `circus_seating` facing gives the look direction. All exist
  in the pavilion template today; no template change.
- **Inside.** The show and the fog run only while the server's existing `inside` flag is true and the scan found a
  ring curb; with no curb it draws nothing.
- **Seats.** Disturbed fragments use seats and gallery decks alike. Applause is visual (hands up, a sway) on an
  act's beat, Attuned only.
- **Audience bodies.** A mix: the faculty body for most seats plus the slimmer performer bodies (acrobat, knife
  thrower) for variety; each body used gets one seated pose.
- **Stutter.** Only `CircusPerformerRenderer` (where the jitter lives today).

## After-states (owner ruling, 2026-11-16)

- **Ruin (Liberated pavilion): a hollow echo.** Whatever the player's stage, only Disturbed-style fragments: a few
  audience glimpses, dead bulbs, no performers, light darkening only. The show is over; the place remembers it.
- **Tenancy (Succeeded pavilion): the full show as normal.**
- How the client knows: prefer reading it from the scan (the ruin's own debris blocks); if that is not reliable, one
  site-state field is added to `PacketSyncCircusPerception` (the one permitted server-side change: protocol bump and
  payload-registry rewrite), still presentation only.

## Left to the builder

- Exact counts, alphas, colours, cone sizes, timings within the table above.
- Which model plays which act, and the seated pose angles.
- How a posed model is fed without an entity (a stub entity instance cached once is acceptable if a model's
  `setupAnim` needs one).
- Shader or plain render types, as long as they follow the caching rule.

## Verification

- Unit tests for the pure rules: stage to counts/alpha table, seeded bulb states, the peripheral-fade angle test,
  catenary points.
- The existing `circusReviewClient` (`CircusClientReview`) captures the pavilion interior at each of the four stages,
  day and night, from the floor and from a gallery; the owner judges the stills.
- Guardrails stay green: world effects, HUD layer, animation time, render-type caching, vanilla particle ratchet.
- Docs: `docs/CIRCUS.md` (perception paragraph and a dated build entry), the matching paragraph of
  `docs/HEMOMANCY_REFERENCE.md`, and `docs/OPEN_QUESTIONS.md` for the sound pass.
