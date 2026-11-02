# Ferric and Ductilis second pass — 9 September 2026

The implementation adds rigid blood-iron defenses, successive nervous lightning, dedicated paralysis and bounded environmental conduction. It was developed in the existing dirty checkout; no staging, commit, reset, worktree or runtime-save cleanup was performed.

## Delivered behavior

- Ferric Rampart and Ferric Spikes are remembered forms of Sanguine Magnetism at family levels 1 and 2. They share mastery, cost, rank, range and cooldown. Placement rejects unsupported or occupied space before payment. The five-by-three thin wall blocks bodies/projectiles; five blades share the ten-tick 3.5-damage contact interval.
- Existing iron registrations synchronize configured mode, cardinal orientation, owner and absolute expiry. Configured constructs remain anchored, produce no drops and disappear after six seconds or source loss. Encounter spikes retain their legacy mode. Magnetic pillars retain attraction and pinning and gain electrical relays.
- Jolt interrupts actions; Activation uses distinct visible radial branches; Storm chooses each next endpoint from its previous endpoint, with conductive priority, deterministic ties, line of sight, no revisits, five/eight-block hops and at most eight victims within the eighteen-block acquisition envelope.
- Paralysis preserves gravity, camera rotation, incoming damage and automatic defensive passives. It blocks voluntary movement, jumping, attacks, item use, blood casts, alternate casting controls and Still Arts, cancels channels and held charges, and requires release before charging again. Players cap at twenty ticks, ordinary mobs at sixty, bosses receive reduced movement disruption, and recovery lasts twenty ticks. Slime contact, natural creeper windups and held mount-jump releases are covered.
- Offensive Ductilis strikes energize summoned iron, face-connected water and tagged construction metal. Each connected network is bounded to 256 loaded nodes within twelve blocks, forty ticks, and three touching victims per ten-tick pulse. Connectivity is rechecked; shared hit accounting prevents direct/mark/relay multiplication. Teams, owners and PvP settings remain authoritative. No terrain, fluid, redstone or oxidation changes occur.
- Dedicated opaque Ferric and translucent Ductilis shader materials replace the affected old primary meshes and formation/expiry overlays. Iron uses dark plate faces, rusty edges and liquid formation/fracture. Electrical strikes retain HutosLib bolts with brief restrikes; moving connections use its anchors with narrow shader ribbons. Marks, wards, circuits, sensory responses and severing threads use the same material language.
- Current Ironhearted capacity, full-charge rejection, knockback protection, body-relative chest placement and HUD remain. Choir shows actual interceptions; Retort fractures on consumption; utility/conjuration effects use hand/tool anchors. Retired entries remain retired.
- Existing visual ordinals and Lux/Umbra flow serialization are preserved. Added conduction-path, moving-link and charge-interruption payloads are separate. Sustained states replay to new trackers and retire on expiry/dispel/source loss. Networks clear on shutdown and dimension/source loss.

Player rules and reproduction commands are in [Ferric and Ductilis](../../../wiki/Ferric-Ductilis.md).

## Automated evidence

| Check | Result |
| --- | --- |
| Focused JVM suite | 14 passed: family/mastery, construct geometry/contact timing, deterministic chain rules, conductor graph bounds/continuity, payload limits and renderer geometry/budgets. |
| New dedicated runtime checks | 31 passed: 24 Ductilis, one natural ranged/melee combat test, five Ferric and one observer synchronization test. |
| Complete dedicated runtime suite | 296/298 on the final run. The unrelated formation fixture found an occupied stone block, and Veinwing's volley fixture found no volley. Both varied across earlier runs. |
| Full JVM suite | 1,849 executed; 1,793 passed and 56 existing failures remain. They include stale source paths, legacy discovery count and unchanged layout/attachment expectations. |
| `build` and `alphaCheck` | Both attempted together with `--continue`; the full gate remains failed because of the suite failures above. The resource-log gate is blocked by the dedicated test dependency. |
| `build -x test` | Passed; release JAR assembled. This is packaging evidence, not a clean full build. |
| Shader validation | Both programs, all four stages and declared uniform defaults passed the GLSL validator. |

The full run exposed two changes needed in this pass: memory glyphs exceeded their eight-pixel opening, and a source contract still expected Activation's damage call before its move into the shared magic-damage helper. Both checks now pass. Slime's unlocked negative control required its test player to pass the normal sixty-tick login grace; the test still checks both locked and unlocked contact damage.

Logs are under `build/ferric-ductilis-work`: `ductilis-action-verified.log`, `final-package.log`, `build-alpha-final.log`, `full-jvm-summary.json` and `jvm-failures.txt`. The complete full-run XML was retained in `full-jvm-results` before the focused rerun. The final scoped whitespace check passed, and both shader programs were validated again after capture.

## Native capture

