import type { Diagnostic, ManipulationNodeModel, ManipulationWorkspace, PreviewResult } from '../shared/types';
import { beginConnectionDrag, type ConnectionDragState, finishConnectionDrag } from './connectionEditing';
import { beginDragPan, type DragPanState, shouldStartDragPan, updateDragPan } from './dragPan';
import {
  beginNodeDrag,
  manipulationRingMetricsFromClusterSize,
  modelPositionFromRenderedPosition,
  type ManipulationRingMetrics,
  type NodeDragState,
  updateNodeDrag
} from './layoutEditing';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import {
  createMovementHistory,
  recordMovement,
  redoMovement,
  undoMovement
} from './movementHistory';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';

interface PositionedNode {
  node: ManipulationNodeModel;
  x: number;
  y: number;
}

interface NodePosition {
  x: number;
  y: number;
}

interface TendencyFrame {
  key: string;
  anchorX: number;
  anchorY: number;
  clusterCenterX: number;
  clusterCenterY: number;
}

interface ManipulationLayout {
  width: number;
  height: number;
  offsetX: number;
  offsetY: number;
  ringCenterX: number;
  ringCenterY: number;
  nodes: PositionedNode[];
  edges: Array<{ from: ManipulationNodeModel; to: ManipulationNodeModel }>;
  positionsByName: Map<string, NodePosition>;
  frameByTendency: Map<string, TendencyFrame>;
}

const graphMinZoom = 0.55;
const graphMaxZoom = 2.75;
const wheelZoomStep = 1.12;
const buttonZoomStep = 1.2;
const tendencyOrder = ['ANIMUS', 'FLAMMEUS', 'DUCTILIS', 'LUX', 'MORTEM', 'CONGEATIO', 'FERRIC', 'TENEBRIS'] as const;
const sourceCoordinateScale = 1;
const traceNodeTrimRadius = 11;
const traceOrganicSwayMin = 8;
const traceOrganicSwayMax = 18;
const traceOrganicSwayFactor = 0.08;
const layoutNodeSize = 26;
const layoutNodeGapX = 80;
const layoutPadding = 40;
const layoutRingMinRadius = 250;
const layoutRingRadiusScale = 1.35;
const tendencyColors = new Map<string, string>([
  ['ANIMUS', '#ff0000'],
  ['FLAMMEUS', '#ff6400'],
  ['DUCTILIS', '#ffff00'],
  ['LUX', '#ffffff'],
  ['MORTEM', '#003a00'],
  ['CONGEATIO', '#0064ff'],
  ['FERRIC', '#353535'],
  ['TENEBRIS', '#46006e']
]);

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
let dragConnectionSource: NodePosition | null = null;
let suppressNextGraphNodeClick = false;
let renderedLayout: ManipulationLayout | null = null;
let stableClusterCenters = new Map<string, NodePosition>();
let stableRingMetrics: ManipulationRingMetrics | null = null;

const appRoot = document.querySelector<HTMLDivElement>('#app');
if (!appRoot) throw new Error('Missing app root.');
const app = appRoot;

window.addEventListener('keydown', event => {
  if (shouldIgnoreMovementShortcut(event.target)) return;
  if (isMovementUndoShortcut(event)) {
    event.preventDefault();
    undoLastMovement();
  } else if (isMovementRedoShortcut(event)) {
    event.preventDefault();
    redoLastMovement();
  }
});

void load();

