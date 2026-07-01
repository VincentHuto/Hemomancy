export interface DragPanState {
  startX: number;
  startY: number;
  startScrollLeft: number;
  startScrollTop: number;
}

export interface DragPanUpdate {
  scrollLeft: number;
  scrollTop: number;
  moved: boolean;
}

const PAN_BLOCK_SELECTOR = '.skill-node, .material-node, .bucket-root, .label-plaque, button, input, select, textarea, a';

export function beginDragPan(
  clientX: number,
  clientY: number,
  scrollLeft: number,
  scrollTop: number
): DragPanState {
  return {
    startX: clientX,
    startY: clientY,
    startScrollLeft: scrollLeft,
    startScrollTop: scrollTop
  };
}

export function updateDragPan(state: DragPanState, clientX: number, clientY: number): DragPanUpdate {
  const deltaX = clientX - state.startX;
  const deltaY = clientY - state.startY;
  return {
    scrollLeft: state.startScrollLeft - deltaX,
    scrollTop: state.startScrollTop - deltaY,
    moved: Math.abs(deltaX) > 2 || Math.abs(deltaY) > 2
  };
}

export function shouldStartDragPan(target: EventTarget | null): boolean {
  const maybeElement = target as { closest?: (selector: string) => unknown } | null;
  return typeof maybeElement?.closest === 'function' && !maybeElement.closest(PAN_BLOCK_SELECTOR);
}
