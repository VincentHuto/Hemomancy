package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LivingFlailDeploymentPersistenceTest {
	@Test
	void itemDeploymentIdentifierRoundTripsThroughCustomDataTag() {
		UUID id = UUID.fromString("12345678-1234-5678-90ab-1234567890ab");
		CompoundTag tag = new CompoundTag();
		LivingFlailDeployment.writeDeployment(tag, id);
		assertEquals(id, LivingFlailDeployment.readDeployment(tag).orElseThrow());
	}
}
