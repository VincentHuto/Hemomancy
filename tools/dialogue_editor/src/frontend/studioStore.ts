import { create } from 'zustand';
import type { DialogueFile, DialogueNodeModel, DialogueOptionModel, DialogueTreeModel, DialogueWorkspace, PreviewResult } from '../shared/types';
import { createNode, createSimulator, createTree as createDialogueTree, deleteTree, duplicateTree, findTree, followChoice, makeDraft, renameNode, treeIdentity, type SimulatorState, type StudioDraft, type StudioMode } from './studioModel';
import type { StoredPosition } from './studioLayout';

type PositionMap = Record<string, Record<string, StoredPosition>>;

interface StudioState {
  draft: StudioDraft | null;
  filePath: string | null;
  treeMethod: string | null;
  treeVariant: number | undefined;
  treeKey: string | null;
  nodeId: string | null;
  mode: StudioMode;
  query: string;
  rightTab: 'edit' | 'simulate';
  simulator: SimulatorState | null;
  positions: PositionMap;
  preview: PreviewResult | null;
  loading: boolean;
  message: string;
  undoStack: StudioDraft[];
  redoStack: StudioDraft[];
  load(workspace: DialogueWorkspace): void;
  selectFile(path: string): void;
  selectTree(method: string, variant?: number, paramSource?: string): void;
  selectNode(id: string | null): void;
  setMode(mode: StudioMode): void;
  setQuery(query: string): void;
  setRightTab(tab: 'edit' | 'simulate'): void;
  updateNodeLine(nodeId: string, index: number, prose: string): void;
  updateChoice(nodeId: string, index: number, patch: Partial<DialogueOptionModel>): void;
  updateChoiceProse(nodeId: string, index: number, prose: string): void;
  addNode(): void;
  duplicateTree(): void;
  createTree(): void;
  removeTree(): void;
  renameNode(oldId: string, newId: string): void;
  addLine(nodeId: string): void;
  addChoice(nodeId: string): void;
  removeChoice(nodeId: string, index: number): void;
  removeNode(nodeId: string): void;
  updateInquiryLine(key: string, prose: string): void;
  updateTrigger(nodeId: string, optionIndex: number, field: 'animationTrigger' | 'soundTrigger', value: string): void;
  setPositions(key: string, positions: Record<string, StoredPosition>): void;
  pinNode(key: string, nodeId: string, position: StoredPosition): void;
  startSimulation(): void;
  choose(index: number): void;
  simulatorBack(): void;
  setPreview(preview: PreviewResult | null): void;
  setMessage(message: string): void;
  undo(): void;
  redo(): void;
}

const DRAFT_KEY = 'hemomancy-dialogue-studio-draft-v2';
const POSITIONS_KEY = 'hemomancy-dialogue-studio-layout-v2';

function clone<T>(value: T): T { return JSON.parse(JSON.stringify(value)) as T; }

function persist(draft: StudioDraft): void {
  try { localStorage.setItem(DRAFT_KEY, JSON.stringify(draft)); } catch { /* local draft recovery is best effort */ }
}

function loadPositions(): PositionMap {
  try { return JSON.parse(localStorage.getItem(POSITIONS_KEY) ?? '{}') as PositionMap; } catch { return {}; }
}

function persistPositions(positions: PositionMap): void {
  try { localStorage.setItem(POSITIONS_KEY, JSON.stringify(positions)); } catch { /* layout persistence is best effort */ }
}

function initialSelection(workspace: DialogueWorkspace): { filePath: string | null; treeMethod: string | null; treeVariant: number | undefined; treeKey: string | null } {
  const file = workspace.dialogueFiles[0];
  const tree = file?.trees.find(candidate => !candidate.dispatchOnly) ?? file?.trees[0];
  return { filePath: file?.path ?? null, treeMethod: tree?.method ?? null, treeVariant: tree?.variant, treeKey: tree ? treeIdentity(tree) : null };
}

