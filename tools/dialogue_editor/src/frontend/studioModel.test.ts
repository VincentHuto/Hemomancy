import { describe, expect, test } from 'vitest';
import type { DialogueFile, DialogueWorkspace } from '../shared/types';
import {
  changeNodeLine,
  createNode,
  createSimulator,
  followChoice,
  makeDraft,
  pendingSummary,
  duplicateTree,
  deleteTree,
  renameNode
  ,treeIdentity
  ,buildPreviewRequest
  ,createTree
} from './studioModel';

function workspace(): DialogueWorkspace {
  const file: DialogueFile = {
    path: 'npc.java', sourceFile: 'npc.java', speaker: 'NPC', icon: '', diagnostics: [],
    trees: [{ method: 'greeting', visibility: 'public', params: ['entityId'], theme: 'BLOOD', startNode: 'root', nodes: [
      { id: 'root', lines: ['npc.root.line1'], options: [{ text: 'npc.root.ask', next: 'answer', event: null }] },
      { id: 'answer', lines: ['npc.answer.line1'], options: [] }
    ] }]
  };
  return { repoRoot: '/repo', revision: 'r1', dialogueFiles: [file], translations: {
    'npc.root.line1': 'Welcome.', 'npc.root.ask': 'Who are you?', 'npc.answer.line1': 'A witness.'
  }, inquiries: [], registries: [], events: [], memos: [], diagnostics: [] };
}

describe('dialogue studio model', () => {
  test('edits readable prose without replacing its localization key', () => {
    const draft = makeDraft(workspace());
    changeNodeLine(draft, 'npc.java', 'greeting', 'root', 0, 'Come closer.');
    expect(draft.workspace.dialogueFiles[0].trees[0].nodes[0].lines[0]).toBe('npc.root.line1');
    expect(draft.workspace.translations['npc.root.line1']).toBe('Come closer.');
  });

  test('renaming a node updates incoming choices and the start node', () => {
    const draft = makeDraft(workspace());
    renameNode(draft, 'npc.java', 'greeting', 'root', 'entrance');
    const tree = draft.workspace.dialogueFiles[0].trees[0];
    expect(tree.startNode).toBe('entrance');
    expect(tree.nodes.map(node => node.id)).toContain('entrance');
  });

  test('creates a node with a stable generated prose key', () => {
    const draft = makeDraft(workspace());
    const node = createNode(draft, 'npc.java', 'greeting', 'reveal');
    expect(node.lines[0]).toBe('hemomancy.dialogue.npc.greeting.reveal.line1');
    expect(draft.workspace.translations[node.lines[0]]).toBe('New dialogue line');
  });

  test('simulator follows choices and records the visited path', () => {
    const tree = workspace().dialogueFiles[0].trees[0];
    const sim = followChoice(createSimulator(tree), tree, 0);
    expect(sim.currentNodeId).toBe('answer');
    expect(sim.history).toEqual(['root', 'answer']);
  });

  test('summarizes changes across files and translations', () => {
    const draft = makeDraft(workspace());
    changeNodeLine(draft, 'npc.java', 'greeting', 'root', 0, 'Changed');
    expect(pendingSummary(draft)).toMatchObject({ dialogueFiles: 1, translations: 1, total: 2 });
  });

  test('duplicates and deletes builder trees without touching router methods', () => {
    const draft = makeDraft(workspace());
    const copy = duplicateTree(draft, 'npc.java', 'greeting', 'greeting_copy');
    expect(copy.method).toBe('greeting_copy');
    expect(copy.nodes).not.toBe(draft.workspace.dialogueFiles[0].trees[0].nodes);
    deleteTree(draft, 'npc.java', 'greeting_copy');
    expect(draft.workspace.dialogueFiles[0].trees.map(tree => tree.method)).toEqual(['greeting']);
  });

  test('creates a blank authorable tree with an entry node and readable prose', () => {
    const draft = makeDraft(workspace());
    const tree = createTree(draft, 'npc.java', 'newDialogue');
    expect(tree.newTree).toBe(true);
    expect(tree.startNode).toBe('root');
    expect(draft.workspace.translations[tree.nodes[0].lines[0]]).toBe('New dialogue line');
  });

  test('tree identity distinguishes overloaded methods with the same name', () => {
    const base = workspace().dialogueFiles[0].trees[0];
    expect(treeIdentity({ ...base, paramSource: 'int entityId' })).not.toBe(treeIdentity({ ...base, paramSource: 'int entityId, HeldItem held' }));
  });

  test('preview request includes only changed resources and affected dialogue files', () => {
    const draft = makeDraft(workspace());
    expect(buildPreviewRequest(draft, [])).toMatchObject({ dialogueFiles: [], translations: {}, inquiries: [] });
    changeNodeLine(draft, 'npc.java', 'greeting', 'root', 0, 'Edited prose');
    const request = buildPreviewRequest(draft, []);
    expect(request.dialogueFiles?.map(file => file.path)).toEqual(['npc.java']);
    expect(request.translations).toEqual({ 'npc.root.line1': 'Edited prose' });
    draft.workspace.metadata = { Npc: { version: 2, options: { 'greeting::root::0': { soundTrigger: 'hemomancy:test' } } } };
    draft.original.metadata = { Npc: { version: 2, options: {} } };
    expect(buildPreviewRequest(draft, []).metadata).toEqual(draft.workspace.metadata);
  });
});
