import type { Diagnostic, ManipulationNodeModel, ManipulationWorkspace, PreviewResult } from '../shared/types';
import { beginConnectionDrag, type ConnectionDragState, finishConnectionDrag } from './connectionEditing';
import { beginDragPan, type DragPanState, shouldStartDragPan, updateDragPan } from './dragPan';
import { beginNodeDrag, type NodeDragState, updateNodeDrag } from './layoutEditing';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import {
  createMovementHistory,
  recordMovement,
  redoMovement,
  undoMovement
} from './movementHistory';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';

const graphMinZoom = 0.55;
const graphMaxZoom = 2.75;
const wheelZoomStep = 1.12;
const buttonZoomStep = 1.2;

let workspace: ManipulationWorkspace | null = null;
let selectedName = '';
let currentTab: ViewTab = 'graph';
let preview: PreviewResult | null = null;
let statusText = 'Loading manipulation workspace...';
let isBusy = false;
let snapToGrid = true;
let graphZoom = 1;
let movementHistory = createMovementHistory();
let draggingNode: NodeDragState | null = null;
let dragOriginBase: { x: number; y: number } | null = null;
let dragPan: DragPanState | null = null;
let dragConnection: ConnectionDragState | null = null;
let suppressNextGraphNodeClick = false;

const appRoot = document.querySelector<HTMLDivElement>('#app');
if (!appRoot) throw new Error('Missing app root.');
const app = appRoot;

window.addEventListener('keydown', event => {
  if (event.target && (event.target as HTMLElement).tagName === 'INPUT') return;
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    if (event.shiftKey) redoLastMovement();
    else undoLastMovement();
  }
});

void load();

