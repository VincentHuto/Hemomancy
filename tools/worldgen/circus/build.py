"""Writes the Circus templates. Refuses to write anything if one fails checks.py.

It writes the kit sampler into the GameTest resources, and the pavilion, the five teaching camps and the abandoned stage
into the mod's structures, with the pavilion's ruin and tenancy overlays (site_states.py). An overlay is only the cells
that change, so it gets the metal check alone; the cells it lays come from the checked pavilion and the kit.
"""
import os
import sys
from collections import Counter

from . import camps, checks, pavilion, sampler, site_states, stage

# The Crimson Troupe GameTest suite's own namespace, where its templates must live.
GAMETEST_STRUCTURES = os.path.join('src', 'gameTest', 'resources', 'data', 'crimson_troupe_validation', 'structure')
STRUCTURES = os.path.join('src', 'main', 'resources', 'data', 'hemomancy', 'structure')

TEMPLATES = [
    (sampler.NAME, sampler.build, GAMETEST_STRUCTURES),
    (pavilion.NAME, pavilion.build, STRUCTURES),
] + [(name, make, STRUCTURES) for name, make in camps.TEMPLATES + stage.TEMPLATES + site_states.TEMPLATES]
OVERLAYS = {name for name, _make in site_states.TEMPLATES}


def main():
    built, problems = [], []
    for name, make, folder in TEMPLATES:
        site = make()
        built.append((name, site, folder))
        if name in OVERLAYS:
            problems += ['%s: %s' % (name, p) for p in checks.metal_problems(site)]
        else:
            problems += checks.all_problems(name, site)
    if problems:
        print('\n'.join(problems[:80]))
        print('%d problems; nothing written' % len(problems))
        sys.exit(1)
    for name, site, folder in built:
        site.save(os.path.join(folder, name + '.nbt'))
        kinds = Counter(n for n, _p, _b in site.blocks.values())
        circus = sorted(n for n in kinds if n.startswith('hemomancy:circus_'))
        print('%s: %s, %d cells, %d block kinds, %d Circus set pieces' % (
            name, site.size, len(site.blocks), len(kinds), len(circus)))
