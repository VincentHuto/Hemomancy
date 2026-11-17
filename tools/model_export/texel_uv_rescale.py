"""Texel batch T1: rescale the UVs of the kitbashed JSON models to 16 texels per block.

Faces drawn with a 16 px tiling block texture get a UV span equal to the face size in model units (capped at the
whole texture). A face that showed the whole texture takes its position-derived offset, as vanilla would give it, so
neighbouring cubes tile; a face that showed a chosen part of the texture keeps the start of that part. Faces drawn
with the 256 px `blood_fill_tiled` sheet get a span of face size over 16. A decal (a picture meant to show whole,
such as a port, a rocket or a mouth) stays whole on a face at least 4 units each way; on a smaller face, where the
picture cannot read anyway, it shows its centre at face size. Faces on a painted model sheet named in SHEETS show
the centre of their painted region at face size, and faces on a sheet named in REGIONS stay centred in their region.
Other painted sheets, and 16 px item textures, are left as authored.

An axis is rewritten only when its span is off its target by more than a quarter. Each element is rewritten the same
way wherever it appears, so a planted staff stays its held model minus the fist.

Usage:
    python tools/model_export/texel_uv_rescale.py            # rewrite the batch T1 models
    python tools/model_export/texel_uv_rescale.py --check    # list what would change, write nothing
"""
from __future__ import annotations

import argparse
import fnmatch
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import texel_density as td  # noqa: E402

MODELS = td.ASSETS / 'hemomancy/models'

BATCH_T1 = [
    'item/living_staff*',
    'item/living_baghnakh', 'item/barbed_blade', 'item/living_crossbow*', 'item/chitinite_mace',
    'block/chitinite_mace', 'item/living_blaster', 'item/living_rifle',
    'block/blood_crystal', 'block/blood_crystal_stage[0-2]',
    'block/puffball_fungus',
    'item/hematic_microscope', 'item/hematic_microscope_viewing_left',
    'block/ghastly_alembic_athanor_growth', 'block/ghastly_alembic_condenser_growth',
    'block/rune_mod_station', 'block/scrying_podium', 'block/unstained_podium', 'block/fungal_podium',
    'block/phlebotomists_cabinet_specimen_*', 'block/phlebotomists_field_case_specimen_*',
    'item/phlebotomists_field_case',
    'block/vacant_effigy_[0-5]', 'block/escharian_scyphus_[1-4]', 'block/resonant_forge_cylinder_wax',
    'block/mortal_display', 'block/rite_fragment_inscription', 'block/semi_sentient_construct',
]

DECALS = {
    'hemomancy:block/leech_mouth', 'hemomancy:block/vat_port', 'hemomancy:block/podium_top',
    'hemomancy:block/crossbow_firework', 'hemomancy:block/jar_bottom_side', 'hemomancy:item/rite_hint',
}
DECAL_MIN = 4.0
SHEETS = {'hemomancy:block/model_ghastly_alembic'}
# Painted 16 px sheets whose faces must stay inside one region: the cylinder atlas (tools/model_export/cylinders.py)
# keeps its recording surface at the top left and its end stamp below it.
REGIONS = {'hemomancy:item/wax_cylinder_recorded': (0, 0, 12, 6), 'hemomancy:item/wax_cylinder': (0, 8, 8, 16)}
FILL = 'hemomancy:block/blood_fill_tiled'
TOLERANCE = 1.25


def _num(value: float) -> float | int:
    value = round(value, 4)
    return int(value) if value == int(value) else value


def _axis(lo: float, hi: float, target: float, derived: float, origin: str, limit: float = 16.0):
    """New (lo, hi) for one UV axis, keeping its direction, or None when the span is already near its target.

    origin is 'derived' (position-derived offset), 'centre' (centre of the current span) or 'start' (its start).
    """
    span = abs(hi - lo)
    if span == 0 or (target / TOLERANCE <= span <= target * TOLERANCE):
        return None
    if origin == 'derived':
        start = derived % limit
    elif origin == 'centre':
        start = (lo + hi - target) / 2
    else:
        start = min(lo, hi)
    start = max(0.0, min(start, limit - target))
    end = start + target
    return (end, start) if hi < lo else (start, end)


