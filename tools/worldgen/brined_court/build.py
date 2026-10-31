"""Writes the two Seats' templates and template pools, and points their structures at the start jigsaw.

Every pool element ignores waterlogging: the templates carry their own water (kit.py). Refuses to write anything if a
template fails checks.py (forbidden blocks, an air pocket under the waterline that water could reach).
"""
import json
import os
import sys

from . import checks, garden, sanctum

ROOT = os.path.join('src', 'main', 'resources', 'data', 'hemomancy')
STRUCTURES = os.path.join(ROOT, 'structure', 'brined_court')
POOLS = os.path.join(ROOT, 'worldgen', 'template_pool', 'brined_court')

# (site, piece, builder, the pool it is drawn from, whether its top face opens into the piece above)
PIECES = [
    ('lighthouse_garden', 'garden', lambda: garden.Garden().build(), 'start_pool', False),
    ('lighthouse_garden', 'undercroft', lambda: garden.Undercroft().build(), 'undercroft', True),
    ('vent_sanctum', 'sanctum', lambda: sanctum.Sanctum().build(), 'start_pool', False),
    ('vent_sanctum', 'body', lambda: sanctum.Body().build(), 'body', False),
]


def write_json(path, value):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', newline='\n') as f:
        json.dump(value, f, indent=2)
        f.write('\n')


def main():
    rooms, problems = {}, []
    for site, piece, make, pool, open_top in PIECES:
        room = make()
        rooms[(site, piece)] = (room, pool)
        problems += checks.all_problems('%s/%s' % (site, piece), room, open_top)
    if problems:
        print('\n'.join(problems[:60]))
        print('%d problems; nothing written' % len(problems))
        sys.exit(1)
    for (site, piece), (room, pool) in rooms.items():
        room.save(os.path.join(STRUCTURES, site, piece + '.nbt'))
        write_json(os.path.join(POOLS, site, pool + '.json'), {
            'name': 'hemomancy:brined_court/%s/%s' % (site, pool), 'fallback': 'minecraft:empty',
            'elements': [{'weight': 1, 'element': {
                'element_type': 'minecraft:single_pool_element',
                'location': 'hemomancy:brined_court/%s/%s' % (site, piece),
                'processors': 'minecraft:empty', 'projection': 'rigid',
                'override_liquid_settings': 'ignore_waterlogging'}}]})
        print('%s/%s: %s, %d blocks' % (site, piece, room.size, len(room.blocks)))
    for site in ('lighthouse_garden', 'vent_sanctum'):
        path = os.path.join(ROOT, 'worldgen', 'structure', site + '.json')
        with open(path) as f:
            structure = json.load(f)
        structure['start_jigsaw_name'] = 'hemomancy:court_seat_start'
        structure['size'] = 2
        write_json(path, structure)
