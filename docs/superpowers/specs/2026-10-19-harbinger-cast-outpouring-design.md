# Harbinger Cast Outpouring — design

Date: 2026-10-19. Status: approved and built; revised after the user's first live review (see Revision 1).

## Intent

Today a Harbinger cast puts several things on and around the caster: vein strands on the arms, pulsing beads on the hand, and from Summa rank up a rank-scaled construct hovering behind the player. The constructs are a heart, a furnace, a nerve spine, angel wings for Lux, a cadaver, an ice-crystal ring, iron columns, and a shadow figure. They were built by earlier, weaker passes. They don't belong in Hemomancy: they are abstract shapes floating near the player rather than blood leaving a body.

Replace all of it, from scratch, with the **Cast Outpouring**. The caster's own blood leaves the body at the vascular section the manipulation draws from. Shader veins light up under the skin around that site. The blood then transforms according to the tendency: it freezes, ignites, hardens, whips, swirls, oozes, shines or smokes. It gets wilder and larger as rank rises. Where it touches the player or nearby blocks it leaves a fading mark. Shaders do the heavy lifting; the old stacks of small modelled pieces go.

The user does not want the old effects adapted. Read them only to find what to delete and what wiring to reuse.

## Goals

- Every Harbinger cast (charge, channel, quick, instant) shows blood leaving the caster from the manipulation's vascular section.
- Shader veins on the player's skin radiate from that section, coloured and textured per tendency.
- Each of the eight tendencies has its own outpouring behaviour (see the per-tendency section).
- Rank (HUMILIS → PERFECTUS) scales volume, reach, wildness and mark lifetime. Rank 1 is a trickle with faint veins; Perfectus is a full outpouring that spreads marks into the surrounding world.
- The effect builds for the whole charge or channel, peaks at release, then plays out its aftermath and fades.
- Blood that touches blocks or the player leaves a tendency-specific mark that fades. The marks are visual only.
- Lux lights its surroundings with a shader glow, not real block light. Tenebris darkens its surroundings and rolls fog over them.
- Other players see the full effect. The caster sees it in first person too, scaled so it never blocks their view.

## Non-goals

- **Unstained Still Arts.** `StillArtRings` (the hand ring and the ground ring) and the Still Art path through `CastingAnatomyRenderer.stillHand` / `stillGround` stay exactly as they are.
- **Spell-specific charge and form visuals** in `ManipulationVisualRenderer` / `ManipulationSignatureEffects`, such as the Crimson Coronation crown of swords, charge-form geometry and lingering-form ambients. They belong to the spell, not to casting. The outpouring plays underneath them.
- **`SchoolStateVisuals`** status meters and the `ThermalSkinLayer` / `MortemSkinLayer` spell skins. They are driven by spell state, not by casting.
- **Cast poses and animation tracks** (Casting Animations v2). This replaces the effects drawn on the body, not how the body moves.
- **Shader-pack (Iris/Oculus) compatibility.** The user explicitly ruled it out, so full-screen depth-reading passes are allowed.
- **Real world changes.** No fire, freezing, block damage or entity damage from outpouring contact for now. See "Real-effect hook".
- **Non-player casters** (Drudges, mobs). Only players receive `CastingAnimationPacket`, and the outpouring follows that packet.

## Anti-goals

The design has failed, even if it technically works, if any of these happen:

- Anything floats behind or above the player as a detached construct: wings, halos, figures, rings of shapes, emblems.
- The effect reads as "magic particles around a player" rather than "this person's blood is coming out of their body and turning into something".
- Generic vanilla particle clouds dominate the effect. Vanilla `ParticleTypes` are not used at all. HutosLib/Hemomancy particles are accents only; shaders and geometry carry the effect.
- Brass, bronze, copper or silver tones appear. Those belong to the Unstained/antibiotic side. Use the blood palette plus each tendency's material colours.
- The caster's own first-person view is walled off (Tenebris fog, Lux glare, blood across the camera).
- A cast permanently changes the world or saves anything to disk.

## What gets removed

