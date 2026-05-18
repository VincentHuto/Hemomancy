package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Sanguine Ignition — a T1 (HUMILIS) FLAMMEUS quick manipulation that ignites
 * all living entities within a 5-block radius using superheated blood expelled
 * from the caster's pores.
 *
 * <p>Enemies catch fire for 4 seconds and receive 1 heart of direct heat damage.
 * The caster is unaffected. A reliable, low-cost area denial ability that pairs
 * well with the field-smelting utility of Pyretic Forge.
 */
public class SanguineIgnitionManip extends BloodManipulation {

	private static final double RADIUS = 5.0;
	/** Seconds of fire applied to each target. */
	private static final int FIRE_SECONDS = 4;
	/** Direct damage dealt on ignition (in half-hearts). */
	private static final float IGNITION_DAMAGE = 2.0f; // 1 heart

	public SanguineIgnitionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(RADIUS);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		int hit = 0;
		for (LivingEntity target : targets) {
			if (target.distanceTo(player) <= RADIUS) {
				target.igniteForSeconds((float) FIRE_SECONDS);
				target.hurt(world.damageSources().onFire(), (float) (IGNITION_DAMAGE * SkillPointHelper.getCrimsonMasteryMultiplier(player)));
				hit++;
			}
		}

		world.playSound(null, center, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9f, 0.8f);
		if (hit > 0) {
			world.playSound(null, center, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6f, 1.1f);
		}

		RandomSource random = world.random;
		for (int i = 0; i < 35; i++) {
			float r = 220 + random.nextFloat() * 35;
			float g = 60 + random.nextFloat() * 100;
			float b = random.nextFloat() * 10;
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(r, g, b)),
					center.getX() + 0.5 + (random.nextDouble() - 0.5) * RADIUS * 2,
					center.getY() + 0.3 + random.nextDouble() * 1.5,
					center.getZ() + 0.5 + (random.nextDouble() - 0.5) * RADIUS * 2,
					1, 0f, 0.2f, 0f, 0.02f);
		}
	}
}
