"""The Chapel trials across a wing (Track 3): where each kind's marks stand in its trial hall, the set-piece block that
stands at each mark (Track 6), and the focus block entity's template NBT.

A wing is approach -> <kind>_hall -> <kind>_focus. The hall and the focus room are fixed per kind, so a mark's offset
from the focus is known when the templates are written: the hall's east connector meets the focus room's west one, and
both are authored running along +x (same rotation once placed). The focus block is authored facing north; its block
entity turns every offset by the rotation that took north to its placed facing.

Mark kinds (ChapelFocusBlockEntity.MarkKind): WITNESS (Beholding: the block the memory touches and the player walks
back to), LITANY_STONE (Litany: read on approach), OFFERING_STAND (Litany: holds candidate Index), START (Downfall: the
gauntlet's gate), SPAWN (Downfall: a wave rises here; Index is the wave), CIRCLE (Repose: centre of the still ring),
PYLON (Repose: a pulse fires from here).

The focus drives a set piece only when the block at the mark is that piece (Track 6, R6), so each builder below puts
the piece exactly on its mark.
"""
from nbtlib import Compound, List, String, Int

LITANY_ITEMS = ['minecraft:bone', 'minecraft:rotten_flesh', 'minecraft:spider_eye', 'minecraft:ink_sac',
                'minecraft:glowstone_dust', 'minecraft:redstone']
LITANY_STOCK = 4
REPOSE_RADIUS = 3.5


class Mark:
    def __init__(self, x, y, z, kind, index=0):
        self.pos = (x, y, z)
        self.kind = kind
        self.index = index


def witness(room, c, mark, facing):
    """A witness stone on an inlay: a carved stele whose eye lights when the memory touches it. The mark is the stele
    (player height); it faces the walk so its eye is seen from the hall's centre line."""
    x, y, z = mark.pos
    room.set(x, y - 1, z, c['inlay'])
    room.set(x, y, z, 'hemomancy:witness_stone', {'facing': facing, 'lit': 'false'})


def litany_stone(room, c, mark, facing):
    """The litany stone: a two-tall inscribed slab, its lower half on the mark, its inscribed face toward `facing`."""
    x, y, z = mark.pos
    room.set(x, y, z, 'hemomancy:litany_stone', {'facing': facing, 'half': 'lower'})
    room.set(x, y + 1, z, 'hemomancy:litany_stone', {'facing': facing, 'half': 'upper'})


def offering_stand(room, c, mark, style='stand'):
    """An offering stand on the mark holding a stack of its candidate, so a challenger finds all six on the way and the
    puzzle is which three, in what order. Its block entity keeps the stack (Item). `style` is its cut: stand (a dish on
    a pedestal), or bowl (Velorum's frozen bowl, standing on a table top)."""
    x, y, z = mark.pos
    room.set(x, y, z, 'hemomancy:offering_stand', {'style': style}, Compound({
        'id': String('hemomancy:offering_stand'),
        'Item': Compound({'id': String(LITANY_ITEMS[mark.index]), 'count': Int(LITANY_STOCK)})}))


def spawn_seal(room, c, mark):
    """A spawn mark: the cracked seal a wave rises from, a floor plate in the air cell over a cracked flag ringed with an
    inlay cross. Its notches are the wave it answers (Index)."""
    x, y, z = mark.pos
    room.set(x, y - 1, z, c['floor_alt'])
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        room.set(x + dx, y - 1, z + dz, c['inlay'])
    room.set(x, y, z, 'hemomancy:spawn_mark', {'wave': str(mark.index), 'due': 'false'})


def pylon(room, c, mark, facing, style='venous'):
    """A pulse pylon three tall standing on the floor; the mark is its upper part, which holds the block entity. Its
    face turns toward the ring along z. `style` is its cut: venous, or anchor (Seraphae's Still Ward)."""
    x, y, z = mark.pos
    for dy, part in ((-2, 'lower'), (-1, 'middle'), (0, 'upper')):
        room.set(x, y + dy, z, 'hemomancy:pulse_pylon', {'facing': facing, 'part': part, 'charge': '0',
                                                         'style': style})


def focus_nbt(saint_type, kind, slot, marks):
    return Compound({
        'id': String('hemomancy:chapel_focus'), 'Kind': String(kind), 'Slot': Int(slot), 'Saint': String(saint_type),
        'Marks': List[Compound]([Compound({'x': Int(dx), 'y': Int(dy), 'z': Int(dz), 'Kind': String(m.kind),
                                           'Index': Int(m.index)}) for (dx, dy, dz), m in marks])})
