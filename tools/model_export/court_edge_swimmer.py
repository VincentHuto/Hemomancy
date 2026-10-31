"""The Edge-swimmer of the Shared Wake: the fast member who guards the school's edge in open water.

An altered humanoid, barely recognisable as a former person: wears Mermaid's Purse at Apex (the flank purses, the
forearm finlets and fin ridges and the sail along the spine come from the strain layer). The body under them is
streamlined: countershaded cobalt down the back to silver on the flank with a crimson lateral line, a smooth skull
with a low fin ridge and gill slits, trailing fins behind the forearms, flattened fin-feet, and a tapering tail from
the base of the spine ending in a small fluke. Very little clothing: a cord harness with a bone toggle and a short
purse-leather loincloth. It carries a bone lance shaped like a sailfish's bill, the thing it strikes with.

    python tools/model_export/court_edge_swimmer.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, countershade, flesh, humanoid, mix, paint_base, weave, write

PAL = PALETTES['shared_wake']


def rig():
    r = humanoid('edge_swimmer', 'CourtEdgeSwimmerModel')
    # A low fin ridge over the smooth skull, front to back.
    r.part('crest', 'head', (0, -8.0, 0))
    r.cube('crest', (-0.3, -1.2, -2.5, 0.6, 1.2, 6.0), 'fin')
    r.cube('crest', (-0.3, -0.6, 3.5, 0.6, 2.6, 0.9), 'fin')
    # The harness: one cord over the left shoulder to the right hip, a bone toggle on the chest.
    r.part('harness', 'body', (0, -10, 0))
    r.cube('harness', (-3.4, 0.4, -2.4, 1.0, 10.0, 0.4), 'cord')
    r.cube('harness', (2.4, 0.4, 2.0, 1.0, 10.0, 0.4), 'cord')
    r.cube('harness', (-4.2, 8.6, -2.4, 8.4, 0.8, 4.8), 'cord')
    r.cube('harness', (-0.8, 3.0, -2.75, 1.6, 1.0, 0.5), 'bone')
    # A short purse-leather loincloth, front and back panels free to swing with the kick.
    r.part('skirt', 'hips')
    r.part('skirt_front', 'skirt')
    r.cube('skirt_front', (-2.4, -0.5, -2.4, 4.8, 3.6, 0.4), 'cloth2')
    r.part('skirt_back', 'skirt')
    r.cube('skirt_back', (-2.4, -0.5, 2.05, 4.8, 3.0, 0.4), 'cloth2')
    # Trailing fins along the back of each forearm (the strain's ridges run down the outside).
    for side, sign in (('right', -1), ('left', 1)):
        r.part(f'{side}_trail_fin', f'{side}_forearm', (0, 1.0, 2.0), (12, 0, 0))
        r.cube(f'{side}_trail_fin', (-0.25, 0.0, 0.0, 0.5, 5.0, 1.8), 'fin')
        r.cube(f'{side}_trail_fin', (-0.25, 1.0, 1.8, 0.5, 3.0, 0.9), 'fin')
    # Flattened fin-feet reaching past the toes.
    for side in ('right', 'left'):
        r.cube(f'{side}_shin', (-2.2, 5.6, -4.6, 4.4, 0.4, 3.0), 'fin')
        r.cube(f'{side}_shin', (-1.6, 5.6, -5.6, 3.2, 0.4, 1.0), 'fin')
    # The tail: two tapering segments from the base of the spine, then a small upright fluke.
    r.part('tail', 'hips', (0, -1.0, 2.0), (28, 0, 0))
    r.cube('tail', (-1.5, -1.5, 0.0, 3.0, 3.0, 5.0), 'tail')
    r.part('tail_tip', 'tail', (0, 0, 5.0), (8, 0, 0))
    r.cube('tail_tip', (-1.0, -1.0, 0.0, 2.0, 2.0, 4.0), 'tail')
    r.cube('tail_tip', (-0.25, -3.0, 2.6, 0.5, 6.0, 2.2), 'fin')
    # The bone lance, a sailfish's bill: held point forward in the right hand.
    r.part('lance', 'right_forearm', (0, 6.5, 0), (90, 0, 0))
    r.cube('lance', (-0.5, -4.0, -0.5, 1.0, 14.0, 1.0), 'bone')
    r.cube('lance', (-0.35, -9.0, -0.35, 0.7, 5.0, 0.7), 'bone')
    r.cube('lance', (-0.6, 0.6, -0.6, 1.2, 1.6, 1.2), 'cord')
    r.cube('lance', (-0.3, 4.6, -0.6, 0.6, 1.4, 1.2), 'cloth2')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        if t['face'] == 'north':
            px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
            side = px if px < 4 else 7 - px
            if py in (3, 4) and side in (1, 2):
                return (14, 18, 34) if py == 4 else (40, 70, 170)      # large dark eyes
            if py == 6 and px in (3, 4):
                return add(PAL['skin'], -40)
        if t['face'] in ('east', 'west') and t['gy'] > -2.0 and t['u'] % 2 == 0 and 2 <= t['u'] <= 5:
            return add(PAL['line'], -40)                                # gill slits behind the jaw
        return countershade(t, PAL)
    if m in ('torso', 'arm', 'leg', 'tail'):
        if m == 'leg' and t['gy'] > 23.0:
            return add(countershade(t, PAL), -24)
        return countershade(t, PAL)
    if m == 'fin':
        k = 0.5 + 0.5 * (t['fv'] if t['side'] else 0.5)
        rgb = mix((58, 92, 196), (150, 170, 220), k * 0.6)
        return rgb + (232,) if t['u'] % 3 else add(rgb, -24) + (232,)
    if m == 'bone':
        return add((222, 214, 196), (t['n'] - 0.5) * 18)
    return paint_base(cube, t, PAL)


def glow(cube, t):
    return None


POSES = [('rest', {'right_forearm': (-30, 0, 0), 'lance': (60, 0, 0)}, ()),
         ('swim', {'body': (0, 0, 0), 'right_arm': (8, 0, 6), 'left_arm': (8, 0, -6), 'right_leg': (14, 0, 0),
                   'left_leg': (14, 0, 0), 'tail': (10, 0, 0), 'lance': (90, 0, 0)}, ()),
         ('strike', {'body': (14, -16, 0), 'right_arm': (-80, 0, 0), 'right_forearm': (-10, 0, 0),
                     'lance': (90, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'edge_swimmer', 'court_edge_swimmer.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
