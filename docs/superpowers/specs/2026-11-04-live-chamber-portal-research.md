# Research: a live view through the husk, and no loading screen into the Chamber

Written 2026-11-04 at the owner's request, after the Chamber gateway (`2026-11-04-chamber-gateway-design.md`). This is
an assessment, not a design: nothing here is built.

## The two asks are very different in cost

| Ask | Cheapest sound route | Effort | Frame cost | New dependency |
|---|---|---|---|---|
| No loading screen into or out of the Chamber | A veil: NeoForge's dimension transition screen hook draws the void instead of the dirt screen | 1 to 2 days | none | none |
| A live view of the Chamber through the open eye | A proxy render: draw a stand-in of the room ourselves into a texture from the portal's viewpoint | about a week | one small extra scene pass while the eye is open and on screen | none |
| Same, including blocks the player built in their Chamber | A one-shot room snapshot packet drawn into a baked buffer | 2 to 3 more days | small | none |
| Either, through Immersive Portals | Optional compat on its `PortalAPI` | 1 to 2 weeks, then ongoing | about double the world render per visible portal | yes, and upstream is archived |
| A true second client level in-house (what Immersive Portals does) | Deep mixins into the connection, level, chunk cache and level renderer | weeks | as above | none |

## 1. No loading screen

**Built 2026-11-04** as described below: `DimensionTransitionRegistrar`, `ChamberTransitionScreen`, `ChamberVeil`,
`ChamberVeilRules`, `LidlessPassageView.renderVeil`; guardrail `ChamberTransitionScreenTest`; captures `gate-veil-*` in the
review client. The live view (section 2) is not built.

### What produces it

`Minecraft.setLevel` is called from `ClientPacketListener.handleRespawn` on every dimension change. It tears down the
old `ClientLevel`, builds the new one and shows a `ReceivingLevelScreen` ("Downloading terrain"). `LevelLoadStatusManager`
closes that screen once the chunk under the player has arrived, or after 30 seconds. There is no way to avoid the
level swap itself short of reimplementing the protocol, which is what Immersive Portals does.

### What NeoForge gives us

`Minecraft.setLevel` asks `DimensionTransitionScreenManager.getScreenFromLevel(target, source)` for the screen's
factory, and `RegisterDimensionTransitionScreenEvent` lets a mod register one per target dimension, per source
dimension, or per pair (`registerConditionalEffect`). So the screen into and out of `hemomancy:chamber_of_will` can be
our own `ReceivingLevelScreen` subclass, with no mixin. It still closes itself when the player's chunk arrives.

### Why it will look seamless here

- The Chamber's generator is flat void with a single air layer, and the room is pre-built by the manager, so the
  player's chunk is one small packet. The screen should stand for a handful of ticks.
- The crossing already ends looking into a black hole in the Chamber's colours. The veil screen draws that same view
  (the view shader already exists; it only needs the last crossing frame's state kept across `LevelEvent.Unload`,
  which currently clears it), with the hole widening to fill the screen. When the screen closes, the corridor is
  there. On the way out, the same in reverse: the void closes over the Chamber and the husk is there.
- Nothing else changes: the walk, the pin, the arrival position and the inventory handling all stand.

### Work

1. A `ChamberTransitionScreen` extending `ReceivingLevelScreen` that overrides `render` and `renderBackground` to draw
   the Lidless view shader full screen with `ChamberTurn` 1 and a growing hole, and draws no text.
2. Register it for both directions in a `client/registry` class on `RegisterDimensionTransitionScreenEvent`.
3. Keep a small "veil" record (theme, seed, hole size) across the level unload instead of clearing it.
4. One source-text guardrail that the screen is registered, and a review-client capture mid-transition.

Risk: low. The only unknown is how many ticks the screen stands on a slow connection; the veil covers any length.

## 2. A live view through the eye

**Built 2026-11-04**, with the player-built blocks, as `2026-11-04-live-eye-design.md` describes: `ChamberProxySnapshot`,
`ChamberProxyRules`, `ChamberProxyView`, the `Live` sampling in `lidless_door_panel`; captures `live-eye-*` in the review client.

### What the Chamber actually is, from a renderer's view

Almost everything in a Chamber is procedural and already known to the client before it arrives:

- the sky is our own `DimensionSpecialEffects` (`ChamberOfWillEffects`), drawn from a model-view matrix, projection and
  camera, from the theme in `ChamberOfWillClientData` (theme, tier, radius and pome count arrive in
  `PacketSyncChamberOfWill`);
- the floor is `ChamberRoomShape` plus the radius: Blood Wood planks with four Sporitic Crystal markers;
- the arches are a baked mesh per radius in `ChamberGateRenderer`;
- the Arbor of Will is an entity renderer configured from the same client data;
- the seat and the gate are two blocks at known offsets.

What is not procedural: blocks the player placed, items and any other entity. A first version can leave those out and
a later one can send a snapshot of the room's non-default blocks when the eye opens (a few hundred blocks at most).

### The proxy render

While the eye is open, within a short distance and on screen: each frame, render the stand-in room into a half-size
offscreen target (we already keep two for the crossing view) from a camera placed in the Chamber the way Immersive
Portals places it, at the gate plus the camera's offset from the husk, turned by the facing difference. Then the eye's
fragment shader samples that texture by screen position, so the view has true parallax as the player moves. The pass
is: sky renderer, a few hundred floor quads, the rib mesh, the arbor, two blocks.

One design choice makes this better than a plain window: the crossing is six blocks long, so the proxy camera should
stand six blocks further down the corridor than a true window would, so what is seen through the eye is what the
walk arrives at. The window and the walk then agree.

### Cost and risk

- Frame cost: one extra small scene render. Unmeasured; the sky renderer is the heaviest part (the Chamber effects
  are about 8,000 lines with many shader switches), so half resolution and a distance cutoff are the levers. Budget
  a millisecond or two on a middling GPU while the eye is open, nothing when it is shut or off screen.
- The sky renderer and the theme registry read the current level and static client data; they need to take an
  explicit theme and camera to draw for a room the player is not in. That is the main refactor.
- Render state: the sky code drives `RenderSystem` directly; the pass needs careful save and restore of targets,
  depth and fog, which the crossing view already does for its targets.
- Shader packs (Iris or Oculus) treat offscreen passes unpredictably; the crossing view has the same exposure today.

### Immersive Portals, and why not here

- The official repository (`iPortalTeam/ImmersivePortalsMod`) was archived on 21 April 2026. Its last 1.21.1 build is
  v6.0.3, a pre-release whose only note is a third-person rendering fix; a community fork ("Immersive Portal - CE",
  NeoForge 21.1.228+, needs Cloth Config) carries on.
- Its API creates `Portal` entities with `axisW` and `axisH`, a rotation quaternion, `createReversePortal` and
  `createFlippedPortal`, and needs the mod on both client and server. The documented shape is a rectangle; a diamond or
  eye needs its custom shape support, which the documentation does not describe for 6.x (unconfirmed).
- It renders the destination as a real second client level every frame per visible portal, roughly doubling world
  render cost, and keeps that dimension's chunks loaded on the client. It has a long history of conflicts with
  Sodium, Embeddium, Iris and Oculus and with Distant Horizons (the owner keeps a DH jar in `libs/`, currently
  disabled).
- Its portal is a direct step through, which replaces the six-block void walk the gateway is built on.
- Our Chamber sky, the single `WorldEffectDispatcher` subscriber and the full-screen crossing pass would all have to
  become aware of being drawn for a second level, so most of the proxy's refactor is needed anyway, plus the compat.

## Recommendation

Do the veil first: it is nearly free with NeoForge's hook and removes the loading screen for good. Then build the
proxy live view with the six-block camera offset, without player-built blocks, and judge it in captures before adding
the room snapshot. Leave Immersive Portals alone.

## Sources

- NeoForge 21.1.251 sources: `Minecraft.setLevel`, `DimensionTransitionScreenManager`,
  `RegisterDimensionTransitionScreenEvent`, `ReceivingLevelScreen`, `LevelLoadStatusManager`.
- Immersive Portals release v6.0.3-mc1.21.1 and the archive notice: https://github.com/iPortalTeam/ImmersivePortalsMod/releases/tag/v6.0.3-mc1.21.1
- Immersive Portals API for other mods: https://qouteall.fun/immptl/wiki/API-for-Other-Mods.html
- Immersive Portals for (Neo)Forge on Modrinth: https://modrinth.com/mod/immersive-portals-neoforge
- Immersive Portal - CE (community NeoForge 1.21.1 build): https://www.modpackindex.com/mod/175287/immersive-portal-ce
