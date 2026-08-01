import type {
  Diagnostic,
  PreviewResult,
  RecipeMapEditorEntry,
  RecipeMapEditorLink,
  RecipeMapEditorTab,
  RecipeMapTabKey,
  RecipeMapWorkspace
} from '../shared/types';
import { layoutRecipeMap, type RecipeMapLayer } from './recipeMapGraph';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';

let workspace: RecipeMapWorkspace | null = null;
let activeMap: RecipeMapTabKey = 'RITES';
let activeLayer: RecipeMapLayer = 'surface';
let selectedId = '';
let currentTab: ViewTab = 'graph';
let preview: PreviewResult | null = null;
let statusText = 'Loading Crafting and Rites maps...';
let isBusy = false;

const appRoot = document.querySelector<HTMLDivElement>('#app');
if (!appRoot) throw new Error('Missing app root.');
const app = appRoot;

void load();

async function load(): Promise<void> {
  isBusy = true;
  render();
  try {
    workspace = await api<RecipeMapWorkspace>('/api/recipe-maps');
    activeMap = workspace.tabs[0]?.key ?? 'RITES';
    selectedId = currentMap().entries[0]?.id ?? '';
    preview = null;
    statusText = `Loaded ${workspace.tabs.reduce((sum, tab) => sum + tab.entries.length, 0)} Crafting and Rites nodes.`;
  } catch (error) {
    statusText = error instanceof Error ? error.message : String(error);
  } finally {
    isBusy = false;
    render();
  }
}

function render(): void {
  if (!workspace) {
    app.innerHTML = `<main class="loading"><div>${escapeHtml(statusText)}</div></main>`;
    return;
  }
  ensureSelection();
  app.innerHTML = `
    <main class="shell recipe-map-editor">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark"></span>
          <div>
            <h1>Crafting &amp; Rites Editor</h1>
            <p>${escapeHtml(relativeRoot())}</p>
            <p><a href="/workspace.html">Skills</a> - <a href="/manipulations.html">Manipulations</a> - <a href="/scars.html">Scars</a> - <a href="/materials.html">Materials</a> - <b>Craft/Rites</b></p>
          </div>
        </div>
        <div class="toolbar">
          <button data-action="reload" ${isBusy ? 'disabled' : ''}>Reload</button>
          <button data-action="preview" ${isBusy ? 'disabled' : ''}>Preview</button>
          <button data-action="apply" ${!preview?.canApply || isBusy ? 'disabled' : ''}>Apply</button>
        </div>
        <div class="branch-list">
          ${workspace.tabs.map(mapButton).join('')}
        </div>
        <div class="skill-list">
          ${currentMap().families.map(familySection).join('')}
        </div>
      </aside>
      <section class="main">
        <header class="topbar">
          <nav class="tabs">
            ${viewButton('graph', 'Map')}
            ${viewButton('validation', `Validation${badge(workspace.diagnostics.length)}`)}
            ${viewButton('diff', `Diff${badge(preview?.diffs.length ?? 0)}`)}
          </nav>
          <div class="history-controls">
            <div class="workspace-toggle" aria-label="Recipe map layer">
              <button class="${activeLayer === 'surface' ? 'active' : ''}" data-layer="surface">Surface (0-4)</button>
              <button class="${activeLayer === 'deep' ? 'active' : ''}" data-layer="deep">Deep (5-8)</button>
            </div>
          </div>
          <div class="status">${escapeHtml(statusText)}</div>
        </header>
        <section class="content">
          <section class="canvas-panel">${renderView()}</section>
          <aside class="inspector">${renderInspector()}</aside>
        </section>
      </section>
    </main>`;
  wireEvents();
}

function renderView(): string {
  if (currentTab === 'validation') return renderDiagnostics(workspace?.diagnostics ?? []);
  if (currentTab === 'diff') return renderDiffs();
  return renderGraph();
}

