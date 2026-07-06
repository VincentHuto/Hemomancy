package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ConserveStateHelper {
	private static final int CONSERVE_ENTRY_TICKS = 60;
	private static final int CONSERVE_RAMP_TICKS = 600;
	private static final double CONSERVE_MAX_MULTIPLIER = 4.0D;
	private static final double CONSERVE_REGEN_PER_SECOND = 3.0D;
	private static final double STILL_MOVEMENT_THRESHOLD_SQ = 0.0009D;
	private static final Map<UUID, ConserveState> STATES = new ConcurrentHashMap<>();

	private ConserveStateHelper() {
	}

	public static int nextStillTicks(int current, boolean physicallyStill, boolean castInWindow) {
		return physicallyStill && !castInWindow ? current + 1 : 0;
	}

	public static double multiplierForStillTicks(int stillTicks) {
		if (stillTicks <= CONSERVE_ENTRY_TICKS) {
			return 1.0D;
		}
		double progress = Math.min(1.0D, (stillTicks - CONSERVE_ENTRY_TICKS) / (double) CONSERVE_RAMP_TICKS);
		return 1.0D + progress * (CONSERVE_MAX_MULTIPLIER - 1.0D);
	}

	public static void tick(Player player, ItemStack fungalSlot) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (!fungalSlot.is(ItemInit.cryostroma_perdurans.get())) {
			STATES.remove(serverPlayer.getUUID());
			return;
		}

		ConserveState state = STATES.computeIfAbsent(serverPlayer.getUUID(), id -> new ConserveState());
		Vec3 movement = serverPlayer.getDeltaMovement();
		boolean physicallyStill = movement.horizontalDistanceSqr() <= STILL_MOVEMENT_THRESHOLD_SQ
				&& Math.abs(movement.y) <= 0.04D;
		boolean castInWindow = state.castTick == serverPlayer.level().getGameTime();
		state.stillTicks = nextStillTicks(state.stillTicks, physicallyStill, castInWindow);

		if (serverPlayer.tickCount % 20 != 0 || state.stillTicks < CONSERVE_ENTRY_TICKS) {
			return;
		}
		double request = CONSERVE_REGEN_PER_SECOND * multiplierForStillTicks(state.stillTicks);
		HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(volume -> {
			BloodFlowLedger.applyCirculationIncome(serverPlayer, volume, "cryostroma_conserve",
					"Cryostroma Stillness", Category.SCAR, request, 20,
					CirculationIncomeHelper.IncomeChannel.SCAR);
		});
	}

	public static void markManipulationCast(Player player) {
		if (player == null || player.level().isClientSide) {
			return;
		}
		STATES.computeIfAbsent(player.getUUID(), id -> new ConserveState())
				.castTick = player.level().getGameTime();
	}

	public static double vascularHealMultiplier(Player player) {
		if (player == null || !hasCryostroma(player)) {
			return 1.0D;
		}
		ConserveState state = STATES.get(player.getUUID());
		return state == null ? 1.0D : multiplierForStillTicks(state.stillTicks);
	}

	private static boolean hasCryostroma(Player player) {
		return HemoCapabilityAccess.getScarState(player)
				.map(scars -> scars.getFungalScar().is(ItemInit.cryostroma_perdurans.get()))
				.orElse(false);
	}

	private static void syncBlood(ServerPlayer player, IBloodVolume volume, double granted) {
		if (granted > 0.0D) {
			PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		}
	}

	private static final class ConserveState {
		private int stillTicks;
		private long castTick = Long.MIN_VALUE;
	}
}
