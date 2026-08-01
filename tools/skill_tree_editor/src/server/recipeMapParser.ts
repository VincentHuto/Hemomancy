import type {
  Diagnostic,
  RecipeMapEditorEntry,
  RecipeMapEditorLink,
  RecipeMapEditorTab,
  RecipeMapTabKey
} from '../shared/types';

const startMarker = '// <recipe-map-editor>';
const endMarker = '// </recipe-map-editor>';
const miscFamily = 'Miscellaneous';

export function parseRecipeMapDefinitionsJava(path: string, source: string): {
  tabs: RecipeMapEditorTab[];
  diagnostics: Diagnostic[];
} {
  const diagnostics: Diagnostic[] = [];
  const region = editorRegion(source);
  if (!region) {
    diagnostics.push({
      severity: 'error',
      code: 'recipe_map_editor_region_missing',
      message: 'Recipe map definition editor markers are missing.',
      file: path
    });
  }
  const body = region?.body ?? source;
  const tabs: RecipeMapEditorTab[] = [
    createTab('RITES', parseFamilies(source, 'RITE_FAMILIES')),
    createTab('CRAFTING', parseFamilies(source, 'CRAFTING_FAMILIES'))
  ];

  for (const match of body.matchAll(/register(Rites|Crafting|Puppetry)\(\s*"([^"]+)"\s*((?:,\s*"[^"]+")*)\s*\)\s*;/g)) {
    const method = match[1];
    const family = match[2];
    const ids = [...match[3].matchAll(/"([^"]+)"/g)].map(id => id[1]);
    const tab = method === 'Rites' ? tabs[0] : tabs[1];
    const prefix = method === 'Rites' ? 'cardinal_rite/' : method === 'Puppetry' ? 'puppeteer_trial/' : 'blood_structure/';
    for (const id of ids) {
      const fullId = id.includes('/') ? id : prefix + id;
      tab.entries.push({
        id: fullId,
        family,
        order: tab.entries.filter(entry => entry.family === family).length,
        column: 0,
        displayName: labelize(id.substring(id.lastIndexOf('/') + 1))
      });
    }
  }

  for (const match of body.matchAll(/link(Rites|Crafting)\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*RecipeMapLink\.Kind\.(PROGRESSION|CONCEPTUAL)\s*\)\s*;/g)) {
    const tab = match[1] === 'Rites' ? tabs[0] : tabs[1];
    tab.links.push({ from: match[2], to: match[3], kind: match[4] as RecipeMapEditorLink['kind'] });
  }

  return { tabs, diagnostics };
}

export function renderRecipeMapDefinitionsJava(source: string, tabs: RecipeMapEditorTab[]): string {
  const region = editorRegion(source);
  if (!region) return source;
  const current = parseRecipeMapDefinitionsJava('', source).tabs;
  if (sameAuthoredModel(current, tabs)) return source;
  const lines: string[] = [];
  for (const tab of tabs) {
    for (const family of tab.families.filter(candidate => candidate !== miscFamily)) {
      const entries = tab.entries
        .filter(entry => entry.family === family)
        .sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
      if (!entries.length) continue;
      const groups = tab.key === 'RITES'
        ? [{ method: 'registerRites', prefix: 'cardinal_rite/', entries }]
        : [
          { method: 'registerCrafting', prefix: 'blood_structure/', entries: entries.filter(entry => entry.id.startsWith('blood_structure/')) },
          { method: 'registerPuppetry', prefix: 'puppeteer_trial/', entries: entries.filter(entry => entry.id.startsWith('puppeteer_trial/')) }
        ];
      for (const group of groups) {
        if (!group.entries.length) continue;
        const ids = group.entries.map(entry => `"${entry.id.slice(group.prefix.length)}"`).join(', ');
        lines.push(`\t\t${group.method}("${escapeJava(family)}", ${ids});`);
      }
    }
    for (const link of tab.links) {
      const method = tab.key === 'RITES' ? 'linkRites' : 'linkCrafting';
      lines.push(`\t\t${method}("${escapeJava(link.from)}", "${escapeJava(link.to)}", RecipeMapLink.Kind.${link.kind});`);
    }
    lines.push('');
  }
  if (lines.at(-1) === '') lines.pop();
  const replacement = `${startMarker}\n${lines.join('\n')}\n\t\t${endMarker}`;
  let rendered = source.slice(0, region.start) + replacement + source.slice(region.end);
  for (const tab of tabs) {
    const constant = tab.key === 'RITES' ? 'RITE_FAMILIES' : 'CRAFTING_FAMILIES';
    const values = tab.families.filter(family => family !== miscFamily).map(family => `"${escapeJava(family)}"`);
    values.push('RecipeMapLayout.MISC_FAMILY');
    rendered = rendered.replace(
      new RegExp(`(${constant}\\s*=\\s*)List\\.of\\(([\\s\\S]*?)\\);`),
      `$1List.of(${values.join(', ')});`
    );
  }
  return rendered;
}

function sameAuthoredModel(left: RecipeMapEditorTab[], right: RecipeMapEditorTab[]): boolean {
  const authored = (models: RecipeMapEditorTab[]) => models.map(tab => ({
    key: tab.key,
    families: tab.families,
    entries: tab.entries.map(entry => ({ id: entry.id, family: entry.family, order: entry.order })),
    links: tab.links
  }));
  return JSON.stringify(authored(left)) === JSON.stringify(authored(right));
}

function createTab(key: RecipeMapTabKey, families: string[]): RecipeMapEditorTab {
  return { key, families, entries: [], links: [] };
}

function parseFamilies(source: string, constant: string): string[] {
  const match = new RegExp(`${constant}\\s*=\\s*List\\.of\\(([\\s\\S]*?)\\);`).exec(source);
  const values = match ? [...match[1].matchAll(/"([^"]+)"/g)].map(value => value[1]) : [];
  if (!values.includes(miscFamily)) values.push(miscFamily);
  return values;
}

function editorRegion(source: string): { start: number; end: number; body: string } | null {
  const start = source.indexOf(startMarker);
  const close = source.indexOf(endMarker, start + startMarker.length);
  if (start < 0 || close < 0) return null;
  return {
    start,
    end: close + endMarker.length,
    body: source.slice(start + startMarker.length, close)
  };
}

function labelize(value: string): string {
  return value.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
}

function escapeJava(value: string): string {
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}
