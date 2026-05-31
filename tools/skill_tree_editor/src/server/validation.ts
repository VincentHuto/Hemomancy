import type { Diagnostic, SkillBranchFile, SkillModel } from '../shared/types';

export function validateSkillWorkspace(branches: SkillBranchFile[], translations: Record<string, string>): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];
  const allSkills = branches.flatMap(branch => branch.skills);
  const byField = new Map<string, SkillModel>();
  const ids = new Map<number, SkillModel>();
  const names = new Map<string, SkillModel>();

  for (const branch of branches) {
    diagnostics.push(...branch.diagnostics);
    if (!branch.skills.length) {
      diagnostics.push({
        severity: 'warning',
        code: 'empty_skill_branch',
        message: `Branch "${branch.branch}" has no editable skills.`,
        file: branch.path
      });
    }
    for (const skill of branch.skills) {
      const existingId = ids.get(skill.id);
      if (existingId) {
        diagnostics.push({
          severity: 'error',
          code: 'duplicate_skill_id',
          message: `Skill id ${skill.id} is used by both ${existingId.field} and ${skill.field}.`,
          file: branch.path,
          skill: skill.field
        });
      }
      ids.set(skill.id, skill);

      const existingName = names.get(skill.name);
      if (existingName) {
        diagnostics.push({
          severity: 'error',
          code: 'duplicate_skill_name',
          message: `Skill name "${skill.name}" is used by both ${existingName.field} and ${skill.field}.`,
          file: branch.path,
          skill: skill.field
        });
      }
      names.set(skill.name, skill);
      byField.set(skill.field, skill);

      if (!translations[`skill.hemomancy.${skill.name}.desc`] && !skill.description.trim()) {
        diagnostics.push({
          severity: 'warning',
          code: 'missing_skill_description',
          message: `Missing lang description skill.hemomancy.${skill.name}.desc.`,
          file: branch.path,
          skill: skill.field
        });
      }
    }
  }

  for (const branch of branches) {
    for (const skill of branch.skills) {
      if (skill.parentField && !byField.has(skill.parentField)) {
        diagnostics.push({
          severity: 'error',
          code: 'missing_parent_skill',
          message: `Parent skill ${skill.parentField} was not found.`,
          file: branch.path,
          skill: skill.field
        });
      }
      if (hasParentCycle(skill, byField)) {
        diagnostics.push({
          severity: 'error',
          code: 'skill_parent_cycle',
          message: `Skill ${skill.field} is part of a parent cycle.`,
          file: branch.path,
          skill: skill.field
        });
      }
    }
  }

  return diagnostics;
}

export function hasBlockingDiagnostics(diagnostics: Diagnostic[]): boolean {
  return diagnostics.some(diagnostic => diagnostic.severity === 'error');
}

function hasParentCycle(skill: SkillModel, byField: Map<string, SkillModel>): boolean {
  const seen = new Set<string>();
  let current: SkillModel | undefined = skill;
  while (current?.parentField) {
    if (seen.has(current.field)) return true;
    seen.add(current.field);
    current = byField.get(current.parentField);
  }
  return false;
}
