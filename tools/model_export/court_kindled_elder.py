"""A Kindled elder: a member of the Kindling grown into the mature Hydra form, still among the house, still preparing.

A new body on the Court rig's joint conventions (every limb breaks at a joint with an inset filler): one long jointed
neck ending in a pale polyp head ringed by a crown of flame-coloured tentacles; a separate pair of jointed tentacle arms
below the neck, ending in three grasping fingers; digitigrade legs on clawed feet; and a long segmented tail. Pale,
translucent-looking tissue with red vessels showing through; the crown runs amber at the root through flame orange to
crimson at the tips, and the tips carry biological light. Nothing burns.

    python tools/model_export/court_kindled_elder.py [--preview]
"""
import argparse
import math

from court_humanoid import add, h, mix, write
from annetta_kit import Rig

NECK = 4
CROWN = 8
TAIL = 6


def rig():
    r = Rig('court_kindled_elder', 'CourtKindledElderModel', 'court_kindled_elder')
    r.part('hips', 'root', (0, 6.5, 0))
    r.cube('hips', (-3.5, -2.0, -2.5, 7.0, 4.0, 5.0), 'polyp')
    # Digitigrade legs: thigh forward, shin back, the long foot forward again onto three claws.
    for side, sign in (('right', -1), ('left', 1)):
        leg, shin, foot, toes = (f'{side}_{p}' for p in ('leg', 'shin', 'foot', 'toes'))
        r.part(leg, 'hips', (sign * 2.6, 1.0, 0.0), (-30, 0, 0))
        r.cube(leg, (-1.6, 0.0, -1.6, 3.2, 8.2, 3.2), 'polyp')
        r.part(shin, leg, (0, 8.0, 0), (70, 0, 0))
        r.cube(shin, (-1.3, 0.0, -1.3, 2.6, 7.0, 2.6), 'polyp')
        r.cube(shin, (-1.2, -1.0, -1.2, 2.4, 1.1, 2.4), 'polyp', filler=True)
        r.part(foot, shin, (0, 7.0, 0), (-55, 0, 0))
        r.cube(foot, (-1.0, 0.0, -1.0, 2.0, 4.6, 2.0), 'polyp')
        r.cube(foot, (-0.9, -0.9, -0.9, 1.8, 1.0, 1.8), 'polyp', filler=True)
        r.part(toes, foot, (0, 4.4, 0), (15, 0, 0))
        for i, x in enumerate((-0.8, 0.0, 0.8)):
            r.cube(toes, (x - 0.35, -0.4, -3.2 + abs(i - 1) * 0.6, 0.7, 0.8, 3.2 - abs(i - 1) * 0.6), 'claw')
    # The body leans forward from the waist over the hips, ribbed where the tissue has thickened.
    r.part('body', 'hips', (0, -2.0, 0), (15, 0, 0))
    r.cube('body', (-4.0, -9.0, -3.0, 8.0, 10.0, 6.0), 'polyp')
    for i in range(4):
        r.cube('body', (-4.2, -7.5 + i * 2.0, -3.2, 8.4, 0.8, 0.5), 'tissue')
    # The neck: segments arching up and over, swan-like, to the polyp head.
    parent, angles = 'body', (5, -15, -15, 0)
    for i in range(NECK):
        name = f'neck_{i}'
        r.part(name, parent, (0, -9.0 if i == 0 else -4.4, -0.8 if i == 0 else 0.0), (angles[i], 0, 0))
        w = 3.4 - i * 0.25
        r.cube(name, (-w / 2, -4.8, -w / 2, w, 5.0, w), 'polyp')
        r.cube(name, (-w / 2 + 0.1, -1.0, -w / 2 + 0.1, w - 0.2, 1.2, w - 0.2), 'polyp', filler=True)
        parent = name
    r.part('head', parent, (0, -4.4, 0), (70, 0, 0))
    r.cube('head', (-2.6, -4.2, -2.6, 5.2, 4.6, 5.2), 'polyp')
    r.cube('head', (-1.8, -4.8, -1.8, 3.6, 0.7, 3.6), 'mouth')
    for i in range(CROWN):
        a = 2 * math.pi * i / CROWN
        cx, cz = math.cos(a), math.sin(a)
        name = f'crown_{i}'
        r.part(name, 'head', (2.2 * cx, -3.8, 2.2 * cz), (-40 * cz, 0, 40 * cx))
        r.cube(name, (-0.55, -3.6, -0.55, 1.1, 3.8, 1.1), 'mane', seg=0)
        r.part(name + '_tip', name, (0, -3.5, 0), (-25 * cz, 0, 25 * cx))
        r.cube(name + '_tip', (-0.4, -3.4, -0.4, 0.8, 3.5, 0.8), 'mane', seg=1)
    # Tentacle arms below the neck: upper arm, forearm (with its filler), three fingers.
    for side, sign in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        r.part(arm, 'body', (sign * 4.2, -7.5, -0.5), (-20, 0, sign * -18))
        r.cube(arm, (-1.1, -0.5, -1.1, 2.2, 6.4, 2.2), 'polyp')
        r.part(fore, arm, (0, 6.0, 0), (-30, 0, 0))
        r.cube(fore, (-0.9, 0.0, -0.9, 1.8, 6.0, 1.8), 'polyp')
        r.cube(fore, (-0.85, -0.9, -0.85, 1.7, 1.0, 1.7), 'polyp', filler=True)
        for i, spread in enumerate((-22, 0, 22)):
            name = f'{side}_finger_{i}'
            r.part(name, fore, (0, 5.8, 0), (10, spread, sign * spread * 0.5))
            r.cube(name, (-0.35, 0.0, -0.35, 0.7, 3.2, 0.7), 'mane', seg=0)
    # The tail: down from the hips, then sweeping back and lifting at the end.
    parent = 'hips'
    for i in range(TAIL):
        name = f'tail_{i}'
        r.part(name, parent, (0, 0.0, 2.2) if i == 0 else (0, 0, 4.3), (-30 if i == 0 else 7, 0, 0))
        w = 3.2 - i * 0.42
        r.cube(name, (-w / 2, -w / 2, 0.0, w, w, 4.6), 'polyp')
        parent = name
    r.cube(parent, (-0.3, -2.2, 1.0, 0.6, 4.4, 3.4), 'mane', seg=1)
    return r


