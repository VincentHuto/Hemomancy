package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;

public record VesperVulnerableRotation(boolean active, float yaw) {
	public static final String NBT_KEY = "VulnerableYaw";

	public static VesperVulnerableRotation capture(float yaw) {
		return new VesperVulnerableRotation(true, yaw);
	}

	public static VesperVulnerableRotation inactive() {
		return new VesperVulnerableRotation(false, 0.0F);
	}

	public static VesperVulnerableRotation load(CompoundTag tag, boolean anchorActive, float fallbackYaw) {
		if (!anchorActive) return inactive();
		return capture(tag.contains(NBT_KEY) ? tag.getFloat(NBT_KEY) : fallbackYaw);
	}

	public VesperVulnerableRotation clear() {
		return inactive();
	}

	public void save(CompoundTag tag) {
		if (active) tag.putFloat(NBT_KEY, yaw);
	}
}
