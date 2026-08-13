export type DiagnosticSeverity = 'error' | 'warning' | 'info';

export interface Diagnostic {
  severity: DiagnosticSeverity;
  code: string;
  message: string;
  file?: string;
  skill?: string;
}

export interface SkillModel {
  field: string;
  id: number;
  name: string;
  branch: string;
  bloodCost: number;
  maxLevels: number;
  state: string;
  parentField: string | null;
  parentFields: string[];
  skillPointCost: number;
  requiredDegree: number;
  treeX: number | null;
  treeY: number | null;
  nodeShape?: string;
  toggleable?: boolean;
  iconSource: IconSource;
  iconItem: string | null;
  description: string;
}

export type IconSource = 'item' | 'block' | null;

export interface IconRegistryOptions {
  items: string[];
  blocks: string[];
}

export interface DegreeLabelPosition {
  degree: number;
  x: number;
  y: number;
}

export interface SkillBranchFile {
  path: string;
  branch: string;
  color: string;
  degreeLabels?: DegreeLabelPosition[];
  className: string;
  source: string;
  skills: SkillModel[];
  diagnostics: Diagnostic[];
}

export interface SkillWorkspace {
  repoRoot: string;
  branches: SkillBranchFile[];
  translations: Record<string, string>;
  iconOptions: IconRegistryOptions;
  diagnostics: Diagnostic[];
}

export interface FileDiff {
  path: string;
  before: string;
  after: string;
  patch: string;
}

export interface PreviewRequest {
  branches?: SkillBranchFile[];
  translations?: Record<string, string>;
}

export interface PreviewResult {
  id: string;
  diffs: FileDiff[];
  diagnostics: Diagnostic[];
  canApply: boolean;
}

export interface ManipulationNodeModel {
  name: string;
  treeX: number;
  treeY: number;
  parents: string[];
  softParents: string[];
  nodeShape: string;
  tendency: string | null;
  secondaryTendency: string | null;
  color: string;
}

export interface ManipulationTreeFile {
  path: string;
  source: string;
  nodes: ManipulationNodeModel[];
  diagnostics: Diagnostic[];
}

export interface ManipulationWorkspace {
  repoRoot: string;
  tree: ManipulationTreeFile;
  diagnostics: Diagnostic[];
}

export interface ManipulationPreviewRequest {
  nodes: ManipulationNodeModel[];
}

export interface ScarTreeNodeModel {
  id: string;
  displayName: string;
  tendency: string | null;
  tier: number;
  color: string;
  treeX: number;
  treeY: number;
  parents: string[];
}

export interface ScarTreeFile {
  path: string;
  source: string;
  nodes: ScarTreeNodeModel[];
  diagnostics: Diagnostic[];
}

export interface ScarTreeWorkspace {
  repoRoot: string;
  tree: ScarTreeFile;
  diagnostics: Diagnostic[];
}

export interface ScarTreePreviewRequest {
  nodes: ScarTreeNodeModel[];
}

export interface TendenciesWorkspace {
  repoRoot: string;
  manipulations: ManipulationWorkspace;
  scars: ScarTreeWorkspace;
  diagnostics: Diagnostic[];
}

export interface TendenciesPreviewRequest {
  manipulations: ManipulationNodeModel[];
  scars: ScarTreeNodeModel[];
}

export type MaterialAtlasPathKey = 'HARBINGER' | 'UNSTAINED';

export type MaterialGateType = 'ALWAYS' | 'DEGREE' | 'PURITY' | 'CLARITY';

export interface MaterialGateModel {
  type: MaterialGateType;
  value: number | null;
}

export interface MaterialAtlasBucketModel {
  path: MaterialAtlasPathKey;
  id: string;
  label: string;
  color: string;
  centerX: number;
  centerY: number;
  plaqueX: number;
  plaqueY: number;
}

export interface MaterialCatalogEntryModel {
  path: MaterialAtlasPathKey;
  id: string;
  displayName: string;
  description: string;
  category: string;
  iconSource: Exclude<IconSource, null>;
  iconField: string;
  hasRecipe: boolean;
  gate: MaterialGateModel;
}

export interface MaterialAtlasEntryModel {
  path: MaterialAtlasPathKey;
  id: string;
  bucketId: string;
  gate: MaterialGateModel;
  order: number;
  parentIds: string[];
  nodeX: number | null;
  nodeY: number | null;
  catalog?: MaterialCatalogEntryModel;
}

export interface MaterialAtlasPathModel {
  path: MaterialAtlasPathKey;
  hubX: number;
  hubY: number;
  hubLabelX: number;
  hubLabelY: number;
  buckets: MaterialAtlasBucketModel[];
  entries: MaterialAtlasEntryModel[];
}

export interface MaterialAtlasWorkspace {
  repoRoot: string;
  paths: MaterialAtlasPathModel[];
  iconOptions: IconRegistryOptions;
  diagnostics: Diagnostic[];
}

export interface MaterialAtlasPreviewRequest {
  paths: MaterialAtlasPathModel[];
  catalogueEntries: MaterialCatalogEntryModel[];
}

export type RecipeMapTabKey = 'RITES' | 'CRAFTING';
export type RecipeMapLinkKind = 'PROGRESSION' | 'CONCEPTUAL';

export interface RecipeMapEditorEntry {
  id: string;
  family: string;
  order: number;
  column: number;
  displayName: string;
  treeX?: number;
  treeY?: number;
  iconSource?: IconSource;
  iconItem?: string | null;
  resultIcon?: string;
}

export interface RecipeMapEditorLink {
  from: string;
  to: string;
  kind: RecipeMapLinkKind;
}

export interface RecipeMapEditorTab {
  key: RecipeMapTabKey;
  families: string[];
  entries: RecipeMapEditorEntry[];
  links: RecipeMapEditorLink[];
}

export interface RecipeMapWorkspace {
  repoRoot: string;
  tabs: RecipeMapEditorTab[];
  iconOptions: IconRegistryOptions;
  diagnostics: Diagnostic[];
}

export interface RecipeMapPreviewRequest {
  tabs: RecipeMapEditorTab[];
}
