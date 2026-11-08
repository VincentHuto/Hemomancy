"""Light the Crimson Troupe pavilion and the teaching camps so no walkable interior floor can spawn monsters.

A monster spawns in 1.21 only where block light is 0. This script reads the shipped structure NBTs, works out
block light from the light sources already in them, and hangs Hematic Lanterns (the troupe's lamp) from roofs,
canvas and iron beams until every standable interior cell has block light of at least 1. It then prints the
dark-cell counts before and after.

It edits the shipped templates in place because their builder (build_troupe_assets.py) cannot run (audit W-3).
It only fills air or unlisted cells, appends the lantern states to the end of the palette and leaves DataVersion,
entities and every other block alone. Running it again changes nothing.

    python -I tools/circus/light_pavilion.py            # write
    python -I tools/circus/light_pavilion.py --check    # count only
"""
from pathlib import Path
import shutil
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent))
import nbt_io  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
STRUCTURES = ROOT / 'src/main/resources/data/hemomancy/structure'
BACKUP = ROOT / 'removed_assets/src/main/resources/data/hemomancy/structure'

LANTERN = 'hemomancy:hematic_lantern'
HANGING = {'hanging': 'true', 'waterlogged': 'false'}

# Light each block gives off (only those that occur in these templates).
EMITTERS = {'minecraft:lantern': 15, 'hemomancy:hematic_lantern': 15, 'hemomancy:pallid_lantern': 15}

# Full cubes: they stop light, and a monster can stand on them.
FULL = {
    'hemomancy:polished_venous_stone', 'hemomancy:venous_stone', 'hemomancy:hematic_iron_pillar',
    'hemomancy:puppeteers_wool', 'hemomancy:circus_quarters_record', 'hemomancy:circus_loft_record',
    'hemomancy:circus_stage_record', 'hemomancy:impaled_procession_record',
    'minecraft:red_wool', 'minecraft:blue_wool', 'minecraft:white_wool', 'minecraft:bone_block', 'minecraft:target',
    'minecraft:dark_oak_planks', 'minecraft:crafting_table', 'minecraft:hay_block', 'minecraft:barrel',
    'minecraft:smoker', 'minecraft:red_terracotta', 'minecraft:smooth_stone', 'minecraft:grass_block',
    'minecraft:coarse_dirt', 'minecraft:gravel', 'minecraft:dark_oak_log', 'minecraft:bookshelf',
    'minecraft:blackstone', 'minecraft:polished_blackstone', 'minecraft:stone', 'minecraft:dirt',
}
# Blocks light passes through unchanged (no occlusion, light block 0).
CLEAR = {
    'minecraft:air', 'hemomancy:circus_curtain', 'hemomancy:hematic_iron_chain', 'minecraft:lantern',
    'hemomancy:hematic_lantern', 'minecraft:ladder', 'hemomancy:blank_mask', 'hemomancy:laughing_mask',
    'hemomancy:mourning_mask', 'hemomancy:troupe_rigging', 'minecraft:red_banner', 'minecraft:white_banner',
    'minecraft:dark_oak_fence', 'minecraft:dark_oak_trapdoor',
}
# A cell a monster's feet can occupy (no collision).
SPAWN_EMPTY = {'minecraft:air', 'minecraft:red_banner', 'minecraft:white_banner'}
# Anything else (slabs, anvils, cauldrons, jars, spindles, beds) is counted as stopping light: the computed light
# is never more than the game's, so a cell this script calls lit is lit in game.

CARPET_SUFFIX = '_carpet'


