import { mkdtempSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { applyPreview, loadWorkspace, previewWorkspaceChanges } from './workspace';

const branchSource = `package example;
public final class LivingStaffSkillBranch {
  public static void register(List<SkillPoint> branch) {
    // <skill-editor branch="living_staff">
    SkillPointInit.skill_living_conduit = SkillPointInit.registerSkill(branch,
        new SkillPoint(21, "skill_living_conduit", 250, 3, EnumSkillStates.LOCKED, SkillPointInit.skill_manip_slots)
            .setSkillPointCost(2).setRequiredDegree(1)
            .setIconItem(() -> new ItemStack(ItemInit.living_staff.get())));
    // </skill-editor>
  }
}
`;

const coreBranchSource = `package example;
public final class CoreSkillBranch {
  public static void register(List<SkillPoint> branch) {
    // <skill-editor branch="core">
    SkillPointInit.base_skill = SkillPointInit.registerSkill(branch,
        new SkillPoint(0, "base", 0, 1, EnumSkillStates.UNLOCKED, null)
            .setIconItem(() -> new ItemStack(ItemInit.sanguine_formation.get())));
    SkillPointInit.skill_manip_slots = SkillPointInit.registerSkill(branch,
        new SkillPoint(14, "skill_manip_slots", 200, 5, EnumSkillStates.LOCKED, SkillPointInit.base_skill)
            .setSkillPointCost(2).setRequiredDegree(1)
            .setIconItem(() -> new ItemStack(ItemInit.scrying_dish.get())));
    // </skill-editor>
  }
}
`;

test('loads Java skill branch files and attaches translation descriptions', async () => {
  const root = makeRepo();
  writeBranch(root, 'CoreSkillBranch.java', coreBranchSource);
  writeBranch(root, 'LivingStaffSkillBranch.java', branchSource);
  writeInit(root, 'ItemInit.java', `package example;
public class ItemInit {
  public static final DeferredHolder<Item, Item> living_staff = BASEITEMS.register("living_staff", () -> null);
  public static final DeferredHolder<Item, Item> scrying_dish = BASEITEMS.register("scrying_dish", () -> null);
}
`);
  writeInit(root, 'BlockInit.java', `package example;
public class BlockInit {
  public static final DeferredHolder<Block, Block> dendritic_distributor = BASEBLOCKS.register("dendritic_distributor", () -> null);
}
`);
  writeLang(root, {
    'skill.hemomancy.base.desc': 'The root of blood instruction.',
    'skill.hemomancy.skill_manip_slots.desc': 'More room for manipulations.',
    'skill.hemomancy.skill_living_conduit.desc': 'The staff answers through disciplined circulation.'
  });

  const workspace = await loadWorkspace(root);

  const livingStaffBranch = workspace.branches.find(branch => branch.branch === 'living_staff');
  expect(workspace.branches).toHaveLength(2);
  expect(livingStaffBranch?.skills[0].description).toBe('The staff answers through disciplined circulation.');
  expect(workspace.iconOptions).toEqual({
    items: ['living_staff', 'scrying_dish'],
    blocks: ['dendritic_distributor']
  });
});

test('loads wrapped DeferredHolder registry fields for icon options', async () => {
  const root = makeRepo();
  writeBranch(root, 'CoreSkillBranch.java', coreBranchSource);
  writeLang(root, {});
  writeInit(root, 'ItemInit.java', `package example;
public class ItemInit {
  public static final DeferredHolder<Item, Item> blood_rock = BASEITEMS
      .register("blood_rock", () -> null);
}
`);
  writeInit(root, 'BlockInit.java', `package example;
public class BlockInit {
  public static final DeferredHolder<Block, Block> somatic_loom = MODELEDBLOCKS
      .register("somatic_loom", () -> null);
  public static final DeferredHolder<Block, Block> chiseled_hematic_iron_block = BASEBLOCKS
      .register("chiseled_hematic_iron_block", () -> null);
}
`);

  const workspace = await loadWorkspace(root);

  expect(workspace.iconOptions).toEqual({
    items: ['blood_rock'],
    blocks: ['chiseled_hematic_iron_block', 'somatic_loom']
  });
});

test('previews Java branch edits and lang changes without applying them', async () => {
  const root = makeRepo();
  writeBranch(root, 'CoreSkillBranch.java', coreBranchSource);
  writeBranch(root, 'LivingStaffSkillBranch.java', branchSource);
  writeLang(root, {
    'skill.hemomancy.base.desc': 'The root of blood instruction.',
    'skill.hemomancy.skill_manip_slots.desc': 'More room for manipulations.'
  });
  const workspace = await loadWorkspace(root);
  const livingStaffBranch = workspace.branches.find(branch => branch.branch === 'living_staff');
  expect(livingStaffBranch).toBeDefined();
  livingStaffBranch!.skills[0].bloodCost = 275;
  livingStaffBranch!.skills[0].description = 'A sharper rite of staff handling.';

  const preview = await previewWorkspaceChanges(root, {
    branches: workspace.branches,
    translations: {
      'skill.hemomancy.skill_living_conduit.desc': 'A sharper rite of staff handling.'
    }
  });

  expect(preview.canApply).toBe(true);
  expect(preview.diffs.map(diff => diff.path)).toEqual([
    'src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java',
    'src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java',
    'src/main/resources/assets/hemomancy/lang/en_us.json'
  ]);
  expect(preview.diffs[1].after).toContain('new SkillPoint(21, "skill_living_conduit", 275, 3');
  expect(readFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java'), 'utf8')).toContain('250');
});

test('applies degree label positions and reloads them from Java', async () => {
  const root = makeRepo();
  writeBranch(root, 'CoreSkillBranch.java', coreBranchSource);
  writeBranch(root, 'LivingStaffSkillBranch.java', branchSource);
  writeLang(root, {});
  const workspace = await loadWorkspace(root);
  const coreBranch = workspace.branches.find(branch => branch.branch === 'core');
  expect(coreBranch).toBeDefined();
  coreBranch!.degreeLabels = [
    { degree: 0, x: 172, y: 214 },
    { degree: 4, x: 812, y: 364 }
  ];

  const preview = await previewWorkspaceChanges(root, { branches: workspace.branches });
  await applyPreview(root, preview.id);
  const reloaded = await loadWorkspace(root);
  const reloadedCore = reloaded.branches.find(branch => branch.branch === 'core');

  expect(reloadedCore?.degreeLabels).toEqual([
    { degree: 0, x: 172, y: 214 },
    { degree: 4, x: 812, y: 364 }
  ]);
});

function makeRepo(): string {
  const root = mkdtempSync(join(tmpdir(), 'hemo-skill-editor-'));
  mkdirSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills'), { recursive: true });
  mkdirSync(join(root, 'src/main/resources/assets/hemomancy/lang'), { recursive: true });
  return root;
}

function writeBranch(root: string, name: string, source: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills', name), source, 'utf8');
}

function writeInit(root: string, name: string, source: string): void {
  const dir = join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init');
  mkdirSync(dir, { recursive: true });
  writeFileSync(join(dir, name), source, 'utf8');
}

function writeLang(root: string, values: Record<string, string>): void {
  writeFileSync(join(root, 'src/main/resources/assets/hemomancy/lang/en_us.json'), JSON.stringify(values, null, 2) + '\n', 'utf8');
}
