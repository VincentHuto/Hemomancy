# Boss presentation: bar frames and opening scenes

Date: 2026-10-24. Status: the user approved building on my proposal without a question round, so every specific
below is my call unless marked **(user)**.

## Intent

Every Hemomancy boss currently shows the plain vanilla boss bar, and only the Mycophant has an opening (its waking,
built as one-off code). The user wants each fight to feel authored: a boss bar frame of its own, and an opening scene
that brings the boss in before the fight starts. **(user)** The Mycophant's proto waking is the model for that and is
the first scene moved onto the new system.

## Goals

1. **One shared opening-scene system** (the *director*) that any boss plugs a scene into. It handles
   letterboxing, fades, hiding the HUD, a title card (name and epithet), input lockout, an optional cinematic camera,
   camera shake and angle offsets, and the hand-off to the fight.
2. **A bar frame for each boss**, drawn in place of the vanilla bar. Each has its own motif and palette, a lagging damage
   chip, phase notches, a reveal animation when the bar first appears, and a change of look when the boss changes
   phase.
3. **The Mycophant waking and rematch run through the director.** They keep every beat they have now (lids, fibers,
   pose, shakes) and gain the letterbox, the hidden HUD, the title card at the roar and the framed bar reveal. The
   Gardens capture runs as a director scene too, so one system owns the camera and input.
4. **An opening for every boss.** Phase changes that already have a staged scene (Mycophant split, Naeglerophaeon
   transition, the Vesper transformation, Annetta's break) get a short *phase card*: a letterbox and a renamed title
   card, with no camera takeover.

## Non-goals

- New combat moves, AI or balance changes. Openings add invulnerable time only where a boss has none and the scene
  needs it.
- New boss models. Openings reuse existing poses (Mycophant intro pose, Priestess emergence, Latent emergence,
  Naeglerophaeon `NERVE_EMERGE`, Evening Star awakening). A boss without an intro pose gets a short procedural one in
  its existing model code, never a regenerated model.
- Dialogue rewrites. Title-card names and epithets are new lang keys only.
- Unstained ritual content and MnA compat.

## Anti-goals (a failure even if it works)

- A scene that takes the camera mid-fight or traps the player while the boss can hurt them. Input is locked only while
  the boss is invulnerable and passive.
- Vanilla particle clouds as the visual of a scene (AGENTS.md and the custom-particles memory).
- Copper, brass or bronze accents on blood-side frames.
- A frame that is unreadable: the health fill must stay the dominant, readable element at GUI scale 2 and above.
- Desync: server timing stays authoritative. The client scene is presentation only; skipping it changes nothing on the
  server.
- Repeated long cutscenes. Rematches use the short form, and any scene can be skipped by holding sneak (client only).

## Constraints (from AGENTS.md and the architecture standards)

- HUD drawing is a layer in `GuiLayerRegistrar`; no `RenderGuiEvent`. World drawing goes through `WorldEffectDispatcher`.
- Payloads follow `PacketHandler` rules: bump `PROTOCOL_VERSION` and rewrite `payload-registry.txt`.
- Player-facing text is lang keys only.
- `common/` names no client class.
- Client caches clear in `ClientSessionEvents` on logout.
- Easing comes from `HemoEasing`, and animation time from `AnimationTime`.
- A synced boss tick is replayed with `BossReplayClock`.
- The mixin list stays in step with `SourceTreeHygieneTest`.

## Design

### Bar frames

- **Binding.** `BossBarBinding` takes a `BossBarFrame` id (a `ResourceLocation`, e.g. `hemomancy:mycophant`). On
  `show(player)` it sends `BossBarFramePacket(barUuid, frameId)` after vanilla adds the bar. The frame is tied to the
  bar's UUID, so it survives renames and colour changes.
- **Client state.** `BossBarFrameClientState` keeps the UUID-to-frame map and, per bar, the lag-chip progress and the
  first-seen tick. Entries are pruned when vanilla stops listing the bar and cleared on logout.
- **Drawing.** One `CustomizeGuiOverlayEvent.BossEventProgress` subscriber (`BossBarFrameRenderer`) draws framed bars.
  It cancels the vanilla draw, paints the frame, and sets the increment so stacked bars don't overlap. Bars without a
  frame are left to vanilla. A client config toggle `bossBarFrames` (default on) turns all of this off.
- **Layout.** The fill keeps vanilla's 182 px width at 7 px tall. The frame is about 206×22, with the name centred above
  in the boss's text colour and the increment at 30.
- **Shared painter.** `BossBarFramePainter` draws the parts every frame shares:
  - recessed well;
  - gradient fill;
  - damage chip: the lost segment stays lit for 16 ticks, then drains at a fixed rate;
  - notches at the frame's thresholds;
  - reveal: the frame draws outward from the centre over 20 ticks, then the fill rises.
- **Per-boss art.** Each boss has a small `BossBarFrame` implementation: palette, notches, end caps, centrepiece, and how
  it reacts to the phase. The art is GUI quads (`drawManaged`, one flush), the same technique as
  `MycophantMyceliumOverlay`, so frames can animate. There are no new textures.
- **Phase.** A frame reads phase from data the client already has: progress against its thresholds, the bar colour (the
  Naeglerophaeon recolours per phase) and the bar name's translation key (`.joining`, `saint_hemorath_the_vessel_empties`,
  `vesper_the_shamed_evening_star`).

