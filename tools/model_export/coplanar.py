"""Find and separate coplanar overlapping faces (z-fighting) in Java block model elements.

Two faces fight when they point the same way, lie in (nearly) the same plane and overlap. Faces
that would draw identical texels (same opaque texture, same UV mapping, same shading) are left
alone because the fight cannot be seen. So are fights sealed inside a closed opaque element.
Everything else is separated by moving faces along their normals: an element buried inside its
neighbour is pushed proud so it stays visible; otherwise the smaller face is tucked just behind
the larger one.

Elements use the Java model JSON shape ('from', 'to', optional 'rotation', 'faces' with
'texture' and 'uv'). `resolve` mutates 'from'/'to' in place, so callers can pass views whose
lists are shared with their own model objects.

    python coplanar.py model.json [model.json ...]          check each model
    python coplanar.py --together a.json b.json             check models drawn in one space
    --translucent=ns:block/a,ns:block/b                     textures that never hide a fight
"""
import json
import math
import sys

# Side -> (axis index, 0 for the 'from' plane / 1 for the 'to' plane).
SIDES = {'west': (0, 0), 'east': (0, 1), 'down': (1, 0), 'up': (1, 1), 'north': (2, 0), 'south': (2, 1)}
SEPARATION = .03    # model pixels between separated faces: holds in a 24-bit depth buffer to ~40 blocks
TOLERANCE = .029    # faces closer than this still fight
MIN_THICKNESS = .05
BOUNDS = (-16, 32)  # vanilla's element coordinate limits


def _matrix(rotation):
    if not rotation or not rotation.get('angle'):
        return None
    a = math.radians(rotation['angle'])
    c, s = math.cos(a), math.sin(a)
    # Right-handed rotation about the axis, matching FaceBakery.
    return {'x': ((1, 0, 0), (0, c, -s), (0, s, c)),
            'y': ((c, 0, s), (0, 1, 0), (-s, 0, c)),
            'z': ((c, -s, 0), (s, c, 0), (0, 0, 1))}[rotation['axis']]


def _apply(m, v):
    return tuple(sum(m[i][j] * v[j] for j in range(3)) for i in range(3))


def _apply_t(m, v):
    return tuple(sum(m[j][i] * v[j] for j in range(3)) for i in range(3))


def _dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def _cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def _unit(v):
    n = math.sqrt(_dot(v, v))
    return tuple(x / n for x in v)


class _Element:
    def __init__(self, index, data, frozen):
        self.index, self.data, self.frozen = index, data, frozen
        self.m = _matrix(data.get('rotation'))
        self.origin = tuple(data['rotation']['origin']) if self.m else (0, 0, 0)

    def world(self, p):
        if not self.m:
            return tuple(p)
        d = _apply(self.m, [p[k] - self.origin[k] for k in range(3)])
        return tuple(d[k] + self.origin[k] for k in range(3))

    def local(self, p):
        if not self.m:
            return tuple(p)
        d = _apply_t(self.m, [p[k] - self.origin[k] for k in range(3)])
        return tuple(d[k] + self.origin[k] for k in range(3))

    def axis(self, k):
        e = [0, 0, 0]
        e[k] = 1
        return _apply(self.m, e) if self.m else tuple(e)

    def contains(self, other):
        f, t = self.data['from'], self.data['to']
        for corner in other.corners():
            p = self.local(corner)
            if any(p[k] < f[k] - 1e-3 or p[k] > t[k] + 1e-3 for k in range(3)):
                return False
        return True

    def closed(self, translucent):
        """Six opaque faces: anything sealed inside cannot be seen from any angle."""
        faces = self.data['faces']
        return len(faces) == 6 and not any(f['texture'].lstrip('#') in translucent for f in faces.values())

    def seals(self, point):
        f, t = self.data['from'], self.data['to']
        p = self.local(point)
        return all(f[k] + 1e-3 < p[k] < t[k] - 1e-3 for k in range(3))

    def corners(self):
        f, t = self.data['from'], self.data['to']
        return [self.world((x, y, z)) for x in (f[0], t[0]) for y in (f[1], t[1]) for z in (f[2], t[2])]

    def volume(self):
        f, t = self.data['from'], self.data['to']
        return (t[0] - f[0]) * (t[1] - f[1]) * (t[2] - f[2])


