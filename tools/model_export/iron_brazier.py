"""Author the Iron Brazier: a rite brazier raised on a hematic iron pillar over a venous-stone dais.

The model follows its blood-structure recipe, iron block, iron pillar, venous stone: an octagonal
iron dais set with a venous-stone inlay carries a hematic iron pillar, a blood vein running up its
front. Above it sits a stepped iron bowl that holds a coal bed. Four iron talons curl up from the
rim and cradle the offering that IronBrazierRenderer floats above the coals. An iron pour-gutter on the front lip takes the blood of the
Projection that lights it.

The fire itself is not baked: BrazierFlames draws shader flame tongues over the coals and feeds them
with rising Flammeus licks. The ember texture is authored here, and so is the hot-cored crimson fire
strip that the Crimson Flames fire block burns as `crimson_fire`.

It can also be mounted (BrazierMount): as a sconce on a wall, the same bowl held a quarter-block lower on a
bracket arm from an iron wall plate, or hanging, held lower still on four chains from a ceiling plate, so its
talons clear the ceiling. The spout faces out from a sconce's wall.

Emits, for each mount (standing, `_wall`, `_hanging`), three block models:
- the body, drawn in every phase;
- `_lit`, the glowing ember bed shown once lit (phase 1 and 2);
- `_sanguine`, the full-bright veins shown while sanguine flames await the echo (phase 2);
and the item model, which carries the standing body.

It also writes the mount/facing/ritual-phase multipart blockstate and an editable Blockbench project.
`--preview` writes build/iron_brazier_preview.png.

Model space is the north-facing block: the spout faces the player at z = 0, and a sconce's wall is at z = 16.
Python 3 + Pillow + NumPy.
"""
import argparse
import json
import math

import numpy as np
from PIL import Image

import station_kit as kit
from station_kit import BRIGHT, Model, polar

TEXTURES = {
    'iron': 'hemomancy:block/hematic_iron_block',
    'chisel': 'hemomancy:block/chiseled_hematic_iron_block',
    'bold': 'hemomancy:block/bold_chiseled_hematic_iron_block',
    'pillar': 'hemomancy:block/hematic_iron_pillar',
    'pillartop': 'hemomancy:block/hematic_iron_pillar_top',
    'venous': 'hemomancy:block/chiseled_polished_venous_stone',
    'brick': 'hemomancy:block/polished_venous_stone_bricks',
    'vein': 'hemomancy:block/hemorrhagic_crust',
    'scab': 'hemomancy:block/blood_scorched_scab',
    'ember': 'hemomancy:block/iron_brazier_embers',
}
CAPS = {'pillar': 'pillartop'}
NAME = 'iron_brazier'

CENTRE = (8.0, 8.0)
# How far each mount holds the bowl below the standing one's, in sixteenths (BrazierMount.WALL_DROP, CEILING_DROP).
WALL_DROP, CEILING_DROP = -4.0, -6.0
CHAINS = (0, 90, 180, 270)           # the axes, between the talons; the front chain hooks into the spout channel
CHAIN_TILT = 22.5                    # chains splay out from the ceiling plate's eyelets to the rim
CHAIN_EYE, CHAIN_EYE_Y = 3.0, 14.3   # eyelet radius and height under the ceiling plate
BOWL_RADIUS, BOWL_FLOOR, BOWL_TOP = 6.0, 10.6, 12.6
RIM_TOP = 13.2
COAL_TOP = 11.3
TALONS = (45, 135, 225, 315)         # diagonals, leaving the spout and the offering's faces open
VEIN_GLYPHS = TALONS                 # bowl facets under each talon weep blood from the rim
DRIPS = ((-.3, .3, 10.9), (-1.05, -.6, 11.75), (.6, 1.05, 11.35))   # tangent span and lowest reach

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, -1.5, 0], 'scale': [.55, .55, .55]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, -1, 0], 'scale': [.5, .5, .5]},
    'head': {'rotation': [0, 0, 0], 'translation': [0, 10, 0], 'scale': [.8, .8, .8]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
}


def dais(m):
    """Octagonal iron dais, its venous-stone inlay stepped up to the stem."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('dais', 'y', c3, 4.9, 0, 1.3, 'bold')
    m.shell('dais_band', CENTRE, 5.15, .55, 1.05, .3, 'iron', ends=True)
    m.octo('dais_inlay', 'y', c3, 3.9, 1.3, 1.8, 'venous')
    m.octo('dais_step', 'y', c3, 2.6, 1.8, 2.2, 'chisel')


def stem(m):
    """Hematic iron pillar, a vein running up its front."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('stem_core', 'y', c3, 1.6, 2.2, 8.4, 'pillar')
    m.radial('stem_vein', CENTRE, 90, 1.55, 1.85, -.3, .3, 2.2, 8.4, 'vein')


