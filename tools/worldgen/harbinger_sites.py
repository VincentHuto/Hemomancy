"""Generates the Hematic Order's surface sites: the Blood Temple (data/hemomancy/structure/blood_temple.nbt) and the
Harbinger Outpost's twelve jigsaw pieces (data/hemomancy/structure/harbinger_outpost/). Re-run from the repository
root:

    python tools/worldgen/harbinger_sites.py                 check and write every template
    python tools/worldgen/harbinger_sites.py --preview [names]  also render cutaway sheets to build/harbinger-sites/

Every template is checked before anything is written (harbinger_sites/kit.py check()); one problem writes nothing.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from harbinger_sites import outpost, temple  # noqa: E402
from harbinger_sites.kit import AIR, check  # noqa: E402

STRUCTURES = os.path.join('src', 'main', 'resources', 'data', 'hemomancy', 'structure')
PREVIEW = os.path.join('build', 'harbinger-sites')


def templates():
    """(path under data/hemomancy/structure, piece) for every template."""
    out = [('blood_temple.nbt', temple.build())]
    out += [(os.path.join('harbinger_outpost', p.name + '.nbt'), p) for p in outpost.pieces()]
    return out


def main(argv):
    found = templates()
    problems = [line for _, piece in found for line in check(piece)]
    if problems:
        print('\n'.join(problems))
        print('%d problems; nothing written' % len(problems))
        sys.exit(1)
    for path, piece in found:
        piece.save(os.path.join(STRUCTURES, path))
        solid = sum(1 for entry in piece.blocks.values() if entry[0] != AIR)
        print('%s: %s, %d blocks' % (path, piece.size, solid))
    if '--preview' in argv:
        sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mausoleum'))
        import importlib.util
        spec = importlib.util.spec_from_file_location(
            'kit_preview', os.path.join(os.path.dirname(os.path.abspath(__file__)), 'mausoleum', 'kit_preview.py'))
        preview = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(preview)
        names = [a for a in argv if not a.startswith('--')]
        for _, piece in found:
            if names and piece.name not in names:
                continue
            print(preview.sheet(piece, piece.name, os.path.join(PREVIEW, piece.name + '.png')))


if __name__ == '__main__':
    main(sys.argv[1:])
