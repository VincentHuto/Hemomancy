package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;

final class VesperModelAssetContractTest {
	private static final Path TEXTURE_ROOT = Path.of(
			"src/main/resources/assets/hemomancy/textures/entity/boss/endgame");

	@Test
	void crownedRefusalUsesTheAuthoredThroneAndSixLeggedMountHierarchy() {
		ModelPart whole = VesperTheCrownedRefusalModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart upperBody = whole.getChild("upperBody");
		ModelPart rider = upperBody.getChild("vesper");
		ModelPart mount = whole.getChild("lowerBody");

		assertNotNull(upperBody.getChild("throne"));
		assertNotNull(upperBody.getChild("head").getChild("upperJaw"));
		assertNotNull(rider.getChild("head2"));
		assertNotNull(rider.getChild("leftArm").getChild("lShoulder").getChild("lElbow"));
		assertNotNull(mount.getChild("fLeftArm"));
		assertNotNull(mount.getChild("fRightArm"));
		assertNotNull(mount.getChild("lLegs").getChild("fLeg3"));
		assertNotNull(mount.getChild("lLegs2").getChild("fLeg6"));
		assertNotNull(mount.getChild("backAbdomen").getChild("tail")
				.getChild("tail2").getChild("tail3").getChild("tail4").getChild("tail5").getChild("stinger"));
	}

	@Test
	void eveningStarUsesTheAuthoredClothAndWeaponAttachmentHierarchy() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");

		assertNotNull(whole.getChild("head").getChild("hood1"));
		assertNotNull(whole.getChild("body").getChild("ClothBack").getChild("ClothBack1")
				.getChild("ClothBack2"));
		assertNotNull(whole.getChild("leftArm").getChild("lShoulder").getChild("lElbow"));
		assertNotNull(whole.getChild("rightArm").getChild("rShoulder").getChild("rElbow"));
		assertNotNull(whole.getChild("leftLeg").getChild("leftLeg2"));
		assertNotNull(whole.getChild("rightLeg").getChild("rightLeg2"));
	}

	@Test
	void vesperHeadsRotateAroundTheAuthoredNeckPivots() {
		ModelPart crownedWhole = VesperTheCrownedRefusalModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart crownedHead = crownedWhole.getChild("upperBody").getChild("vesper").getChild("head2");
		assertPartPosition(crownedHead, -16.1725F, -4.6424F, -1.3872F);
		assertPartPosition(crownedHead.getChild("hood1"), 22.0F, 3.075F, -0.375F);

		ModelPart eveningWhole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart eveningHead = eveningWhole.getChild("head");
		assertPartPosition(eveningHead, -14.2975F, -4.8167F, 1.075F);
		assertPartPosition(eveningHead.getChild("hood1"), 20.125F, 3.075F, -0.375F);
	}

	@Test
	void eveningStarBodyRotatesAroundTheAuthoredTorsoPivot() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart body = whole.getChild("body");

		assertPartPosition(body, -13.6663F, 1.1188F, -0.2716F);
		assertPartPosition(body.getChild("ClothBack"), 19.4937F, 15.5896F, 7.5716F);
		assertPartPosition(body.getChild("SideclothL"), 5.4437F, 15.5146F, 0.9716F);
		assertPartPosition(body.getChild("SideclothL4"), -6.9562F, 15.5146F, 0.9716F);
		assertPartPosition(body.getChild("cloak"), -1.5062F, -2.359F, 6.8371F);
		assertPartPosition(body.getChild("belt"), -0.5062F, 34.3229F, 1.2216F);
	}

	@Test
	void crownedRiderWeaponAttachmentFollowsTheRightHandHierarchy() throws Exception {
		ModelPart root = VesperTheCrownedRefusalModel.createBodyLayer().bakeRoot();
		VesperTheCrownedRefusalModel model = new VesperTheCrownedRefusalModel(root);
		Method attachment = assertDoesNotThrow(() -> VesperTheCrownedRefusalModel.class
				.getMethod("translateToRiderWeapon", PoseStack.class));

		PoseStack actual = new PoseStack();
		attachment.invoke(model, actual);

		PoseStack expected = new PoseStack();
		ModelPart whole = root.getChild("whole");
		ModelPart upperBody = whole.getChild("upperBody");
		ModelPart rider = upperBody.getChild("vesper");
		ModelPart rightArm = rider.getChild("rightArm");
		ModelPart shoulder = rightArm.getChild("rShoulder");
		ModelPart elbow = shoulder.getChild("rElbow");
		whole.translateAndRotate(expected);
		upperBody.translateAndRotate(expected);
		rider.translateAndRotate(expected);
		rightArm.translateAndRotate(expected);
		shoulder.translateAndRotate(expected);
		elbow.translateAndRotate(expected);
		expected.translate(-0.05D, 0.42D, -0.03D);

		assertMatrixEquals(expected.last().pose(), actual.last().pose());
	}

	@Test
	void runtimeTexturesMatchTheNewUvCanvases() throws Exception {
		assertTextureSize("vesper_crowned_refusal.png", 1024, 1024);
		assertTextureSize("vesper_evening_star.png", 256, 256);
	}

	@Test
	void eveningStarLowHealthMaskOnlyContainsTheNewRedAccents() throws Exception {
		BufferedImage lines = assertTextureSize("vesper_evening_star_lines.png", 256, 256);
		long visiblePixels = 0L;
		for (int y = 0; y < lines.getHeight(); y++) {
			for (int x = 0; x < lines.getWidth(); x++) {
				int argb = lines.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				visiblePixels++;
				int red = (argb >>> 16) & 0xFF;
				int green = (argb >>> 8) & 0xFF;
				int blue = argb & 0xFF;
				assertTrue(red > green * 1.6F && red > blue * 1.6F,
						"Emissive mask contains a non-red texel at " + x + "," + y);
			}
		}
		assertTrue(visiblePixels >= 1_000 && visiblePixels <= 4_096,
				"Emissive mask should isolate the authored red accents, not the entire skin");
	}

	private static BufferedImage assertTextureSize(String name, int width, int height) throws Exception {
		BufferedImage image = ImageIO.read(TEXTURE_ROOT.resolve(name).toFile());
		assertNotNull(image, name + " must be a readable PNG");
		assertEquals(width, image.getWidth(), name + " width");
		assertEquals(height, image.getHeight(), name + " height");
		return image;
	}

	private static void assertPartPosition(ModelPart part, float x, float y, float z) {
		assertEquals(x, part.x, 0.0001F, "x pivot");
		assertEquals(y, part.y, 0.0001F, "y pivot");
		assertEquals(z, part.z, 0.0001F, "z pivot");
	}

	private static void assertMatrixEquals(Matrix4f expected, Matrix4f actual) {
		for (int column = 0; column < 4; column++) {
			for (int row = 0; row < 4; row++) {
				assertEquals(expected.get(column, row), actual.get(column, row), 0.0001F,
						"pose matrix [" + column + "," + row + "]");
			}
		}
	}
}
