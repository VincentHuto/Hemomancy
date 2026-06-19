import '../frontend/styles.css';
import type { Diagnostic, DialogueFile, DialogueInquiryEntry, DialogueTreeModel, DialogueWorkspace, NpcMetadata, PreviewResult } from '../shared/types';
import { applyPreview as applyPreviewApi, fetchMetadata, fetchPreview, loadWorkspace as loadWorkspaceApi, pushMetadata } from './api';
import { renderGraph } from './graph';
import { renderInspector } from './inspector';
import { renderSidebar } from './sidebar';
import { currentFile, paletteFor, pushUndo, redo, speakerSlug, state, undo } from './state';
import type { Tab } from './state';

const tabs: Tab[] = ['Graph', 'Translations', 'Events', 'Item Inquiries', 'Validation', 'Diff'];

document.querySelector<HTMLDivElement>('#app')!.innerHTML = `
  <div class="shell">
    <header class="topbar">
      <div class="brand">Hemomancy Dialogue Workspace</div>
      <button id="undo" disabled>↩ Undo</button>
      <button id="redo" disabled>↪ Redo</button>
      <button id="reload">Reload</button>
      <button id="preview">Preview Diff</button>
      <button id="apply" class="primary" disabled>Apply Preview</button>
      <div class="status" id="status"></div>
    </header>
    <div class="layout">
      <aside class="sidebar" id="sidebar"></aside>
      <main class="main">
        <nav class="tabs" id="tabs"></nav>
        <section class="content" id="content"></section>
      </main>
      <aside class="inspector" id="inspector"></aside>
    </div>
    <div id="translation-popover" class="translation-popover" hidden></div>
  </div>`;

document.getElementById('reload')!.onclick = () => init();
document.getElementById('preview')!.onclick = () => runPreview();
document.getElementById('apply')!.onclick = () => runApply();
document.getElementById('undo')!.onclick = () => {
  const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
  if (undo()) { render(); syncMeta(beforeMeta); }
};
document.getElementById('redo')!.onclick = () => {
  const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
  if (redo()) { render(); syncMeta(beforeMeta); }
};
document.addEventListener('keydown', e => {
  const mod = e.metaKey || e.ctrlKey;
  if (!mod) return;
  if (e.key === 'z' && !e.shiftKey) {
    e.preventDefault();
    const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
    if (undo()) { render(); syncMeta(beforeMeta); }
  } else if (e.key === 'y' || (e.key === 'z' && e.shiftKey)) {
    e.preventDefault();
    const beforeMeta = JSON.parse(JSON.stringify(state.metadata)) as Record<string, NpcMetadata>;
    if (redo()) { render(); syncMeta(beforeMeta); }
  }
});
document.addEventListener('graph-drag-render', () => {
  renderGraph(document.getElementById('content')!, render);
});

init();

async function init(): Promise<void> {
  state.message = 'Loading workspace...';
  render();
  await loadWorkspaceApi();
  state.fileIndex = 0;
  state.selectedRow = null;
  state.preview = null;
  state.undoStack = [];
  state.redoStack = [];
  if (state.workspace) {
    await Promise.all(state.workspace.dialogueFiles.map(async (f) => {
      const slug = speakerSlug(f);
      state.metadata[slug] = await fetchMetadata(slug);
    }));
  }
  state.message = `${state.workspace?.dialogueFiles.length ?? 0} dialogue files loaded`;
  render();
}

function render(): void {
  applyThemeClass();
  document.getElementById('status')!.textContent = state.message;
  (document.getElementById('apply') as HTMLButtonElement).disabled = !state.preview?.canApply;
  (document.getElementById('undo') as HTMLButtonElement).disabled = !state.undoStack.length;
  (document.getElementById('redo') as HTMLButtonElement).disabled = !state.redoStack.length;
  renderSidebar(document.getElementById('sidebar')!, render);
  renderTabs();
  renderContent();
  renderInspector(document.getElementById('inspector')!, render);
}

