package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CauterizingRebukeManip extends BloodManipulation {
	private static final double RADIUS = 5.0;

	public CauterizingRebukeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		int purged = 0;
		if (player.removeEffect(MobEffects.POISON)) purged++;
		if (player.removeEffect(MobEffects.WITHER)) purged++;
		if (player.removeEffect(EffectInit.blood_loss)) purged++;
		if (purged > 0) {
			player.setHealth(Math.max(1.0F, player.getHealth() - purged * 2.0F));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, false, true));
		}
		world.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.7F);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
		for (int i = 0; i < 45; i++) {
			sLevel.sendParticles(ParticleTypes.FLAME,
					player.getX() + (world.random.nextDouble() - 0.5) * RADIUS * 1.5,
					player.getY() + 0.2 + world.random.nextDouble() * 1.4,
					player.getZ() + (world.random.nextDouble() - 0.5) * RADIUS * 1.5,
					1, 0, 0.08, 0, 0.02);
		}
	}
}
