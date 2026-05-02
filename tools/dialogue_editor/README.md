# Dialogue Workspace Editor

Java-first workspace editor for Hemomancy dialogue trees, translations,
dialogue inquiry JSON, and dialogue event references.

The mod runtime still uses the Java `*DialogueTrees.java` files as the source
of truth. This tool reads those files, builds an editable workspace model, and
only writes repo files after you preview and apply a diff.

## Quick Start

From `tools/dialogue_editor`:

```bash
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5174/workspace.html
```

The dev command starts:

- Vite UI on port `5174`
- Dialogue Workspace API on port `5175`

## What The Workspace Edits

- Dialogue builder chains in `src/main/java/.../npc/dialogue/*DialogueTrees.java`
- Translation entries in `src/main/resources/assets/hemomancy/lang/en_us.json`
- Item inquiry JSON in `src/main/resources/data/hemomancy/dialogue_inquiry/`
- Event IDs handled by `DialogueEventHandler`
- Memo-producing dialogue events as a read-only catalog from `MemoDefinitions`

## Main Tabs

- `Graph` - node graph preview and node/option selection.
- `Translations` - inline text editing for keys used by the selected file.
- `Events` - handled event catalog, memo event catalog, and new event stub queue.
- `Item Inquiries` - editable inquiry line-key JSON files.
- `Validation` - broken links, duplicate nodes, unknown events, missing lang keys.
- `Diff` - generated patch preview before any file write happens.

## Save Model

1. Edit nodes, options, translation values, inquiry lines, or queue a new event.
2. Click `Preview Diff`.
3. Review generated Java/lang/JSON patches.
4. Click `Apply Preview` only when the preview is valid.

The preview step does not write files. The apply step only writes the exact
validated preview currently shown.

## Tests And Build

```bash
npm test
npm run build
```

The backend tests cover Java parsing, private helper tree discovery, memo event
expression preservation, validation diagnostics, event/memo catalogs, and
preview/apply safety.

## Legacy JSON Tool

The original self-contained editor is still available as `index.html`, with
`export_to_json.py` and `import_from_json.py`. It is useful for quick visual
checks or older JSON snapshots, but the workspace app is now the safer authoring
flow for live repo files.
