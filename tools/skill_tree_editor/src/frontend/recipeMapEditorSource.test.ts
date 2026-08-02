import { readFileSync } from 'node:fs';

const source = readFileSync(new URL('./recipeMaps.ts', import.meta.url), 'utf8');

test('wires recipe map zoom controls and pointer-centered wheel zoom', () => {
  expect(source).toContain('data-action="zoom-out"');
  expect(source).toContain('data-action="zoom-reset"');
  expect(source).toContain('data-action="zoom-in"');
  expect(source).toContain("scroller.addEventListener('wheel'");
  expect(source).toContain('zoomScrollAnchor(');
});

test('wires background panning node dragging and shift marquee selection', () => {
  expect(source).toContain("scroller.addEventListener('pointerdown'");
  expect(source).toContain('shouldStartDragPan(event.target)');
  expect(source).toContain('event.shiftKey');
  expect(source).toContain('selectedIds');
  expect(source).toContain('moveRecipeMapEntries(');
});

test('offers registry-backed item and block display icon selectors', () => {
  expect(source).toContain('data-entry-edit="iconSource"');
  expect(source).toContain('data-entry-edit="iconItem"');
  expect(source).toContain('workspace?.iconOptions.blocks');
  expect(source).toContain('workspace?.iconOptions.items');
  expect(source).toContain('/asset/${sourceType}/');
});
