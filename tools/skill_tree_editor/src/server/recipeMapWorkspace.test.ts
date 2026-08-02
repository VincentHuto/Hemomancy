import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { loadRecipeMapWorkspace, previewRecipeMapWorkspaceChanges } from './recipeMapWorkspace';

const definitionsPath = 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java';

function repoFixture(): string {
  const root = mkdtempSync(join(tmpdir(), 'hemo-recipe-map-'));
  const absolute = join(root, definitionsPath);
  mkdirSync(join(absolute, '..'), { recursive: true });
  writeFileSync(absolute, `
public final class HarbingerRecipeMapDefinitions {
  public static final List<String> RITE_FAMILIES = List.of("Order", RecipeMapLayout.MISC_FAMILY);
  public static final List<String> CRAFTING_FAMILIES = List.of("Foundations", RecipeMapLayout.MISC_FAMILY);
  static {
    // <recipe-map-editor>
    registerRites("Order", "sanguine_initiation");
    registerCrafting("Foundations", "living_staff");
    // </recipe-map-editor>
  }
}`);
  const rite = join(root, 'src/main/resources/data/hemomancy/recipe/cardinal_rite/sanguine_initiation.json');
  mkdirSync(join(rite, '..'), { recursive: true });
  writeFileSync(rite, JSON.stringify({ required_degree: 3, riteName: 'Rite of Sanguine Initiation' }));
  const craft = join(root, 'src/main/resources/data/hemomancy/recipe/blood_structure/living_staff.json');
  mkdirSync(join(craft, '..'), { recursive: true });
  writeFileSync(craft, JSON.stringify({ required_degree: 2, result: { id: 'hemomancy:living_staff' } }));
  const initRoot = join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init');
  mkdirSync(initRoot, { recursive: true });
  writeFileSync(join(initRoot, 'ItemInit.java'), `public class ItemInit {
    public static final DeferredHolder<Item, Item> living_staff = ITEMS.register("living_staff", () -> null);
    // public static final DeferredHolder<Item, Item> retired_icon = ITEMS.register("retired_icon", () -> null);
  }`);
  writeFileSync(join(initRoot, 'BlockInit.java'), `public class BlockInit {
    public static final DeferredHolder<Block, Block> iron_brazier = BLOCKS.register("iron_brazier", () -> null);
  }`);
  return root;
}

test('loads recipe degree and display metadata for both editable tabs', async () => {
  const workspace = await loadRecipeMapWorkspace(repoFixture());

  expect(workspace.tabs[0].entries[0]).toEqual(expect.objectContaining({
    id: 'cardinal_rite/sanguine_initiation',
    column: 3,
    displayName: 'Rite of Sanguine Initiation'
  }));
  expect(workspace.tabs[1].entries[0]).toEqual(expect.objectContaining({
    id: 'blood_structure/living_staff',
    column: 2,
    displayName: 'Living Staff',
    resultIcon: 'living_staff'
  }));
  expect(workspace.iconOptions).toEqual({ items: ['living_staff'], blocks: ['iron_brazier'] });
  expect(workspace.diagnostics).toEqual([]);
});

test('preview rejects missing icon fields and renders valid item and block overrides', async () => {
  const root = repoFixture();
  const workspace = await loadRecipeMapWorkspace(root);
  workspace.tabs[0].entries[0].iconSource = 'item';
  workspace.tabs[0].entries[0].iconItem = 'missing_item';

  const invalid = await previewRecipeMapWorkspaceChanges(root, { tabs: workspace.tabs });
  expect(invalid.canApply).toBe(false);
  expect(invalid.diagnostics.map(diagnostic => diagnostic.code)).toContain('recipe_map_missing_icon_field');

  workspace.tabs[0].entries[0].iconItem = 'living_staff';
  workspace.tabs[1].entries[0].iconSource = 'block';
  workspace.tabs[1].entries[0].iconItem = 'iron_brazier';
  const valid = await previewRecipeMapWorkspaceChanges(root, { tabs: workspace.tabs });
  expect(valid.canApply).toBe(true);
  expect(valid.diffs[0].after).toContain('iconRites("cardinal_rite/sanguine_initiation", () -> new ItemStack(ItemInit.living_staff.get()));');
  expect(valid.diffs[0].after).toContain('iconCrafting("blood_structure/living_staff", () -> new ItemStack(BlockInit.iron_brazier.get()));');
});

test('preview rejects duplicate links and renders valid family edits', async () => {
  const root = repoFixture();
  const workspace = await loadRecipeMapWorkspace(root);
  const rites = workspace.tabs[0];
  rites.links = [
    { from: rites.entries[0].id, to: rites.entries[0].id, kind: 'CONCEPTUAL' },
    { from: rites.entries[0].id, to: rites.entries[0].id, kind: 'CONCEPTUAL' }
  ];

  const invalid = await previewRecipeMapWorkspaceChanges(root, { tabs: workspace.tabs });
  expect(invalid.canApply).toBe(false);
  expect(invalid.diagnostics.map(diagnostic => diagnostic.code)).toContain('recipe_map_self_link');
  expect(invalid.diagnostics.map(diagnostic => diagnostic.code)).toContain('recipe_map_duplicate_link');

  rites.links = [];
  rites.families.splice(0, 0, 'Founding');
  rites.entries[0].family = 'Founding';
  const valid = await previewRecipeMapWorkspaceChanges(root, { tabs: workspace.tabs });
  expect(valid.canApply).toBe(true);
  expect(valid.diffs[0].after).toContain('registerRites("Founding", "sanguine_initiation");');
});
