"""The eight Brined Court Morphling strains' body attachments (Track 3), one function per strain.

For every anchor a strain grows on, this writes:
  - the Java model  client/model/entity/morphling/<Strain><Anchor>Model.java  (mesh + part table)
  - its textures    textures/models/morphling/<strain>_<anchor>.png and _glow.png
  - a Blockbench project  models/entity/bbmodel/morphling/<Strain><Anchor>Model.bbmodel
  - a preview       build/morphling-strain-preview/<strain>_<anchor>.png (all six stages side by side)
plus MorphlingStrainModels.java (the anchor index the client registry reads) and a contact sheet of all eight at
Apex (build/morphling-strain-preview/apex_contact.png).

Every part carries the stage it grows at (0 Unfed .. 5 Primal), a limb tag (UPPER / LOWER for arm and leg parts, so
Track 9's jointed NPC rig can put forearm and calf growths on the right segment), a motion and the active-state bits
it answers (MorphlingActiveState). Stages add parts and the runtime grows each new part in (0.7, 0.88, then 1.0 of
its authored size), so a strain reads as one organism growing, not a swapped model.

Texel density: one texture pixel per model unit, the player skin's own density, which is 16 px per block before
the player renderer's 0.9375 scale (the same scale the skin gets). The exporter asserts it.

Geometry is authored in the anchor part's own space (Minecraft model space, +Y down, front -Z): BODY x -4..4,
y 0..12, back face z = 2; HEAD x -4..4, y -8..0; ARMS the left arm, x -1..3, y -2..10, outer face x = 3 (the right
arm is mirrored); LEGS the left leg, x -2..2, y 0..12.

Run: python tools/model_export/morphling_strains.py   (Python 3 + Pillow + NumPy)
"""
from pathlib import Path
from PIL import Image, ImageDraw
import base64
import json
import math
import uuid

import numpy as np

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/morphling'
PREVIEW = ROOT / 'build/morphling-strain-preview'
TEXELS_PER_UNIT = 1  # 16 px per block, like the skin the parts grow on
STAGES = ['Unfed', 'Fledgling', 'Developing', 'Mature', 'Apex', 'Primal']
PI = math.pi

FLAG_BITS = ['HOME', 'STILL', 'ANCHORED', 'LANTERN_FRONDS', 'ARBOR', 'CLING', 'BRACE', 'BREAKWATER', 'HOLDING',
             'SAIL_RAISED', 'SHARED_WAKE', 'FOLDED_AWAY', 'DELIQUESCE', 'FOLD_SHUT', 'CRYPTOBIOSIS', 'SECOND_HEAD',
             'SECOND_HEAD_TWIN', 'KINDLED', 'PLUME_CLOUD', 'WHALE_FALL_ROOTED', 'REELING', 'LIFELINE', 'BLOOM']
MOTIONS = ['NONE', 'SWAY', 'WAG', 'RIPPLE', 'FLAME', 'FROND', 'BREATHE', 'SAIL', 'POUCH', 'DRIP', 'GROW_ON',
           'SPLAY_ON']
GLOWS = ['NONE', 'SUNLIGHT', 'VEIN_PULSE', 'SAIL_FLUSH', 'BREATH', 'FLAME']
ANCHOR_SPACE = {  # the anchor part's own cube, for previews: (x, y, z, w, h, d)
    'BODY': (-4, 0, -2, 8, 12, 4), 'HEAD': (-4, -8, -4, 8, 8, 8),
    'ARMS': (-1, -2, -2, 4, 12, 4), 'LEGS': (-2, 0, -2, 4, 12, 4)}


def uid(value):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'hemomancy:morphling_strain/' + value))


# --------------------------------------------------------------------------------------------------------------
#  Rig


class Rig:
    def __init__(self, strain, anchor, materials, glow='NONE', visible_from=2, hide_at=-1, primal_scale=1.0,
                 hide_head_flags=()):
        self.strain, self.anchor, self.materials, self.glow = strain, anchor, materials, glow
        self.visible_from, self.hide_at, self.primal_scale = visible_from, hide_at, primal_scale
        self.hide_head_flags = tuple(hide_head_flags)
        self.parts = []
        self.names = set()

    @property
    def name(self):
        return f'{self.strain}_{self.anchor.lower()}'

    @property
    def java_name(self):
        anchor = {'BODY': 'Body', 'HEAD': 'Head', 'ARMS': 'Arm', 'LEGS': 'Leg'}[self.anchor]
        return ''.join(w.capitalize() for w in self.strain.split('_')) + anchor + 'Model'

    def part(self, name, parent='root', pivot=(0, 0, 0), rotation=(0, 0, 0), stage=2, limb=None, motion='NONE',
             amp=0.0, speed=0.0, phase=0.0, flags=(), glow_group=0):
        assert name not in self.names, (self.name, name)
        assert motion in MOTIONS, motion
        assert all(f in FLAG_BITS for f in flags), flags
        if parent != 'root':
            parent_part = self.get(parent)
            assert stage >= parent_part['stage'], (self.name, name, 'grows before its parent')
            if limb is None:
                limb = parent_part['limb']
        if limb is None:
            limb = 'NONE'
        if self.anchor in ('ARMS', 'LEGS'):
            assert limb in ('UPPER', 'LOWER'), (self.name, name, 'limb parts need an UPPER or LOWER tag')
        self.names.add(name)
        self.parts.append(dict(name=name, parent=parent, pivot=tuple(pivot), rotation=tuple(rotation), stage=stage,
                               limb=limb, motion=motion, amp=amp, speed=speed, phase=phase, flags=tuple(flags),
                               glow_group=glow_group, cubes=[]))
        return name

    def get(self, name):
        return next(p for p in self.parts if p['name'] == name)

    def cube(self, part, box, material):
        assert all(abs(v * 8 - round(v * 8)) < 1e-9 for v in box), (self.name, part, box)
        assert min(box[3:]) >= .25, (self.name, part, box)
        assert material in self.materials, (self.name, material)
        self.get(part)['cubes'].append(dict(box=tuple(box), material=material))

    def chain(self, name, parent, pivot, rotation, segments, box, materials, stage, limb=None, motion='RIPPLE',
              amp=0.25, speed=0.08, bend=(0, 0, 0), phase0=0.0, flags=(), stages=None, beads=None):
        """A hanging chain of segments along +Y, each a child of the last, for strands, tentacles and roots."""
        last = parent
        names = []
        for i in range(segments):
            seg_stage = stages[i] if stages else stage
            seg_motion = motion if i == 0 or motion not in ('GROW_ON', 'SPLAY_ON') else 'NONE'
            seg = self.part(f'{name}_{i}', last, pivot if i == 0 else (0, box[1] + box[4], 0),
                            rotation if i == 0 else bend, seg_stage, limb, seg_motion, amp, speed, phase0 + i * 0.7,
                            flags if i == 0 else (), glow_group=i)
            self.cube(seg, box, materials[min(i, len(materials) - 1)])
            if beads and i % beads[0] == beads[0] - 1:
                bead = self.part(f'{name}_{i}_bead', seg, (0, box[1] + box[4] * .85, 0), stage=max(seg_stage, beads[1]),
                                 glow_group=i)
                self.cube(bead, (-.5, -.5, -.5, 1, 1, 1), beads[2])
            names.append(seg)
            last = seg
        return names


# --------------------------------------------------------------------------------------------------------------
#  Materials: base RGBA, optional glow RGB, and a painter style


def mat(rgba, glow=None, style='plain'):
    if len(rgba) == 3:
        rgba = (*rgba, 255)
    return dict(rgba=rgba, glow=glow, style=style)


SUNBURST = dict(crust=mat((217, 130, 43), style='crust'), crust_dark=mat((138, 62, 18), style='crust'),
                crust_light=mat((240, 168, 72), style='crust'), alga=mat((126, 154, 58), style='fleck'),
                frond=mat((240, 186, 82), (255, 222, 140), 'frond'), rib=mat((184, 106, 30), (255, 196, 96), 'rib'),
                root=mat((126, 82, 38), style='root'))
HOLDFAST = dict(stone=mat((115, 102, 92), style='stone'), stone_dark=mat((82, 72, 66), style='stone'),
                root=mat((142, 124, 104), style='root'), pink=mat((214, 140, 150), style='fleck'),
                strand=mat((160, 140, 120), style='root'))
PURSE = dict(cobalt=mat((40, 70, 170), style='scale'), silver=mat((190, 200, 214), style='scale'),
             purse=mat((150, 28, 40), style='leather'), ridge=mat((106, 18, 30), style='leather'),
             horn=mat((196, 78, 66)), fin=mat((58, 92, 196, 235), style='fin'),
             sail=mat((48, 78, 182, 240), (232, 58, 92), 'sail'), crimson=mat((172, 30, 52), (226, 70, 96)))
ROPE = dict(vein=mat((140, 124, 232, 210), (178, 160, 255), 'vein'), strand=mat((116, 106, 214, 175), (150, 138, 255), 'strand'),
            bead=mat((196, 186, 255, 225), (220, 212, 255)), node=mat((92, 80, 190, 225), (170, 150, 255)))
