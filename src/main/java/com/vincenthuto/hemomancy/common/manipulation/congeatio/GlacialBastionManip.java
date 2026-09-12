package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Glacial Bastion raises a compact temporary iceberg around the caster.
 */
public class GlacialBastionManip extends BloodManipulation {

	private static final int BASE_LIFETIME_TICKS = 500;
	private static final int LIFETIME_VARIANCE = 100;
	private static final int HEIGHT = 3;

	public GlacialBastionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		RandomSource random = world.random;
		BlockPos base = player.blockPosition();
		int placed = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				int dist = Math.abs(dx) + Math.abs(dz);
				if (dist < 2 || dist > 3) {
					continue;
				}
				for (int dy = 0; dy < HEIGHT; dy++) {
					if (dy == HEIGHT - 1 && dist == 3 && random.nextBoolean()) {
						continue;
					}
					BlockPos target = base.offset(dx, dy, dz);
					if (target.equals(base) || target.equals(base.above())) {
						continue;
					}
					if (TemporaryIceManager.place(sLevel, target, BlockInit.frozen_cruor.get().defaultBlockState(),
							BASE_LIFETIME_TICKS + random.nextInt(LIFETIME_VARIANCE))) {
						placed++;
					}
				}
			}
		}

		if (placed > 0) {
			world.playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0f, 0.5f);
			world.playSound(null, base, SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 0.6f, 0.8f);
			for (int i = 0; i < 30; i++) {
				sLevel.sendParticles(
						HemoParticleData.glow(new ParticleColor(
								140 + random.nextFloat() * 80,
								200 + random.nextFloat() * 55,
								255)),
						base.getX() + 0.5 + (random.nextDouble() - 0.5) * 5,
						base.getY() + random.nextDouble() * HEIGHT,
						base.getZ() + 0.5 + (random.nextDouble() - 0.5) * 5,
						1, 0f, 0.05f, 0f, 0.01f);
			}
		} else {
			player.displayClientMessage(Component.literal("No space for the bastion to rise."), true);
		}
	        }
    }
}