function applyThemeClass(): void {
  const shell = document.querySelector<HTMLElement>('.shell');
  if (!shell) return;
  shell.classList.remove('theme-harbinger', 'theme-fungal', 'theme-unstained');
  shell.classList.add(`theme-${paletteFor(currentFile())}`);
}

function renderTabs(): void {
  const el = document.getElementById('tabs')!;
  el.innerHTML = tabs.map(t => `<button class="tab ${t === state.tab ? 'active' : ''}" data-tab="${t}">${t}</button>`).join('');
  el.querySelectorAll<HTMLButtonElement>('[data-tab]').forEach(btn => btn.onclick = () => {
    state.tab = btn.dataset.tab as Tab;
    render();
  });
}

function renderContent(): void {
  const el = document.getElementById('content')!;
  if (!state.workspace) { el.innerHTML = '<div class="empty">Loading workspace...</div>'; return; }
  if (state.tab === 'Graph') renderGraph(el, render);
  else if (state.tab === 'Translations') renderTranslations(el);
  else if (state.tab === 'Events') renderEvents(el);
  else if (state.tab === 'Item Inquiries') renderInquiries(el);
  else if (state.tab === 'Validation') renderValidation(el);
  else if (state.tab === 'Diff') renderDiff(el);
}

function escapeHtml(v: string): string { return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'); }
function escapeAttr(v: string): string { return escapeHtml(v).replace(/"/g, '&quot;'); }

function syncMeta(before: Record<string, NpcMetadata>): void {
  const after = state.metadata;
  const all = new Set([...Object.keys(before), ...Object.keys(after)]);
  for (const slug of all) {
    if (JSON.stringify(before[slug]) !== JSON.stringify(after[slug])) {
      pushMetadata(slug, after[slug] ?? { version: 1, options: {} });
    }
  }
}

function renderTranslations(el: HTMLElement): void {
  const ws = state.workspace!;
  const file = currentFile();
  const keys = new Set<string>();
  file?.trees.forEach(t => t.nodes.forEach(n => {
    n.lines.forEach(k => keys.add(k));
    n.options.forEach(o => keys.add(o.text));
  }));
  const sorted = [...keys].sort();
  el.innerHTML = `<div class="grid-list">
    ${sorted.map(key => `<div class="item-card">
      <div class="field"><label>${escapeHtml(key)}</label>
        <textarea data-translation="${escapeAttr(key)}">${escapeHtml(state.dirtyTranslations[key] ?? ws.translations[key] ?? '')}</textarea>
      </div></div>`).join('')}
    ${sorted.length === 0 ? '<div class="empty">No translation keys for this file.</div>' : ''}
  </div>`;
  el.querySelectorAll<HTMLTextAreaElement>('[data-translation]').forEach(ta => {
    ta.oninput = () => { pushUndo(); state.dirtyTranslations[ta.dataset.translation!] = ta.value; state.preview = null; };
  });
}

function renderEvents(el: HTMLElement): void {
  const ws = state.workspace!;
  const usedEvents = new Set(currentFile()?.trees.flatMap(t => t.nodes.flatMap(n => n.options.map(o => o.event).filter(Boolean))) as string[]);
  el.innerHTML = `
    <div class="item-card">
      <div class="field"><label>New Event ID</label><input id="new-event" placeholder="my_new_event"></div>
      <button id="add-event">Generate Handler Stub In Preview</button>
    </div>
    <div class="grid-list">
      ${[...ws.events, ...ws.memos].map(ev => `
        <div class="event-row">
          <span class="mono">${escapeHtml(ev.kind === 'memo' ? `memo_capture:${ev.id}` : ev.id)}</span>
          <span class="count">${ev.kind}${usedEvents.has(ev.id) ? ' / used' : ''}</span>
        </div>`).join('')}
    </div>`;
  el.querySelector<HTMLButtonElement>('#add-event')!.onclick = () => {
    const val = (el.querySelector<HTMLInputElement>('#new-event')!).value.trim();
    if (val) { pushUndo(); state.newEvents.add(val); state.preview = null; state.message = `Event stub queued: ${val}`; render(); }
  };
}

function renderInquiries(el: HTMLElement): void {
  const ws = state.workspace!;
  const file = currentFile();
  const raw = `${file?.path ?? ''} ${file?.speaker ?? ''}`.toLowerCase();
  const npcs: string[] = [];
  if (raw.includes('alchemist')) npcs.push('alchemist');
  if (raw.includes('guardian')) npcs.push('guardian');
  if (raw.includes('vicar')) npcs.push('vicar');
  if (raw.includes('zealot')) npcs.push('zealot');

  if (!npcs.length) { el.innerHTML = `<div class="empty">No item inquiry NPC mapped for this file.</div>`; return; }
  const visible = ws.inquiries.filter(e => npcs.includes(e.npcId));
  const defaultNpc = npcs[0];

  el.innerHTML = `
    <div class="item-card inquiry-create">
      <div class="row">
        <div class="field"><label>NPC</label>
          <select id="new-inquiry-npc">${npcs.map(n => `<option value="${escapeAttr(n)}">${escapeHtml(n)}</option>`).join('')}</select>
        </div>
        <div class="field"><label>Item or Block</label>
          <select id="new-inquiry-registry">${ws.registries.map(r => `<option value="${escapeAttr(`${r.kind}:${r.id}`)}">${escapeHtml(`${r.kind} / ${r.id}${r.hasInquiry ? ' / has inquiry' : ''}`)}</option>`).join('')}</select>
        </div>
        <button id="add-inquiry">Add Inquiry</button>
      </div>
    </div>
    <div class="grid-list">
      ${visible.map(entry => {
        const lines = [...(state.dirtyInquiries.get(entry.path)?.lines ?? entry.lines)];
        return `<div class="item-card inquiry-card">
          <div class="row inquiry-card-head">
            <strong>${escapeHtml(entry.npcId)}</strong>
            <span class="count mono">${escapeHtml(entry.itemId)}</span>
            ${state.createdInquiryPaths.has(entry.path) ? `<button class="icon-button danger" data-remove-inquiry="${escapeAttr(entry.path)}">×</button>` : ''}
          </div>
          <div class="hint mono">${escapeHtml(entry.path)}</div>
          ${lines.map((key, li) => `<div class="inquiry-line">
            <div class="field"><label>Line Key ${li + 1}</label>
              <input class="mono" data-inquiry-key="${escapeAttr(entry.path)}" data-line-index="${li}" value="${escapeAttr(key)}">
            </div>
            <div class="field"><label>en_us</label>
              <textarea class="translation-edit" data-inquiry-translation="${escapeAttr(key)}">${escapeHtml(state.dirtyTranslations[key] ?? ws.translations[key] ?? '')}</textarea>
            </div>
          </div>`).join('')}
          <div class="row">
            <button data-add-inquiry-line="${escapeAttr(entry.path)}">Add Line</button>
          </div>
        </div>`;
      }).join('')}
      ${visible.length === 0 ? `<div class="empty">No inquiries yet. Add one above.</div>` : ''}
    </div>`;

  el.querySelector<HTMLButtonElement>('#add-inquiry')!.onclick = () => {
    const npcId = (el.querySelector<HTMLSelectElement>('#new-inquiry-npc')?.value) || defaultNpc;
    const rv = el.querySelector<HTMLSelectElement>('#new-inquiry-registry')?.value;
    const registry = ws.registries.find(r => `${r.kind}:${r.id}` === rv);
    if (!registry) { state.message = 'No item selected.'; render(); return; }
    const path = `src/main/resources/data/hemomancy/dialogue_inquiry/${npcId}/hemomancy/${registry.id}.json`;
    if (ws.inquiries.some(e => e.path === path) || state.dirtyInquiries.has(path)) {
      state.message = `Inquiry already exists for ${npcId} / ${registry.id}.`; render(); return;
    }
    pushUndo();
    const lineKey = `hemomancy.${npcId}.item_inquiry.${registry.id}.line1`;
    const entry: DialogueInquiryEntry = { path, npcId, itemId: `hemomancy/${registry.id}`, lines: [lineKey], valid: true };
    ws.inquiries = [...ws.inquiries, entry].sort((a, b) => a.path.localeCompare(b.path));
    state.dirtyInquiries.set(path, entry);
    state.createdInquiryPaths.add(path);
    state.dirtyTranslations[lineKey] = state.dirtyTranslations[lineKey] ?? '';
    state.preview = null;
    state.message = `Inquiry queued: ${npcId} / ${registry.id}`;
    render();
  };

  el.querySelectorAll<HTMLButtonElement>('[data-remove-inquiry]').forEach(btn => btn.onclick = () => {
    pushUndo();
    const path = btn.dataset.removeInquiry!;
    const entry = state.dirtyInquiries.get(path);
    ws.inquiries = ws.inquiries.filter(e => e.path !== path);
    state.dirtyInquiries.delete(path);
    state.createdInquiryPaths.delete(path);
    entry?.lines.forEach(l => delete state.dirtyTranslations[l]);
    state.preview = null; state.message = 'Queued inquiry discarded.'; render();
  });

  el.querySelectorAll<HTMLInputElement>('[data-inquiry-key]').forEach(input => input.oninput = () => {
    pushUndo();
    const original = ws.inquiries.find(e => e.path === input.dataset.inquiryKey)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    lines[Number(input.dataset.lineIndex)] = input.value.trim();
    state.dirtyInquiries.set(original.path, { ...original, lines: lines.filter(Boolean) });
    state.preview = null;
  });

  el.querySelectorAll<HTMLTextAreaElement>('[data-inquiry-translation]').forEach(ta => ta.oninput = () => {
    pushUndo(); state.dirtyTranslations[ta.dataset.inquiryTranslation!] = ta.value; state.preview = null;
  });

  el.querySelectorAll<HTMLButtonElement>('[data-add-inquiry-line]').forEach(btn => btn.onclick = () => {
    pushUndo();
    const original = ws.inquiries.find(e => e.path === btn.dataset.addInquiryLine)!;
    const lines = [...(state.dirtyInquiries.get(original.path)?.lines ?? original.lines)];
    const topic = original.itemId.split('/').at(-1) || 'new_item';
    lines.push(`hemomancy.${original.npcId}.item_inquiry.${topic}.line${lines.length + 1}`);
    state.dirtyInquiries.set(original.path, { ...original, lines });
    state.preview = null; render();
  });
}

function renderValidation(el: HTMLElement): void {
  const diags = [...(state.workspace?.diagnostics ?? []), ...(state.preview?.diagnostics ?? [])];
  el.innerHTML = diags.length
    ? diags.map(d => `<div class="diagnostic ${d.severity}">
        <strong>${d.severity.toUpperCase()} ${escapeHtml(d.code)}</strong>
        <div>${escapeHtml(d.message)}</div>
        <div class="hint mono">${escapeHtml([d.file, d.tree, d.node].filter(Boolean).join(' / '))}</div>
      </div>`).join('')
    : '<div class="empty">No diagnostics.</div>';
}

function renderDiff(el: HTMLElement): void {
  if (!state.preview) { el.innerHTML = '<div class="empty">Click Preview Diff to generate a patch.</div>'; return; }
  el.innerHTML = state.preview.diffs.length
    ? state.preview.diffs.map(d => `<h3>${escapeHtml(d.path)}</h3><pre class="diff">${escapeHtml(d.patch)}</pre>`).join('')
    : '<div class="empty">No file changes in preview.</div>';
}

async function runPreview(): Promise<void> {
  const file = currentFile();
  if (!file) return;
  state.message = 'Generating preview...'; render();
  state.preview = await fetchPreview(file, state.dirtyTranslations, [...state.dirtyInquiries.values()], [...state.newEvents]);
  state.tab = 'Diff';
  state.message = state.preview.canApply ? 'Preview ready; no files written' : 'Preview has blocking diagnostics';
  render();
}

async function runApply(): Promise<void> {
  if (!state.preview?.canApply) return;
  state.message = 'Applying preview...'; render();
  await applyPreviewApi(state.preview.id);
  state.dirtyTranslations = {};
  state.dirtyInquiries.clear();
  state.createdInquiryPaths.clear();
  state.newEvents.clear();
  await init();
}
