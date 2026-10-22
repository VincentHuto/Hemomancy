"""Annetta Knowles: the high cleric's body shared by her cowering, Phase 1 and purged looks.

Emits AnnettaKnowlesModel.java and three 2x painted textures:
  annetta_knowles.png         long red hair, gold irises, hem dirtied (cowering / Phase 1)
  annetta_knowles_purged.png  the same vestments cleaned, short white hair, blue irises
  annetta_knowles_eyes.png    gold irises only, rendered emissive while she is unpurged

    python tools/model_export/annetta_knowles.py            # write model + textures
    python tools/model_export/annetta_knowles.py --preview  # also write build/annetta_knowles_preview.png

Then regenerate the Blockbench project:
    node tools/model_export/java_model_to_bbmodel.mjs

Palette: white robes with pale-silver thread, consecrated copper gone to verdigris. Her red hair and gold eyes are the
only things wrong with her. The Phase 1 tick (a swollen Tooth Peck) is the one infected thing on this body.
"""
import argparse
from pathlib import Path
import math

from annetta_kit import (MODEL_DIR, ROOT, TEXTURE_DIR, Rig, add, entity_java_source, flesh, infected_ichor, layout,
                         mix, paint_atlases, preview_sheet, quads, robe_white, verdigris)

import annetta_kit

# Block texel density: she renders at 1:1, so one texture pixel per model unit is 16 px per block, like the mod's
# blocks and items. The UV layout (and so the Java model) does not depend on it.
TEXEL_SCALE = 1
annetta_kit.SCALE = TEXEL_SCALE

JAVA = MODEL_DIR / 'AnnettaKnowlesModel.java'
ATLAS_WIDTH = 128


# ---------------------------------------------------------------- rig

def braid(r, name, parent, pivot, rotation, segments, width):
    """A plait: short segments whose crossings alternate side to side, tapering to a tied end."""
    r.part(name, parent, pivot, rotation)
    for i in range(segments):
        w = width * (1.0 - 0.35 * i / segments)
        shift = (0.22 if i % 2 else -0.22) * w
        r.cube(name, (-w / 2 + shift, i * 1.25, -w / 2, w, 1.45, w), 'hair', lock='braid')
    r.cube(name, (-0.3 * width, segments * 1.25, -0.3 * width, 0.6 * width, 0.5, 0.6 * width), 'braid_tie')
    end = segments * 1.25 + 0.5
    r.cube(name, (-0.35 * width, end, -0.35 * width, 0.7 * width, 1.6, 0.7 * width), 'hair', lock='fall')


