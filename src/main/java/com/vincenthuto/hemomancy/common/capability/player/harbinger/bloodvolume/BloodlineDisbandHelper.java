package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingSanctumSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public final class BloodlineDisbandHelper {

	private BloodlineDisbandHelper() {
	}

	public static int resetOnlineMembers(MinecraftServer server, Bloodline disbandedLine,
			@Nullable Function<ServerPlayer, Component> notificationFactory) {
		int resetCount = 0;
		for (ServerPlayer online : server.getPlayerList().getPlayers()) {
			if (!disbandedLine.hasMember(online.getUUID())) {
				continue;
			}

			HemoCapabilityAccess.getBloodVolume(online).ifPresent(volume -> {
				volume.setBloodLine(Bloodline.NOBLOODLINE);
				BloodVolumeEvents.syncVolume(online, volume);
				burnBloodlineLedgers(online, disbandedLine);
				if (notificationFactory != null) {
					Component notification = notificationFactory.apply(online);
					if (notification != null) {
						online.displayClientMessage(notification, false);
					}
				}
			});
			resetCount++;
		}
		return resetCount;
	}

	public static int burnBloodlineLedgers(ServerPlayer player, Bloodline disbandedLine) {
		if (player == null || disbandedLine == null || !disbandedLine.isValid()) {
			return 0;
		}

		Inventory inventory = player.getInventory();
		int removed = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!isLedgerForBloodline(stack, disbandedLine.getBloodlineUUID())) {
				continue;
			}

			inventory.setItem(slot, ItemStack.EMPTY);
			removed++;
		}

		if (removed > 0) {
			inventory.setChanged();
			player.containerMenu.broadcastChanges();
			player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
					SoundSource.PLAYERS, 0.8f, 1.4f);
			player.displayClientMessage(
					Component.translatable("hemomancy.ledger.disband.ledger_burned", removed)
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}

		return removed;
	}

	public static boolean burnInvalidLedgerIfPresent(ServerPlayer player, ItemStack stack) {
		if (!(stack.getItem() instanceof UnsignedLedgerItem)) {
			return false;
		}

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.getBoolean(UnsignedLedgerItem.TAG_STATE) || !tag.contains(UnsignedLedgerItem.TAG_BLOODLINE)) {
			return false;
		}

		Bloodline savedLine = Bloodline.deserialize(tag.getCompound(UnsignedLedgerItem.TAG_BLOODLINE));
		if (!savedLine.isValid()) {
			return false;
		}

		BloodlineSavedData savedData = BloodlineSavedData.get(player.server.overworld());
		if (savedData.getBloodline(savedLine.getBloodlineUUID()) != null) {
			return false;
		}

		stack.shrink(stack.getCount());
		player.getInventory().setChanged();
		player.containerMenu.broadcastChanges();
		player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
				SoundSource.PLAYERS, 0.8f, 1.6f);
		player.displayClientMessage(
				Component.translatable("hemomancy.ledger.disband.ledger_burned", 1)
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
		return true;
	}

	private static boolean isLedgerForBloodline(ItemStack stack, UUID bloodlineUuid) {
		if (!(stack.getItem() instanceof UnsignedLedgerItem)) {
			return false;
		}

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.getBoolean(UnsignedLedgerItem.TAG_STATE) || !tag.contains(UnsignedLedgerItem.TAG_BLOODLINE)) {
			return false;
		}

		Bloodline savedLine = Bloodline.deserialize(tag.getCompound(UnsignedLedgerItem.TAG_BLOODLINE));
		return savedLine.isValid() && bloodlineUuid.equals(savedLine.getBloodlineUUID());
	}

	public static int removeOwnedSanctums(MinecraftServer server, Bloodline disbandedLine) {
		if (server == null || disbandedLine == null || !disbandedLine.isValid()) {
			return 0;
		}

		int removed = 0;
		for (ServerLevel level : server.getAllLevels()) {
			FoundingSanctumSavedData sanctumData = FoundingSanctumSavedData.get(level);
			for (UUID memberUuid : disbandedLine.getPlayerUUIDS()) {
				if (!sanctumData.hasSanctum(memberUuid)) {
					continue;
				}
				removeOwnedSanctumBlocks(level, sanctumData, memberUuid);
				sanctumData.remove(memberUuid);
				removed++;
			}
		}
		return removed;
	}

	private static void removeOwnedSanctumBlocks(ServerLevel level, FoundingSanctumSavedData sanctumData,
			UUID memberUuid) {
		BlockPos heartPos = sanctumData.getHeart(memberUuid);
		List<BlockPos> stakePositions = sanctumData.removeStakesAndGet(memberUuid);
		for (BlockPos stakePos : stakePositions) {
			if (level.getBlockState(stakePos).is(BlockInit.hematic_stake.get())) {
				level.removeBlock(stakePos, false);
			}
		}
		if (heartPos != null && level.getBlockState(heartPos).is(BlockInit.consecrated_bloodwell.get())) {
			level.removeBlock(heartPos, false);
		}
	}
}
