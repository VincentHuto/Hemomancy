# Core loop design plates

Nine plates that illustrate the proposal in `docs/core_loop/`. They are authored diagrams and mock scenes, drawn as SVG and rendered to PNG. They are not painted concept art and they are not in-game captures. Every number on them comes from the track docs and is a proposal value, not an approved one.

## The plates

| Plate | File | Shows | Track |
|---|---|---|---|
| 1 | `01-two-verbs.png` | Projection and Absorption, what a verb can touch, and who answers the button | 1, 2 |
| 2 | `02-shapings.png` | One verb changed by the selected manipulation | 2, 5 |
| 3 | `03-living-blood.png` | What keeps blood alive, the fresh window, clotting, and what the rule forbids | 3 |
| 4 | `04-temper.png` | Where Temper comes from, the eight pools, what burns it, tempered against dry, scars | 4 |
| 5 | `05-ground-network.png` | Stain, gouge, living vein, and the catalysts a gouge can carry | 6 |
| 6 | `06-farming-and-water.png` | Basin and gutter, scab drips, planting basin, cloche, Blood Chum | 7 |
| 7 | `07-strand-forms.png` | The five strand forms and what holding a strand costs | 12 |
| 8 | `08-lash-and-draw.png` | Anatomy of a lash, the crack, and Absorption's eight riders | 12, 12a |
| 9 | `09-day-and-world.png` | One day of play and how the world answers spilled blood | 10 |

## Visual language

The plates read as pages from a field manual kept by someone who studies blood. The ground is a dark oxblood field with a faint dot grid. Titles are a bone serif. Labels are small spaced capitals. Each figure sits in a panel with corner ticks and a figure number.

The world is drawn in pixel blocks at 16 texels per block, so a scene reads as Minecraft without copying any texture. Blocks, creatures and items stay flat and quiet. The blood is the only thing that glows.

Colour carries meaning. Plain blood is one red. A school's colour appears only where that school is acting, and the eight colours match `EnumBloodTemper`. There is no copper, brass, bronze or silver on anything blood-side. Blood machines are hematic iron, bone and lacquer.

Focal strands are bold: a soft glow, a body and a bright core. Marks on the ground are thin and partly clear, so a stain or a gouge never turns into a tangle of lines.

Text is kept short. A plate should be understood from the picture first, and the words only name what the picture already shows.

## Regenerating

The generator lives in `tools/core_loop/`, which is local and not version-controlled. `kit.py` holds the drawing kit, `plates_a.py`, `plates_b.py` and `plates_c.py` hold the plates, and `render.py` builds them.

```
python tools/core_loop/render.py          # all nine
python tools/core_loop/render.py 7 8      # only the plates named
```

Rendering needs Chrome or Edge installed. Each plate is written as an SVG beside its PNG, at 1600 by 1000 and rendered at twice that size.