async function load(): Promise<void> {
  isBusy = true;
  render();
  try {
    workspace = await api<ManipulationWorkspace>('/api/manipulations');
    selectedName = workspace.tree.nodes[0]?.name ?? '';
    stableClusterCenters = computeClusterCenters(workspace.tree.nodes);
    stableRingMetrics = null;
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

  const priorGraphViewport = captureGraphViewport();

  app.innerHTML = `
    <main class="shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark"></span>
          <div>
            <h1>Manipulation Tree Editor</h1>
            <p>${escapeHtml(relativeRoot())}</p>
            <p><a href="/workspace.html">Skills</a> - <b>Manipulations</b></p>
          </div>
        </div>
        <div class="toolbar">
          <button data-action="reload" ${isBusy ? 'disabled' : ''}>Reload</button>
          <button data-action="preview" ${isBusy ? 'disabled' : ''}>Preview</button>
          <button data-action="apply" ${!preview?.canApply || isBusy ? 'disabled' : ''}>Apply</button>
        </div>
        <div class="branch-list">
          <div class="form-heading">
            <h2>Manipulations</h2>
            <p>${escapeHtml(workspace.tree.path)}</p>
          </div>
        </div>
        <div class="skill-list">
          ${workspace.tree.nodes.map(renderNodeButton).join('')}
        </div>
      </aside>
      <section class="main">
        <header class="topbar">
          <nav class="tabs">
            ${tabButton('graph', 'Layout')}
            ${tabButton('validation', `Validation${countBadge(validationCount())}`)}
            ${tabButton('diff', `Diff${countBadge(preview?.diffs.length ?? 0)}`)}
          </nav>
          <div class="history-controls" aria-label="Movement history controls">
            <button data-action="undo-move" title="Undo move (Ctrl+Z)" ${!movementHistory.canUndo || isBusy ? 'disabled' : ''}>Undo</button>
            <button data-action="redo-move" title="Redo move (Ctrl+Y or Ctrl+Shift+Z)" ${!movementHistory.canRedo || isBusy ? 'disabled' : ''}>Redo</button>
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

  bindEvents();
  restoreGraphViewport(priorGraphViewport);
}

function captureGraphViewport(): { left: number; top: number } | null {
  const scroller = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroller) return null;
  return {
    left: scroller.scrollLeft,
    top: scroller.scrollTop
  };
}

function restoreGraphViewport(viewport: { left: number; top: number } | null): void {
  if (!viewport) return;
  const scroller = document.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroller) return;
  scroller.scrollLeft = Math.max(0, viewport.left);
  scroller.scrollTop = Math.max(0, viewport.top);
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
    <small>${escapeHtml(tendencySummary(node))}</small>
  </button>`;
}

function renderTab(): string {
  if (currentTab === 'validation') return renderValidation();
  if (currentTab === 'diff') return renderDiff();
  return renderGraph();
}

function renderValidation(): string {
  const diagnostics = workspace?.tree.diagnostics ?? [];
  if (!diagnostics.length) return `<div class="empty">No diagnostics.</div>`;
  return `<div class="diagnostics">${diagnostics.map(renderDiagnostic).join('')}</div>`;
}

function renderDiagnostic(diag: Diagnostic): string {
  return `<article class="diagnostic ${escapeAttr(diag.severity)}">
    <b>${escapeHtml(diag.severity.toUpperCase())}</b>
    <span>${escapeHtml(diag.code)}</span>
    <p>${escapeHtml(diag.message)}</p>
  </article>`;
}

function renderDiff(): string {
  if (!preview) return `<div class="empty">Click Preview to generate diffs.</div>`;
  if (!preview.diffs.length) return `<div class="empty">No changes.</div>`;
  return `<div class="diffs">
    ${preview.diffs.map(diff => `<section class="diff">
      <h2>${escapeHtml(diff.path)}</h2>
      <pre>${escapeHtml(diff.patch)}</pre>
    </section>`).join('')}
  </div>`;
}

function renderInspector(): string {
  const node = findNode(selectedName);
  if (!node) return '<div class="empty">No manipulation selected.</div>';

  const candidates = allNodes()
    .filter(candidate => candidate.name !== node.name)
    .sort((left, right) => left.name.localeCompare(right.name));

  const parentOptions = [
    '<option value="">Add parent...</option>',
    ...candidates
      .filter(candidate => !node.parents.includes(candidate.name))
      .map(candidate => `<option value="${escapeAttr(candidate.name)}">${escapeHtml(candidate.name)}</option>`)
  ].join('');

  const softParentOptions = [
    '<option value="">Add soft parent...</option>',
    ...candidates
      .filter(candidate => !node.softParents.includes(candidate.name))
      .map(candidate => `<option value="${escapeAttr(candidate.name)}">${escapeHtml(candidate.name)}</option>`)
  ].join('');

  const parents = node.parents.length
    ? node.parents.map(parent => `<button type="button" class="parent-pill" data-action="remove-parent" data-parent-kind="hard" data-parent-name="${escapeAttr(parent)}"><span>${escapeHtml(parent)}</span><b>x</b></button>`).join('')
    : '<span class="parent-empty">None</span>';
  const softParents = node.softParents.length
    ? node.softParents.map(parent => `<button type="button" class="parent-pill" data-action="remove-parent" data-parent-kind="soft" data-parent-name="${escapeAttr(parent)}"><span>${escapeHtml(parent)}</span><b>x</b></button>`).join('')
    : '<span class="parent-empty">None</span>';
  const tendencyOptions = tendencyOrder
    .map(tendency => `<option value="${escapeAttr(tendency)}" ${node.tendency === tendency ? 'selected' : ''}>${escapeHtml(tendency)}</option>`)
    .join('');
  const secondaryTendencyOptions = [
    `<option value="" ${node.secondaryTendency ? '' : 'selected'}>None</option>`,
    ...tendencyOrder.map(tendency => `<option value="${escapeAttr(tendency)}" ${node.secondaryTendency === tendency ? 'selected' : ''}>${escapeHtml(tendency)}</option>`)
  ].join('');

  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(node.name)}</h2>
        <p>${escapeHtml(tendencySummary(node) || 'Unaligned')} - ${escapeHtml(node.nodeShape)}</p>
      </div>
      <div class="icon-chip">${escapeHtml(node.color)}</div>
    </div>
    <label>
      <span>Tendency</span>
      <select data-edit="tendency">${tendencyOptions}</select>
    </label>
    <label>
      <span>Secondary Tendency</span>
      <select data-edit="secondaryTendency">${secondaryTendencyOptions}</select>
    </label>
    <div class="grid2">
      <label>
        <span>Tree X</span>
        <input type="number" data-edit="treeX" value="${escapeAttr(String(node.treeX))}" />
      </label>
      <label>
        <span>Tree Y</span>
        <input type="number" data-edit="treeY" value="${escapeAttr(String(node.treeY))}" />
      </label>
    </div>
    <label>
      <span>Color</span>
      <input type="text" value="${escapeAttr(node.color)}" readonly />
    </label>
    <div class="parent-editor">
      <span class="field-label">Parents</span>
      <div class="parent-list">${parents}</div>
      <label>Add<select data-action="add-parent" data-parent-kind="hard">${parentOptions}</select></label>
    </div>
    <div class="parent-editor">
      <span class="field-label">Soft Parents</span>
      <div class="parent-list">${softParents}</div>
      <label>Add<select data-action="add-parent" data-parent-kind="soft">${softParentOptions}</select></label>
    </div>
  </form>`;
}

function renderGraph(): string {
  const layout = computeLayout(workspace!.tree.nodes);
  renderedLayout = layout;
  const edges = layout.edges.map(edge => renderEdge(edge.from, edge.to)).join('');
  const nodes = layout.nodes.map(node => renderNode(node.node, node.x, node.y)).join('');
  const zoomLabel = `${Math.round(graphZoom * 100)}%`;
  const scaledWidth = Math.round(layout.width * graphZoom);
  const scaledHeight = Math.round(layout.height * graphZoom);
  const tendencyGuides = tendencyOrder.map(renderTendencyGuide).join('');

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
      <g class="degree-guides">${tendencyGuides}</g>
      <g class="edges">${edges}</g>
      <g class="nodes">${nodes}</g>
    </svg></div>
  </div>`;
}

