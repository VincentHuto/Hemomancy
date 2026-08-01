import type { Diagnostic, ScarTreeFile, ScarTreeNodeModel } from '../shared/types';

interface Span {
  start: number;
  end: number;
}

interface ParsedScarNode {
  model: ScarTreeNodeModel;
  callSpan: Span;
}

export interface ScarMetadata {
  displayName: string;
  tendency: string;
  tier: number;
  color: string;
}

interface ParseResult {
  tree: ScarTreeFile;
  parsedNodes: ParsedScarNode[];
}

const AUTHORED_CALL = /authored\(\s*"([^"]+)"\s*,\s*(-?\d+)\s*,\s*(-?\d+)((?:\s*,\s*"[^"]*")*)\s*\)/g;

export function parseScarTreeJava(
  path: string,
  source: string,
  metadataById: Map<string, ScarMetadata>
): ParseResult {
  const nodes: ScarTreeNodeModel[] = [];
  const parsedNodes: ParsedScarNode[] = [];
  const diagnostics: Diagnostic[] = [];

  for (const match of source.matchAll(AUTHORED_CALL)) {
    const id = match[1];
    const metadata = metadataById.get(id);
    const model: ScarTreeNodeModel = {
      id,
      displayName: metadata?.displayName ?? humanizeId(id),
      tendency: metadata?.tendency ?? null,
      tier: metadata?.tier ?? 0,
      color: metadata?.color ?? '#888888',
      treeX: Number(match[2]),
      treeY: Number(match[3]),
      parents: [...match[4].matchAll(/"([^"]+)"/g)].map(parent => parent[1])
    };
    nodes.push(model);
    parsedNodes.push({
      model,
      callSpan: { start: match.index!, end: match.index! + match[0].length }
    });
  }

  validate(nodes, diagnostics, path);
  return { tree: { path, source, nodes, diagnostics }, parsedNodes };
}

export function renderScarTreeJava(
  source: string,
  parsedNodes: ParsedScarNode[],
  updates: Map<string, Pick<ScarTreeNodeModel, 'treeX' | 'treeY' | 'parents'>>
): string {
  const replacements: Array<{ start: number; end: number; value: string }> = [];
  for (const parsed of parsedNodes) {
    const update = updates.get(parsed.model.id);
    if (!update || unchanged(parsed.model, update)) continue;
    const parents = update.parents.map(parent => `, "${parent}"`).join('');
    replacements.push({
      ...parsed.callSpan,
      value: `authored("${parsed.model.id}", ${Math.round(update.treeX)}, ${Math.round(update.treeY)}${parents})`
    });
  }
  replacements.sort((a, b) => b.start - a.start);
  let next = source;
  for (const replacement of replacements) {
    next = next.slice(0, replacement.start) + replacement.value + next.slice(replacement.end);
  }
  return next;
}

function unchanged(model: ScarTreeNodeModel, update: Pick<ScarTreeNodeModel, 'treeX' | 'treeY' | 'parents'>): boolean {
  return model.treeX === Math.round(update.treeX)
    && model.treeY === Math.round(update.treeY)
    && model.parents.length === update.parents.length
    && model.parents.every((parent, index) => parent === update.parents[index]);
}

function validate(nodes: ScarTreeNodeModel[], diagnostics: Diagnostic[], path: string): void {
  const ids = new Set<string>();
  for (const node of nodes) {
    if (ids.has(node.id)) diagnostics.push({
      severity: 'error', code: 'scar_tree_duplicate',
      message: `Duplicate scar node: ${node.id}`, file: path, skill: node.id
    });
    ids.add(node.id);
  }
  for (const node of nodes) {
    for (const parent of node.parents) {
      if (!ids.has(parent)) diagnostics.push({
        severity: 'warning', code: 'scar_tree_unknown_parent',
        message: `Unknown parent "${parent}" referenced by "${node.id}".`, file: path, skill: node.id
      });
    }
  }
}

function humanizeId(id: string): string {
  const path = id.includes(':') ? id.slice(id.indexOf(':') + 1) : id;
  return path.split('_').map(word => word ? word[0].toUpperCase() + word.slice(1) : word).join(' ');
}
