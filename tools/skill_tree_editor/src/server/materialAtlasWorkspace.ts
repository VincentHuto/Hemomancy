import { existsSync, readFileSync } from 'node:fs';
import type {
  Diagnostic,
  IconRegistryOptions,
  MaterialAtlasEntryModel,
  MaterialAtlasPathKey,
  MaterialAtlasPathModel,
  MaterialAtlasPreviewRequest,
  MaterialAtlasWorkspace,
  MaterialCatalogEntryModel,
  MaterialGateModel,
  PreviewResult
} from '../shared/types';
import { makeFileDiff } from './diff';
import {
  parseMaterialAtlasSpecJava,
  parseMaterialsDataJava,
  renderMaterialAtlasSpecJava,
  renderMaterialsDataJava
} from './materialAtlasParser';
import { hasBlockingDiagnostics } from './validation';
import { loadIconRegistryOptions, safeResolve, storePreview } from './workspace';

const atlasPath = 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java';
const dataPath = 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java';

export async function loadMaterialAtlasWorkspace(repoRoot: string): Promise<MaterialAtlasWorkspace> {
  const atlasSource = readSource(repoRoot, atlasPath);
  const dataSource = readSource(repoRoot, dataPath);
  const atlas = parseMaterialAtlasSpecJava(atlasPath, atlasSource);
  const catalogue = parseMaterialsDataJava(dataPath, dataSource);
  const iconOptions = loadIconRegistryOptions(repoRoot);
  const paths = attachCatalogue(atlas.paths, catalogue.entries);
  const diagnostics = [
    ...atlas.diagnostics,
    ...catalogue.diagnostics,
    ...validateMaterialAtlas(paths, paths.flatMap(path => path.entries.map(entry => entry.catalog).filter(isDefined)), iconOptions)
  ];

  return {
    repoRoot,
    paths,
    iconOptions,
    diagnostics
  };
}

export async function previewMaterialAtlasWorkspaceChanges(repoRoot: string, request: MaterialAtlasPreviewRequest): Promise<PreviewResult> {
  const atlasSource = readSource(repoRoot, atlasPath);
  const dataSource = readSource(repoRoot, dataPath);
  const iconOptions = loadIconRegistryOptions(repoRoot);
  const paths = (request.paths ?? []).map(path => ({
    ...path,
    hubX: path.hubX,
    hubY: path.hubY,
    hubLabelX: path.hubLabelX,
    hubLabelY: path.hubLabelY,
    buckets: path.buckets.map(bucket => ({ ...bucket })),
    entries: path.entries.map(entry => ({ ...entry, parentIds: [...entry.parentIds], catalog: entry.catalog ? { ...entry.catalog } : undefined }))
  }));
  const catalogueEntries = synchronizeCatalogueGates(paths, request.catalogueEntries ?? []);
  const diagnostics = validateMaterialAtlas(paths, catalogueEntries, iconOptions);

  const changes = new Map<string, string>();
  changes.set(atlasPath, renderMaterialAtlasSpecJava(atlasSource, paths));
  changes.set(dataPath, renderMaterialsDataJava(dataSource, catalogueEntries));

  const diffs = [...changes.entries()]
    .map(([path, after]) => {
      const before = readSource(repoRoot, path);
      return before === after ? null : makeFileDiff(path, before, after);
    })
    .filter((diff): diff is NonNullable<typeof diff> => diff !== null);

  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics,
    canApply: diffs.length > 0 && !hasBlockingDiagnostics(diagnostics)
  };
  storePreview(result);
  return result;
}

