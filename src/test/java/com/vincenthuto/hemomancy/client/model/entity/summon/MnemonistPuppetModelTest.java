package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonistPuppetModelTest {
	@Test
	void gaitStrikeAndEncoreDriveTheRigAndResetSharedModelState() {
		var puppet = new MnemonistPuppetModel();
		var pelvis = puppet.root().getChild("pelvis");
		var torso = pelvis.getChild("rib_cage");
		var jaw = torso.getChild("head").getChild("jaw");
		var left = torso.getChild("left_shoulder");
		var right = torso.getChild("right_shoulder");

		puppet.pose(0, 0, 20, 0, 0, false, 0, 0, 0, 0);
		float restingJaw = jaw.xRot;
		float restingLeft = left.xRot;
		var restingSpool = new PoseStack();
		puppet.translateToSpool(restingSpool);

		puppet.pose(1.2F, 1, 20, 25, 0, false, 0, 0, 0, 0);
		assertTrue(pelvis.getChild("right_hip").getChild("right_knee").xRot > 0.5F);

		puppet.pose(0, 0, 20, 0, 0, true, 0.5F, 0, 0, 0);
		assertTrue(left.xRot < right.xRot - 1);

		puppet.pose(0, 0, 20, 0, 0, false, 0, 0.5F, 0, 0);
		assertTrue(jaw.xRot > restingJaw + 0.6F);
		var castingSpool = new PoseStack();
		puppet.translateToSpool(castingSpool);
		assertFalse(castingSpool.last().pose().equals(restingSpool.last().pose(), 1e-5F));

		puppet.pose(0, 0, 20, 0, 0, false, 0, 0, 0, 0);
		assertEquals(restingJaw, jaw.xRot);
		assertEquals(restingLeft, left.xRot);
		var resetSpool = new PoseStack();
		puppet.translateToSpool(resetSpool);
		assertTrue(resetSpool.last().pose().equals(restingSpool.last().pose(), 1e-5F));
	}
}
