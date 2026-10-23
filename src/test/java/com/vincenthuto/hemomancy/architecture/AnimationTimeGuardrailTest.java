package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 10 (roadmap Phase 9, A-4): client animation time comes from {@code AnimationTime}: the frame's partial tick,
 * game time plus a partial tick, or an entity's age plus a partial tick. Render events and HUD layers pass their own
 * partial tick. Wall-clock animation is listed until decision A-4 settles which animations may freeze, and the three
 * common-side visual helpers, which cannot name a client class, are listed too.
 */
class AnimationTimeGuardrailTest {
	private static final String HELPER = "client/render/AnimationTime.java";
	/** A frame-timer read, or game time or an entity's age plus any partial tick, cast or not. */
	private static final Pattern RAW = Pattern.compile("getTimer\\(\\)\\.getGameTimeDeltaPartialTick\\("
			+ "|(?:getGameTime\\(\\)|\\btickCount)\\s*\\+\\s*(?:\\((?:float|double)\\)\\s*)?"
			+ "(?:partial\\w*|pPartialTicks|event\\.getPartialTick|AnimationTime|HLClientUtils\\.getPartialTicks)");
	private static final Pattern WALL_CLOCK = Pattern.compile(
			"System\\.currentTimeMillis\\(\\)|Util\\.getMillis\\(\\)|System\\.nanoTime\\(\\)|getFrameTimeNs\\(\\)");

	/** Common-side visual helpers that add a partial tick themselves. */
	private static final Set<String> COMMON_RAW = Set.of(
			"common/entity/utility/AwakenedIchorianSigilEntity.java",
			"common/entity/utility/HumanitySpriteEntity.java",
			"common/tile/harbinger/functional/CovenantThroneBlockEntity.java");

	/** Client code on wall-clock time, pending decision A-4. */
	private static final Set<String> WALL_CLOCK_USERS = Set.of(
			"client/render/armor/SilentArchonArmorRenderHelper.java",
			"client/render/item/MemoryOfVesperItemRenderer.java",
			"client/render/item/MonolithFragmentItemRenderer.java",
			"client/render/item/MonolithImbuedClothItemRenderer.java",
			"client/render/item/MonolithicCornerstoneItemRenderer.java",
			"client/render/item/MorphicNectarItemDecorator.java",
			"client/render/item/QliphothPomeItemRenderer.java",
			"client/render/item/QliphothSeedItemRenderer.java",
			"client/render/item/ScarPatternItemRenderer.java",
			"client/render/item/harbinger/FirstPersonPalm.java",
			"client/render/item/harbinger/HeldWeaponPose.java",
			"client/render/item/tile/harbinger/functional/CovenantThroneItemRenderer.java",
			"client/render/item/tile/harbinger/functional/HematicStakeItemRenderer.java",
			"client/render/item/tile/harbinger/functional/SanguineMonolithItemRenderer.java",
			"client/render/item/tile/harbinger/functional/SpecimenJarItemRenderer.java",
			"client/render/tile/harbinger/functional/MorphlingCradleRenderer.java",
			"client/screen/item/BloodStructureHintScreen.java",
			"client/screen/item/MnemonicFolioScreen.java",
			"client/screen/item/RiteHintScreen.java",
			"client/screen/item/ScryingDiagnosticsScreen.java",
			"client/screen/item/VascularViewScreen.java",
			"client/screen/item/living/MorphlingJarScreen.java",
			"client/screen/overlay/CardinalRiteOverlay.java",
			"client/screen/overlay/UnstainedGaugeOverlay.java",
			"client/screen/skilltree/shared/CraftingTabView.java",
			"client/screen/skilltree/shared/MaterialsTabView.java",
			"client/screen/skilltree/shared/RitesTabView.java",
			"client/screen/tile/crafting/MorphlingIncubatorScreen.java",
			"client/screen/tile/functional/MnemonicReliquaryScreen.java");

	@Test
	void clientTimeComesFromAnimationTime() throws IOException {
		assertEquals(Set.of(), matching(RAW, "client/", "mixin/"), "use AnimationTime for the partial tick and animation time");
	}

	@Test
	void commonSideRawTimeIsListed() throws IOException {
		assertEquals(new TreeSet<>(COMMON_RAW), matching(RAW, "common/"),
				"common code cannot name AnimationTime; list a new visual helper here");
	}

	@Test
	void wallClockAnimationIsListed() throws IOException {
		assertEquals(new TreeSet<>(WALL_CLOCK_USERS), matching(WALL_CLOCK, "client/", "mixin/"),
				"animate on AnimationTime, or list the wall-clock user here until decision A-4 is settled");
	}

	private static Set<String> matching(Pattern pattern, String... roots) throws IOException {
		Set<String> found = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String path = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
				if (path.equals(HELPER) || List.of(roots).stream().noneMatch(path::startsWith)) continue;
				if (pattern.matcher(SourceAssertions.read(file)).find()) found.add(path);
			}
		}
		return found;
	}
}
