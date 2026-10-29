"""Author the Saint Sarcophagus: an entombed Saint's chest tomb on a stepped venous-stone plinth.

The tomb is the size of a Saint lying down: two blocks wide and four long, its chest a block and a half wide and
three long. The chest is polished venous stone, strapped at its corners in hematic iron and cut along both flanks
into four arcaded panels carved with vein-trees. Its head end bears a witnessing eye, its foot end an inscribed bone
plaque with the slot the offering gutter drains into. Inside, the preserved corpus lies at a man's length in a
stained linen shroud on a crimson pall, a crimson stole down its chest and its desiccated hands clasping a sealed
vial; erythrocytic fruiting bodies have crowned its head and broken through its side. Its grave goods lie at its
feet: a blood-wood casket, a folded vestment and a bone rosary. The lid is a heavy slab bound by three iron straps
and bearing the Saint's gisant, cast in hematic iron: a robed, veiled figure with a bone mask, hands folded over a
lacquered heart, a bone skull at its feet. In front stands the offering bowl, an iron basin on a bone stem, and a
cresset on a tall iron stand burns at each corner of the plinth once the tomb has been consecrated.

Emits the tomb, the lid, the bowl's blood, the cresset coals and the corpus's full-bright memory veins as block
models, the closed-tomb item model (half scale), the blockstate, the painted atlas, an editable Blockbench project
and SarcophagusGeometry.java (filler cells, collision cells, render anchors and the list of Saint palettes). Python 3
+ Pillow + NumPy.

Each Saint with colours in PALETTES also gets saint_sarcophagus_<name>.png, painted in the default atlas's exact
layout (the run asserts it), and is listed in SarcophagusGeometry.SAINT_PALETTES. In game, SarcophagusPalettes moves
every quad of that Saint's tomb, the baked body and the renderer's moving parts alike, from the default atlas onto its
own, so a palette needs no model files of its own. `--preview` writes closed and open turnarounds and close-ups to
build/model-previews/ for the default and every palette (saint_sarcophagus_<name>*.png). `--saint <name>` repaints
only that Saint's atlas (and its previews with --preview) while its colours are being tuned; a plain run repaints
everything, palettes included.

Coordinates are block pixels for the north-facing state. The tomb is centred on the main block's x (x -8..24,
straddling three cell columns), its foot end with the bowl faces the player at z = -16 and its head end lies at
z = 48, two cells behind the main block. Block models cannot reach past -16..32, so every part is cut at z = 32
after the atlas is painted: `<part>` keeps the foot three cells and `<part>_head` holds the last cell shifted back
by 32 (SarcophagusBakedModel adds the static head section to the baked tomb; the renderer draws the moving ones).
"""
import argparse
import contextlib
import copy
import json
import math
import sys

from clairaudiograph import clamp, noise
from export_scriptorium_upgrades import ROOT
from fixture_atlas import ASSETS, PALETTE, box, build, mirror_x, model_json, paint, preview, separate, write

ATLAS = 'saint_sarcophagus'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}
GEOMETRY = ROOT / 'src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/puzzle/SarcophagusGeometry.java'

# The cells the tomb stands in (north frame): three columns, two storeys, four long; the main block is (0, 0, 0).
CELLS = [(x, y, z) for x in (-1, 0, 1) for y in (0, 1) for z in (-1, 0, 1, 2)]
HEAD_CUT = 32               # every part is cut here; the head section is exported shifted back by this much
CENTRE = (8.0, 16.0)        # (x, z) of the tomb's centre on the floor: where its Saint rises

# Renderer anchors (block pixels, north-facing). Written into SarcophagusGeometry.java.
LID_PIVOT = (19.75, 20.5)   # (x, y) of the cornice edge the lid tips over once slid aside
LID_SLIDE = 18.0            # how far the lid slides east before it tips
LID_TIP = 20.0              # degrees the lid leans down past the rim
POOL = (4.5, 10.0, -14.5, 11.5, 11.75, -10.5)   # bowl blood at full fill: x0, y0, z0, x1, y1, z1
CRESSETS = [(x, 29.25, z) for x in (-5.25, 21.25) for z in (-13.5, 45.0)]

TOMB = (62, 10, 16)
TOMB_LIGHT = (92, 18, 26)
BRICK = (96, 10, 14)
MORTAR = (46, 6, 10)
EFFIGY = (86, 80, 92)
BONE_PALE = (226, 212, 184)
LINEN = (190, 176, 150)
SKIN = (132, 106, 90)
CAP = (176, 28, 36)
VEIN_CORE = (236, 52, 64)
VEIN_HOT = (255, 170, 150)
PALL = (112, 16, 30)
BLOT_DEEP = (122, 36, 34)   # old blood soaked through the linen
BLOT_PALE = (164, 104, 84)
EFFIGY_SEAM = (112, 28, 38) # blood in the gisant's iron grain
STAIN = (70, 14, 20)        # iron gone black with offerings
TALLOW = (214, 200, 170)
# The chest's corner and lid straps. Left empty they are hematic iron like the bowl; a Saint that gives them a colour
# has them in bone instead, jointed like a finger with a darker STRAP_JOINT knuckle every few texels.
STRAP = ()
STRAP_JOINT = ()
# Left empty the chest is polished stone veined in TOMB_LIGHT. A Saint that sets CHAR_CRACK has it burned black instead:
# alligator char, scales of TOMB split by seams of CHAR_CRACK.
CHAR_CRACK = ()
# Left empty the carvings are bare. A Saint that sets MOSS has moss packed into them: the flank panels' carved vein-trees
# and the foot of their fields, the plaque's inscription and some of the dais's mortar joints, MOSS_DEEP in the shade.
MOSS = ()
MOSS_DEEP = ()
# Left empty the stone is bare. A Saint that sets RIME has the chest and lid crusted with it: hoar frost over most of
# every upward face and hanging in a ragged fringe from the top of the sides, RIME_SHADE between its crystals.
RIME = ()
RIME_SHADE = ()
# Like MOSS, but frost packed into the carvings instead (FROST_DEEP in the shade). A Saint sets one or the other.
FROST = ()
FROST_DEEP = ()

