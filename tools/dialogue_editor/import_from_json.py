#!/usr/bin/env python3
"""
import_from_json.py — Write a Hemomancy *DialogueTrees.java file back from
the JSON produced (and possibly edited) by the visual editor.

Usage
-----
    python import_from_json.py <trees.json> <DialogueTrees.java>

The script finds each DialogueTree.builder(...)...build() block in the Java
file and replaces it with freshly generated code from the corresponding
JSON tree.

Rules
-----
* Only trees without a "variant" key and without any node that has a
  "__variable" option placeholder are regenerated automatically.  All
  others are left untouched and a warning is printed.
* The method signature, surrounding conditional logic, and class structure
  are never modified — only the .builder(...)...build() chains.
* Make a backup before running (the script overwrites the Java file).
"""

import re
import json
import sys
import os
import shutil
from datetime import datetime


# ── Paren / brace helpers (same as in export_to_json.py) ────────────────────

def find_close_paren(text: str, open_pos: int) -> int:
    depth = 0
    i = open_pos
    in_str = False
    esc = False
    while i < len(text):
        c = text[i]
        if esc:
            esc = False
        elif c == '\\' and in_str:
            esc = True
        elif c == '"':
            in_str = not in_str
        elif not in_str:
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    return -1


# ── Code generation ──────────────────────────────────────────────────────────

INDENT = '\t\t\t\t'   # 4 tabs — matches the existing style inside builder chains
OPT_INDENT = INDENT + '\t\t'


def _q(s: str | None) -> str:
    """Quote a string or return 'null'."""
    return f'"{s}"' if s is not None else 'null'


def gen_option(opt: dict) -> str:
    text = opt.get("text", "")
    nxt = opt.get("next") or None
    evt = opt.get("event") or None
    return f'{OPT_INDENT}new DialogueOption({_q(text)}, {_q(nxt)}, {_q(evt)})'


def gen_node(node: dict) -> str:
    lines = node.get("lines", [])
    options = node.get("options", [])

    # Skip nodes that still have unresolved variable placeholders
    for opt in options:
        if "__variable" in opt:
            raise ValueError(
                f'Node "{node["id"]}" has unresolved variable options — '
                'regeneration is not safe for this method.'
            )

    lines_code = ',\n'.join(f'{OPT_INDENT}"{l}"' for l in lines)
    opts_code = ',\n'.join(gen_option(o) for o in options)

    return (
        f'{INDENT}.addNode(new DialogueNode("{node["id"]}", List.of(\n'
        f'{lines_code}\n'
        f'{INDENT}\t), List.of(\n'
        f'{opts_code}\n'
        f'{INDENT}\t)))'
    )


def gen_builder_chain(tree: dict) -> str:
    """Generate the full  DialogueTree.builder(...)...build();  chain."""
    theme = tree.get("theme", "BLOOD")
    theme_line = (
        f'\n{INDENT}\t\t.theme(DialogueTheme.{theme})'
        if theme != "BLOOD"
        else ''
    )
    nodes_code = '\n'.join(gen_node(n) for n in tree.get("nodes", []))

    return (
        f'DialogueTree.builder(SPEAKER, {_icon_const(tree)}, entityId){theme_line}\n'
        f'{nodes_code}\n'
        f'{INDENT}\t\t.build();'
    )


def _icon_const(tree: dict) -> str:
    """Return the Java constant name used for the speaker icon."""
    # The icons are stored as constants like VICAR_ICON, ALCHEMIST_ICON, etc.
    # We can't know the exact name from the JSON, so we use a best-effort match.
    icon = tree.get("icon", "")
    if "vicar" in icon:
        return "VICAR_ICON"
    if "alchemist" in icon:
        return "ALCHEMIST_ICON"
    if "zealot" in icon:
        return "ZEALOT_ICON"
    if "guardian" in icon:
        return "GUARDIAN_ICON"
    if "hermit" in icon:
        return "HERMIT_ICON"
    if "acolyte" in icon:
        return "ACOLYTE_ICON"
    # Fallback: just return the raw resource string
    raw = icon.replace("hemomancy:", "")
    return f'Hemomancy.rloc("{raw}")'


