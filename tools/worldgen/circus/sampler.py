"""The kit sampler: every Circus set piece and assembly on one board, the template the kit's tests stand on.

It is not a site and never generates in a world. The GameTest suite places it to prove every set piece survives
placement whole (a wheel keeps its four quarters, a two-tall piece its halves, a pole its sections), and the JVM test
reads it for the kit's rules. Three plots: a slice of the big top (ring, curb and footlights, the plinth, a king
pole, seating, the upper gallery and its stairs, the Ringmaster's box, a rigging tower over its net, a laced canvas
wall dressed with the wall fixtures, the valance and guy lines outside); the backyard and midway (a painted wagon,
the cookhouse, the dressing tent, the ticket booth and banner line, the wrecked stage wagon); and the five camps'
apparatus with a lean-to and a practice ring, one camp turned to face east.
"""
from . import kit
from .kit import BONE, PLANKS, STONE, Site

SIZE = (66, 22, 48)
NAME = 'circus_kit_sampler'


def floor(site):
    """A floor that is never one block over a 4 x 4 square."""
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            site.put(x, 0, z, (STONE, BONE, PLANKS)[(x + 2 * z) % 3])


def big_top(f):
    kit.canvas_wall(f, 0, 20, 1, 8, 1)
    kit.valance_band(f, 0, 20, 8, 0, 'north')
    for x in (2, 8, 14, 20):
        kit.guy_line(f, x, 1, -2, 'south')
    for x, fixture in ((3, 'circus_spindle_rack'), (6, 'circus_loft_record'), (11, 'circus_pelt_drape'),
                       (16, 'circus_trophy')):
        f.piece(x, 3, 2, fixture, 'south')
    for x, mask in ((8, 'laughing_mask'), (9, 'mourning_mask'), (10, 'blank_mask')):
        f.piece(x, 4, 2, mask, 'south')
    kit.ring(f, 9, 1, 10, 5, footlights=(0, 90, 180, 270))
    kit.plinth(f, 9, 1, 10)
    kit.pole(f, 17, 1, 10, 16, collars=(6, 11))
    f.piece(13, 1, 10, 'impaled_procession_record', 'west')
    f.piece(17, 1, 13, 'circus_stage_record', 'north')
    kit.seating(f, 4, 14, 1, 17, 3)
    kit.gallery(f, 2, 18, 6, 21, ground=1, depth=2)
    kit.gallery_stairs(f, 17, 1, 28, 6, 'north')
    kit.ringmaster_box(f, 9, 6, 18)
    for x in (5, 13):
        f.piece(x, 5, 22, 'circus_crossbar_rig', 'south')
    kit.rigging_tower(f, 23, 1, 14, 4)


def backyard(f):
    kit.wagon(f, 0, 2, 2)
    kit.cookhouse(f, 9, 1, 2)
    kit.dressing_tent(f, 17, 1, 2)
    kit.ticket_booth(f, 0, 1, 14)
    kit.stage_wreck(f, 20, 2, 14)


def camps(f):
    kit.air_apparatus(f, 2, 1, 10)
    kit.pageantry_apparatus(f, 14, 1, 6)
    kit.embers_apparatus(f, 20, 1, 6)
    kit.memory_apparatus(f, 26, 1, 6)
    kit.lean_to(f, 8, 1, 10)
    kit.ring(f, 20, 1, 12, 3)
    kit.blades_apparatus(f.sub(10, 0, 4, 'east'), 0, 1, 0)


def build():
    site = Site(SIZE[0], SIZE[1], SIZE[2], ground=0, seed=15007)
    floor(site)
    big_top(site.frame(2, 0, 3))
    backyard(site.frame(32, 0, 3))
    camps(site.frame(32, 0, 30))
    site.finish()
    return site
