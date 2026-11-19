"""The Crimson Troupe site kit (chunk 15 T7): a Site canvas and the set pieces every Circus site is built from.

The big top (king pole, quarter poles, the ring and its curb, seating tiers, galleries and their stairs, the
Ringmaster's box, rigging towers with a trapeze and a net, the valance and guy lines, laced canvas walls over a
wainscot), the backyard (painted wagons, the cookhouse lean-to, the dressing tent), the midway (the ticket booth and
the banner line), one apparatus per teaching camp, and the wrecked stage wagon. The set-piece blocks are modelled in
tools/model_export/circus_set_pieces.py; this kit places them, multiblocks whole.

Frames. Every assembly is written in a Frame: local coordinates with "forward" toward -z (north), turned a quarter at
a time about an origin. A frame turns what it writes too: a `facing` turns with it and an `axis` swaps x and z, so an
assembly reads the same whichever way a site faces it. Fences, walls and bars are joined by Site.finish().

Materials are the Troupe's: blood-wood, Crimson Lacquer, hematic iron, bone, canvas and thread. Nothing in the copper
family and no gilt: FORBIDDEN is checked on every template.
"""
import gzip
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import nbtlib  # noqa: E402
from nbtlib import Byte, Compound, Double, Int, List, String  # noqa: E402
from wayside.kit import Site as WaysideSite  # noqa: E402  (the structure canvas with put/finish)
from wayside.kit import full  # noqa: E402

AIR = 'minecraft:air'
FORBIDDEN = ('copper', 'brass', 'bronze', 'gold', 'gilded')
TURN = ('north', 'east', 'south', 'west')

KING_POLE = 'hemomancy:circus_king_pole'
QUARTER_POLE = 'hemomancy:circus_quarter_pole'
CANVAS = 'hemomancy:circus_canvas'
LACED = 'hemomancy:circus_laced_canvas'
WAINSCOT = 'hemomancy:circus_wainscot'
SAWDUST = 'hemomancy:circus_sawdust'
PANEL = 'hemomancy:circus_wagon_panel'
WRECKED = 'hemomancy:circus_wrecked_panel'
PLANKS = 'hemomancy:blood_wood_planks'
PLANK_SLAB = 'hemomancy:blood_wood_slab'
PLANK_STAIRS = 'hemomancy:blood_wood_stairs'
FENCE = 'hemomancy:blood_wood_fence'
LOG = 'hemomancy:stripped_blood_wood_log'
LACQUER = 'hemomancy:dark_lacquer_planks'
LACQUER_SLAB = 'hemomancy:dark_lacquer_planks_slab'
LACQUER_STAIRS = 'hemomancy:dark_lacquer_planks_stairs'
IRON = 'hemomancy:hematic_iron_plating'
CHAIN = 'hemomancy:hematic_iron_chain'
STONE = 'hemomancy:polished_venous_stone_bricks'
BONE = 'hemomancy:bone_bricks'

# Every set-piece block of the kit, by how it is placed (the template test reads this list from the sampler).
FACING_PIECES = ('circus_seating', 'circus_gallery_rail',
                 'circus_trapeze', 'circus_valance', 'circus_guy_stake', 'circus_footlight', 'circus_crossbar_rig',
                 'circus_spindle_rack', 'circus_trophy', 'circus_pelt_drape', 'circus_cookpot', 'circus_costume_rack',
                 'circus_trunk', 'circus_ticket_window', 'circus_pennant_line', 'circus_target_board',
                 'circus_weight_frame', 'circus_fire_pit', 'circus_bellows_stand', 'circus_hound_hoop',
                 'circus_prompt_table', 'circus_stage_record', 'circus_loft_record', 'circus_quarters_record',
                 'impaled_procession_record')
