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
  iconItem: string | null;
  description: string;
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
