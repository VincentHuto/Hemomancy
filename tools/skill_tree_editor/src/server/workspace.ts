import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve, sep } from 'node:path';
import type { IconRegistryOptions, PreviewRequest, PreviewResult, SkillWorkspace } from '../shared/types';
import { makeFileDiff } from './diff';
import { parseSkillBranchJava, renderSkillBranchJava } from './skillParser';
import { hasBlockingDiagnostics, validateSkillWorkspace } from './validation';

const previews = new Map<string, PreviewResult>();

export function storePreview(result: PreviewResult): void {
  previews.set(result.id, result);
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

export async function loadWorkspace(repoRoot = defaultRepoRoot()): Promise<SkillWorkspace> {
  const branchRoot = join(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills');
  const langPath = 'src/main/resources/assets/hemomancy/lang/en_us.json';
  const translations = readJson<Record<string, string>>(safeResolve(repoRoot, langPath), {});
  const branches = existsSync(branchRoot)
    ? readdirSync(branchRoot)
      .filter(name => name.endsWith('SkillBranch.java'))
      .sort()
      .map(name => {
        const abs = join(branchRoot, name);
        const relPath = relative(repoRoot, abs).replaceAll('\\', '/');
        const branch = parseSkillBranchJava(relPath, readFileSync(abs, 'utf8'));
        for (const skill of branch.skills) {
          skill.description = translations[`skill.hemomancy.${skill.name}.desc`] ?? '';
        }
        return branch;
      })
    : [];
  const diagnostics = validateSkillWorkspace(branches, translations);
  return {
    repoRoot,
    branches,
    translations,
    iconOptions: loadIconRegistryOptions(repoRoot),
    diagnostics
  };
}

export async function previewWorkspaceChanges(repoRoot: string, request: PreviewRequest): Promise<PreviewResult> {
  const changes = new Map<string, string>();
  const translations = readJson<Record<string, string>>(
    safeResolve(repoRoot, 'src/main/resources/assets/hemomancy/lang/en_us.json'),
    {}
  );
  const branches = request.branches ?? [];
  const diagnostics = validateSkillWorkspace(branches, { ...translations, ...(request.translations ?? {}) });

  for (const branch of branches) {
    const original = readFileSync(safeResolve(repoRoot, branch.path), 'utf8');
    changes.set(branch.path, renderSkillBranchJava(original, branch));
  }

  if (request.translations) {
    const merged = { ...translations, ...request.translations };
    changes.set(
      'src/main/resources/assets/hemomancy/lang/en_us.json',
      JSON.stringify(sortObject(merged), null, 2) + '\n'
    );
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
  storePreview(result);
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

function readJson<T>(path: string, fallback: T): T {
  if (!existsSync(path)) return fallback;
  try {
    return JSON.parse(readFileSync(path, 'utf8')) as T;
  } catch {
    return fallback;
  }
}

function sortObject<T extends Record<string, unknown>>(value: T): T {
  return Object.fromEntries(Object.entries(value).sort(([a], [b]) => a.localeCompare(b))) as T;
}

function loadIconRegistryOptions(repoRoot: string): IconRegistryOptions {
  return {
    items: loadDeferredHolderFields(safeResolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java')),
    blocks: loadDeferredHolderFields(safeResolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java'))
  };
}

function loadDeferredHolderFields(path: string): string[] {
  if (!existsSync(path)) return [];
  const source = readFileSync(path, 'utf8');
  return [...source.matchAll(/public\s+static\s+final\s+DeferredHolder<[^;=]+>\s+(\w+)\s*=\s*\w+\.register\(/g)]
    .map(match => match[1])
    .sort((a, b) => a.localeCompare(b));
}
