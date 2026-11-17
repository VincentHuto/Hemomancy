"""Author the Specimen Vat: a two-block glass cylinder in a hematic iron cage, where a kept creature stands.

A stepped iron plinth with a bold band carries a hollow octagonal glass column. Four iron ribs climb the
diagonals and two strap bands bind the glass, so the creature inside stays in view between them. The
iron crown caps the column with a valve boss and bone spurs on its diagonals. A feeding vein (the
living buffer's line) runs from a port on the plinth up the back rib and into the crown: the vat is a
living vessel, fed by Projection or a suture, never a tank.

Harbinger materials only: hematic iron, bone, venous stone and hemorrhagic crust. No copper or its alloys.
The column is fired vivianite, the glass blood passes through (docs/LORE_REFERENCE.md, "Vivianite glass"): the
vivianite_glass block's green thinned to a clear wall, pitted with fired pores, with a thick lip of the block's own
glass where it seats in the plinth and crown.

The block is two tall. The whole body is one model on the lower half (elements reach y 32); the upper
half shows nothing of its own. The glass is a separate translucent model so the creature drawn by
SpecimenVatRenderer shows through it. Inner clear radius: GLASS_RADIUS - GLASS_THICK.

Vats join (VatAssembly): empty vats side by side or stacked become one vessel, up to three across and two high, and
the vessel is the same octagonal vat grown to its size: the same plinth, column, ribs, straps, lips, crown, spurs and
feeding vein, drawn about the vessel's middle (Vessel). A vessel wider than one vat is drawn a little inside its
blocks (INSET) so its spurs and port stay within the model limits. One vat of each layer carries that layer's slice of
the vessel (the middle vat of a vessel three across, the corner vat otherwise: a block model reaches a block past its
own on every side, enough for three across); the others draw nothing. The blockstate reads each vat's `part`
(VatPart): `single` for a lone vat, `hidden`, or the size and layer it carries.

Emits the body, glass, vessel-slice and item models, the blockstate and an editable Blockbench project.
`--preview` writes build/specimen_vat_preview.png. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import station_kit as kit
from station_kit import Model

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'venous': 'hemomancy:block/polished_venous_stone',
    'bone': 'minecraft:block/bone_block_side',
    'bonecap': 'minecraft:block/bone_block_top',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'glass': 'hemomancy:block/specimen_vat_glass',
    'lip': 'hemomancy:block/vivianite_glass',
}
CAPS = {'bone': 'bonecap', 'pillar': 'pillartop'}
NAME = 'specimen_vat'

CENTRE = (8.0, 8.0)
C3 = (CENTRE[0], 0, CENTRE[1])
DIAGONALS = (45, 135, 225, 315)

PLINTH_TOP, STEP_TOP = 2.0, 3.2
GLASS_RADIUS, GLASS_THICK = 6.6, .35
GLASS_BOTTOM, GLASS_TOP = STEP_TOP, 27.4
CROWN_BOTTOM, CROWN_TOP = GLASS_TOP, 30.0
BANDS = (11.0, 19.5)
LIP = 1.0

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -3.5, 0], 'scale': [.38, .38, .38]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.2, .2, .2]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -4, 0], 'scale': [.38, .38, .38]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 10, 0], 'scale': [.5, .5, .5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 0], 'scale': [.25, .25, .25]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 1.5, 0], 'scale': [.25, .25, .25]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [.25, .25, .25]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [.25, .25, .25]},
}


class Vessel:
    """A vat's outline: `width` vats across and `layers` high. A lone vat is Vessel(1, 1); its radii are the ones above,
    and a wider vessel keeps every part's distance from the vessel's edge (less INSET)."""

    def __init__(self, width=1, layers=1):
        self.width, self.layers = width, layers
        self.half = 8.0 * width
        self.centre = (self.half, self.half)
        self.c3 = (self.half, 0, self.half)
        self.inset = INSET if width > 1 else 0.0
        self.top = 32.0 * (layers - 1)

    def r(self, radius):
        """A lone vat's radius, moved out with the vessel's edge."""
        return self.half - (8.0 - radius) - self.inset


# How far inside its blocks a vessel wider than one vat is drawn, so the spurs and port stay in the model limits.
INSET = .95


def plinth(m, v):
    """Stepped iron plinth on a venous-stone footing, a bold band round it and a feeding port at the back."""
    m.octo('footing', 'y', v.c3, v.r(7.8), 0, .8, 'venous')
    m.octo('plinth', 'y', v.c3, v.r(7.4), .8, PLINTH_TOP, 'chisel')
    m.shell('plinth_band', v.centre, v.r(7.6), 1.1, 1.7, .25, 'bold', ends=True)
    m.octo('plinth_step', 'y', v.c3, v.r(6.9), PLINTH_TOP, STEP_TOP, 'iron')
    m.octo('floor', 'y', v.c3, v.r(GLASS_RADIUS) - GLASS_THICK + .05, STEP_TOP, STEP_TOP + .1, 'venous', faces=('up',))
    # The feeding port faces back (south in the model; the block faces north by default): where blood comes in.
    m.radial('port', v.centre, 270, v.r(7.2), v.r(8.4), -1.1, 1.1, 1.0, 2.8, 'bold')
    m.radial('port_mouth', v.centre, 270, v.r(8.35), v.r(8.45), -.6, .6, 1.4, 2.4, 'vein')


def column(m, v):
    """The glass column, four diagonal ribs and two strap bands a layer."""
    glass, top = v.r(GLASS_RADIUS), v.top + GLASS_TOP
    m.shell('glass', v.centre, glass, GLASS_BOTTOM, top, GLASS_THICK, 'glass')
    # The fired lip: thick vivianite where the column seats, as dense as the block it is made from.
    for y0, y1 in ((GLASS_BOTTOM, GLASS_BOTTOM + LIP), (top - LIP, top)):
        m.shell('lip', v.centre, glass + .2, y0, y1, GLASS_THICK + .3, 'lip')
    for deg in DIAGONALS:
        m.radial('rib', v.centre, deg, glass - .15, glass + .75, -.55, .55, GLASS_BOTTOM, top, 'pillar')
        m.radial('rib_foot', v.centre, deg, glass - .1, glass + 1.0, -.8, .8, STEP_TOP, STEP_TOP + 1.2, 'chisel')
        m.radial('rib_head', v.centre, deg, glass - .1, glass + 1.0, -.8, .8, top - 1.2, top, 'chisel')
    for layer in range(v.layers):
        for y in BANDS:
            y += 32.0 * layer
            m.shell('band', v.centre, glass + .3, y, y + .8, .3, 'iron', ends=True)
            for deg in DIAGONALS:
                m.radial('band_rivet', v.centre, deg, glass + .7, glass + 1.05, -.3, .3, y + .15, y + .65, 'bonecap')


def crown(m, v):
    """Iron crown with a valve boss, bone spurs on the diagonals and the vein coming in at the back."""
    bottom, top = v.top + CROWN_BOTTOM, v.top + CROWN_TOP
    m.octo('crown', 'y', v.c3, v.r(7.4), bottom, bottom + 1.4, 'chisel')
    m.shell('crown_band', v.centre, v.r(7.6), bottom + .3, bottom + 1.0, .25, 'bold', ends=True)
    m.octo('crown_step', 'y', v.c3, v.r(5.6), bottom + 1.4, top, 'iron')
    # A wider lid carries a wider valve, so the boss still reads on a vessel three vats across.
    k = 1.0 + .35 * (v.width - 1)
    m.octo('valve', 'y', v.c3, 2.2 * k, top, top + 1.0, 'bold')
    m.octo('valve_stem', 'y', v.c3, 1.0 * k, top + 1.0, v.top + 31.8, 'pillar')
    m.octo('valve_cap', 'y', v.c3, 1.5 * k, v.top + 31.2, v.top + 31.8, 'vein', faces=('up', 'north', 'south', 'east', 'west'))
    for deg in DIAGONALS:
        m.radial('spur', v.centre, deg, v.r(6.4), v.r(8.2), -.45, .45, bottom + 1.0, bottom + 1.6, 'bone')
        m.radial('spur_tip', v.centre, deg, v.r(8.2), v.r(8.9), -.28, .28, bottom + 1.2, bottom + 1.5, 'bonecap')


def vein(m, v):
    """The living buffer's line: from the port up the back of the column to the crown, a thin crust vein."""
    glass = v.r(GLASS_RADIUS)
    m.radial('vein_rise', v.centre, 270, glass + .45, glass + 1.15, -.35, .35, 2.8, v.top + CROWN_BOTTOM, 'vein')
    for layer in range(v.layers):
        y = 32.0 * layer
        m.radial('vein_knot', v.centre, 270, glass + .35, glass + 1.35, -.6, .6, y + 14.6, y + 15.8, 'vein')
        m.radial('vein_knot', v.centre, 270, glass + .35, glass + 1.35, -.55, .55, y + 23.0, y + 24.0, 'vein')
    m.radial('vein_crown', v.centre, 270, 2.0 * (1.0 + .35 * (v.width - 1)), v.r(7.2), -.35, .35, v.top + CROWN_TOP,
             v.top + CROWN_TOP + .5, 'vein')


