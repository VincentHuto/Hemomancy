# Chamber Gateway Implementation Plan

> **For agentic workers:** executed inline with superpowers:executing-plans (owner rule: native execution, one final reviewer). No commits unless asked.

**Goal:** Turn the broken Monolith into a reopenable gateway to a keyhole-shaped Chamber of Will, with Seed binding and per-path themes.

**Architecture:** One pure shape class owns the keyhole; a new `chamber_gate` block entity holds the corridor end and draws the arches. `LidlessPortal` gains length and theme; `Mode` gains GATE and RETURN, which leave the level instead of landing. The husk implements `ProjectedBloodSink`.

**Tech Stack:** NeoForge 1.21.1, Java 21, GLSL 150. JUnit 5 for rules, GameTests for behaviour, review client for visuals.

**Spec:** `docs/superpowers/specs/2026-11-04-chamber-gateway-design.md`

## Global Constraints

- `AGENTS.md` rules: payload changes bump `PROTOCOL_VERSION` (to 37) and rewrite `payload-registry.txt`; new registry ids go in the frozen lists; `Component.translatable` only; blood through `BloodAccounting.pay`; render types through `SeededRenderTypes`; nothing client under `common/`; shader noise shared through includes; new session state goes through `TransientServerStateEvents`.
- Walk length 6, charge 40 ticks, 2.5 blood a tick, open window 200 ticks, eye animation 24 ticks.

## Review Focus

- A player leaves the level mid-crossing (logout, death, dimension change): the pin and session are released.
- The husk is removed while its owner is in the Chamber: the gate seals and walking out or the seat still returns them.
- A player with no seat binding enters through the gateway: the seat exists when they arrive.
- A Monolith placed inside the Chamber is broken and projected at: refused, no visit nesting.
- An observational visit (dream, guided) reaches the corridor gate: not admitted, inventory snapshot intact.

---

### Task 1: The keyhole shape and bounds

**Files:** Create `common/encounter/ChamberRoomShape.java`. Modify `ChamberExpansionRules`, `ChamberBoundaryRules`, `ChamberOfWillManager` (`radiusFor`, `ensureRoom`, tick bound), `ChamberOfWillEvents`, `ChamberOfWillRenderHelpers.renderBorderAura`. Test `ChamberRoomShapeTest`, updated `ChamberExpansionRulesTest`, `ChamberBoundaryRulesTest`.

**Produces:** `ChamberRoomShape.halfWidth(int)`, `.length(int)`, `.reach(int)`, `.contains(int dx, int dz, int radius)`, `.clamp(double dx, double dz, int radius)`, `.isGateCell(int dx, int dz, int radius)`.

Tests: area within 6% of the old square at every radius; arbor and entry point inside; corridor end inside and one past it out; clamp is a no-op inside and lands on the boundary outside; band only holds cells new to the larger shape; markers lie on floor.

### Task 2: The gate block and arches

**Files:** Create `common/block/shared/ChamberGateBlock`, `common/tile/shared/ChamberGateBlockEntity`, `client/render/tile/shared/ChamberGateRenderer`. Modify `BlockInit`, `BlockEntityInit`, `EntityRendererRegistrar`, frozen ids, blockstate and model, manager placement.

**Produces:** `ChamberGateBlockEntity.configure(UUID owner, int radius)`, `linked()`, `theme()`, `opening(float)`, `radius()`.

Renderer draws the ribs and the panel. Verified in the review client.

### Task 3: Seed binding

**Files:** Create `common/item/harbinger/SeedBinding`. Modify `DataComponentInit`, `QliphothSeedItem`, `SanguineMonolithBlockEntity.takeSeed/spillSeed`, `CardinalRiteActivation`, frozen ids, lang.

Tests in a `chamber_gateway_validation` GameTest holder: a taken Seed names its taker; a spilled Seed names the breaker; a foreign Seed is refused and an unbound Seed passes.

### Task 4: Crossing modes, length and theme

**Files:** Create `common/block/shared/LidlessTheme`. Modify `LidlessPortal` (`length()`, `theme(ServerPlayer)`), `LidlessPassageService` (`Mode.GATE`, `Mode.RETURN`, `arrive`), `LidlessPassageStatePacket`, `LidlessPassageClientState`, `PacketHandler`, `payload-registry.txt`.

Tests: stub portal of length 6 finishes at 6; a leaving mode calls `completed` and does not land the player; the state packet carries the theme.

### Task 5: The gateway

**Files:** Modify `SanguineMonolithBlockEntity` (sink, eye state, portal), `SanguineMonolithBlock` (hint, removal), `ChamberVisitService.beginGatewayVisit`, `ChamberOfWillManager` (links, return point, arrival), shared `ChamberGatePortal`, lang, `SanguineMonolithDialogueTrees` if Degree 8 needs it. Shader: `lidless_door_panel.fsh` opening and standalone shape. Renderer: `SanguineMonolithRenderer.renderPortal`.

Tests: owner projection charges and opens, cost paid; non-owner refused; charge bleeds; gate crossing arrives in the Chamber with the seat bound; return lands by the husk; removal clears the link; Chamber-dimension husk refused; observational visit not admitted.

### Task 6: Themes and the overgrown husk

**Files:** Create `shaders/include/noise/lidless_theme.glsl`. Modify `lidless_passage_view.fsh` and `.json`, `lidless_door_panel.fsh`, `LidlessPassageView`, `monolith_fissure.fsh` and `.json`, `HemoRenderTypes.monolithFissure`, `SanguineMonolithRenderer`, `ChamberGateRenderer`, `LidlessShaderContractTest` if it pins uniforms.

Tests: first Apotheos projection sets `overgrown` permanently; a D7 owner does not; theme follows path. Visuals in the review client with a theme property.

### Task 7: Docs, review, gate

Update `HEMOMANCY_REFERENCE` §5.7a and §5.9, `LORE_REFERENCE`, `OPEN_QUESTIONS`, memory. Run `alphaCheck`, then the final fresh reviewer, then one fix pass.