def bowl(m):
    """Stepped iron bowl with a bold rim, vein glyphs on its diagonals and a coal bed inside."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('bowl_collar', 'y', c3, 2.6, 8.4, 9.1, 'bold')
    m.octo('bowl_heel', 'y', c3, 3.9, 9.1, 9.85, 'chisel')
    m.octo('bowl_floor', 'y', c3, 5.05, 9.85, BOWL_FLOOR, 'iron')
    m.shell('bowl_wall', CENTRE, BOWL_RADIUS, BOWL_FLOOR, BOWL_TOP, 1.0, 'iron', ends=True)
    m.shell('bowl_rim', CENTRE, BOWL_RADIUS + .45, BOWL_TOP, RIM_TOP, 1.35, 'bold', ends=True)
    for deg in VEIN_GLYPHS:
        for t0, t1, y0 in DRIPS:
            m.radial('bowl_drip', CENTRE, deg, BOWL_RADIUS - .05, BOWL_RADIUS + .15, t0, t1, y0, 12.6, 'vein')
    m.octo('coal_bed', 'y', c3, 5.0, BOWL_FLOOR, COAL_TOP, 'scab')
    for x, z, w, h, angle in ((6.2, 7.0, 1.6, .7, 22.5), (9.4, 6.6, 1.3, .6, -22.5), (8.4, 9.6, 1.8, .8, 0),
                              (5.6, 9.4, 1.2, .55, 45), (10.2, 9.2, 1.1, .5, 22.5)):
        m.box('coal_lump', (x - w / 2, COAL_TOP, z - w / 2), (x + w / 2, COAL_TOP + h, z + w / 2), 'scab',
              rot=('y', angle, [x, COAL_TOP, z]) if angle else None)


def talons(m):
    """Four iron talons curling in from the rim, cradling the floating offering."""
    for deg in TALONS:
        m.radial('talon_root', CENTRE, deg, 5.5, 6.75, -.65, .65, RIM_TOP - .3, 15.3, 'iron')
        m.radial('talon_knee', CENTRE, deg, 5.0, 6.3, -.55, .55, 15.1, 16.7, 'iron')
        m.radial('talon_wrist', CENTRE, deg, 4.6, 5.75, -.48, .48, 16.5, 17.7, 'bold')
        m.radial('talon_tip', CENTRE, deg, 4.5, 5.35, -.38, .38, 17.7, 18.5, 'chisel')


def spout(m):
    """Iron pour-gutter on the front lip, its channel stained with the lighting blood."""
    m.radial('spout_bracket', CENTRE, 90, 6.0, 7.1, -.5, .5, 10.9, 12.2, 'chisel')
    m.radial('spout_floor', CENTRE, 90, 5.6, 7.45, -1.1, 1.1, 12.2, 12.7, 'bold')
    for t0, t1 in ((-1.1, -.5), (.5, 1.1)):
        m.radial('spout_rail', CENTRE, 90, 5.6, 7.45, t0, t1, 12.7, 13.45, 'iron')
    m.radial('spout_lip', CENTRE, 90, 7.45, 7.95, -.75, .75, 11.7, 12.85, 'bold')
    m.radial('spout_channel', CENTRE, 90, 5.6, 7.45, -.5, .5, 12.7, 12.8, 'vein', faces=('up',))


def body():
    m = Model(caps=CAPS)
    dais(m)
    stem(m)
    bowl(m)
    talons(m)
    spout(m)
    return m


def shifted(m, dy):
    """The same elements moved dy sixteenths up (down when negative), rotation origins with them."""
    out = Model(caps=CAPS)
    for e in m.elements:
        a, b = list(e['a']), list(e['b'])
        a[1], b[1] = round(a[1] + dy, 4), round(b[1] + dy, 4)
        rot = e['rot']
        if rot:
            axis, angle, origin = rot
            origin = list(origin)
            origin[1] = round(origin[1] + dy, 4)
            rot = (axis, angle, origin)
        out.elements.append(dict(e, a=a, b=b, rot=rot))
    return out


def vessel(dy):
    """The bowl, its coals, talons and spout, held dy sixteenths below the standing brazier's."""
    m = Model(caps=CAPS)
    bowl(m)
    talons(m)
    spout(m)
    return shifted(m, dy)


