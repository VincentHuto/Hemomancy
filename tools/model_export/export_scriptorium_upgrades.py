"""Author the two Scriptorium upgrades. Run from any directory with Python 3."""

import base64
import copy
import json
from pathlib import Path
import uuid
import math
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/hemomancy"
MODELS = ASSETS / "models/block"
SOURCE = MODELS / "bbmodel"
GLOW_SOURCE = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/ScriptoriumRuneMesh.java'

# Small authored glyphs, shared by the baked assets and generated emissive mesh.
GLYPHS = ['010/111/010/010/101', '100/110/101/110/100',
          '111/001/010/100/111', '101/101/111/010/010',
          '010/101/010/101/010', '111/100/110/100/111',
          '101/010/111/010/101', '110/010/111/010/011']


def glyph(name, pattern, x, y, z, unit=.5, rotation=None):
    elements = []
    for row, pixels in enumerate(pattern.split('/')):
        col = 0
        while col < len(pixels):
            if pixels[col] == '0':
                col += 1
                continue
            end = col + 1
            while end < len(pixels) and pixels[end] == '1':
                end += 1
            e = cube(f'{name}_{row}_{col}', [x+col*unit, y, z+row*unit],
                     [x+end*unit, y+.25, z+(row+1)*unit], 'crystal')
            e['shade'] = False
            if rotation:
                e['rotation'] = copy.deepcopy(rotation)
            elements.append(e)
            col = end
    return elements


def cube(name, lower, upper, texture="iron"):
    # Model units map directly to texture pixels, avoiding stretched full-tile faces.
    x, y, z = [b - a for a, b in zip(lower, upper)]
    sizes = {"north": (x, y), "south": (x, y), "east": (z, y),
             "west": (z, y), "up": (x, z), "down": (x, z)}
    return {"name": name, "from": lower, "to": upper,
            "faces": {face: {"texture": "#" + texture,
                             "uv": [0, 0, w, h]}
                      for face, (w, h) in sizes.items()}}


def texture_size(resource):
    path = ASSETS / "textures" / (resource.split(":")[1] + ".png")
    with Image.open(path) as image:
        width, height = image.size
    frame_width, frame_height = width, height
    if path.with_suffix(".png.mcmeta").exists():
        animation = json.loads(path.with_suffix(".png.mcmeta").read_text())["animation"]
        frame_width = animation.get("width", min(width, height))
        frame_height = animation.get("height", frame_width)
    return width, height, frame_width, frame_height


def fit_uvs(model):
    """One texture pixel per model unit, including atlas and animation-frame sizes."""
    model["textures"]["log_top"] = "hemomancy:block/blood_wood_log_top"
    model["textures"]["crystal"] = "hemomancy:block/sanguine_glass"
    elements = []
    for original in model["elements"]:
        parts = [copy.deepcopy(original)]
        # A 16px tile cannot cover a 26-unit post without stretching. Repeat it
        # on adjoining segments instead of clamping the UV span to 16.
        for axis in range(3):
            split = []
            for part in parts:
                start, end = part["from"][axis], part["to"][axis]
                if end - start <= 16:
                    split.append(part)
                    continue
                index = 0
                while start < end:
                    segment = copy.deepcopy(part)
                    segment["name"] += f"_tile_{axis}_{index}"
                    segment["from"][axis] = start
                    segment["to"][axis] = min(start + 16, end)
                    # Do not bake faces inside the continuous post.
                    lower, upper = [('west', 'east'), ('down', 'up'), ('north', 'south')][axis]
                    if start > part['from'][axis]:
                        segment['faces'].pop(lower, None)
                    if start + 16 < end:
                        segment['faces'].pop(upper, None)
                    split.append(segment)
                    start += 16
                    index += 1
            parts = split
        for e in parts:
            x, y, z = [b - a for a, b in zip(e["from"], e["to"])]
            sizes = {"north": (x, y), "south": (x, y), "east": (z, y),
                     "west": (z, y), "up": (x, z), "down": (x, z)}
            for direction, face in e["faces"].items():
                if face["texture"] in ("#log", "#log_top") and direction in ("up", "down"):
                    face["texture"] = "#log_top"
                _, _, fw, fh = texture_size(model["textures"][face["texture"][1:]])
                w, h = sizes[direction]
                # The book's sides use its parchment region; covers stay red.
                paper = face["texture"] == '#paper' or (face["texture"] == "#book" and
                    direction not in ("up", "down"))
                u, v = (0, 16) if paper else (0, 0)
                face["uv"] = [u*16/fw, v*16/fh, (u+w)*16/fw, (v+h)*16/fh]
            elements.append(e)
    model["elements"] = elements
    return model


