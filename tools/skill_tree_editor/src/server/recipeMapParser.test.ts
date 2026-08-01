import { parseRecipeMapDefinitionsJava, renderRecipeMapDefinitionsJava } from './recipeMapParser';

const source = `package example;

public final class HarbingerRecipeMapDefinitions {
  public static final List<String> RITE_FAMILIES = List.of("Order", "Vessel", RecipeMapLayout.MISC_FAMILY);
  public static final List<String> CRAFTING_FAMILIES = List.of("Foundations", "Puppetry", RecipeMapLayout.MISC_FAMILY);

  static {
    // <recipe-map-editor>
    registerRites("Order", "sanguine_initiation", "votary_rite");
    registerRites("Vessel", "pallid_vessel_rite");
    registerCrafting("Foundations", "living_staff", "iron_brazier");
    registerPuppetry("Puppetry", "gorebound_hulk");
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
      families: ['Order', 'Vessel', 'Miscellaneous'],
      entries: [
        { id: 'cardinal_rite/sanguine_initiation', family: 'Order', order: 0, column: 0, displayName: 'Sanguine Initiation' },
        { id: 'cardinal_rite/votary_rite', family: 'Order', order: 1, column: 0, displayName: 'Votary Rite' },
        { id: 'cardinal_rite/pallid_vessel_rite', family: 'Vessel', order: 0, column: 0, displayName: 'Pallid Vessel Rite' }
      ],
      links: [
        { from: 'cardinal_rite/sanguine_initiation', to: 'cardinal_rite/votary_rite', kind: 'PROGRESSION' }
      ]
    },
    {
      key: 'CRAFTING',
      families: ['Foundations', 'Puppetry', 'Miscellaneous'],
      entries: [
        { id: 'blood_structure/living_staff', family: 'Foundations', order: 0, column: 0, displayName: 'Living Staff' },
        { id: 'blood_structure/iron_brazier', family: 'Foundations', order: 1, column: 0, displayName: 'Iron Brazier' },
        { id: 'puppeteer_trial/gorebound_hulk', family: 'Puppetry', order: 0, column: 0, displayName: 'Gorebound Hulk' }
      ],
      links: [
        { from: 'blood_structure/living_staff', to: 'blood_structure/iron_brazier', kind: 'CONCEPTUAL' }
      ]
    }
  ]);
  expect(parsed.diagnostics).toEqual([]);
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

  const rendered = renderRecipeMapDefinitionsJava(source, parsed.tabs);

  expect(rendered).toContain('public final class HarbingerRecipeMapDefinitions');
  expect(rendered).toContain('registerRites("Order", "sanguine_initiation");');
  expect(rendered).toContain('registerRites("Vessel", "pallid_vessel_rite", "votary_rite");');
  expect(rendered).toContain('linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/votary_rite", RecipeMapLink.Kind.CONCEPTUAL);');
  expect(rendered.indexOf('sanguine_initiation')).toBeLessThan(rendered.indexOf('pallid_vessel_rite'));
});
