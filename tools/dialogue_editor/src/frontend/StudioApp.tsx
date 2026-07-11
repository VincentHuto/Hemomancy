import { useEffect, useMemo, useState } from 'react';
import {
  Background, BackgroundVariant, Controls, Handle, MarkerType, MiniMap, Position, ReactFlow, ReactFlowProvider,
  applyNodeChanges, type Edge, type Node, type NodeChange, type NodeProps
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import type { DialogueNodeModel, DialogueOptionModel, DialogueTreeModel } from '../shared/types';
import { applyPreview, loadWorkspace, previewChanges } from './api';
import { layoutTree, type StoredPosition } from './studioLayout';
import { buildPreviewRequest, pendingSummary, treeIdentity } from './studioModel';
import { activeContext, activeNode, useStudio } from './studioStore';
import './studio.css';

type CardData = {
  node: DialogueNodeModel;
  tree: DialogueTreeModel;
  translations: Record<string, string>;
  selected: boolean;
  events: string[];
  select(): void;
  line(index: number, prose: string): void;
  addLine(): void;
  choice(index: number, prose: string): void;
  patchChoice(index: number, patch: Partial<DialogueOptionModel>): void;
  addChoice(): void;
  removeChoice(index: number): void;
};
type DialogueFlowNode = Node<CardData, 'dialogue'>;

function titleCase(value: string): string {
  const text = value.replace(/DialogueTrees\.java$/, '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/[_-]+/g, ' ');
  return text ? text[0].toUpperCase() + text.slice(1) : text;
}

function NodeCard({ data }: NodeProps<DialogueFlowNode>) {
  const { node, tree, translations } = data;
  return <article className={`dialogue-card ${data.selected ? 'selected' : ''}`} onClick={data.select}>
    <Handle type="target" position={Position.Left} className="flow-port" />
    <header className="card-titlebar">
      <div><span className="node-kind">{tree.startNode === node.id ? 'ENTRY NODE' : 'DIALOGUE NODE'}</span><strong>{node.id}</strong></div>
      <span className="choice-count">{node.options.length}</span>
    </header>
    <section className="speech-stack">
      {node.lines.map((key, index) => <label className="speech-field nodrag" key={`${key}-${index}`}>
        <span>NPC · LINE {index + 1}</span>
        <textarea defaultValue={translations[key] ?? ''} onBlur={event => data.line(index, event.currentTarget.value)} />
        <small title={key}>{key}</small>
      </label>)}
      <button className="card-add nodrag" onClick={event => { event.stopPropagation(); data.addLine(); }}>＋ Add NPC line</button>
    </section>
    <section className="choice-stack">
      <div className="section-kicker">PLAYER RESPONSES</div>
      {node.options.map((option, index) => <div className="choice-editor nodrag" key={`${option.text}-${index}`}>
        <Handle id={`choice-${index}`} type="source" position={Position.Right} className="flow-port choice-port" />
        <span className="choice-number">{index + 1}</span>
        <div className="choice-fields">
          <textarea aria-label={`Response ${index + 1}`} defaultValue={translations[option.text] ?? ''} onBlur={event => data.choice(index, event.currentTarget.value)} />
          <div className="choice-meta">
            <select aria-label={`Destination ${index + 1}`} value={option.next ?? ''} onChange={event => data.patchChoice(index, { next: event.target.value || null })}>
              <option value="">End conversation</option>
              {tree.nodes.filter(candidate => candidate.id !== node.id).map(candidate => <option key={candidate.id} value={candidate.id}>Go to · {candidate.id}</option>)}
            </select>
            <input aria-label={`Event ${index + 1}`} list="event-catalog" value={option.event ?? ''} placeholder="No event" onChange={event => data.patchChoice(index, { event: event.target.value || null })} />
          </div>
        </div>
        <button className="remove-choice" title="Remove response" onClick={() => data.removeChoice(index)}>×</button>
      </div>)}
      <button className="card-add nodrag" onClick={event => { event.stopPropagation(); data.addChoice(); }}>＋ Add response</button>
    </section>
  </article>;
}

const nodeTypes = { dialogue: NodeCard };

function DialogueCanvas() {
  const studio = useStudio();
  const ctx = activeContext(studio);
  const [nodes, setNodes] = useState<DialogueFlowNode[]>([]);
  const tree = ctx?.tree;
  const graphKey = ctx ? `${ctx.file.path}::${ctx.tree.method}::${ctx.tree.variant ?? 'main'}` : '';
  const structureKey = tree ? `${graphKey}:${tree.nodes.map(node => `${node.id}:${node.options.map(option => option.next).join(',')}`).join('|')}` : '';

  useEffect(() => {
    if (!tree || tree.dispatchOnly) { setNodes([]); return; }
    let current = true;
    layoutTree(tree, studio.positions[graphKey] ?? {}).then(positions => {
      if (!current) return;
      studio.setPositions(graphKey, positions);
      setNodes(makeNodes(tree, positions));
    });
    return () => { current = false; };
  // structural changes trigger layout; prose updates are merged by the next effect
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [structureKey]);

  useEffect(() => {
    if (!tree || tree.dispatchOnly) return;
    setNodes(previous => makeNodes(tree, Object.fromEntries(previous.map(node => [node.id, { ...node.position, pinned: studio.positions[graphKey]?.[node.id]?.pinned }]))));
  }, [studio.draft, studio.nodeId, tree, graphKey]);

  function makeNodes(dialogueTree: DialogueTreeModel, positions: Record<string, StoredPosition>): DialogueFlowNode[] {
    const translations = studio.draft?.workspace.translations ?? {};
    const events = studio.draft?.workspace.events.map(event => event.id) ?? [];
    return dialogueTree.nodes.map(node => ({
      id: node.id, type: 'dialogue', position: positions[node.id] ?? { x: 0, y: 0 }, selected: studio.nodeId === node.id,
      data: {
        node, tree: dialogueTree, translations, events, selected: studio.nodeId === node.id,
        select: () => studio.selectNode(node.id),
        line: (index, prose) => studio.updateNodeLine(node.id, index, prose), addLine: () => studio.addLine(node.id),
        choice: (index, prose) => studio.updateChoiceProse(node.id, index, prose),
        patchChoice: (index, patch) => studio.updateChoice(node.id, index, patch),
        addChoice: () => studio.addChoice(node.id), removeChoice: index => studio.removeChoice(node.id, index)
      }
    }));
  }

  const edges = useMemo<Edge[]>(() => tree?.nodes.flatMap(node => node.options.flatMap((option, index) => option.next ? [{
    id: `${node.id}-${index}-${option.next}`, source: node.id, sourceHandle: `choice-${index}`, target: option.next,
    type: 'smoothstep', markerEnd: { type: MarkerType.ArrowClosed }, className: 'dialogue-edge'
  }] : [])) ?? [], [tree]);

  if (!ctx) return <Empty title="Choose a dialogue tree" body="Select an NPC and tree from the navigator." />;
  if (ctx.tree.dispatchOnly) return <Empty title="Router method" body="This Java method chooses a concrete dialogue tree. Open Routes to inspect its routing context." />;
  return <div className="canvas-wrap">
    <ReactFlow<DialogueFlowNode>
      nodes={nodes} edges={edges} nodeTypes={nodeTypes} fitView minZoom={0.18} maxZoom={1.35}
      onNodesChange={(changes: NodeChange<DialogueFlowNode>[]) => setNodes(current => applyNodeChanges(changes, current))}
      onNodeDragStop={(_, node) => studio.pinNode(graphKey, node.id, { ...node.position, pinned: true })}
      onPaneClick={() => studio.selectNode(null)} nodesConnectable={false} proOptions={{ hideAttribution: true }}>
      <Background variant={BackgroundVariant.Dots} gap={28} size={1} />
      <Controls position="bottom-left" />
      <MiniMap position="bottom-right" pannable zoomable nodeColor="var(--accent)" maskColor="rgba(7,9,12,.72)" />
    </ReactFlow>
    <button className="floating-add" onClick={studio.addNode}>＋ New node</button>
  </div>;
}

function Empty({ title, body }: { title: string; body: string }) {
  return <div className="empty-state"><span>HEMOMANCY DIALOGUE STUDIO</span><h2>{title}</h2><p>{body}</p></div>;
}

function Navigator() {
  const studio = useStudio();
  const ws = studio.draft?.workspace;
  const file = ws?.dialogueFiles.find(candidate => candidate.path === studio.filePath);
  const query = studio.query.toLowerCase();
  const trees = file?.trees.filter(tree => !query || [tree.method, tree.startNode, ...tree.nodes.flatMap(node => [node.id, ...node.lines.map(key => ws?.translations[key] ?? '')])].join(' ').toLowerCase().includes(query)) ?? [];
  return <aside className="navigator">
    <div className="navigator-brand"><div className="brand-mark">H</div><div><strong>Dialogue Studio</strong><span>HEMOMANCY AUTHORING</span></div></div>
    <label className="global-search"><span>⌕</span><input value={studio.query} onChange={event => studio.setQuery(event.target.value)} placeholder="Search this NPC…" /></label>
    <div className="nav-section-head"><span>NPC LIBRARY</span><small>{ws?.dialogueFiles.length ?? 0}</small></div>
    <select className="npc-picker" value={studio.filePath ?? ''} onChange={event => studio.selectFile(event.target.value)}>
      {ws?.dialogueFiles.map(candidate => <option key={candidate.path} value={candidate.path}>{titleCase(candidate.path.split(/[\\/]/).at(-1) ?? candidate.path)}</option>)}
    </select>
    <div className="nav-section-head"><span>DIALOGUE TREES</span><span className="nav-head-actions"><button title="Create blank tree" onClick={studio.createTree}>＋</button><button title="Duplicate selected tree" onClick={studio.duplicateTree}>⧉</button><small>{trees.length}</small></span></div>
    <div className="tree-nav-list">{trees.map(tree => <button key={treeIdentity(tree)} className={studio.treeKey === treeIdentity(tree) ? 'active' : ''} onClick={() => studio.selectTree(tree.method, tree.variant, tree.paramSource)}>
      <span className={`tree-icon ${tree.dispatchOnly ? 'router' : ''}`}>{tree.dispatchOnly ? '↯' : '⌘'}</span>
      <span><strong>{titleCase(tree.method)}{tree.variant !== undefined ? ` · ${tree.variant + 1}` : ''}</strong><small>{tree.dispatchOnly ? 'Java router' : `${tree.nodes.length} nodes · starts ${tree.startNode ?? 'unset'}`}</small></span>
    </button>)}</div>
    {file && <><div className="nav-section-head"><span>NODES</span><small>{activeContext(studio)?.tree.nodes.length ?? 0}</small></div>
      <div className="node-nav-list">{activeContext(studio)?.tree.nodes.map(node => <button key={node.id} className={studio.nodeId === node.id ? 'active' : ''} onClick={() => studio.selectNode(node.id)}><span>{node.id}</span><small>{node.options.length}</small></button>)}</div></>}
  </aside>;
}

function Inspector() {
  const studio = useStudio();
  const ctx = activeContext(studio);
  const node = activeNode(studio);
  const simNode = ctx?.tree.nodes.find(candidate => candidate.id === studio.simulator?.currentNodeId);
  const translations = studio.draft?.workspace.translations ?? {};
  const metadataSlug = (ctx?.file.path.split(/[\\/]/).at(-1) ?? '').replace(/DialogueTrees\.java$/, '');
  const metadata = studio.draft?.workspace.metadata?.[metadataSlug];
  return <aside className="right-panel">
    <div className="right-tabs"><button className={studio.rightTab === 'edit' ? 'active' : ''} onClick={() => studio.setRightTab('edit')}>Edit</button><button className={studio.rightTab === 'simulate' ? 'active' : ''} onClick={() => { studio.setRightTab('simulate'); if (!studio.simulator) studio.startSimulation(); }}>Play-through</button></div>
    {studio.rightTab === 'edit' ? node && ctx ? <div className="inspector-content">
      <div className="panel-eyebrow">SELECTED NODE</div><h2>{node.id}</h2><p className="panel-copy">{node.lines.map(key => translations[key]).filter(Boolean).join(' ')}</p>
      <div className="metric-row"><span><b>{node.lines.length}</b> lines</span><span><b>{node.options.length}</b> responses</span><span><b>{ctx.tree.startNode === node.id ? 'Yes' : 'No'}</b> entry</span></div>
      <section className="property-section"><h3>Advanced</h3><label>Node identifier<input defaultValue={node.id} onBlur={event => { const next = event.currentTarget.value.trim(); if (next && next !== node.id) studio.renameNode(node.id, next); }} /></label><label>Tree method<input value={ctx.tree.method} readOnly /></label><label>Theme<input value={ctx.tree.theme} readOnly /></label></section>
      {node.options.length > 0 && <section className="property-section trigger-section"><h3>Response triggers</h3>{node.options.map((option, index) => {
        const key = `${treeIdentity(ctx.tree)}::${node.id}::${index}`;
        const legacyKey = `${ctx.tree.method}${ctx.tree.variant !== undefined ? `::${ctx.tree.variant}` : ''}::${node.id}::${index}`;
        const value = metadata?.options[key] ?? metadata?.options[legacyKey] ?? {};
        return <div className="trigger-card" key={`${option.text}-${index}`}><strong>Response {index + 1}</strong><label>Animation<input defaultValue={value.animationTrigger ?? ''} placeholder="e.g. alchemist_nod" onBlur={event => studio.updateTrigger(node.id, index, 'animationTrigger', event.currentTarget.value.trim())} /></label><label>Sound<input defaultValue={value.soundTrigger ?? ''} placeholder="hemomancy:npc/..." onBlur={event => studio.updateTrigger(node.id, index, 'soundTrigger', event.currentTarget.value.trim())} /></label></div>;
      })}</section>}
      <button className="danger-action" onClick={() => studio.removeNode(node.id)}>Delete node</button>
      {!ctx.tree.dispatchOnly && <button className="danger-action secondary" onClick={() => { if (confirm(`Delete the entire ${ctx.tree.method} tree?`)) studio.removeTree(); }}>Delete tree</button>}
    </div> : <Empty title="Nothing selected" body="Select a node card to inspect its properties." />
    : <div className="simulator">
      <div className="sim-header"><div><span>LIVE PREVIEW</span><strong>{titleCase(ctx?.file.speaker ?? 'Conversation')}</strong></div><button onClick={studio.startSimulation}>Restart</button></div>
      {simNode ? <><div className="sim-history">{studio.simulator?.history.map((id, index) => <span key={`${id}-${index}`}>{id}</span>)}</div>
        <div className="sim-speech">{simNode.lines.map(key => <p key={key}>{translations[key] || <em>Missing translation · {key}</em>}</p>)}</div>
        <div className="sim-choices">{studio.simulator?.ended ? <div className="conversation-ended">Conversation ended</div> : simNode.options.map((option, index) => <button key={`${option.text}-${index}`} onClick={() => studio.choose(index)}><span>{index + 1}</span>{translations[option.text] || option.text}{option.event && <small>fires {option.event}</small>}</button>)}</div>
        <div className="sim-footer"><button disabled={(studio.simulator?.history.length ?? 0) < 2} onClick={studio.simulatorBack}>← Back</button><span>{studio.simulator?.events.length ?? 0} events fired</span></div></> : <Empty title="No entry node" body="Set an entry node before running this conversation." />}
    </div>}
  </aside>;
}

function RoutesView() {
  const studio = useStudio(); const ctx = activeContext(studio); const file = ctx?.file;
  const routers: DialogueTreeModel[] = file?.trees.filter((tree: DialogueTreeModel) => tree.dispatchOnly) ?? [];
  return <div className="workspace-page"><header><span>ROUTING OVERVIEW</span><h1>{file ? titleCase(file.path.split(/[\\/]/).at(-1) ?? '') : 'Routes'}</h1><p>Java routing stays read-only here. Use these method groups to follow how runtime selection leads into authorable dialogue trees.</p></header>
    <div className="route-map">{routers.length ? routers.map((router: DialogueTreeModel) => <section className="router-group" key={treeIdentity(router)}><article className="route-card router-source"><span>JAVA ROUTER</span><strong>{router.method}</strong><small>{router.paramSource || router.params.join(', ') || 'No parameters'}</small></article><div className="route-target-list">{router.routes?.length ? router.routes.map((route, index) => {
        const target = file?.trees.find(tree => tree.method === route.method && !tree.dispatchOnly);
        return <button key={`${route.method}-${index}`} className={`route-rule ${target ? '' : 'unresolved'}`} onClick={() => target && studio.selectTree(target.method, target.variant, target.paramSource)} disabled={!target}><span>{route.condition}</span><strong>{route.method}</strong><small>{target ? `${target.nodes.length} nodes · ${target.startNode}` : 'Java helper or unresolved target'}</small></button>;
      }) : <div className="route-rule unresolved"><span>COMPLEX JAVA</span><strong>No direct return targets parsed</strong><small>Open the source method for its full conditional logic.</small></div>}</div></section>) : <p className="muted">No router methods were detected.</p>}</div></div>;
}

function InquiriesView() {
  const studio = useStudio(); const ws = studio.draft?.workspace; const file = ws?.dialogueFiles.find(candidate => candidate.path === studio.filePath);
  const slug = (file?.path.split(/[\\/]/).at(-1) ?? '').replace(/DialogueTrees\.java$/, '').replace(/^Harbinger/, '').toLowerCase();
  const entries = ws?.inquiries.filter(entry => slug.includes(entry.npcId) || entry.npcId.includes(slug)) ?? [];
  return <div className="workspace-page inquiries-page"><header><span>ITEM INQUIRIES</span><h1>Inspectable knowledge</h1><p>Readable item responses for the selected NPC, with their resource paths kept as secondary detail.</p></header>
    <div className="inquiry-table"><div className="inquiry-table-head"><span>Item or block</span><span>Dialogue</span><span>Status</span></div>{entries.map(entry => <article key={entry.path}><div><strong>{entry.itemId}</strong><small>{entry.path}</small></div><div>{entry.lines.map(key => <textarea key={key} defaultValue={ws?.translations[key] ?? ''} onBlur={event => studio.updateInquiryLine(key, event.currentTarget.value)} />)}</div><span className={entry.valid ? 'status-good' : 'status-bad'}>{entry.valid ? 'Ready' : 'Invalid'}</span></article>)}{!entries.length && <Empty title="No inquiries mapped" body="This NPC currently has no item inquiry resources." />}</div></div>;
}

function ChangesView() {
  const studio = useStudio(); const draft = studio.draft; if (!draft) return null; const summary = pendingSummary(draft);
  const diagnostics = studio.preview?.diagnostics ?? draft.workspace.diagnostics;
  return <div className="workspace-page changes-page"><header><span>WORKSPACE DRAFT</span><h1>{summary.total ? `${summary.total} pending change groups` : 'No pending changes'}</h1><p>Review the readable summary first. Raw Java, language, inquiry, and metadata patches remain available below after preview.</p></header>
    <div className="change-metrics"><article><strong>{summary.dialogueFiles}</strong><span>Affected NPCs</span></article><article><strong>{summary.translations}</strong><span>Prose edits</span></article><article><strong>{summary.inquiries}</strong><span>Inquiries</span></article><article><strong>{summary.metadata}</strong><span>Triggers & layout</span></article><article><strong>{diagnostics.length}</strong><span>Issues</span></article></div>
    <section className="diagnostic-list"><h2>Validation</h2>{diagnostics.length ? diagnostics.map((diag, index) => <article className={diag.severity} key={`${diag.code}-${index}`}><span>{diag.severity}</span><div><strong>{diag.message}</strong><small>{[diag.file, diag.tree, diag.node].filter(Boolean).join(' · ')}</small></div></article>) : <div className="all-clear">✓ No validation issues</div>}</section>
    {studio.preview && <section className="diff-list"><h2>File patches</h2>{studio.preview.diffs.map(diff => <details key={diff.path}><summary>{diff.path}</summary><pre>{diff.patch}</pre></details>)}</section>}
  </div>;
}

function Topbar() {
  const studio = useStudio(); const summary = studio.draft ? pendingSummary(studio.draft) : null;
  async function preview() {
    if (!studio.draft) return;
    studio.setMessage('Validating workspace draft…');
    try {
      const handled = new Set(studio.draft.workspace.events.map(event => event.id));
      studio.draft.workspace.memos.forEach(memo => handled.add(`memo_capture:${memo.id}`));
      const result = await previewChanges(buildPreviewRequest(studio.draft, [...handled]));
      studio.setPreview(result); studio.setMode('changes'); studio.setMessage(!result.diffs.length ? 'No pending changes' : result.canApply ? 'Preview ready to apply' : 'Resolve blocking issues before applying');
    } catch (error) { studio.setMessage(error instanceof Error ? error.message : String(error)); }
  }
  async function apply() {
    if (!studio.preview?.canApply || !studio.preview.diffs.length) return;
    studio.setMessage('Applying reviewed changes…');
    try { await applyPreview(studio.preview.id); localStorage.removeItem('hemomancy-dialogue-studio-draft-v2'); studio.load(await loadWorkspace()); studio.setPreview(null); }
    catch (error) { studio.setMessage(error instanceof Error ? error.message : String(error)); }
  }
  return <header className="studio-topbar"><div className="crumbs"><span>{titleCase(studio.filePath?.split(/[\\/]/).at(-1) ?? 'NPC')}</span><b>/</b><strong>{titleCase(studio.treeMethod ?? 'Tree')}</strong></div>
    <nav>{(['dialogue', 'routes', 'inquiries', 'changes'] as const).map(mode => <button key={mode} className={studio.mode === mode ? 'active' : ''} onClick={() => studio.setMode(mode)}>{mode === 'dialogue' ? 'Canvas' : titleCase(mode)}{mode === 'changes' && summary?.total ? <i>{summary.total}</i> : null}</button>)}</nav>
    <div className="top-actions"><button onClick={() => dispatchEvent(new Event('studio-command'))}>⌘ K</button><button onClick={studio.undo} disabled={!studio.undoStack.length}>↶</button><button onClick={studio.redo} disabled={!studio.redoStack.length}>↷</button><button onClick={preview}>Review changes</button><button className="apply-button" disabled={!studio.preview?.canApply || !studio.preview.diffs.length} onClick={apply}>Apply</button></div></header>;
}

function CommandPalette() {
  const studio = useStudio(); const [open, setOpen] = useState(false); const [query, setQuery] = useState('');
  useEffect(() => { const show = () => { setOpen(true); setQuery(''); }; addEventListener('studio-command', show); return () => removeEventListener('studio-command', show); }, []);
  if (!open || !studio.draft) return null;
  const needle = query.trim().toLowerCase();
  const results = studio.draft.workspace.dialogueFiles.flatMap(file => file.trees.flatMap(tree => tree.nodes.map(node => ({
    file, tree, node, searchable: [file.path, tree.method, node.id, ...node.lines.map(key => studio.draft!.workspace.translations[key] ?? '')].join(' ').toLowerCase()
  })))).filter(result => !needle || result.searchable.includes(needle)).slice(0, 30);
  const openResult = (result: (typeof results)[number]) => { studio.selectFile(result.file.path); studio.selectTree(result.tree.method, result.tree.variant, result.tree.paramSource); studio.selectNode(result.node.id); setOpen(false); };
  return <div className="command-backdrop" onMouseDown={() => setOpen(false)}><section className="command-palette" onMouseDown={event => event.stopPropagation()}>
    <label><span>⌕</span><input autoFocus value={query} onChange={event => setQuery(event.target.value)} onKeyDown={event => { if (event.key === 'Escape') setOpen(false); else if (event.key === 'Enter' && results[0]) openResult(results[0]); }} placeholder="Find any NPC, tree, node, or spoken line…" /></label>
    <div>{results.map(result => <button key={`${result.file.path}-${treeIdentity(result.tree)}-${result.node.id}`} onClick={() => openResult(result)}><span><strong>{result.node.id}</strong><small>{titleCase(result.file.path.split(/[\\/]/).at(-1) ?? '')} · {titleCase(result.tree.method)}</small></span><p>{result.node.lines.map(key => studio.draft!.workspace.translations[key]).filter(Boolean).join(' ')}</p></button>)}{!results.length && <div className="no-results">No matching dialogue found.</div>}</div>
    <footer><span>Enter to open</span><span>Esc to close</span><span>{results.length} results</span></footer>
  </section></div>;
}

export function StudioApp() {
  const studio = useStudio();
  useEffect(() => { loadWorkspace().then(studio.load).catch(error => studio.setMessage(error instanceof Error ? error.message : String(error))); }, []);
  useEffect(() => { const handler = (event: KeyboardEvent) => { if (!(event.ctrlKey || event.metaKey)) return; if (event.key.toLowerCase() === 'z') { event.preventDefault(); event.shiftKey ? studio.redo() : studio.undo(); } else if (event.key.toLowerCase() === 'k') { event.preventDefault(); dispatchEvent(new Event('studio-command')); } }; addEventListener('keydown', handler); return () => removeEventListener('keydown', handler); }, [studio.undo, studio.redo]);
  if (!studio.draft) return <div className="loading-screen"><div className="brand-mark">H</div><strong>{studio.message}</strong></div>;
  return <div className="studio-shell"><Navigator /><main className="studio-main"><Topbar /><div className="mode-content">{studio.mode === 'dialogue' ? <ReactFlowProvider><DialogueCanvas /></ReactFlowProvider> : studio.mode === 'routes' ? <RoutesView /> : studio.mode === 'inquiries' ? <InquiriesView /> : <ChangesView />}</div><footer className="statusbar"><span><i></i>{studio.message}</span><span>{studio.draft.workspace.diagnostics.length} workspace issues</span></footer></main><Inspector /><CommandPalette />
    <datalist id="event-catalog">{studio.draft.workspace.events.map(event => <option key={event.id} value={event.id} />)}</datalist></div>;
}
