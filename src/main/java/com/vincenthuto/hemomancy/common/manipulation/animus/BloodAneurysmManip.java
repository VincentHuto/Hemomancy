package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
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

	public BloodAneurysmManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		getAction(player, world, heldItemMainhand, position, CHARGE_TICKS);
	        }
    }

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	/** The rupture only seeks a fight: monsters, mobs hunting the caster, or players the caster may harm. */
	private static Optional<LivingEntity> target(Player player) {
		return player.level().getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(TARGET_RADIUS),
						e -> ManipulationCombatHelper.isHostileTarget(player, e)).stream()
				.filter(e -> ManipulationCombatHelper.visible(player, e))
				.filter(e -> e.distanceTo(player) <= TARGET_RADIUS)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player).isEmpty()) {
			player.displayClientMessage(Component.literal("No hostile vessel within reach."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : chargeTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel sLevel)) return;
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, CHARGE_TICKS);

		Optional<LivingEntity> targetOpt = target(player);
		if (targetOpt.isEmpty()) return;

		LivingEntity target = targetOpt.get();
		float masteryMult = (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);

		ManipulationParticles.hurt(this, target, world.damageSources().magic(),
				(DIRECT_DAMAGE * masteryMult * strength));
		Vec3 current = target.getDeltaMovement();
		target.setDeltaMovement(current.x, LAUNCH_FORCE * strength, current.z);
		target.hurtMarked = true;

		Vec3 tPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        // The rupture's far point is the caster, so the swelling vessel can reach from them to the victim.
        ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.ANEURYSM_RUPTURE, tPos,
                player.position().add(0, player.getBbHeight() * 0.6, 0), BURST_RADIUS, 28, 1);
		AABB burstBox = new AABB(target.blockPosition()).inflate(BURST_RADIUS);
		world.getEntitiesOfClass(LivingEntity.class, burstBox,
				e -> e != target && ManipulationCombatHelper.canHarm(player, e)
						&& e.position().distanceTo(tPos) <= BURST_RADIUS)
				.forEach(e -> ManipulationParticles.hurt(this, e, world.damageSources().magic(),
						(BURST_DAMAGE * masteryMult * strength)));

		world.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.8f);
		world.playSound(null, target.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.6f, 0.7f);
	        }
    }
}