function renderGraph(): string {
  const layout = layoutRecipeMap(currentMap(), activeLayer);
  const links = layout.links.map(link => `<path class="recipe-map-link ${link.kind.toLowerCase()}" d="M ${link.fromX} ${link.fromY} C ${(link.fromX + link.toX) / 2} ${link.fromY}, ${(link.fromX + link.toX) / 2} ${link.toY}, ${link.toX} ${link.toY}"/>`).join('');
  const rings = layout.rings.map(ring => `<circle class="degree-guide-line" cx="480" cy="480" r="${ring.radius}"/><text class="degree-guide-label" x="480" y="${480 - ring.radius + 13}">D${ring.degree}</text>`).join('');
  const nodes = layout.nodes.map(node => {
    const selected = node.id === selectedId ? 'selected' : '';
    const icon = node.id.substring(node.id.lastIndexOf('/') + 1);
    return `<g class="material-node recipe-map-node ${selected}" data-entry="${escapeAttr(node.id)}" transform="translate(${node.x} ${node.y})">
      <circle class="node-glow" r="22"></circle><rect class="node-frame" x="-15" y="-15" width="30" height="30"></rect>
      <rect class="node-core" x="-11" y="-11" width="22" height="22"></rect>
      <image href="/asset/item/${encodeURIComponent(icon)}.png" x="-9" y="-9" width="18" height="18"></image>
      <text class="recipe-map-node-label" x="0" y="30">${escapeHtml(node.displayName)}</text>
    </g>`;
  }).join('');
  return `<div class="graph-shell"><div class="graph-scroll"><svg class="graph" width="${layout.width}" height="${layout.height}" viewBox="0 0 ${layout.width} ${layout.height}">
    <rect width="100%" height="100%" fill="#090102"></rect><g>${rings}</g><g>${links}</g><g>${nodes}</g>
  </svg></div></div>`;
}

function renderInspector(): string {
  const entry = selectedEntry();
  if (!entry) return `<div class="editor-form"><h2>No node selected</h2>${renderFamilyManager()}</div>`;
  const outgoing = currentMap().links.filter(link => link.from === entry.id);
  const incoming = currentMap().links.filter(link => link.to === entry.id);
  return `<div class="editor-form">
    <div class="form-heading"><div><h2>${escapeHtml(entry.displayName)}</h2><p>${escapeHtml(entry.id)}</p></div><span class="icon-chip">Degree ${entry.column}</span></div>
    <label>Family<select data-entry-edit="family">${currentMap().families.map(family => `<option ${family === entry.family ? 'selected' : ''}>${escapeHtml(family)}</option>`).join('')}</select></label>
    <label>Order within family<input type="number" min="0" step="1" data-entry-edit="order" value="${entry.order}"/></label>
    <div class="parent-editor"><span class="field-label">Outgoing links</span><div class="parent-list">${outgoing.length ? outgoing.map(linkPill).join('') : '<span class="parent-empty">No outgoing links</span>'}</div></div>
    <div class="parent-editor"><span class="field-label">Incoming links</span><div class="parent-list">${incoming.length ? incoming.map(linkPill).join('') : '<span class="parent-empty">No incoming links</span>'}</div></div>
    <div class="grid2">
      <label>Link target<select data-new-link="target">${currentMap().entries.filter(candidate => candidate.id !== entry.id).map(candidate => `<option value="${escapeAttr(candidate.id)}">${escapeHtml(candidate.displayName)}</option>`).join('')}</select></label>
      <label>Link kind<select data-new-link="kind"><option>CONCEPTUAL</option><option>PROGRESSION</option></select></label>
    </div>
    <button data-action="add-link">Add outgoing link</button>
    ${renderFamilyManager()}
  </div>`;
}

function renderFamilyManager(): string {
  return `<div class="parent-editor"><span class="field-label">Families</span><div class="parent-list">${currentMap().families.map(family => `<button class="parent-pill" data-remove-family="${escapeAttr(family)}" ${family === 'Miscellaneous' || currentMap().entries.some(entry => entry.family === family) ? 'disabled' : ''}>${escapeHtml(family)} <b>×</b></button>`).join('')}</div>
    <div class="grid2"><input data-new-family placeholder="New family name"/><button data-action="add-family">Add family</button></div></div>`;
}

