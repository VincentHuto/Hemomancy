import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { dirname, join, relative, resolve, sep } from 'node:path';
import type {
  Diagnostic,
  DialogueFile,
  DialogueInquiryCondition,
  DialogueInquiryEntry,
  DialogueWorkspace,
  FileChange,
  NpcMetadata,
  PreviewRequest,
  PreviewResult,
  RegistryEntry
} from '../shared/types';
import {
  extractEventConstants,
  extractEventCatalog,
  extractMemoCatalog,
  insertEventStubs,
  parseDialogueJava,
  renderDialogueFile
} from './dialogueParser';
import { makeFileDiff } from './diff';
import { hasBlockingDiagnostics, validateDialogueFile } from './validation';

const previews = new Map<string, PreviewResult>();
const previewRevisions = new Map<string, string>();

export function workspaceRevision(repoRoot: string): string {
  const candidates = [
    join(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue'),
    join(repoRoot, 'src/main/resources/assets/hemomancy/lang/en_us.json'),
    join(repoRoot, 'src/main/resources/data/hemomancy/dialogue_inquiry')
  ];
  const files = candidates.flatMap(candidate => {
    if (!existsSync(candidate)) return [];
    try { return readdirSync(candidate, { withFileTypes: true }).length >= 0 ? walkFiles(candidate) : [candidate]; }
    catch { return [candidate]; }
  });
  const metadataDir = join(repoRoot, 'tools/dialogue_editor');
  if (existsSync(metadataDir)) files.push(...readdirSync(metadataDir).filter(name => name.endsWith('Metadata.json')).map(name => join(metadataDir, name)));
  files.sort();
  const hash = createHash('sha256');
  files.forEach(file => { hash.update(relative(repoRoot, file).replaceAll('\\', '/')); hash.update(readFileSync(file)); });
  return hash.digest('hex');
}

export function loadMetadata(metadataDir: string, speaker: string): NpcMetadata {
  const path = join(metadataDir, `${speaker}Metadata.json`);
  if (!existsSync(path)) return { version: 1, options: {} };
  try {
    return JSON.parse(readFileSync(path, 'utf8')) as NpcMetadata;
  } catch {
    return { version: 1, options: {} };
  }
}

export function saveMetadata(metadataDir: string, speaker: string, data: NpcMetadata): void {
  const path = join(metadataDir, `${speaker}Metadata.json`);
  writeFileSync(path, JSON.stringify(data, null, 2) + '\n', 'utf8');
}

export function defaultRepoRoot(): string {
  return resolve(process.cwd(), '..', '..');
}

export function safeResolve(root: string, relPath: string): string {
  const resolved = resolve(root, relPath);
  const rootResolved = resolve(root);
  if (resolved !== rootResolved && !resolved.startsWith(rootResolved + sep)) {
    throw new Error(`Path escapes repository root: ${relPath}`);
  }
  return resolved;
}

export async function loadWorkspace(repoRoot = defaultRepoRoot()): Promise<DialogueWorkspace> {
  const dialogueRoot = join(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue');
  const langPath = 'src/main/resources/assets/hemomancy/lang/en_us.json';
  const translations = readJson<Record<string, string>>(safeResolve(repoRoot, langPath), {});
  const eventSource = readFileSync(join(dialogueRoot, 'DialogueEventHandler.java'), 'utf8');
  const dialogueSourceFiles = readdirSync(dialogueRoot)
    .filter(name => name.endsWith('DialogueTrees.java'))
    .sort()
    .map(name => {
      const abs = join(dialogueRoot, name);
      return {
        name,
        abs,
        relPath: relative(repoRoot, abs).replaceAll('\\', '/'),
        source: readFileSync(abs, 'utf8')
      };
    });
  const eventConstants = new Map<string, string>();
  for (const file of dialogueSourceFiles) {
    for (const [constant, value] of extractEventConstants(file.name, file.source)) {
      eventConstants.set(constant, value);
    }
  }
  const memoPath = findFirstFile(join(repoRoot, 'src/main/java'), 'MemoDefinitions.java');
  const memoSource = memoPath ? readFileSync(memoPath, 'utf8') : '';
  const events = extractEventCatalog(eventSource, eventConstants);
  const memos = extractMemoCatalog(memoSource);
  const knownEvents = new Set([...events.map(e => e.id), ...memos.map(m => `memo_capture:${m.id}`)]);

  const dialogueFiles = dialogueSourceFiles
    .map(sourceFile => {
      const file = parseDialogueJava(sourceFile.relPath, sourceFile.source);
      file.diagnostics = validateDialogueFile(file, translations, knownEvents);
      return file;
    });
  const metadata = Object.fromEntries(dialogueFiles.map(file => {
    const slug = file.path.split(/[\\/]/).at(-1)?.replace(/DialogueTrees\.java$/, '') ?? file.speaker;
    return [slug, loadMetadata(join(repoRoot, 'tools/dialogue_editor'), slug)];
  }));

  const inquiries = loadInquiries(repoRoot);
  const registries = loadRegistryEntries(repoRoot, inquiries);
  const diagnostics = [
    ...dialogueFiles.flatMap(file => file.diagnostics),
    ...inquiries.filter(entry => !entry.valid).map(entry => ({
      severity: 'error' as const,
      code: 'invalid_inquiry_json',
      message: entry.error ?? 'Invalid dialogue inquiry JSON.',
      file: entry.path
    }))
  ];

  return { repoRoot, revision: workspaceRevision(repoRoot), dialogueFiles, translations, inquiries, registries, events, memos, diagnostics, metadata };
}

export async function loadDialogueFile(repoRoot: string, filePath: string): Promise<DialogueFile> {
  const abs = safeResolve(repoRoot, filePath);
  return parseDialogueJava(filePath, readFileSync(abs, 'utf8'));
}

export async function previewWorkspaceChanges(repoRoot: string, request: PreviewRequest): Promise<PreviewResult> {
  const diagnostics: Diagnostic[] = [];
  const changes = new Map<string, string>();
  const currentRevision = workspaceRevision(repoRoot);

  if (request.baseRevision && request.baseRevision !== currentRevision) {
    diagnostics.push({ severity: 'error', code: 'workspace_changed', message: 'Dialogue source changed outside the studio. Reload and reconcile the recovered draft before applying.' });
  }

  for (const file of request.files ?? []) {
    changes.set(file.path, file.content);
  }

  const langPath = 'src/main/resources/assets/hemomancy/lang/en_us.json';
  const currentTranslations = readJson<Record<string, string>>(safeResolve(repoRoot, langPath), {});
  const effectiveTranslations = { ...currentTranslations, ...(request.translations ?? {}) };
  const knownEvents = new Set(request.newEvents ?? []);
  const handlerPath = join(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java');
  if (existsSync(handlerPath)) {
    const currentWorkspace = await loadWorkspace(repoRoot);
    currentWorkspace.events.forEach(event => knownEvents.add(event.id));
    currentWorkspace.memos.forEach(memo => knownEvents.add(`memo_capture:${memo.id}`));
  }
  for (const file of request.dialogueFiles ?? []) {
    diagnostics.push(...validateDialogueFile(file, effectiveTranslations, knownEvents));
    const original = readFileSync(safeResolve(repoRoot, file.path), 'utf8');
    changes.set(file.path, renderDialogueFile(original, file));
  }

  if (request.translations && Object.keys(request.translations).length > 0) {
    const merged = { ...currentTranslations, ...request.translations };
    changes.set(langPath, JSON.stringify(sortObject(merged), null, 2) + '\n');
  }

  for (const inquiry of request.inquiries ?? []) {
    changes.set(inquiry.path, JSON.stringify(renderInquiryJson(inquiry), null, 2) + '\n');
  }

  for (const [slug, metadata] of Object.entries(request.metadata ?? {})) {
    const path = `tools/dialogue_editor/${slug}Metadata.json`;
    changes.set(path, JSON.stringify(metadata, null, 2) + '\n');
  }

  if (request.newEvents?.length) {
    const handlerPath = 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java';
    const source = changes.get(handlerPath) ?? readFileSync(safeResolve(repoRoot, handlerPath), 'utf8');
    changes.set(handlerPath, insertEventStubs(source, request.newEvents));
  }

  const diffs = [...changes.entries()]
    .map(([path, content]) => {
      const abs = safeResolve(repoRoot, path);
      const before = existsSync(abs) ? readFileSync(abs, 'utf8') : '';
      return before === content ? null : makeFileDiff(path, before, content);
    })
    .filter((diff): diff is NonNullable<typeof diff> => diff !== null);

  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics,
    canApply: !hasBlockingDiagnostics(diagnostics)
  };
  previews.set(result.id, result);
  previewRevisions.set(result.id, currentRevision);
  return result;
}

export async function applyPreview(repoRoot: string, previewId: string): Promise<void> {
  const preview = previews.get(previewId);
  if (!preview) throw new Error(`Unknown preview id: ${previewId}`);
  if (!preview.canApply) throw new Error('Preview has blocking diagnostics and cannot be applied.');
  const expectedRevision = previewRevisions.get(previewId);
  if (expectedRevision && workspaceRevision(repoRoot) !== expectedRevision) throw new Error('Dialogue source changed after preview. Generate a new preview before applying.');
  for (const diff of preview.diffs) {
    const abs = safeResolve(repoRoot, diff.path);
    mkdirSync(dirname(abs), { recursive: true });
    writeFileSync(abs, diff.after, 'utf8');
  }
}

function walkFiles(root: string): string[] {
  if (!existsSync(root)) return [];
  try {
    const entries = readdirSync(root, { withFileTypes: true });
    return entries.flatMap(entry => entry.isDirectory() ? walkFiles(join(root, entry.name)) : [join(root, entry.name)]);
  } catch {
    return [root];
  }
}

function loadInquiries(repoRoot: string): DialogueInquiryEntry[] {
  const root = join(repoRoot, 'src/main/resources/data/hemomancy/dialogue_inquiry');
  if (!existsSync(root)) return [];
  return walkJson(root).map(abs => {
    const rel = relative(repoRoot, abs).replaceAll('\\', '/');
    const parts = relative(root, abs).split(sep);
    try {
      const json = JSON.parse(readFileSync(abs, 'utf8')) as { lines?: unknown; conditions?: unknown };
      const conditions = readInquiryConditions(json.conditions);
      const lines = Array.isArray(json.lines)
        ? json.lines.map(String)
        : conditions.flatMap(condition => condition.lines);
      return {
        path: rel,
        npcId: parts[0] ?? '',
        itemId: parts.slice(1).join('/').replace(/\.json$/, ''),
        lines,
        conditions: conditions.length ? conditions : undefined,
        valid: Array.isArray(json.lines) || conditions.length > 0
      };
    } catch (err) {
      return {
        path: rel,
        npcId: parts[0] ?? '',
        itemId: parts.slice(1).join('/').replace(/\.json$/, ''),
        lines: [],
        valid: false,
        error: err instanceof Error ? err.message : String(err)
      };
    }
  });
}

function readInquiryConditions(value: unknown): DialogueInquiryCondition[] {
  if (!Array.isArray(value)) return [];
  return value
    .filter((condition): condition is Record<string, unknown> => typeof condition === 'object' && condition !== null)
    .filter(condition => Array.isArray(condition.lines))
    .map(condition => ({
      ...condition,
      lines: (condition.lines as unknown[]).map(String)
    }));
}

function renderInquiryJson(inquiry: DialogueInquiryEntry): { lines: string[] } | { conditions: DialogueInquiryCondition[] } {
  if (!inquiry.conditions?.length) return { lines: inquiry.lines };
  const remaining = [...inquiry.lines];
  const conditions = inquiry.conditions.map(condition => {
    const lineCount = condition.lines.length;
    return {
      ...condition,
      lines: remaining.splice(0, lineCount)
    };
  });
  if (remaining.length) {
    conditions.at(-1)!.lines.push(...remaining);
  }
  return { conditions };
}

function loadRegistryEntries(repoRoot: string, inquiries: DialogueInquiryEntry[]): RegistryEntry[] {
  const existingInquiryItems = new Set(inquiries.map(inquiry => inquiry.itemId));
  const files: Array<{ path: string; kind: RegistryEntry['kind'] }> = [
    {
      path: 'src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java',
      kind: 'item'
    },
    {
      path: 'src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java',
      kind: 'block'
    }
  ];
  const entries = new Map<string, RegistryEntry>();

  for (const file of files) {
    const abs = safeResolve(repoRoot, file.path);
    if (!existsSync(abs)) continue;
    const source = readFileSync(abs, 'utf8').replace(/\/\/.*$/gm, '');
    const regex = /\b([A-Z_]+)\.register\(\s*"([^"]+)"/g;
    let match: RegExpExecArray | null;
    while ((match = regex.exec(source))) {
      const registryName = match[1];
      const id = match[2];
      if (file.kind === 'item' && !registryName.includes('ITEM')) continue;
      if (file.kind === 'block' && !registryName.includes('BLOCK')) continue;
      const key = `${file.kind}:${id}`;
      entries.set(key, {
        id,
        kind: file.kind,
        source: file.path,
        hasInquiry: existingInquiryItems.has(`hemomancy/${id}`)
      });
    }
  }

  return [...entries.values()].sort((a, b) => a.kind.localeCompare(b.kind) || a.id.localeCompare(b.id));
}

function findFirstFile(root: string, fileName: string): string | null {
  if (!existsSync(root)) return null;
  for (const entry of readdirSync(root, { withFileTypes: true })) {
    const abs = join(root, entry.name);
    if (entry.isFile() && entry.name === fileName) return abs;
    if (entry.isDirectory()) {
      const found = findFirstFile(abs, fileName);
      if (found) return found;
    }
  }
  return null;
}

function walkJson(root: string): string[] {
  const out: string[] = [];
  for (const entry of readdirSync(root, { withFileTypes: true })) {
    const abs = join(root, entry.name);
    if (entry.isDirectory()) out.push(...walkJson(abs));
    if (entry.isFile() && entry.name.endsWith('.json')) out.push(abs);
  }
  return out.sort();
}

function readJson<T>(path: string, fallback: T): T {
  try {
    return JSON.parse(readFileSync(path, 'utf8')) as T;
  } catch {
    return fallback;
  }
}

function sortObject(obj: Record<string, string>): Record<string, string> {
  return Object.fromEntries(Object.entries(obj).sort(([a], [b]) => a.localeCompare(b)));
}
