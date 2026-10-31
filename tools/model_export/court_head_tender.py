"""The Head-tender: the Vent Sanctum's named lead, who keeps the threshold and speaks for the Sanctum.

Wears Kinbud at Apex, the long mane down the back (the strain layer draws it). Formal and severe: a tall stiff collar of
black lacquer, a floor-length basalt-black robe opened over crimson panels, ash-grey wrappings at the forearms, and a
long bone crook in the left hand, the one that marks who may walk the threshold.

    python tools/model_export/court_head_tender.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, cuffs, flesh, humanoid, mix, paint_base, tabard, weave, wrap_skirt, write

PAL = dict(PALETTES['kindling'], skin=(232, 216, 210))


def rig():
    r = humanoid('head_tender', 'CourtHeadTenderModel')
    wrap_skirt(r, 12.0, material='cloth')
    tabard(r, 14.0, material='cloth2', back=False)
    cuffs(r, material='ash')
    for side in ('right', 'left'):
        r.cube(f'{side}_forearm', (-2.15, 1.0, -2.15, 4.3, 3.6, 4.3), 'ash')
    # The stiff collar: three lacquer plates standing round the neck, open at the front.
    r.part('collar', 'body', (0, -10, 0))
    r.cube('collar', (-4.4, -4.0, 2.0, 8.8, 4.5, 0.7), 'lacquer')
    r.cube('collar', (-4.6, -3.5, -2.2, 0.7, 4.0, 4.4), 'lacquer')
    r.cube('collar', (3.9, -3.5, -2.2, 0.7, 4.0, 4.4), 'lacquer')
    r.cube('collar', (-4.4, -4.4, 2.0, 8.8, 0.5, 0.8), 'trim')
    # The crook: a long bone staff hooked at the top, carried in the left hand.
    r.part('crook', 'left_forearm', (0, 5.5, -0.5))
    r.cube('crook', (-0.5, -20.0, -0.5, 1.0, 27.0, 1.0), 'bone')
    r.cube('crook', (-0.5, -21.0, -0.5, 1.0, 1.0, 3.6), 'bone')
    r.cube('crook', (-0.5, -20.0, 2.1, 1.0, 2.4, 1.0), 'bone')
    r.cube('crook', (-0.7, -14.0, -0.7, 1.4, 1.2, 1.4), 'cord')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        return weave(PAL['cloth'], t)
    if m == 'arm':
        return weave(PAL['cloth'], t) if t['gy'] < 6.5 else flesh(t, PAL['skin'])
    if m == 'ash':
        return weave((150, 144, 140), t)
    if m == 'trim':
        return mix((214, 132, 44), (196, 48, 34), t['fu'])   # amber running to crimson, the mane's own colours
    return paint_base(cube, t, PAL)


POSES = [('rest', {'left_arm': (-12, 0, 0), 'left_forearm': (-30, 0, 0), 'crook': (42, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'head_tender', 'court_head_tender.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
