package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberVisit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ChamberVisitService {
	private static final String PREFIX = Hemomancy.MOD_ID + ":chamber_visit_";
	private static final String ACTIVE = PREFIX + "active";
	private static final String MODE = PREFIX + "mode";
	private static final String REMAINING = PREFIX + "remaining";
	private static final String TOTAL = PREFIX + "total";
	private static final String DREAM_ATTEMPTS = PREFIX + "dream_attempts";
	private static final String DREAM_SEEN = PREFIX + "dream_seen";
	private static final String DREAM_INVENTORY = PREFIX + "dream_inventory";
	private static final String CHAIR_BOUND = PREFIX + "chair_bound";
	private static final String ATTUNED = PREFIX + "attuned";
	private static final String PENDING_CHAIR_SLEEP = PREFIX + "pending_chair_sleep";
	private static final String EXIT_SLEEP_TICKS = PREFIX + "exit_sleep_ticks";
	private static final String FOOD = PREFIX + "food";
	private static final String SATURATION = PREFIX + "saturation";
	private static final String EXHAUSTION = PREFIX + "exhaustion";

	private ChamberVisitService() {
	}

	public static boolean isActive(ServerPlayer player) {
		return player.getPersistentData().getBoolean(ACTIVE);
	}

	public static ChamberVisitMode mode(ServerPlayer player) {
		if (!isActive(player)) return ChamberVisitMode.ADMIN;
		try {
			return ChamberVisitMode.valueOf(player.getPersistentData().getString(MODE));
		} catch (IllegalArgumentException ignored) {
			return ChamberVisitMode.ADMIN;
		}
	}

	public static boolean isDream(ServerPlayer player) {
		return isActive(player) && mode(player) == ChamberVisitMode.DREAM;
	}

	public static boolean isProtected(ServerPlayer player) {
		return isActive(player) && ChamberVisitRules.isProtected(mode(player));
	}

	public static boolean isChairBound(ServerPlayer player) {
		return player.getPersistentData().getBoolean(CHAIR_BOUND);
	}

	public static boolean isAttuned(ServerPlayer player) {
		return player.getPersistentData().getBoolean(ATTUNED);
	}

	public static void bindChair(ServerPlayer player) {
		if (!isChairBound(player)) {
			player.getPersistentData().putBoolean(CHAIR_BOUND, true);
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_WARP_CHAIR_BOUND);
			player.displayClientMessage(Component.translatable("message.hemomancy.warp_chair.bound")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
		}
	}

	public static void attune(ServerPlayer player) {
		bindChair(player);
		player.getPersistentData().putBoolean(ATTUNED, true);
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED);
	}

	public static boolean beginChairVisit(ServerPlayer player) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 3) {
			player.displayClientMessage(Component.translatable("message.hemomancy.warp_chair.degree"), true);
			return false;
		}
		bindChair(player);
		return startVisit(player, isAttuned(player) ? ChamberVisitMode.ATTUNED : ChamberVisitMode.TIMED_CHAIR);
	}

	public static boolean beginRiteVisit(ServerPlayer player) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) < 6) {
			player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.rite_degree"), true);
			return false;
		}
		attune(player);
		return startVisit(player, ChamberVisitMode.ATTUNED);
	}

	public static boolean beginAdminVisit(ServerPlayer player) {
		return startVisit(player, ChamberVisitMode.ADMIN);
	}

	public static void markPendingChairSleep(ServerPlayer player, BlockPos chairPos) {
		player.getPersistentData().putLong(PENDING_CHAIR_SLEEP, chairPos.asLong());
	}

	public static void clearInterruptedChairSleep(ServerPlayer player) {
		player.getPersistentData().remove(PENDING_CHAIR_SLEEP);
	}

	public static void onCompletedSleep(ServerPlayer player, boolean completedNormally) {
		var data = player.getPersistentData();
		if (data.contains(PENDING_CHAIR_SLEEP)) {
			data.remove(PENDING_CHAIR_SLEEP);
			if (completedNormally) beginChairVisit(player);
			return;
		}
		if (!completedNormally) return;

		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (!ChamberVisitRules.ordinaryBedDreamEligible(degree, isActive(player),
				FungalGardenTravelHelper.isProjectionActive(player))) return;
		boolean seen = data.getBoolean(DREAM_SEEN);
		int attempts = data.getInt(DREAM_ATTEMPTS);
		if (player.getRandom().nextDouble() < ChamberVisitRules.dreamChance(attempts, seen)) {
			data.putBoolean(DREAM_SEEN, true);
			data.putInt(DREAM_ATTEMPTS, 0);
			startVisit(player, ChamberVisitMode.DREAM);
		} else if (!seen) {
			data.putInt(DREAM_ATTEMPTS, attempts + 1);
		}
	}

	private static boolean startVisit(ServerPlayer player, ChamberVisitMode visitMode) {
		if (isActive(player) || player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)
				|| player.getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) == null) return false;
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		int total = ChamberVisitRules.durationTicks(degree, visitMode, isAttuned(player));
		var data = player.getPersistentData();
		data.putBoolean(ACTIVE, true);
		data.putString(MODE, visitMode.name());
		data.putInt(REMAINING, total);
		data.putInt(TOTAL, total);
		data.putInt(FOOD, player.getFoodData().getFoodLevel());
		data.putFloat(SATURATION, player.getFoodData().getSaturationLevel());
		data.putFloat(EXHAUSTION, player.getFoodData().getExhaustionLevel());
		if (visitMode == ChamberVisitMode.DREAM) {
			data.put(DREAM_INVENTORY, player.getInventory().save(new ListTag()));
		}
		ChamberOfWillManager.get(player.getServer()).enterChamber(player);
		sync(player);
		return true;
	}

	public static void beginPairedChairExit(ServerPlayer player, BlockPos chairPos) {
		if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)
				|| player.getPersistentData().contains(EXIT_SLEEP_TICKS)) return;
		player.getPersistentData().putInt(EXIT_SLEEP_TICKS, 40);
		player.startSleeping(chairPos);
	}

	public static void tick(ServerPlayer player) {
		if (!isActive(player)) return;
		if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			returnFromVisit(player);
			return;
		}
		if (isProtected(player)) restoreFood(player);
		var data = player.getPersistentData();
		if (data.contains(EXIT_SLEEP_TICKS)) {
			int ticks = data.getInt(EXIT_SLEEP_TICKS) - 1;
			if (ticks <= 0) {
				data.remove(EXIT_SLEEP_TICKS);
				player.stopSleepInBed(false, false);
				returnFromVisit(player);
			} else {
				data.putInt(EXIT_SLEEP_TICKS, ticks);
			}
			return;
		}
		if (!mode(player).timed()) return;
		int remaining = data.getInt(REMAINING) - 1;
		data.putInt(REMAINING, remaining);
		if (remaining <= 0) {
			returnFromVisit(player);
			return;
		}
		if (remaining % 20 == 0) {
			int seconds = remaining / 20;
			player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.timer",
					seconds / 60, String.format("%02d", seconds % 60)), true);
			if (seconds == 60 || seconds == 30 || seconds == 10) {
				player.displayClientMessage(Component.translatable("message.hemomancy.chamber_visit.warning", seconds)
						.withStyle(ChatFormatting.DARK_RED), false);
			}
		}
		if (remaining % 20 == 0) sync(player);
	}

	public static void returnFromVisit(ServerPlayer player) {
		if (!isActive(player)) {
			if (player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
				ChamberOfWillManager.get(player.getServer()).exitChamber(player);
			}
			return;
		}
		if (player.containerMenu != player.inventoryMenu) player.closeContainer();
		var data = player.getPersistentData();
		if (mode(player) == ChamberVisitMode.DREAM && data.contains(DREAM_INVENTORY)) {
			player.getInventory().load(data.getList(DREAM_INVENTORY, net.minecraft.nbt.Tag.TAG_COMPOUND));
			player.inventoryMenu.broadcastChanges();
		}
		restoreFood(player);
		clearSession(data);
		ChamberOfWillManager.get(player.getServer()).exitChamber(player);
		PacketHandler.sendToPlayer(player, PacketSyncChamberVisit.inactive());
	}

	/** Repairs a persisted visit after a crash/reconnect that already returned the player to another dimension. */
	public static void recoverOutsideChamber(ServerPlayer player) {
		if (!isActive(player)) return;
		var data = player.getPersistentData();
		if (mode(player) == ChamberVisitMode.DREAM && data.contains(DREAM_INVENTORY)) {
			player.getInventory().load(data.getList(DREAM_INVENTORY, net.minecraft.nbt.Tag.TAG_COMPOUND));
			player.inventoryMenu.broadcastChanges();
		}
		restoreFood(player);
		clearSession(data);
		PacketHandler.sendToPlayer(player, PacketSyncChamberVisit.inactive());
	}

	private static void restoreFood(ServerPlayer player) {
		var data = player.getPersistentData();
		if (!data.contains(FOOD)) return;
		player.getFoodData().setFoodLevel(data.getInt(FOOD));
		player.getFoodData().setSaturation(data.getFloat(SATURATION));
		player.getFoodData().setExhaustion(data.getFloat(EXHAUSTION));
	}

	private static void clearSession(net.minecraft.nbt.CompoundTag data) {
		for (String key : new String[] { ACTIVE, MODE, REMAINING, TOTAL, DREAM_INVENTORY,
				EXIT_SLEEP_TICKS, FOOD, SATURATION, EXHAUSTION }) data.remove(key);
	}

	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player, isActive(player)
				? new PacketSyncChamberVisit(true, mode(player), player.getPersistentData().getInt(REMAINING),
						player.getPersistentData().getInt(TOTAL))
				: PacketSyncChamberVisit.inactive());
	}
}
