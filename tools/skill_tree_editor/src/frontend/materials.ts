import type {
  Diagnostic,
  MaterialAtlasBucketModel,
  MaterialAtlasEntryModel,
  MaterialAtlasPathKey,
  MaterialAtlasPathModel,
  MaterialAtlasWorkspace,
  MaterialCatalogEntryModel,
  MaterialGateModel,
  MaterialGateType,
  PreviewResult
} from '../shared/types';
import {
  layoutMaterialAtlasPath,
  alignMaterialPositionToNodes,
  materialModelPositionFromRendered,
  materialPositionFromDrag,
  materialSelectionIdsInRect,
  sanitizeMaterialParents,
  type MaterialGraphLayout
} from './materialsGraph';
import { beginConnectionDrag, finishConnectionDrag, type ConnectionDragState } from './connectionEditing';
import { beginDragPan, type DragPanState, shouldStartDragPan, updateDragPan } from './dragPan';
import { captureGraphViewport, restoreGraphViewport, type GraphViewport } from './viewportPreservation';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';
type SelectionKind = 'material' | 'category-anchor' | 'label-plaque' | 'atlas-hub' | 'create-entry';

interface Selection {
  kind: SelectionKind;
  id: string;
}

interface PositionChange {
  kind: Exclude<SelectionKind, 'create-entry'>;
  id: string;
  before: { x: number; y: number };
  after: { x: number; y: number };
}

interface BucketChange {
  kind: 'bucket-change';
  id: string;
  beforeBucketId: string;
  afterBucketId: string;
}

interface BucketCreateChange {
  kind: 'bucket-create-change';
  bucket: MaterialAtlasBucketModel;
}

interface ParentChange {
  kind: 'parent-change';
  id: string;
  beforeParentIds: string[];
  afterParentIds: string[];
}

interface AutoPositionChange {
  kind: 'auto-position-change';
  id: string;
  beforeNodeX: number | null;
  beforeNodeY: number | null;
  afterNodeX: number | null;
  afterNodeY: number | null;
}

interface MaterialGroupPositionChange {
  kind: 'material-group-position-change';
  ids: string[];
  before: Record<string, { x: number; y: number }>;
  after: Record<string, { x: number; y: number }>;
}

interface MaterialRemoveChange {
  kind: 'material-remove-change';
  removedIds: string[];
  beforeEntries: MaterialAtlasEntryModel[];
  afterEntries: MaterialAtlasEntryModel[];
}

type HistoryChange = PositionChange | BucketChange | BucketCreateChange | ParentChange | AutoPositionChange | MaterialGroupPositionChange | MaterialRemoveChange;

interface DragState {
  kind: Exclude<SelectionKind, 'create-entry'>;
  id: string;
  pointerId: number;
  pointerStart: { x: number; y: number };
  scrollStart: { x: number; y: number };
  origin: { x: number; y: number };
  groupOrigins?: Record<string, { x: number; y: number }>;
}

interface MarqueeState {
  pointerId: number;
  start: { x: number; y: number };
  current: { x: number; y: number };
}

const graphMinZoom = 0.55;
const graphMaxZoom = 2.75;
const wheelZoomStep = 1.12;
const buttonZoomStep = 1.2;

let workspace: MaterialAtlasWorkspace | null = null;
let activePath: MaterialAtlasPathKey = 'HARBINGER';
let currentTab: ViewTab = 'graph';
let selection: Selection = { kind: 'material', id: '' };
let preview: PreviewResult | null = null;
let statusText = 'Loading material atlas...';
let isBusy = false;
let snapToGrid = true;
let alignToNodes = true;
let alignToleranceX = 10;
let alignToleranceY = 10;
let graphZoom = 1;
let dragState: DragState | null = null;
let dragPan: DragPanState | null = null;
let marqueeState: MarqueeState | null = null;
let connectionState: ConnectionDragState | null = null;
let connectionSource: { x: number; y: number } | null = null;
let connectionPointerId: number | null = null;
let selectedMaterialIds = new Set<string>();
let undoStack: HistoryChange[] = [];
let redoStack: HistoryChange[] = [];
let lastLayout: MaterialGraphLayout | null = null;

const appRoot = document.querySelector<HTMLDivElement>('#app');
if (!appRoot) throw new Error('Missing app root.');
const app = appRoot;

window.addEventListener('keydown', event => {
  if (shouldIgnoreShortcut(event.target)) return;
  if ((event.ctrlKey || event.metaKey) && !event.shiftKey && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    undoMovement();
  } else if ((event.ctrlKey || event.metaKey) && (event.key.toLowerCase() === 'y' || event.shiftKey && event.key.toLowerCase() === 'z')) {
    event.preventDefault();
    redoMovement();
  }
});

void load();

