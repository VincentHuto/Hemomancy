package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingSanctumSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.SanctumFootprint;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.UUID;

public class HematicStakeBlock extends Block {
	public HematicStakeBlock(Properties properties) {
		super(properties);
	}

	public HematicStakeBlock() {
		this(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.requiresCorrectToolForDrops()
				.strength(2.5F, 8.0F)
				.sound(SoundType.METAL)
				.noOcclusion());
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (context.getLevel() instanceof ServerLevel level
				&& context.getPlayer() instanceof ServerPlayer player
				&& !canPlaceStake(level, player, context.getClickedPos())) {
			player.displayClientMessage(Component.translatable("block.hemomancy.hematic_stake.invalid")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
			return null;
		}
		return super.getStateForPlacement(context);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
			ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel serverLevel && placer instanceof ServerPlayer player) {
			registerStake(serverLevel, player, pos);
		}
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
			removeStake(serverLevel, pos);
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	public static boolean canPlaceStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		UUID owner = bloodline.isValid() ? bloodline.getLeaderUUID() : player.getUUID();
		int budget = configuredStakeBudget(
				bloodline.isValid() ? bloodline.getPlayerUUIDS().size() : 1,
				bloodline.isValid() ? bloodline.getNpcMemberCount() : 0);
		return FoundingSanctumSavedData.get(level).canAddStake(owner, pos, budget);
	}

	private static void registerStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		UUID owner = bloodline.isValid() ? bloodline.getLeaderUUID() : player.getUUID();
		int budget = configuredStakeBudget(
				bloodline.isValid() ? bloodline.getPlayerUUIDS().size() : 1,
				bloodline.isValid() ? bloodline.getNpcMemberCount() : 0);
		FoundingSanctumSavedData.get(level).addStake(owner, pos, budget);
	}

	private static int configuredStakeBudget(int playerMembers, int npcMembers) {
		int earned = SanctumFootprint.BASE_STAKE_BUDGET + Math.max(0, playerMembers) + Math.max(0, npcMembers);
		int cap = HemoServerConfig.SANCTUM_MAX_STAKE_BUDGET != null
				? HemoServerConfig.SANCTUM_MAX_STAKE_BUDGET.get()
				: SanctumFootprint.MAX_STAKE_BUDGET;
		return Math.min(cap, earned);
	}

	private static void removeStake(ServerLevel level, BlockPos pos) {
		FoundingSanctumSavedData data = FoundingSanctumSavedData.get(level);
		UUID owner = data.findOwnerContaining(pos);
		if (owner != null) {
			data.removeStake(owner, pos);
		}
	}
}
