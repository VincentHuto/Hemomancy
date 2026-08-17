package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.mission.MnemonicReliquaryProgression;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

import java.util.Set;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MachineAccessEvents {
	private static Set<Block> gatedBlocks;

	private MachineAccessEvents() {
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		if (level.isClientSide || player.isSpectator()) {
			return;
		}

		Block block = resolveGatedBlock(level, event.getPos(), level.getBlockState(event.getPos()));
		if (block == BlockInit.mnemonic_reliquary.get() && player instanceof ServerPlayer serverPlayer
				&& HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 3) {
			MnemonicReliquaryProgression.teach(serverPlayer);
		}
		if (!isAccessBlocked(player, block)) {
			return;
		}

		deny(player, block);
		event.setCancellationResult(InteractionResult.FAIL);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBreakBlock(BreakEvent event) {
		Player player = event.getPlayer();
		if (player == null || player.isSpectator()) {
			return;
		}

		Block block = resolveGatedBlock(event.getLevel(), event.getPos(), event.getState());
		if (!isAccessBlocked(player, block)) {
			return;
		}

		event.setCanceled(true);
		breakWithoutLoot(event, block);
	}

	public static void awardMachineCrafted(ServerPlayer player, ItemStack result) {
		if (result.isEmpty()) {
			return;
		}

		Block block = Block.byItem(result.getItem());
		if (!isGatedMachine(block)) {
			return;
		}

		player.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), Math.max(1, result.getCount()));
	}

	public static void awardMachineCrafted(ServerPlayer player, Block block) {
		if (!isGatedMachine(block)) {
			return;
		}

		Item item = block.asItem();
		if (item == ItemStack.EMPTY.getItem()) {
			return;
		}
		player.awardStat(Stats.ITEM_CRAFTED.get(item), 1);
	}

	private static boolean isAccessBlocked(Player player, Block block) {
		if (player.isCreative()) {
			return false;
		}
		return isGatedMachine(block) && !hasCrafted(player, block);
	}

	private static boolean isGatedMachine(Block block) {
		return getGatedBlocks().contains(block);
	}

	private static boolean hasCrafted(Player player, Block block) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return false;
		}
		if (block == BlockInit.mnemonic_reliquary.get()
				&& MnemonicReliquaryProgression.isTaught(serverPlayer)) {
			return true;
		}

		Item item = block.asItem();
		if (item == ItemStack.EMPTY.getItem()) {
			return false;
		}
		return serverPlayer.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0;
	}

	private static Block resolveGatedBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		if (!state.is(BlockInit.filler_block.get())) {
			return state.getBlock();
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null) {
			return level.getBlockState(filler.getMainBlockPos()).getBlock();
		}
		return state.getBlock();
	}

	private static BlockPos resolveBreakPos(LevelAccessor level, BlockPos pos, BlockState state) {
		if (!state.is(BlockInit.filler_block.get())) {
			return pos;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null) {
			return filler.getMainBlockPos();
		}
		return pos;
	}

	private static void breakWithoutLoot(BreakEvent event, Block block) {
		if (event.getPlayer() != null) {
			event.getPlayer().displayClientMessage(
					Component.translatable("hemomancy.machine_access.no_loot", new ItemStack(block.asItem()).getHoverName())
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					true);
		}
		if (event.getLevel() instanceof Level level) {
			BlockPos breakPos = resolveBreakPos(level, event.getPos(), event.getState());
			level.destroyBlock(breakPos, false);
		}
	}

	private static void deny(Player player, Block block) {
		ItemStack required = new ItemStack(block.asItem());
		player.displayClientMessage(
				Component.translatable("hemomancy.machine_access.locked", required.getHoverName())
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
				true);
	}

	private static Set<Block> getGatedBlocks() {
		if (gatedBlocks == null) {
			gatedBlocks = Set.of(
					BlockInit.vial_centrifuge.get(),
					BlockInit.ghastly_alembic.get(),
					BlockInit.unstained_podium.get(),
					BlockInit.scrying_podium.get(),
					BlockInit.fungal_podium.get(),
					BlockInit.somatic_loom.get(),
					BlockInit.scar_station.get(),
					BlockInit.morphling_incubator.get(),
					BlockInit.mycelial_crucible.get(),
					BlockInit.dendritic_distributor.get(),
					BlockInit.consecrated_bloodwell.get(),
					BlockInit.sanguine_monolith.get(),
					BlockInit.sanguine_vigil.get(),
					BlockInit.covenant_throne.get(),
					BlockInit.visceral_mirror.get(),
					BlockInit.mnemonic_reliquary.get(),
					BlockInit.semi_sentient_construct.get());
		}
		return gatedBlocks;
	}
}
