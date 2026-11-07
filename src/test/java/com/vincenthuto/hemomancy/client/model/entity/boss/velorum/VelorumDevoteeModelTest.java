package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VelorumDevoteeModelTest {
	@Test
	void theJointedRigElbowsFourBelowTheShoulderKneesHalfway() {
		var root = VelorumDevoteeModel.createBodyLayer().bakeRoot().getChild("root");
		var hips = root.getChild("hips");
		var body = hips.getChild("body");
		for (String side : new String[]{"left", "right"}) {
			var arm = body.getChild(side + "_arm");
			assertEquals(4.0F, arm.getChild(side + "_forearm").y, 1.0E-4F);
			var leg = hips.getChild(side + "_leg");
			assertEquals(6.0F, leg.getChild(side + "_shin").y, 1.0E-4F);
			assertTrue(arm.getChild(side + "_forearm").cubes.size() >= 2, "an inset filler closes the elbow");
		}
	}

	@Test
	void everyStateAndPosePosesFinitely() {
		var model = new VelorumDevoteeModel(VelorumDevoteeModel.createBodyLayer().bakeRoot());
		var root = model.root();
		model.pose(1, 0, 100, 0, 0, -1, false, 10, 0, 0);
		var standing = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList();
		for (int state = 0; state < 5; state++) {
			for (int pose = 0; pose < 5; pose++) {
				for (float age : new float[]{0, 10, 20, 40}) {
					model.pose(state, pose, age, age, 0.5F, age % 30, state == 1, age, 10, 5);
					for (ModelPart part : root.getAllParts().toList()) {
						assertTrue(Float.isFinite(part.xRot) && Float.isFinite(part.y));
					}
				}
			}
		}
		model.pose(4, 0, 0, 0, 0, -1, false, 10, 0, 0);
		assertNotEquals(standing, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList(),
				"kneeling home is the block's pose, not standing");
	}

	@Test
	void sixteenPixelsPerBlock() throws Exception {
		var base = ImageIO.read(ResourceReader.path("assets/hemomancy/textures/entity/boss/velorum/velorum_devotee.png").toFile());
		var project = ResourceReader.asset("models/entity/bbmodel/bosses/VelorumDevoteeModel.bbmodel");
		assertEquals(project.getAsJsonObject("resolution").get("width").getAsInt(), base.getWidth());
	}

	@Test
	void aThawStartsFromTheBlocksOwnPoseAndTheStilledKneelHome() {
		var model = new VelorumDevoteeModel(VelorumDevoteeModel.createBodyLayer().bakeRoot());
		var root = model.root();
		for (int pose = 0; pose < 5; pose++) {
			model.pose(4, pose, 0, 0, 0, -1, false, 0, 0, 0);
			var home = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList();
			model.pose(0, pose, 0, 0, 0, -1, false, 0, 0, 0);
			assertEquals(home, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList(),
					"the swap from the block figure does not jump, pose " + pose);
		}
		model.pose(4, 0, 0, 0, 0, -1, false, 0, 0, 0);
		var kneeling = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList();
		model.pose(2, 3, 30, 0, 0, -1, false, 0, 0, 0);
		assertEquals(kneeling, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList(),
				"stilled, it ends kneeling under new rime");
	}

	@Test
	void anEclipseShapeStandsUpOutOfTheBlocksOwnPose() {
		var model = new VelorumDevoteeModel(VelorumDevoteeModel.createBodyLayer().bakeRoot());
		var root = model.root();
		for (int pose = 0; pose < 5; pose++) {
			model.pose(4, pose, 0, 0, 0, -1, false, 0, 0, 0);
			var home = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList();
			model.poseShapeRise(VelorumDevoteeAnimState.shape(1000L + pose, 0.0F), pose, 0.0F, 0.0F);
			assertEquals(home, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.y)).toList(),
					"the shape starts in the figure's own pose, pose " + pose);
		}
		float[] risen = VelorumDevoteeModel.shapeChannels(3, 1.0F, 0.0F, 0.0F, 0.0F);
		assertEquals(1.0F, risen[VelorumDevoteeModel.ALIVE], 1.0E-4F, "fully risen, it stands free of the figure");
		float[] half = VelorumDevoteeModel.shapeChannels(3, 0.5F, 0.0F, 0.0F, 0.0F);
		assertTrue(half[VelorumDevoteeModel.FROZEN_LEGS] < 0.05F, "the legs come first");
		assertTrue(half[VelorumDevoteeModel.ALIVE] < 0.2F, "the head still held in the figure: it comes last");
		assertEquals(1.0F, VelorumDevoteeModel.shapeStretch(0.0F), 1.0E-4F);
		assertEquals(1.25F, VelorumDevoteeModel.shapeStretch(1.0F), 1.0E-4F, "stretched to 1.25 times its height");
		float[] reach = VelorumDevoteeModel.shapeChannels(0, 1.0F, 1.0F, 0.0F, 0.0F);
		assertTrue(reach[VelorumDevoteeModel.R_ARM_X] < risen[VelorumDevoteeModel.R_ARM_X] - 1.2F, "one arm raised to point");
		assertTrue(reach[VelorumDevoteeModel.BODY_X] > risen[VelorumDevoteeModel.BODY_X], "leaning forward");
		float[] grip = VelorumDevoteeModel.shapeChannels(0, 1.0F, 0.0F, 1.0F, 0.0F);
		assertTrue(grip[VelorumDevoteeModel.R_ARM_X] < risen[VelorumDevoteeModel.R_ARM_X] - 1.0F
				&& grip[VelorumDevoteeModel.L_ARM_X] < risen[VelorumDevoteeModel.L_ARM_X] - 1.0F, "both arms forward");
		model.poseShapeGrip(VelorumDevoteeAnimState.shape(2000L, 0.0F), 0, 1.0F, 1.0F, 5.0F);
		for (ModelPart part : root.getAllParts().toList()) assertTrue(Float.isFinite(part.xRot) && Float.isFinite(part.y));
	}

	@Test
	void theBlowComesDownFarFasterThanItRose() {
		float[] arms = new float[31];
		for (int t = 0; t <= 30; t++) arms[t] = VelorumDevoteeModel.channels(1, 0, 100, 0, 0, t, false, false, 0)[VelorumDevoteeModel.R_ARM_X];
		float up = Math.abs(arms[11] - arms[0]) / 11.0F;
		float down = Math.abs(arms[14] - arms[12]) / 2.0F;
		assertTrue(arms[11] < arms[0] - 1.5F, "both arms raised overhead");
		assertTrue(down > 3.0F * up, "the blow snaps down: " + down + " against " + up);
		assertTrue(arms[16] > arms[14], "and carries on past the line");
	}

	@Test
	void standingAwakeItsSolesAreOnTheFloor() {
		var model = new VelorumDevoteeModel(VelorumDevoteeModel.createBodyLayer().bakeRoot());
		var root = model.root();
		for (boolean hostile : new boolean[]{false, true}) {
			float[] a = VelorumDevoteeModel.channels(1, 0, 100, 0, 0, -1, false, hostile, 0);
			model.pose(a, 0, 0, 0);
			var hips = root.getChild("hips");
			float lowest = Float.NEGATIVE_INFINITY;
			for (String side : new String[]{"left", "right"}) {
				var leg = hips.getChild(side + "_leg");
				var pose = new com.mojang.blaze3d.vertex.PoseStack();
				for (var part : new net.minecraft.client.model.geom.ModelPart[]{root, hips, leg, leg.getChild(side + "_shin")})
					part.translateAndRotate(pose);
				lowest = Math.max(lowest, pose.last().pose().transformPosition(new org.joml.Vector3f(0, 6 / 16.0F, 0)).y * 16);
			}
			assertEquals(24.0F, lowest, 0.05F, hostile ? "hunched on bent knees" : "upright");
		}
	}
}
