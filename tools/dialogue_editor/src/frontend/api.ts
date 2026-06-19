import type { DialogueFile, DialogueInquiryEntry, NpcMetadata, PreviewResult } from '../shared/types';
import { state } from './state';

export async function loadWorkspace(): Promise<void> {
  const res = await fetch('/api/workspace');
  state.workspace = await res.json();
}

export async function fetchPreview(
  file: DialogueFile,
  translations: Record<string, string>,
  inquiries: DialogueInquiryEntry[],
  newEvents: string[]
): Promise<PreviewResult> {
  const res = await fetch('/api/preview', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ dialogueFiles: [file], translations, inquiries, newEvents })
  });
  return res.json();
}

export async function applyPreview(previewId: string): Promise<void> {
  await fetch('/api/apply', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ id: previewId })
  });
}

export async function fetchMetadata(slug: string): Promise<NpcMetadata> {
  const res = await fetch(`/api/metadata/${encodeURIComponent(slug)}`);
  return res.json();
}

export async function pushMetadata(slug: string, data: NpcMetadata): Promise<void> {
  await fetch(`/api/metadata/${encodeURIComponent(slug)}`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(data)
  });
}
