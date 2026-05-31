export interface GraphViewport {
  scrollLeft: number;
  scrollTop: number;
}

export function captureGraphViewport(scroller: Pick<HTMLElement, 'scrollLeft' | 'scrollTop'>): GraphViewport {
  return {
    scrollLeft: scroller.scrollLeft,
    scrollTop: scroller.scrollTop
  };
}

export function restoreGraphViewport(scroller: Pick<HTMLElement, 'scrollLeft' | 'scrollTop'>, viewport: GraphViewport): void {
  scroller.scrollLeft = Math.max(0, viewport.scrollLeft);
  scroller.scrollTop = Math.max(0, viewport.scrollTop);
}
