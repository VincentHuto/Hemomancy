export interface NodeDragStart {
  clientX: number;
  clientY: number;
  nodeX: number;
  nodeY: number;
  scrollLeft: number;
  scrollTop: number;
  zoom?: number;
}

export interface NodeDragState {
  pointerOffsetX: number;
  pointerOffsetY: number;
  originX: number;
  originY: number;
}

export interface NodeDragUpdate {
  clientX: number;
  clientY: number;
  scrollLeft: number;
  scrollTop: number;
  snap: number;
  zoom?: number;
}

export interface NodePosition {
  x: number;
  y: number;
}

export function beginNodeDrag(start: NodeDragStart): NodeDragState {
  const zoom = start.zoom ?? 1;
  return {
    pointerOffsetX: (start.clientX + start.scrollLeft) / zoom - start.nodeX,
    pointerOffsetY: (start.clientY + start.scrollTop) / zoom - start.nodeY,
    originX: start.nodeX,
    originY: start.nodeY
  };
}

export function updateNodeDrag(state: NodeDragState, update: NodeDragUpdate): NodePosition {
  const zoom = update.zoom ?? 1;
  const rawX = (update.clientX + update.scrollLeft) / zoom - state.pointerOffsetX;
  const rawY = (update.clientY + update.scrollTop) / zoom - state.pointerOffsetY;
  return {
    x: snapFromOrigin(rawX, state.originX, update.snap),
    y: snapFromOrigin(rawY, state.originY, update.snap)
  };
}

function snapFromOrigin(value: number, origin: number, grid: number): number {
  if (grid <= 1) return Math.round(value);
  return origin + Math.round((value - origin) / grid) * grid;
}
