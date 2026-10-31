"""A Lamp-colony of the Woven Current: an elder joined into the camp's network, hung among the lines like a lamp.

Wears Dead Man's Rope at Primal (the strain layer adds the full vein network and the ribbon strands; the renderer
draws the strands that tie the elder in to the camp's other members, let go while it speaks). The body is far along:
translucent violet-pale skin with the veins showing everywhere and carrying the bacterial light, the hair gone, a
collar of beads, a short indigo wrap, and salp-like beaded chains hanging from the shoulders and back that drift in
the current. It holds the line above it with both hands and lets go of the network to talk.

    python tools/model_export/court_lamp_colony.py [--preview]
"""
import argparse
import math

from court_humanoid import PALETTES, network, add, eye_face, flesh, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = dict(PALETTES['woven_current'], skin=(196, 190, 222))

# (name, parent, pivot, beads): the beaded chains, salp-like, from the shoulders and down the back.
CHAINS = [('chain_0', 'body', (-3.2, -9.6, 1.6), 6), ('chain_1', 'body', (-1.0, -9.8, 2.0), 8),
          ('chain_2', 'body', (1.2, -9.8, 2.0), 7), ('chain_3', 'body', (3.2, -9.6, 1.6), 6),
          ('chain_4', 'right_arm', (-1.6, -1.6, 0.8), 5), ('chain_5', 'left_arm', (1.6, -1.6, 0.8), 5)]


def rig():
    r = humanoid('lamp_colony', 'CourtLampColonyModel')
    wrap_skirt(r, 4.5, material='cloth')
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), 'cord')
    # A bead collar at the throat.
    for i in range(7):
        a = math.pi * (0.15 + 0.7 * i / 6)
        r.cube('body', (-4.0 + 8.0 * i / 6 - 0.45, -9.9 + 1.2 * math.sin(a), -2.5, 0.9, 0.9, 0.5), 'bead')
    # Two pairs of bead bands at the crown where the hair has gone: the network's first knots.
    r.cube('head', (-4.2, -8.3, -0.6, 8.4, 0.5, 1.2), 'bead')
    r.cube('head', (-0.6, -8.3, -4.2, 1.2, 0.5, 8.4), 'bead')
    # The beaded chains: a cord with a bead every two pixels, each hung from its own pivot so it can drift.
    for name, parent, pivot, beads in CHAINS:
        r.part(name, parent, pivot, (12, 0, 0))
        r.cube(name, (-0.2, 0.0, -0.2, 0.4, beads * 2.0, 0.4), 'strand')
        for i in range(beads):
            s = 1.1 - 0.06 * i
            r.cube(name, (-s / 2, 0.6 + i * 2.0, -s / 2, s, s, s), 'bead')
    # Cord loops at the wrists, where the hands take the line.
    for side in ('right', 'left'):
        r.cube(f'{side}_forearm', (-2.15, 4.6, -2.15, 4.3, 0.7, 4.3), 'cord')
    return r


def vein(t):
    return network(t, 0.045)


def skin(t):
    rgb = mix(flesh(t, PAL['skin']), (150, 140, 200), 0.25 + 0.15 * t['n'])
    return mix(rgb, PAL['vein'], 0.7) if vein(t) else rgb


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        if t['face'] == 'north':
            rgb = eye_face(t, PAL)
            return mix(rgb, PAL['vein'], 0.6) if vein(t) and rgb == flesh(t, PAL['skin']) else rgb
        return skin(t)
    if m in ('torso', 'arm', 'leg'):
        return skin(t)
    if m == 'bead':
        # Salp beads: clear violet jelly with a bright core.
        core = 0.3 < t['fu'] < 0.7 and 0.3 < t['fv'] < 0.7
        return (PAL['vein_core'] + (236,)) if core else (mix(PAL['vein'], (90, 80, 160), 0.4) + (200,))
    if m == 'strand':
        return mix(PAL['vein'], (110, 96, 190), t['n']) + (210,)
    return paint_base(cube, t, PAL)


def glow(cube, t):
    m = cube['material']
    if m in ('torso', 'arm', 'leg', 'face') and vein(t) and t['n'] > 0.35:
        return PAL['vein']
    if m == 'bead' and 0.3 < t['fu'] < 0.7 and 0.3 < t['fv'] < 0.7:
        return PAL['vein_core']
    return None


POSES = [('rest', {}, ()),
         ('holding the line', {'right_arm': (-170, 0, -12), 'left_arm': (-170, 0, 12), 'right_forearm': (-12, 0, 0),
                               'left_forearm': (-12, 0, 0), 'right_leg': (-8, 0, 4), 'left_leg': (4, 0, -4),
                               'right_shin': (18, 0, 0), 'left_shin': (10, 0, 0), 'head': (-12, 0, 0)}, ()),
         ('let go, speaking', {'right_arm': (-40, 0, 6), 'left_arm': (-28, 0, -6), 'right_forearm': (-50, 0, 0),
                               'left_forearm': (-40, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'lamp_colony', 'court_lamp_colony.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
