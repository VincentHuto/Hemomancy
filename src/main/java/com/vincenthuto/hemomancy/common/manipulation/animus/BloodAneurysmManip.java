package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Blood Aneurysm — a T3 (SUMMA) ANIMUS charged manipulation that ruptures the
 * blood vessels of the nearest enemy, launching them skyward and detonating a
 * haematic burst that damages all entities around them.
 *
 * <p>The primary target takes heavy magic damage and is launched upward.
 * All other living entities within the burst radius of the primary target
 * take secondary splash damage. Both values scale with Crimson Mastery.
 */
public class BloodAneurysmManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 40;

	private static final double TARGET_RADIUS = 10.0;
	private static final float DIRECT_DAMAGE = 8.0f;
	private static final double BURST_RADIUS = 4.0;
	private static final float BURST_DAMAGE = 3.0f;
	private static final float LAUNCH_FORCE = 1.2f;
	private static final DustParticleOptions BLOOD_DUST =
			new DustParticleOptions(new Vector3f(0.9F, 0.0F, 0.0F), 1.0F);

	public BloodAneurysmManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, CHARGE_TICKS);
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		if (!(world instanceof ServerLevel sLevel)) return;
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, CHARGE_TICKS);

		AABB searchBox = new AABB(player.blockPosition()).inflate(TARGET_RADIUS);
		List<LivingEntity> nearby = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		Optional<LivingEntity> targetOpt = nearby.stream()
				.filter(e -> e.distanceTo(player) <= TARGET_RADIUS)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));

		if (targetOpt.isEmpty()) {
			player.displayClientMessage(Component.literal("§8No vessel within reach."), true);
			return;
		}

		LivingEntity target = targetOpt.get();
		float masteryMult = (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);

		target.hurt(world.damageSources().magic(),
				TendencyAffinityRules.adjustManipulationDamage(player, target, this,
						DIRECT_DAMAGE * masteryMult * strength));
		Vec3 current = target.getDeltaMovement();
		target.setDeltaMovement(current.x, LAUNCH_FORCE * strength, current.z);
		target.hurtMarked = true;

		Vec3 tPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
		AABB burstBox = new AABB(target.blockPosition()).inflate(BURST_RADIUS);
		world.getEntitiesOfClass(LivingEntity.class, burstBox,
				e -> e != player && e != target && e.isAlive()
						&& e.position().distanceTo(tPos) <= BURST_RADIUS)
				.forEach(e -> e.hurt(world.damageSources().magic(),
						TendencyAffinityRules.adjustManipulationDamage(player, e, this,
								BURST_DAMAGE * masteryMult * strength)));

		RandomSource random = world.random;
		for (int i = 0; i < 50; i++) {
			sLevel.sendParticles(
					BLOOD_DUST,
					tPos.x + (random.nextDouble() - 0.5) * BURST_RADIUS,
					tPos.y + random.nextDouble() * 1.5,
					tPos.z + (random.nextDouble() - 0.5) * BURST_RADIUS,
					1, (random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.25,
					(random.nextDouble() - 0.5) * 0.3, 0.04f);
		}

		world.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.8f);
		world.playSound(null, target.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.6f, 0.7f);
	}
}
