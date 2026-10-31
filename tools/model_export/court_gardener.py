"""The Gardener of the Giving Light: the mobile member who feeds visitors and teaches the first cultivation steps.

Wears Sunburst at Mature (the crust and first frond buds come from the strain layer, not this model). A wide woven sun
hat, a sleeveless linen tunic, a saffron wrap skirt with a green sash, cord cuffs and a reed basket of glowing bloom
fruit at the hip. Warm, open and practical: the Court's welcoming face.

    python tools/model_export/court_gardener.py            # model, textures, Blockbench project
    python tools/model_export/court_gardener.py --preview  # also build/court-npc-preview/gardener.png
"""
import argparse

from court_humanoid import (PALETTES, add, anklets, cuffs, flesh, hair_cap, humanoid, mix, paint_base, sash, weave,
                            wrap_skirt, write)

PAL = PALETTES['giving_light']


def rig():
    r = humanoid('gardener', 'CourtGardenerModel')
    hair_cap(r, back=3.0)
    wrap_skirt(r, 7.5)
    sash(r)
    cuffs(r)
    anklets(r)
    # A wide, shallow sun hat of woven reed: stepped rings over the crown.
    r.part('hat', 'head', (0, -8, 0), (-4, 0, 0))
    r.cube('hat', (-7.0, -0.6, -7.0, 14.0, 0.6, 14.0), 'reed')
    r.cube('hat', (-4.6, -1.6, -4.6, 9.2, 1.0, 9.2), 'reed')
    r.cube('hat', (-2.6, -2.6, -2.6, 5.2, 1.0, 5.2), 'reed')
    r.cube('hat', (-1.0, -3.2, -1.0, 2.0, 0.6, 2.0), 'cord')
    r.cube('hat', (-4.7, -1.0, -4.7, 9.4, 0.4, 9.4), 'trim')
    # A reed basket on the left hip, heaped with bloom fruit that glows.
    r.part('basket', 'hips', (4.3, -1.5, 0.0), (0, 0, -6))
    r.cube('basket', (0.0, 0.0, -2.0, 2.6, 3.2, 4.0), 'reed')
    r.cube('basket', (-0.1, -0.3, -2.1, 2.8, 0.5, 4.2), 'cord')
    r.cube('basket', (0.4, -0.9, -1.5, 1.2, 1.0, 1.2), 'fruit')
    r.cube('basket', (1.1, -0.8, 0.2, 1.1, 0.9, 1.1), 'fruit')
    r.cube('basket', (0.5, -0.7, 0.9, 1.0, 0.8, 1.0), 'fruit')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'torso':
        # Sleeveless linen tunic, a shallow neck opening; bare shoulders show the crust the strain grows there.
        if t['face'] == 'north' and t['gy'] < 1.6 and abs(t['gx']) < 1.6 - t['gy'] * 0.5:
            return flesh(t, PAL['skin'])
        if t['gy'] > 9.2:
            return weave(PAL['cloth2'], t)
        return weave(PAL['cloth'], t)
    if m == 'arm' and t['gy'] < 2.4 and not cube.get('filler'):
        return weave(PAL['cloth'], t)          # the tunic's cap over the shoulder
    if m == 'fruit':
        return mix((238, 150, 40), (252, 206, 84), t['n'])
    if m == 'leg' and t['gy'] > 23.0:
        return add(flesh(t, PAL['skin']), -26)  # bare soles, dusty
    return paint_base(cube, t, PAL)


def glow(cube, t):
    if cube['material'] == 'fruit':
        return mix((255, 186, 70), (255, 230, 140), t['n'])
    return None


POSES = [('rest', {}, ()),
         ('walk', {'right_leg': (30, 0, 0), 'right_shin': (35, 0, 0), 'left_leg': (-30, 0, 0),
                   'right_arm': (-20, 0, 0), 'left_arm': (20, 0, 0), 'right_forearm': (-25, 0, 0)}, ()),
         ('tending', {'body': (22, 0, 0), 'right_arm': (-55, 0, 0), 'left_arm': (-50, 0, 0),
                      'right_forearm': (-30, 0, 0), 'left_forearm': (-30, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'gardener', 'court_gardener.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
