package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneFootprint;
import com.vincenthuto.hemomancy.common.tile.functional.HematicStakeBlockEntity;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.UUID;

public class HematicStakeBlock extends Block implements EntityBlock {
	public HematicStakeBlock(Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HematicStakeBlockEntity(pos, state);
	}

	public HematicStakeBlock() {
		this(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.noCollission()
				.instabreak()
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
		if (!isProgenitor(player) || !level.isEmptyBlock(pos)) {
			return false;
		}
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		UUID owner = bloodline.getLeaderUUID();
		int budget = configuredStakeBudget(
				bloodline.getPlayerUUIDS().size(),
				bloodline.getNpcMemberCount());
		return FoundingFaneSavedData.get(level).canAddStake(owner, pos, budget);
	}

	public static boolean manifestStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		if (!canPlaceStake(level, player, pos)) {
			return false;
		}
		if (!level.setBlock(pos, com.vincenthuto.hemomancy.common.init.BlockInit.hematic_stake.get().defaultBlockState(),
				Block.UPDATE_ALL)) {
			return false;
		}
		registerStake(level, player, pos);
		return true;
	}

	public static boolean isProgenitor(ServerPlayer player) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		return bloodline.isValid() && player.getUUID().equals(bloodline.getLeaderUUID());
	}

	private static void registerStake(ServerLevel level, ServerPlayer player, BlockPos pos) {
		Bloodline bloodline = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.getBloodLine())
				.orElse(Bloodline.NOBLOODLINE);
		if (!bloodline.isValid()) {
			return;
		}
		UUID owner = bloodline.getLeaderUUID();
		int budget = configuredStakeBudget(
				bloodline.getPlayerUUIDS().size(),
				bloodline.getNpcMemberCount());
		FoundingFaneSavedData.get(level).addStake(owner, pos, budget);
	}

	private static int configuredStakeBudget(int playerMembers, int npcMembers) {
		int earned = FaneFootprint.BASE_STAKE_BUDGET + Math.max(0, playerMembers) + Math.max(0, npcMembers);
		int cap = HemoServerConfig.FANE_MAX_STAKE_BUDGET != null
				? HemoServerConfig.FANE_MAX_STAKE_BUDGET.get()
				: FaneFootprint.MAX_STAKE_BUDGET;
		return Math.min(cap, earned);
	}

	private static void removeStake(ServerLevel level, BlockPos pos) {
		FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
		UUID owner = data.findOwnerForStake(pos);
		if (owner != null) {
			data.removeStake(owner, pos);
		}
	}
}
