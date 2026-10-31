"""The Kindled Champion: the title's holder, a Kindled elder grown strongest of the Kindling and still a person
(Brined Court Track 10, Tier 2).

Track 9's Kindled elder body (court_kindled_elder.py) with what holding the title has made of it: two rings of crown
buds set between the eight crown tentacles, which the model shows four at a time as the Champion buds at each
threshold of the fight; a longer, heavier mane; healed sting scars as pale weals over the vessels; and a deeper flame
at the crown's tips. Nothing burns: the tips carry biological light.

    python tools/model_export/court_kindled_champion.py [--preview]
"""
import argparse
import math

import court_kindled_elder as elder
from court_humanoid import add, h, mix, write

BUDS = 8


def rig():
    r = elder.rig()
    r.name, r.java_class, r.layer = 'court_kindled_champion', 'CourtKindledChampionModel', 'court_kindled_champion'
    # Two rings of buds between the crown tentacles: the inner four come first, the outer four at the second threshold.
    for i in range(BUDS):
        ring = i // 4
        a = 2 * math.pi * (i % 4 + 0.5 + ring * 0.0) / 4 + ring * math.pi / 4
        cx, cz = math.cos(a), math.sin(a)
        radius = 1.4 if ring == 0 else 2.4
        name = f'bud_{i}'
        r.part(name, 'head', (radius * cx, -4.0, radius * cz), (-(30 + ring * 15) * cz, 0, (30 + ring * 15) * cx))
        r.cube(name, (-0.5, -3.0 - ring, -0.5, 1.0, 3.2 + ring, 1.0), 'mane', seg=1)
    return r


def scar(t):
    return abs(math.sin(t['gx'] * 0.9 - t['gy'] * 1.6 + t['gz'] * 0.5)) < 0.05 and h(t['gx'], t['gy'], t['gz']) > 0.45


def paint(cube, t):
    if cube['material'] == 'polyp' and scar(t):
        return mix((246, 232, 226), (236, 220, 214), t['n'])
    rgb = elder.paint(cube, t)
    if cube['material'] == 'mane' and cube['seg'] == 1:
        rgb = mix(rgb, (190, 22, 26), 0.25)
    return rgb


def glow(cube, t):
    if cube['material'] == 'mane' and cube['seg'] == 1 and t['fv'] > 0.35:
        return mix((255, 176, 72), (255, 84, 46), t['fv'])
    return elder.glow(cube, t)


POSES = [('rest', {}, ()),
         ('sweep', {'neck_0': (40, 0, 0), 'neck_1': (20, 40, 0), 'neck_2': (10, 30, 0), 'head': (20, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'kindled_champion', 'court_kindled_champion.py', paint, glow, POSES, args.preview, base_rig=False)


if __name__ == '__main__':
    main()