def body(vessel=None):
    v = vessel or Vessel()
    m = Model(translucent=('glass', 'lip'), caps=CAPS)
    plinth(m, v)
    column(m, v)
    crown(m, v)
    vein(m, v)
    return m


# -- Joined vessels --

# Every vessel of more than one vat: (width, layers). A vessel one across and one high is a lone vat.
VESSELS = [(width, layers) for width in (1, 2, 3) for layers in (1, 2) if (width, layers) != (1, 1)]


def part_name(width, layers, layer):
    """The VatPart a vessel's carrying vat shows: w2_only, w3_bottom, w1_top..."""
    role = 'only' if layers == 1 else ('bottom', 'top')[layer]
    return 'w%d_%s' % (width, role)


def carrier(width):
    """Which vat of a layer carries the layer's slice, from the vessel's corner: the middle of three, else the corner."""
    return 1 if width == 3 else 0


def layer_slice(model, layer, offset):
    """The elements of a whole-vessel model in one layer, moved into the carrying vat's block: cut at the layer's
    floor and ceiling (the cut ends draw no face), shifted by the carrying vat's place, every UV laid again."""
    out = Model(model.translucent, model.caps)
    low, high = 32.0 * layer, 32.0 * layer + 32.0
    shift = (16.0 * offset, low, 16.0 * offset)
    for e in model.elements:
        a, b = list(e['a']), list(e['b'])
        if b[1] <= low or a[1] >= high:
            continue
        faces = dict(e['faces'])
        if a[1] < low:
            a[1] = low
            faces.pop('down', None)
        if b[1] > high:
            b[1] = high
            faces.pop('up', None)
        a = [round(p - d, 4) for p, d in zip(a, shift)]
        b = [round(p - d, 4) for p, d in zip(b, shift)]
        assert all(-16 <= p <= 32 for p in a + b), (e['name'], a, b)
        rot = e['rot']
        if rot:
            axis, angle, origin = rot
            assert axis == 'y', e['name']
            rot = (axis, angle, [round(origin[0] - shift[0], 4), a[1], round(origin[2] - shift[2], 4)])
        faces = {side: (mat, kit.uv(side, a, b)) for side, (mat, _) in faces.items()}
        out.elements.append(dict(name=e['name'], a=a, b=b, rot=rot, faces=faces, extra=e['extra']))
    return out


