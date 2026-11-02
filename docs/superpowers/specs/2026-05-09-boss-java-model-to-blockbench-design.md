# Boss Java Model To Blockbench Design

## Goal
Generate editable Blockbench modded-entity `.bbmodel` files for the recently edited saint bosses and Annetta model variants from their current Java `createBodyLayer()` geometry.

## Scope
- `HollowVesselModel`
- `SeraphaeModel`
- `ContainmentAnchorModel`
- `SeraphaeFragmentModel`
- `AnnettaKnowlesModel`
- `StainedPriestessModel`
- `LatentAnnettaInfectionModel`
- `PutricielModel`
- `VelorumModel`

## Output
Write generated models to:

`src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/`

The generated files use the repo's existing Blockbench modded entity schema:
- `meta.format_version: "4.0"`
- `model_format: "modded_entity"`
- `box_uv: true`
- `modded_entity_version: "1.17"`
- one texture entry pointing at the renderer's current base texture

## Conversion Rules
- Parse the Java source directly so generated files reflect the actual code, not stale docs.
- Preserve explicit part names from `root.addOrReplaceChild`.
- Preserve cube UV offsets from `.texOffs(u, v)`.
- Preserve cube geometry from `.addBox(x, y, z, width, height, depth)`.
- Preserve `CubeDeformation` as Blockbench `inflate`.
- Preserve part pivots from `PartPose.offset` and `PartPose.offsetAndRotation`.
- Convert static Java part rotations from radians to Blockbench degrees.
- Preserve `mirror()` intent with `mirror_uv: true` on affected elements.
- Generate an outliner group per Java part, with each part's cubes as children.

## Non-Goals
- Do not bake runtime `setupAnim()` state into base geometry.
- Do not alter Java model classes or renderers.
- Do not generate GeckoLib animation files.

## Verification
- Run the exporter and require all nine target models to be generated.
- Validate every generated `.bbmodel` parses as JSON and uses `modded_entity`.
- Run `./gradlew.bat build` to ensure the added resources do not break the mod build.