INK_CAP = dict(cap=mat((232, 226, 214), style='shag'), cap_light=mat((246, 242, 232), style='shag'),
               cap_dark=mat((44, 38, 48), style='shag'), ink=mat((16, 13, 20)), veil=mat((22, 18, 28, 160), style='veil'),
               filament=mat((214, 208, 196)), drip=mat((10, 8, 14, 235)))
HUSHBELL = dict(mantle=mat((140, 168, 194), style='fold'), mantle_dark=mat((98, 122, 146), style='fold'),
                fold=mat((182, 202, 218), (200, 226, 246), 'fold'), rim=mat((76, 96, 116), style='stone'),
                frost=mat((214, 230, 242), (220, 238, 255)))
MARROW = dict(bone=mat((232, 224, 206), style='bone'), bone_dark=mat((196, 186, 168), style='bone'),
              club=mat((198, 40, 40), style='club'), club_dark=mat((140, 22, 28), style='club'),
              plume=mat((238, 96, 84), style='plume'), plume_light=mat((250, 168, 150), style='plume'),
              bead=mat((214, 18, 30)), tendril=mat((176, 40, 50)))
KINBUD = dict(pale=mat((238, 224, 216, 232), style='polyp'), bud=mat((242, 204, 194, 240), style='polyp'),
              amber=mat((246, 176, 70, 235), (255, 196, 90), 'mane'), flame=mat((236, 110, 40, 235), (255, 132, 52), 'mane'),
              crimson=mat((190, 32, 42, 235), (232, 52, 60), 'mane'), vein=mat((196, 64, 64)))


# --------------------------------------------------------------------------------------------------------------
#  Strains


def sunburst():
    body = Rig('sunburst', 'BODY', SUNBURST, glow='SUNLIGHT')
    for s in (-1, 1):
        r = body.part(f'rosette_{side(s)}', pivot=(s * 2.75, 0, 0), stage=2, motion='BREATHE', amp=.03, phase=s)
        body.cube(r, (-1.5, -.5, -1.5, 3, .5, 3), 'crust')
        body.cube(r, (-1, -1, -1, 2, .5, 2), 'crust_light')
        body.cube(r, (s * 1.25 - .5, -.5, -.75, 1, .5, 1.5), 'crust_dark')
        body.cube(r, (-.75, -.75, 1.25, 1.5, .5, .75), 'crust')
        body.cube(r, (-.5, -.75, -.5, .5, .25, .5), 'alga')
    crust = body.part('crust_back', pivot=(0, 0, 2), stage=3)
    for box, m in [((-3.5, .5, 0, 7, 3, .5), 'crust'), ((-2.5, 3.5, 0, 5, 1.5, .5), 'crust'),
                   ((-4, 1, 0, 1, 2, .5), 'crust_dark'), ((3, 1, 0, 1, 2, .5), 'crust_dark'),
                   ((-3, .25, .25, 2, 1, .5), 'crust_light'), ((1, .75, .25, 2.25, 1, .5), 'crust_light'),
                   ((-1, 2.5, .25, 2, 1, .5), 'crust_dark'), ((-2.25, 4.5, 0, 1.25, 1, .5), 'crust_light'),
                   ((1.5, 4.25, .25, 1, .75, .5), 'alga'), ((-.5, 1.5, .5, 1, .5, .25), 'alga')]:
        body.cube(crust, box, m)
    for s in (-1, 1):
        bud = body.part(f'frond_bud_{side(s)}', pivot=(s * 2, 2.5, 2.5), rotation=(-.35, 0, s * .3), stage=3,
                        motion='SWAY', amp=.05, speed=.05, phase=s)
        body.cube(bud, (-.5, -2, -.25, 1, 2, .5), 'frond')
        body.cube(bud, (-.75, -2.75, -.25, 1.5, .75, .5), 'frond')
        body.cube(bud, (-.25, -1.5, 0, .5, 1.5, .5), 'rib')
    # Apex: broad fronds fanning from the back and shoulders; they open wide with the light, Lantern Fronds and Bloom.
    for i, spread in enumerate((-1.05, -.52, 0, .52, 1.05)):
        f = body.part(f'frond_{i}', pivot=(spread * 2.2, 1.5 + abs(spread), 2.5), rotation=(-.5, 0, spread),
                      stage=4, motion='FROND', amp=.45, phase=i * 1.3, flags=('LANTERN_FRONDS', 'BLOOM', 'ARBOR'))
        frond_blade(body, f, 7 - abs(spread) * 1.5)
        tip = body.part(f'frond_{i}_tip', f, (0, -q(7 - abs(spread) * 1.5), 0), (-.35, 0, 0), 4, motion='SWAY',
                        amp=.06, speed=.05, phase=i)
        body.cube(tip, (-1.5, -3, 0, 3, 3, .5), 'frond')
        body.cube(tip, (-.25, -2.5, .25, .5, 2.5, .5), 'rib')
        body.cube(tip, (-2, -2.25, 0, .5, 1.25, .5), 'crust_light')
    for s in (-1, 1):
        f = body.part(f'shoulder_frond_{side(s)}', pivot=(s * 3.5, 0, .5), rotation=(-.2, 0, s * 1.15), stage=4,
                      motion='FROND', amp=.3, phase=s * 2, flags=('LANTERN_FRONDS', 'BLOOM', 'ARBOR'))
        frond_blade(body, f, 4.5)
    # Primal: an oversized canopy over the head; during Arbor, frond roots run from the back into the ground.
    for i, x in enumerate((-2.5, 0, 2.5)):
        stalk = body.part(f'canopy_{i}', pivot=(x, .5, 2.5), rotation=(-.12, 0, x * .06), stage=5, motion='SWAY',
                          amp=.03, speed=.04, phase=i)
        body.cube(stalk, (-.5, -13, 0, 1, 13, .5), 'rib')
        body.cube(stalk, (-1, -6, 0, .5, 2, .5), 'frond')
        body.cube(stalk, (.5, -10, 0, .5, 2, .5), 'frond')
        leaf = body.part(f'canopy_{i}_blade', stalk, (0, -13, 0), (1.05 + abs(x) * .05, 0, x * .12), 5,
                         motion='FROND', amp=.25, phase=i, flags=('LANTERN_FRONDS', 'BLOOM', 'ARBOR'))
        body.cube(leaf, (-3.5, -9, 0, 7, 9, .5), 'frond')
        body.cube(leaf, (-.5, -9, .25, 1, 9, .5), 'rib')
        for j, (dx, dy) in enumerate(((-4.25, -7), (3.5, -6), (-4, -3), (3.5, -2.5), (-1.5, -10))):
            body.cube(leaf, (dx, dy, 0, .75 if abs(dx) > 2 else 3, 2 if abs(dx) > 2 else 1, .5), 'frond')
    for i, (x, tilt) in enumerate(((-2, -.12), (-.75, -.04), (.75, .04), (2, .12))):
        body.chain(f'arbor_root_{i}', 'root', (x, 8, 2.25), (.22, 0, tilt), 3, (-.25, 0, -.25, .5, 5.5, .5),
                   ['root', 'root', 'crust_dark'], 5, motion='GROW_ON', amp=0, flags=('ARBOR',), bend=(-.08, 0, 0))
    return [body]


def frond_blade(rig, part, height):
    height = q(height)
    rig.cube(part, (-.5, -height, 0, 1, height, .5), 'rib')
    rig.cube(part, (-2, -height + .5, -.25, 4, height - 2, .5), 'frond')
    rig.cube(part, (-2.75, -height + 1.5, -.25, .75, 2, .5), 'frond')
    rig.cube(part, (2, -height + 2.5, -.25, .75, 2, .5), 'frond')
    rig.cube(part, (-1.5, -height - .5, -.25, 3, 1, .5), 'frond')
    rig.cube(part, (-1.25, -1.5, -.25, 2.5, 1.5, .5), 'crust')


