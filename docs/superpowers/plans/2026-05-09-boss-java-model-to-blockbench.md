# Boss Java Model To Blockbench Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate editable Blockbench modded entity files for the recently edited Hemomancy boss and Annetta Java models.

**Architecture:** Add a repeatable Node.js exporter that parses the Java model source files, converts static `createBodyLayer()` cubes into Blockbench modded-entity JSON, and writes generated files under the existing asset bbmodel tree. The exporter is intentionally narrow and data-driven for the nine approved models.

**Tech Stack:** Node.js ES modules, Java source parsing with targeted regex/token scanning, Blockbench `.bbmodel` JSON, Gradle build verification.

---

### Task 1: Add Repeatable Exporter

**Files:**
- Create: `tools/model_export/java_model_to_bbmodel.mjs`

- [x] **Step 1: Create the script skeleton**

Create an ES module with `fs/promises`, `path`, and `crypto`, plus a `MODELS` table containing the nine Java source paths, renderer texture paths, and output names.

- [x] **Step 2: Implement Java geometry parsing**

Parse each `root.addOrReplaceChild("name", CubeListBuilder.create()... , PartPose...)` block. Inside each block, track `.texOffs(u, v)`, `.mirror()`, `.addBox(...)`, and optional `new CubeDeformation(value)`.

- [x] **Step 3: Implement Blockbench conversion**

For each part, convert Java coordinates to Blockbench coordinates with:

```js
from = [pivotX + x - inflate, 24 - (pivotY + y + height) - inflate, pivotZ + z - inflate]
to = [pivotX + x + width + inflate, 24 - (pivotY + y) + inflate, pivotZ + z + depth + inflate]
origin = [pivotX, 24 - pivotY, pivotZ]
rotation = radians * 180 / Math.PI
```

Generate `faces` from Java box UV offsets using Blockbench box UV layout.

- [x] **Step 4: Write generated files**

Write all generated `.bbmodel` files to `src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/` with stable deterministic UUIDs derived from model, part, and cube index.

### Task 2: Generate And Validate Models

**Files:**
- Create: `src/main/resources/assets/hemomancy/models/entity/bbmodel/bosses/*.bbmodel`

- [x] **Step 1: Run the exporter**

Run:

```powershell
node tools/model_export/java_model_to_bbmodel.mjs
```

Expected: all nine `.bbmodel` files are written.

- [x] **Step 2: Validate generated JSON**

Run:

```powershell
node tools/model_export/java_model_to_bbmodel.mjs --check
```

Expected: reports all nine files as valid modded entity Blockbench models.

- [x] **Step 3: Inspect generated resource names**

Confirm the output directory contains:

```text
AnnettaKnowlesModel.bbmodel
ContainmentAnchorModel.bbmodel
HollowVesselModel.bbmodel
LatentAnnettaInfectionModel.bbmodel
PutricielModel.bbmodel
SeraphaeFragmentModel.bbmodel
SeraphaeModel.bbmodel
StainedPriestessModel.bbmodel
VelorumModel.bbmodel
```

### Task 3: Build Verification

**Files:**
- No source changes expected.

- [x] **Step 1: Run build**

Run:

```powershell
./gradlew.bat build
```

Expected: build exits 0.

- [x] **Step 2: Review git diff**

Run:

```powershell
git status --short
git diff -- tools/model_export/java_model_to_bbmodel.mjs docs/superpowers/specs/2026-05-09-boss-java-model-to-blockbench-design.md docs/superpowers/plans/2026-05-09-boss-java-model-to-blockbench.md
```

Expected: only the exporter, generated models, and planning docs are added by this task.
