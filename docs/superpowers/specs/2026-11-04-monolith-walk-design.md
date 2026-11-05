# The Monolith Walk: the Qliphoth Seed is taken, not dropped

## Why

The Sanguine Monolith is the Lodge's incubation vessel; at Degree 7 the Archon's blood wakes the fragment inside and
the vessel cracks. Today that moment is an explosion: the Monolith vanishes, the Qliphoth Seed and the Fragments pop
out as items, and a dialogue opens. The Lidless Door (built 2026-11-03, `docs/superpowers/specs/2026-11-03-lidless-door-design.md`)
gives the mod a void crossing held inside a doorway. This design uses it: the Monolith cracks, splits and falls open,
a portal hangs between the halves, and the Archon walks into the void to take the Seed from the thing that has been
watching them. The walk itself is wordless: the eyes, the black hole and the Seed carry it, and the whisper that
today follows the explosion follows the walk instead. The player can turn back, but the closer they come the
harder it is.

## What the player sees and does (user's decisions)

- At Degree 7 the Monolith no longer explodes. On the interaction that used to explode it, cracks appear and spread
  across the stone, then it splits down the middle; both halves fall outward to about 45 degrees, hinged at the base,
  with a pile of black rubble around the fulcrum.
- Fog in the Cardinal rite style pours out around the base, lit by purple lightning.
- Between the fallen halves hangs a roughly diamond-shaped portal showing the Lidless void.
- Stepping into the portal starts a void crossing like the door's, with one difference ahead: instead of the exit
  window, a black hole in the Qliphoth style (the Bloom's and the Chamber's black hole) grows as the player
  approaches, and at its centre the Qliphoth Seed floats, glowing, its tendrils writhing. The closer the player gets,
  the more their vision warps and distorts.
- The player may turn back at any point, but the closer they are to the Seed the slower they retreat, as if pulled.
- Reaching the Seed takes it: it goes into the inventory, or floats on the ground in front of the player when the
  inventory is full. The player steps out the far side of the Monolith. The portal closes behind them.
- The Monolith stays cracked in half and crumbling, but otherwise dormant.

## Rules (my calls, approved in chat)

- **Trigger.** The sequence starts on the second Degree 7 interaction, exactly where `pressFurther` explodes the
  Monolith today. The first interaction keeps its warning dialogue. Everything else about the Monolith below Degree 7
  is unchanged.
- **The break** takes about twelve seconds (240 ticks): cracks grow over the first eight, the split and fall take the
  last four. The existing shatter burst plays at the split. The Monolith Fragments (the same `MonolithFragmentDropRules`
  roll) drop into the rubble when the halves fall, so they are the player's whether or not they walk. The Seed does not
  drop.
- **The portal** opens when the halves have fallen and stays open until the Seed is taken. It admits only the Archon
  who broke the Monolith (the block entity remembers their UUID); to everyone else, and to that Archon after the Seed
  is taken, the gap between the halves is solid. It survives relog and chunk unload: the block state is "broken" and
  the block entity holds the owner and whether the Seed is still inside.
