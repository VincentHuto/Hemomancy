"""Z-buffered textured previews of the Ghastly Alembic stages written by ghastly_alembic.py.

Draws every stage from the operator's side and from behind, with the renderer's fluid column at
two-thirds full and translucent glass blended over it, so the stages can be judged without
launching the client. Rasterising is shared with resonant_forge_preview.
"""
from pathlib import Path
import math

import numpy as np
from PIL import Image, ImageDraw, ImageFont

import ghastly_alembic as alembic
import resonant_forge_preview as raster

OUT = alembic.ROOT / 'build/ghastly_alembic_preview.png'
FLUID_COLOUR = np.array([153, 13, 13]) / 255
LIGHT = np.array([.35, 1, .6]) / np.linalg.norm([.35, 1, .6])


def quads(model):
    """(points, normal, material, uv) for every face, with element rotations applied."""
    for e in model.elements:
        faces = raster.face_quads(e['a'], e['b'])
        for side, (mat, coords) in e['faces'].items():
            pts, normal = faces[side]
            pts, n = np.array(pts, float), np.array(normal, float)
            if e['rot']:
                axis, angle, o = e['rot']
                r = raster.rotation(axis, angle)
                pts = (pts - o) @ r.T + o
                n = r @ n
            yield pts, n, mat, coords


def fluid(lift, fill):
    """The renderer's octagonal fluid column as untextured quads."""
    cx, cz = alembic.CUCURBIT
    r = alembic.FLUID_RADIUS / math.cos(math.radians(22.5))
    y0 = alembic.FLUID_BOTTOM + lift
    y1 = y0 + (alembic.FLUID_TOP - alembic.FLUID_BOTTOM) * fill
    ring = [(cx + r * math.cos(math.radians(22.5 + 45 * i)), cz + r * math.sin(math.radians(22.5 + 45 * i)))
            for i in range(8)]
    for i in range(8):
        (ax, az), (bx, bz) = ring[i], ring[(i + 1) % 8]
        n = np.array([(az - bz), 0, (bx - ax)])
        yield np.array([(ax, y1, az), (bx, y1, bz), (bx, y0, bz), (ax, y0, az)]), n / np.linalg.norm(n)
    top = [(x, y1, z) for x, z in ring]
    for idx in ((0, 1, 2, 3), (0, 3, 4, 7), (4, 5, 6, 7)):
        yield np.array([top[i] for i in idx]), np.array([0, 1., 0])


def blend(c, xs, ys, zs, colour, alpha):
    """Depth-tested, non-writing alpha blend of a flat-coloured triangle."""
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
    z = l0 * zs[0] + l1 * zs[1] + l2 * zs[2]
    region = (slice(y0, y1 + 1), slice(x0, x1 + 1))
    ok = inside & (z < c.depth[region])
    c.color[region][ok] = c.color[region][ok] * (1 - alpha) + colour * alpha


def project(pts, cam, origin, scale):
    right, up, view = cam
    return (pts @ right) * scale + origin[0], -(pts @ up) * scale + origin[1], pts @ view


def draw(canvas, model, cam, origin, scale, lift):
    view = cam[2]
    glass = []
    for pts, n, mat, coords in quads(model):
        if n @ view >= 0 and mat not in alembic.TRANSLUCENT:
            continue
        if mat in alembic.TRANSLUCENT:
            glass.append((pts, n, mat, coords))
            continue
        shade = .55 + .45 * max(0., n @ LIGHT)
        sx, sy, depth = project(pts, cam, origin, scale)
        tex = raster.texture(alembic.TEXTURES[mat])
        u0, v0, u1, v1 = coords
        uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
        for tri in ((0, 1, 2), (0, 2, 3)):
            raster.raster(canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)], uvs[list(tri)], tex, shade)
    # The renderer draws the fluid before the glass blends over it.
    translucent = [(pts, n, FLUID_COLOUR * (.7 + .3 * max(0., n @ LIGHT)), .8) for pts, n in fluid(lift, .66)]
    for pts, n, mat, _ in glass:
        tex = raster.texture(alembic.TEXTURES[mat])
        colour = tex[..., :3].reshape(-1, 3).mean(0) * (.6 + .4 * max(0., abs(n @ LIGHT)))
        translucent.append((pts, n, colour, float(tex[..., 3].mean())))
    translucent.sort(key=lambda q: -(q[0].mean(0) @ view))
    for pts, n, colour, alpha in translucent:
        sx, sy, depth = project(pts, cam, origin, scale)
        for tri in ((0, 1, 2), (0, 2, 3)):
            blend(canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)], colour, alpha)


def render(stages=(('Ghastly Alembic', 'ghastly_alembic'), ('Hematic Condenser', 'ghastly_alembic_condenser'),
                   ('Athanor', 'ghastly_alembic_athanor')),
           views=(('operator side', 215, 24), ('behind', 35, 22)), out=OUT, scale=9.0):
    panel_w, panel_h = int(56 * scale), int(50 * scale)
    canvas = raster.Canvas(panel_w * len(stages), panel_h * len(views))
    centre = np.array([10, 17, 8])
    for row, (_, yaw, pitch) in enumerate(views):
        cam = raster.camera(yaw, pitch)
        for col, (_, name) in enumerate(stages):
            t, lit = alembic.STAGES[name]
            origin = (col * panel_w + panel_w / 2 - centre @ cam[0] * scale,
                      row * panel_h + panel_h / 2 + 20 + centre @ cam[1] * scale)
            draw(canvas, alembic.static(t, lit), cam, origin, scale, alembic.LIFT if t == 2 else 0)
    image = Image.fromarray((np.clip(canvas.color, 0, 1) * 255).astype(np.uint8))
    d = ImageDraw.Draw(image)
    try:
        font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 22)
    except OSError:
        font = ImageFont.load_default()
    for row, (view, _, _) in enumerate(views):
        for col, (title, _) in enumerate(stages):
            d.text((col * panel_w + 14, row * panel_h + 10), '%s - %s' % (title, view), font=font, fill='#efddd5')
    out.parent.mkdir(exist_ok=True)
    image.convert('RGB').save(out)
    return out


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--stage', choices=sorted(alembic.STAGES))
    parser.add_argument('--yaw', type=float, default=215)
    parser.add_argument('--pitch', type=float, default=24)
    parser.add_argument('--scale', type=float, default=14)
    parser.add_argument('--out')
    a = parser.parse_args()
    if a.stage is None:
        print(render())
    else:
        print(render(((a.stage, a.stage),), (('view', a.yaw, a.pitch),),
                     Path(a.out) if a.out else alembic.ROOT / 'build/ghastly_alembic_detail.png', a.scale))
