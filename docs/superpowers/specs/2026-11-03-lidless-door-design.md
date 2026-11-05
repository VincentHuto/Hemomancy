# Lidless Door: a 1x2 door that is a sixteen-block crossing

## Why

A toy, built for the fun of it: a door-like block that the player steps into and walks ten to twenty blocks
through, yet in the real world they only ever pass through its one-block-deep frame. A non-Euclidean hallway.
It is creative-only for now and belongs to no path; the user may give it a purpose later, so nothing here ties it
to blood, degree, rites or a side.

The name "Lidless Door" and the crossing's name "the Lidless Passage" are my calls (eyes that never close); rename
freely, the ids follow.

## What the player sees and does (user's decisions)

- The door is a normal-sized door: one block wide, two tall, placed like a vanilla door and usable from either side.
- Looking at the door from outside, the doorway is a window onto a void: blacks and reds, organic shader motion in
  the Monolith family, a vortex or a magical realm that looks back at you, eyes blinking open and shut in the dark.
  The void is always there; the door has no open or closed state.
- Stepping into the door puts you in that void. You walk a set distance across it (config, default 16 blocks) and
  step out the far side of the same door, one block from where you went in.
- A friend watching from outside sees you simply vanish into the door; nobody stands in the doorway, and nothing
  comes out until you step through the far side.
- Creative-only: no recipe, no loot from anything, no progression gate. It sits on the WIP creative tab.

## How it feels (my calls, approved in chat)

- You cross at walking pace: a 16-block crossing is about three seconds. Sprinting is faster, sneaking slower.
- Mouse look is free. Forward input carries you toward the exit, backward input toward the entry. Letting go of
  the keys leaves you hanging in the void; nothing pushes you either way.
- Turning around shows the door you came through as a lit rectangle receding behind you with the real world visible
  inside it. Ahead, the exit is the same rectangle growing as you approach. The rectangle is the only thing in the
  void that is not void.
- Walking all the way back puts you out the side you entered from. Walking all the way forward puts you out the
  far side.
- The crossing is seamless: no loading screen, no dimension change, no "Loading terrain" flash.
- While crossing you are invisible and silent to everyone else, immune to damage, dropped as a target by every
  mob, and cannot use items, attack or break blocks. Opening a screen (inventory, chat) pauses your input but
  not the void.
- Mobs, items and other non-player entities cannot use the door: the panel is solid to them. Spectators pass
  through it as through any block.

## Rules (my calls)

- **Entering.** A living, non-spectator player who is not a passenger or vehicle and is not in an Axonal
  Transduction crossing enters the moment their bounding box overlaps the door panel (either half). Elytra flight
  stops on entry. The side they came from is read from which side of the door plane their feet are on at that
  moment and is frozen for the crossing. There is no cooldown: stepping back into the door from the far side starts
  a new crossing the other way.
- **Pinning.** For the whole crossing the player is held at the door's centre on the plane of the panel, feet at
  the lower block's floor, with no gravity, no physics and no velocity, the way Axonal Transduction holds a
  traveller. Position drift beyond a small tolerance ends the crossing at the entry side (anti-cheat and
  anti-glitch, same as Axonal).
- **Progress** is a number from 0 to the configured length. Forward input adds the player's ground speed per tick
  (vanilla walking is 4.317 blocks a second; use the player's movement speed attribute scaled the same way vanilla
  does, so speed effects count), sprinting multiplies by 1.3, sneaking by 0.3. Backward input subtracts the same.
  Input older than four ticks counts as released.
- **Leaving.** Progress reaching the length places the player standing in the cell beyond the far side of the
  door, centred, keeping their look. Progress reaching 0 does the same on the entry side. A landing needs a safe
  standing space (no collision, no fluid, inside the border); if the far cell is unsafe the crossing holds at the
  end and the player can only walk back. If the entry cell is unsafe too (it was open when they entered, so this is
  rare), search the cells around the door for a standing space as Axonal's `safeLanding` does, and as a last resort
  leave them in the door cell itself.