function attachCatalogue(paths: MaterialAtlasPathModel[], catalogueEntries: MaterialCatalogEntryModel[]): MaterialAtlasPathModel[] {
  const byKey = new Map(catalogueEntries.map(entry => [entryKey(entry.path, entry.id), entry] as const));
  return paths.map(path => ({
    ...path,
    hubX: path.hubX,
    hubY: path.hubY,
    hubLabelX: path.hubLabelX,
    hubLabelY: path.hubLabelY,
    buckets: path.buckets.map(bucket => ({ ...bucket })),
    entries: path.entries.map(entry => ({
      ...entry,
      parentIds: [...entry.parentIds],
      catalog: byKey.get(entryKey(entry.path, entry.id))
    }))
  }));
}

function synchronizeCatalogueGates(paths: MaterialAtlasPathModel[], requestedCatalogue: MaterialCatalogEntryModel[]): MaterialCatalogEntryModel[] {
  const byKey = new Map<string, MaterialCatalogEntryModel>();
  for (const entry of requestedCatalogue) {
    byKey.set(entryKey(entry.path, entry.id), { ...entry, gate: normalizeGate(entry.gate) });
  }
  for (const path of paths) {
    for (const entry of path.entries) {
      const key = entryKey(entry.path, entry.id);
      const existing = byKey.get(key) ?? entry.catalog;
      if (!existing) continue;
      byKey.set(key, {
        ...existing,
        path: entry.path,
        id: entry.id,
        gate: normalizeGate(entry.gate)
      });
      entry.gate = normalizeGate(entry.gate);
      if (entry.catalog) entry.catalog.gate = normalizeGate(entry.gate);
    }
  }
  return [...byKey.values()];
}

function validateMaterialAtlas(paths: MaterialAtlasPathModel[], catalogueEntries: MaterialCatalogEntryModel[], iconOptions: IconRegistryOptions): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];
  const catalogueByKey = new Map<string, MaterialCatalogEntryModel>();
  const atlasEntriesByPath = new Map<MaterialAtlasPathKey, Map<string, MaterialAtlasEntryModel>>();

  for (const path of paths) {
    const bucketIds = new Set(path.buckets.map(bucket => bucket.id));
    const entryIds = new Map<string, MaterialAtlasEntryModel>();
    atlasEntriesByPath.set(path.path, entryIds);

    for (const bucket of path.buckets) {
      if (!bucket.id.trim()) diagnostics.push(issue('error', 'material_empty_bucket_id', 'Bucket id cannot be empty.', atlasPath));
      if (!/^0x[0-9A-Fa-f]{8}$/.test(bucket.color)) {
        diagnostics.push(issue('error', 'material_invalid_color', `Bucket ${bucket.id} color must be 0xAARRGGBB.`, atlasPath, bucket.id));
      }
    }

    for (const entry of path.entries) {
      if (entryIds.has(entry.id)) {
        diagnostics.push(issue('error', 'material_duplicate_id', `Duplicate material atlas id ${entry.id}.`, atlasPath, entry.id));
      }
      entryIds.set(entry.id, entry);
      if (!bucketIds.has(entry.bucketId)) {
        diagnostics.push(issue('error', 'material_unknown_bucket', `${entry.id} references unknown bucket ${entry.bucketId}.`, atlasPath, entry.id));
      }
      if (!isGateValidForPath(path.path, entry.gate)) {
        diagnostics.push(issue('error', 'material_invalid_gate', `${entry.id} uses ${entry.gate.type} gate on ${path.path}.`, atlasPath, entry.id));
      }
    }

    for (const entry of path.entries) {
      for (const parent of entry.parentIds) {
        if (parent === entry.id || !entryIds.has(parent)) {
          diagnostics.push(issue('error', 'material_unknown_parent', `${entry.id} references unknown parent ${parent}.`, atlasPath, entry.id));
        }
      }
    }

    for (const cycle of parentCycles(entryIds)) {
      diagnostics.push(issue('error', 'material_parent_cycle', `Material parent cycle detected: ${cycle.join(' -> ')}.`, atlasPath, cycle[0]));
    }
  }

  for (const entry of catalogueEntries) {
    const key = entryKey(entry.path, entry.id);
    if (catalogueByKey.has(key)) {
      diagnostics.push(issue('error', 'material_duplicate_id', `Duplicate material catalogue id ${entry.id}.`, dataPath, entry.id));
    }
    catalogueByKey.set(key, entry);
    if (!isGateValidForPath(entry.path, entry.gate)) {
      diagnostics.push(issue('error', 'material_invalid_gate', `${entry.id} uses ${entry.gate.type} unlock on ${entry.path}.`, dataPath, entry.id));
    }
    const iconList = entry.iconSource === 'block' ? iconOptions.blocks : iconOptions.items;
    if (!iconList.includes(entry.iconField)) {
      diagnostics.push(issue('error', 'material_missing_icon_field', `${entry.iconSource === 'block' ? 'BlockInit' : 'ItemInit'}.${entry.iconField} was not found.`, dataPath, entry.id));
    }
    if (!atlasEntriesByPath.get(entry.path)?.has(entry.id)) {
      diagnostics.push(issue('warning', 'material_catalogue_without_atlas', `${entry.id} has catalogue text but no atlas node.`, dataPath, entry.id));
    }
  }

  for (const path of paths) {
    for (const entry of path.entries) {
      if (!catalogueByKey.has(entryKey(entry.path, entry.id))) {
        diagnostics.push(issue('warning', 'material_atlas_without_catalogue', `${entry.id} has an atlas node but no catalogue text.`, atlasPath, entry.id));
      }
    }
  }

  return diagnostics;
}

