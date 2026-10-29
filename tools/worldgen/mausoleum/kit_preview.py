"""Textured cutaway previews of kit rooms and generated templates, so a module or a piece can be looked at without a
client. Blocks are drawn from their blockstate/model textures (the mod's assets, vanilla's from the NeoForge
client-extra jar in build/moddev); stairs, slabs and walls as their boxes, set pieces as a plain box.

    python tools/worldgen/mausoleum/kit_preview.py [saint]            every kit module in a test room
    python tools/worldgen/mausoleum/kit_preview.py [saint] top nave  generated templates by name

Writes build/mausoleum-kit/<saint>/<name>.png: a section with the near half cut away and a plan from above with the
ceiling cut away.
"""
import io
import json
import math
import os
import sys
import zipfile

import numpy as np
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'model_export'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'worldgen'))

import resonant_forge_preview as raster  # noqa: E402
from mausoleum import kit  # noqa: E402

AIR = kit.AIR
OUT = os.path.join(ROOT, 'build', 'mausoleum-kit')
_jar = raster.vanilla_jar()
_json = {}


def _read_json(namespace, kind, path):
    key = (namespace, kind, path)
    if key not in _json:
        file = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', namespace, kind, path + '.json')
        data = None
        if os.path.exists(file):
            with open(file) as f:
                data = json.load(f)
        elif _jar is not None:
            try:
                data = json.loads(_jar.read('assets/%s/%s/%s.json' % (namespace, kind, path)))
            except KeyError:
                data = None
        _json[key] = data
    return _json[key]


def _split(location, default='minecraft'):
    return location.split(':') if ':' in location else (default, location)


def _model_textures(model):
    textures = {}
    while model:
        namespace, path = _split(model)
        data = _read_json(namespace, 'models', path)
        if data is None:
            break
        for key, value in data.get('textures', {}).items():
            textures.setdefault(key, value)
        model = data.get('parent')
    for _ in range(4):
        for key, value in list(textures.items()):
            if value.startswith('#'):
                textures[key] = textures.get(value[1:], value)
    return textures


_faces = {}


def face_textures(name, props):
    """The texture for each of a block's six faces, from the variant matching its state (or its first)."""
    key = (name, props)
    if key in _faces:
        return _faces[key]
    namespace, path = _split(name)
    state = _read_json(namespace, 'blockstates', path) or {}
    model = None
    wanted = dict(props)
    for variant, value in state.get('variants', {}).items():
        pairs = dict(p.split('=') for p in variant.split(',') if p)
        if all(wanted.get(k) == v for k, v in pairs.items()):
            model = (value[0] if isinstance(value, list) else value)['model']
            break
    if model is None:
        for part in state.get('multipart', []):
            apply = part['apply']
            model = (apply[0] if isinstance(apply, list) else apply)['model']
            break
    textures = _model_textures(model) if model else {}
    out = {}
    for side in ('up', 'down', 'north', 'south', 'east', 'west'):
        order = ([side, 'top' if side == 'up' else 'bottom', 'end'] if side in ('up', 'down')
                 else [side, 'side', 'front'])
        order += ['all', 'texture', 'wall', 'side', 'top', 'particle']
        found = next((textures[k] for k in order if k in textures and not textures[k].startswith('#')), None)
        out[side] = found or 'minecraft:block/stone'
    if path.endswith('_stairs') or path.endswith('_slab'):
        base = None
        for k in ('side', 'texture', 'all'):
            if k in textures:
                base = textures[k]
                break
        if base:
            out.update({s: textures.get('top', base) if s == 'up' else textures.get('bottom', base) if s == 'down'
                        else base for s in out})
    _faces[key] = out
    return out


def _texture(location):
    namespace, path = _split(location)
    return raster.texture('%s:%s' % (namespace, path))


# --- geometry -------------------------------------------------------------------------------------------------------

QUARTER = {'north': (0, 8, 0, 16), 'south': (8, 16, 0, 16), 'west': (0, 16, 0, 8), 'east': (0, 16, 8, 16)}
LEFT = {'north': 'west', 'east': 'north', 'south': 'east', 'west': 'south'}
RIGHT = {v: k for k, v in LEFT.items()}