- **Interruptions.** The door being broken or replaced, the player dying, changing dimension or logging out, or
  the server stopping, ends the crossing. Death and dimension change end it in place; everything else returns the
  player to the entry side through the landing rules. Logout returns them before the player is saved, so no
  crossing survives a relog.
- **The hold** reuses the Axonal hold in full, not only its events. The five Axonal mixins (`MixinEntity`'s
  cancelled `move`, `MixinAxonalLivingEntity`'s unpickable, unpushable, unseen traveller, `MixinParalysisPlayer`'s
  cancelled `touch`, `MixinLocalPlayer` and `MixinParalysisServerInput`'s rejected drops, container clicks and
  stripped movement packets), `Paralysis.blocksActions` and the Naeglerophaeon's traveller check all ask one shared
  "held traveller" predicate in `common/util` instead of `AxonalTransductionManager.isTraveling` directly. On the
  server it reads Axonal's state or the passage map; on the client it reads Axonal's mirror or the crossing-id set
  through `ClientBridge`. Axonal's `canEnter` refuses a player who is crossing a door. So the inventory behaves as
  it does in Axonal: a screen may open and be looked at, but nothing can be dropped, moved or clicked, and ground
  items are not picked up.
- **The HUD is hidden** while crossing (hotbar, crosshair, health, blood HUD, every layer), the way a boss scene
  hides it through `BossSceneDirector`'s HUD hold or the same mechanism; chat still works. The void is bare.
- **Several players** may cross the same door at once, each with their own progress and direction. They do not
  see each other in the void.
- **No cost, no gate.** The door reads no blood, no degree, no purity and writes no player attachment. It leaves
  nothing durable on the player: the crossing is session-only server state (a UUID-keyed map registered with
  `TransientServerStateEvents`), and the client learns who is crossing from a state payload, not from persistent
  data. No `getPersistentData` call anywhere in the system.

## The block (my calls)

- `lidless_door`, a `LidlessDoorBlock` in `common/block/` with `FACING` (horizontal) and `HALF` (upper/lower),
  placed and broken as a pair like a door, but it is not a `DoorBlock`: no open state, no hinge, no redstone. The
  item places the lower half when the upper cell is free, the way door items do.
- Shape: a panel 2/16 thick across the block's centre, perpendicular to `FACING`, within a frame. The outline
  shape is that panel for both halves.
- Collision: empty for a player who could cross right now (alive, not a spectator, not a passenger or vehicle,
  not mid-Axonal); the panel for everyone and everything else, so a rider, a horse, a zombie or a dropped item is
  stopped by the door. (`getCollisionShape` reads the entity from the collision context, as powder snow does, and
  asks the same eligibility rule the service uses.)
- `entityInside` on the server, for a `ServerPlayer`, offers the player to the passage service. Nothing else
  happens in the block.
- Light level 7. Hardness like an iron door. No loot table: creative-only means a survival break drops nothing.
- The lower half has a `LidlessDoorBlockEntity` (no data; a plain `BlockEntity`, no sync) so the panel can be
  drawn by a block-entity renderer. Its render bounds cover both halves.
- Frame look: hematic iron and bone, no copper, brass or bronze (standing rule for blood-side gear). Reuse the
  hematic iron door's textures for the jambs and lintel; the builder may add a 16x16 item icon generated by a
  script under `tools/model_export/` (version-controlled) rather than hand-painted. Block and item models are
  authored by hand under `src/main/resources` (a two-half door is not something the datagen authors).
- Creative tab: the WIP list in `CreativeTabInit`. The frozen identifier fixture under
  `src/gameTest/resources/frozen` is updated for the new block, item, block entity and payload ids.

## The void (my calls)

The same void is drawn in two places and must read as the same place:

