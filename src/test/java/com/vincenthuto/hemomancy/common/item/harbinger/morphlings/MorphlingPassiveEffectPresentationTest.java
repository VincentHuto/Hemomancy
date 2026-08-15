package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.minecraft.world.effect.MobEffectInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MorphlingPassiveEffectPresentationTest {
	private static final Path MORPHLING_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings");
	private static final Path LANGUAGE = Path.of(
			"src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path EFFECT_ICONS = Path.of(
			"src/main/resources/assets/hemomancy/textures/mob_effect");

	private static final Map<String, String> PASSIVE_EFFECTS = Map.ofEntries(
			Map.entry("DeadmansPurseMorphlingItem.java", "morphling_deadmans_purse"),
			Map.entry("GravecapMorphlingItem.java", "morphling_gravecap"),
			Map.entry("WitchsEarMorphlingItem.java", "morphling_witchs_ear"),
			Map.entry("LumenlaceMorphlingItem.java", "morphling_lumenlace"),
			Map.entry("BootlaceMorphlingItem.java", "morphling_bootlace"),
			Map.entry("IrontoothMorphlingItem.java", "morphling_irontooth"),
			Map.entry("EmberfangMorphlingItem.java", "morphling_emberfang"),
			Map.entry("WinterShroudMorphlingItem.java", "morphling_winter_shroud"));
	private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
			Map.entry("morphling_deadmans_purse", "Deadman's Purse Morphling"),
			Map.entry("morphling_gravecap", "Gravecap Morphling"),
			Map.entry("morphling_witchs_ear", "Witch's Ear Morphling"),
			Map.entry("morphling_lumenlace", "Lumenlace Morphling"),
			Map.entry("morphling_bootlace", "Bootlace Morphling"),
			Map.entry("morphling_irontooth", "Irontooth Morphling"),
			Map.entry("morphling_emberfang", "Emberfang Morphling"),
			Map.entry("morphling_winter_shroud", "Winter Shroud Morphling"));

	@Test
	void canonicalMorphlingsUseTheRenamedPassiveEffectPresentation() throws IOException {
		String language = Files.readString(LANGUAGE);
		String morphlingItem = Files.readString(MORPHLING_SOURCE.resolve("MorphlingItem.java"));
		assertTrue(morphlingItem.contains("effectDurationTicks(interval), amplifier, false, false, true"),
				"passive effects must hide particles while retaining their inventory icons");
		for (Map.Entry<String, String> entry : PASSIVE_EFFECTS.entrySet()) {
			String source = Files.readString(MORPHLING_SOURCE.resolve(entry.getKey()));
			String effectId = entry.getValue();
			assertTrue(source.contains("MorphlingItem.applyPassiveEffect(player, stack, EffectInit."
					+ effectId), () -> entry.getKey() + " must use " + effectId);
			assertFalse(Pattern.compile("new MobEffectInstance\\(EffectInit\\." + effectId
					+ "\\s*,\\s*100\\b").matcher(source).find(),
					() -> entry.getKey() + " still owns the passive effect duration directly");
			assertTrue(language.contains("\"effect.hemomancy." + effectId + "\": \""
					+ DISPLAY_NAMES.get(effectId) + "\""),
					() -> "Missing renamed display name for " + effectId);
			Path iconPath = EFFECT_ICONS.resolve(effectId + ".png");
			assertTrue(Files.exists(iconPath),
					() -> "Missing icon for " + effectId);
			BufferedImage icon = ImageIO.read(iconPath.toFile());
			assertEquals(16, icon.getWidth(), () -> effectId + " icon must be 16 pixels wide");
			assertEquals(16, icon.getHeight(), () -> effectId + " icon must be 16 pixels high");
		}
	}

	@Test
	void passiveDurationIsInfiniteWhileEquipped() {
		int duration = MorphlingPassiveEffectRules.effectDurationTicks(60);

		assertEquals(MobEffectInstance.INFINITE_DURATION, duration,
				"equipped Morphling passives must not count down while equipped");
		assertFalse(MorphlingPassiveEffectRules.shouldRefresh(duration, 0, 0, 60),
				"an infinite passive must not be reapplied every upkeep tick");
		assertTrue(MorphlingPassiveEffectRules.shouldRefresh(100, 0, 0, 60),
				"a finite legacy passive must be replaced by the infinite passive");
		assertTrue(MorphlingPassiveEffectRules.shouldRefresh(duration, 0, 1, 60),
				"a maturity change must update the passive amplifier");
	}
}
