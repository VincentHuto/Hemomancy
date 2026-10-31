"""Acceptance checks every Court template passes before build.py writes it (CourtSeatTemplateTest repeats the
forbidden-block and dry-pocket rules on the saved NBT)."""
from .kit import AIR, FORBIDDEN, WATER


def forbidden(room):
    out = []
    for pos, (name, props, nbt) in room.blocks.items():
        path = name.split(':', 1)[1]
        if any(word in path for word in FORBIDDEN):
            out.append('forbidden block %s at %s' % (name, pos))
    return out


def wet_state(room, x, y, z, open_top):
    """What the cell at (x, y, z) holds as far as a neighbouring air cell is concerned: True if it can pour water."""
    sx, sy, sz = room.size
    if not (0 <= x < sx and 0 <= z < sz and y >= 0):
        return y <= room.waterline
    if y >= sy:
        return not open_top and y <= room.waterline
    entry = room.blocks.get((x, y, z))
    if entry is None:
        return y <= room.waterline              # structure void: the world's sea below the waterline
    name, props, _ = entry
    if name == WATER:
        return True
    if name == 'minecraft:jigsaw':
        return False
    return dict(props).get('waterlogged') == 'true'


def dry_pockets(room, open_top=False):
    """No air cell under the waterline may have water beside it or above it."""
    out = []
    for (x, y, z), (name, _, _) in room.blocks.items():
        if name != AIR or y > room.waterline:
            continue
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0)):
            if wet_state(room, x + dx, y + dy, z + dz, open_top):
                out.append('air at %s touches water at %s' % ((x, y, z), (x + dx, y + dy, z + dz)))
                break
    return out


def all_problems(name, room, open_top=False):
    return ['%s: %s' % (name, p) for p in forbidden(room) + dry_pockets(room, open_top)]
