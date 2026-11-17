"""Textured cutaway sheets of the Circus templates, through the mausoleum kit's previewer (a section with the near
half cut away and a plan from above with the ceiling cut away).

    python tools/worldgen/circus/preview.py

Writes build/circus-kit/<template>.png.
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'worldgen'))

from circus import build  # noqa: E402
from mausoleum import kit_preview  # noqa: E402


def main():
    os.chdir(ROOT)
    for name, make, _folder in build.TEMPLATES:
        site = make()
        print(kit_preview.sheet(site, 'Circus ' + name, os.path.join(ROOT, 'build', 'circus-kit', name + '.png')))


if __name__ == '__main__':
    main()
