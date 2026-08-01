import { existsSync, readFileSync } from 'node:fs';
import type {
  Diagnostic,
  PreviewResult,
  RecipeMapEditorEntry,
  RecipeMapEditorTab,
  RecipeMapPreviewRequest,
  RecipeMapWorkspace
} from '../shared/types';
import { makeFileDiff } from './diff';
import { parseRecipeMapDefinitionsJava, renderRecipeMapDefinitionsJava } from './recipeMapParser';
import { hasBlockingDiagnostics } from './validation';
import { safeResolve, storePreview } from './workspace';

const definitionsPath = 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java';

export async function loadRecipeMapWorkspace(repoRoot: string): Promise<RecipeMapWorkspace> {
  const source = readFileSync(safeResolve(repoRoot, definitionsPath), 'utf8');
  const parsed = parseRecipeMapDefinitionsJava(definitionsPath, source);
  const tabs = parsed.tabs.map(tab => ({
    ...tab,
    families: [...tab.families],
    entries: tab.entries.map(entry => attachRecipeMetadata(repoRoot, entry)),
    links: tab.links.map(link => ({ ...link }))
  }));
  return {
    repoRoot,
    tabs,
    diagnostics: [...parsed.diagnostics, ...validateRecipeMaps(tabs)]
  };
}

export async function previewRecipeMapWorkspaceChanges(repoRoot: string, request: RecipeMapPreviewRequest): Promise<PreviewResult> {
  const source = readFileSync(safeResolve(repoRoot, definitionsPath), 'utf8');
  const tabs = cloneTabs(request.tabs ?? []);
  const diagnostics = validateRecipeMaps(tabs);
  const after = renderRecipeMapDefinitionsJava(source, tabs);
  const diffs = source === after ? [] : [makeFileDiff(definitionsPath, source, after)];
  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics,
    canApply: diffs.length > 0 && !hasBlockingDiagnostics(diagnostics)
  };
  storePreview(result);
  return result;
}

function attachRecipeMetadata(repoRoot: string, entry: RecipeMapEditorEntry): RecipeMapEditorEntry {
  const recipePath = `src/main/resources/data/hemomancy/recipe/${entry.id}.json`;
  const absolute = safeResolve(repoRoot, recipePath);
  if (!existsSync(absolute)) return entry;
  try {
    const json = JSON.parse(readFileSync(absolute, 'utf8')) as Record<string, unknown>;
    const column = Number(json.required_degree ?? 0);
    const displayName = typeof json.riteName === 'string'
      ? json.riteName
      : resultDisplayName(json.result) ?? entry.displayName;
    return { ...entry, column: Number.isFinite(column) ? Math.max(0, Math.min(8, Math.round(column))) : 0, displayName };
  } catch {
    return entry;
  }
}

function resultDisplayName(result: unknown): string | null {
  if (!result || typeof result !== 'object') return null;
  const id = (result as Record<string, unknown>).id;
  if (typeof id !== 'string') return null;
  return labelize(id.substring(id.indexOf(':') + 1));
}

function validateRecipeMaps(tabs: RecipeMapEditorTab[]): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];
  for (const tab of tabs) {
    const ids = new Set<string>();
    const linkKeys = new Set<string>();
    for (const entry of tab.entries) {
      if (ids.has(entry.id)) diagnostics.push(issue('error', 'recipe_map_duplicate_entry', `${tab.key} contains duplicate entry ${entry.id}.`, entry.id));
      ids.add(entry.id);
      if (!tab.families.includes(entry.family)) diagnostics.push(issue('error', 'recipe_map_unknown_family', `${entry.id} references unknown family ${entry.family}.`, entry.id));
    }
    for (const link of tab.links) {
      const key = `${link.from}|${link.to}|${link.kind}`;
      if (linkKeys.has(key)) diagnostics.push(issue('error', 'recipe_map_duplicate_link', `${tab.key} contains a duplicate ${link.kind.toLowerCase()} link.`, link.from));
      linkKeys.add(key);
      if (link.from === link.to) diagnostics.push(issue('error', 'recipe_map_self_link', `${link.from} cannot link to itself.`, link.from));
      if (!ids.has(link.from) || !ids.has(link.to)) diagnostics.push(issue('error', 'recipe_map_unknown_link_entry', `Link references an entry outside ${tab.key}.`, link.from));
    }
  }
  return diagnostics;
}

function cloneTabs(tabs: RecipeMapEditorTab[]): RecipeMapEditorTab[] {
  return tabs.map(tab => ({
    ...tab,
    families: [...tab.families],
    entries: tab.entries.map(entry => ({ ...entry })),
    links: tab.links.map(link => ({ ...link }))
  }));
}

function issue(severity: Diagnostic['severity'], code: string, message: string, skill?: string): Diagnostic {
  return { severity, code, message, file: definitionsPath, skill };
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}
