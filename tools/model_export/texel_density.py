"""Measure the texel density of Hemomancy's JSON block and item models.

The rule is 16 texels per block as drawn. For each face, density is the square root of the UV span in texture pixels
(both axes) over the face size in blocks, weighted by face area across the model. The "ideal" figure is what the
same geometry reads at exactly one texel per model unit, with the one-texel minimum per face, so small detail faces
are not counted against a model. A model is flagged when it reads more than 1.25 times its ideal, or under 12.

Parents are resolved through Hemomancy's own models and the vanilla resources jar that ModDevGradle unpacks under
build/moddev/artifacts. Animated textures count one frame. Renderer scale is not applied: Java-drawn models and item
renderers are outside this script.

Usage:
    python tools/model_export/texel_density.py                    # flagged models, worst first
    python tools/model_export/texel_density.py --all              # every model with elements
    python tools/model_export/texel_density.py item/living_staff_leech block/puffball_fungus
    python tools/model_export/texel_density.py --glob "item/living_staff*"
"""
from __future__ import annotations

import argparse
import fnmatch
import json
import math
import struct
import sys
import zipfile
from functools import lru_cache
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets'
FLAG_RATIO = 1.25
FLAG_FLOOR = 12.0


def _vanilla_jar() -> zipfile.ZipFile | None:
    jars = sorted((ROOT / 'build/moddev/artifacts').glob('neoforge-*-client-extra-aka-minecraft-resources.jar'))
    return zipfile.ZipFile(jars[-1]) if jars else None


VANILLA = _vanilla_jar()


def _split(ref: str) -> tuple[str, str]:
    ns, _, path = ref.partition(':') if ':' in ref else ('minecraft', '', ref)
    return ns, path


def _read(ns: str, rel: str) -> bytes | None:
    local = ASSETS / ns / rel
    if local.is_file():
        return local.read_bytes()
    if VANILLA is not None:
        try:
            return VANILLA.read(f'assets/{ns}/{rel}')
        except KeyError:
            return None
    return None


@lru_cache(maxsize=None)
def texture_size(ref: str) -> tuple[int, int] | None:
    """Width and height of one frame of a texture, or None when it cannot be found."""
    ns, path = _split(ref)
    data = _read(ns, f'textures/{path}.png')
    if data is None or data[:8] != b'\x89PNG\r\n\x1a\n':
        return None
    width, height = struct.unpack('>II', data[16:24])
    meta = _read(ns, f'textures/{path}.png.mcmeta')
    if meta is not None:
        try:
            anim = json.loads(meta).get('animation')
        except ValueError:
            anim = None
        if anim is not None:
            fw = anim.get('width', min(width, height))
            fh = anim.get('height', fw if 'width' in anim else min(width, height))
            return fw, fh
    return width, height


@lru_cache(maxsize=None)
def load_model(ref: str) -> dict | None:
    ns, path = _split(ref)
    data = _read(ns, f'models/{path}.json')
    if data is None:
        return None
    try:
        return json.loads(data)
    except ValueError:
        return None


def resolve(ref: str) -> tuple[list, dict]:
    """The model's elements (nearest definition up the parent chain) and its merged texture map."""
    elements = None
    textures: dict = {}
    seen = set()
    while ref and ref not in seen:
        seen.add(ref)
        model = load_model(ref)
        if model is None:
            break
        for key, value in model.get('textures', {}).items():
            textures.setdefault(key, value)
        if elements is None and 'elements' in model:
            elements = model['elements']
        ref = model.get('parent')
    return elements or [], textures


def texture_ref(face: dict, textures: dict) -> str | None:
    ref = face.get('texture', '')
    for _ in range(16):
        if not ref.startswith('#'):
            return ref or None
        ref = textures.get(ref[1:], '')
    return None


FACE_AXES = {'north': (0, 1), 'south': (0, 1), 'east': (2, 1), 'west': (2, 1), 'up': (0, 2), 'down': (0, 2)}


