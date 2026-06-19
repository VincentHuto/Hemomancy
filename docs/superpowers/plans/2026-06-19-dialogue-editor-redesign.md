# Dialogue Editor Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign the dialogue editor into focused modules with a multi-tree canvas, richer node cards with SVG bezier edges, a wide contextual inspector, and sidecar metadata for animation/sound triggers.

**Architecture:** Split the 870-line `main.ts` monolith into six frontend modules (`state`, `api`, `graph`, `inspector`, `sidebar`, `main`). The graph renders all NPC trees stacked vertically on one canvas with SVG bezier connections between ports. The inspector is contextual — its content depends on which card section row is selected.

**Tech Stack:** TypeScript, Vite, vanilla DOM, Node.js HTTP server, Vitest

## Global Constraints

- No external frontend frameworks or libraries added
- `dialogueParser.ts`, `workspace.ts` (except metadata additions), `validation.ts`, `diff.ts`, `javaText.ts` — untouched
- Animation/sound triggers never enter `renderOption()` or `parseOptions()`
- Run tests from `tools/dialogue_editor/` with `npm test`
- Run dev with `npm run dev`, verify UI at `http://127.0.0.1:5174/workspace.html`

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| Modify | `src/shared/types.ts` | Add `animationTrigger?`, `soundTrigger?`, `NpcMetadata`, updated `SelectedRow` |
| Modify | `src/server/workspace.ts` | Add `loadMetadata`, `saveMetadata` |
| Modify | `src/server/httpServer.ts` | Add `GET/POST /api/metadata/:speaker` routes |
| Modify | `src/server/workspace.test.ts` | Add metadata round-trip tests |
| **Create** | `src/frontend/state.ts` | State object + typed getters |
| **Create** | `src/frontend/api.ts` | All `fetch` calls |
| **Create** | `src/frontend/sidebar.ts` | File group + node list navigation |
| **Create** | `src/frontend/graph.ts` | Multi-tree canvas, rich node cards, SVG edges, drag |
| **Create** | `src/frontend/inspector.ts` | Contextual node editing panel |
| Rewrite | `src/frontend/main.ts` | Thin orchestrator wiring all modules |
| Modify | `src/frontend/styles.css` | Wider inspector, new card layout, SVG overlay, tree banners |

---

### Task 1: Extend shared types

**Files:**
- Modify: `src/shared/types.ts`

**Interfaces:**
- Produces: `NpcMetadata`, `NpcMetadataOption`, updated `DialogueOptionModel`, `SelectedRow` — all tasks consume these

- [ ] **Step 1: Add types to `src/shared/types.ts`**

Open the file and apply these changes:

Add after `DialogueOptionModel`:
```ts
export interface NpcMetadataOption {
  animationTrigger?: string;
  soundTrigger?: string;
}

export interface NpcMetadata {
  version: number;
  options: Record<string, NpcMetadataOption>;
}

export type SelectedRow =
  | { treeMethod: string; nodeId: string; section: 'lines' | 'triggers' }
  | { treeMethod: string; nodeId: string; section: 'option'; optionIndex: number }
  | null;
```

Add `animationTrigger` and `soundTrigger` to `DialogueOptionModel`:
```ts
export interface DialogueOptionModel {
  text: string;
  next: string | null;
  event: string | null;
  eventExpression?: boolean;
  textExpression?: boolean;
  nextExpression?: boolean;
  animationTrigger?: string;
  soundTrigger?: string;
}
```

- [ ] **Step 2: Type-check**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/shared/types.ts
git commit -m "feat(dialogue-editor): extend types for metadata and SelectedRow"
```

---

### Task 2: Server metadata functions, routes, and tests

**Files:**
- Modify: `src/server/workspace.ts`
- Modify: `src/server/httpServer.ts`
- Modify: `src/server/workspace.test.ts`

**Interfaces:**
- Consumes: `NpcMetadata` from Task 1
- Produces: `loadMetadata(dir, speaker)`, `saveMetadata(dir, speaker, data)`, `GET/POST /api/metadata/:speaker`

- [ ] **Step 1: Write failing tests in `src/server/workspace.test.ts`**

Add at the end of the file:

```ts
import { loadMetadata, saveMetadata } from './workspace';
import type { NpcMetadata } from '../shared/types';

describe('metadata round-trip', () => {
  test('loadMetadata returns empty metadata when file does not exist', () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-meta-'));
    expect(loadMetadata(root, 'TestNpc')).toEqual({ version: 1, options: {} });
  });

  test('saveMetadata writes and loadMetadata reads back correctly', () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-meta-'));
    const metadata: NpcMetadata = {
      version: 1,
      options: {
        'testTree::root::0': { animationTrigger: 'wave', soundTrigger: 'hemomancy:npc/wave' }
      }
    };
    saveMetadata(root, 'TestNpc', metadata);
    const loaded = loadMetadata(root, 'TestNpc');
    expect(loaded.options['testTree::root::0']?.animationTrigger).toBe('wave');
    expect(loaded.options['testTree::root::0']?.soundTrigger).toBe('hemomancy:npc/wave');
  });
});
```

- [ ] **Step 2: Run tests — expect failure**

```bash
cd tools/dialogue_editor && npm test
```

Expected: FAIL — `loadMetadata is not a function`

- [ ] **Step 3: Add `loadMetadata` and `saveMetadata` to `src/server/workspace.ts`**

Add these imports at the top (alongside existing ones):
```ts
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve, sep } from 'node:path';
import type { ..., NpcMetadata } from '../shared/types';
```

Add these two exported functions (before the existing private helpers):

```ts
export function loadMetadata(metadataDir: string, speaker: string): NpcMetadata {
  const path = join(metadataDir, `${speaker}Metadata.json`);
  if (!existsSync(path)) return { version: 1, options: {} };
  try {
    return JSON.parse(readFileSync(path, 'utf8')) as NpcMetadata;
  } catch {
    return { version: 1, options: {} };
  }
}

export function saveMetadata(metadataDir: string, speaker: string, data: NpcMetadata): void {
  const path = join(metadataDir, `${speaker}Metadata.json`);
  writeFileSync(path, JSON.stringify(data, null, 2) + '\n', 'utf8');
}
```

- [ ] **Step 4: Run tests — expect pass**

```bash
cd tools/dialogue_editor && npm test
```

Expected: all tests PASS.

- [ ] **Step 5: Add metadata routes to `src/server/httpServer.ts`**

Add these imports at the top:
```ts
import { applyPreview, defaultRepoRoot, loadDialogueFile, loadMetadata, loadWorkspace, previewWorkspaceChanges, safeResolve, saveMetadata } from './workspace';
import type { NpcMetadata, PreviewRequest } from '../shared/types';
```

Add two new route blocks inside the `createServer` callback, before the `serveStatic` fallback:

```ts
if (req.method === 'GET' && url.pathname.startsWith('/api/metadata/')) {
  const speaker = decodeURIComponent(url.pathname.slice('/api/metadata/'.length));
  return send(res, 200, loadMetadata(process.cwd(), speaker));
}

if (req.method === 'POST' && url.pathname.startsWith('/api/metadata/')) {
  const speaker = decodeURIComponent(url.pathname.slice('/api/metadata/'.length));
  const body = await readJson(req) as NpcMetadata;
  saveMetadata(process.cwd(), speaker, body);
  return send(res, 200, { ok: true });
}
```

- [ ] **Step 6: Type-check and commit**

```bash
cd tools/dialogue_editor && npx tsc --noEmit && npm test
```

Expected: all pass.

```bash
git add src/server/workspace.ts src/server/httpServer.ts src/server/workspace.test.ts
git commit -m "feat(dialogue-editor): add metadata load/save API for animation and sound triggers"
```

---

### Task 3: State module

**Files:**
- Create: `src/frontend/state.ts`

**Interfaces:**
- Consumes: `DialogueFile`, `DialogueNodeModel`, `DialogueTreeModel`, `DialogueWorkspace`, `NpcMetadata`, `PreviewResult`, `SelectedRow` from Tasks 1
- Produces: `state`, `currentFile()`, `currentNode()`, `currentNodeTree()`, `graphKey()`, `speakerSlug()`, `Palette` type

- [ ] **Step 1: Create `src/frontend/state.ts`**

```ts
import type {
  DialogueFile,
  DialogueInquiryEntry,
  DialogueNodeModel,
  DialogueTreeModel,
  DialogueWorkspace,
  NpcMetadata,
  PreviewResult,
  SelectedRow
} from '../shared/types';

