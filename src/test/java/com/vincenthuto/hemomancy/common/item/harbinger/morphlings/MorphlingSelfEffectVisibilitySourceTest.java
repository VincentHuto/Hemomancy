package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingSelfEffectVisibilitySourceTest {
	private static final Path MORPHLING_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings");
	private static final Pattern SELF_EFFECT = Pattern.compile(
			"(?s)(?:player|playerIn)\\.addEffect\\(\\s*new MobEffectInstance\\((.*?)\\)\\s*\\)");
	private static final Pattern EFFECT_FLAGS = Pattern.compile(
			",\\s*(true|false)\\s*,\\s*(true|false)\\s*,\\s*(true|false)\\s*$");

	@Test
	void morphlingEffectsAppliedToThePlayerHidePotionParticles() throws IOException {
		int selfEffectCount = 0;
		try (Stream<Path> files = Files.list(MORPHLING_SOURCE)) {
			for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".java")).toList()) {
				String source = Files.readString(file);
				Matcher effects = SELF_EFFECT.matcher(source);
				while (effects.find()) {
					selfEffectCount++;
					Matcher flags = EFFECT_FLAGS.matcher(effects.group(1));
					assertTrue(flags.find(), () -> file + " must use explicit MobEffectInstance visibility flags");
					assertEquals("false", flags.group(2),
							() -> file + " must hide particles for self effect: " + effects.group());
				}
			}
		}
		assertTrue(selfEffectCount > 0, "expected to find Morphling self effects");
	}
}
