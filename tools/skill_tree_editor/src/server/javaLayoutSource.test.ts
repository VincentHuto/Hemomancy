import { readdirSync, readFileSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';
import { parseSkillBranchJava } from './skillParser';

const repoRoot = resolve(process.cwd(), '..', '..');
const compassCenter = { x: 480, y: 480 };
const maxInGameLayoutWidth = 820;
const maxInGameLayoutHeight = 820;

test('java skill points expose editable in-game tree coordinates', () => {
  const source = read('src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPoint.java');

  expect(source).toContain('setTreePosition(int x, int y)');
  expect(source).toContain('hasTreePosition()');
  expect(source).toContain('getTreeX()');
  expect(source).toContain('getTreeY()');
});

test('in-game skills tab uses explicit skill positions when present', () => {
  const source = read('src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java');

  expect(source).toContain('sp.hasTreePosition()');
  expect(source).toContain('sp.getTreeX()');
  expect(source).toContain('sp.getTreeY()');
});

test('in-game skills tab uses explicit degree label positions when present', () => {
  const init = read('src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java');
  const source = read('src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java');

  expect(init).toContain('setDegreeLabelPosition(int degree, int x, int y)');
  expect(init).toContain('getDegreeLabelPosition(int degree)');
  expect(source).toContain('SkillPointInit.getDegreeLabelPosition(degree)');
});

test('authored java skill positions keep branches separated directionally', () => {
  const skills = javaBranchSkills()
    .filter(skill => skill.treeX !== null && skill.treeY !== null);
  const byField = new Map(skills.map(skill => [skill.field, skill]));

  expect(byField.get('base_skill')).toEqual(expect.objectContaining({ treeX: compassCenter.x, treeY: compassCenter.y }));
  expect(skills.filter(skill => skill.branch === 'living_staff').every(skill => skill.treeX! < compassCenter.x)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'summons').every(skill => skill.treeX! > compassCenter.x)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'scars').every(skill => skill.treeY! > compassCenter.y)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'core' && skill.field !== 'base_skill').every(skill => skill.treeY! < compassCenter.y)).toBe(true);
});

test('authored java skill declarations carry branch metadata for in-game trace colors', () => {
  for (const branch of javaBranches()) {
    expect(branch.source).toContain(`.setBranch("${branch.branch}")`);
    expect(branch.source).toContain('.setBranchColor(0xFF');
  }
});

test('authored java skill positions stay compact enough for the in-game skill screen', () => {
  const positions = javaBranchSkills()
    .filter(skill => skill.treeX !== null && skill.treeY !== null)
    .map(skill => ({ x: skill.treeX!, y: skill.treeY! }));
  const minX = Math.min(...positions.map(position => position.x));
  const maxX = Math.max(...positions.map(position => position.x));
  const minY = Math.min(...positions.map(position => position.y));
  const maxY = Math.max(...positions.map(position => position.y));

  expect(maxX - minX).toBeLessThanOrEqual(maxInGameLayoutWidth);
  expect(maxY - minY).toBeLessThanOrEqual(maxInGameLayoutHeight);
});

function read(path: string): string {
  return readFileSync(resolve(repoRoot, path), 'utf8');
}

function javaBranchSkills() {
  return javaBranches().flatMap(branch => branch.skills);
}

function javaBranches() {
  const branchRoot = resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills');
  return readdirSync(branchRoot)
    .filter(name => name.endsWith('SkillBranch.java'))
    .map(name => {
      const absPath = join(branchRoot, name);
      const source = readFileSync(absPath, 'utf8');
      const relPath = relative(repoRoot, absPath).replaceAll('\\', '/');
      return parseSkillBranchJava(relPath, source);
    });
}
