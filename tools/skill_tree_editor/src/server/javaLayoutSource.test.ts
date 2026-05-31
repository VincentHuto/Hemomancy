import { readdirSync, readFileSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';
import { parseSkillBranchJava } from './skillParser';

const repoRoot = resolve(process.cwd(), '..', '..');
const compassCenter = { x: 480, y: 480 };
const maxInGameLayoutWidth = 960;
const maxInGameLayoutHeight = 960;

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
  expect(byField.get('deep_base_skill')).toEqual(expect.objectContaining({ treeX: compassCenter.x, treeY: compassCenter.y }));
  expect(skills.filter(skill => skill.branch === 'living_staff').every(skill => skill.treeX! < compassCenter.x)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'summons').every(skill => skill.treeX! > compassCenter.x)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'scars').every(skill => skill.treeY! > compassCenter.y)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'core' && !['base_skill', 'deep_base_skill'].includes(skill.field)).every(skill => skill.treeY! < compassCenter.y)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'covenant').every(skill => skill.treeX! > compassCenter.x && skill.treeY! < compassCenter.y)).toBe(true);
  expect(skills.filter(skill => skill.branch === 'mycelial').every(skill => skill.treeX! < compassCenter.x && skill.treeY! > compassCenter.y)).toBe(true);
});

test('authored java skill tree spans all eight harbinger degrees and six branches', () => {
  const branchNames = new Set(javaBranches().map(branch => branch.branch));
  const degrees = new Set(javaBranchSkills().map(skill => skill.requiredDegree));
  const init = read('src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java');

  expect(branchNames).toEqual(new Set(['core', 'living_staff', 'scars', 'summons', 'covenant', 'mycelial']));
  expect([...degrees].sort((a, b) => a - b)).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8]);
  expect(init).toContain('CovenantSkillBranch.register(BASE)');
  expect(init).toContain('MycelialSkillBranch.register(BASE)');
});

test('deep skills share a degree-five deep root without losing branch prerequisites', () => {
  const skills = javaBranchSkills();
  const byField = new Map(skills.map(skill => [skill.field, skill]));
  const deepBase = byField.get('deep_base_skill');

  expect(deepBase).toEqual(expect.objectContaining({
    name: 'deep_base',
    requiredDegree: 5,
    parentFields: []
  }));

  for (const skill of skills.filter(item => item.requiredDegree >= 5 && item.field !== 'deep_base_skill')) {
    expect(skill.parentFields).toContain('deep_base_skill');
    expect(skill.parentFields.some(parent => parent !== 'deep_base_skill')).toBe(true);
  }
});

test('authored java skill branches save late-game branch colors', () => {
  const covenant = javaBranches().find(branch => branch.branch === 'covenant')!;
  const mycelial = javaBranches().find(branch => branch.branch === 'mycelial')!;

  expect(covenant.source).toContain('.setBranchColor(0xFFA54569)');
  expect(mycelial.source).toContain('.setBranchColor(0xFF6E8F3A)');
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
