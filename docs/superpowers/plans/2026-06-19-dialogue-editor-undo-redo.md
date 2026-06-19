# Dialogue Editor Undo/Redo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Ctrl+Z / Ctrl+Y undo/redo to the dialogue editor covering all mutable state: dialogue trees, translations, inquiries, events, and sidecar metadata.

**Architecture:** A `pushUndo()` helper snapshots all undoable state fields via `JSON.parse(JSON.stringify(...))` onto a 50-entry undo stack in `state.ts`. Every mutation site calls `pushUndo()` before mutating. `undo()` / `redo()` pop their respective stacks and restore state; if sidecar metadata changed, the caller re-pushes the affected slugs to the server.

**Tech Stack:** TypeScript, Vitest (tests auto-discovered via `src/**/*.test.ts`), vanilla DOM.

## Global Constraints

- All files in `tools/dialogue_editor/` are gitignored — every `git add` command must use `-f`
- No new dependencies — no libraries added
- `state.ts` must not import from `api.ts` (circular dep) — metadata re-sync is handled in `main.ts` after calling `undo()`/`redo()`
- Undo stack max depth: 50 entries (drop oldest with `shift()` when exceeded)
- `pushUndo()` clears `redoStack` (branching history — no divergent futures)
- Both stacks clear in `init()` on workspace reload
- Keyboard: `Ctrl+Z` / `Cmd+Z` = undo; `Ctrl+Y` / `Ctrl+Shift+Z` / `Cmd+Shift+Z` = redo; `e.preventDefault()` on all to suppress browser default
- UI: two buttons `↩ Undo` and `↪ Redo` in the topbar, disabled when their stack is empty

---

### Task 1: Undo/redo core in `state.ts`

**Files:**
- Modify: `tools/dialogue_editor/src/frontend/state.ts`
- Create: `tools/dialogue_editor/src/frontend/state.test.ts`

**Interfaces:**
- Produces:
  - `export function pushUndo(): void`
  - `export function undo(): boolean` — returns `false` if stack empty
  - `export function redo(): boolean` — returns `false` if stack empty
  - `state.undoStack: UndoSnapshot[]`
  - `state.redoStack: UndoSnapshot[]`

- [ ] **Step 1: Write the failing tests**

Create `tools/dialogue_editor/src/frontend/state.test.ts`:

```ts
import { beforeEach, describe, expect, test } from 'vitest';
import { pushUndo, redo, state, undo } from './state';

function resetState(): void {
  state.workspace = null;
  state.dirtyTranslations = {};
  state.dirtyInquiries = new Map();
  state.createdInquiryPaths = new Set();
  state.newEvents = new Set();
  state.metadata = {};
  state.preview = null;
  state.undoStack = [];
  state.redoStack = [];
}

describe('undo/redo', () => {
  beforeEach(resetState);

  test('pushUndo snapshots dirtyTranslations and clears redoStack', () => {
    state.dirtyTranslations = { 'key.a': 'hello' };
    state.redoStack = [{ workspace: null, dirtyTranslations: {}, dirtyInquiries: [], createdInquiryPaths: [], newEvents: [], metadata: {} }];
    pushUndo();
    expect(state.undoStack).toHaveLength(1);
    expect(state.undoStack[0].dirtyTranslations).toEqual({ 'key.a': 'hello' });
    expect(state.redoStack).toHaveLength(0);
    expect(state.preview).toBeNull();
  });

  test('undo restores previous state and pushes current to redoStack', () => {
    state.dirtyTranslations = { 'key.a': 'before' };
    pushUndo();
    state.dirtyTranslations = { 'key.a': 'after' };
    const result = undo();
    expect(result).toBe(true);
    expect(state.dirtyTranslations).toEqual({ 'key.a': 'before' });
    expect(state.undoStack).toHaveLength(0);
    expect(state.redoStack).toHaveLength(1);
  });

  test('redo re-applies undone state', () => {
    state.dirtyTranslations = { 'key.a': 'before' };
    pushUndo();
    state.dirtyTranslations = { 'key.a': 'after' };
    undo();
    const result = redo();
    expect(result).toBe(true);
    expect(state.dirtyTranslations).toEqual({ 'key.a': 'after' });
    expect(state.undoStack).toHaveLength(1);
    expect(state.redoStack).toHaveLength(0);
  });

  test('undo returns false when stack is empty', () => {
    expect(undo()).toBe(false);
  });

  test('redo returns false when stack is empty', () => {
    expect(redo()).toBe(false);
  });

  test('new edit (pushUndo) clears redoStack', () => {
    state.dirtyTranslations = { 'k': '1' };
    pushUndo();
    state.dirtyTranslations = { 'k': '2' };
    undo();
    expect(state.redoStack).toHaveLength(1);
    pushUndo();
    expect(state.redoStack).toHaveLength(0);
  });

  test('undoStack is capped at 50 entries, oldest dropped', () => {
    for (let i = 0; i < 55; i++) {
      state.dirtyTranslations = { key: String(i) };
      pushUndo();
    }
    expect(state.undoStack).toHaveLength(50);
    expect(state.undoStack[0].dirtyTranslations['key']).toBe('5');
  });

  test('snapshot deep-clones workspace so later mutations do not affect history', () => {
    state.workspace = {
      repoRoot: '/repo',
      dialogueFiles: [{ path: 'a.java', sourceFile: 'a.java', speaker: 'A', icon: '', trees: [], diagnostics: [] }],
      translations: {},
      inquiries: [],
      registries: [],
      events: [],
      memos: [],
      diagnostics: []
    };
    pushUndo();
    state.workspace.dialogueFiles[0].path = 'mutated.java';
    expect(state.undoStack[0].workspace!.dialogueFiles[0].path).toBe('a.java');
  });

  test('Maps and Sets are serialized and restored correctly', () => {
    state.dirtyInquiries = new Map([
      ['path/a.json', { path: 'path/a.json', npcId: 'alchemist', itemId: 'hemomancy/ore', lines: ['k1'], valid: true }]
    ]);
    state.newEvents = new Set(['my_event']);
    state.createdInquiryPaths = new Set(['path/a.json']);
    pushUndo();
    state.dirtyInquiries = new Map();
    state.newEvents = new Set();
    state.createdInquiryPaths = new Set();
    undo();
    expect(state.dirtyInquiries.get('path/a.json')?.npcId).toBe('alchemist');
    expect(state.newEvents.has('my_event')).toBe(true);
    expect(state.createdInquiryPaths.has('path/a.json')).toBe(true);
  });

  test('metadata is deep-cloned and restored', () => {
    state.metadata = { Acolyte: { version: 1, options: { 'method::0::node::0': { animationTrigger: 'kneel' } } } };
    pushUndo();
    state.metadata = {};
    undo();
    expect(state.metadata['Acolyte']?.options['method::0::node::0']?.animationTrigger).toBe('kneel');
  });
});
```

- [ ] **Step 2: Run tests to confirm they fail**

```
cd tools/dialogue_editor
npm test
```

Expected: FAIL — `pushUndo is not a function` (or similar import error).

- [ ] **Step 3: Add `UndoSnapshot` type, stack fields, and helper functions to `state.ts`**

At the top of `tools/dialogue_editor/src/frontend/state.ts`, after the existing imports, add the `UndoSnapshot` type:

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

In the `state` object literal, add two new fields after `metadata`:

```ts
  metadata: {} as Record<string, NpcMetadata>,
  undoStack: [] as UndoSnapshot[],
  redoStack: [] as UndoSnapshot[],
```

After the closing `};` of the `state` declaration, add the private helpers and the three exported functions:

```ts
function snapshot(): UndoSnapshot {
  return {
    workspace: state.workspace ? JSON.parse(JSON.stringify(state.workspace)) : null,
    dirtyTranslations: JSON.parse(JSON.stringify(state.dirtyTranslations)),
    dirtyInquiries: [...state.dirtyInquiries.entries()].map(([k, v]) => [k, JSON.parse(JSON.stringify(v))]),
    createdInquiryPaths: [...state.createdInquiryPaths],
    newEvents: [...state.newEvents],
    metadata: JSON.parse(JSON.stringify(state.metadata)),
  };
}

function restoreSnapshot(snap: UndoSnapshot): void {
  state.workspace = snap.workspace ? JSON.parse(JSON.stringify(snap.workspace)) : null;
  state.dirtyTranslations = JSON.parse(JSON.stringify(snap.dirtyTranslations));
  state.dirtyInquiries = new Map(snap.dirtyInquiries.map(([k, v]) => [k, JSON.parse(JSON.stringify(v))]));
  state.createdInquiryPaths = new Set(snap.createdInquiryPaths);
  state.newEvents = new Set(snap.newEvents);
  state.metadata = JSON.parse(JSON.stringify(snap.metadata));
  state.preview = null;
}

export function pushUndo(): void {
  state.undoStack.push(snapshot());
  if (state.undoStack.length > 50) state.undoStack.shift();
  state.redoStack = [];
  state.preview = null;
}

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

- [ ] **Step 4: Run `tsc --noEmit` to confirm no type errors**

```
cd tools/dialogue_editor
npx tsc --noEmit
```

Expected: no output (zero errors).

- [ ] **Step 5: Run tests to confirm all pass**

```
npm test
```

Expected: at least 9 tests passing in `state.test.ts`, plus the existing 10 server tests = 19 total.

- [ ] **Step 6: Commit**

```bash
git add -f tools/dialogue_editor/src/frontend/state.ts tools/dialogue_editor/src/frontend/state.test.ts
git commit -m "feat(dialogue-editor): add undo/redo core to state.ts with 9 unit tests"
```

---

### Task 2: Wire `pushUndo()` at every mutation site in `inspector.ts`

**Files:**
- Modify: `tools/dialogue_editor/src/frontend/inspector.ts`

**Interfaces:**
- Consumes: `pushUndo` from `./state` (Task 1)

No new tests for this task — inspector.ts is DOM-heavy. Correctness is verified by running the dev server and exercising each edit.

- [ ] **Step 1: Add `pushUndo` to the import in `inspector.ts`**

Change:

```ts
import { currentFile, currentNode, currentNodeTree, metadataKey, optionMeta, speakerSlug, state, translation } from './state';
```

To:

```ts
import { currentFile, currentNode, currentNodeTree, metadataKey, optionMeta, pushUndo, speakerSlug, state, translation } from './state';
```

- [ ] **Step 2: Add `pushUndo()` before the node rename mutation**

In `renderLinesPanel`, find:

```ts
  document.getElementById('node-id')!.addEventListener('change', e => {
    const next = (e.target as HTMLInputElement).value.trim();
    if (!next || next === node.id) return;
    const old = node.id;
```

Change to:

```ts
  document.getElementById('node-id')!.addEventListener('change', e => {
    const next = (e.target as HTMLInputElement).value.trim();
    if (!next || next === node.id) return;
    pushUndo();
    const old = node.id;
```

- [ ] **Step 3: Add `pushUndo()` before the add-line mutation**

Find:

```ts
  document.getElementById('add-line')!.onclick = () => {
    node.lines.push(suggestLineKey(tree, node));
```

Change to:

```ts
  document.getElementById('add-line')!.onclick = () => {
    pushUndo();
    node.lines.push(suggestLineKey(tree, node));
```

- [ ] **Step 4: Add `pushUndo()` before the add-option mutation**

Find:

```ts
  document.getElementById('add-option')!.onclick = () => {
    node.options.push({ text: suggestOptionKey(tree, node, node.options.length + 1), next: null, event: null });
```

Change to:

```ts
  document.getElementById('add-option')!.onclick = () => {
    pushUndo();
    node.options.push({ text: suggestOptionKey(tree, node, node.options.length + 1), next: null, event: null });
```

- [ ] **Step 5: Add `pushUndo()` before the delete-node mutation**

Find:

```ts
  document.getElementById('delete-node')!.onclick = () => {
    tree.nodes = tree.nodes.filter(n => n.id !== node.id);
```

Change to:

```ts
  document.getElementById('delete-node')!.onclick = () => {
    pushUndo();
    tree.nodes = tree.nodes.filter(n => n.id !== node.id);
```

- [ ] **Step 6: Add `pushUndo()` before the line key edit mutation**

Find:

```ts
  el.querySelectorAll<HTMLInputElement>('[data-line]').forEach(input => {
    input.oninput = () => { node.lines[Number(input.dataset.line)] = input.value; state.preview = null; };
  });
```

Change to:

```ts
  el.querySelectorAll<HTMLInputElement>('[data-line]').forEach(input => {
    input.oninput = () => { pushUndo(); node.lines[Number(input.dataset.line)] = input.value; state.preview = null; };
  });
```

- [ ] **Step 7: Add `pushUndo()` before the delete-line mutation**

Find:

```ts
    btn.onclick = () => {
      node.lines.splice(Number(btn.dataset.deleteLine), 1);
```

Change to:

```ts
    btn.onclick = () => {
      pushUndo();
      node.lines.splice(Number(btn.dataset.deleteLine), 1);
```

- [ ] **Step 8: Add `pushUndo()` before the option text edit mutation**

In `renderOptionPanel`, find:

```ts
  document.getElementById('opt-text')!.addEventListener('input', e => {
    option.text = (e.target as HTMLInputElement).value;
    state.preview = null;
  });
```

Change to:

```ts
  document.getElementById('opt-text')!.addEventListener('input', e => {
    pushUndo();
    option.text = (e.target as HTMLInputElement).value;
    state.preview = null;
  });
```

- [ ] **Step 9: Add `pushUndo()` before the option next (goes-to) mutation**

Find:

```ts
  document.getElementById('opt-next')!.addEventListener('change', e => {
    option.next = (e.target as HTMLSelectElement).value || null;
```

Change to:

```ts
  document.getElementById('opt-next')!.addEventListener('change', e => {
    pushUndo();
    option.next = (e.target as HTMLSelectElement).value || null;
```

- [ ] **Step 10: Add `pushUndo()` before the option event mutation**

Find:

```ts
  document.getElementById('opt-event')!.addEventListener('input', e => {
    option.event = (e.target as HTMLInputElement).value.trim() || null;
    state.preview = null;
  });
```

Change to:

```ts
  document.getElementById('opt-event')!.addEventListener('input', e => {
    pushUndo();
    option.event = (e.target as HTMLInputElement).value.trim() || null;
    state.preview = null;
  });
```

- [ ] **Step 11: Add `pushUndo()` before both trigger save calls**

Find:

```ts
  document.getElementById('opt-animation')!.addEventListener('change', e => {
    saveTrigger('animationTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('opt-sound')!.addEventListener('change', e => {
    saveTrigger('soundTrigger', (e.target as HTMLInputElement).value.trim());
  });
```

Change to:

```ts
  document.getElementById('opt-animation')!.addEventListener('change', e => {
    pushUndo();
    saveTrigger('animationTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('opt-sound')!.addEventListener('change', e => {
    pushUndo();
    saveTrigger('soundTrigger', (e.target as HTMLInputElement).value.trim());
  });
```

- [ ] **Step 12: Add `pushUndo()` before the delete-option mutation**

Find:

```ts
  document.getElementById('delete-option')!.onclick = () => {
    node.options.splice(optionIndex, 1);
```

Change to:

```ts
  document.getElementById('delete-option')!.onclick = () => {
    pushUndo();
    node.options.splice(optionIndex, 1);
```

- [ ] **Step 13: Run `tsc --noEmit` to confirm no type errors**

```
cd tools/dialogue_editor
npx tsc --noEmit
```

Expected: no output.

- [ ] **Step 14: Run tests to confirm still passing**

```
npm test
```

Expected: 19 tests passing (9 state + 10 server).

- [ ] **Step 15: Commit**

```bash
git add -f tools/dialogue_editor/src/frontend/inspector.ts
git commit -m "feat(dialogue-editor): wire pushUndo at all inspector mutation sites"
```

---

### Task 3: Wire `pushUndo()` in `main.ts`, add keyboard shortcuts and UI buttons

**Files:**
- Modify: `tools/dialogue_editor/src/frontend/main.ts`

**Interfaces:**
- Consumes: `pushUndo`, `undo`, `redo` from `./state` (Task 1)
- Consumes: `pushMetadata` from `./api` (already imported)

- [ ] **Step 1: Update imports in `main.ts`**

Change the `state` import line from:

```ts
import { currentFile, paletteFor, speakerSlug, state } from './state';
```

To:

```ts
import { currentFile, paletteFor, pushUndo, redo, speakerSlug, state, undo } from './state';
```

Change the shared types import to add `NpcMetadata`:

```ts
import type { Diagnostic, DialogueFile, DialogueInquiryEntry, DialogueTreeModel, DialogueWorkspace, NpcMetadata, PreviewResult } from '../shared/types';
```

- [ ] **Step 2: Add undo/redo buttons to the topbar HTML**

Find the `innerHTML` assignment for `#app` — specifically the topbar line:

```html
      <div class="brand">Hemomancy Dialogue Workspace</div>
      <button id="reload">Reload</button>
```

Change to:

```html
      <div class="brand">Hemomancy Dialogue Workspace</div>
      <button id="undo" disabled>↩ Undo</button>
      <button id="redo" disabled>↪ Redo</button>
      <button id="reload">Reload</button>
```

- [ ] **Step 3: Wire undo/redo button clicks and keyboard shortcut**

After the existing `document.getElementById('apply')!.onclick = ...` block (but before `init()`), add:

```ts
document.getElementById('undo')!.onclick = () => {
  const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
  if (undo()) { render(); syncMeta(beforeMeta); }
};
document.getElementById('redo')!.onclick = () => {
  const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
  if (redo()) { render(); syncMeta(beforeMeta); }
};
document.addEventListener('keydown', e => {
  const mod = e.metaKey || e.ctrlKey;
  if (!mod) return;
  if (e.key === 'z' && !e.shiftKey) {
    e.preventDefault();
    const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
    if (undo()) { render(); syncMeta(beforeMeta); }
  } else if (e.key === 'y' || (e.key === 'z' && e.shiftKey)) {
    e.preventDefault();
    const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
    if (redo()) { render(); syncMeta(beforeMeta); }
  }
});
```

- [ ] **Step 4: Add `syncMeta` helper function**

Add this function anywhere in `main.ts` (after `escapeHtml`/`escapeAttr` is a good spot):

```ts
function syncMeta(before: Record<string, NpcMetadata>): void {
  const after = state.metadata;
  const all = new Set([...Object.keys(before), ...Object.keys(after)]);
  for (const slug of all) {
    if (JSON.stringify(before[slug]) !== JSON.stringify(after[slug])) {
      pushMetadata(slug, after[slug] ?? { version: 1, options: {} });
    }
  }
}
```

- [ ] **Step 5: Update `render()` to control the undo/redo button disabled states**

In the `render()` function, after the line:

```ts
  (document.getElementById('apply') as HTMLButtonElement).disabled = !state.preview?.canApply;
```

Add:

```ts
  (document.getElementById('undo') as HTMLButtonElement).disabled = !state.undoStack.length;
  (document.getElementById('redo') as HTMLButtonElement).disabled = !state.redoStack.length;
```

- [ ] **Step 6: Clear stacks in `init()`**

In the `init()` function, after `state.preview = null;`, add:

```ts
  state.undoStack = [];
  state.redoStack = [];
```

- [ ] **Step 7: Add `pushUndo()` before translation edit mutation**

In `renderTranslations`, find:

```ts
    ta.oninput = () => { state.dirtyTranslations[ta.dataset.translation!] = ta.value; state.preview = null; };
```

Change to:

```ts
    ta.oninput = () => { pushUndo(); state.dirtyTranslations[ta.dataset.translation!] = ta.value; state.preview = null; };
```

- [ ] **Step 8: Add `pushUndo()` before the add-event mutation**

In `renderEvents`, find:

```ts
  el.querySelector<HTMLButtonElement>('#add-event')!.onclick = () => {
    const val = (el.querySelector<HTMLInputElement>('#new-event')!).value.trim();
    if (val) { state.newEvents.add(val); state.preview = null; state.message = `Event stub queued: ${val}`; render(); }
  };
```

Change to:

```ts
  el.querySelector<HTMLButtonElement>('#add-event')!.onclick = () => {
    const val = (el.querySelector<HTMLInputElement>('#new-event')!).value.trim();
    if (val) { pushUndo(); state.newEvents.add(val); state.preview = null; state.message = `Event stub queued: ${val}`; render(); }
  };
```

- [ ] **Step 9: Add `pushUndo()` before the add-inquiry mutation**

In `renderInquiries`, find the `#add-inquiry` onclick handler. It has two early-return guards. Add `pushUndo()` after the guards, before the first mutation:

```ts
  el.querySelector<HTMLButtonElement>('#add-inquiry')!.onclick = () => {
    const npcId = (el.querySelector<HTMLSelectElement>('#new-inquiry-npc')?.value) || defaultNpc;
    const rv = el.querySelector<HTMLSelectElement>('#new-inquiry-registry')?.value;
    const registry = ws.registries.find(r => `${r.kind}:${r.id}` === rv);
    if (!registry) { state.message = 'No item selected.'; render(); return; }
    const path = `src/main/resources/data/hemomancy/dialogue_inquiry/${npcId}/hemomancy/${registry.id}.json`;
    if (ws.inquiries.some(e => e.path === path) || state.dirtyInquiries.has(path)) {
      state.message = `Inquiry already exists for ${npcId} / ${registry.id}.`; render(); return;
    }
    pushUndo();
    const lineKey = `hemomancy.${npcId}.item_inquiry.${registry.id}.line1`;
```

- [ ] **Step 10: Add `pushUndo()` before the remove-inquiry mutation**

Find:

```ts
  el.querySelectorAll<HTMLButtonElement>('[data-remove-inquiry]').forEach(btn => btn.onclick = () => {
    const path = btn.dataset.removeInquiry!;
    const entry = state.dirtyInquiries.get(path);
    ws.inquiries = ws.inquiries.filter(e => e.path !== path);
```

Change to:

```ts
  el.querySelectorAll<HTMLButtonElement>('[data-remove-inquiry]').forEach(btn => btn.onclick = () => {
    pushUndo();
    const path = btn.dataset.removeInquiry!;
    const entry = state.dirtyInquiries.get(path);
    ws.inquiries = ws.inquiries.filter(e => e.path !== path);
```

- [ ] **Step 11: Add `pushUndo()` before the inquiry key edit mutation**

Find:

```ts
  el.querySelectorAll<HTMLInputElement>('[data-inquiry-key]').forEach(input => input.oninput = () => {
    const original = ws.inquiries.find(e => e.path === input.dataset.inquiryKey)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    lines[Number(input.dataset.lineIndex)] = input.value.trim();
```

Change to:

```ts
  el.querySelectorAll<HTMLInputElement>('[data-inquiry-key]').forEach(input => input.oninput = () => {
    pushUndo();
    const original = ws.inquiries.find(e => e.path === input.dataset.inquiryKey)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    lines[Number(input.dataset.lineIndex)] = input.value.trim();
```

- [ ] **Step 12: Add `pushUndo()` before the inquiry translation edit mutation**

Find:

```ts
  el.querySelectorAll<HTMLTextAreaElement>('[data-inquiry-translation]').forEach(ta => ta.oninput = () => {
    state.dirtyTranslations[ta.dataset.inquiryTranslation!] = ta.value; state.preview = null;
  });
```

Change to:

```ts
  el.querySelectorAll<HTMLTextAreaElement>('[data-inquiry-translation]').forEach(ta => ta.oninput = () => {
    pushUndo(); state.dirtyTranslations[ta.dataset.inquiryTranslation!] = ta.value; state.preview = null;
  });
```

- [ ] **Step 13: Add `pushUndo()` before the add-inquiry-line mutation**

Find:

```ts
  el.querySelectorAll<HTMLButtonElement>('[data-add-inquiry-line]').forEach(btn => btn.onclick = () => {
    const original = ws.inquiries.find(e => e.path === btn.dataset.addInquiryLine)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
```

Change to:

```ts
  el.querySelectorAll<HTMLButtonElement>('[data-add-inquiry-line]').forEach(btn => btn.onclick = () => {
    pushUndo();
    const original = ws.inquiries.find(e => e.path === btn.dataset.addInquiryLine)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
```

- [ ] **Step 14: Run `tsc --noEmit`**

```
cd tools/dialogue_editor
npx tsc --noEmit
```

Expected: no output.

- [ ] **Step 15: Run tests**

```
npm test
```

Expected: 19 tests passing.

- [ ] **Step 16: Commit**

```bash
git add -f tools/dialogue_editor/src/frontend/main.ts
git commit -m "feat(dialogue-editor): wire pushUndo in main.ts, add keyboard shortcuts and undo/redo buttons"
```