class _Face:
    def __init__(self, element, side):
        self.element, self.side = element, side
        self.k, self.high = SIDES[side]
        f, t = element.data['from'], element.data['to']
        sign = 1 if self.high else -1
        self.normal = tuple(sign * c for c in element.axis(self.k))
        coord = (t if self.high else f)[self.k]
        o = [i for i in range(3) if i != self.k]
        corners = []
        for a, b in ((0, 0), (1, 0), (1, 1), (0, 1)):
            p = [0, 0, 0]
            p[self.k] = coord
            p[o[0]] = (f, t)[a][o[0]]
            p[o[1]] = (f, t)[b][o[1]]
            corners.append(element.world(p))
        self.corners = corners
        self.offset = _dot(self.normal, corners[0])
        self.key = tuple(round(c, 3) + 0.0 for c in self.normal)
        spec = element.data['faces'][side]
        self.texture, self.uv = spec.get('texture'), spec.get('uv')
        self.uv_rotation = spec.get('rotation', 0)
        self.shade = element.data.get('shade', True)

    def basis(self):
        n = self.normal
        pick = min(range(3), key=lambda i: abs(n[i]))
        a = [0, 0, 0]
        a[pick] = 1
        u = _unit(_cross(n, a))
        return u, _cross(n, u)

    def polygon(self, u, v):
        poly = [(_dot(p, u), _dot(p, v)) for p in self.corners]
        return poly if _area(poly) > 0 else poly[::-1]

    def texel(self, point):
        """Texture coordinate this face draws at a world point, following vanilla vertex order."""
        p = self.element.local(point)
        f, t = self.element.data['from'], self.element.data['to']

        def frac(k, reverse=False):
            span = t[k] - f[k]
            r = (p[k] - f[k]) / span if span else 0
            return 1 - r if reverse else r

        u0, v0, u1, v1 = self.uv
        su, sv = {'up': (frac(0), frac(2)), 'down': (frac(0), frac(2, True)),
                  'north': (frac(0, True), frac(1, True)), 'south': (frac(0), frac(1, True)),
                  'west': (frac(2), frac(1, True)), 'east': (frac(2, True), frac(1, True))}[self.side]
        return u0 + (u1 - u0) * su, v0 + (v1 - v0) * sv

    def thickness(self):
        f, t = self.element.data['from'], self.element.data['to']
        return t[self.k] - f[self.k]

    def name(self):
        return '%s.%s' % (self.element.data.get('name', '?'), self.side)


def _area(poly):
    return sum(poly[i][0] * poly[i - 1][1] - poly[i - 1][0] * poly[i][1] for i in range(len(poly))) / -2


def _clip(subject, clip):
    """Sutherland-Hodgman intersection of two counter-clockwise convex polygons."""
    out = subject
    for i in range(len(clip)):
        a, b = clip[i - 1], clip[i]
        inside = lambda p: (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]) >= -1e-9
        src, out = out, []
        for j in range(len(src)):
            p, q = src[j - 1], src[j]
            if inside(q):
                if not inside(p):
                    out.append(_intersect(a, b, p, q))
                out.append(q)
            elif inside(p):
                out.append(_intersect(a, b, p, q))
        if not out:
            return []
    return out


def _intersect(a, b, p, q):
    dx, dy = b[0] - a[0], b[1] - a[1]
    ex, ey = q[0] - p[0], q[1] - p[1]
    den = dx * ey - dy * ex
    t = ((p[0] - a[0]) * ey - (p[1] - a[1]) * ex) / den if den else 0
    return a[0] + t * dx, a[1] + t * dy


def _overlap_area(a, b):
    overlap = _clip(a.poly, b.poly)
    return abs(_area(overlap)) if len(overlap) >= 3 else 0


def _identical(fa, fb, overlap, textures, translucent):
    if fa.uv_rotation or fb.uv_rotation or fa.shade != fb.shade:
        return False
    ta, tb = textures.get(fa.texture, fa.texture), textures.get(fb.texture, fb.texture)
    if ta != tb or fa.texture.lstrip('#') in translucent or fb.texture.lstrip('#') in translucent:
        return False
    u, v = fa.plane
    n = fa.normal
    cx = sum(p[0] for p in overlap) / len(overlap)
    cy = sum(p[1] for p in overlap) / len(overlap)
    for dx, dy in ((0, 0), (.25, 0), (0, .25)):
        point = tuple(n[k] * fa.offset + u[k] * (cx + dx) + v[k] * (cy + dy) for k in range(3))
        if any(abs(x - y) > 1e-3 for x, y in zip(fa.texel(point), fb.texel(point))):
            return False
    return True


