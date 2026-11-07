package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A-2: easing and interpolation come from {@code common/util/HemoEasing}. No other class declares its own scalar
 * {@code smoothstep}, {@code smooth}, {@code lerp} or {@code clamp01}, or a one-argument {@code clamp} or {@code ease},
 * unless it is listed here with the reason it differs. Stale entries fail. The animation templates under
 * {@code tools/model_export} are checked too, because their generators write them into models. A keyframe curve over
 * {@code (float t, float... keys)} is {@code HemoEasing.catmullRom} (Annetta's Catmull-Rom, lifted for the Saint
 * tracks); a differing one is listed with its reason.
 */
class EasingCopyGuardrailTest {
	private static final String HEMO_EASING = "src/main/java/com/vincenthuto/hemomancy/common/util/HemoEasing.java";

	private static final Map<String, String> ALLOWED = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationUndulation.java#smooth",
			"Edge smoothstep with a minimum span so equal edges cannot divide by zero; delegates to HemoEasing",
			"src/main/java/com/vincenthuto/hemomancy/client/player/WaistPivotRules.java#clamp",
			"Clamps a pitch to plus or minus LIMIT, not to [0,1]",
			"src/main/java/com/vincenthuto/hemomancy/client/render/cinematic/BossSceneTracks.java#keys",
			"Smoothstep segments through scene keys with no carried velocity; not the Catmull-Rom of HemoEasing",
			"src/main/java/com/vincenthuto/hemomancy/client/data/MycophantIntroClientState.java#keys",
			"Delegates to BossSceneTracks.keys");

	private static final Pattern EASING_DECLARATION = Pattern.compile(
			"\\b(?:float|double)\\s+(smoothstep|smoothStep|smooth|smootherstep|lerp|clamp01)\\s*\\(");
	private static final Pattern ONE_ARGUMENT_CLAMP = Pattern.compile(
			"\\b(?:float|double)\\s+(clamp|ease)\\s*\\(\\s*(?:final\\s+)?(?:float|double)\\s+\\w+\\s*\\)");

	/** A keyframe curve over a time and varargs keys: the Catmull-Rom copy Annetta's model used to carry. */
	private static final Pattern KEYFRAME_CURVE = Pattern.compile(
			"\\b(?:float|double)\\s+(\\w+)\\s*\\(\\s*(?:final\\s+)?(?:float|double)\\s+\\w+\\s*,\\s*(?:final\\s+)?"
					+ "(?:float|double)\\s*\\.\\.\\.\\s*\\w+\\s*\\)");

	/** Vanilla's quintic smoothstep is not the mod's cubic one; a call to it is a copy by another route. */
	private static final Pattern VANILLA_SMOOTHSTEP = Pattern.compile("\\bMth\\.(smoothstep)\\s*\\(");

	@Test
	void easingIsDeclaredOnlyInHemoEasing() throws Exception {
		List<String> found = new ArrayList<>();
		try (Stream<Path> files = Stream.concat(Files.walk(RepoPaths.MAIN_JAVA),
				Files.walk(RepoPaths.TOOLS.resolve("model_export")))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (shown.equals(HEMO_EASING)) continue;
				String text = SourceAssertions.read(file);
				collect(text, EASING_DECLARATION, shown, found);
				collect(text, ONE_ARGUMENT_CLAMP, shown, found);
				collect(text, VANILLA_SMOOTHSTEP, shown, found);
				collect(text, KEYFRAME_CURVE, shown, found);
			}
		}
		assertEquals(new TreeSet<>(ALLOWED.keySet()), new TreeSet<>(found),
				"declare easing in HemoEasing (a differing copy becomes a named variant there) or list it here with a reason");
	}

	private static void collect(String text, Pattern pattern, String file, List<String> found) {
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			found.add(file + "#" + matcher.group(1));
		}
	}
}
