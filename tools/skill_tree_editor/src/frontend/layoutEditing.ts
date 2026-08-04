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

export interface RenderedPositionFrame {
  anchorX: number;
  anchorY: number;
  clusterCenterX: number;
  clusterCenterY: number;
}

export interface ManipulationRingMetrics {
  clusterHalf: number;
  branchRadius: number;
  ringCenterRawX: number;
  ringCenterRawY: number;
}

export interface ManipulationRingMetricOptions {
  padding?: number;
  minRadius?: number;
  radiusScale?: number;
}

export interface CompactManipulationRingMetricOptions {
  padding?: number;
  innerRadius?: number;
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

export function modelPositionFromRenderedPosition(
  position: NodePosition,
  frame: RenderedPositionFrame,
  sourceCoordinateScale = 1
): NodePosition {
  return {
    x: Math.round((position.x - frame.anchorX) / sourceCoordinateScale) + frame.clusterCenterX,
    y: Math.round((position.y - frame.anchorY) / sourceCoordinateScale) + frame.clusterCenterY
  };
}

export function manipulationRingMetricsFromClusterSize(
  maxClusterW: number,
  maxClusterH: number,
  stableMetrics?: ManipulationRingMetrics,
  options: ManipulationRingMetricOptions = {}
): ManipulationRingMetrics {
  if (stableMetrics) return stableMetrics;
  const padding = options.padding ?? 40;
  const minRadius = options.minRadius ?? 250;
  const radiusScale = options.radiusScale ?? 1.35;
  const clusterSize = Math.max(maxClusterW, maxClusterH);
  const clusterHalf = clusterSize / 2;
  const branchRadius = Math.max(minRadius, Math.ceil(clusterSize * radiusScale));
  return {
    clusterHalf,
    branchRadius,
    ringCenterRawX: padding + clusterHalf + branchRadius,
    ringCenterRawY: padding + clusterHalf + branchRadius
  };
}

export function manipulationRingMetricsFromRadialOffsets(
  maxClusterW: number,
  maxClusterH: number,
  localRadialOffsets: number[],
  options: CompactManipulationRingMetricOptions = {},
  stableMetrics?: ManipulationRingMetrics
): ManipulationRingMetrics {
  if (stableMetrics) return stableMetrics;
  const padding = options.padding ?? 40;
  const innerRadius = options.innerRadius ?? 175;
  const clusterHalf = Math.max(maxClusterW, maxClusterH) / 2;
  const innermostOffset = localRadialOffsets.length ? Math.min(...localRadialOffsets) : 0;
  const branchRadius = Math.max(0, Math.ceil(innerRadius - innermostOffset));
  return {
    clusterHalf,
    branchRadius,
    ringCenterRawX: padding + clusterHalf + branchRadius,
    ringCenterRawY: padding + clusterHalf + branchRadius
  };
}

function snapFromOrigin(value: number, origin: number, grid: number): number {
  if (grid <= 1) return Math.round(value);
  return origin + Math.round((value - origin) / grid) * grid;
}
