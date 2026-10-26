"""Authored Java item geometry, matching Blockbench source, and a native 8px material atlas."""
import base64
import copy
import json
from pathlib import Path
import struct
import uuid
import zlib

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODEL = ASSETS / 'models/item/terrestrial_speculum.json'
SOURCE = ASSETS / 'models/item/bbmodel/terrestrial_speculum.bbmodel'
TEXTURE = ASSETS / 'textures/item/terrestrial_speculum_materials.png'

# Each 4px quadrant is hand-pixelled: cold iron, aged ivory, blood, and wrapped leather.
PALETTES = [
    ['#24232e', '#383946', '#535565', '#7e8190'],
    ['#665242', '#9e8763', '#c9b58b', '#eee0b5'],
    ['#330e21', '#73162e', '#b52c42', '#ef6b68'],
    ['#171520', '#26212d', '#3c2c39', '#5b3b46'],
]
PIXELS = [
    ['2332', '1221', '1221', '0110'],
    ['3332', '2222', '2221', '1110'],
    ['1221', '2332', '1221', '0110'],
    ['2221', '1110', '2221', '1110'],
]
raw = bytearray()
for y in range(8):
    raw.append(0)
    for x in range(8):
        material = (y // 4) * 2 + x // 4
        color = PALETTES[material][int(PIXELS[material][y % 4][x % 4])]
        raw.extend(bytes.fromhex(color[1:]) + b'\xff')


def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))


png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 8, 8, 8, 6, 0, 0, 0))
png += chunk(b'IDAT', zlib.compress(bytes(raw), 9)) + chunk(b'IEND', b'')
TEXTURE.write_bytes(png)

elements = []
groups = {name: [] for name in ['spike', 'haft', 'lens', 'jaws', 'tendrils']}


def cube(group, name, start, end, material=0, angle=0, pivot=None, uv=None):
    u, v = (material % 2) * 8, (material // 2) * 8
    face = {'uv': uv or [u + .25, v + .25, u + 7.75, v + 7.75], 'texture': '#0'}
    element = {'name': name, 'from': start, 'to': end,
               'faces': {side: copy.deepcopy(face) for side in ['north', 'east', 'south', 'west', 'up', 'down']}}
    if angle:
        element['rotation'] = {'angle': angle, 'axis': 'z', 'origin': pivot}
    elements.append(element)
    groups[group].append(len(elements) - 1)


cube('spike', 'planting_point', [7.5, 0, 7.5], [8.5, 2, 8.5])
cube('spike', 'planting_blade', [7, 2, 7.25], [9, 4.5, 8.75])
cube('spike', 'heel_collar', [6.25, 4, 6.25], [9.75, 5, 9.75], 1)
cube('haft', 'wrapped_handle', [6.75, 5, 6.75], [9.25, 12, 9.25], 3)
cube('haft', 'blood_channel', [7.5, 5, 6.5], [8.5, 12, 6.75], 2)
for index, y in enumerate([5.5, 7.5, 9.5]):
    cube('haft', f'ivory_grip_{index}', [6.5, y, 6.5], [9.5, y + .5, 9.5], 1)
cube('haft', 'hinge_housing', [5.5, 11.5, 6], [10.5, 14, 10])
cube('haft', 'hinge_pin', [7, 12, 5.5], [9, 13.5, 10.5], 1)
cube('haft', 'adjustment_shaft', [10.5, 12.25, 7.5], [12.5, 13.25, 8.5])
cube('haft', 'adjustment_wheel', [12, 11.5, 6.75], [13, 14, 9.25], 1)

# Two parallel instrument jaws cradle the scrying eye and separate into inward hooks.
for side, x in [('left', 3.75), ('right', 10.75)]:
    cube('jaws', f'{side}_jaw', [x, 14, 7], [x + 1.5, 23, 9])
    cube('jaws', f'{side}_ivory_inlay', [x + .5, 15, 6.75], [x + 1, 22.5, 7], 1)
cube('jaws', 'left_shoulder', [4.5, 12.75, 7], [6.5, 15.5, 9], angle=-22.5, pivot=[6, 13, 8])
cube('jaws', 'right_shoulder', [9.5, 12.75, 7], [11.5, 15.5, 9], angle=22.5, pivot=[10, 13, 8])
cube('jaws', 'left_hook', [4, 22.5, 7], [6.5, 24, 9], 1)
cube('jaws', 'right_hook', [9.5, 22.5, 7], [12, 24, 9], 1)
cube('jaws', 'left_hook_tip', [6, 23.5, 7.25], [7, 24.75, 8.75], 1)
cube('jaws', 'right_hook_tip', [9, 23.5, 7.25], [10, 24.75, 8.75], 1)

# A stepped round sight reads clearly from either side, with a slit and a pale glint.
cube('lens', 'sight_bottom', [6, 15, 7.25], [10, 16.25, 8.75], 1)
cube('lens', 'sight_top', [6, 21.75, 7.25], [10, 23, 8.75], 1)
cube('lens', 'sight_left', [4.75, 16.25, 7.25], [6, 21.75, 8.75], 1)
cube('lens', 'sight_right', [10, 16.25, 7.25], [11.25, 21.75, 8.75], 1)
cube('lens', 'blood_lens', [6, 16.25, 7.5], [10, 21.75, 8.5], 2)
cube('lens', 'blood_lens_left', [5.75, 17.25, 7.5], [6, 20.75, 8.5], 2)
cube('lens', 'blood_lens_right', [10, 17.25, 7.5], [10.25, 20.75, 8.5], 2)
cube('lens', 'sighting_slit', [7.5, 17.5, 7.25], [8.5, 21, 8.75], 3)
cube('lens', 'lens_glint_front', [6.5, 20, 7.25], [7.25, 20.75, 7.5], 1, uv=[9, 0, 10, 1])
cube('lens', 'lens_glint_back', [6.5, 20, 8.5], [7.25, 20.75, 8.75], 1, uv=[9, 0, 10, 1])
cube('lens', 'lower_pointer', [7.75, 15.75, 7], [8.25, 17.5, 9], 1)
cube('tendrils', 'root_curl_left', [5.75, 3.5, 7.5], [6.5, 6, 8.25], 2,
     angle=-22.5, pivot=[6.5, 5.5, 8])
cube('tendrils', 'root_curl_right', [9.5, 3.5, 7.5], [10.25, 6, 8.25], 2,
     angle=22.5, pivot=[9.5, 5.5, 8])

display = {
    'thirdperson_righthand': {'rotation': [0, 90, 0], 'translation': [0, -4, 1], 'scale': [.65] * 3},
    'thirdperson_lefthand': {'rotation': [0, -90, 0], 'translation': [0, -4, 1], 'scale': [.65] * 3},
    'firstperson_righthand': {'rotation': [0, -70, 0], 'translation': [1, -4, 1], 'scale': [.72] * 3},
    'firstperson_lefthand': {'rotation': [0, 70, 0], 'translation': [-1, -4, 1], 'scale': [.72] * 3},
    'ground': {'translation': [0, 3, 0], 'scale': [.4] * 3},
    'gui': {'rotation': [15, -25, -15], 'translation': [0, -2, 0], 'scale': [.62] * 3},
    'fixed': {'translation': [0, -5, 0], 'scale': [.7] * 3},
}
model = {'credit': 'Hemomancy - authored Terrestrial Speculum', 'ambientocclusion': True,
         'gui_light': 'front', 'textures': {'0': 'hemomancy:item/terrestrial_speculum_materials',
                                          'particle': 'hemomancy:item/terrestrial_speculum_materials'},
         'elements': elements, 'display': display}
MODEL.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')


def stable_id(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'hemomancy:terrestrial_speculum/' + name))


