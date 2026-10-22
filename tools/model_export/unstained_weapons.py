"""Authored Unstained weapon rigs: warhammer, Silthmere Glaive, Absolution Daggers and the Unstained Shield.

Emits the Java box-UV meshes, painted UV atlases (plus emissive still-water layers), the Annetta dagger
variant atlas and the shield's baked JSON item model. Textures are painted from each texel's position on
its cube, so bands, fullers and edges follow the geometry rather than the atlas layout.

    python tools/model_export/unstained_weapons.py            # write models + textures
    python tools/model_export/unstained_weapons.py --preview  # also write build/unstained_weapons_preview.png

Blockbench projects are regenerated afterwards with java_model_to_bbmodel.mjs (see bbmodel_commands()).
Palette: white, pale silver and patinated copper; glass and still water for the Lady's tears. No blood red.
"""
from pathlib import Path
import argparse
import json
import math

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained'
TEXTURES = ASSETS / 'textures/entity/unstained_weapons'


# ---------------------------------------------------------------- rigs

class Rig:
    def __init__(self, name, java_class, layer):
        self.name, self.java_class, self.layer = name, java_class, layer
        self.parts = [dict(name='root', parent=None, pivot=(0, 0, 0), rotation=(0, 0, 0), cubes=[])]

    def part(self, name, parent='root', pivot=(0, 0, 0), rotation=(0, 0, 0)):
        self.parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=[]))
        return name

    def cube(self, part, box, material):
        next(p for p in self.parts if p['name'] == part)['cubes'].append(dict(box=box, material=material))


def warhammer():
    r = Rig('unstained_warhammer', 'UnstainedWarhammerModel', 'unstained_warhammer')
    r.part('haft')
    r.cube('haft', (-0.5, -13, -0.5, 1, 29, 1), 'lacquer')
    r.cube('haft', (-0.75, 8, -0.75, 1.5, 8, 1.5), 'linen')
    for y in (-4, 2, 7.25):
        r.cube('haft', (-0.75, y, -0.75, 1.5, 0.75, 1.5), 'verdigris')
    # Langets clamp the bell's socket to the haft on both flats.
    r.part('socket')
    r.cube('socket', (-1, -11.75, -1, 2, 1.5, 2), 'verdigris')
    r.cube('socket', (-0.4, -10.25, -0.75, 0.8, 5.5, 0.25), 'langet')
    r.cube('socket', (-0.4, -10.25, 0.5, 0.8, 5.5, 0.25), 'langet')

    # The bell lies across the haft: crown loop is the back peen, the flared mouth is the striking face.
    r.part('bell_head', pivot=(0, -15, 0), rotation=(0, 0, -90))
    r.part('bell_crown', 'bell_head')
    r.cube('bell_crown', (-1.25, -6.25, -0.5, 0.75, 1.75, 1), 'silver_bright')
    r.cube('bell_crown', (0.5, -6.25, -0.5, 0.75, 1.75, 1), 'silver_bright')
    r.cube('bell_crown', (-1.25, -7, -0.5, 2.5, 0.75, 1), 'silver_bright')
    r.cube('bell_crown', (-1.75, -4.5, -1.75, 3.5, 1, 3.5), 'silver')
    r.part('bell_body', 'bell_head')
    r.cube('bell_body', (-2.25, -3.5, -2.25, 4.5, 1, 4.5), 'silver')
    # Each tier is a cross of two slabs (the second inset 0.25 at both ends) so the bell reads round.
    r.cube('bell_body', (-2.5, -2.5, -2, 5, 3, 4), 'bell_waist')
    r.cube('bell_body', (-2, -2.25, -2.5, 4, 2.5, 5), 'bell_waist')
    r.cube('bell_body', (-2.75, -1.75, -2.75, 5.5, 1, 5.5), 'verdigris')
    r.cube('bell_body', (-3, 0.5, -2.5, 6, 1.5, 5), 'engraved')
    r.cube('bell_body', (-2.5, 0.75, -3, 5, 1, 6), 'engraved')
    r.part('bell_flared_face', 'bell_head')
    r.cube('bell_flared_face', (-3.5, 2, -3, 7, 1.5, 6), 'bell_mouth')
    r.cube('bell_flared_face', (-3, 2.25, -3.5, 6, 1, 7), 'bell_mouth')
    r.part('pale_clapper', 'bell_head')
    r.cube('pale_clapper', (-1.25, 3.5, -1.25, 2.5, 1, 2.5), 'silver_bright')
    r.cube('pale_clapper', (-0.75, 4.5, -0.75, 1.5, 0.5, 1.5), 'silver_bright')

    # Crescent finial above the bell: the Lady's watchful moon.
    r.part('crescent_finial')
    r.cube('crescent_finial', (-0.5, -18.75, -0.5, 1, 1.25, 1), 'verdigris')
    r.cube('crescent_finial', (-1.25, -19.5, -0.375, 2.5, 0.75, 0.75), 'silver_bright')
    for side in (-1, 1):
        r.cube('crescent_finial', (-2.25 if side < 0 else 1.25, -20.75, -0.375, 1, 1.5, 0.75), 'silver_bright')
        r.cube('crescent_finial', (-2.5 if side < 0 else 1.75, -21.75, -0.375, 0.75, 1, 0.75), 'silver_bright')
        r.cube('crescent_finial', (-2 if side < 0 else 1.25, -22.5, -0.375, 0.75, 0.75, 0.75), 'silver_bright')
    r.cube('crescent_finial', (-0.75, -21.5, -0.5, 1.5, 1.5, 1), 'glass')

    # A votive ribbon knotted under the bell, turned so it catches the light from the front and side.
    r.part('votive_ribbon', pivot=(1, -10.5, 0), rotation=(0, 35, -6))
    r.cube('votive_ribbon', (0, 0, -1, 0.25, 7, 2), 'ribbon')

    r.part('pommel')
    r.cube('pommel', (-1, 16, -1, 2, 0.75, 2), 'verdigris')
    r.cube('pommel', (-1.5, 16.75, -1.5, 3, 1.25, 3), 'engraved')
    r.cube('pommel', (-0.75, 18, -0.75, 1.5, 0.75, 1.5), 'silver_bright')
    return r