# Per-Saint overrides of the colours above, by name; 'PALETTE' overrides the shared fixture_atlas materials (iron,
# ferric, bone, lacquer, blood, wood, cavity). Every Saint with overrides gets saint_sarcophagus_<name>.png, painted in
# the default atlas's exact layout, and is listed in SarcophagusGeometry.SAINT_PALETTES; the game moves that Saint's
# tomb, baked and moving parts alike, onto its atlas (SarcophagusPalettes). A theme track adds its Saint's colours
# here and re-runs the script: nothing else is needed. A Saint left empty keeps the default tomb.
PALETTES = {
    # Hemorath: the pit the Hematic Order bled him dry in. Venous stone gone deep and dry, hematic iron heavy and
    # blue-black with dark crimson seams, a pall and stole of dried oxblood lacquer, aged bone, a corpus drawn to
    # leather and its memory veins a hungry hot red.
    'hemorath': {
        'TOMB': (36, 8, 18), 'TOMB_LIGHT': (64, 20, 36), 'BRICK': (56, 10, 22), 'MORTAR': (22, 5, 10),
        'EFFIGY': (50, 52, 64), 'EFFIGY_SEAM': (88, 12, 20), 'BONE_PALE': (190, 172, 138),
        'LINEN': (136, 116, 90), 'SKIN': (98, 74, 58), 'CAP': (128, 20, 26), 'PALL': (66, 20, 20),
        'BLOT_DEEP': (70, 18, 18), 'BLOT_PALE': (106, 62, 48), 'STAIN': (36, 10, 14), 'TALLOW': (178, 162, 132),
        'VEIN_CORE': (255, 34, 22), 'VEIN_HOT': (255, 132, 54),
        'PALETTE': {'iron': (34, 36, 46), 'ferric': (76, 80, 96), 'bone': (176, 158, 124),
                    'lacquer': (90, 20, 24), 'lacquer_deep': (58, 12, 16), 'blood': (104, 14, 20),
                    'wood': (56, 26, 28), 'cavity': (22, 9, 12)},
    },
    # Seraphae: the healer her people chained because they could not kill her. Her chest is polished quartz, warm ivory
    # with faint grey veins, on a calcite dais; its straps are aged bone jointed like fingers, not metal. The gisant
    # stays hematic iron, the pall and stole pale lacquer, the corpus one that would not stop mending, and her memory
    # veins burn rose to white.
    'seraphae': {
        'TOMB': (226, 219, 204), 'TOMB_LIGHT': (176, 172, 168), 'BRICK': (198, 196, 190), 'MORTAR': (138, 136, 134),
        'EFFIGY': (74, 72, 84), 'EFFIGY_SEAM': (124, 36, 46), 'BONE_PALE': (238, 228, 204),
        'LINEN': (222, 214, 196), 'SKIN': (194, 166, 148), 'CAP': (192, 66, 76), 'PALL': (164, 118, 116),
        'BLOT_DEEP': (168, 92, 92), 'BLOT_PALE': (204, 162, 146), 'STAIN': (58, 36, 40), 'TALLOW': (232, 224, 206),
        'VEIN_CORE': (255, 116, 110), 'VEIN_HOT': (255, 238, 206),
        'STRAP': (190, 166, 124), 'STRAP_JOINT': (106, 84, 60),
        'PALETTE': {'iron': (60, 58, 70), 'ferric': (118, 112, 126), 'bone': (214, 198, 164),
                    'lacquer': (218, 192, 180), 'lacquer_deep': (178, 150, 142), 'blood': (160, 40, 50),
                    'wood': (132, 104, 86), 'cavity': (64, 52, 52)},
    },
    # Putriciel: the complex burned and rebuilt in cycles until they buried it. His chest is stone burned black and
    # cracked into alligator char, dull and dead with no ember left in it, the cracks gone oxblood at the bottom; moss
    # has filled the carvings. The straps are sooty bone jointed like fingers, the gisant blackened iron with crimson in
    # its seams, the pall scorched oxblood, the linen ash-grey, and his memory veins burn the Flammeus crimson to pink.
    'putriciel': {
        'TOMB': (38, 33, 33), 'TOMB_LIGHT': (62, 54, 52), 'BRICK': (64, 50, 44), 'MORTAR': (30, 24, 22),
        'EFFIGY': (46, 44, 48), 'EFFIGY_SEAM': (104, 22, 32), 'BONE_PALE': (186, 172, 146),
        'LINEN': (128, 120, 108), 'SKIN': (90, 72, 62), 'CAP': (150, 34, 46), 'PALL': (62, 22, 24),
        'BLOT_DEEP': (66, 22, 22), 'BLOT_PALE': (102, 72, 62), 'STAIN': (30, 18, 18), 'TALLOW': (172, 162, 140),
        'VEIN_CORE': (255, 54, 92), 'VEIN_HOT': (255, 184, 206),
        'STRAP': (178, 162, 134), 'STRAP_JOINT': (66, 52, 44),
        'CHAR_CRACK': (24, 10, 12), 'MOSS': (78, 104, 40), 'MOSS_DEEP': (42, 62, 26),
        'PALETTE': {'iron': (40, 38, 42), 'ferric': (90, 84, 88), 'bone': (190, 174, 146),
                    'lacquer': (72, 22, 28), 'lacquer_deep': (36, 24, 24), 'blood': (122, 20, 32),
                    'wood': (42, 32, 30), 'cavity': (16, 12, 12)},
    },
    # Velorum: his own monastery, frozen round him when he stopped. His chest is deepslate gone black-blue under a crust
    # of rime, frost packed into every carving, its stone veined in cold blue; the fittings stay hematic iron, rimed
    # pale. The pall and stole are the monastery's black lacquer, the linen frozen grey, the corpus frost-bitten and
    # blue, the caps frozen dark, and his memory veins burn blue to white.
    'velorum': {
        'TOMB': (34, 36, 46), 'TOMB_LIGHT': (84, 104, 140), 'BRICK': (52, 54, 66), 'MORTAR': (24, 25, 32),
        'EFFIGY': (60, 66, 82), 'EFFIGY_SEAM': (132, 168, 214), 'BONE_PALE': (214, 220, 228),
        'LINEN': (172, 180, 194), 'SKIN': (126, 132, 150), 'CAP': (112, 42, 60), 'PALL': (34, 36, 54),
        'BLOT_DEEP': (66, 40, 56), 'BLOT_PALE': (138, 148, 172), 'STAIN': (30, 34, 44), 'TALLOW': (218, 226, 236),
        'VEIN_CORE': (140, 196, 255), 'VEIN_HOT': (236, 248, 255),
        'RIME': (222, 234, 248), 'RIME_SHADE': (170, 190, 220), 'FROST': (168, 194, 230), 'FROST_DEEP': (96, 120, 164),
        'PALETTE': {'iron': (42, 46, 58), 'ferric': (104, 116, 136), 'bone': (198, 204, 212),
                    'lacquer': (40, 38, 50), 'lacquer_deep': (24, 24, 32), 'blood': (100, 28, 44),
                    'wood': (40, 38, 46), 'cavity': (16, 18, 24)},
    },
}


def painted():
    """The Saints that have a palette, in PALETTES order."""
    return [name for name, colours in PALETTES.items() if colours]


@contextlib.contextmanager
def palette(name):
    """Paint in `name`'s colours for the duration, then put the default ones back."""
    colours = dict(PALETTES[name])
    shared = colours.pop('PALETTE', {})
    unknown = [k for k in colours if not isinstance(globals().get(k), tuple)] + [k for k in shared if k not in PALETTE]
    assert not unknown, f'{name} overrides colours the tomb does not paint: {unknown}'
    saved, saved_shared = {k: globals()[k] for k in colours}, dict(PALETTE)
    globals().update(colours)
    PALETTE.update(shared)
    try:
        yield
    finally:
        globals().update(saved)
        PALETTE.clear()
        PALETTE.update(saved_shared)


def pair(el):
    """The box and its mirror across the tomb's centre line (x = 8)."""
    return [el, mirror_x(el)]


def dais():
    e = [box('dais_base', (-7.5, 0, -15.5), (23.5, 2, 47.5), 'brick'),
         box('dais_step', (-5.5, 2, -8.5), (21.5, 4, 42.5), 'brick', skip=('down',))]
    # Tallow candles either side of the bowl, wax pooled at their feet.
    for x, z, h in ((-1.5, -15, 5), (.5, -12.75, 3), (-2.75, -11.5, 3.75)):
        e += pair(box('candle', (x, 2, z), (x + 1.5, 2 + h, z + 1.5), 'tallow', skip=('down',)))
        e += pair(box('wick', (x + .5, 2 + h, z + .5), (x + 1, 2.75 + h, z + 1), 'hand', skip=('down',)))
    e += pair(box('wax_pool', (-3.25, 2, -15.25), (2.5, 2.25, -10), 'tallow', skip=('down',)))
    return e


def chest():
    e = [box('chest_plinth', (-4, 4, -7), (20, 6, 41), 'tomb', skip=('down',))]
    # Four arcaded panels down each flank between pilasters, blackened stone within.
    for z0, z1 in ((-4.5, 4.5), (6.5, 15.5), (17.5, 26.5), (28.5, 37.5)):
        e += pair(box('flank_panel', (-3.25, 6, z0), (-1.75, 19, z1), 'tomb', west='frieze', east='cavity',
                      skip=('up', 'down')))
    for z0 in (4.5, 15.5, 26.5):
        e += pair(box('pilaster', (-3.75, 6, z0), (-1.75, 19, z0 + 2), 'tomb', skip=('up', 'down')))
    # Straps over the four corners: hematic iron, or bone in a palette that sets STRAP.
    for z0, z1 in ((-7.25, -4.5), (37.5, 41.25)):
        e += pair(box('corner_strap', (-4.25, 4, z0), (-1.75, 21, z1), 'strap', skip=('down',)))
    e.append(box('foot_wall', (-1.75, 6, -5.5), (17.75, 19, -3.5), 'tomb', north='plaque', south='cavity',
                 skip=('up', 'down')))
    e.append(box('head_wall', (-1.75, 6, 37.5), (17.75, 19, 39.5), 'tomb', south='sigil', north='cavity',
                 skip=('up', 'down')))
    # The cornice the lid rests on: stone banded with bone dentils.
    e.append(box('cornice_foot', (-1.75, 19, -6), (17.75, 20.5, -3.5), 'cornice', skip=('down',)))
    e.append(box('cornice_head', (-1.75, 19, 37.5), (17.75, 20.5, 40), 'cornice', skip=('down',)))
    e += pair(box('cornice_side', (-3.75, 19, -4.5), (-1.75, 20.5, 37.5), 'cornice', skip=('down',)))
    return e


