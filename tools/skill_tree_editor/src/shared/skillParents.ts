import type { SkillModel } from './types';

export function parentFieldsOf(skill: Pick<SkillModel, 'field' | 'parentField' | 'parentFields'>): string[] {
  return normalizeParentFields(skill.parentFields, skill.parentField, skill.field);
}

export function setParentFields(skill: Pick<SkillModel, 'field' | 'parentField' | 'parentFields'>, parents: readonly string[]): void {
  const normalized = normalizeParentFields(parents, null, skill.field);
  skill.parentFields = normalized;
  skill.parentField = normalized[0] ?? null;
}

export function addParentField(skill: Pick<SkillModel, 'field' | 'parentField' | 'parentFields'>, parentField: string): void {
  setParentFields(skill, [...parentFieldsOf(skill), parentField]);
}

export function removeParentField(skill: Pick<SkillModel, 'field' | 'parentField' | 'parentFields'>, parentField: string): void {
  setParentFields(skill, parentFieldsOf(skill).filter(candidate => candidate !== parentField));
}

export function normalizeParentFields(
  parentFields: readonly string[] | undefined,
  parentField: string | null | undefined,
  selfField?: string
): string[] {
  const values = parentFields && parentFields.length ? parentFields : parentField ? [parentField] : [];
  const seen = new Set<string>();
  const normalized: string[] = [];
  for (const value of values) {
    const trimmed = value.trim();
    if (!trimmed || trimmed === selfField || seen.has(trimmed)) continue;
    seen.add(trimmed);
    normalized.push(trimmed);
  }
  return normalized;
}
