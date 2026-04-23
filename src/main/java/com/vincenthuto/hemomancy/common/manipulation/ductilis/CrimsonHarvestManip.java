package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crimson Harvest — a T1 (HUMILIS) quick manipulation that accelerates
 * crop growth in a 5x5 area around the player, similar to bone meal.
 * <p>
 * Applies one growth tick to every growable block (crops, saplings,
 * flowers, mushrooms, etc.) in the area. Perfect for farming utility.
 */
public class CrimsonHarvestManip extends BloodManipulation {

	private static final int RADIUS = 2;

	public CrimsonHarvestManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) {
			return;
		}

		BlockPos center = player.blockPosition();
		RandomSource random = world.random;
		int grownCount = 0;

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				for (int dy = -1; dy <= 2; dy++) {
					BlockPos target = center.offset(dx, dy, dz);
					BlockState state = world.getBlockState(target);

					if (state.getBlock() instanceof BonemealableBlock growable) {
						if (growable.isValidBonemealTarget(world, target, state)) {
							if (growable.isBonemealSuccess(world, random, target, state)) {
								growable.performBonemeal(sLevel, random, target, state);
								grownCount++;

								sLevel.sendParticles(
										GlowParticleFactory.createData(
												new ParticleColor(180 + random.nextFloat() * 75, 30, 30)),
										target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
										3, 0.2, 0.2, 0.2, 0.01f);
							}
						}
					}
				}
			}
		}

		if (grownCount > 0) {
			world.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.8f, 1.0f);

			for (int i = 0; i < 10; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(200, 50, 50)),
						center.getX() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						center.getY() + 0.5,
						center.getZ() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						1, 0f, 0.2f, 0f, 0.02f);
			}
		}
	}
}
