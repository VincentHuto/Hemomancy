package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Radiant Wake — Lux Magister. Blood stretched into open radiance holds the caster aloft: while channelled,
 * a falling caster glides along their gaze instead of dropping. Each pulse mends nearby allies (the caster
 * included), burns hostile bodies the light already holds, and leaves them Illuminated for Prismatic Reproof
 * and White Verdict to answer.
 */
public class RadiantWakeManip extends BloodManipulation {
	public static final double RADIUS = 7.0D;
	public static final float PULSE_HEAL = 1.0F;
	/** Lux damage each pulse deals to an enemy that is already Illuminated, before Crimson Mastery. */
	public static final float PULSE_DAMAGE = 1.0F;
	private static final int ILLUMINATED_TICKS = 40;
	/** Glide tuning: the steepest fall the wake allows, and the forward push added each tick. */
	public static final double MAX_FALL_SPEED = -0.08D;
	private static final double GLIDE_THRUST = 0.06D;
	private static final double MAX_GLIDE_SPEED = 0.85D;

	public RadiantWakeManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		try (var schoolCast = SchoolDamage.cast(this, player, 1)) {
			if (!(world instanceof ServerLevel level)) return;
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.RADIANT_WAKE, 1, RadiantWakeRules.CUE_TICKS,
					RadiantWakeRules.encode(gliding(player)));
			mend(player);
			for (Player ally : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(RADIUS),
					candidate -> candidate != player && candidate.isAlive() && !candidate.isSpectator()
							&& ManipulationCombatHelper.allied(player, candidate)))
				mend(ally);
			float damage = PULSE_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
			// The pulse exploits light it already left behind; the hit itself must not re-apply Illuminated.
			SchoolHitContext exploit = SchoolDamage.context(this, player).withRole(SchoolHitContext.Role.EXPLOIT_STATE);
			for (LivingEntity target : ManipulationCombatHelper.hostileTargets(player, level, RADIUS)) {
				if (target.distanceTo(player) > RADIUS) continue;
				if (SchoolStates.has(target, SchoolState.ILLUMINATED))
					ManipulationParticles.hurt(this, target,
							SchoolDamage.attributed(level.damageSources().magic(), exploit, player), damage);
				if (!target.isAlive()) continue;
				SchoolStates.apply(player, target, SchoolState.ILLUMINATED, ILLUMINATED_TICKS);
				ManipulationVisuals.burst(level, ManipulationVisuals.Form.FLARE, target.getEyePosition(),
						target.getEyePosition(), .6, 14);
			}
		}
	}

	private static boolean gliding(Player player) {
		return !player.onGround() && !player.isInWater() && !player.isFallFlying() && !player.getAbilities().flying
				&& player.getDeltaMovement().y < 0;
	}

	private static void mend(Player body) {
		if (body.getHealth() >= body.getMaxHealth()) return;
		body.heal(PULSE_HEAL);
		ManipulationVisuals.attached(body, ManipulationVisuals.Form.LUX_MENDING, .5, 24, 1);
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (world.isClientSide || !gliding(player)) return;
		Vec3 motion = player.getDeltaMovement();
		// Refresh the wings into their gliding pose between the once-a-second pulses.
		if (player.tickCount % 10 == 0)
			ManipulationVisuals.attached(player, ManipulationVisuals.Form.RADIANT_WAKE, 1, RadiantWakeRules.CUE_TICKS,
					RadiantWakeRules.GLIDING);
		Vec3 look = player.getLookAngle();
		Vec3 horizontal = new Vec3(look.x, 0, look.z);
		Vec3 glide = new Vec3(motion.x, 0, motion.z);
		if (horizontal.lengthSqr() > 1.0E-4D) glide = glide.add(horizontal.normalize().scale(GLIDE_THRUST));
		if (glide.length() > MAX_GLIDE_SPEED) glide = glide.normalize().scale(MAX_GLIDE_SPEED);
		player.setDeltaMovement(glide.x, Math.max(MAX_FALL_SPEED, motion.y), glide.z);
		player.hurtMarked = true;
		player.resetFallDistance();
	}
}
