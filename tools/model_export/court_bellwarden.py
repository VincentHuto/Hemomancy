"""The Bellwarden of the Still Bell: the one keeper awake in the bell, who keeps its warmth, its stores and its door.

An altered humanoid: wears Hushbell at Mature (the folded mantle wrapping the chest and back comes from the strain
layer). The Bell's change shows in a body kept against the cold: frost-pale skin with rime at the fingertips, grey hair
cropped close, a folded felt collar standing round the throat like the hood ring the sleepers have, and a long quilted
coat of grey felt to the knees, belted with kelp cord. At the belt hang a knotted tally cord (the bell's stores,
counted) and a small sealed lamp: a bone cage round a living sea pickle, the bell's light.

    python tools/model_export/court_bellwarden.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = PALETTES['still_bell']


def rig():
    r = humanoid('bellwarden', 'CourtBellwardenModel')
    # Cropped hair: a close cap.
    r.part('hair', 'head')
    r.cube('hair', (-4.2, -8.25, -4.2, 8.4, 1.0, 8.4), 'hair')
    r.cube('hair', (-4.2, -7.3, 3.7, 8.4, 3.0, 0.5), 'hair')
    # The felt collar, standing round the throat in folds.
    r.part('collar', 'body', (0, -10, 0))
    r.cube('collar', (-4.6, -1.6, -2.6, 9.2, 2.2, 0.6), 'felt')
    r.cube('collar', (-4.6, -1.6, 2.0, 9.2, 2.2, 0.6), 'felt')
    r.cube('collar', (-4.7, -1.6, -2.6, 0.6, 2.2, 5.2), 'felt')
    r.cube('collar', (4.1, -1.6, -2.6, 0.6, 2.2, 5.2), 'felt')
    # The quilted coat to the knees: skirt panels over the thighs, belted.
    wrap_skirt(r, 8.0, material='felt')
    r.cube('hips', (-4.35, -3.7, -2.35, 8.7, 1.3, 4.7), 'cord')
    # The tally cord on the left hip and the sealed lamp on the right.
    r.part('tally', 'hips', (3.4, -2.6, -2.4), (0, 0, -6))
    r.cube('tally', (-0.2, 0.0, -0.2, 0.4, 6.0, 0.4), 'cord')
    for i in range(5):
        r.cube('tally', (-0.45, 0.8 + i * 1.1, -0.45, 0.9, 0.6, 0.9), 'knot')
    r.part('lamp', 'hips', (-4.4, -2.4, -0.4), (0, 0, 6))
    r.cube('lamp', (-0.2, 0.0, -0.2, 0.4, 1.4, 0.4), 'cord')
    r.cube('lamp', (-1.3, 1.4, -1.3, 2.6, 0.4, 2.6), 'bone')
    r.cube('lamp', (-1.0, 1.8, -1.0, 2.0, 2.4, 2.0), 'pickle')
    for x, z in ((-1.3, -1.3), (0.9, -1.3), (-1.3, 0.9), (0.9, 0.9)):
        r.cube('lamp', (x, 1.8, z, 0.4, 2.4, 0.4), 'bone')
    r.cube('lamp', (-1.3, 4.2, -1.3, 2.6, 0.4, 2.6), 'bone')
    # Felt sleeves to the wrists, with the coat's cuffs.
    for side in ('right', 'left'):
        r.cube(f'{side}_forearm', (-2.15, 4.4, -2.15, 4.3, 0.9, 4.3), 'felt')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        rgb = weave(PAL['cloth'], t)
        if t['face'] == 'north' and abs(t['gx']) < 0.5:
            rgb = add(rgb, -24)        # the coat's closed front
        if t['v'] % 4 == 0:
            rgb = add(rgb, -10)        # quilting
        return rgb
    if m == 'arm':
        if t['gy'] < 8.0:
            return add(weave(PAL['cloth'], t), -6 if t['v'] % 4 == 0 else 0)
        rgb = flesh(t, PAL['skin'])
        return mix(rgb, PAL['frost'], 0.6) if t['gy'] > 10.4 else rgb      # rime at the fingertips
    if m == 'leg':
        return weave(PAL['cloth2'], t) if t['gy'] < 21.0 else add(PAL['lacquer'], (t['n'] - 0.5) * 10)
    if m == 'felt':
        rgb = weave(PAL['cloth'], t)
        if t['v'] % 3 == 0:
            rgb = add(rgb, -14)
        if t['side'] and t['fv'] < 0.1:
            rgb = mix(rgb, PAL['frost'], 0.5)
        return rgb
    if m == 'knot':
        return add(PAL['cord'], 20 if (t['u'] + t['v']) % 2 else -10)
    if m == 'pickle':
        return mix((120, 150, 60), (190, 210, 110), t['n'])
    return paint_base(cube, t, PAL)


def glow(cube, t):
    if cube['material'] == 'pickle':
        return mix((170, 220, 120), (230, 250, 190), t['n'])
    return None


POSES = [('rest', {}, ()),
         ('exact', {'right_arm': (-40, 0, 6), 'right_forearm': (-70, 0, 0), 'left_arm': (-6, 0, -4)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'bellwarden', 'court_bellwarden.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
