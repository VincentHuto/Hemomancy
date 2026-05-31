import type { SkillBranchFile, SkillModel } from '../shared/types';

export interface GraphNodePosition {
  skill: SkillModel;
  x: number;
  y: number;
}

export interface GraphBranchLabel {
  branch: string;
  text: string;
  x: number;
  y: number;
}

export interface GraphBranchBand {
  branch: string;
  y: number;
  height: number;
}

export interface GraphEdge {
  fromField: string;
  toField: string;
  toBranch: string;
  kind: 'local' | 'cross-branch';
  path: string;
}

export interface GraphDegreeGuide {
  degree: number;
  label: string;
  y: number;
}

export interface GraphLayout {
  nodes: GraphNodePosition[];
  labels: GraphBranchLabel[];
  bands: GraphBranchBand[];
  edges: GraphEdge[];
  degreeGuides: GraphDegreeGuide[];
  width: number;
  height: number;
}

const LEFT_LABEL_X = 24;
const NODE_START_X = 210;
const BRANCH_START_Y = 72;
const BRANCH_LABEL_OFFSET_Y = 28;
const NODE_STEP_Y = 72;
const NODE_STEP_X = 96;
const NODE_WIDTH = 48;
const BRANCH_GAP = 120;
const MIN_BRANCH_HEIGHT = 142;
const MIN_MAX_DEGREE = 5;
const CANVAS_PADDING = 120;
const DEGREE_START_Y = 64;

export function computeGraphLayout(branches: SkillBranchFile[]): GraphLayout {
  const skills = branches.flatMap(branch => branch.skills);
  const maxDegree = Math.max(MIN_MAX_DEGREE, ...skills.map(skill => skill.requiredDegree));
  const degreeGuides = Array.from({ length: maxDegree + 1 }, (_, degree) => ({
    degree,
    label: `Degree ${degree}`,
    y: degreeTierY(degree, maxDegree)
  }));
  const maxGuideY = Math.max(...degreeGuides.map(guide => guide.y));
  const nodes: GraphNodePosition[] = [];
  const labels: GraphBranchLabel[] = [];
  const bands: GraphBranchBand[] = [];
  let cursorX = NODE_START_X;
  let maxNodeX = 0;
  let maxNodeY = 0;

  for (const branch of branches) {
    const xByField = layoutBranchColumns(branch, cursorX);
    const branchNodes = branch.skills.map((skill) => ({
      skill,
      x: skill.treeX ?? xByField.get(skill.field) ?? cursorX,
      y: skill.treeY ?? degreeTierY(skill.requiredDegree, maxDegree)
    }));
    const localMinY = Math.min(BRANCH_START_Y, ...branchNodes.map(node => node.y - 44));
    const localMaxY = Math.max(BRANCH_START_Y, ...branchNodes.map(node => node.y + 72));
    const localMaxX = Math.max(cursorX, ...branchNodes.map(node => node.x + 72));
    const branchHeight = Math.max(MIN_BRANCH_HEIGHT, localMaxY - localMinY);

    labels.push({
      branch: branch.branch,
      text: labelize(branch.branch),
      x: LEFT_LABEL_X,
      y: localMinY + BRANCH_LABEL_OFFSET_Y
    });
    bands.push({
      branch: branch.branch,
      y: localMinY,
      height: branchHeight
    });
    nodes.push(...branchNodes);
    maxNodeX = Math.max(maxNodeX, ...branchNodes.map(node => node.x));
    maxNodeY = Math.max(maxNodeY, ...branchNodes.map(node => node.y));
    cursorX = localMaxX + BRANCH_GAP;
  }

  const byPosition = new Map(nodes.map(node => [node.skill.field, node]));
  const edges = nodes
    .filter(node => node.skill.parentField && byPosition.has(node.skill.parentField))
    .map(node => {
      const parent = byPosition.get(node.skill.parentField!)!;
      const kind: GraphEdge['kind'] = parent.skill.branch === node.skill.branch ? 'local' : 'cross-branch';
      return {
        fromField: parent.skill.field,
        toField: node.skill.field,
        toBranch: node.skill.branch,
        kind,
        path: edgePath(parent, node, kind)
      };
    });

  return {
    nodes,
    labels,
    bands,
    edges,
    degreeGuides,
    width: Math.max(980, CANVAS_PADDING + maxNodeX + NODE_WIDTH),
    height: Math.max(620, CANVAS_PADDING + Math.max(maxGuideY, maxNodeY))
  };
}

function edgePath(parent: GraphNodePosition, child: GraphNodePosition, kind: GraphEdge['kind']): string {
  const fromX = Math.round(parent.x);
  const fromY = Math.round(parent.y);
  const toX = Math.round(child.x);
  const toY = Math.round(child.y);
  const midY = Math.round((fromY + toY) / 2);
  return `M ${fromX} ${fromY} L ${fromX} ${midY} L ${toX} ${midY} L ${toX} ${toY}`;
}

function layoutBranchColumns(branch: SkillBranchFile, branchX: number): Map<string, number> {
  const byField = new Map(branch.skills.map(skill => [skill.field, skill]));
  const children = new Map<string, SkillModel[]>();
  for (const skill of branch.skills) {
    if (!skill.parentField || !byField.has(skill.parentField)) continue;
    const existing = children.get(skill.parentField) ?? [];
    existing.push(skill);
    children.set(skill.parentField, existing);
  }
  for (const group of children.values()) {
    group.sort(compareSkillPosition);
  }

  const roots = branch.skills
    .filter(skill => !skill.parentField || !byField.has(skill.parentField))
    .sort(compareSkillPosition);
  const xByField = new Map<string, number>();
  let column = 0;

  const place = (skill: SkillModel): number => {
    const existing = xByField.get(skill.field);
    if (existing !== undefined) return existing;
    const localChildren = children.get(skill.field) ?? [];
    if (!localChildren.length) {
      const x = branchX + column * NODE_STEP_X;
      column++;
      xByField.set(skill.field, x);
      return x;
    }
    const childXs = localChildren.map(place);
    const x = childXs.reduce((sum, childX) => sum + childX, 0) / childXs.length;
    xByField.set(skill.field, x);
    return x;
  };

  for (const root of roots) {
    place(root);
  }
  for (const skill of branch.skills.sort(compareSkillPosition)) {
    place(skill);
  }
  return xByField;
}

function compareSkillPosition(a: SkillModel, b: SkillModel): number {
  return a.id - b.id || a.field.localeCompare(b.field);
}

function degreeTierY(degree: number, maxDegree: number): number {
  return DEGREE_START_Y + Math.max(0, maxDegree - Math.max(0, degree)) * NODE_STEP_Y;
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}
