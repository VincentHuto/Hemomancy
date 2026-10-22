"""Author the Puppeteer's Spindle: a Saxony spinning wheel grown from bone, sinew and muscle.

Emits the Java mesh, a painted UV atlas plus emissive layer, and an editable Blockbench project
through the shared quarter-grid Rig exporter. Runtime animation lives in PuppeteersSpindleModel.
Python 3 + Pillow + NumPy. `--preview` also writes textured turnaround renders to build/.

Coordinates are body-local entity-model units: x across, y down (ground at 0), z toward the
back. The renderer turns model -z toward the player, so the wheel is seen face-on.
"""
from pathlib import Path
import argparse
import hashlib
import json
import math
import shutil

import pelagic_ecology as mesh

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
BUILD = ROOT / 'build/puppeteers-spindle-assets'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/tile/crafting/PuppeteersSpindleMesh.java'
NAME = 'model_puppeteers_spindle'

HUB = (2.5, -9.5)          # wheel axle, body-local x/y
RIM = 5.75                 # outer rim radius (drive-band groove)
FLYER = (-5.25, -12.0)     # flyer/bobbin axis
WHORL = 1.5
CRANK = 1.25               # crank pin radius in wheel space
CRANK_Z = -3.75            # crank pin / footman plane
HINGE = (-5.25, -0.75)     # treadle hinge
REACH = 9.0                # hinge -> footman attachment
TRAVEL = 0.6               # treadle tip travel per unit of crank-pin travel

PALETTE = dict(
    wood=(84, 44, 47), bone=(214, 198, 168), bone_dark=(164, 140, 114), muscle=(142, 28, 40),
    sinew=(204, 158, 144), heart=(122, 19, 34), thread=(204, 30, 50), fibre=(118, 52, 70),
    claw=(58, 30, 34), vein=(96, 16, 36))


def footman_length():
    tip = (HINGE[0] + REACH, HINGE[1])
    return math.hypot(tip[0] - (HUB[0] + CRANK), tip[1] - HUB[1])


def quarter(value):
    return max(.25, round(value * 4) / 4)


def rod(r, name, p0, p1, material, thick=.5):
    """A body-level part aimed from p0 to p1 (body-local) along its local +z."""
    dx, dy, dz = (b - a for a, b in zip(p0, p1))
    length = math.sqrt(dx * dx + dy * dy + dz * dz)
    pitch = math.degrees(math.asin(-dy / length))
    yaw = math.degrees(math.atan2(dx, dz))
    r.part(name, 'body', p0, (pitch, yaw, 0))
    half = thick / 2 if thick >= .5 else 0
    r.cube(name, (-half, -half, 0, thick, thick, quarter(length)), material)


def band_tangents():
    """External tangent points between the wheel groove and the whorl, in body-local x/y."""
    c1, c2 = HUB, FLYER
    dx, dy = c2[0] - c1[0], c2[1] - c1[1]
    dist = math.hypot(dx, dy)
    base, spread = math.atan2(dy, dx), math.acos((RIM - WHORL) / dist)
    out = []
    for sign in (1, -1):
        n = (math.cos(base + sign * spread), math.sin(base + sign * spread))
        out.append(((c2[0] + WHORL * n[0], c2[1] + WHORL * n[1]), (c1[0] + RIM * n[0], c1[1] + RIM * n[1])))
    return out


