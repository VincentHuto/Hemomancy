package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HemoCapabilityAccess {

	private HemoCapabilityAccess() {
	}

	// ── Blood Volume ──────────────────────────────────────────────────────────

	/** Access blood volume from a player. */
	public static Optional<IBloodVolume> getBloodVolume(Player player) {
		return HemoCapabilityAccess.getBloodVolume(player);
	}

	/** Access blood volume from any entity (players, summons, etc.). */
	public static Optional<IBloodVolume> getBloodVolume(Entity entity) {
		return HemoCapabilityAccess.getBloodVolume(entity);
	}

	/** Access blood volume from a block entity (IBloodTile implementations). */
	public static Optional<IBloodVolume> getBloodVolume(BlockEntity be) {
		return HemoCapabilityAccess.getBloodVolume(be);
	}

	/** Access blood volume from an item stack (BloodGourdItem and similar). */
	public static Optional<IBloodVolume> getBloodVolume(ItemStack stack) {
		return HemoCapabilityAccess.getBloodVolume(stack);
	}

	public static IBloodVolume requireBloodVolume(Player player) {
		return getBloodVolume(player).orElseThrow(IllegalStateException::new);
	}

	public static IBloodVolume requireBloodVolume(Entity entity) {
		return getBloodVolume(entity).orElseThrow(IllegalStateException::new);
	}

	// ── Blood Tendency ────────────────────────────────────────────────────────

	public static Optional<IBloodTendency> getBloodTendency(Player player) {
		return player.getCapability(BloodTendencyProvider.TENDENCY_CAPA);
	}

	public static IBloodTendency requireBloodTendency(Player player) {
		return getBloodTendency(player).orElseThrow(IllegalStateException::new);
	}
}
