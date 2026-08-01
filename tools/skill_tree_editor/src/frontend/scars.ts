import type { Diagnostic, PreviewResult, ScarTreeNodeModel, ScarTreeWorkspace } from '../shared/types';
import { beginConnectionDrag, finishConnectionDrag, type ConnectionDragState } from './connectionEditing';
import { beginDragPan, shouldStartDragPan, updateDragPan, type DragPanState } from './dragPan';
import { beginNodeDrag, updateNodeDrag, type NodeDragState } from './layoutEditing';
import { createMovementHistory, recordMovement, redoMovement, undoMovement } from './movementHistory';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import './styles.css';

type Tab = 'graph' | 'validation' | 'diff';
const tendencies = ['ANIMUS', 'FLAMMEUS', 'DUCTILIS', 'LUX', 'MORTEM', 'CONGEATIO', 'FERRIC', 'TENEBRIS'];
const center = { x: 480, y: 480 };
let workspace: ScarTreeWorkspace | null = null;
let selectedId = '';
let tab: Tab = 'graph';
let preview: PreviewResult | null = null;
let status = 'Loading scar workspace...';
let busy = false;
let snap = true;
let zoom = .72;
let history = createMovementHistory();
let nodeDrag: NodeDragState | null = null;
let nodeDragOrigin: { x: number; y: number } | null = null;
let panDrag: DragPanState | null = null;
let connectionDrag: ConnectionDragState | null = null;
let connectionSource: { x: number; y: number } | null = null;

const root = document.querySelector<HTMLDivElement>('#app');
if (!root) throw new Error('Missing app root.');

window.addEventListener('keydown', event => {
  if ((event.target as HTMLElement | null)?.matches('input, select, textarea')) return;
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    event.shiftKey ? redo() : undo();
  } else if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'y') {
    event.preventDefault(); redo();
  }
});

void load();

async function load(): Promise<void> {
  busy = true; render();
  try {
    workspace = await api<ScarTreeWorkspace>('/api/scars');
    selectedId = workspace.tree.nodes[0]?.id ?? '';
    preview = null; history = createMovementHistory();
    status = `Loaded ${workspace.tree.nodes.length} scar nodes.`;
  } catch (error) { status = message(error); }
  finally { busy = false; render(); }
}

function render(): void {
  if (!workspace) { root!.innerHTML = `<main class="loading">${escapeHtml(status)}</main>`; return; }
  const viewport = captureViewport();
  root!.innerHTML = `<main class="shell">
    <aside class="sidebar">
      <div class="brand"><span class="brand-mark"></span><div><h1>Scar Tree Editor</h1><p>${escapeHtml(relativeRoot())}</p>
        <p><a href="/workspace.html">Skills</a> - <a href="/manipulations.html">Manipulations</a> - <b>Scars</b> - <a href="/materials.html">Materials</a> - <a href="/recipe_maps.html">Craft/Rites</a></p></div></div>
      <div class="toolbar"><button data-action="reload" ${busy ? 'disabled' : ''}>Reload</button><button data-action="preview" ${busy ? 'disabled' : ''}>Preview</button><button data-action="apply" ${!preview?.canApply || busy ? 'disabled' : ''}>Apply</button></div>
      <div class="branch-list"><div class="form-heading"><div><h2>Scar Families</h2><p>${escapeHtml(workspace.tree.path)}</p></div></div></div>
      <div class="skill-list">${tendencies.map(renderFamily).join('')}</div>
    </aside>
    <section class="main">
      <header class="topbar"><nav class="tabs">${tabButton('graph', 'Layout')}${tabButton('validation', `Validation${badge(diagnostics().length)}`)}${tabButton('diff', `Diff${badge(preview?.diffs.length ?? 0)}`)}</nav>
        <div class="history-controls"><button data-action="undo" ${!history.canUndo ? 'disabled' : ''}>Undo</button><button data-action="redo" ${!history.canRedo ? 'disabled' : ''}>Redo</button></div>
        <label class="snap-toggle"><input type="checkbox" data-action="snap" ${snap ? 'checked' : ''}/> Snap</label><div class="status">${escapeHtml(status)}</div></header>
      <section class="content"><section class="canvas-panel">${renderTab()}</section><aside class="inspector">${renderInspector()}</aside></section>
    </section>
  </main>`;
  bind(); restoreViewport(viewport);
}

