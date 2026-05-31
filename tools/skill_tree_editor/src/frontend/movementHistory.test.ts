import {
  createMovementHistory,
  recordMovement,
  recordMovements,
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
  expect(undo?.updates).toEqual([{
    field: 'skill_sanguine_reach',
    position: { x: 128, y: 256 }
  }]);
  expect(history.canUndo).toBe(false);
  expect(history.canRedo).toBe(true);

  const redo = redoMovement(history);
  expect(redo?.updates).toEqual([{
    field: 'skill_sanguine_reach',
    position: { x: 160, y: 288 }
  }]);
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

test('undoes and redoes a batch of auto-layout movements as one history step', () => {
  const history = createMovementHistory();

  recordMovements(history, [
    {
      field: 'base_skill',
      before: { x: 360, y: 424 },
      after: { x: 480, y: 480 }
    },
    {
      field: 'skill_capacity',
      before: { x: 270, y: 424 },
      after: { x: 439, y: 408 }
    }
  ]);

  expect(undoMovement(history)?.updates).toEqual([
    { field: 'base_skill', position: { x: 360, y: 424 } },
    { field: 'skill_capacity', position: { x: 270, y: 424 } }
  ]);
  expect(redoMovement(history)?.updates).toEqual([
    { field: 'base_skill', position: { x: 480, y: 480 } },
    { field: 'skill_capacity', position: { x: 439, y: 408 } }
  ]);
});
