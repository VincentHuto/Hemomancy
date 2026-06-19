import type { DialogueFile } from '../shared/types';
import { currentFile, paletteFor, state, translation } from './state';
import type { Palette } from './state';

function basename(path: string): string {
  return path.split(/[\\/]/).at(-1) ?? path;
}

function escapeHtml(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(value: string): string {
  return escapeHtml(value).replace(/"/g, '&quot;');
}

export function renderSidebar(el: HTMLElement, onRender: () => void): void {
  const ws = state.workspace;
  if (!ws) {
    el.innerHTML = '<div class="empty">Loading...</div>';
    return;
  }

  const file = currentFile();
  const groupedFiles: Array<{ palette: Palette; label: string; files: Array<{ file: DialogueFile; index: number }> }> = [
    { palette: 'harbinger', label: 'Harbinger', files: [] },
    { palette: 'fungal', label: 'Fungal', files: [] },
    { palette: 'unstained', label: 'Unstained', files: [] }
  ];
  ws.dialogueFiles.forEach((dialogueFile, index) => {
    groupedFiles.find(g => g.palette === paletteFor(dialogueFile))!.files.push({ file: dialogueFile, index });
  });

  const allNodes = file?.trees.flatMap(tree =>
    tree.nodes.map(node => ({ node, treeMethod: tree.method }))
  ) ?? [];

  el.innerHTML = `
    <div class="section-title">Dialogue Files</div>
    ${groupedFiles.filter(g => g.files.length).map(group => {
      const collapsed = state.collapsedFileGroups.has(group.palette);
      return `
        <div class="folder-row ${collapsed ? 'collapsed' : ''}" data-file-group="${group.palette}">
          <span><span class="folder-caret">${collapsed ? '>' : 'v'}</span>${group.label}</span>
          <span class="count">${group.files.length}</span>
        </div>
        ${collapsed ? '' : group.files.map(({ file: f, index: i }) => `
          <div class="file-row file-row-nested ${i === state.fileIndex ? 'active' : ''}" data-file="${i}">
            <span>${escapeHtml(basename(f.path))}</span><span class="count">${f.trees.length}</span>
          </div>`).join('')}`;
    }).join('')}
    <div class="section-title">Nodes</div>
    ${allNodes.map(({ node, treeMethod }) => {
      const isActive = state.selectedRow?.nodeId === node.id && state.selectedRow?.treeMethod === treeMethod;
      return `<div class="node-row ${isActive ? 'active' : ''}" data-node="${escapeAttr(node.id)}" data-tree-method="${escapeAttr(treeMethod)}">
        <span>${escapeHtml(node.id)}</span>
        <span class="count">${node.options.length}</span>
      </div>`;
    }).join('')}
  `;

  el.querySelectorAll<HTMLElement>('[data-file]').forEach(row => row.onclick = () => {
    state.fileIndex = Number(row.dataset.file);
    state.selectedRow = null;
    onRender();
  });
  el.querySelectorAll<HTMLElement>('[data-file-group]').forEach(row => row.onclick = () => {
    const group = row.dataset.fileGroup as Palette;
    if (state.collapsedFileGroups.has(group)) state.collapsedFileGroups.delete(group);
    else state.collapsedFileGroups.add(group);
    onRender();
  });
  el.querySelectorAll<HTMLElement>('[data-node]').forEach(row => row.onclick = () => {
    const nodeId = row.dataset.node!;
    const treeMethod = row.dataset.treeMethod!;
    state.selectedRow = { treeMethod, nodeId, section: 'lines' };
    onRender();
  });
}
