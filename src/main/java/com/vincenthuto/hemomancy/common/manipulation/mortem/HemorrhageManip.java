package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Hemorrhage — a T1 (HUMILIS) MORTEM quick manipulation that tears open
 * the veins of the closest enemy within 8 blocks, inflicting a lethal
 * wither-like blood-loss debuff.
 *
 * <p>The target receives Wither II for 6 seconds (6 ticks of damage = 3 hearts
 * total at base). The manipulation targets the nearest living entity that isn't
 * the caster. If no valid target is in range, the cast fails silently with a
 * feedback message.
 */
public class HemorrhageManip extends BloodManipulation {

	private static final double RADIUS = 8.0;
	private static final int WITHER_TICKS = 120; // 6 s
	private static final int WITHER_AMP = 1;     // Wither II

	public HemorrhageManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(RADIUS);
		List<LivingEntity> nearby = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		Optional<LivingEntity> closest = nearby.stream()
				.filter(e -> e.distanceTo(player) <= RADIUS)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));

		if (closest.isEmpty()) {
			player.displayClientMessage(
					Component.literal("§8No living vessel within reach."), true);
			return;
		}

		LivingEntity target = closest.get();
		target.addEffect(new MobEffectInstance(MobEffects.WITHER,
				WITHER_TICKS, WITHER_AMP, false, true));

		world.playSound(null, center, SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.7f, 1.6f);

		RandomSource random = world.random;
		// Dark green blood-drain particles between caster and target
		for (int i = 0; i < 20; i++) {
			double t = random.nextDouble();
			double px = player.getX() + (target.getX() - player.getX()) * t;
			double py = player.getEyeY() + (target.getEyeY() - player.getEyeY()) * t;
			double pz = player.getZ() + (target.getZ() - player.getZ()) * t;
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(
							0,
							60 + random.nextFloat() * 40,
							0)),
					px, py, pz,
					1, 0f, 0f, 0f, 0.01f);
		}
	}
}
