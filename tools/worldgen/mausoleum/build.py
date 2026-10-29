"""Writes every Saint's templates, template pools, structure, biome tag and the structure set, then lays each complex
out the way JigsawPlacement does (rigid pieces, connectors on box faces) for every combination of approach layouts and
refuses to finish if a mandatory piece would meet another, or an optional niche could steal a mandatory piece's room.
"""
import itertools
import json
import os
import shutil

import nbtlib

from . import checks, kit, pieces
from .pieces import STRUCTURES, pool_name
from .themes import THEMES, SAINTS

ROOT = os.path.join('src', 'main', 'resources', 'data', 'hemomancy')
POOLS = os.path.join(ROOT, 'worldgen', 'template_pool', 'mausoleum')
SIZE = 13
MAX_DISTANCE = 116
# Which template side of the nave the level and crypt wings leave by. Turned half round under the gallery, the nave's
# south side lies under the gallery's treasure room; the upper wing, whose halls rise to within a block of the
# gallery's floor course, leaves by the other side. Checked by the overlap pass below.
LOW_SIDE = 'south'
APPROACHES = {0: [('a', 2), ('b', 1)], 1: [('a', 2), ('b', 1)], 2: [('a', 1), ('b', 1)]}
NICHES = [('niche_ossuary', 3), ('niche_reliquary', 1), ('niche_shrine', 2)]


def write_json(path, value):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', newline='\n') as f:
        json.dump(value, f, indent=2)
        f.write('\n')


def element(saint, piece, weight=1):
    return {'weight': weight, 'element': {
        'element_type': 'minecraft:single_pool_element', 'location': pool_name(saint, piece),
        'projection': 'rigid', 'processors': 'minecraft:empty'}}


def pool(saint, name, elements, fallback='minecraft:empty'):
    write_json(os.path.join(POOLS, saint, name + '.json'), {'fallback': fallback, 'elements': elements})


def templates(saint):
    """Every generated template of one Saint, by name, finished (no flat face larger than 3 x 3)."""
    theme = THEMES[saint]
    out = {'top': pieces.top(saint), 'stairs': pieces.entrance_stairs(saint), 'antechamber': pieces.antechamber(saint),
           'descent': pieces.descent(saint), 'gallery': pieces.gallery(saint), 'threshold': pieces.threshold_hall(saint),
           'nave': pieces.nave(saint, LOW_SIDE), 'chamber': pieces.chamber(saint),
           'niche_ossuary': pieces.niche(saint, 'ossuary'), 'niche_reliquary': pieces.niche(saint, 'reliquary'),
           'niche_shrine': pieces.niche(saint, 'shrine'), 'niche_wall': pieces.niche_wall(saint)}
    out.update(pieces.descent_turns(saint))
    for slot, kind in enumerate(theme.CHAPELS):
        if slot == 2:
            out['wing_2_a'] = pieces.crypt_approach(saint, kind, 'a')
            out['wing_2_b'] = pieces.crypt_approach(saint, kind, 'b')
        else:
            out['wing_%d_a' % slot] = pieces.approach_corridor(saint, kind)
            out['wing_%d_b' % slot] = pieces.approach_ossuary(saint, kind)
        hall, marks = pieces.hall(saint, kind)
        out[kind.lower() + '_hall'] = hall
        out[kind.lower() + '_focus'] = pieces.focus_room(saint, kind, slot, hall, marks)
    motif = getattr(theme, 'motif', None)
    for name, room in out.items():
        if motif:
            motif(name, room, theme.CRYPT)
        kit.finish(room, theme.CRYPT, '%s/%s' % (saint, name), open_outside=(name in ABOVE_GROUND))
    return out


# Pieces that stand above the ground: their outside faces are seen.
ABOVE_GROUND = ('top',)


def problems(saint, rooms):
    """Track 7's acceptance checks on every template (checks.py)."""
    accent = THEMES[saint].CRYPT['accent']
    out = []
    for name, room in rooms.items():
        out += ['%s %s' % (saint, p) for p in checks.all_problems(name, room, accent, name in ABOVE_GROUND)]
    return out


