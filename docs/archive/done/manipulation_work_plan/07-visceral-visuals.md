# Eight expressions of blood magic — visual implementation

Lux and Tenebris material descriptions below are historical. Their current shader-driven treatment is documented in [Lux and Umbra flow visuals](08-lux-umbra-flow.md).
This pass implements the approved **visceral spellcraft / school palettes** direction on top of Area 5/6. Mechanics, ranks, progression, damage, targeting and save identities are unchanged. The only new server-side information is the existing sword-impact visual packet's direction, derived from projectile velocity.

## Materials and subjects

The effect renderer now uses eight 32×32 grayscale material textures, with at most eight values per texture and school color supplied by vertices. Medium-scale pixel clusters carry veins, cracks and surface highlights. The user selected 32×32 after finding 64×64 too dense and 16×16 too plain. Texture mapping is capped at one tile per block on mesh faces so small subjects do not compress a full sheet into a tiny surface. Texture coordinates move along liquid and luminous surfaces. The standard textured-color shader supplies the surfaces; no external shader pack or fullscreen bloom is required. The material reference was generated with the built-in image tool, then imported onto the native effect grid with `tools/import_manipulation_materials.py`. The source atlas and enlarged grid are in `evidence/visceral/`.

| School | Representative and shared treatment |
|---|---|
| Animus | Coronation has tapered textured blades, dark fullers, curled guards, blood-fed formation and short launch ligaments. Fired swords use the same mesh. Blood Shot forms have liquid bodies and tapered tails. Blood Binding keeps its authored topology and retraction, with the Animus material on its core ribbons. |
| Mortem | Funeral Bell has inner and outer walls, a flared rim, clapper, patina strands and a damped toll. Drain beads travel from victims toward the caster. Wounds, debts and withered forms use the dried-blood material. |
| Ductilis | Thread Ripper separates into two anchored bundles with recoil and traveling impulses. Conductive Mark and Living Circuit use fine branching fibers and moving beads. Ward uses translucent blood lamellae threaded with conductive fibers. Existing HutosLib electrical effects remain. |
| Ferric | Iron Choir and Retort use overlapping textured lamellae with red stress seams; count remains server-owned. Mending closes metallic sutures. Ore locators retain inexpensive twelve-edge boxes. |
| Flammeus | Vitric Combustion forms irregular, thick shards containing dark inclusions and bright cracks. Furnace produces curling heated strands and droplets. Phoenix retains its bird subject with textured feathers and curved luminous shafts/tails. |
| Congeatio | Stillness spreads frost branches and suspends blood inside pale frozen shells; motion stops after formation. Stop/expiry releases optional fragments without retaining a field boundary. Bone/ice casts grow tapered branching forms. |
| Lux | Verdict has a flowing bright core, translucent sheath, converging charge fibers and fine longitudinal traces of the full damage radius. Contact branches stay at the clipped endpoint. Suture and Beacon share the luminous membrane. |
| Tenebris | Well has low irregular feeding strands, dark throat and inward droplets. Its square footprint remains visible. Veil and teleport effects use curved, thinning shadow strands. |

Saint materials follow current registrations: Tithe is Mortem/Lux; Eye is Lux/Mortem; Bloom is Mortem/Animus; Hour is Congeatio/Lux. They retain their subjects and exact existing HUD debt values.

The shared material and primitive changes reach the remaining Area 5 world cues and their family variants. Existing Avatar, Living Staff, real crimson fire, clouds, pillars, ice blocks, and HutosLib effects remain authored assets; this pass does not replace their models or mechanics.

## Lifecycle and rendering constraints

- Existing packets, charge refreshes, entity interpolation, readiness counts and channel cancellation remain the source of truth. Charge previews resolve saved selections through the manipulation registry so their progress and accepted duration match the gauge and actual cast. No new particle carries readiness or damage information.
- Decorative mesh particles have four behaviors: liquid drops, tumbling fragments, rising vapor and falling residue. The shared pool holds at most 128 particles, lives for 18–28 ticks, culls at 48 blocks, halves under Decreased, and disappears under Minimal. All clear on world replacement/disconnect.
- Essential subjects remain independent of the particle setting. Existing cue capacity (192) and range culling remain in place. Material draws are depth-tested and sorted; transparent surfaces do not mask later surfaces by writing depth.
- Geometry tests cover finite UVs/vertices, frame construction for vertical casts, exact strand endpoints, readiness counts, stopped frost motion, bell shape, Well height, Verdict length/radius, and per-effect/locator vertex budgets.

