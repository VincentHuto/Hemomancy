import type { Diagnostic, ManipulationNodeModel, ManipulationTreeFile } from '../shared/types';

interface Span {
  start: number;
  end: number;
}

interface ParsedNode {
  model: ManipulationNodeModel;
  xExprSpan: Span;
  yExprSpan: Span;
  parentListSpan: Span | null;
}

interface ParseResult {
  tree: ManipulationTreeFile;
  parsedNodes: ParsedNode[];
}

export function parseManipulationTreeJava(path: string, source: string, tendencyByManip: Map<string, string>, colorByTendency: Map<string, string>): ParseResult {
  const env = new Map<string, number>();
  seedStaticIntConstants(env, source);

  const parsedNodes: ParsedNode[] = [];
  const nodes: ManipulationNodeModel[] = [];
  const diagnostics: Diagnostic[] = [];

  for (const statement of scanJavaStatements(source)) {
    const varDecl = parseIntDeclaration(statement.text);
    if (varDecl) {
      const value = evalIntExpression(varDecl.expr, env);
      if (value !== null) env.set(varDecl.name, value);
      continue;
    }

    const register = parseRegisterStatement(statement.text);
    if (!register) continue;

    const xValue = evalIntExpression(register.xExpr, env);
    const yValue = evalIntExpression(register.yExpr, env);
    if (xValue === null || yValue === null) {
      diagnostics.push({
        severity: 'warning',
        code: 'manip_tree_eval_failed',
        message: `Could not evaluate ${register.name} position expressions; defaulting to 0,0.`,
        file: path,
        skill: register.name
      });
    }

    const tendency = tendencyByManip.get(register.name) ?? null;
    const color = tendency ? (colorByTendency.get(tendency) ?? '#888888') : '#888888';
    const model: ManipulationNodeModel = {
      name: register.name,
      treeX: xValue ?? 0,
      treeY: yValue ?? 0,
      parents: register.parents,
      softParents: register.softParents,
      nodeShape: register.nodeShape,
      tendency,
      color
    };

    nodes.push(model);
    parsedNodes.push({
      model,
      xExprSpan: absoluteSpan(statement.start + register.xSpan.start, statement.start + register.xSpan.end),
      yExprSpan: absoluteSpan(statement.start + register.ySpan.start, statement.start + register.ySpan.end),
      parentListSpan: register.parentsSpan
        ? absoluteSpan(statement.start + register.parentsSpan.start, statement.start + register.parentsSpan.end)
        : null
    });
  }

  validateManipulationTree(nodes, diagnostics, path);

  return {
    tree: {
      path,
      source,
      nodes,
      diagnostics
    },
    parsedNodes
  };
}

export function renderManipulationTreeJava(
  source: string,
  parsedNodes: ParsedNode[],
  updates: Map<string, Pick<ManipulationNodeModel, 'treeX' | 'treeY' | 'parents'>>
): string {
  const replacements: Array<{ start: number; end: number; value: string }> = [];

  for (const parsed of parsedNodes) {
    const update = updates.get(parsed.model.name);
    if (!update) continue;

    replacements.push({
      start: parsed.xExprSpan.start,
      end: parsed.xExprSpan.end,
      value: String(Math.round(update.treeX))
    });
    replacements.push({
      start: parsed.yExprSpan.start,
      end: parsed.yExprSpan.end,
      value: String(Math.round(update.treeY))
    });

    if (parsed.parentListSpan) {
      replacements.push({
        start: parsed.parentListSpan.start,
        end: parsed.parentListSpan.end,
        value: renderParentArgs(update.parents)
      });
    }
  }

  replacements.sort((a, b) => b.start - a.start);
  let next = source;
  for (const replacement of replacements) {
    next = next.slice(0, replacement.start) + replacement.value + next.slice(replacement.end);
  }
  return next;
}

function renderParentArgs(parents: string[]): string {
  if (!parents.length) return '';
  return ', ' + parents.map(name => `"${name}"`).join(', ');
}

function absoluteSpan(start: number, end: number): Span {
  return { start, end };
}

function seedStaticIntConstants(env: Map<string, number>, source: string): void {
  for (const match of source.matchAll(/public\s+static\s+final\s+int\s+(\w+)\s*=\s*(-?\d+)\s*;/g)) {
    env.set(match[1], Number(match[2]));
  }
}

