package com.vincenthuto.hemomancy.common.rite;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a cardinal rite that is currently being cast.
 * Tracks the casting player, position, recipe, and remaining duration.
 */
public class ActiveCardinalRite {

	private final UUID playerUUID;
	private final BlockPos centerPos;
	private final ResourceLocation recipeId;
	private final int totalTicks;
	private final int riteSize;
	private int remainingTicks;

	public ActiveCardinalRite(UUID playerUUID, BlockPos centerPos, ResourceLocation recipeId, int totalTicks,
			int riteSize) {
		this.playerUUID = playerUUID;
		this.centerPos = centerPos;
		this.recipeId = recipeId;
		this.totalTicks = totalTicks;
		this.riteSize = riteSize;
		this.remainingTicks = totalTicks;
	}

	public void tick() {
		if (remainingTicks > 0) {
			remainingTicks--;
		}
	}

	public boolean isComplete() {
		return remainingTicks <= 0;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public BlockPos getCenterPos() {
		return centerPos;
	}

	public ResourceLocation getRecipeId() {
		return recipeId;
	}

	public int getTotalTicks() {
		return totalTicks;
	}

	public int getRemainingTicks() {
		return remainingTicks;
	}

	public int getRiteSize() {
		return riteSize;
	}

	public double getProgress() {
		return 1.0 - (double) remainingTicks / totalTicks;
	}

	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("PlayerUUID", playerUUID);
		tag.putLong("CenterPos", centerPos.asLong());
		tag.putString("RecipeId", recipeId.toString());
		tag.putInt("TotalTicks", totalTicks);
		tag.putInt("RemainingTicks", remainingTicks);
		tag.putInt("RiteSize", riteSize);
		return tag;
	}

	public static ActiveCardinalRite deserialize(CompoundTag tag) {
		UUID playerUUID = tag.getUUID("PlayerUUID");
		BlockPos centerPos = BlockPos.of(tag.getLong("CenterPos"));
		ResourceLocation recipeId = ResourceLocation.parse(tag.getString("RecipeId"));
		int totalTicks = tag.getInt("TotalTicks");
		int riteSize = tag.getInt("RiteSize");
		ActiveCardinalRite rite = new ActiveCardinalRite(playerUUID, centerPos, recipeId, totalTicks, riteSize);
		rite.remainingTicks = tag.getInt("RemainingTicks");
		return rite;
	}
}