function isGateValidForPath(path: MaterialAtlasPathKey, gate: MaterialGateModel): boolean {
  if (gate.type === 'ALWAYS') return gate.value === null;
  if (gate.value === null || !Number.isFinite(gate.value) || gate.value < 0) return false;
  if (path === 'HARBINGER') return gate.type === 'DEGREE';
  return gate.type === 'PURITY' || gate.type === 'CLARITY';
}

function normalizeGate(gate: MaterialGateModel): MaterialGateModel {
  if (gate.type === 'ALWAYS') return { type: 'ALWAYS', value: null };
  return { type: gate.type, value: gate.value == null ? 0 : Number(gate.value) };
}

function readSource(repoRoot: string, path: string): string {
  const abs = safeResolve(repoRoot, path);
  return existsSync(abs) ? readFileSync(abs, 'utf8') : '';
}

function entryKey(path: MaterialAtlasPathKey, id: string): string {
  return `${path}:${id}`;
}

function issue(severity: Diagnostic['severity'], code: string, message: string, file: string, skill?: string): Diagnostic {
  return { severity, code, message, file, skill };
}

function parentCycles(entries: Map<string, MaterialAtlasEntryModel>): string[][] {
  const cycles: string[][] = [];
  const visiting = new Set<string>();
  const visited = new Set<string>();
  const reported = new Set<string>();

  const visit = (id: string, stack: string[]): void => {
    if (visiting.has(id)) {
      const cycle = stack.slice(stack.indexOf(id));
      const key = canonicalCycleKey(cycle);
      if (!reported.has(key)) {
        reported.add(key);
        cycles.push(cycle);
      }
      return;
    }
    if (visited.has(id)) return;
    const entry = entries.get(id);
    if (!entry) return;
    visiting.add(id);
    for (const parent of entry.parentIds) {
      if (entries.has(parent)) visit(parent, [...stack, parent]);
    }
    visiting.delete(id);
    visited.add(id);
  };

  for (const id of entries.keys()) visit(id, [id]);
  return cycles;
}

function canonicalCycleKey(cycle: string[]): string {
  const unique = cycle.slice(0, -1);
  if (!unique.length) return cycle.join('>');
  const rotations = unique.map((_, index) => [...unique.slice(index), ...unique.slice(0, index)].join('>'));
  return rotations.sort()[0];
}

function isDefined<T>(value: T | undefined): value is T {
  return value !== undefined;
}
