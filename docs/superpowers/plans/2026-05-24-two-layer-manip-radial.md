# Two-Layer Manipulation Radial Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a two-layer manipulation radial where blood absorption and blood projection are fixed inner-wheel mechanics, while normal memorized manipulations remain on the outer wheel.

**Architecture:** Add fixed mechanical manipulation rules to the shared equip helper and capability implementation, then extend `GenericRadialMenu` with an inner band while preserving its existing single-band API for callers. `RadialChooseManipScreen` will place absorption/projection into the inner band and all non-mechanical equipped manipulations into the outer band.

**Tech Stack:** Java 21, Minecraft NeoForge 1.21.1 client GUI rendering, existing payload packet networking, source-level Java regression tests.

---

## File Structure

- Modify `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java`: fixed mechanical constants, equip normalization, normal-slot counting, unequip blocking.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulations.java`: route equip/unequip and equipped-list access through the helper.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/network/capa/manips/EquipManipulationPacket.java`: report normal slot usage instead of counting fixed mechanics as normal memorized slots.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java`: use normal slot count in `/hemo manip equip` messaging.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java`: add inner-band item storage, drawing, hover, click, and cursor clamping.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java`: build inner mechanical items and outer normal memorized items separately.
- Modify `src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java`: add pure helper coverage for fixed mechanics and normal-slot limits.
- Create `src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java`: source-level coverage for inner mechanical entries and fixed exclusion from the outer ring.

### Task 1: Lock Mechanical Manipulation Equip Rules

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulations.java`

- [ ] **Step 1: Write the failing helper tests**

Add these calls to `main`:

```java
mechanicalManipulationsAreRestoredAndLocked();
mechanicalManipulationsDoNotConsumeNormalSlots();
```

Add these test methods:

```java
private static void mechanicalManipulationsAreRestoredAndLocked() {
	List<String> equipped = new ArrayList<>();
	equipped.add("blood_shot");

	boolean normalized = ManipulationEquipHelper.normalizeEquippedNames(equipped);
	boolean removedAbsorption = ManipulationEquipHelper.unequipNameIfAllowed(equipped,
			ManipulationEquipHelper.BLOOD_ABSORPTION);
	boolean removedProjection = ManipulationEquipHelper.unequipNameIfAllowed(equipped,
			ManipulationEquipHelper.BLOOD_PROJECTION);

	assertTrue("normalization changed state", normalized);
	assertFalse("absorption cannot be unequipped", removedAbsorption);
	assertFalse("projection cannot be unequipped", removedProjection);
	assertEquals("absorption stays first", ManipulationEquipHelper.BLOOD_ABSORPTION, equipped.get(0));
	assertEquals("projection stays second", ManipulationEquipHelper.BLOOD_PROJECTION, equipped.get(1));
	assertEquals("normal manip preserved after mechanics", "blood_shot", equipped.get(2));
}
```

```java
private static void mechanicalManipulationsDoNotConsumeNormalSlots() {
	List<String> equipped = new ArrayList<>();

	assertTrue("first normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_shot", 3));
	assertTrue("second normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_lamp", 3));
	assertTrue("third normal equip", ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_needle", 3));
	assertFalse("fourth normal blocked by normal slot limit",
			ManipulationEquipHelper.equipNameIfPossible(equipped, "blood_burst", 3));

	assertEquals("total count includes two fixed mechanics", 5, equipped.size());
	assertEquals("normal count respects memorized slots", 3,
			ManipulationEquipHelper.countNormalEquippedNames(equipped));
}
```

- [ ] **Step 2: Run the focused helper test and verify it fails**

Run:

```powershell
javac -d build/tmp/codex-radial src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationGrantHelperTest
```

Expected: compile failure for missing helper members such as `normalizeEquippedNames`, `unequipNameIfAllowed`, and `BLOOD_ABSORPTION`.

- [ ] **Step 3: Implement fixed mechanical helper rules**

Replace `ManipulationEquipHelper` with:

```java
package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ManipulationEquipHelper {
	public static final String BLOOD_ABSORPTION = "blood_absorption";
	public static final String BLOOD_PROJECTION = "blood_projection";

	private ManipulationEquipHelper() {
	}

	public static boolean equipNameIfPossible(List<String> equippedNames, String manipName, int maxSlots) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()) {
			return false;
		}
		boolean changed = normalizeEquippedNames(equippedNames);
		if (isFixedMechanicalManip(manipName)) {
			return changed;
		}
		if (equippedNames.contains(manipName)) {
			return changed;
		}
		if (countNormalEquippedNames(equippedNames) >= maxSlots) {
			return changed;
		}
		equippedNames.add(manipName);
		return true;
	}

	public static boolean unequipNameIfAllowed(List<String> equippedNames, String manipName) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()) {
			return false;
		}
		normalizeEquippedNames(equippedNames);
		if (isFixedMechanicalManip(manipName)) {
			return false;
		}
		return equippedNames.remove(manipName);
	}

	public static boolean normalizeEquippedNames(List<String> equippedNames) {
		if (equippedNames == null) {
			return false;
		}
		List<String> original = new ArrayList<>(equippedNames);
		LinkedHashSet<String> normalNames = new LinkedHashSet<>();
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isFixedMechanicalManip(name)) {
				normalNames.add(name);
			}
		}
		equippedNames.clear();
		equippedNames.add(BLOOD_ABSORPTION);
		equippedNames.add(BLOOD_PROJECTION);
		equippedNames.addAll(normalNames);
		return !original.equals(equippedNames);
	}

	public static int countNormalEquippedNames(List<String> equippedNames) {
		if (equippedNames == null) {
			return 0;
		}
		int count = 0;
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isFixedMechanicalManip(name)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isFixedMechanicalManip(String manipName) {
		return BLOOD_ABSORPTION.equals(manipName) || BLOOD_PROJECTION.equals(manipName);
	}
}
```

- [ ] **Step 4: Route capability equip state through the helper**

Change `KnownManipulations` equipped methods to:

```java
@Override
public List<String> getEquippedManipNames() {
	ManipulationEquipHelper.normalizeEquippedNames(equippedManipNames);
	return equippedManipNames;
}

@Override
public void setEquippedManipNames(List<String> names) {
	this.equippedManipNames = names != null ? names : new ArrayList<>();
	ManipulationEquipHelper.normalizeEquippedNames(this.equippedManipNames);
}

@Override
public boolean isManipEquipped(BloodManipulation manip) {
	if (manip == null || manip == BloodManipulation.BLANK) return false;
	ManipulationEquipHelper.normalizeEquippedNames(equippedManipNames);
	return equippedManipNames.contains(manip.getName());
}

@Override
public boolean equipManip(String manipName, int maxSlots) {
	return ManipulationEquipHelper.equipNameIfPossible(equippedManipNames, manipName, maxSlots);
}

@Override
public boolean unequipManip(String manipName) {
	return ManipulationEquipHelper.unequipNameIfAllowed(equippedManipNames, manipName);
}
```

- [ ] **Step 5: Run the helper test and verify it passes**

Run:

```powershell
javac -d build/tmp/codex-radial src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationGrantHelperTest
```

Expected: no output and exit code `0`.

### Task 2: Make Slot Messaging Count Only Normal Manipulations

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/network/capa/manips/EquipManipulationPacket.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java`

- [ ] **Step 1: Update packet messaging**

Import the helper:

```java
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
```

Replace each user-facing slot count in `EquipManipulationPacket` with:

```java
ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames()) + "/" + maxSlots
```

- [ ] **Step 2: Update command messaging**

Ensure `HemoCommand` imports:

```java
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
```

In `equipManip`, replace `known.getEquippedManipNames().size()` in success and failure text with:

```java
ManipulationEquipHelper.countNormalEquippedNames(known.getEquippedManipNames())
```

- [ ] **Step 3: Run the focused helper test to catch import mistakes**

Run:

```powershell
javac -d build/tmp/codex-radial src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationGrantHelperTest
```

Expected: no output and exit code `0`.

### Task 3: Add Inner-Band Radial Menu Support

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java`