TALL_PIECES = ('circus_podium', 'circus_knife_wheel', 'circus_stilt_rack')
POLES = ('circus_king_pole', 'circus_quarter_pole')
SURFACES = ('circus_canvas', 'circus_laced_canvas', 'circus_wainscot', 'circus_sawdust', 'circus_wagon_panel',
            'circus_wrecked_panel', 'circus_plinth', 'circus_net')
WHEEL = 'circus_wagon_wheel'
# The ring curb takes a rotation of eight 45 degree steps clockwise from north, so it can follow a ring's diagonals.
OCTAGONAL_PIECES = ('circus_ring_curb',)
ALL_PIECES = OCTAGONAL_PIECES + FACING_PIECES + TALL_PIECES + POLES + SURFACES + (WHEEL,)


def turned(facing, turns):
    if facing not in TURN:
        return facing
    return TURN[(TURN.index(facing) + turns) % 4]


def clockwise(facing):
    return turned(facing, 1)


STEP = {'north': (0, -1), 'east': (1, 0), 'south': (0, 1), 'west': (-1, 0)}


class Site(WaysideSite):
    """A Circus template. `ground` is the row that lands on the column's top ground block.

    `entities` are the actors the template places (an id and the cell its feet stand in). `interior`, when set, says
    which cells are under the site's roofs: checks.py wants block light in every standable one."""

    def __init__(self, sx, sy, sz, ground, seed):
        super().__init__(sx, sy, sz, ground, seed)
        self.entities = []
        self.interior = None

    def entity(self, kind, x, y, z):
        self.entities.append((kind, x, y, z))

    def save(self, path):
        """Writes the template and its actors, then gzips it again with no timestamp so every run writes the same
        bytes."""
        super().save(path)
        if self.entities:
            root = nbtlib.load(path)
            root['entities'] = List[Compound]([Compound({
                'pos': List[Double]([Double(x + .5), Double(y), Double(z + .5)]),
                'blockPos': List[Int]([Int(x), Int(y), Int(z)]),
                'nbt': Compound({'id': String('hemomancy:' + kind), 'PersistenceRequired': Byte(1)}),
            }) for kind, x, y, z in self.entities])
            root.save(path, gzipped=True)
        with open(path, 'rb') as f:
            data = gzip.decompress(f.read())
        with open(path, 'wb') as f:
            f.write(gzip.compress(data, mtime=0))

    def frame(self, x, y, z, facing='north'):
        return Frame(self, x, y, z, TURN.index(facing))

    def finish(self):
        """Joins as the wayside kit does, then seals every pole section walled in on all four sides by full cubes
        (where a pole runs through the roof canvas), as the block does in play."""
        super().finish()
        for (x, y, z), (name, props, nbt) in list(self.blocks.items()):
            if name.split(':')[-1] not in POLES:
                continue
            walled = all(full(self.get(x + dx, y, z + dz)) for dx, dz in STEP.values())
            p = dict(props)
            p['sealed'] = 'true' if walled else 'false'
            self.set(x, y, z, name, p, nbt)
        # A net's sides, as TroupeNetBlock.side works them out: more net, a pole or solid block to tie off on, or edge.
        for (x, y, z), (name, props, nbt) in list(self.blocks.items()):
            if name != 'hemomancy:circus_net':
                continue
            p = dict(props)
            for side, (dx, dz) in STEP.items():
                other = self.get(x + dx, y, z + dz) or ''
                p[side] = 'net' if other == name else 'tied' if (other.split(':')[-1] in POLES or full(other))                     else 'edge'
            self.set(x, y, z, name, p, nbt)