def annetta():
    """Standard humanoid boxes, as the Acolyte uses; the vestments are thin panels, not inflated shells.

    The hierarchy bends where a body bends: root -> hips (hip joints, y 12) -> body (waist, y 10) -> head and arms,
    and hips -> legs and skirt. A lean or twist pivots at the waist and carries the shoulders and head with it, so the
    neck, shoulders and hips never part. Each lower limb segment has an inset filler reaching back into its joint, so
    a bent knee or elbow closes over instead of opening a gap.
    """
    r = Rig('annetta_knowles', 'AnnettaKnowlesModel', 'annetta_knowles')

    # Pelvis and belt ride the hips; the torso bends behind the belt, so the waist crease is always covered.
    r.part('hips', 'root', (0, 12, 0))
    r.cube('hips', (-4.15, -3.0, -2.15, 8.3, 3.0, 4.3), 'robe')
    r.cube('hips', (-4.35, -3.7, -2.35, 8.7, 1.1, 4.7), 'belt')
    r.cube('hips', (3.9, -3.0, -2.6, 0.4, 4.5, 0.4), 'chain')
    r.cube('hips', (3.6, 1.3, -2.9, 1.0, 1.4, 1.0), 'copper')

    r.part('body', 'hips', (0, -2, 0))
    r.cube('body', (-4.0, -10.0, -2.0, 8.0, 11.0, 4.0), 'robe')
    r.cube('body', (-1.0, -9.5, -2.3, 2.0, 7.5, 0.3), 'trim')

    # Short capelet one pixel proud of the shoulders, closed by the copper crescent.
    r.part('mantle', 'body', (0, -10, 0))
    r.cube('mantle', (-4.5, -0.3, -2.5, 9.0, 3.5, 5.0), 'mantle')
    r.cube('mantle', (-0.75, 0.6, -2.85, 1.5, 1.5, 0.4), 'clasp')

    # High collar, torn open on her left where the Phase 1 tick latches.
    r.part('collar', 'body', (0, -10, 0))
    r.cube('collar', (-3.0, -1.5, 1.8, 6.0, 1.8, 0.6), 'collar')
    r.cube('collar', (-3.6, -1.5, -2.2, 0.6, 1.8, 4.0), 'collar')
    r.cube('collar', (-3.0, -1.5, -2.6, 2.4, 1.8, 0.6), 'collar')
    r.cube('collar', (3.0, -0.6, -2.2, 0.6, 0.9, 4.0), 'collar')

    # What the burst leaves: a torn stump round the spine's white nub, shown only once she is headless.
    r.part('neck_stump', 'body', (0, -10, 0))
    r.cube('neck_stump', (-2.6, -0.9, -1.9, 5.2, 1.0, 3.8), 'stump')
    r.cube('neck_stump', (-1.6, -1.5, -1.2, 3.2, 0.7, 2.4), 'stump')
    r.cube('neck_stump', (-0.6, -2.1, 0.2, 1.2, 1.3, 1.2), 'stump_bone')

    # Robe skirt as hanging cloth panels over ordinary legs, pivoting at the hips like the thighs do: the side panels
    # hang plumb, and the front and back panels are their own parts so each can swing with the thigh pushing into it.
    r.part('robe_skirt', 'hips')
    r.cube('robe_skirt', (-4.3, 0.0, -2.2, 0.5, 9.5, 4.4), 'robe')
    r.cube('robe_skirt', (3.8, 0.0, -2.2, 0.5, 9.5, 4.4), 'robe')
    r.part('skirt_front', 'robe_skirt')
    r.cube('skirt_front', (-4.0, 0.0, -2.45, 8.0, 10.0, 0.5), 'robe')
    r.cube('skirt_front', (-4.0, 9.4, -2.6, 8.0, 0.6, 0.3), 'trim')
    r.part('skirt_back', 'robe_skirt')
    r.cube('skirt_back', (-4.0, 0.0, 1.95, 8.0, 10.5, 0.5), 'robe')

    r.part('head', 'body', (0, -10, 0))
    r.cube('head', (-4.0, -8.0, -4.0, 8.0, 8.0, 8.0), 'face')

    # Hair is a hat-style overlay painted with alpha, with volume pieces over it.
    # Unpurged: long, almost braided: a thick plait down her back and two thinner braids forward over her shoulders.
    r.part('long_hair', 'head')
    r.cube('long_hair', (-4.25, -8.25, -4.25, 8.5, 8.5, 8.5), 'hair_cap', length='long')
    r.cube('long_hair', (-4.6, -8.9, -4.4, 9.2, 1.4, 9.0), 'hair')
    r.cube('long_hair', (-4.75, -7.6, -3.6, 0.6, 6.0, 7.6), 'hair')
    r.cube('long_hair', (4.15, -7.6, -3.6, 0.6, 6.0, 7.6), 'hair')
    r.cube('long_hair', (-4.4, -7.6, 3.9, 8.8, 7.0, 0.8), 'hair', lock='fall')
    braid(r, 'back_braid', 'long_hair', (0.0, -0.8, 3.7), (-4, 0, 0), 9, 2.0)
    braid(r, 'right_braid', 'long_hair', (-3.9, -2.2, -2.6), (-8, 0, 6), 6, 1.3)
    braid(r, 'left_braid', 'long_hair', (3.9, -2.2, -2.6), (-8, 0, -6), 6, 1.3)
    # Purged: a full pixie-bob, crown lifted, fringe swept to one side, sides flaring to the jaw.
    r.part('short_hair', 'head')
    r.cube('short_hair', (-4.25, -8.25, -4.25, 8.5, 8.5, 8.5), 'hair_cap', length='short')
    r.cube('short_hair', (-4.6, -9.1, -4.5, 9.2, 1.6, 9.2), 'hair')
    r.cube('short_hair', (-4.7, -8.0, -4.7, 5.2, 2.0, 0.7), 'hair', lock='fringe')
    r.cube('short_hair', (0.5, -8.0, -4.6, 4.0, 1.2, 0.6), 'hair')
    r.cube('short_hair', (-4.85, -7.6, -3.9, 0.8, 6.8, 7.8), 'hair', lock='bob')
    r.cube('short_hair', (4.05, -7.6, -3.9, 0.8, 6.8, 7.8), 'hair', lock='bob')
    r.cube('short_hair', (-4.5, -7.6, 3.95, 9.0, 6.4, 0.8), 'hair', lock='bob')

    # A Tooth Peck swollen into a tick, biting the side of her neck under the jaw; the sac points outward.
    r.part('neck_tick', 'body', (4.3, -10.6, -0.8), (0, 75, -20))
    r.cube('neck_tick', (-0.6, -0.5, -1.4, 1.2, 1.0, 1.5), 'tick_head')
    r.cube('neck_tick', (-0.3, -0.3, -2.0, 0.6, 0.6, 0.7), 'tick_mouth')
    for i in range(3):
        z = -1.1 + i * 0.5
        r.cube('neck_tick', (0.55, -0.1, z, 1.1, 0.2, 0.2), 'tick_leg')
        r.cube('neck_tick', (-1.65, -0.1, z, 1.1, 0.2, 0.2), 'tick_leg')
    r.part('tick_abdomen', 'neck_tick', (0, 0, 0.1))
    r.cube('tick_abdomen', (-1.0, -0.9, 0.0, 2.0, 1.8, 2.4), 'tick_sac')
    r.cube('tick_abdomen', (-1.2, -0.7, 0.4, 2.4, 1.4, 1.6), 'tick_sac')
    r.cube('tick_abdomen', (-0.8, -1.1, 0.4, 1.6, 2.2, 1.6), 'tick_sac')

    # Jointed limbs: elbows four pixels below the shoulder pivot, knees halfway down the leg. The shoulders hang from
    # the torso, so a lean carries them.
    r.part('right_arm', 'body', (-5, -8, 0))
    r.cube('right_arm', (-2.0, -2.0, -2.0, 3.0, 6.2, 4.0), 'sleeve', yoff=0.0)
    r.part('right_forearm', 'right_arm', (-0.5, 4.0, 0))
    r.cube('right_forearm', (-1.5, 0.0, -2.0, 3.0, 6.0, 4.0), 'sleeve', yoff=4.0)
    r.cube('right_forearm', (-1.4, -1.2, -1.9, 2.8, 1.3, 3.8), 'sleeve', yoff=4.0)
    r.part('left_arm', 'body', (5, -8, 0))
    r.cube('left_arm', (-1.0, -2.0, -2.0, 3.0, 6.2, 4.0), 'sleeve', yoff=0.0)
    r.part('left_forearm', 'left_arm', (0.5, 4.0, 0))
    r.cube('left_forearm', (-1.5, 0.0, -2.0, 3.0, 6.0, 4.0), 'sleeve', yoff=4.0)
    r.cube('left_forearm', (-1.4, -1.2, -1.9, 2.8, 1.3, 3.8), 'sleeve', yoff=4.0)

    r.part('right_leg', 'hips', (-1.9, 0, 0))
    r.cube('right_leg', (-1.9, 0.0, -2.0, 3.8, 6.2, 4.0), 'hose')
    r.part('right_shin', 'right_leg', (0, 6.0, 0))
    r.cube('right_shin', (-1.9, 0.0, -2.0, 3.8, 6.0, 4.0), 'hose')
    r.cube('right_shin', (-1.8, -1.2, -1.9, 3.6, 1.3, 3.8), 'hose')
    r.part('left_leg', 'hips', (1.9, 0, 0))
    r.cube('left_leg', (-1.9, 0.0, -2.0, 3.8, 6.2, 4.0), 'hose')
    r.part('left_shin', 'left_leg', (0, 6.0, 0))
    r.cube('left_shin', (-1.9, 0.0, -2.0, 3.8, 6.0, 4.0), 'hose')
    r.cube('left_shin', (-1.8, -1.2, -1.9, 3.6, 1.3, 3.8), 'hose')
    return r