function renderTendencyGuide(tendency: string): string {
  const layout = renderedLayout;
  const frame = layout?.frameByTendency.get(tendency);
  if (!layout || !frame) return '';
  const x = frame.anchorX + layout.offsetX;
  const y = frame.anchorY + layout.offsetY;
  return `<text x="${x.toFixed(0)}" y="${(y - 74).toFixed(0)}" class="degree-guide-label">${escapeHtml(tendency)}</text>`;
}

function renderEdge(from: ManipulationNodeModel, to: ManipulationNodeModel): string {
  const layout = renderedLayout ?? computeLayout(workspace!.tree.nodes);
  const fromPos = layout.positionsByName.get(from.name);
  const toPos = layout.positionsByName.get(to.name);
  if (!fromPos || !toPos) return '';
  const path = edgePath(fromPos.x, fromPos.y, toPos.x, toPos.y, layout.ringCenterX, layout.ringCenterY);
  return `<path class="edge wire-edge local-edge" style="stroke: ${escapeAttr(to.color)}" data-edge-from="${escapeAttr(from.name)}" data-edge-to="${escapeAttr(to.name)}" d="${escapeAttr(path)}" />`;
}

function renderNode(node: ManipulationNodeModel, x: number, y: number): string {
  const selected = node.name === selectedName ? 'selected' : '';
  const initial = node.name.charAt(0).toUpperCase() || '?';
  const memoryBaseUrl = '/asset/item/memory_blank.png';
  const memoryOverlayUrl = `/asset/item/${encodeURIComponent(memoryOverlayName(node.name))}.png`;
  return `<g class="skill-node ${selected}" data-node="${escapeAttr(node.name)}" transform="translate(${x} ${y})">
    <rect class="node-glow" x="-22" y="-22" width="44" height="44" style="stroke: ${escapeAttr(secondaryTendencyColor(node))}"></rect>
    <rect class="node-frame" x="-18" y="-18" width="36" height="36" style="stroke: ${escapeAttr(node.color)}"></rect>
    <rect class="node-core" x="-12" y="-12" width="24" height="24"></rect>
    <image class="memory-base" href="${escapeAttr(memoryBaseUrl)}" x="-11" y="-11" width="22" height="22" preserveAspectRatio="xMidYMid meet"></image>
    <image class="memory-overlay" href="${escapeAttr(memoryOverlayUrl)}" x="-11" y="-11" width="22" height="22" preserveAspectRatio="xMidYMid meet"></image>
    <text x="0" y="5" class="node-fallback">${escapeHtml(initial)}</text>
  </g>`;
}