# Blade rows for the glaive: (row, back x, edge x). Row r spans local y -(r+1)..-r above the socket.
# A broad convex belly sweeps forward, then the point hooks back over the shaft like a waning moon.
GLAIVE_ROWS = [(0, -0.75, 1), (1, -0.75, 1.75), (2, -0.5, 2.5), (3, -0.5, 3.25), (4, -0.5, 4), (5, -0.25, 4.5),
               (6, 0, 4.75), (7, 0, 5), (8, 0.25, 5), (9, 0.5, 5), (10, 0.75, 4.75), (11, 1, 4.5),
               (12, 1.25, 4), (13, 1.5, 3.5), (14, 1.5, 3), (15, 1.25, 2.5), (16, 1, 2), (17, 0.5, 1.5)]
GLAIVE_SPINE = 11


def merged(rows):
    runs = []
    for row, back, edge in rows:
        if runs and runs[-1][1] == back and runs[-1][2] == edge and runs[-1][0] + runs[-1][3] == row:
            runs[-1][3] += 1
        else:
            runs.append([row, back, edge, 1])
    return runs


def glaive():
    r = Rig('silthmere_glaive', 'SilthmereGlaiveModel', 'silthmere_glaive')
    r.part('shaft')
    r.cube('shaft', (-0.5, -11, -0.5, 1, 32, 1), 'lacquer')
    r.cube('shaft', (-0.75, 4, -0.75, 1.5, 4, 1.5), 'linen')
    r.cube('shaft', (-0.75, 12, -0.75, 1.5, 5, 1.5), 'linen')
    for y in (-11.75, 3.25, 8, 11.25, 17):
        r.cube('shaft', (-0.75, y, -0.75, 1.5, 0.75, 1.5), 'verdigris')
    r.part('butt_spike')
    r.cube('butt_spike', (-1, 21, -1, 2, 1, 2), 'verdigris')
    r.cube('butt_spike', (-0.5, 22, -0.5, 1, 1.5, 1), 'silver')
    r.cube('butt_spike', (-0.25, 23.5, -0.25, 0.5, 1, 0.5), 'silver_bright')

    r.part('blade_socket')
    r.cube('blade_socket', (-0.75, -16, -0.75, 1.5, 4.25, 1.5), 'engraved')
    r.cube('blade_socket', (-0.3, -12, -0.85, 0.6, 3.5, 0.25), 'langet')
    r.cube('blade_socket', (-0.3, -12, 0.6, 0.6, 3.5, 0.25), 'langet')
    r.part('collar')
    r.cube('collar', (-1.25, -17, -1.25, 2.5, 1.25, 2.5), 'verdigris')

    r.part('blade', pivot=(0, -17, 0))
    r.part('blade_spine', 'blade')
    r.part('main_cutting_blade', 'blade')
    for row, back, edge, height in merged(GLAIVE_ROWS):
        part = 'main_cutting_blade' if row < 15 else 'blade_tip'
        if part == 'blade_tip' and not any(p['name'] == 'blade_tip' for p in r.parts):
            r.part('blade_tip', 'blade')
        r.cube(part, (back, -(row + height), -0.25, edge - back, height, 0.5), 'glaive_blade')
    # A thick unsharpened spine runs up the back of the blade.
    for row, back, edge, height in merged([(rr, b, b + 0.75) for rr, b, e in GLAIVE_ROWS if rr < GLAIVE_SPINE]):
        r.cube('blade_spine', (back, -(row + height), -0.5, 0.75, height, 1), 'silver')
    r.part('back_hook', 'blade')
    for back, edge, row in ((-1, -0.5, 2), (-1.75, -0.5, 3), (-2.5, -1.25, 4), (-2.75, -2, 5)):
        r.cube('back_hook', (back, -(row + 1), -0.25, edge - back, 1, 0.5), 'silver')
    # A Tear of Silthmere set into the blade root, and a ribbon hanging from the collar.
    r.part('silthmere_tear', 'blade')
    r.cube('silthmere_tear', (0.25, -3.5, -0.75, 1.5, 1.5, 1.5), 'glass')
    r.cube('silthmere_tear', (0.5, -4.25, -0.625, 1, 0.75, 1.25), 'glass')
    r.part('collar_ribbon', pivot=(-1.25, -16.5, 0), rotation=(0, 0, 12))
    r.cube('collar_ribbon', (-0.25, 0, -0.6, 0.25, 6, 1.2), 'ribbon')
    return r