async function load(): Promise<void> {
  isBusy = true;
  render();
  try {
    workspace = await api<MaterialAtlasWorkspace>('/api/materials');
    activePath = workspace.paths[0]?.path ?? 'HARBINGER';
    selection = { kind: 'material', id: currentPath()?.entries[0]?.id ?? '' };
    preview = null;
    undoStack = [];
    redoStack = [];
    statusText = `Loaded ${workspace.paths.flatMap(path => path.entries).length} material atlas nodes.`;
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
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

  app.innerHTML = `
    <main class="shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark"></span>
          <div>
            <h1>Material Atlas Editor</h1>
            <p>${escapeHtml(relativeRoot())}</p>
            <p><a href="/workspace.html">Skills</a> - <a href="/manipulations.html">Manipulations</a> - <a href="/scars.html">Scars</a> - <b>Materials</b> - <a href="/recipe_maps.html">Craft/Rites</a></p>
          </div>
        </div>
        <div class="toolbar">
          <button data-action="reload" ${isBusy ? 'disabled' : ''}>Reload</button>
          <button data-action="preview" ${isBusy ? 'disabled' : ''}>Preview</button>
          <button data-action="apply" ${!preview?.canApply || isBusy ? 'disabled' : ''}>Apply</button>
        </div>
        <div class="branch-list">
          ${workspace.paths.map(path => pathButton(path)).join('')}
          <button class="branch-button ${selection.kind === 'create-entry' ? 'selected' : ''}" data-select-kind="create-entry" data-select-id="new">
            <span>Create Entry</span><b>+</b>
          </button>
          <button class="branch-button" data-action="create-category">
            <span>Create Category</span><b>+</b>
          </button>
        </div>
        <div class="skill-list">
          ${currentPath().buckets.map(bucketButton).join('')}
          ${currentPath().entries.map(materialButton).join('')}
        </div>
      </aside>

      <section class="main">
        <header class="topbar">
          <nav class="tabs">
            ${tabButton('graph', 'Atlas')}
            ${tabButton('validation', `Validation${countBadge(workspace.diagnostics.length)}`)}
            ${tabButton('diff', `Diff${countBadge(preview?.diffs.length ?? 0)}`)}
          </nav>
          <div class="history-controls" aria-label="Movement history controls">
            <button data-action="undo-move" title="Undo move (Ctrl+Z)" ${!undoStack.length || isBusy ? 'disabled' : ''}>Undo</button>
            <button data-action="redo-move" title="Redo move (Ctrl+Y or Ctrl+Shift+Z)" ${!redoStack.length || isBusy ? 'disabled' : ''}>Redo</button>
          </div>
          <div class="snap-controls">
            <label class="snap-toggle"><input type="checkbox" data-action="snap-grid" ${snapToGrid ? 'checked' : ''}/> Snap</label>
            <label class="snap-toggle"><input type="checkbox" data-action="align-nodes" ${alignToNodes ? 'checked' : ''}/> Align</label>
            <label class="snap-distance">X <input type="number" min="0" max="64" step="1" data-action="align-tolerance-x" value="${alignToleranceX}" title="Horizontal alignment tolerance" /></label>
            <label class="snap-distance">Y <input type="number" min="0" max="64" step="1" data-action="align-tolerance-y" value="${alignToleranceY}" title="Vertical alignment tolerance" /></label>
          </div>
          <div class="status">${escapeHtml(statusText)}</div>
        </header>
        <section class="content">
          <section class="canvas-panel">${renderTab()}</section>
          <aside class="inspector">${renderInspector()}</aside>
        </section>
      </section>
    </main>
  `;

  wireEvents();
}

function renderPreservingGraphViewport(viewport: GraphViewport | null = captureCurrentGraphViewport()): void {
  render();
  if (!viewport) return;
  restoreCurrentGraphViewport(viewport);
  requestAnimationFrame(() => restoreCurrentGraphViewport(viewport));
}

function captureCurrentGraphViewport(): GraphViewport | null {
  const scroller = app.querySelector<HTMLElement>('.graph-scroll');
  return scroller ? captureGraphViewport(scroller) : null;
}

function restoreCurrentGraphViewport(viewport: GraphViewport): void {
  const scroller = app.querySelector<HTMLElement>('.graph-scroll');
  if (scroller) restoreGraphViewport(scroller, viewport);
}

function renderTab(): string {
  if (currentTab === 'validation') return renderDiagnostics(workspace?.diagnostics ?? []);
  if (currentTab === 'diff') return renderDiffs();
  return renderGraph();
}

function renderGraph(): string {
  const layout = layoutMaterialAtlasPath(currentPath());
  lastLayout = layout;
  const scaledWidth = Math.round(layout.width * graphZoom);
  const scaledHeight = Math.round(layout.height * graphZoom);
  const zoomLabel = `${Math.round(graphZoom * 100)}%`;

  return `<div class="graph-shell material-atlas">
    <div class="graph-zoom-controls" aria-label="Graph zoom controls">
      <button data-action="zoom-out" title="Zoom out">-</button>
      <button data-action="zoom-reset" title="Reset zoom">${zoomLabel}</button>
      <button data-action="zoom-in" title="Zoom in">+</button>
    </div>
    <div class="graph-scroll">
      <svg class="graph material-graph" width="${scaledWidth}" height="${scaledHeight}" viewBox="0 0 ${layout.width} ${layout.height}" role="img">
        <defs>
          <radialGradient id="bloodGlow" cx="50%" cy="48%" r="58%">
            <stop offset="0%" stop-color="#240507" stop-opacity="0.94"></stop>
            <stop offset="100%" stop-color="#090102" stop-opacity="1"></stop>
          </radialGradient>
          <pattern id="veinPattern" width="260" height="180" patternUnits="userSpaceOnUse">
            <path d="M -20 46 C 34 22, 84 84, 128 42 S 232 18, 284 70" class="vein-line vein-line-bold"></path>
            <path d="M 28 154 C 86 110, 148 178, 230 128" class="vein-line"></path>
            <path d="M 172 -18 C 142 44, 222 76, 184 154" class="vein-line vein-line-faint"></path>
          </pattern>
        </defs>
        <rect x="0" y="0" width="${layout.width}" height="${layout.height}" fill="url(#bloodGlow)"></rect>
        <rect x="0" y="0" width="${layout.width}" height="${layout.height}" fill="url(#veinPattern)" opacity="0.5"></rect>
        <g class="edges">${layout.traces.map(renderTrace).join('')}</g>
        <g class="atlas-hub-layer">${renderAtlasHub(layout.hub)}</g>
        <g class="label-plaques">${layout.labelPlaques.map(renderLabelPlaque).join('')}</g>
        <g class="nodes">${layout.nodes.map(renderMaterialNode).join('')}</g>
      </svg>
    </div>
  </div>`;
}

function renderTrace(trace: MaterialGraphLayout['traces'][number]): string {
  return `<path class="edge wire-edge atlas-trace" data-trace-from="${escapeAttr(trace.fromId)}" data-trace-to="${escapeAttr(trace.toId)}" style="stroke:${escapeAttr(colorToCss(trace.color))}" d="${escapeAttr(trace.path)}"></path>`;
}

function renderAtlasHub(hub: MaterialGraphLayout['hub']): string {
  const selected = selection.kind === 'atlas-hub' ? 'selected' : '';
  return `<g class="atlas-hub ${selected}" data-select-kind="atlas-hub" data-select-id="${escapeAttr(activePath)}" transform="translate(${hub.x} ${hub.y})">
    <rect x="-22" y="-22" width="44" height="44" class="atlas-hub-box" style="stroke:${escapeAttr(colorToCss(hub.color))}"></rect>
    <text x="0" y="3" text-anchor="middle" style="fill:${escapeAttr(colorToCss(hub.color))}">${escapeHtml(hub.label)}</text>
  </g>`;
}

function renderLabelPlaque(plaque: MaterialGraphLayout['labelPlaques'][number]): string {
  const selected = selection.kind === 'label-plaque' && selection.id === plaque.id ? 'selected' : '';
  const width = Math.max(118, plaque.label.length * 8 + 22);
  return `<g class="label-plaque ${selected}" data-select-kind="label-plaque" data-select-id="${escapeAttr(plaque.id)}" transform="translate(${plaque.x} ${plaque.y})">
    <rect x="${-width / 2}" y="-14" width="${width}" height="28" class="label-plaque-box" style="stroke:${escapeAttr(colorToCss(plaque.color))}"></rect>
    <text x="0" y="4" text-anchor="middle" style="fill:${escapeAttr(colorToCss(plaque.color))}">${escapeHtml(plaque.label)}</text>
  </g>`;
}

function renderMaterialNode(node: MaterialGraphLayout['nodes'][number]): string {
  const selected = selection.kind === 'material' && selection.id === node.id || selectedMaterialIds.has(node.id) ? 'selected' : '';
  const label = node.entry.catalog?.displayName ?? 'Unknown material';
  return `<g class="material-node ${selected}" data-select-kind="material" data-select-id="${escapeAttr(node.id)}" transform="translate(${node.x} ${node.y})">
    <rect class="node-hit" x="-20" y="-20" width="40" height="40"></rect>
    <rect class="node-glow" x="-15" y="-15" width="30" height="30" style="stroke:${escapeAttr(colorToCss(node.color))}"></rect>
    <rect class="node-frame" x="-13" y="-13" width="26" height="26" style="stroke:${escapeAttr(colorToCss(node.color))}"></rect>
    <rect class="node-core" x="-9" y="-9" width="18" height="18"></rect>
    <image href="${escapeAttr(node.iconUrl)}" x="-8" y="-8" width="16" height="16" preserveAspectRatio="xMidYMid meet"></image>
    <title>${escapeHtml(label)}</title>
  </g>`;
}

function renderInspector(): string {
  if (selection.kind === 'create-entry') return renderCreateEntryForm();
  if (selection.kind === 'atlas-hub') return renderHubInspector();
  if (selection.kind === 'category-anchor' || selection.kind === 'label-plaque') {
    const bucket = selectedBucket();
    return bucket ? renderBucketInspector(bucket) : '<div class="empty">No bucket selected.</div>';
  }
  const entry = selectedEntry();
  return entry ? renderMaterialInspector(entry) : '<div class="empty">No material selected.</div>';
}

function renderHubInspector(): string {
  const path = currentPath();
  const label = path.path === 'UNSTAINED' ? 'Still Atlas' : 'Blood Atlas';
  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(label)}</h2>
        <p>${escapeHtml(path.path)} atlas hub</p>
      </div>
      <div class="icon-chip">hub</div>
    </div>
    <div class="grid2">
      <label>Label X<input type="number" data-path-edit="hubLabelX" value="${path.hubLabelX}" /></label>
      <label>Label Y<input type="number" data-path-edit="hubLabelY" value="${path.hubLabelY}" /></label>
    </div>
  </form>`;
}

function renderBucketInspector(bucket: MaterialAtlasBucketModel): string {
  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(bucket.label)}</h2>
        <p>${selection.kind === 'label-plaque' ? 'Category label plaque' : 'Category anchor'} - ${escapeHtml(bucket.id)}</p>
      </div>
      <div class="icon-chip">${escapeHtml(colorToCss(bucket.color))}</div>
    </div>
    <label>Label<input data-bucket-edit="label" value="${escapeAttr(bucket.label)}" /></label>
    <label>Color<input type="color" data-bucket-edit="color" value="${escapeAttr(colorToCss(bucket.color))}" /></label>
    <div class="grid2">
      <label>Root X<input type="number" data-bucket-edit="centerX" value="${bucket.centerX}" /></label>
      <label>Root Y<input type="number" data-bucket-edit="centerY" value="${bucket.centerY}" /></label>
      <label>Label X<input type="number" data-bucket-edit="plaqueX" value="${bucket.plaqueX}" /></label>
      <label>Label Y<input type="number" data-bucket-edit="plaqueY" value="${bucket.plaqueY}" /></label>
    </div>
  </form>`;
}