def wall_mount(m):
    """An iron wall plate set with a venous panel and a bleeding vein, and a bracket arm braced up to the collar."""
    collar = 8.4 + WALL_DROP
    m.box('wall_plate', (4.5, 1.0, 15.0), (11.5, 12.0, 16.0), 'bold')
    m.box('wall_panel', (5.5, 2.0, 14.65), (10.5, 11.0, 15.0), 'venous')
    m.box('wall_vein', (7.6, 2.4, 14.5), (8.4, 10.6, 14.65), 'vein')
    for x in (4.95, 10.05):
        for y in (1.45, 10.55):
            m.box('wall_rivet', (x - .5, y - .5, 14.4), (x + .5, y + .5, 15.0), 'iron')
    m.box('bracket_arm', (7.0, collar - 1.8, 8.0), (9.0, collar, 14.5), 'pillar',
          caps={'north': 'pillartop', 'south': 'pillartop'})
    m.box('bracket_vein', (7.7, collar - 1.85, 9.0), (8.3, collar - 1.8, 14.5), 'vein', faces=('down',))
    m.octo('bracket_knuckle', 'y', (CENTRE[0], 0, CENTRE[1]), 1.9, collar - 2.2, collar, 'bold')
    # A gusset from low on the plate up under the arm; +45 about x drops its wall end.
    m.box('bracket_brace', (7.35, 1.1, 12.59), (8.65, 2.1, 15.41), 'chisel', rot=('x', 45, [8.0, 1.6, 14.0]))


def chain(m, degrees):
    """Links from an eyelet under the ceiling plate, splayed CHAIN_TILT outward, down to the bowl's rim."""
    rim = RIM_TOP + CEILING_DROP
    length = (CHAIN_EYE_Y - rim) / math.cos(math.radians(CHAIN_TILT))
    ex, ez = polar(CENTRE, degrees, CHAIN_EYE)
    quarter = round(degrees / 90) % 4
    # Positive turns about z swing a hanging line's foot toward +x, about x toward -z.
    axis, angle = (('z', CHAIN_TILT), ('x', CHAIN_TILT), ('z', -CHAIN_TILT), ('x', -CHAIN_TILT))[quarter]
    radial_x = quarter % 2 == 0
    links = 6
    step = length / links
    for i in range(links):
        y1 = CHAIN_EYE_Y - i * step + .15
        y0 = y1 - step - .3
        wide, thin = .5, .2
        # Links alternate their flat across and along the chain's swing.
        rx, rz = (thin, wide) if (i % 2 == 0) == radial_x else (wide, thin)
        m.box('chain_link', (ex - rx, y0, ez - rz), (ex + rx, y1, ez + rz), 'iron',
              rot=(axis, angle, [ex, CHAIN_EYE_Y, ez]))


def ceiling_mount(m):
    """A ceiling plate and swivel, a four-armed spider to the eyelets, and the chains down to the rim."""
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('ceiling_plate', 'y', c3, 3.8, 15.3, 16.0, 'bold')
    m.octo('ceiling_inset', 'y', c3, 2.6, 15.0, 15.3, 'venous')
    m.octo('ceiling_swivel', 'y', c3, .9, 13.9, 15.0, 'iron')
    for deg in CHAINS:
        m.radial('ceiling_spider', CENTRE, deg, .6, CHAIN_EYE + .5, -.4, .4, CHAIN_EYE_Y, 15.0, 'chisel')
        chain(m, deg)
    collar = 8.4 + CEILING_DROP
    m.octo('bowl_finial', 'y', c3, 1.2, collar - 1.4, collar, 'chisel')
    m.octo('bowl_finial_drip', 'y', c3, .5, collar - 2.0, collar - 1.4, 'vein')


def wall_body():
    m = vessel(WALL_DROP)
    wall_mount(m)
    return m


def hanging_body():
    m = vessel(CEILING_DROP)
    ceiling_mount(m)
    return m


def lit(dy=0.0):
    """Glowing embers over the coal bed; the flames above them are BrazierFlames' shader tongues."""
    m = Model(caps=CAPS)
    c3 = (CENTRE[0], 0, CENTRE[1])
    m.octo('ember_bed', 'y', c3, 4.85, COAL_TOP - .2, COAL_TOP + .2, 'ember', faces=('up',), extra=BRIGHT)
    for x, z, w, h in ((6.2, 7.0, 1.0, .8), (9.4, 6.6, .8, .7), (8.4, 9.6, 1.2, .9), (5.6, 9.4, .7, .65)):
        m.box('ember', (x - w / 2, COAL_TOP + h - .2, z - w / 2), (x + w / 2, COAL_TOP + h + .05, z + w / 2), 'ember',
              faces=('up', 'north', 'south', 'east', 'west'), extra=BRIGHT)
    return shifted(m, dy) if dy else m


