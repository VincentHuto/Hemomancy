# Saint model and material reconciliation

This presentation pass follows the October 26 reconciliation. Encounter mechanics,
damage, reward rules and save keys remain governed by the existing implementation.

- Hemorath has irregular iron scabs and spikes erupting from ribs, limbs and skull.
  His hollow skeleton remains exposed. Pressure geometry uses Animus with Ferric
  chains rather than the former Mortem shading.
- Seraphae's animated wing joints now carry narrow branching axons and luminous
  nodes, with open space between them instead of feather vanes. White vascular
  radiance and intermittent gold impulses distinguish the nerves. Existing joint
  identifiers remain stable for animation and captured attachments.
- Putriciel has additional growing back colonies and hanging rot. Cloth, caps and
  discharge use decayed olive and bone tones. Colonies swell before the rot nova
  and contract during absolution. Cleansing fire belongs to the absolution window;
  ordinary spores, nova and melee remain rot. A dedicated `manipulation_putriciel_rot`
  shader retains Mortem's sludge/hyphae shapes with olive depths and pale fungal
  edges; other Mortem effects retain their existing shader.
- Velorum wears a deep plain hood over an exposed dark face, with a knotted cord,
  reduced chest ornament and shorter mantle train. The cold ascetic silhouette
  retains the posed lanterns, claw and staff used by existing effects.

Regenerate with `python tools/model_export/<saint>.py --preview`. Editable
Blockbench projects embed the same atlases as the generated runtime models.
Generator geometry checks run with
`python tools/model_export/test_saint_reconciliation.py`.

Validation:

- Generator reconciliation checks: 4 passed. Each exporter also enforces the
  quarter-unit grid and minimum cube thickness before emitting Java/Blockbench.
- Focused Saint JVM checks: 67 passed, including the regression proving flame
  geometry is absent outside Putriciel's absolution window.
- Full JVM suite: 4,331 tests, zero failures/errors. `test jar compileGameTestJava`
  completed successfully. Compiling GameTests does not execute them.
- Native Hemorath review: all 8 scenes passed, including stored blood and overload.
- Native Velorum review: all 8 scenes passed, including concurrent casts and rest.
- Native Putriciel review: all 9 scenes passed again with the dedicated olive rot
  shader, including the open window, overlapping nova/recoil and final rest.
- Native Seraphae rise: 7 frames captured successfully with the revised nerve
  wings. This is an entrance/model review, not a full containment-fight review.
- The final JAR's 8 Saint textures and 4 changed shader resources match authored
  resources byte for byte. The shared decay-noise include compiles in the native
  client; no shader compilation failures were found in these runs.

Evidence: `build/saint-cleanup-final-tests.log`,
`build/saint-cleanup-hemorath-client.log`,
`build/saint-cleanup-velorum-client.log`, `build/saint-cleanup-final-client.log`,
`build/saint-cleanup-seraphae-client.log`, and the reports and screenshots under
`build/boss-presentation-review/`. Model sheets are `build/<saint>_preview.png`.
These fixtures do not establish multiplayer or natural Survival acceptance.
