import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, test } from 'vitest';

import { extractEventCatalog, extractEventConstants, extractMemoCatalog, parseDialogueJava, renderDialogueFile } from './dialogueParser';
import { validateDialogueFile } from './validation';

const repoRoot = resolve(__dirname, '..', '..', '..', '..');
const dialogueDir = resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue');

function readDialogue(name: string): string {
  return readFileSync(resolve(dialogueDir, name), 'utf8');
}

describe('dialogue parser', () => {
  test('parses public and private DialogueTree builders as editable trees', () => {
    const file = parseDialogueJava('AncestralCommunionDialogueTrees.java', readDialogue('AncestralCommunionDialogueTrees.java'));

    expect(file.trees.map(tree => tree.method)).toContain('forVariant');
    expect(file.trees.map(tree => tree.method)).toContain('communionVariant0');
    expect(file.trees.find(tree => tree.method === 'communionVariant0')?.visibility).toBe('private');
    expect(file.trees.find(tree => tree.method === 'communionVariant0')?.nodes[0].options[0].next).toBe('origin');
  });

  test('preserves memo event expressions instead of treating them as string literals', () => {
    const file = parseDialogueJava('HarbingerHermitDialogueTrees.java', readDialogue('HarbingerHermitDialogueTrees.java'));
    const option = file.trees.flatMap(tree => tree.nodes).flatMap(node => node.options)
      .find(opt => opt.event?.includes('MemoHelper.memoEvent'));

    expect(option).toBeDefined();
    expect(option?.eventExpression).toBe(true);
  });

  test('resolves helper methods that return reusable DialogueNode instances', () => {
    const file = parseDialogueJava('HarbingerMnemonistDialogueTrees.java', readDialogue('HarbingerMnemonistDialogueTrees.java'));
    const purifying = file.trees.find(tree => tree.method === 'purifying');
    const nodeIds = purifying?.nodes.map(node => node.id) ?? [];

    expect(nodeIds).toContain('crude_memories');
    expect(nodeIds).toContain('item_hint');
    expect(nodeIds).not.toContain('crudeMemoriesNode');
    expect(nodeIds).not.toContain('itemHintNode');
  });

  test('reports broken next links and duplicate node IDs', () => {
    const file = parseDialogueJava('GuardianDialogueTrees.java', readDialogue('GuardianDialogueTrees.java'));
    file.trees[0].nodes.push({
      id: file.trees[0].nodes[0].id,
      lines: ['hemomancy.test.missing'],
      options: [{ text: 'hemomancy.test.option', next: 'missing_node', event: null }]
    });

    const diagnostics = validateDialogueFile(file, {}, new Set());

    expect(diagnostics.some(diag => diag.code === 'duplicate_node_id')).toBe(true);
    expect(diagnostics.some(diag => diag.code === 'broken_next')).toBe(true);
    expect(diagnostics.some(diag => diag.code === 'missing_translation')).toBe(true);
  });

  test('inserts newly authored builder methods and removes deleted concrete methods', () => {
    const source = `public final class Demo {\n  private static final String SPEAKER = "npc";\n  public static DialogueTree first(int entityId) {\n    return DialogueTree.builder(SPEAKER, ICON, entityId)\n      .addNode(new DialogueNode("root", List.of("demo.root"), List.of()))\n      .build();\n  }\n}\n`;
    const file = parseDialogueJava('Demo.java', source);
    const copy = structuredClone(file.trees[0]);
    copy.method = 'second'; copy.sourceMethod = undefined; copy.sourceSpan = undefined; copy.methodSourceSpan = undefined; copy.newTree = true;
    file.trees = [copy];
    const rendered = renderDialogueFile(source, file);
    expect(rendered).not.toContain('DialogueTree first(');
    expect(rendered).toContain('DialogueTree second(int entityId)');
    expect(rendered).toContain('"demo.root"');
  });

  test('extracts readable route targets from switch routers', () => {
    const source = `class Demo {\n public static DialogueTree forDegree(int degree) {\n  return switch (degree) {\n   case 0 -> novice(0);\n   case 1 -> adept(0);\n   default -> archon(0);\n  };\n }\n}`;
    const router = parseDialogueJava('Demo.java', source).trees[0];
    expect(router.routes).toEqual([
      { method: 'novice', condition: 'case 0' },
      { method: 'adept', condition: 'case 1' },
      { method: 'archon', condition: 'default' }
    ]);
  });
});

describe('event catalogs', () => {
  test('extracts handled dialogue events and memo definitions from Java sources', () => {
    const handler = readFileSync(resolve(dialogueDir, 'DialogueEventHandler.java'), 'utf8');
    const mnemonistTree = readFileSync(resolve(dialogueDir, 'HarbingerMnemonistDialogueTrees.java'), 'utf8');
    const memoDefinitions = readFileSync(resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/MemoDefinitions.java'), 'utf8');
    const eventConstants = extractEventConstants('HarbingerMnemonistDialogueTrees.java', mnemonistTree);

    const events = extractEventCatalog(handler, eventConstants);
    const memos = extractMemoCatalog(memoDefinitions);

    expect(events.map(event => event.id)).toContain('recruit_harbinger');
    expect(events.map(event => event.id)).toContain('archon_choice_eighth_degree');
    expect(events.map(event => event.id)).toContain('mnemonist_woven_vessel_turn_in');
    expect(memos.map(memo => memo.id)).toContain('hemomancy:fungal_whisper_truth');
    expect(memos.find(memo => memo.constant === 'FIRST_RITE_NOTES')?.path).toBe('HARBINGER');
  });
});
