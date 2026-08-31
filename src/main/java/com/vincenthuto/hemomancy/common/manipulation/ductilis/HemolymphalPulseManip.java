package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.SchoolHitHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Hemolymphal Pulse — a DUCTILIS HUMILIS quick manipulation that sends a blood-sense
 * pulse revealing all nearby living entities through walls for 15 seconds.
 *
 * Ductilis-tended blood moves like nerve signal. Everything alive has a pulse. Find it.
 */
public class HemolymphalPulseManip extends BloodManipulation {

	private static final double RADIUS = 24.0;
	private static final int GLOW_DURATION = 300; // 15 seconds

	public HemolymphalPulseManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		List<LivingEntity> nearby = world.getEntitiesOfClass(
				LivingEntity.class, player.getBoundingBox().inflate(RADIUS));

		int tagged = 0;
		for (LivingEntity entity : nearby) {
			if (entity == player || HemoEntityPredicates.NOBLOOD.test(entity)) continue;
			entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION, 0, false, false));
			if (entity.getHealth() <= entity.getMaxHealth() * .5F) {
				entity.addEffect(new MobEffectInstance(EffectInit.conductive_mark, 200, 0, false, true));
				SchoolHitHelper.markConductive(entity, 200);
			}
			tagged++;
		}

		int steps = 32;
		for (int i = 0; i < steps; i++) {
			double angle = i * (Math.PI * 2.0 / steps);
			for (double r : new double[]{4.0, 10.0, 18.0}) {
				if (world instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
							player.getX() + Math.cos(angle) * r,
							player.getY() + 1.0,
							player.getZ() + Math.sin(angle) * r,
							1, 0, 0, 0, 0.15);
				}
			}
		}
		DuctilisLightningEffects.hemolymphalPulse(player);

		if (tagged > 0) {
			world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT,
					SoundSource.PLAYERS, 0.8f, 0.6f);
		}
	}
}