def _half_box(side):
    """The half of a block on one side: (x0, x1, z0, z1) in pixels."""
    return {'north': (0, 16, 0, 8), 'south': (0, 16, 8, 16), 'west': (0, 8, 0, 16), 'east': (8, 16, 0, 16)}[side]


def _intersect(a, b):
    return (max(a[0], b[0]), min(a[1], b[1]), max(a[2], b[2]), min(a[3], b[3]))


def boxes(name, props):
    """The block's boxes in pixels: [(x0, y0, z0, x1, y1, z1)], and whether it is a full opaque cube."""
    p = dict(props)
    if name.endswith('_stairs'):
        top = p.get('half') == 'top'
        slab_y = (8, 16) if top else (0, 8)
        step_y = (0, 8) if top else (8, 16)
        facing, shape = p.get('facing', 'north'), p.get('shape', 'straight')
        parts = []
        back = _half_box(facing)
        if shape == 'straight':
            parts.append(back)
        elif shape.startswith('inner'):
            side = LEFT[facing] if shape.endswith('left') else RIGHT[facing]
            parts.append(back)
            parts.append(_half_box(side))
        else:
            side = LEFT[facing] if shape.endswith('left') else RIGHT[facing]
            parts.append(_intersect(back, _half_box(side)))
        out = [(0, slab_y[0], 0, 16, slab_y[1], 16)]
        out += [(b[0], step_y[0], b[2], b[1], step_y[1], b[3]) for b in parts]
        return out, False
    if name.endswith('_slab'):
        kind = p.get('type', 'bottom')
        if kind == 'double':
            return [(0, 0, 0, 16, 16, 16)], True
        return [(0, 8, 0, 16, 16, 16) if kind == 'top' else (0, 0, 0, 16, 8, 16)], False
    if name.endswith('_wall'):
        out = [(4, 0, 4, 12, 16, 12)] if p.get('up') == 'true' else []
        arms = {'north': (5, 11, 0, 8), 'south': (5, 11, 8, 16), 'west': (0, 8, 5, 11), 'east': (8, 16, 5, 11)}
        for side, (x0, x1, z0, z1) in arms.items():
            if p.get(side) in ('low', 'tall'):
                out.append((x0, 0, z0, x1, 14 if p[side] == 'low' else 16, z1))
        return out or [(4, 0, 4, 12, 16, 12)], False
    if name.endswith(('_button', '_pressure_plate')):
        return [(5, 0, 5, 11, 1, 11)], False
    if name.endswith('_door'):
        facing = p.get('facing', 'north')
        x0, x1, z0, z1 = {'north': (0, 16, 13, 16), 'south': (0, 16, 0, 3), 'west': (13, 16, 0, 16),
                          'east': (0, 3, 0, 16)}[facing]
        return [(x0, 0, z0, x1, 16, z1)], False
    if name.startswith('hemomancy:') and p and set(p) - {'axis'}:
        if name.endswith(('_niche',)):
            facing = p.get('facing', 'north')
            x0, x1, z0, z1 = {'north': (0, 16, 0, 2), 'south': (0, 16, 14, 16), 'west': (0, 2, 0, 16),
                              'east': (14, 16, 0, 16)}[facing]
            return [(x0, 0, z0, x1, 16, z1)], False
        if name == 'hemomancy:reliquary_sconce':
            facing = p.get('facing', 'north')
            x0, x1, z0, z1 = {'north': (5, 11, 10, 16), 'south': (5, 11, 0, 6), 'west': (10, 16, 5, 11),
                              'east': (0, 6, 5, 11)}[facing]
            return [(x0, 4, z0, x1, 13, z1)], False
        return [(3, 0, 3, 13, 14, 13)], False
    if name in ('hemomancy:saint_sarcophagus', 'hemomancy:ring_door', 'hemomancy:chapel_focus',
                'hemomancy:offering_stand', 'hemomancy:blood_basin'):
        return [(2, 0, 2, 14, 14, 14)], False
    return [(0, 0, 0, 16, 16, 16)], True