function linkPill(link: RecipeMapEditorLink): string {
  const other = link.from === selectedId ? link.to : link.from;
  return `<button class="parent-pill" data-remove-link="${escapeAttr(linkKey(link))}" title="Remove ${escapeAttr(link.kind.toLowerCase())} link">${escapeHtml(displayName(other))} · ${escapeHtml(link.kind)} <b>×</b></button>`;
}

function wireEvents(): void {
  app.querySelectorAll<HTMLElement>('[data-map]').forEach(element => element.addEventListener('click', () => {
    activeMap = element.dataset.map as RecipeMapTabKey;
    selectedId = currentMap().entries[0]?.id ?? '';
    preview = null;
    render();
  }));
  app.querySelectorAll<HTMLElement>('[data-layer]').forEach(element => element.addEventListener('click', () => {
    activeLayer = element.dataset.layer as RecipeMapLayer;
    ensureSelection();
    render();
  }));
  app.querySelectorAll<HTMLElement>('[data-view]').forEach(element => element.addEventListener('click', () => {
    currentTab = element.dataset.view as ViewTab;
    render();
  }));
  app.querySelectorAll<HTMLElement>('[data-entry]').forEach(element => element.addEventListener('click', () => {
    selectedId = element.dataset.entry ?? '';
    render();
  }));
  app.querySelector<HTMLSelectElement>('[data-entry-edit="family"]')?.addEventListener('change', event => {
    const entry = selectedEntry();
    if (entry) { entry.family = (event.target as HTMLSelectElement).value; entry.order = nextOrder(entry.family, entry.id); changed('Updated node family.'); }
  });
  app.querySelector<HTMLInputElement>('[data-entry-edit="order"]')?.addEventListener('change', event => {
    const entry = selectedEntry();
    if (entry) { entry.order = Math.max(0, Math.round(Number((event.target as HTMLInputElement).value) || 0)); changed('Updated node order.'); }
  });
  app.querySelector('[data-action="add-link"]')?.addEventListener('click', addLink);
  app.querySelectorAll<HTMLElement>('[data-remove-link]').forEach(element => element.addEventListener('click', () => {
    const key = element.dataset.removeLink;
    currentMap().links = currentMap().links.filter(link => linkKey(link) !== key);
    changed('Removed link.');
  }));
  app.querySelector('[data-action="add-family"]')?.addEventListener('click', addFamily);
  app.querySelectorAll<HTMLElement>('[data-remove-family]').forEach(element => element.addEventListener('click', () => {
    const family = element.dataset.removeFamily;
    if (family) currentMap().families = currentMap().families.filter(candidate => candidate !== family);
    changed('Removed empty family.');
  }));
  app.querySelector('[data-action="reload"]')?.addEventListener('click', () => void load());
  app.querySelector('[data-action="preview"]')?.addEventListener('click', () => void previewChanges());
  app.querySelector('[data-action="apply"]')?.addEventListener('click', () => void applyChanges());
}

function addLink(): void {
  const from = selectedEntry()?.id;
  const to = app.querySelector<HTMLSelectElement>('[data-new-link="target"]')?.value;
  const kind = app.querySelector<HTMLSelectElement>('[data-new-link="kind"]')?.value as RecipeMapEditorLink['kind'];
  if (!from || !to) return;
  currentMap().links.push({ from, to, kind });
  changed(`Added ${kind.toLowerCase()} link.`);
}

function addFamily(): void {
  const input = app.querySelector<HTMLInputElement>('[data-new-family]');
  const family = input?.value.trim() ?? '';
  if (!family || currentMap().families.includes(family)) return;
  const miscIndex = currentMap().families.indexOf('Miscellaneous');
  currentMap().families.splice(miscIndex < 0 ? currentMap().families.length : miscIndex, 0, family);
  changed(`Added ${family} family.`);
}

async function previewChanges(): Promise<void> {
  isBusy = true; statusText = 'Building preview...'; render();
  try {
    preview = await api<PreviewResult>('/api/recipe-maps/preview', { method: 'POST', body: JSON.stringify({ tabs: workspace!.tabs }) });
    workspace!.diagnostics = preview.diagnostics;
    currentTab = 'diff';
    statusText = preview.canApply ? `Preview ready: ${preview.diffs.length} file changed.` : 'Preview has blocking validation issues or no changes.';
  } catch (error) { statusText = error instanceof Error ? error.message : String(error); }
  finally { isBusy = false; render(); }
}

