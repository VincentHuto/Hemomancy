import type {
  Diagnostic,
  MaterialAtlasBucketModel,
  MaterialAtlasEntryModel,
  MaterialAtlasPathKey,
  MaterialAtlasPathModel,
  MaterialCatalogEntryModel,
  MaterialGateModel
} from '../shared/types';

interface ParseResult {
  paths: MaterialAtlasPathModel[];
  diagnostics: Diagnostic[];
}

interface MaterialsDataParseResult {
  entries: MaterialCatalogEntryModel[];
  diagnostics: Diagnostic[];
}

interface Span {
  start: number;
  end: number;
}

interface ParsedCatalogEntry extends MaterialCatalogEntryModel {
  span: Span;
}

const pathOrder: MaterialAtlasPathKey[] = ['HARBINGER', 'UNSTAINED'];
const pathMethod: Record<MaterialAtlasPathKey, string> = {
  HARBINGER: 'registerHarbingerEntries',
  UNSTAINED: 'registerUnstainedEntries'
};
const dataMethod: Record<MaterialAtlasPathKey, string> = {
  HARBINGER: 'buildBloodEntries',
  UNSTAINED: 'buildUnstainedEntries'
};

export function parseMaterialAtlasSpecJava(file: string, source: string): ParseResult {
  const diagnostics: Diagnostic[] = [];
  const bucketsByPath = new Map<MaterialAtlasPathKey, MaterialAtlasBucketModel[]>(pathOrder.map(path => [path, []]));
  const entriesByPath = new Map<MaterialAtlasPathKey, MaterialAtlasEntryModel[]>(pathOrder.map(path => [path, []]));

  const bucketBody = methodBodySpan(source, 'registerBuckets');
  if (!bucketBody) {
    diagnostics.push(issue('warning', 'material_missing_bucket_method', 'Could not find registerBuckets().', file));
  } else {
    const body = source.slice(bucketBody.start, bucketBody.end);
    for (const call of scanHelperCalls(body, bucketBody.start, ['bucket'])) {
      const parsed = parseBucketCall(call.text);
      if (parsed) {
        bucketsByPath.get(parsed.path)!.push(parsed);
      }
    }
  }

  for (const path of pathOrder) {
    const body = methodBodySpan(source, pathMethod[path]);
    if (!body) {
      diagnostics.push(issue('warning', 'material_missing_entry_method', `Could not find ${pathMethod[path]}().`, file));
      continue;
    }
    for (const call of scanHelperCalls(source.slice(body.start, body.end), body.start, ['entry', 'entryAt'])) {
      const parsed = parseAtlasEntryCall(call.text, path, entriesByPath.get(path)!.length);
      if (parsed) {
        entriesByPath.get(parsed.path)!.push(parsed);
      }
    }
  }

  return {
    paths: pathOrder.map(path => ({
      path,
      buckets: bucketsByPath.get(path)!,
      entries: entriesByPath.get(path)!
    })),
    diagnostics
  };
}

export function renderMaterialAtlasSpecJava(source: string, paths: MaterialAtlasPathModel[]): string {
  let next = source;
  const replacements: Array<{ span: Span; value: string }> = [];
  const byPath = new Map(paths.map(path => [path.path, path] as const));

  const bucketsSpan = methodBodySpan(source, 'registerBuckets');
  if (bucketsSpan) {
    const bucketLines = pathOrder.flatMap(path => (byPath.get(path)?.buckets ?? [])
      .map(bucket => `\t\tbucket(MaterialAtlasPath.${path}, "${escapeJavaString(bucket.id)}", "${escapeJavaString(bucket.label)}", "${escapeJavaString(bucket.rootMaterialId)}", ${normalizeColorLiteral(bucket.color)}, ${Math.round(bucket.centerX)}, ${Math.round(bucket.centerY)}, ${Math.round(bucket.plaqueX)}, ${Math.round(bucket.plaqueY)});`));
    replacements.push({ span: bucketsSpan, value: `\n${bucketLines.join('\n')}\n\t` });
  }

  for (const path of pathOrder) {
    const span = methodBodySpan(source, pathMethod[path]);
    if (!span) continue;
    const variable = path === 'HARBINGER' ? 'h' : 'u';
    const entryLines = [
      `\t\tMaterialAtlasPath ${variable} = MaterialAtlasPath.${path};`,
      ...(byPath.get(path)?.entries ?? [])
        .slice()
        .sort((a, b) => a.order - b.order)
        .map(entry => renderAtlasEntryCall(entry, variable))
    ];
    replacements.push({ span, value: `\n${entryLines.join('\n')}\n\t` });
  }

  replacements.sort((a, b) => b.span.start - a.span.start);
  for (const replacement of replacements) {
    next = next.slice(0, replacement.span.start) + replacement.value + next.slice(replacement.span.end);
  }
  return next;
}

