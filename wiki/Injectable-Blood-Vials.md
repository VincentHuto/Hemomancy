# Injectable blood vials

Loose `hemomancy:bloody_vial` samples can be injected by holding use for 16 ticks. Sampling still uses left-click. Successful survival use replaces the held sample with one empty vial, retaining unrelated components and custom fields. Creative use keeps the sample and produces no extra vessel. Both modes validate the source and apply 400 ticks of Transfusion Saturation. Interrupted use, spectators, dead players, unreadable sources, and empty response sets do not consume blood.

Saturation is an ordinary harmful effect. Its normal persistence, death, milk, command, and effect-removal rules apply. Injection does not award experience, refill blood, alter alignment, or gate progression. Racks remain sampling/transport tools. Centrifuge processing and its existing tags are unchanged.

## Shared specimen contract

`BloodSampleData` reads the existing `CUSTOM_DATA.entity_type` and `state` fields. A present source field, including malformed or removed-mod values, keeps the vial filled and prevents resampling/loading as empty glass. `BloodVialItem.getEntityType` remains a safe compatibility wrapper. Missing sources are inert; their source strings and other data remain intact.

The boolean `hemomancy:blood_sample_identified` component is persistent and synchronized. Absence means unknown. `BloodSampleData.identify` is the server examination entry point for the companion microscope and refuses unresolvable samples. It stores no timestamp or random identity. Independently identified otherwise identical samples remain component-identical. A profile is a temporary, ordered view of current tags, never a saved copy of effects.

The [Hematic Microscope](Hematic-Microscope.md) identifies a vial held in the off hand after two seconds of uninterrupted use. Unknown samples still work; examination supplies information rather than potency. The cabinet and Clairaudiograph remain separate companion features.

## Default responses and provisional tuning

Benefits last 200 ticks at amplifier zero except the 40-tick reveal pulse. Selection follows enum declaration order, then property ID order. Identical benefits merge using maximum duration/amplifier before the three-benefit cap. Reveal occupies a slot. A selected benefit retains its associated drawbacks, including when multiple origins supply the same benefit.

| Origin | Benefit | Associated drawback |
|---|---|---|
| Animus | Regeneration | — |
| Flammeus | Fire Resistance | — |
| Ductilis | Haste | — |
| Lux | Night Vision, then an 8-block, 40-tick Illumination pulse | — |
| Mortem | Strength | — |
| Congeatio | Cryoprotection: freeze immunity and powder-snow traversal | Slowness I, 200 ticks |
| Ferric | +4 armor, +0.1 knockback resistance | — |
| Tenebris | Invisibility, with ordinary equipment visibility | — |
| Aquatic | Water Breathing | — |
| Flying | Slow Falling | — |
| Venomous | Poison resistance, no movement bonus | — |
| Cold native | Same Cryoprotection identity | — |
| Nether native | Same Fire Resistance identity | — |
| Arthropod | Cobweb motion multiplier 0.8 on each axis | — |
| Burrowing | 25% faster mining of tagged earth/stone | — |
| Ender | 25% less projectile damage | — |
| Undead | Wither resistance | 50% reduced incoming healing, 200 ticks |
| Explosive | 25% less explosion damage | Instability represented by Weakness I, 40 ticks |
| Fungal | Identifiable morphology only | — |

Earth/stone membership is controlled by the `hemomancy:blood_injection_mineable` block tag; ores, wood, and machinery are excluded by default. Resistance, healing, mining, armor, and knockback values live together in `BloodInjectionRules`. Durations, amplifiers, and reveal radius live in the response JSON. Poison removal reuses `VenomousResilienceEffect` under a separate registration. Undead healing reduction uses `LivingHealEvent`; Hemolysis blood drain and purity behavior remain untouched. The reveal reuses `SchoolStates` illumination and its targeting rules, without invoking a manipulation cast. No custom effect adds morphling healing, speed, flight, or teleportation.

These values are implementation defaults, not established combat balance. Pig remains Animus and polar bear remains Congeatio; the shipped tendency memberships were not changed.

## Datapacks and synchronization

Files live at `data/<namespace>/blood_injection/<name>.json`. Each file defines exactly one uppercase `tendency` or namespaced `property` entity-tag ID. New property IDs require only a response definition and entity tag. Fungal directly uses `hemomancy:fungal`.

```json
{
  "property": "example:blood_properties/hardy",
  "benefits": [
    {
      "effect": "minecraft:resistance",
      "duration": 200,
      "amplifier": 0,
      "drawbacks": [{ "effect": "minecraft:slowness", "duration": 100 }]
    }
  ]
}
```

