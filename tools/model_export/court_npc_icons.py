"""Dialogue portraits and profession crests for every built Brined Court role.

The portrait is the `<role>_portrait.png` companion DialogueScreen resolves next to the role's texture (48 x 48), a
render of the role's own model, head and shoulders, over its house's water. The crest is the 32 x 32 sprite
`dialogue/crests/court_<role>` the dialogue header draws at its right edge (the hub's style id is the NPC id), on the
same shield the Harbinger and Unstained crests use, in the house's colours with a role glyph.

    python tools/model_export/court_npc_icons.py              # every role below
    python tools/model_export/court_npc_icons.py gardener     # one role
"""
import importlib
import math
import sys

from PIL import Image

import annetta_kit
from annetta_kit import ROOT, layout, paint_atlases, quads
from court_humanoid import ATLAS_WIDTH, PALETTES, finish

TEXTURES = ROOT / 'src/main/resources/assets/hemomancy/textures'
PORTRAIT_DIR = TEXTURES / 'entity/npc/court'
CREST_DIR = TEXTURES / 'gui/sprites/dialogue/crests'
BACKGROUND = (30, 32, 40)

# role -> (house, glyph)
ROLES = {
    'gardener': ('giving_light', 'sun'),
    'arbor': ('giving_light', 'frond'),
    'lightkeeper': ('giving_light', 'lamp'),
    'tidewarden': ('holding_stone', 'wave'),
    'braced_elder': ('holding_stone', 'roots'),
    'edge_swimmer': ('shared_wake', 'lance'),
    'nursery_tender': ('shared_wake', 'nest'),
    'linesman': ('woven_current', 'line'),
    'lamp_colony': ('woven_current', 'chain'),
    'host': ('veiled_fold', 'door'),
    'veiled_resident': ('veiled_fold', 'veil'),
    'bellwarden': ('still_bell', 'bell'),
    'sleeper': ('still_bell', 'sealed'),
    'marrow_tender': ('last_marrow', 'hook'),
    'rooted_elder': ('last_marrow', 'plume'),
    'offering_bearer': ('kindling', 'bowl'),
    'kindled_elder': ('kindling', 'flame'),
    'head_tender': ('kindling', 'crown'),
}

# Roles whose crown sits higher than the rig's head pivot frame this much further up (model px).
LIFT = {'edge_swimmer': 4}

# The water behind each house's portraits: surface colour at the top, depth at the bottom.
WATER = {
    'giving_light': ((86, 150, 140), (24, 58, 66)),
    'holding_stone': ((70, 104, 112), (22, 36, 44)),
    'shared_wake': ((44, 84, 136), (10, 20, 46)),
    'woven_current': ((40, 44, 84), (8, 8, 24)),
    'veiled_fold': ((22, 24, 34), (4, 4, 8)),
    'still_bell': ((44, 58, 76), (10, 14, 22)),
    'last_marrow': ((30, 26, 34), (6, 4, 8)),
    'kindling': ((60, 30, 30), (12, 8, 10)),
}

