import { describe, expect, it } from 'vitest';
import { parseScarTreeJava, renderScarTreeJava } from './scarTreeParser';

const source = `
final class ScarTreeLayout {
  private static final List<AuthoredNode> AUTHORED_NODES = List.of(
      authored("hemomancy:scar_heart", 480, 210),
      authored("hemomancy:scar_marrow", 480, 160, "hemomancy:scar_heart")
  );
}
`;

describe('scar tree parser', () => {
  it('loads authored positions and lineage with canonical metadata', () => {
    const metadata = new Map([
      ['hemomancy:scar_heart', { displayName: 'Scar of the Heart', tendency: 'ANIMUS', tier: 1, color: '#ff0000' }],
      ['hemomancy:scar_marrow', { displayName: 'Scar of Marrow', tendency: 'ANIMUS', tier: 2, color: '#ff0000' }]
    ]);

    const parsed = parseScarTreeJava('ScarTreeLayout.java', source, metadata);

    expect(parsed.tree.nodes).toEqual([
      {
        id: 'hemomancy:scar_heart',
        displayName: 'Scar of the Heart',
        tendency: 'ANIMUS', tier: 1, color: '#ff0000',
        treeX: 480, treeY: 210, parents: []
      },
      {
        id: 'hemomancy:scar_marrow',
        displayName: 'Scar of Marrow',
        tendency: 'ANIMUS', tier: 2, color: '#ff0000',
        treeX: 480, treeY: 160, parents: ['hemomancy:scar_heart']
      }
    ]);
  });

  it('rewrites only edited coordinates and parent arguments', () => {
    const parsed = parseScarTreeJava('ScarTreeLayout.java', source, new Map());
    const updates = new Map([
      ['hemomancy:scar_marrow', { treeX: 455, treeY: 135, parents: [] }]
    ]);

    const rendered = renderScarTreeJava(source, parsed.parsedNodes, updates);

    expect(rendered).toContain('authored("hemomancy:scar_heart", 480, 210)');
    expect(rendered).toContain('authored("hemomancy:scar_marrow", 455, 135)');
    expect(rendered).not.toContain('455, 135, "hemomancy:scar_heart"');
  });

  it('reports unknown lineage targets', () => {
    const invalid = source.replace('"hemomancy:scar_heart")', '"hemomancy:missing")');
    const parsed = parseScarTreeJava('ScarTreeLayout.java', invalid, new Map());

    expect(parsed.tree.diagnostics).toContainEqual(expect.objectContaining({
      code: 'scar_tree_unknown_parent',
      skill: 'hemomancy:scar_marrow'
    }));
  });
});
