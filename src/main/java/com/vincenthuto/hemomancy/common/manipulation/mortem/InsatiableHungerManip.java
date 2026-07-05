package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class InsatiableHungerManip extends BloodManipulation {
	private static final double BASE_RANGE = 10.0D;
	private static final int DURATION_TICKS = 220;

	public InsatiableHungerManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel serverLevel)) return;
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		LivingEntity target = world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(range),
						entity -> entity != player && entity.isAlive() && !entity.isAlliedTo(player)
								&& entity.distanceTo(player) <= range)
				.stream()
				.min(Comparator.comparingDouble(player::distanceToSqr))
				.orElse(null);
		if (target == null) {
			return;
		}

		target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, DURATION_TICKS, 0, false, true, true));
		world.playSound(null, target.blockPosition(), SoundEvents.HUSK_AMBIENT, SoundSource.PLAYERS, 0.65F, 0.7F);
		serverLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(45, 105, 30)),
				target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
				22, 0.45D, 0.55D, 0.45D, 0.02D);
	}
}
