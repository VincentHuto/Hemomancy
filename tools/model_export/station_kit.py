"""Shared authoring kit for scripted Harbinger stations built from world textures.

Used by morphling_incubator.py and mycelial_crucible.py. It is the vial_centrifuge.py element model
(world-aligned UVs, octagonal prisms, hollow glass shells, Blockbench export, z-buffered preview)
with the texture table passed in, so each station names its own materials.

Harbinger materials only: hematic iron, bone, blood-wood, venous stone, flesh and fungal growth.
Copper and its alloys are antiseptic Unstained metals, so no brass, bronze, copper or silver tones.
"""
from pathlib import Path
import json
import math
import zlib

import numpy as np
from PIL import Image, ImageDraw, ImageFont

import coplanar
import resonant_forge_preview as raster

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ASSETS / 'models/block'
BBMODEL = MODELS / 'bbmodel'
TAN = math.tan(math.radians(22.5))
SIDES = ('north', 'south', 'east', 'west', 'up', 'down')
# Element data for parts that glow: unshaded and lit as if by a full-strength light source.
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}


class Model:
    def __init__(self, translucent=(), caps=None):
        self.elements = []
        self.translucent = set(translucent)
        self.caps = caps or {}

    def box(self, name, a, b, mat, rot=None, faces=None, caps=None, extra=None):
        """Axis-aligned cube from a to b; rot=(axis, angle, origin) applies a Java model rotation."""
        a, b = [round(min(p, q), 4) for p, q in zip(a, b)], [round(max(p, q), 4) for p, q in zip(a, b)]
        if any(q - p <= 0 for p, q in zip(a, b)):
            return
        out = {}
        for side in faces or SIDES:
            m = caps.get(side, mat) if caps else mat
            coords = uv(side, a, b)
            # Glass panes keep the world-aligned, face-sized UVs too, so the glass sits at block texel density.
            out[side] = (m, coords)
        self.elements.append(dict(name=name, a=a, b=b, rot=rot, faces=out, extra=extra))

    def octo(self, name, axis, centre, radius, start, end, mat, cap=None, bottom=None, faces=None, extra=None):
        """Solid regular octagonal prism along `axis` (flat-to-flat 2*radius).

        `cap` textures both end faces, `bottom` overrides the low end; `faces` limits the sides emitted.
        """
        cap = cap or self.caps.get(mat, mat)
        i = 'xyz'.index(axis)
        o = [k for k in range(3) if k != i]
        ends = {'x': ('east', 'west'), 'y': ('up', 'down'), 'z': ('south', 'north')}[axis]
        for n, (h0, h1, angle) in enumerate(((radius, radius * TAN, 0), (radius * TAN, radius, 0),
                                             (radius, radius * TAN, 45), (radius, radius * TAN, -45))):
            inset = n * .04   # caps sit clear of coplanar.TOLERANCE instead of fighting
            a, b = [0] * 3, [0] * 3
            a[i], b[i] = start + inset, end - inset
            a[o[0]], b[o[0]] = centre[o[0]] - h0, centre[o[0]] + h0
            a[o[1]], b[o[1]] = centre[o[1]] - h1, centre[o[1]] + h1
            self.box(name, a, b, mat, (axis, angle, list(centre)) if angle else None, faces=faces,
                     caps={ends[0]: cap, ends[1]: bottom or cap}, extra=extra)

    def shell(self, name, centre, radius, y0, y1, thick, mat, ends=False):
        """Hollow upright octagon of eight panes, so whatever stands inside stays visible."""
        cx, cz = centre
        half = radius * TAN
        sides = ('north', 'south', 'east', 'west') + (('up', 'down') if ends else ())
        glass = mat in self.translucent
        for angle in (0, 45):
            rot = ('y', angle, [cx, y0, cz]) if angle else None
            for s in (-1, 1):
                lo, hi = sorted((s * radius, s * (radius - thick)))
                # Glass panes drop their sliver edges: a thin edge face reads as a stripe through the glass.
                xs = ('east', 'west') + sides[4:] if glass else sides
                zs = ('north', 'south') + sides[4:] if glass else sides
                self.box(name, (cx + lo, y0, cz - half), (cx + hi, y1, cz + half), mat, rot, faces=xs)
                self.box(name, (cx - half, y0, cz + lo), (cx + half, y1, cz + hi), mat, rot, faces=zs)

    def radial(self, name, centre, degrees, r0, r1, t0, t1, y0, y1, mat, faces=None, extra=None):
        """Box spanning radius r0..r1 and tangent t0..t1 about centre (x, z), aimed `degrees` from +x toward -z.

        Quarter turns are exact; the remainder (a multiple of 22.5) is vanilla's rotation about Y.
        """
        cx, cz = centre
        quarter = round(degrees / 90)
        rest = degrees - quarter * 90
        c, s = [(1, 0), (0, 1), (-1, 0), (0, -1)][quarter % 4]
        xs = [cx + c * r + s * t for r in (r0, r1) for t in (t0, t1)]
        zs = [cz - s * r + c * t for r in (r0, r1) for t in (t0, t1)]
        rot = ('y', rest, [cx, y0, cz]) if rest else None
        self.box(name, (min(xs), y0, min(zs)), (max(xs), y1, max(zs)), mat, rot, faces=faces, extra=extra)


