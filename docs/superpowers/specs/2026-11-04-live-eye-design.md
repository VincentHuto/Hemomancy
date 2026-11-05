# The live eye: the Chamber seen through the husk

Approved in conversation on 2026-11-04 from `2026-11-04-live-chamber-portal-research.md` section 2, including the
player-built blocks. Builds on the Chamber gateway.

## Intent

When a broken Monolith is its owner's gateway, its open eye shows the Chamber of Will itself, with true parallax as the
viewer moves, rather than the void. The husk's eye is the corridor gate's eye seen from the other side: what is seen is
the corridor through the gate, the arbor at its far end. Anyone looking at the eye sees it; it is a window.

## What is drawn

A stand-in of the room, rendered by the client into an offscreen texture every frame the eye is open and on screen,
from a camera placed in the Chamber the way the viewer stands at the husk:

- the Chamber's sky, in the owner's theme, drawn by the existing theme effects from the proxy camera's rotation;
- every block in the room, from a snapshot the husk carries (so the floor, the lights, the seat, and anything the
  owner built, all look as they are); the gate block has no model and draws nothing;
- the corridor's arches, the same baked ribs the gate draws;
- the gate's own eye at the corridor's end, standing open;
- the Arbor of Will, through its own renderer on a client-side stand-in entity configured from the snapshot.

The husk's eye samples that texture by screen position. Its rim, lids and seam stay as they are; only the void inside
the open eye becomes the view. The Chamber-side gate keeps the void: the way out is still a walk through it.

## The snapshot

The husk's block entity carries `ChamberProxySnapshot`: the owner's theme, built radius, visual degree, pome count and
the room's blocks as packed longs (offset from the cell and block state id), capped at 6000 entries. The server takes
it when the eye wakes, when it is opened briefly for an exit, and when the Seed is taken and the link is made, from
the Chamber level if it is loaded. Without a Chamber level the snapshot has no blocks and the view shows the sky, the
arches and the arbor.

## The camera

The eye's centre is the husk's anchor plus (0.5, 1.77, 0.5); the gate's is the gate block plus the same. The viewer's
offset from the eye is turned so the husk's facing becomes south (+Z, out of the corridor) and added to the gate's
centre. The camera's yaw turns by the same angle; its pitch is kept. Standing two blocks in front of the husk is standing
two blocks in front of the gate, looking in; the gate's own eye is the window and is not drawn in the view (owner
ruling 2026-11-04: an earlier six block push showed the gate's eye in the middle of the view, which made no sense from
the eye's own viewpoint).

## Cost and limits

One extra pass at half the main target's size, only for the nearest open eye in view within 48 blocks, with depth. The
texture lags the request by one frame: the first frame an eye opens it shows the void, then the view. When a shader
pack owns the draw target the pass is skipped and the void shows, as the loom's dome does.

## Non-goals

Entities other than the arbor, items, the border aura, the Chamber-side gate showing the world, and more than one live
eye at once.
