import { mkdtempSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { loadTendenciesWorkspace, previewTendenciesWorkspaceChanges } from './tendenciesWorkspace';
import { applyPreview } from './workspace';

describe('combined tendencies workspace', () => {
  it('loads manipulations and scars together and previews both source updates', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-tendencies-editor-'));
    write(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java', `
      class ManipulationTreeInit {
        static void init() { register("venous_travel", 750, 180); }
        static void register(String name, int x, int y, String... parents) {}
      }
    `);
    write(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java', `
      class ManipulationInit {
        static final Object value = MANIPS.register("venous_travel", () -> new BloodManipulation(
          "venous_travel", 1, 0, 0, TYPE, RANK, EnumBloodTendency.FERRIC, SECTION));
      }
    `);
    write(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java', `
      final class ScarTreeLayout {
        static final Object NODES = List.of(authored("hemomancy:scar_heart", 480, 280));
      }
    `);
    write(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java', `
      class ScarInit {
        static final Object value = reg("scar_heart", () -> cerebral(EnumBloodTendency.ANIMUS, 1.0f, 1));
      }
    `);
    write(root, 'src/main/resources/assets/hemomancy/lang/en_us.json', JSON.stringify({
      'item.hemomancy.scar_heart': 'Scar of the Heart'
    }));

    const workspace = await loadTendenciesWorkspace(root);
    expect(workspace.manipulations.tree.nodes).toHaveLength(1);
    expect(workspace.scars.tree.nodes).toHaveLength(1);
    workspace.manipulations.tree.nodes[0].treeX = 760;
    workspace.scars.tree.nodes[0].treeX = 492;

    const preview = await previewTendenciesWorkspaceChanges(root, {
      manipulations: workspace.manipulations.tree.nodes,
      scars: workspace.scars.tree.nodes
    });

    expect(preview.canApply).toBe(true);
    expect(preview.diffs.map(diff => diff.path)).toEqual(expect.arrayContaining([
      'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java',
      'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java'
    ]));
    await applyPreview(root, preview.id);
    expect(readFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java'), 'utf8')).toContain('"venous_travel",760,180');
    expect(readFileSync(join(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java'), 'utf8')).toContain('"hemomancy:scar_heart", 492, 280');

    workspace.scars.tree.nodes[0].treeX = 493;
    const scarOnlyPreview = await previewTendenciesWorkspaceChanges(root, {
      manipulations: workspace.manipulations.tree.nodes,
      scars: workspace.scars.tree.nodes
    });
    expect(scarOnlyPreview.canApply).toBe(true);
    expect(scarOnlyPreview.diffs.map(diff => diff.path)).toContain(
      'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java'
    );
  });
});

function write(root: string, path: string, source: string): void {
  const absolute = join(root, path);
  mkdirSync(join(absolute, '..'), { recursive: true });
  writeFileSync(absolute, source, 'utf8');
}
