# School combat implementation work log

Approved scope: all eight explicitly school-aligned combat sources, preserving mitigation, existing ability geometry/progression, unrelated changes, blood-volume/vascular systems, and purification. Execute inline in the current checkout.

## Checklist

- [ ] Damage context, source attribution, single affinity resolution, confirmed-hit accounting.
- [ ] Persistent/synced school state attachment and pure rule tests.
- [ ] Lux exposure and Tenebris awareness/concealment/zones.
- [ ] Rime, Disrupted, existing paralysis/conduction integration.
- [ ] Pressure, Necrosis, Lodestone, Searing and four bounded interactions.
- [ ] All manipulation/entity/Drudge/weapon/projectile/Will/boss/Morphling source adapters and migration inventory.
- [ ] Sanguine Marionette family, control lifecycle, memory/resources.
- [ ] Status presentation, cure integrations, documentation.
- [ ] Dedicated behavioral GameTests, JVM tests, alphaCheck/build, multiplayer visual acceptance.

## Baseline

No SchoolHitContext/SchoolDamageSource/new school effects existed when work started. Existing unrelated edits: reference/wiki docs, manipulation node presentation and its test, ConjurationManip, authored chair models, staged-deleted gource.webm and SpecimenJarData, editor backup files and Python cache. Preserve these. The index contains an unreadable gource object; avoid unrelated whole-tree diff operations.

Use registered effects for identity; attachments for meters/ownership/expiry. New hits retain their native DamageType. Hit preparation must not consume status until health or absorption damage is confirmed. Root hit accounting suppresses duplicate application and reactions. Existing HematicCommandManager provides command ownership/AI machinery; Marionette extends it.

## Validation log

Implementation has not yet been validated. Record commands and results here as work progresses.
