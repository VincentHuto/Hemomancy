#!/usr/bin/env python3
"""
export_to_json.py — Parse a Hemomancy *DialogueTrees.java file and write
all public static DialogueTree methods to a JSON file suitable for loading
in the visual editor (index.html).

Usage
-----
    python export_to_json.py <DialogueTrees.java> [output.json]

If output.json is omitted, the JSON is written to stdout.

Notes
-----
* Methods that build a single tree (single .build() call) are extracted
  fully automatically.
* Methods with multiple .build() calls (branching on runtime arguments,
  e.g. alreadyOnPath) produce one tree entry per branch, named
  "methodName__0", "methodName__1", etc.
* Options that were constructed in a variable (greetingOptions.add(...))
  are resolved by scanning the method body for those .add() calls.
"""

import re
import json
import sys
import os


# ── Low-level helpers ────────────────────────────────────────────────────────

def strip_comments(src: str) -> str:
    """Remove Java line and block comments (naively — doesn't handle strings)."""
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.DOTALL)
    src = re.sub(r'//[^\n]*', '', src)
    return src


def find_close_paren(text: str, open_pos: int) -> int:
    """Return the index of the ')' that closes the '(' at open_pos."""
    assert text[open_pos] == '(', f"Expected '(' at {open_pos}, got '{text[open_pos]}'"
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


def find_close_brace(text: str, open_pos: int) -> int:
    """Return the index of the '}' that closes the '{' at open_pos."""
    assert text[open_pos] == '{', f"Expected '{{' at {open_pos}, got '{text[open_pos]}'"
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
            if c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    return -1


def split_top_args(text: str) -> list[str]:
    """Split comma-separated top-level arguments (respects nested parens/braces)."""
    args: list[str] = []
    cur: list[str] = []
    depth = 0
    in_str = False
    esc = False
    for c in text:
        if esc:
            esc = False
            cur.append(c)
            continue
        if c == '\\' and in_str:
            esc = True
            cur.append(c)
            continue
        if c == '"':
            in_str = not in_str
            cur.append(c)
            continue
        if not in_str:
            if c in '([{':
                depth += 1
            elif c in ')]}':
                depth -= 1
            elif c == ',' and depth == 0:
                args.append(''.join(cur).strip())
                cur = []
                continue
        cur.append(c)
    if cur:
        args.append(''.join(cur).strip())
    return args


def extract_strings(text: str) -> list[str]:
    """Extract all double-quoted string literals from text."""
    return re.findall(r'"([^"]*)"', text)


# ── Dialogue-specific parsers ────────────────────────────────────────────────

def parse_option(text: str) -> dict | None:
    """Parse  new DialogueOption("text", "next"|null, "event"|null)  → dict."""
    text = text.strip()
    m = re.match(r'new\s+DialogueOption\s*\(', text)
    if not m:
        return None
    p_start = m.end() - 1
    p_end = find_close_paren(text, p_start)
    args = split_top_args(text[p_start + 1:p_end])
    if len(args) < 3:
        return None
    opt_text = args[0].strip().strip('"')
    next_id = None if 'null' in args[1] else args[1].strip().strip('"')
    event_id = None if 'null' in args[2] else args[2].strip().strip('"')
    return {"text": opt_text, "next": next_id, "event": event_id}


def parse_options_from_list_of(text: str) -> list[dict]:
    """Parse options from  List.of(new DialogueOption(...), ...)  text."""
    options: list[dict] = []
    for m in re.finditer(r'new\s+DialogueOption\s*\(', text):
        p_start = m.end() - 1
        p_end = find_close_paren(text, p_start)
        raw = 'new DialogueOption(' + text[p_start + 1:p_end] + ')'
        opt = parse_option(raw)
        if opt:
            options.append(opt)
    return options


def parse_node(text: str) -> dict | None:
    """Parse  new DialogueNode("id", List.of(...), List.of(...)|variable)  → dict."""
    text = text.strip()
    m = re.match(r'new\s+DialogueNode\s*\(', text)
    if not m:
        return None
    p_start = m.end() - 1
    p_end = find_close_paren(text, p_start)
    args = split_top_args(text[p_start + 1:p_end])
    if len(args) < 3:
        return None

    node_id = args[0].strip().strip('"')

    # Lines
    lines = extract_strings(args[1]) if 'List.of' in args[1] else []

    # Options
    opts_arg = args[2].strip()
    if 'List.of' in opts_arg:
        options = parse_options_from_list_of(opts_arg)
    else:
        # Variable reference — resolved later from method body context
        options = [{"__variable": opts_arg.strip()}]

    return {"id": node_id, "lines": lines, "options": options}


