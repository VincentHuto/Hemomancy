# Bestiary Entity Renderer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a discovered-only, pannable entity preview viewport to the Harbinger Progress Screen bestiary tab.

**Architecture:** Keep the bestiary tab controller and view ownership unchanged. Add a focused client helper for preview mapping/render math, extend `BestiaryTabState` with preview interaction state, and update `BestiaryTabView`/`BestiaryTabController` to render and route input to the split dossier viewport.

**Tech Stack:** Java 21, NeoForge 1.21.1, Minecraft client GUI rendering, `InventoryScreen.renderEntityInInventory`, existing source/compile tests under `src/test/java`.

---

### Task 1: Preview Contracts And State

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryEntityPreviewSourceTest.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryEntityPreview.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabState.java`

- [ ] **Step 1: Write the failing compile test**

Create `BestiaryEntityPreviewSourceTest.java` with:

```java
package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;

public final class BestiaryEntityPreviewSourceTest {
	private BestiaryEntityPreviewSourceTest() {
	}

	public static void main(String[] args) {
		assertTrue("chitinite should have a preview entity",
				BestiaryEntityPreview.hasPreview(Hemomancy.rloc("chitinite")));
		assertTrue("crimson doe should have a preview entity",
				BestiaryEntityPreview.hasPreview(Hemomancy.rloc("crimson_doe")));
		assertTrue("morphling strain keys should not be treated as direct specimens",
				!BestiaryEntityPreview.hasPreview(Hemomancy.rloc("bat")));

		BestiaryTabState state = new BestiaryTabState();
		state.previewPanX = 20.0F;
		state.previewPanY = -12.0F;
		state.previewZoom = 1.75F;
		state.previewRotationAngle = 1.0F;
		state.previewDragging = true;
		state.previewDragMode = BestiaryTabState.PreviewDragMode.PAN;
		state.resetPreviewView();

		assertEquals("pan x resets", 0.0F, state.previewPanX);
		assertEquals("pan y resets", 0.0F, state.previewPanY);
		assertEquals("zoom resets", 1.0F, state.previewZoom);
		assertEquals("rotation resets", 0.0F, state.previewRotationAngle);
		assertTrue("dragging resets", !state.previewDragging);
		assertTrue("drag mode resets", state.previewDragMode == BestiaryTabState.PreviewDragMode.NONE);
	}

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
```

- [ ] **Step 2: Run test compile and verify it fails**

Run: `./gradlew.bat test`

Expected: `compileTestJava` fails because `BestiaryEntityPreview`, `previewPanX`, `previewZoom`, and `PreviewDragMode` do not exist.

- [ ] **Step 3: Add minimal preview helper and state**

Create `BestiaryEntityPreview.java` with public constants/methods:

```java
public final class BestiaryEntityPreview {
	public static final float ZOOM_MIN = 0.5F;
	public static final float ZOOM_MAX = 2.75F;

	public static boolean hasPreview(ResourceLocation id) {
		return id != null && PREVIEW_TYPES.containsKey(id);
	}

