"""Writes both wayside templates. Refuses to write anything if either fails checks.py."""
import os
import sys
from collections import Counter

from . import bivouac, checks, ossuary

STRUCTURES = os.path.join('src', 'main', 'resources', 'data', 'hemomancy', 'structure')

SITES = [
    ('bog_body_ossuary_niche', lambda: ossuary.Ossuary().build(), ossuary.PLACEHOLDER),
    ('sanguine_surveyor_bivouac', lambda: bivouac.Bivouac().build(), bivouac.PLACEHOLDER),
]


def main():
    built, problems = [], []
    for name, make, placeholder in SITES:
        site = make()
        built.append((name, site))
        problems += checks.all_problems(name, site, placeholder)
    if problems:
        print('\n'.join(problems[:80]))
        print('%d problems; nothing written' % len(problems))
        sys.exit(1)
    for name, site in built:
        site.save(os.path.join(STRUCTURES, name + '.nbt'))
        solid = sum(1 for n, _p, _b in site.blocks.values() if n != 'minecraft:air')
        kinds = Counter(n for n, _p, _b in site.blocks.values())
        print('%s: %s, %d cells written (%d not air), %d block kinds' % (
            name, site.size, len(site.blocks), solid, len(kinds)))
