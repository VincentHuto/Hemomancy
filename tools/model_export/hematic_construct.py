"""The Hematic Construct: Hemorath's Downfall wave mob and the Blood Trial Altar's guard, a body of hematic iron.

Built on the Court's jointed body (court_humanoid.humanoid: hips, body, head, elbow and knee joints with fillers),
then armoured: a visored helm with one blood slit and a short crest, a riveted breastplate and back plate over a
core of congealed blood that shows as glowing seams between the plates, heavy pauldrons, gauntlets that end in
crude iron fists, a plated skirt of hanging tassets, and greaves with hobnailed soles. No face: the helm is the
head. Blood pools in the joints and glows at the seams, the visor and the fists.

One texture pixel per model unit (16 px per block), thin panels for plate, no copper, brass or bronze: hematic
iron, blood and bone only.

    python tools/model_export/hematic_construct.py [--preview]

Writes:
  src/main/java/.../client/model/entity/mob/monster/HematicConstructModel.java   (+ hematic_construct_anim.java)
  assets/hemomancy/textures/entity/hematic_construct/hematic_construct.png      (+ hematic_construct_glow.png)
  assets/hemomancy/models/entity/bbmodel/HematicConstructModel.bbmodel
  build/mob-preview/hematic_construct.png                                        (--preview)
"""
import argparse
import math
from pathlib import Path

from court_humanoid import ROOT, ASSETS, add, bbmodel, check_rig, finish, h, humanoid, mix, smooth
from annetta_kit import entity_java_source, layout, paint_atlases, preview_sheet, quads

MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster'
TEXTURE_DIR = ASSETS / 'textures/entity/hematic_construct'
BBMODEL_PATH = ASSETS / 'models/entity/bbmodel/HematicConstructModel.bbmodel'
PREVIEW_PATH = ROOT / 'build/mob-preview/hematic_construct.png'
ATLAS_WIDTH = 128

IRON = (74, 58, 62)             # hematic iron: a dark grey with the red of old blood in it
IRON_LIGHT = (104, 84, 88)
IRON_DARK = (46, 34, 38)
IRON_EDGE = (132, 108, 110)     # worn bright along plate edges
RUST = (112, 44, 36)
CLOT = (72, 12, 18)             # the congealed core, dull
BLOOD = (160, 22, 32)
BLOOD_BRIGHT = (222, 48, 54)
BONE = (196, 184, 160)
RIVET = (150, 126, 124)
GLOW = (236, 60, 56)
GLOW_DIM = (150, 28, 30)


# ---------------------------------------------------------------- rig

