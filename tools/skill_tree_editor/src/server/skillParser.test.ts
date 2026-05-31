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
      skillPointCost: 1,
      requiredDegree: 0,
      treeX: null,
      treeY: null,
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
      skillPointCost: 2,
      requiredDegree: 1,
      treeX: null,
      treeY: null,
      iconItem: 'vitality_chalice',
      description: ''
    }
  ]);
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

test('renders edited skills back into the marked branch section', () => {
  const parsed = parseSkillBranchJava('src/main/java/example/CoreSkillBranch.java', branchSource);
  parsed.skills[1] = { ...parsed.skills[1], bloodCost: 125, requiredDegree: 2, treeX: 360, treeY: 192 };

  const rendered = renderSkillBranchJava(branchSource, parsed);

  expect(rendered).toContain('// <skill-editor branch="core">');
  expect(rendered).toContain('new SkillPoint(1, "skill_capacity", 125, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)');
  expect(rendered).toContain('.setSkillPointCost(2).setRequiredDegree(2).setTreePosition(360, 192)');
  expect(rendered).toContain('// </skill-editor>');
});
