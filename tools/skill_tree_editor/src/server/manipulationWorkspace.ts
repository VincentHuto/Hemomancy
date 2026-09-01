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
  applyManipulationPresentation(parsed.tree.nodes, tendencyInfoByManip, loadManipulationFamilies(repoRoot));

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
  applyManipulationPresentation(parsed.tree.nodes, tendencyInfoByManip, loadManipulationFamilies(repoRoot));

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

function applyManipulationPresentation(
  nodes: ManipulationNodeModel[],
  tendencyInfoByManip: Map<string, { primary: string; secondary: string | null; rank: string | null }>,
  familiesByManip: Map<string, { baseline: string; forms: Array<{ id: string; requiredLevel: number }> }>
): void {
  const rankByManip = new Map([...tendencyInfoByManip.entries()].map(([name, info]) => [name, info.rank]));
  for (const node of nodes) {
    const family = familiesByManip.get(node.name);
    const info = tendencyInfoByManip.get(node.name);
    node.secondaryTendency = info?.secondary ?? null;
    node.rank = info?.rank ?? null;
    node.familyBaseline = family?.baseline ?? null;
    node.familyForms = family?.forms.map(form => form.id) ?? [];
    node.familyRequiredLevel = family?.forms.find(form => form.id === node.name)?.requiredLevel ?? null;
    node.isFamilyBaseline = family?.baseline === node.name;
    node.borderColor = family
      ? rankBorderColor(strongestFamilyRank(family, rankByManip))
      : rankBorderColor(node.rank) || node.color;
  }
}

function loadManipulationTendencies(repoRoot: string): Map<string, { primary: string; secondary: string | null; rank: string | null }> {
  const filePath = 'src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java';
  const abs = resolve(repoRoot, filePath);
  if (!existsSync(abs)) return new Map();
  const lines = readFileSync(abs, 'utf8').split(/\r?\n/);
  const map = new Map<string, { primary: string; secondary: string | null; rank: string | null }>();

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
      const rank = /EnumManipulationRank\.(HUMILIS|MEDIOCRITAS|SUMMA|MAGISTER|PERFECTUS)/.exec(buffer)?.[1] ?? null;
      const secondary = /\.setSecondaryTend\(\s*EnumBloodTendency\.(ANIMUS|FLAMMEUS|DUCTILIS|LUX|MORTEM|CONGEATIO|FERRIC|TENEBRIS)\s*\)/.exec(buffer)?.[1] ?? null;
      if (tend) map.set(currentName, { primary: tend, secondary, rank });
      currentName = null;
      buffer = '';
    }
  }
  return map;
}

function loadManipulationFamilies(repoRoot: string): Map<string, { baseline: string; forms: Array<{ id: string; requiredLevel: number }> }> {
  const filePath = 'src/main/java/com/vincenthuto/hemomancy/common/manipulation/family/ManipulationFamilyRegistry.java';
  const abs = resolve(repoRoot, filePath);
  const source = existsSync(abs) ? readFileSync(abs, 'utf8') : '';
  const byManip = new Map<string, { baseline: string; forms: Array<{ id: string; requiredLevel: number }> }>();
  for (let index = source.indexOf('family('); index >= 0; index = source.indexOf('family(', index + 1)) {
    const close = findMatchingParen(source, index + 'family'.length);
    if (close < 0) continue;
    const call = source.slice(index, close + 1);
    const baseline = /family\("([^"]+)"/.exec(call)?.[1];
    if (!baseline) continue;
    const forms = [...call.matchAll(/form\("([^"]+)"\s*,\s*(\d+)\)/g)]
      .map(form => ({ id: form[1], requiredLevel: Number(form[2]) }));
    if (!forms.length) continue;
    const family = { baseline, forms };
    byManip.set(baseline, family);
    for (const form of forms) byManip.set(form.id, family);
  }
  return byManip;
}

function findMatchingParen(text: string, openParenIndex: number): number {
  let depth = 0;
  let inString = false;
  for (let i = openParenIndex; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '(') depth += 1;
    else if (ch === ')') {
      depth -= 1;
      if (depth === 0) return i;
    }
  }
  return -1;
}

function strongestFamilyRank(
  family: { baseline: string; forms: Array<{ id: string }> },
  rankByManip: Map<string, string | null>
): string | null {
  let strongest = rankByManip.get(family.baseline) ?? null;
  for (const form of family.forms) {
    const rank = rankByManip.get(form.id) ?? null;
    if (rankOrdinal(rank) > rankOrdinal(strongest)) strongest = rank;
  }
  return strongest;
}

function rankOrdinal(rank: string | null): number {
  return ['HUMILIS', 'MEDIOCRITAS', 'SUMMA', 'MAGISTER', 'PERFECTUS'].indexOf(rank ?? '');
}

function rankBorderColor(rank: string | null | undefined): string {
  switch (rank) {
    case 'MEDIOCRITAS': return '#cd7f32';
    case 'SUMMA': return '#a7adb2';
    case 'MAGISTER': return '#ffc43d';
    case 'PERFECTUS': return '#d94cff';
    default: return '';
  }
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
