import type { MaterialAtlasEntryModel, MaterialAtlasPathModel } from '../shared/types';

export interface MaterialGraphNode {
  id: string;
  bucketId: string;
  x: number;
  y: number;
  explicit: boolean;
  color: string;
  iconUrl: string;
  entry: MaterialAtlasEntryModel;
}

export interface MaterialGraphPoint {
  id: string;
  label: string;
  x: number;
  y: number;
  color: string;
}

export interface MaterialGraphTrace {
  fromId: string;
  toId: string;
  fromX: number;
  fromY: number;
  toX: number;
  toY: number;
  color: string;
  path: string;
}

export interface MaterialGraphHub {
  id: string;
  label: string;
  x: number;
  y: number;
  color: string;
}

export interface MaterialGraphLayout {
  width: number;
  height: number;
  viewBoxX: number;
  viewBoxY: number;
  offsetX: number;
  offsetY: number;
  hub: MaterialGraphHub;
  nodes: MaterialGraphNode[];
  categoryAnchors: MaterialGraphPoint[];
  labelPlaques: MaterialGraphPoint[];
  traces: MaterialGraphTrace[];
}

export interface MaterialDragRequest {
  origin: { x: number; y: number };
  pointerStart: { x: number; y: number };
  pointerCurrent: { x: number; y: number };
  scrollStart: { x: number; y: number };
  scrollCurrent: { x: number; y: number };
  zoom: number;
  snap: number;
}

const nodeGapX = 56;
const nodeGapY = 54;
const contentPad = 80;
const nodeSize = 26;

export function layoutMaterialAtlasPath(path: MaterialAtlasPathModel): MaterialGraphLayout {
  const nodes: MaterialGraphNode[] = [];
  const atlasHub = hubForPath(path);
  const layoutHub = { x: path.hubX, y: path.hubY };
  const byBucket = new Map<string, MaterialAtlasEntryModel[]>();
  for (const entry of path.entries) {
    if (!byBucket.has(entry.bucketId)) byBucket.set(entry.bucketId, []);
    byBucket.get(entry.bucketId)!.push(entry);
  }

  for (const bucket of path.buckets) {
    const entries = (byBucket.get(bucket.id) ?? []).slice().sort((a, b) => a.order - b.order);
    const autoPositions = autoLayoutBucket(entries, bucket, layoutHub);
    for (let i = 0; i < entries.length; i += 1) {
      const entry = entries[i];
      const explicit = entry.nodeX !== null && entry.nodeY !== null;
      const auto = autoPositions.get(entry.id) ?? { x: bucket.centerX, y: bucket.centerY };
      nodes.push({
        id: entry.id,
        bucketId: entry.bucketId,
        x: explicit ? entry.nodeX! : auto.x,
        y: explicit ? entry.nodeY! : auto.y,
        explicit,
        color: bucket.color,
        iconUrl: iconAssetUrl(entry),
        entry
      });
    }
  }

  const categoryAnchors = path.buckets.map(bucket => ({
    id: bucket.id,
    label: bucket.label,
    x: bucket.centerX,
    y: bucket.centerY,
    color: bucket.color
  }));
  const labelPlaques = path.buckets.map(bucket => ({
    id: bucket.id,
    label: bucket.label,
    x: bucket.plaqueX,
    y: bucket.plaqueY,
    color: bucket.color
  }));
  const traces: MaterialGraphTrace[] = [];
  let minX = atlasHub.x - contentPad;
  let minY = atlasHub.y - contentPad;
  let maxX = atlasHub.x + contentPad;
  let maxY = atlasHub.y + contentPad;
  minX = Math.min(minX, layoutHub.x - contentPad);
  minY = Math.min(minY, layoutHub.y - contentPad);
  maxX = Math.max(maxX, layoutHub.x + contentPad);
  maxY = Math.max(maxY, layoutHub.y + contentPad);

  for (const node of nodes) {
    minX = Math.min(minX, node.x - nodeSize - contentPad / 2);
    minY = Math.min(minY, node.y - nodeSize - contentPad / 2);
    maxX = Math.max(maxX, node.x + nodeSize + contentPad / 2);
    maxY = Math.max(maxY, node.y + nodeSize + contentPad / 2);
  }

  for (const bucket of path.buckets) {
    minX = Math.min(minX, Math.min(bucket.centerX, bucket.plaqueX) - contentPad);
    minY = Math.min(minY, Math.min(bucket.centerY, bucket.plaqueY) - contentPad);
    maxX = Math.max(maxX, Math.max(bucket.centerX, bucket.plaqueX) + contentPad);
    maxY = Math.max(maxY, Math.max(bucket.centerY, bucket.plaqueY) + contentPad);
  }

  const offsetX = minX < 0 ? -minX : 0;
  const offsetY = minY < 0 ? -minY : 0;
  if (offsetX || offsetY) {
    for (const node of nodes) {
      node.x += offsetX;
      node.y += offsetY;
    }
    maxX += offsetX;
    maxY += offsetY;
  }

  const nodeById = new Map(nodes.map(node => [node.id, node] as const));
  for (const node of nodes) {
    for (const parent of node.entry.parentIds) {
      const parentNode = nodeById.get(parent);
      if (parentNode) traces.push(trace(parent, node.id, parentNode.x, parentNode.y, node.x, node.y, node.color));
    }
  }

  return {
    width: Math.max(1, maxX + contentPad),
    height: Math.max(1, maxY + contentPad),
    viewBoxX: 0,
    viewBoxY: 0,
    offsetX,
    offsetY,
    hub: atlasHub,
    nodes,
    categoryAnchors,
    labelPlaques,
    traces
  };
}