function renderMaterialInspector(entry: MaterialAtlasEntryModel): string {
  const catalog = ensureCatalog(entry);
  const selectedCount = selectedMaterialIds.has(entry.id) ? selectedMaterialIds.size : 1;
  const removeLabel = selectedCount > 1 ? `Remove ${selectedCount} Atlas Nodes` : 'Remove Atlas Node';
  const parentOptions = parentOptionsFor(entry);
  const parentLabels = new Map(currentPath().entries.map(candidate => [
    candidate.id,
    candidate.catalog?.displayName ?? labelize(candidate.id)
  ] as const));
  const parentList = entry.parentIds.length
    ? entry.parentIds.map(parentId => `<button type="button" class="parent-pill" data-action="remove-parent" data-parent-id="${escapeAttr(parentId)}">
        <span>${escapeHtml(parentLabels.get(parentId) ?? parentId)}</span><b>x</b>
      </button>`).join('')
    : '<span class="parent-empty">None</span>';
  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(catalog.displayName || entry.id)}</h2>
        <p>${escapeHtml(entry.bucketId)} - ${escapeHtml(entry.id)}</p>
      </div>
      <div class="icon-chip">${escapeHtml(catalog.iconSource)}:${escapeHtml(catalog.iconField)}</div>
    </div>
    <label>Atlas id<input data-entry-edit="id" value="${escapeAttr(entry.id)}" readonly /></label>
    <label>Display name<input data-catalog-edit="displayName" value="${escapeAttr(catalog.displayName)}" /></label>
    <label>Description<textarea data-catalog-edit="description" rows="5">${escapeHtml(catalog.description)}</textarea></label>
    <label>Category<input data-catalog-edit="category" value="${escapeAttr(catalog.category)}" /></label>
    <div class="icon-editor">
      <label>Icon source<select data-catalog-edit="iconSource">${iconSourceOptions(catalog.iconSource)}</select></label>
      <label>Icon field<select data-catalog-edit="iconField">${iconFieldOptions(catalog.iconSource, catalog.iconField)}</select></label>
    </div>
    <label><span><input type="checkbox" data-catalog-edit="hasRecipe" ${catalog.hasRecipe ? 'checked' : ''}/> Has recipe</span></label>
    <label>Bucket<select data-entry-edit="bucketId">${bucketOptions(entry.bucketId)}</select></label>
    <div class="grid2">
      <label>Gate<select data-entry-edit="gateType">${gateOptions(entry.gate.type)}</select></label>
      <label>Value<input type="number" data-entry-edit="gateValue" value="${entry.gate.value ?? ''}" /></label>
    </div>
    <div class="grid2">
      <label>Node X<input type="number" data-entry-edit="nodeX" value="${entry.nodeX ?? ''}" placeholder="auto" /></label>
      <label>Node Y<input type="number" data-entry-edit="nodeY" value="${entry.nodeY ?? ''}" placeholder="auto" /></label>
    </div>
    <div class="parent-editor">
      <span class="field-label">Parents</span>
      <div class="parent-list">${parentList}</div>
      <label>Add parent<select data-action="add-parent">${'<option value="">Add parent...</option>' + parentOptions}</select></label>
    </div>
    <div class="form-actions">
      <button type="button" data-action="clear-node-position">Use Auto Position</button>
      <button type="button" class="danger-button" data-action="remove-material-node">${escapeHtml(removeLabel)}</button>
    </div>
  </form>`;
}

function renderCreateEntryForm(): string {
  const path = currentPath();
  const firstBucket = path.buckets[0]?.id ?? '';
  const firstItem = workspace?.iconOptions.items[0] ?? '';
  return `<form class="editor-form create-entry">
    <div class="form-heading">
      <div>
        <h2>Create Catalogue Entry</h2>
        <p>${escapeHtml(activePath)} atlas node for an existing registry field.</p>
      </div>
      <div class="icon-chip">V1 catalogue</div>
    </div>
    <label>Id<input data-create="id" value="${escapeAttr(firstItem)}" /></label>
    <label>Display name<input data-create="displayName" value="${escapeAttr(labelize(firstItem))}" /></label>
    <label>Description<textarea data-create="description" rows="4"></textarea></label>
    <label>Category<input data-create="category" value="Materials" /></label>
    <div class="icon-editor">
      <label>Icon source<select data-create="iconSource">${iconSourceOptions('item')}</select></label>
      <label>Icon field<select data-create="iconField">${iconFieldOptions('item', firstItem)}</select></label>
    </div>
    <label><span><input type="checkbox" data-create="hasRecipe" checked/> Has recipe</span></label>
    <label>Bucket<select data-create="bucketId">${bucketOptions(firstBucket)}</select></label>
    <div class="grid2">
      <label>Gate<select data-create="gateType">${gateOptions(activePath === 'HARBINGER' ? 'DEGREE' : 'PURITY')}</select></label>
      <label>Value<input type="number" data-create="gateValue" value="${activePath === 'HARBINGER' ? 1 : 10}" /></label>
    </div>
    <div class="form-actions">
      <button type="button" data-action="create-material">Add Material</button>
    </div>
  </form>`;
}

function renderDiagnostics(diagnostics: Diagnostic[]): string {
  if (!diagnostics.length) return '<div class="empty">No validation issues.</div>';
  return `<div class="diagnostics">${diagnostics.map(diagnostic => `
    <article class="diagnostic ${escapeAttr(diagnostic.severity)}">
      <b>${escapeHtml(diagnostic.severity.toUpperCase())}</b>
      <span>${escapeHtml(diagnostic.code)}</span>
      <p>${escapeHtml(diagnostic.message)}</p>
      ${diagnostic.file ? `<small>${escapeHtml(diagnostic.file)}</small>` : ''}
    </article>
  `).join('')}</div>`;
}

function renderDiffs(): string {
  if (!preview) return '<div class="empty">Click Preview to generate Java diffs.</div>';
  if (!preview.diffs.length) return '<div class="empty">Preview has no file changes.</div>';
  return `<div class="diffs">${preview.diffs.map(diff => `
    <article class="diff">
      <h2>${escapeHtml(diff.path)}</h2>
      <pre>${escapeHtml(diff.patch)}</pre>
    </article>
  `).join('')}</div>`;
}

function wireEvents(): void {
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-action]')) {
    button.addEventListener('click', () => handleAction(button.dataset.action ?? ''));
  }
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-path]')) {
    button.addEventListener('click', () => {
      activePath = button.dataset.path as MaterialAtlasPathKey;
      selection = { kind: 'material', id: currentPath().entries[0]?.id ?? '' };
      selectedMaterialIds = new Set();
      preview = null;
      render();
    });
  }
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-tab]')) {
    button.addEventListener('click', () => {
      currentTab = button.dataset.tab as ViewTab;
      render();
    });
  }
  for (const element of app.querySelectorAll<HTMLElement>('[data-select-kind]')) {
    element.addEventListener('click', () => {
      selection = {
        kind: element.dataset.selectKind as SelectionKind,
        id: element.dataset.selectId ?? ''
      };
      if (selection.kind !== 'material' || !selectedMaterialIds.has(selection.id)) {
        selectedMaterialIds = new Set();
      }
      renderPreservingGraphViewport(captureCurrentGraphViewport());
    });
  }
  app.querySelector<HTMLInputElement>('input[data-action="snap-grid"]')?.addEventListener('change', event => {
    snapToGrid = (event.currentTarget as HTMLInputElement).checked;
  });
  app.querySelector<HTMLInputElement>('input[data-action="align-nodes"]')?.addEventListener('change', event => {
    alignToNodes = (event.currentTarget as HTMLInputElement).checked;
  });
  app.querySelector<HTMLInputElement>('input[data-action="align-tolerance-x"]')?.addEventListener('change', event => {
    alignToleranceX = clampAlignmentTolerance((event.currentTarget as HTMLInputElement).value);
    renderPreservingGraphViewport(captureCurrentGraphViewport());
  });
  app.querySelector<HTMLInputElement>('input[data-action="align-tolerance-y"]')?.addEventListener('change', event => {
    alignToleranceY = clampAlignmentTolerance((event.currentTarget as HTMLInputElement).value);
    renderPreservingGraphViewport(captureCurrentGraphViewport());
  });
  for (const input of app.querySelectorAll<HTMLInputElement | HTMLSelectElement>('[data-bucket-edit]')) {
    input.addEventListener('change', () => updateBucket(input));
  }
  for (const input of app.querySelectorAll<HTMLInputElement>('[data-path-edit]')) {
    input.addEventListener('change', () => updatePath(input));
  }
  for (const input of app.querySelectorAll<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>('[data-entry-edit]')) {
    input.addEventListener('change', () => updateEntry(input));
  }
  for (const input of app.querySelectorAll<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>('[data-catalog-edit]')) {
    input.addEventListener('change', () => updateCatalog(input));
  }
  app.querySelector<HTMLSelectElement>('select[data-action="add-parent"]')?.addEventListener('change', event => {
    const value = (event.currentTarget as HTMLSelectElement).value;
    if (value) addParent(value);
  });
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-action="remove-parent"]')) {
    button.addEventListener('click', () => {
      const parentId = button.dataset.parentId;
      if (parentId) removeParent(parentId);
    });
  }

  wireGraphEvents();
}

function wireGraphEvents(): void {
  const scroller = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroller) return;
  scroller.addEventListener('wheel', event => {
    if (event.deltaY === 0) return;
    const rect = scroller.getBoundingClientRect();
    zoomGraphAt(scroller, event.clientX - rect.left, event.clientY - rect.top, event.deltaY < 0 ? wheelZoomStep : 1 / wheelZoomStep);
    event.preventDefault();
  }, { passive: false });
  scroller.addEventListener('pointerdown', event => {
    if (event.button !== 0) return;
    const target = event.target as Element | null;
    const draggable = target?.closest<SVGGElement>('.material-node, .label-plaque, .atlas-hub');
    if (!draggable) {
      if (event.shiftKey && target?.closest('.material-graph')) {
        const start = graphPointFromPointer(scroller, event);
        marqueeState = { pointerId: event.pointerId, start, current: start };
        updateMarqueeSelectionRect(scroller, marqueeState);
        scroller.setPointerCapture(event.pointerId);
        scroller.classList.add('marquee-selecting');
        event.preventDefault();
        return;
      }
      if (shouldStartDragPan(event.target)) {
        dragPan = beginDragPan(event.clientX, event.clientY, scroller.scrollLeft, scroller.scrollTop);
        scroller.setPointerCapture(event.pointerId);
        scroller.classList.add('panning');
        event.preventDefault();
      }
      return;
    }
    const kind = draggable.dataset.selectKind as Exclude<SelectionKind, 'create-entry'>;
    const id = draggable.dataset.selectId ?? '';
    const origin = positionFor(kind, id);
    if (!origin) return;
    selection = { kind, id };
    if (kind !== 'material' || !selectedMaterialIds.has(id)) {
      selectedMaterialIds = new Set();
    }
    if (kind === 'material' && (event.ctrlKey || event.metaKey)) {
      connectionState = beginConnectionDrag(id);
      connectionSource = origin;
      connectionPointerId = event.pointerId;
      updateConnectionPreview(scroller, connectionSource, graphPointFromPointer(scroller, event));
      scroller.classList.add('connecting-parent');
      scroller.setPointerCapture(event.pointerId);
      event.preventDefault();
      return;
    }
    const groupOrigins = kind === 'material' && selectedMaterialIds.has(id) && selectedMaterialIds.size > 1
      ? materialPositionsForIds(selectedMaterialIds)
      : undefined;
    dragState = {
      kind,
      id,
      pointerId: event.pointerId,
      pointerStart: { x: event.clientX, y: event.clientY },
      scrollStart: { x: scroller.scrollLeft, y: scroller.scrollTop },
      origin,
      groupOrigins
    };
    scroller.setPointerCapture(event.pointerId);
    event.preventDefault();
  });
  scroller.addEventListener('pointermove', event => {
    if (dragPan) {
      const update = updateDragPan(dragPan, event.clientX, event.clientY);
      scroller.scrollLeft = update.scrollLeft;
      scroller.scrollTop = update.scrollTop;
      return;
    }
    if (connectionState && connectionPointerId === event.pointerId && connectionSource) {
      updateConnectionPreview(scroller, connectionSource, graphPointFromPointer(scroller, event));
      event.preventDefault();
      return;
    }
    if (marqueeState && event.pointerId === marqueeState.pointerId) {
      marqueeState.current = graphPointFromPointer(scroller, event);
      updateMarqueeSelectionRect(scroller, marqueeState);
      event.preventDefault();
      return;
    }
    if (!dragState || event.pointerId !== dragState.pointerId) return;
    const dragged = materialPositionFromDrag({
      origin: dragState.origin,
      pointerStart: dragState.pointerStart,
      pointerCurrent: { x: event.clientX, y: event.clientY },
      scrollStart: dragState.scrollStart,
      scrollCurrent: { x: scroller.scrollLeft, y: scroller.scrollTop },
      zoom: graphZoom,
      snap: snapToGrid ? 8 : 1
    });
    const layout = lastLayout ?? layoutMaterialAtlasPath(currentPath());
    const groupIds = dragState.groupOrigins ? new Set(Object.keys(dragState.groupOrigins)) : new Set<string>();
    const alignmentNodes = groupIds.size
      ? layout.nodes.filter(node => !groupIds.has(node.id) || node.id === dragState!.id)
      : layout.nodes;
    const next = alignToNodes && dragState.kind === 'material'
      ? alignMaterialPositionToNodes(dragged, alignmentNodes, dragState.id, { x: alignToleranceX, y: alignToleranceY })
      : dragged;
    if (dragState.groupOrigins && dragState.kind === 'material') {
      const dx = next.x - dragState.origin.x;
      const dy = next.y - dragState.origin.y;
      for (const [materialId, groupOrigin] of Object.entries(dragState.groupOrigins)) {
        const groupPosition = { x: groupOrigin.x + dx, y: groupOrigin.y + dy };
        setPosition('material', materialId, groupPosition);
        const element = app.querySelector<SVGGElement>(draggedGraphElementSelector('material', materialId));
        element?.setAttribute('transform', `translate(${groupPosition.x} ${groupPosition.y})`);
      }
    } else {
      setPosition(dragState.kind, dragState.id, next);
      const element = app.querySelector<SVGGElement>(draggedGraphElementSelector(dragState.kind, dragState.id));
      element?.setAttribute('transform', `translate(${next.x} ${next.y})`);
    }
    updateMaterialGraphDomTraces();
    updatePositionInputs(dragState.kind, dragState.id);
    preview = null;
  });
  const finish = (event: PointerEvent): void => {
    const viewport = captureGraphViewport(scroller);
    if (dragPan) {
      dragPan = null;
      scroller.classList.remove('panning');
      return;
    }
    if (marqueeState && event.pointerId === marqueeState.pointerId) {
      marqueeState.current = graphPointFromPointer(scroller, event);
      const layout = lastLayout ?? layoutMaterialAtlasPath(currentPath());
      const ids = materialSelectionIdsInRect(layout.nodes, marqueeState.start, marqueeState.current);
      selectedMaterialIds = new Set(ids);
      if (ids.length) selection = { kind: 'material', id: ids[0] };
      statusText = ids.length === 1 ? 'Selected 1 material.' : `Selected ${ids.length} materials.`;
      marqueeState = null;
      removeMarqueeSelectionRect(scroller);
      scroller.classList.remove('marquee-selecting');
      event.preventDefault();
      renderPreservingGraphViewport(viewport);
      return;
    }
    if (connectionState && connectionPointerId === event.pointerId) {
      const rewire = finishConnectionDrag(connectionState, connectionTargetIdAt(event));
      if (rewire) {
        const target = currentPath().entries.find(entry => entry.id === rewire.field);
        if (target) {
          selection = { kind: 'material', id: target.id };
          addParentToEntry(target, rewire.parentField, false);
        }
      }
      connectionState = null;
      connectionSource = null;
      connectionPointerId = null;
      removeConnectionPreview(scroller);
      scroller.classList.remove('connecting-parent');
      event.preventDefault();
      renderPreservingGraphViewport(viewport);
      return;
    }
    if (!dragState || event.pointerId !== dragState.pointerId) return;
    if (dragState.groupOrigins && dragState.kind === 'material') {
      const after = materialPositionsForIds(new Set(Object.keys(dragState.groupOrigins)));
      if (!samePositionRecords(dragState.groupOrigins, after)) {
        undoStack.push({
          kind: 'material-group-position-change',
          ids: Object.keys(dragState.groupOrigins),
          before: dragState.groupOrigins,
          after
        });
        redoStack = [];
        statusText = `Moved ${Object.keys(dragState.groupOrigins).length} materials.`;
      }
    } else {
      const after = positionFor(dragState.kind, dragState.id);
      if (after && (after.x !== dragState.origin.x || after.y !== dragState.origin.y)) {
        undoStack.push({ kind: dragState.kind, id: dragState.id, before: dragState.origin, after });
        redoStack = [];
        statusText = `Moved ${dragState.id}.`;
      }
    }
    dragState = null;
    renderPreservingGraphViewport(viewport);
  };
  scroller.addEventListener('pointerup', finish);
  scroller.addEventListener('pointercancel', finish);
}

function handleAction(action: string): void {
  switch (action) {
    case 'reload':
      void load();
      return;
    case 'preview':
      void runPreview();
      return;
    case 'apply':
      void applyPreviewChanges();
      return;
    case 'zoom-in':
      zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), centerX(), centerY(), buttonZoomStep);
      return;
    case 'zoom-out':
      zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), centerX(), centerY(), 1 / buttonZoomStep);
      return;
    case 'zoom-reset':
      zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), centerX(), centerY(), 1 / graphZoom);
      return;
    case 'undo-move':
      undoMovement();
      return;
    case 'redo-move':
      redoMovement();
      return;
    case 'clear-node-position':
      clearNodePosition();
      return;
    case 'remove-material-node':
      removeSelectedMaterialNodes();
      return;
    case 'create-material':
      createMaterial();
      return;
    case 'create-category':
      createMaterialCategory();
      return;
  }
}

function updateBucket(input: HTMLInputElement | HTMLSelectElement): void {
  const bucket = selectedBucket();
  if (!bucket) return;
  const key = input.dataset.bucketEdit as keyof MaterialAtlasBucketModel;
  if (key === 'color') bucket.color = cssToAtlasColor(input.value);
  else if (key === 'label') bucket.label = input.value;
  else if (key === 'centerX' || key === 'centerY' || key === 'plaqueX' || key === 'plaqueY') bucket[key] = Number(input.value);
  preview = null;
  render();
}

function updatePath(input: HTMLInputElement): void {
  const path = currentPath();
  const key = input.dataset.pathEdit as 'hubLabelX' | 'hubLabelY';
  if (key !== 'hubLabelX' && key !== 'hubLabelY') return;
  path[key] = Number(input.value);
  preview = null;
  render();
}

function updateEntry(input: HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement, shouldRender = true): void {
  const entry = selectedEntry();
  if (!entry) return;
  const key = input.dataset.entryEdit;
  if (key === 'bucketId') {
    const beforeBucketId = entry.bucketId;
    const afterBucketId = input.value;
    if (beforeBucketId !== afterBucketId) {
      entry.bucketId = afterBucketId;
      undoStack.push({ kind: 'bucket-change', id: entry.id, beforeBucketId, afterBucketId });
      redoStack = [];
      statusText = `Moved ${entry.id} to ${bucketLabel(afterBucketId)}.`;
    }
  }
  else if (key === 'gateType') {
    entry.gate = defaultGate(input.value as MaterialGateType);
    ensureCatalog(entry).gate = { ...entry.gate };
  } else if (key === 'gateValue') {
    entry.gate.value = input.value.trim() ? Number(input.value) : null;
    ensureCatalog(entry).gate = { ...entry.gate };
  } else if (key === 'nodeX' || key === 'nodeY') {
    entry[key] = input.value.trim() ? Number(input.value) : null;
  }
  preview = null;
  if (shouldRender) render();
}

function updateCatalog(input: HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement): void {
  const entry = selectedEntry();
  if (!entry) return;
  const catalog = ensureCatalog(entry);
  const key = input.dataset.catalogEdit as keyof MaterialCatalogEntryModel;
  if (key === 'hasRecipe' && input instanceof HTMLInputElement) catalog.hasRecipe = input.checked;
  else if (key === 'iconSource') {
    catalog.iconSource = input.value === 'block' ? 'block' : 'item';
    const options = catalog.iconSource === 'block' ? workspace?.iconOptions.blocks : workspace?.iconOptions.items;
    if (!options?.includes(catalog.iconField)) catalog.iconField = options?.[0] ?? catalog.iconField;
  } else if (key === 'iconField') catalog.iconField = input.value;
  else if (key === 'displayName' || key === 'description' || key === 'category') catalog[key] = input.value;
  preview = null;
  render();
}

function addParent(parentId: string): void {
  const entry = selectedEntry();
  if (!entry) return;
  addParentToEntry(entry, parentId);
}

function addParentToEntry(entry: MaterialAtlasEntryModel, parentId: string, shouldRender = true): void {
  const beforeParentIds = [...entry.parentIds];
  const afterParentIds = sanitizeMaterialParents(entry.id, [...entry.parentIds, parentId], new Set(currentPath().entries.map(candidate => candidate.id)));
  if (sameList(beforeParentIds, afterParentIds)) return;
  entry.parentIds = afterParentIds;
  undoStack.push({ kind: 'parent-change', id: entry.id, beforeParentIds, afterParentIds: [...afterParentIds] });
  redoStack = [];
  statusText = `Added ${parentId} as a parent of ${entry.id}.`;
  preview = null;
  if (shouldRender) renderPreservingGraphViewport(captureCurrentGraphViewport());
}

function removeParent(parentId: string): void {
  const entry = selectedEntry();
  if (!entry) return;
  const beforeParentIds = [...entry.parentIds];
  const afterParentIds = beforeParentIds.filter(candidate => candidate !== parentId);
  if (sameList(beforeParentIds, afterParentIds)) return;
  entry.parentIds = afterParentIds;
  undoStack.push({ kind: 'parent-change', id: entry.id, beforeParentIds, afterParentIds: [...afterParentIds] });
  redoStack = [];
  statusText = `Removed ${parentId} from ${entry.id}.`;
  preview = null;
  renderPreservingGraphViewport(captureCurrentGraphViewport());
}

function parentOptionsFor(entry: MaterialAtlasEntryModel): string {
  const path = currentPath();
  const existingParents = new Set(entry.parentIds);
  const currentBucketIds = new Set(path.entries
    .filter(candidate => candidate.bucketId === entry.bucketId)
    .map(candidate => candidate.id));
  const ordered = path.entries
    .filter(candidate => candidate.id !== entry.id && !existingParents.has(candidate.id))
    .slice()
    .sort((a, b) => parentSortRank(a, currentBucketIds) - parentSortRank(b, currentBucketIds)
      || a.order - b.order
      || a.id.localeCompare(b.id));
  return ordered.map(candidate => {
    const suffix = candidate.bucketId === entry.bucketId
        ? ' - same bucket'
        : '';
    const label = `${candidate.catalog?.displayName ?? labelize(candidate.id)} (${candidate.id})${suffix}`;
    return `<option value="${escapeAttr(candidate.id)}">${escapeHtml(label)}</option>`;
  }).join('');
}

function parentSortRank(entry: MaterialAtlasEntryModel, currentBucketIds: Set<string>): number {
  if (currentBucketIds.has(entry.id)) return 0;
  return 1;
}

function createMaterial(): void {
  const form = app.querySelector<HTMLFormElement>('.create-entry');
  if (!form) return;
  const get = (name: string) => form.querySelector<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>(`[data-create="${name}"]`);
  const iconSource = get('iconSource')?.value === 'block' ? 'block' : 'item';
  const iconField = get('iconField')?.value.trim() || '';
  const id = get('id')?.value.trim() || iconField;
  if (!id) {
    statusText = 'Choose an id or registry field first.';
    render();
    return;
  }
  const path = currentPath();
  if (path.entries.some(entry => entry.id === id)) {
    statusText = `${id} already exists in ${activePath}.`;
    render();
    return;
  }
  const bucketId = get('bucketId')?.value || path.buckets[0]?.id || '';
  const bucket = path.buckets.find(candidate => candidate.id === bucketId) ?? path.buckets[0];
  const gate = defaultGate(get('gateType')?.value as MaterialGateType);
  gate.value = gate.type === 'ALWAYS' ? null : Number(get('gateValue')?.value || 0);
  const catalog: MaterialCatalogEntryModel = {
    path: activePath,
    id,
    displayName: get('displayName')?.value.trim() || labelize(id),
    description: get('description')?.value.trim() || '',
    category: get('category')?.value.trim() || 'Materials',
    iconSource,
    iconField,
    hasRecipe: form.querySelector<HTMLInputElement>('[data-create="hasRecipe"]')?.checked ?? true,
    gate: { ...gate }
  };
  const entry: MaterialAtlasEntryModel = {
    path: activePath,
    id,
    bucketId,
    gate,
    order: path.entries.length,
    parentIds: [],
    nodeX: bucket ? bucket.centerX + 36 : 520,
    nodeY: bucket ? bucket.centerY + 36 : 520,
    catalog
  };
  path.entries.push(entry);
  selection = { kind: 'material', id };
  preview = null;
  statusText = `Created material atlas entry ${id}.`;
  render();
}

function createMaterialCategory(): void {
  const path = currentPath();
  const center = currentViewportGraphCenter();
  const id = uniqueBucketId('new_category');
  const label = `New Category ${path.buckets.length + 1}`;
  const color = activePath === 'UNSTAINED' ? '0xFF9EC8D8' : '0xFFD99B2D';
  const bucket: MaterialAtlasBucketModel = {
    path: activePath,
    id,
    label,
    color,
    centerX: center.x,
    centerY: center.y,
    plaqueX: center.x,
    plaqueY: center.y - 70
  };
  path.buckets.push(bucket);
  undoStack.push({ kind: 'bucket-create-change', bucket: { ...bucket } });
  redoStack = [];
  selection = { kind: 'label-plaque', id };
  selectedMaterialIds = new Set();
  preview = null;
  statusText = `Created material category ${label}.`;
  renderPreservingGraphViewport(captureCurrentGraphViewport());
}

async function runPreview(): Promise<void> {
  if (!workspace) return;
  isBusy = true;
  statusText = 'Generating material atlas preview...';
  render();
  try {
    preview = await api<PreviewResult>('/api/materials/preview', {
      method: 'POST',
      body: JSON.stringify({
        paths: workspace.paths,
        catalogueEntries: catalogueEntries()
      })
    });
    workspace.diagnostics = preview.diagnostics;
    currentTab = 'diff';
    statusText = preview.canApply
      ? `Preview ready with ${preview.diffs.length} Java file changes.`
      : 'Preview has blocking material validation issues.';
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
  } finally {
    isBusy = false;
    render();
  }
}

async function applyPreviewChanges(): Promise<void> {
  if (!preview?.id) return;
  isBusy = true;
  statusText = 'Applying material preview...';
  render();
  try {
    await api('/api/apply', {
      method: 'POST',
      body: JSON.stringify({ id: preview.id })
    });
    statusText = 'Applied material atlas changes. Reloading...';
    await load();
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
  } finally {
    isBusy = false;
    render();
  }
}

function undoMovement(): void {
  const change = undoStack.pop();
  if (!change) return;
  if (change.kind === 'bucket-change') {
    setEntryBucket(change.id, change.beforeBucketId);
    statusText = `Undid bucket change for ${change.id}.`;
  } else if (change.kind === 'bucket-create-change') {
    removeBucket(change.bucket.id);
    selection = { kind: 'material', id: currentPath().entries[0]?.id ?? '' };
    statusText = `Removed category ${change.bucket.label}.`;
  } else if (change.kind === 'parent-change') {
    setEntryParents(change.id, change.beforeParentIds);
    statusText = `Undid parent change for ${change.id}.`;
  } else if (change.kind === 'auto-position-change') {
    setEntryNodePosition(change.id, change.beforeNodeX, change.beforeNodeY);
    statusText = `Undid auto position for ${change.id}.`;
  } else if (change.kind === 'material-group-position-change') {
    setMaterialRenderedPositions(change.before);
    selectedMaterialIds = new Set(change.ids);
    statusText = `Undid move for ${change.ids.length} materials.`;
  } else if (change.kind === 'material-remove-change') {
    setMaterialEntries(change.beforeEntries);
    selectedMaterialIds = new Set(change.removedIds);
    selection = { kind: 'material', id: change.removedIds[0] ?? currentPath().entries[0]?.id ?? '' };
    statusText = `Restored ${change.removedIds.length} material atlas node${change.removedIds.length === 1 ? '' : 's'}.`;
  } else {
    setPosition(change.kind, change.id, change.before);
    statusText = `Undid move for ${change.id}.`;
  }
  redoStack.push(change);
  preview = null;
  render();
}

function redoMovement(): void {
  const change = redoStack.pop();
  if (!change) return;
  if (change.kind === 'bucket-change') {
    setEntryBucket(change.id, change.afterBucketId);
    statusText = `Redid bucket change for ${change.id}.`;
  } else if (change.kind === 'bucket-create-change') {
    restoreBucket(change.bucket);
    selection = { kind: 'label-plaque', id: change.bucket.id };
    statusText = `Restored category ${change.bucket.label}.`;
  } else if (change.kind === 'parent-change') {
    setEntryParents(change.id, change.afterParentIds);
    statusText = `Redid parent change for ${change.id}.`;
  } else if (change.kind === 'auto-position-change') {
    setEntryNodePosition(change.id, change.afterNodeX, change.afterNodeY);
    statusText = `Redid auto position for ${change.id}.`;
  } else if (change.kind === 'material-group-position-change') {
    setMaterialRenderedPositions(change.after);
    selectedMaterialIds = new Set(change.ids);
    statusText = `Redid move for ${change.ids.length} materials.`;
  } else if (change.kind === 'material-remove-change') {
    setMaterialEntries(change.afterEntries);
    selectedMaterialIds = new Set();
    selection = { kind: 'material', id: currentPath().entries[0]?.id ?? '' };
    statusText = `Removed ${change.removedIds.length} material atlas node${change.removedIds.length === 1 ? '' : 's'}.`;
  } else {
    setPosition(change.kind, change.id, change.after);
    statusText = `Redid move for ${change.id}.`;
  }
  undoStack.push(change);
  preview = null;
  render();
}

function removeSelectedMaterialNodes(): void {
  if (selection.kind !== 'material') return;
  const path = currentPath();
  const ids = selectedMaterialIds.has(selection.id)
    ? Array.from(selectedMaterialIds)
    : [selection.id];
  const existingIds = new Set(path.entries.map(entry => entry.id));
  const removedIds = ids.filter(id => existingIds.has(id));
  if (!removedIds.length) return;

  const beforeEntries = cloneMaterialEntries(path.entries);
  const removed = new Set(removedIds);
  path.entries = stripRemovedParentIds(path.entries.filter(entry => !removed.has(entry.id)), removed);
  const afterEntries = cloneMaterialEntries(path.entries);

  undoStack.push({ kind: 'material-remove-change', removedIds, beforeEntries, afterEntries });
  redoStack = [];
  selectedMaterialIds = new Set();
  selection = { kind: 'material', id: path.entries[0]?.id ?? '' };
  statusText = `Removed ${removedIds.length} material atlas node${removedIds.length === 1 ? '' : 's'}.`;
  preview = null;
  renderPreservingGraphViewport(captureCurrentGraphViewport());
}

function clearNodePosition(): void {
  const entry = selectedEntry();
  if (!entry) return;
  if (entry.nodeX === null && entry.nodeY === null) return;
  const beforeNodeX = entry.nodeX;
  const beforeNodeY = entry.nodeY;
  entry.nodeX = null;
  entry.nodeY = null;
  undoStack.push({
    kind: 'auto-position-change',
    id: entry.id,
    beforeNodeX,
    beforeNodeY,
    afterNodeX: null,
    afterNodeY: null
  });
  redoStack = [];
  statusText = `Using auto position for ${entry.id}.`;
  preview = null;
  render();
}

function positionFor(kind: Exclude<SelectionKind, 'create-entry'>, id: string): { x: number; y: number } | null {
  if (kind === 'atlas-hub') {
    const path = currentPath();
    return { x: path.hubLabelX, y: path.hubLabelY };
  }
  if (kind === 'material') {
    const node = (lastLayout ?? layoutMaterialAtlasPath(currentPath())).nodes.find(candidate => candidate.id === id);
    return node ? { x: node.x, y: node.y } : null;
  }
  const bucket = currentPath().buckets.find(candidate => candidate.id === id);
  if (!bucket) return null;
  return kind === 'category-anchor'
    ? { x: bucket.centerX, y: bucket.centerY }
    : { x: bucket.plaqueX, y: bucket.plaqueY };
}

function setPosition(kind: Exclude<SelectionKind, 'create-entry'>, id: string, position: { x: number; y: number }): void {
  if (kind === 'atlas-hub') {
    const path = currentPath();
    path.hubLabelX = position.x;
    path.hubLabelY = position.y;
    return;
  }
  if (kind === 'material') {
    const entry = currentPath().entries.find(candidate => candidate.id === id);
    if (entry) {
      const layout = lastLayout ?? layoutMaterialAtlasPath(currentPath());
      const modelPosition = materialModelPositionFromRendered(layout, position);
      entry.nodeX = modelPosition.x;
      entry.nodeY = modelPosition.y;
    }
    return;
  }
  const bucket = currentPath().buckets.find(candidate => candidate.id === id);
  if (!bucket) return;
  if (kind === 'category-anchor') {
    bucket.centerX = position.x;
    bucket.centerY = position.y;
  } else {
    bucket.plaqueX = position.x;
    bucket.plaqueY = position.y;
  }
}

function setEntryBucket(id: string, bucketId: string): void {
  const entry = currentPath().entries.find(candidate => candidate.id === id);
  if (entry) entry.bucketId = bucketId;
}

function removeBucket(id: string): void {
  const path = currentPath();
  path.buckets = path.buckets.filter(bucket => bucket.id !== id);
}

function restoreBucket(bucket: MaterialAtlasBucketModel): void {
  const path = currentPath();
  if (path.buckets.some(candidate => candidate.id === bucket.id)) return;
  path.buckets.push({ ...bucket });
}

function setEntryNodePosition(id: string, nodeX: number | null, nodeY: number | null): void {
  const entry = currentPath().entries.find(candidate => candidate.id === id);
  if (entry) {
    entry.nodeX = nodeX;
    entry.nodeY = nodeY;
  }
}

function setEntryParents(id: string, parentIds: string[]): void {
  const entry = currentPath().entries.find(candidate => candidate.id === id);
  if (entry) entry.parentIds = [...parentIds];
}

function setMaterialEntries(entries: MaterialAtlasEntryModel[]): void {
  currentPath().entries = cloneMaterialEntries(entries);
}

function stripRemovedParentIds(entries: MaterialAtlasEntryModel[], removedIds: Set<string>): MaterialAtlasEntryModel[] {
  return entries.map(entry => ({
    ...cloneMaterialEntry(entry),
    parentIds: entry.parentIds.filter(parentId => !removedIds.has(parentId))
  }));
}

function cloneMaterialEntries(entries: MaterialAtlasEntryModel[]): MaterialAtlasEntryModel[] {
  return entries.map(cloneMaterialEntry);
}

function cloneMaterialEntry(entry: MaterialAtlasEntryModel): MaterialAtlasEntryModel {
  return {
    ...entry,
    gate: { ...entry.gate },
    parentIds: [...entry.parentIds],
    catalog: entry.catalog ? { ...entry.catalog, gate: { ...entry.catalog.gate } } : undefined
  };
}

function materialPositionsForIds(ids: Set<string>): Record<string, { x: number; y: number }> {
  const layout = lastLayout ?? layoutMaterialAtlasPath(currentPath());
  const positions: Record<string, { x: number; y: number }> = {};
  for (const node of layout.nodes) {
    if (ids.has(node.id)) positions[node.id] = { x: node.x, y: node.y };
  }
  return positions;
}

function setMaterialRenderedPositions(positions: Record<string, { x: number; y: number }>): void {
  for (const [id, position] of Object.entries(positions)) {
    setPosition('material', id, position);
  }
}

function samePositionRecords(
  left: Record<string, { x: number; y: number }>,
  right: Record<string, { x: number; y: number }>
): boolean {
  const leftKeys = Object.keys(left);
  const rightKeys = Object.keys(right);
  return leftKeys.length === rightKeys.length
    && leftKeys.every(key => right[key] && left[key].x === right[key].x && left[key].y === right[key].y);
}

function updateMaterialGraphDomTraces(): void {
  const edgeGroup = app.querySelector<SVGGElement>('.edges');
  if (!edgeGroup) return;
  const layout = layoutMaterialAtlasPath(currentPath());
  lastLayout = layout;
  edgeGroup.innerHTML = layout.traces.map(renderTrace).join('');
}

function updatePositionInputs(kind: Exclude<SelectionKind, 'create-entry'>, id: string): void {
  if (kind === 'atlas-hub') {
    const path = currentPath();
    const hubX = app.querySelector<HTMLInputElement>('[data-path-edit="hubLabelX"]');
    const hubY = app.querySelector<HTMLInputElement>('[data-path-edit="hubLabelY"]');
    if (hubX) hubX.value = String(path.hubLabelX);
    if (hubY) hubY.value = String(path.hubLabelY);
    return;
  }
  if (kind === 'material') {
    const entry = currentPath().entries.find(candidate => candidate.id === id);
    if (!entry) return;
    const nodeX = app.querySelector<HTMLInputElement>('[data-entry-edit="nodeX"]');
    const nodeY = app.querySelector<HTMLInputElement>('[data-entry-edit="nodeY"]');
    if (nodeX) nodeX.value = entry.nodeX === null ? '' : String(entry.nodeX);
    if (nodeY) nodeY.value = entry.nodeY === null ? '' : String(entry.nodeY);
    return;
  }
  const bucket = currentPath().buckets.find(candidate => candidate.id === id);
  if (!bucket) return;
  const keys = kind === 'category-anchor'
    ? ['centerX', 'centerY'] as const
    : ['plaqueX', 'plaqueY'] as const;
  for (const key of keys) {
    const input = app.querySelector<HTMLInputElement>(`[data-bucket-edit="${key}"]`);
    if (input) input.value = String(bucket[key]);
  }
}

function selectedEntry(): MaterialAtlasEntryModel | undefined {
  return currentPath().entries.find(entry => entry.id === selection.id);
}

function selectedBucket(): MaterialAtlasBucketModel | undefined {
  return currentPath().buckets.find(bucket => bucket.id === selection.id);
}

function ensureCatalog(entry: MaterialAtlasEntryModel): MaterialCatalogEntryModel {
  if (!entry.catalog) {
    entry.catalog = {
      path: entry.path,
      id: entry.id,
      displayName: labelize(entry.id),
      description: '',
      category: 'Materials',
      iconSource: 'item',
      iconField: entry.id,
      hasRecipe: true,
      gate: { ...entry.gate }
    };
  }
  return entry.catalog;
}

function catalogueEntries(): MaterialCatalogEntryModel[] {
  return workspace?.paths.flatMap(path => path.entries.map(entry => ensureCatalog(entry))) ?? [];
}

function currentPath(): MaterialAtlasPathModel {
  return workspace?.paths.find(path => path.path === activePath) ?? workspace!.paths[0];
}

function pathButton(path: MaterialAtlasPathModel): string {
  return `<button class="branch-button ${path.path === activePath ? 'selected' : ''}" data-path="${escapeAttr(path.path)}">
    <span>${path.path === 'HARBINGER' ? 'Harbinger' : 'Unstained'}</span>
    <b>${path.entries.length}</b>
  </button>`;
}

function bucketButton(bucket: MaterialAtlasBucketModel): string {
  const selected = (selection.kind === 'category-anchor' || selection.kind === 'label-plaque') && selection.id === bucket.id ? 'selected' : '';
  return `<button class="skill-button ${selected}" data-select-kind="category-anchor" data-select-id="${escapeAttr(bucket.id)}">
    <span><b style="color:${escapeAttr(colorToCss(bucket.color))}">${escapeHtml(bucket.label)}</b><br/><small>category anchor / label plaque</small></span>
    <small>${escapeHtml(bucket.id)}</small>
  </button>`;
}

function materialButton(entry: MaterialAtlasEntryModel): string {
  const selected = selection.kind === 'material' && selection.id === entry.id || selectedMaterialIds.has(entry.id) ? 'selected' : '';
  return `<button class="skill-button ${selected}" data-select-kind="material" data-select-id="${escapeAttr(entry.id)}">
    <span>${escapeHtml(entry.catalog?.displayName ?? 'Unknown material')}</span>
    <small>${escapeHtml(entry.id)}</small>
  </button>`;
}

function tabButton(tab: ViewTab, label: string): string {
  return `<button class="${currentTab === tab ? 'active' : ''}" data-tab="${escapeAttr(tab)}">${escapeHtml(label)}</button>`;
}

function countBadge(count: number): string {
  return count ? ` <span class="badge">${count}</span>` : '';
}

function bucketOptions(selected: string): string {
  return currentPath().buckets.map(bucket => `<option value="${escapeAttr(bucket.id)}" ${bucket.id === selected ? 'selected' : ''}>${escapeHtml(bucket.label)}</option>`).join('');
}

function bucketLabel(bucketId: string): string {
  return currentPath().buckets.find(bucket => bucket.id === bucketId)?.label ?? bucketId;
}

function uniqueBucketId(base: string): string {
  const existing = new Set(currentPath().buckets.map(bucket => bucket.id));
  if (!existing.has(base)) return base;
  for (let i = 2; ; i += 1) {
    const candidate = `${base}_${i}`;
    if (!existing.has(candidate)) return candidate;
  }
}

function currentViewportGraphCenter(): { x: number; y: number } {
  const scroller = app.querySelector<HTMLElement>('.graph-scroll');
  if (scroller) {
    return {
      x: Math.round((scroller.scrollLeft + scroller.clientWidth / 2) / graphZoom),
      y: Math.round((scroller.scrollTop + scroller.clientHeight / 2) / graphZoom)
    };
  }
  const path = currentPath();
  return {
    x: path.hubLabelX,
    y: path.hubLabelY
  };
}

function gateOptions(selected: MaterialGateType): string {
  const allowed: MaterialGateType[] = activePath === 'HARBINGER'
    ? ['ALWAYS', 'DEGREE']
    : ['ALWAYS', 'PURITY', 'CLARITY'];
  return allowed.map(type => `<option value="${type}" ${type === selected ? 'selected' : ''}>${type}</option>`).join('');
}

function iconSourceOptions(selected: 'item' | 'block'): string {
  return ['item', 'block'].map(source => `<option value="${source}" ${source === selected ? 'selected' : ''}>${source === 'item' ? 'ItemInit item' : 'BlockInit block'}</option>`).join('');
}

function iconFieldOptions(source: 'item' | 'block', selected: string): string {
  const options = source === 'block' ? workspace?.iconOptions.blocks ?? [] : workspace?.iconOptions.items ?? [];
  const values = selected && !options.includes(selected) ? [selected, ...options] : options;
  return values.map(value => `<option value="${escapeAttr(value)}" ${value === selected ? 'selected' : ''}>${escapeHtml(value)}</option>`).join('');
}

function defaultGate(type: MaterialGateType): MaterialGateModel {
  if (type === 'ALWAYS') return { type, value: null };
  return { type, value: type === 'DEGREE' ? 1 : 10 };
}

function zoomGraphAt(scroller: HTMLElement | null, viewportX: number, viewportY: number, factor: number): void {
  if (!scroller || factor === 1) return;
  const nextZoom = clampZoom(graphZoom * factor, graphMinZoom, graphMaxZoom);
  if (nextZoom === graphZoom) return;
  const nextScroll = zoomScrollAnchor(graphZoom, nextZoom, viewportX, viewportY, scroller.scrollLeft, scroller.scrollTop);
  graphZoom = nextZoom;
  render();
  requestAnimationFrame(() => {
    const nextScroller = document.querySelector<HTMLElement>('.graph-scroll');
    if (!nextScroller) return;
    nextScroller.scrollLeft = Math.max(0, nextScroll.scrollLeft);
    nextScroller.scrollTop = Math.max(0, nextScroll.scrollTop);
  });
}

function centerX(): number {
  return (app.querySelector<HTMLElement>('.graph-scroll')?.clientWidth ?? 0) / 2;
}

function centerY(): number {
  return (app.querySelector<HTMLElement>('.graph-scroll')?.clientHeight ?? 0) / 2;
}

async function api<T = unknown>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      'content-type': 'application/json',
      ...(init?.headers ?? {})
    }
  });
  const json = await response.json();
  if (!response.ok) throw new Error(json.error ?? response.statusText);
  return json as T;
}

function colorToCss(value: string): string {
  const normalized = value.startsWith('0x') ? value.slice(2) : value.replace(/^#/, '');
  const rgb = normalized.length === 8 ? normalized.slice(2) : normalized;
  return `#${rgb.padStart(6, '0').slice(0, 6)}`;
}