def corpus():
    """The preserved Saint at a man's length (crown to toes 30 px) on its pall, with its grave goods at its feet.

    Returns every part and the bare flesh and vial that veins() inflates into the full-bright overlay."""
    e = [box('pall', (-1.75, 6, -3.5), (17.75, 8, 37.5), 'pall', skip=('down', 'north', 'south', 'east', 'west')),
         box('pillow', (3.5, 8, 30), (12.5, 9.5, 37), 'linen', skip=('down',)),
         box('shroud_shins', (4, 8, 7.5), (12, 11.5, 12), 'linen', skip=('down',)),
         box('shroud_thighs', (3, 8, 12), (13, 12.5, 17), 'linen', skip=('down',)),
         box('shroud_torso', (2, 8, 17), (14, 14, 24.5), 'linen', skip=('down',)),
         box('shroud_shoulders', (1, 8, 24.5), (15, 13.5, 27.5), 'linen', skip=('down',))]
    e += pair(box('shroud_foot', (4.5, 11.5, 6), (7.25, 13.75, 7.5), 'linen', skip=('down',)))
    # Arms bound at the Saint's sides, forearms folded across the chest.
    e += pair(box('shroud_arm', (.25, 8, 17.5), (2.25, 11.5, 26.5), 'linen', skip=('down',)))
    e += pair(box('shroud_forearm', (1.5, 12, 18.5), (5.5, 14.5, 21), 'linen', skip=('down',)))
    e.append(box('stole_chest', (6.5, 14, 21), (9.5, 14.25, 24.5), 'stole', skip=('down',)))
    e.append(box('stole_neck', (4, 13.5, 24.5), (12, 13.75, 27.5), 'stole', skip=('down',)))
    e += pair(box('stole_fall', (5.25, 12.5, 12), (6.5, 12.75, 17), 'stole', skip=('down',)))
    e += pair(box('stole_lap', (5.25, 14, 17), (6.5, 14.25, 21), 'stole', skip=('down',)))
    flesh = [box('neck', (6, 9, 27.5), (10, 12.5, 28.5), 'skin', skip=('down',)),
             box('head', (4, 9, 28.5), (12, 15, 36), 'skin', up='face'),
             box('hand_left', (5, 14.25, 18.5), (8, 15.75, 21.5), 'skin', up='knuckles'),
             box('hand_right', (8, 14.25, 18), (11, 15.75, 21), 'skin', up='knuckles')]
    vial = [box('vial', (7, 14.25, 15), (9, 16.25, 18.5), 'vial'),
            box('vial_stopper', (7.25, 14.5, 14.25), (8.75, 16, 15), 'bone')]
    # Erythrocytic fruiting bodies crowning the head and breaking out through the shroud.
    caps = []
    for name, x, z, y, h, r in (('crown', 2.75, 33, 9.5, 3.5, 1.25), ('crown', 13.25, 32.5, 9.5, 3, 1.0),
                                ('crown', 4.5, 35.75, 9.5, 4.5, 1.25), ('crown', 11.25, 35.75, 9.5, 3.5, 1.0),
                                ('crown', 8, 36.25, 9.5, 5.5, 1.25), ('bloom', 14.5, 20.5, 8, 5, 1.25),
                                ('bloom', 15, 18, 8, 3.5, 1.0), ('bloom', 13.5, 23, 13.5, 2.5, .75)):
        caps.append(box(f'{name}_stipe', (x - .5, y, z - .5), (x + .5, y + h, z + .5), 'stipe', skip=('down',)))
        caps.append(box(f'{name}_cap', (x - r, y + h, z - r), (x + r, y + h + 1, z + r), 'cap'))
    return e + flesh + vial + caps + grave_goods(), flesh + vial


def grave_goods():
    """At the Saint's feet: a blood-wood casket bound in iron, a folded vestment and a bone rosary."""
    e = [box('casket', (-.5, 8, -2.75), (5, 11.5, 2.5), 'wood', skip=('down',)),
         box('casket_lid', (-.75, 11.5, -3), (5.25, 12.25, 2.75), 'wood', skip=('down',)),
         box('casket_clasp', (1.75, 9.25, -3), (2.75, 10.75, -2.75), 'bone', skip=('south',)),
         box('vestment', (9.5, 8, -2.75), (16, 9.5, 3), 'stole', skip=('down',)),
         box('vestment_fold', (10, 9.5, -2.25), (15.5, 10.5, 2.5), 'pall', skip=('down',))]
    for z0 in (-2, 1):
        e.append(box('casket_band', (-1, 9, z0), (5.5, 12.5, z0 + 1), 'iron', skip=('down',)))
    for i, (x, z) in enumerate(((6, -1.5), (7, -.75), (7.5, .5), (7.25, 1.75), (6.5, 2.75), (5.75, 3.75))):
        e.append(box('rosary_bead', (x, 8, z), (x + 1, 9, z + 1), 'bone', skip=('down',)))
    e.append(box('rosary_pendant', (5.25, 8, 4.75), (6.75, 9.25, 6), 'bone', skip=('down',)))
    return e


def bowl():
    e = [box('bowl_foot', (4.5, 2, -14.5), (11.5, 3, -10.5), 'tomb', skip=('down',)),
         box('bowl_knuckle', (6, 3, -13.5), (10, 4, -11.5), 'bone'),
         box('bowl_stem', (7, 4, -13), (9, 8, -12), 'bone', skip=('up', 'down')),
         box('bowl_knuckle', (6, 8, -13.5), (10, 9, -11.5), 'bone'),
         box('bowl_floor', (4.5, 9, -14.5), (11.5, 10, -10.5), 'iron', up='stain')]
    e.append(box('bowl_wall', (3.5, 9, -15.5), (12.5, 12.5, -14.5), 'iron', south='stain'))
    e.append(box('bowl_wall', (3.5, 9, -10.5), (12.5, 12.5, -9.5), 'iron', north='stain'))
    e += pair(box('bowl_wall', (3.5, 9, -14.5), (4.5, 12.5, -10.5), 'iron', east='stain'))
    e.append(box('bowl_rim', (3.25, 12.5, -15.75), (12.75, 13.25, -14.5), 'bone', skip=('down',)))
    e.append(box('bowl_rim', (3.25, 12.5, -10.5), (12.75, 13.25, -9.25), 'bone', skip=('down',)))
    e += pair(box('bowl_rim', (3.25, 12.5, -14.5), (4.5, 13.25, -10.5), 'bone', skip=('down',)))
    # The gutter that carries the offering into the slot under the foot plaque.
    e.append(box('gutter', (7, 9.75, -9.25), (9, 10.5, -5.5), 'iron', up='stain'))
    e += pair(box('gutter_lip', (6.5, 9.75, -9.25), (7, 11.25, -5.5), 'iron'))
    return e


def cressets():
    e, coals = [], []
    for x, _, z in CRESSETS:
        e.append(box('cresset_foot', (x - 1.25, 2, z - 1.25), (x + 1.25, 3, z + 1.25), 'iron', skip=('down',)))
        e.append(box('cresset_stand', (x - .5, 3, z - .5), (x + .5, 27, z + .5), 'iron', skip=('up', 'down')))
        for y in (12, 21):
            e.append(box('cresset_collar', (x - .75, y, z - .75), (x + .75, y + 1, z + .75), 'bone'))
        e.append(box('cresset_floor', (x - 1.75, 27, z - 1.75), (x + 1.75, 27.75, z + 1.75), 'iron'))
        for dz in (-1.75, 1.25):
            e.append(box('cresset_bar', (x - 1.75, 27.75, z + dz), (x + 1.75, 29.5, z + dz + .5), 'iron',
                         skip=('down',)))
        for dx in (-1.75, 1.25):
            e.append(box('cresset_bar', (x + dx, 27.75, z - 1.25), (x + dx + .5, 29.5, z + 1.25), 'iron',
                         skip=('down',)))
        e.append(box('cresset_ash', (x - 1.25, 27.75, z - 1.25), (x + 1.25, 28.75, z + 1.25), 'ash', skip=('down',)))
        coals.append(box('cresset_coals', (x - 1, 28.75, z - 1), (x + 1, 29.25, z + 1), 'coal', skip=('down',),
                         extra=BRIGHT))
    return e, coals