def holdfast():
    arms = Rig('holdfast', 'ARMS', HOLDFAST, primal_scale=1.12)
    k = arms.part('wrist_knots', pivot=(1, 8.5, 0), stage=2, limb='LOWER')
    for box, m in [((1.75, -.5, -1, .75, 1, 1), 'stone'), ((-.5, -.5, 1.75, 1, 1, .75), 'stone'),
                   ((.5, 0, -2.5, 1, .75, .75), 'stone_dark'), ((-2.5, -.25, 0, .75, .75, .75), 'stone'),
                   ((1.75, .5, .5, .5, .5, .5), 'pink'), ((1, -.75, 1.75, .75, .5, .5), 'root')]:
        arms.cube(k, box, m)
    band = arms.part('forearm_roots', pivot=(1, 6, 0), stage=3, limb='LOWER')
    for y in (-1.5, 1):
        for box in [(2, y, -2.25, .5, 1, 4.5), (-2.5, y, -2.25, .5, 1, 4.5), (-2, y, 2, 4, 1, .5), (-2, y, -2.5, 4, 1, .5)]:
            arms.cube(band, box, 'root')
    arms.cube(band, (2.25, -.25, -.5, .5, .5, .5), 'pink')
    for i, (pivot, rot) in enumerate((((3.1, 2.5, 1), (.45, 0, 0)), ((1, 2.5, 2.1), (0, 0, .45)),
                                      ((-1.1, 3, -1), (-.4, 0, 0)), ((2, 3, -2.1), (0, 0, -.5)))):
        r = arms.part(f'wrap_root_{i}', pivot=pivot, rotation=rot, stage=3, limb='LOWER')
        arms.cube(r, (-.25, 0, -.25, .5, 4.5, .5), 'root')
    c = arms.part('forearm_cluster', pivot=(1, 7, 0), stage=4, limb='LOWER')
    for box, m in [((1.5, -2.5, -2, 2.5, 4, 3.5), 'stone'), ((-1.5, -2, 1.5, 3.5, 3, 1.75), 'stone_dark'),
                   ((2.5, .75, 1, 1.5, 1.5, 1.5), 'pink'), ((-3.25, -1.5, -1.5, 1.5, 3, 2.75), 'stone'),
                   ((2.5, -4, -1, 1.5, 1.5, 1.5), 'root'), ((-1, -3, -3, 2.5, 2, 1.25), 'stone'),
                   ((-.75, 1, -2.75, 1, 1, .75), 'pink'), ((3.75, -1, -.5, .75, 2, 1), 'root')]:
        arms.cube(c, box, m)
    for i, (x, z, tilt) in enumerate(((-.5, -1.25, -.35), (1, -1.5, 0), (2.5, -1, .35), (1, 1.25, .2))):
        arms.chain(f'grip_{i}', 'root', (x, 10, z), (0, 0, tilt), 2, (-.25, 0, -.25, .5, 1.5, .5), ['strand'], 4,
                   limb='LOWER', motion='SPLAY_ON', amp=tilt * 1.6 + .2, flags=('ANCHORED', 'CLING', 'BREAKWATER'))
    m = arms.part('hand_mass', pivot=(1, 9, 0), stage=5, limb='LOWER')
    for box, mat_ in [((-2.75, -2, -2.75, 5.5, 3.5, 5.5), 'root'), ((-3.25, -1, -1, .75, 2, 2), 'stone'),
                      ((2.5, -1.5, .5, 1, 2, 1.5), 'stone'), ((-1, -2.5, 2.5, 2, 1.5, 1), 'stone_dark'),
                      ((1, 1, -3, 1.25, 1, .75), 'pink'), ((-2, .75, 2.5, .75, .75, .75), 'pink')]:
        arms.cube(m, box, mat_)
    sm = arms.part('shoulder_mass', pivot=(1, -1, 0), stage=5, limb='UPPER')
    arms.cube(sm, (-.5, -1.5, -2.5, 4, 2.5, 5), 'stone')
    arms.cube(sm, (2.5, -1, -1, 1, 1.5, 2), 'pink')
    for i, (x, z, tilt) in enumerate(((-1.5, -1.5, -.6), (3.5, -1, .6), (1, 2, .2), (1, -2.5, -.15))):
        arms.chain(f'anchor_strand_{i}', 'root', (x, 10.5, z), (z * .12, 0, tilt), 2, (-.25, 0, -.25, .5, 4, .5),
                   ['strand', 'root'], 5, limb='LOWER', motion='GROW_ON', amp=0, bend=(0, 0, -tilt * .5),
                   flags=('ANCHORED', 'BREAKWATER', 'HOLDING', 'CLING'))

    legs = Rig('holdfast', 'LEGS', HOLDFAST, visible_from=4, primal_scale=1.12)
    c = legs.part('calf_cluster', pivot=(0, 8, 0), stage=4, limb='LOWER')
    for box, m in [((1.5, -2, -1, 1.25, 3, 2.5), 'stone'), ((-1.5, -1.5, 1.5, 3, 2.5, 1.25), 'stone_dark'),
                   ((1.75, .5, 1, .75, .75, .75), 'pink'), ((-2.5, -1, -.5, .75, 1.5, 1.5), 'root')]:
        legs.cube(c, box, m)
    f = legs.part('foot_mass', pivot=(0, 11, 0), stage=5, limb='LOWER')
    for box, m in [((-2.75, -1.5, -3.25, 5.5, 2.5, 6), 'root'), ((-3, -.5, -3.5, 1.25, 1.5, 1.25), 'stone'),
                   ((1.75, -.5, -3.5, 1.25, 1.5, 1.25), 'stone'), ((-.5, -2, 2.5, 1.5, 1.5, 1), 'pink')]:
        legs.cube(f, box, m)
    for i, (x, z, yaw) in enumerate(((-2, -2.5, 0), (2, -2.5, 0), (-2, 2.5, PI), (2, 2.5, PI))):
        legs.chain(f'ground_strand_{i}', 'root', (x, 12, z), (0, yaw, 1.25 * (1 if x > 0 else -1)), 2,
                   (-.25, 0, -.25, .5, 4, .5), ['strand', 'root'], 5, limb='LOWER', motion='GROW_ON', amp=0,
                   bend=(0, 0, -.35 * (1 if x > 0 else -1)), flags=('ANCHORED', 'BREAKWATER', 'HOLDING'))
    return [arms, legs]


def mermaids_purse():
    body = Rig('mermaids_purse', 'BODY', PURSE, glow='SAIL_FLUSH')
    for s in (-1, 1):
        p = body.part(f'purse_{side(s)}', pivot=(s * 4.5, 7.5, 0), stage=2, motion='POUCH', phase=s)
        body.cube(p, (-1, -2, -1.5, 2, 4, 3), 'purse')
        body.cube(p, ((.75 if s > 0 else -1), -1.5, -1, .25, 3, 2), 'ridge')
        body.cube(p, (-.75, -2.25, -1.25, 1.5, .25, 2.5), 'ridge')
        body.cube(p, (-.75, 2, -1.25, 1.5, .25, 2.5), 'ridge')
        for hz in (-1.5, 1):
            body.cube(p, (-.25, -3.25, hz, .5, 1.25, .5), 'horn')
            body.cube(p, (-.25, 2, hz, .5, 1.25, .5), 'horn')
        body.cube(p, ((1 if s > 0 else -1.25), -.25, -.5, .25, .5, 1), 'crimson')
    sail = body.part('sail', pivot=(0, .5, 2), stage=4, motion='SAIL', amp=1.45, flags=('SAIL_RAISED',))
    for y, h, d in ((0, 2, 6), (2, 2, 5.5), (4, 2, 4.75), (6, 2, 3.75), (8, 2, 2.75), (10, 1.5, 1.5)):
        body.cube(sail, (-.25, y, 0, .5, h, d), 'sail')
    for y in (.5, 3, 5.5, 8):
        body.cube(sail, (-.5, y, 0, 1, .5, 1), 'cobalt')
    body.cube(sail, (-.5, 0, 5.5, 1, 1, .75), 'crimson')
    tail = body.part('tail', pivot=(0, 11, 2), stage=5, motion='WAG', amp=.28, speed=.11)
    body.cube(tail, (-.75, -.75, 0, 1.5, 1.5, 3), 'silver')
    body.cube(tail, (-.5, -1, 0, 1, .5, 2.5), 'cobalt')
    for name, rx, box in (('tail_upper', -.55, (-.25, -6, -.5, .5, 6, 1.5)), ('tail_lower', .55, (-.25, 0, -.5, .5, 6, 1.5))):
        lobe = body.part(name, tail, (0, 0, 3), (rx, 0, 0), 5)
        body.cube(lobe, box, 'fin')
    head = Rig('mermaids_purse', 'HEAD', PURSE, glow='SAIL_FLUSH', visible_from=5)
    crest = head.part('bill_crest', pivot=(0, -6.5, -4), stage=5)
    head.cube(crest, (-.5, -1.5, -.5, 1, 1.5, 4), 'cobalt')
    head.cube(crest, (-.25, -2.25, 0, .5, .75, 3), 'sail')
    head.cube(crest, (-.25, -.75, -3.5, .5, .75, 3), 'silver')
    head.cube(crest, (-.25, -.5, -5, .5, .5, 1.5), 'silver')
    arms = Rig('mermaids_purse', 'ARMS', PURSE, glow='SAIL_FLUSH')
    fl = arms.part('forearm_finlets', pivot=(1, 4, 2), stage=2, limb='LOWER')
    for i in range(5):
        size = 1.25 - i * .2
        arms.cube(fl, (-.25, i * 1.25, 0, .5, max(.5, round(size * 4) / 4), max(.5, round(size * 4) / 4)), 'fin')
    for name, y, h, limb in (('fin_ridge_upper', -1, 5, 'UPPER'), ('fin_ridge_lower', 4, 5, 'LOWER')):
        r = arms.part(name, pivot=(3, y, 0), stage=3, limb=limb, motion='SWAY', amp=.03, speed=.09)
        arms.cube(r, (0, 0, -.25, 1.5, h, .5), 'fin')
        arms.cube(r, (0, .5, -.5, .5, h - 1, 1), 'cobalt')
        arms.cube(r, (1.5, 1, -.25, .5, h - 2.5, .5), 'fin')
    legs = Rig('mermaids_purse', 'LEGS', PURSE, glow='SAIL_FLUSH')
    cf = legs.part('calf_finlets', pivot=(0, 6.5, 2), stage=2, limb='LOWER')
    for i in range(4):
        size = 1.25 - i * .25
        legs.cube(cf, (-.25, i * 1.25, 0, .5, max(.5, size), max(.5, size)), 'fin')
    return [body, arms, legs, head]