function hubForPath(path: MaterialAtlasPathModel): MaterialGraphHub {
  return path.path === 'UNSTAINED'
    ? { id: 'atlas-hub', label: 'Still Atlas', x: path.hubLabelX, y: path.hubLabelY, color: '0xFF80B0A0' }
    : { id: 'atlas-hub', label: 'Blood Atlas', x: path.hubLabelX, y: path.hubLabelY, color: '0xFFCC6644' };
}

export function materialModelPositionFromRendered(
  layout: Pick<MaterialGraphLayout, 'offsetX' | 'offsetY'>,
  rendered: { x: number; y: number }
): { x: number; y: number } {
  return {
    x: rendered.x - layout.offsetX,
    y: rendered.y - layout.offsetY
  };
}

export function alignMaterialPositionToNodes(
  position: { x: number; y: number },
  nodes: Array<Pick<MaterialGraphNode, 'id' | 'x' | 'y'>>,
  selfId: string,
  tolerance: number | { x: number; y: number } = 10
): { x: number; y: number } {
  const toleranceX = typeof tolerance === 'number' ? tolerance : Math.max(0, tolerance.x);
  const toleranceY = typeof tolerance === 'number' ? tolerance : Math.max(0, tolerance.y);
  let alignedX = position.x;
  let alignedY = position.y;
  let bestXDistance = toleranceX + 1;
  let bestYDistance = toleranceY + 1;

  for (const node of nodes) {
    if (node.id === selfId) continue;
    const dx = Math.abs(node.x - position.x);
    const dy = Math.abs(node.y - position.y);
    if (dx <= toleranceX && dx < bestXDistance) {
      alignedX = node.x;
      bestXDistance = dx;
    }
    if (dy <= toleranceY && dy < bestYDistance) {
      alignedY = node.y;
      bestYDistance = dy;
    }
  }

  return { x: alignedX, y: alignedY };
}

export function materialPositionFromDrag(request: MaterialDragRequest): { x: number; y: number } {
  const zoom = request.zoom || 1;
  const rawX = request.origin.x + ((request.pointerCurrent.x + request.scrollCurrent.x) - (request.pointerStart.x + request.scrollStart.x)) / zoom;
  const rawY = request.origin.y + ((request.pointerCurrent.y + request.scrollCurrent.y) - (request.pointerStart.y + request.scrollStart.y)) / zoom;
  return {
    x: snapFromOrigin(rawX, request.origin.x, request.snap),
    y: snapFromOrigin(rawY, request.origin.y, request.snap)
  };
}

