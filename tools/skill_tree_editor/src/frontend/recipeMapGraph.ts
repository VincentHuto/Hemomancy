import type { RecipeMapEditorEntry, RecipeMapEditorLink, RecipeMapEditorTab } from '../shared/types';

export type RecipeMapLayer = 'surface' | 'deep';

export interface RecipeMapGraphNode {
  id: string;
  displayName: string;
  family: string;
  column: number;
  x: number;
  y: number;
}

export interface RecipeMapGraphLink extends RecipeMapEditorLink {
  fromX: number;
  fromY: number;
  toX: number;
  toY: number;
}

export interface RecipeMapGraphLayout {
  width: number;
  height: number;
  nodes: RecipeMapGraphNode[];
  links: RecipeMapGraphLink[];
  rings: Array<{ degree: number; radius: number }>;
}

const center = 480;
export const recipeMapContentSize = 1030;
export const recipeMapNodeHalfSize = 14;
const size = recipeMapContentSize;
const radii = [72, 120, 170, 220, 270, 320, 370, 420, 470];

export function layoutRecipeMap(tab: RecipeMapEditorTab, layer: RecipeMapLayer): RecipeMapGraphLayout {
  const activeFamilies = tab.families.filter(family => tab.entries.some(entry => entry.family === family));
  const familyAngles = new Map(activeFamilies.map((family, index) => [
    family,
    -Math.PI / 2 + index * Math.PI * 2 / Math.max(1, activeFamilies.length)
  ]));
  const visible = tab.entries.filter(entry => layer === 'surface' ? entry.column <= 4 : entry.column >= 5);
  const nodes: RecipeMapGraphNode[] = [];

  for (const family of activeFamilies) {
    const familyEntries = visible.filter(entry => entry.family === family);
    for (let column = 0; column <= 8; column += 1) {
      const cell = familyEntries.filter(entry => entry.column === column)
        .sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
      const sectorWidth = Math.PI * 2 / Math.max(1, activeFamilies.length);
      const spread = Math.min(sectorWidth * 0.65, Math.PI / 180 * 16 * Math.max(0, cell.length - 1));
      cell.forEach((entry, slot) => {
        const offset = cell.length <= 1 ? 0 : -spread / 2 + spread * slot / (cell.length - 1);
        const radialDegree = layer === 'deep' ? column - 4 : column;
        const angle = (familyAngles.get(family) ?? -Math.PI / 2) + offset;
        const radius = radii[Math.max(0, Math.min(8, radialDegree))];
        const hasAuthoredPosition = Number.isFinite(entry.treeX) && Number.isFinite(entry.treeY);
        nodes.push({
          id: entry.id,
          displayName: entry.displayName,
          family,
          column,
          x: hasAuthoredPosition ? Math.round(entry.treeX!) : center + Math.round(Math.cos(angle) * radius),
          y: hasAuthoredPosition ? Math.round(entry.treeY!) : center + Math.round(Math.sin(angle) * radius)
        });
      });
    }
  }

  const byId = new Map(nodes.map(node => [node.id, node]));
  const links = tab.links.flatMap(link => {
    const from = byId.get(link.from);
    const to = byId.get(link.to);
    return from && to ? [{ ...link, fromX: from.x, fromY: from.y, toX: to.x, toY: to.y }] : [];
  });
  const degrees = layer === 'surface' ? [0, 1, 2, 3, 4] : [5, 6, 7, 8];
  return {
    width: size,
    height: size,
    nodes,
    links,
    rings: degrees.map(degree => ({ degree, radius: radii[layer === 'deep' ? degree - 4 : degree] }))
  };
}

export function recipeMapSelectionIdsInRect(
  nodes: RecipeMapGraphNode[],
  start: { x: number; y: number },
  end: { x: number; y: number }
): string[] {
  const left = Math.min(start.x, end.x);
  const right = Math.max(start.x, end.x);
  const top = Math.min(start.y, end.y);
  const bottom = Math.max(start.y, end.y);
  return nodes.filter(node => node.x >= left && node.x <= right && node.y >= top && node.y <= bottom).map(node => node.id);
}

export function moveRecipeMapEntries(
  entries: RecipeMapEditorEntry[],
  nodes: RecipeMapGraphNode[],
  ids: Set<string>,
  dx: number,
  dy: number
): void {
  const positions = new Map(nodes.map(node => [node.id, node]));
  for (const entry of entries) {
    if (!ids.has(entry.id)) continue;
    const position = positions.get(entry.id);
    if (!position) continue;
    entry.treeX = clampRecipeMapCoordinate(position.x + dx);
    entry.treeY = clampRecipeMapCoordinate(position.y + dy);
  }
}

export function recipeMapDragDelta(start: Point, current: Point, zoom: number, threshold = 4): Point | undefined {
  const clientDx = current.x - start.x;
  const clientDy = current.y - start.y;
  if (Math.hypot(clientDx, clientDy) < threshold) return undefined;
  return { x: clientDx / zoom, y: clientDy / zoom };
}

export function clampRecipeMapCoordinate(value: number): number {
  return Math.max(recipeMapNodeHalfSize, Math.min(recipeMapContentSize - recipeMapNodeHalfSize, Math.round(value)));
}

interface Point { x: number; y: number }