class Frame:
    """Local coordinates over a Site: forward is -z; `turns` quarter turns clockwise about the origin."""

    def __init__(self, site, ox, oy, oz, turns=0):
        self.site, self.o, self.turns = site, (ox, oy, oz), turns

    def at(self, x, y, z):
        for _ in range(self.turns):
            x, z = -z, x
        return self.o[0] + x, self.o[1] + y, self.o[2] + z

    def facing(self, facing):
        return turned(facing, self.turns)

    def sub(self, x, y, z, facing='north'):
        """A frame inside this one, at local (x, y, z), turned to face `facing` in this frame."""
        wx, wy, wz = self.at(x, y, z)
        return Frame(self.site, wx, wy, wz, (self.turns + TURN.index(facing)) % 4)

    def put(self, x, y, z, name, props=None, nbt=None):
        props = dict(props or {})
        if 'facing' in props:
            props['facing'] = self.facing(props['facing'])
        if 'rotation' in props:
            props['rotation'] = str((int(props['rotation']) + 2 * self.turns) % 8)
        if 'axis' in props and self.turns % 2 and props['axis'] in ('x', 'z'):
            props['axis'] = 'z' if props['axis'] == 'x' else 'x'
        self.site.put(*self.at(x, y, z), name, props, nbt)

    def get(self, x, y, z):
        return self.site.get(*self.at(x, y, z))

    def fill(self, x0, x1, y0, y1, z0, z1, name, props=None):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    value = name(x, y, z) if callable(name) else name
                    if value is None:
                        continue
                    if isinstance(value, tuple):
                        self.put(x, y, z, *value)
                    else:
                        self.put(x, y, z, value, props)

    def air(self, x0, x1, y0, y1, z0, z1):
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                for z in range(z0, z1 + 1):
                    self.site.air(*self.at(x, y, z))

    # --- single set pieces ----------------------------------------------------------------------------------------

    def piece(self, x, y, z, name, facing='north'):
        self.put(x, y, z, 'hemomancy:' + name, {'facing': facing})

    def tall(self, x, y, z, name, facing='north'):
        self.put(x, y, z, 'hemomancy:' + name, {'facing': facing, 'half': 'lower'})
        self.put(x, y + 1, z, 'hemomancy:' + name, {'facing': facing, 'half': 'upper'})

    def wheel(self, x, y, z, facing):
        """A wagon wheel on the `facing` side of a wagon, its lower near quarter at (x, y, z); the far quarters run
        toward `facing`'s clockwise side, as TroupeWagonWheelBlock.offset has it."""
        dx, dz = STEP[clockwise(facing)]
        for q in range(4):
            a, up = q & 1, q >> 1
            self.put(x + dx * a, y + up, z + dz * a, 'hemomancy:' + WHEEL, {'facing': facing, 'quarter': str(q)})

    def stairs(self, x, y, z, name, facing, half='bottom', shape='straight'):
        self.put(x, y, z, name, {'facing': facing, 'half': half, 'shape': shape})

    def slab(self, x, y, z, name, kind='bottom'):
        self.put(x, y, z, name, {'type': kind})

    def log(self, x, y, z, axis='y', name=LOG):
        self.put(x, y, z, name, {'axis': axis})


# --- the big top ------------------------------------------------------------------------------------------------------

def pole(f, x, y, z, height, kind=KING_POLE, collars=()):
    """A pole `height` blocks tall from (x, y, z): a foot, shaft, a collar at each listed rise, a bone finial."""
    for i in range(height):
        section = 'foot' if i == 0 else 'finial' if i == height - 1 else 'collar' if i in collars else 'shaft'
        f.put(x, y + i, z, kind, {'section': section})


def circle_cells(r):
    """The midpoint circle of radius r about (0, 0): a ring one cell thick, stepping on the diagonals."""
    cells, x, z, err = set(), r, 0, 1 - r
    while x >= z:
        for a, b in ((x, z), (z, x), (-z, x), (-x, z), (-x, -z), (-z, -x), (z, -x), (x, -z)):
            cells.add((a, b))
        z += 1
        if err < 0:
            err += 2 * z + 1
        else:
            x -= 1
            err += 2 * (z - x) + 1
    return sorted(cells)


# The curb's rotation, by its step toward the ring's centre: 45 degree steps clockwise from north (odd steps are the
# diagonal model, whose panelled face looks the same way).
CURB_ROTATION = {(0, -1): 0, (1, -1): 1, (1, 0): 2, (1, 1): 3, (0, 1): 4, (-1, 1): 5, (-1, 0): 6, (-1, -1): 7}