function memoryOverlayName(nodeName: string): string {
  switch (nodeName) {
    case 'conjure_axe': return 'memory_living_axe_overlay';
    case 'conjure_blade': return 'memory_living_blade_overlay';
    case 'conjure_claws': return 'memory_living_claws_overlay';
    case 'conjure_crossbow': return 'memory_living_crossbow_overlay';
    case 'conjure_flail': return 'memory_living_flail_overlay';
    case 'conjure_spear': return 'memory_living_spear_overlay';
    case 'conjure_staff': return 'memory_living_staff_overlay';
    case 'conjure_torch': return 'memory_living_torch_overlay';
    default: return `memory_${nodeName}_overlay`;
  }
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

  wireInspectorEvents();

  const scroll = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroll) return;
  const svg = scroll.querySelector<SVGSVGElement>('.graph');
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
    const layout = renderedLayout ?? computeLayout(workspace!.tree.nodes);

    if (edgeElement && (event.shiftKey || event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      event.stopPropagation();
      const sourceField = edgeElement.dataset.edgeFrom ?? '';
      dragConnection = beginConnectionDrag(sourceField);
      const sourceNode = findNode(sourceField);
      if (sourceNode) {
        const sourcePosition = renderedLayout?.positionsByName.get(sourceField);
        if (sourcePosition) dragConnectionSource = sourcePosition;
      }
      scroll.setPointerCapture(event.pointerId);
      scroll.classList.add('connecting-parent');
      if (svg && dragConnectionSource) {
        updateConnectionPreview(svg, dragConnectionSource, graphPointFromPointer(scroll, event));
      }
      return;
    }

    if (nodeElement) {
      const name = nodeElement.dataset.node ?? '';
      const node = findNode(name);
      if (!node) return;
      const nodePos = layout.positionsByName.get(name);
      if (!nodePos) return;
      selectedName = name;

      if (event.ctrlKey || event.metaKey) {
        event.preventDefault();
        event.stopPropagation();
        dragConnection = beginConnectionDrag(name);
        dragConnectionSource = nodePos;
        scroll.setPointerCapture(event.pointerId);
        scroll.classList.add('connecting-parent');
        if (svg && dragConnectionSource) {
          updateConnectionPreview(svg, dragConnectionSource, graphPointFromPointer(scroll, event));
        }
        return;
      }

      selectedName = name;
      draggingNode = beginNodeDrag({
        clientX: event.clientX,
        clientY: event.clientY,
        nodeX: nodePos.x,
        nodeY: nodePos.y,
        scrollLeft: scroll.scrollLeft,
        scrollTop: scroll.scrollTop,
        zoom: graphZoom
      });
      dragOriginBase = { x: node.treeX, y: node.treeY };
      scroll.setPointerCapture(event.pointerId);
      scroll.classList.add('dragging-node');
      return;
    }

    if (event.ctrlKey || event.metaKey) return;

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
    if (dragConnection) {
      if (svg && dragConnectionSource) {
        updateConnectionPreview(svg, dragConnectionSource, graphPointFromPointer(scroll, event));
      }
      return;
    }
    if (!draggingNode) return;
    const node = findNode(selectedName);
    if (!node) return;
    const layout = renderedLayout ?? computeLayout(workspace!.tree.nodes);
    const pos = updateNodeDrag(draggingNode, {
      clientX: event.clientX,
      clientY: event.clientY,
      scrollLeft: scroll.scrollLeft,
      scrollTop: scroll.scrollTop,
      snap: snapToGrid ? 10 : 1,
      zoom: graphZoom
    });
    const nextModelPosition = toModelPosition(node, pos, layout);
    node.treeX = nextModelPosition.x;
    node.treeY = nextModelPosition.y;
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
      const targetField = connectionTargetNodeAt(event);
      const rewire = finishConnectionDrag(dragConnection, targetField ?? undefined);
      dragConnection = null;
      dragConnectionSource = null;
      if (svg) removeConnectionPreview(svg);
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
    render();
  });
}

