import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { describe, expect, test } from 'vitest';

const repoRoot = resolve(__dirname, '../../../..');
const materialsData = readFileSync(resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java'), 'utf8');
const atlasSpec = readFileSync(resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialAtlasSpec.java'), 'utf8');

function activeSource(source: string): string {
  return source
    .split('\n')
    .filter(line => !line.trimStart().startsWith('//'))
    .join('\n');
}

const activeMaterialsData = activeSource(materialsData);

function materialEntryIds(): Set<string> {
  return new Set([...activeMaterialsData.matchAll(/new\s+MaterialEntry\s*\(\s*"([^"]+)"/g)].map(match => match[1]));
}

function atlasEntryIds(): Set<string> {
  return new Set([...atlasSpec.matchAll(/\bentry(?:At)?\s*\(\s*"([^"]+)"/g)].map(match => match[1]));
}

describe('material atlas registry gap policy', () => {
  test('adds representative accepted registry gap families to catalogue and atlas', () => {
    const catalogueIds = materialEntryIds();
    const atlasIds = atlasEntryIds();
    const expectedIncluded = [
      'enzyme_primer',
      'echo_of_heart',
      'vivacious_spores',
      'talaromyces_minus',
      'gourd_seeds',
      'blood_gourd_red',
      'tengu_mask',
      'chitinite_chestplate',
      'calcified_hyphae',
      'qliphoth_seed',
      'memory_of_vesper',
      'pallid_retort',
      'unstained_chestplate'
    ];

    for (const id of expectedIncluded) {
      expect(catalogueIds.has(id), `${id} should have catalogue text`).toBe(true);
      expect(atlasIds.has(id), `${id} should have an atlas node`).toBe(true);
    }
  });

  test('keeps noisy or system-owned registry entries out of the material atlas', () => {
    const catalogueIds = materialEntryIds();
    const atlasIds = atlasEntryIds();
    const expectedExcluded = [
      'spawn_egg_leech',
      'debug_showcase',
      'field_notes',
      'memory_blood_absorption',
      'blood_bolt',
      'filler_block',
      'gourd_stem',
      'potted_ghost_pipe'
    ];

    for (const id of expectedExcluded) {
      expect(catalogueIds.has(id), `${id} should not have catalogue text`).toBe(false);
      expect(atlasIds.has(id), `${id} should not have an atlas node`).toBe(false);
    }
  });

  test('declares focused buckets for manually arranging newly accepted families', () => {
    for (const bucketId of [
      'gourds_vessels',
      'spores_cultures',
      'myco_realm_blocks',
      'masks_vestments',
      'idols_fixtures',
      'qliphoth_reagents'
    ]) {
      expect(atlasSpec).toContain(`"${bucketId}"`);
    }
  });
});
