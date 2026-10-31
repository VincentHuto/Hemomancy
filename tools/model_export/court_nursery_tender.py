"""The Nursery tender of the Shared Wake: the member who stays at the nursery ground and raises the young.

An altered humanoid, less far along than the Edge-swimmer: wears Mermaid's Purse at Mature (the flank purses, forearm
finlets and fin ridges come from the strain layer). Still plainly a person: long dark hair tied back in a tail with
sinew cord, the cobalt countershading only begun across the shoulders and back, a short tail stub at the base of the
spine. A sleeveless top and a short wrap of woven sea-grass with a crimson purse-leather sash, and a net sling across
the body holding three nest cups (the bird's-nest fungus the purses are named for) with pale young inside them.

    python tools/model_export/court_nursery_tender.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, countershade, flesh, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = dict(PALETTES['shared_wake'], skin=(196, 176, 162))


def rig():
    r = humanoid('nursery_tender', 'CourtNurseryTenderModel')
    # Hair: a cap over the crown, tied back into a long tail with two cord bands.
    r.part('hair', 'head')
    r.cube('hair', (-4.25, -8.3, -4.25, 8.5, 1.6, 8.5), 'hair')
    r.cube('hair', (-4.25, -6.9, 3.6, 8.5, 4.0, 0.7), 'hair')
    r.cube('hair', (-4.3, -6.9, -3.6, 0.6, 3.0, 7.2), 'hair')
    r.cube('hair', (3.7, -6.9, -3.6, 0.6, 3.0, 7.2), 'hair')
    r.part('hair_tail', 'hair', (0, -5.5, 4.2), (14, 0, 0))
    r.cube('hair_tail', (-1.2, 0.0, -0.6, 2.4, 9.0, 1.2), 'hair')
    r.cube('hair_tail', (-1.4, 0.4, -0.7, 2.8, 0.7, 1.4), 'cord')
    r.cube('hair_tail', (-1.4, 6.0, -0.7, 2.8, 0.7, 1.4), 'cord')
    wrap_skirt(r, 5.5, material='cloth')
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cloth2')
    # The net sling: a strap over the right shoulder, the net bag on the left hip with three nest cups in it.
    r.part('sling', 'body', (0, -10, 0))
    r.cube('sling', (-3.6, 0.2, -2.45, 1.0, 9.0, 0.4), 'cord')
    r.cube('sling', (-3.6, 0.2, 2.05, 1.0, 9.0, 0.4), 'cord')
    r.part('net', 'hips', (4.4, -2.0, -0.2), (0, 0, -8))
    r.cube('net', (0.0, 0.0, -2.2, 2.4, 4.0, 4.4), 'net')
    for i, z in enumerate((-1.7, -0.2, 1.3)):
        r.cube('net', (0.3, -0.8 + 0.2 * i, z, 1.6, 1.4, 1.4), 'cup')
        r.cube('net', (0.7, -1.0 + 0.2 * i, z + 0.4, 0.8, 0.4, 0.6), 'egg')
    # A short tail stub at the base of the spine.
    r.part('tail', 'hips', (0, -1.0, 2.0), (34, 0, 0))
    r.cube('tail', (-1.2, -1.2, 0.0, 2.4, 2.4, 3.6), 'tail')
    r.cube('tail', (-0.25, -1.8, 2.4, 0.5, 3.6, 1.6), 'fin')
    # Webbed fin-edges at the heels, the start of the Edge-swimmer's feet.
    for side in ('right', 'left'):
        r.cube(f'{side}_shin', (-2.1, 5.6, -3.4, 4.2, 0.4, 1.6), 'fin')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        # Sleeveless sea-grass top, open at the throat; shoulders bare where the cobalt has begun.
        if t['gy'] < 1.2 or (t['face'] == 'north' and t['gy'] < 2.2 and abs(t['gx']) < 1.6):
            return countershade(t, PAL, 0.45)
        return weave(PAL['cloth'], t)
    if m in ('arm', 'tail'):
        return countershade(t, PAL, 0.45)
    if m == 'leg':
        rgb = countershade(t, PAL, 0.3)
        return add(rgb, -24) if t['gy'] > 23.0 else rgb
    if m == 'fin':
        rgb = mix((58, 92, 196), (150, 170, 220), 0.4 + 0.3 * t['fv'])
        return rgb + (226,)
    if m == 'net':
        if (t['u'] + t['v']) % 2 == 0 or (t['u'] - t['v']) % 4 == 0:
            return PAL['cord']
        return None
    if m == 'cup':
        # The nest cup: a grey-brown fungal funnel, darker inside the rim.
        rgb = add((132, 116, 96), (t['n'] - 0.5) * 18)
        return add(rgb, -36) if t['face'] == 'up' else rgb
    if m == 'egg':
        return add((214, 210, 196), (t['n'] - 0.5) * 12)
    return paint_base(cube, t, PAL)


POSES = [('rest', {}, ()),
         ('walk', {'right_leg': (30, 0, 0), 'right_shin': (35, 0, 0), 'left_leg': (-30, 0, 0),
                   'right_arm': (-20, 0, 0), 'left_arm': (20, 0, 0)}, ()),
         ('tending', {'body': (24, 0, 0), 'right_arm': (-60, 0, 0), 'left_arm': (-55, 0, 0),
                      'right_forearm': (-25, 0, 0), 'left_forearm': (-25, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'nursery_tender', 'court_nursery_tender.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
