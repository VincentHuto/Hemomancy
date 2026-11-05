# The Chamber Gateway and the Keyhole Chamber

Approved in conversation on 2026-11-04. Follows the Monolith walk (`2026-11-04-monolith-walk-design.md`).

## Intent

After the Seed is taken, the husk has no purpose. It becomes the Archon's permanent gateway into their own Chamber of
Will: the tier above the Somnolent Seat (restricted, timed) and the Chamber rite (portable, cumbersome). The price of
that power is that it stands in one place. The Chamber itself changes from a square to a keyhole, so that the gateway
has somewhere to arrive and a way to walk back out.

## Owner decisions

- The portal collapses vertically down the middle, like a closing third eye with its lids turned on their side.
- The owner reopens it with a small Blood Projection donation that takes a second or two. Right click only hints: the
  escape-hatch argument rests on the donation taking time.
- The walk is about 6 blocks.
- Using it as an escape hatch is fine. There is no combat lockout beyond the donation.
- A destroyed husk means building another Monolith and breaking it. Every fresh Monolith gives a Seed, as redundancy
  for a lost Seed or a broken Qliphoth tree.
- Seeds are bound to the player who took them, so a high-degree player cannot hand them to someone else.
- At Degree 8, an Apotheos who projects at the husk leaves it permanently infested, and their crossing gets a new
  fungal theme. Revised after the first capture: it must be a subtle fungal infestation in reds, oranges and yellows,
  not an intense white and yellow overlay and not a pile of thick strands.
- A Silentium's portal looks like the current one, but with the Silentium Chamber's grey fog and large distant pillars.
- Getting back: sleep in the Somnolent Seat at the arbor's foot (existing), or walk out through the portal at the end
  of the corridor.
- The Chamber becomes a keyhole: one long corridor under arches, leading to a circular platform with the Arbor of Will
  at its centre. Rank growth makes the corridor wider and taller and the circle larger.

## Keyhole Chamber (every owner, from their first visit)

Radius `R` stays `ChamberVisitRules.radiusForDegree` (3 to 10).

- Circle: cells with `dx^2 + dz^2 <= (R + 0.5)^2` around the cell centre, with the arbor at the exact centre.
- Corridor: runs along +Z from the circle's edge. Half width `hw = 1 + round((R - 3) * 3 / 7)`, so width 3 at `R` 3 and
  9 at `R` 10. It covers `|dx| <= hw`, `R <= dz <= R + L`, with length `L = R + 4`.
- Floor area is never meaningfully below today's square at any radius, and up to about 20% above it.
- Boundaries (block placement, movement, the rescue check, the Orb's "beyond the platform" test, the border aura) all
  read one shape, `ChamberRoomShape`.
- Growth stays non-destructive: it places only new floor into air and never touches player blocks. Corner markers move
  to four points on the circle.
- The clamp reads the built radius, not the tier radius, so a player can walk on every floor block they have.
- The seat stays north of the arbor. The corridor is south, so walking in leads straight to the arbor.

### Arches

Smooth, organic ribs in the Monolith's black surface material, not blocks. A block entity at the corridor's end draws
them, so growth changes geometry with nothing to clear and nothing that can collide with player builds. Their legs
stand just outside the walkway. Height is 3 to the spring plus a semicircle of radius `hw + 1`, so they grow with
rank. One rib every three blocks, plus a heavier one framing the far end.

### The corridor gate

`chamber_gate`, an unbreakable block with no item, sits on the last corridor row in every Chamber. It is a sealed eye
until its owner has a linked husk, a quiet hint of what is coming. Once linked it stands open permanently. Walking
into its plane within its width starts a 6 block crossing back to the husk. It moves when the room grows.

## The gateway

### The husk's states

1. Seed inside: the portal stands open, as built.
2. Seed taken: the third eye closes. A faint hairline seam stays between the halves. The husk is linked as the owner's
   gateway, so their corridor gate opens.
3. Reopening: the owner projects blood at the husk. Charge builds for 40 fed ticks at 2.5 blood a tick (100 total),
   the slit trembling wider as it builds. Charge bleeds off when feeding stops. Non-owners are refused, as is a husk
   standing inside the Chamber dimension.
4. Open: for 10 seconds, then the eye closes on its own. Closes behind the owner when they cross.

Right click on a closed husk shows a hint to its owner and the existing dormant line to anyone else.

### Crossing in (husk to Chamber)

A new `Mode.GATE`, length 6. No Seed and no retreat pull. The black hole sits ahead as the destination, and the view is
in the Chamber's colours from the first step. The far end does not land in the level: it starts an unlimited,
full-inventory Chamber visit (`ChamberVisitMode.ATTUNED`) and places the player at the far end of the corridor, facing
the arbor. It binds the seat if the player never bound one, so the way back exists. It does not grant the rite's
attunement or advancement. The return point is the side of the husk the player walked in from, so the seat also brings
them back there.

### Crossing out (Chamber to husk)

A new `Mode.RETURN`, length 6. The colours turn from the Chamber back to the Lidless void along the walk. It ends the
visit and places the player beside the husk. A husk that has since vanished clears the link and falls back to the
stored return point, so no one is stranded.

### One link per owner

The manager stores one husk per owner, set when a Seed is taken and whenever a husk is crossed. An older husk still
opens and re-links on use. Removing the linked husk clears the link. The Chamber gate then seals, and the seat is the
way out.

## Seed binding

A new `seed_binding` data component holds the owner's UUID and name, set when the Seed is taken or spilled. The Bloom
rite refuses a Seed bound to someone else, before anything is escrowed. Unbound Seeds (Creative, commands, test
fixtures) keep working for anyone. A tooltip names the bound owner. Holding or moving someone else's Seed is allowed;
it is simply inert for them.

## Themes

`LidlessTheme` is QLIPHOTH, SILENTIUM or APOTHEOS, read from the owner's path. It applies to the crossing view and the
portal panel through one shared include.

- QLIPHOTH: the current Chamber palette (sky 2C2635, cloud 6D5F86, tint D8B6FF, rim D10B1A).
- SILENTIUM: the Silent Archon theme's grey fog (skybox 5F7472, cloud 8CA6A2, nebula 223031, muted crimson 6E3337) with
  large fog-faded slab pillars standing in the distance on every side.
- APOTHEOS: an ember dark, the void's veins running red to orange, with soft drifting patches of fungal crust flecked
  with tiny yellow specks. No strands. Ring and halo in orange, core a warm near-black.

### The overgrown husk

The first Apotheos projection sets a permanent `overgrown` flag with a burst of spores and a line of text. The fissure
shader then draws a partly clear infestation over the halves' faces, sides, tops and raw break and over the rubble
mound: soft patches spreading from the cracks and the base, most of the stone left bare, a mottled crust from deep red
to orange, tiny yellow fruiting specks where it is thickest, and a few hairline threads creeping ahead of it. The cracks
keep their red mouths and run to orange and amber further down. The mound stays black beneath it.

## Non-goals

Guests in the Chamber, a Silentium-specific sound or silence pass, keyhole migration for dev worlds (the mod is not
live; dev worlds need a fresh Chamber), and any combat lockout.

## Builder calls

Blood cost, charge time, the 10 second window, corridor sizes, rib spacing, the choice of ribs over blocks, the sealed
gate being present from the first visit, and seat binding without rite attunement.
