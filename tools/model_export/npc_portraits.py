"""Dialogue portraits for the Harbinger, Unstained and Circus NPCs, in the Brined Court portrait style.

The portrait is the `<texture>_portrait.png` companion DialogueScreen resolves next to the speaker texture (48 x 48):
a render of the NPC's own model, head and shoulders, three-quarter view, over a backdrop for its side. Unlike the Court
roles these models are hand-authored Java layer definitions, so the script reads `createBodyLayer()` straight from the
model source and renders it with the model's own texture.

    python tools/model_export/npc_portraits.py                 # every NPC below
    python tools/model_export/npc_portraits.py harbinger_vicar # one NPC
"""
import math
import re
import sys

import numpy as np
from PIL import Image

from annetta_kit import ROOT
from unstained_weapons import face_rects, local_point, render_quads, rot_matrix

MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
NPC = ROOT / 'src/main/resources/assets/hemomancy/textures/entity/npc'
BACKGROUND = (30, 32, 40)

# Backdrops, surface colour at the top and depth at the bottom, plus the colour of the light shaft.
SIDES = {
    'harbinger': dict(light=(255, 196, 150)),
    'unstained': dict(light=(226, 236, 240)),
    'circus': dict(light=(255, 214, 140)),
}

# id -> (model class, texture path under textures/entity/npc, side, backdrop (top, bottom), parts to hide)
NPCS = {
    'harbinger_vicar': ('HarbingerVicarModel', 'harbinger/harbinger_vicar/harbinger_vicar.png', 'harbinger',
                        ((104, 26, 30), (20, 6, 8)), ()),
    'harbinger_alchemist': ('HarbingerAlchemistModel', 'harbinger/harbinger_alchemist/harbinger_alchemist.png',
                            'harbinger', ((112, 64, 30), (24, 12, 8)), ()),
    'harbinger_mnemonist': ('HarbingerMnemonistModel', 'harbinger/harbinger_mnemonist/harbinger_mnemonist.png',
                            'harbinger', ((76, 38, 78), (16, 8, 20)), ()),
    'harbinger_cicatrix_anchorite': ('HarbingerCicatrixAnchoriteModel',
                                     'harbinger/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite.png',
                                     'harbinger', ((120, 34, 40), (26, 4, 8)), ()),
    'harbinger_voyager': ('HarbingerVoyagerModel', 'harbinger/harbinger_voyager/harbinger_voyager.png', 'harbinger',
                          ((84, 40, 44), (14, 14, 22)), ()),
    'harbinger_artificer': ('HarbingerArtificerModel', 'harbinger/harbinger_artificer/harbinger_artificer.png',
                            'harbinger', ((78, 48, 46), (16, 12, 14)), ()),
    'harbinger_hermit': ('HarbingerHermitModel', 'harbinger/harbinger_hermit/harbinger_hermit.png', 'harbinger',
                         ((70, 50, 36), (14, 12, 8)), ()),
    'harbinger_votary_wayfarer': ('HarbingerVotaryWayfarerModel',
                                  'harbinger/harbinger_votary_wayfarer/harbinger_votary_wayfarer.png', 'harbinger',
                                  ((100, 52, 34), (22, 10, 8)), ()),
    'unstained_acolyte': ('UnstainedAcolyteModel', 'unstained/unstained_acolyte/unstained_acolyte.png', 'unstained',
                          ((150, 170, 176), (40, 52, 60)), ()),
    'unstained_zealot': ('UnstainedZealotModel', 'unstained/unstained_zealot/unstained_zealot.png', 'unstained',
                         ((120, 140, 150), (28, 36, 46)), ()),
    'unstained_guardian': ('UnstainedGuardianModel', 'unstained/unstained_guardian/unstained_guardian.png',
                           'unstained', ((104, 124, 132), (22, 30, 38)), ()),
    'unstained_scout': ('UnstainedScoutModel', 'unstained/unstained_scout/unstained_scout.png', 'unstained',
                        ((132, 156, 154), (30, 44, 46)), ()),
    'ringmaster': ('CircusRingmasterModel', 'harbinger/circus/ringmaster.png', 'circus',
                   ((96, 20, 34), (18, 6, 12)), ()),
    'acrobat': ('CircusAcrobatModel', 'harbinger/circus/acrobat_0.png', 'circus',
                ((70, 28, 70), (14, 6, 18)), ()),
    'fire_eater': ('CircusFireEaterModel', 'harbinger/circus/fire_eater_0.png', 'circus',
                   ((110, 44, 22), (22, 8, 6)), ()),
    'knife_thrower': ('CircusKnifeThrowerModel', 'harbinger/circus/knife_thrower_0.png', 'circus',
                      ((60, 30, 52), (12, 6, 14)), ()),
    'stilt_walker': ('CircusStiltWalkerModel', 'harbinger/circus/stilt_walker_0.png', 'circus',
                     ((40, 30, 70), (8, 6, 18)), ()),
    'circus_beast_tamer': ('CircusBeastTamerModel', 'harbinger/circus/circus_beast_tamer_0.png', 'circus',
                           ((84, 52, 24), (18, 10, 6)), ()),
    'circus_strongman': ('CircusStrongmanModel', 'harbinger/circus/circus_strongman_0.png', 'circus',
                         ((100, 28, 28), (20, 6, 6)), ('barbell',)),
    'circus_threadkeeper': ('CircusThreadkeeperModel', 'harbinger/circus/circus_threadkeeper_0.png', 'circus',
                            ((60, 24, 44), (12, 4, 10)), ()),
    'circus_understudy': ('CircusUnderstudyModel', 'harbinger/circus/circus_understudy_0.png', 'circus',
                          ((76, 34, 48), (14, 6, 12)), ()),
}

