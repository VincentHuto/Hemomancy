import ELK from 'elkjs/lib/elk.bundled.js';
import type { DialogueTreeModel } from '../shared/types';

export interface StoredPosition { x: number; y: number; pinned?: boolean }

const elk = new ELK();

export async function layoutTree(
  tree: DialogueTreeModel,
  stored: Record<string, StoredPosition>
): Promise<Record<string, StoredPosition>> {
  const graph = await elk.layout({
    id: 'root',
    layoutOptions: {
      'elk.algorithm': 'layered',
      'elk.direction': 'RIGHT',
      'elk.spacing.nodeNode': '80',
      'elk.layered.spacing.nodeNodeBetweenLayers': '150',
      'elk.layered.nodePlacement.strategy': 'NETWORK_SIMPLEX'
    },
    children: tree.nodes.map(node => ({
      id: node.id,
      width: 390,
      height: Math.max(190, 126 + node.lines.length * 58 + node.options.length * 52)
    })),
    edges: tree.nodes.flatMap(node => node.options.flatMap((option, index) => option.next ? [{
      id: `${node.id}-${index}-${option.next}`,
      sources: [node.id],
      targets: [option.next]
    }] : []))
  });

  return Object.fromEntries((graph.children ?? []).map(node => {
    const pinned = stored[node.id]?.pinned ? stored[node.id] : null;
    return [node.id, pinned ?? { x: node.x ?? 0, y: node.y ?? 0, pinned: false }];
  }));
}