def resolve_variable_options(var_name: str, method_body: str) -> list[dict]:
    """
    Resolve a variable like 'greetingOptions' by finding all
    varName.add(new DialogueOption(...)) calls in method_body.
    """
    options: list[dict] = []
    pattern = re.compile(re.escape(var_name) + r'\s*\.\s*add\s*\(')
    for m in pattern.finditer(method_body):
        p_start = m.end() - 1
        p_end = find_close_paren(method_body, p_start)
        inner = method_body[p_start + 1:p_end].strip()
        opt = parse_option(inner)
        if opt:
            options.append(opt)
    return options


def extract_builder_chains(text: str) -> list[str]:
    """
    Find all  DialogueTree.builder(...)...build();  blocks in text.
    Returns each as a raw string.
    """
    chains: list[str] = []
    pos = 0
    while True:
        m = re.search(r'DialogueTree\.builder\s*\(', text[pos:])
        if not m:
            break
        abs_start = pos + m.start()
        build_m = re.search(r'\.build\s*\(\s*\)\s*;', text[abs_start:])
        if not build_m:
            break
        abs_end = abs_start + build_m.end()
        chains.append(text[abs_start:abs_end])
        pos = abs_end
    return chains


def parse_builder_chain(chain: str, method_body: str) -> dict:
    """Parse a builder chain into tree metadata + list of nodes."""
    # Theme
    theme_m = re.search(r'\.theme\s*\(\s*DialogueTheme\.(\w+)\s*\)', chain)
    theme = theme_m.group(1) if theme_m else "BLOOD"

    nodes: list[dict] = []
    for m in re.finditer(r'\.addNode\s*\(', chain):
        p_start = m.end() - 1
        p_end = find_close_paren(chain, p_start)
        raw = chain[p_start + 1:p_end]
        node = parse_node(raw)
        if not node:
            continue
        # Resolve variable options
        for i, opt in enumerate(node["options"]):
            if "__variable" in opt:
                var = opt["__variable"]
                resolved = resolve_variable_options(var, method_body)
                node["options"] = resolved
                break
        nodes.append(node)

    start_node = nodes[0]["id"] if nodes else None
    return {"theme": theme, "start_node": start_node, "nodes": nodes}


# ── Main file parser ─────────────────────────────────────────────────────────

def extract_class_constants(src_clean: str) -> tuple[str, str]:
    """Extract SPEAKER string and icon ResourceLocation from class body."""
    speaker = "unknown"
    icon = "unknown"

    m = re.search(r'SPEAKER\s*=\s*"([^"]*)"', src_clean)
    if m:
        speaker = m.group(1)

    # Match  XXXX_ICON = Hemomancy.rloc("...")  or just  ICON = Hemomancy.rloc("...")
    m = re.search(r'\w*ICON\s*=\s*Hemomancy\.rloc\s*\(\s*"([^"]*)"\s*\)', src_clean)
    if m:
        icon = "hemomancy:" + m.group(1)

    return speaker, icon


def parse_file(filepath: str) -> dict:
    with open(filepath, encoding="utf-8") as f:
        raw = f.read()

    src = strip_comments(raw)
    filename = os.path.basename(filepath)
    speaker, icon = extract_class_constants(src)

    trees: list[dict] = []

    # Match every  public static DialogueTree methodName(params) {
    method_re = re.compile(
        r'^\tpublic static DialogueTree\s+(\w+)\s*\(([^)]*)\)\s*\{',
        re.MULTILINE
    )

    for match in method_re.finditer(src):
        method_name = match.group(1)
        params_raw = match.group(2).strip()
        params = [p.strip().split()[-1] for p in params_raw.split(',') if p.strip()]

        brace_open = match.end() - 1
        brace_close = find_close_brace(src, brace_open)
        if brace_close == -1:
            continue
        body = src[brace_open + 1:brace_close]

        chains = extract_builder_chains(body)

        if not chains:
            continue

        if len(chains) == 1:
            tree = parse_builder_chain(chains[0], body)
            tree["method"] = method_name
            tree["params"] = params
            tree["speaker"] = speaker
            tree["icon"] = icon
            trees.append(tree)
        else:
            for i, chain in enumerate(chains):
                tree = parse_builder_chain(chain, body)
                tree["method"] = method_name
                tree["variant"] = i
                tree["params"] = params
                tree["speaker"] = speaker
                tree["icon"] = icon
                trees.append(tree)

    return {
        "source_file": filename,
        "source_path": os.path.abspath(filepath),
        "speaker": speaker,
        "icon": icon,
        "trees": trees,
    }


# ── Entry point ──────────────────────────────────────────────────────────────

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(
            "Usage: python export_to_json.py <DialogueTrees.java> [output.json]",
            file=sys.stderr,
        )
        sys.exit(1)

    result = parse_file(sys.argv[1])

    json_str = json.dumps(result, indent=2, ensure_ascii=False)

    if len(sys.argv) >= 3:
        with open(sys.argv[2], "w", encoding="utf-8") as f:
            f.write(json_str)
        tree_count = len(result["trees"])
        print(f"✔ Wrote {sys.argv[2]}  ({tree_count} trees extracted)")
    else:
        print(json_str)
