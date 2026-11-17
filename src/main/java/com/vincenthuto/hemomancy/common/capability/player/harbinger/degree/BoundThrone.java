package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Where a player bound their Covenant Throne: its block and the respawn point it set. */
public record BoundThrone(ResourceKey<Level> dimension, BlockPos throne, BlockPos respawn) {

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putString("dimension", dimension.location().toString());
		tag.putLong("throne", throne.asLong());
		tag.putLong("respawn", respawn.asLong());
		return tag;
	}

	@Nullable
	public static BoundThrone load(CompoundTag tag) {
		ResourceLocation id = ResourceLocation.tryParse(tag.getString("dimension"));
		if (id == null) return null;
		return new BoundThrone(ResourceKey.create(Registries.DIMENSION, id), BlockPos.of(tag.getLong("throne")),
				BlockPos.of(tag.getLong("respawn")));
	}
}