GLOW = ('shroomlight', 'sea_lantern', 'glowstone', 'reliquary_sconce', 'lantern')


def render(room, keep, yaw, pitch, scale, size=None):
    """Draws the kept cells of a room; returns a PIL image."""
    cells = {pos: entry for pos, entry in room.blocks.items() if entry[0] not in (AIR, 'minecraft:jigsaw')
             and keep(*pos)}
    cube = {pos: boxes(entry[0], entry[1])[1] for pos, entry in cells.items()}
    cam = raster.camera(yaw, pitch)
    right, up, view = cam
    pts = np.array(list(cells) or [(0, 0, 0)], float)
    sx, sy = pts @ right, -(pts @ up)
    w = int((sx.max() - sx.min() + 3) * scale) + 40
    h = int((sy.max() - sy.min() + 3) * scale) + 60
    canvas = raster.Canvas(*(size or (w, h)))
    origin = (20 - (sx.min() - 1.5) * scale, 40 - (sy.min() - 1.5) * scale)
    light = np.array([.35, 1, .6])
    light /= np.linalg.norm(light)
    neighbours = {'up': (0, 1, 0), 'down': (0, -1, 0), 'north': (0, 0, -1), 'south': (0, 0, 1), 'east': (1, 0, 0),
                  'west': (-1, 0, 0)}
    for pos, (name, props, _) in cells.items():
        parts, full = boxes(name, props)
        textures = face_textures(name, props)
        glow = any(g in name for g in GLOW)
        for (x0, y0, z0, x1, y1, z1) in parts:
            a = (pos[0] + x0 / 16, pos[1] + y0 / 16, pos[2] + z0 / 16)
            b = (pos[0] + x1 / 16, pos[1] + y1 / 16, pos[2] + z1 / 16)
            for side, (corners, normal) in raster.face_quads(a, b).items():
                n = np.array(normal, float)
                if n @ view >= 0:
                    continue
                if full:
                    d = neighbours[side]
                    if cube.get((pos[0] + d[0], pos[1] + d[1], pos[2] + d[2])):
                        continue
                corners = np.array(corners, float)
                px = corners @ right * scale + origin[0]
                py = -(corners @ up) * scale + origin[1]
                depth = corners @ view
                if side in ('up', 'down'):
                    u0, u1, v0, v1 = x0, x1, z0, z1
                elif side in ('north', 'south'):
                    u0, u1, v0, v1 = x0, x1, 16 - y1, 16 - y0
                else:
                    u0, u1, v0, v1 = z0, z1, 16 - y1, 16 - y0
                uvs = np.array([(u0, v0), (u1, v0), (u1, v1), (u0, v1)], float)
                shade = 1.0 if glow else .5 + .5 * max(0., n @ light)
                tex = _texture(textures[side])
                for tri in ((0, 1, 2), (0, 2, 3)):
                    raster.raster(canvas, px[list(tri)], py[list(tri)], depth[list(tri)], uvs[list(tri)], tex, shade)
    return Image.fromarray((np.clip(canvas.color, 0, 1) * 255).astype(np.uint8))


