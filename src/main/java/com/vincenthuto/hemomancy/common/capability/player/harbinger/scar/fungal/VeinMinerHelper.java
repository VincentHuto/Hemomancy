package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.manipulation.ferric.OreVeins;
import com.vincenthuto.hemomancy.common.manipulation.verb.CrawlSession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Arrays;

/**
 * The Talaromyces Minus scar's vein mine and the break-permission checks {@code PlayerBlockEdits} shares. A sneaking
 * break of an ore with the scar crawls the rest of its lode through {@link OreVeins}, the same crawl Lode Draw uses
 * (core-loop Track 8): one vein miner with one set of rules and guards, breaking each block through
 * {@code PlayerBlockEdits.breakBlock} with the player's own tool. Each extra ore costs {@link #BLOOD_PER_EXTRA_ORE}.
 */
public class VeinMinerHelper {
	/** Each ore past the one the player broke costs this, the Lode Draw price (NQ-04-5, CQ-8). */
	public static final double BLOOD_PER_EXTRA_ORE = 12.0D;
	static final BloodAccounting.Source VEIN = BloodAccounting.Source.of("talaromyces_vein", Category.SCAR, false);

	public static boolean hasBreakPermission(ServerPlayer player, BlockPos pos) {
		if (!hasEditPermission(player, pos)) {
			return false;
		}
		BlockState state = player.getCommandSenderWorld().getBlockState(pos);
		return !CommonHooks.fireBlockBreak(player.getCommandSenderWorld(), player.gameMode.getGameModeForPlayer(),
				player, pos, state).isCanceled();
	}

	public static boolean hasEditPermission(ServerPlayer player, BlockPos pos) {
		if (ServerLifecycleHooks.getCurrentServer().isUnderSpawnProtection((ServerLevel) player.getCommandSenderWorld(),
				pos, player) || !player.level().mayInteract(player, pos)) {
			return false;
		}
		return Arrays.stream(Direction.values()).allMatch(e -> player.mayUseItemAt(pos, e, ItemStack.EMPTY));
	}

	/** Whether {@code tool} gets drops from {@code state}: the vein never mines past the tool's tier. */
	public static boolean canHarvest(BlockState state, ItemStack tool) {
		return !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
	}

	/**
	 * Crawls the lode the player just broke into and breaks the rest of it with the tool in hand. The block the
	 * player broke is left to that ordinary break. Never mines past the tool's tier.
	 */
	public static InteractionResult tryVeinMine(ItemStack tool, Player player, BlockPos pos) {
		if (!(player instanceof ServerPlayer server) || !(server.level() instanceof ServerLevel level)) {
			return InteractionResult.PASS;
		}
		BlockState target = level.getBlockState(pos);
		// The player's own break must harvest the ore (the HarvestCheck event included): a dry Living pickaxe, which
		// digs like a bare hand, opens no vein.
		if (!OreVeins.isOre(target) || !canHarvest(target, tool) || !target.canHarvestBlock(level, pos, server)) {
			return InteractionResult.PASS;
		}
		CrawlSession crawl = OreVeins.crawl(level, pos, state -> canHarvest(state, tool));
		crawl.advance(level, OreVeins.MAX_BLOCKS);
		// The vein is paid ore by ore and stops where the blood stops.
		int broken = OreVeins.mine(level, server, crawl, tool, pos,
				(minedPos, state) -> BloodAccounting.pay(server, BLOOD_PER_EXTRA_ORE, VEIN),
				next -> BloodAccounting.canPayInFull(server, BLOOD_PER_EXTRA_ORE, VEIN));
		if (broken <= 0) {
			return InteractionResult.PASS;
		}
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STONE_BREAK,
				SoundSource.PLAYERS, 1.0F, 1.0F);
		return InteractionResult.SUCCESS;
	}
}
