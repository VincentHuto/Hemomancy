# Animus and Mortem presentation

Animus uses flowing crimson blood, narrow liquid trails and small pale reflections. Mortem uses dried oxblood, brown crust, slow sludge and broken wisps. Later Mortem effects introduce pale mycelium and spores, with Bloom of Rot showing the strongest contamination. These are presentation changes; they do not add an infection system or persistent damaging terrain.

## Family treatment

| Family | Presentation |
|---|---|
| Blood Shot, Guided Shot, Hematic Mortar, Sanguine Halo | Liquid projectile bodies and trails follow the actual flight path, including steering and orbiting. Mortar retains a heavier body and radial rupture. |
| Blood Needle, Fan, Lance | Separate charge arrangements gather narrow streams. Needle and Coronation sword silhouettes remain authored geometry with an animated blood surface. |
| Blood Binding, Lingering Binding, Chain Binding, Blood Lattice | Thin joined liquid ribbons retain the existing ground approach, pointed roots, target coils and staged retraction. Ground collision sampling runs on ticks; frames interpolate the curves. |
| Blood Cloud, Expansive Cloud, Pursuing Cloud, Sanguine Tempest | A liquid carrier opens into drifting mist and hanging blood threads. Radius and motion distinguish variants. Tempest uses a thin crimson HutosLib bolt. Legacy carrier particle spam and construct dust are suppressed without changing lifetime or damage. |
| Blood Rush, Vital Effusion, Hematic Rebuke, Hematic Impressment, Sovereign Instinct | Rush coils, upward growth streams, and command crowns use different motion and proportions. Command targets and timing remain server-owned. |
| Avatar and its arms, armor, legs and complete forms; Conjure Blade | Existing body parts and transitions retain their silhouettes. The active model material uses circulating crimson. Blade formation gathers thin streams. |
| Hemorrhage | A short anchored tear and dried fissures on the affected model, accompanied by sparse rising wisps. |
| Insatiable Hunger | Dark cavities and inward motion; prevented healing triggers a brief collapse. |
| Grave Debt | Fine decay seams, a short eruption at the actual threshold, and an extraction stream on the actual death refund. |
| Exsanguinate | Blood travels from the execution position into the caster. A fixed source point survives removal of the corpse. |
| Lignum Mortis, Canopy Mortis, Worked Lignum | HutosLib centerlines carry thin decay ribbons to dismantled wood, with dry accents on successful block removal. Tree selection, cadence, permissions and drops remain intact. |
| Carrion Communion | Moving victim-to-caster sludge streams, brief ground staining and small fungal accents. Channel cancellation retires the streams. |
| Blackhearted | A body-relative clot grows and pulses with saturation. Rupture releases a short decay burst. |
| Funeral Bell | A hollow spectral bell made from shader silhouettes and wisps, with a traveling ground ring and target-local impacts. |
| Crimson Tithe | A suspended clot represents the active debt. Collection collapses inward in dried blood; unused blood returns in living crimson. Existing reserve, amount-due and time text remains. |
| Bloom of Rot; Conjure Axe and Sickle | Bloom briefly stains sampled collision surfaces, releasing spores and branched infection on affected models. Axe and sickle formation use slow decay streams. |

Retired Vital Reservoir and Summon Thrall remain excluded. Deadly Gaze remains Ductilis. No costs, ranks, hit eligibility, progression or authored weapon/Avatar models are changed.

## Rendering and lifetime

Three shader materials cover living blood, decay and posed infection. Living blood has separate program registrations for world and entity vertex formats, sharing the same shader source. World ribbons, mist and pools join the existing sorted translucent pass. They test depth without writing over other translucent effects. Shader time and modulation are restored after each pass.

Secondary particles share the existing 48-per-tick budget, 48-block distance limit and reduced/minimal settings. Core spell surfaces remain visible on Minimal. Flow connections and free mist each cap at 128 entries; projectile trails and general/status cues have bounded caches. Missing sources retire their last resolved geometry; changing client worlds clears transient caches. Status overlays respect model poses and invisibility.

