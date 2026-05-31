import type { SkillBranchFile } from '../shared/types';
import { computeGraphLayout } from './graphLayout';

test('spreads fallback branches into compass directions around the center', () => {
  const layout = computeGraphLayout([
    branch('core', 15),
    branch('living_staff', 3),
    branch('scars', 3),
    branch('summons', 3)
  ]);
  const core = layout.nodes.find(node => node.skill.branch === 'core')!;
  const staff = layout.nodes.find(node => node.skill.branch === 'living_staff')!;
  const scars = layout.nodes.find(node => node.skill.branch === 'scars')!;
  const summons = layout.nodes.find(node => node.skill.branch === 'summons')!;

  expect(layout.labels.map(label => label.branch)).toEqual(['core', 'living_staff', 'scars', 'summons']);
  expect(core).toEqual(expect.objectContaining({ x: 480, y: 480 }));
  expect(staff.x).toBeLessThan(core.x);
  expect(scars.y).toBeGreaterThan(core.y);
  expect(summons.x).toBeGreaterThan(core.x);
});

test('keeps long root edges out of unrelated sibling skill rows', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        skill('base_skill', 'base', 0, null),
        skill('skill_capacity', 'skill_capacity', 1, 'base_skill'),
        skill('skill_last_wind', 'skill_last_wind', 3, 'skill_capacity'),
        skill('skill_feeding_frenzy', 'skill_feeding_frenzy', 5, 'skill_last_wind'),
        skill('skill_efficiency', 'skill_efficiency', 2, 'base_skill'),
        skill('skill_dynamic_use', 'skill_dynamic_use', 4, 'skill_efficiency'),
        skill('skill_hemostasis', 'skill_hemostasis', 6, 'skill_efficiency'),
        skill('skill_manip_slots', 'skill_manip_slots', 14, 'base_skill')
      ]
    }
  ]);
  const y = Object.fromEntries(layout.nodes.map(node => [node.skill.field, node.y]));

  expect(isBetween(y.skill_last_wind, y.base_skill, y.skill_manip_slots)).toBe(false);
});

test('marks edges that cross between branch bands', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('skill_manip_slots', 'skill_manip_slots', 14, null, 'core')]
    },
    {
      path: 'LivingStaffSkillBranch.java',
      branch: 'living_staff',
      className: 'LivingStaffSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('skill_living_conduit', 'skill_living_conduit', 21, 'skill_manip_slots', 'living_staff')]
    }
  ]);

  expect(layout.edges).toEqual([
    expect.objectContaining({
      fromField: 'skill_manip_slots',
      toField: 'skill_living_conduit',
      kind: 'cross-branch'
    })
  ]);
});

test('records destination branch on each edge for branch-colored traces', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('base_skill', 'base', 0, null, 'core')]
    },
    {
      path: 'ScarSkillBranch.java',
      branch: 'scars',
      className: 'ScarSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('skill_scar_affinity', 'skill_scar_affinity', 15, 'base_skill', 'scars')]
    }
  ]);

  expect(layout.edges[0]).toEqual(expect.objectContaining({
    fromField: 'base_skill',
    toField: 'skill_scar_affinity',
    toBranch: 'scars'
  }));
});

test('positions fallback nodes on concentric degree rings with higher core degrees farther north', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        skill('base_skill', 'base', 0, null, 'core', 0),
        skill('skill_last_wind', 'skill_last_wind', 3, 'base_skill', 'core', 2),
        skill('skill_vital_link', 'skill_vital_link', 9, 'skill_last_wind', 'core', 5)
      ]
    }
  ]);
  const guides = Object.fromEntries(layout.degreeGuides.map(guide => [guide.degree, guide]));
  const node = Object.fromEntries(layout.nodes.map(item => [item.skill.field, item]));

  expect(layout.degreeGuides.map(guide => guide.degree)).toEqual([0, 1, 2, 3, 4, 5]);
  expect(guides[0]).toEqual(expect.objectContaining({ cx: 480, cy: 480, radius: 72 }));
  expect(guides[5]).toEqual(expect.objectContaining({ cx: 480, cy: 480, radius: 400 }));
  expect(node.base_skill).toEqual(expect.objectContaining({ x: 480, y: 480 }));
  expect(node.skill_last_wind).toEqual(expect.objectContaining({ x: 480, y: 290 }));
  expect(node.skill_vital_link).toEqual(expect.objectContaining({ x: 480, y: 80 }));
  expect(node.skill_vital_link.y).toBeLessThan(node.skill_last_wind.y);
});

test('uses explicit tree positions when skills define in-game coordinates', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        { ...skill('base_skill', 'base', 0, null, 'core', 0), treeX: 512, treeY: 384 },
        { ...skill('skill_capacity', 'skill_capacity', 1, 'base_skill', 'core', 1), treeX: 512, treeY: 480 }
      ]
    }
  ]);

  expect(layout.nodes.find(node => node.skill.field === 'base_skill')).toEqual(expect.objectContaining({
    x: 512,
    y: 384
  }));
  expect(layout.nodes.find(node => node.skill.field === 'skill_capacity')).toEqual(expect.objectContaining({
    x: 512,
    y: 480
  }));
});

test('draws skill links as game-style curved connector paths', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        { ...skill('base_skill', 'base', 0, null, 'core', 0), treeX: 300, treeY: 100 },
        { ...skill('skill_capacity', 'skill_capacity', 1, 'base_skill', 'core', 1), treeX: 420, treeY: 220 }
      ]
    }
  ]);

  expect(layout.edges[0].path).toMatch(/^M \d+ \d+ C /);
  expect(layout.edges[0].path).toContain(' C ');
});

function branch(name: string, count: number): SkillBranchFile {
  const skills = Array.from({ length: count }, (_, index) => ({
    field: `${name}_${index}`,
    id: index,
    name: `${name}_${index}`,
    branch: name,
    bloodCost: 100,
    maxLevels: 1,
    state: 'LOCKED',
    parentField: index === 0 ? null : `${name}_${index - 1}`,
    skillPointCost: 1,
    requiredDegree: 0,
    treeX: null,
    treeY: null,
    iconItem: null,
    description: ''
  }));
  return {
    path: `${name}.java`,
    branch: name,
    className: `${name}Branch`,
    source: '',
    skills,
    diagnostics: []
  };
}

function skill(field: string, name: string, id: number, parentField: string | null, branch = 'core', requiredDegree = 0) {
  return {
    field,
    id,
    name,
    branch,
    bloodCost: 100,
    maxLevels: 1,
    state: 'LOCKED',
    parentField,
    skillPointCost: 1,
    requiredDegree,
    treeX: null,
    treeY: null,
    iconItem: null,
    description: ''
  };
}

function isBetween(value: number, a: number, b: number): boolean {
  return value > Math.min(a, b) && value < Math.max(a, b);
}
