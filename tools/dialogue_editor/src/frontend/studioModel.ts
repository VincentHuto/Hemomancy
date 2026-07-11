import type { DialogueFile, DialogueNodeModel, DialogueTreeModel, DialogueWorkspace, PreviewRequest } from '../shared/types';

export type StudioMode = 'dialogue' | 'routes' | 'inquiries' | 'changes';

export interface StudioDraft {
  baseRevision: string;
  original: DialogueWorkspace;
  workspace: DialogueWorkspace;
}

export interface SimulatorState {
  currentNodeId: string | null;
  history: string[];
  events: string[];
  ended: boolean;
}

export function treeIdentity(tree: Pick<DialogueTreeModel, 'method' | 'variant' | 'paramSource' | 'params'>): string {
  return `${tree.method}::${tree.paramSource ?? tree.params.join(',')}::${tree.variant ?? 'main'}`;
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

export function makeDraft(workspace: DialogueWorkspace): StudioDraft {
  return { baseRevision: workspace.revision ?? '', original: clone(workspace), workspace: clone(workspace) };
}

function findFile(draft: StudioDraft, path: string): DialogueFile {
  const file = draft.workspace.dialogueFiles.find(candidate => candidate.path === path);
  if (!file) throw new Error(`Dialogue file not found: ${path}`);
  return file;
}

export function findTree(draft: StudioDraft, path: string, method: string, variant?: number, paramSource?: string): DialogueTreeModel {
  const candidates = findFile(draft, path).trees.filter(candidate => candidate.method === method && (paramSource === undefined || candidate.paramSource === paramSource));
  const tree = variant === undefined ? candidates.find(candidate => candidate.variant === undefined) ?? candidates[0] : candidates.find(candidate => candidate.variant === variant);
  if (!tree) throw new Error(`Dialogue tree not found: ${method}`);
  return tree;
}

function findNode(draft: StudioDraft, path: string, method: string, nodeId: string): DialogueNodeModel {
  const node = findTree(draft, path, method).nodes.find(candidate => candidate.id === nodeId);
  if (!node) throw new Error(`Dialogue node not found: ${nodeId}`);
  return node;
}

export function changeNodeLine(draft: StudioDraft, path: string, method: string, nodeId: string, index: number, prose: string): void {
  const node = findNode(draft, path, method, nodeId);
  const key = node.lines[index];
  if (!key) throw new Error(`Dialogue line not found: ${index}`);
  draft.workspace.translations[key] = prose;
}

export function changeChoiceText(draft: StudioDraft, path: string, method: string, nodeId: string, index: number, prose: string): void {
  const option = findNode(draft, path, method, nodeId).options[index];
  if (!option) throw new Error(`Dialogue choice not found: ${index}`);
  draft.workspace.translations[option.text] = prose;
}

export function renameNode(draft: StudioDraft, path: string, method: string, oldId: string, newId: string, variant?: number, paramSource?: string): void {
  const tree = findTree(draft, path, method, variant, paramSource);
  if (!newId.trim() || tree.nodes.some(node => node.id === newId)) throw new Error(`Node id must be unique: ${newId}`);
  const node = findNode(draft, path, method, oldId);
  node.id = newId;
  if (tree.startNode === oldId) tree.startNode = newId;
  tree.nodes.forEach(candidate => candidate.options.forEach(option => { if (option.next === oldId) option.next = newId; }));
}

function slugFor(file: DialogueFile): string {
  return (file.path.split(/[\\/]/).at(-1) ?? 'npc')
    .replace(/DialogueTrees\.java$/, '')
    .replace(/\.java$/, '')
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .toLowerCase();
}

export function createNode(draft: StudioDraft, path: string, method: string, requestedId = 'new_node', variant?: number, paramSource?: string): DialogueNodeModel {
  const file = findFile(draft, path);
  const tree = findTree(draft, path, method, variant, paramSource);
  let id = requestedId;
  let suffix = 2;
  while (tree.nodes.some(node => node.id === id)) id = `${requestedId}_${suffix++}`;
  const key = `hemomancy.dialogue.${slugFor(file)}.${method}.${id}.line1`;
  const node: DialogueNodeModel = { id, lines: [key], options: [] };
  tree.nodes.push(node);
  if (!tree.startNode) tree.startNode = id;
  draft.workspace.translations[key] = 'New dialogue line';
  return node;
}

export function duplicateTree(draft: StudioDraft, path: string, method: string, requestedMethod: string, variant?: number, paramSource?: string): DialogueTreeModel {
  const file = findFile(draft, path);
  if (file.trees.some(tree => tree.method === requestedMethod)) throw new Error(`Tree method must be unique: ${requestedMethod}`);
  const source = findTree(draft, path, method, variant, paramSource);
  if (source.dispatchOnly) throw new Error('Router methods cannot be duplicated in the studio.');
  const copy = clone(source);
  copy.method = requestedMethod;
  copy.sourceMethod = undefined;
  copy.sourceSpan = undefined;
  copy.methodSourceSpan = undefined;
  copy.variant = undefined;
  copy.newTree = true;
  file.trees.push(copy);
  return copy;
}

export function createTree(draft: StudioDraft, path: string, requestedMethod: string): DialogueTreeModel {
  const file = findFile(draft, path);
  if (!/^[A-Za-z_$][\w$]*$/.test(requestedMethod) || file.trees.some(tree => tree.method === requestedMethod)) throw new Error(`Tree method must be a unique Java identifier: ${requestedMethod}`);
  const template = file.trees.find(tree => !tree.dispatchOnly);
  const key = `hemomancy.dialogue.${slugFor(file)}.${requestedMethod}.root.line1`;
  const tree: DialogueTreeModel = {
    method: requestedMethod,
    visibility: 'private',
    params: template?.params ?? ['entityId'],
    paramSource: template?.paramSource || 'int entityId',
    theme: template?.theme ?? 'BLOOD',
    startNode: 'root',
    nodes: [{ id: 'root', lines: [key], options: [] }],
    speaker: file.speaker,
    icon: file.icon,
    newTree: true
  };
  file.trees.push(tree);
  draft.workspace.translations[key] = 'New dialogue line';
  return tree;
}

export function deleteTree(draft: StudioDraft, path: string, method: string, variant?: number, paramSource?: string): void {
  const file = findFile(draft, path);
  const matching = file.trees.filter(candidate => candidate.method === method && (paramSource === undefined || candidate.paramSource === paramSource));
  const tree = variant === undefined ? matching.find(candidate => candidate.variant === undefined) ?? matching[0] : matching.find(candidate => candidate.variant === variant);
  if (!tree || tree.dispatchOnly) throw new Error('Only concrete builder trees can be deleted in the studio.');
  file.trees = file.trees.filter(candidate => candidate !== tree);
}

export function createSimulator(tree: DialogueTreeModel): SimulatorState {
  const start = tree.startNode ?? tree.nodes[0]?.id ?? null;
  return { currentNodeId: start, history: start ? [start] : [], events: [], ended: !start };
}

export function followChoice(state: SimulatorState, tree: DialogueTreeModel, choiceIndex: number): SimulatorState {
  if (!state.currentNodeId) return { ...state, ended: true };
  const option = tree.nodes.find(node => node.id === state.currentNodeId)?.options[choiceIndex];
  if (!option) return state;
  const events = option.event ? [...state.events, option.event] : state.events;
  if (!option.next) return { ...state, events, ended: true };
  return { currentNodeId: option.next, history: [...state.history, option.next], events, ended: false };
}

export function pendingSummary(draft: StudioDraft): { dialogueFiles: number; translations: number; inquiries: number; metadata: number; total: number } {
  const translationKeys = new Set([...Object.keys(draft.original.translations), ...Object.keys(draft.workspace.translations)]);
  const changedTranslationKeys = [...translationKeys].filter(key => draft.original.translations[key] !== draft.workspace.translations[key]);
  const translations = changedTranslationKeys.length;
  const dialogueFiles = draft.workspace.dialogueFiles.filter((file, index) => {
    if (JSON.stringify(file) !== JSON.stringify(draft.original.dialogueFiles[index])) return true;
    const used = new Set(file.trees.flatMap(tree => tree.nodes.flatMap(node => [...node.lines, ...node.options.map(option => option.text)])));
    return changedTranslationKeys.some(key => used.has(key));
  }).length;
  const inquiries = draft.workspace.inquiries.filter((entry, index) => JSON.stringify(entry) !== JSON.stringify(draft.original.inquiries[index])).length;
  const metadataKeys = new Set([...Object.keys(draft.original.metadata ?? {}), ...Object.keys(draft.workspace.metadata ?? {})]);
  const metadata = [...metadataKeys].filter(key => JSON.stringify(draft.original.metadata?.[key]) !== JSON.stringify(draft.workspace.metadata?.[key])).length;
  return { dialogueFiles, translations, inquiries, metadata, total: dialogueFiles + translations + inquiries + metadata };
}

export function buildPreviewRequest(draft: StudioDraft, handledEventIds: string[]): PreviewRequest {
  const translations = Object.fromEntries(Object.keys(draft.workspace.translations)
    .filter(key => draft.workspace.translations[key] !== draft.original.translations[key])
    .map(key => [key, draft.workspace.translations[key]]));
  const changedKeys = new Set(Object.keys(translations));
  const originalFiles = new Map(draft.original.dialogueFiles.map(file => [file.path, file]));
  const dialogueFiles = draft.workspace.dialogueFiles.filter(file => {
    if (JSON.stringify(file) !== JSON.stringify(originalFiles.get(file.path))) return true;
    return file.trees.some(tree => tree.nodes.some(node => [...node.lines, ...node.options.map(option => option.text)].some(key => changedKeys.has(key))));
  });
  const originalInquiries = new Map(draft.original.inquiries.map(entry => [entry.path, entry]));
  const inquiries = draft.workspace.inquiries.filter(entry => JSON.stringify(entry) !== JSON.stringify(originalInquiries.get(entry.path)));
  const handled = new Set(handledEventIds);
  const newEvents = [...new Set(dialogueFiles.flatMap(file => file.trees.flatMap(tree => tree.nodes.flatMap(node => node.options
    .filter(option => option.event && !option.eventExpression && !handled.has(option.event))
    .map(option => option.event!)))))];
  const metadata = Object.fromEntries(Object.entries(draft.workspace.metadata ?? {})
    .filter(([slug, value]) => JSON.stringify(value) !== JSON.stringify(draft.original.metadata?.[slug])));
  return { baseRevision: draft.baseRevision, dialogueFiles, translations, inquiries, newEvents, metadata };
}
