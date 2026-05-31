import { beginConnectionDrag, finishConnectionDrag } from './connectionEditing';

test('ctrl-dragging from parent to child proposes adding the source as a parent', () => {
  const drag = beginConnectionDrag('skill_vascular_draw');

  expect(finishConnectionDrag(drag, 'skill_crimson_projection')).toEqual({
    field: 'skill_crimson_projection',
    parentField: 'skill_vascular_draw'
  });
});

test('connection drags ignore empty or self targets', () => {
  const drag = beginConnectionDrag('skill_crimson_projection');

  expect(finishConnectionDrag(drag, null)).toBeUndefined();
  expect(finishConnectionDrag(drag, 'skill_crimson_projection')).toBeUndefined();
});
