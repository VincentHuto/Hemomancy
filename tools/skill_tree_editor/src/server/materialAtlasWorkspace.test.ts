import { mkdtempSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { loadMaterialAtlasWorkspace, previewMaterialAtlasWorkspaceChanges } from './materialAtlasWorkspace';

const atlasSource = `package example;

public final class MaterialAtlasSpec {
  private static void registerBuckets() {
    bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", "sanguine_formation", 0xFFD04436, 520, 230, 520, 170);
    bucket(MaterialAtlasPath.UNSTAINED, "still_waters_core", "Still Waters Core", "hemolytic_solution", 0xFF80B0A0, 520, 230, 520, 170);
  }

  private static void registerHarbingerEntries() {
    MaterialAtlasPath h = MaterialAtlasPath.HARBINGER;
    entry("sanguine_formation", h, "bloodcraft_core", a());
  }

  private static void registerUnstainedEntries() {
    MaterialAtlasPath u = MaterialAtlasPath.UNSTAINED;
    entry("hemolytic_solution", u, "still_waters_core", a());
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
    return Collections.unmodifiableList(list);
  }

  private static List<MaterialEntry> buildUnstainedEntries() {
    List<MaterialEntry> list = new ArrayList<>();
    list.add(new MaterialEntry("hemolytic_solution", "Hemolytic Solution",
        "Dissolves sanguine formations.",
        "Anti-Blood", () -> new ItemStack(ItemInit.hemolytic_solution.get())));
    return Collections.unmodifiableList(list);
  }
}
`;

test('loads both material atlas paths with catalogue and registry icon options', async () => {
  const root = makeRepo();
  writeAtlas(root, atlasSource);
  writeMaterials(root, materialsSource);
  writeRegistries(root);

  const workspace = await loadMaterialAtlasWorkspace(root);

  expect(workspace.paths.map(path => path.path)).toEqual(['HARBINGER', 'UNSTAINED']);
  expect(workspace.paths[0].entries[0]).toEqual(expect.objectContaining({
    id: 'sanguine_formation',
    catalog: expect.objectContaining({ displayName: 'Sanguine Formation', iconField: 'sanguine_formation' })
  }));
  expect(workspace.iconOptions).toEqual({
    items: ['bloody_vial', 'hemolytic_solution', 'sanguine_formation'],
    blocks: ['blood_basin']
  });
});

test('preview renders atlas and catalogue diffs without applying them', async () => {
  const root = makeRepo();
  writeAtlas(root, atlasSource);
  writeMaterials(root, materialsSource);
  writeRegistries(root);
  const workspace = await loadMaterialAtlasWorkspace(root);
  const harbinger = workspace.paths.find(path => path.path === 'HARBINGER')!;
  harbinger.buckets[0].centerX = 555;
  harbinger.buckets[0].plaqueY = 188;
  harbinger.entries[0].nodeX = 610;
  harbinger.entries[0].nodeY = 300;
  harbinger.entries[0].catalog!.description = 'An edited atlas note.';

  const preview = await previewMaterialAtlasWorkspaceChanges(root, {
    paths: workspace.paths,
    catalogueEntries: workspace.paths.flatMap(path => path.entries.map(entry => entry.catalog!).filter(Boolean))
  });

  expect(preview.canApply).toBe(true);
  expect(preview.diffs.map(diff => diff.path)).toEqual([
    'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java',
    'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java'
  ]);
  expect(preview.diffs[0].after).toContain('bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", "sanguine_formation", 0xFFD04436, 555, 230, 520, 188);');
  expect(preview.diffs[0].after).toContain('entryAt("sanguine_formation", h, "bloodcraft_core", a(), 610, 300);');
  expect(preview.diffs[1].after).toContain('"An edited atlas note."');
  expect(readFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java'), 'utf8')).toContain('520, 230');
});

test('preview blocks duplicate ids unknown buckets unknown parents invalid gates and missing icon fields', async () => {
  const root = makeRepo();
  writeAtlas(root, atlasSource);
  writeMaterials(root, materialsSource);
  writeRegistries(root);
  const workspace = await loadMaterialAtlasWorkspace(root);
  const harbinger = workspace.paths.find(path => path.path === 'HARBINGER')!;
  harbinger.entries.push({
    path: 'HARBINGER',
    id: 'sanguine_formation',
    bucketId: 'missing_bucket',
    gate: { type: 'PURITY', value: 25 },
    order: 99,
    parentIds: ['missing_parent'],
    nodeX: 600,
    nodeY: 600,
    catalog: {
      path: 'HARBINGER',
      id: 'sanguine_formation',
      displayName: 'Duplicate',
      description: 'Invalid.',
      category: 'Materials',
      iconSource: 'item',
      iconField: 'missing_icon',
      hasRecipe: true,
      gate: { type: 'PURITY', value: 25 }
    }
  });

  const preview = await previewMaterialAtlasWorkspaceChanges(root, {
    paths: workspace.paths,
    catalogueEntries: workspace.paths.flatMap(path => path.entries.map(entry => entry.catalog!).filter(Boolean))
  });

  expect(preview.canApply).toBe(false);
  expect(preview.diagnostics.map(diagnostic => diagnostic.code)).toEqual(expect.arrayContaining([
    'material_duplicate_id',
    'material_unknown_bucket',
    'material_unknown_parent',
    'material_invalid_gate',
    'material_missing_icon_field'
  ]));
});

function makeRepo(): string {
  const root = mkdtempSync(join(tmpdir(), 'hemo-material-editor-'));
  mkdirSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared'), { recursive: true });
  mkdirSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init'), { recursive: true });
  return root;
}

function writeAtlas(root: string, source: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java'), source, 'utf8');
}

function writeMaterials(root: string, source: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java'), source, 'utf8');
}

function writeRegistries(root: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java'), `package example;
public class ItemInit {
  public static final DeferredHolder<Item, Item> sanguine_formation = BASEITEMS.register("sanguine_formation", () -> null);
  public static final DeferredHolder<Item, Item> bloody_vial = BASEITEMS.register("bloody_vial", () -> null);
  public static final DeferredHolder<Item, Item> hemolytic_solution = BASEITEMS.register("hemolytic_solution", () -> null);
}
`, 'utf8');
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java'), `package example;
public class BlockInit {
  public static final DeferredHolder<Block, Block> blood_basin = BASEBLOCKS.register("blood_basin", () -> null);
}
`, 'utf8');
}
