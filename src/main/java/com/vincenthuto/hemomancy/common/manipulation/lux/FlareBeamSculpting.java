package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
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
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Hematic Flare under held Projection: a searchlight. A beam of light runs from the hand along the look to the first
 * block in its way, and goes wherever the caster turns. Every body it passes over that the caster may harm is
 * Illuminated, which strips concealment, and stays outlined for a few seconds after the beam has moved on. Held steady
 * on one body the beam builds heat there; the attack button spends the heat as a flash that wounds and blinds that
 * body. The beam itself does not wound.
 */
public final class FlareBeamSculpting implements VerbSculpting {
	/** One caster's beam: the body it is heating, how hot, and when it last marked each body and last flashed. */
	private static final class Beam {
		int focus = -1;
		double heat;
		long lastFlash = Long.MIN_VALUE / 2;
		final Map<Integer, Long> marked = new HashMap<>();
	}

	private static final Map<UUID, Beam> BEAMS = new HashMap<>();
	private static final int CUE_TICKS = DEFAULT_CADENCE + 8;

	@Override
	public Set<VerbTargetKind> wants() {
		return EnumSet.of(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK, VerbTargetKind.AIR);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return !(target instanceof VerbTarget.Block block) || !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public double bloodPerPulse() {
		return FlareBeamRules.BLOOD_PER_PULSE;
	}

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
		return HeldSkills.pace(player, FlareBeamRules.PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		BEAMS.put(player.getUUID(), new Beam());
		player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.4F, 1.8F);
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		Beam beam = BEAMS.get(player.getUUID());
		if (beam == null) return;
		long now = player.level().getGameTime();
		List<LivingEntity> lit = lit(player);
		for (LivingEntity body : lit) {
			Long last = beam.marked.get(body.getId());
			if (last != null && now - last < FlareBeamRules.MARK_LOCKOUT) continue;
			beam.marked.put(body.getId(), now);
			SchoolStates.apply(player, body, SchoolState.ILLUMINATED, FlareBeamRules.ILLUMINATED_TICKS, 1);
			body.addEffect(new MobEffectInstance(MobEffects.GLOWING, FlareBeamRules.OUTLINE_TICKS, 0, false, false, false), player);
		}
		// Heat builds on the nearest body in the beam while it stays the same one.
		LivingEntity nearest = lit.stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
		if (nearest != null && (beam.focus == -1 || beam.heat <= 0.0D)) beam.focus = nearest.getId();
		boolean same = nearest != null && nearest.getId() == beam.focus;
		beam.heat = FlareBeamRules.heat(beam.heat, same);
		if (beam.heat <= 0.0D && !same) beam.focus = -1;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		Beam beam = BEAMS.get(player.getUUID());
		if (beam != null) show(player, beam, CUE_TICKS);
	}

	/** The beam: a keyed cue on the caster. Its size is the beam's reach; the heat rides in the cue's far point. */
	private static void show(ServerPlayer player, Beam beam, int ticks) {
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND_ACCENT, new Vec3(beam.heat, 0.0D, 0.0D), reach(player), ticks,
				StrandCue.encodeAccent(StrandCue.Accent.BEAM, EnumBloodTemper.LUX, EnumBloodTemper.FLAMMEUS));
	}

	@Override
	public boolean accent(ServerPlayer player) {
		Beam beam = BEAMS.get(player.getUUID());
		BloodManipulation flare = manipulation(player);
		if (beam == null || flare == null || !(player.level() instanceof ServerLevel level)) return false;
		long now = level.getGameTime();
		if (now - beam.lastFlash < FlareBeamRules.FLASH_COOLDOWN || !FlareBeamRules.mayFlash(beam.heat)) return false;
		if (!(level.getEntity(beam.focus) instanceof LivingEntity target) || !target.isAlive() || !lit(player).contains(target)) return false;
		float damage = HeldSkills.accentDamage(player,
				(float) (FlareBeamRules.flash(beam.heat) * SkillPointHelper.getCrimsonMasteryMultiplier(player)));
		target.invulnerableTime = 0;
		try (var cast = SchoolDamage.cast(flare, player, 1)) {
			ManipulationCombatHelper.hurt(flare, player, target, level, damage,
					HeldSkills.accentState(player, FlareBeamRules.FLASH_ILLUMINATED_TICKS), 1);
		}
		target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, FlareBeamRules.BLIND_TICKS, 0, false, true, true), player);
		Vec3 at = target.getBoundingBox().getCenter();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, at, at.add(player.getLookAngle()), 1.0D + beam.heat, 14,
				StrandCue.encodeAccent(StrandCue.Accent.PIERCE, EnumBloodTemper.LUX, EnumBloodTemper.FLAMMEUS));
		level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.7F);
		beam.heat = 0.0D;
		beam.lastFlash = now;
		show(player, beam, CUE_TICKS);
		return true;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Beam beam = BEAMS.remove(player.getUUID());
		if (beam != null) show(player, beam, 0);
	}

	public static double reach(Player player) {
		return VerbReach.of(player, Verb.PROJECT) * FlareBeamRules.REACH_SCALE * HeldSkills.reach(player);
	}

	/** Where the beam ends: the first block in the look's way, or its full reach. */
	public static Vec3 end(Player player) {
		Vec3 eye = player.getEyePosition();
		Vec3 far = eye.add(player.getLookAngle().scale(reach(player)));
		return player.level().clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
	}

	/** Every body the caster may harm that the beam crosses now. */
	public static List<LivingEntity> lit(Player player) {
		return ManipulationCombatHelper.swept(player, List.<Vec3[]>of(new Vec3[] {player.getEyePosition(), end(player)}), FlareBeamRules.RADIUS);
	}

	/** The heat {@code player}'s beam has built, 0 to 1, and the body it is building on (or null). */
	public static double heat(Player player) {
		Beam beam = BEAMS.get(player.getUUID());
		return beam == null ? 0.0D : beam.heat;
	}

	@Nullable
	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	public static void clearSessionState() {
		BEAMS.clear();
	}

	public static void logout(Player player) {
		BEAMS.remove(player.getUUID());
	}
}