# ── Replacement logic ────────────────────────────────────────────────────────

def find_builder_chain_spans(java_src: str) -> list[tuple[int, int]]:
    """Return (start, end) index pairs for every builder chain in the source."""
    spans: list[tuple[int, int]] = []
    pos = 0
    while True:
        m = re.search(r'DialogueTree\.builder\s*\(', java_src[pos:])
        if not m:
            break
        abs_start = pos + m.start()
        build_m = re.search(r'\.build\s*\(\s*\)\s*;', java_src[abs_start:])
        if not build_m:
            break
        abs_end = abs_start + build_m.end()
        spans.append((abs_start, abs_end))
        pos = abs_end
    return spans


def replace_chains(java_src: str, trees: list[dict]) -> str:
    """
    Replace each builder chain in java_src with generated code from the
    corresponding tree entry.  Chains and tree entries are matched by order
    of appearance.
    """
    spans = find_builder_chain_spans(java_src)

    if len(spans) != len(trees):
        # Mismatch — map by method name where possible, otherwise abort
        raise ValueError(
            f'Chain count mismatch: found {len(spans)} builder chains in '
            f'the Java file but {len(trees)} trees in JSON. '
            'Make sure the JSON came from the same file version.'
        )

    # Replace from right to left so indices stay valid
    result = java_src
    for (start, end), tree in reversed(list(zip(spans, trees))):
        # Preserve leading whitespace of the original chain
        leading_ws_m = re.search(r'\S', result[max(0, start - 40):start])
        leading = ''
        if leading_ws_m:
            line_start = result.rfind('\n', 0, start) + 1
            leading = result[line_start:start]

        new_chain = leading + gen_builder_chain(tree)
        result = result[:start] + new_chain + result[end:]

    return result


# ── Entry point ──────────────────────────────────────────────────────────────

def main() -> None:
    if len(sys.argv) < 3:
        print(
            'Usage: python import_from_json.py <trees.json> <DialogueTrees.java>',
            file=sys.stderr,
        )
        sys.exit(1)

    json_path = sys.argv[1]
    java_path = sys.argv[2]

    with open(json_path, encoding='utf-8') as f:
        data = json.load(f)

    trees: list[dict] = data.get('trees', [])

    # Filter out trees we can't regenerate
    skipped: list[str] = []
    regenerable: list[dict] = []
    for tree in trees:
        label = tree['method'] + (f'__{tree["variant"]}' if 'variant' in tree else '')
        try:
            # Dry-run generation to check for variable placeholders
            for node in tree.get('nodes', []):
                for opt in node.get('options', []):
                    if '__variable' in opt:
                        raise ValueError(f'variable placeholder in node "{node["id"]}"')
            regenerable.append(tree)
        except ValueError as exc:
            skipped.append(f'  {label}: {exc}')

    if skipped:
        print('⚠ Skipping (manual editing required):')
        for s in skipped:
            print(s)

    if not regenerable:
        print('Nothing to regenerate.')
        return

    with open(java_path, encoding='utf-8') as f:
        java_src = f.read()

    # Back up the original
    backup = java_path + '.bak'
    shutil.copy2(java_path, backup)
    print(f'✔ Backup written to {backup}')

    try:
        new_src = replace_chains(java_src, regenerable)
    except ValueError as exc:
        print(f'✘ {exc}', file=sys.stderr)
        sys.exit(1)

    with open(java_path, 'w', encoding='utf-8') as f:
        f.write(new_src)

    print(f'✔ Wrote {java_path}  ({len(regenerable)} chains regenerated)')


if __name__ == '__main__':
    main()
