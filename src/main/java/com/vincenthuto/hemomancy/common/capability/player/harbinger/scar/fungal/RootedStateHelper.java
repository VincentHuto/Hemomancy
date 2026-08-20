package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RootedStateHelper {
	public static final TagKey<Block> FUNGAL_NETWORK_GROUND =
			TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("hemomancy", "fungal_network_ground"));

	private static final double ROOTED_REFUND_FRACTION = 0.15D;
	private static final double ROOTED_REGEN_PER_SECOND = 4.0D;
	private static final int ROOT_SCAN_RADIUS = 2;
	private static final int ROOT_SCAN_INTERVAL_TICKS = 20;
	private static final Map<UUID, RootedCache> ROOTED_CACHE = new ConcurrentHashMap<>();

	private RootedStateHelper() {
	}

	public static void clearSessionState() {
		ROOTED_CACHE.clear();
	}

	public static double refundAmount(double cost, boolean rooted) {
		return rooted ? Math.max(0.0D, cost) * ROOTED_REFUND_FRACTION : 0.0D;
	}

	public static double regenPerSecond(boolean rooted) {
		return rooted ? ROOTED_REGEN_PER_SECOND : 0.0D;
	}

	public static void tick(Player player, ItemStack fungalSlot) {
		if (!(player instanceof ServerPlayer serverPlayer)
				|| player.tickCount % 20 != 0
				|| !fungalSlot.is(ItemInit.rhizovitta_communis.get())) {
			return;
		}
		if (!isRooted(serverPlayer)) {
			return;
		}
		HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(volume -> {
			BloodFlowLedger.applyCirculationIncome(serverPlayer, volume, "rhizovitta_rooted",
					"Rhizovitta Rooted", Category.SCAR, regenPerSecond(true), 20,
					CirculationIncomeHelper.IncomeChannel.SCAR);
		});
	}

	public static void refundManipulationCost(Player player, double effectiveCost) {
		if (!(player instanceof ServerPlayer serverPlayer) || !hasRhizovitta(serverPlayer) || !isRooted(serverPlayer)) {
			return;
		}
		HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(volume -> {
			double granted = CirculationIncomeHelper.grant(serverPlayer, volume, refundAmount(effectiveCost, true),
					CirculationIncomeHelper.IncomeChannel.SCAR);
			syncBlood(serverPlayer, volume, granted);
		});
	}

	public static boolean isRooted(ServerPlayer player) {
		long now = player.level().getGameTime();
		RootedCache cached = ROOTED_CACHE.get(player.getUUID());
		if (cached != null && now - cached.checkedAt() < ROOT_SCAN_INTERVAL_TICKS) {
			return cached.rooted();
		}
		boolean rooted = computeRooted(player);
		ROOTED_CACHE.put(player.getUUID(), new RootedCache(now, rooted));
		return rooted;
	}

	private static boolean hasRhizovitta(Player player) {
		return HemoCapabilityAccess.getScarState(player)
				.map(scars -> scars.getFungalScar().is(ItemInit.rhizovitta_communis.get()))
				.orElse(false);
	}

	private static boolean computeRooted(ServerPlayer player) {
		if (player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS)) {
			return true;
		}
		if (QliphothBloomEvents.isInQliphothBloom(player)) {
			return true;
		}

		BlockPos feet = player.blockPosition();
		for (BlockPos candidate : BlockPos.betweenClosed(
				feet.offset(-ROOT_SCAN_RADIUS, -1, -ROOT_SCAN_RADIUS),
				feet.offset(ROOT_SCAN_RADIUS, 1, ROOT_SCAN_RADIUS))) {
			if (player.level().getBlockState(candidate).is(FUNGAL_NETWORK_GROUND)) {
				return true;
			}
		}
		return false;
	}

	private static void syncBlood(ServerPlayer player, IBloodVolume volume, double granted) {
		if (granted > 0.0D) {
			PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		}
	}

	private record RootedCache(long checkedAt, boolean rooted) {
	}
}
