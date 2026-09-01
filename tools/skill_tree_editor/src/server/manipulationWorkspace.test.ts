import { mkdtempSync, mkdirSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { writeFileSync } from 'node:fs';
import { loadManipulationWorkspace, previewManipulationWorkspaceChanges } from './manipulationWorkspace';

const treeSource = `package example;

public class ManipulationTreeInit {
  public static void init() {
    register("venous_travel", 750, 180);
  }

  private static void register(String name, int x, int y, String... parents) {
  }
}
`;

const initSource = `package example;

public class ManipulationInit {
  public static final DeferredHolder<BloodManipulation, BloodManipulation> venous_travel = MANIPS.register("venous_travel",
      () -> new BloodManipulation("venous_travel", 1000, 0, 0, EnumManipulationType.CONTINUOUS,
          EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FERRIC, EnumVeinSections.ARMS)
          .setSecondaryTend(EnumBloodTendency.DUCTILIS)
          .setCooldownTicks(20));
}
`;

test('previews manipulation tendency edits in ManipulationInit.java', async () => {
  const root = makeRepo();
  writeManipulationTree(root, treeSource);
  writeManipulationInit(root, initSource);
  const workspace = await loadManipulationWorkspace(root);
  const node = workspace.tree.nodes.find(item => item.name === 'venous_travel');
  expect(node).toEqual(expect.objectContaining({ tendency: 'FERRIC', color: '#353535' }));
  node!.tendency = 'TENEBRIS';
  node!.color = '#46006e';

  const preview = await previewManipulationWorkspaceChanges(root, {
    nodes: workspace.tree.nodes
  });

  expect(preview.canApply).toBe(true);
  expect(preview.diffs.map(diff => diff.path)).toContain('src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java');
  const initDiff = preview.diffs.find(diff => diff.path.endsWith('ManipulationInit.java'));
  expect(initDiff?.after).toContain('EnumBloodTendency.TENEBRIS');
  expect(initDiff?.after).not.toContain('EnumBloodTendency.FERRIC');
});

test('previews manipulation secondary tendency edits in ManipulationInit.java', async () => {
  const root = makeRepo();
  writeManipulationTree(root, treeSource);
  writeManipulationInit(root, initSource);
  const workspace = await loadManipulationWorkspace(root);
  const node = workspace.tree.nodes.find(item => item.name === 'venous_travel');
  expect(node).toEqual(expect.objectContaining({ secondaryTendency: 'DUCTILIS' }));
  node!.secondaryTendency = 'TENEBRIS';

  const preview = await previewManipulationWorkspaceChanges(root, {
    nodes: workspace.tree.nodes
  });

  expect(preview.canApply).toBe(true);
  const initDiff = preview.diffs.find(diff => diff.path.endsWith('ManipulationInit.java'));
  expect(initDiff?.after).toContain('.setSecondaryTend(EnumBloodTendency.TENEBRIS)');
  expect(initDiff?.after).not.toContain('.setSecondaryTend(EnumBloodTendency.DUCTILIS)');
});

test('previews inserting manipulation secondary tendency before cooldown chains', async () => {
  const root = makeRepo();
  writeManipulationTree(root, treeSource);
  writeManipulationInit(root, initSource.replace(/\n {10}\.setSecondaryTend\(EnumBloodTendency\.DUCTILIS\)/, ''));
  const workspace = await loadManipulationWorkspace(root);
  const node = workspace.tree.nodes.find(item => item.name === 'venous_travel');
  expect(node).toEqual(expect.objectContaining({ secondaryTendency: null }));
  node!.secondaryTendency = 'TENEBRIS';

  const preview = await previewManipulationWorkspaceChanges(root, {
    nodes: workspace.tree.nodes
  });

  const initDiff = preview.diffs.find(diff => diff.path.endsWith('ManipulationInit.java'));
  expect(initDiff?.after).toContain([
    '          .setSecondaryTend(EnumBloodTendency.TENEBRIS)',
    '          .setCooldownTicks(20)'
  ].join('\n'));
});

test('loads manipulation family presentation metadata for the editor preview', async () => {
  const root = makeRepo();
  writeManipulationTree(root, `
public class ManipulationTreeInit {
  public static void init() {
    register("hematic_rebuke", 670, 182, "blood_rush");
  }
  private static void register(String name, int x, int y, String... parents) {}
}
`);
  writeManipulationInit(root, `
public class ManipulationInit {
  public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_rebuke = MANIPS.register("hematic_rebuke",
      () -> new BloodManipulation("hematic_rebuke", 225, 10, 0, EnumManipulationType.QUICK,
          EnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
  public static final DeferredHolder<BloodManipulation, BloodManipulation> hematic_impressment = MANIPS.register("hematic_impressment",
      () -> new BloodManipulation("hematic_impressment", 500, 25, 0, EnumManipulationType.QUICK,
          EnumManipulationRank.SUMMA, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD));
}
`);
  write(root, 'src/main/java/com/vincenthuto/hemomancy/common/manipulation/family/ManipulationFamilyRegistry.java', `
public final class ManipulationFamilyRegistry {
  private static final Object FAMILIES = List.of(
      family("hematic_rebuke", form("hematic_impressment", 3)));
}
`);

  const workspace = await loadManipulationWorkspace(root);
  expect(workspace.tree.nodes[0]).toEqual(expect.objectContaining({
    name: 'hematic_rebuke',
    rank: 'MEDIOCRITAS',
    borderColor: '#a7adb2',
    familyBaseline: 'hematic_rebuke',
    familyForms: ['hematic_impressment'],
    familyRequiredLevel: null,
    isFamilyBaseline: true
  }));
});

function makeRepo(): string {
  const root = mkdtempSync(join(tmpdir(), 'hemo-manip-editor-'));
  mkdirSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init'), { recursive: true });
  return root;
}

function writeManipulationTree(root: string, source: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java'), source, 'utf8');
}

function writeManipulationInit(root: string, source: string): void {
  writeFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java'), source, 'utf8');
}

function write(root: string, path: string, source: string): void {
  const absolute = join(root, path);
  mkdirSync(join(absolute, '..'), { recursive: true });
  writeFileSync(absolute, source, 'utf8');
}