export function parseMaterialsDataJava(file: string, source: string): MaterialsDataParseResult {
  const diagnostics: Diagnostic[] = [];
  return {
    entries: parseCatalogEntries(file, source, diagnostics).map(entry => stripCatalogSpan(entry)),
    diagnostics
  };
}

export function renderMaterialsDataJava(source: string, entries: MaterialCatalogEntryModel[]): string {
  const parsed = parseCatalogEntries('MaterialsData.java', source, []);
  const existingByKey = new Map(parsed.map(entry => [catalogKey(entry), entry] as const));
  const replacementByStart = new Map<number, { span: Span; value: string }>();
  const insertionsByPath = new Map<MaterialAtlasPathKey, MaterialCatalogEntryModel[]>();

  for (const entry of entries) {
    const existing = existingByKey.get(catalogKey(entry));
    if (existing) {
      replacementByStart.set(existing.span.start, {
        span: existing.span,
        value: renderCatalogEntry(entry)
      });
    } else {
      if (!insertionsByPath.has(entry.path)) insertionsByPath.set(entry.path, []);
      insertionsByPath.get(entry.path)!.push(entry);
    }
  }

  const replacements = [...replacementByStart.values()];
  for (const [path, newEntries] of insertionsByPath.entries()) {
    const span = methodBodySpan(source, dataMethod[path]);
    if (!span) continue;
    const body = source.slice(span.start, span.end);
    const returnIndex = body.indexOf('return Collections.unmodifiableList(list);');
    const insertAt = returnIndex >= 0 ? span.start + returnIndex : span.end;
    const value = newEntries.map(renderCatalogEntry).join('\n\n') + '\n\n        ';
    replacements.push({
      span: { start: insertAt, end: insertAt },
      value
    });
  }

  replacements.sort((a, b) => b.span.start - a.span.start);
  let next = source;
  for (const replacement of replacements) {
    next = next.slice(0, replacement.span.start) + replacement.value + next.slice(replacement.span.end);
  }
  return next;
}

function parseCatalogEntries(file: string, source: string, diagnostics: Diagnostic[]): ParsedCatalogEntry[] {
  const entries: ParsedCatalogEntry[] = [];
  for (const path of pathOrder) {
    const span = methodBodySpan(source, dataMethod[path]);
    if (!span) {
      diagnostics.push(issue('warning', 'material_missing_catalogue_method', `Could not find ${dataMethod[path]}().`, file));
      continue;
    }
    const body = source.slice(span.start, span.end);
    let offset = 0;
    while (offset < body.length) {
      const localIndex = body.indexOf('list.add(', offset);
      if (localIndex < 0) break;
      const absoluteIndex = span.start + localIndex;
      offset = localIndex + 'list.add('.length;
      if (isLineCommented(source, absoluteIndex)) continue;
      const openParen = absoluteIndex + 'list.add'.length;
      const closeParen = findMatchingParen(source, openParen);
      if (closeParen < 0) continue;
      const statementEnd = source[closeParen + 1] === ';' ? closeParen + 2 : closeParen + 1;
      const statement = source.slice(absoluteIndex, statementEnd);
      const entry = parseMaterialEntryStatement(path, statement, { start: absoluteIndex, end: statementEnd });
      if (entry) entries.push(entry);
      offset = statementEnd - span.start;
    }
  }
  return entries;
}

