import {
  alignMaterialPositionToNodes,
  layoutMaterialAtlasPath,
  materialModelPositionFromRendered,
  materialPositionFromDrag,
  sanitizeMaterialParents
} from './materialsGraph';
import type { MaterialAtlasPathModel } from '../shared/types';

const pathModel: MaterialAtlasPathModel = {
  path: 'HARBINGER',
  hubX: 686,
  hubY: 617,
  hubLabelX: 686,
  hubLabelY: 617,
  buckets: [
    {
      path: 'HARBINGER',
      id: 'bloodcraft_core',
      label: 'Bloodcraft Core',
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
    x: 610,
    y: 300,
    explicit: true,
    iconUrl: '/asset/item/sanguine_formation.png'
  }));
  expect(layout.nodes.find(node => node.id === 'blood_crystal_shard')).toEqual(expect.objectContaining({
    id: 'blood_crystal_shard',
    y: expect.any(Number)
  }));
  expect(layout.nodes.find(node => node.id === 'blood_crystal_shard')!.y).toBeLessThan(230);
  expect(layout.hub).toEqual(expect.objectContaining({
    id: 'atlas-hub',
    label: 'Blood Atlas',
    x: 686,
    y: 617,
    color: '0xFFCC6644'
  }));
  expect(layout.viewBoxX).toBe(0);
  expect(layout.viewBoxY).toBe(0);
  expect(layout.width).toBeGreaterThan(layout.hub.x);
  expect(layout.height).toBeGreaterThan(layout.hub.y);
  expect(layout.categoryAnchors).toEqual(expect.arrayContaining([
    expect.objectContaining({ id: 'bloodcraft_core', x: 520, y: 230, color: '0xFFD04436' })
  ]));
  expect(layout.labelPlaques).toEqual(expect.arrayContaining([
    expect.objectContaining({ id: 'bloodcraft_core', label: 'Bloodcraft Core', x: 520, y: 170 })
  ]));
  expect(layout.traces.some(trace => trace.fromId === 'sanguine_formation' && trace.toId === 'blood_crystal_shard')).toBe(true);
});

test('moving material atlas label does not move auto-positioned nodes', () => {
  const before = layoutMaterialAtlasPath(pathModel);
  const movedLabel = {
    ...pathModel,
    hubLabelX: pathModel.hubLabelX + 120,
    hubLabelY: pathModel.hubLabelY + 40
  };
  const after = layoutMaterialAtlasPath(movedLabel);

  expect(after.hub).toEqual(expect.objectContaining({
    x: movedLabel.hubLabelX,
    y: movedLabel.hubLabelY
  }));
  expect(after.nodes.map(node => [node.id, node.x, node.y])).toEqual(before.nodes.map(node => [node.id, node.x, node.y]));
});

test('material graph only draws explicit parent traces', () => {
  const layout = layoutMaterialAtlasPath(pathModel);
  const rootTrace = layout.traces.filter(trace => trace.fromId === 'sanguine_formation' && trace.toId === 'blood_crystal_shard');
  const categoryTrace = layout.traces.filter(trace => trace.fromId.startsWith('category:') || trace.toId.startsWith('category:'));

  expect(rootTrace).toHaveLength(1);
  expect(categoryTrace).toHaveLength(0);
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

test('material alignment snaps independently to nearby node rows and columns', () => {
  const aligned = alignMaterialPositionToNodes({
    x: 404,
    y: 258
  }, [
    { id: 'self', x: 400, y: 240 },
    { id: 'left_neighbor', x: 392, y: 260 },
    { id: 'upper_neighbor', x: 404, y: 112 }
  ], 'self');

  expect(aligned).toEqual({ x: 404, y: 260 });
});

test('material alignment respects separate x and y tolerances', () => {
  const aligned = alignMaterialPositionToNodes({
    x: 207,
    y: 207
  }, [
    { id: 'column', x: 200, y: 20 },
    { id: 'row', x: 20, y: 200 }
  ], 'self', { x: 8, y: 4 });

  expect(aligned).toEqual({ x: 200, y: 207 });
});

test('material alignment chooses the nearest matching axis candidate', () => {
  const aligned = alignMaterialPositionToNodes({
    x: 506,
    y: 298
  }, [
    { id: 'far_x', x: 498, y: 100 },
    { id: 'near_x', x: 504, y: 120 },
    { id: 'near_y', x: 900, y: 296 }
  ], 'self');

  expect(aligned).toEqual({ x: 504, y: 296 });
});

test('rendered material drag positions convert back through atlas layout offset', () => {
  const shiftedPath: MaterialAtlasPathModel = {
    ...pathModel,
    hubLabelX: pathModel.hubLabelX,
    hubLabelY: pathModel.hubLabelY,
    buckets: [{
      ...pathModel.buckets[0],
      centerX: -10,
      plaqueX: -20
    }],
    entries: [{
      ...pathModel.entries[0],
      nodeX: -30,
      nodeY: 120
    }]
  };
  const layout = layoutMaterialAtlasPath(shiftedPath);
  const node = layout.nodes.find(candidate => candidate.id === 'sanguine_formation')!;

  expect(layout.offsetX).toBeGreaterThan(0);
  expect(materialModelPositionFromRendered(layout, { x: node.x + 24, y: node.y + 16 })).toEqual({
    x: shiftedPath.entries[0].nodeX! + 24,
    y: shiftedPath.entries[0].nodeY! + 16
  });
});

test('parent editing removes self parents duplicates and unknown ids', () => {
  expect(sanitizeMaterialParents('blood_crystal_shard', [
    'sanguine_formation',
    'blood_crystal_shard',
    'missing',
    'sanguine_formation'
  ], new Set(['sanguine_formation', 'blood_crystal_shard']))).toEqual(['sanguine_formation']);
});
