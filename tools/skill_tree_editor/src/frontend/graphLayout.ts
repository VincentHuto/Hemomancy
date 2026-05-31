import type { SkillBranchFile, SkillModel } from '../shared/types';
import {
  COMPASS_CENTER,
  COMPASS_MAX_RING_RADIUS,
  compassGuideRadius,
  createCompassPositionMap
} from './compassLayout';

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
  cx: number;
  cy: number;
  radius: number;
  labelX: number;
  labelY: number;
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
const BRANCH_START_Y = 72;
const BRANCH_LABEL_OFFSET_Y = 28;
const NODE_WIDTH = 48;
const MIN_BRANCH_HEIGHT = 142;
const MIN_MAX_DEGREE = 5;
const CANVAS_PADDING = 160;

export function computeGraphLayout(branches: SkillBranchFile[]): GraphLayout {
  const skills = branches.flatMap(branch => branch.skills);
  const maxDegree = Math.max(MIN_MAX_DEGREE, ...skills.map(skill => skill.requiredDegree));
  const degreeGuides = Array.from({ length: maxDegree + 1 }, (_, degree) => ({
    degree,
    label: `Degree ${degree}`,
    cx: COMPASS_CENTER.x,
    cy: COMPASS_CENTER.y,
    radius: compassGuideRadius(degree),
    labelX: COMPASS_CENTER.x - compassGuideRadius(degree) + 10,
    labelY: COMPASS_CENTER.y - 8
  }));
  const maxGuideRadius = Math.max(...degreeGuides.map(guide => guide.radius));
  const fallbackPositions = createCompassPositionMap(branches);
  const nodes: GraphNodePosition[] = [];
  const labels: GraphBranchLabel[] = [];
  const bands: GraphBranchBand[] = [];
  let maxNodeX = 0;
  let maxNodeY = 0;

  for (const branch of branches) {
    const branchLabel = branchLabelPosition(branch.branch);
    const branchNodes = branch.skills.map((skill) => ({
      skill,
      x: skill.treeX ?? fallbackPositions.get(skill.field)?.x ?? COMPASS_CENTER.x,
      y: skill.treeY ?? fallbackPositions.get(skill.field)?.y ?? COMPASS_CENTER.y
    }));
    const localMinY = Math.min(BRANCH_START_Y, ...branchNodes.map(node => node.y - 44));
    const localMaxY = Math.max(BRANCH_START_Y, ...branchNodes.map(node => node.y + 72));
    const branchHeight = Math.max(MIN_BRANCH_HEIGHT, localMaxY - localMinY);

    labels.push({
      branch: branch.branch,
      text: labelize(branch.branch),
      x: branchLabel.x,
      y: branchLabel.y
    });
    bands.push({
      branch: branch.branch,
      y: localMinY,
      height: branchHeight
    });
    nodes.push(...branchNodes);
    maxNodeX = Math.max(maxNodeX, ...branchNodes.map(node => node.x));
    maxNodeY = Math.max(maxNodeY, ...branchNodes.map(node => node.y));
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
    height: Math.max(980, CANVAS_PADDING + Math.max(COMPASS_CENTER.y + maxGuideRadius, maxNodeY))
  };
}

function edgePath(parent: GraphNodePosition, child: GraphNodePosition, kind: GraphEdge['kind']): string {
  const fromX = Math.round(parent.x);
  const fromY = Math.round(parent.y);
  const toX = Math.round(child.x);
  const toY = Math.round(child.y);
  const distance = Math.hypot(toX - fromX, toY - fromY);
  const handle = Math.max(48, Math.min(150, distance * (kind === 'cross-branch' ? 0.42 : 0.34)));
  const fromRadial = radialVector(fromX, fromY, toX - fromX, toY - fromY);
  const toRadial = radialVector(toX, toY, toX - fromX, toY - fromY);
  const c1x = Math.round(fromX + fromRadial.x * handle);
  const c1y = Math.round(fromY + fromRadial.y * handle);
  const c2x = Math.round(toX - toRadial.x * handle);
  const c2y = Math.round(toY - toRadial.y * handle);
  return `M ${fromX} ${fromY} C ${c1x} ${c1y}, ${c2x} ${c2y}, ${toX} ${toY}`;
}

function radialVector(x: number, y: number, fallbackX: number, fallbackY: number): { x: number; y: number } {
  let dx = x - COMPASS_CENTER.x;
  let dy = y - COMPASS_CENTER.y;
  let length = Math.hypot(dx, dy);
  if (length < 0.001) {
    dx = fallbackX;
    dy = fallbackY;
    length = Math.hypot(dx, dy);
  }
  if (length < 0.001) return { x: 0, y: -1 };
  return { x: dx / length, y: dy / length };
}

function branchLabelPosition(branch: string): { x: number; y: number } {
  switch (branch) {
    case 'living_staff':
      return { x: Math.max(LEFT_LABEL_X, COMPASS_CENTER.x - COMPASS_MAX_RING_RADIUS - 84), y: COMPASS_CENTER.y };
    case 'summons':
      return { x: COMPASS_CENTER.x + COMPASS_MAX_RING_RADIUS + 34, y: COMPASS_CENTER.y };
    case 'scars':
      return { x: COMPASS_CENTER.x - 24, y: COMPASS_CENTER.y + COMPASS_MAX_RING_RADIUS + 52 };
    case 'core':
    case 'base':
    default:
      return { x: COMPASS_CENTER.x - 16, y: COMPASS_CENTER.y - COMPASS_MAX_RING_RADIUS - BRANCH_LABEL_OFFSET_Y };
  }
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}
