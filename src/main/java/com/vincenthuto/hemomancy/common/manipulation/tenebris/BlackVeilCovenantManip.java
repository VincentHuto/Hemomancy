package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlackVeilCovenantManip extends BloodManipulation {
	private static final double RADIUS = 8.0;
	private static final int DURATION_TICKS = 600;

	public BlackVeilCovenantManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		BlackVeilCovenantManager.addVeil(sLevel, center, RADIUS, DURATION_TICKS);
		PacketHandler.sendBlackVeil(Vec3.atCenterOf(center), RADIUS, sLevel, DURATION_TICKS);

		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(center).inflate(RADIUS),
				e -> e != player && e.isAlive())) {
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0, false, true));
		}

		world.playSound(null, center, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.55F, 0.55F);
		RandomSource random = world.random;
		for (int i = 0; i < 90; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0;
			double radius = random.nextDouble() * RADIUS;
			sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(
							20 + random.nextFloat() * 35, 0, 35 + random.nextFloat() * 70)),
					center.getX() + 0.5 + Math.cos(angle) * radius,
					center.getY() + 0.2 + random.nextDouble() * 5.0,
					center.getZ() + 0.5 + Math.sin(angle) * radius,
					1, 0, -0.03, 0, 0.01);
		}
	}
}