## Reproducing the visual study

`VisceralVisualReview` is an opt-in GameTest-source fixture. It refuses to modify a world not named `VisceralReview`. Prepare a disposable save under `build/visceral-review-client/saves/VisceralReview`, then run:

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle runClient --console=plain
```

The fixture sends real production visual packets from the integrated server and takes five frames per scene: formation, early action, active action, fading and cleared state. It covers all eight exemplars in a frontal player view, an offset observer view, night, and overlapping effects with Minimal particles. These are **material/packet studies**, not survival-combat, third-person player-model or separate-client acceptance. The existing Area 6 dedicated-server/two-client fixture remains the route for actual casts and remote readiness/launch verification.

## Verification record

- Prior 16-pixel texture/geometry checks: **23 tests passed**, including native texture dimensions, palette size, UV density, readiness counts, beam bounds and mesh budgets. `compileGameTestJava` passed. Log: `build/visceral-pixel-check.log`.
- The prior 16-pixel integrated-client study completed successfully with **160 captures**. Log: `build/visceral-pixel-live.log`.
- Dedicated-server acceptance with the 16-pixel materials completed with **19 assertions passed / zero failures** and both real clients exited successfully. Actual casts covered learning, Blood Shot, rejected Flare casts, protected-target Verdict, Coronation launch, Vitric Combustion, Eclipse Well, Stillness and Funeral Bell. Observer and caster captures are under `evidence/visceral/remote-Observer/` and `remote-Caster/`. Logs: `build/visceral-remote-{server,caster,observer}.log`.
- The broader `alphaCheck --continue` run passed **all 263 required GameTests**, but its JVM suite reported **1,783 executions / 53 failures**. The earlier Area 6 failure ledger remains relevant; this pass does not establish a green full suite. Log: `build/visceral-alpha.log`.
- Independent code review found three regressions before completion: omitted beam-radius cues, reversed drain travel, and excessive ore-locator geometry. These were corrected; beam bounds and locator budgets are now tested.

The retained roster uses the shared material treatment, but every retained model has not been individually certified under every camera angle. The generated atlas is an authoring reference; runtime assets are the small PNGs under `textures/effect/manipulation/`.

## 32-pixel material revision

All eight sheets now use 32×32 pixels and eight grayscale tones, with the current mesh UV density and nearest-neighbor sampling. The atlas has distinct medium-scale structure rather than enlarged 16-pixel clusters. Import this reference with `tools/import_manipulation_materials.py <reference> --row-split 0.48034`; the split compensates for the generated atlas row boundary.

Eight focused signature/material tests passed in `build/visceral-32-check.log`. The mesh, motion and gameplay code did not change in this revision. The earlier full-suite and dedicated-server results above belong to the prior revision.
`runClient` completed successfully with 160 new captures using the 32-pixel assets. See the [updated capture index](evidence/visceral/README.md); the new images are stored separately under `evidence/visceral/study-32/`. Log: `build/visceral-32-live.log`.

## Blood-cell and glow accents

Player casts now emit a brief blood-cell/glow accent after success. Continuous powers emit that accent when the channel starts; their paid refresh pulses do not repeat it. Charge cues gather a few small motes on initial formation. Existing BloodCellParticle and HutosLib ParticleGlow sprites supply the effect; the 32-pixel material sheets and physical subjects are unchanged.

Confirmed direct manipulation hits, Needle/Coronation hits, Blood Shot hits and mortar damage emit a short contact burst. The damage wrapper preserves the existing source, amount and boolean result, then sends a cosmetic packet only after a successful hit. Cells retain crimson or depleted wine coloring; glow picks up the tendency palette, with a minority of secondary-tendency glows for Saint casts.

Sustained Coronation, Choir, Ward, Well, Beacon, Veil, Furnace, Phoenix readiness and Retort forms receive two small motes every 12 ticks. Well draws them inward. Stillness becomes quiet after formation, and consumed Retort protection stops emitting immediately.

New accents last 16-24 ticks and share a 48-sprite-per-client-tick ceiling. Repeated impacts on the same target are limited to one accent per five ticks. Decreased reduces emission and Minimal disables these decorative accents. Emission is culled beyond 48 blocks; client world changes clear all budget and hit-cooldown state. These limits cover the new accent layer; existing spell-specific particles retain their own behavior.

The initial focused run passed 12 tests (budget/settings/world-reset cases, school and Saint packet round trips, and signature geometry/material checks). All 263 required GameTests and the resource-error gate passed in `build/manipulation-particles-check4.log`. The previous full-JVM-suite failure ledger has not been rerun or changed by this focused check.

Final particle verification: the updated client completed 160 study captures; a dedicated server and two real clients completed 19 assertions with zero failures and clean shutdowns. See the [particle capture index](evidence/visceral/particles.md). Logs: `build/manipulation-particles-live.log` and `build/manipulation-particles-remote-{server,caster,observer}.log`.

## Organic contour movement

The shared manipulation world meshes, Coronation projectiles and Blood Shot forms now receive coherent local-space vertex displacement. Three spatial waves combine a fixed irregular contour with slower traveling undulation. Each cue uses its entity or position as a stable seed; projectiles use their entity ID. The contour follows the object through camera and pose transforms without frame-to-frame random jitter. The approved 32-pixel textures remain unchanged.

Animus and Tenebris have the largest movement. Mortem, Flammeus and Lux use intermediate motion; Ductilis has fine, faster movement. Ferric and Congeatio keep small distortions. Stillness freezes its deformation clock after eight ticks. Sword tips taper to a fixed point. Verdict's active beam and charge guide retain their clipped endpoints, axis and outer width; square field boundaries and ore locators remain exact.

`ManipulationUndulation` uses Minecraft's sine lookup table. `UndulatingVertexConsumer` applies it while the existing vertices are submitted, before their pose transform. This adds no mesh vertices, render passes or draw calls and allocates no objects per displaced vertex. It does add bounded CPU arithmetic per vertex; no FPS improvement or CPU/GPU equivalence is claimed. Droplets emit positions in the same local frame as their supporting strands so they remain attached. Existing separately authored Avatar/Staff models, blocks and HutosLib renderers retain their own geometry and animation.

The focused check passed 19 tests, including wrapped lifecycle meshes, UV-seam continuity, camera transforms, droplet attachment, static field boundaries, beam bounds, stopped Stillness and vertex budgets. `compileGameTestJava` passed. Log: `build/manipulation-undulation-check.log`. Independent review verified the charge endpoint and droplet-frame fixes. This visual-only revision does not change the earlier full-suite failure ledger or claim a new dedicated-server combat run.

The updated integrated client completed successfully with 160 fresh captures across all eight schools, frontal and offset views, darkness, and overlapping effects with Minimal particles. Representative captures were visually inspected. See the [undulation capture index](evidence/visceral/undulation.md); this revision is saved separately under `study-undulation/`. Log: `build/manipulation-undulation-live.log`.

## Formation and dissolution follow-up

The subsequent [blood lifecycle pass](08-blood-formation-lifecycle.md) adds shared material formation, blood ligaments, refresh continuity, cancellation/expiry transitions, projectile formation/remnants, and server-authored status removal for observers. It expands packet/render coverage to all 59 shared forms and records the separately authored paths that retain their existing transitions.

## Charged preview timing after loading a save

Saved manipulation records are plain `BloodManipulation` instances. Their default charged duration is 20 ticks, while spells such as White Verdict and Vitric Combustion require 60 and Crimson Coronation requires 80. `ManipulationChargeVisualPacket` previously used the saved record directly: its progress advanced too quickly and it rejected further refreshes after tick 20. The preview then expired while the gauge continued charging.

The preview handler now resolves the selected name through `ManipulationInit`, matching the client gauge and `UseManipKeyPacket`. It retains the existing four-tick refresh limit, ten-tick preview lifetime and zero-tick release message. Gameplay durations, costs, partial-charge behavior and packet formats are unchanged.

`ManipulationChargeVisualGameTests` restores serialized selections for all 16 charged spells and checks progress, late/full-charge refreshes, release messages, invalid durations, and the absence of payment or cooldown side effects. `ManipulationChargeVisualReview` uses the actual client key and network path for White Verdict, Vitric Combustion and Crimson Coronation, at full charge and early release. It also executes the packet regression in the integrated server.

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PchargeVisualReview runClient
```

The fixture is restricted to the disposable `VisceralReview` save under `build/visceral-review-client`.

The saved-selection regression failed before the correction and now passes for all 16 spells. All 41 focused JVM tests, compilation and `assemble` pass. The client completed all six full-charge/early-release scenes, with 390 frames per run; late-charge captures retain the focus, glass buildup and crown swords while the gauge is still filling. The comparison is `build/lux-umbra-review/charge-preview-before-after.webm` (before left, after right). Logs: `build/charge-preview-before.log` and `build/charge-preview-after.log`. This focused check does not rerun the full dedicated-server or alpha suite.
