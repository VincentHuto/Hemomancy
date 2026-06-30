# Lowtide Ruin Structure Kit

Editable vanilla-block mockups for Mnemonic Lowtide foreground ruin OBJ planning live in:

`src/main/resources/data/hemomancy/structure/lowtide_ruin/`

Use these as in-game sculpting bases, then revise/save/export the final shapes for OBJ conversion. The current renderer uses converted OBJ foreground ruin assets for the nearest Lowtide sky clusters while keeping procedural far/distant silhouettes for depth. These NBT templates are not wired into worldgen and remain artist/developer source material.

## Placement Commands

```mcfunction
/place template hemomancy:lowtide_ruin/tower_broken
/place template hemomancy:lowtide_ruin/tower_short
/place template hemomancy:lowtide_ruin/arch_fragment
/place template hemomancy:lowtide_ruin/dome_chapel
/place template hemomancy:lowtide_ruin/foundation_rubble
/place template hemomancy:lowtide_ruin/page_slab
/place template hemomancy:lowtide_ruin/spire_fragment
/place template hemomancy:lowtide_ruin/kit_showcase
```

## Piece Intent

- `tower_broken`: tall near-view church/library tower with broken upper edge and spire cap.
- `tower_short`: smaller secondary tower for asymmetrical clusters.
- `arch_fragment`: thick freestanding broken arch for the best near silhouette.
- `dome_chapel`: domed chapel/library mass with open front and window cuts.
- `foundation_rubble`: low island foundation and rubble base.
- `page_slab`: tilted-open-page style slab for parchment/book silhouettes.
- `spire_fragment`: narrow spire shard for cluster accents.
- `kit_showcase`: all pieces laid out in one template for quick inspection.

Keep the final converted models low-poly and silhouette-first. The renderer can still use procedural distant ruins while replacing only near hero clusters with authored assets.
