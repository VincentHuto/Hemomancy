"""One module per Saint: biomes, Chapel kinds by slot, and the kit Theme (CRYPT) every piece, the entrance included, is
painted with.
The theme tracks (8-11) own these files and restyle the complexes through them.

Optional hooks a theme module may define, each called with a list of (kind, (x, y, z), facing) sockets to fill with
set-piece blocks (Track 6; kit.devotee, kit.containment_anchor and friends):
  chamber_sockets(room, c, sockets)  pieces.CHAMBER_SOCKETS, the chamber's (see the comment there);
  nave_sockets(room, c, sockets)     pieces.NAVE_SOCKETS, the congregation's places in the nave's vessel;
  hall_sockets(room, c, sockets)     pieces.hall_sockets, each trial hall's (entry_flank either side of its way in).
Two more restyle whole pieces (Track 8):
  HALLS = {kind: builder}            a kind's trial hall built the theme's way: builder(saint) -> (room, marks), the
                                     same length, width and exit floor as the shared hall so the focus room still
                                     continues its run (pieces.hall);
  motif(name, room, c)               a pass over every template, by piece name, after it is built and before
                                     kit.finish breaks its flats (build.templates)."""
from . import hemorath, seraphae, putriciel, velorum

THEMES = {module.SAINT: module for module in (hemorath, seraphae, putriciel, velorum)}
SAINTS = list(THEMES)
