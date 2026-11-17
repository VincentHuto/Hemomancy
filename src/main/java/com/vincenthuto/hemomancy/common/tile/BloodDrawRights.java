package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Who may take blood, clot or Formations out of a machine (CL-8): the player who placed it and their bloodline. Anyone
 * else only when the server allows it ({@code foreignReservoirDraw}). A machine with no recorded owner (placed by a
 * structure, or before Track 3) is open to all. Absorption, Blood Thralls and Condenser collection all ask here.
 */
public final class BloodDrawRights {
	private BloodDrawRights() {
	}

	public static boolean mayDraw(Player player, BlockEntity blockEntity) {
		return player != null && mayDraw(player.level() instanceof ServerLevel level ? level : null,
				player.getUUID(), blockEntity);
	}

	public static boolean mayDraw(@Nullable ServerLevel level, @Nullable UUID drawer, BlockEntity blockEntity) {
		if (!(blockEntity instanceof IBloodReservoir)) return true;
		UUID owner = ReservoirBlood.settled(blockEntity).owner();
		Bloodline line = owner == null || level == null ? null
				: BloodlineSavedData.get(level.getServer().overworld()).getBloodlineForPlayer(owner);
		return permits(owner, drawer, line != null && drawer != null && line.hasMember(drawer),
				HemoConfigValues.get(HemoServerConfig.FOREIGN_RESERVOIR_DRAW, false));
	}

	/** The rule itself: unowned or own blood, kin's blood, or anyone's when the server says so. */
	public static boolean permits(@Nullable UUID owner, @Nullable UUID drawer, boolean sameBloodline,
			boolean foreignAllowed) {
		if (owner == null) return true;
		if (drawer == null) return foreignAllowed;
		return owner.equals(drawer) || sameBloodline || foreignAllowed;
	}
}
