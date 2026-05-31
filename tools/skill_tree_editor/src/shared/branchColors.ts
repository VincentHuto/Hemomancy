export const DEFAULT_BRANCH_COLORS: Record<string, string> = {
  base: '#d00000',
  core: '#d00000',
  living_staff: '#d9ad28',
  scars: '#9a9a9f',
  summons: '#2370db',
  covenant: '#a54569',
  mycelial: '#6e8f3a'
};

export function defaultBranchColor(branch: string): string {
  return DEFAULT_BRANCH_COLORS[branch] ?? DEFAULT_BRANCH_COLORS.core;
}

export function normalizeBranchColor(value: string | null | undefined, branch = 'core'): string {
  const fallback = defaultBranchColor(branch);
  if (!value) return fallback;
  const trimmed = value.trim();
  const hex = trimmed.startsWith('#') ? trimmed.slice(1) : trimmed;
  if (/^[0-9a-fA-F]{6}$/.test(hex)) {
    return `#${hex.toLowerCase()}`;
  }
  if (/^[0-9a-fA-F]{8}$/.test(hex)) {
    return `#${hex.slice(2).toLowerCase()}`;
  }
  return fallback;
}

export function hexFromArgbLiteral(value: string | null | undefined, branch = 'core'): string {
  if (!value) return defaultBranchColor(branch);
  const hex = value.trim().replace(/^0x/i, '');
  if (!/^[0-9a-fA-F]{6,8}$/.test(hex)) return defaultBranchColor(branch);
  return normalizeBranchColor(hex.padStart(8, 'f').slice(-6), branch);
}

export function argbLiteralFromHex(value: string | null | undefined, branch = 'core'): string {
  const hex = normalizeBranchColor(value, branch).slice(1).toUpperCase();
  return `0xFF${hex}`;
}
