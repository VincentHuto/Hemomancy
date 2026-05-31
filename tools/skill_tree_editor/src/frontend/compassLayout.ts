import type { SkillBranchFile, SkillModel } from '../shared/types';
import type { NodePosition } from './layoutEditing';
import type { MovementChange } from './movementHistory';
import { isSkillInWorkspace, type SkillTreeWorkspaceMode } from './skillTreeLayers';

export const COMPASS_CENTER: NodePosition = { x: 480, y: 480 };
export const COMPASS_TANGENT_SPACING = 82;
export const COMPASS_MAX_RING_RADIUS = 470;

const NON_ROOT_DEGREE_ZERO_RADIUS = 72;
const DEGREE_RADII = new Map<number, number>([
  [1, 120],
  [2, 170],
  [3, 220],
  [4, 270],
  [5, 320],
  [6, 370],
  [7, 420],
  [8, 470]
]);
const DEEP_DEGREE_RADII = new Map<number, number>([
  [5, 120],
  [6, 170],
  [7, 220],
  [8, 270]
]);

export interface CompassLayoutOptions {
  workspace?: SkillTreeWorkspaceMode | 'all';
}

interface BranchCompass {
  dx: number;
  dy: number;
  tx: number;
  ty: number;
}

const BRANCH_COMPASS: Record<string, BranchCompass> = {
  core: { dx: 0, dy: -1, tx: 1, ty: 0 },
  base: { dx: 0, dy: -1, tx: 1, ty: 0 },
  living_staff: { dx: -1, dy: 0, tx: 0, ty: -1 },
  summons: { dx: 1, dy: 0, tx: 0, ty: 1 },
  scars: { dx: 0, dy: 1, tx: 1, ty: 0 },
  covenant: { dx: Math.SQRT1_2, dy: -Math.SQRT1_2, tx: Math.SQRT1_2, ty: Math.SQRT1_2 },
  mycelial: { dx: -Math.SQRT1_2, dy: Math.SQRT1_2, tx: Math.SQRT1_2, ty: Math.SQRT1_2 }
};

export function applyCompassLayout(branches: SkillBranchFile[], options: CompassLayoutOptions = {}): MovementChange[] {
  const changes = planCompassLayout(branches, options);
  const byField = new Map(branches.flatMap(branch => branch.skills.map(skill => [skill.field, skill] as const)));

  for (const change of changes) {
    const skill = byField.get(change.field);
    if (!skill) continue;
    skill.treeX = change.after.x;
    skill.treeY = change.after.y;
  }

  return changes;
}

export function planCompassLayout(branches: SkillBranchFile[], options: CompassLayoutOptions = {}): MovementChange[] {
  const fallbackPositions = createCompassPositionMap(branches, options);
  const changes: MovementChange[] = [];
  const workspace = options.workspace ?? 'all';

  for (const branch of branches) {
    for (const skill of branch.skills) {
      if (!isSkillInWorkspace(skill, workspace)) continue;
      const after = fallbackPositions.get(skill.field);
      if (!after) continue;
      const before = {
        x: skill.treeX ?? after.x,
        y: skill.treeY ?? after.y
      };
      const wasImplicit = skill.treeX === null || skill.treeY === null;
      if (!wasImplicit && before.x === after.x && before.y === after.y) continue;
      changes.push({
        field: skill.field,
        before,
        after
      });
    }
  }

  return changes;
}

export function createCompassPositionMap(branches: SkillBranchFile[], options: CompassLayoutOptions = {}): Map<string, NodePosition> {
  const groups = new Map<string, { branch: string; radius: number; skills: SkillModel[] }>();
  const workspace = options.workspace ?? 'all';

  for (const branch of branches) {
    for (const skill of branch.skills) {
      if (!isSkillInWorkspace(skill, workspace)) continue;
      const radius = compassRadiusForSkill(skill, workspace);
      const key = `${branch.branch}:${radius}`;
      const group = groups.get(key) ?? { branch: branch.branch, radius, skills: [] };
      group.skills.push(skill);
      groups.set(key, group);
    }
  }

  const positions = new Map<string, NodePosition>();
  for (const group of groups.values()) {
    group.skills.sort((left, right) => compareCompassOrder(group.branch, left, right));
    group.skills.forEach((skill, index) => {
      positions.set(skill.field, compassPositionForSkill(skill, index, group.skills.length, group.branch, workspace));
    });
  }

  return positions;
}

export function compassPositionForSkill(
  skill: SkillModel,
  siblingIndex: number,
  siblingCount: number,
  branchName = skill.branch,
  workspace: SkillTreeWorkspaceMode | 'all' = 'all'
): NodePosition {
  const compass = compassForBranch(branchName);
  const radius = compassRadiusForSkill(skill, workspace);
  const siblingOffset = (siblingIndex - (Math.max(1, siblingCount) - 1) / 2) * COMPASS_TANGENT_SPACING;
  return {
    x: Math.round(COMPASS_CENTER.x + compass.dx * radius + compass.tx * siblingOffset),
    y: Math.round(COMPASS_CENTER.y + compass.dy * radius + compass.ty * siblingOffset)
  };
}

export function compassGuideRadius(degree: number, workspace: SkillTreeWorkspaceMode | 'all' = 'all'): number {
  if (workspace === 'deep') return DEEP_DEGREE_RADII.get(degree) ?? DEEP_DEGREE_RADII.get(8)! + Math.max(0, degree - 8) * 50;
  if (degree <= 0) return NON_ROOT_DEGREE_ZERO_RADIUS;
  return DEGREE_RADII.get(degree) ?? DEGREE_RADII.get(8)! + (degree - 8) * 50;
}

function compassRadiusForSkill(skill: SkillModel, workspace: SkillTreeWorkspaceMode | 'all'): number {
  if (!skill.parentField && skill.requiredDegree === 0 && (skill.branch === 'core' || skill.branch === 'base')) return 0;
  if (skill.field === 'deep_base_skill' || skill.name === 'deep_base') return 0;
  return compassGuideRadius(skill.requiredDegree, workspace);
}

function compareCompassOrder(branchName: string, left: SkillModel, right: SkillModel): number {
  const compass = compassForBranch(branchName);
  const leftProjection = projection(left, compass);
  const rightProjection = projection(right, compass);
  return leftProjection - rightProjection || left.id - right.id || left.field.localeCompare(right.field);
}

function projection(skill: SkillModel, compass: BranchCompass): number {
  const x = skill.treeX ?? COMPASS_CENTER.x;
  const y = skill.treeY ?? COMPASS_CENTER.y;
  return x * compass.tx + y * compass.ty;
}

function compassForBranch(branchName: string): BranchCompass {
  return BRANCH_COMPASS[branchName] ?? BRANCH_COMPASS.core;
}
