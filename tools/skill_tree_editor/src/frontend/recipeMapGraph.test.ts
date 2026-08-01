import { layoutRecipeMap } from './recipeMapGraph';
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
