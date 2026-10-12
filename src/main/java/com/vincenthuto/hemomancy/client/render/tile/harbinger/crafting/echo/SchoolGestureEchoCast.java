package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.world.phys.Vec3;

/** Until a memory has its real cast: a burst of its school's colour from the echo's hands along its gaze. */
final class SchoolGestureEchoCast implements EchoCast {
	@Override
	public void release(EchoContext context) {
		var color = context.manipulation().getTend().getColor();
		Vec3 hands = context.hands();
		float scale = context.figure() / 0.42f;
		for (int i = 0; i < 12; i++) {
			Vec3 v = context.forward().scale(0.08 * scale).add(
					(context.level().random.nextDouble() - 0.5) * 0.04, (context.level().random.nextDouble() - 0.3) * 0.04,
					(context.level().random.nextDouble() - 0.5) * 0.04);
			context.level().addParticle(HemoParticleData.glow(color), hands.x, hands.y, hands.z, v.x, v.y, v.z);
		}
		for (int i = 0; i < 6; i++)
			context.level().addParticle(HemoParticleData.ember(color, 0.06F * scale, 0.9F, 18),
					hands.x, hands.y, hands.z, context.forward().x * 0.05, 0.02, context.forward().z * 0.05);
	}
}
