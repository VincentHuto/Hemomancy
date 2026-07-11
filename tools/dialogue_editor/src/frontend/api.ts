import type { DialogueWorkspace, PreviewRequest, PreviewResult } from '../shared/types';

async function expectJson<T>(response: Response): Promise<T> {
  if (!response.ok) throw new Error(await response.text() || `${response.status} ${response.statusText}`);
  return response.json() as Promise<T>;
}

export async function loadWorkspace(): Promise<DialogueWorkspace> {
  return expectJson(await fetch('/api/workspace'));
}

export async function previewChanges(request: PreviewRequest): Promise<PreviewResult> {
  return expectJson(await fetch('/api/preview', {
    method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify(request)
  }));
}

export async function applyPreview(id: string): Promise<void> {
  const response = await fetch('/api/apply', {
    method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ id })
  });
  if (!response.ok) throw new Error(await response.text() || 'Apply failed');
}