function validateManipulationTree(nodes: ManipulationNodeModel[], diagnostics: Diagnostic[], path: string): void {
  const byName = new Map<string, ManipulationNodeModel>();
  for (const node of nodes) {
    if (byName.has(node.name)) {
      diagnostics.push({
        severity: 'error',
        code: 'manip_tree_duplicate',
        message: `Duplicate manipulation node name: ${node.name}`,
        file: path,
        skill: node.name
      });
    }
    byName.set(node.name, node);
  }
  for (const node of nodes) {
    for (const parent of node.parents) {
      if (!byName.has(parent)) {
        diagnostics.push({
          severity: 'warning',
          code: 'manip_tree_unknown_parent',
          message: `Unknown parent "${parent}" referenced by "${node.name}".`,
          file: path,
          skill: node.name
        });
      }
    }
  }
}

function parseIntDeclaration(statement: string): { name: string; expr: string } | null {
  const match = /^\s*(?:final\s+)?int\s+(\w+)\s*=\s*([^;]+);\s*$/.exec(statement);
  if (!match) return null;
  return {
    name: match[1],
    expr: match[2]
  };
}

function parseRegisterStatement(statement: string): null | {
  name: string;
  xExpr: string;
  yExpr: string;
  parents: string[];
  softParents: string[];
  nodeShape: string;
  xSpan: Span;
  ySpan: Span;
  parentsSpan: Span | null;
} {
  const startIndex = statement.indexOf('register(');
  if (startIndex < 0) return null;

  const callStart = startIndex + 'register('.length;
  const callClose = findMatchingParen(statement, startIndex + 'register'.length);
  if (callClose < 0) return null;

  const argsText = statement.slice(callStart, callClose);
  const args = splitTopLevelArgs(argsText);
  if (args.length < 3) return null;

  const nameToken = parseStringLiteral(args[0].text.trim());
  if (!nameToken) return null;

  const xExpr = args[1].text.trim();
  const yExpr = args[2].text.trim();

  const xSpan = spanWithinStatement(args[1].start + callStart, args[1].end + callStart);
  const ySpan = spanWithinStatement(args[2].start + callStart, args[2].end + callStart);

  const parents = args.slice(3)
    .map(arg => parseStringLiteral(arg.text.trim()))
    .filter((value): value is string => Boolean(value));

  const parentsSpan = spanWithinStatement(
    callStart + args[2].end,
    callStart + argsText.length
  );

  const softParents = parseSoftParents(statement);
  const nodeShape = parseNodeShape(statement) ?? 'SQUARE';

  return {
    name: nameToken,
    xExpr,
    yExpr,
    parents,
    softParents,
    nodeShape,
    xSpan,
    ySpan,
    parentsSpan
  };
}

function spanWithinStatement(start: number, end: number): Span {
  return { start, end };
}

function parseSoftParents(statement: string): string[] {
  const match = /\.setSoftParents\(([^)]*)\)/.exec(statement);
  if (!match) return [];
  return splitTopLevelArgs(match[1])
    .map(arg => parseStringLiteral(arg.text.trim()))
    .filter((value): value is string => Boolean(value));
}

function parseNodeShape(statement: string): string | null {
  const match = /\.setNodeShape\(\s*EnumNodeShape\.(\w+)\s*\)/.exec(statement);
  return match ? match[1] : null;
}

function parseStringLiteral(token: string): string | null {
  const match = /^"([^"]*)"$/.exec(token);
  return match ? match[1] : null;
}