def face_dims(element: dict, direction: str) -> tuple[float, float]:
    a, b = FACE_AXES[direction]
    lo, hi = element['from'], element['to']
    return abs(hi[a] - lo[a]), abs(hi[b] - lo[b])


def default_uv(element: dict, direction: str) -> list[float]:
    """Vanilla's position-derived UV for a face that names none."""
    (x0, y0, z0), (x1, y1, z1) = element['from'], element['to']
    return {
        'down': [x0, 16 - z1, x1, 16 - z0],
        'up': [x0, z0, x1, z1],
        'north': [16 - x1, 16 - y1, 16 - x0, 16 - y0],
        'south': [x0, 16 - y1, x1, 16 - y0],
        'west': [z0, 16 - y1, z1, 16 - y0],
        'east': [16 - z1, 16 - y1, 16 - z0, 16 - y0],
    }[direction]


def measure(ref: str) -> dict | None:
    """Area-weighted measured and ideal density of one model, or None when it has no measurable face."""
    elements, textures = resolve(ref)
    total_area = measured = ideal = 0.0
    missing = set()
    for element in elements:
        for direction, face in element.get('faces', {}).items():
            if direction not in FACE_AXES:
                continue
            fw, fh = face_dims(element, direction)
            area = fw * fh
            if area <= 0:
                continue
            tex = texture_ref(face, textures)
            size = texture_size(tex) if tex else None
            if size is None:
                missing.add(tex or '?')
                continue
            uv = face.get('uv') or default_uv(element, direction)
            du, dv = abs(uv[2] - uv[0]), abs(uv[3] - uv[1])
            if face.get('rotation', 0) in (90, 270):
                fw, fh = fh, fw
            px_u, px_v = du * size[0] / 16.0, dv * size[1] / 16.0
            density = math.sqrt(px_u * px_v / area) * 16.0 if px_u * px_v > 0 else 0.0
            best = math.sqrt(max(fw, 1.0) * max(fh, 1.0) / area) * 16.0
            measured += density * area
            ideal += best * area
            total_area += area
    if total_area == 0:
        return None
    m, i = measured / total_area, ideal / total_area
    return {'model': ref, 'measured': m, 'ideal': i, 'flagged': m > FLAG_RATIO * i or m < FLAG_FLOOR,
            'missing': sorted(missing)}


def all_models() -> list[str]:
    base = ASSETS / 'hemomancy/models'
    return sorted('hemomancy:' + p.relative_to(base).with_suffix('').as_posix() for p in base.rglob('*.json'))


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('models', nargs='*', help='model paths such as item/living_staff_leech')
    parser.add_argument('--glob', action='append', default=[], help='fnmatch pattern over model paths')
    parser.add_argument('--all', action='store_true', help='print every measurable model, not only flagged ones')
    args = parser.parse_args(argv)
    if VANILLA is None:
        print('warning: no vanilla resources jar under build/moddev/artifacts; vanilla parents are skipped',
              file=sys.stderr)
    refs = [m if ':' in m else 'hemomancy:' + m for m in args.models]
    for pattern in args.glob:
        refs += [r for r in all_models() if fnmatch.fnmatch(r.split(':', 1)[1], pattern)]
    explicit = bool(refs)
    refs = refs or all_models()
    rows = [r for r in (measure(ref) for ref in dict.fromkeys(refs)) if r]
    if not (explicit or args.all):
        rows = [r for r in rows if r['flagged']]
    rows.sort(key=lambda r: r['measured'] / r['ideal'], reverse=True)
    print(f'{"model":64s} {"measured":>9s} {"ideal":>7s} {"ratio":>6s}')
    for r in rows:
        mark = ' *' if r['flagged'] else ''
        note = f'  missing: {", ".join(r["missing"])}' if r['missing'] else ''
        print(f'{r["model"]:64s} {r["measured"]:9.1f} {r["ideal"]:7.1f} {r["measured"] / r["ideal"]:6.2f}{mark}{note}')
    print(f'{len(rows)} models, {sum(r["flagged"] for r in rows)} flagged')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