class Site:
    def __init__(self, name, protect=(), keep_clear=None):
        self.name = name
        self.path = STRUCTURES / (name + '.nbt')
        self.root = nbt_io.load(self.path)
        body = self.root[1][1]
        self.body = body
        self.size = tuple(body['size'][1][1])
        self.palette = body['palette'][1][1]
        self.blocks = body['blocks'][1][1]
        self.names = [entry['Name'][1] for entry in self.palette]
        self.cells = {tuple(entry['pos'][1][1]): i for i, entry in enumerate(self.blocks)}
        self.protect = set(protect)
        for entity in body['entities'][1][1]:
            x, y, z = entity['blockPos'][1][1]
            self.protect.update({(x, y, z), (x, y + 1, z)})
        self.keep_clear = keep_clear or (lambda x, y, z: False)

    def name_at(self, pos):
        index = self.cells.get(pos)
        if index is None:
            return 'minecraft:air' if pos[1] > 0 else 'terrain'
        return self.names[self.blocks[index]['state'][1]]

    def inside(self, pos):
        return all(0 <= v < bound for v, bound in zip(pos, self.size))

    # -- block classes -------------------------------------------------------------------------------------------
    def full(self, pos):
        name = self.name_at(pos)
        return name == 'terrain' or name in FULL

    def clear(self, pos):
        name = self.name_at(pos)
        return name in CLEAR or name.endswith(CARPET_SUFFIX)

    def standable(self, pos):
        """A monster could spawn with its feet here: empty feet cell over a full block."""
        x, y, z = pos
        return y >= 1 and self.name_at(pos) in SPAWN_EMPTY and self.full((x, y - 1, z))

    def covered(self, pos):
        x, y, z = pos
        return any(self.full((x, above, z)) for above in range(y + 1, self.size[1]))

    # -- light ---------------------------------------------------------------------------------------------------
    def light(self, extra=()):
        level = {}
        frontier = []
        sources = [(pos, EMITTERS[self.name_at(pos)]) for pos in self.cells if self.name_at(pos) in EMITTERS]
        sources += [(pos, 15) for pos in extra]
        for pos, value in sources:
            if level.get(pos, 0) < value:
                level[pos] = value
                frontier.append(pos)
        while frontier:
            nxt = []
            for pos in frontier:
                value = level[pos] - 1
                if value <= 0:
                    continue
                x, y, z = pos
                for step in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
                    near = (x + step[0], y + step[1], z + step[2])
                    if not self.inside(near) or not self.clear(near) or near in extra:
                        continue
                    if level.get(near, 0) < value:
                        level[near] = value
                        nxt.append(near)
            frontier = nxt
        return level

    def interior(self):
        sx, sy, sz = self.size
        return [(x, y, z) for x in range(sx) for y in range(1, sy) for z in range(sz)
                if self.standable((x, y, z)) and self.covered((x, y, z))]

    def dark(self, extra=()):
        level = self.light(extra)
        return [pos for pos in self.interior() if level.get(pos, 0) == 0 and pos not in extra]

    # -- where a lantern may hang --------------------------------------------------------------------------------
    def can_hang(self, pos):
        x, y, z = pos
        if not self.inside(pos) or pos in self.protect or self.keep_clear(x, y, z):
            return False
        here = self.name_at(pos)
        if here != 'minecraft:air' and not here.endswith(CARPET_SUFFIX):
            return False
        if not self.full((x, y + 1, z)):
            return False
        # Headroom: the lantern hangs at least three cells above whatever is stood on beneath it.
        below = y - 1
        while below > 0 and self.name_at((x, below, z)) == 'minecraft:air':
            below -= 1
        feet = below + 1 if self.full((x, below, z)) else below
        return y - feet >= 3

    def candidates(self):
        sx, sy, sz = self.size
        return [(x, y, z) for x in range(sx) for y in range(1, sy - 1) for z in range(sz) if self.can_hang((x, y, z))]

    # -- writing -------------------------------------------------------------------------------------------------
    def state_index(self, name, props):
        for i, entry in enumerate(self.palette):
            if entry['Name'][1] != name:
                continue
            have = {k: v[1] for k, v in entry.get('Properties', (0, {}))[1].items()}
            if have == props:
                return i
        self.palette.append({'Name': nbt_io.string(name),
                             'Properties': nbt_io.compound(**{k: nbt_io.string(v) for k, v in props.items()})})
        return len(self.palette) - 1

    def place(self, positions):
        state = self.state_index(LANTERN, HANGING)
        for pos in positions:
            index = self.cells.get(pos)
            if index is None:
                self.blocks.append({'pos': (nbt_io.LIST, (nbt_io.INT, list(pos))), 'state': (nbt_io.INT, state)})
                self.cells[pos] = len(self.blocks) - 1
            else:
                self.blocks[index]['state'] = (nbt_io.INT, state)

    def save(self):
        nbt_io.save(self.path, self.root)


