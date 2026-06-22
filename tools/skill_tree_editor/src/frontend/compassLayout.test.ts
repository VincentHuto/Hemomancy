import type { SkillBranchFile, SkillModel } from '../shared/types';
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
    ]),
    branch('covenant', [
      skill('skill_fane_suture', 27, 'skill_blood_flow', 5)
    ]),
    branch('mycelial', [
      skill('skill_sporitic_attunement', 24, 'base_skill', 4)
    ])
  ];

  const changes = applyCompassLayout(branches);
  const byField = Object.fromEntries(branches.flatMap(file => file.skills.map(item => [item.field, item])));

  expect(changes).toHaveLength(8);
  expect(byField.base_skill.treeX).toBe(COMPASS_CENTER.x);
  expect(byField.base_skill.treeY).toBe(COMPASS_CENTER.y);
  expect(byField.skill_last_wind.treeY).toBeLessThan(COMPASS_CENTER.y);
  expect(byField.skill_living_conduit.treeX).toBeLessThan(COMPASS_CENTER.x);
  expect(byField.skill_puppet_skein.treeX).toBeGreaterThan(COMPASS_CENTER.x);
  expect(byField.skill_scar_affinity.treeY).toBeGreaterThan(COMPASS_CENTER.y);
  expect(byField.skill_fane_suture.treeX).toBeGreaterThan(COMPASS_CENTER.x);
  expect(byField.skill_fane_suture.treeY).toBeLessThan(COMPASS_CENTER.y);
  expect(byField.skill_sporitic_attunement.treeX).toBeLessThan(COMPASS_CENTER.x);
  expect(byField.skill_sporitic_attunement.treeY).toBeGreaterThan(COMPASS_CENTER.y);
});

test('uses non-root degree zero fallback ring and degree radii outward from center', () => {
  const root = skill('base_skill', 0, null, 0);
  const degreeZero = skill('skill_capacity', 1, 'base_skill', 0);
  const degreeThree = skill('skill_iron_will', 10, 'skill_capacity', 3);

  expect(compassPositionForSkill(root, 0, 1)).toEqual({ x: 480, y: 480 });
  expect(compassPositionForSkill(degreeZero, 0, 1)).toEqual({ x: 480, y: 408 });
  expect(compassPositionForSkill(degreeThree, 0, 1)).toEqual({ x: 480, y: 260 });
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

  expect(first.treeY).toBe(750);
  expect(second.treeY).toBe(750);
  expect(second.treeX! - first.treeX!).toBe(82);
});

test('surface compass layout only moves degree zero through four skills', () => {
  const branches = [
    branch('core', [
      { ...skill('base_skill', 0, null, 0), treeX: 111, treeY: 222 },
      { ...skill('skill_feeding_frenzy', 5, 'base_skill', 4), treeX: 333, treeY: 444 },
      { ...skill('skill_crimson_mastery', 8, 'skill_feeding_frenzy', 5), treeX: 777, treeY: 888 }
    ])
  ];

  const changes = applyCompassLayout(branches, { workspace: 'surface' });
  const byField = Object.fromEntries(branches[0].skills.map(item => [item.field, item]));

  expect(changes.map(change => change.field).sort()).toEqual(['base_skill', 'skill_feeding_frenzy']);
  expect(byField.skill_crimson_mastery.treeX).toBe(777);
  expect(byField.skill_crimson_mastery.treeY).toBe(888);
});

test('deep compass layout normalizes degree five through eight into its own rings', () => {
  const branches = [
    branch('living_staff', [
      skill('skill_hematic_focus', 25, 'skill_crimson_projection', 5),
      skill('skill_vespers_refusal', 26, 'skill_hematic_focus', 7)
    ])
  ];

  applyCompassLayout(branches, { workspace: 'deep' });
  const byField = Object.fromEntries(branches[0].skills.map(item => [item.field, item]));

  expect(byField.skill_hematic_focus.treeX).toBe(360);
  expect(byField.skill_hematic_focus.treeY).toBe(480);
  expect(byField.skill_vespers_refusal.treeX).toBe(260);
  expect(byField.skill_vespers_refusal.treeY).toBe(480);
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

function skill(field: string, id: number, parentField: string | null, requiredDegree: number): SkillModel {
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