# Glyphs: 12 wide, 14 tall. '#' cream, '+' house accent.
GLYPHS = {
    'sun': [
        '.....##.....',
        '..#..##..#..',
        '...#....#...',
        '....####....',
        '...#++++#...',
        '##.#++++#.##',
        '##.#++++#.##',
        '...#++++#...',
        '....####....',
        '...#....#...',
        '..#..##..#..',
        '.....##.....',
        '............',
        '............',
    ],
    'frond': [
        '.....##.....',
        '....#++#....',
        '.#..#++#..#.',
        '.##..##..##.',
        '..##.##.##..',
        '...######...',
        '.#...##...#.',
        '.##..##..##.',
        '..##.##.##..',
        '...######...',
        '.....##.....',
        '.....##.....',
        '....####....',
        '...##..##...',
    ],
    'lamp': [
        '.....##.....',
        '.....##.....',
        '...######...',
        '..#......#..',
        '..#.++++.#..',
        '..#+++++++#.',
        '..#.++++.#..',
        '..#.++++.#..',
        '..#..++..#..',
        '..#......#..',
        '...######...',
        '....####....',
        '.....##.....',
        '............',
    ],
    'wave': [
        '............',
        '....###.....',
        '..##...##...',
        '.#...##..#..',
        '.#..#..#.#..',
        '.....##..#..',
        '........#...',
        '.......#....',
        '............',
        '..++++++++..',
        '.++++++++++.',
        '+++++##+++++',
        '++++####++++',
        '++++++++++++',
    ],
    'roots': [
        '............',
        '...++++++...',
        '..++++++++..',
        '.++++++++++.',
        '.++++++++++.',
        '..++++++++..',
        '...#.##.#...',
        '..#..##..#..',
        '.#..#..#..#.',
        '.#..#..#..#.',
        '#..#....#..#',
        '#..#....#..#',
        '...#....#...',
        '............',
    ],
    'lance': [
        '..........##',
        '.........###',
        '........###.',
        '.......#+#..',
        '......#+#...',
        '.....#+#....',
        '....#+#.....',
        '...#+#......',
        '..###.......',
        '.#.#........',
        '#.#.........',
        '.#..........',
        '............',
        '............',
    ],
    'nest': [
        '............',
        '............',
        '...++..++...',
        '..++++++++..',
        '..++++++++..',
        '#..++..++..#',
        '##........##',
        '.##......##.',
        '.###....###.',
        '..########..',
        '...######...',
        '.....##.....',
        '....####....',
        '............',
    ],
    'line': [
        '+...........',
        '.+..........',
        '..+.........',
        '..##........',
        '.####.......',
        '..##+.......',
        '.....+......',
        '......+.....',
        '.......##...',
        '......####..',
        '.......##+..',
        '..........+.',
        '...........+',
        '............',
    ],
    'chain': [
        '+..........+',
        '.+........+.',
        '..+......+..',
        '...##..##...',
        '..####.###..',
        '...##..##...',
        '....+..+....',
        '....##.+....',
        '...####+....',
        '....####....',
        '.....##.....',
        '.....+......',
        '....###.....',
        '.....#......',
    ],
    'door': [
        '...######...',
        '..#......#..',
        '.#..++++..#.',
        '.#.+.+..+.#.',
        '.#.+.+..+.#.',
        '.#.++++++.#.',
        '.#.+..+.+.#.',
        '.#.+..+.+.#.',
        '.#.++++++.#.',
        '.#.+.+..+.#.',
        '.#.+.+..+.#.',
        '.#.++++++.#.',
        '.#........#.',
        '############',
    ],
    'veil': [
        '....####....',
        '...######...',
        '..###..###..',
        '..##.##.##..',
        '..##....##..',
        '..###..###..',
        '.##########.',
        '.###+##+###.',
        '.##+.##.+##.',
        '###+.##.+###',
        '##+..##..+##',
        '##+..##..+##',
        '#+...##...+#',
        '#+...##...+#',
    ],
    'bell': [
        '.....##.....',
        '....####....',
        '...######...',
        '..###++###..',
        '..##++++##..',
        '..##++++##..',
        '.###++++###.',
        '.###++++###.',
        '.##++++++##.',
        '###++++++###',
        '############',
        '............',
        '....+..+....',
        '.....++.....',
    ],
    'sealed': [
        '.....##.....',
        '.....##.....',
        '....####....',
        '...######...',
        '..###..###..',
        '..##.++.##..',
        '..##.++.##..',
        '.###....###.',
        '.##.++++.##.',
        '.##......##.',
        '###.++++.###',
        '###......###',
        '############',
        '.##########.',
    ],
    'hook': [
        '.....##.....',
        '.....##.....',
        '....####....',
        '.....##.....',
        '.....##.....',
        '.....##.....',
        '.....##.....',
        '.....##.....',
        '.+...##.....',
        '.++..##.....',
        '.++..##.....',
        '..++##......',
        '...###......',
        '............',
    ],
    'plume': [
        '+.+.+..+.+.+',
        '.+++....+++.',
        '+.+.+..+.+.+',
        '..+......+..',
        '...#....#...',
        '....#..#....',
        '.....##.....',
        '.....##.....',
        '.....##.....',
        '....####....',
        '...######...',
        '..##.##.##..',
        '.##..##..##.',
        '##...##...##',
    ],
    'bowl': [
        '.....+......',
        '....++......',
        '....+++.+...',
        '...+++++++..',
        '...+++++++..',
        '....+++++...',
        '#..........#',
        '############',
        '.##########.',
        '..########..',
        '...######...',
        '.....##.....',
        '...######...',
        '............',
    ],
    'flame': [
        '.....#......',
        '.....##.....',
        '....###.....',
        '....####..#.',
        '...#####.##.',
        '..###+####..',
        '..##+++###..',
        '.###+++####.',
        '.##++++++##.',
        '.##+++++++#.',
        '.##++++++##.',
        '..##++++##..',
        '...######...',
        '............',
    ],
    'crown': [
        '.+...+..+...',
        '.+..+...+..+',
        '..+.+..+..+.',
        '..+.+..+.+..',
        '..+..+.+.+..',
        '...+.+.++...',
        '############',
        '#..##..##..#',
        '############',
        '.##########.',
        '..########..',
        '..##....##..',
        '..##....##..',
        '.####..####.',
    ],
}


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def build(role):
    """The role's rig and painted atlas, exactly as its generator writes them."""
    module = importlib.import_module(f'court_{role}')
    r = module.rig()
    size = layout(r, ATLAS_WIDTH)
    painters = [lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(module.paint(c, t))]
    return r, paint_atlases(r, size, painters)[0]