def dagger():
    r = Rig('absolution_dagger', 'AbsolutionDaggerModel', 'absolution_dagger')
    r.part('stiletto_blade')
    r.cube('stiletto_blade', (-0.75, -9.5, -0.5, 1.5, 2, 1), 'silver_bright')
    r.cube('stiletto_blade', (-0.75, -16, -0.25, 1.5, 6.5, 0.5), 'stiletto')
    r.cube('stiletto_blade', (-0.5, -20.5, -0.25, 1, 4.5, 0.5), 'stiletto')
    r.cube('stiletto_blade', (-0.25, -23.5, -0.25, 0.5, 3, 0.5), 'stiletto')
    # Raised hemolytic channel down both faces; it carries the permanent treatment.
    r.cube('stiletto_blade', (-0.25, -19.5, -0.5, 0.5, 10, 1), 'channel')

    r.part('guard')
    r.cube('guard', (-1.75, -7.5, -0.75, 3.5, 1, 1.5), 'silver')
    r.cube('guard', (-3.25, -7.25, -0.5, 1.5, 0.75, 1), 'silver')
    r.cube('guard', (1.75, -7.25, -0.5, 1.5, 0.75, 1), 'silver')
    # Quillons rise into a crescent cradling the blade.
    r.cube('guard', (-3.75, -9, -0.5, 0.75, 1.75, 1), 'quillon')
    r.cube('guard', (3, -9, -0.5, 0.75, 1.75, 1), 'quillon')
    r.cube('guard', (-3.5, -9.75, -0.375, 0.5, 0.75, 0.75), 'silver_bright')
    r.cube('guard', (3, -9.75, -0.375, 0.5, 0.75, 0.75), 'silver_bright')
    r.cube('guard', (-0.75, -7.75, -1, 1.5, 1.5, 2), 'glass')

    r.part('grip')
    r.cube('grip', (-0.75, -6.5, -0.75, 1.5, 7, 1.5), 'grip_wrap')
    r.cube('grip', (-1, -6.5, -1, 2, 0.75, 2), 'verdigris')
    r.cube('grip', (-1, -0.25, -1, 2, 0.75, 2), 'verdigris')

    r.part('pommel')
    r.cube('pommel', (-1.5, 0.5, -0.5, 3, 3, 1), 'pommel_disc')
    r.cube('pommel', (-0.5, 1.5, -0.75, 1, 1, 1.5), 'pommel_boss')
    r.cube('pommel', (-0.5, 3.5, -0.5, 1, 0.75, 1), 'silver_bright')

    # Only Annetta's dagger shows this: a braid of her hair strung with a pulled tooth.
    r.part('annetta_braid', pivot=(0.25, 4, 0), rotation=(0, 0, -14))
    r.cube('annetta_braid', (-0.5, 0, -0.5, 1, 4, 1), 'braid')
    r.cube('annetta_braid', (-0.625, 4, -0.625, 1.25, 0.75, 1.25), 'verdigris')
    r.cube('annetta_braid', (-0.5, 4.75, -0.5, 1, 1.5, 1), 'braid')
    r.cube('annetta_braid', (-0.25, 6.25, -0.25, 0.5, 1.25, 0.5), 'tooth')
    return r


# ---------------------------------------------------------------- painting

PALETTE = dict(
    silver=(204, 212, 222), silver_bright=(232, 237, 243), silver_dark=(118, 128, 146),
    verdigris=(86, 156, 136), linen=(228, 223, 206), lacquer=(234, 231, 221),
    glass=(132, 192, 210), water=(112, 200, 206), ribbon=(240, 238, 231),
    bone=(226, 214, 186), hair=(104, 52, 40), nail=(228, 194, 182),
)


def h(*values):
    """Deterministic per-texel noise in [0, 1)."""
    n = 0
    for value in values:
        n = (n * 73856093) ^ int(round(value * 4 + 1000)) * 19349663
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return (n ^ (n >> 16)) % 1000 / 1000


def add(rgb, amount):
    return tuple(max(0, min(255, round(c + amount))) for c in rgb)


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def silver(t, base='silver'):
    rgb = PALETTE[base]
    rgb = add(rgb, (t['n'] - 0.5) * 10)
    if t['side'] and t['u'] == 1 and t['fw'] >= 3:
        rgb = add(rgb, 16)            # sheen line down each flat
    if t['side'] and t['u'] == t['fw'] - 1:
        rgb = add(rgb, -14)
    return rgb


def verdigris(t):
    n = t['n']
    if n < 0.22:
        rgb = (62, 122, 108)
    elif n > 0.86:
        rgb = (148, 204, 178)
    elif 0.5 < n < 0.53:
        rgb = (150, 100, 72)          # a little bare copper the patina never took
    else:
        rgb = PALETTE['verdigris']
    return rgb


def linen(t):
    rgb = PALETTE['linen']
    k = (t['u'] + t['gy']) % 3
    if k == 0:
        rgb = (192, 186, 168)
    elif k == 1:
        rgb = add(rgb, 8)
    if t['gy'] % 6 == 0 and k == 2:
        rgb = (186, 198, 214)         # silver thread binding the wrap
    return rgb


def lacquer(t):
    rgb = add(PALETTE['lacquer'], (t['n'] - 0.5) * 8)
    if (t['gy'] + t['u']) % 5 == 0:
        rgb = (172, 184, 202)         # spiral of silver wire
    elif (t['gy'] + t['u']) % 5 == 1:
        rgb = (238, 243, 248)
    return rgb