function cssToAtlasColor(value: string): string {
  return `0xFF${value.replace(/^#/, '').toUpperCase()}`;
}

function relativeRoot(): string {
  return workspace?.repoRoot.split(/[\\/]/).slice(-3).join('/') ?? '';
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}

function cssEscape(value: string): string {
  return value.replace(/["\\]/g, '\\$&');
}

function clampAlignmentTolerance(value: string): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return Math.max(0, Math.min(64, Math.round(parsed)));
}

function draggedGraphElementSelector(kind: Exclude<SelectionKind, 'create-entry'>, id: string): string {
  const selector = `[data-select-kind="${kind}"][data-select-id="${cssEscape(id)}"]`;
  if (kind === 'material') return `.material-graph .material-node${selector}`;
  if (kind === 'category-anchor') return `.material-graph .category-anchor${selector}`;
  if (kind === 'atlas-hub') return `.material-graph .atlas-hub${selector}`;
  return `.material-graph .label-plaque${selector}`;
}

function graphPointFromPointer(scroller: HTMLElement, event: PointerEvent): { x: number; y: number } {
  const rect = scroller.getBoundingClientRect();
  return {
    x: (event.clientX - rect.left + scroller.scrollLeft) / graphZoom,
    y: (event.clientY - rect.top + scroller.scrollTop) / graphZoom
  };
}

function updateMarqueeSelectionRect(scroller: HTMLElement, state: MarqueeState): void {
  const svg = scroller.querySelector<SVGSVGElement>('svg.graph');
  if (!svg) return;
  let rect = svg.querySelector<SVGRectElement>('.marquee-selection');
  if (!rect) {
    rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    rect.setAttribute('class', 'marquee-selection');
    svg.appendChild(rect);
  }
  const x = Math.min(state.start.x, state.current.x);
  const y = Math.min(state.start.y, state.current.y);
  const width = Math.abs(state.current.x - state.start.x);
  const height = Math.abs(state.current.y - state.start.y);
  rect.setAttribute('x', String(x));
  rect.setAttribute('y', String(y));
  rect.setAttribute('width', String(width));
  rect.setAttribute('height', String(height));
}

function removeMarqueeSelectionRect(scroller: HTMLElement): void {
  scroller.querySelector('.marquee-selection')?.remove();
}

function updateConnectionPreview(scroller: HTMLElement, from: { x: number; y: number }, to: { x: number; y: number }): void {
  const svg = scroller.querySelector<SVGSVGElement>('svg.graph');
  if (!svg) return;
  let path = svg.querySelector<SVGPathElement>('.connection-preview');
  if (!path) {
    path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('class', 'connection-preview');
    svg.appendChild(path);
  }
  path.setAttribute('d', connectionPreviewPath(from, to));
}

function removeConnectionPreview(scroller: HTMLElement): void {
  scroller.querySelector('.connection-preview')?.remove();
}

function connectionPreviewPath(from: { x: number; y: number }, to: { x: number; y: number }): string {
  const midX = (from.x + to.x) / 2;
  return `M ${from.x} ${from.y} C ${midX} ${from.y}, ${midX} ${to.y}, ${to.x} ${to.y}`;
}

function connectionTargetIdAt(event: PointerEvent): string | null {
  const target = document.elementFromPoint(event.clientX, event.clientY) as Element | null;
  return target?.closest<SVGGElement>('.material-node[data-select-id]')?.dataset.selectId ?? null;
}

function sameList(left: string[], right: string[]): boolean {
  return left.length === right.length && left.every((value, index) => value === right[index]);
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>"']/g, ch => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[ch] ?? ch));
}

function escapeAttr(value: string): string {
  return escapeHtml(value);
}

function shouldIgnoreShortcut(target: EventTarget | null): boolean {
  return target instanceof HTMLInputElement
    || target instanceof HTMLSelectElement
    || target instanceof HTMLTextAreaElement
    || target instanceof HTMLElement && target.isContentEditable;
}