def spindle():
    r = mesh.Rig(NAME)
    # Bench: two cured rails with the wheel running between them, a headstock and an end cap.
    for z in (-2.5, 1.25):
        r.cube('body', (-7, -5.5, z, 14, 1.5, 1.25), 'wood')
        r.cube('body', (-6.5, -5.75, z + .5, 13, .25, .5), 'vein')
        for x in (-2.75, 5.25):
            r.cube('body', (x, -5.75, z - .25, .75, 2, 1.75), 'sinew')
    r.cube('body', (-7.5, -6, -2.75, 4, 2, 5.5), 'wood')
    r.cube('body', (-7.75, -6.25, -3, 1, 1, 6), 'sinew')
    r.cube('body', (6.75, -5.5, -2.75, .75, 1.5, 5.5), 'wood')
    r.cube('body', (-6, -1, -2.75, 1, 1, 4.5), 'bone_dark')

    # Splayed femur legs with knuckled hips and condyle feet.
    for name, pivot, rot in [('leg_front', (-6.5, -4, -2.25), (-12, 0, 10)),
                             ('leg_back', (-6.5, -4, 2.25), (12, 0, 10)),
                             ('leg_right', (7, -4, 0), (0, 0, -10))]:
        r.part(name, 'body', pivot, rot)
        r.cube(name, (-.75, -.5, -.75, 1.5, 1.25, 1.5), 'bone_dark')
        r.cube(name, (-.5, .5, -.5, 1, 3, 1), 'bone')
        r.cube(name, (-.75, 3.25, -1, 1.5, 1, 2), 'bone_dark')

    # Axle uprights: a socketed back post and an open front cradle that leaves the heart in view.
    r.cube('body', (2, -10.75, 1.5, 1, 5.25, .75), 'bone')
    r.cube('body', (1.75, -10.5, 1.25, 1.5, 2, 1.25), 'bone_dark')
    r.cube('body', (2, -8.25, -2.25, 1, 2.75, .75), 'bone')
    r.cube('body', (1.25, -8.75, -2.5, 2.5, .75, 1.25), 'bone_dark')
    for x in (1.25, 3.25):
        r.cube('body', (x, -10.25, -2.5, .5, 1.5, 1.25), 'bone_dark')
    r.cube('body', (2, -10, -3.25, 1, 1, 6.5), 'bone')

    # The wheel: a ring of striated muscle on finger-bone spokes around a beating heart.
    r.part('wheel', 'body', (HUB[0], HUB[1], 0))
    for i in range(12):
        seg = r.part(f'rim{i}', 'wheel', rotation=(0, 0, i * 30))
        r.cube(seg, (-1.5, -RIM, -.75, 3, 1.5, 1.5), 'muscle')
        if i % 2 == 0:
            r.cube(seg, (-.5, -3, -.5, 1, 1.5, 1), 'bone')
            r.cube(seg, (-.75, -3.5, -.75, 1.5, .75, 1.5), 'bone_dark')
            r.cube(seg, (-.5, -4.5, -.5, 1, 1, 1), 'bone')
            r.cube(seg, (-.75, -6, -1, 1.5, 2, 2), 'bone')
        if i % 4 == 2:
            r.cube(seg, (.5, -4.25, -.25, .5, 2.75, .5), 'vein')
    r.part('heart', 'wheel')
    r.cube('heart', (-1.5, -1.5, -1.25, 3, 3, 2.5), 'heart')
    r.cube('heart', (-2, -1, -.75, 4, 2, 1.5), 'heart')
    r.cube('heart', (-1, -2, -1, 2, 4, 2), 'heart')
    r.cube('heart', (.25, -2.75, -.5, .75, 1.25, 1), 'vein')
    r.cube('heart', (-1.25, 1.75, -.5, 1, 1, 1), 'vein')
    r.part('crank', 'wheel', (0, 0, CRANK_Z + .25))
    r.cube('crank', (-.5, -.5, 0, 2, 1, .5), 'bone_dark')
    r.cube('crank', (CRANK - .25, -.25, -.5, .5, .5, .5), 'bone')

    # Treadle and footman; the model solves this crank linkage every frame.
    r.part('treadle', 'body', (HINGE[0], HINGE[1], -.5))
    r.cube('treadle', (0, -.5, -2.25, 4.5, .5, 4), 'wood')
    r.cube('treadle', (.5, -.75, -1.75, 3.5, .25, 3), 'sinew')
    r.cube('treadle', (0, -.5, -3.25, REACH, .5, .75), 'bone')
    r.cube('treadle', (REACH - .5, -1, -3.5, 1, 1, 1), 'bone_dark')
    r.part('footman', 'body', (HUB[0] + CRANK, HUB[1], CRANK_Z))
    r.cube('footman', (-.25, 0, -.25, .5, quarter(footman_length()), .5), 'bone')

    # Flyer mount: tension post, mother-of-all and two maidens.
    fx, fy = FLYER
    r.cube('body', (fx - .5, -8.5, -1, 1, 2.5, 1), 'bone')
    r.cube('body', (fx - 1, -7.75, -1.5, 2, .75, 2), 'bone_dark')
    r.cube('body', (fx - 1, -9.75, -4.5, 2, 1.25, 6.5), 'wood')
    for z in (-4.25, .75):
        r.cube('body', (fx - .5, -13.5, z, 1, 3.75, .75), 'bone')
        r.cube('body', (fx - .75, -14.25, z - .25, 1.5, .75, 1.25), 'bone_dark')

    # Flyer: a pair of clawed finger bones around the bobbin, orifice facing the spinner.
    r.part('flyer', 'body', (fx, fy, 0))
    r.cube('flyer', (-.25, -.25, -5, .5, .5, 5.5), 'bone')
    r.cube('flyer', (-.75, -.75, -5.75, 1.5, 1.5, 1), 'bone_dark')
    r.cube('flyer', (-WHORL, -1, -.25, WHORL * 2, 2, .75), 'bone_dark')
    r.cube('flyer', (-1, -WHORL, -.25, 2, WHORL * 2, .75), 'bone_dark')
    r.cube('flyer', (-.5, -2.25, -3.5, 1, 4.5, .5), 'bone')
    for y in (-2.25, 1.75):
        r.cube('flyer', (-.25, y, -3, .5, .5, 2.5), 'bone')
        for z in (-2.5, -1.75, -1):
            r.cube('flyer', (-.25, y - .5 if y < 0 else y + .5, z, .5, .5, .25), 'claw')
    r.part('bobbin', 'body', (fx, fy, 0))
    r.cube('bobbin', (-1, -1, -2.75, 2, 2, 2), 'thread')
    r.cube('bobbin', (-1.25, -.75, -2.5, 2.5, 1.5, 1.5), 'thread')
    r.cube('bobbin', (-.75, -1.25, -2.5, 1.5, 2.5, 1.5), 'thread')
    for z in (-3, -.75):
        r.cube('bobbin', (-1.25, -1.25, z, 2.5, 2.5, .25), 'bone_dark')

    # Drive band from the wheel groove to the whorl, and a leader thread hanging from the orifice.
    for name, (whorl, wheel) in zip(('band_upper', 'band_lower'), band_tangents()):
        rod(r, name, (whorl[0], whorl[1], -.25), (wheel[0], wheel[1], -.25), 'thread')
    rod(r, 'leader', (fx, fy, -5.75), (fx + 1.25, -8, -6.5), 'thread', .25)

    # Distaff: a vertebral rod dressed with raw red fibre, bound in thread.
    r.cube('body', (-7, -15.5, 2, .75, 9.5, .75), 'bone')
    for y in (-8, -10):
        r.cube('body', (-7.25, y, 1.75, 1.25, .5, 1.25), 'bone_dark')
    r.part('fibre', 'body', (-6.625, -10, 2.375))
    r.cube('fibre', (-1.25, -4.5, -1.25, 2.5, 3.5, 2.5), 'fibre')
    r.cube('fibre', (-1, -5.5, -1, 2, 1, 2), 'fibre')
    r.cube('fibre', (-.75, -1, -.75, 1.5, 1, 1.5), 'fibre')
    r.cube('fibre', (-1.5, -3.75, -.5, .5, 1.5, 1), 'fibre')
    r.cube('fibre', (1, -2.75, -.75, .5, 1.25, 1.25), 'fibre')
    for y in (-3.5, -2):
        r.cube('fibre', (-1.5, y, -1.5, 3, .5, 3), 'thread')
    return r


