"""A Veiled resident of the Veiled Fold: rarely seen, folded out of sight in the cleft's inner rooms when still.

An altered humanoid gone further than the Host: wears Ink Cap at Apex (the cap and the translucent black veil from the
cap to the shoulders, dripping ink, come from the strain layer). The body under it is shrouded whole: a hooded drape
of ink-black cloth from the crown to the floor, closed at the front, the arms inside long sleeves that hang past the
hands, so the outline is a fold of dark cloth with a pale face inside it. Fine bone-white sensory filaments trail from
the sleeve ends and the hem. When it has stood still a while it folds down into the drape and all but vanishes (the
renderer fades it); it unfolds to speak.

    python tools/model_export/court_veiled_resident.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, eye_face, flesh, humanoid, mix, paint_base, weave, wrap_skirt, write

PAL = dict(PALETTES['veiled_fold'], skin=(200, 196, 196))


def rig():
    r = humanoid('veiled_resident', 'CourtVeiledResidentModel')
    # The hood: close round the head, open at the face.
    r.part('hood', 'head')
    r.cube('hood', (-4.5, -8.6, -4.5, 9.0, 1.0, 9.0), 'shroud')
    r.cube('hood', (-4.5, -7.6, 3.7, 9.0, 8.2, 0.8), 'shroud')
    r.cube('hood', (-4.6, -7.6, -4.5, 0.6, 8.2, 8.2), 'shroud')
    r.cube('hood', (4.0, -7.6, -4.5, 0.6, 8.2, 8.2), 'shroud')
    # The drape: from the shoulders to the floor, closed at the front, with a back panel that pools at the heels.
    wrap_skirt(r, 12.5, material='shroud')
    r.part('drape', 'body', (0, -10, 0))
    r.cube('drape', (-4.6, -0.4, -2.45, 9.2, 13.0, 0.45), 'shroud')
    r.cube('drape', (-4.6, -0.4, 2.0, 9.2, 13.0, 0.45), 'shroud')
    r.cube('drape', (-4.7, -0.4, -2.4, 0.45, 13.0, 4.8), 'shroud')
    r.cube('drape', (4.25, -0.4, -2.4, 0.45, 13.0, 4.8), 'shroud')
    # Long sleeves past the hands, with filaments trailing from the ends.
    for side in ('right', 'left'):
        r.part(f'{side}_sleeve', f'{side}_forearm')
        r.cube(f'{side}_sleeve', (-2.5, -0.6, -2.5, 5.0, 9.0, 5.0), 'shroud')
        for i, x in enumerate((-1.4, 0.0, 1.4)):
            r.cube(f'{side}_sleeve', (x - 0.15, 8.4, -0.15 + 0.6 * (i - 1), 0.3, 3.0 + i % 2, 0.3), 'filament')
    for i, x in enumerate((-3.6, -1.4, 1.0, 3.2)):
        r.cube('skirt_front', (x, 11.8, -2.5, 0.3, 2.6 + (i % 2), 0.3), 'filament')
    return r


def paint(cube, t):
    m = cube['material']
    if m == 'face':
        rgb = eye_face(t, PAL)
        if t['face'] == 'north':
            px, py = int(t['lx'] + 4), int(t['ly'] + 8)
            if py == 4 and px in (1, 2, 5, 6):
                return (18, 16, 22)        # eyes gone wholly dark
        return rgb
    if m in ('torso', 'arm', 'leg'):
        return weave(PAL['cloth'], t)
    if m == 'shroud':
        rgb = add(PAL['ink'], (t['n'] - 0.5) * 10)
        if t['side'] and t['u'] % 4 == 1:
            rgb = add(rgb, 8)
        if t['side'] and t['fv'] > 0.9:
            rgb = mix(rgb, PAL['trim'], 0.18)    # the hem paling where filaments begin
        return rgb
    if m == 'filament':
        return mix(PAL['trim'], (150, 146, 140), t['fv']) + (220,)
    return paint_base(cube, t, PAL)


POSES = [('rest', {}, ()),
         ('folded', {'hips': (0, 0, 0), 'body': (40, 0, 0), 'head': (30, 0, 0), 'right_leg': (-80, 0, 0),
                     'left_leg': (-80, 0, 0), 'right_shin': (150, 0, 0), 'left_shin': (150, 0, 0),
                     'right_arm': (-40, -20, -10), 'left_arm': (-40, 20, 10), 'right_forearm': (-90, 30, 0),
                     'left_forearm': (-90, -30, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'veiled_resident', 'court_veiled_resident.py', paint, None, POSES, args.preview)


if __name__ == '__main__':
    main()