# Models whose crown or hat sits further above the head pivot frame this much further up (model px).
LIFT = {'ringmaster': 3}


def number(text):
    text = text.strip().replace('(float)', '').replace('Math.PI', str(math.pi)).replace('Mth.PI', str(math.pi))
    text = re.sub(r'(?<=[0-9.])[Ff]\b', '', text)
    return float(eval(text, {'__builtins__': {}}, {}))


def args(text, start):
    """The argument text of the call whose '(' is at start, and the index after its ')'."""
    depth = 0
    for i in range(start, len(text)):
        if text[i] == '(':
            depth += 1
        elif text[i] == ')':
            depth -= 1
            if depth == 0:
                return text[start + 1:i], i + 1
    raise ValueError('unbalanced call')


def split_top(text):
    parts, depth, current = [], 0, ''
    for ch in text:
        if ch in '([':
            depth += 1
        elif ch in ')]':
            depth -= 1
        if ch == ',' and depth == 0:
            parts.append(current)
            current = ''
        else:
            current += ch
    parts.append(current)
    return [p.strip() for p in parts]


def parse_cubes(builder):
    cubes, u, v, mirror = [], 0, 0, False
    for m in re.finditer(r'\.(texOffs|mirror|addBox)\(', builder):
        body, _ = args(builder, m.end() - 1)
        if m.group(1) == 'texOffs':
            u, v = (number(a) for a in split_top(body))
        elif m.group(1) == 'mirror':
            mirror = body.strip() != 'false'
        else:
            a = split_top(body)
            if a[0].startswith('"'):
                a = a[1:]
            box = [number(x) for x in a[:6]]
            grow = (0.0, 0.0, 0.0)
            if len(a) > 6 and 'CubeDeformation' in a[6]:
                g = [number(x) for x in split_top(a[6][a[6].index('(') + 1:a[6].rindex(')')])]
                grow = tuple(g) if len(g) == 3 else (g[0],) * 3
            elif len(a) > 6 and a[6] in ('true', 'false'):
                mirror = a[6] == 'true'
            cubes.append(dict(box=box, uv=(u, v), mirror=mirror, grow=grow))
    return cubes


def parse_pose(text):
    text = text.strip()
    if text.endswith('ZERO'):
        return (0, 0, 0), (0, 0, 0)
    kind = re.match(r'PartPose\.(\w+)\(', text).group(1)
    values = [number(x) for x in split_top(args(text, text.index('('))[0])]
    if kind == 'offset':
        return tuple(values), (0, 0, 0)
    if kind == 'rotation':
        return (0, 0, 0), tuple(math.degrees(x) for x in values)
    return tuple(values[:3]), tuple(math.degrees(x) for x in values[3:6])