def paint(material, t):
    """Return (rgba, glow_rgba | None) for one texel. t holds face geometry; see texel_info()."""
    face, u, v, fw, fh = t['face'], t['u'], t['v'], t['fw'], t['fh']
    edge_row = t['side'] and (v == 0 or v == fh - 1) and fh > 2
    glow = None
    if material in ('silver', 'silver_bright', 'langet'):
        rgb = silver(t, 'silver_bright' if material != 'silver' else 'silver')
        if material == 'langet' and v % 2 == 1:
            rgb = (96, 160, 140) if v % 4 == 1 else rgb   # copper rivets down the strap
    elif material == 'engraved':
        rgb = silver(t)
        if t['side'] and (v + round(1.2 * math.sin(u * 1.3))) % 3 == 1:
            rgb = (150, 162, 184)     # still-water ripples cut into the metal
    elif material == 'bell_waist':
        rgb = silver(t)
        if t['side'] and fw >= 5 and abs(u - fw / 2 + 0.5) <= 0.5 and v in (0, 2):
            rgb = (140, 152, 174)
    elif material == 'bell_mouth':
        rgb = silver(t, 'silver_bright')
        if face == 'up':              # the open mouth: a dark throat behind a polished lip
            d = min(u, v, fw - 1 - u, fh - 1 - v)
            rgb = (82, 92, 112) if d >= 1 else rgb
            if d == 1:
                rgb = (128, 138, 158)
        elif t['side'] and v == 1 and u % 2 == 0:
            rgb = (92, 158, 138)      # patinated rivets around the lip
    elif material == 'verdigris':
        rgb = verdigris(t)
    elif material == 'quillon':
        rgb = verdigris(t)
        if t['side'] and v == 0:
            rgb = (148, 204, 178)
    elif material == 'linen' or material == 'grip_wrap':
        rgb = linen(t)
    elif material == 'lacquer':
        rgb = lacquer(t)
    elif material == 'ribbon':
        rgb = PALETTE['ribbon']
        if t['side'] and (u == 0 or u == fw - 1):
            rgb = (178, 190, 208)
        if t['side'] and v == fh - 1 and 0 < u < fw - 1:
            return (0, 0, 0, 0), None  # swallowtail notch
        if t['side'] and v % 4 == 2 and 0 < u < fw - 1:
            rgb = (214, 222, 232)
    elif material == 'glass':
        rgb = PALETTE['glass']
        if u == 0 and v == 0:
            rgb = (240, 250, 252)
        elif (u + v) % 3 == 0:
            rgb = (104, 166, 190)
        glow = (54, 120, 138, 255)
    elif material == 'glaive_blade':
        rgb = silver(t)
        cx, cy = t['gx'], t['gy']
        row = -cy - 18
        back = next((b for rr, b, e in GLAIVE_ROWS if rr == math.floor(row)), None)
        edge = next((e for rr, b, e in GLAIVE_ROWS if rr == math.floor(row)), None)
        if back is not None and face in ('north', 'south'):
            if cx > edge - 0.75:
                rgb = (244, 248, 252) if (t['u'] + t['v']) % 2 == 0 else (226, 236, 246)   # honed edge
            elif 2 <= row < 13 and abs(cx - (back + 1.5)) < 0.5:
                rgb = (138, 150, 174)  # engraved fuller following the curve
            elif 2 <= row < 13 and abs(cx - (back + 1.5)) < 1.0:
                rgb = add(rgb, -8)
        elif face in ('up', 'down', 'east'):
            rgb = (236, 242, 248)
    elif material == 'edge':
        rgb = (244, 248, 252) if (v + u) % 2 == 0 else (222, 234, 244)
    elif material == 'stiletto':
        rgb = silver(t, 'silver_bright')
        if t['side'] and face in ('east', 'west'):
            rgb = (246, 250, 253)
    elif material == 'channel':
        rgb = silver(t)
        if t['side'] and face in ('north', 'south'):
            rgb = (106, 194, 200) if v % 3 else (150, 218, 220)
            glow = (30, 104, 110, 255) if v % 3 else (54, 140, 144, 255)
    elif material == 'pommel_disc':
        rgb = silver(t)
        if face in ('north', 'south'):
            d = min(u, v, fw - 1 - u, fh - 1 - v)
            rgb = (226, 232, 240) if d == 0 else (176, 186, 204)
    elif material == 'pommel_boss':
        rgb, glow = PALETTE['glass'], (54, 120, 138, 255)
    elif material == 'braid':
        rgb = PALETTE['hair']
        k = (v + abs(u - (fw - 1) / 2)) % 3
        rgb = (74, 36, 30) if k < 1 else (134, 72, 54) if k >= 2 else rgb
    elif material == 'tooth':
        rgb = PALETTE['bone'] if v < fh - 1 else (196, 180, 150)
    else:
        raise KeyError(material)
    if edge_row and material not in ('ribbon', 'edge', 'glass', 'channel', 'braid'):
        rgb = add(rgb, -12)
    return rgb + (255,), glow


# Annetta's dagger: the same rig worn by her hair, teeth and nails rather than linen and copper.
ANNETTA = dict(grip_wrap='braid_grip', quillon='nail', pommel_boss='tooth_boss', pommel_disc='bone_disc')


def paint_annetta(material, t):
    material = ANNETTA.get(material, material)
    if material == 'braid_grip':
        k = (t['u'] + t['v'] * 2) % 4
        rgb = (78, 38, 30) if k == 0 else (128, 68, 50) if k == 2 else PALETTE['hair']
        return rgb + (255,), None
    if material == 'nail':
        rgb = PALETTE['nail'] if t['u'] % 2 == 0 else (206, 168, 158)
        return (add(rgb, 18) if t['v'] == 0 else rgb) + (255,), None
    if material == 'tooth_boss':
        return (PALETTE['bone'] if (t['u'] + t['v']) % 3 else (206, 192, 160)) + (255,), None
    if material == 'bone_disc':
        rgb = add(PALETTE['bone'], (t['n'] - 0.5) * 14)
        if t['face'] in ('north', 'south') and min(t['u'], t['v'], t['fw'] - 1 - t['u'], t['fh'] - 1 - t['v']) == 0:
            rgb = (198, 184, 154)
        return rgb + (255,), None
    return paint(material, t)


# ---------------------------------------------------------------- box UV

def ceil(value):
    return math.ceil(value - 1e-6)


def face_rects(u, v, w, hgt, d):
    """Minecraft ModelPart.Cube box-UV rectangles (float texels)."""
    return dict(down=(u + d, v, u + d + w, v + d), up=(u + d + w, v, u + d + w + w, v + d),
                west=(u, v + d, u + d, v + d + hgt), north=(u + d, v + d, u + d + w, v + d + hgt),
                east=(u + d + w, v + d, u + d + w + d, v + d + hgt),
                south=(u + d + w + d, v + d, u + d + w + d + w, v + d + hgt))


def local_point(face, box, fu, fv):
    """Cube-local point for a fractional position (fu, fv in 0..1) across a face, following Cube's vertex order."""
    x, y, z, w, hgt, d = box
    X, Y, Z = x + w, y + hgt, z + d
    lerp = lambda a, b, t: a + (b - a) * t
    if face == 'north':
        return lerp(x, X, fu), lerp(y, Y, fv), z
    if face == 'south':
        return lerp(X, x, fu), lerp(y, Y, fv), Z
    if face == 'west':
        return x, lerp(y, Y, fv), lerp(Z, z, fu)
    if face == 'east':
        return X, lerp(y, Y, fv), lerp(z, Z, fu)
    if face == 'down':
        return lerp(x, X, fu), y, lerp(Z, z, fv)
    return lerp(x, X, fu), Y, lerp(Z, z, fv)