def blockstate():
    """Lower half: a lone vat's cutout body and translucent glass, turned by facing, or the slice of a joined vessel
    its vat carries. Upper half, and a joined vat carrying nothing: nothing of their own."""
    parts = []
    for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for model in (NAME, NAME + '_glass'):
            apply = {'model': 'hemomancy:block/' + model}
            if angle:
                apply['y'] = angle
            parts.append({'apply': apply, 'when': {'facing': facing, 'half': 'lower', 'part': 'single'}})
    for width, layers in VESSELS:
        for layer in range(layers):
            part = part_name(width, layers, layer)
            for model in (NAME + '_' + part, NAME + '_' + part + '_glass'):
                parts.append({'apply': {'model': 'hemomancy:block/' + model}, 'when': {'half': 'lower', 'part': part}})
    parts.append({'apply': {'model': 'hemomancy:block/' + NAME + '_upper'}, 'when': {'half': 'upper'}})
    return {'multipart': parts}


# Fired vivianite (docs/LORE_REFERENCE.md, "Vivianite glass"): the vivianite_glass block's green, thinned so the
# creature shows through, and pitted with the round pores its driven-off water leaves. The pores are uneven in size
# and spread, finer toward the face (x 0 and 15 sit at the octagon's edges), and a few hold a bead of blood.
GLASS_TINT = (34, 140, 98)
PORE_RIM = (10, 74, 50)
PORE_HOLLOW = (150, 214, 186)
BLOOD_BEAD = (120, 14, 22)
# (x, y, size): size 1 is a single dark pit, size 2 a 2x2 pore with a pale hollow at its upper left.
PORES = ((4, 2, 2), (9, 1, 1), (13, 4, 1), (6, 6, 1), (10, 7, 2), (3, 9, 1), (7, 11, 2), (12, 12, 1),
         (5, 14, 1), (14, 9, 1), (8, 4, 1), (11, 14, 1))
