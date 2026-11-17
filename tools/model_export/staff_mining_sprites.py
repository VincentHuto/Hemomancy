"""Memory overlays for the Living Staff's mining (core-loop Track 8): the pickaxe form, Lode Draw, Hematic Hewing.

Each is a 16x16 glyph inside the frame the other memory overlays keep to (columns and rows 4 to 11, corners clear), in the palette of its family:
bone and clotted flesh for the Living Arsenal forms, pale iron for Ferric memories.

    python tools/model_export/staff_mining_sprites.py
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'src/main/resources/assets/hemomancy/textures/item/memories'

# Living Arsenal palette (memory_living_*_overlay): bone, shaded bone, clotted flesh, blood.
ARSENAL = {'A': (243, 217, 191), 'B': (191, 165, 154), 'C': (140, 86, 91), 'D': (234, 60, 60)}
# Ferric palette (memory_iron_retort_overlay, memory_vascular_dowsing_overlay): bright, mid and dark iron.
FERRIC = {'A': (227, 235, 223), 'B': (161, 180, 188), 'C': (100, 115, 129), 'D': (234, 60, 60)}

GLYPHS = {
    # A bone pick head curving down at both points, on a haft running down to a blood pommel.
    'memory_living_pickaxe_overlay': (ARSENAL, [
        '.....AAAAA......',
        '....B..CB.B.....',
        '.......CB.......',
        '.......B........',
        '......B.........',
        '.....D..........',
        '....C...........',
    ]),
    # Two nuggets of one lode joined by a drawn line, the far one struck.
    'memory_lode_draw_overlay': (FERRIC, [
        '.....AA.........',
        '....ABBA........',
        '.....AAC........',
        '........C.......',
        '........BAB.....',
        '.......BADAB....',
        '........BAB.....',
    ]),
    # A cut face: an iron frame round a tunnel two tall, the stroke of the cut across its corner.
    'memory_hematic_hewing_overlay': (FERRIC, [
        '.....AAAAAA.....',
        '.....A.C..A.....',
        '.....ACBC.A.....',
        '.....ACBC.A.....',
        '.....A.C..A.....',
        '.....AAAAAD.....',
        '..........DD....',
    ]),
}


def main():
    for name, (palette, rows) in GLYPHS.items():
        image = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        for dy, row in enumerate(rows):
            for x, key in enumerate(row):
                if key != '.':
                    image.putpixel((x, 4 + dy), palette[key] + (255,))
        image.save(OUT / f'{name}.png')
        print('wrote', name)


if __name__ == '__main__':
    main()
