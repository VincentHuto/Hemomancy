"""A Rooted elder of Last Marrow: a member who chose to root in the hollow bones of the fall at the end of a life.

A new body on the Court rig's joint conventions (arms break at the elbow with an inset filler). Below the waist there
are no legs any more, only a mass of pale root-sheaths that spreads into the bone it lies in. The torso leans back
against the bone with the arms raised, forearms sheathed bone-white and beaded red, and from the forearms, the
shoulders and the crown rise stalked feathery plumes, blood red, that comb the water for what drifts down (the stalked
sea lily of the spec's references). Roots run from the hands back into the bone. The face is old, calm and awake:
rooting is anchoring, chosen and reversible, never petrification.

    python tools/model_export/court_rooted_elder.py [--preview]
"""
import argparse
import math

from court_humanoid import PALETTES, add, eye_face, flesh, h, mix, write
from annetta_kit import Rig

PAL = dict(PALETTES['last_marrow'], skin=(206, 184, 166), hair=((214, 206, 196), (180, 172, 162), (236, 230, 222)))
CROWN = 6           # plumes round the head
SHOULDER = 4        # plumes along the shoulders and back
ARM_PLUMES = 2      # plumes on each forearm


def plume(r, name, parent, pivot, rotation, stalk, crown_size=2.2):
    """A stalked feathery plume: a pale-red stalk, then a crown of fine red pinnules that can open and close."""
    r.part(name, parent, pivot, rotation)
    r.cube(name, (-0.35, -stalk, -0.35, 0.7, stalk, 0.7), 'stalk', seg=0)
    r.part(name + '_crown', name, (0, -stalk, 0))
    for i in range(6):
        a = i * math.pi / 3
        r.cube(name + '_crown', (-0.2 + 0.8 * math.cos(a), -crown_size, -0.2 + 0.8 * math.sin(a), 0.4, crown_size, 0.4),
               'pinnule', seg=1)
    r.cube(name + '_crown', (-0.6, -0.6, -0.6, 1.2, 0.8, 1.2), 'stalk', seg=1)