def _sealed(fa, fb, overlap, wrapped, translucent):
    """True when a closed opaque element hides the overlap at both depths: the faces either lie
    inside it or look straight into its solid across a slit too thin to see through."""
    u, v = fa.plane
    n = fa.normal
    cx = sum(p[0] for p in overlap) / len(overlap)
    cy = sum(p[1] for p in overlap) / len(overlap)
    points = [tuple(n[k] * face.offset + u[k] * x + v[k] * y for k in range(3))
              for face in (fa, fb) for x, y in overlap + [(cx, cy)]]
    ahead = [tuple(p[k] + n[k] * 5 * SEPARATION for k in range(3)) for p in points]
    return any(el is not fa.element and el is not fb.element and el.closed(translucent)
               and all(el.seals(p) or el.seals(q) for p, q in zip(points, ahead)) for el in wrapped)


def find(elements, textures=None, translucent=(), frozen=(), tolerance=TOLERANCE):
    """Return visible fights as (face_a, face_b, overlap_area); faces carry their elements."""
    wrapped = [_Element(i, e, id(e) in frozen) for i, e in enumerate(elements)]
    groups = {}
    for el in wrapped:
        for side in el.data['faces']:
            face = _Face(el, side)
            groups.setdefault(face.key, []).append(face)
    textures = textures or {}
    fights = []
    for faces in groups.values():
        faces.sort(key=lambda f: f.offset)
        plane = faces[0].basis()
        for face in faces:
            face.group, face.plane, face.poly = faces, plane, face.polygon(*plane)
        for i, fa in enumerate(faces):
            for fb in faces[i + 1:]:
                if fb.offset - fa.offset >= tolerance:
                    break
                if fa.element is fb.element:
                    continue
                overlap = _clip(fa.poly, fb.poly)
                if len(overlap) < 3 or abs(_area(overlap)) < 1e-3:
                    continue
                if _identical(fa, fb, overlap, textures, translucent):
                    continue
                if _sealed(fa, fb, overlap, wrapped, translucent):
                    continue
                fights.append((fa, fb, abs(_area(overlap))))
    return fights


def _move(face, target):
    """Move a face's plane to the given offset along its normal; False if the box would break."""
    data = face.element.data
    k = face.k
    coord = data['to'] if face.high else data['from']
    new = round(coord[k] + (target - face.offset) * (1 if face.high else -1), 4)
    lo, hi = (data['from'][k], new) if face.high else (new, data['to'][k])
    if hi - lo < MIN_THICKNESS or not BOUNDS[0] <= new <= BOUNDS[1]:
        return False
    coord[k] = new
    return True


def _clear(face, target, step, planned):
    """Step `target` away until no other overlapping face sits within TOLERANCE of it."""
    neighbours = [g for g in face.group if g.element is not face.element
                  and abs(g.offset - face.offset) < 1 and _overlap_area(face, g) >= 1e-3]
    while any(abs(planned.get(g, g.offset) - target) < TOLERANCE for g in neighbours):
        target += step
    return target


