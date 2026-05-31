import type { SkillBranchFile, SkillModel } from '../shared/types';
import { normalizeBranchColor } from '../shared/branchColors';
import { parentFieldsOf } from '../shared/skillParents';
import {
  COMPASS_CENTER,
  COMPASS_MAX_RING_RADIUS,
  compassGuideRadius,
  createCompassPositionMap
} from './compassLayout';
import {
  DEEP_ROOT_FIELD,
  degreeRangeForWorkspace,
  filterBranchesForWorkspace,
  type SkillTreeWorkspaceMode
} from './skillTreeLayers';

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
  color: string;
  kind: 'local' | 'cross-branch' | 'cross-layer-anchor';
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
const MIN_MAX_DEGREE = 8;
const CANVAS_PADDING = 160;
const EDGE_NODE_TRIM_RADIUS = 16;
const EDGE_ORGANIC_SWAY_MIN = 8;
const EDGE_ORGANIC_SWAY_MAX = 18;
const EDGE_ORGANIC_SWAY_FACTOR = 0.08;

export interface GraphLayoutOptions {
  workspace?: SkillTreeWorkspaceMode | 'all';
}

export function computeGraphLayout(branches: SkillBranchFile[], options: GraphLayoutOptions = {}): GraphLayout {
  const workspace = options.workspace ?? 'all';
  const visibleBranches = filterBranchesForWorkspace(branches, workspace);
  const skills = visibleBranches.flatMap(branch => branch.skills);
  const branchColors = new Map(branches.map(branch => [branch.branch, normalizeBranchColor(branch.color, branch.branch)]));
  const degreeLabelPositions = new Map(branches
    .flatMap(branch => branch.degreeLabels ?? [])
    .map(label => [label.degree, { x: label.x, y: label.y }]));
  const degrees = workspace === 'all'
    ? Array.from({ length: Math.max(MIN_MAX_DEGREE, ...skills.map(skill => skill.requiredDegree)) + 1 }, (_, degree) => degree)
    : degreeRangeForWorkspace(workspace);
  const degreeGuides = degrees.map((degree) => {
    const label = degreeLabelPositions.get(degree) ?? degreeGuideLabelPosition(degree);
    return {
      degree,
      label: `Degree ${degree}`,
      cx: COMPASS_CENTER.x,
      cy: COMPASS_CENTER.y,
      radius: compassGuideRadius(degree, workspace),
      labelX: label.x,
      labelY: label.y
    };
  });
  const maxGuideRadius = Math.max(...degreeGuides.map(guide => guide.radius));
  const fallbackPositions = createCompassPositionMap(branches, { workspace });
  const nodes: GraphNodePosition[] = [];
  const labels: GraphBranchLabel[] = [];
  const bands: GraphBranchBand[] = [];
  let maxNodeX = 0;
  let maxNodeY = 0;

  for (const branch of visibleBranches) {
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
    .flatMap(node => parentFieldsOf(node.skill)
      .flatMap(parentField => {
        const parent = byPosition.get(parentField);
        if (!parent && workspace !== 'deep') return [];
        const visualParent = parent ?? {
          skill: node.skill,
          x: COMPASS_CENTER.x,
          y: COMPASS_CENTER.y
        };
        const kind: GraphEdge['kind'] = parent
          ? (parent.skill.branch === node.skill.branch ? 'local' : 'cross-branch')
          : 'cross-layer-anchor';
        return {
          fromField: parent?.skill.field ?? DEEP_ROOT_FIELD,
          toField: node.skill.field,
          toBranch: node.skill.branch,
          color: branchColors.get(node.skill.branch) ?? normalizeBranchColor(null, node.skill.branch),
          kind,
          path: edgePath(visualParent, node, kind)
        };
      }));

  return {
    nodes,
    labels,
    bands,
    edges,
    degreeGuides,
    width: Math.max(980, CANVAS_PADDING + Math.max(maxNodeX + NODE_WIDTH, ...degreeGuides.map(guide => guide.labelX))),
    height: Math.max(980, CANVAS_PADDING + Math.max(COMPASS_CENTER.y + maxGuideRadius, maxNodeY, ...degreeGuides.map(guide => guide.labelY)))
  };
}

function edgePath(parent: GraphNodePosition, child: GraphNodePosition, kind: GraphEdge['kind']): string {
  const trimmed = trimEdgeEndpoints(parent, child);
  const fromX = Math.round(trimmed.from.x);
  const fromY = Math.round(trimmed.from.y);
  const toX = Math.round(trimmed.to.x);
  const toY = Math.round(trimmed.to.y);
  const distance = Math.hypot(toX - fromX, toY - fromY);
  const handle = Math.max(48, Math.min(150, distance * (kind === 'cross-branch' ? 0.42 : 0.34)));
  const fromRadial = radialVector(fromX, fromY, toX - fromX, toY - fromY);
  const toRadial = radialVector(toX, toY, toX - fromX, toY - fromY);
  const sway = organicSway(fromX, fromY, toX, toY);
  const c1x = Math.round(fromX + fromRadial.x * handle + sway.x);
  const c1y = Math.round(fromY + fromRadial.y * handle + sway.y);
  const c2x = Math.round(toX - toRadial.x * handle - sway.x);
  const c2y = Math.round(toY - toRadial.y * handle - sway.y);
  return `M ${fromX} ${fromY} C ${c1x} ${c1y}, ${c2x} ${c2y}, ${toX} ${toY}`;
}

function trimEdgeEndpoints(parent: GraphNodePosition, child: GraphNodePosition): { from: { x: number; y: number }; to: { x: number; y: number } } {
  const dx = child.x - parent.x;
  const dy = child.y - parent.y;
  const distance = Math.hypot(dx, dy);
  if (distance < 0.001) {
    return {
      from: { x: parent.x, y: parent.y },
      to: { x: child.x, y: child.y }
    };
  }
  const offset = Math.min(EDGE_NODE_TRIM_RADIUS, Math.max(0, distance / 2 - 1));
  const nx = dx / distance;
  const ny = dy / distance;
  return {
    from: { x: parent.x + nx * offset, y: parent.y + ny * offset },
    to: { x: child.x - nx * offset, y: child.y - ny * offset }
  };
}

function degreeGuideLabelPosition(degree: number): { x: number; y: number } {
  return {
    x: COMPASS_CENTER.x - COMPASS_MAX_RING_RADIUS + 10,
    y: COMPASS_CENTER.y - COMPASS_MAX_RING_RADIUS + 28 + degree * 34
  };
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

function organicSway(fromX: number, fromY: number, toX: number, toY: number): { x: number; y: number } {
  const dx = toX - fromX;
  const dy = toY - fromY;
  const distance = Math.hypot(dx, dy);
  if (distance < 0.001) return { x: 0, y: 0 };
  const amount = Math.min(EDGE_ORGANIC_SWAY_MAX, Math.max(EDGE_ORGANIC_SWAY_MIN, distance * EDGE_ORGANIC_SWAY_FACTOR));
  const sign = organicSwaySign(fromX, fromY, toX, toY);
  return {
    x: -dy / distance * amount * sign,
    y: dx / distance * amount * sign
  };
}

function organicSwaySign(fromX: number, fromY: number, toX: number, toY: number): number {
  const hash = Math.trunc(fromX * 31 + fromY * 17 + toX * 13 + toY * 7);
  return (hash & 1) === 0 ? 1 : -1;
}

function branchLabelPosition(branch: string): { x: number; y: number } {
  switch (branch) {
    case 'living_staff':
      return { x: Math.max(LEFT_LABEL_X, COMPASS_CENTER.x - COMPASS_MAX_RING_RADIUS - 84), y: COMPASS_CENTER.y };
    case 'summons':
      return { x: COMPASS_CENTER.x + COMPASS_MAX_RING_RADIUS + 34, y: COMPASS_CENTER.y };
    case 'scars':
      return { x: COMPASS_CENTER.x - 24, y: COMPASS_CENTER.y + COMPASS_MAX_RING_RADIUS + 52 };
    case 'covenant':
      return {
        x: COMPASS_CENTER.x + Math.round(COMPASS_MAX_RING_RADIUS * Math.SQRT1_2) + 32,
        y: COMPASS_CENTER.y - Math.round(COMPASS_MAX_RING_RADIUS * Math.SQRT1_2) - 14
      };
    case 'mycelial':
      return {
        x: Math.max(LEFT_LABEL_X, COMPASS_CENTER.x - Math.round(COMPASS_MAX_RING_RADIUS * Math.SQRT1_2) - 120),
        y: COMPASS_CENTER.y + Math.round(COMPASS_MAX_RING_RADIUS * Math.SQRT1_2) + 36
      };
    case 'core':
    case 'base':
    default:
      return { x: COMPASS_CENTER.x - 16, y: COMPASS_CENTER.y - COMPASS_MAX_RING_RADIUS - BRANCH_LABEL_OFFSET_Y };
  }
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}