1. **The door panel**, seen from outside, by the block-entity renderer: one quad per face of the panel (front and
   back), drawn with a world shader `lidless_door_panel` through a `SeededRenderTypes` holder keyed by the door's
   seed (a hash of its position). The fragment shader casts a ray from the camera through the panel point and
   shades the void along that ray, so the void has parallax: moving past the door shows depth behind it, like a
   window.
2. **The crossing view**, seen by the crossing player, as a full-screen pass in `ScreenOverlayEvents` next to
   `AxonalTravelView.renderGrade`: the main framebuffer is copied to a scene target (the Axonal pattern), then a
   screen shader `lidless_passage_view` draws a full-screen quad. For each pixel it reconstructs the view ray from
   the camera's projection (uniforms: the camera basis in passage space, the vertical field of view tangent and the
   aspect), shades the void along that ray, and composites the scene texture inside the two doorway windows.

Shared void field: one GLSL include under `assets/hemomancy/shaders/include/noise/` (it must read no uniforms and
import nothing, per the shader-include guardrail; every value is a parameter) holding the functions both shaders
call: the void colour for a ray, and the eye field. Each shader imports the Monolith noise include before it, in
dependency order.

What the void field draws, in order of importance:

- **Ground**: near-black with a dark red grain, built from the Monolith noise `fbm` layered in the ray direction,
  drifting slowly with time. Deep, not flat: two or three noise layers at different scales moved at different
  rates so the void has depth.
- **Vortex**: the field twists around the passage axis (toward the exit). The twist tightens with time and, in
  the crossing view, with the player's own progress, so walking feels like moving into the spiral. Red veins in
  the Monolith style run along the twist.