	public static float clampZoom(float zoom) {
		return Mth.clamp(zoom, ZOOM_MIN, ZOOM_MAX);
	}
}
```

Map each current `SpecimenBestiaryDefinitions` specimen id that has a direct living entity to the matching `EntityInit` holder. Do not map morphling strain layer ids.

Modify `BestiaryTabState` to add:

```java
enum PreviewDragMode { NONE, PAN, ROTATE }

float previewPanX;
float previewPanY;
float previewZoom = 1.0F;
float previewRotationAngle;
boolean previewDragging;
PreviewDragMode previewDragMode = PreviewDragMode.NONE;
double previewDragLastX;
double previewDragLastY;
String previewKey;
LivingEntity previewEntity;

void resetPreviewView() {
	previewPanX = 0.0F;
	previewPanY = 0.0F;
	previewZoom = 1.0F;
	previewRotationAngle = 0.0F;
	previewDragging = false;
	previewDragMode = PreviewDragMode.NONE;
	previewDragLastX = 0.0D;
	previewDragLastY = 0.0D;
	previewKey = null;
	previewEntity = null;
}
```

Call `resetPreviewView()` from `select(Entry entry)` when the selected entry changes.

- [ ] **Step 4: Run tests and verify compile passes**

Run: `./gradlew.bat test`

Expected: `BUILD SUCCESSFUL`.

### Task 2: Split Dossier Rendering

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabView.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryEntityPreview.java`

- [ ] **Step 1: Write the failing source contract**

Extend `BestiaryEntityPreviewSourceTest.java` to read `BestiaryTabView.java` and assert these source fragments:

```java
assertContains("view exposes preview hit testing", view, "static boolean isOverPreview");
assertContains("detail uses split dossier", view, "drawPreviewDossier");
assertContains("preview renderer is discovered-only", view, "entry.discovered() && BestiaryEntityPreview.hasPreview");
assertContains("preview uses scissor", view, "gfx.enableScissor(previewX + 1");
assertContains("preview hover hint names pan", view, "Drag pan");
assertContains("preview hover hint names zoom", view, "Wheel zoom");
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `./gradlew.bat test`

Expected: source contract fails or compile fails because the view does not yet expose preview hit testing/split dossier rendering.

- [ ] **Step 3: Implement split dossier view**

Change `drawDetail` so discovered entries with `BestiaryEntityPreview.hasPreview(entry.previewId())` use a split layout:

- preview viewport at left, 45% width when detail width is at least 360 px
- text column at right
- locked entries keep the existing withheld text behavior
- no silhouette for locked entries

Add static hit-test helpers:

```java
static boolean isOverPreview(ProgressScreenContext ctx, BestiaryTabState state, double mx, double my)
static boolean hasPreviewForSelected(BestiaryTabState state)
```

Implement `BestiaryEntityPreview.render(...)` using the existing `SummonsTabView` entity preview pattern:

- create entity from mapped `EntityType` in the current client level
- cache by selected key/current level
- disable AI when entity is a `Mob`
- scissor to preview rectangle
- apply pan offsets and zoom to center/scale
- set yaw from `state.previewRotationAngle`
- capture and restore rotations
- fallback text: `Preview unavailable`

- [ ] **Step 4: Run tests and verify pass**

Run: `./gradlew.bat test`

Expected: `BUILD SUCCESSFUL`.

### Task 3: Preview Input Routing

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryEntityPreviewSourceTest.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabController.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryTabState.java`

- [ ] **Step 1: Write the failing input contract**

Extend `BestiaryEntityPreviewSourceTest.java` to read `BestiaryTabController.java` and assert these source fragments:

```java
assertContains("click inside preview starts dragging", controller, "state.previewDragging = true");
assertContains("shift click rotates", controller, "Screen.hasShiftDown()");
assertContains("pan drag applies dx", controller, "state.previewPanX += (float) dx");
assertContains("pan drag applies dy", controller, "state.previewPanY += (float) dy");
assertContains("rotate drag changes rotation", controller, "PREVIEW_DRAG_ROTATION_SPEED");
assertContains("wheel over preview changes zoom", controller, "BestiaryEntityPreview.clampZoom");
assertContains("preview scroll does not scroll text", controller, "return true;");
```

- [ ] **Step 2: Run tests and verify they fail**

Run: `./gradlew.bat test`

Expected: source contract fails because the bestiary controller does not route preview input yet.

- [ ] **Step 3: Implement preview input**

In `BestiaryTabController`:

- left click inside preview starts dragging
- `Screen.hasShiftDown()` selects rotate mode
- normal left drag selects pan mode
- mouse release clears dragging/mode
- drag in pan mode changes `previewPanX` and `previewPanY`
- drag in rotate mode changes `previewRotationAngle`
- scroll over preview changes `previewZoom` and returns before list/detail scroll logic

Keep existing list selection and list/detail scroll behavior outside the preview viewport.

- [ ] **Step 4: Run tests and verify pass**

Run: `./gradlew.bat test`

Expected: `BUILD SUCCESSFUL`.

### Task 4: Build Verification And Docs Check

**Files:**
- Verify: all touched files
- Optional modify: `docs/HEMOMANCY_REFERENCE.md` only if implementation changes mechanics or unlock behavior

- [ ] **Step 1: Run full build**

Run: `./gradlew.bat build`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Review docs impact**

Check implementation against `docs/HEMOMANCY_REFERENCE.md`. If only client presentation changed, do not edit the reference doc.

- [ ] **Step 3: Review git diff**

Run: `git diff -- src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger src/test/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger docs/HEMOMANCY_REFERENCE.md`

Expected: diff only contains bestiary preview implementation and tests, with no unrelated formatting churn.
