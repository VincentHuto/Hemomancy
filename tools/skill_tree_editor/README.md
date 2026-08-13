# Hemomancy Skill Tree Editor

Browser-based editor for the Java-owned Hemomancy skill tree. The layout canvas mirrors the in-game skill screen, including all six node shapes, toggleable decagon techniques, branch-colored links, degree tiers, and draggable node positions.

## Run

```powershell
cd tools/skill_tree_editor
npm install
npm run dev
```

Open `http://127.0.0.1:5184/workspace.html`.

Tendencies editor (manipulations and scars): `http://127.0.0.1:5184/tendencies.html`.

Material atlas editor: `http://127.0.0.1:5184/materials.html`.

Crafting and Rites map editor: `http://127.0.0.1:5184/recipe_maps.html`.

The API defaults to port `5185`. Set `HEMO_REPO_ROOT` when running the tool outside this folder.

## Source Model

The editor ignores the old `data/hemomancy/skilltrees` JSON folder. It reads Java branch files under:

```text
src/main/java/com/vincenthuto/hemomancy/common/init/skills
```

Only declarations between `// <skill-editor branch="...">` and `// </skill-editor>` are rewritten. The tool previews Java and lang-file diffs before applying them.

Skill node positions are stored in Java with `.setTreePosition(x, y)`. The in-game skill tab reads those same content-space coordinates, so moving a node in the browser changes the authored game layout after preview/apply.

The inspector preserves `.setNodeShape(EnumNodeShape...)` and `.setToggleable(true)`. Toggleable techniques are previewed in their unlocked/enabled decagon state with the same bronze border and subtle red glow used by the game.

The Tendencies editor loads manipulation metadata from `ManipulationInit.java` and scar metadata from `ScarInit.java`. It edits both `ManipulationTreeInit.java` and the explicit `authored(...)` entries in `ScarTreeLayout.java`, mirrors the combined in-game radial layout, and previews/applies both kinds of changes as one operation.

The material atlas editor reads `MaterialAtlasSpec.java` and `MaterialsData.java`. Buckets are cosmetic category metadata only: they provide the atlas color, category anchor, and label plaque, while explicit `parentIds` provide node lineage/vein links. Gates, parent veins, catalogue text, and icon registry fields are previewed back into those Java files. Material nodes keep the existing auto-layout until moved; moved nodes are written as `entryAt(...)` calls with explicit atlas coordinates.

The Crafting and Rites editor reads `HarbingerRecipeMapDefinitions.java` plus recipe JSON metadata. It edits family sectors, order within a family, progression/conceptual links, optional authored node positions, and optional `ItemInit` or `BlockInit` display icons for both radial maps. Nodes use their recipe result as the display icon until an override is assigned. Drag nodes to move them, Ctrl-click or Shift-drag to select multiple nodes, drag the background to pan, and use the mouse wheel or zoom buttons to zoom. Recipe degree remains owned by each recipe JSON and is shown as a read-only ring assignment. All edits use the same preview/apply workflow as the other editors.
