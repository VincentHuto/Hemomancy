"""Quick look at a Seat template without launching the game: a top view (highest block per column) and two cut-aways
through the centre column, coloured by block. python -m brined_court.preview from tools/worldgen writes
build/brined-seat-preview/<site>_<piece>.png. A layout aid only; the review client is the real proof.
"""
import os

from PIL import Image, ImageDraw

from . import garden, sanctum

COLOURS = {
    'stone': (120, 120, 120), 'andesite': (130, 130, 128), 'tuff': (100, 104, 96), 'diorite': (190, 190, 186),
    'brick': (110, 110, 112), 'moss': (88, 120, 52), 'sunburst': (236, 154, 44), 'frond': (226, 176, 70),
    'lamp': (255, 230, 140), 'calcite': (220, 220, 214), 'bone': (220, 210, 180), 'glass': (180, 220, 230),
    'lacquer': (120, 30, 40), 'water': (40, 80, 170), 'air': (24, 20, 26), 'blackstone': (52, 44, 52),
    'basalt': (76, 76, 82), 'tissue': (220, 90, 40), 'pickle': (140, 200, 90), 'magma': (200, 90, 30),
    'follower': (140, 150, 120), 'basin': (90, 60, 50), 'jigsaw': (255, 0, 255), 'kelp': (60, 110, 50),
    'seagrass': (60, 140, 60), 'coral': (180, 50, 60), 'bed': (230, 120, 40), 'cake': (240, 230, 220),
    'wool': (240, 140, 40), 'oak': (70, 50, 30), 'lichen': (110, 160, 110), 'vines': (120, 150, 60),
}


def colour(name):
    path = name.split(':')[1]
    for key, rgb in COLOURS.items():
        if key in path:
            return rgb
    return (200, 0, 200)


def render(room, out, scale=6):
    sx, sy, sz = room.size
    top = Image.new('RGB', (sx, sz), (10, 30, 70))
    for x in range(sx):
        for z in range(sz):
            for y in range(sy - 1, -1, -1):
                name = room.get(x, y, z)
                if name and name != 'minecraft:air':
                    shade = 0.55 + 0.45 * y / sy
                    top.putpixel((x, z), tuple(int(c * shade) for c in colour(name)))
                    break
    cuts = []
    for axis in ('x', 'z'):
        image = Image.new('RGB', (sx if axis == 'x' else sz, sy), (10, 30, 70))
        for u in range(image.width):
            for y in range(sy):
                pos = (u, y, sz // 2) if axis == 'x' else (sx // 2, y, u)
                name = room.get(*pos)
                if name:
                    image.putpixel((u, sy - 1 - y), colour(name))
                elif y > room.waterline:
                    image.putpixel((u, sy - 1 - y), (120, 160, 200))
        cuts.append(image)
    width = top.width + sum(c.width for c in cuts) + 20
    sheet = Image.new('RGB', (width * scale, max(top.height, sy) * scale + 20), (20, 16, 20))
    x = 0
    for image in [top] + cuts:
        sheet.paste(image.resize((image.width * scale, image.height * scale), Image.NEAREST), (x, 20))
        x += (image.width + 10) * scale
    ImageDraw.Draw(sheet).text((4, 4), out + ': top view, cut along x, cut along z', fill=(230, 230, 230))
    os.makedirs('build/brined-seat-preview', exist_ok=True)
    sheet.save(os.path.join('build/brined-seat-preview', out + '.png'))


def main():
    render(garden.Garden().build(), 'lighthouse_garden_garden')
    render(garden.Undercroft().build(), 'lighthouse_garden_undercroft')
    render(sanctum.Sanctum().build(), 'vent_sanctum_sanctum')
    render(sanctum.Body().build(), 'vent_sanctum_body')


if __name__ == '__main__':
    main()
