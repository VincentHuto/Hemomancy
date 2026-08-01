import type { RecipeMapEditorLink, RecipeMapEditorTab } from '../shared/types';

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
const size = 1040;
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
        nodes.push({
          id: entry.id,
          displayName: entry.displayName,
          family,
          column,
          x: center + Math.round(Math.cos(angle) * radius),
          y: center + Math.round(Math.sin(angle) * radius)
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
