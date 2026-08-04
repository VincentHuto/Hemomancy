import type { PreviewResult, TendenciesPreviewRequest, TendenciesWorkspace } from '../shared/types';
import { loadManipulationWorkspace, previewManipulationWorkspaceChanges } from './manipulationWorkspace';
import { loadScarTreeWorkspace, previewScarTreeWorkspaceChanges } from './scarTreeWorkspace';
import { storePreview } from './workspace';
import { hasBlockingDiagnostics } from './validation';

export async function loadTendenciesWorkspace(repoRoot: string): Promise<TendenciesWorkspace> {
  const [manipulations, scars] = await Promise.all([
    loadManipulationWorkspace(repoRoot),
    loadScarTreeWorkspace(repoRoot)
  ]);
  return {
    repoRoot,
    manipulations,
    scars,
    diagnostics: [...manipulations.diagnostics, ...scars.diagnostics]
  };
}

export async function previewTendenciesWorkspaceChanges(
  repoRoot: string,
  request: TendenciesPreviewRequest
): Promise<PreviewResult> {
  const [manipulationPreview, scarPreview] = await Promise.all([
    previewManipulationWorkspaceChanges(repoRoot, { nodes: request.manipulations ?? [] }),
    previewScarTreeWorkspaceChanges(repoRoot, { nodes: request.scars ?? [] })
  ]);
  const diagnostics = [...manipulationPreview.diagnostics, ...scarPreview.diagnostics];
  const diffs = [...manipulationPreview.diffs, ...scarPreview.diffs];
  const result: PreviewResult = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    diffs,
    diagnostics,
    canApply: diffs.length > 0 && !hasBlockingDiagnostics(diagnostics)
  };
  storePreview(result);
  return result;
}