def rig():
    r = humanoid('hematic_construct', 'HematicConstructModel')
    r.name = r.layer = 'hematic_construct'

    # The helm: a closed casque a little larger than the skull, the visor slit across the front, a crest of three
    # iron fins along the crown, and a gorget ring at the neck.
    r.part('helm', 'head')
    r.cube('helm', (-4.6, -8.6, -4.6, 9.2, 0.8, 9.2), 'plate', crown=True)
    r.cube('helm', (-4.6, -7.8, -4.6, 0.6, 7.2, 9.2), 'plate')
    r.cube('helm', (4.0, -7.8, -4.6, 0.6, 7.2, 9.2), 'plate')
    r.cube('helm', (-4.0, -7.8, 4.0, 8.0, 7.2, 0.6), 'plate')
    r.cube('helm', (-4.0, -7.8, -4.6, 8.0, 2.6, 0.6), 'plate', brow=True)
    r.cube('helm', (-4.0, -3.8, -4.6, 8.0, 3.2, 0.6), 'plate', chin=True)
    r.cube('helm', (-3.6, -5.2, -4.5, 7.2, 1.4, 0.4), 'visor')
    for i, x in enumerate((-2.4, -0.4, 1.6)):
        r.cube('helm', (x, -10.0 + (0.6 if i != 1 else 0.0), -1.5, 0.8, 1.6, 4.0), 'fin')
    r.cube('helm', (-4.3, -0.8, -4.3, 8.6, 1.2, 8.6), 'gorget')

    # The breastplate and back plate are thin panels over the blood core; the core shows as seams between them.
    r.part('plates', 'body', (0, -10, 0))
    r.cube('plates', (-4.4, 0.2, -2.5, 3.9, 5.4, 0.5), 'plate', rivets=True)
    r.cube('plates', (0.5, 0.2, -2.5, 3.9, 5.4, 0.5), 'plate', rivets=True)
    r.cube('plates', (-4.4, 5.9, -2.5, 8.8, 4.3, 0.5), 'plate', rivets=True)
    r.cube('plates', (-4.4, 0.2, 2.0, 8.8, 10.0, 0.5), 'plate', rivets=True)
    r.cube('plates', (-4.5, 0.2, -2.0, 0.5, 10.0, 4.0), 'plate')
    r.cube('plates', (4.0, 0.2, -2.0, 0.5, 10.0, 4.0), 'plate')
    # The seams: the core glowing between and around the plates.
    r.cube('plates', (-0.5, 0.0, -2.45, 1.0, 5.8, 0.4), 'seam')
    r.cube('plates', (-4.4, 5.6, -2.45, 8.8, 0.4, 0.4), 'seam')
    r.cube('plates', (-4.4, -0.2, -2.45, 8.8, 0.5, 0.4), 'seam')
    r.cube('plates', (-4.4, -0.2, 2.05, 8.8, 0.5, 0.4), 'seam')

    # The skirt of tassets hanging from the belt, and the belt itself.
    r.cube('hips', (-4.4, -3.4, -2.4, 8.8, 1.4, 4.8), 'belt')
    r.part('tassets', 'hips')
    for x in (-4.3, -1.6, 1.1):
        r.cube('tassets', (x, -2.0, -2.6, 2.7 if x < 1.0 else 3.2, 5.2, 0.5), 'plate', tasset=True)
    r.cube('tassets', (-4.3, -2.0, 2.1, 8.6, 5.6, 0.5), 'plate', tasset=True)
    r.cube('tassets', (-4.6, -2.0, -2.0, 0.5, 4.6, 4.0), 'plate', tasset=True)
    r.cube('tassets', (4.1, -2.0, -2.0, 0.5, 4.6, 4.0), 'plate', tasset=True)

    for side, sign in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        x0 = -3.4 if sign < 0 else -1.4
        # A heavy pauldron over the shoulder, stepped in two plates, and a seam of blood under its lip.
        r.cube(arm, (x0 - 0.4, -2.9, -2.8, 5.6, 1.4, 5.6), 'plate', pauldron=True)
        r.cube(arm, (x0 - 0.2, -1.6, -2.6, 5.2, 1.6, 5.2), 'plate', pauldron=True)
        r.cube(arm, (x0, 0.0, -2.4, 4.8, 0.5, 4.8), 'seam')
        # Vambrace plates on the upper arm's outer face.
        r.cube(arm, (x0 - 0.5 if sign < 0 else x0 + 4.0, 0.4, -1.8, 0.5, 4.2, 3.6), 'plate')
        # The gauntlet: a plated cuff and a fist of iron with the blood core in the knuckles.
        r.cube(fore, (-2.4, 0.4, -2.4, 4.8, 2.2, 4.8), 'plate', cuff=True)
        r.cube(fore, (-2.3, 5.6, -2.3, 4.6, 2.8, 4.6), 'fist')
        r.cube(fore, (-2.0, 8.2, -2.0, 4.0, 0.6, 4.0), 'seam')
        leg, shin = f'{side}_leg', f'{side}_shin'
        # Cuisse plate on the thigh and a greave with a hobnailed sole.
        r.cube(leg, (-2.4, 0.2, -2.5, 4.8, 5.0, 0.6), 'plate', rivets=True)
        r.cube(shin, (-2.3, 0.4, -2.5, 4.6, 5.0, 0.6), 'plate', rivets=True)
        r.cube(shin, (-2.4, 4.6, -2.4, 4.8, 1.8, 4.8), 'plate', cuff=True)
        r.cube(shin, (-2.5, 5.8, -2.6, 5.0, 0.4, 5.2), 'sole')
    return r


# ---------------------------------------------------------------- paint

def column(t):
    return math.floor(t['gz'] if t['face'] in ('east', 'west') else t['gx'])


def plate(cube, t, base=IRON):
    """Hammered hematic iron: a worn bright edge, hammer marks, rust blooming in the low noise, and rivet heads."""
    rgb = base
    marks = smooth(t['gx'], t['gy'], t['gz'], 1.6, 3)
    rgb = mix(rgb, IRON_LIGHT, 0.5 * max(0.0, marks - 0.5))
    rgb = mix(rgb, IRON_DARK, 0.5 * max(0.0, 0.42 - marks))
    rust = smooth(t['gx'] + 7.0, t['gy'], t['gz'], 1.3, 9)
    if rust > 0.68:
        rgb = mix(rgb, RUST, min(1.0, (rust - 0.68) * 4.0))
    if t['side'] and (t['fv'] < 0.18 or t['u'] == 0 or t['u'] == t['fw'] - 1):
        rgb = mix(rgb, IRON_EDGE, 0.55)                 # the edge of a plate, worn bright
    if cube.get('rivets') and t['side'] and t['u'] % 3 == 1 and t['v'] % 4 == 1:
        return RIVET if (t['u'] + t['v']) % 2 else add(RIVET, -24)
    if cube.get('tasset') and t['side'] and t['fv'] > 0.86:
        rgb = mix(rgb, CLOT, 0.5)                       # blood dried along the hanging edge
    if cube.get('crown') and t['face'] == 'down':
        rgb = mix(rgb, IRON_DARK, 0.25 if (t['u'] + t['v']) % 2 else 0.0)
    return rgb


