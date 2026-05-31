import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const sourceRoot = resolve(process.cwd(), 'src/frontend');

test('preview node artwork fits compact in-game coordinate spacing', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(rectWidth(main, 'node-frame')).toBeLessThanOrEqual(36);
  expect(rectWidth(main, 'node-glow')).toBeLessThanOrEqual(48);
  expect(numberAttr(main, 'node-level', 'y')).toBeLessThanOrEqual(36);
  expect(cssPx(css, '.skill-node .node-level', 'font')).toBeLessThanOrEqual(18);
  expect(cssPx(css, '.local-edge', 'stroke-width')).toBeLessThanOrEqual(6);
});

test('preview exposes movement undo and redo controls with keyboard shortcuts', () => {
  const main = read('main.ts');

  expect(main).toContain('data-action="undo-move"');
  expect(main).toContain('data-action="redo-move"');
  expect(main).toContain('isMovementUndoShortcut');
  expect(main).toContain('isMovementRedoShortcut');
});

test('preview records inspector edits and supports ctrl-drag parent rewiring', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(main).toContain('recordSkillEdit');
  expect(main).toContain('beginConnectionDrag');
  expect(main).toContain('finishConnectionDrag');
  expect(main).toContain('event.ctrlKey');
  expect(main).toContain('connection-preview');
  expect(main).toContain('refreshAfterConnectionRewire');
  expect(connectionDropBlock(main)).toContain('candidate.field === rewire.field');
  expect(connectionDropBlock(main)).not.toContain('renderPreservingGraphViewport');
  expect(css).toContain('.connection-preview');
});

test('preview exposes multi-parent inspector controls', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(main).toContain('data-action="add-parent"');
  expect(main).toContain('data-action="remove-parent"');
  expect(main).toContain('parentFields');
  expect(css).toContain('.parent-list');
  expect(css).toContain('.parent-pill');
});

test('preview exposes draggable degree labels', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(main).toContain('data-degree-label');
  expect(main).toContain('recordDegreeLabelMovement');
  expect(main).toContain('ensureDegreeLabelPosition');
  expect(css).toContain('.degree-guide-label');
  expect(css).toContain('dragging-degree-label');
});

test('preview preserves the graph viewport across full renders', () => {
  const main = read('main.ts');
  const renderBlockSource = renderBlock(main);

  expect(renderBlockSource).toContain('captureCurrentGraphViewport');
  expect(renderBlockSource).toContain('restoreCurrentGraphViewport');
  expect(main).toContain("requestAnimationFrame(() => restoreCurrentGraphViewport(viewport))");
});

test('preview colors trace lines by destination branch', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(main).toContain('edge-branch-');
  expect(main).toContain('renderWireEdge');
  expect(main).toContain('wire-edge');
  expect(main).not.toContain('tendril-edge-halo');
  expect(main).not.toContain('tendril-edge-shadow');
  expect(main).not.toContain('tendril-edge-core');
  expect(css).toContain('stroke-linecap: round');
  expect(css).toContain('.wire-edge');
  expect(css).not.toContain('.tendril-edge-halo');
  expect(css).not.toContain('.tendril-edge-shadow');
  expect(css).not.toContain('.tendril-edge-core');
  expect(css).toContain('.edge-branch-core');
  expect(css).toContain('.edge-branch-scars');
  expect(css).toContain('.edge-branch-summons');
  expect(css).toContain('.edge-branch-living-staff');
});

test('preview colors branch list buttons by branch', () => {
  const main = read('main.ts');
  const css = read('styles.css');

  expect(main).toContain('branch-button-');
  expect(main).toContain('branchButtonStyle');
  expect(main).toContain('data-branch-edit="color"');
  expect(css).toContain('.branch-button-core');
  expect(css).toContain('.branch-button-scars');
  expect(css).toContain('.branch-button-summons');
  expect(css).toContain('.branch-button-living-staff');
});

function read(path: string): string {
  return readFileSync(resolve(sourceRoot, path), 'utf8');
}

function rectWidth(source: string, className: string): number {
  const match = new RegExp(`<rect class="${className}"[^>]*width="(\\d+)"`).exec(source);
  if (!match) throw new Error(`Missing ${className} rect width.`);
  return Number(match[1]);
}

function numberAttr(source: string, className: string, attr: string): number {
  const match = new RegExp(`<text[^>]*${attr}="(\\d+)"[^>]*class="${className}"`).exec(source);
  if (!match) throw new Error(`Missing ${className} ${attr} attribute.`);
  return Number(match[1]);
}

function cssPx(source: string, selector: string, property: string): number {
  const escaped = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = new RegExp(`${escaped} \\{[\\s\\S]*?${property}:\\s*(\\d+)px`).exec(source);
  if (!match) throw new Error(`Missing ${property} for ${selector}.`);
  return Number(match[1]);
}

function connectionDropBlock(source: string): string {
  const start = source.indexOf('if (connectionState) {');
  const end = source.indexOf('if (draggedDegree !== null && dragOrigin)', start + 1);
  if (start < 0 || end < 0) throw new Error('Missing connection drop block.');
  return source.slice(start, end);
}

function renderBlock(source: string): string {
  const start = source.indexOf('function render(): void {');
  const end = source.indexOf('function renderBranchButton', start);
  if (start < 0 || end < 0) throw new Error('Missing render block.');
  return source.slice(start, end);
}
