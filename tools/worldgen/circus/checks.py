"""Acceptance checks run on every Circus template before anything is written:

- states: every placed state names only properties its block has, with values it accepts (the mausoleum table, which
  reads each Hemomancy block's blockstate file);
- metals: nothing in the copper family and no gilt (FORBIDDEN);
- multiblocks: every wagon wheel has its four quarters round one hub, every two-tall piece both halves, and every pole
  runs foot, shaft or collar, finial with nothing missing between;
- mounts: a wall fixture has a full block behind it, a hanging fixture something above it, a valance a wall, a
  beam or poles at both ends of its run, a ladder its wall;
- flats: no exposed surface larger than 3 x 3 is one flat block (a 4 x 4 window of one full block);
- light: on a site with an `interior` (cells under its roofs), every open cell a monster could spawn in there has
  block light. Light runs from the kit's lamps and lit set pieces, one level less per step through anything that is
  not a cube; stairs and slabs are counted as stopping it, so a cell called lit here is lit in game.
"""
from collections import deque
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from mausoleum import checks as mausoleum_checks  # noqa: E402

from .kit import AIR, FORBIDDEN, POLES, STEP, TALL_PIECES, WHEEL, clockwise  # noqa: E402
from wayside.kit import full  # noqa: E402

WALL_FIXTURES = ('circus_spindle_rack', 'circus_trophy', 'circus_pelt_drape', 'circus_loft_record', 'laughing_mask',
                 'mourning_mask', 'blank_mask')
HANGING = ('circus_trapeze', 'circus_crossbar_rig')
OPPOSITE = {'north': 'south', 'south': 'north', 'east': 'west', 'west': 'east'}
F4 = ('north', 'east', 'south', 'west')
BOOL = ('true', 'false')
# Vanilla blocks the kit places with state that the mausoleum table does not know.
VANILLA = {'minecraft:ladder': {'facing': F4, 'waterlogged': BOOL}}
# Set pieces with no state that are not full cubes, so never a flat face.
NOT_FLAT = ('hemomancy:circus_net', 'hemomancy:circus_plinth')


def state_problems(site):
    problems = []
    for (x, y, z), (name, props, _n) in site.blocks.items():
        if name in VANILLA:
            for key, value in props:
                if value not in VANILLA[name].get(key, ()):
                    problems.append('%s.%s does not accept %s' % (name, key, value))
    return sorted(set(problems)) + [p for p in mausoleum_checks.state_problems(site)
                                    if not any(p.startswith(name + ' ') for name in VANILLA)]


def flat_problems(site):
    return ['%s face of %s from %s' % (side, name, cells[0]) for name, cells, side in
            mausoleum_checks.flat_windows(site, True, 4, sides=True) if name not in NOT_FLAT]


def strung(site, x, y, z, facing):
    """A valance hangs from something above, a wall behind, or a run whose two ends meet a pole or a wall."""
    if site.get(x, y + 1, z) not in (None, AIR):
        return True
    bx, bz = STEP[OPPOSITE[facing]]
    if full(site.get(x + bx, y, z + bz)):
        return True
    ends = []
    for step in (STEP[clockwise(facing)], STEP[OPPOSITE[clockwise(facing)]]):
        cx, cz = x, z
        while site.get(cx, y, cz) == 'hemomancy:circus_valance':
            cx, cz = cx + step[0], cz + step[1]
        other = site.get(cx, y, cz)
        ends.append(other is not None and (full(other) or other.endswith('_pole')))
    return all(ends)


def metal_problems(site):
    return ['%s at %s is a forbidden metal' % (name, pos) for pos, (name, _p, _n) in sorted(site.blocks.items())
            if any(word in name.split(':')[1] for word in FORBIDDEN)]


