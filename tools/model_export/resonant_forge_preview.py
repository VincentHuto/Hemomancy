"""Z-buffered textured previews of the Resonant Forge models written by resonant_forge.py.

Renders each stage from the worker's side and from behind, with the animated parts posed, so
rotated cubes and pivots can be checked without launching the client. Vanilla textures are read
from the NeoForge client-extra resources jar in build/moddev.
"""
from pathlib import Path
import io
import json
import math
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFont

import resonant_forge as forge

ROOT = forge.ROOT
OUT = ROOT / 'build/resonant_forge_preview.png'
SCALE = 7.0
_textures = {}


def vanilla_jar():
    jars = sorted((ROOT / 'build/moddev/artifacts').glob('neoforge-*-client-extra-aka-minecraft-resources.jar'))
    return zipfile.ZipFile(jars[-1]) if jars else None


_jar = vanilla_jar()


def texture(location):
    if location not in _textures:
        ns, path = location.split(':')
        image = None
        file = ROOT / 'src/main/resources/assets' / ns / 'textures' / (path + '.png')
        if file.exists():
            image = Image.open(file)
        elif _jar is not None:
            try:
                image = Image.open(io.BytesIO(_jar.read('assets/%s/textures/%s.png' % (ns, path))))
            except KeyError:
                image = None
        if image is None:
            image = Image.new('RGBA', (16, 16), (255, 0, 255, 255))
        image = image.convert('RGBA')
        image = image.crop((0, 0, image.width, min(image.width, image.height)))
        _textures[location] = np.asarray(image).astype(np.float32) / 255
    return _textures[location]


def rotation(axis, degrees):
    a = math.radians(degrees)
    c, s = math.cos(a), math.sin(a)
    if axis == 'x':
        return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])
    if axis == 'y':
        return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


# Face corners in Minecraft order: (u0,v0) (u1,v0) (u1,v1) (u0,v1); normal.
def face_quads(a, b):
    x0, y0, z0 = a
    x1, y1, z1 = b
    return {
        'north': ([(x1, y1, z0), (x0, y1, z0), (x0, y0, z0), (x1, y0, z0)], (0, 0, -1)),
        'south': ([(x0, y1, z1), (x1, y1, z1), (x1, y0, z1), (x0, y0, z1)], (0, 0, 1)),
        'west': ([(x0, y1, z0), (x0, y1, z1), (x0, y0, z1), (x0, y0, z0)], (-1, 0, 0)),
        'east': ([(x1, y1, z1), (x1, y1, z0), (x1, y0, z0), (x1, y0, z1)], (1, 0, 0)),
        'up': ([(x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1)], (0, 1, 0)),
        'down': ([(x0, y0, z1), (x1, y0, z1), (x1, y0, z0), (x0, y0, z0)], (0, -1, 0)),
    }


class Canvas:
    def __init__(self, w, h):
        self.color = np.zeros((h, w, 3), np.float32)
        self.color[:] = np.array([32, 32, 39]) / 255
        self.depth = np.full((h, w), np.inf, np.float32)
        self.w, self.h = w, h


def draw_model(canvas, model, transform, camera, origin):
    """transform(points)->points applies part pose; camera=(right, up, view) unit vectors."""
    right, up, view = camera
    light = np.array([.35, 1, .6]); light /= np.linalg.norm(light)
    for e in model.elements:
        quads = face_quads(e['a'], e['b'])
        for side, (mat, coords) in e['faces'].items():
            pts, normal = quads[side]
            pts = np.array(pts, float)
            n = np.array(normal, float)
            if e['rot']:
                axis, angle, o = e['rot']
                r = rotation(axis, angle)
                pts = (pts - o) @ r.T + o
                n = r @ n
            pts, n = transform(pts, n)
            if n @ view >= 0:
                continue
            shade = .55 + .45 * max(0., n @ light)
            sx = (pts @ right) * SCALE + origin[0]
            sy = -(pts @ up) * SCALE + origin[1]
            depth = pts @ view
            tex = texture(forge.TEXTURES[mat])
            u0, v0, u1, v1 = coords
            uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
            for tri in ((0, 1, 2), (0, 2, 3)):
                raster(canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)], uvs[list(tri)], tex, shade)


