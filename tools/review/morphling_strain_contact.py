"""Contact sheets from a morphlingStrainReviewClient run (Brined Court Track 3).

Reads build/morphling-strain-review/screenshots and writes, next to them:
  - apex_contact.png    all eight strains at Apex side by side, back / side / front rows (the "read apart" check)
  - stages_<strain>.png one strain's six stages, back / side / front rows
  - states.png          every active-state scene's back shot, labelled

Run: python tools/review/morphling_strain_contact.py
"""
from pathlib import Path
from PIL import Image, ImageDraw
import re

ROOT = Path(__file__).resolve().parents[2]
SHOTS = ROOT / 'build/morphling-strain-review/screenshots'
STRAINS = ['sunburst', 'holdfast', 'mermaids_purse', 'dead_mans_rope', 'ink_cap', 'hushbell', 'marrowplume', 'kinbud']
STAGES = ['unfed', 'fledgling', 'developing', 'mature', 'apex', 'primal']
VIEWS = ['back', 'side', 'front']
CELL = (320, 180)
PATTERN = re.compile(r'morphling-\d+-(.+)-(back|side|front|high_back)\.png$')


def shots():
    found = {}
    for path in sorted(SHOTS.glob('morphling-*.png')):
        m = PATTERN.match(path.name)
        if m:
            found[(m.group(1), m.group(2))] = path
    return found


def crop(path):
    image = Image.open(path).convert('RGB')
    w, h = image.size
    # The player stands in the middle of the frame; keep the central band.
    box = (int(w * .3), int(h * .05), int(w * .7), int(h * .95))
    return image.crop(box).resize((CELL[0] // 2, CELL[1]), Image.LANCZOS)


def sheet(columns, rows, cells, title, out):
    cw, ch = CELL[0] // 2, CELL[1]
    image = Image.new('RGB', (cw * len(columns), ch * len(rows) + 20), (30, 28, 36))
    draw = ImageDraw.Draw(image)
    for c, column in enumerate(columns):
        draw.text((c * cw + 4, ch * len(rows) + 4), column, fill=(225, 220, 230))
        for r, row in enumerate(rows):
            path = cells.get((column, row))
            if path:
                image.paste(crop(path), (c * cw, r * ch))
    image.save(out)
    print('wrote', out.relative_to(ROOT), title)


def main():
    found = shots()
    apex = {(s, v): found.get((f'{s}-apex', v)) for s in STRAINS for v in VIEWS}
    sheet(STRAINS, VIEWS, apex, 'Apex, all eight', SHOTS / 'apex_contact.png')
    for strain in STRAINS:
        cells = {(stage, v): found.get((f'{strain}-{stage}', v)) for stage in STAGES for v in VIEWS}
        sheet(STAGES, VIEWS, cells, strain, SHOTS / f'stages_{strain}.png')
    states = sorted({name for name, _ in found if name.startswith('state-')})
    cells = {(name.replace('state-', ''), 'back'): found.get((name, 'back')) for name in states}
    cells.update({(name.replace('state-', ''), 'high_back'): found.get((name, 'high_back')) for name in states})
    if states:
        sheet([n.replace('state-', '') for n in states], ['back', 'high_back'], cells, 'states', SHOTS / 'states.png')


if __name__ == '__main__':
    main()