export type Palette = 'harbinger' | 'fungal' | 'unstained';
export type Tab = 'Graph' | 'Translations' | 'Events' | 'Item Inquiries' | 'Validation' | 'Diff';

export const state: {
  workspace: DialogueWorkspace | null;
  fileIndex: number;
  selectedRow: SelectedRow;
  tab: Tab;
  dirtyTranslations: Record<string, string>;
  dirtyInquiries: Map<string, DialogueInquiryEntry>;
  createdInquiryPaths: Set<string>;
  collapsedFileGroups: Set<Palette>;
  graphPositions: Record<string, Record<string, { x: number; y: number }>>;
  newEvents: Set<string>;
  preview: PreviewResult | null;
  message: string;
  metadata: Record<string, NpcMetadata>;
} = {
  workspace: null,
  fileIndex: 0,
  selectedRow: null,
  tab: 'Graph',
  dirtyTranslations: {},
  dirtyInquiries: new Map(),
  createdInquiryPaths: new Set(),
  collapsedFileGroups: new Set(),
  graphPositions: {},
  newEvents: new Set(),
  preview: null,
  message: 'Loading workspace...',
  metadata: {}
};

export function currentFile(): DialogueFile | null {
  return state.workspace?.dialogueFiles[state.fileIndex] ?? null;
}

export function currentNode(): DialogueNodeModel | null {
  const row = state.selectedRow;
  if (!row) return null;
  const file = currentFile();
  if (!file) return null;
  const tree = file.trees.find(t => t.method === row.treeMethod);
  return tree?.nodes.find(n => n.id === row.nodeId) ?? null;
}

export function currentNodeTree(): DialogueTreeModel | null {
  const row = state.selectedRow;
  if (!row) return null;
  return currentFile()?.trees.find(t => t.method === row.treeMethod) ?? null;
}

export function graphKey(file: DialogueFile, tree: DialogueTreeModel): string {
  return `${file.path}::${tree.method}::${tree.variant ?? 'main'}`;
}

export function speakerSlug(file: DialogueFile): string {
  const base = file.path.split(/[\\/]/).at(-1) ?? '';
  return base.replace('DialogueTrees.java', '').replace('DialogueTrees', '');
}

export function paletteFor(file: DialogueFile | null | undefined, tree?: DialogueTreeModel | null): Palette {
  const raw = `${file?.path ?? ''} ${file?.speaker ?? ''} ${tree?.method ?? ''} ${tree?.theme ?? ''}`.toLowerCase();
  if (raw.includes('fungal')) return 'fungal';
  if (raw.includes('unstained') || raw.includes('guardian') || raw.includes('ourlady') || raw.includes('ladywhisper') || raw.includes('zealot')) return 'unstained';
  return 'harbinger';
}

export function translation(key: string): string {
  return state.dirtyTranslations[key] ?? state.workspace?.translations[key] ?? '';
}

export function metadataKey(treeMethod: string, nodeId: string, optionIndex: number): string {
  return `${treeMethod}::${nodeId}::${optionIndex}`;
}

export function optionMeta(slug: string, treeMethod: string, nodeId: string, optionIndex: number) {
  return state.metadata[slug]?.options[metadataKey(treeMethod, nodeId, optionIndex)] ?? {};
}
```

- [ ] **Step 2: Type-check**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/frontend/state.ts
git commit -m "feat(dialogue-editor): add state module with typed getters"
```

---

### Task 4: API module

**Files:**
- Create: `src/frontend/api.ts`

**Interfaces:**
- Consumes: `DialogueFile`, `DialogueInquiryEntry`, `NpcMetadata`, `PreviewResult` from types; `state`, `speakerSlug`, `currentFile` from state
- Produces: `loadWorkspace()`, `preview()`, `applyPreview()`, `loadMetadata()`, `saveMetadata()`

- [ ] **Step 1: Create `src/frontend/api.ts`**

```ts
import type { DialogueFile, DialogueInquiryEntry, NpcMetadata, PreviewResult } from '../shared/types';
import { currentFile, speakerSlug, state } from './state';

export async function loadWorkspace(): Promise<void> {
  const res = await fetch('/api/workspace');
  state.workspace = await res.json();
}

export async function fetchPreview(
  file: DialogueFile,
  translations: Record<string, string>,
  inquiries: DialogueInquiryEntry[],
  newEvents: string[]
): Promise<PreviewResult> {
  const res = await fetch('/api/preview', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ dialogueFiles: [file], translations, inquiries, newEvents })
  });
  return res.json();
}

export async function applyPreview(previewId: string): Promise<void> {
  await fetch('/api/apply', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ id: previewId })
  });
}

export async function fetchMetadata(slug: string): Promise<NpcMetadata> {
  const res = await fetch(`/api/metadata/${encodeURIComponent(slug)}`);
  return res.json();
}

export async function pushMetadata(slug: string, data: NpcMetadata): Promise<void> {
  await fetch(`/api/metadata/${encodeURIComponent(slug)}`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(data)
  });
}
```

- [ ] **Step 2: Type-check and commit**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
git add src/frontend/api.ts
git commit -m "feat(dialogue-editor): add api module isolating all fetch calls"
```

---

### Task 5: Sidebar module

**Files:**
- Create: `src/frontend/sidebar.ts`

**Interfaces:**
- Consumes: `state`, `currentFile`, `paletteFor`, `Palette`, `Tab` from state
- Produces: `renderSidebar(el, onRender)`

- [ ] **Step 1: Create `src/frontend/sidebar.ts`**

```ts
import type { DialogueFile } from '../shared/types';
import { currentFile, paletteFor, state, translation } from './state';
import type { Palette } from './state';

function basename(path: string): string {
  return path.split(/[\\/]/).at(-1) ?? path;
}