- **The crossing** reuses the Lidless Passage service and client with a mode: `DOOR` (today's behaviour) or `SEED`.
  In `SEED` mode: no exit window ahead; the black hole and the Seed replace it; retreat speed scales from full at the
  start to a quarter of walking pace at the end (forward speed is unchanged); reaching the end takes the Seed, places
  the player in the cell beyond the far side (the door's landing rules), and the Monolith's block entity marks the
  Seed taken, which closes the portal. The crossing length is the door's config value.
- **Taking the Seed**: `ItemHandlerHelper`-style insertion into the player's inventory; what does not fit is dropped
  as the existing custom Seed item entity at the player's feet on the far side, with no pickup delay beyond vanilla's.
- **After**: the post-shatter whisper dialogue (`FungalWhisperDialogueTrees.postMonolithShatter`, under its existing
  memo rule) opens when the player steps out with the Seed, not when the stone breaks. Its two lines
  (`hemomancy.whisper.post_shatter.line1/2`) are rewritten for a Seed already in hand, in the Fungal Whisper voice
  (`docs/NPC_VOICES.md`); nothing new is said during the walk. The broken Monolith keeps its block entity and
  renderer and answers no interaction: `pressFurther` and the dialogue refuse a Monolith that is breaking or broken.
- **Breaking the husk.** Anyone may break it (Monoliths have no owner guard today; decision D7-2 stays open). A
  broken Monolith drops nothing: the shatter consumed the Monolith before and still does, so one Working craft still
  yields one Seed. If it is broken while the Seed is still inside, whether mid-crossing or not, the Seed drops as
  the custom Seed item entity at the Monolith so it is never lost; a player mid-crossing returns to the entry side
  and finds it there. The loot table gains a `BROKEN` condition.
- **Clearance.** The halves fall sideways, along the slab's width, so neither lands in the entry or far cell. No
  clearance check: the fall is visual, the collision stays the Monolith's own column (base plus filler) and the
  halves may clip into neighbouring blocks at outposts built against walls.
- **Blocked far cell.** Reaching the end always takes the Seed: the Seed is the Degree 7 key and is never held back
  by a wall. The landing then uses the door's full recovery search (far cell, around it, the entry side, world spawn)
  rather than the door's hold-at-the-end rule.
- **Discovery.** A Seed inserted straight into the inventory skips the pickup event the Liber relies on, so the
  completion hook calls the Liber discovery unlock for the Seed itself.
- **Interruptions** follow the door's rules: dying or logging out mid-crossing returns the player to the entry side
  without the Seed and the portal stays open for them; the Monolith being broken mid-crossing returns them to the
  entry side and the Seed drops there (see breaking the husk). Nothing durable is written to the player; the
  Monolith's block entity is the only record.
- **Not for the Unstained or anyone else**: only the owning Archon can enter, so no gate beyond ownership is needed.

## The look (my calls)

- **Cracks**: glowing red-black fissures drawn by the Monolith renderer over the slab, growing from a seed point at
  the base up the centre and branching, driven by a `crack` progress the block entity syncs. They use the existing
  radiant render type the renderer already draws its accents with, not a new shader.
- **The split**: the block state changes to `BROKEN`. The renderer draws the slab as two halves (a second layer
  definition with the slab cut down its vertical centre), each rotated about the base edge of the slab by 45 degrees
  outward, with the Monolith's existing entity-surface shader. The fall animates over the four seconds from upright to
  45 degrees with an ease-out and a small bounce.
- **Rubble**: drawn by the renderer around the base (a ring of a dozen irregular dark facets, the shatter renderer's
  shard look, sitting still), not a new block. Fragments are visible items on it.
- **Fog and lightning**: the Cardinal rite fog renderer's material, drawn as a low disc around the base (radius about
  three blocks, knee height) while the Monolith is broken, with the HutosLib lightning spawner's purple arcs (the
  Axonal gold config recoloured) flickering from the fulcrum every few seconds. The fog stays after the Seed is taken,
  thinner, as the "crumbling" state.
- **The portal panel**: a diamond-shaped quad between the halves (points at the top of the halves, the fulcrum, and
  the two outer edges), drawn with the Lidless Door panel shader; the shader gets a mask so the void fills the diamond
  with a feathered edge and the rest is clear. From the back it is the same.
- **Inside**: the crossing view's far window is replaced in `SEED` mode by the Qliphoth black hole: the view shader
  draws the lensed, swirling black hole on the passage axis, its apparent radius growing with progress (small at the
  start, filling most of the view at the end), with the eyes thinning out around it. The scene behind the player
  (the entry window) stays. Vision distortion is a radial lens warp of the whole view that scales with progress, in
  the same shader. The Seed floats at the hole's centre: drawn in world space on the passage axis at a fixed distance
  ahead of the camera, with the item's existing custom renderer and tendril effects, into a mask target the view
  composites over the void (the Axonal scene-and-mask pattern), so it is never behind the void. Its glow pulses as
  the player nears.
