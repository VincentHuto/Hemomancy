package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PaleIntercessionAssetContractTest {
	private static final Path ROOT = Path.of("src/main/resources");

	@Test
	void authoredModelContainsEveryPresentationChannel() throws Exception {
		var json = JsonParser.parseString(Files.readString(ROOT.resolve(
				"assets/hemomancy/models/entity/bbmodel/pale_intercession.bbmodel"))).getAsJsonObject();
		assertEquals(128, json.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(7, json.getAsJsonArray("animations").size());
		String model = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/PaleIntercessionAnimations.java"));
		for (String channel : new String[]{"MANIFEST", "STILL", "GLIDE", "INTERPOSE", "STRIKE", "DISSOLVE", "DISTORT"}) {
			assertTrue(model.contains("AnimationDefinition " + channel), "missing exported animation " + channel);
		}
	}

	@Test
	void runtimeTexturesAreTranslucent128PixelAtlases() throws Exception {
		for (String name : new String[]{"base.png", "emissive.png"}) {
			var image = ImageIO.read(ROOT.resolve("assets/hemomancy/textures/entity/pale_intercession/" + name).toFile());
			assertNotNull(image);
			assertEquals(128, image.getWidth());
			assertEquals(128, image.getHeight());
			assertTrue(image.getColorModel().hasAlpha());
		}
	}

	@Test
	void rendererUsesDedicatedTranslucentLayers() throws Exception {
		String renderer = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/PaleIntercessionRenderer.java"));
		String glow = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/layer/mob/PaleIntercessionGlowLayer.java"));
		assertTrue(renderer.contains("entityTranslucent(TEXTURE)"));
		assertTrue(glow.contains("entityTranslucentEmissive"));
		assertFalse(renderer.contains("blank.png"));
	}

	@Test
	void dedicatedSoundsAreValidOggStreams() throws Exception {
		for (String name : new String[]{"manifest.ogg", "strike.ogg", "dissolve.ogg"}) {
			byte[] bytes = Files.readAllBytes(ROOT.resolve("assets/hemomancy/sounds/entity/pale_intercession/" + name));
			assertTrue(bytes.length > 1_000);
			assertArrayEquals(new byte[]{'O','g','g','S'}, new byte[]{bytes[0],bytes[1],bytes[2],bytes[3]});
		}
	}

	@Test
	void playerFacingResourcesExist() {
		assertTrue(Files.isRegularFile(ROOT.resolve("data/hemomancy/books/liberimmaculatus/our_lady/pages/pale_intercession.json")));
		assertTrue(Files.isRegularFile(ROOT.resolve("data/hemomancy/damage_type/pale_intercession.json")));
	}

	@Test
	void compatibilityIdAndOwnerAttributedDamageRemainWired() throws Exception {
		String registry = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String damage = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/damage/HemoDamageTypes.java"));
		String entity = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/PaleIntercessionEntity.java"));
		assertTrue(registry.contains("spectral_companion = ENTITY_TYPES.register("));
		assertTrue(registry.contains("\"spectral_companion\""));
		assertTrue(damage.contains("manifestation, owner"));
		assertTrue(entity.contains("paleIntercession(level(), this, owner)"));
	}

	@Test
	void defensiveTargetExclusionsRemainExplicit() throws Exception {
		String entity = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/entity/summon/PaleIntercessionEntity.java"));
		for (String exclusion : new String[]{"target instanceof Player", "target instanceof PaleIntercessionEntity",
				"UnstainedAcolyteEntity", "UnstainedGuardianEntity", "UnstainedScoutEntity", "UnstainedZealotEntity",
				"target.isAlliedTo(owner)", "target instanceof OwnableEntity", "target instanceof BoundPuppeteerSummon"}) {
			assertTrue(entity.contains(exclusion), "missing target exclusion: " + exclusion);
		}
	}
}