def dead_mans_rope():
    arms = Rig('dead_mans_rope', 'ARMS', ROPE, glow='VEIN_PULSE')
    vein_path(arms, 'hand_vein', [(3, 9.5, 0), (3, 7.5, .5), (3, 5.5, -.25)], 2, 'LOWER', 0)
    vein_path(arms, 'hand_vein_b', [(3, 8.5, .25), (3, 7, 1.75)], 2, 'LOWER', 1)
    vein_path(arms, 'hand_vein_c', [(3, 7.5, .5), (3, 6.5, -1.75)], 2, 'LOWER', 1)
    vein_path(arms, 'back_vein', [(1, 9.5, 2), (1.75, 7.5, 2), (1, 5, 2)], 2, 'LOWER', 0, axis='z')
    vein_path(arms, 'upper_vein', [(3, 5.5, -.25), (3, 3, .5), (3, .5, 0)], 3, 'UPPER', 3)
    for i, (x, z) in enumerate(((2.5, 2.1), (0, 2.1), (3.1, -1))):
        arms.chain(f'ribbon_{i}', 'root', (x, 6.5 + i * .5, z), (.3 if z > 0 else 0, 0, .35 if x > 2.9 else 0), 4,
                   (-.5, 0, -.125, 1, 3, .25), ['strand'], 3, limb='LOWER', motion='RIPPLE', amp=.3, speed=.06,
                   phase0=i * 1.9, beads=(1, 4, 'bead'))
    for i, (x, z) in enumerate(((1.5, 2.1), (3.1, 1))):
        arms.chain(f'apex_ribbon_{i}', 'root', (x, 8, z), (.25, 0, .3 if x > 3 else 0), 5,
                   (-.5, 0, -.125, 1, 3, .25), ['strand'], 4, limb='LOWER', motion='RIPPLE', amp=.32, speed=.055,
                   phase0=3 + i * 1.3, beads=(1, 4, 'bead'))
    body = Rig('dead_mans_rope', 'BODY', ROPE, glow='VEIN_PULSE', visible_from=3)
    for s in (-1, 1):
        vein_path(body, f'shoulder_vein_{side(s)}', [(0, .5, 2.1), (s * 2, 1, 2.1), (s * 3.75, .75, 2.1)], 3, 'NONE', 0, axis='z')
        vein_path(body, f'blade_vein_{side(s)}', [(s * 2, 1, 2.1), (s * 2.5, 3.5, 2.1), (s * 1.5, 6, 2.1)], 3, 'NONE', 2, axis='z')
        vein_path(body, f'top_vein_{side(s)}', [(s * 1, 0, 1), (s * 3.5, 0, -.5)], 3, 'NONE', 1, axis='y')
    for i, x in enumerate((-2.5, -.75, .75, 2.5)):
        body.chain(f'train_{i}', 'root', (x, 10, 2.1), (.85 + abs(x) * .05, 0, x * .05), 6,
                   (-.5, 0, -.125, 1, 4, .25), ['strand'], 5, motion='RIPPLE', amp=.18, speed=.05, phase0=i * 1.1,
                   bend=(-.05, 0, 0), beads=(2, 5, 'bead'))
    return [arms, body]


def vein_path(rig, name, points, stage, limb, group, axis='x'):
    """Thin vein segments lying on a face: x = on the outer (+X) face, z = on the back face, y = on the top."""
    for i, (a, b) in enumerate(zip(points, points[1:])):
        ax, ay, az = a
        bx, by, bz = b
        if axis == 'x':
            length = math.hypot(by - ay, bz - az)
            angle = math.atan2(bz - az, by - ay)
            seg = rig.part(f'{name}_{i}', pivot=a, rotation=(angle, 0, 0), stage=stage, limb=limb, glow_group=group + i)
            rig.cube(seg, (0, 0, -.25, .25, q(length), .5), 'vein')
        elif axis == 'z':
            length = math.hypot(bx - ax, by - ay)
            angle = math.atan2(bx - ax, by - ay)
            seg = rig.part(f'{name}_{i}', pivot=a, rotation=(0, 0, -angle), stage=stage, limb=limb, glow_group=group + i)
            rig.cube(seg, (-.25, 0, 0, .5, q(length), .25), 'vein')
        else:
            length = math.hypot(bx - ax, bz - az)
            angle = math.atan2(bz - az, bx - ax)
            seg = rig.part(f'{name}_{i}', pivot=a, rotation=(0, -angle, 0), stage=stage, limb=limb, glow_group=group + i)
            rig.cube(seg, (0, -.25, -.25, q(length), .25, .5), 'vein')
        rig.cube(seg, (-.25, -.25, -.25, .75, .75, .75), 'node')


def ink_cap():
    head = Rig('ink_cap', 'HEAD', INK_CAP, hide_at=5)
    sc = head.part('crown_scales', pivot=(0, -8, 0), stage=2)
    for box, m in [((-1.5, -.75, -1, 1, .75, 1), 'cap'), ((.5, -1, 0, 1, 1, 1), 'cap_light'),
                   ((-.5, -.75, 1, 1, .75, 1), 'cap'), ((1.5, -.5, -1.5, .75, .5, .75), 'cap_light'),
                   ((-2.5, -.5, .5, .75, .5, .75), 'cap'), ((-.25, -1.25, -.5, .5, .5, .5), 'cap_light')]:
        head.cube(sc, box, m)
    cap = head.part('cap', pivot=(0, -7, 0), stage=3, motion='SWAY', amp=.015, speed=.04)
    for box, m in [((-5, .5, -5, 10, 1.5, 10), 'cap_dark'), ((-4.75, -2.5, -4.75, 9.5, 3, 9.5), 'cap'),
                   ((-4.25, -5.5, -4.25, 8.5, 3, 8.5), 'cap'), ((-3.5, -8, -3.5, 7, 2.5, 7), 'cap_light'),
                   ((-2.5, -10, -2.5, 5, 2, 5), 'cap'), ((-1.5, -11, -1.5, 3, 1, 3), 'cap_light')]:
        head.cube(cap, box, m)
    for i, (x, y, z, w, d) in enumerate(((-5.25, -1.5, -2, .5, 1.5), (4.75, -2, 1, .5, 2), (-1, -2.25, -5.25, 2, .5),
                                         (2, -1, 4.75, 1.5, .5), (-4.75, -4.5, 1.5, .5, 1.5), (4.25, -4, -2.5, .5, 1.5),
                                         (1, -4.75, -4.75, 1.5, .5), (-2.5, -4.25, 4.25, 1.5, .5),
                                         (-4, -7.5, -1, .5, 1.5), (3.5, -7, .5, .5, 1.5))):
        head.cube(cap, (x, y, z, w, 1.5, d), 'cap_light' if i % 2 else 'cap')
    for i, (x, z) in enumerate(((-4.5, -4.5), (-1.5, -5), (2, -5), (4.5, -3.5), (5, 0), (4.5, 4.5), (1, 5), (-2.5, 5),
                                (-5, 2.5), (-5, -1.5))):
        d = head.part(f'drip_{i}', 'cap', (x, 2, z), stage=3, motion='DRIP', speed=.018 + (i % 3) * .004,
                      phase=i * .37)
        head.cube(d, (-.25, 0, -.25, .5, 1.5 + (i % 3) * .5, .5), 'drip')
    veil = head.part('veil', pivot=(0, -5, 0), stage=4, motion='SWAY', amp=.03, speed=.035)
    for box in [(-4.75, 0, 4.5, 9.5, 7, .25), (4.5, 0, -3, .25, 7, 7.5), (-4.75, 0, -3, .25, 7, 7.5)]:
        head.cube(veil, box, 'veil')
    for i, (x, z) in enumerate(((-3.5, 4.5), (0, 4.5), (3, 4.5), (4.5, 1), (-4.75, -1.5), (4.5, -2.5))):
        d = head.part(f'veil_drip_{i}', 'veil', (x, 7, z), stage=4, motion='DRIP', speed=.015, phase=i * .41)
        head.cube(d, (0, 0, 0, .25, 1.5, .25), 'drip')
    hood = head.part('hood', pivot=(0, 0, 0), stage=5)
    head.cube(hood, (-4.25, -8.5, -4.25, 8.5, 8.5, 8.5), 'ink')
    head.cube(hood, (-3.5, -9.25, -3.5, 7, .75, 7), 'ink')
    head.cube(hood, (-4.5, -6, -4.5, 9, 6, .25), 'veil')
    for i, x in enumerate((-2.5, -1, .5, 2)):
        head.chain(f'filament_{i}', 'hood', (x, -4.5 + (i % 2), -4.5), (-.25, 0, x * .05), 3,
                   (-.125, 0, -.125, .25, 2.5, .25), ['filament'], 5, motion='RIPPLE', amp=.2, speed=.05, phase0=i)
    body = Rig('ink_cap', 'BODY', INK_CAP, visible_from=5)
    v = body.part('body_veil', pivot=(0, 0, 0), stage=5, motion='SWAY', amp=.02, speed=.035)
    for box in [(-4.5, -.5, 2.25, 9, 12.5, .25), (4.25, -.5, -2.5, .25, 12.5, 5), (-4.5, -.5, -2.5, .25, 12.5, 5),
                (-4.5, -.5, -2.5, 3, 12.5, .25), (1.5, -.5, -2.5, 3, 12.5, .25), (-4.5, -.75, -2.5, 9, .5, 5)]:
        body.cube(v, box, 'veil')
    for i, (x, z) in enumerate(((-3.5, 2.25), (-.5, 2.25), (2.5, 2.25), (4.25, 0), (-4.5, -1), (-3, -2.5), (3, -2.5))):
        d = body.part(f'hem_drip_{i}', 'body_veil', (x, 12, z), stage=5, motion='DRIP', speed=.014, phase=i * .29)
        body.cube(d, (0, 0, 0, .25, 1.5, .25), 'drip')
    return [head, body]