# ---------------------------------------------------------------- paint

HAIR = dict(red=((168, 40, 30), (118, 22, 18), (206, 74, 50)),
            white=((236, 236, 232), (204, 208, 214), (250, 250, 248)))
IRIS = dict(gold=(232, 178, 48), blue=(126, 168, 214))


# Front of the head on the 16x16 (2x) pixel grid, px left to right, py top to bottom; mirrored per eye.
LASH = (58, 30, 32)
BLUSH = (226, 166, 160)


def face_pixel(px, py):
    """Feature at a face pixel, or None for plain skin. The right half mirrors the left."""
    side = px if px < 8 else 15 - px
    if py == 5 and 3 <= side <= 5 or py == 6 and side == 2:
        return 'brow'                 # thin, arched
    if py == 7 and 2 <= side <= 5 or py == 6 and side == 1:
        return 'lash'                 # upper lash line, flicked outward
    if py in (8, 9) and 3 <= side <= 6:
        if side == 6 or side == 3:
            return 'white'
        if (side, py) == ((4 if px < 8 else 5), 8):
            return 'catchlight'       # the same light falls on both eyes
        return 'iris' if py == 8 else 'iris_low'
    if py == 10 and side == 3:
        return 'lower_lash'
    if py == 11 and 2 <= side <= 3:
        return 'blush'
    if px == 8 and py == 10:
        return 'nose'
    if py == 12 and 6 <= side <= 7:
        return 'lip'
    if py == 13 and side == 7:
        return 'lip_low'
    return None


