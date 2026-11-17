package com.vincenthuto.hemomancy.common.manipulation.tenebris;

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
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;

/**
 * Blood Eclipse under held Projection: a dome of dark the caster carries. It opens two blocks wide and grows to eight
 * over three seconds of holding, and it moves with them. Every body the caster may harm inside it is Obscured, and what
 * stands in it is hidden from a creature more than four blocks off, as in the field of dark this memory's Absorption
 * hangs ({@link DarkFields}); the caster sees through their own. The attack button ends it in a flash: everything
 * inside is struck, harder the wider the dome had grown, and no new dome opens for a moment after.
 *
 * <p>Nothing is aimed. Like a strand it opens on open air, a creature or a plain block, and never on a block that
 * takes blood, so Projection into a station with this memory selected still fills the station.
 */
public final class EclipseDomeSculpting implements VerbSculpting {
	/** Player to the ticks their dome has been held; session state, cleared with the server's. */
	private static final Map<UUID, Integer> HELD = new HashMap<>();
	/** Player to the game time their spent dark returns. */
	private static final Map<UUID, Long> SPENT = new HashMap<>();
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
	public boolean admits(ServerPlayer player, VerbTarget target) {
		Long back = SPENT.get(player.getUUID());
		return back == null || player.level().getGameTime() >= back;
	}

	@Override
	public double bloodPerPulse() {
		return EclipseDomeRules.BLOOD_PER_PULSE;
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
		return HeldSkills.pace(player, EclipseDomeRules.PACE);
	}

	@Override
	public double reachScale(Player player) {
		return HeldSkills.reach(player);
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		HELD.put(player.getUUID(), 0);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_AMBIENT, SoundSource.PLAYERS, 0.5F, 0.5F);
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		int held = HELD.merge(player.getUUID(), 1, Integer::sum);
		DarkFields.carry(player, EclipseDomeRules.radius(held), 3);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		int held = held(player);
		for (LivingEntity body : inside(player, held)) {
			SchoolStates.apply(player, body, SchoolState.OBSCURED, EclipseDomeRules.OBSCURED_TICKS, 1);
		}
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.UMBRAL_FIELD, EclipseDomeRules.radius(held), CUE_TICKS, 1);
		shell(player, EclipseDomeRules.radius(held), CUE_TICKS);
	}

	/** The dome's shell, seen from outside and in: a keyed cue on the caster, as wide as the dome is now. */
	private static void shell(ServerPlayer player, double radius, int ticks) {
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STRAND_ACCENT, player.position(), radius, ticks,
				StrandCue.encodeAccent(StrandCue.Accent.DOME, EnumBloodTemper.TENEBRIS, EnumBloodTemper.LUX));
	}

	@Override
	public boolean accent(ServerPlayer player) {
		if (!HELD.containsKey(player.getUUID()) || !(player.level() instanceof ServerLevel level)) return false;
		int held = held(player);
		BloodManipulation eclipse = manipulation(player);
		if (eclipse == null) return false;
		float damage = HeldSkills.accentDamage(player,
				(float) (EclipseDomeRules.flashDamage(held) * SkillPointHelper.getCrimsonMasteryMultiplier(player)));
		int obscured = HeldSkills.accentState(player, EclipseDomeRules.FLASH_OBSCURED_TICKS);
		try (var cast = SchoolDamage.cast(eclipse, player, 1)) {
			for (LivingEntity body : inside(player, held)) {
				ManipulationCombatHelper.hurt(eclipse, player, body, level, damage, obscured, 1);
			}
		}
		Vec3 centre = player.position().add(0.0D, 0.1D, 0.0D);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.STRAND_ACCENT, centre, centre.add(0.0D, 1.0D, 0.0D),
				EclipseDomeRules.radius(held), 24, StrandCue.encodeAccent(StrandCue.Accent.ERUPTION, EnumBloodTemper.TENEBRIS, EnumBloodTemper.LUX));
		level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.7F, 1.6F);
		SPENT.put(player.getUUID(), level.getGameTime() + EclipseDomeRules.SPENT_TICKS);
		SculptingSessions.end(player, false);
		return true;
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		HELD.remove(player.getUUID());
		DarkFields.drop(player);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.UMBRAL_FIELD, 0.0D, 0, 1);
		shell(player, 0.0D, 0);
	}

	/** Ticks {@code player}'s dome has been held, or 0 with none up. */
	public static int held(Player player) {
		return HELD.getOrDefault(player.getUUID(), 0);
	}

	/** Every body {@code player} may harm inside their dome. */
	private static List<LivingEntity> inside(ServerPlayer player, int held) {
		double radius = EclipseDomeRules.radius(held);
		Vec3 centre = player.position().add(0.0D, 1.0D, 0.0D);
		return player.level().getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius),
				body -> ManipulationCombatHelper.canHarm(player, body)
						&& EclipseDomeRules.inside(body.getBoundingBox().getCenter().distanceTo(centre), held));
	}

	private static BloodManipulation manipulation(Player player) {
		SculptingSessions.Session session = SculptingSessions.current(player);
		return session == null ? null : ManipulationInit.getByName(session.manipulation());
	}

	public static void clearSessionState() {
		HELD.clear();
		SPENT.clear();
	}

	public static void logout(Player player) {
		HELD.remove(player.getUUID());
		SPENT.remove(player.getUUID());
	}
}
