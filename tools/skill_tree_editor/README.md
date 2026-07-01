# Hemomancy Skill Tree Editor

Browser-based editor for the Java-owned Hemomancy skill tree. The layout canvas mirrors the in-game skill screen: square item nodes, red orthogonal links, vertical degree tiers, and draggable node positions.

## Run

```powershell
cd tools/skill_tree_editor
npm install
npm run dev
```

Open `http://127.0.0.1:5184/workspace.html`.

Manipulation editor: `http://127.0.0.1:5184/manipulations.html`.

Material atlas editor: `http://127.0.0.1:5184/materials.html`.

The API defaults to port `5185`. Set `HEMO_REPO_ROOT` when running the tool outside this folder.

## Source Model

The editor ignores the old `data/hemomancy/skilltrees` JSON folder. It reads Java branch files under:

```text
src/main/java/com/vincenthuto/hemomancy/common/init/skills
```

Only declarations between `// <skill-editor branch="...">` and `// </skill-editor>` are rewritten. The tool previews Java and lang-file diffs before applying them.

Skill node positions are stored in Java with `.setTreePosition(x, y)`. The in-game skill tab reads those same content-space coordinates, so moving a node in the browser changes the authored game layout after preview/apply.

The material atlas editor reads `MaterialAtlasSpec.java` and `MaterialsData.java`. Bucket roots, category label plaques, gates, parent veins, catalogue text, and icon registry fields are previewed back into those Java files. Material nodes keep the existing auto-layout until moved; moved nodes are written as `entryAt(...)` calls with explicit atlas coordinates.
