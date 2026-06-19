import { beforeEach, describe, expect, test } from 'vitest';
import { pushUndo, redo, state, undo } from './state';

function resetState(): void {
  state.workspace = null;
  state.dirtyTranslations = {};
  state.dirtyInquiries = new Map();
  state.createdInquiryPaths = new Set();
  state.newEvents = new Set();
  state.metadata = {};
  state.preview = null;
  state.undoStack = [];
  state.redoStack = [];
}

describe('undo/redo', () => {
  beforeEach(resetState);

  test('pushUndo snapshots dirtyTranslations and clears redoStack', () => {
    state.dirtyTranslations = { 'key.a': 'hello' };
    state.redoStack = [{ workspace: null, dirtyTranslations: {}, dirtyInquiries: [], createdInquiryPaths: [], newEvents: [], metadata: {} }];
    pushUndo();
    expect(state.undoStack).toHaveLength(1);
    expect(state.undoStack[0].dirtyTranslations).toEqual({ 'key.a': 'hello' });
    expect(state.redoStack).toHaveLength(0);
    expect(state.preview).toBeNull();
  });

  test('undo restores previous state and pushes current to redoStack', () => {
    state.dirtyTranslations = { 'key.a': 'before' };
    pushUndo();
    state.dirtyTranslations = { 'key.a': 'after' };
    const result = undo();
    expect(result).toBe(true);
    expect(state.dirtyTranslations).toEqual({ 'key.a': 'before' });
    expect(state.undoStack).toHaveLength(0);
    expect(state.redoStack).toHaveLength(1);
  });

  test('redo re-applies undone state', () => {
    state.dirtyTranslations = { 'key.a': 'before' };
    pushUndo();
    state.dirtyTranslations = { 'key.a': 'after' };
    undo();
    const result = redo();
    expect(result).toBe(true);
    expect(state.dirtyTranslations).toEqual({ 'key.a': 'after' });
    expect(state.undoStack).toHaveLength(1);
    expect(state.redoStack).toHaveLength(0);
  });

  test('undo returns false when stack is empty', () => {
    expect(undo()).toBe(false);
  });

  test('redo returns false when stack is empty', () => {
    expect(redo()).toBe(false);
  });

  test('new edit (pushUndo) clears redoStack', () => {
    state.dirtyTranslations = { 'k': '1' };
    pushUndo();
    state.dirtyTranslations = { 'k': '2' };
    undo();
    expect(state.redoStack).toHaveLength(1);
    pushUndo();
    expect(state.redoStack).toHaveLength(0);
  });

  test('undoStack is capped at 50 entries, oldest dropped', () => {
    for (let i = 0; i < 55; i++) {
      state.dirtyTranslations = { key: String(i) };
      pushUndo();
    }
    expect(state.undoStack).toHaveLength(50);
    expect(state.undoStack[0].dirtyTranslations['key']).toBe('5');
  });

  test('snapshot deep-clones workspace so later mutations do not affect history', () => {
    state.workspace = {
      repoRoot: '/repo',
      dialogueFiles: [{ path: 'a.java', sourceFile: 'a.java', speaker: 'A', icon: '', trees: [], diagnostics: [] }],
      translations: {},
      inquiries: [],
      registries: [],
      events: [],
      memos: [],
      diagnostics: []
    };
    pushUndo();
    state.workspace.dialogueFiles[0].path = 'mutated.java';
    expect(state.undoStack[0].workspace!.dialogueFiles[0].path).toBe('a.java');
  });

  test('Maps and Sets are serialized and restored correctly', () => {
    state.dirtyInquiries = new Map([
      ['path/a.json', { path: 'path/a.json', npcId: 'alchemist', itemId: 'hemomancy/ore', lines: ['k1'], valid: true }]
    ]);
    state.newEvents = new Set(['my_event']);
    state.createdInquiryPaths = new Set(['path/a.json']);
    pushUndo();
    state.dirtyInquiries = new Map();
    state.newEvents = new Set();
    state.createdInquiryPaths = new Set();
    undo();
    expect(state.dirtyInquiries.get('path/a.json')?.npcId).toBe('alchemist');
    expect(state.newEvents.has('my_event')).toBe(true);
    expect(state.createdInquiryPaths.has('path/a.json')).toBe(true);
  });

  test('metadata is deep-cloned and restored', () => {
    state.metadata = { Acolyte: { version: 1, options: { 'method::0::node::0': { animationTrigger: 'kneel' } } } };
    pushUndo();
    state.metadata = {};
    undo();
    expect(state.metadata['Acolyte']?.options['method::0::node::0']?.animationTrigger).toBe('kneel');
  });
});
