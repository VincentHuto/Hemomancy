"""The Brined Votary: a drowned crewman of a Harbinger research vessel, still walking the wreck it went down with.

Built on the Court's jointed body (court_humanoid.humanoid: hips, body, head, elbow and knee joints with fillers), then
dressed in what the sea left: a waterlogged Harbinger hood and open coat in faded crimson over a dark undershirt,
coat skirts torn ragged at the hem, a hematic-iron diving collar, a votive blood vial on a short chain, a sealed letter
case at the hip (the Brine-Sealed Letter it still carries) and the frayed end of the rope that tied it to the ship.
Salt has dried white along the top edges and in crusts on the crown and shoulders, barnacles have settled in
clusters, strands of kelp hang from the shoulders and the back, and the skin under it all is pale, bloated and mottled. The eyes have sunk back into the skull
with a dull blood light left in them; the vial still glows.

One texture pixel per model unit (16 px per block), thin panels for cloth, no copper, brass or bronze: hematic iron,
bone, lacquer, hemp and cloth only.

    python tools/model_export/brined_votary.py [--preview]

Writes:
  src/main/java/.../client/model/entity/mob/aquatic/BrinedVotaryModel.java   (+ brined_votary_anim.java)
  assets/hemomancy/textures/entity/brined_votary/brined_votary.png          (+ brined_votary_glow.png)
  assets/hemomancy/models/entity/bbmodel/BrinedVotaryModel.bbmodel
  build/mob-preview/brined_votary.png                                         (--preview)
"""
import argparse
import math
from pathlib import Path

from court_humanoid import ROOT, ASSETS, add, bbmodel, check_rig, finish, h, humanoid, mix, smooth
from annetta_kit import entity_java_source, layout, paint_atlases, preview_sheet, quads

MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic'
TEXTURE_DIR = ASSETS / 'textures/entity/brined_votary'
BBMODEL_PATH = ASSETS / 'models/entity/bbmodel/BrinedVotaryModel.bbmodel'
PREVIEW_PATH = ROOT / 'build/mob-preview/brined_votary.png'
ATLAS_WIDTH = 128

SKIN = (156, 168, 156)          # drowned pallor, grey-green
SKIN_BLUE = (124, 140, 150)
LIVID = (122, 106, 126)         # lividity pooled low in the body
CRIMSON = (98, 30, 36)          # Harbinger vestment red, faded by the water
CRIMSON_LIGHT = (122, 42, 46)
CRIMSON_DARK = (60, 20, 26)
LINING = (36, 14, 20)           # the hood's inside, in shadow
NAVY = (40, 44, 56)
NAVY_DARK = (26, 28, 38)
RAG = (112, 104, 88)            # undyed linen wraps
LEATHER = (66, 46, 36)
LEATHER_DARK = (42, 30, 24)
IRON = (86, 68, 70)             # hematic iron
RUST = (120, 50, 36)
SILT = (64, 58, 44)
SALT = (210, 208, 196)
SALT_GREY = (178, 176, 164)     # crust with silt dried into it
KELP = (70, 66, 32)             # olive-brown kelp
KELP_DARK = (48, 44, 22)
KELP_TIP = (128, 118, 58)
HEMP = (128, 112, 82)
BARNACLE = (194, 188, 172)
HAIR = (32, 36, 34)
BLOOD = (150, 20, 30)
EYE_GLOW = (236, 82, 58)


# ---------------------------------------------------------------- rig

def strands(r, part, specs, tip_part=None, tip_specs=()):
    """Kelp strands: (x, length, width) hanging from a part; tip_specs continue some of them from a child part."""
    for x, length, width in specs:
        r.cube(part, (x, 0.0, 0.0, width, length, 0.4), 'kelp')
    for x, length, width in tip_specs:
        r.cube(tip_part, (x, 0.0, 0.0, width, length, 0.4), 'kelp', tip=True)


