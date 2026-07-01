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
  materialPositionFromDrag,
  sanitizeMaterialParents,
  type MaterialGraphLayout
} from './materialsGraph';
import { beginDragPan, type DragPanState, shouldStartDragPan, updateDragPan } from './dragPan';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';
type SelectionKind = 'material' | 'bucket-root' | 'label-plaque' | 'create-entry';

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

interface DragState {
  kind: Exclude<SelectionKind, 'create-entry'>;
  id: string;
  pointerId: number;
  pointerStart: { x: number; y: number };
  scrollStart: { x: number; y: number };
  origin: { x: number; y: number };
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
let graphZoom = 1;
let dragState: DragState | null = null;
let dragPan: DragPanState | null = null;
let undoStack: PositionChange[] = [];
let redoStack: PositionChange[] = [];
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
            <p><a href="/workspace.html">Skills</a> - <a href="/manipulations.html">Manipulations</a> - <b>Materials</b></p>
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
          <label class="snap-toggle"><input type="checkbox" data-action="snap-grid" ${snapToGrid ? 'checked' : ''}/> Snap</label>
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
        <rect width="${layout.width}" height="${layout.height}" fill="url(#bloodGlow)"></rect>
        <rect width="${layout.width}" height="${layout.height}" fill="url(#veinPattern)" opacity="0.5"></rect>
        <g class="edges">${layout.traces.map(renderTrace).join('')}</g>
        <g class="bucket-roots">${layout.bucketRoots.map(renderBucketRoot).join('')}</g>
        <g class="label-plaques">${layout.labelPlaques.map(renderLabelPlaque).join('')}</g>
        <g class="nodes">${layout.nodes.map(renderMaterialNode).join('')}</g>
      </svg>
    </div>
  </div>`;
}

function renderTrace(trace: MaterialGraphLayout['traces'][number]): string {
  return `<path class="edge wire-edge atlas-trace" data-trace-from="${escapeAttr(trace.fromId)}" data-trace-to="${escapeAttr(trace.toId)}" style="stroke:${escapeAttr(colorToCss(trace.color))}" d="${escapeAttr(trace.path)}"></path>`;
}

function renderBucketRoot(root: MaterialGraphLayout['bucketRoots'][number]): string {
  const selected = selection.kind === 'bucket-root' && selection.id === root.id ? 'selected' : '';
  return `<g class="bucket-root ${selected}" data-select-kind="bucket-root" data-select-id="${escapeAttr(root.id)}" transform="translate(${root.x} ${root.y})">
    <rect x="-15" y="-15" width="30" height="30" class="bucket-root-core" style="stroke:${escapeAttr(colorToCss(root.color))}"></rect>
    <line x1="-22" y1="0" x2="22" y2="0" style="stroke:${escapeAttr(colorToCss(root.color))}"></line>
    <line x1="0" y1="-22" x2="0" y2="22" style="stroke:${escapeAttr(colorToCss(root.color))}"></line>
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
  const selected = selection.kind === 'material' && selection.id === node.id ? 'selected' : '';
  const label = node.entry.catalog?.displayName ?? 'Unknown material';
  return `<g class="material-node ${selected}" data-select-kind="material" data-select-id="${escapeAttr(node.id)}" transform="translate(${node.x} ${node.y})">
    <rect class="node-glow" x="-23" y="-23" width="46" height="46" style="stroke:${escapeAttr(colorToCss(node.color))}"></rect>
    <rect class="node-frame" x="-18" y="-18" width="36" height="36" style="stroke:${escapeAttr(colorToCss(node.color))}"></rect>
    <rect class="node-core" x="-12" y="-12" width="24" height="24"></rect>
    <image href="${escapeAttr(node.iconUrl)}" x="-10" y="-10" width="20" height="20" preserveAspectRatio="xMidYMid meet"></image>
    <title>${escapeHtml(label)}</title>
  </g>`;
}

