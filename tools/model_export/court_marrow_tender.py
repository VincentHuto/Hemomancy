"""A Marrow-tender of Last Marrow: the mobile member who cares for the rooted elders and harvests the whale fall.

Wears Marrowplume at Mature (the clusters of red club-stalks with feathery crowns along the forearms and the pale
sheaths beading red at the wrists come from the strain layer). Under it: weathered skin, dark hair knotted up and
pinned with a bone pin, a sleeveless hide jerkin and a wrapped hide skirt with a red-dyed sash, a shoulder guard cut
from a whale's scapula on the left, a hide satchel at the hip for what the fall gives, and a bone flensing hook in the
right hand. Practical, worn, unadorned: every piece is made from something that was once alive and then of use.

    python tools/model_export/court_marrow_tender.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, anklets, flesh, hair_cap, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = PALETTES['last_marrow']


def rig():
    r = humanoid('marrow_tender', 'CourtMarrowTenderModel')
    hair_cap(r, back=3.0)
    # The hair knotted up at the crown and pinned through with bone.
    r.cube('hair', (-1.6, -10.0, -0.4, 3.2, 1.8, 3.2), 'hair')
    r.cube('hair', (-2.6, -9.4, 0.8, 5.2, 0.6, 0.6), 'bone')
    wrap_skirt(r, 6.5, material='cloth')
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cloth2')
    # The scapula guard over the left shoulder, lashed on.
    r.part('guard', 'left_arm', (0.6, -2.4, 0), (0, 0, -12))
    r.cube('guard', (-2.4, -0.8, -2.6, 4.8, 1.0, 5.2), 'bone')
    r.cube('guard', (1.6, -0.2, -2.6, 0.8, 3.4, 5.2), 'bone')
    r.cube('guard', (-2.5, 0.2, -0.3, 5.0, 0.5, 0.6), 'cord')
    # The satchel on the right hip, strap over the left shoulder.
    r.part('strap', 'body', (0, -10, 0))
    for i in range(5):
        x = 1.6 - i * 1.4
        r.cube('strap', (x, 0.2 + i * 2.0, -2.45, 1.4, 2.2, 0.4), 'cord')
        r.cube('strap', (x, 0.2 + i * 2.0, 2.05, 1.4, 2.2, 0.4), 'cord')
    r.part('satchel', 'hips', (-4.5, -2.6, 0.0), (0, 0, 8))
    r.cube('satchel', (-1.6, 0.0, -2.0, 1.6, 3.4, 4.0), 'hide')
    r.cube('satchel', (-1.7, -0.2, -2.1, 1.8, 1.0, 4.2), 'hide')
    r.cube('satchel', (-1.8, 0.6, -0.4, 0.3, 0.8, 0.8), 'bone')
    anklets(r, material='cord')
    # The flensing hook: a short bone haft with a curved blade, carried point down.
    r.part('hook', 'right_forearm', (0, 5.6, -0.5))
    r.cube('hook', (-0.5, -2.0, -0.5, 1.0, 7.0, 1.0), 'bone')
    r.cube('hook', (-0.6, -1.0, -0.6, 1.2, 2.0, 1.2), 'cord')
    r.cube('hook', (-0.35, 4.6, -2.4, 0.7, 0.9, 2.4), 'bone')
    r.cube('hook', (-0.3, 3.2, -2.9, 0.6, 1.8, 0.7), 'bone')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        # The hide jerkin, laced up the front over bare collarbones.
        if t['gy'] < 1.4 and t['face'] in ('north', 'south'):
            return flesh(t, PAL['skin'])
        rgb = add(PAL['cloth'], (t['n'] - 0.5) * 18)
        if t['face'] == 'north' and abs(t['gx']) < 0.5 and t['v'] % 2 == 0:
            rgb = PAL['cord']
        return rgb
    if m == 'arm':
        return flesh(t, PAL['skin'])
    if m == 'leg':
        return flesh(t, add(PAL['skin'], -10)) if t['gy'] > 18.0 else add(PAL['cloth'], -16 + (t['n'] - 0.5) * 12)
    if m == 'cloth':
        rgb = add(PAL['cloth'], (t['n'] - 0.5) * 18)
        return add(rgb, -20) if t['side'] and t['fv'] > 0.9 else rgb
    if m == 'hide':
        return add(mix(PAL['cloth'], (110, 84, 62), 0.5), (t['n'] - 0.5) * 16)
    if m == 'bone':
        rgb = add(PAL['trim'], (t['n'] - 0.5) * 16)
        return mix(rgb, PAL['marrow'], 0.5) if (t['u'] * 3 + t['v'] * 5) % 17 == 0 else rgb   # beads of red
    return paint_base(cube, t, PAL)


POSES = [('rest', {}, ()),
         ('walk', {'right_leg': (30, 0, 0), 'right_shin': (35, 0, 0), 'left_leg': (-30, 0, 0),
                   'right_arm': (-20, 0, 0), 'left_arm': (20, 0, 0)}, ()),
         ('tending', {'body': (20, 0, 0), 'left_arm': (-60, 0, -6), 'left_forearm': (-30, 0, 0),
                      'right_arm': (-35, 0, 0), 'right_forearm': (-20, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'marrow_tender', 'court_marrow_tender.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
