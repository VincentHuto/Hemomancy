import type {
  Diagnostic,
  IconSource,
  PreviewResult,
  RecipeMapEditorEntry,
  RecipeMapEditorLink,
  RecipeMapEditorTab,
  RecipeMapTabKey,
  RecipeMapWorkspace
} from '../shared/types';
import { beginDragPan, shouldStartDragPan, updateDragPan, type DragPanState } from './dragPan';
import {
  clampRecipeMapCoordinate,
  layoutRecipeMap,
  moveRecipeMapEntries,
  recipeMapContentSize,
  recipeMapDragDelta,
  recipeMapSelectionIdsInRect,
  type RecipeMapGraphNode,
  type RecipeMapGraphLink,
  type RecipeMapLayer
} from './recipeMapGraph';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';

let workspace: RecipeMapWorkspace | null = null;
let activeMap: RecipeMapTabKey = 'RITES';
let activeLayer: RecipeMapLayer = 'surface';
let selectedId = '';
let selectedIds = new Set<string>();
let currentTab: ViewTab = 'graph';
let preview: PreviewResult | null = null;
let statusText = 'Loading Crafting and Rites maps...';
let isBusy = false;
let graphZoom = .82;
let dragPan: DragPanState | null = null;
let nodeDrag: {
  pointerId: number;
  startX: number;
  startY: number;
  nodes: RecipeMapGraphNode[];
  ids: Set<string>;
  originalPositions: Map<string, { treeX?: number; treeY?: number }>;
  moved: boolean;
} | null = null;
let marquee: { pointerId: number; start: Point; current: Point } | null = null;

interface Point { x: number; y: number }
interface GraphViewport { scrollLeft: number; scrollTop: number }

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
    selectedIds = new Set();
    preview = null;
    statusText = `Loaded ${workspace.tabs.reduce((sum, tab) => sum + tab.entries.length, 0)} Crafting and Rites nodes.`;
  } catch (error) {
    statusText = error instanceof Error ? error.message : String(error);
  } finally {
    isBusy = false;
    render();
  }
}

function render(viewport?: GraphViewport): void {
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
  if (viewport) restoreGraphViewport(viewport);
}

function renderView(): string {
  if (currentTab === 'validation') return renderDiagnostics(workspace?.diagnostics ?? []);
  if (currentTab === 'diff') return renderDiffs();
  return renderGraph();
}

function renderGraph(): string {
  const layout = layoutRecipeMap(currentMap(), activeLayer);
  const links = layout.links.map((link, index) => `<path class="recipe-map-link ${link.kind.toLowerCase()}" data-link-index="${index}" d="${recipeLinkPath(link)}"/>`).join('');
  const rings = layout.rings.map(ring => `<circle class="degree-guide-line" cx="480" cy="480" r="${ring.radius}"/><text class="degree-guide-label" x="480" y="${480 - ring.radius + 13}">D${ring.degree}</text>`).join('');
  const nodes = layout.nodes.map(node => {
    const selected = node.id === selectedId || selectedIds.has(node.id) ? 'selected' : '';
    const entry = currentMap().entries.find(candidate => candidate.id === node.id)!;
    const icon = iconAssetUrl(entry);
    return `<g class="material-node recipe-map-node ${selected}" data-entry="${escapeAttr(node.id)}" transform="translate(${node.x} ${node.y})">
      <circle class="node-glow" r="22"></circle><rect class="node-frame" x="-15" y="-15" width="30" height="30"></rect>
      <rect class="node-core" x="-11" y="-11" width="22" height="22"></rect>
      <image href="${escapeAttr(icon)}" x="-9" y="-9" width="18" height="18"></image>
      <text class="recipe-map-node-label" x="0" y="30">${escapeHtml(node.displayName)}</text>
    </g>`;
  }).join('');
  const scaledWidth = Math.round(layout.width * graphZoom);
  const scaledHeight = Math.round(layout.height * graphZoom);
  return `<div class="graph-shell"><div class="graph-zoom-controls" aria-label="Map zoom controls"><button data-action="zoom-out">-</button><button data-action="zoom-reset">${Math.round(graphZoom * 100)}%</button><button data-action="zoom-in">+</button></div><div class="graph-scroll"><svg class="graph" width="${scaledWidth}" height="${scaledHeight}" viewBox="0 0 ${layout.width} ${layout.height}">
    <rect width="100%" height="100%" fill="#090102"></rect><g>${rings}</g><g>${links}</g><g>${nodes}</g>
  </svg></div></div>`;
}