def uv(side, a, b):
    """World-aligned UVs so neighbouring cubes continue the same texture."""
    if side in ('north', 'south'):
        u, v = (a[0], b[0]), (16 - b[1], 16 - a[1])
    elif side in ('east', 'west'):
        u, v = (a[2], b[2]), (16 - b[1], 16 - a[1])
    else:
        u, v = (a[0], b[0]), (a[2], b[2])
    out = []
    for lo, hi in (u, v):
        size = min(hi - lo, 16)
        start = lo % 16
        if start + size > 16:
            start = 16 - size
        out.append((round(start, 3), round(start + size, 3)))
    return [out[0][0], out[1][0], out[0][1], out[1][1]]


def polar(centre, degrees, radius):
    """Point `radius` from centre (x, z) at `degrees` from +x toward -z, matching Model.radial."""
    a = math.radians(degrees)
    return centre[0] + radius * math.cos(a), centre[1] - radius * math.sin(a)


# ---------------------------------------------------------------- export

def views(model, textures):
    """Java-model-shaped views for coplanar.py; they share each element's from/to lists."""
    out = []
    for e in model.elements:
        view = {'name': e['name'], 'from': e['a'], 'to': e['b'],
                'faces': {side: {'texture': textures[mat], 'uv': coords} for side, (mat, coords) in e['faces'].items()}}
        if e['rot']:
            axis, angle, origin = e['rot']
            view['rotation'] = {'axis': axis, 'angle': angle, 'origin': origin}
        out.append(view)
    return out


def separate(models, textures, translucent, label):
    """Pull coplanar overlapping faces apart until no pass moves anything."""
    glass = {textures[m] for m in translucent}
    for _ in range(10):
        if not sum(coplanar.resolve(views(model, textures), translucent=glass) for model in models):
            return
    raise ValueError(label + ' keeps moving its own faces')


def split_glass(model):
    """(body, glass): elements whose every face is translucent move to their own model."""
    body, glass = Model(model.translucent, model.caps), Model(model.translucent, model.caps)
    for e in model.elements:
        clear = all(mat in model.translucent for mat, _ in e['faces'].values())
        (glass if clear else body).elements.append(e)
    return body, glass


def two_layer_blockstate(name):
    """Facing multipart applying the cutout body and its translucent glass as separate baked models."""
    parts = []
    for facing, angle in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        for model in (name, name + '_glass'):
            apply = {'model': 'hemomancy:block/' + model}
            if angle:
                apply['y'] = angle
            parts.append({'apply': apply, 'when': {'facing': facing}})
    return {'multipart': parts}


def to_json(model, textures, particle, display=None):
    used, elements = [], []
    for e in model.elements:
        faces = {}
        for side, (mat, coords) in e['faces'].items():
            if mat not in used:
                used.append(mat)
            faces[side] = dict(uv=coords, texture='#' + mat)
        assert all(-16 <= v <= 32 for v in e['a'] + e['b']), (e['name'], e['a'], e['b'])
        el = {'name': e['name'], 'from': e['a'], 'to': e['b']}
        if e['rot']:
            axis, angle, origin = e['rot']
            el['rotation'] = dict(angle=angle, axis=axis, origin=origin)
        el['faces'] = faces
        el.update(e['extra'] or {})
        elements.append(el)
    out = {'parent': 'minecraft:block/block'}
    # Glass-free bodies bake into the cutout layer, which draws before block entities, so renderer-drawn
    # fluids (which do not write depth) are not painted over by the floors beneath them.
    out['render_type'] = 'translucent' if any(mat in model.translucent for mat in used) else 'minecraft:cutout'
    # The open frame and glass read wrong with vanilla corner occlusion.
    out['ambientocclusion'] = False
    if display:
        out['gui_light'] = 'front'
        out['display'] = display
    out['textures'] = dict({'particle': textures[particle]}, **{mat: textures[mat] for mat in used})
    out['elements'] = elements
    return out


