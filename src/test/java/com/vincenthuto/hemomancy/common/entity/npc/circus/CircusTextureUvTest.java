package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.nio.file.Files;
import com.google.gson.JsonParser;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusTextureUvTest {
	@Test
	void facesUseOneHeadCubeWithPaintedEyes() throws Exception {
		for (String actor : new String[] { "Ringmaster", "KnifeThrower", "FireEater", "Acrobat", "StiltWalker" }) {
			var path = Path.of("src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus/Circus" + actor + "Model.bbmodel");
			var model = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
			int heads = 0;
			for (var node : model.getAsJsonArray("outliner")) {
				var group = node.getAsJsonObject();
				if (!group.get("name").getAsString().equals("head")) continue;
				for (var child : group.getAsJsonArray("children")) if (child.isJsonPrimitive()) heads++;
			}
			org.junit.jupiter.api.Assertions.assertEquals(1, heads, actor + " must use one head cube");
			for (var entry : model.getAsJsonArray("elements")) {
				var element = entry.getAsJsonObject();
				String name = element.get("name").getAsString();
				assertTrue(!name.startsWith("mask_") && !name.startsWith("eye_bridge"), name);
				if (!name.startsWith("head_0_0_")) continue;
				var uv = element.getAsJsonObject("faces").getAsJsonObject("north").getAsJsonArray("uv");
				int x = (int) Math.floor(uv.get(0).getAsDouble());
				int y = (int) Math.floor(uv.get(1).getAsDouble());
				for (var texture : model.getAsJsonArray("textures")) {
					String file = texture.getAsJsonObject().get("name").getAsString();
					var image = ImageIO.read(TEXTURES.resolve(file).toFile());
					assertTrue(image.getRGB(x + 1, y + 3) != image.getRGB(x, y + 3), file + " needs painted eyes");
				}
			}
		}
	}
	private static final Path TEXTURES = Path.of(
			"src/main/resources/assets/hemomancy/textures/entity/npc/harbinger/circus");

	@Test
	void baseTexturesPaintUvIslandsInsteadOfTheWholeAtlas() throws Exception {
		String[] names = {
				"fire_eater_0.png", "fire_eater_1.png", "stilt_walker_0.png", "stilt_walker_1.png",
				"acrobat_0.png", "acrobat_1.png", "knife_thrower_0.png", "knife_thrower_1.png",
				"ringmaster.png", "carousel.png"
		};
		for (String name : names) {
			var image = ImageIO.read(TEXTURES.resolve(name).toFile());
			int transparent = 0;
			int painted = 0;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					if ((image.getRGB(x, y) >>> 24) == 0) transparent++;
					else painted++;
				}
			}
			assertTrue(transparent > image.getWidth() * image.getHeight() / 10,
					name + " must leave unused UV space transparent");
			assertTrue(painted > 0, name + " must paint at least one model face");
		}
	}
}
