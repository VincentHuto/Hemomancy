"""Writes the six outposts' templates and start pools (Track 8), and points their structures at the outpost start.

Every pool element ignores waterlogging (the templates carry their own water, kit.py). The Line Camp's pool holds its
two variants, joined and parted, at equal weight, so each placement picks one. Refuses to write anything if a template
fails checks.py (forbidden blocks, an air pocket under the sea that water could reach).
"""
import json
import os
import sys

from . import checks, outposts
from .build import POOLS, ROOT, STRUCTURES, write_json

SITES = ('tide_refuge', 'wake_nursery', 'line_camp', 'screened_cleft', 'still_bell', 'whale_fall')


def main():
    built, problems = [], []
    for site, piece, make in outposts.PIECES:
        outpost = make()
        room = outpost.build()
        built.append((site, piece, room))
        problems += checks.all_problems('%s/%s' % (site, piece), room)
    if problems:
        print('\n'.join(problems[:60]))
        print('%d problems; nothing written' % len(problems))
        sys.exit(1)
    elements = {}
    for site, piece, room in built:
        room.save(os.path.join(STRUCTURES, site, piece + '.nbt'))
        elements.setdefault(site, []).append({'weight': 1, 'element': {
            'element_type': 'minecraft:single_pool_element',
            'location': 'hemomancy:brined_court/%s/%s' % (site, piece),
            'processors': 'minecraft:empty', 'projection': 'rigid',
            'override_liquid_settings': 'ignore_waterlogging'}})
        print('%s/%s: %s, %d blocks' % (site, piece, room.size, len(room.blocks)))
    for site in SITES:
        write_json(os.path.join(POOLS, site, 'start_pool.json'), {
            'name': 'hemomancy:brined_court/%s/start_pool' % site, 'fallback': 'minecraft:empty',
            'elements': elements[site]})
        path = os.path.join(ROOT, 'worldgen', 'structure', site + '.json')
        with open(path) as f:
            structure = json.load(f)
        structure['start_jigsaw_name'] = outposts.OUTPOST_START
        structure['size'] = 1
        write_json(path, structure)
    stub = os.path.join(STRUCTURES, 'stub_marker.nbt')
    if os.path.exists(stub):
        os.remove(stub)                  # Track 6's one-block stand-in; every outpost has its template now
        print('removed the stub marker template')
