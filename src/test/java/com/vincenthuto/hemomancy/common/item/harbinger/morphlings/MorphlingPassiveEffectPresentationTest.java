package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.support.EnglishText;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/** Every strain row ships its model, art, incubation recipe and text (Brined Court Track 1). */
final class MorphlingPassiveEffectPresentationTest {
	@Test
	void everyStrainHasItsResources() throws IOException {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String path = strain.itemPath();
			assertTrue(ResourceReader.exists("assets/hemomancy/models/item/" + path + ".json"), path + " model");
			assertTrue(ResourceReader.exists("data/hemomancy/recipe/incubator/" + path + ".json"), path + " recipe");
			for (String folder : new String[] {"", "anim/", "anim2/"}) {
				assertTrue(ResourceReader.exists("assets/hemomancy/textures/gui/morphling_overlay/" + folder
						+ strain.hudTexture() + ".png"), path + " HUD sprite " + folder);
			}
			assertSixteenSquare("assets/hemomancy/textures/item/" + path + ".png");
			assertSixteenSquare("assets/hemomancy/textures/mob_effect/" + path + ".png");
			String name = EnglishText.of(strain.folkNameKey());
			assertTrue(name.endsWith(" Morphling"), path + " item name");
			assertEquals(name, EnglishText.of(strain.passiveEffectKey()), path + " effect shares the item name");
			assertFalse(EnglishText.of(strain.binomialKey()).isBlank(), path + " binomial");
			assertFalse(EnglishText.of("material.hemomancy." + path + ".desc").isBlank(), path + " atlas text");
		}
	}

	@Test
	void incubationAsksForThePartnerFamilyCatalyst() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			String recipe = ResourceReader.text("data/hemomancy/recipe/incubator/" + strain.itemPath() + ".json");
			assertTrue(recipe.contains("\"" + strain.polypFamily().hintItemId() + "\""),
					strain.id() + " incubation must use its partner family's catalyst");
			assertTrue(recipe.contains("\"hemomancy:" + strain.itemPath() + "\""), strain.id() + " result");
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

	private static void assertSixteenSquare(String resource) throws IOException {
		assertTrue(ResourceReader.exists(resource), "missing " + resource);
		BufferedImage image = ImageIO.read(ResourceReader.path(resource).toFile());
		assertEquals(16, image.getWidth(), resource + " width");
		assertEquals(16, image.getHeight(), resource + " height");
	}
}
