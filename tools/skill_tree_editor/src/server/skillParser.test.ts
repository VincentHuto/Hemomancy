import { parseSkillBranchJava, renderSkillBranchJava } from './skillParser';

const branchSource = `package example;

public final class CoreSkillBranch {
  public static void register(List<SkillPoint> branch) {
    // <skill-editor branch="core">
    SkillPointInit.base_skill = SkillPointInit.registerSkill(branch,
        new SkillPoint(0, "base", 0, 1, EnumSkillStates.UNLOCKED, null)
            .setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
    SkillPointInit.skill_capacity = SkillPointInit.registerSkill(branch,
        new SkillPoint(1, "skill_capacity", 100, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
            .setSkillPointCost(2).setRequiredDegree(1)
            .setIconItem(() -> new ItemStack(ItemInit.vitality_chalice.get())));
    // </skill-editor>
  }
}
`;

test('parses editable skill declarations from marked Java branches', () => {
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', branchSource);

  expect(parsed.branch).toBe('core');
  expect(parsed.color).toBe('#d00000');
  expect(parsed.className).toBe('CoreSkillBranch');
  expect(parsed.skills).toEqual([
    {
      field: 'base_skill',
      id: 0,
      name: 'base',
      branch: 'core',
      bloodCost: 0,
      maxLevels: 1,
      state: 'UNLOCKED',
      parentField: null,
      parentFields: [],
      skillPointCost: 1,
      requiredDegree: 0,
            treeX: null,
            treeY: null,
      iconSource: 'item',
      iconItem: 'sanguine_formation',
      description: ''
    },
    {
      field: 'skill_capacity',
      id: 1,
      name: 'skill_capacity',
      branch: 'core',
      bloodCost: 100,
      maxLevels: 5,
      state: 'LOCKED',
      parentField: 'base_skill',
      parentFields: ['base_skill'],
      skillPointCost: 2,
      requiredDegree: 1,
      treeX: null,
      treeY: null,
      iconSource: 'item',
      iconItem: 'vitality_chalice',
      description: ''
    }
  ]);
});

test('parses and renders block-backed icon items', () => {
  const source = branchSource.replace(
    'ItemInit.vitality_chalice.get()',
    'BlockInit.dendritic_distributor.get()'
  );
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', source);

  expect(parsed.skills[1]).toEqual(expect.objectContaining({
    iconSource: 'block',
    iconItem: 'dendritic_distributor'
  }));

  const rendered = renderSkillBranchJava(source, parsed);

  expect(rendered).toContain('.setIconItem(() -> new ItemStack(BlockInit.dendritic_distributor.get())));');
});

test('adds a BlockInit import when rendering a newly block-backed icon item', () => {
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', branchSource);
  parsed.skills[1] = {
    ...parsed.skills[1],
    iconSource: 'block',
    iconItem: 'dendritic_distributor'
  };

  const rendered = renderSkillBranchJava(branchSource, parsed);

  expect(rendered).toContain('import com.vincenthuto.hemomancy.common.init.BlockInit;');
  expect(rendered).toContain('.setIconItem(() -> new ItemStack(BlockInit.dendritic_distributor.get())));');
});

test('parses and renders branch trace colors', () => {
  const source = branchSource.replace(
    '.setSkillPointCost(2).setRequiredDegree(1)',
    '.setSkillPointCost(2).setRequiredDegree(1).setBranch("living_staff").setBranchColor(0xFFD9AD28)'
  );
  const parsed = parseSkillBranchJava('src/main/java/example/LivingStaffSkillBranch.java', source);

  expect(parsed.color).toBe('#d9ad28');

  parsed.color = '#ffd166';
  const rendered = renderSkillBranchJava(source, parsed);

  expect(rendered).toContain('.setBranch("core").setBranchColor(0xFFFFD166)');
});

test('parses and renders editable degree label positions', () => {
  const source = branchSource.replace(
    '// <skill-editor branch="core">',
    '// <skill-editor branch="core">\n    SkillPointInit.setDegreeLabelPosition(0, 96, 124);\n    SkillPointInit.setDegreeLabelPosition(1, 112, 156);'
  );
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', source);

  expect(parsed.degreeLabels).toEqual([
    { degree: 0, x: 96, y: 124 },
    { degree: 1, x: 112, y: 156 }
  ]);

  parsed.degreeLabels = [
    { degree: 0, x: 140, y: 180 },
    { degree: 1, x: 144, y: 214 }
  ];
  const rendered = renderSkillBranchJava(source, parsed);

  expect(rendered).toContain('SkillPointInit.setDegreeLabelPosition(0, 140, 180);');
  expect(rendered).toContain('SkillPointInit.setDegreeLabelPosition(1, 144, 214);');
});

test('parses explicit in-game tree positions from marked Java branches', () => {
  const source = branchSource.replace(
    '.setSkillPointCost(2).setRequiredDegree(1)',
    '.setSkillPointCost(2).setRequiredDegree(1).setTreePosition(340, 176)'
  );

  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', source);

  expect(parsed.skills[1]).toEqual(expect.objectContaining({
    field: 'skill_capacity',
    treeX: 340,
    treeY: 176
  }));
});

test('parses and renders additional parent requirements', () => {
  const source = branchSource.replace(
    '.setSkillPointCost(2).setRequiredDegree(1)',
    '.setSkillPointCost(2).setRequiredDegree(1).addParents(SkillPointInit.skill_efficiency)'
  );
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', source);

  expect(parsed.skills[1]).toEqual(expect.objectContaining({
    parentField: 'base_skill',
    parentFields: ['base_skill', 'skill_efficiency']
  }));

  parsed.skills[1] = {
    ...parsed.skills[1],
    parentFields: ['base_skill', 'skill_efficiency', 'skill_last_wind']
  };
  const rendered = renderSkillBranchJava(source, parsed);

  expect(rendered).toContain('new SkillPoint(1, "skill_capacity", 100, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)');
  expect(rendered).toContain('.addParents(SkillPointInit.skill_efficiency, SkillPointInit.skill_last_wind)');
});

test('renders edited skills back into the marked branch section', () => {
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', branchSource);
  parsed.skills[1] = { ...parsed.skills[1], bloodCost: 125, requiredDegree: 2, treeX: 360, treeY: 192 };

  const rendered = renderSkillBranchJava(branchSource, parsed);

  expect(rendered).toContain('// <skill-editor branch="core">');
  expect(rendered).toContain('new SkillPoint(1, "skill_capacity", 125, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)');
  expect(rendered).toContain('.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(360, 192)');
  expect(rendered).toContain('// </skill-editor>');
});
