import { existsSync, mkdirSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { describe, expect, test } from 'vitest';

import { previewWorkspaceChanges, applyPreview, loadMetadata, saveMetadata, workspaceRevision } from './workspace';
import type { DialogueFile, NpcMetadata } from '../shared/types';

describe('workspace preview/apply', () => {
  test('rejects a preview based on an out-of-date workspace revision', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const preview = await previewWorkspaceChanges(root, { baseRevision: 'stale-revision' });
    expect(preview.canApply).toBe(false);
    expect(preview.diagnostics.map(diagnostic => diagnostic.code)).toContain('workspace_changed');
  });

  test('validates edited dialogue files before allowing apply', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    writeFileSync(resolve(root, 'npc.java'), 'class Npc {}\n', 'utf8');
    const file: DialogueFile = {
      path: 'npc.java', sourceFile: 'npc.java', speaker: 'NPC', icon: '', diagnostics: [], trees: [{
        method: 'main', visibility: 'public', params: [], theme: 'BLOOD', startNode: 'root', nodes: [{
          id: 'root', lines: ['npc.root'], options: [{ text: 'npc.choice', next: 'missing', event: null }]
        }]
      }]
    };
    const preview = await previewWorkspaceChanges(root, { dialogueFiles: [file], translations: { 'npc.root': 'Root', 'npc.choice': 'Continue' } });
    expect(preview.canApply).toBe(false);
    expect(preview.diagnostics.map(diagnostic => diagnostic.code)).toContain('broken_next');
  });

  test('workspace revision changes when a source file changes', () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const source = resolve(root, 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/SampleDialogueTrees.java');
    mkdirSync(dirname(source), { recursive: true });
    writeFileSync(source, 'first', 'utf8');
    const before = workspaceRevision(root);
    writeFileSync(source, 'second', 'utf8');
    expect(workspaceRevision(root)).not.toBe(before);
  });
  test('preview produces diffs without writing files, while apply writes only a validated preview', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const target = resolve(root, 'sample.txt');
    writeFileSync(target, 'old text\n', 'utf8');

    const preview = await previewWorkspaceChanges(root, {
      files: [{ path: 'sample.txt', content: 'new text\n' }]
    });

    expect(readFileSync(target, 'utf8')).toBe('old text\n');
    expect(preview.diffs[0].path).toBe('sample.txt');
    expect(preview.diffs[0].patch).toContain('-old text');
    expect(preview.diffs[0].patch).toContain('+new text');

    await applyPreview(root, preview.id);

    expect(readFileSync(target, 'utf8')).toBe('new text\n');
  });

  test('preview can create a new dialogue inquiry JSON without touching disk first', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const path = 'src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/test_vessel.json';
    const preview = await previewWorkspaceChanges(root, {
      inquiries: [{
        path,
        npcId: 'alchemist',
        itemId: 'hemomancy/test_vessel',
        lines: ['hemomancy.alchemist.item_inquiry.test_vessel.line1'],
        valid: true
      }]
    });

    expect(existsSync(resolve(root, path))).toBe(false);
    expect(preview.diffs[0].before).toBe('');
    expect(preview.diffs[0].after).toContain('hemomancy.alchemist.item_inquiry.test_vessel.line1');

    await applyPreview(root, preview.id);

    expect(readFileSync(resolve(root, path), 'utf8')).toContain('test_vessel.line1');
  });

  test('previews sidecar metadata transactionally', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const preview = await previewWorkspaceChanges(root, { metadata: { TestNpc: { version: 2, options: { 'main::root::0': { animationTrigger: 'wave' } } } } });
    expect(preview.diffs[0].path).toBe('tools/dialogue_editor/TestNpcMetadata.json');
    expect(existsSync(resolve(root, preview.diffs[0].path))).toBe(false);
    await applyPreview(root, preview.id);
    expect(readFileSync(resolve(root, preview.diffs[0].path), 'utf8')).toContain('wave');
  });

  test('preview preserves conditional dialogue inquiry JSON gates', async () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-dialogue-'));
    const path = 'src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/test_gate.json';
    const target = resolve(root, path);
    mkdirSync(dirname(target), { recursive: true });
    writeFileSync(target, JSON.stringify({
      conditions: [
        { min_degree: 3, lines: ['hemomancy.test_gate.open'] },
        { lines: ['hemomancy.test_gate.locked'] }
      ]
    }, null, 2) + '\n', 'utf8');

    const preview = await previewWorkspaceChanges(root, {
      inquiries: [{
        path,
        npcId: 'alchemist',
        itemId: 'hemomancy/test_gate',
        lines: ['hemomancy.test_gate.open.edited', 'hemomancy.test_gate.locked'],
        conditions: [
          { min_degree: 3, lines: ['hemomancy.test_gate.open'] },
          { lines: ['hemomancy.test_gate.locked'] }
        ],
        valid: true
      }]
    });

    const after = JSON.parse(preview.diffs[0].after);
    expect(after.conditions[0].min_degree).toBe(3);
    expect(after.conditions[0].lines).toEqual(['hemomancy.test_gate.open.edited']);
    expect(after.conditions[1].lines).toEqual(['hemomancy.test_gate.locked']);
  });
});

describe('metadata round-trip', () => {
  test('loadMetadata returns empty metadata when file does not exist', () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-meta-'));
    expect(loadMetadata(root, 'TestNpc')).toEqual({ version: 1, options: {} });
  });

  test('saveMetadata writes and loadMetadata reads back correctly', () => {
    const root = mkdtempSync(join(tmpdir(), 'hemo-meta-'));
    const metadata: NpcMetadata = {
      version: 1,
      options: {
        'testTree::root::0': { animationTrigger: 'wave', soundTrigger: 'hemomancy:npc/wave' }
      }
    };
    saveMetadata(root, 'TestNpc', metadata);
    const loaded = loadMetadata(root, 'TestNpc');
    expect(loaded.options['testTree::root::0']?.animationTrigger).toBe('wave');
    expect(loaded.options['testTree::root::0']?.soundTrigger).toBe('hemomancy:npc/wave');
  });
});