Late entity observers receive active Blood Loss, Hunger and Grave Debt from actual effects, plus tracked Bloom infection. Cures and expirations explicitly retire remote visuals. Bloom provenance is transient: it survives observer changes within a loaded server session, but is not saved as cosmetic NBT across a server restart or entity unload. Existing gameplay effects persist normally.

A live cast review exposed an existing asynchronous knowledge-packet race. `KnownManipulationServerPacket` now captures its map, levels and collections before encoding. This keeps queued packet contents stable while the server updates learned powers; wire fields and order are unchanged.

## Verification

- 53 focused geometry, shader contract, lifetime, binding and shared renderer tests passed with zero failures. GameTest sources compiled. See `build/animus-mortem-final-tests.log`. This was a focused suite, not a full legacy-suite run.
- GLSL compilation and linking: `python tools/validate_blood_shaders.py`; see `build/animus-mortem-shaders.log`.
- The disposable cast review lives in `build/animus-mortem-client`, separate from normal runtime saves. It casts registered manipulations and records actual outcomes. Prerequisites and accelerated status triggers are fixture setup, not a progression playthrough.
- The packet-observer check covers late status delivery, cure removal, channel stop, corpse-safe extraction and stable knowledge snapshots.

- The multiplayer review completed all 126 scenes (42 forms in daylight, night/first person and Minimal particles), saving 2,898 native frames. A second client entered tracking range after casts. Resource reload and the observer/cleanup regression checks passed. See `build/animus-mortem-multiplayer.log` and `build/animus-mortem-observer.log`.
- A follow-up activated all five Avatar manifestations through the real manifestation manager in each viewing mode, and repeated Coronation. The original fixture only toggled the generic passive state; those captures were replaced. The external camera was widened for the large models. See `build/animus-mortem-avatar-review.log` and `build/animus-mortem-avatar-framing.log`.
- All nine supported NPC review casts succeeded, including Hemorrhage, Exsanguinate, Hunger, Grave Debt and Bloom. See `build/animus-mortem-npc.log`.
- The Communion restart regression failed before the correction and passed after it: a new channel refresh now reattaches during the previous stop fade.
- The local capture index is `build/animus-mortem-review/index.html`, generated by `python tools/assemble_blood_review.py`. Baseline Command casts did not meet the target health gate; final targets do. The baseline and final external cameras differ, so these are cast evidence rather than matched pixel comparisons.

- The package build passed: `./gradlew.bat build -x test --max-workers=1 --no-daemon`; see `build/animus-mortem-package.log`. The focused test suite above was run separately.
- Git could not read a pre-existing missing object. Source review used the pre-edit working-file snapshot in `build/animus-mortem-baseline`; the resulting task diff is `build/animus-mortem-task.diff`. No staging, commit or Git repair was performed.

- The screenshot-free run completed all 42 actual casts with 16,077 frame samples: mean 8.49 ms, 95th percentile 10.28 ms. The client used 1280x800, Fancy graphics, 12-chunk render/simulation distance, VSync enabled and a 120 FPS limit. This measures the current capped run, not an uncapped GPU budget or an improvement over baseline. See `build/animus-mortem-benchmark.log`.

## Reproduce

```powershell
./gradlew.bat -I tools/animus-mortem-review.init.gradle -PbloodReviewLabel=review runClient --max-workers=1 --no-daemon
./gradlew.bat -I tools/animus-mortem-review.init.gradle -PbloodMultiplayer -PbloodReviewLabel=multiplayer runClient --max-workers=1 --no-daemon
# Once the disposable host reports ANIMUS_MORTEM_SERVER_READY:
./tools/review-blood-observer.ps1
./gradlew.bat -I tools/animus-mortem-review.init.gradle -PbloodNpc -PbloodReviewModes=1 -PbloodReviewLabel=npc runClient --max-workers=1 --no-daemon
./gradlew.bat -I tools/animus-mortem-review.init.gradle -PbloodBenchmark -PbloodReviewModes=1 -PbloodReviewLabel=benchmark runClient --max-workers=1 --no-daemon
```

The review requires a disposable world named `VisceralReview` in its own directory. Multiplayer uses development profiles and port 25579; the observer launch script reuses prepared runtime arguments without rebuilding dependency jars under the running host.