def rig():
    r = humanoid('brined_votary', 'BrinedVotaryModel')
    r.name = r.layer = 'brined_votary'

    # The hood: a cowl standing out past the face so the face sits back in shadow, a brim sagging over the brow,
    # ragged at every edge, and a wet peak drooping down the back.
    r.part('hood', 'head')
    r.cube('hood', (-4.7, -8.8, -5.6, 9.4, 0.8, 10.2), 'crimson', crust=True)
    r.cube('hood', (-4.7, -8.0, -5.6, 9.4, 1.7, 0.6), 'crimson', ragged=0.9, brim=True)
    r.cube('hood', (-4.7, -8.0, -5.0, 0.7, 8.4, 9.6), 'crimson', ragged=1.6, cowl=True)
    r.cube('hood', (4.0, -8.0, -5.0, 0.7, 8.4, 9.6), 'crimson', ragged=1.6, cowl=True)
    r.cube('hood', (-4.0, -8.0, 4.0, 8.0, 9.4, 0.6), 'crimson', ragged=2.2)
    # Lank wet hair down the sides of the face, inside the cowl.
    r.cube('hood', (-4.0, -7.0, -4.3, 0.9, 6.2, 0.3), 'hair', ragged=1.8)
    r.cube('hood', (3.1, -7.0, -4.3, 0.9, 5.4, 0.3), 'hair', ragged=1.8)
    # A barnacle cluster on the crown and one on the left of the cowl.
    r.cube('hood', (-2.8, -9.6, -1.6, 1.6, 0.8, 1.6), 'barnacle')
    r.cube('hood', (-1.3, -9.4, -0.4, 1.0, 0.6, 1.0), 'barnacle')
    r.cube('hood', (4.7, -5.2, 0.4, 0.7, 1.4, 1.4), 'barnacle')
    r.part('hood_tip', 'hood', (0, -8.6, 4.5), (34, 0, 0))
    r.cube('hood_tip', (-2.2, 0.0, -0.3, 4.4, 3.6, 0.6), 'crimson')
    r.part('hood_tip_end', 'hood_tip', (0, 3.6, 0), (-22, 0, 0))
    r.cube('hood_tip_end', (-1.4, 0.0, -0.25, 2.8, 3.4, 0.5), 'crimson', ragged=1.4)
    # The bloated jaw, hanging a little open below the slack mouth.
    r.part('jaw', 'head', (0, -2.0, 0), (8, 0, 0))
    r.cube('jaw', (-3.9, 0.0, -4.4, 7.8, 2.9, 4.8), 'jaw')

    # The hematic-iron diving collar at the base of the neck.
    r.cube('body', (-4.3, -10.9, -2.7, 8.6, 1.5, 5.4), 'iron')
    r.cube('body', (-3.0, -11.3, -2.9, 1.0, 1.0, 0.4), 'iron_bolt')
    r.cube('body', (2.0, -11.3, -2.9, 1.0, 1.0, 0.4), 'iron_bolt')

    # The open coat: two front panels with the undershirt showing between, the back and the sides.
    r.part('coat', 'body', (0, -10, 0))
    r.cube('coat', (-4.2, 0.6, -2.45, 3.5, 10.6, 0.4), 'crimson')
    r.cube('coat', (0.7, 0.6, -2.45, 3.5, 10.6, 0.4), 'crimson')
    r.cube('coat', (-4.3, 0.4, 2.05, 8.6, 10.8, 0.4), 'crimson')
    r.cube('coat', (-4.25, 0.6, -2.0, 0.3, 10.6, 4.0), 'crimson')
    r.cube('coat', (3.95, 0.6, -2.0, 0.3, 10.6, 4.0), 'crimson')
    # A barnacle cluster between the shoulder blades.
    r.cube('coat', (0.8, 2.2, 2.4, 1.6, 1.4, 0.7), 'barnacle')
    r.cube('coat', (2.2, 3.4, 2.4, 1.0, 1.0, 0.5), 'barnacle')
    r.cube('coat', (0.2, 3.8, 2.4, 0.8, 0.8, 0.4), 'barnacle')

    # The votive: a short hematic-iron chain from the collar and a stoppered blood vial, hanging free of the coat.
    r.part('votive', 'body', (0, -9.4, -2.6))
    r.cube('votive', (-0.35, 0.0, -0.45, 0.7, 2.8, 0.4), 'chain')
    r.cube('votive', (-0.9, 2.6, -0.85, 1.8, 0.6, 1.2), 'bone')
    r.cube('votive', (-0.75, 3.2, -0.75, 1.5, 2.4, 1.0), 'vial')

    # A short torn mantle down the back from the collar.
    r.part('mantle', 'body', (0, -10, 2.45))
    r.cube('mantle', (-4.4, 0.0, 0.0, 8.8, 13.5, 0.4), 'crimson_dark', ragged=4.0)

    # Belt with the sealed letter case on the right hip.
    r.cube('hips', (-4.35, -3.5, -2.35, 8.7, 1.3, 4.7), 'leather')
    r.cube('hips', (-1.0, -3.7, -2.6, 2.0, 1.6, 0.4), 'iron_bolt')
    r.part('letter_case', 'hips', (-4.4, -2.4, -0.4))
    r.cube('letter_case', (-1.3, 0.0, -1.4, 1.3, 3.6, 2.8), 'lacquer')
    r.cube('letter_case', (-1.5, 0.6, -0.4, 0.3, 0.9, 0.8), 'seal')
    r.cube('letter_case', (-0.6, -0.9, -0.3, 0.6, 1.1, 0.6), 'leather')

    # Coat skirts: plumb side panels, two front panels that follow the thighs, one long ragged back panel.
    r.part('skirt', 'hips')
    r.cube('skirt', (-4.35, -0.4, -2.3, 0.4, 8.6, 4.6), 'crimson', ragged=2.5)
    r.cube('skirt', (3.95, -0.4, -2.3, 0.4, 8.0, 4.6), 'crimson', ragged=2.5)
    r.part('skirt_front_right', 'skirt', (-2.0, 0, 0))
    r.cube('skirt_front_right', (-2.2, -0.4, -2.55, 4.1, 8.4, 0.4), 'crimson', ragged=3.0)
    r.part('skirt_front_left', 'skirt', (2.0, 0, 0))
    r.cube('skirt_front_left', (-1.9, -0.4, -2.55, 4.1, 7.2, 0.4), 'crimson', ragged=3.0)
    r.part('skirt_back', 'skirt', (0, 0, 2.15))
    r.cube('skirt_back', (-4.2, -0.4, 0.0, 8.4, 10.0, 0.4), 'crimson', ragged=3.5)

    # The broken tether: a hemp line knotted at the left hip, hanging in two pieces to a frayed end.
    r.part('tether', 'hips', (4.4, -2.6, 0.8))
    r.cube('tether', (-0.5, -0.5, -0.9, 1.0, 1.4, 1.8), 'hemp')
    r.cube('tether', (-0.45, 0.6, -0.45, 0.9, 5.6, 0.9), 'hemp')
    r.part('tether_end', 'tether', (0, 6.0, 0))
    r.cube('tether_end', (-0.4, 0.0, -0.4, 0.8, 4.4, 0.8), 'hemp')
    r.cube('tether_end', (-0.55, 4.2, -0.55, 1.1, 1.2, 1.1), 'hemp', frayed=True)

    for side, sign in (('right', -1), ('left', 1)):
        arm = f'{side}_arm'
        x_out = -3.6 if sign < 0 else 3.0              # the outer face of the arm
        # A sodden shoulder cap of coat cloth with a salt crust, and a torn sleeve flap hanging past the elbow.
        r.cube(arm, (-3.4 if sign < 0 else -1.4, -2.65, -2.4, 4.8, 0.6, 4.8), 'crimson', crust=True)
        r.cube(arm, (x_out, -2.1, -2.4, 0.6, 7.2, 4.8), 'crimson', ragged=2.0)
        r.cube(arm, (-3.0 if sign < 0 else -1.0, -2.05, 2.05, 4.0, 7.6, 0.4), 'crimson', ragged=2.5)
        # A barnacle cluster on the shoulder cap.
        r.cube(arm, (-2.4 if sign < 0 else 0.4, -3.4, -1.2, 1.6, 0.8, 1.6), 'barnacle')
        r.cube(arm, (-0.9 if sign < 0 else -0.6, -3.2, 0.6, 1.0, 0.6, 1.0), 'barnacle')
        # A cluster of kelp strands over the back of the shoulder, at different lengths.
        kelp = f'kelp_{side}'
        r.part(kelp, arm, (sign * 0.8, -2.7, 2.5), (6, 0, 0))
        r.part(f'{kelp}_tip', kelp, (0.0, 8.0, 0.0))
        strands(r, kelp, [(-1.8, 8.0, 1.0), (-0.3, 5.5, 1.0), (1.0, 8.0, 1.0)],
                f'{kelp}_tip', [(-1.8, 4.5, 1.0), (1.0, 2.5, 2.0)])
    # A twist of kelp round the right forearm with two trailing strands; barnacles on the left wrist.
    r.cube('right_forearm', (-2.15, 1.0, -2.15, 4.3, 1.0, 4.3), 'kelp_band')
    r.part('kelp_wrist', 'right_forearm', (0.0, 1.8, 2.1), (10, 0, 0))
    r.cube('kelp_wrist', (-1.2, 0.0, 0.0, 1.0, 6.0, 0.4), 'kelp', tip=True)
    r.cube('kelp_wrist', (0.4, 0.0, 0.0, 1.0, 4.0, 0.4), 'kelp', tip=True)
    r.cube('left_forearm', (1.9, 2.2, -1.0, 0.6, 1.2, 1.2), 'barnacle')
    r.cube('left_forearm', (1.9, 3.3, 0.2, 0.5, 0.8, 0.8), 'barnacle')
    # Kelp from the collar down the back, a looser cluster.
    r.part('kelp_back', 'body', (-1.2, -10.4, 2.9), (4, 0, 0))
    r.part('kelp_back_tip', 'kelp_back', (0.0, 10.0, 0.0))
    strands(r, 'kelp_back', [(-1.6, 10.0, 1.0), (0.0, 6.0, 1.0), (1.4, 10.0, 1.0)],
            'kelp_back_tip', [(-1.6, 5.5, 1.0), (1.4, 3.0, 2.0)])

    # Rag wraps bind the trousers at the calf; heavy sea boots with a turned cuff, toe cap and thick sole.
    for side in ('right', 'left'):
        shin = f'{side}_shin'
        r.cube(shin, (-2.25, 2.6, -2.25, 4.5, 3.0, 4.5), 'boot')
        r.cube(shin, (-2.45, 2.2, -2.45, 4.9, 1.0, 4.9), 'boot_cuff')
        r.cube(shin, (-2.15, 4.3, -3.4, 4.3, 1.4, 1.2), 'boot', toe=True)
        r.cube(shin, (-2.35, 5.6, -3.6, 4.7, 0.4, 6.0), 'sole')
    r.cube('left_shin', (1.0, 0.0, -2.4, 1.0, 4.4, 0.3), 'rag', ragged=1.5)      # a loose wrap hanging free
    r.cube('right_shin', (-2.7, 3.2, -0.8, 0.6, 1.2, 1.2), 'barnacle')
    r.cube('right_shin', (-2.6, 4.2, 0.6, 0.5, 0.8, 0.8), 'barnacle')
    return r


