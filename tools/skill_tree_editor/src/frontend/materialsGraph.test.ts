import { layoutMaterialAtlasPath, materialPositionFromDrag, sanitizeMaterialParents } from './materialsGraph';
import type { MaterialAtlasPathModel } from '../shared/types';

const pathModel: MaterialAtlasPathModel = {
  path: 'HARBINGER',
  buckets: [
    {
      path: 'HARBINGER',
      id: 'bloodcraft_core',
      label: 'Bloodcraft Core',
      rootMaterialId: 'sanguine_formation',
      color: '0xFFD04436',
      centerX: 520,
      centerY: 230,
      plaqueX: 520,
      plaqueY: 170
    }
  ],
  entries: [
    {
      path: 'HARBINGER',
      id: 'sanguine_formation',
      bucketId: 'bloodcraft_core',
      gate: { type: 'ALWAYS', value: null },
      order: 0,
      parentIds: [],
      nodeX: 610,
      nodeY: 300
    },
    {
      path: 'HARBINGER',
      id: 'blood_crystal_shard',
      bucketId: 'bloodcraft_core',
      gate: { type: 'DEGREE', value: 2 },
      order: 1,
      parentIds: ['sanguine_formation'],
      nodeX: null,
      nodeY: null
    }
  ]
};

test('material graph layout exposes nodes bucket roots label plaques traces and icon assets', () => {
  const layout = layoutMaterialAtlasPath(pathModel);

  expect(layout.nodes.find(node => node.id === 'sanguine_formation')).toEqual(expect.objectContaining({
    id: 'sanguine_formation',
    x: 520,
    y: 230,
    explicit: false,
    iconUrl: '/asset/item/sanguine_formation.png'
  }));
  expect(layout.nodes.find(node => node.id === 'blood_crystal_shard')).toEqual(expect.objectContaining({
    id: 'blood_crystal_shard',
    y: expect.any(Number)
  }));
  expect(layout.nodes.find(node => node.id === 'blood_crystal_shard')!.y).toBeLessThan(230);
  expect(layout.bucketRoots).toEqual(expect.arrayContaining([
    expect.objectContaining({ id: 'bloodcraft_core', x: 520, y: 230, color: '0xFFD04436' })
  ]));
  expect(layout.labelPlaques).toEqual(expect.arrayContaining([
    expect.objectContaining({ id: 'bloodcraft_core', label: 'Bloodcraft Core', x: 520, y: 170 })
  ]));
  expect(layout.traces.some(trace => trace.fromId === 'sanguine_formation' && trace.toId === 'blood_crystal_shard')).toBe(true);
});

test('material drag helper preserves model coordinates with zoom and snapping', () => {
  const position = materialPositionFromDrag({
    origin: { x: 610, y: 300 },
    pointerStart: { x: 200, y: 160 },
    pointerCurrent: { x: 260, y: 210 },
    scrollStart: { x: 100, y: 80 },
    scrollCurrent: { x: 120, y: 90 },
    zoom: 2,
    snap: 8
  });

  expect(position).toEqual({ x: 650, y: 332 });
});

test('parent editing removes self parents duplicates and unknown ids', () => {
  expect(sanitizeMaterialParents('blood_crystal_shard', [
    'sanguine_formation',
    'blood_crystal_shard',
    'missing',
    'sanguine_formation'
  ], new Set(['sanguine_formation', 'blood_crystal_shard']))).toEqual(['sanguine_formation']);
});