Delete, don't adapt:

| Item | Where |
|---|---|
| Hand veins (1–2 strands per arm) and the blood/accent hand beads | `CastingAnatomyRenderer.hand()` (the Harbinger path; keep the `stillHand` dispatch) |
| Chest vessels plus all per-school body constructs: `heart`, `furnace`, `nerves`, `cadaver`, `crystal`, `foundry`, `shadow`, `vessel` | `CastingAnatomyRenderer.body()` and its helpers (keep the `stillGround` dispatch) |
| Lux wings | `LuxCastingWings` (whole class) and `LuxCastingWingsTest`. Its render type is still used by `StillArtRings`, so move the render type to `StillArtRings` or to a small shared holder before deleting the class. |
| The generic activation burst on player Harbinger casts | `ManipulationParticles.activate` → `ManipulationAmbientParticles.accept`. Suppress it when the outpouring will draw (the caster is a player and the cast is Harbinger). Keep it for non-player casters and for the impact path. |

`CastingAnatomyRenderer` can shrink to a Still Art dispatcher, or be renamed so its job is clear. Either is fine, but keep the source tests that pin the Still Art slice (`StillArtRingsTest`, `StillArtPresentationSourceTest`) green by updating their markers.

## Inputs the client already has

- `CastingAnimationPacket`: `school` (tendency), `secondary`, `rank`, `phase` (OPENING, HOLD, RELEASE, RECOVERY, CANCEL), `started`, `updated`, `held`, `required` (so charge progress is `held / required`), `hand`, `lane`, `manipulation` id.
- **Vascular section is not in the packet.** Resolve it client-side with `ManipulationInit.getByName(packet.manipulation()).getSection()`, the same lookup `CastingAnimationClientState.passive()` already uses. No protocol change is needed. If the lookup fails, fall back to ARMS.
- `CastingAnimationClientState.animation(player)` exposes `weight(partial)` and playback timing. Only render Harbinger-lane animations; Unstained keeps its rings.
- Model anchoring: `CastingAnimationLayer` already has the parent `PlayerModel` with the posed parts, and `CastingAnimationClientEvents.hand` already has the first-person posed arm.

## Timeline

Decided: build through the whole charge or channel, peak at release.

- **Build (OPENING/HOLD):** the strength is `held / required` for CHARGED casts.
- **QUICK casts** (68 of 108) *(my call)*. They send only a RELEASE packet after the spell has already fired, so there is no build to show. They peak on arrival with a token 1–2 tick swell. Visual timing never waits on, or lags, gameplay. No protocol change and no pre-cast cue.
- **CONTINUOUS channels** *(my call)*. They have no progress value (held = required = 0). Strength ramps from OPENING to a near-peak over a rank-scaled ~10–20 ticks, then stays at a living near-peak through the HOLD heartbeats. If the channel stops with RELEASE, a final burst plays (the peak) and then the aftermath. If it stops with CANCEL, the cancel rule applies.
- **PASSIVE toggles and living-staff form toggles** *(my call)*. Toggle-on (RELEASE) plays a short, reduced outpouring: veins plus a brief exit flow, with no world marks beyond the feet. Toggle-off (CANCEL) plays the drawn-back cancel. Skip the skin veins while a blood-avatar stage encases the body.
- **Veins at the build stage:**
  - Veins appear first at the section and spread outward as the strength rises.
  - The blood starts as a trickle and visibly changes into the tendency's material as charge progresses.
- **Peak (RELEASE):** a burst. Rank decides its size, reach and wildness. This is the moment most world contact (and therefore most marks) happens.
- **Held channels:** sit at a steady near-peak with a living, cycling motion for as long as the channel holds.
- **Aftermath (RECOVERY and beyond):** the blood finishes its behaviour, then fades. The Animus pools snake about, Mortem sludge drips off, Tenebris fog rolls away, Ferric plates crack and flake. The aftermath may outlive the pose animation by a rank-scaled number of ticks. Marks then fade on their own timers.
- **Cancel:** *builder's call, suggested:* the blood is drawn back into the veins at the section over a few ticks, and the veins fade. No burst, no new marks.