def _layer(faces, edges, planned, translucent, log):
    """Give each face in one cluster of mutual fights its own plane, one SEPARATION apart.

    Order runs from the face tucked deepest to the one drawn on top: faces of elements that are
    not buried in a neighbour come first, smallest to largest, so the broad surface stays visible
    over parts that merely run into it; buried detail comes last, largest to smallest, so inlays
    and trims show through. Faces that do not overlap may share a plane. One face (a frozen one,
    else the largest unburied surface) keeps its position and the rest are stacked around it.
    An unburied face wholly behind a larger closed surface just tucks one step inside that solid,
    where it is sealed from view, instead of taking a plane of its own.
    """
    buried = {f: any(g.element.contains(f.element) for g in edges[f]) for f in faces}
    area = {f: abs(_area(f.poly)) for f in faces}
    rank = sorted(faces, key=lambda f: (-area[f], f.element.index))
    cover = {}
    for i, f in enumerate(rank):
        if buried[f] or f.element.frozen:
            continue
        # Any larger face of the cluster can hide f, including one whose texels happen to match it.
        for g in rank[:i]:
            if (g not in cover and abs(g.offset - f.offset) < TOLERANCE and g.element.closed(translucent)
                    and g.thickness() > 3 * SEPARATION and abs(_overlap_area(f, g) - area[f]) < 1e-3):
                cover[f] = g
                break
    order = sorted((f for f in faces if f not in cover),
                   key=lambda f: (buried[f], -area[f] if buried[f] else area[f], f.element.index))
    level = {}
    for f in order:
        level[f] = 1 + max((level[g] for g in edges[f] if g in level), default=-1)
    fixed = [f for f in faces if f.element.frozen]
    if len(fixed) > 1:
        raise ValueError('frozen faces fight: ' + ', '.join(f.name() for f in fixed))
    if fixed:
        anchor = fixed[0]
    else:
        flush = [f for f in order if not buried[f]]
        anchor = flush[-1] if flush else max(order, key=lambda f: area[f])

    moved = 0
    for f in order:
        if f is anchor:
            continue
        steps = level[f] - level[anchor]
        step = SEPARATION if steps > 0 else -SEPARATION
        options = [_clear(f, anchor.offset + steps * SEPARATION, step, planned),
                   _clear(f, anchor.offset - (steps or -1) * SEPARATION, -step, planned)]
        if abs(options[0] - f.offset) < 1e-6:
            continue
        target = next((t for t in options if _move(f, t)), None)
        if target is None:
            raise ValueError('cannot separate %s from %s' % (f.name(), anchor.name()))
        planned[f] = target
        moved += 1
        if log is not None:
            log.append('%s %+.3f, layered against %s' % (f.name(), target - f.offset, anchor.name()))
    for f, g in cover.items():
        target = planned.get(g, g.offset) - SEPARATION
        if abs(target - f.offset) < 1e-6:
            continue
        if not _move(f, target):
            raise ValueError('cannot tuck %s behind %s' % (f.name(), g.name()))
        planned[f] = target
        moved += 1
        if log is not None:
            log.append('%s %+.3f, tucked behind %s' % (f.name(), target - f.offset, g.name()))
    return moved


def resolve(elements, textures=None, translucent=(), frozen=(), log=None, rounds=20):
    """Separate every visible fight in place. Elements whose id() is in `frozen` never move."""
    moved = 0
    for _ in range(rounds):
        fights = find(elements, textures, translucent, frozen)
        if not fights:
            return moved
        edges = {}
        for fa, fb, _overlap in fights:
            edges.setdefault(fa, set()).add(fb)
            edges.setdefault(fb, set()).add(fa)
        seen, planned = set(), {}
        for start in edges:
            if start in seen:
                continue
            cluster, stack = [], [start]
            seen.add(start)
            while stack:
                f = stack.pop()
                cluster.append(f)
                for g in edges[f] - seen:
                    seen.add(g)
                    stack.append(g)
            moved += _layer(cluster, edges, planned, translucent, log)
    raise ValueError('fights remain after %d rounds' % rounds)


def describe(fights):
    return ['%s ~ %s (%.2f px2, gap %.3f)' % (fa.name(), fb.name(), area, abs(fb.offset - fa.offset))
            for fa, fb, area in fights]


def _load(path):
    model = json.load(open(path, encoding='utf-8'))
    textures = {'#' + k: v for k, v in model.get('textures', {}).items()}
    elements = model.get('elements', [])
    for e in elements:
        for face in e['faces'].values():
            face['texture'] = textures.get(face['texture'], face['texture'])
    return elements


if __name__ == '__main__':
    args = sys.argv[1:]
    together = '--together' in args
    translucent = {t for a in args if a.startswith('--translucent=') for t in a.split('=', 1)[1].split(',')}
    paths = [a for a in args if not a.startswith('--')]
    sets = [sum((_load(p) for p in paths), [])] if together else [_load(p) for p in paths]
    total = 0
    for label, elements in zip(['combined'] if together else paths, sets):
        lines = describe(find(elements, translucent=translucent))
        total += len(lines)
        print('%s: %d fights' % (label, len(lines)))
        for line in lines:
            print('  ' + line)
    sys.exit(1 if total else 0)
