"""Author the fruiting-body cache: a squat fungus grown over a dead Apotheos's gear, plainly a container.

A pale gilled body sits on a rooted foot, bracket-fungus shelves stepping round it. A bulbous spotted cap
closes over it in two halves along a dark seam where it will split. The shapes of what it holds press out
against the flesh of the cap: a blade's hilt on one half, a rounded helm on the other.

The two cap halves are not in the block model. FruitingBodyCacheRenderer draws `fruiting_body_cache_lid_west`
and `_lid_east` so the cap breathes while closed. The withering ghost (HeartNodeMomentRenderer) draws them
swinging open about their hinges when the owner opens it. LID_HINGE_X and LID_HINGE_Y are mirrored in both.
The gills under the cap and the seam's dark core stay in the body, so an open cache shows them.

Emits the body, both lids, the blockstate and an editable Blockbench project (the cache has no item). `--preview` writes build/fruiting_body_cache_preview.png. Python 3 + Pillow + NumPy.
"""
import argparse
import json

import station_kit as kit
from station_kit import Model

NAME = 'fruiting_body_cache'
TEXTURES = {
    'hide': 'hemomancy:block/fruiting_body_cache',
    'knot': 'hemomancy:block/erythrocytic_mycelium_top',
    'root': 'hemomancy:block/calcified_hyphae',
    'bracket': 'hemomancy:block/morel_cap',
    'gill': 'hemomancy:block/fungal_gill',
    'cap': 'hemomancy:block/infected_cap',
    'seam': 'hemomancy:block/blood_scorched_scab',
}

CENTRE = (8.0, 8.0)
CAP_BASE = 5.6
# Each cap tier: (radius, y0, y1). The cap splits along x = 8.
TIERS = ((5.4, CAP_BASE, 8.2), (4.6, 8.2, 9.8), (3.4, 9.8, 10.9), (2.0, 10.9, 11.6))
SEAM = .12
# The dark flesh lining each half's cut face, where the cap will part.
SEAM_LINING = .15
# Each lid swings outward about the bottom edge of its outer side, along z.
LID_HINGE_X = (CENTRE[0] - TIERS[0][0], CENTRE[0] + TIERS[0][0])
LID_HINGE_Y = CAP_BASE

DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [.625, .625, .625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [.25, .25, .25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [.5, .5, .5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [.375, .375, .375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 0, 0], 'scale': [.4, .4, .4]},
}


def foot(m):
    """The rooted foot it grew from, roots gripping the ground round it."""
    m.octo('foot', 'y', (8, 0, 8), 5.0, 0, 1.2, 'knot', cap='root')
    for deg in (22.5, 112.5, 202.5, 292.5):
        m.radial('root', CENTRE, deg, 4.4, 7.4, -.38, .38, 0, .5, 'root')
    for deg in (67.5, 157.5, 247.5, 337.5):
        m.radial('rootlet', CENTRE, deg, 4.6, 6.4, -.24, .24, 0, .32, 'root')


def stipe(m):
    """The squat pale body under the cap, bracket shelves stepping round it with gills under each."""
    m.octo('body', 'y', (8, 0, 8), 4.1, 1.2, CAP_BASE, 'hide')
    m.octo('body_swell', 'y', (8, 0, 8), 4.5, 2.2, 4.4, 'hide')
    for deg, y0, r1 in ((45, 2.2, 6.4), (135, 3.1, 6.0), (225, 2.5, 6.6), (315, 3.4, 5.8), (90, 4.2, 5.6)):
        m.radial('bracket', CENTRE, deg, 3.8, r1, -1.6, 1.6, y0, y0 + .6, 'bracket')
        m.radial('bracket_lip', CENTRE, deg, 3.8, r1 - .6, -1.2, 1.2, y0 + .6, y0 + .9, 'bracket')
        m.radial('bracket_gill', CENTRE, deg, 3.8, r1 - .2, -1.4, 1.4, y0 - .15, y0, 'gill', faces=('down',))
    m.octo('cap_gills', 'y', (8, 0, 8), 5.1, CAP_BASE - .35, CAP_BASE - .05, 'gill')


def half_tier(m, name, side, radius, y0, y1, mat):
    """One tier of the dome cut at the seam: an octagon built from axis-aligned boxes so the cut stays flat."""
    cx, cz = CENTRE
    lining = cx + side * (SEAM + SEAM_LINING)
    for hx, hz in ((radius, radius * kit.TAN), (radius * kit.TAN, radius), (radius * .78, radius * .78)):
        m.box(name, (lining, y0, cz - hz), (cx + side * hx, y1, cz + hz), mat)
    m.box('seam', (cx + side * SEAM, y0, cz - radius), (lining, y1, cz + radius), 'seam')


def lid(side):
    """One half of the cap: a stepped dome, spotted, with the shape of what it holds pressing out of it."""
    m = Model()
    for k, (radius, y0, y1) in enumerate(TIERS):
        half_tier(m, 'cap' if k else 'cap_rim', side, radius, y0, y1, 'cap')
    if side < 0:
        # A blade's hilt, guard and pommel pushing up through the flesh of the crown.
        m.box('held_hilt', (4.2, 10.4, 9.0), (7.2, 11.2, 9.7), 'hide')
        m.box('held_guard', (6.6, 10.5, 8.2), (7.2, 11.4, 10.6), 'hide')
        m.octo('held_pommel', 'y', (3.9, 0, 9.35), .6, 9.8, 10.6, 'hide')
    else:
        # The round of a helm bulging from the flank.
        m.box('held_helm', (11.6, 6.4, 5.4), (13.95, 7.9, 8.4), 'hide')
        m.box('held_helm', (11.8, 8.2, 5.8), (13.0, 9.0, 8.0), 'hide')
    return m


def body():
    m = Model()
    foot(m)
    stipe(m)
    return m


def blockstate():
    return {'variants': {'': {'model': 'hemomancy:block/' + NAME}}}


def export():
    solid, west, east = body(), lid(-1), lid(1)
    kit.separate([solid, west, east], TEXTURES, set(), 'Fruiting-body cache')
    kit.write_json(kit.MODELS / (NAME + '.json'), kit.to_json(solid, TEXTURES, 'hide', DISPLAY))
    kit.write_json(kit.MODELS / (NAME + '_lid_west.json'), kit.to_json(west, TEXTURES, 'cap'))
    kit.write_json(kit.MODELS / (NAME + '_lid_east.json'), kit.to_json(east, TEXTURES, 'cap'))
    kit.write_json(kit.ASSETS / 'blockstates' / (NAME + '.json'), blockstate(), indent=2)
    project = kit.bbmodel(NAME, [('body', solid), ('lid_west', west), ('lid_east', east)], TEXTURES, DISPLAY)
    (kit.BBMODEL / (NAME + '.bbmodel')).write_text(json.dumps(project), encoding='utf-8')
    return solid


def opened(side, degrees):
    """A preview transform: a lid swung outward `degrees` about its hinge, as the opening ghost draws it."""
    import numpy as np
    import resonant_forge_preview as raster
    hinge = np.array([LID_HINGE_X[0 if side < 0 else 1], LID_HINGE_Y, 8.0])
    r = raster.rotation('z', -degrees * side)

    def apply(p, n):
        return (p - hinge) @ r.T + hinge, r @ n
    return apply


def preview():
    closed = [(body(), kit.identity), (lid(-1), kit.identity), (lid(1), kit.identity)]
    split = [(body(), kit.identity), (lid(-1), opened(-1, 70)), (lid(1), opened(1, 70))]
    panels = [('Closed', 200, 18, closed), ('Three-quarter', 235, 26, closed), ('Above', 210, 62, closed),
              ('Split open', 180, 30, split)]
    return kit.preview(panels, 4, kit.ROOT / 'build/fruiting_body_cache_preview.png', (360, 380), 19, (8, 6, 8),
                       TEXTURES)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    parser.add_argument('--no-export', action='store_true')
    args = parser.parse_args()
    if not args.no_export:
        model = export()
        print('Exported fruiting-body cache: %d elements' % len(model.elements))
    if args.preview:
        print(preview())