def eightfold(base):
    model = copy.deepcopy(base)
    elements = model["elements"]
    by_name = {e['name']: e for e in elements}
    # A single pedestal and thin desk edging keep the book as the focus.
    by_name['bloodwood_stem'].update({'from': [5, 2.5, 6], 'to': [11, 10.5, 11]})
    by_name['lower_collar'].update({'from': [4.5, 3, 5.5], 'to': [11.5, 3.75, 11.5]})
    by_name['upper_collar'].update({'from': [4.5, 9.5, 5.5], 'to': [11.5, 10.25, 11.5]})
    for face in by_name['front_book_rest']['faces'].values():
        face['texture'] = '#iron'
    for side, x in [('left', 1), ('right', 14.5)]:
        trim = cube(side + '_desk_edge', [x, 12, 3], [x+.5, 12.5, 13])
        trim['rotation'] = copy.deepcopy(by_name['sloped_reading_desk']['rotation'])
        elements.append(trim)
    for i, (x, z) in enumerate([(-3, 7), (0, -2), (7, -3), (16, -2),
                                 (18, 7), (16, 16), (7, 18), (-2, 16)]):
        elements += glyph(f'ground_rune_{i}', GLYPHS[i], x, 0, z)
    # A short rubric on the sloped desk creates the book's luminous text base.
    for i in range(6):
        elements += glyph(f'book_script_{i}', GLYPHS[i], 2.25+i*2, 12.25, 3.25,
                          unit=.25, rotation=by_name['sloped_reading_desk']['rotation'])
    model["display"] = {"gui": {"rotation": [30, 225, 0], "translation": [0, -3, 0],
                                "scale": [.48, .48, .48]}}
    return fit_uvs(model)


def palimpsest(first):
    model = copy.deepcopy(first)
    elements = model["elements"]
    by_name = {e['name']: e for e in elements}
    model['textures']['paper'] = 'hemomancy:entity/liber_sanguinum'
    for i, x in enumerate([2, 5.25, 8.5, 11.75]):
        tag = cube(f'parchment_tag_{i}', [x, 8, 1.5], [x+2, 12, 1.75], 'paper')
        tag['rotation'] = copy.deepcopy(by_name['sloped_reading_desk']['rotation'])
        elements.append(tag)
    for i, z in enumerate([4.5, 7.5, 10.5]):
        rim = 10.5 + i*1.25
        elements += [
            cube(f'burin_{i}_hook', [14.5, rim-.25, z], [16, rim+.25, z+.75]),
            cube(f'burin_{i}_handle', [15.25, rim-2.25, z], [16.25, rim-.25, z+1], 'log'),
            cube(f'burin_{i}_shaft', [15.5, rim-5.25, z+.25], [16, rim-2.25, z+.75]),
            cube(f'burin_{i}_tip', [15.5, rim-5.75, z+.25], [16, rim-5.25, z+.75]),
        ]
    return fit_uvs(model)


