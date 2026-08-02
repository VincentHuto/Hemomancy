import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { parseRecipeMapDefinitionsJava, renderRecipeMapDefinitionsJava } from './recipeMapParser';
import { loadIconRegistryOptions } from './workspace';

const source = `package example;

public final class HarbingerRecipeMapDefinitions {
  public static final List<String> RITE_FAMILIES = List.of("Order", "Vessel", "Puppetry", RecipeMapLayout.MISC_FAMILY);
  public static final List<String> CRAFTING_FAMILIES = List.of("Foundations", RecipeMapLayout.MISC_FAMILY);

  static {
    // <recipe-map-editor>
    registerRites("Order", "sanguine_initiation", "votary_rite");
    registerRites("Vessel", "pallid_vessel_rite");
    registerRites("Puppetry", "puppeteer_trial_gorebound_hulk");
    registerCrafting("Foundations", "living_staff", "iron_brazier");
    positionRites("cardinal_rite/votary_rite", 612, 344);
    positionCrafting("blood_structure/living_staff", 288, 416);
    iconRites("cardinal_rite/votary_rite", () -> new ItemStack(ItemInit.living_staff.get()));
    iconCrafting("blood_structure/living_staff", () -> new ItemStack(BlockInit.iron_brazier.get()));
    linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/votary_rite", RecipeMapLink.Kind.PROGRESSION);
    linkCrafting("blood_structure/living_staff", "blood_structure/iron_brazier", RecipeMapLink.Kind.CONCEPTUAL);
    // </recipe-map-editor>
  }
}
`;

test('parses both recipe map tabs with families entries and typed links', () => {
  const parsed = parseRecipeMapDefinitionsJava('HarbingerRecipeMapDefinitions.java', source);

  expect(parsed.tabs).toEqual([
    {
      key: 'RITES',
      families: ['Order', 'Vessel', 'Puppetry', 'Miscellaneous'],
      entries: [
        { id: 'cardinal_rite/sanguine_initiation', family: 'Order', order: 0, column: 0, displayName: 'Sanguine Initiation' },
        { id: 'cardinal_rite/votary_rite', family: 'Order', order: 1, column: 0, displayName: 'Votary Rite', treeX: 612, treeY: 344, iconSource: 'item', iconItem: 'living_staff' },
        { id: 'cardinal_rite/pallid_vessel_rite', family: 'Vessel', order: 0, column: 0, displayName: 'Pallid Vessel Rite' },
        { id: 'cardinal_rite/puppeteer_trial_gorebound_hulk', family: 'Puppetry', order: 0, column: 0, displayName: 'Puppeteer Trial Gorebound Hulk' }
      ],
      links: [
        { from: 'cardinal_rite/sanguine_initiation', to: 'cardinal_rite/votary_rite', kind: 'PROGRESSION' }
      ]
    },
    {
      key: 'CRAFTING',
      families: ['Foundations', 'Miscellaneous'],
      entries: [
        { id: 'blood_structure/living_staff', family: 'Foundations', order: 0, column: 0, displayName: 'Living Staff', treeX: 288, treeY: 416, iconSource: 'block', iconItem: 'iron_brazier' },
        { id: 'blood_structure/iron_brazier', family: 'Foundations', order: 1, column: 0, displayName: 'Iron Brazier' }
      ],
      links: [
        { from: 'blood_structure/living_staff', to: 'blood_structure/iron_brazier', kind: 'CONCEPTUAL' }
      ]
    }
  ]);
  expect(parsed.diagnostics).toEqual([]);
});

test('does not parse the retired Blood Crafting puppetry registration method', () => {
  const legacy = source.replace(
    'registerCrafting("Foundations", "living_staff", "iron_brazier");',
    'registerPuppetry("Puppetry", "gorebound_hulk");'
  );

  const parsed = parseRecipeMapDefinitionsJava('HarbingerRecipeMapDefinitions.java', legacy);

  expect(parsed.tabs.flatMap(tab => tab.entries).some(entry => entry.id.startsWith('puppeteer_trial/'))).toBe(false);
});

