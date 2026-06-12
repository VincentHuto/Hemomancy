package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Glacial Grasp — a T1 (HUMILIS) quick manipulation that freezes water
 * in a 5x5 area around and below the player's feet, creating temporary
 * frosted ice paths.
 * <p>
 * Converts water source blocks into frosted ice, which naturally melts
 * back over time. Perfect for traversing oceans, rivers, and swamps
 * during early exploration.
 * <p>
 * Similar to Frost Walker but activated on-demand via blood magic.
 */
public class GlacialGraspManip extends BloodManipulation {

	private static final int RADIUS = 3;

	public GlacialGraspManip(String name, double cost, double alignLevel, double xpCost,
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
		int frozenCount = 0;
		List<BlockPos> frozenTargets = new ArrayList<>();

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				// Check a circular-ish area
				if (dx * dx + dz * dz > (RADIUS + 1) * (RADIUS + 1)) {
					continue;
				}
				for (int dy = -1; dy <= 0; dy++) {
					BlockPos target = center.offset(dx, dy, dz);
					BlockState state = world.getBlockState(target);

					if (state.getFluidState().is(Fluids.WATER)
							&& state.getFluidState().isSource()
							&& world.getBlockState(target.above()).isAir()) {
						world.setBlock(target, Blocks.FROSTED_ICE.defaultBlockState(), 3);
						// Schedule the ice to start melting via normal tick
						world.scheduleTick(target, Blocks.FROSTED_ICE, 60 + random.nextInt(40));
						frozenCount++;
						frozenTargets.add(target.immutable());
					}
				}
			}
		}

		if (frozenCount > 0) {
			HemomancyTendrilEffects.glacialGrasp(player, center, frozenTargets);
			world.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.8f, 1.3f);

			for (int i = 0; i < 25; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								150 + random.nextFloat() * 105,
								200 + random.nextFloat() * 55,
								255)),
						center.getX() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						center.getY() + 0.2,
						center.getZ() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						1, 0f, 0.15f, 0f, 0.01f);
			}
		}
	}
}
