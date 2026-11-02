# Blood formation and dissolution

The shared world effects now reveal connected blood-colored material patches during formation. Small blood ligaments and beads gather at the subject before the school material finishes appearing. The effect's actual activation, damage, targeting and duration do not wait for this cosmetic transition.

## Coverage

- Every shared visual form passes through the lifecycle renderer. There are 59 forms including charge variants; these are visual forms, not 59 independently certified manipulation casts. Ore locators preserve exact block outlines and are deliberately excluded from blood-material transitions.
- Blood Shot and ordinary Needle/Fan/Lance projectiles coalesce rapidly while remaining visible in flight. Coronation projectiles use the same treatment and keep their launch ligament. Their removal leaves a bounded local blood-remnant effect; the projectile itself is never retained after removal.
- Blood Cloud's shared CLOUD cue receives the lifecycle treatment. Its separate entity renderer is empty; the entity's existing particle emission remains.
- Avatar already uses its dedicated manifestation/melting transition. Living Staff already uses its morph/dissolve sequence. Blood Binding retains its authored extension/retraction. Iron Pillar retains its existing growth/death animation. These paths were inspected; this change does not replace those established animations.
- Actual fire and ice blocks retain their existing block rendering and gameplay lifetimes. Their accompanying shared spell cues receive the lifecycle treatment. This is not a claim that every block model or existing particle renderer was rebuilt.

## Timing and gameplay cues

Formation lasts at most eight ticks, shortened to a quarter of the initial lifetime for short effects. Material fades through connected spatial patches rather than uniformly appearing at full opacity. Natural expiry for transient bursts reverses that material reveal over the last quarter of their lifetime, capped at eight ticks.

Attached and refreshed effects preserve their birth time. Refreshes neither restart formation nor cause periodic fades between packets. Explicit cancellation, source removal, status removal and sustained timeout enter an eight-tick cosmetic exit. A fully faded transient is never resurrected for that exit.

Cosmetic subjects can drain into blood patches and fall slightly during the exit. Their square field boundary is removed immediately. Beams, readiness counts, shields and status symbols disappear immediately and leave only blood remnants; their old subjects are not displayed as if still active. HUD debt/time readouts stop immediately too. Ore locators vanish without residual blood.

Exit motes now cover all shared schools: blood drops, cooled fragments, sparse vapor or depleted residue. They use the existing particle cap/settings/culling. The connecting formation/remnant mesh is part of the subject presentation and remains visible under Minimal particles. All client state clears on world changes.

## Observer correctness

Remote entities do not synchronize their full active-effect map to ordinary observers. Status visibility therefore uses local effect checks only for the local player. The server sends existing zero-tick visual packets when Conductive Mark, Iron Retort, Hunger, Grave Debt or Blood Loss actually ends. Removal is checked at the end of the server tick, so canceled cures and same-tick reapplication do not falsely clear the cue. This changes presentation only.

## Verification

The focused suite covers short lifetimes, refresh continuity, bounded cancellation, no resurrection on expiry, spatial material reveal, final mesh parity, boundary removal, readiness/beam exclusions, and the earlier geometry/undulation constraints. All 22 focused tests passed. All 263 required GameTests and the resource-error gate passed in `build/manipulation-lifecycle-final-check.log`.

The all-form client fixture is invoked with:

```powershell
./gradlew.bat -I tools/manipulation-visceral-review.init.gradle -PvisceralRoster runClient --console=plain
```

It uses the disposable VisceralReview save. Each of the 59 forms is emitted through a production packet attached to an invisible source, refreshed, then explicitly stopped. Six screenshots record initial appearance, formation, established effect after refresh, dissolution, and two cleared frames. This covers 354 packet/render captures, not 59 actual combat casts. The ordinary fixture without `-PvisceralRoster` remains available for natural-expiry studies of the eight school representatives in four views/settings.

The final client run and focused tests completed successfully in `build/manipulation-lifecycle-acceptance.log`. All 354 frames were checked for freshness and saved separately; representative formation, dissolution and cleared frames were visually inspected. See the [capture index](evidence/visceral/lifecycle.md). Independent source review found and verified fixes for inherited body transforms and remote effect-map checks. The earlier full-JVM-suite failure ledger is unchanged; this revision does not claim a new dedicated-server/two-client combat acceptance run.
