package com.vincenthuto.hemomancy.common.block.unstained.crafting;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.crafting.StillwaterCondenserBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class StillwaterCondenserBlock extends BaseEntityBlock {
	public static final MapCodec<StillwaterCondenserBlock> CODEC = simpleCodec(StillwaterCondenserBlock::new);
	public StillwaterCondenserBlock(BlockBehaviour.Properties properties) { super(properties); }
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
	@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StillwaterCondenserBlockEntity(pos, state);
	}
	@Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, BlockEntityInit.stillwater_condenser.get(),
				StillwaterCondenserBlockEntity::serverTick);
	}

	private InteractionResult open(Level level, BlockPos pos, Player player) {
		boolean eligible = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification() && progress.getPurity() >= 50f).orElse(false);
		if (!eligible) {
			player.displayClientMessage(Component.literal("The condenser remains still until Purity reaches 50.")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
				&& level.getBlockEntity(pos) instanceof StillwaterCondenserBlockEntity be) {
			serverPlayer.openMenu(be, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) { return open(level, pos, player); }
	@Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		return open(level, pos, player) == InteractionResult.FAIL ? ItemInteractionResult.FAIL : ItemInteractionResult.SUCCESS;
	}
	@Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof StillwaterCondenserBlockEntity be) {
			Containers.dropContents(level, pos, be);
			level.updateNeighbourForOutputSignal(pos, this);
		}
		super.onRemove(state, level, pos, newState, moving);
	}
	@Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() < 0.45f) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.35 + random.nextDouble() * 0.3,
					pos.getY() + 0.72, pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0, 0.008, 0);
		}
	}
}