def layout(rig):
    """Assign texOffs to every cube, growing the atlas height as needed."""
    for height in (32, 64, 128, 256):
        width = 64
        u = v = 0
        row = 0
        ok = True
        for part in rig.parts:
            for cube in part['cubes']:
                _, _, _, w, hgt, d = cube['box']
                cw, ch = ceil(2 * (w + d)) + 1, ceil(hgt + d) + 1
                if u + cw > width:
                    u, v, row = 0, v + row, 0
                if v + ch > height:
                    ok = False
                cube['uv'] = (u, v)
                u += cw
                row = max(row, ch)
        if ok:
            return width, height
    raise ValueError(rig.name + ' does not fit a 64x256 atlas')


def part_world(rig):
    """Pivot offsets accumulated through the parent chain, ignoring rotation (used only for painting coordinates)."""
    world = {}
    for part in rig.parts:
        parent = world.get(part['parent'], (0, 0, 0))
        world[part['name']] = tuple(a + b for a, b in zip(parent, part['pivot']))
    return world


def texel_info(face, box, rect, px, py, origin):
    x0, y0, x1, y1 = rect
    lo_u, hi_u = sorted((x0, x1))
    lo_v, hi_v = sorted((y0, y1))
    fw, fh = ceil(hi_u) - math.floor(lo_u), ceil(hi_v) - math.floor(lo_v)
    u, v = px - math.floor(lo_u), py - math.floor(lo_v)
    fu = min(1, max(0, (px + 0.5 - lo_u) / max(hi_u - lo_u, 1e-6)))
    fv = min(1, max(0, (py + 0.5 - lo_v) / max(hi_v - lo_v, 1e-6)))
    lx, ly, lz = local_point(face, box, fu, fv)
    gx, gy, gz = lx + origin[0], ly + origin[1], lz + origin[2]
    return dict(face=face, u=u, v=v, fw=fw, fh=fh, side=face not in ('up', 'down'),
                gx=gx, gy=math.floor(gy), gz=gz, n=h(gx, gy, gz))


def paint_atlas(rig, size, painter):
    image, glow = Image.new('RGBA', size), Image.new('RGBA', size)
    world = part_world(rig)
    for part in rig.parts:
        origin = world[part['name']]
        for cube in part['cubes']:
            u, v = cube['uv']
            _, _, _, w, hgt, d = cube['box']
            # Caps first so the long side faces own any texel they share.
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                rect = face_rects(u, v, w, hgt, d)[face]
                lo_u, hi_u = sorted((rect[0], rect[2]))
                lo_v, hi_v = sorted((rect[1], rect[3]))
                for py in range(math.floor(lo_v), ceil(hi_v)):
                    for px in range(math.floor(lo_u), ceil(hi_u)):
                        t = texel_info(face, cube['box'], rect, px, py, origin)
                        rgba, lit = painter(cube['material'], t)
                        image.putpixel((px, py), rgba)
                        if lit:
                            glow.putpixel((px, py), lit)
    return image, glow


# ---------------------------------------------------------------- Java

def f(value):
    text = f'{float(value):.4f}'.rstrip('0')
    return (text + '0' if text.endswith('.') else text) + 'F'


def java_source(rig, size, extra_fields='', extra_ctor='', extra_methods=''):
    lines = []
    for part in rig.parts:
        name = part['name']
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            box = ', '.join(f(c) for c in cube['box'])
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({box}, new CubeDeformation(0.0F))'
        if part['parent'] is None:
            parent, var = 'partdefinition', 'root'
        else:
            parent, var = part['parent'], name
        rot = part['rotation']
        if any(rot):
            radians = ', '.join(f(math.radians(a)) for a in rot)
            pose = f'PartPose.offsetAndRotation({", ".join(f(p) for p in part["pivot"])}, {radians})'
        else:
            pose = f'PartPose.offset({", ".join(f(p) for p in part["pivot"])})'
        lines.append(f'\t\tPartDefinition {var} = {parent}.addOrReplaceChild("{name}", {chain}, {pose});')
    body = '\n\n'.join(lines)
    return f'''package com.vincenthuto.hemomancy.client.model.item.unstained;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/** Generated by tools/model_export/unstained_weapons.py; edit the rig there and re-run it. */
public class {rig.java_class} extends Model {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("{rig.layer}"), "root");

	private final ModelPart root;{extra_fields}

	public {rig.java_class}(ModelPart root) {{
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("root");{extra_ctor}
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

{body}

		return LayerDefinition.create(meshdefinition, {size[0]}, {size[1]});
	}}
{extra_methods}
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {{
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}}
}}
'''


DAGGER_EXTRAS = dict(
    extra_fields='\n\tprivate final ModelPart annettaBraid;',
    extra_ctor='\n\t\tthis.annettaBraid = this.root.getChild("annetta_braid");',
    extra_methods='''
	/** Annetta's dagger carries her braid and tooth; the issued dagger does not. */
	public void setAnnettaVariant(boolean annetta) {
		this.annettaBraid.visible = annetta;
	}
''')


# ---------------------------------------------------------------- shield (baked JSON item model)

SHIELD_TEX = 32           # 16 UV units -> 2 px per unit; the front composite uses UV 0..8, one texel per model unit.
COMPOSITE = SHIELD_TEX // 2   # front and back composites, px across the 16-unit face (block/item density)
SHELL_ROWS = [(1, 2, 6, 10), (2, 3, 4, 12), (3, 4, 3, 13), (4, 6, 2, 14), (6, 10, 1, 15), (10, 12, 2, 14),
              (12, 13, 3, 13), (13, 14, 4, 12), (14, 15, 6, 10)]
