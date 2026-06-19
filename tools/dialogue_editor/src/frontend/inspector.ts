import type { DialogueNodeModel, DialogueTreeModel, NpcMetadata } from '../shared/types';
import { pushMetadata } from './api';
import { currentFile, currentNode, currentNodeTree, metadataKey, optionMeta, pushUndo, speakerSlug, state, translation } from './state';

function escapeHtml(v: string): string {
  return v.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttr(v: string): string {
  return escapeHtml(v).replace(/"/g, '&quot;');
}

function suggestLineKey(tree: DialogueTreeModel, node: DialogueNodeModel): string {
  return `hemomancy.dialogue.${tree.method}.${node.id}.line${node.lines.length + 1}`;
}

function suggestOptionKey(tree: DialogueTreeModel, node: DialogueNodeModel, index: number): string {
  return `hemomancy.dialogue.${tree.method}.option.${node.id}_${index}`;
}

export function renderInspector(el: HTMLElement, onRender: () => void): void {
  const row = state.selectedRow;
  if (!row) {
    el.innerHTML = '<div class="empty">Select a node section to edit it.</div>';
    return;
  }

  const node = currentNode();
  const tree = currentNodeTree();
  if (!node || !tree) {
    el.innerHTML = '<div class="empty">Node not found.</div>';
    return;
  }

  if (row.section === 'lines') renderLinesPanel(el, node, tree, onRender);
  else if (row.section === 'option') renderOptionPanel(el, node, tree, (row as { optionIndex: number }).optionIndex, onRender);
  else renderTriggersPanel(el);
}

function renderLinesPanel(el: HTMLElement, node: DialogueNodeModel, tree: DialogueTreeModel, onRender: () => void): void {
  el.innerHTML = `
    <div class="insp-block">
      <div class="insp-field">
        <label>NODE ID</label>
        <input id="node-id" class="mono" value="${escapeAttr(node.id)}">
      </div>
    </div>
    <div class="insp-block">
      <div class="insp-section-head">
        <span>LINES</span>
        <button id="add-line">+ Add Line</button>
      </div>
      ${node.lines.map((line, i) => `
        <div class="insp-line-card">
          <div class="insp-line-key-row">
            <input class="mono" data-line="${i}" value="${escapeAttr(line)}">
            <button class="danger icon-button" data-delete-line="${i}">×</button>
          </div>
          <div class="insp-translation">${escapeHtml(translation(line) || '(no translation)')}</div>
        </div>`).join('')}
      ${node.lines.length === 0 ? '<div class="empty" style="padding:10px">No lines. Add one above.</div>' : ''}
    </div>
    <div class="insp-block">
      <div class="insp-section-head">
        <span>OPTIONS</span>
        <button id="add-option">+ Add Option</button>
      </div>
      ${node.options.map((_, i) => `
        <div class="insp-option-stub" data-jump-option="${i}">Option ${i + 1}</div>`).join('')}
    </div>
    <div class="insp-block insp-danger-block">
      <button id="delete-node" class="danger">Delete Node</button>
    </div>`;

  document.getElementById('node-id')!.addEventListener('change', e => {
    const next = (e.target as HTMLInputElement).value.trim();
    if (!next || next === node.id) return;
    pushUndo();
    const old = node.id;
    node.id = next;
    tree.nodes.forEach(n => n.options.forEach(o => { if (o.next === old) o.next = next; }));
    if (state.selectedRow) state.selectedRow = { ...state.selectedRow, nodeId: next };
    state.preview = null;
    onRender();
  });

  document.getElementById('add-line')!.onclick = () => {
    pushUndo();
    node.lines.push(suggestLineKey(tree, node));
    state.preview = null;
    onRender();
  };

  document.getElementById('add-option')!.onclick = () => {
    pushUndo();
    node.options.push({ text: suggestOptionKey(tree, node, node.options.length + 1), next: null, event: null });
    state.preview = null;
    onRender();
  };

  document.getElementById('delete-node')!.onclick = () => {
    pushUndo();
    tree.nodes = tree.nodes.filter(n => n.id !== node.id);
    tree.nodes.forEach(n => n.options.forEach(o => { if (o.next === node.id) o.next = null; }));
    state.selectedRow = null;
    state.preview = null;
    onRender();
  };

  el.querySelectorAll<HTMLInputElement>('[data-line]').forEach(input => {
    input.oninput = () => { pushUndo(); node.lines[Number(input.dataset.line)] = input.value; state.preview = null; };
  });

  el.querySelectorAll<HTMLButtonElement>('[data-delete-line]').forEach(btn => {
    btn.onclick = () => {
      pushUndo();
      node.lines.splice(Number(btn.dataset.deleteLine), 1);
      state.preview = null;
      onRender();
    };
  });

  el.querySelectorAll<HTMLElement>('[data-jump-option]').forEach(stub => {
    stub.onclick = () => {
      if (state.selectedRow) {
        state.selectedRow = { ...state.selectedRow, section: 'option', optionIndex: Number(stub.dataset.jumpOption) } as typeof state.selectedRow;
      }
      onRender();
    };
  });
}

function renderOptionPanel(el: HTMLElement, node: DialogueNodeModel, tree: DialogueTreeModel, optionIndex: number, onRender: () => void): void {
  const option = node.options[optionIndex];
  if (!option) { el.innerHTML = '<div class="empty">Option not found.</div>'; return; }

  const file = currentFile();
  const slug = file ? speakerSlug(file) : '';
  const meta = optionMeta(slug, tree.method, tree.variant, node.id, optionIndex);

  const nodeOptions = ['<option value="">-- end conversation --</option>']
    .concat(tree.nodes.filter(n => n.id !== node.id).map(n =>
      `<option value="${escapeAttr(n.id)}"${option.next === n.id ? ' selected' : ''}>${escapeHtml(n.id)}</option>`
    )).join('');

  el.innerHTML = `
    <div class="insp-block">
      <div class="insp-section-head">OPTION ${optionIndex + 1}</div>
      <div class="insp-field">
        <label>TEXT KEY</label>
        <input class="mono" id="opt-text" value="${escapeAttr(option.text)}">
        <div class="insp-translation">${escapeHtml(translation(option.text) || '(no translation)')}</div>
      </div>
      <div class="insp-field">
        <label>GOES TO</label>
        <select id="opt-next">${nodeOptions}</select>
      </div>
      <div class="insp-field">
        <label>EVENT</label>
        <input class="mono" id="opt-event" value="${escapeAttr(option.event ?? '')}">
      </div>
      <div class="insp-divider">Triggers (sidecar metadata)</div>
      <div class="insp-field">
        <label>ANIMATION TRIGGER</label>
        <input class="mono" id="opt-animation" placeholder="e.g. acolyte_kneel" value="${escapeAttr(meta.animationTrigger ?? '')}">
      </div>
      <div class="insp-field">
        <label>SOUND TRIGGER</label>
        <input class="mono" id="opt-sound" placeholder="e.g. hemomancy:npc/acolyte/kneel" value="${escapeAttr(meta.soundTrigger ?? '')}">
      </div>
    </div>
    <div class="insp-block insp-danger-block">
      <button id="delete-option" class="danger">Delete Option</button>
    </div>`;

  document.getElementById('opt-text')!.addEventListener('input', e => {
    pushUndo();
    option.text = (e.target as HTMLInputElement).value;
    state.preview = null;
  });

  document.getElementById('opt-next')!.addEventListener('change', e => {
    pushUndo();
    option.next = (e.target as HTMLSelectElement).value || null;
    state.preview = null;
    onRender();
  });

  document.getElementById('opt-event')!.addEventListener('input', e => {
    pushUndo();
    option.event = (e.target as HTMLInputElement).value.trim() || null;
    state.preview = null;
  });

  async function saveTrigger(field: 'animationTrigger' | 'soundTrigger', value: string): Promise<void> {
    if (!file) return;
    const key = metadataKey(tree.method, tree.variant, node.id, optionIndex);
    const current = state.metadata[slug] ?? { version: 1, options: {} };
    const existing = current.options[key] ?? {};
    const updated: NpcMetadata = {
      ...current,
      options: { ...current.options, [key]: { ...existing, [field]: value || undefined } }
    };
    state.metadata[slug] = updated;
    await pushMetadata(slug, updated);
  }

  document.getElementById('opt-animation')!.addEventListener('change', e => {
    pushUndo();
    saveTrigger('animationTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('opt-sound')!.addEventListener('change', e => {
    pushUndo();
    saveTrigger('soundTrigger', (e.target as HTMLInputElement).value.trim());
  });

  document.getElementById('delete-option')!.onclick = () => {
    pushUndo();
    node.options.splice(optionIndex, 1);
    state.selectedRow = state.selectedRow ? { ...state.selectedRow, section: 'lines' } : null;
    state.preview = null;
    onRender();
  };
}

function renderTriggersPanel(el: HTMLElement): void {
  el.innerHTML = `<div class="insp-block">
    <div class="insp-section-head">NODE TRIGGERS</div>
    <div class="empty" style="padding:14px">Node-level triggers are not yet implemented. Use option-level triggers via the Options section.</div>
  </div>`;
}
