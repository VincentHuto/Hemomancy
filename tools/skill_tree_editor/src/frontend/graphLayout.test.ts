import type { SkillBranchFile } from '../shared/types';
import { defaultBranchColor } from '../shared/branchColors';
import { computeGraphLayout } from './graphLayout';

test('spreads fallback branches into compass directions around the center', () => {
  const layout = computeGraphLayout([
    branch('core', 15),
    branch('living_staff', 3),
    branch('scars', 3),
    branch('summons', 3),
    branch('covenant', 4),
    branch('mycelial', 4)
  ]);
  const core = branchCenter(layout.nodes, 'core');
  const staff = branchCenter(layout.nodes, 'living_staff');
  const scars = branchCenter(layout.nodes, 'scars');
  const summons = branchCenter(layout.nodes, 'summons');
  const covenant = branchCenter(layout.nodes, 'covenant');
  const mycelial = branchCenter(layout.nodes, 'mycelial');
  const root = layout.nodes.find(node => node.skill.field === 'core_0')!;

  expect(layout.labels.map(label => label.branch)).toEqual([
    'core',
    'living_staff',
    'scars',
    'summons',
    'covenant',
    'mycelial'
  ]);
  expect(root).toEqual(expect.objectContaining({ x: 480, y: 480 }));
  expect(core.y).toBeLessThan(480);
  expect(staff.x).toBeLessThan(480);
  expect(scars.y).toBeGreaterThan(480);
  expect(summons.x).toBeGreaterThan(480);
  expect(covenant.x).toBeGreaterThan(480);
  expect(covenant.y).toBeLessThan(480);
  expect(mycelial.x).toBeLessThan(480);
  expect(mycelial.y).toBeGreaterThan(480);
});

test('keeps long root edges out of unrelated sibling skill rows', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
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
      color: '#d00000',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('skill_manip_slots', 'skill_manip_slots', 14, null, 'core')]
    },
    {
      path: 'LivingStaffSkillBranch.java',
      branch: 'living_staff',
      color: '#d9ad28',
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
      color: '#d00000',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('base_skill', 'base', 0, null, 'core')]
    },
    {
      path: 'ScarSkillBranch.java',
      branch: 'scars',
      color: '#b6b6bd',
      className: 'ScarSkillBranch',
      source: '',
      diagnostics: [],
      skills: [skill('skill_scar_affinity', 'skill_scar_affinity', 15, 'base_skill', 'scars')]
    }
  ]);

  expect(layout.edges[0]).toEqual(expect.objectContaining({
    fromField: 'base_skill',
    toField: 'skill_scar_affinity',
    toBranch: 'scars',
    color: '#b6b6bd'
  }));
});

test('uses default colors for covenant and mycelial branches', () => {
  expect(defaultBranchColor('covenant')).toBe('#a54569');
  expect(defaultBranchColor('mycelial')).toBe('#6e8f3a');
});

test('draws one connector for each parent requirement', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        skill('base_skill', 'base', 0, null, 'core'),
        skill('skill_capacity', 'skill_capacity', 1, 'base_skill', 'core'),
        {
          ...skill('skill_efficiency', 'skill_efficiency', 2, 'base_skill', 'core'),
          parentFields: ['base_skill', 'skill_capacity']
        }
      ]
    }
  ]);

  expect(layout.edges.filter(edge => edge.toField === 'skill_efficiency').map(edge => edge.fromField)).toEqual([
    'base_skill',
    'skill_capacity'
  ]);
});

test('positions fallback nodes on concentric degree rings with higher core degrees farther north', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
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

  expect(layout.degreeGuides.map(guide => guide.degree)).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8]);
  expect(guides[0]).toEqual(expect.objectContaining({ cx: 480, cy: 480, radius: 72 }));
  expect(guides[8]).toEqual(expect.objectContaining({ cx: 480, cy: 480, radius: 470 }));
  expect(node.base_skill).toEqual(expect.objectContaining({ x: 480, y: 480 }));
  expect(node.skill_last_wind).toEqual(expect.objectContaining({ x: 480, y: 310 }));
  expect(node.skill_vital_link).toEqual(expect.objectContaining({ x: 480, y: 160 }));
  expect(node.skill_vital_link.y).toBeLessThan(node.skill_last_wind.y);
});

test('stacks degree guide labels away from the center tree nodes', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
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

  const labelYs = layout.degreeGuides.map(guide => guide.labelY);

  expect(new Set(labelYs).size).toBe(layout.degreeGuides.length);
  expect(layout.degreeGuides.every(guide => guide.labelX < 160)).toBe(true);
});

test('uses saved degree guide label positions when present', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
      degreeLabels: [{ degree: 3, x: 212, y: 344 }],
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        skill('base_skill', 'base', 0, null, 'core', 0),
        skill('skill_feeding_frenzy', 'skill_feeding_frenzy', 5, 'base_skill', 'core', 3)
      ]
    }
  ]);

  expect(layout.degreeGuides.find(guide => guide.degree === 3)).toEqual(expect.objectContaining({
    labelX: 212,
    labelY: 344
  }));
});

test('uses explicit tree positions when skills define in-game coordinates', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
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
      color: '#d00000',
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

test('tucks connector endpoints under node frames instead of stopping in open space', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        { ...skill('base_skill', 'base', 0, null, 'core', 0), treeX: 480, treeY: 360 },
        { ...skill('skill_capacity', 'skill_capacity', 1, 'base_skill', 'core', 1), treeX: 480, treeY: 480 }
      ]
    }
  ]);

  const points = pathPoints(layout.edges[0].path);

  expect(points.start).toEqual({ x: 480, y: 376 });
  expect(points.end).toEqual({ x: 480, y: 464 });
});

test('adds a subtle organic sway to straight connector paths', () => {
  const layout = computeGraphLayout([
    {
      path: 'CoreSkillBranch.java',
      branch: 'core',
      color: '#d00000',
      className: 'CoreSkillBranch',
      source: '',
      diagnostics: [],
      skills: [
        { ...skill('base_skill', 'base', 0, null, 'core', 0), treeX: 480, treeY: 360 },
        { ...skill('skill_capacity', 'skill_capacity', 1, 'base_skill', 'core', 1), treeX: 480, treeY: 480 }
      ]
    }
  ]);

  const numbers = layout.edges[0].path.match(/-?\d+/g)?.map(Number) ?? [];
  const [startX, , c1x, , c2x, , endX] = numbers;

  expect(startX).toBe(480);
  expect(endX).toBe(480);
  expect(c1x).not.toBe(480);
  expect(c2x).not.toBe(480);
  expect(Math.sign(c1x - 480)).toBe(-Math.sign(c2x - 480));
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
    parentFields: index === 0 ? [] : [`${name}_${index - 1}`],
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
    color: '#d00000',
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
    parentFields: parentField ? [parentField] : [],
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

function branchCenter(nodes: { skill: { branch: string }; x: number; y: number }[], branch: string) {
  const branchNodes = nodes.filter(node => node.skill.branch === branch);
  return {
    x: Math.round(branchNodes.reduce((sum, node) => sum + node.x, 0) / branchNodes.length),
    y: Math.round(branchNodes.reduce((sum, node) => sum + node.y, 0) / branchNodes.length)
  };
}

function pathPoints(path: string): { start: { x: number; y: number }; end: { x: number; y: number } } {
  const numbers = path.match(/-?\d+/g)?.map(Number) ?? [];
  return {
    start: { x: numbers[0], y: numbers[1] },
    end: { x: numbers[numbers.length - 2], y: numbers[numbers.length - 1] }
  };
}
