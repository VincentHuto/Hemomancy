import type { DialogueFile, DialogueNodeModel, DialogueOptionModel, DialogueTreeModel } from '../shared/types';
import { currentFile, graphKey, optionMeta, state, translation } from './state';

const CARD_WIDTH = 260;
const HEADER_H = 38;
const SECTION_LABEL_H = 32;
const OPTION_ROW_H = 34;
const BOTTOM_ROW_H = 36;
const COL_WIDTH = 330;
const NODE_V_STEP = 260;
const NODE_START_X = 30;
const TREE_BANNER_H = 36;
const TREE_GAP = 80;

type NodePos = { x: number; y: number };

let dragState: {
  treeMethod: string;
  nodeId: string;
  startX: number;
  startY: number;
  originX: number;
  originY: number;
  moved: boolean;
} | null = null;

let suppressNextCardClick = false;

function escapeHtml(v: string): string {
  return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(v: string): string {
  return escapeHtml(v).replace(/"/g, '&quot;');
}

function truncate(v: string, len: number): string {
  return v.length > len ? v.slice(0, len - 1) + '…' : v;
}

function treeHeight(tree: DialogueTreeModel): number {
  const depth = new Map<string, number>();
  const rowsByDepth = new Map<number, number>();
  const queue = [{ id: tree.startNode ?? tree.nodes[0]?.id ?? '', d: 0 }];
  while (queue.length) {
    const { id, d } = queue.shift()!;
    if (!id || depth.has(id)) continue;
    depth.set(id, d);
    rowsByDepth.set(d, (rowsByDepth.get(d) ?? 0) + 1);
    tree.nodes.find(n => n.id === id)?.options.forEach(o => {
      if (o.next) queue.push({ id: o.next, d: d + 1 });
    });
  }
  tree.nodes.forEach(n => { if (!depth.has(n.id)) { depth.set(n.id, 0); rowsByDepth.set(0, (rowsByDepth.get(0) ?? 0) + 1); } });
  const maxRows = Math.max(1, ...rowsByDepth.values());
  return maxRows * NODE_V_STEP;
}

function bfsPositions(tree: DialogueTreeModel, yOrigin: number): Map<string, NodePos> {
  const depth = new Map<string, number>();
  const queue = [{ id: tree.startNode ?? tree.nodes[0]?.id ?? '', d: 0 }];
  while (queue.length) {
    const { id, d } = queue.shift()!;
    if (!id || depth.has(id)) continue;
    depth.set(id, d);
    tree.nodes.find(n => n.id === id)?.options.forEach(o => {
      if (o.next) queue.push({ id: o.next, d: d + 1 });
    });
  }
  tree.nodes.forEach(n => { if (!depth.has(n.id)) depth.set(n.id, 0); });
  const rowCount = new Map<number, number>();
  const positions = new Map<string, NodePos>();
  depth.forEach((d, id) => {
    const row = rowCount.get(d) ?? 0;
    rowCount.set(d, row + 1);
    positions.set(id, { x: NODE_START_X + d * COL_WIDTH, y: yOrigin + row * NODE_V_STEP });
  });
  return positions;
}

function computePositions(file: DialogueFile, tree: DialogueTreeModel, yOrigin: number): Map<string, NodePos> {
  const auto = bfsPositions(tree, yOrigin);
  const manual = state.graphPositions[graphKey(file, tree)] ?? {};
  for (const [id, pos] of Object.entries(manual)) auto.set(id, pos);
  return auto;
}

function inputPortPos(pos: NodePos): NodePos {
  return { x: pos.x, y: pos.y + HEADER_H / 2 };
}

function outputPortPos(pos: NodePos, optionIndex: number): NodePos {
  return {
    x: pos.x + CARD_WIDTH,
    y: pos.y + HEADER_H + SECTION_LABEL_H + SECTION_LABEL_H + (optionIndex + 0.5) * OPTION_ROW_H
  };
}

function bezierPath(from: NodePos, to: NodePos): string {
  const dx = Math.max(60, Math.abs(to.x - from.x) * 0.45);
  return `M ${from.x} ${from.y} C ${from.x + dx} ${from.y} ${to.x - dx} ${to.y} ${to.x} ${to.y}`;
}

function renderEdges(
  tree: DialogueTreeModel,
  positions: Map<string, NodePos>
): string {
  return tree.nodes.flatMap(node =>
    node.options.map((option, i) => {
      if (!option.next || !positions.has(option.next)) return '';
      const from = outputPortPos(positions.get(node.id)!, i);
      const to = inputPortPos(positions.get(option.next)!);
      return `<path d="${bezierPath(from, to)}" class="edge-path"/>`;
    })
  ).join('');
}

function isActive(treeMethod: string, nodeId: string): boolean {
  return state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.nodeId === nodeId;
}

function isOptionActive(treeMethod: string, nodeId: string, optionIndex: number): boolean {
  const row = state.selectedRow;
  return row?.treeMethod === treeMethod && row?.nodeId === nodeId && row.section === 'option' && (row as { optionIndex: number }).optionIndex === optionIndex;
}

function optionLabel(option: DialogueOptionModel): string {
  return translation(option.text) || option.text.split('.').at(-1) || option.text;
}

function renderNodeCard(node: DialogueNodeModel, pos: NodePos, treeMethod: string): string {
  const active = isActive(treeMethod, node.id);
  return `<div class="card-node ${active ? 'active' : ''}" data-node-card="${escapeAttr(node.id)}" data-tree-method="${escapeAttr(treeMethod)}" style="left:${pos.x}px;top:${pos.y}px">
    <div class="card-header" data-drag-node="${escapeAttr(node.id)}" data-drag-tree="${escapeAttr(treeMethod)}">
      <div class="port-in"></div>
      <span class="card-title">${escapeHtml(node.id)}</span>
    </div>
    <div class="card-section-row ${state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.section === 'lines' ? 'active' : ''}" data-select-section="lines" data-section-node="${escapeAttr(node.id)}" data-section-tree="${escapeAttr(treeMethod)}">
      <span>Lines</span>
      <span class="count">${node.lines.length}</span>
    </div>
    <div class="card-options-section">
      <div class="card-section-label">Options</div>
      ${node.options.map((option, i) => {
        const label = truncate(optionLabel(option), 28);
        const isOpt = isOptionActive(treeMethod, node.id, i);
        return `<div class="option-row ${isOpt ? 'active' : ''}" data-select-option="${i}" data-opt-node="${escapeAttr(node.id)}" data-opt-tree="${escapeAttr(treeMethod)}">
          <span class="option-text">${escapeHtml(label)}</span>
          <div class="port-out"></div>
        </div>`;
      }).join('')}
    </div>
    <div class="card-section-row ${state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod && state.selectedRow?.section === 'triggers' ? 'active' : ''}" data-select-section="triggers" data-section-node="${escapeAttr(node.id)}" data-section-tree="${escapeAttr(treeMethod)}">
      <span>Triggers</span>
    </div>
    <div class="card-add-row" data-add-node-tree="${escapeAttr(treeMethod)}">+</div>
  </div>`;
}

function renderTreeBanner(tree: DialogueTreeModel, y: number): string {
  return `<div class="tree-banner" style="top:${y}px">
    <span class="tree-banner-method">${escapeHtml(tree.method)}</span>
    <span class="count">${tree.visibility}</span>
    <span class="count">${tree.nodes.length} nodes</span>
    ${tree.dispatchOnly ? '<span class="count">dispatch</span>' : ''}
  </div>`;
}

export function renderGraph(el: HTMLElement, onRender: () => void): void {
  const file = currentFile();
  if (!file) {
    el.innerHTML = '<div class="empty">No file selected.</div>';
    return;
  }

  let currentY = 0;
  const treeLayouts: Array<{ tree: DialogueTreeModel; positions: Map<string, NodePos>; bannerY: number }> = [];

  for (const tree of file.trees) {
    const bannerY = currentY;
    const originY = currentY + TREE_BANNER_H;
    const positions = computePositions(file, tree, originY);
    treeLayouts.push({ tree, positions, bannerY });
    currentY = originY + treeHeight(tree) + TREE_GAP;
  }

  const totalHeight = currentY;
  const totalWidth = Math.max(920, NODE_START_X + 10 * COL_WIDTH);

  const svgEdges = treeLayouts.map(({ tree, positions }) =>
    tree.dispatchOnly ? '' : renderEdges(tree, positions)
  ).join('');

  const cards = treeLayouts.map(({ tree, positions, bannerY }) =>
    renderTreeBanner(tree, bannerY) +
    (tree.dispatchOnly ? '' : [...positions.entries()].map(([nodeId]) => {
      const node = tree.nodes.find(n => n.id === nodeId);
      return node ? renderNodeCard(node, positions.get(nodeId)!, tree.method) : '';
    }).join(''))
  ).join('');

  el.innerHTML = `<div class="graph" style="min-height:${totalHeight}px;min-width:${totalWidth}px;position:relative">
    <svg class="edges" width="${totalWidth}" height="${totalHeight}"><defs><linearGradient id="edge-gradient" x1="0%" y1="0%" x2="100%" y2="0%"><stop offset="0%" style="stop-color:var(--edge-a,#b84a43);stop-opacity:0.7"/><stop offset="100%" style="stop-color:var(--edge-b,#d08a60);stop-opacity:0.9"/></linearGradient></defs>${svgEdges}</svg>
    ${cards}
  </div>`;

  bindGraphEvents(el, file, treeLayouts, onRender);
}

function bindGraphEvents(
  el: HTMLElement,
  file: DialogueFile,
  treeLayouts: Array<{ tree: DialogueTreeModel; positions: Map<string, NodePos>; bannerY: number }>,
  onRender: () => void
): void {
  el.querySelectorAll<HTMLElement>('[data-select-section]').forEach(row => {
    row.onclick = e => {
      e.stopPropagation();
      const section = row.dataset.selectSection as 'lines' | 'triggers';
      const nodeId = row.dataset.sectionNode!;
      const treeMethod = row.dataset.sectionTree!;
      state.selectedRow = { treeMethod, nodeId, section };
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-select-option]').forEach(row => {
    row.onclick = e => {
      e.stopPropagation();
      if (suppressNextCardClick) { suppressNextCardClick = false; return; }
      const optionIndex = Number(row.dataset.selectOption);
      const nodeId = row.dataset.optNode!;
      const treeMethod = row.dataset.optTree!;
      state.selectedRow = { treeMethod, nodeId, section: 'option', optionIndex };
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-add-node-tree]').forEach(btn => {
    btn.onclick = e => {
      e.stopPropagation();
      const treeMethod = btn.dataset.addNodeTree!;
      const tree = file.trees.find(t => t.method === treeMethod);
      if (!tree) return;
      const newId = `node_${tree.nodes.length + 1}`;
      tree.nodes.push({ id: newId, lines: [], options: [] });
      state.selectedRow = { treeMethod, nodeId: newId, section: 'lines' };
      state.preview = null;
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-drag-node]').forEach(header => {
    header.onmousedown = e => {
      const nodeId = header.dataset.dragNode!;
      const treeMethod = header.dataset.dragTree!;
      const layout = treeLayouts.find(l => l.tree.method === treeMethod);
      const pos = layout?.positions.get(nodeId);
      if (!pos) return;
      e.preventDefault();
      dragState = { treeMethod, nodeId, startX: e.clientX, startY: e.clientY, originX: pos.x, originY: pos.y, moved: false };
    };
  });
}

document.addEventListener('mousemove', e => {
  if (!dragState) return;
  const file = currentFile();
  if (!file) return;
  const dx = e.clientX - dragState.startX;
  const dy = e.clientY - dragState.startY;
  if (Math.abs(dx) > 2 || Math.abs(dy) > 2) dragState.moved = true;
  const tree = file.trees.find(t => t.method === dragState!.treeMethod);
  if (!tree) return;
  const key = graphKey(file, tree);
  state.graphPositions[key] = {
    ...(state.graphPositions[key] ?? {}),
    [dragState.nodeId]: { x: Math.max(0, dragState.originX + dx), y: Math.max(0, dragState.originY + dy) }
  };
  document.dispatchEvent(new CustomEvent('graph-drag-render'));
});

document.addEventListener('mouseup', () => {
  if (!dragState) return;
  suppressNextCardClick = dragState.moved;
  dragState = null;
});
