"""Render the Marionette Crossbar in its idle, working, tugged and lifted poses into one sheet.

The pose maths mirrors CrossbarMotionRules.pose and MarionetteCrossbarModel.pose; columns are three-quarter,
side and top-down views. Usage: python preview_marionette_crossbar.py [output.png]
"""
import math
import sys
from PIL import Image
import preview_faculty as pf
from build_marionette_crossbar import crossbar, STRINGS, SEGMENTS, TEXTURE, ATLAS
import puppet_rig
from puppet_rig import geometry, ROOT


def motion(time, engage, tug, lift):
    walk = math.sin(time * .16)
    return dict(pitch=engage * math.sin(time * .11) * .10 - tug * .55 - lift * .30,
                roll=(1 - engage) * math.sin(time * .05) * .02 + engage * math.sin(time * .07) * .12,
                rise=lift * 3 + tug * 1.2, front=engage * walk * .22, rear=-engage * walk * .32,
                swing=math.sin(time * .05) * .03 + engage * math.sin(time * .2) * .15,
                flick=tug * .7 + max(0, lift) * .3)


def model_pose(m, time, view_pitch):
    pose = {'view': {'rot': (view_pitch, 0, 0)}, 'grip': {'move': (0, -m['rise'], 0)},
            'control': {'rot': (m['pitch'], 0, m['roll'])}, 'front_bar': {'rot': (0, 0, m['front'])},
            'rear_bar': {'rot': (0, 0, m['rear'])}}
    for i, (name, _, _) in enumerate(STRINGS):
        rock = 0 if i == 0 else m['front'] if i < 3 else m['rear']
        for s in range(SEGMENTS):
            lag = math.sin(time * .2 - s * .6 - i * 1.3)
            x = (-m['pitch'] if s == 0 else 0) + m['swing'] * lag - m['flick'] * (.9 if s == 0 else .4)
            z = (-m['roll'] - rock if s == 0 else 0) + m['swing'] * .6 * lag
            pose[f'{name}_{s}'] = {'rot': (x, 0, z)}
    return pose


def main():
    output = sys.argv[1] if len(sys.argv) > 1 else str(ROOT / 'build/marionette-crossbar-preview.png')
    parts = crossbar()
    for part in parts:
        if part['parent'] == 'mesh.getRoot()':
            part['parent'] = 'view'
    parts.insert(0, dict(name='view', parent='mesh.getRoot()', pivot=(0, 0, 0), rotation=(0, 0, 0), cubes=[]))
    previous, puppet_rig.ATLAS = puppet_rig.ATLAS, ATLAS
    try:
        _, materials = geometry(parts)
    finally:
        puppet_rig.ATLAS = previous
    uvs = list(materials.keys())
    texture = Image.open(TEXTURE).convert('RGBA')
    pf.SCALE, pf.SIZE = 12, 30
    states = [('idle', motion(30, 0, 0, 0)), ('working', motion(30, 1, 0, 0)),
              ('attack tug', motion(30, 1, 1, 0)), ('call lift', motion(30, 1, 0, 1)),
              ('call snap', motion(30, 1, 0, -.35))]
    views = [(math.radians(35), -.35), (math.radians(90), 0), (math.radians(20), -1.2)]
    cell = pf.SIZE * pf.SCALE
    sheet = Image.new('RGBA', (cell * len(views), cell * len(states)), (24, 20, 26, 255))
    for row, (_, m) in enumerate(states):
        for col, (yaw, pitch) in enumerate(views):
            sheet.paste(pf.render(parts, uvs, texture, yaw, model_pose(m, 30, pitch)), (col * cell, row * cell))
    sheet.save(output)
    print(output)


if __name__ == '__main__':
    main()
