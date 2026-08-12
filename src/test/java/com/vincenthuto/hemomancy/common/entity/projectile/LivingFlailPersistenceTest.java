package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LivingFlailPersistenceTest {
	@Test
	void projectileStateRoundTripsEveryRecoveryField() {
		UUID deployment = UUID.fromString("10101010-2020-3030-4040-505050505050");
		UUID owner = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
		LivingFlailProjectileState original = new LivingFlailProjectileState(deployment, owner, 0.625F,
				InteractionHand.OFF_HAND, EnumBloodTendency.CONGEATIO, EnumBloodTendency.FERRIC,
				true, 12.5D, 63.25D, -8.75D);
		CompoundTag encoded = original.write(new CompoundTag());
		assertEquals(original, LivingFlailProjectileState.read(encoded));
	}
}