- [ ] **Step 1: Add inner item collections and accessors**

Add fields beside `items` and `visibleItems`:

```java
private final List<RadialMenuItem> innerItems = Lists.newArrayList();
private final List<RadialMenuItem> visibleInnerItems = Lists.newArrayList();
```

Add radius fields beside `radiusIn`, `radiusOut`, and `itemRadius`:

```java
public float centerRadius;
public float innerRadiusOut;
public float innerItemRadius;
```

Add methods:

```java
public int getTotalVisibleItemCount() {
	return visibleItems.size() + visibleInnerItems.size();
}

public void addInner(RadialMenuItem item) {
	innerItems.add(item);
	if (item.isVisible()) {
		visibleInnerItems.add(item);
	}
}

public void addAllInner(Collection<? extends RadialMenuItem> itemsIn) {
	innerItems.addAll(itemsIn);
	for (RadialMenuItem item : itemsIn) {
		if (item.isVisible()) {
			visibleInnerItems.add(item);
		}
	}
}
```

- [ ] **Step 2: Update visibility and clearing**

Replace `visibilityChanged` with logic that rebuilds both visible lists:

```java
public void visibilityChanged(RadialMenuItem item) {
	visibleItems.clear();
	for (RadialMenuItem radialMenuItem : items) {
		if (radialMenuItem.isVisible()) {
			visibleItems.add(radialMenuItem);
		}
	}
	visibleInnerItems.clear();
	for (RadialMenuItem radialMenuItem : innerItems) {
		if (radialMenuItem.isVisible()) {
			visibleInnerItems.add(radialMenuItem);
		}
	}
}
```

Update `clear`:

```java
public void clear() {
	items.clear();
	visibleItems.clear();
	innerItems.clear();
	visibleInnerItems.clear();
}
```

- [ ] **Step 3: Make hover/click consider both bands**

Update `getHoveredItem` so inner items are checked before outer items:

```java
@Nullable
public RadialMenuItem getHoveredItem() {
	for (RadialMenuItem item : visibleInnerItems) {
		if (item.isHovered()) {
			return item;
		}
	}
	for (RadialMenuItem item : visibleItems) {
		if (item.isHovered()) {
			return item;
		}
	}
	return null;
}
```

Update `setHovered(int which)` to clear inner hover state and preserve existing keyboard behavior for the outer ring:

```java
public void setHovered(int which) {
	for (RadialMenuItem item : visibleInnerItems) {
		item.setHovered(false);
	}
	for (int i = 0; i < visibleItems.size(); i++) {
		visibleItems.get(i).setHovered(i == which);
	}
}
```

- [ ] **Step 4: Split background and icon drawing by band**

Add a band-aware helper:

```java
private void iterateBand(List<RadialMenuItem> visible, TriConsumer<RadialMenuItem, Float, Float> consumer) {
	int numItems = visible.size();
	for (int i = 0; i < numItems; i++) {
		float s = (float) getAngleFor(i - 0.5, numItems);
		float e = (float) getAngleFor(i + 0.5, numItems);
		consumer.accept(visible.get(i), s, e);
	}
}
```

Keep `iterateVisible` as:

```java
private void iterateVisible(TriConsumer<RadialMenuItem, Float, Float> consumer) {
	iterateBand(visibleItems, consumer);
}
```

In `draw`, compute radii:

```java
centerRadius = animated ? Math.max(0.1f, 18 * animProgress) : 18;
innerRadiusOut = animated ? Math.max(centerRadius + 0.1f, 42 * animProgress) : 42;
radiusIn = animated ? Math.max(innerRadiusOut + 0.1f, 50 * animProgress) : 50;
radiusOut = animated ? Math.max(radiusIn + 0.1f, 86 * animProgress) : 86;
innerItemRadius = (centerRadius + innerRadiusOut) * 0.5f;
itemRadius = (radiusIn + radiusOut) * 0.5f;
```

Update `drawBackground` to draw both bands:

