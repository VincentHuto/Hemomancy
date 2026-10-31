"""A Linesman of the Woven Current: the member who tends the camp's lines across the gully and ties in to the others.

Wears Dead Man's Rope at Mature (the vein network over the shoulders and the ribbon strands trailing from the forearms
come from the strain layer, and the renderer draws the strands that link a Linesman to the camp's other members). Under
it: twilight-pale skin with violet vein glints on the hands, hair worked into one long rope down the back with cord
bands, an indigo wrap tunic and short split skirt, a harness strap over the shoulder carrying a coil of pale fibre line
at the hip, line wrapped round the wrists and ankles for grip, and a bone gaff with a hooked head for catching and
clearing lines.

    python tools/model_export/court_linesman.py [--preview]
"""
import argparse
import math

from court_humanoid import PALETTES, network, add, anklets, flesh, hair_cap, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = PALETTES['woven_current']


def rig():
    r = humanoid('linesman', 'CourtLinesmanModel')
    hair_cap(r, back=4.5)
    # The hair worked into a single rope down the back, banded with cord.
    r.part('hair_rope', 'hair', (0, -4.0, 4.1), (10, 0, 0))
    r.cube('hair_rope', (-0.9, 0.0, -0.6, 1.8, 10.0, 1.2), 'hair')
    for y in (0.6, 4.0, 7.4):
        r.cube('hair_rope', (-1.1, y, -0.75, 2.2, 0.7, 1.5), 'cord')
    r.cube('hair_rope', (-1.2, 9.6, -0.8, 2.4, 1.2, 1.6), 'cord')
    wrap_skirt(r, 6.0, material='cloth', front_split=True)
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cloth2')
    # The harness: one strap from the left shoulder across to the right hip, front and back.
    r.part('harness', 'body', (0, -10, 0))
    for i in range(5):
        x = 1.6 - i * 1.4
        r.cube('harness', (x, 0.2 + i * 2.0, -2.45, 1.4, 2.2, 0.4), 'cord')
        r.cube('harness', (x, 0.2 + i * 2.0, 2.05, 1.4, 2.2, 0.4), 'cord')
    # The coil of line at the right hip, hung from the strap.
    r.part('coil', 'hips', (-4.6, -2.0, 0.0), (0, 0, 10))
    for i in range(4):
        y = i * 0.9
        r.cube('coil', (-1.4, y, -2.2, 1.4, 0.8, 0.5), 'line')
        r.cube('coil', (-1.4, y, 1.7, 1.4, 0.8, 0.5), 'line')
        r.cube('coil', (-1.4, y, -1.7, 0.5, 0.8, 3.4), 'line')
    # Line wrapped round the wrists for grip.
    for side in ('right', 'left'):
        r.cube(f'{side}_forearm', (-2.15, 3.4, -2.15, 4.3, 0.6, 4.3), 'line')
        r.cube(f'{side}_forearm', (-2.15, 4.4, -2.15, 4.3, 0.6, 4.3), 'line')
    anklets(r, material='line')
    # The gaff: a bone shaft with a hooked bone head, carried in the right hand.
    r.part('gaff', 'right_forearm', (0, 5.5, -0.5))
    r.cube('gaff', (-0.5, -12.0, -0.5, 1.0, 18.0, 1.0), 'bone')
    r.cube('gaff', (-0.65, -1.0, -0.65, 1.3, 2.4, 1.3), 'line')
    r.cube('gaff', (-0.45, -13.4, -0.45, 0.9, 1.6, 0.9), 'bone')
    r.cube('gaff', (-0.4, -14.2, -2.6, 0.8, 0.9, 2.6), 'bone')
    r.cube('gaff', (-0.35, -13.6, -3.0, 0.7, 1.6, 0.7), 'bone')
    return r


def vein(t):
    return network(t, 0.035)


def skin(t, glints=True):
    rgb = flesh(t, PAL['skin'])
    if glints and vein(t):
        return mix(rgb, PAL['vein'], 0.55)
    return rgb


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        # An indigo wrap tunic crossing left over right; the throat and collarbones bare.
        if t['face'] == 'north' and t['gy'] < 2.6 and abs(t['gx'] + 0.6 * (t['gy'] - 2.6)) < 1.6:
            return skin(t, False)
        rgb = weave(PAL['cloth'], t)
        if t['face'] == 'north' and abs(t['gx'] - (t['gy'] - 5.0) * 0.5) < 0.4:
            rgb = add(rgb, -22)       # the wrap's crossing edge
        return rgb
    if m == 'arm':
        return weave(PAL['cloth'], t) if t['gy'] < 4.0 else skin(t, t['gy'] > 7.0)
    if m == 'leg':
        return skin(t, False) if t['gy'] > 18.0 else weave(add(PAL['cloth'], -8), t)
    if m == 'line':
        # Pale twisted fibre: a diagonal lay.
        return PAL['cord'] if (t['u'] + t['v']) % 3 else add(PAL['cord'], 26)
    return paint_base(cube, t, PAL)


def glow(cube, t):
    if cube['material'] == 'arm' and t['gy'] > 7.0 and vein(t) and t['n'] > 0.45:
        return PAL['vein']
    return None


POSES = [('rest', {'right_arm': (-12, 0, 0), 'right_forearm': (-30, 0, 0), 'gaff': (42, 0, 0)}, ()),
         ('walk', {'right_leg': (30, 0, 0), 'right_shin': (35, 0, 0), 'left_leg': (-30, 0, 0),
                   'left_arm': (25, 0, 0), 'right_arm': (-12, 0, 0), 'right_forearm': (-30, 0, 0), 'gaff': (42, 0, 0)}, ()),
         ('offering the line', {'left_arm': (-50, 0, -12), 'left_forearm': (-30, 0, 0),
                                'right_arm': (-12, 0, 0), 'right_forearm': (-30, 0, 0), 'gaff': (42, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'linesman', 'court_linesman.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