def write_saint(saint):
    for folder in (os.path.join(STRUCTURES, saint), os.path.join(POOLS, saint)):
        if os.path.isdir(folder):
            shutil.rmtree(folder)
    rooms = templates(saint)
    for name, room in rooms.items():
        room.save(os.path.join(STRUCTURES, saint, name + '.nbt'))
    for name in [n for n in rooms if not n.startswith(('wing_', 'niche_'))]:
        pool(saint, name, [element(saint, name)])
    for slot, layouts in APPROACHES.items():
        pool(saint, 'wing_%d' % slot, [element(saint, 'wing_%d_%s' % (slot, layout), weight)
                                       for layout, weight in layouts])
    pool(saint, 'niche', [element(saint, name, weight) for name, weight in NICHES],
         fallback=pool_name(saint, 'niche_end'))
    pool(saint, 'niche_end', [element(saint, 'niche_wall')])
    write_json(os.path.join(ROOT, 'worldgen', 'structure', 'mausoleum_%s.json' % saint), {
        'type': 'hemomancy:mausoleum', 'start_pool': pool_name(saint, 'top'), 'size': SIZE,
        # Row 0 of the entrance (its floor) replaces the ground's top block: JigsawPlacement puts a rigid start's row 0
        # at start_height + first free block - 1. MausoleumStructure then lifts the complex onto the highest ground
        # under the entrance. Pinned by MausoleumComplexResourceTest.theEntranceFloorIsLevelWithTheGround.
        'start_height': {'type': 'minecraft:constant', 'value': {'absolute': 0}},
        'project_start_to_heightmap': 'WORLD_SURFACE_WG', 'terrain_adaptation': 'beard_thin',
        # 116 plus beard_thin's 12-block padding is vanilla's 128 ceiling; the complex folds back under itself instead.
        'max_distance_from_center': MAX_DISTANCE, 'use_expansion_hack': False,
        'biomes': '#hemomancy:has_structure/mausoleum_%s' % saint, 'step': 'surface_structures',
        'spawn_overrides': {}})
    write_json(os.path.join(ROOT, 'tags', 'worldgen', 'biome', 'has_structure', 'mausoleum_%s.json' % saint),
               {'replace': False, 'values': THEMES[saint].BIOMES})
    return problems(saint, rooms)


# --- the layout check -----------------------------------------------------------------------------------------------

DIRS = {'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0), 'west': (-1, 0, 0), 'up': (0, 1, 0),
        'down': (0, -1, 0)}
CLOCKWISE = ['north', 'east', 'south', 'west']
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east', 'up': 'down', 'down': 'up'}


def rot_pos(r, x, y, z):
    for _ in range(r):
        x, z = -z, x
    return x, y, z


def rot_dir(r, d):
    return CLOCKWISE[(CLOCKWISE.index(d) + r) % 4] if d in CLOCKWISE else d


class Template:
    def __init__(self, path):
        f = nbtlib.load(path)
        self.size = tuple(int(v) for v in f['size'])
        palette = f['palette']
        self.jigsaws = []
        for block in f['blocks']:
            state = palette[int(block['state'])]
            if str(state['Name']) != 'minecraft:jigsaw':
                continue
            front, top = str(state['Properties']['orientation']).split('_')
            nbt = block['nbt']
            self.jigsaws.append({'pos': tuple(int(v) for v in block['pos']), 'front': front, 'top': top,
                                 'name': str(nbt['name']), 'target': str(nbt['target']), 'pool': str(nbt['pool']),
                                 'joint': str(nbt['joint'])})


def box(template, origin, r):
    sx, sy, sz = template.size
    corners = [rot_pos(r, x, y, z) for x in (0, sx - 1) for y in (0, sy - 1) for z in (0, sz - 1)]
    lo = tuple(origin[i] + min(c[i] for c in corners) for i in range(3))
    hi = tuple(origin[i] + max(c[i] for c in corners) for i in range(3))
    return lo, hi


def meets(a, b):
    return all(a[0][i] <= b[1][i] and b[0][i] <= a[1][i] for i in range(3))


