# Silent Archon Radial Combat and Slip Input Design

## Approved Direction

The full Silent Archon armor set should split its active identity into two controls:

- The manipulation radial activates a combat effect.
- Silent Slipping activates from a quick double tap of the vanilla jump key, matching the feel of creative flight.

## Radial Combat Ability

Replace the current radial `hemomancy:silent_slipping` registration with `hemomancy:silent_archon_severance`.

`Silent Severance` uses the Silent Archon helmet as its radial icon, requires the full four-piece set, and remains server-authoritative through the armor ability registry. When activated, the wearer releases a short-range monolith-shatter silence burst. The burst deals magic damage and applies control pressure through Darkness, Weakness, Slowness, and Blood Loss. This keeps the attack non-physical, so it respects the Silent Archon restriction against mundane physical offense.

## Silent Slipping Input

Silent Slipping remains a server-authoritative timed no-physics state with slow flight, block phasing, safe-exit resolution, blood cost, and cooldown. It is no longer exposed as the radial armor ability.

The client detects a double tap of `Minecraft.options.keyJump` while no screen is open and the local player appears to be wearing the complete Silent Archon set. The client sends only a toggle request packet. The server then validates the full armor set, cooldown, blood cost, and current active state before starting or cancelling Silent Slipping.

## Networking

Add `ToggleSilentSlippingC2SPacket` for the double-tap jump path. The packet carries no effect details. It only asks the server to toggle the Silent Slipping state for the sending player.

## Documentation

Update the Silent Archon section of `docs/HEMOMANCY_REFERENCE.md` so the radial combat ability and the double-tap jump movement ability are described separately.
