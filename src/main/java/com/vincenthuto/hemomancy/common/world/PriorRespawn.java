package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The respawn point a player had before planting their heart-node, usually their bed (PT-80, NQ-12-4). The node's entry
 * keeps it: the way home from the Gardens leads there, and it comes back as the respawn when the node goes.
 */
public record PriorRespawn(ResourceKey<Level> dimension, BlockPos pos, float angle, boolean forced) {
	public PriorRespawn {
		pos = pos.immutable();
	}

	/** {@code player}'s respawn point as it stands, or null when they have none. */
	@Nullable
	public static PriorRespawn of(ServerPlayer player) {
		BlockPos pos = player.getRespawnPosition();
		return pos == null ? null
				: new PriorRespawn(player.getRespawnDimension(), pos, player.getRespawnAngle(), player.isRespawnForced());
	}

	/** Gives {@code player} back {@code prior} as their respawn, or none (world spawn) when it is null. */
	public static void restore(ServerPlayer player, @Nullable PriorRespawn prior) {
		if (prior == null) {
			player.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
		} else {
			player.setRespawnPosition(prior.dimension, prior.pos, prior.angle, prior.forced, false);
		}
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Dimension", dimension.location().toString());
		tag.put("Pos", NbtUtils.writeBlockPos(pos));
		tag.putFloat("Angle", angle);
		tag.putBoolean("Forced", forced);
		return tag;
	}

	/** Reads a saved point; null when {@code tag} holds none. */
	@Nullable
	public static PriorRespawn load(CompoundTag tag) {
		if (!tag.contains("Dimension") || !tag.contains("Pos")) return null;
		ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
		if (dimension == null) return null;
		return NbtUtils.readBlockPos(tag, "Pos").map(pos -> new PriorRespawn(ResourceKey.create(Registries.DIMENSION, dimension),
				pos, tag.getFloat("Angle"), tag.getBoolean("Forced"))).orElse(null);
	}
}
