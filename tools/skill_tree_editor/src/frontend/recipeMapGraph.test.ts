import {
  layoutRecipeMap,
  moveRecipeMapEntries,
  recipeMapDragDelta,
  recipeMapSelectionIdsInRect
} from './recipeMapGraph';
import type { RecipeMapEditorTab } from '../shared/types';

const tab: RecipeMapEditorTab = {
  key: 'RITES',
  families: ['Order', 'Vessel', 'Miscellaneous'],
  entries: [
    { id: 'cardinal_rite/init', family: 'Order', order: 0, column: 0, displayName: 'Init' },
    { id: 'cardinal_rite/votary', family: 'Order', order: 1, column: 0, displayName: 'Votary' },
    { id: 'cardinal_rite/vessel', family: 'Vessel', order: 0, column: 3, displayName: 'Vessel' },
    { id: 'cardinal_rite/deep', family: 'Vessel', order: 1, column: 6, displayName: 'Deep' }
  ],
  links: [
    { from: 'cardinal_rite/init', to: 'cardinal_rite/votary', kind: 'PROGRESSION' },
    { from: 'cardinal_rite/vessel', to: 'cardinal_rite/deep', kind: 'CONCEPTUAL' }
  ]
};

test('matches recipe map family sectors and surface degree rings', () => {
  const layout = layoutRecipeMap(tab, 'surface');

  expect(layout.nodes.map(node => node.id)).toEqual([
    'cardinal_rite/init', 'cardinal_rite/votary', 'cardinal_rite/vessel'
  ]);
  expect(layout.nodes.find(node => node.id === 'cardinal_rite/vessel')).toEqual(expect.objectContaining({ x: 480, y: 700 }));
  expect(layout.links).toHaveLength(1);
  expect(layout.links[0].kind).toBe('PROGRESSION');
});

test('maps deep degrees onto their compact in-game radial degrees', () => {
  const layout = layoutRecipeMap(tab, 'deep');
  expect(layout.nodes).toEqual([
    expect.objectContaining({ id: 'cardinal_rite/deep', x: 480, y: 650 })
  ]);
  expect(layout.links).toEqual([]);
});

test('uses authored coordinates when a recipe map node has been moved', () => {
  const moved = structuredClone(tab);
  moved.entries[0].treeX = 612;
  moved.entries[0].treeY = 344;

  expect(layoutRecipeMap(moved, 'surface').nodes[0]).toEqual(expect.objectContaining({ x: 612, y: 344 }));
});

test('selects recipe map nodes inside a marquee regardless of drag direction', () => {
  const nodes = layoutRecipeMap(tab, 'surface').nodes;
  const target = nodes.find(node => node.id === 'cardinal_rite/vessel')!;

  expect(recipeMapSelectionIdsInRect(nodes, { x: target.x + 20, y: target.y + 20 }, { x: target.x - 20, y: target.y - 20 }))
    .toEqual(['cardinal_rite/vessel']);
});

test('moves every selected recipe map entry by the same graph-space delta', () => {
  const entries = structuredClone(tab.entries);
  const layout = layoutRecipeMap({ ...tab, entries }, 'surface');
  moveRecipeMapEntries(entries, layout.nodes, new Set(['cardinal_rite/init', 'cardinal_rite/votary']), 24, -16);

  expect(entries.slice(0, 2).map(entry => ({ x: entry.treeX, y: entry.treeY }))).toEqual([
    { x: layout.nodes[0].x + 24, y: layout.nodes[0].y - 16 },
    { x: layout.nodes[1].x + 24, y: layout.nodes[1].y - 16 }
  ]);
  expect(entries[2].treeX).toBeUndefined();
});

test('does not author a position until pointer movement clears the drag threshold', () => {
  expect(recipeMapDragDelta({ x: 100, y: 100 }, { x: 103, y: 102 }, 1)).toBeUndefined();
  expect(recipeMapDragDelta({ x: 100, y: 100 }, { x: 110, y: 106 }, 2)).toEqual({ x: 5, y: 3 });
});

test('keeps moved recipe map nodes fully inside runtime content bounds', () => {
  const entries = structuredClone(tab.entries);
  const layout = layoutRecipeMap({ ...tab, entries }, 'surface');

  moveRecipeMapEntries(entries, layout.nodes, new Set(['cardinal_rite/init']), -2000, 2000);

  expect(entries[0]).toEqual(expect.objectContaining({ treeX: 14, treeY: 1016 }));
});