## Vascular sections

The section decides where the blood leaves, where the veins centre, and where marks on the player's own body start.

| Section | Exit sites | Vein field |
|---|---|---|
| HEAD | Eyes, nose, ears, temples | Face, temples, neck |
| HEART | Left-centre chest | Chest radiating to shoulders and neck |
| BODY | Flanks, abdomen, back | Torso front and back |
| ARMS | Forearms and palms. The lead (casting) hand always; both arms from MEDIOCRITAS, matching the existing `FirstPersonHandRules.affectsOffHand` rule | Hands to shoulders |
| LEGS | Thighs and calves; blood runs down to the feet and ground | Hips to feet |

## Veins (skin shader)

- Re-render the posed parent `PlayerModel` with a new casting-veins render type (NEW_ENTITY format, translucent, LEQUAL depth, no cull), following the `ThermalSkinLayer` precedent.
- The vein pattern is procedural in the fragment shader. Mask it by body region using the standard 64×64 player-skin UV layout (head, body, arms and legs regions, both layers). It grows outward from the section's exit sites as strength rises.
- Encode inputs in vertex colour and uniforms as the existing skin shaders do: tendency, section, strength, time and fade.
- **Per-tendency look:**
  - Animus: hot red, fast pulse.
  - Flammeus: ember orange cracks.
  - Congeatio: pale frost-blue rimed veins over red.
  - Ferric: dark iron-grey raised veins.
  - Ductilis: veins that crackle and flicker with electric gold.
  - Mortem: dark green-brown necrotic veins.
  - Lux: red-white veins that throb with light, glowing brightest of all.
  - Tenebris: black-violet veins that smear and smoke.
- **The same shader carries "body contact" marks:** frost, scorch, corrosion, iron crust or blood smear creeping over the player model from the section during the peak, then fading in the aftermath.
- **Armour (decided):** veins and body-contact marks show over armour too. Add a second overlay pass on the worn armour models: humanoid armour layers, using their own UV layout and a region mask per armour piece, driven by the same section, strength and tendency inputs. Bare skin and armoured slots must read as one continuous vein field. Modded armour on the vanilla humanoid armour model gets it automatically. Custom-model armour may be skipped.
- **First person:** the posed arm gets the same overlay where the arm is visible. Other sections show nothing in first person.

## Outpouring (the blood itself)

- **One consistent blood material system.** All outpouring geometry draws on new per-tendency shader materials under `shaders/core/world/` (registered in `ShaderInit` like the existing `manipulation_*` materials). It starts as wet blood and transforms into the tendency's material over the build.
- **Prefer shader-driven shapes over hand-modelled stacks.** Ribbons and streams along paths, blobs and droplets as SDF-shaded cards or low-poly lathes, and volume cards for fog and vapour. The `LuxUmbraGeometry` primitives (`ribbonPath`, `card`, `stream`) are an acceptable base.
- **Blood is simulated client-side** as a modest set of drops and streams, with gravity and simple block collision against the client level. Collisions are what spawn marks. Keep the simulation in pure rules classes where it doesn't need Minecraft types, so JVM tests can cover it.
- **Anchoring follows the Riposte standard** (`docs/MANIPULATION_VISUALS.md`): root on the posed body part and scale by entity scale. In first person, skip the near part so the effect never walls off the view.
- **Secondary tendency:** a small accent only, such as a minority of drops or a fringe colour. It is not a second effect.
- **Spells that already draw their own blood on the body** *(my call)*: Hematic Riposte's arm, Crimson Coronation, the blood-avatar stages and the living-staff forms. Add a per-manipulation presentation flag that reduces the outpouring to veins plus the exit wound, so the spell's own blood reads as coming out of that wound instead of doubling up. Audit the roster for other on-body spell geometry and list the flagged ids in a test.
- **Entities** *(my call)*: drops collide with blocks and with the caster's own body only. Ductilis strands that pass through another entity spark there (a custom-particle accent), but no marks are laid on other entities.