def core(t, bright=BLOOD, dim=CLOT):
    """The congealed core: dull clot with brighter blood where it still runs."""
    run = smooth(t['gx'], t['gy'] * 0.5, t['gz'], 1.2, 5)
    return mix(dim, bright, max(0.0, run - 0.35) * 1.4)


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        return plate(cube, t, IRON_DARK)              # the skull under the helm, hidden
    if m in ('torso', 'cloth', 'arm', 'leg'):
        # The body between the plates is the core, wrapped in a few iron bands.
        band = math.floor(t['ly'] + 100.0) % 4 == 0 and t['side']
        if band:
            return plate(cube, t)
        rgb = core(t)
        if t['side'] and t['n'] > 0.78:
            rgb = mix(rgb, BLOOD_BRIGHT, 0.5)
        return rgb
    if m == 'plate':
        return plate(cube, t)
    if m == 'belt':
        rgb = plate(cube, t, IRON_DARK)
        if t['side'] and t['fv'] > 0.35 and t['fv'] < 0.65 and t['u'] % 4 == 0:
            return RIVET
        return rgb
    if m == 'gorget':
        return plate(cube, t, IRON_LIGHT) if t['side'] and t['fv'] < 0.5 else plate(cube, t, IRON_DARK)
    if m == 'visor':
        return (28, 8, 12) if t['face'] == 'north' else plate(cube, t, IRON_DARK)
    if m == 'fin':
        return mix(plate(cube, t), IRON_EDGE, 0.35 if t['face'] == 'down' else 0.0)
    if m == 'fist':
        rgb = plate(cube, t, IRON_DARK)
        if t['face'] == 'north' and t['v'] % 2 == 0:
            rgb = add(rgb, 18)                        # the knuckles
        return rgb
    if m == 'sole':
        return (36, 26, 28) if (t['u'] + t['v']) % 3 else RIVET
    if m == 'seam':
        return core(t, BLOOD_BRIGHT, BLOOD)
    return IRON


def glow(cube, t):
    m = cube['material']
    if m == 'visor' and t['face'] == 'north':
        return GLOW if 0.15 < t['fu'] < 0.85 else GLOW_DIM
    if m == 'seam':
        run = smooth(t['gx'], t['gy'] * 0.5, t['gz'], 1.2, 5)
        return GLOW if run > 0.55 else GLOW_DIM
    if m == 'fist' and t['face'] == 'north' and t['v'] % 2 == 1:
        return GLOW_DIM
    if m in ('torso', 'cloth', 'arm', 'leg') and not (math.floor(t['ly'] + 100.0) % 4 == 0 and t['side']):
        if smooth(t['gx'], t['gy'] * 0.5, t['gz'], 1.2, 5) > 0.72:
            return GLOW_DIM
    return None


# ---------------------------------------------------------------- write

POSES = [('rest', {}, ()),
         ('stomp', {'body': (6, 0, 0), 'right_arm': (-34, 0, 8), 'left_arm': (28, 0, -8), 'right_forearm': (-20, 0, 0),
                    'left_forearm': (-20, 0, 0), 'right_leg': (-28, 0, 0), 'left_leg': (24, 0, 0),
                    'left_shin': (40, 0, 0)}, ()),
         ('slam', {'body': (18, 0, 0), 'right_arm': (-160, 0, 12), 'right_forearm': (-40, 0, 0),
                   'left_arm': (-150, 0, -12), 'left_forearm': (-40, 0, 0)}, ())]


def write(r, preview=False):
    check_rig(r)
    size = layout(r, ATLAS_WIDTH)
    painters = [lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(paint(c, t)),
                lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(glow(c, t))]
    base, emissive = paint_atlases(r, size, painters)
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'hematic_construct.png')
    emissive.save(TEXTURE_DIR / 'hematic_construct_glow.png')
    anim = Path(__file__).with_name('hematic_construct_anim.java').read_text(encoding='utf-8')
    source = entity_java_source(
        r, size, 'HematicConstructEntity', 'hematic_construct.py', extra_methods=anim,
        extra_imports='import com.vincenthuto.hemomancy.common.util.HemoEasing;\n',
        package='com.vincenthuto.hemomancy.client.model.entity.mob.monster',
        entity_package='com.vincenthuto.hemomancy.common.entity.mob.monster')
    for unused in ('import com.mojang.blaze3d.vertex.PoseStack;\n', 'import net.minecraft.world.entity.HumanoidArm;\n'):
        source = source.replace(unused, '')
    source = source.replace('/** Generated by tools/model_export/hematic_construct.py; edit the rig there and re-run it. */',
                            '/** Generated by tools/model_export/hematic_construct.py on the court_humanoid.py rig; edit the '
                            'generator (and hematic_construct_anim.java) and re-run it. */')
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    (MODEL_DIR / 'HematicConstructModel.java').write_text(source, encoding='utf-8')
    bbmodel(r, size, TEXTURE_DIR / 'hematic_construct.png', BBMODEL_PATH)
    print(f'hematic_construct: {sum(len(p["cubes"]) for p in r.parts)} cubes, {len(r.parts)} parts, '
          f'atlas {size[0]}x{size[1]}, glow')
    if preview:
        tiles = []
        for _, overrides, hidden in POSES:
            q = quads(r, hidden=hidden, overrides=overrides)
            tiles += [(q, base, 0.0, 0.1), (q, base, math.pi * 0.75, 0.15)]
        preview_sheet(tiles, PREVIEW_PATH)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), args.preview)


if __name__ == '__main__':
    main()