- **Eyes**: a cellular field on the far sphere. Each cell holds at most one eye (a hash decides whether the cell has
  one, about one cell in four). An eye is an almond of dark red sclera with a black slit pupil that points at the
  viewer (the iris is offset toward the ray's centre). Each eye blinks on its own hashed cycle, open for a few
  seconds, closed for a moment; some stay shut for long stretches and open as you look. Eyes near the passage axis
  ahead are larger and rarer than those at the periphery.
- **Doorway windows** (crossing view only): the entry rectangle at distance `progress` behind the player and the
  exit rectangle at distance `length - progress` ahead, each 1 wide and 2 tall, perpendicular to the passage axis,
  with the player's eye at the door's eye height (so the rectangle's bottom is about 1.62 below the eye and its top
  about 0.38 above). A pixel whose ray hits a rectangle shows the scene texture at that pixel (the camera really is
  in the door, so the world it sees in that direction is the right world); its edge is feathered and warped by the
  void, and a thin red rim marks the frame. At the moment of entry and exit the window fills the screen, so the
  transition into and out of the void needs no fade.
- Palette: the Monolith fragment shader's blacks and reds (its `red` and `black` constants), red vein glow as its
  `veinGlow`. No other hue except the eyes' slightly brighter red iris.

Client state for the crossing player (`client/event/LidlessPassageClientEvents`, the Axonal client as the
pattern): force first person while crossing and restore the camera after; cancel hand rendering, block screen
overlays and interaction key presses; zero the movement input after sending it; send the input payload every
tick; mirror the pin locally so the client does not fight the server's position. For every crossing player,
cancel `RenderPlayerEvent.Pre`. The client interpolates progress between the last two synced values with the
partial tick for the view. A client-side set of crossing entity ids is cleared in `ClientSessionEvents` on logout
and when the level changes.

Sound: a low loop while crossing from an existing registered ambience (the builder picks the nearest drone in
`SoundInit`; no new audio assets), and a soft entry and exit sound from an existing one. Entry and exit scatter a
few dark-red glow motes through `HemoParticleData` on the door's face (never a vanilla particle).

## Networking (my calls)

Two payloads under `common/network/lidless/`, registered in `PacketHandler` in their real direction, the protocol
version bumped (34 to 35) and `src/test/resources/network/payload-registry.txt` rewritten:

- `LidlessPassageStatePacket` (clientbound): dimension, player entity id, token, active, door position, door
  facing, whether the player is heading toward the door's front, progress, length. Sent to the crossing player and
  everyone tracking them when a crossing starts and ends, to the crossing player alone every tick while it runs,
  and to a player who starts tracking a crossing player.
- `LidlessPassageInputPacket` (serverbound): token, direction (-1, 0, 1), sprinting, sneaking. Validated against
  the session token; paced the same way `AxonalInputPacket` is.

Both decode, validate and call one method of `LidlessPassageService` (`common/block/` beside the block, or
`common/util` if the builder prefers; not under `common/network`).

## Config

`HemoServerConfig`: `LIDLESS_PASSAGE_LENGTH`, integer blocks, default 16, range 4 to 64. Synced to the client
through the state payload, so the client never reads the server config.

## Tests and review

- GameTests in a new `lidless_passage_validation` holder (fixtures in the `AxonalTransductionGameTests` style,
  calling the service's `tick` and `input` directly): enter from the front pins the player and syncs an active
  state; forward input raises progress by walking speed; sprint and sneak scale it; completion lands the player in
  the far cell facing wherever they looked; walking back lands at the entry side; a blocked far cell holds at the
  end; a broken door mid-crossing returns the player to the entry side; death ends it in place; a spectator does
  not enter; a zombie's collision shape is the panel and a player's is empty; a crossing survives neither logout
  nor server stop (the maps are cleared).
- JVM tests that must stay green: payload registry, `FrozenIdentifierGameTests`' fixture, `HardCodedEnglish`,
  `EnglishLiteralAllowList` (add no literals), `VanillaParticleRatchet` (add no vanilla particles),
  `PersistentDataAllowList` (unchanged), `ShaderNoiseIncludeGuardrailTest` (the include reads no uniforms),
  `RenderTypeCachingGuardrailTest`, `HudAndWorldDrawingGuardrailTest` (the panel is a block-entity renderer, the
  view goes through the existing overlay path), `CommonClientIsolationTest`.
- A review client row `lidlessReviewClient` in `gradle/review-clients.gradle` with a client-side script
  (`LidlessDoorClientReview` in `src/gameTest`) that places a door, screenshots it from outside at two angles,
  walks in, screenshots looking ahead at one third and two thirds, turns and screenshots looking back, and walks
  out the far side. The launch proves the two shaders compile on the GPU (offline validators miss driver errors).
- `./gradlew.bat alphaCheck` green.

## Docs

- A short entry in `docs/HEMOMANCY_REFERENCE.md` in the blocks section describing the door as a creative-only
  curiosity, and an open question in `docs/OPEN_QUESTIONS.md`: what the Lidless Door is for.
- Lang: the block name; the WIP tab needs nothing new.

## Non-goals and anti-goals

- No real hallway: the void has no floor, no blocks, nothing to interact with. Portal-style rendering of the real
  world through the door (showing the far side's world in the panel from outside) is out; the panel shows the void.
- No mobs, items, projectiles or redstone through the door.
- No pocket dimension, no chunk loading, no teleport across dimensions.
- No blood cost, degree gate, lore text, advancement, recipe or loot.
- It must never strand a player: every exit path lands them somewhere standable, and a relog always finds them
  outside the door.
- It must not make the Monolith material family look cheap: if the void reads as a generic noise texture, it is
  wrong. The bar is the Monolith fragment and the Qliphoth black hole.

## Left to the builder

- Exact noise scales, rates, eye density and blink timing; the panel's parallax depth; the window feather width.
- Whether the door frame model is one model with two halves or two models.
- The item icon script and look.
- Which existing sounds serve as the loop, entry and exit.
- The service's package (`common/block/lidless/` with the block, or `common/util`).
- The block-entity renderer skips the panel of the door the local player is crossing, so the doorway windows show
  the world and not the panel. `safeLanding` and `safe` move out of `AxonalTransductionManager` into a shared
  helper rather than being copied. Observers drop a crossing id when that entity leaves their level.
- Test fixture layout.
