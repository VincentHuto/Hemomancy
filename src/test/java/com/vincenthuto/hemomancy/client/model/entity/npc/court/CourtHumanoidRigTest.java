package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingPartSpec;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModels;
import com.vincenthuto.hemomancy.client.model.morphling.MorphlingModelAttachment.AttachmentPoint;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Brined Court Track 9: every Court humanoid is the court_humanoid.py rig (elbows four pixels below the shoulder, knees
 * halfway, shoulders and hips where the player's are; court_humanoid.py also asserts a filler in each lower segment),
 * and the strain anchor adapter
 * puts each arm and leg growth on its limb segment: at rest a LOWER growth lands where it does on the player arm, and
 * a bent elbow carries it.
 */
final class CourtHumanoidRigTest {
	private static ModelPart bake(CourtMemberModels.Row row) {
		return row.definition().get().bakeRoot();
	}

	private static void assertPivot(ModelPart part, float x, float y, float z, String what) {
		assertEquals(x, part.x, 1e-4, what + " x");
		assertEquals(y, part.y, 1e-4, what + " y");
		assertEquals(z, part.z, 1e-4, what + " z");
	}

	@Test
	void everyHumanoidHasTheRigsJoints() {
		for (CourtMemberModels.Row row : CourtMemberModels.ALL) {
			if (row.role().body() == CourtRole.Body.NEW_BODY) continue;
			ModelPart root = bake(row).getChild("root");
			ModelPart hips = root.getChild("hips");
			ModelPart body = hips.getChild("body");
			String role = row.role().id();
			assertPivot(hips, 0, 12, 0, role + " hips");
			assertPivot(body, 0, -2, 0, role + " waist");
			for (int sign : new int[] {-1, 1}) {
				String side = sign < 0 ? "right" : "left";
				ModelPart arm = body.getChild(side + "_arm");
				ModelPart forearm = arm.getChild(side + "_forearm");
				ModelPart leg = hips.getChild(side + "_leg");
				ModelPart shin = leg.getChild(side + "_shin");
				assertPivot(arm, sign * 5, -8, 0, role + " " + side + " shoulder");
				assertPivot(forearm, sign, 4, 0, role + " " + side + " elbow, four below the shoulder");
				assertPivot(leg, sign * 1.9F, 0, 0, role + " " + side + " hip");
				assertPivot(shin, 0, 6, 0, role + " " + side + " knee, halfway down the leg");
			}
		}
	}

	@Test
	void armAndLegGrowthsAllHaveASegment() {
		for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.ALL) {
			if (anchor.point() != AttachmentPoint.ARMS && anchor.point() != AttachmentPoint.LEGS) continue;
			for (MorphlingPartSpec part : anchor.parts()) {
				assertNotEquals(MorphlingPartSpec.Limb.NONE, part.limb(), anchor.strainId() + " " + part.name()
						+ " would not be drawn on the jointed rig");
			}
		}
	}

	@Test
	void lowerGrowthsLandOnTheForearmAndBendWithTheElbow() {
		CourtGardenerModel model = new CourtGardenerModel(bake(CourtMemberModels.ALL.get(0)));
		Vector3f wrist = new Vector3f(1.0F / 16, 9.0F / 16, 0);
		Vector3f onArm = transform(stack -> model.toArm(stack, false), wrist);
		Vector3f onForearm = transform(stack -> model.toForearm(stack, false), wrist);
		assertEquals(onArm.x, onForearm.x, 1e-5);
		assertEquals(onArm.y, onForearm.y, 1e-5);
		assertEquals(onArm.z, onForearm.z, 1e-5);
		model.leftForearm.xRot = -1.2F;
		Vector3f bent = transform(stack -> model.toForearm(stack, false), wrist);
		Vector3f elbow = transform(stack -> model.toForearm(stack, false), new Vector3f(1.0F / 16, 4.0F / 16, 0));
		assertTrue(bent.z < onArm.z - 0.1F, "the wrist growth swings forward with the bent forearm");
		assertEquals(onArm.distance(transform(stack -> model.toArm(stack, false), new Vector3f(1.0F / 16, 4.0F / 16, 0))),
				bent.distance(elbow), 1e-4, "the growth keeps its distance from the elbow");
	}

	private static Vector3f transform(java.util.function.Consumer<PoseStack> segment, Vector3f point) {
		PoseStack stack = new PoseStack();
		segment.accept(stack);
		return stack.last().pose().transformPosition(new Vector3f(point));
	}
}
