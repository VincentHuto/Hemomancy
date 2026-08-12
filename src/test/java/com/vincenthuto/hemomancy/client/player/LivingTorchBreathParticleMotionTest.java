package com.vincenthuto.hemomancy.client.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

final class LivingTorchBreathParticleMotionTest {
	@Test
	void flameTonguesTravelForwardFarEnoughToReadAsAProjectedStream() {
		Vec3 velocity = LivingTorchBreathParticleMotion.velocity(
				new Vec3(0.0D, 0.0D, 1.0D), new Vec3(-1.0D, 0.0D, 0.0D),
				0, 0, 0.0D);

		assertEquals(0.0D, velocity.x, 0.0001D);
		assertEquals(0.0D, velocity.y, 0.0001D);
		assertTrue(velocity.z >= 0.18D, "the slowest tongue must visibly leave the torch tip");
		assertTrue(velocity.z * LivingTorchBreathParticleMotion.EFFECTIVE_FLAME_LIFETIME_TICKS >= 2.0D,
				"the slowest tongue must cover at least two blocks before expiring");
	}

	@Test
	void writheChangesOnlyTheSideAxisAndNeverCancelsForwardTravel() {
		Vec3 look = new Vec3(0.0D, 0.0D, -1.0D);
		Vec3 side = new Vec3(1.0D, 0.0D, 0.0D);
		Vec3 velocity = LivingTorchBreathParticleMotion.velocity(look, side, 7, 3, 0.012D);

		assertTrue(velocity.dot(look) > 0.20D);
		assertEquals(0.012D, velocity.y, 0.0001D);
	}

	@Test
	void bloodCellFactoryInputCompensatesForItsBuiltInDoubleSpeed() {
		Vec3 factoryInput = LivingTorchBreathParticleMotion.bloodCellFactoryInput(
				new Vec3(0.04D, 0.02D, 0.22D));

		assertEquals(0.02D, factoryInput.x, 0.0001D);
		assertEquals(0.01D, factoryInput.y, 0.0001D);
		assertEquals(0.11D, factoryInput.z, 0.0001D);
	}

}
