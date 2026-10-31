package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Marimo's Heart (Brined Court Track 10): the densest knot of his moss, at the top of the mass on the undercroft's
 * island. It is no mob and has no hitbox to strike; like the rest of the moss it can't be broken outside Creative. Its
 * block entity notices the living who come down to it (the first-sight scene, and a pulse through the moss for every
 * arrival) and its renderer draws the pulse. Touching it with an empty hand lets the anchored speak for him.
 */
public class MarimoHeartBlock extends MarimoMossBlock implements EntityBlock {
	public MarimoHeartBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MarimoHeartBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.marimo_heart.get(), MarimoHeartBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player instanceof ServerPlayer server) CourtPresences.chorus(server, pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if (random.nextFloat() > 0.3F) return;
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double r = 0.6D + random.nextDouble() * 0.5D;
		level.addParticle(HemoParticleData.glow(GOLD), pos.getX() + 0.5D + Math.cos(angle) * r,
				pos.getY() + 0.9D + random.nextDouble() * 0.4D, pos.getZ() + 0.5D + Math.sin(angle) * r,
				0.0D, 0.012D, 0.0D);
	}
}
