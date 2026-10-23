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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step 10 (roadmap Phase 9, A-1): a boss replays a synced tick counter through {@code BossReplayClock} and times its
 * moves with {@code BossMoveTimeline}; no class keeps its own replay clock. The renderers that still read a synced
 * counter plus the partial tick are listed until decision A-1b moves them onto the clock.
 */
class BossAnimationClockGuardrailTest {
	private static final Pattern HAND_CLOCK = Pattern.compile(
			"tickCount\\s*-\\s*client\\w*Start|Math\\.abs\\([^;]*tickCount[^;]*\\)\\s*>\\s*\\d");
	private static final Pattern RAW_SYNCED = Pattern.compile("get\\w*Tick\\(\\)\\s*\\+\\s*partial");
	private static final List<String> BOSS_CLIENT = List.of("client/model/entity/boss/", "client/render/entity/boss/",
			"client/render/layer/mob/endgame/");
	/** Raw synced counter plus partial tick, pending decision A-1b. */
	private static final Set<String> RAW_READERS = Set.of(
			"client/model/entity/boss/annetta/LatentAnnettaInfectionModel.java",
			"client/model/entity/boss/annetta/StainedPriestessModel.java",
			"client/model/entity/boss/endgame/MycophantModel.java",
			"client/model/entity/boss/endgame/VesperTheEveningStarModel.java",
			"client/render/entity/boss/annetta/LatentAnnettaInfectionRenderer.java",
			"client/render/entity/boss/annetta/LatentInfectionLayer.java",
			"client/render/layer/mob/endgame/VesperMountAbsorptionLayer.java");

	@Test
	void oldNamesAreGone() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path root : List.of(RepoPaths.MAIN_JAVA, RepoPaths.TOOLS.resolve("model_export"))) {
			for (Path file : files(root)) {
				String text = SourceAssertions.read(file);
				if (text.contains("VesperAnimClock") || text.contains("VesperMoveTimeline")) found.add(RepoPaths.display(file));
			}
		}
		assertEquals(Set.of(), found, "use BossReplayClock and BossMoveTimeline");
	}

	@Test
	void noHandWrittenReplayClocks() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : files(RepoPaths.MAIN_JAVA)) {
			if (HAND_CLOCK.matcher(SourceAssertions.read(file)).find()) found.add(relative(file));
		}
		assertEquals(Set.of(), found, "replay a synced counter with BossReplayClock");
		assertTrue(SourceAssertions.readMainJava("common/entity/boss/annetta/AnnettaKnowlesEntity.java")
				.contains("actionClock.sync(getAction(), getActionTick(), this.tickCount, 1);"));
	}

	@Test
	void rawSyncedReadersAreListed() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : files(RepoPaths.MAIN_JAVA)) {
			String path = relative(file);
			if (BOSS_CLIENT.stream().anyMatch(path::startsWith) && RAW_SYNCED.matcher(SourceAssertions.read(file)).find())
				found.add(path);
		}
		assertEquals(new TreeSet<>(RAW_READERS), found,
				"replay the synced counter with BossReplayClock, or list the reader here under decision A-1b");
	}

	private static String relative(Path file) {
		return RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
	}

	private static List<Path> files(Path root) throws IOException {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".py")).toList();
		}
	}
}
