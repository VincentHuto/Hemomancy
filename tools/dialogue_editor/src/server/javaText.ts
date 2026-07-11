export function stripComments(src: string): string {
  let out = '';
  let i = 0;
  let inString = false;
  let inChar = false;
  let escaped = false;

  while (i < src.length) {
    const c = src[i];
    const next = src[i + 1];

    if (escaped) {
      out += c;
      escaped = false;
      i++;
      continue;
    }

    if ((inString || inChar) && c === '\\') {
      out += c;
      escaped = true;
      i++;
      continue;
    }

    if (!inChar && c === '"') {
      inString = !inString;
      out += c;
      i++;
      continue;
    }

    if (!inString && c === "'") {
      inChar = !inChar;
      out += c;
      i++;
      continue;
    }

    if (!inString && !inChar && c === '/' && next === '/') {
      while (i < src.length && src[i] !== '\n') i++;
      out += '\n';
      i++;
      continue;
    }

    if (!inString && !inChar && c === '/' && next === '*') {
      i += 2;
      while (i < src.length && !(src[i] === '*' && src[i + 1] === '/')) {
        if (src[i] === '\n') out += '\n';
        i++;
      }
      i += 2;
      continue;
    }

    out += c;
    i++;
  }

  return out;
}

export function findClose(text: string, openPos: number, openChar: string, closeChar: string): number {
  let depth = 0;
  let inString = false;
  let inChar = false;
  let escaped = false;

  for (let i = openPos; i < text.length; i++) {
    const c = text[i];
    if (escaped) {
      escaped = false;
      continue;
    }
    if ((inString || inChar) && c === '\\') {
      escaped = true;
      continue;
    }
    if (!inChar && c === '"') {
      inString = !inString;
      continue;
    }
    if (!inString && c === "'") {
      inChar = !inChar;
      continue;
    }
    if (inString || inChar) continue;
    if (c === openChar) depth++;
    if (c === closeChar) {
      depth--;
      if (depth === 0) return i;
    }
  }
  return -1;
}

export function findCloseParen(text: string, openPos: number): number {
  return findClose(text, openPos, '(', ')');
}

export function findCloseBrace(text: string, openPos: number): number {
  return findClose(text, openPos, '{', '}');
}

export function splitTopArgs(text: string): string[] {
  const args: string[] = [];
  let cur = '';
  let depth = 0;
  let inString = false;
  let inChar = false;
  let escaped = false;

  for (const c of text) {
    if (escaped) {
      cur += c;
      escaped = false;
      continue;
    }
    if ((inString || inChar) && c === '\\') {
      cur += c;
      escaped = true;
      continue;
    }
    if (!inChar && c === '"') {
      inString = !inString;
      cur += c;
      continue;
    }
    if (!inString && c === "'") {
      inChar = !inChar;
      cur += c;
      continue;
    }
    if (!inString && !inChar) {
      if ('([{'.includes(c)) depth++;
      if (')]}'.includes(c)) depth--;
      if (c === ',' && depth === 0) {
        args.push(cur.trim());
        cur = '';
        continue;
      }
    }
    cur += c;
  }
  if (cur.trim()) args.push(cur.trim());
  return args;
}

export function extractJavaStrings(text: string): string[] {
  const strings: string[] = [];
  let cur = '';
  let inString = false;
  let escaped = false;

  for (const c of text) {
    if (!inString) {
      if (c === '"') {
        inString = true;
        cur = '';
      }
      continue;
    }
    if (escaped) {
      cur += c;
      escaped = false;
      continue;
    }
    if (c === '\\') {
      escaped = true;
      cur += c;
      continue;
    }
    if (c === '"') {
      strings.push(cur);
      inString = false;
      continue;
    }
    cur += c;
  }

  return strings;
}

export function parseJavaStringOrExpression(raw: string): { value: string | null; expression: boolean } {
  const trimmed = raw.trim();
  if (trimmed === 'null') return { value: null, expression: false };
  if (trimmed.startsWith('"') && trimmed.endsWith('"')) {
    return { value: trimmed.slice(1, -1), expression: false };
  }
  return { value: trimmed, expression: true };
}

export function quoteJava(value: string | null, expression = false): string {
  if (value == null || value === '') return 'null';
  if (expression) return value;
  return `"${value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')}"`;
}

export function lineNumberAt(text: string, index: number): number {
  return text.slice(0, index).split(/\r?\n/).length;
}
