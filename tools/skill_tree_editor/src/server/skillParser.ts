import type { Diagnostic, SkillBranchFile, SkillModel } from '../shared/types';

export function parseSkillBranchJava(path: string, source: string): SkillBranchFile {
  const className = /public\s+final\s+class\s+(\w+)/.exec(source)?.[1] ?? '';
  const marker = /^([ \t]*)\/\/ <skill-editor branch="([^"]+)">$/m.exec(source);
  const diagnostics: Diagnostic[] = [];
  if (!marker) {
    return {
      path,
      branch: 'unknown',
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
  const skills = splitSkillDeclarations(section)
    .map(block => parseSkillDeclaration(block, branch))
    .filter((skill): skill is SkillModel => skill !== null);

  return {
    path,
    branch,
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
  const renderedSkills = file.skills.map(skill => renderSkillDeclaration(skill, marker[1])).join('\n');
  return `${source.slice(0, marker.index)}${startLine}\n${renderedSkills}\n${endLine}${source.slice(footerEnd)}`;
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
  return {
    field,
    id: Number(skill[1]),
    name: skill[2],
    branch,
    bloodCost: Number(skill[3]),
    maxLevels: Number(skill[4]),
    state: skill[5],
    parentField: skill[6] === 'null' ? null : skill[7],
    skillPointCost: Number(/\.setSkillPointCost\((\d+)\)/.exec(block)?.[1] ?? '1'),
    requiredDegree: Number(/\.setRequiredDegree\((\d+)\)/.exec(block)?.[1] ?? '0'),
    treeX: treePosition ? Number(treePosition[1]) : null,
    treeY: treePosition ? Number(treePosition[2]) : null,
    iconItem: /ItemInit\.(\w+)\.get\(\)/.exec(block)?.[1] ?? null,
    description: ''
  };
}

function renderSkillDeclaration(skill: SkillModel, markerIndent: string): string {
  const statementIndent = `${markerIndent}\t`;
  const newSkillIndent = `${statementIndent}\t\t`;
  const chainIndent = `${newSkillIndent}\t\t`;
  const parent = skill.parentField ? `SkillPointInit.${skill.parentField}` : 'null';
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

function escapeJavaString(value: string): string {
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
}