function renderFamily(tendency: string): string {
  const nodes = workspace!.tree.nodes.filter(node => node.tendency === tendency).sort((a, b) => a.tier - b.tier || a.displayName.localeCompare(b.displayName));
  if (!nodes.length) return '';
  return `<section class="scar-family"><h3 style="color:${escapeAttr(nodes[0].color)}">${tendency}</h3>${nodes.map(node => `<button class="skill-button ${node.id === selectedId ? 'selected' : ''}" data-node="${escapeAttr(node.id)}"><span>${escapeHtml(node.displayName)}</span><small>Tier ${node.tier}</small></button>`).join('')}</section>`;
}

function renderTab(): string {
  if (tab === 'validation') return renderDiagnostics();
  if (tab === 'diff') return renderDiff();
  return renderGraph();
}

function renderGraph(): string {
  const nodes = workspace!.tree.nodes;
  const byId = new Map(nodes.map(node => [node.id, node]));
  const edges = nodes.flatMap(node => node.parents.map(parent => ({ from: byId.get(parent), to: node }))).filter(edge => edge.from)
    .map(edge => `<path class="edge wire-edge local-edge scar-edge" style="stroke:${escapeAttr(edge.to.color)}" data-edge-from="${escapeAttr(edge.from!.id)}" d="${organicPath(edge.from!, edge.to)}"/>`).join('');
  const renderedNodes = nodes.map(node => `<g class="skill-node ${node.id === selectedId ? 'selected' : ''}" data-node="${escapeAttr(node.id)}" transform="translate(${node.treeX} ${node.treeY})">
    <rect class="node-glow" x="-22" y="-22" width="44" height="44" style="stroke:${escapeAttr(node.color)}"></rect><rect class="node-frame" x="-18" y="-18" width="36" height="36" style="stroke:${escapeAttr(node.color)}"></rect><rect class="node-core" x="-12" y="-12" width="24" height="24"></rect>
    <image href="/asset/item/${encodeURIComponent(node.id.split(':')[1])}.png" x="-11" y="-11" width="22" height="22"></image><text x="0" y="5" class="node-fallback">${escapeHtml(node.displayName[0] ?? '?')}</text><text x="0" y="31" class="node-level">T${node.tier}</text></g>`).join('');
  const scaled = Math.round(960 * zoom);
  return `<div class="graph-shell"><div class="graph-zoom-controls"><button data-action="zoom-out">-</button><button data-action="zoom-reset">${Math.round(zoom * 100)}%</button><button data-action="zoom-in">+</button></div>
    <div class="graph-scroll"><svg class="graph workspace-surface" width="${scaled}" height="${scaled}" viewBox="0 0 960 960">
      <defs><radialGradient id="bloodGlow"><stop offset="0%" stop-color="#240507"/><stop offset="100%" stop-color="#090102"/></radialGradient><pattern id="veinPattern" width="260" height="180" patternUnits="userSpaceOnUse"><path d="M -20 46 C 34 22,84 84,128 42 S 232 18,284 70" class="vein-line vein-line-bold"/><path d="M 28 154 C 86 110,148 178,230 128" class="vein-line"/></pattern></defs>
      <rect width="960" height="960" fill="url(#bloodGlow)"/><rect width="960" height="960" fill="url(#veinPattern)" opacity=".58"/>
      ${renderTendencyStar()}<g class="edges">${edges}</g><g class="nodes">${renderedNodes}</g></svg></div></div>`;
}

function renderTendencyStar(): string {
  return `<g class="scar-tendency-star">${tendencies.map((tendency, index) => {
    const angle = -Math.PI / 2 + index * Math.PI / 4;
    const next = angle + Math.PI / 4;
    const p1 = point(angle, 104), p2 = point(next, 104), tip = point(angle + Math.PI / 8, 174);
    const color = workspace!.tree.nodes.find(node => node.tendency === tendency)?.color ?? '#888';
    const label = point(angle + Math.PI / 8, 202);
    return `<path d="M ${center.x} ${center.y} L ${p1.x} ${p1.y} L ${tip.x} ${tip.y} L ${p2.x} ${p2.y} Z" fill="${escapeAttr(color)}" fill-opacity=".18" stroke="${escapeAttr(color)}" stroke-opacity=".72"/><text x="${label.x}" y="${label.y}" class="degree-guide-label">${tendency}</text>`;
  }).join('')}<circle cx="480" cy="480" r="39" fill="#130506" stroke="#a94237" stroke-width="2"/><circle cx="480" cy="480" r="23" fill="#5b1111" stroke="#e17764"/></g>`;
}

function point(angle: number, radius: number): { x: number; y: number } { return { x: Math.round(center.x + Math.cos(angle) * radius), y: Math.round(center.y + Math.sin(angle) * radius) }; }

