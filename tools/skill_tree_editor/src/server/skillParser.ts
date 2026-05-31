import type { DegreeLabelPosition, Diagnostic, SkillBranchFile, SkillModel } from '../shared/types';
import { argbLiteralFromHex, hexFromArgbLiteral, normalizeBranchColor } from '../shared/branchColors';
import { normalizeParentFields, parentFieldsOf } from '../shared/skillParents';

export function parseSkillBranchJava(path: string, source: string): SkillBranchFile {
  const className = /public\s+final\s+class\s+(\w+)/.exec(source)?.[1] ?? '';
  const marker = /^([ \t]*)\/\/ <skill-editor branch="([^"]+)">$/m.exec(source);
  const diagnostics: Diagnostic[] = [];
  if (!marker) {
    return {
      path,
      branch: 'unknown',
      color: normalizeBranchColor(null, 'unknown'),
      degreeLabels: [],
      className,
      source,
      skills: [],
      diagnostics: [{
        severity: 'warning',
        code: 'missing_skill_editor_marker',
        message: 'No editable skill branch marker was found.',
        file: path
      }]
    };
  }

  const footerRegex = /^([ \t]*)\/\/ <\/skill-editor>$/m;
  footerRegex.lastIndex = marker.index + marker[0].length;
  const footer = footerRegex.exec(source.slice(marker.index + marker[0].length));
  if (!footer) {
    return {
      path,
      branch: marker[2],
      color: normalizeBranchColor(null, marker[2]),
      degreeLabels: [],
      className,
      source,
      skills: [],
      diagnostics: [{
        severity: 'error',
        code: 'missing_skill_editor_end_marker',
        message: 'The editable skill branch marker is missing its closing marker.',
        file: path
      }]
    };
  }

  const sectionStart = marker.index + marker[0].length;
  const sectionEnd = marker.index + marker[0].length + footer.index;
  const section = source.slice(sectionStart, sectionEnd);
  const branch = marker[2];
  const color = parseBranchColor(section, branch);
  const degreeLabels = parseDegreeLabels(section);
  const skills = splitSkillDeclarations(section)
    .map(block => parseSkillDeclaration(block, branch))
    .filter((skill): skill is SkillModel => skill !== null);

  return {
    path,
    branch,
    color,
    degreeLabels,
    className,
    source,
    skills,
    diagnostics
  };
}

export function renderSkillBranchJava(source: string, file: SkillBranchFile): string {
  const markerRegex = /^([ \t]*)\/\/ <skill-editor branch="([^"]+)">$/m;
  const marker = markerRegex.exec(source);
  if (!marker) throw new Error(`Cannot render ${file.path}: missing skill editor marker.`);
  const footerRegex = /^([ \t]*)\/\/ <\/skill-editor>$/m;
  const footer = footerRegex.exec(source.slice(marker.index + marker[0].length));
  if (!footer) throw new Error(`Cannot render ${file.path}: missing skill editor end marker.`);

  const startLine = marker[0];
  const endLine = footer[0];
  const markerEnd = marker.index + marker[0].length;
  const footerStart = markerEnd + footer.index;
  const footerEnd = footerStart + footer[0].length;
  const renderedDegreeLabels = renderDegreeLabels(file.degreeLabels ?? [], marker[1]);
  const renderedSkills = file.skills.map(skill => renderSkillDeclaration(skill, marker[1], file.color)).join('\n');
  const renderedSection = [renderedDegreeLabels, renderedSkills].filter(Boolean).join('\n');
  return `${source.slice(0, marker.index)}${startLine}\n${renderedSection}\n${endLine}${source.slice(footerEnd)}`;
}

function splitSkillDeclarations(section: string): string[] {
  const lines = section.split(/\r?\n/);
  const blocks: string[] = [];
  let current: string[] = [];
  for (const line of lines) {
    if (/^\s*SkillPointInit\.\w+\s*=/.test(line)) {
      if (current.length) blocks.push(current.join('\n'));
      current = [line];
      continue;
    }
    if (current.length) current.push(line);
  }
  if (current.length) blocks.push(current.join('\n'));
  return blocks.map(block => block.trim()).filter(Boolean);
}