def hushbell():
    body = Rig('hushbell', 'BODY', HUSHBELL, glow='BREATH', hide_head_flags=('FOLD_SHUT', 'CRYPTOBIOSIS'))
    for s in (-1, 1):
        f = body.part(f'flap_{side(s)}', pivot=(s * 3, 0, 0), rotation=(0, 0, s * .25), stage=2, motion='BREATHE',
                      amp=.04, phase=s)
        body.cube(f, (-1.5, -.5, -2.25, 3, .5, 4.5), 'mantle')
        body.cube(f, (-1.25, -.75, -2, .5, .25, 4), 'fold')
        body.cube(f, (.25, -.75, -2, .5, .25, 4), 'fold')
        body.cube(f, (s * 1.5 - (0 if s > 0 else .5), -.25, -2.25, .5, 1.25, 4.5), 'mantle_dark')
    mantle = body.part('mantle', stage=3, motion='BREATHE', amp=.025)
    panel_ring(body, mantle, 0, 8, 4.5, 2.75, 'mantle', 'mantle_dark')
    body.cube(mantle, (-4.75, .25, -3, 9.5, 1.5, .25), 'mantle_dark')
    body.cube(mantle, (-4.75, .25, 2.75, 9.5, 1.5, .25), 'mantle_dark')
    bell = body.part('bell', stage=4, motion='BREATHE', amp=.02, phase=.4)
    for i, (y0, h, rx, rz) in enumerate(((0, 6, 5, 3.25), (6, 6, 5.75, 4), (12, 6, 6.5, 4.75))):
        panel_ring(body, bell, y0, h, rx, rz, 'mantle' if i % 2 == 0 else 'mantle_dark', 'mantle_dark')
    rim_ring(body, bell, 17, 1.5, 6.75, 5, 'rim')
    lower = body.part('bell_lower', stage=5, motion='BREATHE', amp=.02, phase=.8)
    panel_ring(body, lower, 18, 5, 7.25, 5.5, 'mantle', 'mantle_dark')
    rim_ring(body, lower, 22.5, 1.5, 7.5, 5.75, 'rim')
    ring = body.part('hood_ring', stage=5, motion='BREATHE', amp=.03, phase=1.2)
    rim_ring(body, ring, -2, 2, 5, 5, 'mantle_dark')
    body.cube(ring, (-5.25, -2.5, -5.25, 10.5, .5, .5), 'frost')
    dome = body.part('dome', pivot=(0, -2, 0), stage=5, motion='GROW_ON', flags=('FOLD_SHUT', 'CRYPTOBIOSIS'))
    for box, m in [((-5, -3, -5, 10, 3, 10), 'mantle'), ((-4, -6, -4, 8, 3, 8), 'mantle'),
                   ((-3, -8, -3, 6, 2, 6), 'mantle_dark'), ((-1.5, -9, -1.5, 3, 1, 3), 'rim'),
                   ((-5.25, -1, -5.25, 10.5, 1, .25), 'fold'), ((-.25, -8, -3.25, .5, 8, .25), 'fold'),
                   ((-.25, -8, 3, .5, 8, .25), 'fold'), ((-.5, -10, -.5, 1, 1, 1), 'frost')]:
        body.cube(dome, box, m)
    return [body]


def panel_ring(rig, part, y0, h, rx, rz, m, ridge):
    rig.cube(part, (-rx, y0, -rz - .5, 2 * rx, h, .5), m)
    rig.cube(part, (-rx, y0, rz, 2 * rx, h, .5), m)
    rig.cube(part, (rx, y0, -rz, .5, h, 2 * rz), m)
    rig.cube(part, (-rx - .5, y0, -rz, .5, h, 2 * rz), m)
    for x in (-rx * .66, -rx * .2, rx * .25, rx * .66):
        x = q(x)
        rig.cube(part, (x, y0 + .5, -rz - .75, .5, h - .5, .25), 'fold')
        rig.cube(part, (x, y0 + .5, rz + .5, .5, h - .5, .25), 'fold')
    rig.cube(part, (rx + .5, y0 + .5, q(-rz * .3), .25, h - .5, .5), ridge)
    rig.cube(part, (-rx - .75, y0 + .5, q(rz * .3), .25, h - .5, .5), ridge)


def rim_ring(rig, part, y0, h, rx, rz, m):
    rig.cube(part, (-rx, y0, -rz - .75, 2 * rx, h, .75), m)
    rig.cube(part, (-rx, y0, rz, 2 * rx, h, .75), m)
    rig.cube(part, (rx, y0, -rz - .75, .75, h, 2 * rz + 1.5), m)
    rig.cube(part, (-rx - .75, y0, -rz - .75, .75, h, 2 * rz + 1.5), m)


def marrowplume():
    arms = Rig('marrowplume', 'ARMS', MARROW)
    for i, (pivot, rot) in enumerate((((3, 8.5, -1), (0, 0, 1.25)), ((3, 9, 1), (0, 0, 1.4)), ((1.5, 10, -2), (-1.3, 0, 0)),
                                      ((0, 9.5, -2), (-1.2, 0, -.2)), ((2, 8, 2), (1.25, 0, .2)))):
        c = arms.part(f'knuckle_club_{i}', pivot=pivot, rotation=rot, stage=2, limb='LOWER', motion='SWAY', amp=.04,
                      speed=.05, phase=i)
        arms.cube(c, (-.25, -1, -.25, .5, 1, .5), 'club_dark')
        arms.cube(c, (-.5, -1.75, -.5, 1, .75, 1), 'club')
    for i, (x, z) in enumerate(((-.5, -1), (1, .5), (2.5, -1.25))):
        arms.chain(f'feeding_tendril_{i}', 'root', (x, 10, z), (0, 0, (x - 1) * .15), 2, (-.125, 0, -.125, .25, 1.5, .25),
                   ['tendril'], 2, limb='LOWER', motion='SWAY', amp=.12, speed=.06, phase0=i)
    sheath = arms.part('wrist_sheath', pivot=(1, 8, 0), stage=3, limb='LOWER')
    arms.cube(sheath, (-2.25, -1, -2.25, 4.5, 2, 4.5), 'bone')
    arms.cube(sheath, (-2.5, .75, -2.5, 5, .5, 5), 'bone_dark')
    for i, (pivot, rot, h, st) in enumerate((((3, 4.5, 0), (0, 0, .5), 3, 3), ((3, 6.5, -1.25), (-.2, 0, .6), 2.5, 3),
                                             ((2, 5.5, 2), (.5, 0, .2), 3, 3), ((0, 4, 2), (.6, 0, -.15), 2.5, 3),
                                             ((3, 3, 1.25), (.3, 0, .55), 4.5, 4), ((1, 6, 2), (.55, 0, 0), 4, 4),
                                             ((3, 6, .75), (.1, 0, .75), 4, 4))):
        stalk_with_crown(arms, f'stalk_{i}', 'root', pivot, rot, h, st, 'LOWER', i)
    us = arms.part('upper_sheath', pivot=(1, 0, 0), stage=4, limb='UPPER')
    arms.cube(us, (-2.25, -2.25, -2.25, 4.5, 4, 4.5), 'bone')
    arms.cube(us, (-2.5, 1.5, -2.5, 5, .5, 5), 'bone_dark')
    ps = arms.part('primal_sheath', pivot=(1, 4, 0), stage=5, limb='LOWER')
    arms.cube(ps, (-2.25, -2, -2.25, 4.5, 5, 4.5), 'bone')
    for i, (pivot, rot) in enumerate((((1, -2, 1.5), (.35, 0, .1)), ((2.5, -2, -.5), (-.1, 0, .45)),
                                      ((-.25, -2, -1), (-.35, 0, -.2)), ((2, -1, 2), (.6, 0, .35)))):
        stalk_with_crown(arms, f'shoulder_plume_{i}', 'root', pivot, rot, 5.5, 5, 'UPPER', i + 3)
    for i, (x, y, z) in enumerate(((3.25, 9, .5), (1, 9, 2.25), (-1.25, 9, -.5), (3.25, 2, -1), (1, 2, 2.25))):
        b = arms.part(f'bead_{i}', pivot=(x, y, z), stage=3 if y > 5 else 4, limb='LOWER' if y > 4 else 'UPPER',
                      motion='DRIP', speed=.02, phase=i * .31)
        arms.cube(b, (-.25, 0, -.25, .5, .5, .5), 'bead')
    legs = Rig('marrowplume', 'LEGS', MARROW, visible_from=4)
    for i, (pivot, rot, h) in enumerate((((0, 7.5, 2), (.5, 0, 0), 3.5), ((1.75, 8.5, 1.75), (.45, 0, .4), 3),
                                         ((-1.5, 9, 2), (.55, 0, -.3), 2.5))):
        stalk_with_crown(legs, f'calf_stalk_{i}', 'root', pivot, rot, h, 4, 'LOWER', i)
    ls = legs.part('leg_sheath', pivot=(0, 8, 0), stage=5, limb='LOWER')
    legs.cube(ls, (-2.25, -3, -2.25, 4.5, 6, 4.5), 'bone')
    legs.cube(ls, (-2.5, 2.5, -2.5, 5, .5, 5), 'bone_dark')
    for i, (x, z, yaw) in enumerate(((-1.75, -1.75, PI * .75), (1.75, -1.75, PI * .25), (0, 2, -PI * .5), (1.75, 1.5, -PI * .2))):
        legs.chain(f'fall_root_{i}', 'root', (x, 12, z), (0, yaw, 1.0), 2, (-.25, 0, -.25, .5, 3.5, .5),
                   ['club', 'club_dark'], 5, limb='LOWER', motion='GROW_ON', amp=0, bend=(0, 0, -.5),
                   flags=('WHALE_FALL_ROOTED',))
    return [arms, legs]