export const useStudio = create<StudioState>((set, get) => {
  function mutate(mutator: (draft: StudioDraft) => void): void {
    const current = get().draft;
    if (!current) return;
    const next = clone(current);
    mutator(next);
    persist(next);
    set(state => ({ draft: next, undoStack: [...state.undoStack.slice(-49), current], redoStack: [], preview: null }));
  }

  function context(): { draft: StudioDraft; filePath: string; tree: DialogueTreeModel } | null {
    const { draft, filePath, treeMethod, treeVariant, treeKey } = get();
    if (!draft || !filePath || !treeMethod) return null;
    const file = draft.workspace.dialogueFiles.find(candidate => candidate.path === filePath);
    const tree = file?.trees.find(candidate => treeIdentity(candidate) === treeKey) ?? findTree(draft, filePath, treeMethod, treeVariant);
    return { draft, filePath, tree };
  }

  return {
    draft: null, filePath: null, treeMethod: null, treeVariant: undefined, treeKey: null, nodeId: null, mode: 'dialogue', query: '', rightTab: 'edit',
    simulator: null, positions: loadPositions(), preview: null, loading: true, message: 'Loading dialogue workspace…', undoStack: [], redoStack: [],
    load(workspace) {
      let draft = makeDraft(workspace);
      try {
        const saved = JSON.parse(localStorage.getItem(DRAFT_KEY) ?? 'null') as StudioDraft | null;
        if (saved?.baseRevision === (workspace.revision ?? '')) draft = saved;
      } catch { /* ignore malformed draft */ }
      const metadataPositions = Object.values(draft.workspace.metadata ?? {}).reduce<PositionMap>((all, meta) => {
        Object.entries(meta.layouts ?? {}).forEach(([key, layout]) => { all[key] = layout.positions; }); return all;
      }, {});
      set(state => ({ draft, ...initialSelection(draft.workspace), positions: { ...state.positions, ...metadataPositions }, loading: false, message: `${workspace.dialogueFiles.length} NPC dialogue files ready`, undoStack: [], redoStack: [] }));
    },
    selectFile(path) {
      const file = get().draft?.workspace.dialogueFiles.find(candidate => candidate.path === path);
      const tree = file?.trees.find(candidate => !candidate.dispatchOnly) ?? file?.trees[0];
      set({ filePath: path, treeMethod: tree?.method ?? null, treeVariant: tree?.variant, treeKey: tree ? treeIdentity(tree) : null, nodeId: null, simulator: null, query: '' });
    },
    selectTree(method, treeVariant, paramSource) { set({ treeMethod: method, treeVariant, treeKey: treeIdentity({ method, variant: treeVariant, paramSource, params: [] }), nodeId: null, simulator: null, mode: 'dialogue' }); },
    selectNode(nodeId) { set({ nodeId, rightTab: nodeId ? 'edit' : get().rightTab }); },
    setMode(mode) { set({ mode }); },
    setQuery(query) { set({ query }); },
    setRightTab(rightTab) { set({ rightTab }); },
    updateNodeLine(nodeId, index, prose) { mutate(draft => {
      const ctx = context(); if (!ctx) return;
      const node = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource).nodes.find(candidate => candidate.id === nodeId);
      const key = node?.lines[index]; if (key) draft.workspace.translations[key] = prose;
    }); },
    updateChoice(nodeId, index, patch) { mutate(draft => {
      const ctx = context(); if (!ctx) return;
      const option = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource).nodes.find(candidate => candidate.id === nodeId)?.options[index];
      if (option) Object.assign(option, patch);
    }); },
    updateChoiceProse(nodeId, index, prose) { mutate(draft => {
      const ctx = context(); if (!ctx) return;
      const option = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource).nodes.find(candidate => candidate.id === nodeId)?.options[index];
      if (option) draft.workspace.translations[option.text] = prose;
    }); },
    addNode() { const ctx = context(); if (!ctx) return; mutate(draft => { const node = createNode(draft, ctx.filePath, ctx.tree.method, 'new_node', ctx.tree.variant, ctx.tree.paramSource); set({ nodeId: node.id }); }); },
    createTree() { const draft = get().draft; const filePath = get().filePath; if (!draft || !filePath) return; let method = 'newDialogue'; let n = 2; const methods = new Set(findFileMethods(draft, filePath)); while (methods.has(method)) method = `newDialogue${n++}`; mutate(next => { createDialogueTree(next, filePath, method); }); const tree = get().draft?.workspace.dialogueFiles.find(file => file.path === filePath)?.trees.find(candidate => candidate.method === method); set({ treeMethod: method, treeVariant: undefined, treeKey: tree ? treeIdentity(tree) : null, nodeId: 'root', mode: 'dialogue' }); },
    duplicateTree() { const ctx = context(); if (!ctx || ctx.tree.dispatchOnly) return; let method = `${ctx.tree.method}Copy`; let n = 2; const methods = new Set(findFileMethods(ctx.draft, ctx.filePath)); while (methods.has(method)) method = `${ctx.tree.method}Copy${n++}`; mutate(draft => { duplicateTree(draft, ctx.filePath, ctx.tree.method, method, ctx.tree.variant, ctx.tree.paramSource); }); set({ treeMethod: method, treeVariant: undefined, treeKey: treeIdentity({ method, variant: undefined, paramSource: ctx.tree.paramSource, params: ctx.tree.params }), nodeId: null }); },
    removeTree() { const ctx = context(); if (!ctx || ctx.tree.dispatchOnly) return; mutate(draft => { deleteTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource); }); const file = get().draft?.workspace.dialogueFiles.find(candidate => candidate.path === ctx.filePath); const next = file?.trees.find(tree => !tree.dispatchOnly); set({ treeMethod: next?.method ?? null, treeVariant: next?.variant, treeKey: next ? treeIdentity(next) : null, nodeId: null }); },
    renameNode(oldId, newId) { const ctx = context(); if (!ctx) return; mutate(draft => renameNode(draft, ctx.filePath, ctx.tree.method, oldId, newId, ctx.tree.variant, ctx.tree.paramSource)); set({ nodeId: newId }); },
    addLine(nodeId) { mutate(draft => {
      const ctx = context(); if (!ctx) return;
      const tree = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource);
      const node = tree.nodes.find(candidate => candidate.id === nodeId); if (!node) return;
      const file = draft.workspace.dialogueFiles.find(candidate => candidate.path === ctx.filePath)!;
      const slug = (file.path.split(/[\\/]/).at(-1) ?? 'npc').replace(/DialogueTrees\.java$/, '').replace(/([a-z0-9])([A-Z])/g, '$1_$2').toLowerCase();
      const key = `hemomancy.dialogue.${slug}.${tree.method}.${node.id}.line${node.lines.length + 1}`;
      node.lines.push(key); draft.workspace.translations[key] = 'New dialogue line';
    }); },
    addChoice(nodeId) { mutate(draft => {
      const ctx = context(); if (!ctx) return;
      const tree = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource);
      const node = tree.nodes.find(candidate => candidate.id === nodeId); if (!node) return;
      const key = `hemomancy.dialogue.${tree.method}.${node.id}.option${node.options.length + 1}`;
      node.options.push({ text: key, next: null, event: null }); draft.workspace.translations[key] = 'New response';
    }); },
    removeChoice(nodeId, index) { mutate(draft => { const ctx = context(); if (!ctx) return; findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource).nodes.find(node => node.id === nodeId)?.options.splice(index, 1); }); },
    removeNode(nodeId) { mutate(draft => {
      const ctx = context(); if (!ctx) return; const tree = findTree(draft, ctx.filePath, ctx.tree.method, ctx.tree.variant, ctx.tree.paramSource);
      tree.nodes = tree.nodes.filter(node => node.id !== nodeId); tree.nodes.forEach(node => node.options.forEach(option => { if (option.next === nodeId) option.next = null; }));
      if (tree.startNode === nodeId) tree.startNode = tree.nodes[0]?.id ?? null;
    }); set({ nodeId: null }); },
    updateInquiryLine(key, prose) { mutate(draft => { draft.workspace.translations[key] = prose; }); },
    updateTrigger(nodeId, optionIndex, field, value) { const ctx = context(); if (!ctx) return; mutate(draft => {
      const slug = fileSlug(ctx.filePath); const metadata = draft.workspace.metadata ??= {};
      const npc = metadata[slug] ??= { version: 2, options: {} };
      npc.version = 2;
      const key = `${treeIdentity(ctx.tree)}::${nodeId}::${optionIndex}`;
      npc.options[key] = { ...(npc.options[key] ?? {}), [field]: value || undefined };
    }); },
    setPositions(key, positions) { set(state => { const next = { ...state.positions, [key]: positions }; persistPositions(next); return { positions: next }; }); },
    pinNode(key, nodeId, position) { const ctx = context(); if (ctx) mutate(draft => {
      const slug = fileSlug(ctx.filePath); const metadata = draft.workspace.metadata ??= {};
      const npc = metadata[slug] ??= { version: 2, options: {} }; npc.version = 2; npc.layouts ??= {};
      const previous = npc.layouts[key]?.positions ?? {};
      npc.layouts[key] = { positions: { ...previous, [nodeId]: { ...position, pinned: true } } };
    }); set(state => {
      const next = {
        ...state.positions,
        [key]: { ...(state.positions[key] ?? {}), [nodeId]: { ...position, pinned: true } }
      };
      persistPositions(next);
      return { positions: next };
    }); },
    startSimulation() { const ctx = context(); if (ctx) set({ simulator: createSimulator(ctx.tree), rightTab: 'simulate' }); },
    choose(index) { const ctx = context(); const simulator = get().simulator; if (ctx && simulator) { const next = followChoice(simulator, ctx.tree, index); set({ simulator: next, nodeId: next.currentNodeId }); } },
    simulatorBack() { const ctx = context(); const simulator = get().simulator; if (!ctx || !simulator || simulator.history.length < 2) return; const history = simulator.history.slice(0, -1); set({ simulator: { ...simulator, history, currentNodeId: history.at(-1) ?? null, ended: false }, nodeId: history.at(-1) ?? null }); },
    setPreview(preview) { set({ preview }); }, setMessage(message) { set({ message }); },
    undo() { const state = get(); const previous = state.undoStack.at(-1); if (!previous || !state.draft) return; persist(previous); set({ draft: clone(previous), ...reconcileSelection(previous, state), undoStack: state.undoStack.slice(0, -1), redoStack: [...state.redoStack, state.draft], preview: null }); },
    redo() { const state = get(); const next = state.redoStack.at(-1); if (!next || !state.draft) return; persist(next); set({ draft: clone(next), ...reconcileSelection(next, state), undoStack: [...state.undoStack, state.draft], redoStack: state.redoStack.slice(0, -1), preview: null }); }
  };
});