# ---------------------------------------------------------------- paint

def column(t):
    """The fold column a texel sits in: x on the front and back, z on the sides."""
    return math.floor(t['gz'] if t['face'] in ('east', 'west') else t['gx'])


def banded(base, light, dark, t, cube, period=5):
    """Calm cloth: three values, folds as darker vertical bands, a highlight beside each fold, a silted hem."""
    rgb = base
    if t['side']:
        fold = h(column(t) * 1.7, period)                # an uneven run of folds, column by column
        rgb = dark if fold < 0.16 else mix(base, dark, 0.45) if fold < 0.36 else light if fold > 0.86 else base
    box = cube['box']
    from_bottom = box[1] + box[4] - t['ly']
    hem = 1.0 if from_bottom < 1.0 else 0.6 if from_bottom < 2.0 else 0.3 if from_bottom < 3.0 else 0.0
    if cube.get('ragged') and t['side']:
        rgb = mix(rgb, SILT, 0.55 * hem)
    silt = 0.4 if t['gy'] > 20.0 else 0.2 if t['gy'] > 16.0 else 0.0     # the tide line low on the body
    return mix(rgb, SILT, silt)


def salted(rgb, t, cube):
    """Salt only where it dries: a broken line along top edges, and clustered crust on the crown and shoulders."""
    if t['side'] and t['ly'] - cube['box'][1] < 1.0 and smooth(t['gx'], t['gy'], t['gz'], 2.4, 5) > 0.62:
        return mix(rgb, SALT, 0.4)
    if cube.get('crust') and t['face'] == 'down':      # 'down' is a box's visual top (model y points down)
        patch = smooth(t['gx'], 0.0, t['gz'], 1.3, 7)
        if patch > 0.55:
            return SALT_GREY if patch > 0.66 else mix(rgb, SALT_GREY, 0.5)
    return rgb