function parseMaterialEntryStatement(path: MaterialAtlasPathKey, statement: string, span: Span): ParsedCatalogEntry | null {
  const ctorIndex = statement.indexOf('new MaterialEntry(');
  if (ctorIndex < 0) return null;
  const openParen = ctorIndex + 'new MaterialEntry'.length;
  const closeParen = findMatchingParen(statement, openParen);
  if (closeParen < 0) return null;
  const args = splitTopLevelArgs(statement.slice(openParen + 1, closeParen)).map(arg => arg.text.trim());
  if (args.length < 5) return null;
  const id = parseStringLiteral(args[0]);
  const displayName = parseStringLiteral(args[1]);
  const description = parseStringLiteral(args[2]);
  const category = parseStringLiteral(args[3]);
  if (!id || displayName === null || description === null || category === null) return null;
  const iconMatch = /(ItemInit|BlockInit)\.(\w+)\.get\(\)/.exec(args[4]);
  if (!iconMatch) return null;
  const hasRecipe = args.length >= 6 && /^(true|false)$/.test(args[5])
    ? args[5] === 'true'
    : true;
  const gate = args.length >= 7 ? parseUnlockPredicate(args[6]) : alwaysGate();
  return {
    path,
    id,
    displayName,
    description,
    category,
    iconSource: iconMatch[1] === 'BlockInit' ? 'block' : 'item',
    iconField: iconMatch[2],
    hasRecipe,
    gate,
    span
  };
}

function parseAtlasEntryCall(text: string, methodPath: MaterialAtlasPathKey, order: number): MaterialAtlasEntryModel | null {
  const helper = text.trim().startsWith('entryAt(') ? 'entryAt' : text.trim().startsWith('entry(') ? 'entry' : null;
  if (!helper) return null;
  const open = text.indexOf('(');
  const close = findMatchingParen(text, open);
  if (open < 0 || close < 0) return null;
  const args = splitTopLevelArgs(text.slice(open + 1, close)).map(arg => arg.text.trim());
  if ((helper === 'entry' && args.length < 4) || (helper === 'entryAt' && args.length < 6)) return null;
  const id = parseStringLiteral(args[0]);
  const path = parsePathArg(args[1], methodPath);
  const bucketId = parseStringLiteral(args[2]);
  if (!id || !bucketId) return null;
  const gate = parseMaterialGate(args[3]);
  const nodeX = helper === 'entryAt' ? Number(args[4]) : null;
  const nodeY = helper === 'entryAt' ? Number(args[5]) : null;
  const parentStart = helper === 'entryAt' ? 6 : 4;
  return {
    path,
    id,
    bucketId,
    gate,
    order,
    parentIds: args.slice(parentStart)
      .map(parseStringLiteral)
      .filter((value): value is string => value !== null),
    nodeX: Number.isFinite(nodeX) ? nodeX : null,
    nodeY: Number.isFinite(nodeY) ? nodeY : null
  };
}

function parseBucketCall(text: string): MaterialAtlasBucketModel | null {
  const open = text.indexOf('(');
  const close = findMatchingParen(text, open);
  if (open < 0 || close < 0) return null;
  const args = splitTopLevelArgs(text.slice(open + 1, close)).map(arg => arg.text.trim());
  if (args.length !== 8 && args.length !== 9) return null;
  const pathMatch = /MaterialAtlasPath\.(HARBINGER|UNSTAINED)/.exec(args[0]);
  const id = parseStringLiteral(args[1]);
  const label = parseStringLiteral(args[2]);
  if (!pathMatch || !id || label === null) return null;
  const hasRoot = args.length === 9;
  const rootMaterialId = hasRoot ? parseStringLiteral(args[3]) : id;
  if (!rootMaterialId) return null;
  const offset = hasRoot ? 1 : 0;
  return {
    path: pathMatch[1] as MaterialAtlasPathKey,
    id,
    label,
    rootMaterialId,
    color: normalizeColorLiteral(args[3 + offset]),
    centerX: Number(args[4 + offset]),
    centerY: Number(args[5 + offset]),
    plaqueX: Number(args[6 + offset]),
    plaqueY: Number(args[7 + offset])
  };
}

function renderAtlasEntryCall(entry: MaterialAtlasEntryModel, pathVariable: string): string {
  const parents = entry.parentIds.map(parent => `"${escapeJavaString(parent)}"`).join(', ');
  const gate = renderMaterialGate(entry.gate);
  const parentSuffix = parents ? `, ${parents}` : '';
  if (entry.nodeX !== null && entry.nodeY !== null) {
    return `\t\tentryAt("${escapeJavaString(entry.id)}", ${pathVariable}, "${escapeJavaString(entry.bucketId)}", ${gate}, ${Math.round(entry.nodeX)}, ${Math.round(entry.nodeY)}${parentSuffix});`;
  }
  return `\t\tentry("${escapeJavaString(entry.id)}", ${pathVariable}, "${escapeJavaString(entry.bucketId)}", ${gate}${parentSuffix});`;
}