test('reports position declarations whose entry no longer exists', () => {
  const parsed = parseRecipeMapDefinitionsJava('HarbingerRecipeMapDefinitions.java', source.replace(
    'positionRites("cardinal_rite/votary_rite", 612, 344);',
    'positionRites("cardinal_rite/missing", 612, 344);'
  ));

  expect(parsed.diagnostics).toEqual([expect.objectContaining({
    code: 'recipe_map_unknown_position_entry',
    severity: 'error',
    skill: 'cardinal_rite/missing'
  })]);
});

test('renders family ordering and link edits only inside the editor region', () => {
  const parsed = parseRecipeMapDefinitionsJava('HarbingerRecipeMapDefinitions.java', source);
  const rites = parsed.tabs[0];
  rites.entries[1] = { ...rites.entries[1], family: 'Vessel', order: 1 };
  rites.links.push({
    from: 'cardinal_rite/pallid_vessel_rite',
    to: 'cardinal_rite/votary_rite',
    kind: 'CONCEPTUAL'
  });
  rites.entries[0].treeX = 501;
  rites.entries[0].treeY = 277;
  rites.entries[0].iconSource = 'block';
  rites.entries[0].iconItem = 'dendritic_distributor';

  const rendered = renderRecipeMapDefinitionsJava(source, parsed.tabs);

  expect(rendered).toContain('public final class HarbingerRecipeMapDefinitions');
  expect(rendered).toContain('registerRites("Order", "sanguine_initiation");');
  expect(rendered).toContain('registerRites("Vessel", "pallid_vessel_rite", "votary_rite");');
  expect(rendered).toContain('positionRites("cardinal_rite/sanguine_initiation", 501, 277);');
  expect(rendered).toContain('iconRites("cardinal_rite/sanguine_initiation", () -> new ItemStack(BlockInit.dendritic_distributor.get()));');
  expect(rendered).toContain('linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/votary_rite", RecipeMapLink.Kind.CONCEPTUAL);');
  expect(rendered.indexOf('sanguine_initiation')).toBeLessThan(rendered.indexOf('pallid_vessel_rite'));
});

test('every authored Rite has a thematic display icon', () => {
  const repoRoot = resolve(process.cwd(), '..', '..');
  const definitions = readFileSync(resolve(repoRoot,
    'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java'), 'utf8');
  const rites = parseRecipeMapDefinitionsJava('HarbingerRecipeMapDefinitions.java', definitions).tabs[0].entries;
  const iconOptions = loadIconRegistryOptions(repoRoot);

  expect(rites).toHaveLength(37);
  expect(rites.every(rite => rite.iconSource && rite.iconItem)).toBe(true);
  expect(rites.every(rite => /^[a-z0-9_]+$/.test(rite.iconItem ?? ''))).toBe(true);
  expect(rites.every(rite => rite.iconSource === 'block'
    ? iconOptions.blocks.includes(rite.iconItem ?? '')
    : iconOptions.items.includes(rite.iconItem ?? ''))).toBe(true);
  expect(Object.fromEntries(rites.map(rite => [rite.id, `${rite.iconSource}:${rite.iconItem}`]))).toMatchObject({
    'cardinal_rite/sanguine_initiation': 'item:sanguine_formation',
    'cardinal_rite/pallid_vessel_rite': 'item:blood_gourd_white',
    'cardinal_rite/sanguine_eclipse': 'item:memory_blood_eclipse',
    'cardinal_rite/bloom_of_qliphoth': 'block:qliphoth_bloom',
    'cardinal_rite/puppeteer_trial_gorebound_hulk': 'item:gorebound_yoke',
    'cardinal_rite/puppeteer_trial_marrow_spitter': 'item:marrow_spitter_carriage',
    'cardinal_rite/puppeteer_trial_mnemonist_puppet': 'item:mnemonist_cradle',
    'cardinal_rite/puppeteer_trial_veinwing_vulture': 'item:veinwing_harness'
  });
});
