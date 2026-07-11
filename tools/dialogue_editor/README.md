# Hemomancy Dialogue Studio

A visual, Java-first authoring studio for Hemomancy NPC conversations.

The studio reads the live `*DialogueTrees.java` sources, English language file,
item inquiries, event catalog, and memo catalog. Edits remain in a recoverable
workspace draft until they pass validation and are explicitly applied.

## Start

```bash
cd tools/dialogue_editor
npm install
npm run dev
```

Open `http://127.0.0.1:5174/workspace.html`.

## Authoring workflow

- Pick an NPC and dialogue tree from the left navigator.
- Read and edit complete NPC prose and player responses directly on the canvas.
- Choose destinations and events without manually copying identifiers.
- Drag a node to pin it; unpinned nodes use automatic left-to-right layout.
- Use **Play-through** to walk the conversation and jump between visited nodes.
- Use **Routes** to follow read-only Java router conditions into concrete trees.
- Use **Inquiries** for item-specific NPC responses.
- Press `Ctrl/Cmd + K` to find any NPC, tree, node, or spoken line.
- Open **Changes** or click **Review changes** for validation, a readable summary,
  optional raw patches, and the final Apply action.

The draft is restored after a browser refresh when the underlying source
revision still matches. If source changes outside the studio, preview/apply is
blocked until the workspace is reloaded and reconciled.

## Safety model

- Java and resource files are never written while editing.
- Preview validates all submitted dialogue trees and creates exact file diffs.
- Apply rejects invalid, unknown, or stale previews.
- Arbitrary Java router logic is visualized but never rewritten.
- Existing localization keys are preserved; new content receives stable keys.

## Verification

```bash
npm test
npm run build
```
