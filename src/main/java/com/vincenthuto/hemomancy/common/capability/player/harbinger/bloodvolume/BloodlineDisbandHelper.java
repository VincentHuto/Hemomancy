package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleBursts;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
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

import java.util.List;
import java.util.UUID;

public final class BloodlineDisbandHelper {

	private static final ParticleColor ASH = new ParticleColor(78, 66, 62);

	private BloodlineDisbandHelper() {
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

	/**
	 * Burns a signed ledger whose line is gone, or crumbles one written before its holder was removed from the line
	 * (PT-37, NQ-11-1 B). Returns whether the stack went.
	 */
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
			if (!savedData.ledgerCrumbles(savedLine.getBloodlineUUID(), player.getUUID(),
					tag.getLong(UnsignedLedgerItem.TAG_MINTED))) {
				return false;
			}
			stack.shrink(stack.getCount());
			changed(player);
			crumbleEffect(player);
			return true;
		}

		stack.shrink(stack.getCount());
		changed(player);
		player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
				SoundSource.PLAYERS, 0.8f, 1.6f);
		player.displayClientMessage(
				Component.translatable("hemomancy.ledger.disband.ledger_burned", 1)
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);
		return true;
	}

	/** Applies the removed-player rule to every carried ledger at once, as when the removal happens. */
	public static int crumbleRemovedLedgers(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		int removed = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.getItem() instanceof UnsignedLedgerItem && burnInvalidLedgerIfPresent(player, stack)) removed++;
		}
		return removed;
	}

	private static void changed(ServerPlayer player) {
		player.getInventory().setChanged();
		player.containerMenu.broadcastChanges();
	}

	/** One line, a dry crumbling sound and a small burst of ash motes. Nothing is left behind (NQ-11-2). */
	private static void crumbleEffect(ServerPlayer player) {
		player.level().playSound(null, player.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 0.9f, 0.7f);
		HemoParticleBursts.outward(player.serverLevel(), HemoParticleData.ember(ASH, 0.08F, 0.8F, 24),
				player.getX(), player.getY() + 1.0D, player.getZ(), 14, 0.05D, 0.2D);
		player.displayClientMessage(Component.translatable("hemomancy.ledger.crumbled")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
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

	public static int removeOwnedFanes(MinecraftServer server, Bloodline disbandedLine) {
		if (server == null || disbandedLine == null || !disbandedLine.isValid()) {
			return 0;
		}

		int removed = 0;
		for (ServerLevel level : server.getAllLevels()) {
			FoundingFaneSavedData faneData = FoundingFaneSavedData.get(level);
			for (UUID memberUuid : disbandedLine.getPlayerUUIDS()) {
				if (!faneData.hasFane(memberUuid)) {
					continue;
				}
				removeOwnedFaneBlocks(level, faneData, memberUuid);
				faneData.remove(memberUuid);
				removed++;
			}
		}
		return removed;
	}

	private static void removeOwnedFaneBlocks(ServerLevel level, FoundingFaneSavedData faneData,
			UUID memberUuid) {
		BlockPos heartPos = faneData.getHeart(memberUuid);
		List<BlockPos> stakePositions = faneData.removeStakesAndGet(memberUuid);
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