function wireInspectorEvents(): void {
  const selected = findNode(selectedName);
  if (!selected) return;

  for (const input of app.querySelectorAll<HTMLInputElement>('input[data-edit]')) {
    input.addEventListener('change', () => updateSelectedNode(input));
  }
  const tendency = app.querySelector<HTMLSelectElement>('select[data-edit="tendency"]');
  if (tendency) {
    tendency.addEventListener('change', () => updateSelectedNodeTendency(tendency));
  }
  const secondaryTendency = app.querySelector<HTMLSelectElement>('select[data-edit="secondaryTendency"]');
  if (secondaryTendency) {
    secondaryTendency.addEventListener('change', () => updateSelectedNodeSecondaryTendency(secondaryTendency));
  }

  for (const button of app.querySelectorAll<HTMLButtonElement>('button[data-action="remove-parent"]')) {
    button.addEventListener('click', () => {
      const kind = button.dataset.parentKind === 'soft' ? 'soft' : 'hard';
      const parentName = button.dataset.parentName;
      if (!parentName) return;
      removeParentFromSelectedNode(kind, parentName);
    });
  }

  for (const select of app.querySelectorAll<HTMLSelectElement>('select[data-action="add-parent"]')) {
    select.addEventListener('change', () => {
      const parentName = select.value;
      if (!parentName) return;
      const kind = select.dataset.parentKind === 'soft' ? 'soft' : 'hard';
      addParentToSelectedNode(kind, parentName);
    });
  }
}

function updateSelectedNode(input: HTMLInputElement): void {
  const node = findNode(selectedName);
  if (!node) return;
  const key = input.dataset.edit;
  if (key !== 'treeX' && key !== 'treeY') return;
  const numeric = Number(input.value);
  if (!Number.isFinite(numeric)) {
    render();
    return;
  }
  const nextValue = Math.round(numeric);
  const before = { x: node.treeX, y: node.treeY };
  if (key === 'treeX') node.treeX = nextValue;
  if (key === 'treeY') node.treeY = nextValue;
  const after = { x: node.treeX, y: node.treeY };
  recordMovement(movementHistory, {
    field: node.name,
    before,
    after
  });
  preview = null;
  statusText = `Updated ${node.name} to (${node.treeX}, ${node.treeY}).`;
  render();
}

function updateSelectedNodeTendency(select: HTMLSelectElement): void {
  const node = findNode(selectedName);
  if (!node) return;
  const nextTendency = select.value;
  if (!tendencyColors.has(nextTendency) || node.tendency === nextTendency) {
    render();
    return;
  }
  node.tendency = nextTendency;
  node.color = tendencyColors.get(nextTendency) ?? node.color;
  if (node.secondaryTendency === nextTendency) node.secondaryTendency = null;
  preview = null;
  statusText = `Updated ${node.name} tendency to ${nextTendency}.`;
  render();
}

function updateSelectedNodeSecondaryTendency(select: HTMLSelectElement): void {
  const node = findNode(selectedName);
  if (!node) return;
  const nextTendency = select.value || null;
  if (nextTendency !== null && !tendencyColors.has(nextTendency)) {
    render();
    return;
  }
  const normalized = nextTendency === node.tendency ? null : nextTendency;
  if (node.secondaryTendency === normalized) {
    render();
    return;
  }
  node.secondaryTendency = normalized;
  preview = null;
  statusText = normalized
    ? `Updated ${node.name} secondary tendency to ${normalized}.`
    : `Cleared ${node.name} secondary tendency.`;
  render();
}

function tendencySummary(node: ManipulationNodeModel): string {
  if (!node.tendency) return node.secondaryTendency ?? '';
  return node.secondaryTendency ? `${node.tendency} / ${node.secondaryTendency}` : node.tendency;
}

function secondaryTendencyColor(node: ManipulationNodeModel): string {
  return node.secondaryTendency ? (tendencyColors.get(node.secondaryTendency) ?? node.color) : node.color;
}

function addParentToSelectedNode(kind: 'hard' | 'soft', parentName: string): void {
  const node = findNode(selectedName);
  if (!node || parentName === node.name) return;
  const list = kind === 'soft' ? node.softParents : node.parents;
  if (list.includes(parentName)) return;
  list.push(parentName);
  preview = null;
  statusText = `Added ${kind === 'soft' ? 'soft parent' : 'parent'} ${parentName} to ${node.name}.`;
  render();
}

