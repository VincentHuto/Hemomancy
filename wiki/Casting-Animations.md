# Casting animations

Each of the eight tendencies has five authored casting presentations, from Humilis through Perfectus. Rank changes the composition and silhouette; the manipulation's existing quick, charged, continuous or passive type controls playback. Costs, cooldowns, targeting, damage, movement and charge thresholds remain gameplay-owned.

The restrained-motion revision keeps active gestures at chest/shoulder height, with small first-person rotations. Casting does not pitch the torso away from its waist attachment. Charged and continuous casts ease into a fixed pose once, keep it until release/cancellation, and recover once afterward. Repeating arm pumps, torso heaves and 24-tick pose cycles have been removed from every rank, including bespoke overrides.

| School | Motion identity | Advanced anatomy | Cancellation |
|---|---|---|---|
| Animus | Small chest-height opening into a steady guard | Pulsing vascular heart | Subsides |
| Flammeus | Raised asymmetric brace and short forward release | Venting blood conduits | Vents |
| Ductilis | Angular raised arms, held without twitch cycles | Branching nerves with travelling impulses | Circuit collapses |
| Lux | Even elevation, parting and upright proclamation | Large paired white wings | Fades |
| Mortem | Raised reach and short shoulder-height withdrawal | Bowed cadaver and stolen flow | Relaxes |
| Congeatio | Deceleration, compression and simultaneous lock | Frozen red vessels and crystal shell | Fractures |
| Ferric | Chest-height brace and restrained downward release | Aligned foundry vessels and iron clots | Unloads |
| Tenebris | Concealment, counter-turns and delayed unfolding | Detached blood-shadow | Folds into alignment |

The [concept package](../docs/HEMOMANCY_CASTING_ANIMATION_CONCEPTS_v1/HEMOMANCY_CASTING_ANIMATION_CONCEPTS/README.md) contains the five-rank sheets. Their materials guide the meshes; player motion uses only the six rigid model parts.

## Playback and compatibility

Quick casts begin at release after successful server execution. They have no decorative delay before gameplay. Charged casts open and hold using the existing charge duration; damage can reduce progress without restarting the animation clock. Channels retain one uninterrupted hold across upkeep pulses. Manual passive activation/dismissal gestures briefly; automatic passive triggers retain their own effect accents without repeatedly taking control of limbs.

Opening lasts six visual ticks, release six, recovery fourteen and cancellation ten. Existing charge and channel durations are independent of those decorative intervals. New casts replace recovery. Sequence ordering rejects old phase updates, and tracking entry receives ongoing sessions without replaying releases. Entity removal, death, disconnect, level changes and missing heartbeats remove poses.

Standing legs join the stance. Walking fades them into locomotion; riding, swimming, crawling and Elytra preserve required body posture. Item use, swings, Living Staff morphing, Living Torch breath, vial injection, microscope and Cardinal Rite animation retain their occupied limbs. Armor and sleeves follow the resulting model pose. Left-handed players mirror the motion; Morphling-hidden hands remain hidden. First-person tracks share the third-person clock but use separately authored arm motion and isolated render scopes.

Purpose is explicit metadata: projectile, self, area, targeted, construction, conjuration or travel. It adds a restrained directional ending. Ironhearted, Umbral Step/Reversal, Avatar forms, Living Staff forms and the four Saint Canons also have bespoke pose overrides. Existing specialized animation remains authoritative. Secondary tendencies add a small material accent at the hand; vascular-condition degradation is deferred.

## Local preview

In a development client/world, enter:

```text
/hemocastpreview lux perfectus area hold
/hemocastpreview ferric magister construction release
/hemocastpreview tenebris summa travel cancel
/hemocastpreview stop
```

Tab completion lists all eight schools, five ranks, seven purposes and five phases (`opening`, `hold`, `release`, `recovery`, `cancel`). Preview lasts eight seconds and overrides only the local visual state. It spends no blood, unlocks nothing, applies no gameplay and sends no cast request. Use first person or F5 to inspect the same clock from either view.

## Effects and performance controls

The casting layer reuses `ManipulationMaterials` and `VisceralMesh` rather than duplicating spell projectiles, impacts or fields. Hand veins use live player-model transforms in third person. Advanced meshes sit around or behind the caster. No additional particle emitters or entities are spawned.

Vascular strands share their ring vertices and texture coordinates across bends. This keeps the Tenebris blood-shadow's torso and limbs continuous instead of leaving gaps between independently oriented tube segments; the existing curves, taper and mesh budget are retained.

Lux casts at Summa, Magister and Perfectus instead unfold a mirrored pair of white feathered wings behind the shoulders. The wings follow the torso's crouch and twist, open over six visual ticks, grow wider with rank, and use the cast's existing fade and cleanup. A continuous wing body joins the overlapping flight feathers. Their untextured white material stays luminous in darkness and avoids the blood tint of the other anatomy meshes.

Minimal particles removes large anatomy while retaining the pose and small hand cues. Decreased replaces the anatomy with short chest vessels. Full anatomy simplifies past 24 blocks and disappears past 32; the pose remains. These are bounded meshes per caster, not the concept document's maximum particle allocations. Existing spell particles keep their shared budget.

## Source and verification

The common entry points are `CastPresentation`, `CastingAnimationManager`, `CastingAnimationPacket` and `CastingChargePacket`. `CastingClips`, `CastingPlayback`, `CastingPoseModifiers`, `CastingPlayerPose` and `CastingAnimationClientState` contain the client tracks and lifecycle. Presentation metadata is not added to saved manipulation data.

Focused verification commands:

```powershell
.\gradlew.bat test --tests '*Casting*Test' --tests '*ManipulationInputRulesTest' --tests '*PlayerAnimationPriorityTest'
.\gradlew.bat runCastingAnimationGameTestServer
```

The dedicated tests cover every active registry mapping, packet round trips, immediate quick-cast payment/release, rejected casts, charge cancellation and reduction, continuous upkeep and late tracking. JVM tests cover all 40 compositions, mirroring, phase transitions, recovery continuity, purpose identity, stance blending and terminal ordering.

`tools/casting-animation-review.init.gradle` and the flag-gated `CastingClientAcceptance` GameTest source provide a disposable two-client capture run. Use a separate `build/<name>-server` directory with a localhost-only server on port 25579 and first-run accessibility onboarding disabled in both review clients. First run `./gradlew.bat classes gameTestClasses` once; concurrent compilation into the same output directory is unsafe. Then launch `runServer`, `runCastingCaster` and `runCastingObserver` with `-I tools/casting-animation-review.init.gradle -PcastingRun=<name>`. `-PcastingIntegrationOnly=true` on the server skips the 40-profile sweep; `-PcastingCasterName=CastCaster1` selects the WIDE review player. Do not use a personal world for this fixture: it prepares a quartz stage and test inventories in its review directory.

Live evidence and remaining validation limits are recorded in [Casting-Animation-Validation.md](Casting-Animation-Validation.md).
