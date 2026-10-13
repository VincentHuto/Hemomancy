# Cardinal Rite floors — review and consecration visual, 2026-10-12

## Floor fixes

- **No copper.** All four Working floors used `minecraft:cut_copper`. Copper is Unstained material: it is antiseptic, counts as a White Humor witness, and weathers. Because floor keys match exact blocks, the floors also stopped matching once the copper weathered, and waxed copper never matched. The accent is now `minecraft:bone_block`. The Vicar's Rooted Vein lesson was updated to match.
- **Symmetric sockets that avoid anchors.** Every tier's brazier sockets were rebuilt:
  - The Greater rings were asymmetric in three quadrants.
  - Grand sockets on the axes and diagonals collided with Apotheos ring-4/5 anchors.
  - Sockets also collided with the support-sigil placements of Archon, Ancestral Communion, Bloom of the Qliphoth, Pallid Shadow, Sanctified, Sanguine Dominion and Scarlet Summons.

  Sockets now sit one block outside the floor edge, in pairs flanking the axis and diagonal lanes (`CardinalRiteRingTuning` only ever places anchors on those lanes). Counts are 8/8/8/16. Each tier keeps the rows of the tier below one block further out, so upgrading a floor means sliding each brazier one block outward.
- **Dominion is Overworld-buildable and costs the same iron as Threshold.** The field stone is now `#hemomancy:cardinal_floor_dominion_stone`: deepslate tiles, bricks or polished deepslate, with polished blackstone bricks still accepted. Chiseled hematic iron studs number 4/8/12/16, matching Threshold's cross, down from 0/16/16/48. The nested star pattern keeps every tier's centre equal to the tier below.
- **Threshold accepts weathered bricks.** It uses `#hemomancy:cardinal_floor_threshold_stone`: stone, mossy, cracked and chiseled stone bricks.
- **Unchanged after review.**
  - The minor floors stay. `CardinalRiteLayeredRecipeDataTest` requires every style × tier, and they are the base of each nested pattern.
  - Founding Fane's Grand Dominion requirement is taught explicitly by the Vicar, so it was kept.
  - Hematic Unbinding (a greater rite on a lesser Working floor) was left as authored.

## Consecration visual

The Cardinal Focus now ticks:

- **Server:** every 20 ticks, staggered by position, `CardinalRiteStationMatcher.formedFloor` finds the highest-tier unambiguous floor and its orientation. The result is saved and synced on the block entity. Forming plays a sculk-bloom and heartbeat; breaking plays a sculk break. The check is visual only, and rite matching is unchanged.
- **Client:** `CardinalFloorConsecrationRenderer`, called from `CardinalFocusRenderer`, draws three layers:
  - **Membrane.** One `cardinal_floor_membrane` shader quad per floor block, with UVs in block offsets from the Focus centre, so the surface is continuous across block seams. It shows:
    - a ragged spreading edge with a hot front;
    - warped tissue, with arteries and capillaries traced from cellular noise;
    - the style's motif restated as raised vessels: the threshold cross, dominion seal rings and spokes, communion weave, or working bone ribs and sinew;
    - a heartbeat pulse travelling out from the Focus and a darker rim.
  - **Formation tendrils.** On formation, HutosLib tendrils race from the Focus to every socket.
  - **Socket rings.** Emissive rings mark every socket that has no brazier yet.
- **Particles:** all Hemomancy/HutosLib (`ember`, `bloodCell`, `glow`): sparks on the spreading edge, surfacing blood cells, and a heartbeat glow at the Focus.
- **Lifecycle:** a broken floor's membrane withers over 24 ticks. Timing and palettes live in the pure `CardinalRiteFloorVisualRules`.

## Client sync

Floor definitions used to reach clients only through shared statics, so blueprints, rite hints and the Rites tab were empty on dedicated servers. `CardinalRiteFloorSync` now sends each floor's source JSON on `OnDatapackSyncEvent`. Remote clients parse it with the server's loader; integrated clients ignore it.

## Verification