def face(t, variant):
    rgb = flesh(t, (232, 210, 198))
    if t['face'] != 'north':
        return rgb
    px, py = min(15, int((t['lx'] + 4) * 2)), min(15, int((t['ly'] + 8) * 2))
    feature = face_pixel(px, py)
    iris = IRIS[variant['iris']]
    return {
        'brow': HAIR[variant['hair']][1],
        'lash': LASH,
        'white': (240, 236, 232),
        'catchlight': (252, 250, 244),
        'iris': iris,
        'iris_low': add(iris, -26),
        'lower_lash': mix(rgb, LASH, 0.45),
        'blush': mix(rgb, BLUSH, 0.45),
        'nose': mix(rgb, (190, 150, 142), 0.45),
        'lip': (190, 98, 108),
        'lip_low': (212, 132, 134),
    }.get(feature, rgb)


def hair(cube, t, variant):
    base, dark, light = HAIR[variant['hair']]
    if cube.get('lock') == 'braid':
        cross = (t['u'] + t['v']) % 4
        return light if cross == 0 else dark if t['v'] in (0, t['fh'] - 1) or cross == 2 else base
    k = (t['u'] + (t['v'] // 5 if cube.get('lock') else 0)) % 5
    rgb = dark if k == 0 else light if k == 3 else base
    if cube.get('lock') and t['side'] and t['fv'] > 0.9:
        rgb = dark                    # loose ends
    return rgb


def hair_cap(cube, t, variant):
    """Hat-layer hair: opaque only where hair covers the head, so the face and neck show through."""
    long = cube['length'] == 'long'
    face, x, y = t['face'], t['lx'], t['ly']
    if face == 'up':
        return None                   # model y points down, so 'up' is the underside, under her chin
    if face == 'north':
        covered = y < -6.5 or (long and abs(x) > 3.4 and y < -1.0)
    elif face in ('east', 'west'):
        covered = y < (-1.5 if long else -4.5) or (not long and y < -3.0 and t['fu'] > 0.5)
    elif face == 'south':
        covered = long or y < -3.0
    else:
        covered = True
    return hair(cube, t, variant) + (255,) if covered else None


def dirt(rgb, t, variant):
    if variant['dirty'] and t['gy'] > 17.5:
        return mix(rgb, (148, 136, 116), min(0.55, (t['gy'] - 17.5) * 0.12 + t['n'] * 0.2))
    return rgb


def copper(t):
    return verdigris(t) if t['n'] < 0.55 else add((176, 112, 70), (t['n'] - 0.75) * 30)


def tick(cube, t):
    m = cube['material']
    if m == 'tick_mouth':
        return (212, 200, 170) if (t['u'] + t['v']) % 2 else (150, 132, 104)
    if m == 'tick_leg':
        return (74, 44, 40)
    if m == 'tick_head':
        return mix((92, 50, 42), (58, 30, 28), t['n'])
    # Sac: stretched pale skin, infected yellow-orange pooling under it, dark mottling.
    skin = mix((196, 176, 160), (170, 146, 132), t['n'])
    vein = abs(math.sin(t['lx'] * 2.3 + t['lz'] * 1.7)) < 0.1 or abs(math.sin(t['ly'] * 1.9 - t['lz'] * 1.3)) < 0.08
    if vein:
        return infected_ichor(t)
    if t['n'] > 0.82:
        return (96, 58, 52)
    return mix(skin, infected_ichor(t), 0.25 + 0.2 * t['fv'])


def paint(cube, t, variant):
    m = cube['material']
    if m == 'face':
        return face(t, variant) + (255,)
    if m == 'skin':
        return flesh(t, (226, 204, 190)) + (255,)
    if m == 'hair':
        return hair(cube, t, variant) + (255,)
    if m == 'braid_tie':
        return copper(t) + (255,)     # a twist of consecrated copper wire
    if m == 'hair_cap':
        return hair_cap(cube, t, variant)
    if m == 'sleeve':
        arm_y = t['ly'] + cube.get('yoff', 0.0)   # position down the whole arm, across both segments
        if arm_y >= 8.5:
            return flesh(t, (226, 204, 190)) + (255,)
        if arm_y >= 7.5:
            return copper(t) + (255,)
        return robe_white(t) + (255,)
    if m.startswith('tick'):
        return tick(cube, t) + (255,)
    if m == 'stump':
        # Torn meat: a wet red core on top, clotted dark sides, a few pale shreds of skin.
        if t['n'] > 0.86:
            return flesh(t, (222, 190, 176)) + (255,)
        wet = t['face'] == 'down'     # model y points down, so 'down' is the top face
        return (mix((156, 22, 30), (96, 10, 18), t['n']) if wet else mix((84, 10, 16), (58, 6, 12), t['n'])) + (255,)
    if m == 'stump_bone':
        return add((226, 214, 188), (t['n'] - 0.5) * 18) + (255,)
    if m in ('robe', 'mantle'):
        rgb = robe_white(t)
        if m == 'mantle':
            rgb = add(rgb, -6)
            if t['side'] and t['fv'] > 0.82:
                rgb = (172, 186, 206) if t['fv'] > 0.92 else copper(t)   # copper band, silver-thread edge
        return dirt(rgb, t, variant) + (255,)
    if m == 'trim':
        rgb = (214, 222, 232) if (t['u'] + t['v']) % 3 else (176, 190, 206)
        return dirt(rgb, t, variant) + (255,)
    if m == 'collar':
        rgb = robe_white(t)
        if t['side'] and t['fv'] < 0.18:
            rgb = copper(t)           # consecrated copper edging
        return rgb + (255,)
    if m == 'cuff':
        rgb = robe_white(t)
        if 0.35 < t['fv'] < 0.6:
            rgb = copper(t)
        return rgb + (255,)
    if m == 'belt':
        return add((150, 168, 186), (t['n'] - 0.5) * 12) + (255,)
    if m == 'chain':
        return (copper(t) if t['v'] % 3 else (60, 70, 66)) + (255,)
    if m in ('copper', 'clasp'):
        rgb = copper(t)
        if m == 'clasp' and t['face'] == 'north':
            dx, dy = t['fu'] - 0.5, t['fv'] - 0.5
            if dx * dx + dy * dy < 0.16 and (dx + 0.18) ** 2 + dy * dy > 0.09:
                rgb = (224, 232, 236)  # the Lady's crescent, polished pale
        return rgb + (255,)
    if m == 'hose':
        rgb = (182, 186, 196) if t['gy'] < 20.5 else (64, 70, 82)
        return add(rgb, (t['n'] - 0.5) * 10) + (255,)
    if m == 'shield_face':
        rgb = add((238, 238, 234), (t['n'] - 0.5) * 8)
        edge = t['fv'] > 0.94 if cube.get('piece') == 'upper' else t['fv'] < 0.06
        if edge:
            rgb = (70, 66, 62)        # the crack's dark lip
        elif t['n'] > 0.93:
            rgb = (196, 194, 188)     # hairline crazing in the lacquer
        return rgb + (255,)
    if m == 'shield_rim':
        return verdigris(t) + (255,)
    raise ValueError('unpainted material ' + m)


def eyes_only(cube, t):
    if cube['material'] != 'face' or t['face'] != 'north':
        return None
    px, py = min(15, int((t['lx'] + 4) * 2)), min(15, int((t['ly'] + 8) * 2))
    if face_pixel(px, py) in ('iris', 'iris_low'):
        return IRIS['gold'] + (255,)
    return None


VARIANTS = dict(base=dict(hair='red', iris='gold', dirty=True), purged=dict(hair='white', iris='blue', dirty=False))


# ---------------------------------------------------------------- Java

EXTRA_IMPORTS = """import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaLooks;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity.EncounterState;
import org.joml.Quaternionf;
"""

EXTRA_FIELDS = """

	private AnnettaLooks.Look look = AnnettaLooks.Look.COWERING;
	private float tickSwell = 0.15F;
	private boolean bladeTrail;
	/** Where the shield's centre sits on the left forearm: out from the arm's axis, and down it from the elbow. */
	private static final float SHIELD_OUT = 3.3F;
	private static final float SHIELD_ALONG = 3.5F;
	private static final float SHIELD_SCALE = 0.75F;"""

# The animation timelines live as plain Java beside this script so they read and diff as Java.
EXTRA_METHODS = (Path(__file__).with_name('annetta_knowles_anim.java')).read_text(encoding='utf-8')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = annetta()
    size = layout(rig, ATLAS_WIDTH)
    base, purged, eyes = paint_atlases(rig, size, [
        lambda c, t: paint(c, t, VARIANTS['base']),
        lambda c, t: paint(c, t, VARIANTS['purged']),
        eyes_only,
    ])
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'annetta_knowles.png')
    purged.save(TEXTURE_DIR / 'annetta_knowles_purged.png')
    eyes.save(TEXTURE_DIR / 'annetta_knowles_eyes.png')
    JAVA.write_text(entity_java_source(rig, size, 'AnnettaKnowlesEntity', 'annetta_knowles.py', armed=True,
                                       extra_fields=EXTRA_FIELDS, extra_methods=EXTRA_METHODS,
                                       extra_imports=EXTRA_IMPORTS), encoding='utf-8')
    print(f'annetta: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        unpurged = quads(rig, hidden=('short_hair', 'neck_tick', 'neck_stump'))
        phase_one = quads(rig, hidden=('short_hair', 'neck_stump'), scales={'tick_abdomen': (2.4, 2.0, 2.4)})
        purged_q = quads(rig, hidden=('long_hair', 'neck_tick', 'neck_stump'))
        preview_sheet([(unpurged, base, 0.0, 0.0), (unpurged, base, math.pi, 0.0),
                       (phase_one, base, -0.9, 0.2), (phase_one, base, -1.6, 0.0),
                       (purged_q, purged, 0.0, 0.0), (purged_q, purged, math.pi * 0.8, 0.15),
                       (phase_one, base, -2.3, 0.3), (unpurged, base, 0.5, 0.15)],
                      ROOT / 'build/annetta_knowles_preview.png')


if __name__ == '__main__':
    main()
