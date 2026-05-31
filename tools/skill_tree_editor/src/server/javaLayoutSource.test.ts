import { readdirSync, readFileSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';
import { parseSkillBranchJava } from './skillParser';

const repoRoot = resolve(process.cwd(), '..', '..');
const degreeStartY = 64;
const degreeStepY = 72;
const minimumMaxDegree = 5;
const maxInGameLayoutWidth = 720;
const maxInGameLayoutHeight = 400;

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

test('authored java skill positions line up with required degree tiers', () => {
  const branchRoot = resolve(repoRoot, 'src/main/java/com/vincenthuto/hemomancy/common/init/skills');
  const parsedBranches = readdirSync(branchRoot)
    .filter(name => name.endsWith('SkillBranch.java'))
    .map(name => {
      const absPath = join(branchRoot, name);
      const source = readFileSync(absPath, 'utf8');
      const relPath = relative(repoRoot, absPath).replaceAll('\\', '/');
      return parseSkillBranchJava(relPath, source);
    });
  const maxDegree = Math.max(minimumMaxDegree, ...parsedBranches.flatMap(branch => branch.skills.map(skill => skill.requiredDegree)));
  const mismatches = parsedBranches.flatMap(branch => branch.skills
    .filter(skill => skill.treeY !== null)
    .filter(skill => skill.treeY !== degreeStartY + (maxDegree - skill.requiredDegree) * degreeStepY)
    .map(skill => `${skill.field}: y=${skill.treeY}, degree=${skill.requiredDegree}`));

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