FIELD_ROWS = [(2.5, 3.5, 6, 10), (3.5, 4.5, 4, 12), (4.5, 6, 3, 13), (6, 10, 2.5, 13.5), (10, 11.5, 3, 13),
              (11.5, 12.5, 4, 12), (12.5, 13.5, 6, 10)]
RING_ROWS = [(5.5, 6.5, 6.5, 9.5), (6.5, 9.5, 5.5, 10.5), (9.5, 10.5, 6.5, 9.5)]
BOSS_ROWS = [(6.5, 7, 7.5, 8.5), (7, 9, 6.5, 9.5), (9, 9.5, 7.5, 8.5)]
# Swatches (UV units) for edges and the back: x0, y0, x1, y1.
SWATCH = dict(silver=(8, 8, 12, 10), bright=(12, 8, 16, 10), copper=(8, 10, 12, 12), glass=(12, 10, 16, 12),
              linen=(8, 12, 12, 14), back=(8, 0, 16, 8))


def shield_front(px, py):
    """Front composite, 64x64 px across the 16-unit face, as seen from the front."""
    cx = cy = 32
    dx, dy = px + 0.5 - cx, py + 0.5 - cy
    r = math.hypot(dx, dy)
    a = math.atan2(dy, dx)
    n = h(px, py, 7)
    if r > 25:                                  # polished rim with engraved ticks and copper rivets
        rgb = add((222, 228, 236), (n - 0.5) * 10)
        if r > 27.5:
            rgb = add(rgb, -16)
        tick = (math.degrees(a) % 22.5)
        if 25.5 < r < 27.5 and tick < 2.2:
            rgb = (150, 162, 184)
        for k in range(8):
            ra = math.radians(k * 45 + 11.25)
            if math.hypot(dx - 26.4 * math.cos(ra), dy - 26.4 * math.sin(ra)) < 1.4:
                rgb = (92, 162, 140) if dx < 0 else (130, 196, 170)
        return rgb
    if r > 23:                                  # groove between rim and field
        return (132, 142, 162)
    if r > 21 and r <= 23:
        return (190, 200, 214)
    if r < 11.5:                                # boss: copper ring around the still-water moon
        if r > 7.5:
            rgb = (86, 156, 136) if n > 0.2 else (62, 122, 108)
            if n > 0.86 or (r > 10.5 and dy < 0):
                rgb = (148, 204, 178)
            return rgb
        moon = math.hypot(dx - 1.6, dy + 1.4) > 4.6 and r < 6.2       # crescent
        if moon:
            return (236, 244, 250)
        return (126, 186, 206) if (px + py) % 3 else (108, 168, 194)
    # Still-water field: pale blue-silver, concentric ripples and faint moonlight rays.
    rgb = (196, 212, 224)
    ripple = math.sin(r * 0.62)
    if ripple > 0.82:
        rgb = (224, 234, 242)
    elif ripple < -0.86:
        rgb = (164, 182, 200)
    if (math.degrees(a) + 11.25) % 45 < 2.0 and r > 13:
        rgb = add(rgb, 12)
    if -0.35 < (dx + dy) / 64 < -0.28 and r > 12:
        rgb = add(rgb, 14)                      # reflection across the water
    return add(rgb, (n - 0.5) * 6)


def shield_back(px, py):
    """Back composite: plain silver with white linen padding across the grip."""
    cx = cy = 32
    r = math.hypot(px + 0.5 - cx, py + 0.5 - cy)
    rgb = add((176, 186, 200), (h(px, py, 3) - 0.5) * 10)
    if r > 26:
        rgb = add(rgb, 20)
    elif 19 < r < 20.5:
        rgb = (150, 162, 184)
    if 16 <= py < 48 and 26 <= px < 38:
        rgb = (226, 221, 204) if (px + py) % 3 else (194, 188, 170)
    return rgb


def shield_texture():
    image = Image.new('RGBA', (SHIELD_TEX, SHIELD_TEX))
    unit = SHIELD_TEX // 16
    # The composites are designed on a 64-px grid; each texel samples that design at its centre so the rim,
    # rivets, ring and moon stay crisp pixel art at one texel per model unit.
    step = 64 // COMPOSITE
    for py in range(COMPOSITE):
        for px in range(COMPOSITE):
            sx, sy = px * step + step // 2, py * step + step // 2
            image.putpixel((px, py), shield_front(sx, sy) + (255,))
            image.putpixel((COMPOSITE + px, py), shield_back(sx, sy) + (255,))
    for name, (x0, y0, x1, y1) in SWATCH.items():
        if name == 'back':
            continue
        for py in range(y0 * unit, y1 * unit):
            for px in range(x0 * unit, x1 * unit):
                n = h(px, py, 11)
                if name == 'silver':
                    rgb = add((200, 208, 220), (n - 0.5) * 12)
                elif name == 'bright':
                    rgb = add((232, 237, 243), (n - 0.5) * 8)
                elif name == 'copper':
                    rgb = (62, 122, 108) if n < 0.22 else (148, 204, 178) if n > 0.86 else (86, 156, 136)
                elif name == 'glass':
                    rgb = (132, 192, 210) if (px + py) % 3 else (108, 168, 194)
                else:
                    rgb = (226, 221, 204) if (px + py) % 3 else (194, 188, 170)
                image.putpixel((px, py), rgb + (255,))
    return image


