package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ferric Resonance makes the caster's blood-iron ring through tools, weapons,
 * and armor for a short burst of disciplined work and combat.
 */
public class FerricResonanceManip extends BloodManipulation {

	private static final int DURATION_TICKS = 600;

	public FerricResonanceManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, DURATION_TICKS, 1, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 0, false, true, true));

		if (world instanceof ServerLevel sLevel) {
			sLevel.sendParticles(ParticleTypes.CRIT,
					player.getX(), player.getY() + 1.0, player.getZ(),
					24, 0.4, 0.4, 0.4, 0.08);
		}

		world.playSound(null, player.blockPosition(),
				SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.6f, 1.4f);
	}
}
