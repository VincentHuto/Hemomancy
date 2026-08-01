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
    displayName: 'Living Staff'
  }));
  expect(workspace.diagnostics).toEqual([]);
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
