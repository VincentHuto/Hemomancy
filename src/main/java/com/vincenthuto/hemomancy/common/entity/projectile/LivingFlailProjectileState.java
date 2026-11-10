package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;
import java.util.UUID;

public record LivingFlailProjectileState(UUID deploymentId, UUID ownerId, float charge,
		InteractionHand originalHand, EnumBloodTemper primaryTemper,
		@Nullable EnumBloodTemper secondaryTemper, boolean impacted,
		double lastSafeX, double lastSafeY, double lastSafeZ) {
	public CompoundTag write(CompoundTag tag) {
		tag.putUUID("DeploymentId", deploymentId);
		tag.putUUID("FlailOwner", ownerId);
		tag.putFloat("FlailCharge", charge);
		tag.putString("OriginalHand", originalHand.name());
		tag.putString("Temper", primaryTemper.name());
		tag.putString("SecondaryTemper", secondaryTemper == null ? "" : secondaryTemper.name());
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
		EnumBloodTemper primary = parse(tag.getString("Temper"), EnumBloodTemper.CONGEATIO);
		EnumBloodTemper secondary = parse(tag.getString("SecondaryTemper"), null);
		return new LivingFlailProjectileState(deployment, owner, tag.getFloat("FlailCharge"), hand,
				primary, secondary, tag.getBoolean("Impacted"), tag.getDouble("LastSafeX"),
				tag.getDouble("LastSafeY"), tag.getDouble("LastSafeZ"));
	}

	@Nullable
	private static EnumBloodTemper parse(String name, @Nullable EnumBloodTemper fallback) {
		try {
			return EnumBloodTemper.valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}
}