## Per-tendency behaviour (user's vision)

| Tendency | Behaviour | Fading mark |
|---|---|---|
| **Congeatio** | Blood pours out and freezes into reddish ice: shards, icicles and rime crusts. | Frost spreading over blocks and the player |
| **Flammeus** | Blood rises and ignites into flammeus flames. | Scorch on blocks and on the player where it touches |
| **Ferric** | Blood hardens into solid plates and spikes. | Iron crust and rust-dark residue |
| **Ductilis** | Blood whips around wildly in lashing strands and snaps. | Spark burst plus a scorched crackle mark where it hits blocks or entities |
| **Animus** | Blood pours out quickly and pools, whirling round the player in a lively whirlpool. | Pools that keep quivering and snaking about before fading |
| **Mortem** | Blood oozes slowly in gross blobs and sludgy strands. | Dark green-brown corrosion stains |
| **Lux** | Veins glow and pulse; red-and-white orbs of light bud from the blood. | Glow on surfaces (screen pass, no block light) |
| **Tenebris** | Blood turns into black-violet fog that rolls off the player in a dark cascade. | Light drained from the area; fog hides the player and blocks |

Exact shapes, counts and motion are the builder's, held to the Riposte layering bar.

## Rank scaling

Rank scales every axis together. These are starting points the builder may tune:

| Rank | Veins | Outpouring | World contact |
|---|---|---|---|
| HUMILIS | Faint, near the exit site | A trickle from one exit site | Rare, small marks; short life |
| MEDIOCRITAS | Clear, spreading | Steady flow; both arms for ARMS | Marks near the feet |
| SUMMA | Bright, covering the section | Strong outpouring with clear material change | Marks within about 2 blocks |
| MAGISTER | Spill into neighbouring sections | Wild, large, multiple exit sites | Marks within about 3–4 blocks |
| PERFECTUS | Most of the body | Full outpouring; the most dramatic shapes and motion | Marks within about 5 blocks; longest life |

## World marks

- **Client-only, never saved.** They are decal quads laid on the block faces where simulated blood landed, using per-tendency shaders that grow in, hold, then fade. Marks on the player use the skin shader above.
- **Global cap:** about 256 marks. When full, drop the oldest. Per-cast caps scale with rank. Clear on level change.
- **Reuse is allowed:** `BlockShellFaces`-style face emission, and the existing `blood_pool` / `infected_pool` shaders where they fit (for example the Animus pools).
- **Animus pools** are marks with motion: they quiver and creep in a slow snake path along the surface before fading.

## Lux and Tenebris screen passes

- **Lux:** a depth-aware full-screen pass reconstructs world positions and adds red-white light falloff around the caster and each live Lux orb, with a cap on light count. It pulses with the veins. No real block light.
- **Tenebris:** a depth-aware pass darkens and desaturates surfaces within a rank-scaled radius of the caster. It pairs with rolling fog volume cards that hide the caster and nearby blocks. For the caster's own view, use a reduced strength and keep a clear centre so they can still see.
- **Precedents for copying the main target and drawing a full-screen quad:** `SanguineOmenOverlay`, `FaneBoundaryRenderer`, `AxonalTravelView`.
- When several Lux/Tenebris casters are near, combine them into one pass per type rather than one pass per caster.

## Real-effect hook (built, off)

Decided: write the code for real world effects now, but don't use it yet.

- Add a server-side hook keyed by tendency, called at release with the caster, the section, the rank and an approximate contact set. Examples: Flammeus sets fire, Congeatio places snow or frost, Mortem corrodes.
- Gate it with a server config value, a minimum rank for real effects, defaulting to "never". Ship no-op implementations. Nothing may change the world unless the gate opens.
- Visual timing never gates gameplay. Contact points for the real effects are computed server-side, not taken from the client simulation.

## Performance and settings

- **Particle setting:**
  - MINIMAL: veins only.
  - DECREASED: veins plus a reduced outpouring, no world marks beyond the feet.
  - ALL: everything.