function organicPath(from: ScarTreeNodeModel, to: ScarTreeNodeModel): string {
  const dx = to.treeX - from.treeX, dy = to.treeY - from.treeY;
  const length = Math.max(1, Math.hypot(dx, dy)); const nx = -dy / length, ny = dx / length;
  const sway = Math.min(18, Math.max(8, length * .08)) * (hash(`${from.id}>${to.id}`) % 2 ? 1 : -1);
  const x1 = from.treeX + dx * .34 + nx * sway, y1 = from.treeY + dy * .34 + ny * sway;
  const x2 = from.treeX + dx * .68 - nx * sway, y2 = from.treeY + dy * .68 - ny * sway;
  return `M ${from.treeX} ${from.treeY} C ${x1.toFixed(1)} ${y1.toFixed(1)}, ${x2.toFixed(1)} ${y2.toFixed(1)}, ${to.treeX} ${to.treeY}`;
}

function renderInspector(): string {
  const node = findNode(selectedId); if (!node) return '<div class="empty">No scar selected.</div>';
  const candidates = workspace!.tree.nodes.filter(item => item.id !== node.id && !node.parents.includes(item.id)).sort((a, b) => a.displayName.localeCompare(b.displayName));
  const parents = node.parents.length ? node.parents.map(id => `<button class="parent-pill" type="button" data-remove-parent="${escapeAttr(id)}"><span>${escapeHtml(findNode(id)?.displayName ?? id)}</span><b>x</b></button>`).join('') : '<span class="parent-empty">None</span>';
  return `<form class="editor-form"><div class="form-heading"><div><h2>${escapeHtml(node.displayName)}</h2><p>${escapeHtml(node.tendency ?? 'Unaligned')} - Tier ${node.tier}</p></div><div class="icon-chip" style="color:${escapeAttr(node.color)}">${escapeHtml(node.id.split(':')[1])}</div></div>
    <div class="grid2"><label><span>Tree X</span><input type="number" data-edit="treeX" value="${node.treeX}"/></label><label><span>Tree Y</span><input type="number" data-edit="treeY" value="${node.treeY}"/></label></div>
    <label><span>Tendency</span><input value="${escapeAttr(node.tendency ?? 'Unaligned')}" readonly/></label><label><span>Tier</span><input value="${node.tier}" readonly/></label>
    <div class="parent-editor"><span class="field-label">Lineage parents</span><div class="parent-list">${parents}</div><label>Add<select data-add-parent><option value="">Add parent...</option>${candidates.map(item => `<option value="${escapeAttr(item.id)}">${escapeHtml(item.displayName)} (${item.tendency} T${item.tier})</option>`).join('')}</select></label><small>Ctrl-drag a node onto another node to add a lineage link.</small></div></form>`;
}

function bind(): void {
  root!.querySelectorAll<HTMLButtonElement>('button[data-action]').forEach(button => button.addEventListener('click', () => action(button.dataset.action ?? '')));
  root!.querySelectorAll<HTMLButtonElement>('button[data-node]').forEach(button => button.addEventListener('click', () => { selectedId = button.dataset.node ?? ''; render(); }));
  root!.querySelectorAll<HTMLButtonElement>('button[data-tab]').forEach(button => button.addEventListener('click', () => { tab = button.dataset.tab as Tab; render(); }));
  root!.querySelector<HTMLInputElement>('input[data-action="snap"]')?.addEventListener('change', event => { snap = (event.target as HTMLInputElement).checked; });
  root!.querySelectorAll<HTMLInputElement>('input[data-edit]').forEach(input => {
    input.addEventListener('focus', () => { input.dataset.before = input.value; });
    input.addEventListener('input', () => syncPosition(input));
    input.addEventListener('change', () => commitPosition(input));
  });
  root!.querySelectorAll<HTMLButtonElement>('button[data-remove-parent]').forEach(button => button.addEventListener('click', () => { const node = findNode(selectedId); if (node) { node.parents = node.parents.filter(id => id !== button.dataset.removeParent); changed(`Removed lineage parent from ${node.displayName}.`); } }));
  root!.querySelector<HTMLSelectElement>('select[data-add-parent]')?.addEventListener('change', event => { const value = (event.target as HTMLSelectElement).value; const node = findNode(selectedId); if (node && value && !node.parents.includes(value)) { node.parents.push(value); changed(`Added lineage parent to ${node.displayName}.`); } });
  bindGraph();
}

