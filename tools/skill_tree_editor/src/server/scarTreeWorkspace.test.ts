import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { loadScarTreeWorkspace, previewScarTreeWorkspaceChanges } from './scarTreeWorkspace';

describe('scar tree workspace', () => {
  it('loads scar metadata and previews authored layout edits', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-scar-editor-'));
    write(root, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java', `
      final class ScarTreeLayout {
        static final Object NODES = List.of(authored("hemomancy:scar_heart", 480, 210));
      }
    `);
    write(root, 'src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java', `
      class ScarInit {
        static final Object scar_heart = reg("scar_heart", () -> cerebral(EnumBloodTendency.ANIMUS, 1.0f, 1));
      }
    `);
    write(root, 'src/main/resources/assets/hemomancy/lang/en_us.json', JSON.stringify({
      'item.hemomancy.scar_heart': 'Scar of the Heart'
    }));

    const workspace = await loadScarTreeWorkspace(root);
    expect(workspace.tree.nodes[0]).toEqual(expect.objectContaining({
      displayName: 'Scar of the Heart', tendency: 'ANIMUS', tier: 1, color: '#ff0000'
    }));
    workspace.tree.nodes[0].treeX = 500;

    const preview = await previewScarTreeWorkspaceChanges(root, { nodes: workspace.tree.nodes });
    expect(preview.canApply).toBe(true);
    expect(preview.diffs[0].after).toContain('authored("hemomancy:scar_heart", 500, 210)');
  });
});

function write(root: string, path: string, source: string): void {
  const absolute = join(root, path);
  mkdirSync(join(absolute, '..'), { recursive: true });
  writeFileSync(absolute, source, 'utf8');
}