def torn(cube, t):
    """True where a ragged hem has rotted away: a per-column tear up from the panel's bottom edge."""
    depth = cube.get('ragged')
    if not depth:
        return False
    box = cube['box']
    from_bottom = box[1] + box[4] - t['ly']
    if t['face'] in ('up', 'down') and from_bottom < 1e-3:
        return True
    col = column(t)
    tear = depth * (0.15 + 1.1 * ((col * 0.37 + smooth(col, 0, 0, 1.7, 4)) % 1.0))
    return from_bottom < tear


def drowned_skin(t):
    rgb = mix(SKIN, SKIN_BLUE, smooth(t['gx'], t['gy'], t['gz'], 1.8, 1) * 0.8)
    if smooth(t['gx'] + 3.0, t['gy'], t['gz'], 1.4, 13) > 0.68:
        rgb = mix(rgb, (110, 122, 110), 0.5)            # a sodden, sloughing patch
    livid = max(0.0, min(1.0, (t['gy'] - 4.0) / 18.0)) * smooth(t['gx'], t['gy'], t['gz'], 1.8, 6)
    rgb = mix(rgb, LIVID, livid * 0.8)
    if abs(smooth(t['gx'], t['gy'] * 0.6, t['gz'], 2.0, 3) - 0.5) < 0.035:
        rgb = mix(rgb, (84, 92, 112), 0.5)              # a dark vein under the swollen skin
    return rgb


