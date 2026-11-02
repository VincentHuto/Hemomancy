# Silent Archon Radial Combat and Slip Input Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move Silent Slipping from the manipulation radial to double-tap jump, and make the Silent Archon radial ability a non-physical combat burst.

**Architecture:** Register `hemomancy:silent_archon_severance` as the Silent Archon armor set ability while keeping Silent Slipping as a separate server-authoritative toggle. Add a no-detail C2S packet for the jump input path, then detect vanilla jump double taps client-side and let the server perform all validation and activation.

**Tech Stack:** NeoForge 1.21.1 payload networking, existing `ArmorSetAbilityRegistry`, client tick input handling, existing Silent Archon armor handler.

---

### Task 1: Source Contract Tests

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/SilentArchonIncorporealitySourceTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/SilentArchonInputAndCombatSourceTest.java`

- [x] Update the existing Silent Archon source test so it expects `silent_archon_severance` as the registry ability and treats Silent Slipping as a non-radial movement toggle.
- [x] Add a focused source test for the new packet, packet registration, client jump double-tap detection, server handler entry point, combat effect method, and docs.
- [x] Run `./gradlew.bat testClasses` and both direct tests; expect failure before implementation because the new packet, registry id, and client input path do not exist.

### Task 2: Server Ability Split

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/ArmorSetAbilityRegistry.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java`

- [x] Replace the Silent Archon radial registration with `SILENT_ARCHON_SEVERANCE`.
- [x] Add `activateSilentArchonSeverance(ServerPlayer player)` to emit the monolith-shatter burst, damage nearby living targets with magic damage, and apply Darkness, Weakness, Slowness, and Blood Loss.
- [x] Add `tryToggleSilentSlipping(ServerPlayer player)` for the double-tap packet path, preserving current start/cancel behavior, blood cost, cooldown, and full-set validation.
- [x] Make full Silent Archon set detection available through `hasFullSilentArchonSet(Player player)`.

### Task 3: Double-Tap Jump Packet and Client Input

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/ToggleSilentSlippingC2SPacket.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`

- [x] Add `ToggleSilentSlippingC2SPacket` with no payload beyond its type.
- [x] Register the packet with `playToServer`.
- [x] In the common client tick input path, detect a quick double tap of `mc.options.keyJump` with no screen open and send the toggle packet only when the local full set appears equipped.

### Task 4: Docs and Verification

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [x] Document `Silent Severance` as the radial combat ability.
- [x] Document Silent Slipping as double-tap jump movement.
- [ ] Run the focused direct tests, `./gradlew.bat build`, and `git diff --check`.