function renderInspector(): string {
  const entry = selectedEntry();
  if (!entry) return `<div class="editor-form"><h2>No node selected</h2>${renderFamilyManager()}</div>`;
  const outgoing = currentMap().links.filter(link => link.from === entry.id);
  const incoming = currentMap().links.filter(link => link.to === entry.id);
  const selectedCount = selectedIds.has(entry.id) ? selectedIds.size : 1;
  const layoutNode = layoutRecipeMap(currentMap(), activeLayer).nodes.find(node => node.id === entry.id);
  return `<div class="editor-form">
    <div class="form-heading"><div><h2>${escapeHtml(entry.displayName)}</h2><p>${escapeHtml(entry.id)}</p></div><span class="icon-chip">${escapeHtml(iconLabel(entry))}</span></div>
    ${selectedCount > 1 ? `<p class="selection-summary">${selectedCount} nodes selected. Drag any selected node to move the group.</p>` : ''}
    <div class="grid2"><label>Tree X<input type="number" step="1" data-entry-edit="treeX" value="${entry.treeX ?? layoutNode?.x ?? 0}"/></label><label>Tree Y<input type="number" step="1" data-entry-edit="treeY" value="${entry.treeY ?? layoutNode?.y ?? 0}"/></label></div>
    <div class="icon-editor"><label>Display icon<select data-entry-edit="iconSource">${iconSourceOptions(entry.iconSource ?? null)}</select></label><label>Icon ${entry.iconSource ? `<select data-entry-edit="iconItem">${iconItemOptions(entry.iconSource, entry.iconItem ?? null)}</select>` : '<select data-entry-edit="iconItem" disabled><option>Recipe result</option></select>'}</label></div>
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
    selectedIds = new Set();
    preview = null;
    render();
  }));
  app.querySelectorAll<HTMLElement>('[data-layer]').forEach(element => element.addEventListener('click', () => {
    activeLayer = element.dataset.layer as RecipeMapLayer;
    ensureSelection();
    selectedIds = new Set();
    render();
  }));
  app.querySelectorAll<HTMLElement>('[data-view]').forEach(element => element.addEventListener('click', () => {
    currentTab = element.dataset.view as ViewTab;
    render();
  }));
  app.querySelectorAll<HTMLElement>('.skill-list [data-entry]').forEach(element => element.addEventListener('click', () => {
    selectedId = element.dataset.entry ?? '';
    selectedIds = new Set();
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
  app.querySelectorAll<HTMLInputElement>('[data-entry-edit="treeX"], [data-entry-edit="treeY"]').forEach(input => input.addEventListener('change', event => {
    const entry = selectedEntry();
    if (!entry) return;
    const position = layoutRecipeMap(currentMap(), activeLayer).nodes.find(node => node.id === entry.id);
    if (!Number.isFinite(entry.treeX)) entry.treeX = position?.x ?? 480;
    if (!Number.isFinite(entry.treeY)) entry.treeY = position?.y ?? 480;
    const value = clampRecipeMapCoordinate(Number((event.target as HTMLInputElement).value) || 0);
    if ((event.target as HTMLInputElement).dataset.entryEdit === 'treeX') entry.treeX = value;
    else entry.treeY = value;
    changed('Updated node position.');
  }));
  app.querySelector<HTMLSelectElement>('[data-entry-edit="iconSource"]')?.addEventListener('change', event => {
    const entry = selectedEntry();
    if (!entry) return;
    const value = (event.target as HTMLSelectElement).value;
    entry.iconSource = value === 'item' || value === 'block' ? value : null;
    if (!entry.iconSource) entry.iconItem = null;
    else if (!iconOptionsForSource(entry.iconSource).includes(entry.iconItem ?? '')) entry.iconItem = iconOptionsForSource(entry.iconSource)[0] ?? null;
    changed('Updated node display icon source.');
  });
  app.querySelector<HTMLSelectElement>('[data-entry-edit="iconItem"]')?.addEventListener('change', event => {
    const entry = selectedEntry();
    if (!entry) return;
    entry.iconItem = (event.target as HTMLSelectElement).value || null;
    changed('Updated node display icon.');
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
  app.querySelector('[data-action="zoom-in"]')?.addEventListener('click', () => zoomFromCenter(1.15));
  app.querySelector('[data-action="zoom-out"]')?.addEventListener('click', () => zoomFromCenter(1 / 1.15));
  app.querySelector('[data-action="zoom-reset"]')?.addEventListener('click', () => zoomFromCenter(1 / graphZoom));
  wireGraphEvents();
}

function wireGraphEvents(): void {
  const scroller = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (!scroller) return;
  scroller.addEventListener('wheel', event => {
    if (!event.deltaY) return;
    const rect = scroller.getBoundingClientRect();
    zoomGraphAt(scroller, event.clientX - rect.left, event.clientY - rect.top, event.deltaY < 0 ? 1.12 : 1 / 1.12);
    event.preventDefault();
  }, { passive: false });
  scroller.addEventListener('pointerdown', event => {
    if (event.button !== 0) return;
    const target = event.target as Element | null;
    const node = target?.closest<SVGGElement>('.recipe-map-node');
    if (node) {
      const id = node.dataset.entry ?? '';
      if (!id) return;
      selectedId = id;
      if (event.ctrlKey || event.metaKey) {
        if (selectedIds.has(id)) selectedIds.delete(id); else selectedIds.add(id);
        if (!selectedIds.size) selectedIds.add(id);
        statusText = `${selectedIds.size} node${selectedIds.size === 1 ? '' : 's'} selected.`;
        render(captureGraphViewport(scroller));
        return;
      }
      if (!selectedIds.has(id)) selectedIds = new Set([id]);
      const layout = layoutRecipeMap(currentMap(), activeLayer);
      nodeDrag = {
        pointerId: event.pointerId,
        startX: event.clientX,
        startY: event.clientY,
        nodes: layout.nodes.map(candidate => ({ ...candidate })),
        ids: new Set(selectedIds),
        originalPositions: new Map(currentMap().entries.filter(entry => selectedIds.has(entry.id))
          .map(entry => [entry.id, { treeX: entry.treeX, treeY: entry.treeY }])),
        moved: false
      };
      scroller.setPointerCapture(event.pointerId);
      scroller.classList.add('dragging-node');
      event.preventDefault();
      return;
    }
    if (event.shiftKey && target?.closest('.graph')) {
      const start = graphPointFromPointer(scroller, event);
      marquee = { pointerId: event.pointerId, start, current: start };
      updateMarquee(scroller);
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
  });
  scroller.addEventListener('pointermove', event => {
    if (dragPan) {
      const next = updateDragPan(dragPan, event.clientX, event.clientY);
      scroller.scrollLeft = next.scrollLeft;
      scroller.scrollTop = next.scrollTop;
      return;
    }
    if (marquee && marquee.pointerId === event.pointerId) {
      marquee.current = graphPointFromPointer(scroller, event);
      updateMarquee(scroller);
      event.preventDefault();
      return;
    }
    if (!nodeDrag || nodeDrag.pointerId !== event.pointerId) return;
    const delta = recipeMapDragDelta(
      { x: nodeDrag.startX, y: nodeDrag.startY },
      { x: event.clientX, y: event.clientY },
      graphZoom
    );
    if (!delta) return;
    nodeDrag.moved = true;
    moveRecipeMapEntries(currentMap().entries, nodeDrag.nodes, nodeDrag.ids, delta.x, delta.y);
    preview = null;
    updateGraphDom();
    event.preventDefault();
  });
  const finish = (event: PointerEvent): void => {
    const viewport = captureGraphViewport(scroller);
    if (dragPan) {
      dragPan = null;
      scroller.classList.remove('panning');
      return;
    }
    if (marquee && marquee.pointerId === event.pointerId) {
      marquee.current = graphPointFromPointer(scroller, event);
      const ids = recipeMapSelectionIdsInRect(layoutRecipeMap(currentMap(), activeLayer).nodes, marquee.start, marquee.current);
      selectedIds = new Set(ids);
      selectedId = ids[0] ?? selectedId;
      statusText = `${ids.length} node${ids.length === 1 ? '' : 's'} selected.`;
      marquee = null;
      scroller.classList.remove('marquee-selecting');
      render(viewport);
      return;
    }
    if (!nodeDrag || nodeDrag.pointerId !== event.pointerId) return;
    statusText = nodeDrag.moved
      ? `Moved ${nodeDrag.ids.size} node${nodeDrag.ids.size === 1 ? '' : 's'}.`
      : `${nodeDrag.ids.size} node${nodeDrag.ids.size === 1 ? '' : 's'} selected.`;
    nodeDrag = null;
    scroller.classList.remove('dragging-node');
    render(viewport);
  };
  scroller.addEventListener('pointerup', finish);
  scroller.addEventListener('pointercancel', event => {
    if (nodeDrag && nodeDrag.pointerId === event.pointerId) {
      for (const entry of currentMap().entries) {
        const original = nodeDrag.originalPositions.get(entry.id);
        if (!original) continue;
        entry.treeX = original.treeX;
        entry.treeY = original.treeY;
      }
      nodeDrag = null;
      scroller.classList.remove('dragging-node');
      render(captureGraphViewport(scroller));
      return;
    }
    finish(event);
  });
}

function graphPointFromPointer(scroller: HTMLElement, event: PointerEvent): Point {
  const rect = scroller.getBoundingClientRect();
  return {
    x: (event.clientX - rect.left + scroller.scrollLeft) / graphZoom,
    y: (event.clientY - rect.top + scroller.scrollTop) / graphZoom
  };
}

function updateMarquee(scroller: HTMLElement): void {
  if (!marquee) return;
  const svg = scroller.querySelector<SVGSVGElement>('svg.graph');
  if (!svg) return;
  let rect = svg.querySelector<SVGRectElement>('.marquee-selection');
  if (!rect) {
    rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    rect.setAttribute('class', 'marquee-selection');
    svg.appendChild(rect);
  }
  rect.setAttribute('x', String(Math.min(marquee.start.x, marquee.current.x)));
  rect.setAttribute('y', String(Math.min(marquee.start.y, marquee.current.y)));
  rect.setAttribute('width', String(Math.abs(marquee.current.x - marquee.start.x)));
  rect.setAttribute('height', String(Math.abs(marquee.current.y - marquee.start.y)));
}

function updateGraphDom(): void {
  const layout = layoutRecipeMap(currentMap(), activeLayer);
  for (const node of layout.nodes) {
    app.querySelector<SVGGElement>(`.recipe-map-node[data-entry="${cssEscape(node.id)}"]`)
      ?.setAttribute('transform', `translate(${node.x} ${node.y})`);
  }
  layout.links.forEach((link, index) => app.querySelector<SVGPathElement>(`[data-link-index="${index}"]`)
    ?.setAttribute('d', recipeLinkPath(link)));
}

function zoomFromCenter(factor: number): void {
  const scroller = app.querySelector<HTMLDivElement>('.graph-scroll');
  if (scroller) zoomGraphAt(scroller, scroller.clientWidth / 2, scroller.clientHeight / 2, factor);
}

function zoomGraphAt(scroller: HTMLElement, viewportX: number, viewportY: number, factor: number): void {
  const nextZoom = clampZoom(graphZoom * factor, .35, 2);
  if (nextZoom === graphZoom) return;
  const next = zoomScrollAnchor(scroller.scrollLeft, scroller.scrollTop, viewportX, viewportY, graphZoom, nextZoom);
  graphZoom = nextZoom;
  const svg = scroller.querySelector<SVGSVGElement>('svg.graph');
  if (svg) {
    svg.setAttribute('width', String(Math.round(recipeMapContentSize * graphZoom)));
    svg.setAttribute('height', String(Math.round(recipeMapContentSize * graphZoom)));
  }
  scroller.scrollLeft = next.scrollLeft;
  scroller.scrollTop = next.scrollTop;
  const reset = app.querySelector<HTMLButtonElement>('[data-action="zoom-reset"]');
  if (reset) reset.textContent = `${Math.round(graphZoom * 100)}%`;
}

function captureGraphViewport(scroller: HTMLElement): GraphViewport {
  return { scrollLeft: scroller.scrollLeft, scrollTop: scroller.scrollTop };
}

function captureCurrentGraphViewport(): GraphViewport | undefined {
  const scroller = app.querySelector<HTMLElement>('.graph-scroll');
  return scroller ? captureGraphViewport(scroller) : undefined;
}

function restoreGraphViewport(viewport: GraphViewport): void {
  const scroller = app.querySelector<HTMLElement>('.graph-scroll');
  if (scroller) {
    scroller.scrollLeft = viewport.scrollLeft;
    scroller.scrollTop = viewport.scrollTop;
  }
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

function changed(message: string): void { const viewport = captureCurrentGraphViewport(); preview = null; currentTab = 'graph'; statusText = message; render(viewport); }
function currentMap(): RecipeMapEditorTab { return workspace!.tabs.find(tab => tab.key === activeMap) ?? workspace!.tabs[0]; }
function selectedEntry(): RecipeMapEditorEntry | undefined { return currentMap().entries.find(entry => entry.id === selectedId); }
function ensureSelection(): void {
  const visible = currentMap().entries.filter(entry => activeLayer === 'surface' ? entry.column <= 4 : entry.column >= 5);
  if (!visible.some(entry => entry.id === selectedId)) selectedId = visible[0]?.id ?? currentMap().entries[0]?.id ?? '';
  const visibleIds = new Set(visible.map(entry => entry.id));
  selectedIds = new Set([...selectedIds].filter(id => visibleIds.has(id)));
}
function nextOrder(family: string, excluding: string): number { return currentMap().entries.filter(entry => entry.family === family && entry.id !== excluding).reduce((max, entry) => Math.max(max, entry.order + 1), 0); }
function displayName(id: string): string { return currentMap().entries.find(entry => entry.id === id)?.displayName ?? id; }
function linkKey(link: RecipeMapEditorLink): string { return `${link.from}|${link.to}|${link.kind}`; }
function mapButton(tab: RecipeMapEditorTab): string { return `<button class="branch-button ${tab.key === activeMap ? 'selected' : ''}" data-map="${tab.key}"><span>${tab.key === 'RITES' ? 'Rites' : 'Crafting'}</span><b>${tab.entries.length}</b></button>`; }
function familySection(family: string): string { const entries = currentMap().entries.filter(entry => entry.family === family).sort((a, b) => a.order - b.order); return entries.length ? `<p class="recipe-map-family-heading">${escapeHtml(family)}</p>${entries.map(entry => `<button class="skill-button ${entry.id === selectedId || selectedIds.has(entry.id) ? 'selected' : ''}" data-entry="${escapeAttr(entry.id)}"><span>${escapeHtml(entry.displayName)}</span><small>D${entry.column}</small></button>`).join('')}` : ''; }
function viewButton(tab: ViewTab, label: string): string { return `<button class="${currentTab === tab ? 'active' : ''}" data-view="${tab}">${label}</button>`; }
function badge(count: number): string { return count ? ` <span class="badge">${count}</span>` : ''; }
function renderDiagnostics(diagnostics: Diagnostic[]): string { return `<div class="diagnostics">${diagnostics.length ? diagnostics.map(diagnostic => `<article class="diagnostic ${diagnostic.severity}"><b>${escapeHtml(diagnostic.severity.toUpperCase())}</b><span>${escapeHtml(diagnostic.code)}</span><p>${escapeHtml(diagnostic.message)}</p></article>`).join('') : '<div class="empty">No diagnostics.</div>'}</div>`; }
function renderDiffs(): string { return `<div class="diffs">${preview?.diffs.length ? preview.diffs.map(diff => `<article class="diff"><h2>${escapeHtml(diff.path)}</h2><pre>${escapeHtml(diff.patch)}</pre></article>`).join('') : '<div class="empty">Preview changes to inspect a diff.</div>'}</div>`; }
function relativeRoot(): string { return workspace?.repoRoot.split(/[\\/]/).slice(-3).join('/') ?? ''; }
async function api<T = unknown>(path: string, init?: RequestInit): Promise<T> { const response = await fetch(path, { ...init, headers: { 'content-type': 'application/json', ...(init?.headers ?? {}) } }); const json = await response.json(); if (!response.ok) throw new Error(json.error ?? response.statusText); return json as T; }
function escapeHtml(value: string): string { return value.replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character] ?? character)); }
function escapeAttr(value: string): string { return escapeHtml(value); }
function iconSourceOptions(current: IconSource): string {
  return [
    ['', 'Recipe result'],
    ['item', 'ItemInit item'],
    ['block', 'BlockInit block']
  ].map(([value, label]) => `<option value="${value}" ${value === (current ?? '') ? 'selected' : ''}>${label}</option>`).join('');
}
function iconOptionsForSource(source: Exclude<IconSource, null>): string[] {
  return source === 'block' ? workspace?.iconOptions.blocks ?? [] : workspace?.iconOptions.items ?? [];
}
function iconItemOptions(source: Exclude<IconSource, null>, selected: string | null): string {
  const options = iconOptionsForSource(source);
  const values = selected && !options.includes(selected) ? [selected, ...options] : options;
  return values.map(value => `<option value="${escapeAttr(value)}" ${value === selected ? 'selected' : ''}>${escapeHtml(value)}</option>`).join('');
}
function iconAssetUrl(entry: RecipeMapEditorEntry): string {
  const sourceType = entry.iconSource === 'block' ? 'block' : 'item';
  const icon = entry.iconItem ?? entry.resultIcon ?? entry.id.substring(entry.id.lastIndexOf('/') + 1);
  return `/asset/${sourceType}/${encodeURIComponent(icon)}.png`;
}
function iconLabel(entry: RecipeMapEditorEntry): string {
  return entry.iconSource && entry.iconItem ? `${entry.iconSource}:${entry.iconItem}` : `Degree ${entry.column} · recipe result`;
}
function cssEscape(value: string): string { return globalThis.CSS?.escape ? globalThis.CSS.escape(value) : value.replace(/["\\]/g, '\\$&'); }
function recipeLinkPath(link: RecipeMapGraphLink): string { return `M ${link.fromX} ${link.fromY} C ${(link.fromX + link.toX) / 2} ${link.fromY}, ${(link.fromX + link.toX) / 2} ${link.toY}, ${link.toX} ${link.toY}`; }
