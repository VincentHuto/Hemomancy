import { parseMaterialAtlasSpecJava, parseMaterialsDataJava, renderMaterialAtlasSpecJava, renderMaterialsDataJava } from './materialAtlasParser';

const atlasSource = `package example;

public final class MaterialAtlasSpec {
  private static final int HARBINGER_HUB_X = 686;
  private static final int HARBINGER_HUB_Y = 617;
  private static final int HARBINGER_HUB_LABEL_X = 686;
  private static final int HARBINGER_HUB_LABEL_Y = 617;
  private static final int UNSTAINED_HUB_X = 495;
  private static final int UNSTAINED_HUB_Y = 522;
  private static final int UNSTAINED_HUB_LABEL_X = 495;
  private static final int UNSTAINED_HUB_LABEL_Y = 522;

  static {
    registerBuckets();
    registerHarbingerEntries();
    registerUnstainedEntries();
  }

  private static void registerBuckets() {
    bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", "sanguine_formation", 0xFFD04436, 520, 230, 520, 170);
    bucket(MaterialAtlasPath.UNSTAINED, "still_waters_core", "Still Waters Core", "hemolytic_solution", 0xFF80B0A0, 520, 230, 520, 170);
  }

  private static void registerHarbingerEntries() {
    MaterialAtlasPath h = MaterialAtlasPath.HARBINGER;
    entry("sanguine_formation", h, "bloodcraft_core", a());
    entryAt("blood_crystal_shard", h, "bloodcraft_core", d(2), 604, 244, "sanguine_formation");
  }

  private static void registerUnstainedEntries() {
    MaterialAtlasPath u = MaterialAtlasPath.UNSTAINED;
    entry("hemolytic_solution", u, "still_waters_core", a());
    entry("silver_chalice", u, "still_waters_core", c(10.0F), "hemolytic_solution");
  }
}
`;

const materialsSource = `package example;

public final class MaterialsData {
  private static List<MaterialEntry> buildBloodEntries() {
    List<MaterialEntry> list = new ArrayList<>();
    list.add(new MaterialEntry("sanguine_formation", "Sanguine Formation",
        "Raw blood crystal.",
        "Materials", () -> new ItemStack(ItemInit.sanguine_formation.get())));
//  list.add(new MaterialEntry("commented_material", "Commented",
//      "Should stay invisible.",
//      "Materials", () -> new ItemStack(ItemInit.commented_material.get())));
    list.add(new MaterialEntry("blood_basin", "Blood Basin",
        "Ritual basin.",
        "Functional Blocks", () -> new ItemStack(BlockInit.blood_basin.get()),
        true, UnlockPredicate.minDegree(2)));
    return Collections.unmodifiableList(list);
  }

  private static List<MaterialEntry> buildUnstainedEntries() {
    List<MaterialEntry> list = new ArrayList<>();
    list.add(new MaterialEntry("hemolytic_solution", "Hemolytic Solution",
        "Dissolves sanguine formations.",
        "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_solution.get())));
    list.add(new MaterialEntry("silver_chalice", "Silver Chalice",
        "Vessel of purification.",
        "Materials", () -> new ItemStack(ItemInit.silver_chalice.get()),
        true, UnlockPredicate.minClarity(10f)));
    return Collections.unmodifiableList(list);
  }
}
`;

test('parses atlas bucket coordinates colors entryAt coordinates gates and parents', () => {
  const parsed = parseMaterialAtlasSpecJava('MaterialAtlasSpec.java', atlasSource);
  const harbinger = parsed.paths.find(path => path.path === 'HARBINGER');
  const unstained = parsed.paths.find(path => path.path === 'UNSTAINED');

  expect(harbinger?.buckets[0]).toEqual({
    path: 'HARBINGER',
    id: 'bloodcraft_core',
    label: 'Bloodcraft Core',
    color: '0xFFD04436',
    centerX: 520,
    centerY: 230,
    plaqueX: 520,
    plaqueY: 170
  });
  expect(harbinger).toEqual(expect.objectContaining({
    hubX: 686,
    hubY: 617,
    hubLabelX: 686,
    hubLabelY: 617
  }));
  expect(unstained).toEqual(expect.objectContaining({
    hubX: 495,
    hubY: 522,
    hubLabelX: 495,
    hubLabelY: 522
  }));
  expect(unstained?.entries.find(entry => entry.id === 'silver_chalice')).toEqual(expect.objectContaining({
    bucketId: 'still_waters_core',
    gate: { type: 'CLARITY', value: 10 },
    parentIds: ['hemolytic_solution'],
    nodeX: null,
    nodeY: null
  }));
  expect(harbinger?.entries.find(entry => entry.id === 'blood_crystal_shard')).toEqual(expect.objectContaining({
    gate: { type: 'DEGREE', value: 2 },
    parentIds: ['sanguine_formation'],
    nodeX: 604,
    nodeY: 244
  }));
});