def ring(f, cx, y, cz, r, footlights=()):
    """The sawdust ring of radius r centred on (cx, cz), floored at y - 1, with its curb at y. A footlight stands
    behind the curb at each listed angle (degrees, 0 toward +x), shining in."""
    for dx in range(-r, r + 1):
        for dz in range(-r, r + 1):
            if math.hypot(dx, dz) < r - .3:
                f.put(cx + dx, y - 1, cz + dz, SAWDUST)
    for dx, dz in circle_cells(r):
        angle = math.degrees(math.atan2(-dz, -dx)) % 360          # from the cell toward the centre
        octant = int(((angle + 22.5) % 360) // 45)
        sx = 1 if octant in (7, 0, 1) else -1 if octant in (3, 4, 5) else 0
        sz = 1 if octant in (1, 2, 3) else -1 if octant in (5, 6, 7) else 0
        f.put(cx + dx, y, cz + dz, 'hemomancy:circus_ring_curb', {'rotation': str(CURB_ROTATION[(sx, sz)])})
        f.put(cx + dx, y - 1, cz + dz, STONE)
    for angle in footlights:
        a = math.radians(angle)
        lx, lz = round(cx + (r + 1) * math.cos(a)), round(cz + (r + 1) * math.sin(a))
        toward = (-round(math.cos(a)), -round(math.sin(a)))
        facing = min(STEP, key=lambda k: (STEP[k][0] - toward[0]) ** 2 + (STEP[k][1] - toward[1]) ** 2)
        f.put(lx, y - 1, lz, STONE)
        f.put(lx, y, lz, 'hemomancy:circus_footlight', {'facing': facing})


def plinth(f, x, y, z):
    """The plinth the Impaled Procession turns on: a bone-banded drum on a stepped venous-stone footing."""
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if dx and dz:
                f.slab(x + dx, y, z + dz, 'hemomancy:polished_venous_stone_slab')
            elif dx or dz:
                f.stairs(x + dx, y, z + dz, 'hemomancy:polished_venous_stone_stairs',
                         {(1, 0): 'west', (-1, 0): 'east', (0, 1): 'north', (0, -1): 'south'}[(dx, dz)])
    f.put(x, y, z, 'hemomancy:circus_plinth')


def seating(f, x0, x1, y, z, rows):
    """Seating tiers facing north (toward the ring) from row z, each row a step back and up; blood-wood stringers
    at both ends and a wainscot riser under each row."""
    for i in range(rows):
        for x in range(x0, x1 + 1):
            f.put(x, y + i, z + i, 'hemomancy:circus_seating', {'facing': 'north'})
            for below in range(y, y + i):
                f.put(x, below, z + i, WAINSCOT)
        for x in (x0 - 1, x1 + 1):
            for up in range(y, y + i + 1):
                f.log(x, up, z + i)
    for x in range(x0 - 1, x1 + 2):
        for up in range(y, y + rows):
            if x0 <= x <= x1:
                f.put(x, up, z + rows, WAINSCOT)
            else:
                f.log(x, up, z + rows)
        f.put(x, y + rows, z + rows, 'hemomancy:circus_gallery_rail', {'facing': 'north'})


def gallery(f, x0, x1, y, z, ground, depth=2, posts=4, rail_side='north'):
    """A gallery deck at y from x0 to x1, `depth` deep from z, on quarter poles every `posts` blocks from the
    ground row; a lacquer beam under the open edge and a rail along it. The deck runs blood-wood with a lacquer
    board every third, so no stretch of it is one flat block."""
    edge = z if rail_side == 'north' else z + depth - 1
    for x in range(x0, x1 + 1):
        for d in range(depth):
            f.put(x, y, z + d, LACQUER if (x - x0) % 3 == 2 else PLANKS)
        f.log(x, y - 1, edge, 'x')
        f.put(x, y + 1, edge, 'hemomancy:circus_gallery_rail', {'facing': rail_side})
    for x in list(range(x0, x1 + 1, posts)) + [x1]:
        pole(f, x, ground, edge, y - 1 - ground, QUARTER_POLE, collars=(max(1, (y - 1 - ground) // 2),))


def gallery_stairs(f, x, y, z, rise, facing='north'):
    """A flight of `rise` steps climbing toward `facing` from (x, y, z): blood-wood treads, a sawtooth of
    upside-down lacquer stairs under them and a rail on each side."""
    dx, dz = STEP[facing]
    side = STEP[clockwise(facing)]
    for i in range(rise):
        sx, sz = x + dx * i, z + dz * i
        for w in (0, 1):
            f.stairs(sx + side[0] * w, y + i, sz + side[1] * w, PLANK_STAIRS, facing)
            if i:
                f.stairs(sx + side[0] * w, y + i - 1, sz + side[1] * w, LACQUER_STAIRS, turned(facing, 2), 'top')
        for w in (-1, 2):
            f.put(sx + side[0] * w, y + i, sz + side[1] * w, FENCE)


def ringmaster_box(f, x, y, z):
    """The Ringmaster's box on the upper gallery: a five-wide bay over the ring, railed in front and at the sides,
    quarter poles at its front corners carrying a valance, his podium at the back, and his trophy and Spindle rack on
    the back canvas. Its deck is at y; a player walks in from the gallery through the openings at its back corners."""
    for dx in range(-2, 3):
        for dz in range(0, 3):
            f.put(x + dx, y, z + dz, LACQUER if dz == 0 or dx in (-2, 2) else PLANKS)
    for dx in range(-1, 2):
        f.put(x + dx, y + 1, z, 'hemomancy:circus_gallery_rail', {'facing': 'north'})
        f.put(x + dx, y + 4, z, 'hemomancy:circus_valance', {'facing': 'north'})
    for dx in (-2, 2):
        pole(f, x + dx, y + 1, z, 4, QUARTER_POLE, collars=(2,))
        f.put(x + dx, y + 1, z + 1, 'hemomancy:circus_gallery_rail', {'facing': 'east'})
    f.tall(x, y + 1, z + 2, 'circus_podium', 'north')
    for dx in range(-2, 3):
        for up in range(1, 5):
            if dx in (-2, 2) and up <= 2:
                f.put(x + dx, y + up, z + 3, AIR)
            else:
                f.put(x + dx, y + up, z + 3, WAINSCOT if up == 1 else LACED if dx in (-2, 2) else CANVAS)
    f.piece(x - 1, y + 3, z + 2, 'circus_trophy', 'north')
    f.piece(x + 1, y + 3, z + 2, 'circus_spindle_rack', 'north')


def rigging_tower(f, x, y, z, height, net_y=None, net_reach=7):
    """A rigging tower: a 3 x 3 platform `height` up on four quarter poles, a ladder up its back, rails on three
    sides, a trapeze hung from an arm reaching north, and a net under the swing, `net_reach` rows out from the tower
    (at `net_y`, the floor unless given)."""
    top = y + height
    for dx, dz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        pole(f, x + dx, y, z + dz, height + 2, QUARTER_POLE, collars=(height // 2, height))
    for dx, dz in ((0, 0), (-1, 0), (1, 0), (0, -1)):
        f.slab(x + dx, top, z + dz, PLANK_SLAB, 'top')
    f.put(x, top, z + 1, PLANKS)
    f.put(x, top + 1, z - 1, 'hemomancy:circus_gallery_rail', {'facing': 'north'})
    f.put(x - 1, top + 1, z, 'hemomancy:circus_gallery_rail', {'facing': 'east'})
    f.put(x + 1, top + 1, z, 'hemomancy:circus_gallery_rail', {'facing': 'east'})
    for up in range(y, top + 1):
        f.put(x, up, z + 2, 'minecraft:ladder', {'facing': 'south', 'waterlogged': 'false'})
        if up < top:
            f.log(x, up, z + 1)
    for dx in (-1, 0, 1):
        f.log(x + dx, top + 2, z - 1, 'x')
    for dz in range(-4, -1):
        f.log(x, top + 2, z + dz, 'z')
    f.put(x, top + 1, z - 4, 'hemomancy:circus_trapeze', {'facing': 'east'})
    net_y = y if net_y is None else net_y
    for dx in range(-2, 3):
        for dz in range(-net_reach, -1):
            f.put(x + dx, net_y, z + dz, 'hemomancy:circus_net')


def valance_band(f, x0, x1, y, z, facing='north'):
    for x in range(x0, x1 + 1):
        f.put(x, y, z, 'hemomancy:circus_valance', {'facing': facing})


def canvas_wall(f, x0, x1, y0, y1, z, lace_every=3):
    """A big-top wall along x at z: two courses of wainscot, then striped canvas laced shut every `lace_every`
    columns, so no run of one canvas is more than two wide."""
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            if y < y0 + 2:
                f.put(x, y, z, WAINSCOT)
            else:
                f.put(x, y, z, LACED if (x - x0) % lace_every == 0 else CANVAS)


def guy_line(f, x, y, z, toward='north'):
    f.piece(x, y, z, 'circus_guy_stake', toward)


# --- the backyard and the midway --------------------------------------------------------------------------------------

def wagon(f, x, y, z, wrecked=False):
    """A painted wagon three wide and five long, its length along z from (x, y, z) at the front-left wheel cell's
    corner: chassis on four wagon wheels, panelled sides on blood-wood posts, a gabled lacquer roof, steps and a
    door at the back, the shaft forward. `wrecked` burns the panels, opens the roof and drops the west wheels."""
    panel = WRECKED if wrecked else PANEL
    body = range(x + 1, x + 4)
    for bx in body:
        for bz in range(z, z + 5):
            f.slab(bx, y, bz, LACQUER_SLAB, 'top')
            f.put(bx, y + 1, bz, LACQUER)
    for side, wx in (('west', x), ('east', x + 4)):
        if wrecked and side == 'west':
            for bz in range(z, z + 5):
                f.slab(x + 1, y - 1, bz, PLANK_SLAB, 'top' if bz % 2 else 'bottom')
            continue
        f.wheel(wx, y - 1, z + (1 if side == 'west' else 0), side)
        f.wheel(wx, y - 1, z + (4 if side == 'west' else 3), side)
    for bz in range(z, z + 5):
        for bx in body:
            corner = bx in (x + 1, x + 3) and bz in (z, z + 4)
            for up in (2, 3):
                if corner:
                    f.log(bx, y + up, bz)
                elif bx in (x + 1, x + 3) or bz in (z, z + 4):
                    f.put(bx, y + up, bz, panel)
    f.put(x + 2, y + 2, z + 4, 'hemomancy:blood_wood_door', {'facing': 'north', 'half': 'lower', 'hinge': 'left',
                                                             'open': 'false', 'powered': 'false'})
    f.put(x + 2, y + 3, z + 4, 'hemomancy:blood_wood_door', {'facing': 'north', 'half': 'upper', 'hinge': 'left',
                                                             'open': 'false', 'powered': 'false'})
    f.stairs(x + 2, y + 1, z + 5, PLANK_STAIRS, 'north')
    f.stairs(x + 2, y, z + 6, PLANK_STAIRS, 'north')
    for bz in range(z - 1, z + 6):
        if not (wrecked and bz < z + 2):
            f.stairs(x + 1, y + 4, bz, LACQUER_STAIRS, 'east')
        f.stairs(x + 3, y + 4, bz, LACQUER_STAIRS, 'west')
        if z <= bz <= z + 4:
            f.put(x + 2, y + 4, bz, LACQUER)
        if not (wrecked and bz % 2):
            f.slab(x + 2, y + 5, bz, LACQUER_SLAB)
    for bz in (z - 1, z - 2):
        f.put(x + 2, y, bz, FENCE)
    if not wrecked:
        f.piece(x + 2, y + 3, z - 1, 'circus_pennant_line', 'north')


def wheel_spares(f, x, y, z):
    """A wrecked wagon's dropped wheel, propped upright on its own, with a loose board beside it."""
    f.wheel(x, y, z, 'west')
    f.slab(x + 1, y, z - 1, PLANK_SLAB)


def cookhouse(f, x, y, z):
    """The cookhouse lean-to: a laced canvas back wall, a lacquer roof sloping forward onto two quarter poles, the
    cook pot on its hearth, a trunk and a crossbar rig of drying thread."""
    for dx in range(0, 5):
        for up in range(0, 4):
            f.put(x + dx, y + up, z + 3, WAINSCOT if up < 1 else LACED if dx in (0, 4) else CANVAS)
        for dz, up in ((0, 2), (1, 3), (2, 4)):
            f.stairs(x + dx, y + up, z + dz, LACQUER_STAIRS, 'south')
        f.put(x + dx, y + 4, z + 3, LACQUER)
    for dx in (0, 4):
        pole(f, x + dx, y, z, 2, QUARTER_POLE)
    f.piece(x + 2, y, z + 1, 'circus_cookpot', 'north')
    f.piece(x + 4, y, z + 2, 'circus_trunk', 'west')
    f.piece(x, y, z + 2, 'circus_costume_rack', 'east')


def dressing_tent(f, x, y, z):
    """The dressing tent: five by five, laced canvas walls on a wainscot, a stepped canvas roof to a quarter-pole
    peak, costume racks and trunks inside, a mask wall facing the door."""
    for dx in range(5):
        for dz in range(5):
            f.put(x + dx, y - 1, z + dz, PLANKS if (dx + dz) % 3 else LACQUER)
            if dx in (0, 4) or dz in (0, 4):
                for up in range(0, 3):
                    corner = dx in (0, 4) and dz in (0, 4)
                    f.put(x + dx, y + up, z + dz, WAINSCOT if up == 0 else LACED if corner or (dx + dz) % 3 == 0
                          else CANVAS)
    for up in (0, 1):
        f.put(x + 2, y + up, z, AIR)
    for ring_i, (a, b) in enumerate(((0, 4), (1, 3))):
        for dx in range(a, b + 1):
            for dz in range(a, b + 1):
                if dx in (a, b) or dz in (a, b):
                    f.put(x + dx, y + 3 + ring_i, z + dz, CANVAS if (dx + dz + ring_i) % 2 else LACED)
    pole(f, x + 2, y, z + 2, 6, QUARTER_POLE, collars=(3,))
    f.piece(x + 1, y, z + 3, 'circus_costume_rack', 'south')
    f.piece(x + 3, y, z + 3, 'circus_trunk', 'north')
    f.piece(x + 3, y, z + 1, 'circus_trunk', 'west')
    for dx, mask in ((1, 'laughing_mask'), (2, 'mourning_mask'), (3, 'blank_mask')):
        f.piece(x + dx, y + 1, z + 3, mask, 'north')
    f.piece(x + 1, y + 1, z + 1, 'circus_pelt_drape', 'east')


def ticket_booth(f, x, y, z):
    """The midway's ticket booth: a three-by-three of painted panels, the ticket window in its front, a lacquer
    roof, and the banner line run off its corner to a quarter pole."""
    for dx in range(3):
        for dz in range(3):
            if dx in (0, 2) or dz in (0, 2):
                for up in range(0, 3):
                    corner = dx in (0, 2) and dz in (0, 2)
                    if corner:
                        f.log(x + dx, y + up, z + dz)
                    else:
                        f.put(x + dx, y + up, z + dz, PANEL)
            f.slab(x + dx, y + 3, z + dz, LACQUER_SLAB)
    f.put(x + 1, y, z, AIR)
    f.put(x + 1, y + 1, z, AIR)
    f.piece(x + 1, y, z, 'circus_ticket_window', 'north')
    f.put(x + 1, y + 1, z, 'hemomancy:circus_valance', {'facing': 'north'})
    f.put(x + 1, y + 1, z + 2, AIR)
    f.put(x + 1, y, z + 2, AIR)
    banner_line(f, x + 3, x + 8, y + 3, z)


def banner_line(f, x0, x1, y, z):
    """Quarter poles at each end, pennant lines strung between them at y."""
    pole(f, x1, y - 3, z, 5, QUARTER_POLE, collars=(3,))
    for x in range(x0, x1):
        f.put(x, y, z, 'hemomancy:circus_pennant_line', {'facing': 'north'})


# --- the camps: one apparatus each ----------------------------------------------------------------------------------

def air_apparatus(f, x, y, z):
    rigging_tower(f, x, y, z, 4)


def blades_apparatus(f, x, y, z):
    f.tall(x, y, z, 'circus_knife_wheel', 'south')
    f.piece(x + 2, y, z, 'circus_target_board', 'south')
    f.piece(x - 2, y, z, 'circus_target_board', 'south')
    f.piece(x, y, z - 2, 'circus_trunk', 'south')


def pageantry_apparatus(f, x, y, z):
    f.tall(x, y, z, 'circus_stilt_rack', 'south')
    f.piece(x + 2, y, z + 1, 'circus_weight_frame', 'south')
    f.piece(x - 2, y, z + 1, 'circus_weight_frame', 'east')


def embers_apparatus(f, x, y, z):
    f.piece(x, y, z, 'circus_fire_pit', 'north')
    f.piece(x + 2, y, z, 'circus_bellows_stand', 'west')
    f.piece(x, y, z - 2, 'circus_hound_hoop', 'south')
    f.piece(x, y, z + 2, 'circus_hound_hoop', 'north')


def memory_apparatus(f, x, y, z):
    f.piece(x, y, z, 'circus_prompt_table', 'south')
    f.piece(x + 2, y, z, 'circus_trunk', 'south')
    f.piece(x - 2, y, z, 'circus_costume_rack', 'south')
    f.piece(x, y, z + 2, 'circus_quarters_record', 'north')


APPARATUS = {'air': air_apparatus, 'blades': blades_apparatus, 'pageantry': pageantry_apparatus,
             'embers': embers_apparatus, 'memory': memory_apparatus}


def lean_to(f, x, y, z, width=3):
    """A camp's canvas lean-to: a laced canvas back on a wainscot, a lacquer roof onto two quarter poles."""
    for dx in range(width):
        for up in range(3):
            f.put(x + dx, y + up, z + 2, WAINSCOT if up == 0 else LACED if dx in (0, width - 1) else CANVAS)
        f.stairs(x + dx, y + 2, z, LACQUER_STAIRS, 'south')
        f.stairs(x + dx, y + 3, z + 1, LACQUER_STAIRS, 'south')
        f.put(x + dx, y + 3, z + 2, LACQUER)
    for dx in (0, width - 1):
        pole(f, x + dx, y, z, 2, QUARTER_POLE)


def stage_wreck(f, x, y, z):
    """The abandoned stage: a wrecked stage wagon with its boards run out west as a broken stage, the stage bill
    still standing on them, and one wheel propped by the road."""
    wagon(f, x, y, z, wrecked=True)
    for dx in range(-2, 1):
        for dz in range(0, 5):
            if (dx, dz) not in ((-2, 0), (-2, 4), (-1, 4)):
                f.slab(x + dx, y + 1, z + dz, PLANK_SLAB, 'top')
    f.piece(x - 1, y + 2, z + 2, 'circus_stage_record', 'west')
    wheel_spares(f, x - 3, y - 1, z + 6)