- JVM: `CardinalRiteFloorDesignDataTest` covers the copper ban, anchor lanes, socket symmetry and clearance, outward slide, nested centres and iron parity. `CardinalRiteFloorVisualRulesTest` and `CardinalRiteLayeredRecipeDataTest` also pass. The full `test` task has 16 failures that were already present and are unrelated: model generators, mojibake, missing lang/texture, legacy suite count, and a non-UTF-8 `docs/TESTING.md`.
- Native client: `gradlew.bat -I build/rite-floor-world.init.gradle runRiteFloorReviewClient` (`RiteFloorClientReview`) builds one greater floor of each style. It captures `build/rite-floor-client-review/screenshots/rite-floor-*.png` during formation, settled, in close-up, at night, and while dissolving. Restore the world from `build/brazier-client-review/saves/BrazierReview` before each run so the formation replays.
  - **Pass 1:** the shader compiled and the membrane spread, but there were four problems:
    - the Focus read as a black hole;
    - the vessels looked like uniform cracks;
    - the socket rings were faint olive smudges on grass;
    - the bone membrane on Working let the block grid show.
  - **Pass 2:**
    - the membrane pools in the Focus basin at y 0.754, below the floating medium, with vessels converging on its rim;
    - vessels are tapered and more strongly warped;
    - alpha is 0.93;
    - each socket gets a stain, a blood ring and a beating inner edge.

    Working's square ribs still read as a grid.
  - **Pass 3:** Working's ribs became wavy pale bone lamellae under a red sinew cross. All four styles now read as continuous surfaces, and breaking a Working floor block withers its membrane.
  - **Pass 4 (user follow-up):**
    - **Irregular edges.** The membrane also draws on the ring of blocks just outside the floor, but only where the block has a sturdy top and the space above is open. The shader cuts a crisp, noise-driven boundary that spills up to about 0.9 of a block onto the ground and bites back up to about 0.65 of a block. A dark scab lines that edge, and the forming front is ragged rather than blurred.
    - **Focus collar.** Formation tendrils root on a ring of radius 0.5 around the Focus, facing their sockets, instead of one crowded point. A collar layer covers that ring: dark radial folds with a pulsing red inner lip. The basin pool and medium stay open in the centre, and a rim layer covers the Focus's own top corners.
    - **Quad sorting off.** The membrane render type no longer sorts quads. Sorting by distance had drawn nearer floor cells over the collar. These quads never overlap, so submission order keeps the collar on top.
  - **Pass 5 (user follow-up):** the folded sphincter and pulsing lip read too anatomical. The collar is now a crisp round hematic iron rim around the basin, a braided band of two twisted vessels on dark clot, and short root strands merging into the membrane.
  - **Pass 6 (user follow-up):** the flat collar became `CardinalFocusBraidRenderer`. It draws three tube plies (96 segments by 8 sides) twisted six times around a ring of radius 0.56 at y 1.075, using `entityCutoutNoCull` on `dura_membrane.png` tinted with the floor's vein colour. Real normals and the light above the Focus shade it. The twist creeps along the ring, the plies and ring undulate, and strands swell with the heartbeat. It grows in over 20 ticks (`braidGrowth`) and shrinks with the dissolve. The shader collar is now only its bed: a contact-shadow clot, the iron rim and the roots.
  - **Pass 7 (user follow-up):**
    - **Drapes over ledges.** Every covered block side whose neighbour is neither covered nor solid gets a hanging sheet up to 0.9 of a block deep. Its UVs continue outward from the lip, so the pattern and the wandering edge decide where it ends. The vertex red channel carries how far down it hangs, which the shader uses for a ragged hem with occasional drips and some shading.
    - **No floating rings.** Socket rings are hidden when the socket has no sturdy ground beneath it.
    - **Harness.** The review now digs a pit beside the threshold floor and adds a ledge shot.
  - **Pass 8 (user follow-up):** the braid has four distinct strands, spaced a quarter turn apart:
    - artery: radius 0.058, red, swell 0.26;
    - vein: 0.036, dark blue;
    - nerve: 0.030, myelin texture, with a ridged cross-section, fine tremor and occasional twitches;
    - lymph: 0.044, dark green, beaded along its length and lumpy around it.

    To stop the braid looking unlit and pasted on, the tints are muted and each vertex bakes occlusion, darker where it faces into the bundle or downward. The contact shadow is wider in the collar bed and continues into the membrane. A low close-up shot was added to the harness.
  - **Pass 9 (user follow-up, "feels out of place"):** the diagnosis was that the ring sat on the membrane rather than in it, its colours appeared nowhere else, it was lit while the membrane was self-lit, and it was too regular. Changes:
    - **Dives.** Each strand sinks 0.17 and swings 0.14 outward into the floor at `CardinalRiteFloorVisualRules.braidDive`: strand `i` dives at `i * PI/4` and opposite, so the eight dives are evenly spaced.
    - **Tributaries.** The shader continues each strand from its dive angles as a meandering tributary in the strand's colour, fading by about 2.5 blocks: a beaded lymph, a wavering nerve.
    - **Shared light.** A new `FloorLight` uniform is the brighter of block light and sky light times `getSkyDarken`. It dims the membrane, while vessels keep a minimum glow of 0.62. The braid gets at least block light 7, and socket rings dim with the same light.
  - **Pass 10 (user follow-up):**
    - **Mound.** `CardinalRiteFloorVisualRules.moundHeight` describes a swelling: a lip at the iron rim (radius 0.40), a crest of 0.11 at radius 0.58 under the braid, and a cosine settle to zero at `MOUND_OUTER` 1.45. It is lumpy around its circumference and bulges where strands dive.
    - **Mesh and shading.** The renderer draws it as an 18 x 72 polar mesh using the same membrane shader and continuous UVs. Green 128 marks mound vertices, and red carries slope shading from finite-difference normals.
    - **Depth.** The mound has its own depth-writing render type (`cardinalFloorMound`), so the braid (ring now at y 1.125, half-sunk) and the tendril roots are hidden inside it.
    - **Seam.** The flat membrane and the mound split at the same per-pixel circle. The mound mesh runs a flat 0.04 skirt past it so polygon chords cannot leave slivers. The old flat collar and Focus-rim layers are retired, and the mound carries the iron rim.
    - **Drape gap.** Drapes now start at the surface height, 0.002 off the face, and the surface quad reaches out by the same amount over draped sides. This closes the bright sliver the user saw at ledges. A grazing ledge shot was added to the harness.
  - **Pass 11 (user follow-up):** the socket markers are now organic pads drawn by the membrane shader instead of glowing rings. Green 32 marks a socket quad (half-width 0.6), and red and blue carry the position inside it. Each pad gets:
    - a lobed outline, seeded per socket;
    - rootlets running into the ground;
    - membrane tissue warmed toward the vein colour, with capillaries;
    - a dark clot ringed by a wet lip that pulses with the heartbeat;
    - the shared world-light dimming.

    Pads still open in turn and skip braziers and drops.
  - **Pass 12 (user follow-up):** the separate socket pads are replaced by one continuous brazier gash.
    - **Path.** The renderer sorts the sockets by angle and runs a closed Catmull-Rom curve through them, with 12 samples per span. It draws a strip of half-width 0.55. Red carries the position across the strip, blue swells toward each socket (Gaussian, 0.65), and the reveal sweeps around the loop.
    - **Ground.** Stretches without sturdy, open ground are skipped.
    - **Look.** The shader's `gash` mode draws ragged flesh lips, a wet blood channel with a slow glossy sheen and a faint centre-line highlight, and creeping capillaries. At each socket it heaps up a lumpy, glistening clot pool, replacing the old dark hole-like clot.
  - **Pass 13 (user follow-up):**
    - **Braid off.** The braid is toggled off by `SHOW_FOCUS_BRAID = false`, and the mound goes with it, since it exists to seat the braid. The flat membrane carries blue 255 or 191, so the shader cuts it away only when the mound is drawn.
    - **Focus pad.** The Focus mouth wears the old socket-pad look, scaled 2.45x (quad half-width 1.45, green 64): ragged rim, rootlets and a wet lip. Its round clot opening (about 0.4 block) sits just inside the basin, which shows through as a dark clot. The medium stays visible.
    - **Tendrils and gash.** Formation tendrils root at y 0.99, under the surface. The gash uses green 8.
    - **Fix along the way.** The first pad attempt used square metrics for the lip and shading, and read as a dark square frame. It now uses the socket pad's radial proportions.
  - **Pass 14 (user follow-up, "carved out of the ground"):** the gash is now a parallax trench.
    - **View ray.** The vertex shader passes the camera-relative `Position` as `viewOffset`. Inside the opening, each pixel follows the view ray down to a virtual V-trench, 0.2 deep plus 0.14 x swell, with its floor 45% of the opening's width.
    - **Trench axis.** The sideways axis is solved from screen-space derivatives of the across coordinate, computed before any discard.
    - **Floor.** Rays that reach the floor show pooled blood and clots, darker against the walls.
    - **Walls.** Rays that hit a sloping wall show a band of torn earth over striated flesh, darkening with depth, with an occlusion shadow under the lip.
    - **Outside the cut.** A dark crust, a ground shadow and the capillaries.
    - **First pass.** The 0.32-deep trench showed only a smooth wall gradient and read as a raised tube. It was made shallower, and the soil band and striations were added.
  - **Pass 15 (user follow-up):**
    - **Seams.** The trench showed seams at every section. The shader had been solving the trench's sideways axis from screen-space derivatives of the across coordinate, which jump at each quad boundary and diagonal. Gash vertices now carry the exact sideways normal in their texture coordinates, shared at each join. The shader rebuilds the ground position from the camera-relative position and a new `FocusOffset` uniform (the Focus centre minus the camera).
    - **Bigger trench.** Strip half-width is 0.8, the opening edge is 0.24 plus 0.16 x swell, and depth is 0.34 plus 0.18 x swell.
    - **Vein depth.** The floor's main vessels are sunk into the membrane. A `groove` profile gives shaded walls, a dark lip and a wet centre highlight, for arteries, each style's laid lines (threshold cross, dominion rings, working sinew) and tributaries. The blood inside is sampled with a small view parallax.
  - **Pass 16 (user follow-up):** where the trench is broken by a gap, its ends taper shut.
    - **Renderer.** It marks each grounded sample next to a gap as an end, then gives every sample a taper of smoothstep(arc distance to the nearest end / 0.8). The taper rides in the length of the sideways vector in the texture coordinates (minimum 0.05).
    - **Shader.** The opening edge scales by the taper, depth by mix(0.3, 1, taper), and the crust, shadow and capillaries fade, so the cut closes to a tip.
    - **Harness.** A shot of the trench ending at the pit was added.
  - **Pass 17 (user follow-up):** the trench walls' pattern drifted as the camera moved.
    - **Cause.** The edge noise that sets the trench width was sampled at the surface pixel, so different view rays reaching the same wall point traced different trench widths. The wall streaks were also keyed to a coordinate built from that pixel's interpolated side vector.
    - **Fix.** `trenchEdge` now samples the noise on the trench's centre line (pixel minus side x across). The wall intersection (`wallHit`) is solved twice, refining the width at the stretch of trench the ray actually reaches (centre plus along-trench drop x t). Wall soil and striae are keyed to that centre-line site and the depth t, and the floor width is taken at the floor's own centre-line point.
    - **Harness.** A second, sideways-shifted view of the same trench was added for comparison.
  - **Pass 18 (user follow-up):** a placed brazier seats into the trench.
    - **Closing.** Each socket holding an Iron Brazier on sturdy ground becomes a plug. The trench taper is capped by the distance to the nearest plug: shut within 0.35, fully open by 0.9.
    - **Plug.** A plug quad (green 24, half-width 0.6, 0.022 above the trench) draws `brazierPlug`, a low dome of clotted, veined flesh with a ragged rim, one-sided shading and a wet sheen on top.
    - **Layer thresholds.** Gash below 0.06, plug 0.06 to 0.12, Focus pad 0.12 to 0.35.
  - **Pass 19 (user experiment):** `EXTEND_TO_GASH`, on for now.
    - **Shared loop.** The trench centre line is built once as a `GashLoop` and shared by the membrane and the trench.
    - **Coverage.** The membrane covers every grounded block (sturdy top, open above) within half+4 that has a corner inside the loop. Each flat vertex encodes its signed distance inside the centre line in blue (0.5 plus 0.1 x distance, clamped to ±1 block). The shader discards below 0.5 and skips the laid floor's wandering edge, scab and spill there.
    - **Edges.** Drapes still hang wherever covered blocks meet a drop, and the trench's inner lip covers the seam.
  - **Pass 20 (user follow-up):** `EXTEND_TO_GASH` stays on.
    - **Cache.** Each Focus's `GashLoop` is now cached by Focus position and rebuilt only when its floor id or orientation changes (cleared above 128 entries). The loop memoises signed distances on a 1/256-block grid, so the per-frame polygon tests are gone after the first frame.
    - **Overhangs.** Drapes were flagged as fully inside the loop, so blocks beyond the trench whose top membrane was trimmed still grew detached sheets down their faces. Each drape edge now carries the loop distance of its top corner, so it is trimmed on exactly the same line as the surface above it.
  - **Pass 21 (user follow-up, small gaps):**
    - **Slivers.** Under the extension, a drape now hangs only if both top corners of its face are inside the trench line, so it is never trimmed to a blade.
    - **Drape ends.** Drapes are built in up to three strips. An end that does not carry on (around the corner on the same block, or along the ledge onto the next block's drape) curls its hem up to nothing over 0.35. Green on drape vertices runs from 0.78 at a curled end to 1, and the shader scales the hem by it.
    - **Webbing over drops.** The trench strip's reach on each side is clipped to ground: the widest of 0.8/0.62/0.48/0.36 whose end and midpoint are both on sturdy ground. Red is rescaled so the shader's across distances stay true, so the lip shadow and capillaries no longer float out over cliffs.
    - **Harness.** A one-block notch and a cliff just past the trench were added east of the working floor, with shots of both.
  - **Pass 22 (user follow-up):** edges only partly inside the trench line got no drape, so the membrane stopped dead at the ledge.
    - **Span.** `insideSpan` finds the inside part of each top edge, bisecting the crossing with cached loop distances, and the drape hangs over just that span. An end cut short by the line always curls, with the taper capped at 45% of the span, and spans under 8% are skipped.
    - **No trimming.** Drape blue is now a constant inside value, so drapes are confined only by their span and are never cut into slivers.
  - **Pass 23 (user follow-ups):**
    - **NW seam wedge.** A thin wedge showed in each trench's north-west corner. The loop is sorted by angle, so it starts and ends at the first socket clockwise from due west, and the first and last sections took one-sided normals there. Neighbour lookups now wrap around the closed loop.
    - **Seat mounds.** `drawGash` returns the seated sockets, and `drawSeats` draws a depth-writing 12 x 56 polar mound around each one (green 166, red slope shading): radius 0.1 to 0.55, peak 0.24 at the stem and shoulder 0.14 over the dais, easing down to the plug. It is lumpy and wrinkled, and swells slightly on the heartbeat. The shader's `brazierSeat` mode gives it the floor's darkened, veined skin. The mound flag range is now 0.35 to 0.58.
    - **Harness.** Shots of a trench seam and a seated brazier close-up were added.
  - **Pass 24 (user follow-up):** the trench no longer pinches shut at a seated brazier.
    - **Detour.** `GashLoop.through` now takes the seated sockets and swings the centre line radially outward around each one, by 0.85 x exp(-d² / 1.0²), so the trench wraps around the outside of the mound like a moat.
    - **Membrane.** It follows the detoured line, so it fills up to the mound, which sits on the inner bank.
    - **Caching and width.** The loop cache key now includes the seated set. Only empty sockets widen the trench, and the plug-closing taper (`plugOpening`) was removed.
  - **Pass 25 (user follow-up):** the seat mound read as a separate object, with a different texture, a height step and a peeking plug disc.
    - **Same shading.** The shader's `seat` flag now runs the full membrane shading with the floor's world-continuous UVs, scaled by slope shade, so the veins run straight over it. Its body is opaque.
    - **Rim.** It rises from the floor surface (y = SURFACE_Y plus its height) to an outer radius of 0.7, fading in over its outer 30% through vertex alpha.
    - **Cleanup.** The plug quads, `brazierPlug` and the separate `brazierSeat` shading were removed.
  - **Pass 26 (user follow-up):** collapse mirrors formation.
    - **Rules.** `COLLAPSE_TICKS` = `FORM_TICKS`. `collapseAge(t)` maps time since breaking to a reversed formation age, so spread, front glow, trench reveal and Focus-pad growth all run backwards. The old `DISSOLVE_TICKS` and `dissolveAlpha` were removed.
    - **Seat mounds.** `seatRise(spread, distance)` raises a seated brazier's mound only once the membrane's edge reaches it, and sinks it as the edge withdraws.
    - **Client timing.** On every floor change, the Focus block entity keeps the old floor collapsing from the extent it had reached. A new floor's client-side formation start waits for the collapse to finish, and a floor repaired mid-collapse resumes from the current extent.
    - **Particles.** Edge sparks drift inward during a collapse.
  - **Pass 27 (user follow-up):** the trench no longer pops in and out section by section.
    - **Dig front.** `digProgress(age, sample, samples)` runs a front once around the loop over the second half of formation, carving each point over `DIG_TICKS` (6). The carve value caps the trench taper, so the cut opens from a closed seam to full width and depth. Collapse replays it backwards through `collapseAge`.
    - **Glow.** `digGlow` = 4p(1-p) travels in the trench's vertex alpha (255 minus 190 x glow; the trench is opaque). The shader draws a bright seam down the line, lights the crust it is cutting, and glows the fresh walls and floor behind the front.
    - **Cleanup.** `socketReveal` was retired.
    - **Harness.** The first three wide shots are now timed through formation to catch the front.
  - **Pass 28 (user follow-up):** the trench started from a flat end where the dig front begins, at the loop's north-west seam. Carving is now also scaled by `digTail` (a smoothstep over the first 0.8 blocks of arc from the start) until the front completes the loop, which is when the last sample's dig progress reaches 1. The start pinches to a closed tip, and the last stretch to fill during a collapse pinches shut the same way. One early harness shot now watches the seam during formation.
  - **Pass 29 (user experiment):** `DIG_BLOCK_DUST`, on for now. Once per game tick per Focus, three vanilla `BLOCK` break particles of the ground block burst up from random trench samples mid-carve (dig progress 0.1 to 0.9). They also appear while a collapse fills the trench. This is a deliberate exception to the custom-particle rule, at the user's request, for evaluation.
  - **Pass 30 (user follow-up):** cut lines showed where the trench met a brazier mound. The mound's faded rim (out to `SEAT_OUTER` 0.7) overlapped the trench's inner lip, which sat about 0.37 inside the line at a 0.85 detour, and its polygon edges showed across the trench. The detour is now 1.1 with a spread of 1.3, so the lip stays at least about 0.7 from the socket along the whole bend. Render bounds grew to half-width + 5.
  - **Pass 31 (user follow-up):** cut lines still showed from low side angles. They were the trench strip folding over itself: on the inside of the tight bends around seated braziers, its 0.8 half-width exceeded the bend's radius of curvature. `gashBendLimits` takes each sample's circumradius from its neighbours and caps the inner-side reach at 0.85 of it (minimum 0.2), min-smoothed over ±2 samples. `gashReach` applies the cap. A low side-on shot of a brazier bend was added to the harness.
  - **Pass 32 (user follow-up, "tenting"):** creased ridges remained inside the trench at bends. They came from two approximations in the parallax trench.
    - **Sections too long.** The trench's sideways direction and across coordinate are interpolated linearly per triangle, and at tight bends the sections were long enough to crease along their diagonals. `GASH_STEPS` went from 12 to 28 samples per span, and the bend-limit smoothing window widened to ±5.
    - **Looking too far ahead.** At grazing angles the view ray reached up to about 4 blocks along a trench it treats as straight. The along-trench component of the ray is now clamped to 0.6 blocks.
    - **Cost.** Per-sample normals and ground-limited reaches are computed once per sample rather than per strip corner. A steep close shot into a brazier bend was added.
  - **Pass 33 (user follow-up):** the trench walls read as stretched vertical lines. Their noise varied quickly along the trench and barely with depth.
    - **New wall.** The walls now use `trenchWall(site, v, t)`, built on a new `noise3` 3D value noise over (site.x, depth in blocks, site.y), so they stay world-stable and vary in both directions.
    - **Layers.** Crumbling topsoil (0.04 to 0.08 deep) with grain, pebbles and sparse hanging roots; a lumpy pinkish fat band (0.02 to 0.045); muscle with fibres running along the cut, bundled into layers with fascia sheaths.
    - **Detail.** Severed vessel ends with wet rims and short drips, a winding mid-wall vein that swells with the heartbeat, wet glints, and depth darkening.
    - **Tuning.** The first pass had soil and fat too thick, a too-pale fat band and scribbly root contours. Those were thinned and toned down.
  - **Pass 34 (user follow-up):**
    - **Ground-coloured topsoil.** The band takes the average colour of the block each wall cuts through: the particle sprite's original image, averaging opaque pixels, cached per BlockState and sampled 0.3 into each side.
      - Every vertex channel was taken, so trench UVs now pack data: x is the taper step x 8 plus the side angle, and y is the colour as an exact 24-bit integer.
      - The vertex shader unpacks these into new `gashSide` / `gashLean` / `soilTint` varyings, so they interpolate cleanly.
      - `trenchWall` tints its grain and stones with the colour, falling back to dirt when it is unknown.
    - **Softer blood edge.** The trench floor and walls are both shaded and blended across a ragged, noise-shifted shoreline, instead of a hard branch at the floor's edge, and a soak stain climbs the lower wall.
    - **Harness.** The dominion floor now sits on stone, with a close shot of its trench.
  - **Pass 35 (user follow-up):**
    - **Dark band at ledges.** A wall's ground sample next to a ledge landed in air. It returned 0 (black), and interpolating toward that darkened the topsoil band. `groundColour` now also rejects non-sturdy tops. A missing side falls back to the block under the trench centre, then the other wall's block, then a default earth brown.
    - **Tan band.** The tan fat band read as dirt under any ground. It is now a thin (0.008 to 0.018) glossy membrane in the floor's vein colours, so the cut runs from the ground block straight into flesh.
