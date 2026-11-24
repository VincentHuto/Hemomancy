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
 * partial tick. Under A-4 every world, item and armour animation freezes under {@code /tick freeze}; the wall-clock
 * users left are listed with their reason, and the three common-side visual helpers, which cannot name a client class,
 * are listed too.
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

	/**
	 * Client code on wall-clock time for good (A-4, 2026-11-14). Every world, item and armour animation reads
	 * {@code AnimationTime} and freezes under {@code /tick freeze}; these do not animate the world.
	 */
	private static final Set<String> WALL_CLOCK_USERS = Set.of(
			// The veil over a dimension change has no level clock: the old level is gone and the new one not yet synced.
			"client/data/ChamberVeil.java",
			"client/screen/ChamberTransitionScreen.java",
			"client/screen/overlay/LidlessPassageView.java",
			// Cache stamps: they time how stale a recorded pose is, not an animation.
			"client/render/item/harbinger/FirstPersonPalm.java",
			"client/render/item/harbinger/HeldWeaponPose.java",
			// HUD smoothing: eases the rite progress bar by frame time.
			"client/screen/overlay/CardinalRiteOverlay.java",
			// Screen chrome.
			"client/screen/item/BloodStructureHintScreen.java",
			"client/screen/item/MnemonicFolioScreen.java",
			"client/screen/item/RiteHintScreen.java",
			"client/screen/item/ScryingDiagnosticsScreen.java",
			"client/screen/item/VascularViewScreen.java",
			"client/screen/item/living/MorphlingJarScreen.java",
			"client/screen/skilltree/shared/CraftingTabView.java",
			"client/screen/skilltree/shared/MaterialsTabView.java",
			"client/screen/skilltree/shared/RitesTabView.java",
			"client/screen/tile/crafting/MorphlingIncubatorScreen.java",
			"client/screen/tile/functional/MnemonicReliquaryScreen.java");

	/** A-4: a rite's client clocks (beat ages, boundary growth, the escalation weather, its heartbeat) stop with the world. */
	@Test
	void riteClientClocksStandStillUnderFreeze() {
		for (String[] tick : new String[][] {
				{ "client/data/CardinalRiteEventClientState.java", "public static void tick(" },
				{ "client/render/world/zone/RiteSpaceMotes.java", "public static void tick(" },
				{ "client/sound/RiteSoundBed.java", "public static void tick(" },
				{ "client/event/ClientTickEvents.java", "public static void onClientTickPost(" } }) {
			SourceAssertions.assertContains(tick[0] + " stops under /tick freeze", SourceAssertions.methodBody(
					SourceAssertions.readMainJava(tick[0]), tick[1]), "AnimationTime.worldTicks()");
		}
		String clientTick = SourceAssertions.methodBody(SourceAssertions.readMainJava("client/event/ClientTickEvents.java"),
				"public static void onClientTickPost(");
		SourceAssertions.assertContains("boundaries grow only while the world ticks", SourceAssertions.section(clientTick,
				"AnimationTime.worldTicks()", "RiteSoundBed.tick("), "ActiveRiteClientData.tick();");
	}

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
				"animate on AnimationTime (A-4); only screen chrome, the dimension veil, cache stamps and the bar ease may list here");
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
