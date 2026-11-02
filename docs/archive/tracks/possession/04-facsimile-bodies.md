# Track 4 — Facsimile bodies done properly

**Goal:** a possessed player is a facsimile in every way that shows: the body animates (wings beat, legs stride, the bat hangs), its hitbox is the form's, it carries the player's nameplate, blazes burn crimson, golems are hematic iron.

## Current state
- `client/event/PossessionClientEvents`: on `RenderPlayerEvent.Pre` it cancels the player render and draws a cached facsimile entity (`BODIES` map, keyed by player id) posed from the player each frame. The cached entity is never ticked, so its animation states (bat flight, blaze rods, golem arms) never start; only `walkAnimation` is updated. The hitbox stays the player's; no nameplate is drawn.
- Facsimile renderers (`client/render/entity/mob/facsimile/FacsimileRenderers`, 2a) extend the vanilla renderers and add the growth layer; the blaze keeps vanilla fire, the golem the vanilla iron texture.
- `SurrogateForm` sets speed/jump/knockback/flight; `PossessionEvents` handles water drain for enderman and blaze.

## Target
1. **A live body.** Replace the never-ticked cache with a proper client-side **stand-in**: the cached facsimile entity is ticked every client tick (`tick()` with its AI stripped, on the client only) after mirroring the player's position, rotation, on-ground, vehicle, pose (sneak, sprint, swim) and motion, so vanilla animation states run (bat `isResting` from the player sneaking against a ceiling, blaze rod spin, golem arm swing on attack, spider leg cycle, horse rear on leap, wolf tail). Attack swings mirror `player.swinging`. The body's `tickCount` follows the player's.
2. **Hitbox.** While possessed, the player's dimensions become the form's (`EntityEvent.Size` on the player keyed by `possessionForm`: bat 0.5×0.9, enderman 0.6×2.9, blaze 0.6×1.8, golem 1.4×2.7, horse 1.4×1.6, donkey 1.4×1.5, wolf 0.6×0.85, spider 1.4×0.9, stand 0.5×1.975) and the eye height follows. Rules class `PossessionBodyRules` (pure, tested): dimensions and eye height per form; the server and client agree because both read it. The enderman's and golem's height means doorways matter; that is the cost of the form.
3. **Nameplate.** The player's name renders above the body (the renderer's `renderNameTag` with the player's display name, scoreboard team colour respected), so other players know who they are looking at.
4. **Crimson fire for the blaze.** The facsimile blaze (fauna and possessed) renders with a crimson flame texture instead of vanilla fire: its own rod/fire layer texture at 16 px, and a `HemoParticleData` crimson ember instead of vanilla `FLAME`/`SMOKE`. Its fireball (`PossessionAbilities.use` blaze, and the fauna blaze's ranged attack) is a Hemomancy projectile with crimson flames that sets **crimson fire** on impact (the mod's `patch_crimson_flames` block family already exists; confirm it is a fire-like block that burns out, else add `crimson_fire`), never vanilla fire (which also fixes the deferred "fireball places fire" minor and the hallowed-ground griefing route).
5. **Hematic iron golem.** The facsimile golem's texture is hematic iron: dark red-black iron plates with lacquer seams and bone rivets (no copper, no vanilla iron grey), cracks that show crimson; the growth layer sits over it. Authored at 16 px/block; the golem's `crackiness` layer gets a matching crimson-crack texture.
6. **Forms table review.** While here, check each form's body against its facsimile in a client: enderman arms, blaze hover height, spider climb (`horizontalCollision` nudge in `PossessionClientEvents.onClientTick`), horse jump.

## Tasks
1. `PossessionBodyRules` + tests; the size event; eye height.
2. The ticked stand-in in `PossessionClientEvents` (keep the logout clear through `ClientSessionEvents` for the `ClientSessionCacheTest`).
3. Nameplate.
4. Crimson blaze: textures, particle swap, projectile, fire block check.
5. Hematic golem textures.
6. Proof: possession review client screenshots per form, moving and idle, plus the blaze firing and the golem slamming.

## Acceptance
- Every form animates as its vanilla mob does when the player moves, jumps, flies or attacks.
- Hitboxes and eye heights match the form on both sides (no rubber-banding through doorways).
- Other players see the possessor's name.
- No vanilla fire particle, fire block or iron-grey golem appears anywhere in possession or on the fauna facsimiles.
