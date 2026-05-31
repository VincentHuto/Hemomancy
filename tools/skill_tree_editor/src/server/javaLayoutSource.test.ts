import { readdirSync, readFileSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';
import { parseSkillBranchJava } from './skillParser';

const repoRoot = resolve(process.cwd(), '..', '..');
const compassCenter = { x: 480, y: 480 };
const degreeRadii = new Map<number, number>([
  [0, 72],
  [1, 120],
  [2, 190],
  [3, 260],
  [4, 330],
  [5, 400]
]);
const branchDirections = new Map<string, { x: number; y: number }>([
  ['core', { x: 0, y: -1 }],
  ['base', { x: 0, y: -1 }],
  ['living_staff', { x: -1, y: 0 }],
  ['summons', { x: 1, y: 0 }],
  ['scars', { x: 0, y: 1 }]
]);
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

test('authored java skill positions line up with compass degree rings', () => {
  const branchRoot = resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills');
  const parsedBranches = readdirSync(branchRoot)
    .filter(name => name.endsWith('SkillBranch.java'))
    .map(name => {
      const absPath = join(branchRoot, name);
      const source = readFileSync(absPath, 'utf8');
      const relPath = relative(repoRoot, absPath).replaceAll('\\', '/');
      return parseSkillBranchJava(relPath, source);
    });
  const mismatches = parsedBranches.flatMap(branch => branch.skills
    .filter(skill => skill.treeX !== null && skill.treeY !== null)
    .filter(skill => {
      const direction = branchDirections.get(skill.branch)!;
      const actual = Math.round((skill.treeX! - compassCenter.x) * direction.x + (skill.treeY! - compassCenter.y) * direction.y);
      return actual !== expectedCompassRadius(skill);
    })
    .map(skill => `${skill.field}: x=${skill.treeX}, y=${skill.treeY}, branch=${skill.branch}, degree=${skill.requiredDegree}`));

  expect(mismatches).toEqual([]);
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
  const branchRoot = resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills');
  return readdirSync(branchRoot)
    .filter(name => name.endsWith('SkillBranch.java'))
    .flatMap(name => {
      const absPath = join(branchRoot, name);
      const source = readFileSync(absPath, 'utf8');
      const relPath = relative(repoRoot, absPath).replaceAll('\\', '/');
      return parseSkillBranchJava(relPath, source).skills;
    });
}

function expectedCompassRadius(skill: { branch: string; parentField: string | null; requiredDegree: number }): number {
  if ((skill.branch === 'core' || skill.branch === 'base') && !skill.parentField && skill.requiredDegree === 0) return 0;
  return degreeRadii.get(skill.requiredDegree) ?? degreeRadii.get(5)! + (skill.requiredDegree - 5) * 70;
}
