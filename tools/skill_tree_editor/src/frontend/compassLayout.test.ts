import type { SkillBranchFile } from '../shared/types';
import {
  applyCompassLayout,
  COMPASS_CENTER,
  compassPositionForSkill
} from './compassLayout';

test('places branches in their compass directions by degree ring', () => {
  const branches = [
    branch('core', [
      skill('base_skill', 0, null, 0),
      skill('skill_capacity', 1, 'base_skill', 0),
      skill('skill_last_wind', 3, 'skill_capacity', 2)
    ]),
    branch('living_staff', [
      skill('skill_living_conduit', 21, 'skill_capacity', 1)
    ]),
    branch('summons', [
      skill('skill_puppet_skein', 18, 'skill_capacity', 2)
    ]),
    branch('scars', [
      skill('skill_scar_affinity', 15, 'skill_last_wind', 4)
    ])
  ];

  const changes = applyCompassLayout(branches);
  const byField = Object.fromEntries(branches.flatMap(file => file.skills.map(item => [item.field, item])));

  expect(changes).toHaveLength(6);
  expect(byField.base_skill.treeX).toBe(COMPASS_CENTER.x);
  expect(byField.base_skill.treeY).toBe(COMPASS_CENTER.y);
  expect(byField.skill_last_wind.treeY).toBeLessThan(COMPASS_CENTER.y);
  expect(byField.skill_living_conduit.treeX).toBeLessThan(COMPASS_CENTER.x);
  expect(byField.skill_puppet_skein.treeX).toBeGreaterThan(COMPASS_CENTER.x);
  expect(byField.skill_scar_affinity.treeY).toBeGreaterThan(COMPASS_CENTER.y);
});

test('uses non-root degree zero fallback ring and degree radii outward from center', () => {
  const root = skill('base_skill', 0, null, 0);
  const degreeZero = skill('skill_capacity', 1, 'base_skill', 0);
  const degreeThree = skill('skill_iron_will', 10, 'skill_capacity', 3);

  expect(compassPositionForSkill(root, 0, 1)).toEqual({ x: 480, y: 480 });
  expect(compassPositionForSkill(degreeZero, 0, 1)).toEqual({ x: 480, y: 408 });
  expect(compassPositionForSkill(degreeThree, 0, 1)).toEqual({ x: 480, y: 220 });
});

test('offsets same-branch same-ring siblings along the branch tangent', () => {
  const branches = [
    branch('scars', [
      skill('skill_scar_affinity', 15, 'skill_crimson_mastery', 4),
      skill('skill_scar_resonance', 16, 'skill_scar_affinity', 4)
    ])
  ];

  applyCompassLayout(branches);
  const [first, second] = branches[0].skills;

  expect(first.treeY).toBe(810);
  expect(second.treeY).toBe(810);
  expect(second.treeX! - first.treeX!).toBe(82);
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
    iconItem: null,
    description: ''
  };
}
