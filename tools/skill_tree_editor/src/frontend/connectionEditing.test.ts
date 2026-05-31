import { beginConnectionDrag, finishConnectionDrag } from './connectionEditing';

test('ctrl-dragging from one node to another proposes a parent rewire', () => {
  const drag = beginConnectionDrag('skill_crimson_projection');

  expect(finishConnectionDrag(drag, 'skill_vascular_draw')).toEqual({
    field: 'skill_crimson_projection',
    parentField: 'skill_vascular_draw'
  });
});

test('connection drags ignore empty or self targets', () => {
  const drag = beginConnectionDrag('skill_crimson_projection');

  expect(finishConnectionDrag(drag, null)).toBeUndefined();
  expect(finishConnectionDrag(drag, 'skill_crimson_projection')).toBeUndefined();
});
