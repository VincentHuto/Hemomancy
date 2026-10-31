"""A Sleeper of the Still Bell: an elder sealed in the bell's ring, hung from the dome until the bell needs them.

An altered humanoid gone into the Bell's mature form: wears Hushbell at Primal (the oversized bell mantle, its hood
ring and the seal that closes over the head come from the strain layer). Under it the sleeper is already wrapped: the
body's own folded mantle in layered frost-grey folds from the shoulders to the ankles, the arms crossed on the chest
inside it, the face pale and still with the eyes shut and rime on the brows. A cord harness under the arms hangs the
sleeper from its bone hook in the dome. Awakening one is a communal event (Track 11); this body only breathes, slowly.

    python tools/model_export/court_sleeper.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, eye_face, flesh, humanoid, mix, paint_base, weave, write

PAL = dict(PALETTES['still_bell'], skin=(196, 204, 214))


def rig():
    r = humanoid('sleeper', 'CourtSleeperModel')
    for p in r.parts:
        if p['name'] == 'head':
            p['cubes'][0]['closed'] = True
    r.part('hair', 'head')
    r.cube('hair', (-4.25, -8.3, -4.25, 8.5, 1.4, 8.5), 'hair')
    r.cube('hair', (-4.25, -7.0, 3.6, 8.5, 6.0, 0.7), 'hair')
    # The body's own folded mantle: overlapping layers from the shoulders down, each a little wider than the last.
    r.part('mantle', 'body', (0, -10, 0))
    for i, (y, length, grow) in enumerate(((-0.6, 8.0, 0.0), (5.0, 8.0, 0.4), (10.6, 8.0, 0.8), (16.2, 7.0, 1.1))):
        x0, z0 = -4.6 - grow, -2.6 - grow
        w, d = 9.2 + 2 * grow, 5.2 + 2 * grow
        r.cube('mantle', (x0, y, z0, w, length, 0.5), 'fold', layer=i)
        r.cube('mantle', (x0, y, z0 + d - 0.5, w, length, 0.5), 'fold', layer=i)
        r.cube('mantle', (x0, y, z0, 0.5, length, d), 'fold', layer=i)
        r.cube('mantle', (x0 + w - 0.5, y, z0, 0.5, length, d), 'fold', layer=i)
    # A low hood ring round the neck.
    r.cube('mantle', (-4.9, -1.8, -2.9, 9.8, 1.6, 5.8), 'fold', layer=0)
    # The harness: a cord under the arms and up behind the head to the hook.
    r.cube('body', (-4.4, -8.6, -2.4, 8.8, 0.8, 4.8), 'cord')
    r.part('hang_cord', 'body', (0, -9.0, 2.3))
    r.cube('hang_cord', (-0.3, -12.0, -0.3, 0.6, 12.0, 0.6), 'cord')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        rgb = eye_face(t, PAL, closed=True)
        if t['face'] == 'north' and int(t['ly'] + 8) == 3:
            return mix(rgb, PAL['frost'], 0.7)     # rime on the brows
        return rgb
    if m in ('torso', 'arm', 'leg'):
        return weave(PAL['cloth2'], t)
    if m == 'fold':
        layer = cube.get('layer', 0)
        rgb = add(mix(PAL['cloth'], PAL['frost'], 0.12 * layer), (t['n'] - 0.5) * 12)
        if t['side'] and t['fv'] > 0.86:
            rgb = add(rgb, -26)        # each fold's shadowed lower edge
        if t['side'] and t['fv'] < 0.08:
            rgb = mix(rgb, PAL['frost'], 0.6)
        return rgb
    return paint_base(cube, t, PAL)


POSES = [('rest', {}, ()),
         ('sealed', {'body': (6, 0, 0), 'head': (28, 0, 0), 'right_arm': (-30, -24, -8), 'left_arm': (-30, 24, 8),
                     'right_forearm': (-100, 36, 0), 'left_forearm': (-100, -36, 0), 'right_leg': (-6, 0, 2),
                     'left_leg': (-6, 0, -2), 'right_shin': (12, 0, 0), 'left_shin': (12, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'sleeper', 'court_sleeper.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
