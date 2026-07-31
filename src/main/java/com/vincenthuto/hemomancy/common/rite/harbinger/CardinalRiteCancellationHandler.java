package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCancellationRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Resolves continuous Blood Absorption aimed at an owner's planted rite staff. */
public final class CardinalRiteCancellationHandler {
	private CardinalRiteCancellationHandler() {
	}

	public static boolean tryChannel(ServerPlayer player, double range) {
		ActiveCardinalRite rite = targetedRite(player, range);
		if (rite == null) return false;
		rite.requestCancellation(player.serverLevel().getGameTime());
		CardinalRiteSavedData.get(player.serverLevel()).setDirty();
		return true;
	}

	public static boolean canStart(ServerPlayer player, double range) {
		return targetedRite(player, range) != null;
	}

	private static ActiveCardinalRite targetedRite(ServerPlayer player, double range) {
		ServerLevel level = player.serverLevel();
		ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(player.getUUID());
		if (rite == null) return null;

		HitResult trace = player.pick(range, 0.0F, true);
		boolean targetsFocus = trace instanceof BlockHitResult blockHit
				&& trace.getType() == HitResult.Type.BLOCK
				&& blockHit.getBlockPos().equals(rite.getCenterPos());
		boolean targetsRenderedStaff = CardinalRiteCancellationRules.aimsAtPlantedStaff(
				player.getEyePosition(), player.getViewVector(0.0F), range, rite.getCenterPos());
		BlockPos target = targetsFocus || targetsRenderedStaff ? rite.getCenterPos() : null;
		if (!CardinalRiteCancellationRules.canChannel(
				player.getUUID(), rite.getPlayerUUID(), target,
				rite.getCenterPos(), rite.hasEscrowedStaff(), rite.isTerminal())) {
			return null;
		}
		return rite;
	}
}