The final capture covers all 68 scenarios with 2,652 primary-client screenshots: 32 daylight/control scenarios, eighteen night scenarios with resource reload and late observers, and eighteen Minimal-particle scenarios with moving dense combat. It uses real paid player releases, supported entity casts, reactive defenses and a live allied recipient. Charge previews are scripted through the production visual entry point. The dedicated player-control scene attempts native movement, jumping, attack and item-use inputs during paralysis while rotating the camera, then verifies movement resumes.

Native player assertions passed: horizontal displacement during the lock was zero, camera yaw changed by 36 degrees, the active Choir channel stopped, and movement resumed after expiry. The final captures were reviewed for rigid wall/spike silhouettes, chest and hand placement, narrow first-person bolt cores, body nerve patterns, water-surface transmission and expiry, and iron rendering through glass alongside Lux/Umbra effects. The resource-reload segment includes the normal loading overlay; subsequent frames rendered the reloaded materials correctly.

The final evidence combines daylight scenes 0–21 from `final-client-daylight.log`, corrected scenes 22–30 from `final-client-part22.log`, and scenes 31–67 from `final-client-part31.log`. The first daylight attempt failed after cumulative fixture damage killed its caster during NPC Retort; those affected scenes were replaced. The fixture now restores health between scenarios, applies the NPC reaction to its actual caster, respawns a previously dead review player before setup, and rejects unexpected caster death immediately. Failed intermediate captures and their timings are not used for the affected scenes in the assembled results.

The earlier visual baseline contains 702 screenshots of the previous presentation. It already uses the new Ferric gameplay, so the comparison isolates presentation rather than demonstrating the old mechanics. The original Circuit and Thread Ripper baseline scenes were material studies; the final scenes perform their actual casts. The first updated capture contains 936 screenshots across 24 scenarios. The final capture supersedes those intermediate visuals after rust, nerve visibility, translucent-order and bolt-width corrections.

Clips are assembled from native screenshots sampled at five frames per second, encoded at 30 FPS with repeated frames. They show effect shape and lifecycle; they are not full-rate recordings suitable for judging microstutter. Whole rendered-frame intervals were collected separately.

| Clip | Coverage |
| --- | --- |
| [Iron before/after](../build/ferric-ductilis-review/iron-comparison.mp4) | Matched views of Rampart, Spikes and Magnetism. |
| [Defenses and utilities](../build/ferric-ductilis-review/defenses-after.mp4) | Ironhearted, Choir, Retort, Mending, Dowsing and Staff. |
| [Nervous electricity](../build/ferric-ductilis-review/nerves-after.mp4) | Jolt, Activation, Storm, Mark, Ward, Circuit, Pulse, Thread Ripper and connected water. |
| [Entity casts](../build/ferric-ductilis-review/entity-casts-after.mp4) | Jolt, Activation, Mark, Pulse, Retort and Magnetism. |
| [Conjuration](../build/ferric-ductilis-review/conjuration-after.mp4) | Blood Absorption, Blood Projection, blade, axe, spear, crossbow and sickle. |
| [Player paralysis](../build/ferric-ductilis-review/player-paralysis.mp4) | Locked inputs, camera rotation and recovery. |
| [Night and reload](../build/ferric-ductilis-review/night-reload-observers.mp4) | Eighteen night scenarios, resource reload and late tracking. |
| [Minimal particles and combat](../build/ferric-ductilis-review/minimal-dense-combat.mp4) | Eighteen scenarios with twenty moving combatants. |
| [Second client](../build/ferric-ductilis-review/observer-daylight.mp4) | Remote chest/hand effects, actual linked recipients, impacts and corrected daylight tail. |

Each primary-view scenario occupies 7.8 seconds. The timing summary is [frame-timings.json](../build/ferric-ductilis-review/frame-timings.json).

| Measured mode | Frames | Mean | 95th percentile | Maximum |
| --- | ---: | ---: | ---: | ---: |
| Daylight/control | 20,834 | 8.525 ms | 11.002 ms | 20.487 ms |
| Night/reload | 11,627 | 8.562 ms | 11.060 ms | 125.715 ms |
| Minimal/dense combat | 11,703 | 8.537 ms | 10.660 ms | 19.128 ms |
| Combined | 44,164 | 8.538 ms | 10.951 ms | 125.715 ms |

The assembled observer clip contains 1,309 native frames, replacing the affected tail of the first daylight run with the corrected segment. Raw night/late-observer frames are retained separately. Final packaging produced `build/libs/hemomancy-6.0.1-neoforge.1.21.1.0.jar` (98,336,963 bytes).

## Acceptance limits

Numeric limits remain tuning defaults. Automated combat and the disposable native review do not establish final player-versus-player balance or replace player visual acceptance.

The original before fixture sampled screenshot spacing rather than complete frame intervals. Those values are not a valid performance baseline. Final measurements include two running clients, periodic screenshot capture and a 120 FPS cap; they measure whole frame intervals, not isolated shader/GPU cost. A comparable before/after performance claim remains open.
