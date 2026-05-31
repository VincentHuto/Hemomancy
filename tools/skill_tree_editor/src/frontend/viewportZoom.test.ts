import { clampZoom, zoomScrollAnchor } from './viewportZoom';

test('clamps graph zoom between configured bounds', () => {
  expect(clampZoom(0.2, 0.5, 2.5)).toBe(0.5);
  expect(clampZoom(3, 0.5, 2.5)).toBe(2.5);
  expect(clampZoom(1.25, 0.5, 2.5)).toBe(1.25);
});

test('keeps the same graph point under the cursor when zooming', () => {
  const anchor = zoomScrollAnchor(1, 2, 200, 100, 400, 300);

  expect(anchor).toEqual({
    scrollLeft: 1000,
    scrollTop: 700
  });
});
