package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
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

/**
 * Sanguine Mending — a T1 (HUMILIS) quick manipulation that repairs
 * the player's held item by infusing it with blood.
 * <p>
 * Restores 50 durability points to the held item per use.
 * Only works on damageable items that are currently damaged.
 */
public class SanguineMendingManip extends BloodManipulation {

	private static final int REPAIR_AMOUNT = 50;

	public SanguineMendingManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty() || !heldItemMainhand.isDamageableItem()
				|| !heldItemMainhand.isDamaged()) {
			return;
		}

		int currentDamage = heldItemMainhand.getDamageValue();
		int repaired = Math.min(currentDamage, REPAIR_AMOUNT);
		heldItemMainhand.setDamageValue(currentDamage - repaired);

		world.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.5f);

		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;
			for (int i = 0; i < 15; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(150 + random.nextFloat() * 105, 50, 50)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						pos.getY() + 1.0 + random.nextDouble() * 0.5,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						1, 0f, 0.1f, 0f, 0.01f);
			}
		}
	}
}