function bindGraph(): void {
  const scroll = root!.querySelector<HTMLDivElement>('.graph-scroll'); if (!scroll) return;
  scroll.addEventListener('wheel', event => { if (!event.deltaY) return; const rect = scroll.getBoundingClientRect(); zoomAt(scroll, event.clientX - rect.left, event.clientY - rect.top, event.deltaY < 0 ? 1.12 : 1 / 1.12); event.preventDefault(); }, { passive: false });
  scroll.addEventListener('pointerdown', event => {
    if (event.button !== 0) return;
    const group = (event.target as Element).closest<SVGGElement>('g[data-node]');
    if (group) {
      const id = group.dataset.node ?? ''; const node = findNode(id); if (!node) return; selectedId = id;
      if (event.ctrlKey || event.metaKey) { connectionDrag = beginConnectionDrag(id); connectionSource = { x: node.treeX, y: node.treeY }; scroll.setPointerCapture(event.pointerId); return; }
      nodeDrag = beginNodeDrag({ clientX: event.clientX, clientY: event.clientY, nodeX: node.treeX, nodeY: node.treeY, scrollLeft: scroll.scrollLeft, scrollTop: scroll.scrollTop, zoom });
      nodeDragOrigin = { x: node.treeX, y: node.treeY }; scroll.setPointerCapture(event.pointerId); scroll.classList.add('dragging-node'); return;
    }
    if (shouldStartDragPan(event.target)) { panDrag = beginDragPan(event.clientX, event.clientY, scroll.scrollLeft, scroll.scrollTop); scroll.setPointerCapture(event.pointerId); scroll.classList.add('panning'); }
  });
  scroll.addEventListener('pointermove', event => {
    if (panDrag) { const next = updateDragPan(panDrag, event.clientX, event.clientY); scroll.scrollLeft = next.scrollLeft; scroll.scrollTop = next.scrollTop; return; }
    if (!nodeDrag) return; const node = findNode(selectedId); if (!node) return;
    const next = updateNodeDrag(nodeDrag, { clientX: event.clientX, clientY: event.clientY, scrollLeft: scroll.scrollLeft, scrollTop: scroll.scrollTop, snap: snap ? 10 : 1, zoom });
    node.treeX = next.x; node.treeY = next.y; const group = scroll.querySelector<SVGGElement>(`g[data-node="${CSS.escape(node.id)}"]`); group?.setAttribute('transform', `translate(${node.treeX} ${node.treeY})`);
  });
  scroll.addEventListener('pointerup', event => {
    if (panDrag) { panDrag = null; scroll.classList.remove('panning'); return; }
    if (connectionDrag) { const target = document.elementFromPoint(event.clientX, event.clientY)?.closest<SVGGElement>('g[data-node]')?.dataset.node; const link = finishConnectionDrag(connectionDrag, target); connectionDrag = null; connectionSource = null; if (link) { const node = findNode(link.field); if (node && !node.parents.includes(link.parentField)) node.parents.push(link.parentField); changed(link ? 'Added scar lineage connection.' : status); } return; }
    if (nodeDrag) { const node = findNode(selectedId); if (node && nodeDragOrigin) recordMovement(history, { field: node.id, before: nodeDragOrigin, after: { x: node.treeX, y: node.treeY } }); nodeDrag = null; nodeDragOrigin = null; changed(node ? `Moved ${node.displayName}.` : status); }
  });
}

function syncPosition(input: HTMLInputElement): void { const node = findNode(selectedId); if (!node) return; const value = Math.round(Number(input.value)); if (!Number.isFinite(value)) return; if (input.dataset.edit === 'treeX') node.treeX = value; else node.treeY = value; preview = null; status = `Updated ${node.displayName}.`; const statusElement = root!.querySelector<HTMLElement>('.status'); if (statusElement) statusElement.textContent = status; }
function commitPosition(input: HTMLInputElement): void { const node = findNode(selectedId); if (!node) return; const prior = Math.round(Number(input.dataset.before)); const before = { x: node.treeX, y: node.treeY }; if (Number.isFinite(prior)) { if (input.dataset.edit === 'treeX') before.x = prior; else before.y = prior; } recordMovement(history, { field: node.id, before, after: { x: node.treeX, y: node.treeY } }); changed(`Updated ${node.displayName}.`); }
function changed(text: string): void { preview = null; status = text; render(); }
function undo(): void { const target = undoMovement(history); applyHistory(target); }
function redo(): void { const target = redoMovement(history); applyHistory(target); }
function applyHistory(target: ReturnType<typeof undoMovement>): void { const update = target?.updates[0]; const node = update?.field ? findNode(update.field) : null; if (node && update?.position) { node.treeX = update.position.x; node.treeY = update.position.y; preview = null; status = `Moved ${node.displayName} to (${node.treeX}, ${node.treeY}).`; render(); } }