def shield_elements():
    def front_uv(x0, x1, y0, y1):
        # Model y grows upward; the composite is drawn top-down and mirrored for the north face.
        return [round((16 - x1) / 2, 4), round((16 - y1) / 2, 4), round((16 - x0) / 2, 4), round((16 - y0) / 2, 4)]

    def back_uv(x0, x1, y0, y1):
        return [round(8 + x0 / 2, 4), round((16 - y1) / 2, 4), round(8 + x1 / 2, 4), round((16 - y0) / 2, 4)]

    def swatch(name, horizontal=True):
        x0, y0, x1, y1 = SWATCH[name]
        return [x0, y0, x1, y0 + 1] if horizontal else [x0, y0, x0 + 1, y1]

    elements = []

    def slab(name, rows, z0, z1, edge, back=False):
        for y0, y1, x0, x1 in rows:
            faces = dict(north=dict(uv=front_uv(x0, x1, y0, y1), texture='#shield'),
                         east=dict(uv=swatch(edge, False), texture='#shield'),
                         west=dict(uv=swatch(edge, False), texture='#shield'),
                         up=dict(uv=swatch(edge), texture='#shield'),
                         down=dict(uv=swatch(edge), texture='#shield'))
            faces['south'] = dict(uv=back_uv(x0, x1, y0, y1) if back else swatch(edge), texture='#shield')
            elements.append(dict(name=name, **{'from': [x0, y0, z0]}, to=[x1, y1, z1], faces=faces))

    slab('silver shell', SHELL_ROWS, 7.5, 8.75, 'bright', back=True)
    slab('still-water field', FIELD_ROWS, 7.25, 7.5, 'silver')
    slab('verdigris boss ring', RING_ROWS, 7.0, 7.25, 'copper')
    slab('lunar boss', BOSS_ROWS, 6.5, 7.0, 'glass')
    linen = dict(uv=swatch('linen'), texture='#shield')
    for name, a, b in (('rear grip upper mount', [6, 10, 8.75], [10, 11, 9.5]),
                       ('rear grip lower mount', [6, 5, 8.75], [10, 6, 9.5]),
                       ('rear hand grip', [7, 5.5, 9.5], [9, 10.5, 10.5])):
        elements.append(dict(name=name, **{'from': a}, to=b, faces={k: dict(linen) for k in
                                                                      ('north', 'south', 'east', 'west', 'up', 'down')}))
    return elements


def write_shield():
    path = ASSETS / 'models/item/unstained_shield.json'
    model = json.loads(path.read_text(encoding='utf-8'))
    model['textures'] = {'shield': 'hemomancy:item/unstained_shield', 'particle': 'hemomancy:item/unstained_shield'}
    model['elements'] = shield_elements()
    order = ['parent', 'gui_light', 'textures', 'elements', 'display']
    model = {k: model[k] for k in order if k in model}
    path.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
    shield_texture().save(ASSETS / 'textures/item/unstained_shield.png')


# ---------------------------------------------------------------- preview

def rot_matrix(rotation):
    import numpy as np
    x, y, z = (math.radians(a) for a in rotation)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    return rz @ ry @ rx


def rig_quads(rig, hidden=()):
    """World-space textured quads: (corners[4], uvs[4], normal)."""
    import numpy as np
    transforms = {}
    for part in rig.parts:
        m = np.eye(4)
        m[:3, :3] = rot_matrix(part['rotation'])
        m[:3, 3] = part['pivot']
        parent = transforms.get(part['parent'], np.eye(4))
        transforms[part['name']] = parent @ m
    quads = []
    for part in rig.parts:
        if part['name'] in hidden:
            continue
        m = transforms[part['name']]
        for cube in part['cubes']:
            u, v = cube['uv']
            x, y, z, w, hgt, d = cube['box']
            for face, rect in face_rects(u, v, w, hgt, d).items():
                corners, uvs = [], []
                for fu, fv in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    corners.append(local_point(face, cube['box'], fu, fv))
                    uvs.append((rect[0] + (rect[2] - rect[0]) * fu, rect[1] + (rect[3] - rect[1]) * fv))
                pts = np.array([list(c) + [1] for c in corners]) @ m.T
                quads.append((pts[:, :3], np.array(uvs, float)))
    return quads


def render_quads(quads, tex, yaw, pitch, size=360, flip=True, focus=None):
    import numpy as np
    tex = np.asarray(tex.convert('RGBA'), np.float32) / 255
    color = np.zeros((size, size, 3), np.float32) + np.array([30, 32, 40]) / 255
    depth = np.full((size, size), np.inf, np.float32)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)
    right = np.array([cy, 0, sy])
    view = np.array([-sy * cp, sp, cy * cp])
    up = np.cross(view, right)
    allpts = np.concatenate([q[0] for q in quads])
    if flip:
        allpts = allpts * np.array([1, -1, 1])
    if focus is not None:
        # focus is a model-space y range (pre-flip) to frame, e.g. a weapon head.
        keep = (-allpts[:, 1] >= focus[0]) & (-allpts[:, 1] <= focus[1]) if flip else             (allpts[:, 1] >= focus[0]) & (allpts[:, 1] <= focus[1])
        allpts = allpts[keep]
    center = (allpts.min(axis=0) + allpts.max(axis=0)) / 2
    extent = max(np.ptp(allpts @ right), np.ptp(allpts @ up))
    scale = size * 0.9 / extent
    light = np.array([0.3, 0.9, -0.5]); light /= np.linalg.norm(light)
    th, tw = tex.shape[:2]
    for pts, uvs in quads:
        p = (pts * np.array([1, -1, 1]) if flip else pts) - center
        n = np.cross(p[1] - p[0], p[2] - p[0])
        if np.linalg.norm(n) < 1e-9:
            n = np.cross(p[2] - p[0], p[3] - p[0])
        if np.linalg.norm(n) < 1e-9:
            continue
        n /= np.linalg.norm(n)
        shade = 0.6 + 0.4 * abs(n @ light)
        sx = p @ right * scale + size / 2
        sy_ = -(p @ up) * scale + size / 2
        dz = p @ view
        for tri in ((0, 1, 2), (0, 2, 3)):
            xs, ys, zs, uv = sx[list(tri)], sy_[list(tri)], dz[list(tri)], uvs[list(tri)]
            x0, x1 = max(int(xs.min()), 0), min(int(math.ceil(xs.max())), size - 1)
            y0, y1 = max(int(ys.min()), 0), min(int(math.ceil(ys.max())), size - 1)
            det = (ys[1] - ys[2]) * (xs[0] - xs[2]) + (xs[2] - xs[1]) * (ys[0] - ys[2])
            if x0 > x1 or y0 > y1 or abs(det) < 1e-9:
                continue
            gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + .5, np.arange(y0, y1 + 1) + .5)
            l0 = ((ys[1] - ys[2]) * (gx - xs[2]) + (xs[2] - xs[1]) * (gy - ys[2])) / det
            l1 = ((ys[2] - ys[0]) * (gx - xs[2]) + (xs[0] - xs[2]) * (gy - ys[2])) / det
            l2 = 1 - l0 - l1
            inside = (l0 >= -1e-4) & (l1 >= -1e-4) & (l2 >= -1e-4)
            z = l0 * zs[0] + l1 * zs[1] + l2 * zs[2]
            tu = np.clip((l0 * uv[0, 0] + l1 * uv[1, 0] + l2 * uv[2, 0]).astype(int), 0, tw - 1)
            tv = np.clip((l0 * uv[0, 1] + l1 * uv[1, 1] + l2 * uv[2, 1]).astype(int), 0, th - 1)
            texel = tex[tv, tu]
            region = (slice(y0, y1 + 1), slice(x0, x1 + 1))
            ok = inside & (texel[..., 3] > .5) & (z < depth[region])
            depth[region][ok] = z[ok]
            color[region][ok] = texel[..., :3][ok] * shade
    return Image.fromarray((np.clip(color, 0, 1) * 255).astype('uint8'))