function renderInspector(): string {
  if (selection.kind === 'create-entry') return renderCreateEntryForm();
  if (selection.kind === 'bucket-root' || selection.kind === 'label-plaque') {
    const bucket = selectedBucket();
    return bucket ? renderBucketInspector(bucket) : '<div class="empty">No bucket selected.</div>';
  }
  const entry = selectedEntry();
  return entry ? renderMaterialInspector(entry) : '<div class="empty">No material selected.</div>';
}

function renderBucketInspector(bucket: MaterialAtlasBucketModel): string {
  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(bucket.label)}</h2>
        <p>${selection.kind === 'label-plaque' ? 'Category label plaque' : 'Bucket root'} - ${escapeHtml(bucket.id)}</p>
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
  const knownIds = currentPath().entries.map(candidate => candidate.id);
  const parentOptions = knownIds
    .filter(id => id !== entry.id)
    .map(id => `<option value="${escapeAttr(id)}">${escapeHtml(id)}</option>`)
    .join('');
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
      <textarea data-entry-edit="parentIds" rows="3">${escapeHtml(entry.parentIds.join(', '))}</textarea>
      <label>Add parent<select data-action="add-parent">${'<option value="">Add parent...</option>' + parentOptions}</select></label>
    </div>
    <div class="form-actions">
      <button type="button" data-action="clear-node-position">Use Auto Position</button>
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
      render();
    });
  }
  app.querySelector<HTMLInputElement>('input[data-action="snap-grid"]')?.addEventListener('change', event => {
    snapToGrid = (event.currentTarget as HTMLInputElement).checked;
  });
  for (const input of app.querySelectorAll<HTMLInputElement | HTMLSelectElement>('[data-bucket-edit]')) {
    input.addEventListener('change', () => updateBucket(input));
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
    const draggable = target?.closest<SVGGElement>('.material-node, .bucket-root, .label-plaque');
    if (!draggable) {
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
    dragState = {
      kind,
      id,
      pointerId: event.pointerId,
      pointerStart: { x: event.clientX, y: event.clientY },
      scrollStart: { x: scroller.scrollLeft, y: scroller.scrollTop },
      origin
    };
    draggable.setPointerCapture(event.pointerId);
    event.preventDefault();
  });
  scroller.addEventListener('pointermove', event => {
    if (dragPan) {
      const update = updateDragPan(dragPan, event.clientX, event.clientY);
      scroller.scrollLeft = update.scrollLeft;
      scroller.scrollTop = update.scrollTop;
      return;
    }
    if (!dragState || event.pointerId !== dragState.pointerId) return;
    const next = materialPositionFromDrag({
      origin: dragState.origin,
      pointerStart: dragState.pointerStart,
      pointerCurrent: { x: event.clientX, y: event.clientY },
      scrollStart: dragState.scrollStart,
      scrollCurrent: { x: scroller.scrollLeft, y: scroller.scrollTop },
      zoom: graphZoom,
      snap: snapToGrid ? 8 : 1
    });
    setPosition(dragState.kind, dragState.id, next);
    const element = app.querySelector<SVGGElement>(`[data-select-kind="${dragState.kind}"][data-select-id="${cssEscape(dragState.id)}"]`);
    element?.setAttribute('transform', `translate(${next.x} ${next.y})`);
    preview = null;
  });
  const finish = (event: PointerEvent): void => {
    if (dragPan) {
      dragPan = null;
      scroller.classList.remove('panning');
      return;
    }
    if (!dragState || event.pointerId !== dragState.pointerId) return;
    const after = positionFor(dragState.kind, dragState.id);
    if (after && (after.x !== dragState.origin.x || after.y !== dragState.origin.y)) {
      undoStack.push({ kind: dragState.kind, id: dragState.id, before: dragState.origin, after });
      redoStack = [];
      statusText = `Moved ${dragState.id}.`;
    }
    dragState = null;
    render();
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
    case 'create-material':
      createMaterial();
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

function updateEntry(input: HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement): void {
  const entry = selectedEntry();
  if (!entry) return;
  const key = input.dataset.entryEdit;
  if (key === 'bucketId') entry.bucketId = input.value;
  else if (key === 'gateType') {
    entry.gate = defaultGate(input.value as MaterialGateType);
    ensureCatalog(entry).gate = { ...entry.gate };
  } else if (key === 'gateValue') {
    entry.gate.value = input.value.trim() ? Number(input.value) : null;
    ensureCatalog(entry).gate = { ...entry.gate };
  } else if (key === 'nodeX' || key === 'nodeY') {
    entry[key] = input.value.trim() ? Number(input.value) : null;
  } else if (key === 'parentIds') {
    entry.parentIds = sanitizeMaterialParents(entry.id, input.value.split(','), new Set(currentPath().entries.map(candidate => candidate.id)));
  }
  preview = null;
  render();
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
  entry.parentIds = sanitizeMaterialParents(entry.id, [...entry.parentIds, parentId], new Set(currentPath().entries.map(candidate => candidate.id)));
  preview = null;
  render();
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
  setPosition(change.kind, change.id, change.before);
  redoStack.push(change);
  preview = null;
  statusText = `Undid move for ${change.id}.`;
  render();
}

function redoMovement(): void {
  const change = redoStack.pop();
  if (!change) return;
  setPosition(change.kind, change.id, change.after);
  undoStack.push(change);
  preview = null;
  statusText = `Redid move for ${change.id}.`;
  render();
}

function clearNodePosition(): void {
  const entry = selectedEntry();
  if (!entry) return;
  entry.nodeX = null;
  entry.nodeY = null;
  preview = null;
  render();
}

function positionFor(kind: Exclude<SelectionKind, 'create-entry'>, id: string): { x: number; y: number } | null {
  if (kind === 'material') {
    const entry = currentPath().entries.find(candidate => candidate.id === id);
    if (!entry) return null;
    const rootBucket = currentPath().buckets.find(bucket => bucket.id === entry.bucketId && bucket.rootMaterialId === entry.id);
    if (rootBucket) return { x: rootBucket.centerX, y: rootBucket.centerY };
    const node = (lastLayout ?? layoutMaterialAtlasPath(currentPath())).nodes.find(candidate => candidate.id === id);
    return { x: entry.nodeX ?? node?.x ?? 0, y: entry.nodeY ?? node?.y ?? 0 };
  }
  const bucket = currentPath().buckets.find(candidate => candidate.id === id);
  if (!bucket) return null;
  return kind === 'bucket-root'
    ? { x: bucket.centerX, y: bucket.centerY }
    : { x: bucket.plaqueX, y: bucket.plaqueY };
}

function setPosition(kind: Exclude<SelectionKind, 'create-entry'>, id: string, position: { x: number; y: number }): void {
  if (kind === 'material') {
    const entry = currentPath().entries.find(candidate => candidate.id === id);
    const rootBucket = entry ? currentPath().buckets.find(bucket => bucket.id === entry.bucketId && bucket.rootMaterialId === entry.id) : undefined;
    if (entry && rootBucket) {
      rootBucket.centerX = position.x;
      rootBucket.centerY = position.y;
      entry.nodeX = null;
      entry.nodeY = null;
      return;
    }
    if (entry) {
      entry.nodeX = position.x;
      entry.nodeY = position.y;
    }
    return;
  }
  const bucket = currentPath().buckets.find(candidate => candidate.id === id);
  if (!bucket) return;
  if (kind === 'bucket-root') {
    bucket.centerX = position.x;
    bucket.centerY = position.y;
  } else {
    bucket.plaqueX = position.x;
    bucket.plaqueY = position.y;
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
  const selected = (selection.kind === 'bucket-root' || selection.kind === 'label-plaque') && selection.id === bucket.id ? 'selected' : '';
  return `<button class="skill-button ${selected}" data-select-kind="bucket-root" data-select-id="${escapeAttr(bucket.id)}">
    <span><b style="color:${escapeAttr(colorToCss(bucket.color))}">${escapeHtml(bucket.label)}</b><br/><small>bucket root / label plaque</small></span>
    <small>${escapeHtml(bucket.id)}</small>
  </button>`;
}

function materialButton(entry: MaterialAtlasEntryModel): string {
  const selected = selection.kind === 'material' && selection.id === entry.id ? 'selected' : '';
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
