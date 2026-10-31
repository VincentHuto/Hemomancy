"""An Arbor of the Giving Light: a mature member anchored among the fronds, a voice newcomers meet Marimo through.

Wears Sunburst at Primal (the frond canopy comes from the strain layer). The body is crusted in gold-orange lichen
over bark-dark skin, hair grown long and threaded with frond shoots, a short wrap at the hips, and root strands along
the shins where the knees meet the ground. Anchored, not petrified: the eyes are half shut, and the member can still
withdraw (CourtHumanoidModel folds the arms in and bows the head).

    python tools/model_export/court_arbor.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, h, hair_cap, humanoid, mix, paint_base, wrap_skirt, write

PAL = dict(PALETTES['giving_light'], skin=(176, 128, 92), eye=(204, 150, 52))


def rig():
    r = humanoid('arbor', 'CourtArborModel')
    next(c for p in r.parts if p['name'] == 'head' for c in p['cubes']).update(closed=True)
    hair_cap(r, back=6.5)
    r.part('hair_fall', 'hair', (0, -2, 4.0), (8, 0, 0))
    r.cube('hair_fall', (-3.6, 0.0, -0.4, 7.2, 7.0, 0.8), 'hair')
    for i, (x, rot) in enumerate(((-2.6, -14), (0.2, 4), (2.8, 18))):
        name = f'shoot_{i}'
        r.part(name, 'hair', (x, -8.0, 1.0), (-20, 0, rot))
        r.cube(name, (-0.4, -3.2, -0.4, 0.8, 3.2, 0.8), 'shoot')
        r.cube(name, (-1.0, -4.2, -0.3, 2.0, 1.2, 0.6), 'lichen')
    wrap_skirt(r, 4.5, material='cloth2', front_split=True)
    # Root strands down the front of each shin, where the kneeling body takes hold of the ground.
    for side, sign in (('right', -1), ('left', 1)):
        for i, (x, length) in enumerate(((-1.2, 5.0), (0.4, 6.5), (1.4, 4.0))):
            name = f'{side}_root_{i}'
            r.part(name, f'{side}_shin', (x * sign, 0.5, -2.1), (8 + 6 * i, 0, 10 * sign * (i - 1)))
            r.cube(name, (-0.35, 0.0, -0.35, 0.7, length, 0.7), 'root')
    return r


def lichen(t):
    """Crust blotches: dense on the shoulders and back, thinning down the limbs."""
    n = h(t['gx'] * 0.7, t['gy'] * 0.7, t['gz'] * 0.7)
    rgb = mix((214, 132, 34), (240, 178, 62), n)
    if n > 0.82:
        rgb = (252, 214, 112)
    return rgb


def paint(cube, t):
    m = cube['material']
    if m in ('torso', 'arm', 'leg'):
        crust = 0.75 - (t['gy'] - 2.0) * 0.045 + (0.25 if t['face'] == 'south' else 0.0)
        if h(t['gx'], t['gy'], t['gz']) < crust * 0.8:
            return lichen(t)
        return add(flesh(t, PAL['skin']), -10 if (t['u'] + t['v']) % 4 == 0 else 0)
    if m == 'lichen':
        return lichen(t)
    if m == 'shoot':
        return mix((112, 146, 52), (148, 180, 70), t['n'])
    if m == 'root':
        return mix((92, 64, 40), (124, 90, 56), t['n']) if t['v'] % 3 else (210, 140, 50)
    return paint_base(cube, t, PAL)


def glow(cube, t):
    if cube['material'] == 'lichen' and t['n'] > 0.6:
        return (255, 196, 90)
    return None


POSES = [('rest', {}, ()),
         ('kneel', {'right_leg': (-5, 6, 0), 'left_leg': (-5, -6, 0), 'right_shin': (90, 0, 0),
                    'left_shin': (90, 0, 0), 'right_arm': (-26, 0, 31), 'left_arm': (-26, 0, -31),
                    'right_forearm': (-46, 0, 0), 'left_forearm': (-46, 0, 0)}, ()),
         ('withdrawn', {'body': (28, 0, 0), 'head': (31, 0, 0), 'right_arm': (-40, -11, -11),
                        'left_arm': (-40, 11, 11), 'right_forearm': (-97, 34, 0), 'left_forearm': (-97, -34, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'arbor', 'court_arbor.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