- **Sound**: the door's drone, plus the black hole's existing hum if one is registered; otherwise the drone pitched
  down as the player nears.

## Code shape (my calls; the builder may adjust names)

- `LidlessPassageService` gains `enum Mode { DOOR, SEED }` on the session and a `LidlessPortal` contract the two
  entry blocks implement: which side the player came from, the far cell, whether this player may enter, and what
  happens on completion. The service keeps the pin, input, exits and interruptions; the mode only changes the
  retreat speed and the completion hook. The state payload carries the mode.
- `SanguineMonolithBlock` gains a `BROKEN` boolean property (default false); the block entity gains `breakStart`
  (game time, -1 when whole), `owner` (UUID), `seedTaken` (boolean), all saved and synced. `pressFurther` starts the
  break instead of exploding and refuses a Monolith that is breaking or broken. A server tick on the block entity
  drops the Fragments and sets `BROKEN` when the fall completes. The broken Monolith's column (base and the filler
  above, whose `getCollisionShape` gets a Monolith branch since it ignores the entity today) is empty for the owner
  while the Seed is inside and solid otherwise (the door's `getCollisionShape` rule); `entityInside` on either cell
  offers the owner to the service in `SEED` mode. `playerWillDestroy` of a broken Monolith with the Seed inside drops
  the Seed entity.
- Client: `LidlessPassageView` reads the mode and draws the black hole and warp; a `QliphothSeedFocusRenderer` (world
  effect, called from `WorldEffectDispatcher`) draws the Seed into the mask target while a `SEED` crossing runs.
  `SanguineMonolithRenderer` draws cracks, the halves, rubble, fog disc and the portal diamond by state and progress.

## Tests and review

- GameTests (`lidless_passage_validation` or a new `monolith_walk_validation` holder): the second D7 interaction
  starts the break instead of dropping anything; after 240 ticks the state is `BROKEN`, Fragments lie on the ground
  and no Seed does; the owner's collision in the gap is empty and a second player's is solid; the owner's crossing
  runs in `SEED` mode; retreat speed at the end is a quarter of the start; completion puts the Seed in the inventory
  (and on the ground when full), lands the player on the far side, marks the Seed taken and makes the gap solid; a
  blocked far cell still yields the Seed and lands the player elsewhere; breaking the husk with the Seed inside drops
  the Seed and nothing else; breaking it after drops nothing; a relog mid-crossing returns the player to the entry
  side with the portal still open; the broken Monolith survives a save and reload with owner and seed state;
  `PlaythroughArchonGameTests.pressFurtherShatters` and `shatterBeforeCornerstone` are rewritten for the break.
- JVM tests: the payload registry (the mode field re-lays the state payload: protocol bump), the shader contract
  tests extended for the mode uniform and the mask, the existing guardrails green.
- Review client: `runMonolithWalkReviewClient` captures the whole Monolith, the cracks at half progress, the fallen
  halves with fog and portal from two angles, the crossing at a third and two thirds (black hole growing, Seed
  visible, warp), and the exit with the Seed in hand.
- `alphaCheck` green.

## Non-goals

- No change to the Bloom rite, the Pomes or anything after the Seed is in hand.
- No new sounds, textures or dimension.
- The Lidless Door itself behaves exactly as before.
- The Unstained and other players never enter; no cosmetic for them beyond seeing the broken Monolith and the fog.

## Left to the builder

- Crack branching pattern, fall easing and bounce, rubble facet layout, fog disc size and density, lightning cadence,
  black hole growth curve, warp strength curve, Seed glow pulse, tendril count.
- Whether the halves are one layer definition with two parts or two definitions.
- Which existing hum, if any, plays under the black hole.
- Exact GameTest fixture layout and holder choice.