export function sanitizeMaterialParents(selfId: string, parentIds: string[], knownIds: Set<string>): string[] {
  const sanitized: string[] = [];
  for (const parent of parentIds.map(value => value.trim()).filter(Boolean)) {
    if (parent === selfId || !knownIds.has(parent) || sanitized.includes(parent)) continue;
    sanitized.push(parent);
  }
  return sanitized;
}

function autoLayoutBucket(
  entries: MaterialAtlasEntryModel[],
  bucket: MaterialAtlasPathModel['buckets'][number],
  hub: { x: number; y: number }
): Map<string, { x: number; y: number }> {
  const positions = new Map<string, { x: number; y: number }>();
  const branchEntries = entries;
  const angle = Math.atan2(bucket.centerY - hub.y, bucket.centerX - hub.x);
  const ux = Math.cos(angle);
  const uy = Math.sin(angle);
  const tx = -uy;
  const ty = ux;
  const cols = Math.max(1, Math.min(5, Math.ceil(Math.sqrt(branchEntries.length * 1.25))));

  for (let i = 0; i < branchEntries.length; i += 1) {
    const row = Math.floor(i / cols);
    const col = i % cols;
    const rowCount = Math.min(cols, branchEntries.length - row * cols);
    const lateral = (col - (rowCount - 1) * 0.5) * nodeGapX + jitter(branchEntries[i].id, 9);
    const radial = nodeGapY + row * nodeGapY + ((i & 1) === 0 ? 0 : 11);
    const x = Math.round(bucket.centerX + tx * lateral + ux * radial);
    const y = Math.round(bucket.centerY + ty * lateral + uy * radial);
    positions.set(branchEntries[i].id, { x, y });
  }
  return positions;
}

function trace(fromId: string, toId: string, fromX: number, fromY: number, toX: number, toY: number, color: string): MaterialGraphTrace {
  return {
    fromId,
    toId,
    fromX,
    fromY,
    toX,
    toY,
    color,
    path: organicPath(fromX, fromY, toX, toY)
  };
}

function organicPath(fromX: number, fromY: number, toX: number, toY: number): string {
  const dx = toX - fromX;
  const dy = toY - fromY;
  const distance = Math.hypot(dx, dy);
  const handle = Math.max(36, Math.min(140, distance * 0.34));
  const length = distance || 1;
  const nx = dx / length;
  const ny = dy / length;
  const sway = Math.max(8, Math.min(24, distance * 0.08)) * ((((Math.round(fromX) * 31 + Math.round(toY) * 17) & 1) === 0) ? 1 : -1);
  const sx = -ny * sway;
  const sy = nx * sway;
  return `M ${fromX.toFixed(1)} ${fromY.toFixed(1)} C ${(fromX + nx * handle + sx).toFixed(1)} ${(fromY + ny * handle + sy).toFixed(1)}, ${(toX - nx * handle - sx).toFixed(1)} ${(toY - ny * handle - sy).toFixed(1)}, ${toX.toFixed(1)} ${toY.toFixed(1)}`;
}

function iconAssetUrl(entry: MaterialAtlasEntryModel): string {
  const catalog = entry.catalog;
  const source = catalog?.iconSource ?? 'item';
  const field = catalog?.iconField ?? entry.id;
  return `/asset/${source}/${encodeURIComponent(field)}.png`;
}

function snapFromOrigin(value: number, origin: number, grid: number): number {
  if (grid <= 1) return Math.round(value);
  return origin + Math.round((value - origin) / grid) * grid;
}

function jitter(id: string, maxAbs: number): number {
  const range = maxAbs * 2 + 1;
  let hash = 0;
  for (let i = 0; i < id.length; i += 1) {
    hash = (hash * 31 + id.charCodeAt(i)) | 0;
  }
  return ((hash % range) + range) % range - maxAbs;
}