def sheet(room, title, path, scale=None, section_z=None, plan_y=None):
    """A section (the half nearer the viewer, z under section_z, cut away) and a plan (everything over plan_y cut)."""
    sx, sy, sz = room.size
    scale = scale or max(8, min(24, int(520 / max(sx, sz, sy))))
    section_z = (sz // 2) if section_z is None else section_z
    plan_y = (sy // 2) if plan_y is None else plan_y
    views = [('section', lambda x, y, z: z >= section_z, 200, 22),
             ('vault from below', lambda x, y, z: z >= section_z and y > 0, 200, -28),
             ('plan', lambda x, y, z: y <= plan_y, 215, 58),
             ('outside', lambda x, y, z: True, 215, 30)]
    images = [(label, render(room, keep, yaw, pitch, scale)) for label, keep, yaw, pitch in views]
    w = sum(i.width for _, i in images)
    h = max(i.height for _, i in images)
    out = Image.new('RGB', (w, h + 30), (32, 32, 39))
    draw = ImageDraw.Draw(out)
    try:
        font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 18)
    except OSError:
        font = ImageFont.load_default()
    x = 0
    for label, image in images:
        out.paste(image, (x, 30))
        draw.text((x + 10, 5), '%s: %s' % (title, label), font=font, fill='#efddd5')
        x += image.width
    os.makedirs(os.path.dirname(path), exist_ok=True)
    out.save(path)
    return path


# --- the module test rooms ------------------------------------------------------------------------------------------

def module_rooms(c):
    """One small test room per kit module, each carved from mass and finished like a template."""
    import random
    rooms = {}

    def base(sx, sy, sz, key):
        rng = random.Random('preview/' + key)
        room = kit.Room(sx, sy, sz)
        kit.mass(room, c, rng, 0, sx - 1, 0, sy - 1, 0, sz - 1)
        return room, rng

    room, rng = base(17, 9, 7, 'corridor')
    kit.corridor(room, c, rng, 1, 15, 3, 1, 0, 5)
    kit.arch(room, c, 0, 0, 3)
    kit.arch(room, c, 16, 0, 3)
    rooms['corridor'] = room

    room, rng = base(19, 16, 15, 'hall')
    kit.hall(room, c, rng, 1, 17, 7, 5, 0, 9)
    kit.arch(room, c, 0, 0, 7)
    rooms['hall'] = room

    room, rng = base(11, 11, 11, 'vault_cross')
    room.carve(1, 9, 1, 5, 1, 9)
    kit.vault_ceiling(room, c, 1, 9, 1, 9, 6, kind='cross')
    rooms['vault_ceiling_cross'] = room

    room, rng = base(13, 10, 9, 'vault_barrel')
    room.carve(1, 11, 1, 4, 1, 7)
    kit.vault_ceiling(room, c, 1, 11, 1, 7, 5, kind='barrel', axis='x')
    rooms['vault_ceiling_barrel'] = room

    room, rng = base(9, 9, 5, 'arch')
    room.carve(1, 7, 1, 7, 1, 1)
    kit.arch_z(room, c, 2, 0, 4)
    room.carve(3, 5, 1, 3, 3, 4)
    rooms['arch'] = room

    shaft = kit.Shaft(15, 22, 17, turns=2)
    rooms['stair_shaft'] = kit.stair_shaft(c, random.Random('preview/shaft'), shaft)

    room, rng = base(9, 7, 5, 'niche')
    room.carve(1, 7, 1, 5, 2, 3)
    for x, kind in ((2, 'sealed'), (4, 'opened'), (6, 'sealed')):
        kit.niche(room, c, rng, x, 2, 1, 0, 'south', kind)
    rooms['niche'] = room

    room, rng = base(13, 8, 13, 'dais')
    room.carve(1, 11, 1, 6, 1, 11)
    kit.dais(room, c, 2, 10, 2, 10, 0, 3, ring_radius=1.4)
    rooms['dais'] = room

    room, rng = base(9, 9, 2, 'speckle')
    kit.speckle(room, c, rng, 0, 8, 0, 8, 0, 1, chance=0.3)
    rooms['speckle'] = room

    for name, room in rooms.items():
        kit.finish(room, c, 'preview/' + name)
    return rooms


def main(argv):
    from mausoleum.themes import THEMES
    saint = argv[0] if argv and argv[0] in THEMES else 'hemorath'
    names = [a for a in argv if a not in THEMES]
    c = THEMES[saint].CRYPT
    out = os.path.join(OUT, saint)
    if names:
        from mausoleum import build
        rooms = build.templates(saint)
        for name in names:
            print(sheet(rooms[name], '%s %s' % (saint, name), os.path.join(out, name + '.png')))
        return
    for name, room in module_rooms(c).items():
        print(sheet(room, '%s %s' % (saint, name), os.path.join(out, name + '.png')))


if __name__ == '__main__':
    os.chdir(ROOT)
    main(sys.argv[1:])