async function load(): Promise<void> {
  isBusy = true;
  render();
  try {
    workspace = await api<ManipulationWorkspace>('/api/manipulations');
    selectedName = workspace.tree.nodes[0]?.name ?? '';
    movementHistory = createMovementHistory();
    preview = null;
    statusText = `Loaded ${workspace.tree.nodes.length} manipulation nodes.`;
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
            <h1>Skill Tree Editor</h1>
            <p>${escapeHtml(relativeRoot())}</p>
            <p><a href="/workspace.html">Skills</a> · <b>Manipulations</b></p>
          </div>
        </div>
        <div class="toolbar">
          <button data-action="reload" ${isBusy ? 'disabled' : ''}>Reload</button>
          <button data-action="preview" ${isBusy ? 'disabled' : ''}>Preview</button>
          <button data-action="apply" ${!preview?.canApply || isBusy ? 'disabled' : ''}>Apply</button>
        </div>
        <div class="branch-list">
          <div class="form-heading">
            <h2>Nodes</h2>
            <p>${escapeHtml(workspace.tree.path)}</p>
          </div>
          <div class="selection-controls">
            <label><input type="checkbox" data-action="snap-grid" ${snapToGrid ? 'checked' : ''}/> Snap</label>
          </div>
          ${workspace.tree.nodes.map(renderNodeButton).join('')}
        </div>
        <div class="status">${escapeHtml(statusText)}</div>
      </aside>
      <section class="content">
        <header class="tabs">
          ${tabButton('graph', 'Graph')}
          ${tabButton('validation', `Validation${countBadge(validationCount())}`)}
          ${tabButton('diff', `Diff${countBadge(preview?.diffs.length ?? 0)}`)}
        </header>
        <div class="tab-body">${renderTab()}</div>
      </section>
    </main>
  `;

  bindEvents();
}

function tabButton(tab: ViewTab, label: string): string {
  return `<button class="${currentTab === tab ? 'active' : ''}" data-tab="${tab}">${label}</button>`;
}

function countBadge(count: number): string {
  if (!count) return '';
  return ` <span class="badge">${count}</span>`;
}

function renderNodeButton(node: ManipulationNodeModel): string {
  const selected = node.name === selectedName ? 'selected' : '';
  const coords = `${Math.round(node.treeX)}, ${Math.round(node.treeY)}`;
  return `<button class="skill-button ${selected}" data-node="${escapeAttr(node.name)}">
    <span>
      <b style="color: ${escapeAttr(node.color)}">${escapeHtml(node.name)}</b><br/>
      <small>${escapeHtml(coords)}</small>
    </span>
    <small>${escapeHtml(node.tendency ?? '')}</small>
  </button>`;
}

function renderTab(): string {
  if (currentTab === 'validation') return renderValidation();
  if (currentTab === 'diff') return renderDiff();
  return renderGraph();
}

function renderValidation(): string {
  const diagnostics = workspace?.tree.diagnostics ?? [];
  if (!diagnostics.length) return `<div class="empty-state">No diagnostics.</div>`;
  return `<div class="diagnostics">${diagnostics.map(renderDiagnostic).join('')}</div>`;
}

function renderDiagnostic(diag: Diagnostic): string {
  const sev = escapeAttr(diag.severity);
  return `<div class="diag diag-${sev}">
    <b>${escapeHtml(diag.severity.toUpperCase())}</b>
    <span>${escapeHtml(diag.code)}</span>
    <p>${escapeHtml(diag.message)}</p>
  </div>`;
}

function renderDiff(): string {
  if (!preview) return `<div class="empty-state">Click Preview to generate diffs.</div>`;
  if (!preview.diffs.length) return `<div class="empty-state">No changes.</div>`;
  return `<div class="diffs">
    ${preview.diffs.map(diff => `<section class="diff">
      <h2>${escapeHtml(diff.path)}</h2>
      <pre>${escapeHtml(diff.patch)}</pre>
    </section>`).join('')}
  </div>`;
}

function renderGraph(): string {
  const layout = computeLayout(workspace!.tree.nodes);
  const edges = layout.edges.map(edge => renderEdge(edge.from, edge.to)).join('');
  const nodes = layout.nodes.map(node => renderNode(node.node, node.x, node.y)).join('');
  const zoomLabel = `${Math.round(graphZoom * 100)}%`;
  const scaledWidth = Math.round(layout.width * graphZoom);
  const scaledHeight = Math.round(layout.height * graphZoom);

  return `<div class="graph-shell">
    <div class="graph-zoom-controls" aria-label="Graph zoom controls">
      <button data-action="zoom-out" title="Zoom out">-</button>
      <button data-action="zoom-reset" title="Reset zoom">${zoomLabel}</button>
      <button data-action="zoom-in" title="Zoom in">+</button>
    </div>
    <div class="graph-scroll"><svg class="graph workspace-surface" width="${scaledWidth}" height="${scaledHeight}" viewBox="0 0 ${layout.width} ${layout.height}" role="img">
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
      <rect width="${layout.width}" height="${layout.height}" fill="url(#veinPattern)" opacity="0.58"></rect>
      <g class="edges">${edges}</g>
      <g class="nodes">${nodes}</g>
    </svg></div>
  </div>`;
}

function renderEdge(from: ManipulationNodeModel, to: ManipulationNodeModel): string {
  const bounds = computeBounds(workspace!.tree.nodes);
  const path = edgePath(from.treeX + bounds.offsetX, from.treeY + bounds.offsetY, to.treeX + bounds.offsetX, to.treeY + bounds.offsetY);
  return `<path class="edge wire-edge local-edge" style="stroke: ${escapeAttr(to.color)}" data-edge-from="${escapeAttr(from.name)}" data-edge-to="${escapeAttr(to.name)}" d="${escapeAttr(path)}" />`;
}

function renderNode(node: ManipulationNodeModel, x: number, y: number): string {
  const selected = node.name === selectedName ? 'selected' : '';
  const initial = node.name.charAt(0).toUpperCase() || '?';
  return `<g class="skill-node ${selected}" data-node="${escapeAttr(node.name)}" transform="translate(${x} ${y})">
    <rect class="node-glow" x="-22" y="-22" width="44" height="44" style="stroke: ${escapeAttr(node.color)}"></rect>
    <rect class="node-frame" x="-18" y="-18" width="36" height="36" style="stroke: ${escapeAttr(node.color)}"></rect>
    <rect class="node-core" x="-12" y="-12" width="24" height="24"></rect>
    <text x="0" y="5" class="node-fallback">${escapeHtml(initial)}</text>
  </g>`;
}

function bindEvents(): void {
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-action]')) {
    button.addEventListener('click', () => handleAction(button.dataset.action ?? ''));
  }
  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-node]')) {
    button.addEventListener('click', () => {
      selectedName = button.dataset.node ?? '';
      render();
    });
  }
  for (const tab of app.querySelectorAll<HTMLButtonElement>('button[data-tab]')) {
    tab.addEventListener('click', () => {
      currentTab = tab.dataset.tab as ViewTab;
      render();
    });
  }
  const snap = app.querySelector<HTMLInputElement>('input[data-action="snap-grid"]');
  if (snap) {
    snap.addEventListener('change', () => {
      snapToGrid = snap.checked;
      render();
    });
  }

  const scroll = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroll) return;
  scroll.addEventListener('wheel', event => {
    if (event.deltaY === 0) return;
    const rect = scroll.getBoundingClientRect();
    const factor = event.deltaY < 0 ? wheelZoomStep : 1 / wheelZoomStep;
    zoomGraphAt(scroll, event.clientX - rect.left, event.clientY - rect.top, factor);
    event.preventDefault();
  }, { passive: false });

  scroll.addEventListener('pointerdown', event => {
    if (event.button !== 0) return;
    const target = event.target as HTMLElement | null;
    const nodeElement = target?.closest<SVGGElement>('g[data-node]');
    const edgeElement = target?.closest<SVGPathElement>('path[data-edge-from]');

    if (edgeElement && event.shiftKey) {
      dragConnection = beginConnectionDrag(edgeElement.dataset.edgeFrom ?? '');
      scroll.classList.add('connecting-parent');
      return;
    }

    if (nodeElement) {
      const name = nodeElement.dataset.node ?? '';
      const node = findNode(name);
      if (!node) return;
      const bounds = computeBounds(workspace!.tree.nodes);
      selectedName = name;
      draggingNode = beginNodeDrag({
        clientX: event.clientX,
        clientY: event.clientY,
        nodeX: node.treeX + bounds.offsetX,
        nodeY: node.treeY + bounds.offsetY,
        scrollLeft: scroll.scrollLeft,
        scrollTop: scroll.scrollTop,
        zoom: graphZoom
      });
      dragOriginBase = { x: node.treeX, y: node.treeY };
      scroll.setPointerCapture(event.pointerId);
      scroll.classList.add('dragging-node');
      render();
      return;
    }

    if (shouldStartDragPan(event.target)) {
      dragPan = beginDragPan(event.clientX, event.clientY, scroll.scrollLeft, scroll.scrollTop);
      scroll.setPointerCapture(event.pointerId);
      scroll.classList.add('panning');
    }
  });

  scroll.addEventListener('pointermove', event => {
    if (dragPan) {
      const update = updateDragPan(dragPan, event.clientX, event.clientY);
      scroll.scrollLeft = update.scrollLeft;
      scroll.scrollTop = update.scrollTop;
      return;
    }
    if (!draggingNode) return;
    const node = findNode(selectedName);
    if (!node) return;
    const bounds = computeBounds(workspace!.tree.nodes);
    const pos = updateNodeDrag(draggingNode, {
      clientX: event.clientX,
      clientY: event.clientY,
      scrollLeft: scroll.scrollLeft,
      scrollTop: scroll.scrollTop,
      snap: snapToGrid ? 10 : 1,
      zoom: graphZoom
    });
    node.treeX = pos.x - bounds.offsetX;
    node.treeY = pos.y - bounds.offsetY;
    suppressNextGraphNodeClick = true;
    render();
  });

  scroll.addEventListener('pointerup', event => {
    if (dragPan) {
      dragPan = null;
      scroll.classList.remove('panning');
      return;
    }

    if (dragConnection) {
      const target = event.target as HTMLElement | null;
      const nodeElement = target?.closest<SVGGElement>('g[data-node]');
      const rewire = finishConnectionDrag(dragConnection, nodeElement?.dataset.node);
      dragConnection = null;
      scroll.classList.remove('connecting-parent');
      if (rewire) {
        const node = findNode(rewire.field);
        if (node && !node.parents.includes(rewire.parentField)) {
          node.parents = [...node.parents, rewire.parentField];
          statusText = `Added parent ${rewire.parentField} -> ${rewire.field}`;
          render();
        }
      }
      return;
    }

    if (!draggingNode) return;
    const node = findNode(selectedName);
    if (node && dragOriginBase) {
      recordMovement(movementHistory, {
        field: node.name,
        before: { x: dragOriginBase.x, y: dragOriginBase.y },
        after: { x: node.treeX, y: node.treeY }
      });
    }
    draggingNode = null;
    dragOriginBase = null;
    scroll.classList.remove('dragging-node');
  });
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
      zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), (app.querySelector<HTMLElement>('.graph-scroll')?.clientWidth ?? 0) / 2, (app.querySelector<HTMLElement>('.graph-scroll')?.clientHeight ?? 0) / 2, buttonZoomStep);
      return;
    case 'zoom-out':
      zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), (app.querySelector<HTMLElement>('.graph-scroll')?.clientWidth ?? 0) / 2, (app.querySelector<HTMLElement>('.graph-scroll')?.clientHeight ?? 0) / 2, 1 / buttonZoomStep);
      return;
    case 'zoom-reset':
      if (graphZoom !== 1) {
        zoomGraphAt(app.querySelector<HTMLElement>('.graph-scroll'), (app.querySelector<HTMLElement>('.graph-scroll')?.clientWidth ?? 0) / 2, (app.querySelector<HTMLElement>('.graph-scroll')?.clientHeight ?? 0) / 2, 1 / graphZoom);
      }
      return;
  }
}

function zoomGraphAt(scroller: HTMLElement | null, viewportX: number, viewportY: number, factor: number): void {
  if (!scroller) return;
  const nextZoom = clampZoom(graphZoom * factor, graphMinZoom, graphMaxZoom);
  if (nextZoom === graphZoom) return;
  const nextScroll = zoomScrollAnchor(
    graphZoom,
    nextZoom,
    viewportX,
    viewportY,
    scroller.scrollLeft,
    scroller.scrollTop
  );
  graphZoom = nextZoom;
  render();
  requestAnimationFrame(() => {
    const nextScroller = document.querySelector<HTMLElement>('.graph-scroll');
    if (!nextScroller) return;
    nextScroller.scrollLeft = Math.max(0, nextScroll.scrollLeft);
    nextScroller.scrollTop = Math.max(0, nextScroll.scrollTop);
  });
}

async function runPreview(): Promise<void> {
  if (!workspace) return;
  isBusy = true;
  statusText = 'Generating preview...';
  render();
  try {
    preview = await api<PreviewResult>('/api/manipulations/preview', {
      method: 'POST',
      body: JSON.stringify({ nodes: workspace.tree.nodes })
    });
    statusText = preview.diffs.length ? `Preview ready (${preview.diffs.length} file(s)).` : 'No changes.';
    currentTab = 'diff';
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
  statusText = 'Applying preview...';
  render();
  try {
    await api('/api/apply', {
      method: 'POST',
      body: JSON.stringify({ id: preview.id })
    });
    statusText = 'Applied changes. Reloading...';
    preview = null;
    await load();
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
  } finally {
    isBusy = false;
    render();
  }
}

function undoLastMovement(): void {
  const target = undoMovement(movementHistory);
  if (!target) return;
  applyMovementTarget(target);
}

function redoLastMovement(): void {
  const target = redoMovement(movementHistory);
  if (!target) return;
  applyMovementTarget(target);
}

function applyMovementTarget(target: { updates: Array<{ field?: string; position?: { x: number; y: number } }> }): void {
  if (!workspace) return;
  for (const update of target.updates) {
    if (!update.field || !update.position) continue;
    const node = findNode(update.field);
    if (!node) continue;
    node.treeX = update.position.x;
    node.treeY = update.position.y;
  }
  render();
}

function validationCount(): number {
  return workspace?.tree.diagnostics.length ?? 0;
}

function computeLayout(nodes: ManipulationNodeModel[]): {
  width: number;
  height: number;
  nodes: Array<{ node: ManipulationNodeModel; x: number; y: number }>;
  edges: Array<{ from: ManipulationNodeModel; to: ManipulationNodeModel }>;
} {
  const bounds = computeBounds(nodes);
  const width = bounds.width;
  const height = bounds.height;

  const byName = new Map(nodes.map(node => [node.name, node] as const));
  const edges: Array<{ from: ManipulationNodeModel; to: ManipulationNodeModel }> = [];
  for (const node of nodes) {
    for (const parent of node.parents) {
      const parentNode = byName.get(parent);
      if (parentNode) edges.push({ from: parentNode, to: node });
    }
  }

  return {
    width,
    height,
    nodes: nodes.map(node => ({ node, x: node.treeX + bounds.offsetX, y: node.treeY + bounds.offsetY })),
    edges
  };
}

function computeBounds(nodes: ManipulationNodeModel[]): { offsetX: number; offsetY: number; width: number; height: number } {
  const padding = 80;
  const positions = nodes.map(node => ({ x: node.treeX, y: node.treeY }));
  const minX = Math.min(...positions.map(pos => pos.x), 0);
  const minY = Math.min(...positions.map(pos => pos.y), 0);
  const maxX = Math.max(...positions.map(pos => pos.x), 0);
  const maxY = Math.max(...positions.map(pos => pos.y), 0);
  const offsetX = padding - minX;
  const offsetY = padding - minY;
  const width = maxX - minX + padding * 2;
  const height = maxY - minY + padding * 2;
  return { offsetX, offsetY, width, height };
}

function edgePath(x0: number, y0: number, x3: number, y3: number): string {
  const dx = x3 - x0;
  const dy = y3 - y0;
  const distance = Math.hypot(dx, dy);
  const handle = Math.max(36, Math.min(140, distance * 0.34));
  const sway = organicSway(x0, y0, x3, y3);
  const c1x = x0 + dx / Math.max(1, distance) * handle + sway.x;
  const c1y = y0 + dy / Math.max(1, distance) * handle + sway.y;
  const c2x = x3 - dx / Math.max(1, distance) * handle - sway.x;
  const c2y = y3 - dy / Math.max(1, distance) * handle - sway.y;
  return `M ${x0} ${y0} C ${c1x.toFixed(2)} ${c1y.toFixed(2)}, ${c2x.toFixed(2)} ${c2y.toFixed(2)}, ${x3} ${y3}`;
}

function organicSway(x1: number, y1: number, x2: number, y2: number): { x: number; y: number } {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const distance = Math.hypot(dx, dy);
  if (distance < 0.001) return { x: 0, y: 0 };
  const amount = Math.min(18, Math.max(8, distance * 0.08));
  const hash = ((x1 * 31) ^ (y1 * 17) ^ (x2 * 13) ^ (y2 * 7)) & 1;
  const sign = hash === 0 ? 1 : -1;
  return {
    x: -dy / distance * amount * sign,
    y: dx / distance * amount * sign
  };
}

function findNode(name: string): ManipulationNodeModel | undefined {
  return workspace?.tree.nodes.find(node => node.name === name);
}

function relativeRoot(): string {
  const root = workspace?.repoRoot ?? '';
  return root.replace(/^.*Hemomancy[\\/]/, '');
}

async function api<T = unknown>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, init);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  return await res.json() as T;
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
