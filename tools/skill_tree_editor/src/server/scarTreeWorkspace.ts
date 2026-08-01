import { existsSync, readFileSync } from 'node:fs';
import type { PreviewResult, ScarTreePreviewRequest, ScarTreeWorkspace } from '../shared/types';
import { makeFileDiff } from './diff';
import { parseScarTreeJava, renderScarTreeJava, type ScarMetadata } from './scarTreeParser';
import { hasBlockingDiagnostics } from './validation';
import { safeResolve, storePreview } from './workspace';

const TREE_PATH = 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/ScarTreeLayout.java';
const SCAR_INIT_PATH = 'src/main/java/com/vincenthuto/hemomancy/common/init/ScarInit.java';
const LANG_PATH = 'src/main/resources/assets/hemomancy/lang/en_us.json';

export async function loadScarTreeWorkspace(repoRoot: string): Promise<ScarTreeWorkspace> {
  const source = read(repoRoot, TREE_PATH);
  const parsed = parseScarTreeJava(TREE_PATH, source, loadScarMetadata(repoRoot));
  return { repoRoot, tree: parsed.tree, diagnostics: parsed.tree.diagnostics };
}

export async function previewScarTreeWorkspaceChanges(
  repoRoot: string,
  request: ScarTreePreviewRequest
): Promise<PreviewResult> {
  const before = read(repoRoot, TREE_PATH);
  const parsed = parseScarTreeJava(TREE_PATH, before, loadScarMetadata(repoRoot));
  const updates = new Map((request.nodes ?? []).map(node => [node.id, {
    treeX: node.treeX,
    treeY: node.treeY,
    parents: node.parents ?? []
  }]));
  const after = renderScarTreeJava(before, parsed.parsedNodes, updates);
  const diffs = before === after ? [] : [makeFileDiff(TREE_PATH, before, after)];
  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics: parsed.tree.diagnostics,
    canApply: diffs.length > 0 && !hasBlockingDiagnostics(parsed.tree.diagnostics)
  };
  storePreview(result);
  return result;
}

function loadScarMetadata(repoRoot: string): Map<string, ScarMetadata> {
  const source = read(repoRoot, SCAR_INIT_PATH);
  const translations = readJson(read(repoRoot, LANG_PATH));
  const colors = tendencyColors();
  const result = new Map<string, ScarMetadata>();
  const pattern = /reg\("(scar_[^"]+)"\s*,\s*\(\)\s*->\s*cerebral\(\s*EnumBloodTendency\.(\w+)\s*,\s*[\d.]+f?\s*,\s*(\d+)\s*\)/g;
  for (const match of source.matchAll(pattern)) {
    const id = `hemomancy:${match[1]}`;
    const tendency = match[2];
    result.set(id, {
      displayName: translations[`item.hemomancy.${match[1]}`] ?? humanize(match[1]),
      tendency,
      tier: Number(match[3]),
      color: colors.get(tendency) ?? '#888888'
    });
  }
  if (source.includes('reg("scar_blood_honed", BloodHonedDefinition::new)')) {
    result.set('hemomancy:scar_blood_honed', {
      displayName: translations['item.hemomancy.scar_blood_honed'] ?? 'Blood-Honed Scar',
      tendency: 'FERRIC', tier: 2, color: colors.get('FERRIC')!
    });
  }
  return result;
}

function read(repoRoot: string, path: string): string {
  const absolute = safeResolve(repoRoot, path);
  return existsSync(absolute) ? readFileSync(absolute, 'utf8') : '';
}

function readJson(source: string): Record<string, string> {
  try { return JSON.parse(source) as Record<string, string>; } catch { return {}; }
}

function humanize(id: string): string {
  return id.split('_').map(word => word ? word[0].toUpperCase() + word.slice(1) : word).join(' ');
}

function tendencyColors(): Map<string, string> {
  return new Map([
    ['ANIMUS', '#ff0000'], ['FLAMMEUS', '#ff6400'], ['DUCTILIS', '#ffff00'], ['LUX', '#ffffff'],
    ['MORTEM', '#003a00'], ['CONGEATIO', '#0064ff'], ['FERRIC', '#353535'], ['TENEBRIS', '#46006e']
  ]);
}
