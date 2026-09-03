package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class LuminalCicadaRulesTest {
	@Test
	void rulesHold() {
		main(new String[0]);
	}

	@Test
	void clingAnchorClearsLogCollisionShape() {
		double halfWidth = 0.45D / 2.0D;
		Vec3 anchor = LuminalCicadaRules.clingAnchor(new BlockPos(10, 20, 30), Direction.NORTH, halfWidth);
		assertTrue(anchor.z + halfWidth < 30.0D, "cicada hitbox must remain outside the north log face");
	}

	@Test
	void idleFlightRetargetsOnlyWhenNeeded() {
		assert LuminalCicadaRules.shouldPickIdleDestination(false, false, 99.0D, 20);
		assert LuminalCicadaRules.shouldPickIdleDestination(false, true, 0.2D, 20);
		assert LuminalCicadaRules.shouldPickIdleDestination(false, true, 4.0D, 0);
		assert !LuminalCicadaRules.shouldPickIdleDestination(false, true, 4.0D, 20);
		assert !LuminalCicadaRules.shouldPickIdleDestination(true, false, 0.0D, 0);
	}

	@Test
	void airborneLegsHangBelowTheBody() {
		assert LuminalCicadaRules.legRoll(false, false) < 0.0F;
		assert LuminalCicadaRules.legRoll(false, true) > 0.0F;
		assert LuminalCicadaRules.legRoll(true, false) > 0.0F;
		assert LuminalCicadaRules.legRoll(true, true) < 0.0F;
	}

	@Test
	void tailGlowPulsesWithinSubtleBounds() {
		float first = LuminalCicadaRules.tailGlowScale(0.0F);
		float later = LuminalCicadaRules.tailGlowScale(20.0F);
		assert first >= 0.9F && first <= 1.1F;
		assert later >= 0.9F && later <= 1.1F;
		assert Math.abs(first - later) > 0.01F;
	}

	@Test
	void airborneAbdomenHangsBelowThorax() {
		float airborne = LuminalCicadaRules.abdomenPitch(false, 0.0F);
		assert airborne < -0.1F && airborne > -0.3F;
		assert LuminalCicadaRules.abdomenPitch(true, 0.0F) == 0.0F;
	}

	public static void main(String[] args) {
		assert LuminalCicadaRules.canCling(true, true);
		assert !LuminalCicadaRules.canCling(false, true);
		assert !LuminalCicadaRules.canCling(true, false);

		assert LuminalCicadaRules.shouldFlash(15.9D, 0);
		assert !LuminalCicadaRules.shouldFlash(16.1D, 0);
		assert !LuminalCicadaRules.shouldFlash(1.0D, 1);

		assert LuminalCicadaRules.canNaturalSpawn(true, true);
		assert !LuminalCicadaRules.canNaturalSpawn(false, true);
		assert !LuminalCicadaRules.canNaturalSpawn(true, false);

		assert LuminalCicadaRules.clingBodyYaw(Direction.NORTH) == 180.0F;
		assert LuminalCicadaRules.clingBodyYaw(Direction.SOUTH) == 0.0F;
		assert LuminalCicadaRules.clingBodyYaw(Direction.WEST) == 90.0F;
		assert LuminalCicadaRules.clingBodyYaw(Direction.EAST) == 270.0F;
		assert LuminalCicadaRules.clingTiltDegrees(Direction.NORTH) == 90.0F;
		assert LuminalCicadaRules.clingTiltDegrees(Direction.UP) == 0.0F;
	}
}