function findFileMethods(draft: StudioDraft, path: string): string[] {
  return draft.workspace.dialogueFiles.find(file => file.path === path)?.trees.map(tree => tree.method) ?? [];
}

function fileSlug(path: string): string {
  return (path.split(/[\\/]/).at(-1) ?? path).replace(/DialogueTrees\.java$/, '');
}

function reconcileSelection(draft: StudioDraft, state: Pick<StudioState, 'filePath' | 'treeKey' | 'nodeId'>) {
  const file = draft.workspace.dialogueFiles.find(candidate => candidate.path === state.filePath) ?? draft.workspace.dialogueFiles[0];
  const tree = file?.trees.find(candidate => treeIdentity(candidate) === state.treeKey) ?? file?.trees.find(candidate => !candidate.dispatchOnly) ?? file?.trees[0];
  const nodeId = tree?.nodes.some(node => node.id === state.nodeId) ? state.nodeId : null;
  return { filePath: file?.path ?? null, treeMethod: tree?.method ?? null, treeVariant: tree?.variant, treeKey: tree ? treeIdentity(tree) : null, nodeId };
}

export function activeContext(state: Pick<StudioState, 'draft' | 'filePath' | 'treeMethod' | 'treeVariant' | 'treeKey'>): { file: DialogueFile; tree: DialogueTreeModel } | null {
  if (!state.draft || !state.filePath || !state.treeMethod) return null;
  const file = state.draft.workspace.dialogueFiles.find(candidate => candidate.path === state.filePath);
  const candidates = file?.trees.filter(candidate => candidate.method === state.treeMethod) ?? [];
  const tree = candidates.find(candidate => treeIdentity(candidate) === state.treeKey) ?? (state.treeVariant === undefined ? candidates.find(candidate => candidate.variant === undefined) ?? candidates[0] : candidates.find(candidate => candidate.variant === state.treeVariant));
  return file && tree ? { file, tree } : null;
}

export function activeNode(state: Pick<StudioState, 'draft' | 'filePath' | 'treeMethod' | 'treeVariant' | 'treeKey' | 'nodeId'>): DialogueNodeModel | null {
  const ctx = activeContext(state);
  return ctx?.tree.nodes.find(node => node.id === state.nodeId) ?? null;
}