def attach(parent_jigsaw, parent_origin, parent_r, child, child_name):
    """Where JigsawPlacement puts `child` on this connector: (origin, rotation), the one rotation that faces it."""
    fx, fy, fz = DIRS[rot_dir(parent_r, parent_jigsaw['front'])]
    px, py, pz = rot_pos(parent_r, *parent_jigsaw['pos'])
    at = (parent_origin[0] + px + fx, parent_origin[1] + py + fy, parent_origin[2] + pz + fz)
    want = OPPOSITE[rot_dir(parent_r, parent_jigsaw['front'])]
    for r in range(4):
        for j in child.jigsaws:
            if j['name'] != parent_jigsaw['target'] or rot_dir(r, j['front']) != want:
                continue
            if parent_jigsaw['front'] in ('up', 'down') and parent_jigsaw['joint'] == 'aligned' \
                    and rot_dir(r, j['top']) != rot_dir(parent_r, parent_jigsaw['top']):
                continue
            cx, cy, cz = rot_pos(r, *j['pos'])
            return (at[0] - cx, at[1] - cy, at[2] - cz), r
    raise ValueError('%s cannot attach to %s' % (child_name, parent_jigsaw['name']))


def lay_out(saint, chosen):
    """The mandatory pieces for one choice of approach per slot, plus every niche connector's would-be pieces."""
    folder = os.path.join(STRUCTURES, saint)
    cache = {}

    def template(name):
        if name not in cache:
            cache[name] = Template(os.path.join(folder, name + '.nbt'))
        return cache[name]

    placed = []
    niches = []
    queue = [('top', (0, 0, 0), 0, 0)]
    while queue:
        name, origin, r, depth = queue.pop(0)
        t = template(name)
        placed.append((name, box(t, origin, r), depth, origin, r))
        for j in t.jigsaws:
            if j['pool'] == 'minecraft:empty':
                continue
            target_pool = j['pool'].split('/')[-1]
            if target_pool == 'niche':
                for piece in [n for n, _ in NICHES] + ['niche_wall']:
                    o, rr = attach(j, origin, r, template(piece), piece)
                    niches.append((name, piece, box(template(piece), o, rr), depth + 1))
                continue
            child = 'wing_%s_%s' % (target_pool[-1], chosen[int(target_pool[-1])]) \
                if target_pool.startswith('wing_') else target_pool
            o, rr = attach(j, origin, r, template(child), child)
            queue.append((child, o, rr, depth + 1))
    return placed, niches


def check(saint):
    problems = []
    report = None
    for combo in itertools.product(*[[layout for layout, _ in APPROACHES[s]] for s in sorted(APPROACHES)]):
        placed, niches = lay_out(saint, combo)
        for (a, b) in itertools.combinations(placed, 2):
            if meets(a[1], b[1]):
                problems.append('%s %s: %s meets %s' % (saint, combo, a[0], b[0]))
        for depth in [p[2] for p in placed] + [n[3] for n in niches]:
            if depth > SIZE:
                problems.append('%s %s: depth %d beyond size %d' % (saint, combo, depth, SIZE))
        for (parent, piece, nbox, _) in niches:
            for other in placed:
                if meets(nbox, other[1]):
                    problems.append('%s %s: niche %s off %s would meet %s' % (saint, combo, piece, parent, other[0]))
        top = placed[0][1]
        cx, cz = (top[0][0] + top[1][0]) / 2, (top[0][2] + top[1][2]) / 2
        reach = max(max(abs(b[0][0] - cx), abs(b[1][0] - cx), abs(b[0][2] - cz), abs(b[1][2] - cz))
                    for _, b, *_ in placed)
        floor_y = {p[0]: p[1][0][1] for p in placed}
        report = (reach, min(b[0][1] for _, b, *_ in placed), floor_y, len(placed))
    return problems, report


def main():
    failed = []
    for saint in SAINTS:
        failed += write_saint(saint)
    write_json(os.path.join(ROOT, 'worldgen', 'structure_set', 'saint_mausoleums.json'), {
        'structures': [{'structure': 'hemomancy:mausoleum_%s' % s, 'weight': 1} for s in SAINTS],
        'placement': {'type': 'minecraft:random_spread', 'spacing': 40, 'separation': 12, 'salt': 581274903}})
    for saint in SAINTS:
        problems, (reach, bottom, floors, count) = check(saint)
        failed += problems
        print('%s: %d pieces, reach %.1f from the entrance centre, lowest box y %d' % (saint, count, reach, bottom))
    for problem in failed:
        print('PROBLEM', problem)
    if failed:
        raise SystemExit(1)
    print('ok')
