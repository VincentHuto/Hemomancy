import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { ManipulationNodeModel, ManipulationPreviewRequest, ManipulationWorkspace, PreviewResult } from '../shared/types';
import { makeFileDiff } from './diff';
import { parseManipulationTreeJava, renderManipulationTreeJava } from './manipulationParser';
import { hasBlockingDiagnostics } from './validation';
import { safeResolve, storePreview } from './workspace';

export async function loadManipulationWorkspace(repoRoot: string): Promise<ManipulationWorkspace> {
  const treePath = 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java';
  const treeAbs = safeResolve(repoRoot, treePath);
  const source = existsSync(treeAbs) ? readFileSync(treeAbs, 'utf8') : '';
  const tendencyInfoByManip = loadManipulationTendencies(repoRoot);
  const tendencyByManip = new Map([...tendencyInfoByManip.entries()].map(([name, info]) => [name, info.primary]));
  const colorByTendency = defaultTendencyColors();
  const parsed = parseManipulationTreeJava(treePath, source, tendencyByManip, colorByTendency);
  applySecondaryTendencies(parsed.tree.nodes, tendencyInfoByManip);

  return {
    repoRoot,
    tree: parsed.tree,
    diagnostics: parsed.tree.diagnostics
  };
}

export async function previewManipulationWorkspaceChanges(repoRoot: string, request: ManipulationPreviewRequest): Promise<PreviewResult> {
  const treePath = 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java';
  const treeAbs = safeResolve(repoRoot, treePath);
  const before = existsSync(treeAbs) ? readFileSync(treeAbs, 'utf8') : '';
  const tendencyInfoByManip = loadManipulationTendencies(repoRoot);
  const tendencyByManip = new Map([...tendencyInfoByManip.entries()].map(([name, info]) => [name, info.primary]));
  const colorByTendency = defaultTendencyColors();
  const parsed = parseManipulationTreeJava(treePath, before, tendencyByManip, colorByTendency);
  applySecondaryTendencies(parsed.tree.nodes, tendencyInfoByManip);

  const updates = new Map<string, { treeX: number; treeY: number; parents: string[]; softParents: string[]; tendency: string | null; secondaryTendency: string | null }>();
  for (const node of request.nodes ?? []) {
    updates.set(node.name, {
      treeX: node.treeX,
      treeY: node.treeY,
      parents: node.parents ?? [],
      softParents: node.softParents ?? [],
      tendency: node.tendency ?? null,
      secondaryTendency: node.secondaryTendency ?? null
    });
  }

  const changes = new Map<string, string>();
  changes.set(treePath, renderManipulationTreeJava(before, parsed.parsedNodes, updates));
  const initPath = 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java';
  const initAbs = safeResolve(repoRoot, initPath);
  const initBefore = existsSync(initAbs) ? readFileSync(initAbs, 'utf8') : '';
  if (initBefore) {
    changes.set(initPath, renderManipulationInitTendencies(initBefore, updates));
  }

  const diffs = [...changes.entries()]
    .map(([path, after]) => {
      const abs = safeResolve(repoRoot, path);
      const sourceBefore = existsSync(abs) ? readFileSync(abs, 'utf8') : '';
      return sourceBefore === after ? null : makeFileDiff(path, sourceBefore, after);
    })
    .filter((diff): diff is NonNullable<typeof diff> => diff !== null);
  const diagnostics = parsed.tree.diagnostics;

  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics,
    canApply: diffs.length > 0 && !hasBlockingDiagnostics(diagnostics)
  };
  storePreview(result);
  return result;
}