| Boss (bar key) | Frame motif (my call) | Notches | Phase reaction |
|---|---|---|---|
| Mycophant | hyphae-wrapped bone rails; amber gestation sacs at the end caps; folded frill collar as the centrepiece; crimson fill with amber chip | 0.5 | at `.joining` the collar splits into two halves, and wing-lattice fiddleheads curl out of the caps |
| Annetta Knowles | pale chipped church wood with ichor drips from the lower rail; a small black-iron reliquary as the centrepiece | — | the BREAKING state has no bar change; the bar is hidden when she rises |
| Stained Priestess | blood-clot rails; tendril wings spread from both caps; lancea-blue spike as the centrepiece | 0.5 | below 0.5 the wings flare and the spike glows |
| Latent Infection | coiled pale tendrils wrapping a thin rail; spore motes | — | — |
| Naeglerophaeon | synapse rail of nodes joined by flickering magenta nerve arcs | 0.5, 0.1 | arcs recolour with the bar (purple, yellow overload, white drained) and arc rate rises |
| Vesper, Crowned Refusal | umber-maroon scute plates; a thorned crown centrepiece; ice glints | 0.72, 0.42, 0.12 (the wounds) | each notch passed cracks a scute open with an ember glow |
| Vesper, Evening Star | black lacquer rails with a four-ray white star centrepiece; blood fill | 0.67, 0.33 (acts) | rays lengthen per act; when shamed the star dims and the fill shows the absorption |
| Saint Hemorath | hollow iron vessel rails, bloodless (an empty well behind the fill) | 0.5 | when the vessel empties the rails tarnish violet |
| Saint Putriciel | rot-bloom petals on mossed rails | 0.5 | petals open as absolutions fill |
| Saint Velorum | frost-rimed rails with icicle drips; tenebrous centrepiece | 0.25 | below 0.25 the frost spreads over the fill edge |
| Saint Seraphae | chain links around a radiant core | — | chain links visibly loosen as integrity falls |

### The director (opening and phase scenes)

- **Start.** A scene is started by `BossScenePacket(sceneId, bossEntityId, startOffset)`. The server sends it to every
  player who will see the bar, within 64 blocks of the boss. The Mycophant keeps `PacketMycophantIntro`; its
  WAKING, REMATCH and CAPTURE kinds start director scenes, and BLOOM, WATCH, GLIMPSE and CLEAR stay as they are.
