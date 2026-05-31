import type { NodePosition } from './layoutEditing';

export type SkillEditValue = string | number | null;

export interface MovementChange {
  type?: 'position';
  field: string;
  before: NodePosition;
  after: NodePosition;
}

export interface SkillEditChange {
  type?: 'edit';
  fieldBefore: string;
  fieldAfter: string;
  key: string;
  before: SkillEditValue;
  after: SkillEditValue;
}

export type HistoryChange = MovementChange | SkillEditChange;

export interface MovementHistory {
  undoStack: HistoryChange[][];
  redoStack: HistoryChange[][];
  canUndo: boolean;
  canRedo: boolean;
}

export interface MovementTargetUpdate {
  field: string;
  position?: NodePosition;
  edit?: {
    key: string;
    value: SkillEditValue;
  };
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

export function recordSkillEdit(history: MovementHistory, change: SkillEditChange): void {
  if (sameEditValue(change.before, change.after)) return;
  history.undoStack.push([change]);
  history.redoStack = [];
  refreshFlags(history);
}

export function undoMovement(history: MovementHistory): MovementTarget | undefined {
  const changes = history.undoStack.pop();
  if (!changes) return undefined;
  history.redoStack.push(changes);
  refreshFlags(history);
  return {
    updates: changes.map(change => changeToTargetUpdate(change, 'undo'))
  };
}

export function redoMovement(history: MovementHistory): MovementTarget | undefined {
  const changes = history.redoStack.pop();
  if (!changes) return undefined;
  history.undoStack.push(changes);
  refreshFlags(history);
  return {
    updates: changes.map(change => changeToTargetUpdate(change, 'redo'))
  };
}

function changeToTargetUpdate(change: HistoryChange, direction: 'undo' | 'redo'): MovementTargetUpdate {
  if (isSkillEditChange(change)) {
    return {
      field: direction === 'undo' ? change.fieldAfter : change.fieldBefore,
      edit: {
        key: change.key,
        value: direction === 'undo' ? change.before : change.after
      }
    };
  }
  return {
    field: change.field,
    position: direction === 'undo' ? change.before : change.after
  };
}

function isSkillEditChange(change: HistoryChange): change is SkillEditChange {
  return change.type === 'edit' || 'key' in change;
}

function samePosition(left: NodePosition, right: NodePosition): boolean {
  return left.x === right.x && left.y === right.y;
}

function sameEditValue(left: SkillEditValue, right: SkillEditValue): boolean {
  return left === right;
}

function refreshFlags(history: MovementHistory): void {
  history.canUndo = history.undoStack.length > 0;
  history.canRedo = history.redoStack.length > 0;
}