def lid():
    e = [box('lid_slab', (-4.5, 20.5, -7.5), (20.5, 23, 41.5), 'tomb', down='cavity'),
         box('lid_bevel', (-3.5, 23, -6.5), (19.5, 24, 40.5), 'tomb', skip=('down',))]
    # Three straps (iron unless the Saint's palette makes them bone) bind the slab, clasped on either flank.
    for z0 in (2, 17, 32):
        e.append(box('lid_strap', (-5, 20.5, z0), (21, 23.5, z0 + 2), 'strap', skip=('down',)))
        e += pair(box('strap_clasp', (-5.25, 20.75, z0 + .25), (-5, 22.75, z0 + 1.75), 'ferric', skip=('east',)))
    # The gisant: a robed and veiled Saint with a bone mask, hands folded over a lacquered heart.
    e.append(box('effigy_cushion', (3, 24, 33.5), (13, 25.5, 40), 'effigy', up='tassel_cushion', skip=('down',)))
    e.append(box('effigy_head', (4, 24, 30), (12, 30.5, 37), 'effigy', up='mask', skip=('down',)))
    e.append(box('effigy_hood', (3.5, 24, 37), (12.5, 31, 38.5), 'effigy', skip=('down',)))
    e.append(box('effigy_shoulders', (1.5, 24, 26), (14.5, 27.5, 30), 'robe', skip=('down',)))
    e.append(box('effigy_chest', (2.5, 24, 15), (13.5, 28, 26), 'robe', skip=('down',)))
    e.append(box('effigy_skirt', (3.5, 24, 1), (12.5, 26.5, 15), 'robe', skip=('down',)))
    e += pair(box('effigy_arm', (.75, 24, 17), (2.5, 27, 27), 'effigy', skip=('down',)))
    e += pair(box('effigy_forearm', (2, 27.5, 18), (6, 29, 20.5), 'effigy', skip=('down',)))
    e += pair(box('effigy_foot', (4.5, 26.5, 1), (7, 29, 3), 'effigy', skip=('down',)))
    e.append(box('effigy_hands', (5.5, 28, 17.5), (10.5, 29.5, 21), 'effigy', up='knuckles_iron'))
    e.append(box('effigy_heart', (6.5, 28, 14), (9.5, 30.5, 17.5), 'lacquer', up='heart'))
    e.append(box('effigy_stole', (6.5, 28, 21), (9.5, 28.25, 26), 'stole', skip=('down',)))
    e.append(box('effigy_stole_yoke', (4.5, 27.5, 26), (11.5, 27.75, 29.5), 'stole', skip=('down',)))
    e += pair(box('effigy_stole_fall', (5.25, 26.5, 3), (6.5, 26.75, 15), 'stole', skip=('down',)))
    e.append(box('effigy_skull', (5.5, 24, -5.5), (10.5, 29, -1.5), 'bone', north='skull', skip=('down',)))
    return e


def veins(parts):
    grow = .075
    return [box(el['name'] + '_vein', [a - grow for a in el['lo']], [b + grow for b in el['hi']], 'veins',
                skip=el['skip'], extra=BRIGHT, density=el['density']) for el in parts]


def pool():
    x0, y0, z0, x1, y1, z1 = POOL
    return [box('bowl_blood', (x0, y0, z0), (x1, y1, z1), 'pool', skip=('down',))]


# Coarse collision boxes (north-facing pixels); finer detail is decorative only.
COLLISION = [
    (-7.5, 0, -15.5, 23.5, 2, 47.5),
    (-5.5, 2, -8.5, 21.5, 4, 42.5),
    (-4.5, 4, -7.5, 20.5, 24, 41.5),
    (3, 24, -5.5, 13, 31, 38.5),
    (3.5, 2, -15.5, 12.5, 13, -9.5),
] + [b for x, _, z in CRESSETS for b in ((x - .5, 2, z - .5, x + .5, 27, z + .5),
                                         (x - 1.75, 27, z - 1.75, x + 1.75, 29.5, z + 1.75))]


# --- special faces --------------------------------------------------------------------------

def tomb(face, lx, ly, w, h, key):
    """Polished venous stone: deep crimson, mottled, with paler veins wandering through it (or, with CHAR_CRACK set,
    burned to char)."""
    n = noise('tomb', key, face, lx, ly)
    if CHAR_CRACK:
        return char(face, lx, ly, key, n)
    if RIME and rimed(face, lx, ly, h, key):
        return rime(key, face, lx, ly)
    vein = abs(math.sin(lx * .7 + ly * .45 + noise('vphase', key, face) * 9) + math.sin(ly * .3 - lx * .2)) < .12
    if vein:
        return clamp(TOMB_LIGHT, int(n * 10)) + (255,)
    edge = (lx in (0, w - 1) or ly in (0, h - 1)) and w >= 3 and h >= 3
    return clamp(TOMB, int(n * 12) - 6 + (10 if ly == 0 and h > 1 and face not in ('up', 'down') else 0)
                 - (10 if edge else 0)) + (255,)


