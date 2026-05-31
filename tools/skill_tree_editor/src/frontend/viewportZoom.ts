export interface ZoomScrollAnchor {
  scrollLeft: number;
  scrollTop: number;
}

export function clampZoom(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

export function zoomScrollAnchor(
  currentZoom: number,
  nextZoom: number,
  viewportX: number,
  viewportY: number,
  scrollLeft: number,
  scrollTop: number
): ZoomScrollAnchor {
  const graphX = (scrollLeft + viewportX) / currentZoom;
  const graphY = (scrollTop + viewportY) / currentZoom;
  return {
    scrollLeft: graphX * nextZoom - viewportX,
    scrollTop: graphY * nextZoom - viewportY
  };
}
