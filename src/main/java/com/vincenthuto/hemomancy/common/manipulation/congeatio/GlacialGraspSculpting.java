package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.HeldSkills;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Glacial Grasp under held Projection: a hand of ice that closes. Held on a creature, frost rises round it as a hand
 * over a second; once formed the hand holds the creature where it stands and drives a Rime level into it each pulse.
 * The hand keeps its creature wherever the caster then looks. The attack button squeezes: the hand shatters and the
 * creature takes damage that grows with how long it was held. A boss is slowed, not held.
 *
 * <p>It claims only a creature the caster may harm. Water still freezes under this memory's narrow Sculpting, which is
 * tried first.
 */
public final class GlacialGraspSculpting implements VerbSculpting {
	/** One caster's grip: the creature in the hand and the ticks it has been there. */
	private static final class Grip {
		final int target;
		int held;

		Grip(int target) {
			this.target = target;
		}
	}

	private static final Map<UUID, Grip> GRIPS = new HashMap<>();
	/** Player to the game time a new hand may form after a squeeze. */
	private static final Map<UUID, Long> SPENT = new HashMap<>();
	private static final int CUE_TICKS = DEFAULT_CADENCE + 8;

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.CREATURE);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Creature creature && ManipulationCombatHelper.canHarm(player, creature.living());
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		Long back = SPENT.get(player.getUUID());
		return back == null || player.level().getGameTime() >= back;
	}

	@Override
	public double bloodPerPulse() {
		return GlacialGraspRules.BLOOD_PER_PULSE;
	}

	/** It draws itself: the hand pours no ordinary stream toward the aim while this holds the channel. */
	@Override
	public boolean drawsOwnStream() {
		return true;
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public boolean takesAccent() {
		return true;
	}

	@Override
	public OptionalDouble movement(Player player) {
		return HeldSkills.pace(player, GlacialGraspRules.PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Creature creature)) return;
		GRIPS.put(player.getUUID(), new Grip(creature.living().getId()));
		player.level().playSound(null, creature.living().blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.8F, 0.6F);
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		Grip grip = GRIPS.get(player.getUUID());
		LivingEntity target = gripped(player);
		if (grip == null || target == null) {
			SculptingSessions.end(player, false);
			return;
		}
		grip.held++;
		if (!GlacialGraspRules.holds(grip.held) || HemoEntityPredicates.isBoss(target)) return;
		// Held: it does not walk, and what pushes it does not carry it.
		Vec3 motion = target.getDeltaMovement();
		target.setDeltaMovement(0.0D, Math.min(0.0D, motion.y), 0.0D);
		if (target instanceof Mob mob) mob.getNavigation().stop();
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		Grip grip = GRIPS.get(player.getUUID());
		LivingEntity held = gripped(player);
		if (grip == null || held == null) return;
		show(player, held, CUE_TICKS);
		if (!GlacialGraspRules.holds(grip.held)) return;
		SchoolStates.apply(player, held, SchoolState.RIME, GlacialGraspRules.RIME_TICKS, 1);
		ManipulationVisuals.attached(held, ManipulationVisuals.Form.FROZEN_VEINS, held.getBbWidth(), CUE_TICKS, 1);
		boolean boss = HemoEntityPredicates.isBoss(held);
		held.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, GlacialGraspRules.HOLD_TICKS,
				boss ? GlacialGraspRules.BOSS_SLOW_AMPLIFIER : 9, false, false, true), player);
	}

	@Override
	public boolean accent(ServerPlayer player) {
		Grip grip = GRIPS.get(player.getUUID());
		LivingEntity target = gripped(player);
		if (grip == null || target == null || !(player.level() instanceof ServerLevel level)) return false;
		float squeeze = GlacialGraspRules.squeeze(grip.held);
		BloodManipulation grasp = manipulation(player);
		if (squeeze <= 0.0F || grasp == null) return false;
		float damage = HeldSkills.accentDamage(player, (float) (squeeze * SkillPointHelper.getCrimsonMasteryMultiplier(player)));
		target.invulnerableTime = 0;
		try (var cast = SchoolDamage.cast(grasp, player, 1)) {
			ManipulationCombatHelper.hurt(grasp, player, target, level, damage, HeldSkills.accentState(player, GlacialGraspRules.RIME_TICKS),
					GlacialGraspRules.SQUEEZE_RIME);
		}
		Vec3 centre = target.getBoundingBox().getCenter();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.CRUOR_BREAK, centre, centre.add(0.0D, 0.5D, 0.0D),
				Math.max(0.8D, target.getBbWidth()), 18);
		level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
		SPENT.put(player.getUUID(), level.getGameTime() + GlacialGraspRules.SPENT_TICKS);
		SculptingSessions.end(player, false);
		return true;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Grip grip = GRIPS.remove(player.getUUID());
		if (grip != null && player.level().getEntity(grip.target) instanceof LivingEntity target) show(player, target, 0);
	}

	/** The hand round {@code target}: a keyed cue on the creature, refreshed each pulse and retired with a zero life. */
	private static void show(ServerPlayer player, LivingEntity target, int ticks) {
		ManipulationVisuals.attached(target, ManipulationVisuals.Form.STRAND_ACCENT, player.position(),
				Math.max(0.5D, target.getBbWidth()), ticks, StrandCue.encodeAccent(StrandCue.Accent.GRASP, EnumBloodTemper.CONGEATIO, null));
	}

	/**
	 * The creature in {@code player}'s hand, or null once it is dead, gone, no longer theirs to harm, or carried past
	 * the hand's leash.
	 */
	@Nullable
	public static LivingEntity gripped(Player player) {
		Grip grip = GRIPS.get(player.getUUID());
		if (grip == null || !(player.level().getEntity(grip.target) instanceof LivingEntity target) || !target.isAlive()) return null;
		if (!ManipulationCombatHelper.canHarm(player, target)) return null;
		double leash = VerbReach.of(player, Verb.PROJECT) * HeldSkills.reach(player) * GlacialGraspRules.LEASH + target.getBbWidth();
		return player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()) <= leash ? target : null;
	}

	/** Ticks {@code player}'s hand has been on its creature, or 0 with none. */
	public static int held(Player player) {
		Grip grip = GRIPS.get(player.getUUID());
		return grip == null ? 0 : grip.held;
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	public static void clearSessionState() {
		GRIPS.clear();
		SPENT.clear();
	}

	public static void logout(Player player) {
		GRIPS.remove(player.getUUID());
		SPENT.remove(player.getUUID());
	}
}
