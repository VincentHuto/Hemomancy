# Claw Slash Ribbon Design

## Goal

Replace glow-dot slash stand-ins on Gloam Laceration with a client-rendered claw slash: three parallel, tapered, short-lived ribbon strokes that read as an actual cutting arc.

## Scope

- Add a client world renderer for slash effects.
- Add a server-to-client payload that spawns the visual near relevant players.
- Wire Gloam Laceration player casts and entity/Will casts to the new visual.
- Keep luminous Lux effects glow-based.
- Leave the existing `blood_claw` particle available for older/simple effects.

## Visual Behavior

Each slash is three parallel curved strokes. Every stroke has a faint outer glow pass and a brighter inner cut pass. Strokes taper at both ends, peak in width near the middle, expand very slightly during the first ticks, then fade quickly over roughly half a second. Gloam uses a dark violet/near-black palette with a stronger center when the cast qualifies as an ambush.

## Architecture

`ClawSlashRenderer` owns client-side effect lifetime and rendering. It stores short-lived slash instances, ticks them from `ClientEvents.onClientTickPost`, and renders after translucent blocks from `ClientEvents.renderLevelLastEvent`.

`SpawnClawSlashPacket` carries compact spawn data from server to client: center position, forward vector, scale, red/green/blue color, ambush flag, and seed. `PacketHandler.sendClawSlash` sends the payload to nearby clients.

Manipulations do not depend on client renderer classes directly. They call `PacketHandler.sendClawSlash` when running on a `ServerLevel`.

## Testing

Source-level tests guard that:

- the renderer exists and draws three strokes;
- client events tick and render it;
- the packet is registered and has a send helper;
- Gloam Laceration player and entity cast paths call `sendClawSlash`;
- Gloam no longer relies on `GlowParticleFactory` for its slash trail.