def vessel(t):
    return abs(math.sin(t['gx'] * 1.7 + t['gz'] * 1.3 + t['gy'] * 0.4)) < 0.09 or \
        abs(math.sin(t['gy'] * 1.1 - t['gz'] * 0.9)) < 0.06


def paint(cube, t):
    m = cube['material']
    if m == 'polyp':
        base = mix((232, 214, 206), (210, 188, 182), t['n'])
        if vessel(t):
            return mix((176, 40, 44), (132, 24, 30), t['n'])
        return add(base, -12) if (t['u'] + t['v']) % 5 == 0 else base
    if m == 'tissue':
        return mix((200, 120, 98), (170, 92, 76), t['n'])
    if m == 'mouth':
        return mix((120, 30, 34), (80, 18, 24), t['n'])
    if m == 'claw':
        return add((96, 80, 70), (t['n'] - 0.5) * 20)
    if m == 'mane':
        f = min(1.0, cube['seg'] * 0.5 + t['fv'] * 0.5)
        rgb = mix((228, 152, 44), (238, 98, 30), f * 1.6) if f < 0.6 else mix((238, 98, 30), (176, 26, 30), (f - 0.6) * 2.5)
        return add(rgb, 14) if h(t['gx'], t['gy'], t['gz']) > 0.8 else rgb
    raise ValueError('unpainted material ' + m)


def glow(cube, t):
    if cube['material'] == 'mane' and cube['seg'] == 1 and t['fv'] > 0.45:
        return mix((255, 170, 70), (255, 90, 50), t['fv'])
    if cube['material'] == 'polyp' and vessel(t) and t['n'] > 0.7:
        return (200, 60, 60)
    return None


POSES = [('rest', {}, ()),
         ('reaching', {'right_arm': (-60, 0, 10), 'right_forearm': (-40, 0, 0), 'neck_3': (-15, 0, 0),
                       'head': (-15, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'kindled_elder', 'court_kindled_elder.py', paint, glow, POSES, args.preview, base_rig=False)


if __name__ == '__main__':
    main()
