import type { FileDiff } from '../shared/types';

export function makeFileDiff(path: string, before: string, after: string): FileDiff {
  return {
    path,
    before,
    after,
    patch: unifiedDiff(path, before, after)
  };
}

function unifiedDiff(path: string, before: string, after: string): string {
  const a = before.split(/\r?\n/);
  const b = after.split(/\r?\n/);
  const lines = [`--- a/${path}`, `+++ b/${path}`, `@@ -1,${a.length} +1,${b.length} @@`];
  const max = Math.max(a.length, b.length);
  for (let i = 0; i < max; i++) {
    if (a[i] === b[i]) {
      if (a[i] !== undefined) lines.push(` ${a[i]}`);
      continue;
    }
    if (a[i] !== undefined) lines.push(`-${a[i]}`);
    if (b[i] !== undefined) lines.push(`+${b[i]}`);
  }
  return lines.join('\n');
}
