"""Lists, for every JUnit test file, the repository files whose text it reads.

Writes docs/architecture/evidence/source-text-impact-map.csv. Consolidation steps consult it to find the tests
that pin code they move or delete. Run from the repository root:

    python gradle/source-text-impact-map.py

A test "pins" a file when one of its string literals names that file:
- exact:    the literal is a repository-relative path, such as "src/main/java/.../PacketHandler.java".
- base-dir: the literal is relative to a directory literal in the same test, such as Path.of(".../common")
            followed by resolve("network/PacketHandler.java").
- by-name:  the literal is a bare file name, such as "PacketHandler.java", that matches exactly one file in the
            scanned roots.
- helper:   the literal is passed to a support helper with its own base directory, such as
            readMainJava("common/network/PacketHandler.java") or ResourceReader.data("recipe/vial.json").
Paths assembled at runtime from several fragments are not detected, so treat the map as a strong lower bound.
"""
import csv
import os
import re
import sys
from collections import defaultdict

TEST_ROOT = "src/test/java"
SCANNED_ROOTS = ["src/main", "src/gameTest", "docs", "tools", "wiki", "gradle"]
OUTPUT = "docs/architecture/evidence/source-text-impact-map.csv"
LITERAL = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
FILE_NAME = re.compile(r"^[\w.\- ]+\.(java|json|png|md|py|mcmeta|nbt|txt|gradle|toml|properties|ps1|yml|glsl|fsh|vsh)$")
PATH_LIKE = re.compile(r"^[\w.\-]+(/[\w.\- ]+)+/?$")
# Calls into src/test/.../support whose literal argument is relative to a base directory held in RepoPaths.
HELPER_BASES = [
    (r"\b(?:readMainJava|RepoPaths\.mainJava)", "src/main/java/com/vincenthuto/hemomancy"),
    (r"\bResourceReader\.(?:text|json|jsonObject|path|exists)", "src/main/resources"),
    (r"\bResourceReader\.asset", "src/main/resources/assets/hemomancy"),
    (r"\bResourceReader\.data", "src/main/resources/data/hemomancy"),
]


def norm(path):
    return path.replace("\\", "/")


def kind_of(path):
    if path.startswith("src/main/java/") or path.startswith("src/gameTest/java/"):
        return "java"
    if path.startswith("src/main/resources/") or path.startswith("src/generated/"):
        return "resource"
    if path.startswith("docs/") or path.startswith("wiki/") or path.endswith(".md"):
        return "doc"
    if path.startswith("tools/"):
        return "tool"
    return "other"


def index_names():
    by_name = defaultdict(list)
    for root in SCANNED_ROOTS:
        for directory, dirs, files in os.walk(root):
            dirs[:] = [d for d in dirs if d not in ("__pycache__", "node_modules", ".git")]
            for name in files:
                by_name[name].append(norm(os.path.join(directory, name)))
    return by_name


def test_style(text):
    if "@Test" in text or "@TestFactory" in text or "@ParameterizedTest" in text:
        return "junit"
    if re.search(r"public\s+static\s+void\s+main\s*\(", text):
        return "main"
    return "none"


def helper_pins(text):
    """Paths read through the shared support helpers, whose base directories live in RepoPaths."""
    found = {}
    for call, base in HELPER_BASES:
        for match in re.finditer(call + r'\(\s*"([^"\n]+)"', text):
            path = norm(os.path.join(base, match.group(1))) if base else match.group(1)
            if os.path.exists(path):
                found.setdefault(path.rstrip("/"), "helper")
    # SourceAssertions.readDialogueRoutes() reads every dialogue route class.
    if "readDialogueRoutes()" in text:
        routes = "src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/route"
        for name in sorted(os.listdir(routes)):
            if name.endswith(".java"):
                found.setdefault(routes + "/" + name, "helper")
    return found


def pinned_files(text, by_name):
    literals = [m.group(1).replace("\\\\", "/") for m in LITERAL.finditer(text)]
    base_dirs = [lit.rstrip("/") for lit in literals if PATH_LIKE.match(lit) and os.path.isdir(lit)]
    found = helper_pins(text)
    for lit in literals:
        lit = lit.strip()
        if not lit or len(lit) > 300:
            continue
        if PATH_LIKE.match(lit) and os.path.exists(lit):
            path = norm(lit.rstrip("/"))
            found.setdefault(path, "exact" if os.path.isfile(lit) else "exact-dir")
            continue
        if not (PATH_LIKE.match(lit) or FILE_NAME.match(lit)):
            continue
        resolved = [norm(os.path.join(base, lit)) for base in base_dirs if os.path.isfile(os.path.join(base, lit))]
        if len(resolved) == 1:
            found.setdefault(resolved[0], "base-dir")
            continue
        if FILE_NAME.match(lit):
            matches = by_name.get(lit, [])
            if len(matches) == 1:
                found.setdefault(matches[0], "by-name")
    return found


def main():
    if not os.path.isdir(TEST_ROOT):
        sys.exit("run from the repository root")
    by_name = index_names()
    rows = []
    for directory, _, files in os.walk(TEST_ROOT):
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            test = norm(os.path.join(directory, name))
            with open(test, encoding="utf-8", errors="replace") as handle:
                text = handle.read()
            style = test_style(text)
            for path, resolution in sorted(pinned_files(text, by_name).items()):
                if path == test:
                    continue
                rows.append((test, style, path, kind_of(path), resolution))
    rows.sort()
    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    with open(OUTPUT, "w", encoding="utf-8", newline="\n") as handle:
        writer = csv.writer(handle, lineterminator="\n")
        writer.writerow(["test_file", "test_style", "pinned_file", "pinned_kind", "resolution"])
        writer.writerows(rows)
    tests = {row[0] for row in rows}
    java_tests = {row[0] for row in rows if row[3] == "java"}
    pinned_java = {row[2] for row in rows if row[3] == "java"}
    print(f"{len(rows)} rows, {len(tests)} tests pin files, {len(java_tests)} pin Java source, "
          f"{len(pinned_java)} Java files pinned -> {OUTPUT}")


if __name__ == "__main__":
    main()