function removeParentFromSelectedNode(kind: 'hard' | 'soft', parentName: string): void {
  const node = findNode(selectedName);
  if (!node) return;
  if (kind === 'soft') {
    node.softParents = node.softParents.filter(parent => parent !== parentName);
  } else {
    node.parents = node.parents.filter(parent => parent !== parentName);
  }
  preview = null;
  statusText = `Removed ${kind === 'soft' ? 'soft parent' : 'parent'} ${parentName} from ${node.name}.`;
  render();
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
    case 'undo-move':
      undoLastMovement();
      return;
    case 'redo-move':
      redoLastMovement();
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

function isMovementUndoShortcut(event: KeyboardEvent): boolean {
  return hasCommandModifier(event) && !event.shiftKey && !event.altKey && event.key.toLowerCase() === 'z';
}

function isMovementRedoShortcut(event: KeyboardEvent): boolean {
  if (!hasCommandModifier(event) || event.altKey) return false;
  const key = event.key.toLowerCase();
  return key === 'y' || (event.shiftKey && key === 'z');
}

function hasCommandModifier(event: KeyboardEvent): boolean {
  return event.ctrlKey || event.metaKey;
}

function shouldIgnoreMovementShortcut(target: EventTarget | null): boolean {
  return target instanceof HTMLInputElement
    || target instanceof HTMLSelectElement
    || target instanceof HTMLTextAreaElement
    || target instanceof HTMLElement && target.isContentEditable;
}

function validationCount(): number {
  return workspace?.tree.diagnostics.length ?? 0;
}

function computeLayout(nodes: ManipulationNodeModel[]): {
  width: number;
  height: number;
  offsetX: number;
  offsetY: number;
  ringCenterX: number;
  ringCenterY: number;
  nodes: Array<{ node: ManipulationNodeModel; x: number; y: number }>;
  edges: Array<{ from: ManipulationNodeModel; to: ManipulationNodeModel }>;
  positionsByName: Map<string, NodePosition>;
  frameByTendency: Map<string, TendencyFrame>;
} {
  const groups = new Map<string, ManipulationNodeModel[]>();
  const fallbackNodes: ManipulationNodeModel[] = [];
  for (const node of nodes) {
    const key = tendencyKey(node);
    if (key === '__FALLBACK__') {
      fallbackNodes.push(node);
      continue;
    }
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key)!.push(node);
  }

  let maxClusterW = 0;
  let maxClusterH = 0;
  const boundsByTendency = new Map<string, { minX: number; maxX: number; minY: number; maxY: number }>();
  for (const tendency of tendencyOrder) {
    const tendencyNodes = groups.get(tendency) ?? [];
    if (!tendencyNodes.length) continue;
    const minX = Math.min(...tendencyNodes.map(node => node.treeX));
    const maxX = Math.max(...tendencyNodes.map(node => node.treeX));
    const minY = Math.min(...tendencyNodes.map(node => node.treeY));
    const maxY = Math.max(...tendencyNodes.map(node => node.treeY));
    boundsByTendency.set(tendency, { minX, maxX, minY, maxY });
    maxClusterW = Math.max(maxClusterW, maxX - minX + layoutNodeSize);
    maxClusterH = Math.max(maxClusterH, maxY - minY + layoutNodeSize);
  }

  const ringMetrics = manipulationRingMetricsFromClusterSize(maxClusterW, maxClusterH, stableRingMetrics ?? undefined, {
    padding: layoutPadding,
    minRadius: layoutRingMinRadius,
    radiusScale: layoutRingRadiusScale
  });
  if (!stableRingMetrics) stableRingMetrics = ringMetrics;
  const { clusterHalf, branchRadius, ringCenterRawX, ringCenterRawY } = ringMetrics;

  const frameByTendency = new Map<string, TendencyFrame>();
  for (let i = 0; i < tendencyOrder.length; i += 1) {
    const tendency = tendencyOrder[i];
    const tendencyNodes = groups.get(tendency) ?? [];
    if (!tendencyNodes.length) continue;
    const bounds = boundsByTendency.get(tendency);
    if (!bounds) continue;
    const stableCenter = stableClusterCenters.get(tendency);
    const clusterCenterX = stableCenter?.x ?? (bounds.minX + bounds.maxX) * 0.5;
    const clusterCenterY = stableCenter?.y ?? (bounds.minY + bounds.maxY) * 0.5;
    const angle = (-Math.PI / 2) + (i * Math.PI / 4);
    const anchorX = ringCenterRawX + Math.cos(angle) * branchRadius;
    const anchorY = ringCenterRawY + Math.sin(angle) * branchRadius;
    frameByTendency.set(tendency, {
      key: tendency,
      anchorX,
      anchorY,
      clusterCenterX,
      clusterCenterY
    });
  }

  const absoluteByName = new Map<string, NodePosition>();
  for (const node of nodes) {
    const key = tendencyKey(node);
    const frame = frameByTendency.get(key);
    if (!frame) {
      absoluteByName.set(node.name, { x: node.treeX, y: node.treeY });
      continue;
    }
    const local = {
      x: (node.treeX - frame.clusterCenterX) * sourceCoordinateScale,
      y: (node.treeY - frame.clusterCenterY) * sourceCoordinateScale
    };
    const absolute = {
      x: frame.anchorX + local.x,
      y: frame.anchorY + local.y
    };
    absoluteByName.set(node.name, absolute);
  }

  if (fallbackNodes.length) {
    const fallbackY = ringCenterRawY + branchRadius + clusterHalf + layoutNodeGapX;
    const startX = ringCenterRawX - ((fallbackNodes.length - 1) * layoutNodeGapX) * 0.5;
    fallbackNodes
      .slice()
      .sort((a, b) => a.name.localeCompare(b.name))
      .forEach((node, index) => {
        absoluteByName.set(node.name, {
          x: startX + index * layoutNodeGapX,
          y: fallbackY
        });
      });
  }

  const bounds = computeBounds(Array.from(absoluteByName.values()));
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

  const positionsByName = new Map<string, NodePosition>();
  for (const [name, position] of absoluteByName.entries()) {
    positionsByName.set(name, {
      x: position.x + bounds.offsetX,
      y: position.y + bounds.offsetY
    });
  }

  return {
    width,
    height,
    offsetX: bounds.offsetX,
    offsetY: bounds.offsetY,
    ringCenterX: ringCenterRawX + bounds.offsetX,
    ringCenterY: ringCenterRawY + bounds.offsetY,
    nodes: nodes.map(node => {
      const position = positionsByName.get(node.name) ?? { x: bounds.offsetX, y: bounds.offsetY };
      return { node, x: position.x, y: position.y };
    }),
    edges,
    positionsByName,
    frameByTendency
  };
}