function findMatchingParen(text: string, openParenIndex: number): number {
  let depth = 0;
  let inString = false;
  for (let i = openParenIndex; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '(') depth++;
    else if (ch === ')') {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

function splitTopLevelArgs(text: string): Array<{ text: string; start: number; end: number }> {
  const result: Array<{ text: string; start: number; end: number }> = [];
  let depthParen = 0;
  let depthBracket = 0;
  let inString = false;
  let start = 0;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"' && text[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === '(') depthParen++;
    else if (ch === ')') depthParen--;
    else if (ch === '[') depthBracket++;
    else if (ch === ']') depthBracket--;
    else if (ch === ',' && depthParen === 0 && depthBracket === 0) {
      result.push({ text: text.slice(start, i), start, end: i });
      start = i + 1;
    }
  }
  result.push({ text: text.slice(start), start, end: text.length });
  return result;
}

function scanJavaStatements(source: string): Array<{ text: string; start: number; end: number }> {
  const results: Array<{ text: string; start: number; end: number }> = [];
  let start = 0;
  let inString = false;
  for (let i = 0; i < source.length; i++) {
    const ch = source[i];
    if (ch === '"' && source[i - 1] !== '\\') inString = !inString;
    if (inString) continue;
    if (ch === ';') {
      const end = i + 1;
      const text = source.slice(start, end);
      results.push({ text, start, end });
      start = end;
    }
  }
  return results;
}

type ExprToken = { type: 'num'; value: number }
  | { type: 'id'; value: string }
  | { type: 'op'; value: '+' | '-' | '*' | '/' }
  | { type: 'lparen' }
  | { type: 'rparen' };

function evalIntExpression(expr: string, env: Map<string, number>): number | null {
  const tokens = tokenizeExpr(expr);
  if (!tokens) return null;
  const parser = new ExprParser(tokens, env);
  const value = parser.parseExpression();
  if (value === null) return null;
  if (!parser.isAtEnd()) return null;
  return Math.trunc(value);
}

function tokenizeExpr(expr: string): ExprToken[] | null {
  const tokens: ExprToken[] = [];
  let i = 0;
  while (i < expr.length) {
    const ch = expr[i];
    if (/\s/.test(ch)) {
      i++;
      continue;
    }
    if (ch === '(') {
      tokens.push({ type: 'lparen' });
      i++;
      continue;
    }
    if (ch === ')') {
      tokens.push({ type: 'rparen' });
      i++;
      continue;
    }
    if (ch === '+' || ch === '-' || ch === '*' || ch === '/') {
      tokens.push({ type: 'op', value: ch });
      i++;
      continue;
    }
    if (/[0-9]/.test(ch)) {
      let j = i + 1;
      while (j < expr.length && /[0-9]/.test(expr[j])) j++;
      tokens.push({ type: 'num', value: Number(expr.slice(i, j)) });
      i = j;
      continue;
    }
    if (/[A-Za-z_]/.test(ch)) {
      let j = i + 1;
      while (j < expr.length && /[A-Za-z0-9_]/.test(expr[j])) j++;
      tokens.push({ type: 'id', value: expr.slice(i, j) });
      i = j;
      continue;
    }
    return null;
  }
  return tokens;
}

class ExprParser {
  private index = 0;
  constructor(private tokens: ExprToken[], private env: Map<string, number>) {}

  isAtEnd(): boolean {
    return this.index >= this.tokens.length;
  }

  parseExpression(): number | null {
    return this.parseAdditive();
  }

  private parseAdditive(): number | null {
    let value = this.parseMultiplicative();
    if (value === null) return null;
    while (!this.isAtEnd()) {
      const token = this.peek();
      if (token.type !== 'op' || (token.value !== '+' && token.value !== '-')) break;
      const op = this.advance() as Extract<ExprToken, { type: 'op' }>;
      const right = this.parseMultiplicative();
      if (right === null) return null;
      value = op.value === '+' ? value + right : value - right;
    }
    return value;
  }

  private parseMultiplicative(): number | null {
    let value = this.parseUnary();
    if (value === null) return null;
    while (!this.isAtEnd()) {
      const token = this.peek();
      if (token.type !== 'op' || (token.value !== '*' && token.value !== '/')) break;
      const op = this.advance() as Extract<ExprToken, { type: 'op' }>;
      const right = this.parseUnary();
      if (right === null) return null;
      if (op.value === '/') {
        if (right === 0) return null;
        value = Math.trunc(value / right);
      } else {
        value = value * right;
      }
    }
    return value;
  }

  private parseUnary(): number | null {
    if (!this.isAtEnd()) {
      const token = this.peek();
      if (token.type === 'op' && token.value === '-') {
      this.advance();
      const inner = this.parseUnary();
      return inner === null ? null : -inner;
      }
    }
    return this.parsePrimary();
  }

  private parsePrimary(): number | null {
    if (this.isAtEnd()) return null;
    const token = this.advance();
    if (token.type === 'num') return token.value;
    if (token.type === 'id') {
      const value = this.env.get(token.value);
      return value ?? null;
    }
    if (token.type === 'lparen') {
      const value = this.parseExpression();
      if (value === null) return null;
      if (this.isAtEnd() || this.peek().type !== 'rparen') return null;
      this.advance();
      return value;
    }
    return null;
  }

  private peek(): ExprToken {
    return this.tokens[this.index];
  }

  private advance(): ExprToken {
    return this.tokens[this.index++];
  }
}
