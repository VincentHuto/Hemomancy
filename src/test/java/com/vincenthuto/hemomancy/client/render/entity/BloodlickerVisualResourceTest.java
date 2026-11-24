package com.vincenthuto.hemomancy.client.render.entity;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.BloodlickerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class BloodlickerVisualResourceTest {
	private static final Path PROJECT = Path.of("docs/art/bloodlicker/bloodlicker_redesign.bbmodel");
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final String TEXTURE = "textures/entity/bloodlicker/model_bloodlicker.png";

	@Test
	void runtimeTextureMatchesTheBlockbenchAtlas() throws Exception {
		var project = JsonParser.parseString(Files.readString(PROJECT)).getAsJsonObject();
		String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
		assertArrayEquals(Base64.getDecoder().decode(embedded.split(",", 2)[1]),
				Files.readAllBytes(ASSETS.resolve(TEXTURE)));
		var atlas = ImageIO.read(ASSETS.resolve(TEXTURE).toFile());
		int wings = 0;
		for (var item : project.getAsJsonArray("elements")) {
			var cube = item.getAsJsonObject();
			if (!cube.get("name").getAsString().equals("Wing membrane")) continue;
			wings++;
			var uv = cube.getAsJsonObject("faces").getAsJsonObject("north").getAsJsonArray("uv");
			int visible = 0;
			int cutout = 0;
			for (int y = uv.get(1).getAsInt(); y < uv.get(3).getAsInt(); y++) {
				for (int x = uv.get(0).getAsInt(); x < uv.get(2).getAsInt(); x++) {
					if ((atlas.getRGB(x, y) >>> 24) == 0) cutout++;
					else visible++;
				}
			}
			assertTrue(visible > 0 && cutout > 0);
		}
		assertEquals(2, wings);
	}

	@Test
	void nativeRigCanAnimateBothWingsAndAllSixLegs() {
		ModelPart thorax = new BloodlickerModel().root().getChild("thorax");
		assertTrue(thorax.hasChild("crop"));
		assertTrue(thorax.getChild("head").hasChild("siphon"));
		for (String side : new String[]{"left", "right"}) {
			assertTrue(thorax.hasChild(side + "_wing"));
			assertTrue(thorax.getChild(side + "_arm").hasChild(side + "_forearm"));
			assertTrue(thorax.hasChild(side + "_middle_leg"));
			assertTrue(thorax.getChild(side + "_hind_leg").hasChild(side + "_hind_shin"));
		}
	}
}
