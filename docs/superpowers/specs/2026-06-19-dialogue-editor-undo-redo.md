# Dialogue Editor Undo/Redo

**Date:** 2026-06-19
**Status:** Approved

## Problem

The dialogue editor has no undo/redo. Any accidental node rename, line delete, option edit, or trigger change requires manual correction. With the redesigned inspector (contextual panels, one-click delete) the risk of accidental edits is higher than before.

## Chosen Approach

Snapshot-based undo/redo with a `pushUndo()` helper. Before any mutation, the caller snapshots the six undoable state fields onto an undo stack. Ctrl+Z pops and restores; Ctrl+Y/Ctrl+Shift+Z redoes.

---

## Snapshot Type

```ts
type UndoSnapshot = {
  workspace: DialogueWorkspace | null;
  dirtyTranslations: Record<string, string>;
  dirtyInquiries: [string, DialogueInquiryEntry][];
  createdInquiryPaths: string[];
  newEvents: string[];
  metadata: Record<string, NpcMetadata>;
};
```

Maps and Sets are serialized to arrays for `JSON.parse(JSON.stringify(...))` deep-clone. `workspace` and `metadata` are cloned the same way.

### Fields included

| Field | Reason |
|---|---|
| `workspace` | Dialogue trees, nodes, options — the core editable content |
| `dirtyTranslations` | Translation text edits that feed into preview/apply |
| `dirtyInquiries` | Inquiry line edits that feed into preview/apply |
| `createdInquiryPaths` | New inquiry files created this session |
| `newEvents` | Event stubs queued for the handler |
| `metadata` | Animation/sound trigger sidecar JSON |

### Fields excluded

| Field | Reason |
|---|---|
| `graphPositions` | Cosmetic drag layout — surprising to undo |
| `selectedRow` | UI navigation state |
| `tab`, `fileIndex`, `collapsedFileGroups` | UI-only |
| `preview` | Always cleared on any mutation |
| `message` | Status text |

---

## Stack Management

- `state.undoStack: UndoSnapshot[]` and `state.redoStack: UndoSnapshot[]` added to the state object, both starting empty.
- Max depth: **50** entries. When `undoStack` exceeds 50, `shift()` drops the oldest.
- Any new edit clears `redoStack` (branching history — no divergent futures).
- Both stacks clear on `init()` (workspace reload).

---

## `pushUndo()`, `undo()`, `redo()`

All three exported from `state.ts`.

### `pushUndo()`

```ts
export function pushUndo(): void {
  state.undoStack.push(snapshot());
  if (state.undoStack.length > 50) state.undoStack.shift();
  state.redoStack = [];
  state.preview = null;
}
```

Called by every mutation site before mutating. `snapshot()` is a private helper that performs the deep-clone and Map/Set serialization.

### `undo()` / `redo()`

```ts
export function undo(): boolean {
  if (!state.undoStack.length) return false;
  state.redoStack.push(snapshot());
  restoreSnapshot(state.undoStack.pop()!);
  return true;
}

export function redo(): boolean {
  if (!state.redoStack.length) return false;
  state.undoStack.push(snapshot());
  restoreSnapshot(state.redoStack.pop()!);
  return true;
}
```

`restoreSnapshot()` converts arrays back to Maps/Sets and writes all six fields into `state`. It also sets `state.preview = null`.

### Metadata re-sync

After `restoreSnapshot()`, `undo()`/`redo()` compare the restored `state.metadata` against the pre-restore snapshot. For any slug whose value differs, they call `pushMetadata(slug, data)` (fire-and-forget, matching existing `saveTrigger` behavior) to keep the server's sidecar JSON in sync with the restored state.

---

## Mutation Sites

Every mutation calls `pushUndo()` before mutating. Call sites:

### `inspector.ts`

- Node rename (`change` on `#node-id`)
- Add line (`#add-line` click)
- Delete line (`[data-delete-line]` click)
- Edit line key (`[data-line]` input — currently only updates `node.lines[i]`, add `pushUndo()` before)
- Add option (`#add-option` click)
- Delete option (`#delete-option` click)
- Edit option text (`#opt-text` input)
- Edit option next (`#opt-next` change)
- Edit option event (`#opt-event` input)
- Delete node (`#delete-node` click)
- Animation trigger save (`#opt-animation` change → `saveTrigger`)
- Sound trigger save (`#opt-sound` change → `saveTrigger`)

### `main.ts`

- Translation edit (`[data-translation]` textarea `oninput`)
- Add event stub (`#add-event` click)
- Inquiry line edit (`[data-inquiry-line]` input `oninput`)
- Delete inquiry line (`[data-delete-inquiry-line]` click)
- Add inquiry (`#add-inquiry` click)

---

## Keyboard Shortcuts

Registered once on `document` in `main.ts`:

- `Ctrl+Z` (or `Cmd+Z` on Mac) → `undo()` → if returned true, `render()`
- `Ctrl+Y` or `Ctrl+Shift+Z` (or `Cmd+Shift+Z`) → `redo()` → if returned true, `render()`

Check `e.metaKey || e.ctrlKey` for cross-platform support. Call `e.preventDefault()` to suppress browser default undo behavior in text inputs.

---

## UI

Two buttons added to the existing `<header class="topbar">` in `main.ts`:

```html
<button id="undo" disabled>↩ Undo</button>
<button id="redo" disabled>↪ Redo</button>
```

Positioned between the brand and the Reload button. Disabled state reflects whether the respective stack is non-empty. Updated in `render()` by checking `state.undoStack.length` and `state.redoStack.length`.

No undo history panel — buttons + keyboard are sufficient.

---

## Out of Scope

- Per-keystroke coalescing (e.g., grouping rapid typing into one undo step) — each `oninput` is its own snapshot.
- Undo history persistence across page reloads.
- Visual diff of what changed between undo steps.
- Undo of `graphPositions` (drag layout).
