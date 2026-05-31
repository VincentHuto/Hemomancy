import {
  createMovementHistory,
  recordMovement,
  redoMovement,
  undoMovement
} from './movementHistory';

test('undoes and redoes recorded node movements as single drag steps', () => {
  const history = createMovementHistory();

  recordMovement(history, {
    field: 'skill_sanguine_reach',
    before: { x: 128, y: 256 },
    after: { x: 160, y: 288 }
  });

  const undo = undoMovement(history);
  expect(undo?.field).toBe('skill_sanguine_reach');
  expect(undo?.position).toEqual({ x: 128, y: 256 });
  expect(history.canUndo).toBe(false);
  expect(history.canRedo).toBe(true);

  const redo = redoMovement(history);
  expect(redo?.field).toBe('skill_sanguine_reach');
  expect(redo?.position).toEqual({ x: 160, y: 288 });
  expect(history.canUndo).toBe(true);
  expect(history.canRedo).toBe(false);
});

test('drops redo movements when a new movement is recorded', () => {
  const history = createMovementHistory();

  recordMovement(history, {
    field: 'skill_one',
    before: { x: 0, y: 0 },
    after: { x: 16, y: 16 }
  });
  undoMovement(history);

  recordMovement(history, {
    field: 'skill_two',
    before: { x: 32, y: 32 },
    after: { x: 48, y: 48 }
  });

  expect(history.canUndo).toBe(true);
  expect(history.canRedo).toBe(false);
  expect(redoMovement(history)).toBeUndefined();
});

