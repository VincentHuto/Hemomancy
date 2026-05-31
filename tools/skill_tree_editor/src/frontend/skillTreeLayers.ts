import type { SkillBranchFile, SkillModel } from '../shared/types';

export type SkillTreeWorkspaceMode = 'surface' | 'deep';

export const DEEP_ROOT_FIELD = '__deep_root__';

export function skillWorkspaceForDegree(requiredDegree: number): SkillTreeWorkspaceMode {
  return requiredDegree <= 4 ? 'surface' : 'deep';
}

export function isSkillInWorkspace(skill: SkillModel, workspace: SkillTreeWorkspaceMode | 'all' = 'all'): boolean {
  return workspace === 'all' || skillWorkspaceForDegree(skill.requiredDegree) === workspace;
}

export function degreeRangeForWorkspace(workspace: SkillTreeWorkspaceMode | 'all' = 'all'): number[] {
  if (workspace === 'surface') return [0, 1, 2, 3, 4];
  if (workspace === 'deep') return [5, 6, 7, 8];
  return [0, 1, 2, 3, 4, 5, 6, 7, 8];
}

export function filterBranchesForWorkspace(
  branches: SkillBranchFile[],
  workspace: SkillTreeWorkspaceMode | 'all' = 'all'
): SkillBranchFile[] {
  if (workspace === 'all') return branches;
  return branches
    .map(branch => ({
      ...branch,
      skills: branch.skills.filter(skill => isSkillInWorkspace(skill, workspace))
    }))
    .filter(branch => branch.skills.length > 0);
}
