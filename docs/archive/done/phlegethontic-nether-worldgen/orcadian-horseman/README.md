# Orcadian Horseman

Open `orcadian_horseman.bbmodel` in Blockbench. It is a **Java modded entity** project made entirely from **58 rotated cuboids**, organized into 27 named groups. There are no mesh elements.

The forearm continues directly into the bow junction. Eight slender, angled cuboids form the upper and lower bow limbs. The drawing arm, bowstring, and arrow have their own groups. Horse legs, horse head, human head, and tail also have editable pivots.

`orcadian_horseman_atlas.png` is the matching 256 × 256 texture, also embedded in the project. Logical UV size is 256 × 256, giving one image pixel per model UV unit. Every cube uses a Java-compatible UV net. Skull, jaw, chest, and waist nets have extra texture space and explicit face UVs; the exporter matches these using per-cube texture scale. Keep that logical UV size when exporting.

Skull features, ribs, muscle shading, bone shading, and hoof detail are painted details. They are not built from small cubes. The original painted source was generated with built-in ImageGen; `texture_atlas_prompt.txt` records its prompt. Blender transfers that artwork into the precise cube UV nets without baking scene lighting into it.

`preview_hero.png`, `preview_side.png`, `preview_rear.png`, and `preview_detail.png` show the actual cuboid model with its exported atlas. These are studio previews, not Minecraft screenshots. This asset is installed as the in-game Excoriated; Java drives its poses and string draw, including an extended dragging idle that retracts smoothly into the aiming pose. The BBModel itself has no animation clips.

## Rebuilding the asset

1. Run `python build_model.py`.
2. Run Blender in background mode with `--python bake_texture.py`.
3. Run `python build_model.py --embed-only`.
4. Run `python validate_model.py`.
5. Run Blender in background mode with `--python render_preview.py`.

The validator checks the native entity format, cube-only geometry, every box UV rectangle, UV bounds, hierarchy references, and embedded/external PNG agreement. `validation.json` records the result.

UV correction: the human skull front now uses 18.4 × 22 pixels instead of 4.6 × 5.5. The Java UV parity test compares all 348 baked face rectangles against the BBModel. Geometry and idle animation are unchanged.