def plan(site, symmetries):
    """Greedy cover: hang the lantern set that lights the most still-dark cells per lantern, until none are dark.

    Light from separate lanterns only ever adds, so each candidate's reach is worked out once on its own. A
    candidate is tried with its mirror images (the first symmetry whose images are all valid) so the layout stays
    as symmetric as the building.
    """
    targets = set(site.dark())
    candidates = set(site.candidates())
    reach = {}
    for pos in candidates:
        lit = site.light([pos])
        reach[pos] = {cell for cell in targets if lit.get(cell, 0) > 0}
    groups = []
    for pos in sorted(candidates):
        for symmetry in symmetries:
            group = tuple(sorted({image(pos) for image in symmetry} | {pos}))
            if set(group) <= candidates:
                groups.append(group)
                break
        else:
            groups.append((pos,))
    chosen, dark = [], set(targets)
    while dark:
        best, best_score = None, 0.0
        for group in groups:
            if any(pos in chosen for pos in group):
                continue
            lit = set().union(*(reach[pos] for pos in group)) & dark
            score = len(lit) / len(group)
            if score > best_score:
                best, best_score = group, score
        if best is None:
            break
        chosen += best
        dark -= set().union(*(reach[pos] for pos in best))
    return chosen, dark


def pavilion():
    # The carousel turns in the ring's centre; keep its sweep clear.
    def carousel(x, y, z):
        return (x - 32) ** 2 + (z - 32) ** 2 < 8 * 8 and y <= 7
    return Site('crimson_troupe_pavilion', keep_clear=carousel)


def camp(variant):
    protect = {(3, 1, 4), (4, 1, 4)}
    for left in (1, 15):
        protect.update({(left + 4, 5, 5), (left, 1, 5), (left + 4, 5, 3)})
        protect.update({(x, y, 9) for x in range(left + 3, left + 6) for y in range(1, 4)})
    return Site('troupe_camp_' + variant, protect=protect)


def main():
    check = '--check' in sys.argv
    flip_x = lambda p: (64 - p[0], p[1], p[2])  # noqa: E731
    flip_z = lambda p: (p[0], p[1], 64 - p[2])  # noqa: E731
    flip_xz = lambda p: (64 - p[0], p[1], 64 - p[2])  # noqa: E731
    sites = [(pavilion(), [(flip_x, flip_z, flip_xz), (flip_x,)])]
    camp_flip = lambda p: (24 - p[0], p[1], p[2])  # noqa: E731
    sites += [(camp(v), [(camp_flip,)]) for v in ('air', 'blades', 'pageantry', 'embers', 'memory')]
    for site, mirrors in sites:
        before = site.dark()
        if check or not before:
            print(f'{site.name}: {len(before)} dark spawnable interior cells of {len(site.interior())}')
            continue
        chosen, left = plan(site, mirrors)
        assert not left, (site.name, sorted(left)[:10])
        BACKUP.mkdir(parents=True, exist_ok=True)
        backup = BACKUP / site.path.name
        if not backup.exists():
            shutil.copy2(site.path, backup)
        site.place(chosen)
        site.save()
        after = Site(site.name).dark()
        print(f'{site.name}: dark spawnable interior cells {len(before)} -> {len(after)}; '
              f'hung {len(chosen)} Hematic Lanterns at {chosen}')


if __name__ == '__main__':
    main()
