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

test('does not start panning from skill nodes or form controls', () => {
  const element = (blocked: boolean) => ({
    closest: (_selector: string) => blocked ? {} : null
  }) as unknown as EventTarget;

  expect(shouldStartDragPan(element(false))).toBe(true);
  expect(shouldStartDragPan(element(true))).toBe(false);
});