BEADED = ((10, 7), (7, 11))


def glass_texture():
    """Thin vivianite-green glass with a pale highlight down one side, pitted with fired pores, a few blood-filled,
    so the column reads as the porous glass blood passes through while the creature inside stays plain to see."""
    from PIL import Image
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            alpha = 70 + (8 if (x * 7 + y * 3) % 11 == 0 else 0)
            colour = GLASS_TINT
            if x in (1, 2):
                colour, alpha = (196, 236, 218), 128 if x == 1 else 92
            px[x, y] = colour + (alpha,)
    for x, y, size in PORES:
        beaded = (x, y) in BEADED
        for dy in range(size):
            for dx in range(size):
                hollow = size == 2 and dx == 0 and dy == 0
                colour = BLOOD_BEAD if beaded and not hollow else PORE_HOLLOW if hollow else PORE_RIM
                px[x + dx, y + dy] = colour + ((200 if beaded and not hollow else 150 if hollow else 140),)
    image.save(kit.ASSETS / 'textures/block' / (NAME + '_glass.png'))


def export():
    glass_texture()
    whole = body()
    solid, glass = kit.split_glass(whole)
    kit.separate([solid, glass], TEXTURES, {'glass', 'lip'}, 'Specimen Vat')
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron'))
    kit.write_json(kit.MODELS / (NAME + '_glass.json'), kit.to_json(glass, TEXTURES, 'glass'))
    kit.write_json(kit.MODELS / (NAME + '_upper.json'),
                   {'textures': {'particle': TEXTURES['iron']}, 'elements': []})
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(whole, TEXTURES, 'iron', DISPLAY))
    for width, layers in VESSELS:
        vessel_solid, vessel_glass = kit.split_glass(body(Vessel(width, layers)))
        for layer in range(layers):
            part = NAME + '_' + part_name(width, layers, layer)
            # Separated a layer at a time: a slice is the size of a lone layer's vessel, which the kit settles.
            solid_slice = layer_slice(vessel_solid, layer, carrier(width))
            glass_slice = layer_slice(vessel_glass, layer, carrier(width))
            kit.separate([solid_slice, glass_slice], TEXTURES, {'glass', 'lip'}, 'Specimen Vat ' + part)
            kit.write_json(kit.MODELS / (part + '.json'), kit.to_json(solid_slice, TEXTURES, 'iron'))
            kit.write_json(kit.MODELS / (part + '_glass.json'), kit.to_json(glass_slice, TEXTURES, 'glass'))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    project = kit.bbmodel(NAME, [('body', solid), ('glass', glass)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    return whole


def preview():
    whole = [(body(), kit.identity)]
    panels = [('Front', 200, 14, whole), ('Three-quarter', 235, 22, whole),
              ('Back', 20, 14, whole), ('Above', 210, 60, whole)]
    out = kit.preview(panels, 4, kit.ROOT / 'build/specimen_vat_preview.png', (360, 620), 15, (8, 16, 8), TEXTURES)
    vessels = [('%d x %d x %d' % (w, w, l), 235, 22, [(body(Vessel(w, l)), kit.identity)])
               for w, l in ((2, 1), (3, 1), (2, 2), (3, 2))]
    kit.preview(vessels, 4, kit.ROOT / 'build/specimen_vat_vessels_preview.png', (420, 620), 6, (24, 32, 24), TEXTURES)
    return out


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Specimen Vat: %d elements' % len(model.elements))
    if args.preview:
        print(preview())
