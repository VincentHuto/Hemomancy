package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PaleSilverBellsBlock extends Block {

	public static final BooleanProperty RUNG = BooleanProperty.create("rung");
	private static final float SILVER_BELLS_PURITY_BOOST = 30.0f;

	public PaleSilverBellsBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(RUNG, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(RUNG);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.CONSUME;
		}

		if (state.getValue(RUNG)) {
			player.displayClientMessage(Component.translatable("hemomancy.silver_bells.already_rung"), false);
			return InteractionResult.CONSUME;
		}

		HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
			if (!progress.hasBegunPurification()) {
				player.displayClientMessage(Component.translatable("hemomancy.silver_bells.not_on_path"), false);
				return;
			}
			if (progress.isPurified()) {
				player.displayClientMessage(Component.translatable("hemomancy.silver_bells.already_pure"), false);
				return;
			}

			progress.addPurity(SILVER_BELLS_PURITY_BOOST);
			UnstainedProgressEvents.syncProgress(serverPlayer, progress);

			level.setBlock(pos, state.setValue(RUNG, Boolean.TRUE), 3);
			level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 0.9f);
			level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.3f);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.END_ROD,
						pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
						32, 0.35, 0.6, 0.35, 0.03);
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
						pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
						16, 0.45, 0.45, 0.45, 0.06);
			}
			player.displayClientMessage(Component.translatable("hemomancy.silver_bells.chimed"), false);
		});

		return InteractionResult.CONSUME;
	}
}