def stalk_with_crown(rig, name, parent, pivot, rot, h, stage, limb, i):
    s = rig.part(name, parent, pivot, rot, stage, limb, 'SWAY', .07, .05, i * 1.3)
    rig.cube(s, (-.25, -h, -.25, .5, h, .5), 'club')
    rig.cube(s, (-.375, -1, -.375, .75, 1, .75), 'bone_dark')
    crown = rig.part(f'{name}_crown', s, (0, -h, 0), (0, i * .7, 0), stage, motion='SWAY', amp=.1, speed=.07, phase=i)
    rig.cube(crown, (-.5, -1, -.5, 1, 1, 1), 'club')
    rig.cube(crown, (-1.5, -1.25, -.125, 3, .25, .25), 'plume')
    rig.cube(crown, (-.125, -1.25, -1.5, .25, .25, 3), 'plume')
    rig.cube(crown, (-.5, -1.75, -.5, 1, .5, 1), 'plume_light')
    full = rig.part(f'{name}_crown_full', crown, (0, -1, 0), (0, .78, 0), max(stage, 4))
    rig.cube(full, (-1.75, -.5, -.125, 3.5, .25, .25), 'plume')
    rig.cube(full, (-.125, -.5, -1.75, .25, .25, 3.5), 'plume_light')


def kinbud():
    mane = ['amber', 'flame', 'flame', 'crimson']
    body = Rig('kinbud', 'BODY', KINBUD, glow='FLAME')
    for i in range(5):
        b = body.part(f'spine_bud_{i}', pivot=(0, 1.5 + i * 2.1, 2), stage=2, motion='BREATHE', amp=.06, phase=i * .6)
        size = q(1.5 - i * .125)
        body.cube(b, (-size / 2, -size / 2, 0, size, size, 1), "bud")
        body.cube(b, (-.25, -.25, .75, .5, .5, .5), 'amber')
    for i, x in enumerate((-3, -1.75, -.5, .75, 2, 3.25)):
        body.chain(f'mane_{i}', 'root', (q(x - .25), 0, 2.25), (.18 + (i % 2) * .08, 0, (x - .1) * .06), 4,
                   (-.5, 0, -.25, 1, 3.25, .5), mane, 4, motion='FLAME', amp=.16, speed=.22, phase0=i * .9,
                   bend=(-.04, 0, 0))
    for flag, s in (('SECOND_HEAD', -1), ('SECOND_HEAD_TWIN', 1)):
        root = body.part(f'second_head_{side(s)}', pivot=(s * 3.25, 0, .5), stage=4, motion='GROW_ON', flags=(flag,))
        neck = body.part(f'second_head_{side(s)}_neck', root, (0, 0, 0), (0, 0, s * .5), 4, motion='SWAY', amp=.08,
                         speed=.06, phase=s)
        body.cube(neck, (-1, -4, -1, 2, 4, 2), 'pale')
        body.cube(neck, (-.5, -3.5, .75, 1, 3, .5), 'vein')
        neck2 = body.part(f'second_head_{side(s)}_neck2', neck, (0, -4, 0), (0, 0, -s * .3), 4, motion='SWAY',
                          amp=.1, speed=.07, phase=s + 1)
        body.cube(neck2, (-1, -3.5, -1, 2, 3.5, 2), 'pale')
        head = body.part(f'second_head_{side(s)}_head', neck2, (0, -3.5, 0), (0, 0, -s * .15), 4)
        body.cube(head, (-2.25, -4.5, -2.25, 4.5, 4.5, 4.5), 'pale')
        body.cube(head, (-2, -4.75, -2, 4, .25, 4), 'bud')
        for j, (dx, dz) in enumerate(((-1.75, -.75), (1.25, -.75), (-.75, 1.25), (.25, -1.75))):
            body.chain(f'second_head_{side(s)}_crown_{j}', head, (dx * 1.25 + .25, -4.25, dz * 1.25 + .25), (-.4 if dz < 0 else .4, 0, dx * .3),
                       3, (-.375, 0, -.25, .75, 2, .5), ['amber', 'flame', 'crimson'], 4, motion='FLAME', amp=.2, speed=.25,
                       phase0=j)
    head = Rig('kinbud', 'HEAD', KINBUD, glow='FLAME', hide_at=5)
    for i, x in enumerate((-2, 0, 2)):
        b = head.part(f'nape_bud_{i}', pivot=(x, -1.5 + abs(x) * .25, 4), stage=2, motion='BREATHE', amp=.05, phase=i)
        head.cube(b, (-.5, -.5, 0, 1, 1, .75), 'bud')
    for i, x in enumerate((-2.75, -1.25, .25, 1.75, 3)):
        head.chain(f'nape_mane_{i}', 'root', (q(x - .25), -2.75, 4), (.55, 0, (x - .1) * .08), 4,
                   (-.375, 0, -.25, .75, 2.5, .5), mane, 3, motion='FLAME', amp=.2, speed=.24, phase0=i * .8,
                   stages=(3, 3, 4, 4), bend=(-.05, 0, 0))
    polyp = head.part('polyp', stage=5)
    head.cube(polyp, (-4, -8, -4, 8, 8, 8), 'pale')
    head.cube(polyp, (-3.5, -8.5, -3.5, 7, .5, 7), 'bud')
    head.cube(polyp, (-4.25, -6, -3.5, .25, 5, 7), 'pale')
    head.cube(polyp, (4, -6, -3.5, .25, 5, 7), 'pale')
    head.cube(polyp, (-3.5, -6, -4.25, 7, 5, .25), 'pale')
    head.cube(polyp, (-1.5, -5, -4.5, 3, .5, .25), 'vein')
    for i in range(10):
        angle = i / 10 * 2 * PI
        x, z = 3.75 * math.sin(angle), 3.75 * math.cos(angle)
        head.chain(f'crown_{i}', 'polyp', (q(x), -7.75, q(z)), (2.25, angle, 0), 3, (-.375, 0, -.25, .75, 2.75, .5),
                   ['amber', 'flame', 'crimson'], 5, motion='FLAME', amp=.18, speed=.24, phase0=i * .63,
                   bend=(-.55, 0, 0))
    return [body, head]


def side(s):
    return 'l' if s > 0 else 'r'


def q(v):
    return round(v * 4) / 4


STRAINS = [sunburst, holdfast, mermaids_purse, dead_mans_rope, ink_cap, hushbell, marrowplume, kinbud]


# --------------------------------------------------------------------------------------------------------------
#  Export: texture atlas, Java mesh, Blockbench project


def faces(u, v, w, h, d):
    return dict(north=[u + d, v + d, u + d + w, v + d + h], east=[u, v + d, u + d, v + d + h],
                south=[u + d + w + d, v + d, u + d + w + d + w, v + d + h], west=[u + d + w, v + d, u + d + w + d, v + d + h],
                up=[u + d + w, v + d, u + d, v], down=[u + d + w + w, v, u + d + w, v + d])


def noise(*values):
    h = 2166136261
    for value in values:
        h = (h ^ (int(value) & 0xFFFFFFFF)) * 16777619 & 0xFFFFFFFF
    return (h >> 8 & 0xFFFF) / 65535.0