def shield_quads():
    import numpy as np
    quads = []
    for e in shield_elements():
        a, b = e['from'], e['to']
        x0, y0, z0 = a
        x1, y1, z1 = b
        corners = dict(north=[(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)],
                       south=[(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)],
                       west=[(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)],
                       east=[(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)],
                       up=[(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)],
                       down=[(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)])
        for face, data in e['faces'].items():
            u0, v0, u1, v1 = data['uv']
            k = SHIELD_TEX / 16
            uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float) * k
            quads.append((np.array(corners[face], float), uvs))
    return quads


HEAD_FOCUS = dict(unstained_warhammer=(-23, -9), silthmere_glaive=(-37, -10), absolution_dagger=(-24, 8))


def preview(rigs, textures):
    tiles = []
    for rig in rigs:
        image = textures[rig.name]
        hidden = () if rig.name != 'absolution_dagger' else ('annetta_braid',)
        quads = rig_quads(rig, hidden)
        head = HEAD_FOCUS[rig.name]
        tiles.append(render_quads(quads, image, 0.0, 0.0))
        tiles.append(render_quads(quads, image, 0.0, 0.0, focus=head))
        tiles.append(render_quads(quads, image, 0.7, 0.35, focus=head))
        tiles.append(render_quads(quads, image, 2.4, -0.3, focus=head))
        if rig.name == 'absolution_dagger':
            annetta = rig_quads(rig)
            tiles.append(render_quads(annetta, textures['annettas_absolution_dagger'], 0.0, 0.0))
            tiles.append(render_quads(annetta, textures['annettas_absolution_dagger'], 0.7, 0.35, focus=(-12, 12)))
    shield = Image.open(ASSETS / 'textures/item/unstained_shield.png')
    tiles.append(render_quads(shield_quads(), shield, math.pi, 0.0, flip=False))
    tiles.append(render_quads(shield_quads(), shield, math.pi + 0.6, 0.3, flip=False))
    tiles.append(render_quads(shield_quads(), shield, 0.5, 0.2, flip=False))
    cols = 5
    sheet = Image.new('RGB', (360 * cols, 360 * math.ceil(len(tiles) / cols)), (30, 32, 40))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * 360, (i // cols) * 360))
    out = ROOT / 'build/unstained_weapons_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


# ---------------------------------------------------------------- main

def bbmodel_commands():
    return [f'node tools/model_export/java_model_to_bbmodel.mjs '
            f'src/main/java/com/vincenthuto/hemomancy/client/model/item/unstained/{cls}.java '
            f'--texture textures/entity/unstained_weapons/{tex}.png '
            f'--out src/main/resources/assets/hemomancy/models/item/bbmodel'
            for cls, tex in (('UnstainedWarhammerModel', 'unstained_warhammer'),
                             ('SilthmereGlaiveModel', 'silthmere_glaive'),
                             ('AbsolutionDaggerModel', 'absolution_dagger'))]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    TEXTURES.mkdir(parents=True, exist_ok=True)
    rigs = [warhammer(), glaive(), dagger()]
    textures = {}
    for rig in rigs:
        size = layout(rig)
        image, glow = paint_atlas(rig, size, paint)
        image.save(TEXTURES / f'{rig.name}.png')
        glow.save(TEXTURES / f'{rig.name}_glow.png')
        textures[rig.name] = image
        extras = DAGGER_EXTRAS if rig.name == 'absolution_dagger' else {}
        (JAVA / f'{rig.java_class}.java').write_text(java_source(rig, size, **extras), encoding='utf-8')
        if rig.name == 'absolution_dagger':
            image, glow = paint_atlas(rig, size, paint_annetta)
            image.save(TEXTURES / 'annettas_absolution_dagger.png')
            glow.save(TEXTURES / 'annettas_absolution_dagger_glow.png')
            textures['annettas_absolution_dagger'] = image
        print(f'{rig.name}: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    write_shield()
    if args.preview:
        preview(rigs, textures)
    print('Regenerate Blockbench projects with:')
    for command in bbmodel_commands():
        print('  ' + command)


if __name__ == '__main__':
    main()