def parse_model(java_class):
    source = (MODELS / f'{java_class}.java').read_text(encoding='utf-8')
    source = source[source.index('createBodyLayer'):]
    variables, parts = {}, [dict(name='root', parent=None, pivot=(0, 0, 0), rotation=(0, 0, 0), cubes=[])]
    for m in re.finditer(r'(?:PartDefinition\s+(\w+)\s*=\s*)?([\w.()]+?)\.addOrReplaceChild\(', source):
        assigned, parent_expr = m.group(1), m.group(2)
        body, _ = args(source, m.end() - 1)
        a = split_top(body)
        name = a[0].strip('"')
        pose_index = next(i for i, x in enumerate(a) if x.startswith('PartPose'))
        builder = ','.join(a[1:pose_index])
        parent = variables.get(parent_expr, 'root')
        pivot, rotation = parse_pose(a[pose_index])
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=parse_cubes(builder)))
        if assigned:
            variables[assigned] = name
    return parts


def transforms(parts):
    out = {}
    for part in parts:
        m = np.eye(4)
        m[:3, :3] = rot_matrix(part['rotation'])
        m[:3, 3] = part['pivot']
        out[part['name']] = out.get(part['parent'], np.eye(4)) @ m
    return out


def model_quads(parts, hidden):
    parents = {p['name']: p['parent'] for p in parts}
    m_all = transforms(parts)

    def skip(name):
        while name is not None:
            if name in hidden:
                return True
            name = parents[name]
        return False

    result = []
    for part in parts:
        if skip(part['name']):
            continue
        m = m_all[part['name']]
        for cube in part['cubes']:
            x, y, z, w, hgt, d = cube['box']
            gx, gy, gz = cube['grow']
            box = (x - gx, y - gy, z - gz, w + 2 * gx, hgt + 2 * gy, d + 2 * gz)
            rects = face_rects(*cube['uv'], w, hgt, d)
            if cube['mirror']:
                rects['west'], rects['east'] = rects['east'], rects['west']
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                rect = rects[face]
                corners, uvs = [], []
                for fu, fv in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    corners.append(local_point(face, box, fu, fv))
                    tu = 1 - fu if cube['mirror'] else fu
                    uvs.append((rect[0] + (rect[2] - rect[0]) * tu, rect[1] + (rect[3] - rect[1]) * fv))
                pts = np.array([list(c) + [1] for c in corners]) @ m.T
                result.append((pts[:, :3], np.array(uvs, float)))
    return result


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def portrait(npc):
    java_class, texture, side, (top, bottom), hidden = NPCS[npc]
    parts = parse_model(java_class)
    head = transforms(parts).get('head')
    y = head[1, 3] if head is not None else 0.0
    atlas = Image.open(NPC / texture).convert('RGBA')
    lift = LIFT.get(npc, 0)
    image = render_quads(model_quads(parts, hidden), atlas, -0.4, 0.06, 480,
                         focus=(y - 12.5 - lift, y + 6.5 - lift))
    light = SIDES[side]['light']
    out = Image.new('RGB', image.size)
    px, src = out.load(), image.load()
    w, h = image.size
    for j in range(h):
        backdrop = mix(top, bottom, j / (h - 1))
        for i in range(w):
            c = src[i, j]
            if c == BACKGROUND:
                # A soft shaft of light from above, brighter near the figure's crown.
                shaft = max(0.0, 1 - abs(i - w * 0.42) / (w * 0.5)) * (1 - j / h) * 0.25
                c = mix(backdrop, light, shaft)
            px[i, j] = c
    small = out.resize((48, 48), Image.BOX)
    # A darkened rim so the portrait frame's edge reads.
    sp = small.load()
    for j in range(48):
        for i in range(48):
            d = min(i, j, 47 - i, 47 - j)
            if d < 3:
                sp[i, j] = mix(sp[i, j], (6, 8, 12), 0.35 * (3 - d) / 3)
    target = NPC / texture.replace('.png', '_portrait.png')
    small.convert('RGBA').save(target)
    return target


def main():
    for npc in sys.argv[1:] or list(NPCS):
        print(f'{npc}: {portrait(npc).relative_to(ROOT)}')


if __name__ == '__main__':
    main()