function renderCatalogEntry(entry: MaterialCatalogEntryModel): string {
  const init = entry.iconSource === 'block' ? 'BlockInit' : 'ItemInit';
  return [
    `        list.add(new MaterialEntry("${escapeJavaString(entry.id)}", "${escapeJavaString(entry.displayName)}",`,
    `                "${escapeJavaString(entry.description)}",`,
    `                "${escapeJavaString(entry.category)}", () -> new ItemStack(${init}.${entry.iconField}.get()),`,
    `                ${entry.hasRecipe ? 'true' : 'false'}, ${renderUnlockPredicate(entry.gate)}));`
  ].join('\n');
}

function parsePathArg(arg: string, fallback: MaterialAtlasPathKey): MaterialAtlasPathKey {
  if (arg.includes('UNSTAINED') || arg === 'u') return 'UNSTAINED';
  if (arg.includes('HARBINGER') || arg === 'h') return 'HARBINGER';
  return fallback;
}

function parseMaterialGate(expr: string): MaterialGateModel {
  const text = expr.replace(/\s+/g, '');
  if (/^a\(\)$/.test(text) || /MaterialGate\.always\(\)/.test(text)) return alwaysGate();
  const degree = /(?:^d|MaterialGate\.degree)\(([-\d.]+)[fF]?\)/.exec(text);
  if (degree) return { type: 'DEGREE', value: Number(degree[1]) };
  const purity = /(?:^p|MaterialGate\.purity)\(([-\d.]+)[fF]?\)/.exec(text);
  if (purity) return { type: 'PURITY', value: Number(purity[1]) };
  const clarity = /(?:^c|MaterialGate\.clarity)\(([-\d.]+)[fF]?\)/.exec(text);
  if (clarity) return { type: 'CLARITY', value: Number(clarity[1]) };
  return alwaysGate();
}

function parseUnlockPredicate(expr: string): MaterialGateModel {
  const text = expr.replace(/\s+/g, '');
  if (/UnlockPredicate\.always\(\)/.test(text)) return alwaysGate();
  const degree = /UnlockPredicate\.minDegree\(([-\d.]+)[fF]?\)/.exec(text);
  if (degree) return { type: 'DEGREE', value: Number(degree[1]) };
  const purity = /UnlockPredicate\.minPurity\(([-\d.]+)[fF]?\)/.exec(text);
  if (purity) return { type: 'PURITY', value: Number(purity[1]) };
  const clarity = /UnlockPredicate\.minClarity\(([-\d.]+)[fF]?\)/.exec(text);
  if (clarity) return { type: 'CLARITY', value: Number(clarity[1]) };
  return alwaysGate();
}

function renderMaterialGate(gate: MaterialGateModel): string {
  switch (gate.type) {
    case 'DEGREE': return `d(${Math.round(gate.value ?? 0)})`;
    case 'PURITY': return `p(${formatFloat(gate.value ?? 0, true)})`;
    case 'CLARITY': return `c(${formatFloat(gate.value ?? 0, true)})`;
    default: return 'a()';
  }
}

function renderUnlockPredicate(gate: MaterialGateModel): string {
  switch (gate.type) {
    case 'DEGREE': return `UnlockPredicate.minDegree(${Math.round(gate.value ?? 0)})`;
    case 'PURITY': return `UnlockPredicate.minPurity(${formatFloat(gate.value ?? 0, false)})`;
    case 'CLARITY': return `UnlockPredicate.minClarity(${formatFloat(gate.value ?? 0, false)})`;
    default: return 'UnlockPredicate.always()';
  }
}

function formatFloat(value: number, upper: boolean): string {
  const suffix = upper ? 'F' : 'f';
  return `${Number(value).toFixed(1)}${suffix}`;
}

function alwaysGate(): MaterialGateModel {
  return { type: 'ALWAYS', value: null };
}

function scanHelperCalls(source: string, baseOffset: number, helpers: string[]): Array<{ text: string; start: number; end: number }> {
  const calls: Array<{ text: string; start: number; end: number }> = [];
  for (let i = 0; i < source.length; i++) {
    if (!helpers.some(helper => source.startsWith(`${helper}(`, i))) continue;
    if (i > 0 && /[A-Za-z0-9_]/.test(source[i - 1])) continue;
    const open = source.indexOf('(', i);
    const close = findMatchingParen(source, open);
    if (close < 0) continue;
    const semicolon = source.indexOf(';', close);
    const end = semicolon >= 0 ? semicolon + 1 : close + 1;
    calls.push({
      text: source.slice(i, end),
      start: baseOffset + i,
      end: baseOffset + end
    });
    i = end - 1;
  }
  return calls;
}