def rescale_face(element: dict, direction: str, face: dict, textures: dict) -> bool:
    tex = td.texture_ref(face, textures)
    size = td.texture_size(tex) if tex else None
    fw, fh = td.face_dims(element, direction)
    if size is None or fw * fh <= 0:
        return False
    if tex in DECALS and min(fw, fh) >= DECAL_MIN:
        return False
    if face.get('rotation', 0) in (90, 270):
        fw, fh = fh, fw
    uv = face.get('uv') or td.default_uv(element, direction)
    derived = td.default_uv(element, direction)
    if tex in REGIONS:
        r0, s0, r1, s1 = REGIONS[tex]
        spans = (min(fw, r1 - r0), min(fh, s1 - s0))
        cu, cv = (r0 + r1) / 2, (s0 + s1) / 2
        face['uv'] = [_num(cu - spans[0] / 2), _num(cv - spans[1] / 2), _num(cu + spans[0] / 2), _num(cv + spans[1] / 2)]
        return face['uv'] != uv
    if tex == FILL or tex in SHEETS:
        targets = (min(fw * 16.0 / size[0], 16.0), min(fh * 16.0 / size[1], 16.0))
        origin = 'start' if tex == FILL else 'centre'
    elif size == (16, 16) and tex.split(':')[-1].startswith('block/'):
        targets = (min(fw, 16.0), min(fh, 16.0))
        whole = abs(uv[2] - uv[0]) >= 16 and abs(uv[3] - uv[1]) >= 16
        origin = 'centre' if tex in DECALS else ('derived' if whole else 'start')
    else:
        return False
    u = _axis(uv[0], uv[2], targets[0], min(derived[0], derived[2]), origin)
    v = _axis(uv[1], uv[3], targets[1], min(derived[1], derived[3]), origin)
    if u is None and v is None:
        return False
    new = list(uv)
    if u is not None:
        new[0], new[2] = u
    if v is not None:
        new[1], new[3] = v
    face['uv'] = [_num(x) for x in new]
    return True


def rescale_model(data: dict, ref: str) -> int:
    _, textures = td.resolve(ref)
    changed = 0
    for element in data.get('elements', []):
        for direction, face in element.get('faces', {}).items():
            if direction in td.FACE_AXES and rescale_face(element, direction, face, textures):
                changed += 1
    return changed


def _compact(value) -> str:
    return json.dumps(value, separators=(', ', ': '))


def dump_blockbench(data: dict) -> str:
    """Blockbench's layout: one line per element field and per face, tab indented."""
    lines = ['{']
    keys = list(data)
    for i, key in enumerate(keys):
        tail = ',' if i < len(keys) - 1 else ''
        value = data[key]
        if key == 'elements':
            lines.append('\t"elements": [')
            for j, element in enumerate(value):
                lines.append('\t\t{')
                fields = list(element)
                for k, field in enumerate(fields):
                    comma = ',' if k < len(fields) - 1 else ''
                    if field == 'faces':
                        lines.append('\t\t\t"faces": {')
                        faces = list(element['faces'].items())
                        for m, (direction, face) in enumerate(faces):
                            sep = ',' if m < len(faces) - 1 else ''
                            lines.append(f'\t\t\t\t"{direction}": {_compact(face)}{sep}')
                        lines.append('\t\t\t}' + comma)
                    else:
                        lines.append(f'\t\t\t"{field}": {_compact(element[field])}{comma}')
                lines.append('\t\t}' + (',' if j < len(value) - 1 else ''))
            lines.append('\t]' + tail)
        else:
            body = json.dumps(value, indent='\t', ensure_ascii=False).replace('\n', '\n\t')
            lines.append(f'\t"{key}": {body}{tail}')
    lines.append('}')
    return '\n'.join(lines) + '\n'


def write(path: Path, data: dict, original: str) -> None:
    newline = '\r\n' if '\r\n' in original else '\n'
    if '{"uv"' in original:
        text = dump_blockbench(data)
    else:
        indent = '\t' if '\n\t' in original else (4 if '\n    "' in original else 2)
        text = json.dumps(data, indent=indent, ensure_ascii=False) + '\n'
    path.write_bytes(text.replace('\n', newline).encode('utf-8'))


def targets() -> list[str]:
    names = [p.relative_to(MODELS).with_suffix('').as_posix() for p in MODELS.rglob('*.json')]
    return sorted({n for n in names for pattern in BATCH_T1 if fnmatch.fnmatch(n, pattern)})


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--check', action='store_true', help='report only, write nothing')
    args = parser.parse_args(argv)
    for name in targets():
        path = MODELS / f'{name}.json'
        original = path.read_text(encoding='utf-8')
        data = json.loads(original)
        if 'elements' not in data:
            continue
        ref = f'hemomancy:{name}'
        before = td.measure(ref)
        changed = rescale_model(data, ref)
        if changed and not args.check:
            write(path, data, original)
            td.load_model.cache_clear()
        after = td.measure(ref) if not args.check else None
        figure = f'{before["measured"]:.1f}' + (f' -> {after["measured"]:.1f}' if after else '')
        print(f'{name:56s} {changed:4d} faces  {figure} (ideal {before["ideal"]:.1f})')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
