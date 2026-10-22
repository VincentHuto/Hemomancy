"""Z-buffered textured previews of the Hematic Armature models written by hematic_armature.py.

Renders each stage from the subject's front and from behind, with the reservoirs part-filled and
the banners mid-sway, so sections, pivots and the slimmer footprint can be checked without
launching the client. Rasterising is shared with resonant_forge_preview; glass is blended over
the opaque pass by vial_centrifuge_preview.
"""
import numpy as np
from PIL import Image, ImageDraw, ImageFont

import hematic_armature as armature
import resonant_forge_preview as raster
import vial_centrifuge_preview

OUT = armature.ROOT / 'build/hematic_armature_preview.png'


def draw(canvas, model, transform, cam, origin, scale, glass):
    right, up, view = cam
    light = np.array([.35, 1, .6])
    light /= np.linalg.norm(light)
    for e in model.elements:
        quads = raster.face_quads(e['a'], e['b'])
        for side, (mat, coords) in e['faces'].items():
            if (mat in armature.TRANSLUCENT) != glass:
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
            tex = raster.texture(armature.TEXTURES[mat])
            u0, v0, u1, v1 = coords
            uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
            for tri in ((0, 1, 2), (0, 2, 3)):
                (vial_centrifuge_preview.blend if glass else raster.raster)(
                    canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)], uvs[list(tri)], tex, shade)


def fluid(floor, depth, fill, dx=0.):
    """Mirror of the renderer: stretch a unit-height fluid model from its floor to the fill level."""
    height = depth * fill
    return lambda p, n: ((p - [0, floor, 0]) * [1, height, 1] + [dx, floor, 0], n)


def render(stages=(('Base', 0), ('Consecrated', 1), ('Monolithic', 2)),
           views=(('subject front', 200, 22), ('behind', 20, 18)), out=OUT, scale=5.0, fill=.6, sway=4.):
    panel_w, panel_h = int(100 * scale), int(100 * scale)
    canvas = raster.Canvas(panel_w * len(stages), panel_h * len(views))
    centre = np.array([8, 38, 0])
    identity = lambda p, n: (p, n)
    parts = {name: build() for name, build in armature.PARTS.items()}
    radius, bottom, top = armature.VIAL_FLUID
    _, _, h_radius, h_bottom, h_top = armature.HEART + armature.HEART_FLUID
    for row, (_, yaw, pitch) in enumerate(views):
        cam = raster.camera(yaw, pitch)
        for col, (_, t) in enumerate(stages):
            origin = (col * panel_w + panel_w / 2 - centre @ cam[0] * scale,
                      row * panel_h + panel_h / 2 + centre @ cam[1] * scale)
            body = armature.static(t)
            for glass in (False, True):
                draw(canvas, body, identity, cam, origin, scale, glass)
                for k, x in enumerate(armature.POSTS):
                    draw(canvas, parts['vial_blood'], fluid(bottom, top - bottom, fill, x - armature.POSTS[0]),
                         cam, origin, scale, glass)
                if t == 2:
                    draw(canvas, parts['heart_glass'], identity, cam, origin, scale, glass)
                    draw(canvas, parts['heart_blood'], fluid(h_bottom, h_top - h_bottom, fill), cam, origin, scale, glass)
                if t:
                    banner = parts['banner' if t == 1 else 'banner_monolithic']
                    py, pz = armature.BANNER_PIVOT
                    for k, x in enumerate(armature.BANNERS):
                        swing = raster.pivot('z', sway if k == 0 else -sway, (armature.BANNERS[0], py, pz))
                        shift = raster.translate((x - armature.BANNERS[0], 0, 0))
                        draw(canvas, banner, vial_centrifuge_preview.chain(swing, shift), cam, origin, scale, glass)
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
    parser.add_argument('--yaw', type=float, default=200)
    parser.add_argument('--pitch', type=float, default=22)
    parser.add_argument('--scale', type=float, default=5)
    parser.add_argument('--out')
    a = parser.parse_args()
    if a.stage is None:
        print(render())
    else:
        names = ['Base', 'Consecrated', 'Monolithic']
        print(render(((names[a.stage], a.stage),), (('view', a.yaw, a.pitch),),
                     armature.ROOT / (a.out or 'build/hematic_armature_detail.png'), a.scale))