def sanguine(mount='floor'):
    """Full-bright copies of every vein, a hair proud, so the iron bleeds light while the rite waits."""
    m = Model(caps=CAPS)
    if mount == 'floor':
        m.radial('stem_vein_lit', CENTRE, 90, 1.52, 1.9, -.34, .34, 2.18, 8.42, 'vein', extra=BRIGHT)
    for deg in VEIN_GLYPHS:
        for t0, t1, y0 in DRIPS:
            m.radial('bowl_drip_lit', CENTRE, deg, BOWL_RADIUS - .08, BOWL_RADIUS + .2, t0 - .04, t1 + .04, y0 - .04, 12.58,
                     'vein', extra=BRIGHT)
    m.radial('spout_channel_lit', CENTRE, 90, 5.6, 7.45, -.5, .5, 12.7, 12.87, 'vein', faces=('up',), extra=BRIGHT)
    if mount == 'wall':
        m = shifted(m, WALL_DROP)
        m.box('wall_vein_lit', (7.56, 2.36, 14.46), (8.44, 10.64, 14.62), 'vein', extra=BRIGHT)
        return m
    if mount == 'ceiling':
        m = shifted(m, CEILING_DROP)
        collar = 8.4 + CEILING_DROP
        m.octo('bowl_finial_drip_lit', 'y', (CENTRE[0], 0, CENTRE[1]), .55, collar - 2.04, collar - 1.38, 'vein',
               extra=BRIGHT)
        return m
    first = len(m.elements)
    m.shell('dais_band_lit', CENTRE, 5.25, .6, 1.0, .14, 'vein')
    for e in m.elements[first:]:
        e['extra'] = BRIGHT
    return m


# ---------------------------------------------------------------- textures

FLAME_FRAMES = 32
# Intensity thresholds from the guttering edge to the hot core; below the first is clear (cutout).
FLAME_RAMP = ((.16, (74, 0, 14)), (.32, (138, 6, 24)), (.5, (196, 22, 34)), (.68, (236, 64, 52)),
              (.84, (255, 132, 104)), (1.0, (255, 214, 196)))


def _lattice(seed, rows):
    """Value-noise lattice of 10 columns by `rows`, wrapping vertically so a scroll loops."""
    return np.random.default_rng(seed).random((rows, 10))


def _sample(lattice, x, y):
    rows, cols = lattice.shape
    x0, y0 = int(math.floor(x)), int(math.floor(y))
    fx, fy = x - x0, y - y0
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)

    def at(i, j):
        return lattice[j % rows, min(max(i, 0), cols - 1)]

    top = at(x0, y0) * (1 - fx) + at(x0 + 1, y0) * fx
    bottom = at(x0, y0 + 1) * (1 - fx) + at(x0 + 1, y0 + 1) * fx
    return top * (1 - fy) + bottom * fy


def flame_strip():
    """32-frame crimson fire for the Crimson Flames block: tongues rise through noise that wraps after one loop."""
    coarse, fine = _lattice(7, 8), _lattice(11, 16)
    strip = Image.new('RGBA', (16, 16 * FLAME_FRAMES), (0, 0, 0, 0))
    px = strip.load()
    for frame in range(FLAME_FRAMES):
        t = frame / FLAME_FRAMES
        for y in range(16):
            rise = 15 - y                                   # 0 at the coal bed
            for x in range(16):
                n = (.65 * _sample(coarse, x / 2, (rise / 4 - t * 8) % 8)
                     + .35 * _sample(fine, x / 2, (rise / 2 - t * 16) % 16))
                v = 1.2 - rise / 17 - abs(x - 7.5) / 9.5 + (n - .5) * 1.1
                if v < FLAME_RAMP[0][0]:
                    continue
                colour = [c for edge, c in FLAME_RAMP if v >= edge][-1]
                px[x, frame * 16 + y] = colour + (255,)
    return strip


