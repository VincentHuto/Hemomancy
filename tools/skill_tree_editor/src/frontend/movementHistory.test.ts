import {
  createMovementHistory,
  recordDegreeLabelMovement,
  recordMovement,
  recordMovements,
  recordSkillEdit,
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

test('undoes and redoes inspector field edits', () => {
  const history = createMovementHistory();

  recordSkillEdit(history, {
    fieldBefore: 'skill_crimson_projection',
    fieldAfter: 'skill_crimson_projection',
    key: 'parentField',
    before: 'skill_vascular_draw',
    after: 'skill_living_conduit'
  });

  expect(undoMovement(history)?.updates).toEqual([{
    field: 'skill_crimson_projection',
    edit: { key: 'parentField', value: 'skill_vascular_draw' }
  }]);
  expect(history.canUndo).toBe(false);
  expect(history.canRedo).toBe(true);

  expect(redoMovement(history)?.updates).toEqual([{
    field: 'skill_crimson_projection',
    edit: { key: 'parentField', value: 'skill_living_conduit' }
  }]);
  expect(history.canUndo).toBe(true);
  expect(history.canRedo).toBe(false);
});

test('undoes and redoes parent list edits', () => {
  const history = createMovementHistory();

  recordSkillEdit(history, {
    fieldBefore: 'skill_crimson_projection',
    fieldAfter: 'skill_crimson_projection',
    key: 'parentFields',
    before: ['skill_living_conduit'],
    after: ['skill_living_conduit', 'skill_vascular_draw']
  });

  expect(undoMovement(history)?.updates).toEqual([{
    field: 'skill_crimson_projection',
    edit: { key: 'parentFields', value: ['skill_living_conduit'] }
  }]);

  expect(redoMovement(history)?.updates).toEqual([{
    field: 'skill_crimson_projection',
    edit: { key: 'parentFields', value: ['skill_living_conduit', 'skill_vascular_draw'] }
  }]);
});

test('undoes and redoes degree label movements', () => {
  const history = createMovementHistory();

  recordDegreeLabelMovement(history, {
    degree: 4,
    before: { x: 90, y: 160 },
    after: { x: 188, y: 244 }
  });

  expect(undoMovement(history)?.updates).toEqual([{
    degreeLabel: {
      degree: 4,
      position: { x: 90, y: 160 }
    }
  }]);
  expect(redoMovement(history)?.updates).toEqual([{
    degreeLabel: {
      degree: 4,
      position: { x: 188, y: 244 }
    }
  }]);
});

test('undoes and redoes field renames using the current field as the lookup key', () => {
  const history = createMovementHistory();

  recordSkillEdit(history, {
    fieldBefore: 'skill_old',
    fieldAfter: 'skill_new',
    key: 'field',
    before: 'skill_old',
    after: 'skill_new'
  });

  expect(undoMovement(history)?.updates).toEqual([{
    field: 'skill_new',
    edit: { key: 'field', value: 'skill_old' }
  }]);

  expect(redoMovement(history)?.updates).toEqual([{
    field: 'skill_old',
    edit: { key: 'field', value: 'skill_new' }
  }]);
});
