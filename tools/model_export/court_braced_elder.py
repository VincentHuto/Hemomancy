"""A Braced elder of the Holding Stone: cemented across the refuge's entrance by masses of gripping growth.

Wears Holdfast at Primal (the oversized root masses at the hands and calves come from the strain layer, whose anchor
strands spread into the ground while it holds). The body under them is weathered to the colour of the rock, crusted
grey-brown and flecked with coralline pink; long grey hair bound back with kelp cord; a heavy kelp-fibre mantle down
the back; a coralline-dyed wrap; knots of holdfast on the shoulders; and root strands running from the ankles into the
stone it stands on and from the hands into the walls it grips. Anchored, not petrified: the elder can still let go
(CourtHumanoidModel's withdrawal, which this role turns into folding the arms across the doorway).

    python tools/model_export/court_braced_elder.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, h, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = dict(PALETTES['holding_stone'], skin=(150, 128, 110), hair=((168, 162, 152), (132, 126, 118), (196, 190, 180)))


def rig():
    r = humanoid('braced_elder', 'CourtBracedElderModel')
    # Long grey hair bound back with kelp cord, falling down between the shoulders.
    r.part('hair', 'head')
    r.cube('hair', (-4.25, -8.3, -4.25, 8.5, 1.6, 8.5), 'hair')
    r.cube('hair', (-4.25, -6.9, 3.6, 8.5, 6.9, 0.7), 'hair')
    r.cube('hair', (-4.3, -6.9, -3.6, 0.6, 3.6, 7.2), 'hair')
    r.cube('hair', (3.7, -6.9, -3.6, 0.6, 3.6, 7.2), 'hair')
    r.part('hair_fall', 'hair', (0, -1.0, 4.0), (6, 0, 0))
    r.cube('hair_fall', (-1.6, 0.0, -0.5, 3.2, 8.0, 1.0), 'hair')
    r.cube('hair_fall', (-1.8, 0.6, -0.6, 3.6, 0.8, 1.2), 'cord')
    r.cube('hair_fall', (-1.8, 5.4, -0.6, 3.6, 0.8, 1.2), 'cord')
    # A heavy kelp mantle hung from the shoulders down the back.
    r.part('mantle', 'body', (0, -10, 2.2), (4, 0, 0))
    r.cube('mantle', (-4.6, -0.4, 0.0, 9.2, 15.0, 0.6), 'kelp')
    r.cube('mantle', (-4.8, -0.8, -0.4, 9.6, 1.4, 1.2), 'cord')
    wrap_skirt(r, 5.0, material='cloth2', front_split=True)
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cord')
    # Knots of holdfast at the shoulders: the growth's old start, now as broad as the arm.
    for side, sign in (('right', -1), ('left', 1)):
        x = -3.4 if sign < 0 else -0.6
        r.cube(f'{side}_arm', (x, -2.6, -2.4, 4.0, 2.4, 4.8), 'knot')
        r.cube(f'{side}_arm', (x + 0.6 * sign, -1.0, -2.6, 2.8, 1.6, 1.0), 'knot')
    # Grip strands from each hand into the wall, splayed so they reach past the fingers.
    for side, sign in (('right', -1), ('left', 1)):
        for i, (z, spread, length) in enumerate(((-1.3, -18, 4.0), (0.0, 4, 5.0), (1.3, 22, 3.6))):
            name = f'{side}_grip_{i}'
            r.part(name, f'{side}_forearm', (sign * 1.2, 7.6, z), (spread * 0.4, 0, sign * (24 + 8 * i)))
            r.cube(name, (-0.4, 0.0, -0.4, 0.8, length, 0.8), 'root')
    # Root strands from each ankle into the stone, cemented: they run out and down past the sole.
    for side, sign in (('right', -1), ('left', 1)):
        r.cube(f'{side}_shin', (-2.4, 3.6, -2.4, 4.8, 2.4, 4.8), 'knot')
        for i, (x, z, yaw, length) in enumerate(((1.0, -1.6, -30, 4.0), (-1.4, -1.0, 40, 3.6),
                                                  (0.2, 1.8, 170, 3.2), (1.8, 0.6, -95, 3.8))):
            name = f'{side}_foot_root_{i}'
            r.part(name, f'{side}_shin', (x * sign, 5.4, z), (0, yaw * sign, 0))
            r.part(name + '_down', name, (0, 0, 0), (-62, 0, 0))
            r.cube(name + '_down', (-0.4, 0.0, -0.4, 0.8, length, 0.8), 'root')
    return r


def crust(t):
    """Grey-brown stone crust, the colour of the rock the elder grips, flecked with pale coralline pink."""
    n = h(t['gx'] * 0.6, t['gy'] * 0.6, t['gz'] * 0.6)
    rgb = mix((104, 96, 86), (138, 128, 114), n)
    if h(t['gx'] * 1.9, t['gy'] * 1.9, t['gz'] * 1.9) > 0.9:
        rgb = (214, 156, 160)
    return rgb


def paint(cube, t):
    m = cube['material']
    if m in ('torso', 'arm', 'leg'):
        # Crust everywhere the rock touches: dense on the shoulders, hands and feet, thinner over the chest.
        load = 0.45 + (0.35 if t['face'] == 'south' else 0.0) + (0.3 if m != 'torso' else 0.0)
        if h(t['gx'], t['gy'], t['gz']) < load:
            return crust(t)
        return add(flesh(t, PAL['skin']), -8 if (t['u'] + t['v']) % 5 == 0 else 0)
    if m == 'knot':
        rgb = mix((96, 82, 64), (128, 110, 84), h(t['gx'] * 1.1, t['gy'] * 1.1, t['gz'] * 1.1))
        if (t['u'] + 2 * t['v']) % 7 == 0:
            rgb = add(rgb, -26)
        return (214, 156, 160) if h(t['gx'] * 2.3, t['gy'] * 2.3, t['gz'] * 2.3) > 0.93 else rgb
    if m == 'root':
        return mix((92, 76, 56), (122, 102, 76), t['n']) if t['v'] % 3 else (196, 140, 144)
    if m == 'kelp':
        rgb = weave((70, 74, 48), t)
        if t['side'] and t['u'] % 5 == 2:
            rgb = add(rgb, -14)              # the mantle's long kelp ribs
        return rgb
    return paint_base(cube, t, PAL)


POSES = [('rest', {}, ()),
         ('braced', {'hips': (0, 0, 0), 'right_leg': (-8, 0, 10), 'left_leg': (-8, 0, -10), 'right_shin': (17, 0, 0),
                     'left_shin': (17, 0, 0), 'right_arm': (-17, 0, 72), 'left_arm': (-17, 0, -72),
                     'right_forearm': (0, 0, 34), 'left_forearm': (0, 0, -34)}, ()),
         ('closed', {'body': (14, 0, 0), 'head': (20, 0, 0), 'right_arm': (-40, -11, -11), 'left_arm': (-40, 11, 11),
                     'right_forearm': (-97, 34, 0), 'left_forearm': (-97, -34, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'braced_elder', 'court_braced_elder.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
