export type DialogueTheme = 'BLOOD' | 'UNSTAINED' | 'FUNGAL' | string;
export type DiagnosticSeverity = 'error' | 'warning' | 'info';

export interface SourceSpan {
  start: number;
  end: number;
}

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

export interface NpcMetadataOption {
  animationTrigger?: string;
  soundTrigger?: string;
}

export interface NpcMetadata {
  version: number;
  options: Record<string, NpcMetadataOption>;
  layouts?: Record<string, { positions: Record<string, { x: number; y: number; pinned?: boolean }> }>;
}

export type SelectedRow =
  | { treeMethod: string; nodeId: string; section: 'lines' | 'triggers' }
  | { treeMethod: string; nodeId: string; section: 'option'; optionIndex: number }
  | null;

export interface DialogueNodeModel {
  id: string;
  lines: string[];
  options: DialogueOptionModel[];
}

export interface DialogueTreeModel {
  method: string;
  visibility: 'public' | 'private';
  params: string[];
  variant?: number;
  theme: DialogueTheme;
  startNode: string | null;
  nodes: DialogueNodeModel[];
  sourceSpan?: SourceSpan;
  methodSourceSpan?: SourceSpan;
  sourceMethod?: string;
  paramSource?: string;
  newTree?: boolean;
  routes?: RouteTarget[];
  dispatchOnly?: boolean;
  speaker?: string;
  icon?: string;
}

export interface DialogueFile {
  path: string;
  sourceFile: string;
  speaker: string;
  icon: string;
  trees: DialogueTreeModel[];
  diagnostics: Diagnostic[];
}

export interface TranslationEntry {
  key: string;
  value: string;
  usedBy?: string[];
}

export interface DialogueInquiryEntry {
  path: string;
  npcId: string;
  itemId: string;
  lines: string[];
  conditions?: DialogueInquiryCondition[];
  valid: boolean;
  error?: string;
}

export interface DialogueInquiryCondition {
  lines: string[];
  [key: string]: unknown;
}

export interface DialogueEventCatalogEntry {
  id: string;
  kind: 'handler' | 'memo';
  source: string;
  constant?: string;
  path?: string;
  handled: boolean;
}

export interface RegistryEntry {
  id: string;
  kind: 'item' | 'block';
  source: string;
  hasInquiry: boolean;
}

export interface Diagnostic {
  severity: DiagnosticSeverity;
  code: string;
  message: string;
  file?: string;
  tree?: string;
  node?: string;
  optionIndex?: number;
}

export interface DialogueWorkspace {
  repoRoot: string;
  revision?: string;
  dialogueFiles: DialogueFile[];
  translations: Record<string, string>;
  inquiries: DialogueInquiryEntry[];
  registries: RegistryEntry[];
  events: DialogueEventCatalogEntry[];
  memos: DialogueEventCatalogEntry[];
  diagnostics: Diagnostic[];
  metadata?: Record<string, NpcMetadata>;
}

export interface RouteTarget {
  method: string;
  condition: string;
  sourceLine?: number;
}

export interface DialogueRouterModel {
  method: string;
  params: string[];
  targets: RouteTarget[];
}

export interface FileChange {
  path: string;
  content: string;
}

export interface PreviewRequest {
  baseRevision?: string;
  files?: FileChange[];
  dialogueFiles?: DialogueFile[];
  translations?: Record<string, string>;
  inquiries?: DialogueInquiryEntry[];
  newEvents?: string[];
  metadata?: Record<string, NpcMetadata>;
}

export interface FileDiff {
  path: string;
  before: string;
  after: string;
  patch: string;
}

export interface PreviewResult {
  id: string;
  diffs: FileDiff[];
  diagnostics: Diagnostic[];
  canApply: boolean;
}
