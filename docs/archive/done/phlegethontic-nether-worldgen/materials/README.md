# Material and model sources

The two original concept images and `conversation background.md` in the parent folder are preserved unchanged.

`scab-source.png`, `ichor-source.png`, and `sagittary-source.png` were generated for this implementation. The material direction was dark scorched blood/netherrack, arterial red ichor with sparse fatty foam and black clots, and exposed muscle/bone/vessel/hoof surfaces. Orange highlights were kept restrained. These are source materials, not screenshots or reference-anatomy replacements.

`tools/model_export/prepare_phlegethontic_resources.py` packs the scab tile and entity atlas, copies the current ichor animation masters, and emits the related resource JSON. The native fluid has light level 7 and no bucket item.

The current ichor artwork lives in [ichor-16px](ichor-16px/). Built-in ImageGen produced separate seamless surface and downward-current master tiles; [generation-prompts.json](ichor-16px/generation-prompts.json) preserves both the original and current prompts. The current outputs are `still-source-v2.png` and `flow-source-v2.png`; the first pass remains as `still-source.png` and `flow-source.png`, and `previous/` preserves the earlier installed fluid assets.

Run `pwsh -File tools/model_export/pack_phlegethontic_ichor.ps1` from the checkout to export and install the resources. The packer reduces the generated art to an eight-color blood/clot/foam palette and packs exact 16×16 frames. Still uses 48 authored convection frames over 48 ticks instead of cross-fading between four unrelated keys. Flow uses 64 frames over 64 ticks, combining downstream travel with changing lateral bends and differently paced surface motes instead of translating one tile. Interpolation is disabled: every transition is real pixel motion at 20 frames per second rather than a texture fade. The matching 16×16 underwater overlay retains alpha 105. [preview.png](ichor-16px/preview.png) shows eight evenly spaced still frames on the top row and eight flow frames on the bottom row at nearest-pixel scale; [animation-preview.gif](ichor-16px/animation-preview.gif) plays both complete loops side by side.

The Blockbench Java export is kept directly in `ExcoriatedModel`, alongside its animation code. It preserves the left arm-bow and separate right drawing arm. The Blockbench file contains editable geometry, named articulated groups, UVs, and an embedded texture. Do not expect Java animation logic to appear as Blockbench timeline tracks.

Model: `src/main/resources/assets/hemomancy/models/entity/bbmodel/excoriated.bbmodel`.

Texture: `src/main/resources/assets/hemomancy/textures/entity/excoriated/excoriated.png`.
