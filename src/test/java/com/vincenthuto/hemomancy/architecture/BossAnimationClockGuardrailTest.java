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
 * moves with {@code BossMoveTimeline}; no class keeps its own replay clock, and no boss model, renderer, layer or entity
 * reads a synced counter plus the partial tick (decision A-1b, done 2026-10-24).
 */
class BossAnimationClockGuardrailTest {
	private static final Pattern HAND_CLOCK = Pattern.compile(
			"tickCount\\s*-\\s*client\\w*Start|Math\\.abs\\([^;]*tickCount[^;]*\\)\\s*>\\s*\\d");
	/** A synced counter ({@code getXTick()} or {@code getXTicks()}) plus the partial tick. */
	private static final Pattern RAW_SYNCED = Pattern.compile("get\\w*Ticks?\\(\\)\\s*\\+\\s*partial");
	/** Where boss animation time is read: the client code, and the boss entities' own per-frame getters. */
	private static final List<String> BOSS_CODE = List.of("client/model/entity/boss/", "client/render/entity/boss/",
			"client/render/layer/mob/endgame/", "common/entity/boss/");

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
	void noRawSyncedReaders() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : files(RepoPaths.MAIN_JAVA)) {
			String path = relative(file);
			if (BOSS_CODE.stream().anyMatch(path::startsWith) && RAW_SYNCED.matcher(SourceAssertions.read(file)).find())
				found.add(path);
		}
		assertEquals(Set.of(), found, "replay the synced counter with BossReplayClock instead of adding the partial tick");
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