def multiblock_problems(site):
    problems = []
    blocks = site.blocks
    for (x, y, z), (name, props, _n) in blocks.items():
        short = name.split(':')[1]
        p = dict(props)
        if short == WHEEL:
            facing, q = p['facing'], int(p['quarter'])
            dx, dz = STEP[clockwise(facing)]
            for other in range(4):
                ax, up = (other & 1) - (q & 1), (other >> 1) - (q >> 1)
                at = (x + dx * ax, y + up, z + dz * ax)
                entry = blocks.get(at)
                if entry is None or entry[0] != name or dict(entry[1]) != {'facing': facing, 'quarter': str(other)}:
                    problems.append('wheel quarter %d at %s lacks quarter %d at %s' % (q, (x, y, z), other, at))
        elif short in TALL_PIECES:
            partner = (x, y + 1, z) if p['half'] == 'lower' else (x, y - 1, z)
            want = 'upper' if p['half'] == 'lower' else 'lower'
            entry = blocks.get(partner)
            if entry is None or entry[0] != name or dict(entry[1]).get('half') != want \
                    or dict(entry[1]).get('facing') != p['facing']:
                problems.append('%s %s half at %s has no %s half' % (short, p['half'], (x, y, z), want))
        elif short in POLES:
            below = blocks.get((x, y - 1, z))
            above = blocks.get((x, y + 1, z))
            section = p['section']
            if section != 'foot' and (below is None or below[0] != name):
                problems.append('%s %s at %s stands on no pole' % (short, section, (x, y, z)))
            if section != 'finial' and (above is None or above[0] != name):
                problems.append('%s %s at %s has no finial above it' % (short, section, (x, y, z)))
            if section == 'foot' and below is not None and below[0] == name:
                problems.append('%s foot at %s stands on its own pole' % (short, (x, y, z)))
    return problems


def mount_problems(site):
    problems = []
    for (x, y, z), (name, props, _n) in site.blocks.items():
        short = name.split(':')[1]
        p = dict(props)
        if short in WALL_FIXTURES:
            dx, dz = STEP[OPPOSITE[p['facing']]]
            if not full(site.get(x + dx, y, z + dz)):
                problems.append('%s at %s has no wall behind it' % (short, (x, y, z)))
        if short == 'circus_valance' and not strung(site, x, y, z, p['facing']):
            problems.append('valance at %s hangs from nothing' % ((x, y, z),))
        if short in HANGING and site.get(x, y + 1, z) in (None, AIR):
            problems.append('%s at %s hangs from nothing' % (short, (x, y, z)))
        if name == 'minecraft:ladder':
            dx, dz = STEP[OPPOSITE[p['facing']]]
            if not full(site.get(x + dx, y, z + dz)):
                problems.append('ladder at %s has no wall' % ((x, y, z),))
    return problems


# Block light by emitter, as BlockInit gives it.
LIGHT = {'hemomancy:hematic_lantern': 15, 'hemomancy:circus_footlight': 12, 'hemomancy:circus_fire_pit': 13,
         'hemomancy:circus_cookpot': 9, 'hemomancy:circus_hound_hoop': 7}


def stops_light(entry):
    return entry is not None and entry[0] not in NOT_FLAT and (
        mausoleum_checks.cube(entry) or entry[0].endswith(('_stairs', '_slab')))


def spawn_floor(entry):
    """Whether a monster can stand on top of this block: a cube, a top or double slab, an upside-down stair."""
    if entry is None:
        return False
    name, props, _n = entry
    p = dict(props)
    if name.endswith('_slab'):
        return p.get('type') in ('top', 'double')
    if name.endswith('_stairs'):
        return p.get('half') == 'top'
    return name != 'hemomancy:circus_net' and mausoleum_checks.cube(entry)


def block_light(site):
    level = {}
    queue = deque()
    for pos, (name, _p, _n) in site.blocks.items():
        if name in LIGHT:
            level[pos] = LIGHT[name]
            queue.append(pos)
    while queue:
        x, y, z = queue.popleft()
        value = level[(x, y, z)] - 1
        if value <= 0:
            continue
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            at = (x + dx, y + dy, z + dz)
            if not site.inside(*at) or level.get(at, 0) >= value or stops_light(site.blocks.get(at)):
                continue
            level[at] = value
            queue.append(at)
    return level


def light_problems(site):
    if site.interior is None:
        return []
    level = block_light(site)
    dark = []
    for (x, y, z), entry in site.blocks.items():
        if entry[0] != AIR or not site.interior(x, y, z) or not spawn_floor(site.blocks.get((x, y - 1, z))):
            continue
        if level.get((x, y, z), 0) == 0:
            dark.append((x, y, z))
    return ['dark standable cell at %s' % (pos,) for pos in sorted(dark)]


def all_problems(name, site):
    found = (state_problems(site) + metal_problems(site) + multiblock_problems(site) + mount_problems(site)
             + flat_problems(site) + light_problems(site))
    return ['%s: %s' % (name, p) for p in found]