def blockbench(name, model):
    textures = []
    indices = {}
    for key, resource in model["textures"].items():
        if key == "particle":
            continue
        texture_path = ASSETS / "textures" / (resource.split(":")[1] + ".png")
        indices[key] = len(textures)
        width, height, _, _ = texture_size(resource)
        textures.append({"name": texture_path.name, "id": key,
                         "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, resource + "/" + key)),
                         "uv_width": width, "uv_height": height,
                         "source": "data:image/png;base64," + base64.b64encode(texture_path.read_bytes()).decode(),
                         "mode": "bitmap", "namespace": "hemomancy",
                         "folder": str(texture_path.parent.relative_to(ASSETS / "textures")).replace("\\", "/")})
    elements = []
    for i, original in enumerate(model["elements"]):
        e = copy.deepcopy(original)
        e["uuid"] = str(uuid.uuid5(uuid.NAMESPACE_URL, name + "/" + str(i)))
        e["type"] = "cube"
        e["box_uv"] = False
        rotation = e.pop("rotation", None)
        e["origin"] = rotation["origin"] if rotation else [8, 8, 8]
        if rotation:
            e["rotation"] = [rotation["angle"] if a == rotation["axis"] else 0 for a in "xyz"]
        for face in e["faces"].values():
            key = face["texture"][1:]
            _, _, fw, fh = texture_size(model["textures"][key])
            face["uv"] = [v * (fw if i % 2 == 0 else fh) / 16
                          for i, v in enumerate(face["uv"])]
            face["texture"] = indices[key]
        elements.append(e)
    return {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
            "name": name, "ambientocclusion": model["ambientocclusion"],
            "resolution": {"width": 16, "height": 16}, "elements": elements,
            "outliner": [e["uuid"] for e in elements], "textures": textures,
            "display": model.get("display", {})}


def main():
    original_base = json.loads((MODELS / "enzymatic_scriptorium.json").read_text())
    base = fit_uvs(copy.deepcopy(original_base))
    # The base pass changes material sampling only, preserving all existing boxes.
    assert len(base['elements']) == len(original_base['elements'])
    for old, new in zip(original_base['elements'], base['elements']):
        for field in ('name', 'from', 'to', 'rotation'):
            assert old.get(field) == new.get(field)
    first = eightfold(base)
    second = palimpsest(first)
    GLOW_SOURCE.write_text(glow_source(first))
    SOURCE.mkdir(exist_ok=True)
    for name, model in [("enzymatic_scriptorium", base),
                        ("eightfold_scriptorium", first), ("palimpsest_scriptorium", second)]:
        (MODELS / (name + ".json")).write_text(json.dumps(model, indent=2) + "\n")
        (SOURCE / (name + ".bbmodel")).write_text(json.dumps(blockbench(name, model), indent=2) + "\n")
        print(f"Exported {name}: {len(model['elements'])} cubes")
    # The inventory model has a static book in addition to the same lectern body.
    item_path = ASSETS / 'models/item/enzymatic_scriptorium.json'
    item = json.loads(item_path.read_text())
    book = [e for e in item['elements'] if e['name'] in ('red_liber_cover', 'liber_pages')]
    item['textures'] = copy.deepcopy(base['textures'])
    item['elements'] = copy.deepcopy(base['elements']) + book
    item_path.write_text(json.dumps(item, indent=2) + '\n')
    # Keep the old internal resource name usable by saved model references/resource packs.
    (MODELS / "monolithic_scriptorium.json").write_text(
        json.dumps({"parent": "hemomancy:block/palimpsest_scriptorium"}, indent=2) + "\n")


def glow_source(model):
    quads = []
    for e in model['elements']:
        if not e['name'].startswith(('ground_rune_', 'book_script_')):
            continue
        x0, _, z0 = e['from']; x1, y, z1 = e['to']
        vertices = [[x0, y+.03125, z0], [x1, y+.03125, z0],
                    [x1, y+.03125, z1], [x0, y+.03125, z1]]
        if 'rotation' in e:
            rot = e['rotation']; ox, oy, oz = rot['origin']
            angle = math.radians(rot['angle'])
            for v in vertices:
                dy, dz = v[1]-oy, v[2]-oz
                v[1] = oy + dy*math.cos(angle)-dz*math.sin(angle)
                v[2] = oz + dy*math.sin(angle)+dz*math.cos(angle)
        quads.append('        {' + ', '.join(f'{v/16:.7f}F' for vertex in vertices for v in vertex) + '}')
    return ('package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;\n\n'
            '// Generated by tools/model_export/export_scriptorium_upgrades.py.\n'
            'final class ScriptoriumRuneMesh {\n'
            '    static final float[][] QUADS = {\n' + ',\n'.join(quads) + '\n    };\n'
            '    private ScriptoriumRuneMesh() {}\n}\n')


if __name__ == "__main__":
    main()
