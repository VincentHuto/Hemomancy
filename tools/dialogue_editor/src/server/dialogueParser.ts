import type {
  DialogueEventCatalogEntry,
  DialogueFile,
  DialogueNodeModel,
  DialogueOptionModel,
  DialogueTreeModel
} from '../shared/types';
import {
  extractJavaStrings,
  findCloseBrace,
  findCloseParen,
  parseJavaStringOrExpression,
  quoteJava,
  splitTopArgs,
  stripComments
} from './javaText';

export function parseDialogueJava(sourceFile: string, rawSource: string): DialogueFile {
  const src = stripComments(rawSource);
  const speaker = extractSpeaker(src);
  const icon = extractIcon(src);
  const nodeHelpers = extractDialogueNodeHelpers(src);
  const trees: DialogueTreeModel[] = [];

  const methodRe = /^\s*(public|private)\s+static\s+DialogueTree\s+(\w+)\s*\(([^)]*)\)\s*\{/gm;
  let match: RegExpExecArray | null;
  while ((match = methodRe.exec(src))) {
    const visibility = match[1] as 'public' | 'private';
    const method = match[2];
    const methodStart = match.index;
    const paramSource = match[3].trim();
    const params = match[3].split(',').map(part => part.trim()).filter(Boolean)
      .map(part => part.split(/\s+/).at(-1) ?? part);
    const braceOpen = match.index + match[0].lastIndexOf('{');
    const braceClose = findCloseBrace(src, braceOpen);
    if (braceClose < 0) continue;
    const body = src.slice(braceOpen + 1, braceClose);
    const bodyOffset = braceOpen + 1;
    const chains = extractBuilderChains(body, bodyOffset);

    if (chains.length === 0) {
      trees.push({
        method,
        visibility,
        params,
        theme: 'BLOOD',
        startNode: null,
        nodes: [],
        dispatchOnly: true,
        routes: extractRouteTargets(body),
        speaker,
        icon
      });
      trees.at(-1)!.sourceMethod = method;
      trees.at(-1)!.paramSource = paramSource;
      trees.at(-1)!.methodSourceSpan = { start: methodStart, end: braceClose + 1 };
      continue;
    }

    chains.forEach((chain, index) => {
      const tree = parseBuilderChain(chain.text, nodeHelpers);
      tree.method = method;
      tree.visibility = visibility;
      tree.params = params;
      tree.speaker = speaker;
      tree.icon = icon;
      tree.sourceSpan = { start: chain.start, end: chain.end };
      tree.sourceMethod = method;
      tree.paramSource = paramSource;
      tree.methodSourceSpan = { start: methodStart, end: braceClose + 1 };
      if (chains.length > 1) tree.variant = index;
      trees.push(tree);
    });
  }

  return {
    path: sourceFile,
    sourceFile,
    speaker,
    icon,
    trees,
    diagnostics: []
  };
}

function extractRouteTargets(body: string): Array<{ method: string; condition: string }> {
  const targets: Array<{ method: string; condition: string }> = [];
  const switchRoute = /\b(case\s+[^\n]+?|default)\s*->\s*(\w+)\s*\(/g;
  let match: RegExpExecArray | null;
  while ((match = switchRoute.exec(body))) targets.push({ method: match[2], condition: match[1].trim() });
  const ifRoute = /if\s*\(([^)]+)\)[\s\S]{0,160}?return\s+(\w+)\s*\(/g;
  while ((match = ifRoute.exec(body))) {
    if (!targets.some(target => target.method === match![2] && target.condition === match![1].trim())) targets.push({ method: match[2], condition: `if ${match[1].trim()}` });
  }
  if (!targets.length) {
    const direct = /return\s+(\w+)\s*\(/g;
    while ((match = direct.exec(body))) targets.push({ method: match[1], condition: 'return' });
  }
  return targets;
}

function extractSpeaker(src: string): string {
  return /SPEAKER\s*=\s*"([^"]+)"/.exec(src)?.[1] ?? 'unknown';
}

function extractIcon(src: string): string {
  const direct = /\w*ICON\s*=\s*Hemomancy\.rloc\s*\(\s*"([^"]+)"\s*\)/.exec(src)?.[1];
  return direct ? `hemomancy:${direct}` : 'unknown';
}

