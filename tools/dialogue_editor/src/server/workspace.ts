import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
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

  return { repoRoot, dialogueFiles, translations, inquiries, registries, events, memos, diagnostics };
}

export async function loadDialogueFile(repoRoot: string, filePath: string): Promise<DialogueFile> {
  const abs = safeResolve(repoRoot, filePath);
  return parseDialogueJava(filePath, readFileSync(abs, 'utf8'));
}

export async function previewWorkspaceChanges(repoRoot: string, request: PreviewRequest): Promise<PreviewResult> {
  const diagnostics: Diagnostic[] = [];
  const changes = new Map<string, string>();

  for (const file of request.files ?? []) {
    changes.set(file.path, file.content);
  }

  for (const file of request.dialogueFiles ?? []) {
    const original = readFileSync(safeResolve(repoRoot, file.path), 'utf8');
    changes.set(file.path, renderDialogueFile(original, file));
  }

  if (request.translations) {
    const langPath = 'src/main/resources/assets/hemomancy/lang/en_us.json';
    const current = readJson<Record<string, string>>(safeResolve(repoRoot, langPath), {});
    const merged = { ...current, ...request.translations };
    changes.set(langPath, JSON.stringify(sortObject(merged), null, 2) + '\n');
  }

  for (const inquiry of request.inquiries ?? []) {
    changes.set(inquiry.path, JSON.stringify(renderInquiryJson(inquiry), null, 2) + '\n');
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
  return result;
}

export async function applyPreview(repoRoot: string, previewId: string): Promise<void> {
  const preview = previews.get(previewId);
  if (!preview) throw new Error(`Unknown preview id: ${previewId}`);
  if (!preview.canApply) throw new Error('Preview has blocking diagnostics and cannot be applied.');
  for (const diff of preview.diffs) {
    const abs = safeResolve(repoRoot, diff.path);
    mkdirSync(dirname(abs), { recursive: true });
    writeFileSync(abs, diff.after, 'utf8');
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
