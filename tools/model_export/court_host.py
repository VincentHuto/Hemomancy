"""A Host of the Veiled Fold: the member who receives visitors at the cleft's screened door.

An altered humanoid: wears Ink Cap at Mature (the tall shaggy cap over the head, edges blackening and dripping, comes
from the strain layer). The Fold's change shows in the outline: a long ink-dyed over-robe hangs from the shoulders to
the ankles in loose panels, open at the front over a grey under-wrap, with wide hanging sleeves, so where the body ends
is hard to say. Bone-white edging and a pair of bone pins at the collar are the only light colour on it. Pale skin,
dark eyes, short dark hair under the cap. The hands stay folded at the waist: a host, not a guard.

    python tools/model_export/court_host.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, hair_cap, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = PALETTES['veiled_fold']


def rig():
    r = humanoid('host', 'CourtHostModel')
    hair_cap(r, back=3.5)
    wrap_skirt(r, 11.0, material='cloth2')
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cord')
    # The over-robe: panels hung from the shoulders, front pair open, back panel to the heels.
    r.part('robe', 'body', (0, -10, 0))
    r.cube('robe', (-4.5, -0.3, 2.1, 9.0, 23.0, 0.45), 'robe')
    r.cube('robe', (-4.6, -0.3, -2.2, 0.4, 22.5, 4.3), 'robe')
    r.cube('robe', (4.2, -0.3, -2.2, 0.4, 22.5, 4.3), 'robe')
    r.part('robe_front', 'robe', (0, 10.0, -2.3))
    r.cube('robe_front', (-4.5, -10.3, -0.3, 2.6, 22.0, 0.4), 'robe')
    r.cube('robe_front', (1.9, -10.3, -0.3, 2.6, 22.0, 0.4), 'robe')
    # A standing collar with two bone pins.
    r.cube('robe', (-4.4, -1.2, -2.4, 8.8, 1.4, 0.5), 'robe')
    r.cube('robe', (-2.4, -0.6, -2.7, 0.8, 0.8, 0.4), 'bone')
    r.cube('robe', (1.6, -0.6, -2.7, 0.8, 0.8, 0.4), 'bone')
    # Wide hanging sleeves on the forearms.
    for side, sign in (('right', -1), ('left', 1)):
        r.part(f'{side}_sleeve', f'{side}_forearm', (0, 0.0, 0))
        r.cube(f'{side}_sleeve', (-2.6, -0.6, -2.6, 5.2, 5.4, 5.2), 'robe')
        r.cube(f'{side}_sleeve', (-2.6, 4.8, 0.4, 5.2, 2.6, 2.2), 'robe')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        return weave(PAL['cloth2'], t)
    if m == 'arm':
        return weave(PAL['cloth'], t) if t['gy'] < 9.0 else flesh(t, PAL['skin'])
    if m == 'leg':
        return add(weave(PAL['cloth'], t), -6)
    if m == 'robe':
        rgb = weave(PAL['cloth'], t)
        # Bone-white edging along every hem and opening, and a faint grey sheen down the folds.
        if t['side'] and (t['fv'] > 0.94 or t['fu'] < 0.06 or t['fu'] > 0.94):
            return PAL['trim']
        if t['side'] and t['u'] % 5 == 2:
            rgb = add(rgb, 10)
        return rgb
    return paint_base(cube, t, PAL)


POSES = [('rest', {'right_arm': (-26, 18, 0), 'left_arm': (-26, -18, 0), 'right_forearm': (-62, 26, 0),
                   'left_forearm': (-62, -26, 0)}, ()),
         ('answering', {'right_arm': (-30, 0, 8), 'right_forearm': (-50, 0, 0), 'left_arm': (-26, -18, 0),
                        'left_forearm': (-62, -26, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'host', 'court_host.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