- **Distance:**
  - Over 24 blocks: veins and a reduced outpouring.
  - Over 32 blocks: veins only.
  - Over 48 blocks: nothing.
  These mirror the existing rules.
- Batch all outpouring geometry per material per frame, not per caster.
- Hide everything when the caster is invisible.

## Tests and verification

**Delete or update:**
- Delete `LuxCastingWingsTest`.
- Update the source-slice markers in `StillArtRingsTest` and `StillArtPresentationSourceTest`.
- Keep `CastingAnimationPacketTest`, the track tests and `FirstPersonHandRulesTest` green.

**New JVM tests for the pure rules:**
- Timeline strength by phase and charge progress (including the instant-cast compression).
- Rank scaling.
- Section-to-exit-site mapping and the off-hand rule.
- Drop simulation and contact.
- Mark cap and eviction.
- The real-effect gate defaulting to off and refusing every rank.

**Source-contract tests:**
- The removed classes and methods are gone.
- No vanilla `ParticleTypes` appear in the new classes.
- The new layer is registered on player skins.
- The Still Art dispatch is untouched.

**Live client:**
- Shader compile errors on NVIDIA aren't caught by glslangValidator, so launch a client to prove the new shaders compile.
- Drive `/hemocastpreview` (or a dedicated review client) through every tendency × section × a low, middle and top rank.
- Capture third-person and first-person screenshots, including a charge build-up, a release peak and a held channel.
- Gate builds with `./gradlew.bat build test` and `alphaCheck`.

## Docs to update with the build

- `wiki/Casting-Animations.md`: the "Anatomy and effects" section, the rank scope, the Lux wings column and the rendering class table.
- `wiki/Casting-Animation-Validation.md`
- `docs/MANIPULATION_VISUAL_CATALOGUE.md`: the casting-anatomy and wings rows.
- `docs/HEMOMANCY_REFERENCE.md`, wherever casting presentation is described.
- Note in the Casting Animations v2 spec and plan that the anatomy, wings and per-school constructs they describe have been replaced by this design.

## Context and constraints

- This builds on the uncommitted Casting Animations v2 work in the working tree. Do not revert or reshape those files beyond the effect-drawing calls.
- NeoForge 1.21.1 idioms only.
- Hemomancy/HutosLib custom particles only, as accents.
- No copper, brass, bronze or silver tones.
- Do not commit; leave the work in the working tree.

## Left to the builder

- Exact shader maths, noise and vein generation.
- Mesh counts, simulation parameters, timings, colours within each tendency's described look, mark sizes and lifetimes.
- Class names and package layout under `client/render/world/` and `client/render/layer/`.
- Whether the outpouring uses a dedicated review client or extends `/hemocastpreview`.

## Revision 1 (user review, 2026-10-19)

The first build used its own material and mark shaders. The user found the result blocky and unlike the rest of the mod. Their direction, now built:

- **Reuse the mod's visual toolkit** instead of a separate material system. `client/render/world/OutpouringBrushes` gives the outpouring access to the shared material batch and geometry: living blood, flame, molten, cruor and glass, blood-iron, ductilis, mortem, lux and umbra. It also exposes the school particles and the HutosLib Ductilis lightning. The `cast_outpouring` and `cast_mark` shaders were removed.
- **Veins** must look like the floor-shader veins at screen resolution, not pixel-matched. The atlas now records each texel's body axes so the shader rebuilds sub-texel body points. The vessels use the Cardinal floor membrane's artery and capillary web.
- **Per school:**
  - **Animus:** brazier-blood pools (merging and growing), with the whirling blood as a whirlwind of the Riposte's wisp mist.
  - **Flammeus:** flammeus flames and particles (reddish pink, black, orange).
  - **Ferric:** the ferric material, with riveted plates and finer forged spikes.
  - **Congeatio:** ice that reads as ice (glass shard clusters, frost clots, cruor frost cracks).
  - **Mortem:** mortem sludge at resolution.
  - **Lux and Umbra:** more, smaller particles.
  - **Ductilis:** branching lightning trees rather than many separate bolts.
