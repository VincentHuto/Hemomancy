"""The Lightkeeper: the Lighthouse Garden's named lead, keeper of the bloom-lamp and the Court's fifth-Degree teacher.

Wears Sunburst at Apex, fronds open (the strain layer draws them). A long linen robe over a saffron underskirt, a
lacquered frond crest worn like a headdress, a green-and-gold stole, and a bone staff from which a living bloom pod
hangs on a cord: the lamp the Garden is named for, carried down from the lighthouse.

    python tools/model_export/court_lightkeeper.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, cuffs, flesh, hair_cap, humanoid, mix, paint_base, tabard, weave, wrap_skirt, write

PAL = dict(PALETTES['giving_light'], skin=(206, 160, 118), hair=((214, 210, 200), (180, 176, 168), (236, 234, 228)))


def rig():
    r = humanoid('lightkeeper', 'CourtLightkeeperModel')
    hair_cap(r, back=5.0)
    wrap_skirt(r, 11.5, material='cloth')
    tabard(r, 13.0, material='cloth2')
    cuffs(r, material='trim')
    # The stole: two bands down the chest from the shoulders.
    r.cube('body', (-3.4, -10.2, -2.4, 1.2, 9.0, 0.4), 'trim')
    r.cube('body', (2.2, -10.2, -2.4, 1.2, 9.0, 0.4), 'trim')
    # A fan of lacquered fronds above the brow, like the lamp's own bloom.
    r.part('crest', 'head', (0, -8, -1.5))
    for i, angle in enumerate((-40, -20, 0, 20, 40)):
        name = f'crest_{i}'
        r.part(name, 'crest', (0, 0, 0), (0, 0, angle))
        r.cube(name, (-0.6, -4.5 + abs(angle) / 20.0, -0.3, 1.2, 4.5 - abs(angle) / 20.0, 0.6), 'lacquer')
        r.cube(name, (-0.3, -5.2 + abs(angle) / 20.0, -0.35, 0.6, 0.8, 0.7), 'frond_tip')
    # The staff, held upright in the right hand, and the bloom pod hanging from its crook.
    r.part('staff', 'right_forearm', (0, 5.5, -0.5))
    r.cube('staff', (-0.55, -17.0, -0.55, 1.1, 24.0, 1.1), 'bone')
    r.cube('staff', (-0.7, -17.6, -0.7, 1.4, 0.8, 1.4), 'cord')
    r.cube('staff', (-0.5, -18.6, -3.0, 1.0, 1.0, 3.0), 'bone')
    r.part('lamp', 'staff', (0, -18.0, -2.6))
    r.cube('lamp', (-0.2, 0.0, -0.2, 0.4, 2.0, 0.4), 'cord')
    r.cube('lamp', (-1.6, 2.0, -1.6, 3.2, 3.4, 3.2), 'pod')
    r.cube('lamp', (-1.1, 5.4, -1.1, 2.2, 0.8, 2.2), 'pod')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        return weave(PAL['cloth'], t)
    if m == 'arm':
        return weave(PAL['cloth'], t) if t['gy'] < 9.0 else flesh(t, PAL['skin'])
    if m == 'frond_tip':
        return (246, 186, 64)
    if m == 'pod':
        rgb = mix((236, 150, 36), (254, 214, 100), t['n'])
        return add(rgb, 20) if (t['u'] + t['v']) % 3 == 0 else rgb
    return paint_base(cube, t, PAL)


def glow(cube, t):
    if cube['material'] == 'pod':
        return mix((255, 190, 80), (255, 238, 160), t['n'])
    if cube['material'] == 'frond_tip':
        return (255, 200, 96)
    return None


POSES = [('rest', {'right_arm': (-14, 0, 0), 'right_forearm': (-34, 0, 0), 'staff': (48, 0, 0)}, ()),
         ('speaking', {'right_arm': (-14, 0, 0), 'right_forearm': (-34, 0, 0), 'staff': (48, 0, 0),
                       'left_arm': (-32, 0, -9), 'left_forearm': (-54, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'lightkeeper', 'court_lightkeeper.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