async function applyChanges(): Promise<void> {
  if (!preview?.canApply) return;
  isBusy = true; statusText = 'Applying preview...'; render();
  try {
    await api('/api/apply', { method: 'POST', body: JSON.stringify({ id: preview.id }) });
    await load();
    statusText = 'Applied Crafting and Rites map changes.';
  } catch (error) { statusText = error instanceof Error ? error.message : String(error); isBusy = false; render(); }
}

function changed(message: string): void { preview = null; currentTab = 'graph'; statusText = message; render(); }
function currentMap(): RecipeMapEditorTab { return workspace!.tabs.find(tab => tab.key === activeMap) ?? workspace!.tabs[0]; }
function selectedEntry(): RecipeMapEditorEntry | undefined { return currentMap().entries.find(entry => entry.id === selectedId); }
function ensureSelection(): void {
  const visible = currentMap().entries.filter(entry => activeLayer === 'surface' ? entry.column <= 4 : entry.column >= 5);
  if (!visible.some(entry => entry.id === selectedId)) selectedId = visible[0]?.id ?? currentMap().entries[0]?.id ?? '';
}
function nextOrder(family: string, excluding: string): number { return currentMap().entries.filter(entry => entry.family === family && entry.id !== excluding).reduce((max, entry) => Math.max(max, entry.order + 1), 0); }
function displayName(id: string): string { return currentMap().entries.find(entry => entry.id === id)?.displayName ?? id; }
function linkKey(link: RecipeMapEditorLink): string { return `${link.from}|${link.to}|${link.kind}`; }
function mapButton(tab: RecipeMapEditorTab): string { return `<button class="branch-button ${tab.key === activeMap ? 'selected' : ''}" data-map="${tab.key}"><span>${tab.key === 'RITES' ? 'Rites' : 'Crafting'}</span><b>${tab.entries.length}</b></button>`; }
function familySection(family: string): string { const entries = currentMap().entries.filter(entry => entry.family === family).sort((a, b) => a.order - b.order); return entries.length ? `<p class="recipe-map-family-heading">${escapeHtml(family)}</p>${entries.map(entry => `<button class="skill-button ${entry.id === selectedId ? 'selected' : ''}" data-entry="${escapeAttr(entry.id)}"><span>${escapeHtml(entry.displayName)}</span><small>D${entry.column}</small></button>`).join('')}` : ''; }
function viewButton(tab: ViewTab, label: string): string { return `<button class="${currentTab === tab ? 'active' : ''}" data-view="${tab}">${label}</button>`; }
function badge(count: number): string { return count ? ` <span class="badge">${count}</span>` : ''; }
function renderDiagnostics(diagnostics: Diagnostic[]): string { return `<div class="diagnostics">${diagnostics.length ? diagnostics.map(diagnostic => `<article class="diagnostic ${diagnostic.severity}"><b>${escapeHtml(diagnostic.severity.toUpperCase())}</b><span>${escapeHtml(diagnostic.code)}</span><p>${escapeHtml(diagnostic.message)}</p></article>`).join('') : '<div class="empty">No diagnostics.</div>'}</div>`; }
function renderDiffs(): string { return `<div class="diffs">${preview?.diffs.length ? preview.diffs.map(diff => `<article class="diff"><h2>${escapeHtml(diff.path)}</h2><pre>${escapeHtml(diff.patch)}</pre></article>`).join('') : '<div class="empty">Preview changes to inspect a diff.</div>'}</div>`; }
function relativeRoot(): string { return workspace?.repoRoot.split(/[\\/]/).slice(-3).join('/') ?? ''; }
async function api<T = unknown>(path: string, init?: RequestInit): Promise<T> { const response = await fetch(path, { ...init, headers: { 'content-type': 'application/json', ...(init?.headers ?? {}) } }); const json = await response.json(); if (!response.ok) throw new Error(json.error ?? response.statusText); return json as T; }
function escapeHtml(value: string): string { return value.replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character] ?? character)); }
function escapeAttr(value: string): string { return escapeHtml(value); }
