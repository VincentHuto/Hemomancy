package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.platform.NativeImage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class RecipeMapRingStyleTest {
	@Test
	void recipeMapRingRasterMatchesTheSkillsRingRaster() throws Exception {
		int size = 280;
		int center = size / 2;
		int radius = 120;
		int color = 0x1858231F;
		try (NativeImage skillsRing = new NativeImage(size, size, false);
			 NativeImage recipeMapRing = new NativeImage(size, size, false)) {
			Method bakeSkillsRing = SkillTraceLayerCache.class.getDeclaredMethod(
					"bakeDegreeRing", NativeImage.class, int.class, int.class, int.class, int.class);
			bakeSkillsRing.setAccessible(true);
			bakeSkillsRing.invoke(null, skillsRing, center, center, radius, color);

			Method bakeRecipeMapRing = RecipeMapTraceLayerCache.class.getDeclaredMethod(
					"bakeRing", NativeImage.class, RecipeMapTracePlan.Ring.class);
			bakeRecipeMapRing.setAccessible(true);
			bakeRecipeMapRing.invoke(null, recipeMapRing,
					new RecipeMapTracePlan.Ring(center, center, radius, color));

			assertArrayEquals(snapshot(skillsRing), snapshot(recipeMapRing));
		}
	}

	private static int[] snapshot(NativeImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		int index = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				pixels[index++] = image.getPixelRGBA(x, y);
			}
		}
		return pixels;
	}
}
