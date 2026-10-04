# Centrifuge upgrade kit sprites - 2026-09-30

Both upgrades now have distinct authored native 16x16 RGBA sprites. The Centrifugal Governor Kit replaces the earlier generic governor texture with a dark iron regulator, a bone-rimmed crimson calibration dial, and a short mounting spindle. The Fractionating Rotor Kit replaces its Condenser sprite reference with a broad forked iron crosshead, red central hub, and two separate glass fractions. Their shapes and material palette follow the new machine models.

## Assets and source

- `src/main/resources/assets/hemomancy/textures/item/centrifugal_governor_kit.png`
- `src/main/resources/assets/hemomancy/textures/item/fractionating_rotor_kit.png`
- Each corresponding generated item model references its own texture under `hemomancy:item/`.
- Explicit 16-row, 16-character grids and the twelve-color shared palette: `tools/model_export/centrifuge_kit_sprites.json`.
- Regenerate: `python tools/model_export/export_centrifuge_kit_sprites.py`.
- Check without writing: `python tools/model_export/export_centrifuge_kit_sprites.py --check`.
- Native-size dark/light samples and enlarged nearest-neighbor comparison: `output/textures/centrifuge_upgrade_kits/centrifuge_upgrade_kits_preview.png`.
- Original generated concept references are saved alongside that preview. They are reference art, not the installed textures.

## Generation method and prompt set

Built-in ImageGen created one transparent concept per asset. The final textures were then deliberately authored as explicit native pixel grids, preserving the concept silhouettes and key cues rather than downsampling the enlarged output.

Governor prompt: One Minecraft inventory item concept for the D4 Centrifugal Governor Kit: a compact mechanical blood-pressure governor with a chunky octagonal dial, pale calcified rim, deep crimson glass face, one pale needle, charcoal iron housing, short spindle, and small foot. A slight three-quarter angle shows housing thickness. Coarse 16x16 pixel-art intent, roughly twelve pixels wide and fourteen high; full item centered on transparency, maximum twelve flat colors, chunky clusters, upper-left light, and a dark contour. Clinical gothic machinery. Exclude lettering, numerals, runes, cables, gradients, glow, antialiasing, soft shadows, backgrounds, and checkerboards. The dial dominates the silhouette.

Fractionating prompt: One Minecraft inventory item concept for the D6 Fractionating Rotor Kit: a removable dark iron rotor crosshead with calcified trim, a crimson central hub, and two short glass receiver tubes underneath. The left fraction is fuller than the right, with pale desaturated glass edges, dark collars, and iron base fittings. A wide forked silhouette with a visible gap between the receivers, front view with slight upper-left depth. Coarse 16x16 pixel-art intent, about thirteen pixels wide and high; full item centered on transparency, maximum twelve flat colors, contiguous clusters, and dark contours. Exclude lettering, numerals, gold, glow, gradients, antialiasing, soft shadows, backgrounds, checkerboards, tiny gears, and decorative cables.

## Verification

- Governor: 16x16, 10 opaque colors, 138 visible pixels, alpha values 0/255.
- Fractionating: 16x16, 11 opaque colors, 136 visible pixels, alpha values 0/255.
- Export pixels match the explicit source grids exactly.
- Item models reference their respective texture IDs.
- Both textures are distinct from one another and the Condenser sprite.
- Enlarged nearest-neighbor previews and 32-pixel dark/light inventory samples inspected.
- `./gradlew.bat processResources assemble --offline --console=plain` passed.
- Final assembled JAR entries match the source PNG bytes; packaged dimensions and model references checked.
- `git diff --check` passed.

Build log: `build/centrifuge-sprite-validation.log`. This pass verified the native assets and their packaging; it did not repeat gameplay tests or live-client rendering.