def char(face, lx, ly, key, n):
    """Stone burned black and cracked like alligator char: rows three texels deep split by dark seams, each row broken
    into scales three to five wide, staggered row to row, a dull sheen along a scale's upper edge. Dead, no ember."""
    row = ly // 3
    width = 3 + int(noise('scale', key, face, row) * 3)
    shift = int(noise('stagger', key, face, row) * width)
    col = (lx + shift) // width
    seam = ly % 3 == 2 and noise('seam', key, face, lx // 2, row) > .15
    split = (lx + shift) % width == width - 1 and noise('split', key, face, row, col) > .2
    if seam or split:
        return clamp(CHAR_CRACK, int(n * 8) - 4) + (255,)
    tone = int(noise('scale_tone', key, face, row, col) * 12) - 6
    return clamp(TOMB, tone + int(n * 8) - 4 + (8 if ly % 3 == 0 else 0)) + (255,)


def rimed(face, lx, ly, h, key):
    """Whether rime crusts this texel: most of an upward face, and a ragged fringe one to three texels deep hanging from
    the top of a side (ly = 0 is the top), with the odd crystal caught lower down."""
    if face == 'up':
        return noise('rime_up', key, lx // 2, ly // 2) < .62 or noise('rime_fleck', key, lx, ly) < .25
    if face == 'down':
        return False
    if ly < 1 + int(noise('rime_fringe', key, face, lx) * 3):
        return True
    return h > 4 and noise('rime_speck', key, face, lx, ly) < .04


def rime(key, face, lx, ly):
    """Hoar frost: white crystals with blue shade between them."""
    n = noise('rime', key, face, lx, ly)
    if n > .82:
        return (246, 250, 255, 255)
    return clamp(RIME if n > .3 else RIME_SHADE, int(n * 10) - 5) + (255,)


def packed(key, face, lx, ly):
    """Whatever the Saint has packed into the carvings: moss, or frost."""
    if MOSS:
        return moss(key, face, lx, ly)
    n = noise('frost', key, face, lx, ly)
    if n > .72:
        return clamp(FROST, 18 + int(n * 10)) + (255,)
    return clamp(FROST if n > .3 else FROST_DEEP, int(n * 10) - 5) + (255,)


def moss(key, face, lx, ly):
    """Moss packed into a carved recess: deep green in the shade, tufted lighter where it catches the light."""
    n = noise('moss', key, face, lx, ly)
    if n > .72:
        return clamp(MOSS, 16 + int(n * 10)) + (255,)
    return clamp(MOSS if n > .3 else MOSS_DEEP, int(n * 10) - 5) + (255,)


def brick(face, lx, ly, w, h, key):
    """Venous stone bricks laid in offset courses."""
    n = noise('brick', key, face, lx, ly)
    course = ly // 3 if face not in ('up', 'down') else ly // 4
    if face in ('up', 'down'):
        joint = ly % 4 == 3 or (lx + (course % 2) * 3) % 6 == 5
    else:
        joint = ly % 3 == 2 or (lx + (course % 2) * 3) % 6 == 5
    if joint:
        if (MOSS or FROST) and noise('mortar_moss', key, face, lx, ly) < .35:
            return packed(key, face, lx, ly)
        return clamp(MORTAR, int(n * 8)) + (255,)
    tone = int(noise('face', key, face, course, (lx + (course % 2) * 3) // 6) * 22) - 11
    return clamp(BRICK, tone + int(n * 8) - 4) + (255,)


def frieze(face, lx, ly, w, h, key):
    """A sunken lancet arch framed in bone with a vein-tree carved into its field (ly = 0 is the top)."""
    n = noise('frieze', key, face, lx, ly)
    cx, half, spring = (w - 1) / 2, (w - 5) / 2, min(5, h // 2)

    def opening(row):
        if row >= spring:
            return half + .01
        return -1 if row < 1 else half * ((row - .5) / (spring - .5)) ** .55

    d = abs(lx - cx)
    if ly < h - 1 and d <= opening(ly):
        trunk = d < .6 and ly >= spring - 1
        fork = (ly - spring) % 3
        branch = ly > spring and ly < h - 2 and ((fork == 1 and .6 < d <= 1.6) or (fork == 2 and 1.4 < d <= 2.6))
        twig = noise('twig', key, face, lx, ly) < .05 and d < half
        if trunk or branch or twig:
            return packed(key, face, lx, ly) if MOSS or FROST else clamp(PALETTE['blood'], 20 + int(n * 16)) + (255,)
        if (MOSS or FROST) and ly >= h - 5 and noise('creep', key, face, lx, ly) < (ly - (h - 6)) / 5:
            return packed(key, face, lx, ly)
        return clamp(PALETTE['lacquer_deep'], -18 + (h - ly) / max(h, 1) * 10 + int(n * 6)) + (255,)
    if ly == h - 1 or d <= opening(ly) + 1.01 or d <= opening(ly + 1):
        return clamp(PALETTE['bone'], int(n * 10) - (10 if ly == h - 1 else 16)) + (255,)
    return clamp(TOMB, int(n * 10) - 6) + (255,)


def plaque(face, lx, ly, w, h, key):
    """The foot end: a bone plaque of inscription over the dark slot the offering gutter drains into."""
    n = noise('plaque', key, face, lx, ly)
    if 2 <= lx < w - 2 and 1 <= ly <= h - 6:
        if lx in (2, w - 3) or ly in (1, h - 6):
            return clamp(PALETTE['bone'], int(n * 10) - 22) + (255,)
        if ly in (3, 5) and 4 <= lx < w - 4 and lx % 3 != 1 and noise('glyph', key, lx // 2, ly) > .3:
            return packed(key, face, lx, ly) if MOSS or FROST else (64, 16, 20, 255)
        return clamp(BONE_PALE, int(n * 10) - 8) + (255,)
    if abs(lx - (w - 1) / 2) < 2 and h - 4 <= ly <= h - 3:
        return (20, 4, 8, 255)
    if abs(lx - (w - 1) / 2) < 1 and ly > h - 3:
        return clamp(PALETTE['blood'], int(n * 20)) + (255,)
    return tomb(face, lx, ly, w, h, key)


def sigil(face, lx, ly, w, h, key):
    """The head end: a witnessing eye in bone on a lacquered field, weeping down the stone."""
    n = noise('sigil', key, face, lx, ly)
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(PALETTE['bone'], int(n * 10) - 18) + (255,)
    u, v = (lx + .5 - w / 2) / (w * .4), (ly + .5 - h * .42) / (h * .2)
    lens = abs(v) - (1 - u * u)
    r = math.hypot(u * w * .4, v * h * .2)
    if abs(u) <= 1 and -.4 < lens <= 0:
        return clamp(PALETTE['bone'], int(n * 10) - 8) + (255,)
    if r < 1.2:
        return (20, 4, 8, 255)
    if r < 2.4 and lens < 0:
        return clamp(PALETTE['blood'], 10 + int(n * 16)) + (255,)
    if lx == round(w / 2 - .5) and ly > h * .55 and noise('tear', key, ly) < .8:
        return clamp(PALETTE['blood'], int(n * 16)) + (255,)
    return clamp(PALETTE['lacquer_deep'], (h / 2 - ly) / max(h, 1) * 14 + int(n * 6)) + (255,)


def cornice(face, lx, ly, w, h, key):
    """Stone cornice set with a band of bone dentils along its outer faces."""
    if face == 'up':
        return tomb(face, lx, ly, w, h, key)
    if face != 'down' and ly == h - 1 and lx % 2 == 0:
        return clamp(PALETTE['bone'], int(noise('dentil', key, face, lx) * 10) - 12) + (255,)
    return clamp(TOMB_LIGHT, int(noise('cornice', key, face, lx, ly) * 10) - 6 + (12 if ly == 0 else 0)) + (255,)


def pall(face, lx, ly, w, h, key):
    """Crimson funeral pall, its hem stitched in bone."""
    n = noise('pall', key, face, lx, ly)
    if face != 'up' and ly == h - 1 and lx % 2 == 0:
        return clamp(PALETTE['bone'], -20) + (255,)
    if face == 'up' and (lx in (0, w - 1) or ly in (0, h - 1)) and (lx + ly) % 2 == 0:
        return clamp(PALETTE['bone'], -24) + (255,)
    return clamp(PALL, int(n * 10) - 5 + (5 if (lx + ly) % 2 else -5)) + (255,)


def linen(face, lx, ly, w, h, key):
    """Burial linen wound in slanting strips, yellowed, seamed in shadow and soaked through with old blood."""
    n = noise('linen', key, face, lx, ly)
    wind = int(noise('wind', key, face) * 3)
    band = (ly + (lx // 2 if face in ('up', 'east', 'west') else 0) + wind) % 3
    blot = noise('blot', key, face, lx // 2, ly // 2)
    if blot < .05:
        return clamp(BLOT_DEEP, int(n * 14) - 7) + (255,)
    if blot < .11:
        return clamp(BLOT_PALE, int(n * 12) - 6) + (255,)
    return clamp(LINEN, int(n * 10) - 5 + {0: -30, 1: 8, 2: 0}[band]) + (255,)


def skin(face, lx, ly, w, h, key):
    """Desiccated flesh drawn tight and cracked."""
    n = noise('skin', key, face, lx, ly)
    if noise('crack', key, face, lx, ly // 2) < .06:
        return clamp(SKIN, -40) + (255,)
    return clamp(SKIN, int(n * 14) - 7) + (255,)


def face_tex(face, lx, ly, w, h, key):
    """The Saint's face turned to the lid, 8 texels square like any head: sunken sockets, a fallen nose and bared
    teeth (chin at ly = 0, crown at ly = h - 1)."""
    n = noise('face', key, lx, ly)
    mid = w // 2
    if ly in (4, 5) and lx in (mid - 3, mid - 2, mid + 1, mid + 2):
        return (22, 10, 10, 255) if ly == 4 or lx in (mid - 2, mid + 1) else clamp(SKIN, -34) + (255,)
    if ly == 3 and lx in (mid - 1, mid):
        return (34, 16, 16, 255)
    if ly == 1 and mid - 2 <= lx <= mid + 1:
        return (204, 190, 160, 255) if lx % 2 == 0 else (40, 18, 18, 255)
    if ly == 2 and mid - 2 <= lx <= mid + 1:
        return clamp(SKIN, -30) + (255,)
    if lx in (0, w - 1):
        return clamp(SKIN, -18 + int(n * 8)) + (255,)
    return clamp(SKIN, int(n * 12) - 2 + (8 if ly == 6 else 0)) + (255,)


def knuckles_iron(face, lx, ly, w, h, key):
    if lx % 2 == 1 and ly < h - 1:
        return clamp(EFFIGY, -34) + (255,)
    return effigy(face, lx, ly, w, h, key)


def knuckles(face, lx, ly, w, h, key):
    if lx % 2 == 1 and ly < h - 1:
        return clamp(SKIN, -30) + (255,)
    return skin(face, lx, ly, w, h, key)


def vial(face, lx, ly, w, h, key):
    """A sealed vial of the Saint's blood, a glint down one side."""
    if face in ('up', 'down'):
        return clamp(PALETTE['blood'], -20) + (255,)
    if lx == 0 and ly < h - 1:
        return (232, 140, 140, 255)
    return clamp(PALETTE['blood'], int(noise('vial', key, face, lx, ly) * 20) - (24 if ly < 2 else 0)) + (255,)


def stole(face, lx, ly, w, h, key):
    """Crimson stole, darker along its edges, a bone thread stitched every few texels down its middle."""
    n = noise('stole', key, face, lx, ly)
    if face == 'up' and w >= 3 and lx == (w - 1) // 2 and ly % 4 == 1:
        return clamp(PALETTE['bone'], -20) + (255,)
    return clamp(PALETTE['lacquer'], int(n * 10) - 5 - (22 if face == 'up' and lx in (0, w - 1) else 0)) + (255,)


def tallow(face, lx, ly, w, h, key):
    """Bone-white tallow, a run of blood-dark drip down one side."""
    n = noise('tallow', key, face, lx, ly)
    if face in ('north', 'west') and lx == 0 and ly <= h * .6:
        return clamp(PALETTE['blood'], -10 + int(n * 12)) + (255,)
    return clamp(TALLOW, int(n * 12) - 6 + (10 if ly == 0 else 0)) + (255,)


def cap(face, lx, ly, w, h, key):
    """Erythrocytic fruiting body: wet crimson cap, pale flecks on top, pale gills beneath."""
    n = noise('cap', key, face, lx, ly)
    if face == 'down':
        return clamp((214, 176, 168), int(n * 12) - 6 - (20 if lx % 2 else 0)) + (255,)
    if face == 'up' and n > .78:
        return clamp(BONE_PALE, -10) + (255,)
    return clamp(CAP, int(n * 14) - 7 + (12 if ly == 0 else 0)) + (255,)


def stipe(face, lx, ly, w, h, key):
    return clamp((208, 186, 172), int(noise('stipe', key, face, lx, ly) * 14) - 7) + (255,)


def robe(face, lx, ly, w, h, key):
    """The gisant's robe cast in hematic iron: deep folds running its length, blood in the grain."""
    n = noise('robe', key, face, lx, ly)
    fold = (lx + int(noise('pleat', key, face) * 4)) % 3
    shade = {0: -24, 1: 14, 2: 0}[fold]
    if noise('vein', key, face, lx, ly // 2) < .04:
        return EFFIGY_SEAM + (255,)
    return clamp(EFFIGY, shade + int(n * 8) - 4) + (255,)


def effigy(face, lx, ly, w, h, key):
    n = noise('effigy', key, face, lx, ly)
    if noise('vein', key, face, lx, ly) < .035:
        return EFFIGY_SEAM + (255,)
    return clamp(EFFIGY, int(n * 12) - 6 + (22 if ly == 0 and face not in ('up', 'down') else 0)
                 + (8 if face == 'up' else 0)) + (255,)


def mask(face, lx, ly, w, h, key):
    """The veiled head: a serene bone mask with closed eyes weeping blood, framed by the iron hood.

    Laid out on the 8 x 7 face, chin at ly = 0 and crown at ly = 6 under the hood."""
    n = noise('mask', key, lx, ly)
    if lx in (0, w - 1) or ly == h - 1:
        return effigy(face, lx, ly, w, h, key)
    seam = (58, 30, 30, 255)
    mid = w // 2
    for eye in (mid - 2, mid + 1):
        if ly == 4 and abs(lx - eye - .5 + (1 if eye < mid else 0)) <= 1:
            return seam
        if lx == eye and ly in (2, 3):
            return clamp(PALETTE['blood'], 24 + int(n * 16)) + (255,)
    if lx in (mid - 1, mid) and ly == 3:
        return clamp(BONE_PALE, -48) + (255,)
    if ly == 1 and lx in (mid - 1, mid):
        return seam
    return clamp(BONE_PALE, int(n * 8) - 4 - abs(lx * 2 - w + 1) * 2) + (255,)


def tassel_cushion(face, lx, ly, w, h, key):
    if (lx in (0, w - 1)) and (ly in (0, h - 1)):
        return clamp(PALETTE['bone'], -10) + (255,)
    return effigy(face, lx, ly, w, h, key)


def heart(face, lx, ly, w, h, key):
    n = noise('heart', key, lx, ly)
    if abs(lx - (w - 1) / 2) < 1 and ly < h - 1:
        return clamp(PALETTE['lacquer_deep'], -10) + (255,)
    return clamp(PALETTE['lacquer'], 18 - ly * 4 + int(n * 8)) + (255,)


def skull(face, lx, ly, w, h, key):
    """A skull at the gisant's feet, staring at whoever kneels at the bowl: 5 x 5 texels, brow at ly = 0."""
    n = noise('skull', key, lx, ly)
    if ly in (1, 2) and lx in (1, w - 2):
        return (24, 10, 12, 255)
    if ly == 3 and lx == w // 2:
        return (40, 18, 18, 255)
    if ly == h - 1 and 1 <= lx <= w - 2:
        return (40, 18, 18, 255) if lx % 2 == 0 else clamp(BONE_PALE, -8) + (255,)
    return clamp(PALETTE['bone'], int(n * 12) - 6 - (12 if lx in (0, w - 1) else 0)) + (255,)


def stain(face, lx, ly, w, h, key):
    """Iron gone black with old offerings."""
    n = noise('stain', key, face, lx, ly)
    return clamp(STAIN, int(n * 16) - 8) + (255,)


def strap(face, lx, ly, w, h, key):
    """A strap of the chest: hematic iron texel for texel, or with STRAP set, aged bone in finger-like segments, a
    dark knuckle joint every four texels with the bone swelling either side of it and shading toward its edges.

    Joints are laid out from the middle of the strap's long side, so faces whose u runs the other way still meet."""
    if not STRAP:
        return paint('iron', face, lx, ly, w, h, key, {})
    n = noise('strap', key, face, lx, ly)
    along, across, length, width = (lx, ly, w, h) if w >= h else (ly, lx, h, w)
    if length >= 6:
        d = abs(along - (length - 1) / 2)
        step = int(d) % 4
        if step == 3:
            return clamp(STRAP_JOINT, int(n * 10) - 5) + (255,)
        swell = 10 if step == 2 or (step == 0 and d >= 1) else 0
    else:
        swell = 6
    edge = -16 if width >= 3 and across in (0, width - 1) else 0
    if noise('stain', key, face, lx // 2, ly) < .06:
        return clamp(STRAP_JOINT, 26 + int(n * 8)) + (255,)
    return clamp(STRAP, swell + edge + int(n * 12) - 6) + (255,)


def ash(face, lx, ly, w, h, key):
    n = noise('ash', key, face, lx, ly)
    if face == 'up' and n > .86:
        return (96, 30, 26, 255)
    return clamp((54, 48, 50), int(n * 18) - 9) + (255,)


def coal(face, lx, ly, w, h, key):
    """Live coals: crimson embers with hot cores."""
    n = noise('coal', key, face, lx, ly)
    if n > .8:
        return (255, 196, 120, 255)
    if n > .45:
        return (240, 84, 44, 255)
    return (168, 22, 26, 255)


def pool_tex(face, lx, ly, w, h, key):
    n = noise('pool', key, face, lx, ly)
    if face == 'up' and (lx + ly * 2) % 7 == 0 and n > .5:
        return (196, 52, 60, 255)
    return clamp((124, 8, 20), int(n * 14) - 7) + (255,)


def veins_tex(face, lx, ly, w, h, key):
    """Memory veins kindling under dead flesh: sparse branching crimson threads, clear between them."""
    lane = lx * 2 + ly + int(noise('trunk', key, face) * 13)
    trunk = lane % 9 == 0 and noise('run', key, face, lane // 9, ly // 3) < .55
    twig = noise('twig', key, face, lx, ly // 2) < .03
    if trunk or twig:
        return (VEIN_HOT if noise('hot', key, face, lx, ly) > .8 else VEIN_CORE) + (255,)
    return (0, 0, 0, 0)


SPECIAL = dict(tomb=tomb, brick=brick, frieze=frieze, plaque=plaque, sigil=sigil, cornice=cornice, pall=pall,
               linen=linen, skin=skin, face=face_tex, knuckles=knuckles, vial=vial, stole=stole, cap=cap,
               stipe=stipe, robe=robe, effigy=effigy, mask=mask, tassel_cushion=tassel_cushion, heart=heart,
               skull=skull, tallow=tallow, knuckles_iron=knuckles_iron, stain=stain, strap=strap, ash=ash, coal=coal, pool=pool_tex,
               veins=veins_tex)


# --- outputs --------------------------------------------------------------------------------

# The item is the whole tomb at half scale, so its display scales are doubled.
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1, 0], 'scale': [0.44, 0.44, 0.44]},
    'ground': {'translation': [0, 2, 0], 'scale': [0.22, 0.22, 0.22]},
    'fixed': {'rotation': [0, 90, 0], 'translation': [0, 0, 0], 'scale': [0.44, 0.44, 0.44]},
    'head': {'translation': [0, 6, 0], 'scale': [0.6, 0.6, 0.6]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.2, 0.2, 0.2]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.2, 0.2, 0.2]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2, 0], 'scale': [0.18, 0.18, 0.18]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2, 0], 'scale': [0.18, 0.18, 0.18]},
}


def blockstate():
    variants = {}
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        variants[f'facing={facing}'] = dict({'model': 'hemomancy:block/saint_sarcophagus'}, **({'y': y} if y else {}))
    return {'variants': variants}


def drop_culling(elements, keep=('down',)):
    """The tomb spans twelve cells, so one neighbour cannot hide a whole face; only the plinth floor may cull."""
    for el in elements:
        for side, f in el['faces'].items():
            if side not in keep:
                f.pop('cullface', None)
    return elements


def lerp(a, b, t):
    return round(a + (b - a) * t, 5)


def cut_z(elements, at=HEAD_CUT):
    """Cut painted elements at z = `at` into the foot section and the head section shifted back by `at`.

    UVs are split in proportion, following vanilla's face orientation: east runs u from the far z to the near, west
    from near to far, up runs v from near to far and down from far to near. North stays with the foot piece, south
    with the head piece, and the two faces the cut makes are left open."""
    foot, head = [], []
    for el in copy.deepcopy(elements):
        z0, z1 = el['from'][2], el['to'][2]
        if z1 <= at:
            foot.append(el)
            continue
        if z0 >= at:
            head.append(shift_z(el, -at))
            continue
        assert 'rotation' not in el, ('a rotated element crosses the cut', el['name'])
        t = (at - z0) / (z1 - z0)
        a, b = copy.deepcopy(el), copy.deepcopy(el)
        a['to'][2] = at
        b['from'][2] = at
        a['faces'].pop('south', None)
        b['faces'].pop('north', None)
        for face in ('east', 'west', 'up', 'down'):
            if face not in el['faces']:
                continue
            u0, v0, u1, v1 = el['faces'][face]['uv']
            if face == 'east':
                mid = lerp(u1, u0, t)
                a['faces'][face]['uv'] = [mid, v0, u1, v1]
                b['faces'][face]['uv'] = [u0, v0, mid, v1]
            elif face == 'west':
                mid = lerp(u0, u1, t)
                a['faces'][face]['uv'] = [u0, v0, mid, v1]
                b['faces'][face]['uv'] = [mid, v0, u1, v1]
            elif face == 'up':
                mid = lerp(v0, v1, t)
                a['faces'][face]['uv'] = [u0, v0, u1, mid]
                b['faces'][face]['uv'] = [u0, mid, u1, v1]
            else:
                mid = lerp(v1, v0, t)
                a['faces'][face]['uv'] = [u0, mid, u1, v1]
                b['faces'][face]['uv'] = [u0, v0, u1, mid]
        foot.append(a)
        head.append(shift_z(b, -at))
    return foot, head


def shift_z(el, dz):
    el['from'][2] = round(el['from'][2] + dz, 4)
    el['to'][2] = round(el['to'][2] + dz, 4)
    if 'rotation' in el:
        el['rotation']['origin'][2] += dz
    return el


def scaled(elements, s, origin, offset=(0, 0, 0)):
    """Elements scaled by `s` about `origin`, then moved by `offset`; UVs untouched, so the texture stays whole."""
    out = []
    for el in copy.deepcopy(elements):
        for k in ('from', 'to'):
            el[k] = [round((v - o) * s + o + d, 4) for v, o, d in zip(el[k], origin, offset)]
        if 'rotation' in el:
            el['rotation']['origin'] = [(v - o) * s + o + d for v, o, d in zip(el['rotation']['origin'], origin,
                                                                               offset)]
        for f in el['faces'].values():
            f.pop('cullface', None)
        out.append(el)
    return out


def opened(elements, t=1.0):
    """Lid elements slid aside and tipped over the rim, as the renderer draws them at open fraction t."""
    slide = LID_SLIDE * min(t / .7, 1)
    tip = LID_TIP * max((t - .6) / .4, 0)
    out = []
    for el in copy.deepcopy(elements):
        assert 'rotation' not in el, el['name']
        el['from'][0] += slide
        el['to'][0] += slide
        if tip:
            el['rotation'] = {'angle': -tip, 'axis': 'z', 'origin': [LID_PIVOT[0], LID_PIVOT[1], 8]}
        out.append(el)
    return out


def check_cells():
    """Every cell the fillers stand in carries some of the tomb, so none is an invisible full block."""
    for cx, cy, cz in CELLS:
        lo, hi = (cx * 16, cy * 16, cz * 16), (cx * 16 + 16, cy * 16 + 16, cz * 16 + 16)
        assert any(all(b[i] < hi[i] and b[i + 3] > lo[i] for i in range(3)) for b in COLLISION), (cx, cy, cz)
        for b in COLLISION:
            assert -8 <= b[0] and b[3] <= 24 and 0 <= b[1] and b[4] <= 32 and -16 <= b[2] and b[5] <= 48, b


def fmt(v):
    return f'{v:g}' if v != int(v) else f'{v:.1f}'


def geometry_java():
    boxes = ',\n'.join('        {' + ', '.join(fmt(v) for v in b) + '}' for b in COLLISION)
    cressets = ',\n'.join('        {' + ', '.join(fmt(v) + 'f' for v in c) + '}' for c in CRESSETS)
    fillers = ', '.join(f'new BlockPos({x}, {y}, {z})' for x, y, z in CELLS if (x, y, z) != (0, 0, 0))
    palettes = ', '.join(f'"{name}"' for name in painted())
    x0, y0, z0, x1, y1, z1 = POOL
    return f'''package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Generated by tools/model_export/saint_sarcophagus.py; pixels of the north-facing tomb, which is centred on the main
 * block's x (x -8..24) with its foot end at z = -16 and its head end at z = 48.
 */
public final class SarcophagusGeometry {{
    /** The cells the fillers stand in, from the main block of a north-facing tomb (turn with rotateNorthOffsets). */
    public static final BlockPos[] FILLERS = {{{fillers}}};
    /** Every part is cut here: the {{@code _head}} models hold the last cell, exported this many pixels back. */
    public static final int HEAD_CUT = {HEAD_CUT};
    /** The tomb's centre on the floor (x, z), where its Saint rises. */
    public static final float CENTRE_X = {fmt(CENTRE[0])}f, CENTRE_Z = {fmt(CENTRE[1])}f;
    /** Cornice edge (x, y) the lid tips over once it has slid {fmt(LID_SLIDE)} pixels east. */
    public static final float LID_PIVOT_X = {fmt(LID_PIVOT[0])}f, LID_PIVOT_Y = {fmt(LID_PIVOT[1])}f;
    public static final float LID_SLIDE = {fmt(LID_SLIDE)}f, LID_TIP = {fmt(LID_TIP)}f;
    /** Offering bowl blood at full fill: floor and surface heights; its centre is the bowl particle anchor. */
    public static final float POOL_FLOOR = {fmt(y0)}f, POOL_TOP = {fmt(y1)}f;
    public static final float BOWL_X = {fmt((x0 + x1) / 2)}f, BOWL_Z = {fmt((z0 + z1) / 2)}f;
    /** Top of each cresset's coals (x, y, z). */
    public static final float[][] CRESSETS = {{
{cressets}
    }};
    /** The Saints whose tomb wears its own palette: {{@code saint_sarcophagus_<name>}}, in the default's layout. */
    public static final String[] SAINT_PALETTES = {{{palettes}}};
    private static final double[][] BOXES = {{
{boxes}
    }};
    private static final Map<BlockPos, VoxelShape>[] CELLS = buildCells();

    private SarcophagusGeometry() {{}}

    /** The block texture the tomb of {{@code saint}} is painted with: its own palette if it has one, else the default. */
    public static String atlas(@Nullable EnumSaintType saint) {{
        if (saint != null)
            for (String name : SAINT_PALETTES)
                if (name.equals(saint.name().toLowerCase(Locale.ROOT))) return "{ATLAS}_" + name;
        return "{ATLAS}";
    }}

    public static VoxelShape shape(Direction facing, BlockPos offset) {{
        return CELLS[facing.get2DDataValue()].getOrDefault(offset, Shapes.empty());
    }}

    /** A north-facing pixel point turned to {{@code facing}} and returned in block units from the main block. */
    public static double[] point(Direction facing, double x, double y, double z) {{
        for (int turn = 0; turn < turns(facing); turn++) {{
            double nextX = 16 - z;
            z = x;
            x = nextX;
        }}
        return new double[]{{x / 16, y / 16, z / 16}};
    }}

    private static int turns(Direction facing) {{
        return switch (facing) {{ case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0; }};
    }}

    @SuppressWarnings("unchecked")
    private static Map<BlockPos, VoxelShape>[] buildCells() {{
        Map<BlockPos, VoxelShape>[] result = new Map[4];
        for (Direction facing : Direction.Plane.HORIZONTAL) {{
            Map<BlockPos, VoxelShape> cells = new LinkedHashMap<>();
            for (double[] box : BOXES) {{
                double x0=box[0], z0=box[2], x1=box[3], z1=box[5];
                for (int turn = 0; turn < turns(facing); turn++) {{
                    double nextX0=16-z1, nextX1=16-z0;
                    z0=x0; z1=x1; x0=nextX0; x1=nextX1;
                }}
                VoxelShape shape = Block.box(x0, box[1], z0, x1, box[4], z1);
                for (int x=(int)Math.floor(x0/16); x<(int)Math.ceil(x1/16); x++)
                    for (int y=(int)Math.floor(box[1]/16); y<(int)Math.ceil(box[4]/16); y++)
                        for (int z=(int)Math.floor(z0/16); z<(int)Math.ceil(z1/16); z++) {{
                            BlockPos offset = new BlockPos(x,y,z);
                            VoxelShape clipped = Shapes.join(shape.move(-x,-y,-z), Shapes.block(), BooleanOp.AND);
                            if (!clipped.isEmpty()) cells.merge(offset, clipped, Shapes::or);
                        }}
            }}
            cells.replaceAll((offset, shape) -> shape.optimize());
            result[facing.get2DDataValue()] = Map.copyOf(cells);
        }}
        return result;
    }}
}}
'''


def centred(elements, lift=8):
    """Whole-tomb elements moved so the preview camera (which looks at 8, 8, 8) sees the middle of the tomb."""
    return scaled(elements, 1, (0, 0, 0), (0, -lift, 8 - CENTRE[1]))


def closeup(elements, atlas, title, out_name, views, size=640, scale=11):
    """A larger sheet of chosen (yaw, pitch) views for reading the corpus and gisant."""
    import numpy as np
    from PIL import Image, ImageDraw
    from clairaudiograph import render
    texture = np.array(Image.open(ASSETS / f'textures/block/{atlas}.png').convert('RGBA'))
    sheet = Image.new('RGBA', (size * len(views), size + 30), (30, 24, 28, 255))
    for i, (yaw, pitch) in enumerate(views):
        sheet.paste(render(elements, {'atlas': texture}, yaw, pitch, size, scale), (size * i, 30))
    ImageDraw.Draw(sheet).text((10, 8), title, fill=(220, 200, 200))
    out = ROOT / f'build/model-previews/{out_name}.png'
    sheet.convert('RGB').save(out)
    print(out)


def groups():
    """Every part of the tomb by group, coplanar faces pushed apart, and how many faces that moved; the same
    whatever the palette."""
    stand, coals = cressets()
    body_corpus, flesh = corpus()
    body = dais() + chest() + body_corpus + bowl() + stand
    top, glow = lid(), veins(flesh)
    moved = separate(body) + separate(top)
    return {'body': body, 'lid': top, 'blood': pool(), 'coals': coals, 'veins': glow}, moved


def paint_palette(name, layout):
    """Paint `name`'s atlas in the default layout; returns the elements, which must match the default's exactly."""
    with palette(name):
        atlas, models = build(groups()[0], SPECIAL)
    assert models == layout, f'{name} packed differently from the default atlas'
    atlas.save(ASSETS / f'textures/block/{ATLAS}_{name}.png')
    print(f'Painted the {name} palette, 128x{atlas.height}')
    return models


def previews(models, atlas, label, suffix=''):
    whole = models['body'] + models['lid']
    lit = centred(models['body'] + models['blood'] + models['coals'])
    preview(centred(whole), atlas, f'{label} - sealed, actual atlas', f'saint_sarcophagus{suffix}', size=420, scale=5)
    preview(lit + centred(opened(models['lid']) + models['veins']), atlas,
            f'{label} - consecrated, lid slid aside, corpus kindled', f'saint_sarcophagus{suffix}_open',
            size=420, scale=5)
    closeup(lit + centred(models['veins']), atlas, f'{label} - corpus from above (lid removed)',
            f'saint_sarcophagus{suffix}_corpus', [(110, 55), (60, 70), (160, 50)])
    closeup(centred(whole), atlas, f'{label} - gisant and foot end', f'saint_sarcophagus{suffix}_gisant',
            [(-30, 35), (30, 25), (120, 40)])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--saint', choices=sorted(PALETTES),
                        help="repaint only this Saint's atlas (and its previews), leaving every other output alone")
    args = parser.parse_args()
    if args.saint and not PALETTES[args.saint]:
        sys.exit(f'{args.saint} has no sarcophagus palette yet; its theme track adds one to PALETTES')
    check_cells()
    parts, moved = groups()
    atlas, models = build(parts, SPECIAL)
    if args.saint:
        paint_palette(args.saint, models)
        if args.preview:
            previews(models, f'{ATLAS}_{args.saint}', f'Saint Sarcophagus ({args.saint})', f'_{args.saint}')
        return
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    for name in painted():
        paint_palette(name, models)
    particle = 'hemomancy:block/polished_venous_stone'

    def block(elements):
        for el in elements:
            assert all(-16 <= v <= 32 for v in el['from'] + el['to']), (el['name'], el['from'], el['to'])
        return model_json(elements, ATLAS, atlas.height, particle)

    outputs = {}
    for part, elements in models.items():
        foot, head = cut_z(elements)
        name = 'saint_sarcophagus' if part == 'body' else f'saint_sarcophagus_{part}'
        outputs[f'block/{name}'] = block(drop_culling(foot))
        outputs[f'block/{name}_head'] = block(drop_culling(head, keep=()))
    whole = models['body'] + models['lid']
    item = model_json(scaled(whole, .5, (8, 0, 8), (0, 0, -4)), ATLAS, atlas.height, particle, DISPLAY)
    item['gui_light'] = 'front'
    outputs['item/saint_sarcophagus'] = item
    project = dict(item, elements=item['elements'] + scaled(models['blood'] + models['coals'] + models['veins'],
                                                            .5, (8, 0, 8), (0, 0, -4)))
    write(outputs, 'saint_sarcophagus', project)
    (ASSETS / 'blockstates/saint_sarcophagus.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    GEOMETRY.write_text(geometry_java())
    print(f'Exported Saint Sarcophagus: {len(parts["body"])} body, {len(parts["lid"])} lid, '
          f'{len(parts["coals"])} coal, {len(parts["veins"])} vein cubes, '
          f'{moved} faces separated, 128x{atlas.height} atlas, '
          f'{len(CELLS) - 1} fillers, palettes: {", ".join(painted()) or "none"}')
    if args.preview:
        previews(models, ATLAS, 'Saint Sarcophagus')
        for name in painted():
            previews(models, f'{ATLAS}_{name}', f'Saint Sarcophagus ({name})', f'_{name}')

if __name__ == '__main__':
    main()
