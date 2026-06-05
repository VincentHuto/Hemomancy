package com.vincenthuto.hemomancy.common.effect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MobEffectIconCoverageResourceTest {
	private static final Path EFFECT_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java");
	private static final Path MOB_EFFECT_TEXTURES = Path.of("src/main/resources/assets/hemomancy/textures/mob_effect");
	private static final Pattern EFFECT_REGISTRATION = Pattern.compile("EFFECTS\\.register\\(\"([a-z0-9_]+)\"");

	private MobEffectIconCoverageResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(EFFECT_INIT);
		Matcher matcher = EFFECT_REGISTRATION.matcher(source);
		while (matcher.find()) {
			String id = matcher.group(1);
			Path icon = MOB_EFFECT_TEXTURES.resolve(id + ".png");
			if (!Files.exists(icon)) {
				throw new AssertionError("Missing mob effect icon for " + id + ": " + icon);
			}
		}
	}
}