function extractBuilderChains(text: string, offset = 0): Array<{ text: string; start: number; end: number }> {
  const chains: Array<{ text: string; start: number; end: number }> = [];
  let pos = 0;
  while (pos < text.length) {
    const builder = /DialogueTree\.builder\s*\(/g;
    builder.lastIndex = pos;
    const m = builder.exec(text);
    if (!m) break;
    const absStart = m.index;
    const build = /\.build\s*\(\s*\)\s*;?/g;
    build.lastIndex = absStart;
    const b = build.exec(text);
    if (!b) break;
    const absEnd = b.index + b[0].length;
    chains.push({ text: text.slice(absStart, absEnd), start: offset + absStart, end: offset + absEnd });
    pos = absEnd;
  }
  return chains;
}

type NodeHelperMap = Map<string, string>;

function extractDialogueNodeHelpers(src: string): NodeHelperMap {
  const helpers: NodeHelperMap = new Map();
  const methodRe = /^\s*(?:public|private)\s+static\s+DialogueNode\s+(\w+)\s*\(\s*\)\s*\{/gm;
  let match: RegExpExecArray | null;
  while ((match = methodRe.exec(src))) {
    const methodName = match[1];
    const braceOpen = match.index + match[0].lastIndexOf('{');
    const braceClose = findCloseBrace(src, braceOpen);
    if (braceClose < 0) continue;
    const body = src.slice(braceOpen + 1, braceClose);
    const returnedNode = extractReturnedDialogueNode(body);
    if (returnedNode) helpers.set(methodName, returnedNode);
  }
  return helpers;
}

function extractReturnedDialogueNode(body: string): string | null {
  const returnRe = /return\s+new\s+DialogueNode\s*\(/g;
  const match = returnRe.exec(body);
  if (!match) return null;
  const newIndex = match.index + match[0].indexOf('new');
  const open = match.index + match[0].lastIndexOf('(');
  const close = findCloseParen(body, open);
  if (close < 0) return null;
  return body.slice(newIndex, close + 1);
}

function parseBuilderChain(chain: string, nodeHelpers: NodeHelperMap = new Map()): DialogueTreeModel {
  const theme = /\.theme\s*\(\s*DialogueTheme\.(\w+)\s*\)/.exec(chain)?.[1] ?? 'BLOOD';
  const nodes: DialogueNodeModel[] = [];

  let pos = 0;
  while (pos < chain.length) {
    const add = /\.addNode\s*\(/g;
    add.lastIndex = pos;
    const m = add.exec(chain);
    if (!m) break;
    const open = m.index + m[0].lastIndexOf('(');
    const close = findCloseParen(chain, open);
    if (close < 0) break;
    const raw = chain.slice(open + 1, close);
    const node = parseNode(raw, nodeHelpers);
    if (node) nodes.push(node);
    pos = close + 1;
  }

  return {
    method: '',
    visibility: 'public',
    params: [],
    theme,
    startNode: nodes[0]?.id ?? null,
    nodes
  };
}

function parseNode(text: string, nodeHelpers: NodeHelperMap = new Map(), seenHelpers = new Set<string>()): DialogueNodeModel | null {
  const nodeMatch = /^\s*new\s+DialogueNode\s*\(/.exec(text);
  if (!nodeMatch) {
    const expression = text.trim();
    const helperCall = /^(?:\w+\.)?(\w+)\s*\(\s*\)$/.exec(expression);
    if (helperCall) {
      const helperName = helperCall[1];
      const helperNode = nodeHelpers.get(helperName);
      if (helperNode && !seenHelpers.has(helperName)) {
        seenHelpers.add(helperName);
        return parseNode(helperNode, nodeHelpers, seenHelpers);
      }
    }
    if (expression) {
      return {
        id: expression.replace(/\W+/g, '_').replace(/^_+|_+$/g, '') || 'unparsed_node',
        lines: [],
        options: []
      };
    }
    return null;
  }

  const open = nodeMatch[0].lastIndexOf('(');
  const close = findCloseParen(text, open);
  if (close < 0) return null;
  const args = splitTopArgs(text.slice(open + 1, close));
  if (args.length < 3) return null;
  const id = parseJavaStringOrExpression(args[0]).value ?? 'unnamed';
  const lines = extractJavaStrings(args[1]);
  const options = parseOptions(args[2]);
  return { id, lines, options };
}

function parseOptions(text: string): DialogueOptionModel[] {
  const options: DialogueOptionModel[] = [];
  let pos = 0;
  while (pos < text.length) {
    const opt = /new\s+DialogueOption\s*\(/g;
    opt.lastIndex = pos;
    const m = opt.exec(text);
    if (!m) break;
    const open = m.index + m[0].lastIndexOf('(');
    const close = findCloseParen(text, open);
    if (close < 0) break;
    const args = splitTopArgs(text.slice(open + 1, close));
    if (args.length >= 3) {
      const textArg = parseJavaStringOrExpression(args[0]);
      const nextArg = parseJavaStringOrExpression(args[1]);
      const eventArg = parseJavaStringOrExpression(args[2]);
      options.push({
        text: textArg.value ?? args[0].trim(),
        next: nextArg.value,
        event: eventArg.value,
        textExpression: textArg.expression,
        nextExpression: nextArg.expression,
        eventExpression: eventArg.expression
      });
    }
    pos = close + 1;
  }
  return options;
}

export function renderDialogueFile(originalSource: string, file: DialogueFile): string {
  const originalFile = parseDialogueJava(file.path, originalSource);
  const signature = (tree: DialogueTreeModel, source = false) => `${source ? tree.sourceMethod ?? tree.method : tree.method}::${tree.paramSource ?? ''}`;
  const retainedSourceMethods = new Set(file.trees.filter(tree => tree.sourceMethod).map(tree => signature(tree, true)));
  const originalMethodCounts = new Map<string, number>();
  originalFile.trees.forEach(tree => originalMethodCounts.set(signature(tree), (originalMethodCounts.get(signature(tree)) ?? 0) + 1));
  const deletedMethods = originalFile.trees.filter(tree => tree.methodSourceSpan && !retainedSourceMethods.has(signature(tree)) && originalMethodCounts.get(signature(tree)) === 1);
  const replacable = file.trees.filter(tree => tree.sourceSpan && !tree.dispatchOnly);
  let result = originalSource;
  const edits: Array<{ start: number; end: number; content: string }> = deletedMethods.map(tree => ({ start: tree.methodSourceSpan!.start, end: tree.methodSourceSpan!.end, content: '' }));
  replacable.forEach(tree => {
    const span = tree.sourceSpan!;
    const lineStart = originalSource.lastIndexOf('\n', span.start) + 1;
    const leading = originalSource.slice(lineStart, span.start).match(/^\s*/)?.[0] ?? '';
    edits.push({ start: span.start, end: span.end, content: renderBuilderChain(tree, leading) });
  });
  for (const edit of edits.sort((a, b) => b.start - a.start)) {
    result = result.slice(0, edit.start) + edit.content + result.slice(edit.end);
  }
  const newTrees = file.trees.filter(tree => tree.newTree && !tree.dispatchOnly);
  if (newTrees.length) {
    const classClose = result.lastIndexOf('}');
    const methods = newTrees.map(renderNewTreeMethod).join('\n\n');
    result = result.slice(0, classClose) + `\n${methods}\n` + result.slice(classClose);
  }
  return result;
}

function renderNewTreeMethod(tree: DialogueTreeModel): string {
  const params = tree.paramSource?.trim() || 'int entityId';
  const chain = renderBuilderChain(tree).split('\n');
  const body = [`\t\treturn ${chain[0]}`, ...chain.slice(1).map(line => `\t\t${line}`)].join('\n');
  return `\t${tree.visibility} static DialogueTree ${tree.method}(${params}) {\n${body}\n\t}`;
}

export function renderBuilderChain(tree: DialogueTreeModel, leading = ''): string {
  const iconConst = iconConstant(tree.icon ?? '');
  const entityArg = tree.params.includes('entityId') ? 'entityId' : '0';
  const lines = [`DialogueTree.builder(SPEAKER, ${iconConst}, ${entityArg})`];
  if (tree.theme && tree.theme !== 'BLOOD') lines.push(`\t\t\t\t\t.theme(DialogueTheme.${tree.theme})`);
  tree.nodes.forEach(node => lines.push(renderNode(node)));
  lines.push('\t\t\t\t\t.build();');
  return leading + lines.join(`\n${leading}`);
}

function renderNode(node: DialogueNodeModel): string {
  const lineKeys = node.lines.map(line => `\t\t\t\t\t\t"${line}"`).join(',\n');
  const opts = node.options.map(renderOption).join(',\n');
  return [
    `\t\t\t\t\t.addNode(new DialogueNode("${node.id}", List.of(`,
    lineKeys,
    '\t\t\t\t\t), List.of(',
    opts,
    '\t\t\t\t\t)))'
  ].join('\n');
}

function renderOption(option: DialogueOptionModel): string {
  return `\t\t\t\t\t\tnew DialogueOption(${quoteJava(option.text, option.textExpression)}, ${quoteJava(option.next, option.nextExpression)}, ${quoteJava(option.event, option.eventExpression)})`;
}

function iconConstant(icon: string): string {
  if (icon.includes('vicar')) return 'VICAR_ICON';
  if (icon.includes('alchemist')) return 'ALCHEMIST_ICON';
  if (icon.includes('zealot')) return 'ZEALOT_ICON';
  if (icon.includes('guardian')) return 'GUARDIAN_ICON';
  if (icon.includes('hermit')) return 'HERMIT_ICON';
  if (icon.includes('acolyte')) return 'ACOLYTE_ICON';
  if (icon.includes('lady')) return 'LADY_ICON';
  if (icon.includes('mystery') || icon.includes('fungal')) return 'MYSTERY_ICON';
  return 'MYSTERY_ICON';
}

export function extractEventConstants(sourceFile: string, source: string): Map<string, string> {
  const constants = new Map<string, string>();
  const className = sourceFile.replace(/\.java$/, '').split(/[\\/]/).at(-1) ?? sourceFile;
  const re = /public\s+static\s+final\s+String\s+(\w+)\s*=\s*"([^"]+)"/g;
  let match: RegExpExecArray | null;
  while ((match = re.exec(source))) {
    constants.set(match[1], match[2]);
    constants.set(`${className}.${match[1]}`, match[2]);
  }
  return constants;
}

export function extractEventCatalog(handlerSource: string, eventConstants = new Map<string, string>()): DialogueEventCatalogEntry[] {
  const events = new Map<string, { id: string; constant?: string }>();
  const caseRe = /case\s+([^:\n]+?)\s*->/g;
  let match: RegExpExecArray | null;
  while ((match = caseRe.exec(handlerSource))) {
    const labels = match[1].split(',').map(label => label.trim()).filter(Boolean);
    for (const label of labels) {
      const literal = /^"([^"]+)"$/.exec(label)?.[1];
      const resolved = literal ?? eventConstants.get(label) ?? eventConstants.get(label.split('.').at(-1) ?? label);
      if (resolved) {
        events.set(resolved, { id: resolved, constant: literal ? undefined : label });
      }
    }
  }
  return [...events.values()].sort((a, b) => a.id.localeCompare(b.id)).map(event => ({
    id: event.id,
    kind: 'handler' as const,
    source: 'DialogueEventHandler.java',
    constant: event.constant,
    handled: true
  }));
}

export function extractMemoCatalog(source: string): DialogueEventCatalogEntry[] {
  const entries: DialogueEventCatalogEntry[] = [];
  const re = /public\s+static\s+final\s+MemoDefinition\s+(\w+)\s*=\s*register\s*\(\s*new\s+MemoDefinition\s*\(\s*Hemomancy\.rloc\s*\(\s*"([^"]+)"\s*\)\s*,[\s\S]*?MemoDefinition\.MemoPath\.(\w+)/g;
  let match: RegExpExecArray | null;
  while ((match = re.exec(source))) {
    entries.push({
      id: `hemomancy:${match[2]}`,
      kind: 'memo',
      source: 'MemoDefinitions.java',
      constant: match[1],
      path: match[3],
      handled: true
    });
  }
  return entries;
}

export function insertEventStubs(handlerSource: string, eventIds: string[]): string {
  const missing = eventIds.filter(id => id && !handlerSource.includes(`case "${id}"`));
  if (missing.length === 0) return handlerSource;
  const insertion = missing.map(id => [
    `\t\t\tcase "${id}" -> {`,
    '\t\t\t\t// TODO Dialogue Workspace: implement side-effects for this dialogue event.',
    `\t\t\t\tHemomancy.LOGGER.debug("Dialogue event stub fired: ${id}");`,
    '\t\t\t}'
  ].join('\n')).join('\n');
  const marker = '\t\t\tdefault -> {';
  const index = handlerSource.indexOf(marker);
  if (index < 0) return handlerSource;
  return handlerSource.slice(0, index) + insertion + '\n' + handlerSource.slice(index);
}
