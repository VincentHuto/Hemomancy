# Silent Archon Monolith Player Core Design

## Approved Direction

When a player wears the full four-piece Silent Archon set, the underlying player body should render as a solid monolith-like core while the existing slightly transparent Silent Archon armor remains visible on top.

## Rendering Approach

Use a client-only mixin on `LivingEntityRenderer#getRenderType` with an `AbstractClientPlayer` guard to replace only the base player body render type while the full Silent Archon set is equipped. The replacement render type is the existing `HemoRenderTypes.monolithEntitySurface(...)`, matching the Sanguine Monolith and Monolith Fragment shader family.

This preserves normal vanilla player render ordering:

1. Base player model renders as the monolith core.
2. Vanilla/custom armor layers render after the body.
3. Existing `SilentArchonArmorOverlayLayer` renders the translucent Monolith Fragment armor overlay on top.

## Scope

- Full-set only: helm, chestplate, leggings, and boots must all be Silent Archon pieces.
- Client visual only: no gameplay, networking, damage, or movement changes.
- Respect vanilla visibility: invisible/translucent/outline-only player render paths should not be forced into the monolith core.
