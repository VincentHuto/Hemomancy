package com.vincenthuto.hemomancy.common.block.plant;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Ghost Pipe (Monotropa uniflora) — the "corpse plant."
 * Ghost-white, lacks chlorophyll, survives via mycorrhizal fungi.
 * Grows in the dark on mycelium, erythrocytic mycelium, or infested wood.
 * When a player is nearby, all mobs in the area receive the Glowing effect.
 */
public class GhostPipeBlock extends FlowerBlock {

	public GhostPipeBlock(MobEffect effect, int effectDuration, Properties properties) {
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
					|| belowState.is(Blocks.MYCELIUM);
		}
		return this.mayPlaceOn(belowState, level, below);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		AABB area = new AABB(pos).inflate(8.0);
		List<Player> players = level.getEntitiesOfClass(Player.class, area);
		if (!players.isEmpty()) {
			List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class, area,
					e -> !(e instanceof Player));
			for (LivingEntity mob : mobs) {
				if (!mob.hasEffect(MobEffects.GLOWING)) {
					mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, true, false, true));
				}
			}
		}
	}
}
