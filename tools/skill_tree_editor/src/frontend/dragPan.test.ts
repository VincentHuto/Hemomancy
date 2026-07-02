import { beginDragPan, shouldStartDragPan, updateDragPan } from './dragPan';

test('converts pointer movement into inverse scroll movement', () => {
  const state = beginDragPan(120, 80, 400, 250);

  const update = updateDragPan(state, 90, 110);

  expect(update).toEqual({
    scrollLeft: 430,
    scrollTop: 220,
    moved: true
  });
});

test('does not start panning from graph nodes or form controls', () => {
  const element = (className: string | null) => ({
    closest: (selector: string) => className && selector.includes(className) ? {} : null
  }) as unknown as EventTarget;

  expect(shouldStartDragPan(element(null))).toBe(true);
  expect(shouldStartDragPan(element('skill-node'))).toBe(false);
  expect(shouldStartDragPan(element('material-node'))).toBe(false);
  expect(shouldStartDragPan(element('atlas-hub'))).toBe(false);
  expect(shouldStartDragPan(element('category-anchor'))).toBe(false);
  expect(shouldStartDragPan(element('label-plaque'))).toBe(false);
  expect(shouldStartDragPan(element('button'))).toBe(false);
});
