package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncPomeProgress;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Shared mutual-exclusion helpers for the Harbinger and Unstained paths.
 */
public final class PathMutualExclusionHelper {

	private PathMutualExclusionHelper() {
	}

	public static boolean resetHarbingerProgress(ServerPlayer player) {
		return HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> resetHarbingerProgress(player, degree))
				.orElse(false);
	}

	public static boolean resetHarbingerProgress(ServerPlayer player, IInitiatoryDegree degree) {
		boolean hadProgress = degree.getDegreeNumber() > 0
				|| degree.isQliphothCommunionDone()
				|| degree.getTotalPomesConsumed() > 0
				|| degree.getPomeEmpowermentExpiry() > 0L;
		if (!hadProgress) {
			return false;
		}
		degree.setDegreeNumber(0);
		degree.resetPomeCommunion();
		InitiatoryDegreeEvents.syncDegree(player, degree);
		PacketHandler.sendToPlayer(player, new PacketSyncPomeProgress(0));
		return true;
	}

	public static boolean resetUnstainedProgress(ServerPlayer player) {
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> resetUnstainedProgress(player, progress))
				.orElse(false);
	}

	public static boolean resetUnstainedProgress(ServerPlayer player, IUnstainedProgress progress) {
		boolean hadKnownStillArts = HemoCapabilityAccess.getKnownStillArts(player)
				.map(known -> !known.getKnownArtNames().isEmpty())
				.orElse(false);
		boolean hadProgress = progress.hasBegunPurification()
				|| progress.getPurity() > 0.0f
				|| progress.hasClarityUnlocked()
				|| progress.getClarity() > 0.0f
				|| hadKnownStillArts;
		if (!hadProgress) {
			return false;
		}
		progress.setBegunPurification(false);
		progress.setPurity(0.0f);
		progress.setClarityUnlocked(false);
		progress.setClarity(0.0f);
		UnstainedProgressEvents.syncProgress(player, progress);
		HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> {
			known.setKnownArtNames(List.of());
			KnownStillArtEvents.sync(player, known);
		});
		return true;
	}

	public static boolean enforceHarbingerResetOnClarity(ServerPlayer player, IUnstainedProgress progress) {
		if (!progress.isPurified() || !progress.hasClarityUnlocked()) {
			return false;
		}
		return resetHarbingerProgress(player);
	}
}

