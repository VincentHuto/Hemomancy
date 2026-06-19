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

export function metadataKey(treeMethod: string, variant: number | undefined, nodeId: string, optionIndex: number): string {
  const vStr = variant !== undefined ? `::${variant}` : '';
  return `${treeMethod}${vStr}::${nodeId}::${optionIndex}`;
}

export function optionMeta(slug: string, treeMethod: string, variant: number | undefined, nodeId: string, optionIndex: number) {
  return state.metadata[slug]?.options[metadataKey(treeMethod, variant, nodeId, optionIndex)] ?? {};
}
