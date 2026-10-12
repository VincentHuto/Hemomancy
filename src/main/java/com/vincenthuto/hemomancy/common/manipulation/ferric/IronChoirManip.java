package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class IronChoirManip extends BloodManipulation {
	private static final String SHOTS_LEFT = "hemomancy:iron_choir_shots";
	/** Each pulse also sings one needle at the nearest hostile in this range. */
	public static final double VOLLEY_RANGE = 10.0D;
	/** Same base as a Blood Needle volley needle; at this speed it lands for about 3 before Crimson Mastery. */
	public static final double NEEDLE_BASE_DAMAGE = 0.4D;
	private static final float NEEDLE_SPEED = 7.5F;
	public IronChoirManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 0, false, true));
		player.getPersistentData().putInt(SHOTS_LEFT, 3);
		fireVolleyNeedle(player, level);
		tickContinuousAction(player, world);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CHOIR, 5, 25, player.getPersistentData().getInt(SHOTS_LEFT));
	        }
    }

	private void fireVolleyNeedle(Player player, ServerLevel level) {
		LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(VOLLEY_RANGE),
						e -> e.distanceToSqr(player) <= VOLLEY_RANGE * VOLLEY_RANGE
								&& ManipulationCombatHelper.isHostileTarget(player, e)
								&& ManipulationCombatHelper.visible(player, e))
				.stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
		if (target == null) return;
		BloodNeedleEntity needle = BloodNeedleEntity.fromManipulation(level, player, ItemStack.EMPTY);
		needle.setBaseDamage(NEEDLE_BASE_DAMAGE
				* com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player));
		needle.setForged(true);
		needle.setDamageTendency(getTend());
		needle.setSecondaryDamageTendency(getSecondaryTend());
		Vec3 aim = target.getEyePosition().subtract(needle.position());
		needle.shoot(aim.x, aim.y, aim.z, NEEDLE_SPEED, 0.0F);
		level.addFreshEntity(needle);
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (!(world instanceof ServerLevel level)) return;
		int remaining = player.getPersistentData().getInt(SHOTS_LEFT);
        int before = remaining;
		if (remaining <= 0) return;
		for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(5),
				shot -> ManipulationCombatHelper.hostileProjectile(player, shot)).stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(remaining).toList()) {
			Entity owner = projectile.getOwner();
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.FERRIC_IMPACT,
					player.position().add(0, 1.1, 0), projectile.position(), .5, 12);
			projectile.discard();
			remaining--;
			if (owner instanceof LivingEntity attacker) ManipulationCombatHelper.hurt(this, player, attacker, level,
					4.0F * (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player));
		}
		player.getPersistentData().putInt(SHOTS_LEFT, remaining);
        if (remaining != before) ManipulationVisuals.attached(player, ManipulationVisuals.Form.CHOIR, 5, 25, remaining);
	}

	@Override
	public void finishContinuousAction(Player player, boolean released) {
		player.getPersistentData().remove(SHOTS_LEFT);
		super.finishContinuousAction(player, released);
	}
}