bb_elements = []
for element in elements:
    bb = copy.deepcopy(element)
    bb['uuid'] = stable_id(bb['name'])
    bb['type'] = 'cube'
    rotation = bb.pop('rotation', None)
    bb['origin'] = rotation['origin'] if rotation else [8, 12, 8]
    if rotation:
        bb['rotation'] = [0, 0, rotation['angle']]
    for face in bb['faces'].values():
        face['texture'] = 0
        face['uv'] = [coordinate / 2 for coordinate in face['uv']]
    bb_elements.append(bb)
bb_groups = [{'name': name, 'uuid': stable_id(name), 'origin': [8, 12, 8], 'rotation': [0, 0, 0]}
             for name in groups]
source = {'meta': {'format_version': '5.0', 'model_format': 'java_block', 'box_uv': False},
          'name': 'terrestrial_speculum', 'java_block_version': '1.9.0', 'ambientocclusion': True,
          'front_gui_light': True, 'resolution': {'width': 8, 'height': 8}, 'elements': bb_elements,
          'groups': bb_groups,
          'outliner': [{'uuid': stable_id(name), 'children': [bb_elements[i]['uuid'] for i in indices]}
                      for name, indices in groups.items()],
          'textures': [{'name': TEXTURE.name, 'relative_path': '../../../textures/item/' + TEXTURE.name,
                        'folder': 'item', 'namespace': 'hemomancy', 'id': '0', 'width': 8, 'height': 8,
                        'uv_width': 8, 'uv_height': 8, 'particle': True, 'render_mode': 'default',
                        'uuid': stable_id('atlas'), 'source': 'data:image/png;base64,' + base64.b64encode(png).decode()}],
          'display': display}
SOURCE.write_text(json.dumps(source, indent=2) + '\n', encoding='utf-8')
print(f'Exported {len(elements)} cuboids, matching Blockbench source, and 8x8 atlas')
