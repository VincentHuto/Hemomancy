package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.entity.summon.PhantasmalEchoEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;

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
				|| progress.isInfectionSuppressed()
				|| progress.isClarityPrepared()
				|| progress.isBaselineRestored()
				|| progress.isNovitiateRetortComplete()
				|| progress.getNovitiateDewProduced() > 0
				|| progress.getNovitiateBlocksConsecrated() > 0
				|| progress.isNovitiateProtectionComplete()
				|| progress.getAcceptedObservances() != 0
				|| progress.getClaimedObservances() != 0
				|| hadKnownStillArts;
		if (!hadProgress) {
			return false;
		}
		progress.setBegunPurification(false);
		progress.setPurity(0.0f);
		progress.setClarityUnlocked(false);
		progress.setClarity(0.0f);
		progress.setInfectionSuppressed(false);
		progress.setClarityPrepared(false);
		progress.setBaselineRestored(false);
		progress.setNovitiateRetortComplete(false);
		progress.setNovitiateDewProduced(0);
		progress.setNovitiateBlocksConsecrated(0);
		progress.setNovitiateProtectionComplete(false);
		progress.setAcceptedObservances(0);
		progress.setClaimedObservances(0);
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

	public static void completeUnstainedCure(ServerPlayer player, IUnstainedProgress progress) {
		HemoCapabilityAccess.getInitiatoryDegree(player).ifPresent(degree -> {
			resetHarbingerProgress(player, degree);
			degree.setHasFoundedBloodline(false);
			degree.setFounderIntegrationSevered(false);
			InitiatoryDegreeEvents.syncDegree(player, degree);
		});
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			Bloodline line = volume.getBloodLine();
			if (line != null && line.isValid()) {
				BloodlineSavedData.get(player.server.overworld()).removeMember(line.getBloodlineUUID(), player.getUUID());
			}
			volume.setActive(false);
			volume.setBloodVolume(0);
			volume.setBloodLine(Bloodline.NOBLOODLINE);
			volume.resetBloodDebt();
			volume.setTrickleEnabled(false);
			volume.setAutoDrawEnabled(false);
			volume.setBloodRoutingOptInEnabled(false);
			BloodVolumeEvents.syncVolume(player, volume);
		});
		HemoCapabilityAccess.getKnownManipulations(player).ifPresent(known -> {
			known.setEquippedManipNames(List.of());
			known.setEquippedMemoryRefs(List.of());
			known.setSelectedMemoryRef(null);
			known.setSelectedManip(BloodManipulation.BLANK);
			known.setAvatarActive(false);
			known.setLoadouts(List.of(ManipulationLoadout.empty(0), ManipulationLoadout.empty(1),
					ManipulationLoadout.empty(2)));
			KnownManipulationEvents.syncPlayerEvent(player);
		});
		HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
			var charm = equipment.getStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX);
			if (charm.is(ItemInit.charm_of_vascularium.get())) {
				equipment.setStackInSlot(HarbingerEquipmentMenu.CHARM_SLOT_INDEX, net.minecraft.world.item.ItemStack.EMPTY);
			}
		});
		for (var level : player.server.getAllLevels()) {
			for (var entity : level.getAllEntities()) {
				boolean owned = entity instanceof BoundPuppeteerSummon bound
						&& player.getUUID().equals(bound.hemomancy$getOwnerUUID());
				owned |= entity instanceof BloodThrallEntity thrall && player.getUUID().equals(thrall.getOwnerUUID());
				owned |= entity instanceof PhantasmalEchoEntity echo && player.getUUID().equals(echo.getOwnerUUID());
				owned |= entity instanceof EnthralledDollEntity doll && player.getUUID().equals(doll.getOwnerUUID());
				if (owned) entity.discard();
			}
		}
		progress.setBaselineRestored(true);
		progress.setInfectionSuppressed(false);
		UnstainedProgressEvents.syncProgress(player, progress);
	}
}