function action(name: string): void {
  if (name === 'reload') return void load(); if (name === 'preview') return void runPreview(); if (name === 'apply') return void apply(); if (name === 'undo') return undo(); if (name === 'redo') return redo();
  const scroll = root!.querySelector<HTMLElement>('.graph-scroll'); if (!scroll) return; if (name === 'zoom-in') zoomAt(scroll, scroll.clientWidth / 2, scroll.clientHeight / 2, 1.2); if (name === 'zoom-out') zoomAt(scroll, scroll.clientWidth / 2, scroll.clientHeight / 2, 1 / 1.2); if (name === 'zoom-reset') zoomAt(scroll, scroll.clientWidth / 2, scroll.clientHeight / 2, 1 / zoom);
}

async function runPreview(): Promise<void> { if (!workspace) return; busy = true; status = 'Generating preview...'; render(); try { preview = await api<PreviewResult>('/api/scars/preview', { nodes: workspace.tree.nodes }); tab = 'diff'; status = preview.diffs.length ? `Previewed ${preview.diffs.length} file change.` : 'No changes to preview.'; } catch (error) { status = message(error); } finally { busy = false; render(); } }
async function apply(): Promise<void> { if (!preview?.canApply) return; busy = true; render(); try { await api('/api/apply', { id: preview.id }); status = 'Applied scar layout changes.'; await load(); } catch (error) { status = message(error); busy = false; render(); } }
function zoomAt(scroll: HTMLElement, x: number, y: number, factor: number): void { const next = clampZoom(zoom * factor, .55, 2.75); const anchor = zoomScrollAnchor(zoom, next, x, y, scroll.scrollLeft, scroll.scrollTop); zoom = next; render(); requestAnimationFrame(() => { const current = root!.querySelector<HTMLElement>('.graph-scroll'); if (current) { current.scrollLeft = anchor.scrollLeft; current.scrollTop = anchor.scrollTop; } }); }

function renderDiagnostics(): string { const items = diagnostics(); return items.length ? `<div class="diagnostics">${items.map(diagnostic => `<article class="diagnostic ${diagnostic.severity}"><b>${escapeHtml(diagnostic.code)}</b><span>${escapeHtml(diagnostic.severity)}</span><p>${escapeHtml(diagnostic.message)}</p></article>`).join('')}</div>` : '<div class="empty">No validation issues.</div>'; }
function renderDiff(): string { if (!preview) return '<div class="empty">Click Preview to generate diffs.</div>'; return preview.diffs.length ? `<div class="diffs">${preview.diffs.map(diff => `<section class="diff"><h2>${escapeHtml(diff.path)}</h2><pre>${escapeHtml(diff.patch)}</pre></section>`).join('')}</div>` : '<div class="empty">No changes.</div>'; }
function diagnostics(): Diagnostic[] { return [...(workspace?.diagnostics ?? []), ...(preview?.diagnostics ?? [])].filter((item, index, all) => all.findIndex(other => other.code === item.code && other.skill === item.skill) === index); }
function findNode(id: string): ScarTreeNodeModel | undefined { return workspace?.tree.nodes.find(node => node.id === id); }
function tabButton(key: Tab, label: string): string { return `<button class="${tab === key ? 'active' : ''}" data-tab="${key}">${label}</button>`; }
function badge(count: number): string { return count ? ` <span class="badge">${count}</span>` : ''; }
function captureViewport(): { left: number; top: number } | null { const scroll = root!.querySelector<HTMLElement>('.graph-scroll'); return scroll ? { left: scroll.scrollLeft, top: scroll.scrollTop } : null; }
function restoreViewport(value: { left: number; top: number } | null): void { if (!value) return; const scroll = root!.querySelector<HTMLElement>('.graph-scroll'); if (scroll) { scroll.scrollLeft = value.left; scroll.scrollTop = value.top; } }
function relativeRoot(): string { return workspace?.repoRoot.replaceAll('\\', '/') ?? ''; }
function hash(value: string): number { let result = 0; for (const char of value) result = ((result << 5) - result + char.charCodeAt(0)) | 0; return Math.abs(result); }
function message(value: unknown): string { return value instanceof Error ? value.message : String(value); }
async function api<T = unknown>(path: string, body?: unknown): Promise<T> { const response = await fetch(path, { method: body === undefined ? 'GET' : 'POST', headers: body === undefined ? undefined : { 'content-type': 'application/json' }, body: body === undefined ? undefined : JSON.stringify(body) }); if (!response.ok) throw new Error((await response.json()).error ?? `Request failed: ${response.status}`); return response.json() as Promise<T>; }
function escapeHtml(value: string): string { return value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' })[char]!); }
function escapeAttr(value: string): string { return escapeHtml(value); }
