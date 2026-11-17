package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery;
import com.vincenthuto.hemomancy.common.manipulation.impact.CastImpacts;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Every strand hit goes through here, decided by the server from its own record of the player's position and look:
 * {@link ManipulationCombatHelper#canHarm}, the per-target lockout, the manipulation's school through a cast scope at
 * the last pulse's strength (so the hit is attributed to the caster, carries its school and shows an impact), and
 * Crimson Mastery. An ordinary hit lands as damage only; an accent also lays the school's state at its long duration
 * (shorter when the strand ran dry).
 */
final class StrandHits {
	private StrandHits() {
	}

	/**
	 * Strikes {@code target} for {@code damage} before mastery. {@code accent} lays the school's state for the spec's
	 * long duration and may land inside the lockout (it clears the hurt cooldown for its own hit). Returns whether the
	 * hit landed. {@code lockouts} is the per-target lockout map it reads: the twin lash's sweeps keep their own, so
	 * the lead's next sweep is not used up by the twin's.
	 */
	static boolean strike(ServerPlayer player, StrandSession session, LivingEntity target, float damage, boolean accent,
			Delivery delivery, @Nullable Vec3 travel, Map<Integer, Long> lockouts) {
		if (!(player.level() instanceof ServerLevel level) || damage <= 0.0F) return false;
		if (!ManipulationCombatHelper.canHarm(player, target)) return false;
		long now = level.getGameTime();
		if (!accent && !StrandSession.mayHit(lockouts, target.getId(), now)) return false;
		if (accent) target.invulnerableTime = 0;
		// Whipcrack: an accent bites harder.
		double whip = accent ? StrandSkills.accentDamage(session.whipcrack) : 1.0D;
		float amount = (float) (damage * whip * session.sculpting.spec().damageScale()
				* SkillPointHelper.getCrimsonMasteryMultiplier(player));
		boolean landed;
		try (var cast = SchoolDamage.cast(session.manipulation, player, 1.0F, session.strength);
				var shown = CastImpacts.deliveredAs(delivery, travel)) {
			SchoolHitContext hit = SchoolDamage.context(session.manipulation, player).newAttack()
					.withRole(accent ? SchoolHitContext.Role.APPLY_STATE : SchoolHitContext.Role.DAMAGE_ONLY)
					.withApplication(accent ? stateTicks(session) : 0, 1);
			DamageSource source = SchoolDamage.attributed(level.damageSources().magic(), hit, player);
			landed = ManipulationParticles.hurt(session.manipulation, target, source, amount);
		}
		if (landed) StrandSession.hit(lockouts, target.getId(), now);
		return landed;
	}

	/** {@link #strike} against the strand's own lockouts. */
	static boolean strike(ServerPlayer player, StrandSession session, LivingEntity target, float damage, boolean accent,
			Delivery delivery, @Nullable Vec3 travel) {
		return strike(player, session, target, damage, accent, delivery, travel, session.lastHit);
	}

	/**
	 * A tap's blow on {@code target}: {@code damage} (the profile's tap damage, or a tail's share of it), raised by
	 * Whipcrack. It wounds only, unless {@code stateTicks} is above 0, when it lays the school's state for that long
	 * (Whipcrack lengthens it). {@code session} is the tap's own, opened for the one tap.
	 */
	static boolean tap(ServerPlayer player, StrandSession session, LivingEntity target, float damage, int stateTicks, Vec3 travel) {
		if (!(player.level() instanceof ServerLevel level) || !ManipulationCombatHelper.canHarm(player, target)) return false;
		boolean lays = stateTicks > 0;
		float amount = (float) (damage * StrandSkills.accentDamage(session.whipcrack)
				* session.sculpting.spec().damageScale() * SkillPointHelper.getCrimsonMasteryMultiplier(player));
		int ticks = (int) Math.round(stateTicks * StrandSkills.accentState(session.whipcrack));
		// A tap is paid for and paced by its own cooldown: the body's hurt cooldown does not swallow it.
		target.invulnerableTime = 0;
		try (var cast = SchoolDamage.cast(session.manipulation, player, 1.0F, session.strength);
				var shown = CastImpacts.deliveredAs(Delivery.PROJECTILE, travel)) {
			SchoolHitContext hit = SchoolDamage.context(session.manipulation, player).newAttack()
					.withRole(lays ? SchoolHitContext.Role.APPLY_STATE : SchoolHitContext.Role.DAMAGE_ONLY)
					.withApplication(lays ? ticks : 0, 1);
			DamageSource source = SchoolDamage.attributed(level.damageSources().magic(), hit, player);
			return ManipulationParticles.hurt(session.manipulation, target, source, amount);
		}
	}

	/** Lays the school's state on {@code target} without a blow (a brand setting). */
	static boolean mark(ServerPlayer player, StrandSession session, LivingEntity target) {
		if (!ManipulationCombatHelper.canHarm(player, target)) return false;
		try (var cast = SchoolDamage.cast(session.manipulation, player, 1.0F, session.strength)) {
			return SchoolStates.apply(player, target, SchoolStates.primaryState(session.manipulation.getTend()),
					stateTicks(session), 1);
		}
	}

	/** The accent's state duration: the spec's long duration, lengthened by Whipcrack, cut by a dry strand's strength. */
	static int stateTicks(StrandSession session) {
		return Math.max(20, (int) Math.round(session.sculpting.spec().stateTicks() * StrandSkills.accentState(session.whipcrack)
				* session.strength * session.stateScale));
	}
}