def portrait(role, house):
    r, atlas = build(role)
    heads = annetta_kit.transforms(r)
    head = heads.get('head')
    y = head[1, 3] if head is not None else 0.0
    q = quads(r)
    # Head and shoulders: from the hat brim to the chest, three-quarter view.
    image = annetta_kit.render_quads(q, atlas, -0.4, 0.06, 480, focus=(y - 12.5 - LIFT.get(role, 0), y + 6.5 - LIFT.get(role, 0)))
    top, bottom = WATER[house]
    out = Image.new('RGB', image.size)
    px, src = out.load(), image.load()
    w, h = image.size
    for j in range(h):
        water = mix(top, bottom, j / (h - 1))
        for i in range(w):
            c = src[i, j]
            if c == BACKGROUND:
                # A soft shaft of light from above, brighter near the figure's crown.
                shaft = max(0.0, 1 - abs(i - w * 0.42) / (w * 0.5)) * (1 - j / h) * 0.25
                c = mix(water, (230, 240, 230), shaft)
            px[i, j] = c
    small = out.resize((48, 48), Image.BOX)
    # A darkened rim so the portrait frame's edge reads.
    sp = small.load()
    for j in range(48):
        for i in range(48):
            d = min(i, j, 47 - i, 47 - j)
            if d < 3:
                sp[i, j] = mix(sp[i, j], (6, 8, 12), 0.35 * (3 - d) / 3)
    small.convert('RGBA').save(PORTRAIT_DIR / f'{role}_portrait.png')


def shield():
    """The shared shield's alpha and top chevron, read from the default crest."""
    base = Image.open(CREST_DIR / 'default.png').convert('RGBA')
    cream, inner = (245, 223, 177, 255), (33, 22, 21, 255)
    alpha = [[base.getpixel((x, y))[3] > 0 for x in range(32)] for y in range(32)]
    chevron = set()
    for y in range(10):
        for x in range(32):
            if base.getpixel((x, y)) != cream:
                continue
            near = [base.getpixel((x + dx, y + dy)) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            if inner not in near:
                chevron.add((x, y))
    return alpha, chevron


def crest(role, house, glyph, alpha, chevron):
    pal = PALETTES[house]
    trim = {'giving_light': pal['cloth2'], 'holding_stone': pal['cloth2'], 'shared_wake': pal['trim'],
            'kindling': pal['trim'], 'woven_current': pal['cloth2'], 'veiled_fold': pal['trim'],
            'still_bell': pal['trim'], 'last_marrow': pal['trim']}[house]
    field = mix(pal['lacquer'], (0, 0, 0), 0.35)
    accent = {'giving_light': (255, 196, 84), 'holding_stone': (184, 104, 112), 'shared_wake': (172, 30, 52),
              'kindling': (232, 120, 44), 'woven_current': (156, 136, 255), 'veiled_fold': (120, 116, 128),
              'still_bell': (207, 230, 246), 'last_marrow': (216, 52, 60)}[house]
    cream = (245, 223, 177)
    image = Image.new('RGBA', (32, 32), (0, 0, 0, 0))
    for y in range(32):
        for x in range(32):
            if not alpha[y][x]:
                continue
            edge = any(not (0 <= x + dx < 32 and 0 <= y + dy < 32 and alpha[y + dy][x + dx])
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            colour = trim if edge else cream if (x, y) in chevron else field
            image.putpixel((x, y), colour + (255,))
    rows = GLYPHS[glyph]
    ox, oy = 10, 11
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch == '#':
                image.putpixel((ox + i, oy + j), cream + (255,))
            elif ch == '+':
                image.putpixel((ox + i, oy + j), accent + (255,))
    image.save(CREST_DIR / f'court_{role}.png')


def main():
    roles = sys.argv[1:] or list(ROLES)
    alpha, chevron = shield()
    for role in roles:
        house, glyph = ROLES[role]
        portrait(role, house)
        crest(role, house, glyph, alpha, chevron)
        print(f'{role}: portrait and crest')


if __name__ == '__main__':
    main()
