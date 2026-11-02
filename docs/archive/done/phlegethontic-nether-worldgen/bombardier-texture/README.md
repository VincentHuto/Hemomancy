# Bombardier texture repaint

The September 14 reference repaint replaces the debug atlas with mottled charcoal-brown chitin, narrow rusty-red wing markings, dark red abdominal joints, and worn bone accents on the wing rims, nozzle, feet, and antenna tips.

The runtime PNG is `src/main/resources/assets/hemomancy/textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png`. The matching PNG is embedded in `src/main/resources/assets/hemomancy/models/entity/bbmodel/phlegethontic_bombardier.bbmodel`.

The PNG and the model's UV canvas are both 128 by 128, giving one painted pixel per original UV unit at Minecraft's native 16x density. The earlier 512 by 512 atlas was reduced with nearest-neighbor sampling, preserving hard pixel edges and the original UV layout. All 62 cuboids, 372 face rectangles, pivots, rotations, groups, and animation code are unchanged. Unused atlas space remains transparent. The more rectangular silhouette and square nozzle are properties of the existing geometry, not changes made for this repaint.

## Paint sources and packing

The built-in image-generation tool produced `material-source.png` and `wing-source.png` from the user's reference. The exact initial, refinement, and wing prompts are in [generation-prompts.txt](generation-prompts.txt). No CLI/API fallback was used.

`pack_texture.cjs` crops and resamples those painted sources into the existing face rectangles, assigns the material boundaries to the authored anatomy, downsamples to the native 128 by 128 atlas, and synchronizes the embedded PNG. It requires Node.js and `sharp` on the module path. Run `node pack_texture.cjs` from this directory. It checks for UV overlaps and compares every non-texture model field with the pre-repaint backup under `build/bombardier-texture-review/`.

## Visual verification

`render_preview.py` uses Blender 4.3 and the existing Orcadian cuboid renderer to render the actual BBModel with nearest-neighbor texture sampling and its group rotations. The spray preview applies the current runtime animation's endpoint rotations; it does not edit the authored rest pose.

Run `blender --background --python render_preview.py` to recreate the resting and spray previews. These are offline model renders, not Minecraft screenshots. In-game lighting and animation still need a live-client check.

Focused verification: `gradlew.bat test --tests "*PhlegethonticBombardierResourceTest" --tests "*PhlegethonticBombardierModelTest" jar --console=plain`.

This change is cosmetic. No lore, progression, compatibility, spawning, combat, or animation behavior changes were needed.