def raster(c, xs, ys, zs, uvs, tex, shade):
    x0, x1 = max(int(math.floor(xs.min())), 0), min(int(math.ceil(xs.max())), c.w - 1)
    y0, y1 = max(int(math.floor(ys.min())), 0), min(int(math.ceil(ys.max())), c.h - 1)
    if x0 > x1 or y0 > y1:
        return
    det = (ys[1] - ys[2]) * (xs[0] - xs[2]) + (xs[2] - xs[1]) * (ys[0] - ys[2])
    if abs(det) < 1e-9:
        return
    gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + .5, np.arange(y0, y1 + 1) + .5)
    l0 = ((ys[1] - ys[2]) * (gx - xs[2]) + (xs[2] - xs[1]) * (gy - ys[2])) / det
    l1 = ((ys[2] - ys[0]) * (gx - xs[2]) + (xs[0] - xs[2]) * (gy - ys[2])) / det
    l2 = 1 - l0 - l1
    inside = (l0 >= -1e-4) & (l1 >= -1e-4) & (l2 >= -1e-4)
    if not inside.any():
        return
    z = l0 * zs[0] + l1 * zs[1] + l2 * zs[2]
    u = l0 * uvs[0, 0] + l1 * uvs[1, 0] + l2 * uvs[2, 0]
    v = l0 * uvs[0, 1] + l1 * uvs[1, 1] + l2 * uvs[2, 1]
    th, tw = tex.shape[:2]
    tu = np.clip((u / 16 * tw).astype(int), 0, tw - 1)
    tv = np.clip((v / 16 * th).astype(int), 0, th - 1)
    texel = tex[tv, tu]
    region = (slice(y0, y1 + 1), slice(x0, x1 + 1))
    ok = inside & (texel[..., 3] > .5) & (z < c.depth[region])
    c.depth[region][ok] = z[ok]
    c.color[region][ok] = texel[..., :3][ok] * shade


def camera(yaw, pitch):
    y, p = math.radians(yaw), math.radians(pitch)
    view = np.array([-math.sin(y) * math.cos(p), -math.sin(p), -math.cos(y) * math.cos(p)])
    right = np.array([math.cos(y), 0, -math.sin(y)])
    up = np.cross(right, view)
    return right, -up if up[1] < 0 else up, view


def pivot(axis, degrees, centre):
    r = rotation(axis, degrees)
    centre = np.array(centre, float)
    return lambda p, n: ((p - centre) @ r.T + centre, r @ n)


def translate(offset):
    offset = np.array(offset, float)
    return lambda p, n: (p + offset, n)


