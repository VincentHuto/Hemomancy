"""Render the faculty rigs the way ModelPart does (box UVs, ZYX joint rotations) into one sheet.

Rows are figures, columns are rest views (front, three-quarter, side, back) plus the act pose.
Usage: python preview_faculty.py [output.png]
"""
import math
import sys
from PIL import Image
from build_faculty import FACULTY, TEXTURES, geometry
from puppet_rig import ROOT

SCALE = 6
SIZE = 44  # model pixels per view cell


def rot(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    X = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Y = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Z = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    mul = lambda a, b: [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mul(mul(Z, Y), X)


def apply(m, p):
    return [sum(m[i][k] * p[k] for k in range(3)) for i in range(3)]


def polygons(cube, u, v):
    """ModelPart.Cube: vertices and (u, v) corners in the game's own order."""
    (x, y, z), (w, h, d) = cube['origin'], cube['size']
    i, j, k = x + w, y + h, z + d
    V = [(x, y, z), (i, y, z), (i, j, z), (x, j, z), (x, y, k), (i, y, k), (i, j, k), (x, j, k)]
    f4, f5, f6, f7, f8, f9 = u, u + d, u + d + w, u + d + w + w, u + d + w + d, u + d + w + d + w
    f10, f11, f12 = v, v + d, v + d + h
    faces = [((5, 1, 2, 6), f6, f11, f8, f12), ((0, 4, 7, 3), f4, f11, f5, f12),
             ((5, 4, 0, 1), f5, f10, f6, f11), ((2, 3, 7, 6), f6, f11, f7, f10),
             ((1, 0, 3, 2), f5, f11, f6, f12), ((4, 5, 6, 7), f8, f11, f9, f12)]
    for idx, u1, v1, u2, v2 in faces:
        yield [V[n] for n in idx], [(u2, v1), (u1, v1), (u1, v2), (u2, v2)]


def transforms(parts, pose):
    by_name = {p['name']: p for p in parts}
    cache = {}

    def world(name):
        if name in cache:
            return cache[name]
        part = by_name[name]
        override = pose.get(name, {})
        pivot = [a + b for a, b in zip(part['pivot'], override.get('move', (0, 0, 0)))]
        rotation = override.get('rot', tuple(math.radians(n) for n in part['rotation']))
        m = rot(*rotation)
        if part['parent'] in by_name:
            pm, pt = world(part['parent'])
            result = ([[sum(pm[a][c] * m[c][b] for c in range(3)) for b in range(3)] for a in range(3)],
                      [pt[a] + apply(pm, pivot)[a] for a in range(3)])
        else:
            result = (m, pivot)
        cache[name] = result
        return result
    return world


def hidden(parts, pose, name):
    by_name = {p['name']: p for p in parts}
    while name in by_name:
        if pose.get(name, {}).get('visible', True) is False:
            return True
        name = by_name[name]['parent']
    return False


def render(parts, uvs, texture, yaw, pose):
    image = Image.new('RGBA', (SIZE * SCALE, SIZE * SCALE), (36, 32, 38, 255))
    depth = [[1e9] * (SIZE * SCALE) for _ in range(SIZE * SCALE)]
    world = transforms(parts, pose)
    view = rot(0, yaw, 0)
    tex = texture.load()
    index = 0
    for part in parts:
        m, t = world(part['name'])
        for cube in part['cubes']:
            u, v = uvs[index]
            index += 1
            if hidden(parts, pose, part['name']):
                continue
            for corners, uv in polygons(cube, u, v):
                pts = [apply(view, [t[a] + apply(m, c)[a] for a in range(3)]) for c in corners]
                e1 = [pts[1][a] - pts[0][a] for a in range(3)]
                e2 = [pts[2][a] - pts[0][a] for a in range(3)]
                normal = [e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0]]
                length = math.sqrt(sum(n * n for n in normal)) or 1
                light = .55 + .45 * max(0, (-normal[1] * .7 - normal[2] * .5 + normal[0] * .2) / length)
                for tri in ((0, 1, 2), (0, 2, 3)):
                    raster(image, depth, tex, [pts[n] for n in tri], [uv[n] for n in tri], light)
    return image


def raster(image, depth, tex, pts, uvs, light):
    half = SIZE * SCALE / 2
    sx = [half + p[0] * SCALE for p in pts]
    sy = [SCALE * (p[1] + 12) for p in pts]
    sz = [p[2] for p in pts]
    area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0])
    if abs(area) < 1e-9:
        return
    w, h = image.size
    for py in range(max(0, int(min(sy))), min(h, int(max(sy)) + 1)):
        for px in range(max(0, int(min(sx))), min(w, int(max(sx)) + 1)):
            cx, cy = px + .5, py + .5
            a = ((sx[1] - cx) * (sy[2] - cy) - (sx[2] - cx) * (sy[1] - cy)) / area
            b = ((sx[2] - cx) * (sy[0] - cy) - (sx[0] - cx) * (sy[2] - cy)) / area
            c = 1 - a - b
            if a < -1e-6 or b < -1e-6 or c < -1e-6:
                continue
            z = a * sz[0] + b * sz[1] + c * sz[2]
            if z >= depth[py][px]:
                continue
            tu = a * uvs[0][0] + b * uvs[1][0] + c * uvs[2][0]
            tv = a * uvs[0][1] + b * uvs[1][1] + c * uvs[2][1]
            color = tex[min(127, max(0, int(tu))), min(127, max(0, int(tv)))]
            if color[3] == 0:
                continue
            depth[py][px] = z
            image.putpixel((px, py), tuple(int(ch * light) for ch in color[:3]) + (255,))