function methodBodySpan(source: string, methodName: string): Span | null {
  const pattern = new RegExp(`(?:private|public|protected)\\s+static\\s+[^\\s(<>]+(?:<[^>]+>)?\\s+${methodName}\\s*\\(`);
  const match = pattern.exec(source);
  if (!match || match.index == null) return null;
  const brace = source.indexOf('{', match.index);
  if (brace < 0) return null;
  const close = findMatchingBrace(source, brace);
  if (close < 0) return null;
  return { start: brace + 1, end: close };
}

function findMatchingParen(text: string, openParenIndex: number): number {
  let depth = 0;
  let inString = false;
  let inLineComment = false;
  let inBlockComment = false;
  for (let i = openParenIndex; i < text.length; i++) {
    const ch = text[i];
    const next = text[i + 1];
    if (inLineComment) {
      if (ch === '\n') inLineComment = false;
      continue;
    }
    if (inBlockComment) {
      if (ch === '*' && next === '/') {
        inBlockComment = false;
        i++;
      }
      continue;
    }
    if (!inString && ch === '/' && next === '/') {
      inLineComment = true;
      i++;
      continue;
    }
    if (!inString && ch === '/' && next === '*') {
      inBlockComment = true;
      i++;
      continue;
    }
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '(') depth++;
    if (ch === ')') {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

function findMatchingBrace(text: string, openBraceIndex: number): number {
  let depth = 0;
  let inString = false;
  let inLineComment = false;
  let inBlockComment = false;
  for (let i = openBraceIndex; i < text.length; i++) {
    const ch = text[i];
    const next = text[i + 1];
    if (inLineComment) {
      if (ch === '\n') inLineComment = false;
      continue;
    }
    if (inBlockComment) {
      if (ch === '*' && next === '/') {
        inBlockComment = false;
        i++;
      }
      continue;
    }
    if (!inString && ch === '/' && next === '/') {
      inLineComment = true;
      i++;
      continue;
    }
    if (!inString && ch === '/' && next === '*') {
      inBlockComment = true;
      i++;
      continue;
    }
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '{') depth++;
    if (ch === '}') {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

function splitTopLevelArgs(text: string): Array<{ text: string; start: number; end: number }> {
  const result: Array<{ text: string; start: number; end: number }> = [];
  let depthParen = 0;
  let depthBrace = 0;
  let inString = false;
  let start = 0;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '(') depthParen++;
    else if (ch === ')') depthParen--;
    else if (ch === '{') depthBrace++;
    else if (ch === '}') depthBrace--;
    else if (ch === ',' && depthParen === 0 && depthBrace === 0) {
      result.push({ text: text.slice(start, i), start, end: i });
      start = i + 1;
    }
  }
  result.push({ text: text.slice(start), start, end: text.length });
  return result;
}

function parseStringLiteral(token: string): string | null {
  const match = /^"((?:\\.|[^"])*)"$/.exec(token.trim());
  return match ? unescapeJavaString(match[1]) : null;
}

function stripCatalogSpan(entry: ParsedCatalogEntry): MaterialCatalogEntryModel {
  const { span: _span, ...model } = entry;
  return model;
}

function catalogKey(entry: Pick<MaterialCatalogEntryModel, 'path' | 'id'>): string {
  return `${entry.path}:${entry.id}`;
}

function normalizeColorLiteral(value: string): string {
  const trimmed = value.trim();
  if (trimmed.startsWith('0x')) return `0x${trimmed.slice(2).toUpperCase()}`;
  if (trimmed.startsWith('#')) {
    const raw = trimmed.slice(1).toUpperCase();
    return raw.length === 6 ? `0xFF${raw}` : `0x${raw}`;
  }
  const num = Number(trimmed);
  if (!Number.isFinite(num)) return trimmed;
  return `0x${(num >>> 0).toString(16).toUpperCase().padStart(8, '0')}`;
}

function escapeJavaString(value: string): string {
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}

function unescapeJavaString(value: string): string {
  return value.replace(/\\"/g, '"').replace(/\\\\/g, '\\');
}

function isLineCommented(source: string, index: number): boolean {
  const lineStart = source.lastIndexOf('\n', index) + 1;
  const prefix = source.slice(lineStart, index);
  return prefix.includes('//');
}

function issue(severity: Diagnostic['severity'], code: string, message: string, file: string): Diagnostic {
  return { severity, code, message, file };
}