def face(t):
    skin = drowned_skin(t)
    if t['face'] != 'north':
        return skin
    px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
    side = px if px < 4 else 7 - px
    shade = {0: 0.8, 1: 0.55, 2: 0.3}.get(py, 0.0) + (0.25 if side == 0 else 0.0)   # the cowl's shadow
    if py in (3, 4) and side in (1, 2):
        if py == 4 and side == 2:
            return (170, 54, 40)                        # the dull light left in the sunken eye
        return (22, 20, 28) if py == 4 else (40, 36, 48)
    if py == 2 and side in (1, 2, 3):
        skin = add(skin, -18)                           # the swollen brow over the sockets
    elif py == 4 and side == 3:
        skin = add(skin, -26)
    elif py == 5 and px in (3, 4):
        skin = (64, 56, 66)                             # the nose, collapsed to two dark slits
    elif py == 5 and side in (0, 1):
        skin = add(skin, 12)                            # bloated cheek
    elif py >= 6 and 2 <= px <= 5:
        return (30, 18, 24) if 3 <= px <= 4 else mix(skin, (40, 26, 34), 0.6)   # the slack mouth behind the jaw
    return mix(skin, (20, 18, 26), min(0.85, shade))


def jaw(t):
    skin = mix(drowned_skin(t), LIVID, 0.2)
    if t['face'] == 'north':
        if t['ly'] < 0.8 and 0.3 < t['fu'] < 0.7:
            return (96, 78, 96)                         # the swollen lower lip
        if t['ly'] > 2.2:
            return mix(skin, LIVID, 0.5)
    if t['face'] == 'down':
        return (30, 18, 24) if t['fv'] < 0.7 else skin  # the inside of the mouth
    return skin