def poses(t, phase):
    """Mirror of ResonantForgeRenderer: part name -> transform, at animation phase 0..1."""
    spin = phase * 360
    crank_angle = spin
    lift = math.sin(math.radians(crank_angle))
    helve_angle = -12 * max(0., math.sin(math.radians(crank_angle * 2)))
    # Crank pin, pitman and treadle tail as one linkage, solved like the renderer does.
    _, ay, az = forge.HAMMER_AXIS
    _, ty, tz = forge.TREADLE_PIVOT
    pin_y = ay + forge.CRANK_RADIUS * math.cos(math.radians(crank_angle))
    pin_z = az + forge.CRANK_RADIUS * math.sin(math.radians(crank_angle))
    reach, length = tz - az, ay + forge.CRANK_RADIUS - ty
    dy, dz = pin_y - ty, pin_z - tz
    span = math.hypot(dy, dz)
    along = (reach * reach - length * length + span * span) / (2 * span)
    across = math.sqrt(max(0., reach * reach - along * along))
    tail_y, tail_z = ty + (along * dy + across * dz) / span, tz + (along * dz - across * dy) / span
    rest_pin = (-15.5, ay + forge.CRANK_RADIUS, az)
    rod = pivot('x', math.degrees(math.atan2(pin_z - tail_z, pin_y - tail_y)), rest_pin)
    shift = translate((0, pin_y - rest_pin[1], pin_z - rest_pin[2]))
    out = {
        'crank': pivot('x', crank_angle, forge.HAMMER_AXIS),
        'pitman': lambda p, n: shift(*rod(p, n)),
        'treadle': pivot('x', math.degrees(math.atan2(tail_y - ty, tz - tail_z)), forge.TREADLE_PIVOT),
        'wheel': pivot('z', -spin, forge.WHEEL_AXIS),
        'stylus': pivot('x', 0, forge.STYLUS_PIVOT),
        'carriage': translate((forge.CARRIAGE_TRAVEL * (2 * phase - 1), 0, 0)),
        'governor': pivot('y', spin * 2, forge.GOVERNOR_AXIS),
        'heart': lambda p, n: ((p - forge.HEART_CENTER) * (1 + .06 * lift) + forge.HEART_CENTER, n),
    }
    for part in ('helve', 'helve_iron', 'head', 'head_iron'):
        out[part] = pivot('x', helve_angle, forge.HELVE_PIVOT)
    return out


def render(stages=(('Base', 0), ('Precision', 1), ('Masterwork', 2)),
           views=(('worker side', 35, 28), ('behind', 215, 24)), out=OUT, scale=7.0, phase=.1):
    global SCALE
    SCALE = scale
    panel_w, panel_h = int(80 * scale), int(67 * scale)
    canvas = Canvas(panel_w * len(stages), panel_h * len(views))
    centre = np.array([8, 12, 16])
    for row, (_, yaw, pitch) in enumerate(views):
        cam = camera(yaw, pitch)
        for col, (_, t) in enumerate(stages):
            origin = (col * panel_w + panel_w / 2 - centre @ cam[0] * SCALE,
                      row * panel_h + panel_h / 2 + 30 + centre @ cam[1] * SCALE)
            identity = lambda p, n: (p, n)
            draw_model(canvas, forge.static(t), identity, cam, origin)
            pose = poses(t, phase)
            for part in forge.stage_parts(t):
                draw_model(canvas, forge.PARTS[part](), pose.get(part, identity), cam, origin)
            for mat, axis in (('wax', forge.HAMMER_AXIS), ('wax', forge.RECORD_AXIS)):
                cyl = forge.cylinder(mat)
                spin_axis = 'x'
                draw_model(canvas, cyl, lambda p, n, a=axis: (p + np.array(a), n), cam, origin)
    image = Image.fromarray((np.clip(canvas.color, 0, 1) * 255).astype(np.uint8))
    draw = ImageDraw.Draw(image)
    try:
        font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 22)
    except OSError:
        font = ImageFont.load_default()
    for row, (view, _, _) in enumerate(views):
        for col, (title, _) in enumerate(stages):
            draw.text((col * panel_w + 14, row * panel_h + 10), '%s - %s' % (title, view), font=font, fill='#efddd5')
    image = image.convert('RGB')
    out.parent.mkdir(exist_ok=True)
    image.save(out)
    return out


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--stage', type=int)
    parser.add_argument('--yaw', type=float, default=35)
    parser.add_argument('--pitch', type=float, default=28)
    parser.add_argument('--scale', type=float, default=7)
    parser.add_argument('--phase', type=float, default=.1)
    parser.add_argument('--out')
    a = parser.parse_args()
    if a.stage is None:
        print(render())
    else:
        names = ['Base', 'Precision', 'Masterwork']
        print(render(((names[a.stage], a.stage),), (('view', a.yaw, a.pitch),),
                     Path(a.out) if a.out else ROOT / 'build/resonant_forge_detail.png', a.scale, a.phase))