function renderManipulationInitTendencies(
  source: string,
  updates: Map<string, { tendency: string | null; secondaryTendency: string | null }>
): string {
  const replacements: Array<{ start: number; end: number; value: string }> = [];
  const pattern = /MANIPS\.register\("([^"]+)"[\s\S]*?\);/g;
  for (const match of source.matchAll(pattern)) {
    const name = match[1];
    const update = updates.get(name);
    if (!update || match.index == null) continue;
    const matchedText = match[0];
    const primaryMatch = /EnumBloodTendency\.(ANIMUS|FLAMMEUS|DUCTILIS|LUX|MORTEM|CONGEATIO|FERRIC|TENEBRIS)/.exec(matchedText);
    if (update.tendency && defaultTendencyColors().has(update.tendency) && primaryMatch && primaryMatch[1] !== update.tendency) {
      const enumOffset = primaryMatch.index + 'EnumBloodTendency.'.length;
      replacements.push({
        start: match.index + enumOffset,
        end: match.index + enumOffset + primaryMatch[1].length,
        value: update.tendency
      });
    }

    const secondary = update.secondaryTendency;
    const secondaryMatch = /\.setSecondaryTend\(\s*EnumBloodTendency\.(ANIMUS|FLAMMEUS|DUCTILIS|LUX|MORTEM|CONGEATIO|FERRIC|TENEBRIS)\s*\)/.exec(matchedText);
    if (secondary && defaultTendencyColors().has(secondary)) {
      if (secondaryMatch) {
        if (secondaryMatch[1] === secondary) continue;
        const enumOffset = secondaryMatch.index + secondaryMatch[0].lastIndexOf(secondaryMatch[1]);
        replacements.push({
          start: match.index + enumOffset,
          end: match.index + enumOffset + secondaryMatch[1].length,
          value: secondary
        });
      } else {
        const insertion = secondaryTendencyInsertion(matchedText);
        replacements.push({
          start: match.index + insertion.offset,
          end: match.index + insertion.offset,
          value: `\n${insertion.indent}.setSecondaryTend(EnumBloodTendency.${secondary})`
        });
      }
    } else if (secondaryMatch) {
      const lineStart = matchedText.lastIndexOf('\n', secondaryMatch.index);
      const removeStart = lineStart >= 0 ? lineStart : secondaryMatch.index;
      const removeEnd = secondaryMatch.index + secondaryMatch[0].length;
      replacements.push({
        start: match.index + removeStart,
        end: match.index + removeEnd,
        value: ''
      });
    }
  }

  replacements.sort((a, b) => b.start - a.start);
  let next = source;
  for (const replacement of replacements) {
    next = next.slice(0, replacement.start) + replacement.value + next.slice(replacement.end);
  }
  return next;
}

function secondaryTendencyInsertion(statement: string): { offset: number; indent: string } {
  const target = /\n([ \t]*)\.(?:setCooldownTicks|setDrudgeAction)\(/.exec(statement);
  if (target) return { offset: target.index, indent: target[1] };

  const chainIndent = /\n([ \t]*)\./.exec(statement)?.[1] ?? '\t\t\t\t\t';
  return { offset: statement.lastIndexOf(');'), indent: chainIndent };
}

function applySecondaryTendencies(nodes: ManipulationNodeModel[], tendencyInfoByManip: Map<string, { primary: string; secondary: string | null }>): void {
  for (const node of nodes) {
    node.secondaryTendency = tendencyInfoByManip.get(node.name)?.secondary ?? null;
  }
}

function loadManipulationTendencies(repoRoot: string): Map<string, { primary: string; secondary: string | null }> {
  const filePath = 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java';
  const abs = resolve(repoRoot, filePath);
  if (!existsSync(abs)) return new Map();
  const lines = readFileSync(abs, 'utf8').split(/\r?\n/);
  const map = new Map<string, { primary: string; secondary: string | null }>();

  let currentName: string | null = null;
  let buffer = '';
  for (const line of lines) {
    const startMatch = /^\s*public\s+static\s+final\s+DeferredHolder<[^>]+>\s+(\w+)\s*=\s*MANIPS\.register\("([^"]+)"/.exec(line);
    if (startMatch) {
      currentName = startMatch[2];
      buffer = line;
      continue;
    }
    if (!currentName) continue;
    buffer += '\n' + line;
    const end = line.includes(');');
    if (end) {
      const tend = /EnumBloodTendency\.(ANIMUS|FLAMMEUS|DUCTILIS|LUX|MORTEM|CONGEATIO|FERRIC|TENEBRIS)/.exec(buffer)?.[1];
      const secondary = /\.setSecondaryTend\(\s*EnumBloodTendency\.(ANIMUS|FLAMMEUS|DUCTILIS|LUX|MORTEM|CONGEATIO|FERRIC|TENEBRIS)\s*\)/.exec(buffer)?.[1] ?? null;
      if (tend) map.set(currentName, { primary: tend, secondary });
      currentName = null;
      buffer = '';
    }
  }
  return map;
}

function defaultTendencyColors(): Map<string, string> {
  return new Map([
    ['ANIMUS', '#ff0000'],
    ['FLAMMEUS', '#ff6400'],
    ['DUCTILIS', '#ffff00'],
    ['LUX', '#ffffff'],
    ['MORTEM', '#003a00'],
    ['CONGEATIO', '#0064ff'],
    ['FERRIC', '#353535'],
    ['TENEBRIS', '#46006e']
  ]);
}
