package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class VesperModelAssetContractTest {
	private static final Path TEXTURE_ROOT = Path.of(
			"src/main/resources/assets/hemomancy/textures/entity/boss/endgame");

	@Test
	void crownedRefusalSplitsTheBlockbenchRiderFromTheGeneratedSixLeggedMount() {
		ModelPart whole = VesperTheCrownedRefusalModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart rider = whole.getChild("vesper");
		assertThrows(java.util.NoSuchElementException.class, () -> whole.getChild("lowerBody"),
				"the rider model must no longer carry the old mount");
		assertNotNull(rider.getChild("head2").getChild("crown"));
		assertNotNull(rider.getChild("leftArm").getChild("lShoulder").getChild("lElbow"));

		ModelPart body = VesperMountModel.createBodyLayer().bakeRoot().getChild("root").getChild("body");
		ModelPart thorax = body.getChild("thorax");
		assertNotNull(thorax.getChild("seat"));
		assertNotNull(thorax.getChild("neck").getChild("head").getChild("mandible_r"));
		assertNotNull(thorax.getChild("neck").getChild("burrow_neck_grip"));
		assertNotNull(thorax.getChild("neck").getChild("wound_neck"));
		assertNotNull(thorax.getChild("claw_arm_l").getChild("claw_fore_l").getChild("pincer_l")
				.getChild("pincer_finger_l"));
		for (int pair = 0; pair < 3; pair++) {
			for (String side : new String[] { "r", "l" }) {
				assertNotNull(thorax.getChild("leg" + pair + "_coxa_" + side).getChild("leg" + pair + "_femur_" + side)
						.getChild("leg" + pair + "_tibia_" + side).getChild("leg" + pair + "_tarsus_" + side));
			}
		}
		for (String wing : new String[] { "wing_fore_r", "wing_fore_l", "wing_hind_r", "wing_hind_l" }) {
			assertNotNull(thorax.getChild(wing).getChild(wing.replace("_r", "_membrane_r").replace("_l", "_membrane_l")));
		}
		ModelPart abdomen = body.getChild("abdomen");
		assertNotNull(abdomen.getChild("wound_carapace"));
		assertNotNull(abdomen.getChild("wound_tail"));
		assertNotNull(abdomen.getChild("scute_front"));
		assertNotNull(abdomen.getChild("scute_mid"));
		assertNotNull(abdomen.getChild("scute_rear"));
		assertNotNull(abdomen.getChild("tail0").getChild("tail1").getChild("tail2").getChild("tail3")
				.getChild("tail4").getChild("tail5").getChild("stinger").getChild("stinger_tip"));
	}

	@Test
	void eveningStarUsesTheAuthoredClothAndWeaponAttachmentHierarchy() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");

		// The head and arms ride the torso, so a lean or a twist carries the shoulders and neck with it.
		ModelPart body = whole.getChild("body");
		assertNotNull(body.getChild("head").getChild("crown"));
		assertNotNull(body.getChild("head").getChild("hood"));
		assertNotNull(body.getChild("head").getChild("hair"));
		assertNotNull(body.getChild("ClothBack").getChild("ClothBack1").getChild("ClothBack2"));
		assertNotNull(body.getChild("leftArm").getChild("rShoulder2").getChild("rElbow2"));
		// A real elbow: the sleeve splits into an upper arm and a forearm that carries the hand.
		assertNotNull(body.getChild("rightArm").getChild("rShoulder").getChild("rElbow").getChild("rForearm"));
		assertNotNull(body.getChild("leftArm").getChild("rShoulder2").getChild("rElbow2").getChild("lForearm"));
		// Boots hinge at the ankle; the shin plates belong to the shin.
		assertNotNull(whole.getChild("leftLeg").getChild("leftLeg2").getChild("leftBoot"));
		assertNotNull(whole.getChild("leftLeg").getChild("leftLeg2").getChild("lShin"));
		assertNotNull(whole.getChild("rightLeg").getChild("rightLeg2").getChild("rightBoot"));
		assertNotNull(whole.getChild("rightLeg").getChild("rightLeg2").getChild("rshin2"));
	}

	@Test
	void vesperHeadsRotateAroundTheAuthoredNeckPivots() {
		ModelPart crownedWhole = VesperTheCrownedRefusalModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart crownedHead = crownedWhole.getChild("vesper").getChild("head2");
		assertPartPosition(crownedHead, -2.0143F, -11.4387F, 0.2109F);
		assertPartPosition(crownedHead.getChild("hood1"), 22.0F, 3.075F, -0.375F);

		ModelPart eveningWhole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart eveningHead = eveningWhole.getChild("body").getChild("head");
		assertPartPosition(eveningHead, -0.3813F, -19.942F, 1.3466F);
		assertPartPosition(eveningHead.getChild("hood"), 20.125F, 3.075F, -0.375F);
	}

	@Test
	void eveningStarBodyRotatesAroundTheWaist() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart body = whole.getChild("body");

		// The torso hinges just above the hips, so a lean folds at the waist instead of swinging the hips off the
		// legs; every child moved with the pivot and still sits where it was authored.
		assertPartPosition(body, 0.3594F, 10.0F, -0.3722F);
		assertPartPosition(body.getChild("ClothBack"), 19.4937F, 1.583F, 7.5716F);
		assertPartPosition(body.getChild("SideclothL"), 5.4438F, 1.508F, 0.9716F);
		assertPartPosition(body.getChild("SideclothL4"), -6.9562F, 1.508F, 0.9716F);
		assertPartPosition(body.getChild("cloak"), -1.5063F, -16.3656F, 6.8371F);
		assertPartPosition(body.getChild("belt"), -0.5062F, 20.3163F, 1.2216F);
		assertPartPosition(body.getChild("rightArm"), -7.2563F, -13.867F, 0.9716F);
		assertPartPosition(body.getChild("leftArm"), 6.2437F, -13.867F, 0.9716F);
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
		ModelPart rider = whole.getChild("vesper");
		ModelPart rightArm = rider.getChild("rightArm");
		ModelPart shoulder = rightArm.getChild("rShoulder");
		ModelPart elbow = shoulder.getChild("rElbow");
		whole.translateAndRotate(expected);
		rider.translateAndRotate(expected);
		rightArm.translateAndRotate(expected);
		shoulder.translateAndRotate(expected);
		elbow.translateAndRotate(expected);
		expected.translate(0.0D, 0.78D, -0.4D);

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
