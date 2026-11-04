"""HUD sprites for the eight house strains, from the same pixel maps as the item icons (Brined Court Track 3).

Each strain's 16x16 map (pixel-maps.json) is enlarged to the HUD's native 32x32 frame with EPX (Scale2x), which keeps
hard pixel-art edges and rounds diagonals instead of doubling pixels, then given a feeding tendril that hooks onto the
blood vessel at the HUD's feeding point (x 5, y 16; EquippedMorphlingOverlayPlacement.MOUTH_X/Y). The result is
written as authored rows to hud-pixel-maps.json (so the art can be hand-edited and re-exported from the maps alone)
and exported to textures/gui/morphling_overlay/<id>.png, plus five-frame 48x48 feeding strips under anim/ and anim2/
(the creature breathing in place while its tendril grips).

Run from the repo root:  python tools/model_export/morphling_hud/export_hud_sprites.py
       re-export only:   python tools/model_export/morphling_hud/export_hud_sprites.py --from-maps
"""
from pathlib import Path
from PIL import Image
import argparse
import json

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
OUT = ROOT / 'src/main/resources/assets/hemomancy/textures/gui/morphling_overlay'
MOUTH = (5, 16)
KEYS = '123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'


def load_icons():
    return json.loads((HERE / 'pixel-maps.json').read_text(encoding='utf-8'))


def scale2x(rows):
    h, w = len(rows), len(rows[0])
    out = [['.'] * (w * 2) for _ in range(h * 2)]
    get = lambda x, y: rows[min(h - 1, max(0, y))][min(w - 1, max(0, x))]
    for y in range(h):
        for x in range(w):
            p, a, b, c, d = get(x, y), get(x, y - 1), get(x + 1, y), get(x - 1, y), get(x, y + 1)
            e0 = a if (c == a and c != d and a != b) else p
            e1 = b if (a == b and a != c and b != d) else p
            e2 = c if (d == c and d != b and c != a) else p
            e3 = d if (b == d and b != a and d != c) else p
            out[2 * y][2 * x], out[2 * y][2 * x + 1], out[2 * y + 1][2 * x], out[2 * y + 1][2 * x + 1] = e0, e1, e2, e3
    return out


def darkest(palette):
    return min(palette, key=lambda k: sum(int(palette[k][i:i + 2], 16) for i in (1, 3, 5)))


def lightest(palette):
    return max(palette, key=lambda k: sum(int(palette[k][i:i + 2], 16) for i in (1, 3, 5)))


def with_tendril(grid, palette):
    """A hooked feeding tendril from the creature's nearest left edge to the vessel at the feeding point."""
    dark, light = darkest(palette), lightest(palette)
    mx, my = MOUTH
    target = None
    for dy in (0, 1, -1, 2, -2, 3, -3, 4, -4):
        y = my + dy
        xs = [x for x in range(len(grid[y])) if grid[y][x] != '.']
        if xs:
            target = (min(xs), y)
            break
    tx, ty = target
    x, y = tx - 1, ty
    while x > mx:
        grid[y][x] = dark
        if y != my and (tx - x) % 2 == 0:
            y += 1 if my > y else -1
            grid[y][x] = dark
        x -= 1
    for yy in range(min(y, my), max(y, my) + 1):
        grid[yy][mx] = dark
    # The hook: a pale sucker on the vessel at the feeding point, with a curl above and below it.
    grid[my][mx] = light
    grid[my][mx - 1] = dark
    grid[my - 1][mx - 1] = dark
    grid[my + 1][mx - 1] = dark
    grid[my - 2][mx] = dark
    grid[my + 2][mx] = dark
    return grid


def build_maps():
    maps = []
    for icon in load_icons():
        palette = dict(icon['palette'])
        grid = with_tendril(scale2x(icon['rows']), palette)
        maps.append(dict(name=icon['name'], title=icon['title'], rows=[''.join(r) for r in grid], palette=palette))
    (HERE / 'hud-pixel-maps.json').write_text(json.dumps(maps, indent=1) + '\n', encoding='utf-8')
    return maps


def image(spec):
    rows, palette = spec['rows'], spec['palette']
    img = Image.new('RGBA', (len(rows[0]), len(rows)))
    for y, row in enumerate(rows):
        for x, key in enumerate(row):
            if key != '.':
                hexcode = palette[key]
                img.putpixel((x, y), tuple(int(hexcode[i:i + 2], 16) for i in (1, 3, 5)) + (255,))
    return img


def strip(sprite, breathe):
    """Five 48x48 frames: the body swells a pixel and settles while the hook stays fixed on the vessel."""
    frames = Image.new('RGBA', (48, 240))
    for f, (dy, dx) in enumerate(breathe):
        frame = Image.new('RGBA', (48, 48))
        body = sprite.copy()
        hook = sprite.crop((0, 0, 8, 32))
        frame.alpha_composite(body, (8 + dx, 8 + dy))
        frame.alpha_composite(hook, (8, 8))
        frames.alpha_composite(frame, (0, f * 48))
    return frames


def export(maps):
    for spec in maps:
        sprite = image(spec)
        assert sprite.size == (32, 32), spec['name']
        assert sprite.getpixel(MOUTH)[3] == 255, spec['name'] + ' feeding point'
        assert set(sprite.getchannel('A').tobytes()) <= {0, 255}, spec['name'] + ' binary alpha'
        sprite.save(OUT / f"{spec['name']}.png")
        (OUT / 'anim').mkdir(exist_ok=True)
        (OUT / 'anim2').mkdir(exist_ok=True)
        strip(sprite, [(0, 0), (-1, 0), (-1, 1), (0, 1), (0, 0)]).save(OUT / 'anim' / f"{spec['name']}.png")
        strip(sprite, [(0, 0), (1, 0), (1, -1), (0, -1), (0, 0)]).save(OUT / 'anim2' / f"{spec['name']}.png")
    sheet = Image.new('RGBA', (len(maps) * 34 * 4, 34 * 4), (36, 35, 44, 255))
    for i, spec in enumerate(maps):
        sheet.alpha_composite(image(spec).resize((128, 128), Image.NEAREST), (i * 136 + 4, 4))
    sheet.save(HERE / 'hud-preview.png')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--from-maps', action='store_true', help='export hud-pixel-maps.json as it stands')
    args = parser.parse_args()
    maps = json.loads((HERE / 'hud-pixel-maps.json').read_text(encoding='utf-8')) if args.from_maps else build_maps()
    export(maps)
    print(f'Exported {len(maps)} HUD sprites and their feeding strips.')


if __name__ == '__main__':
    main()
