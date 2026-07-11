import type { Diagnostic, DialogueFile } from '../shared/types';

export function validateDialogueFile(
  file: DialogueFile,
  translations: Record<string, string>,
  knownEvents: Set<string>
): Diagnostic[] {
  const diagnostics: Diagnostic[] = [];

  for (const tree of file.trees) {
    if (tree.dispatchOnly) continue;
    if (!tree.startNode) {
      diagnostics.push({ severity: 'error', code: 'missing_start_node', message: 'Tree has no start node.', file: file.path, tree: tree.method });
    }

    const ids = new Set<string>();
    const dupes = new Set<string>();
    for (const node of tree.nodes) {
      if (ids.has(node.id)) dupes.add(node.id);
      ids.add(node.id);
    }
    for (const id of dupes) {
      diagnostics.push({ severity: 'error', code: 'duplicate_node_id', message: `Duplicate node id "${id}".`, file: file.path, tree: tree.method, node: id });
    }

    for (const node of tree.nodes) {
      for (const line of node.lines) {
        if (!translations[line]) {
          diagnostics.push({ severity: 'warning', code: 'missing_translation', message: `Missing translation for "${line}".`, file: file.path, tree: tree.method, node: node.id });
        }
      }

      node.options.forEach((option, optionIndex) => {
        if (option.next && !option.nextExpression && !ids.has(option.next)) {
          diagnostics.push({ severity: 'error', code: 'broken_next', message: `Option points to missing node "${option.next}".`, file: file.path, tree: tree.method, node: node.id, optionIndex });
        }
        if (option.text && !option.textExpression && !translations[option.text]) {
          diagnostics.push({ severity: 'warning', code: 'missing_translation', message: `Missing translation for option "${option.text}".`, file: file.path, tree: tree.method, node: node.id, optionIndex });
        }
        if (option.event && !option.eventExpression && !knownEvents.has(option.event)) {
          diagnostics.push({ severity: 'warning', code: 'unknown_event', message: `Event "${option.event}" is not handled yet.`, file: file.path, tree: tree.method, node: node.id, optionIndex });
        }
      });
    }
  }

  return diagnostics;
}

export function hasBlockingDiagnostics(diagnostics: Diagnostic[]): boolean {
  return diagnostics.some(diag => diag.severity === 'error');
}
