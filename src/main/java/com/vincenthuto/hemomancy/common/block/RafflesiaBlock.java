package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Rafflesia arnoldii — the world's largest flower.
 * Parasitic, lacks leaves, stems, and roots; derives all nutrients from vines.
 * Very rare spawn. Causes nausea to nearby players.
 */
public class RafflesiaBlock extends FlowerBlock {

	public RafflesiaBlock(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		if (state.getBlock() == this) {
			return belowState.canSustainPlant(level, below, Direction.UP, this)
					|| belowState.getBlock() == BlockInit.erythrocytic_mycelium.get()
					|| belowState.getBlock() == BlockInit.infested_wood.get()
					|| belowState.is(Blocks.MYCELIUM)
					|| belowState.is(Blocks.JUNGLE_LOG)
					|| belowState.is(Blocks.JUNGLE_WOOD);
		}
		return this.mayPlaceOn(belowState, level, below);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		AABB area = new AABB(pos).inflate(6.0);
		List<Player> players = level.getEntitiesOfClass(Player.class, area);
		for (Player player : players) {
			if (!player.hasEffect(MobEffects.CONFUSION)) {
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, true, true));
			}
		}
	}
}
