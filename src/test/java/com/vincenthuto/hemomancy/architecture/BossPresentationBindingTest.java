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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boss presentation: every boss bar names its frame where the bar is built, the frame reaches each viewer every time
 * the bar is shown to them (so a player who joins mid-fight still gets it), and every scene the server plays is a
 * named id the client registers.
 */
class BossPresentationBindingTest {
	private static final Pattern BINDING = Pattern.compile("new BossBarBinding\\(\\s*\"[^\"]+\"\\s*,\\s*([\\w.]+)");
	private static final Pattern PLAY = Pattern.compile("BossScenes\\.play(?:To)?\\(");
	private static final Pattern ID = Pattern.compile("BossPresentationIds\\.(\\w+)");

	@Test
	void everyBindingNamesAFrame() throws IOException {
		Set<String> wrong = new TreeSet<>();
		int found = 0;
		for (Path file : sources()) {
			Matcher matcher = BINDING.matcher(SourceAssertions.read(file));
			while (matcher.find()) {
				found++;
				if (!matcher.group(1).replace("BossPresentationIds.", "").startsWith("FRAME_"))
					wrong.add(RepoPaths.display(file) + ": " + matcher.group(1));
			}
		}
		assertEquals(11, found, "one bar per boss");
		assertEquals(Set.of(), wrong, "pass a BossPresentationIds.FRAME_* id as the bar's frame");
	}

	@Test
	void showSendsTheFrame() {
		String show = SourceAssertions.methodBody(SourceAssertions.readMainJava("common/entity/boss/BossBarBinding.java"),
				"public void show(ServerPlayer player)");
		assertTrue(show.contains("BossBarFramePacket"), "show sends the frame binding to the viewer");
	}

	@Test
	void everyPlayedSceneIsAnId() throws IOException {
		Set<String> wrong = new TreeSet<>();
		for (Path file : sources()) {
			if (file.endsWith("BossScenes.java")) continue;
			String source = SourceAssertions.read(file);
			Matcher matcher = PLAY.matcher(source);
			while (matcher.find()) {
				String call = arguments(source, matcher.end());
				Matcher ids = ID.matcher(call);
				boolean named = false;
				while (ids.find()) {
					named = true;
					if (!ids.group(1).startsWith("SCENE_")) wrong.add(RepoPaths.display(file) + ": " + ids.group(1));
				}
				if (!named) wrong.add(RepoPaths.display(file) + ": " + call);
			}
		}
		assertEquals(Set.of(), wrong, "play a BossPresentationIds.SCENE_* id");
		assertFalse(SourceAssertions.readMainJava("common/entity/boss/presentation/BossPresentationIds.java").isEmpty());
	}

	/** A call's arguments, from just after its opening parenthesis to the matching close. */
	private static String arguments(String source, int from) {
		int depth = 1;
		for (int i = from; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '(') depth++;
			else if (c == ')' && --depth == 0) return source.substring(from, i);
		}
		return source.substring(from);
	}

	private static List<Path> sources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.filter(path -> !RepoPaths.display(path).contains("/compat/mna/")).toList();
		}
	}
}
