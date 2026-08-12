package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;
import java.util.UUID;

public record LivingFlailProjectileState(UUID deploymentId, UUID ownerId, float charge,
		InteractionHand originalHand, EnumBloodTendency primaryTendency,
		@Nullable EnumBloodTendency secondaryTendency, boolean impacted,
		double lastSafeX, double lastSafeY, double lastSafeZ) {
	public CompoundTag write(CompoundTag tag) {
		tag.putUUID("DeploymentId", deploymentId);
		tag.putUUID("FlailOwner", ownerId);
		tag.putFloat("FlailCharge", charge);
		tag.putString("OriginalHand", originalHand.name());
		tag.putString("Tendency", primaryTendency.name());
		tag.putString("SecondaryTendency", secondaryTendency == null ? "" : secondaryTendency.name());
		tag.putBoolean("Impacted", impacted);
		tag.putDouble("LastSafeX", lastSafeX);
		tag.putDouble("LastSafeY", lastSafeY);
		tag.putDouble("LastSafeZ", lastSafeZ);
		return tag;
	}

	public static LivingFlailProjectileState read(CompoundTag tag) {
		UUID deployment = tag.hasUUID("DeploymentId") ? tag.getUUID("DeploymentId") : UUID.randomUUID();
		UUID owner = tag.hasUUID("FlailOwner") ? tag.getUUID("FlailOwner") : new UUID(0L, 0L);
		InteractionHand hand;
		try {
			hand = InteractionHand.valueOf(tag.getString("OriginalHand"));
		} catch (IllegalArgumentException ignored) {
			hand = InteractionHand.MAIN_HAND;
		}
		EnumBloodTendency primary = parse(tag.getString("Tendency"), EnumBloodTendency.CONGEATIO);
		EnumBloodTendency secondary = parse(tag.getString("SecondaryTendency"), null);
		return new LivingFlailProjectileState(deployment, owner, tag.getFloat("FlailCharge"), hand,
				primary, secondary, tag.getBoolean("Impacted"), tag.getDouble("LastSafeX"),
				tag.getDouble("LastSafeY"), tag.getDouble("LastSafeZ"));
	}

	@Nullable
	private static EnumBloodTendency parse(String name, @Nullable EnumBloodTendency fallback) {
		try {
			return EnumBloodTendency.valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}
}