function parseSkillDeclaration(block: string, branch: string): SkillModel | null {
  const field = /SkillPointInit\.(\w+)\s*=/.exec(block)?.[1];
  const skill = /new\s+SkillPoint\(\s*(\d+)\s*,\s*"([^"]+)"\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*EnumSkillStates\.([A-Z_]+)\s*,\s*(null|SkillPointInit\.(\w+))\s*\)/s.exec(block);
  if (!field || !skill) return null;
  const treePosition = /\.setTreePosition\(\s*(-?\d+)\s*,\s*(-?\d+)\s*\)/.exec(block);
  const parentField = skill[6] === 'null' ? null : skill[7];
  const parentFields = normalizeParentFields([
    ...(parentField ? [parentField] : []),
    ...parseAdditionalParents(block)
  ], null, field);
  return {
    field,
    id: Number(skill[1]),
    name: skill[2],
    branch,
    bloodCost: Number(skill[3]),
    maxLevels: Number(skill[4]),
    state: skill[5],
    parentField,
    parentFields,
    skillPointCost: Number(/\.setSkillPointCost\((\d+)\)/.exec(block)?.[1] ?? '1'),
    requiredDegree: Number(/\.setRequiredDegree\((\d+)\)/.exec(block)?.[1] ?? '0'),
    treeX: treePosition ? Number(treePosition[1]) : null,
    treeY: treePosition ? Number(treePosition[2]) : null,
    iconItem: /ItemInit\.(\w+)\.get\(\)/.exec(block)?.[1] ?? null,
    description: ''
  };
}

function renderSkillDeclaration(skill: SkillModel, markerIndent: string, branchColor: string): string {
  const statementIndent = `${markerIndent}\t`;
  const newSkillIndent = `${statementIndent}\t\t`;
  const chainIndent = `${newSkillIndent}\t\t`;
  const parents = parentFieldsOf(skill);
  const parent = parents[0] ? `SkillPointInit.${parents[0]}` : 'null';
  const lines = [
    `${statementIndent}SkillPointInit.${skill.field} = SkillPointInit.registerSkill(branch,`,
    `${newSkillIndent}new SkillPoint(${skill.id}, "${escapeJavaString(skill.name)}", ${skill.bloodCost}, ${skill.maxLevels}, EnumSkillStates.${skill.state}, ${parent})`
  ];
  const metadataChain: string[] = [];
  if (skill.skillPointCost !== 1 || skill.requiredDegree !== 0) {
    metadataChain.push(`setSkillPointCost(${skill.skillPointCost})`);
    metadataChain.push(`setRequiredDegree(${skill.requiredDegree})`);
  }
  if (skill.treeX !== null && skill.treeY !== null) {
    metadataChain.push(`setTreePosition(${skill.treeX}, ${skill.treeY})`);
  }
  metadataChain.push(`setBranch("${escapeJavaString(skill.branch)}")`);
  metadataChain.push(`setBranchColor(${argbLiteralFromHex(branchColor, skill.branch)})`);
  if (parents.length > 1) {
    metadataChain.push(`addParents(${parents.slice(1).map(parentField => `SkillPointInit.${parentField}`).join(', ')})`);
  }
  if (metadataChain.length) {
    lines.push(`${chainIndent}.${metadataChain.join('.')}`);
  }
  if (skill.iconItem) {
    lines.push(`${chainIndent}.setIconItem(() -> new ItemStack(ItemInit.${skill.iconItem}.get())));`);
  } else {
    lines[lines.length - 1] += ');';
  }
  return lines.join('\n');
}

function parseAdditionalParents(block: string): string[] {
  const match = /\.addParents\(\s*([^)]*?)\s*\)/s.exec(block);
  if (!match) return [];
  const parents: string[] = [];
  for (const parent of match[1].matchAll(/SkillPointInit\.(\w+)/g)) {
    parents.push(parent[1]);
  }
  return parents;
}

function parseDegreeLabels(section: string): DegreeLabelPosition[] {
  return [...section.matchAll(/SkillPointInit\.setDegreeLabelPosition\(\s*(\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*\)\s*;/g)]
    .map(match => ({
      degree: Number(match[1]),
      x: Number(match[2]),
      y: Number(match[3])
    }))
    .sort((a, b) => a.degree - b.degree);
}

function renderDegreeLabels(degreeLabels: DegreeLabelPosition[], markerIndent: string): string {
  if (!degreeLabels.length) return '';
  const statementIndent = `${markerIndent}\t`;
  return [...degreeLabels]
    .sort((a, b) => a.degree - b.degree)
    .map(label => `${statementIndent}SkillPointInit.setDegreeLabelPosition(${label.degree}, ${label.x}, ${label.y});`)
    .join('\n');
}

function parseBranchColor(section: string, branch: string): string {
  const match = /\.setBranchColor\(\s*(0x[0-9a-fA-F]+|[0-9a-fA-F]{6,8})\s*\)/.exec(section);
  return match ? hexFromArgbLiteral(match[1], branch) : normalizeBranchColor(null, branch);
}

function escapeJavaString(value: string): string {
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}
