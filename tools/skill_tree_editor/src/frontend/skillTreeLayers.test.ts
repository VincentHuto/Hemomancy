import { DEEP_ROOT_FIELD, filterBranchesForWorkspace, skillWorkspaceForDegree } from './skillTreeLayers';
import { computeGraphLayout } from './graphLayout';
import type { SkillBranchFile } from '../shared/types';

test('assigns skills to surface and deep workspaces by required degree', () => {
  expect(skillWorkspaceForDegree(0)).toBe('surface');
  expect(skillWorkspaceForDegree(4)).toBe('surface');
  expect(skillWorkspaceForDegree(5)).toBe('deep');
  expect(skillWorkspaceForDegree(8)).toBe('deep');
});

test('filters branch files without changing branch metadata', () => {
  const branches = [
    branch('core', [
      skill('base_skill', 0, null, 0),
      skill('skill_feeding_frenzy', 5, 'base_skill', 4),
      skill('skill_crimson_mastery', 8, 'skill_feeding_frenzy', 5)
    ])
  ];

  const surface = filterBranchesForWorkspace(branches, 'surface');
  const deep = filterBranchesForWorkspace(branches, 'deep');

  expect(surface[0]).toEqual(expect.objectContaining({ branch: 'core', color: '#d00000' }));
  expect(surface[0].skills.map(item => item.field)).toEqual(['base_skill', 'skill_feeding_frenzy']);
  expect(deep[0]).toEqual(expect.objectContaining({ branch: 'core', color: '#d00000' }));
  expect(deep[0].skills.map(item => item.field)).toEqual(['skill_crimson_mastery']);
});

test('deep workspace renders cross-layer parents from a center anchor', () => {
  const layout = computeGraphLayout([
    branch('core', [
      skill('base_skill', 0, null, 0),
      skill('skill_feeding_frenzy', 5, 'base_skill', 4),
      skill('skill_crimson_mastery', 8, 'skill_feeding_frenzy', 5)
    ])
  ], { workspace: 'deep' });

  expect(layout.nodes.map(node => node.skill.field)).toEqual(['skill_crimson_mastery']);
  expect(layout.degreeGuides.map(guide => guide.degree)).toEqual([5, 6, 7, 8]);
  expect(layout.edges).toEqual([
    expect.objectContaining({
      fromField: DEEP_ROOT_FIELD,
      toField: 'skill_crimson_mastery',
      kind: 'cross-layer-anchor'
    })
  ]);
});

function branch(name: string, skills: ReturnType<typeof skill>[]): SkillBranchFile {
  return {
    path: `${name}.java`,
    branch: name,
    color: '#d00000',
    className: `${name}Branch`,
    source: '',
    diagnostics: [],
    skills: skills.map(item => ({ ...item, branch: name }))
  };
}

function skill(field: string, id: number, parentField: string | null, requiredDegree: number) {
  return {
    field,
    id,
    name: field === 'base_skill' ? 'base' : field,
    branch: 'core',
    bloodCost: 100,
    maxLevels: 1,
    state: 'LOCKED',
    parentField,
    parentFields: parentField ? [parentField] : [],
    skillPointCost: 1,
    requiredDegree,
    treeX: null,
    treeY: null,
    iconSource: null,
    iconItem: null,
    description: ''
  };
}