```java
if (getTotalVisibleItemCount() > 0) {
	RenderSystem.enableBlend();
	RenderSystem.defaultBlendFunc();
	RenderSystem.setShader(GameRenderer::getPositionColorShader);
	RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

	Tesselator tessellator = Tesselator.getInstance();
	BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
	iterateBand(visibleInnerItems, (item, s, e) -> {
		int color = item.isHovered() ? backgroundColorHover : backgroundColor;
		drawPieArc(buffer, x, y, z, centerRadius, innerRadiusOut, s, e, color);
	});
	iterateBand(visibleItems, (item, s, e) -> {
		int color = item.isHovered() ? backgroundColorHover : backgroundColor;
		drawPieArc(buffer, x, y, z, radiusIn, radiusOut, s, e, color);
	});
	BufferUploader.drawWithShader(buffer.buildOrThrow());
	RenderSystem.disableBlend();
}
```

Update `drawItems` to draw inner items with `innerItemRadius` and outer items with `itemRadius`.

- [ ] **Step 5: Update mouse hover geometry**

In `processMouse`, set `hovered` by checking inner radius first and outer radius second. Use `RadialMenuItem hoveredItem = null;`, clear all hovered states, and set exactly one hovered item:

```java
RadialMenuItem hoveredItem = findHoveredInBand(visibleInnerItems, a, d, centerRadius, innerRadiusOut);
if (hoveredItem == null) {
	hoveredItem = findHoveredInBand(visibleItems, a, d, radiusIn, radiusOut);
}
for (RadialMenuItem item : visibleInnerItems) {
	item.setHovered(item == hoveredItem);
}
for (RadialMenuItem item : visibleItems) {
	item.setHovered(item == hoveredItem);
}
```

Add:

```java
@Nullable
private RadialMenuItem findHoveredInBand(List<RadialMenuItem> visible, double angle, double distance,
		float innerRadius, float outerRadius) {
	int numItems = visible.size();
	for (int i = 0; i < numItems; i++) {
		float s = (float) getAngleFor(i - 0.5, numItems);
		float e = (float) getAngleFor(i + 0.5, numItems);
		if (angle >= s && angle < e && distance >= innerRadius && distance < outerRadius) {
			return visible.get(i);
		}
	}
	return null;
}
```

Keep cursor clamping based on `radiusOut`.

- [ ] **Step 6: Run a build to catch client compile errors**

Run:

```powershell
./gradlew.bat build
```

Expected: build succeeds, with existing warnings allowed.

### Task 4: Build the Two-Layer Manipulation Screen

**Files:**
- Create: `src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java`

- [ ] **Step 1: Write the failing source-level screen test**

Create:

```java
package com.vincenthuto.hemomancy.client.screen.manips;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RadialChooseManipScreenSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private RadialChooseManipScreenSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
		String menu = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java");

		assertContains("radial screen imports fixed mechanical helper", screen,
				"ManipulationEquipHelper");
		assertContains("radial screen adds mechanical entries to the inner band", screen,
				"this.menu.addAllInner(this.cachedMechanicalItems);");
		assertContains("absorption is added before projection for the top inner half", screen,
				"ManipulationEquipHelper.BLOOD_ABSORPTION");
		assertContains("projection is added to the second inner half", screen,
				"ManipulationEquipHelper.BLOOD_PROJECTION");
		assertContains("outer ring skips fixed mechanical manipulations", screen,
				"ManipulationEquipHelper.isFixedMechanicalManip(c.getName())");
		assertContains("generic radial supports inner band items", menu,
				"visibleInnerItems");
		assertContains("generic radial exposes inner add API", menu,
				"addAllInner");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
```

- [ ] **Step 2: Run the source test and verify it fails before screen edits**

Run:

```powershell
javac -d build/tmp/codex-radial src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreenSourceTest
```

Expected: failure until `RadialChooseManipScreen` uses `addAllInner` and excludes fixed mechanical names from the outer ring.

- [ ] **Step 3: Split cached items by band in the screen**

Change fields:

```java
private final List<RadialMenuItem> cachedMenuItems = Lists.newArrayList();
private final List<RadialMenuItem> cachedMechanicalItems = Lists.newArrayList();
```