test('renders bucket movement entry coordinates gate edits and parent edits', () => {
  const parsed = parseMaterialAtlasSpecJava('MaterialAtlasSpec.java', atlasSource);
  const harbinger = parsed.paths.find(path => path.path === 'HARBINGER')!;
  harbinger.hubX = 704;
  harbinger.hubY = 640;
  harbinger.hubLabelX = 760;
  harbinger.hubLabelY = 690;
  harbinger.buckets[0] = { ...harbinger.buckets[0], color: '0xFFDD3344', centerX: 560, centerY: 260, plaqueX: 575, plaqueY: 190 };
  harbinger.entries[1] = {
    ...harbinger.entries[1],
    gate: { type: 'DEGREE', value: 3 },
    nodeX: 640,
    nodeY: 288,
    parentIds: ['sanguine_formation']
  };

  const rendered = renderMaterialAtlasSpecJava(atlasSource, parsed.paths);

  expect(rendered).toContain('private static final int HARBINGER_HUB_X = 704;');
  expect(rendered).toContain('private static final int HARBINGER_HUB_Y = 640;');
  expect(rendered).toContain('private static final int HARBINGER_HUB_LABEL_X = 760;');
  expect(rendered).toContain('private static final int HARBINGER_HUB_LABEL_Y = 690;');
  expect(rendered).toContain('bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", 0xFFDD3344, 560, 260, 575, 190);');
  expect(rendered).not.toContain('"Bloodcraft Core", "sanguine_formation", 0xFFDD3344');
  expect(rendered).toContain('entryAt("blood_crystal_shard", h, "bloodcraft_core", d(3), 640, 288, "sanguine_formation");');
  expect(rendered).not.toContain('entry("blood_crystal_shard", h, "bloodcraft_core", d(2)');
});

test('parses active material catalogue entries and ignores commented entries', () => {
  const parsed = parseMaterialsDataJava('MaterialsData.java', materialsSource);

  expect(parsed.entries.map(entry => entry.id)).toEqual([
    'sanguine_formation',
    'blood_basin',
    'hemolytic_solution',
    'silver_chalice'
  ]);
  expect(parsed.entries.find(entry => entry.id === 'blood_basin')).toEqual(expect.objectContaining({
    path: 'HARBINGER',
    iconSource: 'block',
    iconField: 'blood_basin',
    hasRecipe: true,
    gate: { type: 'DEGREE', value: 2 }
  }));
  expect(parsed.entries.find(entry => entry.id === 'silver_chalice')).toEqual(expect.objectContaining({
    path: 'UNSTAINED',
    iconSource: 'item',
    iconField: 'silver_chalice',
    gate: { type: 'CLARITY', value: 10 }
  }));
});

test('renders catalogue text edits and appends new entries with matching unlock predicates', () => {
  const parsed = parseMaterialsDataJava('MaterialsData.java', materialsSource);
  const edited = parsed.entries.map(entry => entry.id === 'sanguine_formation'
    ? { ...entry, displayName: 'Sanguine Formation Revised', description: 'A revised catalogue note.' }
    : entry);
  edited.push({
    path: 'HARBINGER',
    id: 'fresh_vial',
    displayName: 'Fresh Vial',
    description: 'A newly catalogued vial.',
    category: 'Materials',
    iconSource: 'item',
    iconField: 'bloody_vial',
    hasRecipe: true,
    gate: { type: 'DEGREE', value: 4 }
  });

  const rendered = renderMaterialsDataJava(materialsSource, edited);

  expect(rendered).toContain('"Sanguine Formation Revised"');
  expect(rendered).toContain('"A revised catalogue note."');
  expect(rendered).toContain('list.add(new MaterialEntry("fresh_vial", "Fresh Vial",');
  expect(rendered).toContain('() -> new ItemStack(ItemInit.bloody_vial.get()),');
  expect(rendered).toContain('true, UnlockPredicate.minDegree(4)));');
});
