"""The Tidewarden of the Holding Stone: the mobile member who patrols the refuge's passages and keeps them safe.

Wears Holdfast at Mature (the roots wrapped round the forearms come from the strain layer, so the arms stay bare). A
crossed harness of kelp cord over a sleeveless kelp-fibre wrap, a short coralline-dyed skirt under a belt of slate
weights, cord bindings up the shins for the rocks, hair bound in a topknot with a cord band and two stone beads, and a
long sounding pole of driftwood shod in bone, a slate weight hanging from its head on a cord: the tool that tests a
passage before anyone else is sent through it. The Tidewarden defends home; the pole is what it strikes with.

    python tools/model_export/court_tidewarden.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, h, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = dict(PALETTES['holding_stone'])


def rig():
    r = humanoid('tidewarden', 'CourtTidewardenModel')
    wrap_skirt(r, 6.0, material='cloth2', front_split=True)
    # A belt of slate weights over the skirt's top: the house's own stone carried at the hip.
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cord')
    for x in (-3.4, -1.2, 1.2):
        r.cube('hips', (x, -3.9, -2.75, 1.8, 1.8, 0.5), 'stone')
    # The harness: two cord bands crossing the chest and the back.
    r.part('harness', 'body', (0, -10, 0))
    for sign in (-1, 1):
        r.cube('harness', (-0.5 + sign * 1.6, 0.2, -2.45, 1.0, 9.6, 0.4), 'cord')
        r.cube('harness', (-0.5 + sign * 1.6, 0.2, 2.05, 1.0, 9.6, 0.4), 'cord')
    r.cube('harness', (-3.0, 4.0, -2.55, 6.0, 1.0, 0.5), 'cord')
    r.cube('harness', (-1.0, 3.6, -2.75, 2.0, 1.8, 0.4), 'stone')
    # Shin bindings for the rocks.
    for side in ('right', 'left'):
        r.cube(f'{side}_shin', (-2.1, 0.6, -2.1, 4.2, 0.6, 4.2), 'cord')
        r.cube(f'{side}_shin', (-2.1, 2.4, -2.1, 4.2, 0.6, 4.2), 'cord')
        r.cube(f'{side}_shin', (-2.1, 4.4, -2.1, 4.2, 0.7, 4.2), 'cord')
    # Hair cropped close at the sides and bound up in a topknot under a cord band with two stone beads.
    r.part('hair', 'head')
    r.cube('hair', (-4.25, -8.3, -4.25, 8.5, 1.4, 8.5), 'hair')
    r.cube('hair', (-4.25, -7.0, 3.6, 8.5, 3.0, 0.7), 'hair')
    r.cube('hair', (-4.3, -6.9, -2.0, 0.6, 2.0, 5.6), 'hair')
    r.cube('hair', (3.7, -6.9, -2.0, 0.6, 2.0, 5.6), 'hair')
    r.cube('hair', (-4.4, -6.4, -4.4, 8.8, 0.8, 8.8), 'cord')
    r.cube('hair', (-4.7, -6.6, -1.0, 0.5, 1.2, 1.2), 'stone')
    r.cube('hair', (4.2, -6.6, -1.0, 0.5, 1.2, 1.2), 'stone')
    r.part('topknot', 'hair', (0, -8.3, 0.6))
    r.cube('topknot', (-1.3, -2.4, -1.3, 2.6, 2.4, 2.6), 'hair')
    r.cube('topknot', (-1.5, -1.0, -1.5, 3.0, 0.5, 3.0), 'cord')
    # The sounding pole, held upright in the right hand, with its slate weight on a cord.
    r.part('pole', 'right_forearm', (0, 5.5, -0.5))
    r.cube('pole', (-0.55, -18.0, -0.55, 1.1, 26.0, 1.1), 'wood')
    r.cube('pole', (-0.7, 7.0, -0.7, 1.4, 1.6, 1.4), 'bone')
    r.cube('pole', (-0.75, -18.6, -0.75, 1.5, 1.0, 1.5), 'bone')
    r.cube('pole', (-0.7, -12.0, -0.7, 1.4, 0.8, 1.4), 'cord')
    r.part('weight', 'pole', (0, -18.2, -0.8))
    r.cube('weight', (-0.2, 0.0, -0.2, 0.4, 2.4, 0.4), 'cord')
    r.cube('weight', (-1.0, 2.4, -0.8, 2.0, 2.2, 1.6), 'stone')
    return r


def coralline(t):
    """Pale pink flecks of coralline alga, the strain's partner, sparse on the skin."""
    return h(t['gx'] * 1.7, t['gy'] * 1.7, t['gz'] * 1.7) > 0.94


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        # A sleeveless kelp-fibre wrap, open at the throat.
        if t['face'] == 'north' and t['gy'] < 1.4 and abs(t['gx']) < 1.4:
            return flesh(t, PAL['skin'])
        return weave(PAL['cloth'], t)
    if m in ('arm', 'leg') and coralline(t):
        return (226, 170, 172)
    if m == 'leg' and t['gy'] > 23.0:
        return add(flesh(t, PAL['skin']), -30)    # bare soles, rock-worn
    if m == 'wood':
        k = (t['v'] // 2 + t['u']) % 4
        return add((132, 116, 92) if k else (110, 96, 76), (t['n'] - 0.5) * 10)
    if m == 'stone':
        rgb = add((92, 94, 98), (t['n'] - 0.5) * 24)
        return (206, 150, 156) if coralline(t) else rgb
    return paint_base(cube, t, PAL)


POSES = [('rest', {'right_arm': (-14, 0, 0), 'right_forearm': (-34, 0, 0), 'pole': (48, 0, 0)}, ()),
         ('walk', {'right_leg': (30, 0, 0), 'right_shin': (35, 0, 0), 'left_leg': (-30, 0, 0),
                   'right_arm': (-14, 0, 0), 'right_forearm': (-34, 0, 0), 'pole': (48, 0, 0),
                   'left_arm': (22, 0, 0)}, ()),
         ('strike', {'body': (12, -20, 0), 'right_arm': (-80, 0, 0), 'right_forearm': (-10, 0, 0),
                     'pole': (-10, 0, 0), 'left_arm': (-60, 30, 0), 'left_forearm': (-40, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'tidewarden', 'court_tidewarden.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
