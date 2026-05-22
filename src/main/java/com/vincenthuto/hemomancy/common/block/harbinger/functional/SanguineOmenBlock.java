package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SanguineOmenBlock extends Block {

	private static final double EFFECT_RADIUS = 64.0D;
	private static final int EFFECT_DURATION_TICKS = 120;
	private static final float EFFECT_PEAK_ALPHA = 0.88F;

	public SanguineOmenBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		triggerEffect(level, pos, player.isShiftKeyDown());
		return InteractionResult.SUCCESS;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		triggerEffect(level, pos, player.isShiftKeyDown());
		return ItemInteractionResult.SUCCESS;
	}

	private static void triggerEffect(Level level, BlockPos pos, boolean screenOverlay) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.35D, 0.0D);
		level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.9F, 0.55F);
		level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.7F, 0.35F);
		PacketHandler.sendSanguineOmenEffect(center, EFFECT_RADIUS, serverLevel, EFFECT_DURATION_TICKS,
				EFFECT_PEAK_ALPHA, screenOverlay);
	}
}
