package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Track 10: Putriciel's fire is the Flammeus look (crimson, pink-white heat, black soot), never green or bile-yellow.
 * The Rot Pyre bakes no flame (its renderer draws the shader fire), and no full-bright texel of his fire pieces is
 * green or yellow. Each new variant has its blockstate entries and models.
 */
class PutricielFireResourceTest {
	@ParameterizedTest
	@ValueSource(strings = {"rot_pyre", "rot_pyre_lit"})
	void theRotPyreBakesNoFlame(String model) {
		for (JsonElement element : ResourceReader.asset("models/block/" + model + ".json").getAsJsonArray("elements")) {
			String name = element.getAsJsonObject().get("name").getAsString();
			assertFalse(name.contains("flame"), model + " bakes a flame plane (" + name + "); the renderer draws the fire");
		}
	}

	@Test
	void theUnlitPyreGivesNoGlow() {
		for (JsonElement element : ResourceReader.asset("models/block/rot_pyre.json").getAsJsonArray("elements")) {
			assertFalse(element.getAsJsonObject().has("neoforge_data"), "an unlit pyre shows only cold char");
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"rot_pyre_lit", "reliquary_sconce_smoulder", "pulse_pylon_pyre_upper", "spawn_mark_ash_0_due"})
	void putricielsFireIsNeverGreenOrYellow(String model) throws IOException {
		JsonObject json = ResourceReader.asset("models/block/" + model + ".json");
		String texture = json.getAsJsonObject("textures").get("atlas").getAsString().replace("hemomancy:", "");
		BufferedImage atlas = ImageIO.read(RepoPaths.ASSETS.resolve("textures/" + texture + ".png").toFile());
		int bright = 0;
		for (JsonElement element : json.getAsJsonArray("elements")) {
			JsonObject el = element.getAsJsonObject();
			if (!el.has("neoforge_data")) continue;
			for (Map.Entry<String, JsonElement> face : el.getAsJsonObject("faces").entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				int x0 = pixel(uv.get(0).getAsDouble(), atlas.getWidth()), x1 = pixel(uv.get(2).getAsDouble(), atlas.getWidth());
				int y0 = pixel(uv.get(1).getAsDouble(), atlas.getHeight()), y1 = pixel(uv.get(3).getAsDouble(), atlas.getHeight());
				for (int y = Math.min(y0, y1); y < Math.max(y0, y1); y++) {
					for (int x = Math.min(x0, x1); x < Math.max(x0, x1); x++) {
						int argb = atlas.getRGB(x, y);
						if ((argb >>> 24) == 0) continue;
						int r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
						bright++;
						boolean green = g > r;
						boolean yellow = g > r * 0.7 && b < g * 0.8;
						assertFalse(green || yellow, model + "." + el.get("name").getAsString() + " " + face.getKey()
								+ " has a green or yellow texel (" + r + ", " + g + ", " + b + ") at " + x + "," + y);
					}
				}
			}
		}
		assertTrue(bright > 0, model + " has full-bright embers or seep to check");
	}

	@Test
	void everyNewVariantHasItsModels() {
		Map<String, String> variants = Map.of(
				"reliquary_sconce", "facing=north,flame=smoulder",
				"pulse_pylon", "facing=north,part=upper,style=pyre",
				"litany_stone", "charred=true,facing=north,half=lower",
				"spawn_mark", "due=true,style=ash,wave=0");
		for (Map.Entry<String, String> entry : variants.entrySet()) {
			JsonObject states = ResourceReader.asset("blockstates/" + entry.getKey() + ".json").getAsJsonObject("variants");
			assertTrue(states.has(entry.getValue()), entry.getKey() + " has " + entry.getValue());
			String model = states.getAsJsonObject(entry.getValue()).get("model").getAsString();
			assertTrue(ResourceReader.exists("assets/hemomancy/models/" + model.replace("hemomancy:", "") + ".json"),
					entry.getKey() + " " + entry.getValue() + " -> " + model);
		}
		assertEquals("hemomancy:block/pulse_pylon_upper", ResourceReader.asset("blockstates/pulse_pylon.json")
				.getAsJsonObject("variants").getAsJsonObject("facing=north,part=upper,style=venous").get("model")
				.getAsString(), "the venous pylon keeps its model");
	}

	private static int pixel(double uv, int size) {
		return (int) Math.round(uv * size / 16.0);
	}
}
