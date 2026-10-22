"""Z-buffered textured previews of the Vial Centrifuge models written by vial_centrifuge.py.

Renders each stage from the operator's side and from behind with the rotor mid-spin, buckets swung
out and vials loaded, so pivots and swing clearance can be checked without launching the client.
Rasterising is shared with resonant_forge_preview; glass is blended over the opaque pass.
"""
import math

import numpy as np
from PIL import Image, ImageDraw, ImageFont

import resonant_forge_preview as raster
import vial_centrifuge as centrifuge

OUT = centrifuge.ROOT / 'build/vial_centrifuge_preview.png'


def draw(canvas, model, transform, cam, origin, scale, glass):
    right, up, view = cam
    light = np.array([.35, 1, .6])
    light /= np.linalg.norm(light)
    for e in model.elements:
        quads = raster.face_quads(e['a'], e['b'])
        for side, (mat, coords) in e['faces'].items():
            if (mat in centrifuge.TRANSLUCENT) != glass:
                continue
            pts, normal = quads[side]
            pts, n = np.array(pts, float), np.array(normal, float)
            if e['rot']:
                axis, angle, o = e['rot']
                r = raster.rotation(axis, angle)
                pts = (pts - o) @ r.T + o
                n = r @ n
            pts, n = transform(pts, n)
            if n @ view >= 0:
                continue
            shade = .55 + .45 * max(0., n @ light)
            sx = (pts @ right) * scale + origin[0]
            sy = -(pts @ up) * scale + origin[1]
            depth = pts @ view
            tex = raster.texture(centrifuge.TEXTURES[mat])
            u0, v0, u1, v1 = coords
            uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
            for tri in ((0, 1, 2), (0, 2, 3)):
                (blend if glass else raster.raster)(canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)],
                                                    uvs[list(tri)], tex, shade)


def blend(c, xs, ys, zs, uvs, tex, shade):
    """Glass pass: alpha-blend in front of what the opaque pass drew, without writing depth."""
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
    u = l0 * uvs[0, 0] + l1 * uvs[1, 0] + l2 * uvs[2, 0]
    v = l0 * uvs[0, 1] + l1 * uvs[1, 1] + l2 * uvs[2, 1]
    th, tw = tex.shape[:2]
    texel = tex[np.clip((v / 16 * th).astype(int), 0, th - 1), np.clip((u / 16 * tw).astype(int), 0, tw - 1)]
    region = (slice(y0, y1 + 1), slice(x0, x1 + 1))
    ok = inside & (z < c.depth[region])
    alpha = texel[..., 3:4]
    c.color[region][ok] = (c.color[region] * (1 - alpha) + texel[..., :3] * shade * alpha)[ok]


def chain(*steps):
    def apply(p, n):
        for step in steps:
            p, n = step(p, n)
        return p, n
    return apply


def poses(t, angle, speed):
    """Mirror of VialCentrifugeRenderer at rotor angle `angle` and speed fraction `speed`."""
    centre = (centrifuge.CENTRE[0], 0, centrifuge.CENTRE[1])
    swing = centrifuge.MAX_SWING * min(1., speed / .7)
    y, z = centrifuge.CRANK_AXIS
    spin = raster.pivot('y', angle, centre)
    out = {
        'crank': raster.pivot('x', angle * .25, (0, y, z)),
        'flywheel': raster.pivot('x', angle * .25, (0, y, z)),
        'needle': raster.pivot('z', -120 + 240 * speed, centrifuge.DIAL),
    }
    for part in ('rotor', 'rotor_calibrated', 'rotor_fractionating', 'governor', 'governor_crowned'):
        out[part] = spin
    buckets = [chain(raster.pivot('z', swing, centrifuge.PIVOT), raster.pivot('y', angle + k * 45, centre))
               for k in range(8)]
    flyballs = [chain(raster.pivot('z', 40 * speed * speed, centrifuge.HINGE), raster.pivot('y', angle + k * 180, centre))
                for k in range(2)]
    return out, buckets, flyballs


def render(stages=(('Base', 0), ('Calibrated', 1), ('Fractionating', 2)),
           views=(('operator side', 210, 26), ('behind', 30, 22)), out=OUT, scale=14.0, angle=20, speed=.9):
    panel_w, panel_h = int(30 * scale), int(36 * scale)
    canvas = raster.Canvas(panel_w * len(stages), panel_h * len(views))
    centre = np.array([8, 11, 8])
    identity = lambda p, n: (p, n)
    contents = ['vial_blood', 'vial_blood', 'vial_spent', 'vial_hallowed', 'vial_blood', 'vial_blood', 'vial_spent', 'vial_hallowed']
    reservoir = lambda p, n: (p * np.array([1, 2.5, 1]) - np.array([0, centrifuge.JAR_FLUID[1] * 1.5, 0]), n)
    for row, (_, yaw, pitch) in enumerate(views):
        cam = raster.camera(yaw, pitch)
        for col, (_, t) in enumerate(stages):
            origin = (col * panel_w + panel_w / 2 - centre @ cam[0] * scale,
                      row * panel_h + panel_h / 2 + 30 + centre @ cam[1] * scale)
            pose, buckets, flyballs = poses(t, angle, speed)
            for glass in (False, True):
                draw(canvas, centrifuge.static(t), identity, cam, origin, scale, glass)
                draw(canvas, centrifuge.reservoir_blood(), reservoir, cam, origin, scale, glass)
                for part in centrifuge.stage_parts(t):
                    draw(canvas, centrifuge.PARTS[part](), pose.get(part, identity), cam, origin, scale, glass)
                for k, bucket in enumerate(buckets):
                    draw(canvas, centrifuge.bucket(), bucket, cam, origin, scale, glass)
                    draw(canvas, centrifuge.vial_glass(), bucket, cam, origin, scale, glass)
                    draw(canvas, centrifuge.PARTS[contents[k]](), bucket, cam, origin, scale, glass)
                if t:
                    for ball in flyballs:
                        draw(canvas, centrifuge.flyball(), ball, cam, origin, scale, glass)
    image = Image.fromarray((np.clip(canvas.color, 0, 1) * 255).astype(np.uint8))
    text = ImageDraw.Draw(image)
    try:
        font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 22)
    except OSError:
        font = ImageFont.load_default()
    for row, (view, _, _) in enumerate(views):
        for col, (title, _) in enumerate(stages):
            text.text((col * panel_w + 14, row * panel_h + 10), '%s - %s' % (title, view), font=font, fill='#efddd5')
    out.parent.mkdir(exist_ok=True)
    image.convert('RGB').save(out)
    return out


if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument('--stage', type=int)
    parser.add_argument('--yaw', type=float, default=210)
    parser.add_argument('--pitch', type=float, default=26)
    parser.add_argument('--scale', type=float, default=14)
    parser.add_argument('--angle', type=float, default=20)
    parser.add_argument('--speed', type=float, default=.9)
    parser.add_argument('--out')
    a = parser.parse_args()
    if a.stage is None:
        print(render(angle=a.angle, speed=a.speed))
    else:
        names = ['Base', 'Calibrated', 'Fractionating']
        print(render(((names[a.stage], a.stage),), (('view', a.yaw, a.pitch),),
                     centrifuge.ROOT / (a.out or 'build/vial_centrifuge_detail.png'), a.scale, a.angle, a.speed))
