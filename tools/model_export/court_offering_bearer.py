"""An Offering-bearer of the Kindling: the member who carries food down to the vents and feeds the heads.

Wears Kinbud at Mature, mane showing at the nape (the strain layer draws it). Shaven pale scalp, a basalt-black
lacquered tabard over a deep red wrap, a cord sash and a string of bone beads, and a stone offering bowl held before
the chest in both hands, heaped with what is going down to the heads.

    python tools/model_export/court_offering_bearer.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, anklets, flesh, humanoid, mix, paint_base, sash, tabard, weave, wrap_skirt, write

PAL = PALETTES['kindling']


def rig():
    r = humanoid('offering_bearer', 'CourtOfferingBearerModel')
    wrap_skirt(r, 9.0, material='cloth2', front_split=True)
    tabard(r, 11.0, material='lacquer')
    sash(r, material='cord')
    anklets(r)
    # Bone beads across the collarbones.
    for i in range(7):
        x = -3.0 + i
        r.cube('body', (x - 0.35, -9.6 + abs(i - 3) * -0.25 + 0.75, -2.6, 0.7, 0.7, 0.6), 'bone')
    # The offering bowl, carried at the chest.
    r.part('bowl', 'body', (0, -5.0, -5.6))
    r.cube('bowl', (-3.0, 0.0, -2.5, 6.0, 1.6, 5.0), 'stone')
    r.cube('bowl', (-3.4, -0.6, -2.9, 6.8, 0.6, 5.8), 'stone')
    r.cube('bowl', (-2.2, -1.2, -1.8, 4.4, 0.8, 3.6), 'offering')
    r.cube('bowl', (-1.0, -1.9, -0.9, 2.0, 0.8, 1.8), 'offering')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        return weave(PAL['cloth2'], t) if t['gy'] > 8.5 else flesh(t, PAL['skin'])
    if m == 'arm' and t['gy'] > 9.0:
        return add(flesh(t, PAL['skin']), -8)
    if m == 'face':
        rgb = paint_base(cube, t, PAL)
        # A shaven scalp: the top and back of the head go the same pale as the skin, with a faint flush of veins.
        if t['face'] in ('down', 'south') and t['n'] > 0.88:
            rgb = mix(rgb, (190, 70, 64), 0.25)
        return rgb
    if m == 'offering':
        return mix((150, 104, 66), (186, 132, 82), t['n']) if t['n'] < 0.75 else (94, 140, 64)
    return paint_base(cube, t, PAL)


POSES = [('carrying', {'right_arm': (-46, 0, -10), 'left_arm': (-46, 0, 10), 'right_forearm': (-34, 20, 0),
                       'left_forearm': (-34, -20, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'offering_bearer', 'court_offering_bearer.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
