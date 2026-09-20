# Entity blood profiles

Blood composition and collection requirements are authored per entity at
`data/<entity namespace>/blood_profiles/<entity path>.json`. For example,
`data/minecraft/blood_profiles/elder_guardian.json` describes `minecraft:elder_guardian`:

```json
{
  "tendencies": ["congeatio"],
  "properties": ["hemomancy:blood_properties/aquatic"],
  "requires_living_syringe": true
}
```

`tendencies` accepts `animus`, `flammeus`, `ductilis`, `lux`, `mortem`, `congeatio`,
`ferric`, and `tenebris`. `properties` accepts namespaced identifiers, including
custom addon properties. `hemomancy:fungal` retains the infected-fungus centrifuge
output. Memberships are deduplicated and ordered consistently.

Missing lists default to empty; a missing boolean defaults to false. A missing
profile has those same defaults. Invalid field types, unknown tendencies, and
invalid property IDs reject that file with an error identifying its resource.
Unknown entity IDs are allowed so addon definitions can remain dormant when the
addon is absent. An unresolved source in an existing vial remains unreadable.

## Overrides and migration

A higher-priority datapack replaces the complete file at the same path. Arrays
do not merge. Include all desired tendencies and properties in an override;
an empty object explicitly clears a profile. `/reload` replaces the full server
snapshot, including removal of deleted definitions, and synchronizes it to players.
Login sends the current definitions; disconnect clears only the client snapshot.
Profiles are limited to 4,096 definitions, 4,096 JSON characters per definition,
and a 900,000-byte sync budget; excess definitions are rejected with an error.

The former tendency tags (`vivacious`, `fervent`, `neurotic`, `incandescent`,
`ruinous`, `frigid`, `ferric`, `umbral`), `fungal`, and `blood_properties/*`
entity membership tags no longer supply blood traits. Move addon memberships
into entity profiles. Existing property IDs and `blood_injection` response files
remain valid; a custom property needs an injection response only if it grants an
injection effect. Informational properties still appear under the microscope.

Existing vials retain their source ID, identification, and other components.
Their composition is resolved from the current profile; no save migration is
needed. Centrifuge amounts and selection behavior remain unchanged. Sample
tooltips, microscopy, cabinet previews, injections, centrifuges, and target
tendency affinity all use the profile lookup. Will school-specific affinity
continues to use the Will's actual school.

## Living Syringe

`requires_living_syringe` restricts collection, not storage, examination,
centrifugation, or use of existing samples. A loose vial rejects the target with
“Sampling this creature requires a Living Syringe.” It cancels the sampling
attack without filling the vial or awarding collection evidence. Carrying a
syringe is insufficient: use it with its loaded vial rack. Dead and invulnerable
targets remain invalid during direct sampling even with the syringe.

The initial data contains 131 profiles, of which 80 require the syringe: the
selected large/armored vanilla animals and hostile mobs, villagers and wandering
traders, all registered Hemomancy NPCs, registered Hemomancy mobs/summons with
declared base health above 20 HP, and Chitinite, Fervent Chitinite, Chalybeate Snail,
and Hematic Construct. Flags are static data; damage and temporary health changes
do not change eligibility. Unlisted creatures retain ordinary sampling.

Piglin, Piglin Brute, and Zombified Piglin are included; the illager set includes
Pillager, Vindicator, Evoker, and Illusioner. Camel Husk has no registered entity
in this 1.21.1 checkout and is not mapped to Husk. Add its real addon ID when available.

Consenting professional NPC donations also require a held syringe when their
profile requires it. The donation fills a rack vial and retains authenticated
donor provenance. Original professionals remain protected from direct sampling; their established consent-based donation bypasses that protection only within the authorized dialogue transaction. Consent, distance, and degree checks still apply. Personal
self-sampling, Hemolymphopoda's cleansing hemolymph, and the Warden's special
syringe sample retain their existing behavior.

## Verification

`BloodProfileMigrationTest` compares every migrated membership against the
pre-migration fixture and checks the approved restriction set. Parser and lookup
tests cover defaults, malformed data, immutability, dormant entities, and client
replacement. `BloodProfileGameTests` exercises sampling transactions, health
coverage, pack priority, reload removal, and packet round trips alongside the
existing blood-injection/microscope/storage tests. Succession GameTests cover
restricted consent-based donation and personal self-sampling.

Run focused JVM tests and `runBloodInjectionGameTestServer` /
`runSuccessionGameTestServer`. Automated packet checks do not replace a live
two-client login/reload and feedback review.

### Validation result (2026-09-17)

- 17 focused JVM tests passed.
- All 78 dedicated blood-injection/profile GameTests passed, including two-player login/reload packet delivery, malformed profile rejection, dormant addon profiles, pack overrides, reload removal, centrifuge outputs, and sample handling.
- All 22 dedicated succession GameTests passed, including syringe-only authenticated donation, retention of original NPC protection, and personal self-sampling.
- `assemble` passed. The packaged jar contains 131 profiles and no legacy blood-trait membership JSONs.
- Live two-client UI, rendered feedback, and manual login/reload acceptance remain unverified. Automated event delivery and packet round trips passed.

### Java entry points

`EntityBloodProfile` contains immutable tendency/property lists and `requiresLivingSyringe()`.
Use `BloodProfileData.profile(entityType, clientSide)` for entity composition and
`BloodSampleData.profile(vial, clientSide)` for a specimen's current traits and identification.
The latter replaces the old supported-properties argument with an explicit side selection.
The removed tendency tag constants and `getEntityTagForTendency` are no longer API entry points.
