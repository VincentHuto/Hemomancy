package com.vincenthuto.hemomancy.common.manipulation.lux;

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
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Hemosynthesis — a T1 (HUMILIS) quick manipulation that converts
 * blood into nourishment, restoring hunger and saturation.
 * <p>
 * Restores 4 hunger points (2 shanks) and 4.0 saturation per use.
 * Will not fire if the player's hunger bar is already full.
 */
public class HemosynthesisManip extends BloodManipulation {

	private static final int HUNGER_RESTORE = 4;
	private static final float SATURATION_RESTORE = 4.0f;

	public HemosynthesisManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		FoodData food = player.getFoodData();
		if (food.getFoodLevel() >= 20) {
			return;
		}

		food.eat(HUNGER_RESTORE, SATURATION_RESTORE);

		world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;
			for (int i = 0; i < 12; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(255, 255 * random.nextFloat(), 200 * random.nextFloat())),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
						pos.getY() + 0.8 + random.nextDouble() * 0.6,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
						1, 0f, 0.15f, 0f, 0.01f);
			}
		}
	}
}