def kelp(cube, t):
    from_bottom = cube['box'][1] + cube['box'][4] - t['ly']
    tip = cube.get('tip')
    if tip and cube['box'][3] > 1.5 and from_bottom < 1.0 and t['side'] and t['fu'] > 0.5:
        return None                                     # a two-pixel blade narrowing to a point
    rgb = KELP_DARK if t['ly'] < 2.0 and not tip else KELP          # darker where it lies against the cloth
    if t['face'] in ('east', 'west'):
        rgb = add(rgb, -10)
    if tip and from_bottom < 2.0:
        rgb = KELP_TIP if from_bottom < 1.0 else mix(KELP, KELP_TIP, 0.5)
    return rgb


def paint(cube, t):
    m = cube['material']
    if torn(cube, t):
        return None
    if m == 'face':
        return face(t)
    if m == 'jaw':
        return jaw(t)
    if m in ('torso', 'cloth'):
        if m == 'torso' and t['face'] == 'north' and abs(t['lx']) < 1.0 and t['ly'] < -4.0:
            return drowned_skin(t)                      # the undershirt torn open at the throat
        return banded(NAVY, add(NAVY, 12), NAVY_DARK, t, cube, 4)
    if m == 'arm':
        if cube.get('seg', 0.0) == 0.0:
            return salted(banded(CRIMSON, CRIMSON_LIGHT, CRIMSON_DARK, t, cube, 4), t, cube)
        if t['ly'] < 2.6 - (1.4 if smooth(t['gx'], 0, t['gz'], 0.9, 12) > 0.5 else 0.0):
            return banded(CRIMSON, CRIMSON_LIGHT, CRIMSON_DARK, t, cube, 4)   # what is left of the sleeve
        rgb = mix(drowned_skin(t), SKIN_BLUE, max(0.0, (t['ly'] - 2.0) / 6.0) * 0.5)
        if t['ly'] > 5.0:
            rgb = mix(rgb, LIVID, 0.5)                  # the hands, darker where the blood settled
        if t['ly'] > 6.0 and t['face'] == 'up':
            return add(rgb, -20)
        if t['ly'] > 6.6 and t['side']:
            return (70, 66, 72)                         # blackened nails
        if t['ly'] > 5.0 and t['side'] and t['u'] % 2 == 1:
            return add(rgb, -26)                        # fingers
        return rgb
    if m == 'leg':
        if cube.get('seg', 0.0) > 0.0 and t['side']:
            # Undyed rag wound round the calf over the trousers, in diagonal turns.
            turn = math.floor(t['ly'] + (t['lx'] if t['face'] in ('north', 'south') else t['lz']) * 0.5) % 3
            if turn != 2:
                return mix(RAG, SILT, 0.15 if turn else 0.4)
        return banded(NAVY, add(NAVY, 12), NAVY_DARK, t, cube, 4)
    if m == 'rag':
        return mix(RAG, SILT, 0.25 if t['u'] % 2 else 0.4)
    if m in ('boot', 'boot_cuff', 'sole'):
        if m == 'sole':
            return (34, 26, 22)
        rgb = LEATHER if m == 'boot' else add(LEATHER, 14)
        if t['side'] and m == 'boot' and not cube.get('toe') and math.floor(t['ly']) % 2 == 0 and column(t) % 3 == 0:
            rgb = LEATHER_DARK                          # creases at the ankle
        if m == 'boot' and t['face'] == 'east' and 3.4 < t['ly'] < 4.4 and t['lz'] < 0.4:
            rgb = IRON                                  # a hematic-iron buckle
        if cube.get('toe') and t['face'] == 'down':
            rgb = add(rgb, 18)
        return salted(mix(rgb, SILT, 0.2), t, cube) if m == 'boot_cuff' else mix(rgb, SILT, 0.2)
    if m == 'crimson':
        if cube.get('cowl') and t['face'] in ('east', 'west') and ((t['face'] == 'east') == (t['lx'] < 0)):
            return LINING                               # the inside of the cowl
        if cube.get('brim') and t['face'] == 'south':
            return LINING
        if cube.get('cowl') and t['face'] == 'north':
            return CRIMSON_DARK                         # the cowl's front edge
        return salted(banded(CRIMSON, CRIMSON_LIGHT, CRIMSON_DARK, t, cube), t, cube)
    if m == 'crimson_dark':
        return banded(CRIMSON_DARK, add(CRIMSON_DARK, 14), (40, 14, 20), t, cube)
    if m == 'hair':
        return add(HAIR, 10) if (t['u'] + t['v'] // 3) % 3 == 0 else HAIR
    if m == 'leather':
        rgb = LEATHER if column(t) % 4 else LEATHER_DARK
        return salted(add(rgb, 12) if t['side'] and t['fv'] < 0.2 else rgb, t, cube)
    if m in ('iron', 'iron_bolt', 'chain'):
        rgb = IRON
        if t['side'] and t['fv'] < 0.25:
            rgb = add(rgb, 26)                          # worn bright along the top edge
        if m == 'chain' and t['v'] % 2 == 1:
            rgb = add(rgb, -30)
        if m == 'iron_bolt':
            rgb = add(rgb, 22 if (t['u'] + t['v']) % 2 else -10)
        rust = smooth(t['gx'], t['gy'], t['gz'], 1.4, 7)
        if rust > 0.6:
            rgb = RUST if rust > 0.7 else mix(rgb, RUST, 0.5)
        return rgb
    if m == 'bone':
        return (206, 194, 168) if t['v'] % 2 else (186, 174, 148)
    if m == 'vial':
        if t['side'] and (t['u'] == 0 or t['fv'] < 0.25):
            return (178, 196, 192)                      # the glass rim and highlight
        return BLOOD if t['n'] > 0.3 else (110, 14, 24)
    if m == 'lacquer':
        rgb = (44, 26, 28)
        if t['side'] and t['fv'] < 0.1:
            rgb = add(rgb, 34)
        return rgb
    if m == 'seal':
        return (132, 22, 30)
    if m == 'hemp':
        if cube.get('frayed') and t['side'] and t['fv'] > 0.4 and t['u'] % 2 == 0:
            return None                                 # the frayed fibres
        return HEMP if (t['u'] + t['v']) % 3 else add(HEMP, -28)
    if m == 'kelp':
        return kelp(cube, t)
    if m == 'kelp_band':
        return KELP if (t['u'] + t['v']) % 3 else KELP_DARK
    if m == 'barnacle':
        if t['face'] == 'down':
            cx, cy = abs(t['fu'] - 0.5), abs(t['fv'] - 0.5)
            if cx < 0.26 and cy < 0.26:
                return (58, 54, 50)                     # the shell's open mouth
            return BARNACLE
        return BARNACLE if t['fv'] < 0.5 else (150, 150, 132)
    raise ValueError('unpainted material ' + m)


def glow(cube, t):
    m = cube['material']
    if m == 'face' and t['face'] == 'north':
        px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
        side = px if px < 4 else 7 - px
        if py == 4 and side == 2:
            return EYE_GLOW
        if py == 4 and side == 1:
            return (90, 20, 16)
    if m == 'vial' and not (t['side'] and (t['u'] == 0 or t['fv'] < 0.25)):
        return (180, 30, 34) if t['n'] > 0.3 else (120, 18, 24)
    return None


# ---------------------------------------------------------------- write

POSES = [('rest', {}, ()),
         ('wade', {'body': (8, 0, 0), 'head': (-8, 0, 0), 'right_arm': (-52, 0, 6), 'left_arm': (-44, 0, -6),
                   'right_forearm': (-18, 0, 0), 'left_forearm': (-22, 0, 0), 'right_leg': (-22, 0, 0),
                   'left_leg': (18, 0, 0), 'left_shin': (34, 0, 0), 'skirt_front_right': (-20, 0, 0),
                   'skirt_back': (14, 0, 0), 'kelp_left': (20, 0, 0), 'kelp_right': (20, 0, 0),
                   'votive': (-8, 0, 0), 'tether': (12, 0, 0), 'jaw': (6, 0, 0)}, ()),
         ('strike', {'body': (16, -12, 0), 'right_arm': (-150, 0, 10), 'right_forearm': (-30, 0, 0),
                     'left_arm': (-60, 0, -8), 'left_forearm': (-20, 0, 0)}, ())]


def write(r, preview=False):
    check_rig(r)
    size = layout(r, ATLAS_WIDTH)
    painters = [lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(paint(c, t)),
                lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(glow(c, t))]
    base, emissive = paint_atlases(r, size, painters)
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'brined_votary.png')
    emissive.save(TEXTURE_DIR / 'brined_votary_glow.png')
    anim = Path(__file__).with_name('brined_votary_anim.java').read_text(encoding='utf-8')
    source = entity_java_source(
        r, size, 'BrinedVotaryEntity', 'brined_votary.py', extra_methods=anim,
        extra_imports='import com.vincenthuto.hemomancy.common.util.HemoEasing;\n',
        package='com.vincenthuto.hemomancy.client.model.entity.mob.aquatic',
        entity_package='com.vincenthuto.hemomancy.common.entity.mob.aquatic')
    for unused in ('import com.mojang.blaze3d.vertex.PoseStack;\n', 'import net.minecraft.world.entity.HumanoidArm;\n'):
        source = source.replace(unused, '')
    source = source.replace('/** Generated by tools/model_export/brined_votary.py; edit the rig there and re-run it. */',
                            '/** Generated by tools/model_export/brined_votary.py on the court_humanoid.py rig; edit the '
                            'generator (and brined_votary_anim.java) and re-run it. */')
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    (MODEL_DIR / 'BrinedVotaryModel.java').write_text(source, encoding='utf-8')
    bbmodel(r, size, TEXTURE_DIR / 'brined_votary.png', BBMODEL_PATH)
    print(f'brined_votary: {sum(len(p["cubes"]) for p in r.parts)} cubes, {len(r.parts)} parts, '
          f'atlas {size[0]}x{size[1]}, glow')
    if preview:
        tiles = []
        for _, overrides, hidden in POSES:
            q = quads(r, hidden=hidden, overrides=overrides)
            tiles += [(q, base, 0.0, 0.1), (q, base, math.pi * 0.75, 0.15)]
        q = quads(r)
        tiles += [(q, base, math.pi, 0.1), (q, base, math.pi * 0.5, 0.05)]
        preview_sheet(tiles, PREVIEW_PATH)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), args.preview)


if __name__ == '__main__':
    main()
