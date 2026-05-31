import type { NodePosition } from './layoutEditing';

export interface MovementChange {
  field: string;
  before: NodePosition;
  after: NodePosition;
}

export interface MovementHistory {
  undoStack: MovementChange[];
  redoStack: MovementChange[];
  canUndo: boolean;
  canRedo: boolean;
}

export interface MovementTarget {
  field: string;
  position: NodePosition;
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
  if (samePosition(change.before, change.after)) return;
  history.undoStack.push(change);
  history.redoStack = [];
  refreshFlags(history);
}

export function undoMovement(history: MovementHistory): MovementTarget | undefined {
  const change = history.undoStack.pop();
  if (!change) return undefined;
  history.redoStack.push(change);
  refreshFlags(history);
  return {
    field: change.field,
    position: change.before
  };
}

export function redoMovement(history: MovementHistory): MovementTarget | undefined {
  const change = history.redoStack.pop();
  if (!change) return undefined;
  history.undoStack.push(change);
  refreshFlags(history);
  return {
    field: change.field,
    position: change.after
  };
}

function samePosition(left: NodePosition, right: NodePosition): boolean {
  return left.x === right.x && left.y === right.y;
}

function refreshFlags(history: MovementHistory): void {
  history.canUndo = history.undoStack.length > 0;
  history.canRedo = history.redoStack.length > 0;
}

