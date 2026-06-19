import { existsSync, mkdirSync, mkdtempSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { describe, expect, test } from 'vitest';

import { previewWorkspaceChanges, applyPreview, loadMetadata, saveMetadata } from './workspace';
import type { NpcMetadata } from '../shared/types';

describe('workspace preview/apply', () => {
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