def noise(*values):
    digest = hashlib.blake2s(repr(values).encode(), digest_size=2).digest()
    return int.from_bytes(digest, 'big') / 65535


def clamp(rgb, shift=0):
    return tuple(max(0, min(255, c + shift)) for c in rgb)


def paint(material, face, lx, ly, fw, fh, cube):
    base = PALETTE[material]
    n = noise(material, face, lx, ly, cube)
    shade = {'up': 14, 'down': -22, 'east': -8, 'west': -8, 'north': 4, 'south': 0}[face]
    edge = lx in (0, fw - 1) or ly in (0, fh - 1)
    glow = None
    if material == 'wood':
        rgb = clamp(base, shade + int(noise('grain', cube, face, ly) * 16) - 8 + int(n * 6))
        if n < .07:
            rgb = (118, 26, 40)
        elif n > .97:
            rgb = (48, 24, 28)
        if edge:
            rgb = clamp(rgb, -12)
    elif material in ('bone', 'bone_dark'):
        rgb = clamp(base, shade + int(n * 14) - 7 - (16 if edge else 0))
        if n > .94:
            rgb = clamp(base, -40)
        elif noise('stain', cube, face, lx // 2, ly) < .1:
            rgb = clamp((158, 84, 72), shade)
    elif material == 'muscle':
        rgb = clamp(base, shade + (12 if lx % 2 == 0 else -6) + int(n * 8))
        if ly == 0 and face != 'down':
            rgb = clamp(rgb, 26)
        if n > .95:
            rgb = (196, 62, 70)
    elif material == 'sinew':
        rgb = clamp(base, shade + (8 if ly % 2 == 0 else -8) + int(n * 6) - (14 if edge else 0))
    elif material == 'heart':
        rgb = clamp(base, shade + int(n * 10))
        if (lx + ly * 2) % 5 == 0 and n > .35:
            rgb, glow = (66, 10, 26), (120, 12, 28, 255)
        elif n > .9:
            rgb, glow = (206, 72, 84), (170, 30, 44, 255)
    elif material == 'thread':
        stripe = (lx + ly) % 3
        rgb = clamp(base, shade // 2 + (-46 if stripe == 0 else 12 if stripe == 1 and n > .5 else 0))
        glow = (150, 16, 34, 255) if stripe else (70, 6, 16, 255)
    elif material == 'fibre':
        tone = noise('tuft', cube, face, lx // 2, ly // 2)
        rgb = (78, 30, 46) if tone < .25 else (150, 72, 90) if tone > .7 else base
        if n > .9:
            rgb = (184, 118, 120)
        rgb = clamp(rgb, shade)
    elif material == 'vein':
        rgb = clamp(base, shade + (30 if n > .7 else 0))
        glow = (90, 8, 22, 255) if n > .7 else None
    else:
        rgb = clamp(base, shade + (40 if ly == 0 else 0))
    return rgb + (255,), glow


def write_java(body):
    pin_rest = (HUB[0] + CRANK, HUB[1])
    constants = {
        'HUB_X': HUB[0], 'HUB_Y': HUB[1], 'CRANK_RADIUS': CRANK, 'HINGE_X': HINGE[0], 'HINGE_Y': HINGE[1],
        'TREADLE_REACH': REACH, 'TREADLE_TRAVEL': TRAVEL, 'FOOTMAN_LENGTH': quarter(footman_length()),
        'FLYER_RATIO': RIM / WHORL}
    assert pin_rest[0] == HINGE[0] + REACH, 'footman must hang plumb at rest'
    fields = '\n'.join(f'    public static final float {k} = {float(v):.4f}F;' for k, v in constants.items())
    JAVA.write_text('''package com.vincenthuto.hemomancy.client.model.tile.crafting;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated by tools/model_export/puppeteers_spindle.py; animation lives in PuppeteersSpindleModel. */
public final class PuppeteersSpindleMesh {
''' + fields + '''

    private PuppeteersSpindleMesh() {}

    public static LayerDefinition create() {
''' + body + '\n    }\n}\n', encoding='utf-8')


def write_blockbench():
    model = json.loads((BUILD / f'models/entity/bbmodel/pelagic/{NAME}.bbmodel').read_text(encoding='utf-8'))
    model['name'] = model['model_identifier'] = 'PuppeteersSpindleModel'
    groups = {}

    def collect(nodes):
        for node in nodes:
            if isinstance(node, dict):
                groups[node['name']] = node['uuid']
                collect(node['children'])
    collect(model['outliner'])
    spins = [('wheel', 1), ('flyer', RIM / WHORL), ('bobbin', RIM / WHORL - .4)]
    animators = {}
    for name, turns in spins:
        frames = [dict(channel='rotation', time=t, interpolation='linear',
                       data_points=[dict(x=0, y=0, z=-360 * turns * t / 4)], uuid=mesh.uid(f'{NAME}/spin/{name}/{t}'))
                  for t in (0, 1, 2, 3, 4)]
        animators[groups[name]] = dict(name=name, type='bone', keyframes=frames)
    model['animations'] = [dict(uuid=mesh.uid(NAME + '/spin'), name='spin', loop='loop', length=4, animators=animators)]
    path = ASSETS / 'models/block/bbmodel/PuppeteersSpindleModel.bbmodel'
    path.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
    return model


# --- preview -------------------------------------------------------------------------------

def rotation_matrix(rx, ry, rz):
    import numpy as np
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mz @ my @ mx


def pose(rig, age):
    """Mirror PuppeteersSpindleModel.setupAnim: returns {part: (pivot, rotation_radians, scale)}."""
    turn = age * .06
    pin = (HUB[0] + CRANK * math.cos(turn), HUB[1] + CRANK * math.sin(turn))
    angle = math.asin((pin[1] - HUB[1]) * TRAVEL / REACH)
    tip = (HINGE[0] + REACH * math.cos(angle), HINGE[1] + REACH * math.sin(angle))
    dx, dy = tip[0] - pin[0], tip[1] - pin[1]
    overrides = {
        'wheel': dict(rot=(0, 0, turn)), 'flyer': dict(rot=(0, 0, turn * RIM / WHORL)),
        'bobbin': dict(rot=(0, 0, turn * (RIM / WHORL - .4))), 'treadle': dict(rot=(0, 0, angle)),
        'footman': dict(pivot=(pin[0], pin[1], CRANK_Z), rot=(0, 0, math.atan2(-dx, dy)),
                        scale=(1, math.hypot(dx, dy) / quarter(footman_length()), 1))}
    out = {}
    for part in rig.parts:
        o = overrides.get(part['name'], {})
        out[part['name']] = (o.get('pivot', part['pivot']),
                             o.get('rot', tuple(math.radians(a) for a in part['rotation'])),
                             o.get('scale', (1, 1, 1)))
    return out


def render(rig, elements, texture, yaw, pitch, age, scale=22, size=460):
    import numpy as np
    posed = pose(rig, age)
    parts = {p['name']: p for p in rig.parts}
    canvas = np.zeros((size, size, 3)); canvas[:] = (34, 28, 33)
    depth = np.full((size, size), -np.inf)
    a, b = math.radians(yaw), math.radians(pitch)
    view = rotation_matrix(b, 0, 0) @ rotation_matrix(0, a, 0)
    tex = np.asarray(texture.convert('RGBA'), dtype=float)
    light = np.array([.4, -.8, -.5]); light /= np.linalg.norm(light)
    index = 0
    for part in rig.parts:
        chain, name = [], part['name']
        while name != 'root':
            chain.append(name); name = parts[name]['parent']
        for cube in part['cubes']:
            faces = elements[index]['faces']; index += 1
            x, y, z, w, h, d = cube['box']
            corners = np.array([[x + w * i, y + h * j, z + d * k] for i in (0, 1) for j in (0, 1) for k in (0, 1)], dtype=float)
            for name in chain:
                pivot, rot, scl = posed[name]
                corners = (rotation_matrix(*rot) @ (corners * scl).T).T + pivot
            corners[:, 1] -= 24 if 'body' in chain else 0
            world = corners * [1, -1, -1] - [0, 8, 0]   # renderer flip: y up, model -z toward the viewer; centred
            screen = (view @ world.T).T
            # face: origin corner, u-edge corner, v-edge corner (corner index = i*4 + j*2 + k)
            spec = {'north': (4, 0, 6), 'south': (1, 5, 3), 'west': (0, 1, 2), 'east': (5, 4, 7),
                    'up': (1, 5, 0), 'down': (3, 7, 2)}
            for face, (o, ue, ve) in spec.items():
                O, U, V = screen[o], screen[ue] - screen[o], screen[ve] - screen[o]
                normal = np.cross(world[ue] - world[o], world[ve] - world[o])
                if np.linalg.norm(normal) == 0:
                    continue
                normal /= np.linalg.norm(normal)
                centre = world[o] + (world[ue] - world[o] + world[ve] - world[o]) / 2
                if (centre - world.mean(0)) @ normal < 0:
                    normal = -normal
                shade = .55 + .45 * max(0, -normal @ light)
                pts = np.array([O, O + U, O + V, O + U + V])[:, :2] * scale + size / 2
                x0, y0 = np.floor(pts.min(0)).astype(int); x1, y1 = np.ceil(pts.max(0)).astype(int)
                x0, y0, x1, y1 = max(x0, 0), max(y0, 0), min(x1, size), min(y1, size)
                if x1 <= x0 or y1 <= y0:
                    continue
                gx, gy = np.meshgrid(np.arange(x0, x1) + .5, np.arange(y0, y1) + .5)
                M = np.array([[U[0], V[0]], [U[1], V[1]]]) * scale
                if abs(np.linalg.det(M)) < 1e-6:
                    continue
                inv = np.linalg.inv(M)
                rel = np.stack([gx - (O[0] * scale + size / 2), gy - (O[1] * scale + size / 2)])
                s, t = np.tensordot(inv, rel, axes=1)
                inside = (s >= 0) & (s <= 1) & (t >= 0) & (t <= 1)
                zval = O[2] + s * U[2] + t * V[2]
                u0, v0, u1, v1 = faces[face]['uv']
                tu = np.clip((u0 + s * (u1 - u0)).astype(int), 0, tex.shape[1] - 1)
                tv = np.clip((v0 + t * (v1 - v0)).astype(int), 0, tex.shape[0] - 1)
                sample = tex[tv, tu]
                region = depth[y0:y1, x0:x1]
                hit = inside & (zval > region) & (sample[..., 3] > 0)
                region[hit] = zval[hit]
                canvas[y0:y1, x0:x1][hit] = sample[..., :3][hit] * shade
    # The model's y axis points down; the screen's also does, so world-up flips once more.
    return np.flipud(canvas.astype('uint8'))


def preview(rig, model):
    from PIL import Image
    import numpy as np
    texture = Image.open(ASSETS / f'textures/entity/{NAME}.png')
    views = [(0, 8, 0), (35, 22, 20), (-40, 25, 45), (90, 10, 70), (180, 18, 0), (25, 60, 30)]
    tiles = [render(rig, model['elements'], texture, yaw, pitch, age) for yaw, pitch, age in views]
    sheet = np.vstack([np.hstack(tiles[:3]), np.hstack(tiles[3:])])
    out = ROOT / 'build/puppeteers_spindle_preview.png'
    Image.fromarray(sheet).save(out)
    small = Image.fromarray(render(rig, model['elements'], texture, 35, 22, 0, scale=3.5, size=80))
    small.resize((320, 320), Image.NEAREST).save(ROOT / 'build/puppeteers_spindle_preview_small.png')
    print('Wrote', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    mesh.COLORS.update({k: v + (255,) for k, v in PALETTE.items()})
    mesh.ASSETS, mesh.SIZE, mesh.PAINT = BUILD, (128, 128), paint
    rig = spindle()
    write_java(mesh.export(rig, True))
    for suffix in ('', '_glow'):
        shutil.copyfile(BUILD / f'textures/entity/pelagic/{NAME}{suffix}.png',
                        ASSETS / f'textures/entity/{NAME}{suffix}.png')
    model = write_blockbench()
    print(f'Exported Puppeteer\'s Spindle: {len(model["elements"])} quarter-grid cubes in {len(rig.parts)} parts.')
    if args.preview:
        preview(rig, model)


if __name__ == '__main__':
    main()