def paint(material, face, lx, ly, fw, fh, seed):
    r, g, b, a = material['rgba']
    style = material['style']
    shade = {'up': 16, 'down': -22, 'east': -9, 'west': -6, 'north': 4, 'south': 0}[face]
    n = noise(seed, lx, ly, ord(face[0]))
    shade += int((n - .5) * 18)
    glow = None
    colour = None
    if style == 'crust':
        if n > .82: shade -= 26
        elif n < .12: shade += 20
    elif style == 'fleck':
        shade += 10 if (lx + ly) % 2 == 0 else -6
    elif style == 'frond':
        if fw >= 3 and lx == fw // 2: colour = (196, 120, 36)
        elif (lx + ly * 2) % 5 == 0: shade -= 18
        if material['glow'] and (lx + ly) % 3 != 0: glow = material['glow']
    elif style == 'rib':
        if material['glow'] and ly % 2 == 0: glow = material['glow']
    elif style == 'root':
        if lx % 2 == 0: shade -= 10
        if n > .9: colour = (214, 140, 150)
    elif style == 'stone':
        if n > .9: colour = (214, 140, 150)
        elif n < .08: shade -= 24
    elif style == 'scale':
        if (lx + ly) % 3 == 0: shade += 12
    elif style == 'leather':
        if ly % 2 == 0: shade -= 14
        if n > .85: shade += 18
    elif style in ('fin', 'sail'):
        if lx % 2 == 0: shade -= 12
        if style == 'sail' and material['glow'] and (ly % 3 == 1 or lx % 4 == 0): glow = material['glow']
    elif style in ('vein', 'strand'):
        if material['glow'] and (style == 'vein' or ly % 2 == 0): glow = material['glow']
    elif style == 'shag':
        if lx % 2 == 0: shade -= 16
        if ly == fh - 1 and fh > 1: colour = (36, 30, 40)
    elif style == 'veil':
        if lx % 3 == 0: shade += 8
    elif style == 'fold':
        if lx % 3 == 0: shade -= 18
        if material['glow'] and lx % 3 == 1 and ly % 4 == 0: glow = material['glow']
    elif style == 'bone':
        if n > .88: colour = (206, 22, 34)
        elif n < .1: shade -= 18
    elif style == 'club':
        if ly % 3 == 0: shade -= 14
    elif style == 'plume':
        if (lx + ly) % 2 == 0: shade += 14
    elif style == 'polyp':
        if n > .86: colour = (200, 70, 70)
    elif style == 'mane':
        if material['glow'] and (lx + ly) % 2 == 0: glow = material['glow']
        if lx % 2 == 1: shade -= 12
    if style not in ('vein', 'strand', 'mane', 'frond', 'rib', 'sail') and material['glow'] and n > .7:
        glow = material['glow']
    rgb = colour or tuple(max(0, min(255, c + shade)) for c in (r, g, b))
    return (*rgb, a), (None if glow is None else (*glow, 255))


def atlas_size(rig):
    for size in ((64, 64), (128, 64), (128, 128), (256, 128), (256, 256)):
        if pack(rig, size, dry=True):
            return size
    raise AssertionError(f'{rig.name} does not fit a 256x256 atlas')


def pack(rig, size, dry=False):
    u, v, row = 0, 0, 0
    placed = []
    for part in rig.parts:
        for cube in part['cubes']:
            x, y, z, w, h, d = cube['box']
            width, height = math.ceil(2 * (w + d)), math.ceil(h + d)
            if u + width > size[0]:
                u, v, row = 0, v + row, 0
            if v + height > size[1] or width > size[0]:
                return None
            placed.append((cube, u, v))
            row = max(row, height)
            u += width
    return placed


def export(rig):
    size = atlas_size(rig)
    placed = {id(c): (u, v) for c, u, v in pack(rig, size)}
    image, glow = Image.new('RGBA', size), Image.new('RGBA', size)
    has_glow = False
    elements, groups, world = [], {}, {'root': (0, 0, 0)}
    lines = ['        MeshDefinition mesh = new MeshDefinition();', '        PartDefinition root = mesh.getRoot();']
    for index, part in enumerate(rig.parts):
        name, parent, pivot = part['name'], part['parent'], part['pivot']
        absolute = tuple(a + b for a, b in zip(world[parent], pivot))
        world[name] = absolute
        rot_deg = [math.degrees(a) for a in part['rotation']]
        group = dict(name=name, origin=[absolute[0], 24 - absolute[1], absolute[2]],
                     rotation=[-rot_deg[0], rot_deg[1], -rot_deg[2]], uuid=uid(rig.name + '/' + name), children=[],
                     export=True, isOpen=False, visibility=True)
        groups[name] = group
        if parent != 'root':
            groups[parent]['children'].append(group)
        chain = 'CubeListBuilder.create()'
        for ci, cube in enumerate(part['cubes']):
            x, y, z, w, h, d = cube['box']
            u, v = placed[id(cube)]
            material = rig.materials[cube['material']]
            uv = faces(u, v, w, h, d)
            for face, rect in uv.items():
                x0, x1 = sorted((rect[0], rect[2]))
                y0, y1 = sorted((rect[1], rect[3]))
                fx0, fy0 = math.floor(x0), math.floor(y0)
                fw, fh = max(1, math.ceil(x1) - fx0), max(1, math.ceil(y1) - fy0)
                for py in range(fy0, fy0 + fh):
                    for px in range(fx0, fx0 + fw):
                        if px >= size[0] or py >= size[1]:
                            continue
                        colour, lit = paint(material, face, px - fx0, py - fy0, fw, fh, index * 31 + ci)
                        image.putpixel((px, py), colour)
                        if lit:
                            glow.putpixel((px, py), lit)
                            has_glow = True
            eid = uid(rig.name + '/' + name + '/' + str(ci))
            group['children'].append(eid)
            elements.append({'name': f'{name}_{ci}', 'uuid': eid, 'type': 'cube',
                             'from': [absolute[0] + x, 24 - (absolute[1] + y + h), absolute[2] + z],
                             'to': [absolute[0] + x + w, 24 - (absolute[1] + y), absolute[2] + z + d],
                             'origin': group['origin'], 'uv_offset': [u, v], 'autouv': 0,
                             'faces': {key: dict(uv=val, texture=0) for key, val in uv.items()}})
            chain += f'\n                .texOffs({u}, {v}).addBox({jf(x)}, {jf(y)}, {jf(z)}, {jf(w)}, {jf(h)}, {jf(d)})'
        rot = part['rotation']
        pose = ', '.join(f'{float(n):.6f}F' for n in (*pivot, *rot))
        lines.append(f'        PartDefinition {name} = {parent}.addOrReplaceChild("{name}", {chain}, '
                     f'PartPose.offsetAndRotation({pose}));')
    lines.append(f'        return LayerDefinition.create(mesh, {size[0]}, {size[1]});')
    assert TEXELS_PER_UNIT == 1 and image.size == size, 'one texture pixel per model unit (16 px per block)'
    tex_dir = ASSETS / 'textures/models/morphling'
    tex_dir.mkdir(parents=True, exist_ok=True)
    image.save(tex_dir / f'{rig.name}.png')
    glow_path = tex_dir / f'{rig.name}_glow.png'
    if has_glow and rig.glow != 'NONE':
        glow.save(glow_path)
    elif glow_path.exists():
        glow_path.unlink()
    rig.has_glow = has_glow and rig.glow != 'NONE'
    rig.size = size
    rig.translucent = any(rig.materials[c['material']]['rgba'][3] < 255 for p in rig.parts for c in p['cubes'])
    source = 'data:image/png;base64,' + base64.b64encode((tex_dir / f'{rig.name}.png').read_bytes()).decode()
    bb = dict(meta=dict(format_version='4.10', model_format='free', box_uv=True), name=rig.java_name,
              model_identifier=rig.name, resolution=dict(width=size[0], height=size[1]), elements=elements,
              outliner=[groups[p['name']] for p in rig.parts if p['parent'] == 'root'],
              textures=[dict(name=f'{rig.name}.png', uuid=uid(rig.name + '/texture'), id='0', source=source,
                             mode='bitmap', render_mode='default', visible=True)], animations=[])
    bbpath = ASSETS / 'models/entity/bbmodel/morphling' / f'{rig.java_name}.bbmodel'
    bbpath.parent.mkdir(parents=True, exist_ok=True)
    bbpath.write_text(json.dumps(bb, indent=1) + '\n', encoding='utf-8')
    write_java(rig, '\n'.join(lines))


def jf(v):
    return f'{float(v):g}F'.replace('.0F', 'F') if float(v).is_integer() else f'{float(v):g}F'


def flags_expr(flags):
    return ' | '.join(f'MorphlingActiveState.{f}' for f in flags) if flags else '0'


def write_java(rig, body):
    specs = []
    for p in rig.parts:
        grows = p['parent'] == 'root' or rig.get(p['parent'])['stage'] < p['stage']
        specs.append(f'            new MorphlingPartSpec("{p["name"]}", "{p["parent"]}", {p["stage"]}, '
                     f'MorphlingPartSpec.Limb.{p["limb"]}, MorphlingPartSpec.Motion.{p["motion"]}, '
                     f'{p["amp"]:.3f}F, {p["speed"]:.3f}F, {p["phase"]:.3f}F, {flags_expr(p["flags"])}, '
                     f'{p["glow_group"]}, {str(grows).lower()})')
    glow_tex = (f'Hemomancy.rloc("textures/models/morphling/{rig.name}_glow.png")' if rig.has_glow else 'null')
    source = f'''package com.vincenthuto.hemomancy.client.model.entity.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * {rig.strain.replace('_', ' ').title()} Morphling, {rig.anchor} anchor. Generated by tools/model_export/morphling_strains.py;
 * edit the generator, not this file. One texture pixel per model unit (16 px per block).
 */
public final class {rig.java_name} extends MorphlingStrainModel {{
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("strain_{rig.name}"), "main");
    public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/models/morphling/{rig.name}.png");
    public static final ResourceLocation GLOW_TEXTURE = {glow_tex};
    public static final List<MorphlingPartSpec> PARTS = List.of(
{(',' + chr(10)).join(specs)});

    public {rig.java_name}(ModelPart root) {{
        super(root, PARTS, TEXTURE, GLOW_TEXTURE, Glow.{rig.glow}, {str(rig.translucent).lower()});
    }}

    public static LayerDefinition createBodyLayer() {{
{body}
    }}
}}
'''
    JAVA.mkdir(parents=True, exist_ok=True)
    (JAVA / f'{rig.java_name}.java').write_text(source, encoding='utf-8')