In the rebuild block, clear both lists:

```java
this.cachedMenuItems.clear();
this.cachedMechanicalItems.clear();
```

Build an index lookup:

```java
List<BloodManipulation> allManips = manips.getManipList();
List<String> equippedNames = manips.getEquippedManipNames();

addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_ABSORPTION);
addMechanicalManipulation(allManips, ManipulationEquipHelper.BLOOD_PROJECTION);
```

Skip fixed mechanics when building the outer ring:

```java
for (int i = 0; i < allManips.size(); i++) {
	BloodManipulation c = allManips.get(i);
	if (!equippedNames.contains(c.getName()) || ManipulationEquipHelper.isFixedMechanicalManip(c.getName())) {
		continue;
	}
	this.cachedMenuItems.add(createManipulationItem(c, i));
}
```

Add to the menu:

```java
this.menu.clear();
this.menu.addAllInner(this.cachedMechanicalItems);
this.menu.addAll(this.cachedMenuItems);
```

- [ ] **Step 4: Add screen helper methods**

Add:

```java
private void addMechanicalManipulation(List<BloodManipulation> allManips, String manipName) {
	for (int i = 0; i < allManips.size(); i++) {
		BloodManipulation manipulation = allManips.get(i);
		if (manipulation != null && manipName.equals(manipulation.getName())) {
			this.cachedMechanicalItems.add(createManipulationItem(manipulation, i));
			return;
		}
	}
}

private BlitRadialMenuItem createManipulationItem(BloodManipulation manipulation, int slot) {
	BlitRadialMenuItem item = new BlitRadialMenuItem(this.menu, slot,
			Hemomancy.rloc("textures/item/memories/memory_" + manipulation.getName() + "_overlay.png"),
			Hemomancy.rloc("textures/item/memories/memory_blank.png"),
			0, 0, 16, 16, 16, 16,
			Component.literal(manipulation.getProperName())) {
		@Override
		public boolean onClick() {
			PacketHandler.sendToServer(new UpdateCurrentManipPacket(slot));
			RadialChooseManipScreen.this.menu.close();
			return true;
		}
	};
	item.setVisible(true);
	return item;
}
```

Update central text's empty check:

```java
if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)
		&& this.cachedMechanicalItems.stream().noneMatch(RadialMenuItem::isVisible)) {
	this.menu.setCentralText(Component.literal("No Memorized Manipulations"));
}
```

- [ ] **Step 5: Run the screen source test and helper test**

Run:

```powershell
javac -d build/tmp/codex-radial src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreenSourceTest
```

Run:

```powershell
javac -d build/tmp/codex-radial src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationGrantHelperTest
```

Expected: both pass with no output.

### Task 5: Final Verification

**Files:**
- Verify all modified production and test files.

- [ ] **Step 1: Run focused source tests**

Run:

```powershell
javac -d build/tmp/codex-radial src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java src/test/java/com/vincenthuto/hemomancy/client/render/layer/player/CellHandRenderingResourceTest.java; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.client.screen.manips.RadialChooseManipScreenSourceTest; java -cp build/tmp/codex-radial com.vincenthuto.hemomancy.client.render.layer.player.CellHandRenderingResourceTest
```

Expected: no output and exit code `0`.

- [ ] **Step 2: Run the full build**

Run:

```powershell
./gradlew.bat build
```

Expected: build succeeds, with existing deprecation/unchecked warnings allowed.

- [ ] **Step 3: Inspect git diff**

Run:

```powershell
git diff -- src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulations.java src/main/java/com/vincenthuto/hemomancy/common/network/capa/manips/EquipManipulationPacket.java src/main/java/com/vincenthuto/hemomancy/common/command/HemoCommand.java src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java src/test/java/com/vincenthuto/hemomancy/common/capability/player/manip/KnownManipulationGrantHelperTest.java src/test/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreenSourceTest.java
```

Expected: diff only includes the two-layer radial implementation, fixed mechanical equip rules, slot-count messaging, and tests.