Use `{"operation":"reveal","duration":40,"radius":8}` for a pulse benefit. Root-level `drawbacks` are associated with each benefit; nested drawbacks allow explicit per-benefit association. A property with an empty benefits array is informational only. Definitions permit up to 16 benefits/drawbacks, duration 1–72000 ticks, amplifier 0–4, and reveal radius greater than zero through 32. Definitions reject invalid origins/effects, fractional integers, conflicting positive/negative identities within a response, and unsupported operations. Custom effect magnitudes are fixed; amplifier scaling follows their actual effect implementation.

Normal pack priority replaces a file with the same resource ID. Different files claiming the same origin are all discarded. Other valid definitions survive, and the complete valid snapshot swaps at apply time. Each bad resource is logged once per reload. The synchronized snapshot is bounded to 128 definitions, each at most 2048 JSON characters with resource IDs up to 256 characters. Even at maximum UTF-8 expansion this stays below the clientbound payload limit.

`OnDatapackSyncEvent` sends the snapshot on login and reload through `PacketHandler`. Server/client snapshots are separate, and disconnect clears client definitions. In the bundled 1.21.1 sources, `RegistrySynchronization.networkSafeRegistries` includes static registries and `TagNetworkSerialization` sends their complete tag membership, including entity-type property tags. No local client datapack determines injection results. Identified tooltips resolve the same capped response list from the received definitions and synchronized tags.

## Presentation and verification

The player raises the vial, draws the arm back, and drives its pointed end into the same-side upper thigh at the existing 16-tick completion point. The pose mirrors by physical arm, including the left-handed setting. First-person hands and third-person body/held-item rendering share the timing; the needle is aimed at the posed receiving thigh. A two-tick contact hold and six-tick withdrawal are cosmetic and yield to another action or item change. Successful survival use shows the emptied vial during withdrawal; creative retains the sample.

Only server-confirmed completion triggers the compact crimson blood-cell burst, tendency accent, and original puncture/compression/glass cue (`item.blood_vial.inject`). Presentation start, impact and cancellation synchronize to the user and tracking clients. Interrupted and rejected injections produce no impact. Cardinal Rite planting retains priority, and death/world changes clear transient poses. The original mono sound is reproducibly authored by `tools/audio/author_blood_vial_injection.py`. Effect icons reuse authored Hemomancy status textures. Alchemist vial inquiry retains the centrifuge lesson.

Focused commands:

```powershell
.\gradlew.bat test --tests '*BloodInjectionRulesTest' --tests '*BloodSamplingRulesTest' --tests '*VialCentrifugeStartupRulesTest'
.\gradlew.bat runBloodInjectionGameTestServer
```

Dedicated fixtures live under `blood_injection_validation`, in their own build-directory server world, and are also included in the normal GameTest namespace list. Live checks still required: remote-client tooltip refresh after `/reload`, first/third-person arm placement, short-duration survival usefulness, and companion-machine offhand dispatch after those blocks exist.

### Verification recorded 2026-09-15

- Package assembly and all six focused JVM tests passed.
- All 15 dedicated GameTests passed: legacy/malformed data, identification persistence/equality, both hands with full inventories, interruption/saturation, creative handling, item changes/death/spectators, packet round trip, pack replacement and refreshed identified answers, custom resistance/mobility/healing/mining, two-tendency selection, syringe preservation, centrifuge startup, and machine interaction before offhand fallback.
- The full JVM suite ran 2,035 tests; 2,033 passed. Two failures remain outside this feature: `MorphlingLumenlaceRenameResourceTest` encounters invalid UTF-8 in the untouched ignored `docs/phlegethontic-nether-worldgen/orcadian-horseman/README.md`; `LegacyMainTestSuiteContractTest` expects 361 legacy main-method tests while 360 exist. The full `build` task therefore fails its test phase, despite successful assembly.
- A focused read-only code review found no actionable correctness issues. No live-client visual, remote multiplayer, or survival balance result is claimed.

### Injection animation verification

- 24 focused JVM tests pass, covering physical-hand selection, wide/slim and crouched thigh contact, needle direction, recovery blending, delayed confirmation, single impact emission, timeout, and existing weapon/rite pose rules.
- All 65 tests in the dedicated blood-injection validation run pass. New fixtures capture actual server presentation packets for both hands and dominant-arm settings, verify the full-to-empty snapshot and payload round trip, and check cancellation on release, rejected completion, or switching to an identical empty vial during recovery.
- The isolated client loaded the updated classes and resources successfully. Windows window capture failed with `SetIsBorderRequired` / `0x80004002`; live first/third-person animation, audio feel and two-client observation remain unverified. The isolated client was closed afterward.
- The authored sound is 0.48 seconds of mono 44.1 kHz Vorbis, with a decoded peak of -1.6 dBFS. The existing injection mechanics and 16-tick use duration remain unchanged.
- Package assembly passes with the animation classes and custom OGG included. The final read-only review found no remaining concrete correctness issue after the recovery-switch regression was fixed.