def bbmodel(name, groups, textures, display=None):
    """Editable Blockbench project with the static body and every renderer-drawn part as named groups."""
    tex_list, tex_index, elements, outliner = [], {}, [], []
    uid = 0
    for group, model in groups:
        children = []
        for e in model.elements:
            faces = {}
            for side in ('north', 'east', 'south', 'west', 'up', 'down'):
                if side in e['faces']:
                    mat, coords = e['faces'][side]
                    if mat not in tex_index:
                        ns, path = textures[mat].split(':')
                        folder, file = path.split('/', 1)
                        tex_index[mat] = len(tex_list)
                        tex_list.append(dict(path=str(ASSETS.parent / ns / 'textures' / (path + '.png')).replace('\\', '/'),
                                             name=file + '.png', folder=folder, namespace=ns, id=str(len(tex_list)),
                                             render_mode='default'))
                    faces[side] = dict(uv=coords, texture=tex_index[mat])
                else:
                    faces[side] = dict(uv=[0, 0, 0, 0], texture=None)
            u = '%08x-0000-4000-8000-%012x' % (zlib.crc32(name.encode()), uid)
            uid += 1
            rotation, origin = [0, 0, 0], [8, 8, 8]
            if e['rot']:
                axis, angle, origin = e['rot']
                rotation['xyz'.index(axis)] = angle
            elements.append(dict(name=e['name'], **{'from': e['a'], 'to': e['b']}, rotation=rotation, origin=origin,
                                 faces=faces, uuid=u, box_uv=False, autouv=0, rescale=False))
            children.append(u)
        outliner.append(dict(name=group, origin=[8, 8, 8], children=children))
    project = dict(meta=dict(format_version='4.10', model_format='java_block', box_uv=False), name=name,
                   ambientocclusion=False, front_gui_light=True, resolution=dict(width=16, height=16),
                   elements=elements, outliner=outliner, textures=tex_list)
    if display:
        project['display'] = display
    return project


def write_json(path, data, indent=1):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=indent) + '\n', encoding='utf-8')


# ---------------------------------------------------------------- preview

def _draw(canvas, model, textures, transform, cam, origin, scale, glass):
    right, up, view = cam
    light = np.array([.35, 1, .6])
    light /= np.linalg.norm(light)
    for e in model.elements:
        quads = raster.face_quads(e['a'], e['b'])
        bright = bool(e['extra'] and e['extra'].get('shade') is False)
        for side, (mat, coords) in e['faces'].items():
            if (mat in model.translucent) != glass:
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
            shade = 1.0 if bright else .55 + .45 * max(0., n @ light)
            sx = (pts @ right) * scale + origin[0]
            sy = -(pts @ up) * scale + origin[1]
            depth = pts @ view
            tex = raster.texture(textures[mat])
            u0, v0, u1, v1 = coords
            uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
            for tri in ((0, 1, 2), (0, 2, 3)):
                (_blend if glass else raster.raster)(canvas, sx[list(tri)], sy[list(tri)], depth[list(tri)],
                                                     uvs[list(tri)], tex, shade)


def _blend(c, xs, ys, zs, uvs, tex, shade):
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


def identity(p, n):
    return p, n


def preview(panels, columns, out, size, scale, centre, textures):
    """Sheet of panels; each is (title, yaw, pitch, [(model, transform)]) drawn opaque first, then glass."""
    rows = math.ceil(len(panels) / columns)
    canvas = raster.Canvas(size[0] * columns, size[1] * rows)
    centre = np.array(centre, float)
    for k, (_, yaw, pitch, parts) in enumerate(panels):
        cam = raster.camera(yaw, pitch)
        col, row = k % columns, k // columns
        origin = (col * size[0] + size[0] / 2 - centre @ cam[0] * scale,
                  row * size[1] + size[1] / 2 + 20 + centre @ cam[1] * scale)
        for glass in (False, True):
            for model, transform in parts:
                _draw(canvas, model, textures, transform, cam, origin, scale, glass)
    image = Image.fromarray((np.clip(canvas.color, 0, 1) * 255).astype(np.uint8))
    text = ImageDraw.Draw(image)
    try:
        font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 22)
    except OSError:
        font = ImageFont.load_default()
    for k, (title, _, _, _) in enumerate(panels):
        text.text(((k % columns) * size[0] + 14, (k // columns) * size[1] + 10), title, font=font, fill='#efddd5')
    out.parent.mkdir(exist_ok=True)
    image.convert('RGB').save(out)
    return out
