import { describe, expect, test } from 'vitest';
import type { DialogueTreeModel } from '../shared/types';
import { layoutTree } from './studioLayout';

const tree: DialogueTreeModel = {
  method: 'main', visibility: 'public', params: [], theme: 'BLOOD', startNode: 'root', nodes: [
    { id: 'root', lines: [], options: [{ text: 'a', next: 'left', event: null }, { text: 'b', next: 'right', event: null }] },
    { id: 'left', lines: [], options: [] },
    { id: 'right', lines: [], options: [] }
  ]
};

describe('studio graph layout', () => {
  test('lays destinations to the right of their source', async () => {
    const positions = await layoutTree(tree, {});
    expect(positions.left.x).toBeGreaterThan(positions.root.x);
    expect(positions.right.x).toBeGreaterThan(positions.root.x);
  });

  test('retains explicitly pinned positions', async () => {
    const positions = await layoutTree(tree, { left: { x: 44, y: 77, pinned: true } });
    expect(positions.left).toEqual({ x: 44, y: 77, pinned: true });
  });
});
