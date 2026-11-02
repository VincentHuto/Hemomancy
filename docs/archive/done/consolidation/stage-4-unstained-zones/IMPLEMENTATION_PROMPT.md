# Implementation prompt: Stage 4 — Unstained zone lifecycle

Implement Stage 4 in the current Hemomancy checkout. Read the [Stage 4 audit](stage-4-unstained-zones.md), then inspect the current Still Waters, Pale Consecration, and Lethe Covenant event handlers and SavedData classes.

Put their identical 100-tick Overworld expiry triggers behind one `LevelTickEvent.Post` entry point. Call each owner's existing `removeExpired(currentTick)` once, in the current order. Preserve the `currentTick >= expiryTick` boundary and each data object's `setDirty()` behavior. Keep the Pale 20-tick effect and Lethe 1,200-tick purity work in their owners. Keep creation, overlap, ward, spawn, and other effect rules local to their rites.

If a small shared NBT read/write helper removes genuinely identical field handling, use it only after checking old-world compatibility. Preserve all three SavedData names, entry types, stored dimension, expiry values, and `BlockRadius` versus `ChunkRadius` units. Do not unify Pale AABB containment with Still/Lethe X/Z square checks. Resolve any uncertainty about cross-dimension effects from current source and a focused gameplay check before changing their routes.

Verify boundary expiry, save/reload of each record type, and the affected dimension route with focused checks. Compile the changed code. Avoid broad test scaffolding. Report the exact lifecycle call order, persistence compatibility evidence, checks run, and any world scenario not exercised. Preserve unrelated working-tree changes and do not create a worktree or pause for routine approval.

Consolidation should also reduce file and code bloat where responsibilities truly overlap. Remove obsolete handlers, helpers, and duplicate branches made unnecessary by this stage; avoid creating a new wrapper file for a few lines of shared logic. In the final report, quantify files deleted, lines removed, lines added, and the net line change for gameplay code. If the stage does not reduce file count or code size, explain why the remaining structure is needed.