def write_index(rigs):
    rows = []
    for rig in rigs:
        first = min(p['stage'] for p in rig.parts)
        assert first >= rig.visible_from, (rig.name, first, rig.visible_from)
        rows.append(f'            new Anchor("{rig.strain}", AttachmentPoint.{rig.anchor}, {rig.java_name}.LAYER_LOCATION, '
                    f'{rig.java_name}::createBodyLayer, {rig.java_name}::new, {rig.java_name}.PARTS, {rig.java_name}.TEXTURE, {first}, {rig.hide_at}, '
                    f'{rig.primal_scale:.2f}F, {flags_expr(rig.hide_head_flags)})')
    source = f'''package com.vincenthuto.hemomancy.client.model.entity.morphling;

import com.vincenthuto.hemomancy.client.model.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Every strain's attachment anchors, generated by tools/model_export/morphling_strains.py with the models it lists.
 * The client registry builds each strain's mutation from these rows, and Track 9's Court NPCs dress from them.
 */
public final class MorphlingStrainModels {{
    /**
     * One anchor of one strain.
     *
     * @param strainId       the strain table id ({{@code MorphlingStrain.id()}})
     * @param parts          the model's part table (stages, limb tags, motions)
     * @param visibleFrom    the first stage any part grows at
     * @param hideAt         the stage at which the anchor replaces the player part it sits on, or -1
     * @param primalScale    the whole attachment's scale at Primal
     * @param hideHeadFlags  active-state bits that also hide the head at Primal (Hushbell's closed bell), or 0
     */
    public record Anchor(String strainId, AttachmentPoint point, ModelLayerLocation layer,
                         Supplier<LayerDefinition> definition, Function<ModelPart, MorphlingStrainModel> factory,
                         List<MorphlingPartSpec> parts, ResourceLocation texture,
                         int visibleFrom, int hideAt, float primalScale, int hideHeadFlags) {{
    }}

    public static final List<Anchor> ALL = List.of(
{(',' + chr(10)).join(rows)});

    private MorphlingStrainModels() {{
    }}

    public static List<Anchor> forStrain(String strainId) {{
        return ALL.stream().filter(anchor -> anchor.strainId().equals(strainId)).toList();
    }}
}}
'''
    (JAVA / 'MorphlingStrainModels.java').write_text(source, encoding='utf-8')


# --------------------------------------------------------------------------------------------------------------
#  Preview: an orthographic three-quarter back view of the anchor part with each stage's growth


def rot_matrix(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


def stage_scale(part, rig, stage):
    grows = part['parent'] == 'root' or rig.get(part['parent'])['stage'] < part['stage']
    if not grows:
        return 1.0
    k = stage - part['stage']
    return .7 if k <= 0 else .88 if k == 1 else 1.0


def world_cubes(rig, stage, flags=()):
    transforms = {'root': (np.eye(3), np.zeros(3))}
    out = []
    for part in rig.parts:
        if stage < part['stage']:
            transforms[part['name']] = None
            continue
        parent = transforms[part['parent']]
        if parent is None:
            transforms[part['name']] = None
            continue
        if part['motion'] == 'GROW_ON' and not (set(part['flags']) & set(flags)):
            transforms[part['name']] = None
            continue
        pm, pt = parent
        scale = stage_scale(part, rig, stage)
        m = pm @ rot_matrix(*part['rotation']) * scale
        t = pt + pm @ np.array(part['pivot'], dtype=float)
        transforms[part['name']] = (m, t)
        for cube in part['cubes']:
            x, y, z, w, h, d = cube['box']
            corners = np.array([[x + dx * w, y + dy * h, z + dz * d] for dx in (0, 1) for dy in (0, 1) for dz in (0, 1)])
            out.append((t + corners @ m.T, rig.materials[cube['material']]['rgba']))
    return out


QUADS = [(0, 1, 3, 2), (4, 6, 7, 5), (0, 4, 5, 1), (2, 3, 7, 6), (0, 2, 6, 4), (1, 5, 7, 3)]


def draw_panel(draw, cubes, centre, scale, yaw, pitch, focus=(0, 0, 0), light=(.4, -.8, .5)):
    view = rot_matrix(pitch, 0, 0) @ rot_matrix(0, yaw, 0)
    shift = np.array(focus, dtype=float) @ view.T
    lv = np.array(light) / np.linalg.norm(light)
    polys = []
    for corners, rgba in cubes:
        v = corners @ view.T - shift
        for quad in QUADS:
            pts = v[list(quad)]
            normal = np.cross(pts[1] - pts[0], pts[2] - pts[0])
            n = np.linalg.norm(normal)
            if n < 1e-9:
                continue
            normal /= n
            brightness = .55 + .45 * abs(float(normal @ lv))
            depth = float(pts[:, 2].mean())
            colour = tuple(int(c * brightness) for c in rgba[:3]) + (rgba[3],)
            polys.append((depth, [(centre[0] + p[0] * scale, centre[1] + p[1] * scale) for p in pts], colour))
    for _, pts, colour in sorted(polys, key=lambda p: -p[0]):
        draw.polygon(pts, fill=colour)


def preview(rig, flags=()):
    PREVIEW.mkdir(parents=True, exist_ok=True)
    panel, scale = 220, 7
    image = Image.new('RGBA', (panel * 6, panel + 24), (34, 30, 38, 255))
    for stage in range(6):
        layer = Image.new('RGBA', (panel, panel), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer, 'RGBA')
        x, y, z, w, h, d = ANCHOR_SPACE[rig.anchor]
        host = [(np.array([[x + dx * w, y + dy * h, z + dz * d] for dx in (0, 1) for dy in (0, 1) for dz in (0, 1)]),
                 (96, 92, 104, 255))]
        state = flags if stage == 5 else ()
        draw_panel(draw, host + world_cubes(rig, stage, state), (panel / 2, panel / 2), scale, math.radians(205),
                   math.radians(-18), focus=(x + w / 2, y + h / 2, z + d / 2))
        image.alpha_composite(layer, (stage * panel, 0))
        ImageDraw.Draw(image).text((stage * panel + 8, panel + 6), STAGES[stage], fill=(220, 214, 224, 255))
    image.save(PREVIEW / f'{rig.name}.png')
    return image


def contact(all_rigs):
    by_strain = {}
    for rig in all_rigs:
        by_strain.setdefault(rig.strain, []).append(rig)
    panel, scale = 260, 6
    image = Image.new('RGBA', (panel * len(by_strain), panel + 30), (34, 30, 38, 255))
    offsets = {'HEAD': (0, 0, 0), 'BODY': (0, 0, 0), 'ARMS': (5, 2, 0), 'LEGS': (1.9, 12, 0)}
    for i, (strain, rigs) in enumerate(by_strain.items()):
        layer = Image.new('RGBA', (panel, panel), (0, 0, 0, 0))
        draw = ImageDraw.Draw(layer, 'RGBA')
        cubes = []
        for anchor, (x, y, z, w, h, d) in ANCHOR_SPACE.items():
            for mirror in ((1, -1) if anchor in ('ARMS', 'LEGS') else (1,)):
                off = np.array(offsets[anchor], dtype=float) * np.array([mirror, 1, 1])
                c = np.array([[x + dx * w, y + dy * h, z + dz * d] for dx in (0, 1) for dy in (0, 1) for dz in (0, 1)])
                c = c * np.array([mirror, 1, 1]) + off
                cubes.append((c, (96, 92, 104, 255)))
        for rig in rigs:
            for mirror in ((1, -1) if rig.anchor in ('ARMS', 'LEGS') else (1,)):
                off = np.array(offsets[rig.anchor], dtype=float) * np.array([mirror, 1, 1])
                for corners, rgba in world_cubes(rig, 4):
                    cubes.append((corners * np.array([mirror, 1, 1]) + off, rgba))
        draw_panel(draw, cubes, (panel / 2, panel / 2), scale, math.radians(215), math.radians(-12), focus=(0, 8, 0))
        image.alpha_composite(layer, (i * panel, 0))
        ImageDraw.Draw(image).text((i * panel + 8, panel + 8), strain.replace('_', ' ').title(), fill=(220, 214, 224, 255))
    image.save(PREVIEW / 'apex_contact.png')


ACTIVE_PREVIEW = {'sunburst': ('ARBOR',), 'holdfast': ('ANCHORED',), 'mermaids_purse': ('SAIL_RAISED',),
                  'hushbell': ('FOLD_SHUT',), 'marrowplume': ('WHALE_FALL_ROOTED',),
                  'kinbud': ('SECOND_HEAD', 'SECOND_HEAD_TWIN')}


def main():
    rigs = [rig for strain in STRAINS for rig in strain()]
    for rig in rigs:
        export(rig)
        preview(rig, ACTIVE_PREVIEW.get(rig.strain, ()))
    write_index(rigs)
    contact(rigs)
    parts = sum(len(r.parts) for r in rigs)
    cubes = sum(len(p['cubes']) for r in rigs for p in r.parts)
    print(f'Exported {len(rigs)} strain anchors ({parts} parts, {cubes} cubes) at 16 px per block; '
          f'previews in {PREVIEW.relative_to(ROOT)}')


if __name__ == '__main__':
    main()