- **Scene definition.** `BossScene` is a client definition: a length in ticks plus a set of tracks, each a function of
  scene time:
  - `letterbox(t)` (0..1 bar height)
  - `fade(t)` (black alpha)
  - `hudHidden(t)`
  - `inputLocked(t)`
  - `titleCard(t)` → the card's window and lang keys
  - `camera(t)` → none, first-person offsets (roll, pitch, yaw, shake), or a cinematic shot
  - `overlay(graphics, t)` for scene-specific art (the Mycophant's lids and fibers)
  - `tick(t)` for one-shot client cues (sounds, forced poses)

  Scenes are registered by id in `BossScenes`.
- **Cinematic shots.** A shot is a list of keys `(tick, yawAroundBoss, distance, height, lookHeight)`, measured
  around the boss's interpolated position and body yaw and eased with `HemoEasing`. During a shot the director switches
  to third person, so the player model renders and the hand is hidden, and restores the previous camera type after.
  - A new client mixin, `MixinCamera`, places the camera at the end of `Camera.setup`.
  - Rotation goes through the existing `ViewportEvent.ComputeCameraAngles`.
  - If the boss entity is missing or more than 96 blocks away, the shot falls back to the player's own view.
- **Shared visual pieces.**
  - The letterbox draws as two black bars easing in and out.
  - The title card sits on the lower third: the epithet in small caps above, the name large below, a thin ornamental
    rule in the boss's palette that draws outward, and a 12-tick fade at each end.
  - HUD hiding cancels the vanilla HUD layers (hotbar, health, food, air, experience, crosshair, effects, boss bars)
    through `RenderGuiLayerEvent.Pre` while `hudHidden`. Hemomancy's own layers stay unless they belong to the scene.
- **Input.** Movement input is zeroed through `MovementInputUpdateEvent` while `inputLocked`. Server rules still decide
  invulnerability and movement; the Mycophant keeps zeroing velocity on the server.
- **Skip.** Holding sneak for 20 ticks ends a scene's camera, letterbox and card. The input lock stays if the server
  still binds the player (the Mycophant waking before CONTROL).
- **Config.** `cinematicBossScenes` (default on): off keeps letterbox and card, but no camera takeover. `bossSceneShake`
  (default on).
- **Lifetime.** One scene at a time. A new scene replaces the old one. It clears on logout, on dimension change and on
  `CLEAR`.

### Per-boss openings (my calls; timings are targets, tuned in review)

| Boss | Trigger (existing) | Scene | Server change |
|---|---|---|---|
| Mycophant (waking) | `enterNursery` | the current 220-tick waking; letterbox from 0, cleared by CONTROL (130); HUD hidden until CONTROL; title card at ROAR (150) for 40 ticks; the framed bar reveals at ROAR | none beyond the packet route |
| Mycophant (rematch) | `enterNursery` rematch | 61 ticks: a short cinematic shot circling from her back to her face through the turn, card at ROAR (20) | none |
| Mycophant (capture) | `beginClaim` | the current capture, now owned by the director (no card, no letterbox) | none |
| Annetta Knowles | `startHarbingerRoute` | 60 ticks: a shot from behind her shoulder that swings to her face as she draws dagger and shield, card at the scream | the scream window grows to the scene length: she is invulnerable and passive for 60 ticks (`AnnettaEncounterRules`) |
| Annetta's break → Priestess | `startBreak` … Priestess `EMERGING` | phase card at the break (no camera; the player keeps control). At the Priestess's emergence, a 90-tick shot low at the pool, rising with her and holding on the scream at STAND_END, then a card | none (she is already invulnerable while emerging) |
| Latent Infection | cure route `beginEmerging` | a 34-tick shot of the tear (90 → 124) and a card at the end | none |
| Naeglerophaeon | `VagrantMindEncounterManager` spawn | 70 ticks: it starts in `NERVE_EMERGE` out of the node, with a shot tracking the nerve-lightning, and a card | a new intro window: the first 70 ticks are `NERVE_EMERGE` with invulnerability and no attacks |
| Vesper, Crowned Refusal | `VesperOrdealManager.enter` | 80 ticks: a long shot down the arena axis onto the mount, which rears (a procedural rear in the mount anim state), then the rider's raised crown and a card | a new intro window of 80 ticks: invulnerable and passive, and the bar shows at the end |
| Vesper transformation → Evening Star | `breakAnchor` → `spawnEveningStar` | phase card at the cocoon burst; the Evening Star's existing awakening (now actually started by `spawnEveningStar` calling `beginAwakening`) gets a 72-tick shot of the growth and the sigils, then a card | wire `beginAwakening()` (currently never called) |
| Saints (all four) | `HarbingerSaintEncounterHooks.spawnSaintBoss` | one shared 60-tick *rise*: the boss rises out of the sarcophagus footprint (a render offset from below plus a dust ring from the Hemomancy toolkit), with a shot from the foot of the sarcophagus and a card | a shared intro window in `SaintTrialEntity` (Seraphae gets the same through a small helper): invulnerable and passive, with the bar hidden until the end |

Phase cards (letterbox and card, 50 ticks, no camera, no input lock):

- Mycophant split ("The Mycophant, Joining")
- Naeglerophaeon transition
- Hemorath's vessel emptying
- Vesper's cocoon burst

### Title cards (lang keys `boss.hemomancy.<id>.name` / `.epithet`; wording mine, to review against `docs/NPC_VOICES.md` and `docs/LORE_REFERENCE.md`)

| id | Name | Epithet |
|---|---|---|
| mycophant | The Mycophant | Mother of Every Strain |
| mycophant.joining | The Mycophant | Joining |
| annetta_knowles | Annetta Knowles | Last of the Broken Church |
| stained_priestess | The Stained Priestess | What Wears Annetta |
| latent_annetta_infection | The Latent Infection | Torn Out of Annetta |
| naeglerophaeon | Naeglerophaeon | Predator of the Vagrant Mind |
| vesper_crowned_refusal | Vesper | The Crowned Refusal |
| vesper_evening_star | Vesper | The Evening Star |
| hemorath | Saint Hemorath | The Hollow Vessel |
| putriciel | Saint Putriciel | The Rotting Saint |
| velorum | Saint Velorum | The Frozen Martyr |
| seraphae | Saint Seraphae | The Bound Radiance |

## Verification

- JVM tests:
  - scene track timing (letterbox, card window, input-lock windows match the server rules: the Mycophant lock ends at
    CONTROL, each new intro window matches its server constant);
  - the frame chip and reveal math;
  - every `BossBarBinding` names a registered frame;
  - every scene id the server sends is registered client-side (source scan);
  - lang keys exist.
- Guardrails: payload registry and protocol bump, mixin list, english literal and HUD guardrails stay green.
- `./gradlew.bat build` and `test`.
- A review client, `runBossPresentationReviewClient` (a `gameTest` review like `MycophantClientReview`), with scenes
  that spawn each boss with its frame and play each opening. It captures screenshots at the card and bar-reveal beats.
  The existing Mycophant review gains a `waking_director` scene.

## Left to the builder

- Exact pixel art of each frame within the motif table.
- Exact shot keys and timings within the targets.
- How each boss with a new intro window stops attacking. Prefer its existing passive/NoAi path: `beginAwakening` for
  Vesper, the `EMERGING` mode pattern for the Priestess.
