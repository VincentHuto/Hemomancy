"""Rebuild only the five teaching camps; preserve faculty and worldgen registrations."""
from troupe_templates import Template

VARIANTS = {
    'air': ['circus_acrobat'],
    'blades': ['circus_knife_thrower'],
    'pageantry': ['circus_stilt_walker', 'circus_strongman'],
    'embers': ['circus_fire_eater', 'circus_beast_tamer'],
    'memory': ['circus_understudy'],
}
FENCE = {'north': 'false', 'east': 'false', 'south': 'false', 'west': 'false', 'waterlogged': 'false'}


def tent(t, left):
    """Nine-wide canvas A-frame, opening toward the teaching yard at z=9."""
    for dx in range(9):
        roof = 1 + min(dx, 8 - dx)
        for z in range(1, 10):
            canvas = 'minecraft:white_wool' if z in (3, 4, 7, 8) else 'minecraft:red_wool'
            t.block(left + dx, roof, z, canvas)
            if z in (1, 9):
                for y in range(1, roof): t.block(left + dx, y, z, 'minecraft:red_wool')
    middle = left + 4
    for x in range(middle - 1, middle + 2):
        for y in range(1, 4): t.block(x, y, 9, 'minecraft:air')
    for x in (middle - 2, middle + 2):
        for y in range(1, 4): t.block(x, y, 9, 'minecraft:white_wool')
    for z in (1, 9):
        t.block(middle, 5, z, 'minecraft:dark_oak_log', {'axis': 'y'})
    t.block(middle, 6, 1, 'minecraft:red_banner', {'rotation': '8'})
    t.block(middle, 4, 5, 'hemomancy:hematic_lantern', {'hanging': 'true', 'waterlogged': 'false'})
    for x in (left - 1, left + 9):
        for z in (0, 10):
            t.block(x, 1, z, 'minecraft:dark_oak_fence', FENCE)
    t.block(left + 1, 1, 3, 'minecraft:barrel', {'facing': 'up', 'open': 'false'})


def camp(variant):
    t = Template((25, 9, 25))
    for x in range(25):
        for z in range(25):
            yard = (x - 12) ** 2 + (z - 15) ** 2 < 110
            ground = 'minecraft:coarse_dirt' if yard else 'minecraft:grass_block'
            if yard and (x * 7 + z * 11) % 13 == 0: ground = 'minecraft:gravel'
            t.block(x, 0, z, ground)
            for y in range(1, 9): t.block(x, y, z, 'minecraft:air')
    for left in (1, 15):
        for x in range(left, left + 9):
            for z in range(1, 11): t.block(x, 0, z, 'minecraft:dark_oak_planks')
        tent(t, left)
    # Connect the entrances without turning the whole camp into a rectangular deck.
    for x in range(1, 24):
        for z in (10, 11): t.block(x, 0, z, 'minecraft:dark_oak_planks')
    for x in range(11, 14):
        for z in range(12, 25): t.block(x, 0, z, 'minecraft:coarse_dirt')
    for z in range(12, 24, 3): t.block(12, 0, z, 'hemomancy:polished_venous_stone')
    for middle in (8, 16):
        for dx in range(-3, 4):
            for dz in range(-3, 4):
                if abs(dx) == abs(dz) == 3: continue
                edge = max(abs(dx), abs(dz)) == 3
                material = 'minecraft:red_terracotta' if edge else 'minecraft:smooth_stone'
                t.block(middle + dx, 0, 16 + dz, material)
        t.block(middle, 0, 16, 'hemomancy:polished_venous_stone')
    for x in (11, 13):
        for y in range(1, 4): t.block(x, y, 10, 'minecraft:dark_oak_fence', FENCE)
        t.block(x, 4, 10, 'hemomancy:hematic_lantern', {'hanging': 'false', 'waterlogged': 'false'})
    # Teaching and rest furnishings stay out of the entrance and practice lanes.
    t.entity('circus_threadkeeper', 5.5, 1, 4.5)
    t.block(3, 1, 4, 'hemomancy:puppeteers_spindle')
    t.block(4, 1, 4, 'minecraft:crafting_table')
    for i, role in enumerate(VARIANTS[variant]): t.entity(role, 9.5 + i * 8, 1, 16.5)
    for part, z in [('head', 6), ('foot', 7)]:
        t.block(19, 1, z, 'minecraft:red_bed', {'facing': 'north', 'occupied': 'false', 'part': part})
    t.block(22, 1, 5, 'minecraft:barrel', {'facing': 'up', 'open': 'false'})
    for x in (6, 7, 17, 18):
        t.block(x, 1, 21, 'minecraft:dark_oak_slab', {'type': 'bottom', 'waterlogged': 'false'})
        t.block(x, 1, 22, 'minecraft:dark_oak_trapdoor',
                {'facing': 'north', 'half': 'bottom', 'open': 'true', 'powered': 'false', 'waterlogged': 'false'})
    if variant == 'air':
        for y in range(1, 8): t.block(21, y, 19, 'minecraft:dark_oak_fence', FENCE)
        t.block(21, 7, 19, 'minecraft:dark_oak_fence', {**FENCE, 'east': 'true', 'west': 'true'})
        t.block(20, 7, 19, 'minecraft:dark_oak_fence', {**FENCE, 'east': 'true'})
        t.block(22, 7, 19, 'minecraft:dark_oak_fence', {**FENCE, 'west': 'true'})
        t.block(21, 7, 18, 'hemomancy:troupe_rigging')
        t.block(22, 1, 16, 'minecraft:hay_block', {'axis': 'y'})
    elif variant == 'blades':
        for x in (4, 20):
            t.block(x, 1, 21, 'minecraft:dark_oak_fence', FENCE)
            t.block(x, 2, 21, 'minecraft:target', {'power': '0'})
        t.block(22, 1, 16, 'minecraft:hay_block', {'axis': 'y'})
    elif variant == 'pageantry':
        t.block(2, 1, 16, 'minecraft:anvil', {'facing': 'north'})
        for x in (2, 4):
            t.block(x, 1, 21, 'minecraft:polished_blackstone')
        t.block(3, 1, 21, 'minecraft:bone_block', {'axis': 'x'})
        for z in (15, 17, 19):
            t.block(22, 1, z, 'hemomancy:blank_mask', {'facing': 'west'})
    elif variant == 'embers':
        t.block(22, 1, 22, 'minecraft:water_cauldron', {'level': '3'})
        t.block(21, 1, 22, 'minecraft:smoker', {'facing': 'north', 'lit': 'false'})
        for x in (21, 22): t.block(x, 0, 22, 'minecraft:polished_blackstone')
        for z in (15, 16): t.block(22, 1, z, 'minecraft:hay_block', {'axis': 'y'})
    else:
        t.block(19, 1, 3, 'minecraft:bookshelf')
        t.block(19, 1, 4, 'hemomancy:circus_quarters_record')
        t.block(21, 1, 4, 'minecraft:bookshelf')
    return t


def build():
    for variant in VARIANTS:
        template = camp(variant)
        template.save('troupe_camp_' + variant)
        print(f'{variant}: pitched canvas, {len(template.entities)} preserved faculty, 25x9x25 footprint')


if __name__ == '__main__':
    build()
