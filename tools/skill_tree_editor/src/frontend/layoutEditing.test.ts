import {
  beginNodeDrag,
  manipulationRingMetricsFromClusterSize,
  modelPositionFromRenderedPosition,
  updateNodeDrag
} from './layoutEditing';

test('moves a node in canvas coordinates while preserving pointer offset', () => {
  const drag = beginNodeDrag({ clientX: 240, clientY: 160, nodeX: 200, nodeY: 120, scrollLeft: 30, scrollTop: 10 });

  const position = updateNodeDrag(drag, { clientX: 300, clientY: 220, scrollLeft: 30, scrollTop: 10, snap: 1 });

  expect(position).toEqual({ x: 260, y: 180 });
});

test('snaps dragged node movement to the configured grid', () => {
  const drag = beginNodeDrag({ clientX: 240, clientY: 160, nodeX: 200, nodeY: 120, scrollLeft: 0, scrollTop: 0 });

  const position = updateNodeDrag(drag, { clientX: 299, clientY: 213, scrollLeft: 0, scrollTop: 0, snap: 16 });

  expect(position).toEqual({ x: 264, y: 168 });
});

test('moves nodes in graph coordinates while the preview is zoomed', () => {
  const drag = beginNodeDrag({ clientX: 200, clientY: 160, nodeX: 100, nodeY: 80, scrollLeft: 0, scrollTop: 0, zoom: 2 });

  const position = updateNodeDrag(drag, { clientX: 240, clientY: 200, scrollLeft: 0, scrollTop: 0, snap: 1, zoom: 2 });

  expect(position).toEqual({ x: 120, y: 100 });
});

test('snaps drag deltas without forcing unsnapped node origins onto the grid', () => {
  const drag = beginNodeDrag({ clientX: 360, clientY: 424, nodeX: 360, nodeY: 424, scrollLeft: 0, scrollTop: 0 });

  const position = updateNodeDrag(drag, { clientX: 360, clientY: 456, scrollLeft: 0, scrollTop: 0, snap: 16 });

  expect(position).toEqual({ x: 360, y: 456 });
});

test('maps rendered manipulation drag movement back to model coordinates with the same direction', () => {
  const model = modelPositionFromRenderedPosition(
    { x: 610, y: 440 },
    {
      anchorX: 500,
      anchorY: 300,
      clusterCenterX: 2100,
      clusterCenterY: 120
    }
  );

  expect(model).toEqual({ x: 2210, y: 260 });
});

test('can reuse manipulation ring metrics while a dragged node expands cluster bounds', () => {
  const loadedMetrics = manipulationRingMetricsFromClusterSize(266, 236);
  const expandedDuringDrag = manipulationRingMetricsFromClusterSize(266, 539, loadedMetrics);

  expect(expandedDuringDrag).toEqual(loadedMetrics);
});
