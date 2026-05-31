import type { NodePosition } from './layoutEditing';

export interface MovementChange {
  field: string;
  before: NodePosition;
  after: NodePosition;
}

export interface MovementHistory {
  undoStack: MovementChange[][];
  redoStack: MovementChange[][];
  canUndo: boolean;
  canRedo: boolean;
}

export interface MovementTargetUpdate {
  field: string;
  position: NodePosition;
}

export interface MovementTarget {
  updates: MovementTargetUpdate[];
}

export function createMovementHistory(): MovementHistory {
  return {
    undoStack: [],
    redoStack: [],
    canUndo: false,
    canRedo: false
  };
}

export function recordMovement(history: MovementHistory, change: MovementChange): void {
  recordMovements(history, [change]);
}

export function recordMovements(history: MovementHistory, changes: MovementChange[]): void {
  const meaningfulChanges = changes.filter(change => !samePosition(change.before, change.after));
  if (!meaningfulChanges.length) return;
  history.undoStack.push(meaningfulChanges);
  history.redoStack = [];
  refreshFlags(history);
}

export function undoMovement(history: MovementHistory): MovementTarget | undefined {
  const changes = history.undoStack.pop();
  if (!changes) return undefined;
  history.redoStack.push(changes);
  refreshFlags(history);
  return {
    updates: changes.map(change => ({
      field: change.field,
      position: change.before
    }))
  };
}

export function redoMovement(history: MovementHistory): MovementTarget | undefined {
  const changes = history.redoStack.pop();
  if (!changes) return undefined;
  history.undoStack.push(changes);
  refreshFlags(history);
  return {
    updates: changes.map(change => ({
      field: change.field,
      position: change.after
    }))
  };
}

function samePosition(left: NodePosition, right: NodePosition): boolean {
  return left.x === right.x && left.y === right.y;
}

function refreshFlags(history: MovementHistory): void {
  history.canUndo = history.undoStack.length > 0;
  history.canRedo = history.redoStack.length > 0;
}
