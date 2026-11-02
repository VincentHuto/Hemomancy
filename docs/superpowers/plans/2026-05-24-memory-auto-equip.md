# Memory Auto-Equip Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Auto-equip newly consumed manipulation memories while the player still has open normal manipulation slots.

**Architecture:** Keep learning in `BloodMemoryItem.use(...)`, and reuse existing capability helpers for slot limits. Add source-level regression coverage because the item depends on Minecraft runtime types that are expensive to instantiate in a pure JVM test.

**Tech Stack:** Java 21, NeoForge 1.21.1, existing manipulation capability/network sync, source-level tests.

---

### Task 1: Regression Test

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItemAutoEquipSourceTest.java`

- [ ] Write a source-level test asserting `BloodMemoryItem` imports `ManipSlotHelper`, calls `ManipSlotHelper.getMaxSlots(playerIn)`, calls `known.equipManip(manipulation.getName(), maxSlots)` after learning, sends the existing `KnownManipulationServerPacket`, and has separate feedback for auto-equipped and learned-but-full memories.
- [ ] Run the test and verify it fails before production changes.

### Task 2: Implement Auto-Equip

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java`

- [ ] After a new memory is added to `knownList`, compute `maxSlots` with `ManipSlotHelper.getMaxSlots(playerIn)`.
- [ ] Attempt `known.equipManip(manipulation.getName(), maxSlots)`.
- [ ] Show `Memorized and equipped: <proper name>` when equip succeeds.
- [ ] Show `Memory learned. Use a Mnemonic Reliquary to change equipped memories.` when the new memory is learned but cannot be equipped.
- [ ] Keep duplicate, inactive, and rank-gated behavior unchanged.

### Task 3: Verify

**Files:**
- Verify production and test files.

- [ ] Run the new source test.
- [ ] Run the existing helper/source tests touched by recent manipulation work.
- [ ] Run `./gradlew.bat build`.