function escapeHtml(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(value: string): string {
  return escapeHtml(value).replace(/"/g, '&quot;');
}

export function renderSidebar(el: HTMLElement, onRender: () => void): void {
  const ws = state.workspace;
  if (!ws) {
    el.innerHTML = '<div class="empty">Loading...</div>';
    return;
  }

  const file = currentFile();
  const groupedFiles: Array<{ palette: Palette; label: string; files: Array<{ file: DialogueFile; index: number }> }> = [
    { palette: 'harbinger', label: 'Harbinger', files: [] },
    { palette: 'fungal', label: 'Fungal', files: [] },
    { palette: 'unstained', label: 'Unstained', files: [] }
  ];
  ws.dialogueFiles.forEach((dialogueFile, index) => {
    groupedFiles.find(g => g.palette === paletteFor(dialogueFile))!.files.push({ file: dialogueFile, index });
  });

  const allNodes = file?.trees.flatMap(tree =>
    tree.nodes.map(node => ({ node, treeMethod: tree.method }))
  ) ?? [];

  el.innerHTML = `
    <div class="section-title">Dialogue Files</div>
    ${groupedFiles.filter(g => g.files.length).map(group => {
      const collapsed = state.collapsedFileGroups.has(group.palette);
      return `
        <div class="folder-row ${collapsed ? 'collapsed' : ''}" data-file-group="${group.palette}">
          <span><span class="folder-caret">${collapsed ? '>' : 'v'}</span>${group.label}</span>
          <span class="count">${group.files.length}</span>
        </div>
        ${collapsed ? '' : group.files.map(({ file: f, index: i }) => `
          <div class="file-row file-row-nested ${i === state.fileIndex ? 'active' : ''}" data-file="${i}">
            <span>${escapeHtml(basename(f.path))}</span><span class="count">${f.trees.length}</span>
          </div>`).join('')}`;
    }).join('')}
    <div class="section-title">Nodes</div>
    ${allNodes.map(({ node, treeMethod }) => {
      const isActive = state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod;
      return `<div class="node-row ${isActive ? 'active' : ''}" data-node="${escapeAttr(node.id)}" data-tree-method="${escapeAttr(treeMethod)}">
        <span>${escapeHtml(node.id)}</span>
        <span class="count">${node.options.length}</span>
      </div>`;
    }).join('')}
  `;

  el.querySelectorAll<HTMLElement>('[data-file]').forEach(row => row.onclick = () => {
    state.fileIndex = Number(row.dataset.file);
    state.selectedRow = null;
    onRender();
  });
  el.querySelectorAll<HTMLElement>('[data-file-group]').forEach(row => row.onclick = () => {
    const group = row.dataset.fileGroup as Palette;
    if (state.collapsedFileGroups.has(group)) state.collapsedFileGroups.delete(group);
    else state.collapsedFileGroups.add(group);
    onRender();
  });
  el.querySelectorAll<HTMLElement>('[data-node]').forEach(row => row.onclick = () => {
    const nodeId = row.dataset.node!;
    const treeMethod = row.dataset.treeMethod!;
    state.selectedRow = { treeMethod, nodeId, section: 'lines' };
    onRender();
  });
}
```

- [ ] **Step 2: Type-check and commit**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
git add src/frontend/sidebar.ts
git commit -m "feat(dialogue-editor): add sidebar module without tree-list section"
```

---

### Task 6: Graph module — multi-tree canvas, rich cards, SVG bezier edges

**Files:**
- Create: `src/frontend/graph.ts`

**Interfaces:**
- Consumes: `state`, `currentFile`, `graphKey`, `translation`, `optionMeta`, `speakerSlug`, `SelectedRow` from state; `DialogueFile`, `DialogueTreeModel`, `DialogueNodeModel` from types
- Produces: `renderGraph(el, onRender)`, drag state management

Layout constants (internal to graph.ts):
```
CARD_WIDTH = 260
HEADER_H = 38
SECTION_LABEL_H = 32
OPTION_ROW_H = 34
BOTTOM_ROW_H = 36
COL_WIDTH = 330
NODE_V_STEP = 260
NODE_START_X = 30
TREE_BANNER_H = 36
TREE_GAP = 80
```

- [ ] **Step 1: Create `src/frontend/graph.ts`**

```ts
import type { DialogueFile, DialogueNodeModel, DialogueOptionModel, DialogueTreeModel } from '../shared/types';
import { currentFile, graphKey, optionMeta, speakerSlug, state, translation } from './state';

const CARD_WIDTH = 260;
const HEADER_H = 38;
const SECTION_LABEL_H = 32;
const OPTION_ROW_H = 34;
const BOTTOM_ROW_H = 36;
const COL_WIDTH = 330;
const NODE_V_STEP = 260;
const NODE_START_X = 30;
const TREE_BANNER_H = 36;
const TREE_GAP = 80;

type NodePos = { x: number; y: number };

let dragState: {
  treeMethod: string;
  nodeId: string;
  startX: number;
  startY: number;
  originX: number;
  originY: number;
  moved: boolean;
} | null = null;

let suppressNextCardClick = false;

function escapeHtml(v: string): string {
  return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(v: string): string {
  return escapeHtml(v).replace(/"/g, '&quot;');
}

function truncate(v: string, len: number): string {
  return v.length > len ? v.slice(0, len - 1) + '…' : v;
}

function cardHeight(node: DialogueNodeModel): number {
  return HEADER_H + SECTION_LABEL_H + SECTION_LABEL_H + node.options.length * OPTION_ROW_H + SECTION_LABEL_H + BOTTOM_ROW_H;
}

function treeHeight(tree: DialogueTreeModel): number {
  const depth = new Map<string, number>();
  const rowsByDepth = new Map<number, number>();
  const queue = [{ id: tree.startNode ?? tree.nodes[0]?.id ?? '', d: 0 }];
  while (queue.length) {
    const { id, d } = queue.shift()!;
    if (!id || depth.has(id)) continue;
    depth.set(id, d);
    rowsByDepth.set(d, (rowsByDepth.get(d) ?? 0) + 1);
    tree.nodes.find(n => n.id === id)?.options.forEach(o => {
      if (o.next) queue.push({ id: o.next, d: d + 1 });
    });
  }
  tree.nodes.forEach(n => { if (!depth.has(n.id)) { depth.set(n.id, 0); rowsByDepth.set(0, (rowsByDepth.get(0) ?? 0) + 1); } });
  const maxRows = Math.max(1, ...rowsByDepth.values());
  return maxRows * NODE_V_STEP;
}

function bfsPositions(tree: DialogueTreeModel, yOrigin: number): Map<string, NodePos> {
  const depth = new Map<string, number>();
  const queue = [{ id: tree.startNode ?? tree.nodes[0]?.id ?? '', d: 0 }];
  while (queue.length) {
    const { id, d } = queue.shift()!;
    if (!id || depth.has(id)) continue;
    depth.set(id, d);
    tree.nodes.find(n => n.id === id)?.options.forEach(o => {
      if (o.next) queue.push({ id: o.next, d: d + 1 });
    });
  }
  tree.nodes.forEach(n => { if (!depth.has(n.id)) depth.set(n.id, 0); });
  const rowCount = new Map<number, number>();
  const positions = new Map<string, NodePos>();
  depth.forEach((d, id) => {
    const row = rowCount.get(d) ?? 0;
    rowCount.set(d, row + 1);
    positions.set(id, { x: NODE_START_X + d * COL_WIDTH, y: yOrigin + row * NODE_V_STEP });
  });
  return positions;
}

function computePositions(file: DialogueFile, tree: DialogueTreeModel, yOrigin: number): Map<string, NodePos> {
  const auto = bfsPositions(tree, yOrigin);
  const manual = state.graphPositions[graphKey(file, tree)] ?? {};
  for (const [id, pos] of Object.entries(manual)) auto.set(id, pos);
  return auto;
}

function inputPortPos(pos: NodePos): NodePos {
  return { x: pos.x, y: pos.y + HEADER_H / 2 };
}

function outputPortPos(pos: NodePos, optionIndex: number): NodePos {
  return {
    x: pos.x + CARD_WIDTH,
    y: pos.y + HEADER_H + SECTION_LABEL_H + SECTION_LABEL_H + (optionIndex + 0.5) * OPTION_ROW_H
  };
}

function bezierPath(from: NodePos, to: NodePos): string {
  const dx = Math.max(60, Math.abs(to.x - from.x) * 0.45);
  return `M ${from.x} ${from.y} C ${from.x + dx} ${from.y} ${to.x - dx} ${to.y} ${to.x} ${to.y}`;
}

function renderEdges(
  tree: DialogueTreeModel,
  positions: Map<string, NodePos>
): string {
  return tree.nodes.flatMap(node =>
    node.options.map((option, i) => {
      if (!option.next || !positions.has(option.next)) return '';
      const from = outputPortPos(positions.get(node.id)!, i);
      const to = inputPortPos(positions.get(option.next)!);
      return `<path d="${bezierPath(from, to)}" class="edge-path"/>`;
    })
  ).join('');
}

function isActive(treeMethod: string, nodeId: string): boolean {
  return state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.nodeId === nodeId;
}

function isOptionActive(treeMethod: string, nodeId: string, optionIndex: number): boolean {
  const row = state.selectedRow;
  return row?.treeMethod === treeMethod && row?.nodeId === nodeId && row.section === 'option' && (row as { optionIndex: number }).optionIndex === optionIndex;
}

function optionLabel(option: DialogueOptionModel): string {
  return translation(option.text) || option.text.split('.').at(-1) || option.text;
}

function renderNodeCard(node: DialogueNodeModel, pos: NodePos, treeMethod: string, slug: string): string {
  const active = isActive(treeMethod, node.id);
  return `<div class="card-node ${active ? 'active' : ''}" data-node-card="${escapeAttr(node.id)}" data-tree-method="${escapeAttr(treeMethod)}" style="left:${pos.x}px;top:${pos.y}px">
    <div class="card-header" data-drag-node="${escapeAttr(node.id)}" data-drag-tree="${escapeAttr(treeMethod)}">
      <div class="port-in"></div>
      <span class="card-title">${escapeHtml(node.id)}</span>
    </div>
    <div class="card-section-row ${state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.section === 'lines' ? 'active' : ''}" data-select-section="lines" data-section-node="${escapeAttr(node.id)}" data-section-tree="${escapeAttr(treeMethod)}">
      <span>Lines</span>
      <span class="count">${node.lines.length}</span>
    </div>
    <div class="card-options-section">
      <div class="card-section-label">Options</div>
      ${node.options.map((option, i) => {
        const label = truncate(optionLabel(option), 28);
        const isOpt = isOptionActive(treeMethod, node.id, i);
        return `<div class="option-row ${isOpt ? 'active' : ''}" data-select-option="${i}" data-opt-node="${escapeAttr(node.id)}" data-opt-tree="${escapeAttr(treeMethod)}">
          <span class="option-text">${escapeHtml(label)}</span>
          <div class="port-out"></div>
        </div>`;
      }).join('')}
    </div>
    <div class="card-section-row ${state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.section === 'triggers' ? 'active' : ''}" data-select-section="triggers" data-section-node="${escapeAttr(node.id)}" data-section-tree="${escapeAttr(treeMethod)}">
      <span>Triggers</span>
    </div>
    <div class="card-add-row" data-add-node-tree="${escapeAttr(treeMethod)}">+</div>
  </div>`;
}

function renderTreeBanner(tree: DialogueTreeModel, y: number): string {
  return `<div class="tree-banner" style="top:${y}px">
    <span class="tree-banner-method">${escapeHtml(tree.method)}</span>
    <span class="count">${tree.visibility}</span>
    <span class="count">${tree.nodes.length} nodes</span>
    ${tree.dispatchOnly ? '<span class="count">dispatch</span>' : ''}
  </div>`;
}

export function renderGraph(el: HTMLElement, onRender: () => void): void {
  const file = currentFile();
  if (!file) {
    el.innerHTML = '<div class="empty">No file selected.</div>';
    return;
  }

  const slug = speakerSlug(file);
  let currentY = 0;
  const treeLayouts: Array<{ tree: DialogueTreeModel; positions: Map<string, NodePos>; bannerY: number }> = [];

  for (const tree of file.trees) {
    const bannerY = currentY;
    const originY = currentY + TREE_BANNER_H;
    const positions = computePositions(file, tree, originY);
    treeLayouts.push({ tree, positions, bannerY });
    currentY = originY + treeHeight(tree) + TREE_GAP;
  }

  const totalHeight = currentY;
  const totalWidth = Math.max(920, NODE_START_X + 10 * COL_WIDTH);

  const svgEdges = treeLayouts.map(({ tree, positions }) =>
    tree.dispatchOnly ? '' : renderEdges(tree, positions)
  ).join('');

  const cards = treeLayouts.map(({ tree, positions, bannerY }) =>
    renderTreeBanner(tree, bannerY) +
    (tree.dispatchOnly ? '' : [...positions.entries()].map(([nodeId]) => {
      const node = tree.nodes.find(n => n.id === nodeId);
      return node ? renderNodeCard(node, positions.get(nodeId)!, tree.method, slug) : '';
    }).join(''))
  ).join('');

  el.innerHTML = `<div class="graph" style="min-height:${totalHeight}px;min-width:${totalWidth}px;position:relative">
    <svg class="edges" width="${totalWidth}" height="${totalHeight}">${svgEdges}</svg>
    ${cards}
  </div>`;

  bindGraphEvents(el, file, treeLayouts, onRender);
}

function bindGraphEvents(
  el: HTMLElement,
  file: DialogueFile,
  treeLayouts: Array<{ tree: DialogueTreeModel; positions: Map<string, NodePos>; bannerY: number }>,
  onRender: () => void
): void {
  el.querySelectorAll<HTMLElement>('[data-select-section]').forEach(row => {
    row.onclick = e => {
      e.stopPropagation();
      const section = row.dataset.selectSection as 'lines' | 'triggers';
      const nodeId = row.dataset.sectionNode!;
      const treeMethod = row.dataset.sectionTree!;
      state.selectedRow = { treeMethod, nodeId, section };
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-select-option]').forEach(row => {
    row.onclick = e => {
      e.stopPropagation();
      if (suppressNextCardClick) { suppressNextCardClick = false; return; }
      const optionIndex = Number(row.dataset.selectOption);
      const nodeId = row.dataset.optNode!;
      const treeMethod = row.dataset.optTree!;
      state.selectedRow = { treeMethod, nodeId, section: 'option', optionIndex };
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-add-node-tree]').forEach(btn => {
    btn.onclick = e => {
      e.stopPropagation();
      const treeMethod = btn.dataset.addNodeTree!;
      const tree = file.trees.find(t => t.method === treeMethod);
      if (!tree) return;
      const newId = `node_${tree.nodes.length + 1}`;
      tree.nodes.push({ id: newId, lines: [], options: [] });
      state.selectedRow = { treeMethod, nodeId: newId, section: 'lines' };
      state.preview = null;
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-drag-node]').forEach(header => {
    header.onmousedown = e => {
      const nodeId = header.dataset.dragNode!;
      const treeMethod = header.dataset.dragTree!;
      const layout = treeLayouts.find(l => l.tree.method === treeMethod);
      const pos = layout?.positions.get(nodeId);
      if (!pos) return;
      e.preventDefault();
      dragState = { treeMethod, nodeId, startX: e.clientX, startY: e.clientY, originX: pos.x, originY: pos.y, moved: false };
    };
  });
}

document.addEventListener('mousemove', e => {
  if (!dragState) return;
  const file = currentFile();
  if (!file) return;
  const dx = e.clientX - dragState.startX;
  const dy = e.clientY - dragState.startY;
  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) dragState.moved = true;
  const tree = file.trees.find(t => t.method === dragState!.treeMethod);
  if (!tree) return;
  const key = graphKey(file, tree);
  state.graphPositions[key] = {
    ...(state.graphPositions[key] ?? {}),
    [dragState.nodeId]: { x: Math.max(0, dragState.originX + dx), y: Math.max(0, dragState.originY + dy) }
  };
  const graphEl = document.querySelector<HTMLElement>('#content');
  if (graphEl) {
    const el = document.querySelector<HTMLElement>('.graph');
    if (el) {
      document.dispatchEvent(new CustomEvent('graph-drag-render'));
    }
  }
});

document.addEventListener('mouseup', () => {
  if (!dragState) return;
  suppressNextCardClick = dragState.moved;
  dragState = null;
});
```

- [ ] **Step 2: Type-check**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add src/frontend/graph.ts
git commit -m "feat(dialogue-editor): add graph module with multi-tree canvas and SVG bezier edges"
```

---

### Task 7: Inspector module

**Files:**
- Create: `src/frontend/inspector.ts`

**Interfaces:**
- Consumes: `state`, `currentNode`, `currentNodeTree`, `translation`, `optionMeta`, `metadataKey`, `speakerSlug`, `currentFile` from state; `pushMetadata` from api
- Produces: `renderInspector(el, onRender)`

- [ ] **Step 1: Create `src/frontend/inspector.ts`**

```ts
import type { DialogueNodeModel, DialogueTreeModel, NpcMetadata } from '../shared/types';
import { pushMetadata } from './api';
import { currentFile, currentNode, currentNodeTree, metadataKey, optionMeta, speakerSlug, state, translation } from './state';

function escapeHtml(v: string): string {
  return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(v: string): string {
  return escapeHtml(v).replace(/"/g, '&quot;');
}

function suggestLineKey(tree: DialogueTreeModel, node: DialogueNodeModel): string {
  return `hemomancy.dialogue.${tree.method}.${node.id}.line${node.lines.length + 1}`;
}

function suggestOptionKey(tree: DialogueTreeModel, node: DialogueNodeModel, index: number): string {
  return `hemomancy.dialogue.${tree.method}.option.${node.id}_${index}`;
}

export function renderInspector(el: HTMLElement, onRender: () => void): void {
  const row = state.selectedRow;
  if (!row) {
    el.innerHTML = '<div class="empty">Select a node section to edit it.</div>';
    return;
  }

  const node = currentNode();
  const tree = currentNodeTree();
  if (!node || !tree) {
    el.innerHTML = '<div class="empty">Node not found.</div>';
    return;
  }

  if (row.section === 'lines') renderLinesPanel(el, node, tree, onRender);
  else if (row.section === 'option') renderOptionPanel(el, node, tree, (row as { optionIndex: number }).optionIndex, onRender);
  else renderTriggersPanel(el);
}

function renderLinesPanel(el: HTMLElement, node: DialogueNodeModel, tree: DialogueTreeModel, onRender: () => void): void {
  el.innerHTML = `
    <div class="insp-block">
      <div class="insp-field">
        <label>NODE ID</label>
        <input id="node-id" class="mono" value="${escapeAttr(node.id)}">
      </div>
    </div>
    <div class="insp-block">
      <div class="insp-section-head">
        <span>LINES</span>
        <button id="add-line">+ Add Line</button>
      </div>
      ${node.lines.map((line, i) => `
        <div class="insp-line-card">
          <div class="insp-line-key-row">
            <input class="mono" data-line="${i}" value="${escapeAttr(line)}">
            <button class="danger icon-button" data-delete-line="${i}">×</button>
          </div>
          <div class="insp-translation">${escapeHtml(translation(line) || '(no translation)')}</div>
        </div>`).join('')}
      ${node.lines.length === 0 ? '<div class="empty" style="padding:10px">No lines. Add one above.</div>' : ''}
    </div>
    <div class="insp-block">
      <div class="insp-section-head">
        <span>OPTIONS</span>
        <button id="add-option">+ Add Option</button>
      </div>
      ${node.options.map((_, i) => `
        <div class="insp-option-stub" data-jump-option="${i}">Option ${i + 1}</div>`).join('')}
    </div>
    <div class="insp-block insp-danger-block">
      <button id="delete-node" class="danger">Delete Node</button>
    </div>`;

  document.getElementById('node-id')!.addEventListener('change', e => {
    const next = (e.target as HTMLInputElement).value.trim();
    if (!next || next === node.id) return;
    const old = node.id;
    node.id = next;
    tree.nodes.forEach(n => n.options.forEach(o => { if (o.next === old) o.next = next; }));
    if (state.selectedRow) state.selectedRow = { ...state.selectedRow, nodeId: next };
    state.preview = null;
    onRender();
  });

  document.getElementById('add-line')!.onclick = () => {
    node.lines.push(suggestLineKey(tree, node));
    state.preview = null;
    onRender();
  };

  document.getElementById('add-option')!.onclick = () => {
    node.options.push({ text: suggestOptionKey(tree, node, node.options.length + 1), next: null, event: null });
    state.preview = null;
    onRender();
  };

  document.getElementById('delete-node')!.onclick = () => {
    tree.nodes = tree.nodes.filter(n => n.id !== node.id);
    tree.nodes.forEach(n => n.options.forEach(o => { if (o.next === node.id) o.next = null; }));
    state.selectedRow = null;
    state.preview = null;
    onRender();
  };

  el.querySelectorAll<HTMLInputElement>('[data-line]').forEach(input => {
    input.oninput = () => { node.lines[Number(input.dataset.line)] = input.value; state.preview = null; };
  });

  el.querySelectorAll<HTMLButtonElement>('[data-delete-line]').forEach(btn => {
    btn.onclick = () => {
      node.lines.splice(Number(btn.dataset.deleteLine), 1);
      state.preview = null;
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-jump-option]').forEach(stub => {
    stub.onclick = () => {
      if (state.selectedRow) {
        state.selectedRow = { ...state.selectedRow, section: 'option', optionIndex: Number(stub.dataset.jumpOption) } as typeof state.selectedRow;
      }
      onRender();
    };
  });
}

function renderOptionPanel(el: HTMLElement, node: DialogueNodeModel, tree: DialogueTreeModel, optionIndex: number, onRender: () => void): void {
  const option = node.options[optionIndex];
  if (!option) { el.innerHTML = '<div class="empty">Option not found.</div>'; return; }

  const file = currentFile();
  const slug = file ? speakerSlug(file) : '';
  const meta = optionMeta(slug, tree.method, node.id, optionIndex);

  const nodeOptions = ['<option value="">-- end conversation --</option>']
    .concat(tree.nodes.filter(n => n.id !== node.id).map(n =>
      `<option value="${escapeAttr(n.id)}"${option.next === n.id ? ' selected' : ''}>${escapeHtml(n.id)}</option>`
    )).join('');

  el.innerHTML = `
    <div class="insp-block">
      <div class="insp-section-head">OPTION ${optionIndex + 1}</div>
      <div class="insp-field">
        <label>TEXT KEY</label>
        <input class="mono" id="opt-text" value="${escapeAttr(option.text)}">
        <div class="insp-translation">${escapeHtml(translation(option.text) || '(no translation)')}</div>
      </div>
      <div class="insp-field">
        <label>GOES TO</label>
        <select id="opt-next">${nodeOptions}</select>
      </div>
      <div class="insp-field">
        <label>EVENT</label>
        <input class="mono" id="opt-event" value="${escapeAttr(option.event ?? '')}">
      </div>
      <div class="insp-divider">Triggers (sidecar metadata)</div>
      <div class="insp-field">
        <label>ANIMATION TRIGGER</label>
        <input class="mono" id="opt-animation" placeholder="e.g. acolyte_kneel" value="${escapeAttr(meta.animationTrigger ?? '')}">
      </div>
      <div class="insp-field">
        <label>SOUND TRIGGER</label>
        <input class="mono" id="opt-sound" placeholder="e.g. hemomancy:npc/acolyte/kneel" value="${escapeAttr(meta.soundTrigger ?? '')}">
      </div>
    </div>
    <div class="insp-block insp-danger-block">
      <button id="delete-option" class="danger">Delete Option</button>
    </div>`;

  document.getElementById('opt-text')!.addEventListener('input', e => {
    option.text = (e.target as HTMLInputElement).value;
    state.preview = null;
  });

  document.getElementById('opt-next')!.addEventListener('change', e => {
    option.next = (e.target as HTMLSelectElement).value || null;
    state.preview = null;
    onRender();
  });

  document.getElementById('opt-event')!.addEventListener('input', e => {
    option.event = (e.target as HTMLInputElement).value.trim() || null;
    state.preview = null;
  });

  async function saveTrigger(field: 'animationTrigger' | 'soundTrigger', value: string): Promise<void> {
    if (!file) return;
    const key = metadataKey(tree.method, node.id, optionIndex);
    const current = state.metadata[slug] ?? { version: 1, options: {} };
    const existing = current.options[key] ?? {};
    const updated: NpcMetadata = {
      ...current,
      options: { ...current.options, [key]: { ...existing, [field]: value || undefined } }
    };
    state.metadata[slug] = updated;
    await pushMetadata(slug, updated);
  }

  document.getElementById('opt-animation')!.addEventListener('change', e => {
    saveTrigger('animationTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('opt-sound')!.addEventListener('change', e => {
    saveTrigger('soundTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('delete-option')!.onclick = () => {
    node.options.splice(optionIndex, 1);
    state.selectedRow = state.selectedRow ? { ...state.selectedRow, section: 'lines' } : null;
    state.preview = null;
    onRender();
  };
}

function renderTriggersPanel(el: HTMLElement): void {
  el.innerHTML = `<div class="insp-block">
    <div class="insp-section-head">NODE TRIGGERS</div>
    <div class="empty" style="padding:14px">Node-level triggers are not yet implemented. Use option-level triggers via the Options section.</div>
  </div>`;
}
```

- [ ] **Step 2: Type-check and commit**

```bash
cd tools/dialogue_editor && npx tsc --noEmit
git add src/frontend/inspector.ts
git commit -m "feat(dialogue-editor): add contextual inspector with animation/sound trigger fields"
```

---

### Task 8: Main.ts rewrite + CSS update

**Files:**
- Rewrite: `src/frontend/main.ts`
- Modify: `src/frontend/styles.css`

**Interfaces:**
- Consumes: all five frontend modules + api + state

- [ ] **Step 1: Rewrite `src/frontend/main.ts`**

```ts
import '../frontend/styles.css';
import type { Diagnostic, DialogueFile, DialogueInquiryEntry, DialogueTreeModel, DialogueWorkspace, PreviewResult } from '../shared/types';
import { applyPreview as applyPreviewApi, fetchMetadata, fetchPreview, loadWorkspace as loadWorkspaceApi } from './api';
import { renderGraph } from './graph';
import { renderInspector } from './inspector';
import { renderSidebar } from './sidebar';
import { currentFile, paletteFor, speakerSlug, state, translation } from './state';
import type { Tab } from './state';

const tabs: Tab[] = ['Graph', 'Translations', 'Events', 'Item Inquiries', 'Validation', 'Diff'];

document.querySelector<HTMLDivElement>('#app')!.innerHTML = `
  <div class="shell">
    <header class="topbar">
      <div class="brand">Hemomancy Dialogue Workspace</div>
      <button id="reload">Reload</button>
      <button id="preview">Preview Diff</button>
      <button id="apply" class="primary" disabled>Apply Preview</button>
      <div class="status" id="status"></div>
    </header>
    <div class="layout">
      <aside class="sidebar" id="sidebar"></aside>
      <main class="main">
        <nav class="tabs" id="tabs"></nav>
        <section class="content" id="content"></section>
      </main>
      <aside class="inspector" id="inspector"></aside>
    </div>
    <div id="translation-popover" class="translation-popover" hidden></div>
  </div>`;

document.getElementById('reload')!.onclick = () => init();
document.getElementById('preview')!.onclick = () => runPreview();
document.getElementById('apply')!.onclick = () => runApply();
document.addEventListener('graph-drag-render', () => {
  renderGraph(document.getElementById('content')!, render);
});

init();

async function init(): Promise<void> {
  state.message = 'Loading workspace...';
  render();
  await loadWorkspaceApi();
  state.fileIndex = 0;
  state.selectedRow = null;
  state.preview = null;
  const file = currentFile();
  if (file) {
    const slug = speakerSlug(file);
    state.metadata[slug] = await fetchMetadata(slug);
  }
  state.message = `${state.workspace?.dialogueFiles.length ?? 0} dialogue files loaded`;
  render();
}

function render(): void {
  applyThemeClass();
  document.getElementById('status')!.textContent = state.message;
  (document.getElementById('apply') as HTMLButtonElement).disabled = !state.preview?.canApply;
  renderSidebar(document.getElementById('sidebar')!, render);
  renderTabs();
  renderContent();
  renderInspector(document.getElementById('inspector')!, render);
}

function applyThemeClass(): void {
  const shell = document.querySelector<HTMLElement>('.shell');
  if (!shell) return;
  shell.classList.remove('theme-harbinger', 'theme-fungal', 'theme-unstained');
  shell.classList.add(`theme-${paletteFor(currentFile())}`);
}

function renderTabs(): void {
  const el = document.getElementById('tabs')!;
  el.innerHTML = tabs.map(t => `<button class="tab ${t === state.tab ? 'active' : ''}" data-tab="${t}">${t}</button>`).join('');
  el.querySelectorAll<HTMLButtonElement>('[data-tab]').forEach(btn => btn.onclick = () => {
    state.tab = btn.dataset.tab as Tab;
    render();
  });
}

function renderContent(): void {
  const el = document.getElementById('content')!;
  if (!state.workspace) { el.innerHTML = '<div class="empty">Loading workspace...</div>'; return; }
  if (state.tab === 'Graph') renderGraph(el, render);
  else if (state.tab === 'Translations') renderTranslations(el);
  else if (state.tab === 'Events') renderEvents(el);
  else if (state.tab === 'Item Inquiries') renderInquiries(el);
  else if (state.tab === 'Validation') renderValidation(el);
  else if (state.tab === 'Diff') renderDiff(el);
}

function escapeHtml(v: string): string { return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
function escapeAttr(v: string): string { return escapeHtml(v).replace(/"/g, '&quot;'); }

function renderTranslations(el: HTMLElement): void {
  const ws = state.workspace!;
  const file = currentFile();
  const keys = new Set<string>();
  file?.trees.forEach(t => t.nodes.forEach(n => {
    n.lines.forEach(k => keys.add(k));
    n.options.forEach(o => keys.add(o.text));
  }));
  const sorted = [...keys].sort();
  el.innerHTML = `<div class="grid-list">
    ${sorted.map(key => `<div class="item-card">
      <div class="field"><label>${escapeHtml(key)}</label>
        <textarea data-translation="${escapeAttr(key)}">${escapeHtml(state.dirtyTranslations[key] ?? ws.translations[key] ?? '')}</textarea>
      </div></div>`).join('')}
    ${sorted.length === 0 ? '<div class="empty">No translation keys for this file.</div>' : ''}
  </div>`;
  el.querySelectorAll<HTMLTextAreaElement>('[data-translation]').forEach(ta => {
    ta.oninput = () => { state.dirtyTranslations[ta.dataset.translation!] = ta.value; state.preview = null; };
  });
}

function renderEvents(el: HTMLElement): void {
  const ws = state.workspace!;
  const usedEvents = new Set(currentFile()?.trees.flatMap(t => t.nodes.flatMap(n => n.options.map(o => o.event).filter(Boolean))) as string[]);
  el.innerHTML = `
    <div class="item-card">
      <div class="field"><label>New Event ID</label><input id="new-event" placeholder="my_new_event"></div>
      <button id="add-event">Generate Handler Stub In Preview</button>
    </div>
    <div class="grid-list">
      ${[...ws.events, ...ws.memos].map(ev => `
        <div class="event-row">
          <span class="mono">${escapeHtml(ev.kind === 'memo' ? `memo_capture:${ev.id}` : ev.id)}</span>
          <span class="count">${ev.kind}${usedEvents.has(ev.id) ? ' / used' : ''}</span>
        </div>`).join('')}
    </div>`;
  el.querySelector<HTMLButtonElement>('#add-event')!.onclick = () => {
    const val = (el.querySelector<HTMLInputElement>('#new-event')!).value.trim();
    if (val) { state.newEvents.add(val); state.preview = null; state.message = `Event stub queued: ${val}`; render(); }
  };
}

function renderInquiries(el: HTMLElement): void {
  const ws = state.workspace!;
  const file = currentFile();
  const raw = `${file?.path ?? ''} ${file?.speaker ?? ''}`.toLowerCase();
  const npcs: string[] = [];
  if (raw.includes('alchemist')) npcs.push('alchemist');
  if (raw.includes('guardian')) npcs.push('guardian');
  if (raw.includes('vicar')) npcs.push('vicar');
  if (raw.includes('zealot')) npcs.push('zealot');

  if (!npcs.length) { el.innerHTML = `<div class="empty">No item inquiry NPC mapped for this file.</div>`; return; }
  const visible = ws.inquiries.filter(e => npcs.includes(e.npcId));
  const defaultNpc = npcs[0];

  el.innerHTML = `
    <div class="item-card inquiry-create">
      <div class="row">
        <div class="field"><label>NPC</label>
          <select id="new-inquiry-npc">${npcs.map(n => `<option value="${escapeAttr(n)}">${escapeHtml(n)}</option>`).join('')}</select>
        </div>
        <div class="field"><label>Item or Block</label>
          <select id="new-inquiry-registry">${ws.registries.map(r => `<option value="${escapeAttr(`${r.kind}:${r.id}`)}">${escapeHtml(`${r.kind} / ${r.id}${r.hasInquiry ? ' / has inquiry' : ''}`)}</option>`).join('')}</select>
        </div>
        <button id="add-inquiry">Add Inquiry</button>
      </div>
    </div>
    <div class="grid-list">
      ${visible.map(entry => {
        const lines = [...(state.dirtyInquiries.get(entry.path)?.lines ?? entry.lines)];
        return `<div class="item-card inquiry-card">
          <div class="row inquiry-card-head">
            <strong>${escapeHtml(entry.npcId)}</strong>
            <span class="count mono">${escapeHtml(entry.itemId)}</span>
            ${state.createdInquiryPaths.has(entry.path) ? `<button class="icon-button danger" data-remove-inquiry="${escapeAttr(entry.path)}">×</button>` : ''}
          </div>
          <div class="hint mono">${escapeHtml(entry.path)}</div>
          ${lines.map((key, li) => `<div class="inquiry-line">
            <div class="field"><label>Line Key ${li + 1}</label>
              <input class="mono" data-inquiry-key="${escapeAttr(entry.path)}" data-line-index="${li}" value="${escapeAttr(key)}">
            </div>
            <div class="field"><label>en_us</label>
              <textarea class="translation-edit" data-inquiry-translation="${escapeAttr(key)}">${escapeHtml(state.dirtyTranslations[key] ?? ws.translations[key] ?? '')}</textarea>
            </div>
          </div>`).join('')}
          <div class="row">
            <button data-add-inquiry-line="${escapeAttr(entry.path)}">Add Line</button>
          </div>
        </div>`;
      }).join('')}
      ${visible.length === 0 ? `<div class="empty">No inquiries yet. Add one above.</div>` : ''}
    </div>`;

  el.querySelector<HTMLButtonElement>('#add-inquiry')!.onclick = () => {
    const npcId = (el.querySelector<HTMLSelectElement>('#new-inquiry-npc')?.value) || defaultNpc;
    const rv = el.querySelector<HTMLSelectElement>('#new-inquiry-registry')?.value;
    const registry = ws.registries.find(r => `${r.kind}:${r.id}` === rv);
    if (!registry) { state.message = 'No item selected.'; render(); return; }
    const path = `src/main/resources/data/hemomancy/dialogue_inquiry/${npcId}/hemomancy/${registry.id}.json`;
    if (ws.inquiries.some(e => e.path === path) || state.dirtyInquiries.has(path)) {
      state.message = `Inquiry already exists for ${npcId} / ${registry.id}.`; render(); return;
    }
    const lineKey = `hemomancy.${npcId}.item_inquiry.${registry.id}.line1`;
    const entry: DialogueInquiryEntry = { path, npcId, itemId: `hemomancy/${registry.id}`, lines: [lineKey], valid: true };
    ws.inquiries = [...ws.inquiries, entry].sort((a, b) => a.path.localeCompare(b.path));
    state.dirtyInquiries.set(path, entry);
    state.createdInquiryPaths.add(path);
    state.dirtyTranslations[lineKey] = state.dirtyTranslations[lineKey] ?? '';
    state.preview = null;
    state.message = `Inquiry queued: ${npcId} / ${registry.id}`;
    render();
  };

  el.querySelectorAll<HTMLButtonElement>('[data-remove-inquiry]').forEach(btn => btn.onclick = () => {
    const path = btn.dataset.removeInquiry!;
    const entry = state.dirtyInquiries.get(path);
    ws.inquiries = ws.inquiries.filter(e => e.path !== path);
    state.dirtyInquiries.delete(path);
    state.createdInquiryPaths.delete(path);
    entry?.lines.forEach(l => delete state.dirtyTranslations[l]);
    state.preview = null; state.message = 'Queued inquiry discarded.'; render();
  });

  el.querySelectorAll<HTMLInputElement>('[data-inquiry-key]').forEach(input => input.oninput = () => {
    const original = ws.inquiries.find(e => e.path === input.dataset.inquiryKey)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    lines[Number(input.dataset.lineIndex)] = input.value.trim();
    state.dirtyInquiries.set(original.path, { ...original, lines: lines.filter(Boolean) });
    state.preview = null;
  });

  el.querySelectorAll<HTMLTextAreaElement>('[data-inquiry-translation]').forEach(ta => ta.oninput = () => {
    state.dirtyTranslations[ta.dataset.inquiryTranslation!] = ta.value; state.preview = null;
  });

  el.querySelectorAll<HTMLButtonElement>('[data-add-inquiry-line]').forEach(btn => btn.onclick = () => {
    const original = ws.inquiries.find(e => e.path === btn.dataset.addInquiryLine)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    const topic = original.itemId.split('/').at(-1) || 'new_item';
    lines.push(`hemomancy.${original.npcId}.item_inquiry.${topic}.line${lines.length + 1}`);
    state.dirtyInquiries.set(original.path, { ...original, lines });
    state.preview = null; render();
  });
}

function renderValidation(el: HTMLElement): void {
  const diags = [...(state.workspace?.diagnostics ?? []), ...(state.preview?.diagnostics ?? [])];
  el.innerHTML = diags.length
    ? diags.map(d => `<div class="diagnostic ${d.severity}">
        <strong>${d.severity.toUpperCase()} ${escapeHtml(d.code)}</strong>
        <div>${escapeHtml(d.message)}</div>
        <div class="hint mono">${escapeHtml([d.file, d.tree, d.node].filter(Boolean).join(' / '))}</div>
      </div>`).join('')
    : '<div class="empty">No diagnostics.</div>';
}

function renderDiff(el: HTMLElement): void {
  if (!state.preview) { el.innerHTML = '<div class="empty">Click Preview Diff to generate a patch.</div>'; return; }
  el.innerHTML = state.preview.diffs.length
    ? state.preview.diffs.map(d => `<h3>${escapeHtml(d.path)}</h3><pre class="diff">${escapeHtml(d.patch)}</pre>`).join('')
    : '<div class="empty">No file changes in preview.</div>';
}

async function runPreview(): Promise<void> {
  const file = currentFile();
  if (!file) return;
  state.message = 'Generating preview...'; render();
  state.preview = await fetchPreview(file, state.dirtyTranslations, [...state.dirtyInquiries.values()], [...state.newEvents]);
  state.tab = 'Diff';
  state.message = state.preview.canApply ? 'Preview ready; no files written' : 'Preview has blocking diagnostics';
  render();
}

async function runApply(): Promise<void> {
  if (!state.preview?.canApply) return;
  state.message = 'Applying preview...'; render();
  await applyPreviewApi(state.preview.id);
  state.dirtyTranslations = {};
  state.dirtyInquiries.clear();
  state.createdInquiryPaths.clear();
  state.newEvents.clear();
  await init();
}
```

- [ ] **Step 2: Update `src/frontend/styles.css`**

Apply these changes (add/replace the relevant blocks):

Replace `.layout` grid:
```css
.layout {
  display: grid;
  grid-template-columns: 280px minmax(560px, 1fr) 520px;
  min-height: 0;
}
```

Replace `.card-node` and add new card styles:
```css
.card-node {
  position: absolute;
  width: 260px;
  border: 1px solid var(--accent-line);
  background: var(--panel-3);
  border-radius: 7px;
  box-shadow: 0 8px 20px #0009;
  z-index: 3;
  user-select: none;
}
.card-node.active { outline: 2px solid var(--focus); }
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px 0 18px;
  height: 38px;
  border-bottom: 1px solid var(--line);
  background: var(--panel-2);
  border-radius: 7px 7px 0 0;
  font-family: "Cascadia Mono", Consolas, monospace;
  font-weight: 700;
  color: var(--button-text);
  cursor: grab;
  position: relative;
}
.card-header:active { cursor: grabbing; }
.card-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.card-section-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
  padding: 0 10px;
  color: var(--muted);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  cursor: pointer;
  border-bottom: 1px solid color-mix(in srgb, var(--line) 50%, transparent);
}
.card-section-row:hover { background: var(--panel-2); }
.card-section-row.active { background: var(--accent-soft); color: var(--focus); }
.card-section-label {
  padding: 4px 10px;
  font-size: 10px;
  text-transform: uppercase;
  color: var(--muted);
  letter-spacing: 0.5px;
}
.card-options-section { border-bottom: 1px solid color-mix(in srgb, var(--line) 50%, transparent); }
.option-row {
  display: flex;
  align-items: center;
  height: 34px;
  padding: 0 18px 0 18px;
  position: relative;
  cursor: pointer;
  border-top: 1px solid color-mix(in srgb, var(--line) 35%, transparent);
}
.option-row:hover { background: var(--panel-2); }
.option-row.active { background: var(--accent-soft); }
.option-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: #b7e871;
}
.port-in {
  position: absolute;
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--panel-3);
  border: 2px solid var(--accent-line);
  z-index: 5;
}
.port-out {
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--panel-3);
  border: 2px solid var(--accent-line);
  z-index: 5;
}
.card-add-row {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  cursor: pointer;
  color: var(--muted);
  font-size: 18px;
  border-radius: 0 0 7px 7px;
}
.card-add-row:hover { background: var(--panel-2); color: var(--focus); }
.tree-banner {
  position: absolute;
  left: 0;
  right: 0;
  height: 36px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  color: var(--brand);
  font-family: "Cascadia Mono", Consolas, monospace;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  background: color-mix(in srgb, var(--panel) 90%, transparent);
  border-bottom: 1px solid var(--accent-line);
  z-index: 6;
}
.tree-banner-method { color: var(--brand); }
.graph .edges {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: 2;
  overflow: visible;
}
.edge-path {
  fill: none;
  stroke: url(#edge-gradient);
  stroke-width: 2.5;
  opacity: 0.8;
}
```

Remove the old `.edge`, `.edge-label`, `.card-head`, `.card-body`, `.card-badges`, `.line-preview`, `.option-preview` blocks.

Add inspector panel styles:
```css
.insp-block {
  padding: 14px;
  border-bottom: 1px solid var(--line);
}
.insp-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 11px;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--brand) 58%, #f1ded0);
  letter-spacing: 0.5px;
}
.insp-field { margin-bottom: 12px; }
.insp-field label {
  display: block;
  margin-bottom: 5px;
  color: color-mix(in srgb, var(--brand) 58%, #f1ded0);
  font-size: 11px;
  text-transform: uppercase;
}
.insp-translation {
  margin-top: 5px;
  color: var(--muted);
  font-size: 13px;
  font-style: italic;
  line-height: 1.4;
}
.insp-line-card {
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 8px;
}
.insp-line-key-row {
  display: flex;
  gap: 6px;
  align-items: center;
}
.insp-line-key-row input { flex: 1; }
.insp-divider {
  font-size: 11px;
  text-transform: uppercase;
  color: var(--muted);
  letter-spacing: 0.5px;
  padding: 10px 0 6px;
  border-top: 1px solid var(--line);
  margin-top: 4px;
}
.insp-option-stub {
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 6px;
  margin-bottom: 6px;
  cursor: pointer;
  color: var(--muted);
  font-size: 13px;
}
.insp-option-stub:hover { background: var(--panel-2); color: var(--button-text); }
.insp-danger-block { display: flex; justify-content: flex-end; }
```

Add SVG gradient to the graph div by updating `renderGraph` to include a `<defs>` block in the SVG:

In `graph.ts`, update the `el.innerHTML` SVG line to:
```ts
`<svg class="edges" width="${totalWidth}" height="${totalHeight}">
  <defs>
    <linearGradient id="edge-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" style="stop-color:var(--edge-a);stop-opacity:1"/>
      <stop offset="100%" style="stop-color:var(--edge-b);stop-opacity:1"/>
    </linearGradient>
  </defs>
  ${svgEdges}
</svg>`
```

- [ ] **Step 3: Type-check and full test run**

```bash
cd tools/dialogue_editor && npx tsc --noEmit && npm test
```

Expected: all pass.

- [ ] **Step 4: Run dev server and verify in browser**

```bash
cd tools/dialogue_editor && npm run dev
```

Open `http://127.0.0.1:5174/workspace.html` and verify:
- All NPC trees stack vertically on one canvas with tree banners between them
- Clicking a node section row (Lines, Options row, Triggers) populates the 520px right inspector
- Inspector shows Lines panel with inline translations and a delete button per line
- Inspector shows Option panel with Animation Trigger and Sound Trigger fields
- Typing in Animation/Sound fields auto-saves to `<Speaker>Metadata.json`
- SVG bezier curves connect option ports to target node input ports
- Dragging the card header repositions the card
- Sidebar shows file groups and all nodes across all trees
- Clicking a node in the sidebar opens its Lines panel
- Translations, Events, Item Inquiries, Validation, Diff tabs still function

- [ ] **Step 5: Commit**

```bash
git add src/frontend/main.ts src/frontend/styles.css
git commit -m "feat(dialogue-editor): rewrite main as orchestrator, update CSS for new layout"
```

---

## Self-Review

**Spec coverage:**
- ✅ Module split (state, api, graph, inspector, sidebar, main)
- ✅ Multi-tree canvas with vertical stacking and label banners
- ✅ Node cards with section rows (Lines, Options, Triggers) and connection ports
- ✅ SVG bezier edges replacing rotated divs
- ✅ Inspector widens to 520px, contextual based on selected row
- ✅ Inline translation text under every key (no hover-only)
- ✅ Delete button per line (was missing)
- ✅ Node rename moved to Lines panel header
- ✅ Option panel has Animation Trigger + Sound Trigger fields
- ✅ Triggers save to sidecar metadata via `/api/metadata/:speaker`
- ✅ `animationTrigger`/`soundTrigger` never touch `renderOption` or `parseOptions`
- ✅ Sidecar format: `treeMethod::nodeId::optionIndex` key
- ✅ `state.treeIndex` removed — no longer needed (all trees on one canvas)
- ✅ Drag repositioning preserved per tree via `graphKey`

**Type consistency across tasks:**
- `SelectedRow` defined in Task 1, used in Tasks 3, 6, 7 — consistent
- `NpcMetadata` defined in Task 1, used in Tasks 2, 4, 7 — consistent
- `metadataKey(treeMethod, nodeId, optionIndex)` defined in Task 3, used in Task 7 — consistent
- `graphKey(file, tree)` now takes `(DialogueFile, DialogueTreeModel)` — Tasks 3 and 6 both use this signature — consistent
- `speakerSlug(file)` defined in Task 3, used in Tasks 7 and 8 — consistent
