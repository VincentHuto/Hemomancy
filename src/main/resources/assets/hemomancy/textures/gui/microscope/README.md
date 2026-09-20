# Editing the microscope screen

Edit these PNG files directly. The game loads them as textures; the Python generator is **not** part of rendering, building, or resource reload. PNG transparency controls the opacity of each screen surface.

| PNG | Size | Controls |
| --- | --- | --- |
| `frame.png` | 256 x 256 | Brass lens rim, screws, outer glass backing, dust and glass highlights. Drawn over the cells. |
| `plasma.png` | 256 x 256 | Cellular field beneath the cells. Transparent outside the lens. |
| `readout.png` | 168 x 240 | Entire right-hand panel, including its top border. Text is drawn over this sprite. |
| `screen_dimming.png` | 16 x 16 | Background overlay, stretched across the screen. Reduce its alpha or erase it to show more of the world. |
| `progress_track.png` | 148 x 3 | Empty progress bar. |
| `progress_fill.png` | 148 x 3 | Filled progress bar, revealed from left to right without stretching. |
| `tendencies/*.png` | 32 x 32 each | Eight individually editable tendency cell sprites. |
| `properties/*.png` | 32 x 32 each | Twelve individually editable biological motifs. |
| `ductilis_link.png` | 32 x 32 | Connections between branching Ductilis cells. |

Special specimens use individually editable transparent sprites under `special/`. The renderer only places,
rotates, fades, and clips these PNGs; it does not paint their shapes with GUI fill primitives.

| PNG | Size | Controls |
| --- | --- | --- |
| `special/hemolymph_plasma.png` | 256 x 256 | Blue cellular field beneath Cleansing Hemolymph pieces. |
| `special/hemolymph_cell.png` | 32 x 32 | Bright-blue Cleansing Hemolymph cell. |
| `special/hemolymph_spot.png` | 8 x 8 | White hemolymph inclusion. |
| `special/hemolymph_filament.png` | 32 x 32 | Fine hemolymph connective strand. |
| `special/hemorath_iron.png`, `special/hemorath_void.png` | 16 x 16 | Hemorath's ferric and mortem inclusions. |
| `special/seraphae_halo.png`, `special/seraphae_mote.png` | 32 x 32, 8 x 8 | Seraphae's animus and lux inclusions. |
| `special/putriciel_ember.png`, `special/putriciel_spore.png` | 16 x 16 | Putriciel's flammeus and mortem inclusions. |
| `special/velorum_crystal.png`, `special/velorum_shadow.png` | 16 x 16 | Velorum's congeatio and tenebris inclusions. |

Tendency files: `animus.png`, `flammeus.png`, `ductilis.png`, `lux.png`, `mortem.png`, `congeatio.png`, `ferric.png`, and `tenebris.png`.

Property files: `aquatic.png`, `flying.png`, `venomous.png`, `arthropod.png`, `cold_native.png`, `nether_native.png`, `ender.png`, `undead.png`, `fungal.png`, `explosive.png`, `burrowing.png`, and `neutral.png` (the fallback for unknown properties).

Edit the individual files in these subfolders. The original `tendencies.png` and `properties.png` atlases are retained as references but are no longer loaded by the microscope renderer. All extracted icons preserve their original pixels and transparency.

The ordinary blood-cell sprite is `../../particle/particle_blood_cell.png`; it is shared with other blood effects.

Keep the listed canvas sizes and filenames. Static screen sprites render with white tint, preserving the painted colors and alpha. Tendency sprites use the existing tendency palette as a tint, so use light neutral shading for those. Property sprites also receive a pale tint. Cell placement, drift, pulses, lens-edge fading, text, and examination progress remain dynamic Java behavior. Changing the overall layout or circular cell bounds still requires adjusting the renderer's coordinates.

The logical screen canvas is 448 x 276, scaled uniformly to fit the window:

- Left slide: `(8, 10)`, 256 x 256.
- Readout: `(272, 18)`, 168 x 240. Text starts at panel-local `(10, 11)` and wraps at 148 pixels.
- Progress bar: `(282, 240)`, 148 x 3; panel-local `(10, 222)`.

For a resource pack, use the same `assets/hemomancy/textures/gui/microscope/` paths. Reload resources with **F3+T**. For source edits during development, run `gradlew.bat processResources` before reloading the running client, or restart through Gradle so it copies the edited resources.

`tools/model_export/hematic_microscope.py` now creates only missing GUI PNGs. Existing GUI PNGs are preserved. Its explicit `--reset-gui-textures` option replaces GUI artwork with the original defaults, so do not use that option on your painted versions. Missing individual icons are extracted from a retained atlas when available, otherwise generated from the defaults. Existing individual icons are never replaced without `--reset-gui-textures`. The script still rebuilds the handheld item model and its item texture separately; `--model-only` leaves all textures untouched.