def act_pose(figure):
    """Mirror the PERFORM branch of each Java animateCostume()."""
    if figure == 'threadkeeper':
        return {'head': {'rot': (.5, 0, 0)}, 'left_arm': {'rot': (-1.05, .35, 0)}, 'right_arm': {'rot': (-.85, -.3, 0)}}
    if figure == 'understudy':
        return {'body': {'rot': (.12, 0, 0)}, 'head': {'move': (0, 0, -.8), 'rot': (.1, 0, 0)},
                'right_arm': {'move': (0, 0, -.6), 'rot': (-2.1, -.35, 0)}, 'left_arm': {'move': (0, 0, -.6)}}
    if figure == 'strongman':
        lift = -2.95
        return {'right_arm': {'rot': (lift, 0, 0)}, 'left_arm': {'rot': (lift, 0, 0)}, 'head': {'rot': (-.3, 0, 0)},
                'barbell': {'move': (0, 2 + math.cos(lift) * 10 - 4, math.sin(lift) * 10 + 10)}}
    if figure == 'beast_tamer':
        pose = {'right_arm': {'rot': (-2.6, 0, 0)}, 'left_arm': {'rot': (-.5, 0, -.25)}, 'whip_coil': {'visible': False}}
        for i in range(3):
            pose['whip_lash_' + str(i)] = {'rot': (.35 + math.sin(-i * .9) * .55, 0, 0)}
        return pose
    return {}


def rest_pose(figure):
    if figure == 'understudy':
        return {'body': {'rot': (.12, 0, 0)}, 'head': {'move': (0, 0, -.8)}, 'right_arm': {'move': (0, 0, -.6)},
                'left_arm': {'move': (0, 0, -.6)}, 'half_mask': {'move': (0, -3.5, 0), 'rot': (-.35, 0, 0)}}
    if figure == 'strongman':
        return {'barbell': {'visible': False}, 'right_arm': {'rot': (0, 0, .12)}, 'left_arm': {'rot': (0, 0, -.12)}}
    if figure == 'beast_tamer':
        return {'whip_handle': {'visible': False}}
    return {}


def main():
    output = sys.argv[1] if len(sys.argv) > 1 else str(ROOT / 'build/faculty-preview.png')
    views = [0, math.radians(35), math.radians(90), math.pi]
    cell = SIZE * SCALE
    sheet = Image.new('RGBA', (cell * (len(views) + 2), cell * len(FACULTY) * 2), (24, 20, 26, 255))
    for row, spec in enumerate(FACULTY):
        parts = spec['parts']()
        _, materials = geometry(parts)
        uvs = list(materials.keys())
        for variant in (0, 1):
            texture = Image.open(TEXTURES / f'circus_{spec["id"]}_{variant}.png').convert('RGBA')
            y = (row * 2 + variant) * cell
            for col, yaw in enumerate(views):
                sheet.paste(render(parts, uvs, texture, yaw, rest_pose(spec['id'])), (col * cell, y))
            sheet.paste(render(parts, uvs, texture, math.radians(35), act_pose(spec['id'])), (len(views) * cell, y))
            sheet.paste(render(parts, uvs, texture, math.radians(90), act_pose(spec['id'])), ((len(views) + 1) * cell, y))
    sheet.save(output)
    print(output)


if __name__ == '__main__':
    main()
