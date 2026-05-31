import type {
  Diagnostic,
  PreviewResult,
  SkillBranchFile,
  SkillModel,
  SkillWorkspace
} from '../shared/types';
import { applyCompassLayout } from './compassLayout';
import { beginConnectionDrag, type ConnectionDragState, finishConnectionDrag } from './connectionEditing';
import { beginDragPan, type DragPanState, shouldStartDragPan, updateDragPan } from './dragPan';
import { computeGraphLayout, type GraphEdge } from './graphLayout';
import { beginNodeDrag, type NodeDragState, type NodePosition, updateNodeDrag } from './layoutEditing';
import {
  createMovementHistory,
  type MovementTarget,
  recordMovement,
  recordMovements,
  recordSkillEdit,
  redoMovement,
  type SkillEditValue,
  undoMovement
} from './movementHistory';
import { clampZoom, zoomScrollAnchor } from './viewportZoom';
import './styles.css';

type ViewTab = 'graph' | 'validation' | 'diff';
type EditableSkillKey = keyof SkillModel;

const graphMinZoom = 0.55;
const graphMaxZoom = 2.75;
const wheelZoomStep = 1.12;
const buttonZoomStep = 1.2;

let workspace: SkillWorkspace | null = null;
let selectedField = '';
let selectedBranch = '';
let currentTab: ViewTab = 'graph';
let preview: PreviewResult | null = null;
let statusText = 'Loading workspace...';
let isBusy = false;
let snapToGrid = true;
let graphZoom = 1;
let movementHistory = createMovementHistory();

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
    workspace = await api<SkillWorkspace>('/api/workspace');
    selectedBranch = workspace.branches[0]?.branch ?? '';
    selectedField = workspace.branches[0]?.skills[0]?.field ?? '';
    movementHistory = createMovementHistory();
    preview = null;
    statusText = `Loaded ${allSkills().length} skills from ${workspace.branches.length} Java branches.`;
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
          </div>
        </div>
        <div class="toolbar">
          <button data-action="reload" ${isBusy ? 'disabled' : ''}>Reload</button>
          <button data-action="preview" ${isBusy ? 'disabled' : ''}>Preview</button>
          <button data-action="apply" ${!preview?.canApply || isBusy ? 'disabled' : ''}>Apply</button>
        </div>
        <div class="branch-list">
          ${workspace.branches.map(renderBranchButton).join('')}
        </div>
        <div class="skill-list">
          ${currentBranch()?.skills.map(renderSkillButton).join('') ?? ''}
        </div>
      </aside>

      <section class="main">
        <header class="topbar">
          <nav class="tabs">
            ${tabButton('graph', 'Layout')}
            ${tabButton('validation', `Validation ${workspace.diagnostics.length ? `(${workspace.diagnostics.length})` : ''}`)}
            ${tabButton('diff', `Diff ${preview ? `(${preview.diffs.length})` : ''}`)}
          </nav>
          <div class="history-controls" aria-label="Movement history controls">
            <button data-action="compass-layout" title="Arrange skills into compass rings" ${isBusy ? 'disabled' : ''}>Compass Auto-Layout</button>
            <button data-action="undo-move" title="Undo move (Ctrl+Z)" ${!movementHistory.canUndo || isBusy ? 'disabled' : ''}>Undo</button>
            <button data-action="redo-move" title="Redo move (Ctrl+Y or Ctrl+Shift+Z)" ${!movementHistory.canRedo || isBusy ? 'disabled' : ''}>Redo</button>
          </div>
          <label class="snap-toggle"><input type="checkbox" data-action="snap-grid" ${snapToGrid ? 'checked' : ''} /> Snap</label>
          <div class="status">${escapeHtml(statusText)}</div>
        </header>
        <section class="content">
          <section class="canvas-panel">
            ${currentTab === 'graph' ? renderGraph() : ''}
            ${currentTab === 'validation' ? renderDiagnostics(workspace.diagnostics) : ''}
            ${currentTab === 'diff' ? renderDiffs() : ''}
          </section>
          <aside class="inspector">
            ${renderInspector()}
          </aside>
        </section>
      </section>
    </main>
  `;

  wireEvents();
}

function renderBranchButton(branch: SkillBranchFile): string {
  const selected = branch.branch === selectedBranch ? 'selected' : '';
  return `<button class="branch-button ${selected} branch-button-${branchClassName(branch.branch)}" data-branch="${escapeAttr(branch.branch)}">
    <span>${escapeHtml(labelize(branch.branch))}</span>
    <b>${branch.skills.length}</b>
  </button>`;
}

function renderSkillButton(skill: SkillModel): string {
  const selected = skill.field === selectedField ? 'selected' : '';
  return `<button class="skill-button ${selected}" data-skill="${escapeAttr(skill.field)}">
    <span>${escapeHtml(skill.name)}</span>
    <small>${skill.id}</small>
  </button>`;
}

function tabButton(tab: ViewTab, label: string): string {
  return `<button class="${currentTab === tab ? 'active' : ''}" data-tab="${tab}">${escapeHtml(label)}</button>`;
}

function renderGraph(): string {
  const layout = computeGraphLayout(workspace!.branches);
  const scaledWidth = Math.round(layout.width * graphZoom);
  const scaledHeight = Math.round(layout.height * graphZoom);
  const edges = layout.edges
    .map(renderWireEdge)
    .join('');
  const degreeGuides = layout.degreeGuides.map(guide =>
    `<circle cx="${guide.cx}" cy="${guide.cy}" r="${guide.radius}" class="degree-guide-line"></circle>
    <text x="${guide.labelX}" y="${guide.labelY}" class="degree-guide-label">${escapeHtml(guide.label)}</text>`
  ).join('');
  const nodes = layout.nodes.map(renderSkillNode).join('');
  const zoomLabel = `${Math.round(graphZoom * 100)}%`;

  return `<div class="graph-shell">
    <div class="graph-zoom-controls" aria-label="Graph zoom controls">
      <button data-action="zoom-out" title="Zoom out">-</button>
      <button data-action="zoom-reset" title="Reset zoom">${zoomLabel}</button>
      <button data-action="zoom-in" title="Zoom in">+</button>
    </div>
    <div class="graph-scroll"><svg class="graph" width="${scaledWidth}" height="${scaledHeight}" viewBox="0 0 ${layout.width} ${layout.height}" role="img">
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
    <g class="degree-guides">${degreeGuides}</g>
    <g class="edges">${edges}</g>
    <g class="nodes">${nodes}</g>
  </svg></div></div>`;
}

function renderWireEdge(edge: GraphEdge): string {
  const edgeKind = edge.kind === 'cross-branch' ? 'cross-edge' : 'local-edge';
  const branch = `edge-branch-${branchClassName(edge.toBranch)}`;
  const toField = escapeAttr(edge.toField);
  const path = escapeAttr(edge.path);
  return `<path class="edge wire-edge ${edgeKind} ${branch}" data-edge-to="${toField}" d="${path}" />`;
}

function renderSkillNode({ skill, x, y }: { skill: SkillModel; x: number; y: number }): string {
  const selected = skill.field === selectedField ? 'selected' : '';
  const imageUrl = skill.iconItem ? `/asset/item/${encodeURIComponent(skill.iconItem)}.png` : null;
  const initial = skill.name.replace(/^skill_/, '').charAt(0).toUpperCase() || '?';
  return `<g class="skill-node ${selected}" data-skill="${escapeAttr(skill.field)}" transform="translate(${x} ${y})">
    <rect class="node-glow" x="-22" y="-22" width="44" height="44"></rect>
    <rect class="node-frame" x="-18" y="-18" width="36" height="36"></rect>
    <rect class="node-core" x="-12" y="-12" width="24" height="24"></rect>
    ${imageUrl ? `<image href="${escapeAttr(imageUrl)}" x="-9" y="-9" width="18" height="18" preserveAspectRatio="xMidYMid meet"></image>` : ''}
    <text x="0" y="5" class="node-fallback">${escapeHtml(initial)}</text>
    <text x="0" y="34" class="node-level">0/${skill.maxLevels}</text>
  </g>`;
}

function renderDiagnostics(diagnostics: Diagnostic[]): string {
  if (!diagnostics.length) return '<div class="empty">No validation issues.</div>';
  return `<div class="diagnostics">
    ${diagnostics.map(diagnostic => `
      <article class="diagnostic ${diagnostic.severity}">
        <b>${escapeHtml(diagnostic.severity.toUpperCase())}</b>
        <span>${escapeHtml(diagnostic.code)}</span>
        <p>${escapeHtml(diagnostic.message)}</p>
        ${diagnostic.file ? `<small>${escapeHtml(diagnostic.file)}</small>` : ''}
      </article>
    `).join('')}
  </div>`;
}

function renderDiffs(): string {
  if (!preview) return '<div class="empty">No preview has been generated.</div>';
  if (!preview.diffs.length) return '<div class="empty">Preview has no file changes.</div>';
  return `<div class="diffs">
    ${preview.diffs.map(diff => `
      <article class="diff">
        <h2>${escapeHtml(diff.path)}</h2>
        <pre>${escapeHtml(diff.patch)}</pre>
      </article>
    `).join('')}
  </div>`;
}

function renderInspector(): string {
  const branch = currentBranch();
  const skill = selectedSkill();
  if (!branch) return '<div class="empty">No branch selected.</div>';
  if (!skill) return '<div class="empty">No skill selected.</div>';
  const parentOptions = [
    `<option value="" ${skill.parentField ? '' : 'selected'}>None</option>`,
    ...allSkills()
      .filter(candidate => candidate.field !== skill.field)
      .sort((a, b) => a.id - b.id)
      .map(candidate => `<option value="${escapeAttr(candidate.field)}" ${candidate.field === skill.parentField ? 'selected' : ''}>${escapeHtml(candidate.name)}</option>`)
  ].join('');
  const branchOptions = workspace!.branches.map(candidate =>
    `<option value="${escapeAttr(candidate.branch)}" ${candidate.branch === skill.branch ? 'selected' : ''}>${escapeHtml(labelize(candidate.branch))}</option>`
  ).join('');

  return `<form class="editor-form">
    <div class="form-heading">
      <div>
        <h2>${escapeHtml(skill.name)}</h2>
        <p>${escapeHtml(branch.path)}</p>
      </div>
      <div class="icon-chip">${escapeHtml(skill.iconItem ?? 'none')}</div>
    </div>

    <label>Branch<select data-edit="branch">${branchOptions}</select></label>
    <label>Field<input data-edit="field" value="${escapeAttr(skill.field)}" /></label>
    <label>Name<input data-edit="name" value="${escapeAttr(skill.name)}" /></label>
    <div class="grid2">
      <label>ID<input type="number" data-edit="id" value="${skill.id}" /></label>
      <label>State<select data-edit="state">${stateOptions(skill.state)}</select></label>
      <label>Blood cost<input type="number" data-edit="bloodCost" value="${skill.bloodCost}" /></label>
      <label>Max levels<input type="number" data-edit="maxLevels" value="${skill.maxLevels}" /></label>
      <label>Skill points<input type="number" data-edit="skillPointCost" value="${skill.skillPointCost}" /></label>
      <label>Degree<input type="number" data-edit="requiredDegree" value="${skill.requiredDegree}" /></label>
    </div>
    <label>Parent<select data-edit="parentField">${parentOptions}</select></label>
      <label>Icon item<input data-edit="iconItem" value="${escapeAttr(skill.iconItem ?? '')}" placeholder="living_staff" /></label>
    <div class="grid2">
      <label>Tree X<input type="number" data-edit="treeX" value="${skill.treeX ?? ''}" /></label>
      <label>Tree Y<input type="number" data-edit="treeY" value="${skill.treeY ?? ''}" /></label>
    </div>
    <label>Description<textarea data-edit="description" rows="5">${escapeHtml(skill.description)}</textarea></label>
    <div class="form-actions">
      <button type="button" data-action="add-skill">Add</button>
      <button type="button" data-action="duplicate-skill">Duplicate</button>
      <button type="button" data-action="delete-skill">Delete</button>
    </div>
  </form>`;
}

function stateOptions(current: string): string {
  return ['UNLOCKED', 'LOCKED'].map(state =>
    `<option value="${state}" ${state === current ? 'selected' : ''}>${state}</option>`
  ).join('');
}

function wireEvents(): void {
  document.querySelector('[data-action="reload"]')?.addEventListener('click', () => void load());
  document.querySelector('[data-action="preview"]')?.addEventListener('click', () => void makePreview());
  document.querySelector('[data-action="apply"]')?.addEventListener('click', () => void applyCurrentPreview());
  document.querySelector('[data-action="add-skill"]')?.addEventListener('click', addSkill);
  document.querySelector('[data-action="duplicate-skill"]')?.addEventListener('click', duplicateSkill);
  document.querySelector('[data-action="delete-skill"]')?.addEventListener('click', deleteSkill);
  document.querySelector('[data-action="compass-layout"]')?.addEventListener('click', applyCompassAutoLayout);
  document.querySelector('[data-action="undo-move"]')?.addEventListener('click', undoLastMovement);
  document.querySelector('[data-action="redo-move"]')?.addEventListener('click', redoLastMovement);
  document.querySelector('[data-action="zoom-in"]')?.addEventListener('click', () => zoomGraphFromCenter(buttonZoomStep));
  document.querySelector('[data-action="zoom-out"]')?.addEventListener('click', () => zoomGraphFromCenter(1 / buttonZoomStep));
  document.querySelector('[data-action="zoom-reset"]')?.addEventListener('click', resetGraphZoom);
  document.querySelector<HTMLInputElement>('[data-action="snap-grid"]')?.addEventListener('change', event => {
    snapToGrid = (event.currentTarget as HTMLInputElement).checked;
  });

  for (const button of document.querySelectorAll<HTMLButtonElement>('[data-branch]')) {
    button.addEventListener('click', () => {
      selectedBranch = button.dataset.branch ?? selectedBranch;
      selectedField = currentBranch()?.skills[0]?.field ?? '';
      preview = null;
      render();
    });
  }
  for (const button of document.querySelectorAll<HTMLElement>('.skill-button[data-skill], .skill-node[data-skill]')) {
    button.addEventListener('click', () => {
      selectedField = button.dataset.skill ?? selectedField;
      const skill = selectedSkill();
      if (skill) selectedBranch = skill.branch;
      render();
    });
  }
  for (const button of document.querySelectorAll<HTMLButtonElement>('[data-tab]')) {
    button.addEventListener('click', () => {
      currentTab = button.dataset.tab as ViewTab;
      render();
    });
  }
  for (const input of document.querySelectorAll<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>('[data-edit]')) {
    input.addEventListener('input', () => updateSelectedSkill(input));
    input.addEventListener('change', () => updateSelectedSkill(input));
  }
  wireGraphDragPan();
  wireGraphZoom();
  wireGraphNodeDragging();
}

function wireGraphDragPan(): void {
  const scroller = document.querySelector<HTMLElement>('.graph-scroll');
  if (!scroller) return;
  let panState: DragPanState | null = null;
  let pointerId: number | null = null;
  let mousePanning = false;

  scroller.addEventListener('pointerdown', event => {
    if (event.button !== 0 || !shouldStartDragPan(event.target)) return;
    panState = beginDragPan(event.clientX, event.clientY, scroller.scrollLeft, scroller.scrollTop);
    pointerId = event.pointerId;
    scroller.classList.add('panning');
    try {
      scroller.setPointerCapture(event.pointerId);
    } catch {
      // Synthetic pointer events used by tests do not always have capture state.
    }
    event.preventDefault();
  });

  scroller.addEventListener('pointermove', event => {
    if (!panState || pointerId !== event.pointerId) return;
    const update = updateDragPan(panState, event.clientX, event.clientY);
    scroller.scrollLeft = update.scrollLeft;
    scroller.scrollTop = update.scrollTop;
    event.preventDefault();
  });

  const stopPan = (event: PointerEvent): void => {
    if (pointerId !== event.pointerId) return;
    panState = null;
    pointerId = null;
    scroller.classList.remove('panning');
    try {
      scroller.releasePointerCapture(event.pointerId);
    } catch {
      // Capture may already be gone if the pointer left the document.
    }
  };

  scroller.addEventListener('pointerup', stopPan);
  scroller.addEventListener('pointercancel', stopPan);

  scroller.addEventListener('mousedown', event => {
    if (pointerId !== null || event.button !== 0 || !shouldStartDragPan(event.target)) return;
    panState = beginDragPan(event.clientX, event.clientY, scroller.scrollLeft, scroller.scrollTop);
    mousePanning = true;
    scroller.classList.add('panning');
    event.preventDefault();
  });

  window.addEventListener('mousemove', event => {
    if (!mousePanning || !panState) return;
    const update = updateDragPan(panState, event.clientX, event.clientY);
    scroller.scrollLeft = update.scrollLeft;
    scroller.scrollTop = update.scrollTop;
    event.preventDefault();
  });

  window.addEventListener('mouseup', () => {
    if (!mousePanning) return;
    panState = null;
    mousePanning = false;
    scroller.classList.remove('panning');
  });
}

function wireGraphZoom(): void {
  const scroller = document.querySelector<HTMLElement>('.graph-scroll');
  if (!scroller) return;
  scroller.addEventListener('wheel', event => {
    if (event.deltaY === 0) return;
    const rect = scroller.getBoundingClientRect();
    const factor = event.deltaY < 0 ? wheelZoomStep : 1 / wheelZoomStep;
    zoomGraphAt(scroller, event.clientX - rect.left, event.clientY - rect.top, factor);
    event.preventDefault();
  }, { passive: false });
}

function zoomGraphFromCenter(factor: number): void {
  const scroller = document.querySelector<HTMLElement>('.graph-scroll');
  if (!scroller) return;
  zoomGraphAt(scroller, scroller.clientWidth / 2, scroller.clientHeight / 2, factor);
}

function resetGraphZoom(): void {
  const scroller = document.querySelector<HTMLElement>('.graph-scroll');
  if (!scroller || graphZoom === 1) return;
  zoomGraphAt(scroller, scroller.clientWidth / 2, scroller.clientHeight / 2, 1 / graphZoom);
}

function zoomGraphAt(scroller: HTMLElement, viewportX: number, viewportY: number, factor: number): void {
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

function wireGraphNodeDragging(): void {
  const scroller = document.querySelector<HTMLElement>('.graph-scroll');
  const svg = document.querySelector<SVGSVGElement>('.graph');
  if (!scroller || !svg || !workspace) return;

  let dragState: NodeDragState | null = null;
  let dragOrigin: NodePosition | null = null;
  let connectionState: ConnectionDragState | null = null;
  let connectionSource: NodePosition | null = null;
  let pointerId: number | null = null;
  let draggedField = '';

  for (const node of document.querySelectorAll<SVGGElement>('.skill-node[data-skill]')) {
    node.addEventListener('pointerdown', event => {
      if (event.button !== 0) return;
      const field = node.dataset.skill ?? '';
      const skill = allSkills().find(candidate => candidate.field === field);
      const layoutNode = computeGraphLayout(workspace!.branches).nodes.find(candidate => candidate.skill.field === field);
      if (!skill || !layoutNode) return;

      selectedField = skill.field;
      selectedBranch = skill.branch;
      draggedField = skill.field;
      pointerId = event.pointerId;
      if (event.ctrlKey) {
        connectionState = beginConnectionDrag(skill.field);
        connectionSource = { x: layoutNode.x, y: layoutNode.y };
        updateConnectionPreview(svg, connectionSource, graphPointFromPointer(scroller, event));
        scroller.classList.add('connecting-parent');
        try {
          node.setPointerCapture(event.pointerId);
        } catch {
          // Browser tests can synthesize pointer events without capture support.
        }
        event.stopPropagation();
        event.preventDefault();
        return;
      }

      const rect = scroller.getBoundingClientRect();
      dragOrigin = {
        x: skill.treeX ?? layoutNode.x,
        y: skill.treeY ?? layoutNode.y
      };
      dragState = beginNodeDrag({
        clientX: event.clientX - rect.left,
        clientY: event.clientY - rect.top,
        nodeX: dragOrigin.x,
        nodeY: dragOrigin.y,
        scrollLeft: scroller.scrollLeft,
        scrollTop: scroller.scrollTop,
        zoom: graphZoom
      });
      scroller.classList.add('dragging-node');
      try {
        node.setPointerCapture(event.pointerId);
      } catch {
        // Browser tests can synthesize pointer events without capture support.
      }
      event.stopPropagation();
      event.preventDefault();
    });
  }

  const onMove = (event: PointerEvent): void => {
    if (connectionState && pointerId === event.pointerId && connectionSource) {
      updateConnectionPreview(svg, connectionSource, graphPointFromPointer(scroller, event));
      event.preventDefault();
      return;
    }
    if (!dragState || pointerId !== event.pointerId) return;
    const skill = allSkills().find(candidate => candidate.field === draggedField);
    if (!skill) return;
    const rect = scroller.getBoundingClientRect();
    const next = updateNodeDrag(dragState, {
      clientX: event.clientX - rect.left,
      clientY: event.clientY - rect.top,
      scrollLeft: scroller.scrollLeft,
      scrollTop: scroller.scrollTop,
      snap: snapToGrid ? 16 : 1,
      zoom: graphZoom
    });
    skill.treeX = next.x;
    skill.treeY = next.y;
    preview = null;
    const node = svg.querySelector<SVGGElement>(`.skill-node[data-skill="${skill.field}"]`);
    if (node) node.setAttribute('transform', `translate(${next.x} ${next.y})`);
    updateGraphDomEdges();
    updatePositionInputs(skill);
    event.preventDefault();
  };

  const stopDrag = (event: PointerEvent): void => {
    if (pointerId !== event.pointerId) return;
    if (connectionState) {
      const rewire = finishConnectionDrag(connectionState, connectionTargetFieldAt(event));
      const skill = allSkills().find(candidate => candidate.field === draggedField);
      if (rewire && skill) {
        const before = skill.parentField;
        skill.parentField = rewire.parentField;
        recordSkillEdit(movementHistory, {
          fieldBefore: skill.field,
          fieldAfter: skill.field,
          key: 'parentField',
          before,
          after: skill.parentField
        });
        selectedField = skill.field;
        selectedBranch = skill.branch;
        preview = null;
        statusText = `Rewired ${skill.name} parent.`;
      }
      connectionState = null;
      connectionSource = null;
      pointerId = null;
      draggedField = '';
      removeConnectionPreview(svg);
      scroller.classList.remove('connecting-parent');
      workspace!.diagnostics = validateClientWorkspace();
      render();
      return;
    }
    const skill = allSkills().find(candidate => candidate.field === draggedField);
    if (skill && dragOrigin) {
      recordMovement(movementHistory, {
        field: skill.field,
        before: dragOrigin,
        after: {
          x: skill.treeX ?? dragOrigin.x,
          y: skill.treeY ?? dragOrigin.y
        }
      });
    }
    dragState = null;
    dragOrigin = null;
    pointerId = null;
    draggedField = '';
    scroller.classList.remove('dragging-node');
    workspace!.diagnostics = validateClientWorkspace();
    render();
  };

  scroller.addEventListener('pointermove', onMove);
  scroller.addEventListener('pointerup', stopDrag);
  scroller.addEventListener('pointercancel', stopDrag);
}

function undoLastMovement(): void {
  applyMovementTarget(undoMovement(movementHistory), 'Undid move');
}

function redoLastMovement(): void {
  applyMovementTarget(redoMovement(movementHistory), 'Redid move');
}

function applyMovementTarget(target: MovementTarget | undefined, verb: string): void {
  if (!target || !workspace) return;
  let lastSkill: SkillModel | null = null;
  for (const update of target.updates) {
    const skill = allSkills().find(candidate => candidate.field === update.field);
    if (!skill) continue;
    if (update.position) {
      skill.treeX = update.position.x;
      skill.treeY = update.position.y;
    } else if (update.edit) {
      applySkillEditValue(skill, update.edit.key, update.edit.value);
    }
    lastSkill = skill;
  }
  if (!lastSkill) {
    statusText = 'History targets no longer exist.';
    render();
    return;
  }
  selectedField = lastSkill.field;
  selectedBranch = lastSkill.branch;
  preview = null;
  workspace.diagnostics = validateClientWorkspace();
  statusText = `${verb} for ${target.updates.length} skill${target.updates.length === 1 ? '' : 's'}.`;
  render();
}

function applyCompassAutoLayout(): void {
  if (!workspace) return;
  const changes = applyCompassLayout(workspace.branches);
  recordMovements(movementHistory, changes);
  if (changes.length) {
    const last = allSkills().find(skill => skill.field === changes[changes.length - 1].field);
    if (last) {
      selectedField = last.field;
      selectedBranch = last.branch;
    }
  }
  preview = null;
  workspace.diagnostics = validateClientWorkspace();
  statusText = changes.length
    ? `Compass layout placed ${changes.length} skills.`
    : 'Compass layout already matches the draft.';
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

function updateGraphDomEdges(): void {
  if (!workspace) return;
  const layout = computeGraphLayout(workspace.branches);
  for (const edge of layout.edges) {
    const paths = document.querySelectorAll<SVGPathElement>(`.edge[data-edge-to="${edge.toField}"]`);
    for (const path of paths) {
      path.setAttribute('d', edge.path);
    }
  }
}

function updatePositionInputs(skill: SkillModel): void {
  const xInput = document.querySelector<HTMLInputElement>('[data-edit="treeX"]');
  const yInput = document.querySelector<HTMLInputElement>('[data-edit="treeY"]');
  if (xInput) xInput.value = String(skill.treeX ?? '');
  if (yInput) yInput.value = String(skill.treeY ?? '');
}

function graphPointFromPointer(scroller: HTMLElement, event: PointerEvent): NodePosition {
  const rect = scroller.getBoundingClientRect();
  return {
    x: (event.clientX - rect.left + scroller.scrollLeft) / graphZoom,
    y: (event.clientY - rect.top + scroller.scrollTop) / graphZoom
  };
}

function connectionTargetFieldAt(event: PointerEvent): string | null {
  const target = document.elementFromPoint(event.clientX, event.clientY);
  if (!(target instanceof Element)) return null;
  return target.closest<SVGGElement>('.skill-node[data-skill]')?.dataset.skill ?? null;
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

function updateSelectedSkill(input: HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement): void {
  const skill = selectedSkill();
  if (!skill) return;
  const key = input.dataset.edit as EditableSkillKey;
  const fieldBefore = skill.field;
  const before = skillEditValue(skill, key);
  const next = coerceSkillEditValue(key, input.value);
  preview = null;
  applySkillEditValue(skill, key, next);
  recordSkillEdit(movementHistory, {
    fieldBefore,
    fieldAfter: skill.field,
    key,
    before,
    after: skillEditValue(skill, key)
  });
  selectedField = skill.field;
  selectedBranch = skill.branch;
  workspace!.diagnostics = validateClientWorkspace();
  render();
}

function skillEditValue(skill: SkillModel, key: EditableSkillKey): SkillEditValue {
  return skill[key] as SkillEditValue;
}

function coerceSkillEditValue(key: EditableSkillKey, value: string): SkillEditValue {
  if (key === 'id' || key === 'bloodCost' || key === 'maxLevels' || key === 'skillPointCost' || key === 'requiredDegree') {
    return Number(value);
  }
  if (key === 'treeX' || key === 'treeY') {
    return value.trim() ? Number(value) : null;
  }
  if (key === 'parentField') {
    return value || null;
  }
  if (key === 'iconItem') {
    return value.trim() || null;
  }
  return value;
}

function applySkillEditValue(skill: SkillModel, key: string, value: SkillEditValue): void {
  if (key === 'id' || key === 'bloodCost' || key === 'maxLevels' || key === 'skillPointCost' || key === 'requiredDegree') {
    (skill[key] as number) = Number(value);
  } else if (key === 'treeX' || key === 'treeY') {
    (skill[key] as number | null) = typeof value === 'number' ? value : null;
  } else if (key === 'parentField') {
    skill.parentField = typeof value === 'string' && value ? value : null;
  } else if (key === 'iconItem') {
    skill.iconItem = typeof value === 'string' && value.trim() ? value.trim() : null;
  } else if (key === 'branch') {
    if (typeof value === 'string') moveSkillToBranch(skill, value);
  } else if (key === 'field' || key === 'name' || key === 'state' || key === 'description') {
    skill[key] = String(value ?? '');
  }
}

function addSkill(): void {
  const branch = currentBranch();
  if (!branch) return;
  const nextId = Math.max(0, ...allSkills().map(skill => skill.id)) + 1;
  const newSkill: SkillModel = {
    field: `skill_new_${nextId}`,
    id: nextId,
    name: `skill_new_${nextId}`,
    branch: branch.branch,
    bloodCost: 100,
    maxLevels: 1,
    state: 'LOCKED',
    parentField: selectedSkill()?.field ?? null,
    skillPointCost: 1,
    requiredDegree: 0,
    treeX: selectedSkill()?.treeX != null ? selectedSkill()!.treeX! + 64 : null,
    treeY: selectedSkill()?.treeY != null ? selectedSkill()!.treeY! + 64 : null,
    iconItem: null,
    description: ''
  };
  branch.skills.push(newSkill);
  selectedField = newSkill.field;
  preview = null;
  workspace!.diagnostics = validateClientWorkspace();
  render();
}

function duplicateSkill(): void {
  const branch = currentBranch();
  const skill = selectedSkill();
  if (!branch || !skill) return;
  const nextId = Math.max(0, ...allSkills().map(item => item.id)) + 1;
  const clone = {
    ...skill,
    id: nextId,
    field: `${skill.field}_copy`,
    name: `${skill.name}_copy`,
    parentField: skill.field,
    treeX: skill.treeX != null ? skill.treeX + 64 : null,
    treeY: skill.treeY != null ? skill.treeY + 64 : null
  };
  branch.skills.push(clone);
  selectedField = clone.field;
  preview = null;
  workspace!.diagnostics = validateClientWorkspace();
  render();
}

function deleteSkill(): void {
  const branch = currentBranch();
  const skill = selectedSkill();
  if (!branch || !skill) return;
  branch.skills = branch.skills.filter(candidate => candidate.field !== skill.field);
  for (const candidate of allSkills()) {
    if (candidate.parentField === skill.field) candidate.parentField = null;
  }
  selectedField = branch.skills[0]?.field ?? '';
  preview = null;
  workspace!.diagnostics = validateClientWorkspace();
  render();
}

function moveSkillToBranch(skill: SkillModel, branchName: string): void {
  const from = workspace!.branches.find(branch => branch.branch === skill.branch);
  const to = workspace!.branches.find(branch => branch.branch === branchName);
  if (!from || !to || from === to) return;
  from.skills = from.skills.filter(candidate => candidate.field !== skill.field);
  skill.branch = branchName;
  to.skills.push(skill);
}

async function makePreview(): Promise<void> {
  if (!workspace) return;
  isBusy = true;
  statusText = 'Preparing preview...';
  render();
  try {
    preview = await api<PreviewResult>('/api/preview', {
      method: 'POST',
      body: JSON.stringify({
        branches: workspace.branches,
        translations: collectSkillTranslations()
      })
    });
    workspace.diagnostics = preview.diagnostics;
    currentTab = 'diff';
    statusText = preview.canApply
      ? `Preview ready with ${preview.diffs.length} file changes.`
      : 'Preview has blocking validation issues.';
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
  } finally {
    isBusy = false;
    render();
  }
}

async function applyCurrentPreview(): Promise<void> {
  if (!preview) return;
  isBusy = true;
  statusText = 'Applying preview...';
  render();
  try {
    await api('/api/apply', {
      method: 'POST',
      body: JSON.stringify({ id: preview.id })
    });
    statusText = 'Preview applied.';
    await load();
  } catch (err) {
    statusText = err instanceof Error ? err.message : String(err);
  } finally {
    isBusy = false;
    render();
  }
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
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

function validateClientWorkspace(): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];
  const skills = allSkills();
  const ids = new Map<number, SkillModel>();
  const fields = new Map<string, SkillModel>();
  const names = new Map<string, SkillModel>();
  for (const skill of skills) {
    if (ids.has(skill.id)) diagnostics.push(issue('error', 'duplicate_skill_id', `Duplicate id ${skill.id}.`, skill));
    if (fields.has(skill.field)) diagnostics.push(issue('error', 'duplicate_skill_field', `Duplicate field ${skill.field}.`, skill));
    if (names.has(skill.name)) diagnostics.push(issue('error', 'duplicate_skill_name', `Duplicate name ${skill.name}.`, skill));
    ids.set(skill.id, skill);
    fields.set(skill.field, skill);
    names.set(skill.name, skill);
    if (skill.parentField && !fields.has(skill.parentField) && !skills.some(candidate => candidate.field === skill.parentField)) {
      diagnostics.push(issue('error', 'missing_parent_skill', `Missing parent ${skill.parentField}.`, skill));
    }
    if (!skill.description.trim()) diagnostics.push(issue('warning', 'missing_skill_description', `Missing description for ${skill.name}.`, skill));
  }
  return diagnostics;
}

function issue(severity: Diagnostic['severity'], code: string, message: string, skill: SkillModel): Diagnostic {
  return { severity, code, message, skill: skill.field };
}

function collectSkillTranslations(): Record<string, string> {
  return Object.fromEntries(allSkills().map(skill => [`skill.hemomancy.${skill.name}.desc`, skill.description]));
}

function allSkills(): SkillModel[] {
  return workspace?.branches.flatMap(branch => branch.skills) ?? [];
}

function currentBranch(): SkillBranchFile | undefined {
  return workspace?.branches.find(branch => branch.branch === selectedBranch);
}

function selectedSkill(): SkillModel | undefined {
  return allSkills().find(skill => skill.field === selectedField);
}

function relativeRoot(): string {
  return workspace?.repoRoot.split(/[\\/]/).slice(-3).join('/') ?? '';
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}

function branchClassName(value: string): string {
  return value.replace(/_/g, '-').replace(/[^a-zA-Z0-9-]/g, '').toLowerCase();
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function escapeAttr(value: string): string {
  return escapeHtml(value);
}