function computeBounds(positions: NodePosition[]): { offsetX: number; offsetY: number; width: number; height: number } {
  const padding = 80;
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

function computeClusterCenters(nodes: ManipulationNodeModel[]): Map<string, NodePosition> {
  const groups = new Map<string, ManipulationNodeModel[]>();
  for (const node of nodes) {
    const key = tendencyKey(node);
    if (key === '__FALLBACK__') continue;
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key)!.push(node);
  }
  const centers = new Map<string, NodePosition>();
  for (const [key, group] of groups.entries()) {
    centers.set(key, {
      x: (Math.min(...group.map(node => node.treeX)) + Math.max(...group.map(node => node.treeX))) * 0.5,
      y: (Math.min(...group.map(node => node.treeY)) + Math.max(...group.map(node => node.treeY))) * 0.5
    });
  }
  return centers;
}

function tendencyKey(node: ManipulationNodeModel): string {
  const key = (node.tendency ?? '').trim().toUpperCase();
  return tendencyOrder.includes(key as (typeof tendencyOrder)[number]) ? key : '__FALLBACK__';
}

function toModelPosition(node: ManipulationNodeModel, absolutePosition: NodePosition, layout: ManipulationLayout): NodePosition {
  const frame = layout.frameByTendency.get(tendencyKey(node));
  if (!frame) return { x: node.treeX, y: node.treeY };
  return modelPositionFromRenderedPosition({
    x: absolutePosition.x - layout.offsetX,
    y: absolutePosition.y - layout.offsetY
  }, frame, sourceCoordinateScale);
}

function edgePath(x0: number, y0: number, x3: number, y3: number, centerX: number, centerY: number): string {
  const start = trimTraceEndpoint(x0, y0, x3, y3);
  const end = trimTraceEndpoint(x3, y3, x0, y0);
  const controls = cubicControls(start.x, start.y, end.x, end.y, centerX, centerY);
  return `M ${start.x.toFixed(2)} ${start.y.toFixed(2)} C ${controls.c1x.toFixed(2)} ${controls.c1y.toFixed(2)}, ${controls.c2x.toFixed(2)} ${controls.c2y.toFixed(2)}, ${end.x.toFixed(2)} ${end.y.toFixed(2)}`;
}