def rig():
    r = Rig('court_rooted_elder', 'CourtRootedElderModel', 'court_rooted_elder')
    # The root mass: a low mound of sheaths where the legs were, with root strands spreading over the ground.
    r.part('hips', 'root', (0, 21.0, 0))
    r.cube('hips', (-4.5, -1.5, -3.0, 9.0, 4.5, 6.5), 'sheath')
    r.cube('hips', (-5.5, 1.0, -4.0, 11.0, 2.0, 8.5), 'sheath')
    for i in range(8):
        a = i * math.pi / 4 + 0.3
        name = f'root_{i}'
        r.part(name, 'hips', (math.cos(a) * 4.0, 2.4, math.sin(a) * 3.0), (0, -math.degrees(a) + 90, 0))
        r.part(name + '_down', name, (0, 0, 0), (-78, 0, 0))
        r.cube(name + '_down', (-0.45, 0.0, -0.45, 0.9, 4.0 + (i % 3), 0.9), 'root')
    # The torso, leaning back against the bone.
    r.part('body', 'hips', (0, -1.0, 0.5), (-22, 0, 0))
    r.cube('body', (-4.0, -10.0, -2.0, 8.0, 10.5, 4.0), 'torso')
    r.cube('body', (-4.2, -2.0, -2.2, 8.4, 2.5, 4.4), 'sheath')
    r.part('head', 'body', (0, -10.0, 0))
    r.cube('head', (-4.0, -8.0, -4.0, 8.0, 8.0, 8.0), 'face')
    r.cube('head', (-4.2, -8.3, -4.2, 8.4, 1.4, 8.4), 'hair')
    r.cube('head', (-4.2, -7.0, 3.5, 8.4, 6.0, 0.7), 'hair')
    for i in range(CROWN):
        a = math.pi * (0.15 + 0.7 * i / (CROWN - 1))
        plume(r, f'crown_{i}', 'head', (-3.4 * math.cos(a), -7.6, 1.0 + 1.6 * math.sin(a)),
              (-8 - 10 * math.sin(a), 0, 34 * math.cos(a)), 5.5 + 1.5 * (i % 2), 3.4)
    for i in range(SHOULDER):
        x = -3.0 + 2.0 * i
        plume(r, f'back_{i}', 'body', (x, -9.0, 1.8), (-24 - 8 * (i % 2), 0, x * 7), 8.0 + (i % 2) * 2.5, 3.8)
    # The arms raised, sheathed from the elbow; plumes along the forearms; roots from the hands into the bone.
    for side, sign in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        r.part(arm, 'body', (sign * 5.0, -8.0, 0), (-40, 0, sign * -30))
        x0 = -3.0 if sign < 0 else -1.0
        r.cube(arm, (x0, -2.0, -2.0, 4.0, 6.2, 4.0), 'arm', seg=0)
        r.part(fore, arm, (sign * 1.0, 4.0, 0), (-35, 0, 0))
        r.cube(fore, (-2.0, 0.0, -2.0, 4.0, 6.0, 4.0), 'sheath', seg=1)
        r.cube(fore, (-1.9, -1.2, -1.9, 3.8, 1.3, 3.8), 'sheath', seg=1, filler=True)
        for i in range(ARM_PLUMES):
            plume(r, f'{side}_plume_{i}', fore, (sign * 1.2, 1.5 + 2.6 * i, -1.6), (-60 + 10 * i, 0, sign * -20), 4.5, 3.0)
        name = f'{side}_hand_root'
        r.part(name, fore, (0, 6.0, 0), (40, 0, 0))
        r.cube(name, (-0.5, 0.0, -0.5, 1.0, 6.0, 1.0), 'root')
        r.cube(name, (-1.4, 0.0, -0.4, 0.8, 4.0, 0.8), 'root')
    return r


def beads(t):
    return h(t['gx'] * 2.1, t['gy'] * 2.1, t['gz'] * 2.1) > 0.955


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        return eye_face(t, PAL)
    if m in ('torso', 'arm'):
        rgb = flesh(t, PAL['skin'])
        return mix(rgb, PAL['trim'], 0.4) if h(t['gx'] * 0.7, t['gy'] * 0.7, t['gz'] * 0.7) > 0.7 else rgb
    if m == 'hair':
        base, dark, light = PAL['hair']
        k = (t['u'] + t['v'] // 3) % 5
        return dark if k == 0 else light if k == 3 else base
    if m == 'sheath':
        rgb = add(PAL['trim'], (t['n'] - 0.5) * 18)
        if t['side'] and t['v'] % 3 == 0:
            rgb = add(rgb, -14)        # ringed like a stalk
        return PAL['marrow'] if beads(t) else rgb
    if m == 'root':
        return mix((206, 192, 170), (170, 152, 128), t['n']) if t['v'] % 3 else (190, 80, 80)
    if m == 'stalk':
        return mix((226, 196, 186), (200, 90, 90), min(1.0, cube['seg'] * 0.5 + t['fv'] * 0.4))
    if m == 'pinnule':
        rgb = mix((216, 52, 60), (150, 24, 34), t['fv'])
        return add(rgb, 16) if t['v'] % 2 == 0 else rgb
    raise ValueError('unpainted material ' + m)


def glow(cube, t):
    # No light of its own: the Last Marrow lives in the dark below the light. The red reads by the bell's pickles.
    return None


POSES = [('rest', {}, ()),
         ('closed', {'body': (-10, 0, 0), 'head': (30, 0, 0), 'right_arm': (-10, 0, 20), 'left_arm': (-10, 0, -20),
                     'right_forearm': (-90, 0, 0), 'left_forearm': (-90, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'rooted_elder', 'court_rooted_elder.py', paint, None, POSES, args.preview, base_rig=False)


if __name__ == '__main__':
    main()