def ember_texture():
    """Black coals split by glowing crimson cracks with a few white-hot seams."""
    lattice = _lattice(23, 16)
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            n = _sample(lattice, x / 3.2 + (y % 3) * .1, y / 3.2)
            crack = abs(n - .5)
            if crack < .025:
                colour = (255, 168, 140)
            elif crack < .07:
                colour = (226, 46, 40)
            elif crack < .13:
                colour = (128, 8, 20)
            else:
                colour = (36 + int(n * 18), 6, 10)
            px[x, y] = colour + (255,)
    return image


def write_textures():
    folder = kit.ASSETS / 'textures/block'
    flame_strip().save(folder / 'crimson_fire.png')
    meta = {'animation': {'frametime': 2}}
    (folder / 'crimson_fire.png.mcmeta').write_text(json.dumps(meta, indent=2) + '\n', encoding='utf-8')
    ember_texture().save(folder / 'iron_brazier_embers.png')
    for stale in ('iron_brazier_flame.png', 'iron_brazier_flame.png.mcmeta'):
        if (folder / stale).exists():
            (folder / stale).unlink()


MOUNTS = (('floor', ''), ('wall', '_wall'), ('ceiling', '_hanging'))


def blockstate():
    """Per mount and facing: the body in every phase, lit embers in phases 1 and 2, sanguine veins only in phase 2."""
    parts = []
    for face, suffix in MOUNTS:
        for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            for model, phase in ((NAME + suffix, None), (NAME + suffix + '_lit', '1|2'),
                                 (NAME + suffix + '_sanguine', '2')):
                apply = {'model': 'hemomancy:block/' + model}
                if angle:
                    apply['y'] = angle
                when = {'face': face, 'facing': facing}
                if phase:
                    when['ritual_phase'] = phase
                parts.append({'apply': apply, 'when': when})
    return {'multipart': parts}


def export():
    write_textures()
    solid, flames, veins = body(), lit(), sanguine()
    sconce, sconce_flames, sconce_veins = wall_body(), lit(WALL_DROP), sanguine('wall')
    hung, hung_flames, hung_veins = hanging_body(), lit(CEILING_DROP), sanguine('ceiling')
    kit.separate([solid], TEXTURES, set(), 'Iron Brazier')
    kit.separate([sconce], TEXTURES, set(), 'Iron Brazier sconce')
    kit.separate([hung], TEXTURES, set(), 'Iron Brazier hanging')
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_lit.json'), kit.to_json(flames, TEXTURES, 'iron'))
    kit.write_json(kit.MODELS / (NAME + '_sanguine.json'), kit.to_json(veins, TEXTURES, 'iron'))
    for suffix, (b, f, v) in (('_wall', (sconce, sconce_flames, sconce_veins)),
                              ('_hanging', (hung, hung_flames, hung_veins))):
        kit.write_json(kit.MODELS / (NAME + suffix + '.json'), kit.to_json(b, TEXTURES, 'iron'))
        kit.write_json(kit.MODELS / (NAME + suffix + '_lit.json'), kit.to_json(f, TEXTURES, 'iron'))
        kit.write_json(kit.MODELS / (NAME + suffix + '_sanguine.json'), kit.to_json(v, TEXTURES, 'iron'))
    kit.write_json(kit.ASSETS / 'models/item' / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'iron', DISPLAY))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    project = kit.bbmodel(NAME, [('brazier', solid), ('lit', flames), ('sanguine', veins), ('sconce', sconce),
                                 ('hanging', hung)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    stale = kit.BBMODEL / 'brazier.bbmodel'
    if stale.exists():
        stale.unlink()
    return solid


def preview():
    solid, flames, veins = body(), lit(), sanguine()
    unlit = [(solid, kit.identity)]
    burning = unlit + [(flames, kit.identity)]
    waiting = burning + [(veins, kit.identity)]
    sconce = [(wall_body(), kit.identity), (lit(WALL_DROP), kit.identity)]
    hung = [(hanging_body(), kit.identity), (lit(CEILING_DROP), kit.identity)]
    panels = [('Unlit', 200, 18, unlit), ('Lit', 235, 26, burning),
              ('Sanguine', 20, 22, waiting), ('Above', 210, 62, waiting),
              ('Sconce', 215, 16, sconce), ('Sconce side', 270, 8, sconce),
              ('Hanging', 215, 10, hung), ('Hanging below', 200, -40, hung)]
    return kit.preview(panels, 4, kit.ROOT / 'build/iron_brazier_preview.png', (400, 460), 17, (8, 9, 8), TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported Iron Brazier: %d elements' % len(model.elements))
    if args.preview:
        print(preview())