function trimTraceEndpoint(x: number, y: number, towardX: number, towardY: number): NodePosition {
  const dx = towardX - x;
  const dy = towardY - y;
  const length = Math.hypot(dx, dy);
  if (length < 0.001) return { x, y };
  const offset = Math.min(traceNodeTrimRadius, Math.max(0, length / 2 - 1));
  return {
    x: x + dx / length * offset,
    y: y + dy / length * offset
  };
}

function cubicControls(x1: number, y1: number, x2: number, y2: number, centerX: number, centerY: number): { c1x: number; c1y: number; c2x: number; c2y: number } {
  const distance = Math.hypot(x2 - x1, y2 - y1);
  const handle = Math.max(36, Math.min(120, distance * 0.34));
  const fromRadial = radialFromCenter(x1, y1, x2 - x1, y2 - y1, centerX, centerY);
  const toRadial = radialFromCenter(x2, y2, x2 - x1, y2 - y1, centerX, centerY);
  const sway = organicSway(x1, y1, x2, y2);
  return {
    c1x: x1 + fromRadial.x * handle + sway.x,
    c1y: y1 + fromRadial.y * handle + sway.y,
    c2x: x2 - toRadial.x * handle - sway.x,
    c2y: y2 - toRadial.y * handle - sway.y
  };
}

function radialFromCenter(x: number, y: number, fallbackX: number, fallbackY: number, centerX: number, centerY: number): NodePosition {
  let dx = x - centerX;
  let dy = y - centerY;
  let length = Math.hypot(dx, dy);
  if (length < 0.001) {
    dx = fallbackX;
    dy = fallbackY;
    length = Math.hypot(dx, dy);
  }
  if (length < 0.001) return { x: 0, y: -1 };
  return { x: dx / length, y: dy / length };
}

function organicSway(x1: number, y1: number, x2: number, y2: number): { x: number; y: number } {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const distance = Math.hypot(dx, dy);
  if (distance < 0.001) return { x: 0, y: 0 };
  const amount = Math.min(traceOrganicSwayMax, Math.max(traceOrganicSwayMin, distance * traceOrganicSwayFactor));
  const sign = organicSwaySign(x1, y1, x2, y2);
  return {
    x: -dy / distance * amount * sign,
    y: dx / distance * amount * sign
  };
}

function organicSwaySign(x1: number, y1: number, x2: number, y2: number): number {
  const hash = Math.trunc(x1 * 31 + y1 * 17 + x2 * 13 + y2 * 7);
  return (hash & 1) === 0 ? 1 : -1;
}

function findNode(name: string): ManipulationNodeModel | undefined {
  return workspace?.tree.nodes.find(node => node.name === name);
}

function allNodes(): ManipulationNodeModel[] {
  return workspace?.tree.nodes ?? [];
}

function graphPointFromPointer(scroller: HTMLElement, event: PointerEvent): NodePosition {
  const rect = scroller.getBoundingClientRect();
  return {
    x: (event.clientX - rect.left + scroller.scrollLeft) / graphZoom,
    y: (event.clientY - rect.top + scroller.scrollTop) / graphZoom
  };
}

function connectionTargetNodeAt(event: PointerEvent): string | null {
  const element = document.elementFromPoint(event.clientX, event.clientY);
  if (!(element instanceof Element)) return null;
  return element.closest<SVGGElement>('.skill-node[data-node]')?.dataset.node ?? null;
}

function updateConnectionPreview(svg: SVGSVGElement, from: NodePosition, to: NodePosition): void {
  let path = svg.querySelector<SVGPathElement>('.connection-preview');
  if (!path) {
    path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('class', 'connection-preview');
    svg.querySelector('.edges')?.append(path);
  }
  path.setAttribute('d', connectionPreviewPath(from, to));
}

function removeConnectionPreview(svg: SVGSVGElement): void {
  svg.querySelector('.connection-preview')?.remove();
}

function connectionPreviewPath(from: NodePosition, to: NodePosition): string {
  const distance = Math.hypot(to.x - from.x, to.y - from.y);
  const handle = Math.max(48, Math.min(150, distance * 0.36));
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  const length = Math.hypot(dx, dy) || 1;
  const nx = dx / length;
  const ny = dy / length;
  return [
    `M ${Math.round(from.x)} ${Math.round(from.y)}`,
    `C ${Math.round(from.x + nx * handle)} ${Math.round(from.y + ny * handle)},`,
    `${Math.round(to.x - nx * handle)} ${Math.round(to.y - ny * handle)},`,
    `${Math.round(to.x)} ${Math.round(to.y)}`
  ].join(' ');
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
