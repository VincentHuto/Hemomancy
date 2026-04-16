package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Vitric Combustion — a T3 (SUMMA) FLAMMEUS quick manipulation that ignites
 * the caster's blood and triggers a concentrated eruption at the targeted
 * surface, dealing heavy fire damage and setting all nearby entities ablaze.
 *
 * <p>The explosion radius scales with the Crimson Mastery skill. Unlike vanilla
 * explosions, Vitric Combustion does <em>not</em> destroy blocks — it is a
 * pure anti-entity blast. Targets within the radius receive:
 * <ul>
 *   <li>4 hearts of fire damage</li>
 *   <li>8 seconds on fire</li>
 *   <li>Knockback away from the epicenter</li>
 * </ul>
 */
public class VitricCombustionManip extends BloodManipulation {

	private static final double BASE_RANGE = 22.0;
	private static final double BASE_BLAST_RADIUS = 4.0;
	private static final float BLAST_DAMAGE = 8.0f; // 4 hearts
	private static final int FIRE_SECONDS = 8;
	private static final double KNOCKBACK_STRENGTH = 1.2;

	public VitricCombustionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier();
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hit = world.clip(new ClipContext(eyePos, endPos,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		Vec3 blastCenter;
		if (hit.getType() == HitResult.Type.MISS) {
			// Detonate at max range
			blastCenter = endPos;
		} else {
			blastCenter = Vec3.atCenterOf(hit.getBlockPos().relative(hit.getDirection()));
		}

		double blastRadius = BASE_BLAST_RADIUS * SkillPointHelper.getCrimsonMasteryMultiplier();

		BlockPos blastPos = BlockPos.containing(blastCenter);
		AABB searchBox = new AABB(blastPos).inflate(blastRadius);
		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		for (LivingEntity target : targets) {
			if (target.position().distanceTo(blastCenter) > blastRadius) continue;
			Vec3 toTarget = target.position().subtract(blastCenter).normalize();
			target.setSecondsOnFire(FIRE_SECONDS);
			target.hurt(world.damageSources().explosion(null, player), BLAST_DAMAGE);
			target.push(toTarget.x * KNOCKBACK_STRENGTH,
					0.4 * KNOCKBACK_STRENGTH,
					toTarget.z * KNOCKBACK_STRENGTH);
		}

		world.playSound(null, blastPos, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, 0.7f);
		world.playSound(null, blastPos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.6f);

		RandomSource random = world.random;
		for (int i = 0; i < 60; i++) {
			float r = 200 + random.nextFloat() * 55;
			float g = 50 + random.nextFloat() * 120;
			float b = random.nextFloat() * 15;
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(r, g, b)),
					blastCenter.x + (random.nextDouble() - 0.5) * blastRadius * 2,
					blastCenter.y + random.nextDouble() * blastRadius,
					blastCenter.z + (random.nextDouble() - 0.5) * blastRadius * 2,
					1, 0f, 0.25f, 0f, 0.03f);
		}
	}
}
