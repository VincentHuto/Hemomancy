export interface ConnectionDragState {
  sourceField: string;
}

export interface ConnectionRewire {
  field: string;
  parentField: string;
}

export function beginConnectionDrag(sourceField: string): ConnectionDragState {
  return { sourceField };
}

export function finishConnectionDrag(
  state: ConnectionDragState,
  targetField: string | null | undefined
): ConnectionRewire | undefined {
  if (!targetField || targetField === state.sourceField) return undefined;
  return {
    field: targetField,
    parentField: state.sourceField
  };
}
